(ns lcmap-rest.util)

(defrecord Accept [media-range quality accept-extension])

(defn accept [str]
  "Parse a single accept string into a map"
  ; according to RFC2616, the "q" parameter must precede the accept-extension
  (let [pattern #"([^;]+)\s*(?:;q=([0-9+\.]+))?\s*(;.+)*"
        matches (re-find pattern str)
        [_ media-range qvalue accept-extension] matches
        quality (java.lang.Double/parseDouble (or qvalue "1"))]
    (Accept. media-range quality accept-extension)))
