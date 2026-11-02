(declare p15-s23-c11-mir-digest)

(defn- p15-s23-c6c10-reader-owner-field!
  [source-path value key]
  ;; Parent product records have comparator-free native keyword headers. Do
  ;; not traverse unconsumed source forms just to obtain their metadata view.
  (let [pairs
        (try (source-interface-header/pairs! value nil)
             (catch clojure.lang.ExceptionInfo error
               (if (= :versioned-c11-interface (:missing-fact (ex-data error)))
                 (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :reader-owner-product-header {})
                 (throw error))))
        keys (mapv first pairs)]
    (when-not (and (every? #(identical? clojure.lang.Keyword (class %)) keys)
                   (= (count keys) (count (set keys))))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :reader-owner-product-header {}))
    (second (first (filter #(identical? key (first %)) pairs)))))

(def p15-s23-c6c10-reader-binding-projection-keys
  [:artifact :status :semantic-authority :compiled-by :executed-by
   :generic-bridge-residual? :self-hosted? :source-path :source-byte-count
   :source-content-hash :plan-semantic-hash :functions-semantic-hash
   :function-count :function-names-hash :function-shapes-hash
   :entrypoint-semantic-hash :verifier-semantic-hash :builtin-functions-hash
   :observed-instruction-nodes :observed-instruction-depth])
(def p15-s23-c6c10-reader-revision-projection-keys
  [:owner :source-language :logical-source-path :source-content-hash
   :source-byte-count :plan-semantic-hash :functions-semantic-hash
   :builder-function :builder-semantic-hash])
(def p15-s23-c6c10-reader-rule-projection-keys
  [:artifact :owner :source-content-hash :source-byte-count :plan-semantic-hash
   :functions-semantic-hash :builder-function :builder-semantic-hash
   :verifier-function :verifier-semantic-hash :semantic-authority
   :compiled-by :executed-by :self-hosted? :admission-status])
(def p15-s23-c6c10-reader-product-binding-projection-keys
  [:adapter-contract :adapted-source-unit-id :adapted-token-stream-id
   :adapted-form-tree-id :adapted-extension-invocation-set-id])

(defn- p15-s23-c6c10-require-reader-owner-metadata!
  [source-path witness]
  ;; SH02 owner metadata permits only the native default comparator. This is
  ;; not the strict Core carrier domain, which continues to reject TreeMaps.
  ;; Preflight precedes every lookup, equality, traversal and digest below.
  (let [validation (p15-s23-trusted-carrier-validation witness :default-only)]
    (when-not (= :passed (:status validation))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path
       :trusted-current-reader-owner-metadata
       (select-keys validation [:reason :class]))))
  witness)

(defn- p15-s23-c6c10-project-reader-owner-metadata
  [source-path witness]
  (p15-s23-c6c10-require-reader-owner-metadata! source-path witness)
  (let [binding (:binding witness) descriptor (:descriptor witness)
        revision (:source-revision descriptor) envelope (:envelope witness)
        rule (:source-rule envelope) replay (:gravity-template-replay envelope)
        provenance (:provenance witness)
        owner-id (fn [branch value]
                   (clojure.core/binding [*print-length* nil *print-level* nil *print-meta* false
                             *print-dup* false *print-readably* true *print-namespace-maps* false]
                     (p15-s23-c11-mir-digest
                      {:domain :gravity/c6-current-reader-owner-metadata-v1
                       :branch branch :metadata value})))]
    {:owner-metadata-content-id (owner-id :complete-witness witness)
     :projection-contract
     {:artifact :gravity/c6-reader-owner-metadata-projection :schema-version 1
      :encoding :owner-canonical-semantic-v1
      :sorted-metadata-policy :default-only :core-carrier-policy :reject}
     :reader-plan-binding
     (assoc (select-keys binding p15-s23-c6c10-reader-binding-projection-keys)
            :instruction-summary-content-id (owner-id :instruction-summary (:instruction-summary binding)))
     :reader-source-revision
     (assoc (select-keys revision p15-s23-c6c10-reader-revision-projection-keys)
            :function-shapes-content-id (owner-id :reader-function-shapes (:function-shapes revision))
            :verifier-semantic-hash
            (get-in revision [:function-shapes 'sh03-verify-reader-result :semantic-hash]))
     :reader-result-identity
     (assoc (select-keys provenance
              [:actual-sh03-reader-result-id :actual-c2-artifact-id
               :actual-reader-product-integrity-hash :actual-reader-source-id])
            :actual-sh03-semantic-product-binding
            (select-keys (:actual-sh03-semantic-product-binding provenance)
                         p15-s23-c6c10-reader-product-binding-projection-keys))
     :authenticated-envelope
     (assoc (select-keys envelope
              [:artifact :schema-version :status :stage :semantic-envelope-id :provenance-binding-id])
            :source-rule
            (assoc (select-keys rule p15-s23-c6c10-reader-rule-projection-keys)
                   :function-shapes-content-id (owner-id :envelope-function-shapes (:function-shapes rule))
                   :owner-source-rule-content-id (owner-id :complete-source-rule rule))
            :gravity-template-replay
            (assoc (select-keys replay
                     [:status :identity-enforcement :eligible-for-contextual-acceptance? :request-count :semantic-authority])
                   :checks-content-id (owner-id :template-checks (:checks replay))
                   :diagnostics-content-id (owner-id :template-diagnostics (:diagnostics replay))))}))

(defn- p15-s23-c6c10-project-expression-owner-metadata
  [source-path binding]
  (p15-s23-c6c10-require-reader-owner-metadata! source-path binding)
  (let [owner-id (fn [branch value]
                   (clojure.core/binding [*print-length* nil *print-level* nil *print-meta* false
                             *print-dup* false *print-readably* true *print-namespace-maps* false]
                     (p15-s23-c11-mir-digest
                      {:domain :gravity/c6-current-expression-owner-metadata-v1
                       :branch branch :metadata value})))]
    (assoc (select-keys binding
             [:artifact :status :source-path :source-content-hash :semantic-hash
              :artifact-hash :generic-bridge-residual? :clojure-seed-boundary?
              :self-hosted? :source-contract-version :functions-semantic-hash :function-count])
           :functions-content-id (owner-id :export-shapes (:functions binding))
           :declared-plan-compiler-labels-content-id
           (owner-id :declared-plan-compiler-labels (:declared-plan-compiler-labels binding))
           :owner-binding-content-id (owner-id :complete-binding-metadata binding))))
