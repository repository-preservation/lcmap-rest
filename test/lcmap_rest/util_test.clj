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
