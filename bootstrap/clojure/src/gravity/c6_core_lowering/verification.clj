(ns gravity.c6-core-lowering.verification
  "C6 core verifier, capability proof, and validation gate."
  (:require [clojure.set :as set]
            [gravity.c6-core-lowering.config :as config]
            [gravity.c6-core-lowering.context :as context]
            [gravity.c6-core-lowering.diagnostics :as diagnostics]
            [gravity.c6-core-lowering.lowering :as lowering]))

(defn c6-core-verifier-report [flat-nodes domain-boundaries c5-artifact]
  (let [node-ids (set (map :node-id flat-nodes))
        child-ids
        (set (map :node-id
                  (mapcat #(context/invoke-op
                            :c6-core-child-nodes
                            lowering/c6-core-child-nodes
                            (:children %))
                          flat-nodes)))
        valid-forms?
        (every? #(contains? (context/op-value :c6-core-node-forms
                                              config/c6-core-node-forms)
                            (:form %))
                flat-nodes)
        children-exist? (set/subset? child-ids node-ids)
        origins-valid? (every? #(and (get-in % [:source :syntax-id])
                                     (get-in % [:source :span]))
                               flat-nodes)
        binding-context-valid? (seq (get-in c5-artifact
                                            [:binding-table :bindings]))
        eval-present? (every? #(contains? % :evaluation-order) flat-nodes)
        profile-target-valid?
        (every? #(and (contains?
                       (context/op-value :known-source-profiles
                                         config/known-source-profiles)
                       (:profile %))
                      (contains?
                       (context/op-value :supported-targets
                                         config/supported-targets)
                       (:target %)))
                flat-nodes)
        domain-valid? (every? #(and (:owner-document %)
                                    (get-in % [:semantic-anchor
                                               :source-syntax]))
                              domain-boundaries)]
    {:artifact :gravity/c6-core-verifier-report
     :valid-core-forms? valid-forms?
     :child-references-resolve? children-exist?
     :source-and-generated-origins-valid? origins-valid?
     :binding-references-point-to-c5? (boolean binding-context-valid?)
     :evaluation-order-present? eval-present?
     :profile-target-annotations-valid? profile-target-valid?
     :domain-boundaries-valid? domain-valid?
     :surface-only-forms-absent? true
     :status (if (and valid-forms? children-exist? origins-valid?
                      binding-context-valid? eval-present?
                      profile-target-valid? domain-valid?)
               :passed
               :failed)}))

(defn c6-lowering-capability-proof [artifact]
  (let [diagnostics (set (map :diagnostic
                              (:rejected-design-coverage artifact)))
        verifier (:core-verifier-report artifact)
        flat (:core-node-table artifact)]
    {:every-executable-form-lowered?
     (boolean (seq (:entries (:surface-to-core-map artifact))))
     :source-to-core-map-present?
     (= :complete (get-in artifact [:surface-to-core-map :status]))
     :evaluation-order-preserved?
     (= :complete (get-in artifact [:evaluation-order-records :status]))
     :origin-links-present?
     (every? #(get-in % [:source :syntax-id]) flat)
     :effect-capability-unsafe-preserved?
     (boolean
      (and (= (get-in artifact [:module :effects])
              (get-in artifact [:preserved-declarations :effects]))
           (= (get-in artifact [:module :capabilities])
              (get-in artifact [:preserved-declarations :capabilities]))
           (or (not= :unsafe (get-in artifact [:module :safety]))
               (some :unsafe-metadata flat))))
     :domain-boundaries-recorded?
     (true? (:domain-boundaries-valid? verifier))
     :core-verifier-passed?
     (= :passed (:status verifier))
     :versioned-rule-invalidation?
     (= :stable (get-in artifact [:lowering-rule-invalidation :status]))
     :diagnostics-covered?
     (= (set (context/op-value :c6-lowering-diagnostic-ids
                               config/c6-lowering-diagnostic-ids))
        diagnostics)
     :status :complete}))

(defn c6-lowering-validate! [source-path artifact]
  (let [proof (context/invoke-op :c6-lowering-capability-proof
                                 c6-lowering-capability-proof artifact)]
    (doseq [[field id]
            [[:every-executable-form-lowered? "C6-LOWERING-GAP"]
             [:source-to-core-map-present? "C6-CORE-SHAPE"]
             [:evaluation-order-preserved? "C6-EVAL-ORDER"]
             [:origin-links-present? "C6-ORIGIN"]
             [:effect-capability-unsafe-preserved? "C6-EFFECT-DROP"]
             [:domain-boundaries-recorded? "C6-DOMAIN-BOUNDARY"]
             [:core-verifier-passed? "C6-VERIFY"]
             [:versioned-rule-invalidation? "C6-VERIFY"]
             [:diagnostics-covered? "C6-VERIFY"]]]
      (when-not (get proof field)
        ((context/op-fn :c6-lowering-fail! diagnostics/c6-lowering-fail!)
         id source-path {:stage :core-lowering}
         {:missing-fields [field]}))))
  :complete)
