(ns lcmap.rest.middleware.problem
  ""
  (:require [clojure.tools.logging :as log]
            [ring.util.accept :refer [defaccept]]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.problem :refer :all]))

(defaccept respond-to
  "application/problem+json" http/to-json
  "application/problem+xml"  http/to-xml)

(defn handle-problem
  "Convert problematic body into acceptable representation."
  [request response]
  (if (satisfies? Problematic (response :body))
    (let [as-problem (update response :body ->problem)
          as-content (respond-to request as-problem)
          status-code (get-in response [:body :status] 500)]
      (assoc as-content :status status-code))
    response))

(defn handler
  "Build problem and update HTTP headers."
  [handler]
  (fn [request]
    (->> (handler request)
         (handle-problem request))))
