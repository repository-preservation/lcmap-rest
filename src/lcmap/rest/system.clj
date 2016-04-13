(ns lcmap.rest.system
  (:import [java.lang Runtime])
  (:require [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [dire.core :refer [with-handler!]]
            [metrics.core :as metrics]
            [lcmap.rest.serializer :as serializer])
  (:import [com.codahale.metrics MetricRegistry]))

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

(defprotocol Metrics
  (get-metrics-names [this]
    "Returns a set of the names of all the metrics in the default registry.")
  (get-metrics-counters [this]
    "Returns a map of all the counters in the registry and their names.")
  (get-metrics-gauges [this]
    "Returns a map of all the gauges in the registry and their names.")
  (get-metrics-histograms [this]
    "Returns a map of all the histograms in the registry and their names.")
  (get-metrics-meters [this]
    "Returns a map of all the meters in the registry and their names.")
  (get-metrics-timers [this]
    "Returns a map of all the timers in the registry and their names."))

(def metrics-behaviour
  {:get-metrics-names (fn [this] (into [] (.getNames this)))
   :get-metrics-counters (fn [this] (keys (into {} (.getCounters this))))
   :get-metrics-gauges (fn [this] (keys (into {} (.getGauges this))))
   :get-metrics-histograms (fn [this] (keys (into {} (.getHistograms this))))
   :get-metrics-meters (fn [this] (keys (into {} (.getMeters this))))
   :get-metrics-timers (fn [this] (keys (into {} (.getTimers this))))})

(extend MetricRegistry Metrics metrics-behaviour)

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
