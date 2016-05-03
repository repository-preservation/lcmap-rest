(ns lcmap.rest.util-test
  (:require [clojure.test :refer :all]
            [lcmap.rest.util :as util]))

(deftest get-args-hash-test
  (are [a b] (= a b)
    (util/get-args-hash "model-name" "a" "b" "c")
    "7e564057bd312ec540d3fbdc208427ba"
    (util/get-args-hash "model-name" "a" "b" "c" [1 2 3])
    "83a13779b6a2fd6286335402c94468f4"
    (util/get-args-hash "model-name" :arg-1 "a" "b" "c" :arg-2 [1 2 3])
    "643eea8614e1118b4bd24bbdffef0d51"))

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
