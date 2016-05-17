(ns lcmap.rest.api.models.sample-os-process
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [schema.core :as schema]
            [lcmap.client.models.sample-os-process]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as jobs]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any StrInt StrYear]]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.sample :as sample-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")
(def science-model-name "sample model")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-default-row
  [id]
  ""
  (model/make-default-row id result-table science-model-name))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-model
  ""
  [^Any request
   ^StrInt seconds
   ^StrYear year]
  (log/debugf "run-model got args: [%s %s]" seconds year)
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (let [job-id (util/get-args-hash
                 science-model-name :delay seconds :year year)]
    (sample-runner/run-model
      (:conn (httpd/jobdb-key request))
      (:eventd (httpd/eventd-key request))
      job-id
      (make-default-row job-id)
      result-table
      seconds
      year)
    (log/debug "Called sample-runner ...")
    (http/response :result {:link {:href (jobs/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-os-process/context []
    (POST "/" [token delay year :as request]
      ;; XXX use token to check user/session/authorization
      (model/validate #'run-model request delay year))
    (GET "/:job-id" [job-id :as request]
      (jobs/get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
