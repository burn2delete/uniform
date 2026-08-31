(defn- __gravity_bootstrap_closed_core_println_effect [ctx
                                                       instruction
                                                       form-record
                                                       path
                                                       env
                                                       depth
                                                       origin-products]
  (case
    (:op instruction)
    :println
    (let [args-product (p15-s23-closed-core-map-arguments
                         ctx
                         instruction
                         form-record
                         path
                         env
                         depth
                         origin-products)
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
                  :effect
                  :println
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
                {:missing-fact :closed-println-printable-operand,
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
          transcript-check (p15-s23-closed-core-transcript-delivery-check
                             (:source-content-hash ctx)
                             path
                             (:source origin-products))
          node (p15-s23-closed-core-node
                 (:source-content-hash ctx)
                 path
                 :effect
                 :println
                 true
                 depth
                 (mapv :node-id arg-root-nodes)
                 {:arity (count (:args instruction)),
                  :ordering :source-sequence,
                  :runtime-check-id (:check-id transcript-check)}
                 :gravity/nil
                 (conj (:effects args-product) :io/write)
                 (conj (:capabilities args-product) :io/stdout)
                 (p15-s23-closed-core-persistent-ownership
                   :stdout-result
                   {:storage :static-nil})
                 {:outcome :runtime-checked,
                  :check transcript-check,
                  :basis :closed-println-reference-transcript}
                 (:profile ctx)
                 (:source origin-products))]
      (p15-s23-closed-core-add-node args-product node origin-products))))
