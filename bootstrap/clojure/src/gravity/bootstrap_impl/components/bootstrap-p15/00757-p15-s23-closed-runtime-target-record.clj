

(defn p15-s23-closed-runtime-target-record
  "Build target evidence after the consumer has authenticated the packet once.
  The replay record is distinct from the packet's authoritative invocation."
  [packet]
  (let [runtime-rule (:stage2-runtime-rule packet)
        adapter-record (:runtime-contract-adapter-record packet)
        contract-validation (:runtime-contract-validation-record runtime-rule)
        validation (:closed-plan-validation-record packet)
        execution (:closed-plan-execution-record packet)
        invocation (:closed-plan-invocation-record packet)
        validation-product
        (assoc (select-keys validation
                            [:artifact :status :entrypoint :operation-set
                             :observed-operation-set
                             :maximum-depth :maximum-nodes])
               :plan-id (:plan-id invocation))
        validation-hash
        (str "sha256:"
             (sha256-hex
              (pr-str
               (c-backend-canonical-value validation-product))))
        actual-path-binding-base
        {:actual-path (:runtime-artifact-source-path runtime-rule)
         :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
         :runtime-source-content-hash
         (:runtime-source-content-hash runtime-rule)}
        actual-path-binding
        (assoc actual-path-binding-base
               :binding-hash
               (p15-s23-reference-runtime-hash actual-path-binding-base))
        record
        {:artifact :gravity/p15-s23-runtime-closed-plan-target-record
         :runtime-artifact-source-content-hash
         (:runtime-artifact-source-content-hash runtime-rule)
         :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
         :runtime-contract-binding
         {:contract-definition-hash
          (:runtime-contract-definition-hash runtime-rule)
          :derived-contract-facts-hash
          (:runtime-contract-derived-facts-hash runtime-rule)
          :function-hashes (:runtime-artifact-function-hashes runtime-rule)
          :providers (:runtime-artifact-providers runtime-rule)
          :validation
          (select-keys contract-validation
                       [:artifact :status :function-count
                        :contract-definition-count :operation-count
                        :proven-allocation-count
                        :allocation-unproven-count :handler-scope
                        :escaping-io-functions])
          :generic-emitter-effect-summary-credited? false}
         :executor-function
         (:runtime-artifact-closed-plan-function runtime-rule)
         :executor-function-hash
         (:runtime-artifact-closed-plan-function-hash runtime-rule)
         :helper-function-hashes
         (:runtime-artifact-closed-function-hashes runtime-rule)
         :authority-binding
         {:adapter-record-hash (:record-hash adapter-record)
          :decision-record-ids (mapv :decision-id
                                     (:decision-records adapter-record))
          :action-record-ids (mapv :record-id
                                   (:action-records adapter-record))
          :source-principal (:source-principal adapter-record)
          :handler-principal (:handler-principal adapter-record)
          :provider-ids (:provider-ids adapter-record)
          :grant-ids (:grant-ids adapter-record)
          :io-write-active? (:io-write-active? adapter-record)
          :reference-interpreter? true
          :deployment-runtime? false}
         :validation validation-product
         :validation-hash validation-hash
         :invocation
         (select-keys invocation
                      [:artifact :function :function-hash
                       :runtime-artifact-hash :plan-id
                       :stdout-hash :invocation-count
                       :invocation-count-scope
                       :verification-replays-excluded? :status])
         :verification-replay
         {:artifact
          :gravity/p15-s23-runtime-closed-plan-verification-replay-record
          :function p15-s23-stage2-runtime-artifact-closed-plan-function
          :replay-count 1
          :count-scope :consumer-packet-authentication
          :included-in-authoritative-invocation-count? false
          :status :passed}
         :execution
         {:artifact (:artifact execution)
          :entrypoint (:entrypoint execution)
          :plan-id (:plan-id invocation)
          :stdout-hash (:stdout-hash invocation)
          :status (:status execution)}
         :primitive-boundary
         {:kind :clojure-seed-runtime-primitive-classification
          :pure-primitives '#{= < <= > >= count first get second}
          :proven-allocation-primitives '#{assoc conj str}
          :proven-allocation-literals #{:vector-literal :map-literal}
          :allocation-unproven-primitives '#{rest}
          :proven-allocation-operation-counts
          {:str 11 :vector 10 :map 2 :conj 1 :assoc 1}
          :proven-allocation-count 25
          :allocation-unproven-operation-counts {:rest 4}
          :allocation-unproven-count 4
          :effects #{:memory/allocate}
          :capabilities #{:memory/allocator}}
         :effect-projections
         {:closed-plan-reference
          {:scope :closed-plan-interpreter
           :direct-handler-function
           p15-s23-reference-runtime-handler-function
           :transitive-function-scope
           '#{p15-s23-runtime-evaluate-arguments
              p15-s23-runtime-evaluate-bindings
              p15-s23-runtime-evaluate-sequence
              p15-s23-runtime-evaluate-closed-instruction
              p15-s23-runtime-execute-closed-plan}
           :handled #{:io/write}
           :escaping #{:memory/allocate}
           :source-capabilities #{:memory/allocator :io/stdout}
           :handler-capabilities #{:test/fixture}
           :provider-id :gravity.reference/transcript-capture
           :handler-id :gravity.reference/transcript-string-handler
           :live-stdout? false}
          :legacy-println-helpers
          {:function-scope
           '#{p15-s23-runtime-println-value p15-s23-runtime-println-two}
           :handled #{}
           :escaping #{:memory/allocate :io/write}
           :provider-selection :unresolved
           :grant :unresolved
           :status :unresolved}
          :deployment-stdout
          {:effect :io/write
           :capability :io/stdout
           :provider-selection :unresolved
           :grant :unresolved
           :closed-plan-interpreter-excluded? true
           :status :unresolved}
          :build-effects #{}}
         :credit-boundary
         {:reference-runtime? true
          :deployment-runtime? false
          :checked-core-str-println-admission? true
          :checked-core-binary-integer-comparison-admission? true
          :checked-core-binary-integer-comparison-operations
          '#{= < <= > >=}
          :checked-core-admission-scope
          :authenticated-hosted-jvm-reference-interpreter
          :typed-fourth-authority
          :gravity/p15-s23-checked-core-authority-binding-v1
          :typed-fourth-authority-consumed-by-this-packet? false
          :verification-replay-policy-id
          p15-s23-checked-core-verification-replay-policy-id
          :target-lowering-credit? false
          :release-credit? false
          :c11-credit? false}
         :provenance-binding
         {:kind :actual-path-context
          :context-key :runtime-artifact-source-path
          :semantic-identity-input? false}
         :actual-path-binding actual-path-binding
         :migration-comparison
         {:clojure-stage2-executor? true
          :stage0-oracle? true
          :three-way-output-equivalent? true
          :closed-plan-variadic-println? true}
         :mir-derived? false
         :clojure-seed-boundary? true
         :self-hosted? false}]
    (assoc record :record-hash
           (p15-s23-reference-runtime-hash
            (dissoc record :actual-path-binding)))))

(defn p15-s23-closed-runtime-target-context
  [packet]
  {:plan-id (get-in packet [:plan :plan-id])
   :source-id (get-in packet [:plan :source :sha256])
   :entrypoint (get-in packet [:plan :entrypoint])
   :runtime-artifact-source-path
   (get-in packet [:stage2-runtime-rule :runtime-artifact-source-path])
   :runtime-adapter-record-hash
   (get-in packet [:runtime-contract-adapter-record :record-hash])
   :runtime-adapter-record (:runtime-contract-adapter-record packet)
   :runtime-decision-record-ids
   (mapv :decision-id
         (get-in packet [:runtime-contract-adapter-record :decision-records]))
   :runtime-action-record-ids
   (mapv :record-id
         (get-in packet [:runtime-contract-adapter-record :action-records]))
   :stdout-hash
   (str "sha256:"
        (sha256-hex
         (get-in packet [:closed-plan-execution-record :stdout])))})

(defn p15-s23-closed-runtime-target-semantic-record
  [record]
  (if (map? record)
    (dissoc record :actual-path-binding)
    record))