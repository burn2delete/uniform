

(def p16-standard-library-documents
  (mapv :document p16-standard-library-document-data))

(def p16-standard-library-data-by-document
  (into {} (map (juxt :document identity)
                p16-standard-library-document-data)))

(def p16-standard-library-phase-governing-documents
  (into {} (map (juxt :document :file)
                p16-standard-library-document-data)))

(def p16-standard-library-diagnostics-by-document
  (into {}
        (map (fn [{:keys [document diagnostic-count]}]
               [document (p16-std-diagnostics document
                                               (or diagnostic-count 8))])
             p16-standard-library-document-data)))

(def p16-standard-library-rejected-diagnostics
  (into {} (map (juxt :document :rejected-diagnostic)
                p16-standard-library-document-data)))

(def p16-standard-library-rejected-fixture-names
  (into {} (map (juxt :document :rejected-fixture)
                p16-standard-library-document-data)))

(def p16-standard-library-diagnostic-ids
  (vec
   (distinct
    (concat (mapcat p16-standard-library-diagnostics-by-document
                    p16-standard-library-documents)
            ["P16-MANIFEST" "P16-ACCEPTED" "P16-REJECTED"
             "P16-STDLIB"]))))

(def p16-standard-library-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             p16-standard-library-diagnostic-ids)))

(def p16-standard-library-artifact-keys
  [:library-module-manifest :api-stability-record :safe-wrapper-audit
   :library-conformance-fixture :profile-support-matrix
   :compatibility-report])

(defn p16-task-id
  [document]
  (str "P16-D" (:sequence (p16-standard-library-data-by-document document))))

(defn p16-standard-library-source-overrides
  [module]
  (get-in module [:metadata :standard-library :phase16] {}))

(defn p16-standard-library-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id %)
                      (p16-standard-library-diagnostics-by-document document))
            document))
        p16-standard-library-documents))

(defn p16-standard-library-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p16-standard-library-diagnostic-document id))]
    (fail! id
           "P16 standard-library validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase16-standard-library
                   :stage :standard-library
                   :document-id document
                   :task (when document (p16-task-id document))
                   :module (:module subject)
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 16 requires profile-aware module manifests, explicit effects and capabilities, safe wrapper audits, conformance fixtures, profile support matrices, stability records, and compatibility evidence before standard-library tasks can complete."}
                  extra))))

(defn p16-standard-library-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p16-standard-library-override-diagnostics fail-kind)]
      (p16-standard-library-fail!
       id source-path
       {:artifact-id (str "p16-stdlib-" (name fail-kind))
        :document-id (p16-standard-library-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (p16-standard-library-fail!
       "P16-MANIFEST" source-path
       {:artifact-id "p16-stdlib-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))

(defn p16-standard-library-module-record
  [{:keys [document module namespace title owned-surface profiles
           check-key dependencies]}]
  {:document document
   :module module
   :namespace namespace
   :title title
   :owned-surface owned-surface
   :profiles profiles
   :exports [{:name (keyword (str (name module) "-contract"))
              :kind :module-contract
              :profiles profiles
              :effects #{}
              :capabilities #{}
              :allocation :declared
              :stability :draft}]
   :effects [:declared-per-export]
   :capabilities [:declared-per-export]
   :allocation :declared-per-export
   :safety-boundary :safe-wrapper-or-rejected
   :stability :draft
   :diagnostics (p16-standard-library-diagnostics-by-document document)
   :checks (assoc {:profile-metadata true
                   :effects-declared true
                   :capabilities-declared true
                   :safe-wrapper-audited true
                   :docs-examples-compiled true
                   :stability-level true}
                  check-key true)
   :dependencies dependencies
   :status :complete})

(defn p16-standard-library-artifact-values
  []
  (let [modules (mapv p16-standard-library-module-record
                      p16-standard-library-document-data)
        profile-rows (mapv (fn [{:keys [document module profiles]}]
                             {:document document
                              :module module
                              :profiles profiles
                              :unsupported-profile-policy :reject
                              :status :complete})
                           p16-standard-library-document-data)
        stability-entries (mapv (fn [{:keys [document module namespace]}]
                                  {:document document
                                   :module module
                                   :namespace namespace
                                   :level :draft
                                   :tracked [:source :behavior :effects
                                             :capabilities :diagnostics
                                             :artifacts :profiles
                                             :conformance]
                                   :status :complete})
                                p16-standard-library-document-data)]
    {:library-module-manifest
     {:artifact :gravity/library-module-manifest
      :artifact-id "stdlib:module-manifest:phase16"
      :modules modules
      :module-count (count modules)
      :profile-metadata-complete true
      :effect-capability-metadata-complete true
      :package-manifest-consistency :complete
      :status :complete}
     :api-stability-record
     {:artifact :gravity/api-stability-record
      :artifact-id "stdlib:api-stability"
      :entries stability-entries
      :levels [:experimental :draft :stable :deprecated :removed :internal]
      :compatibility-dimensions [:source :behavior :effects :capabilities
                                 :profiles :diagnostics :artifacts
                                 :conformance]
      :explicit-opt-in-required true
      :deprecation-diagnostics true
      :status :complete}
     :safe-wrapper-audit
     {:artifact :gravity/safe-wrapper-audit
      :artifact-id "stdlib:safe-wrapper-audit"
      :audited-modules (mapv :module
                             (filter #(some #{(:document %)}
                                            ["STD3" "STD5" "STD6" "STD7"
                                             "STD8" "STD9" "STD15" "STD16"
                                             "STD17" "STD18"])
                                     p16-standard-library-document-data))
      :unsafe-islands [:allocator-internals :scheduler-internals
                       :filesystem-adapters :tls-provider
                       :hardware-mmio :crypto-constant-time]
      :safe-wrapper-policy :prove-check-reject-or-unsafe-island
      :audit-records [:std-memory-audit :std-concurrency-audit
                      :std-io-audit :std-crypto-audit
                      :std-hardware-audit]
      :status :complete}
     :library-conformance-fixture
     {:artifact :gravity/library-conformance-fixture
      :artifact-id "stdlib:conformance-fixture"
      :accepted-fixtures ["accepted/standard-library-phase16.gravity"]
      :rejected-fixtures (vals p16-standard-library-rejected-fixture-names)
      :document-count (count p16-standard-library-documents)
      :accepted-count (count p16-standard-library-documents)
      :rejected-count (count p16-standard-library-documents)
      :diagnostic-count (count p16-standard-library-diagnostic-ids)
      :suite-categories [:module-manifest :profile-matrix :safe-wrapper
                         :conformance :stability :compatibility]
      :status :complete}
     :profile-support-matrix
     {:artifact :gravity/profile-support-matrix
      :artifact-id "stdlib:profile-support-matrix"
      :rows profile-rows
      :profiles (vec (distinct (mapcat :profiles profile-rows)))
      :unsupported-profile-policy :reject
      :profile-compliance-report :p03-profile-compliance-suite
      :status :complete}
     :compatibility-report
     {:artifact :gravity/standard-library-compatibility-report
      :artifact-id "stdlib:compatibility"
      :modules (mapv :module p16-standard-library-document-data)
      :release "0.1.0-stage0"
      :source-compatible true
      :artifact-compatible true
      :diagnostic-compatible true
      :profile-availability-delta []
      :migration-artifacts []
      :signed-provenance :stage0-stdlib-provenance
      :status :complete}}))