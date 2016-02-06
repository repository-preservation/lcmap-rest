(ns ^{:doc
  "Config LCMAP REST Service system component

  For more information, see the module-level code comments in
  ``lcmap-rest.components``."}
  lcmap-rest.components.config
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.config :as config]))

(defrecord Configuration []
  component/Lifecycle

  (start [component]
    (log/info "Setting up LCMAP configuration ...")
    (let [cfg (config/get-updated-config)
          auth-backend (get-in cfg [:env :auth :backend])
          auth-cfg (get-in cfg [:env :auth auth-backend])]
      (log/info "Using lein profile:" (get-in cfg [:env :active-profile]))
      (log/infof "Will connect to the %s authentication endpoint ..."
                 (:endpoint auth-cfg))
      (log/debug "Successfully generated LCMAP configuration.")
      cfg))

  (stop [component]
    (log/info "Tearing down LCMAP configuration ...")
    (log/debug "Component keys" (keys component))
    (assoc component :cfg nil)))

(defn new-configuration []
  (->Configuration))
