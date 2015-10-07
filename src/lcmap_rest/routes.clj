(ns lcmap-rest.routes
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [lcmap-rest.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.management :as management]))

;; XXX for now use straight-up Compojure; switch to Liberator for the
;; convenience of handlers, content negotiation, etc. We'll also need
;; routes-per-version a la the accept header, e.g.:
;; Accept: application/vnd.usgs-lcmap.v1+json

(defroutes v1
  (GET "/L1/T/Landsat/8/SurfaceReflectance" [] (l8-sr/get-resource-children))
  (GET "/L1/T/Landsat/8/SurfaceReflectance/tiles" [] (l8-sr/get-tiles))
  (GET "/L1/T/Landsat/8/SurfaceReflectance/rod" [] (l8-sr/get-rod))
  ;; XXX this needs to go into a protected area
  (GET "/manage/status" [] (management/get-status)))

;; XXX add app + handler(s)

;; XXX add query-param wrapper for extraction
