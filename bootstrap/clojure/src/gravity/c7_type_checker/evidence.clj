(ns gravity.c7-type-checker.evidence
  "Compatibility entry points for hosted seed records owned by gravity.types."
  (:require [gravity.types :as types]))

(defn dynamic-boundary-records [node-operator nodes module]
  (types/dynamic-boundary-records node-operator nodes module))

(defn cast-records [node-operator nodes]
  (types/cast-records node-operator nodes))

(defn generic-instantiations [node-operator nodes]
  (types/generic-instantiations node-operator nodes))

(defn protocol-dispatch-table [node-operator nodes]
  (types/protocol-dispatch-table node-operator nodes))

(defn schema-links [domain-boundaries]
  (types/schema-links domain-boundaries))

(defn layout-facts [node-type nodes]
  (types/layout-facts node-type nodes))
