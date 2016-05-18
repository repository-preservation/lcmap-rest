(ns lcmap.rest.api.four-oh-four
  (:import [java.lang Runtime])
  (:require [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [compojure.route :as route]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (route/not-found {:result nil :errors ["Resource not found"]}))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
