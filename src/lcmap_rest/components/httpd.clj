(ns lcmap-rest.components.httpd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))

(defn inject-app-jobdb [handler job-db-component]
  (fn [request]
    (handler (assoc request ::jobdb job-db-component))))

(defrecord HTTPServer [ring-handler]
  component/Lifecycle

  (start [component]
    (log/info "Starting HTTP server ...")
    (let [httpd-cfg (get-in component [:cfg :http])
          db (:jobdb component)
          handler (inject-app-jobdb ring-handler db)
          server (httpkit/run-server handler httpd-cfg)]
      (log/debug "Using config:" httpd-cfg)
      (log/debug "Successfully created server:" server)
      (log/debug "Component keys:" (keys component))
      (log/debug "Job DB keys:" (keys (:jobdb component)))
      (assoc component :httpd server)))

  (stop [component]
    (log/info "Stopping HTTP server ...")
    (if-let [server (:httpd component)]
      (do (log/debug "Using server object:" server)
          (server))) ; calling server like this stops it, if started
    (dissoc component :httpd)))

(defn new-server [ring-handler]
  (->HTTPServer ring-handler))
