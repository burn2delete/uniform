

(defn sh07-core-from-authenticated-request
  [resolution-artifact authenticated-request]
  (let [run (sh07-core-run-request-for-test
             resolution-artifact authenticated-request)
          core (:canonical-core-artifact run)
          source-path
          (sh07-core-source-path-from-resolution resolution-artifact)
          boundary
          {:slice :SH-07
           :owner :gravity-source
           :adapter-contract sh07-core-adapter-contract
           :plan-binding
           (dissoc @sh07-core-cached-binding :plan :source-path)
           :authenticated-sh06-resolution-artifact resolution-artifact
           :authenticated-core-request authenticated-request
           :raw-template-result (:raw-template-result run)
           :canonical-core-artifact core
           :digest-requests (:digest-requests run)
           :resolved-digests (:resolved-digests run)
           :template-verification (:template-verification run)
           :resolved-verification (:resolved-verification run)
           :authenticated-envelope-descriptor
           {:artifact :gravity/sh07-authenticated-envelope-descriptor
            :semantic-artifact-id (:artifact-id core)
            :source-revision-id
            (get-in authenticated-request
                    [:lineage :source-revision-id])
            :authenticated-sh06-artifact-id
            (get-in authenticated-request
                    [:lineage :authenticated-sh06-artifact-id])
            :sh06-semantic-projection-id
            (get-in authenticated-request
                    [:lineage :sh06-semantic-projection-id])}
           :authenticated-envelope
           {:artifact :gravity/sh07-authenticated-envelope
            :semantic-artifact-id (:artifact-id core)
            :authenticated-sh06-artifact-id
            (get-in authenticated-request
                    [:lineage :authenticated-sh06-artifact-id])
            :actual-source-path source-path}
           :target-source-reread? false
           :clojure-adapter-residual? true
           :self-hosted? false}
          artifact-base
          {:kind :gravity/sh07-core-artifact
           :status :accepted
           :slice :SH-07
           :task "SH-07-B47"
           :document-set ["L2" "L3" "L6" "L7" "L9" "C5" "C6"]
           :governing-document sh07-core-governing-document
           :artifact-id (:artifact-id core)
           :sh06-resolution-artifact resolution-artifact
           :gravity-core-boundary boundary
           :provenance {:source-path source-path}
           :pass
           {:name :c6-gravity-core-lowering-b47
            :input :authenticated-sh06-resolution
            :output :canonical-core
            :owner :gravity.checked-core}
           :execution-boundary
           {:gravity-owned
            [:core-template-construction :template-verification
             :control-flow-construction :control-flow-verification
             :reference-construction :reference-verification
             :var-reference-construction
             :var-reference-verification
             :call-construction :call-verification
             :function-record-construction
             :function-record-verification
             :call-edge-construction
             :call-edge-verification
             :recursion-component-construction
             :recursion-component-verification
             :keyword-map-lookup-construction
             :keyword-map-lookup-verification
             :lexical-binding-construction
             :lexical-binding-verification
             :loop-binding-construction
             :loop-binding-verification
             :recur-target-construction
             :recur-target-verification
             :recur-transfer-construction
             :recur-transfer-verification
             :mutation-construction
             :mutation-verification
             :error-transfer-construction
             :error-transfer-verification
             :error-handler-construction
             :error-handler-verification
             :match-branch-construction
             :match-branch-verification
             :match-decision-skeleton-construction
             :match-decision-skeleton-verification
             :match-pattern-record-construction
             :match-pattern-record-verification
             :fragment-partition-validation
             :fragment-ordered-lowering
             :module-assembly-validation
             :resolved-verification]
            :clojure-seed-owned
            [:plan-execution :sh06-projection-authentication
             :digest-resolution :envelope-binding
             :compatibility-routing
             :fragment-envelope-construction
             :final-artifact-binding]
            :downstream-fact-statuses
            {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
            :pending-lowering-families
            [:cross-file-module-linking
             :incremental-fragment-cache
             :parallel-fragment-lowering
             :whole-program-execution
             :alias-qualified-type-references
             :alias-qualified-var-references
             :alias-qualified-set-mutations
             :qualified-var-references
             :var-profile-legality-sh09
             :keyword-default-value-lookup
             :general-callable-keywords
             :destructuring-bindings
             :variadic-function-recur
             :recur-type-compatibility
             :higher-order-and-cross-module-recursion
             :try-finally
             :try-protected-sequencing
             :try-handler-sequencing
             :map-list-set-record-constructor-patterns
             :variable-width-vector-patterns
             :duplicate-pattern-binding-policy
             :guard-patterns
             :match-exhaustiveness
             :match-result-type-join]
            :sh07-complete? false
            :self-hosted? false}
           :capability-based-proof nil
           :diagnostics []}
          upstream-verification
          (sh06-resolution-artifact-verification resolution-artifact)
          proof
          (sh07-core-proof-from-checks
           (sh07-core-verification-checks
            artifact-base artifact-base upstream-verification))]
    (assoc artifact-base :capability-based-proof proof)))

(defn sh07-core-from-resolution-artifact
  [resolution-artifact]
  (sh07-core-from-authenticated-request
   resolution-artifact
   (sh07-core-authenticated-request resolution-artifact)))

(def ^:private sh07-core-verification-session-schema-version 2)
(def ^:private sh07-core-verification-session-kind
  :gravity/sh07-core-verification-session)

(def ^:private sh07-core-verification-session-upstream-identity-keys
  [:source-path :source-byte-count :source-content-hash
   :plan-semantic-hash :functions-semantic-hash :function-count
   :function-names-hash :function-shapes-hash :public-function-hashes
   :public-function-shapes])

(defn- sh07-core-verification-session-expected-integrity
  [expected]
  (reader-canonical-hash
   {:domain :gravity/sh07-verification-session-expected-v1
    :expected (sh07-core-exact-comparison-value expected)}))

(defn- sh07-core-verification-session-upstream-snapshot
  [resolution-artifact]
  ;; The artifact digest covers every retained SH-06 product; the explicit
  ;; identity projection keeps the source and plan/function pins visible in
  ;; the session binding as well. Replay compares this immutable snapshot and
  ;; never trusts an artifact-id by itself.
  (let [plan-binding
        (get-in resolution-artifact
                [:gravity-resolution-boundary :plan-binding])]
    {:artifact-id (:artifact-id resolution-artifact)
     :source-path (sh07-core-source-path-from-resolution resolution-artifact)
     :source-and-plan-identities
     (select-keys plan-binding
                  sh07-core-verification-session-upstream-identity-keys)
     :plan-binding-integrity
     (reader-canonical-hash
      {:domain :gravity/sh07-verification-session-plan-binding-v1
       :plan-binding (sh07-core-exact-comparison-value plan-binding)})
     :resolution-artifact-integrity
     (reader-canonical-hash
      {:domain :gravity/sh07-verification-session-upstream-v1
       :resolution-artifact
       (sh07-core-exact-comparison-value resolution-artifact)})}))

(defn- sh07-core-verification-session-binding
  ([upstream-snapshot expected upstream-verification]
   (sh07-core-verification-session-binding
    upstream-snapshot expected upstream-verification
    (sh07-core-verification-session-expected-integrity expected)))
  ([upstream-snapshot expected upstream-verification expected-integrity]
   (reader-canonical-hash
    {:domain :gravity/sh07-verification-session-v1
     :resolution-artifact-id (:artifact-id upstream-snapshot)
     :expected-artifact-id (:artifact-id expected)
     :expected-integrity expected-integrity
     :source-path (:source-path upstream-snapshot)
     :source-and-plan-identities
     (:source-and-plan-identities upstream-snapshot)
     :plan-binding-integrity (:plan-binding-integrity upstream-snapshot)
     :resolution-artifact-integrity
     (:resolution-artifact-integrity upstream-snapshot)
     :upstream-status (:status upstream-verification)})))