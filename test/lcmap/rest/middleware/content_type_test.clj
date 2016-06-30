(ns lcmap.rest.middleware.content-type-test
  (:require [clojure.test :refer :all]
            [clojure.set :refer [subset?]]
            [lcmap.rest.middleware.content-type :as content-type]))

(deftest build-accept-header
  (testing "a simple accept header with wild cards"
    (let [actual (first (content-type/accept-builder "*/*"))]
      (testing "constructed map"
        (is (= "*/*") (actual :media-range)))
        (is (= "*" (actual :type)))
        (is (= "*" (actual :subtype)))
        (is (= nil (actual :suffix)))
        (is (= nil (actual :parameters)))
        (is (= [] (actual :extensions)))
        (is (= 1.0 (actual :quality)))))
  (testing "an accept header with quality and extensions"
    (let [actual (first (content-type/accept-builder "text/html;level=1;q=0.9"))]
      (testing "constructed map"
        (is (= "text/html" (actual :media-range)))
        (is (= "text" (actual :type)))
        (is (= "html" (actual :subtype)))
        (is (= ";level=1;q=0.9" (actual :parameters)))
        (is (= ["level" "1"] (actual :extensions)))
        (is (= 0.9 (actual :quality))))))
  (testing "multiple accept headers"
    (testing "specificity ordering"
      (let [header "text/*, text/html, text/html;level=1, */*"
            actual (content-type/accept-builder header)
            it (vec (map :original actual))]
        (is (= it ["text/html;level=1"
                   "text/html"
                   "text/*"
                   "*/*"])))))
  (testing "quality ordering"
    (let [header "text/html;q=0.8, text/xml;q=1.0, text/json;q=0.9, text/xhtml"
          actual (content-type/accept-builder header)
          it (vec (map :media-range actual))]
      (is (= it ["text/xml" "text/xhtml" "text/json" "text/html"]))))
  (testing "quality trumps and specificity"
    (let [header "text/*;q=0.9, text/json;level=1;q=1.0, text/html;q=0.9, text/plain;q=0.9;foo=bar"
          actual (content-type/accept-builder header)
          it (vec (map :media-range actual))]
      (is (= it ["text/json" "text/plain" "text/html" "text/*"])))))

(deftest non-standard-accept-values
  (testing "typical LCMAP header value f"
    (let [header "application/vndr.usgs.lcmap.v0.5+json"
          actual (first (content-type/accept-builder header))]
      (is (= "0.5" (actual :version)))
      (is (= "vndr.usgs.lcmap.v0.5+json" (actual :subtype)))
      (is (= "json" (actual :suffix))))))
