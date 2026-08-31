

(defn math-proof-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "MATH6-CLAIM" "interval proof claim is malformed or unsupported"
           "MATH6-DOMAIN" "interval proof domain is invalid"
           "MATH6-ROUNDING" "outward rounding assumptions are unproved"
           "MATH6-BRANCH" "interval proof branch policy is incompatible"
           "MATH6-PARTITION" "interval partition tree is unreplayable or incomplete"
           "MATH6-BOUND" "interval proof bounds are missing or insufficient"
           "MATH6-UNRESOLVED" "unresolved cells prevent certificate acceptance"
           "MATH6-PROVIDER" "real-proof provider output is outside trust policy"
           "MATH6-INVALIDATED" "proof artifact has been invalidated"
           "MATH9-RULE-SHAPE" "rewrite rule is malformed"
           "MATH9-DOMAIN" "rewrite rule domain is missing or incompatible"
           "MATH9-BRANCH" "rewrite rule branch policy is incompatible"
           "MATH9-SIDE-CONDITION" "rewrite side condition is undischarged"
           "MATH9-PROOF" "unproved rewrite was used as accepted"
           "MATH9-TRACE" "rewrite trace cannot replay"
           "MATH9-TERMINATION" "rewrite strategy is unbounded"
           "MATH9-COUNTEREXAMPLE" "accepted rewrite is disproved by counterexample"
           "MATH9-EGRAPH" "e-graph artifact has invalid analysis, guard, fuel, cost, or explanation"
           "MATH9-EQUALITY" "tree identity or unproved extraction was used as equality"
           "math proof record is invalid")
         (merge {:source-span (or (:source-span record)
                                  {:source source-path})
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :claim-id (:claim-id record)
                 :rule-id (:rule-id record)
                 :rule-version (:version record)
                 :expression-id (:expression-id record)
                 :graph-id (or (get-in record [:source :efir])
                               (:target-efir record))
                 :candidate-id (get-in record [:source :eml-candidate])
                 :domain (:domain record)
                 :branch-policy (:branch-policy record)
                 :numeric-mode (:numeric-mode record)
                 :bound (:bound record)
                 :checker (get-in record [:checker :name])
                 :provider (:provider record)
                 :diagnostic-family :math-proof}
                extra)))

(defn math6-claim-missing-fields
  [claim]
  (vec (remove #(perf-present? (get claim %))
               [:claim-id :source :claim :domain :branch-policy
                :numeric-mode :precision :status])))

(defn math9-rule-missing-fields
  [rule]
  (vec (remove #(perf-present? (get rule %))
               [:rule-id :version :pattern :replacement :domain
                :branch-policy :numeric-modes :proof-status :source])))

(defn math-proof-validate-math6!
  [source-path manifest suite]
  (doseq [claim (:claims suite)]
    (let [missing-fields (math6-claim-missing-fields claim)]
      (when (seq missing-fields)
        (math-proof-fail! "MATH6-CLAIM" source-path manifest claim
                          {:missing-fields missing-fields
                           :remediation "Emit complete claim, source, domain, mode, branch, precision, and status fields."}))
      (when (or (not (perf-present? (:domain claim)))
                (false? (:domain-valid? claim)))
        (math-proof-fail! "MATH6-DOMAIN" source-path manifest claim
                          {:remediation "Use exact rational interval endpoints or equivalent checker-exact domains."}))
      (when (or (not (perf-present? (:branch-policy claim)))
                (false? (:branch-compatible? claim)))
        (math-proof-fail! "MATH6-BRANCH" source-path manifest claim
                          {:remediation "Keep proof branch policy compatible with EFIR and EML facts."}))
      (when (or (:invalidated? claim) (= :invalidated (:status claim)))
        (math-proof-fail! "MATH6-INVALIDATED" source-path manifest claim
                          {:remediation "Reject stale proof artifacts after invalidating changes."}))))
  (doseq [roundoff (:roundoff-ledger suite)]
    (when-not (true? (:outward-rounding-proven? roundoff))
      (math-proof-fail! "MATH6-ROUNDING" source-path manifest roundoff
                        {:remediation "Prove outward rounding and record floating assumptions."})))
  (doseq [partition (:partitions suite)]
    (when (or (false? (:replayable? partition))
              (not (perf-present? (:cells partition))))
      (math-proof-fail! "MATH6-PARTITION" source-path manifest partition
                        {:remediation "Emit deterministic partition trees with replayable cell ordering."}))
    (when (and (seq (:unresolved partition))
               (not (and (get-in partition [:residual-check :permitted?])
                         (get-in partition [:residual-check :emitted?]))))
      (math-proof-fail! "MATH6-UNRESOLVED" source-path manifest partition
                        {:remediation "Unresolved cells require legal emitted residual checks or proof rejection."})))
  (doseq [bound (:bound-ledger suite)]
    (when (or (not (true? (:bounds-sufficient? bound)))
              (not (number? (:real-bound bound)))
              (not (number? (:roundoff-bound bound)))
              (not (number? (:combined-bound bound)))
              (and (number? (:combined-bound bound))
                   (number? (:required-bound bound))
                   (> (:combined-bound bound) (:required-bound bound))))
      (math-proof-fail! "MATH6-BOUND" source-path manifest bound
                        {:remediation "Record sufficient separate real and roundoff bounds."})))
  (doseq [provider (:provider-results suite)]
    (when (or (not (true? (:imported-through-safe15? provider)))
              (not= :accepted (:trust-policy provider))
              (not (true? (:replayable? provider))))
      (math-proof-fail! "MATH6-PROVIDER" source-path manifest provider
                        {:remediation "Import provider output through SAFE15 trust policy with replayable evidence."})))
  :complete)

(defn math-proof-validate-math9!
  [source-path manifest suite]
  (doseq [rule (:rewrite-rules suite)]
    (let [missing-fields (math9-rule-missing-fields rule)]
      (when (seq missing-fields)
        (math-proof-fail! "MATH9-RULE-SHAPE" source-path manifest rule
                          {:missing-fields missing-fields
                           :remediation "Rewrite rules need pattern, replacement, guards, modes, proof status, and source provenance."}))
      (when (or (not (perf-present? (:domain rule)))
                (false? (:domain-compatible? rule)))
        (math-proof-fail! "MATH9-DOMAIN" source-path manifest rule
                          {:remediation "Attach compatible domains to rewrite rules."}))
      (when (or (not (perf-present? (:branch-policy rule)))
                (false? (:branch-compatible? rule)))
        (math-proof-fail! "MATH9-BRANCH" source-path manifest rule
                          {:remediation "Attach compatible branch policy to rewrite rules."}))
      (when (and (:used-as-accepted? rule)
                 (not (contains? #{:proved :bounded} (:proof-status rule))))
        (math-proof-fail! "MATH9-PROOF" source-path manifest rule
                          {:remediation "Only proved or bounded rewrites may affect generated code."}))))
  (doseq [trace (:rewrite-traces suite)]
    (when (or (false? (:replayable? trace))
              (not (perf-present? (:rule trace)))
              (not (perf-present? (:source trace))))
      (math-proof-fail! "MATH9-TRACE" source-path manifest trace
                        {:remediation "Rewrite traces must replay without rerunning the optimizer."}))
    (when (some #(not= :proved (:result %))
                (:side-condition-results trace))
      (math-proof-fail! "MATH9-SIDE-CONDITION" source-path manifest trace
                        {:remediation "Discharge side conditions before accepting rewrite applications."})))
  (let [termination (:termination-report suite)]
    (when (or (:unbounded? termination)
              (not (true? (:deterministic? termination)))
              (not (perf-present? (:fuel termination))))
      (math-proof-fail! "MATH9-TERMINATION" source-path manifest termination
                        {:remediation "Bound rewrite application with deterministic fuel or proof-guided termination."})))
  (doseq [counterexample (:counterexamples suite)]
    (when (:disproves-accepted? counterexample)
      (math-proof-fail! "MATH9-COUNTEREXAMPLE" source-path manifest counterexample
                        {:remediation "Do not accept identities disproved by counterexample fixtures."})))
  (let [egraph (:egraph-report suite)]
    (when (or (false? (:valid? egraph))
              (not (perf-present? (:cost-model egraph)))
              (not (perf-present? (:bounds egraph)))
              (not (perf-present? (:proof-replay egraph)))
              (not (perf-present? (:explanation egraph))))
      (math-proof-fail! "MATH9-EGRAPH" source-path manifest egraph
                        {:remediation "E-graph extraction needs guards, bounded fuel, cost model, explanation, and proof replay."})))
  (doseq [claim (:equality-claims suite)]
    (when (and (:accepted? claim)
               (contains? #{:tree-identity :eclass-membership
                            :unproved-extraction}
                          (:source claim)))
      (math-proof-fail! "MATH9-EQUALITY" source-path manifest claim
                        {:remediation "Use proof replay, interval proof, or accepted certificate evidence for equality."})))
  :complete)

(defn math-proof-capability-proof
  [suite]
  {:interval-claims-complete?
   (every? #(empty? (math6-claim-missing-fields %)) (:claims suite))
   :exact-domains-and-branch-policy?
   (every? #(and (:domain-valid? %) (:branch-compatible? %)) (:claims suite))
   :partition-replayable?
   (every? #(and (:replayable? %) (perf-present? (:cells %)))
           (:partitions suite))
   :bounds-separated-and-sufficient?
   (every? #(and (:bounds-sufficient? %)
                 (number? (:real-bound %))
                 (number? (:roundoff-bound %)))
           (:bound-ledger suite))
   :providers-safe15-imported?
   (every? #(and (:imported-through-safe15? %)
                 (= :accepted (:trust-policy %)))
           (:provider-results suite))
   :rewrite-rules-proof-gated?
   (every? #(or (not (:used-as-accepted? %))
                (contains? #{:proved :bounded} (:proof-status %)))
           (:rewrite-rules suite))
   :rewrite-traces-replayable?
   (every? #(and (:replayable? %) (perf-present? (:source %)))
           (:rewrite-traces suite))
   :egraph-extraction-proof-backed?
   (let [egraph (:egraph-report suite)]
     (and (:valid? egraph)
          (perf-present? (:proof-replay egraph))
          (perf-present? (:explanation egraph))))
   :tree-identity-not-equality?
   (every? #(not (and (:accepted? %)
                      (contains? #{:tree-identity :eclass-membership
                                   :unproved-extraction}
                                 (:source %))))
           (:equality-claims suite))
   :status :complete})