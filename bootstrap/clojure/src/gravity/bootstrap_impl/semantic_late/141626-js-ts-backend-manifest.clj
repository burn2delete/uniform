; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-js-ts-backend-source-artifact-manifest
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
     execution]}
   state
   manifest-input
   {:host-globals
    (if
     writes-stdout?
     [{:module "node:process",
       :symbol :stdout,
       :effect :io/write,
       :capability :io/stdout,
       :representation :uint8array-bytes}]
     []),
    :release-grade? false,
    :capabilities (get-in plan [:module :capabilities]),
    :typescript-compiler {:available? false, :required? false, :reason :tsc-not-installed},
    :diagnostics [],
    :closed-plan-runtime closed-plan-runtime,
    :conformance
    {:node-check :passed,
     :stage2-differential :passed,
     :stdout-byte-exact? true,
     :source-map :partial,
     :source-map-coverage :source-unit-only,
     :per-form-origin-preserved? false,
     :b6-conforming? false},
    :exception-policy :no-host-exception-boundary-in-slice,
    :emits
    [:javascript :typescript-declarations :source-map :package-metadata :manifest :provenance],
    :schema-version 1,
    :content-hashes
    {:javascript js-hash,
     :typescript-declarations declaration-hash,
     :source-map source-map-hash,
     :package-metadata package-hash},
    :self-hosted? false,
    :module {:side-effects writes-stdout?, :package-boundary :standalone},
    :effects (:effect-summary plan),
    :safety {:mode (get-in plan [:module :safety]), :unsafe-islands [], :status :preserved},
    :numeric-representation
    {:mode :hosted-scalar-spelling, :bytes :utf8, :lossy-number-lowering? false},
    :artifact :gravity/js-ts-backend-manifest,
    :input
    {:source-content-hash source-hash,
     :plan-assembly-invoked? (:plan-assembly-invoked? compiler-artifact-record),
     :source-declared-target (get-in packet [:target-eligibility :source-declared-target]),
     :plan-assembly-artifact-hash (:plan-assembly-artifact-hash compiler-artifact-record),
     :expression-lowering-artifact-hash (:artifact-hash compiler-artifact-record),
     :compiler-driver-rule-hash (:driver-rule-hash driver-rule),
     :runtime-rule-hash (:runtime-rule-hash runtime-rule),
     :expression-lowering-semantic-hash (:semantic-hash compiler-artifact-record),
     :expression-lowering-generic-bridge-residual?
     (:generic-bridge-residual? compiler-artifact-record),
     :plan-assembly-generic-bridge-residual?
     (:plan-assembly-generic-bridge-residual? compiler-artifact-record),
     :stage2-plan-hash plan-hash,
     :requested-backend-target target,
     :plan-assembly-function (:plan-assembly-function compiler-artifact-record),
     :expression-lowering-invoked? (:invoked? compiler-artifact-record),
     :runtime-artifact-hash (:runtime-artifact-hash runtime-rule),
     :expression-lowering-source-content-hash (:source-content-hash compiler-artifact-record),
     :plan-assembly-semantic-hash (:plan-assembly-semantic-hash compiler-artifact-record),
     :plan-assembly-source-content-hash
     (:plan-assembly-source-content-hash compiler-artifact-record),
     :target-eligibility (:target-eligibility packet)},
    :target
    {:runtime js-ts-backend-runtime,
     :runtime-version node-version,
     :ecmascript js-ts-backend-ecmascript,
     :module-format js-ts-backend-module-format},
    :backend :gravity.backend/js-ts,
    :profile :hosted,
    :nullish-policy :no-host-nullish-inputs,
    :clojure-seed-boundary? true}
   manifest-hash
   (str
    "sha256:"
    (sha256-hex
     (pr-str
      (c-backend-canonical-value
       (update
        manifest-input
        :closed-plan-runtime
        p15-s23-closed-runtime-target-semantic-record)))))
   manifest
   (assoc manifest-input :manifest-hash manifest-hash)
   _
   (js-ts-backend-validate-manifest! source-path manifest closed-runtime-context)]
  (clojure.core/assoc
   state
   :manifest-input
   manifest-input
   :manifest-hash
   manifest-hash
   :manifest
   manifest)))
