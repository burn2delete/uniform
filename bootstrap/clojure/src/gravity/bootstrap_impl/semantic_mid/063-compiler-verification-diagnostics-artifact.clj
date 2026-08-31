(defn- semantic-mid-compiler-verification-diagnostics-artifact
  [{:keys [overrides optimization-artifact input-id diagnostic-schema
           diagnostic-stream]}]
  {:kind :gravity/stage0-compiler-verification-artifact
   :document-set ["C15" "C16" "C17" "C18"]
   :pass {:name :compiler-diagnostics-and-verification
          :input :optimization-lowering-manifest
          :output :compiler-verification-report
          :requires [:diagnostic-schema :incremental-graph
                     :plugin-manifest :risk-classification
                     :translation-validation :trust-report]
          :preserves [:source-spans :generated-origins :profile
                      :target :diagnostics :proofs]
          :emits [:diagnostic-schema :diagnostic-stream
                  :diagnostic-catalog :golden-diagnostic-fixtures
                  :incremental-dependency-graph :cache-key-schema
                  :cache-entry-manifest :invalidation-trace
                  :artifact-reuse-report :revalidation-report
                  :plugin-manifest :plugin-execution-trace
                  :compiler-verification-plan :pass-risk-classification
                  :pass-evidence-records :translation-validation-log
                  :compiler-trust-report :release-gate-report]
          :rejects compiler-verification-diagnostic-ids}
   :source-overrides overrides
   :optimization-lowering-artifact-kind (:kind optimization-artifact)
   :optimization-lowering-artifact-hash input-id
   :diagnostic-schema diagnostic-schema
   :diagnostic-stream diagnostic-stream
   :diagnostic-catalog
   {:artifact :gravity/diagnostic-catalog
    :rules compiler-verification-diagnostic-ids
    :status :complete}
   :related-span-map
   {:artifact :gravity/related-span-map
    :status :complete
    :entries (get-in diagnostic-stream [:diagnostics 0 :related])}
   :remediation-and-quick-fix-records
   [{:rule "C15-GOLDEN"
     :remediation :update-golden-fixture
     :quick-fix :regenerate-fixture
     :status :available}]
   :redaction-report
   {:artifact :gravity/diagnostic-redaction-report
    :status :passed
    :redacted-values []
    :public-safe? true}
   :rendering-records
   [{:renderer :cli :source :gravity/diagnostic-stream :status :complete}
    {:renderer :ide :source :gravity/diagnostic-stream :status :complete}
    {:renderer :ci :source :gravity/diagnostic-stream :status :complete}]
   :golden-diagnostic-fixtures
   [{:fixture :compiler-verification-golden
     :rules ["C15-GOLDEN"]
     :status :matched}]})
