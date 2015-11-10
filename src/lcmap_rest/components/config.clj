(ns lcmap-rest.components.config
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [lcmap-rest.util :as util]))

(defrecord Configuration []
  component/Lifecycle

  (start [component]
    (log/info "Setting up LCMAP configuration ...")
    (let [cfg (util/get-config)]
      (log/info "Using lein profile:" (:active-profile cfg))
      cfg))

  (stop [component]
    (log/info "Tearing down LCMAP configuration ...")
    (dissoc component :cfg)))

(defn new-configuration []
  (->Configuration))
