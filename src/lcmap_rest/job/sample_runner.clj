(ns lcmap-rest.job.sample-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt]))

(defn long-running-func [[job-id sleep-time year]]
  (log/debugf "\n\nRunning job %s (waiting for %s seconds) ...\n"
                     job-id
                     sleep-time)
  @(exec/sh ["sleep" (str sleep-time)])
  (:out @(exec/sh ["cal" year])))

(defn run-model [conn eventd job-id default-row result-table seconds year]
  ;; Define some vars for pedagogical clarity
  (let [func #'long-running-func
        args [job-id seconds year]]
    (jt/track-job conn
                  eventd
                  job-id
                  default-row
                  result-table
                  [func args])))
