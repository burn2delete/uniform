

(def p15-s23-c11-mir-block-keys
  #{:artifact :block-id :arguments :instructions :terminator
    :predecessors :successors :dominators :data-flow :source})

(def p15-s23-c11-mir-operation-keys
  #{:artifact :op-id :opcode :source-operation :operands :result
    :result-kind :constant-payload :type :effects :capabilities :ordering :source
    :profile :facts :domain-anchor :block-id :verifier-status})

(def p15-s23-c11-mir-terminator-keys
  #{:artifact :terminator-id :kind :operands :successors :effects
    :ordering :source :profile :facts :verifier-status})

(defn p15-s23-c11-mir-validate-module-envelope!
  [source-path checked-core mir]
  (let [entrypoint (:entrypoint checked-core)
        function (get-in mir [:functions entrypoint])
        root-id (first (:root-node-ids checked-core))
        root-node (first (filter #(= root-id (:node-id %))
                                 (:core-nodes checked-core)))
        return-id (last (:operands root-node))
        return-node (first (filter #(= return-id (:node-id %))
                                   (:core-nodes checked-core)))]
    (p15-s23-c11-mir-require!
     (and (map? mir)
          (= p15-s23-c11-mir-module-keys (set (keys mir)))
          (= :gravity/mir-module (:artifact mir))
          (= 1 (:schema-version mir))
          (= (:artifact-id checked-core) (:source-core mir))
          (= (get-in checked-core [:source-core-input :module])
             (:module-id mir))
          (= (:profile checked-core) (:profile mir))
          (= (:source-target checked-core) (:source-target mir))
          (= (get-in checked-core
                     [:target-request-metadata :requested-target])
             (:target-request mir))
          (= (:target-request-metadata checked-core)
             (:target-request-metadata mir))
          (map? (:functions mir))
          (= #{entrypoint} (set (keys (:functions mir))))
          (map? (:globals mir))
          (= {} (:globals mir))
          (map? (:domain-anchors mir))
          (= {} (:domain-anchors mir))
          (vector? (:diagnostics mir))
          (= [] (:diagnostics mir))
          (= :pending (:verification-status mir))
          (true? (:target-independent? mir))
          (true? (:clojure-seed-boundary? mir))
          (false? (:self-hosted? mir)))
     "C11-MODULE" source-path mir :authenticated-c11-module-envelope)
    (p15-s23-c11-mir-require!
     (and (map? root-node)
          (map? return-node)
          (map? function)
          (= p15-s23-c11-mir-function-keys (set (keys function)))
          (= :gravity/mir-function (:artifact function))
          (= root-id (:fn-id function))
          (= entrypoint (:name function))
          (vector? (:params function))
          (= [] (:params function))
          (= (:type return-node) (:returns function))
          (= (get-in checked-core
                     [:source-core-input :declared-effects])
             (:latent-effects function))
          (= (get-in checked-core
                     [:source-core-input :declared-capabilities])
             (:capabilities function))
          (= (:source root-node) (:source function))
          (= {:type-fact-id
              (get-in checked-core [:type-facts root-id :fact-id])
              :effect-fact-id
              (get-in checked-core [:effect-facts root-id :fact-id])
              :ownership-fact-id
              (get-in checked-core [:ownership-facts root-id :fact-id])
              :safety-outcome-id (str root-id ":safety-outcome")
              :profile-target-fact-id
              (str root-id ":profile-target-fact")}
             (:facts function))
          (= {:checked-core-artifact-id (:artifact-id checked-core)
              :core-node-id root-id}
             (:provenance function)))
     "C11-MODULE" source-path function :authenticated-c11-function-envelope)
    (p15-s23-c11-mir-require!
     (= {:checked-core-artifact-id (:artifact-id checked-core)
         :checked-core-mapping-id (:mapping-id checked-core)
         :checked-core-provenance-binding-id
         (:provenance-binding-id checked-core)
         :checked-core-origin-closure-binding-id
         (:provenance-binding-id checked-core)}
        (:provenance mir))
     "C11-ORIGIN" source-path mir :checked-core-provenance-binding)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     {:checked-core-artifact-id (:artifact-id checked-core)
      :checked-core-mapping-id (:mapping-id checked-core)
      :checked-core-provenance-binding-id
      (:provenance-binding-id checked-core)
      :checked-core-origin-closure-binding-id
      (:provenance-binding-id checked-core)}
     (:provenance mir)
     :type-sensitive-checked-core-provenance-binding)
    (p15-s23-c11-mir-require!
     (= {:owner :gravity-source
         :function :c11-build-target-independent-mir
         :status :constructed}
        (:construction mir))
     "C11-VERIFY" source-path mir :gravity-owned-mir-construction)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     {:owner :gravity-source
      :function :c11-build-target-independent-mir
      :status :constructed}
     (:construction mir) :type-sensitive-gravity-owned-mir-construction)
    (p15-s23-c11-mir-require!
     (and (= (p15-s23-c11-mir-pass-contract-record)
             (:pass-contract mir))
          (= {:artifact :gravity/build-mir-pass-execution-record
              :pass-id :c11-build-mir-bounded-slice
              :pass-contract-hash :pending-source-binding
              :input-artifact-id (:artifact-id checked-core)
              :output-content-id :pending-independent-verifier
              :verifier-report-id :pending-independent-verifier
              :verifier-report-hash :pending-independent-verifier
              :diagnostics []
              :status :constructed-unverified
              :record-id :pending-independent-verifier}
             (:pass-execution-record mir)))
     "C11-VERIFY" source-path mir
     :gravity-owned-build-mir-pass-contract-and-pending-execution)
    (p15-s23-c11-mir-require-strict-structure!
     source-path (p15-s23-c11-mir-pass-contract-record)
     (:pass-contract mir) :type-sensitive-build-mir-pass-contract)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     {:artifact :gravity/build-mir-pass-execution-record
      :pass-id :c11-build-mir-bounded-slice
      :pass-contract-hash :pending-source-binding
      :input-artifact-id (:artifact-id checked-core)
      :output-content-id :pending-independent-verifier
      :verifier-report-id :pending-independent-verifier
      :verifier-report-hash :pending-independent-verifier
      :diagnostics []
      :status :constructed-unverified
      :record-id :pending-independent-verifier}
     (:pass-execution-record mir)
     :type-sensitive-pending-build-mir-pass-execution)
    (p15-s23-c11-mir-require!
     (and (= {:input-kind :gravity/mir
              :requires-verifier-status :passed
              :status :pending-c11-verifier
              :backend-credit? false}
             (:b1-preflight mir))
          (= {:operation-set
              [:literal :implicit-nil :quote :local :let-binding :truthy
               :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
               :do :if :let :str :println :function
               :runtime-check]
              :maximum-conditionals 1
              :maximum-module-carrier-nodes
              p15-s23-c11-mir-max-carrier-nodes
              :maximum-final-artifact-carrier-nodes
              p15-s23-c11-mir-max-final-artifact-carrier-nodes
              :maximum-carrier-depth 256
              :whole-c11? false
              :domain-ir-credit? false
              :optimization-credit? false
              :target-lowering-credit? false
              :backend-credit? false
              :llvm-credit? false
              :release-credit? false
              :whole-language? false
              :self-hosted? false}
             (:scope mir)))
     "C11-VERIFY" source-path mir :bounded-c11-and-b1-scope)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     {:input-kind :gravity/mir
      :requires-verifier-status :passed
      :status :pending-c11-verifier
      :backend-credit? false}
     (:b1-preflight mir) :type-sensitive-pending-b1-preflight)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     {:operation-set
      [:literal :implicit-nil :quote :local :let-binding :truthy
       :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
       :do :if :let :str :println :function
       :runtime-check]
      :maximum-conditionals 1
      :maximum-module-carrier-nodes p15-s23-c11-mir-max-carrier-nodes
      :maximum-final-artifact-carrier-nodes
      p15-s23-c11-mir-max-final-artifact-carrier-nodes
      :maximum-carrier-depth 256
      :whole-c11? false
      :domain-ir-credit? false
      :optimization-credit? false
      :target-lowering-credit? false
      :backend-credit? false
      :llvm-credit? false
      :release-credit? false
      :whole-language? false
      :self-hosted? false}
     (:scope mir) :type-sensitive-bounded-c11-and-b1-scope)
    {:function function
     :root-node root-node
     :return-node return-node
     :return-id return-id}))