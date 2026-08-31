; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-safety-provenance
 [state]
 (clojure.core/let
  [{:keys
    [record
     context
     digest?
     context-envelope-valid?
     expected-record-hash
     expected-actual-path-binding-base
     expected-actual-path-binding
     runtime-source-file
     runtime-source-file-valid?
     runtime-source-file-hash
     contract-binding
     authority-binding
     adapter-record
     adapter-decisions
     adapter-actions
     validation
     io-write-active?
     capture-invoked?
     expected-adapter-record
     expected-decision-count
     expected-action-count
     expected-provider-ids
     expected-grant-ids
     required-decision-fields
     required-action-fields
     invocation
     verification-replay
     execution
     expected-validation-hash]}
   state]
  (clojure.core/and
   (=
    {:proven-allocation-count 25,
     :capabilities #{:memory/allocator},
     :allocation-unproven-primitives '#{rest},
     :allocation-unproven-count 4,
     :proven-allocation-operation-counts {:str 11, :vector 10, :map 2, :conj 1, :assoc 1},
     :pure-primitives '#{first = < <= get second > >= count},
     :allocation-unproven-operation-counts {:rest 4},
     :effects #{:memory/allocate},
     :kind :clojure-seed-runtime-primitive-classification,
     :proven-allocation-literals #{:map-literal :vector-literal},
     :proven-allocation-primitives '#{conj str assoc}}
    (:primitive-boundary record))
   (=
    {:closed-plan-reference
     {:handler-id :gravity.reference/transcript-string-handler,
      :live-stdout? false,
      :provider-id :gravity.reference/transcript-capture,
      :handler-capabilities #{:test/fixture},
      :source-capabilities #{:memory/allocator :io/stdout},
      :direct-handler-function p15-s23-reference-runtime-handler-function,
      :scope :closed-plan-interpreter,
      :transitive-function-scope
      '#{p15-s23-runtime-execute-closed-plan
         p15-s23-runtime-evaluate-bindings
         p15-s23-runtime-evaluate-arguments
         p15-s23-runtime-evaluate-closed-instruction
         p15-s23-runtime-evaluate-sequence},
      :handled #{:io/write},
      :escaping #{:memory/allocate}},
     :legacy-println-helpers
     {:function-scope '#{p15-s23-runtime-println-value p15-s23-runtime-println-two},
      :handled #{},
      :escaping #{:memory/allocate :io/write},
      :provider-selection :unresolved,
      :grant :unresolved,
      :status :unresolved},
     :deployment-stdout
     {:effect :io/write,
      :capability :io/stdout,
      :provider-selection :unresolved,
      :grant :unresolved,
      :closed-plan-interpreter-excluded? true,
      :status :unresolved},
     :build-effects #{}}
    (:effect-projections record))
   (=
    {:release-credit? false,
     :c11-credit? false,
     :deployment-runtime? false,
     :target-lowering-credit? false,
     :typed-fourth-authority-consumed-by-this-packet? false,
     :checked-core-binary-integer-comparison-operations '#{= < <= > >=},
     :verification-replay-policy-id p15-s23-checked-core-verification-replay-policy-id,
     :checked-core-admission-scope :authenticated-hosted-jvm-reference-interpreter,
     :typed-fourth-authority :gravity/p15-s23-checked-core-authority-binding-v1,
     :checked-core-binary-integer-comparison-admission? true,
     :reference-runtime? true,
     :checked-core-str-println-admission? true}
    (:credit-boundary record))
   (=
    {:kind :actual-path-context,
     :context-key :runtime-artifact-source-path,
     :semantic-identity-input? false}
    (:provenance-binding record))
   (=
    {:clojure-stage2-executor? true,
     :stage0-oracle? true,
     :three-way-output-equivalent? true,
     :closed-plan-variadic-println? true}
    (:migration-comparison record))
   (false? (:mir-derived? record))
   (true? (:clojure-seed-boundary? record))
   (false? (:self-hosted? record)))))
