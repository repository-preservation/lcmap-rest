(ns lcmap.rest.api.jobs
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap.client.jobs]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs.ccdc]
            [lcmap.rest.api.jobs.ccdc-docker-process]
            [lcmap.rest.api.jobs.sample-docker-process]
            [lcmap.rest.api.jobs.sample-os-process]
            [lcmap.rest.api.jobs.sample-piped-processes]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "jobs resources tbd")
    status/ok))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.jobs/context []
    (GET "/" request
      (get-resources (:uri request))))
  lcmap.rest.api.jobs.ccdc/routes
  lcmap.rest.api.jobs.ccdc-docker-process/routes
  lcmap.rest.api.jobs.sample-docker-process/routes
  lcmap.rest.api.jobs.sample-os-process/routes
  lcmap.rest.api.jobs.sample-piped-processes/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
