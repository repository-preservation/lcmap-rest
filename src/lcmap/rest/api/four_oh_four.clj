(ns lcmap.rest.api.four-oh-four
  (:import [java.lang Runtime])
  (:require [compojure.core :refer [GET HEAD POST PUT context defroutes]]
            [compojure.route :as route]
            [lcmap.rest.problem :as problem]))

;;; Supporting Functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD

;;; Routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defroutes routes
  (route/not-found {:body (problem/map->Problem {:title "Not Found"
                                                 :type "Not Found"
                                                 :detail "The resource you requested is not defined."
                                                 :status 404})}))

;;; Exception Handling ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TBD
