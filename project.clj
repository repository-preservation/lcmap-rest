(defproject gov.usgs/lcmap-rest "0.1.0-SNAPSHOT"
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
                 [ch.qos.logback/logback-classic "1.1.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :main lcmap-rest.main)
