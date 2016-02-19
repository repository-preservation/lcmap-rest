(ns ^{:doc
  "HTTP LCMAP REST Service system component

  For more information, see the module-level code comments in
  ``lcmap.rest.components``."}
  lcmap.rest.components.httpd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))

;; We should keep these definitions here so that component interdependencies
;; are kept to a minimum.
(def authcfg-key ::authcfg)
(def jobdb-key ::jobdb)
(def eventd-key ::eventd)
(def tiledb-key ::tiledb)

(defn inject-app [handler auth-cfg eventd-component
                  jobdb-component tiledb-component]
  (fn [request]
    (handler (-> request
                 (assoc authcfg-key auth-cfg)
                 (assoc jobdb-key jobdb-component)
                 (assoc eventd-key eventd-component)
                 (assoc tiledb-key tiledb-component)))))

(defrecord HTTPServer [ring-handler]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (let [httpd-cfg (get-in component [:cfg :env :http])
          auth-backend (get-in component [:cfg :env :auth :backend])
          auth-cfg (get-in component [:cfg :env :auth auth-backend])
          eventd (:eventd component)
          jobdb (:jobdb component)
          tiledb (:tiledb component)
          handler (inject-app ring-handler auth-cfg eventd jobdb tiledb)
          server (httpkit/run-server handler httpd-cfg)]
      (log/debug "Using config:" httpd-cfg)
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
