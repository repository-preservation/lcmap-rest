(ns lcmap.rest.api.models.sample-piped-processes
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.models.sample-piped-processes]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as jobs]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.sample-pipe :as sample-pipe-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")
(def science-model-name "sample model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [db eventd number count bytes words lines]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debugf "run-model got args: %s" [number count bytes words lines])
  (let [job-id (util/get-args-hash science-model-name
                                   :number number
                                   :count count
                                   :bytes bytes
                                   :words words
                                   :lines lines)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}]
    ;;(log/debugf "sample model run (job id: %s)" job-id)
    ;;(log/debugf "default row: %s" default-row)
    (sample-pipe-runner/run-model (:conn db)
                                  (:eventd eventd)
                                  job-id
                                  default-row
                                  result-table
                                  number count bytes words lines)
    (log/debug "Called sample-piped-processes runner ...")
    (http/response :result {:link {:href (jobs/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-piped-processes/context []
    (POST "/" [token number count bytes words lines :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (httpd/jobdb-key request)
                 (httpd/eventd-key request)
                 number count bytes words lines))
    (GET "/:job-id" [job-id :as request]
      (jobs/get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
