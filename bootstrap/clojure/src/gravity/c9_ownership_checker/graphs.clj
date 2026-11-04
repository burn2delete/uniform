(ns gravity.c9-ownership-checker.graphs
  "Ownership and borrow graph projections for the hosted C9 facade."
  (:require [gravity.ownership :as ownership]))

(defn ownership-graph [source-span node-ids node module effect-graph]
  (ownership/native-ownership-graph source-span node-ids node module effect-graph))

(defn borrow-graph [node-ids node module effect-graph]
  (ownership/native-borrow-graph node-ids node module effect-graph))
