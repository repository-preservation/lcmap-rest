(ns lcmap.rest.api.v0.jobs.sample-piped-processes
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :as ring]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.jobs.sample-pipe]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.see.job.db :as db]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-result-path
  [result-id]
  (format "%s/%s"
          lcmap.client.jobs.sample-pipe/context
          result-id))

(defn get-job-resources [request]
  (http/response :result "sample job resources tbd"))

(defn -job-status
  ([result]
    (apply #'http/response (mapcat identity result)))
  ([component job-id]
    (match [(first @(db/job? (db/get-conn component) job-id))]
      [[]]
        (http/response :errors ["Job not found."]
                       :status status/no-resource)
      [nil]
        (http/response :errors ["Job not found."]
                       :status status/no-resource)
      [({:status (st :guard #'status/pending?)} :as result)]
        (http/response :result :pending
                       :status status/pending)
      [({:status st} :as result)]
        (http/response :result  (get-result-path job-id)
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
  ([component job-id]
    (get-job-result (db/get-conn component) job-id result-table #'-job-status))
  ([db-conn job-id table func]
    (-> (db/get-job-result db-conn job-id table func)
        ;; XXX
        (parse-job)
        (ring/response)
        (ring/status status/ok))))

(defn update-job [component job-id]
  (http/response :result "sample job update tbd"
                 :status status/pending))

(defn get-info [component job-id]
  (http/response :result "sample job info tbd"))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.jobs.sample-pipe/context []
    (GET "/" request
      (get-job-resources (:uri request)))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (:component request) job-id))
    (PUT "/:job-id" [job-id :as request]
      (update-job (:component request) job-id))
    (HEAD "/:job-id" [job-id :as request]
      (get-info (:component request) job-id))
    (GET "/status/:job-id" [job-id :as request]
      (get-job-status (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
