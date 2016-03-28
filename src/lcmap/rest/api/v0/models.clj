(ns lcmap.rest.api.v0.models
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap.client.models]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.v0.models.ccdc]
            [lcmap.rest.api.v0.models.ccdc-docker-process]
            [lcmap.rest.api.v0.models.ccdc-piped-processes]
            [lcmap.rest.api.v0.models.sample-docker-process]
            [lcmap.rest.api.v0.models.sample-os-process]
            [lcmap.rest.api.v0.models.sample-piped-processes]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "models resources tbd")
    status/ok))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models/context []
    (GET "/" request
      (get-resources (:uri request))))
  lcmap.rest.api.v0.models.ccdc/routes
  lcmap.rest.api.v0.models.ccdc-docker-process/routes
  lcmap.rest.api.v0.models.ccdc-piped-processes/routes
  lcmap.rest.api.v0.models.sample-docker-process/routes
  lcmap.rest.api.v0.models.sample-os-process/routes
  lcmap.rest.api.v0.models.sample-piped-processes/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
