(ns lcmap.rest.middleware.content-type
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojusc.ring.xml :as ring-xml]
            [lcmap.rest.middleware.core :as core]
            [lcmap.rest.middleware.http-util :as http]))

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

(defn lookup-content-type-handler
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
    (lookup-content-type-handler content-type #'json-handler))
  ([content-type default-type-hanlder]
    (log/tracef "Looking up handler for content type '%s' ..." content-type)
    (case (string/lower-case content-type)
      "json" #'json-handler
      "xml" #'xml-handler
      "raw" #'core/identity-handler
      default-type-hanlder)))

(defn get-content-type-wrapper
  "This is a utility function for extracting the route version from the request
  and then getting a supported route that matches the requested version.

  This is a custom Ring handler for extracting the content-type from the Accept
  header and then selecting the appropriate response wrapper."
  [request default-version]
  (-> request
      (http/get-accept default-version)
      (:content-type)
      (lookup-content-type-handler)))

(defn handler
  "This is a custom Ring handler for extracting the content-type from the Accept
  header and then selecting the appropriate response wrapper."
  [handler default-version]
  (fn [request]
    (-> request
        (get-content-type-wrapper default-version)
        (apply [handler])
        (apply [request]))))
