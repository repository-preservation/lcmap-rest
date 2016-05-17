(ns lcmap.rest.api.models.ccdc
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.models.ccdc]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as jobs]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.sample :as sample-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "XXX")
(def science-model-name "ccdc model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [db eventd arg1 arg2]
  (log/debugf "run-model got args: [%s %s]" arg1 arg2)
  (let [job-id (util/get-args-hash "ccdc" :arg1 arg1 :arg2 arg2)]
    (str "model run (job id: " job-id ") tbd")))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.ccdc/context []
    (POST "/" [arg1 arg2 :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (httpd/jobdb-key request)
                 (httpd/eventd-key request)
                 arg1
                 arg2))
    (GET "/:job-id" [job-id :as request]
      (jobs/get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
