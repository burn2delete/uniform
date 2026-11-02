(ns gravity.pass-execution.receipt
  "Exactly-once pass execution and receipt construction."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.contract :as contract]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.request :as request]
            [gravity.pass-execution.validation :as validation]))

(defn output-facts
  [pass-contract input-facts]
  (set/union (set/intersection input-facts (:preserves pass-contract))
             (:regenerates pass-contract)))

(defn receipt-id-projection
  [receipt]
  (-> receipt
      (dissoc :receipt-id)
      (assoc :provenance-id (get-in receipt [:provenance :provenance-id]))
      (dissoc :provenance)))

(defn calculated-receipt-id
  [receipt]
  (canonical/content-id :gravity/pass-execution-receipt-v1
                        (receipt-id-projection receipt)))

(defn execute-pass!
  "Execute and validate one injected pass exactly once, then emit its receipt."
  [execution-request operations]
  (binding [diagnostics/*diagnostic-context*
            (merge diagnostics/*diagnostic-context*
                   {:pass (:stage execution-request)
                    :artifact-id (first (:input-artifact-ids execution-request))
                    :profile-id (:profile-id execution-request)
                    :target-id (:target-id execution-request)})]
    (let [execution-request (request/validate-request! execution-request)
          operations (validation/validate-operations!
                      operations config/execute-operation-fields "C16-ENTRY")
          pass-contract (:contract execution-request)
          produced ((:produce! operations) execution-request)
          artifact ((:validate-output! operations) produced execution-request
                    pass-contract)
          output-id ((:artifact-id-of operations) artifact)
          _ (validation/require-sha256! :output-artifact-id output-id)
          verifier-reports ((:verifier-reports operations)
                            artifact execution-request pass-contract)
          evidence-records ((:evidence-records operations)
                            artifact execution-request pass-contract)
          _ (when-not (vector? verifier-reports)
              (diagnostics/fail! "C18-EVIDENCE"
                                 "verifier operation must return a vector"
                                 {:pass (:pass pass-contract)}))
          _ (when-not (vector? evidence-records)
              (diagnostics/fail! "C18-EVIDENCE"
                                 "evidence operation must return a vector"
                                 {:pass (:pass pass-contract)}))
          _ (when (or (> (count verifier-reports)
                         config/maximum-evidence-records)
                      (> (count evidence-records)
                         config/maximum-evidence-records))
              (diagnostics/fail! "C18-EVIDENCE"
                                 "pass evidence exceeds its record bound"
                                 {:pass (:pass pass-contract)
                                  :maximum-records
                                  config/maximum-evidence-records}))
          _ (doseq [report verifier-reports]
              (validation/validate-verifier-report-shape!
               report output-id (:stage execution-request)))
          _ (doseq [record evidence-records]
              (validation/validate-evidence-record-shape! record output-id))
          _ (when-not (= (count verifier-reports)
                         (count (distinct (map :verifier-id verifier-reports))))
              (diagnostics/fail!
               "C18-EVIDENCE" "verifier reports contain duplicate identities"
               {:pass (:pass pass-contract)}))
          _ (when-not (= (count evidence-records)
                         (count (distinct (map :evidence-id evidence-records))))
              (diagnostics/fail!
               "C18-EVIDENCE" "evidence records contain duplicate identities"
               {:pass (:pass pass-contract)}))
          _ (when-not (= (count evidence-records)
                         (count (distinct (map :kind evidence-records))))
              (diagnostics/fail! "C18-EVIDENCE"
                                 "evidence records contain duplicate kinds"
                                 {:pass (:pass pass-contract)}))
          _ (when (and (:verifier-required? pass-contract)
                       (empty? verifier-reports))
              (diagnostics/fail! "C18-EVIDENCE"
                                 "required pass verifier evidence is missing"
                                 {:pass (:pass pass-contract)}))
          observed-evidence (set (map :kind evidence-records))
          missing-evidence
          (set/difference
           (set/union (:required-evidence pass-contract)
                      (set (vals (:replacement-evidence pass-contract))))
           observed-evidence)
          _ (when (seq missing-evidence)
              (diagnostics/fail! "C18-EVIDENCE"
                                 "required pass evidence is missing"
                                 {:pass (:pass pass-contract)
                                  :missing-evidence
                                  (vec (sort missing-evidence))}))
          authority-request (:authority execution-request)
          ceiling (:authority-ceiling pass-contract)
          claimed (:claimed-level authority-request)
          effective
          (validation/weakest-authority
           (into (conj (vec (vals (:input-authorities authority-request)))
                       ceiling claimed)
                 (map :authority-level evidence-records)))
          receipt-base
          {:artifact :gravity/pass-execution-receipt
           :schema-version 1
           :stage (:stage execution-request)
           :pass-contract-id (contract/pass-contract-id pass-contract)
           :producer-binding-id (:producer-binding-id execution-request)
           :input-artifact-ids (:input-artifact-ids execution-request)
           :external-root-inputs (:external-root-inputs execution-request)
           :output-artifact-id output-id
           :input-facts (:input-facts execution-request)
           :output-facts (output-facts pass-contract
                                       (:input-facts execution-request))
           :requires (:requires pass-contract)
           :preserves (:preserves pass-contract)
           :invalidates (:invalidates pass-contract)
           :regenerates (:regenerates pass-contract)
           :replacement-evidence (:replacement-evidence pass-contract)
           :effects (:effects pass-contract)
           :semantic-bindings (:semantic-bindings execution-request)
           :dependency-graph-id (:dependency-graph-id execution-request)
           :build-effect-replay-id (:build-effect-replay-id execution-request)
           :profile-id (:profile-id execution-request)
           :target-id (:target-id execution-request)
           :policy-ids (:policy-ids execution-request)
           :provenance (:provenance execution-request)
           :diagnostic-stream-id (:diagnostic-stream-id execution-request)
           :verifier-reports verifier-reports
           :evidence-records evidence-records
           :execution-mode :executed
           :authority
           {:input-authorities (:input-authorities authority-request)
            :claimed-level claimed :effective-level effective :ceiling ceiling
            :scope (:scope authority-request)
            :authority-contribution? false
            :aggregate-authoritative? false}}
          receipt (assoc receipt-base :receipt-id
                         (calculated-receipt-id receipt-base))]
      {:artifact artifact :receipt receipt})))
