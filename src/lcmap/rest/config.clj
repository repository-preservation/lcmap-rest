(ns lcmap.rest.config
  (:require [lcmap.config.helpers :as cfg]
            [lcmap.data.config :as data-cfg]
            [lcmap.client.config :as client-cfg]
            [schema.core :as schema]))

(def opt-spec [])

(def cfg-schema
  (merge data-cfg/cfg-schema
         client-cfg/cfg-schema
         {schema/Keyword schema/Any}))

(defn init
  "Produce a validated configuration map. When configuration is
  built in the context of another system, you may want to compose
  a schema for only the components you will use."
  [{:keys [path spec args schema]
    :or   {path (clojure.java.io/file (System/getenv "HOME") ".usgs" "lcmap.ini")
           spec opt-spec
           args *command-line-args*
           schema cfg-schema}
    :as params}]
  (cfg/init-cfg {:ini  path
                 :args args
                 :spec spec
                 :schema schema}))

(init {})
