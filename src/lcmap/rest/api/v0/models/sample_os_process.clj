(ns lcmap.rest.api.v0.models.sample-os-process
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap.rest.api.v0.jobs.sample-os-process :refer [get-result-path
                                                              get-job-result
                                                              result-table]]
            [lcmap.client.models.sample]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.see.util :as util]
            [lcmap.see.backend.native.models.sample :as sample-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def science-model-name "sample model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [component seconds year]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debugf "run-model got args: [%s %s]" seconds year)
  (let [job-id (util/get-args-hash science-model-name
                                   :delay seconds
                                   :year year)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}]
    ;;(log/debugf "sample model run (job id: %s)" job-id)
    ;;(log/debugf "default row: %s" default-row)
    (sample-runner/run-model
      component
      job-id
      default-row
      result-table
      seconds
      year)
    (log/debug "Called sample-runner ...")
    (ring/status
      (ring/response
        {:result
          {:link {:href (get-result-path job-id)}}})
      status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample/context []
    (POST "/" [token delay year :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (:component request)
                 delay
                 year))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
