(ns lcmap-rest.job.sample-runner
  (:require [clojure.tools.logging :as log]
            [clj-commons-exec :as exec]
            [lcmap-rest.job.tracker :as jt]))

(defn long-running-func []
  (let [sleep-time 10]
    (log/debug (str "\n\nWaiting for " sleep-time " seconds ...\n"))
    @(exec/sh ["sleep" (str sleep-time)])
    (exec/sh ["ls" "-l"])))

(defn run-sample []
  (jt/track-job #'long-running-func))
