; Semantic decomposition of committed HEAD reader line 138453.
; Loaded into gravity.bootstrap; intentionally no ns form.

(clojure.core/defn
 semantic-late-p15-s23-closed-runtime-target-record-authentic?-record-contract
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
   (= :gravity/p15-s23-runtime-closed-plan-target-record (:artifact record))
   (=
    #{:execution
      :runtime-contract-binding
      :validation-hash
      :actual-path-binding
      :verification-replay
      :effect-projections
      :executor-function-hash
      :helper-function-hashes
      :invocation
      :self-hosted?
      :credit-boundary
      :record-hash
      :migration-comparison
      :executor-function
      :mir-derived?
      :artifact
      :validation
      :provenance-binding
      :runtime-artifact-hash
      :runtime-artifact-source-content-hash
      :authority-binding
      :primitive-boundary
      :clojure-seed-boundary?}
    (set (keys record)))
   (= expected-record-hash (:record-hash record))
   (= expected-actual-path-binding (:actual-path-binding record))
   (=
    p15-s23-stage2-runtime-artifact-expected-source-content-hash
    (:runtime-artifact-source-content-hash record))
   (= p15-s23-stage2-runtime-artifact-expected-artifact-hash (:runtime-artifact-hash record))
   (=
    {:contract-definition-hash p15-s23-reference-runtime-expected-contract-definition-hash,
     :derived-contract-facts-hash p15-s23-reference-runtime-expected-derived-facts-hash,
     :function-hashes p15-s23-reference-runtime-expected-function-hashes,
     :providers p15-s23-reference-runtime-source-provider-selections,
     :validation
     {:proven-allocation-count 25,
      :contract-definition-count (count p15-s23-reference-runtime-contract-definition-names),
      :allocation-unproven-count 4,
      :operation-count 328,
      :function-count 11,
      :status :complete,
      :escaping-io-functions '#{p15-s23-runtime-println-value p15-s23-runtime-println-two},
      :artifact :gravity/p15-s23-reference-runtime-contract-validation,
      :handler-scope
      '#{p15-s23-runtime-execute-closed-plan
         p15-s23-runtime-evaluate-bindings
         p15-s23-runtime-evaluate-arguments
         p15-s23-runtime-evaluate-closed-instruction
         p15-s23-runtime-evaluate-sequence}},
     :generic-emitter-effect-summary-credited? false}
    contract-binding)
   (= p15-s23-stage2-runtime-artifact-closed-plan-function (:executor-function record))
   (=
    (get
     p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
     p15-s23-stage2-runtime-artifact-closed-plan-function)
    (:executor-function-hash record))
   (=
    p15-s23-stage2-runtime-artifact-expected-closed-function-hashes
    (:helper-function-hashes record))
   (=
    {:reference-interpreter? true,
     :deployment-runtime? false,
     :adapter-record-hash (:runtime-adapter-record-hash context),
     :source-principal 'gravity.bootstrap.p15-s23.runtime,
     :io-write-active? io-write-active?,
     :decision-record-ids (:runtime-decision-record-ids context),
     :provider-ids expected-provider-ids,
     :handler-principal :gravity.bootstrap/reference-harness,
     :grant-ids expected-grant-ids,
     :action-record-ids (:runtime-action-record-ids context)}
    authority-binding)
   (every?
    digest?
    (concat
     [(:adapter-record-hash authority-binding)]
     (:decision-record-ids authority-binding)
     (:action-record-ids authority-binding)))
   (= :gravity/p15-s23-runtime-closed-plan-validation-record (:artifact validation))
   (=
    #{:observed-operation-set
      :maximum-depth
      :plan-id
      :operation-set
      :status
      :entrypoint
      :artifact
      :maximum-nodes}
    (set (keys validation)))
   (= :complete (:status validation))
   (= expected-validation-hash (:validation-hash record))
   (= p15-s23-closed-runtime-operations (:operation-set validation))
   (= io-write-active? (contains? (:observed-operation-set validation) :println))
   (= p15-s23-closed-runtime-max-depth (:maximum-depth validation))
   (= p15-s23-closed-runtime-max-nodes (:maximum-nodes validation)))))
