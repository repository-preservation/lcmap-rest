(ns lcmap.rest.auth.usgs
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [clj-http.client :as http]
            [lcmap.rest.exceptions :as exceptions]
            [lcmap.rest.util :as util]))

;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; Constants
;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

;;; USGS EROS status codes for the ERS authentication API

(def success 10)
(def auth-error 20)
(def generic-error 30)
(def input-error 31)

;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; Supporting functions
;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(defn make-auth-url [rest-cfg]
  (str (:auth-endpoint rest-cfg) (:auth-login-resource rest-cfg)))

(defn make-user-url [rest-cfg]
  (str (:auth-endpoint rest-cfg) (:auth-user-resource rest-cfg)))

(defn make-token-header [token]
  {:headers {:x-authtoken token}
   :as :json})

(defn make-cred-form [username password]
  {:form-params {:username username :password password}
   :as :json})

(defn check-status [ers-status errors]
  (if (util/in? [auth-error input-error generic-error] ers-status)
      ;; Note that the custom exception thrown here is caught by an
      ;; error handler in lcmap.rest.api.auth where an appropriate
      ;; message with error info payload is sent to the client.
      (throw+ (exceptions/auth-error (string/join "; " errors)))))

;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; DB calls for user/session data
;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(defn save-session-data [user-data token]
  ;; XXX save user data in db
  {:user-id (:contact_id user-data)
   :username (:username user-data)
   :roles (:roles user-data)
   :email (:email user-data)
   :token token})

;; XXX add a function to ensure that the given token is associated with valid
;; user/session data (e.g., ensure that the user hasn't logged out or been
;; logged out/invalidated by the system)
(defn valid-session? [conn token]
  )

;; XXX add a function that removes the saved user/session data (to be used by
;; the logout API function)
(defn remove-session-data [conn token]
  )

;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; HTTP calls to ERS
;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(defn- post-auth
  "Post to the USGS auth service and return the auth code."
  [rest-cfg username password]
  (let [url (make-auth-url rest-cfg)]
    (log/debugf "Authenticating to %s ..." url)
    (http/post url (make-cred-form username password))))

(defn- get-user [rest-cfg token]
  (http/get (make-user-url rest-cfg) (make-token-header token)))

;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; ERS data structure functions
;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

;;; The data structure of the results in these functions is defined by the
;;; ERS service over which the LCMAP project has no control. For this reason
;;; it does not match the payload structure defined in most other areas of
;;; the LCMAP codebase.

(defn get-user-data [rest-cfg token]
  (let [results (get-user rest-cfg token)]
    (log/debug "Extracting user data from" results)
    (get-in results [:body :data])))

(defn get-user-token [results]
  (get-in results [:body :data :authToken]))

(defn get-ers-status [results]
  (get-in results [:body :status]))

(defn get-ers-errors [results]
  (get-in results [:body :errors]))

;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; USGS Auth API
;;;>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(defn login [component username password]
  (let [rest-cfg (get-in component [:cfg :lcmap.rest])
        ; user-db (get-in component [:userdb])
        results (post-auth rest-cfg username password)
        token (get-user-token results)]
    (log/debug "Login results:" results)
    (check-status (get-ers-status results) (get-ers-errors results))
    (log/infof "User %s successfully authenticated with token %s"
               username
               token)
    (let [user-data (get-user-data rest-cfg token)]
      (log/debugf "Got user data %s for token %s" user-data token)
      ; (save-session-data (:conn user-db) user-data token)
      (save-session-data user-data token))))

(defn logout [component token]
  ; (let [rest-cfg (get-in component [:cfg :lcmap.rest])
  ;       user-db (get-in component [:userdb])]
  ;   (remove-session-data (:conn user-db) token)
  ;   (log/debug (str "Successfully removed token %s and associated "
  ;                   "session data") token)
  )
