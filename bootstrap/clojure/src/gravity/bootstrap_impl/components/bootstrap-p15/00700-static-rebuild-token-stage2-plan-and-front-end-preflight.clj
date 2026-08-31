(defn- __gravity_bootstrap_checked_core_stage2_plan_and_front_end_preflight [state]
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
          invalid-str-arity-form]} state
        preflight-stage2-rule (c-backend-stage2-plan-emitter-source-rule!
                                source-path
                                requested-target)
        preflight-driver-rule (c-backend-stage2-compiler-driver-source-rule!
                                source-path
                                requested-target)
        preflight-plan (binding [*additional-bootstrap-targets* stage2-runtime-derived-source-targets]
                         (p15-s23-stage2-plan-emitter-compile-source
                           (:emitter preflight-stage2-rule)
                           source-path
                           source-text))
        preflight-plan-validation (p15-s23-closed-runtime-plan-validation!
                                    source-path
                                    requested-target
                                    preflight-plan)
        packet-delay (delay
                       (stage2-runtime-derived-packet
                         source-path
                         source-text
                         requested-target))
        front-end (:front-end preflight-driver-rule)
        fresh-front-end (p15-s23-stage2-front-end-source-module-record
                          front-end
                          source-path
                          source-text)
        top-level-forms (:forms fresh-front-end)
        _ (when-not (and
                      (= 2 (count top-level-forms))
                      (seq? (first top-level-forms))
                      (= 'ns (ffirst top-level-forms))
                      (p15-s23-closed-core-function-form?
                        (:entrypoint preflight-plan)
                        (second top-level-forms)))
            (p15-s23-closed-core-fail!
              "C6-CORE-SHAPE"
              source-path
              fresh-front-end
              {:missing-fact :closed-slice-exhaustive-top-level-lowering,
               :required-top-level-shape [:ns :single-entrypoint-defn],
               :observed-top-level-count (count top-level-forms)}))
        reader-products (:reader-products fresh-front-end)
        c2-artifact (:c2-reader-artifact reader-products)
        form-tree (:form-tree fresh-front-end)
        root-form-ids (:top-level-form-ids fresh-front-end)
        indexes (p15-s23-closed-core-form-indexes source-path form-tree root-form-ids)
        token-stream (:token-stream fresh-front-end)
        token-ordinal-by-id (into
                              {}
                              (map-indexed (fn [idx token] [(:token-id token) idx]))
                              token-stream)
        plan preflight-plan]
    (assoc
      state
      'preflight-stage2-rule
      preflight-stage2-rule
      'preflight-driver-rule
      preflight-driver-rule
      'preflight-plan
      preflight-plan
      'preflight-plan-validation
      preflight-plan-validation
      'packet-delay
      packet-delay
      'front-end
      front-end
      'fresh-front-end
      fresh-front-end
      'top-level-forms
      top-level-forms
      '_
      _
      'reader-products
      reader-products
      'c2-artifact
      c2-artifact
      'form-tree
      form-tree
      'root-form-ids
      root-form-ids
      'indexes
      indexes
      'token-stream
      token-stream
      'token-ordinal-by-id
      token-ordinal-by-id
      'plan
      plan)))
