(ns lcmap.rest.api.models.ccdc-piped-processes
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.models.ccdc-piped-processes]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs.core :as jobs]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.ccdc-pipe :as ccdc-pipe-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")
(def science-model-name "sample model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [db eventd spectra x-val y-val start-time end-time
                           row col in-dir out-dir scene-list verbose]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where the ccdc result will
  ;; be
  (log/debugf "run-model got args: %s" [spectra x-val y-val start-time end-time
                                        row col in-dir out-dir scene-list verbose])
  (let [job-id (util/get-args-hash science-model-name
                                   :spectra spectra
                                   :x-val x-val
                                   :y-val y-val
                                   :start-time start-time
                                   :end-time end-time
                                   :row row
                                   :col col
                                   :in-dir in-dir
                                   :out-dir out-dir
                                   :scene-list scene-list
                                   :verbose verbose)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}]

    (ccdc-pipe-runner/run-model (:conn db)
                                  (:eventd eventd)
                                  job-id
                                  default-row
                                  result-table
                                  spectra x-val y-val start-time end-time
                                  row col in-dir out-dir scene-list verbose)
    (log/debug "Called ccdc-piped-processes runner ...")
    (http/response :result {:link {:href (jobs/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.ccdc-piped-processes/context []
    (POST "/" [token spectra x-val y-val start-time end-time
                     row col in-dir out-dir scene-list verbose :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (httpd/jobdb-key request)
                 (httpd/eventd-key request)
                 spectra x-val y-val start-time end-time
                 row col in-dir out-dir scene-list verbose))
    (GET "/:job-id" [job-id :as request]
      (jobs/get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
