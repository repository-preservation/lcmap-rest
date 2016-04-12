(ns lcmap.rest.api.system
  (:import [java.lang Runtime])
  (:require [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [lcmap.client.status-codes :as status]
            [lcmap.client.system]
            [lcmap.rest.serializer :as serializer]
            [lcmap.rest.middleware.http-util :as http]))

;;; Supporting Protocols ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol ClojureRuntime
  (get-procs [this]
    "Returns the number of processors available to the Java virtual machine.")
  (free-mem [this]
    "Returns the amount of free memory in the Java Virtual Machine.")
  (max-mem [this]
    "Returns the maximum amount of memory that the Java virtual machine will
    attempt to use.")
  (total-mem [this]
    "Returns the total amount of memory in the Java virtual machine."))

(def runtime-behaviour
  {:get-procs (fn [this] (.availableProcessors this))
   :free-mem (fn [this] (.freeMemory this))
   :max-mem (fn [this] (.maxMemory this))
   :total-mem (fn [this] (.totalMemory this))})

(extend Runtime ClojureRuntime runtime-behaviour)

(defn get-runtime
  "Returns the runtime object associated with the current Java application."
  []
  (Runtime/getRuntime))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (http/response "system resources tbd"))

(defn get-sexp-status
  "Get the Tomcat-compatible status info as s-expressions."
  []
  (let [rt (get-runtime)]
    [:status
      [:jvm
        [:memory {:free (str (free-mem rt))
                  :total (str (total-mem rt))
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
          [:workers]]]]))

(defn get-edn-status
  "Get the Tomcat-compatible status info as s-expressions."
  []
  (let [rt (get-runtime)]
    {:status
      {:jvm
        {:memory {:free (str (free-mem rt))
                  :total (str (total-mem rt))
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
           :workers nil}}}}))

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
    ;; Note that lcmap.rest.middleware sets up a route for /api/system/metrics
    ;; XXX add more management resources
    (GET "/status" []
      (get-xml-status))
    ;; XXX json-status is a hack until we get dynamic content type support
    (GET "/json-status" []
      (get-json-status))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
