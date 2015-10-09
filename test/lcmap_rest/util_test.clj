(ns lcmap-rest.util-test
  (:require [clojure.test :refer :all]
            [lcmap-rest.util :as util]))

(deftest parse-accept-test
  (is (= (util/parse-accept "text/html")
         (util/map->Accept {:media-range "text/html" :quality 1.0 :accept-extension nil} )))
  (is (= (util/parse-accept "text/html;q=0.9")
         (util/map->Accept {:media-range "text/html" :quality 0.9 :accept-extension nil} ))))
