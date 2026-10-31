(ns gravity.backend.c
  "C17 emission data for caller-validated operations; no backend admission.")

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {abi-layout-record {:arglists ([b1-record])},
  assert-exact!
  {:arglists
   ([c6c10-strict-first-mismatch
     c-backend-fail!
     source-path
     expected
     actual
     missing-fact])},
  build-manifest-record {:arglists ([dialect-selection])},
  constant-expression {:arglists ([value])},
  discard-cast-records {:arglists ([operations operation-records])},
  forwarded-opcode? {:arglists ([opcode])},
  header-text {:kind :constant},
  operation-expression
  {:arglists
   ([value-name
     constant-expression
     forwarded-opcode?
     operation
     operations
     operation-index])},
  operation-proof-bindings
  {:arglists ([operations operation-records])},
  operation-record
  {:arglists
   ([value-name
     operation-expression
     operation
     operations
     operation-index])},
  process-exit-code {:arglists ([value])},
  proof-to-c-assumption-record
  {:arglists
   ([operation-proof-bindings
     discard-cast-records
     b1-record
     mir
     operations
     operation-records
     return-id
     return-value-name
     semantic-result])},
  return-id {:arglists ([function block-order])},
  runtime-helper-record {:arglists ([b1-record])},
  source-debug-entries {:arglists ([operations operation-records])},
  source-debug-map-record
  {:arglists
   ([source-debug-entries
     b1-record
     operations
     operation-records
     return-id
     return-value-name])},
  source-text {:arglists ([operation-records return-value-name])},
  value-name {:arglists ([operation-index operation-id])}},
 :supported
 [:existing-scalar-c17-data-emission
  :stable-source-and-proof-associations
  :pending-toolchain-manifests
  :compatibility-var-interposition],
 :dependency-direction
 {:requires [],
  :consumed-by [gravity.bootstrap],
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts [D1 D2 D3 D6 D8 D9 C1 C15 B1 B2 B13 B14 TEST2],
 :unsupported
 [:untrusted-input-validation
  :b1-c11-admission
  :proof-authentication
  :general-mir-or-domain-lowering
  :gate-a
  :gate-b
  :native-execution
  :toolchain-or-abi-verification
  :full-b2-conformance
  :release
  :migration
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-validated-mir-operations
  :operation-index
  :b1-shaped-manifest-data
  :source-and-proof-associations
  :explicit-compatibility-operations],
 :namespace gravity.backend.c,
 :diagnostic-ownership
 {:stage ["B2-MANIFEST" :exact-data-mismatch],
  :existing-owner
  [:diagnostic-construction-and-publication :input-admission]},
 :artifact-outputs
 [:c17-source-text
  :c-header-text
  :operation-records
  :build-manifest
  :abi-layout-record
  :runtime-helper-record
  :source-debug-map
  :proof-assumption-map
  :discard-cast-records
  :exact-comparison-or-refusal],
 :issues-certificates? false,
 :contract-boundary :native-c17-emission-data}
))

(defn build-manifest-record
  [dialect-selection]
  {:artifact :gravity/b2-c-build-manifest
   :status :pending-gate-b-toolchain-execution
   :compiler-family :apple-clang
   :compiler-version-constraint "21.0.0"
   :target-triple (:target-triple dialect-selection)
   :dialect :c17
   :flags (:flags dialect-selection)
   :source-files ["program.c"]
   :header-files ["program.h"]
   :object-files []
   :executables []
   :external-tool-execution :not-performed-in-gate-a})

(def header-text
  (str "#ifndef GRAVITY_B2_PROGRAM_H\n"
       "#define GRAVITY_B2_PROGRAM_H\n\n"
       "int main(void);\n\n"
       "#endif\n"))

(defn forwarded-opcode?
  [opcode]
  (contains? #{:local :local-binding :sequence :lexical-scope
               :function-boundary}
             opcode))

(defn value-name
  [operation-index operation-id]
  (str "gravity_v" (get operation-index operation-id)))

(defn constant-expression
  [value]
  (cond
    (true? value) "INT64_C(1)"
    (false? value) "INT64_C(0)"
    (nil? value) "INT64_C(0)"
    (= Long/MIN_VALUE value) "INT64_MIN"
    (neg? value) (str "(-INT64_C(" (- value) "))")
    :else (str "INT64_C(" value ")")))(defn operation-expression
  [value-name constant-expression forwarded-opcode? operation operations operation-index]
  (let [opcode (:opcode operation)
        operands (:operands operation)
        name-for #(value-name operation-index %)]
    (cond
      (= :constant opcode)
      (constant-expression
       (get-in operation [:constant-payload :value]))

      (forwarded-opcode? opcode)
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

(defn operation-record
  [value-name operation-expression operation operations operation-index]
  (let [name (value-name
              operation-index (:op-id operation))
        expression
        (operation-expression
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

(defn return-id
  [function block-order]
  (first (get-in function [:blocks (last block-order)
                           :terminator :operands])))

(defn source-text
  [operation-records return-value-name]
  (str "#include <stdint.h>\n"
       "#include \"program.h\"\n\n"
       "int main(void) {\n"
       (apply str (map :c-statement operation-records))
       "  return (int)" return-value-name ";\n"
       "}\n"))

(defn abi-layout-record
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

(defn runtime-helper-record
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

(defn source-debug-entries
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

(defn source-debug-map-record
  [source-debug-entries b1-record operations operation-records return-id return-value-name]
  {:artifact :gravity/b2-c-source-debug-map
   :status :source-map-preserved-debug-emission-pending-gate-b
   :source-map-id (get-in b1-record [:source-map :id])
   :generated-file "program.c"
   :entries
   (source-debug-entries
    operations operation-records)
   :final-return
   {:mir-operation-id return-id
    :c-value return-value-name
    :generated-return-line (+ 5 (* (count operation-records) 2))}
   :debug-info-emission :not-performed-in-gate-a})

(defn operation-proof-bindings
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

(defn discard-cast-records
  [operations operation-records]
  (mapv
   (fn [operation record]
     {:kind :discarded-value
      :site (:c-liveness-use record)
      :from :int64-t :to :void
      :proof :initialized-scalar-liveness-use
      :safety-proof-id (get-in operation [:facts :safety-proof-id])})
   operations operation-records))(defn process-exit-code
  [value]
  (if (boolean? value) (if value 1 0) value))

(defn proof-to-c-assumption-record
  [operation-proof-bindings discard-cast-records b1-record mir operations operation-records return-id
   return-value-name semantic-result]
  (let [certificates (get-in b1-record [:proofs :certificates])
        base-entries
        [{:assumption :signed-i64-carrier
          :evidence :authenticated-mir-scalar-type-and-constant-closure
          :c-site :all-generated-int64-t-initializers
          :outcome :proven-safe}
         {:assumption :explicit-discarded-value-conversions
          :evidence :initialized-generated-scalar-liveness
          :c-site :one-void-conversion-per-generated-local
          :outcome :proven-safe}
         {:assumption :final-exit-code-narrowing
          :evidence :independent-pure-mir-evaluation-in-0-to-255
          :c-site (str "return (int)" return-value-name ";")
          :outcome :proven-safe}]
        equality-entries
        (if (some #(= :integer-eq (:opcode %)) operations)
          (conj
           base-entries
           {:assumption :defined-signed-i64-equality
            :evidence
            :authenticated-two-integer-operands-and-boolean-result
            :c-site :generated-integer-eq-expressions
            :outcome :proven-safe})
          base-entries)
        entries
        (if (some #(contains?
                    #{:integer-lt :integer-lte :integer-gt :integer-gte}
                    (:opcode %))
                  operations)
          (conj
           equality-entries
           {:assumption :defined-signed-i64-ordering
            :evidence
            :authenticated-ordered-two-integer-operands-and-boolean-result
            :c-site :generated-integer-ordering-expressions
            :outcome :proven-safe})
          equality-entries)]
    {:artifact :gravity/b2-proof-to-c-assumption-map
     :proof-certificate-binding
     {:content-id (:content-id certificates)
      :entry-count (:entry-count certificates)}
     :operation-bindings
     (operation-proof-bindings
      operations operation-records)
     :entries entries
     :generated-c-casts
     (conj
      (discard-cast-records
       operations operation-records)
      {:kind :integer-narrowing
       :site (str "return (int)" return-value-name ";")
       :from :int64-t :to :int
       :proof :independent-pure-mir-evaluation-in-0-to-255
       :proof-binding
       {:kind :independent-bounded-mir-evaluation
        :mir-module-id (:module-id mir)
        :return-operation-id return-id
        :semantic-result semantic-result
        :permitted-result-range [0 255]
        :b1-artifact-id (:artifact-id b1-record)
        :proof-table-content-id (:content-id certificates)}})
     :all-casts-proof-authorized? true
     :implicit-undefined-behavior-permitted? false}))

(defn assert-exact!
  [c6c10-strict-first-mismatch c-backend-fail! source-path expected actual missing-fact]
  (when-let [mismatch
             (c6c10-strict-first-mismatch
              source-path expected actual [])]
    (c-backend-fail!
     "B2-MANIFEST" source-path actual
     {:missing-fact missing-fact
      :bounded-reason
      (select-keys mismatch
                   [:path :expected-kind :actual-kind
                    :expected-count :actual-count
                    :missing-key-count :unexpected-key-count])})))


(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
