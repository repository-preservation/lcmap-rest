(ns lcmap-rest.api.system
  (:import [java.lang Runtime])
  (:require [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [dire.core :refer [with-handler!]]
            [ring.util.response :as ring]
            [lcmap-client.system]
            [lcmap-rest.status-codes :as status]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [request]
  (ring/status
    (ring/response "system resources tbd")
    status/ok))

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

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX this needs to be protected; see this ticket:
;;   https://my.usgs.gov/jira/browse/LCMAP-71
(defroutes routes
  (context lcmap-client.system/context []
    (GET "/" request
      (get-resources (:uri request)))
    ;; XXX add more management resources
    (GET "/status" []
      (get-status))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
