(ns lcmap.rest.api.v0.data
  (:require [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap.client.data]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.api.v0.data.surface-reflectance]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "data resources tbd")
    status/ok))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.data/context []
    (GET "/" request
      (get-resources (:uri request))))
  lcmap.rest.api.v0.data.surface-reflectance/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
