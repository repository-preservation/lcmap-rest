(ns lcmap-rest.main
  (:require [clojure.tools.logging :as log]
            [org.httpkit.server :as httpkit]
            [lcmap-rest.routes :as routes])
  (:gen-class))

(defn -main
  "This is the entry point.

  'lein run' will use this as well as 'java -jar'."
  [& args]
  (log/info "Starting Compojure server ...")
  (httpkit/run-server #'routes/app {:port 8080}))
