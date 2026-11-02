(ns gravity.c8-effect-checker.catalog
  "Compatibility defaults for the native C8 catalog."
  (:require [gravity.effects :as effects]))

(def diagnostic-ids effects/native-diagnostic-ids)

(def governing-document effects/native-governing-document)

(def rejected-designs effects/native-rejected-designs)

(def override-diagnostics effects/native-override-diagnostics)

(def known-effects effects/native-known-effects)

(def effect-capability effects/native-effect-capability)

(def replay-sensitive-effects effects/native-replay-sensitive-effects)

(defn effect-message [id]
  (effects/native-effect-message id))
