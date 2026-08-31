; Semantic decomposition of committed HEAD reader line 141626.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-js-ts-backend-source-artifact-artifact
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
     manifest
     provenance-input
     provenance-hash
     paths
     provenance
     identity-input]}
   state
   artifact
   {:capabilities (get-in plan [:module :capabilities]),
    :seed-boundary {:clojure-seed-boundary? true, :self-hosted? false, :final-release? false},
    :input-plan-hash plan-hash,
    :diagnostics [],
    :closed-runtime-validation-context closed-runtime-context,
    :input-plan-id (:plan-id plan),
    :task "HOSTED-JS-TS-TARGET",
    :artifact-id (c4-artifact-id (c-backend-canonical-value identity-input)),
    :provenance-hash provenance-hash,
    :effect-summary (:effect-summary plan),
    :manifest manifest,
    :source {:kind :co-canonical-gravity-source, :sha256 source-hash},
    :instruction-summary (:instruction-summary plan),
    :closed-plan-runtime-target-record closed-plan-runtime,
    :manifest-hash manifest-hash,
    :stage2-compiler-driver-record driver-record,
    :stage2-expression-lowering-artifact (dissoc compiler-artifact-record :source-path),
    :status :complete-for-slice,
    :kind :gravity/js-ts-backend-artifact,
    :typescript-declarations js-ts-backend-declaration-source,
    :source-map source-map,
    :provenance provenance,
    :target (:target manifest),
    :package-metadata package-metadata,
    :compiled-execution-output-bytes (:stdout-bytes execution),
    :javascript-source javascript,
    :stage2-runtime-execution-record runtime-record,
    :compiled-execution-output expected-output,
    :target-eligibility (:target-eligibility packet)}]
  (clojure.core/assoc state :artifact artifact)))
