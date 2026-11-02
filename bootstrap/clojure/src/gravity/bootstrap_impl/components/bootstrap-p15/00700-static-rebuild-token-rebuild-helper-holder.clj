(def ^:private __gravity_bootstrap_checked_core_rebuild_helper
  (let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper]
  (fn p15-s23-stage2-closed-checked-core-rebuild-internal [context execution-evidence]
    (p15-s23-checked-core-bounded-context! context)
    (let [supported-context? (and
                               (map? context)
                               (contains?
                                 p15-s23-reference-runtime-supported-collection-class-names
                                 (some-> context class .getName))
                               (<= (count context) 5))]
      (when-not supported-context?
        (p15-s23-closed-core-fail!
          "C6-CORE-SHAPE"
          "<closed-core-context>"
          {}
          {:missing-fact :bounded-closed-core-source-context}))
      (try
        (p15-s23-reference-runtime-bounded-value!
          "p15-s23-closed-core-context"
          :jvm
          :checked-core-context
          context
          p15-s23-reference-runtime-max-contract-nodes
          p15-s23-reference-runtime-max-contract-depth)
        (catch
          Exception
          _
          (p15-s23-closed-core-fail!
            "C6-CORE-SHAPE"
            "<closed-core-context>"
            {}
            {:missing-fact :bounded-closed-core-source-context})))
      (p15-s23-closed-core-source-request-bounds!
        (:source-path context)
        (:source-text context)
        (:requested-target context))
      (let [pure-keys #{:source-content-hash
                        :requested-target
                        :source-text
                        :source-path}
            effectful-keys (conj pure-keys :authority-record)
            context-keys (set (keys context))
            effectful? (= effectful-keys context-keys)]
        (when-not (and
                    (contains? #{pure-keys effectful-keys} context-keys)
                    (string? (:source-path context))
                    (string? (:source-text context))
                    (keyword? (:requested-target context))
                    (=
                      (:source-content-hash context)
                      (str "sha256:" (sha256-hex (:source-text context))))
                    (if effectful?
                      (and
                        (p15-s23-checked-core-authority-record-valid?
                          (:authority-record context))
                        (map? execution-evidence))
                      (nil? execution-evidence)))
          (p15-s23-closed-core-fail!
            (if effectful? "C8-CAPABILITY" "C6-CORE-SHAPE")
            (:source-path context)
            context
            {:missing-fact
             (if effectful?
               :trusted-effectful-closed-core-context-and-execution-evidence
               :trusted-pure-closed-core-source-context)}))
        (if effectful?
          (p15-s23-stage2-closed-checked-core-source-artifact-internal
            (:source-path context)
            (:source-text context)
            (:requested-target context)
            (:authority-record context)
            :static-verification-rebuild
            execution-evidence
            static-rebuild-token)
          (p15-s23-stage2-closed-checked-core-source-artifact-internal
            (:source-path context)
            (:source-text context)
            (:requested-target context)
            nil
            :authoritative-artifact-construction
            nil
            nil)))))))
