(ns lcmap-rest.api.sample
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :as ring]
            [lcmap-client.sample.model]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.job.sample-runner :as sample-runner]
            [lcmap-rest.status-codes :as status]
            [lcmap-rest.util :as util]))

(def science-model-name "sample model")
(def result-keyspace "lcmap")
(def result-table "samplemodel")

(defn get-result-path
  [result-id]
  (format "%s/%s"
          lcmap-client.sample.model/context
          result-id))

(defn get-resources [request]
  (ring/status
    (ring/response "sample resources tbd")
    status/ok))

(defn get-job-resources [request]
  (ring/status
    (ring/response "sample job resources tbd")
    status/ok))

(defn get-job-status [job-id]
  (match [(db/job? job-id)]
    [[]]
      (ring/status
        (ring/response {:error "Job not found."})
        status/no-resource)
    [nil]
      (ring/status
        (ring/response {:error "Job not found."})
        status/no-resource)
    ;; XXX we can remove 200 before alpha; it was just used internally for testing
    [({:status status/ok} :as result)]
      (ring/status
        (ring/response {:result (get-result-path job-id)})
        status/ok)
    [({:status status/pending-link} :as result)]
      (ring/status
        (ring/response {:result (get-result-path job-id)})
        status/pending-link)
    [({:status status/permanant-link} :as result)]
      (ring/status
        (ring/response {:result (get-result-path job-id)})
        status/permanant-link)
    [({:status status/pending} :as result)]
      (ring/status
        (ring/response {:result :pending})
        status/pending)))

(defn get-job-result [job-id]
  (match [(db/result? result-table job-id)]
    [[]]
      (get-job-status job-id)
    [nil]
      (get-job-status job-id)
    [{:result result}]
      (ring/status
        (ring/response {:result result})
        status/ok)))

(defn update-job [job-id]
  (ring/status
    (ring/response "sample job update tbd")
    status/pending))

(defn get-info [job-id]
  (ring/response "sample job info tbd"))

(defn get-model-resources [request]
  (ring/response "sample model resources tbd"))

(defn run-model [seconds year]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debug (format "run-model got args: [%s %s]" seconds year))
  (let [job-id (util/get-args-hash "sample model" seconds year)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}
        db-conn (db/connect)]
    ;;(log/debug (format "sample model run (job id: %s)" job-id))
    ;;(log/debug (format "default row: %s" default-row))
    (sample-runner/run-model job-id
                             db-conn
                             default-row
                             result-table
                             seconds
                             year)
    (log/debug "Called sample-runner ...")
    (ring/status
      (ring/response
        {:result
          {:link (get-result-path job-id)}})
      status/pending-link)))

