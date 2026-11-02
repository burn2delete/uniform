(defn p15-s23-b3-llvm-evaluate-operations
  [operations]
  (let [by-id (into {} (map (juxt :op-id identity)) operations)]
    (reduce
     (fn [values operation]
       (let [opcode (:opcode operation)
             operands (:operands operation)
             value
             (cond
               (= :constant opcode)
               (get-in operation [:constant-payload :value])
               (contains? p15-s23-b3-llvm-forward-opcodes opcode)
               (get values (last operands))
               (= :truthiness opcode)
               (let [operand-id (first operands)
                     operand (get by-id operand-id)
                     operand-value (get values operand-id)]
                 (case (:type operand)
                   :gravity/nil false
                   :gravity/bool operand-value
                   :gravity/integer true))
               (= :integer-eq opcode)
               (= (get values (first operands))
                  (get values (second operands)))
               (= :integer-lt opcode)
               (< (get values (first operands))
                  (get values (second operands)))
               (= :integer-lte opcode)
               (<= (get values (first operands))
                   (get values (second operands)))
               (= :integer-gt opcode)
               (> (get values (first operands))
                  (get values (second operands)))
               (= :integer-gte opcode)
               (>= (get values (first operands))
                   (get values (second operands)))
               (= :conditional-join opcode)
               (if (get values (first operands))
                 (get values (second operands))
                 (get values (nth operands 2))))]
         (assoc values (:op-id operation) value)))
     {}
     operations)))

(defn p15-s23-b3-llvm-scalar-exit-code
  [value]
  (cond (true? value) 1 (false? value) 0 (nil? value) 0 :else value))
