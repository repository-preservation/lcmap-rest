;;;; This module is for top-level  code independent of both the routes and main
;;;; modules, providing a means of averting cyclic dependencies.
(ns lcmap-rest.core
  (:require [clojure.tools.logging :as log]))

(def noop :noop)
