(ns lcmap-rest.api.sample
  (:require [clojure.tools.logging :as log]
            [ring.util.response :refer [response]]
            [clojure.tools.logging :as log]
            [lcmap-client.sample.model]
            [lcmap-rest.job.sample-runner :as sample-runner]
            [lcmap-rest.util :as util]))

(defn get-resources [request]
  (response "sample resources tbd"))

(defn get-job-resources [request]
  (response "sample job resources tbd"))

(defn get-job-result [job-id]
  ;; query job tracking databas
  ;; do results exist?
  ;; if not, return 202
  ;; if so, return data from query
  (response "sample job result tbd"))

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
  (let [job-id (util/get-args-hash "sample model" seconds year)]
    (log/debug (format "sample model run (job id: %s)" job-id))
    (sample-runner/run-model job-id seconds year)
    (log/debug "Called sample-runner ...")
    (response
      {:result
        {:link (format "%s/%s"
                       lcmap-client.sample.model/context
                       job-id)}})))

