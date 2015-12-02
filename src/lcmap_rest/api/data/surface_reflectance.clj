(ns lcmap-rest.api.data.surface-reflectance
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :refer :all]
            [ring.util.response :refer [response]]
            [lcmap-client.data.surface-reflectance]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  (response
   {:links
    (map (fn [x]
           (str context x))
         ["tiles" "rod"])}))

(defn get-tiles [point extent time band request]
  (str "point: " point ", extent: " extent ", time: " time
       ", band: " band))

(defn get-rod [point time band request]
  (str "point: " point ", time: " time ", band: " band
       ", user agent: " request))

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap-client.data.surface-reflectance/context []
    (GET "/" request
      (get-resources (:uri request)))
    (GET "/tiles" [point extent time band :as request]
      (get-tiles point extent time band request))
    (GET "/rod" [point time band :as request]
      (get-rod point time band request))))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
