(ns gravity.optimization.pipeline-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as b]
            [gravity.optimization :as optimization]
            [gravity.self-hosting.sh17-c13-optimized-mir-carrier-test]))

(defn- helper [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh17-c13-optimized-mir-carrier-test name)))

(defn- invoke [mir evidence]
  (b/p15-s23-stage2-runtime-execute-function
    {:engine :gravity-optimization-reconstruction-development :compiler-artifact-plan? true}
    @(helper 'development-c13-plan) 'c13-build-bounded-identity-optimized-mir [mir evidence]))

(defn- input [] ((helper 'development-comparison-input) 2 3 false))

(deftest contract-declares-the-data-and-reconstruction-boundary
  (is (empty? (ns-aliases 'gravity.optimization)))
  (is (= #{'selection 'rewrite 'restore 'output-id 'extend-evidence 'translated-record}
         (set (keys (ns-publics 'gravity.optimization)))))
  (doseq [[name operation] (ns-publics 'gravity.optimization)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.optimization (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:safety-check-elision} (:unsupported contract))))))

(deftest current-gravity-program-emits-the-same-fold-and-refuses-output-drift
  ;; Actual current Gravity execution on supplied MIR-shaped DATA. Its passed
  ;; fact labels and /development path are fixture data, not genuine C11 input.
  (doseq [[left right repeated? expected] [[2 2 false true] [2 3 false false]
                                          [Long/MIN_VALUE Long/MAX_VALUE false false]
                                          [Long/MAX_VALUE 0 true true]]]
    (let [{:keys [mir evidence]} ((helper 'development-comparison-input) left right repeated?)
          selected (optimization/selection mir)
          output (optimization/rewrite mir selected)
          actual (invoke mir evidence)
          bad-evidence (assoc evidence :output-mir-id "sha256:changed-output")
          rejection (invoke mir bad-evidence)]
      (is (= :accepted (:status actual)))
      (is (= actual (b/p15-s23-c13-expected-record mir evidence)))
      (is (= output (:optimized-mir actual)))
      (is (= mir (optimization/restore output (:delta selected))))
      (is (= selected (gravity.bootstrap-c13-comparison/selection mir)))
      (is (= {:present? true :value expected}
             (get-in output [:data-flow-graph :values "comparison" :constant-payload])))
      (is (= (:source-map mir) (:source-map output)))
      (is (= (:runtime-check-table mir) (:runtime-check-table output)))
      (is (= (gravity.bootstrap-c13-comparison/output-id output)
             (b/p15-s23-c11-mir-digest
               {:domain :gravity/c13-equality-fold-output-v1
                :mir (b/p15-s23-c11-mir-path-neutral-value output)})))
      (is (= :rejected (:status rejection)))
      (is (= ["C13-VERIFY"] (:diagnostics rejection)))
      (is (= rejection (b/p15-s23-c13-expected-record mir bad-evidence))))))

(deftest invalid-premises-and-reconstruction-indices-remain-rejected
  (let [{:keys [mir]} (input)
        entry (get-in mir [:control-flow-graph :entry])
        path [:functions 'main :blocks entry :instructions]
        selected (optimization/selection mir)
        output (optimization/rewrite mir selected)
        delta (:delta selected)]
    (doseq [changed [(assoc-in mir (conj path 2 :effects) #{:io/write})
                     (assoc-in mir [:source-map "comparison" :origin-id] "changed")
                     (assoc-in mir [:type-table "comparison:type" :type] :gravity/integer)
                     (assoc-in mir (conj path 0 :constant-payload :value) (inc (bigint Long/MAX_VALUE)))
                     (assoc-in mir (conj path 2 :operands) ["left"])
                     (assoc-in mir [:functions 'main :params] ["argument"])]]
      (is (nil? (optimization/selection changed))))
    (doseq [changed [(assoc delta :unexpected true) (assoc delta :edges [])
                     (assoc-in delta [:edges 0 :index] -1)
                     (assoc-in delta [:edges 1 :index] 65536)
                     (assoc-in delta [:edges 1 :index] (get-in delta [:edges 0 :index]))
                     (assoc-in delta [:uses 0 :index] 65536)
                     (assoc-in delta [:uses 0 :index] "0")
                     (update delta :uses #(vec (reverse %)))]]
      (is (nil? (optimization/restore output changed))))))

(defn- relabel [value from to]
  (walk/postwalk #(if (string? %) (str/replace % from to) %) value))

(defn- add-unused-constants [mir]
  ;; A counterexample to the removed 256-operation/definition/value quotas.
  ;; This supplied data does not claim authenticated MIR or general C13 support.
  (let [entry (get-in mir [:control-flow-graph :entry])
        path [:functions 'main :blocks entry :instructions]
        [left right comparison] (get-in mir path)
        seed {:operation left :value (assoc (get-in mir [:data-flow-graph :values "left"]) :uses [])
              :definition (get-in mir [:data-flow-graph :definitions "left"])
              :source (get-in mir [:source-map "left"])
              :type (get-in mir [:type-table "left:type"])
              :effect (get-in mir [:effect-table "left:effect"])
              :ownership (get-in mir [:ownership-table "left:ownership"])
              :capability (get-in mir [:capability-table "left:capability"])
              :safety (get-in mir [:safety-table "left:safety"])}
        additions (mapv #(relabel seed "left" (str "unused-" %)) (range 257))]
    (reduce (fn [result {:keys [operation value definition source type effect ownership capability safety]}]
              (let [id (:op-id operation)]
                (-> result
                    (assoc-in [:data-flow-graph :values id] value)
                    (assoc-in [:data-flow-graph :definitions id] definition)
                    (assoc-in [:source-map id] source)
                    (assoc-in [:type-table (str id ":type")] type)
                    (assoc-in [:effect-table (str id ":effect")] effect)
                    (assoc-in [:ownership-table (str id ":ownership")] ownership)
                    (assoc-in [:capability-table (str id ":capability")] capability)
                    (assoc-in [:safety-table (str id ":safety")] safety))))
            (assoc-in mir path (into [left right] (concat (map :operation additions) [comparison]))) additions)))

(deftest formerly-quota-rejected-data-retains-exact-reconstruction
  (let [{:keys [mir]} (input)
        long-identifier (apply str (repeat 300 "a"))]
    (doseq [candidate [(relabel mir "left" long-identifier) (add-unused-constants mir)]]
      (let [selected (optimization/selection candidate)
            output (optimization/rewrite candidate selected)]
        (is (some? selected))
        (is (= candidate (optimization/restore output (:delta selected))))
        (is (= (:source-map candidate) (:source-map output)))))))

(deftest compatibility-ports-preserve-late-var-interposition
  (let [original-extend gravity.bootstrap-c13-comparison/extend-evidence
        original-record gravity.bootstrap-c13-comparison/translated-record
        base {:unchanged :base}]
    (with-redefs [gravity.bootstrap-c13-comparison/selection (constantly nil)]
      (is (identical? base (original-extend :input base)))
      (is (identical? base (original-record :input :evidence base)))))
  (let [seen (atom nil)]
    (with-redefs [b/p15-s23-c11-mir-path-neutral-value (constantly :neutral-port)
                  b/p15-s23-c11-mir-digest (fn [input] (reset! seen input) :digest-port)]
      (is (= :digest-port (gravity.bootstrap-c13-comparison/output-id :input)))
      (is (= {:domain :gravity/c13-equality-fold-output-v1 :mir :neutral-port} @seen)))))
