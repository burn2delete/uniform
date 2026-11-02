(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-b2-c17-operation-expression
  [operation operations operation-index]
  (let [opcode (:opcode operation)
        operands (:operands operation)
        name-for #(p15-s23-b2-c17-value-name operation-index %)]
    (cond
      (= :constant opcode)
      (p15-s23-b2-c17-constant-expression
       (get-in operation [:constant-payload :value]))

      (p15-s23-b2-c17-forwarded-opcode? opcode)
      (name-for (last operands))

      (= :truthiness opcode)
      (let [operand-id (first operands)
            operand (some #(when (= operand-id (:op-id %)) %) operations)
            value-name (name-for operand-id)]
        (case (:type operand)
          :gravity/nil "INT64_C(0)"
          :gravity/bool
          (str "(" value-name
               " == INT64_C(0) ? INT64_C(0) : INT64_C(1))")
          "INT64_C(1)"))

      (contains?
       #{:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte}
       opcode)
      (str "(" (name-for (first operands))
           " "
           (case opcode
             :integer-eq "=="
             :integer-lt "<"
             :integer-lte "<="
             :integer-gt ">"
             :integer-gte ">=")
           " " (name-for (second operands))
           " ? INT64_C(1) : INT64_C(0))")

      (= :conditional-join opcode)
      (str "(" (name-for (first operands))
           " == INT64_C(0) ? " (name-for (last operands))
           " : " (name-for (second operands)) ")")

      :else "INT64_C(0)")))

(defn p15-s23-b2-c17-operation-record
  [operation operations operation-index]
  (let [name (p15-s23-b2-c17-value-name
              operation-index (:op-id operation))
        expression
        (p15-s23-b2-c17-operation-expression
         operation operations operation-index)]
    {:mir-operation-id (:op-id operation)
     :mir-block-id (:block-id operation)
     :source-operation (:source-operation operation)
     :mir-opcode (:opcode operation)
     :mir-type (:type operation)
     :c-type "int64_t"
     :c-value name
     :c-expression expression
     :c-statement
     (str "  int64_t " name " = " expression ";\n"
          "  (void)" name ";\n")
     :c-liveness-use (str "(void)" name ";")
     :safety-outcome :proven-safe}))

(defn p15-s23-b2-c17-return-id
  [function block-order]
  (first (get-in function [:blocks (last block-order)
                           :terminator :operands])))

(defn p15-s23-b2-c17-source-text
  [operation-records return-value-name]
  (str "#include <stdint.h>\n"
       "#include \"program.h\"\n\n"
       "int main(void) {\n"
       (apply str (map :c-statement operation-records))
       "  return (int)" return-value-name ";\n"
       "}\n"))

(defn p15-s23-b2-c17-abi-layout-record
  [b1-record]
  {:artifact :gravity/b2-abi-layout-manifest
   :status :pending-gate-b-toolchain-verification
   :compiler-family :apple-clang
   :compiler-version-constraint "21.0.0"
   :target-triple (get-in b1-record [:target :triple])
   :data-layout (get-in b1-record [:target :data-layout])
   :data-model :lp64
   :endianness :little
   :integer-carrier :int64-t
   :process-result :int
   :process-result-range [0 255]
   :alignment
   {:int {:size-bits 32 :alignment-bits 32}
    :int64-t {:size-bits 64 :alignment-bits 64}}
   :entrypoint-calling-convention :darwin-pcs-ccc
   :struct-field-order :not-applicable-bounded-scalar-slice
   :padding :not-applicable-bounded-scalar-slice
   :enum-and-tag-widths :not-applicable-bounded-scalar-slice
   :closure-representation :not-applicable-bounded-scalar-slice
   :slice-and-buffer-representation
   :not-applicable-bounded-scalar-slice
   :region-and-arena-handle-representation
   :not-applicable-bounded-scalar-slice
   :linear-resource-handle-representation
   :not-applicable-bounded-scalar-slice
   :ffi-calling-convention :not-applicable-no-ffi
   :abi (:abi b1-record)
   :implementation-defined-behavior-contract-pinned? true
   :toolchain-verification :not-performed-in-gate-a})

(defn p15-s23-b2-c17-runtime-helper-record
  [b1-record]
  {:artifact :gravity/b2-runtime-helper-manifest
   :profile :hosted
   :helpers []
   :gravity-runtime-providers
   (get-in b1-record [:runtime :gravity-runtime-providers])
   :platform-runtime-providers
   (get-in b1-record [:runtime :platform-runtime-providers])
   :provider-contract (:providers b1-record)
   :hidden-runtime-dependence? false
   :gate-b-platform-startup-required? true})

(defn- p15-s23-b2-c17-source-debug-entries
  [operations operation-records]
  (mapv
   (fn [index operation record]
     {:mir-operation-id (:op-id operation)
      :mir-block-id (:block-id operation)
      :source-origin-id (get-in operation [:facts :source-origin-id])
      :source-span (get-in operation [:source :span])
      :generated-origin-chain
      (get-in operation [:source :generated-origin])
      :c-value (:c-value record)
      :generated-declaration-line (+ 5 (* index 2))
      :generated-liveness-line (+ 6 (* index 2))})
   (range) operations operation-records))

(defn- p15-s23-b2-c17-source-debug-map-record
  [b1-record operations operation-records return-id return-value-name]
  {:artifact :gravity/b2-c-source-debug-map
   :status :source-map-preserved-debug-emission-pending-gate-b
   :source-map-id (get-in b1-record [:source-map :id])
   :generated-file "program.c"
   :entries
   (p15-s23-b2-c17-source-debug-entries
    operations operation-records)
   :final-return
   {:mir-operation-id return-id
    :c-value return-value-name
    :generated-return-line (+ 5 (* (count operation-records) 2))}
   :debug-info-emission :not-performed-in-gate-a})

(defn- p15-s23-b2-c17-operation-proof-bindings
  [operations operation-records]
  (mapv
   (fn [operation record]
     {:mir-operation-id (:op-id operation)
      :source-origin-id (get-in operation [:facts :source-origin-id])
      :safety-proof-id (get-in operation [:facts :safety-proof-id])
      :proof-certificate-ids
      (get-in operation [:facts :proof-certificate-ids])
      :c-value (:c-value record)})
   operations operation-records))

(defn- p15-s23-b2-c17-discard-cast-records
  [operations operation-records]
  (mapv
   (fn [operation record]
     {:kind :discarded-value
      :site (:c-liveness-use record)
      :from :int64-t :to :void
      :proof :initialized-scalar-liveness-use
      :safety-proof-id (get-in operation [:facts :safety-proof-id])})
   operations operation-records)))
