(ns lcmap-rest.status-codes)

(def ok 200)
(def pending 202)
(def pending-link 307)
(def permanant-link 308)
(def no-resource 404)

(defn ok? [status] (= status ok))
(defn pending? [status] (= status pending))
(defn pending-link? [status] (= status pending-link))
(defn permanant-link? [status] (= status permanant-link))
(defn no-resource? [status] (= status no-resource))
