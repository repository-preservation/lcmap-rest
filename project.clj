(defproject gov.usgs.eros/lcmap-rest "0.1.0-dev"
  :description "LCMAP REST Service API"
  :url "https://github.com/USGS-EROS/lcmap-rest"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/core.memoize "0.5.8"]
                 ;; Componentization
                 [com.stuartsierra/component "0.3.0"]
                 ;; Logging and Error Handling
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [ring.middleware.logger "0.5.0"]
                 [dire "0.5.3"]
                 [slingshot "0.12.2"]
                 ;; REST
                 [compojure "1.4.0"]
                 [http-kit "2.1.17"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 ;; Authentication and authorization
                 [com.uswitch/clj-soap "0.2.3"] ; USGS ERS is a SOAP service
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
                 ;; uncomment the dependancy below and remove the temporary
                 ;; dependancies below.
                 ;;[gov.usgs.eros/lcmap-client-clj "0.1.0-dev"]
                 ;; XXX note that we may still need to explicitly include the
                 ;; Apache Java HTTP client, since the version used by the LCMAP
                 ;; client is more recent than that used by Chas Emerick's
                 ;; 'friend' library (the conflict causes a compile error which
                 ;; is worked around by explicitly including Apache Java HTTP
                 ;; client library).
                 ;; XXX temp dependencies:
                 [org.apache.httpcomponents/httpclient "4.5"]
                 [clojure-ini "0.0.2"]
                 [clj-http "2.0.0"]
                 ;; Dev and project metadata
                 [leiningen-core "2.5.3"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-pprint "1.1.1"]]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.3"]]
  :jvm-opts ["-Dco.paralleluniverse.fibers.detectRunawayFibers=false"]
  :repl-options {:init-ns lcmap-rest.dev}
  :main lcmap-rest.app
  :target-path "target/%s"
  ;; List the namespaces whose log levels we want to control; note that if we
  ;; add more dependencies that are chatty in the logs, we'll want to add them
  ;; here.
  :logging-namespaces [lcmap-rest
                       lcmap-client
                       com.datastax.driver
                       co.paralleluniverse]
  :profiles {
    ;; configuration for local environment
    :local
      {:active-profile "local"
       :db {:hosts ["127.0.0.1"]
            :port 9042
            :protocol-version 3
            :keyspace "lcmap"}
        :http {:port 1077     ; port number obtained via this bit of geekery:
               :ip "0.0.0.0"} ;   (reduce + (map int "USGS-EROS LCMAP"))
        :log-level :info
        :dependencies [[org.clojure/tools.namespace "0.2.11"]
                       [slamhound "1.5.5"]]
        :aliases {"slamhound" ["run" "-m" "slam.hound"]}}
    ;; configuration for dev environment
    :dev
      {:active-profile "dev"
       :db {:hosts ["127.0.0.1"]
            :port 9042
            :protocol-version 3
            :keyspace "lcmap"}
        :http {:port 1077
               :ip "0.0.0.0"}
        :log-level :debug
        :dependencies [[org.clojure/tools.namespace "0.2.11"]
                       [slamhound "1.5.5"]]
        :aliases {"slamhound" ["run" "-m" "slam.hound"]}
        :plugins [[lein-kibit "0.1.2"]]}
    ;; configuration for testing environment
    :testing
      {:active-profile "testing"
       :db {}
       :http {}
       :log-level :debug}
    ;; configuration for staging environment
    :staging
      {:active-profile "staging"
       :db {}
       :http {}
       :log-level :warn}
    ;; configuration for production environment
    :prod
      {:active-profile "prod"
       :db {}
       :http {}
       :log-level :error}})
