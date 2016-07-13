(defproject auth-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [clojusc/twig "0.2.1"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.17"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring.middleware.logger "0.5.0" :exclusions [org.slf4j/slf4j-log4j12]]
                 [org.apache.httpcomponents/httpclient "4.5"]
                 [com.cemerick/friend "0.2.1"]
                 [leiningen-core "2.5.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :main ^:skip-aot auth-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {
              :env {:ip "0.0.0.0"
                    :port 9999}}})
