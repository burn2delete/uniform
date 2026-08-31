(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c-backend-contain-exception!
  [source-path boundary exception]
  (let [data (p15-s23-backend-trusted-exception-data
              exception 65536 128)
        c-diagnostic
        (p15-s23-c-backend-sanitized-complete-diagnostic data)
        llvm-diagnostic
        (p15-s23-b3-llvm-sanitized-complete-diagnostic data)
        c11-diagnostic
        (p15-s23-c11-mir-sanitized-complete-diagnostic data)]
    (cond
      c-diagnostic
      (p15-s23-c-backend-throw-record! c-diagnostic)

      llvm-diagnostic
      (p15-s23-c-backend-fail!
       (:rule llvm-diagnostic) source-path
       {:artifact-id (get-in llvm-diagnostic [:primary :artifact])
        :op-id (get-in llvm-diagnostic [:primary :mir-operation-id])}
       (:facts llvm-diagnostic))

      c11-diagnostic
      (p15-s23-c11-mir-throw-record! c11-diagnostic)

      :else
      (p15-s23-c-backend-fail!
       "B2-MANIFEST" source-path {}
       {:missing-fact boundary
        :stderr-hash
        (str "sha256:"
             (sha256-hex (.getName (class exception))))}))))

(def p15-s23-c13-c14-b1-c-final-packet-keys
  p15-s23-c13-c14-b1-final-packet-keys)

(def p15-s23-c13-c14-b1-c-final-packet-scope
  {:bounded-c? true :dialect :hosted-c17
   :whole-c13? false :whole-c14? false :whole-b1? false
   :whole-b2? false :public? false :release? false
   :self-hosted? false})

(defn p15-s23-c14-c-integer-comparison-rejection
  [operations operation]
  (let [by-id (into {} (map (juxt :op-id identity)) operations)
        position-by-id
        (into {} (map-indexed (fn [index item] [(:op-id item) index])
                              operations))
        operands (:operands operation)
        [left-id right-id] operands
        left (get by-id left-id)
        right (get by-id right-id)
        current-position (get position-by-id (:op-id operation))
        equality? (= :integer-eq (:opcode operation))]
    (cond
      (seq (:effects operation)) :program-effects-unsupported
      (seq (:capabilities operation)) :program-capabilities-unsupported
      (not= (:opcode operation) (:source-operation operation))
      (if equality?
        :integer-eq-source-operation
        :integer-order-source-operation)
      (not= :not-applicable (:constant-payload operation))
      (if equality?
        :integer-eq-constant-payload
        :integer-order-constant-payload)
      (not= 2 (count operands))
      (if equality?
        :integer-eq-requires-two-operands
        :integer-order-requires-two-operands)
      (nil? left)
      (if equality? :integer-eq-left-definition :integer-order-left-definition)
      (nil? right)
      (if equality? :integer-eq-right-definition :integer-order-right-definition)
      (not (< (get position-by-id left-id) current-position))
      (if equality? :integer-eq-left-dominance :integer-order-left-dominance)
      (not (< (get position-by-id right-id) current-position))
      (if equality? :integer-eq-right-dominance :integer-order-right-dominance)
      (not= :gravity/integer (:type left))
      (if equality? :integer-eq-left-type :integer-order-left-type)
      (not= :gravity/integer (:type right))
      (if equality? :integer-eq-right-type :integer-order-right-type)
      (not= :gravity/bool (:type operation))
      (if equality? :integer-eq-result-type :integer-order-result-type)
      :else nil)))

(defn p15-s23-c14-c-operation-rejection
  [operations operation]
  (if (contains?
       #{:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte}
       (:opcode operation))
    (p15-s23-c14-c-integer-comparison-rejection operations operation)
    (p15-s23-b3-llvm-operation-rejection operations operation)))

(defn p15-s23-c14-c-evaluate-operations
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

(defn- p15-s23-c13-c14-b1-c-preflight!
  [source-path c11-artifact]
  (let [mir (:mir-module c11-artifact)
        function (get-in mir [:functions 'main])
        block-order
        (when (map? function)
          (p15-s23-b3-llvm-block-order mir function))
        operations
        (when (map? function)
          (p15-s23-b3-llvm-operation-sequence function block-order))]
    (when-not
     (and (= :gravity/p15-s23-c11-authenticated-mir-artifact
             (:kind c11-artifact))
          (= :passed (:verification-status mir))
          (= :verified-mir-candidate-for-b1
             (get-in c11-artifact [:b1-preflight :status]))
          (true? (:target-independent? mir))
          (= :hosted (:profile mir))
          (= :jvm (:source-target mir))
          (= :c (:target-request mir))
          (= #{'main} (set (keys (:functions mir))))
          (map? function)
          (= [] (:params function))
          (contains? #{1 4} (count (:blocks function)))
          (empty? (:latent-effects function))
          (empty? (:capabilities function))
          (empty? (:runtime-check-table mir))
          (empty? (:domain-anchors mir))
          (empty? (:globals mir))
          (empty? (:diagnostics mir))
          (vector? operations)
          (<= 1 (count operations) 128)
          (every? empty? (map :effects operations))
          (every? empty? (map :capabilities operations)))
      (p15-s23-c-backend-fail!
       "B1-INPUT" source-path c11-artifact
       {:missing-fact :verified-pure-c11-c-backend-input
        :requested-target (:target-request mir)
        :c11-mir-id (:mir-id c11-artifact)}))
    (when-let [operation
               (first
                (filter
                 #(p15-s23-c14-c-operation-rejection operations %)
                 operations))]
      (p15-s23-c-backend-fail!
       "C14-UNSUPPORTED" source-path operation
       {:missing-fact
        (p15-s23-c14-c-operation-rejection operations operation)
        :operation-id (:op-id operation)
        :opcode (:opcode operation)
        :source-operation (:source-operation operation)
        :observed-type (:type operation)
        :c11-mir-id (:mir-id c11-artifact)}))
    (let [values (p15-s23-c14-c-evaluate-operations operations)
          return-id
          (first
           (get-in function
                   [:blocks (last block-order) :terminator :operands]))
          result (get values return-id)]
      (when-not (and (contains? values return-id)
                     (integer? result)
                     (<= 0 result 255))
        (p15-s23-c-backend-fail!
         "C14-UNSUPPORTED" source-path
         (or (get (into {} (map (juxt :op-id identity)) operations)
                  return-id)
             {})
         {:missing-fact :process-result-outside-0-to-255
          :operation-id return-id
          :observed-type
          (:type (get (into {} (map (juxt :op-id identity)) operations)
                      return-id))
          :c11-mir-id (:mir-id c11-artifact)}))
      {:mir mir :function function :block-order block-order
       :operations operations :semantic-result result}))))
