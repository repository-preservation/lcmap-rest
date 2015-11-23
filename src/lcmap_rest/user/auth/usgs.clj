(ns lcmap-rest.user.auth.usgs
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [clj-http.client :as http]
            [lcmap-rest.exceptions :as exceptions]
            [lcmap-rest.status-codes :as status]
            [lcmap-rest.util :as util]))

;; XXX uncomment the next line when the service is ready
;;(def api-url "https://ers.cr.usgs.gov/api")
;; XXX remove the next line when the ERS api is redy
(def api-url "http://localhost:8888")
(def auth-url (str api-url "/auth"))
(def user-url (str api-url "/me"))

;; USGS EROS status codes for the ERS authentication API
(def success 10)
(def auth-error 20)
(def generic-error 30)
(def input-error 31)

(defn- post-auth  [username password]
  "Post to the USGS auth service and return the auth code."
  (http/post auth-url
             {:form-params {:username username :password password}
              :as :json}))

(defn- get-user [token]
  (http/get user-url
            {:headers {:x-auth-token token}
             :as :json}))

(defn get-user-data [token]
  (let [results (get-user token)]
    (log/debugf "Got user data for token %s" token)
    (get-in results [:body :data])))

(defn check-status [ers-status errors]
  (condp #'util/in? ers-status
    [auth-error input-error generic-error]
      (throw (exception/Auth-Error (string/join "; " errors)))))

(defn login [username password]
  (let [results (post-auth username password)
        ers-status (get-in results [:body :status])
        errors (get-in results [:body :errors])
        token (get-in results [:body :data :authToken])]
    (check-status ers-status errors)
    (let [user-data (get-user-data token)]
      (log/infof "User %s successfully authenticated with token %s"
                 username
                 token)
      ;; XXX save user data in db
      {:user-id (:contact_id user-data)
       :username (:username user-data)
       :roles (:roles user-data)
       :email (:email user-data)
       :token token}))

;;; Login exception handling

(with-handler! #'login
  java.net.ConnectException
  (fn [e & args]
    (log/error "Cannot connect to remote server")))

;; If we want to use our own exceptions, we can catch those in the following
;; manner:
(with-handler! #'login
  [:type 'Auth-Error]
  (fn [e & args]
    (log/error e)))

;; HTTP error status codes returned as exceptions from clj-http
(with-handler! #'login
  [:status status/server-error]
  (fn [e & args]
    (log/error "Authentication server error")))

(with-handler! #'login
  [:status status/forbidden]
  (fn [e & args]
    (log/error "Bad username or password")))

(with-handler! #'login
  [:status status/no-resource]
  (fn [e & args]
    (log/error "Authentication resource not found")))
