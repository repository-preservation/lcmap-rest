(ns lcmap.rest.auth.usgs
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [clj-http.client :as http]
            [lcmap.rest.exceptions :as exceptions]
            [lcmap.rest.util :as util]))

;; USGS EROS status codes for the ERS authentication API
(def success 10)
(def auth-error 20)
(def generic-error 30)
(def input-error 31)

(defn- post-auth
  "Post to the USGS auth service and return the auth code."
  [httpd-cfg username password]
  (let [url (str (:auth-endpoint httpd-cfg) (:auth-login-resource httpd-cfg))]
    (log/debugf "Authenticating to %s ..." url)
    (http/post url
               {:form-params {:username username :password password}
                :as :json})))

(defn- get-user [httpd-cfg token]
  (http/get (str (:auth-endpoint httpd-cfg) (:auth-user-resource httpd-cfg))
            {:headers {:x-authtoken token}
             :as :json}))

(defn get-user-data [httpd-cfg token]
  (let [results (get-user httpd-cfg token)]
    (log/debug "Extracting user data from" results)
    (get-in results [:body :data])))

(defn check-status [ers-status errors]
  (if (util/in? [auth-error input-error generic-error] ers-status)
      (throw+ (exceptions/auth-error (string/join "; " errors)))))

(defn login [component username password]
  (let [httpd-cfg (get-in component [:cfg :lcmap.rest])
        results (post-auth httpd-cfg username password)
        token (get-in results [:body :data :authToken])]
    (log/debug "Login results:" results)
    (check-status (get-in results [:body :status])
                  (get-in results [:body :errors]))
    (log/infof "User %s successfully authenticated with token %s"
               username
               token)
    (let [user-data (get-user-data httpd-cfg token)]
      (log/debugf "Got user data %s for token %s" user-data token)
      ;; XXX save user data in db
      {:user-id (:contact_id user-data)
       :username (:username user-data)
       :roles (:roles user-data)
       :email (:email user-data)
       :token token})))

(defn logout [component token]
  ;; XXX delete the records for the user session/token
  )
