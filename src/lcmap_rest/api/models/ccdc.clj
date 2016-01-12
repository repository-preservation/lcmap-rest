(ns lcmap-rest.api.models.ccdc
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap-client.models.ccdc]
            [lcmap-client.status-codes :as status]
            [lcmap-rest.api.jobs.ccdc :refer [get-result-path
                                              get-job-result
                                              result-table]]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.job.db :as db]
            [lcmap-rest.job.sample-runner :as sample-runner]
            [lcmap-rest.util :as util]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def science-model-name "ccdc model")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [db eventd arg1 arg2]
  (log/debugf "run-model got args: [%s %s]" arg1 arg2)
  (let [job-id (util/get-args-hash "ccdc" :arg1 arg1 :arg2 arg2)]
    (str "model run (job id: " job-id ") tbd")))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.models.ccdc/context []
    (POST "/" [arg1 arg2 :as request]
      ;;(log/debug "Request data keys in routes:" (keys request))
      (run-model (httpd/jobdb-key request)
                 (httpd/eventd-key request)
                 arg1
                 arg2))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (httpd/jobdb-key request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
