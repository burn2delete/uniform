(ns gravity.backend.llvm
  "Scalar LLVM lowering and native document data; no authenticated backend admission."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {native-manifest
  {:arglists ([operations input-id]), :admission :none,
   :contract-boundary :native-llvm-document-data, :seed-compatibility-data-only? true,
   :operation-inputs [:artifact-id :llvm-ir], :authenticates-inputs? false,
   :artifact-inputs [:unverified-input-id :fixed-fixture-text],
   :artifact-outputs [:legacy-llvm-document-manifest], :grants-authority? false},
  validate-native-manifest!
  {:arglists ([operations source-path artifact]), :admission :none,
   :contract-boundary :native-llvm-document-checks, :seed-compatibility-checks-only? true,
   :operation-inputs [:fail! :llvm-ir :diagnostic-ids], :authenticates-inputs? false,
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-structural-completion-or-refusal],
   :diagnostics ["B3-MANIFEST" "B3-TARGET" "B3-METADATA" "B3-UB" "B3-POINTER"
                 "B3-NUMERIC" "B3-ATOMIC" "B3-RUNTIME" "B3-ABI" "B3-PASS"],
   :diagnostic-publication :caller-owned, :grants-authority? false},
  native-coverage-report
  {:arglists ([operations artifact]), :admission :none,
   :contract-boundary :native-llvm-document-data, :seed-compatibility-data-only? true,
   :operation-inputs [:llvm-ir :diagnostic-ids], :authenticates-inputs? false,
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-document-coverage-record], :grants-authority? false},
  block-labels {:arglists ([block-order]), :admission :none},
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
 [:native-document-manifest-data-and-structural-checks
  :existing-scalar-operation-legality
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
 [:explicit-unverified-native-document-data
  :late-native-document-operations
  :explicit-mir-and-scalar-operation-data
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
 [:legacy-llvm-document-manifest
  :legacy-structural-completion-or-refusal
  :legacy-document-coverage-record
  :legality-reason-or-nil
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


(defn- native-manifest-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations input-id]
  (let [ir-hash ((native-manifest-port operations :artifact-id) (native-manifest-port operations :llvm-ir))]
    {:artifact :gravity/llvm-backend-manifest
     :backend :gravity.backend/llvm
     :accepts #{:gravity/mir :gravity/domain-ir}
     :emits #{:llvm-ir :bitcode :object :static-library :shared-library}
     :requires #{:target-triple :data-layout :abi :runtime-providers
                 :proof-table :source-map :safety-bundle}
     :metadata-gated #{:nuw :nsw :exact :noalias :nonnull
                       :dereferenceable :align :range :inbounds
                       :fast-math :tbaa}
     :rejects #{:proofless-llvm-metadata :implicit-llvm-ub
                :unpinned-data-layout :unsupported-runtime-service}
     :target-record {:triple "x86_64-unknown-linux-gnu"
                     :cpu "generic"
                     :feature-string ""
                     :pointer-width 64
                     :address-spaces {:generic 0}
                     :endianness :little
                     :integer-layout {:i64 {:abi 64 :preferred 64}}
                     :floating-layout {:f64 {:abi 64 :preferred 64}}
                     :aggregate-alignment 64
                     :vector-legal-widths [128]
                     :relocation-model :static
                     :code-model :small
                     :calling-conventions [:ccc]
                     :unwind-strategy :trap
                     :thread-local-storage :not-requested
                     :sanitizer-mode :none
                     :instrumentation-mode :none
                     :object-format :elf
                     :data-layout "e-m:e-p:64:64-i64:64-n8:16:32:64-S128"
                     :status :pinned}
     :llvm-ir-files [{:path "gravity_stage0_b3.ll"
                      :content (native-manifest-port operations :llvm-ir)
                      :hash ir-hash}]
     :proof-to-llvm-metadata-map
     [{:metadata :range
       :operation :checked-add-overflow-guard
       :proof :proof/c18-interval-check}
      {:metadata :nonnull
       :operation :managed-reference
       :proof :proof/c18-check-dominance}
      {:metadata :align
       :operation :region-pointer
       :proof :proof/c18-layout-alignment}]
     :metadata-policy {:forbidden-without-proof
                       [:nuw :nsw :exact :nonnull :dereferenceable
                        :align :range :noalias :inbounds :fast-math
                        :tbaa]
                       :emitted-in-fixture []
                       :status :gated}
     :pointer-ownership-memory-map
     {:preserved-facts [:object-identity :address-space :provenance
                       :allocation-provider :lifetime-interval
                       :valid-byte-range :alignment :initialized-state
                       :mutability :aliasing-mode :nullability]
      :gep-inbounds-policy :proof-only
      :raw-pointer-casts :ffi-unsafe-island-or-intrinsic-only
      :lifetime-intrinsics :diagnostic-safe-only
      :status :complete}
     :numeric-floating-lowering
     {:integer-modes {:checked :branch-trap-or-helper
                      :wrapping :no-overflow-assumptions
                      :saturating :intrinsic-or-helper}
      :bounded-range-metadata :interval-proof-only
      :narrowing :checked-unless-proven
      :floating-modes [:strict :reproducible :relaxed :approximate
                       :target-native]
      :fast-math-flags :certificate-only
      :status :complete}
     :atomic-volatile-concurrency-lowering
     {:records [:operation :address-space :memory-order
                :synchronization-scope :alignment :failure-ordering
                :target-feature-requirement :fallback-helper]
      :mmio-policy :volatile-and-ordered
      :data-race-evidence :safe8-required
      :status :complete}
     :runtime-abi-helper-map
     {:helpers [:allocation :deallocation :region :arena :panic :trap
                :bounds-check :numeric-check :resource-cleanup
                :atomics :synchronization :math-provider :ffi-adapter
                :stack-probe :debug-hook]
      :hidden-runtime-services []
      :profile-selection :manifest-required
      :status :complete}
     :pass-pipeline-record
     {:llvm-version :stage0-recorded
      :target-backend :x86_64
      :optimization-level :o0
      :mandatory-verification-passes [:verify-after-emission
                                      :verify-after-pipeline]
      :instrumentation-and-sanitizer-passes []
      :vectorization-passes []
      :target-specific-passes []
      :metadata-preservation [:source-map :safety :capability
                              :proof :unsafe-audit :conformance]
      :disabled-passes [{:pass :unsafe-fast-math
                         :reason :missing-explicit-floating-certificate}
                        {:pass :metadata-stripping
                         :reason :would-erase-required-evidence}]
      :post-pass-verifier-results :requires-proof-command
      :status :complete}
     :source-debug-map {:source input-id
                        :generated-origin-chain [:mir
                                                 :c14-target-lowering
                                                 :b1-interface
                                                 :b3-llvm-backend]
                        :generated-files ["gravity_stage0_b3.ll"]
                        :status :preserved}
     :safety-capability-unsafe-audit-preservation-map
     {:preserved [:source-spans :debug-locations :proofs :safety
                  :capabilities :unsafe-audit-ids :conformance]
      :stripped []
      :status :preserved}
     :unsupported-feature-report
     {:unsupported []
      :fallbacks [{:feature :proofless-noalias
                   :fallback :omit-metadata}
                  {:feature :invalid-inbounds
                   :fallback :plain-gep-or-reject}
                  {:feature :unsupported-atomic-ordering
                   :fallback :runtime-helper-or-reject}]
      :status :complete}
     :llvm-verifier-record
     {:declared-command "clang -target x86_64-unknown-linux-gnu -x ir -S -o /tmp/gravity-p07-b3.s /tmp/gravity-p07-b3.ll"
      :proof-artifact "docs/artifacts/phase-07/reports/p07-d100-b3-llvm-backend-report.md"
      :status :requires-proof-command}
     :input-artifact input-id
     :status :complete}))

(defn validate-native-manifest!
  [operations source-path artifact]
  (let [native (:native-lowering-artifact artifact)
        manifest (:llvm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b3-diagnostic-stream
                                       :diagnostics])))
        forbidden-ir-flags [" nsw" " nuw" " inbounds" " fast"
                            "!tbaa"]]
    (when-not (= :gravity/stage0-native-lowering-artifact (:kind native))
      ((native-manifest-port operations :fail!) "B3-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-artifact]}))
    (when-not (= :complete (get-in native
                                   [:capability-based-proof :status]))
      ((native-manifest-port operations :fail!) "B3-MANIFEST" source-path native
                         {:missing-fields [:native-lowering-proof]}))
    (when-not (= :pinned (get-in manifest [:target-record :status]))
      ((native-manifest-port operations :fail!) "B3-TARGET" source-path manifest
                         {:missing-fields [:target-record]}))
    (when-not (= :gated (get-in manifest [:metadata-policy :status]))
      ((native-manifest-port operations :fail!) "B3-METADATA" source-path manifest
                         {:missing-fields [:metadata-policy]}))
    (when (some #(str/includes? (native-manifest-port operations :llvm-ir) %) forbidden-ir-flags)
      ((native-manifest-port operations :fail!) "B3-UB" source-path manifest
                         {:missing-fields [:conservative-llvm-ir]}))
    (when-not (= :complete (get-in manifest
                                   [:pointer-ownership-memory-map
                                    :status]))
      ((native-manifest-port operations :fail!) "B3-POINTER" source-path manifest
                         {:missing-fields [:pointer-ownership-memory-map]}))
    (when-not (= :complete (get-in manifest
                                   [:numeric-floating-lowering :status]))
      ((native-manifest-port operations :fail!) "B3-NUMERIC" source-path manifest
                         {:missing-fields [:numeric-floating-lowering]}))
    (when-not (= :complete
                 (get-in manifest
                         [:atomic-volatile-concurrency-lowering :status]))
      ((native-manifest-port operations :fail!) "B3-ATOMIC" source-path manifest
                         {:missing-fields [:atomic-volatile-lowering]}))
    (when-not (empty? (get-in manifest
                              [:runtime-abi-helper-map
                               :hidden-runtime-services]))
      ((native-manifest-port operations :fail!) "B3-RUNTIME" source-path manifest
                         {:missing-fields [:hidden-runtime-services]}))
    (when-not (contains? (set (get-in manifest
                                      [:target-record
                                       :calling-conventions]))
                         :ccc)
      ((native-manifest-port operations :fail!) "B3-ABI" source-path manifest
                         {:missing-fields [:calling-convention]}))
    (when-not (= :complete (get-in manifest
                                   [:pass-pipeline-record :status]))
      ((native-manifest-port operations :fail!) "B3-PASS" source-path manifest
                         {:missing-fields [:pass-pipeline-record]}))
    (when-not (every? #(contains? manifest %)
                      [:target-record :llvm-ir-files
                       :proof-to-llvm-metadata-map :runtime-abi-helper-map
                       :pass-pipeline-record :source-debug-map
                       :safety-capability-unsafe-audit-preservation-map
                       :unsupported-feature-report])
      ((native-manifest-port operations :fail!) "B3-MANIFEST" source-path manifest
                         {:missing-fields [:llvm-artifact-manifest]}))
    (when-not (= (set (native-manifest-port operations :diagnostic-ids)) diagnostics)
      ((native-manifest-port operations :fail!) "B3-MANIFEST" source-path
                         (:b3-diagnostic-stream artifact)
                         {:missing-fields [:b3-diagnostics]})))
  :complete)
(defn native-coverage-report
  [operations artifact]
  (let [manifest (:llvm-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b3-diagnostic-stream
                                       :diagnostics])))
        forbidden-ir-flags [" nsw" " nuw" " inbounds" " fast"
                            "!tbaa"]]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:native-lowering-artifact
                           :capability-based-proof :status]))
     :target-and-data-layout-pinned?
     (= :pinned (get-in manifest [:target-record :status]))
     :proof-gated-metadata-covered?
     (= :gated (get-in manifest [:metadata-policy :status]))
     :safe-llvm-undefined-behavior-rejected?
     (and (contains? diagnostics "B3-UB")
          (not-any? #(str/includes? (native-manifest-port operations :llvm-ir) %)
                    forbidden-ir-flags))
     :pointer-ownership-memory-covered?
     (= :complete (get-in manifest
                          [:pointer-ownership-memory-map :status]))
     :numeric-floating-lowering-covered?
     (= :complete (get-in manifest
                          [:numeric-floating-lowering :status]))
     :atomic-volatile-ordering-covered?
     (= :complete
        (get-in manifest
                [:atomic-volatile-concurrency-lowering :status]))
     :runtime-helpers-profile-legal?
     (and (= :complete (get-in manifest
                               [:runtime-abi-helper-map :status]))
          (empty? (get-in manifest
                          [:runtime-abi-helper-map
                           :hidden-runtime-services])))
     :abi-record-covered?
     (contains? (set (get-in manifest
                             [:target-record :calling-conventions]))
                :ccc)
     :pass-pipeline-verification-covered?
     (= :complete (get-in manifest
                          [:pass-pipeline-record :status]))
     :source-debug-proof-safety-capability-preserved?
     (= :preserved
        (get-in manifest
                [:safety-capability-unsafe-audit-preservation-map
                 :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:target-record :llvm-ir-files
              :proof-to-llvm-metadata-map :runtime-abi-helper-map
              :pass-pipeline-record :source-debug-map
              :safety-capability-unsafe-audit-preservation-map
              :unsupported-feature-report])
     :diagnostics-covered?
     (= (set (native-manifest-port operations :diagnostic-ids)) diagnostics)
     :requires-external-llvm-verifier-proof?
     (= :requires-proof-command
        (get-in manifest [:llvm-verifier-record :status]))
     :status :complete}))

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
