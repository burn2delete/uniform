(ns gravity.self-hosting.stage3-verification-runner
  "Runs the fixed, non-authoritative Stage3--Stage9 development batches.

  Stage7 exposes the exact C11 source profiles, one cache-affine SH12 adapter
  batch, and one public C11 check.  The shape profile remains an execution-only
  alias of the complete source profile; it does not own a second copy of the
  namespace catalog.  Proof candidates are launched only by the Python policy
  boundary and remain non-authoritative pending independent attestation.

  This namespace intentionally has no compile-time dependency on the Clojure
  bootstrap, the C7 tests, or the SH-07 iteration runner.  The production
  delegate and catalog loader are resolved lazily; tests can therefore exercise
  the routing and receipt contract without loading the heavy authority graph."
  (:require [clojure.set :as set]
            [clojure.string :as string]))

(def ^:private maximum-cache-entries 1)
(def ^:private maximum-report-bytes 65536)
(def ^:private maximum-report-path-chars 4096)
(def ^:private maximum-report-binding-chars 256)
(def ^:private default-catalog-loader-marker ::default-catalog-loader)

(def ^:private primitive-test-namespace
  'gravity.self-hosting.sh08-primitive-function-type-test)
(def ^:private recursive-test-namespace
  'gravity.self-hosting.sh08-recursive-function-type-test)
(def ^:private authoritative-ho-test-namespace
  'gravity.self-hosting.sh08-authoritative-higher-order-function-test)
(def ^:private fragment-test-namespace
  'gravity.self-hosting.stage3-fragment-size-preflight-test)
(def ^:private c8-source-test-namespace
  'gravity.self-hosting.sh07-c8-effect-source-coverage-test)
(def ^:private sh09-adapter-test-namespace
  'gravity.self-hosting.sh09-c7-effect-adapter-test)
(def ^:private c9-source-test-namespace
  'gravity.self-hosting.sh07-c9-ownership-source-coverage-test)
(def ^:private sh10-ownership-transition-test-namespace
  'gravity.self-hosting.sh10-ownership-transition-test)
(def ^:private sh10-c8-ownership-adapter-test-namespace
  'gravity.self-hosting.sh10-c8-ownership-adapter-test)
(def ^:private c10-source-test-namespace
  'gravity.self-hosting.sh07-c10-safety-source-coverage-test)
(def ^:private c11-source-test-namespace
  'gravity.self-hosting.sh07-c11-mir-source-preflight-test)
(def ^:private sh11-numeric-safety-test-namespace
  'gravity.self-hosting.sh11-numeric-safety-test)
(def ^:private sh11-c9-safety-adapter-test-namespace
  'gravity.self-hosting.sh11-c9-safety-adapter-test)
(def ^:private sh12-c10-mir-adapter-test-namespace
  'gravity.self-hosting.sh12-c10-mir-adapter-test)
(def ^:private c12-shape-test-namespace
  'gravity.self-hosting.sh07-c12-domain-ir-shape-preflight-test)
(def ^:private sh13-c11-domain-evidence-test-namespace
  'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test)
(def ^:private c13-shape-test-namespace
  'gravity.self-hosting.sh07-c13-mir-optimization-shape-preflight-test)
(def ^:private sh16-c13-evidence-boundary-test-namespace
  'gravity.self-hosting.sh16-c12-domain-evidence-boundary-test)
(def ^:private public-test-namespace
  'gravity.bootstrap-test)

;; These vectors are source order, not alphabetical order.  Keep them literal:
;; a changed or newly added deftest must be admitted deliberately below.
(def primitive-pure-selectors
  ['gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-structure-and-fixture-parity
   'gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-ho-diagonal
   'gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-ho-unsupported-is-explicit
   'gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-ho-mutation-is-not-silent])

(def primitive-bool-authenticated-selectors
  ['gravity.self-hosting.sh08-primitive-function-type-test/sh08-primitive-family-authenticated-bool-gravity-boundary])

(def recursive-pure-selectors
  ['gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-source-reachability-and-structure
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-fixture-pair-is-byte-identical
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-pure-positive-and-monotone-fixed-point
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-pure-hostile-matrix
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-unsupported-external-primitive-keeps-evidence
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-primitive-family-diagonal-and-conflicts
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-nonconvergence-is-precise])

(def recursive-authenticated-selectors
  ;; These two authenticated boundaries share the recursive test namespace and
  ;; therefore run in one cold JVM. Preserve the source/deftest order: the
  ;; integer boundary is declared before the string boundary.
  ['gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-authenticated-gravity-boundary
   'gravity.self-hosting.sh08-recursive-function-type-test/sh08-recursive-authenticated-string-gravity-boundary])

(def authoritative-ho-pure-selectors
  ['gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-c7-reachability-and-identity
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-pure-proof-and-context-matrix
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-first-order-record-shape-is-additive
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-pure-proof-rejects-substitution
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-pure-proof-rejects-nonfunction-capture-and-arity
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-rejected-proof-uses-first-order-public-fallback
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-higher-order-pending-is-an-exact-replacement])

(def authoritative-ho-authenticated-selectors
  ;; The fixture parity and authenticated boundary are adjacent deftests in
  ;; one namespace; retain that declaration order in their shared JVM.
  ['gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-ho2-fixtures-are-co-canonical
   'gravity.self-hosting.sh08-authoritative-higher-order-function-test/sh08-authoritative-ho2-authenticated-fixture-boundary])

(def source-plan-contract-selectors
  ['gravity.self-hosting.sh07-c7-type-source-coverage-test/sh07-b47-c7-stage2-plan-identity-is-exact
   'gravity.self-hosting.sh07-c7-type-source-coverage-test/sh07-b28-c7-source-contracts-bounds-pending-and-limitations-are-exact])

(def coverage-census-contract-selectors
  ['gravity.self-hosting.sh07-authoritative-coverage-census-test/proof-contract-binds-the-measured-c7-census
   'gravity.self-hosting.sh07-authoritative-coverage-census-test/source-contract-mismatch-stops-before-authoritative-proof])

(def source-control-form-arity-selectors
  ['gravity.self-hosting.sh07-c7-type-source-coverage-test/sh07-b47-c7-source-control-form-arities-are-exact])

(def public-c7-check-selectors
  ['gravity.bootstrap-test/public-check-accepts-gravity-authored-c7-type-checker-engine])

(def fragment-size-preflight-selectors
  ['gravity.self-hosting.stage3-fragment-size-preflight-test/stage3-fragment-size-preflight])

;; C8 source coverage is a deliberate execution order, not a copy of the
;; source-file order.  The control-form arity check is intentionally moved
;; immediately after the proof-contract registration so malformed input fails
;; before the broader policy/contract traversal can do expensive work.  The
;; fixed-catalog validator records this exception explicitly below.
(def stage4-c8-source-structural-selectors
  ['gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-proof-contract-registers-c8-source-exactly
   'gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-c8-source-control-form-arities-are-bounded
   'gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-c8-source-contracts-policy-and-boundaries-are-exact
   'gravity.self-hosting.sh07-c8-effect-source-coverage-test/sh07-b29-c8-structural-limitations-remain-explicit])

(def stage4-sh09-adapter-selectors
  ['gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-source-structure-and-policy-are-exact
   'gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-derives-one-pure-effect-fact-per-type-fact
   'gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-rejects-upstream-and-candidate-substitution
   'gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-derives-declared-pure-function-call-effects
   'gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-binds-ordered-effect-identities
   ;; Keep the authenticated boundary in this same namespace/JVM, after every
   ;; cheap synthetic adapter check.  A failure in the synthetic prefix must
   ;; still produce the exact remaining suffix as skipped evidence.
   'gravity.self-hosting.sh09-c7-effect-adapter-test/sh09-c7-adapter-authenticated-gravity-boundary])

(def stage4-public-c8-selectors
  ['gravity.bootstrap-test/public-check-accepts-gravity-authored-c8-effect-checker-engine])

;; C9 source checks intentionally put control-form arity immediately after
;; proof-contract registration.  Coverage vars 5--9 in this partial source
;; namespace remain deferred; they are not silently admitted by a generic
;; namespace run.
(def stage5-c9-source-structural-selectors
  ['gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-proof-contract-registers-c9-source-exactly
   'gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-c9-source-control-form-arities-are-bounded
   'gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-c9-source-contracts-states-and-reasons-are-exact
   'gravity.self-hosting.sh07-c9-ownership-source-coverage-test/sh07-b30-c9-structural-limitations-remain-explicit])

(def stage5-c9-kernel-selectors
  ['gravity.self-hosting.sh10-ownership-transition-test/sh10-source-and-fixtures-compile-as-gravity
   'gravity.self-hosting.sh10-ownership-transition-test/sh10-accepts-initiation-borrow-move-and-bounded-lifetime-flows
   'gravity.self-hosting.sh10-ownership-transition-test/sh10-rejects-invalid-state-transitions-structurally
   'gravity.self-hosting.sh10-ownership-transition-test/sh10-fails-closed-on-request-event-and-result-substitution])

(def stage5-sh10-c8-adapter-selectors
  ['gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-source-api-and-policy-are-exact
   'gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-accepts-persistent-primitive-read
   'gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-accepts-primitive-type-family
   'gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-rejects-mutation-and-non-read-events
   'gravity.self-hosting.sh10-c8-ownership-adapter-test/sh10-c8-adapter-authenticated-gravity-boundary])

(def stage5-public-c9-selectors
  ['gravity.bootstrap-test/public-check-accepts-gravity-authored-c9-ownership-checker-engine])

;; C10 source admission is deliberately independent of the artifact-backed
;; coverage tail.  Reader-shape and export completeness precede every exact
;; policy/hash lookup so an unfinished or malformed adapter stops cheaply.
(def stage6-c10-source-structural-selectors
  ['gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-control-form-arities-are-bounded
   'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-export-definitions-are-complete
   'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-proof-contract-registers-c10-source-exactly
   'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-contracts-policy-outcomes-and-reasons-are-exact
   'gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-static-lookup-and-residual-boundaries-are-exact])

(def stage6-c10-kernel-selectors
  ['gravity.self-hosting.sh11-numeric-safety-test/sh11-source-and-fixtures-compile-as-gravity
   'gravity.self-hosting.sh11-numeric-safety-test/sh11-classifies-every-supported-operation-into-one-outcome
   'gravity.self-hosting.sh11-numeric-safety-test/sh11-enforces-each-supported-mode-semantics
   'gravity.self-hosting.sh11-numeric-safety-test/sh11-rejects-unresolved-and-invalid-numeric-safety
   'gravity.self-hosting.sh11-numeric-safety-test/sh11-contains-i64-overflow-and-binds-index-and-shift-domains
   'gravity.self-hosting.sh11-numeric-safety-test/sh11-fails-closed-on-schema-mode-lineage-and-structural-attacks
   'gravity.self-hosting.sh11-numeric-safety-test/sh11-fails-closed-on-runtime-unsafe-and-result-attacks])

;; This is an explicit fail-fast order, not source order.  The source/API and
;; four synthetic identity checks must all pass before the single authenticated
;; .gravity carrier is built.  The co-canonical .qst fixture is byte-compared by
;; the prefix but never artifact-built in this development batch.
(def stage6-sh11-c9-safety-adapter-selectors
  ['gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-source-api-is-complete
   'gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-identity-binding-is-sequential-and-exact
   'gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-adapter-binds-one-real-read
   'gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-generic-classifier-and-substitutions-fail-closed
   'gravity.self-hosting.sh11-c9-safety-adapter-test/sh11-c9-safety-authenticated-gravity-boundary])

;; Keep the exact source order and complete three-test census here.  The
;; moving-source shape alias below may bypass the pin, but the durable Stage7
;; graph always enters through this exact binding-first batch.
(def stage7-c11-source-preflight-selectors
  ['gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-binding-is-exact
   'gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-control-form-arities-are-exact
   'gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-exports-have-definitions])

;; This is a runner-only execution profile for the cheap shape/export gate.
;; Its selectors intentionally overlap the complete three-test source profile;
;; the profile is never a second catalog owner (see `execution-profile-batches`
;; and `validate-fixed-catalog!`).
(def stage7-c11-shape-preflight-selectors
  ['gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-control-form-arities-are-exact
   'gravity.self-hosting.sh07-c11-mir-source-preflight-test/sh07-c11-source-exports-have-definitions])

;; One namespace, one C11 plan, one process-local SH07 cache.  Put the bounded
;; envelope helper first so malformed verification members stop before the
;; semantic matrix and real carrier build.  The authenticated .gravity
;; boundary remains last; its paired .qst bytes are parity evidence only.
(def stage7-sh12-c10-mir-adapter-selectors
  ['gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-verification-envelope-preflight
   'gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-proof-reference-api-and-positive
   'gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-path-neutral-provenance-pair
   'gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-representative-mutation-rejections
   'gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-carrier-preflight-boundaries
   'gravity.self-hosting.sh12-c10-mir-adapter-test/sh12-c10-authenticated-gravity-boundary])

(def stage7-public-c11-selectors
  ['gravity.bootstrap-test/gravity-c11-source-and-builder-identities-are-pinned
   'gravity.bootstrap-test/public-check-accepts-gravity-authored-c11-mir-specification])

;; The C12 source-coverage namespace still owns deliberately stale deep
;; artifact/census oracles.  Development admission therefore uses only the
;; reviewed, single-snapshot moving-source shape gate.
(def stage8-c12-source-shape-selectors
  ['gravity.self-hosting.sh07-c12-domain-ir-shape-preflight-test/sh07-c12-domain-ir-source-shape-and-control
   'gravity.self-hosting.sh07-c12-domain-ir-shape-preflight-test/sh07-c12-domain-ir-export-completeness])

;; One namespace, one C12 plan, and one prepared C8->C11 carrier chain.  Keep
;; admission ahead of construction and all mutation families after the first
;; complete positive.  This order preserves an exact skipped tail while
;; avoiding six repeated cold plan chains.
(def stage8-sh13-c11-domain-evidence-selectors
  ['gravity.self-hosting.sh13-c11-domain-evidence-adapter-test/sh13-c11-domain-evidence-surface-and-policy
   'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test/sh13-c11-domain-evidence-input-admission
   'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test/sh13-c11-domain-evidence-positive
   'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test/sh13-c11-domain-evidence-fact-table-and-id-mutations
   'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test/sh13-c11-domain-evidence-hostile-carriers-and-recomputation
   'gravity.self-hosting.sh13-c11-domain-evidence-adapter-test/sh13-c11-domain-evidence-path-neutral-provenance])

;; One namespace and one authenticated C11 carrier.  Source parsing and the
;; public surface precede the positive; hostile mutations precede provenance.
(def stage8-sh14-authenticated-layout-selectors
  ['gravity.self-hosting.sh14-authenticated-layout-test/sh14-authenticated-layout-source-parses-before-compilation
   'gravity.self-hosting.sh14-authenticated-layout-test/sh14-authenticated-layout-surface-arity-and-nonclaims
   'gravity.self-hosting.sh14-authenticated-layout-test/sh14-authenticated-layout-genuine-positive-computes-logical-offsets
   'gravity.self-hosting.sh14-authenticated-layout-test/sh14-authenticated-layout-rejects-mutations-and-hostile-carriers
   'gravity.self-hosting.sh14-authenticated-layout-test/sh14-authenticated-layout-identity-is-path-neutral-with-separate-provenance])

(def stage8-public-c12-selectors
  ['gravity.bootstrap-test/public-check-accepts-gravity-authored-c12-domain-ir-architecture])

(def stage9-c13-source-shape-selectors
  ['gravity.self-hosting.sh07-c13-mir-optimization-shape-preflight-test/sh07-c13-mir-optimization-source-shape-and-control
   'gravity.self-hosting.sh07-c13-mir-optimization-shape-preflight-test/sh07-c13-mir-optimization-export-completeness])

;; One namespace, one C13 plan, and the exact prepared C11->C12 evidence chain.
;; The surface stays first, the complete positive precedes every mutation, and
;; provenance separation remains the final non-authoritative boundary.
(def stage9-sh16-c13-evidence-boundary-selectors
  ['gravity.self-hosting.sh16-c12-domain-evidence-boundary-test/sh16-c13-evidence-boundary-surface
   'gravity.self-hosting.sh16-c12-domain-evidence-boundary-test/sh16-c13-evidence-boundary-positive
   'gravity.self-hosting.sh16-c12-domain-evidence-boundary-test/sh16-c13-evidence-boundary-rejects-substitution-and-hostile-carriers
   'gravity.self-hosting.sh16-c12-domain-evidence-boundary-test/sh16-c13-evidence-boundary-separates-top-level-provenance])

(def ^:private execution-profile-batches
  {:stage7-c11-shape-preflight
   {:owner :stage7-c11-source-preflight
    :selectors stage7-c11-shape-preflight-selectors}})

(def stage6-public-c10-selectors
  ['gravity.bootstrap-test/public-check-accepts-gravity-authored-c10-safety-analysis-pipeline])

(def ^:private batch-order
  [:primitive-pure
   :primitive-bool-authenticated
   :recursive-pure
   :recursive-authenticated
   :authoritative-ho-pure
   :authoritative-ho-authenticated
   :source-control-form-arity
   :source-plan-contract
   :coverage-census-contract
   :fragment-size-preflight
   :public-c7-check
   :stage4-c8-source-structural
   :stage4-sh09-adapter
   :stage4-public-c8
   :stage5-c9-source-structural
   :stage5-c9-kernel
   :stage5-sh10-c8-adapter
   :stage5-public-c9
   :stage6-c10-source-structural
   :stage6-c10-kernel
   :stage6-public-c10
   :stage6-sh11-c9-safety-adapter
   :stage7-c11-source-preflight
   :stage7-c11-shape-preflight
   :stage7-sh12-c10-mir-adapter
   :stage7-public-c11
   :stage8-c12-source-shape
   :stage8-public-c12
   :stage8-sh13-c11-domain-evidence
   :stage8-sh14-authenticated-layout
   :stage9-c13-source-shape
   :stage9-sh16-c13-evidence-boundary])

(def ^:private batch-selectors
  (array-map
   :primitive-pure primitive-pure-selectors
   :primitive-bool-authenticated primitive-bool-authenticated-selectors
   :recursive-pure recursive-pure-selectors
   :recursive-authenticated recursive-authenticated-selectors
   :authoritative-ho-pure authoritative-ho-pure-selectors
   :authoritative-ho-authenticated authoritative-ho-authenticated-selectors
   :source-control-form-arity source-control-form-arity-selectors
   :source-plan-contract source-plan-contract-selectors
   :coverage-census-contract coverage-census-contract-selectors
   :fragment-size-preflight fragment-size-preflight-selectors
   :public-c7-check public-c7-check-selectors
   :stage4-c8-source-structural stage4-c8-source-structural-selectors
   :stage4-sh09-adapter stage4-sh09-adapter-selectors
   :stage4-public-c8 stage4-public-c8-selectors
   :stage5-c9-source-structural stage5-c9-source-structural-selectors
   :stage5-c9-kernel stage5-c9-kernel-selectors
   :stage5-sh10-c8-adapter stage5-sh10-c8-adapter-selectors
   :stage5-public-c9 stage5-public-c9-selectors
   :stage6-c10-source-structural stage6-c10-source-structural-selectors
   :stage6-c10-kernel stage6-c10-kernel-selectors
   :stage6-public-c10 stage6-public-c10-selectors
   :stage6-sh11-c9-safety-adapter
   stage6-sh11-c9-safety-adapter-selectors
   :stage7-c11-source-preflight stage7-c11-source-preflight-selectors
   :stage7-c11-shape-preflight stage7-c11-shape-preflight-selectors
   :stage7-sh12-c10-mir-adapter stage7-sh12-c10-mir-adapter-selectors
   :stage7-public-c11 stage7-public-c11-selectors
   :stage8-c12-source-shape stage8-c12-source-shape-selectors
   :stage8-public-c12 stage8-public-c12-selectors
   :stage8-sh13-c11-domain-evidence
   stage8-sh13-c11-domain-evidence-selectors
   :stage8-sh14-authenticated-layout
   stage8-sh14-authenticated-layout-selectors
   :stage9-c13-source-shape stage9-c13-source-shape-selectors
   :stage9-sh16-c13-evidence-boundary
   stage9-sh16-c13-evidence-boundary-selectors))

(def fixed-batch-ids
  "The complete CLI allowlist, in deterministic presentation order."
  batch-order)

(def fixed-batches
  "Fixed batch metadata.  The selector vectors are the source-order contract."
  (into (array-map)
        (map (fn [batch-id]
               [batch-id
                {:batch-id batch-id
                 :name (name batch-id)
                 :test-vars (get batch-selectors batch-id)
                 :selector-vector (get batch-selectors batch-id)
                 :maximum-entries maximum-cache-entries
                 :fail-fast? (> (count (get batch-selectors batch-id)) 1)
                 ;; An execution profile may reuse selectors from its
                 ;; complete source owner, but must never inflate ownership,
                 ;; completeness, or duplicate-selector accounting.
                 :catalog-owner? (not (contains? execution-profile-batches batch-id))
                 :catalog-owner-batch (get-in execution-profile-batches
                                              [batch-id :owner])
                 ;; Most partial namespaces retain source/deftest order.  The
                 ;; reviewed exceptions use their vector as exact execution
                 ;; order while membership is checked independently.
                 :catalog-order-policy
                 (if (#{:stage4-c8-source-structural
                        :stage6-c10-source-structural
                        :stage6-sh11-c9-safety-adapter
                        :stage7-sh12-c10-mir-adapter
                        :stage7-public-c11} batch-id)
                   :explicit-execution-order
                   :source-subsequence)
                 :authority :non-authoritative
                 :authoritative? false}])
             batch-order)))

(def fixed-batch-selectors
  "Public read-only view used by static/unit contract checks."
  batch-selectors)

(def ^:private complete-owned-source-namespaces
  #{primitive-test-namespace
    recursive-test-namespace
    authoritative-ho-test-namespace
    fragment-test-namespace
    sh09-adapter-test-namespace
    sh10-ownership-transition-test-namespace
    sh10-c8-ownership-adapter-test-namespace
    sh11-numeric-safety-test-namespace
    sh11-c9-safety-adapter-test-namespace
    sh12-c10-mir-adapter-test-namespace
    c11-source-test-namespace
    c12-shape-test-namespace
    sh13-c11-domain-evidence-test-namespace
    c13-shape-test-namespace
    sh16-c13-evidence-boundary-test-namespace})

(def ^:private partial-selector-namespaces
  #{'gravity.self-hosting.sh07-authoritative-coverage-census-test
    'gravity.self-hosting.sh07-c7-type-source-coverage-test
    c8-source-test-namespace
    c9-source-test-namespace
    c10-source-test-namespace
    public-test-namespace})

(def ^:private catalog-source-namespaces
  (set/union complete-owned-source-namespaces
             partial-selector-namespaces))

(defn- exception
  [id message data]
  (throw (ex-info message (merge {:id id} data))))

(defn- selector-namespace
  [selector]
  (when (symbol? selector)
    (some-> (namespace selector) symbol)))

(defn- ensure-selector-vector!
  [batch-id selectors]
  (when-not (vector? selectors)
    (exception "STAGE3-BATCH-SELECTORS"
               "Stage3 batch selectors must be a vector"
               {:batch-id batch-id :selectors selectors}))
  (when (empty? selectors)
    (exception "STAGE3-BATCH-EMPTY"
               "Stage3 batches must select at least one test var"
               {:batch-id batch-id}))
  (when-not (every? #(and (symbol? %) (selector-namespace %)) selectors)
    (exception "STAGE3-BATCH-QUALIFIED-SELECTOR"
               "Stage3 selectors must be namespace-qualified symbols"
               {:batch-id batch-id :selectors selectors}))
  (let [duplicates (->> selectors frequencies (keep (fn [[selector count]]
                                                       (when (> count 1) selector))) vec)]
    (when (seq duplicates)
      (exception "STAGE3-BATCH-DUPLICATE-SELECTOR"
                 "Stage3 batch selectors must be unique"
                 {:batch-id batch-id :duplicates duplicates})))
  selectors)

(defn- expected-batch-selectors-by-namespace
  [batches]
  ;; `fixed-batches` has more than eight entries and therefore cannot rely on
  ;; Clojure's insertion-ordered array-map representation.  Always derive the
  ;; fixed execution order from the literal fixed-batch-id vector.  Keep the
  ;; batch identity beside each selector: partial source files may interleave
  ;; selectors from batches whose execution order is intentionally different
  ;; from source order.
  (reduce
   (fn [result batch-id]
     (if (and (contains? batches batch-id)
              ;; Execution-profile aliases are validated against their
              ;; owner's source below, but do not become catalog owners.
              (get-in batches [batch-id :catalog-owner?] true))
      (reduce
        (fn [result selector]
          (let [namespace-symbol (selector-namespace selector)]
            (if (contains? catalog-source-namespaces namespace-symbol)
              (update result namespace-symbol (fnil conj [])
                      {:batch-id batch-id :selector selector})
              result)))
        result
        (ensure-selector-vector! batch-id (get-in batches [batch-id :test-vars])))
       result))
   {}
   batch-order))

(defn- ordered-subsequence?
  [expected actual]
  (loop [expected (seq expected)
         actual (seq actual)]
    (cond
      (nil? expected) true
      (nil? actual) false
      (= (first expected) (first actual))
      (recur (next expected) (next actual))
      :else
      (recur expected (next actual)))))

(defn- validate-execution-profile-batches!
  "Validate aliases without treating them as additional catalog owners.

  An execution profile is allowed to select a strict subset of its owner's
  source deftests (for example the C11 shape/export prefix), but every selected
  var must still exist, be unique, and obey the profile's declared source or
  explicit execution order.  Keeping this check separate from the ownership
  reduction is what permits intentional overlap without weakening drift
  detection.
  "
  [batches discovered loaded-namespaces]
  (doseq [[batch-id batch] (sort-by (comp str key) batches)
          :when (and (false? (:catalog-owner? batch))
                     ;; A batch invocation loads only the namespaces selected
                     ;; by that batch.  Validate an alias when its source is
                     ;; in that invocation; the full catalog call validates
                     ;; every profile by passing the complete namespace set.
                     (contains? (set loaded-namespaces)
                                (some-> (:test-vars batch)
                                        first
                                        selector-namespace)))]
    (let [selectors (ensure-selector-vector!
                     batch-id (:test-vars batch))
          owner-id (:catalog-owner-batch batch)
          owner (get batches owner-id)
          declared-profile (get-in execution-profile-batches
                                    [batch-id :selectors])
          namespaces (set (map selector-namespace selectors))]
      (when-not (and owner-id owner
                     (not (false? (:catalog-owner? owner))))
        (exception "STAGE3-CATALOG-PROFILE-OWNER"
                   "An execution profile must name an existing catalog owner"
                   {:batch-id batch-id :owner-batch owner-id}))
      (when-not (= 1 (count namespaces))
        (exception "STAGE3-CATALOG-PROFILE-NAMESPACE"
                   "An execution profile must select one source namespace"
                   {:batch-id batch-id :namespaces (vec (sort-by str namespaces))}))
      (when-not (= declared-profile selectors)
        (exception "STAGE3-CATALOG-PROFILE-ALLOWLIST"
                   "An execution profile selector vector drifted"
                   {:batch-id batch-id :expected declared-profile
                    :actual selectors}))
      (let [namespace-symbol (first namespaces)
            actual (vec (get discovered namespace-symbol []))
            actual-set (set actual)
            missing (vec (remove actual-set selectors))
            owner-selectors (set (:test-vars owner))
            owner-missing (vec (remove owner-selectors selectors))
            policy (get batch :catalog-order-policy :source-subsequence)]
        (when-not (contains? (set loaded-namespaces) namespace-symbol)
          (exception "STAGE3-CATALOG-PROFILE-NAMESPACE"
                     "An execution profile source namespace was not loaded"
                     {:batch-id batch-id :namespace namespace-symbol}))
        (when (seq missing)
          (exception "STAGE3-CATALOG-PROFILE-MISSING-TEST-VAR"
                     "An execution profile selects a test var missing from its source"
                     {:batch-id batch-id :namespace namespace-symbol
                      :missing missing :actual actual}))
        (when (seq owner-missing)
          (exception "STAGE3-CATALOG-PROFILE-OWNER-COVERAGE"
                     "An execution profile selects a var outside its catalog owner"
                     {:batch-id batch-id :owner-batch owner-id
                      :unexpected owner-missing}))
        (if (= policy :explicit-execution-order)
          (when-not (= (count selectors) (count (set selectors)))
            (exception "STAGE3-CATALOG-PROFILE-ORDER"
                       "An explicit execution profile contains duplicate selectors"
                       {:batch-id batch-id :selectors selectors}))
          (when-not (ordered-subsequence? selectors actual)
            (exception "STAGE3-CATALOG-PROFILE-SOURCE-ORDER"
                       "An execution profile no longer matches source order"
                       {:batch-id batch-id :namespace namespace-symbol
                        :expected selectors :actual actual
                        :policy policy})))))))

(defn validate-fixed-catalog!
  "Validate a fixed batch map against discovered deftest selectors.

  `discovered` is a map from namespace symbol to source-order selector vector.
  This function performs no namespace loading and is the injection seam for
  unit tests.  The zero-argument form lazily discovers the production catalog."
  ([discovered]
   (validate-fixed-catalog! fixed-batches discovered catalog-source-namespaces))
  ([batches discovered]
   (validate-fixed-catalog! batches discovered catalog-source-namespaces))
  ([batches discovered loaded-namespaces]
   (let [batch-ids (vec (keys batches))
         expected-ids (set fixed-batch-ids)
         actual-ids (set batch-ids)
         intentionally-unowned (atom {})]
     (when-not (= expected-ids actual-ids)
       (exception "STAGE3-BATCH-ALLOWLIST-DRIFT"
                  "Stage3 batch allowlist drifted"
                  {:expected (vec fixed-batch-ids)
                   :actual batch-ids
                   :missing (vec (sort (set/difference expected-ids actual-ids)))
                   :extra (vec (sort (set/difference actual-ids expected-ids)))}))
     (doseq [batch-id batch-ids]
       (let [selectors (ensure-selector-vector!
                        batch-id (get-in batches [batch-id :test-vars]))
             unexpected (vec (remove #(contains? catalog-source-namespaces
                                      (selector-namespace %))
                                     selectors))]
         (when (seq unexpected)
           (exception "STAGE3-CATALOG-UNEXPECTED-NAMESPACE"
                      "A fixed Stage3 selector belongs to an unexpected namespace"
                      {:batch-id batch-id :unexpected unexpected
                       :expected (vec (sort-by str catalog-source-namespaces))}))))
     (validate-execution-profile-batches! batches discovered loaded-namespaces)
     (let [expected-batches (expected-batch-selectors-by-namespace batches)
           expected-all
           (into {}
                 (map (fn [[namespace-symbol entries]]
                        [namespace-symbol (mapv :selector entries)]))
                 expected-batches)
           loaded-namespaces (set loaded-namespaces)
           unexpected-loaded (set/difference loaded-namespaces
                                              catalog-source-namespaces)
           _ (when (seq unexpected-loaded)
               (exception "STAGE3-CATALOG-UNEXPECTED-NAMESPACE"
                          "Requested Stage3 catalog contains an unexpected namespace"
                          {:unexpected (vec (sort-by str unexpected-loaded))
                           :expected (vec (sort-by str catalog-source-namespaces))}))
           expected (select-keys expected-all loaded-namespaces)
           flattened (mapcat (fn [[_ entries]] (map :selector entries)) expected)
           duplicates (->> flattened frequencies
                           (keep (fn [[selector count]]
                                   (when (> count 1) selector))) vec)]
       (when (seq duplicates)
         (exception "STAGE3-BATCH-DUPLICATE-SELECTOR"
                    "A test var is assigned to more than one Stage3 batch"
                    {:duplicates duplicates}))
       (let [discovered (or discovered {})
             expected-namespaces (set (keys expected))
             discovered-namespaces (set (keys discovered))
             extra-namespaces (set/difference discovered-namespaces
                                               expected-namespaces)]
         (when (seq extra-namespaces)
           (exception "STAGE3-CATALOG-UNEXPECTED-NAMESPACE"
                      "Discovered Stage3 catalog contains an unexpected namespace"
                      {:unexpected (vec (sort-by str extra-namespaces))
                       :expected (vec (sort-by str expected-namespaces))}))
         (doseq [namespace-symbol (sort-by str expected-namespaces)]
           (let [expected-selectors (vec (get expected namespace-symbol))
                 actual-selectors (vec (get discovered namespace-symbol []))
                 partial? (contains? partial-selector-namespaces namespace-symbol)
                 duplicate-actuals
                 (->> actual-selectors frequencies
                      (keep (fn [[selector count]]
                              (when (> count 1) selector))) vec)
                 missing (vec (remove (set actual-selectors) expected-selectors))
                 extra (vec (remove (set expected-selectors) actual-selectors))
                 expected-by-batch
                 (group-by :batch-id (get expected-batches namespace-symbol))
                 complete-explicit-order?
                 (and (not partial?)
                      (seq expected-by-batch)
                      (every?
                       (fn [batch-id]
                         (= :explicit-execution-order
                            (get-in batches
                                    [batch-id :catalog-order-policy]
                                    :source-subsequence)))
                       (keys expected-by-batch)))]
             (when (seq duplicate-actuals)
               (exception "STAGE3-CATALOG-DUPLICATE-TEST-VAR"
                          "Discovered Stage3 test catalog contains duplicates"
                          {:namespace namespace-symbol
                           :duplicates duplicate-actuals}))
             (when (seq missing)
               (exception "STAGE3-CATALOG-MISSING-TEST-VAR"
                          "A fixed Stage3 selector is missing from its source"
                          {:namespace namespace-symbol :missing missing
                           :expected expected-selectors
                           :actual actual-selectors}))
             (when (and (seq extra) (not partial?))
               (exception "STAGE3-CATALOG-EXTRA-TEST-VAR"
                          "A source deftest is not admitted by a fixed Stage3 batch"
                          {:namespace namespace-symbol :extra extra
                           :expected expected-selectors
                           :actual actual-selectors}))
             (when (and partial? (seq extra))
               (swap! intentionally-unowned assoc namespace-symbol extra))
             ;; Partial source files can interleave selectors assigned to
             ;; batches whose execution order differs from source order. Each
             ;; fixed batch vector is therefore checked independently as an
             ;; in-source-order subsequence; concatenating vectors in execution
             ;; order would reject a valid interleaving (or hide drift).  The
             ;; C8 source batch is the one reviewed exception: its deliberate
             ;; arity-before-contract order is checked as an exact unique
             ;; membership vector, not as a source-order subsequence.
             (when (and partial? (seq expected-selectors))
               (doseq [[batch-id entries] expected-by-batch]
                 (let [batch-selectors (mapv :selector entries)
                       policy (get-in batches [batch-id :catalog-order-policy]
                                      :source-subsequence)]
                   (if (= policy :explicit-execution-order)
                     (let [actual-set (set actual-selectors)
                           expected-set (set batch-selectors)]
                       (when (or (not= (count batch-selectors)
                                       (count expected-set))
                                 (not (set/subset? expected-set actual-set)))
                         (exception "STAGE3-CATALOG-EXPLICIT-ORDER"
                                    "A fixed explicit-order batch has missing or duplicate selectors"
                                    {:namespace namespace-symbol
                                     :batch-id batch-id
                                     :expected batch-selectors
                                     :actual actual-selectors
                                     :policy policy})))
                     (when-not (ordered-subsequence?
                                batch-selectors actual-selectors)
                       (exception "STAGE3-CATALOG-SOURCE-ORDER"
                                  "A fixed partial-batch selector vector no longer matches source order"
                                  {:namespace namespace-symbol
                                   :batch-id batch-id
                                   :expected batch-selectors
                                   :actual actual-selectors
                                   :partial-namespace? true
                                   :policy policy}))))))
             (when (and (not partial?)
                        (not complete-explicit-order?)
                        (seq expected-selectors)
                        (not= expected-selectors actual-selectors))
               (exception "STAGE3-CATALOG-SOURCE-ORDER"
                          "Stage3 selectors no longer match source order"
                          {:namespace namespace-symbol
                           :expected expected-selectors
                           :actual actual-selectors
                           :partial-namespace? false}))))
     {:status :passed
     :batch-ids (vec fixed-batch-ids)
     :discovered discovered
      :intentionally-unowned @intentionally-unowned
      :authority :non-authoritative
      :authoritative? false})))))

(defn- discovered-test-vars
  [namespace-symbol]
  (let [namespace-object (find-ns namespace-symbol)]
    (when-not namespace-object
      (exception "STAGE3-CATALOG-NAMESPACE"
                 "Stage3 test namespace was not loaded"
                 {:namespace namespace-symbol}))
    (->> (vals (ns-interns namespace-object))
         (filter #(-> % meta :test))
         (sort-by (fn [test-var]
                    [(or (:line (meta test-var)) Long/MAX_VALUE)
                     (str (:name (meta test-var)))]))
         (mapv (fn [test-var]
                 (symbol (str namespace-symbol) (str (:name (meta test-var)))))))))

(defn- default-catalog-loader
  [namespace-symbols]
  (doseq [namespace-symbol (sort-by str namespace-symbols)]
    (require namespace-symbol))
  (into {}
        (map (fn [namespace-symbol]
               [namespace-symbol (discovered-test-vars namespace-symbol)]))
        (sort-by str namespace-symbols)))

(defn- invoke-catalog-loader
  [loader namespace-symbols]
  ;; The production loader is deliberately namespace-scoped.  A one-argument
  ;; loader remains a useful explicit test seam for callers that want to
  ;; return a prebuilt map; it is never used by the production default.
  (try
    (loader namespace-symbols)
    (catch clojure.lang.ArityException _
      (loader))))

(def ^:dynamic *catalog-loader*
  "Catalog-loader seam.  Bind to nil to skip loading in isolated unit tests."
  default-catalog-loader-marker)

(def ^:dynamic *delegate-run-test-vars*
  "Delegate seam.  The default resolves SH-07 lazily at invocation time."
  nil)

(def ^:dynamic *exit-fn*
  "Exit seam for CLI tests; production defaults to the process exit function."
  (fn [code] (System/exit (int code))))

(def ^:dynamic *require-report?*
  "Production CLI requires a command-bound report file.  Isolated routing
  tests may bind this to false when they intentionally exercise stdout-only
  compatibility paths."
  true)

(def ^:dynamic *report-publisher*
  "Report publication seam.  The default writes a closed-shape JSON file
  atomically after lifecycle cleanup; tests can bind a recorder function."
  nil)

(def ^:dynamic *before-report-link-hook*
  "Test seam invoked after the target absence check and before the exclusive
  hard-link publication.  Production leaves this as a no-op; a test may use it
  to create the target and prove that publication never replaces a race winner."
  (fn [_target _temporary] nil))

(defn- default-delegate
  [selection]
  (require 'gravity.self-hosting.sh07-iteration-cache-runner)
  (let [run-test-vars
        (or (ns-resolve 'gravity.self-hosting.sh07-iteration-cache-runner
                        'run-test-vars)
            (exception "STAGE3-DELEGATE-ABSENT"
                       "SH-07 iteration cache runner is absent"
                       {}))]
    (run-test-vars selection)))

(defn- invoke-delegate
  [selection]
  (if *delegate-run-test-vars*
    (*delegate-run-test-vars* selection)
    (default-delegate selection)))

(defn- resolved-catalog-loader
  []
  (when-not (nil? *catalog-loader*)
    (if (= default-catalog-loader-marker *catalog-loader*)
      default-catalog-loader
      *catalog-loader*)))

(defn- normalize-batch-id
  [batch-id]
  (cond
    (keyword? batch-id) batch-id
    (string? batch-id) (keyword batch-id)
    :else batch-id))

(defn batch-definition
  "Return a fixed batch definition or throw for an unknown ID."
  [batch-id]
  (let [batch-id (normalize-batch-id batch-id)]
    (or (get fixed-batches batch-id)
        (exception "STAGE3-UNKNOWN-BATCH"
                   "Unknown Stage3 batch"
                   {:batch-id batch-id
                    :allowed (vec (map name fixed-batch-ids))}))))

(defn- summary-counts
  [summary]
  (merge {:test 0 :pass 0 :fail 0 :error 0}
         (select-keys (or summary {}) [:test :pass :fail :error])))

(defn- var-status
  [summary completed?]
  (if-not completed?
    :skipped
    (let [{:keys [fail error]} (summary-counts summary)]
      (cond
        (or (pos? error) (pos? fail)) :failed
        :else :passed))))

(defn- cache-map
  [cache]
  (or cache
      {:sh06-hits 0 :sh06-misses 0
       :core-hits 0 :core-misses 0
       :verification-hits 0 :verification-misses 0}))

(defn- merge-cache
  [left right]
  (merge-with + (cache-map left) (cache-map right)))

(defn- enrich-var-result
  [selector selection-index delegate-result skipped-tail]
  (let [delegate-result (or delegate-result {})
        candidate-completed? (not= false (:completed? delegate-result))
        ;; A missing delegate result is a skipped tail, never an implicit pass.
        completed? (and (some? delegate-result)
                        (or (contains? delegate-result :completed?)
                            (contains? delegate-result :test-result)
                            (contains? delegate-result :status))
                        candidate-completed?)
        test-result (summary-counts (:test-result delegate-result))
        status (if (and (empty? delegate-result) (seq skipped-tail))
                 :skipped
                 (var-status test-result completed?))]
    (merge
     {:test-var selector
      :selection-index selection-index
      :status status
      :counts test-result
      :test (:test test-result)
      :pass (:pass test-result)
      :fail (:fail test-result)
      :error (:error test-result)
      :cache (cache-map (:cache delegate-result))
      :elapsed-ms (long (or (:elapsed-ms delegate-result) 0))
      :completed? completed?
      :skipped? (= :skipped status)
      :skipped-tail? (= :skipped status)
      :skipped-tail (vec skipped-tail)
      :authority :non-authoritative
      :authoritative? false}
     delegate-result
     ;; Contract fields above are intentionally restored after merging so an
     ;; untrusted delegate cannot claim authority or reorder selection.
     {:test-var selector
      :selection-index selection-index
      :status status
      :counts test-result
      :test (:test test-result)
      :pass (:pass test-result)
      :fail (:fail test-result)
      :error (:error test-result)
      :cache (cache-map (:cache delegate-result))
      :elapsed-ms (long (or (:elapsed-ms delegate-result) 0))
      :completed? completed?
      :skipped? (= :skipped status)
      :skipped-tail? (= :skipped status)
      :skipped-tail (vec skipped-tail)
      :authority :non-authoritative
      :authoritative? false})))

(defn- normalized-var-results
  [selectors delegate-result]
  (let [delegate-results (vec (:test-var-results delegate-result))
        by-index
        (into {}
              (keep (fn [result]
                      (when (integer? (:selection-index result))
                        [(:selection-index result) result])))
              delegate-results)
        by-var
        (into {}
              (keep (fn [result]
                      (when-let [test-var (:test-var result)]
                        [test-var result])))
              delegate-results)
        skipped-tail (vec (:skipped-test-vars delegate-result))]
    (mapv
     (fn [selection-index selector]
       (let [result (or (get by-index selection-index)
                        (get by-var selector))
             tail (if result
                    skipped-tail
                    (vec (drop (inc selection-index) selectors)))]
         (enrich-var-result selector selection-index result tail)))
     (range)
     selectors)))

(defn- nonnegative-integer?
  [value]
  (and (integer? value) (not (neg? value))))

(def ^:private summary-counter-keys
  [:test :pass :fail :error])

(def ^:private cache-counter-keys
  [:sh06-hits :sh06-misses
   :core-hits :core-misses
   :verification-hits :verification-misses])

(defn- required-nonnegative-counters?
  [value keys]
  (and (map? value)
       (every? #(nonnegative-integer? (get value %)) keys)))

(defn- exact-cache-counters?
  [value]
  (and (map? value)
       (= (set cache-counter-keys) (set (keys value)))
       (required-nonnegative-counters? value cache-counter-keys)))

(defn- delegate-contract-error
  [message data]
  (exception "STAGE3-DELEGATE-CONTRACT"
             message
             data))

(defn- require-equal-when-present!
  [result field expected context]
  (when (and (contains? result field) (not= expected (get result field)))
    (delegate-contract-error
     (str "SH-07 delegate field " field " disagrees with the fixed selection")
     (assoc context :field field :expected expected :actual (get result field)))))

(defn- validate-delegate-result!
  "Fail closed on every identity/order/summary field supplied by SH-07.

  The runner owns the fixed selector vector.  A delegate may omit optional
  receipt fields, but it may never reorder, duplicate, invent, or relabel an
  executed result."
  [selectors fail-fast? delegate-result]
  (when-not (map? delegate-result)
    (delegate-contract-error
     "SH-07 delegate did not return a map"
     {:actual delegate-result}))
  (doseq [field [:test-var-results :skipped-test-vars :test-result
                 :cache :elapsed-ms]]
    (when-not (contains? delegate-result field)
      (delegate-contract-error
       "SH-07 delegate omitted a required aggregate contract field"
       {:field field :delegate-result delegate-result})))
  (when-not (vector? (:test-var-results delegate-result))
    (delegate-contract-error
     "SH-07 delegate executed results must be a vector"
     {:actual (:test-var-results delegate-result)}))
  (when-not (vector? (:skipped-test-vars delegate-result))
    (delegate-contract-error
     "SH-07 delegate skipped tail must be a vector"
     {:actual (:skipped-test-vars delegate-result)}))
  (when (and (contains? delegate-result :authoritative?)
             (true? (:authoritative? delegate-result)))
    (delegate-contract-error
     "SH-07 delegate aggregate cannot claim authority"
     {:field :authoritative?}))
  (when (and (contains? delegate-result :authority)
             (not= :non-authoritative (:authority delegate-result)))
    (delegate-contract-error
     "SH-07 delegate aggregate has non-nonauthoritative authority"
     {:field :authority :actual (:authority delegate-result)}))
  (when (and (contains? delegate-result :cache-authoritative?)
             (true? (:cache-authoritative? delegate-result)))
    (delegate-contract-error
     "SH-07 delegate aggregate cannot claim cache authority"
     {:field :cache-authoritative?}))
  (when (and (contains? delegate-result :fresh-authoritative-run-required?)
             (not (true? (:fresh-authoritative-run-required?
                          delegate-result))))
    (delegate-contract-error
     "SH-07 delegate aggregate must retain fresh-authoritative-run evidence"
     {:field :fresh-authoritative-run-required?}))
  (when (and (contains? delegate-result :maximum-entries)
             (not= maximum-cache-entries (:maximum-entries delegate-result)))
    (delegate-contract-error
     "SH-07 delegate cache maximum disagrees with fixed bound"
     {:expected maximum-cache-entries
      :actual (:maximum-entries delegate-result)}))
  (let [completed (:test-var-results delegate-result)
        skipped (:skipped-test-vars delegate-result)
        completed-count (count completed)
        expected-skipped (vec (drop completed-count selectors))
        results-by-index
        (mapv
         (fn [position result]
           (when-not (map? result)
             (delegate-contract-error
              "SH-07 delegate test-var result is not a map"
              {:position position :actual result}))
           (let [index (:selection-index result)
                 selector (:test-var result)]
             (when-not (= position index)
               (delegate-contract-error
                "SH-07 delegate executed prefix has a non-contiguous index"
                {:position position :selection-index index
                 :expected-index position}))
             (when-not (= selector (nth selectors position nil))
               (delegate-contract-error
                "SH-07 delegate test-var identity disagrees with fixed selection"
                {:position position :expected (nth selectors position nil)
                 :actual selector}))
             result))
         (range completed-count)
         completed)
        duplicate-indices
        (->> completed (map :selection-index) frequencies
             (keep (fn [[index count]] (when (> count 1) index))) vec)
        duplicate-vars
        (->> completed (map :test-var) frequencies
             (keep (fn [[selector count]] (when (> count 1) selector))) vec)]
    (when (seq duplicate-indices)
      (delegate-contract-error "SH-07 delegate returned duplicate selection indices"
                               {:duplicates duplicate-indices}))
    (when (seq duplicate-vars)
      (delegate-contract-error "SH-07 delegate returned duplicate test vars"
                               {:duplicates duplicate-vars}))
    (when-not (= expected-skipped skipped)
      (delegate-contract-error
       "SH-07 delegate skipped tail is not the exact ordered suffix"
       {:expected expected-skipped :actual skipped
        :completed-count completed-count}))
    (when (and (not fail-fast?) (seq skipped))
      (delegate-contract-error
       "A singleton Stage3 batch cannot report a skipped tail"
       {:skipped skipped}))
    (when (and fail-fast? (seq skipped)
               (not (some (fn [result]
                            (let [summary (summary-counts (:test-result result))]
                              (or (pos? (:fail summary))
                                  (pos? (:error summary)))))
                          completed)))
      (delegate-contract-error
       "Fail-fast delegate skipped work without a preceding failure"
       {:skipped skipped}))
    (when (and (seq completed)
               (not (every? #(and (map? (:test-result %))
                                  (required-nonnegative-counters?
                                   (:test-result %) summary-counter-keys))
                            completed)))
      (delegate-contract-error
       "Every executed result must provide exact non-negative summary counters"
       {:completed completed
        :required summary-counter-keys}))
    (when-not (and (map? (:test-result delegate-result))
                   (required-nonnegative-counters?
                    (:test-result delegate-result) summary-counter-keys))
      (delegate-contract-error
       "SH-07 aggregate result must provide exact non-negative summary counters"
       {:aggregate (:test-result delegate-result)
        :required summary-counter-keys}))
    (when-not (exact-cache-counters? (:cache delegate-result))
      (delegate-contract-error
       "SH-07 aggregate cache must provide exactly six non-negative counters"
       {:cache (:cache delegate-result)
        :required cache-counter-keys}))
    (when-not (nonnegative-integer? (:elapsed-ms delegate-result))
      (delegate-contract-error
       "SH-07 aggregate elapsed-ms must be a non-negative integer"
       {:elapsed-ms (:elapsed-ms delegate-result)}))
    (doseq [[position result] (map-indexed vector results-by-index)]
      (let [summary (summary-counts (:test-result result))
            status (var-status summary true)]
        (when-not (pos? (:test summary))
          (delegate-contract-error
           "Completed SH-07 result must contain at least one test"
           {:position position :summary summary}))
        (when-not (true? (:completed? result))
          (delegate-contract-error
           "Completed SH-07 prefix result is not marked completed"
           {:position position :result result}))
        (require-equal-when-present! result :status status
                                     {:position position})
        (require-equal-when-present! result :ok? (= :passed status)
                                     {:position position})
        (doseq [field [:test :pass :fail :error]]
          (require-equal-when-present! result field (get summary field)
                                       {:position position}))
        (when-not (and (nonnegative-integer? (:elapsed-ms result))
                       (exact-cache-counters? (:cache result)))
          (delegate-contract-error
           "SH-07 delegate elapsed/cache evidence is malformed"
           {:position position :result result
            :required-cache-keys cache-counter-keys}))
        (when (and (contains? result :authoritative?)
                   (true? (:authoritative? result)))
          (delegate-contract-error
           "SH-07 iteration result cannot claim authority"
           {:position position :result result}))
        (when (and (contains? result :authority)
                   (not= :non-authoritative (:authority result)))
          (delegate-contract-error
           "SH-07 iteration result has non-nonauthoritative authority"
           {:position position :result result}))
        (when (and (contains? result :cache-authoritative?)
                   (true? (:cache-authoritative? result)))
          (delegate-contract-error
           "SH-07 iteration result cannot claim cache authority"
           {:position position :result result}))))
    (let [failure-positions
          (vec
           (keep-indexed
            (fn [position result]
              (let [summary (:test-result result)]
                (when (or (pos? (:fail summary))
                          (pos? (:error summary)))
                  position)))
            completed))
          last-completed (dec completed-count)
          _ (when (and fail-fast?
                       (seq failure-positions)
                       (not= [last-completed] failure-positions))
              (delegate-contract-error
               "Fail-fast execution must stop at exactly one final failed result"
               {:failure-positions failure-positions
                :completed-count completed-count}))
          expected-cache
          (reduce
           (fn [totals result]
             (merge-with + totals (:cache result)))
           (zipmap cache-counter-keys (repeat 0))
           completed)
          _ (when-not (= expected-cache (:cache delegate-result))
              (delegate-contract-error
               "SH-07 aggregate cache must equal executed per-var cache deltas"
               {:expected expected-cache
                :actual (:cache delegate-result)}))
          expected-summary
          (assoc
           (reduce
            (fn [totals result]
              (merge-with + totals
                          (select-keys (summary-counts (:test-result result))
                                       [:test :pass :fail :error])))
            {:test 0 :pass 0 :fail 0 :error 0}
            completed)
           :type :summary)
          actual-summary (summary-counts (:test-result delegate-result))]
      (when (and (contains? delegate-result :test-result)
                 (not= (select-keys expected-summary [:test :pass :fail :error])
                       actual-summary))
        (delegate-contract-error
         "SH-07 delegate aggregate counts do not equal the executed prefix sum"
         {:expected expected-summary :actual (:test-result delegate-result)}))
      (when (and (contains? delegate-result :ok?)
                 (not= (:ok? delegate-result)
                       (and (zero? (:fail actual-summary))
                            (zero? (:error actual-summary))
                            (empty? skipped))))
        (delegate-contract-error
         "SH-07 delegate aggregate ok? disagrees with counts/skipped tail"
         {:actual (:ok? delegate-result)
          :summary actual-summary :skipped skipped})))
    (when (and (contains? delegate-result :test-vars)
               (not= selectors (:test-vars delegate-result)))
      (delegate-contract-error
       "SH-07 delegate selected vars disagree with fixed source-order vector"
       {:expected selectors :actual (:test-vars delegate-result)}))
    (when (and (contains? delegate-result :fail-fast?)
               (not= (boolean fail-fast?) (boolean (:fail-fast? delegate-result))))
      (delegate-contract-error
       "SH-07 delegate fail-fast mode disagrees with fixed batch"
       {:expected (boolean fail-fast?) :actual (:fail-fast? delegate-result)}))
    {:completed completed
     :skipped skipped
     :results-by-index results-by-index
     :summary (summary-counts (:test-result delegate-result))}))

(defn- aggregate-test-result
  [per-var-results delegate-result]
  (or (:test-result delegate-result)
      (assoc
       (reduce
        (fn [totals result]
          (merge-with + totals (select-keys result [:test :pass :fail :error])))
        {:test 0 :pass 0 :fail 0 :error 0}
        per-var-results)
       :type :summary)))

(defn run-batch
  "Run one fixed batch through the SH-07 iteration runner.

  The iteration cache bound is always one.  Multi-var batches receive
  fail-fast; singleton batches deliberately omit the generic fail-fast option,
  because the underlying runner rejects that option for a singleton."
  [batch-id]
  (let [definition (batch-definition batch-id)
        selectors (:test-vars definition)
        loader (resolved-catalog-loader)
        selected-namespaces (set (keep selector-namespace selectors))]
    (when loader
      (let [discovered (invoke-catalog-loader loader selected-namespaces)]
        (validate-fixed-catalog! fixed-batches discovered selected-namespaces)))
    (let [selection
          (cond-> {:test-vars selectors
                   :maximum-entries maximum-cache-entries}
            (> (count selectors) 1) (assoc :fail-fast? true))
          started (System/nanoTime)
          delegate-result (invoke-delegate selection)
          elapsed (long (/ (- (System/nanoTime) started) 1000000))
          _ (validate-delegate-result!
             selectors (> (count selectors) 1) delegate-result)
          per-var-results (normalized-var-results selectors delegate-result)
          test-result (aggregate-test-result per-var-results delegate-result)
          counts (summary-counts test-result)
          skipped-tail (vec (:skipped-test-vars delegate-result))
          failed? (or (pos? (:fail counts)) (pos? (:error counts)))
          status (cond
                   failed? :failed
                   (seq skipped-tail) :partial
                   :else :passed)
          cache (reduce merge-cache {} (map :cache per-var-results))]
      (array-map
       :schema :gravity/stage3-verification-batch-v1
       :stage :stage3
       :batch-id (:batch-id definition)
       :batch-name (:name definition)
       :selector-vector (vec selectors)
       :selection-order (vec selectors)
       :test-vars (vec selectors)
       :maximum-entries maximum-cache-entries
       :fail-fast? (> (count selectors) 1)
       :status status
       :ok? (and (= :passed status)
                 (zero? (:fail counts))
                 (zero? (:error counts)))
       :counts counts
       :test (:test counts)
       :pass (:pass counts)
       :fail (:fail counts)
       :error (:error counts)
       :test-result test-result
       :test-var-results per-var-results
       :delegate-test-var-results (vec (:test-var-results delegate-result))
       :skipped-tail skipped-tail
       :skipped-test-vars skipped-tail
       :stopped-early? (boolean (seq skipped-tail))
       :cache cache
       :delegate-cache (:cache delegate-result)
       :elapsed-ms (long (or (:elapsed-ms delegate-result) elapsed))
       :delegate-elapsed-ms (:elapsed-ms delegate-result)
       :authority :non-authoritative
       :authoritative? false
       :cache-authoritative? false
       :fresh-authoritative-run-required? true
       :delegate-result delegate-result))))

(defn run-gate
  "Run a supplied fixed batch sequence in order; no authority is inferred."
  ([] (run-gate fixed-batch-ids))
  ([batch-ids]
   (let [batch-ids (mapv normalize-batch-id batch-ids)
         _ (doseq [batch-id batch-ids] (batch-definition batch-id))
         results (mapv run-batch batch-ids)
         failed? (some #(not (:ok? %)) results)]
     {:schema :gravity/stage3-verification-gate-v1
      :stage :stage3
      :status (if failed? :failed :passed)
      :ok? (not failed?)
      :batch-ids batch-ids
      :batches results
      :authority :non-authoritative
      :authoritative? false
      :fresh-authoritative-run-required? true})))

(defn parse-arguments
  "Parse the strict Stage3 CLI.

  A production invocation is one fixed batch plus the command-bound report
  tuple `--report-file`, `--report-nonce`, `--report-check-id`, and
  `--report-command-identity-sha256`.  Generic namespace/test-var selectors,
  bare positional values, duplicate options, and arbitrary batch names are
  deliberately rejected."
  [arguments]
  (let [arguments (vec arguments)]
    (cond
      (or (= arguments ["--help"]) (= arguments ["-h"])
          (= arguments ["help"]))
      {:help? true}

      (empty? arguments)
      (exception "STAGE3-CLI-MISSING-BATCH"
                 "Stage3 CLI requires one fixed --batch value"
                 {:arguments arguments
                  :allowed (vec (map name fixed-batch-ids))})

      (not= "--batch" (first arguments))
      (if (some #(or (= "--namespace" %)
                     (= "--test-var" %)
                     (= "--exact" %)
                     (= "--fail-fast" %)
                     (= "--max-cache-entries" %))
               arguments)
        (exception "STAGE3-CLI-ARBITRARY-SELECTOR"
                   "Generic namespace/test-var selectors are not accepted"
                   {:arguments arguments})
        (if (and (string? (first arguments))
                 (string/starts-with? (first arguments) "--"))
          (exception "STAGE3-CLI-UNKNOWN-OPTION"
                     "Unknown Stage3 CLI option"
                     {:option (first arguments)
                      :arguments arguments})
          (exception "STAGE3-CLI-POSITIONAL"
                     "Stage3 CLI does not accept positional selectors"
                     {:arguments arguments})))

      :else
      (let [recognized #{"--batch" "--report-file" "--report-nonce"
                      "--report-check-id"
                      "--report-command-identity-sha256"}
            pair-options (loop [remaining arguments
                                result {}
                                seen #{}]
                           (if (empty? remaining)
                             result
                             (let [option (first remaining)
                                   value (second remaining)]
                               (when-not (contains? recognized option)
                                 (if (or (= "--namespace" option)
                                         (= "--test-var" option)
                                         (= "--exact" option)
                                         (= "--fail-fast" option)
                                         (= "--max-cache-entries" option))
                                   (exception "STAGE3-CLI-ARBITRARY-SELECTOR"
                                              "Generic namespace/test-var selectors are not accepted"
                                              {:arguments arguments})
                                   (exception "STAGE3-CLI-UNKNOWN-OPTION"
                                              "Unknown Stage3 CLI option"
                                              {:option option
                                               :arguments arguments})))
                               (when (contains? seen option)
                                 (exception (if (= option "--batch")
                                              "STAGE3-CLI-DUPLICATE-BATCH"
                                              "STAGE3-CLI-DUPLICATE-OPTION")
                                            "Stage3 CLI option was supplied more than once"
                                            {:option option
                                             :arguments arguments}))
                               (when (or (nil? value)
                                         (and (string? value)
                                              (string/starts-with? value "--")))
                                 (exception (if (= option "--batch")
                                              "STAGE3-CLI-MISSING-BATCH"
                                              "STAGE3-CLI-MISSING-OPTION")
                                            "Stage3 CLI option requires one value"
                                            {:option option
                                             :arguments arguments}))
                               (recur (nnext remaining)
                                      (assoc result option value)
                                      (conj seen option)))))]
        (when-not (contains? pair-options "--batch")
          (exception "STAGE3-CLI-MISSING-BATCH"
                     "Stage3 CLI requires one fixed --batch value"
                     {:arguments arguments
                      :allowed (vec (map name fixed-batch-ids))}))
        (let [batch-name (get pair-options "--batch")
              batch-id (normalize-batch-id batch-name)]
          (when-not (contains? (set fixed-batch-ids) batch-id)
            (exception "STAGE3-CLI-UNKNOWN-BATCH"
                       "Unknown Stage3 batch"
                       {:batch-name batch-name
                        :allowed (vec (map name fixed-batch-ids))}))
          (let [report-options
                {"--report-file" :report-file
                 "--report-nonce" :report-nonce
                 "--report-check-id" :report-check-id
                 "--report-command-identity-sha256"
                 :report-command-identity-sha256}
                report
                (into {}
                      (keep (fn [[option key]]
                              (when-let [value (get pair-options option)]
                                [key value])))
                      report-options)
                required-report-keys
                [:report-file :report-nonce :report-check-id
                 :report-command-identity-sha256]
                missing-report
                (vec (remove #(contains? report %) required-report-keys))]
            (when (and (or *require-report?* (contains? report :report-file))
                       (seq missing-report))
              (exception "STAGE3-CLI-MISSING-REPORT-BINDING"
                         "Production Stage3 CLI requires a command-bound report tuple"
                         {:arguments arguments :missing missing-report}))
            (when (and (contains? report :report-file)
                       (string/blank? (:report-file report)))
              (exception "STAGE3-CLI-INVALID-REPORT-FILE"
                         "Stage3 report path must be non-empty"
                         {:arguments arguments}))
            (when (and (contains? report :report-file)
                       (> (count (:report-file report))
                          maximum-report-path-chars))
              (exception "STAGE3-CLI-INVALID-REPORT-FILE"
                         "Stage3 report path exceeds its bounded length"
                         {:maximum maximum-report-path-chars
                          :arguments arguments}))
            (when (and (contains? report :report-file)
                       (not (.isAbsolute (java.io.File.
                                          (:report-file report)))))
              (exception "STAGE3-CLI-INVALID-REPORT-FILE"
                         "Stage3 report path must be absolute"
                         {:arguments arguments}))
            (doseq [field [:report-nonce :report-check-id]]
              (when (and (contains? report field)
                         (string/blank? (get report field)))
                (exception "STAGE3-CLI-INVALID-REPORT-BINDING"
                           "Stage3 report binding values must be non-empty"
                           {:field field :arguments arguments})))
            (doseq [field [:report-nonce :report-check-id]]
              (when (and (contains? report field)
                         (> (count (get report field))
                            maximum-report-binding-chars))
                (exception "STAGE3-CLI-INVALID-REPORT-BINDING"
                           "Stage3 report binding exceeds its bounded length"
                           {:field field
                            :maximum maximum-report-binding-chars
                            :arguments arguments})))
            (when (and (contains? report :report-command-identity-sha256)
                       (not (re-matches #"sha256:[0-9a-f]{64}"
                                        (:report-command-identity-sha256 report))))
              (exception "STAGE3-CLI-INVALID-COMMAND-IDENTITY"
                         "Stage3 command identity must be a lowercase sha256 value"
                         {:value (:report-command-identity-sha256 report)}))
            (merge {:help? false
                    :batch-id batch-id
                    :batch-name batch-name}
                   report)))))))

(defn usage
  []
  (str "Usage: clojure -M:stage3-verification --batch <"
       (string/join "|" (map name fixed-batch-ids))
       "> --report-file <path> --report-nonce <nonce>"
       " --report-check-id <id>"
       " --report-command-identity-sha256 <sha256:...>"))

(defn cleanup!
  "Flush output and stop agent executors, preserving fatal/interrupt causes."
  []
  (let [errors (atom [])
        attempt! (fn [operation]
                   (try
                     (operation)
                     (catch Throwable error
                       (swap! errors conj error))))]
    (attempt! flush)
    (attempt! #(.flush ^java.io.Writer *err*))
    (attempt! #(.flush ^java.io.OutputStream System/out))
    (attempt! #(.flush ^java.io.PrintStream System/err))
    (attempt! shutdown-agents)
    (let [errors (vec @errors)
          fatal? (fn [throwable]
                   (loop [current throwable
                          seen #{}]
                     (if (or (nil? current) (contains? seen current))
                       false
                       (if (or (instance? InterruptedException current)
                               (instance? ThreadDeath current)
                               (instance? VirtualMachineError current)
                               (instance? LinkageError current))
                         true
                         (recur (.getCause ^Throwable current)
                                (conj seen current))))))
          interrupt? (fn [throwable]
                       (loop [current throwable
                              seen #{}]
                         (if (or (nil? current)
                                 (contains? seen current))
                           false
                           (if (instance? InterruptedException current)
                             true
                             (recur (.getCause ^Throwable current)
                                    (conj seen current))))))
          fatal-errors (vec (filter fatal? errors))
          primary (or (first fatal-errors) (first errors))]
      (when (some interrupt? errors)
        (.interrupt (Thread/currentThread)))
      (when primary
        (doseq [suppressed (remove #(identical? primary %) errors)]
          (.addSuppressed ^Throwable primary suppressed))
        (throw primary)))
    nil))

(defn run-cli!
  "Execute parsed CLI arguments and return a non-authoritative result.

  Lifecycle cleanup and command-bound report publication belong to `-main`.
  Keeping this function cleanup-free is also the unit-test injection seam for
  delegate failures."
  [arguments]
  (let [parsed (parse-arguments arguments)]
    (if (:help? parsed)
      {:schema :gravity/stage3-verification-help-v1
       :status :help
       :exit-code 0
       :usage (usage)
       :authority :non-authoritative
       :authoritative? false}
      (let [loader (resolved-catalog-loader)
            result
            (binding [*catalog-loader* loader]
              (run-batch (:batch-id parsed)))]
        (merge result
               (select-keys parsed
                            [:report-file :report-nonce :report-check-id
                             :report-command-identity-sha256])
               {:exit-code (if (:ok? result) 0 1)})))))

(def ^:private usage-error-prefixes
  ;; Only syntax/allowlist selection errors are command usage.  A catalog
  ;; drift, delegate contract violation, fixture failure, or lifecycle error
  ;; is execution infrastructure and must retain the parsed report binding.
  ["STAGE3-CLI-" "STAGE3-UNKNOWN-BATCH"])

(defn- usage-error?
  [throwable]
  (let [id (str (:id (ex-data throwable)))]
    (boolean (some #(string/starts-with? id %) usage-error-prefixes))))

(defn- bounded-receipt
  [result]
  (let [per-var-results (vec (:test-var-results result))
        compact-vars
        (mapv #(select-keys % [:test-var :selection-index :status :counts
                               :cache :elapsed-ms :completed? :skipped-tail?])
              per-var-results)]
    {:schema :gravity/stage3-verification-receipt-v1
     :stage :stage3
     :status (:status result)
     :exit-code (long (or (:exit-code result)
                          (if (:ok? result) 0 1)))
     :batch-id (:batch-id result)
     :batch-name (:batch-name result)
     :selection-order (vec (:selection-order result))
     :executed-vars (mapv :test-var (filter :completed? per-var-results))
     :executed (mapv :test-var (filter :completed? per-var-results))
     :skipped-tail (vec (:skipped-tail result))
     :skipped-vars (vec (:skipped-tail result))
     :counts (summary-counts (:test-result result))
     :cache (cache-map (:cache result))
     :elapsed-ms (long (or (:elapsed-ms result) 0))
     :per-var-results compact-vars
     :authority :non-authoritative
     :authoritative? false
     :cache-authoritative? false
     :fresh-authoritative-run-required? true
     :report-file (:report-file result)
     :nonce (:report-nonce result)
     :check-id (:report-check-id result)
     :command-identity-sha256 (:report-command-identity-sha256 result)}))

(defn- json-escape
  [value]
  (let [value (str value)]
    (str "\""
         (reduce
          (fn [result character]
            (str result
                 (case character
                   \" "\\\""
                   \\ "\\\\"
                   \backspace "\\b"
                   \formfeed "\\f"
                   \newline "\\n"
                   \return "\\r"
                   \tab "\\t"
                   (if (< (int character) 0x20)
                     (format "\\u%04x" (int character))
                     character))))
          ""
          value)
         "\"")))

(declare json-value)

(defn- json-map-key
  [key]
  (cond
    (keyword? key) (name key)
    (symbol? key) (str key)
    (string? key) key
    :else (str key)))

(defn- json-value
  [value]
  (cond
    (nil? value) "null"
    (string? value) (json-escape value)
    (keyword? value) (json-escape (if-let [namespace-symbol (namespace value)]
                                    (str namespace-symbol "/" (name value))
                                    (name value)))
    (symbol? value) (json-escape (str value))
    (boolean? value) (if value "true" "false")
    (number? value) (str value)
    (map? value)
    (str "{" (string/join ","
                           (map (fn [[key item]]
                                  (str (json-escape (json-map-key key)) ":"
                                       (json-value item)))
                                (sort-by (comp str key) value))) "}")
    (or (vector? value) (seq? value) (set? value))
    (str "[" (string/join "," (map json-value value)) "]")
    :else
    (throw
     (ex-info "Stage3 receipt contains an unsupported JSON value"
              {:id "STAGE3-REPORT-UNSUPPORTED-VALUE"
               :class (.getName (class value))}))))

(defn- canonical-json
  [receipt]
  (json-value receipt))

(defn- no-follow-links
  []
  (into-array java.nio.file.LinkOption
              [java.nio.file.LinkOption/NOFOLLOW_LINKS]))

(defn- report-target-path
  [report-file]
  (let [path (.toPath (java.io.File. report-file))
        absolute (.normalize (.toAbsolutePath path))
        parent (.getParent absolute)]
    (when-not parent
      (exception "STAGE3-REPORT-PATH"
                 "Stage3 report path has no parent directory"
                 {:report-file report-file}))
    (when-not (java.nio.file.Files/isDirectory parent (no-follow-links))
      (exception "STAGE3-REPORT-PATH"
                 "Stage3 report parent is not a directory"
                 {:report-file report-file :parent (str parent)}))
    (when (java.nio.file.Files/isSymbolicLink parent)
      (exception "STAGE3-REPORT-PATH"
                 "Stage3 report parent may not be a symlink"
                 {:report-file report-file :parent (str parent)}))
    (when (or (java.nio.file.Files/isSymbolicLink absolute)
              (java.nio.file.Files/exists absolute (no-follow-links)))
      (exception "STAGE3-REPORT-EXISTS"
                 "Stage3 report target must not pre-exist"
                 {:report-file report-file}))
    {:target absolute :parent parent}))

(defn- publish-report-default!
  [report-file receipt]
  (let [{:keys [target parent]} (report-target-path report-file)
        ;; createTempFile is CREATE_NEW by contract.  The final hard-link
        ;; creation below is the no-replace primitive: the Java API does not
        ;; permit an implementation to replace an existing target when
        ;; createLink sees a race.
        temporary (java.nio.file.Files/createTempFile
                   parent ".gravity-stage3-report-" ".tmp"
                   (make-array java.nio.file.attribute.FileAttribute 0))]
    (try
      (let [report-bytes (.getBytes (str (canonical-json receipt) "\n")
                                    java.nio.charset.StandardCharsets/UTF_8)]
        (when (> (alength report-bytes) maximum-report-bytes)
          (throw
           (ex-info "Stage3 report exceeds bounded JSON size"
                    {:id "STAGE3-REPORT-BYTES"
                     :observed (alength report-bytes)
                     :maximum maximum-report-bytes})))
        (java.nio.file.Files/write
         temporary
         report-bytes
       (into-array java.nio.file.OpenOption
                   [java.nio.file.StandardOpenOption/WRITE
                    java.nio.file.StandardOpenOption/TRUNCATE_EXISTING])))
        (with-open [channel
                    (java.nio.channels.FileChannel/open
                   temporary
                   (into-array java.nio.file.OpenOption
                               [java.nio.file.StandardOpenOption/WRITE]))]
        (.force channel true))
      ;; The hook exists only for the deterministic race regression.  The
      ;; createLink call itself remains the atomic, exclusive commit point.
      (*before-report-link-hook* target temporary)
      (java.nio.file.Files/createLink target temporary)
      (with-open [channel
                  (java.nio.channels.FileChannel/open
                   target
                   (into-array java.nio.file.OpenOption
                               [java.nio.file.StandardOpenOption/WRITE]))]
        (.force channel true))
      ;; A directory fsync is not supported by every provider/platform.  When
      ;; it is available, force the directory before and after unlinking the
      ;; temporary name so both the link and final nlink=1 state are durable.
      (try
        (with-open [channel
                    (java.nio.channels.FileChannel/open
                     parent
                     (into-array java.nio.file.OpenOption
                                 [java.nio.file.StandardOpenOption/READ]))]
          (.force channel true))
        (catch java.lang.UnsupportedOperationException _)
        (catch java.io.IOException _))
      (java.nio.file.Files/deleteIfExists temporary)
      (try
        (with-open [channel
                    (java.nio.channels.FileChannel/open
                     parent
                     (into-array java.nio.file.OpenOption
                                 [java.nio.file.StandardOpenOption/READ]))]
          (.force channel true))
        (catch java.lang.UnsupportedOperationException _)
        (catch java.io.IOException _))
      report-file
      (catch Throwable error
        (try (java.nio.file.Files/deleteIfExists temporary) (catch Throwable _))
        (throw error)))))

(defn publish-report!
  "Atomically publish one closed-shape JSON receipt after cleanup.

  The Python wrapper independently validates its private parent directory and
  report shape.  The Clojure side still rejects an existing/symlink target and
  never replaces a target leaf."
  [report-file receipt]
  (if *report-publisher*
    (*report-publisher* report-file receipt)
    (publish-report-default! report-file receipt)))

(defn- bounded-error-text
  [value maximum]
  (let [text (str (or value ""))]
    (if (> (count text) maximum)
      (subs text 0 maximum)
      text)))

(defn- infrastructure-failure-receipt
  "Build a bounded command-bound receipt for non-test infrastructure failure.

  This schema is intentionally not a verification receipt: it carries no
  selector, execution, summary, or cache evidence that the wrapper could
  mistake for a valid fixed batch.  The Python supervisor rejects this schema
  as test evidence and maps the child to its infrastructure-failure outcome.
  The report binding remains present whenever strict argument parsing reached
  the selected batch."
  [throwable report-binding]
  (let [report-binding (or report-binding {})
        batch-id (:batch-id report-binding)]
    {:schema :gravity/stage3-infrastructure-failure-v1
     :stage :stage3
     :status :infrastructure-failure
     :exit-code 1
     :batch-id batch-id
     :batch-name (some-> batch-id name)
     :error-id (bounded-error-text
                (or (:id (ex-data throwable))
                    "STAGE3-INFRASTRUCTURE-FAILURE") 128)
     :error-class (bounded-error-text (.getName (class throwable)) 256)
     :error-message (bounded-error-text (.getMessage throwable) 1024)
     :authority :non-authoritative
     :authoritative? false
     :cache-authoritative? false
     :fresh-authoritative-run-required? true
     :report-file (:report-file report-binding)
     :nonce (:report-nonce report-binding)
     :check-id (:report-check-id report-binding)
     :command-identity-sha256 (:report-command-identity-sha256 report-binding)}))

(defn- cleanup-preserving!
  [original]
  (try
    (cleanup!)
    (catch Throwable cleanup-error
      (if original
        (.addSuppressed ^Throwable original cleanup-error)
        (throw cleanup-error)))))

(defn- report-binding-from-arguments
  [arguments]
  (try
    (select-keys (parse-arguments arguments)
                 [:batch-id :batch-name :report-file :report-nonce :report-check-id
                  :report-command-identity-sha256])
    (catch Throwable _ {})))

(defn -main
  [& arguments]
  (let [outcome
        (try
          {:result (run-cli! arguments)}
          (catch clojure.lang.ExceptionInfo error
            (if (usage-error? error)
              {:result
               {:schema :gravity/stage3-verification-cli-error-v1
                :status :usage
                :exit-code 2
                :error-id (:id (ex-data error))
                :error-data (ex-data error)
                :authority :non-authoritative
                :authoritative? false}}
              ;; A delegate/fixture ExceptionInfo is an execution failure,
              ;; never a usage receipt.  Preserve it as the primary cause so
              ;; cleanup failures can only be suppressed, not substituted.
              {:throwable error}))
          (catch Throwable throwable
            ;; Errors and interrupts are rethrown after the receipt and
            ;; cleanup.  Their cause is never converted to usage exit 2.
            {:throwable throwable}))
        throwable (:throwable outcome)
        result (:result outcome)
        report-binding
        (merge (report-binding-from-arguments arguments)
               (select-keys (or result {})
                            [:batch-id :batch-name :report-file :report-nonce
                             :report-check-id :report-command-identity-sha256]))
        receipt (if throwable
                  (infrastructure-failure-receipt throwable report-binding)
                  (if (= :usage (:status result))
                    (merge (bounded-receipt result)
                           {:status :usage
                            :error-id (:error-id result)
                            :error-data (:error-data result)})
                    (bounded-receipt result)))
        cleanup-error
        (try
          (cleanup-preserving! throwable)
          nil
          (catch Throwable error error))
        final-receipt
        (if (and cleanup-error (nil? throwable))
          (infrastructure-failure-receipt cleanup-error report-binding)
          receipt)
        report-file (:report-file final-receipt)
        publish-error
        (when report-file
          (try
            (publish-report! report-file final-receipt)
            nil
            (catch Throwable error error)))
        primary-error (or throwable cleanup-error)
        _ (when (and publish-error primary-error)
            (.addSuppressed ^Throwable primary-error publish-error))]
    (if report-file
      ;; Stdout is diagnostics only when the command-bound report exists; the
      ;; wrapper trusts the atomically published JSON file, never this line.
      (binding [*out* *err*]
        (prn {:stage :stage3
              :report-file report-file
              :report-published? (nil? publish-error)
              :status (:status final-receipt)
              :cleanup-error-id
              (when cleanup-error
                (bounded-error-text (:id (ex-data cleanup-error)) 128))
              :cleanup-error-class
              (when cleanup-error
                (bounded-error-text (.getName (class cleanup-error)) 256))
              :cleanup-error-message
              (when cleanup-error
                (bounded-error-text (.getMessage cleanup-error) 512))}))
      ;; Compatibility-only stdout path used by isolated tests when
      ;; *require-report?* is false.  It is never an authority receipt.
      (prn final-receipt))
    (cond
      throwable (throw throwable)
      cleanup-error (throw cleanup-error)
      publish-error (do
                      (binding [*out* *err*]
                        (prn {:stage :stage3
                              :report-published? false
                              :error (bounded-error-text
                                      (.getMessage ^Throwable publish-error)
                                      512)}))
                      (*exit-fn* 1))
      :else (do
              (when (pos? (:exit-code final-receipt 0))
                (*exit-fn* (:exit-code final-receipt)))
              result))))
