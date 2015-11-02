(ns lcmap-rest.job.tracker
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [co.paralleluniverse.pulsar.actors :as actors]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [lcmap-rest.job.db :as db])
  (:refer-clojure :exclude [promise await bean])
  (:import [co.paralleluniverse.common.util Debug]
           [co.paralleluniverse.actors LocalActor]
           [co.paralleluniverse.strands Strand]))

(declare dispatch-handler)

(def resource-ready-status 307)

(defn job-result-exists? [db-conn args-or-hash]
  ;; XXX query the database, if the result exists, return the data,
  ;; else return false
  false)

(pulsar/defsfn init-job-track
  [{job-id :job-id db-conn :db-conn default-row :default-row service :service
   func-args :result :as args}]
  (log/debug "Starting job tracking ...")
  ;; XXX check to see if result already exists
  ;; (result-exists? ...)
  (let [exists? (job-result-exists? db-conn job-id)]
    (if exists?
      (actors/notify! service
                      (into args {:type :job-result-exists}))
      (do
        @(db/insert-default db-conn job-id default-row)
        (actors/notify! service
                        (into args {:type :job-run}))))))

(pulsar/defsfn return-existing-result
  [{service :service :as args}]
  (log/debug "Returning ID for existing job results ...")
  ;; XXX get job ID to the function that needs it ... still need to give this
  ;; some thought
  (actors/notify! service
                  (into args {:type :done})))

(pulsar/defsfn run-job
  [{service :service [job-func job-args] :result :as args}]
  (log/debug (format "Running the job with args %s ..." job-args))
  (let [job-data (job-func job-args)]
    (log/debug "Finished job.")
    (actors/notify! service
                    (into args {:type :job-save-data
                                :result job-data}))))

(pulsar/defsfn save-job-data
  [{job-id :job-id db-conn :db-conn result-table :result-table service :service
    job-output :result :as args}]
  (log/debug (format "Saving job data \n%s with id %s to %s ..."
                     job-output
                     job-id
                     result-table))
  @(cql/insert-async db-conn result-table {:result_id job-id :result job-output})
  (log/debug "Saved.")
  (actors/notify! service
                  (into args {:type :job-track-finish})))

(pulsar/defsfn finish-job-track
  [{job-id :job-id db-conn :db-conn service :service result :result :as args}]
  ;; XXX update tracking data with information on completed job
  @(db/update-status db-conn job-id resource-ready-status)
  (log/debug (str "Updated job traking data with " result))
  (actors/notify! service
                  (into args {:type :done})))

(pulsar/defsfn done
  [{job-id :job-id :as args}]
  ;; XXX Perform any needed cleanup
  (log/debug "Finished tracking for job %s." job-id)
  ;;(actors/remove-handler! service #'dispatch-handler)
  ;;(actors/shutdown! event-server)
  ;;(log/debug "Removed event handler.")
  )

(pulsar/defsfn dispatch-handler
  [{type :type :as args}]
  (match [type]
    [:job-track-init] (init-job-track args)
    [:job-result-exists] (return-existing-result args)
    [:job-run] (run-job args)
    [:job-save-data] (save-job-data args)
    [:job-track-finish] (finish-job-track args)
    [:done] (done args)))

(defn track-job
  [job-id db-conn default-row result-table func-args]
  (log/debug "Creating event server ...")
  (let [event-server (actors/spawn (actors/gen-event))]
    (actors/add-handler! event-server #'dispatch-handler)
    (log/debug "Added event handler.")
    (actors/notify! event-server
                    {:type :job-track-init
                     :job-id job-id
                     :db-conn db-conn
                     :default-row default-row
                     :result-table result-table
                     :result func-args
                     :service event-server})))
