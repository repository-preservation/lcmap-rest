(ns lcmap-rest.components.system
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.components.config :as config]
            [lcmap-rest.components.db :as db]
            [lcmap-rest.components.eventd :as eventd]
            [lcmap-rest.components.httpd :as httpd]))

(defrecord LCMAPSystem []
  component/Lifecycle

  (start [component]
    (log/info "System dependencies started; finishing LCMAP startup ...")
    ;; XXX add any start-up needed for system as a whole
    (log/debug "System startup complete.")
    component)

  (stop [component]
    (log/info "Shutting down top-level LCMAP ...")
    ;; XXX add any tear-down needed for system as a whole
    (log/debug "Top-level shutdown complete; shutting down system dependencies ...")
    component))

(defn new-lcmap-system []
  (->LCMAPSystem))

(defn init [app]
  (component/system-map
    :cfg (config/new-configuration)
    :jobdb (component/using
             (db/new-job-client)
             [:cfg])
    :eventd (component/using
              (eventd/new-event-server)
              [:cfg])
    :httpd (component/using
             (httpd/new-server app)
             [:cfg
              :jobdb
              :eventd])
    :sys (component/using
           (new-lcmap-system)
           [:cfg
            :jobdb
            :eventd
            :httpd])))
