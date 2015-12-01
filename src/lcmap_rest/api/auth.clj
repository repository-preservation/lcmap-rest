(ns lcmap-rest.api.auth
  (:import [java.lang Runtime])
  (:require [clojure.tools.logging :as log]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap-rest.auth.usgs :as usgs]))

;; XXX add db-connection as parameter
(defn login [username password]
  (ring/response
    (usgs/login username password)))

(defn logout [db-conn token]
  (ring/response
    (usgs/logout token)))

;;; Login exception handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(with-handler! #'login
  java.net.ConnectException
  (fn [e & args]
    (log/error "Cannot connect to remote server")
    (ring/response {:result nil :errors e})))

;; If we want to use our own exceptions, we can catch those in the following
;; manner:
(with-handler! #'login
  [:type 'Auth-Error]
  (fn [e & args]
    (log/error e)
    (ring/response {:result nil :errors e})))

;; HTTP error status codes returned as exceptions from clj-http
(with-handler! #'login
  [:status status/server-error]
  (fn [e & args]
    (log/error "Authentication server error")
    (ring/response {:result nil :errors e})))

(with-handler! #'login
  [:status status/forbidden]
  (fn [e & args]
    (log/error "Bad username or password")
    (ring/response {:result nil :errors e})))

(with-handler! #'login
  [:status status/no-resource]
  (fn [e & args]
    (log/error "Authentication resource not found")
    (ring/response {:result nil :errors e})))
