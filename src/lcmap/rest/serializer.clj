(ns lcmap.rest.serializer
  (:require [clojure.data.codec.base64 :as b64]
            [clojure.data.json :as json]
            [clj-time.coerce :as time])
  (:import [java.io StringWriter StringReader]
           [java.nio HeapByteBuffer]))

;;; Serialize date

(defn- encode-java-util-date [x #^StringWriter out]
    (json/json-str {:timestamp (time/to-string x)}))

(extend java.util.Date json/JSONWriter
    {:-write encode-java-util-date})

;;; Serialize heap byte buffer
;;; We don't actually need this if we do blob->varchar on the blob fields
(defn- serialize-java-nio-heapbytebuffer [x #^StringWriter out]
  (let [str-val (String. (.array x))]
    (json/json-str {:heap-byte-buffer str-val})))

(defn- heap-byte-buffer->unicode
  ""
  [x #^StringWriter out]
  (let [str-val (String. (.array x))]
    (json/json-str {:heap-byte-buffer str-val})))

(defn- heap-byte-buffer->base64
  ""
  [x #^StringWriter out]
  (let [str-val (b64/encoding-transfer x out)]))

(extend java.nio.HeapByteBuffer json/JSONWriter
        {:-write serialize-java-nio-heapbytebuffer}
        #_{:-write heap-byte-buffer->base64})

;;; JSON Reader

(defn decode-type [k v]
  (case k
    :timestamp (time/from-string v)
    ;; XXX decoding back to HeapByetBuffer is a tricky prospect ...
    ;; we don't need to do it if all our queries to blob data do a
    ;; cassandra server-sicde cast
    ;;:heap-byte-buffer (...? )
    ))

;;; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
;;; LCMAP serialization API functions
;;; >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

(defn json->edn [json-str]
  (json/read-str
    json-str
    :key-fn keyword
    :value-fn decode-type))

(defn edn->json [edn]
  (json/write-str edn))
