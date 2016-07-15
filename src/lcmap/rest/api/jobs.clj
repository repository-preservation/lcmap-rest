(ns lcmap.rest.api.jobs
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

;;; XXX consider RFC 6570 - URI Templates for link syntax
(defn get-resources [context]
  (log/info (str "get-resources: " context))
  {:links (map #(str context %) ["" ":job-id" "status/:job-id"])})

(defn get-job-status
  ([result]
    (apply #'http/response (mapcat identity result)))
  ([component job-id]
    (match [(first @(db/job? (db/get-conn component) job-id))]
      [[]]
        {:body ["Job not found."]
         :status status/no-resource}
      [nil]
        {:body ["Job not found."]
         :status status/no-resource}
      [({:status (st :guard #'status/pending?)} :as result)]
        {:body :pending
         :status status/pending}
      [({:status st} :as result)]
        {:body (get-result-path job-id)
         :status st})))

(defn get-job-result
  ([component job-id]
    (let [conn (db/get-conn component)
          result-table (db/get-results-table conn job-id)]
      (log/debugf "Got result-table: %s (type: %s)" result-table (type result-table))
      (get-job-result conn job-id result-table #'get-job-status)))
  ([conn job-id result-table func]
    (db/get-job-result conn job-id result-table func)))

(defn update-job [component job-id]
  "sample job update tbd")

(defn get-info [component job-id]
  "sample job info tbd")

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.jobs/context []
    (GET "/" request
         (->> (get-resources (:uri request))
              (http/response :body)))
    (GET "/:job-id" [job-id :as request]
         (->> (get-job-result (:component request) job-id)
              (http/response*)))
    (PUT "/:job-id" [job-id :as request]
         (->> (update-job (:component request) job-id)
              (http/response :status status/pending :body)))
    (HEAD "/:job-id" [job-id :as request]
          (->> (get-info (:component request) job-id)
               (http/response :status status/ok :body)))
    (GET "/status/:job-id" [job-id :as request]
         (->> (get-job-status (:component request) job-id)
              (http/response*)))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
