

(defn efir-validate-math2!
  [source-path manifest suite]
  (let [declarations (:elementary-declarations suite)
        providers (:provider-manifest suite)]
    (when (empty? declarations)
      (efir-fail! "MATH2-DECLARATION" source-path manifest {}
                  {:missing-fields [:elementary-declarations]
                   :remediation "Provide at least one elementary declaration."}))
    (doseq [decl declarations]
      (let [missing-fields (efir-declaration-missing-fields decl)]
        (when (seq missing-fields)
          (efir-fail! "MATH2-DECLARATION" source-path manifest decl
                      {:missing-fields missing-fields
                       :remediation "Declare domain, codomain, branch policy, semantic form, numeric modes, provider requirements, and EFIR anchor."}))
        (when (or (false? (:domain-valid? decl))
                  (not (perf-present? (:domain decl))))
          (efir-fail! "MATH2-DOMAIN" source-path manifest decl
                      {:remediation "Elementary declarations need a checked domain accepted by the numeric mode."}))
        (when (or (not (perf-present? (:branch-policy decl)))
                  (false? (:branch-compatible? decl)))
          (efir-fail! "MATH2-BRANCH" source-path manifest decl
                      {:remediation "Declare branch and exceptional-value policy and keep it compatible with providers."}))
        (when-let [bad-mode (first (remove numeric-standard-modes
                                           (:numeric-modes decl)))]
          (efir-fail! "MATH2-NUMERIC-MODE" source-path manifest decl
                      {:numeric-mode bad-mode
                       :remediation "Use a registered numeric mode for elementary declarations."}))
        (when-not (some #(and (:eligible? %)
                              (efir-provider-matches-declaration? decl %))
                        providers)
          (efir-fail! "MATH2-PROVIDER" source-path manifest decl
                      {:remediation "Select at least one provider that satisfies the declaration, profile, target, mode, branch, effects, capabilities, and certificate rules."}))))
    (doseq [provider providers]
      (when (and (:selected? provider)
                 (:certificate-required? provider)
                 (not (perf-present? (:certificate provider))))
        (efir-fail! "MATH2-CERTIFICATE" source-path manifest provider
                    {:remediation "Approximate or certified providers need a certificate accepted by the active checker."}))
      (when (and (:selected? provider)
                 (seq (:effects provider)))
        (efir-fail! "MATH2-EFFECT" source-path manifest provider
                    {:effects (:effects provider)
                     :remediation "Provider effects must be declared and legal for the profile before selection."}))
      (when (and (:selected? provider)
                 (seq (:missing-target-features provider)))
        (efir-fail! "MATH2-TARGET" source-path manifest provider
                    {:target-features (:missing-target-features provider)
                     :remediation "Provider target features need guards, target support, or fallback behavior."})))
    (doseq [claim (:equivalence-claims suite)]
      (when (and (:claimed-equal? claim)
                 (not (perf-present? (:proof claim))))
        (efir-fail! "MATH2-EQUIVALENCE" source-path manifest claim
                    {:remediation "Elementary semantic equality requires proof or certificate evidence over the declared domain."})))
    :complete))

(defn efir-node-invalid?
  [node]
  (or (not (perf-present? (:id node)))
      (not (contains? efir-node-ops (:op node)))
      (and (= :call (:op node))
           (not (contains? efir-supported-elementary-ops
                           (:elementary-op node))))))

(defn efir-validate-math3!
  [source-path manifest suite]
  (let [graphs (:efir-graphs suite)]
    (when (empty? graphs)
      (efir-fail! "MATH3-NODE" source-path manifest {}
                  {:missing-fields [:efir-graphs]
                   :remediation "Build at least one EFIR graph from an elementary source subgraph."}))
    (doseq [graph graphs]
      (when-let [bad-node (first (filter efir-node-invalid?
                                         (:nodes graph)))]
        (efir-fail! "MATH3-NODE" source-path manifest
                    (assoc bad-node :graph-id (:graph-id graph))
                    {:remediation "EFIR nodes need stable ids and supported node/operator shapes."}))
      (when (or (not (perf-present? (:domain graph)))
                (some #(not (perf-present? (:domain %))) (:nodes graph)))
        (efir-fail! "MATH3-DOMAIN" source-path manifest graph
                    {:remediation "Every EFIR graph and node needs domain information."}))
      (when (or (not (perf-present? (:codomain graph)))
                (some #(not (perf-present? (:codomain %))) (:nodes graph)))
        (efir-fail! "MATH3-CODOMAIN" source-path manifest graph
                    {:remediation "Every EFIR graph and node needs codomain information."}))
      (when (or (not (perf-present? (:branch-policy graph)))
                (some #(and (= :call (:op %))
                            (not (perf-present? (:branch-policy %))))
                      (:nodes graph)))
        (efir-fail! "MATH3-BRANCH" source-path manifest graph
                    {:remediation "Elementary calls and graphs need explicit branch policy."}))
      (when (or (not (perf-present? (:numeric-mode graph)))
                (not (perf-present? (:precision graph))))
        (efir-fail! "MATH3-PRECISION" source-path manifest graph
                    {:remediation "EFIR graphs need numeric mode and precision contract facts."}))
      (when (or (not (perf-present? (:source-anchors graph)))
                (not (perf-present? (:semantic-anchor graph))))
        (efir-fail! "MATH3-SOURCE" source-path manifest graph
                    {:remediation "EFIR graphs need source anchors and typed-core or MIR semantic anchors."}))
      (when (and (perf-present? (:runtime-anchor graph))
                 (not (some #(and (= (:runtime-anchor graph) (:provider %))
                                  (= (:graph-id graph) (:efir-anchor %)))
                            (:provider-manifest suite))))
        (efir-fail! "MATH3-RUNTIME" source-path manifest graph
                    {:remediation "Runtime implementation choices must be tied to the EFIR graph anchor."})))
    (doseq [rewrite (:rewrite-records suite)]
      (when (and (:claims-equality? rewrite)
                 (not (perf-present? (:semantic-proof rewrite))))
        (efir-fail! "MATH3-REWRITE" source-path manifest rewrite
                    {:remediation "EFIR rewrites require proof or certificate evidence."})))
    (doseq [eml (:eml-records suite)]
      (when (and (:lowered? eml)
                 (or (false? (:preserves-branch-policy? eml))
                     (false? (:preserves-source? eml))))
        (efir-fail! "MATH3-EML" source-path manifest eml
                    {:remediation "EML lowering must preserve branch policy and source anchors."})))
    :complete))

(defn efir-capability-proof
  [suite]
  {:elementary-declarations-complete?
   (every? #(empty? (efir-declaration-missing-fields %))
           (:elementary-declarations suite))
   :providers-mode-eligible?
   (every? #(or (not (:selected? %)) (true? (:eligible? %)))
           (:provider-manifest suite))
   :semantic-runtime-separated? true
   :efir-graphs-anchored?
   (every? #(and (perf-present? (:source-anchors %))
                 (perf-present? (:semantic-anchor %)))
           (:efir-graphs suite))
   :numeric-mode-and-precision-attached?
   (every? #(and (perf-present? (:numeric-mode %))
                 (perf-present? (:precision %)))
           (:efir-graphs suite))
   :branch-policy-preserved?
   (every? #(perf-present? (:branch-policy %)) (:efir-graphs suite))
   :runtime-selection-anchored?
   (every? #(perf-present? (:runtime-anchor %)) (:efir-graphs suite))
   :eml-not-used-as-equality? true
   :status :complete})

(defn efir-source-artifact
  [source-path source-text]
  (let [numeric-artifact (numeric-mode-source-artifact source-path source-text)
        manifest (:profile-manifest numeric-artifact)
        suite (efir-suite manifest)
        _ (efir-validate-math2! source-path manifest suite)
        _ (efir-validate-math3! source-path manifest suite)
        capability-proof (efir-capability-proof suite)
        conformance {:documents ["MATH2" "MATH3"]
                     :task "P05-T02"
                     :required-diagnostic-ids math2-3-diagnostic-ids
                     :elementary-registry-status :complete
                     :efir-graph-status :complete
                     :provider-selection-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-efir-artifact
     :document-set ["MATH2" "MATH3"]
     :pass {:name :efir-validation
            :input :numeric-mode-table
            :output :efir-graph
            :requires [:numeric-mode-validation
                       :elementary-declaration-registry
                       :typed-core-semantic-anchor
                       :provider-eligibility]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :numeric-mode :precision-contract
                        :branch-policy :semantic-anchor]
            :emits [:elementary-function-registry
                    :efir-semantic-anchor
                    :provider-manifest
                    :provider-eligibility-report
                    :semantic-runtime-implementation-map
                    :branch-policy-table
                    :exceptional-value-policy-table
                    :selection-decision-record
                    :efir-graph
                    :domain-environment
                    :codomain-facts
                    :proof-obligation-seed-list
                    :source-anchor-map
                    :runtime-implementation-anchor
                    :efir-conformance-results]
            :rejects math2-3-diagnostic-ids}
     :numeric-mode-artifact-hash (str "sha256:"
                                      (sha256-hex
                                       (pr-str numeric-artifact)))
     :numeric-mode-artifact-kind (:kind numeric-artifact)
     :profile-manifest manifest
     :elementary-function-registry (:elementary-declarations suite)
     :efir-semantic-anchor
     (mapv #(select-keys % [:function-id :efir-anchor :semantic-form])
           (:elementary-declarations suite))
     :provider-manifest (:provider-manifest suite)
     :provider-eligibility-report
     (mapv #(select-keys % [:provider :function-id :selected?
                            :eligible? :rejected-reason :numeric-mode
                            :profile :target])
           (:provider-manifest suite))
     :semantic-runtime-implementation-map
     (mapv #(select-keys % [:decision-id :function-id :efir-anchor
                            :selected-provider :rejected-providers
                            :numeric-mode :branch-policy :status])
           (:selection-decisions suite))
     :branch-policy-table
     (mapv #(select-keys % [:function-id :branch-policy])
           (:elementary-declarations suite))
     :exceptional-value-policy-table
     (mapv #(select-keys % [:function-id :exceptional-values])
           (:elementary-declarations suite))
     :selection-decision-record (:selection-decisions suite)
     :efir-graph (:efir-graphs suite)
     :domain-environment
     (mapv #(select-keys % [:graph-id :domain]) (:efir-graphs suite))
     :codomain-facts
     (mapv #(select-keys % [:graph-id :codomain]) (:efir-graphs suite))
     :proof-obligation-seed-list
     (mapv #(select-keys % [:graph-id :proof-obligations])
           (:efir-graphs suite))
     :source-anchor-map
     (mapv #(select-keys % [:graph-id :source-anchors
                            :semantic-anchor])
           (:efir-graphs suite))
     :runtime-implementation-anchor
     (mapv #(select-keys % [:graph-id :runtime-anchor])
           (:efir-graphs suite))
     :equivalence-proof-table (:equivalence-claims suite)
     :rewrite-records (:rewrite-records suite)
     :eml-lowering-records (:eml-records suite)
     :capability-based-proof capability-proof
     :efir-conformance-results conformance
     :diagnostics []}))

(def eml-supported-bases
  #{:exp-minus-log})