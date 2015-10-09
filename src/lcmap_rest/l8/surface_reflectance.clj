(ns lcmap-rest.l8.surface-reflectance
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :refer :all]
            [ring.util.response :refer [response]]
            [lcmap-client.l8.surface-reflectance :as sr]))
  
(defn get-resources [context]
  (response
   {:links
    (map (fn [x] (str sr/context x)) [:tiles :rod])}))

(defn get-tiles [point extent time band request]
  (str "point: " point ", extent: " extent ", time: " time
       ", band: " band))

(defn get-rod [point time band request]
  (str "point: " point ", time: " time ", band: " band
       "user agent: " request))
