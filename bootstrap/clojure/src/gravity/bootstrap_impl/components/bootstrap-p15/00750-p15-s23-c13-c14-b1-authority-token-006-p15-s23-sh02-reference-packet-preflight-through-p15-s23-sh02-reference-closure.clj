(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn- p15-s23-sh02-reference-packet-preflight!
  [stage packet]
  (p15-s23-sh02-require-carrier!
   "<sh02-reference-closure>" :sh02-reference-packet packet)
  (let [mir (:optimized-mir packet) functions (:functions mir)
        reject (fn [reason]
                 (p15-s23-sh02-fail! "<sh02-reference-closure>" {}
                                    :sh02-reference-closure
                                    {:stage stage :reason reason}))]
    (when-not (and (map? mir) (map? functions) (seq functions))
      (reject :exact-mir-function-carrier))
    (let [blocks (get-in mir [:functions (first (keys functions)) :blocks])]
      (doseq [block-id (p15-s23-c11-mir-canonical-block-order mir)]
        (let [instructions (get-in blocks [block-id :instructions] [])]
          (when-not (vector? instructions) (reject :exact-mir-instruction-vector))
          (doseq [instruction instructions]
            (when-not (and (map? instruction) (vector? (:operands instruction)))
              (reject :exact-mir-operation-carrier))))))
    (doseq [[_ function] functions]
      (when-not (map? (:blocks function)) (reject :exact-mir-block-map))
      (doseq [[_ block] (:blocks function)]
        (when-not (and (map? block) (vector? (:successors block)))
          (reject :exact-mir-successor-vector))))
    :passed))

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
        edge-state
        (p15-s23-sh02-reference-edge-state
         stage root-id
         (concat root-operation-edges root-block-edges
                 cfg-edges operand-edges))
        edges
        (->> (:edges edge-state)
             (sort-by #(pr-str [(:from %) (:role %) (:to %)]))
             vec)
        node-ids
        (->> (:node-ids edge-state)
             (sort-by pr-str)
             vec)
        observed-maximum-depth
        (p15-s23-sh02-reference-depth root-id edges)]

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
