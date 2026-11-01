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

(def flat-bool-required-functions
  (merge v2-required-functions
    {'sh12-flat-bool-sequence-view {:arity 1 :params ['core]}
     'sh12-flat-bool-sequence-topology-valid? {:arity 2 :params ['core 'view]}
     'sh12-flat-bool-sequence-origin-valid? {:arity 3 :params ['core 'node 'literal?]}
     'sh12-flat-bool-sequence-node-facts-valid? {:arity 4 :params ['core 'node 'literal? 'function?]}
     'sh12-flat-bool-sequence-original-facts-valid? {:arity 1 :params ['core]}
     'sh12-flat-bool-sequence-original-graphs-valid? {:arity 1 :params ['core]}
     'sh12-flat-bool-sequence-original-lineage-valid? {:arity 1 :params ['core]}
     'sh12-verify-core-mir-preservation {:arity 2 :params ['checked-core 'mir]}
     'sh12-flat-bool-sequence-mir-policy {:arity 0 :params []}
     'sh12-flat-bool-sequence-report {:arity 2 :params ['checked-core 'report]}
     'sh12-flat-bool-sequence-core-preflight {:arity 1 :params ['core]}
     'sh12-build-flat-bool-sequence-mir {:arity 1 :params ['checked-core]}
     'sh12-verify-flat-bool-sequence-mir {:arity 2 :params ['checked-core 'mir]}}))

;; Observed and independently reviewed from the corrected source. Each owner
;; must still compile the same stable source bytes and compare all identities.
;; This does not replace Core/source authentication or a fresh verifier call.
(def flat-bool-source-contract
  {:interface :gravity/c11-mir :version 3
   :semantic-family :closed-flat-bool-literal-sequence
   :source-relative-path "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"
   :source-byte-count 597279
   :source-content-hash "sha256:4ca2bf8c551c9b0e7aefbeab84e7f7c9b5742bdeee10f68e74aff3470d35b3dd"
   :plan-semantic-hash "sha256:36c1a98c8753852754147f01bdd310b952391fea406125093d20d4f242b8199a"
   :functions-semantic-hash "sha256:58b6002f47dbdecc9a8b202ea7dbfe97433b6629d1a6418bc01b7996ababee71"
   :builder-function 'sh12-build-flat-bool-sequence-mir
   :builder-semantic-hash "sha256:f24491eab6a0f88c4c7b638af5bbcf1d6c371b66e509657afdf8b2bf1f5bb93a"
   :verifier-function 'sh12-verify-flat-bool-sequence-mir
   :verifier-semantic-hash "sha256:7562fc08983f1341bcd8d4d510fe361902f36a7f1cacd917dce3161a3fb3b90f"
   :required-functions flat-bool-required-functions})
