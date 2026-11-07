(ns gravity.module-analysis.effects
  (:require [gravity.effects :as stage]))

(defn uses-println?
  [recur-check form]
  (stage/stage0-uses-println? recur-check form))

(defn infer-effects
  [uses-println? forms]
  (stage/infer-stage0-effects uses-println? forms))

(defn required-capabilities-for-effects
  [effect-capability effects]
  (stage/required-stage0-capabilities effect-capability effects))

(defn validate-module-effects!
  [{:keys [uses-println? fail!]} module]
  (stage/validate-stage0-module-effects! {:uses-println? uses-println? :fail! fail!} module))
