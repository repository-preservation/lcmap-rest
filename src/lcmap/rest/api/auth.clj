(ns lcmap.rest.api.auth
  (:import [java.lang Runtime])
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [lcmap.client.auth]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.auth.usgs :as usgs]
            [lcmap.rest.errors :as errors]
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
    (http/response :errors [(errors/process-error e errors/no-auth-conn)]
                   :status status/bad-gateway)))

;; If we want to use our own exceptions, we can catch those by checking the
;; key we used to define our error types (see lcmap.rest.exceptions).

(with-handler! #'usgs/login
  [:type 'Auth-Error]
  (fn [e & args]
    (http/response :errors [(errors/process-error e errors/bad-creds)]
                   :status status/unauthorized)))

;; HTTP error status codes returned as exceptions from clj-http

(with-handler! #'usgs/login
  [:status status/server-error]
  (fn [e & args]
    (http/response :errors [(errors/process-error e errors/auth-server-error)]
                   :status status/server-error)))

(with-handler! #'usgs/login
  [:status status/unauthorized]
  (fn [e & args]
    (http/response :errors [(errors/process-error e errors/bad-creds)]
                   :status status/unauthorized)))

(with-handler! #'usgs/login
  [:status status/no-resource]
  (fn [e & args]
    (http/response :errors [(errors/process-error e errors/auth-not-found)]
                   :status status/no-resource)))
