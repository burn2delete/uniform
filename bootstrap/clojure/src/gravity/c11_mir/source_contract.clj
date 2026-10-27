(ns gravity.c11-mir.source-contract
  "Required current-source function shapes only, not an admitted source rule.
   Compiled plan/function/builder/verifier identities are still required before
   the versioned public constructor can accept this source. Historical v1
   constants and public entrypoints remain untouched.")

(def v2-required-functions
  {'c11-build-target-independent-mir {:arity 1 :params ['checked-core]}
   'verify-c11-mir-module {:arity 1 :params ['mir-module]}
   'c11-build-linear-mir {:arity 3 :params ['checked-core 'nodes 'return-value-id]}
   'c11-build-conditional-mir {:arity 4 :params ['checked-core 'nodes 'if-node 'return-value-id]}
   'c11-build-operation {:arity 3 :params ['checked-core 'node 'block-id]}
   'c11-constant-payload {:arity 1 :params ['node]}
   'c11-build-data-flow
   {:arity 10 :params ['nodes 'if-id 'then-ids 'else-ids 'downstream-ids
                       'entry-id 'then-id 'else-id 'join-id 'result]}
   'c11-build-data-flow-for-operands
   {:arity 5 :params ['operands 'consumer 'consumer-block 'operand-index 'result]}
   'c11-build-definitions
   {:arity 11 :params ['checked-core 'nodes 'if-id 'then-ids 'else-ids 'downstream-ids
                       'entry-id 'then-id 'else-id 'join-id 'result]}
   'c11-build-values
   {:arity 6 :params ['checked-core 'nodes 'data-flow 'definitions 'terminator-uses 'result]}
   'c11-build-runtime-check-table {:arity 3 :params ['checked-core 'nodes 'result]}
   'c11-build-runtime-check-operation {:arity 3 :params ['checked-core 'node 'block-id]}
   'c11-runtime-check-operation-id {:arity 1 :params ['check]}
   'c11-runtime-check-token-id {:arity 1 :params ['check]}
   'c11-runtime-check-fact-id {:arity 2 :params ['check 'fact-kind]}
   'c11-runtime-check-fact-record {:arity 3 :params ['checked-core 'node 'fact-kind]}
   'c11-add-runtime-check-facts {:arity 4 :params ['checked-core 'nodes 'fact-kind 'result]}
   'c11-operation-operands {:arity 1 :params ['node]}
   'c11-build-fact-table-by-id
   {:arity 5 :params ['nodes 'source-table 'id-field 'generated-suffix 'result]}
   'c11-build-data-flow-for-node {:arity 4 :params ['node 'consumer-block 'check-block 'result]}
   'c11-build-profile-target-table {:arity 3 :params ['checked-core 'nodes 'result]}
   'c11-build-capability-proof-table {:arity 2 :params ['proofs 'result]}
   'c11-build-safety-proof-table {:arity 2 :params ['nodes 'result]}
   'c11-build-mir-pass-contract {:arity 0 :params []}
   'c11-nodes-before-id {:arity 3 :params ['nodes 'node-id 'result]}
   'c11-nodes-from-id {:arity 2 :params ['nodes 'node-id]}
   'c11-build-source-map {:arity 2 :params ['nodes 'result]}})

;; Reviewed compile/plan observations, not permission to skip fresh source
;; binding or the public constructor/verifier. There is no fallback to these
;; values when a live comparison fails. No legacy v1 constant is changed.
(def v2-source-contract
  {:interface :gravity/c11-mir
   :version 2
   :source-relative-path "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"
   :source-byte-count 567399
   :source-content-hash "sha256:c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e"
   :plan-semantic-hash "sha256:91334a8348ee6c56e8d9384ade2060af04b8b71e1aa9150386b7e5e5dd119f8e"
   :functions-semantic-hash "sha256:3d3719623d75bc5fb3831f86669a013c47256c81128d3bd2347e9dae7fbc0ce3"
   :builder-semantic-hash "sha256:4471ced7b19c9225ba6f7641c13a565eea26281413d8d3704e51968dbbcb6081"
   :verifier-semantic-hash "sha256:13a4cbc1f63e62728aa821a75e85626a4fd14b4d14d6017ac3d5ca47531e4079"
   :required-functions v2-required-functions})
