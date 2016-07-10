(ns lcmap.rest.middleware.problem
  ""
  (:require [ring.util.response :as rr]
            [lcmap.rest.problem :refer :all]))

(defn build-problem
  "Convert problematic values into a Problem record."
  [response]
  (if (satisfies? Problematic (response :body))
    (update response :body ->problem)
    response))

(defn set-status
  "Set HTTP status header to Problem's status value."
  [response]
  (if-let [status (get-in response [:body :status])]
    (rr/status response status)
    response))

;; XXX Determine if this is something that should be handled
;;     by other middleware.
;; XXX Provide XML response if requested.
(defn set-content-type
  "Set response content-type to match preferred media-type of request."
  [request response]
  (rr/content-type response "application/problem+json"))

(defn handler
  "Build problem and update HTTP headers."
  [handler]
  (fn [request]
    (->> (handler request)
         (build-problem)
         (set-content-type request)
         (set-status))))
