(ns gravity.c8-effect-checker.facts
  "Compatibility entry points for the hosted effects stage."
  (:require [gravity.effects :as effects]))

(defn fact-direct-effects [fact] (effects/fact-direct-effects fact))

(defn effectful-facts [direct-effects type-facts]
  (effects/effectful-facts direct-effects type-facts))

(defn effect-graph
  [effectful-facts direct-effects replay-sensitive-effects
   module type-facts functions]
  (effects/effect-graph effectful-facts direct-effects replay-sensitive-effects
                        module type-facts functions))
