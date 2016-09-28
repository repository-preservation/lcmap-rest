(ns lcmap.rest.api.models.sample-os-process
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.client.models.sample-os-process]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as job]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any StrInt StrYear]]
            [lcmap.rest.util :as util]
            [lcmap.see.backend :as see]
            [lcmap.see.job.db :as db]
            [lcmap.see.backend.native.models.sample]
            [lcmap.see.backend.mesos.models.sample]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX move these into lcmap.see
(def result-table "samplemodel")
;; XXX if we use a fully-qualified namespace, we won't need the model name hack
(def science-model-name "sample model")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX move into lcmap.see
(defn make-default-row
  ""
  [id]
  (model/make-default-row id result-table science-model-name))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-model
  ""
  [^Any request
   ^StrInt seconds
   ^StrYear year]
  (log/debugf "run-model got args: [%s %s]" seconds year)
  (let [component (:component request)
        backend-impl (get-in component [:see :backend])
        ;; XXX move into lcmap.see
        job-id (util/get-args-hash
                 science-model-name :delay seconds :year year)]
    (log/debugf "Got backend in REST API: %s (%s)" backend-impl (type backend-impl))
    (log/debug "Using job id: " job-id)
    (see/run-model
      backend-impl
      "sample"
      [component
       ;; XXX move next three args into lcmap.see
       job-id
       (make-default-row job-id)
       result-table
       seconds
       year])
    (log/debug "Called sample-runner ...")
    ;; XXX running the model needs to return the job id; this will then be used
    ;; in the HTTP response
    (http/response :result {:link {:href (job/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-os-process/context []
    (POST "/" [token delay year :as request]
      ;; XXX use token to check user/session/authorization
      (model/validate #'run-model request delay year))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
