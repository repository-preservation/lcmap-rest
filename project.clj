(defproject gov.usgs.eros/lcmap-rest "0.0.2-SNAPSHOT"
  :description "LCMAP REST Service API"
  :url "https://github.com/USGS-EROS/lcmap-rest"
  :license {:name "NASA Open Source Agreement, Version 1.3"
            :url "http://ti.arc.nasa.gov/opensource/nosa/"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.xml "0.1.0-beta1"]
                 [org.clojure/core.memoize "0.5.8"]
                 ;; Componentization
                 [com.stuartsierra/component "0.3.0"]
                 [com.grammarly/omniconf "0.2.2"]
                 ;; Logging and Error Handling -- note that we need to explicitly pull
                 ;; in a version of slf4j so that we don't get conflict messages on the
                 ;; console
                 [twig "0.1.6"]
                 [ring.middleware.logger "0.5.0" :exclusions [org.slf4j/slf4j-log4j12]]
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
                 [clojusc/ring-xml "0.0.6"]
                 ;; Authentication and authorization
                 [com.cemerick/friend "0.2.1"]
                 ;; Event system support
                 [com.novemberain/langohr "3.5.0"]
                 ;; Job Tracker
                 [org.clojure/core.memoize "0.5.6"] ; These two are not used directly, but
                 [org.clojure/core.cache "0.6.4"]   ; without them an exception is raised
                 [co.paralleluniverse/pulsar "0.7.3"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [digest "1.4.4"]
                 ;; DB
                 [clojurewerkz/cassaforte "2.0.0"]
                 [net.jpountz.lz4/lz4 "1.3.0"]
                 [org.xerial.snappy/snappy-java "1.1.2"]
                 ;; Note that the projects in ./checkouts override these:
                 [gov.usgs.eros/lcmap-data "0.0.1"]
                 [gov.usgs.eros/lcmap-see "0.0.1"]
                 [gov.usgs.eros/lcmap-client-clj "0.0.1"]
                 ;[gov.usgs.eros/lcmap-config "0.5.0-SNAPSHOT"]
                 ;[gov.usgs.eros/lcmap-event "0.5.0-SNAPSHOT"]
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
                 ;; Data types, encoding, validation, etc.
                 [prismatic/schema "1.1.0"]
                 [byte-streams "0.2.0"]
                 [clj-time "0.11.0"]
                 [commons-codec "1.9"]
                 ;; Geospatial libraries
                 [element84/clj-gdal "0.3.2"]
                 ;; Metrics
                 [metrics-clojure "2.6.1"]
                 [metrics-clojure-jvm "2.6.1"]
                 [metrics-clojure-ring "2.6.1"]
                 ;; Dev and project metadata
                 [leiningen-core "2.5.3"]]
  :plugins [[lein-ring "0.9.7"]
            [lein-pprint "1.1.1"]
            [lein-codox "0.9.1"]
            [lein-simpleton "1.3.0"]]
  :source-paths ["src" "test/support/auth-server/src"]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.3"]]
  :jvm-opts ["-Dco.paralleluniverse.fibers.detectRunawayFibers=false"]
  :repl-options {:init-ns lcmap.rest.dev}
  :main lcmap.rest.app
  :target-path "target/%s"
  :codox {:project {:name "LCMAP REST Server"
                    :description "The REST Service for the USGS Land Change Monitoring Assessment and Projection (LCMAP) Computation and Analysis Platform"}
          :namespaces [#"^lcmap.rest\."]
          :output-path "docs/master/current"
          :doc-paths ["docs/source"]
          :metadata {:doc/format :markdown
                     :doc "Documentation forthcoming"}}
  ;; List the namespaces whose log levels we want to control; note that if we
  ;; add more dependencies that are chatty in the logs, we'll want to add them
  ;; here.
  :logging-namespaces [lcmap.rest
                       lcmap.see
                       lcmap.client
                       lcmap.data
                       com.datastax.driver
                       co.paralleluniverse]
  :profiles {
    ;; configuration for dev environment -- if you need to make local changes,
    ;; copy `:env { ... }` into `{:user ...}` in your ~/.lein/profiles.clj and
    ;; then override values there
    :dev {
      :dependencies [[org.clojure/tools.namespace "0.3.0-alpha3"]
                     [slamhound "1.5.5"]]
      :aliases {"slamhound" ["run" "-m" "slam.hound"]}
      :source-paths ["dev-resources/src"]
      :plugins [[lein-kibit "0.1.2"]]
      :env
        {:active-profile "development"
         :db {:hosts ["127.0.0.1"]
              :port 9042
              :protocol-version 3
              :keyspace "lcmap"
              :credentials {
              :username nil
              :password nil}}
         :messaging {:host "127.0.0.1"
                     :port 5672
                     :vhost "/"
                     :default-exchange-name "lcmap.event"
                     :default-queue-name "lcmap.event-stream"}
         :http {:port 1077     ; port number obtained via this bit of geekery:
                 :ip "0.0.0.0"} ;   (reduce + (map int "USGS-EROS LCMAP"))
         :auth {
           :backend :usgs
           ;:backend :nasa
           :usgs {:endpoint "http://127.0.0.1:8888/api"
                   ;:endpoint "https://ers.cr.usgs.gov/api"
                  :login-resource "/auth"
                  :user-resource "/me"}}
         :log-level :debug}}
    ;; configuration for testing environment
    :testing {
      :env
        {:active-profile "testing"
         :db {}
         :http {}
         :log-level :debug}}
    ;; configuration for staging environment
    :staging {
      :env
        {:active-profile "staging"
         :db {}
         :http {}
         :log-level :warn}}
    ;; configuration for production environment
    :prod {
      :env
        {:active-profile "production"
         :db {}
         :http {}
         :log-level :error}}})
