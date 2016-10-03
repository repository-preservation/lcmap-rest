(ns lcmap.rest.api.models.sample-pipe
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.client.models.sample-pipe]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as job]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any OptionalStrBool]]
            [lcmap.rest.util :as util]
            [lcmap.see.backend :as see]
            [lcmap.see.model.sample-pipe]))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-model
  ""
  [^Any request
   ^OptionalStrBool number
   ^OptionalStrBool count
   ^OptionalStrBool bytes
   ^OptionalStrBool words
   ^OptionalStrBool lines]
  (log/debugf "run-model got args: %s" [number count bytes words lines])
  (let [component (:component request)
        backend-impl (get-in component [:see :backend])
        job-id (see/run-model
                 backend-impl
                 ["sample-pipe" number count bytes words lines])]
    (log/debug "Called sample-pipe ...")
    (http/response :result {:link {:href (job/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.sample-piped-processes/context []
    (POST "/" [token number count bytes words lines :as request]
      ;; XXX use token to check user/session/authorization
      (model/validate #'run-model request number count bytes words lines))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
