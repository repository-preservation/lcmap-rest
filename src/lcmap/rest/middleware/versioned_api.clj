(ns lcmap.rest.middleware.versioned-api
  (:require [lcmap.rest.api.routes :as routes]
            [lcmap.rest.middleware.http-util :as http]))

(defn get-versioned-routes
  "This is a utility function for extracting the route version from the request
  and then getting a supported route that matches the requested version."
  [request default-version]
  (-> request
      (http/get-accept default-version)
      (:version)
      (routes/get-versioned-routes default-version)))

(defn handler
  "This is a custom Ring handler for extracting the API version from the Accept
  header and then selecting the versioned API route accordingly.

  It is expected that this handler is first in the chain of LCMAP Ring handlers,
  and as such does not take a handler as its first argument. This that ever
  changes, this function will need to be updated."
  [default-version]
  (fn [request]
    (-> request
        (get-versioned-routes default-version)
        (apply [request]))))
