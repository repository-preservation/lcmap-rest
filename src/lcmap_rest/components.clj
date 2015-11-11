;;;; LCMAP REST Service system components
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
(ns lcmap-rest.components
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.components.config :as config]
            [lcmap-rest.components.db :as db]
            [lcmap-rest.components.eventd :as eventd]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.components.logger :as logger]
            [lcmap-rest.components.system :as system]))

(defn init [app]
  (component/system-map
    :cfg (config/new-configuration)
    :logger (component/using
              (logger/new-logger)
              [:cfg])
    :jobdb (component/using
             (db/new-job-client)
             [:cfg
              :logger])
    :eventd (component/using
              (eventd/new-event-server)
              [:cfg
               :logger])
    :httpd (component/using
             (httpd/new-server app)
             [:cfg
              :logger
              :jobdb
              :eventd])
    :sys (component/using
           (system/new-lcmap-toplevel)
           [:cfg
            :logger
            :jobdb
            :eventd
            :httpd])))
