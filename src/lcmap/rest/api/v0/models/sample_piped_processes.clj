(ns lcmap.rest.api.v0.models.sample-piped-processes
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap.rest.api.v0.jobs.sample-piped-processes :refer [get-result-path
                                                                   get-job-result
                                                                   result-table]]
            [lcmap.client.models.sample-piped-processes]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.sample-pipe :as sample-pipe-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def science-model-name "sample model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [component number count bytes words lines]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debugf "run-model got args: %s" [number count bytes words lines])
  (let [db (:jobdb component)
        job-id (util/get-args-hash science-model-name
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
    (sample-pipe-runner/run-model
      component
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
  (context lcmap.client.models.sample-piped-processes/context []
    (POST "/" [token number count bytes words lines :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (:component request)
                 number count bytes words lines))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
