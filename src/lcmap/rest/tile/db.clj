(ns lcmap.rest.tile.db
  (:require [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [gdal.core :as gdal-core]
            [dire.core :refer [with-handler!]])
  (:import [org.gdal.gdal gdal]))

(defn find-spec
  "Retrieve the tile spec for the given area. Useful for determining
   what keyspace and table contain a set of tiles."
  [ubid tiledb]
  (let [conn    (:conn tiledb)
        table   (:spec-table tiledb)
        params  (query/where {:ubid ubid})
        _       (cql/use-keyspace conn "lcmap")
        specs   (cql/select conn "tile_specs" params)
        spec    (first specs)]
    ;; XXXs
    ;; This value is not properly transformed by Cassaforte!!!
    ;; ...so we have to fake it for now.
    (assoc spec :data_shape [256 256])))

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
        [t1 t2] acquired
        where   (query/where [[= :ubid ubid]
                              [= :x tx]
                              [= :y ty]
                              [>= :acquired (str t1)]
                              [< :acquired (str t2)]])
        columns (query/columns :ubid
                               :x
                               :y
                               :acquired
                               :source
                               :data)
        _       (cql/use-keyspace conn ks)]
    (cql/select conn table columns where)))

(defn inverse-matrix
  "Transform the (x,y) coordinate in the projection coordinate system (defined
  in the tile spec) to a raster grid coorindate."
  [spec x y tile]
  (let [tile-x   (double (:x tile))
        tile-y   (double (:y tile))
        pixel-x  (:pixel_x spec)
        pixel-y  (:pixel_y spec)
        rot-x    0.0
        rot-y    0.0
        params  (double-array [tile-x pixel-x rot-x tile-y rot-y pixel-y])
        inverse (double-array 6)]
    (org.gdal.gdal.gdal/InvGeoTransform params inverse)
    inverse))

(defn pixel-xform
  "Transform projection (x,y) into raster (x,y)"
  [spec x y tile]
  (let [[x1 x2 x3 y1 y2 y3] (inverse-matrix spec x y tile)
        img-x  (+ x1 (* x x2) (* y x3))
        img-y  (+ y1 (* y y2) (* y y3))
        buf-x  (int (Math/floor img-x))
        buf-y  (int (Math/floor img-y))]
    [buf-x buf-y]))

;; This assumes that the data is row-major order, values are
;; contiguous along the x axis.
(defn pixel-offset
  [spec x y]
  (let [row-size (Math/abs (/ (:tile_x spec) (:pixel_x spec)))]
    (+ x (* row-size y))))

(defn extract-pixel
  "Get single value from buffer"
  [spec x y tile]
  (let [xform  (pixel-xform spec x y tile)
        [px py] (pixel-xform spec x y tile)
        tile-pixels (apply * (spec :data_shape))
        tile-bytes (count (tile :data))
        pixel-bytes (/ tile-bytes tile-pixels)
        offset (* pixel-bytes (pixel-offset spec px py))
        pixel (subs (tile :data) offset (+ offset pixel-bytes))]
    (assoc tile :data pixel)))

(with-handler! #'extract-pixel
  java.lang.NullPointerException
  (fn [e & args] (println "Problem with tile (x,y) or spec pixel sizes")))

(defn find-rod [ubid x y acquired tiledb]
  (let [spec (find-spec ubid tiledb)]
    (map #(extract-pixel spec x y %)
         (find-tiles ubid x y acquired tiledb))))
