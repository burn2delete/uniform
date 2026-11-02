(ns gravity.pass-execution.dag
  "Order-invariant pass-receipt DAG validation and composition."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.contract :as contract]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.receipt-validation :as receipt-validation]
            [gravity.pass-execution.validation :as validation]))

(defn detect-cycle
  [edges]
  (let [state (atom {})
        stack (atom [])
        found (atom nil)]
    (letfn [(visit [node]
              (when-not @found
                (swap! state assoc node :active)
                (swap! stack conj node)
                (doseq [next-node (get edges node [])]
                  (case (get @state next-node)
                    :active (reset! found
                                    (conj (vec (drop-while
                                               #(not= % next-node) @stack))
                                          next-node))
                    :done nil
                    (visit next-node)))
                (swap! stack pop)
                (swap! state assoc node :done)))]
      (doseq [node (keys edges)]
        (when-not (get @state node) (visit node)))
      @found)))

(defn dag-id-projection
  [dag]
  (-> dag
      (dissoc :evidence-root-id)
      (update :receipts
              (fn [receipts]
                (mapv (fn [receipt]
                        (-> receipt
                            (assoc :provenance-id
                                   (get-in receipt
                                           [:provenance :provenance-id]))
                            (dissoc :provenance)))
                      receipts)))))

(defn compose-evidence-dag
  "Validate and compose an order-invariant evidence DAG of pass receipts."
  [receipts contracts]
  (when-not (and (vector? receipts) (seq receipts)
                 (vector? contracts) (seq contracts))
    (diagnostics/fail! "D1-ARTIFACT-GAP"
                       "receipts and contracts must be nonempty vectors" {}))
  (when (or (> (count receipts) config/maximum-dag-receipts)
            (> (count contracts) config/maximum-dag-receipts))
    (diagnostics/fail! "D1-ARTIFACT-GAP"
                       "pass evidence DAG exceeds its receipt bound"
                       {:maximum-receipts config/maximum-dag-receipts}))
  (doseq [pass-contract contracts]
    (contract/validate-pass-contract! pass-contract))
  (let [contracts-by-id
        (into {} (map (juxt contract/pass-contract-id identity) contracts))]
    (when-not (= (count contracts) (count contracts-by-id))
      (diagnostics/fail! "C1-PASS-CONTRACT"
                         "pass contracts contain duplicate identities" {}))
    (when-not (= (count contracts) (count (distinct (map :pass contracts))))
      (diagnostics/fail! "C1-PASS-CONTRACT"
                         "pass contracts contain duplicate pass ids" {}))
    (when-not (= (count contracts) (count (distinct (map :order contracts))))
      (diagnostics/fail! "D1-PIPELINE-ORDER"
                         "pass contracts contain duplicate orders" {}))
    (let [receipts
          (mapv
           (fn [receipt]
             (binding [diagnostics/*diagnostic-context*
                       (merge diagnostics/*diagnostic-context*
                              {:pass (:stage receipt)
                               :artifact-id (:output-artifact-id receipt)
                               :profile-id (:profile-id receipt)
                               :target-id (:target-id receipt)})]
               (if-let [pass-contract
                        (get contracts-by-id (:pass-contract-id receipt))]
                 (receipt-validation/validate-receipt-structure!
                  receipt pass-contract)
                 (diagnostics/fail! "C1-PASS-CONTRACT"
                                    "receipt names an unknown pass contract"
                                    {:pass-contract-id
                                     (:pass-contract-id receipt)}))))
           receipts)
          receipts-by-id (into {} (map (juxt :receipt-id identity) receipts))
          producers-by-output
          (into {} (map (juxt :output-artifact-id identity) receipts))]
      (when-not (= (count receipts) (count receipts-by-id))
        (diagnostics/fail! "C16-ENTRY"
                           "evidence DAG contains duplicate receipts" {}))
      (when-not (= (count receipts) (count producers-by-output))
        (diagnostics/fail! "D1-ARTIFACT-GAP"
                           "multiple receipts produce one artifact id" {}))
      (let [used-contracts (set (map :pass-contract-id receipts))]
        (when-not (= used-contracts (set (keys contracts-by-id)))
          (diagnostics/fail!
           "C1-PASS-CONTRACT" "evidence DAG contains unused pass contracts"
           {:unused-contract-ids
            (vec (sort (set/difference (set (keys contracts-by-id))
                                       used-contracts)))})))
      (doseq [receipt receipts
              input-id (:input-artifact-ids receipt)]
        (let [producer (get producers-by-output input-id)
              external? (contains? (:external-root-inputs receipt) input-id)
              observed-authority
              (get-in receipt [:authority :input-authorities input-id])]
          (if producer
            (let [producer-contract
                  (get contracts-by-id (:pass-contract-id producer))
                  consumer-contract
                  (get contracts-by-id (:pass-contract-id receipt))]
              (when external?
                (diagnostics/fail!
                 "D1-ARTIFACT-GAP"
                 "an internally produced input cannot be an external root"
                 {:artifact-id input-id :pass (:stage receipt)}))
              (when-not (= (:output producer-contract)
                           (:input consumer-contract))
                (diagnostics/fail!
                 "C1-PASS-CONTRACT"
                 "producer output IR does not match consumer input IR"
                 {:artifact-id input-id
                  :producer-output (:output producer-contract)
                  :consumer-input (:input consumer-contract)}))
              (when-not (= observed-authority
                           (get-in producer [:authority :effective-level]))
                (diagnostics/fail!
                 "C16-POLICY"
                 "internal edge authority differs from producer authority"
                 {:artifact-id input-id :observed observed-authority
                  :expected (get-in producer
                                    [:authority :effective-level])})))
            (when-not external?
              (diagnostics/fail!
               "D1-ARTIFACT-GAP"
               "input has no producer and is not an explicit external root"
               {:artifact-id input-id :pass (:stage receipt)})))))
      (let [predecessors
            (into {}
                  (map (fn [receipt]
                         [(:receipt-id receipt)
                          (->> (:input-artifact-ids receipt)
                               (keep #(some-> (get producers-by-output %)
                                              :receipt-id))
                               set)]))
                  receipts)
            successors
            (reduce-kv
             (fn [result child parents]
               (reduce #(update %1 %2 (fnil conj #{}) child) result parents))
             (zipmap (keys predecessors) (repeat #{}))
             predecessors)
            cycle (detect-cycle successors)]
        (when cycle
          (diagnostics/fail! "D1-PIPELINE-ORDER"
                             "pass evidence graph contains a cycle"
                             {:cycle cycle}))
        (doseq [receipt receipts
                predecessor-id (get predecessors (:receipt-id receipt))
                :let [predecessor (get receipts-by-id predecessor-id)
                      predecessor-contract
                      (get contracts-by-id (:pass-contract-id predecessor))
                      pass-contract
                      (get contracts-by-id (:pass-contract-id receipt))]]
          (when-not (< (:order predecessor-contract) (:order pass-contract))
            (diagnostics/fail!
             "D1-PIPELINE-ORDER"
             "pass receipt consumes an artifact out of canonical order"
             {:producer (:stage predecessor) :consumer (:stage receipt)})))
        (let [roots (filterv #(empty? (get predecessors (:receipt-id %)))
                             receipts)
              sinks (filterv #(empty? (get successors (:receipt-id %)))
                             receipts)]
          (when-not (and (= 1 (count roots)) (= 1 (count sinks)))
            (diagnostics/fail! "D1-ARTIFACT-GAP"
                               "pass evidence must form one connected rooted DAG"
                               {:root-count (count roots)
                                :sink-count (count sinks)}))
          (let [reachable
                (loop [pending [(:receipt-id (first roots))] seen #{}]
                  (if-let [node (peek pending)]
                    (if (contains? seen node)
                      (recur (pop pending) seen)
                      (recur (into (pop pending) (get successors node))
                             (conj seen node)))
                    seen))]
            (when-not (= reachable (set (keys receipts-by-id)))
              (diagnostics/fail!
               "D1-ARTIFACT-GAP" "pass evidence DAG is disconnected"
               {:unreachable
                (vec (sort (set/difference (set (keys receipts-by-id))
                                           reachable)))})))
          (doseq [receipt receipts
                  :let [parents (get predecessors (:receipt-id receipt))]]
            (let [parent-facts
                  (map #(get-in receipts-by-id [% :output-facts]) parents)
                  external-facts
                  (map :facts (vals (:external-root-inputs receipt)))
                  expected-input-facts
                  (reduce set/union #{} (concat parent-facts external-facts))]
              (when-not (= expected-input-facts (:input-facts receipt))
                (diagnostics/fail!
                 "C1-EVIDENCE-DROP"
                 "consumer facts do not equal predecessor output facts"
                 {:pass (:stage receipt) :expected expected-input-facts
                  :observed (:input-facts receipt)}))))
          (let [ordered-receipts
                (->> receipts
                     (sort-by (fn [receipt]
                                [(get-in contracts-by-id
                                         [(:pass-contract-id receipt) :order])
                                 (:receipt-id receipt)]))
                     vec)
                ordered-contracts
                (->> contracts
                     (sort-by (juxt :order contract/pass-contract-id)) vec)
                effective-level
                (validation/weakest-authority
                 (mapv #(get-in % [:authority :effective-level]) receipts))
                dag-base
                {:artifact :gravity/pass-evidence-dag
                 :schema-version 1
                 :root-receipt-id (:receipt-id (first sinks))
                 :receipts ordered-receipts
                 :contracts ordered-contracts
                 :edges (->> predecessors
                             (mapcat (fn [[child parents]]
                                       (map (fn [parent]
                                              {:from parent :to child})
                                            parents)))
                             (sort-by (juxt :from :to)) vec)
                 :authority
                 {:effective-level effective-level
                  :authority-contribution? false
                  :aggregate-authoritative? false}}]
            (assoc dag-base :evidence-root-id
                   (canonical/content-id :gravity/pass-evidence-dag-v1
                                         (dag-id-projection dag-base)))))))))
