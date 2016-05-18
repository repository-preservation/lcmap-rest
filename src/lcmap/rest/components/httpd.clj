(ns ^{:doc
  "HTTP LCMAP REST Service system component

  For more information, see the module-level code comments in
  ``lcmap.rest.components``."}
  lcmap.rest.components.httpd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]
            [lcmap.rest.config :as config]
            [lcmap.config.helpers :as cfg-help]))

(:lcmap.rest.components.httpd/httpkit (cfg-help/build-cfg config/defaults))

(def jobdb-key :jobdb)
(def eventd-key :eventd)

(defn inject-app
  "Make app components available to request handlers."
  [handler component]
  (fn [request]
    (handler (merge request component))))

(defrecord HTTPServer [ring-handler]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (let [http-cfg (get-in component [:cfg :lcmap.rest])
          handler (inject-app ring-handler component)
          server (httpkit/run-server handler http-cfg)]
      (log/debug "Using config:" http-cfg)
      (log/debug "Component keys:" (keys component))
      (log/debug "Successfully created server:" server)
      (assoc component :httpd server)))

  (stop [component]
    (log/info "Stopping HTTP server ...")
    (log/debug "Component keys" (keys component))
    (if-let [server (:httpd component)]
      (do (log/debug "Using server object:" server)
          (server))) ; calling server like this stops it, if started
    (assoc component :httpd nil)))

(defn new-server [ring-handler]
  (->HTTPServer ring-handler))
