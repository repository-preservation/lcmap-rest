(ns ^{:doc
  "LCMAP REST Service errors.

  This namespace presents a data structure containing all the errors defined
  for LCMAP REST as well as related accessor and utility functions.
  "}
  lcmap.rest.errors
  (:require [clojure.tools.logging :as log]
            [lcmap.client.system]))

(def context lcmap.client.system/reference)
(def category-uri (str context "/error-type/%s"))

;;; Error categories ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def category
  {:gen {:uri (format category-uri "gen")
         :title "General Errors"
         :description ""}
   :auth {:uri (format category-uri "auth")
          :title "Authentication Errors"
          :description ""}
   :data {:uri (format category-uri "data")
          :title "Data Errors"
          :description ""}
   :model {:uri (format category-uri "model")
           :title "Execution Environment Errors"
           :description ""}
   :event {:uri (format category-uri "event")
           :title "Event System Errors"
           :description ""}
   :user {:uri (format category-uri "user")
          :title "User Management Errors"
          :description ""}
   :system {:uri (format category-uri "system")
            :title "System Management Errors"
            :description ""}})

;;; Error codes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def invalid-type :10000)
(def bad-creds :20401)
(def auth-not-found :20400)
(def auth-server-error :20500)
(def no-auth-conn :20502)

;;; Error data lookup ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX Below we mention "<URL at error>" ... idea: save errors to logging
;; service (or db) and get a UUID for 1) the full log + traceback (accessible)
;; only be those with admin permissions) and for 2) a limited error log entry
;; that any user would have access to. This extended info would then be made
;; available via an /api/errors/:uuid resource request.

(def lookup
  "LCMAP REST service error lookup map."
  { ;; General errors
    invalid-type {
      :title "Invalid input data type"
      :category :gen
      :type (get-in category [:gen :uri])
      :detail ""
      :instance "<URL at error>"}
    :10001 {
      :title ""
      :category :gen
      :type (get-in category [:gen :uri])
      :detail ""
      :instance "<URL at error>"}
    :10002 {
      :title ""
      :category :gen
      :type (get-in category [:gen :uri])
      :detail ""
      :instance "<URL at error>"}
    ;; Authentication errors
    bad-creds {
      :title "Bad username or password"
      :category :auth
      :type (get-in category [:auth :uri])
      :detail ""
      :instance "<URL at error>"}
    auth-not-found {
      :title "Authentication resource not found"
      :category :auth
      :type (get-in category [:auth :uri])
      :detail ""
      :instance "<URL at error>"}
    auth-server-error {
      :title "Authentication server error"
      :category :auth
      :type (get-in category [:auth :uri])
      :detail ""
      :instance "<URL at error>"}
    no-auth-conn {
      :title "Cannot connect to authentication server"
      :category :auth
      :type (get-in category [:auth :uri])
      :detail ""
      :instance "<URL at error>"}
    ;; Data/query and data component errors
    :30000 {
      :title ""
      :category :data
      :type (get-in category [:data :uri])
      :detail ""
      :instance "<URL at error>"}
    :30001 {
      :title ""
      :category :data
      :type (get-in category [:data :uri])
      :detail ""
      :instance "<URL at error>"}
    :30002 {
      :title ""
      :category :data
      :type (get-in category [:data :uri])
      :detail ""
      :instance "<URL at error>"}
    ;; Model execution and SEE component errors
    :40000 {
      :title ""
      :category :model
      :type (get-in category [:model :uri])
      :detail ""
      :instance "<URL at error>"}
    :40001 {
      :title ""
      :category :model
      :type (get-in category [:model :uri])
      :detail ""
      :instance "<URL at error>"}
    :40002 {
      :title ""
      :category :model
      :type (get-in category [:model :uri])
      :detail ""
      :instance "<URL at error>"}
    ;; Event, notification, and subscription component errors
    :50000 {
      :title ""
      :category :event
      :type (get-in category [:event :uri])
      :detail ""
      :instance "<URL at error>"}
    :50001 {
      :title ""
      :category :event
      :type (get-in category [:event :uri])
      :detail ""
      :instance "<URL at error>"}
    :50002 {
      :title ""
      :category :event
      :type (get-in category [:event :uri])
      :detail ""
      :instance "<URL at error>"}
    ;; User management errors
    :60000 {
      :title ""
      :category :user
      :type (get-in category [:user :uri])
      :detail ""
      :instance "<URL at error>"}
    :60001 {
      :title ""
      :category :user
      :type (get-in category [:user :uri])
      :detail ""
      :instance "<URL at error>"}
    :60002 {
      :title ""
      :category :user
      :type (get-in category [:user :uri])
      :detail ""
      :instance "<URL at error>"}
    ;; System management errors
    :70000 {
      :title ""
      :category :system
      :type (get-in category [:system :uri])
      :detail ""
      :instance "<URL at error>"}
    :70001 {
      :title ""
      :category :system
      :type (get-in category [:system :uri])
      :detail ""
      :instance "<URL at error>"}
    :70002 {
      :title ""
      :category :system
      :type (get-in category [:system :uri])
      :detail ""
      :instance "<URL at error>"}})

;;; Error utility functions ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-message [exception]
  (if (map? exception)
    exception
    (.getMessage exception)))

(defn process-error [exception id]
  (let [error-data (id lookup)]
    (log/error (:title error-data))
    (assoc error-data :detail (get-message exception))))
