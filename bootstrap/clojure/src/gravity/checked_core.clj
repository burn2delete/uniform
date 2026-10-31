(ns
 gravity.checked-core
 "Current checked-core source and plan runtime with explicit temporary seed operations.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {module-assembly-manifest
    {:arglists ([lineage fragment-manifest _operations]),
     :operation-inputs [],
     :admission :none},
    request-preflight!
    {:arglists [[request operations]],
     :operation-inputs
     [:p15-s23-c6c10-carrier-validation
      :reader-canonical-hash
      :sh07-core-projection-binding-input
      :sh07-core-request-diagnostic!],
     :admission :none},
    semantic-mutation
    {:arglists ([semantic-projection-id mutation operations]),
     :operation-inputs [:sh07-core-semantic-projection-id],
     :admission :none},
    current-plan-binding!
    {:arglists ([operations]),
     :operation-inputs
     [:input :sh07-core-build-binding! :sh07-core-current-source-key],
     :admission :none},
    root-syntax-id
    {:arglists [[source-revision-id ordinal form operations]],
     :operation-inputs
     [:reader-canonical-hash :sh05-path-neutral-semantic-value],
     :admission :none},
    run-structural-request-for-test
    {:arglists [[authenticated-request operations]],
     :operation-inputs
     [:input
      :with-execution-binding
      :sh07-core-canonical-artifact
      :sh07-core-current-source-key
      :sh07-core-digest-requests
      :sh07-core-execute!
      :sh07-core-raise-diagnostic!
      :sh07-core-request-preflight!
      :sh07-core-resolve-result],
     :admission :none,
     :test-only? true,
     :authenticates-sh06-membership? false},
    canonical-artifact
    {:arglists ([resolved-template operations]),
     :operation-inputs [:sh07-core-semantic-identity-preimage],
     :admission :none},
    execute!
    {:arglists ([source-path function arguments operations]),
     :operation-inputs
     [:input
      :p15-s23-c6c10-carrier-validation
      :p15-s23-stage2-runtime-execute-function
      :sh04-syntax-strip-host-metadata],
     :admission :none},
    alias-name
    {:arglists ([resolution _operations]),
     :operation-inputs [],
     :admission :none},
    run-request-for-test
    {:arglists
     [[resolution-artifact authenticated-request operations]
      [resolution-artifact
       authenticated-request
       invocation
       operations]],
     :operation-inputs
     [:sh07-core-run-request-for-test
      :sh03-reader-authenticated-envelope-options
      :sh06-resolution-artifact-verification
      :sh07-core-authenticated-request
      :sh07-core-request-diagnostic!
      :sh07-core-run-structural-request-for-test
      :sh07-core-source-path-from-resolution],
     :admission :none,
     :test-only? true,
     :authenticates-sh06-membership? true},
    children
    {:arglists [[value _operations]],
     :operation-inputs [],
     :admission :none},
    resolve-reference-vector!
    {:arglists [[source-path values resolved-digests operations]],
     :operation-inputs [:sh07-core-resolve-reference!],
     :admission :none},
    binding-order
    {:arglists ([_module trees bindings _operations]),
     :operation-inputs [],
     :admission :none},
    resolve-digest-preimage!
    {:arglists
     ([source-path purpose preimage resolved-digests operations]),
     :operation-inputs
     [:sh07-core-execute!
      :sh07-core-resolve-reference-vector!
      :sh07-core-semantic-identity-preimage],
     :admission :none},
    semantic-var-reference
    {:arglists [[semantic-projection-id reference operations]],
     :operation-inputs [:sh07-core-semantic-projection-id],
     :admission :none},
    projection-binding-input
    {:arglists ([request operations]),
     :operation-inputs [:sh05-path-neutral-semantic-value],
     :admission :none},
    fragment-manifest
    {:arglists
     ([lineage module trees bindings resolutions aliases operations]),
     :operation-inputs [:sh07-b13-fragment-record],
     :admission :none},
    source-map-order
    {:arglists [[resolution-artifact syntaxes trees operations]],
     :operation-inputs
     [:sh07-core-projected-source-mismatch
      :sh07-core-source-occurrence-context],
     :admission :none},
    fragment-coverage
    {:arglists
     ([_module
       forms
       _bindings
       resolutions
       fragment-manifest
       _operations]),
     :operation-inputs [],
     :admission :none},
    lineage
    {:arglists ([resolution-artifact operations]),
     :operation-inputs
     [:sh07-core-lineage-with-semantic-trace
      :sh07-core-semantic-macro-trace],
     :admission :none},
    decimal-descriptor
    {:arglists
     [[source-path value evidence projection-form-id operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    request-diagnostic!
    {:arglists ([request rule-specific operations]),
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input],
     :admission :none},
    semantic-error-handler
    {:arglists ([semantic-projection-id handler operations]),
     :operation-inputs [:sh07-core-semantic-projection-id],
     :admission :none},
    semantic-binding
    {:arglists [[semantic-projection-id binding _operations]],
     :operation-inputs [],
     :admission :none},
    semantic-error-transfer
    {:arglists ([semantic-projection-id transfer operations]),
     :operation-inputs [:sh07-core-semantic-projection-id],
     :admission :none},
    decimal-evidence
    {:arglists [[resolution-artifact operations]],
     :operation-inputs [:p15-s23-c6c10-literal-scalar-descriptor],
     :admission :none},
    digest-requests
    {:arglists ([source-path digest-requests operations]),
     :operation-inputs
     [:p15-s23-c6c10-carrier-validation
      :reader-canonical-hash
      :sh07-core-resolve-digest-preimage!],
     :admission :none},
    source-key
    {:arglists ([operations]),
     :operation-inputs
     [:c-backend-resolve-p15-s23-compiler-source-path
      :p15-s23-stage2-compiler-artifact-source-path
      :sh07-core-source-path
      :sha256-bytes-hex],
     :admission :none},
    macro-trace
    {:arglists [[lineage sh05-step operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    fragment-record
    {:arglists
     ([_lineage
       _module
       ordinal
       tree
       bindings
       resolutions
       aliases
       operations]),
     :operation-inputs [:sh07-b13-alias-name],
     :admission :none},
    semantic-resolution
    {:arglists [[resolution _operations]],
     :operation-inputs [],
     :admission :none},
    semantic-identity-preimage
    {:arglists ([preimage operations]),
     :operation-inputs
     [:sh07-core-semantic-binding
      :sh07-core-semantic-error-handler
      :sh07-core-semantic-error-transfer
      :sh07-core-semantic-mutation
      :sh07-core-semantic-node
      :sh07-core-semantic-projection-id
      :sh07-core-semantic-resolution
      :sh07-core-semantic-var-reference],
     :admission :none},
    public-decimal-presentation
    {:arglists ([core _operations]),
     :operation-inputs [],
     :admission :none},
    value-kind
    {:arglists [[value _operations]],
     :operation-inputs [],
     :admission :none},
    build-form-tree
    {:arglists
     [[source-path
       source-revision-id
       decimal-evidence
       syntax
       trace
       operations]],
     :operation-inputs
     [:sh05-path-neutral-semantic-value
      :sh07-core-children
      :sh07-core-neutral-value
      :sh07-core-projected-form-id
      :sh07-core-projected-syntax-id
      :sh07-core-semantic-span
      :sh07-core-value-kind],
     :admission :none},
    resolve-reference!
    {:arglists [[source-path value resolved-digests operations]],
     :operation-inputs
     [:p15-s23-sh02-sha256-id? :sh07-core-digest-reference-ordinal],
     :admission :none},
    source-path
    {:arglists ([operations]),
     :operation-inputs
     [:input :p15-s23-stage2-compiler-artifact-source-path],
     :admission :none},
    semantic-macro-trace
    {:arglists [[trace operations]],
     :operation-inputs [:sh05-path-neutral-semantic-value],
     :admission :none},
    projected-syntax-id
    {:arglists
     [[source-revision-id root-syntax-id path kind trace operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    digest-reference-ordinal
    {:arglists [[value _operations]],
     :operation-inputs [],
     :admission :none},
    source-path-from-resolution
    {:arglists [[resolution-artifact _operations]],
     :operation-inputs [],
     :admission :none},
    decimal-evidence-complete!
    {:arglists [[source-path decimal-evidence _operations]],
     :operation-inputs [],
     :admission :none},
    parameter-binding-paths
    {:arglists [[parameters parameter-path _operations]],
     :operation-inputs [],
     :admission :none},
    neutral-value
    {:arglists
     [[source-path
       source-revision-id
       root-syntax-id
       path
       decimal-evidence
       value
       operations]
      [source-path
       source-revision-id
       root-syntax-id
       path
       decimal-evidence
       value
       source-occurrences
       operations]],
     :operation-inputs
     [:p15-s23-c6c10-literal-scalar-descriptor
      :sh07-core-decimal-descriptor
      :sh07-core-neutral-value
      :sh07-core-projected-form-id],
     :admission :none},
    projected-form-id
    {:arglists
     [[source-revision-id root-syntax-id path kind operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    authenticated-request
    {:arglists
     [[resolution-artifact operations]
      [resolution-artifact context operations]
      [resolution-artifact context invocation operations]],
     :operation-inputs
     [:sh03-reader-authenticated-envelope-options
      :sh06-resolution-artifact-verification
      :sh07-core-authenticated-request
      :sh07-core-request-from-verified-upstream
      :sh07-core-source-path-from-resolution],
     :admission :none,
     :verifies-upstream-owner? true},
    semantic-span
    {:arglists [[span _operations]],
     :operation-inputs [],
     :admission :none},
    semantic-node
    {:arglists ([semantic-projection-id node operations]),
     :operation-inputs
     [:sh07-core-semantic-error-handler
      :sh07-core-semantic-error-transfer
      :sh07-core-semantic-mutation
      :sh07-core-semantic-var-reference],
     :admission :none},
    resolve-result
    {:arglists
     ([source-path
       template
       _digest-requests
       resolved-digests
       operations]),
     :operation-inputs [:sh07-core-execute!],
     :admission :none},
    semantic-projection-id
    {:arglists [[value _operations]],
     :operation-inputs [],
     :admission :none},
    build-plan-binding!
    {:arglists ([operations]),
     :operation-inputs
     [:input
      :c-backend-stage2-plan-emitter-source-rule!
      :p15-s23-stage2-compiler-artifact-plan
      :read-forms
      :sh07-core-plan-identities
      :sh07-core-source-path
      :sha256-bytes-hex],
     :admission :none},
    raise-diagnostic!
    {:arglists ([_source-path template operations]),
     :operation-inputs [:reader-canonical-hash],
     :admission :none}},
   :operation-inputs
   [:c-backend-resolve-p15-s23-compiler-source-path
    :c-backend-stage2-plan-emitter-source-rule!
    :input
    :p15-s23-c11-mir-digest
    :p15-s23-c6c10-carrier-validation
    :p15-s23-c6c10-literal-scalar-descriptor
    :p15-s23-sh02-sha256-id?
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :read-forms
    :reader-canonical-hash
    :sh03-reader-authenticated-envelope-options
    :sh03-reader-host-character-value!
    :sh04-syntax-strip-host-metadata
    :sh05-path-neutral-semantic-value
    :sh06-resolution-artifact-verification
    :sh07-b13-alias-name
    :sh07-b13-binding-order
    :sh07-b13-fragment-coverage
    :sh07-b13-fragment-manifest
    :sh07-b13-fragment-record
    :sh07-b13-module-assembly-manifest
    :sh07-core-authenticated-request
    :sh07-core-build-binding!
    :sh07-core-build-form-tree
    :sh07-core-canonical-artifact
    :sh07-core-children
    :sh07-core-current-source-key
    :sh07-core-decimal-descriptor
    :sh07-core-decimal-evidence
    :sh07-core-decimal-evidence-complete!
    :sh07-core-digest-reference-ordinal
    :sh07-core-digest-requests
    :sh07-core-execute!
    :sh07-core-lineage-with-semantic-trace
    :sh07-core-macro-trace
    :sh07-core-neutral-value
    :sh07-core-plan-identities
    :sh07-core-projected-form-id
    :sh07-core-projected-source-mismatch
    :sh07-core-projected-syntax-id
    :sh07-core-projection-binding-input
    :sh07-core-raise-diagnostic!
    :sh07-core-request-diagnostic!
    :sh07-core-request-from-verified-upstream
    :sh07-core-request-preflight!
    :sh07-core-resolution-projection!
    :sh07-core-resolve-digest-preimage!
    :sh07-core-resolve-reference!
    :sh07-core-resolve-reference-vector!
    :sh07-core-resolve-result
    :sh07-core-root-syntax-id
    :sh07-core-run-request-for-test
    :sh07-core-run-structural-request-for-test
    :sh07-core-semantic-binding
    :sh07-core-semantic-error-handler
    :sh07-core-semantic-error-transfer
    :sh07-core-semantic-identity-preimage
    :sh07-core-semantic-macro-trace
    :sh07-core-semantic-mutation
    :sh07-core-semantic-node
    :sh07-core-semantic-projection-id
    :sh07-core-semantic-resolution
    :sh07-core-semantic-span
    :sh07-core-semantic-var-reference
    :sh07-core-source-map-order
    :sh07-core-source-occurrence-context
    :sh07-core-source-path
    :sh07-core-source-path-from-resolution
    :sh07-core-value-kind
    :sha256-bytes-hex
    :with-execution-binding],
   :scoped-binding-operation :with-execution-binding,
   :dependency-direction
   {:requires [clojure.core], :forbids [gravity.bootstrap]},
   :seed-lifetime
   [:cache-atom
    :cached-binding-deref
    :scoped-execution-binding
    :configuration
    :host-operations],
   :self-hosted? false,
   :private-implementation
   {source-occurrence-context
    {:arglists [[resolution-artifact syntaxes operations]],
     :operation-inputs
     [:sh03-reader-host-character-value!
      :sh07-core-children
      :sh07-core-source-path-from-resolution
      :sh07-core-value-kind],
     :admission :none},
    projected-source-mismatch
    {:arglists [[source target operations]],
     :operation-inputs [:sh07-core-value-kind],
     :admission :none},
    lineage-with-semantic-trace
    {:arglists ([resolution-artifact semantic-trace operations]),
     :operation-inputs
     [:reader-canonical-hash :sh05-path-neutral-semantic-value],
     :admission :none},
    request-from-verified-upstream
    {:arglists
     [[resolution-artifact upstream-verification operations]],
     :operation-inputs
     [:reader-canonical-hash
      :sh07-b13-binding-order
      :sh07-b13-fragment-coverage
      :sh07-b13-fragment-manifest
      :sh07-b13-module-assembly-manifest
      :sh07-core-build-form-tree
      :sh07-core-decimal-evidence
      :sh07-core-decimal-evidence-complete!
      :sh07-core-lineage-with-semantic-trace
      :sh07-core-macro-trace
      :sh07-core-projection-binding-input
      :sh07-core-resolution-projection!
      :sh07-core-root-syntax-id
      :sh07-core-semantic-macro-trace
      :sh07-core-source-map-order
      :sh07-core-source-occurrence-context
      :sh07-core-source-path-from-resolution],
     :admission :none}},
   :governing-contracts
   [D1 D2 D3 D9 L2 C1 C3 C4 C5 C6 C15 BOOT2 BOOT8 TEST2 L1 C2 B13],
   :seed-inputs
   [:*sh07-core-execution-binding*
    :sh07-core-cached-binding
    :sh07-core-development-binding
    :sh07-core-public-function-shapes
    :sh07-core-source-relative-path],
   :unsupported
   [:canonical-c6-admission
    :lowering-assembly
    :artifact-verification
    :owner-publication
    :historical-migration
    :type-effect-safety-completion
    :native-launch
    :release
    :performance
    :self-hosting
    :seed-retirement
    :backend-artifact-manifest-completion
    :diagnostic-safe-public-projection
    :diagnostic-publication
    :structural-test-runner-c5-authentication
    :test-runner-core-owner-admission],
   :artifact-inputs
   [:current-core-source
    :compiler-and-emitter-snapshots
    :plan-records
    :function-and-argument-carriers
    :owner-verified-c5-and-syntax-records
    :c2-form-occurrences
    :literal-decoding-records
    :verified-c5-semantic-records
    :projected-fragment-trees
    :bindings-resolutions-and-aliases
    :c5-artifact-and-lexical-context
    :explicit-reader-invocation
    :digest-reference-table
    :core-template-and-preimage-records
    :ordered-declared-digest-requests
    :resolved-digest-identities
    :verified-resolved-template-data
    :explicit-decimal-descriptors
    :rejected-gravity-result
    :request-rule-specific-failure-data
    :native-core-request
    :verified-c5-parent-and-reader-invocation
    :current-scoped-core-plan],
   :namespace gravity.checked-core,
   :diagnostic-ownership
   {:stage
    [:source-plan-contract
     :source-toolchain-drift
     :native-carrier-transport
     :host-containment
     :source-occurrence-bijections
     :decimal-c2-evidence
     :projected-parent-child-links
     :fragment-undeclared-alias
     :missing-upstream-context
     :fresh-upstream-premises
     :unresolved-digest-reference
     :digest-purpose-and-ordinal-legality
     :failed-gravity-digest-or-result-resolution
     :core-lowering-rejection-carrier
     :core-request-failure-carrier
     :request-shape-and-identity-preflight
     :test-request-membership
     :test-execution-revision-bookends
     :template-and-resolved-replay-failure],
    :catalog :existing-c6-catalog,
    :lowering-semantics :gravity-source},
   :artifact-outputs
   [:current-development-plan-binding
    :source-and-function-identities
    :gravity-function-results
    :projected-form-and-syntax-identities
    :form-trees
    :source-collection-order
    :macro-origin-traces
    :private-decimal-occurrence-state
    :semantic-source-lineage
    :projection-binding-inputs
    :fragment-manifests
    :fragment-coverage
    :module-assembly-records
    :c5-to-core-request
    :resolved-digest-references
    :normalized-semantic-references
    :semantic-node-mutation-and-error-records
    :normalized-core-identity-preimages
    :declared-digest-identities
    :materialized-core-template-data
    :canonical-core-candidate-data
    :explicit-host-decimal-presentation
    :c6-diagnostic-construction-records
    :request-preflight-acceptance
    :test-only-core-template-digest-and-replay-bundle],
   :clojure-seed-boundary? true,
   :contract-boundary
   :checked-core-supported-data-runtime-and-test-execution})

(defn
 source-path
 [operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-stage2-compiler-artifact-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-source-path)
   anchor
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
       (java.io.File.
        directory
        (input :sh07-core-source-relative-path))]
      (if
       (.isFile candidate)
       (.getPath candidate)
       (recur (.getParentFile directory))))))
   (input :sh07-core-source-relative-path))))

(defn
 plan-identities
 [plan operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-c11-mir-digest
   (.valAt ^clojure.lang.ILookup operations :p15-s23-c11-mir-digest)
   p15-s23-stage2-compiler-artifact-semantic-input
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-semantic-input)
   functions
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
    (keys (input :sh07-core-public-function-shapes))),
   :public-function-shapes
   (select-keys
    shapes
    (keys (input :sh07-core-public-function-shapes)))}))

(defn
 build-plan-binding!
 [operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   c-backend-stage2-plan-emitter-source-rule!
   (.valAt
    ^clojure.lang.ILookup operations
    :c-backend-stage2-plan-emitter-source-rule!)
   p15-s23-stage2-compiler-artifact-plan
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-plan)
   read-forms
   (.valAt ^clojure.lang.ILookup operations :read-forms)
   sh07-core-plan-identities
   (.valAt ^clojure.lang.ILookup operations :sh07-core-plan-identities)
   sh07-core-source-path
   (.valAt ^clojure.lang.ILookup operations :sh07-core-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source-path
   (sh07-core-source-path)
   bytes
   (java.nio.file.Files/readAllBytes
    (.toPath (java.io.File. source-path)))
   source-text
   (String. bytes java.nio.charset.StandardCharsets/UTF_8)
   source-hash
   (str "sha256:" (sha256-bytes-hex bytes))
   emitter
   (:emitter
    (c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
   plan
   (p15-s23-stage2-compiler-artifact-plan
    emitter
    source-path
    source-text)
   identities
   (sh07-core-plan-identities plan)]
  (when-not
   (and
    (= source-hash (get-in plan [:source :sha256]))
    (=
     (input :sh07-core-public-function-shapes)
     (:public-function-shapes identities))
    (=
     (set
      (concat
       '[sh07-core-contract sh07-core-diagnostic-catalog]
       (keys (input :sh07-core-public-function-shapes))))
     (set
      (some
       (fn*
        [p1__156#]
        (when (= :exports (first p1__156#)) (second p1__156#)))
       (drop 2 (first (read-forms source-path source-text))))))
    (= 'gravity.checked-core (get-in plan [:module :module]))
    (= :meta (get-in plan [:module :profile]))
    (= :jvm (get-in plan [:module :target]))
    (= #{} (get-in plan [:module :effects]))
    (= #{} (get-in plan [:module :capabilities]))
    (= :safe (get-in plan [:module :safety])))
   (throw
    (ex-info
     "SH-07 checked-core module contract changed"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :missing-fields [:sh07-core-module-contract],
      :observed
      (merge
       {:source-byte-count (alength bytes),
        :source-content-hash source-hash,
        :module (:module plan),
        :plan-source (:source plan)}
       identities)})))
  (when-not
   (=
    source-hash
    (str
     "sha256:"
     (sha256-bytes-hex
      (java.nio.file.Files/readAllBytes
       (.toPath (java.io.File. source-path))))))
   (throw
    (ex-info
     "SH-07 source changed during compilation"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :reason :checked-core-source-drift})))
  (merge
   {:source-content-hash source-hash,
    :executed-by :clojure-stage2-generic-rule-runner,
    :revision-status :development-unadmitted,
    :self-hosted? false,
    :source-path source-path,
    :source-byte-count (alength bytes),
    :artifact :gravity/sh07-current-core-plan-binding,
    :semantic-authority :gravity-source,
    :plan plan,
    :compiled-by :clojure-stage0-seed,
    :generic-bridge-residual? true}
   identities)))

(defn
 source-key
 [operations]
 (let
  [c-backend-resolve-p15-s23-compiler-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :c-backend-resolve-p15-s23-compiler-source-path)
   p15-s23-stage2-compiler-artifact-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-source-path)
   sh07-core-source-path
   (.valAt ^clojure.lang.ILookup operations :sh07-core-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source
   (sh07-core-source-path)
   compiler
   (java.io.File. (c-backend-resolve-p15-s23-compiler-source-path))
   emitter
   (java.io.File. (p15-s23-stage2-compiler-artifact-source-path))
   snapshot
   (fn
    [file]
    [(.getCanonicalPath ^java.io.File file)
     (str
      "sha256:"
      (sha256-bytes-hex
       (java.nio.file.Files/readAllBytes
        (.toPath ^java.io.File file))))])]
  {:source (snapshot (java.io.File. source)),
   :toolchain [(snapshot compiler) (snapshot emitter)]}))

(defn
 current-plan-binding!
 "Compile the current source; reuse only a binding for unchanged input bytes."
 [operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   sh07-core-build-binding!
   (.valAt ^clojure.lang.ILookup operations :sh07-core-build-binding!)
   sh07-core-current-source-key
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-current-source-key)
   cache-lock
   (input :sh07-core-development-binding)]
  (locking
   cache-lock
   (let
    [key
     (sh07-core-current-source-key)
     cached
     @(input :sh07-core-development-binding)]
    (when
     (and
      cached
      (not= (:toolchain key) (get-in cached [:key :toolchain])))
     (throw
      (ex-info
       "SH-07 compiler dependencies changed; reload the toolchain"
       {:id "C6-VERIFY",
        :stage :core-lowering,
        :reason :checked-core-toolchain-drift})))
    (if
     (= key (:key cached))
     (:binding cached)
     (let
      [binding (sh07-core-build-binding!)]
      (when-not
       (= (second (:source key)) (:source-content-hash binding))
       (throw
        (ex-info
         "SH-07 source changed before binding publication"
         {:id "C6-VERIFY",
          :stage :core-lowering,
          :reason :checked-core-source-drift})))
      (let
       [after (sh07-core-current-source-key)]
       (when-not
        (= key after)
        (throw
         (ex-info
          "SH-07 inputs changed before binding publication"
          {:id "C6-VERIFY",
           :stage :core-lowering,
           :reason
           (if
            (= (:toolchain key) (:toolchain after))
            :checked-core-source-drift
            :checked-core-toolchain-drift)}))))
      (reset!
       (input :sh07-core-development-binding)
       {:key key, :binding binding})
      binding))))))

(defn
 execute!
 [source-path function arguments operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-c6c10-carrier-validation
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-carrier-validation)
   p15-s23-stage2-runtime-execute-function
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-runtime-execute-function)
   sh04-syntax-strip-host-metadata
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-strip-host-metadata)]
  (try
   (p15-s23-c6c10-carrier-validation source-path arguments)
   (let
    [result
     (p15-s23-stage2-runtime-execute-function
      {:engine :gravity-sh07-pinned-core-runner,
       :compiler-artifact-plan? true}
      (:plan
       (or
        (input :*sh07-core-execution-binding*)
        @(input :sh07-core-cached-binding)))
      function
      (sh04-syntax-strip-host-metadata arguments))]
    (p15-s23-c6c10-carrier-validation source-path result)
    (sh04-syntax-strip-host-metadata result))
   (catch
    InterruptedException
    interrupted
    (.interrupt (Thread/currentThread))
    (throw interrupted))
   (catch
    StackOverflowError
    error
    (throw
     (ex-info
      "SH-07 core boundary rejected host stack exhaustion"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch
    OutOfMemoryError
    error
    (throw
     (ex-info
      "SH-07 core boundary rejected host memory exhaustion"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch
    AssertionError
    error
    (throw
     (ex-info
      "SH-07 core boundary contained an assertion"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch
    LinkageError
    error
    (throw
     (ex-info
      "SH-07 core boundary contained a linkage failure"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch clojure.lang.ExceptionInfo error (throw error))
   (catch
    Exception
    error
    (throw
     (ex-info
      "SH-07 core boundary contained a host failure"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))}
      error))))))

(do
 (defn
  source-path-from-resolution
  [resolution-artifact _operations]
  (or
   (get-in resolution-artifact [:provenance :source-path])
   (get-in
    resolution-artifact
    [:gravity-resolution-boundary
     :authenticated-resolution-request
     :provenance
     :actual-source-path])
   "<sh07-core-input>"))
 (defn
  semantic-span
  [span _operations]
  (select-keys
   (or (:primary span) span {})
   [:byte-start
    :byte-end
    :line-start
    :column-start
    :line-end
    :column-end
    :scalar-start
    :scalar-end]))
 (defn
  value-kind
  [value _operations]
  (cond
   (nil? value)
   :nil
   (boolean? value)
   :boolean
   (integer? value)
   :integer
   (or
    (instance? java.math.BigDecimal value)
    (instance? Double value)
    (instance? Float value))
   :decimal
   (ratio? value)
   :ratio
   (char? value)
   :character
   (string? value)
   :string
   (keyword? value)
   :keyword
   (symbol? value)
   :symbol
   (vector? value)
   :vector
   (map? value)
   :map
   (set? value)
   :set
   (seq? value)
   :list
   :else
   :unsupported))
 (defn
  children
  [value _operations]
  (cond
   (map? value)
   (vec (mapcat identity value))
   (set? value)
   (vec (sort-by pr-str value))
   (or (vector? value) (seq? value))
   (vec value)
   :else
   []))
 (defn
  decimal-descriptor
  [source-path value evidence projection-form-id operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    decimal
    ^java.math.BigDecimal value
    scale
    (long (.scale decimal))
    unscaled
    (.unscaledValue decimal)]
   (when-not
    (and
     evidence
     (= :decimal (:kind evidence))
     (= value (:decoded evidence)))
    (throw
     (ex-info
      "Decimal literal is not exactly bound to C2 evidence"
      {:id "C6-ORIGIN",
       :stage :core-lowering,
       :source-path source-path,
       :reason :exact-decimal-c2-evidence})))
   {:kind :gravity/arbitrary-decimal-literal,
    :unscaled-value (.toString unscaled),
    :scale scale,
    :literal-id
    (reader-canonical-hash
     {:domain :gravity/sh07-c2-decimal-literal-evidence-v1,
      :literal-id (:literal-id evidence),
      :form-id (:form-id evidence)}),
    :form-id
    (reader-canonical-hash
     {:domain :gravity/sh07-c2-decimal-form-evidence-v1,
      :form-id (:form-id evidence),
      :literal-id (:literal-id evidence)}),
    :token-id
    (reader-canonical-hash
     {:domain :gravity/sh07-c2-decimal-token-evidence-v1,
      :token-id (:token-id evidence),
      :literal-id (:literal-id evidence),
      :form-id (:form-id evidence)}),
    :projection-form-id projection-form-id}))
 (defn
  decimal-evidence
  [resolution-artifact operations]
  (let
   [p15-s23-c6c10-literal-scalar-descriptor
    (.valAt
     ^clojure.lang.ILookup operations
     :p15-s23-c6c10-literal-scalar-descriptor)
    records
    (get-in
     resolution-artifact
     [:sh05-macro-artifact
      :gravity-macro-boundary
      :authenticated-sh04-artifact
      :c2-reader-artifact
      :literal-decoding-records])]
   {:records
    (reduce
     (fn
      [result record]
      (if
       (instance? java.math.BigDecimal (:decoded record))
       (update
        result
        (p15-s23-c6c10-literal-scalar-descriptor (:decoded record))
        (fnil conj [])
        record)
       result))
     {}
     records),
    :next (atom {}),
    :cache (atom {})}))
 (defn
  projected-form-id
  [source-revision-id root-syntax-id path kind operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
   (reader-canonical-hash
    {:domain :gravity/sh07-form-occurrence-v1,
     :source-revision-id source-revision-id,
     :root-syntax-id root-syntax-id,
     :path path,
     :kind kind})))
 (defn
  projected-syntax-id
  [source-revision-id root-syntax-id path kind trace operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
   (cond
    (empty? path)
    root-syntax-id
    (and trace (= path [2]))
    (:introduced-fn-syntax-id trace)
    :else
    (reader-canonical-hash
     {:domain :gravity/sh07-form-syntax-v1,
      :source-revision-id source-revision-id,
      :root-syntax-id root-syntax-id,
      :path path,
      :kind kind}))))
 (defn-
  projected-source-mismatch
  [source target operations]
  (let
   [sh07-core-value-kind
    (.valAt ^clojure.lang.ILookup operations :sh07-core-value-kind)]
   (when
    source
    (let
     [value
      (:value source)
      expected-kind
      (if
       (= :character (:kind source))
       :character
       (sh07-core-value-kind value))]
     (cond
      (not= expected-kind (:kind target))
      :map-source-value-or-kind
      (and
       (not (coll? value))
       (not
        (if
         (instance? java.math.BigDecimal value)
         (and
          (=
           :gravity/arbitrary-decimal-literal
           (:kind (:value target)))
          (=
           (.scale ^java.math.BigDecimal value)
           (:scale (:value target)))
          (=
           (str (.unscaledValue ^java.math.BigDecimal value))
           (:unscaled-value (:value target))))
         (= value (:value target)))))
      :map-source-projected-literal
      :else
      nil)))))
 (defn
  neutral-value
  ([source-path
    source-revision-id
    root-syntax-id
    path
    decimal-evidence
    value
    operations]
   (let
    [sh07-core-neutral-value
     (.valAt
      ^clojure.lang.ILookup operations
      :sh07-core-neutral-value)]
    (sh07-core-neutral-value
     source-path
     source-revision-id
     root-syntax-id
     path
     decimal-evidence
     value
     nil)))
  ([source-path
    source-revision-id
    root-syntax-id
    path
    decimal-evidence
    value
    source-occurrences
    operations]
   (let
    [p15-s23-c6c10-literal-scalar-descriptor
     (.valAt
      ^clojure.lang.ILookup operations
      :p15-s23-c6c10-literal-scalar-descriptor)
     sh07-core-decimal-descriptor
     (.valAt
      ^clojure.lang.ILookup operations
      :sh07-core-decimal-descriptor)
     sh07-core-neutral-value
     (.valAt ^clojure.lang.ILookup operations :sh07-core-neutral-value)
     sh07-core-projected-form-id
     (.valAt
      ^clojure.lang.ILookup operations
      :sh07-core-projected-form-id)]
    (cond
     (= :character (get-in source-occurrences [:sources path :kind]))
     (if
      (instance? clojure.lang.IObj value)
      (with-meta value nil)
      value)
     (instance? java.math.BigDecimal value)
     (let
      [descriptor
       (p15-s23-c6c10-literal-scalar-descriptor value)
       cache-key
       [root-syntax-id path]
       cached
       (get @(:cache decimal-evidence) cache-key)]
      (or
       cached
       (let
        [ordinal
         (get @(:next decimal-evidence) descriptor 0)
         evidence
         (get-in decimal-evidence [:records descriptor ordinal])
         projection-form-id
         (sh07-core-projected-form-id
          source-revision-id
          root-syntax-id
          path
          :decimal)
         result
         (sh07-core-decimal-descriptor
          source-path
          value
          evidence
          projection-form-id)]
        (swap! (:next decimal-evidence) assoc descriptor (inc ordinal))
        (swap! (:cache decimal-evidence) assoc cache-key result)
        result)))
     (map? value)
     (into
      {}
      (map-indexed
       (fn
        [index [key child]]
        [(sh07-core-neutral-value
          source-path
          source-revision-id
          root-syntax-id
          (conj path (* 2 index))
          decimal-evidence
          key
          source-occurrences)
         (sh07-core-neutral-value
          source-path
          source-revision-id
          root-syntax-id
          (conj path (inc (* 2 index)))
          decimal-evidence
          child
          source-occurrences)]))
      value)
     (vector? value)
     (mapv
      (fn
       [index child]
       (sh07-core-neutral-value
        source-path
        source-revision-id
        root-syntax-id
        (conj path index)
        decimal-evidence
        child
        source-occurrences))
      (range)
      value)
     (set? value)
     (into
      #{}
      (map-indexed
       (fn
        [index child]
        (sh07-core-neutral-value
         source-path
         source-revision-id
         root-syntax-id
         (conj path index)
         decimal-evidence
         child
         source-occurrences)))
      (sort-by pr-str value))
     (seq? value)
     (apply
      list
      (map-indexed
       (fn
        [index child]
        (sh07-core-neutral-value
         source-path
         source-revision-id
         root-syntax-id
         (conj path index)
         decimal-evidence
         child
         source-occurrences))
       value))
     :else
     value))))
 (defn
  macro-trace
  [lineage sh05-step operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    input-id
    (:input-syntax-id sh05-step)
    output-id
    (:output-syntax-id sh05-step)
    fn-id
    (reader-canonical-hash
     {:domain :gravity/sh07-introduced-fn-syntax-v1,
      :input-syntax-id input-id,
      :output-def-syntax-id output-id})
    def-origin-id
    (reader-canonical-hash
     {:domain :gravity/sh07-def-generated-origin-v1,
      :input-syntax-id input-id,
      :output-syntax-id output-id})
    fn-origin-id
    (reader-canonical-hash
     {:domain :gravity/sh07-fn-generated-origin-v1,
      :input-syntax-id input-id,
      :fn-syntax-id fn-id})]
   {:macro-expansion-trace-id (:macro-expansion-trace-id lineage),
    :def-generated-origin-id def-origin-id,
    :sh05-artifact-id (:sh05-artifact-id lineage),
    :output-def-syntax-id output-id,
    :source-revision-id (:source-revision-id lineage),
    :macro 'defn,
    :fn-generated-origin-id fn-origin-id,
    :introduced-fn-syntax-id fn-id,
    :input-syntax-id input-id}))
 (defn
  root-syntax-id
  [source-revision-id ordinal form operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)]
   (reader-canonical-hash
    {:domain :gravity/sh07-root-syntax-v1,
     :source-revision-id source-revision-id,
     :ordinal ordinal,
     :form (sh05-path-neutral-semantic-value form)})))
 (defn
  decimal-evidence-complete!
  [source-path decimal-evidence _operations]
  (doseq
   [[descriptor records] (:records decimal-evidence)]
   (let
    [consumed (get @(:next decimal-evidence) descriptor 0)]
    (when-not
     (= consumed (count records))
     (throw
      (ex-info
       "SH-07 decimal occurrence evidence is not bijective"
       {:id "C6-ORIGIN",
        :stage :core-lowering,
        :source-path source-path,
        :reason :decimal-c2-occurrence-bijection,
        :descriptor descriptor,
        :expected-occurrences (count records),
        :observed-occurrences consumed})))))
  :passed)
 (defn
  semantic-macro-trace
  [trace operations]
  (let
   [sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)]
   (mapv
    (fn
     [ordinal step]
     {:capabilities
      (sh05-path-neutral-semantic-value (:capabilities step)),
      :diagnostics
      (sh05-path-neutral-semantic-value (:diagnostics step)),
      :build-effects
      (sh05-path-neutral-semantic-value (:build-effects step)),
      :ordinal ordinal,
      :macro (:macro step),
      :artifact :gravity/sh07-preserved-macro-expansion-step,
      :target (:target step),
      :step (:step step),
      :profile (:profile step)})
    (range)
    trace)))
 (defn-
  source-occurrence-context
  [resolution-artifact syntaxes operations]
  (let
   [sh03-reader-host-character-value!
    (.valAt
     ^clojure.lang.ILookup operations
     :sh03-reader-host-character-value!)
    sh07-core-children
    (.valAt ^clojure.lang.ILookup operations :sh07-core-children)
    sh07-core-source-path-from-resolution
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-source-path-from-resolution)
    sh07-core-value-kind
    (.valAt ^clojure.lang.ILookup operations :sh07-core-value-kind)
    source-path
    (sh07-core-source-path-from-resolution resolution-artifact)
    c2
    (get-in
     resolution-artifact
     [:sh05-macro-artifact
      :gravity-macro-boundary
      :authenticated-sh04-artifact
      :c2-reader-artifact])
    sources
    (:form-tree c2)
    source-index
    (into {} (map (juxt :form-id identity)) sources)
    literals
    (group-by :form-id (:literal-decoding-records c2))
    fail
    (fn
     [reason source]
     (throw
      (ex-info
       "SH-07 source occurrence projection is not bijective"
       {:id "C6-ORIGIN",
        :rule "C6-ORIGIN",
        :stage :core-lowering,
        :reason reason,
        :source-path source-path,
        :source-span (:span source),
        :source-form-id (:form-id source)})))]
   (when-not
    (and (vector? sources) (= (count sources) (count source-index)))
    (fail :map-source-form-id-bijection nil))
   (letfn
    [(children!
      [source]
      (let
       [ids (:children source)]
       (when-not
        (and
         (vector? ids)
         (= (count ids) (count (set ids)))
         (every?
          (fn*
           [p1__157#]
           (=
            (:form-id source)
            (:parent-form-id (get source-index p1__157#))))
          ids))
        (fail :map-source-child-link source))
       (mapv source-index ids)))
     (unwrap
      [source]
      (loop
       [source source seen #{}]
       (when
        (or (nil? source) (contains? seen (:form-id source)))
        (fail :map-source-occurrence-missing source))
       (if
        (= :metadata-wrapper (:kind source))
        (let
         [children (children! source)]
         (when-not
          (= 2 (count children))
          (fail :map-source-metadata-shape source))
         (recur (second children) (conj seen (:form-id source))))
        source)))
     (character!
      [source value]
      (let
       [records
        (get literals (:form-id source))
        record
        (first records)
        codepoint
        (if
         (char? value)
         (int value)
         (when (map? value) (:codepoint value)))
        expected
        (when
         (and
          (integer? codepoint)
          (<= 0 codepoint 1114111)
          (not (<= 55296 codepoint 57343)))
         (sh03-reader-host-character-value!
          source-path
          {:codepoint codepoint}))]
       (when-not
        (and
         (= [] (:children source))
         (= 1 (count records))
         (= :character (:kind record))
         (= value expected (:value source) (:decoded record))
         (= (:raw source) (:raw record))
         (= (:span source) (:span record)))
        (fail :character-c2-occurrence-mismatch source))))]
    (into
     {}
     (map
      (fn
       [syntax]
       (let
        [occurrences
         (atom {})
         orders
         (atom {})
         used
         (atom #{})
         remember!
         (fn
          [source path]
          (when
           (contains? @used (:form-id source))
           (fail :map-source-occurrence-duplicate source))
          (swap! used conj (:form-id source))
          (swap! occurrences assoc path source))]
        (letfn
         [(walk
           [source value path inert?]
           (let
            [source
             (unwrap source)
             kind
             (:kind source)
             children
             (children! source)]
            (remember! source path)
            (if
             (= :character kind)
             (character! source value)
             (do
              (when-not
               (or
                (= kind (sh07-core-value-kind value))
                (contains? #{:tagged-literal :abbreviation} kind))
               (fail :map-source-value-or-kind source))
              (when
               (and (not (coll? value)) (not= (:value source) value))
               (fail :map-source-projected-literal source))
              (cond
               (and
                (seq? value)
                (= 'quote (first value))
                (= 2 (count value)))
               (do
                (if
                 (= :abbreviation kind)
                 (when-not
                  (and
                   (= :quote (:abbrev source))
                   (= 1 (count children)))
                  (fail :map-source-quote-shape source))
                 (do
                  (when-not
                   (= 2 (count children))
                   (fail :map-source-quote-shape source))
                  (walk (first children) 'quote (conj path 0) inert?)))
                (walk
                 (last children)
                 (second value)
                 (conj path 1)
                 true))
               (map? value)
               (let
                [entries
                 (vec value)
                 pairs
                 (mapv vec (partition 2 children))
                 slots
                 (into
                  {}
                  (map-indexed (fn [index [key _]] [key index]))
                  entries)
                 matched
                 (mapv
                  (fn*
                   [p1__158#]
                   (get
                    slots
                    (:value (first p1__158#))
                    :gravity.bootstrap/missing))
                  pairs)]
                (when-not
                 (and
                  (even? (count children))
                  (if
                   inert?
                   true
                   (and
                    (= (count children) (* 2 (count entries)))
                    (not-any? #{:gravity.bootstrap/missing} matched)
                    (= (count matched) (count (set matched))))))
                 (fail :map-source-entry-bijection source))
                (swap! orders assoc path (when-not inert? matched))
                (doseq
                 [[index [key value]] (map-indexed vector entries)]
                 (let
                  [pair
                   (last
                    (filter
                     (fn*
                      [p1__159#]
                      (and
                       (= key (:value (first p1__159#)))
                       (= value (:value (second p1__159#)))))
                     pairs))]
                  (when-not
                   pair
                   (fail :map-source-entry-bijection source))
                  (walk
                   (first pair)
                   key
                   (conj path (* 2 index))
                   inert?)
                  (walk
                   (second pair)
                   value
                   (conj path (inc (* 2 index)))
                   inert?))))
               (set? value)
               (let
                [values
                 (sh07-core-children value)
                 slots
                 (zipmap values (range))
                 matched
                 (mapv
                  (fn*
                   [p1__160#]
                   (get
                    slots
                    (:value p1__160#)
                    :gravity.bootstrap/missing))
                  children)]
                (when-not
                 (and
                  (=
                   (count children)
                   (count values)
                   (count (set matched)))
                  (not-any? #{:gravity.bootstrap/missing} matched))
                 (fail :map-source-set-bijection source))
                (swap! orders assoc path (when-not inert? matched))
                (doseq
                 [[child index] (map vector children matched)]
                 (walk
                  child
                  (get values index)
                  (conj path index)
                  inert?)))
               (= :abbreviation kind)
               (do
                (when-not
                 (and
                  (= 1 (count children))
                  (= 2 (count value))
                  (seq? value)
                  (=
                   (first value)
                   (get
                    {:quote 'quote,
                     :syntax-quote 'syntax-quote,
                     :unquote 'unquote,
                     :splice-unquote 'splice-unquote,
                     :deref 'deref}
                    (:abbrev source))))
                 (fail :map-source-abbreviation-shape source))
                (walk
                 (first children)
                 (second value)
                 (conj path 1)
                 inert?))
               (or (vector? value) (seq? value))
               (do
                (when-not
                 (= (count children) (count value))
                 (fail :map-source-sequence-shape source))
                (doseq
                 [[index child raw]
                  (map vector (range) children value)]
                 (walk child raw (conj path index) inert?)))
               :else
               nil)))))]
         (let
          [source
           (unwrap
            (get source-index (get-in syntax [:source :form-id])))
           value
           (:form syntax)]
          (if
           (and
            (seq? (:value source))
            (= 'defn (first (:value source))))
           (let
            [children
             (children! source)
             fn-value
             (nth value 2 nil)
             trace
             (filter
              (fn*
               [p1__161#]
               (and
                (= 'defn (:macro p1__161#))
                (= (:syntax/id syntax) (:output-syntax-id p1__161#))))
              (get-in
               resolution-artifact
               [:sh05-macro-artifact :macro-expansion-trace]))]
            (when-not
             (and
              (= 1 (count trace))
              (seq? value)
              (= 'def (first value))
              (= 3 (count value))
              (seq? fn-value)
              (= 'fn (first fn-value))
              (= (dec (count children)) (count fn-value)))
             (fail :map-source-defn-transfer source))
            (remember! source [])
            (walk (get children 1) (second value) [1] false)
            (doseq
             [index (range 2 (count children))]
             (walk
              (get children index)
              (nth fn-value (dec index))
              [2 (dec index)]
              false)))
           (walk source value [] false))))
        [(:sh07/root-syntax-id syntax)
         {:sources @occurrences, :collection-orders @orders}]))
      syntaxes)))))
 (defn
  build-form-tree
  [source-path
   source-revision-id
   decimal-evidence
   syntax
   trace
   operations]
  (let
   [sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)
    sh07-core-children
    (.valAt ^clojure.lang.ILookup operations :sh07-core-children)
    sh07-core-neutral-value
    (.valAt ^clojure.lang.ILookup operations :sh07-core-neutral-value)
    sh07-core-projected-form-id
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-projected-form-id)
    sh07-core-projected-syntax-id
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-projected-syntax-id)
    sh07-core-semantic-span
    (.valAt ^clojure.lang.ILookup operations :sh07-core-semantic-span)
    sh07-core-value-kind
    (.valAt ^clojure.lang.ILookup operations :sh07-core-value-kind)
    root-id
    (:sh07/root-syntax-id syntax)
    source-span
    (sh07-core-semantic-span (:span syntax))
    trace?
    (and trace (= root-id (:output-def-syntax-id trace)))
    records
    (atom [])]
   (letfn
    [(walk
      [value path parent-form-id]
      (let
       [source
        (get-in syntax [:sh07/source-occurrences :sources path])
        kind
        (if
         (= :character (:kind source))
         :character
         (sh07-core-value-kind value))
        syntax-id
        (sh07-core-projected-syntax-id
         source-revision-id
         root-id
         path
         kind
         (when trace? trace))
        form-id
        (sh07-core-projected-form-id
         source-revision-id
         root-id
         path
         kind)
        children
        (if (= :character kind) [] (sh07-core-children value))
        child-records
        (mapv
         (fn [index child] (walk child (conj path index) form-id))
         (range)
         children)
        generated-origin
        (cond
         (and trace? (empty? path))
         [{:origin-id (:def-generated-origin-id trace),
           :kind :macro-expansion,
           :from-syntax-id (:input-syntax-id trace),
           :role :introduced-def}]
         (and trace? (= path [2]))
         [{:origin-id (:fn-generated-origin-id trace),
           :kind :macro-expansion,
           :from-syntax-id (:input-syntax-id trace),
           :role :introduced-fn}]
         :else
         [])
        record
        {:source-span source-span,
         :origin-chain [],
         :value
         (sh07-core-neutral-value
          source-path
          source-revision-id
          root-id
          path
          decimal-evidence
          value
          (:sh07/source-occurrences syntax)),
         :syntax-id syntax-id,
         :parent-form-id parent-form-id,
         :kind kind,
         :form-id form-id,
         :scope-id nil,
         :metadata
         (sh05-path-neutral-semantic-value (or (meta value) {})),
         :child-form-ids (mapv :form-id child-records),
         :generated-origin generated-origin}]
       (swap! records conj record)
       record))]
    (let
     [root (walk (:form syntax) [] nil)]
     {:root root, :records @records}))))
 (defn
  source-map-order
  [resolution-artifact syntaxes trees operations]
  (let
   [sh07-core-projected-source-mismatch
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-projected-source-mismatch)
    sh07-core-source-occurrence-context
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-source-occurrence-context)
    contexts
    (sh07-core-source-occurrence-context resolution-artifact syntaxes)
    records
    (vec (mapcat :records trees))
    projected
    (into {} (map (juxt :form-id identity)) records)
    reordered
    (atom {})
    fail
    (fn
     [reason target]
     (throw
      (ex-info
       "SH-07 projected source occurrence does not match C2"
       {:id "C6-ORIGIN",
        :rule "C6-ORIGIN",
        :stage :core-lowering,
        :reason reason,
        :projected-form-id (:form-id target)})))]
   (when-not
    (and
     (= (count records) (count projected))
     (= (count syntaxes) (count trees)))
    (fail :map-source-form-id-bijection nil))
   (doseq
    [[syntax tree] (map vector syntaxes trees)]
    (let
     [context (get contexts (:sh07/root-syntax-id syntax))]
     (letfn
      [(walk
        [id path seen]
        (let
         [target
          (get projected id)
          source
          (get (:sources context) path)
          children
          (:child-form-ids target)]
         (when-not
          (and
           target
           (not (contains? seen id))
           (vector? children)
           (= (count children) (count (set children)))
           (every?
            (fn*
             [p1__162#]
             (= id (:parent-form-id (get projected p1__162#))))
            children))
          (fail :map-projected-child-link target))
         (when
          (= :character (:kind source))
          (when-not
           (and
            (= :character (:kind target))
            (= [] children)
            (= (:value source) (:value target)))
           (fail :character-c2-occurrence-mismatch target)))
         (when-let
          [reason (sh07-core-projected-source-mismatch source target)]
          (fail reason target))
         (when
          (contains? #{:set :map} (:kind target))
          (when-not
           (= (:kind target) (:kind source))
           (fail :map-source-value-or-kind target))
          (when-let
           [order (get (:collection-orders context) path)]
           (let
            [width (if (= :map (:kind target)) 2 1)]
            (when-not
             (= (count children) (* width (count order)))
             (fail :map-source-entry-bijection target))
            (swap!
             reordered
             assoc
             id
             (vec
              (mapcat
               (fn*
                [p1__163#]
                (subvec
                 children
                 (* width p1__163#)
                 (+ width (* width p1__163#))))
               order))))))
         (doseq
          [[index child] (map-indexed vector children)]
          (walk child (conj path index) (conj seen id)))))]
      (walk (get-in tree [:root :form-id]) [] #{}))))
   (mapv
    (fn
     [tree]
     (let
      [change
       (fn
        [record]
        (if-let
         [ids (get @reordered (:form-id record))]
         (assoc record :child-form-ids ids)
         record))]
      {:root (change (:root tree)),
       :records (mapv change (:records tree))}))
    trees)))
 (defn
  parameter-binding-paths
  [parameters parameter-path _operations]
  (loop
   [remaining
    (seq (if (sequential? parameters) parameters []))
    index
    0
    result
    []]
   (if
    (empty? remaining)
    result
    (let
     [item (first remaining)]
     (cond
      (= item ':-)
      (recur (nnext remaining) (+ index 2) result)
      (= item '&)
      (recur (next remaining) (inc index) result)
      (symbol? item)
      (recur
       (next remaining)
       (inc index)
       (conj result {:name item, :path (conj parameter-path index)}))
      :else
      (recur (next remaining) (inc index) result))))))
 (defn-
 lineage-with-semantic-trace
 [resolution-artifact semantic-trace operations]
 (let
  [reader-canonical-hash
   ^{:line 6, :column 5}
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh05-path-neutral-semantic-value
   ^{:line 8, :column 5}
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-path-neutral-semantic-value)
   boundary
   ^{:line 12, :column 5}
   (:gravity-resolution-boundary resolution-artifact)
   analysis
   ^{:line 14, :column 5}
   (:resolved-analysis boundary)
   sh05
   ^{:line 16, :column 5}
   (:sh05-macro-artifact resolution-artifact)
   neutral
   sh05-path-neutral-semantic-value
   source-unit
   ^{:line 20, :column 5}
   (get-in
    sh05
    [:gravity-macro-boundary
     :authenticated-sh04-artifact
     :c2-reader-artifact
     :source-unit-record])
   source-revision-id
   ^{:line 27, :column 5}
   (:bytes-hash source-unit)
   expanded-forms
   ^{:line 29, :column 5}
   (mapv :form ^{:line 29, :column 17} (:expanded-syntax-stream sh05))
   neutral-expanded-forms
   ^{:line 31, :column 5}
   (neutral expanded-forms)
   semantic-module
   ^{:line 33, :column 5}
   (select-keys
    ^{:line 34, :column 6}
    (:module-contract analysis)
    [:namespace
     :profile
     :target
     :safety
     :effects
     :capabilities
     :exports])
   binding-semantics
   ^{:line 43, :column 5}
   (mapv
    ^{:line 44, :column 6}
    (fn*
     [p1__151#]
     ^{:line 46, :column 7}
     (select-keys
      p1__151#
      [:name
       :kind
       :namespace
       :package
       :binding-class
       :visibility
       :profile-set
       :target-set
       :type-ref
       :effects
       :capabilities
       :safety
       :semantic-span]))
    ^{:line 61, :column 6}
    (:binding-table analysis))
   resolution-semantics
   ^{:line 63, :column 5}
   (mapv
    ^{:line 64, :column 6}
    (fn*
     [p1__152#]
     ^{:line 66, :column 7}
     (select-keys
      p1__152#
      [:reference-syntax-id
       :symbol
       :position
       :resolution-order
       :semantic-span
       :resolution-kind]))
    ^{:line 74, :column 6}
    (:resolution-table analysis))
   alias-semantics
   ^{:line 76, :column 5}
   (vec ^{:line 76, :column 10} (:alias-table analysis))
   sh05-artifact-id
   ^{:line 78, :column 5}
   (reader-canonical-hash
    {:domain :gravity/sh07-semantic-sh05-artifact-v1,
     :source-revision-id source-revision-id,
     :expanded-forms neutral-expanded-forms,
     :macro-trace semantic-trace})
   semantic-projection-id
   ^{:line 85, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-semantic-sh06-artifact-v1,
     :source-revision-id source-revision-id,
     :module semantic-module,
     :expanded-forms neutral-expanded-forms,
     :bindings binding-semantics,
     :aliases alias-semantics,
     :resolutions resolution-semantics})]
  {:macro-expansion-trace-id
   ^{:line 94, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-semantic-macro-trace-v1,
     :source-revision-id source-revision-id,
     :trace semantic-trace}),
   :authenticated-envelope-id
   ^{:line 99, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-semantic-upstream-envelope-v1,
     :source-revision-id source-revision-id,
     :sh05-artifact-id sh05-artifact-id}),
   :expanded-syntax-stream-id
   ^{:line 104, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-semantic-expanded-stream-v1,
     :source-revision-id source-revision-id,
     :expanded-forms neutral-expanded-forms}),
   :sh05-artifact-id sh05-artifact-id,
   :sh06-analysis-artifact-id
   ^{:line 110, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-semantic-sh06-analysis-v1,
     :source-revision-id source-revision-id,
     :module semantic-module,
     :bindings binding-semantics,
     :aliases alias-semantics,
     :resolutions resolution-semantics}),
   :resolution-table-id
   ^{:line 118, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-sh06-resolution-table-v1,
     :resolutions resolution-semantics}),
   :binding-table-id
   ^{:line 122, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-sh06-binding-table-v1,
     :bindings binding-semantics}),
   :alias-table-id
   ^{:line 126, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-sh06-alias-table-v1,
     :aliases alias-semantics}),
   :source-revision-id source-revision-id,
   :sh06-semantic-projection-id semantic-projection-id,
   :sh06-artifact-id semantic-projection-id,
   :lexical-scope-graph-id
   ^{:line 133, :column 6}
   (reader-canonical-hash
    {:domain :gravity/sh07-sh06-lexical-scope-graph-v1,
     :source-revision-id source-revision-id,
     :graph
     ^{:line 137, :column 8}
     (neutral
      ^{:line 138, :column 9}
      (select-keys
       ^{:line 139, :column 10}
       (:lexical-scope-graph analysis)
       [:scope-count :edge-count :status]))}),
   :authenticated-sh06-artifact-id
   ^{:line 142, :column 6}
   (:artifact-id resolution-artifact)}))
 (defn
  lineage
  [resolution-artifact operations]
  (let
   [sh07-core-lineage-with-semantic-trace
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-lineage-with-semantic-trace)
    sh07-core-semantic-macro-trace
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-semantic-macro-trace)
    sh05
    (:sh05-macro-artifact resolution-artifact)]
   (sh07-core-lineage-with-semantic-trace
    resolution-artifact
    (sh07-core-semantic-macro-trace (:macro-expansion-trace sh05)))))
 (defn
  projection-binding-input
  [request operations]
  (let
   [sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)]
   {:domain :gravity/sh07-authenticated-sh06-core-projection-v15,
    :request
    (->
     request
     (dissoc :projection-binding :provenance)
     sh05-path-neutral-semantic-value)}))
 (defn
  alias-name
  [resolution _operations]
  (when
   (= :alias-qualified-required-binding (:resolution-order resolution))
   (some-> (:symbol resolution) namespace symbol)))
 (defn
  fragment-record
  [_lineage
   _module
   ordinal
   tree
   bindings
   resolutions
   aliases
   operations]
  (let
   [sh07-b13-alias-name
    (.valAt ^clojure.lang.ILookup operations :sh07-b13-alias-name)
    forms
    (:records tree)
    form-ids
    (mapv :form-id forms)
    syntax-ids
    (set (map :syntax-id forms))
    local-bindings
    (filterv
     (fn*
      [p1__153#]
      (contains? syntax-ids (:definition-syntax-id p1__153#)))
     bindings)
    local-binding-ids
    (mapv :binding-id local-bindings)
    local-binding-id-set
    (set local-binding-ids)
    fragment-resolutions
    (filterv
     (fn*
      [p1__154#]
      (contains? syntax-ids (:reference-syntax-id p1__154#)))
     resolutions)
    external-binding-ids
    (->>
     fragment-resolutions
     (map :binding-id)
     (remove local-binding-id-set)
     distinct
     vec)
    alias-names
    (->> fragment-resolutions (keep sh07-b13-alias-name) distinct vec)
    available-aliases
    (set (map :alias aliases))
    root-form-id
    (get-in tree [:root :form-id])
    root-node-id
    root-form-id
    content
    {:ordinal ordinal,
     :root-form-ids [root-form-id],
     :form-ids form-ids,
     :local-binding-ids local-binding-ids,
     :external-binding-ids external-binding-ids,
     :resolution-reference-syntax-ids
     (mapv :reference-syntax-id fragment-resolutions),
     :alias-names alias-names,
     :root-node-ids [root-node-id]}
    content-id
    root-form-id
    fragment-id
    root-form-id]
   (when-not
    (every? available-aliases alias-names)
    (throw
     (ex-info
      "SH-07-B13 fragment referenced an undeclared alias"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :reason :fragment-undeclared-alias,
       :ordinal ordinal,
       :alias-names alias-names})))
   (assoc content :fragment-id fragment-id :content-id content-id)))
 (defn
  fragment-manifest
  [lineage module trees bindings resolutions aliases operations]
  (let
   [sh07-b13-fragment-record
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-b13-fragment-record)]
   (mapv
    (fn*
     [p1__155# p2__156#]
     (sh07-b13-fragment-record
      lineage
      module
      p1__155#
      p2__156#
      bindings
      resolutions
      aliases))
    (range)
    trees)))
 (defn
  binding-order
  [_module trees bindings _operations]
  (let
   [fragment-by-syntax
    (into
     {}
     (mapcat
      (fn
       [ordinal tree]
       (map (fn [form] [(:syntax-id form) ordinal]) (:records tree)))
      (range)
      trees))
    indexed
    (map-indexed vector bindings)
    local?
    (fn*
     [p1__157#]
     (contains? fragment-by-syntax (:definition-syntax-id p1__157#)))]
   (->>
    indexed
    (sort-by
     (fn
      [[index binding]]
      [(if
        (local? binding)
        (get
         fragment-by-syntax
         (:definition-syntax-id binding)
         Integer/MAX_VALUE)
        Integer/MAX_VALUE)
       index]))
    (mapv second))))
 (defn
  fragment-coverage
  [_module forms _bindings resolutions fragment-manifest _operations]
  (let
   [local-binding-ids
    (vec (mapcat :local-binding-ids fragment-manifest))]
   {:covered-local-binding-ids local-binding-ids,
    :form-count (count forms),
    :fragment-count (count fragment-manifest),
    :root-form-count (count (mapcat :root-form-ids fragment-manifest)),
    :covered-resolution-reference-syntax-ids
    (vec (mapcat :resolution-reference-syntax-ids fragment-manifest)),
    :covered-form-ids (vec (mapcat :form-ids fragment-manifest)),
    :local-binding-count (count local-binding-ids),
    :covered-root-form-ids
    (vec (mapcat :root-form-ids fragment-manifest)),
    :resolution-count (count resolutions)}))
 (defn
  module-assembly-manifest
  [lineage fragment-manifest _operations]
  (let
   [content
    {:ordered-fragment-ids (mapv :fragment-id fragment-manifest),
     :root-form-ids (vec (mapcat :root-form-ids fragment-manifest)),
     :source-revision-id (:source-revision-id lineage),
     :sh06-semantic-projection-id
     (:sh06-semantic-projection-id lineage),
     :alias-table-id (:alias-table-id lineage)}
    content-id
    (:source-revision-id lineage)]
   (assoc
    content
    :content-id
    content-id
    :module-id
    (:sh06-semantic-projection-id lineage))))
 (defn-
 request-from-verified-upstream
 [resolution-artifact upstream-verification operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh07-b13-binding-order
   (.valAt ^clojure.lang.ILookup operations :sh07-b13-binding-order)
   sh07-b13-fragment-coverage
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-b13-fragment-coverage)
   sh07-b13-fragment-manifest
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-b13-fragment-manifest)
   sh07-b13-module-assembly-manifest
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-b13-module-assembly-manifest)
   sh07-core-build-form-tree
   (.valAt ^clojure.lang.ILookup operations :sh07-core-build-form-tree)
   sh07-core-decimal-evidence
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-decimal-evidence)
   sh07-core-decimal-evidence-complete!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-decimal-evidence-complete!)
   sh07-core-lineage-with-semantic-trace
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-lineage-with-semantic-trace)
   sh07-core-macro-trace
   (.valAt ^clojure.lang.ILookup operations :sh07-core-macro-trace)
   sh07-core-projection-binding-input
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-projection-binding-input)
   sh07-core-resolution-projection!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-resolution-projection!)
   sh07-core-root-syntax-id
   (.valAt ^clojure.lang.ILookup operations :sh07-core-root-syntax-id)
   sh07-core-semantic-macro-trace
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-macro-trace)
   sh07-core-source-map-order
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-source-map-order)
   sh07-core-source-occurrence-context
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-source-occurrence-context)
   sh07-core-source-path-from-resolution
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-source-path-from-resolution)
   source-path
   (sh07-core-source-path-from-resolution resolution-artifact)]
  (when-not
   (and
    (= :gravity/sh06-resolution-artifact (:kind resolution-artifact))
    (= :passed (:status upstream-verification))
    (=
     :complete
     (get-in resolution-artifact [:capability-based-proof :status])))
   (throw
    (ex-info
     "SH-07 requires a fresh verified SH-06 artifact"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :source-path source-path,
      :missing-fields [:fresh-authenticated-sh06-resolution]})))
  (let
   [sh05
    (:sh05-macro-artifact resolution-artifact)
    semantic-macro-trace
    (sh07-core-semantic-macro-trace (:macro-expansion-trace sh05))
    lineage
    (sh07-core-lineage-with-semantic-trace
     resolution-artifact
     semantic-macro-trace)
    module-contract
    (get-in
     resolution-artifact
     [:gravity-resolution-boundary
      :resolved-analysis
      :module-contract])
    module
    {:namespace (:namespace module-contract),
     :profile (:profile module-contract),
     :target (:target module-contract),
     :safety (:safety module-contract),
     :effects (vec (:effects module-contract)),
     :capabilities (vec (:capabilities module-contract)),
     :exports (vec (:exports module-contract)),
     :source-revision-id (:source-revision-id lineage)}
    decimal-evidence
    (sh07-core-decimal-evidence resolution-artifact)
    executable-syntax
    (->>
     (:expanded-syntax-stream sh05)
     (remove
      (fn*
       [p1__154#]
       (and (seq? (:form p1__154#)) (= 'ns (first (:form p1__154#))))))
     (map-indexed
      (fn
       [ordinal syntax]
       (assoc
        syntax
        :sh07/root-syntax-id
        (sh07-core-root-syntax-id
         (:source-revision-id lineage)
         ordinal
         (:form syntax)))))
     vec)
    source-occurrences
    (sh07-core-source-occurrence-context
     resolution-artifact
     executable-syntax)
    executable-syntax
    (mapv
     (fn*
      [p1__155#]
      (assoc
       p1__155#
       :sh07/source-occurrences
       (get source-occurrences (:sh07/root-syntax-id p1__155#))))
     executable-syntax)
    root-id-by-upstream-id
    (into
     {}
     (map
      (fn
       [syntax]
       [(:syntax/id syntax) (:sh07/root-syntax-id syntax)]))
     executable-syntax)
    raw-traces
    (filterv
     (fn* [p1__156#] (= 'defn (:macro p1__156#)))
     (:macro-expansion-trace sh05))
    traces
    (mapv
     (fn
      [ordinal step]
      (sh07-core-macro-trace
       lineage
       (assoc
        step
        :input-syntax-id
        (reader-canonical-hash
         {:domain :gravity/sh07-macro-input-syntax-v1,
          :source-revision-id (:source-revision-id lineage),
          :ordinal ordinal,
          :macro (:macro step)})
        :output-syntax-id
        (get root-id-by-upstream-id (:output-syntax-id step)))))
     (range)
     raw-traces)
    trace-by-output
    (into {} (map (juxt :output-def-syntax-id identity)) traces)
    trees
    (sh07-core-source-map-order
     resolution-artifact
     executable-syntax
     (mapv
      (fn*
       [p1__157#]
       (sh07-core-build-form-tree
        source-path
        (:source-revision-id lineage)
        decimal-evidence
        p1__157#
        (get trace-by-output (:sh07/root-syntax-id p1__157#))))
      executable-syntax))
    _
    (sh07-core-decimal-evidence-complete! source-path decimal-evidence)
    authenticated-sh06-request
    (get-in
     resolution-artifact
     [:gravity-resolution-boundary :authenticated-resolution-request])
    resolved-analysis
    (get-in
     resolution-artifact
     [:gravity-resolution-boundary :resolved-analysis])
    projection
    (sh07-core-resolution-projection!
     source-path
     (:source-revision-id lineage)
     executable-syntax
     trees
     traces
     authenticated-sh06-request
     resolved-analysis)
    forms
    (:forms projection)
    roots
    (mapv (comp :form-id :root) trees)
    bindings
    (sh07-b13-binding-order module trees (:binding-table projection))
    aliases
    (:alias-table projection)
    resolutions
    (:resolution-table projection)
    fragment-manifest
    (sh07-b13-fragment-manifest
     lineage
     module
     trees
     bindings
     resolutions
     aliases)
    fragment-coverage
    (sh07-b13-fragment-coverage
     module
     forms
     bindings
     resolutions
     fragment-manifest)
    module-assembly-manifest
    (sh07-b13-module-assembly-manifest lineage fragment-manifest)
    expectation
    {:source-revision-id (:source-revision-id lineage),
     :sh05-artifact-id (:sh05-artifact-id lineage),
     :macro-expansion-trace-id (:macro-expansion-trace-id lineage),
     :expanded-defn-count (count traces),
     :expected-input-syntax-ids (mapv :input-syntax-id traces),
     :expected-output-def-syntax-ids
     (mapv :output-def-syntax-id traces),
     :expected-introduced-fn-syntax-ids
     (mapv :introduced-fn-syntax-id traces)}
    request
    {:resolution-table resolutions,
     :fragment-manifest fragment-manifest,
     :lineage lineage,
     :fragment-coverage fragment-coverage,
     :schema-version 15,
     :macro-origin-traces traces,
     :projection-binding nil,
     :scope :sh07-b15-keyword-map-lookup,
     :binding-table bindings,
     :module module,
     :top-level-form-ids roots,
     :provenance {:actual-source-path source-path},
     :artifact :gravity/sh07-authenticated-sh06-core-request,
     :alias-table aliases,
     :module-assembly-manifest module-assembly-manifest,
     :macro-origin-expectation expectation,
     :macro-expansion-trace semantic-macro-trace,
     :forms forms}
    binding
    (reader-canonical-hash
     (sh07-core-projection-binding-input request))]
   (assoc request :projection-binding binding))))

(defn
 authenticated-request
 ([resolution-artifact operations]
  (let
   [sh07-core-authenticated-request
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-authenticated-request)]
   (sh07-core-authenticated-request
    resolution-artifact
    :gravity.bootstrap/legacy)))
 ([resolution-artifact context operations]
  (let
   [sh07-core-authenticated-request
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-authenticated-request)]
   (sh07-core-authenticated-request resolution-artifact context nil)))
 ([resolution-artifact context invocation operations]
  (let
   [sh03-reader-authenticated-envelope-options
    (.valAt
     ^clojure.lang.ILookup operations
     :sh03-reader-authenticated-envelope-options)
    sh06-resolution-artifact-verification
    (.valAt
     ^clojure.lang.ILookup operations
     :sh06-resolution-artifact-verification)
    sh07-core-request-from-verified-upstream
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-request-from-verified-upstream)
    sh07-core-source-path-from-resolution
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-source-path-from-resolution)
    contextual?
    (not= :gravity.bootstrap/legacy context)
    source-path
    (sh07-core-source-path-from-resolution resolution-artifact)
    _
    (when
     invocation
     (sh03-reader-authenticated-envelope-options
      source-path
      invocation))
    _
    (when
     (and contextual? (nil? context))
     (throw
      (ex-info
       "SH-07 requires lexical SH-06 authority"
       {:id "C6-VERIFY",
        :stage :core-lowering,
        :source-path source-path,
        :missing-fields [:sh06-context]})))
    verification
    (cond
     invocation
     (sh06-resolution-artifact-verification
      resolution-artifact
      context
      invocation)
     contextual?
     (sh06-resolution-artifact-verification
      resolution-artifact
      context)
     :else
     (sh06-resolution-artifact-verification resolution-artifact))]
   (sh07-core-request-from-verified-upstream
    resolution-artifact
    verification))))

(defn
 digest-reference-ordinal
 [value _operations]
 (when
  (and
   (map? value)
   (=
    #{:authority :schema-version :ordinal :artifact}
    (set (keys value)))
   (= :gravity/sh07-internal-digest-reference (:artifact value))
   (= 1 (:schema-version value))
   (= :sh07-digest-resolver (:authority value))
   (integer? (:ordinal value))
   (not (neg? (:ordinal value))))
  (:ordinal value)))

(defn
 resolve-reference!
 [source-path value resolved-digests operations]
 (let
  [p15-s23-sh02-sha256-id?
   (.valAt ^clojure.lang.ILookup operations :p15-s23-sh02-sha256-id?)
   sh07-core-digest-reference-ordinal
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-digest-reference-ordinal)
   ordinal
   (sh07-core-digest-reference-ordinal value)
   resolved
   (when (some? ordinal) (get resolved-digests ordinal))]
  (when-not
   (and (some? ordinal) (p15-s23-sh02-sha256-id? resolved))
   (throw
    (ex-info
     "SH-07 digest reference is unresolved"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :source-path source-path,
      :digest-reference value,
      :resolved-count (count resolved-digests)})))
  resolved))

(defn
 resolve-reference-vector!
 [source-path values resolved-digests operations]
 (let
  [sh07-core-resolve-reference!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-resolve-reference!)]
  (mapv
   (fn*
    [p1__158#]
    (sh07-core-resolve-reference!
     source-path
     p1__158#
     resolved-digests))
   values)))

(defn
 semantic-projection-id
 [value _operations]
 (or
  (get-in value [:lineage :sh06-semantic-projection-id])
  (get value :sh06-semantic-projection-id)
  (get-in value [:attributes :sh06-semantic-projection-id])))

(defn
 semantic-binding
 [semantic-projection-id binding _operations]
 (cond->
  binding
  (:upstream-binding-id binding)
  (assoc :upstream-binding-id (:binding-id binding))
  (and semantic-projection-id (:definition-artifact-id binding))
  (assoc :definition-artifact-id semantic-projection-id)))

(defn
 semantic-resolution
 [resolution _operations]
 (cond->
  resolution
  (:upstream-binding-id resolution)
  (assoc :upstream-binding-id (:binding-id resolution))))

(defn
 semantic-var-reference
 [semantic-projection-id reference operations]
 (let
  [sh07-core-semantic-projection-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-projection-id)
   semantic-projection-id
   (or
    semantic-projection-id
    (sh07-core-semantic-projection-id reference))]
  (cond->
   reference
   (:upstream-binding-id reference)
   (assoc :upstream-binding-id (:binding-id reference))
   (and semantic-projection-id (:definition-artifact-id reference))
   (assoc :definition-artifact-id semantic-projection-id)
   (and
    semantic-projection-id
    (:authenticated-sh06-artifact-id reference))
   (assoc :authenticated-sh06-artifact-id semantic-projection-id))))

(defn
 semantic-mutation
 [semantic-projection-id mutation operations]
 (let
  [sh07-core-semantic-projection-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-projection-id)
   semantic-projection-id
   (or
    semantic-projection-id
    (sh07-core-semantic-projection-id mutation))]
  (cond->
   mutation
   (:target-upstream-binding-id mutation)
   (assoc :target-upstream-binding-id (:target-binding-id mutation))
   (and
    semantic-projection-id
    (:target-definition-artifact-id mutation))
   (assoc :target-definition-artifact-id semantic-projection-id)
   (and
    semantic-projection-id
    (:authenticated-sh06-artifact-id mutation))
   (assoc :authenticated-sh06-artifact-id semantic-projection-id))))

(defn
 semantic-error-transfer
 [semantic-projection-id transfer operations]
 (let
  [sh07-core-semantic-projection-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-projection-id)
   semantic-projection-id
   (or
    semantic-projection-id
    (sh07-core-semantic-projection-id transfer))]
  (cond->
   transfer
   (and
    semantic-projection-id
    (:authenticated-sh06-artifact-id transfer))
   (assoc :authenticated-sh06-artifact-id semantic-projection-id))))

(defn
 semantic-error-handler
 [semantic-projection-id handler operations]
 (let
  [sh07-core-semantic-projection-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-projection-id)
   semantic-projection-id
   (or
    semantic-projection-id
    (sh07-core-semantic-projection-id handler))]
  (cond->
   handler
   (and
    semantic-projection-id
    (:authenticated-sh06-artifact-id handler))
   (assoc :authenticated-sh06-artifact-id semantic-projection-id))))

(defn
 semantic-node
 [semantic-projection-id node operations]
 (let
  [sh07-core-semantic-error-handler
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-error-handler)
   sh07-core-semantic-error-transfer
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-error-transfer)
   sh07-core-semantic-mutation
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-mutation)
   sh07-core-semantic-var-reference
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-var-reference)]
  (case
   (:core-form node)
   :var
   (update
    node
    :attributes
    (fn*
     [p1__151#]
     (sh07-core-semantic-var-reference
      semantic-projection-id
      p1__151#)))
   :set!
   (update
    node
    :attributes
    (fn*
     [p1__152#]
     (sh07-core-semantic-mutation semantic-projection-id p1__152#)))
   :throw
   (update
    node
    :attributes
    (fn*
     [p1__153#]
     (sh07-core-semantic-error-transfer
      semantic-projection-id
      p1__153#)))
   :try
   (update
    node
    :attributes
    (fn*
     [p1__154#]
     (sh07-core-semantic-error-handler
      semantic-projection-id
      p1__154#)))
   node)))

(defn
 semantic-identity-preimage
 [preimage operations]
 (let
  [sh07-core-semantic-binding
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-binding)
   sh07-core-semantic-error-handler
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-error-handler)
   sh07-core-semantic-error-transfer
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-error-transfer)
   sh07-core-semantic-mutation
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-mutation)
   sh07-core-semantic-node
   (.valAt ^clojure.lang.ILookup operations :sh07-core-semantic-node)
   sh07-core-semantic-projection-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-projection-id)
   sh07-core-semantic-resolution
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-resolution)
   sh07-core-semantic-var-reference
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-var-reference)
   semantic-projection-id
   (sh07-core-semantic-projection-id preimage)]
  (cond->
   preimage
   (and
    semantic-projection-id
    (contains? preimage :projection-binding))
   (assoc :projection-binding semantic-projection-id)
   (and semantic-projection-id (contains? preimage :lineage))
   (assoc-in
    [:lineage :authenticated-sh06-artifact-id]
    semantic-projection-id)
   (:binding-table preimage)
   (update
    :binding-table
    (fn*
     [p1__155#]
     (mapv
      (partial sh07-core-semantic-binding semantic-projection-id)
      p1__155#)))
   (:resolution-table preimage)
   (update
    :resolution-table
    (fn* [p1__156#] (mapv sh07-core-semantic-resolution p1__156#)))
   (:var-references preimage)
   (update
    :var-references
    (fn*
     [p1__157#]
     (mapv
      (partial sh07-core-semantic-var-reference semantic-projection-id)
      p1__157#)))
   (:mutations preimage)
   (update
    :mutations
    (fn*
     [p1__158#]
     (mapv
      (partial sh07-core-semantic-mutation semantic-projection-id)
      p1__158#)))
   (:error-transfers preimage)
   (update
    :error-transfers
    (fn*
     [p1__159#]
     (mapv
      (partial
       sh07-core-semantic-error-transfer
       semantic-projection-id)
      p1__159#)))
   (:error-handlers preimage)
   (update
    :error-handlers
    (fn*
     [p1__160#]
     (mapv
      (partial sh07-core-semantic-error-handler semantic-projection-id)
      p1__160#)))
   (:nodes preimage)
   (update
    :nodes
    (fn*
     [p1__161#]
     (mapv
      (partial sh07-core-semantic-node semantic-projection-id)
      p1__161#)))
   (= :var (:core-form preimage))
   (update
    :attributes
    (fn*
     [p1__162#]
     (sh07-core-semantic-var-reference
      semantic-projection-id
      p1__162#)))
   (= :set! (:core-form preimage))
   (update
    :attributes
    (fn*
     [p1__163#]
     (sh07-core-semantic-mutation semantic-projection-id p1__163#)))
   (= :throw (:core-form preimage))
   (update
    :attributes
    (fn*
     [p1__164#]
     (sh07-core-semantic-error-transfer
      semantic-projection-id
      p1__164#)))
   (= :try (:core-form preimage))
   (update
    :attributes
    (fn*
     [p1__165#]
     (sh07-core-semantic-error-handler
      semantic-projection-id
      p1__165#))))))

(defn
 resolve-digest-preimage!
 [source-path purpose preimage resolved-digests operations]
 (let
  [sh07-core-execute!
   (.valAt ^clojure.lang.ILookup operations :sh07-core-execute!)
   sh07-core-resolve-reference-vector!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-resolve-reference-vector!)
   sh07-core-semantic-identity-preimage
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-identity-preimage)]
  (case
   purpose
   :sh07-core-node-id
   (->
    preimage
    (assoc
     :children
     (sh07-core-resolve-reference-vector!
      source-path
      (:children preimage)
      resolved-digests))
    (assoc
     :evaluated-children
     (sh07-core-resolve-reference-vector!
      source-path
      (:evaluated-children preimage)
      resolved-digests))
    sh07-core-semantic-identity-preimage)
   :sh07-core-artifact-id
   (let
    [result
     (sh07-core-execute!
      source-path
      'sh07-resolve-identity-preimage
      [preimage resolved-digests])]
    (when-not
     (= :accepted (:status result))
     (throw
      (ex-info
       "SH-07 identity digest preimage did not resolve"
       {:id "C6-VERIFY",
        :stage :core-lowering,
        :source-path source-path,
        :reason (:reason result)})))
    (sh07-core-semantic-identity-preimage (:value result)))
   :sh07-core-provenance-binding-id
   (let
    [result
     (sh07-core-execute!
      source-path
      'sh07-resolve-provenance-preimage
      [preimage resolved-digests])]
    (when-not
     (= :accepted (:status result))
     (throw
      (ex-info
       "SH-07 provenance digest preimage did not resolve"
       {:id "C6-VERIFY",
        :stage :core-lowering,
        :source-path source-path,
        :reason (:reason result)})))
    (:value result))
   :sh07-source-core-map-id
   (let
    [result
     (sh07-core-execute!
      source-path
      'sh07-resolve-source-core-map-preimage
      [preimage resolved-digests])]
    (when-not
     (= :accepted (:status result))
     (throw
      (ex-info
       "SH-07 mapping digest preimage did not resolve"
       {:id "C6-VERIFY",
        :stage :core-lowering,
        :reason (:reason result)})))
    (sh07-core-semantic-identity-preimage (:value result)))
   :sh07-core-diagnostic-id
   preimage
   (throw
    (ex-info
     "Unknown SH-07 digest purpose"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :reason :unknown-digest-purpose,
      :purpose purpose})))))

(defn
 digest-requests
 [source-path digest-requests operations]
 (let
  [p15-s23-c6c10-carrier-validation
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-carrier-validation)
   reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh07-core-resolve-digest-preimage!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-resolve-digest-preimage!)]
  (p15-s23-c6c10-carrier-validation source-path digest-requests)
  (loop
   [remaining digest-requests ordinal 0 resolved []]
   (if
    (empty? remaining)
    resolved
    (let
     [request (first remaining)]
     (when-not
      (and
       (= ordinal (:ordinal request))
       (= #{:purpose :preimage :ordinal} (set (keys request))))
      (throw
       (ex-info
        "Malformed SH-07 digest request sequence"
        {:id "C6-VERIFY",
         :stage :core-lowering,
         :digest-request request,
         :ordinal ordinal})))
     (let
      [purpose
       (:purpose request)
       preimage
       (sh07-core-resolve-digest-preimage!
        source-path
        purpose
        (:preimage request)
        resolved)
       digest
       (reader-canonical-hash
        {:domain :gravity/sh07-declared-digest-v1,
         :purpose purpose,
         :preimage preimage})]
      (recur
       (rest remaining)
       (inc ordinal)
       (conj resolved digest))))))))

(defn
 resolve-result
 [source-path template _digest-requests resolved-digests operations]
 (let
  [sh07-core-execute!
   (.valAt ^clojure.lang.ILookup operations :sh07-core-execute!)
   resolution
   (sh07-core-execute!
    source-path
    'sh07-resolve-core-template
    [template resolved-digests])]
  (when-not
   (= :accepted (:status resolution))
   (throw
    (ex-info
     "SH-07 controlled digest-slot resolution failed"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :source-path source-path,
      :reason (:reason resolution),
      :resolution resolution})))
  (:value resolution)))

(defn
 public-decimal-presentation
 "Return an explicit host presentation view. Do not use this view as an\n  authenticated canonical core: its decimal values differ from the preimage."
 [core _operations]
 (update
  core
  :nodes
  (fn
   [nodes]
   (mapv
    (fn
     [node]
     (let
      [descriptor (get-in node [:attributes :value])]
      (if
       (and
        (= :decimal (get-in node [:attributes :literal-kind]))
        (map? descriptor)
        (= :gravity/arbitrary-decimal-literal (:kind descriptor)))
       (assoc-in
        node
        [:attributes :value]
        (java.math.BigDecimal.
         (java.math.BigInteger. (:unscaled-value descriptor))
         (int (:scale descriptor))))
       node)))
    nodes))))

(defn
 canonical-artifact
 [resolved-template operations]
 (let
  [sh07-core-semantic-identity-preimage
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-semantic-identity-preimage)]
  (->
   resolved-template
   (assoc
    :artifact
    :gravity/sh07-canonical-core-artifact
    :artifact-id
    (:artifact-id-request resolved-template)
    :provenance-binding-id
    (:provenance-binding-id-request resolved-template)
    :identity-preimage
    (sh07-core-semantic-identity-preimage
     (:identity-preimage resolved-template)))
   (dissoc :artifact-id-request :provenance-binding-id-request))))

(defn
 raise-diagnostic!
 [_source-path template operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   diagnostic
   (first (:diagnostics template))
   request
   (first (:digest-requests template))
   diagnostic-id
   (reader-canonical-hash
    {:domain :gravity/sh07-declared-digest-v1,
     :purpose (:purpose request),
     :preimage (:preimage request)})
   resolved
   (assoc diagnostic :diagnostic-id-request diagnostic-id)]
  (throw
   (ex-info
    "Gravity checked-core lowering rejected the source"
    resolved))))

(defn
 request-diagnostic!
 [request rule-specific operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   source-path
   (get-in request [:provenance :actual-source-path])
   lineage
   (:lineage request)
   module
   (:module request)
   reason
   (or (:reason rule-specific) :authenticated-core-request)
   remediation
   "Replay the Gravity template and bind every digest ordinal exactly once."
   diagnostic
   {:lowering-rule :sh07-b47-function-call-recursion-products,
    :remediation remediation,
    :source-span {:source source-path},
    :stage :core-lowering,
    :rule "C6-VERIFY",
    :diagnostic-id-request
    (reader-canonical-hash
     {:domain :gravity/sh07-request-diagnostic-v16,
      :source-revision-id (:source-revision-id lineage),
      :rule-specific rule-specific}),
    :generated-origin-chain [],
    :syntax-id nil,
    :severity :error,
    :core-node-id nil,
    :artifact :gravity/sh07-core-diagnostic,
    :form-id nil,
    :target (:target module),
    :facts
    {:reason reason,
     :rule-specific rule-specific,
     :source-revision-id (:source-revision-id lineage),
     :sh06-artifact-id (:sh06-artifact-id lineage),
     :authenticated-sh06-artifact-id
     (:authenticated-sh06-artifact-id lineage),
     :sh06-semantic-projection-id
     (:sh06-semantic-projection-id lineage),
     :fail-closed true},
    :namespace (:namespace module),
    :profile (:profile module)}]
  (throw
   (ex-info
    "SH-07 authenticated request failed validation"
    diagnostic))))

(defn
 request-preflight!
 [request operations]
 (let
  [p15-s23-c6c10-carrier-validation
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-carrier-validation)
   reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh07-core-projection-binding-input
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-projection-binding-input)
   sh07-core-request-diagnostic!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-request-diagnostic!)]
  (p15-s23-c6c10-carrier-validation "<sh07-core-request>" request)
  (let
   [value-shape
    (fn
     [value]
     (cond
      (nil? value)
      :nil
      (map? value)
      :map
      (vector? value)
      :vector
      (set? value)
      :set
      (sequential? value)
      :sequential
      (coll? value)
      :collection
      :else
      :scalar))
    require-shape
    (fn
     [predicate key reason]
     (when-not
      (predicate (get request key))
      (sh07-core-request-diagnostic!
       request
       {:reason reason,
        :observed-shape (value-shape (get request key))})))]
   (when-not
    (map? request)
    (sh07-core-request-diagnostic!
     {}
     {:reason :request-map-required,
      :observed-shape (value-shape request)}))
   (doseq
    [[key reason]
     [[:lineage :request-lineage-map-required]
      [:module :request-module-map-required]
      [:provenance :request-provenance-map-required]]]
    (require-shape map? key reason))
   (doseq
    [[key reason]
     [[:forms :request-forms-vector-required]
      [:binding-table :request-binding-table-vector-required]
      [:alias-table :request-alias-table-vector-required]
      [:resolution-table :request-resolution-table-vector-required]
      [:fragment-manifest :request-fragment-manifest-vector-required]
      [:macro-expansion-trace :request-macro-trace-vector-required]]]
    (require-shape vector? key reason))
   (when-not
    (=
     (:projection-binding request)
     (reader-canonical-hash
      (sh07-core-projection-binding-input request)))
    (sh07-core-request-diagnostic!
     request
     {:reason :request-projection-binding-mismatch}))
   (when-not
    (=
     (get-in request [:lineage :alias-table-id])
     (reader-canonical-hash
      {:domain :gravity/sh07-sh06-alias-table-v1,
       :aliases (:alias-table request)}))
    (sh07-core-request-diagnostic!
     request
     {:reason :request-alias-table-lineage-mismatch}))
   (when-not
    (= 15 (:schema-version request))
    (sh07-core-request-diagnostic!
     request
     {:reason :request-schema-version,
      :expected 15,
      :observed (:schema-version request)}))
   (when-not
    (= :sh07-b15-keyword-map-lookup (:scope request))
    (sh07-core-request-diagnostic!
     request
     {:reason :request-scope,
      :expected :sh07-b15-keyword-map-lookup,
      :observed (:scope request)}))
   :passed)))

(defn
 run-structural-request-for-test
 "Test-only structural runner. It does not authenticate SH-06 membership;\n  callers testing the executable boundary must use\n  sh07-core-run-request-for-test with the verified SH-06 artifact."
 [authenticated-request operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   with-execution-binding
   (.valAt ^clojure.lang.ILookup operations :with-execution-binding)
   sh07-core-canonical-artifact
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-canonical-artifact)
   sh07-core-current-source-key
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-current-source-key)
   sh07-core-digest-requests
   (.valAt ^clojure.lang.ILookup operations :sh07-core-digest-requests)
   sh07-core-execute!
   (.valAt ^clojure.lang.ILookup operations :sh07-core-execute!)
   sh07-core-raise-diagnostic!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-raise-diagnostic!)
   sh07-core-request-preflight!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-request-preflight!)
   sh07-core-resolve-result
   (.valAt ^clojure.lang.ILookup operations :sh07-core-resolve-result)
   input-key
   (sh07-core-current-source-key)
   core-binding
   (or
    (input :*sh07-core-execution-binding*)
    @(input :sh07-core-cached-binding))
   _
   (when-not
    (=
     (second (:source input-key))
     (:source-content-hash core-binding))
    (throw
     (ex-info
      "SH-07 construction started with a stale binding"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :reason :checked-core-operation-revision-drift})))
   run
   (with-execution-binding
    core-binding
    (fn
     []
     (let
      [_
       (sh07-core-request-preflight! authenticated-request)
       source-path
       (get-in authenticated-request [:provenance :actual-source-path])
       result
       (sh07-core-execute!
        source-path
        'sh07-build-core-template
        [authenticated-request])]
      (case
       (:status result)
       :rejected
       (sh07-core-raise-diagnostic! source-path result)
       :accepted
       (let
        [template
         (:core-template result)
         digest-requests
         (:digest-requests result)
         template-verification
         (sh07-core-execute!
          source-path
          'sh07-verify-core-template
          [authenticated-request template digest-requests])
         _
         (when-not
          (= :passed (:status template-verification))
          (throw
           (ex-info
            "Gravity SH-07 template replay failed"
            {:id "C6-VERIFY",
             :stage :core-lowering,
             :source-path source-path,
             :template-verification template-verification})))
         resolved-digests
         (sh07-core-digest-requests source-path digest-requests)
         resolved-template
         (sh07-core-resolve-result
          source-path
          template
          digest-requests
          resolved-digests)
         resolved-verification
         (sh07-core-execute!
          source-path
          'sh07-verify-core-resolved
          [authenticated-request
           resolved-template
           digest-requests
           resolved-digests])
         _
         (when-not
          (= :passed (:status resolved-verification))
          (throw
           (ex-info
            "Gravity SH-07 resolved replay failed"
            {:id "C6-VERIFY",
             :stage :core-lowering,
             :source-path source-path,
             :resolved-verification resolved-verification})))]
        {:raw-template-result result,
         :canonical-core-artifact
         (sh07-core-canonical-artifact resolved-template),
         :digest-requests digest-requests,
         :resolved-digests resolved-digests,
         :template-verification template-verification,
         :resolved-verification resolved-verification})
       (throw
        (ex-info
         "Gravity SH-07 returned an invalid result status"
         {:id "C6-VERIFY",
          :stage :core-lowering,
          :source-path source-path,
          :status (:status result)}))))))]
  (when-not
   (= input-key (sh07-core-current-source-key))
   (throw
    (ex-info
     "SH-07 inputs changed during construction"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :reason :checked-core-operation-revision-drift})))
  run))

(defn
 run-request-for-test
 ([resolution-artifact authenticated-request operations]
  (let
   [sh07-core-run-request-for-test
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-run-request-for-test)]
   (sh07-core-run-request-for-test
    resolution-artifact
    authenticated-request
    nil)))
 ([resolution-artifact authenticated-request invocation operations]
  (let
   [sh03-reader-authenticated-envelope-options
    (.valAt
     ^clojure.lang.ILookup operations
     :sh03-reader-authenticated-envelope-options)
    sh06-resolution-artifact-verification
    (.valAt
     ^clojure.lang.ILookup operations
     :sh06-resolution-artifact-verification)
    sh07-core-authenticated-request
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-authenticated-request)
    sh07-core-request-diagnostic!
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-request-diagnostic!)
    sh07-core-run-structural-request-for-test
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-run-structural-request-for-test)
    sh07-core-source-path-from-resolution
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-source-path-from-resolution)
    _
    (when
     invocation
     (sh03-reader-authenticated-envelope-options
      (sh07-core-source-path-from-resolution resolution-artifact)
      invocation))
    upstream-verification
    (if
     invocation
     (sh06-resolution-artifact-verification
      resolution-artifact
      :gravity.bootstrap/legacy
      invocation)
     (sh06-resolution-artifact-verification resolution-artifact))
    expected-request
    (when
     (= :passed (:status upstream-verification))
     (sh07-core-authenticated-request
      resolution-artifact
      :gravity.bootstrap/legacy
      invocation))]
   (when-not
    (= :passed (:status upstream-verification))
    (sh07-core-request-diagnostic!
     authenticated-request
     {:reason :authenticated-sh06-artifact-required,
      :failed-checks (:failed-checks upstream-verification)}))
   (when-not
    (= expected-request authenticated-request)
    (sh07-core-request-diagnostic!
     authenticated-request
     {:reason :authenticated-sh06-request-membership-mismatch}))
   (sh07-core-run-structural-request-for-test authenticated-request))))

(doseq
 [operation
  [#'build-plan-binding!
   #'current-plan-binding!
   #'execute!
   #'plan-identities
   #'source-key
   #'source-path
   #'build-form-tree
   #'children
   #'decimal-descriptor
   #'decimal-evidence
   #'decimal-evidence-complete!
   #'macro-trace
   #'neutral-value
   #'parameter-binding-paths
   #'projected-form-id
   #'projected-source-mismatch
   #'projected-syntax-id
   #'root-syntax-id
   #'semantic-macro-trace
   #'semantic-span
   #'source-map-order
   #'source-occurrence-context
   #'source-path-from-resolution
   #'value-kind
   #'alias-name
   #'binding-order
   #'fragment-coverage
   #'fragment-manifest
   #'fragment-record
   #'module-assembly-manifest
   #'lineage
   #'lineage-with-semantic-trace
   #'projection-binding-input
   #'authenticated-request
   #'digest-reference-ordinal
   #'request-from-verified-upstream
   #'resolve-reference!
   #'resolve-reference-vector!
   #'semantic-binding
   #'semantic-projection-id
   #'semantic-resolution
   #'semantic-var-reference
   #'digest-requests
   #'resolve-digest-preimage!
   #'resolve-result
   #'semantic-error-handler
   #'semantic-error-transfer
   #'semantic-identity-preimage
   #'semantic-mutation
   #'semantic-node
   #'canonical-artifact
   #'public-decimal-presentation
   #'raise-diagnostic!
   #'request-diagnostic!
   #'request-preflight!
   #'run-structural-request-for-test
   #'run-request-for-test]]
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract)))
