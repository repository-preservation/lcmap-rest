(ns lcmap-rest.job.ccdc-docker-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt]))

(defn exec-docker-run [[job-id arg1 arg2]]
  (log/debugf "\n\nRunning job %s (executing docker tag %s) ...\n"
                     arg1
                     arg2)
  (:out @(exec/sh ["docker run -t usgseros/debian-c-ccdc"
                   " --arg-1 " arg1
                   " --arg-2 " arg2])))

(defn run-model [conn eventd job-id default-row result-table arg1 arg2]
  ;; Define some vars for pedagogical clarity
  (let [func #'exec-docker-run
        args [job-id arg1 arg2]]
    (jt/track-job conn
                  eventd
                  job-id
                  default-row
                  result-table
                  [func args])))
