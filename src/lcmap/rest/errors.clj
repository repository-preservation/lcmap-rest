(ns ^{:doc
  "LCMAP REST Service errors.

  This namespace presents a data structure containing all the errors defined
  for LCMAP REST as well as related utility functions.
  "
  lcmap.rest.errors)

(def lookup
  "LCMAP REST service error lookup map."
  { ;; General errors
    :10000 {}
    :10001 {}
    :10002 {}
    ;; Authentication errors
    :20000 {}
    :20001 {}
    :20002 {}
    ;; Data/query and data component errors
    :30000 {}
    :30001 {}
    :30002 {}
    ;; Model execution and SEE component errors
    :40000 {}
    :40001 {}
    :40002 {}
    ;; Event, notification, and subscription component errors
    :50000 {}
    :50001 {}
    :50002 {}
    ;; User management errors
    :60000 {}
    :60001 {}
    :60002 {}
    ;; System management errors
    :70000 {}
    :70001 {}
    :70002 {}})
