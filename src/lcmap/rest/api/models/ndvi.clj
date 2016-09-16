(ns lcmap.rest.api.models.ndvi
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.rest.api.jobs :as job]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any Str StrBool StrInt StrDate]]
            [lcmap.rest.util :as util]
            [lcmap.see.backend.core :as see]
            [lcmap.see.model.ndvi :as ndvi]))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context "/api/models/ndvi" []
    (POST "/" [token x y t1 t2 :as request]
      (log/debug "Create NDVI job" x y t1 t2)
      (let [see-backend (get-in request [:component :see :backend])
            run-ndvi-model (see/get-model see-backend "ndvi")
            result (run-ndvi-model "fake-job-id" x y t1 t2)]
        (http/response :result result)))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
