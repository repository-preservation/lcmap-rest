(ns lcmap.rest.api.models.sample-os-process
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [schema.core :as schema]
            [lcmap.client.models.sample-os-process]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs.core :as jobs]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any StrInt StrYear]]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.sample :as sample-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")
(def science-model-name "sample model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-default-row
  ""
  [result-id default-status]
  {:science_model_name science-model-name
   :result_keyspace result-keyspace
   :result_table result-table
   :result_id result-id
   :status default-status})

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-typed-model
  ""
  [db :- Any
   eventd :- Any
   seconds :- StrInt
   year :- StrYear]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debugf "run-model got args: [%s %s]" seconds year)
  (let [job-id (util/get-args-hash
                 science-model-name :delay seconds :year year)]
    ;;(log/debugf "sample model run (job id: %s)" job-id)
    ;;(log/debugf "default row: %s" default-row)
    (sample-runner/run-model
      (:conn db)
      (:eventd eventd)
      job-id
      (make-default-row job-id status/pending)
      result-table
      seconds
                       year)
    (log/debug "Called sample-runner ...")
    (http/response :result {:link {:href (jobs/get-result-path job-id)}}
                   :status status/pending-link)))

(defn run-model
  ""
  [& args]
  (try
    (schema/with-fn-validation
      (apply run-typed-model args))
    (catch RuntimeException e
      (let [error (.getMessage e)]
        (log/error "Got error:" error)
        (->
         ;; XXX status/client-error doesn't exist?
         (http/response :errors [error]
                        :status status/server-error)
         ;; update to take mime sub-type from Accept
         (http/problem-header))))))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-os-process/context []
    (POST "/" [token delay year :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model
        ;; XXX choose a good way to pass the Accept header to the model
        ;;     so that the error problem type header can be set appropriately
        (httpd/jobdb-key request)
        (httpd/eventd-key request)
        delay
        year))
    (GET "/:job-id" [job-id :as request]
      (jobs/get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(with-handler! #'run-model
  RuntimeException
  (fn [e & args]
    (log/error "error: %s; args: %s" e args)
    (http/response :errors [e])))
