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
    (let [cfg (config/get-config)]
      (log/info "Using lein profile:" (get-in cfg [:env :active-profile]))
      (log/debug "Successfully generated LCMAP configuration.")
      cfg))

  (stop [component]
    (log/info "Tearing down LCMAP configuration ...")
    (log/debug "Component keys" (keys component))
    (assoc component :cfg nil)))

(defn new-configuration []
  (->Configuration))
