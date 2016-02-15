(ns lcmap-rest.api.data.surface-reflectance
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :refer [response]]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.tile.db :as tile-db]
            [lcmap-client.data.surface-reflectance]
            [clj-time.format :as time-fmt])
  (:import [org.apache.commons.codec.binary Base64]))

;; this mutates the buffer by reading it...
(defn base64-encode [src-data]
  (let [size (- (.limit src-data) (.position src-data))
        copy (byte-array size)]
    (.get src-data copy)
    (Base64/encodeBase64String copy)))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  (response
   {:links
    (map (fn [x]
           (str context x))
         ["/tiles"])}))

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

(defn get-tiles
  ""
  [band point time system]
  (let [[x y]   (point->pair point)
        times   (iso8601->datetimes time)
        spec    (tile-db/find-spec band system)
        results (tile-db/find-tiles band x y times system)
        encoded (map #(assoc % :data (base64-encode (% :data))) results)]
    (log/info "GET tiles" band x y times (count results))
    (response {:result {:spec spec :tiles encoded}})))

(comment "Not ready for use."
  (defn get-rod
  "NOT IMPLEMENTED"
  [band point time system]
  (let [[x y]   (point->pair point)
        times   (iso8601->datetimes time)
        spec    (tile-db/find-spec band system)
        results (tile-db/find-rod band x y times system)
        encoded (map #(assoc % :data (base64-encode (% :data))) results)]
    (log/info "GET rod" band x y times (count results))
    (response {:result {:spec spec :rod []}}))))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.data.surface-reflectance/context []
    (GET "/" request
      (get-resources (:uri request)))
    (GET "/tiles" [band point time :as request]
         (get-tiles band point time (httpd/tiledb-key request)))
    (comment "not ready for use" ;; XXX Respond with appropriate status code?
      (GET "/rod" [band point time :as request]
         (get-rod band point time (httpd/tiledb-key request))))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
