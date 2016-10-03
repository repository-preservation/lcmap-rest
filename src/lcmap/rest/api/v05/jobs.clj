(ns lcmap.rest.api.v05.jobs
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.jobs]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.see.job.db :as db]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-result-path
  [result-id]
  (format "%s/%s" lcmap.client.jobs/context result-id))

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  (http/response
    :result {:links (map #(str context %) ["" ":job-id" "status/:job-id"])}))

(defn get-job-status
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
        (http/response :result (get-result-path job-id)
                       :status st))))

(defn get-job-result
  ([component job-id]
    (let [conn (db/get-conn component)
          result-table (db/get-results-table conn job-id)]
      (log/debugf "Got result-table: %s (type: %s)" result-table (type result-table))
      (get-job-result conn job-id result-table #'get-job-status)))
  ([conn job-id result-table func]
    (db/get-job-result conn job-id result-table func)))

(defn update-job [component job-id]
  (http/response :result "sample job update tbd"
                 :status status/pending))

(defn get-info [component job-id]
  (http/response :result "sample job info tbd"))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.jobs/context []
    (GET "/" request
      (get-resources (:uri request)))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (:component request) job-id))
    (PUT "/:job-id" [job-id :as request]
      (update-job (:component request) job-id))
    (HEAD "/:job-id" [job-id :as request]
      (get-info (:component request) job-id))
    (GET "/status/:job-id" [job-id :as request]
      (get-job-status (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
