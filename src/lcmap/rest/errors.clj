(ns ^{:doc
  "LCMAP REST Service errors.

  This namespace presents a data structure containing all the errors defined
  for LCMAP REST as well as related accessor and utility functions.
  "}
  lcmap.rest.errors)

(def category
  {:gen {:uri ""
         :title ""
         :description ""}
   :auth {:uri ""
          :title ""
          :description ""}
   :data {:uri ""
          :title ""
          :description ""}
   :model {:uri ""
           :title ""
           :description ""}
   :event {:uri ""
           :title ""
           :description ""}
   :user {:uri ""
          :title ""
          :description ""}
   :system {:uri ""
            :title ""
            :description ""}})

(def lookup
  "LCMAP REST service error lookup map."
  { ;; General errors
    :10000 {:title "Invalid input data type"
            :category :gen
            :type (get-in category [:gen :uri])
            :detail ""
            :instance "<URL at error>"}
    :10001 {:title ""
            :category :gen
            :type (get-in category [:gen :uri])
            :detail ""
            :instance "<URL at error>"}
    :10002 {:title ""
            :category :gen
            :type (get-in category [:gen :uri])
            :detail ""
            :instance "<URL at error>"}
    ;; Authentication errors
    :20000 {:title ""
            :category :auth
            :type (get-in category [:auth :uri])
            :detail ""
            :instance "<URL at error>"}
    :20001 {:title ""
            :category :auth
            :type (get-in category [:auth :uri])
            :detail ""
            :instance "<URL at error>"}
    :20002 {:title ""
            :category :auth
            :type (get-in category [:auth :uri])
            :detail ""
            :instance "<URL at error>"}
    ;; Data/query and data component errors
    :30000 {:title ""
            :category :data
            :type (get-in category [:data :uri])
            :detail ""
            :instance "<URL at error>"}
    :30001 {:title ""
            :category :data
            :type (get-in category [:data :uri])
            :detail ""
            :instance "<URL at error>"}
    :30002 {:title ""
            :category :data
            :type (get-in category [:data :uri])
            :detail ""
            :instance "<URL at error>"}
    ;; Model execution and SEE component errors
    :40000 {:title ""
            :category :model
            :type (get-in category [:model :uri])
            :detail ""
            :instance "<URL at error>"}
    :40001 {:title ""
            :category :model
            :type (get-in category [:model :uri])
            :detail ""
            :instance "<URL at error>"}
    :40002 {:title ""
            :category :model
            :type (get-in category [:model :uri])
            :detail ""
            :instance "<URL at error>"}
    ;; Event, notification, and subscription component errors
    :50000 {:title ""
            :category :event
            :type (get-in category [:event :uri])
            :detail ""
            :instance "<URL at error>"}
    :50001 {:title ""
            :category :event
            :type (get-in category [:event :uri])
            :detail ""
            :instance "<URL at error>"}
    :50002 {:title ""
            :category :event
            :type (get-in category [:event :uri])
            :detail ""
            :instance "<URL at error>"}
    ;; User management errors
    :60000 {:title ""
            :category :user
            :type (get-in category [:user :uri])
            :detail ""
            :instance "<URL at error>"}
    :60001 {:title ""
            :category :user
            :type (get-in category [:user :uri])
            :detail ""
            :instance "<URL at error>"}
    :60002 {:title ""
            :category :user
            :type (get-in category [:user :uri])
            :detail ""
            :instance "<URL at error>"}
    ;; System management errors
    :70000 {:title ""
            :category :system
            :type (get-in category [:system :uri])
            :detail ""
            :instance "<URL at error>"}
    :70001 {:title ""
            :category :system
            :type (get-in category [:system :uri])
            :detail ""
            :instance "<URL at error>"}
    :70002 {:title ""
            :category :system
            :type (get-in category [:system :uri])
            :detail ""
            :instance "<URL at error>"}})
