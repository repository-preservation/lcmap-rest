(ns lcmap.rest.config
  (:require [lcmap.config.helpers :as cfg]
            [lcmap.client.config :as client-cfg]
            [lcmap.data.config :as data-cfg]
            [lcmap.event.config :as event-cfg]
            [lcmap.see.config :as see-cfg]
            [schema.core :as schema]))

(def opt-spec [])

(def cfg-schema
  (merge client-cfg/cfg-schema
         data-cfg/cfg-schema
         see-cfg/cfg-schema
         event-cfg/cfg-schema
         {schema/Keyword schema/Any}))

(def defaults
  {:ini (clojure.java.io/file (System/getenv "HOME") ".usgs" "lcmap.ini")
   :spec opt-spec
   :args *command-line-args*
   :schema cfg-schema})
