(ns lcmap-rest.job.ccdc-docker-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt])
  (:import [java.io ByteArrayOutputStream]))

(def dockerhub-org "usgseros")
(def dockerhub-repo "debian-c-ccdc")
(def docker-tag (format "%s/%s" dockerhub-org dockerhub-repo))

(defn exec-docker-run
  ""
  [[job-id arg1 arg2]]
  (let [cmd ["/usr/bin/sudo" "/usr/bin/docker"
             "run" "-t" docker-tag
             "--arg-1" arg1
             "--arg-2" arg2]
        result @(exec/sh cmd)]
    (case (:exit result)
      0 (:out result)
      1 (:err result)
      [:error "unexpected output" result])))

(defn run-model [conn eventd job-id default-row result-table arg1 arg2]
  ;; Define some vars for pedagogical clarity
  (let [func #'exec-docker-run
        args [job-id arg1 arg2]]
    (log/debugf "run-model has [func args]: [%s %s]" func args)
    (jt/track-job conn
                  eventd
                  job-id
                  default-row
                  result-table
                  [func args])))
