(ns gravity.c13-optimization.projection
  (:require [gravity.optimization :as optimization]
            [gravity.c13-optimization.operations :as operations]
            [gravity.digest]))

(defn- sha256-hex [value]
  (operations/invoke :sha256-hex gravity.digest/sha256-hex value))

(defn artifact-base
  [configuration source-text module source-overrides domain-ir-artifact
   input-id contracts decisions diagnostics]
  (optimization/native-artifact-base configuration source-text module source-overrides domain-ir-artifact
                                     input-id contracts decisions diagnostics
                                     {:sha256-hex (fn [] sha256-hex)}))
