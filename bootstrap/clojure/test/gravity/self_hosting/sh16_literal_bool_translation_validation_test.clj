(ns gravity.self-hosting.sh16-literal-bool-translation-validation-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh16-literal-bool-branch-rewrite-test]))

(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh16-literal-bool-branch-rewrite-test name)))
(defn- invoke [name & arguments] (apply (existing 'invoke) name arguments))
(defn- call [stage name arguments] ((existing 'call) stage name arguments))
(defn- evidence [mir report] ((existing 'evidence) mir report))
(defn- original [condition] ((existing 'original) condition))
(defn- rewrite [mir report] ((existing 'rewrite) mir report))
(defn- verify-specialization [core before after]
  (invoke 'sh12-verify-literal-bool-branch-specialization core before after))
(defn- consume [before input-report after output-report]
  (call :c13 'sh16-c13-build-literal-bool-translation-validation
        [before (evidence before input-report) after output-report]))

(def ^:private cases
  (into {} (for [condition ["true" "false"]]
             [condition (delay
                          (let [{:keys [core mir report] :as pair} (original condition)
                                kernel (rewrite mir report) after (:candidate-mir kernel)]
                            (assoc pair :kernel kernel :after after
                                   :output-report (verify-specialization core mir after))))])))

(defn- check-original [condition]
  ;; Fresh C11 validation owns the complete Core/before/after relation. The
  ;; returned scoped report does not grant public source or target admission.
  (let [{:keys [raw upstream core mir report kernel after output-report]} @(get cases condition)
        result (consume mir report after output-report)]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream) (:status report) (:status output-report)))
    (is (= {:status :passed} (:input-carrier-validation core)))
    (is (not (contains? core :bounds)))
    (is (= output-report (verify-specialization core mir after)))
    (is (not= mir after))
    (is (= :constructed-unverified (:status result)))
    (is (= :passed (:translation-validation-status result)))
    (is (= [:pending :not-admitted] ((juxt :verification-status :admission-status) result)))
    (is (= [true false] ((juxt :clojure-seed-boundary? :self-hosted?) result)))
    (is (every? (set (:nonclaims result)) [:optimizer-credit :admission-credit :public-admission]))
    (is (= after (:candidate-mir result)))
    (is (= output-report (:post-pass-report result)))
    (is (= 1 (get-in result [:pass-contract :version])))
    (is (= :sh16-literal-bool-branch-specialization (get-in result [:pass-contract :pass-id])))
    (is (= :passed (get-in result [:decision-record :verifier-result])))
    (is (= :literal-bool-branch-specialization (get-in result [:decision-record :verifier-scope])))
    (is (= (:invalidation-ledger kernel) (:invalidation-ledger result)))
    (is (= result (consume mir report after output-report)))
    (is (= (:source-core mir) (:artifact-id core) (:source-core after)))
    (is (= (:runtime-check-table mir) (:runtime-check-table after)))
    (is (= :rejected (:status (invoke 'sh12-verify-branch-join-mir core after))))
    (is (false? (call :c14 'c14-c13-envelope-valid? [result])))
    (is (false? (call :c14 'c14-bounded-optimized-input-valid? [result {}])))))

(deftest sh16-validates-original-true-bool-branch-specialization (check-original "true"))
(deftest sh16-validates-original-false-bool-branch-specialization (check-original "false"))

(deftest sh16-literal-bool-report-requires-ordered-semantic-checks
  (let [{:keys [mir report after output-report]} @(get cases "true")
        checks (:checks output-report)]
    (is (= [:input-carrier-kinds :fresh-original-mir-verification
            :original-zero-arity-hosted-jvm-bool-diamond
            :literal-selected-edge :cfg-and-dominance-reconstruction
            :terminator-and-value-use-reconstruction
            :complete-after-equality :type-effect-safety-origin-preservation
            :unchanged-instructions-joins-fact-check-and-proof-tables]
           checks))
    (is (= :passed (:translation-validation-status
                     (consume mir report after output-report))))
    (doseq [candidate (concat
                      (map (fn [index]
                             (into (subvec checks 0 index) (subvec checks (inc index))))
                           (range (count checks)))
                      (map #(assoc checks % :unknown-semantic-check) (range (count checks)))
                      (map #(assoc checks % (get checks (mod (inc %) (count checks))))
                           (range (count checks)))
                      [(vec (reverse checks))
                       (conj checks :unknown-semantic-check)
                       (assoc checks 0 :bounded-input-carriers)])]
      (let [result (consume mir report after (assoc output-report :checks candidate))]
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))))))

(deftest sh16-literal-bool-validation-rejects-substituted-source-tuples
  (let [true-case @(get cases "true") false-case @(get cases "false")
        {:keys [core mir report after output-report]} true-case]
    (doseq [[core before output] [[(:core false-case) mir after]
                                 [core (:mir false-case) after]
                                 [core mir (:after false-case)]]]
      (let [fresh (verify-specialization core before output)]
        (is (= :rejected (:status fresh)))
        (is (= :rejected (:status (consume before report output fresh))))))
    (doseq [[before input-report output stale]
            [[mir report after (:output-report false-case)]
             [(:mir false-case) (:report false-case) (:after false-case) output-report]
             [mir report (:after false-case) output-report]]]
      (let [result (consume before input-report output stale)]
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))
        (is (nil? (:candidate-mir result)))))
    (is (= mir (:mir (original "true"))))))

(deftest sh16-literal-bool-validation-rejects-corrupted-output-and-report
  (let [{:keys [core mir report after output-report]} @(get cases "true")
        function (get-in after [:functions 'main]) entry (:entry function)
        selected (get-in after [:functions 'main :blocks entry :terminator :successors 0])
        condition (get-in mir [:functions 'main :blocks entry :terminator :operands 0])]
    (doseq [candidate [(assoc after :control-flow-graph (:control-flow-graph mir))
                      (assoc after :data-flow-graph (:data-flow-graph mir))
                      (assoc-in after [:functions 'main :blocks entry :terminator :successors] [])
                      (assoc-in after [:functions 'main :blocks selected :instructions 0 :constant-payload :value] false)
                      (assoc-in after [:control-flow-graph :dominance selected] [selected])
                      (assoc-in after [:data-flow-graph :values condition :uses] [])]]
      (let [fresh (verify-specialization core mir candidate)
            result (consume mir report candidate fresh)]
        (is (= :rejected (:status fresh)))
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))
        (is (nil? (:candidate-mir result)))))
    (doseq [bad-report (concat (map #(dissoc output-report %) (keys output-report))
                              [(assoc output-report :status :rejected)
                               (assoc output-report :injected true)])]
      (let [result (consume mir report after bad-report)]
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))
        (is (nil? (:candidate-mir result)))))
    (doseq [malformed [nil [] {} 0 false "not-mir"]
            [before output] [[malformed after] [mir malformed]]]
      (let [result (consume before report output output-report)]
        (is (= :rejected (:status result)))
        (is (= ["C13-VERIFY"] (:diagnostics result)))
        (is (nil? (:candidate-mir result)))))))

(defn- check-qst-original [condition]
  (let [pair ((var-get (ns-resolve 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'original-pair))
              condition "qst")
        {:keys [raw upstream core mir]} pair
        report (invoke 'sh12-verify-branch-join-mir core mir)
        after (:candidate-mir (rewrite mir report))
        output-report (verify-specialization core mir after)
        result (consume mir report after output-report)]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream) (:status report) (:status output-report)))
    (is (= [:hosted :jvm] ((juxt :profile :target-request) mir)))
    (is (= [] (get-in mir [:functions 'main :params])))
    (is (= :gravity/bool (get-in mir [:functions 'main :returns])))
    (is (= :passed (:translation-validation-status result)))
    (is (= [:pending :not-admitted] ((juxt :verification-status :admission-status) result)))
    (is (= after (:candidate-mir result)))
    (is (= (:artifact-id core) (:source-core mir) (:source-core after)
           (get-in output-report [:input-bindings :checked-core-artifact-id])))
    (is (= output-report (:post-pass-report result)))
    (is (false? (call :c14 'c14-c13-envelope-valid? [result])))
    (is (false? (call :c14 'c14-bounded-optimized-input-valid? [result {}])))))

(deftest sh16-validates-original-true-bool-branch-specialization-qst (check-qst-original "true"))
(deftest sh16-validates-original-false-bool-branch-specialization-qst (check-qst-original "false"))
