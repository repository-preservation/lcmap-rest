(ns lcmap.rest.shared-test
  (:require [clojure.tools.logging :as logging]
            [com.stuartsierra.component :as component]
            [dire.core :refer [with-handler!]]
            [lcmap.rest.components :as rest-components]
            [lcmap.rest.config :as rest-config]
            [lcmap.rest.app :as rest-app]
            [lcmap.config.helpers :as config-helpers])
  (:import  [com.datastax.driver.core.exceptions NoHostAvailableException]))

(with-handler! #'component/start
  NoHostAvailableException
  (fn [e & args]
    (logging/warn "no db host -- not unusual")
    args))

(def cfg-file (clojure.java.io/file config-helpers/*lcmap-config-dir* "lcmap.test.ini"))

(def cfg-opts (merge rest-config/defaults {:ini cfg-file}))

(defmacro with-system
  [[binding cfg-opts] & body]
  `(let [~binding (component/start (rest-components/init rest-app/app))]
     (try
       (do ~@body)
       (finally
         (component/stop ~binding)))))

;; XXX lcmap-client-clj does not use a component for configuration in
;; this namespace. Given the utility-like nature of these functions
;; I don't think adapting it to use components make sense for now.
#_ (alter-var-root #'lcmap.client.http/*http-config*
                (fn [_] (cfg-opts :lcmap.client)))
