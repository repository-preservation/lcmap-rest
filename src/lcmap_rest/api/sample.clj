(ns lcmap-rest.api.sample
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :as ring]
            [lcmap-client.sample.job]
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
          lcmap-client.sample.job/context
          result-id))

(defn get-resources [request]
  (ring/status
    (ring/response "sample resources tbd")
    status/ok))

(defn get-job-resources [request]
  (ring/status
    (ring/response "sample job resources tbd")
    status/ok))

(defn get-job-status [db job-id]
  (match [(first @(db/job? (:conn db) job-id))]
    [[]]
      (ring/status
        (ring/response {:error "Job not found."})
        status/no-resource)
    [nil]
      (ring/status
        (ring/response {:error "Job not found."})
        status/no-resource)
    [({:status (st :guard #'status/pending?)} :as result)]
      (ring/status
        (ring/response {:result :pending})
        status/pending)
    [({:status st} :as result)]
      (ring/status
        (ring/response {:result (get-result-path (:conn db) job-id)})
        st)))

(defn get-job-result [db job-id]
  (match [(first @(db/result? (:conn db) result-table job-id))]
    [[]]
      (get-job-status db job-id)
    [nil]
      (get-job-status db job-id)
    [{:result result}]
      (ring/status
        (ring/response {:result result})
        status/ok)))

(defn update-job [db job-id]
  (ring/status
    (ring/response "sample job update tbd")
    status/pending))

(defn get-info [db job-id]
  (ring/response "sample job info tbd"))

(defn get-model-resources [request]
  (ring/response "sample model resources tbd"))

(defn run-model [db eventd seconds year]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debugf "run-model got args: [%s %s]" seconds year)
  (let [job-id (util/get-args-hash "sample model" seconds year)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}]
    ;;(log/debugf "sample model run (job id: %s)" job-id)
    ;;(log/debugf "default row: %s" default-row)
    (sample-runner/run-model (:conn db)
                             (:eventd eventd)
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

