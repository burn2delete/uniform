(ns gravity.c8-effect-checker.diagnostics
  "Compatibility entry points for native C8 diagnostic projection."
  (:require [gravity.effects :as effects]))

(defn source-overrides [module]
  (effects/native-source-overrides module))

(defn fail! [fail-operation effect-message source-span governing-document id source-path subject extra]
  (effects/native-diagnostic-fail! fail-operation effect-message source-span governing-document id source-path subject extra))

(defn validate-overrides! [effect-fail! source-span override-diagnostics effect-capability source-path module overrides]
  (effects/validate-native-overrides! effect-fail! source-span override-diagnostics effect-capability source-path module overrides))

(defn effect-diagnostics [source-span diagnostic-ids rejected-designs effect-capability source-path type-facts]
  (effects/native-diagnostics source-span diagnostic-ids rejected-designs effect-capability source-path type-facts))
