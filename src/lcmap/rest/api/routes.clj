(ns ^{:doc
  "These are the routes defined for the LCMAP REST service.

  Note that all routes live under the ``/api`` path. This is to provide a clean
  delineation for deployment: the website may be hosted at
  http://anyhost.usgs.gov and the only thing the usgs.gov site admins
  would need to do is ensure that http://anyhost.usgs.gov/api is
  forwarded to wherever the LCMAP ``/api`` endpoint is running.

  This placement of all REST resources under the ``/api`` path should *not*
  be confused with the organization of the codebase. In particular, the
  ``lcmap.rest.api`` namespace only holds the code that is specific to the REST
  resources available at the ``/api`` service endpoint. There is a lot
  of supporting code for this project that lives in ``lcmap.rest.*``, not
  under ``lcmap.rest.api``."}
  lcmap.rest.api.routes
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes]]
            ;; v0
            [lcmap.rest.api.v0.auth]
            [lcmap.rest.api.v0.compatibility]
            [lcmap.rest.api.v0.data]
            [lcmap.rest.api.v0.four-oh-four]
            [lcmap.rest.api.v0.jobs]
            [lcmap.rest.api.v0.models]
            [lcmap.rest.api.v0.system]
            ;; v0.5
            [lcmap.rest.api.auth]
            [lcmap.rest.api.compatibility]
            [lcmap.rest.api.data]
            [lcmap.rest.api.four-oh-four]
            [lcmap.rest.api.jobs]
            [lcmap.rest.api.models]
            [lcmap.rest.api.system]
            [lcmap.rest.components.httpd :as httpd]
            [lcmap.rest.util :as util]))

(defroutes v0
  lcmap.rest.api.v0.auth/routes
  lcmap.rest.api.v0.compatibility/routes
  lcmap.rest.api.v0.data/routes
  lcmap.rest.api.v0.jobs/routes
  lcmap.rest.api.v0.models/routes
  lcmap.rest.api.v0.system/routes
  lcmap.rest.api.v0.four-oh-four/routes)

(defroutes v0.5
  lcmap.rest.api.auth/routes
  lcmap.rest.api.compatibility/routes
  lcmap.rest.api.data/routes
  lcmap.rest.api.jobs/routes
  lcmap.rest.api.models/routes
  ;;lcmap.rest.api.notifications/routes
  ;;lcmap.rest.api.operations/routes
  lcmap.rest.api.system/routes
  ;;lcmap.rest.api.users/routes
  lcmap.rest.api.four-oh-four/routes)

(defroutes v1
  ;; LCMAP Core Functionality
  ;; Support functionality
  lcmap.rest.api.auth/routes
  lcmap.rest.api.system/routes
  lcmap.rest.api.four-oh-four/routes)

(defroutes v2
  ;; LCMAP Core Functionality
  ;; Support functionality
  lcmap.rest.api.auth/routes
  lcmap.rest.api.system/routes
  lcmap.rest.api.four-oh-four/routes)

(defn get-versioned-routes
  "Perform a look up for the versioned route given an API version number."
  [version-str default]
  (case version-str
    "v0.0" #'v0
    "v0.5" #'v0.5
    "v1.0" #'v1
    "v2.0" #'v2
    ;; If no case applies, do the lookup using the default
    (get-versioned-routes default default)))
