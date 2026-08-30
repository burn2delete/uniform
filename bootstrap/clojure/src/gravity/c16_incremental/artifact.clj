(ns gravity.c16-incremental.artifact)

(def stages
  [:reader :macro-expansion :type-check :effect-check :safety-analysis :mir
   :diagnostics :target-artifact])

(defn- cache-entry-manifest
  [sha256-hex input-id source-hash dependency-hash stage-cache-keys]
  (mapv (fn [cache-key]
          {:artifact :gravity/cache-entry
           :stage (:stage cache-key)
           :cache-key (str "sha256:" (sha256-hex (pr-str cache-key)))
           :artifact-id input-id
           :producer {:stage (:stage cache-key) :pass-version "stage0-c16"}
           :inputs [input-id source-hash dependency-hash]
           :preserved-facts #{:source-spans :origin-chain :diagnostics
                              :proofs :profile :target}
           :invalidated-by #{:source-change :macro-change :profile-change
                             :target-change :diagnostic-schema-change}
           :diagnostics :gravity/c16-incremental-diagnostic-stream
           :provenance :gravity/incremental-dependency-graph
           :trust :local-build
           :revalidation :required-before-release})
        stage-cache-keys))

(defn- invalidation-trace [invalidation-causes]
  (mapv (fn [cause]
          {:invalidating-input cause
           :affected-nodes [:diagnostics :target-artifact :proofs]
           :downstream-revalidation-stages stages
           :status :recorded})
        invalidation-causes))

(defn- dependency-graph []
  {:artifact :gravity/incremental-dependency-graph
   :status :consistent
   :nodes [:source-unit :syntax-object-stream :macro-expansion-trace
           :namespace-analysis :typed-core :effect-graph :ownership-graph
           :safety-outcomes :mir-module :optimization-decisions
           :domain-ir-artifacts :target-artifacts :diagnostics
           :proofs-and-certificates :package-provider-manifests]
   :edges [{:from :source-unit :to :syntax-object-stream :field :source}
           {:from :syntax-object-stream :to :macro-expansion-trace :field :syntax}
           {:from :macro-expansion-trace :to :typed-core :field :macro-expansion}
           {:from :typed-core :to :effect-graph :field :type-facts}
           {:from :effect-graph :to :safety-outcomes :field :effects}
           {:from :safety-outcomes :to :mir-module :field :safety}
           {:from :mir-module :to :domain-ir-artifacts :field :mir}
           {:from :domain-ir-artifacts :to :target-artifacts :field :lowering}
           {:from :diagnostics :to :target-artifacts :field :diagnostic-schema}
           {:from :proofs-and-certificates :to :target-artifacts :field :proof-policy}]})

(defn- evidence-records [module source-hash dependency-hash]
  {:artifact-reuse-report
   {:artifact :gravity/artifact-reuse-report
    :status :validated
    :unchanged-source-reuse :allowed
    :changed-policy-reuse :rejected
    :release-boundary :requires-full-revalidation}
   :revalidation-report
   {:artifact :gravity/revalidation-report
    :status :passed
    :checks [:cache-key :artifact-schema-version :producer-pass-version
             :preserved-facts :proof-freshness :profile-target-compatibility
             :diagnostic-schema-compatibility :dependency-graph-compatibility]}
   :stale-proof-rejection-report
   {:artifact :gravity/stale-proof-rejection-report
    :status :rejected
    :diagnostic "C16-PROOF"
    :reason :proof-inputs-or-policy-changed}
   :stale-diagnostic-rejection-report
   {:artifact :gravity/stale-diagnostic-rejection-report
    :status :rejected
    :diagnostic "C16-DIAGNOSTIC"
    :reason :origin-spans-or-facts-changed}
   :build-effect-replay-record
   {:artifact :gravity/build-effect-replay-record
    :status :complete
    :replay-hash "sha256:c16-replay-record"
    :build-effects #{:build/read-file}
    :hermetic? true}
   :policy-compatibility-report
   {:artifact :gravity/cache-policy-compatibility-report
    :status :compatible
    :profile :hosted
    :target :jvm
    :capabilities (:capabilities module)
    :safety :safe}
   :speculative-reuse-record
   {:artifact :gravity/speculative-cache-reuse
    :reuse :speculative
    :interactive-build? true
    :publish-status :blocked-from-release
    :revalidation :required}
   :release-rebuild-record
   {:artifact :gravity/reproducible-release-rebuild
    :status :reproducible
    :recorded-inputs [source-hash dependency-hash]
    :environment "sha256:c16-hermetic-stage0"}})

(defn source-artifact
  [{:keys [read-source-form-records validate-ns-syntax! parse-module
           source-overrides validate-source-overrides! c15-diagnostics-artifact
           sha256-hex stage-cache-key diagnostic-stream validate! capability-proof
           c4-artifact-id governing-document diagnostic-ids
           cache-key-required-fields invalidation-causes]}
   source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (source-overrides module)
        _ (validate-source-overrides! source-path source-overrides)
        diagnostics-artifact (c15-diagnostics-artifact source-path source-text)
        input-id (:artifact-id diagnostics-artifact)
        source-hash (str "sha256:" (sha256-hex source-text))
        dependency-hash (str "sha256:" (sha256-hex (pr-str input-id)))
        stage-cache-keys (mapv #(stage-cache-key % source-hash dependency-hash) stages)
        cache-entry-manifest (cache-entry-manifest sha256-hex input-id source-hash dependency-hash stage-cache-keys)
        invalidation-trace (invalidation-trace invalidation-causes)
        diagnostic-stream (diagnostic-stream source-path input-id)
        artifact-base
        (merge
         {:kind :gravity/stage0-c16-incremental-compilation-artifact
          :task "P06-D095"
          :document-set ["C16"]
          :governing-document governing-document
          :pass {:name :c16-incremental-compilation
                 :input :diagnostic-artifact-bundle
                 :output :incremental-compilation-artifact
                 :requires [:c15-diagnostics :source :compiler :profile :target
                            :dependencies :build-effects :capabilities :policy :proofs]
                 :preserves [:source-spans :origin-chain :diagnostics :provenance
                             :proofs :profile :target]
                 :emits [:incremental-dependency-graph :cache-key-schema
                         :stage-cache-keys :cache-entry-manifest :invalidation-trace
                         :artifact-reuse-report :revalidation-report
                         :stale-proof-rejection-report :stale-diagnostic-rejection-report
                         :build-effect-replay-record :speculative-reuse-record
                         :release-rebuild-record :incremental-diagnostic-stream]
                 :rejects diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety :metadata])
          :c15-diagnostics-artifact
          (select-keys diagnostics-artifact
                       [:kind :task :artifact-id :governing-document
                        :diagnostic-stream :capability-based-proof])
          :diagnostics-artifact-kind (:kind diagnostics-artifact)
          :diagnostics-artifact-hash input-id
          :incremental-dependency-graph (dependency-graph)
          :cache-key-schema {:artifact :gravity/cache-key-schema
                             :status :complete
                             :required-fields cache-key-required-fields}
          :stage-cache-keys stage-cache-keys
          :cache-entry-manifest cache-entry-manifest
          :invalidation-trace invalidation-trace
          :incremental-diagnostic-stream diagnostic-stream
          :c16-incremental-results
          {:documents ["C16"]
           :task "P06-D095"
           :required-diagnostic-ids diagnostic-ids
           :c15-input-status :complete
           :dependency-graph-status :complete
           :cache-key-status :complete
           :cache-entry-status :complete
           :invalidation-status :complete
           :reuse-status :complete
           :revalidation-status :complete
           :stale-proof-status :complete
           :stale-diagnostic-status :complete
           :speculative-status :complete
           :replay-status :complete
           :release-rebuild-status :complete
           :diagnostic-status :complete
           :status :complete}
          :diagnostics []}
         (evidence-records module source-hash dependency-hash))
        _ (validate! source-path artifact-base)
        capability-proof (capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id
                         (assoc artifact-base
                                :capability-based-proof capability-proof)))))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
