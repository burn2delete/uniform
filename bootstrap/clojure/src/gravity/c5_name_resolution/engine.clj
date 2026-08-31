(ns gravity.c5-name-resolution.engine
  (:require [gravity.c5-name-resolution.artifacts :as artifacts]
            [gravity.c5-name-resolution.bindings :as bindings]
            [gravity.c5-name-resolution.config :as config]
            [gravity.c5-name-resolution.diagnostics :as diagnostics]
            [gravity.c5-name-resolution.lexical :as lexical]
            [gravity.c5-name-resolution.operations :as ops]
            [gravity.c5-name-resolution.resolution :as resolution]))

(defn compiler-c5-resolution-source-artifact [source-path source-text]
  (let [records (ops/read-source-form-records source-path source-text) forms (mapv :form records)
        _ (ops/validate-ns-syntax! source-path forms) module (ops/parse-module source-path forms)
        overrides ((ops/op-fn :c5-resolution-source-overrides config/c5-resolution-source-overrides) module)
        _ ((ops/op-fn :c5-resolution-validate-overrides! diagnostics/c5-resolution-validate-overrides!) source-path module overrides)
        c4-artifact (ops/compiler-c4-macro-source-artifact source-path source-text)
        module-artifact (ops/module-source-artifact source-path source-text)
        expanded-stream (:expanded-syntax-stream c4-artifact)
        alias-table ((ops/op-fn :c5-alias-table bindings/c5-alias-table) module)
        import-export-table ((ops/op-fn :c5-import-export-table bindings/c5-import-export-table) module)
        definition-bindings ((ops/op-fn :c5-definition-bindings bindings/c5-definition-bindings) module module-artifact c4-artifact)
        macro-bindings ((ops/op-fn :c5-macro-bindings bindings/c5-macro-bindings) module c4-artifact)
        lexical-scope-graph ((ops/op-fn :c5-local-scope-graph lexical/c5-local-scope-graph) module expanded-stream)
        binding-table ((ops/op-fn :c5-binding-table resolution/c5-binding-table) module definition-bindings macro-bindings lexical-scope-graph expanded-stream)
        dependency-graph ((ops/op-fn :c5-dependency-graph artifacts/c5-dependency-graph) module)
        cross-profile-report ((ops/op-fn :c5-cross-profile-edge-report artifacts/c5-cross-profile-edge-report) module dependency-graph)
        invalidation ((ops/op-fn :c5-incremental-invalidation-keys artifacts/c5-incremental-invalidation-keys) module c4-artifact binding-table dependency-graph)
        namespace-analysis ((ops/op-fn :c5-namespace-analysis-artifact artifacts/c5-namespace-analysis-artifact) module binding-table alias-table import-export-table dependency-graph cross-profile-report)
        verifier ((ops/op-fn :c5-resolution-verification-report artifacts/c5-resolution-verification-report) binding-table lexical-scope-graph dependency-graph cross-profile-report invalidation)
        artifact-base {:kind :gravity/stage0-c5-name-resolution-artifact :task "P06-D084" :document-set ["C5"]
                       :governing-document (ops/op-value :c5-resolution-governing-document config/c5-resolution-governing-document)
                       :pass {:name :c5-name-resolution-and-namespace-analyzer :input :c4-expanded-syntax-artifact :output :namespace-analysis
                              :requires [:expanded-syntax-stream :macro-expansion-context :alias-table :package-dependency-graph :active-profile :active-target :language-facets]
                              :preserves [:source-spans :syntax-ids :hygiene :generated-origin :profile :target :effects :capabilities]
                              :emits [:namespace-analysis :binding-table :alias-table :import-export-table :lexical-scope-graph :dependency-graph :cross-profile-edge-report :resolution-diagnostics :incremental-invalidation-keys]
                              :rejects (ops/op-value :c5-resolution-diagnostic-ids config/c5-resolution-diagnostic-ids)}
                       :source-overrides overrides
                       :module (select-keys module [:module :source-path :profile :target :effects :capabilities :safety :metadata])
                       :c4-macro-expansion-artifact (select-keys c4-artifact [:kind :artifact-id :expanded-syntax-stream :macro-expansion-trace :macro-environment :generated-origin-source-map])
                       :namespace-analysis namespace-analysis :binding-table binding-table :alias-table alias-table
                       :import-export-table import-export-table :lexical-scope-graph lexical-scope-graph
                       :dependency-graph dependency-graph :cross-profile-edge-report cross-profile-report
                       :resolution-diagnostics ((ops/op-fn :c5-resolution-diagnostics artifacts/c5-resolution-diagnostics) module)
                       :incremental-invalidation-keys invalidation :resolution-verification-report verifier
                       :rejected-design-coverage (ops/op-value :c5-resolution-rejected-designs config/c5-resolution-rejected-designs)
                       :diagnostics []}
        _ ((ops/op-fn :c5-resolution-validate! diagnostics/c5-resolution-validate!) source-path artifact-base)
        capability-proof ((ops/op-fn :c5-resolution-capability-proof artifacts/c5-resolution-capability-proof) artifact-base)
        conformance {:documents ["C5"] :task "P06-D084" :required-diagnostic-ids (ops/op-value :c5-resolution-diagnostic-ids config/c5-resolution-diagnostic-ids)
                     :namespace-analysis-status :complete :binding-table-status :complete :alias-table-status :complete
                     :import-export-status :complete :lexical-scope-status :complete :dependency-graph-status :complete
                     :cross-profile-status :complete :diagnostic-status :complete :invalidation-status :stable :status :complete}
        artifact (assoc artifact-base :capability-based-proof capability-proof :c5-resolution-results conformance)]
    (assoc artifact :artifact-id (ops/c4-artifact-id artifact))))

(defn compiler-c5-resolution-file-artifact [path]
  ((ops/op-fn :compiler-c5-resolution-source-artifact compiler-c5-resolution-source-artifact) path (slurp path)))
