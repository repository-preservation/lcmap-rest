(ns lcmap.rest.api.system
  (:import [java.lang Runtime])
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [metrics.core :as metrics]
            [lcmap.client.system]
            [lcmap.rest.serializer :as serializer]
            [lcmap.rest.system :as system]
            [lcmap.rest.middleware.http-util :as http]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (http/response "system resources tbd"))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX this needs to be protected; see this ticket:
;;   https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes routes
  (context lcmap.client.system/context []
    (GET "/" request
      (get-resources (:uri request)))
    ;; Note that lcmap.rest.middleware sets up a route for
    ;; /api/system/metrics/all
    (GET "/metrics/names" []
      (http/response :result
        (system/get-metrics-names metrics/default-registry)))
    (GET "/metrics/counters" []
      (http/response :result
        (system/get-metrics-counters metrics/default-registry)))
    (GET "/metrics/gauges" []
      (http/response :result
        (system/get-metrics-gauges metrics/default-registry)))
    (GET "/metrics/histograms" []
      (http/response :result
        (system/get-metrics-histograms metrics/default-registry)))
    (GET "/metrics/meters" []
      (http/response :result
        (system/get-metrics-meters metrics/default-registry)))
    (GET "/metrics/timers" []
      (http/response :result
        (system/get-metrics-timers metrics/default-registry)))
    ;; XXX add more management resources
    (GET "/status" []
      (system/get-xml-status))
    ;; XXX json-status is a hack until we get dynamic content type support
    (GET "/json-status" []
      (system/get-json-status))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
