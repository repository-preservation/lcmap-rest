(ns lcmap.rest.api.models.ccdc
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [schema.core :as schema]
            [lcmap.client.models.ccdc]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.jobs :as job]
            [lcmap.rest.api.models.core :as model]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.types :refer [Any Str StrBool StrInt StrDate]]
            [lcmap.rest.util :as util]
            ; [lcmap.see.job.db :as db]
            ; [lcmap.see.model.ccdc :as ccdc-runner]
            ))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def result-table "ccdcmodel")
(def science-model-name "ccdc")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn make-default-row
  ""
  [id]
  (model/make-default-row id result-table science-model-name))

;;; Science Model Execution ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(schema/defn run-model
  ""
  [^Any request
   ^Str spectra
   ^StrInt x-val
   ^StrInt y-val
   ^StrDate start-time
   ^StrDate end-time
   ^StrInt row
   ^StrInt col
   ^Str in-dir
   ^Str out-dir
   ^Any scene-list
   ^StrBool verbose]
  (log/debugf "run-model got args: %s" [spectra x-val y-val start-time end-time
                                        row col in-dir out-dir scene-list verbose])
  ;; generate job-id from hash of args
  ;; return status code 200 with body that has link to where the ccdc result will
  ;; be
  (let [job-id (util/get-args-hash science-model-name
                                   :spectra spectra
                                   :x-val x-val
                                   :y-val y-val
                                   :start-time start-time
                                   :end-time end-time
                                   :row row
                                   :col col
                                   :in-dir in-dir
                                   :out-dir out-dir
                                   :scene-list scene-list
                                   :verbose verbose)]
    (log/warn "Mesos infrastructure not yet in place!")
    ; (ccdc-runner/run-model
    ;   (:component request)
    ;   job-id
    ;   (make-default-row job-id)
    ;   result-table
    ;   spectra x-val y-val start-time end-time
    ;   row col in-dir out-dir scene-list verbose)
    ;   (log/debug "Called ccdc runner ...")
    (http/response :result {:link {:href (job/get-result-path job-id)}}
                   :status status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.ccdc/context []
    (POST "/" [token spectra x-val y-val start-time end-time
                     row col in-dir out-dir scene-list verbose :as request]
      (model/validate
        #'run-model request
        spectra x-val y-val start-time end-time
        row col in-dir out-dir scene-list verbose))
    (GET "/:job-id" [job-id :as request]
      (job/get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
