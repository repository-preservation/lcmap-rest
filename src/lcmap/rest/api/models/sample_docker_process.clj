(ns lcmap.rest.api.models.sample-docker-process
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.client.models.sample-docker-process]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as job]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any Str StrYear]]
            [lcmap.rest.util :as util]
            [lcmap.see.backend.core :as see]
            [lcmap.see.model.sample-docker]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "samplemodel")
(def science-model-name "sample docker")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-default-row
  ""
  [id]
  (model/make-default-row id result-table science-model-name))

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
  (let [see-backend (get-in request [:component :see :backend])
        run-sample-docker-model (see/get-model see-backend "sample-docker")
        job-id (util/get-args-hash
                 science-model-name :docker-tag docker-tag :year year)]
    (run-sample-docker-model
      (:component request)
      job-id
      (make-default-row job-id)
      result-table
      docker-tag
      year)
    (log/debug "Called sample-docker-runner ...")
    (http/response :result {:link {:href (job/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-docker-process/context []
    (POST "/" [token docker-tag year :as request]
      ;; XXX use token to check user/session/authorization
      ;;(log/debug "Request data keys in routes:" (keys request))
      (model/validate #'run-model request docker-tag year))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
