(ns gravity.pass-execution.contract
  "Pass-contract validation and stable contract identity."
  (:require [clojure.set :as set]
            [gravity.pass-execution.canonical :as canonical]
            [gravity.pass-execution.config :as config]
            [gravity.pass-execution.diagnostics :as diagnostics]
            [gravity.pass-execution.validation :as validation]))

(def ^:private validated-contract-context-token (Object.))
(def ^:private ^:dynamic *validated-contract-context* nil)

(defn- current-validated-contract
  [contract]
  (let [context *validated-contract-context*
        entries (:entries context)]
    (when (and (map? context)
               (identical? validated-contract-context-token (:token context))
               (instance? java.util.IdentityHashMap entries))
      (.get ^java.util.IdentityHashMap entries contract))))

(defn validated-pass-contract-identity
  "Return the current composition-local identity, when one is active.

  This is an internal read-only seam; the public `gravity.pass-execution`
  facade does not expose composition contexts or trusted markers."
  [contract]
  (let [validated (current-validated-contract contract)]
    (when (map? validated)
      validated)))

(defn validate-pass-contract!
  "Validate one exact C1/C16/C18 pass contract."
  [contract]
  (binding [diagnostics/*diagnostic-context*
            (merge diagnostics/*diagnostic-context* {:pass (:pass contract)})]
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

(defn- canonical-pass-contract*
  [contract]
  (into (sorted-map)
        (map (fn [[key value]]
               [key (cond
                      (set? value) (canonical/canonical-sort value)
                      (map? value) (into (sorted-map) value)
                      :else value)]))
        contract))

(defn canonical-pass-contract
  "Return the stable semantic projection used to identify a pass contract."
  [contract]
  (if (current-validated-contract contract)
    (canonical-pass-contract* contract)
    (canonical-pass-contract* (validate-pass-contract! contract))))

(defn pass-contract-id
  "Return the content identity of one validated pass contract."
  [contract]
  (if-let [validated (current-validated-contract contract)]
    (if (map? validated)
      (:id validated)
      (canonical/content-id :gravity/pass-contract-v1
                            (canonical-pass-contract contract)))
    (canonical/content-id :gravity/pass-contract-v1
                          (canonical-pass-contract contract))))

(defn with-validated-contract-identities
  "Run `thunk` with one request-local identity for each validated contract.

  The context is identity-keyed and private to one composition.  It is not a
  cache or a public trust marker; all entries are populated only after the
  normal contract validator succeeds."
  [contracts thunk]
  (let [validated (mapv (fn [pass-contract]
                          (validate-pass-contract! pass-contract)
                          pass-contract)
                        contracts)
        entries (java.util.IdentityHashMap.)
        context {:token validated-contract-context-token :entries entries}]
    (doseq [pass-contract validated]
      (.put entries pass-contract ::validated))
    (binding [*validated-contract-context* context]
      (let [identities
            (mapv (fn [pass-contract]
                    {:contract pass-contract
                     :id (pass-contract-id pass-contract)})
                  validated)]
        (doseq [{:keys [contract] :as identity} identities]
          (.put entries contract identity))
        (thunk identities)))))
