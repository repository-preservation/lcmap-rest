(ns lcmap-rest.routes
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [compojure.route :as route]
            [lcmap-client.core]
            [lcmap-client.lcmap]
            [lcmap-client.ccdc]
            [lcmap-client.ccdc.job]
            [lcmap-client.ccdc.model]
            [lcmap-client.l8.surface-reflectance]
            [lcmap-rest.ccdc :as ccdc]
            [lcmap-rest.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.management :as management]
            [lcmap-rest.util :as util]))

(defroutes ccdc-science-model
  (context lcmap-client.ccdc.model/context []
    (POST "/" [arg1 arg2 :as request]
      (ccdc/run-model arg1 arg2))))

(defroutes ccdc-job-management
  (context lcmap-client.ccdc.job/context []
    (POST "/" [arg1 arg2 :as request]
      (ccdc/create-job arg1 arg2))
    (GET "/:id" [id]
      (ccdc/get-job-result id))
    (PUT "/:id" [id]
      (ccdc/update-job id))
    (HEAD "/:id" [id]
      (ccdc/get-info id))))

(defroutes ccdc
  (context lcmap-client.ccdc/context []
    (GET "/" request
      (ccdc/get-resources (:uri request)))
    ccdc-science-model
    ccdc-job-management))

(defroutes surface-reflectance
  (context lcmap-client.l8.surface-reflectance/context []
    (GET "/" request
      (l8-sr/get-resources (:uri request)))
    (GET "/tiles" [point extent time band :as request]
      (l8-sr/get-tiles point extent time band request))
    (GET "/rod" [point time band :as request]
      (l8-sr/get-rod point time band request))))

;; XXX this needs to go into a protected area; see this ticket:
;;  https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes management
  (context (str lcmap-client.lcmap/context "/manage") []
    (GET "/status" [] (management/get-status))
    ;; XXX add more management resources
    ))

(defroutes v0
  ccdc
  surface-reflectance
  management
  (route/not-found "Resource not found"))

(defroutes v1
  management
  (route/not-found "Resource not found"))

(defroutes v2
  management
  (route/not-found "Resource not found"))

