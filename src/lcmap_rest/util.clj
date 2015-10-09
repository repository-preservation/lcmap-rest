(ns lcmap-rest.util)

(def accept-regex (re-pattern #"([^;]+)\s*(?:;q=([0-9+\.]+))?\s*(;.+)*"))

(defn parse-accept [str]
  "Parse a single accept string into a map"
  ; according to RFC2616, the "q" parameter must precede the accept-extension
  (let [[_ mr q ae] (re-find accept-regex str)]
    {:media-range mr
     :quality  (java.lang.Double. (or q "1"))
     :accept-extension ae}))

(def accept-version-regex (re-pattern #"application/vnd\.(usgs\.lcmap.)(v(\d(\.\d)?))\+([^;]+)"))

(defn parse-accept-version [str]
  (let [[_ vend str-vers vers _ ct] (re-find accept-version-regex str)]
    {:vendor vend
     :string-version str-vers
     :version  (java.lang.Double. (or vers "1"))
     :content-type ct}))
