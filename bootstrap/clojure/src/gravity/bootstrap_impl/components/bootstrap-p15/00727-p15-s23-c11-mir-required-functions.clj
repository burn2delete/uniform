

(def p15-s23-c11-mir-required-functions
  {'c11-build-target-independent-mir
   {:arity 1 :params ['checked-core]}
   'verify-c11-mir-module
   {:arity 1 :params ['mir-module]}
   'c11-build-linear-mir
   {:arity 3 :params ['checked-core 'nodes 'return-value-id]}
   'c11-build-conditional-mir
   {:arity 4 :params ['checked-core 'nodes 'if-node 'return-value-id]}
   'c11-build-operation
   {:arity 3 :params ['checked-core 'node 'block-id]}
   'c11-constant-payload
   {:arity 1 :params ['node]}
   'c11-build-data-flow
   {:arity 10
    :params ['nodes 'if-id 'then-ids 'else-ids 'downstream-ids
             'entry-id 'then-id 'else-id 'join-id 'result]}
   'c11-build-data-flow-for-operands
   {:arity 5
    :params ['operands 'consumer 'consumer-block 'operand-index 'result]}
   'c11-build-definitions
   {:arity 10
    :params ['nodes 'if-id 'then-ids 'else-ids 'downstream-ids
             'entry-id 'then-id 'else-id 'join-id 'result]}
   'c11-build-values
   {:arity 6
    :params ['checked-core 'nodes 'data-flow 'definitions
             'terminator-uses 'result]}
   'c11-build-runtime-check-table
   {:arity 3 :params ['checked-core 'nodes 'result]}
   'c11-build-runtime-check-operation
   {:arity 3 :params ['checked-core 'node 'block-id]}
   'c11-runtime-check-operation-id
   {:arity 1 :params ['check]}
   'c11-runtime-check-token-id
   {:arity 1 :params ['check]}
   'c11-runtime-check-fact-id
   {:arity 2 :params ['check 'fact-kind]}
   'c11-runtime-check-fact-record
   {:arity 3 :params ['checked-core 'node 'fact-kind]}
   'c11-add-runtime-check-facts
   {:arity 4 :params ['checked-core 'nodes 'fact-kind 'result]}
   'c11-operation-operands
   {:arity 1 :params ['node]}
   'c11-build-fact-table-by-id
   {:arity 5
    :params ['nodes 'source-table 'id-field 'generated-suffix 'result]}
   'c11-build-data-flow-for-node
   {:arity 4 :params ['node 'consumer-block 'check-block 'result]}
   'c11-build-profile-target-table
   {:arity 3 :params ['checked-core 'nodes 'result]}
   'c11-build-capability-proof-table
   {:arity 2 :params ['proofs 'result]}
   'c11-build-safety-proof-table
   {:arity 2 :params ['nodes 'result]}
   'c11-build-mir-pass-contract
   {:arity 0 :params []}
   'c11-nodes-before-id
   {:arity 3 :params ['nodes 'node-id 'result]}
   'c11-nodes-from-id
   {:arity 2 :params ['nodes 'node-id]}
   'c11-build-source-map
   {:arity 2 :params ['nodes 'result]}})

(def p15-s23-c11-mir-max-carrier-nodes 16384)
(def p15-s23-c11-mir-max-final-artifact-carrier-nodes 32768)
(def p15-s23-c11-mir-max-carrier-depth 256)
(def p15-s23-c11-mir-max-blocks 4)

(def p15-s23-c11-mir-allowed-opcodes
  #{:constant :local :local-binding :truthiness :sequence
    :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
    :conditional-join :lexical-scope :call :function-boundary
    :runtime-check})

(def p15-s23-c11-mir-allowed-source-operations
  #{:literal :implicit-nil :quote :local :let-binding :truthy
    :integer-eq :integer-lt :integer-lte :integer-gt :integer-gte
    :do :if :let :str :println :function :runtime-check})

(defn p15-s23-c11-mir-digest
  [value]
  (str "sha256:"
       (sha256-hex
        (pr-str (c-backend-canonical-value value)))))

(defn p15-s23-c11-mir-safe-source-path
  [source-path]
  (if (and (string? source-path)
           (<= (.length ^String source-path) 4096)
           (try
             (= :valid
                (:status
                 (p15-s23-closed-core-bounded-utf8-count
                  source-path 4096)))
             (catch InterruptedException interrupted
               (.interrupt (Thread/currentThread))
               (throw interrupted))
             (catch Exception _ false)))
    source-path
    "<c11-mir>"))

(def p15-s23-c11-mir-diagnostic-fact-keys
  #{:missing-fact :conditional-count :requested-target :source-target
    :module-id :function-id :block-id :operation-id :op-id :value-id
    :source-operation :checked-core-artifact-id :mir-id :syntax-id :origin-id
    :core-node-id :c2-form-id :lowering-rule :expected-type :actual-type
    :effect :capability :provider :grant :owner-id :borrow-id :region-id
    :resource-id :control-path :specialized-safe-rule :safety-mode
    :proof-id :runtime-check :unsafe-audit :boundary-identity-reason
    :expected-source-bytes :observed-source-bytes
    :expected-source-content-hash :observed-source-content-hash
    :observed-plan-semantic-hash :observed-functions-semantic-hash
    :observed-builder-semantic-hash :construction-mode
    :checked-core-artifact-kind :checked-core-context-kind
    :checked-core-ingress-mode :checked-core-semantic-authority
    :checked-core-verification-status :checked-core-ingress-schema-version
    :producer-diagnostic-id
    :observed-nodes :observed-depth
    :observed-width :maximum-width :observed-total-scalar-bytes
    :maximum-total-scalar-bytes :maximum-nodes :maximum-depth
    :runtime-contract-definition :bounded-reason})