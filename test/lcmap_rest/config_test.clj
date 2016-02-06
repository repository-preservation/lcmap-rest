(ns lcmap-rest.config-test
  (:require [clojure.test :refer :all]
            [lcmap-rest.config :as config]))

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
