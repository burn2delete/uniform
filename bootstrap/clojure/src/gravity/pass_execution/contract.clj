(ns gravity.pass-execution.contract
  "Pass-contract validation and stable contract identity."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.validation :as validation]))

(defn validate-pass-contract!
  "Validate one exact, bounded C1/C16/C18 pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context*
            (merge diagnostics/*diagnostic-context* {:pass (:pass contract)})]
    (canonical/preflight-canonical! contract)
    (validation/exact-map! contract config/pass-contract-fields
                           "C1-PASS-CONTRACT" :contract)
    (when-not (keyword? (:pass contract))
      (diagnostics/fail! "C1-PASS-CONTRACT" "pass id must be a keyword" {}))
    (when-not (and (string? (:version contract))
                   (not (empty? (:version contract))))
      (diagnostics/fail! "C1-PASS-CONTRACT"
                         "pass version must be a nonempty string" {}))
    (when-not (and (integer? (:order contract)) (pos? (:order contract)))
      (diagnostics/fail! "D1-PIPELINE-ORDER"
                         "pass order must be a positive integer"
                         {:observed (:order contract)}))
    (doseq [field [:input :output]]
      (when-not (keyword? (get contract field))
        (diagnostics/fail! "C1-PASS-CONTRACT"
                           "pass IR kinds must be keywords"
                           {:field field :observed (get contract field)})))
    (doseq [field [:requires :preserves :invalidates :regenerates :emits
                   :effects :capabilities :profiles :required-evidence]]
      (validation/keyword-set! field (get contract field)))
    (when-not (and (map? (:replacement-evidence contract))
                   (every? keyword? (keys (:replacement-evidence contract)))
                   (every? keyword? (vals (:replacement-evidence contract)))
                   (= (count (:replacement-evidence contract))
                      (count (distinct
                              (vals (:replacement-evidence contract))))))
      (diagnostics/fail!
       "C1-PASS-CONTRACT"
       "replacement evidence must map facts to unique evidence kinds"
       {:field :replacement-evidence
        :observed (:replacement-evidence contract)}))
    (when-not (boolean? (:verifier-required? contract))
      (diagnostics/fail! "C1-PASS-CONTRACT"
                         "verifier requirement must be boolean" {}))
    (validation/authority-level! :authority-ceiling
                                 (:authority-ceiling contract))
    (when (seq (set/intersection (:preserves contract)
                                 (:invalidates contract)))
      (diagnostics/fail!
       "C1-EVIDENCE-DROP"
       "a pass cannot preserve and invalidate one fact"
       {:facts (vec (sort (set/intersection (:preserves contract)
                                            (:invalidates contract))))}))
    (let [unregenerated (set/difference (:invalidates contract)
                                        (:regenerates contract))
          replacement-facts (set (keys (:replacement-evidence contract)))]
      (when-not (= unregenerated replacement-facts)
        (diagnostics/fail!
         "C1-EVIDENCE-DROP"
         "replacement evidence must cover exactly unregenerated invalidations"
         {:facts (vec (sort unregenerated))
          :replacement-facts (vec (sort replacement-facts))})))
    (canonical/canonical-bytes contract)
    contract))

(defn canonical-pass-contract
  "Return the stable semantic projection used to identify a pass contract."
  [contract]
  (validate-pass-contract! contract)
  (into (sorted-map)
        (map (fn [[key value]]
               [key (cond
                      (set? value) (canonical/canonical-sort value)
                      (map? value) (into (sorted-map) value)
                      :else value)]))
        contract))

(defn pass-contract-id
  "Return the content identity of one validated pass contract."
  [contract]
  (canonical/content-id :gravity/pass-contract-v1
                        (canonical-pass-contract contract)))
