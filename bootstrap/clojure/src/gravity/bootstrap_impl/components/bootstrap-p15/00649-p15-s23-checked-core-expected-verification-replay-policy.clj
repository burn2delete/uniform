

(def p15-s23-checked-core-expected-verification-replay-policy
  {:artifact :gravity/p15-s23-checked-core-verification-replay-policy
   :schema-version 1
   :policy-id p15-s23-checked-core-verification-replay-policy-id
   :audit-policy-id
   :gravity.reference/checked-core-verification-replay-audit-policy
   :status :complete-for-authenticated-hosted-jvm-reference-interpreter-slice
   :profile :hosted
   :target :jvm
   :verifier-principal :gravity.bootstrap/checked-core-verifier
   :runtime-principal 'gravity.bootstrap.p15-s23.runtime
   :handler-principal :gravity.bootstrap/verification-transcript-harness
   :invocation-contract
   {:caller-principal :gravity.bootstrap/checked-core-verifier
    :callee-principal 'gravity.bootstrap.p15-s23.runtime
    :invocation-handle-id
    :gravity.reference/checked-core-verification-runtime-handle
    :authorization-source :exact-verifier-provider-grants
    :scope :pinned-checked-core-artifact-replay
    :authority-transfer? false}
   :provider-contracts
   {:verifier-managed-allocation
    {:provider-id :gravity.reference/jvm-managed-allocator
     :effect :memory/allocate
     :capability :memory/allocator
     :required-when :every-verification-replay}
    :verifier-transcript-fixture
    {:provider-id :gravity.reference/transcript-capture
     :effect :io/write
     :capability :test/fixture
     :required-when :structural-println-present}
    :managed-allocation
    {:provider-id :gravity.reference/jvm-managed-allocator
     :effect :memory/allocate
     :capability :memory/allocator
     :required-when :every-verification-replay}
    :transcript-capture
    {:provider-id :gravity.reference/transcript-capture
     :effect :io/write
     :capability :io/stdout
     :handler-capability :test/fixture
     :required-when :structural-println-present}}
   :grant-contracts
   {:verifier-managed-allocation
    {:grant-id
     :gravity.reference/verification-grant-verifier-managed-allocation
     :principal :gravity.bootstrap/checked-core-verifier
     :provider-id :gravity.reference/jvm-managed-allocator
     :effect :memory/allocate
     :capability :memory/allocator
     :scope :pinned-checked-core-artifact-replay}
    :verifier-transcript-fixture
    {:grant-id
     :gravity.reference/verification-grant-verifier-test-fixture
     :principal :gravity.bootstrap/checked-core-verifier
     :provider-id :gravity.reference/transcript-capture
     :effect :io/write
     :capability :test/fixture
     :scope :verification-transcript}
    :managed-allocation
    {:grant-id
     :gravity.reference/verification-grant-managed-allocation
     :principal 'gravity.bootstrap.p15-s23.runtime
     :provider-id :gravity.reference/jvm-managed-allocator
     :effect :memory/allocate
     :capability :memory/allocator
     :scope :pinned-verification-runtime-plan}
    :transcript-capture
    {:grant-id
     :gravity.reference/verification-grant-transcript-capture
     :principal 'gravity.bootstrap.p15-s23.runtime
     :provider-id :gravity.reference/transcript-capture
     :effect :io/write
     :capability :io/stdout
     :scope :verification-transcript}
    :fixture
    {:grant-id :gravity.reference/verification-grant-test-fixture
     :principal :gravity.bootstrap/verification-transcript-harness
     :provider-id :gravity.reference/transcript-capture
     :effect :io/write
     :capability :test/fixture
     :scope :verification-transcript}}
   :binding-inputs
   #{:checked-core-artifact-id :mapping-id :provenance-binding-id
     :source-content-hash :plan-id :module
     :runtime-source-content-hash :runtime-artifact-hash
     :runtime-contract-definition-hash :runtime-derived-facts-hash
     :runtime-function :runtime-function-hash :verification-policy-id
     :verification-policy-hash :verification-audit-policy-hash
     :structural-operation-set :concrete-core-operation-set
     :reissued-program-authority-record-id
     :reissued-program-authority-evidence-id}
   :plan-requirement-source :authenticated-structural-operation-set
   :host-service-boundary :typed-r1-delegated-adapters
   :deny-by-default? true
   :redaction-policy :hash-host-class-and-message
   :audit-record-contract
   {:invocation-decision :required-before-runtime-call
    :provider-decisions :exactly-required-structural-services
    :pre-call-actions :none-before-runtime-invocation
    :action-records :exactly-invoked-or-structurally-not-invoked
    :failure-diagnostics
    {:gate-owned #{"R1-FAILURE" "R4-EXCEPTION" "R11-GRANT"}
     :preserved-runtime-projection
     :bounded-authenticated-runtime-diagnostic-projection}
    :raw-host-message? false}
   :phase :verification
   :lifetime :single-verification-replay
   :reference-invocation :single-verification-replay
   :package :gravity/bootstrap
   :deployment :verification-harness-only
   :single-replay? true
   :authoritative-invocation? false
   :excluded-from-authoritative-invocation-count? true
   :result-producing? true
   :external-io-delivery :in-memory-reference-transcript
   :live-external-io? false
   :program-authority-consumed? false
   :program-grants-consumed? false
   :delegation :none
   :authority-widening? false})

(def p15-s23-checked-core-verification-replay-audit-policy-keys
  #{:artifact :schema-version :policy-id :status :principals
    :decision-record-fields :action-record-fields :failure-record-fields
    :failure-diagnostics :redaction-policy :raw-host-message?
    :deny-by-default? :phase :lifetime :package :deployment
    :live-external-io? :delegation :authority-widening?})

(def p15-s23-checked-core-expected-verification-replay-audit-policy
  {:artifact
   :gravity/p15-s23-checked-core-verification-replay-audit-policy
   :schema-version 1
   :policy-id p15-s23-checked-core-verification-replay-audit-policy-id
   :status :complete-for-authenticated-hosted-jvm-reference-interpreter-slice
   :principals
   {:verifier :gravity.bootstrap/checked-core-verifier
    :runtime 'gravity.bootstrap.p15-s23.runtime
    :handler :gravity.bootstrap/verification-transcript-harness}
   :decision-record-fields
   #{:decision-id :kind :action-id :principal-id :effect :capability
     :provider-id :provider-selection-record-id :grant-id :grant-record-id
     :replay-authority-record-id :phase :lifetime :policy-id
     :verification-policy-hash :audit-policy-id
     :verification-audit-policy-hash :runtime-contract-definition-hash
     :runtime-derived-facts-hash :runtime-function :runtime-function-hash
     :scope :plan-id :source-content-hash :checked-core-artifact-id
     :runtime-artifact-hash :profile :target :runtime-family :service-id
     :module :package :deployment :source-span :generated-origin-edge
     :delegated-handle-id :decision :result :reason :redaction
     :redaction-policy :redaction-status :audit-status
     :diagnostic :missing-fact}
   :action-record-fields
   #{:record-id :kind :action-id :principal-id :effect :capability
     :provider-id :provider-selection-record-id :grant-id :grant-record-id
     :replay-authority-record-id :phase :lifetime :policy-id
     :verification-policy-hash :audit-policy-id
     :verification-audit-policy-hash :runtime-contract-definition-hash
     :runtime-derived-facts-hash :runtime-function :runtime-function-hash
     :scope :plan-id :source-content-hash :checked-core-artifact-id
     :runtime-artifact-hash :action-started? :action-status
     :result-committed? :output-committed? :diagnostic :redaction
     :profile :target :runtime-family :service-id :module :package
     :deployment :source-span :generated-origin-edge :delegated-handle-id
     :redaction-policy :redaction-status :audit-status
     :missing-fact :reason}
   :failure-record-fields
   #{:failure-record-id :diagnostic :missing-fact :decision-record :action-record
     :result-committed? :output-committed? :redaction :redaction-policy
     :redaction-status :audit-status :remediation :profile :target
     :runtime-family :service-id :effect :capability :provider-id
     :runtime-function :module :package :delegated-handle-id
     :host-runtime :host-symbol :host-package :gravity-type :adapter-id
     :missing-policy :runtime-diagnostic-projection}
   :failure-diagnostics
   {:gate-owned #{"R1-FAILURE" "R4-EXCEPTION" "R11-GRANT"}
    :preserved-runtime-projection
    :bounded-authenticated-runtime-diagnostic-projection}
   :redaction-policy :hash-host-class-and-message
   :raw-host-message? false
   :deny-by-default? true
   :phase :verification
   :lifetime :single-verification-replay
   :package :gravity/bootstrap
   :deployment :verification-harness-only
   :live-external-io? false
   :delegation :none
   :authority-widening? false})

(def p15-s23-reference-runtime-max-contract-nodes 16384)
(def p15-s23-reference-runtime-max-instruction-depth 256)
(def p15-s23-reference-runtime-max-contract-depth 256)
(def p15-s23-reference-runtime-max-closed-plan-carrier-depth 512)
(def p15-s23-reference-runtime-max-rule-nodes 65536)
(def p15-s23-reference-runtime-max-packet-nodes 131072)
(def p15-s23-reference-runtime-max-context-source-bytes (* 1024 1024))
(def p15-s23-reference-runtime-max-scalar-bytes (* 1024 1024))
(def p15-s23-reference-runtime-max-total-scalar-bytes (* 32 1024 1024))
(def p15-s23-reference-runtime-max-integer-bits 4096)
(def p15-s23-reference-runtime-max-collection-width 16384)

(def p15-s23-reference-runtime-supported-number-class-names
  #{"java.lang.Byte" "java.lang.Short" "java.lang.Integer"
    "java.lang.Long" "java.math.BigInteger" "clojure.lang.BigInt"
    "clojure.lang.Ratio" "java.math.BigDecimal" "java.lang.Float"
    "java.lang.Double"})

(def p15-s23-reference-runtime-supported-collection-class-names
  #{"clojure.lang.PersistentArrayMap" "clojure.lang.PersistentHashMap"
    "clojure.lang.PersistentTreeMap" "clojure.lang.PersistentVector"
    "clojure.lang.PersistentHashSet" "clojure.lang.PersistentTreeSet"
    "clojure.lang.PersistentList" "clojure.lang.PersistentList$EmptyList"
    "clojure.lang.Cons"})