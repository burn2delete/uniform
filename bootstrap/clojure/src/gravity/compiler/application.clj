(ns gravity.compiler.application
  "Current source to freshly checked Core and verified scalar MIR.

   The compiler runs on the Clojure seed host. Source policy remains explicit
   and source-target identity remains JVM at this intermediate boundary."
  (:require [gravity.bootstrap :as bootstrap]))

(defn current-source-to-verified-mir
  "Compile an actual .gravity/.qst source file under an explicit source policy.

   Policy must contain exactly :profile, :target, :effects, :capabilities and
   :safety, matching the source declaration. This entrypoint supports the
   hosted/safe JVM scalar functions with checked I64 addition, Bool branches,
   eager local bindings and nonrecursive fixed-arity direct calls.
   It returns source-bound checked Core, verified MIR, actual verifier reports
   and current owner identities. Owned diagnostics are thrown on rejection.
   Caller-supplied Core, MIR, verifier reports or authority are not accepted."
  [source-path policy]
  (bootstrap/p15-s23-current-source-to-verified-mir source-path policy))
