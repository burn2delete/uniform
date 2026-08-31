

(defn p15-s23-core-lowering-diagnostic-preservation-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :core-lowering-diagnostic-preservation source-path
   (fn [] (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path
         (:forms source-data)
         'p15-s23-core-lowering-diagnostic-preservation-report)
        source-syntax-artifact
        (p15-s23-source-syntax-serialization-proof-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        c6-artifact
        (p15-s23-core-diagnostic-c6-artifact
         source-path source-syntax-artifact pipeline-artifact)
        c15-artifact
        (p15-s23-core-diagnostic-c15-artifact source-path c6-artifact)
        preservation (:diagnostic-preservation-report c15-artifact)
        candidate {:proof-contract proof-contract
                   :source-syntax-artifact source-syntax-artifact
                   :compiler-pipeline-manifest-artifact pipeline-artifact
                   :c6-core-lowering-artifact c6-artifact
                   :c15-diagnostics-artifact c15-artifact
                   :diagnostic-preservation-report preservation}
        diagnostics
        (p15-s23-core-diagnostic-proof-diagnostics source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-core-diagnostic-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :source-syntax-artifact
                       (:artifact-id source-syntax-artifact)
                       :pipeline-manifest (:artifact-id pipeline-artifact)
                       :c6-artifact (:artifact-id c6-artifact)
                       :c15-artifact (:artifact-id c15-artifact)
                       :proof-contract proof-contract})))
        rejected-records
        (p15-s23-core-diagnostic-rejected-records source-path)
        artifact-base
        {:kind
         :gravity/p15-s23-core-lowering-diagnostic-preservation-artifact
         :phase "15"
         :task "P15-S23"
         :stage :p15-s23-core-lowering-diagnostic-preservation-report
         :source-path source-path
         :proof-id proof-id
         :proof-contract proof-contract
         :source-syntax-artifact
         (select-keys source-syntax-artifact
                      [:kind :artifact-id :proof-id
                       :serialization-roundtrip-record])
         :compiler-pipeline-manifest-artifact
         (select-keys pipeline-artifact
                      [:kind :artifact-id :manifest-id])
         :c6-core-lowering-artifact
         (select-keys c6-artifact
                      [:kind :artifact-id :core-ast-module
                       :surface-to-core-map :core-verifier-report
                       :capability-based-proof])
         :c15-diagnostics-artifact
         (select-keys c15-artifact
                      [:kind :artifact-id :diagnostic-schema
                       :diagnostic-stream
                       :diagnostic-preservation-report
                       :golden-diagnostic-fixtures
                       :capability-based-proof])
         :core-node-summary
         (mapv #(select-keys % [:artifact :node-id :form :source
                                :profile :target :evaluation-order
                                :lowering-rule])
               (:core-node-table c6-artifact))
         :diagnostic-preservation-report preservation
         :full-language-compiler-self-hosted?
         (get-in proof-contract
                 [:self-hosting-claims
                  :full-language-compiler-self-hosted?])
         :clojure-seed-retired?
         (get-in proof-contract
                 [:self-hosting-claims :clojure-seed-retired?])
         :accepted-p15-s23-core-diagnostic-fixtures
         [{:fixture source-path
           :status :accepted
           :core-node-count (count (:core-node-table c6-artifact))
           :diagnostic-count
           (count (get-in c15-artifact
                          [:diagnostic-stream :diagnostics]))}]
         :rejected-p15-s23-core-diagnostic-fixtures rejected-records
         :p15-s23-core-diagnostic-preservation-diagnostic-stream
         (p15-s23-core-diagnostic-stream source-path proof-id)
         :p15-s23-core-diagnostic-preservation-results
         {:accepted-fixtures 1
          :rejected-fixtures (count rejected-records)
          :diagnostic-count (count p15-s23-core-diagnostic-ids)
          :core-node-count (count (:core-node-table c6-artifact))
          :status :in-progress}
         :diagnostics []}
        proof
        (p15-s23-core-lowering-diagnostic-preservation-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))))

(defn p15-s23-core-lowering-diagnostic-preservation-file-artifact
  [path]
  (when-not (.isFile (java.io.File. path))
    (p15-s23-core-diagnostic-fail!
     "P15S23D001" path nil {:missing-fields [:compiler-source]}))
  (p15-s23-with-artifact-build-context
   #(p15-s23-core-lowering-diagnostic-preservation-source-artifact path)))

(def p15-s23-runtime-required-preserves
  #{:profile :target :effects :capabilities :source-spans
    :artifact-provenance :runtime-capability-manifest
    :capability-decision-log :runtime-service-classification})

(def p15-s23-runtime-action-families
  #{:filesystem :network :database :environment :process :shell :secrets
    :ffi :raw-memory :model :tool :memory :ai/human-review :observability
    :deployment :package-mutation})

(def p15-s23-runtime-diagnostic-messages
  {"P15S23R001" "P15-S23 runtime manifest/capability enforcement report is missing"
   "P15S23R002" "P15-S23 runtime manifest or family selection is incomplete"
   "P15S23R003" "P15-S23 runtime service classification is incomplete or hides a dependency"
   "P15S23R004" "P15-S23 runtime capability enforcement is incomplete or not deny-by-default"
   "P15S23R005" "P15-S23 runtime audit, principal, delegation, revocation, or redaction evidence is incomplete"
   "P15S23R006" "P15-S23 runtime/capability evidence is not linked to the verified compiler artifacts"
   "P15S23R007" "P15-S23 runtime/capability report makes an unsupported self-hosting or seed-retirement claim"})

(def p15-s23-runtime-diagnostic-ids
  ["P15S23R001" "P15S23R002" "P15S23R003" "P15S23R004"
   "P15S23R005" "P15S23R006" "P15S23R007"])

(defn p15-s23-runtime-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-runtime-diagnostic-messages
              id
              "P15-S23 runtime manifest/capability proof failed")
         (merge {:source-span {:source source-path}
                 :stage
                 :p15-s23-runtime-manifest-capability-enforcement-report
                 :diagnostic-family
                 :p15-s23-runtime-manifest-capability-enforcement-report
                 :value value
                 :remediation "Keep runtime selection explicit, classify every runtime service, enforce capabilities deny-by-default, preserve principal/audit/redaction records, link the evidence to verified compiler artifacts, and keep self-hosting claims false until the full P15-S23 evidence bundle exists."}
                data)))

(defn p15-s23-runtime-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-runtime-manifest-capability-enforcement-report
   :source-span {:source source-path}
   :message (get p15-s23-runtime-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_runtime_manifest_capability_enforcement_report})

(defn p15-s23-runtime-manifest
  [source-path core-artifact-id]
  {:artifact :gravity/p15-s23-runtime-manifest
   :profile :meta
   :target {:backend :jvm
            :platform :clojure-stage0
            :artifact core-artifact-id}
   :family :managed
   :selection-record
   {:artifact :gravity/runtime-family-selection-record
    :profile :meta
    :target :jvm
    :backend :clojure-stage0
    :selected-family :managed
    :selection-inputs [:profile :target :backend :effects :capabilities
                       :package-policy :deployment-policy]
    :status :complete}
   :services {:linked #{:diagnostic-renderer :source-map-loader
                        :capability-check-hook :panic-diagnostic}
              :generated #{:runtime-check-table
                           :capability-decision-dispatch
                           :denial-diagnostic}
              :delegated #{:clojure-stage0-artifact-store
                           :clojure-stage0-test-runner}
              :external #{}
              :forbidden #{:ambient-filesystem :ambient-network
                           :ambient-environment :ambient-process
                           :ambient-shell :ambient-secret-store
                           :dynamic-eval :unchecked-reflection}}
   :capability-checks true
   :diagnostics
   :gravity/p15-s23-runtime-capability-diagnostic-stream
   :consumed-by [:backend :package :observability :conformance
                 :self-hosting-gate]
   :source-span {:source source-path}
   :status :complete})

(defn p15-s23-runtime-service-table
  [runtime-manifest]
  (let [services (:services runtime-manifest)]
    {:artifact :gravity/p15-s23-runtime-service-table
     :runtime-manifest (:artifact runtime-manifest)
     :classification-kinds #{:linked :generated :delegated :external
                             :forbidden}
     :services services
     :linked (:linked services)
     :generated (:generated services)
     :delegated (:delegated services)
     :external (:external services)
     :forbidden (:forbidden services)
     :hidden-services []
     :backend-consumer :p15-s23-lower-target
     :package-consumer :p15-s23-artifact-bundle
     :observability-consumer :p15-s23-diagnostic-bundle
     :conformance-consumer :p15-s23-self-hosting-gate
     :status :complete}))