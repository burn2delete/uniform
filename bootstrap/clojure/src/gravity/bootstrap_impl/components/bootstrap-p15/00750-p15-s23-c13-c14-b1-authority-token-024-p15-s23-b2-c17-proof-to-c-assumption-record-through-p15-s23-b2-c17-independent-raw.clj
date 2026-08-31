(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-b2-c17-proof-to-c-assumption-record
  [b1-record mir operations operation-records return-id
   return-value-name semantic-result]
  (let [certificates (get-in b1-record [:proofs :certificates])
        base-entries
        [{:assumption :signed-i64-carrier
          :evidence :authenticated-mir-scalar-type-and-constant-closure
          :c-site :all-generated-int64-t-initializers
          :outcome :proven-safe}
         {:assumption :explicit-discarded-value-conversions
          :evidence :initialized-generated-scalar-liveness
          :c-site :one-void-conversion-per-generated-local
          :outcome :proven-safe}
         {:assumption :final-exit-code-narrowing
          :evidence :independent-pure-mir-evaluation-in-0-to-255
          :c-site (str "return (int)" return-value-name ";")
          :outcome :proven-safe}]
        equality-entries
        (if (some #(= :integer-eq (:opcode %)) operations)
          (conj
           base-entries
           {:assumption :defined-signed-i64-equality
            :evidence
            :authenticated-two-integer-operands-and-boolean-result
            :c-site :generated-integer-eq-expressions
            :outcome :proven-safe})
          base-entries)
        entries
        (if (some #(contains?
                    #{:integer-lt :integer-lte :integer-gt :integer-gte}
                    (:opcode %))
                  operations)
          (conj
           equality-entries
           {:assumption :defined-signed-i64-ordering
            :evidence
            :authenticated-ordered-two-integer-operands-and-boolean-result
            :c-site :generated-integer-ordering-expressions
            :outcome :proven-safe})
          equality-entries)]
    {:artifact :gravity/b2-proof-to-c-assumption-map
     :proof-certificate-binding
     {:content-id (:content-id certificates)
      :entry-count (:entry-count certificates)}
     :operation-bindings
     (p15-s23-b2-c17-operation-proof-bindings
      operations operation-records)
     :entries entries
     :generated-c-casts
     (conj
      (p15-s23-b2-c17-discard-cast-records
       operations operation-records)
      {:kind :integer-narrowing
       :site (str "return (int)" return-value-name ";")
       :from :int64-t :to :int
       :proof :independent-pure-mir-evaluation-in-0-to-255
       :proof-binding
       {:kind :independent-bounded-mir-evaluation
        :mir-module-id (:module-id mir)
        :return-operation-id return-id
        :semantic-result semantic-result
        :permitted-result-range [0 255]
        :b1-artifact-id (:artifact-id b1-record)
        :proof-table-content-id (:content-id certificates)}})
     :all-casts-proof-authorized? true
     :implicit-undefined-behavior-permitted? false}))

(defn- p15-s23-b2-c17-assert-exact!
  [source-path expected actual missing-fact]
  (when-let [mismatch
             (p15-s23-c6c10-strict-first-mismatch
              source-path expected actual [])]
    (p15-s23-c-backend-fail!
     "B2-MANIFEST" source-path actual
     {:missing-fact missing-fact
      :bounded-reason
      (select-keys mismatch
                   [:path :expected-kind :actual-kind
                    :expected-count :actual-count
                    :missing-key-count :unexpected-key-count])})))

(defn- p15-s23-b2-c17-independent-raw
  [source-path c11-artifact c-packet]
  (let [b1-record (:b1 c-packet)
        payload (:bounded-lowering-payload b1-record)
        mir (:mir payload)
        preflight
        (p15-s23-c13-c14-b1-c-preflight! source-path c11-artifact)
        expected-mir (:mir preflight)
        operations (:operations preflight)
        block-order (:block-order preflight)
        operation-order (mapv :op-id operations)
        operation-index (zipmap operation-order (range))
        operation-records
        (mapv #(p15-s23-b2-c17-operation-record
                % operations operation-index)
              operations)
        function (:function preflight)
        return-id (p15-s23-b2-c17-return-id function block-order)
        return-value-name
        (p15-s23-b2-c17-value-name operation-index return-id)
        source-text
        (p15-s23-b2-c17-source-text
         operation-records return-value-name)
        semantic-result (:semantic-result preflight)
        source-debug-map
        (p15-s23-b2-c17-source-debug-map-record
         b1-record operations operation-records return-id
         return-value-name)]
    (p15-s23-b2-c17-assert-exact!
     source-path expected-mir mir :fresh-c11-to-sealed-b1-mir-parity)
    (when-not
     (and (= operation-order (:operation-order payload))
          (= (:artifact-id c11-artifact) (:c11-artifact-id payload))
          (= (:artifact-id c11-artifact)
             (get-in c-packet [:c11 :artifact-id]))
          (= (:mir-id c11-artifact)
             (get-in c-packet [:c11 :mir-id]))
          (= (:artifact-id b1-record)
             (get-in c-packet [:b1 :artifact-id]))
          (= (:semantic-id b1-record)
             (get-in c-packet [:b1 :semantic-id])))
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path b1-record
       {:missing-fact :fresh-context-bound-b1-to-b2-input-closure}))
    {:artifact :gravity/b2-bounded-hosted-c17
     :schema-version 1
     :status :constructed-unverified
     :policy p15-s23-b2-c17-policy-record
     :input-bindings
     {:b1-semantic-id (:semantic-id b1-record)
      :b1-artifact-id (:artifact-id b1-record)
      :b1-actual-path-binding-id (:actual-path-binding-id b1-record)
      :c11-artifact-id (:c11-artifact-id payload)
      :c13-artifact-id (:c13-artifact-id payload)
      :c13-semantic-id (:c13-semantic-id payload)
      :mir-module-id (:module-id mir)
      :b1-source-rule (:source-rule b1-record)}
     :backend-manifest (:backend-manifest b1-record)
     :c-build-manifest
     (p15-s23-b2-c17-build-manifest-record
      p15-s23-b2-c17-dialect-selection-record)
     :source-debug-map source-debug-map
     :dialect-selection p15-s23-b2-c17-dialect-selection-record
     :abi-layout-manifest
     (p15-s23-b2-c17-abi-layout-record b1-record)
     :runtime-helper-manifest
     (p15-s23-b2-c17-runtime-helper-record b1-record)
     :proof-to-c-assumption-map
     (p15-s23-b2-c17-proof-to-c-assumption-record
      b1-record mir operations operation-records return-id
      return-value-name semantic-result)
     :block-order block-order
     :operation-index operation-index
     :operation-records operation-records
     :header-file
     {:artifact :gravity/b2-c-header-file
      :path "program.h" :dialect :c17
      :content p15-s23-b2-c17-header-text
      :generated? true}
     :source-file
     {:artifact :gravity/b2-c-source-file
      :path "program.c" :dialect :c17 :content source-text
      :includes ["stdint.h" "program.h"]
      :generated? true}
     :semantic-result semantic-result
     :expected-exit-code semantic-result
     :verified-input-closure
     {:status :structurally-closed-after-root-contextual-gate
      :sealed-b1-record
      :fresh-contextual-strict-equality-required-before-invocation
      :raw-source-validator :not-a-standalone-seal-verifier
      :invocation-authority :clojure-seed-root-capability-gate
      :sealed-b1-semantic-id (:semantic-id b1-record)
      :sealed-b1-artifact-id (:artifact-id b1-record)
      :sealed-b1-actual-path-binding-id
      (:actual-path-binding-id b1-record)
      :actual-path-provenance (:actual-path-provenance b1-record)
      :mir-source :bounded-lowering-payload-only
      :source-core (:source-core mir)
      :mir-module-id (:module-id mir)
      :pass-execution-record-id
      (get-in mir [:pass-execution-record :record-id])
      :operation-count (count operations)
      :conditional-count
      (count (filter #(= :conditional-join (:opcode %)) operations))
      :full-dfg-definition-count
      (count (get-in mir [:data-flow-graph :definitions]))
      :full-dfg-value-count
      (count (get-in mir [:data-flow-graph :values]))
      :runtime-check-count (count (:runtime-check-table mir))
      :effect-count (count (:effect-table mir))
      :capability-count (count (:capability-table mir))}
     :diagnostics []
     :clojure-seed-boundary? true
     :whole-b2? false
     :public? false
     :release? false
     :self-hosted? false})))
