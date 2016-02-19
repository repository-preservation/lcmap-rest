(ns lcmap.rest.auth.usgs
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [clj-http.client :as http]
            [lcmap.client.status-codes :as status]
            [lcmap.rest.exceptions :as exceptions]
            [lcmap.rest.util :as util]))

;; USGS EROS status codes for the ERS authentication API
(def success 10)
(def auth-error 20)
(def generic-error 30)
(def input-error 31)

(defn- post-auth [auth-cfg username password]
  "Post to the USGS auth service and return the auth code."
  (http/post (str (:endpoint auth-cfg) (:login-resource auth-cfg))
             {:form-params {:username username :password password}
              :as :json}))

(defn- get-user [auth-cfg token]
  (http/get (str (:endpoint auth-cfg) (:user-resource auth-cfg))
            {:headers {:x-authtoken token}
             :as :json}))

(defn get-user-data [auth-cfg token]
  (let [results (get-user auth-cfg token)]
    (log/debugf "Got user data %s for token %s" results token)
    (get-in results [:body :data])))

(defn check-status [ers-status errors]
  (if (util/in? [auth-error input-error generic-error] ers-status)
      (throw+ (exceptions/auth-error (string/join "; " errors)))))

(defn login [auth-cfg username password]
  (let [results (post-auth auth-cfg username password)
        ers-status (get-in results [:body :status])
        errors (get-in results [:body :errors])
        token (get-in results [:body :data :authToken])]
    (check-status ers-status errors)
    (log/infof "User %s successfully authenticated with token %s"
               username
               token)
    (let [user-data (get-user-data auth-cfg token)]
      ;; XXX save user data in db
      {:user-id (:contact_id user-data)
       :username (:username user-data)
       :roles (:roles user-data)
       :email (:email user-data)
       :token token})))

(defn logout [auth-cfg db-conn token]
  ;; XXX delete the records for the user session/token
  )

