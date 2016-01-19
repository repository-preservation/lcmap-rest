(ns lcmap-rest.tile.db
  (:require [clojurewerkz.cassaforte.client :as cc]
            [clojurewerkz.cassaforte.cql :as cql]
            [clojurewerkz.cassaforte.query :refer :all]))

(defn find-spec [ubid system]
  (let [conn    (get-in system [:tiledb :conn])
        table   (get-in system [:tiledb :spec-table])
        params  {:ubid ubid}
        spec    (cql/select conn table params)]
    spec))

(defn find-tiles [spec x y acquired system]
  (let [results []]
    results))
