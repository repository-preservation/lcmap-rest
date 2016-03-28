(ns lcmap.rest.app
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as httpkit]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.logger :as ring-logger]
            [compojure.core :refer [defroutes]]
            [compojure.response :as response]
            [com.stuartsierra.component :as component]
            [twig.core :as logger]
            [lcmap.rest.api.routes :as routes]
            [lcmap.rest.components :as components]
            [lcmap.rest.util :as util])
  (:gen-class))

(def default-api-version #'routes/v0.5)

(defn get-api-version [version default]
  (cond
    (= version 0.0) #'routes/v0
    (= version 0.5) #'routes/v0.5
    (and (>= version 1.0) (< version 2.0)) #'routes/v1
    (and (>= version 2.0) (< version 3.0)) #'routes/v2
    :else default))

(defn version-handler
  "This is a custom Ring handler for extracting the API version from the Accept
  header and then selecting the versioned API route accordingly."
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
      ;; (log/tracef "Headers: %s" headers)
      ;; (log/tracef "Accept: %s" accept)
      (log/debugf "Processing request for version %s of the API ..." version)
      (log/debugf "Using API route %s ..." api)
      (response/render (api request) request))))

(defroutes app
  (-> default-api-version
      (version-handler)
      ;; XXX once we support SSL, api-defaults needs to be changed to
      ;; secure-api-defaults
      (wrap-defaults api-defaults)
      (wrap-json-response)
      ;; XXX maybe move this handler into the httpd component setup, that way
      ;; we could enable it conditionally, based upon some configuration value.
      (ring-logger/wrap-with-logger)))

(defn -main
  "This is the entry point. Note, however, that the system components are
  defined in lcmap.rest.components. In particular, lcmap.rest.components.system
  brings together all the defined (and active) components; that is the module
  which is used to bring the system up when (component/start ...) is called.

  'lein run' will use this as well as 'java -jar'."
  [& args]
  ;; Set the initial log-level before the components set the log-levels for
  ;; the configured namespaces
  (logger/set-level! ['lcmap] :info)
  (let [system (components/init #'app)
        local-ip  (.getHostAddress (java.net.InetAddress/getLocalHost))]
    (log/info "LCMAP REST server's local IP address:" local-ip)
    (component/start system)
    (util/add-shutdown-handler #(component/stop system))))
