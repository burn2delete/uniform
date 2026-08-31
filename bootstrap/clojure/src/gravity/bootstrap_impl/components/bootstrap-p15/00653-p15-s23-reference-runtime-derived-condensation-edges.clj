

(defn p15-s23-reference-runtime-derived-condensation-edges
  [call-edges function-to-scc]
  (set
   (for [[caller callees] call-edges
         callee callees
         :let [caller-scc (get function-to-scc caller)
               callee-scc (get function-to-scc callee)]
         :when (not= caller-scc callee-scc)]
     [caller-scc callee-scc])))

(defn p15-s23-reference-runtime-allocation-site-kind
  [operation]
  (case operation
    :str :builtin/str
    :vector :literal/vector
    :map :literal/map
    :conj :builtin/conj
    :assoc :builtin/assoc
    :rest :builtin/rest))

(defn p15-s23-reference-runtime-validate-function-graph!
  [source-path target definitions derived]
  (let [graph (get definitions
                   'p15-s23-reference-runtime-function-graph)
        call-edges (:call-edges derived)
        partition (get-in derived [:scc :partition])
        recursive-groups (get-in derived [:scc :recursive-groups])
        asserted-sccs (:sccs graph)
        function-to-scc (:function-to-scc graph)
        asserted-partition (set (map :functions asserted-sccs))
        asserted-recursive-groups
        (set (map :functions (filter :recursive? asserted-sccs)))
        derived-condensation
        (p15-s23-reference-runtime-derived-condensation-edges
         call-edges function-to-scc)
        derived-recursive-ids
        (set (keep (fn [{:keys [scc-id functions]}]
                     (when (contains? recursive-groups functions) scc-id))
                   asserted-sccs))]
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-function-graph-functions
     p15-s23-reference-runtime-function-set (:functions graph))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-function-graph-edges
     call-edges (:edges graph))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-function-graph-scc-partition
     partition asserted-partition)
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-function-graph-recursive-sccs
     recursive-groups asserted-recursive-groups)
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-function-graph-condensation
     derived-condensation (:condensation-edges graph))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-function-graph-recursive-ids
     derived-recursive-ids (:recursive-scc-ids graph))
    (doseq [{:keys [scc-id functions]} asserted-sccs
            function-name functions]
      (p15-s23-reference-runtime-ensure!
       source-path target :runtime-contract-function-to-scc
       scc-id (get function-to-scc function-name)))))

(defn p15-s23-reference-runtime-validate-function-effects!
  [source-path target definitions authoritative-module derived]
  (let [table (get definitions
                   'p15-s23-reference-runtime-function-effects)
        effect-graph (get definitions
                          'p15-s23-reference-runtime-effect-graph)
        allocation-records (:allocation-records derived)
        effect-facts (:function-effects derived)
        declared-effects (:effects authoritative-module)]
    (doseq [function-name (sort-by pr-str p15-s23-reference-runtime-function-set)]
      (let [asserted (get-in table [:functions function-name])
            asserted-node (get-in effect-graph [:nodes function-name])
            asserted-function (get-in effect-graph
                                      [:functions function-name])
            effects (get effect-facts function-name)
            allocation-summary
            (p15-s23-reference-runtime-allocation-summary
             (get allocation-records function-name))
            proven-operations (keys (:proven-counts allocation-summary))
            expected-table
            (merge effects
                   {:direct-allocation-sites
                    (set (map p15-s23-reference-runtime-allocation-site-kind
                              proven-operations))
                    :proven-allocation-operation-counts
                    (:proven-counts allocation-summary)
                    :allocation-unproven-operation-counts
                    (:unproven-counts allocation-summary)
                    :transitive-allocation?
                    (contains? (:transitive-effects effects)
                               :memory/allocate)
                    :ordering :sequence})
            expected-node
            {:direct (:direct-effects effects)
             :latent #{}
             :transitive (:transitive-effects effects)
             :residual (:residual-effects effects)
             :handled (:handled-effects effects)
             :escaping (:escaping-effects effects)
             :ordering :sequence
             :source {:module (:module authoritative-module)
                      :function function-name}}
            expected-function
            {:declared declared-effects
             :inferred (:transitive-effects effects)
             :latent (:transitive-effects effects)
             :throws #{}
             :handled (:handled-effects effects)
             :escaping (:escaping-effects effects)}]
        (p15-s23-reference-runtime-ensure!
         source-path target :runtime-contract-function-effect-row
         expected-table
         (dissoc asserted
                 :proven-allocation-sites
                 :allocation-unproven-sites))
        (doseq [[fact expected-sites asserted-sites]
                [[:runtime-contract-proven-allocation-sites
                  (:proven-sites allocation-summary)
                  (:proven-allocation-sites asserted)]
                 [:runtime-contract-unproven-allocation-sites
                  (:unproven-sites allocation-summary)
                  (:allocation-unproven-sites asserted)]]]
          (p15-s23-reference-runtime-ensure!
           source-path target fact
           {:count (count expected-sites) :sites (set expected-sites)}
           {:count (count asserted-sites) :sites (set asserted-sites)}))
        (p15-s23-reference-runtime-ensure!
         source-path target :runtime-contract-effect-graph-node
         expected-node asserted-node)
        (p15-s23-reference-runtime-ensure!
         source-path target :runtime-contract-effect-graph-function
         expected-function asserted-function)))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-proven-allocation-counts
     (:proven-allocation-operation-counts derived)
     (:proven-allocation-operation-counts table))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-proven-allocation-count
     (reduce + 0 (vals (:proven-allocation-operation-counts derived)))
     (:proven-allocation-count table))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-unproven-allocation-counts
     (:allocation-unproven-operation-counts derived)
     (:allocation-unproven-operation-counts table))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-unproven-allocation-count
     (reduce + 0 (vals (:allocation-unproven-operation-counts derived)))
     (:allocation-unproven-count table))))

(defn p15-s23-reference-runtime-validate-effect-namespace!
  [source-path target definitions authoritative-module derived]
  (let [effect-graph
        (get definitions 'p15-s23-reference-runtime-effect-graph)
        namespace-record (:namespace effect-graph)
        effects (:function-effects derived)
        inferred (apply set/union #{} (map :transitive-effects (vals effects)))
        escaping (apply set/union #{} (map :escaping-effects (vals effects)))
        handled-records (:handled-effects namespace-record)
        ordering-records (:ordering-constraints effect-graph)
        expected-scope (:handler-scope derived)
        expected-excluded (:escaping-io-functions derived)]
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-effect-namespace
     {:declared (:effects authoritative-module)
      :inferred inferred
      :escaping-effects escaping
      :residual-effects escaping
      :escaping-io-functions expected-excluded}
     (select-keys namespace-record
                  [:declared :inferred :escaping-effects :residual-effects
                   :escaping-io-functions]))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-effect-build-effects #{}
     (:build-effects effect-graph))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-effect-replay #{}
     (:replay-required effect-graph))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-effect-handler-count 1
     (count handled-records))
    (let [handler (first handled-records)]
      (p15-s23-reference-runtime-ensure!
       source-path target :runtime-contract-effect-handler
       {:effect :io/write
        :handler-id :gravity.reference/transcript-string-handler
        :provider-id :gravity.reference/transcript-capture
        :fixture-id :gravity.reference/pinned-runtime-transcript
        :direct-handler-function p15-s23-reference-runtime-handler-function
        :transitive-function-scope expected-scope
        :excluded-functions expected-excluded}
       handler))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-effect-ordering-count 1
     (count ordering-records))
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-effect-ordering
     {:effect :io/write
      :ordering :source-left-to-right
      :must-not-duplicate true
      :must-not-eliminate true
      :handler-id :gravity.reference/transcript-string-handler
      :function-scope expected-scope}
     (first ordering-records))))

(defn p15-s23-reference-runtime-validate-scope-records!
  [source-path target definitions derived]
  (let [records
        (filter #(= :closed-plan-interpreter
                    (get-in % [:value :scope]))
                (p15-s23-reference-runtime-contract-map-records
                 definitions))
        expected-scope (:handler-scope derived)
        expected-excluded (:escaping-io-functions derived)]
    (p15-s23-reference-runtime-ensure!
     source-path target :runtime-contract-handler-scope-record-count
     13 (count records))
    (doseq [{:keys [path value]} records]
      (p15-s23-reference-runtime-ensure!
       source-path target :runtime-contract-handler-direct-function
       p15-s23-reference-runtime-handler-function
       (:direct-handler-function value))
      (p15-s23-reference-runtime-ensure!
       source-path target :runtime-contract-handler-transitive-scope
       expected-scope (:transitive-function-scope value))
      (p15-s23-reference-runtime-ensure!
       source-path target :runtime-contract-handler-excluded-functions
       expected-excluded (:excluded-functions value))
      (when-not (and (contains? value :direct-handler-function)
                     (contains? value :transitive-function-scope)
                     (contains? value :excluded-functions))
        (p15-s23-reference-runtime-fail!
         source-path target :runtime-contract-handler-scope-schema value
         {:runtime-contract-path path})))))