(ns gravity.backend.llvm.integer-text-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.runtime :as runtime]))

; Current source execution is development evidence, not migration authority.
(def ^:private compiler-plan
  (delay
    (let [source (.getCanonicalPath
                  (io/file "language/compiler/src/gravity/backend/b3_llvm_backend_design.gravity"))
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :c))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-llvm-integer-text-development :compiler-artifact-plan? true}
   @compiler-plan function arguments))

(deftest decimal-text-computes-signed-digits
  (doseq [[value expected]
          [[0 "0"] [1 "1"] [9 "9"] [10 "10"] [11 "11"]
           [99 "99"] [100 "100"] [101 "101"]
           [-1 "-1"] [-9 "-9"] [-10 "-10"] [-11 "-11"]
           [-99 "-99"] [-100 "-100"] [-101 "-101"]
           [Long/MIN_VALUE "-9223372036854775808"]
           [Long/MAX_VALUE "9223372036854775807"]]]
    (is (= expected (invoke 'llvm-integer-text [value])))))

(deftest decimal-text-rejects-nonintegers-without-coercion
  (doseq [value [nil true false "12" :twelve 'twelve \1 1.5 1/2 [] {} #{} '(12)]]
    (let [result (invoke 'llvm-integer-text [value])]
      (is (= :rejected (:status result)))
      (is (= "B3-NUMERIC" (:diagnostic result)))
      (is (= :integer-text-input (:missing-fact result))))))

(deftest decimal-text-rejects-wrong-arity
  (doseq [arguments [[] [1 2]]]
    (is (= "L2-FUNCTION-ARITY"
           (try
             (invoke 'llvm-integer-text arguments)
             :unexpected-success
             (catch clojure.lang.ExceptionInfo error
               (:id (ex-data error))))))))

(deftest value-references-compute-multidigit-ordinals
  (doseq [[ordinal expected] [[0 "%v0"] [9 "%v9"] [10 "%v10"]
                            [99 "%v99"] [100 "%v100"]]]
    (is (= expected (invoke 'b3-value-reference [{"value" ordinal} "value"])))))

; B3/BOOT4: real current-source construction with independent text expectations.
; These synthetic MIR inputs do not claim upstream C11 authentication or LLVM
; verification. No seed reconstruction or historical migration pins are used.
(def ^:private constants
  [[0 "0"] [1 "1"] [-1 "-1"] [9 "9"] [10 "10"] [99 "99"] [100 "100"]
   [-9 "-9"] [-10 "-10"] [-99 "-99"] [-100 "-100"]
   [Long/MIN_VALUE "-9223372036854775808"]
   [Long/MAX_VALUE "9223372036854775807"]
   [true "1"] [false "0"] [nil "0"]])

(def ^:private comparisons
  [[:integer-eq "eq"] [:integer-lt "slt"] [:integer-lte "sle"]
   [:integer-gt "sgt"] [:integer-gte "sge"]])

(defn- operation [id opcode operands type value]
  {:artifact :gravity/mir-operation
   :op-id id :block-id "entry" :opcode opcode
   :source-operation (if (= opcode :constant) :literal opcode)
   :operands operands :result id :result-kind (if (= type :gravity/nil) :unit :value)
   :type type :effects #{} :capabilities #{} :ordering :none :profile :hosted
   :constant-payload (if (= opcode :constant) {:present? true :value value} :not-applicable)
   :source {:span {:source "integer-text.gravity" :start {:line 1 :column 1}
                   :end {:line 1 :column 2}}
            :generated-origin [{:kind :fixture :origin-id id}]}
   :facts {:type-fact-id (str "type-" id) :effect-fact-id (str "effect-" id)
           :capability-fact-id nil :capability-proof-ids [] :ownership-fact-id nil
           :safety-outcome-id :proven-safe :safety-proof-id (str "proof-" id)
           :profile-target-fact-id nil :runtime-check-id nil :proof-certificate-ids []
           :failure-behavior :none :source-origin-id (str "source-" id)}
   :domain-anchor nil :verifier-status :passed})

(defn- fixture-operations [constant-cases]
  (into (mapv (fn [i [value _]]
                (operation (str "c" i) :constant []
                           (cond (boolean? value) :gravity/bool
                                 (nil? value) :gravity/nil
                                 :else :gravity/integer)
                           value))
              (range) constant-cases)
        (map-indexed (fn [i [opcode _]]
                       (operation (str "p" i) opcode ["c0" "c1"] :gravity/bool nil))
                     comparisons)))

(defn- fixture [operations]
  (let [definitions (into {} (map (fn [{:keys [op-id block-id]}]
                                    [op-id {:value-id op-id :operation-id op-id
                                            :block-id block-id}]) operations))
        values (into {} (map (fn [op]
                              [(:op-id op)
                               {:artifact :gravity/mir-value :value-id (:op-id op)
                                :kind :instruction-result :type (:type op)
                                :constant-payload (:constant-payload op)
                                :defined-by (get definitions (:op-id op)) :uses []
                                :source (:source op) :ownership {} :effects #{}
                                :safety {}}]) operations))
        function {:returns :gravity/bool
                  :blocks {"entry" {:block-id "entry" :instructions operations
                                    :terminator {:terminator-id "entry:return"
                                                 :kind :return :operands ["p4"]
                                                 :successors []}}}}
        mir {:module-id "integer-text-module" :functions {'main function}
             :source-core {:id "integer-text-core"}
             :pass-execution-record {:record-id "integer-text-pass"}
             :type-table (into {} (map (juxt :op-id :type) operations))
             :effect-table (into {} (map (juxt :op-id :effects) operations))
             :safety-table (into {} (map (juxt :op-id :facts) operations))
             :source-map (into {} (map (juxt :op-id :source) operations))
             :control-flow-graph {}
             :data-flow-graph
             {:definitions definitions :values values :edges []
              :terminator-uses [{:value-id "p4" :consumer-kind :terminator
                                 :consumer-id "entry:return" :consumer-block "entry"
                                 :operand-index 0 :edge-kind :return-value}]}}]
    [mir function]))

(defn- expected-records [operations constant-cases offset]
  (mapv (fn [i op]
          (let [ordinal (+ offset i)
                value (str "%v" ordinal)
                instruction
                (if (< i (count constant-cases))
                  (str "  " value " = add i64 0, " (second (nth constant-cases i)))
                  (str "  %cmp" ordinal " = icmp "
                       (second (nth comparisons (- i (count constant-cases))))
                       " i64 %v" offset ", %v" (inc offset) "\n"
                       "  " value " = zext i1 %cmp" ordinal " to i64"))]
            {:mir-operation-id (:op-id op) :mir-block-id "entry"
             :source-operation (:source-operation op) :mir-opcode (:opcode op)
             :mir-type (:type op) :llvm-type :i64 :llvm-value value
             :llvm-instruction instruction}))
        (range) operations))

(defn- expected-ir [records]
  (str "; Gravity authenticated B3 bounded LLVM\n"
       "source_filename = \"gravity-b3-internal\"\n"
       "target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128\"\n"
       "target triple = \"x86_64-unknown-linux-gnu\"\n\n"
       "define ccc i32 @main() {\nentry:\n"
       (str/join "\n" (map :llvm-instruction records)) "\n"
       "  %exit = trunc i64 " (:llvm-value (last records)) " to i32\n"
       "  ret i32 %exit\n}\n"))

(deftest real-builder-computes-text-with-seed-numeric-formatting-denied
  (is (= #{:memory/allocate} (get-in @compiler-plan [:module :effects])))
  (is (= #{} (get-in @compiler-plan [:module :capabilities])))
  (let [hosted-str runtime/hosted-str
        calls (atom [])
        string-only (fn [arguments]
                      (when-not (every? string? arguments)
                        (throw (ex-info "numeric formatting denied" {})))
                      (swap! calls conj arguments)
                      (hosted-str arguments))
        emitted
        (with-redefs [runtime/hosted-str string-only]
          (is (thrown-with-msg? clojure.lang.ExceptionInfo #"numeric formatting denied"
                               (runtime/hosted-str ["value=" 10])))
          (mapv
           (fn [[constant-cases offset]]
             (let [operations (fixture-operations constant-cases)
                   [mir function] (fixture operations)
                   index (invoke 'b3-index-operations [operations offset {}])
                   records (expected-records operations constant-cases offset)
                   result (invoke 'b3-build-emitted-lowering
                                  [mir function ["entry"] operations index {"entry" "entry"}])]
               (is (= :constructed-unverified (:status result)))
               (is (= records (:operation-records result)))
               (is (= (expected-ir records) (:llvm-ir result)))
               (is (= [{:mir-block-id "entry" :llvm-label "entry"
                        :operation-lines (mapv :llvm-instruction records)
                        :terminator-lines [(str "  %exit = trunc i64 "
                                                (:llvm-value (last records)) " to i32")
                                           "  ret i32 %exit"]}]
                      (:block-records result)))
               (is (= index (:operation-index result)))
               (is (= false (:semantic-result result)))
               (is (= 0 (:expected-exit-code result)))
               (is (= :llvm-x86_64-linux (:target result)))
               (is (= :pending-host-content-binding
                      (get-in result [:verified-input-closure :status])))
               (is (= (:source-core mir) (get-in result [:verified-input-closure :source-core])))
               (is (= "integer-text-pass"
                      (get-in result [:verified-input-closure :pass-execution-record-id])))
               (doseq [field [:type-fact-count :effect-fact-count :safety-fact-count :source-map-count]]
                 (is (= (count operations) (get-in result [:verified-input-closure field]))))
               (is (true? (:clojure-seed-boundary? result)))
               (doseq [field [:self-hosted? :public? :release? :whole-b3?]]
                 (is (false? (get result field))))
               (is (= [] (:emitted-metadata result) (:emitted-function-attributes result)
                      (:ub-sensitive-flags result)))
               (when (= offset 0)
                 (is (= result (invoke 'b3-build-validated-operations
                                       [mir function ["entry"] operations]))))
               result))
           [[constants 0] [(assoc constants 4 [11 "11"]) 0] [constants 90]]))]
    (is (seq @calls) "The actual seed str operation must be intercepted")
    (is (some #(> (count %) 3) @calls) "Also exercise variadic String concatenation")
    (is (not= (:llvm-ir (first emitted)) (:llvm-ir (second emitted))))
    (is (not= (:llvm-ir (first emitted)) (:llvm-ir (last emitted))))))

(deftest validated-builder-retains-scalar-and-operation-rejections
  (let [operations (fixture-operations constants)]
    (doseq [[position update-operation diagnostic reason]
            [[0 #(assoc-in % [:constant-payload :value] (inc (bigint Long/MAX_VALUE)))
              "B1-UNSUPPORTED" :bounded-scalar-constant-payload]
             [0 #(assoc-in % [:constant-payload :value] (dec (bigint Long/MIN_VALUE)))
              "B1-UNSUPPORTED" :bounded-scalar-constant-payload]
             [0 #(assoc-in % [:constant-payload :value] "0")
              "B1-UNSUPPORTED" :bounded-scalar-constant-payload]
             [0 #(assoc % :operands ["c1"])
              "B1-UNSUPPORTED" :constant-must-have-zero-operands]
             [16 #(assoc % :operands ["missing" "c1"])
              "B1-UNSUPPORTED" :integer-comparison-left-definition]
             [16 #(assoc % :operands ["c0"])
              "B1-UNSUPPORTED" :integer-comparison-requires-two-operands]
             [16 #(assoc % :operands ["c0" "c13"])
              "B1-UNSUPPORTED" :integer-comparison-right-operand-type]
             [16 #(assoc % :type :gravity/integer)
              "B1-UNSUPPORTED" :integer-comparison-result-type]
             [16 #(assoc % :operands "c0")
              "B1-INPUT" :operation-operands-vector]
             [16 #(assoc % :opcode :unsupported)
              "B1-UNSUPPORTED" :source-operation-opcode-parity]
             [0 #(assoc % :effects #{:io/write})
              "B1-INPUT" :operation-effects-must-be-empty-set]
             [0 #(assoc % :capabilities #{:io/stdout})
              "B1-INPUT" :operation-capabilities-must-be-empty-set]]]
      (let [changed (update operations position update-operation)
            [mir function] (fixture changed)
            result (invoke 'b3-build-validated-operations [mir function ["entry"] changed])]
        (is (= :rejected (:status result)))
        (is (= diagnostic (:diagnostic result)))
        (is (= reason (:missing-fact result)))
        (is (nil? (:llvm-ir result)))))))
