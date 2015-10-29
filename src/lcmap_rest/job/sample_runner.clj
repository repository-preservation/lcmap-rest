(ns lcmap-rest.job.sample-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt]))

(defn long-running-func [[fake-id sleep-time year]]
  (log/debug (format "\n\nRunning job %s (waiting for %s seconds) ...\n"
                     fake-id
                     sleep-time))
  @(exec/sh ["sleep" (str sleep-time)])
  (exec/sh ["cal" year]))

(defn run-model [job-id seconds year]
  ;; Define some vars for pedagogical clarity
  (let [func #'long-running-func
        args [job-id seconds year]]
    (jt/track-job [func args])))
