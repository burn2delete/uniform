(defn p15-s23-closed-core-map-instruction [ctx
                                           instruction
                                           form-id
                                           path
                                           env
                                           depth
                                           generated-role
                                           base-origin-products]
  (when (> depth p15-s23-closed-core-max-plan-depth)
    (p15-s23-closed-core-fail!
      "C6-VERIFY"
      (:source-path ctx)
      instruction
      {:missing-fact :closed-core-plan-depth-bound,
       :observed-depth depth,
       :maximum-depth p15-s23-closed-core-max-plan-depth,
       :plan-path path}))
  (let [form-record (get-in ctx [:indexes :form-by-id form-id])
        origin-products (if generated-role
                          (p15-s23-closed-core-generated-origin-products
                            (:source-path ctx)
                            (:source-content-hash ctx)
                            path
                            base-origin-products
                            generated-role)
                          (p15-s23-closed-core-origin-products
                            (:source-path ctx)
                            (:source-content-hash ctx)
                            path
                            form-record
                            (:root-syntax ctx)
                            (:expanded-root-syntax ctx)
                            (:indexes ctx)
                            (:token-ordinal-by-id ctx)
                            nil))
        _ (p15-s23-closed-core-assert-plan-form!
            (:source-path ctx)
            path
            instruction
            form-record
            generated-role
            origin-products)
        op (:op instruction)]
    (when-let [shadowed-builtin (cond
                                  (and
                                    (= :builtin-call op)
                                    (contains? env (:function instruction))) (:function
                                                                               instruction)
                                  (and (= :println op) (contains? env 'println)) 'println
                                  :else nil)]
      (p15-s23-closed-core-fail!
        "C6-LOWERING-GAP"
        (:source-path ctx)
        (assoc
          form-record
          :syntax-id
          (get-in origin-products [:raw :c3-syntax-id])
          :c2-form-id
          (get-in origin-products [:raw :c2-form-id])
          :source-span
          (get-in origin-products [:raw :c2-span])
          :generated-origin
          (vec
            (concat
              (or (get-in origin-products [:raw :c2-reader-generated-origin]) [])
              (or (get-in origin-products [:raw :c3-origin]) [])
              (or (get-in origin-products [:raw :expanded-generated-origin]) [])))
          :lowering-rule
          :lexical-binding-precedes-builtin
          :profile
          (:profile ctx)
          :source-target
          (:source-target ctx)
          :requested-target
          (:requested-target ctx)
          :target
          (:requested-target ctx))
        {:missing-fact :resolved-shadowed-builtin-call-lowering,
         :plan-path path,
         :binding shadowed-builtin,
         :active-profile (:profile ctx),
         :source-target (:source-target ctx),
         :requested-target (:requested-target ctx),
         :target (:requested-target ctx),
         :target-neutral-request? true}))
    (case
      op
      :literal
      (__gravity_bootstrap_closed_core_literal_quote_and_local
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :quote
      (__gravity_bootstrap_closed_core_literal_quote_and_local
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :local
      (__gravity_bootstrap_closed_core_literal_quote_and_local
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :builtin-call
      (__gravity_bootstrap_closed_core_builtin_call
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :println
      (__gravity_bootstrap_closed_core_println_effect
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :do
      (__gravity_bootstrap_closed_core_sequence_and_control
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :if
      (__gravity_bootstrap_closed_core_sequence_and_control
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products)
      :let
      (__gravity_bootstrap_closed_core_sequence_and_control
        ctx
        instruction
        form-record
        path
        env
        depth
        origin-products))))
