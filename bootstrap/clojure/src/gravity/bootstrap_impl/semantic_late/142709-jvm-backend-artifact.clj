; Semantic decomposition of committed HEAD reader line 142709.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-jvm-backend-source-artifact-artifact
 [state]
 (clojure.core/let
  [{:keys
    [source-path
     source-text
     output-path
     emit?
     output
     parent
     javac-version
     java-version
     packet
     trusted-emitter-rule
     trusted-driver-rule
     trusted-runtime-rule
     trusted-plan
     plan
     writes-stdout?
     java-source
     module-source
     source-map
     plan-hash
     source-hash
     compiler-record
     closed-plan-runtime
     stage-directory
     staged-paths
     class-path
     module-class-path
     class-major
     jar-record
     expected-entries
     execution
     expected-output
     expected-bytes
     source-map-text
     content-hashes
     expected-input
     expected-effects
     expected-capabilities
     manifest-input
     manifest-hash
     manifest
     validation-context
     provenance-input
     provenance-hash
     final-paths
     provenance]}
   state
   identity
   {:source-content-hash source-hash,
    :closed-plan-target-record-hash (:record-hash closed-plan-runtime),
    :expression-lowering-artifact-hash (:artifact-hash compiler-record),
    :provenance-hash provenance-hash,
    :content-hashes content-hashes,
    :plan-emitter-source-rule-hash (get-in packet [:stage2-plan-emitter-rule :source-rule-hash]),
    :stage2-plan-hash plan-hash,
    :manifest-hash manifest-hash,
    :kind :gravity/jvm-backend-artifact}
   artifact
   {:capabilities (get-in plan [:module :capabilities]),
    :seed-boundary {:clojure-seed-boundary? true, :self-hosted? false, :final-release? false},
    :input-plan-hash plan-hash,
    :diagnostics [],
    :jar-record jar-record,
    :input-plan-id (:plan-id plan),
    :task "HOSTED-JVM-TARGET",
    :artifact-id (c4-artifact-id (c-backend-canonical-value identity)),
    :manifest-validation-context validation-context,
    :emitted-files final-paths,
    :provenance-hash provenance-hash,
    :effect-summary (:effect-summary plan),
    :manifest manifest,
    :source {:kind :co-canonical-gravity-source, :sha256 source-hash},
    :instruction-summary (:instruction-summary plan),
    :closed-plan-runtime-target-record closed-plan-runtime,
    :plan-emitter-source-rule-hash (get-in packet [:stage2-plan-emitter-rule :source-rule-hash]),
    :manifest-hash manifest-hash,
    :stage2-compiler-driver-record (:stage2-compiler-driver-record packet),
    :stage2-expression-lowering-artifact compiler-record,
    :status :complete-for-slice,
    :kind :gravity/jvm-backend-artifact,
    :module-source module-source,
    :source-map source-map,
    :provenance provenance,
    :classfile-major class-major,
    :target (:target manifest),
    :compiled-execution-output-bytes (:stdout-bytes execution),
    :stage2-runtime-execution-record (:stage2-runtime-execution-record packet),
    :java-source java-source,
    :compiled-execution-output expected-output,
    :target-eligibility (:target-eligibility packet)}]
  (clojure.core/assoc state :identity identity :artifact artifact)))
