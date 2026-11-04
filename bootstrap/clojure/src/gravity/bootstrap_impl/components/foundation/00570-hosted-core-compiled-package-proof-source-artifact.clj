

(defn hosted-core-compiled-package-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        package-report (stage0-compiled-plan-package-report compiled-plan
                                                            module)
        manifest (:package-manifest package-report)
        project-manifest (:project-manifest-record package-report)
        lockfile (:lockfile-record package-report)
        build-graph (:build-graph-record package-report)
        artifact-manifest (:artifact-manifest-record package-report)
        package-operation (:package-operation-record package-report)
        resolution (:resolution-record package-report)
        capability-manifest (:capability-manifest-record package-report)
        reproducible-build (:reproducible-build-record package-report)
        package-safety (:package-safety-record package-report)
        registry (:registry-record package-report)
        provenance (:provenance-record package-report)
        target-matrix (:target-matrix-record package-report)
        signing-sbom (:signing-sbom-verification-record package-report)
        conformance (:package-conformance-results package-report)
        proof {:compiled-package-gate-validated? true
               :package-manifest-recorded?
               (= :complete (:status manifest))
               :project-and-lockfile-recorded?
               (and (true? (:offline-parse project-manifest))
                    (true? (:lockfile-complete project-manifest))
                    (true? (:complete lockfile)))
               :build-and-artifact-recorded?
               (and (= :complete (:status build-graph))
                    (boolean
                     (get-in artifact-manifest [:evidence :safety])))
               :package-operation-and-resolution-recorded?
               (and (true? (:download-verified package-operation))
                    (true? (:deterministic resolution))
                    (true? (:capability-compatible resolution)))
               :capability-and-safety-recorded?
               (and (= :complete (:status capability-manifest))
                    (= :reviewed (:review-state package-safety))
                    (boolean
                     (p12-present?
                      (:unsafe-audit-metadata package-safety))))
               :reproducibility-recorded?
               (and (= :disabled
                       (get-in reproducible-build
                               [:environment :network]))
                    (= :manifest-and-content-hash
                       (:rebuild-verification reproducible-build)))
               :registry-provenance-targets-recorded?
               (and (true? (:access-grant registry))
                    (:source-graph-hash provenance)
                    (false? (:implicit-host-target? target-matrix)))
               :signing-sbom-verification-recorded?
               (and (true? (get-in signing-sbom
                                    [:signature :canonical-payload]))
                    (= :accepted (:consumer-decision signing-sbom)))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"PKG1006" "PKG2001" "PKG3005" "PKG4001"
                    "PKG5002" "PKG6004" "PKG7003" "PKG8001"
                    "PKG9001" "PKG10001" "PKG11002" "PKG12002"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :production-package-manager? false
                             :external-registry-resolution? false
                             :live-publish-or-yank? false
                             :production-signing-service? false
                             :sbom-file-emitted? false
                             :attestation-service? false
                             :self-hosted-package-tooling? false
                             :next-required-capability
                             :compile-and-run-real-package-build-artifact-slices}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-package-proof
         :phase "12"
         :task "P12-S1"
         :governing-documents ["D1" "PKG1-PKG12"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :package-report
         (select-keys package-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :package-manifest
                       :project-manifest-record
                       :lockfile-record
                       :build-graph-record
                       :artifact-manifest-record
                       :package-operation-record
                       :resolution-record
                       :capability-manifest-record
                       :reproducible-build-record
                       :package-safety-record
                       :registry-record
                       :provenance-record
                       :target-matrix-record
                       :signing-sbom-verification-record
                       :package-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :proof-command (str "clojure -M:gravity hosted-core-compiled-package "
                             source-path)
         :rejected-fixtures stage0-compiled-package-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :production-package-manager? false
                            :external-registry-resolution? false
                            :live-publish-or-yank? false
                            :production-signing-service? false
                            :sbom-file-emitted? false
                            :attestation-service? false
                            :self-hosted-package-tooling? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-package-proof-file-artifact
  [path]
  (hosted-core-compiled-package-proof-source-artifact path (slurp path)))

(defn stage0-compiled-plan-tooling-report
  [plan module]
  (gravity.tooling/native-plan-report {:artifact-values (fn [] p13-tooling-artifact-values) :documents (fn [] p13-tooling-documents) :rejected-diagnostics (fn [] p13-tooling-rejected-diagnostics) :sha256 (fn [] sha256-hex) :compiled-rejected-fixtures (fn [] stage0-compiled-tooling-rejected-fixtures)} plan module))