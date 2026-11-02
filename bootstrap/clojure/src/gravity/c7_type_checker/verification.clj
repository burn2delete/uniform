(ns gravity.c7-type-checker.verification
  "Typed-core verification, capability evidence, and fail-closed C7 validation.")

(defn typed-core-verifier-report
  [nodes type-facts constraints functions dynamic cast generic dispatch schema layout]
  (let [node-ids (set (map :node-id nodes))
        typed-node-ids (set (map :core-node type-facts))
        all-typed? (= node-ids typed-node-ids)
        constraints-solved? (every? #(= :solved (:status %))
                                    (:constraints constraints))
        functions-have-effects? (every? #(contains? % :latent-effects)
                                        (:functions functions))
        casts-classified? (every? #(contains? % :classification)
                                  (:records cast))
        dynamic-profiled? (every? #(contains? % :profile) (:records dynamic))
        schema-preserved? (seq (:records schema))
        layout-recorded? (and (seq (:records layout))
                              (every? #(= :recorded (:status %))
                                      (:records layout)))
        origins-preserved? (every? #(get-in % [:source :syntax-id]) nodes)
        generic-solved? (= :complete (:status generic))
        dispatch-typed? (= :complete (:status dispatch))]
    {:artifact :gravity/c7-typed-core-verifier-report
     :every-node-typed-or-diagnostic? all-typed?
     :constraints-solved? constraints-solved?
     :function-latent-effects-present? functions-have-effects?
     :casts-classified? casts-classified?
     :dynamic-boundaries-profile-marked? dynamic-profiled?
     :schema-derived-types-preserve-identity? (boolean schema-preserved?)
     :layout-facts-recorded? layout-recorded?
     :generic-instantiations-solved? generic-solved?
     :protocol-dispatch-typed? dispatch-typed?
     :origins-preserved? origins-preserved?
     :status (if (and all-typed? constraints-solved?
                      functions-have-effects? casts-classified?
                      dynamic-profiled? schema-preserved? layout-recorded?
                      generic-solved? dispatch-typed? origins-preserved?)
               :passed
               :failed)}))

(defn type-capability-proof
  [diagnostic-ids artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:type-diagnostics :diagnostics])))
        verifier (:typed-core-verifier-report artifact)]
    {:every-core-node-has-type-or-diagnostic?
     (:every-node-typed-or-diagnostic? verifier)
     :constraints-solved?
     (:constraints-solved? verifier)
     :function-types-include-latent-effects?
     (:function-latent-effects-present? verifier)
     :dynamic-boundaries-profile-gated?
     (:dynamic-boundaries-profile-marked? verifier)
     :casts-classified?
     (:casts-classified? verifier)
     :generic-and-protocol-evidence?
     (and (:generic-instantiations-solved? verifier)
          (:protocol-dispatch-typed? verifier))
     :schema-identity-preserved?
     (:schema-derived-types-preserve-identity? verifier)
     :layout-facts-recorded?
     (:layout-facts-recorded? verifier)
     :diagnostics-covered?
     (= (set diagnostic-ids) diagnostics)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(defn validate!
  [type-capability-proof type-fail! source-path artifact]
  (let [proof (type-capability-proof artifact)]
    (doseq [[field id] [[:every-core-node-has-type-or-diagnostic?
                         "C7-TYPE-MISMATCH"]
                        [:constraints-solved? "C7-VERIFY"]
                        [:function-types-include-latent-effects?
                         "C7-VERIFY"]
                        [:dynamic-boundaries-profile-gated? "C7-DYNAMIC"]
                        [:casts-classified? "C7-CAST"]
                        [:generic-and-protocol-evidence? "C7-GENERIC"]
                        [:schema-identity-preserved? "C7-SCHEMA"]
                        [:layout-facts-recorded? "C7-LAYOUT"]
                        [:diagnostics-covered? "C7-VERIFY"]
                        [:verifier-passed? "C7-VERIFY"]]]
      (when-not (get proof field)
        (type-fail! id source-path {:stage :type-check}
                    {:missing-fields [field]}))))
  :complete)
