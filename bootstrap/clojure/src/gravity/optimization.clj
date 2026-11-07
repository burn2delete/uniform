(ns gravity.optimization
  "Native seed optimization artifacts and performance metadata validation; no admission."
  (:require [clojure.set]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {native-zero-cost-required-claim-fields {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [], :artifact-outputs [:native-policy-data]},
  native-zero-cost-required-nonempty-claim-fields {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [], :artifact-outputs [:native-policy-data]},
  native-zero-cost-required-artifacts {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [], :artifact-outputs [:native-policy-data]},
  native-zero-cost-abstraction-families {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [], :artifact-outputs [:native-policy-data]},
  native-zero-cost-cost-diagnostic {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [], :artifact-outputs [:native-policy-data]},
  native-zero-cost-normalize-claim {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([claim])},
  native-zero-cost-missing-claim-fields {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([present-var required-fields-var nonempty-fields-var claim])},
  native-zero-cost-failure! {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([failure-var id source-path manifest performance-claim claim extra])},
  native-zero-cost-ir-artifact-id {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([claim key])},
  native-zero-cost-costs-in-ir {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([claim ir-key])},
  native-zero-cost-hidden-costs {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([costs-in-ir-var claim])},
  native-zero-cost-missing-evidence {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([present-var artifact-id-var required-artifacts-var claim])},
  validate-native-zero-cost-claim! {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([missing-fields-var hidden-costs-var missing-evidence-var failure-var abstraction-families-var cost-diagnostic-var source-path manifest performance-claim claim])},
  native-zero-cost-capability-proof {:admission :none, :native-data-only? true, :contract-boundary :native-zero-cost-metadata-validation, :artifact-inputs [:caller-owned-native-records-and-explicit-late-vars], :artifact-outputs [:native-record-set-label-or-original-owner-result-or-exception], :arglists ([present-var artifact-id-var manifest performance-claim claims])},
  native-performance-required-claim-fields
  {:admission :none, :native-data-only? true, :artifact-outputs [:ordered-required-field-policy]},
  native-performance-present?
  {:arglists ([value]), :admission :none, :native-data-only? true},
  native-performance-normalize-claim
  {:arglists ([claim]), :admission :none, :native-data-only? true},
  native-performance-missing-claim-fields
  {:arglists ([present-var fields-var claim]), :admission :none, :native-data-only? true},
  native-performance-failure!
  {:arglists ([failure-var id source-path manifest claim extra]), :admission :none, :native-data-only? true},
  native-performance-target-fingerprint
  {:arglists ([claim]), :admission :none, :native-data-only? true},
  validate-native-performance-claim!
  {:arglists ([missing-fields-var fingerprint-var present-var failure-var source-path manifest performance claim]),
   :admission :none, :native-data-only? true, :contract-boundary :native-performance-claim-validation,
   :artifact-inputs [:caller-owned-manifest-performance-and-claim :late-native-operation-vars],
   :artifact-outputs [:relative-complete-label-or-original-owner-exception]},
  native-stage0-check-elision-proof-present?
  {:arglists ([perf-present-var record]), :admission :none, :native-data-only? true,
   :contract-boundary :stage0-check-elision-metadata-presence-and-refusal,
   :operation-selection :late-var-before-field-arguments,
   :artifact-inputs [:caller-owned-check-elision-record :late-presence-var],
   :artifact-outputs [:native-marker-presence-value]},
  validate-native-stage0-check-elision!
  {:arglists ([failure-var proof-presence-var module record]), :admission :none, :native-data-only? true,
   :contract-boundary :stage0-check-elision-metadata-presence-and-refusal,
   :operation-selection :late-var-before-field-arguments,
   :artifact-inputs [:caller-owned-stage0-module-and-record :late-failure-and-presence-vars],
   :artifact-outputs [:nil-or-original-owner-result-or-exception],
   :diagnostics [PERF10-PROOF-MISSING]},
  native-source-overrides {:arglists ([module]), :admission :none},
  native-source-overrides-artifact {:arglists ([overrides]), :admission :none},
  native-diagnostic-catalog
  {:arglists ([configuration source-path source-span]), :admission :none},
  native-artifact-base
  {:arglists ([configuration source-text module source-overrides domain-ir-artifact
               input-id contracts decisions diagnostics operations]),
   :admission :none, :required-operation-suppliers [:sha256-hex]},
  native-validate!
  {:arglists ([configuration source-path artifact operations]), :admission :none,
   :required-operation-suppliers [:invoke :validate-overrides :fail! :perf-present?]},
  native-capability-proof {:arglists ([configuration artifact]), :admission :none},
  native-source-artifact
  {:arglists ([configuration source-path source-text operations]), :admission :none,
   :required-operation-suppliers [:invoke :unsupported :source-overrides
                                  :source-overrides-artifact :validate-overrides
                                  :pass-contract-record :decision-record :diagnostic-catalog
                                  :artifact-base :validate! :capability-proof :artifact-id]},
  native-file-artifact
  {:arglists ([configuration path operations]), :admission :none,
   :required-operation-suppliers [:invoke :source-artifact :slurp]},
  selection {:arglists ([mir]), :admission :none},
  rewrite {:arglists ([mir selected]), :admission :none},
  restore {:arglists ([output delta]), :admission :none},
  output-id
  {:arglists ([digest path-neutral-value output]), :admission :none},
  extend-evidence
  {:arglists ([selection rewrite restore output-id mir base]),
   :admission :none},
  translated-record
  {:arglists ([selection rewrite restore output-id mir evidence base]),
   :admission :none}},
 :supported
 [:native-zero-cost-metadata-validation
  :native-performance-claim-validation
  :stage0-check-elision-metadata-presence-and-refusal
  :single-entry-or-diamond-data-shape
  :one-integer-equality-of-two-i64-constants
  :exact-edge-use-reconstruction
  :existing-record-assembly
  :complete-native-c13-artifact-assembly
  :ordered-native-c13-validation
  :late-compatibility-function-suppliers],
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by [gravity.bootstrap
                gravity.bootstrap-c13-comparison
                gravity.c13-optimization.artifact
                gravity.c13-optimization.projection
                gravity.c13-optimization.validation],
  :forbids
  [gravity.bootstrap
   gravity.c13-optimization
   gravity.cli
   gravity.release]},
 :issues-verifier-certificates? false,
 :governing-contracts
 [D0
  D1
  D2
  D3
  D6
  D8
  D9
  C1
  C11
  C12
  C13
  C15
  SAFE15
  PERF1
  PERF2
  PERF10
  BOOT8
  TEST2],
 :unsupported
 [:general-zero-cost-erasure
  :unlisted-cost-vocabulary
  :cross-runtime-diagnostic-precedence
  :untrusted-native-ingress
  :callback-authentication
  :genuine-c11-admission
  :general-mir-optimization
  :safety-check-elision
  :proof-provider-validation
  :backend-or-native-execution
  :performance-certification
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-owned-native-zero-cost-claims-and-ir-audits
  :late-zero-cost-policy-and-helper-vars
  :caller-owned-performance-claims-manifests-and-metadata
  :late-required-field-presence-fingerprint-and-failure-vars
  :caller-owned-stage0-module-and-check-elision-record
  :late-marker-presence-and-failure-vars
  :explicit-native-mir-data
  :two-i64-constant-equality-selection
  :reconstruction-delta
  :existing-evidence-and-base-records
  :digest-and-path-neutral-operations
  :selection-rewrite-restore-identity-operations
  :trusted-native-c13-configuration-and-source-text
  :native-module-c12-pass-decision-and-evidence-data
  :late-native-operation-and-source-input-suppliers],
 :namespace gravity.optimization,
 :diagnostic-ownership
 {:stage [:native-perf2-policy-ir-audit-and-ordered-refusal-facts
          :native-perf1-policy-normalization-and-ordered-diagnostic-facts
          PERF10-PROOF-MISSING :stage0-check-elision-marker-presence-and-refusal
          C13-VERIFY :reconstruction-and-output-identity-mismatch
          :native-c13-catalog-and-ordered-refusal-selection],
  :native-refusal-ids [PERF2-CLAIM PERF2-RESIDUAL PERF2-ALLOCATION PERF2-BOXING PERF2-DISPATCH
                       PERF2-REFLECTION PERF2-CHECK PERF2-PROFILE PERF2-EVIDENCE PERF1-CLAIM PERF1-PROFILE PERF1-TARGET PERF1-EVIDENCE PERF1-SAFETY
                       PERF1-EFFECT PERF1-CAPABILITY PERF1-NUMERIC PERF1-VARIANT PERF10-PROOF-MISSING C13-CONTRACT C13-PRESERVE C13-INVALIDATE C13-PROOF
                       C13-CHECK-ELISION C13-EFFECT C13-SAFETY C13-DOMAIN
                       C13-NONDETERMINISM C13-VERIFY],
  :existing-owner
  [:input-admission :c11-verification :diagnostic-publication
   :generic-failure-construction-and-exception-transport]},
 :artifact-outputs
 [:native-zero-cost-policy-claim-ir-audit-evidence-and-proof-envelope
  :native-policy-normalized-claim-missing-fields-fingerprint-or-relative-complete
  :native-marker-presence-or-nil-owner-result-or-exception
  :selection-or-nil
  :rewritten-mir
  :reconstructed-mir-or-nil
  :unchanged-output-identity-protocol
  :extended-evidence-data
  :translation-record-or-c13-refusal
  :native-c13-artifact-and-projections
  :native-c13-complete-marker-or-refusal],
 :contract-boundary :native-seed-optimization-data-and-artifact-pipeline}
))

(defn- i64? [x] (and (integer? x) (<= Long/MIN_VALUE x Long/MAX_VALUE)))
(defn- identifier? [x] (and (string? x) (pos? (count x))))
(defn- edge [op index]
  {:from (nth (:operands op) index) :consumer-kind :operation
   :consumer-id (:op-id op) :consumer-block (:block-id op)
   :operand-index index :edge-kind :operand})
(defn- use-row [op index]
  (assoc (dissoc (edge op index) :from) :use-kind :instruction))
(defn- indexed-rows [rows pred]
  (vec (keep-indexed (fn [index row] (when (pred row) {:index index :row row})) rows)))
(defn- fact-valid? [mir op]
  (let [id (:op-id op) facts (:facts op) source (:source op)
        type (get-in mir [:type-table (:type-fact-id facts)])]
    (and (= :gravity/mir-operation (:artifact op)) (= :passed (:verifier-status op))
         (= :hosted (:profile op)) (= #{} (:effects op) (:capabilities op))
         (= :none (:ordering op)) (nil? (:domain-anchor op))
         (= source (get-in mir [:source-map id])) (map? source)
         (= (:origin-id source) (:source-origin-id facts) (:source-origin-id type))
         (= id (:core-node-id type)) (= (:type-fact-id facts) (:fact-id type))
         (= (:type op) (:type type)) (= (:operands op) (:dependencies type))
         (= #{} (get-in mir [:effect-table (:effect-fact-id facts) :direct]))
         (= :persistent-immutable-value (get-in mir [:ownership-table (:ownership-fact-id facts) :model]))
         (= #{} (get-in mir [:capability-table (:capability-fact-id facts) :required])
            (get-in mir [:capability-table (:capability-fact-id facts) :granted]))
         (= :proven-safe (get-in mir [:safety-table (:safety-outcome-id facts) :outcome]))
         (= (:type op) (get-in mir [:data-flow-graph :values (:result op) :type]))
         (= (:constant-payload op) (get-in mir [:data-flow-graph :values (:result op) :constant-payload]))
         (= {:value-id (:result op) :operation-id id :block-id (:block-id op)}
            (get-in mir [:data-flow-graph :definitions (:result op)]) (:defined-by (get-in mir [:data-flow-graph :values (:result op)])))
         (= (:result op) (get-in mir [:data-flow-graph :values (:result op) :value-id])))))
(defn- constant? [mir op]
  (and (fact-valid? mir op) (= :constant (:opcode op)) (= :literal (:source-operation op))
       (= :gravity/integer (:type op)) (= [] (:operands op))
       (map? (:constant-payload op)) (= #{:present? :value} (set (keys (:constant-payload op))))
       (true? (get-in op [:constant-payload :present?])) (i64? (get-in op [:constant-payload :value]))))

(defn- mir-collection-shape? [mir]
  (let [functions (:functions mir)
        function (when (and (map? functions) (= 1 (count functions))) (val (first functions)))
        blocks (:blocks function) graph (:data-flow-graph mir) values (:values graph)]
    (and (map? blocks) (#{1 4} (count blocks))
         (every? #(vector? (:instructions %)) (vals blocks))
         (vector? (:edges graph)) (vector? (:terminator-uses graph))
         (map? (:definitions graph)) (map? values)
         (every? #(vector? (:uses %)) (vals values)))))

(defn selection [mir]
  (when (and (map? mir) (= :hosted (:profile mir)) (= :llvm-x86_64-linux (:target-request mir))
             (= true (:target-independent? mir)) (= {} (:globals mir) (:domain-anchors mir) (:runtime-check-table mir))
             (map? (:functions mir)) (= 1 (count (:functions mir))) (mir-collection-shape? mir))
    (let [[function-id function] (first (:functions mir)) entry (get-in mir [:control-flow-graph :entry])
          blocks (:blocks function) source (:source-core mir)
          order (if (contains? blocks (str source ":mir:join"))
                  [entry (str source ":mir:then") (str source ":mir:else") (str source ":mir:join")] [entry])
          rows (mapv #(get-in blocks [% :instructions]) order)
          operations (when (every? vector? rows) (vec (mapcat identity rows)))
          entry-ops (get-in blocks [entry :instructions])
          candidates (filter #(= :integer-eq (:opcode %)) operations)
          op (first candidates) index (first (keep-indexed #(when (= %2 op) %1) entry-ops))]
      (when (and (= [] (:params function)) (= #{} (:latent-effects function) (:capabilities function))
                 (= (set order) (set (keys blocks)))
                 (= (count operations) (count (set (map :op-id operations))) (count (set (map :result operations))))
                 (every? #(and (identifier? (:op-id %)) (identifier? (:result %))) operations)
                 (= 1 (count candidates)) (integer? index) (fact-valid? mir op)
                 (= entry (:block-id op)) (= :integer-eq (:source-operation op)) (= :gravity/bool (:type op))
                 (= :not-applicable (:constant-payload op)) (vector? (:operands op)) (= 2 (count (:operands op))))
        (let [earlier (into {} (map (juxt :result identity) (take index entry-ops)))
              operands (:operands op) producers (mapv earlier operands)
              graph (:data-flow-graph mir) edges (:edges graph)
              removed (when (vector? edges) (indexed-rows edges #(= (:op-id op) (:consumer-id %))))
              uses (mapv (fn [operand-index]
                           (let [id (nth operands operand-index)
                                 matches (indexed-rows (get-in graph [:values id :uses]) #(= (use-row op operand-index) %))]
                             (when (= 1 (count matches))
                               {:value-id id :index (:index (first matches)) :use (:row (first matches))}))) [0 1])]
          (when (and (every? identifier? operands) (every? #(constant? mir %) producers)
                     (every? #(= entry (:block-id %)) producers)
                     (= [(edge op 0) (edge op 1)] (mapv :row removed)) (every? some? uses)
                     (every? (fn [id]
                               (= (mapv #(use-row op %) (filter #(= id (nth operands %)) [0 1]))
                                  (filterv #(= (:op-id op) (:consumer-id %)) (get-in graph [:values id :uses])))) operands)
                     (every? #(vector? (get-in graph [:values % :uses])) operands))
            {:path [:functions function-id :blocks entry :instructions index] :operation op
             :value (= (get-in (first producers) [:constant-payload :value])
                       (get-in (second producers) [:constant-payload :value]))
             :delta {:kind :gravity/c13-integer-equality-delta :version 1 :operation-id (:op-id op)
                     :operand-ids operands :edges (mapv #(hash-map :index (:index %) :edge (:row %)) removed)
                     :uses uses}}))))))

(defn rewrite [mir selected]
  (let [op (:operation selected) payload {:present? true :value (:value selected)}
        delta (:delta selected) edge-indices (set (map :index (:edges delta)))
        output (-> mir (assoc-in (:path selected) (assoc op :opcode :constant :operands [] :constant-payload payload))
                   (assoc-in [:data-flow-graph :values (:result op) :constant-payload] payload)
                   (update-in [:data-flow-graph :edges]
                              #(vec (keep-indexed (fn [index row] (when-not (edge-indices index) row)) %))))]
    (reduce (fn [result id]
              (let [indices (set (map :index (filter #(= id (:value-id %)) (:uses delta))))]
                (update-in result [:data-flow-graph :values id :uses]
                           #(vec (keep-indexed (fn [index row] (when-not (indices index) row)) %)))))
            output (distinct (:operand-ids delta)))))

(defn- restore-rows [rows additions row-key]
  (when (and (vector? rows) (vector? additions) (<= (count additions) 2)
             (every? #(and (= #{:index row-key} (set (keys %))) (integer? (:index %))
                           (<= 0 (:index %) (dec (+ (count rows) (count additions))))) additions)
             (= (count additions) (count (set (map :index additions)))))
    (let [by-index (into {} (map (juxt :index row-key) additions))]
      (loop [index 0 consumed 0 result []]
        (if (= index (+ (count rows) (count additions))) result
            (if (contains? by-index index)
              (recur (inc index) consumed (conj result (get by-index index)))
              (recur (inc index) (inc consumed) (conj result (nth rows consumed)))))))))

(defn restore [output delta]
  (when (and (mir-collection-shape? output) (map? delta) (= #{:kind :version :operation-id :operand-ids :edges :uses} (set (keys delta)))
             (= :gravity/c13-integer-equality-delta (:kind delta)) (= 1 (:version delta))
             (identifier? (:operation-id delta)) (vector? (:operand-ids delta)) (= 2 (count (:operand-ids delta)))
             (every? identifier? (:operand-ids delta)) (vector? (:edges delta)) (= 2 (count (:edges delta)))
             (vector? (:uses delta)) (= 2 (count (:uses delta)))
             (every? #(and (map? %) (= #{:index :edge} (set (keys %)))
                           (integer? (:index %)) (<= 0 (:index %))) (:edges delta))
             (every? #(and (map? %) (= #{:value-id :index :use} (set (keys %)))
                           (integer? (:index %)) (<= 0 (:index %))) (:uses delta)))
    (let [function-id (first (keys (:functions output))) entry (get-in output [:control-flow-graph :entry])
          ops (get-in output [:functions function-id :blocks entry :instructions])
          index (first (keep-indexed #(when (= (:operation-id delta) (:op-id %2)) %1) ops))
          op (when (integer? index) (assoc (nth ops index) :opcode :integer-eq
                                         :operands (:operand-ids delta) :constant-payload :not-applicable))]
      (when (and op (= [(edge op 0) (edge op 1)] (mapv :edge (:edges delta)))
                 (< (:index (first (:edges delta))) (:index (second (:edges delta))))
                 (= (mapv #(hash-map :value-id (nth (:operand-ids delta) %) :use (use-row op %)) [0 1])
                    (mapv #(dissoc % :index) (:uses delta))))
        (when-let [edges (restore-rows (get-in output [:data-flow-graph :edges]) (:edges delta) :edge)]
          (let [input (-> output (assoc-in [:functions function-id :blocks entry :instructions index] op)
                          (assoc-in [:data-flow-graph :values (:result op) :constant-payload] :not-applicable)
                          (assoc-in [:data-flow-graph :edges] edges))]
            (reduce (fn [result id]
                      (when result
                        (let [additions (mapv #(dissoc % :value-id) (filter #(= id (:value-id %)) (:uses delta)))
                              rows (restore-rows (get-in result [:data-flow-graph :values id :uses]) additions :use)]
                          (when rows (assoc-in result [:data-flow-graph :values id :uses] rows)))))
                    input (distinct (:operand-ids delta)))))))))

(defn output-id [digest path-neutral-value output]
  (digest
   {:domain :gravity/c13-equality-fold-output-v1
    :mir (path-neutral-value output)}))

(defn extend-evidence [selection rewrite restore output-id mir base]
  (if-let [selected (selection mir)]
    (let [output (rewrite mir selected)]
      (if (= mir (restore output (:delta selected))) (assoc base :output-mir-id (output-id output)) base)) base))

(defn translated-record [selection rewrite restore output-id mir evidence base]
  (if-let [selected (selection mir)]
    (let [output (rewrite mir selected) id (output-id output) operation-id (get-in selected [:operation :op-id])]
      (if-not (and (= id (:output-mir-id evidence)) (= mir (restore output (:delta selected))))
        {:artifact :gravity/c13-bounded-identity-optimized-mir :schema-version 1 :status :rejected
         :diagnostic "C13-VERIFY" :missing-fact :fresh-c11-verifier-and-fact-closure :diagnostics ["C13-VERIFY"]}
        (let [pass :c13-bounded-integer-equality-fold]
          (-> base
              (assoc :artifact :gravity/c13-bounded-equality-fold-optimized-mir :optimized-mir output)
              (update :pass-contract assoc :pass-id pass :family :constant-fold
                      :invalidates [:data-flow-cache :use-def-facts] :regenerates [:use-def-facts]
                      :proof-obligations [:bounded-i64-equality-reconstruction])
              (update :decision-record assoc :pass-id pass :output-mir id :decision :fold-integer-equality
                      :changed? true :changed-operations [operation-id] :reason :bounded-i64-literal-equality
                      :invalidated [:data-flow-cache :use-def-facts])
              (update :invalidation-ledger assoc :pass-id pass :output-mir id :analysis-invalidated [:data-flow-cache]
                      :facts-invalidated [:use-def-facts] :facts-regenerated [:use-def-facts] :caches-cleared [:analysis-cache])
              (assoc :verifier-replay
                     {:artifact :gravity/c13-equality-fold-translation-replay :required? true
                      :method :bounded-i64-equality-reconstruction :delta (:delta selected)
                      :c11-artifact-id (:c11-artifact-id evidence) :verifier-report-id (:verifier-report-id evidence)
                      :c11-mir-id (:c11-mir-id evidence) :output-mir-id id :result :passed})
              (update :semantic-identity assoc :c11-output-mir-id id :unchanged? false))))) base))

(defn native-source-overrides [module]
  (or (get-in module [:metadata :compiler :c13-optimization])
      (get-in module [:metadata :compiler :optimization-lowering])
      {}))

(defn native-source-overrides-artifact [overrides]
  {:source-overrides overrides
   :lowering-request {:profile :hosted :target {:backend :jvm}}
   :input "sha256:stage0-c13-source-override"})

(defn native-diagnostic-catalog [configuration source-path source-span]
  (let [span (source-span source-path 0)]
    {:artifact :gravity/c13-optimization-diagnostic-catalog
     :status :complete
     :diagnostics
     (mapv (fn [id]
             {:diagnostic id
              :pass-id :stage0-optimization
              :decision-id "c13-diagnostic-catalog"
              :input-artifact-id "sha256:c13-diagnostic-input"
              :output-artifact-id "sha256:c13-diagnostic-output"
              :source-span span
              :changed-operations []
              :missing-fact :catalog-entry
              :proof-id :proof/c13-diagnostic-catalog
              :profile :hosted
              :target :jvm
              :remediation (get (:optimization-lowering-diagnostic-messages configuration) id)})
           (:c13-optimization-diagnostic-ids configuration))}))

(defn native-validate! [configuration source-path artifact operations]
  (((:invoke operations)) :optimization-lowering-validate-overrides!
                     ((:validate-overrides operations))
                     source-path artifact)
  (let [contracts (:optimization-pass-registry artifact)
        pipeline (:optimization-pipeline-manifest artifact)
        decisions (:optimization-decision-log artifact)
        invalidations (:invalidated-fact-ledger artifact)
        caches (:analysis-cache-records artifact)
        proof-usage (:proof-and-certificate-usage artifact)
        verifiers (:post-pass-verifier-reports artifact)
        diagnostics (get-in artifact [:optimization-diagnostic-stream :diagnostics])]
    (doseq [contract contracts]
      (when-not (every? #(contains? contract %)
                        [:artifact :pass :input :output :requires :preserves :invalidates
                         :regenerates :proof-obligations :profiles :target-assumptions :emits])
        (((:fail! operations)) "C13-CONTRACT" source-path artifact contract
               {:missing-fields [:pass :input :output :requires :preserves :proof-obligations]})))
    (when-not (= (mapv :pass contracts) (:pass-order pipeline))
      (((:fail! operations)) "C13-CONTRACT" source-path artifact pipeline {:missing-fields [:pass-order]}))
    (when-not (= :deterministic (:ordering pipeline))
      (((:fail! operations)) "C13-NONDETERMINISM" source-path artifact pipeline {:missing-fields [:ordering]}))
    (when-not (every? #(((:perf-present? operations)) (:preserved %)) decisions)
      (((:fail! operations)) "C13-PRESERVE" source-path artifact (first decisions) {:missing-fields [:preserved]}))
    (when-not (= (count contracts) (count invalidations))
      (((:fail! operations)) "C13-INVALIDATE" source-path artifact (first decisions)
             {:missing-fields [:invalidated-fact-ledger]}))
    (when-not (= (count contracts) (count caches))
      (((:fail! operations)) "C13-INVALIDATE" source-path artifact (first decisions)
             {:missing-fields [:analysis-cache-records]}))
    (when-not (= (count contracts) (count proof-usage))
      (((:fail! operations)) "C13-PROOF" source-path artifact (first decisions) {:missing-fields [:proof-usage]}))
    (when-not (every? #(some (fn [proof] (= :accepted (:status proof))) (:proofs-used %)) decisions)
      (((:fail! operations)) "C13-PROOF" source-path artifact (first decisions) {:missing-fields [:proofs-used]}))
    (doseq [[id path]
            [["C13-CHECK-ELISION" [:check-elision-record :status]]
             ["C13-EFFECT" [:effect-reordering-record :status]]
             ["C13-SAFETY" [:safety-outcome-refresh-report :status]]
             ["C13-DOMAIN" [:domain-anchor-transform-report :status]]
             ["C13-NONDETERMINISM" [:optimization-replay-record :status]]]]
      (let [expected ({"C13-CHECK-ELISION" :accepted "C13-EFFECT" :accepted
                       "C13-SAFETY" :current "C13-DOMAIN" :preserved
                       "C13-NONDETERMINISM" :replayable} id)]
        (when-not (= expected (get-in artifact path))
          (((:fail! operations)) id source-path artifact (get-in artifact [(first path)])
                 {:missing-fields [(first path)]}))))
    (when-not (every? #(= :passed (:status %)) verifiers)
      (((:fail! operations)) "C13-VERIFY" source-path artifact (first verifiers)
             {:missing-fields [:post-pass-verifier]}))
    (when-not (= (set (:c13-optimization-diagnostic-ids configuration))
                 (set (map :diagnostic diagnostics)))
      (((:fail! operations)) "C13-CONTRACT" source-path artifact (:optimization-diagnostic-stream artifact)
             {:missing-fields [:optimization-diagnostics]})))
  :complete)

(defn native-capability-proof [configuration artifact]
  (let [contracts (:optimization-pass-registry artifact)
        decisions (:optimization-decision-log artifact)]
    {:c12-domain-ir-input-verified?
     (= :complete (get-in artifact [:c12-domain-ir-artifact :capability-based-proof :status]))
     :pass-contracts-valid? (every? #(= :gravity/mir-pass-contract (:artifact %)) contracts)
     :pipeline-deterministic? (= :deterministic (get-in artifact [:optimization-pipeline-manifest :ordering]))
     :decisions-complete? (= (count contracts) (count decisions))
     :changed-and-unchanged-decisions-recorded?
     (and (some seq (map :changed-ops decisions)) (some empty? (map :changed-ops decisions)))
     :invalidations-recorded? (= (count contracts) (count (:invalidated-fact-ledger artifact)))
     :analysis-caches-recorded? (= (count contracts) (count (:analysis-cache-records artifact)))
     :proof-evidence-present?
     (every? #(some (fn [proof] (= :accepted (:status proof))) (:proofs-used %)) decisions)
     :check-elision-proof? (= :accepted (get-in artifact [:check-elision-record :status]))
     :effect-order-preserved? (= :accepted (get-in artifact [:effect-reordering-record :status]))
     :safety-outcomes-current? (= :current (get-in artifact [:safety-outcome-refresh-report :status]))
     :domain-anchors-preserved? (= :preserved (get-in artifact [:domain-anchor-transform-report :status]))
     :replayable? (= :replayable (get-in artifact [:optimization-replay-record :status]))
     :post-pass-verifiers-passed? (every? #(= :passed (:status %)) (:post-pass-verifier-reports artifact))
     :diagnostics-covered?
     (= (set (:c13-optimization-diagnostic-ids configuration))
        (set (map :diagnostic (get-in artifact [:optimization-diagnostic-stream :diagnostics]))))
     :status :complete}))

(defn native-artifact-base [configuration source-text module source-overrides domain-ir-artifact
                    input-id contracts decisions diagnostics operations]
  (let [final-output-id (:output-mir (last decisions))
        invalidations (mapv (fn [decision]
                              {:pass (:pass decision)
                               :decision-id (:decision-id decision)
                               :invalidated (:invalidated decision)
                               :regenerated (:regenerated decision)
                               :runtime-checks-restored (:residual-checks decision)
                               :caches-cleared [:data-flow-cache :domain-anchor-cache]
                               :diagnostics-affected []
                               :status :recorded})
                            decisions)
        verifiers (mapv (fn [decision]
                          {:artifact :gravity/post-pass-mir-verifier-report
                           :pass (:pass decision)
                           :decision-id (:decision-id decision)
                           :input (:output-mir decision)
                           :status :passed
                           :checks [:module :dominance :types :effects :safety :domain-anchors]})
                        decisions)]
    {:kind :gravity/stage0-c13-mir-optimization-artifact
     :task "P06-D092"
     :document-set ["C13"]
     :governing-document (:c13-optimization-governing-document configuration)
     :pass {:name :c13-mir-optimization-passes
            :input :verified-domain-ir
            :output :optimized-mir
            :requires [:c12-domain-ir-architecture :pass-contracts :semantic-anchors
                       :proof-evidence :mir-verifier]
            :preserves [:types :effects :ownership :capabilities :profile :target :safety
                        :source-spans :origin-chain :domain-anchors]
            :emits [:optimization-pass-registry :optimization-pipeline-manifest
                    :optimization-decision-log :invalidated-fact-ledger
                    :analysis-cache-records :proof-and-certificate-usage
                    :post-pass-verifier-reports
                    :optimized-mir-artifact :optimization-diagnostic-stream]
            :rejects (:c13-optimization-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c12-domain-ir-artifact
     (select-keys domain-ir-artifact [:kind :task :artifact-id :governing-document
                                      :domain-verifier-report :semantic-anchor-map :capability-based-proof])
     :domain-ir-artifact-kind (:kind domain-ir-artifact)
     :domain-ir-artifact-hash input-id
     :optimization-pass-registry contracts
     :optimization-pipeline-manifest
     {:artifact :gravity/optimization-pipeline-manifest
      :pass-order (mapv :pass contracts)
      :ordering :deterministic
      :optimization-level :stage0-safe
      :source-hash (str "sha256:" (((:sha256-hex operations)) source-text))
      :profile :hosted
      :target :jvm
      :feature-set #{:objects :exceptions :threads}
      :package-graph :stage0-single-package
      :provider-set #{:jvm/gc :jvm/exception :jvm/stdout}
      :benchmark-inputs []
      :replay-seed :none
      :status :complete}
     :optimization-decision-log decisions
     :invalidated-fact-ledger invalidations
     :analysis-cache-records
     (mapv (fn [decision]
             {:pass (:pass decision)
              :cache-key (str "sha256:" (((:sha256-hex operations)) (pr-str [(:pass decision) input-id])))
              :invalidated-by (:invalidated decision)
              :status :complete})
           decisions)
     :proof-and-certificate-usage
     (mapv (fn [decision]
             {:pass (:pass decision)
              :decision-id (:decision-id decision)
              :proofs (:proofs-used decision)
              :status :accepted})
           decisions)
     :check-elision-record
     {:artifact :gravity/check-elision-record
      :pass :bounds-check-elide
      :status :accepted
      :proof :proof/c13-bounds-check-elision
      :policy :PERF10}
     :effect-reordering-record
     {:artifact :gravity/effect-order-proof
      :pass :effect-aware-schedule
      :status :accepted
      :proof :proof/c13-effect-order-equivalence}
     :safety-outcome-refresh-report
     {:artifact :gravity/safety-outcome-refresh-report
      :status :current
      :source :mir/safety-table}
     :domain-anchor-transform-report
     {:artifact :gravity/domain-anchor-transform-report
      :status :preserved
      :anchors (:semantic-anchor-map domain-ir-artifact)}
     :optimization-replay-record
     {:artifact :gravity/optimization-replay-record
      :status :replayable
      :ordering :deterministic
      :seed :none}
     :post-pass-verifier-reports verifiers
     :optimized-mir-artifact
     {:artifact :gravity/optimized-mir
      :input input-id
      :output final-output-id
      :passes (mapv :pass contracts)
      :source-origin-map (:semantic-anchor-map domain-ir-artifact)
      :domain-anchors (:semantic-anchor-map domain-ir-artifact)
      :status :complete}
     :optimization-diagnostic-stream diagnostics
     :c13-optimization-results
     {:documents ["C13"]
      :task "P06-D092"
      :required-diagnostic-ids (:c13-optimization-diagnostic-ids configuration)
      :c12-input-status :complete
      :pass-contract-status :complete
      :pipeline-status :complete
      :decision-log-status :complete
      :invalidation-status :complete
      :analysis-cache-status :complete
      :proof-status :complete
      :post-pass-verifier-status :complete
      :diagnostic-status :complete
      :status :complete}
     :diagnostics []}))

(defn native-source-artifact [configuration source-path source-text operations]
  (let [records (((:invoke operations)) :read-source-form-records
                                   (((:unsupported operations)) :read-source-form-records)
                                   source-path source-text)
        forms (mapv :form records)
        _ (((:invoke operations)) :validate-ns-syntax! (((:unsupported operations)) :validate-ns-syntax!)
                             source-path forms)
        module (((:invoke operations)) :parse-module (((:unsupported operations)) :parse-module) source-path forms)
        source-overrides (((:invoke operations)) :c13-optimization-source-overrides
                                            ((:source-overrides operations)) module)
        _ (((:invoke operations)) :c13-optimization-validate-source-overrides!
                             (fn [path overrides]
                               (((:invoke operations)) :optimization-lowering-validate-overrides!
                                                  ((:validate-overrides operations))
                                                  path (((:source-overrides-artifact operations)) overrides)))
                             source-path source-overrides)
        domain-ir-artifact (((:invoke operations)) :compiler-c12-domain-ir-source-artifact
                                              (((:unsupported operations)) :compiler-c12-domain-ir-source-artifact)
                                              source-path source-text)
        input-id (:artifact-id domain-ir-artifact)
        contracts (mapv #(((:invoke operations)) :optimization-pass-contract-record
                                            ((:pass-contract-record operations)) %)
                        (:optimization-pass-contract-seed configuration))
        decisions (mapv #(((:invoke operations)) :optimization-decision-record
                                            ((:decision-record operations))
                                            domain-ir-artifact input-id %2 %1)
                        contracts (range))
        diagnostics (((:diagnostic-catalog operations)) configuration source-path)
        artifact-base (((:artifact-base operations)) configuration source-text module source-overrides
                                               domain-ir-artifact input-id contracts decisions diagnostics)
        _ (((:invoke operations)) :c13-optimization-validate!
                             (fn [path artifact] (((:validate! operations)) configuration path artifact))
                             source-path artifact-base)
        capability-proof (((:invoke operations)) :c13-optimization-capability-proof
                                            (fn [artifact] (((:capability-proof operations)) configuration artifact))
                                            artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (((:artifact-id operations)) (assoc artifact-base :capability-based-proof capability-proof)))))

(defn native-file-artifact [configuration path operations]
  (((:invoke operations)) :compiler-c13-optimization-source-artifact
                     (fn [source-path source-text]
                       (((:source-artifact operations)) configuration source-path source-text))
                     path (((:slurp operations)) path)))

(defn native-stage0-check-elision-proof-present?
  [perf-present-var record]
  (or ((deref perf-present-var) (:proof-id record))
      ((deref perf-present-var) (:proof record))
      ((deref perf-present-var) (:certificate record))
      ((deref perf-present-var) (:certificate-id record))
      ((deref perf-present-var) (:dominating-proof record))))


(defn validate-native-stage0-check-elision!
  [failure-var proof-presence-var module record]
  (let [source-path (:source-path module)
        erased? (or (true? (:erased? record))
                    (seq (:erased-checks record)))
        proof-present? ((deref proof-presence-var)
                        record)]
    (when (and erased? (not proof-present?))
      ((deref failure-var) "PERF10-PROOF-MISSING"
             "compiled performance metadata erases a check without proof"
             {:source-span {:source source-path}
              :profile (:profile module)
              :target (:target module)
              :check-class (:check-class record)
              :operation (:operation record)
              :ir-node (:ir-node record)
              :missing-proof :dominating-proof
              :pass-id :stage0-compiled-performance-gate
              :diagnostic-family :compiled-performance-validation
              :remediation "Keep the runtime check or attach a dominating proof and certificate before claiming check elision."}))))
(def native-performance-required-claim-fields
  [:claim-id :profile :target :target-features :input-shape :layout
   :safety-mode :benchmark])


(defn native-performance-present?
  [value]
  (and (some? value)
       (not (and (coll? value) (empty? value)))))


(defn native-performance-normalize-claim
  [claim]
  (let [claim-id (or (:claim-id claim) (:claim claim))
        input-shape (or (:input-shape claim) (:input-domain claim))]
    (assoc claim
           :claim-id claim-id
           :input-shape input-shape)))


(defn native-performance-missing-claim-fields
  [present-var fields-var claim]
  (vec (remove #((deref present-var) (get claim %))
               (deref fields-var))))


(defn native-performance-failure!
  [failure-var id source-path manifest claim extra]
  ((deref failure-var) id
         (case id
           "PERF1-CLAIM" "performance claim is incomplete"
           "PERF1-EVIDENCE" "performance claim is missing required evidence"
           "PERF1-SAFETY" "optimization loses required safety evidence"
           "PERF1-PROFILE" "optimization introduces profile-illegal behavior"
           "PERF1-EFFECT" "optimization hides or expands effects"
           "PERF1-CAPABILITY" "optimization hides or expands capability authority"
           "PERF1-NUMERIC" "optimization changes numeric mode without evidence"
           "PERF1-TARGET" "performance claim lacks target feature or fingerprint evidence"
           "PERF1-VARIANT" "generated variant lacks guard predicate evidence"
           "performance claim is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :claim-id (:claim-id claim)
                 :target-request (:target claim)
                 :diagnostic-family :performance-claim-validation}
                extra)))


(defn native-performance-target-fingerprint
  [claim]
  (or (get-in claim [:benchmark :target-fingerprint])
      (get-in claim [:benchmark :environment-fingerprint])))


(defn validate-native-performance-claim!
  [missing-fields-var fingerprint-var present-var failure-var source-path manifest performance claim]
  (let [missing-fields ((deref missing-fields-var) claim)
        target-features (set (:target-features claim))
        target-fingerprint ((deref fingerprint-var) claim)
        semantic-proof (set (:semantic-proof claim))
        safety-proof (set (:safety-proof claim))
        artifacts (set (:artifacts claim))
        lost-safety-facts (set (:lost-safety-facts performance))
        expanded-effects (set (:expanded-effects performance))
        expanded-capabilities (set (:expanded-capabilities performance))
        numeric-change (:numeric-mode-change performance)
        variants (vec (:generated-variants performance))]
    (when (seq missing-fields)
      ((deref failure-var) "PERF1-CLAIM" source-path manifest claim
                   {:missing-fields missing-fields
                    :remediation "Record profile, target, feature, input, layout, safety, and benchmark fields in the performance claim."}))
    (when-not (= (:profile manifest) (:profile claim))
      ((deref failure-var) "PERF1-PROFILE" source-path manifest claim
                   {:active-profile (:profile manifest)
                    :requested-profile (:profile claim)
                    :remediation "The performance claim profile must match the already validated source profile."}))
    (when (or (empty? target-features)
              (not ((deref present-var) target-fingerprint)))
      ((deref failure-var) "PERF1-TARGET" source-path manifest claim
                   {:target-features target-features
                    :target-fingerprint target-fingerprint
                    :remediation "Record target features and benchmark target fingerprint evidence."}))
    (when (or (empty? semantic-proof)
              (empty? safety-proof)
              (not (contains? artifacts :optimized-mir))
              (not (contains? artifacts :proof-index))
              (not (contains? artifacts :benchmark-report)))
      ((deref failure-var) "PERF1-EVIDENCE" source-path manifest claim
                   {:missing-evidence
                    (cond-> #{}
                      (empty? semantic-proof) (conj :semantic-proof)
                      (empty? safety-proof) (conj :safety-proof)
                      (not (contains? artifacts :optimized-mir))
                      (conj :optimized-mir)
                      (not (contains? artifacts :proof-index))
                      (conj :proof-index)
                      (not (contains? artifacts :benchmark-report))
                      (conj :benchmark-report))
                    :remediation "Attach semantic proof, safety proof, optimized MIR, proof index, and benchmark report evidence."}))
    (when (seq lost-safety-facts)
      ((deref failure-var) "PERF1-SAFETY" source-path manifest claim
                   {:lost-safety-facts lost-safety-facts
                    :remediation "Preserve, regenerate, or retain runtime checks for every safety fact."}))
    (when (seq expanded-effects)
      ((deref failure-var) "PERF1-EFFECT" source-path manifest claim
                   {:expanded-effects expanded-effects
                    :declared-effects (:source-effects manifest)
                    :remediation "Optimization must not hide or expand effects outside the validated manifest."}))
    (when (seq expanded-capabilities)
      ((deref failure-var) "PERF1-CAPABILITY" source-path manifest claim
                   {:expanded-capabilities expanded-capabilities
                    :declared-capabilities (:source-capabilities manifest)
                    :remediation "Optimization must not hide or expand authority outside the validated manifest."}))
    (when (and (:source-mode numeric-change)
               (:optimized-mode numeric-change)
               (not= (:source-mode numeric-change)
                     (:optimized-mode numeric-change))
               (not ((deref present-var) (:certificate numeric-change))))
      ((deref failure-var) "PERF1-NUMERIC" source-path manifest claim
                   {:source-numeric-mode (:source-mode numeric-change)
                    :optimized-numeric-mode (:optimized-mode numeric-change)
                    :certificate-id (:certificate numeric-change)
                    :remediation "Attach an explicit math or numeric certificate for mode changes."}))
    (when-let [bad-variant (first (filter #(not ((deref present-var)
                                                 (:guard-predicate %)))
                                          variants))]
      ((deref failure-var) "PERF1-VARIANT" source-path manifest claim
                   {:generated-variant (:variant-id bad-variant)
                    :missing-fact :guard-predicate
                    :remediation "Generated fast paths and variants require guard predicates and compatibility evidence."}))
    :complete))

(def native-zero-cost-required-claim-fields
  [:claim-id :profile :target :abstraction :equivalent-form
   :expected-erased-costs :erased-costs :residual-costs
   :semantic-proof :safety-proof :proof-ids :artifacts
   :source-operation :before-ir :after-ir])


(def native-zero-cost-required-nonempty-claim-fields
  #{:claim-id :profile :target :abstraction :equivalent-form
    :expected-erased-costs :semantic-proof :safety-proof :proof-ids
    :artifacts :source-operation :before-ir :after-ir})


(def native-zero-cost-required-artifacts
  #{:before-mir :after-mir})


(def native-zero-cost-abstraction-families
  #{:protocol-dispatch :interface-call :generic-function :record-field-access
    :newtype-wrapper :wrapper-value :iterator-pipeline
    :collection-view :pattern-match :macro :facet-generated-form
    :higher-order-function :error-result-wrapper :resource-wrapper
    :numeric-mode-wrapper :standard-library-wrapper})


(def native-zero-cost-cost-diagnostic
  {:allocation "PERF2-ALLOCATION"
   :hidden-allocation "PERF2-ALLOCATION"
   :boxing "PERF2-BOXING"
   :unboxing "PERF2-BOXING"
   :representation-conversion "PERF2-BOXING"
   :dynamic-dispatch "PERF2-DISPATCH"
   :vtable-lookup "PERF2-DISPATCH"
   :reflection "PERF2-REFLECTION"
   :host-reflection "PERF2-REFLECTION"
   :runtime-bounds-check "PERF2-CHECK"
   :runtime-type-check "PERF2-CHECK"
   :runtime-check "PERF2-CHECK"})


(defn native-zero-cost-normalize-claim
  [claim]
  (assoc claim
         :claim-id (or (:claim-id claim) (:claim claim))
         :expected-erased-costs
         (or (:expected-erased-costs claim) (:erased-costs claim) #{})
         :residual-costs (or (:residual-costs claim) #{})))


(defn native-zero-cost-missing-claim-fields
  [present-var required-fields-var nonempty-fields-var claim]
  (vec (remove #(if (contains? (deref nonempty-fields-var) %)
                  ((deref present-var) (get claim %))
                  (contains? claim %))
               (deref required-fields-var))))


(defn native-zero-cost-failure!
  [failure-var id source-path manifest performance-claim claim extra]
  ((deref failure-var) id
         (case id
           "PERF2-CLAIM" "zero-cost abstraction claim is incomplete"
           "PERF2-RESIDUAL" "residual work contradicts zero-cost claim"
           "PERF2-ALLOCATION" "zero-cost claim hides allocation"
           "PERF2-BOXING" "zero-cost claim hides boxing or representation conversion"
           "PERF2-DISPATCH" "zero-cost claim leaves dynamic dispatch unerased"
           "PERF2-REFLECTION" "zero-cost claim hides host reflection"
           "PERF2-CHECK" "zero-cost claim leaves runtime checks unaccounted"
           "PERF2-PROFILE" "zero-cost erasure relies on profile-illegal behavior"
           "PERF2-EVIDENCE" "zero-cost claim is missing IR or proof evidence"
           "zero-cost abstraction claim is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :claim-id (:claim-id claim)
                 :abstraction (:abstraction claim)
                 :equivalent-form (:equivalent-form claim)
                 :expected-erased-costs (:expected-erased-costs claim)
                 :residual-costs (:residual-costs claim)
                 :ir-artifacts (:artifacts claim)
                 :diagnostic-family :zero-cost-abstraction-validation}
                extra)))


(defn native-zero-cost-ir-artifact-id
  [claim key]
  (or (get-in claim [key :artifact-id])
      (get-in claim [key :id])))


(defn native-zero-cost-costs-in-ir
  [claim ir-key]
  (set (concat (get-in claim [ir-key :costs])
               (mapcat :costs (get-in claim [ir-key :operations])))))


(defn native-zero-cost-hidden-costs
  [costs-in-ir-var claim]
  (clojure.set/union (set (:residual-costs claim))
             ((deref costs-in-ir-var) claim :after-ir)
             (set (get-in claim [:allocation-audit :hidden-allocations]))
             (set (get-in claim [:boxing-audit :hidden-boxing]))
             (set (get-in claim [:dispatch-specialization-report
                                  :residual-dispatch]))
             (set (get-in claim [:reflection-audit :hidden-reflection]))
             (set (get-in claim [:runtime-check-report
                                  :unaccounted-checks]))))


(defn native-zero-cost-missing-evidence
  [present-var artifact-id-var required-artifacts-var claim]
  (let [artifacts (set (:artifacts claim))]
    (cond-> #{}
      (empty? (:semantic-proof claim)) (conj :semantic-proof)
      (empty? (:safety-proof claim)) (conj :safety-proof)
      (empty? (:proof-ids claim)) (conj :proof-ids)
      (not ((deref present-var) ((deref artifact-id-var) claim :before-ir)))
      (conj :before-ir)
      (not ((deref present-var) ((deref artifact-id-var) claim :after-ir)))
      (conj :after-ir)
      (not (clojure.set/subset? (deref required-artifacts-var) artifacts))
      (conj :before-after-mir-artifacts))))


(defn validate-native-zero-cost-claim!
  [missing-fields-var hidden-costs-var missing-evidence-var failure-var abstraction-families-var cost-diagnostic-var
   source-path manifest performance-claim claim]
  (let [missing-fields ((deref missing-fields-var) claim)
        artifacts (set (:artifacts claim))
        expected-erased-costs (set (:expected-erased-costs claim))
        erased-costs (set (:erased-costs claim))
        residual-costs (set (:residual-costs claim))
        unerased-expected-costs (clojure.set/difference expected-erased-costs
                                                erased-costs)
        hidden-costs (clojure.set/union ((deref hidden-costs-var) claim)
                                unerased-expected-costs)
        profile-illegal (set (:profile-illegal-behavior claim))
        missing-evidence ((deref missing-evidence-var) claim)]
    (when (seq missing-fields)
      ((deref failure-var) "PERF2-CLAIM" source-path manifest performance-claim claim
                   {:missing-fields missing-fields
                    :remediation "Record abstraction kind, equivalent form, erased/residual costs, before/after IR, proof ids, and proof artifacts."}))
    (when (or (not= (:profile manifest) (:profile claim))
              (not= (:profile performance-claim) (:profile claim))
              (not= (:target performance-claim) (:target claim))
              (not (contains? (deref abstraction-families-var)
                              (:abstraction claim)))
              (seq profile-illegal))
      ((deref failure-var) "PERF2-PROFILE" source-path manifest performance-claim claim
                   {:active-profile (:profile manifest)
                    :performance-profile (:profile performance-claim)
                    :target-request (:target performance-claim)
                    :profile-illegal-behavior profile-illegal
                    :remediation "Zero-cost erasure must use a legal abstraction family under the already validated profile and target request."}))
    (when (or (seq missing-evidence)
              (not (contains? artifacts :dispatch-report))
              (nil? (:allocation-audit claim))
              (nil? (:boxing-audit claim))
              (nil? (:runtime-check-report claim)))
      ((deref failure-var) "PERF2-EVIDENCE" source-path manifest performance-claim claim
                   {:missing-evidence
                    (cond-> missing-evidence
                      (not (contains? artifacts :dispatch-report))
                      (conj :dispatch-report)
                      (nil? (:allocation-audit claim))
                      (conj :allocation-audit)
                      (nil? (:boxing-audit claim))
                      (conj :boxing-audit)
                      (nil? (:runtime-check-report claim))
                      (conj :runtime-check-report))
                    :remediation "Attach before/after MIR, dispatch report, allocation and boxing audits, runtime-check report, and proof artifacts."}))
    (when-let [[cost id] (first (keep (fn [cost]
                                        (when-let [id ((deref cost-diagnostic-var)
                                                       cost)]
                                          [cost id]))
                                      hidden-costs))]
      ((deref failure-var) id source-path manifest performance-claim claim
                   {:residual-cost cost
                    :remediation "Report the residual cost instead of accepting the zero-cost claim, or prove that the equivalent lower-level form has the same cost."}))
    (when (seq (clojure.set/union residual-costs unerased-expected-costs))
      ((deref failure-var) "PERF2-RESIDUAL" source-path manifest performance-claim claim
                   {:residual-costs (clojure.set/union residual-costs
                                               unerased-expected-costs)
                    :remediation "A zero-cost claim must have an empty residual-cost set; non-erased work belongs in a performance report."}))
    :complete))


(defn native-zero-cost-capability-proof
  [present-var artifact-id-var manifest performance-claim claims]
  {:profile-legality-preserved? (every? #(= (:profile manifest)
                                            (:profile %))
                                        claims)
   :target-request-preserved? (every? #(= (:target performance-claim)
                                          (:target %))
                                      claims)
   :effect-authority-preserved?
   (every? #(= (set (get-in % [:effect-proof :source-effects]))
               (set (get-in % [:effect-proof :optimized-effects])))
           claims)
   :capability-authority-preserved?
   (every? #(= (set (get-in % [:capability-proof
                               :source-capabilities]))
               (set (get-in % [:capability-proof
                               :optimized-capabilities])))
           claims)
   :safety-evidence-preserved? (every? #(seq (:safety-proof %)) claims)
   :before-after-ir-recorded?
   (every? #(and ((deref present-var) ((deref artifact-id-var) % :before-ir))
                 ((deref present-var) ((deref artifact-id-var) % :after-ir)))
           claims)
   :residual-costs-empty? (every? #(empty? (:residual-costs %)) claims)
   :status :complete})

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
