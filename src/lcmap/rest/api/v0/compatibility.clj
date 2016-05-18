(ns lcmap.rest.api.v0.compatibility
  (:require [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [ring.util.response :as ring]
            [lcmap.client.compatibility]
            [lcmap.client.status-codes :as status]
            ; [lcmap.rest.api.v0.compatibility.wmts]
            ))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "compatibility resources tbd")
    status/ok))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.compatibility/context []
    (GET "/" request
      (get-resources (:uri request))))
  ;;lcmap.rest.api.v0.compatibility.wmts/routes
  )

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
