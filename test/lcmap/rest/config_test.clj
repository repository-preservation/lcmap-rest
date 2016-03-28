(ns lcmap.rest.config-test
  (:require [clojure.test :refer :all]
            [omniconf.core :as cfg]
            [clojure-ini.core :as ini]
            [lcmap.rest.config :as config]))

;;; Fixture to to wrap all tests in the namespace, called just once

(def ini-path "test/data/sample-config.ini")
(def edn-path "test/data/sample-config.edn")
(def ^:dynamic *config-ini*)
(def ^:dynamic *config-edn*)

(defn setup-once [test-fn]
  (with-redefs [*config-ini* (config/read ini-path :type :ini)
                *config-edn* (config/read edn-path :type :edn)]
    (test-fn)))

(defn teardown-once [])

(defn fixture-once [test-fn]
  (setup-once test-fn)
  (teardown-once))

(use-fixtures :once fixture-once)

(deftest make-env-name-test
  (is (= "LCMAP_SERVER" (config/make-env-name)))
  (is (= "LCMAP_SERVER_ENV" (config/make-env-name [:env])))
  (is (= "LCMAP_SERVER_ENV_DB" (config/make-env-name [:env :db])))
  (is (= "LCMAP_SERVER_ENV_DB_HOST" (config/make-env-name [:env :db :host])))
  (is (= "LCMAP_SERVER_ENV_LOG_LEVEL" (config/make-env-name [:env :log-level]))))

(deftest parse-env-var-test
  (is (= nil (config/parse-env-var nil [:nope])))
  (is (= nil (config/parse-env-var "" [:nope])))
  (is (= "thing" (config/parse-env-var "thing" [:nope])))
  (is (= "thing1:thing2" (config/parse-env-var "thing1:thing2" [:nope])))
  (is (= ["thing1"] (config/parse-env-var "thing1" [:env :db :hosts])))
  (is (= ["thing1" "thing2"] (config/parse-env-var "thing1:thing2" [:env :db :hosts])))
  (is (= nil (config/parse-env-var nil [:env :db :hosts]))))

(deftest config-test
  (is (= "" *config-ini*)))
