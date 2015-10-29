(ns lcmap-rest.api.sample
  (:require [clojure.tools.logging :as log]
            [lcmap-client.sample.model]
            [lcmap-rest.job.sample-runner :as sample-runner]))

(defn get-resources [request]
  "sample resources tbd")

(defn get-job-resources [request]
  "sample job resources tbd")

(defn get-job-result [job-id]
  ;; query job tracking databas
  ;; do results exist?
  ;; if not, return 202
  ;; if so, return data from query
  "sample job result tbd")

(defn update-job [job-id]
  "sample job update tbd")

(defn get-info [job-id]
  "sample job info tbd")

(defn get-model-resources [request]
  "sample model resources tbd")

(defn run-model [seconds year]
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (log/debug (format "run-model got args: [%s %s]" seconds year))
  (let [job-id "abc123"]
    (log/debug (format "sample model run (job id: %s)" job-id))
    (sample-runner/run-model job-id seconds year)
    {:result
      {:link (format "%s/%s"
                     lcmap-client.sample.model/context
                     job-id)}}))

