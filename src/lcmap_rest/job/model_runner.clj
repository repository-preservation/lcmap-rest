(ns lcmap-rest.job.model-runner
  (:require [clojure.tools.logging :as log]
            [lcmap-rest.job.tracker :as jt]))

(defn model-func []
    :noop)

(defn get-model-hash [args]
  :fix-me!)

(defn run-model []
  (jt/track-job #'model-func))
