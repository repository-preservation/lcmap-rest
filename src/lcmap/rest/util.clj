(ns lcmap.rest.util
  (:require [clojure.core.memoize :as memo]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [digest]
            [lcmap.client.http :as http])
  (:import [java.security.MessageDigest]
           [java.math.BigInteger]))

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

(defn parse-accept-version [string]
  ;;(log/info (str "Parsing: " string))
  (let [string (or string "")
        [_ vend str-vers vers _ ct] (re-find accept-version-regex string)]
    {:vendor (or vend "NoVendor")
     :string-version (or str-vers "v0.0") ; XXX put the default version somewhere more visible -- project.clj?
     :version  (java.lang.Double. (or vers "1"))
     :content-type (or ct "")}))

(defn serialize [args]
  (cond (list? args)
          (string/join (sort (map #'str args)))
        (map? args)
          (str (into (sorted-map) args))
        :else
          (str args)))

(defn get-args-hash [model-name & args]
  (->> args
       (serialize)
       (str model-name)
       (digest/md5)))

(defn add-shutdown-handler [func]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. func)))

(defn in?
  "This function returns true if the provided seqenuce contains the given
  elment."
  [sequence elm]
  (some #(= elm %) sequence))

(defn response [& {:keys [result errors status]
                   :or {result nil errors [] status 200}
                   :as args}]
  (-> (http/response :result result :errors errors)
      (ring/status status)))

(defn make-bool
  ""
  [input]
  (case input
    0 false
    "0" false
    false false
    "false" false
    :false false
    nil false
    "nil" false
    :nil false
    true))

(defn make-flag
  "There are three cases we want to handle for command line options:
  * a flag that takes a value
  * a flag which should be passed, since a value was given
  * a flag which should not be passed, since no value was given"
  [flag value & {:keys [unary?] :or {unary? false}}]
  (cond
    unary? (if (make-bool value)
             flag
             nil)
    (nil? value) nil
    :else (format "%s %s" flag value)))

(defn get-local-ip
  "Get the IP address of the local machine."
  []
  (.getHostAddress (java.net.InetAddress/getLocalHost)))

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
  (log/debug "Result:" result)
  [:body [:result result]
         (errors->sexp errors)])

(defn response->sexp
  "Converts the clojure.lang.PersistentArrayMap of a Ring repsonse to
  S-expressions that can be consumed by clojure.data.xml."
  [{status :status headers :headers body :body} & {:keys [root] :or {root :data}}]
  (log/debug "Body:" body)
  (log/debug "Body type:" (type body))
  (log/debug "Body result:" (:result body))
  [root [:status status]
        (headers->sexp headers)
        (body->sexp body)])

