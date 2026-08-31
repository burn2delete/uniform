(ns gravity.compiler-pass-manifest.incremental-validation
  "Incremental cache and proof reuse validation."
  (:require [gravity.compiler-pass-manifest.failures :as failures]
            [gravity.compiler-pass-manifest.support :as support]))

(defn compiler-pass-validate-incremental!
  [source-path manifest suite]
  (let [required-fields (set (get-in suite [:cache-key-schema
                                            :required-fields]))]
    (doseq [cache-key (:cache-keys suite)]
      (let [missing-fields (vec (remove #(support/present? (get cache-key %))
                                        required-fields))]
        (when (seq missing-fields)
          (failures/compiler-pass-fail! "C16-KEY" source-path manifest cache-key
                               {:missing-fields missing-fields
                                :remediation "Cache keys must include every semantic, policy, profile, capability, pass, and dependency fact that can affect meaning."})))))
  (doseq [entry (:cache-entries suite)]
    (let [missing-fields (failures/compiler-pass-missing-fields
                          entry
                          [:stage :cache-key :artifact-id :producer :inputs
                           :preserved-facts :invalidated-by :diagnostics
                           :trust :revalidation])]
      (when (seq missing-fields)
        (failures/compiler-pass-fail! "C16-ENTRY" source-path manifest entry
                             {:missing-fields missing-fields
                              :remediation "Cache entries are artifacts and must retain producer, inputs, facts, diagnostics, trust, and revalidation state."}))))
  (doseq [proof (:proof-reuse-records suite)]
    (when (and (= :stale (:status proof)) (= :accepted (:reuse proof)))
      (failures/compiler-pass-fail! "C16-PROOF" source-path manifest proof
                           {:remediation "Stale proofs and certificates must be rejected or regenerated before reuse."})))
  (doseq [reuse (:speculative-reuse-records suite)]
    (when (and (= :speculative (:reuse reuse)) (:publishable? reuse))
      (failures/compiler-pass-fail! "C16-SPECULATIVE" source-path manifest reuse
                           {:remediation "Speculative interactive reuse cannot reach publishable or release artifact boundaries."})))
  :complete)
