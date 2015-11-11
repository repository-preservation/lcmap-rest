;;;; Logger LCMAP REST Service system component
;;;; For more information, see the module-level code comments in
;;;; lcmap-rest.components.
(ns lcmap-rest.components.logger
  (:require [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as log-impl]
            [com.stuartsierra.component :as component]
            [lcmap-rest.logger :as logger]))

(defrecord Logger []
  component/Lifecycle

  (start [component]
    (log/info "Setting up LCMAP logging ...")
    (let [log-level (get-in component [:cfg :log-level])
          namespaces (get-in component [:cfg :logging-namespaces])]
      (log/info "Using log-level" log-level)
      (logger/set-level! namespaces log-level)
      ;;(dorun (map #(logger/set-level! % log-level) namespaces))
      (log/debug "Logging agent:" log/*logging-agent*)
      (log/debug "Logging factory:" (logger/get-factory))
      (log/debug "Logging factory name:" (logger/get-factory-name))
      (log/debug "Logger:" (logger/get-logger *ns*))
      (log/debug "Logger name:" (logger/get-logger-name *ns*))
      (log/debug "Set log level for these namespaces:" namespaces)
      (log/debug "Successfully configured logging.")
      component))

  (stop [component]
    (log/info "Tearing down LCMAP logging ...")
    (log/debug "Component keys" (keys component))
    (assoc component :cfg nil)))

(defn new-logger []
  (->Logger))
