(defn- __gravity_bootstrap_checked_core_c7_literal_and_arity_admission [state]
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
          early-metadata-bearing-form]} state
        c7-source-violation (p15-s23-closed-core-first-c7-source-violation
                              executable-form-records)
        unsupported-quote-literal-form (when (=
                                               :quoted-value
                                               (:kind c7-source-violation))
                                         (:record c7-source-violation))
        _ (when unsupported-quote-literal-form
            (let [value (:value unsupported-quote-literal-form)
                  quoted-value (second value)
                  actual-type (p15-s23-closed-core-quoted-value-type quoted-value)
                  prospective-node-id (p15-s23-closed-core-digest
                                        {:source-content-hash source-content-hash,
                                         :c2-form-id
                                         (:form-id unsupported-quote-literal-form),
                                         :prospective-operation :quote,
                                         :pass :c7-pure-quoted-scalar-classification})]
              (p15-s23-closed-core-fail!
                "C7-TYPE-MISMATCH"
                source-path
                (merge
                  function-subject
                  unsupported-quote-literal-form
                  {:c2-form-id (:form-id unsupported-quote-literal-form),
                   :prospective-core-node-id? true,
                   :source-span (:span unsupported-quote-literal-form),
                   :operation-id prospective-node-id,
                   :syntax-id (:syntax-id function-subject),
                   :expected-type p15-s23-closed-core-quoted-scalar-type,
                   :relevant-binding-id :not-applicable,
                   :core-node-id prospective-node-id,
                   :actual-type actual-type,
                   :generated-origin
                   (vec
                     (concat
                       (:generated-origin function-subject)
                       (or (:generated-origin unsupported-quote-literal-form) [])))})
                {:missing-fact :pure-closed-quoted-scalar,
                 :expected-type p15-s23-closed-core-quoted-scalar-type,
                 :actual-type actual-type,
                 :offending-reader-origin (:origin unsupported-quote-literal-form),
                 :offending-generated-origin
                 (vec (or (:generated-origin unsupported-quote-literal-form) [])),
                 :relevant-binding-id :not-applicable})))
        unsupported-numeric-form (when (= :numeric-literal (:kind c7-source-violation))
                                   (:record c7-source-violation))
        _ (when unsupported-numeric-form
            (let [value (:value unsupported-numeric-form)
                  prospective-node-id (p15-s23-closed-core-digest
                                        {:source-content-hash source-content-hash,
                                         :c2-form-id
                                         (:form-id unsupported-numeric-form),
                                         :prospective-operation :literal,
                                         :pass :c7-pure-scalar-classification})]
              (p15-s23-closed-core-fail!
                "C7-TYPE-MISMATCH"
                source-path
                (merge
                  function-subject
                  unsupported-numeric-form
                  {:c2-form-id (:form-id unsupported-numeric-form),
                   :prospective-core-node-id? true,
                   :source-span (:span unsupported-numeric-form),
                   :operation-id prospective-node-id,
                   :syntax-id (:syntax-id function-subject),
                   :expected-type :gravity/integer,
                   :relevant-binding-id :not-applicable,
                   :core-node-id prospective-node-id,
                   :actual-type
                   (cond
                     (and
                       (map? value)
                       (= :gravity/deferred-ratio-literal (:artifact value))) :gravity/deferred-ratio
                     (ratio? value) :gravity/ratio
                     :else :gravity/noninteger-number)})
                {:missing-fact :pure-closed-integer-numeric-scalar,
                 :expected-type :gravity/integer,
                 :actual-type
                 (cond
                   (and
                     (map? value)
                     (= :gravity/deferred-ratio-literal (:artifact value))) :gravity/deferred-ratio
                   (ratio? value) :gravity/ratio
                   :else :gravity/noninteger-number),
                 :relevant-binding-id :not-applicable})))
        invalid-str-arity-form (when (= :str-arity (:kind c7-source-violation))
                                 (:record c7-source-violation))
        _ (when invalid-str-arity-form
            (let [value (:value invalid-str-arity-form)
                  actual-arity (dec (count value))
                  prospective-node-id (p15-s23-closed-core-digest
                                        {:source-content-hash source-content-hash,
                                         :c2-form-id (:form-id invalid-str-arity-form),
                                         :prospective-operation :str,
                                         :pass :c7-pure-str-arity})]
              (p15-s23-closed-core-fail!
                "C7-TYPE-MISMATCH"
                source-path
                (merge
                  function-subject
                  invalid-str-arity-form
                  {:c2-form-id (:form-id invalid-str-arity-form),
                   :prospective-core-node-id? true,
                   :source-span (:span invalid-str-arity-form),
                   :operation-id prospective-node-id,
                   :syntax-id (:syntax-id function-subject),
                   :expected-type {:kind :arity, :allowed #{1 2}},
                   :relevant-binding-id :not-applicable,
                   :core-node-id prospective-node-id,
                   :actual-type {:kind :arity, :value actual-arity}})
                {:missing-fact :closed-str-arity,
                 :expected-type {:kind :arity, :allowed #{1 2}},
                 :actual-type {:kind :arity, :value actual-arity},
                 :relevant-binding-id :not-applicable})))]
    (assoc
      state
      'c7-source-violation
      c7-source-violation
      'unsupported-quote-literal-form
      unsupported-quote-literal-form
      '_
      _
      'unsupported-numeric-form
      unsupported-numeric-form
      '_
      _
      'invalid-str-arity-form
      invalid-str-arity-form
      '_
      _)))
