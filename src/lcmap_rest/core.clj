;;;; This module is for top-level  code independent of both the routes and main
;;;; modules, providing a means of averting cyclic dependencies.
(ns lcmap-rest.core
  (:require [clojure.tools.logging :as log]
            [lcmap-rest.util :as util]
            [lcmap-rest.l8 :as l8]
            [lcmap-rest.l8.surface-reflectance]))

(def noop :noop)
