(ns lcmap-rest.util
  (:require [clojure.core.memoize :as memo]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [digest]
            [leiningen.core.project :as lein-prj])
  (:import [java.security.MessageDigest]
           [java.math.BigInteger]))

(def accept-regex (re-pattern #"([^;]+)\s*(?:;q=([0-9+\.]+))?\s*(;.+)*"))

(defn parse-accept [string]
  "Parse a single accept string into a map"
  ;; according to RFC2616, the "q" parameter must precede the accept-extension
  (let [[_ mr q ae] (re-find accept-regex string)]
    {:media-range mr
     :quality  (java.lang.Double. (or q "1"))
     :accept-extension ae}))

(def accept-version-regex (re-pattern #"application/vnd\.(usgs\.lcmap.)(v(\d(\.\d)?))\+([^;]+)"))

(defn parse-accept-version [string]
  ;;(log/info (str "Parsing: " string))
  (let [string (or string "")
        [_ vend str-vers vers _ ct] (re-find accept-version-regex string)]
    {:vendor (or vend "NoVendor")
     :string-version (or str-vers "v0.0")
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

(def -get-config
  (memo/lu
    (fn []
      (log/debug "Memoizing LCMAP configuration ...")
      (lein-prj/read))))

(defn get-config
  ([]
    (-get-config))
  ([arg]
    (if (not (= arg :force-reload))
      (-get-config)
      (do (memo/memo-clear! -get-config)
          (-get-config)))))

(defn add-shutdown-handler [func]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. func)))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))
