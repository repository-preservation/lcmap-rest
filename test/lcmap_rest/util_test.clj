(ns lcmap-rest.util-test
  (:require [clojure.test :refer :all]
            [lcmap-rest.util :refer :all]
            :reload))

(deftest accept-parser-tests
  (is (= (accept "text/html")
         (map->Accept {:media-range "text/html" :quality 1.0 :accept-extension nil} )))
  (is (= (accept "text/html;q=0.9")
         (map->Accept {:media-range "text/html" :quality 0.9 :accept-extension nil} ))))
