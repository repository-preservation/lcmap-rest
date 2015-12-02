(ns lcmap-rest.api.jobs
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap-client.jobs]
            [lcmap-rest.api.jobs.ccdc]
            [lcmap-rest.api.jobs.sample-os-process]
            [lcmap-rest.status-codes :as status]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "jobs resources tbd")
    status/ok))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.jobs/context []
    (GET "/" request
      (get-resources (:uri request))))
  lcmap-rest.api.jobs.ccdc/routes
  lcmap-rest.api.jobs.sample-os-process/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
