(ns lcmap.rest.util-test
  (:require [clojure.test :refer :all]
            [lcmap.rest.util :as util]))

(deftest make-bool-test
  (are [a b] (= a b)
    (util/make-bool 0) false
    (util/make-bool "0") false
    (util/make-bool false) false
    (util/make-bool "false") false
    (util/make-bool :false) false
    (util/make-bool nil) false
    (util/make-bool "nil") false
    (util/make-bool :nil) false
    (util/make-bool 1) true
    (util/make-bool "1") true
    (util/make-bool true) true
    (util/make-bool "true") true
    (util/make-bool :true) true
    (util/make-bool :something) true
    (util/make-bool "data") true
    (util/make-bool :nil) false))

(deftest make-flag-test
  (are [a b] (= a b)
    (util/make-flag "--help" nil :unary? true) nil
    (util/make-flag "--help" false :unary? true) nil
    (util/make-flag "--help" true :unary? true) "--help"
    (util/make-flag "--help" "something" :unary? true) "--help"
    (util/make-flag "--result" nil) nil
    (util/make-flag "--result" 3.14) "--result 3.14"))
