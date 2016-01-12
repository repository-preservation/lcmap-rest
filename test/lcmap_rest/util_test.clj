(ns lcmap-rest.util-test
  (:require [clojure.test :refer :all]
            [lcmap-rest.util :as util]))

(deftest parse-accept-test
  (are [a b] (= a b)
    (util/parse-accept "text/html") {:accept-extension nil
                                     :media-range "text/html"
                                     :quality 1.0}
    (util/parse-accept "text/html;q=0.9") {:media-range "text/html"
                                           :quality 0.9
                                           :accept-extension nil}))

(deftest get-args-hash-test
  (are [a b] (= a b)
    (util/get-args-hash "model-name" "a" "b" "c")
    "7e564057bd312ec540d3fbdc208427ba"
    (util/get-args-hash "model-name" "a" "b" "c" [1,2,3])
    "83a13779b6a2fd6286335402c94468f4"
    (util/get-args-hash "model-name" :arg-1 "a" "b" "c" :arg-2 [1,2,3])
    "643eea8614e1118b4bd24bbdffef0d51"))
