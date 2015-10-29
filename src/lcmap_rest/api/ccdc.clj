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
