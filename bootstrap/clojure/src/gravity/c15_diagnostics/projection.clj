(ns gravity.c15-diagnostics.projection)
(defn artifact-base [configuration module source-overrides lowering-artifact diagnostics catalog]
  (let [lowering-id (:artifact-id lowering-artifact)]
    {:kind :gravity/stage0-c15-compiler-diagnostics-artifact :task "P06-D094" :document-set ["C15"]
     :governing-document (:c15-diagnostics-governing-document configuration)
     :pass {:name :c15-compiler-diagnostics :input :target-artifact-manifest :output :diagnostic-artifact-bundle
            :requires [:c14-target-artifact-manifest :source-spans :origin-chain :profile :target :facts
                       :remediation-policy :redaction-policy]
            :preserves [:source-spans :origin-chain :profile :target :artifact-provenance :facts :redactions]
            :emits [:diagnostic-schema :diagnostic-stream :diagnostic-catalog :related-span-map
                    :remediation-and-quick-fix-records :redaction-report :rendering-records :golden-diagnostic-fixtures]
            :rejects (:c15-diagnostics-diagnostic-ids configuration)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
     :c14-lowering-artifact (select-keys lowering-artifact [:kind :task :artifact-id :governing-document
                                                             :target-artifact-manifest :capability-based-proof])
     :lowering-artifact-kind (:kind lowering-artifact) :lowering-artifact-hash lowering-id
     :diagnostic-schema {:artifact :gravity/diagnostic-schema :status :complete
                         :required-fields (:c15-diagnostic-required-fields configuration)
                         :stable-id-input [:rule :primary-artifact :stage :facts]
                         :display-wording-version "stage0-c15"}
     :diagnostic-stream {:artifact :gravity/diagnostic-stream :stage :c15-compiler-diagnostics
                         :input-artifact lowering-id :output-artifact :gravity/diagnostic-artifact-bundle
                         :diagnostics diagnostics :summary (frequencies (map :severity diagnostics))
                         :deterministic-ordering-key :ordering-key :redaction-policy :public-safe
                         :rendering-version "stage0-c15" :status :complete}
     :diagnostic-catalog catalog
     :related-span-map {:artifact :gravity/related-span-map :status :complete
                        :entries (mapv (fn [d] {:diagnostic-id (:diagnostic-id d) :related (:related d)}) diagnostics)}
     :remediation-and-quick-fix-records
     (mapv (fn [id] {:rule id :remediation :structured-diagnostic-repair
                     :quick-fix :regenerate-diagnostic-artifact :status :available})
           (:c15-diagnostics-diagnostic-ids configuration))
     :redaction-report {:artifact :gravity/diagnostic-redaction-report :status :passed :public-safe? true
                        :redacted-value-hashes ["sha256:redacted-stage0"] :private-artifact-store :authorized-only
                        :raw-secret-values-present? false}
     :rendering-records [{:renderer :cli :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :ide :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :ci :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :safety-report :source :gravity/diagnostic-stream :status :complete}
                         {:renderer :package-report :source :gravity/diagnostic-stream :status :complete}]
     :golden-diagnostic-fixtures
     (mapv (fn [id] {:fixture (str "compiler-c15-" id) :rule id
                     :asserts [:rule :severity :primary :related :stage :profile :target :facts
                               :remediation :redactions :ordering] :status :matched})
           (:c15-diagnostics-diagnostic-ids configuration))
     :c15-diagnostics-results {:documents ["C15"] :task "P06-D094"
                               :required-diagnostic-ids (:c15-diagnostics-diagnostic-ids configuration)
                               :c14-input-status :complete :schema-status :complete :stream-status :complete
                               :catalog-status :complete :related-span-status :complete :remediation-status :complete
                               :redaction-status :complete :rendering-status :complete :golden-status :complete
                               :status :complete}
     :diagnostics []}))
