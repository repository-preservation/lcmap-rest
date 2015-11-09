(ns lcmap-rest.components.system
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.components.db :as db]
            [lcmap-rest.components.httpd :as httpd]))

(defrecord LCMAPSystem [cfg]
  component/Lifecycle

  (start [component]
    (log/info "System dependencies started; finishing LCMAP startup ...")
    ;; XXX add any start-up needed for system as a whole
    )

  (stop [component]
    (log/info "System dependencies stopped; finishing LCMAP shutdown ...")
    ;; XXX add any tear-down needed for system as a whole
    ))

(defn new-lcmap-system [cfg]
  (map->LCMAPSystem {:cfg cfg}))

(def system-deps [:httpd
                  :db])

(defn init [cfg]
  (component/system-map
    :httpd (httpd/new-server (:app cfg) (:http cfg))
    :db (db/new-client (:db cfg))
    :sys (component/using
           (new-lcmap-system cfg)
           system-deps)))
