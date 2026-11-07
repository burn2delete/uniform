(ns gravity.module-analysis.clauses
  (:require [gravity.resolution.stage0-namespace :as stage]))

(defn require-ns
  [{:keys [ns-form? fail! source-span]} source-path forms]
  (stage/require-ns {:ns-form? ns-form?, :fail! fail!, :source-span source-span} source-path forms))

(defn parse-clause
  [{:keys [fail! source-span]} source-path clause]
  (stage/parse-clause {:fail! fail!, :source-span source-span} source-path clause))

(defn single-clause-value
  [{:keys [fail! source-span]} source-path clause-map key required?]
  (stage/single-clause-value {:fail! fail!, :source-span source-span} source-path clause-map key required?))

(defn clause-args
  [clause-map key]
  (stage/clause-args clause-map key))
