(ns lcmap-rest.tile.util
  (:require [clojure.tools.logging :as log]
            [gdal.core :as gdal-core]
            [dire.core :refer [with-handler!]])
  (:import [org.gdal.gdal gdal]))

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
