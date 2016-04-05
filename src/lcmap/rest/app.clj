(ns lcmap.rest.app
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [org.httpkit.server :as httpkit]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.logger :as ring-logger]
            [compojure.core :refer [defroutes]]
            [compojure.response :as response]
            [clojusc.ring.xml :as ring-xml]
            [com.stuartsierra.component :as component]
            [twig.core :as logger]
            [lcmap.rest.api.routes :as routes]
            [lcmap.rest.components :as components]
            [lcmap.rest.util :as util])
  (:gen-class))

(def default-versioned-route #'routes/v0.5)

(defn get-versioned-routes [version default]
  (cond
    (= version 0.0) #'routes/v0
    (= version 0.5) #'routes/v0.5
    (and (>= version 1.0) (< version 2.0)) #'routes/v1
    (and (>= version 2.0) (< version 3.0)) #'routes/v2
    :else default))

(defn versioned-route-handler
  "This is a custom Ring handler for extracting the API version from the Accept
  header and then selecting the versioned API route accordingly."
  [default-api]
  (fn [request]
    (let [headers (:headers request)
          ;; This next line is nuts and took a while to figure out -- results
          ;; are rendered in log files as symbols, but (headers 'accept) and
          ;; ('accept headers) didn't work. After an inordinate amount of trial
          ;; and error, it was discovered that the header keys are actually in
          ;; lower-case strings at this point in the middleware chain, despite
          ;; what *looked* like was getting logged
          accept (headers "accept")
          {version :version} (util/parse-accept-version accept)
          routes (get-versioned-routes version default-api)]
      ;; (log/tracef "Headers: %s" headers)
      ;; (log/tracef "Accept: %s" accept)
      (log/debugf "Processing request for version %s of the API ..." version)
      (log/debugf "Using API routes %s ..." routes)
      (routes request))))

(defn json-handler
  "A Ring handler that converts the entire response to JSON and then updates
  the response body with that JSON."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (json/write-str response)))))

(defn sexpr-handler
  "A Ring handler that converts a response to S-expressions and sets the body
  of the response to be that collection of S-expressions."
  [handler]
  (fn [request]
    (let [response (handler request)
          sexp (util/response->sexp response :root :xml)]
      (assoc response :body sexp))))

(defn xml-handler
  "A Ring handler that converts a response (clojure.lang.PersistentArrayMap)
  to XML by way of S-expressions."
  [handler]
  (-> handler
      (sexpr-handler)
      (ring-xml/wrap-xml-response {:sexprs true})))

(defn identity-handler
  "A no-op Ring handler, more or less: it updates the response body in the same
  manner as the other LCMAP Ring handlers."
  [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body response))))

(defn get-content-type-wrapper
  "Given a content-type (mime-type), return a suitable Ring handler for that type.

  LCMAP REST API functions return calls to lcmap.rest.util/response, which provides
  the data structure for both results and errors. Without modification, these are
  of the form:

    [:status 200]
    [:headers {}]
    [:body {:result ... :errors [...]}]

  The JSON handler converts it to data of the form:

    {...}

  The XML handler extracts the :body and converts it to data of the form:

    <...>
  "
  [content-type]
  (case (string/lower-case content-type)
    "json" #'json-handler
    "xml" #'xml-handler
    "raw" #'identity-handler
    ;; default
    #'json-handler))

;; XXX Both the versioned-routes handler and the content-type handler are
;;     performing similar operations ... this is a bit wasteful. This should
;;     be combined into a single meta-handler -- maybe "lcmap-handler" -- that
;;     would actually run all the lcmap-specific Ring handlers

(defn content-type-handler
  "This is a custom Ring handler for extracting the content-type from the Accept
  header and then selecting the appropriate response wrapper."
  [handler]
  (fn [request]
    (let [headers (:headers request)
          accept (headers "accept")
          {content-type :content-type} (util/parse-accept-version accept)
          wrapper-fn (get-content-type-wrapper content-type)
          wrapper (wrapper-fn handler)]
      (log/debugf "Got wrapper-fn %s" wrapper-fn)
      (log/debugf "Got wrapper %s" wrapper)
      (wrapper request))))

(defroutes app
  (-> default-versioned-route
      (versioned-route-handler)
      (content-type-handler)
      ;; XXX once we support SSL, api-defaults needs to be changed to
      ;; secure-api-defaults
      (wrap-defaults api-defaults)
      ;; XXX maybe move this handler into the httpd component setup, that way
      ;; we could enable it conditionally, based upon some configuration value.
      (ring-logger/wrap-with-logger)))

(defn -main
  "This is the entry point. Note, however, that the system components are
  defined in lcmap.rest.components. In particular, lcmap.rest.components.system
  brings together all the defined (and active) components; that is the module
  which is used to bring the system up when (component/start ...) is called.

  'lein run' will use this as well as 'java -jar'."
  [& args]
  ;; Set the initial log-level before the components set the log-levels for
  ;; the configured namespaces
  (logger/set-level! ['lcmap] :info)
  (let [system (components/init #'app)]
    (log/info "LCMAP REST server's local IP address:" (util/get-local-ip))
    (component/start system)
    (util/add-shutdown-handler #(component/stop system))))
