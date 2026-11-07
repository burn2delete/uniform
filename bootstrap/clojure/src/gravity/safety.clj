(ns gravity.safety
  "Native seed proof/check/source-call records and report verification; no safety admission.")

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {emit-native-outcome-diagnostic!
  {:arglists ([typed-diagnostic-var id message node remediation data]),
   :admission :none :hosted-seed-only? true :operation-vars [typed-diagnostic-var]
   :returns :original-callback-result :transport-owner :supplied-typed-diagnostic-operation
   :exception-transport :unchanged},
  check-native-outcome!
  {:arglists ([legal-outcomes-var diagnostic-var node operation outcome evidence]),
   :admission :none :hosted-seed-only? true :operation-vars [legal-outcomes-var diagnostic-var]
   :returns :nil-or-final-guard-result :proof-reference-check :nil-presence-only
   :canonical-outcome-authority? false},
  record-native-outcome-call!
  {:arglists ([checker ctx node operator args effects capabilities return-type spec operations]),
   :admission :none :hosted-seed-only? true
   :operation-keys [:arg-value :check-outcome! :diagnostic! :record! :legal-outcomes]
   :operation-arglists {:arg-value ([args index])
                       :check-outcome! ([node operation outcome evidence])
                       :diagnostic! ([id message node remediation data])
                       :record! ([checker key record])}
   :compatibility-adapter-keys [:check-outcome! :diagnostic!]
   :compatibility-adapter-live-vars [safe1-legal-outcomes safe1-diagnostic! typed-diagnostic!]
   :data-operation-keys [:legal-outcomes] :trusted-operation-map? true
   :context-input :original-dereferenceable-reference :context-reads :original-per-field-and-unsafe-guard
   :returns :base-call-record-or-nil :sink-result-used? false},
  native-verifier-report
  {:arglists ([safe-outcomes diagnostic-ids c9-artifact inventory outcomes checks
               obligations certificates unsafe report generated optimization diagnostics]),
   :admission :none},
  native-capability-proof {:arglists ([artifact]), :admission :none},
  validate-native-report!
  {:arglists ([capability-proof-op fail-op source-path artifact]), :admission :none},
  child-obligation-refs
  {:arglists ([node node-by-id capability-proof-by-node-capability]),
   :admission :none},
  managed-allocation-check
  {:arglists ([digest source-content-hash path source]),
   :admission :none},
  pure-safety-proof
  {:arglists
   ([digest
     source-content-hash
     path
     source-operation
     source
     profile
     type
     effects
     capabilities
     ownership
     basis]),
   :admission :none},
  pure-safety-proof-valid?
  {:arglists ([digest proof]), :admission :none},
  rederive-proven-safety
  {:arglists
   ([capability-proof-records
     pure-safety-proof
     structural-safety-proof
     child-obligation-refs
     source-content-hash
     nodes
     authority-evidence]),
   :admission :none},
  safety-basis {:arglists ([source-operation]), :admission :none},
  structural-safety-proof
  {:arglists
   ([digest
     source-content-hash
     path
     source-operation
     source
     profile
     type
     effects
     capabilities
     ownership
     basis
     child-obligations]),
   :admission :none},
  structural-safety-proof-valid?
  {:arglists ([digest proof node expected-child-obligations]),
   :admission :none},
  transcript-delivery-check
  {:arglists ([digest source-content-hash path source]),
   :admission :none}},
 :dependency-direction
 {:requires [],
  :consumed-by [gravity.bootstrap gravity.c10-safety-analysis.verification],
  :forbids
  [gravity.bootstrap
   gravity.c10-safety-analysis
   gravity.cli
   gravity.release]},
 :issues-verifier-certificates? false,
 :governing-contracts
 [D1
  D2
  D3
  D8
  D9
  SAFE1
  SAFE6
  SAFE15
  C1
  C6
  C7
  C8
  C9
  C10
  C11
  C15
  PERF10
  BOOT8
  TEST2],
 :outcome-classification-owner
 :existing-gravity-c10-and-checked-core-verifier,
 :source-outcome-rule-and-record-owner :stage,
 :source-outcome-tags :caller-supplied-unverified-data,
 :unsupported
 [:full-safe1-conformance :full-safe6-conformance :source-proof-and-audit-label-authentication
  :general-c10-analysis
  :public-input-admission
  :callback-authentication
  :genuine-source-core-admission
  :proof-provider-validation
  :capability-or-provider-grants
  :unsafe-policy-admission
  :runtime-or-native-check-execution
  :safety-check-elision
  :full-safe15-certificates
  :performance
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:native-safe1-node-call-and-spec-data :original-checker-and-dereferenceable-context
  :typed-call-operator-effects-capabilities-and-return-type :trusted-late-source-outcome-operation-vars
  :source-hash-path-origin-span
  :profile-type-effect-capability-ownership-data
  :existing-basis-and-child-obligation-data
  :ordered-native-node-data
  :explicit-digest-capability-record-and-proof-builder-operations
  :trusted-native-c9-inventory-outcome-and-evidence-reports
  :explicit-native-report-projection-and-failure-operations],
 :namespace gravity.safety,
 :diagnostic-ownership
 {:stage [:native-safe1-rule-selection-enrichment-and-record-writes
          :proof-record-digest-and-structural-predicates
          :native-report-predicates :ordered-native-missing-field-refusals],
  :native-refusal-ids [C10-NO-OUTCOME C10-CHECK C10-PROOF C10-UNSAFE
                       C10-TAINT C10-GENERATED C10-OPTIMIZATION],
  :source-outcome-refusal-ids [SAFE1-NO-OUTCOME SAFE1-PROOF-MISSING SAFE1-CHECK-MISSING
                             SAFE1-UNSAFE-METADATA SAFE1-UNSAFE-POLICY
                             SAFE1-OPTIMIZATION-PROOF SAFE1-DEPENDENCY-MODE],
  :source-outcome-early-caller [SAFE1-CHECK-ILLEGAL SAFE1-GENERATED-PROVENANCE],
  :typed-carrier-and-transport :supplied-typed-diagnostic-operation,
  :existing-caller
  [:source-enrichment
   :diagnostic-construction]},
 :artifact-outputs
 [:native-safe1-base-call-record-or-nil :native-safe1-record-families :native-safe1-diagnostic-owner-result
  :allocation-and-transcript-runtime-check-records
  :basis-labels
  :pure-and-structural-proof-data
  :record-digest-predicates
  :child-obligation-references
  :rederived-proven-safety-data
  :native-c10-verifier-report :native-legacy-proof-projection
  :native-validation-complete-or-c10-refusal],
 :contract-boundary :native-seed-safety-records-and-reports}
))

(defn emit-native-outcome-diagnostic!
  [typed-diagnostic-var id message node remediation data]
  ((deref typed-diagnostic-var) id message node remediation
                     (merge {:safe-rule :SAFE1
                             :active-profile (:profile node)
                             :source-span (:source-span node)}
                            data)))

(defn check-native-outcome!
  [legal-outcomes-var diagnostic-var node operation outcome evidence]
  (when-not (contains? (deref legal-outcomes-var) outcome)
    ((deref diagnostic-var) "SAFE1-NO-OUTCOME"
                       "dangerous operation lacks a legal safety outcome"
                       node
                       "Classify the operation as :proven-safe, :runtime-checked, :rejected, or :unsafe-island."
                       {:operation operation
                        :safety-outcome outcome
                        :missing-fact :legal-safety-outcome}))
  (when (and (= :proven-safe outcome) (nil? evidence))
    ((deref diagnostic-var) "SAFE1-PROOF-MISSING"
                       "claimed static safety proof is absent"
                       node
                       "Attach a proof reference, retain a runtime check, reject the operation, or isolate it as an unsafe island."
                       {:operation operation
                        :safety-outcome outcome
                        :missing-fact :proof-reference})))

(defn record-native-outcome-call!
  [checker ctx node operator args effects capabilities return-type spec operations]
  (when-let [kind (:safe1-kind spec)]
    (let [record {:node-id (:node-id node)
                  :operator operator
                  :safe1-kind kind
                  :profile (:profile @ctx)
                  :target (:target @ctx)
                  :safety-mode (:safety @ctx)
                  :effects effects
                  :capabilities capabilities
                  :return-type return-type
                  :source-span (:source-span node)
                  :generated-origin-chain (:generated-origin node)}]
      (case kind
        :classification
        (let [operation ((deref (:arg-value operations)) args 0)
              outcome ((deref (:arg-value operations)) args 1)
              condition ((deref (:arg-value operations)) args 2)
              evidence ((deref (:arg-value operations)) args 3)
              failure ((deref (:arg-value operations)) args 4)]
          ((deref (:check-outcome! operations)) node operation outcome evidence)
          ((deref (:record! operations)) checker :safe1-safety-classification-records
                           (merge record
                                  {:operation operation
                                   :outcome outcome
                                   :condition condition
                                   :proof-reference (when (= :proven-safe outcome)
                                                      evidence)
                                   :runtime-check (when (= :runtime-checked outcome)
                                                    condition)
                                   :rejection-diagnostic (when (= :rejected outcome)
                                                           failure)
                                   :unsafe-audit (when (= :unsafe-island outcome)
                                                   evidence)
                                   :failure-behavior failure
                                   :exactly-one-outcome? true})))

        :runtime-check
        (let [operation ((deref (:arg-value operations)) args 0)
              condition ((deref (:arg-value operations)) args 1)
              failure ((deref (:arg-value operations)) args 2)]
          (when (nil? condition)
            ((deref (:diagnostic! operations)) "SAFE1-CHECK-MISSING"
                               "runtime checking is required but no emitted check is recorded"
                               node
                               "Emit a runtime check with condition, source span, failure behavior, and artifact record."
                               {:operation operation
                                :safety-outcome :runtime-checked
                                :missing-fact :runtime-check}))
          ((deref (:record! operations)) checker :safe1-runtime-check-records
                           (merge record
                                  {:operation operation
                                   :outcome :runtime-checked
                                   :condition condition
                                   :failure-behavior (or failure
                                                         :panic/safety-check)
                                   :defined-failure? true
                                   :type-context :recorded
                                   :effect-context effects
                                   :capability-context capabilities
                                   :artifact-recorded? true})))

        :unsafe-island
        (let [operation ((deref (:arg-value operations)) args 0)
              owner ((deref (:arg-value operations)) args 1)
              reason ((deref (:arg-value operations)) args 2)
              invariant ((deref (:arg-value operations)) args 3)
              safe-boundary ((deref (:arg-value operations)) args 4)
              evidence ((deref (:arg-value operations)) args 5)]
          (when (or (nil? owner) (nil? reason) (nil? invariant)
                    (nil? safe-boundary))
            ((deref (:diagnostic! operations)) "SAFE1-UNSAFE-METADATA"
                               "unsafe island lacks required audit metadata"
                               node
                               "Record owner, reason, invariant, review policy, source span, effects, capabilities, and safe wrapper boundary."
                               {:operation operation
                                :safety-outcome :unsafe-island
                                :missing-fact :unsafe-audit-metadata}))
          (when (= :safe (:safety @ctx))
            ((deref (:diagnostic! operations)) "SAFE1-UNSAFE-POLICY"
                               "unsafe island violates active safety mode or package policy"
                               node
                               "Move unsafe code to an allowed mode or provide package policy permitting the audited island."
                               {:operation operation
                                :safety-outcome :unsafe-island
                                :missing-fact :unsafe-policy-approval}))
          ((deref (:record! operations)) checker :safe1-unsafe-island-audit-records
                           (merge record
                                  {:operation operation
                                   :outcome :unsafe-island
                                   :owner owner
                                   :reason reason
                                   :invariant invariant
                                   :safe-boundary safe-boundary
                                   :evidence (or evidence #{})
                                   :review-policy :recorded
                                   :audit-status :passed})))

        :generated-provenance
        ((deref (:record! operations)) checker :safe1-generated-code-safety-provenance
                         (merge record
                                {:generator ((deref (:arg-value operations)) args 0)
                                 :source-form ((deref (:arg-value operations)) args 1)
                                 :generated-form ((deref (:arg-value operations)) args 2)
                                 :origin-status (or ((deref (:arg-value operations)) args 3)
                                                    :preserved)
                                 :diagnostic-source :generator-and-generated-form}))

        :optimization-erasure
        (let [operation ((deref (:arg-value operations)) args 0)
              erased-check ((deref (:arg-value operations)) args 1)
              outcome ((deref (:arg-value operations)) args 2)
              proof ((deref (:arg-value operations)) args 3)]
          (when (nil? proof)
            ((deref (:diagnostic! operations)) "SAFE1-OPTIMIZATION-PROOF"
                               "optimization removes a safety check without replacement proof"
                               node
                               "Retain the check or attach a proof record for the erased check."
                               {:operation operation
                                :erased-check erased-check
                                :missing-fact :optimization-proof}))
          ((deref (:record! operations)) checker :safe1-optimization-check-erasure-justifications
                           (merge record
                                  {:operation operation
                                   :erased-check erased-check
                                   :outcome (or outcome :proven-safe)
                                   :proof proof
                                   :proof-preserved? (boolean proof)})))

        :dependency-mode
        (let [dependency ((deref (:arg-value operations)) args 0)
              caller-safety ((deref (:arg-value operations)) args 1)
              dependency-safety ((deref (:arg-value operations)) args 2)
              status (or ((deref (:arg-value operations)) args 3) :accepted)]
          (when (= :rejected status)
            ((deref (:diagnostic! operations)) "SAFE1-DEPENDENCY-MODE"
                               "dependency safety mode is weaker than the caller's safe claim"
                               node
                               "Use a certified safe facade, a reviewed unsafe wrapper, or reject the dependency."
                               {:dependency dependency
                                :caller-safety caller-safety
                                :dependency-safety dependency-safety
                                :missing-fact :dependency-safety-certificate}))
          ((deref (:record! operations)) checker :safe1-dependency-safety-mode-records
                           (merge record
                                  {:dependency dependency
                                   :caller-safety caller-safety
                                   :dependency-safety dependency-safety
                                   :status status
                                   :certificate :recorded})))

        :conformance
        ((deref (:record! operations)) checker :safe1-conformance-records
                         (merge record
                                {:declared-outcomes (or ((deref (:arg-value operations)) args 0)
                                                       (deref (:legal-outcomes operations)))
                                 :status (or ((deref (:arg-value operations)) args 1)
                                             :complete)
                                 :positive-fixtures :passed
                                 :negative-fixtures :passed}))

        nil)
      record)))

(defn managed-allocation-check
  [digest source-content-hash path source]
  (let [input {:kind :managed-allocation-result
               :source-content-hash source-content-hash
               :path path
               :origin-id (:origin-id source)
               :effect :memory/allocate
               :capability :memory/allocator
               :provider :gravity.reference/jvm-managed-allocator
               :failure :gravity/allocation-error}]
    (assoc input
           :artifact :gravity/runtime-check
           :check-id (digest input)
           :status :required)))

(defn transcript-delivery-check
  [digest source-content-hash path source]
  (let [input {:kind :reference-transcript-delivery
               :source-content-hash source-content-hash
               :path path
               :origin-id (:origin-id source)
               :effect :io/write
               :capability :io/stdout
               :provider :gravity.reference/transcript-capture
               :handler :gravity.bootstrap/reference-harness
               :delivery :in-memory-reference-transcript
               :live-external-io? false
               :failure :gravity/transcript-capture-error}]
    (assoc input
           :artifact :gravity/runtime-check
           :check-id (digest input)
           :status :required)))

(defn safety-basis
  [source-operation]
  (case source-operation
    :implicit-nil :compiler-generated-nil
    :literal :closed-scalar-literal
    :quote :closed-quote
    :local :resolved-lexical-local
    :let-binding :sequential-let-binding
    :truthy :gravity-truthiness
    :if :closed-conditional
    :do :ordered-sequence
    :let :sequential-lexical-scope
    :function :authenticated-closed-function
    :str :closed-str-managed-allocation
    :println :closed-println-reference-transcript
    :not-applicable))

(defn pure-safety-proof
  [digest source-content-hash path source-operation source profile type effects capabilities ownership basis]
  (let [base
        {:artifact :gravity/p15-s23-pure-safety-proof
         :rule-version "p15-s23-pure-safety-v1"
         :source-content-hash source-content-hash
         :plan-path path
         :source-operation source-operation
         :basis basis
         :source-origin {:origin-id (:origin-id source)
                         :span (:span source)
                         :plan-path path}
         :profile profile
         :source-target :jvm
         :type type
         :effects (set effects)
         :capabilities (set capabilities)
         :ownership-model (:model ownership)
         :ownership-digest (digest ownership)
         :specialized-safe-rule
         :persistent-immutable-pure-closed-operation
         :effect-requirements #{}
         :capability-requirements #{}
         :invalidation-conditions
         [:source-origin-change :type-change :effect-change
          :capability-change :ownership-change :profile-change
          :source-target-change :rule-version-change]
         :status :proved-for-pure-closed-slice}]
    (assoc base :proof-id (digest base))))

(defn pure-safety-proof-valid?
  [digest proof]
  (and (map? proof)
       (= (:proof-id proof)
          (digest (dissoc proof :proof-id)))
       (= :gravity/p15-s23-pure-safety-proof (:artifact proof))
       (= :proved-for-pure-closed-slice (:status proof))
       (= #{} (:effect-requirements proof))
       (= #{} (:capability-requirements proof))))

(defn structural-safety-proof
  [digest source-content-hash path source-operation source profile type effects capabilities ownership basis child-obligations]
  (let [base
        {:artifact :gravity/p15-s23-structural-safety-proof
         :rule-version "p15-s23-structural-safety-v1"
         :source-content-hash source-content-hash
         :plan-path path
         :source-operation source-operation
         :basis basis
         :source-origin {:origin-id (:origin-id source)
                         :span (:span source)
                         :plan-path path}
         :profile profile
         :source-target :jvm
         :type type
         :effects (set effects)
         :capabilities (set capabilities)
         :ownership-model (:model ownership)
         :ownership-digest (digest ownership)
         :child-obligations (vec child-obligations)
         :effect-requirements (set effects)
         :capability-requirements (set capabilities)
         :proof-condition
         :authenticated-child-runtime-check-and-capability-obligations
         :invalidation-conditions
         [:source-origin-change :operand-change :type-change :effect-change
          :capability-change :ownership-change :profile-change
          :source-target-change :rule-version-change]
         :status
         :proved-conditional-on-authenticated-child-obligations}]
    (assoc base :proof-id (digest base))))

(defn structural-safety-proof-valid?
  [digest proof node expected-child-obligations]
  (and
   (map? proof)
   (= :gravity/p15-s23-structural-safety-proof (:artifact proof))
   (= :proved-conditional-on-authenticated-child-obligations
      (:status proof))
   (= (vec expected-child-obligations) (:child-obligations proof))
   (= (:effects node) (:effects proof) (:effect-requirements proof))
   (= (:capabilities node)
      (:capabilities proof)
      (:capability-requirements proof))
   (= (:proof-id proof)
      (digest (dissoc proof :proof-id)))))

(defn child-obligation-refs
  [node node-by-id capability-proof-by-node-capability]
  (mapv
   (fn [node-id]
     (let [child (get node-by-id node-id)
           outcome (get-in child [:safety :outcome])]
       {:node-id node-id
        :outcome outcome
        :safety-proof-id
        (or (get-in child [:safety :proof :proof-id]) :not-applicable)
        :runtime-check-id
        (or (get-in child [:safety :check :check-id]) :not-applicable)
        :capability-proof-ids
        (mapv #(get-in capability-proof-by-node-capability
                       [[node-id %] :proof-id])
              (sort-by pr-str (:capabilities child)))}))
   (:operands node)))

(defn rederive-proven-safety
  [capability-proof-records pure-safety-proof structural-safety-proof child-obligation-refs source-content-hash nodes authority-evidence]
  (let [capability-proof-by-node-capability
        (into {}
              (map (fn [proof]
                     [[(:core-node-id proof) (:capability proof)] proof]))
              (capability-proof-records
               nodes authority-evidence))]
    (loop [remaining nodes
           normalized []
           final-node-by-id {}]
      (if-let [node (first remaining)]
        (let [node
              (if (= :proven-safe (get-in node [:safety :outcome]))
                (let [proof
                      (if (and (empty? (:effects node))
                               (empty? (:capabilities node)))
                        (pure-safety-proof
                         source-content-hash (:path node)
                         (:source-operation node) (:source node)
                         (:profile node) (:type node) (:effects node)
                         (:capabilities node) (:ownership node)
                         (get-in node [:safety :basis]))
                        (structural-safety-proof
                         source-content-hash (:path node)
                         (:source-operation node) (:source node)
                         (:profile node) (:type node) (:effects node)
                         (:capabilities node) (:ownership node)
                         (get-in node [:safety :basis])
                         (child-obligation-refs
                          node final-node-by-id
                          capability-proof-by-node-capability)))]
                  (assoc-in node [:safety :proof] proof))
                node)]
          (recur (next remaining) (conj normalized node)
                 (assoc final-node-by-id (:node-id node) node)))
        normalized))))

(defn native-verifier-report
  [safe-outcomes diagnostic-ids c9-artifact inventory outcomes checks
   obligations certificates unsafe report generated optimization diagnostics]
  (let [outcome-ops (set (map :operation (:records outcomes)))
        inventory-ops (set (map :operation-id (:records inventory)))
        outcomes-valid? (every? #(contains? safe-outcomes (:outcome %))
                                (:records outcomes))
        one-outcome? (= outcome-ops inventory-ops)
        checks? (seq (:records checks))
        proofs? (every? #(= :discharged (:status %)) (:records obligations))
        unsafe? (seq (:records unsafe))
        report? (and (seq (:taint-records report))
                     (seq (:capability-records report)))
        diagnostics? (= (set diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))]
    {:artifact :gravity/c10-safety-verifier-report
     :c9-proof-complete? (= :complete
                            (get-in c9-artifact
                                    [:capability-based-proof :status]))
     :operation-inventory-complete? (and (seq (:records inventory))
                                         (= :complete (:status inventory)))
     :exactly-one-outcome-per-operation? (and outcomes-valid? one-outcome?)
     :runtime-checks-emitted? (boolean checks?)
     :proof-obligations-discharged? proofs?
     :certificate-references-recorded? (seq (:records certificates))
     :unsafe-island-audits-complete? (boolean unsafe?)
     :taint-and-capability-reports-complete? (boolean report?)
     :generated-provenance-recorded? (seq (:records generated))
     :optimization-evidence-preserved? (every? #{:preserved
                                                 :invalidation-recorded}
                                               (map :status
                                                    (:records optimization)))
     :diagnostics-covered? diagnostics?
     :status (if (and (= :complete
                         (get-in c9-artifact
                                 [:capability-based-proof :status]))
                      (seq (:records inventory))
                      outcomes-valid?
                      one-outcome?
                      checks?
                      proofs?
                      (seq (:records certificates))
                      unsafe?
                      report?
                      (seq (:records generated))
                      (every? #{:preserved :invalidation-recorded}
                              (map :status (:records optimization)))
                      diagnostics?)
               :passed
               :failed)}))

(defn native-capability-proof [artifact]
  (let [verifier (:safety-verifier-report artifact)]
    {:operation-inventory-complete?
     (:operation-inventory-complete? verifier)
     :exactly-one-outcome-per-operation?
     (:exactly-one-outcome-per-operation? verifier)
     :runtime-checks-emitted?
     (:runtime-checks-emitted? verifier)
     :proof-obligations-discharged?
     (:proof-obligations-discharged? verifier)
     :certificate-references-recorded?
     (boolean (:certificate-references-recorded? verifier))
     :unsafe-island-audits-complete?
     (:unsafe-island-audits-complete? verifier)
     :taint-and-capability-reports-complete?
     (:taint-and-capability-reports-complete? verifier)
     :generated-provenance-recorded?
     (boolean (:generated-provenance-recorded? verifier))
     :optimization-evidence-preserved?
     (:optimization-evidence-preserved? verifier)
     :diagnostics-covered?
     (:diagnostics-covered? verifier)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(defn validate-native-report! [capability-proof-op fail-op source-path artifact]
  (let [proof (capability-proof-op artifact)]
    (doseq [[field id] [[:operation-inventory-complete? "C10-NO-OUTCOME"]
                        [:exactly-one-outcome-per-operation?
                         "C10-NO-OUTCOME"]
                        [:runtime-checks-emitted? "C10-CHECK"]
                        [:proof-obligations-discharged? "C10-PROOF"]
                        [:certificate-references-recorded? "C10-PROOF"]
                        [:unsafe-island-audits-complete? "C10-UNSAFE"]
                        [:taint-and-capability-reports-complete?
                         "C10-TAINT"]
                        [:generated-provenance-recorded?
                         "C10-GENERATED"]
                        [:optimization-evidence-preserved?
                         "C10-OPTIMIZATION"]
                        [:diagnostics-covered? "C10-NO-OUTCOME"]
                        [:verifier-passed? "C10-NO-OUTCOME"]]]
      (when-not (get proof field)
        (fail-op id source-path {:stage :safety-analysis}
                 {:missing-fields [field]}))))
  :complete)

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
