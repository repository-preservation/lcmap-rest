(ns lcmap-rest.job.tracker
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [co.paralleluniverse.pulsar.actors :as actors])
  (:refer-clojure :exclude [promise await bean])
  (:import [co.paralleluniverse.common.util Debug]
           [co.paralleluniverse.actors LocalActor]
           [co.paralleluniverse.strands Strand]))

(declare dispatch-handler)

(defn job-result-exists? [args-or-hash]
  ;; XXX query the database, if the result exists, return the data,
  ;; else return false
  false)

(pulsar/defsfn init-job-track
  [job-id service func-args]
  (log/debug "Starting job tracking ...")
  ;; XXX check to see if result already exists
  ;; (result-exists? ...)
  (let [exists? (job-result-exists? :fix-me!)]
    (if exists?
      (actors/notify! job-id
                      service
                      {:type :job-result-exists
                       :result func-args
                       :service service})))
      ;; XXX calculate hash for job and populate job tracking table with
      ;; currently known data and a status of "pending"
      (actors/notify! job-id
                      service
                      {:type :job-run
                       :result func-args
                       :service service}))

(pulsar/defsfn return-existing-result
  [job-id service result]
  (log/debug "Returning ID for existing job results ...")
  ;; XXX get job ID to the function that needs it ... still need to give this
  ;; some thought
  (actors/notify! job-id
                  service
                  {:type :done
                   :result result
                   :service service}))

(pulsar/defsfn run-job
  [job-id service [job-func job-args]]
  (log/debug (format "Running the job with args %s ..." job-args))
  (let [job-data @(job-func job-args)]
    (log/debug "Finished job.")
    (actors/notify! job-id
                    service
                    {:type :job-save-data
                     :result job-data
                     :service service})))

(pulsar/defsfn save-job-data
  [job-id service job-output]
  ;; XXX update results data store
  (log/debug (str "Saved job data <" job-output "> with <insert id>."))
  ;; XXX the update returns an id that you can use to refer to this
  ;; job -- pass that on
  (actors/notify! job-id
                  service
                  {:type :job-track-finish
                   :result "<insert id>"
                   :service service}))

(pulsar/defsfn finish-job-track
  [job-id service result]
  ;; XXX update tracking data with information on completed job
  (log/debug (str "Updated job traking data with " result))
  (actors/notify! job-id
                  service
                  {:type :done
                   :result result
                   :service service}))

(pulsar/defsfn done
  [job-id service result]
  ;; XXX Perform any needed cleanup
  (log/debug "Finished job tracking.")
  ;;(actors/remove-handler! service #'dispatch-handler)
  ;;(actors/shutdown! event-server)
  ;;(log/debug "Removed event handler.")
  )

(pulsar/defsfn dispatch-handler
  [{type :type job-id :job-id result :result service :service}]
  (match [type]
    [:job-track-init] (init-job-track job-id service result)
    [:job-result-exists] (return-existing-result job-id service result)
    [:job-run] (run-job job-id service result)
    [:job-save-data] (save-job-data job-id service result)
    [:job-track-finish] (finish-job-track job-id service result)
    [:done] (done job-id service result)))

(defn track-job
  [job-id func-args]
  (log/debug "Creating event server ...")
  (let [event-server (actors/spawn (actors/gen-event))]
    (actors/add-handler! event-server #'dispatch-handler)
    (log/debug "Added event handler.")
    (actors/notify! job-id
                    event-server
                    {:type :job-track-init
                     :result func-args
                     :service event-server})))
