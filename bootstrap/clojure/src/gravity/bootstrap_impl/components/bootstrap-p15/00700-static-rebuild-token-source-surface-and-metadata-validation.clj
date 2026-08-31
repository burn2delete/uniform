(defn- __gravity_bootstrap_checked_core_source_surface_and_metadata_validation [state]
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
          source-surface-validation]} state
        malformed-quote-record (when (=
                                       :pure-quote-source-arity
                                       (:missing-fact source-surface-validation))
                                 (get
                                   executable-form-by-id
                                   (:offending-form-id source-surface-validation)))
        source-surface-subject (if malformed-quote-record
                                 (merge
                                   function-subject
                                   malformed-quote-record
                                   {:syntax-id (:syntax-id function-subject),
                                    :c2-form-id (:form-id malformed-quote-record),
                                    :source-span (:span malformed-quote-record),
                                    :generated-origin
                                    (vec
                                      (concat
                                        (:generated-origin function-subject)
                                        (or
                                          (:generated-origin malformed-quote-record)
                                          []))),
                                    :lowering-rule :pure-quote-source-arity})
                                 function-subject)
        _ (when-not (= :passed (:status source-surface-validation))
            (p15-s23-closed-core-fail!
              (if (= :over-limit (:status source-surface-validation))
                "C6-VERIFY"
                "C6-LOWERING-GAP")
              source-path
              source-surface-subject
              (merge
                source-surface-validation
                {:requested-target requested-target}
                (when malformed-quote-record
                  {:offending-reader-origin (:origin malformed-quote-record),
                   :offending-generated-origin
                   (vec (or (:generated-origin malformed-quote-record) []))}))))
        _ (when-not (= :hosted (:profile authoritative-module))
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              namespace-subject
              {:missing-fact :pure-closed-slice-hosted-profile,
               :observed-profile (:profile authoritative-module),
               :accepted-profile :hosted}))
        _ (when-not (= :safe (:safety authoritative-module))
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              namespace-subject
              {:missing-fact :pure-closed-slice-safe-mode,
               :observed-safety (:safety authoritative-module),
               :accepted-safety :safe}))
        _ (when-not (= :jvm (:target authoritative-module))
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              namespace-subject
              {:missing-fact :pure-closed-slice-jvm-source-target,
               :observed-source-target (:target authoritative-module),
               :accepted-source-target :jvm,
               :requested-target requested-target}))
        _ (when-not (and
                      (empty? (:requires authoritative-module))
                      (empty? (:imports authoritative-module))
                      (empty? (:providers authoritative-module))
                      (= {} (:metadata authoritative-module))
                      (nil? (:doc authoritative-module)))
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              namespace-subject
              {:missing-fact :closed-slice-module-dependency-closure,
               :excluded-nonempty-fields
               [:requires :imports :providers :metadata :doc]}))
        _ (when-not (contains? #{['main] []} (:exports authoritative-module))
            (p15-s23-closed-core-fail!
              "C6-CORE-SHAPE"
              source-path
              namespace-subject
              {:missing-fact :exact-closed-slice-entrypoint-export,
               :allowed-exports [[] ['main]],
               :observed-exports (:exports authoritative-module)}))
        early-metadata-bearing-form (first
                                      (filter
                                        #(seq (:metadata %))
                                        (:form-tree authoritative-front-end)))
        _ (when early-metadata-bearing-form
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              (merge
                function-subject
                early-metadata-bearing-form
                {:c2-form-id (:form-id early-metadata-bearing-form),
                 :source-span (:span early-metadata-bearing-form),
                 :generated-origin
                 (vec
                   (concat
                     (:generated-origin function-subject)
                     (or (:generated-origin early-metadata-bearing-form) []))),
                 :lowering-rule :pure-closed-core-metadata-exclusion})
              {:missing-fact :metadata-preserving-pure-core-lowering,
               :active-profile :hosted,
               :source-target :jvm,
               :requested-target requested-target}))]
    (assoc
      state
      'malformed-quote-record
      malformed-quote-record
      'source-surface-subject
      source-surface-subject
      '_
      _
      '_
      _
      '_
      _
      '_
      _
      '_
      _
      '_
      _
      'early-metadata-bearing-form
      early-metadata-bearing-form
      '_
      _)))
