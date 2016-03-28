(ns lcmap.rest.api.v0.four-oh-four
  (:import [java.lang Runtime])
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [compojure.route :as route]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap.client.status-codes :as status]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (route/not-found {:result nil :errors ["Resource not found"]}))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
