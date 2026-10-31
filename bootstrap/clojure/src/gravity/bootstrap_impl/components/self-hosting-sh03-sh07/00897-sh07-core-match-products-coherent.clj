

(defn
 sh07-core-match-products-coherent?
 [core]
 (gravity.checked-core/match-products-coherent?
  core
  (checked-core-stage-operations)))

(defn sh07-core-verification-checks
  [artifact expected upstream-verification]
  (let [core
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact])
        expected-core
        (get-in expected
                [:gravity-core-boundary :canonical-core-artifact])
        source-path (get-in artifact [:provenance :source-path])
        self-comparison? (identical? artifact expected)
        replay-equal?
        (fn [expected-value actual-value]
          (or self-comparison?
              (= (sh07-core-exact-comparison-value expected-value)
                 (sh07-core-exact-comparison-value actual-value))))]
    {:wrapper-schema-current?
     (= (set (keys expected)) (set (keys artifact)))
     :wrapper-kind-current?
     (= :gravity/sh07-core-artifact (:kind artifact))
     :upstream-verification-passed?
     (= :passed (:status upstream-verification))
     :semantic-artifact-id-current?
     (= (:artifact-id artifact)
        (:artifact-id core)
        (reader-canonical-hash
         {:domain :gravity/sh07-declared-digest-v1
          :purpose :sh07-core-artifact-id
          :preimage (:identity-preimage core)}))
     :authenticated-request-replays?
     (= (get-in expected
                [:gravity-core-boundary :authenticated-core-request])
        (get-in artifact
                [:gravity-core-boundary :authenticated-core-request]))
     :gravity-template-replays?
     (= (get-in expected
                [:gravity-core-boundary :raw-template-result])
        (get-in artifact
                [:gravity-core-boundary :raw-template-result]))
     :digest-sequence-replays?
     (= (get-in expected
                [:gravity-core-boundary :digest-requests])
        (get-in artifact
                [:gravity-core-boundary :digest-requests]))
     :resolved-digests-replay?
     (= (get-in expected
                [:gravity-core-boundary :resolved-digests])
        (get-in artifact
                [:gravity-core-boundary :resolved-digests]))
     :canonical-core-replays?
     (replay-equal? expected-core core)
     :fragment-manifest-replay?
     (replay-equal? (:fragment-manifest expected-core)
                    (:fragment-manifest core))
     :fragment-coverage-replay?
     (replay-equal? (:fragment-coverage expected-core)
                    (:fragment-coverage core))
     :module-assembly-manifest-replay?
     (replay-equal? (:module-assembly-manifest expected-core)
                    (:module-assembly-manifest core))
     :module-replay?
     (and
      (replay-equal? (:identity-preimage expected-core)
                     (:identity-preimage core))
      (replay-equal? (:module-assembly-manifest expected-core)
                     (:module-assembly-manifest core))
      (= (sh07-core-exact-comparison-value
          (get-in core
                  [:identity-preimage
                   :module-assembly-manifest]))
         (sh07-core-exact-comparison-value
          (:module-assembly-manifest core))))
     :declared-alias-table-replay?
     (replay-equal? (:declared-alias-table expected-core)
                    (:declared-alias-table core))
     :control-flow-replays?
     (replay-equal? (:control-flow expected-core) (:control-flow core))
     :reference-uses-replay?
     (replay-equal? (:reference-uses expected-core) (:reference-uses core))
     :var-references-replay?
     (replay-equal? (:var-references expected-core) (:var-references core))
     :calls-replay?
     (replay-equal? (:calls expected-core) (:calls core))
     :function-records-replay?
     (replay-equal? (:function-records expected-core)
                    (:function-records core))
     :call-edges-replay?
     (replay-equal? (:call-edges expected-core) (:call-edges core))
     :recursion-components-replay?
     (replay-equal? (:recursion-components expected-core)
                    (:recursion-components core))
     :keyword-lookups-replay?
     (replay-equal? (:keyword-lookups expected-core) (:keyword-lookups core))
     :lexical-bindings-replay?
     (replay-equal? (:lexical-bindings expected-core)
                    (:lexical-bindings core))
     :loop-bindings-replay?
     (replay-equal? (:loop-bindings expected-core) (:loop-bindings core))
     :recur-targets-replay?
     (replay-equal? (:recur-targets expected-core) (:recur-targets core))
     :recur-transfers-replay?
     (replay-equal? (:recur-transfers expected-core) (:recur-transfers core))
     :mutations-replay?
     (replay-equal? (:mutations expected-core) (:mutations core))
     :error-transfers-replay?
     (and
      (replay-equal? (:error-transfers expected-core)
                     (:error-transfers core))
      (sh07-core-error-transfers-coherent? core))
     :error-handlers-replay?
     (and
      (replay-equal? (:error-handlers expected-core)
                     (:error-handlers core))
      (sh07-core-error-handlers-coherent? core))
     :match-branch-records-replay?
     (and
      (replay-equal? (:match-branch-records expected-core)
                     (:match-branch-records core))
      (sh07-core-match-products-coherent? core))
     :match-decision-skeletons-replay?
     (and
      (replay-equal? (:match-decision-skeletons expected-core)
                     (:match-decision-skeletons core))
      (sh07-core-match-products-coherent? core))
     :match-pattern-records-replay?
     (and
      (replay-equal? (:match-pattern-records expected-core)
                     (:match-pattern-records core))
      (sh07-core-match-products-coherent? core))
     :template-verification-passed?
     (= :passed
        (get-in artifact
                [:gravity-core-boundary
                 :template-verification :status]))
     :resolved-verification-passed?
     (= :passed
        (get-in artifact
                [:gravity-core-boundary
                 :resolved-verification :status]))
     :authoritative-products-replay?
     (replay-equal? (dissoc expected :capability-based-proof)
                    (dissoc artifact :capability-based-proof))
     :stored-capability-proof-current?
     (= (:capability-based-proof expected)
        (:capability-based-proof artifact))
     :provenance-retained?
     (= source-path
        (get-in core [:provenance :actual-source-path]))}))

(defn sh07-core-proof-from-checks
  [checks]
  (let [failed
        (vec (for [[check passed?] checks
                   :when (not (true? passed?))]
               check))]
    (assoc checks
           :artifact :gravity/sh07-core-capability-proof
           :status (if (empty? failed) :complete :failed)
           :failed-checks failed)))

(defn sh07-core-exact-comparison-value
  [value]
  (cond
    (instance? java.math.BigDecimal value)
    [:gravity/exact-big-decimal
     (.toString (.unscaledValue ^java.math.BigDecimal value))
     (.scale ^java.math.BigDecimal value)]
    (map? value)
    (into {}
          (map (fn [[key child]]
                 [(sh07-core-exact-comparison-value key)
                  (sh07-core-exact-comparison-value child)]))
          value)
    (vector? value)
    (mapv sh07-core-exact-comparison-value value)
    (set? value)
    (into #{} (map sh07-core-exact-comparison-value) value)
    (list? value)
    (apply list (map sh07-core-exact-comparison-value value))
    :else value))