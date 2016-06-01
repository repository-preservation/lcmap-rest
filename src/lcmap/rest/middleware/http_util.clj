(ns lcmap.rest.middleware.http-util
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [dire.core :refer [with-handler!]]
            [lcmap.client.http :as http]
            [lcmap.rest.errors :as errors]))

(def accept-regex
  (re-pattern #"([^;]+)\s*(?:;q=([0-9+\.]+))?\s*(;.+)*"))
(def accept-version-regex
  (re-pattern #"application/vnd\.(usgs\.lcmap.)(v(\d(\.\d)?))\+([^;]+)"))

(defn parse-accept
  "Parse a single accept string into a map"
  [string]
  ;; according to RFC2616, the "q" parameter must precede the accept-extension
  (let [[_ mr q ae] (re-find accept-regex string)]
    {:media-range mr
     :quality  (java.lang.Double. (or q "1"))
     :accept-extension ae}))

(defn parse-accept-version [default-version string]
  (let [string (or string "")
        [_ vend str-vers vers _ ct] (re-find accept-version-regex string)]
    (log/tracef "Parsed API version %s with content type '%s'" str-vers ct)
    {:vendor (or vend "NoVendor")
     :version (or str-vers default-version)
     :content-type (or ct "")}))

(defn get-accept
  "Extract the parsed 'Accept' headers from a request."
  [request default-version]
  (-> (:headers request)
      (get "accept")
      (#(parse-accept-version default-version %))))

(defn add-problem-header
  "Returns an updated Ring response with the HTTP problem header set for the
  given mime sub-type.

  This is per IETF RFC-7807."
  [resp & {:keys [mime] :or {mime :json}}]
  (ring/content-type resp (str "application/problem+" (name mime))))

(defn add-result
  "Update the response with the given result."
  [resp result]
  (-> resp
      (assoc-in [:body :result] result)))

(defn append-error
  ""
  [resp error]
  (-> resp
      (get-in [:body :errors])
      (or [])
      (conj error)))

(defn add-error
  "Update the response with the given errors."
  [resp error]
  (-> resp
      (assoc-in [:body :errors] (append-error resp error))))

(defn add-headers
  "Update the response with the given result."
  [resp headers]
  (-> resp
      (assoc :headers headers)))

(defn add-status
  "Update the response with the given status."
  [resp status]
  (-> resp
      (ring/status status)))

(defn response
  "If the developer is inclinded, this function allows one to set the result,
  errors, headers, and status in one call. Otherwise, one may use individual
  functions for each (see above), if that better suits one's needs."
  [& {:keys [result errors status headers]
      :or {result nil errors [] status 200 headers {}}
      :as args}]
  ;; XXX how much of this should go in lcmap.client.http?
  (-> (http/response :result result :errors errors)
      (ring/response)
      (add-headers headers)
      (ring/status status)))

(defn add-error-handler
  ""
  [func ex err-id err-status]
  (with-handler!
    func
    ex
    (fn [e & args]
      (-> (response)
          (add-error (errors/process-error e err-id))
          (add-status err-status)
          (add-problem-header)))))

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
