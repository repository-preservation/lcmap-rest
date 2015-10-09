(defproject gov.usgs/lcmap-rest "0.1.0-dev"
  :description "LCMAP REST Service API"
  :url "https://github.com/USGS-EROS/lcmap-rest"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.17"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [com.cemerick/friend "0.2.1"]
                 [clojurewerkz/cassaforte "2.0.0"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 ;;[gov.usgs/lcmap-client-clj "0.1.0-dev"]
                 ;; XXX once lcmap-client-clj is released and is not longer
                 ;; being used from the local checkouts directory, we will
                 ;; uncomment the dependancy above and remove the dependancies
                 ;; below.
                 ;; XXX note that we may still need to explicitly include the
                 ;; Apache Java HTTP client, since the version used by the LCMAP
                 ;; client is more recent than that used by Chas Emerick's
                 ;; 'friend' library (the conflict causes a compile error which
                 ;; is worked around by explicitly including Apache Java HTTP
                 ;; client library).
                 [org.apache.httpcomponents/httpclient "4.5"]
                 [clj-http "2.0.0"]
                 [leiningen-core "2.5.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :main lcmap-rest.main)
