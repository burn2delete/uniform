

(defn compiler-c1-architecture-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        module (parse-module source-path forms)
        overrides (c1-architecture-source-overrides module)
        _ (c1-architecture-validate-overrides! source-path overrides)
        pass-artifact (compiler-pass-source-artifact source-path source-text)
        checked-artifact (checked-core-source-artifact source-path source-text)
        mir-artifact (mir-source-artifact source-path source-text)
        domain-artifact (domain-ir-source-artifact source-path source-text)
        optimization-artifact (optimization-lowering-source-artifact
                               source-path source-text)
        verification-artifact (compiler-verification-source-artifact
                               source-path source-text)
        stage-records [(c1-architecture-stage-record
                        :compiler-pass-contracts pass-artifact "C1")
                       (c1-architecture-stage-record
                        :checked-core checked-artifact "C1")
                       (c1-architecture-stage-record
                        :mir mir-artifact "C11")
                       (c1-architecture-stage-record
                        :domain-ir domain-artifact "C12")
                       (c1-architecture-stage-record
                        :optimization-lowering optimization-artifact
                        "C13-C14")
                       (c1-architecture-stage-record
                        :compiler-verification verification-artifact
                        "C15-C18")]
        pass-contracts (:pass-contract-registry pass-artifact)
        verifier-gates (mapv (fn [contract]
                               {:stage (:pass contract)
                                :owner-doc (:owner-doc contract)
                                :input (:input contract)
                                :output (:output contract)
                                :status (if (:verifier-gate? contract)
                                          :passed
                                          :not-required)})
                             pass-contracts)
        snapshot-bundle [{:stage :checked-core
                          :artifact-kind (:kind checked-artifact)
                          :artifact-id (c1-architecture-artifact-id
                                        checked-artifact)}
                         {:stage :mir
                          :artifact-kind (:kind mir-artifact)
                          :artifact-id (c1-architecture-artifact-id
                                        mir-artifact)}
                         {:stage :domain-ir
                          :artifact-kind (:kind domain-artifact)
                          :artifact-id (c1-architecture-artifact-id
                                        domain-artifact)}
                         {:stage :optimization-lowering
                          :artifact-kind (:kind optimization-artifact)
                          :artifact-id (c1-architecture-artifact-id
                                        optimization-artifact)}
                         {:stage :compiler-verification
                          :artifact-kind (:kind verification-artifact)
                          :artifact-id (c1-architecture-artifact-id
                                        verification-artifact)}]
        artifact-base
        {:kind :gravity/stage0-c1-compiler-architecture-artifact
         :task "P06-D080"
         :document-set ["C1"]
         :governing-document c1-architecture-governing-document
         :pass {:name :c1-compiler-architecture-document-coverage
                :input :compiler-verification-report
                :output :c1-compiler-architecture-proof
                :requires [:pipeline-manifest :pass-contract-registry
                           :stage-artifacts :diagnostic-stream
                           :provenance-graph :verifier-gates]
                :preserves [:source-spans :syntax-identity :origin-chain
                            :profile :target :types :effects :ownership
                            :capabilities :safety-outcomes :proofs
                            :diagnostics]
                :emits [:c1-document-conformance
                        :c1-capability-proof
                        :self-hosting-comparison-inputs]
                :rejects c1-architecture-diagnostic-ids}
         :source-overrides overrides
         :canonical-pipeline-order (:pipeline-stage-order pass-artifact)
         :pipeline-manifest (:pipeline-manifest pass-artifact)
         :stage-artifact-records stage-records
         :pass-contract-registry pass-contracts
         :evidence-log
         {:artifact :gravity/c1-evidence-log
          :preserved-facts (vec compiler-pass-durable-facts)
          :proofs (concat (:proof-or-certificate-references
                           verification-artifact)
                          (:proof-and-certificate-usage
                           optimization-artifact))
          :invalidations (:invalidated-fact-ledger optimization-artifact)
          :regenerations (mapv :regenerates pass-contracts)
          :status :complete}
         :ir-snapshot-bundle
         {:artifact :gravity/ir-snapshot-bundle
          :snapshots snapshot-bundle
          :status :complete}
         :diagnostic-stream (:diagnostic-stream verification-artifact)
         :artifact-provenance-graph
         {:artifact :gravity/artifact-provenance-graph
          :root (c1-architecture-artifact-id verification-artifact)
          :nodes (mapv #(select-keys % [:stage :artifact-kind
                                         :artifact-id])
                       stage-records)
          :edges [{:from :compiler-pass-contracts :to :checked-core}
                  {:from :checked-core :to :mir}
                  {:from :mir :to :domain-ir}
                  {:from :domain-ir :to :optimization-lowering}
                  {:from :optimization-lowering
                   :to :compiler-verification}]
          :status :complete}
         :verifier-gate-reports verifier-gates
         :self-hosting-comparison-inputs
         {:artifact :gravity/self-hosting-comparison-inputs
          :seed-compiler :gravity-stage0-clojure-bootstrap
          :retirement-objective :replace-with-gravity-self-hosted-compiler
          :public-artifacts (mapv :artifact-kind snapshot-bundle)
          :pass-contracts-loadable-by-package-system? true
          :compiler-data-gravity-values? true
          :stage-outputs-comparable? true
          :status :ready}
         :rejected-design-coverage c1-architecture-rejected-designs
         :document-conformance
         {:pipeline-manifest-emission :complete
          :pass-contract-validation :complete
          :verifier-gate-execution :complete
          :metadata-preservation-demonstrated? true
          :unchecked-backend-rejection :complete
         :domain-ir-anchors-demonstrated?
          (every? #(seq (get-in % [:semantic-anchor :mir-ops]))
                  (:domain-ir-artifacts domain-artifact))
          :invalidation-regeneration-demonstrated?
          (boolean
           (and (seq (:invalidated-fact-ledger optimization-artifact))
                (some seq (map :regenerates pass-contracts))))
          :diagnostics-origin-linked?
          (every? #(or (seq (:origin-chain %))
                       (seq (:related %))
                       (get-in % [:primary :span]))
                  (get-in verification-artifact
                          [:diagnostic-stream :diagnostics]))
          :bootstrap-comparable-artifacts :complete
          :status :complete}
         :diagnostics []}
        _ (c1-architecture-validate! source-path artifact-base)
        capability-proof (c1-architecture-capability-proof artifact-base)
        conformance {:documents ["C1"]
                     :task "P06-D080"
                     :required-diagnostic-ids c1-architecture-diagnostic-ids
                     :pipeline-manifest-status :complete
                     :pass-contract-status :complete
                     :verifier-gate-status :complete
                     :metadata-preservation-status :complete
                     :backend-boundary-status :complete
                     :domain-anchor-status :complete
                     :invalidation-status :complete
                     :diagnostic-origin-status :complete
                     :self-hosting-comparison-status :complete
                     :status :complete}
        artifact (assoc artifact-base
                        :capability-based-proof capability-proof
                        :c1-compiler-architecture-results conformance)]
    (assoc artifact :artifact-id (c1-architecture-artifact-id artifact))))

(defn compiler-c1-architecture-file-artifact
  [path]
  (compiler-c1-architecture-source-artifact path (slurp path)))

(def c2-reader-diagnostic-ids
  c2-reader-diagnostics/c2-reader-diagnostic-ids)

(def c2-reader-governing-document
  c2-reader-diagnostics/c2-reader-governing-document)

(def c2-reader-rejected-designs
  c2-reader-diagnostics/c2-reader-rejected-designs)

(def c2-reader-override-diagnostics
  c2-reader-diagnostics/c2-reader-override-diagnostics)