

(defn stage1-reader-execute-verified-boot-chain
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
   boot-chain]
  (let [boot-chain-id (:verified-boot-chain-id boot-chain)
        boot-chain-operations (:boot-chain-operations boot-chain)
        annotate-with-boot-chain
        (fn [record]
          (assoc record
                 :verified-boot-chain-id boot-chain-id
                 :verified-boot-chain-engine (:engine boot-chain)
                 :verified-boot-chain
                 :gravity-reader-verified-boot-chain-v1))
        records
        (stage1-reader-execute-runtime-image
         reader-source-path
         definitions
         source-path
         source-text
         self-hosted-runtime
         core-bootstrap-runtime
         core-bootstrap-builtins
         compiler-driver
         runtime-entrypoint
         runtime-image)
        annotated-records (mapv annotate-with-boot-chain records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (let [prior-machine-boundaries (:machine-boundaries trace)]
                 (-> (or trace {})
                     (assoc :verified-boot-chain boot-chain)
                     (assoc :verified-boot-chain-operation-coverage
                            {:required
                             stage1-reader-verified-boot-chain-required-operations
                             :provided boot-chain-operations
                             :covered?
                             (set/subset?
                              (set stage1-reader-verified-boot-chain-required-operations)
                              (set boot-chain-operations))})
                     (assoc :replaced-machine-boundaries
                            prior-machine-boundaries)
                     (assoc :machine-boundaries
                            (:machine-boundaries boot-chain))
                     (assoc :trust-anchor-boundaries
                            (:trust-anchor-boundaries boot-chain))
                     (assoc :boot-chain-fallbacks
                            (:boot-chain-fallbacks boot-chain))
                     (assoc :verified-boot-chain-artifact-routing
                            {:artifact (:artifact boot-chain)
                             :diagnostic-stream
                             (:diagnostic-stream boot-chain)
                             :proof-kind (:proof-kind boot-chain)})
                     (update :gravity-runtimes (fnil conj [])
                             :stage1-reader-verified-boot-chain)
                     (update :character-stream annotate-with-boot-chain)
                     (update :token-stream annotate-with-boot-chain)
                     (assoc :verified-boot-chain-applied
                            {:verified-boot-chain-id boot-chain-id
                             :operation-count
                             (count boot-chain-operations)
                             :replaced-machine-boundaries
                             prior-machine-boundaries
                             :trust-anchor-boundaries
                             (:trust-anchor-boundaries boot-chain)
                             :boot-chain-fallbacks
                             (:boot-chain-fallbacks boot-chain)}))))))
    annotated-records))

(defn stage1-reader-execute-verified-boot-chain-pipeline
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
         reader-source-path definitions)]
    (when-not (stage1-reader-verified-boot-chain-entrypoint-valid?
               definitions)
      (stage1-reader-verified-boot-chain-fail!
       "STAGE1BOOT001" reader-source-path
       stage1-reader-verified-boot-chain-entrypoint
       {:missing-fields [stage1-reader-verified-boot-chain-entrypoint]}))
    {:records
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
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver
     :runtime-entrypoint runtime-entrypoint
     :runtime-image runtime-image
     :verified-boot-chain boot-chain}))

(defn stage1-reader-verified-boot-chain-diagnostic-stream
  [source-path boot-chain-id]
  {:artifact :gravity/stage1-reader-verified-boot-chain-diagnostic-stream
   :stage :stage1-reader-verified-boot-chain
   :source-path source-path
   :verified-boot-chain-id boot-chain-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-verified-boot-chain
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-verified-boot-chain-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-verified-boot-chain-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})