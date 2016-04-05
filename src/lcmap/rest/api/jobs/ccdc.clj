(ns lcmap.rest.api.jobs.ccdc
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [ring.util.response :as ring]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.jobs.ccdc]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.see.job.db :as db]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "XXX")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-result-path
  [result-id]
  (format "%s/%s"
          lcmap.client.jobs.ccdc/context
          result-id))

(defn get-job-resources [request]
  "job resources tbd")

(defn create-job [arg1 arg2]
  "job creation tbd")

(defn get-job-status [job-id]
  "job status tbd")

(defn get-job-result
  ([db-conn job-id]
    (get-job-result db-conn job-id result-table #'get-job-status))
  ([db-conn job-id table func]
    (-> (db/get-job-result db-conn job-id table func)
        ;; XXX need to unify this with lcmap.rest.middleware.http/response
        (ring/response)
        (ring/status status/ok))))

(defn update-job [job-id]
  "job update tbd")

(defn get-info [job-id]
  "job info tbd")

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.jobs.ccdc/context []
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
