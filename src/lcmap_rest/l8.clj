;;;; This module is for Landsat8-specific code independent of both the routes
;;;; and main modules, providing a means of averting cyclic dependencies.
(ns lcmap-rest.l8
  (:require [clojure.tools.logging :as log]))

(def noop :noop)
