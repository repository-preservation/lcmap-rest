(defproject gov.usgs/lcmap-rest "0.1.0-dev"
  :description "LCMAP REST Service API"
  :url "https://github.com/USGS-EROS/lcmap-rest"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/data.xml "0.0.8"]
                 ;; Componentization
                 [com.stuartsierra/component "0.3.0"]
                 ;; Logging
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 ;; REST
                 [compojure "1.4.0"]
                 [http-kit "2.1.17"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [com.cemerick/friend "0.2.1"]
                 ;; Job Tracker
                 [org.clojure/core.memoize "0.5.6"] ; These two are not used directly, but
                 [org.clojure/core.cache "0.6.4"]   ; wihtout them an exception is raised
                 [co.paralleluniverse/pulsar "0.7.3"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [digest "1.4.4"]
                 ;; DB
                 [clojurewerkz/cassaforte "2.0.0"]
                 [net.jpountz.lz4/lz4 "1.3.0"]
                 [org.xerial.snappy/snappy-java "1.1.2"]
                 ;; XXX once lcmap-client-clj is released and is no longer
                 ;; being used from the local checkouts directory, we will
                 ;; uncomment the dependancy below and remove the its
                 ;; dependancies below.
                 ;;[gov.usgs/lcmap-client-clj "0.1.0-dev"]
                 ;; XXX note that we may still need to explicitly include the
                 ;; Apache Java HTTP client, since the version used by the LCMAP
                 ;; client is more recent than that used by Chas Emerick's
                 ;; 'friend' library (the conflict causes a compile error which
                 ;; is worked around by explicitly including Apache Java HTTP
                 ;; client library).
                 [org.apache.httpcomponents/httpclient "4.5"]
                 [clj-http "2.0.0"]
                 ;; Project metadata
                 [leiningen-core "2.5.3"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-pprint "1.1.1"]]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.3"]]
  :jvm-opts ["-Dco.paralleluniverse.fibers.detectRunawayFibers=false"]
  :main lcmap-rest.app
  :target-path "target/%s"
  :profiles {
    ;; overrride local properties in ./profiles.clj: {:local {...}}
    :local
      {:active-profile "local"
       :db {:hosts ["127.0.0.1"]
            :port 9042
            :protocol-version 3
            :keyspace "lcmap"}
        :http {:port 8080
               :ip "127.0.0.1"}}
    ;; configuration for dev environment
    :dev
      {:active-profile "dev"
       :db {:hosts ["127.0.0.1"]
            :port 9042
            :protocol-version 3
            :keyspace "lcmap"}
        :http {:port 8080
               :ip "127.0.0.1"}}
    ;; configuration for testing environment
    :testing
      {:active-profile "testing"
       :db {}}
    ;; configuration for staging environment
    :staging
      {:active-profile "staging"
       :db {}}
    ;; configuration for prod environment
    :prod
      {:active-profile "prod"
       :db {}}})
