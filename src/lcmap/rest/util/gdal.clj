(ns lcmap.rest.util.gdal
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [gdal.band :as band]
            [gdal.core :as gdal]
            [gdal.dataset :as dataset]
            [gdal.driver :as driver])
  (:import [org.gdal.gdalconst gdalconst]))

(def ->gdal-type {"INT16" gdalconst/GDT_Int16
                  "UINT8" gdalconst/GDT_Byte})

(defn create-with-gdal
  ""
  [file gdal-driver tile-spec tiles]
  (let [[xs ys]     (:data_shape tile-spec)
        zs          (count tiles)
        le-type     (-> tile-spec :data_type ->gdal-type)
        le-fill     (:data_fill tile-spec)
        le-scale    (:data_scale tile-spec)
        le-pixel-x  (:pixel_x tile-spec)
        le-pixel-y  (:pixel_y tile-spec)
        le-path     (.getAbsolutePath file)
        le-dataset  (driver/create gdal-driver le-path xs ys zs le-type)
        le-tile     (first tiles)
        le-array    (short-array (* xs ys))]
    (dataset/set-projection-str le-dataset (:projection tile-spec))
    (dataset/set-geo-transform le-dataset [(:x le-tile) le-pixel-x 0.0 (:y le-tile) 0.0 le-pixel-y])
    ;; gdal dataset bands are not zero based indexed... so we start
    ;; and end and offset by 1
    (doseq [[band-ix tile] (zipmap (range 1 (+ 1 (count tiles))) tiles)]
      (let [le-band (dataset/get-band le-dataset band-ix)]
        (-> tile :data
            ;; XXX potential pitfall...
            ;; default byte order is big endian for buffers,
            ;; which is different than original tile data.
            (.order java.nio.ByteOrder/LITTLE_ENDIAN)
            ;; XXX this will differ for masks (which are bytes)
            (.asShortBuffer)
            (.get le-array))
        (band/set-no-data-value le-band le-fill)
        (band/set-scale le-band le-scale)
        (band/write-raster le-band 0 0 xs ys le-array)
        (band/flush-cache le-band)))
    (dataset/flush-cache le-dataset)
    (dataset/delete le-dataset) ;; mandatory, otherwise file may not have data
    file))

;; use content subtype to determine driver...
;; Not all GDAL drivers have a mime-type so we have to define
;; an explicit map of content subtypes to driver names.
(def mapping {"netcdf" "NetCDF"
              "tiff"   "GTiff"
              "envi"   "ENVI"
              "erdas"  "HFA"})

(defn subtype->driver
  ""
  [subtype]
  (-> subtype
      clojure.string/lower-case
      mapping
      gdal/get-driver-by-name))
