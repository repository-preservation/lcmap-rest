(ns lcmap.rest.api.jobs
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [lcmap.client.jobs]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs.ccdc]
            [lcmap.rest.api.jobs.ccdc-docker-process]
            [lcmap.rest.api.jobs.sample-docker-process]
            [lcmap.rest.api.jobs.sample-os-process]
            [lcmap.rest.api.jobs.sample-piped-processes]
            [lcmap.rest.middleware.http-util :as http]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  "jobs resources tbd")

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.jobs/context []
    (GET "/" request
      (http/response :result
        (get-resources (:uri request)))))
  lcmap.rest.api.jobs.ccdc/routes
  lcmap.rest.api.jobs.ccdc-docker-process/routes
  lcmap.rest.api.jobs.sample-docker-process/routes
  lcmap.rest.api.jobs.sample-os-process/routes
  lcmap.rest.api.jobs.sample-piped-processes/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
