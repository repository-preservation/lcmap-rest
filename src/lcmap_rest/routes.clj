(ns lcmap-rest.routes
  (:require [clojure.tools.logging :as log]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [GET context defroutes]]
            [compojure.response :as response]
            [compojure.route :as route]
            [lcmap-client.core]
            [lcmap-client.l8.surface-reflectance]
            [lcmap-rest.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.management :as management]
            [lcmap-rest.util :as util]))

(declare v0)

(def default-api-version #'v0)

(defroutes surface-reflectance
  (context lcmap-client.l8.surface-reflectance/context []
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

(defroutes v0
  surface-reflectance
  management
  (route/not-found "Resource not found"))

(defroutes v1
  management
  (route/not-found "Resource not found"))

(defroutes v2
  management
  (route/not-found "Resource not found"))

(defn get-api-version [version default]
  (cond
    (and (>= version 0.0) (< version 1.0)) #'v0
    (and (>= version 1.0) (< version 2.0)) #'v1
    (and (>= version 2.0) (< version 3.0)) #'v2
    :else default))

(defn version-handler
  "This is a custom handler "
  [default-api]
  (fn [request]
    (let [headers (:headers request)
          ;; This next line is nuts and took a while to figure out -- results
          ;; are rendered in log files as symbols, but (headers 'accept) and
          ;; ('accept headers) didn't work. After an inordinate amount of trial
          ;; and error, it was discovered that the header keys are actually in
          ;; lower-case strings at this point in the middleware chain.
          accept (headers "accept")
          {version :version} (util/parse-accept-version accept)
          api (get-api-version version default-api)]
      (response/render (api request) request))))

(defroutes app
  (-> default-api-version
      version-handler
      ;; XXX once we support SSL, api-defaults needs to be changed to
      ;; secure-api-defaults
      (wrap-defaults api-defaults)
      (wrap-json-response)))

