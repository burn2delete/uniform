(ns gravity.self-hosting.sh09-private-self-effect-test
  "Focused private-self value and declared-effect semantics.

  These six kernels retain the original SH-09 assertions and tamper negatives,
  but depend only on shared fixture support plus the normalized C7 fixture
  provider.  The large SH-08/SH-09 test namespaces are not loaded by this
  namespace.  The six historical selector names are now discovered in this
  namespace; the old SH-09 namespace keeps only compatibility aliases for
  existing cross-suite callers."
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- leaf
  [function & args]
  (apply kernel-support/leaf function args))

(defn- sha
  [n]
  (kernel-support/sha n))

(def ^:private monolithic-namespaces
  #{'gravity.self-hosting.sh08-function-call-type-test
    'gravity.self-hosting.sh08-recursive-function-type-test
    'gravity.self-hosting.sh09-body-effect-test})

(deftest focused-private-self-load-isolation-development-check
  (let [loaded-before (set (map ns-name (all-ns)))
        {:keys [completed? exit output] :as probe} (kernel-support/fresh-sh09-load-probe)
        loaded-after (set (map ns-name (all-ns)))
        monoliths-loaded (fn [loaded]
                           (set (filter #(contains? loaded %) monolithic-namespaces)))]
    (is (= (monoliths-loaded loaded-before)
           (monoliths-loaded loaded-after))
        "The child probe must not mutate the parent's namespace set")
    (is completed? (pr-str probe))
    (is (= 0 exit) (pr-str probe))
    (is (str/includes? output "SH09-FOCUSED-LOAD-ISOLATION") (pr-str probe))))

(deftest current-gravity-private-three-parameter-source-context-development-kernel
  (let [{:keys [core facts bindings forms proof]} (kernel-support/private-self-value-kernel-data :prior-three)
        context (leaf 'body-is-context core facts bindings forms)]
    (is (= :sh08-private-self-value-v1 (:rule-version proof)))

    (is (= :accepted (:status context)) (pr-str context))
    (is (= context (leaf 'body-is-context core facts bindings forms)))))

(deftest current-gravity-indexed-private-source-context-development-kernel
  (let [{:keys [core facts bindings forms nodes proof]} (kernel-support/private-self-value-kernel-data :indexed)
        context (leaf 'body-is-context core facts bindings forms)
        working (assoc core :sh08-private-self-parameter-premise (:parameter-value-premise context))
        request {:canonical-core-artifact working :b47-context {:authenticated-core-request {:forms forms}}}
        successor (leaf 'body-si-context request facts bindings)
        getter (leaf 'body-mg-context request facts bindings)
        equality (leaf 'body-ae-context request facts bindings)
        counted (leaf 'body-cq-context request facts bindings)
        checked (assoc facts :successor-context (:context successor) :map-get-context (:context getter)
                             :equality-context (:context equality) :count-context (:context counted))
        call #(first (filter (fn [c] (= % (:core-node-id c))) (:calls core)))
        edge #(first (filter (fn [e] (= % (:call-core-node-id e))) (:call-edges core)))]
    (is (= :accepted (:status proof)))
    (is (= :accepted (:status context)) (pr-str context))
    (is (= :gravity/sh08-indexed-private-self-entry-premise-v1 (get-in context [:parameter-value-premise :domain])))
    (is (= context (leaf 'body-is-context core facts bindings forms)))
    (is (= [:accepted :accepted :accepted :accepted]
           (mapv :status [successor getter equality counted])))
    (is (true? (leaf 'body-si-call-valid? working checked bindings (nodes :ix-step) (call :ix-step) (edge :ix-step) nodes)))
    (is (true? (leaf 'body-mg-call-valid? working checked bindings (nodes :ix-get) (call :ix-get) (edge :ix-get) nodes)))
    (is (true? (leaf 'body-call-joins-with-foreign-valid? working checked bindings {}))
        (pr-str
         (let [functions (into {} (map (juxt :function-id identity) (:function-type-table checked)))
               records (into {} (map (juxt :function-syntax-id identity) (:function-records core)))
               facts-by-id (into {} (map (juxt :call-core-node-id identity) (:call-type-facts checked)))]
           {:calls (mapv (fn [e]
                           (let [id (:call-core-node-id e) c (call id) f (facts-by-id id)
                                 local? (= :local-function (:classification e))
                                 validator (case (:dispatch f)
                                             :direct-collection-count 'body-cq-call-valid?
                                             :direct-integral-equality 'body-ie-call-valid?
                                             :direct-atomic-equality 'body-ae-call-valid?
                                             :direct-vector-get 'body-mg-call-valid?
                                             :direct-bounded-unit-addition 'body-si-call-valid?
                                             nil)]
                             {:id id :dispatch (:dispatch f)
                              :valid (if local?
                                       [(leaf 'body-call-valid? e c f nodes functions records bindings (:type-table checked))
                                        (leaf 'body-local-arguments-valid? working checked bindings e c f
                                              (functions (:callee-function-syntax-id e)))]
                                       (when validator (leaf validator working checked bindings (nodes id) c e nodes)))}))
                         (:call-edges core))
            :ledger (mapv #(select-keys % [:kind :producer-rule :lhs]) (:constraint-ledger checked))})))
    (doseq [id [:ix-step :ix-get]]
      (let [producer (if (= id :ix-step) :sh08-indexed-private-self-unit-addition-v1 :sh08-vector-get-indexed-private-self-v1)
            row (first (filter #(= producer (:producer-rule %)) (:constraint-ledger checked)))
            missing (update checked :constraint-ledger #(vec (remove (fn [r] (= row r)) %)))
            duplicate (update checked :constraint-ledger conj row)
            orphan (update checked :constraint-ledger conj (assoc-in row [:lhs :core-node-id] :orphan))]
        (is (map? row))
        (doseq [changed [missing duplicate orphan]]
          (is (false? (leaf 'body-call-joins-with-foreign-valid? working changed bindings {}))))))
    (doseq [changed [(assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :normal-entry-count] 0)
                     (assoc-in facts [:recursive-proof :parameter-value-premise :source-induction :vector-extent] 8)
                     (assoc-in facts [:recursive-proof :return-witness :witnesses 0 :node-id] :ix-true)]]
      (is (= :rejected (:status (leaf 'body-is-context core changed bindings forms)))))))

(deftest current-gravity-private-self-effects-development-kernel
  (let [{:keys [core facts bindings forms proof data]} (kernel-support/private-self-value-kernel-data :throw)
        ids (:node-id-map data)
        self (ids :self-call) body (ids :recursive-body)
        _ (leaf 'body-is-context core facts bindings forms)
        derived (leaf 'body-derive-recursive-nodes core facts {})
        graph (:nodes derived)
        checked (leaf 'body-tc-verify core facts bindings graph {:active? true})]
    (is (= :accepted (:status proof)))


    (is (= :effects-and-escaping-error-types (:summary-domain derived)))
    (is (> (:fixed-point-rounds derived) 1))
    (is (= #{:error/throw} (get-in graph [self :transitive])))
    (is (= [:gravity.type/string] (get-in graph [self :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [body :escaping-thrown-error-types])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status checked)))
    (is (= {:helper-form [:gravity.type/string]} (:functions checked)))
    (is (= 3 (count (:rows checked))))
    (is (every? #(= :sh09-private-self-error-equation-v1 (:producer-rule %)) (:rows checked)))
    (is (= [:incoming-private-entry-call :incoming-private-entry-call :self-recursive-call]
           (mapv :role (leaf 'body-recursive-call-facts facts))))
    (is (= :accepted (:status (leaf 'body-derive-recursive-nodes core facts {}))))

    (is (= :rejected (:status (leaf 'body-tc-verify core facts bindings
                                   (assoc-in graph [self :escaping-thrown-error-types] []) {:active? true}))))
    (let [inflated (leaf 'body-derive-nodes-pass core facts {} (:recursive-proof facts)
                         {:transitive #{:error/throw} :escaping-thrown-errors #{:gravity.type/string :gravity.type/bool}
                          :escaping-thrown-error-types [:gravity.type/string :gravity.type/bool]})]
      (is (= :accepted (:status inflated)))
      (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes inflated))))
      (is (not= graph (:nodes inflated))))))

(deftest current-gravity-private-self-source-context-development-kernel
  (let [{:keys [core facts bindings forms proof cold]} (kernel-support/private-self-value-kernel-data :string)
        derive (fn [c f bs fs] (leaf 'body-is-context c f bs fs))
        result (derive core facts bindings forms)]
    (is (= :accepted (:status proof)))
    (is (= :accepted (:status cold)))
    (is (= :gravity/sh08-private-self-value-proof-v1 (get-in facts [:recursive-proof :domain])))
    (is (true? (leaf 'body-is-proof-valid? core facts)))

    (is (= (:parameter-value-premise proof) (:parameter-value-premise result)))



    (let [never-result (assoc-in facts [:type-table :entry-0] :gravity.type/never)]
      (is (= result (derive core never-result bindings forms))))
    (doseq [bad [(assoc-in facts [:type-table :input-0] :gravity.type/bool)
                 (assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :entries] 7)
                 (assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :entries]
                           (vec (repeat 2048 (get-in facts [:recursive-proof :parameter-value-premise :entry-values :entries 0]))))
                 (assoc-in facts [:recursive-proof :cold-entry-derivation :source-argument-call-node-ids] [:forged])
                 (assoc-in facts [:recursive-proof :cold-body-derivation :cut :excluded-node-ids] [])
                 (assoc-in facts [:recursive-proof :return-witness :return-type] :gravity.type/bool)
                 ;; Removed C7 accounting fields remain invalid as unknown proof keys.
                 (assoc-in facts [:recursive-proof :work-count] nil)
                 (assoc facts :constraint-ledger [])]]
      (is (= :rejected (:status (derive core bad bindings forms)))))
    (is (= :rejected (:status (derive (assoc-in core [:module :exports] ['without-path]) facts bindings forms))))
    (is (= :rejected (:status (derive core facts (assoc-in bindings [:helper-binding :visibility] :public) forms))))
    (is (= :rejected (:status (derive core facts bindings (mapv #(if (= :self-op (:form-id %)) (assoc % :value 'forged) %) forms)))))))

(deftest current-gravity-private-self-head-source-context-development-kernel
  (let [{:keys [core facts bindings forms nodes proof]} (kernel-support/private-self-value-kernel-data :head)
        context (leaf 'body-is-context core facts bindings forms)
        working (assoc core :sh08-private-self-parameter-premise (:parameter-value-premise context))
        call (first (filter #(= :call (:core-node-id %)) (:calls core)))
        node (nodes :call)
        types (:type-table facts)
        head (leaf 'body-lq-head-proof working node call nodes bindings types)]
    (is (= :accepted (:status proof)))

    (is (= :accepted (:status head)))
    (is (= :nonempty (get-in head [:proof :knowledge])))
    (is (= (:parameter-value-premise proof) (get-in head [:proof :immutable-binding-record :parameter-value-premise])))
    (is (= (get-in types [:operator :head-proof]) (:proof head)))
    (is (= :list-head-branch-proof-required
           (:reason (leaf 'body-lq-head-proof core node call nodes bindings types))))
    (is (= :rejected (:status (leaf 'body-is-context core
                                    (assoc-in facts [:recursive-proof :parameter-value-premise :entry-values :normal-entry-count] 0)
                                    bindings forms))))))

(deftest current-gravity-declared-self-effects-development-kernel
  (let [[core facts bindings] (kernel-support/declared-self-kernel-data)
        derived (leaf 'body-derive-recursive-nodes core facts {})
        graph (:nodes derived)
        checked (leaf 'body-tc-verify core facts bindings graph {:active? true})]
    (is (true? (leaf 'body-recursive-proof-valid? core facts)))
    (is (true? (leaf 'body-ds-captures-valid? core facts bindings)))
    (is (= :accepted (:status derived)))
    (is (= :effects-and-escaping-error-types (:summary-domain derived)))
    (is (> (:fixed-point-rounds derived) 1))
    (is (= #{:error/throw} (get-in graph [(sha 4) :transitive])))
    (is (= [:gravity.type/string] (get-in graph [(sha 4) :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(sha 6) :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(sha 11) :escaping-thrown-error-types])))
    (is (= [:gravity.type/string] (get-in graph [(sha 4) :throw-call-dependency :callee-error-types])))
    (is (true? (leaf 'body-graph-equations-valid? core facts {} graph)))
    (is (= :accepted (:status checked)))
    (is (= {(sha 20) [:gravity.type/string]} (:functions checked)))
    (is (= 2 (count (:rows checked))))
    (is (every? #(= :sh09-declared-self-error-equation-v1 (:producer-rule %)) (:rows checked)))
    (is (= [:self-recursive-call :incoming-declared-call]
           (mapv :role (leaf 'body-recursive-call-facts facts))))
    (is (= :accepted (:status (leaf 'body-derive-recursive-nodes core facts {}))))

    (doseq [bad [(assoc-in facts [:recursive-proof :self-call-core-node-id] (sha 99))
                 (assoc-in facts [:recursive-proof :owned-node-ids] [])
                 (assoc-in facts [:recursive-proof :incoming-call-node-ids] [(sha 4)])
                 (assoc-in facts [:recursive-proof :self-edge :ordinal] 9)
                 (assoc-in facts [:recursive-proof :declared-return-type] :gravity.type/bool)
                 (assoc-in facts [:function-type-table 0 :thrown-error-effects] [])]]
      (is (false? (leaf 'body-recursive-proof-valid? core bad))))
    (let [bad (assoc-in core [:nodes 2 :attributes :binding-id] (sha 99))
          reference (assoc-in bad [:nodes 2 :core-form] :reference)]
      (is (false? (leaf 'body-ds-captures-valid? reference facts bindings))))
    (is (= :rejected
           (:status (leaf 'body-tc-verify core facts bindings
                          (assoc-in graph [(sha 4) :escaping-thrown-error-types] [])
                          {:active? true}))))
    (let [inflated (leaf 'body-derive-nodes-pass core facts {} (:recursive-proof facts)
                         {:transitive #{:error/throw} :escaping-thrown-errors #{:gravity.type/string :gravity.type/bool}
                          :escaping-thrown-error-types [:gravity.type/string :gravity.type/bool]})]
      (is (= :accepted (:status inflated)))
      (is (true? (leaf 'body-graph-equations-valid? core facts {} (:nodes inflated))))
      (is (not= graph (:nodes inflated))))))
