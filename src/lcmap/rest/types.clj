(ns lcmap.rest.types
  (:require [clojure.tools.logging :as log]
            [clojure.string :as string]
            [schema.core :as schema]))

;;; Supporting Predicates ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn matches?
  "A predicate that returns true if the provided regex matches the given value.
  Otherwise returns false."
  [regex value]
  (if (nil? (re-matches regex value))
    false
    true))

(defn range?
  "A predicate that returns true if the integer value of the given string lies
  between the beginning and end interval values (inclusive). Otherwise returns
  false.

  This function is inmplemented using the less-than-or-equal-to operator."
  [begin end str-value]
  (let [value (new Integer str-value)]
    (and (<= begin value) (<= value end))))

(defn in-ints?
  "A predicate that returns true if the integer value of the given is contained
  in the range created by the beginning and end interval values (inclusive).
  Otherwise returns false.

  This function is inmplemented using the collection ``contains`` method."
  [start stop value]
  (.contains (range start (inc stop)) (new Integer value)))

(defn string-int?
  "A predicate that returns true if the supplied string can be interpreted as
  an integer, positive or negative. Otherwise returns false."
  [x]
  (and (string? x) (matches? #"[-+]?\d+" x)))

(defn day-range?
  "A predicate that returns true if the supplied value lies between 1 and 31,
  inclusive. Otherwise returns false.

  No check is performed if the supplied value is not a string."
  [x]
  (range? 1 31 x))

(defn month-range?
  "A predicate that returns true if the supplied value lies between 1 and 12,
  inclusive. Otherwise returns false.

  No check is performed if the supplied value is not a string."
  [x]
  (range? 1 12 x))

(defn four-digit?
  "A predicate that returns true if the supplied value is comprised of four
  digits.

  No check is performed if the supplied value is not a string."
  [x]
  (matches? #"\d{4}+" x))

(defn string-day?
  "A predicate that returns true if the supplied value lies between 1 and 31,
  inclusive. Otherwise returns false.

  This function differs from ``day-range?`` in that it first does an explicit
  check if the given value is a string."
  [x]
  (and (string-int? x) (day-range? x)))

(defn string-month?
  "A predicate that returns true if the supplied value lies between 1 and 12,
  inclusive. Otherwise returns false.

  This function differs from ``month-range?`` in that it first does an explicit
  check if the given value is a string."
  [x]
  (and (string-int? x) (month-range? x)))


(defn string-year?
  "A predicate that returns true if the supplied value is comprised of four
  digits. Otherwise returns false.

  This function differs from ``four-digit?`` in that it first does an explicit
  check if the given value is a string."
  [x]
  (and (string? x) (four-digit? x)))

(defn string-date?
  "A predicate that returns true if the supplied value can be interpreted as a
  date. Otherwise returns false.

  Note that the order must be year, then month, and lastly day. Month and day
  must both be comprised of two digits while the year value must be comprised
  of four. Any number of punctuation or alphabetic characters (any non-numeric
  values) may separate the year, month, and day. See the unit tests for both
  legal and illegal values."
  [x]
  (if (string? x)
    (let [regex  #"(\d{4})[^0-9]*?(\d{2})[^0-9]*?(\d{2})"
          parsed (re-matches regex x)]
      (and (not (nil? parsed))
           (four-digit? (get parsed 1))
           (month-range? (get parsed 2))
           (day-range? (get parsed 3))))
    false))

(defn string-bool?
  "A predicate that returns true if the supplied value can be interpreted as a
  boolean. Otherwise returns false."
  [x]
  (and (string? x) (.contains ["true" "false"] (string/lower-case x))))

;;; Schemas for Atomic Value Types ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def Any schema/Any)

(def StrInt
  "A Schema type that may be used for simple integer-as-string schemas."
  (schema/pred string-int?))

(def StrBool
  "A Schema type that may be used for simple boolean-as-string schemas."
  (schema/pred string-bool?))

(def StrDay
  "A Schema type that may be used for integer-as-day-of-the-month schemas."
  (schema/pred string-day?))

(def StrMonth
  "A Schema type that may be used for integer-as-month-of-the-year schemas."
  (schema/pred string-month?))

(def StrYear
  "A Schema type that may be used for integer-as-year schemas."
  (schema/pred string-year?))

;;; Schemas for Compound Value Types ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def StrDate
  "A compoound schema representing a string date.

  For details on formatting, see ``string-date?`` and its accompanying unit
  tests."
  (schema/pred string-date?))
