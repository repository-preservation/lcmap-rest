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


;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn base64-decode [tile]
  (assoc tile :data (Base64/decodeBase64 (tile :data))))

(defn base64-encode [src-data]
  (let [size (- (.limit src-data) (.position src-data))
        copy (byte-array size)]
    (.get src-data copy)
    (Base64/encodeBase64String copy)))

(defn point->pair
  "Convert a point x,y into a pair (a seq of ints)"
  [point]
  (map #(Integer/parseInt %) (re-seq #"\-?\d+" point)))

(defn iso8601->datetimes
  "Convert an ISO8610 string into a pair of DateTime"
  [iso8601]
  (let [parse #(time-fmt/parse (time-fmt/formatters :date) %)
        dates (clojure.string/split iso8601 #"/")]
    (map parse dates)))

(defn date->iso8601 [date]
  (time-fmt/unparse (time-fmt/formatters :date-time-no-ms)
                    (time-coerce/from-date date)))

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  {:links (map #(str context %) ["/tiles" "/rod"])})

(defn get-tiles
  ""
  [band point time db]
  (let [[x y]   (point->pair point)
        times   (iso8601->datetimes time)
        spec    (first (tile-spec/find db {:ubid band}))
        results (tile/find db {:ubid band :x x :y y :acquired times})
        encoded (map #(assoc %
                             :data (base64-encode (% :data))
                             :acquired (date->iso8601 (% :acquired))) results)]
    (log/debug "GET tiles" band x y times (count results))
    {:spec spec :tiles encoded}))

(defn save-tile
  ""
  [request db]
  (let [tile (-> request :body :tile)
        band (:ubid tile)
        spec (first (tile-spec/find db {:ubid band}))
        keyspace (:keyspace_name spec)
        table (:table_name spec)]
    (log/debug "POST tile" (dissoc tile :data))
    (tile/save db keyspace table (base64-decode tile))))

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
    (GET "/scenes/:scene" [scene :as request]
      (http/response :result
                     (get-scenes scene (get-in request [:component :tiledb]))))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
