(ns lcmap.rest.api.auth
  (:import [java.lang Runtime])
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [lcmap.client.auth]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.auth.usgs :as usgs]
            [lcmap.rest.middleware.http-util :as http]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  (http/response :result
    {:links (map #(str context %) ["login" "logout"])}))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.auth/context []
    (GET "/" request
      (get-resources (:uri request)))
    (POST "/login" [username password :as request]
      (usgs/login (:component request) username password))
    (POST "/logout" [token :as request]
      (usgs/logout (:component request) token))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(with-handler! #'usgs/login
  java.net.ConnectException
  (fn [e & args]
    (log/error "Cannot connect to remote server")
    (http/response :errors [e] :status status/server-error)))

;; If we want to use our own exceptions, we can catch those in the following
;; manner:
(with-handler! #'usgs/login
  [:type 'Auth-Error]
  (fn [e & args]
    (log/error e)
    (http/response :errors [e] :status status/unauthorized)))

;; HTTP error status codes returned as exceptions from clj-http
(with-handler! #'usgs/login
  [:status status/server-error]
  (fn [e & args]
    (log/error "Authentication server error")
    (http/response :errors [e] :status status/server-error)))

(with-handler! #'usgs/login
  [:status status/forbidden]
  (fn [e & args]
    (log/error "Bad username or password")
    (http/response :errors [e] :status status/unauthorized)))

(with-handler! #'usgs/login
  [:status status/no-resource]
  (fn [e & args]
    (log/error "Authentication resource not found")
    (http/response :errors [e] :status status/no-resource)))
