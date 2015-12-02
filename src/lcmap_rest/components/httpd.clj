;;;; Event LCMAP REST Service system component
;;;; For more information, see the module-level code comments in
;;;; lcmap-rest.components.
(ns lcmap-rest.components.httpd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))

;; We should keep these definitions here so that component interdependencies
;; are kept to a minimum.
(def jobdb-key ::jobdb)
(def eventd-key ::eventd)

(defn inject-app-jobdb [handler jobdb-component eventd-component]
  (fn [request]
    (handler (-> request
                 (assoc jobdb-key jobdb-component)
                 (assoc eventd-key eventd-component)))))

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
