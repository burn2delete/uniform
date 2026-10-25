(ns gravity.self-hosting.sh16-source-constant-bool-diamond-comparison-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]
            [gravity.self-hosting.sh16-checked-mir-identity-bridge-test]))

;; SH16/C13/C18: ordinary source composition over frozen C11 development
;; products. Reconstruction comparison grants no optimization or admission.
(defn- existing [namespace name] (var-get (ns-resolve namespace name)))
(defn- producer [name] (existing 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test name))
(defn- invoke [name & arguments] (apply (producer 'invoke) name arguments))
(defn- call [stage name arguments]
  ((existing 'gravity.self-hosting.sh16-checked-mir-identity-bridge-test 'call) stage name arguments))
(defn- path [relative]
  (str (.resolve @(existing 'gravity.self-hosting.sh12-branch-join-mir-test 'root) relative)))
(defn- sha256 [text]
  (str "sha256:" (apply str (map #(format "%02x" (bit-and 255 %))
                                (.digest (java.security.MessageDigest/getInstance "SHA-256")
                                         (.getBytes text java.nio.charset.StandardCharsets/UTF_8))))))
(defn- fixture [condition extension]
  (path (str "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/"
             "zero_arity_bool_" condition "_source." extension)))
(defn- evidence [mir report]
  {:verification-class :original-c11-reconstruction :reconstructed-mir mir :reconstruction-report report})
(defn- compare-mir [mir report]
  (call :c13 'c13-build-bounded-identity-optimized-mir [mir (evidence mir report)]))

(def ^:private originals
  (into {} (for [condition ["true" "false"]]
             [condition (delay
                          (let [pair ((producer 'original-pair) condition "gravity") core (:core pair)
                                reconstruction (invoke 'sh12-build-branch-join-mir core)]
                            (assoc pair :reconstruction reconstruction
                                   :report (invoke 'sh12-verify-branch-join-mir core reconstruction))))])))

(defn- check-original [condition]
  ;; Only .gravity traverses C6/Core/MIR here; paired bytes/parser are below.
  (let [{:keys [raw upstream input core mir reconstruction report]} @(get originals condition)
        function (get-in mir [:functions 'main]) entry (:entry function)
        before (invoke 'sh12-verify-branch-join-mir core mir)
        result (call :c13 'c13-build-bounded-identity-optimized-mir [mir (evidence reconstruction report)])
        output (:optimized-mir result) expected (= condition "true")
        initial (invoke 'sh13-run-branch-join-mir-with-arguments core mir [])
        retained (invoke 'sh13-run-branch-join-mir-with-arguments core output [])
        c6-plan @(existing 'gravity.self-hosting.sh12-branch-join-mir-test 'parameter-c6-plan)
        emitter @(producer 'current-emitter)]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream)))
    (is (= (sha256 (slurp (fixture condition "gravity"))) (:source-content-hash input)))
    (is (= (sha256 (slurp (path "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity")))
           (get-in c6-plan [:source :sha256])
           (get-in core [:semantic-provenance :binding-pins :builder-source-hash])))
    (is (= (sha256 (slurp (path "bootstrap/gravity/p15_s23/emitter.gravity"))) (:source-content-hash emitter)))
    (is (= [true false true] ((juxt :clojure-seed-boundary? :self-hosted? :generic-bridge-residual?) emitter)))
    (is (= :passed (:status before) (:status report)))
    (is (= :single-conditional-branch-join (:scope report)))
    (is (= [:hosted :jvm] ((juxt :profile :target-request) mir)))
    (is (= (:artifact-id core) (:source-core mir)))
    (is (= [] (:params function)))
    (is (= :gravity/bool (:returns function)))
    (is (= 4 (count (:blocks function))))
    (is (= [:constant :truthiness] (mapv :opcode (get-in function [:blocks entry :instructions]))))
    (is (= expected (get-in function [:blocks entry :instructions 0 :constant-payload :value])))
    (is (= :constructed-unverified (:status result)))
    (is (= [:pending :not-admitted :not-established :not-established]
           ((juxt :verification-status :admission-status :freshness-status :semantic-replay-parity) result)))
    (is (= mir reconstruction output))
    (is (= report (:reconstruction-report result)))
    (is (= false (get-in result [:decision-record :changed?])))
    (is (= [] (get-in result [:decision-record :proofs-used])))
    (is (= :not-established (get-in result [:decision-record :verifier-result])))
    (is (every? empty? (vals (:invalidation-ledger result))))
    (is (= (:runtime-check-table mir) (get-in result [:residual-check-report :retained-runtime-checks])))
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core output))))
    (is (false? (call :c14 'c14-c13-envelope-valid? [result])))
    (is (false? (call :c14 'c14-bounded-optimized-input-valid? [result {}])))
    (is (= :accepted (:status initial)))
    (is (= expected (get-in initial [:execution :value])))
    (is (= initial retained))
    (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments core mir [] initial))))
    (is (= :passed (:status (invoke 'sh13-verify-branch-join-execution-with-arguments core output [] retained))))))

(deftest sh16-source-true-constant-bool-diamond-composition (check-original "true"))
(deftest sh16-source-false-constant-bool-diamond-composition (check-original "false"))

(deftest sh16-source-constant-bool-diamond-fresh-reconstruction-rejects-mutants
  (let [{:keys [core mir report]} @(get originals "true")
        function (get-in mir [:functions 'main]) entry (:entry function)
        instructions [:functions 'main :blocks entry :instructions]
        term [:functions 'main :blocks entry :terminator]
        arm (first (get-in mir (conj term :successors)))
        condition-path (conj instructions 0)
        arm-path [:functions 'main :blocks arm :instructions 0]
        condition (get-in mir condition-path) operation (get-in mir arm-path)
        type-id (get-in operation [:facts :type-fact-id]) id (:op-id operation)
        source (assoc-in (:source operation) [:span :start] (get-in operation [:source :span :end]))
        changed-source (-> mir (assoc-in (conj arm-path :source) source)
                           (assoc-in [:source-map id] source)
                           (assoc-in [:data-flow-graph :values (:result operation) :source] source))
        changed-type (-> mir (assoc-in (conj arm-path :type) :gravity/i64)
                         (assoc-in [:type-table type-id :type] :gravity/i64)
                         (assoc-in [:data-flow-graph :values (:result operation) :type] :gravity/i64))
        change-literal (fn [instruction]
                         (let [op (get-in mir instruction) value (not (get-in op [:constant-payload :value]))]
                           (-> mir (assoc-in (conj instruction :constant-payload :value) value)
                               (assoc-in [:data-flow-graph :values (:result op) :constant-payload :value] value))))]
    (is (contains? (:type-table mir) type-id))
    (is (not= (:source operation) source))
    (doseq [candidate [(change-literal condition-path) (change-literal arm-path)
                      (update-in mir term dissoc :operands)
                      (assoc-in mir (conj term :operands) [(:result operation)])
                      changed-source changed-type
                      (update-in mir instructions #(vec (reverse %)))
                      (assoc-in mir [:functions 'main :params] [(:result condition)])]]
      ;; A coherent candidate/reconstruction pair must still carry a FRESH
      ;; original C11 report. No mutation is given a manufactured passed flag.
      (let [fresh (invoke 'sh12-verify-branch-join-mir core candidate)
            result (compare-mir candidate fresh)]
        (is (= :rejected (:status fresh)))
        (is (contains? #{"C11-BLOCK" "C11-TYPE" "C11-ORIGIN" "C11-DOMINANCE" "C11-VERIFY"}
                       (get-in fresh [:diagnostics 0 :diagnostic-id])))
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))
        (is (nil? (:optimized-mir result)))))
    (doseq [candidate [(dissoc report :status)
                      (assoc report :checked-core-artifact-id (str "sha256:" (apply str (repeat 64 "0"))))
                      (update report :checks #(vec (reverse %)))]]
      (let [result (compare-mir mir candidate)]
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))
        (is (nil? (:optimized-mir result)))))))

(deftest sh16-source-constant-bool-diamond-paired-source
  (doseq [condition ["true" "false"]]
    (let [gravity (slurp (fixture condition "gravity")) qst (slurp (fixture condition "qst"))
          forms (fn [source] (binding [*read-eval* false] (read-string (str "[" source "]"))))]
      (is (= gravity qst))
      (is (every? #(< (int %) 128) gravity))
      (is (= (forms gravity) (forms qst))))))
