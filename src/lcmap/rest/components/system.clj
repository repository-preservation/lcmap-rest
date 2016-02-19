(ns ^{:doc
  "Top-level LCMAP REST Service system component

  For more information, see the module-level code comments in
  ``lcmap.rest.components``."}
  lcmap.rest.components.system
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

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

(defn new-lcmap-toplevel []
  (->LCMAPSystem))
