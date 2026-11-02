(ns gravity.c8-effect-checker.evidence
  "Compatibility entry points for native C8 record builders."
  (:require [gravity.effects :as effects]))

(defn legality-records [effect-capability module effect-graph]
  (effects/legality-records effect-capability module effect-graph))

(defn capability-proof-records [effect-capability module effect-graph]
  (effects/capability-proof-records effect-capability module effect-graph))

(defn build-effect-log [effect-capability module]
  (effects/build-effect-log effect-capability module))

(defn replay-requirements [effect-graph]
  (effects/replay-requirements effect-graph))

(defn ordering-constraints [effect-graph]
  (effects/ordering-constraints effect-graph))

(defn residual-effect-report [effect-graph]
  (effects/residual-effect-report effect-graph))
