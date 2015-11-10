(ns lcmap-rest.components.db
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [clojurewerkz.cassaforte.client :as cc]))

(defrecord JobTrackerDBClient [ ]
  component/Lifecycle

  (start [component]
    (log/info "Starting DB client ...")
    (let [db-cfg (get-in component [:cfg :db])]
      (log/debug "Using config:" db-cfg)
      (let [conn (cc/connect (:hosts db-cfg) (dissoc db-cfg :hosts))]
        (log/debug "Successfully created db connection:" conn)
        (log/debug "Component keys:" (keys component))
        (assoc component :conn conn))))

  (stop [component]
    (log/info "Stopping DB server ...")
    (if-let [conn (:conn component)]
      (do (log/debug "Using connection object:" conn)
          (cc/disconnect conn)))
    (dissoc component :conn)))

(defn new-job-client []
  (->JobTrackerDBClient))
