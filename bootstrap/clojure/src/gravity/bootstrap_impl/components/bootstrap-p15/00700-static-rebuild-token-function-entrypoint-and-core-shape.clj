(defn- __gravity_bootstrap_checked_core_function_entrypoint_and_core_shape [state]
  (let [{:syms
         [source-path
          source-text
          requested-target
          authority-record
          construction-mode
          static-execution-evidence
          static-rebuild-token-candidate
          source-byte-count
          source-content-hash
          early-module-products
          module-attempt
          _
          early-module
          authoritative-front-end
          authoritative-module
          authoritative-records
          namespace-record
          function-record
          namespace-subject
          function-subject
          executable-form-records
          executable-form-by-id
          source-surface-validation
          malformed-quote-record
          source-surface-subject
          early-metadata-bearing-form
          c7-source-violation
          unsupported-quote-literal-form
          unsupported-numeric-form
          invalid-str-arity-form
          preflight-stage2-rule
          preflight-driver-rule
          preflight-plan
          preflight-plan-validation
          packet-delay
          front-end
          fresh-front-end
          top-level-forms
          reader-products
          c2-artifact
          form-tree
          root-form-ids
          indexes
          token-stream
          token-ordinal-by-id
          plan]} state
        preflight-effect-requirements (p15-s23-closed-core-preflight-effect-requirements
                                        plan)
        entrypoint (:entrypoint plan)
        functions (:functions plan)
        definition (get functions entrypoint)
        declared-exports (vec (or (get-in plan [:module :exports]) []))
        entrypoint-binding (first
                             (filter #(= entrypoint (:name %)) (:binding-table plan)))
        entrypoint-visibility (:visibility entrypoint-binding)
        _ (when-not (and
                      (= #{entrypoint} (set (keys functions)))
                      (map? definition)
                      (zero? (:arity definition))
                      (empty? (:params definition))
                      (map? entrypoint-binding)
                      (= declared-exports (:exports authoritative-module))
                      (=
                        entrypoint-visibility
                        (if (seq declared-exports)
                          (if (contains? (set declared-exports) entrypoint)
                            :public
                            :private)
                          :stage2-local)))
            (p15-s23-closed-core-fail!
              "C6-CORE-SHAPE"
              source-path
              plan
              {:missing-fact :single-closed-entrypoint-function,
               :observed-functions (vec (sort-by str (keys functions)))}))
        source-function-records (filterv
                                  #(p15-s23-closed-core-function-form?
                                    entrypoint
                                    (:form %))
                                  (:records fresh-front-end))
        all-source-functions (filterv
                               #(let
                                 [form (:form %)]
                                 (or
                                   (and (seq? form) (= 'defn (first form)))
                                   (and
                                     (seq? form)
                                     (= 'def (first form))
                                     (seq? (nth form 2 nil))
                                     (= 'fn (first (nth form 2 nil))))))
                               (:records fresh-front-end))
        _ (when-not (and
                      (= 1 (count source-function-records))
                      (= 1 (count all-source-functions)))
            (p15-s23-closed-core-fail!
              "C6-CORE-SHAPE"
              source-path
              fresh-front-end
              {:missing-fact :single-source-entrypoint-function,
               :observed-function-count (count all-source-functions)}))
        root-record-wrapper (first source-function-records)
        root-form-id (:form-id root-record-wrapper)
        root-record (get-in indexes [:form-by-id root-form-id])
        metadata-bearing-form (first
                                (filter
                                  #(and
                                    (=
                                      root-form-id
                                      (p15-s23-closed-core-top-level-form-id
                                        (:form-id %)
                                        (:parent-by-id indexes)))
                                    (seq (:metadata %)))
                                  form-tree))
        _ (when metadata-bearing-form
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              (assoc
                metadata-bearing-form
                :syntax-id
                (get-in root-record-wrapper [:c3-syntax-object :syntax/id])
                :c2-form-id
                (:form-id metadata-bearing-form)
                :source-span
                (:span metadata-bearing-form)
                :generated-origin
                (vec
                  (concat
                    (or (get-in root-record-wrapper [:c3-syntax-object :origin]) [])
                    (or (:generated-origin metadata-bearing-form) [])))
                :lowering-rule
                :pure-closed-core-metadata-exclusion
                :profile
                :hosted
                :target
                :jvm)
              {:missing-fact :metadata-preserving-pure-core-lowering,
               :active-profile :hosted,
               :target :jvm,
               :target-neutral-request? true}))
        root-syntax (:c3-syntax-object root-record-wrapper)
        expanded-root-syntax (first
                               (filter
                                 #(= root-form-id (:form-id %))
                                 (:expanded-syntax-object-stream fresh-front-end)))
        _ (when-not (and
                      (=
                        (get root-syntax :syntax/id)
                        (get-in expanded-root-syntax [:c3-syntax-object :syntax/id]))
                      (=
                        (get root-syntax :syntax/id)
                        (get-in
                          fresh-front-end
                          [:macro-expansion-trace 0 :input-syntax-id])))
            (p15-s23-closed-core-fail!
              "C6-ORIGIN"
              source-path
              root-record
              {:missing-fact :generated-origin-input-syntax-closure}))
        function-shape (p15-s23-closed-core-function-source-shape
                         source-path
                         entrypoint
                         (:form-by-id indexes)
                         root-record)
        instructions (:instructions definition)
        body-form-ids (:body-form-ids function-shape)
        _ (when-not (= (count instructions) (count body-form-ids))
            (p15-s23-closed-core-fail!
              "C6-ORIGIN"
              source-path
              root-record
              {:missing-fact :entrypoint-body-lockstep,
               :instruction-count (count instructions),
               :body-form-count (count body-form-ids)}))
        validation preflight-plan-validation
        _ (when-not (and
                      (= :complete (:status validation))
                      (<= (:node-count validation) p15-s23-closed-core-max-plan-nodes))
            (p15-s23-closed-core-fail!
              "C6-VERIFY"
              source-path
              validation
              {:missing-fact :closed-plan-source-node-bound}))
        observed-operation-set (p15-s23-closed-core-observed-plan-operations plan)]
    (assoc
      state
      'preflight-effect-requirements
      preflight-effect-requirements
      'entrypoint
      entrypoint
      'functions
      functions
      'definition
      definition
      'declared-exports
      declared-exports
      'entrypoint-binding
      entrypoint-binding
      'entrypoint-visibility
      entrypoint-visibility
      '_
      _
      'source-function-records
      source-function-records
      'all-source-functions
      all-source-functions
      '_
      _
      'root-record-wrapper
      root-record-wrapper
      'root-form-id
      root-form-id
      'root-record
      root-record
      'metadata-bearing-form
      metadata-bearing-form
      '_
      _
      'root-syntax
      root-syntax
      'expanded-root-syntax
      expanded-root-syntax
      '_
      _
      'function-shape
      function-shape
      'instructions
      instructions
      'body-form-ids
      body-form-ids
      '_
      _
      'validation
      validation
      '_
      _
      'observed-operation-set
      observed-operation-set)))
