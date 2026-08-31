

(defn stage1-reader-execute-runtime-image
  [reader-source-path
   definitions
   source-path
   source-text
   self-hosted-runtime
   core-bootstrap-runtime
   core-bootstrap-builtins
   compiler-driver
   runtime-entrypoint
   runtime-image]
  (let [runtime-image-id (:runtime-image-id runtime-image)
        runtime-image-operations (:runtime-image-operations runtime-image)
        annotate-with-runtime-image
        (fn [record]
          (assoc record
                 :runtime-image-id runtime-image-id
                 :runtime-image-engine (:engine runtime-image)
                 :bootstrapped-runtime-image
                 :gravity-reader-runtime-image-v1))
        records
        (stage1-reader-execute-runtime-entrypoint
         reader-source-path
         definitions
         source-path
         source-text
         self-hosted-runtime
         core-bootstrap-runtime
         core-bootstrap-builtins
         compiler-driver
         runtime-entrypoint)
        annotated-records (mapv annotate-with-runtime-image records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (let [prior-os-boundaries (:os-boundaries trace)]
                 (-> (or trace {})
                     (assoc :runtime-image runtime-image)
                     (assoc :runtime-image-operation-coverage
                            {:required
                             stage1-reader-runtime-image-required-operations
                             :provided runtime-image-operations
                             :covered?
                             (set/subset?
                              (set stage1-reader-runtime-image-required-operations)
                              (set runtime-image-operations))})
                     (assoc :replaced-os-boundaries prior-os-boundaries)
                     (assoc :os-boundaries (:os-boundaries runtime-image))
                     (assoc :machine-boundaries
                            (:machine-boundaries runtime-image))
                     (assoc :image-fallbacks
                            (:image-fallbacks runtime-image))
                     (assoc :runtime-image-artifact-routing
                            {:artifact (:artifact runtime-image)
                             :diagnostic-stream
                             (:diagnostic-stream runtime-image)
                             :proof-kind (:proof-kind runtime-image)})
                     (update :gravity-runtimes (fnil conj [])
                             :stage1-reader-runtime-image)
                     (update :character-stream annotate-with-runtime-image)
                     (update :token-stream annotate-with-runtime-image)
                     (assoc :runtime-image-applied
                            {:runtime-image-id runtime-image-id
                             :operation-count
                             (count runtime-image-operations)
                             :replaced-os-boundaries prior-os-boundaries
                             :machine-boundaries
                             (:machine-boundaries runtime-image)
                             :image-fallbacks
                             (:image-fallbacks runtime-image)}))))))
    annotated-records))

(defn stage1-reader-execute-runtime-image-pipeline
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
         reader-source-path definitions)]
    (when-not (stage1-reader-runtime-image-entrypoint-valid? definitions)
      (stage1-reader-runtime-image-fail!
       "STAGE1IMG001" reader-source-path
       stage1-reader-runtime-image-entrypoint
       {:missing-fields [stage1-reader-runtime-image-entrypoint]}))
    {:records
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
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver
     :runtime-entrypoint runtime-entrypoint
     :runtime-image runtime-image}))

(defn stage1-reader-runtime-image-diagnostic-stream
  [source-path runtime-image-id]
  {:artifact :gravity/stage1-reader-runtime-image-diagnostic-stream
   :stage :stage1-reader-runtime-image
   :source-path source-path
   :runtime-image-id runtime-image-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-runtime-image
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-runtime-image-diagnostic-messages id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat stage1-reader-runtime-image-diagnostic-ids
                 (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})