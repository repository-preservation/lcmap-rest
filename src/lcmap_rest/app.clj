(ns lcmap-rest.app
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as httpkit]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [compojure.core :refer [defroutes]]
            [compojure.response :as response]
            [lcmap-rest.api.routes :as routes]
            [lcmap-rest.util :as util])
  (:gen-class))

(def default-api-version #'routes/v0)

(defn get-api-version [version default]
  (cond
    (and (>= version 0.0) (< version 1.0)) #'routes/v0
    (and (>= version 1.0) (< version 2.0)) #'routes/v1
    (and (>= version 2.0) (< version 3.0)) #'routes/v2
    :else default))

(defn version-handler
  "This is a custom handler "
  [default-api]
  (fn [request]
    (let [headers (:headers request)
          ;; This next line is nuts and took a while to figure out -- results
          ;; are rendered in log files as symbols, but (headers 'accept) and
          ;; ('accept headers) didn't work. After an inordinate amount of trial
          ;; and error, it was discovered that the header keys are actually in
          ;; lower-case strings at this point in the middleware chain.
          accept (headers "accept")
          {version :version} (util/parse-accept-version accept)
          api (get-api-version version default-api)]
      ;; ;; XXX debug
      ;; (log/info (str "Headers: " headers))
      ;; (log/info (str "Accept: " accept))
      ;; (log/info (str "Version: " version))
      ;; (log/info (str "API: " api))
      (response/render (api request) request))))

(defroutes app
  (-> default-api-version
      version-handler
      ;; XXX once we support SSL, api-defaults needs to be changed to
      ;; secure-api-defaults
      (wrap-defaults api-defaults)
      (wrap-json-response)))

(defn -main
  "This is the entry point.

  'lein run' will use this as well as 'java -jar'."
  [& args]
  (let [cfg (util/get-config)
        http-cfg (:http cfg)]
    (log/debug "Using lein profile:" (:active-profile cfg))
    (log/info (format "Starting Compojure server with config: %s ..." http-cfg))
    (httpkit/run-server #'app http-cfg)))
