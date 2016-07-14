(ns lcmap.rest.problem
  "Provide conversion of Problematic types into a Problem record.
  A conversion for RuntimeExceptions is provided."
  (:import [com.datastax.driver.core.exceptions DriverException]))

(defrecord Problem [type title status detail instance])

(defprotocol Problematic (->problem [e]))

(extend-type Problem
  Problematic
  (->problem [p] p))

(extend-type RuntimeException
  Problematic
  (->problem [e]
    (map->Problem {:type     (str (type e))
                   :title    (.getMessage e)
                   :detail   (.getMessage e)
                   :instance (str (java.util.UUID/randomUUID))
                   :status   500})))
