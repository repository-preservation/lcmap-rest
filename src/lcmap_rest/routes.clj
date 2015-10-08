(ns lcmap-rest.routes
  (:require [clojure.tools.logging :as log]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [GET context defroutes]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [lcmap-rest.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.management :as management]))

;; XXX for now use straight-up Compojure; switch to Liberator for the
;; convenience of handlers, content negotiation, etc. We'll also need
;; routes-per-version a la the accept header, e.g.:
;; Accept: application/vnd.usgs-lcmap.v1+json

(defroutes surface-reflectance
  (context l8-sr/context []
    (GET "/" req
      (l8-sr/get-resources (:uri req)))
    (GET "/tiles" [point extent time band :as req]
      (l8-sr/get-tiles point extent time band req))
    (GET "/rod" [point time band :as req]
      (l8-sr/get-rod point time band req))))

;; XXX this needs to go into a protected area; see this ticket:
;;  https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes management
  (context "/manage" []
    (GET "/status" [] (management/get-status))
    ;; XXX add more management resources
    ))

(defroutes v1
  surface-reflectance
  management
  (route/not-found "Resource not found"))

(defroutes app
  (-> #'v1
      (handler/site)
      (wrap-json-response)
      ;; Once we support SSL, site-defaults needs to be changed to
      ;; secure-site-defaults
      (wrap-defaults api-defaults)))

