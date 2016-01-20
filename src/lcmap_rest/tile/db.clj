(ns lcmap-rest.tile.db
  (:require [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :refer :all]))

(defn find-spec [ubid system]
  (let [conn    (get-in system [:tiledb :conn])
        table   (get-in system [:tiledb :spec-table])
        params  {:ubid ubid}
        spec    (cql/select conn table params)]
    spec))

(defn find-tiles [spec x y acquired system]
  (let [results []]
    results))

(defn pixel-xform [spec x y]
  "Transform the (x,y) coordinate in the projection coordinate system (defined
  in the tile spec) to a raster grid coorindate."
  ;; XXX data type
  ;; XXX data_shape
  ;;
  )

(defn extract-pixel [spec x y tile]
  ;; XXX byte-order check
  ;; XXX GDAL data type
  ;; XXX data units
  )

(defn find-rod [spec x y acquired system]
  (map #(extract-pixel spec x y %)
       (find-tiles spec x y acquired system)))
