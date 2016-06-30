(ns lcmap.rest.middleware.content-type
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clojusc.ring.xml :as ring-xml]
            [lcmap.rest.middleware.core :as core]
            [lcmap.rest.middleware.http-util :as http]
            [lcmap.rest.middleware.gdal-content :as gdal-content]))

(defn parse-quality
  "Convert the first 'q' parameter to a number (or 1.0)"
  [extension]
  (let [[_ qvalue] (re-find #";q=([0-9\.]+)" (or extension ""))]
    (java.lang.Double. (or qvalue "1.0"))))

(re-find #"(([\w\*]+)/([\w\.]+\+?([\w\.]*)))(;.*)?" "text/vnd.usgs.v0.5+html;q=1")

(defn +media-range
  ""
  [accept]
  (->> (re-find #"(([\w\*]+)/([\w\*\.]+\+?([\w\.]*)))(;.*)?" accept)
       (zipmap [:original :media-range :type :subtype :suffix :parameters])))

(defn +quality
  "Takes first 'q' parameter with a "
  [accept]
  (->> (if-let [[_ q] (re-find #"q=([10]?\.?[0-9]+)" (:original accept))]
         (java.lang.Double. q)
         1.0)
       (assoc accept :quality)))

(defn +extensions
  "Convert a single extensions string into a list of strings."
  [accept]
  (if-let [params (:parameters accept)]
    (->> (clojure.string/replace-first params #"q=([10]?\.?[0-9]+)" "")
         (re-seq #"[\w\.]+")
         vec
         (assoc accept :extensions))
    (assoc accept :extensions [])))

(defn +version
  "Non-standard."
  [accept]
  (->> (:subtype accept)
       (re-find #"v([0-9\.]+)")
       last
       (assoc accept :version)))

(defn +suffix
  ""
  [accept]
  (->> (:subtype accept)
       (re-find #"\+([\w\.]+)")
       last
       (assoc accept :suffix)))

(defn +specificity
  "Used when sorting indidual accept values"
  [accept]
  (assoc accept :specificity
         (cond->> 0
           (not-empty (accept :extensions)) (+ 4)
           (not= (accept :subtype) "*") (+ 2)
           (not= (accept :type) "*") (+ 1))))

(def accept-xf (comp (map +media-range)
                     (map +quality)
                     (map +extensions)
                     (map +version)
                     (map +suffix)
                     (map +specificity)))

(defn accept-builder
  ""
  [accept]
  (if accept
    (->> (clojure.string/split accept #"[,\s]+")
         (sequence accept-xf)
         (vec)
         (sort-by (juxt :quality :specificity)
                  #(compare %2 %1)))))

(defn accept-handler
  "A Ring handler that parses the Accept header and updates request
   with a map of accept string components."
  [handler]
  (fn [request]
    (if-let [header (get-in request [:headers "accept"])]
      (handler (assoc request :accept (accept-builder header))
               (handler request)))))

(defn json-handler
  "A Ring handler that converts the entire response to JSON and then updates
  the response body with that JSON."
  [handler]
  (fn [request]
    (let [response (handler request)
          body (json/write-str response)]
      (assoc response :body body))))

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
  * ENVI
  * ERDAS
  * geotiff
  * netCDF

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
     "envi"    #'gdal-content/base-handler
     "erdas"   #'gdal-content/base-handler
     "netcdf"  #'gdal-content/base-handler
     "geotiff" #'gdal-content/base-handler
     "raw" #'core/identity-handler
     default-type-hanlder)))

(defn get-content-type-wrapper
  "This is a utility function for extracting the route version from the request
  and then getting a supported route that matches the requested version."
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
