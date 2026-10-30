(ns gravity.syntax
  "Stage0 syntax pipeline with explicit seed dependencies.

  The owning Gravity syntax implementation and existing authentication ports
  retain their authority. Supplied operations are trusted seed composition,
  not user-provided proof of reader or syntax authenticity."
  (:require [gravity.c3-syntax-verification :as verification]))

(def ^:private namespace-contract
 '{:public-api
 {resolve-stream-request-preimage!
  {:arglists ([source-path request resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-digest-preimage},
  resolve-request-preimage!
  {:arglists ([source-path request resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-digest-preimage},
  resolve-stream-template!
  {:arglists
   ([source-path stream-template resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-syntax-stream},
  current-plan-binding!
  {:arglists ([request-source binding-cache operations]),
   :operation-inputs
   [:sh04-syntax-boundary-fail!
    :sh04-syntax-plan-identities
    :sh04-syntax-read-pinned-source!],
   :returns :gravity/sh04-pinned-syntax-plan-binding},
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
  accept-result!
  {:arglists ([source-path result operations]),
   :operation-inputs
   [:c3-syntax-fail! :sh04-syntax-require-carrier! :source-span],
   :returns :accepted-gravity-result},
  reader-products-authentic?
  {:arglists
   ([syntax-stream
     c2-artifact
     gravity-boundary
     invocation
     operations]),
   :returns :boolean},
  plan-identities
  {:arglists ([plan operations]),
   :operation-inputs
   [:p15-s23-c11-mir-digest
    :p15-s23-stage2-compiler-artifact-semantic-input
    :sh04-syntax-public-function-hashes
    :sh04-syntax-public-function-shapes],
   :returns :plan-identity-map},
  plan-binding-cache
  {:arglists ([operations]),
   :returns :delayed-plan-binding,
   :operation-inputs [:sh04-syntax-build-binding!]},
  execute-plan!
  {:arglists ([source-path binding function arguments operations]),
   :operation-inputs
   [:p15-s23-stage2-runtime-execute-function
    :sh04-syntax-boundary-fail!
    :sh04-syntax-strip-host-metadata],
   :returns :gravity-runtime-value},
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
  resolve-object-template!
  {:arglists
   ([source-path syntax-template resolved-digests operations]),
   :operation-inputs [:sh04-syntax-boundary-fail!],
   :returns :resolved-syntax-object},
  build
  {:arglists ([request operations]),
   :returns :gravity/stage0-c3-syntax-object-artifact},
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
  {:arglists ([source-path artifact operations]), :returns :complete}},
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
   :sh04-syntax-public-function-shapes]},
 :bootstrap-hosted? true,
 :dependency-direction
 {:requires [clojure.core gravity.c3-syntax-verification],
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
  :serialization],
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
  :digest-requests],
 :namespace gravity.syntax,
 :release-authority? false,
 :diagnostic-ownership
 {:owns
  [:syntax-pipeline-validation-order
   :reader-syntax-association-refusal
   :pinned-plan-authentication
   :runtime-containment
   :digest-resolution],
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
  :resolved-syntax-template],
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

(doseq [operation [#'build #'reader-products-authentic? #'verify
                     #'boundary-fail!
                     #'resolve-plan-source-path
                     #'read-pinned-plan-source!
                     #'plan-identities
                     #'build-plan-binding!
                     #'current-plan-binding!
                     #'execute-plan!
                     #'require-result-carrier!
                     #'accept-result!
                     #'resolve-object-template!
                     #'resolve-stream-template!
                     #'resolve-request-preimage!
                     #'resolve-stream-request-preimage!
                     #'resolve-template!
                     #'plan-binding-cache
                     ]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
