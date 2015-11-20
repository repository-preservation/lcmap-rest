(ns lcmap-rest.user.auth.usgs
  (:require [clojure.tools.logging :as log]
            [slingshot.slingshot :refer [throw+]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [clj-http.client :as http]
            [lcmap-rest.exceptions :as exceptions]
            [lcmap-rest.status-codes :as status]))

(def api-url "https://ers.cr.usgs.gov/api")
(def auth-url (str api-url "/auth"))

(defn- post [username password]
  "Post to the USGS auth service and return the auth code."
  (http/post auth-url {:username username :password password}))

(defn login [username password]
  (throw+ (exceptions/auth-error "Bad password")))

(with-handler! #'login
  [:type 'Auth-Error]
  (fn [e & args]
    (println "Trouble logging in?")))
