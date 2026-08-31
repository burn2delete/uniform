(defn- semantic-mid-compiler-verification-context
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        module (parse-module source-path forms)
        overrides (compiler-verification-source-overrides module)
        _ (compiler-verification-validate-overrides! source-path overrides)
        optimization-artifact
        (optimization-lowering-source-artifact source-path source-text)
        input-id (str "sha256:" (sha256-hex (pr-str optimization-artifact)))
        diagnostic-schema
        {:artifact :gravity/diagnostic-schema
         :required-fields [:diagnostic-id :rule :severity :stage :primary
                           :related :origin-chain :profile :target :facts
                           :remediation :redactions]
         :status :complete}
        diagnostic-stream
        {:artifact :gravity/diagnostic-stream
         :stage :compiler-verify
         :input-artifact input-id
         :diagnostics
         [{:artifact :gravity/diagnostic
           :diagnostic-id "diag-c15-golden-stage0"
           :rule "C15-GOLDEN"
           :severity :hint
           :stage :compiler-verify
           :message-key :diagnostic.golden-fixture
           :primary {:span (source-span source-path 0)
                     :syntax-id "stage0-syntax-0"
                     :artifact input-id}
           :related [{:role :generated-by
                      :span (source-span source-path 0)
                      :artifact :compiler-verification-fixture}]
           :origin-chain []
           :profile (:profile module)
           :target (:target module)
           :facts {:fixture :compiler-verification :rule :C15-GOLDEN}
           :remediation [{:kind :update-golden-fixture}]
           :redactions []}]
         :summary {:hint 1}
         :ordering-key [:rule :primary :artifact]
         :redaction-policy :public-safe
         :rendering-version "stage0-c15"
         :status :complete}
        incremental-graph
        {:artifact :gravity/incremental-dependency-graph
         :nodes [:source-unit :syntax-object-stream :macro-expansion-trace
                 :typed-core :mir-module :domain-ir
                 :optimization-lowering :diagnostics :target-artifact]
         :edges [{:from :source-unit :to :syntax-object-stream}
                 {:from :syntax-object-stream :to :typed-core}
                 {:from :typed-core :to :mir-module}
                 {:from :mir-module :to :domain-ir}
                 {:from :domain-ir :to :optimization-lowering}
                 {:from :optimization-lowering :to :target-artifact}
                 {:from :optimization-lowering :to :diagnostics}]
         :status :consistent}
        cache-key
        {:artifact :gravity/cache-key
         :stage :compiler-verify
         :source (str "sha256:" (sha256-hex source-text))
         :compiler "gravity-stage0-clojure"
         :profile (:profile module)
         :target (:target module)
         :pass-contract :compiler-verification
         :dependencies (str "sha256:" (sha256-hex (pr-str incremental-graph)))
         :build-effects :none
         :capabilities (:capabilities module)
         :policy :stage0-safe}
        cache-entry
        {:artifact :gravity/cache-entry
         :cache-key (str "sha256:" (sha256-hex (pr-str cache-key)))
         :artifact-id input-id
         :producer {:stage :compiler-verify :pass-version "stage0-c18"}
         :inputs [input-id]
         :preserved-facts #{:source-spans :diagnostics :proofs}
         :invalidated-by #{:source-change :diagnostic-schema-change
                           :proof-policy-change :target-change}
         :diagnostics :gravity/diagnostic-stream
         :trust :local-build
         :revalidation :required-before-release}
        plugin-manifest
        {:artifact :gravity/compiler-plugin
         :plugin 'gravity.plugins.stage0/verifier
         :package {:name 'gravity/compiler-verifier :version "0.1.0"}
         :api-version "1"
         :compiler-compatibility {:min "0.1.0" :max-exclusive "0.2.0"}
         :trust :sandboxed
         :profile :meta
         :build-effects #{}
         :capabilities #{:compiler/ir-transform}
         :capability-scopes
         {:compiler/ir-transform #{:read-mir :emit-diagnostics}}
         :passes [:diagnostic-golden-check]
         :domains []
         :facets []
         :emits #{:diagnostic-stream :verification-report}
         :conformance [:compiler-verification-fixtures]
         :status :accepted}
        risk-records
        [{:artifact :gravity/pass-risk
          :pass :reader :risk :critical
          :reason #{:trusted-semantic-base}
          :affected-profiles #{:core :hosted :native}
          :minimum-evidence #{:golden-fixtures :fuzz}
          :release-gate :required}
         {:artifact :gravity/pass-risk
          :pass :bounds-check-elide :risk :high
          :reason #{:removes-runtime-checks :depends-on-proof}
          :affected-profiles #{:hosted :native :gpu}
          :minimum-evidence #{:translation-validation
                              :proof-dominance-check}
          :release-gate :required}
         {:artifact :gravity/pass-risk
          :pass :target-lowering :risk :high
          :reason #{:emits-backend-artifacts}
          :affected-profiles #{:hosted :native}
          :minimum-evidence #{:backend-conformance
                              :differential-fixtures}
          :release-gate :required}]]
    {:source-path source-path
     :source-text source-text
     :module module
     :overrides overrides
     :optimization-artifact optimization-artifact
     :input-id input-id
     :diagnostic-schema diagnostic-schema
     :diagnostic-stream diagnostic-stream
     :incremental-graph incremental-graph
     :cache-key cache-key
     :cache-entry cache-entry
     :plugin-manifest plugin-manifest
     :risk-records risk-records}))
