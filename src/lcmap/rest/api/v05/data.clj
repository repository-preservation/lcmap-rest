(ns lcmap.rest.api.v05.data
  (:require [clojure.tools.logging :as log]
            [clj-time.format :as time-fmt]
            [clj-time.coerce :as time-coerce]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.accept :refer [defaccept]]
            [ring.util.response :as ring-resp]
            [lcmap.client.data :as data]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.util.gdal :as gdal]
            [lcmap.data.scene :as tile-scene]
            [lcmap.data.tile :as tile]
            [lcmap.data.tile-spec :as tile-spec])
  (:import [org.apache.commons.codec.binary Base64]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn base64-decode
  "Helper for saving tiles sent as JSON."
  [tile]
  (assoc tile :data (Base64/decodeBase64 (tile :data))))

(defn base64-encode
  "Helper for responding to tile requests with JSON."
  [src-data]
  (let [size (- (.limit src-data) (.position src-data))
        copy (byte-array size)]
    (.get src-data copy)
    (Base64/encodeBase64String copy)))

(defn point->pair
  "Convert a point x,y into a pair (a seq of ints)"
  [point]
  (map #(Integer/parseInt %) (re-seq #"\-?\d+" point)))

(defn iso8601->datetimes
  "Convert an ISO8601 string into a pair of DateTime"
  [iso8601]
  (let [parse #(time-fmt/parse (time-fmt/formatters :date) %)
        dates (clojure.string/split iso8601 #"/")]
    (map parse dates)))

(defn date->iso8601
  "Convert a DateTime to an ISO8601 string"
  [date]
  (time-fmt/unparse (time-fmt/formatters :date-time-no-ms)
                    (time-coerce/from-date date)))

;;; Response Formats ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; This is a preliminary step, this relies on the http-util json-handler
;; to transform the response body into a JSON string.
(defn to-json [response]
  (let [tile-list (get-in response [:body :result :tiles])
        tile-base64 (map #(assoc %
                                 :data (base64-encode (% :data))
                                 :acquired (date->iso8601 (% :acquired)))
                         tile-list)]
    (log/debug "Base64 encoding tile response")
    (-> response
        (assoc-in [:body :result :tiles] tile-base64)
        (assoc-in [:headers "Content-Disposition"] "attachment; filename=\"nice.json\""))))

(defn to-netcdf [response]
  (let [tile-spec (get-in response [:body :result :spec])
        tile-list (get-in response [:body :result :tiles])
        driver (gdal/subtype->driver "netcdf")
        file (clojure.java.io/file "temp.nc")]
    {:body (gdal/create-with-gdal file driver tile-spec tile-list)
     :headers {"Content-Disposition" "attachment; filename=\"nice.nc\""}
     :status 200}))

(defn to-geotiff [response]
  (let [tile-spec (get-in response [:body :result :spec])
        tile-list (get-in response [:body :result :tiles])
        driver (gdal/subtype->driver "tiff")
        file (clojure.java.io/file "temp.tiff")]
    {:body (gdal/create-with-gdal file driver tile-spec tile-list)
     :headers {"Content-Disposition" "attachment; filename=\"nice.tiff\""}
     :status 200}))

;; This macro calls the function matching the media-type given in the
;; parsed accept headers of the request map.
(defaccept respond-to
  "application/vnd.usgs.lcmap.v0.5+json" to-json
  "application/vnd.usgs.lcmap.v0.5+geotiff" to-geotiff
  "application/vnd.usgs.lcmap.v0.5+netcdf" to-netcdf)

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources
  "Provide links to sub-resources of the data resource"
  [context]
  (log/info (str "get-resources: " context))
  {:links (map #(str context %) ["/tiles" "/rod" "/specs" "/scenes"])})

(defn get-tiles
  "Find tiles (and related tile-spec) containing given point during time."
  [band point time db]
  (let [[x y]   (point->pair point)
        times   (iso8601->datetimes time)
        spec    (first (tile-spec/find db {:ubid band}))
        results (tile/find db {:ubid band :x x :y y :acquired times})]
    (log/debug "GET tiles" band x y times (count results))
    {:spec spec :tiles results}))

(defn save-tile
  "Create a tile"
  [request db]
  (let [tile (-> request :body :tile)
        band (:ubid tile)
        spec (first (tile-spec/find db {:ubid band}))
        keyspace (:keyspace_name spec)
        table (:table_name spec)]
    (log/debug "POST tile" (dissoc tile :data))
    (tile/save db keyspace table (base64-decode tile))))

(defn get-specs
  "Get spec for given band (UBID)"
  [band db]
  (log/debug "GET spec" band)
  (tile-spec/find db {:ubid band}))

(defn get-scenes
  "Get scene metadata for given scene ID"
  [scene db]
  (log/debug "GET scene" scene)
  (distinct (tile-scene/find db {:source scene})))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  ;; XXX the http-util response helper doesn't work with the threading
  ;; macro; each of the underlying functions returns a data structure
  ;; that is oblivious to the response body structure, so an explicity
  ;; rearrangement is performed.
  (context lcmap.client.data/context []
    (GET "/" request
         (http/response :result (get-resources (:uri request))))
    (GET "/tiles" [band point time :as request]
         (->> (get-tiles band point time (get-in request [:component :tiledb]))
              (assoc {} :result)
              (ring-resp/response)
              (respond-to request)))
    (POST "/tiles" [:as request]
          (->> (save-tile request (get-in request [:component :tiledb]))
               (assoc {} :result)
               (ring-resp/response)))
    (GET "/specs" [band :as request]
         (->> (get-specs band (get-in request [:component :tiledb]))
              (assoc {} :result)
              (ring-resp/response)))
    (GET "/scenes" [scene :as request]
         (->> (get-scenes scene (get-in request [:component :tiledb]))
              (assoc {} :result)
              (ring-resp/response)))
    (POST "/tiles" [:as request]
          (->> (save-tile request (get-in request [:component :tiledb]))
               (assoc {} :result)
               (ring-resp/response)))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
