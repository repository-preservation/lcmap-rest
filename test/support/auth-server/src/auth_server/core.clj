(ns auth-server.core
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as httpkit]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [ring.util.response :as ring]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.logger :as logger]
            [leiningen.core.project :as lein-prj])
  (:gen-class))

(def good-token "3efc6475b5034309af00549a77b7a6e3")

(defn do-no-username []
  (ring/response {:data nil
                  :errors ["Username is required"]
                  :status 31}))

(defn do-no-password []
  (ring/response {:data nil
                  :errors ["Password is required"]
                  :status 31}))

(defn do-bad-creds []
  (ring/response {:data nil
                  :errors ["Invalid username/password"]
                  :status 20}))

(defn do-good-auth []
  (ring/response {:data {:authToken good-token}
                  :status 10}))

(defn do-bad-token []
  ;; XXX find out what the HTTP response is for a bad/expired token
  (ring/status (ring/response nil) 403))

(defn do-good-token []
  (ring/response {:data {:affiliation "U.S. Federal Government"
                         :agency "Geological Survey (USGS)"
                         :contact_id "001010111"
                         :email "alice@usgs.gov"
                         :roles ["RPUBLIC" "LANDSAT8CUST"]
                         :username "alice"}
                  :status 10}))

(defn do-user-data [headers]
  (log/debug "Headers:" headers)
  (log/debug "Passed token:" (headers "x-authtoken"))
  (log/debug "Good token:" good-token)
  (if (not= (headers "x-authtoken") good-token)
    (do-bad-token)
    (do-good-token)))

(defroutes v1
  (POST "/api/auth" [username password :as request]
    (cond
      (nil? username) (do-no-username)
      (nil? password) (do-no-password)
      (and (= username "alice") (= password "secret")) (do-good-auth)
      :else (do-bad-creds)))
  (GET "/api/me" request
    (do-user-data (:headers request))))

(defroutes app
  (-> v1
      (wrap-defaults api-defaults)
      (wrap-json-response)
      (logger/wrap-with-logger)))

(defn -main
  [& args]
  (let [cfg (lein-prj/read)
        port (:port cfg)]
    (log/infof "Starting test auth server on port %s ..." port)
    (httpkit/run-server #'app {:port port})))
