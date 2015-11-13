(ns lcmap-rest.auth
  (:require [clojure.tools.logging :as log]
            [ring.util.response :as ring]
            [lcmap-rest.status-codes :as status]))

(defn save-oauth-code [code]
  (log/debugf "Saving key '%s' ..." code)
  (ring/status
    (ring/response "OAuth2 code save logic TBD ...")
    status/ok))
