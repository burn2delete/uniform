

(defn stage1-reader-formal-release-governance-seed-retirement-entrypoint-valid?
  [definitions]
  (let [definition
        (get definitions
             stage1-reader-formal-release-governance-seed-retirement-entrypoint)]
    (and (= :defn (:kind definition))
         (= '[source-path source-text] (:params definition))
         (= '((reader/execute-formal-release-governance-seed-retirement
               source-path
               source-text
               stage1-reader-formal-release-governance-seed-retirement
               stage1-reader-release-attestation-seed-retirement))
            (:body definition)))))

(defn stage1-reader-execute-formal-release-governance-seed-retirement
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
   release-attestation
   formal-governance]
  (let [formal-governance-id
        (:formal-release-governance-seed-retirement-id
         formal-governance)
        operations (:formal-governance-operations formal-governance)
        annotate-with-formal-governance
        (fn [record]
          (assoc record
                 :formal-release-governance-seed-retirement-id
                 formal-governance-id
                 :formal-release-governance-engine
                 (:engine formal-governance)
                 :formal-release-governance
                 :gravity-reader-formal-release-governance-v1))
        records
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
        annotated-records (mapv annotate-with-formal-governance records)]
    (when *stage1-reader-pipeline-trace*
      (swap! *stage1-reader-pipeline-trace*
             (fn [trace]
               (let [prior-release-governance-boundaries
                     (:residual-release-governance-boundaries trace)]
                 (-> (or trace {})
                     (assoc :formal-release-governance-seed-retirement
                            formal-governance)
                     (assoc :formal-release-governance-operation-coverage
                            {:required
                             stage1-reader-formal-release-governance-seed-retirement-required-operations
                             :provided operations
                             :covered?
                             (set/subset?
                              (set stage1-reader-formal-release-governance-seed-retirement-required-operations)
                              (set operations))})
                     (assoc :replaced-release-governance-boundaries
                            prior-release-governance-boundaries)
                     (assoc :residual-release-governance-boundaries
                            (:residual-release-governance-boundaries
                             formal-governance))
                     (assoc :formal-release-governance-fallbacks
                            (:formal-release-governance-fallbacks
                             formal-governance))
                     (assoc :formal-release-governance-record
                            (:formal-release-governance-record
                             formal-governance))
                     (assoc :deployment-custody-record
                            (:deployment-custody-record
                             formal-governance))
                     (assoc :self-hosting-evidence
                            (:self-hosting-evidence formal-governance))
                     (assoc :formal-seed-retirement-evidence
                            (:seed-retirement-evidence formal-governance))
                     (assoc :formal-tcb-delta-record
                            (:tcb-delta-record formal-governance))
                     (assoc :formal-unsafe-audit-report
                            (:unsafe-audit-report formal-governance))
                     (assoc :formal-release-provenance-record
                            (:formal-release-provenance-record
                             formal-governance))
                     (assoc :formal-release-governance-artifact-routing
                            {:artifact (:artifact formal-governance)
                             :diagnostic-stream
                             (:diagnostic-stream formal-governance)
                             :proof-kind (:proof-kind formal-governance)})
                     (update :gravity-runtimes (fnil conj [])
                             :stage1-reader-formal-release-governance-seed-retirement)
                     (update :character-stream
                             annotate-with-formal-governance)
                     (update :token-stream
                             annotate-with-formal-governance)
                     (assoc
                      :formal-release-governance-applied
                      {:formal-release-governance-seed-retirement-id
                       formal-governance-id
                       :operation-count (count operations)
                       :replaced-release-governance-boundaries
                       prior-release-governance-boundaries
                       :residual-release-governance-boundaries
                       (:residual-release-governance-boundaries
                        formal-governance)
                       :formal-release-governance-fallbacks
                       (:formal-release-governance-fallbacks
                        formal-governance)
                       :clojure-seed-retired?
                       (get-in formal-governance
                               [:self-hosting-evidence
                                :clojure-seed-retired?])}))))))
    annotated-records))

(defn stage1-reader-execute-formal-release-governance-seed-retirement-pipeline
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
         reader-source-path definitions)
        formal-governance
        (stage1-reader-formal-release-governance-seed-retirement-from-definitions
         reader-source-path definitions)]
    (when-not
        (stage1-reader-formal-release-governance-seed-retirement-entrypoint-valid?
         definitions)
      (stage1-reader-formal-release-governance-seed-retirement-fail!
       "STAGE1GOV001" reader-source-path
       stage1-reader-formal-release-governance-seed-retirement-entrypoint
       {:missing-fields
        [stage1-reader-formal-release-governance-seed-retirement-entrypoint]}))
    {:records
     (stage1-reader-execute-formal-release-governance-seed-retirement
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
      release-attestation
      formal-governance)
     :self-hosted-runtime self-hosted-runtime
     :core-bootstrap-runtime core-bootstrap-runtime
     :core-bootstrap-builtins core-bootstrap-builtins
     :compiler-driver compiler-driver
     :runtime-entrypoint runtime-entrypoint
     :runtime-image runtime-image
     :verified-boot-chain boot-chain
     :diverse-bootstrap-verification diverse-verification
     :release-attestation-seed-retirement release-attestation
     :formal-release-governance-seed-retirement formal-governance}))

(defn stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
  [source-path formal-governance-id]
  {:artifact
   :gravity/stage1-reader-formal-release-governance-seed-retirement-diagnostic-stream
   :stage :stage1-reader-formal-release-governance-seed-retirement
   :source-path source-path
   :formal-release-governance-seed-retirement-id formal-governance-id
   :diagnostics
   (mapv (fn [id]
           {:diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage
            :stage1-reader-formal-release-governance-seed-retirement
            :artifact :gravity/diagnostic
            :message
            (or (stage1-reader-formal-release-governance-seed-retirement-diagnostic-messages
                 id)
                (stage1-reader-execution-diagnostic-messages id))})
         (concat
          stage1-reader-formal-release-governance-seed-retirement-diagnostic-ids
          (butlast stage1-reader-execution-diagnostic-ids)))
   :status :complete})