(ns lcmap-rest.routes
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [lcmap-rest.management :as management]
            [lcmap-rest.surface-reflectance :as sr))

;; XXX for now use straight-up Compojure; switch to Liberator for the
;; convenience of handlers, content negotiation, etc. We'll also need
;; routes-per-version a la the accept header, e.g.:
;; Accept: application/vnd.usgs-lcmap.v1+json

(defroutes v1
  (GET "/L1/T/Landsat/8/SurfaceReflectance" [] (sr/get-resource-children))
  (GET "/L1/T/Landsat/8/SurfaceReflectance/tiles" [] (sr/get-tiles))
  (GET "/L1/T/Landsat/8/SurfaceReflectance/rod" [] (sr/get-rod))
  (GET "/manage/status" [] (management/get-status)))
