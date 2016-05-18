(ns lcmap.rest.api.data
  (:require [clojure.tools.logging :as log]
            [clj-time.format :as time-fmt]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.rest.middleware.http-util :as util]
            [lcmap.client.data]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.data.tile-spec :as tile-spec]
            [lcmap.data.tile :as tile])
  (:import [org.apache.commons.codec.binary Base64]))


;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; this mutates the buffer by reading it...

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

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  {:links (map #(str context %) ["/tiles" "/rod"])})

(defn get-tiles
  ""
  [band point time db]
  (let [[x y]   (point->pair point)
        times   (iso8601->datetimes time)
        spec    (tile-spec/find db {:ubid band})
        results (tile/find db {:ubid band :x x :y y :acquired times})
        encoded (map #(assoc %
                             :data (base64-encode (% :data))
                             :acquired (str (% :acquired))) results)]
    (log/debug "GET tiles" band x y times (count results))
    {:spec spec :tiles encoded}))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.data/context []
    (GET "/" request
      (http/response :result
        (get-resources (:uri request))))
    (GET "/tiles" [band point time :as request]
      (http/response :result
        (get-tiles band point time (:tiledb request))))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
