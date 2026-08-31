

(defn stage1-reader-execute-diverse-bootstrap-verification
  [reader-source-path
   definitions
   source-path
   source-text
   self-hosted-runtime
   core-bootstrap-runtime
   core-bootstrap-builtins
   compiler-driver
   runtime-entrypoint
   runtime-image
   boot-chain
   diverse-verification]
  (let [diverse-verification-id
        (:diverse-bootstrap-verification-id diverse-verification)
        diverse-operations
        (:diverse-verification-operations diverse-verification)
        annotate-with-diverse-verification
        (fn [record]
          (assoc record
                 :diverse-bootstrap-verification-id
                 diverse-verification-id
                 :diverse-bootstrap-verification-engine
                 (:engine diverse-verification)
                 :diverse-bootstrap-verification
                 :gravity-reader-diverse-bootstrap-verification-v1))
        records
        (stage1-reader-execute-verified-boot-chain
         reader-source-path
         definitions
         source-path
         source-text
         self-hosted-runtime
         core-bootstrap-runtime
         core-bootstrap-builtins
         compiler-driver
         runtime-entrypoint
         runtime-image
         boot-chain)
        annotated-records
        (mapv annotate-with-diverse-verification records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (let [prior-trust-anchor-boundaries
                     (:trust-anchor-boundaries trace)]
                 (-> (or trace {})
                     (assoc :diverse-bootstrap-verification
                            diverse-verification)
                     (assoc :diverse-bootstrap-verification-operation-coverage
                            {:required
                             stage1-reader-diverse-bootstrap-verification-required-operations
                             :provided diverse-operations
                             :covered?
                             (set/subset?
                              (set stage1-reader-diverse-bootstrap-verification-required-operations)
                              (set diverse-operations))})
                     (assoc :replaced-trust-anchor-boundaries
                            prior-trust-anchor-boundaries)
                     (assoc :trust-anchor-boundaries
                            (:trust-anchor-boundaries diverse-verification))
                     (assoc :residual-trust-boundaries
                            (:residual-trust-boundaries diverse-verification))
                     (assoc :diverse-verification-fallbacks
                            (:diverse-verification-fallbacks
                             diverse-verification))
                     (assoc :independent-toolchains
                            (:independent-toolchains diverse-verification))
                     (assoc :bootstrap-trace-comparisons
                            (:bootstrap-trace-comparisons
                             diverse-verification))
                     (assoc :reproducible-build-evidence
                            (:reproducible-build-evidence
                             diverse-verification))
                     (assoc :independent-audit-record
                            (:independent-audit-record diverse-verification))
                     (assoc :diverse-bootstrap-verification-artifact-routing
                            {:artifact (:artifact diverse-verification)
                             :diagnostic-stream
                             (:diagnostic-stream diverse-verification)
                             :proof-kind (:proof-kind diverse-verification)})
                     (update :gravity-runtimes (fnil conj [])
                             :stage1-reader-diverse-bootstrap-verification)
                     (update :character-stream
                             annotate-with-diverse-verification)
                     (update :token-stream
                             annotate-with-diverse-verification)
                     (assoc :diverse-bootstrap-verification-applied
                            {:diverse-bootstrap-verification-id
                             diverse-verification-id
                             :operation-count (count diverse-operations)
                             :replaced-trust-anchor-boundaries
                             prior-trust-anchor-boundaries
                             :residual-trust-boundaries
                             (:residual-trust-boundaries
                              diverse-verification)
                             :diverse-verification-fallbacks
                             (:diverse-verification-fallbacks
                              diverse-verification)}))))))
    annotated-records))

(defn stage1-reader-execute-diverse-bootstrap-verification-pipeline
  [reader-source-path source-path source-text]
  (let [forms (stage1-reader-source-forms reader-source-path)
        definitions (stage1-reader-definition-map reader-source-path forms)
        self-hosted-runtime
        (stage1-reader-self-hosted-runtime-from-definitions
         reader-source-path definitions)
        core-bootstrap-builtins
        (stage1-reader-core-bootstrap-builtins-from-definitions
         reader-source-path definitions)
        core-bootstrap-runtime
        (stage1-reader-core-bootstrap-runtime-from-definitions
         reader-source-path definitions)
        compiler-driver
        (stage1-reader-compiler-driver-from-definitions
         reader-source-path definitions)
        runtime-entrypoint
        (stage1-reader-runtime-entrypoint-from-definitions
         reader-source-path definitions)
        runtime-image
        (stage1-reader-runtime-image-from-definitions
         reader-source-path definitions)
        boot-chain
        (stage1-reader-verified-boot-chain-from-definitions
         reader-source-path definitions)
        diverse-verification
        (stage1-reader-diverse-bootstrap-verification-from-definitions
         reader-source-path definitions)]
    (when-not (stage1-reader-diverse-bootstrap-verification-entrypoint-valid?
               definitions)
      (stage1-reader-diverse-bootstrap-verification-fail!
       "STAGE1DIV001" reader-source-path
       stage1-reader-diverse-bootstrap-verification-entrypoint
       {:missing-fields
        [stage1-reader-diverse-bootstrap-verification-entrypoint]}))
    {:records
     (stage1-reader-execute-diverse-bootstrap-verification
      reader-source-path
      definitions
      source-path
      source-text
      self-hosted-runtime
      core-bootstrap-runtime
      core-bootstrap-builtins
      compiler-driver
      runtime-entrypoint
      runtime-image
      boot-chain
      diverse-verification)
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver
     :runtime-entrypoint runtime-entrypoint
     :runtime-image runtime-image
     :verified-boot-chain boot-chain
     :diverse-bootstrap-verification diverse-verification}))

(defn stage1-reader-diverse-bootstrap-verification-diagnostic-stream
  [source-path diverse-verification-id]
  {:artifact
   :gravity/stage1-reader-diverse-bootstrap-verification-diagnostic-stream
   :stage :stage1-reader-diverse-bootstrap-verification
   :source-path source-path
   :diverse-bootstrap-verification-id diverse-verification-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-diverse-bootstrap-verification
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-diverse-bootstrap-verification-diagnostic-messages
                 id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-diverse-bootstrap-verification-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})