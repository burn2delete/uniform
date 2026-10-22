(defn- c3-current-reader-identity-view [source-path c2-artifact]
  (if (:registered-literal-projection c2-artifact) c2-artifact
    (let [registry (sh04-syntax-registered-literal-registry source-path c2-artifact)]
      (if (seq (:bindings registry))
        (sh04-syntax-project-registered-literal-values source-path registry c2-artifact)
        c2-artifact))))

(defn- c3-current-reader-revision-authentic? [c2-artifact boundary]
  (if (nil? boundary) true
    (let [retained (or (get-in c2-artifact [:sh03-reader-authentication :reader-source-revision])
                      (when (= :gravity/sh03-current-source-reader-plan-binding
                               (get-in c2-artifact [:gravity-reader-boundary :plan-binding :artifact]))
                        (get-in c2-artifact [:gravity-reader-boundary :authenticated-envelope-descriptor :source-revision])))
          provenance (:reader-authentication-provenance boundary)
          descriptor (:actual-sh03-envelope-descriptor provenance)
          actual (:source-revision descriptor)]
      (if (or (some? retained)
              (not= sh03-reader-expected-source-content-hash (:source-content-hash actual))
              (not= sh03-reader-expected-source-content-hash
                    (get-in boundary [:reader-source-revision :source-content-hash])))
        (and (map? retained) (= actual retained)
             (= :passed (p15-s23-stage2-sh02-descriptor-envelope-verify!
                          (:actual-sh03-authenticated-envelope provenance) :c2-reader
                          :gravity/sh03-reader-products descriptor
                          (get-in c2-artifact [:source-unit-record :path])))
             (let [path (get-in c2-artifact [:source-unit-record :path])
                   expected (sh04-syntax-reader-binding path c2-artifact
                              (sh04-syntax-semantic-source-id path (:source-unit-record c2-artifact)))]
               (and (= (:reader-binding expected) (:reader-semantic-binding boundary))
                    (= (:reader-source-revision expected) (:reader-source-revision boundary))))) true))))
(defn c3-reader-artifact-view
  [c2-artifact]
  (let [boundary (:gravity-reader-boundary c2-artifact)
        source-path (get-in c2-artifact [:source-unit-record :path])
        registry
        (sh04-syntax-registered-literal-registry source-path c2-artifact)
        semantic-source-id
        (sh04-syntax-semantic-source-id
         source-path (:source-unit-record c2-artifact))
        reader-authentication
        (sh04-syntax-reader-binding
         source-path c2-artifact semantic-source-id)
        projection-bindings (:bindings registry)
        projection-id
        (p15-s23-c6c10-canonical-digest
         source-path
         {:domain :gravity/sh04-registered-literal-projection-v1
          :bindings projection-bindings})
        projection
        {:artifact :gravity/sh04-registered-literal-projection
         :schema-version 1
         :projection-id projection-id
         :bindings projection-bindings
         :upstream-artifact-id (:artifact-id c2-artifact)
         :upstream-integrity-hash
         (get-in c2-artifact
                 [:reader-product-integrity :integrity-hash])
         :upstream-product-binding
         (sh04-syntax-current-sh03-product-binding c2-artifact)
         :reader-binding (:reader-binding reader-authentication)
         :reader-source-revision
         (:reader-source-revision reader-authentication)}
        base
        (assoc
         (select-keys
          c2-artifact
          [:kind :artifact-id :task :document-set :source-overrides
           :representation-boundary :capability-based-proof
           :source-unit-record :token-stream :form-tree
           :top-level-form-ids :parsed-semantic-values
           :syntax-seed-stream :reader-source-map
           :literal-decoding-records :semantic-error-deferment-record
           :reader-extension-invocation-records :reader-diagnostics
           :incremental-reader-hashes :reader-product-integrity])
         :sh03-reader-authentication
         (cond-> {:reader-result-id
          (get-in boundary
                  [:resolved-reader-result :incremental-reader-hashes
                   :reader-result])
          :semantic-envelope-id
          (get-in boundary
                  [:authenticated-envelope :semantic-envelope-id])
          :provenance-binding-id
          (get-in boundary
                  [:authenticated-envelope :provenance-binding-id])}
           (= :gravity/sh03-current-source-reader-plan-binding
              (get-in boundary [:plan-binding :artifact]))
           (assoc :reader-source-revision
                  (get-in boundary [:authenticated-envelope-descriptor :source-revision]))))]
    (if (seq projection-bindings)
      (sh04-syntax-project-registered-literal-values
       source-path registry
       (assoc base :registered-literal-projection projection))
      base)))

(declare c3-path-neutral-reader-artifact-view
         c3-path-neutral-syntax-object
         c3-gravity-syntax-boundary-identity-view
         c3-artifact-identity-input
         c3-artifact-id)

(defn- c3-artifact-identity-ops
  []
  {:c2-token-hash-input c2-token-hash-input
   :c2-form-hash-input c2-form-hash-input
   :c2-syntax-seed-hash-input c2-syntax-seed-hash-input
   :c2-extension-hash-input c2-extension-hash-input
   :c2-path-neutral-span c2-path-neutral-span
   :c3-path-neutral-origin c3-path-neutral-origin
   :reader-canonical-hash reader-canonical-hash
   :c3-path-neutral-reader-artifact-view c3-path-neutral-reader-artifact-view
   :c3-path-neutral-syntax-object c3-path-neutral-syntax-object
   :c3-gravity-syntax-boundary-identity-view
   c3-gravity-syntax-boundary-identity-view
   :c3-artifact-identity-input c3-artifact-identity-input
   :c3-artifact-id c3-artifact-id})
