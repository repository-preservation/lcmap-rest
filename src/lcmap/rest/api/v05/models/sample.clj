(ns lcmap.rest.api.v05.models.sample
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.client.models.sample]
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

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-model
  ""
  [^Any request
   ^StrInt seconds
   ^StrYear year]
  (log/debugf "run-model got args: [%s %s]" seconds year)
  (let [component (:component request)
        backend-impl (get-in component [:see :backend])
        job-id (see/run-model
                backend-impl
                ["sample" seconds year])]
        ; job-id "dummy"]
    (log/debugf "Got backend in REST API: %s (%s)" backend-impl (type backend-impl))
    (log/debug "Called sample-runner; got id: " job-id)
    (log/debug "Type of job-id:" (type job-id))
    (http/response :result {:link {:href (job/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample/context []
    (POST "/" [token delay year :as request]
      ;; XXX use token to check user/session/authorization
      (model/validate #'run-model request delay year))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
