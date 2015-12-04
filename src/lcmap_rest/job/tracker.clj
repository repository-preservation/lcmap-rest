(ns lcmap-rest.job.tracker
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [co.paralleluniverse.pulsar.core :refer [defsfn]]
            [co.paralleluniverse.pulsar.actors :as actors]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [lcmap-client.status-codes :as status]
            [lcmap-rest.job.db :as db])
  (:refer-clojure :exclude [promise await bean])
  (:import [co.paralleluniverse.common.util Debug]
           [co.paralleluniverse.actors LocalActor]
           [co.paralleluniverse.strands Strand]))

(declare dispatch-handler)

(defsfn job-result-exists? [db-conn result-table job-id]
  (log/debug "Got args:" db-conn result-table job-id)
  (match [(first @(db/result? db-conn result-table job-id))]
    [[]] false
    [nil] false
    [_] true))

(defsfn init-job-track
  [{job-id :job-id db-conn :db-conn default-row :default-row service :service
   result-table :result-table func-args :result :as args}]
  (log/debug "Starting job tracking ...")
  ;; XXX check to see if result already exists
  ;; (result-exists? ...)
  (if (job-result-exists? db-conn result-table job-id)
    (actors/notify! service
                    (into args {:type :job-result-exists}))
    (do
      @(db/insert-default db-conn job-id default-row)
      (actors/notify! service
                      (into args {:type :job-run})))))

(defsfn return-existing-result
  [{service :service :as args}]
  (log/debug "Returning ID for existing job results ...")
  (actors/notify! service
                  (into args {:type :done})))

(defsfn run-job
  [{job-id :job-id db-conn :db-conn service :service
    [job-func job-args] :result :as args}]
  (log/debugf "Running the job with args %s ..." job-args)
  (let [job-data (job-func job-args)]
    @(db/update-status db-conn job-id status/pending-link)
    (log/debug "Finished job.")
    (actors/notify! service
                    (into args {:type :job-save-data
                                :result job-data}))))

(defsfn save-job-data
  [{job-id :job-id db-conn :db-conn result-table :result-table service :service
    job-output :result :as args}]
  (log/debugf "Saving job data \n%s with id %s to %s ..."
                     job-output
                     job-id
                     result-table)
  @(cql/insert-async db-conn result-table {:result_id job-id :result job-output})
  (log/debug "Saved.")
  (actors/notify! service
                  (into args {:type :job-track-finish})))

(defsfn finish-job-track
  [{job-id :job-id db-conn :db-conn service :service result :result :as args}]
  @(db/update-status db-conn job-id status/permanant-link)
  (log/debug "Updated job traking data with" result)
  (actors/notify! service
                  (into args {:type :done})))

(defsfn done
  [{job-id :job-id :as args service :service}]
  (log/debugf "Finished tracking for job %s." job-id))

(defsfn dispatch-handler
  [{type :type :as args}]
  (match [type]
    [:job-track-init] (init-job-track args)
    [:job-result-exists] (return-existing-result args)
    [:job-run] (run-job args)
    [:job-save-data] (save-job-data args)
    [:job-track-finish] (finish-job-track args)
    [:done] (done args)))

(defn track-job
  [db-conn event-server job-id default-row result-table func-args]
  (log/debug "Using event server" event-server "with db connection" db-conn)
  (actors/notify! event-server
                  {:type :job-track-init
                   :job-id job-id
                   :db-conn db-conn
                   :default-row default-row
                   :result-table result-table
                   :result func-args
                   :service event-server}))
