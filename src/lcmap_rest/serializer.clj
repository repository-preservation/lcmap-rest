(ns lcmap-rest.serializer
  (:require [clojure.data.json :as json]))

(defn- serialize-java-util-date [x #^JSONWriter out]
    (json-str (.toString x)))

(extend java.util.Date json/JSONWriter
    {:-write serialize-date})
