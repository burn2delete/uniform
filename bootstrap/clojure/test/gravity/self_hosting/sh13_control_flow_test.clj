(ns gravity.self-hosting.sh13-control-flow-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh13_control_flow_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-13 test source is not on the classpath"
                {:id "SH13-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH13-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-13")

(defn- fixture-relative-path
  [family basename extension]
  (str fixture-root "/" family "/" basename extension))

(defn- compile-plan
  [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan
  (delay
   (compile-plan
    "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity")))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "control-flow-modules" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "accepted" "control-flow-modules" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-control-flow-modules" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-relative-path
     "rejected" "invalid-control-flow-modules" ".qst"))))

(defn- invoke
  [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh13-control-flow-leaf
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine
  [function arguments]
  (invoke engine-plan function arguments))

(defn- module
  [plan function]
  (invoke plan function []))

(defn- run
  [module arguments]
  (invoke-engine 'sh13-run-module [module arguments]))

(def ^:private rejected-cases
  {'sh13-unverified-module ["C11-MODULE" :module]
   'sh13-duplicate-function-module ["C11-VERIFY" :verify]
   'sh13-missing-terminator-module ["C11-BLOCK" :block]
   'sh13-unknown-callee-module ["C11-VERIFY" :verify]
   'sh13-call-arity-module ["C11-TYPE" :type]
   'sh13-call-type-module ["C11-TYPE" :type]
   'sh13-invalid-lt-module ["C11-TYPE" :type]
   'sh13-unmarked-recursion-module ["C11-VERIFY" :verify]
   'sh13-indirect-allowlist-module ["C11-VERIFY" :verify]
   'sh13-missing-effect-order-module ["C11-EFFECT" :effect]
   'sh13-throw-effect-module ["C11-EFFECT" :effect]
   'sh13-missing-branch-target-module ["C11-BLOCK" :block]
   'sh13-branch-type-module ["C11-TYPE" :type]
   'sh13-use-before-definition-module ["C11-DOMINANCE" :dominance]
   'sh13-missing-origin-module ["C11-ORIGIN" :origin]
   'sh13-missing-lineage-module ["C11-MODULE" :module]
   'sh13-mismatched-verifier-module ["C11-VERIFY" :verify]
   'sh13-duplicate-operation-module ["C11-DOMINANCE" :dominance]
   'sh13-unreachable-block-module ["C11-DOMINANCE" :dominance]
   'sh13-operation-profile-module ["C11-MODULE" :module]
   'sh13-operation-fact-module ["C11-VERIFY" :verify]
   'sh13-pure-ordering-module ["C11-EFFECT" :effect]
   'sh13-mutual-recursion-module ["C11-VERIFY" :verify]})

(deftest sh13-engine-and-co-canonical-fixtures-compile-as-gravity
  (doseq [plan [engine-plan accepted-gravity-plan accepted-qst-plan
                rejected-gravity-plan rejected-qst-plan]]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))
  (doseq [function
          '[sh13-control-flow-policy
            sh13-run-module
            sh13-verify-execution]]
    (is (map? (get-in @engine-plan [:functions function])) function))
  (let [policy (invoke-engine 'sh13-control-flow-policy [])]
    (is (= :gravity/sh13-control-flow-policy (:artifact policy)))
    (is (= 1 (:version policy)))
    (is (= 32 (:maximum-functions policy)))
    (is (= 32 (:maximum-blocks-per-function policy)))
    (is (= 64 (:maximum-operations-per-block policy)))
    (is (= 512 (:maximum-execution-steps policy)))
    (is (contains? (:operation-set policy) :call))
    (is (contains? (:terminator-set policy) :conditional-branch))
    (is (contains? (:diagnostics policy) "C11-DOMINANCE"))
    (is (true? (:clojure-seed-boundary? policy)))
    (is (false? (:self-hosted? policy)))
    (is (some #{:authenticated-sh12-mir-input} (:pending policy)))
    (is (some #{:seedless-runtime-execution} (:pending policy))))
  (doseq [[family basename]
          [["accepted" "control-flow-modules"]
           ["rejected" "invalid-control-flow-modules"]]]
    (is (= (slurp
            (path (fixture-relative-path family basename ".gravity")))
           (slurp
            (path (fixture-relative-path family basename ".qst")))))))

(deftest sh13-executes-conditional-branches-and-joins
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [module (module plan 'sh13-branch-module)
          when-true (run module [true])
          when-false (run module [false])]
      (is (= :accepted (:status when-true)))
      (is (= :returned (get-in when-true [:execution :status])))
      (is (= 42 (get-in when-true [:execution :value])))
      (is (= 7 (get-in when-false [:execution :value])))
      (is (= :returned
             (get-in when-true
                     [:control-flow :structured-exit])))
      (is (true? (:clojure-seed-boundary? when-true)))
      (is (false? (:self-hosted? when-true)))
      (is (pos? (get-in when-true [:control-flow :steps-used])))
      (is (some #(= :conditional-branch (:terminator %))
                (get-in when-true [:execution :trace])))
      (is (= :passed
             (:status
              (invoke-engine
               'sh13-verify-execution
               [module [true] when-true])))))))

(deftest sh13-executes-direct-recursion-with-an-explicit-depth-bound
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [module (module plan 'sh13-factorial-module)
          result (run module [5])
          depth-rejection (run module [20])]
      (is (= :accepted (:status result)))
      (is (= :returned (get-in result [:execution :status])))
      (is (= 120 (get-in result [:execution :value])))
      (is (= 6
             (count
              (filter
               #(= :enter (:event %))
               (get-in result [:execution :trace])))))
      (is (= :rejected (:status depth-rejection)))
      (is (= "C11-VERIFY"
             (get-in depth-rejection [:diagnostics 0 :rule])))
      (is (= :call-depth-bound
             (get-in depth-rejection [:execution :reason]))))))

(deftest sh13-executes-loop-backedges-and-bounded-indirect-calls
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [loop-module (module plan 'sh13-loop-module)
          indirect-module (module plan 'sh13-indirect-module)
          loop-result (run loop-module [4])
          indirect-result (run indirect-module [41])]
      (is (= :accepted (:status loop-result)))
      (is (= 10 (get-in loop-result [:execution :value])))
      (is (= :accepted (:status indirect-result)))
      (is (= 42 (get-in indirect-result [:execution :value])))
      (is (= [:choose :factorial :sum-down :add-one :indirect-main
              :error-main :throw-main :panic-main]
             (:functions (:call-graph indirect-result))))
      (is (true? (get-in indirect-result
                         [:call-graph :bounded-call-depth]))))))

(deftest sh13-preserves-distinct-structured-exits
  (doseq [[function expected]
          [['sh13-error-module :error]
           ['sh13-throw-module :thrown]
           ['sh13-panic-module :panic]]]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [result (run (module plan function) [:fixture/reason])]
        (is (= :accepted (:status result)))
        (is (= expected (get-in result [:execution :status])))
        (is (= expected
               (get-in result [:control-flow :structured-exit])))
        (is (= :fixture/reason
               (get-in result [:execution :value])))))))

(deftest sh13-keeps-actual-path-provenance-outside-identity
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [first-module (module plan 'sh13-branch-module)
          second-module
          (module plan 'sh13-branch-alternate-path-module)
          first-result (run first-module [true])
          second-result (run second-module [true])]
      (is (not= (:actual-source-path first-module)
                (:actual-source-path second-module)))
      (is (= (:identity-input first-result)
             (:identity-input second-result)))
      (is (= :sh13/checked-core
             (get-in first-result [:identity-input :checked-core-id])))
      (is (= :sh13/safety-table
             (get-in first-result [:identity-input :safety-table-id])))
      (is (not=
           (get-in first-result [:provenance :actual-source-path])
           (get-in second-result [:provenance :actual-source-path])))
      (is (= (:execution first-result) (:execution second-result))))))

(deftest sh13-rejects-invalid-mir-before-host-execution
  (doseq [[accepted-plan rejected-plan]
          [[accepted-gravity-plan rejected-gravity-plan]
           [accepted-qst-plan rejected-qst-plan]]]
    (let [base (module accepted-plan 'sh13-factorial-module)]
      (doseq [[function [rule reason]] rejected-cases]
        (testing (str function)
          (let [invalid (invoke rejected-plan function [base])
                result (run invalid [5])]
            (is (= :rejected (:status result)))
            (is (= rule (get-in result [:diagnostics 0 :rule])))
            (is (= reason
                   (get-in result [:diagnostics 0 :reason])))
            (is (nil? (:execution result)))))))))

(deftest sh13-fails-closed-on-runtime-bounds-and-argument-types
  (doseq [[accepted-plan rejected-plan]
          [[accepted-gravity-plan rejected-gravity-plan]
           [accepted-qst-plan rejected-qst-plan]]]
    (let [base (module accepted-plan 'sh13-factorial-module)
          low-fuel (invoke rejected-plan 'sh13-low-fuel-module [base])
          fuel-result (run low-fuel [5])
          type-result (run base [true])]
      (is (= :rejected (:status fuel-result)))
      (is (= "C11-VERIFY"
             (get-in fuel-result [:diagnostics 0 :rule])))
      (is (= :execution-step-bound
             (get-in fuel-result [:execution :reason])))
      (is (= :rejected (:status type-result)))
      (is (= "C11-TYPE"
             (get-in type-result [:diagnostics 0 :rule])))
      (is (nil? (:execution type-result))))))

(deftest sh13-rejects-i64-input-and-arithmetic-overflow
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [module (module plan 'sh13-indirect-module)
          input-rejection (run module [9223372036854775808])
          overflow-rejection (run module [9223372036854775807])]
      (is (= :rejected (:status input-rejection)))
      (is (= "C11-TYPE"
             (get-in input-rejection [:diagnostics 0 :rule])))
      (is (= :rejected (:status overflow-rejection)))
      (is (= :integer-range
             (get-in overflow-rejection [:execution :reason])))
      (is (= "C11-VERIFY"
             (get-in overflow-rejection [:diagnostics 0 :rule]))))))

(deftest sh13-verifier-recomputes-and-rejects-substitution
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [module (module plan 'sh13-loop-module)
          result (run module [4])
          substituted
          (assoc-in result [:execution :value] 11)]
      (is (= :passed
             (:status
              (invoke-engine
               'sh13-verify-execution
               [module [4] result]))))
      (is (= :rejected
             (:status
              (invoke-engine
               'sh13-verify-execution
               [module [4] substituted]))))
      (is (= "C11-VERIFY"
             (get-in
              (invoke-engine
               'sh13-verify-execution
               [module [4] substituted])
              [:diagnostics 0 :rule]))))))

(deftest sh13-branch-transfers-require-vector-carriers
  ;; Original Gravity fixture constructors supply normalized MIR, not fresh
  ;; authenticated SH12 products. Host exceptions are observed, never accepted.
  (let [observe (fn [f]
                  (try {:value (f)}
                       (catch Throwable error
                         {:host-error (.getName (class error))})))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (module plan 'sh13-branch-module)
            entry-path [:functions 0 :blocks 0 :terminator]
            branch-path [:functions 0 :blocks 1 :terminator]
            check-rejected
            (fn [malformed rule]
              (let [observed (observe #(run malformed [true]))
                    result (:value observed)
                    verification
                    (observe #(invoke-engine 'sh13-verify-execution
                                             [malformed [true] result]))]
                (is (nil? (:host-error observed)))
                (is (= :rejected (:status result)))
                (is (= rule (get-in result [:diagnostics 0 :rule])))
                (is (nil? (:execution result)))
                (is (nil? (:host-error verification)))
                (is (= :passed (get-in verification [:value :status])))))]
        (doseq [[subject args expected]
                [[base [true] 42] [base [false] 7]
                 [(module plan 'sh13-loop-module) [4] 10]
                 [(module plan 'sh13-indirect-module) [41] 42]]]
          (let [result (run subject args)]
            (is (= :accepted (:status result)))
            (is (= expected (get-in result [:execution :value])))
            (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                  [subject args result]))))))
        (doseq [arguments [nil 7 {0 :choose/forty-two}
                           (list :choose/forty-two) "x"]]
          (check-rejected (assoc-in base (conj branch-path :arguments) arguments)
                          "C11-TYPE"))
        ;; Check both arms statically, even when runtime would select the other.
        (doseq [field [:then-arguments :else-arguments]
                arguments [nil 7 {} '() ""]]
          (check-rejected (assoc-in base (conj entry-path field) arguments)
                          "C11-TYPE"))
        (doseq [field [:then-arguments :else-arguments]]
          (check-rejected (update-in base entry-path dissoc field) "C11-TYPE"))
        (doseq [[malformed rule]
                [[(-> base
                      (assoc-in (conj branch-path :target) :missing)
                      (assoc-in (conj branch-path :arguments) 7)) "C11-BLOCK"]
                 [(-> base
                      (assoc-in (conj entry-path :then-target) :missing)
                      (assoc-in (conj entry-path :else-arguments) 7)) "C11-BLOCK"]
                 [(-> base
                      (assoc-in (conj entry-path :then-arguments) [:missing])
                      (assoc-in (conj entry-path :else-target) :missing)) "C11-TYPE"]
                 [(-> base
                      (assoc-in (conj entry-path :then-target) :choose/join)
                      (assoc-in (conj entry-path :then-arguments) [:missing])
                      (assoc-in (conj entry-path :else-arguments) 7)) "C11-DOMINANCE"]
                 [(assoc-in base (conj branch-path :arguments) [:missing :extra]) "C11-TYPE"]
                 [(assoc-in base (conj branch-path :arguments) [:missing]) "C11-DOMINANCE"]]]
          (check-rejected malformed rule))
        (let [malformed (assoc-in base (conj entry-path :else-arguments) nil)
              result (:value (observe #(run malformed [true])))
              substituted (assoc result :status :accepted)]
          (is (= :rejected (:status (invoke-engine 'sh13-verify-execution
                                                  [malformed [true] substituted])))))))))

(deftest sh13-rejects-malformed-modules-before-entry-lookup
  ;; Original normalized constructors/runtime; not authenticated SH12 ingress.
  (let [observe (fn [f]
                  (try {:value (f)}
                       (catch Throwable error
                         {:host-error (.getName (class error))})))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [base (module plan 'sh13-branch-module)
            check-rejected
            (fn [subject arguments]
              (let [expected (invoke-engine 'sh13-diagnostic [subject :module])
                    observed (observe #(run subject arguments))
                    result (:value observed)
                    verification
                    (observe #(invoke-engine 'sh13-verify-execution
                                             [subject arguments result]))]
                (is (nil? (:host-error observed)))
                (is (= :rejected (:status result)))
                (is (= [expected] (:diagnostics result)))
                (is (nil? (:execution result)))
                (is (nil? (:host-error verification)))
                (is (= :passed (get-in verification [:value :status])))))]
        (doseq [functions [nil 7 true :not-functions "functions"
                           {} {0 (get-in base [:functions 0])}
                           '() (list (get-in base [:functions 0])) #{}]]
          (check-rejected (assoc base :functions functions) [true]))
        (check-rejected (dissoc base :functions) [true])
        ;; Earlier module failures still win, including over malformed arguments.
        (check-rejected (assoc base :artifact :wrong :functions 7) 7)
        (check-rejected (assoc base :profile :wrong :functions 7) nil)
        (doseq [[subject arguments expected]
                [[base [true] 42] [base [false] 7]
                 [(module plan 'sh13-loop-module) [4] 10]
                 [(module plan 'sh13-indirect-module) [41] 42]]]
          (let [result (run subject arguments)]
            (is (= :accepted (:status result)))
            (is (= expected (get-in result [:execution :value])))
            (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                  [subject arguments result]))))))
        (let [subject (assoc base :functions nil)
              result (run subject [true])]
          (is (= :rejected
                 (:status (invoke-engine 'sh13-verify-execution
                                         [subject [true]
                                          (assoc result :status :accepted)])))))))))

(deftest sh13-constants-have-no-value-operands
  ;; Normalized fixture MIR through the original executor and recompute gate.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (module plan 'sh13-factorial-module)
          operation-path [:functions 1 :blocks 0 :operations 0]
          accepted (run base [5])
          check-rejected
          (fn [subject rule]
            (let [result (run subject [5])]
              (is (= :rejected (:status result)))
              (is (= rule (get-in result [:diagnostics 0 :rule])))
              (is (nil? (:execution result)))
              (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                    [subject [5] result]))))))]
      (is (= [] (get-in base (conj operation-path :operands))))
      (is (= :accepted (:status accepted)))
      (is (= 120 (get-in accepted [:execution :value])))
      (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                            [base [5] accepted]))))
      (doseq [operands [[:n] [:n :n]]]
        (check-rejected (assoc-in base (conj operation-path :operands) operands)
                        "C11-TYPE"))
      ;; Existing checks still take precedence over the constant arity check.
      (check-rejected (assoc-in base (conj operation-path :operands) [:missing])
                      "C11-DOMINANCE")
      (let [nonzero (assoc-in base (conj operation-path :operands) [:n])]
        (check-rejected (assoc-in nonzero (conj operation-path :profile) :wrong)
                        "C11-MODULE")
        (check-rejected (assoc-in nonzero (conj operation-path :facts :type) :wrong)
                        "C11-VERIFY")
        (check-rejected (assoc-in nonzero (conj operation-path :effects) #{:io/write})
                        "C11-EFFECT")
        (check-rejected (assoc-in nonzero (conj operation-path :operands) nil)
                        "C11-ORIGIN")
        (is (= :rejected (:status (invoke-engine 'sh13-verify-execution
                                                [nonzero [5] accepted]))))))))

(deftest sh13-return-values-must-be-defined-before-type-checking
  ;; Supplied normalized MIR, through original G/Q constructors and execution.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (module plan 'sh13-branch-module)
          exit-path [:functions 0 :blocks 3 :terminator]
          check-rejected
          (fn [subject rule]
            (let [result (run subject [true])]
              (is (= :rejected (:status result)))
              (is (= rule (get-in result [:diagnostics 0 :rule])))
              (is (nil? (:execution result)))
              (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                    [subject [true] result]))))))]
      (doseq [[subject arguments expected]
              [[base [true] 42]
               [(module plan 'sh13-factorial-module) [5] 120]]]
        (let [result (run subject arguments)]
          (is (= :accepted (:status result)))
          (is (= expected (get-in result [:execution :value])))
          (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                [subject arguments result]))))))
      (doseq [opcode [:return :error-return :throw :panic]]
        (let [subject (assoc-in base (conj exit-path :opcode) opcode)]
          (check-rejected (assoc-in subject (conj exit-path :value) :missing)
                          "C11-DOMINANCE")
          (check-rejected (update-in subject exit-path dissoc :value)
                          "C11-DOMINANCE")))
      (check-rejected (assoc-in base [:functions 0 :return-type] :bool)
                      "C11-TYPE")
      (let [subject (assoc-in base (conj exit-path :value) :missing)]
        (check-rejected (assoc-in subject (conj exit-path :source-span) nil)
                        "C11-ORIGIN")
        (check-rejected (assoc-in subject (conj exit-path :origin-chain) [])
                        "C11-ORIGIN")
        (is (= :rejected
               (:status (invoke-engine 'sh13-verify-execution
                                       [subject [true] (run base [true])]))))))))

(deftest sh13-callee-parameter-carriers-are-validated-before-counting
  ;; A genuine normalized fixture call targets a later function record.
  (let [observe (fn [f]
                  (try {:value (f)}
                       (catch Throwable error
                         {:host-error (.getName (class error))})))]
    (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
      (let [original (module plan 'sh13-factorial-module)
            call (invoke plan 'sh13-call
                         [:factorial/pre-call :add-one [:n] :i64 :direct #{} 30 31])
            operations-path [:functions 1 :blocks 0 :operations]
            call-path (conj operations-path 0)
            callee-path [:functions 3 :params]
            base (update-in original operations-path #(into [call] %))
            check-rejected
            (fn [subject rule]
              (let [observed (observe #(run subject [5])) result (:value observed)
                    verification
                    (observe #(invoke-engine 'sh13-verify-execution
                                             [subject [5] result]))]
                (is (nil? (:host-error observed)))
                (is (= :rejected (:status result)))
                (is (= rule (get-in result [:diagnostics 0 :rule])))
                (is (nil? (:execution result)))
                (is (nil? (:host-error verification)))
                (is (= :passed (get-in verification [:value :status])))))
            result (run base [5])]
        (is (= :accepted (:status result)))
        (is (= 120 (get-in result [:execution :value])))
        (is (some #(= :factorial/pre-call (:operation %))
                  (get-in result [:execution :trace])))
        (is (some #(and (= :enter (:event %)) (= :add-one (:function %)))
                  (get-in result [:execution :trace])))
        (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                              [base [5] result]))))
        (doseq [params [7 true :bad nil {} {0 :x} '(:x) ":x" #{:x}]]
          (check-rejected (assoc-in base callee-path params) "C11-MODULE"))
        (let [malformed (assoc-in base callee-path 7)]
          (doseq [[subject rule]
                  [[(assoc-in malformed (conj call-path :callee) :missing) "C11-VERIFY"]
                   [(assoc-in malformed (conj call-path :dispatch) :wrong) "C11-VERIFY"]
                   [(assoc-in base (conj call-path :operands) []) "C11-TYPE"]
                   [(assoc-in base (conj call-path :type) :bool) "C11-TYPE"]
                   [(assoc-in malformed (conj call-path :operands) [:missing]) "C11-DOMINANCE"]
                   [(assoc-in malformed (conj call-path :profile) :wrong) "C11-MODULE"]
                   [(assoc-in malformed (conj call-path :facts :type) :wrong) "C11-VERIFY"]
                   [(assoc-in malformed (conj call-path :effects) #{:error/throw}) "C11-EFFECT"]]]
            (check-rejected subject rule))
          (is (= :rejected (:status (invoke-engine 'sh13-verify-execution
                                                  [malformed [5] result])))))))))

(deftest sh13-normalized-bool-values-exclude-nil-and-nonbooleans
  ;; Supplied normalized MIR executes the original public run/recompute APIs;
  ;; no checked-Core provenance or new bridge execution is implied here.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (module plan 'sh13-branch-module)
          booleans (-> base
                       (assoc-in [:functions 0 :return-type] :bool)
                       (assoc-in [:functions 0 :blocks 1 :operations 0 :type] :bool)
                       (assoc-in [:functions 0 :blocks 1 :operations 0 :literal] true)
                       (assoc-in [:functions 0 :blocks 2 :operations 0 :type] :bool)
                       (assoc-in [:functions 0 :blocks 2 :operations 0 :literal] false)
                       (assoc-in [:functions 0 :blocks 3 :param-types] [:bool]))
          good (run booleans [true])]
      (letfn [(reject [subject arguments rule]
                (let [result (run subject arguments)]
                  (is (= :rejected (:status result)))
                  (is (= rule (get-in result [:diagnostics 0 :rule])))
                  (is (nil? (:execution result)))
                  (is (= (:source-span subject)
                         (get-in result [:diagnostics 0 :source-span])))
                  (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                        [subject arguments result]))))
                  (is (= :rejected (:status (invoke-engine 'sh13-verify-execution
                                                          [subject arguments good]))))))]
        (doseq [[argument expected] [[true 42] [false 7]]]
          (let [result (run base [argument])]
            (is (= :accepted (:status result)))
            (is (= expected (get-in result [:execution :value])))
            (is (= :returned (get-in result [:execution :status])))))
        (doseq [argument [true false]]
          (let [result (run booleans [argument])]
            (is (= :accepted (:status result)))
            (is (= argument (get-in result [:execution :value])))
            (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                  [booleans [argument] result]))))))
        (doseq [value [nil 0 :false "false" [] {} '()]]
          (reject base [value] "C11-TYPE")
          (doseq [block [1 2] condition [true false]]
            ;; The unselected arm is validated as well as the selected one.
            (reject (assoc-in booleans
                              [:functions 0 :blocks block :operations 0 :literal]
                              value)
                    [condition] "C11-TYPE")))
        (let [path [:functions 0 :blocks 1 :operations 0]
              bad (assoc-in booleans (conj path :literal) nil)]
          (doseq [[subject rule]
                  [[(assoc bad :artifact :wrong) "C11-MODULE"]
                   [(assoc-in bad (conj path :profile) :wrong) "C11-MODULE"]
                   [(assoc-in bad (into path [:facts :type]) :wrong) "C11-VERIFY"]
                   [(assoc-in bad (conj path :origin-chain) []) "C11-ORIGIN"]
                   [(assoc-in bad (conj path :effects) #{:error/throw}) "C11-EFFECT"]
                   [(assoc-in bad (conj path :operands) [:missing]) "C11-DOMINANCE"]]]
            (reject subject [nil] rule)))
        (is (= :rejected (:status (invoke-engine 'sh13-verify-execution
                                                [booleans [true]
                                                 (assoc-in good [:execution :value] nil)])))))))
  ;; Direct NEW regression controls expose the old hashing seam without
  ;; hashing the fixture object during construction or claiming source proof.
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks conj :equals)
                   (throw (ex-info "opaque equality invoked" {})))
                 (hashCode [] (swap! callbacks conj :hash)
                   (throw (ex-info "opaque hash invoked" {}))))]
    (doseq [value [opaque [opaque] {:value opaque}]]
      (is (false? (try (invoke-engine 'sh13-value-type-valid? [value :bool])
                       (catch Exception _ ::callback-threw))))
      (is (empty? @callbacks))
      (reset! callbacks [])
      (is (false? (try (invoke-engine 'sh13-literal-type-valid?
                                     [{:opcode :const :type :bool :literal value}])
                       (catch Exception _ ::callback-threw))))
      (is (empty? @callbacks))
      (reset! callbacks []))))

(deftest sh13-conditional-values-must-be-defined-before-type-checking
  ;; Supplied normalized MIR from original G/Q constructors, not fresh C10.
  (doseq [plan [accepted-gravity-plan accepted-qst-plan]]
    (let [base (module plan 'sh13-branch-module)
          path [:functions 0 :blocks 0 :terminator]
          operation-path [:functions 0 :blocks 0 :operations]
          number (invoke plan 'sh13-constant [:choose/number 1 :i64 0 1])
          with-number (assoc-in base operation-path [number])
          accepted (run base [true])
          check-rejected
          (fn [subject rule]
            (let [result (run subject [true])]
              (is (= :rejected (:status result)))
              (is (= rule (get-in result [:diagnostics 0 :rule])))
              (is (nil? (:execution result)))
              (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                    [subject [true] result]))))))]
      (doseq [[subject arguments expected]
              [[base [true] 42] [base [false] 7]
               [(module plan 'sh13-factorial-module) [5] 120]]]
        (let [result (run subject arguments)]
          (is (= :accepted (:status result)))
          (is (= expected (get-in result [:execution :value])))
          (is (= :passed (:status (invoke-engine 'sh13-verify-execution
                                                [subject arguments result]))))))
      (doseq [condition [:missing nil :choose/forty-two]]
        (check-rejected (assoc-in base (conj path :condition) condition)
                        "C11-DOMINANCE"))
      (check-rejected (update-in base path dissoc :condition) "C11-DOMINANCE")
      (check-rejected (assoc-in with-number (conj path :condition) :choose/number)
                      "C11-TYPE")
      ;; The condition's definition/type precedes either target's validation.
      (let [bad-target (assoc-in base (conj path :then-target) :missing-target)]
        (check-rejected (assoc-in bad-target (conj path :condition) :missing)
                        "C11-DOMINANCE")
        (check-rejected bad-target "C11-BLOCK")
        (check-rejected (-> with-number
                            (assoc-in (conj path :condition) :choose/number)
                            (assoc-in (conj path :then-target) :missing-target))
                        "C11-TYPE"))
      ;; Existing source/opcode and whole-block operation checks still win.
      (let [missing (assoc-in base (conj path :condition) :missing)]
        (check-rejected (assoc-in missing (conj path :source-span) nil)
                        "C11-ORIGIN")
        (check-rejected (assoc-in missing (conj path :origin-chain) [])
                        "C11-ORIGIN")
        (check-rejected (assoc-in missing (conj path :opcode) :unsupported)
                        "C11-BLOCK")
        (check-rejected (assoc-in missing operation-path [(assoc number :profile :wrong)])
                        "C11-MODULE")
        (check-rejected (assoc-in missing operation-path [(assoc number :source-span nil)])
                        "C11-ORIGIN")
        (is (= :rejected (:status (invoke-engine 'sh13-verify-execution
                                                [missing [true] accepted]))))))))
