(ns lcmap-rest.api.models.ccdc-docker-process
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap-rest.api.jobs.ccdc-docker-process :refer [get-result-path
                                                             get-job-result
                                                             result-table]]
            [lcmap-client.models.ccdc-docker-process]
            [lcmap-client.status-codes :as status]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.job.ccdc-docker-runner :as ccdc-docker-runner]
            [lcmap-rest.util :as util]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def science-model-name "ccdc")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [db eventd arg1 arg2]
  (log/debugf "run-model got args: [%s %s]" arg1 arg2)
  (let [job-id (util/get-args-hash science-model-name
                                   :arg1 arg1
                                   :arg2 arg2)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}]
    ;;(log/debugf "ccdc model run (job id: %s)" job-id)
    ;;(log/debugf "default row: %s" default-row)
    (ccdc-docker-runner/run-model (:conn db)
                             (:eventd eventd)
                             job-id
                             default-row
                             result-table
                             arg1
                             arg2)
    (log/debug "Called ccdc-runner ...")
    (ring/status
      (ring/response
        {:result
          {:link {:href (get-result-path job-id)}}})
      status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.models.ccdc-docker-process/context []
    (POST "/" [token arg1 arg2 :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (httpd/jobdb-key request)
                 (httpd/eventd-key request)
                 arg1
                 arg2))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
