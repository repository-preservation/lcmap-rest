(ns lcmap-rest.api.ccdc
  (:require [clojure.tools.logging :as log]
            [lcmap-client.ccdc.model]
            [lcmap-rest.job.model-runner :as model-runner]
            [lcmap-rest.job.sample-runner :as sample-runner]))

(defn get-resources [request]
  "resources tbd")

(defn get-job-resources [request]
  "job resources tbd")

(defn create-job [arg1 arg2]
  "job creation tbd")

(defn get-job-result [job-id]
  "job result tbd")

(defn update-job [job-id]
  "job update tbd")

(defn get-info [job-id]
  "job info tbd")

(defn run-model [job-id arg1 arg2]
  (str "model run (job id: " job-id ") tbd"))

(defn get-model-resources [request]
  "model resources tbd")

;;; Functions for sample job and model -- for testing purposes only

(defn create-sample-job [seconds directory]
  ;; generate job-id from hash of args
  ;; run query to create row in job-tracking table
  ;; return status code 200 with body that has link to where sample result will
  ;;   be
  (let [job-id "abc123"]
    {"result" {"link" (str lcmap-client.ccdc.model/context
                           "/sample/"
                           job-id)}}))

(defn get-sample-job-result [job-id]
  ;; query job tracking databas
  ;; do results exist?
  ;; if not, return 202
  ;; if so, return data from query
  "")

(defn update-sample-job [job-id]
  "")

(defn get-sample-info [job-id]
  "")

(defn run-sample-model [job-id]
  (sample-runner/run-sample-model job-id))
