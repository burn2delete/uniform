

(defn p15-s23-c6c10-project-front-end
  [source-path raw-front-end]
  (let [raw-source-unit (:source-unit-record raw-front-end)
        source-content-hash (:bytes-hash raw-source-unit)
        literal-authentication
        (p15-s23-c6c10-literal-authentication source-path raw-front-end)
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
    projected))

(def p15-s23-c6c10-plan-projection-keys
  [:kind :compatibility-kind :diagnostics :plan-id :functions
   :effect-summary :source :binding-table :instruction-summary
   :module :compiler :entrypoint])

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