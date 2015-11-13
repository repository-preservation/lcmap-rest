(ns lcmap-rest.user.auth.nasa
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [clj-http.client :as http]
            [lcmap-rest.status-codes :as status]))

(defn login [username password]
  (log/debugf "Got username '%s' and password '%s' ...")
  ;; XXX set up cookies
  ;; XXX generate UUID to associate this request with response from NASA's URS
  ;; XXX make request to NASA, following redirects
  (ring/status
    (ring/response "Authentication pending ...")
    status/ok))

(defn save-oauth-code [auth-request-id code]
  (log/debug "Got auth-request-id" auth-request-id)
  (log/debugf "Saving key '%s' ..." code)
  (ring/status
    (ring/response "OAuth2 code save logic TBD ...")
    status/ok))

; XXX check-results needs to do the following:
;  * See if the user was properly authenticated by URS
;  * If not, return 401 (or whatever's appropriate)
;  * If so, extract the URS auth code and save it
;  * Extract the state data (request UUID, uniquely linking this response
;    to the user that originally made it)
;  * Link the user with the URS code
;  * The client will receive the URS code and that will be used in all
;    subsequent client requests

; Subsequent client requests will pass the URS OAuth2 code in GET query
; params or in POST data. This code will be used to look up the user and
; get their role(s)/permissions on each request, to ensure they have
; access to the requested resource.
(defn check-results [auth-request-id code]
  ;; XXX if the results are good
  (save-oauth-code auth-request-id code)
  ;; * save the auth code
  ;; * look up the user associated with the auth-request-id
  ;; * get the role/perms info for that user
  ;; XXX if the results are not good, return the appropriate error/status code
  )
