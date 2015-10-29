(ns lcmap-rest.api.routes
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [compojure.route :as route]
            [lcmap-client.core]
            [lcmap-client.lcmap]
            [lcmap-client.ccdc]
            [lcmap-client.ccdc.job]
            [lcmap-client.ccdc.model]
            [lcmap-client.l8.surface-reflectance]
            [lcmap-rest.api.ccdc :as ccdc]
            [lcmap-rest.api.l8.surface-reflectance :as l8-sr]
            [lcmap-rest.api.management :as management]
            [lcmap-rest.util :as util]))

;; XXX combine ccdc routes here and test to make sure that works
(defroutes ccdc-top
  (context lcmap-client.ccdc/context []
    (GET "/" request
      (ccdc/get-resources (:uri request)))))

(defroutes ccdc-science-model
  (context lcmap-client.ccdc.model/context []
    (GET "/" request
      (ccdc/get-model-resources (:uri request)))
    (POST "/" [arg1 arg2 :as request]
      (ccdc/run-model arg1 arg2))
    (GET "/:job-id" [job-id]
      (ccdc/get-job-result [job-id]))
    (POST "/run/:job-id" [job-id]
      (ccdc/run-model job-id :arg1 :arg2))
    (POST "/run-sample/:job-id" [job-id]
      (ccdc/run-sample-model job-id))))

(defroutes ccdc-job-management
  (context lcmap-client.ccdc.job/context []
    (GET "/" request
      (ccdc/get-job-resources (:uri request)))
    (POST "/" [arg1 arg2 :as request]
      (ccdc/create-job arg1 arg2))
    (GET "/:job-id" [job-id]
      (ccdc/get-job-result job-id))
    (PUT "/:job-id" [job-id]
      (ccdc/update-job job-id))
    (HEAD "/:job-id" [job-id]
      (ccdc/get-info job-id))
    (POST "/run/:job-id" [job-id]
      (ccdc/run-model job-id :arg1 :arg2))
    (POST "/run-sample/:job-id" [job-id]
      (ccdc/run-sample-model job-id))))

;; XXX add routes for sample

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
  ccdc-top
  ccdc-science-model
  ccdc-job-management
  surface-reflectance
  management
  (route/not-found "Resource not found"))

(defroutes v1
  management
  (route/not-found "Resource not found"))

(defroutes v2
  management
  (route/not-found "Resource not found"))

