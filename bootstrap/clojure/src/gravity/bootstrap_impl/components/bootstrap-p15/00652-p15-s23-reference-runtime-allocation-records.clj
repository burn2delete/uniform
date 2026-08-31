

(defn p15-s23-reference-runtime-allocation-records
  [operation-records]
  (into
   (sorted-map)
   (map
    (fn [[function-name records]]
      (let [state
            (reduce
             (fn [{:keys [ordinals records]} record]
               (if-let [operation
                        (p15-s23-reference-runtime-allocation-kind record)]
                 (let [ordinal (get ordinals operation 0)]
                   {:ordinals (update ordinals operation (fnil inc 0))
                    :records
                    (conj records
                          {:function function-name
                           :path (:path record)
                           :operation operation
                           :ordinal ordinal
                           :classification
                           (if (= :rest operation)
                             :allocation-unproven
                             :proven-allocation)})})
                 {:ordinals ordinals :records records}))
             {:ordinals {} :records []}
             records)]
        [function-name (:records state)]))
    operation-records)))

(defn p15-s23-reference-runtime-call-edges
  [operation-records]
  (into (sorted-map)
        (map (fn [[function-name records]]
               [function-name
                (set (keep #(when (= :function-call (:op %))
                              (:callee %))
                           records))]))
        operation-records))

(defn p15-s23-reference-runtime-reachable
  [edges start]
  (loop [pending [start] seen #{}]
    (if-let [node (peek pending)]
      (if (contains? seen node)
        (recur (pop pending) seen)
        (recur (into (pop pending) (get edges node #{}))
               (conj seen node)))
      seen)))

(defn p15-s23-reference-runtime-scc-partition
  [edges]
  (loop [remaining (set (keys edges)) groups #{}]
    (if-let [start (first (sort-by pr-str remaining))]
      (let [forward (p15-s23-reference-runtime-reachable edges start)
            group
            (set (filter #(contains?
                           (p15-s23-reference-runtime-reachable edges %)
                           start)
                         forward))]
        (recur (set/difference remaining group) (conj groups group)))
      groups)))

(defn p15-s23-reference-runtime-allocation-summary
  [records]
  (let [counts (frequencies (map :operation records))]
    {:proven-counts (dissoc counts :rest)
     :unproven-counts (select-keys counts [:rest])
     :proven-sites
     (mapv #(select-keys % [:operation :ordinal])
           (remove #(= :rest (:operation %)) records))
     :unproven-sites
     (mapv #(select-keys % [:operation :ordinal])
           (filter #(= :rest (:operation %)) records))}))

(defn p15-s23-reference-runtime-effect-derivation
  [call-edges allocation-records]
  (let [functions (set (keys call-edges))
        direct-io
        '#{p15-s23-runtime-println-value
           p15-s23-runtime-println-two
           p15-s23-runtime-evaluate-closed-instruction}
        direct
        (into {}
              (map (fn [function-name]
                     [function-name
                      (cond-> #{}
                        (seq (remove #(= :rest (:operation %))
                                     (get allocation-records
                                          function-name)))
                        (conj :memory/allocate)
                        (contains? direct-io function-name)
                        (conj :io/write))]))
              functions)
        transitive
        (loop [current direct iteration 0]
          (let [next
                (into {}
                      (map (fn [function-name]
                             [function-name
                              (apply set/union
                                     (get direct function-name #{})
                                     (map #(get current % #{})
                                          (get call-edges function-name
                                               #{})))]))
                      functions)]
            (cond
              (= current next) next
              (> iteration (count functions)) next
              :else (recur next (inc iteration)))))
        direct-handler 'p15-s23-runtime-evaluate-closed-instruction
        handled-functions
        (set (filter #(contains?
                       (p15-s23-reference-runtime-reachable call-edges %)
                       direct-handler)
                     functions))]
    (into {}
          (map
           (fn [function-name]
             (let [effects (get transitive function-name #{})
                   handled (if (and (contains? handled-functions function-name)
                                    (contains? effects :io/write))
                             #{:io/write}
                             #{})
                   escaping (set/difference effects handled)]
               [function-name
                {:direct-effects (get direct function-name #{})
                 :transitive-effects effects
                 :handled-effects handled
                 :escaping-effects escaping
                 :residual-effects escaping
                 :source-required-capabilities
                 (cond-> #{}
                   (contains? effects :memory/allocate)
                   (conj :memory/allocator)
                   (contains? effects :io/write)
                   (conj :io/stdout))
                 :handler-required-capabilities
                 (if (seq handled) #{:test/fixture} #{})}]))
           functions))))

(def p15-s23-reference-runtime-handler-function
  'p15-s23-runtime-evaluate-closed-instruction)

(def p15-s23-reference-runtime-excluded-handler-functions
  '#{p15-s23-runtime-println-value p15-s23-runtime-println-two})

(defn p15-s23-reference-runtime-ensure!
  [source-path target fact expected observed]
  (when-not (= expected observed)
    (p15-s23-reference-runtime-fail!
     source-path target fact observed
     {:expected expected :observed observed})))

(defn p15-s23-reference-runtime-contract-map-records
  [definitions]
  (loop [pending
         (vec (map (fn [[name value]]
                     {:path [name] :value value})
                   definitions))
         records []]
    (if-let [{:keys [path value]} (peek pending)]
      (let [pending (pop pending)
            records (if (map? value)
                      (conj records {:path path :value value})
                      records)
            children
            (cond
              (map? value)
              (map (fn [[key child]]
                     {:path (conj path key) :value child})
                   value)

              (or (vector? value) (seq? value))
              (map-indexed (fn [index child]
                             {:path (conj path index) :value child})
                           value)

              (set? value)
              (map-indexed (fn [index child]
                             {:path (conj path [:set index])
                              :value child})
                           (sort-by pr-str value))

              :else nil)]
        (recur (into pending children) records))
      records)))

(defn p15-s23-reference-runtime-derived-scc-record
  [call-edges]
  (let [partition (p15-s23-reference-runtime-scc-partition call-edges)]
    {:partition partition
     :recursive-groups
     (set (filter (fn [group]
                    (or (> (count group) 1)
                        (let [function-name (first group)]
                          (contains? (get call-edges function-name #{})
                                     function-name))))
                  partition))}))

(defn p15-s23-reference-runtime-derived-contract-facts
  [source-path target plan]
  (let [operation-records
        (p15-s23-reference-runtime-operation-records source-path target plan)
        allocation-records
        (p15-s23-reference-runtime-allocation-records operation-records)
        call-edges (p15-s23-reference-runtime-call-edges operation-records)
        effects
        (p15-s23-reference-runtime-effect-derivation
         call-edges allocation-records)
        all-allocations (vec (mapcat second allocation-records))
        proven (remove #(= :allocation-unproven (:classification %))
                       all-allocations)
        unproven (filter #(= :allocation-unproven (:classification %))
                         all-allocations)
        handler-scope
        (set (for [[function-name facts] effects
                   :when (contains? (:handled-effects facts) :io/write)]
               function-name))
        escaping-io-functions
        (set (for [[function-name facts] effects
                   :when (contains? (:escaping-effects facts) :io/write)]
               function-name))]
    {:operation-paths operation-records
     :call-edges call-edges
     :scc (p15-s23-reference-runtime-derived-scc-record call-edges)
     :allocation-records allocation-records
     :proven-allocation-operation-counts
     (frequencies (map :operation proven))
     :allocation-unproven-operation-counts
     (frequencies (map :operation unproven))
     :function-effects effects
     :handler-scope handler-scope
     :escaping-io-functions escaping-io-functions}))