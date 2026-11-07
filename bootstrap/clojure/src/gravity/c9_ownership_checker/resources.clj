(ns gravity.c9-ownership-checker.resources
  "Linear resource, transfer, runtime-check, and unsafe-audit projections."
  (:require [gravity.ownership :as ownership]))

(defn linear-resource-flow-graph [module]
  (ownership/native-linear-resource-flow-graph module))

(defn transfer-records [module]
  (ownership/native-transfer-records module))

(defn runtime-check-records [module]
  (ownership/native-runtime-check-records module))

(defn unsafe-audit-references [module]
  (ownership/native-unsafe-audit-references module))
