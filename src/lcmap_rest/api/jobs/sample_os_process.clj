(ns lcmap-rest.api.jobs.sample-os-process
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :as ring]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap-client.jobs.sample-os-process]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.status-codes :as status]
            [lcmap-rest.util :as util]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-result-path
  [result-id]
  (format "%s/%s"
          lcmap-client.jobs.sample-os-process/context
          result-id))

(defn get-job-resources [request]
  (ring/status
    (ring/response "sample job resources tbd")
    status/ok))

(defn get-job-status [db job-id]
  (match [(first @(db/job? (:conn db) job-id))]
    [[]]
      (ring/status
        (ring/response {:error "Job not found."})
        status/no-resource)
    [nil]
      (ring/status
        (ring/response {:error "Job not found."})
        status/no-resource)
    [({:status (st :guard #'status/pending?)} :as result)]
      (ring/status
        (ring/response {:result :pending})
        status/pending)
    [({:status st} :as result)]
      (ring/status
        (ring/response {:result (get-result-path (:conn db) job-id)})
        st)))

(defn get-job-result
  ([db-conn job-id]
    (get-job-result db-conn job-id result-table #'get-job-status))
  ([db-conn job-id table func]
    (-> (db/get-job-result db-conn job-id table func)
        (ring/response)
        (ring/status status/ok))))

(defn update-job [db job-id]
  (ring/status
    (ring/response "sample job update tbd")
    status/pending))

(defn get-info [db job-id]
  (ring/response "sample job info tbd"))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.jobs.sample-os-process/context []
    (GET "/" request
      (get-job-resources (:uri request)))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (httpd/jobdb-key request) job-id))
    (PUT "/:job-id" [job-id :as request]
      (update-job (httpd/jobdb-key request) job-id))
    (HEAD "/:job-id" [job-id :as request]
      (get-info (httpd/jobdb-key request) job-id))
    (GET "/status/:job-id" [job-id :as request]
      (get-job-status (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
