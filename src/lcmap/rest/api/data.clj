(ns lcmap.rest.api.data
  (:require [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [clj-time.format :as time-fmt]
            [clj-time.coerce :as time-coerce]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.rest.middleware.http-util :as util]
            [lcmap.client.data]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.data.scene :as tile-scene]
            [lcmap.data.tile :as tile]
            [lcmap.data.tile-spec :as tile-spec])
  (:import [org.apache.commons.codec.binary Base64]))

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  {:links (map #(str context %) ["/tiles" "/specs" "/scenes"])})

(defn get-tiles
  ""
  [band point time db]
  (let [[x y]   (point->pair point)
        times   (iso8601->datetimes time)
        spec    (first (tile-spec/find db {:ubid band}))
        results (tile/find db {:ubid band :x x :y y :acquired times})]
    (log/debug "GET tiles" band x y times (count results))
    {:spec spec :tiles results}))

(defn save-tile
  ""
  [request db]
  (let [tile (-> request :body :tile)
        band (:ubid tile)
        spec (first (tile-spec/find db {:ubid band}))
        keyspace (:keyspace_name spec)
        table (:table_name spec)]
    ;; XXX
    (log/debug "POST tile" (dissoc tile :data))
    (tile/save db keyspace table tile)))

(defn get-specs
  ""
  [band db]
  (log/debug "GET spec" band)
  (tile-spec/find db {:ubid band}))

(defn get-scenes
  ""
  [scene db]
  (log/debug "GET scene" scene)
  (distinct (tile-scene/find db {:source scene})))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.data/context []
    (GET "/" request
      (http/response :result
        (get-resources (:uri request))))
    (GET "/tiles" [band point time :as request]
      (http/response :result
        (get-tiles band point time (get-in request [:component :tiledb]))))
    (POST "/tiles" [:as request]
      (http/response :result
                     (save-tile request (get-in request [:component :tiledb]))))
    (GET "/specs" [band :as request]
      (http/response :result
                     (get-specs band (get-in request [:component :tiledb]))))
    (GET "/scenes" [scene :as request]
      (http/response :result
                     (get-scenes scene (get-in request [:component :tiledb]))))
    (GET "/scenes/:scene" [scene :as request]
      (http/response :result
                     (get-scenes scene (get-in request [:component :tiledb]))))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
