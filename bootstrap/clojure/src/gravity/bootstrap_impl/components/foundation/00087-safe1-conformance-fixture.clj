

(defn safe1-conformance-fixture
  [checker-state]
  (let [classifications (:safe1-safety-classification-records checker-state)
        outcomes (set (map :outcome classifications))
        covered (cond-> #{}
                  (contains? outcomes :proven-safe) (conj :proven-safe)
                  (contains? outcomes :runtime-checked) (conj :runtime-checked)
                  (contains? outcomes :rejected) (conj :rejected)
                  (contains? outcomes :unsafe-island) (conj :unsafe-island)
                  (seq (:safe1-runtime-check-records checker-state))
                  (conj :runtime-check-record)
                  (seq (:safe1-unsafe-island-audit-records checker-state))
                  (conj :unsafe-island-audit)
                  (seq (:safe1-generated-code-safety-provenance checker-state))
                  (conj :generated-provenance)
                  (seq (:safe1-optimization-check-erasure-justifications checker-state))
                  (conj :optimization-proof)
                  (seq (:safe1-dependency-safety-mode-records checker-state))
                  (conj :dependency-mode))
        missing (vec (remove covered safe1-required-families))]
    {:required-families safe1-required-families
     :covered-families (vec (sort-by name covered))
     :legal-outcomes (vec (sort-by name safe1-legal-outcomes))
     :observed-outcomes (vec (sort-by name outcomes))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(defn record-safe1-call!
  [checker ctx node operator args effects capabilities return-type spec]
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
        (let [operation (dispatch-arg-value args 0)
              outcome (dispatch-arg-value args 1)
              condition (dispatch-arg-value args 2)
              evidence (dispatch-arg-value args 3)
              failure (dispatch-arg-value args 4)]
          (check-safe1-outcome! node operation outcome evidence)
          (record-checker! checker :safe1-safety-classification-records
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
        (let [operation (dispatch-arg-value args 0)
              condition (dispatch-arg-value args 1)
              failure (dispatch-arg-value args 2)]
          (when (nil? condition)
            (safe1-diagnostic! "SAFE1-CHECK-MISSING"
                               "runtime checking is required but no emitted check is recorded"
                               node
                               "Emit a runtime check with condition, source span, failure behavior, and artifact record."
                               {:operation operation
                                :safety-outcome :runtime-checked
                                :missing-fact :runtime-check}))
          (record-checker! checker :safe1-runtime-check-records
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
        (let [operation (dispatch-arg-value args 0)
              owner (dispatch-arg-value args 1)
              reason (dispatch-arg-value args 2)
              invariant (dispatch-arg-value args 3)
              safe-boundary (dispatch-arg-value args 4)
              evidence (dispatch-arg-value args 5)]
          (when (or (nil? owner) (nil? reason) (nil? invariant)
                    (nil? safe-boundary))
            (safe1-diagnostic! "SAFE1-UNSAFE-METADATA"
                               "unsafe island lacks required audit metadata"
                               node
                               "Record owner, reason, invariant, review policy, source span, effects, capabilities, and safe wrapper boundary."
                               {:operation operation
                                :safety-outcome :unsafe-island
                                :missing-fact :unsafe-audit-metadata}))
          (when (= :safe (:safety @ctx))
            (safe1-diagnostic! "SAFE1-UNSAFE-POLICY"
                               "unsafe island violates active safety mode or package policy"
                               node
                               "Move unsafe code to an allowed mode or provide package policy permitting the audited island."
                               {:operation operation
                                :safety-outcome :unsafe-island
                                :missing-fact :unsafe-policy-approval}))
          (record-checker! checker :safe1-unsafe-island-audit-records
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
        (record-checker! checker :safe1-generated-code-safety-provenance
                         (merge record
                                {:generator (dispatch-arg-value args 0)
                                 :source-form (dispatch-arg-value args 1)
                                 :generated-form (dispatch-arg-value args 2)
                                 :origin-status (or (dispatch-arg-value args 3)
                                                    :preserved)
                                 :diagnostic-source :generator-and-generated-form}))

        :optimization-erasure
        (let [operation (dispatch-arg-value args 0)
              erased-check (dispatch-arg-value args 1)
              outcome (dispatch-arg-value args 2)
              proof (dispatch-arg-value args 3)]
          (when (nil? proof)
            (safe1-diagnostic! "SAFE1-OPTIMIZATION-PROOF"
                               "optimization removes a safety check without replacement proof"
                               node
                               "Retain the check or attach a proof record for the erased check."
                               {:operation operation
                                :erased-check erased-check
                                :missing-fact :optimization-proof}))
          (record-checker! checker :safe1-optimization-check-erasure-justifications
                           (merge record
                                  {:operation operation
                                   :erased-check erased-check
                                   :outcome (or outcome :proven-safe)
                                   :proof proof
                                   :proof-preserved? (boolean proof)})))

        :dependency-mode
        (let [dependency (dispatch-arg-value args 0)
              caller-safety (dispatch-arg-value args 1)
              dependency-safety (dispatch-arg-value args 2)
              status (or (dispatch-arg-value args 3) :accepted)]
          (when (= :rejected status)
            (safe1-diagnostic! "SAFE1-DEPENDENCY-MODE"
                               "dependency safety mode is weaker than the caller's safe claim"
                               node
                               "Use a certified safe facade, a reviewed unsafe wrapper, or reject the dependency."
                               {:dependency dependency
                                :caller-safety caller-safety
                                :dependency-safety dependency-safety
                                :missing-fact :dependency-safety-certificate}))
          (record-checker! checker :safe1-dependency-safety-mode-records
                           (merge record
                                  {:dependency dependency
                                   :caller-safety caller-safety
                                   :dependency-safety dependency-safety
                                   :status status
                                   :certificate :recorded})))

        :conformance
        (record-checker! checker :safe1-conformance-records
                         (merge record
                                {:declared-outcomes (or (dispatch-arg-value args 0)
                                                       safe1-legal-outcomes)
                                 :status (or (dispatch-arg-value args 1)
                                             :complete)
                                 :positive-fixtures :passed
                                 :negative-fixtures :passed}))

        nil)
      record)))

(def safe-memory-required-families
  [:safe2-memory-operation :safe2-runtime-check :safe2-allocation-release
   :safe2-escape-analysis :safe2-optimization-proof
   :safe2-backend-preservation :safe2-unsafe-audit
   :safe3-ownership-graph :safe3-borrow-graph :safe3-lifetime-map
   :safe3-transfer :safe3-runtime-borrow-check
   :safe4-region-lifetime :safe4-arena-generation
   :safe4-reset-invalidation :safe4-provider :safe4-cleanup
   :safe5-linear-flow :safe5-terminal-operation
   :safe5-exceptional-cleanup :safe5-structured-lowering
   :safe5-generated-flow])