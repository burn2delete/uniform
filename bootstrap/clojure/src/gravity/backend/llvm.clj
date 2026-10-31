(ns gravity.backend.llvm
  "Scalar LLVM lowering data; no authenticated backend admission."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {block-labels {:arglists ([block-order]), :admission :none},
  block-order {:arglists ([mir function]), :admission :none},
  comparison-opcodes {:kind :constant, :admission :none},
  constant-number {:arglists ([value]), :admission :none},
  evaluate-operations
  {:arglists ([forward-opcodes-port operations]), :admission :none},
  forward-opcodes {:kind :constant, :admission :none},
  operation-line
  {:arglists
   ([value-reference-port
     constant-number-port
     forward-opcodes-port
     comparison-opcodes-port
     mir
     operation
     operations
     operation-index
     block-labels]),
   :admission :none},
  operation-reference-allowed?
  {:arglists
   ([operation operand-id by-id position-by-id block-labels]),
   :admission :none},
  operation-rejection
  {:arglists
   ([operation-rejection-port
     block-labels-port
     _supported-opcodes-port
     _signed-i64?-port
     _forward-opcodes-port
     _operation-reference-allowed?-port
     _scalar-types-port
     _comparison-opcodes-port
     operations
     operation]
    [_operation-rejection-port
     _block-labels-port
     supported-opcodes-port
     signed-i64?-port
     forward-opcodes-port
     operation-reference-allowed?-port
     scalar-types-port
     comparison-opcodes-port
     operations
     operation
     block-labels]),
   :admission :none},
  operation-sequence
  {:arglists ([function block-order]), :admission :none},
  reconstructed-lowering
  {:arglists
   ([block-order-port
     operation-sequence-port
     block-labels-port
     value-reference-port
     operation-line-port
     evaluate-operations-port
     scalar-exit-code-port
     policy-port
     source-lowering-policy-port
     mir]),
   :admission :none},
  scalar-exit-code {:arglists ([value]), :admission :none},
  scalar-types {:kind :constant, :admission :none},
  signed-i64? {:arglists ([value]), :admission :none},
  supported-opcodes {:kind :constant, :admission :none},
  value-reference
  {:arglists ([operation-index operation-id]), :admission :none}},
 :supported
 [:existing-scalar-operation-legality
  :single-or-diamond-shaped-data
  :conservative-llvm-text-without-proof-sensitive-flags
  :existing-scalar-evaluation
  :source-and-fact-association-preservation
  :compatibility-var-interposition],
 :dependency-direction
 {:requires [clojure.string],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D6 D8 D9 C1 C11 C14 C15 B1 B3 B13 B14 TEST2],
 :unsupported
 [:general-mir-ingress-validation
  :proofless-metadata-admission
  :b1-c11-authenticity
  :native-or-toolchain-execution
  :llvm-verification
  :abi-verification
  :publication
  :full-b3-conformance
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:explicit-mir-and-scalar-operation-data
  :block-and-operation-order
  :source-and-fact-associations
  :explicit-policy-vars-and-compatibility-operations],
 :namespace gravity.backend.llvm,
 :diagnostic-ownership
 {:stage
  [:scalar-type-operand-effect-capability-and-dominance-reasons],
  :existing-owner
  ["B1-UNSUPPORTED"
   "B1-INPUT"
   :diagnostic-publication
   :input-admission]},
 :artifact-outputs
 [:legality-reason-or-nil
  :block-labels
  :llvm-instruction-text
  :scalar-values
  :constructed-unverified-module
  :pending-input-closure-record],
 :issues-certificates? false,
 :contract-boundary :native-llvm-scalar-lowering-data}
))

(def scalar-types
  #{:gravity/integer :gravity/bool :gravity/nil})

(def forward-opcodes
  #{:local :local-binding :sequence :lexical-scope :function-boundary})

(def comparison-opcodes
  #{:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte})

(def supported-opcodes
  (into (conj forward-opcodes
              :constant :truthiness :conditional-join)
        comparison-opcodes))

(defn signed-i64?
  [value]
  (and (integer? value)
       (<= (biginteger Long/MIN_VALUE)
           (biginteger value)
           (biginteger Long/MAX_VALUE))))

(defn block-order
  [mir function]
  (let [entry (:entry function)
        blocks (:blocks function)]
    (if (= 1 (count blocks))
      [entry]
      (let [[then-id else-id] (get-in blocks [entry :successors])
            join-id (get-in mir [:control-flow-graph :join :block-id])]
        [entry then-id else-id join-id]))))

(defn operation-sequence
  [function block-order]
  (vec (mapcat #(get-in function [:blocks % :instructions]) block-order)))

(defn block-labels
  [block-order]
  (case (count block-order)
    1 {(first block-order) "entry"}
    4 {(nth block-order 0) "entry"
       (nth block-order 1) "then"
       (nth block-order 2) "else"
       (nth block-order 3) "join"}
    {}))

(defn operation-reference-allowed?
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

(defn operation-rejection
  ([operation-rejection-port block-labels-port _supported-opcodes-port _signed-i64?-port _forward-opcodes-port _operation-reference-allowed?-port _scalar-types-port _comparison-opcodes-port operations operation]
   (let [block-order (vec (distinct (map :block-id operations)))]
     (operation-rejection-port
      operations operation (block-labels-port block-order))))
  ([_operation-rejection-port _block-labels-port supported-opcodes-port signed-i64?-port forward-opcodes-port operation-reference-allowed?-port scalar-types-port comparison-opcodes-port operations operation block-labels]
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
      (not (contains? @supported-opcodes-port opcode))
      :unsupported-mir-opcode

      (= :constant opcode)
      (cond
        (not (or (contains? #{:literal :implicit-nil} source-operation)
                 (and (= :integer-eq source-operation)
                      (= :gravity/bool type)
                      (vector? operands) (empty? operands)
                      (map? constant)
                      (= #{:present? :value} (set (keys constant)))
                      (true? (:present? constant))
                      (boolean? (:value constant)))))
        :unsupported-constant-source-operation
        (not= true (:present? constant))
        :bounded-scalar-constant-payload
        (= type :gravity/integer)
        (when-not (signed-i64?-port (:value constant))
          :bounded-scalar-constant-payload)
        (= type :gravity/bool)
        (when-not (boolean? (:value constant))
          :bounded-scalar-constant-payload)
        (= type :gravity/nil)
        (when-not (nil? (:value constant))
          :bounded-scalar-constant-payload)
        :else :bounded-scalar-constant-payload)

      (contains? @forward-opcodes-port opcode)
      (cond
        (empty? operands) :forwarded-operation-requires-operand
        (not (every? #(contains? by-id %) operands))
        :forwarded-operation-operand-definition
        (not (every?
              #(operation-reference-allowed?-port
                operation % by-id position-by-id block-labels)
              operands))
        :forwarded-operation-definition-or-dominance
        (= :function-boundary opcode) nil
        (not (contains? @scalar-types-port type))
        :unsupported-forwarded-result-type
        :else nil)

      (= :truthiness opcode)
      (let [operand (get by-id (first operands))]
        (cond
          (not= 1 (count operands)) :truthiness-requires-one-operand
          (nil? operand) :truthiness-operand-definition
          (not (operation-reference-allowed?-port
                operation (first operands) by-id position-by-id block-labels))
          :truthiness-operand-dominance
          (not (contains? @scalar-types-port (:type operand)))
          :truthiness-operand-type
          (not= :gravity/bool type) :truthiness-result-type
          :else nil))

      (contains? @comparison-opcodes-port opcode)
      (let [[left-id right-id] operands
            left (get by-id left-id)
            right (get by-id right-id)
            _current-position (get position-by-id (:op-id operation))]
        (cond
          (not= opcode source-operation)
          :source-operation-opcode-parity
          (not= :not-applicable constant)
          :nonconstant-payload-must-be-not-applicable
          (not= 2 (count operands))
          :integer-comparison-requires-two-operands
          (nil? left) :integer-comparison-left-definition
          (nil? right) :integer-comparison-right-definition
          (not (operation-reference-allowed?-port
                operation left-id by-id position-by-id block-labels))
          :integer-comparison-prior-definition-or-dominance
          (not (operation-reference-allowed?-port
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
          (not (contains? @scalar-types-port
                          (:type then-value)))
          :unsupported-conditional-then-type
          (not (contains? @scalar-types-port
                          (:type else-value)))
          :unsupported-conditional-else-type
          (not (contains? @scalar-types-port type))
          :unsupported-conditional-result-type
          :else nil))

      :else :unsupported-mir-opcode))))
(defn value-reference
  [operation-index operation-id]
  (str "%v" (get operation-index operation-id)))

(defn constant-number
  [value]
  (cond (true? value) 1 (false? value) 0 (nil? value) 0 :else value))

(defn operation-line
  [value-reference-port constant-number-port forward-opcodes-port comparison-opcodes-port mir operation operations operation-index block-labels]
  (let [opcode (:opcode operation)
        result (value-reference-port
                operation-index (:op-id operation))
        operands (:operands operation)
        by-id (into {} (map (juxt :op-id identity)) operations)]
    (cond
      (= :constant opcode)
      (str "  " result " = add i64 0, "
           (constant-number-port
            (get-in operation [:constant-payload :value])))

      (contains? @forward-opcodes-port opcode)
      (str "  " result " = add i64 0, "
           (value-reference-port
            operation-index (last operands)))

      (= :truthiness opcode)
      (let [operand-id (first operands)
            operand (get by-id operand-id)]
        (case (:type operand)
          :gravity/bool
          (str "  " result " = and i64 "
               (value-reference-port
                operation-index operand-id) ", 1")
          :gravity/nil (str "  " result " = add i64 0, 0")
          :gravity/integer (str "  " result " = add i64 0, 1")))

      (contains? @comparison-opcodes-port opcode)
      (let [[left-id right-id] operands
            comparison-result
            (str "%cmp" (get operation-index (:op-id operation)))
            predicate
            (case opcode
              :integer-eq "eq"
              :integer-lt "slt"
              :integer-lte "sle"
              :integer-gt "sgt"
              :integer-gte "sge")]
        (str "  " comparison-result " = icmp " predicate " i64 "
             (value-reference-port operation-index left-id)
             ", "
             (value-reference-port operation-index right-id)
             "\n  " result " = zext i1 " comparison-result " to i64"))

      (= :conditional-join opcode)
      (let [[_ then-id else-id] operands
            [{then-block :predecessor} {else-block :predecessor}]
            (get-in mir [:control-flow-graph :join :incoming])]
        (str "  " result " = phi i64 [ "
             (value-reference-port operation-index then-id)
             ", %" (get block-labels then-block) " ], [ "
             (value-reference-port operation-index else-id)
             ", %" (get block-labels else-block) " ]")))))
(defn evaluate-operations
  [forward-opcodes-port operations]
  (let [by-id (into {} (map (juxt :op-id identity)) operations)]
    (reduce
     (fn [values operation]
       (let [opcode (:opcode operation)
             operands (:operands operation)
             value
             (cond
               (= :constant opcode)
               (get-in operation [:constant-payload :value])
               (contains? @forward-opcodes-port opcode)
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

(defn scalar-exit-code
  [value]
  (cond (true? value) 1 (false? value) 0 (nil? value) 0 :else value))
(defn reconstructed-lowering
  [block-order-port operation-sequence-port block-labels-port value-reference-port operation-line-port evaluate-operations-port scalar-exit-code-port policy-port source-lowering-policy-port mir]
  (let [function (get-in mir [:functions 'main])
        block-order (block-order-port mir function)
        operations
        (operation-sequence-port function block-order)
        operation-index
        (into {} (map-indexed (fn [index operation]
                               [(:op-id operation) index])) operations)
        block-labels (block-labels-port block-order)
        operation-records
        (mapv
         (fn [operation]
           {:mir-operation-id (:op-id operation)
            :mir-block-id (:block-id operation)
            :source-operation (:source-operation operation)
            :mir-opcode (:opcode operation)
            :mir-type (:type operation)
            :llvm-type :i64
            :llvm-value
            (value-reference-port
             operation-index (:op-id operation))
            :llvm-instruction
            (operation-line-port
             mir operation operations operation-index block-labels)})
         operations)
        records-by-block (group-by :mir-block-id operation-records)
        block-records
        (mapv
         (fn [block-id]
           (let [block (get-in function [:blocks block-id])
                 terminator (:terminator block)
                 kind (:kind terminator)
                 operands (:operands terminator)
                 successors (:successors terminator)
                 terminator-lines
                 (case kind
                   :conditional-branch
                   [(str "  %branchcond = icmp ne i64 "
                         (value-reference-port
                          operation-index (first operands)) ", 0")
                    (str "  br i1 %branchcond, label %"
                         (get block-labels (first successors))
                         ", label %"
                         (get block-labels (second successors)))]
                   :branch
                   [(str "  br label %"
                         (get block-labels (first successors)))]
                   :return
                   [(str "  %exit = trunc i64 "
                         (value-reference-port
                          operation-index (first operands)) " to i32")
                    "  ret i32 %exit"])]
             {:mir-block-id block-id
              :llvm-label (get block-labels block-id)
              :operation-lines
              (mapv :llvm-instruction (get records-by-block block-id []))
              :terminator-lines terminator-lines}))
         block-order)
        blocks-text
        (apply str
               (map (fn [block]
                      (str (:llvm-label block) ":\n"
                           (str/join "" (map #(str % "\n")
                                             (:operation-lines block)))
                           (str/join "" (map #(str % "\n")
                                             (:terminator-lines block)))))
                    block-records))
        llvm-ir
        (str "; Gravity authenticated B3 bounded LLVM\n"
             "source_filename = \"gravity-b3-internal\"\n"
             "target datalayout = \"" (:data-layout @policy-port)
             "\"\n"
             "target triple = \"" (:target-triple @policy-port)
             "\"\n\n"
             "define ccc i32 @main() {\n" blocks-text "}\n")
        values (evaluate-operations-port operations)
        return-id
        (first (get-in function
                       [:blocks (last block-order) :terminator :operands]))
        semantic-result (get values return-id)]
    {:artifact :gravity/b3-bounded-llvm-x86_64-linux-lowering
     :schema-version 1
     :status :constructed-unverified
     :policy @source-lowering-policy-port
     :target :llvm-x86_64-linux
     :canonical-target :llvm-x86_64-linux
     :backend :llvm
     :profile :hosted
     :executable-carrier-kind
     :gravity/b3-llvm-x86_64-linux-elf-emission
     :final-artifact-kind
     :gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact
     :source-mir-id (:module-id mir)
     :block-order block-order
     :block-labels block-labels
     :operation-index operation-index
     :operation-records operation-records
     :block-records block-records
     :llvm-ir llvm-ir
     :semantic-result semantic-result
     :expected-exit-code
     (scalar-exit-code-port semantic-result)
     :runtime-providers
     [:linux/process-startup :linux/elf-loader :linux/glibc-2.36]
     :build-providers
     [:llvm-clang-20.1.8 :llvm-llc-20.1.8 :llvm-opt-20.1.8
      :llvm-as-20.1.8 :llvm-dis-20.1.8 :llvm-readobj-20.1.8
      :llvm-objdump-20.1.8 :llvm-lld-20.1.8
      :linux-x86_64-process-loader]
     :emitted-metadata []
     :emitted-function-attributes []
     :proof-to-metadata-map {}
     :ub-sensitive-flags []
     :verified-input-closure
     {:status :pending-host-content-binding
      :c11-full-mir-authenticity :required-upstream-contextual-verifier
      :b3-revalidated-surface :lowering-relevant-closure
      :full-dfg-edge-use-replay :upstream-c11-owned
      :source-core (:source-core mir)
      :pass-execution-record-id
      (get-in mir [:pass-execution-record :record-id])
      :type-fact-count (count (:type-table mir))
      :effect-fact-count (count (:effect-table mir))
      :safety-fact-count (count (:safety-table mir))
      :source-map-count (count (:source-map mir))}
     :diagnostics []
     :clojure-seed-boundary? true
     :self-hosted? false
     :public? false
     :release? false
     :whole-b3? false
     :claims {:clojure-seed-boundary? true
              :public? false :release? false
              :self-hosted? false :whole-b3? false}}))


(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
