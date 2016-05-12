(ns lcmap.rest.config
  (:require [lcmap.config.helpers :refer :all]
            [lcmap.client.config :as client-cfg]
            [lcmap.data.config :as data-cfg]
            [lcmap.event.config :as event-cfg]
            [lcmap.see.config :as see-cfg]
            [schema.core :as schema]))

(def opt-spec [])

(def rest-schema
  {:lcmap.rest {:ip schema/Str
                :port schema/Num
                :auth-backend schema/Str
                :auth-endpoint schema/Str
                schema/Keyword schema/Str}})

(def logging-schema
  {:lcmap.logging {:level schema/Str
                   :namespaces [schema/Str]}})

(def cfg-schema
  (merge rest-schema
         logging-schema
         client-cfg/client-schema
         data-cfg/data-schema
         see-cfg/see-schema
         event-cfg/event-schema
         {schema/Keyword schema/Any}))

(def defaults
  {:ini *lcmap-config-ini*
   :args *command-line-args*
   :spec opt-spec
   :schema cfg-schema})
