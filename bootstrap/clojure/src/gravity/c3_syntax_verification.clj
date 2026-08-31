(ns gravity.c3-syntax-verification
  "Hosted Stage0 C3 verification, capability evidence, and validation routing.

  The leaf recomputes hosted syntax evidence through caller-supplied C2/SH04
  authentication operations. A passing hosted report is not canonical C3,
  proof, self-hosting, attestation, or release authority."
  (:require [clojure.set :as set]
            [gravity.c3-syntax-verification.policy :as policy]))

(def ^:private ^:dynamic *operations* {})
(def ^:private ^:dynamic *active-operation-keys* #{})
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
            (every? #(and (get-in % [:origin 0 :producer])
                          (seq (or (get-in % [:origin 0 :input-syntax-ids])
                                   (get-in % [:origin 0 :inputs])))
                          (or (get-in % [:origin 0 :span])
                              (get-in % [:origin 0 :generated-span])))
                    (filter #(= :generated-form (get-in % [:form :kind]))
                            syntax-stream))
            :hygiene-visible? (every? #(map? (:hygiene %)) syntax-stream)
            :metadata-valid? (every? #(map? (:metadata %)) syntax-stream)
            :namespace-context-valid?
            (every? #(map? (:namespace %)) syntax-stream)
            :phase-transitions-allowed?
            (every? #{:read :macro-expanded} (map :phase syntax-stream))
            :serialization-round-trips? (true? (:roundtrip? serialization))
            :serialization-current?
            (= serialization
               (invoke :c3-syntax-serialization-fixture syntax-stream))
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
          (c3-syntax-verification-report
           syntax-stream fresh-serialization
           (:c2-reader-artifact artifact)
           (:gravity-syntax-boundary artifact))
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
            (and (every? #(some (comp #{:source} :kind) (:origin %))
                         (remove #(= :generated-form
                                      (get-in % [:form :kind]))
                                 syntax-stream))
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
