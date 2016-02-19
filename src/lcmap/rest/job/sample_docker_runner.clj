(ns lcmap.rest.job.sample-docker-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap.rest.job.tracker :as jt]))

(defn exec-docker-run [[job-id docker-tag year]]
  (log/debugf "\n\nRunning job %s (executing docker tag %s) ...\n"
                     job-id
                     docker-tag)
  (let [cmd ["/usr/bin/sudo" "/usr/bin/docker"
             "run" "-t" docker-tag
             "--year" year]
        result @(exec/sh cmd)]
    (case (:exit result)
      0 (:out result)
      1 (:err result)
      [:error "unexpected output" result])))

(defn run-model [conn eventd job-id default-row result-table docker-tag year]
  ;; Define some vars for pedagogical clarity
  (let [func #'exec-docker-run
        args [job-id docker-tag year]]
    (jt/track-job conn
                  eventd
                  job-id
                  default-row
                  result-table
                  [func args])))
