(ns lcmap.rest.api.models.core
  (:require [clojure.tools.logging :as log]
            [dire.core :refer [with-handler!]]
            [schema.core :as schema]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.middleware.http-util :as http]))

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
