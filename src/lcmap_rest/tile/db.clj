(ns lcmap-rest.tile.db
  (:require [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]))

(defn find-spec
  "Retrieve the tile spec for the given area. Useful for determining
   what keyspace and table contain a set of tiles."
  [ubid tiledb]
  (let [conn    (:conn tiledb)
        table   (:spec-table tiledb)
        params  {:ubid ubid}
        _       (cql/use-keyspace conn "lcmap")
        spec    (cql/select conn "tile_specs" params)]
    (first spec)))

(defn snap
  "Transform an arbitrary projection system coordinate (x,y) into the
   coordinate of the tile that contains it."
  [x y spec]
  (let [{:keys [:tile_x :tile_y :shift_x :shift_y]} spec
        tx (+ shift_x (- x (mod x tile_x)))
        ty (+ shift_y (- y (mod y tile_y)))]
    ;;; XXX
    ;;; The combination of int and float types in the tile spec
    ;;; makes it necessary to explicitly cast.
    [(int tx) (int ty)]))

(defn find-tiles
  "Query DB for all tiles that match the UBID and contain (x,y)"
  ;; XXX
  ;; This function does not currently handle different set of tiles
  ;; for different areas of interest in different projectionss: e.g.
  ;; Hawaii or Alaska.
  [ubid x y acquired tiledb]
  (let [conn    (:conn tiledb)
        spec    (find-spec ubid tiledb)
        ks      (:keyspace_name spec)
        table   (:table_name spec)
        [tx ty] (snap x y spec)
        where   (query/where {:ubid ubid :x tx :y ty})
        _       (cql/use-keyspace conn ks)
        results (cql/select conn table where)]
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

(defn find-rod [spec x y acquired tiledb]
  (map #(extract-pixel spec x y %)
       (find-tiles spec x y acquired tiledb)))
