(ns gravity.optimization
  "Native seed integer-equality rewriting and reconstruction data.")

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {selection {:arglists ([mir]), :admission :none},
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
 [:single-entry-or-diamond-data-shape
  :one-integer-equality-of-two-i64-constants
  :exact-edge-use-reconstruction
  :existing-record-assembly],
 :dependency-direction
 {:requires [],
  :consumed-by [gravity.bootstrap-c13-comparison],
  :forbids
  [gravity.bootstrap
   gravity.c13-optimization
   gravity.cli
   gravity.release]},
 :issues-verifier-certificates? false,
 :governing-contracts
 [D1
  D2
  D3
  D6
  D8
  D9
  C1
  C11
  C13
  C15
  SAFE15
  PERF1
  PERF2
  PERF10
  BOOT8
  TEST2],
 :unsupported
 [:untrusted-native-ingress
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
 [:explicit-native-mir-data
  :two-i64-constant-equality-selection
  :reconstruction-delta
  :existing-evidence-and-base-records
  :digest-and-path-neutral-operations
  :selection-rewrite-restore-identity-operations],
 :namespace gravity.optimization,
 :diagnostic-ownership
 {:stage [C13-VERIFY :reconstruction-and-output-identity-mismatch],
  :existing-owner
  [:input-admission :c11-verification :diagnostic-publication]},
 :artifact-outputs
 [:selection-or-nil
  :rewritten-mir
  :reconstructed-mir-or-nil
  :unchanged-output-identity-protocol
  :extended-evidence-data
  :translation-record-or-c13-refusal],
 :contract-boundary :native-seed-integer-equality-reconstruction}
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

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
