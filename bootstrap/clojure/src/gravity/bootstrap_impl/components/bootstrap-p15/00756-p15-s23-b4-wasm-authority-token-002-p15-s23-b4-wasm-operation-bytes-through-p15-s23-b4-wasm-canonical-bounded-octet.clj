(let [p15-s23-b4-wasm-authority-token (nth __gravity_bootstrap_lexical_values_136338 0)
      p15-s23-b4-wasm-node-state (nth __gravity_bootstrap_lexical_values_136338 1)]
(defn p15-s23-b4-wasm-operation-bytes
  [operation operations-by-id operation-index]
  (let [opcode (:opcode operation)
        operands (:operands operation)
        value-bytes
        (cond
          (= :constant opcode)
          (vec (concat [0x41]
                       (p15-s23-b4-wasm-s32-leb
                        (p15-s23-b4-wasm-constant-i32 operation))))

          (p15-s23-b4-wasm-forwarded? opcode)
          (p15-s23-b4-wasm-local
           0x20 (get operation-index (last operands)))

          (= :truthiness opcode)
          (let [operand (get operations-by-id (first operands))]
            (cond (= :gravity/bool (:type operand))
                  (p15-s23-b4-wasm-local
                   0x20 (get operation-index (:op-id operand)))
                  (= :gravity/nil (:type operand)) [0x41 0]
                  :else [0x41 1]))

          (contains? p15-s23-b4-wasm-comparison-opcodes opcode)
          (vec (concat
                (p15-s23-b4-wasm-local
                 0x20 (get operation-index (first operands)))
                (p15-s23-b4-wasm-local
                 0x20 (get operation-index (second operands)))
                [(get p15-s23-b4-wasm-comparison-opcodes opcode)]))

          :else
          (p15-s23-b4-wasm-fail!
           "B1-UNSUPPORTED" "<b4-wasm>" operation
           {:missing-fact :independent-operation-emission
            :opcode opcode :operation-id (:op-id operation)}))]
    (vec (concat value-bytes
                 (p15-s23-b4-wasm-local
                  0x21 (get operation-index (:op-id operation)))))))

(defn p15-s23-b4-wasm-operation-stream
  [operations all-by-id operation-index]
  (vec (mapcat #(p15-s23-b4-wasm-operation-bytes
                 % all-by-id operation-index)
               operations)))

(defn p15-s23-b4-wasm-function-bytes
  [{:keys [function block-order operations operation-index]}]
  (let [blocks (:blocks function)
        by-id (into {} (map (juxt :op-id identity) operations))]
    (if (= 1 (count block-order))
      (let [block (get blocks (first block-order))
            return-id (first (get-in block [:terminator :operands]))]
        (vec (concat
              (p15-s23-b4-wasm-operation-stream
               operations by-id operation-index)
              (p15-s23-b4-wasm-local
               0x20 (get operation-index return-id)))))
      (let [[entry-id then-id else-id join-id] block-order
            entry (get blocks entry-id)
            then-block (get blocks then-id)
            else-block (get blocks else-id)
            join-block (get blocks join-id)
            join-op (first (filter #(= :conditional-join (:opcode %))
                                   (:instructions join-block)))
            remaining-join (vec (remove #(= :conditional-join (:opcode %))
                                        (:instructions join-block)))
            condition-id (first (get-in entry [:terminator :operands]))
            then-value (first (get-in then-block [:terminator :operands]))
            else-value (first (get-in else-block [:terminator :operands]))
            return-id (first (get-in join-block [:terminator :operands]))]
        (when-not (and join-op
                       (= [condition-id then-value else-value]
                          (:operands join-op)))
          (p15-s23-b4-wasm-fail!
           "B1-INPUT" "<b4-wasm>" (or join-op {})
           {:missing-fact :exact-conditional-join-binding}))
        (vec
         (concat
          (p15-s23-b4-wasm-operation-stream
           (:instructions entry) by-id operation-index)
          (p15-s23-b4-wasm-local 0x20 (get operation-index condition-id))
          [0x04 0x7f]
          (p15-s23-b4-wasm-operation-stream
           (:instructions then-block) by-id operation-index)
          (p15-s23-b4-wasm-local 0x20 (get operation-index then-value))
          [0x05]
          (p15-s23-b4-wasm-operation-stream
           (:instructions else-block) by-id operation-index)
          (p15-s23-b4-wasm-local 0x20 (get operation-index else-value))
          [0x0b]
          (p15-s23-b4-wasm-local 0x21 (get operation-index (:op-id join-op)))
          (p15-s23-b4-wasm-operation-stream
           remaining-join by-id operation-index)
          (p15-s23-b4-wasm-local 0x20 (get operation-index return-id))))))))

(defn p15-s23-b4-wasm-evaluate
  [{:keys [function block-order operations]}]
  (let [by-id (into {} (map (juxt :op-id identity) operations))
        values
        (reduce
         (fn [values operation]
           (let [opcode (:opcode operation)
                 operands (:operands operation)
                 value
                 (cond
                   (= :constant opcode)
                   (p15-s23-b4-wasm-constant-i32 operation)
                   (p15-s23-b4-wasm-forwarded? opcode)
                   (get values (last operands))
                   (= :truthiness opcode)
                   (let [operand (get by-id (first operands))]
                     (cond (= :gravity/nil (:type operand)) 0
                           (= :gravity/bool (:type operand))
                           (get values (:op-id operand))
                           :else 1))
                   (= :integer-eq opcode)
                   (if (= (get values (first operands))
                          (get values (second operands))) 1 0)
                   (= :integer-lt opcode)
                   (if (< (get values (first operands))
                          (get values (second operands))) 1 0)
                   (= :integer-lte opcode)
                   (if (<= (get values (first operands))
                           (get values (second operands))) 1 0)
                   (= :integer-gt opcode)
                   (if (> (get values (first operands))
                          (get values (second operands))) 1 0)
                   (= :integer-gte opcode)
                   (if (>= (get values (first operands))
                           (get values (second operands))) 1 0)
                   (= :conditional-join opcode)
                   (if (zero? (get values (first operands)))
                     (get values (last operands))
                     (get values (second operands))))]
             (assoc values (:op-id operation) value)))
         {} operations)
        return-id (first (get-in function
                                 [:blocks (last block-order)
                                  :terminator :operands]))]
    (get values return-id)))

(defn p15-s23-b4-wasm-reconstruct [preflight]
  (let [operations (:operations preflight)
        body (vec (concat
                   [1]
                   (p15-s23-b4-wasm-u32-leb (count operations))
                   [0x7f]
                   (p15-s23-b4-wasm-function-bytes preflight)
                   [0x0b]))
        code-payload (vec (concat [1]
                                  (p15-s23-b4-wasm-u32-leb (count body))
                                  body))
        bytes (vec (concat
                    [0 0x61 0x73 0x6d 1 0 0 0]
                    (p15-s23-b4-wasm-section
                     1 [1 0x60 0 1 0x7f])
                    (p15-s23-b4-wasm-section 3 [1 0])
                    (p15-s23-b4-wasm-section
                     7 [1 4 0x6d 0x61 0x69 0x6e 0 0])
                    (p15-s23-b4-wasm-section 10 code-payload)))
        result (p15-s23-b4-wasm-evaluate preflight)]
    {:artifact :gravity/b4-independent-wasm32-reconstruction
     :target :wasm32-unknown-unknown
     :target-kind :core-module :features #{}
     :abi {:parameters [] :result :i32}
     :operation-count (count operations)
     :operation-opcodes (into {} (map (juxt :op-id :opcode) operations))
     :block-order (:block-order preflight)
     :operation-index (:operation-index preflight)
     :wasm-bytes bytes :expected-result result
     :imports [] :exports [{:name "main" :kind :function :index 0}]
     :memory nil :table nil :globals [] :start nil :data []
     :custom-sections [] :runtime-helpers []
     :component-model? false :wit? false :wasi? false}))

(defn p15-s23-b4-wasm-parser-u32-bytes [value]
  (loop [value value result []]
    (let [q (quot value 128)
          r (mod value 128)]
      (if (zero? q)
        (conj result r)
        (recur q (conj result (bit-or r 0x80)))))))

(defn p15-s23-b4-wasm-parser-s32-bytes [value]
  (loop [value value result []]
    (let [q0 (quot value 128)
          r0 (- value (* q0 128))
          q (if (neg? r0) (dec q0) q0)
          r (if (neg? r0) (+ r0 128) r0)
          done? (or (and (zero? q) (< r 64))
                    (and (= -1 q) (>= r 64)))]
      (if done?
        (conj result r)
        (recur q (conj result (bit-or r 0x80)))))))

(defn- p15-s23-b4-wasm-canonical-bounded-octet!
  [value missing-fact]
  (when-not (and (integer? value) (<= 0 value 255))
    (p15-s23-b4-wasm-fail!
     "B4-MANIFEST" "<b4-wasm>" {}
     {:missing-fact missing-fact}))
  (long value)))
