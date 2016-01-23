(ns lcmap-rest.api.data.surface-reflectance
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :refer [response]]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.tile.db :as tile-db]
            [lcmap-client.data.surface-reflectance]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  (response
   {:links
    (map (fn [x]
           (str context x))
         ["/tiles" "/rod"])}))

(defn get-tiles
  ""
  [band point time system]
  (let [[x y] (map #(Integer/parseInt %) (re-seq #"\-?\d+" point))
        results (tile-db/find-tiles band x y time system)]
    results))

(defn get-rod
  ""
  [band point time system]
  (str "point: " point ", time: " time ", band: " band))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.data.surface-reflectance/context []
    (GET "/" request
      (get-resources (:uri request)))
    (GET "/tiles" [band point time :as request]
      (get-tiles band point time (httpd/tiledb-key request)))
    (GET "/rod" [band point time :as request]
      (get-rod point time band request (httpd/tiledb-key request)))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
