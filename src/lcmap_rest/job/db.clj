(ns lcmap-rest.job.db
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]))

(def job-namespace "lcmap")
(def job-table "job")

(defn connect [& {:keys [keyspace hosts protocol-version default-row]
                  :or {hosts ["127.0.0.1"] ; XXX pull hosts from profile env
                       keyspace job-namespace
                       protocol-version 3}}]
  (cc/connect hosts {:keyspace keyspace :protocol-version protocol-version}))

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

