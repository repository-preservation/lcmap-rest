;;;; Top-level LCMAP REST Service system component
;;;;
;;;; Large applications often consist of many stateful processes which must be
;;;; started and stopped in a particular order. The component model makes
;;;; those relationships explicit and declarative, instead of implicit in
;;;; imperative code. The LCMAP REST service project is one such application
;;;; and early on in its existence it was refactored to support the
;;;; component/dependency-injection approach.
;;;;
;;;; The approach taken in the LCMAP REST application is the following:
;;;;  * The primary entry point starts the top-level system map (this is
;;;;    done in lcmap-rest.app.main).
;;;;  * The top-level system map is defined in
;;;;    lcmap-rest.components.system.init -- this is what is started in
;;;;    the main function.
;;;;  * init takes any parameters not defined in configuration (e.g., the
;;;;    top-level Ring handler) and instantiates each component while at
;;;;    the same time defining each components dependencies.
;;;;  * During start-up and shut-down
;;;;
;;;; For more information on the Clojure component library, see:
;;;;  * https://github.com/stuartsierra/component
;;;;  * https://www.youtube.com/watch?v=13cmHf_kt-Q
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
