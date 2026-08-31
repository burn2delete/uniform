(defn- __gravity_bootstrap_closed_core_builtin_call [ctx
                                                     instruction
                                                     form-record
                                                     path
                                                     env
                                                     depth
                                                     origin-products]
  (case
    (:op instruction)
    :builtin-call
    (let [args-product (p15-s23-closed-core-map-arguments
                         ctx
                         instruction
                         form-record
                         path
                         env
                         depth
                         origin-products)
          _ (when-not (contains? #{1 2} (count (:args instruction)))
              (p15-s23-closed-core-fail!
                "C7-TYPE-MISMATCH"
                (:source-path ctx)
                (p15-s23-closed-core-prospective-node-subject
                  ctx
                  path
                  :call
                  :str
                  origin-products
                  {:expected-type {:kind :arity, :allowed #{1 2}},
                   :actual-type {:kind :arity, :value (count (:args instruction))},
                   :relevant-binding-id :not-applicable})
                {:missing-fact :closed-str-arity,
                 :plan-path path,
                 :expected-type {:kind :arity, :allowed #{1 2}},
                 :actual-type {:kind :arity, :value (count (:args instruction))},
                 :relevant-binding-id :not-applicable}))
          arg-paths (mapv
                      #(p15-s23-closed-core-child-path path :args %)
                      (range (count (:args instruction))))
          node-by-path (into {} (map (juxt :path identity)) (:nodes args-product))
          arg-root-nodes (mapv #(get node-by-path %) arg-paths)
          _ (when-not (every?
                        #(p15-s23-closed-core-printable-type? (:type %))
                        arg-root-nodes)
              (p15-s23-closed-core-fail!
                "C7-TYPE-MISMATCH"
                (:source-path ctx)
                (p15-s23-closed-core-prospective-node-subject
                  ctx
                  path
                  :call
                  :str
                  origin-products
                  {:expected-type
                   {:kind :closed-printable-scalar,
                    :members
                    [:gravity/nil
                     :gravity/string
                     :gravity/bool
                     :gravity/char
                     :gravity/keyword
                     :gravity/symbol]},
                   :actual-type (mapv :type arg-root-nodes),
                   :relevant-binding-id
                   (or
                     (some #(get-in % [:attributes :resolved-binding]) arg-root-nodes)
                     :not-applicable)})
                {:missing-fact :closed-str-printable-operand,
                 :plan-path path,
                 :operand-types (mapv :type arg-root-nodes),
                 :expected-type
                 {:kind :closed-printable-scalar,
                  :members
                  [:gravity/nil
                   :gravity/string
                   :gravity/bool
                   :gravity/char
                   :gravity/keyword
                   :gravity/symbol]},
                 :actual-type (mapv :type arg-root-nodes),
                 :relevant-binding-id
                 (or
                   (some #(get-in % [:attributes :resolved-binding]) arg-root-nodes)
                   :not-applicable),
                 :excluded-types [:integer :non-integer-number :collection :opaque]}))
          allocation-check (p15-s23-closed-core-managed-allocation-check
                             (:source-content-hash ctx)
                             path
                             (:source origin-products))
          node (p15-s23-closed-core-node
                 (:source-content-hash ctx)
                 path
                 :call
                 :str
                 true
                 depth
                 (mapv :node-id arg-root-nodes)
                 {:function 'str,
                  :arity (count (:args instruction)),
                  :runtime-check-id (:check-id allocation-check)}
                 :gravity/string
                 (conj (:effects args-product) :memory/allocate)
                 (conj (:capabilities args-product) :memory/allocator)
                 (p15-s23-closed-core-persistent-ownership
                   :managed-string-result
                   {:storage :host-managed-string,
                    :provider-requirement :gravity.reference/jvm-managed-allocator,
                    :allocator-requirement :memory/allocator,
                    :provider-id :gravity.reference/jvm-managed-allocator,
                    :lifetime :managed-reachability})
                 {:outcome :runtime-checked,
                  :check allocation-check,
                  :basis :closed-str-managed-allocation}
                 (:profile ctx)
                 (:source origin-products))]
      (p15-s23-closed-core-add-node args-product node origin-products))))
