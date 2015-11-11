;;;; Event LCMAP REST Service system component
;;;; For more information, see the module-level code comments in
;;;; lcmap-rest.components.
(ns lcmap-rest.components.eventd
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [co.paralleluniverse.pulsar.actors :as actors]
            [lcmap-rest.job.tracker]))

(defrecord EventServer []
  component/Lifecycle

  (start [component]
    (log/info "Starting event server ...")
    (let [event-server (actors/spawn (actors/gen-event))]
      (actors/add-handler!
        event-server
        #'lcmap-rest.job.tracker/dispatch-handler)
      (log/debug "Component keys:" (keys component))
      (log/debug "Successfully created event server:" event-server)
      (assoc component :eventd event-server)))

  (stop [component]
    (log/info "Stopping event server ...")
    (log/debug "Component keys" (keys component))
    (if-let [event-server (:eventd component)]
      (actors/shutdown! (:eventd component)))
    (assoc component :eventd nil)))

(defn new-event-server []
  (->EventServer))
