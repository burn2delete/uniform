;; These helpers validate/project DATA. They never authenticate a supplied
;; report. Only the lexical public constructor/verifier calls the fresh C6
;; owner; standalone final-artifact validation is relative to checked Core.
(declare p15-s23-c11-mir-expected-ingress-semantic p15-s23-c11-ingress-source-path)

(def p15-s23-c11-core-policy-keys
  #{:source-interface :source-policy-binding-id :admission-status
    :verification-scope :source-policy-receipt :compiler-program})
(def p15-s23-c11-core-policy-receipt-keys
  #{:source-interface :admission-status :verification-scope :reader-plan-binding
    :reader-source-revision :reader-result-identity :authenticated-envelope
    :envelope-verification :self-hosted? :expression-binding
    :owner-metadata-content-id :projection-contract})
(def p15-s23-c11-core-program-projection-keys
  [:source-contract-version :source-byte-count :source-content-hash
   :plan-semantic-hash :functions-semantic-hash :builder-semantic-hash :verifier-semantic-hash])

(def p15-s23-c11-core-projection-contract
  {:artifact :gravity/c6-reader-owner-metadata-projection :schema-version 1
   :encoding :owner-canonical-semantic-v1
   :sorted-metadata-policy :default-only :core-carrier-policy :reject})

(defn- p15-s23-c11-core-policy-content-id? [value]
  (and (identical? String (class value))
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-c11-mir-require-core-policy-data!
  [checked-core context policy]
  (let [path (p15-s23-c11-ingress-source-path context)]
    (p15-s23-c11-mir-require-trusted-carrier! path :current-core-policy-data policy :reject)
    (let [receipt (:source-policy-receipt policy)
        compiler (:compiler-program policy)
        contract c6-source-contract/current-compiler-program
        header {:interface :gravity/c6-c10 :version 2}
        expected-program (assoc (select-keys contract (rest p15-s23-c11-core-program-projection-keys))
                                :source-contract-version 2)
        envelope (:authenticated-envelope receipt)
        expression (:expression-binding receipt)
        expression-contract c6-source-contract/current-expression-program]
    (p15-s23-c11-mir-require!
     (and (= p15-s23-c11-core-policy-keys (set (keys policy)))
          (= (count p15-s23-c11-core-policy-keys) (count policy))
          (= p15-s23-c11-core-policy-receipt-keys (set (keys receipt)))
          (= (count p15-s23-c11-core-policy-receipt-keys) (count receipt))
          (identical? Long (class (get-in policy [:source-interface :version])))
          (identical? Long (class (:source-contract-version compiler)))
          (identical? Long (class (get-in receipt [:source-interface :version])))
          (= header (:source-interface policy) (:source-interface receipt))
          (= :development-unadmitted (:admission-status policy) (:admission-status receipt)
             (get-in envelope [:source-rule :admission-status]))
          (= :selected-current-source-host-seed (:verification-scope policy) (:verification-scope receipt))
          (= :passed (:envelope-verification receipt))
          (false? (:self-hosted? receipt))
          (= p15-s23-c11-core-projection-contract (:projection-contract receipt))
          (identical? Long (class (get-in receipt [:projection-contract :schema-version])))
          (every? p15-s23-c11-core-policy-content-id?
            [(:owner-metadata-content-id receipt)
             (get-in receipt [:reader-plan-binding :instruction-summary-content-id])
             (get-in receipt [:reader-source-revision :function-shapes-content-id])
             (get-in envelope [:source-rule :function-shapes-content-id])
             (get-in envelope [:source-rule :owner-source-rule-content-id])
             (get-in envelope [:gravity-template-replay :checks-content-id])
             (get-in envelope [:gravity-template-replay :diagnostics-content-id])
             (:functions-content-id expression)
             (:declared-plan-compiler-labels-content-id expression)
             (:owner-binding-content-id expression)])
          (= expected-program compiler)
          (= :accepted (:status envelope)) (= :c2-reader (:stage envelope))
          (= #{:actual-sh03-reader-result-id :actual-sh03-semantic-product-binding :actual-c2-artifact-id
               :actual-reader-product-integrity-hash :actual-reader-source-id}
             (set (keys (:reader-result-identity receipt))))
          (every? string? (map (:reader-result-identity receipt)
                              [:actual-sh03-reader-result-id :actual-c2-artifact-id
                               :actual-reader-product-integrity-hash :actual-reader-source-id]))
          (= (get-in c6-source-contract/current-reader-program [:binding :source-content-hash])
             (get-in receipt [:reader-source-revision :source-content-hash]))
          (identical? Long (class (:source-contract-version expression)))
          (= 2 (:source-contract-version expression))
          (= (select-keys expression-contract [:source-content-hash :semantic-hash :functions-semantic-hash :artifact-hash :function-count])
             (select-keys expression [:source-content-hash :semantic-hash :functions-semantic-hash :artifact-hash :function-count]))
          (= (:source-policy-binding-id policy)
             (p15-s23-c6c10-canonical-digest
              path {:domain :gravity/c6-c10-source-policy-v2 :artifact-id (:artifact-id checked-core)
                    :context context :policy receipt})))
     "C11-MODULE" path {} :current-core-source-policy-data)
    policy)))

(defn p15-s23-c11-mir-core-policy-projection
  [checked-core context fresh-report]
  ;; A projection, not a saved-report admission API. Callers establish fresh
  ;; authority before projecting; the structural helper only checks coherence.
  (p15-s23-c11-mir-require-trusted-carrier!
   (p15-s23-c11-ingress-source-path context) :fresh-core-report-projection fresh-report :reject)
  (p15-s23-c11-mir-require-core-policy-data!
   checked-core context
   (assoc (select-keys fresh-report
            [:source-interface :source-policy-binding-id :admission-status
             :verification-scope :source-policy-receipt])
          :compiler-program (select-keys (:source-binding fresh-report)
                              p15-s23-c11-core-program-projection-keys))))

(defn p15-s23-c11-mir-core-policy-hash [policy]
  (when policy (p15-s23-c6c10-canonical-digest
                "<c11-core-policy>" {:domain :gravity/c11-checked-core-policy-v2 :policy policy})))

(defn p15-s23-c11-mir-validate-ingress-data!
  [checked-core context ingress version]
  (when (= 2 version)
    (p15-s23-c11-mir-require-trusted-carrier!
     (p15-s23-c11-ingress-source-path context) :core-ingress-policy-data ingress :reject))
  (let [base (p15-s23-c11-mir-expected-ingress-semantic checked-core context)
        policy (:source-policy ingress)]
    (if (= 2 version)
      (do
        (p15-s23-c11-mir-require-core-policy-data! checked-core context policy)
        (p15-s23-c11-mir-require!
         (and (= :gravity-source-pure (:checked-core-ingress-mode base))
              (identical? Long (class (:schema-version ingress)))
              (= (assoc base :schema-version 2 :source-policy policy) ingress))
         "C11-MODULE" (p15-s23-c11-ingress-source-path context) {} :current-core-ingress-policy-shape))
      (p15-s23-c11-mir-require!
       (= base ingress) "C11-MODULE" (p15-s23-c11-ingress-source-path context) {}
       :authenticated-checked-core-ingress-authority))
    ingress))

(defn p15-s23-c11-mir-core-policy-binding [version policy]
  (when (= 2 version)
    (p15-s23-c11-mir-require-trusted-carrier! "<c11-core-policy>" :core-policy-hash-input policy :reject)
    (p15-s23-c11-mir-require!
     (and (map? policy) (string? (:source-policy-binding-id policy)))
     "C11-MODULE" "<c11-core-policy>" {} :current-core-policy-binding-required)
    {:source-policy-binding-id (:source-policy-binding-id policy)
     :source-policy-hash (p15-s23-c11-mir-core-policy-hash policy)}))
