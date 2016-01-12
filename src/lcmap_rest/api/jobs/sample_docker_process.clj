(ns lcmap-rest.api.jobs.sample-docker-process
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :as ring]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap-client.http :as http]
            [lcmap-client.jobs.sample-docker-process]
            [lcmap-client.status-codes :as status]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.util :as util]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-result-path
  [result-id]
  (format "%s/%s"
          lcmap-client.jobs.sample-docker-process/context
          result-id))

(defn get-job-resources [request]
  (util/response :result "sample job resources tbd"))

(defn -job-status
  ([result]
    (apply #'util/response (mapcat identity result)))
  ([db job-id]
    (match [(first @(db/job? (:conn db) job-id))]
      [[]]
        (util/response :errors ["Job not found."]
                       :status status/no-resource)
      [nil]
        (util/response :errors ["Job not found."]
                       :status status/no-resource)
      [({:status (st :guard #'status/pending?)} :as result)]
        (util/response :result :pending
                       :status status/pending)
      [({:status st} :as result)]
        (util/response :result  (get-result-path job-id)
                       :status st))))

(defn get-job-status [db job-id]
  (let [result (-job-status db job-id)]
    (-> result
      (ring/response)
      (ring/status (:status result)))))

;; XXX
(defn parse-job [arg]
  (log/debug "Parsing job ...")
  (log/debug "Got args:" arg)
  arg)

(defn get-job-result
  ([db-conn job-id]
    (get-job-result db-conn job-id result-table #'-job-status))
  ([db-conn job-id table func]
    (-> (db/get-job-result db-conn job-id table func)
        ;; XXX
        (parse-job)
        (ring/response)
        (ring/status status/ok))))

(defn update-job [db job-id]
  (util/response :result "sample job update tbd"
                 :status status/pending))

(defn get-info [db job-id]
  (util/response :result "sample job info tbd"))

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
