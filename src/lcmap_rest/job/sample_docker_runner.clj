(ns lcmap-rest.job.sample-docker-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt]))

(defn exec-docker-run [[job-id docker-tag docker-args]]
  (log/debugf "\n\nRunning job %s (waiting for %s seconds) ...\n"
                     job-id
                     sleep-time)
  (:out @(exec/sh ["docker run -e LCMAP_MODEL_ARGS=" docker-args " -t " docker-tag])))

(defn run-model [conn eventd job-id default-row result-table docker-tag docker-args]
  ;; Define some vars for pedagogical clarity
  (let [func #'exec-docker-run
        args [job-id docker-tag docker-args]]
    (jt/track-job conn
                  eventd
                  job-id
                  default-row
                  result-table
                  [func args])))
