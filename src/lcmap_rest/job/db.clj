(ns lcmap-rest.job.db
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [lcmap-rest.util :as util]))

(def job-namespace "lcmap")
(def job-table "job")

(defn job? [conn job-id]
  (cql/select-async
    conn
    job-table
    (query/where [[= :job_id job-id]])
    (query/limit 1)))

(defn result? [conn result-table result-id]
  (log/debugf "Checking for result of id %s in table '%s' ..."
              result-id result-table)
  (cql/select-async
    conn
    result-table
    (query/where [[= :result_id result-id]])
    (query/limit 1)))

(defn insert-default [conn job-id default-row]
  (cql/insert-async
    conn
    job-table
    (into default-row {:job_id job-id})))

(defn update-status [conn job-id new-status]
  (cql/update-async conn
                    job-table
                    {:status new-status}
                    (query/where [[= :job_id job-id]])))


(defn get-job-result [db job-id result-table status-func]
  (match [(first @(result? (:conn db) result-table job-id))]
    [[]]
      (status-func db job-id)
    [nil]
      (status-func db job-id)
    [result]
      (status-func result)))
