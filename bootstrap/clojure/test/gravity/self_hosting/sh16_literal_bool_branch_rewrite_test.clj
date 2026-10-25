(ns gravity.self-hosting.sh16-literal-bool-branch-rewrite-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh16-source-constant-bool-diamond-comparison-test]))

;; Independent bounded test oracles, not a public MIR verifier or runtime.
(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh16-source-constant-bool-diamond-comparison-test name)))
(defn- invoke [name & arguments] (apply (existing 'invoke) name arguments))
(defn- call [stage name arguments] ((existing 'call) stage name arguments))
(defn- evidence [mir report] ((existing 'evidence) mir report))
(defn- original [condition] @(get (existing 'originals) condition))
(defn- rewrite [mir report]
  (call :c13 'sh16-c13-rewrite-literal-bool-branch [mir (evidence mir report)]))
(defn- paths [blocks current target visited]
  (cond
    (contains? visited current) []
    (= current target) [[current]]
    :else (mapcat (fn [successor]
                    (map #(into [current] %) (paths blocks successor target (conj visited current))))
                  (get-in blocks [current :terminator :successors]))))
(defn- cfg-valid? [mir]
  (let [function (get-in mir [:functions 'main]) blocks (:blocks function)
        pairs (for [[id block] blocks successor (get-in block [:terminator :successors])] [id successor])
        recorded (map (juxt :from :to) (get-in mir [:control-flow-graph :edges]))]
    (and (= (count pairs) (count recorded) (count (set pairs)))
         (= (set pairs) (set recorded))
         (every? (fn [[id block]]
                   (let [routes (paths blocks (:entry function) id #{})
                         dominance (if (seq routes) (apply set/intersection (map set routes)) #{id})]
                     (and (= (set (:successors block)) (set (get-in block [:terminator :successors])))
                          (= (set (:predecessors block)) (set (map first (filter #(= id (second %)) pairs))))
                          (= dominance (set (:dominators block)))
                          (= dominance (set (get-in mir [:control-flow-graph :dominance id])))))) blocks))))
(defn- known [values id]
  (assert (contains? values id) (str "undefined test-oracle operand " id))
  (get values id))
(defn- walk [mir]
  (let [function (get-in mir [:functions 'main]) blocks (:blocks function)]
    (loop [id (:entry function) values {} trace []]
      (assert (< (count trace) 4))
      (let [block (get blocks id)
            values (reduce (fn [values operation]
                             (let [operands (:operands operation)
                                   value (case (:opcode operation)
                                           :constant (get-in operation [:constant-payload :value])
                                           :truthiness (not (contains? #{nil false} (known values (first operands))))
                                           :conditional-join (known values (get operands (if (true? (known values (first operands))) 1 2)))
                                           :function-boundary (known values (first operands)))]
                               (assoc values (:result operation) value))) values (:instructions block))
            terminator (:terminator block) successors (:successors terminator) trace (conj trace id)]
        (case (:kind terminator)
          :return {:value (known values (first (:operands terminator))) :blocks trace}
          :branch (do (assert (= 1 (count successors))) (recur (first successors) values trace))
          :conditional-branch (recur (get successors (if (true? (known values (first (:operands terminator)))) 0 1)) values trace))))))

(defn- check-original [condition]
  (let [{:keys [raw upstream core mir report]} (original condition)
        result (rewrite mir report) output (:candidate-mir result)
        before (invoke 'sh13-run-branch-join-mir-with-arguments core mir [])
        function (get-in mir [:functions 'main]) entry (:entry function) blocks (:blocks function)
        [then-id else-id] (get-in blocks [entry :terminator :successors])
        selected (if (= condition "true") then-id else-id) other (if (= selected then-id) else-id then-id)
        output-function (get-in output [:functions 'main]) output-blocks (:blocks output-function)
        term-id (get-in blocks [entry :terminator :terminator-id])
        condition-id (get-in blocks [entry :terminator :operands 0])
        original-data (:data-flow-graph mir) output-data (:data-flow-graph output)
        unchanged-top #(dissoc % :functions :control-flow-graph :data-flow-graph)]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream) (:status report)))
    (is (= :constructed-unverified (:status result)))
    (is (= [:pending :not-admitted] ((juxt :verification-status :admission-status) result)))
    (is (= result (rewrite mir report)))
    (is (not= mir output))
    (is (= (unchanged-top mir) (unchanged-top output)))
    (is (= (dissoc function :blocks) (dissoc output-function :blocks)))
    (is (= 4 (count blocks) (count output-blocks)))
    (doseq [[id block] blocks]
      (is (= (:instructions block) (get-in output-blocks [id :instructions])))
      (is (= (:source block) (get-in output-blocks [id :source]))))
    (is (= selected (:selected-block result)))
    (is (= :branch (get-in output-blocks [entry :terminator :kind])))
    (is (= [] (get-in output-blocks [entry :terminator :operands])))
    (is (= [selected] (get-in output-blocks [entry :terminator :successors])))
    (is (= [] (get-in output-blocks [other :predecessors])))
    (is (= term-id (get-in output-blocks [entry :terminator :terminator-id])))
    (is (= (get-in blocks [entry :terminator :source]) (get-in output-blocks [entry :terminator :source])))
    (is (cfg-valid? output))
    (is (= (:edges original-data) (:edges output-data)))
    (is (= (:definitions original-data) (:definitions output-data)))
    (is (= (dissoc (:values original-data) condition-id) (dissoc (:values output-data) condition-id)))
    (is (= (vec (remove #(= term-id (:consumer-id %)) (:terminator-uses original-data)))
           (:terminator-uses output-data)))
    (is (= (vec (remove #(= term-id (:consumer-id %)) (get-in original-data [:values condition-id :uses])))
           (get-in output-data [:values condition-id :uses])))
    (is (= :accepted (:status before)))
    (is (= (get-in before [:execution :value]) (:value (walk mir)) (:value (walk output)) (= condition "true")))
    (is (= [entry selected (get-in mir [:control-flow-graph :join :block-id])] (:blocks (walk output))))
    (is (= :pending (get-in result [:decision-record :verifier-result])))
    (is (= [term-id] (get-in result [:decision-record :changed-terminators])))
    (is (= [:control-flow-facts :dominance-facts :terminator-use-facts]
           (get-in result [:invalidation-ledger :facts-regenerated])))
    (is (= (:runtime-check-table mir) (:runtime-check-table output)))
    (is (= :rejected (:status (invoke 'sh12-verify-branch-join-mir core output))))
    (is (= :rejected (:status (call :c13 'c13-build-bounded-identity-optimized-mir [output (evidence mir report)]))))
    (is (false? (call :c14 'c14-c13-envelope-valid? [result])))
    (is (false? (call :c14 'c14-bounded-optimized-input-valid? [result {}])))))

(deftest sh16-private-true-literal-bool-branch-rewrite (check-original "true"))
(deftest sh16-private-false-literal-bool-branch-rewrite (check-original "false"))

(deftest sh16-private-literal-bool-branch-counterexamples
  (let [{:keys [core mir report]} (original "true") result (rewrite mir report) output (:candidate-mir result)
        function (get-in output [:functions 'main]) entry (:entry function)
        [then-id else-id] (get-in mir [:functions 'main :blocks entry :terminator :successors])
        changed [:functions 'main :blocks entry :terminator :successors]
        wrong (assoc-in output changed [else-id])]
    ;; The independent test oracle exposes choosing the untaken arm; this is
    ;; a counterexample for the forthcoming shared validation, not its success.
    (is (true? (:value (walk output))))
    (is (false? (:value (walk wrong))))
    (is (false? (cfg-valid? wrong)))
    (doseq [candidate [(assoc output :control-flow-graph (:control-flow-graph mir))
                      (assoc-in output [:functions 'main :blocks else-id :predecessors] [entry])
                      (assoc-in output [:control-flow-graph :dominance then-id] [then-id])]]
      (is (false? (cfg-valid? candidate))))
    (doseq [candidate [(assoc mir :target-request :llvm-x86_64-linux)
                      (assoc-in mir [:functions 'main :params] ["invented"])
                      (assoc-in mir [:functions 'main :blocks entry :instructions 0 :constant-payload :value] nil)
                      (assoc-in mir [:functions 'main :blocks entry :instructions 0 :effects] #{:error/raise})
                      (assoc-in mir [:functions 'main :blocks entry :terminator :operands] [])
                      (assoc-in mir [:control-flow-graph :edges] [])]]
      (let [fresh (invoke 'sh12-verify-branch-join-mir core candidate) rejected (rewrite candidate fresh)]
        (is (= :rejected (:status fresh)))
        (is (= :rejected (:status rejected)))
        (is (= ["C13-VERIFY"] (:diagnostics rejected)))
        (is (nil? (:candidate-mir rejected)))))
    (doseq [bad-report [(dissoc report :status) (assoc report :checked-core-artifact-id "substituted")]]
      (is (= :rejected (:status (rewrite mir bad-report)))))
    (is (= mir (:mir (original "true"))))))
