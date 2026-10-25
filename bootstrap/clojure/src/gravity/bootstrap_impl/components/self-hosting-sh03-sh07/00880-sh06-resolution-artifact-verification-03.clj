(defn- sh06-resolution-diagnostic-origin
  "Recover only the existing origin of an exact failed reference.
  The caller has already authenticated the SH05 parent and request."
  [source-path sh05-artifact request diagnostic]
  (let [references (:references request)
        syntax-stream (:expanded-syntax-stream sh05-artifact)
        facts (:facts diagnostic)
        details (:rule-specific facts)
        ordinal (get-in facts [:semantic-span :ordinal])
        reference-fields [:name :qualifier :position :scope-chain]]
    (when (and (= :c5-name-resolution (:diagnostic-family diagnostic))
               (= :name-resolution (:stage diagnostic))
               (not (contains? diagnostic :origin-chain))
               (map? facts) (map? details)
               (vector? references)
               (vector? syntax-stream)
               (integer? ordinal) (pos? ordinal) (<= ordinal (count references)))
      (let [reference (get references (dec ordinal))
            candidates (:candidate-bindings diagnostic)
            candidate-count (:candidate-count details)
            source-span (:source-span reference)
            module (:module request)
            parent-binding (:macro-expansion-binding request)]
        (when (and (map? reference) (map? source-span)
                   ;; Module/import checks may borrow the FIRST reference as a
                   ;; display location. Only reference-check facts establish cause.
                   (every? #(contains? details %) reference-fields)
                   (= (select-keys reference reference-fields)
                      (select-keys details reference-fields))
                   (integer? candidate-count) (not (neg? candidate-count))
                   (vector? candidates) (<= (count candidates) candidate-count)
                   (case (:id diagnostic)
                     "C5-UNRESOLVED"
                     (case (:reason details)
                       :no-legal-binding (= 0 candidate-count (count candidates))
                       :malformed-core-binding-availability (= 1 candidate-count (count candidates))
                       false)
                     "C5-ALIAS" (= 0 candidate-count (count candidates))
                     "C5-AMBIGUOUS" (<= 2 (count candidates) candidate-count)
                     ("C5-PRIVATE" "C5-CROSS-PROFILE" "C5-CAPABILITY" "C5-TARGET")
                     (= 1 candidate-count (count candidates))
                     false)
                   (= (:reason facts) (:reason details))
                   (= (:syntax-id diagnostic) (:syntax-id reference))
                   (= (:symbol diagnostic) (:symbol reference))
                   (= (:semantic-span facts) (:semantic-span reference))
                   (= (:source-span diagnostic) (assoc source-span :source source-path))
                   (= source-path (get-in request [:provenance :actual-source-path]))
                   (= (:namespace diagnostic) (:namespace module))
                   (= (:active-profile diagnostic) (:profile module))
                   (= (:target diagnostic) (:target module))
                   (= (:artifact-id sh05-artifact) (:c4-artifact-id module)
                      (:c4-artifact-id parent-binding) (:c4-artifact-id facts))
                   (= (:source-revision-id module) (:source-revision-id parent-binding)
                      (:source-revision-id facts) (sh06-resolution-source-revision-id sh05-artifact))
                   (= (:expanded-syntax-stream-id sh05-artifact) (:macro-result-id parent-binding)))
          (let [owners (filterv
                        (fn [syntax]
                          (and (= source-span (:span syntax))
                               (= (:syntax-id reference)
                                  (reader-canonical-hash
                                   {:domain :gravity/sh06-reference-syntax-v1
                                    :owner-syntax-id (:syntax/id syntax)
                                    :ordinal ordinal :symbol (:symbol reference)
                                    :position (:position reference)}))))
                        syntax-stream)]
            (when (= 1 (count owners))
              (let [origin (:origin (first owners))]
                (when (and (vector? origin)
                           (some #(= :generated (:kind %)) origin))
                  origin)))))))))

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
        run (try
              (sh06-resolution-run-request! source-path binding request)
              (catch clojure.lang.ExceptionInfo error
                (if-let [origin (sh06-resolution-diagnostic-origin
                                 source-path sh05-artifact request (ex-data error))]
                  (throw (ex-info (.getMessage error)
                                  (assoc (ex-data error) :origin-chain origin) error))
                  (throw error))))
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
           :self-hosted? false}
          :capability-based-proof nil
          :diagnostics []}
         projections)
        identity-chunks (sh06-resolution-product-identity-chunks artifact-base)
        artifact-id (reader-canonical-hash
                     (sh06-resolution-artifact-identity-input-with-chunks
                      artifact-base identity-chunks))
        artifact-with-id (assoc artifact-base :artifact-id artifact-id)
        summary (sh06-resolution-envelope-summary-with-chunks
                 artifact-with-id identity-chunks)
        descriptor
        (sh06-resolution-sh02-descriptor source-path binding summary)
        envelope
        (p15-s23-stage2-sh02-descriptor-envelope
         sh06-resolution-envelope-stage
         sh06-resolution-sealed-artifact-kind descriptor source-path
         (sh06-resolution-envelope-source-options binding))
        _ (p15-s23-stage2-sh02-descriptor-envelope-verify!
           envelope sh06-resolution-envelope-stage
           sh06-resolution-sealed-artifact-kind descriptor source-path
           (sh06-resolution-envelope-source-options binding))
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
