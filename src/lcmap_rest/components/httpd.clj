(ns lcmap-rest.components.httpd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))

(defrecord HTTPServer [ring-handler httpd-cfg]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (log/debug "Using config:" httpd-cfg)
    (let [server (httpkit/run-server ring-handler httpd-cfg)]
      (log/debug "Successfully created server:" server)
      (assoc component :httpd server)))

  (stop [component]
    (log/info "Stopping HTTP server ...")
    (if-let [server (:httpd component)]
      (do (log/debug "Using server object:" server)
          (server))) ; calling server like this stops it, if started
    (dissoc component :httpd)))

(defn new-server [ring-handler httpd-cfg]
  (->HTTPServer ring-handler httpd-cfg))
