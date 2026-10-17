
(defn- sh06-resolution-source-artifact-candidate
  "Construct the authentic internal aggregate before its mandatory proof gate.

  This private value is available to bounded diagnostics and tests only.  It is
  not an accepted artifact and must never cross the public SH-06 boundary
  without sh06-resolution-capability-based-proof-for-construction."
  ([source-path sh05-artifact legacy?]
   (sh06-resolution-source-artifact-candidate
    source-path sh05-artifact legacy? ::legacy))
  ([source-path sh05-artifact legacy? context]
   (sh06-resolution-source-artifact-candidate source-path sh05-artifact legacy? context
    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}))
  ([source-path sh05-artifact legacy? context module-inputs]
  (with-sh06-resolution-transport-bounds
   (let [contextual? (not= ::legacy context)
        _ (when (and contextual? (nil? context))
            (sh06-resolution-c5-fail! source-path :missing-context {}))
        _ (sh06-authenticated-sh05-parent!
            source-path sh05-artifact)
        c2-artifact (get-in sh05-artifact
                            [:gravity-macro-boundary
                             :authenticated-sh04-artifact
                             :c2-reader-artifact])
        source-before
        (if contextual?
          (sh06-authenticated-source-snapshot!
           source-path c2-artifact :source-authentication-before-c5
           legacy? context)
          (sh06-authenticated-source-snapshot!
           source-path c2-artifact :source-authentication-before-c5 legacy?))
        request (if contextual?
                  (sh06-resolution-request source-path sh05-artifact context)
                  (sh06-resolution-module-request source-path sh05-artifact module-inputs))
        binding (if contextual?
                  (sh06-resolution-context-plan-binding! source-path context)
                  (sh06-resolution-current-binding! source-path))
        run (sh06-resolution-run-request! source-path binding request)
        analysis (:resolved-analysis run)
        projections (sh06-resolution-product-projections analysis)
        plan-binding (dissoc binding :plan :source-text :input-key)
        boundary-base
        (cond-> {:slice :SH-06
         :owner :gravity-source
         :adapter-contract sh06-resolution-adapter-contract
         :plan-binding plan-binding
         :authenticated-resolution-request request
         :raw-template-result (:raw-template-result run)
         :raw-analysis (:raw-analysis run)
         :resolved-analysis analysis
         :digest-requests (:digest-requests run)
         :resolved-digests (:resolved-digests run)
         :template-verification (:template-verification run)
         :resolved-verification (:resolved-verification run)
         :target-source-reread? false
         :clojure-adapter-residual? true
         :self-hosted? false}
          (not contextual?) (assoc :module-inputs module-inputs))
        artifact-base
        (merge
         {:kind :gravity/sh06-resolution-artifact
          :status :accepted
          :slice :SH-06
          :task "SH-06"
          :document-set ["L3" "C5"]
          :governing-document c5-resolution-governing-document
          :artifact-id nil
          :sh05-macro-artifact sh05-artifact
          :gravity-resolution-boundary boundary-base
          :provenance
          {:source-path source-path
           :source-revision-id (get-in request
                                       [:module :source-revision-id])}
          :pass
          {:name :sh06-gravity-name-resolution
           :input :authenticated-sh05-expanded-syntax
           :output :authenticated-sh06-namespace-analysis
           :preserves [:source-spans :syntax-ids :macro-lineage
                       :profile :target :effects :capabilities]
           :rejects c5-resolution-diagnostic-ids}
          :execution-boundary
          {:resolution-authority :gravity
           :gravity-module 'gravity.resolution
           :plan-runner :clojure-stage0
           :digest-resolver :clojure-stage0
           :envelope-binder :clojure-stage0
           :compatibility-adapter :clojure-stage0
           :component-transport-bounds sh06-resolution-transport-bounds
           :aggregate-artifact-bounds sh06-resolution-artifact-bounds
           :self-hosted? false}
          :capability-based-proof nil
          :diagnostics []}
         projections)
        artifact-id (sh06-resolution-artifact-id artifact-base)
        artifact-with-id (assoc artifact-base :artifact-id artifact-id)
        summary (sh06-resolution-envelope-summary artifact-with-id)
        descriptor
        (sh06-resolution-sh02-descriptor source-path binding summary)
        envelope
        (p15-s23-stage2-sh02-descriptor-envelope
         sh06-resolution-envelope-stage
         sh06-resolution-sealed-artifact-kind descriptor source-path)
        _ (p15-s23-stage2-sh02-descriptor-envelope-verify!
           envelope sh06-resolution-envelope-stage
           sh06-resolution-sealed-artifact-kind descriptor source-path)
        artifact-with-envelope
        (-> artifact-with-id
            (assoc-in [:gravity-resolution-boundary
                       :authenticated-envelope-descriptor] descriptor)
            (assoc-in [:gravity-resolution-boundary
                       :authenticated-envelope] envelope))]
    (if contextual?
      (sh06-source-snapshot-stable!
       source-path source-before c2-artifact legacy? context)
      (sh06-source-snapshot-stable!
       source-path source-before c2-artifact legacy?))
    artifact-with-envelope))))
