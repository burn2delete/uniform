

(defn p15-s23-c11-mir-validate-final-artifact!
  [artifact checked-core context]
  (let [source-path (p15-s23-c11-ingress-source-path context)
        checked-core-sorted-policy
        (p15-s23-c11-carrier-sorted-policy checked-core)]
    (p15-s23-c11-mir-require-trusted-carrier!
     source-path :authenticated-c11-artifact artifact
     checked-core-sorted-policy)
    (p15-s23-c11-mir-require-trusted-carrier!
     source-path :checked-core checked-core checked-core-sorted-policy)
    (p15-s23-c11-mir-require-trusted-carrier!
     source-path :checked-core-context context checked-core-sorted-policy)
    (p15-s23-c11-mir-bounded-value!
     source-path :authenticated-c11-mir-artifact artifact
     p15-s23-c11-mir-max-final-artifact-carrier-nodes
     p15-s23-c11-mir-max-carrier-depth)
    (binding [*p15-s23-c11-mir-diagnostic-context*
              (p15-s23-c11-mir-diagnostic-context
               checked-core context artifact)]
     (let [source-path source-path]
    (p15-s23-c11-mir-require!
     (p15-s23-c11-mir-metadata-free? artifact)
     "C11-VERIFY" source-path {}
     :metadata-free-authenticated-c11-carrier)
    (p15-s23-c11-mir-require!
     (and (map? artifact)
          (nil? (meta artifact))
          (= p15-s23-c11-mir-artifact-keys (set (keys artifact)))
          (= :gravity/p15-s23-c11-authenticated-mir-artifact
             (:kind artifact))
          (= 1 (:schema-version artifact))
          (= (:artifact-id checked-core)
             (:source-core-artifact-id artifact))
          (= (:b1-preflight artifact)
             (get-in artifact [:mir-module :b1-preflight]))
          (vector? (:diagnostics artifact))
          (= [] (:diagnostics artifact))
          (vector? (get-in artifact [:mir-module :diagnostics]))
          (true? (:mir-derived? artifact))
          (true? (:clojure-seed-boundary? artifact))
          (false? (:self-hosted? artifact)))
     "C11-MODULE" source-path artifact :authenticated-c11-artifact-envelope)
    (p15-s23-c11-mir-require!
     (= (p15-s23-c11-mir-expected-ingress-semantic
         checked-core context)
        (:checked-core-ingress artifact))
     "C11-MODULE" source-path artifact
     :authenticated-checked-core-ingress-authority)
    (p15-s23-c11-mir-require!
     (= p15-s23-c11-mir-module-keys
        (set (keys (:mir-module artifact))))
     "C11-MODULE" source-path artifact :verified-mir-module-envelope)
    (let [final-blocks
          (get-in artifact [:mir-module :functions
                            (first (keys (get-in artifact
                                                 [:mir-module :functions])))
                            :blocks])]
      (p15-s23-c11-mir-require!
       (and (every? #(= :passed (:verifier-status %))
                    (mapcat :instructions (vals final-blocks)))
            (every? #(= :passed
                        (get-in % [:terminator :verifier-status]))
                    (vals final-blocks)))
       "C11-VERIFY" source-path artifact
       :finalized-operation-and-terminator-verifier-status))
    (p15-s23-c11-mir-require!
     (and (= :passed (get-in artifact [:mir-module :verification-status]))
          (= (p15-s23-c11-b1-candidate-record
              (:mir-module artifact)
              checked-core
              (:verification-report artifact)
              (:scope artifact))
             (:b1-preflight artifact)))
     "C11-VERIFY" source-path artifact :verified-mir-candidate-for-b1)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     (p15-s23-c11-b1-candidate-record
      (:mir-module artifact) checked-core
      (:verification-report artifact) (:scope artifact))
     (:b1-preflight artifact) :type-sensitive-verified-mir-candidate-for-b1)
    (p15-s23-c11-mir-require!
     (and (= (p15-s23-c11-mir-pass-contract-record)
             (get-in artifact [:mir-module :pass-contract]))
          (= (p15-s23-c11-mir-pass-execution-record
              (:mir-module artifact) checked-core
              (:verification-report artifact))
             (get-in artifact [:mir-module :pass-execution-record])))
     "C11-VERIFY" source-path artifact
     :content-bound-build-mir-pass-contract-and-execution)
    (p15-s23-c11-mir-require-strict-structure!
     source-path (p15-s23-c11-mir-pass-contract-record)
     (get-in artifact [:mir-module :pass-contract])
     :type-sensitive-content-bound-build-mir-pass-contract)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     (p15-s23-c11-mir-pass-execution-record
      (:mir-module artifact) checked-core (:verification-report artifact))
     (get-in artifact [:mir-module :pass-execution-record])
     :type-sensitive-content-bound-build-mir-pass-execution)
    (p15-s23-c11-mir-require!
     (= (p15-s23-c11-mir-scope-record) (:scope artifact))
     "C11-VERIFY" source-path artifact :exact-partial-c11-scope)
    (p15-s23-c11-mir-require-strict-structure!
     source-path (p15-s23-c11-mir-scope-record) (:scope artifact)
     :type-sensitive-partial-c11-scope)
    (p15-s23-c11-mir-require!
     (= (p15-s23-c11-mir-source-rule-record
         {:source-content-hash
          p15-s23-c11-mir-expected-source-content-hash
          :source-byte-count p15-s23-c11-mir-source-byte-count
          :plan-semantic-hash
          p15-s23-c11-mir-expected-plan-semantic-hash
          :functions-semantic-hash
          p15-s23-c11-mir-expected-functions-semantic-hash
          :builder-semantic-hash
          p15-s23-c11-mir-expected-builder-semantic-hash
          :verifier-semantic-hash
          p15-s23-c11-mir-expected-verifier-semantic-hash
          :function-shapes p15-s23-c11-mir-required-functions})
        (:source-rule artifact))
     "C11-VERIFY" source-path artifact :pinned-c11-source-rule-record)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     (p15-s23-c11-mir-source-rule-record
      {:source-content-hash
       p15-s23-c11-mir-expected-source-content-hash
       :source-byte-count p15-s23-c11-mir-source-byte-count
       :plan-semantic-hash
       p15-s23-c11-mir-expected-plan-semantic-hash
       :functions-semantic-hash
       p15-s23-c11-mir-expected-functions-semantic-hash
       :builder-semantic-hash
       p15-s23-c11-mir-expected-builder-semantic-hash
       :verifier-semantic-hash
       p15-s23-c11-mir-expected-verifier-semantic-hash
       :function-shapes p15-s23-c11-mir-required-functions})
     (:source-rule artifact)
     :type-sensitive-pinned-c11-source-rule)
    (p15-s23-c11-mir-require!
     (= (p15-s23-c11-mir-expected-provenance
         checked-core context (p15-s23-c11-mir-resolve-source-path))
        (:provenance artifact))
     "C11-ORIGIN" source-path artifact
     :exact-checked-core-and-c11-source-provenance)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     (p15-s23-c11-mir-expected-provenance
      checked-core context (p15-s23-c11-mir-resolve-source-path))
     (:provenance artifact)
     :type-sensitive-checked-core-and-c11-source-provenance)
    (p15-s23-c11-mir-require!
     (= (:mir-id artifact)
        (p15-s23-c11-mir-recomputed-id artifact))
     "C11-VERIFY" source-path artifact :path-neutral-c11-mir-id)
    (p15-s23-c11-mir-require!
     (= (:artifact-id artifact)
        (p15-s23-c11-mir-digest
         {:kind (:kind artifact)
          :schema-version (:schema-version artifact)
          :mir-id (:mir-id artifact)}))
     "C11-VERIFY" source-path artifact :content-addressed-c11-artifact-id)
    (p15-s23-c11-mir-require!
     (= (:actual-path-binding-id artifact)
        (p15-s23-c11-mir-recomputed-actual-path-binding-id
         artifact checked-core context))
     "C11-ORIGIN" source-path artifact :actual-path-provenance-binding)
    (p15-s23-c11-mir-require!
     (= {:semantic-constructor
         {:owner :gravity-source
          :function p15-s23-c11-mir-builder-function
          :builder-semantic-hash
          (get-in artifact [:source-rule :builder-semantic-hash])}
         :execution-tcb
         {:runner :clojure-stage0-rule-runner
          :compiled-by :clojure-stage0-seed
          :seed-boundary? true}
         :semantic-replay-parity :public-verifier-required
         :invocation-audit :not-available
         :execution-count :not-claimed
         :live-invocation-claim? false
         :self-hosted? false}
        (:construction-record artifact))
     "C11-VERIFY" source-path artifact
     :honest-gravity-constructor-and-clojure-execution-tcb)
    (p15-s23-c11-mir-require-strict-structure!
     source-path
     {:semantic-constructor
      {:owner :gravity-source
       :function p15-s23-c11-mir-builder-function
       :builder-semantic-hash
       (get-in artifact [:source-rule :builder-semantic-hash])}
      :execution-tcb
      {:runner :clojure-stage0-rule-runner
       :compiled-by :clojure-stage0-seed
       :seed-boundary? true}
      :semantic-replay-parity :public-verifier-required
      :invocation-audit :not-available
      :execution-count :not-claimed
      :live-invocation-claim? false
      :self-hosted? false}
     (:construction-record artifact)
     :type-sensitive-gravity-constructor-and-execution-tcb)
    (let [recomputed-verifier
          (p15-s23-c11-mir-validate-constructed!
           source-path checked-core
           (p15-s23-c11-mir-pending-view (:mir-module artifact)))]
      (p15-s23-c11-mir-require!
       (= (p15-s23-c11-independent-verifier-record recomputed-verifier)
          (:verification-report artifact))
       "C11-VERIFY" source-path artifact
       :independently-recomputed-verifier-report)
      (p15-s23-c11-mir-require-strict-structure!
       source-path
       (p15-s23-c11-independent-verifier-record recomputed-verifier)
       (:verification-report artifact)
       :type-sensitive-independently-recomputed-verifier-report))
    :passed))))