(ns lcmap.rest.api.models.core
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [schema.core :as schema]
            [lcmap.client.models.sample-os-process]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as jobs]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any StrInt StrYear]]
            [lcmap.rest.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.sample :as sample-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-default-row
  ""
  ([id table model-name]
    (make-default-row
      id result-keyspace table model-name status/pending))
  ([id keyspace table model-name]
    (make-default-row
      id keyspace table model-name status/pending))
  ([id keyspace table model-name default-status]
    {:science_model_name model-name
     :result_keyspace keyspace
     :result_table table
     :result_id id
     :status default-status}))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn validate
  ""
  [func & args]
  (try
    (schema/with-fn-validation
      (apply func args))
    (catch RuntimeException e
      (let [error (.getMessage e)]
        (log/error "Got error:" error)
        (->
            ;; XXX status/client-error doesn't exist?
            (http/response :errors [error]
                           :status status/server-error)
            ;; update to take mime sub-type from Accept
            (http/problem-header))))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(with-handler! #'validate
  RuntimeException
  (fn [e & args]
    (log/error "error: %s; args: %s" e args)
    (http/response :errors [e])))
