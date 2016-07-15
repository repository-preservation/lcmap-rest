(ns lcmap.rest.middleware.versioned-api
  (:require [lcmap.rest.api.routes :as routes]
            [lcmap.rest.middleware.http-util :as http]))

(defn get-versioned-routes
  "This is a utility function for extracting the route version from the request
  and then getting a supported route that matches the requested version."
  [request default-version]
  (-> request
      (:accept)
      (first)
      (:version)
      (routes/get-versioned-routes default-version)))

(defn handler
  "This is a custom Ring handler for extracting the API version from the Accept
  header and then selecting the versioned API route accordingly."
  [default-version]
  (fn [request]
    (-> (get-versioned-routes request default-version)
        (apply [request]))))
