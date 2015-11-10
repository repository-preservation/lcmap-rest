(ns lcmap-rest.components.system
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.components.config :as config]
            [lcmap-rest.components.db :as db]
            [lcmap-rest.components.httpd :as httpd]))

(defrecord LCMAPSystem []
  component/Lifecycle

  (start [component]
    (log/info "System dependencies started; finishing LCMAP startup ...")
    ;; XXX add any start-up needed for system as a whole
    )

  (stop [component]
    (log/info "System dependencies stopped; finishing LCMAP shutdown ...")
    ;; XXX add any tear-down needed for system as a whole
    ))

(defn new-lcmap-system []
  (->LCMAPSystem))

(defn init [app]
  (component/system-map
    :cfg (config/new-configuration)
    :jobdb (component/using
             (db/new-job-client)
             [:cfg])
    :httpd (component/using
             (httpd/new-server app)
             [:cfg
              :jobdb])
    :sys (component/using
           (new-lcmap-system)
           [:cfg
            :jobdb
            :httpd])))
