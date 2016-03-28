(ns lcmap.rest.api.v0.system
  (:import [java.lang Runtime])
  (:require [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap.client.status-codes :as status]
            [lcmap.client.system]
            [lcmap.rest.serializer :as serializer]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources
  [request]
  (ring/status
    (ring/response "system resources tbd")
    status/ok))

(defn get-sexp-status
  "Get the Tomcat-compatible status info as s-expressions."
  []
  [:status
    [:jvm
      [:memory {:free (str (. (Runtime/getRuntime) freeMemory))
                :total (str (. (Runtime/getRuntime) totalMemory))
                :max "0"}]
      ;; XXX the rest of this data structure is placeholder and needs to
      ;;     be filled in
      [:connector {:name "test-rest"}
        [:threadInfo {:maxThreads "0"
                      :minSpareThreads "0"
                      :maxSpareThreads "0"
                      :currentThreadCount "0"
                      :currentThreadsBusy "0"}]
        [:requestInfo {:maxTime "0"
                       :processingTime "0"
                       :requestCount "0"
                       :errorCount "0"
                       :bytesReceived "0"
                       :bytesSent "0"}]
        [:workers]]]])

(defn get-edn-status
  "Get the Tomcat-compatible status info as s-expressions."
  []
  {:status
    {:jvm
      {:memory {:free (str (. (Runtime/getRuntime) freeMemory))
                :total (str (. (Runtime/getRuntime) totalMemory))
                :max "0"}
      ;; XXX the rest of this data structure is placeholder and needs to
      ;;     be filled in
       :connector
        {:name "test-rest"
         :threadInfo {:maxThreads "0"
                      :minSpareThreads "0"
                      :maxSpareThreads "0"
                      :currentThreadCount "0"
                      :currentThreadsBusy "0"}
         :requestInfo {:maxTime "0"
                       :processingTime "0"
                       :requestCount "0"
                       :errorCount "0"
                       :bytesReceived "0"
                       :bytesSent "0"}
         :workers nil}}}})

(defn get-json-status
  "This is the Tomcat-compatible status info as JSON data."
  []
  (-> (get-edn-status)
      (serializer/edn->json)))

(defn get-xml-status
  "This is for use by JMeter and other tools which require status output
  in a Tomcat-compatible manner."
  []
  (-> (get-sexp-status)
      (xml/sexp-as-element)
      (xml/emit-str)))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX this needs to be protected; see this ticket:
;;   https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes routes
  (context lcmap.client.system/context []
    (GET "/" request
      (get-resources (:uri request)))
    ;; XXX add more management resources
    (GET "/status" []
      (get-xml-status))
    ;; XXX json-status is a hack until we get dynamic content type support
    (GET "/json-status" []
      (get-json-status))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
