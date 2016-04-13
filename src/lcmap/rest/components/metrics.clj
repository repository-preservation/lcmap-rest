(ns ^{:doc
  "Logger LCMAP REST Service metrics component

  For more information, see the module-level code comments in
  ``lcmap.rest.components``."}
  lcmap.rest.components.metrics
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [metrics.core :as metrics]
            [metrics.jvm.core :as metrics-jvm]
            [metrics.reporters.jmx :as metrics-jmx]))

(defrecord Metrics []
  component/Lifecycle

  (start [component]
    (log/info "Setting up LCMAP metrics ...")
    (metrics-jvm/instrument-jvm)
    (-> {}
        (metrics-jmx/reporter)
        (metrics-jmx/start))
    component)

  (stop [component]
    (log/info "Tearing down LCMAP metrics ...")
    (log/debug "Component keys" (keys component))
    component))

(defn new-metrics []
  (->Metrics))
