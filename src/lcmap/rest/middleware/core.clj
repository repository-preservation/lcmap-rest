(ns lcmap.rest.middleware.core)

(defn identity-handler
  "A no-op Ring handler, more or less: it updates the response body in the same
  manner as the other LCMAP Ring handlers."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body response))))

