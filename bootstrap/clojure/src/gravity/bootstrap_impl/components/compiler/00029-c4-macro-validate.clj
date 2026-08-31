

(defn c4-macro-validate!
  [source-path artifact]
  (let [proof (c4-macro-capability-proof artifact)]
    (doseq [[field id] [[:syntax-input-output-valid? "C4-RETURN"]
                        [:deterministic-expansion-trace? "C4-TRACE"]
                        [:hygiene-and-capture-recorded? "C4-HYGIENE"]
                        [:build-effects-authorized? "C4-BUILD-EFFECT"]
                        [:generated-origin-present? "C4-TRACE"]
                        [:generated-unsafe-checked? "C4-GENERATED-UNSAFE"]
                        [:cache-replay-guarded? "C4-TRACE"]
                        [:diagnostics-covered? "C4-TRACE"]
                        [:self-hosting-comparison-ready? "C4-TRACE"]]]
      (when-not (get proof field)
        (c4-macro-fail! id source-path {:stage :macro-expansion}
                        {:missing-fields [field]}))))
  :complete)

(defn compiler-c4-macro-source-artifact
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (c4-macro-source-overrides module)
        _ (c4-macro-validate-overrides! source-path overrides)
        c3-artifact (compiler-c3-syntax-source-artifact source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        expansion-input (c4-expansion-input module c3-artifact macro-artifact)
        macro-environment (c4-macro-environment macro-artifact)
        expanded-stream (c4-expanded-syntax-stream macro-artifact)
        trace-records (c4-trace-records macro-artifact)
        safety-declarations (c4-macro-safety-declarations macro-environment)
        cache-key (c4-expansion-cache-key expansion-input trace-records)
        artifact-base
        {:kind :gravity/stage0-c4-macro-expansion-artifact
         :task "P06-D083"
         :document-set ["C4"]
         :governing-document c4-macro-governing-document
         :pass {:name :c4-macro-expansion-engine
                :input :c3-syntax-object-artifact
                :output :expanded-syntax-with-macro-proof
                :requires [:syntax-object-graph :macro-environment
                           :build-grants :target-manifest]
                :preserves [:source-spans :generated-origin :hygiene
                            :metadata :profile :safety-metadata]
                :emits [:expanded-syntax-stream :macro-expansion-trace
                        :hygiene-capture-records :build-effect-log
                        :macro-safety-declarations
                        :generated-origin-source-map
                        :expansion-cache-key :expansion-diagnostics]
                :rejects c4-macro-diagnostic-ids}
         :source-overrides overrides
         :c3-syntax-object-artifact
         (select-keys c3-artifact [:kind :artifact-id :syntax-object-stream
                                   :syntax-verification-report])
         :macro-expansion-input expansion-input
         :macro-environment macro-environment
         :expanded-syntax-stream expanded-stream
         :macro-expansion-trace trace-records
         :hygiene-capture-records (c4-hygiene-capture-records trace-records)
         :build-effect-log (c4-build-effect-log module trace-records)
         :macro-safety-declarations safety-declarations
         :generated-origin-source-map (c4-generated-origin-source-map
                                       trace-records expanded-stream)
         :expansion-cache-key cache-key
         :trace-replay-report (c4-trace-replay-report trace-records cache-key)
         :macro-safety-report (c4-macro-safety-report trace-records
                                                      safety-declarations)
         :self-hosting-comparison-inputs
         {:artifact :gravity/macro-self-hosting-comparison-inputs
          :seed-trace-hash (c4-artifact-id trace-records)
          :gravity-trace-slot :pending-self-host
          :status :ready}
         :rejected-design-coverage c4-macro-rejected-designs
         :diagnostics []}
        _ (c4-macro-validate! source-path artifact-base)
        capability-proof (c4-macro-capability-proof artifact-base)
        conformance {:documents ["C4"]
                     :task "P06-D083"
                     :required-diagnostic-ids c4-macro-diagnostic-ids
                     :input-output-status :complete
                     :trace-status :complete
                     :hygiene-status :complete
                     :build-effect-status :complete
                     :generated-origin-status :complete
                     :generated-unsafe-status :complete
                     :cache-replay-status :complete
                     :self-hosting-comparison-status :ready
                     :diagnostic-status :complete
                     :status :complete}
        artifact (assoc artifact-base
                        :capability-based-proof capability-proof
                        :c4-macro-results conformance)]
    (assoc artifact :artifact-id (c4-artifact-id artifact))))

(defn compiler-c4-macro-file-artifact
  [path]
  (compiler-c4-macro-source-artifact path (slurp path)))

(def c5-resolution-diagnostic-ids
  c5/c5-resolution-diagnostic-ids)
(def c5-resolution-governing-document
  c5/c5-resolution-governing-document)
(def c5-resolution-rejected-designs
  c5/c5-resolution-rejected-designs)
(def c5-resolution-override-diagnostics
  c5/c5-resolution-override-diagnostics)
(def c5-special-form-symbols
  c5/c5-special-form-symbols)
(def c5-core-auto-imports
  c5/c5-core-auto-imports)
(def c5-type-auto-imports
  c5/c5-type-auto-imports)

(declare c5-resolution-ops
         c5-resolution-source-overrides
         c5-resolution-message
         c5-resolution-fail!
         c5-resolution-validate-overrides!
         c5-package-record
         c5-binding-id
         c5-binding-identity
         c5-definition-binding
         c5-special-form-binding
         c5-core-binding
         c5-type-binding
         c5-import-binding
         c5-alias-table
         c5-import-export-table
         c5-definition-bindings
         c5-macro-bindings
         c5-param-symbols
         c5-local-bindings-from-params
         c5-let-binding-symbols
         c5-local-scope-graph
         c5-bindings-by-name
         c5-resolve-qualified-symbol
         c5-resolution-record
         c5-binding-table
         c5-namespace-analysis-artifact
         c5-dependency-graph
         c5-cross-profile-edge-report
         c5-incremental-invalidation-keys
         c5-resolution-diagnostics
         c5-resolution-verification-report
         c5-resolution-capability-proof
         c5-resolution-validate!
         compiler-c5-resolution-source-artifact
         compiler-c5-resolution-file-artifact)

(defn- c5-call
  [operation & args]
  (c5/with-operations (c5-resolution-ops)
    #(apply operation args)))

(defn c5-resolution-source-overrides
  [module]
  (c5-call c5/c5-resolution-source-overrides module))

(defn c5-resolution-message
  [id]
  (c5-call c5/c5-resolution-message id))

(defn c5-resolution-fail!
  [id source-path subject extra]
  (c5-call c5/c5-resolution-fail! id source-path subject extra))

(defn c5-resolution-validate-overrides!
  [source-path module overrides]
  (c5-call c5/c5-resolution-validate-overrides!
           source-path module overrides))

(defn c5-package-record
  [module]
  (c5-call c5/c5-package-record module))

(defn c5-binding-id
  [binding]
  (c5-call c5/c5-binding-id binding))

(defn c5-binding-identity
  [binding]
  (c5-call c5/c5-binding-identity binding))

(defn c5-definition-binding
  [module definition artifact-id]
  (c5-call c5/c5-definition-binding module definition artifact-id))

(defn c5-special-form-binding
  [sym module]
  (c5-call c5/c5-special-form-binding sym module))

(defn c5-core-binding
  [sym module]
  (c5-call c5/c5-core-binding sym module))

(defn c5-type-binding
  [sym module]
  (c5-call c5/c5-type-binding sym module))

(defn c5-import-binding
  [module dependency imported-name artifact-id]
  (c5-call c5/c5-import-binding module dependency imported-name artifact-id))

(defn c5-alias-table
  [module]
  (c5-call c5/c5-alias-table module))

(defn c5-import-export-table
  [module]
  (c5-call c5/c5-import-export-table module))

(defn c5-definition-bindings
  [module module-artifact c4-artifact]
  (c5-call c5/c5-definition-bindings module module-artifact c4-artifact))

(defn c5-macro-bindings
  [module c4-artifact]
  (c5-call c5/c5-macro-bindings module c4-artifact))