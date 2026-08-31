(def p15-s23-b3-llvm-scalar-types
  #{:gravity/integer :gravity/bool :gravity/nil})

(def p15-s23-b3-llvm-forward-opcodes
  #{:local :local-binding :sequence :lexical-scope :function-boundary})

(def p15-s23-b3-llvm-comparison-opcodes
  #{:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte})

(def p15-s23-b3-llvm-supported-opcodes
  (into (conj p15-s23-b3-llvm-forward-opcodes
              :constant :truthiness :conditional-join)
        p15-s23-b3-llvm-comparison-opcodes))

(defn p15-s23-b3-llvm-signed-i64?
  [value]
  (and (integer? value)
       (<= (biginteger Long/MIN_VALUE)
           (biginteger value)
           (biginteger Long/MAX_VALUE))))

(defn p15-s23-b3-llvm-block-order
  [mir function]
  (let [entry (:entry function)
        blocks (:blocks function)]
    (if (= 1 (count blocks))
      [entry]
      (let [[then-id else-id] (get-in blocks [entry :successors])
            join-id (get-in mir [:control-flow-graph :join :block-id])]
        [entry then-id else-id join-id]))))

(defn p15-s23-b3-llvm-operation-sequence
  [function block-order]
  (vec (mapcat #(get-in function [:blocks % :instructions]) block-order)))

(defn p15-s23-b3-llvm-block-labels
  [block-order]
  (case (count block-order)
    1 {(first block-order) "entry"}
    4 {(nth block-order 0) "entry"
       (nth block-order 1) "then"
       (nth block-order 2) "else"
       (nth block-order 3) "join"}
    {}))

(defn p15-s23-b3-llvm-operation-reference-allowed?
  [operation operand-id by-id position-by-id block-labels]
  (let [operand (get by-id operand-id)
        current-position (get position-by-id (:op-id operation))
        operand-position (get position-by-id operand-id)
        current-label (get block-labels (:block-id operation))
        operand-label (get block-labels (:block-id operand))
        opcode (:opcode operation)]
    (and operand
         (integer? current-position)
         (integer? operand-position)
         (< operand-position current-position)
         (case current-label
           "entry" (= "entry" operand-label)
           "then" (contains? #{"entry" "then"} operand-label)
           "else" (contains? #{"entry" "else"} operand-label)
           "join" (if (= :conditional-join opcode)
                    (contains? #{"entry" "then" "else"} operand-label)
                    (contains? #{"entry" "join"} operand-label))
           false))))

(defn p15-s23-b3-llvm-operation-rejection
  ([operations operation]
   (let [block-order (vec (distinct (map :block-id operations)))]
     (p15-s23-b3-llvm-operation-rejection
      operations operation (p15-s23-b3-llvm-block-labels block-order))))
  ([operations operation block-labels]
   (let [opcode (:opcode operation)
        source-operation (:source-operation operation)
        operands (:operands operation)
        type (:type operation)
        by-id (into {} (map (juxt :op-id identity)) operations)
        position-by-id
        (into {} (map-indexed (fn [index item] [(:op-id item) index])
                              operations))
        constant (:constant-payload operation)]
    (cond
      (seq (:effects operation)) :program-effects-unsupported
      (seq (:capabilities operation)) :program-capabilities-unsupported
      (not (contains? p15-s23-b3-llvm-supported-opcodes opcode))
      :unsupported-mir-opcode

      (= :constant opcode)
      (cond
        (not (contains? #{:literal :implicit-nil} source-operation))
        :unsupported-constant-source-operation
        (not= true (:present? constant))
        :bounded-scalar-constant-payload
        (= type :gravity/integer)
        (when-not (p15-s23-b3-llvm-signed-i64? (:value constant))
          :bounded-scalar-constant-payload)
        (= type :gravity/bool)
        (when-not (boolean? (:value constant))
          :bounded-scalar-constant-payload)
        (= type :gravity/nil)
        (when-not (nil? (:value constant))
          :bounded-scalar-constant-payload)
        :else :bounded-scalar-constant-payload)

      (contains? p15-s23-b3-llvm-forward-opcodes opcode)
      (cond
        (empty? operands) :forwarded-operation-requires-operand
        (not (every? #(contains? by-id %) operands))
        :forwarded-operation-operand-definition
        (not (every?
              #(p15-s23-b3-llvm-operation-reference-allowed?
                operation % by-id position-by-id block-labels)
              operands))
        :forwarded-operation-definition-or-dominance
        (= :function-boundary opcode) nil
        (not (contains? p15-s23-b3-llvm-scalar-types type))
        :unsupported-forwarded-result-type
        :else nil)

      (= :truthiness opcode)
      (let [operand (get by-id (first operands))]
        (cond
          (not= 1 (count operands)) :truthiness-requires-one-operand
          (nil? operand) :truthiness-operand-definition
          (not (p15-s23-b3-llvm-operation-reference-allowed?
                operation (first operands) by-id position-by-id block-labels))
          :truthiness-operand-dominance
          (not (contains? p15-s23-b3-llvm-scalar-types (:type operand)))
          :truthiness-operand-type
          (not= :gravity/bool type) :truthiness-result-type
          :else nil))

      (contains? p15-s23-b3-llvm-comparison-opcodes opcode)
      (let [[left-id right-id] operands
            left (get by-id left-id)
            right (get by-id right-id)
            current-position (get position-by-id (:op-id operation))]
        (cond
          (not= opcode source-operation)
          :source-operation-opcode-parity
          (not= :not-applicable constant)
          :nonconstant-payload-must-be-not-applicable
          (not= 2 (count operands))
          :integer-comparison-requires-two-operands
          (nil? left) :integer-comparison-left-definition
          (nil? right) :integer-comparison-right-definition
          (not (p15-s23-b3-llvm-operation-reference-allowed?
                operation left-id by-id position-by-id block-labels))
          :integer-comparison-prior-definition-or-dominance
          (not (p15-s23-b3-llvm-operation-reference-allowed?
                operation right-id by-id position-by-id block-labels))
          :integer-comparison-prior-definition-or-dominance
          (not= :gravity/integer (:type left))
          :integer-comparison-left-operand-type
          (not= :gravity/integer (:type right))
          :integer-comparison-right-operand-type
          (not= :gravity/bool type)
          :integer-comparison-result-type
          :else nil))

      (= :conditional-join opcode)
      (let [[condition-id then-id else-id] operands
            condition (get by-id condition-id)
            then-value (get by-id then-id)
            else-value (get by-id else-id)]
        (cond
          (not= 3 (count operands))
          :conditional-join-requires-three-operands
          (nil? condition) :conditional-condition-definition
          (nil? then-value) :conditional-then-definition
          (nil? else-value) :conditional-else-definition
          (not= :gravity/bool (:type condition))
          :conditional-condition-type
          (not (contains? p15-s23-b3-llvm-scalar-types
                          (:type then-value)))
          :unsupported-conditional-then-type
          (not (contains? p15-s23-b3-llvm-scalar-types
                          (:type else-value)))
          :unsupported-conditional-else-type
          (not (contains? p15-s23-b3-llvm-scalar-types type))
          :unsupported-conditional-result-type
          :else nil))

      :else :unsupported-mir-opcode))))
