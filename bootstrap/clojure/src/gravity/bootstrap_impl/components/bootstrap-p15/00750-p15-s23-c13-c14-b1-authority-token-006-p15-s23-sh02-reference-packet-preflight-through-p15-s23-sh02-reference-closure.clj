(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-reference-packet-preflight!
  [stage packet]
  ;; Reject non-persistent/lazy carriers before selecting or flattening MIR
  ;; instructions. Cardinalities are then checked from O(1) vector/map counts
  ;; before `p15-s23-c11-mir-operation-sequence` can allocate its result.
  (p15-s23-sh02-require-bounded-carrier!
   "<sh02-reference-closure>" :sh02-reference-packet packet)
  (let [mir (:optimized-mir packet)
        functions (:functions mir)
        maximum-edges
        (:maximum-reference-edges
         p15-s23-sh02-authenticated-envelope-bounds)]
    (when-not (and (map? mir) (map? functions) (seq functions))
      (p15-s23-sh02-fail!
       "<sh02-reference-closure>" {}
       :bounded-sh02-reference-closure
       {:stage stage :bounded-reason :exact-mir-function-carrier}))
    (let [blocks
          (get-in mir [:functions (first (keys functions)) :blocks])
          block-order (p15-s23-c11-mir-canonical-block-order mir)
          {:keys [operation-count operand-edge-count]}
          (reduce
           (fn [{:keys [operation-count operand-edge-count]} block-id]
             (let [instructions (get-in blocks [block-id :instructions] [])]
               (when-not (vector? instructions)
                 (p15-s23-sh02-fail!
                  "<sh02-reference-closure>" {}
                  :bounded-sh02-reference-closure
                  {:stage stage
                   :bounded-reason :exact-mir-instruction-vector}))
               (let [next-operation-count
                     (+ operation-count (count instructions))
                     next-operand-edge-count
                     (reduce
                      (fn [count-so-far instruction]
                        (let [operands (:operands instruction)]
                          (when-not (and (map? instruction)
                                         (vector? operands))
                            (p15-s23-sh02-fail!
                             "<sh02-reference-closure>" {}
                             :bounded-sh02-reference-closure
                             {:stage stage
                              :bounded-reason
                              :exact-mir-operation-carrier}))
                          (+ count-so-far (count operands))))
                      operand-edge-count instructions)]
                 (when (> (+ next-operation-count
                             next-operand-edge-count)
                          maximum-edges)
                   (p15-s23-sh02-fail!
                    "<sh02-reference-closure>" {}
                    :bounded-sh02-reference-closure
                    {:stage stage
                     :observed-reference-edges
                     (+ next-operation-count next-operand-edge-count)
                     :maximum-reference-edges maximum-edges}))
                 {:operation-count next-operation-count
                  :operand-edge-count next-operand-edge-count})))
           {:operation-count 0 :operand-edge-count 0}
           block-order)
          {:keys [block-count cfg-edge-count]}
          (reduce-kv
           (fn [{:keys [block-count cfg-edge-count]} _ function]
             (let [function-blocks (:blocks function)]
               (when-not (map? function-blocks)
                 (p15-s23-sh02-fail!
                  "<sh02-reference-closure>" {}
                  :bounded-sh02-reference-closure
                  {:stage stage
                   :bounded-reason :exact-mir-block-map}))
               (reduce-kv
                (fn [{:keys [block-count cfg-edge-count]} _ block]
                  (let [successors (:successors block)]
                    (when-not (and (map? block) (vector? successors))
                      (p15-s23-sh02-fail!
                       "<sh02-reference-closure>" {}
                       :bounded-sh02-reference-closure
                       {:stage stage
                        :bounded-reason :exact-mir-successor-vector}))
                    {:block-count (inc block-count)
                     :cfg-edge-count (+ cfg-edge-count
                                        (count successors))}))
                {:block-count block-count
                 :cfg-edge-count cfg-edge-count}
                function-blocks)))
           {:block-count 0 :cfg-edge-count 0}
           functions)
          prospective-edge-count
          (+ operation-count operand-edge-count block-count cfg-edge-count)]
      (when (> prospective-edge-count maximum-edges)
        (p15-s23-sh02-fail!
         "<sh02-reference-closure>" {}
         :bounded-sh02-reference-closure
         {:stage stage
          :observed-reference-edges prospective-edge-count
          :maximum-reference-edges maximum-edges}))
      {:operation-count operation-count
       :operand-edge-count operand-edge-count
       :block-count block-count
       :cfg-edge-count cfg-edge-count
       :prospective-edge-count prospective-edge-count})))

(defn p15-s23-sh02-reference-closure
  [stage packet]
  (p15-s23-sh02-reference-packet-preflight! stage packet)
  (let [record (get packet stage)
        mir (:optimized-mir packet)
        root-id (:artifact-id record)
        operations (p15-s23-c11-mir-operation-sequence mir)
        operand-edges
        (mapcat
         (fn [operation]
           (map-indexed
            (fn [ordinal operand]
              {:from (:op-id operation)
               :role (keyword (str "operand-" ordinal))
               :to operand})
            (:operands operation)))
         operations)
        cfg-edges
        (for [[_ function] (:functions mir)
              [block-id block] (:blocks function)
              successor (:successors block)]
          {:from block-id :role :cfg-successor :to successor})
        block-ids
        (distinct
         (for [[_ function] (:functions mir)
               [block-id _] (:blocks function)]
           block-id))
        root-operation-edges
        (map (fn [operation]
               {:from root-id :role :contains-operation
                :to (:op-id operation)})
             operations)
        root-block-edges
        (map (fn [block-id]
               {:from root-id :role :contains-block :to block-id})
             block-ids)
        bounded-edge-state
        (p15-s23-sh02-bounded-reference-edge-state
         stage root-id
         (concat root-operation-edges root-block-edges
                 cfg-edges operand-edges))
        edges
        (->> (:edges bounded-edge-state)
             (sort-by #(pr-str [(:from %) (:role %) (:to %)]))
             vec)
        node-ids
        (->> (:node-ids bounded-edge-state)
             (sort-by pr-str)
             vec)
        observed-maximum-depth
        (p15-s23-sh02-reference-depth root-id edges)]
    (when (> observed-maximum-depth
             (:maximum-reference-depth
              p15-s23-sh02-authenticated-envelope-bounds))
      (p15-s23-sh02-fail!
       "<sh02-reference-closure>" {}
       :bounded-sh02-reference-closure
       {:stage stage
        :observed-reference-nodes (count node-ids)
        :observed-reference-edges (count edges)
        :observed-reference-depth observed-maximum-depth
        :maximum-reference-depth
        (:maximum-reference-depth
         p15-s23-sh02-authenticated-envelope-bounds)}))
    {:root-id root-id
     :node-ids node-ids
     :edges edges
     :fact-reference-ids
     (p15-s23-sh02-sha256-ids
      (select-keys mir
                   [:type-table :effect-table :ownership-table
                    :capability-table :safety-table]))
     :origin-reference-ids
     (p15-s23-sh02-sha256-ids (:source-map mir))
     :proof-reference-ids
     (p15-s23-sh02-sha256-ids
      {:capability (:capability-proof-table mir)
       :certificates (:proof-certificate-table mir)})
     :runtime-check-reference-ids
     (p15-s23-sh02-sha256-ids (:runtime-check-table mir))
     :observed-node-count (count node-ids)
     :observed-edge-count (count edges)
     :observed-maximum-depth observed-maximum-depth})))
