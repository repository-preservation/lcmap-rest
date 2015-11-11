;;;; Event LCMAP REST Service system component
;;;; For more information, see the module-level code comments in
;;;; lcmap-rest.components.system.
(ns lcmap-rest.components.httpd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))

(defn inject-app-jobdb [handler jobdb-component eventd-component]
  (fn [request]
    (handler (-> request
                 (assoc ::jobdb jobdb-component)
                 (assoc ::eventd eventd-component)))))

(defrecord HTTPServer [ring-handler]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (let [httpd-cfg (get-in component [:cfg :http])
          db (:jobdb component)
          eventd (:eventd component)
          handler (inject-app-jobdb ring-handler db eventd)
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
