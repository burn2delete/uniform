(ns gravity.pass-execution.receipt-validation
  "Receipt structure, fact-flow, evidence, and authority revalidation."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.contract :as contract]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.receipt :as receipt]
            [gravity.pass-execution.validation :as validation]))

(defn validate-receipt-structure!
  [value pass-contract]
  (canonical/preflight-canonical! value)
  (validation/exact-map! value config/receipt-fields "C16-ENTRY" :receipt)
  (contract/validate-pass-contract! pass-contract)
  (when-not (and (= :gravity/pass-execution-receipt (:artifact value))
                 (= 1 (:schema-version value))
                 (= (:pass pass-contract) (:stage value))
                 (= (contract/pass-contract-id pass-contract)
                    (:pass-contract-id value)))
    (diagnostics/fail! "C16-ENTRY" "receipt does not match its pass contract"
                       {:stage (:stage value) :pass (:pass pass-contract)}))
  (doseq [field [:receipt-id :producer-binding-id :output-artifact-id
                 :dependency-graph-id :build-effect-replay-id :profile-id
                 :target-id :diagnostic-stream-id]]
    (validation/require-sha256! field (get value field)))
  (validation/validate-input-artifact-ids! (:input-artifact-ids value))
  (validation/sorted-sha-vector! :policy-ids (:policy-ids value))
  (doseq [field [:input-facts :output-facts :requires :preserves :invalidates
                 :regenerates :effects]]
    (validation/keyword-set! field (get value field)))
  (validation/validate-external-root-inputs!
   (:external-root-inputs value) (:input-artifact-ids value)
   (:input-facts value) (:input pass-contract))
  (validation/validate-semantic-bindings! (:semantic-bindings value))
  (validation/validate-provenance! (:provenance value))
  (when-not (= :executed (:execution-mode value))
    (diagnostics/fail! "C16-ENTRY"
                       "only executed receipts are supported in this wave"
                       {:observed (:execution-mode value)}))
  (when-not (= (:receipt-id value) (receipt/calculated-receipt-id value))
    (diagnostics/fail! "C16-STALE" "receipt content identity does not recompute"
                       {:observed (:receipt-id value)}))
  (when-not (and (= (:requires pass-contract) (:requires value))
                 (= (:preserves pass-contract) (:preserves value))
                 (= (:invalidates pass-contract) (:invalidates value))
                 (= (:regenerates pass-contract) (:regenerates value))
                 (= (:replacement-evidence pass-contract)
                    (:replacement-evidence value))
                 (= (:effects pass-contract) (:effects value))
                 (set/subset? (:requires pass-contract) (:input-facts value))
                 (set/subset? (:preserves pass-contract) (:input-facts value))
                 (= (:output-facts value)
                    (receipt/output-facts pass-contract (:input-facts value))))
    (diagnostics/fail! "C1-EVIDENCE-DROP"
                       "receipt fact flow differs from its contract"
                       {:pass (:pass pass-contract)}))
  (when-not (vector? (:verifier-reports value))
    (diagnostics/fail! "C18-EVIDENCE" "verifier reports must be a vector" {}))
  (when-not (vector? (:evidence-records value))
    (diagnostics/fail! "C18-EVIDENCE" "evidence records must be a vector" {}))
  (when (or (> (count (:verifier-reports value))
               config/maximum-evidence-records)
            (> (count (:evidence-records value))
               config/maximum-evidence-records))
    (diagnostics/fail! "C18-EVIDENCE"
                       "receipt evidence exceeds its record bound"
                       {:maximum-records config/maximum-evidence-records}))
  (doseq [report (:verifier-reports value)]
    (validation/validate-verifier-report-shape!
     report (:output-artifact-id value) (:stage value)))
  (doseq [record (:evidence-records value)]
    (validation/validate-evidence-record-shape!
     record (:output-artifact-id value)))
  (when-not (= (count (:verifier-reports value))
               (count (distinct (map :verifier-id
                                     (:verifier-reports value)))))
    (diagnostics/fail! "C18-EVIDENCE"
                       "verifier reports contain duplicate identities" {}))
  (when-not (= (count (:evidence-records value))
               (count (distinct (map :evidence-id
                                     (:evidence-records value)))))
    (diagnostics/fail! "C18-EVIDENCE"
                       "evidence records contain duplicate identities" {}))
  (when-not (= (count (:evidence-records value))
               (count (distinct (map :kind (:evidence-records value)))))
    (diagnostics/fail! "C18-EVIDENCE"
                       "evidence records contain duplicate kinds" {}))
  (when (and (:verifier-required? pass-contract)
             (empty? (:verifier-reports value)))
    (diagnostics/fail! "C18-EVIDENCE"
                       "required pass verifier evidence is missing" {}))
  (let [observed (set (map :kind (:evidence-records value)))
        required (set/union (:required-evidence pass-contract)
                            (set (vals (:replacement-evidence pass-contract))))
        missing (set/difference required observed)]
    (when (seq missing)
      (diagnostics/fail! "C18-EVIDENCE" "required pass evidence is missing"
                         {:missing-evidence (vec (sort missing))})))
  (let [authority (:authority value)]
    (validation/exact-map! authority config/receipt-authority-fields
                           "C16-POLICY" :receipt-authority)
    (let [input-authorities (:input-authorities authority)
          _ (when-not (and (map? input-authorities)
                           (= (set (:input-artifact-ids value))
                              (set (keys input-authorities))))
              (diagnostics/fail!
               "C16-POLICY" "receipt authority does not bind its exact inputs"
               {}))
          levels (mapv (fn [[artifact-id level]]
                         (validation/require-sha256!
                          :input-authority-artifact-id artifact-id)
                         (validation/authority-level!
                          :input-authority-level level))
                       input-authorities)
          claimed (validation/authority-level!
                   :claimed-level (:claimed-level authority))
          effective (validation/authority-level!
                     :effective-level (:effective-level authority))
          ceiling (validation/authority-level! :ceiling (:ceiling authority))
          maximum (validation/weakest-authority (conj levels ceiling))
          expected-effective
          (validation/weakest-authority
           (into (conj levels ceiling claimed)
                 (map :authority-level (:evidence-records value))))]
      (when-not
       (and (= ceiling (:authority-ceiling pass-contract))
            (= expected-effective effective)
            (<= (config/authority-rank effective)
                (config/authority-rank maximum))
            (let [scope (:scope authority)]
              (or (and (keyword? scope) (not (empty? (name scope))))
                  (and (string? scope)
                       (some #(not (Character/isWhitespace ^char %)) scope))))
            (false? (:authority-contribution? authority))
            (false? (:aggregate-authoritative? authority)))
        (diagnostics/fail! "C16-POLICY"
                           "receipt authority is incomplete or widened"
                           {:authority authority :maximum maximum}))))
  value)

(defn validate-execution-receipt!
  "Revalidate one receipt and invoke each supplied evidence validator once."
  [value pass-contract operations]
  (binding [diagnostics/*diagnostic-context*
            (merge diagnostics/*diagnostic-context*
                   {:pass (:stage value)
                    :artifact-id (:output-artifact-id value)
                    :profile-id (:profile-id value)
                    :target-id (:target-id value)})]
    (let [operations (validation/validate-operations!
                      operations config/receipt-validation-operation-fields
                      "C16-ENTRY")
          value (validate-receipt-structure! value pass-contract)]
      ((:validate-diagnostic-stream! operations)
       (:diagnostic-stream-id value) value)
      (doseq [report (:verifier-reports value)]
        ((:validate-verifier-report! operations) report value))
      (doseq [record (:evidence-records value)]
        ((:validate-evidence-record! operations) record value))
      value)))
