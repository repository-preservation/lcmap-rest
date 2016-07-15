(ns lcmap.rest.middleware.http-util
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojusc.ring.xml :as ring-xml]
            [ring.util.response :as ring-resp]
            [dire.core :refer [with-handler!]]
            [lcmap.client.http :as http]
            [lcmap.rest.errors :as errors]
            [lcmap.rest.problem :as problem]))

(defn response
  "If the developer is inclined, this function allows one to set the result,
  errors, headers, and status in one call. Otherwise, one may use individual
  functions for each (see above), if that better suits one's needs."
  [& {:keys [body status content-type headers]
      :or {body nil status 200 content-type nil headers {}}
      :as args}]
  (-> (ring-resp/response body)
      (assoc :headers headers)
      (ring-resp/content-type content-type)
      (ring-resp/status status)))

;; Supplement of `response` -- works better with threading macro
;; when supplied value is a response-like map.
(defn response*
  ""
  [{:keys [body status content-type headers]
    :or {body nil status 200 content-type nil headers {}}
    :as args}]
  (-> (ring-resp/response body)
      (assoc :headers headers)
      (ring-resp/content-type content-type)
      (ring-resp/status status)))

(defn add-error-handler
  "The error handler needs to return a response map with a body that
  is a Problem or a type that extends the Problematic protocol. See
  lcmap.rest.middleware.problem for how these are handled."
  [func ex err-id err-status]
  (with-handler! func ex
    (fn [exception & args]
      ;; XXX ignoring err-id and status for now, just returning
      ;; the exception because it satisfies problematic and will
      ;; be handled by problem middleware.
      exception)))

(defn headers->sexp
  "Convert response headers to S-expressions that can be consumed by
  clojure.data.xml."
  [headers]
  (into [:headers] headers))

(defn errors->sexp
  "Convert the body errors to S-expressions that can be consumed by
  clojure.data.xml."
  [errors]
  [:errors (map #(conj [:error] %) errors)])

(defn body->sexp
  "Convert the response body to S-expressions that can be consumed by
  clojure.data.xml."
  [{result :result errors :errors}]
  [:body [:result result]
         (errors->sexp errors)])

(defn response->sexp
  "Converts the clojure.lang.PersistentArrayMap of a Ring repsonse to
  S-expressions that can be consumed by clojure.data.xml."
  [{status :status headers :headers body :body} & {:keys [root] :or {root :data}}]
  [root [:status status]
        (headers->sexp headers)
        (body->sexp body)])

(defn to-json
  "Convert response body to JSON"
  [response]
  (update response :body json/write-str))

(defn to-xml
  "Convert response body to XML"
  [response]
  (assoc response :body (-> (response :body)
                            (body->sexp :root :xml)
                            (ring-xml/->xml {:sexprs true}))))
