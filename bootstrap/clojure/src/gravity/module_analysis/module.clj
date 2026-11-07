(ns gravity.module-analysis.module
  (:require [gravity.resolution.stage0-namespace :as stage]))

(defn parse-module
  [{:keys [require-ns parse-clause single-clause-value clause-args parse-dependencies fail! source-span known-source-profiles supported-profiles supported-targets bootstrap-target-supported?]} source-path forms]
  (stage/parse-module {:clause-args clause-args, :source-span source-span, :bootstrap-target-supported? bootstrap-target-supported?, :single-clause-value single-clause-value, :parse-dependencies parse-dependencies, :supported-targets supported-targets, :require-ns require-ns, :supported-profiles supported-profiles, :parse-clause parse-clause, :fail! fail!, :known-source-profiles known-source-profiles} source-path forms))
