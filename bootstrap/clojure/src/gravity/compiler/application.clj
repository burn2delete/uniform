(ns gravity.compiler.application
  "Current source to freshly checked Core and verified scalar MIR.

   The compiler runs on the Clojure seed host. Source policy remains explicit
   and the source target is preserved separately from that host dependency."
  (:require [gravity.bootstrap :as bootstrap]))

(defn current-source-to-verified-mir
  "Compile an actual .gravity/.qst source file under an explicit source policy.

   Policy must contain exactly :profile, :target, :effects, :capabilities and
   :safety, matching the source declaration. This entrypoint supports the
   hosted/safe JVM or canonical :js-ts scalar functions with checked I64 addition, Bool branches,
   eager local bindings and nonrecursive fixed-arity direct calls.
   It returns source-bound checked Core, verified MIR, actual verifier reports
   and current owner identities. Owned diagnostics are thrown on rejection.
   Caller-supplied Core, MIR, verifier reports or authority are not accepted."
  [source-path policy]
  (bootstrap/p15-s23-current-source-to-verified-mir source-path policy))

(defn current-source-to-js-module
  "Compile hosted/safe :js-ts source and atomically publish a standalone module.

   Policy is the explicit matching five-key source policy. Options contain
   exactly :output-path, :target (:js-ts or the :js emission alias), :runtime
   :node20, :language :es2022 and :module-format :esm. The output directory must
   be fresh. I64 uses checked BigInt and Bool uses boolean. No accepted IR,
   verifier reports, compiler bindings or authority tokens are caller inputs."
  [source-path policy options]
  ((requiring-resolve 'gravity.backend.js-checked-mir/compile-source!) source-path policy options))
