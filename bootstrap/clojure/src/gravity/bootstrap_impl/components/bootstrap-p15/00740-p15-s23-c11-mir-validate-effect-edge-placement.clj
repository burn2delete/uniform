

(defn p15-s23-c11-mir-validate-effect-edge-placement!
  [source-path mir operation-products]
  (gravity.mir/validate-effect-edge-placement! #'p15-s23-c11-mir-require! source-path mir operation-products))

(defn p15-s23-c11-mir-validate-data-flow!
  [source-path checked-core mir operation-products]
  (gravity.mir/validate-data-flow!
   {:require! #'p15-s23-c11-mir-require!
    :entry-parameter! #'p15-s23-c11-mir-entry-parameter!
    :expected-data-flow #'p15-s23-c11-mir-expected-data-flow
    :expected-terminator-uses #'p15-s23-c11-mir-expected-terminator-uses
    :runtime-check-token-id #'p15-s23-c11-mir-runtime-check-token-id
    :runtime-check-operation-id #'p15-s23-c11-mir-runtime-check-operation-id
    :expected-value #'p15-s23-c11-mir-expected-value
    :expected-runtime-check-value #'p15-s23-c11-mir-expected-runtime-check-value
    :runtime-check-source #'p15-s23-c11-mir-runtime-check-source
    :valid-dominance-edge? #'p15-s23-c11-mir-valid-dominance-edge?
    :valid-terminator-use? #'p15-s23-c11-mir-valid-terminator-use?}
   source-path checked-core mir operation-products))

(defn p15-s23-c11-mir-validate-constructed!*
  "Independent Clojure verifier for the untrusted result returned by the
  pinned Gravity builder.  It bounds the carrier before traversal, then checks
  C6-C10 fact parity, CFG/SSA dominance, direct effects, safety, provenance,
  target independence, and the narrow B1 handoff without recursively walking
  attacker-controlled graphs."
  [source-path checked-core mir]
  (gravity.mir/validate-constructed!
   {:validate-carrier! #'p15-s23-c11-mir-bounded-value!
    :validate-module-envelope! #'p15-s23-c11-mir-validate-module-envelope!
    :validate-fact-tables! #'p15-s23-c11-mir-validate-fact-tables!
    :validate-operations! #'p15-s23-c11-mir-validate-operations!
    :validate-runtime-checks! #'p15-s23-c11-mir-validate-runtime-checks!
    :validate-cfg! #'p15-s23-c11-mir-validate-cfg!
    :validate-effect-edge-placement! #'p15-s23-c11-mir-validate-effect-edge-placement!
    :validate-data-flow! #'p15-s23-c11-mir-validate-data-flow!}
   source-path checked-core mir))

(defn p15-s23-c11-mir-validate-constructed!
  [source-path checked-core mir]
  (binding [*p15-s23-c11-mir-diagnostic-context*
            (p15-s23-c11-mir-diagnostic-context
             checked-core {} mir)]
   (try
    (p15-s23-c11-mir-validate-constructed!*
      source-path checked-core mir)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-host-stack error))
    (catch AssertionError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-assertion error))
    (catch LinkageError error
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-linkage error))
    (catch clojure.lang.ExceptionInfo exception
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-diagnostic exception))
    (catch Exception exception
      (p15-s23-c11-mir-contain-exception!
       source-path :contained-c11-constructed-verifier-host-failure
       exception)))))



(defn p15-s23-c11-mir-v2-source-family!
  "A semantic family gate on already authenticated Core, never MIR authority."
  [source-path checked-core version]
  (when (= 2 version)
    (let [parameter (p15-s23-c11-mir-entry-parameter! source-path checked-core)]
      (p15-s23-c11-mir-require!
       (bool-branch-join/source-family? checked-core (some? parameter))
       "C11-MODULE" source-path checked-core
       :v2-bool-branch-join-source-family)))
  (when (= 3 version)
    (p15-s23-c11-mir-require-trusted-carrier! source-path :flat-bool-original-core checked-core :reject)
    (p15-s23-c11-mir-require!
     (bool-branch-join/flat-sequence-source-family? checked-core)
     "C11-MODULE" source-path checked-core :v3-flat-bool-sequence-source-family))
  nil)

(defn- p15-s23-c11-mir-verify-flat-produced!
  "Run the selected real Gravity verifier on a freshly produced v3 MIR.
   The caller has already authenticated the Core and source binding; this
   helper accepts neither a supplied report nor a cached acceptance result."
  [source-path source-binding checked-core mir]
  (p15-s23-c11-mir-require-trusted-carrier!
   source-path :flat-bool-original-core checked-core :reject)
  (p15-s23-c11-mir-require-trusted-carrier!
   source-path :flat-bool-produced-mir mir :reject)
  (let [report
        (p15-s23-stage2-runtime-execute-function
         {:engine :gravity-c11-pinned-builder-host-runner
          :compiler-artifact-plan? true}
         (:plan source-binding)
         (p15-s23-c11-mir-selected-verifier-function 3)
         [checked-core mir])]
    (p15-s23-c11-mir-require-trusted-carrier!
     source-path :flat-bool-gravity-verifier-report report :reject)
    (p15-s23-c11-mir-require!
     (and (map? report)
          (= #{:artifact :schema-version :scope :phase :status :checked-core-artifact-id
               :mir-module-id :checks :preserves :diagnostics :nonclaims} (set (keys report)))
          (= :gravity/sh12-branch-join-mir-report (:artifact report))
          (= 1 (:schema-version report)) (= :verification (:phase report))
          (= :closed-flat-bool-literal-sequence (:scope report))
          (= :passed (:status report)) (= [] (:diagnostics report))
          (= (:artifact-id checked-core) (:checked-core-artifact-id report))
          (= (:module-id mir) (:mir-module-id report))
          (vector? (:checks report)) (vector? (:preserves report))
          (vector? (:nonclaims report))
          (= [:single-function-linear :blocks-terminators-and-cfg-edges
              :same-block-definitions-uses-and-return-dominance
              :type-fact-preservation :effect-order-preservation
              :ownership-capability-and-profile-preservation
              :safety-outcome-preservation :source-origin-preservation]
             (:checks report))
          (= [:type-facts :effect-facts :ownership-facts :capability-facts
              :safety-facts :profile-target-facts :source-spans :source-origin-map
              :generated-origin-chains :control-flow-edges :data-flow-edges
              :dominance-facts] (:preserves report))
          (= [:generic-c11-verifier :independent-acceptance :optimizer-credit
              :backend-credit :self-hosted?] (:nonclaims report)))
     "C11-VERIFY" source-path checked-core :actual-flat-gravity-verifier-report))
  nil)

;; These helpers validate/project DATA. They never authenticate a supplied
;; report. Only the lexical public constructor/verifier calls the fresh C6
;; owner; standalone final-artifact validation is relative to checked Core.
(declare p15-s23-c11-mir-expected-ingress-semantic
         p15-s23-c11-ingress-source-path
         p15-s23-c6c10-source-binding!
         p15-s23-c6c10-project-expression-owner-metadata
         p15-s23-stage2-current-expression-development-binding!)

(def p15-s23-c11-core-policy-keys
  #{:source-interface :source-policy-binding-id :admission-status
    :verification-scope :source-policy-receipt :compiler-program})
(def p15-s23-c11-core-policy-receipt-keys
  #{:source-interface :admission-status :verification-scope :reader-plan-binding
    :reader-source-revision :reader-result-identity :authenticated-envelope
    :envelope-verification :self-hosted? :expression-binding
    :owner-metadata-content-id :projection-contract})
(def p15-s23-c11-core-program-projection-keys
  [:source-contract-version :source-byte-count :source-content-hash
   :plan-semantic-hash :functions-semantic-hash :builder-semantic-hash :verifier-semantic-hash
   :source-mode])

(def p15-s23-c11-core-projection-contract
  {:artifact :gravity/c6-reader-owner-metadata-projection :schema-version 1
   :encoding :owner-canonical-semantic-v1
   :sorted-metadata-policy :default-only :core-carrier-policy :reject})

(defn- p15-s23-c11-core-policy-content-id? [value]
  (and (identical? String (class value))
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-c11-mir-require-core-policy-data!
  ([checked-core context policy]
   (p15-s23-c11-mir-require-core-policy-data! checked-core context policy 2))
  ([checked-core context policy version]
  (p15-s23-c11-mir-contract version)
  (let [path (p15-s23-c11-ingress-source-path context)]
    (p15-s23-c11-mir-require-trusted-carrier! path :current-core-policy-data policy :reject)
    (let [receipt (:source-policy-receipt policy)
        compiler (:compiler-program policy)
        current-source? (= :development-current (:source-mode compiler))
        contract (if current-source?
                   (p15-s23-c6c10-source-binding!
                    path version :development-current)
                   (p15-s23-c6c10-source-program-contract version))
        header {:interface :gravity/c6-c10 :version version}
        program-keys (cond-> p15-s23-c11-core-program-projection-keys
                       (= 3 version) (conj :semantic-family))
        expected-program (cond->
                          (assoc (select-keys contract (rest program-keys))
                                 :source-contract-version version)
                           current-source?
                           (assoc :source-mode :development-current))
        envelope (:authenticated-envelope receipt)
        expression (:expression-binding receipt)
        expression-contract c6-source-contract/current-expression-program
        current-expression? (and (contains? #{2 3} version)
                                 (= :development-current
                                    (:source-mode expression)))
        expected-expression
        (if current-expression?
          (p15-s23-c6c10-project-expression-owner-metadata
           path
           (dissoc (p15-s23-stage2-current-expression-development-binding!
                   path)
                   :plan))
          (select-keys expression-contract
                       [:source-content-hash :semantic-hash
                        :functions-semantic-hash :artifact-hash
                        :function-count]))]
    (p15-s23-c11-mir-require!
     (and (= (cond-> p15-s23-c11-core-policy-keys (= 3 version) (conj :semantic-family)) (set (keys policy)))
          (= (count (cond-> p15-s23-c11-core-policy-keys (= 3 version) (conj :semantic-family))) (count policy))
          (= (cond-> p15-s23-c11-core-policy-receipt-keys (= 3 version) (conj :semantic-family)) (set (keys receipt)))
          (= (count (cond-> p15-s23-c11-core-policy-receipt-keys (= 3 version) (conj :semantic-family))) (count receipt))
          (identical? Long (class (get-in policy [:source-interface :version])))
          (identical? Long (class (:source-contract-version compiler)))
          (identical? Long (class (get-in receipt [:source-interface :version])))
          (= header (:source-interface policy) (:source-interface receipt))
          (if (= 3 version)
            (= :closed-flat-bool-literal-sequence (:semantic-family policy)
               (:semantic-family receipt) (:semantic-family compiler)) true)
          (= :development-unadmitted (:admission-status policy) (:admission-status receipt)
             (get-in envelope [:source-rule :admission-status]))
          (= :selected-current-source-host-seed (:verification-scope policy) (:verification-scope receipt))
          (= :passed (:envelope-verification receipt))
          (false? (:self-hosted? receipt))
          (= p15-s23-c11-core-projection-contract (:projection-contract receipt))
          (identical? Long (class (get-in receipt [:projection-contract :schema-version])))
          (every? p15-s23-c11-core-policy-content-id?
            [(:owner-metadata-content-id receipt)
             (get-in receipt [:reader-plan-binding :instruction-summary-content-id])
             (get-in receipt [:reader-source-revision :function-shapes-content-id])
             (get-in envelope [:source-rule :function-shapes-content-id])
             (get-in envelope [:source-rule :owner-source-rule-content-id])
             (get-in envelope [:gravity-template-replay :checks-content-id])
             (get-in envelope [:gravity-template-replay :diagnostics-content-id])
             (:functions-content-id expression)
             (:declared-plan-compiler-labels-content-id expression)
             (:owner-binding-content-id expression)])
          (= expected-program compiler)
          (= :accepted (:status envelope)) (= :c2-reader (:stage envelope))
          (= #{:actual-sh03-reader-result-id :actual-sh03-semantic-product-binding :actual-c2-artifact-id
               :actual-reader-product-integrity-hash :actual-reader-source-id}
             (set (keys (:reader-result-identity receipt))))
          (every? string? (map (:reader-result-identity receipt)
                              [:actual-sh03-reader-result-id :actual-c2-artifact-id
                               :actual-reader-product-integrity-hash :actual-reader-source-id]))
          (= (get-in c6-source-contract/current-reader-program [:binding :source-content-hash])
             (get-in receipt [:reader-source-revision :source-content-hash]))
          (identical? Long (class (:source-contract-version expression)))
          (= 2 (:source-contract-version expression))
          (= expected-expression
             (if current-expression?
               expression
               (select-keys expression
                            [:source-content-hash :semantic-hash
                             :functions-semantic-hash :artifact-hash
                             :function-count])))
          (= (:source-policy-binding-id policy)
             (p15-s23-c6c10-canonical-digest
              path {:domain (if (= 3 version) :gravity/c6-c10-source-policy-v3 :gravity/c6-c10-source-policy-v2)
                    :artifact-id (:artifact-id checked-core)
                    :context context :policy receipt})))
     "C11-MODULE" path {} :current-core-source-policy-data)
    policy))))

(defn p15-s23-c11-mir-core-policy-projection
  ([checked-core context fresh-report]
   (p15-s23-c11-mir-core-policy-projection checked-core context fresh-report 2))
  ([checked-core context fresh-report version]
  ;; A projection, not a saved-report admission API. Callers establish fresh
  ;; authority before projecting; the structural helper only checks coherence.
  (p15-s23-c11-mir-require-trusted-carrier!
   (p15-s23-c11-ingress-source-path context) :fresh-core-report-projection fresh-report :reject)
  (p15-s23-c11-mir-require-core-policy-data!
   checked-core context
   (assoc (select-keys fresh-report
            (cond-> [:source-interface :source-policy-binding-id :admission-status
                     :verification-scope :source-policy-receipt]
              (= 3 version) (conj :semantic-family)))
          :compiler-program (select-keys (:source-binding fresh-report)
                              (cond-> p15-s23-c11-core-program-projection-keys
                                (= 3 version) (conj :semantic-family)))) version)))

(defn p15-s23-c11-mir-core-policy-hash
  ([policy] (p15-s23-c11-mir-core-policy-hash policy 2))
  ([policy version]
  (when policy (p15-s23-c6c10-canonical-digest
                "<c11-core-policy>"
                {:domain (if (= 3 version) :gravity/c11-checked-core-policy-v3 :gravity/c11-checked-core-policy-v2)
                 :policy policy}))))

(defn p15-s23-c11-mir-validate-ingress-data!
  [checked-core context ingress version]
  (when (contains? #{2 3} version)
    (p15-s23-c11-mir-require-trusted-carrier!
     (p15-s23-c11-ingress-source-path context) :core-ingress-policy-data ingress :reject))
  (let [base (p15-s23-c11-mir-expected-ingress-semantic checked-core context)
        policy (:source-policy ingress)]
    (if (contains? #{2 3} version)
      (do
        (p15-s23-c11-mir-require-core-policy-data! checked-core context policy version)
        (p15-s23-c11-mir-require!
         (and (= :gravity-source-pure (:checked-core-ingress-mode base))
              (identical? Long (class (:schema-version ingress)))
              (= (assoc base :schema-version version :source-policy policy) ingress))
         "C11-MODULE" (p15-s23-c11-ingress-source-path context) {} :current-core-ingress-policy-shape))
      (p15-s23-c11-mir-require!
       (= base ingress) "C11-MODULE" (p15-s23-c11-ingress-source-path context) {}
       :authenticated-checked-core-ingress-authority))
    ingress))

(defn p15-s23-c11-mir-core-policy-binding [version policy]
  (when (contains? #{2 3} version)
    (p15-s23-c11-mir-require-trusted-carrier! "<c11-core-policy>" :core-policy-hash-input policy :reject)
    (p15-s23-c11-mir-require!
     (and (map? policy) (string? (:source-policy-binding-id policy)))
     "C11-MODULE" "<c11-core-policy>" {} :current-core-policy-binding-required)
    {:source-policy-binding-id (:source-policy-binding-id policy)
     :source-policy-hash (p15-s23-c11-mir-core-policy-hash policy version)}))
