(ns lcmap.rest.api.models
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [lcmap.client.models]
            [lcmap.rest.api.models.ccdc]
            [lcmap.rest.api.models.ccdc-docker-process]
            [lcmap.rest.api.models.ccdc-piped-processes]
            [lcmap.rest.api.models.sample-docker-process]
            [lcmap.rest.api.models.sample-os-process]
            [lcmap.rest.api.models.sample-piped-processes]
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
  lcmap.rest.api.models.ccdc-docker-process/routes
  lcmap.rest.api.models.ccdc-piped-processes/routes
  lcmap.rest.api.models.sample-docker-process/routes
  lcmap.rest.api.models.sample-os-process/routes
  lcmap.rest.api.models.sample-piped-processes/routes)

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX TBD
