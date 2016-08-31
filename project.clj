(def centos-lib-paths
  ["/usr/java/packages/lib/amd64"
   "/usr/lib64"
   "/lib64"
   "/lib"
   "/usr/lib"])

(def ubuntu-lib-paths
  ["/usr/java/packages/lib/amd64"
   "/usr/lib/x86_64-linux-gnu/jni"
   "/lib/x86_64-linux-gnu"
   "/usr/lib/x86_64-linux-gnu"
   "/usr/lib/jni"
   "/lib:/usr/lib"])

(def gdal-paths
  ["/usr/lib/java/gdal"])

(defn get-lib-path []
  (->> gdal-paths
       (into centos-lib-paths)
       (into ubuntu-lib-paths)
       (clojure.string/join ":")
       (str "-Djava.library.path=")))

(defproject gov.usgs.eros/lcmap-rest "1.0.0-SNAPSHOT"
  :parent-project {
    :coords [gov.usgs.eros/lcmap-system "1.0.0-SNAPSHOT"]
    :inherit [
      :deploy-repositories
      :license
      :managed-dependencies
      :plugins
      :pom-addition
      :repositories
      :target-path
      ;; XXX The following can be un-commented once this issue is resolved:
      ;;     * https://github.com/achin/lein-parent/issues/3
      ;; [:profiles [:uberjar :dev]]
      ]}
  :description "LCMAP REST Service API"
  :url "https://github.com/USGS-EROS/lcmap-rest"
  :dependencies [[org.clojure/clojure]
                 [org.clojure/core.match]
                 [org.clojure/data.codec]
                 [org.clojure/data.json]
                 [org.clojure/data.xml]
                 [org.clojure/core.memoize]
                 ;; Componentization
                 [com.stuartsierra/component]
                 ;; Logging and Error Handling -- note that we need to explicitly pull
                 ;; in a version of slf4j so that we don't get conflict messages on the
                 ;; console
                 [ring.middleware.logger]
                 [dire]
                 [slingshot]
                 ;; REST
                 [compojure]
                 [http-kit]
                 [ring/ring-jetty-adapter]
                 [ring/ring-core]
                 [ring/ring-devel]
                 [ring/ring-json]
                 [ring/ring-defaults]
                 [jmorton/ring-accept]
                 [clojusc/ring-xml]
                 ;; Authentication and authorization
                 [com.cemerick/friend]
                 ;; Job Tracker
                 [org.clojure/core.cache]
                 [co.paralleluniverse/pulsar]
                 [org.clojars.hozumi/clj-commons-exec]
                 [digest]
                 ;; DB
                 [clojurewerkz/cassaforte]
                 [net.jpountz.lz4/lz4]
                 [org.xerial.snappy/snappy-java]
                 ;; LCMAP Components - note that the projects in ./checkouts
                 ;; override these:
                 [gov.usgs.eros/lcmap-config]
                 [gov.usgs.eros/lcmap-client-clj]
                 [gov.usgs.eros/lcmap-logger]
                 [gov.usgs.eros/lcmap-event]
                 [gov.usgs.eros/lcmap-see]
                 [gov.usgs.eros/lcmap-data]
                 ;; XXX note that we may still need to explicitly include the
                 ;; Apache Java HTTP client, since the version used by the LCMAP
                 ;; client is more recent than that used by Chas Emerick's
                 ;; 'friend' library (the conflict causes a compile error which
                 ;; is worked around by explicitly including Apache Java HTTP
                 ;; client library).
                 ;; XXX temp dependencies:
                 [org.apache.httpcomponents/httpclient]
                 [clojure-ini]
                 [clj-http]
                 ;; Data types, encoding, validation, etc.
                 [prismatic/schema]
                 [byte-streams]
                 [clj-time]
                 [commons-codec]
                 ;; Geospatial libraries
                 [clj-gdal]
                 ;; Metrics
                 [metrics-clojure]
                 [metrics-clojure-jvm]
                 [metrics-clojure-ring]
                 ;; Dev and project metadata
                 [leiningen-core]]
  :plugins [[lein-parent "0.3.0"]]
  :source-paths ["src" "test/support/auth-server/src"]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.6"]]
  :jvm-opts ["-Dco.paralleluniverse.fibers.detectRunawayFibers=false"]
  :repl-options {:init-ns lcmap.rest.dev}
  :main lcmap.rest.app
  :codox {:project {:name "lcmap.rest"
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
    :uberjar {:aot :all}
    ;; configuration for dev environment -- if you need to make local changes,
    ;; copy `:env { ... }` into `{:user ...}` in your ~/.lein/profiles.clj and
    ;; then override values there
    :dev {
      ;; XXX 0.3.0-alpha3 breaks reload
      :jvm-opts [~(get-lib-path)]
      :aliases {"slamhound" ["run" "-m" "slam.hound"]}
      :source-paths ["dev-resources/src"]
      :env
        {:active-profile "development"
         :log-level :debug}}
    ;; configuration for testing environment
    :testing {
      :env
        {:active-profile "testing"
         :db {}
         :http {}
         :log-level :info}}
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
