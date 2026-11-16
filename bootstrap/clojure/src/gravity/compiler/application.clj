(ns gravity.compiler.application
  "Current source to freshly checked Core and verified scalar MIR.

   The compiler runs on the Clojure seed host. Source policy is declared by the
   source and its target is preserved separately from that host dependency."
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

(defn- discover-source-policy
  "Discover the declared policy with current Reader/Syntax and module parsing.
   This is preliminary input discovery, not semantic acceptance. The final
   source constructor rereads the source and verifies policy/byte agreement."
  [source-path]
  (let [source-text (bootstrap/read-gravity-source-text source-path)
        path (.getCanonicalPath (java.io.File. source-path))
        reader-path (bootstrap/sh03-reader-resolve-source-path)
        front-end
        (bootstrap/p15-s23-stage2-c2-c3-front-end-products
         path source-text (bootstrap/reader-project-context-for-source path)
         true reader-path
         {:reader-source-path reader-path
          :authenticated-envelope-options {:source :development-current}
          :syntax-source-selection
          {:schema :gravity/sh04-development-syntax-selection-v1
           :source-path (bootstrap/sh04-syntax-resolve-source-path)}})]
    (bootstrap/validate-ns-syntax! path (:forms front-end))
    (select-keys (bootstrap/parse-module path (:forms front-end))
                 [:profile :target :effects :capabilities :safety])))

(defn current-source-to-js-module
  "Compile hosted/safe :js-ts source and atomically publish a standalone module.

   The two-argument entrypoint discovers policy through the existing current
   Reader/module machinery. The three-argument entrypoint accepts an explicit
   matching five-key source policy. Both use the same fresh source constructor;
   preliminary products are never supplied as accepted IR. Options contain
   exactly :output-path, :target (:js-ts or the :js emission alias), :runtime
   :node20, :language :es2022 and :module-format :esm. The output directory must
   be fresh. I64 uses checked BigInt and Bool uses boolean. No accepted IR,
   verifier reports, compiler bindings or authority tokens are caller inputs."
  ([source-path options]
   (current-source-to-js-module source-path (discover-source-policy source-path) options))
  ([source-path policy options]
   ((requiring-resolve 'gravity.backend.js-checked-mir/compile-source!) source-path policy options)))
