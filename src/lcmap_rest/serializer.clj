(ns lcmap-rest.serializer
  (:require [clojure.data.json :as json]
            [clj-time.coerce :as time])
  (:import [java.io StringWriter StringReader]))

;;; Serialize date
(defn- serialize-java-util-date [x #^StringWriter out]
    (json/json-str (time/to-string x)))

(extend java.util.Date json/JSONWriter
    {:-write serialize-java-util-date})
