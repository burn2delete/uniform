; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-js-ts-backend-source-artifact-provenance
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     target
     output-path
     emit?
     node-version
     packet
     compiler-artifact-record
     compiler-artifact-source-path
     driver-record
     runtime-record
     runtime-rule
     driver-rule
     closed-plan-runtime
     closed-runtime-context
     plan
     plan-hash
     javascript
     writes-stdout?
     source-map
     package-metadata
     js-hash
     declaration-hash
     source-map-hash
     package-hash
     source-hash
     expected-output
     expected-bytes
     temp-directory
     temp-module
     execution
     manifest-input
     manifest-hash
     manifest]}
   state
   provenance-input
   {:source-content-hash source-hash,
    :seed-boundary {:clojure-seed-boundary? true, :self-hosted? false},
    :stage2-compiler-driver-rule-hash (:driver-rule-hash driver-rule),
    :closed-plan-runtime closed-plan-runtime,
    :stage2-runtime-artifact-hash (:runtime-artifact-hash runtime-rule),
    :backend-version 1,
    :schema-version 1,
    :stage2-plan-hash plan-hash,
    :manifest-hash manifest-hash,
    :stage2-expression-lowering-artifact (dissoc compiler-artifact-record :source-path),
    :pass-history
    [:c2-reader
     :stage2-source-front-end
     :stage2-plan-emitter
     :stage2-compiler-driver
     :stage2-runtime-executor
     :js-ts-lowering],
    :artifact :gravity/js-ts-provenance,
    :stage2-runtime-rule-hash (:runtime-rule-hash runtime-rule),
    :target js-ts-backend-target,
    :backend :gravity.backend/js-ts,
    :target-eligibility (:target-eligibility packet)}
   provenance-hash
   (str
    "sha256:"
    (sha256-hex
     (pr-str
      (c-backend-canonical-value
       (update
        provenance-input
        :closed-plan-runtime
        p15-s23-closed-runtime-target-semantic-record)))))
   paths
   (when output-path (js-ts-backend-output-paths (str output-path)))
   provenance
   (assoc
    provenance-input
    :provenance-hash
    provenance-hash
    :actual-paths
    {:source source-path,
     :outputs paths,
     :stage2-expression-lowering-source compiler-artifact-source-path,
     :stage2-runtime-artifact-source (:runtime-artifact-source-path runtime-rule)})
   identity-input
   {:source-content-hash source-hash,
    :closed-plan-target-record-hash (:record-hash closed-plan-runtime),
    :javascript-hash js-hash,
    :package-hash package-hash,
    :source-map-hash source-map-hash,
    :expression-lowering-artifact-hash (:artifact-hash compiler-artifact-record),
    :provenance-hash provenance-hash,
    :stage2-plan-hash plan-hash,
    :manifest-hash manifest-hash,
    :declaration-hash declaration-hash,
    :kind :gravity/js-ts-backend-artifact}]
  (clojure.core/assoc
   state
   :provenance-input
   provenance-input
   :provenance-hash
   provenance-hash
   :paths
   paths
   :provenance
   provenance
   :identity-input
   identity-input)))
