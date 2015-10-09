(ns lcmap-rest.management
  (:import [java.lang Runtime])
  (:require [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]))

(defn get-status []
  (xml/emit-str
   (xml/sexp-as-element
    [:status
     [:jvm
      [:memory {:free (str (. (Runtime/getRuntime) freeMemory))
                :total (str (. (Runtime/getRuntime) totalMemory))
                :max "0"}]
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
       [:workers]]]])))
