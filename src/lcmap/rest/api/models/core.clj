(ns lcmap.rest.api.models.core
  (:require [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [schema.core :as schema]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.exceptions :as exceptions]
            [lcmap.rest.errors :as errors]
            [lcmap.rest.middleware.http-util :as http]))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn validate
  "A general purpose API function parameters validation function.

  The various models supported by the LCMAP REST API use this function in order
  to check that developers (and their applications) are passing the appropriate
  data types in their function arguments."
  [func & args]
  (try
    (schema/with-fn-validation
      (apply func args))
    (catch RuntimeException e
      (-> (.getMessage e)
          (exceptions/type-error)
          (throw+)))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(http/add-error-handler
  #'validate
  [:type 'Type-Error]
  errors/invalid-type
  status/client-error)
