(ns lcmap-rest.routes
  (:require [compojure.core :refer [GET context defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [lcmap-rest.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.management :as management]))

;; XXX for now use straight-up Compojure; switch to Liberator for the
;; convenience of handlers, content negotiation, etc. We'll also need
;; routes-per-version a la the accept header, e.g.:
;; Accept: application/vnd.usgs-lcmap.v1+json

(defroutes surface-reflectance
  (context "/api/L1/T/Landsat/8/SurfaceReflectance" []
    (GET "/" [] (l8-sr/get-resources))
    (GET "/tiles" [] (l8-sr/get-tiles))
    (GET "/rod" [] (l8-sr/get-rod))))

;; XXX this needs to go into a protected area
(defroutes management
  (context "/manage" []
    (GET "/status" [] (management/get-status))))

(defroutes v1
  surface-reflectance
  management
  (route/not-found "Resource not found"))

(defroutes app
  (handler/site v1))

;; XXX add query-param wrapper for extraction
