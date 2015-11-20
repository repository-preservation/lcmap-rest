(ns lcmap-rest.status-codes)

;;; Status code aliases

;; 2xx
(def ok 200)
(def pending 202)
;; 3xx
(def pending-link 307)
(def permanant-link 308)
;; 4xx
(def unauthorized 401)
(def forbidden 403)
(def no-resource 404)
(def auth-timeout 419)
(def token-invalid 498)
(def token-required 499)

;; 5xx
(def server-error 500)

;;; Status code predicates

;; 2xx
(defn ok? [status] (= status ok))
(defn pending? [status] (= status pending))

;; 3xx
(defn pending-link? [status] (= status pending-link))
(defn permanant-link? [status] (= status permanant-link))

;; 4xx
(defn unauthorized?[status] (= status unauthorized))
(defn forbidden?[status] (= status forbidden))
(defn forbidden?[status] (= status forbidden))
(defn no-resource? [status] (= status no-resource))
(defn auth-timeout? [status] (= status auth-timeout))
(defn token-invalid? [status] (= status token-invalid))
(defn token-required? [status] (= status token-required))

;; 5xx
(defn server-error? [status] (= status server-error))
