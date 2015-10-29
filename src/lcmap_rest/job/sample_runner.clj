(ns lcmap-rest.job.sample-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt]))

(defn long-running-func [fake-id]
  (let [sleep-time 10]
    (log/debug (str "\n\nRunning job " fake-id " (waiting for " sleep-time " seconds) ...\n"))
    @(exec/sh ["sleep" (str sleep-time)])
    (exec/sh ["ls" "-l"])))

(defn run-model [job-id]
  ;; Define some vars for pedagogical clarity
  (let [func #'long-running-func
        args [job-id]]
    (jt/track-job [func args])))
