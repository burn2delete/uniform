(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-b2-c17-process-exit-code
  [value]
  (gravity.backend.c/process-exit-code value))

(defn p15-s23-b2-c17-proof-to-c-assumption-record
  [b1-record mir operations operation-records return-id return-value-name semantic-result]
  (gravity.backend.c/proof-to-c-assumption-record #'p15-s23-b2-c17-operation-proof-bindings #'p15-s23-b2-c17-discard-cast-records b1-record mir operations operation-records return-id return-value-name semantic-result))

(defn- p15-s23-b2-c17-assert-exact!
  [source-path expected actual missing-fact]
  (gravity.backend.c/assert-exact! #'p15-s23-c6c10-strict-first-mismatch #'p15-s23-c-backend-fail! source-path expected actual missing-fact))

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
        semantic-result
        (p15-s23-b2-c17-process-exit-code (:semantic-result preflight))
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
