(ns lcmap.rest.middleware.http-util
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [lcmap.client.http :as http]))

(def accept-regex
  (re-pattern #"([^;]+)\s*(?:;q=([0-9+\.]+))?\s*(;.+)*"))
(def accept-version-regex
  (re-pattern #"application/vnd\.(usgs\.lcmap.)(v(\d(\.\d)?))\+([^;]+)"))

(defn parse-accept [string]
  "Parse a single accept string into a map"
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

(defn problem-header
  "Create a problem header given a mime sub-type (default :json).

  This is per IETF RFC-7807."
  [& {:keys [mime] :or {mime :json} :as args}]
    (format "Content-Type: application/problem+%s" mime))

(defn response [& {:keys [result errors status headers]
                   :or {result nil errors [] status 200 headers []}
                   :as args}]
  ;; XXX how much of this should go in lcmap.client.http?
  (-> (ring/response)
      (update-in [:headers] #(into headers %))
      (assoc :body (http/response :result result :errors errors))
      (ring/status status)))

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
