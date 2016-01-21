(ns lcmap-rest.serializer
  (:require [clojure.data.json :as json]
            [clj-time.core :as time])
  (:import (java.io PrintWriter
             StringWriter
             StringReader)))

(defn- serialize-java-util-date [x #^StringWriter out]
    (json/json-str (.toString x)))

(extend java.util.Date json/JSONWriter
    {:-write serialize-java-util-date})
