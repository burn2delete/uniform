(ns gravity.c8-effect-checker.verification
  "Compatibility entry points for native effect report verification."
  (:require [gravity.effects :as effects]))

(defn verifier-report
  [known-effects diagnostic-ids module effect-graph legality capability-proof
   build-log replay ordering residual diagnostics]
  (effects/native-verifier-report known-effects diagnostic-ids module effect-graph legality capability-proof
                                  build-log replay ordering residual diagnostics))

(defn capability-proof [artifact]
  (effects/native-capability-proof artifact))

(defn validate! [capability-proof effect-fail! source-path artifact]
  (effects/validate-native-report! capability-proof effect-fail! source-path artifact))
