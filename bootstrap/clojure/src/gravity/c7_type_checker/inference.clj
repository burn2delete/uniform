(ns gravity.c7-type-checker.inference
  "Compatibility entry points for the hosted seed builders owned by gravity.types."
  (:require [gravity.types :as types]))

(defn literal-type [value] (types/literal-type value))

(defn node-operator [node] (types/node-operator node))

(defn
 node-type
 [literal-type node-operator node]
 (types/node-type literal-type node-operator node))

(defn type-fact [node-type node] (types/type-fact node-type node))

(defn type-environment [type-facts] (types/type-environment type-facts))

(defn
 constraint-ledger
 [type-facts]
 (types/constraint-ledger type-facts))

(defn function-table [nodes] (types/function-table nodes))
