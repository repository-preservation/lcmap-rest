(ns lcmap.rest.middleware
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojusc.ring.xml :as ring-xml]
            [lcmap.rest.api.routes :as routes]
            [lcmap.rest.middleware.http :as http]))

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
          {version :version} (http/parse-accept-version accept)
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
          sexp (http/response->sexp response :root :xml)]
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
  "Given a content-type (technically, the sub-type of the mime-type), return a
  suitable Ring handler for that type.

  Supported content-type values are:

  * json
  * xml
  * raw

  If any other (i.e., unsupported) content-type values are provided, the default
  content-type handler will be returned.

  LCMAP REST API functions return calls to lcmap.rest.http/response, which provides
  the data structure for both results and errors. Without modification, these are
  of the form:

    [:status ...]
    [:headers {}]
    [:body {:result ... :errors [...]}]

  The JSON handler converts it to data of the form:

    {\"status\": ...,
     \"headers\": {...},
     \"body\": {\"result\": \"...\",
                \"errors\":[...]}}

  The XML handler extracts the :body and converts it to data of the form:

    <?xml version=\"1.0\" encoding=\"UTF-8\"?>
    <xml>
      <status>...</status>
      <headers>...</headers>
      <body>
        <result>...</result>
        <errors>...</errors>
      </body>
    </xml>"
  ([content-type]
    (get-content-type-wrapper content-type #'json-handler))
  ([content-type default-hanlder]
    (case (string/lower-case content-type)
      "json" #'json-handler
      "xml" #'xml-handler
      "raw" #'identity-handler
      default-hanlder)))

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
          {content-type :content-type} (http/parse-accept-version accept)
          wrapper-fn (get-content-type-wrapper content-type)
          wrapper (wrapper-fn handler)]
      (log/debugf "Parsed content type '%s' got %s handler" content-type wrapper-fn)
      (wrapper request))))

(defn lcmap-handlers
  "This function provides the LCMAP REST server with the single means by which
  the application pulls in all LCMAP Ring handers. Any new handlers that are
  created should be chained here and not in ``lcmap.rest.app``."
  [routes]
  (-> routes
      (versioned-route-handler)
      (content-type-handler)))
