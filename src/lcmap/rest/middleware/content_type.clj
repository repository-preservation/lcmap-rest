q(ns lcmap.rest.middleware.content-type
  (:require [clojure.tools.logging :as log]
            [lcmap.rest.middleware.http-util :as http]
            [ring.util.accept :refer [defaccept]]))

;; XXX media-range pattern-like matching could reduce
;; the boiler plate like nature of this macro. This
;; is relatively complicated... and isn't too cumbersome
;; for now.

(defaccept respond-to
  ;; default responses
  "*/*"                                  http/to-json
  "application/*"                        http/to-json
  "application/vnd.usgs.lcmap"           http/to-json
  "application/vnd.usgs.lcmap+json"      http/to-json
  "application/vnd.usgs.lcmap+xml"       http/to-xml
  ;; v0.5 responses
  "application/vnd.usgs.lcmap.v0.5"      http/to-json
  "application/vnd.usgs.lcmap.v0.5+json" http/to-json
  "application/vnd.usgs.lcmap.v0.5+xml"  http/to-xml
    ;; v0.1 responses
  "application/vnd.usgs.lcmap.v0.1"      http/to-json
  "application/vnd.usgs.lcmap.v0.1+json" http/to-json
  "application/vnd.usgs.lcmap.v0.1+xml"  http/to-xml)

(defn handler
  "This custom Ring handler responds with the best representation
  for a given request Accept header.

  By convention, a response header content-type implies a response
  body that has already been converted to that representation. An
  unset response content-type header implies the body has not
  been transformed."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (if (empty? (get-in response [:headers "Content-Type"]))
        (respond-to request response)
        response))))
