;;;; These are the routes defined for the LCMAP REST service.
;;;;
;;;; Note that all routes live under the /api path. This is to provide a clean
;;;; delineation for deployment: the website may be hosted at
;;;; http://lcmap.eros.usgs.gov and the only thing the usgs.gov site admins
;;;; would need to do is ensure that http://lcmap.eros.usgs.gov/api is
;;;; forwarded to wherever the LCMAP /api endpoint is running.
;;;;
;;;; This placement of all REST resources under the /api path should not be
;;;; confused with the organization of the codebase. In particular, the
;;;; lcmap-rest.api namespace holds code that is specific to the REST resources
;;;; available at the /api endpoint, however there is a lot of supporting code
;;;; for this project that lives in lcmap-rest.*, and not under lcmap-rest.api.
(ns lcmap-rest.api.routes
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes]]
            [lcmap-rest.api.auth]
            [lcmap-rest.api.compatibility]
            [lcmap-rest.api.data]
            [lcmap-rest.api.four-oh-four]
            [lcmap-rest.api.jobs]
            [lcmap-rest.api.models]
            [lcmap-rest.api.system]
            [lcmap-rest.components.httpd :as httpd]
            [lcmap-rest.util :as util]))

(defroutes v0
  ;; LCMAP Core Functionality
  lcmap-rest.api.data/routes
  lcmap-rest.api.models/routes
  ;;lcmap-rest.api.notifications/routes
  ;; Support functionality
  lcmap-rest.api.auth/routes
  lcmap-rest.api.compatibility/routes
  lcmap-rest.api.jobs/routes
  ;;lcmap-rest.api.operations/routes
  ;;lcmap-rest.api.users/routes
  lcmap-rest.api.system/routes
  lcmap-rest.api.four-oh-four/routes)

(defroutes v1
  ;; LCMAP Core Functionality
  ;; Support functionality
  lcmap-rest.api.auth/routes
  lcmap-rest.api.system/routes
  lcmap-rest.api.four-oh-four/routes)

(defroutes v2
  ;; LCMAP Core Functionality
  ;; Support functionality
  lcmap-rest.api.auth/routes
  lcmap-rest.api.system/routes
  lcmap-rest.api.four-oh-four/routes)

