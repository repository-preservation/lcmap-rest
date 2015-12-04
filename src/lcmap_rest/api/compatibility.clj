(ns lcmap-rest.api.compatibility
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap-client.compatibility]
            [lcmap-client.status-codes :as status]
            [lcmap-rest.api.compatibility.wmts]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "compatibility resources tbd")
    status/ok))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.compatibility/context []
    (GET "/" request
      (get-resources (:uri request))))
  ;;lcmap-rest.api.compatibility.wmts/routes
  )

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
