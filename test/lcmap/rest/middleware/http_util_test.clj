(ns lcmap.rest.middleware.http-util-test
  (:require [clojure.test :refer :all]
            [lcmap.rest.middleware.http-util :as http]))

(deftest parse-accept-test
  (are [a b] (= a b)
    (http/parse-accept "text/html") {:accept-extension nil
                                     :media-range "text/html"
                                     :quality 1.0}
    (http/parse-accept "text/html;q=0.9") {:media-range "text/html"
                                           :quality 0.9
                                           :accept-extension nil}))

