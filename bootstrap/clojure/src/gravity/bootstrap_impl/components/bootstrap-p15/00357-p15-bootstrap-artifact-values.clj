

(defn p15-bootstrap-artifact-values
  []
  {:bootstrap-stage-matrix
   {:artifact :gravity/bootstrap-stage-matrix
    :artifact-id "bootstrap:stage-matrix:phase15"
    :stages [:stage0 :stage1 :stage2 :stage3]
    :stage-manifests
    [{:stage :stage0
      :compiler-owner :clojure-bootstrap
      :profiles [:core]
      :backends [:jvm]
      :release-eligible false}
     {:stage :stage1
      :compiler-owner :gravity-reader-macro
      :profiles [:core :meta]
      :backends [:jvm]
      :release-eligible false}
     {:stage :stage2
      :compiler-owner :gravity-analyzer-mir
      :profiles [:core :meta :hosted]
      :backends [:jvm :wasm]
      :release-eligible false}
     {:stage :stage3
      :compiler-owner :self-hosted-candidate
      :profiles [:core :meta :hosted]
      :backends [:jvm :wasm :llvm]
      :release-eligible true}]
    :trusted-inputs [:clojure-bootstrap :locked-package-graph
                     :bootstrap-runtime]
    :produced-artifacts [:gravityc-stage0 :gravityc-stage1
                         :gravityc-stage2 :gravityc-stage3]
    :conformance-reports [:phase14-self-hosting-validation
                          :stage3-language-subset-conformance]
    :equivalence-reports [:phase15-stage2-stage3-equivalence]
    :tcb-deltas [{:stage :stage3
                  :removed-trusted-components
                  [:reader :macroexpander :analyzer :mir-passes]}]
    :locked-dependencies [:gravity-lock-stage3]
    :compiler-lineage [{:artifact :gravityc-stage3
                        :compiled-by :gravityc-stage2}]
    :stage-gaps [{:stage :stage3
                  :gap :full-gpu-backend-migration
                  :status :deferred-with-owner}]
    :release-gates [:conformance :equivalence :reproducible :provenance]
    :unsafe-audit-records [:compiler-unsafe-audit-stage3]
    :status :complete}
   :seed-compiler-manifest
   {:artifact :gravity/seed-compiler-manifest
    :artifact-id "bootstrap:seed-compiler"
    :document "BOOT2"
    :seed-language :clojure
    :implemented-documents ["L1" "L2" "L3" "C1" "C2" "C5" "C6" "C7"
                            "C8" "C11" "C15"]
    :excluded-documents ["A1" "DOM16" "M7" "PKG9"]
    :supported-profiles [:core :meta]
    :unsupported-profiles-rejected true
    :diagnostics ["BOOT2001" "BOOT2002" "BOOT2003" "BOOT2004"
                  "BOOT2005" "BOOT2006"]
    :provenance-record :seed-provenance-stage0-clojure
    :host-dependencies ["clojure:stage0" "jvm:bootstrap"]
    :runtime-assumptions [:deterministic-filesystem-order
                          :disabled-network-after-fetch]
    :bootstrap-backend-artifact :gravityc-seed-jvm-backend
    :conformance-report :seed-language-compiler-backend-subset
    :metadata-for-stage-comparison [:source-hash :compiler-hash
                                    :backend-id :diagnostic-edn]
    :retirement-objective :replace-with-gravity-self-hosted-compiler
    :status :complete}
   :self-hosted-component-manifest
   {:artifact :gravity/self-hosted-component-manifest
    :artifact-id "bootstrap:self-hosted-components"
    :document "BOOT3"
    :profile :meta
    :migrated-modules [:reader :syntax :macroexpander :resolver
                       :typed-core :effect-checker :mir :diagnostics]
    :module-manifests
    [{:module :reader
      :inputs [:source-bytes]
      :outputs [:syntax-tree]
      :effects #{}
      :capabilities #{}
      :preserves [:source-spans]}
     {:module :mir
      :inputs [:typed-core]
      :outputs [:mir]
      :effects #{}
      :capabilities #{}
      :preserves [:types :effects :source-spans :safety-facts]}]
    :ambient-authority-denied true
    :stage-comparisons [:stage1-reader-macro :stage2-analyzer-mir]
    :equivalence-reports [:self-hosted-reader-equivalence
                          :self-hosted-mir-equivalence]
    :diagnostic-compatibility-report :self-hosted-diagnostic-spans
    :provenance-records [:generated-reader-provenance
                         :generated-mir-provenance]
    :tcb-deltas [:reader-removed-from-seed-tcb
                 :mir-removed-from-seed-tcb]
    :unsafe-audit-records [:compiler-internals-safe-api-audit]
    :status :complete}
   :compiler-coding-standard-report
   {:artifact :gravity/compiler-coding-standard-report
    :artifact-id "bootstrap:compiler-coding-standard"
    :document "BOOT4"
    :module-manifests [:reader-module-manifest :mir-module-manifest]
    :effect-capability-declarations
    [{:module :reader :effects #{} :capabilities #{}}
     {:module :build-artifact-writer
      :effects #{:artifact-io}
      :capabilities #{:compiler-artifact-write}}]
    :pass-preservation-report
    {:preserved [:source-spans :types :effects :capabilities
                 :safety-facts]
     :dropped []}
    :diagnostic-manifest :stable-compiler-diagnostics
    :deterministic-output-report :fixed-input-output-order
    :unsafe-audit-report :compiler-unsafe-islands-reviewed
    :ambient-access-policy :deny-host-time-random-network-filesystem
    :generated-artifact-provenance :generated-compiler-artifact-provenance
    :preservation-tests [:preserve-source-spans
                         :preserve-types-effects-safety]
    :status :complete}
   :stage-compatibility-matrix
   {:artifact :gravity/stage-compatibility-matrix
    :artifact-id "bootstrap:stage-compatibility"
    :document "BOOT5"
    :version "2026-06-29.phase15"
    :stages [:stage0 :stage1 :stage2 :stage3]
    :supported-features {:stage3 [:reader :macroexpander :analyzer
                                  :mir :diagnostics
                                  :package-build-subset]}
    :unsupported-features {:stage3 [:full-gpu-backend :full-ai-runtime]}
    :conformance-link-table
    {:stage3 [:phase14-self-hosting-validation
              :phase14-language-compiler-runtime-profile]}
    :profile-compliance-reports {:stage3 :profile-meta-hosted-compliance}
    :backend-conformance-reports
    {:stage3 [:jvm-backend-conformance :wasm-backend-conformance
              :llvm-backend-conformance]}
    :stage-gap-report [{:stage :stage3
                        :gap :full-gpu-backend-migration
                        :owner :backend-roadmap
                        :reviewed true}]
    :support-level-report {:stage3 :release-candidate-subset}
    :release-readiness-summary {:stage3 :eligible-after-policy-review}
    :matrix-change-record {:version "2026-06-29.phase15"
                           :linked-artifact :gravityc-stage3}
    :status :complete}
   :trusting-trust-report
   {:artifact :gravity/trusting-trust-report
    :artifact-id "bootstrap:trusting-trust"
    :document "BOOT6"
    :build-recipe :bootstrap-recipe-stage3
    :environment-manifest {:time :fixed
                           :locale "C"
                           :filesystem-order :sorted
                           :network :disabled-after-fetch
                           :toolchain :diverse-jvm-bootstrap}
    :locked-dependencies [:gravity-lock-stage3]
    :compiler-lineage [{:artifact :gravityc-stage3
                        :compiled-by :gravityc-stage2}]
    :rebuild-comparison-report {:manifest-hash "sha256:manifest"
                                :content-hash "sha256:content"
                                :status :matched}
    :diverse-rebuild-report {:toolchain :independent-jvm-toolchain
                             :identity :diverse-toolchain-001
                             :status :matched}
    :accepted-delta-report [{:field :provenance.timestamp
                             :policy :normalized
                             :reviewed true}]
    :revocation-check-report {:revoked-inputs []
                              :status :passed}
    :release-trust-summary {:residual-trusted-components
                            [:clojure-bootstrap :jvm-toolchain]
                            :tcb-delta :reduced}
    :network-policy :disabled-after-dependency-fetch
    :status :complete}
   :equivalence-report
   {:artifact :gravity/stage-equivalence-report
    :artifact-id "bootstrap:equivalence"
    :document "BOOT7"
    :compiler-a :gravityc-stage2
    :compiler-b :gravityc-stage3
    :compared-artifacts [:gravityc-stage3 :stdlib-core-stage3
                         :diagnostic-bundle-stage3]
    :comparison-modes [:manifest :diagnostics :conformance
                       :artifact-hashes :ir-modulo-ids :accepted-delta]
    :accepted-deltas [{:field :provenance.builder
                       :review :BOOT7-reviewed-delta}]
    :conformance-report :stage3-supported-feature-conformance
    :diagnostic-comparison-report :stable-code-and-span-comparison
    :ir-comparison-report :ir-equivalence-modulo-generated-ids
    :performance-bounds {:compile-time-ratio "<=1.10"
                         :status :within-bound}
    :release-decision :advance-stage3-with-recorded-deltas
    :provenance-links [:phase15-bootstrap-provenance]
    :status :complete}
   :bootstrap-provenance-record
   {:artifact :gravity/bootstrap-provenance-record
    :artifact-id "bootstrap:provenance"
    :document "BOOT8"
    :stage :stage3
    :source-graph-hash "sha256:source-stage3"
    :compiler-artifact-id :gravityc-stage2
    :compiler-hash "sha256:compiler-stage2"
    :lockfile-hash "sha256:lock-stage3"
    :build-recipe-hash "sha256:recipe-stage3"
    :environment-manifest-hash "sha256:environment-stage3"
    :dependency-graph-hash "sha256:dependencies-stage3"
    :conformance-report-links [:stage3-supported-feature-conformance]
    :equivalence-report-links [:phase15-stage-equivalence-report]
    :safety-report-links [:compiler-unsafe-audit-stage3]
    :sbom-links [:stage3-sbom]
    :signature-links [:stage3-provenance-signature]
    :builder-identity :bootstrap-builder-001
    :canonicalization :canonical-edn-v1
    :revocation-check {:status :passed :revoked-inputs []}
    :compiler-lineage-graph [{:artifact :gravityc-stage3
                              :compiled-by :gravityc-stage2}
                             {:artifact :gravityc-stage2
                              :compiled-by :gravityc-stage1}
                             {:artifact :gravityc-stage1
                              :compiled-by :gravityc-stage0}]
    :auditor-query-index {:gravityc-stage3 [:gravityc-stage2
                                            :gravityc-stage1
                                            :gravityc-stage0]}
    :lineage-acyclic true
    :status :complete}})