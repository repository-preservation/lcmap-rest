(ns lcmap.rest.middleware
  (:require [ring.middleware.accept :as ring-accept]
            [metrics.ring.expose :as ring-metrics]
            [metrics.ring.instrument :as ring-instrument]
            [lcmap.client.system]
            [lcmap.rest.middleware.content-type :as content-type]
            [lcmap.rest.middleware.problem :as problem]
            [lcmap.rest.middleware.versioned-api :as versioned-api]))

(defn lcmap-handlers
  "This function provides the LCMAP REST server with the single means by which
  the application pulls in all LCMAP Ring handers. Any new handlers that are
  created should be chained here and not in ``lcmap.rest.app``."
  [default-version]
  (-> (versioned-api/handler default-version)
      ;; turn problematic response body into acceptable representation
      (problem/handler)
      ;; turn unprocessed response body into acceptable default representation
      (content-type/handler)
      (ring-metrics/expose-metrics-as-json
        (str lcmap.client.system/metrics "/all"))
      (ring-instrument/instrument)))
