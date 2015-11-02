(ns lcmap-rest.job.db
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [lcmap-rest.util :as util]))

(def job-namespace "lcmap")
(def job-table "job")

(defn connect [& {:keys [keyspace hosts protocol-version default-row]}]
  (let [default-db-cfg (:db (util/get-config))
        keyspace (or keyspace (:keyspace default-db-cfg))
        protocol-version (or protocol-version (:protocol-version default-db-cfg))
        hosts (or hosts (:hosts default-db-cfg))
        db-cfg (dissoc
                 (into default-db-cfg
                       {:keyspace keyspace
                        :protocol-version protocol-version})
                 :hosts)]
    (log/debug (format "Connecting to database hosts %s with config: %s ..."
                       hosts db-cfg))
    (cc/connect hosts db-cfg)))

(defn job? [db-conn job-id]
  (cql/select db-conn
              job-table
              (query/where [[= :job_id job-id]])))

(defn insert-default [db-conn job-id default-row]
  (cql/insert-async db-conn
                    job-table
                    (into default-row {:job_id job-id})))

(defn update-status [db-conn job-id new-status]
  (cql/update-async db-conn
                    job-table
                    {:status new-status}
                    (query/where [[= :job_id job-id]])))

