(ns lcmap.rest.api.v0.models.ccdc-docker-process
  (:require [clojure.tools.logging :as log]
            [clojure.core.match :refer [match]]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap.rest.api.v0.jobs.ccdc-docker-process :refer [get-result-path
                                                                get-job-result
                                                                result-table]]
            [lcmap.client.models.ccdc-docker-process]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.see.util :as util]
            [lcmap.see.job.db :as db]
            [lcmap.see.model.ccdc-docker :as ccdc-docker-runner]))

;;; Supporting Constants ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def science-model-name "ccdc")
(def result-keyspace "lcmap")

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-model [component row col in-dir out-dir scene-list verbose]
  (log/debugf "run-model got args: %s" [row col in-dir out-dir scene-list verbose])
  (let [job-id (util/get-args-hash science-model-name
                                   :row row
                                   :col col
                                   :in-dir in-dir
                                   :out-dir :out-dir
                                   :scene-list scene-list
                                   :verbose verbose)
        default-row {:science_model_name science-model-name
                     :result_keyspace result-keyspace
                     :result_table result-table
                     :result_id job-id
                     :status status/pending}]
    ;;(log/debugf "ccdc model run (job id: %s)" job-id)
    ;;(log/debugf "default row: %s" default-row)
    (ccdc-docker-runner/run-model
      component
      job-id
      default-row
      result-table
      row col in-dir out-dir scene-list verbose)
    (log/debug "Called ccdc-runner ...")
    (ring/status
      (ring/response
        {:result
          {:link {:href (get-result-path job-id)}}})
      status/pending-link)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models.ccdc-docker-process/context []
    (POST "/" [token row col in-dir out-dir scene-list verbose :as request]
      ;;(log/debugf "POST request got: %s" request)
      ;;(log/debug "Request data keys in routes:" (keys request))-
      (run-model (:component request)
                 row col in-dir out-dir scene-list verbose))
    (GET "/:job-id" [job-id :as request]
      (get-job-result (:component request) job-id))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
