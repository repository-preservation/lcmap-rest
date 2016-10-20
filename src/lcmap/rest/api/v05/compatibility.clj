(ns lcmap.rest.api.v05.compatibility
  (:require [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.compatibility]
            ; [lcmap.rest.api.compatibility.wmts]
            [lcmap.rest.middleware.http-util :as http]
            ))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  "compatibility resources tbd")

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.compatibility/context []
    (GET "/" request
      (http/response :result
        (get-resources (:uri request)))))
  ;;lcmap.rest.api.compatibility.wmts/routes
  )

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
