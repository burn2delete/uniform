(ns gravity.syntax
  "Stage0 syntax pipeline with explicit seed dependencies.

  The owning Gravity syntax implementation and existing authentication ports
  retain their authority. Supplied operations are trusted seed composition,
  not user-provided proof of reader or syntax authenticity."
  (:require [gravity.c3-syntax-verification :as verification]))

(def ^:private namespace-contract
 '{:public-api
  {build
   {:arglists ([request operations]),
    :returns :gravity/stage0-c3-syntax-object-artifact},
   verify
   {:arglists ([source-path artifact operations]), :returns :complete},
   reader-products-authentic?
   {:arglists
    ([syntax-stream
      c2-artifact
      gravity-boundary
      invocation
      operations]),
    :returns :boolean}},
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
    :sh04-syntax-adapter-contract]},
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
   :seed-authentication-operations],
  :namespace gravity.syntax,
  :release-authority? false,
  :diagnostic-ownership
  {:owns
   [:syntax-pipeline-validation-order
    :reader-syntax-association-refusal],
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
   :gravity/syntax-verification-report],
  :clojure-seed-boundary? true,
  :contract-boundary
  :stage0-syntax-construction-and-verification-pipeline})

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

(doseq [operation [#'build #'reader-products-authentic? #'verify]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
