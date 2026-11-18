(ns gravity.c3-syntax-verification
  "Hosted Stage0 C3 verification, capability evidence, and validation routing.

  The leaf recomputes hosted syntax evidence through caller-supplied C2/SH04
  authentication operations. A passing hosted report is not canonical C3,
  proof, self-hosting, attestation, or release authority."
  (:require [clojure.set :as set]
            [gravity.c3-syntax-verification.policy :as policy]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
(def ^:private default-serialization-token (Object.))
;; This is a transaction-local handoff, never a cache of verification output.
(def ^:private ^:dynamic *fresh-serialization-context* nil)
(def ^:private namespace-contract policy/namespace-contract)

(defn with-operations [operations thunk]
  (policy/validate-operations! operations)
  (when-not (fn? thunk)
    (throw (ex-info "C3 syntax verification thunk must be a function"
                    {:thunk thunk})))
  (binding [*operations* operations] (thunk)))

(defn- current-operation [key]
  (when-not (contains? *active-operation-keys* key) (get *operations* key)))

(defn- invoke [key & args]
  (if-let [operation (current-operation key)]
    (apply operation args)
    (throw (ex-info (str "C3 syntax verification requires operation " key)
                    {:operation key}))))

#_{:clj-kondo/ignore [:unused-private-var]}
(defn- mark-production-default-serialization-operation
  [operation live-valid?]
  (with-meta
    operation
    (assoc (meta operation)
           :gravity.c3-syntax-verification/default-serialization-token
           default-serialization-token
           :gravity.c3-syntax-verification/default-serialization-live-valid?
           live-valid?)))

(defn- production-default-serialization-binding
  []
  (let [operation (current-operation :c3-syntax-serialization-fixture)
        metadata (meta operation)
        live-valid?
        (:gravity.c3-syntax-verification/default-serialization-live-valid?
         metadata)]
    (when (and (identical?
                default-serialization-token
                (:gravity.c3-syntax-verification/default-serialization-token
                 metadata))
               (fn? live-valid?)
               (true? (live-valid?)))
      {:operation operation
       :live-valid? live-valid?})))

(defn- transaction-fresh-serialization
  [syntax-stream supplied-serialization]
  (let [{transaction-stream :syntax-stream
         transaction-operations :operations
         transaction-operation :serialization-operation
         transaction-input-serialization :input-serialization
         transaction-live-valid? :live-valid?
         transaction-serialization :serialization}
        *fresh-serialization-context*
        operation (current-operation :c3-syntax-serialization-fixture)]
    (if (and transaction-stream
             (identical? syntax-stream transaction-stream)
             (identical? supplied-serialization
                         transaction-input-serialization)
             (identical? *operations* transaction-operations)
             (identical? operation transaction-operation)
             (fn? transaction-live-valid?)
             (true? (transaction-live-valid?)))
      transaction-serialization
      (invoke :c3-syntax-serialization-fixture syntax-stream))))

(defn- origin-has-producer-and-span? [origin]
  (and (:producer origin)
       (or (:span origin) (:generated-span origin))))

(defn- generated-origin-valid? [prior-ids hosted-source? c2-artifact origin]
  (and (origin-has-producer-and-span? origin)
       (let [input-fields (select-keys origin [:input-syntax-ids :inputs])
             abbreviation? (and hosted-source?
                                (= {:kind :reader
                                    :name 'gravity.stage0/reader-abbreviation
                                    :version "stage0"}
                                   (:producer origin)))]
         (and (seq input-fields)
              (every? (fn [[field ids]]
                        (if (and abbreviation? (= :inputs field))
                          ;; Hosted Reader authentication reconstructs this C2 seed link.
                          (and c2-artifact (vector? ids)
                               (= 1 (count ids)) (some? (first ids)))
                          (and (coll? ids) (seq ids) (every? prior-ids ids))))
                      input-fields)))))

(defn- generated-origins-valid? [syntax-stream c2-artifact gravity-boundary]
  (loop [remaining (seq syntax-stream) prior-ids #{}]
    (if remaining
      (let [{:keys [form origin] syntax-id :syntax/id} (first remaining)
            generated-form? (= :generated-form (:kind form))
            origins (when (sequential? origin) origin)
            generated (if generated-form?
                        (remove #(= :source (:kind %)) origins)
                        (filter #(= :generated (:kind %)) origins))
            available (disj prior-ids syntax-id)
            hosted-source? (and (nil? gravity-boundary) (not generated-form?))]
        (if (and (or (not generated-form?)
                     (and (origin-has-producer-and-span? (first origins))
                          (seq generated)))
                 (every? #(generated-origin-valid? available hosted-source? c2-artifact %)
                         generated))
          (recur (next remaining) (conj prior-ids syntax-id))
          false))
      true)))

(defn c3-syntax-verification-report
  ([syntax-stream serialization]
   (c3-syntax-verification-report syntax-stream serialization nil nil))
  ([syntax-stream serialization c2-artifact]
   (c3-syntax-verification-report syntax-stream serialization c2-artifact nil))
  ([syntax-stream serialization c2-artifact gravity-boundary]
   (if-let [operation (current-operation :c3-syntax-verification-report)]
     (binding [*active-operation-keys*
               (conj *active-operation-keys*
                     :c3-syntax-verification-report)]
       (operation syntax-stream serialization c2-artifact gravity-boundary))
     (let [required-fields
           (set (:required-fields (invoke :c3-syntax-schema)))
           checks
           {:required-fields-present?
            (every? #(set/subset? required-fields (set (keys %)))
                    syntax-stream)
            :source-spans-resolve?
            (every? #(invoke :c3-resolvable-span? (:span %)) syntax-stream)
            :generated-origins-valid?
            (generated-origins-valid? syntax-stream c2-artifact gravity-boundary)
            :hygiene-visible? (every? #(map? (:hygiene %)) syntax-stream)
            :metadata-valid? (every? #(map? (:metadata %)) syntax-stream)
            :namespace-context-valid?
            (every? #(map? (:namespace %)) syntax-stream)
            :phase-transitions-allowed?
            (every? #{:read :macro-expanded} (map :phase syntax-stream))
            :serialization-round-trips? (true? (:roundtrip? serialization))
            :serialization-current?
            (= serialization
               (transaction-fresh-serialization syntax-stream serialization))
            :reader-products-authentic?
            (if c2-artifact
              (invoke :c3-syntax-stream-reader-products-authentic?
                      syntax-stream c2-artifact gravity-boundary)
              true)}]
       (assoc checks
              :artifact :gravity/syntax-verification-report
              :status (if (every? true? (vals checks)) :passed :failed))))))

(defn c3-syntax-capability-proof
  [artifact]
  (if-let [operation (current-operation :c3-syntax-capability-proof)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys* :c3-syntax-capability-proof)]
      (operation artifact))
    (let [syntax-stream (:syntax-object-stream artifact)
          source-syntax
          (remove #(= :generated-form (get-in % [:form :kind])) syntax-stream)
          diagnostics
          (set (map :diagnostic (:rejected-design-coverage artifact)))
          metadata-ledger (:metadata-ledger artifact)
          expected-metadata-syntax-ids
          (set (map :syntax/id (filter #(seq (:metadata %)) source-syntax)))
          recorded-metadata-syntax-ids
          (set (map :syntax-id (:source-metadata metadata-ledger)))
          fact-ledger (:fact-ledger artifact)
          serialization (:syntax-serialization-fixture artifact)
          fresh-serialization
          (invoke :c3-syntax-serialization-fixture syntax-stream)
          generated-report (:generated-syntax-report artifact)
          verifier (:syntax-verification-report artifact)
          gravity-result
          (get-in artifact [:gravity-syntax-boundary :resolved-syntax-result])
          gravity-ownership (:gravity-syntax-ownership-product artifact)
          fresh-verifier
          (let [serialization-binding
                (production-default-serialization-binding)]
            (binding [*fresh-serialization-context*
                      (when serialization-binding
                        {:syntax-stream syntax-stream
                         :operations *operations*
                         :serialization-operation
                         (:operation serialization-binding)
                         :input-serialization fresh-serialization
                         :live-valid? (:live-valid? serialization-binding)
                         :serialization fresh-serialization})]
              (c3-syntax-verification-report
               syntax-stream fresh-serialization
               (:c2-reader-artifact artifact)
               (:gravity-syntax-boundary artifact))))
          checks
          {:construction-from-reader-seeds?
           (boolean
            (and (seq syntax-stream)
                 (= (inc (count (:syntax-seed-stream
                                 (:c2-reader-artifact artifact))))
                    (count syntax-stream))))
           :stable-syntax-ids?
           (boolean
            (and (every? #(re-find #"^sha256:" (:syntax/id %)) syntax-stream)
                 (= (count syntax-stream)
                    (count (set (map :syntax/id syntax-stream))))))
           :source-and-generated-origins?
           (boolean
            (and (true? (:generated-origins-valid? fresh-verifier))
                 (every? #(some (comp #{:source} :kind) (:origin %))
                         source-syntax)
                 (seq (:generated generated-report))))
           :hygiene-propagated?
           (boolean
            (and (= :complete (get-in artifact [:hygiene-context-map :status]))
                 (some #(seq (get-in % [:hygiene :marks])) syntax-stream)))
           :intentional-capture-recorded?
           (boolean (some #(seq (get-in % [:hygiene :captures])) syntax-stream))
           :capture-rejection-covered? (contains? diagnostics "C3-CAPTURE")
           :metadata-preservation-and-change?
           (boolean
            (and (= expected-metadata-syntax-ids recorded-metadata-syntax-ids)
                 (seq (:explicit-changes metadata-ledger))))
           :fact-invalidation-recorded?
           (boolean (and (seq (:attached fact-ledger))
                         (seq (:invalidated fact-ledger))))
           :serialization-round-trips?
           (and (true? (:roundtrip? serialization))
                (= serialization fresh-serialization))
           :reader-products-authentic?
           (true? (:reader-products-authentic? fresh-verifier))
           :syntax-verifier-current? (= verifier fresh-verifier)
           :syntax-verifier-passed? (= :passed (:status fresh-verifier))
           :gravity-authoritative-products-current?
           (boolean
            (and (= (:gravity-hygiene-context-map artifact)
                    (:hygiene-context-map gravity-result))
                 (= (:gravity-metadata-ledger artifact)
                    (:metadata-ledger gravity-result))
                 (= (:gravity-fact-invalidation-ledger artifact)
                    (:fact-invalidation-ledger gravity-result))
                 (= (:gravity-origin-chain-graph artifact)
                    (:origin-chain-graph gravity-result))
                 (= gravity-ownership (:ownership-product gravity-result))
                 (= :gravity-source (:owner gravity-ownership))
                 (= 'gravity.bootstrap.syntax (:module gravity-ownership))
                 (= (mapv :ownership
                          (:syntax-object-stream gravity-result))
                    (:syntax-ownership gravity-ownership))))
           :diagnostics-covered?
           (= (set (:c3-syntax-diagnostic-ids *operations*)) diagnostics)}]
      (assoc checks :status (if (every? true? (vals checks))
                              :complete
                              :failed)))))

(defn c3-syntax-validate!
  [source-path artifact]
  (if-let [operation (current-operation :c3-syntax-validate!)]
    (binding [*active-operation-keys*
              (conj *active-operation-keys* :c3-syntax-validate!)]
      (operation source-path artifact))
    (let [proof (c3-syntax-capability-proof artifact)]
      (doseq [[field id]
              [[:construction-from-reader-seeds? "C3-SHAPE"]
               [:stable-syntax-ids? "C3-ID"]
               [:source-and-generated-origins? "C3-ORIGIN"]
               [:hygiene-propagated? "C3-HYGIENE"]
               [:intentional-capture-recorded? "C3-CAPTURE"]
               [:metadata-preservation-and-change? "C3-METADATA"]
               [:fact-invalidation-recorded? "C3-FACT-STALE"]
               [:serialization-round-trips? "C3-SERIALIZE"]
               [:reader-products-authentic? "C3-FACT-STALE"]
               [:syntax-verifier-current? "C3-FACT-STALE"]
               [:syntax-verifier-passed? "C3-SHAPE"]
               [:gravity-authoritative-products-current? "C3-FACT-STALE"]
               [:diagnostics-covered? "C3-SHAPE"]]]
        (when-not (get proof field)
          (invoke :c3-syntax-fail!
                  id source-path {:stage :syntax-object-model}
                  {:missing-fields [field]})))
      :complete)))
