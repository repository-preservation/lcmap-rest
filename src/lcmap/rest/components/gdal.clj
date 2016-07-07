(ns lcmap.rest.components.gdal
  "The GDAL component ensures that the GDAL library is initialized. Without
   initializing GDAL, opening datasets will fail."
  (:require [com.stuartsierra.component :as component]
            [gdal.core :as gc]
            [clojure.tools.logging :as log]))

(defrecord GDAL []
  component/Lifecycle
  (start [component]
    (log/info "Starting GDAL component ...")
    (gc/init)
    component)
  (stop [component]
    (log/info "Stopping GDAL component ...")
    component))

(defn new-gdal []
  (log/debug "Building GDAL component ...")
  (->GDAL))
