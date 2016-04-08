(ns lcmap.rest.app
  (:require [clojure.tools.logging :as log]
            [ring.middleware.defaults :as ring-defaults]
            [ring.middleware.logger :as ring-logger]
            [clojusc.ring.xml :as ring-xml]
            [com.stuartsierra.component :as component]
            [twig.core :as logger]
            [lcmap.rest.api.routes :as routes]
            [lcmap.rest.components :as components]
            [lcmap.rest.middleware :as middleware]
            [lcmap.rest.util :as util])
  (:gen-class))

(def default-version "0.5")
(def base-handler {})

(def app
  (-> base-handler
      (middleware/lcmap-handlers default-version)
      ;; XXX once we support SSL, api-defaults needs to be changed to
      ;; ring-defaults/secure-api-defaults
      (ring-defaults/wrap-defaults ring-defaults/api-defaults)
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
  (let [system (components/init #'app)]
    (log/info "LCMAP REST server's local IP address:" (util/get-local-ip))
    (component/start system)
    ;; Setup interrupt/terminate handling
    (util/add-shutdown-handler #(component/stop system))))
