(ns lcmap.rest.shared-test
  (:require [clojure.tools.logging :as logging]
            [com.stuartsierra.component :as component]
            [dire.core :refer [with-handler!]]
            [lcmap.data.system :as system]
            [lcmap.data.config :as config]
            [lcmap.config.helpers :as config-helpers]))

(def cfg-file (clojure.java.io/file config-helpers/*lcmap-config-dir* "lcmap.test.ini"))

(def cfg-opts (merge config/defaults {:ini cfg-file}))

(def test-system (-> (system/build cfg-opts) (component/start)))
