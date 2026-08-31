(defn- __gravity_bootstrap_checked_core_source_request_and_module_admission [state]
  (let [{:syms
         [source-path
          source-text
          requested-target
          authority-record
          construction-mode
          static-execution-evidence
          static-rebuild-token-candidate]} state
        source-byte-count (p15-s23-closed-core-source-request-bounds!
                            source-path
                            source-text
                            requested-target)
        source-content-hash (str "sha256:" (sha256-hex source-text))
        early-module-products (p15-s23-closed-core-early-module-products
                                source-path
                                source-text
                                requested-target)
        module-attempt (:module-attempt early-module-products)
        _ (when-not (= :valid (:status module-attempt))
            (p15-s23-closed-core-fail!
              "C6-CORE-SHAPE"
              source-path
              (:subject early-module-products)
              {:missing-fact :pure-closed-module-source-shape,
               :observed-legacy-module-reason (:legacy-diagnostic-id module-attempt)}))
        early-module (:module module-attempt)
        _ (when-not (= :jvm (:target early-module))
            (p15-s23-closed-core-fail!
              "C6-LOWERING-GAP"
              source-path
              (:subject early-module-products)
              {:missing-fact :pure-closed-slice-jvm-source-target,
               :observed-source-target (:target early-module),
               :accepted-source-target :jvm,
               :requested-target requested-target}))
        authoritative-front-end (p15-s23-stage2-c2-c3-front-end-products
                                  source-path
                                  source-text)]
    (assoc
      state
      'source-byte-count
      source-byte-count
      'source-content-hash
      source-content-hash
      'early-module-products
      early-module-products
      'module-attempt
      module-attempt
      '_
      _
      'early-module
      early-module
      '_
      _
      'authoritative-front-end
      authoritative-front-end)))
