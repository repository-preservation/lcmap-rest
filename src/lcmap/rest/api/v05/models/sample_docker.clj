(ns lcmap.rest.api.v05.models.sample-docker
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.client.models.sample-docker]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as job]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any Str StrYear]]
            [lcmap.rest.util :as util]
            [lcmap.see.backend :as see]
            [lcmap.see.backend.native.models.sample-docker]))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-model
  ""
  [^Any request
   ^Str docker-tag
   ^StrYear year]
  (log/debugf "run-model got args: [%s %s]" docker-tag year)
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where sample result will
  ;; be
  (let [component (:component request)
        backend-impl (get-in component [:see :backend])
        job-id (see/run-model
                 backend-impl
                 ["sample-docker" docker-tag year])]
    (log/debugf "Got backend in REST API: %s (%s)" backend-impl (type backend-impl))
    (log/debug "Called sample-docker; got id: " job-id)
    (http/response :result {:link {:href (job/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-docker/context []
    (POST "/" [token docker-tag year :as request]
      ;; XXX use token to check user/session/authorization
      ;;(log/debug "Request data keys in routes:" (keys request))
      (model/validate #'run-model request docker-tag year))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
