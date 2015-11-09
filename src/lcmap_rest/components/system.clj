(ns lcmap-rest.components.system
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.components.httpd :as httpd]))

(defrecord LCMAPSystem [cfg]
  component/Lifecycle

  (start [component]
    (log/info "Starting the system of LCMAP services ...")
    ;; XXX add any start-up needed for system as a whole
    )

  (stop [component]
    (log/info "Stopping the system of LCMAP services ...")
    ;; XXX add any tear-down needed for system as a whole
    ))

(defn new-lcmap-system [cfg]
  (map->LCMAPSystem {:cfg cfg}))

(defn init [cfg]
  (component/system-map
    :httpd (httpd/new-server (:app cfg) (:http cfg))
    :sys (component/using (new-lcmap-system cfg) [:httpd])))
