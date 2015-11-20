(ns lcmap-rest.exceptions
  (:require [slingshot.slingshot :refer [throw+]]))

(defn error [type msg]
  (throw+ type "%s: %s" % msg))

(defn lcmap-error [msg]
  (error 'LCMAP-Error msg))

(defn auth-error [msg]
  (error 'Auth-Error msg))
