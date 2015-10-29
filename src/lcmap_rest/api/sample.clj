(ns lcmap-rest.api.sample
  (:require [clojure.tools.logging :as log]
            [lcmap-client.sample.model]
            [lcmap-rest.job.sample-runner :as sample-runner]))

(defn get-resources [request]
  "sample resources tbd")

(defn get-job-resources [request]
  "sample job resources tbd")

(defn create-job [seconds directory]
  ;; generate job-id from hash of args
  ;; run query to create row in job-tracking table
  ;; return status code 200 with body that has link to where sample result will
  ;;   be
  (let [job-id "abc123"]
    {"result" {"link" (str lcmap-client.ccdc.model/context
                           "/sample/"
                           job-id)}}))

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

(defn run-model [job-id arg1 arg2]
  (log/debug (str "sample model run (job id: " job-id ")"))
  (sample-runner/run-model job-id))

(defn get-model-resources [request]
  "sample model resources tbd")
