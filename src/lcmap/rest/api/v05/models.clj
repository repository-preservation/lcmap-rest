(ns lcmap.rest.api.v05.models
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.models]
            [lcmap.rest.api.models.ccdc]
            [lcmap.rest.api.models.ccdc-docker]
            [lcmap.rest.api.models.ccdc-pipe]
            [lcmap.rest.api.models.sample-docker]
            [lcmap.rest.api.models.sample]
            [lcmap.rest.api.models.sample-pipe]
            [lcmap.rest.api.models.ndvi]
            [lcmap.rest.middleware.http-util :as http]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;; API Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources [context]
  (log/info (str "get-resources: " context))
  {:links (map #(str context %) ["/ccdc"
                                 "/ccdc-docker-process"
                                 "/ccdc-piped-processes"
                                 "/sample-docker-process"
                                 "/sample-os-process"
                                 "/sample-piped-processes"])})

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (context lcmap.client.models/context []
    (GET "/" request
      (http/response :result
        (get-resources (:uri request)))))
  lcmap.rest.api.models.ccdc/routes
  lcmap.rest.api.models.ccdc-docker/routes
  lcmap.rest.api.models.ccdc-pipe/routes
  lcmap.rest.api.models.ndvi/routes
  lcmap.rest.api.models.sample-docker/routes
  lcmap.rest.api.models.sample/routes
  lcmap.rest.api.models.sample-pipe/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
