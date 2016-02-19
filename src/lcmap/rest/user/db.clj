(ns lcmap.rest.user.db
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]
            [lcmap.rest.util :as util]))

(def user-namespace "lcmap")
(def user-table "users")

