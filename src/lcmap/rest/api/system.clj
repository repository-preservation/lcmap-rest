(ns lcmap.rest.api.system
  (:import [java.lang Runtime])
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [metrics.core :as metrics]
            [lcmap.client.system]
            [lcmap.rest.serializer :as serializer]
            [lcmap.rest.system :as system]
            [lcmap.rest.errors :as errors]
            [lcmap.rest.middleware.http-util :as http]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  "system resources tbd")

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX this needs to be protected; see this ticket:
;;   https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes routes
  ;; Top-level system routes
  (context lcmap.client.system/context []
    (GET "/" request
      (http/response :result
        (get-resources (:uri request))))
    ;; XXX add more management resources
    (GET "/status" []
      (system/get-xml-status))
    ;; XXX json-status is a hack until we get dynamic content type support
    (GET "/json-status" []
      (system/get-json-status)))
  ;; system/metrics routes
  (context lcmap.client.system/metrics []
    ;; Note that lcmap.rest.middleware sets up a route for
    ;; /api/system/metrics/all
    (GET "/names" []
      (http/response :result
        (system/get-metrics-names metrics/default-registry)))
    (GET "/counters" []
      (http/response :result
        (system/get-metrics-counters metrics/default-registry)))
    (GET "/gauges" []
      (http/response :result
        (system/get-metrics-gauges metrics/default-registry)))
    (GET "/histograms" []
      (http/response :result
        (system/get-metrics-histograms metrics/default-registry)))
    (GET "/meters" []
      (http/response :result
        (system/get-metrics-meters metrics/default-registry)))
    (GET "/timers" []
      (http/response :result
        (system/get-metrics-timers metrics/default-registry))))
  ;; system/reference routes
  (context lcmap.client.system/reference []
    (GET "/error-types" []
      (http/response :result errors/category))
    (GET "/error-type/:id" [id]
      (http/response :result ((keyword id) errors/category)))
    (GET "/errors" []
      (http/response :result errors/lookup))
    (GET "/error/:id" [id]
      (http/response :result ((keyword id) errors/lookup)))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
