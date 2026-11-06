(ns gravity.c9-ownership-checker.lifecycles
  "Lifetime, escape, region, and arena projections for the hosted C9 facade."
  (:require [gravity.ownership :as ownership]))

(defn lifetime-interval-map [module]
  (ownership/native-lifetime-interval-map module))

(defn escape-analysis-report [module]
  (ownership/native-escape-analysis-report module))

(defn region-lifetime-graph [module]
  (ownership/native-region-lifetime-graph module))

(defn arena-generation-graph [module]
  (ownership/native-arena-generation-graph module))
