(ns gravity.c10-safety-analysis.verification
  "Compatibility entry points for native Safety report verification."
  (:require [gravity.safety :as safety]))

(defn verifier-report
  [safe-outcomes diagnostic-ids c9-artifact inventory outcomes checks
   obligations certificates unsafe report generated optimization diagnostics]
  (safety/native-verifier-report safe-outcomes diagnostic-ids c9-artifact inventory outcomes checks
                                 obligations certificates unsafe report generated optimization diagnostics))

(defn capability-proof [artifact]
  (safety/native-capability-proof artifact))

(defn validate! [capability-proof-op fail-op source-path artifact]
  (safety/validate-native-report! capability-proof-op fail-op source-path artifact))
