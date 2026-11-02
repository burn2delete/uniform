(ns gravity.syntax
  "Stage0 syntax pipeline with explicit seed dependencies.

  The owning Gravity syntax implementation and existing authentication ports
  retain their authority. Supplied operations are trusted seed composition,
  not user-provided proof of reader or syntax authenticity."
  (:require [clojure.string :as str]
            [gravity.reader.pure-text :as reader-pure-text]
            [gravity.c3-reader-expansions :as c3-reader-expansions]
            [gravity.c3-syntax-verification :as verification]))

(def ^:private namespace-contract
 '{:public-api
 {resolve-stream-request-preimage!
  {:arglists ([source-path request resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-digest-preimage},
  registered-literal-fail!
  {:arglists
   ([source-path form-record missing-fields facts operations]),
   :operation-inputs [:c3-syntax-fail! :source-span],
   :returns :throws-c3-fact-stale},
  resolve-request-preimage!
  {:arglists ([source-path request resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-digest-preimage},
  resolve-stream-template!
  {:arglists
   ([source-path stream-template resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-syntax-stream},
  current-reader-revision-authentic?
  {:arglists ([c2-artifact boundary invocation operations]),
   :operation-inputs
   [:p15-s23-stage2-sh02-descriptor-envelope-verify!
    :sh03-reader-expected-source-content-hash
    :sh03-reader-verification-invocation!
    :sh03-reader-verifier
    :sh04-syntax-reader-binding
    :sh04-syntax-semantic-source-id],
   :returns :boolean},
  registered-literal-registry
  {:arglists ([source-path c2-artifact operations]),
   :operation-inputs [:sh04-syntax-registered-literal-fail!],
   :returns :registered-occurrence-registry},
  current-plan-binding!
  {:arglists ([request-source binding-cache operations]),
   :operation-inputs
   [:sh04-syntax-boundary-fail!
    :sh04-syntax-plan-identities
    :sh04-syntax-read-pinned-source!],
   :returns :gravity/sh04-pinned-syntax-plan-binding},
  form-index
  {:arglists
   ([c2-artifact transaction operations canonical-operations]),
   :returns :native-form-index,
   :operation-inputs
   [:sh04-syntax-form-index-roots-current?
    :sh04-syntax-native-keyword-map?
    :runtime-supported?]},
  require-result-carrier!
  {:arglists ([source-path carrier value operations]),
   :operation-inputs
   [:p15-s23-trusted-carrier-validation :sh04-syntax-boundary-fail!],
   :returns :native-carrier},
  boundary-fail!
  {:arglists
   ([rule source-path missing-field observed facts operations]),
   :operation-inputs [:c3-syntax-fail! :source-span],
   :returns :throws-c3-diagnostic},
  source-descriptor
  {:arglists
   ([seed
     form-record
     c2-artifact
     integrity-report
     semantic-source-id
     reader-binding
     reader-source-revision
     registered-literals
     operations
     canonical-operations
     transaction-var]),
   :returns :syntax-source-descriptor,
   :operation-inputs
   [:c3-lossless-literal-descriptor
    :c3-source-facts
    :c3-source-form-kind
    :c3-tagged-literal-descriptor
    :sh04-syntax-descendant-form-ids
    :sh04-syntax-empty-hygiene
    :sh04-syntax-form-index
    :sh04-syntax-project-registered-literal-values
    :sh04-syntax-semantic-span
    :sh04-syntax-strip-host-metadata]},
  accept-result!
  {:arglists ([source-path result operations]),
   :operation-inputs
   [:c3-syntax-fail! :sh04-syntax-require-carrier! :source-span],
   :returns :accepted-gravity-result},
  empty-hygiene
  {:arglists ([namespace]),
   :returns :hygiene-context,
   :operation-inputs []},
  descriptor-reader-product-binding
  {:arglists ([descriptor operations]),
   :operation-inputs [:sh04-syntax-sh03-product-binding-keys],
   :returns :reader-product-binding},
  native-keyword-map?
  {:arglists ([value]), :returns :boolean, :operation-inputs []},
  descendant-form-ids
  {:arglists ([forms-by-id root-form-id]),
   :returns :descendant-form-ids,
   :operation-inputs []},
  registered-literal-projection-authentic?
  {:arglists ([c2-view gravity-boundary operations]),
   :operation-inputs
   [:p15-s23-c6c10-canonical-digest
    :p15-s23-trusted-carrier-validation
    :sh04-syntax-strip-host-metadata],
   :returns :boolean},
  reader-expansions-current?
  {:arglists
   ([syntax-stream c2 boundary invocation history operations]),
   :operation-inputs
   [:p15-s23-stage2-sh02-descriptor-envelope-verify!
    :sh03-reader-authenticated-envelope-options],
   :returns :boolean},
  assemble-reader!
  {:arglists ([source-path c2-artifact operations]),
   :operation-inputs
   [:c3-validate-c2-reader-artifact!
    :sh04-syntax-build-from-fresh-c2!],
   :returns :verified-syntax-assembly},
  reader-products-authentic?
  {:arglists
   ([syntax-stream
     c2-artifact
     gravity-boundary
     invocation
     operations]),
   :returns :boolean},
  logical-source-stem
  {:arglists ([source-path source-unit operations]),
   :operation-inputs [:reader-normalize-relative-path],
   :returns :logical-source-stem},
  plan-identities
  {:arglists ([plan operations]),
   :operation-inputs
   [:p15-s23-c11-mir-digest
    :p15-s23-stage2-compiler-artifact-semantic-input
    :sh04-syntax-public-function-hashes
    :sh04-syntax-public-function-shapes],
   :returns :plan-identity-map},
  strip-host-metadata
  {:arglists ([value operations]),
   :returns :metadata-free-value,
   :operation-inputs [:sh04-syntax-strip-host-metadata]},
  reader-expansion-history
  {:arglists ([source-path c2 operations]),
   :operation-inputs
   [:c3-current-reader-identity-view
    :p15-s23-c6c10-canonical-digest
    :sh04-syntax-semantic-source-id
    :sh04-syntax-strip-host-metadata],
   :returns :reader-expansion-history},
  plan-binding-cache
  {:arglists ([operations]),
   :returns :delayed-plan-binding,
   :operation-inputs [:sh04-syntax-build-binding!]},
  reader-artifact-view
  {:arglists ([c2-artifact invocation operations]),
   :operation-inputs
   [:p15-s23-c6c10-canonical-digest
    :p15-s23-trusted-carrier-validation
    :sh03-c3-precomputed-c2-verify!
    :sh03-reader-internal-product-authority
    :sh04-syntax-current-sh03-product-binding
    :sh04-syntax-project-registered-literal-values
    :sh04-syntax-reader-binding
    :sh04-syntax-registered-literal-registry
    :sh04-syntax-semantic-source-id],
   :returns :reader-artifact-view},
  execute-plan!
  {:arglists ([source-path binding function arguments operations]),
   :operation-inputs
   [:p15-s23-stage2-runtime-execute-function
    :sh04-syntax-boundary-fail!
    :sh04-syntax-strip-host-metadata],
   :returns :gravity-runtime-value},
  literal-index
  {:arglists
   ([artifact
     cache-key
     field
     id-key
     original
     transaction
     operations
     canonical-operations]),
   :returns :native-literal-index,
   :operation-inputs
   [:literal-operation-defaults
    :literal-operations-current?
    :runtime-supported?
    :sh04-syntax-form-index-roots-current?
    :sh04-syntax-native-keyword-map?]},
  read-pinned-plan-source!
  {:arglists ([request-source operations]),
   :operation-inputs
   [:sh04-syntax-boundary-fail!
    :sh04-syntax-expected-source-byte-count
    :sh04-syntax-expected-source-content-hash
    :sh04-syntax-resolve-source-path
    :sha256-bytes-hex],
   :returns :pinned-source-record},
  resolve-plan-source-path
  {:arglists ([operations]),
   :operation-inputs
   [:p15-s23-stage2-compiler-artifact-source-path
    :sh04-syntax-source-relative-path],
   :returns :source-path},
  project-registered-literal-values
  {:arglists ([source-path registry value operations]),
   :operation-inputs [:sh04-syntax-registered-literal-fail!],
   :returns :native-projected-value},
  resolve-template!
  {:arglists
   ([source-path
     binding
     raw
     reader-binding
     reader-source-revision
     operations]),
   :operation-inputs
   [:p15-s23-c6c10-canonical-digest
    :sh04-syntax-boundary-fail!
    :sh04-syntax-execute!
    :sh04-syntax-raise-result!
    :sh04-syntax-resolve-object-template!
    :sh04-syntax-resolve-request-preimage!],
   :returns :resolved-syntax-template},
  current-reader-identity-view
  {:arglists ([source-path c2-artifact operations]),
   :operation-inputs
   [:sh04-syntax-project-registered-literal-values
    :sh04-syntax-registered-literal-registry],
   :returns :reader-identity-view},
  rich-object
  {:arglists ([syntax serialization-id operations]),
   :operation-inputs [],
   :returns :compatibility-syntax-object},
  assemble-fresh-reader!
  {:arglists
   ([source-path c2-artifact integrity-report invocation operations]),
   :operation-inputs
   [:c2-top-level-products
    :p15-s23-c6c10-canonical-digest
    :p15-s23-stage2-sh02-descriptor-envelope
    :p15-s23-stage2-sh02-descriptor-envelope-verify!
    :reader-canonical-hash
    :sh03-reader-authenticated-envelope-options
    :sh04-syntax-adapter-contract
    :sh04-syntax-boundary-fail!
    :sh04-syntax-current-binding!
    :sh04-syntax-descriptor-sh03-product-binding
    :sh04-syntax-envelope-stage
    :sh04-syntax-execute!
    :sh04-syntax-generated-products!
    :sh04-syntax-reader-binding
    :sh04-syntax-reader-expansion-history
    :sh04-syntax-registered-literal-registry
    :sh04-syntax-require-carrier!
    :sh04-syntax-resolve-stream-request-preimage!
    :sh04-syntax-resolve-stream-template!
    :sh04-syntax-resolve-template!
    :sh04-syntax-rich-object
    :sh04-syntax-sealed-artifact-kind
    :sh04-syntax-semantic-source-id
    :sh04-syntax-sh02-descriptor
    :sh04-syntax-source-descriptor
    :syntax-with-source-index-transaction],
   :returns :verified-syntax-assembly},
  envelope-descriptor
  {:arglists ([source-path binding summary operations]),
   :operation-inputs
   [:p15-s23-c6c10-canonical-digest
    :sh04-syntax-public-function-hashes
    :sh04-syntax-public-function-shapes
    :sh04-syntax-sealed-artifact-kind
    :sh04-syntax-source-relative-path],
   :returns :private-envelope-descriptor},
  resolve-object-template!
  {:arglists
   ([source-path syntax-template resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-syntax-object},
  build
  {:arglists ([request operations]),
   :returns :gravity/stage0-c3-syntax-object-artifact},
  index-roots-current?
  {:arglists ([roots]), :returns :boolean, :operation-inputs []},
  generated-products!
  {:arglists
   ([source-path
     binding
     base
     semantic-source-id
     reader-binding
     reader-source-revision
     operations]),
   :operation-inputs
   [:reader-canonical-hash
    :sh04-syntax-execute!
    :sh04-syntax-resolve-template!],
   :returns :resolved-generated-products},
  semantic-span
  {:arglists ([span semantic-source-id operations]),
   :operation-inputs [],
   :returns :source-span},
  semantic-source-id
  {:arglists ([source-path source-unit operations]),
   :operation-inputs
   [:reader-canonical-hash :sh04-syntax-logical-stem],
   :returns :source-id},
  reader-semantic-binding
  {:arglists ([source-path c2-artifact semantic-source-id operations]),
   :operation-inputs
   [:c2-extension-hash-input
    :c3-current-reader-identity-view
    :p15-s23-c6c10-canonical-digest
    :p15-s23-sh02-sha256-id?
    :reader-canonical-hash
    :sh03-reader-boundary-fail!
    :sh03-reader-entrypoint
    :sh03-reader-expected-entrypoint-semantic-hash
    :sh03-reader-expected-functions-semantic-hash
    :sh03-reader-expected-plan-semantic-hash
    :sh03-reader-expected-source-byte-count
    :sh03-reader-expected-source-content-hash
    :sh03-reader-expected-verifier-semantic-hash
    :sh03-reader-source-relative-path
    :sh03-reader-verifier],
   :returns :reader-semantic-binding-and-revision},
  build-plan-binding!
  {:arglists ([request-source operations]),
   :operation-inputs
   [:c-backend-canonical-value
    :c-backend-stage2-plan-emitter-source-rule!
    :p15-s23-stage2-compiler-artifact-plan
    :sh04-syntax-boundary-fail!
    :sh04-syntax-expected-function-count
    :sh04-syntax-expected-function-names-hash
    :sh04-syntax-expected-function-shapes-hash
    :sh04-syntax-expected-functions-semantic-hash
    :sh04-syntax-expected-plan-semantic-hash
    :sh04-syntax-plan-identities
    :sh04-syntax-public-function-hashes
    :sh04-syntax-public-function-shapes
    :sh04-syntax-read-pinned-source!
    :sha256-hex],
   :returns :gravity/sh04-pinned-syntax-plan-binding},
  verify
  {:arglists ([source-path artifact operations]), :returns :complete},
  current-reader-product-binding
  {:arglists ([c2-artifact operations]),
   :operation-inputs
   [:c2-extension-hash-input
    :c2-form-hash-input
    :c2-token-hash-input
    :reader-canonical-hash],
   :returns :reader-product-binding}},
 :operation-inputs
 {:build
  [:c2-reader-fail!
   :compiler-c2-reader-source-artifact
   :c3-validate-c2-reader-artifact!
   :c3-syntax-overrides-from-forms
   :c3-syntax-validate-overrides!
   :sh03-c3-precomputed-c2-verify!
   :sh04-syntax-build-from-fresh-c2!
   :c3-syntax-serialization-fixture
   :c3-reader-artifact-view
   :c3-syntax-schema
   :c3-hygiene-context-map
   :c3-origin-chain-graph
   :c3-metadata-ledger
   :c3-generated-syntax-report
   :c3-fact-ledger
   :c3-syntax-verification-report
   :c3-syntax-validate!
   :c3-syntax-capability-proof
   :c3-artifact-id
   :sh04-syntax-adapter-contract
   :sh04-syntax-facade-relative-path
   :c3-syntax-governing-document
   :c3-syntax-diagnostic-ids
   :c3-syntax-rejected-designs],
  :reader-products-authentic?
  [:sh03-reader-authenticated-envelope-options
   :c3-current-reader-revision-authentic?
   :p15-s23-c6c10-canonical-digest
   :sh04-syntax-rich-object
   :sh04-syntax-registered-literal-projection-authentic?
   :sh04-syntax-reader-expansions-current?
   :sh04-syntax-reader-expansion-history
   :c3-c2-reader-integrity-report
   :sh04-syntax-semantic-source-id
   :sh04-syntax-reader-binding
   :sh04-syntax-current-sh03-product-binding
   :sh04-syntax-descriptor-sh03-product-binding
   :p15-s23-stage2-sh02-descriptor-envelope-verify!
   :sh04-syntax-current-binding!
   :sh04-syntax-execute!
   :reader-canonical-hash
   :sh04-syntax-sh02-descriptor
   :p15-s23-stage2-sh02-descriptor-envelope
   :c2-top-level-products
   :c3-syntax-object
   :c3-generated-syntax-object
   :sh04-syntax-adapter-contract],
  :runtime
  [:sh04-syntax-boundary-fail!
   :sh04-syntax-resolve-source-path
   :sh04-syntax-read-pinned-source!
   :sh04-syntax-plan-identities
   :sh04-syntax-build-binding!
   :sh04-syntax-execute!
   :sh04-syntax-require-carrier!
   :sh04-syntax-raise-result!
   :sh04-syntax-resolve-object-template!
   :sh04-syntax-resolve-request-preimage!
   :c3-syntax-fail!
   :source-span
   :p15-s23-stage2-compiler-artifact-source-path
   :sha256-bytes-hex
   :p15-s23-c11-mir-digest
   :p15-s23-stage2-compiler-artifact-semantic-input
   :c-backend-stage2-plan-emitter-source-rule!
   :p15-s23-stage2-compiler-artifact-plan
   :sha256-hex
   :c-backend-canonical-value
   :sh04-syntax-strip-host-metadata
   :p15-s23-stage2-runtime-execute-function
   :p15-s23-trusted-carrier-validation
   :p15-s23-c6c10-canonical-digest
   :sh04-syntax-source-relative-path
   :sh04-syntax-expected-source-byte-count
   :sh04-syntax-expected-source-content-hash
   :sh04-syntax-expected-plan-semantic-hash
   :sh04-syntax-expected-functions-semantic-hash
   :sh04-syntax-expected-function-count
   :sh04-syntax-expected-function-names-hash
   :sh04-syntax-expected-function-shapes-hash
   :sh04-syntax-public-function-hashes
   :sh04-syntax-public-function-shapes],
  :reader-views
  [:c3-syntax-fail!
   :p15-s23-c6c10-canonical-digest
   :p15-s23-stage2-sh02-descriptor-envelope-verify!
   :p15-s23-trusted-carrier-validation
   :sh03-c3-precomputed-c2-verify!
   :sh03-reader-expected-source-content-hash
   :sh03-reader-internal-product-authority
   :sh03-reader-verification-invocation!
   :sh03-reader-verifier
   :sh04-syntax-current-sh03-product-binding
   :sh04-syntax-project-registered-literal-values
   :sh04-syntax-reader-binding
   :sh04-syntax-registered-literal-fail!
   :sh04-syntax-registered-literal-registry
   :sh04-syntax-semantic-source-id
   :sh04-syntax-strip-host-metadata
   :source-span],
  :assembly
  [:c2-extension-hash-input
   :c2-form-hash-input
   :c2-token-hash-input
   :c2-top-level-products
   :c3-current-reader-identity-view
   :c3-validate-c2-reader-artifact!
   :p15-s23-c6c10-canonical-digest
   :p15-s23-sh02-sha256-id?
   :p15-s23-stage2-sh02-descriptor-envelope
   :p15-s23-stage2-sh02-descriptor-envelope-verify!
   :reader-canonical-hash
   :reader-normalize-relative-path
   :sh03-reader-authenticated-envelope-options
   :sh03-reader-boundary-fail!
   :sh03-reader-entrypoint
   :sh03-reader-expected-entrypoint-semantic-hash
   :sh03-reader-expected-functions-semantic-hash
   :sh03-reader-expected-plan-semantic-hash
   :sh03-reader-expected-source-byte-count
   :sh03-reader-expected-source-content-hash
   :sh03-reader-expected-verifier-semantic-hash
   :sh03-reader-source-relative-path
   :sh03-reader-verifier
   :sh04-syntax-adapter-contract
   :sh04-syntax-boundary-fail!
   :sh04-syntax-build-from-fresh-c2!
   :sh04-syntax-current-binding!
   :sh04-syntax-descriptor-sh03-product-binding
   :sh04-syntax-envelope-stage
   :sh04-syntax-execute!
   :sh04-syntax-generated-products!
   :sh04-syntax-logical-stem
   :sh04-syntax-public-function-hashes
   :sh04-syntax-public-function-shapes
   :sh04-syntax-reader-binding
   :sh04-syntax-reader-expansion-history
   :sh04-syntax-registered-literal-registry
   :sh04-syntax-require-carrier!
   :sh04-syntax-resolve-stream-request-preimage!
   :sh04-syntax-resolve-stream-template!
   :sh04-syntax-resolve-template!
   :sh04-syntax-rich-object
   :sh04-syntax-sealed-artifact-kind
   :sh04-syntax-semantic-source-id
   :sh04-syntax-sh02-descriptor
   :sh04-syntax-sh03-product-binding-keys
   :sh04-syntax-source-descriptor
   :sh04-syntax-source-relative-path
   :sh04-syntax-strip-host-metadata
   :syntax-with-source-index-transaction],
  :descriptor
  [:c3-lossless-literal-descriptor
   :c3-source-facts
   :c3-source-form-kind
   :c3-tagged-literal-descriptor
   :sh04-syntax-descendant-form-ids
   :sh04-syntax-empty-hygiene
   :sh04-syntax-form-index
   :sh04-syntax-project-registered-literal-values
   :sh04-syntax-semantic-span
   :sh04-syntax-strip-host-metadata],
  :native-index
  [:literal-operation-defaults
   :literal-operations-current?
   :runtime-supported?
   :sh04-syntax-form-index-roots-current?
   :sh04-syntax-native-keyword-map?]},
 :bootstrap-hosted? true,
 :dependency-direction
 {:requires
  [clojure.core
   clojure.string
   gravity.reader.pure-text
   gravity.c3-reader-expansions
   gravity.c3-syntax-verification],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :test-owner gravity.syntax.pipeline-test,
 :injected-operations-are-proof-authority? false,
 :self-hosted? false,
 :invalidates [],
 :governing-contracts [:D1 :D2 :D3 :D9 :L1 :C1 :C2 :C3 :C15 :TEST2],
 :preserves
 [:source-spans
  :syntax-identity
  :reader-revision
  :source-provenance
  :metadata
  :generated-origin
  :hygiene
  :fact-invalidation
  :serialization
  :opaque-callback-order
  :native-index-owner-and-thread],
 :artifact-inputs
 [:decoded-source-text
  :source-path
  :explicit-project-context
  :authenticated-reader-products
  :reader-invocation
  :seed-construction-operations
  :seed-authentication-operations
  :pinned-gravity-syntax-source
  :plan-binding-cache
  :digest-requests
  :registered-reader-occurrences
  :reader-revision
  :internal-reader-reverification-port
  :fresh-c2-validation-report
  :source-descriptor-port
  :index-transaction-port
  :native-index-transaction
  :root-snapshots
  :descriptor-operations
  :canonical-operation-context
  :seed-transaction-var],
 :namespace gravity.syntax,
 :release-authority? false,
 :diagnostic-ownership
 {:owns
  [:syntax-pipeline-validation-order
   :reader-syntax-association-refusal
   :pinned-plan-authentication
   :runtime-containment
   :digest-resolution
   :registered-literal-projection-refusal
   :reader-view-revision-join
   :syntax-stream-replay
   :syntax-envelope-assembly
   :syntax-round-trip
   :source-descriptor-projection],
  :propagates
  [:C2-HASH
   :C3-SHAPE
   :C3-ID
   :C3-SPAN
   :C3-ORIGIN
   :C3-HYGIENE
   :C3-CAPTURE
   :C3-METADATA
   :C3-FACT-STALE
   :C3-SERIALIZE],
  :catalog-owner gravity.c3-syntax-diagnostics},
 :artifact-outputs
 [:gravity/stage0-c3-syntax-object-artifact
  :gravity/syntax-verification-report
  :gravity/sh04-pinned-syntax-plan-binding
  :resolved-syntax-template
  :gravity/sh04-registered-literal-projection
  :reader-artifact-view
  :resolved-syntax-stream
  :syntax-envelope
  :serialization-records
  :syntax-source-descriptor
  :private-native-lookup-index],
 :clojure-seed-boundary? true,
 :contract-boundary
 :stage0-syntax-construction-verification-and-pinned-plan-runtime}
)

(defn
 build
 "Construct and validate the existing C3 syntax artifact from explicit inputs."
 [{:keys
   [source-path
    source-text
    project-context
    precomputed-c2-artifact
    candidate
    reader-source-path
    invocation]}
  {:keys
   [c2-reader-fail!
    compiler-c2-reader-source-artifact
    c3-validate-c2-reader-artifact!
    c3-syntax-overrides-from-forms
    c3-syntax-validate-overrides!
    sh03-c3-precomputed-c2-verify!
    sh04-syntax-build-from-fresh-c2!
    c3-syntax-serialization-fixture
    c3-reader-artifact-view
    c3-syntax-schema
    c3-hygiene-context-map
    c3-origin-chain-graph
    c3-metadata-ledger
    c3-generated-syntax-report
    c3-fact-ledger
    c3-syntax-verification-report
    c3-syntax-validate!
    c3-syntax-capability-proof
    c3-artifact-id
    sh04-syntax-adapter-contract
    sh04-syntax-facade-relative-path
    c3-syntax-governing-document
    c3-syntax-diagnostic-ids
    c3-syntax-rejected-designs]}]
 (when
  (and
   (some? reader-source-path)
   (or (some? precomputed-c2-artifact) (some? candidate)))
  (c2-reader-fail!
   "C2-HASH"
   source-path
   {:stage :read-source}
   {:missing-fields [:direct-current-sh03-reader-construction]}))
 (let
  [c2-artifact
   (if
    precomputed-c2-artifact
    (apply
     sh03-c3-precomputed-c2-verify!
     (cond->
      [candidate source-path precomputed-c2-artifact]
      invocation
      (conj invocation)))
    (compiler-c2-reader-source-artifact
     source-path
     source-text
     project-context
     nil
     nil
     reader-source-path
     invocation))
   integrity-report
   (c3-validate-c2-reader-artifact! source-path c2-artifact)
   forms
   (:parsed-semantic-values c2-artifact)
   overrides
   (c3-syntax-overrides-from-forms forms)
   _
   (c3-syntax-validate-overrides! source-path overrides)
   sh04-products
   (sh04-syntax-build-from-fresh-c2!
    source-path
    c2-artifact
    integrity-report
    invocation)
   syntax-stream
   (:rich-syntax sh04-products)
   sh04-result
   (:resolved-result sh04-products)
   gravity-boundary
   {:plan-binding (:plan-binding sh04-products),
    :resolved-stream-verification (:stream-verification sh04-products),
    :clojure-adapter-residual? true,
    :reader-source-revision (:reader-source-revision sh04-products),
    :authenticated-envelope-descriptor (:descriptor sh04-products),
    :resolved-syntax-result sh04-result,
    :self-hosted? false,
    :stream-resolved-digests (:stream-resolved-digests sh04-products),
    :gravity-syntax-deserialization (:deserialization sh04-products),
    :slice :SH-04,
    :stream-digest-requests (:stream-digest-requests sh04-products),
    :uncredited-compatibility-facade
    {:module 'gravity.compiler.c3-syntax-object-model,
     :source-path sh04-syntax-facade-relative-path,
     :status :compatibility-only,
     :authentication-credit? false,
     :authoritative-result? false,
     :self-hosting-credit? false,
     :seed-retirement-credit? false,
     :release-credit? false},
    :reader-semantic-binding (:reader-binding sh04-products),
    :reader-authentication-provenance
    (:reader-authentication-provenance sh04-products),
    :adapter-contract sh04-syntax-adapter-contract,
    :gravity-syntax-serialization (:serialization sh04-products),
    :target-source-reread? false,
    :owner :gravity-source,
    :authenticated-envelope (:envelope sh04-products)}
   serialization
   (c3-syntax-serialization-fixture syntax-stream)
   artifact-base
   {:generated-syntax-report
    (c3-generated-syntax-report syntax-stream),
    :syntax-object-stream syntax-stream,
    :gravity-metadata-ledger (:metadata-ledger sh04-result),
    :gravity-syntax-ownership-product (:ownership-product sh04-result),
    :syntax-serialization-fixture serialization,
    :diagnostics [],
    :gravity-hygiene-context-map (:hygiene-context-map sh04-result),
    :task "P06-D082",
    :fact-ledger (c3-fact-ledger syntax-stream),
    :c2-reader-artifact
    (apply
     c3-reader-artifact-view
     (cond-> [c2-artifact] invocation (conj invocation))),
    :gravity-fact-invalidation-ledger
    (:fact-invalidation-ledger sh04-result),
    :document-set ["C3"],
    :governing-document c3-syntax-governing-document,
    :syntax-verification-report
    (c3-syntax-verification-report
     syntax-stream
     serialization
     c2-artifact
     gravity-boundary
     invocation),
    :hygiene-context-map (c3-hygiene-context-map syntax-stream),
    :gravity-syntax-boundary gravity-boundary,
    :kind :gravity/stage0-c3-syntax-object-artifact,
    :syntax-object-schema (c3-syntax-schema),
    :rejected-design-coverage c3-syntax-rejected-designs,
    :metadata-ledger (c3-metadata-ledger syntax-stream),
    :pass
    {:name :c3-syntax-object-model,
     :input :c2-reader-document-artifact,
     :output :syntax-object-model-proof,
     :requires
     [:source-unit-record :syntax-seed-stream :reader-source-map],
     :preserves
     [:source-spans
      :generated-origin
      :namespace-context
      :profile
      :metadata
      :hygiene],
     :emits
     [:syntax-object-schema
      :syntax-object-stream
      :hygiene-context-map
      :origin-chain-graph
      :metadata-ledger
      :generated-syntax-report
      :syntax-verification-report
      :syntax-serialization-fixture],
     :rejects c3-syntax-diagnostic-ids},
    :origin-chain-graph (c3-origin-chain-graph syntax-stream),
    :source-overrides overrides,
    :gravity-origin-chain-graph (:origin-chain-graph sh04-result)}
   _
   (c3-syntax-validate! source-path artifact-base invocation)
   capability-proof
   (c3-syntax-capability-proof artifact-base invocation)
   conformance
   {:task "P06-D082",
    :serialization-status :complete,
    :diagnostic-status :complete,
    :hygiene-status :complete,
    :schema-status :complete,
    :capture-status :complete,
    :documents ["C3"],
    :metadata-status :complete,
    :status :complete,
    :stream-status :complete,
    :fact-invalidation-status :complete,
    :required-diagnostic-ids c3-syntax-diagnostic-ids,
    :origin-status :complete}
   artifact
   (assoc
    artifact-base
    :capability-based-proof
    capability-proof
    :c3-syntax-results
    conformance)]
  (assoc artifact :artifact-id (c3-artifact-id artifact))))

(defn
 reader-products-authentic?
 "Recompute the existing reader-to-syntax association and envelope checks."
 [syntax-stream
  c2-artifact
  gravity-boundary
  invocation
  {:keys
   [sh03-reader-authenticated-envelope-options
    c3-current-reader-revision-authentic?
    p15-s23-c6c10-canonical-digest
    sh04-syntax-rich-object
    sh04-syntax-registered-literal-projection-authentic?
    sh04-syntax-reader-expansions-current?
    sh04-syntax-reader-expansion-history
    c3-c2-reader-integrity-report
    sh04-syntax-semantic-source-id
    sh04-syntax-reader-binding
    sh04-syntax-current-sh03-product-binding
    sh04-syntax-descriptor-sh03-product-binding
    p15-s23-stage2-sh02-descriptor-envelope-verify!
    sh04-syntax-current-binding!
    sh04-syntax-execute!
    reader-canonical-hash
    sh04-syntax-sh02-descriptor
    p15-s23-stage2-sh02-descriptor-envelope
    c2-top-level-products
    c3-syntax-object
    c3-generated-syntax-object
    sh04-syntax-adapter-contract]}]
 (try
  (let
   [ae-options
    (when
     invocation
     (sh03-reader-authenticated-envelope-options
      (get-in c2-artifact [:source-unit-record :path])
      invocation))]
   (if-not
    (apply
     c3-current-reader-revision-authentic?
     (cond->
      [c2-artifact gravity-boundary]
      invocation
      (conj invocation)))
    false
    (if
     (:registered-literal-projection c2-artifact)
     (if
      gravity-boundary
      (let
       [result
        (:resolved-syntax-result gravity-boundary)
        result-stream
        (:syntax-object-stream result)
        source-path
        (get-in c2-artifact [:source-unit-record :path])
        serialization-id
        (p15-s23-c6c10-canonical-digest
         source-path
         (get-in
          gravity-boundary
          [:gravity-syntax-serialization :payload-id-request]))
        expected-rich-stream
        (mapv
         sh04-syntax-rich-object
         result-stream
         (repeat serialization-id))]
       (and
        (sh04-syntax-registered-literal-projection-authentic?
         c2-artifact
         gravity-boundary)
        (= :accepted (:status result))
        (= syntax-stream expected-rich-stream)
        (=
         :passed
         (get-in
          gravity-boundary
          [:resolved-stream-verification :status]))
        (=
         :accepted
         (get-in
          gravity-boundary
          [:gravity-syntax-serialization :status]))
        (=
         :accepted
         (get-in
          gravity-boundary
          [:gravity-syntax-deserialization :status]))
        (sh04-syntax-reader-expansions-current?
         syntax-stream
         c2-artifact
         gravity-boundary
         invocation
         (sh04-syntax-reader-expansion-history
          source-path
          c2-artifact))))
      false)
     (let
      [integrity-report (c3-c2-reader-integrity-report c2-artifact)]
      (if
       gravity-boundary
       (let
        [result
         (:resolved-syntax-result gravity-boundary)
         result-stream
         (:syntax-object-stream result)
         source-path
         (get-in c2-artifact [:source-unit-record :path])
         semantic-source-id
         (sh04-syntax-semantic-source-id
          source-path
          (:source-unit-record c2-artifact))
         expected-reader-authentication
         (sh04-syntax-reader-binding
          source-path
          c2-artifact
          semantic-source-id)
         sh03-authentication
         (or
          (:sh03-reader-authentication c2-artifact)
          (let
           [reader-boundary (:gravity-reader-boundary c2-artifact)]
           {:reader-result-id
            (get-in
             reader-boundary
             [:resolved-reader-result
              :incremental-reader-hashes
              :reader-result]),
            :semantic-envelope-id
            (get-in
             reader-boundary
             [:authenticated-envelope :semantic-envelope-id]),
            :provenance-binding-id
            (get-in
             reader-boundary
             [:authenticated-envelope :provenance-binding-id])}))
         reader-authentication-provenance
         (:reader-authentication-provenance gravity-boundary)
         actual-sh03-envelope
         (:actual-sh03-authenticated-envelope
          reader-authentication-provenance)
         actual-sh03-envelope-descriptor
         (:actual-sh03-envelope-descriptor
          reader-authentication-provenance)
         expected-sh03-product-binding
         (sh04-syntax-current-sh03-product-binding c2-artifact)
         actual-sh03-product-binding
         (sh04-syntax-descriptor-sh03-product-binding
          actual-sh03-envelope-descriptor)
         actual-sh03-envelope-verification
         (apply
          p15-s23-stage2-sh02-descriptor-envelope-verify!
          (cond->
           [actual-sh03-envelope
            :c2-reader
            :gravity/sh03-reader-products
            actual-sh03-envelope-descriptor
            source-path]
           ae-options
           (conj ae-options)))
         binding
         (sh04-syntax-current-binding! source-path)
         requests
         (:stream-digest-requests gravity-boundary)
         digests
         (:stream-resolved-digests gravity-boundary)
         fresh-stream-verification
         (sh04-syntax-execute!
          source-path
          binding
          'c3-syntax-stream-verify-resolved
          [result requests digests])
         fresh-serialization
         (sh04-syntax-execute!
          source-path
          binding
          'c3-syntax-serialize-template
          [result requests digests])
         fresh-deserialization
         (sh04-syntax-execute!
          source-path
          binding
          'c3-syntax-deserialize-template
          [(:carrier fresh-serialization)])
         serialization-id
         (p15-s23-c6c10-canonical-digest
          source-path
          (get-in
           gravity-boundary
           [:gravity-syntax-serialization :payload-id-request]))
         expected-rich-stream
         (mapv
          sh04-syntax-rich-object
          result-stream
          (repeat serialization-id))
         source-syntax
         (filterv
          (fn*
           [p1__142#]
           (not= :generated-form (get-in p1__142# [:form :kind])))
          syntax-stream)
         generated-syntax
         (filterv
          (fn*
           [p1__143#]
           (= :generated-form (get-in p1__143# [:form :kind])))
          syntax-stream)
         expected-form-ids
         (mapv :form-id (:syntax-seed-stream c2-artifact))
         observed-form-ids
         (mapv
          (fn* [p1__144#] (get-in p1__144# [:source :form-id]))
          source-syntax)
         envelope
         (:authenticated-envelope gravity-boundary)
         envelope-descriptor
         (:authenticated-envelope-descriptor gravity-boundary)
         expansion-history
         (sh04-syntax-reader-expansion-history source-path c2-artifact)
         expected-summary
         (cond->
          {:serialization-set-id serialization-id,
           :reader-binding-id
           (reader-canonical-hash
            (:reader-binding expected-reader-authentication)),
           :reader-source-revision-id
           (get-in
            expected-reader-authentication
            [:reader-source-revision :revision-id]),
           :status :accepted,
           :slice :SH-04,
           :syntax-stream-id (:artifact-id result),
           :semantic-source-id semantic-source-id,
           :graph-id
           (reader-canonical-hash (:graph-verification-report result)),
           :adapter-contract sh04-syntax-adapter-contract,
           :syntax-result-id (:artifact-id result)}
          (:id expansion-history)
          (assoc :reader-expansion-history-id (:id expansion-history)))
         expected-envelope-descriptor
         (sh04-syntax-sh02-descriptor
          source-path
          binding
          expected-summary)
         expected-envelope
         (apply
          p15-s23-stage2-sh02-descriptor-envelope
          (cond->
           [:c3-syntax
            :gravity/sh04-syntax-products
            expected-envelope-descriptor
            source-path]
           ae-options
           (conj ae-options)))
         envelope-verification
         (apply
          p15-s23-stage2-sh02-descriptor-envelope-verify!
          (cond->
           [envelope
            :c3-syntax
            :gravity/sh04-syntax-products
            envelope-descriptor
            source-path]
           ae-options
           (conj ae-options)))]
        (and
         (:authentic? integrity-report)
         (= :SH-04 (:slice gravity-boundary))
         (= :gravity-source (:owner gravity-boundary))
         (= :accepted (:status result))
         (= syntax-stream expected-rich-stream)
         (=
          (:reader-binding expected-reader-authentication)
          (:reader-semantic-binding gravity-boundary))
         (=
          (:reader-source-revision expected-reader-authentication)
          (:reader-source-revision gravity-boundary))
         (=
          (:reader-result-id sh03-authentication)
          (:actual-sh03-reader-result-id
           reader-authentication-provenance))
         (=
          (:semantic-envelope-id sh03-authentication)
          (:actual-sh03-semantic-envelope-id
           reader-authentication-provenance))
         (=
          (:provenance-binding-id sh03-authentication)
          (:actual-sh03-provenance-binding-id
           reader-authentication-provenance))
         (= :passed actual-sh03-envelope-verification)
         (= expected-sh03-product-binding actual-sh03-product-binding)
         (=
          expected-sh03-product-binding
          (:actual-sh03-semantic-product-binding
           reader-authentication-provenance))
         (=
          (:semantic-envelope-id actual-sh03-envelope)
          (:actual-sh03-semantic-envelope-id
           reader-authentication-provenance))
         (=
          (:provenance-binding-id actual-sh03-envelope)
          (:actual-sh03-provenance-binding-id
           reader-authentication-provenance))
         (= expected-form-ids observed-form-ids)
         (= 1 (count generated-syntax))
         (=
          (:reader-semantic-binding gravity-boundary)
          (:reader-binding result))
         (=
          (:reader-source-revision gravity-boundary)
          (:reader-source-revision result))
         (= :passed (:status fresh-stream-verification))
         (=
          fresh-stream-verification
          (:resolved-stream-verification gravity-boundary))
         (= :accepted (:status fresh-serialization))
         (=
          fresh-serialization
          (:gravity-syntax-serialization gravity-boundary))
         (= :accepted (:status fresh-deserialization))
         (=
          fresh-deserialization
          (:gravity-syntax-deserialization gravity-boundary))
         (=
          (:semantic-payload fresh-serialization)
          (:semantic-payload fresh-deserialization))
         (= expected-envelope-descriptor envelope-descriptor)
         (= expected-envelope envelope)
         (=
          :passed
          (get-in result [:graph-verification-report :status]))
         (= :accepted (:status envelope))
         (= :c3-syntax (:stage envelope))
         (= :passed envelope-verification)
         (sh04-syntax-reader-expansions-current?
          syntax-stream
          c2-artifact
          gravity-boundary
          invocation
          expansion-history)))
       (let
        [source-unit
         (:source-unit-record c2-artifact)
         top-level-products
         (c2-top-level-products c2-artifact)
         expected-base
         (mapv
          (fn
           [seed {:keys [form-record token-record]}]
           (c3-syntax-object
            seed
            form-record
            token-record
            source-unit
            c2-artifact
            integrity-report))
          (:syntax-seed-stream c2-artifact)
          top-level-products)
         expected-generated
         (c3-generated-syntax-object
          (or
           (some
            (fn* [p1__145#] (when (seq (:origin p1__145#)) p1__145#))
            expected-base)
           (first expected-base)))
         expected-stream
         (conj expected-base expected-generated)]
        (and
         (:authentic? integrity-report)
         (= expected-stream syntax-stream))))))))
  (catch StackOverflowError _ false)
  (catch Exception _ false)))

(defn verify
  "Recompute syntax validation using the supplied existing owning operations."
  [source-path artifact operations]
  (verification/with-operations
   operations
   #(verification/c3-syntax-validate! source-path artifact)))

;; Pinned SH-04 plan runtime. Operations are trusted seed dependencies.

(defn
 boundary-fail!
 [rule
  source-path
  missing-field
  observed
  facts
  {:keys [c3-syntax-fail! source-span]}]
 (c3-syntax-fail!
  rule
  source-path
  {:source-span (source-span source-path 0),
   :producer :gravity.bootstrap.syntax,
   :form-kind :syntax-object}
  {:missing-fields [missing-field],
   :facts (merge {:sh04-boundary :gravity-syntax-plan} facts),
   :observed observed}))

(defn
 resolve-plan-source-path
 [{:keys
   [p15-s23-stage2-compiler-artifact-source-path
    sh04-syntax-source-relative-path]}]
 (let
  [anchor
   (java.io.File. (p15-s23-stage2-compiler-artifact-source-path))
   start
   (if (.isDirectory anchor) anchor (.getParentFile anchor))]
  (or
   (loop
    [directory start]
    (when
     directory
     (let
      [candidate
       (java.io.File. directory sh04-syntax-source-relative-path)]
      (if
       (.isFile candidate)
       (.getPath candidate)
       (recur (.getParentFile directory))))))
   sh04-syntax-source-relative-path)))

(defn
 read-pinned-plan-source!
 [request-source
  {:keys
   [sh04-syntax-boundary-fail!
    sh04-syntax-expected-source-byte-count
    sh04-syntax-expected-source-content-hash
    sh04-syntax-resolve-source-path
    sha256-bytes-hex]}]
 (let
  [source-path
   (sh04-syntax-resolve-source-path)
   nio-path
   (.toPath (java.io.File. source-path))
   nofollow
   (into-array
    java.nio.file.LinkOption
    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
   attributes
   (try
    (java.nio.file.Files/readAttributes
     nio-path
     java.nio.file.attribute.BasicFileAttributes
     nofollow)
    (catch
     Exception
     error
     (sh04-syntax-boundary-fail!
      "C3-ID"
      request-source
      :pinned-syntax-source-readable
      source-path
      {:cause-message (.getMessage error)})))
   _
   (when-not
    (and
     attributes
     (.isRegularFile attributes)
     (=
      (long sh04-syntax-expected-source-byte-count)
      (.size attributes)))
    (sh04-syntax-boundary-fail!
     "C3-ID"
     request-source
     :exact-pinned-syntax-source
     source-path
     {:expected-byte-count sh04-syntax-expected-source-byte-count,
      :observed-byte-count (when attributes (.size attributes))}))
   bytes
   (try
    (java.nio.file.Files/readAllBytes nio-path)
    (catch
     Exception
     error
     (sh04-syntax-boundary-fail!
      "C3-ID"
      request-source
      :pinned-syntax-source-bytes
      source-path
      {:cause-message (.getMessage error)})))
   content-hash
   (str "sha256:" (sha256-bytes-hex bytes))]
  (when-not
   (and
    (= sh04-syntax-expected-source-byte-count (alength bytes))
    (= sh04-syntax-expected-source-content-hash content-hash))
   (sh04-syntax-boundary-fail!
    "C3-ID"
    request-source
    :pinned-syntax-source-identity
    source-path
    {:observed-content-hash content-hash,
     :observed-byte-count (alength bytes)}))
  {:source-path source-path,
   :source-text
   (String. bytes java.nio.charset.StandardCharsets/UTF_8),
   :source-byte-count (alength bytes),
   :source-content-hash content-hash}))

(defn
 plan-identities
 [plan
  {:keys
   [p15-s23-c11-mir-digest
    p15-s23-stage2-compiler-artifact-semantic-input
    sh04-syntax-public-function-hashes
    sh04-syntax-public-function-shapes]}]
 (let
  [functions
   (:functions plan)
   shapes
   (into
    (sorted-map)
    (map
     (fn
      [[name function]]
      [name (select-keys function [:arity :params])]))
    functions)]
  {:plan-semantic-hash
   (p15-s23-c11-mir-digest
    (p15-s23-stage2-compiler-artifact-semantic-input plan)),
   :functions-semantic-hash (p15-s23-c11-mir-digest functions),
   :function-count (count functions),
   :function-names-hash
   (p15-s23-c11-mir-digest (vec (keys functions))),
   :function-shapes-hash (p15-s23-c11-mir-digest shapes),
   :public-function-hashes
   (into
    (sorted-map)
    (map
     (fn [name] [name (p15-s23-c11-mir-digest (get functions name))]))
    (keys sh04-syntax-public-function-hashes)),
   :public-function-shapes
   (select-keys shapes (keys sh04-syntax-public-function-shapes))}))

(defn
 build-plan-binding!
 [request-source
  {:keys
   [c-backend-canonical-value
    c-backend-stage2-plan-emitter-source-rule!
    p15-s23-stage2-compiler-artifact-plan
    sh04-syntax-boundary-fail!
    sh04-syntax-expected-function-count
    sh04-syntax-expected-function-names-hash
    sh04-syntax-expected-function-shapes-hash
    sh04-syntax-expected-functions-semantic-hash
    sh04-syntax-expected-plan-semantic-hash
    sh04-syntax-plan-identities
    sh04-syntax-public-function-hashes
    sh04-syntax-public-function-shapes
    sh04-syntax-read-pinned-source!
    sha256-hex]}]
 (let
  [source
   (sh04-syntax-read-pinned-source! request-source)
   emitter
   (:emitter
    (c-backend-stage2-plan-emitter-source-rule!
     (:source-path source)
     :jvm))
   raw-plan
   (p15-s23-stage2-compiler-artifact-plan
    emitter
    (:source-path source)
    (:source-text source))
   identities
   (sh04-syntax-plan-identities raw-plan)]
  (when-not
   (and
    (= :gravity/stage2-compiler-artifact-plan (:kind raw-plan))
    (true? (:compiler-artifact-plan? raw-plan))
    (= 'gravity.bootstrap.syntax (get-in raw-plan [:module :module]))
    (= :meta (get-in raw-plan [:module :profile]))
    (= :jvm (get-in raw-plan [:module :target]))
    (= #{} (get-in raw-plan [:module :effects]))
    (= #{} (get-in raw-plan [:module :capabilities]))
    (= :safe (get-in raw-plan [:module :safety]))
    (=
     sh04-syntax-expected-plan-semantic-hash
     (:plan-semantic-hash identities))
    (=
     sh04-syntax-expected-functions-semantic-hash
     (:functions-semantic-hash identities))
    (=
     sh04-syntax-expected-function-count
     (:function-count identities))
    (=
     sh04-syntax-expected-function-names-hash
     (:function-names-hash identities))
    (=
     sh04-syntax-expected-function-shapes-hash
     (:function-shapes-hash identities))
    (=
     sh04-syntax-public-function-hashes
     (:public-function-hashes identities))
    (=
     sh04-syntax-public-function-shapes
     (:public-function-shapes identities)))
   (sh04-syntax-boundary-fail!
    "C3-ID"
    request-source
    :pinned-syntax-plan-and-functions
    identities
    {}))
  (let
   [authenticated-plan-base
    (assoc-in
     (dissoc raw-plan :plan-id)
     [:source :byte-count]
     (:source-byte-count source))
    plan
    (assoc
     authenticated-plan-base
     :plan-id
     (str
      "sha256:"
      (sha256-hex
       (pr-str (c-backend-canonical-value authenticated-plan-base)))))]
   (merge
    source
    identities
    {:artifact :gravity/sh04-pinned-syntax-plan-binding,
     :status :complete,
     :semantic-authority :gravity-source,
     :compiled-by :clojure-stage0-seed,
     :executed-by :clojure-stage2-generic-rule-runner,
     :generic-bridge-residual? true,
     :self-hosted? false,
     :plan plan}))))

(defn
 current-plan-binding!
 [request-source
  binding-cache
  {:keys
   [sh04-syntax-boundary-fail!
    sh04-syntax-plan-identities
    sh04-syntax-read-pinned-source!]}]
 (let
  [fresh
   (sh04-syntax-read-pinned-source! request-source)
   binding
   @binding-cache
   identities
   (sh04-syntax-plan-identities (:plan binding))]
  (when-not
   (and
    (= (:source-byte-count fresh) (:source-byte-count binding))
    (= (:source-content-hash fresh) (:source-content-hash binding))
    (=
     (select-keys
      binding
      [:plan-semantic-hash
       :functions-semantic-hash
       :function-count
       :function-names-hash
       :function-shapes-hash
       :public-function-hashes
       :public-function-shapes])
     identities))
   (sh04-syntax-boundary-fail!
    "C3-ID"
    request-source
    :fresh-syntax-source-and-plan-binding
    binding
    {}))
  binding))

(defn
 execute-plan!
 [source-path
  binding
  function
  arguments
  {:keys
   [p15-s23-stage2-runtime-execute-function
    sh04-syntax-boundary-fail!
    sh04-syntax-strip-host-metadata]}]
 (try
  (let
   [clean-arguments (sh04-syntax-strip-host-metadata arguments)]
   (sh04-syntax-strip-host-metadata
    (p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh04-pinned-syntax-runner,
      :compiler-artifact-plan? true}
     (:plan binding)
     function
     clean-arguments)))
  (catch
   InterruptedException
   interrupted
   (.interrupt (Thread/currentThread))
   (throw interrupted))
  (catch
   StackOverflowError
   error
   (sh04-syntax-boundary-fail!
    "C3-SHAPE"
    source-path
    :bounded-syntax-host-stack
    function
    {:contained-host-error (.getName (class error))}))
  (catch
   AssertionError
   error
   (sh04-syntax-boundary-fail!
    "C3-SHAPE"
    source-path
    :contained-syntax-assertion
    function
    {:contained-host-error (.getName (class error))}))
  (catch
   LinkageError
   error
   (sh04-syntax-boundary-fail!
    "C3-SHAPE"
    source-path
    :contained-syntax-linkage
    function
    {:contained-host-error (.getName (class error))}))
  (catch
   clojure.lang.ExceptionInfo
   error
   (sh04-syntax-boundary-fail!
    "C3-SHAPE"
    source-path
    :contained-syntax-runtime-diagnostic
    function
    {:contained-diagnostic (:id (ex-data error))}))
  (catch
   Exception
   error
   (sh04-syntax-boundary-fail!
    "C3-SHAPE"
    source-path
    :contained-syntax-host-failure
    function
    {:contained-host-error (.getName (class error)),
     :cause-message (.getMessage error)}))))

(defn
 require-result-carrier!
 [source-path
  carrier
  value
  {:keys
   [p15-s23-trusted-carrier-validation sh04-syntax-boundary-fail!]}]
 (let
  [validation
   (p15-s23-trusted-carrier-validation
    value
    :default-only
    1048576
    256
    4096)]
  (when-not
   (= :passed (:status validation))
   (sh04-syntax-boundary-fail!
    "C3-SHAPE"
    source-path
    :bounded-syntax-result-carrier
    carrier
    (select-keys
     validation
     [:reason
      :observed-nodes
      :observed-depth
      :maximum-nodes
      :maximum-depth
      :maximum-width])))
  value))

(defn
 accept-result!
 [source-path
  result
  {:keys [c3-syntax-fail! sh04-syntax-require-carrier! source-span]}]
 (sh04-syntax-require-carrier!
  source-path
  :gravity-syntax-result
  result)
 (when-not
  (= :accepted (:status result))
  (let
   [diagnostic
    (first (:diagnostics result))
    rule
    (or (:rule diagnostic) "C3-SHAPE")]
   (c3-syntax-fail!
    rule
    source-path
    {:source-span
     (or
      (get-in diagnostic [:primary :span])
      (source-span source-path 0)),
     :syntax-id (get-in diagnostic [:primary :syntax-id]),
     :producer :gravity.bootstrap.syntax,
     :form-kind (get-in diagnostic [:facts :form-kind])}
    {:missing-fields
     (or
      (get-in diagnostic [:facts :missing-fields])
      [:accepted-gravity-syntax-result]),
     :facts {:gravity-diagnostic diagnostic}})))
 result)

(defn
 resolve-object-template!
 [source-path
  syntax-template
  resolved-digests
  {:keys [sh04-syntax-boundary-fail!]}]
 (when-not
  (and
   (= 2 (count resolved-digests))
   (= {:digest-ref 1} (:syntax-id syntax-template)))
  (sh04-syntax-boundary-fail!
   "C3-ID"
   source-path
   :declared-syntax-id-reference
   (:syntax-id syntax-template)
   {:resolved-digest-count (count resolved-digests)}))
 (assoc syntax-template :syntax-id (second resolved-digests)))

(defn
 resolve-stream-template!
 [source-path
  stream-template
  resolved-digests
  {:keys [sh04-syntax-boundary-fail!]}]
 (when-not
  (and
   (= 1 (count resolved-digests))
   (= {:digest-ref 0} (:artifact-id stream-template)))
  (sh04-syntax-boundary-fail!
   "C3-ID"
   source-path
   :declared-stream-artifact-id-reference
   (:artifact-id stream-template)
   {:resolved-digest-count (count resolved-digests)}))
 (assoc stream-template :artifact-id (first resolved-digests)))

(defn
 resolve-request-preimage!
 [source-path
  request
  resolved-digests
  {:keys [sh04-syntax-boundary-fail!]}]
 (let
  [ordinal
   (:ordinal request)
   dependencies
   (:depends-on request)
   preimage
   (:preimage request)]
  (case
   ordinal
   0
   (do
    (when-not
     (and (= [] dependencies) (empty? resolved-digests))
     (sh04-syntax-boundary-fail!
      "C3-ID"
      source-path
      :exact-syntax-request-zero-dependencies
      dependencies
      {:resolved-digest-count (count resolved-digests)}))
    preimage)
   1
   (do
    (when-not
     (and
      (= [0] dependencies)
      (= 1 (count resolved-digests))
      (=
       {:digest-ref 0}
       (get-in preimage [:reader-binding :semantic-binding-id])))
     (sh04-syntax-boundary-fail!
      "C3-ID"
      source-path
      :declared-reader-binding-reference
      (get-in preimage [:reader-binding :semantic-binding-id])
      {:dependencies dependencies,
       :resolved-digest-count (count resolved-digests)}))
    (assoc-in
     preimage
     [:reader-binding :semantic-binding-id]
     (first resolved-digests)))
   (sh04-syntax-boundary-fail!
    "C3-ID"
    source-path
    :known-syntax-request-ordinal
    ordinal
    {:dependencies dependencies}))))

(defn
 resolve-stream-request-preimage!
 [source-path
  request
  resolved-digests
  {:keys [sh04-syntax-boundary-fail!]}]
 (when-not
  (and
   (= 0 (:ordinal request))
   (= [] (:depends-on request))
   (empty? resolved-digests))
  (sh04-syntax-boundary-fail!
   "C3-ID"
   source-path
   :exact-syntax-stream-request-dependencies
   request
   {:resolved-digest-count (count resolved-digests)}))
 (:preimage request))

(defn
 resolve-template!
 [source-path
  binding
  raw
  reader-binding
  reader-source-revision
  {:keys
   [p15-s23-c6c10-canonical-digest
    sh04-syntax-boundary-fail!
    sh04-syntax-execute!
    sh04-syntax-raise-result!
    sh04-syntax-resolve-object-template!
    sh04-syntax-resolve-request-preimage!]}]
 (sh04-syntax-raise-result! source-path raw)
 (let
  [requests
   (:digest-requests raw)
   request-count
   (count requests)
   _
   (when-not
    (and
     (= :gravity/sh04-syntax-template-result (:artifact raw))
     (= 1 (:schema-version raw))
     (= 2 request-count)
     (=
      false
      (get-in raw [:containment :downstream-artifacts-forbidden])))
    (sh04-syntax-boundary-fail!
     "C3-SHAPE"
     source-path
     :exact-syntax-template-result
     raw
     {}))
   verification
   (sh04-syntax-execute!
    source-path
    binding
    'c3-syntax-verify-template
    [(:syntax-template raw) requests])
   _
   (when-not
    (and
     (=
      :gravity/sh04-syntax-verification-report
      (:artifact verification))
     (= :passed (:status verification))
     (=
      false
      (get-in
       verification
       [:containment :downstream-artifacts-forbidden])))
    (sh04-syntax-boundary-fail!
     "C3-ID"
     source-path
     :fresh-gravity-syntax-template-replay
     verification
     {}))
   digests
   (reduce
    (fn
     [resolved request]
     (let
      [ordinal
       (:ordinal request)
       resolved-preimage
       (sh04-syntax-resolve-request-preimage!
        source-path
        request
        resolved)]
      (when-not
       (= ordinal (count resolved))
       (sh04-syntax-boundary-fail!
        "C3-ID"
        source-path
        :ordered-syntax-digest-requests
        request
        {:resolved-count (count resolved)}))
      (conj
       resolved
       (p15-s23-c6c10-canonical-digest
        source-path
        resolved-preimage))))
    []
    requests)
   syntax
   (sh04-syntax-resolve-object-template!
    source-path
    (:syntax-template raw)
    digests)
   resolved-verification
   (sh04-syntax-execute!
    source-path
    binding
    'c3-syntax-verify-resolved
    [syntax requests digests reader-binding reader-source-revision])
   _
   (when-not
    (and
     (=
      :gravity/sh04-resolved-syntax-verification-report
      (:artifact resolved-verification))
     (= :passed (:status resolved-verification))
     (= (:semantic-binding-id reader-binding) (first digests))
     (= (:syntax-id syntax) (second digests))
     (=
      false
      (get-in
       resolved-verification
       [:containment :downstream-artifacts-forbidden])))
    (sh04-syntax-boundary-fail!
     "C3-ID"
     source-path
     :fresh-resolved-syntax-verification
     resolved-verification
     {}))]
  {:raw-result raw,
   :verification-report verification,
   :resolved-verification-report resolved-verification,
   :resolved-digests digests,
   :syntax syntax,
   :serialization-id (:syntax-id syntax)}))

(defn
 plan-binding-cache
 "Create the lazy bootstrap-lifetime cache using the owning plan builder."
 [{:keys [sh04-syntax-build-binding!]}]
 (delay (sh04-syntax-build-binding! "<sh04-syntax-bootstrap>")))

;; Registered-literal transport and authenticated reader views.

(defn
 registered-literal-fail!
 [source-path
  form-record
  missing-fields
  facts
  {:keys [c3-syntax-fail! source-span]}]
 (c3-syntax-fail!
  "C3-FACT-STALE"
  source-path
  {:source-span (or (:span form-record) (source-span source-path 0)),
   :producer :gravity.bootstrap.reader,
   :form-kind :tagged-literal}
  {:missing-fields missing-fields, :facts facts}))

(defn
 registered-literal-registry
 [source-path
  c2-artifact
  {:keys [sh04-syntax-registered-literal-fail!]}]
 (let
  [forms
   (:form-tree c2-artifact)
   forms-by-id
   (into {} (map (juxt :form-id identity)) forms)
   tagged-forms
   (filterv
    (fn*
     [p1__153#]
     (and
      (= :tagged-literal (:kind p1__153#))
      (contains? #{'uuid 'inst} (:tag p1__153#))))
    forms)
   literals-by-form
   (group-by :form-id (:literal-decoding-records c2-artifact))
   invocations-by-form
   (group-by
    :form-id
    (for
     [extension
      (:reader-extension-invocation-records c2-artifact)
      invocation
      (:invocations extension)]
     (assoc invocation :tag (:tag extension))))
   entries
   (mapv
    (fn
     [form-record]
     (let
      [tag
       (:tag form-record)
       payload-record
       (get forms-by-id (first (:children form-record)))
       literal-records
       (get literals-by-form (:form-id form-record) [])
       invocation-records
       (get invocations-by-form (:form-id form-record) [])
       literal-record
       (first literal-records)
       invocation-record
       (first invocation-records)
       decoded
       (:value form-record)
       payload
       (:value payload-record)
       canonical-text
       (case
        tag
        inst
        (when
         (= java.util.Date (class decoded))
         (let
          [expected
           (try
            (.toEpochMilli
             (.toInstant (java.time.OffsetDateTime/parse payload)))
            (catch Exception _ nil))
           observed
           (.getTime decoded)]
          (when (= expected observed) (Long/toString observed))))
        uuid
        (when
         (= java.util.UUID (class decoded))
         (let
          [expected
           (try
            (java.util.UUID/fromString payload)
            (catch Exception _ nil))
           observed
           (.toString decoded)]
          (when (= expected decoded) observed)))
        nil)
       descriptor
       (case
        tag
        inst
        {:artifact :gravity/registered-literal-value,
         :kind :tagged-literal,
         :tag 'inst,
         :canonical-value
         {:kind :instant, :epoch-milliseconds canonical-text},
         :semantic-validation :accepted}
        uuid
        {:artifact :gravity/registered-literal-value,
         :kind :tagged-literal,
         :tag 'uuid,
         :canonical-value
         {:kind :uuid, :canonical-text canonical-text},
         :semantic-validation :accepted})]
      (when-not
       (and
        (= 1 (count (:children form-record)))
        (= :string (:kind payload-record))
        (string? payload)
        (= 1 (count literal-records))
        (= 1 (count invocation-records))
        (=
         tag
         (get-in literal-record [:facts :tag])
         (:tag invocation-record))
        (= decoded (:decoded literal-record))
        (=
         (:raw form-record)
         (:raw literal-record)
         (:raw invocation-record))
        (=
         (:span form-record)
         (:span literal-record)
         (:span invocation-record))
        (string? canonical-text))
       (sh04-syntax-registered-literal-fail!
        source-path
        form-record
        [:authenticated-registered-literal-occurrence]
        {:tag tag, :form-id (:form-id form-record)}))
      {:lookup-key [tag canonical-text],
       :descriptor descriptor,
       :binding
       {:form-id (:form-id form-record),
        :literal-id (:literal-id literal-record),
        :tag tag,
        :raw (:raw form-record),
        :payload payload,
        :descriptor descriptor}}))
    tagged-forms)
   expected-form-ids
   (set (map :form-id tagged-forms))
   invoked-form-ids
   (set
    (for
     [[form-id records]
      invocations-by-form
      :when
      (some
       (fn* [p1__154#] (contains? #{'uuid 'inst} (:tag p1__154#)))
       records)]
     form-id))]
  (when-not
   (= expected-form-ids invoked-form-ids)
   (sh04-syntax-registered-literal-fail!
    source-path
    nil
    [:registered-literal-invocation-bijection]
    {:expected-form-ids expected-form-ids,
     :invoked-form-ids invoked-form-ids}))
  {:lookup
   (reduce
    (fn
     [lookup {:keys [lookup-key descriptor]}]
     (if-let
      [current (get lookup lookup-key)]
      (if
       (= current descriptor)
       lookup
       (sh04-syntax-registered-literal-fail!
        source-path
        nil
        [:unambiguous-registered-literal-descriptor]
        {:lookup-key lookup-key}))
      (assoc lookup lookup-key descriptor)))
    {}
    entries),
   :bindings (mapv :binding entries)}))

(defn
 project-registered-literal-values
 [source-path
  registry
  value
  {:keys [sh04-syntax-registered-literal-fail!]}]
 (letfn
  [(project
    [item]
    (cond
     (= java.util.Date (class item))
     (or
      (get-in
       registry
       [:lookup ['inst (Long/toString (.getTime item))]])
      (sh04-syntax-registered-literal-fail!
       source-path
       nil
       [:registered-instant-bound-to-reader-occurrence]
       {}))
     (= java.util.UUID (class item))
     (or
      (get-in registry [:lookup ['uuid (.toString item)]])
      (sh04-syntax-registered-literal-fail!
       source-path
       nil
       [:registered-uuid-bound-to-reader-occurrence]
       {}))
     (map? item)
     (into
      (empty item)
      (map (fn [[key child]] [(project key) (project child)]))
      item)
     (vector? item)
     (mapv project item)
     (set? item)
     (into #{} (map project) item)
     (seq? item)
     (apply list (map project item))
     :else
     item))]
  (project value)))

(defn
 registered-literal-projection-authentic?
 [c2-view
  gravity-boundary
  {:keys
   [p15-s23-c6c10-canonical-digest
    p15-s23-trusted-carrier-validation
    sh04-syntax-strip-host-metadata]}]
 (try
  (let
   [projection
    (:registered-literal-projection c2-view)
    bindings
    (:bindings projection)
    source-path
    (get-in c2-view [:source-unit-record :path])
    expected-projection-id
    (p15-s23-c6c10-canonical-digest
     source-path
     {:domain :gravity/sh04-registered-literal-projection-v1,
      :bindings bindings})
    forms-by-id
    (into {} (map (juxt :form-id identity)) (:form-tree c2-view))
    literals-by-form
    (group-by :form-id (:literal-decoding-records c2-view))
    invocations-by-form
    (group-by
     :form-id
     (for
      [extension
       (:reader-extension-invocation-records c2-view)
       invocation
       (:invocations extension)]
      (assoc invocation :tag (:tag extension))))
    tagged-form-ids
    (set
     (map
      :form-id
      (filter
       (fn*
        [p1__155#]
        (and
         (= :tagged-literal (:kind p1__155#))
         (contains? #{'uuid 'inst} (:tag p1__155#))))
       (:form-tree c2-view))))
    binding-form-ids
    (set (map :form-id bindings))
    provenance
    (:reader-authentication-provenance gravity-boundary)
    carrier-validation
    (p15-s23-trusted-carrier-validation
     (sh04-syntax-strip-host-metadata c2-view)
     :default-only
     1048576
     256
     4096)]
   (and
    (=
     #{:reader-source-revision
       :bindings
       :schema-version
       :reader-binding
       :upstream-product-binding
       :upstream-integrity-hash
       :projection-id
       :artifact
       :upstream-artifact-id}
     (set (keys projection)))
    (=
     :gravity/sh04-registered-literal-projection
     (:artifact projection))
    (= 1 (:schema-version projection))
    (= expected-projection-id (:projection-id projection))
    (= :passed (:status carrier-validation))
    (= tagged-form-ids binding-form-ids)
    (= (count bindings) (count binding-form-ids))
    (every?
     (fn
      [{:keys [form-id literal-id tag raw payload descriptor]}]
      (let
       [form-record
        (get forms-by-id form-id)
        payload-record
        (get forms-by-id (first (:children form-record)))
        literal-records
        (get literals-by-form form-id [])
        invocation-records
        (get invocations-by-form form-id [])
        literal-record
        (first literal-records)
        invocation-record
        (first invocation-records)]
       (and
        (= :tagged-literal (:kind form-record))
        (=
         tag
         (:tag form-record)
         (get-in literal-record [:facts :tag])
         (:tag invocation-record))
        (= 1 (count (:children form-record)))
        (= :string (:kind payload-record))
        (= payload (:value payload-record))
        (= 1 (count literal-records))
        (= literal-id (:literal-id literal-record))
        (= 1 (count invocation-records))
        (=
         raw
         (:raw form-record)
         (:raw literal-record)
         (:raw invocation-record))
        (=
         descriptor
         (:value form-record)
         (:decoded literal-record)))))
     bindings)
    (=
     (:upstream-artifact-id projection)
     (:actual-c2-artifact-id provenance))
    (=
     (:upstream-integrity-hash projection)
     (:actual-reader-product-integrity-hash provenance))
    (=
     (:upstream-product-binding projection)
     (:actual-sh03-semantic-product-binding provenance))
    (=
     (:reader-binding projection)
     (:reader-semantic-binding gravity-boundary))
    (=
     (:reader-source-revision projection)
     (:reader-source-revision gravity-boundary))))
  (catch
   InterruptedException
   interrupted
   (.interrupt (Thread/currentThread))
   (throw interrupted))
  (catch Throwable _ false)))

(defn
 current-reader-identity-view
 [source-path
  c2-artifact
  {:keys
   [sh04-syntax-project-registered-literal-values
    sh04-syntax-registered-literal-registry]}]
 (if
  (:registered-literal-projection c2-artifact)
  c2-artifact
  (let
   [registry
    (sh04-syntax-registered-literal-registry source-path c2-artifact)]
   (if
    (seq (:bindings registry))
    (sh04-syntax-project-registered-literal-values
     source-path
     registry
     c2-artifact)
    c2-artifact))))

(defn
 current-reader-revision-authentic?
 [c2-artifact
  boundary
  invocation
  {:keys
   [p15-s23-stage2-sh02-descriptor-envelope-verify!
    sh03-reader-expected-source-content-hash
    sh03-reader-verification-invocation!
    sh03-reader-verifier
    sh04-syntax-reader-binding
    sh04-syntax-semantic-source-id]}]
 (if
  (nil? boundary)
  true
  (let
   [retained
    (or
     (get-in
      c2-artifact
      [:sh03-reader-authentication :reader-source-revision])
     (when
      (=
       :gravity/sh03-current-source-reader-plan-binding
       (get-in
        c2-artifact
        [:gravity-reader-boundary :plan-binding :artifact]))
      (get-in
       c2-artifact
       [:gravity-reader-boundary
        :authenticated-envelope-descriptor
        :source-revision])))
    provenance
    (:reader-authentication-provenance boundary)
    descriptor
    (:actual-sh03-envelope-descriptor provenance)
    actual
    (:source-revision descriptor)
    path
    (get-in c2-artifact [:source-unit-record :path])
    context
    (when
     invocation
     (sh03-reader-verification-invocation! path invocation))]
   (if
    (or
     (some? retained)
     (contains?
      (get-in actual [:function-shapes sh03-reader-verifier])
      :semantic-hash)
     (not=
      sh03-reader-expected-source-content-hash
      (:source-content-hash actual))
     (not=
      sh03-reader-expected-source-content-hash
      (get-in
       boundary
       [:reader-source-revision :source-content-hash])))
    (and
     (map? retained)
     (= actual retained)
     (or
      (nil? invocation)
      (=
       (select-keys
        actual
        [:source-content-hash
         :source-byte-count
         :plan-semantic-hash
         :functions-semantic-hash])
       (select-keys
        (:binding context)
        [:source-content-hash
         :source-byte-count
         :plan-semantic-hash
         :functions-semantic-hash])))
     (=
      :passed
      (apply
       p15-s23-stage2-sh02-descriptor-envelope-verify!
       (cond->
        [(:actual-sh03-authenticated-envelope provenance)
         :c2-reader
         :gravity/sh03-reader-products
         descriptor
         path]
        (:envelope-options context)
        (conj (:envelope-options context)))))
     (let
      [path
       (get-in c2-artifact [:source-unit-record :path])
       expected
       (sh04-syntax-reader-binding
        path
        c2-artifact
        (sh04-syntax-semantic-source-id
         path
         (:source-unit-record c2-artifact)))]
      (and
       (=
        (:reader-binding expected)
        (:reader-semantic-binding boundary))
       (=
        (:reader-source-revision expected)
        (:reader-source-revision boundary)))))
    true))))

(defn
 reader-artifact-view
 [c2-artifact
  invocation
  {:keys
   [p15-s23-c6c10-canonical-digest
    p15-s23-trusted-carrier-validation
    sh03-c3-precomputed-c2-verify!
    sh03-reader-internal-product-authority
    sh04-syntax-current-sh03-product-binding
    sh04-syntax-project-registered-literal-values
    sh04-syntax-reader-binding
    sh04-syntax-registered-literal-registry
    sh04-syntax-semantic-source-id]}]
 (when (reader-pure-text/admission-required? c2-artifact)
   (when-not (= :passed (:status (p15-s23-trusted-carrier-validation c2-artifact :default-only)))
     (throw (ex-info "Reader admission requires native pure-text data"
                     {:id "C2-HASH" :missing-fields [:native-pure-text-admission-input]}))))
 (when (contains? c2-artifact :pure-extension-results)
   (throw (ex-info "Unsealed pure Reader results require their owning verification path"
                   {:id "C2-HASH"
                    :missing-fields [:authenticated-pure-extension-results]})))
 (when
  (or invocation (reader-pure-text/admission-required? c2-artifact))
  (sh03-c3-precomputed-c2-verify!
   sh03-reader-internal-product-authority
   (get-in c2-artifact [:source-unit-record :path])
   c2-artifact
   invocation))
 (let
  [boundary
   (:gravity-reader-boundary c2-artifact)
   source-path
   (get-in c2-artifact [:source-unit-record :path])
   registry
   (sh04-syntax-registered-literal-registry source-path c2-artifact)
   semantic-source-id
   (sh04-syntax-semantic-source-id
    source-path
    (:source-unit-record c2-artifact))
   reader-authentication
   (sh04-syntax-reader-binding
    source-path
    c2-artifact
    semantic-source-id)
   projection-bindings
   (:bindings registry)
   projection-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:domain :gravity/sh04-registered-literal-projection-v1,
     :bindings projection-bindings})
   projection
   {:reader-source-revision
    (:reader-source-revision reader-authentication),
    :bindings projection-bindings,
    :schema-version 1,
    :reader-binding (:reader-binding reader-authentication),
    :upstream-product-binding
    (sh04-syntax-current-sh03-product-binding c2-artifact),
    :upstream-integrity-hash
    (get-in c2-artifact [:reader-product-integrity :integrity-hash]),
    :projection-id projection-id,
    :artifact :gravity/sh04-registered-literal-projection,
    :upstream-artifact-id (:artifact-id c2-artifact)}
   base
   (assoc
    (select-keys
     c2-artifact
     [:kind
      :artifact-id
      :task
      :document-set
      :source-overrides
      :representation-boundary
      :capability-based-proof
      :source-unit-record
      :token-stream
      :form-tree
      :top-level-form-ids
      :parsed-semantic-values
      :syntax-seed-stream
      :reader-source-map
      :literal-decoding-records
      :semantic-error-deferment-record
      :reader-extension-invocation-records
      :reader-diagnostics
      :incremental-reader-hashes
      :reader-product-integrity])
    :sh03-reader-authentication
    (cond->
     {:reader-result-id
      (get-in
       boundary
       [:resolved-reader-result
        :incremental-reader-hashes
        :reader-result]),
      :semantic-envelope-id
      (get-in
       boundary
       [:authenticated-envelope :semantic-envelope-id]),
      :provenance-binding-id
      (get-in
       boundary
       [:authenticated-envelope :provenance-binding-id])}
     (=
      :gravity/sh03-current-source-reader-plan-binding
      (get-in boundary [:plan-binding :artifact]))
     (assoc
      :reader-source-revision
      (get-in
       boundary
       [:authenticated-envelope-descriptor :source-revision]))))]
  (if
   (seq projection-bindings)
   (sh04-syntax-project-registered-literal-values
    source-path
    registry
    (assoc base :registered-literal-projection projection))
   base)))

;; Semantic reader bindings, syntax stream construction and envelope assembly.

(defn
 logical-source-stem
 [source-path source-unit {:keys [reader-normalize-relative-path]}]
 (->
  (or
   (:project-relative-path source-unit)
   (.getName (java.io.File. source-path)))
  reader-normalize-relative-path
  (str/replace #"\.(gravity|qst)$" "")))

(defn
 semantic-source-id
 [source-path
  source-unit
  {:keys [reader-canonical-hash sh04-syntax-logical-stem]}]
 (reader-canonical-hash
  {:domain :gravity/sh04-co-canonical-source-v1,
   :logical-source-stem
   (sh04-syntax-logical-stem source-path source-unit),
   :encoding (:encoding source-unit),
   :bytes-hash (:bytes-hash source-unit),
   :reader-options (:reader-options source-unit)}))

(defn
 semantic-span
 [span semantic-source-id {:keys []}]
 {:column-end (or (:column-end span) (get-in span [:end :column])),
  :line-start (or (:line-start span) (get-in span [:start :line])),
  :file semantic-source-id,
  :source (:source span),
  :line-end (or (:line-end span) (get-in span [:end :line])),
  :scalar-end (or (:scalar-end span) (get-in span [:end :char])),
  :scalar-start (or (:scalar-start span) (get-in span [:start :char])),
  :byte-start (:byte-start span),
  :byte-end (:byte-end span),
  :column-start
  (or (:column-start span) (get-in span [:start :column]))})

(defn
 current-reader-product-binding
 [c2-artifact
  {:keys
   [c2-extension-hash-input
    c2-form-hash-input
    c2-token-hash-input
    reader-canonical-hash]}]
 {:adapter-contract
  (or
   (get-in c2-artifact [:gravity-reader-boundary :adapter-contract])
   :gravity/sh03-to-c2-reader-products-v2),
  :adapted-source-unit-id
  (get-in c2-artifact [:source-unit-record :source-id]),
  :adapted-token-stream-id
  (reader-canonical-hash
   (c2-token-hash-input (:token-stream c2-artifact))),
  :adapted-form-tree-id
  (reader-canonical-hash
   (c2-form-hash-input (:form-tree c2-artifact))),
  :adapted-extension-invocation-set-id
  (reader-canonical-hash
   (c2-extension-hash-input
    (:reader-extension-invocation-records c2-artifact)))})

(defn
 descriptor-reader-product-binding
 [descriptor {:keys [sh04-syntax-sh03-product-binding-keys]}]
 (let
  [summary
   (:value
    (some
     (fn*
      [p1__156#]
      (when (= :reader-product-identities (:name p1__156#)) p1__156#))
     (:semantic-projections descriptor)))]
  (when
   (map? summary)
   (select-keys summary sh04-syntax-sh03-product-binding-keys))))

(defn
 reader-semantic-binding
 [source-path
  c2-artifact
  semantic-source-id
  {:keys
   [c2-extension-hash-input
    c3-current-reader-identity-view
    p15-s23-c6c10-canonical-digest
    p15-s23-sh02-sha256-id?
    reader-canonical-hash
    sh03-reader-boundary-fail!
    sh03-reader-entrypoint
    sh03-reader-expected-entrypoint-semantic-hash
    sh03-reader-expected-functions-semantic-hash
    sh03-reader-expected-plan-semantic-hash
    sh03-reader-expected-source-byte-count
    sh03-reader-expected-source-content-hash
    sh03-reader-expected-verifier-semantic-hash
    sh03-reader-source-relative-path
    sh03-reader-verifier]}]
 (let
  [reader-source
   (or
    (get-in
     c2-artifact
     [:gravity-reader-boundary
      :authenticated-envelope-descriptor
      :source-revision])
    (get-in
     c2-artifact
     [:sh03-reader-authentication :reader-source-revision]))
   verifier-hash
   (get-in
    reader-source
    [:function-shapes sh03-reader-verifier :semantic-hash])
   current?
   (or
    (contains?
     (:sh03-reader-authentication c2-artifact)
     :reader-source-revision)
    (=
     :gravity/sh03-current-source-reader-plan-binding
     (get-in
      c2-artifact
      [:gravity-reader-boundary :plan-binding :artifact]))
    (and
     (some? reader-source)
     (or
      (some? verifier-hash)
      (not=
       sh03-reader-expected-source-content-hash
       (:source-content-hash reader-source)))))
   _
   (when
    (and
     (or current? (some? reader-source))
     (not
      (and
       (map? reader-source)
       (integer? (:source-byte-count reader-source))
       (<= 0 (:source-byte-count reader-source))
       (or (not current?) (p15-s23-sh02-sha256-id? verifier-hash))
       (every?
        p15-s23-sh02-sha256-id?
        (map
         reader-source
         [:source-content-hash
          :plan-semantic-hash
          :functions-semantic-hash
          :builder-semantic-hash])))))
    (sh03-reader-boundary-fail!
     source-path
     :actual-sh03-reader-source-revision
     nil
     {}))
   c2-artifact
   (if
    current?
    (c3-current-reader-identity-view source-path c2-artifact)
    c2-artifact)
   source-unit
   (:source-unit-record c2-artifact)
   token-stream
   (:token-stream c2-artifact)
   form-tree
   (:form-tree c2-artifact)
   seeds
   (:syntax-seed-stream c2-artifact)
   span-input
   (fn
    [span]
    (select-keys
     span
     [:byte-start
      :byte-end
      :scalar-start
      :scalar-end
      :line-start
      :column-start
      :line-end
      :column-end]))
   semantic-core
   {:form-tree-id
    (reader-canonical-hash
     (mapv
      (fn
       [form]
       {:form-id (:form-id form),
        :kind (:kind form),
        :children (:children form),
        :parent-form-id (:parent-form-id form),
        :raw (:raw form),
        :span (span-input (:span form))})
      form-tree)),
    :literal-records-id
    (reader-canonical-hash
     (mapv
      (fn* [p1__157#] (update p1__157# :span span-input))
      (:literal-decoding-records c2-artifact))),
    :reader-options (:reader-options source-unit),
    :deferment-records-id
    (reader-canonical-hash
     (mapv
      (fn* [p1__158#] (update p1__158# :span span-input))
      (get-in
       c2-artifact
       [:semantic-error-deferment-record :deferred-literal-records]))),
    :source-bytes-hash (:bytes-hash source-unit),
    :semantic-source-id semantic-source-id,
    :extension-records-id
    (reader-canonical-hash
     (c2-extension-hash-input
      (:reader-extension-invocation-records c2-artifact))),
    :token-stream-id
    (reader-canonical-hash
     (mapv
      (fn
       [token]
       {:token-id (:token-id token),
        :kind (:kind token),
        :raw (:raw token),
        :span (span-input (:span token))})
      token-stream)),
    :syntax-seed-stream-id
    (reader-canonical-hash
     (mapv
      (fn
       [seed]
       {:form-id (:form-id seed),
        :form (:form seed),
        :span (span-input (:span seed)),
        :metadata (:metadata seed),
        :phase (:phase seed),
        :profile (:profile seed)})
      seeds))}
   reader-result-id
   (reader-canonical-hash
    {:domain :gravity/sh04-semantic-sh03-reader-result-v1,
     :semantic-reader-products semantic-core})
   c2-artifact-id
   (reader-canonical-hash
    {:domain :gravity/sh04-semantic-c2-adapter-v1,
     :adapter-contract
     (or
      (get-in c2-artifact [:gravity-reader-boundary :adapter-contract])
      :gravity/sh03-to-c2-reader-products-v2),
     :semantic-reader-products semantic-core})
   envelope-id
   (reader-canonical-hash
    {:domain :gravity/sh04-semantic-sh03-envelope-v1,
     :source-content-hash
     (get
      reader-source
      :source-content-hash
      sh03-reader-expected-source-content-hash),
     :plan-semantic-hash
     (get
      reader-source
      :plan-semantic-hash
      sh03-reader-expected-plan-semantic-hash),
     :reader-result-id reader-result-id,
     :c2-artifact-id c2-artifact-id})
   binding-base
   (merge
    {:artifact :gravity/sh04-reader-semantic-binding,
     :schema-version 1}
    semantic-core
    {:reader-result-id reader-result-id,
     :c2-artifact-id c2-artifact-id,
     :authenticated-envelope-id envelope-id})
   binding-preimage
   (assoc
    binding-base
    :domain
    :gravity/sh04-reader-semantic-binding-v1)
   semantic-binding-id
   (p15-s23-c6c10-canonical-digest source-path binding-preimage)
   revision-base
   {:source-content-hash
    (get
     reader-source
     :source-content-hash
     sh03-reader-expected-source-content-hash),
    :authenticated-envelope-id envelope-id,
    :entry-function sh03-reader-entrypoint,
    :verifier-function sh03-reader-verifier,
    :schema-version 1,
    :c2-artifact-id c2-artifact-id,
    :entry-semantic-hash
    (get
     reader-source
     :builder-semantic-hash
     sh03-reader-expected-entrypoint-semantic-hash),
    :reader-result-id reader-result-id,
    :source-byte-count
    (get
     reader-source
     :source-byte-count
     sh03-reader-expected-source-byte-count),
    :verifier-semantic-hash
    (if
     current?
     verifier-hash
     sh03-reader-expected-verifier-semantic-hash),
    :functions-semantic-hash
    (get
     reader-source
     :functions-semantic-hash
     sh03-reader-expected-functions-semantic-hash),
    :source-language :gravity,
    :artifact :gravity/sh03-reader-source-revision,
    :semantic-binding-id semantic-binding-id,
    :owner :sh03-reader,
    :logical-source-path sh03-reader-source-relative-path,
    :plan-semantic-hash
    (get
     reader-source
     :plan-semantic-hash
     sh03-reader-expected-plan-semantic-hash)}
   revision
   (assoc
    revision-base
    :revision-id
    (p15-s23-c6c10-canonical-digest
     source-path
     {:domain :gravity/sh03-reader-source-revision-v1,
      :revision revision-base}))
   binding
   (assoc
    binding-base
    :semantic-binding-id
    semantic-binding-id
    :source-revision-id
    (:revision-id revision))]
  {:reader-binding binding, :reader-source-revision revision}))

(defn
 rich-object
 [syntax serialization-id {:keys []}]
 (->
  syntax
  (assoc
   :syntax/id
   (:syntax-id syntax)
   :identity
   {:algorithm :sha256,
    :authority :gravity.bootstrap.syntax,
    :domain :gravity/sh04-syntax-object-v1,
    :input-hash (:syntax-id syntax),
    :serialization-id serialization-id}
   :authority
   {:slice :SH-04,
    :owner :gravity-source,
    :module 'gravity.bootstrap.syntax,
    :clojure-adapter-residual? true})
  (dissoc :syntax-id :schema-version)))

(defn
 reader-expansion-history
 [source-path
  c2
  {:keys
   [c3-current-reader-identity-view
    p15-s23-c6c10-canonical-digest
    sh04-syntax-semantic-source-id
    sh04-syntax-strip-host-metadata]}]
 (let
  [view
   (c3-current-reader-identity-view source-path c2)
   source-id
   (sh04-syntax-semantic-source-id
    source-path
    (:source-unit-record view))
   roots
   (sh04-syntax-strip-host-metadata
    (c3-reader-expansions/roots view source-id))]
  {:roots roots,
   :id
   (when
    (some (fn* [p1__160#] (seq (:origins p1__160#))) roots)
    (p15-s23-c6c10-canonical-digest
     source-path
     {:domain :gravity/sh04-reader-expansion-history-v1,
      :roots roots}))}))

(defn
 reader-expansions-current?
 [syntax-stream
  c2
  boundary
  invocation
  history
  {:keys
   [p15-s23-stage2-sh02-descriptor-envelope-verify!
    sh03-reader-authenticated-envelope-options]}]
 (let
  [source-path
   (get-in c2 [:source-unit-record :path])
   descriptor
   (:authenticated-envelope-descriptor boundary)
   summary
   (:value
    (first
     (filter
      (fn* [p1__161#] (= :syntax-product-identities (:name p1__161#)))
      (:semantic-projections descriptor))))
   source-nodes
   (filterv
    (fn*
     [p1__162#]
     (not= :generated-form (get-in p1__162# [:form :kind])))
    syntax-stream)
   key
   :reader-expansion-history-id
   present?
   (some? (:id history))]
  (and
   (= present? (contains? summary key))
   (= (:id history) (get summary key))
   (or
    (not present?)
    (=
     :passed
     (apply
      p15-s23-stage2-sh02-descriptor-envelope-verify!
      (cond->
       [(:authenticated-envelope boundary)
        :c3-syntax
        :gravity/sh04-syntax-products
        descriptor
        source-path]
       invocation
       (conj
        (sh03-reader-authenticated-envelope-options
         source-path
         invocation))))))
   (c3-reader-expansions/current? (:roots history) source-nodes))))

(defn
 generated-products!
 [source-path
  binding
  base
  semantic-source-id
  reader-binding
  reader-source-revision
  {:keys
   [reader-canonical-hash
    sh04-syntax-execute!
    sh04-syntax-resolve-template!]}]
 (let
  [producer-id
   (reader-canonical-hash
    {:domain :gravity/sh04-generated-producer-v1,
     :semantic-source-id semantic-source-id,
     :input-syntax-id (:syntax-id base)})
   producer
   {:kind :macro,
    :name 'gravity.bootstrap.syntax/generated-conformance,
    :identity producer-id,
    :version "SH-04",
    :source-id semantic-source-id,
    :generated-form-id :generated-form-0}
   generated-span
   {:kind :generated, :producer-id producer-id, :ordinal 0}
   namespace-context
   (:namespace base)
   hygiene
   {:marks [:sh04/generated-mark],
    :lexical-scopes [:caller-scope :introduced-scope],
    :renames {'tmp__auto__ 'tmp__sh04__1},
    :introduced-identifiers ['tmp__sh04__1],
    :captures
    [{:identifier 'captured-binding,
      :macro-api 'gravity.syntax/capture,
      :call-site-namespace (:current namespace-context),
      :intentional? true,
      :authority-bearing? false,
      :policy-result :not-required}],
    :macro-definition-namespace 'gravity.bootstrap.syntax,
    :macro-call-site-namespace (:current namespace-context)}
   raw
   (sh04-syntax-execute!
    source-path
    binding
    'c3-generated-syntax-template
    [(:syntax-id base)
     {:kind :generated-form,
      :value '(do tmp__sh04__1),
      :raw "(do tmp__sh04__1)"}
     generated-span
     producer
     :generated-syntax-conformance
     namespace-context
     (:profile base)
     {:generated true, :source-metadata (:metadata base)}
     hygiene
     [{:producer-stage :macro-expansion,
       :fact-kind :generated-origin-checked,
       :value true,
       :version 1,
       :invalidated-by [:name-resolution]}]
     reader-binding
     reader-source-revision])]
  (sh04-syntax-resolve-template!
   source-path
   binding
   raw
   reader-binding
   reader-source-revision)))

(defn
 envelope-descriptor
 [source-path
  binding
  summary
  {:keys
   [p15-s23-c6c10-canonical-digest
    sh04-syntax-public-function-hashes
    sh04-syntax-public-function-shapes
    sh04-syntax-sealed-artifact-kind
    sh04-syntax-source-relative-path]}]
 (let
  [projection-name
   :syntax-product-identities
   fact-name
   :syntax-product-binding
   identity-name
   :syntax-result
   identity-domain
   :gravity/sh04-syntax-result-identity-v1
   evidence-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:domain :gravity/sh04-syntax-envelope-evidence-v1,
     :summary summary,
     :plan-semantic-hash (:plan-semantic-hash binding)})
   identity-preimage
   {:summary summary}
   observed-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:domain identity-domain, :semantic-input identity-preimage})
   fact-entries
   [{:syntax-result-id (:syntax-result-id summary),
     :syntax-stream-id (:syntax-stream-id summary),
     :serialization-id (:serialization-set-id summary),
     :graph-id (:graph-id summary)}]
   fact-value
   {:family fact-name, :entries fact-entries}
   artifact-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:syntax-result (:syntax-result-id summary)})]
  {:proof-composite
   {:proof-records [{:proof-id evidence-id, :status :checked}],
    :proof-certificate-table {evidence-id {:status :checked}},
    :proof-summary {:required 1, :checked 1},
    :proof-usage [{:proof-id evidence-id, :used-by :syntax-products}]},
   :effect-capability-relation
   {:effect-facts {:declared #{}, :observed #{}},
    :capability-facts {:required #{}, :granted #{}},
    :capability-proof-facts {:proof-ids [evidence-id]},
    :effect-order [],
    :provider-selections [],
    :grant-scopes []},
   :semantic-projections
   [{:name projection-name,
     :role :complete-syntax-product-identities,
     :entry-count (count summary),
     :value summary}],
   :stage :c3-syntax,
   :lineage
   [{:stage :sh04-syntax,
     :artifact-kind :gravity/sh04-syntax-object-artifact,
     :semantic-id (:syntax-result-id summary),
     :artifact-id artifact-id,
     :verification-id evidence-id,
     :relation :produced-from-gravity-syntax}],
   :identity-subjects
   [{:name identity-name,
     :domain identity-domain,
     :preimage identity-preimage,
     :observed-id observed-id}],
   :projection-contract
   {:contract-kind :gravity/sh04-syntax-product-envelope-contract,
    :contract-version 1,
    :profile :meta,
    :target :jvm,
    :required-semantic-projections [projection-name],
    :required-fact-families [fact-name],
    :required-identity-subjects [identity-name]},
   :schema-version 2,
   :source-revision
   {:source-content-hash (:source-content-hash binding),
    :builder-function 'c3-syntax-stream-build-template,
    :builder-semantic-hash
    (get
     sh04-syntax-public-function-hashes
     'c3-syntax-stream-build-template),
    :source-byte-count (:source-byte-count binding),
    :functions-semantic-hash (:functions-semantic-hash binding),
    :source-language :gravity,
    :function-shapes sh04-syntax-public-function-shapes,
    :owner :sh04-syntax,
    :logical-source-path sh04-syntax-source-relative-path,
    :plan-semantic-hash (:plan-semantic-hash binding)},
   :actual-path-provenance
   {:source-path source-path,
    :workspace-root (System/getProperty "user.dir"),
    :invocation-root (System/getProperty "user.dir")},
   :preservation
   {:requires [fact-name],
    :preserves [fact-name],
    :invalidates [],
    :regenerates [],
    :residual-checks
    [:identity-subject-equality :digest-graph-reachability]},
   :fact-transitions
   [{:name fact-name,
     :disposition :preserved,
     :input fact-value,
     :output fact-value,
     :input-count (count fact-value),
     :output-count (count fact-value),
     :evidence-ids [evidence-id]}],
   :reference-closure
   {:root-id "sh04-syntax-result",
    :node-ids ["sh04-syntax-result"],
    :proof-reference-ids [evidence-id],
    :observed-maximum-depth 0,
    :observed-node-count 1,
    :observed-edge-count 0,
    :runtime-check-reference-ids [],
    :fact-reference-ids [evidence-id],
    :edges [],
    :origin-reference-ids []},
   :artifact :gravity/private-authenticated-envelope-descriptor,
   :artifact-kind sh04-syntax-sealed-artifact-kind}))

(defn
 assemble-fresh-reader!
 [source-path
  c2-artifact
  integrity-report
  invocation
  {:keys
   [c2-top-level-products
    p15-s23-c6c10-canonical-digest
    p15-s23-stage2-sh02-descriptor-envelope
    p15-s23-stage2-sh02-descriptor-envelope-verify!
    reader-canonical-hash
    sh03-reader-authenticated-envelope-options
    sh04-syntax-adapter-contract
    sh04-syntax-boundary-fail!
    sh04-syntax-current-binding!
    sh04-syntax-descriptor-sh03-product-binding
    sh04-syntax-envelope-stage
    sh04-syntax-execute!
    sh04-syntax-generated-products!
    sh04-syntax-reader-binding
    sh04-syntax-reader-expansion-history
    sh04-syntax-registered-literal-registry
    sh04-syntax-require-carrier!
    sh04-syntax-resolve-stream-request-preimage!
    sh04-syntax-resolve-stream-template!
    sh04-syntax-resolve-template!
    sh04-syntax-rich-object
    sh04-syntax-sealed-artifact-kind
    sh04-syntax-semantic-source-id
    sh04-syntax-sh02-descriptor
    sh04-syntax-source-descriptor
    syntax-with-source-index-transaction]}]
 (let
  [ae-options
   (when
    invocation
    (sh03-reader-authenticated-envelope-options
     source-path
     invocation))
   binding
   (sh04-syntax-current-binding! source-path)
   source-unit
   (:source-unit-record c2-artifact)
   semantic-source-id
   (sh04-syntax-semantic-source-id source-path source-unit)
   reader-authentication
   (sh04-syntax-reader-binding
    source-path
    c2-artifact
    semantic-source-id)
   reader-binding
   (:reader-binding reader-authentication)
   reader-source-revision
   (:reader-source-revision reader-authentication)
   registered-literals
   (sh04-syntax-registered-literal-registry source-path c2-artifact)
   top-level-products
   (c2-top-level-products c2-artifact)
   seeds
   (:syntax-seed-stream c2-artifact)
   base-products
   (syntax-with-source-index-transaction
    c2-artifact
    sh04-syntax-source-descriptor
    (fn
     []
     (mapv
      (fn
       [seed {:keys [form-record]}]
       (let
        [descriptor
         (sh04-syntax-source-descriptor
          seed
          form-record
          c2-artifact
          integrity-report
          semantic-source-id
          reader-binding
          reader-source-revision
          registered-literals)
         raw
         (sh04-syntax-execute!
          source-path
          binding
          'c3-syntax-build-template
          [descriptor])]
        (sh04-syntax-resolve-template!
         source-path
         binding
         raw
         reader-binding
         reader-source-revision)))
      seeds
      top-level-products)))
   _
   (when
    (empty? base-products)
    (sh04-syntax-boundary-fail!
     "C3-SHAPE"
     source-path
     :nonempty-syntax-object-stream
     base-products
     {}))
   generated-product
   (sh04-syntax-generated-products!
    source-path
    binding
    (:syntax (first base-products))
    semantic-source-id
    reader-binding
    reader-source-revision)
   all-products
   (conj base-products generated-product)
   resolved-products
   (mapv
    (fn
     [product]
     {:syntax-object (:syntax product),
      :digest-requests (get-in product [:raw-result :digest-requests]),
      :resolved-digests (:resolved-digests product)})
    all-products)
   root-syntax-ids
   (mapv
    (fn* [p1__163#] (get-in p1__163# [:syntax :syntax-id]))
    base-products)
   stream-template-result
   (sh04-syntax-execute!
    source-path
    binding
    'c3-syntax-stream-build-template
    [resolved-products
     reader-binding
     reader-source-revision
     root-syntax-ids])
   _
   (sh04-syntax-require-carrier!
    source-path
    :gravity-syntax-stream-template
    stream-template-result)
   _
   (when-not
    (and
     (=
      :gravity/sh04-syntax-stream-template-result
      (:artifact stream-template-result))
     (= :accepted (:status stream-template-result))
     (= 1 (count (:digest-requests stream-template-result)))
     (=
      false
      (get-in
       stream-template-result
       [:containment :downstream-artifacts-forbidden])))
    (sh04-syntax-boundary-fail!
     "C3-ID"
     source-path
     :gravity-syntax-stream-template
     stream-template-result
     {}))
   stream-requests
   (:digest-requests stream-template-result)
   stream-digests
   (reduce
    (fn
     [resolved request]
     (let
      [ordinal
       (:ordinal request)
       resolved-preimage
       (sh04-syntax-resolve-stream-request-preimage!
        source-path
        request
        resolved)]
      (when-not
       (= ordinal (count resolved))
       (sh04-syntax-boundary-fail!
        "C3-ID"
        source-path
        :ordered-syntax-stream-digest-requests
        request
        {:resolved-count (count resolved)}))
      (conj
       resolved
       (p15-s23-c6c10-canonical-digest
        source-path
        resolved-preimage))))
    []
    stream-requests)
   resolved-stream
   (sh04-syntax-resolve-stream-template!
    source-path
    (:stream-template stream-template-result)
    stream-digests)
   _
   (sh04-syntax-require-carrier!
    source-path
    :resolved-gravity-syntax-stream
    resolved-stream)
   stream-verification
   (sh04-syntax-execute!
    source-path
    binding
    'c3-syntax-stream-verify-resolved
    [resolved-stream stream-requests stream-digests])
   _
   (sh04-syntax-require-carrier!
    source-path
    :gravity-syntax-stream-verification
    stream-verification)
   _
   (when-not
    (and
     (=
      :gravity/sh04-resolved-syntax-stream-verification-report
      (:artifact stream-verification))
     (= :passed (:status stream-verification))
     (=
      false
      (get-in
       stream-verification
       [:containment :downstream-artifacts-forbidden])))
    (sh04-syntax-boundary-fail!
     "C3-ID"
     source-path
     :fresh-resolved-syntax-stream-verification
     stream-verification
     {}))
   serialization
   (sh04-syntax-execute!
    source-path
    binding
    'c3-syntax-serialize-template
    [resolved-stream stream-requests stream-digests])
   _
   (sh04-syntax-require-carrier!
    source-path
    :gravity-syntax-stream-serialization
    serialization)
   _
   (when-not
    (= :accepted (:status serialization))
    (sh04-syntax-boundary-fail!
     "C3-SERIALIZE"
     source-path
     :gravity-syntax-stream-serialization
     serialization
     {}))
   deserialization
   (sh04-syntax-execute!
    source-path
    binding
    'c3-syntax-deserialize-template
    [(:carrier serialization)])
   _
   (sh04-syntax-require-carrier!
    source-path
    :gravity-syntax-stream-deserialization
    deserialization)
   _
   (when-not
    (and
     (= :accepted (:status deserialization))
     (=
      (:semantic-payload serialization)
      (:semantic-payload deserialization)))
    (sh04-syntax-boundary-fail!
     "C3-SERIALIZE"
     source-path
     :gravity-syntax-stream-round-trip
     deserialization
     {}))
   serialization-id
   (p15-s23-c6c10-canonical-digest
    source-path
    (:payload-id-request serialization))
   resolved-syntax
   (:syntax-object-stream resolved-stream)
   rich-syntax
   (mapv
    sh04-syntax-rich-object
    resolved-syntax
    (repeat serialization-id))
   graph-report
   (:graph-verification-report resolved-stream)
   syntax-result-id
   (:artifact-id resolved-stream)
   expansion-history
   (sh04-syntax-reader-expansion-history source-path c2-artifact)
   summary
   (cond->
    {:serialization-set-id serialization-id,
     :reader-binding-id (reader-canonical-hash reader-binding),
     :reader-source-revision-id (:revision-id reader-source-revision),
     :status :accepted,
     :slice :SH-04,
     :syntax-stream-id syntax-result-id,
     :semantic-source-id semantic-source-id,
     :graph-id (reader-canonical-hash graph-report),
     :adapter-contract sh04-syntax-adapter-contract,
     :syntax-result-id syntax-result-id}
    (:id expansion-history)
    (assoc :reader-expansion-history-id (:id expansion-history)))
   descriptor
   (sh04-syntax-sh02-descriptor source-path binding summary)
   envelope
   (apply
    p15-s23-stage2-sh02-descriptor-envelope
    (cond->
     [sh04-syntax-envelope-stage
      sh04-syntax-sealed-artifact-kind
      descriptor
      source-path]
     ae-options
     (conj ae-options)))
   _
   (apply
    p15-s23-stage2-sh02-descriptor-envelope-verify!
    (cond->
     [envelope
      sh04-syntax-envelope-stage
      sh04-syntax-sealed-artifact-kind
      descriptor
      source-path]
     ae-options
     (conj ae-options)))
   reader-authentication-provenance
   {:actual-sh03-semantic-product-binding
    (sh04-syntax-descriptor-sh03-product-binding
     (get-in
      c2-artifact
      [:gravity-reader-boundary :authenticated-envelope-descriptor])),
    :actual-sh03-reader-result-id
    (get-in
     c2-artifact
     [:gravity-reader-boundary
      :resolved-reader-result
      :incremental-reader-hashes
      :reader-result]),
    :actual-sh03-semantic-envelope-id
    (get-in
     c2-artifact
     [:gravity-reader-boundary
      :authenticated-envelope
      :semantic-envelope-id]),
    :actual-c2-artifact-id (:artifact-id c2-artifact),
    :semantic-source-id semantic-source-id,
    :actual-reader-source-id (:source-id source-unit),
    :actual-sh03-envelope-descriptor
    (get-in
     c2-artifact
     [:gravity-reader-boundary :authenticated-envelope-descriptor]),
    :actual-sh03-authenticated-envelope
    (get-in
     c2-artifact
     [:gravity-reader-boundary :authenticated-envelope]),
    :actual-sh03-provenance-binding-id
    (get-in
     c2-artifact
     [:gravity-reader-boundary
      :authenticated-envelope
      :provenance-binding-id]),
    :actual-reader-product-integrity-hash
    (get-in c2-artifact [:reader-product-integrity :integrity-hash])}]
  {:plan-binding (dissoc binding :plan :source-text),
   :envelope envelope,
   :descriptor descriptor,
   :rich-syntax rich-syntax,
   :reader-source-revision reader-source-revision,
   :deserialization deserialization,
   :stream-verification stream-verification,
   :reader-binding reader-binding,
   :summary summary,
   :serialization-id serialization-id,
   :stream-resolved-digests stream-digests,
   :raw-template-results (mapv :raw-result all-products),
   :stream-digest-requests stream-requests,
   :reader-authentication-provenance reader-authentication-provenance,
   :resolved-result resolved-stream,
   :serialization serialization,
   :stream-template-result stream-template-result}))

(defn
 assemble-reader!
 [source-path
  c2-artifact
  {:keys
   [c3-validate-c2-reader-artifact! sh04-syntax-build-from-fresh-c2!]}]
 (let
  [integrity-report
   (c3-validate-c2-reader-artifact! source-path c2-artifact)]
  (sh04-syntax-build-from-fresh-c2!
   source-path
   c2-artifact
   integrity-report)))

;; Source adaptation and transaction-local native lookup projections.

(defn
 empty-hygiene
 [namespace]
 {:marks [],
  :lexical-scopes [],
  :renames {},
  :introduced-identifiers [],
  :captures [],
  :macro-definition-namespace nil,
  :macro-call-site-namespace namespace})

(defn
 strip-host-metadata
 [value operations]
 (let
  [sh04-syntax-strip-host-metadata
   ^{:line 43, :column 58}
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-strip-host-metadata)
   clean
   (cond
    (map? value)
    (into
     (empty value)
     (map
      (fn
       [[key item]]
       [(sh04-syntax-strip-host-metadata key)
        (sh04-syntax-strip-host-metadata item)]))
     value)
    (vector? value)
    (mapv sh04-syntax-strip-host-metadata value)
    (set? value)
    (into #{} (map sh04-syntax-strip-host-metadata) value)
    (seq? value)
    (apply list (map sh04-syntax-strip-host-metadata value))
    :else
    value)]
  (if (instance? clojure.lang.IObj clean) (with-meta clean nil) clean)))

(defn
 descendant-form-ids
 [forms-by-id root-form-id]
 (loop
  [pending [root-form-id] visited #{}]
  (if-let
   [form-id (peek pending)]
   (if
    (contains? visited form-id)
    (recur (pop pending) visited)
    (let
     [record (get forms-by-id form-id)]
     (recur
      (into (pop pending) (:children record))
      (conj visited form-id))))
   visited)))

(defn
 source-descriptor
 [seed
  form-record
  c2-artifact
  integrity-report
  semantic-source-id
  reader-binding
  reader-source-revision
  registered-literals
  operations
  canonical-operations
  transaction-var]
 (letfn
  [(project
    []
    (let
     [c3-lossless-literal-descriptor
      (.valAt
       ^clojure.lang.ILookup operations
       :c3-lossless-literal-descriptor)
      c3-source-facts
      (.valAt ^clojure.lang.ILookup operations :c3-source-facts)
      c3-source-form-kind
      (.valAt ^clojure.lang.ILookup operations :c3-source-form-kind)
      c3-tagged-literal-descriptor
      (.valAt
       ^clojure.lang.ILookup operations
       :c3-tagged-literal-descriptor)
      sh04-syntax-descendant-form-ids
      (.valAt
       ^clojure.lang.ILookup operations
       :sh04-syntax-descendant-form-ids)
      sh04-syntax-empty-hygiene
      (.valAt
       ^clojure.lang.ILookup operations
       :sh04-syntax-empty-hygiene)
      sh04-syntax-form-index
      (.valAt ^clojure.lang.ILookup operations :sh04-syntax-form-index)
      sh04-syntax-project-registered-literal-values
      (.valAt
       ^clojure.lang.ILookup operations
       :sh04-syntax-project-registered-literal-values)
      sh04-syntax-semantic-span
      (.valAt
       ^clojure.lang.ILookup operations
       :sh04-syntax-semantic-span)
      sh04-syntax-strip-host-metadata
      (.valAt
       ^clojure.lang.ILookup operations
       :sh04-syntax-strip-host-metadata)
      span
      (sh04-syntax-semantic-span (:span seed) semantic-source-id)
      source-namespace
      (or (:namespace seed) 'gravity.user)
      namespace-context
      {:current source-namespace, :aliases {}, :imports []}
      forms-by-id
      (sh04-syntax-form-index c2-artifact)
      descendant-form-ids
      (sh04-syntax-descendant-form-ids
       forms-by-id
       (:form-id form-record))
      registered-literal-bindings
      (filterv
       (fn*
        [p1__162#]
        (contains? descendant-form-ids (:form-id p1__162#)))
       (:bindings registered-literals))
      literal-descriptor
      (or
       (c3-lossless-literal-descriptor
        seed
        form-record
        c2-artifact
        integrity-report)
       (c3-tagged-literal-descriptor
        seed
        form-record
        c2-artifact
        integrity-report))
      kind
      (c3-source-form-kind
       seed
       form-record
       c2-artifact
       integrity-report)
      host-facts
      (c3-source-facts seed form-record c2-artifact integrity-report)
      host-facts
      (cond->
       host-facts
       (seq registered-literal-bindings)
       (assoc
        :registered-literal-bindings
        registered-literal-bindings))
      expansions
      (c3-reader-expansions/project
       (:form-tree c2-artifact)
       descendant-form-ids
       seed
       semantic-source-id)
      host-facts
      (cond->
       host-facts
       (seq expansions)
       (assoc :reader-expansion-origins expansions))
      facts
      (cond->
       (dissoc
        host-facts
        :reader-product-integrity-hash
        :reader-source-id)
       (seq host-facts)
       (assoc
        :semantic-source-id
        semantic-source-id
        :reader-result-id
        (:reader-result-id reader-binding)
        :c2-artifact-id
        (:c2-artifact-id reader-binding)
        :authenticated-envelope-id
        (:authenticated-envelope-id reader-binding)))]
     (sh04-syntax-strip-host-metadata
      (sh04-syntax-project-registered-literal-values
       (get-in c2-artifact [:source-unit-record :path])
       registered-literals
       {:hygiene (sh04-syntax-empty-hygiene source-namespace),
        :reader-source-revision reader-source-revision,
        :phase (:phase seed),
        :source
        {:source-id semantic-source-id,
         :form-id (:form-id form-record),
         :token-range
         [(:open-token form-record) (:close-token form-record)]},
        :reader-binding reader-binding,
        :origin
        [{:kind :source,
          :span span,
          :producer
          {:kind :reader,
           :name 'gravity.bootstrap.reader,
           :identity (:reader-result-id reader-binding),
           :source-id semantic-source-id,
           :generated-form-id nil},
          :producer-version "SH-03",
          :input-syntax-ids [],
          :generation-reason :source-read,
          :build-effects []}],
        :form
        {:kind kind,
         :value (or literal-descriptor (:form seed)),
         :raw (or (get-in seed [:reader-origin :raw-excerpt]) "")},
        :facts
        [{:producer-stage :reader,
          :fact-kind :authenticated-reader-products,
          :value facts,
          :version 1,
          :invalidated-by
          [:macro-expansion :metadata-change :namespace-change]}],
        :version 1,
        :namespace namespace-context,
        :metadata (or (:metadata seed) {}),
        :profile (or (:profile seed) :meta),
        :span span}))))]
  ^{:line 21, :column 16}
  (if
   ^{:line 21, :column 20}
   (identical? operations canonical-operations)
   ^{:line 22, :column 18}
   (project)
   ^{:line 23, :column 18}
   (with-bindings
    {transaction-var nil}
    ^{:line 23, :column 55}
    (project)))))

(defn
 native-keyword-map?
 [value]
 (and
  (or
   (identical? clojure.lang.PersistentArrayMap (class value))
   (identical? clojure.lang.PersistentHashMap (class value)))
  (let
   [entries (.iterator (.entrySet ^java.util.Map value))]
   (loop
    []
    (if
     (.hasNext entries)
     (and
      (identical?
       clojure.lang.Keyword
       (class (.getKey ^{:tag java.util.Map$Entry} (.next entries))))
      (recur))
     true)))))

(defn
 index-roots-current?
 [roots]
 (let
  [entries (.iterator (.entrySet roots))]
  (loop
   []
   (if
    (.hasNext entries)
    (let
     [entry ^{:tag java.util.Map$Entry} (.next entries)]
     (and
      (nil?
       (.getThreadBinding ^{:tag clojure.lang.Var} (.getKey entry)))
      (identical?
       (.getValue entry)
       (.deref ^{:tag clojure.lang.Var} (.getKey entry)))
      (recur)))
    true))))

(defn
 form-index
 [c2-artifact transaction operations canonical-operations]
 (let
  [transaction
   ^{:line 27, :column 48}
   (when
    ^{:line 27, :column 54}
    (identical? operations canonical-operations)
    transaction)
   sh04-syntax-form-index-roots-current?
   ^{:line 59, :column 66}
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-form-index-roots-current?)
   sh04-syntax-native-keyword-map?
   ^{:line 60, :column 60}
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-native-keyword-map?)
   runtime-supported?
   ^{:line 61, :column 47}
   (.valAt ^clojure.lang.ILookup operations :runtime-supported?)]
  (if
   (and
    transaction
    (identical? (Thread/currentThread) (:thread transaction))
    (identical? c2-artifact (:artifact transaction))
    (sh04-syntax-form-index-roots-current?)
    (sh04-syntax-native-keyword-map? c2-artifact)
    ^{:line 55, :column 80}
    (runtime-supported?))
   (let
    [tree (:form-tree c2-artifact) previous @(:slot transaction)]
    (if
     (and previous (identical? tree (:tree previous)))
     (:index previous)
     (let
      [index (into {} (map (juxt :form-id identity)) tree)]
      (when
       (and
        (identical? clojure.lang.PersistentVector (class tree))
        (sh04-syntax-native-keyword-map? index)
        (let
         [records (.iterator ^java.lang.Iterable tree)]
         (loop
          []
          (if
           (.hasNext records)
           (let
            [record (.next records)]
            (and
             (sh04-syntax-native-keyword-map? record)
             (identical?
              clojure.lang.Keyword
              (class (:form-id record)))
             (recur)))
           true))))
       (vreset! (:slot transaction) {:tree tree, :index index}))
      index)))
   (into {} (map (juxt :form-id identity)) (:form-tree c2-artifact)))))

(defn
 literal-index
 [artifact
  cache-key
  field
  id-key
  original
  transaction
  operations
  canonical-operations]
 (let
  [transaction
   ^{:line 27, :column 48}
   (when
    ^{:line 27, :column 54}
    (identical? operations canonical-operations)
    transaction)
   sh04-syntax-form-index-roots-current?
   ^{:line 71, :column 66}
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-form-index-roots-current?)
   sh04-syntax-native-keyword-map?
   ^{:line 72, :column 60}
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-native-keyword-map?)
   runtime-supported?
   ^{:line 73, :column 47}
   (.valAt ^clojure.lang.ILookup operations :runtime-supported?)
   literal-operation-defaults
   ^{:line 74, :column 55}
   (.valAt
    ^clojure.lang.ILookup operations
    :literal-operation-defaults)
   literal-operations-current?
   ^{:line 75, :column 56}
   (.valAt
    ^clojure.lang.ILookup operations
    :literal-operations-current?)]
  (if
   (and
    transaction
    (identical? (Thread/currentThread) (:thread transaction))
    (identical? artifact (:artifact transaction))
    (sh04-syntax-form-index-roots-current?)
    (sh04-syntax-native-keyword-map? artifact)
    ^{:line 65, :column 58}
    (runtime-supported?)
    (literal-operations-current?
     ^{:line 66, :column 65}
     (literal-operation-defaults)))
   (let
    [records
     (get artifact field)
     previous
     (get @(:slot transaction) cache-key)]
    (if
     (and previous (identical? records (:records previous)))
     (:index previous)
     (let
      [index (original)]
      (when
       (and
        (sh04-syntax-form-index-roots-current?)
        (identical? clojure.lang.PersistentVector (class records))
        (sh04-syntax-native-keyword-map? index)
        (let
         [iterator (.iterator ^java.lang.Iterable records)]
         (loop
          []
          (if
           (.hasNext iterator)
           (let
            [record (.next iterator)]
            (and
             (sh04-syntax-native-keyword-map? record)
             (identical?
              clojure.lang.Keyword
              (class (.valAt ^clojure.lang.ILookup record id-key)))
             (recur)))
           true))))
       (vreset!
        (:slot transaction)
        (assoc
         @(:slot transaction)
         cache-key
         {:records records, :index index})))
      index)))
   (original))))

(doseq
 [operation
  [#'accept-result!
   #'assemble-fresh-reader!
   #'assemble-reader!
   #'boundary-fail!
   #'build
   #'build-plan-binding!
   #'current-plan-binding!
   #'current-reader-identity-view
   #'current-reader-product-binding
   #'current-reader-revision-authentic?
   #'descendant-form-ids
   #'descriptor-reader-product-binding
   #'empty-hygiene
   #'envelope-descriptor
   #'execute-plan!
   #'form-index
   #'generated-products!
   #'index-roots-current?
   #'literal-index
   #'logical-source-stem
   #'native-keyword-map?
   #'plan-binding-cache
   #'plan-identities
   #'project-registered-literal-values
   #'read-pinned-plan-source!
   #'reader-artifact-view
   #'reader-expansion-history
   #'reader-expansions-current?
   #'reader-products-authentic?
   #'reader-semantic-binding
   #'registered-literal-fail!
   #'registered-literal-projection-authentic?
   #'registered-literal-registry
   #'require-result-carrier!
   #'resolve-object-template!
   #'resolve-plan-source-path
   #'resolve-request-preimage!
   #'resolve-stream-request-preimage!
   #'resolve-stream-template!
   #'resolve-template!
   #'rich-object
   #'semantic-source-id
   #'semantic-span
   #'source-descriptor
   #'strip-host-metadata
   #'verify]]
 ^{:line 103, :column 39}
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
