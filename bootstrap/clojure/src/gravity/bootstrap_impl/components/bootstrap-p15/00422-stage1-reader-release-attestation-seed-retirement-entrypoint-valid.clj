

(defn stage1-reader-release-attestation-seed-retirement-entrypoint-valid?
  [definitions]
  (let [definition
        (get definitions
             stage1-reader-release-attestation-seed-retirement-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-release-attestation-seed-retirement
               source-path
               source-text
               stage1-reader-release-attestation-seed-retirement
               stage1-reader-diverse-bootstrap-verification
               stage1-reader-verified-boot-chain))
            (:body definition)))))

(defn stage1-reader-execute-release-attestation-seed-retirement
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
   diverse-verification
   release-attestation]
  (let [release-attestation-id
        (:release-attestation-seed-retirement-id release-attestation)
        release-operations
        (:release-attestation-operations release-attestation)
        annotate-with-release-attestation
        (fn [record]
          (assoc record
                 :release-attestation-seed-retirement-id
                 release-attestation-id
                 :release-attestation-seed-retirement-engine
                 (:engine release-attestation)
                 :release-attestation-seed-retirement
                 :gravity-reader-release-attestation-seed-retirement-v1))
        records
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
        annotated-records
        (mapv annotate-with-release-attestation records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (let [prior-residual-boundaries
                     (:residual-trust-boundaries trace)]
                 (-> (or trace {})
                     (assoc :release-attestation-seed-retirement
                            release-attestation)
                     (assoc :release-attestation-operation-coverage
                            {:required
                             stage1-reader-release-attestation-seed-retirement-required-operations
                             :provided release-operations
                             :covered?
                             (set/subset?
                              (set stage1-reader-release-attestation-seed-retirement-required-operations)
                              (set release-operations))})
                     (assoc :replaced-physical-release-boundaries
                            prior-residual-boundaries)
                     (assoc :physical-release-boundaries
                            (:physical-release-boundaries
                             release-attestation))
                     (assoc :residual-trust-boundaries
                            (:residual-trust-boundaries release-attestation))
                     (assoc :residual-release-governance-boundaries
                            (:residual-release-governance-boundaries
                             release-attestation))
                     (assoc :release-attestation-fallbacks
                            (:release-attestation-fallbacks
                             release-attestation))
                     (assoc :release-attestation-record
                            (:release-attestation-record
                             release-attestation))
                     (assoc :seed-retirement-evidence
                            (:seed-retirement-evidence
                             release-attestation))
                     (assoc :supply-chain-manifest
                            (:supply-chain-manifest
                             release-attestation))
                     (assoc :release-custody-record
                            (:release-custody-record
                             release-attestation))
                     (assoc :governance-approval-record
                            (:governance-approval-record
                             release-attestation))
                     (assoc :revocation-check-report
                            (:revocation-check-report
                             release-attestation))
                     (assoc :release-provenance-record
                            (:release-provenance-record
                             release-attestation))
                     (assoc :release-attestation-artifact-routing
                            {:artifact (:artifact release-attestation)
                             :diagnostic-stream
                             (:diagnostic-stream release-attestation)
                             :proof-kind (:proof-kind release-attestation)})
                     (update :gravity-runtimes (fnil conj [])
                             :stage1-reader-release-attestation-seed-retirement)
                     (update :character-stream
                             annotate-with-release-attestation)
                     (update :token-stream
                             annotate-with-release-attestation)
                     (assoc :release-attestation-seed-retirement-applied
                            {:release-attestation-seed-retirement-id
                             release-attestation-id
                             :operation-count (count release-operations)
                             :replaced-physical-release-boundaries
                             prior-residual-boundaries
                             :residual-trust-boundaries
                             (:residual-trust-boundaries
                              release-attestation)
                             :residual-release-governance-boundaries
                             (:residual-release-governance-boundaries
                              release-attestation)
                             :release-attestation-fallbacks
                             (:release-attestation-fallbacks
                              release-attestation)}))))))
    annotated-records))

(defn stage1-reader-execute-release-attestation-seed-retirement-pipeline
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
         reader-source-path definitions)
        release-attestation
        (stage1-reader-release-attestation-seed-retirement-from-definitions
         reader-source-path definitions)]
    (when-not (stage1-reader-release-attestation-seed-retirement-entrypoint-valid?
               definitions)
      (stage1-reader-release-attestation-seed-retirement-fail!
       "STAGE1REL001" reader-source-path
       stage1-reader-release-attestation-seed-retirement-entrypoint
       {:missing-fields
        [stage1-reader-release-attestation-seed-retirement-entrypoint]}))
    {:records
     (stage1-reader-execute-release-attestation-seed-retirement
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
      diverse-verification
      release-attestation)
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver
     :runtime-entrypoint runtime-entrypoint
     :runtime-image runtime-image
     :verified-boot-chain boot-chain
     :diverse-bootstrap-verification diverse-verification
     :release-attestation-seed-retirement release-attestation}))

(defn stage1-reader-release-attestation-seed-retirement-diagnostic-stream
  [source-path release-attestation-id]
  {:artifact
   :gravity/stage1-reader-release-attestation-seed-retirement-diagnostic-stream
   :stage :stage1-reader-release-attestation-seed-retirement
   :source-path source-path
   :release-attestation-seed-retirement-id release-attestation-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage :stage1-reader-release-attestation-seed-retirement
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-release-attestation-seed-retirement-diagnostic-messages
                 id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat
          stage1-reader-release-attestation-seed-retirement-diagnostic-ids
          (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})