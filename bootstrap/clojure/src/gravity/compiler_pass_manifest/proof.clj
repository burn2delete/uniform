(ns gravity.compiler-pass-manifest.proof
  "Capability-based proof derived from an assembled pass suite."
  (:require [clojure.set :as set]
            [gravity.compiler-pass-manifest.contracts :as contracts]
            [gravity.compiler-pass-manifest.failures :as failures]
            [gravity.compiler-pass-manifest.support :as support]))

(defn compiler-pass-capability-proof
  [suite]
  (let [contracts (:contracts suite)
        contracts-by-pass (into {} (map (juxt :pass identity) contracts))
        risk-records (:risk-classification suite)
        trust-passes (set (or (:covered-passes (:compiler-trust-report suite))
                              (map :pass (:passes (:compiler-trust-report
                                                   suite)))))]
    {:canonical-order-exposed?
     (= contracts/compiler-pass-default-stage-order (:stage-order suite))
     :contracts-complete?
     (and (= (set (:stage-order suite)) (set (map :pass contracts)))
          (every? #(empty? (failures/compiler-pass-missing-fields
                            % contracts/compiler-pass-contract-required-fields))
                  contracts))
     :metadata-preserved-or-replaced?
     (every? (fn [contract]
               (let [durable-drops (set/intersection contracts/compiler-pass-durable-facts
                                                     (set (:invalidates
                                                           contract)))
                     replacements (set (concat (:regenerates contract)
                                               (:replacement-evidence
                                                contract)
                                               (:emits contract)))]
                 (empty? (set/difference durable-drops replacements))))
             contracts)
     :backend-lowering-checked?
     (= :verified-mir-or-domain-ir (get-in contracts-by-pass
                                           [:lower-target :input]))
     :diagnostics-structured?
     (every? #(and (support/present? (:rule %))
                   (support/present? (get-in % [:primary :span]))
                   (support/present? (:origin-chain %))
                   (support/present? (:facts %))
                   (support/present? (:remediation %))
                   (:secret-free? %))
             (:diagnostic-fixtures suite))
     :incremental-keys-complete?
     (every? #(empty? (failures/compiler-pass-missing-fields
                       % (get-in suite [:cache-key-schema :required-fields])))
             (:cache-keys suite))
     :plugin-capabilities-scoped?
     (let [plugin (:plugin-manifest suite)]
       (set/subset? (set (:requested-scopes plugin))
                    (set (get-in plugin
                                 [:capability-scopes
                                  :compiler/ir-transform]))))
     :verification-gates-present?
     (and (every? #(set/subset? (set (:minimum-evidence %))
                                (set (:available-evidence %)))
                  risk-records)
          (set/subset? (set (map :pass contracts)) trust-passes)
          (empty? (:evidence-gaps (:release-gate-report suite))))
     :status :complete}))
