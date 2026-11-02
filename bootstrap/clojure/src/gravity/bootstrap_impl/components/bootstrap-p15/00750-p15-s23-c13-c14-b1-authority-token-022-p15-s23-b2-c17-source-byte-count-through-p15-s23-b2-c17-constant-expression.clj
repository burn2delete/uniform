(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(def p15-s23-b2-c17-source-byte-count 122488)
(def p15-s23-b2-c17-expected-source-content-hash
  "sha256:63811e04d496ca3e0d49f6d4effac2ff892b7510c02708ff14a1ece014d53217")
(def p15-s23-b2-c17-expected-plan-semantic-hash
  "sha256:2c0420eb7695ec26174a1a4f469e3c17412f76211ceb53f9ac8de796381c9886")
(def p15-s23-b2-c17-expected-functions-semantic-hash
  "sha256:a870a77b0a225d7983304cfc7946a5784f21b1df9abcff6dbb4127487727b63c")
(def p15-s23-b2-c17-expected-builder-semantic-hash
  "sha256:1eb13380d0364d7cbf49c442e4b3ed571153b22fd25ac726d9eb30bccca032e8")

(def p15-s23-b2-c17-required-functions
  {'b2-b1-c-packet-structurally-valid?
   {:arity 1 :params ['packet]}
   'b2-mir-validation
   {:arity 2 :params ['mir 'operation-order]}
   'b2-verifier-mir-cross-links-valid?
   {:arity 3 :params ['packet 'mir 'validation]}
   'b2-operation-reason
   {:arity 4
    :params ['operation 'operations 'operation-index 'block-order]}
   'b2-join-return-chain-valid?
   {:arity 6
    :params ['operation-id 'join-operation 'operations 'operation-index
             'join-id 'remaining]}
   'b2-c-build-manifest-record
   {:arity 1 :params ['dialect-selection]}
   'b2-abi-layout-record
   {:arity 1 :params ['packet]}
   'b2-source-debug-map-record
   {:arity 5
    :params ['packet 'operations 'operation-records
             'return-id 'return-value-name]}
   'b2-proof-to-c-assumption-record
   {:arity 7
    :params ['packet 'mir 'operations 'operation-records 'return-id
             'return-value-name 'semantic-result]}
   'b2-build-accepted-hosted-c17
   {:arity 4 :params ['packet 'mir 'validation 'semantic-result]}
   'b2-build-bounded-hosted-c17
   {:arity 1 :params ['packet]}})

(def p15-s23-b2-c17-raw-artifact-keys
  #{:artifact :schema-version :status :policy :input-bindings
    :backend-manifest :c-build-manifest :source-debug-map
    :dialect-selection :abi-layout-manifest :runtime-helper-manifest
    :proof-to-c-assumption-map :block-order :operation-index
    :operation-records :header-file :source-file :semantic-result
    :expected-exit-code :verified-input-closure :diagnostics
    :clojure-seed-boundary? :whole-b2? :public? :release? :self-hosted?})

(def p15-s23-b2-c17-final-artifact-keys
  (into p15-s23-b2-c17-raw-artifact-keys
        [:source-rule :actual-path-provenance :semantic-id :artifact-id
         :actual-path-binding-id]))

(def p15-s23-b2-c17-policy-record
  {:artifact :gravity/b2-bounded-hosted-c17-policy
   :owner :gravity.backend/b2-c
   :tier :experimental
   :exposure :internal
   :backend-status :partial-bounded-source-emission-slice
   :source-declaration-target :jvm
   :requested-lowering-target :c
   :dialect :c17
   :target-triple "arm64-apple-macosx14.0.0"
   :object-format :mach-o
   :architecture :arm64
   :integer-carrier :i64
   :process-result :i32
   :process-result-range [0 255]
   :maximum-operation-count 128
   :maximum-conditional-count 1
   :maximum-data-flow-edge-count 256
   :runtime-helpers []
   :supported-opcodes
   [:constant :local :local-binding :sequence :lexical-scope
    :function-boundary :truthiness :integer-eq
    :integer-lt :integer-lte :integer-gt :integer-gte
    :conditional-join]
   :whole-b2? false
   :public? false
   :release? false
   :self-hosted? false
   :root-contextual-b1-verification-required? true
   :raw-structural-validator-is-seal-verifier? false})

(def p15-s23-b2-c17-dialect-selection-record
  {:artifact :gravity/b2-c-dialect-selection-record
   :dialect :hosted-c17
   :c-standard :c17
   :hosted? true
   :freestanding? false
   :compiler-family :apple-clang
   :compiler-version-constraint "21.0.0"
   :target-triple "arm64-apple-macosx14.0.0"
   :flags
   ["-std=c17" "-Wall" "-Wextra" "-Werror" "-Wconversion"
    "-Wsign-conversion" "-pedantic"]
   :runtime-helper-policy :none
   :profile :hosted
   :target :c
   :proof-assumptions
   [:signed-i64-carrier
    :all-generated-scalar-expressions-defined
    :final-exit-code-proven-in-0-to-255]
   :gate-b-toolchain-execution-required? true})

(defn p15-s23-b2-c17-build-manifest-record
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

(def p15-s23-b2-c17-header-text
  (str "#ifndef GRAVITY_B2_PROGRAM_H\n"
       "#define GRAVITY_B2_PROGRAM_H\n\n"
       "int main(void);\n\n"
       "#endif\n"))

(def ^:private p15-s23-b2-c17-pinned-source-failure-facts
  #{:exact-regular-pinned-gravity-bridge-source
    :stable-exact-pinned-gravity-bridge-source-size
    :pinned-gravity-bridge-source
    :pinned-gravity-bridge-source-identity
    :pinned-gravity-bridge-function-identity})

(defn- p15-s23-b2-c17-source-binding!
  [candidate source-path]
  (try
    (p15-s23-c13-c14-b1-source-binding!
     candidate source-path
     {:owner :gravity.backend/b2-c-backend
      :relative-path p15-s23-b2-c17-source-relative-path
      :source-byte-count p15-s23-b2-c17-source-byte-count
      :source-content-hash p15-s23-b2-c17-expected-source-content-hash
      :plan-semantic-hash p15-s23-b2-c17-expected-plan-semantic-hash
      :functions-semantic-hash
      p15-s23-b2-c17-expected-functions-semantic-hash
      :builder-semantic-hash
      p15-s23-b2-c17-expected-builder-semantic-hash
      :builder-function p15-s23-b2-c17-builder-function
      :required-functions p15-s23-b2-c17-required-functions
      :emitter-target :c})
    (catch clojure.lang.ExceptionInfo exception
      (let [data (ex-data exception)
            facts (if (map? (:facts data)) (:facts data) {})
            missing-fact
            (or (:missing-fact data) (:missing-fact facts))]
        (if (and (= "B1-INPUT" (or (:id data) (:rule data)))
                 (contains? p15-s23-b2-c17-pinned-source-failure-facts
                            missing-fact))
          (p15-s23-c-backend-fail!
           "B2-MANIFEST" source-path {}
           (merge
            (select-keys
             facts
             [:expected-source-content-hash
              :observed-source-content-hash
              :expected-source-bytes :observed-source-bytes])
            {:missing-fact :pinned-b2-gravity-source-identity
             :bounded-reason missing-fact}))
          (throw exception))))))

(defn p15-s23-b2-c17-forwarded-opcode?
  [opcode]
  (contains? #{:local :local-binding :sequence :lexical-scope
               :function-boundary}
             opcode))

(defn p15-s23-b2-c17-value-name
  [operation-index operation-id]
  (str "gravity_v" (get operation-index operation-id)))

(defn p15-s23-b2-c17-constant-expression
  [value]
  (cond
    (true? value) "INT64_C(1)"
    (false? value) "INT64_C(0)"
    (nil? value) "INT64_C(0)"
    (= Long/MIN_VALUE value) "INT64_MIN"
    (neg? value) (str "(-INT64_C(" (- value) "))")
    :else (str "INT64_C(" value ")"))))
