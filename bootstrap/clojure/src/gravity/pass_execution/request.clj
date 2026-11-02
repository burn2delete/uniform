(ns gravity.pass-execution.request
  "Execution-request validation before any injected pass operation runs."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.contract :as contract]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.validation :as validation]))

(defn validate-request!
  [request]
  (canonical/preflight-canonical! request)
  (validation/exact-map! request config/execution-request-fields "C16-KEY"
                         :request)
  (let [pass-contract (contract/validate-pass-contract! (:contract request))]
    (when-not (= (:stage request) (:pass pass-contract))
      (diagnostics/fail! "D1-PIPELINE-ORDER"
                         "request stage differs from its pass contract"
                         {:stage (:stage request)
                          :contract-pass (:pass pass-contract)}))
    (validation/require-sha256! :producer-binding-id
                                (:producer-binding-id request))
    (validation/keyword-set! :input-facts (:input-facts request))
    (validation/validate-input-artifact-ids! (:input-artifact-ids request))
    (validation/validate-external-root-inputs!
     (:external-root-inputs request) (:input-artifact-ids request)
     (:input-facts request) (:input pass-contract))
    (when-not (set/subset? (:requires pass-contract) (:input-facts request))
      (diagnostics/fail!
       "C1-PASS-CONTRACT" "pass input lacks required facts"
       {:missing-facts
        (vec (sort (set/difference (:requires pass-contract)
                                   (:input-facts request))))}))
    (when-not (set/subset? (:preserves pass-contract) (:input-facts request))
      (diagnostics/fail!
       "C1-EVIDENCE-DROP" "a pass cannot preserve absent input facts"
       {:missing-facts
        (vec (sort (set/difference (:preserves pass-contract)
                                   (:input-facts request))))}))
    (validation/validate-semantic-bindings! (:semantic-bindings request))
    (doseq [field [:dependency-graph-id :build-effect-replay-id :profile-id
                   :target-id :diagnostic-stream-id]]
      (validation/require-sha256! field (get request field)))
    (validation/sorted-sha-vector! :policy-ids (:policy-ids request))
    (validation/validate-provenance! (:provenance request))
    (when-not (= :executed (:execution-mode request))
      (diagnostics/fail! "C16-ENTRY"
                         "execute-pass! can only attest a producer execution"
                         {:observed (:execution-mode request)}))
    (validation/validate-request-authority!
     (:authority request) (:authority-ceiling pass-contract)
     (:input-artifact-ids request))
    (canonical/canonical-bytes request))
  request)
