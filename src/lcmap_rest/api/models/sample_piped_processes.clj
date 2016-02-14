(ns lcmap-rest.api.models.sample-piped-processes
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap-rest.api.jobs.sample-piped-processes :refer [get-result-path
                                                                get-job-result
                                                                result-table]]
            [lcmap-client.models.sample-piped-processes]
            [lcmap-client.status-codes :as status]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.job.sample-pipe-runner :as sample-pipe-runner]
            [lcmap-rest.util :as util]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def science-model-name "sample model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
    (ring/status
      (ring/response
        {:result
          {:link {:href (get-result-path job-id)}}})
      status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.models.sample-piped-processes/context []
    (POST "/" [token number count bytes words lines :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (httpd/jobdb-key request)
                 (httpd/eventd-key request)
                 number count bytes words lines))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
