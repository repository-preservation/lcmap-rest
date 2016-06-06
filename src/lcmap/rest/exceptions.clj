(ns lcmap.rest.exceptions)

(defn error [type msg]
  (ex-info (str type) {:msg msg :type type}))

(defn lcmap-error [msg]
  (error 'LCMAP-Error msg))

(defn auth-error [msg]
  (error 'Auth-Error msg))

(defn type-error [msg]
  (error 'Type-Error msg))
