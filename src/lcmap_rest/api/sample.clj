(ns lcmap-rest.api.sample
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :refer [response]]
            [lcmap-client.sample.model]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.job.sample-runner :as sample-runner]
            [lcmap-rest.util :as util]))

(def science-model-name "sample model")
(def result-keyspace "lcmap")
(def result-table "samplemodel")
(def pending-status 202)

(defn get-result-path
  [result-id]
  (format "%s/%s"
          lcmap-client.sample.model/context
          result-id))

(defn get-resources [request]
  (response "sample resources tbd"))

(defn get-job-resources [request]
  (response "sample job resources tbd"))

(defn get-job-status [job-id]
  (match [(db/job? job-id)]
    [[]]
      (response {:error "Job not found."})
    [nil]
      (response {:error "Job not found."})
    [({:status 200} :as result)]
      (response {:result (get-result-path job-id)})
    [({:status 202} :as result)]
      (response {:result :pending})))

(defn get-job-result [job-id]
  (match [(db/result? result-table job-id)]
    [[]]
      (get-job-status job-id)
    [nil]
      (get-job-status job-id)
    [{:result result}]
      (response {:result result})))

(defn update-job [job-id]
  (response "sample job update tbd"))

(defn get-info [job-id]
  (response "sample job info tbd"))

(defn get-model-resources [request]
  (response "sample model resources tbd"))

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
                     :status pending-status}
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
    (response
      {:result
        {:link (get-result-path job-id)}})))

