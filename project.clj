(defproject gov.usgs/lcmap-rest "0.1.0-dev"
  :description "LCMAP REST Service API"
  :url "https://github.com/USGS-EROS/lcmap-rest"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/data.xml "0.0.8"]
                 ;; Logginga
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
                 ;; DB
                 [clojurewerkz/cassaforte "2.0.0"]
                 ;; XXX once lcmap-client-clj is released and is not longer
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
  :plugins [[lein-ring "0.9.7"]]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.3"]]
  :jvm-opts ["-Dco.paralleluniverse.fibers.detectRunawayFibers=false"]
  :main lcmap-rest.app)
