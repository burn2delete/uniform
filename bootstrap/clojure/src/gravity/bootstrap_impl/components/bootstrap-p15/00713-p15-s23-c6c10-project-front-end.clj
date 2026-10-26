

(defn p15-s23-c6c10-project-front-end
  ([source-path raw-front-end]
   (p15-s23-c6c10-project-front-end source-path raw-front-end nil))
  ([source-path raw-front-end invocation]
  (let [raw-source-unit (:source-unit-record raw-front-end)
        source-content-hash (:bytes-hash raw-source-unit)
        literal-authentication
        (p15-s23-c6c10-literal-authentication source-path raw-front-end invocation)
        private-identity
        (p15-s23-c6c10-private-source-identity source-path raw-source-unit)
        private-source-id (:private-source-id private-identity)
        source-unit-record
        (-> (select-keys raw-source-unit
                         p15-s23-c6c10-source-unit-projection-keys)
            (dissoc :source-id :identity-inputs))
        token-stream
        (mapv #(p15-s23-c6c10-private-token-record private-source-id %)
              (:token-stream raw-front-end))
        form-tree
        (mapv #(p15-s23-c6c10-private-form-record private-source-id %)
              (:form-tree raw-front-end))
        syntax-seeds
        (mapv #(p15-s23-c6c10-private-syntax-seed private-source-id %)
              (:syntax-seed-stream raw-front-end))
        reader-source-map
        (mapv #(p15-s23-c6c10-private-reader-source-record
                private-source-id %)
              (:reader-source-map raw-front-end))
        literal-records
        (mapv #(p15-s23-c6c10-private-literal-record private-source-id %)
              (:literal-decoding-records raw-front-end))
        deferment-record
        (-> (p15-s23-c6c10-path-neutral-value
             private-source-id (:semantic-error-deferment-record
                                raw-front-end))
            (update :deferred-literal-records
                    #(mapv (fn [record]
                             (p15-s23-c6c10-private-literal-record
                              private-source-id record))
                           (or % []))))
        extension-records
        (mapv #(p15-s23-c6c10-private-extension-record
                private-source-id %)
              (:reader-extension-invocation-records raw-front-end))
        diagnostics
        (mapv #(p15-s23-c6c10-private-diagnostic private-source-id %)
              (:reader-diagnostics raw-front-end))
        normalized
        {:token-stream token-stream
         :form-tree form-tree
         :top-level-form-ids (:top-level-form-ids raw-front-end)
         :syntax-seed-stream syntax-seeds
         :literal-decoding-records literal-records
         :semantic-error-deferment-record deferment-record
         :reader-extension-invocation-records extension-records
         :reader-diagnostics diagnostics}
        c2-derived
        (p15-s23-c6c10-private-c2-derived-products
         source-path literal-authentication source-unit-record
         private-source-id (:identity-inputs private-identity) normalized)
        integrity-hash
        (get-in c2-derived [:reader-product-integrity :integrity-hash])
        normalized-c3-syntaxes
        (mapv #(p15-s23-c6c10-normalize-private-c3-syntax
                private-source-id integrity-hash %)
              (:c3-syntax-object-stream raw-front-end))
        c3-rekeyed
        (p15-s23-c6c10-rekey-private-c3-syntaxes
         source-path literal-authentication normalized-c3-syntaxes)
        syntaxes (:syntax-object-stream c3-rekeyed)
        records
        (p15-s23-c6c10-private-front-end-records
         private-source-id (:records raw-front-end)
         token-stream form-tree syntaxes)
        capability-proof
        (p15-s23-c6c10-private-c3-capability-proof
         (:c3-capability-proof raw-front-end)
         (:top-level-form-ids raw-front-end) syntaxes)
        projected-base
        {:artifact (:artifact raw-front-end)
         :status (:status raw-front-end)
         :source-unit-record source-unit-record
         :source-unit-id private-source-id
         :token-stream token-stream
         :form-tree form-tree
         :top-level-form-ids (:top-level-form-ids raw-front-end)
         :syntax-seed-stream syntax-seeds
         :reader-source-map reader-source-map
         :literal-decoding-records literal-records
         :semantic-error-deferment-record deferment-record
         :reader-extension-invocation-records extension-records
         :reader-diagnostics diagnostics
         :incremental-reader-hashes
         (:incremental-reader-hashes c2-derived)
         :reader-product-integrity
         (:reader-product-integrity c2-derived)
         :c3-syntax-object-stream syntaxes
         :c3-capability-proof capability-proof
         :records records
         :forms (p15-s23-c6c10-path-neutral-value
                 private-source-id (:forms raw-front-end))}
        c3-artifact-authentication
        (p15-s23-c6c10-authentication-with-occurrences
         literal-authentication :private-c3-artifact
         (p15-s23-c6c10-private-c3-artifact-occurrences
          source-path literal-authentication projected-base))
        new-c3-artifact-id
        (p15-s23-c6c10-authenticated-semantic-digest
         source-path c3-artifact-authentication :private-c3-artifact
         {:domain :gravity/c6-c10-private-c3-artifact-v1
          :front-end projected-base})
        projected (assoc projected-base :c3-artifact-id new-c3-artifact-id)
        _ (p15-s23-c6c10-verify-private-front-end-projection!
           source-path raw-front-end literal-authentication projected)]
    projected)))

(def p15-s23-c6c10-plan-projection-keys
  [:kind :compatibility-kind :diagnostics :plan-id :functions
   :effect-summary :source :binding-table :instruction-summary
   :module :compiler :entrypoint])

(def p15-s23-c6c10-raw-plan-projection-keys
  [:kind :compiler-artifact-plan? :entrypoint :source :compiler :module
   :functions :instruction-summary :effect-summary :plan-id])

(declare sh06-resolution-source-artifact sh07-core-authenticated-request
         sh05-macro-artifact-from-c3-artifact sh06-resolution-artifact-from-sh05-artifact
         p15-s23-c6c10-resolved-parameter-row)

(defn p15-s23-c6c10-resolved-parameter
  ([source-path source-text front-end]
   (p15-s23-c6c10-resolved-parameter source-path source-text front-end nil))
  ([source-path source-text front-end c3-parent]
   (p15-s23-c6c10-resolved-parameter source-path source-text front-end c3-parent nil))
  ([source-path source-text front-end c3-parent invocation]
  ;; This existing resolver projection constructs no SH-07 Core. Its original
  ;; SH-06 replay and C2 occurrence bijection own the binding/source premise.
  (let [resolved (if (nil? c3-parent)
                   (do (when (some? invocation)
                         (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :explicit-reader-c3-parent {}))
                       (sh06-resolution-source-artifact source-path source-text))
                   (let [parent (if (some? invocation)
                                  (sh05-macro-artifact-from-c3-artifact source-path c3-parent invocation)
                                  (sh05-macro-artifact-from-c3-artifact source-path c3-parent))]
                     (if (some? invocation)
                       (sh06-resolution-artifact-from-sh05-artifact source-path parent
                         {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                         (:reader-source-path invocation) invocation)
                       (sh06-resolution-artifact-from-sh05-artifact source-path parent))))
        request (if (some? invocation) (sh07-core-authenticated-request resolved ::legacy invocation)
                    (sh07-core-authenticated-request resolved))
        forms (into {} (map (juxt :form-id identity)) (:forms request))
        root (get forms (first (:top-level-form-ids request)))
        function (get forms (get (:child-form-ids root) 2))
        parameters (get forms (get (:child-form-ids function) 1))
        projected (mapv forms (:child-form-ids parameters))
        sources (into {} (map (juxt :form-id identity)) (:form-tree front-end))
        source (get sources (second (:top-level-form-ids front-end)))
        original (get sources (get (:children source) 2))
        source-rows (mapv sources (:children original))
        bindings (:binding-table request)
        hash (get-in request [:lineage :source-revision-id])
        n (quot (count projected) 3)]
    (when-not (and (= hash (get-in front-end [:source-unit-record :bytes-hash]))
                  (= 1 (count (:top-level-form-ids request)))
                  (contains? #{1 2 3} n) (= (* 3 n) (count projected) (count source-rows))
                  (= :vector (:kind original)))
      (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path
                               :resolved-single-bool-parameter {}))
    (let [rows (mapv #(p15-s23-c6c10-resolved-parameter-row
                       source-path parameters original projected source-rows bindings (:resolution-table request) %)
                     (range n))
          base {:source-content-hash hash :sh06-artifact-id (:artifact-id resolved)
                :sh06-semantic-projection-id (get-in request [:lineage :sh06-semantic-projection-id])
                :projection-binding (:projection-binding request) :root-syntax-id (:syntax-id root)
                :function-syntax-id (:syntax-id function) :function-scope-id (:scope-id function)}]
      (when-not (and (= n (count (set (map #(get-in % [:parameter-binding :name]) rows))))
                     (= n (count (set (map #(get-in % [:parameter-binding :binding-id]) rows))))
                     (= (* 3 n) (count (set (map :form-id source-rows)))
                        (count (set (map :syntax-id projected))))
                     (apply = (map :type-binding rows)))
        (p15-s23-c6c10-host-fail! "C6-VERIFY" source-path :distinct-resolved-bool-parameters {}))
      (if (= n 1)
        (merge base {:kind (if (= 'I64 (get-in (first rows) [:type-binding :name]))
                            :gravity/c6-c10-resolved-i64-parameter-v1
                            :gravity/c6-c10-resolved-bool-parameter-v1)} (first rows))
        (assoc base :kind (if (= 'I64 (get-in (first rows) [:type-binding :name]))
                           :gravity/c6-c10-resolved-two-i64-parameters-v1
                           (if (= n 2) :gravity/c6-c10-resolved-two-bool-parameters-v1
                             :gravity/c6-c10-resolved-three-bool-parameters-v1))
               :type-binding (:type-binding (first rows))
               :parameters (mapv #(dissoc % :type-binding) rows)))))))

(def p15-s23-c6c10-function-projection-keys
  [:name :instructions :params :definition-form :binding
   :body-form-count :arity :body])

(def p15-s23-c6c10-binding-projection-keys
  [:capabilities :name :effects :kind :target
   :namespace :visibility :profile])

(defn p15-s23-c6c10-stage2-order-key
  [value]
  (binding [*print-length* nil
            *print-level* nil
            *print-meta* false
            *print-dup* false
            *print-readably* true
            *print-namespace-maps* false]
    (pr-str value)))

(defn p15-s23-c6c10-plan-shape-fail!
  [source-path facts]
  (p15-s23-c6c10-host-fail!
   "C6-ORIGIN" source-path
   :stage2-plan-c2-form-correspondence facts))

(defn p15-s23-c6c10-static-literal-instruction-value
  [instruction]
  (case (:op instruction)
    :literal {:static? true :value (:value instruction)}
    :map-literal
    (loop [entries (:entries instruction)
           result {}]
      (if-let [entry (first entries)]
        (let [key-result
              (p15-s23-c6c10-static-literal-instruction-value
               (:key entry))
              value-result
              (p15-s23-c6c10-static-literal-instruction-value
               (:value entry))]
          (if (and (:static? key-result) (:static? value-result)
                   (not (contains? result (:value key-result))))
            (recur (rest entries)
                   (assoc result (:value key-result) (:value value-result)))
            {:static? false}))
        {:static? true :value result}))
    {:static? false}))

(defn p15-s23-c6c10-authentic-deferred-ratio-instruction?
  [literal-authentication form-id instruction]
  (let [evidence
        (get (:deferred-ratio-by-form-id literal-authentication) form-id)
        static (p15-s23-c6c10-static-literal-instruction-value instruction)]
    (and evidence
         (= :map-literal (:op instruction))
         (true? (:static? static))
         (= (:descriptor evidence) (:value static)))))
