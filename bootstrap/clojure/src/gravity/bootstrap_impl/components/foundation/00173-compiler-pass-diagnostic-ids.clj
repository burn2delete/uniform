
(def compiler-pass-diagnostic-ids compiler-pass-manifest/compiler-pass-diagnostic-ids)
(def compiler-pass-default-stage-order compiler-pass-manifest/compiler-pass-default-stage-order)
(def compiler-pass-contract-required-fields compiler-pass-manifest/compiler-pass-contract-required-fields)
(def compiler-pass-durable-facts compiler-pass-manifest/compiler-pass-durable-facts)
(def compiler-pass-default-contracts compiler-pass-manifest/compiler-pass-default-contracts)
(def compiler-pass-default-diagnostic-schema compiler-pass-manifest/compiler-pass-default-diagnostic-schema)
(def compiler-pass-default-diagnostic-catalog compiler-pass-manifest/compiler-pass-default-diagnostic-catalog)
(def compiler-pass-default-diagnostic-fixtures compiler-pass-manifest/compiler-pass-default-diagnostic-fixtures)
(def compiler-pass-default-cache-key-schema compiler-pass-manifest/compiler-pass-default-cache-key-schema)
(def compiler-pass-default-cache-keys compiler-pass-manifest/compiler-pass-default-cache-keys)
(def compiler-pass-default-cache-entries compiler-pass-manifest/compiler-pass-default-cache-entries)
(def compiler-pass-default-proof-reuse-records compiler-pass-manifest/compiler-pass-default-proof-reuse-records)
(def compiler-pass-default-speculative-reuse-records compiler-pass-manifest/compiler-pass-default-speculative-reuse-records)
(def compiler-pass-default-plugin-manifest compiler-pass-manifest/compiler-pass-default-plugin-manifest)
(def compiler-pass-default-plugin-pass-contracts compiler-pass-manifest/compiler-pass-default-plugin-pass-contracts)
(def compiler-pass-default-plugin-execution-traces compiler-pass-manifest/compiler-pass-default-plugin-execution-traces)
(def compiler-pass-default-release-gate-report compiler-pass-manifest/compiler-pass-default-release-gate-report)
(defn compiler-pass-contract
  [pass owner-doc input output requires preserves invalidates regenerates emits
   rejects risk evidence-class]
  (compiler-pass-manifest/compiler-pass-contract
   pass owner-doc input output requires preserves invalidates regenerates emits
   rejects risk evidence-class))
(defn compiler-pass-default-risk-classification
  [contracts]
  (compiler-pass-manifest/compiler-pass-default-risk-classification contracts))
(defn compiler-pass-default-trust-report
  [contracts risk-records]
  (compiler-pass-manifest/compiler-pass-default-trust-report
   contracts risk-records))
(defn compiler-pass-merge-record-overrides
  [defaults overrides id-key]
  (compiler-pass-manifest/compiler-pass-merge-record-overrides
   defaults overrides id-key))
(defn compiler-pass-suite
  [manifest]
  (compiler-pass-manifest/compiler-pass-suite manifest))
(defn compiler-pass-fail!
  [id source-path manifest record extra]
  (compiler-pass-manifest/compiler-pass-fail!
   id source-path manifest record extra))
(defn compiler-pass-missing-fields
  [record required-fields]
  (compiler-pass-manifest/compiler-pass-missing-fields record required-fields))
(defn compiler-pass-validate-pipeline!
  [source-path manifest suite]
  (compiler-pass-manifest/compiler-pass-validate-pipeline!
   source-path manifest suite))
(defn compiler-pass-validate-diagnostics!
  [source-path manifest suite]
  (compiler-pass-manifest/compiler-pass-validate-diagnostics!
   source-path manifest suite))
(defn compiler-pass-validate-incremental!
  [source-path manifest suite]
  (compiler-pass-manifest/compiler-pass-validate-incremental!
   source-path manifest suite))
(defn compiler-pass-validate-plugins!
  [source-path manifest suite]
  (compiler-pass-manifest/compiler-pass-validate-plugins!
   source-path manifest suite))
(defn compiler-pass-validate-verification!
  [source-path manifest suite]
  (compiler-pass-manifest/compiler-pass-validate-verification!
   source-path manifest suite))
(defn compiler-pass-capability-proof
  [suite]
  (compiler-pass-manifest/compiler-pass-capability-proof suite))
(defn compiler-pass-source-artifact
  [source-path source-text]
  (compiler-pass-manifest/compiler-pass-source-artifact-from-upstream
   source-path
   (math-conformance-source-artifact source-path source-text)))

(def checked-core-stage-order
  [:read-source
   :build-syntax
   :macro-expand
   :resolve-names
   :lower-to-core
   :type-check
   :effect-check
   :profile-validate
   :capability-validate
   :ownership-check
   :safety-analyze])

(def checked-core-diagnostic-ids
  ["C1-EVIDENCE-DROP"
   "C2-HASH"
   "C3-ORIGIN"
   "C4-TRACE"
   "C5-UNRESOLVED"
   "C6-VERIFY"
   "C7-VERIFY"
   "C8-CAPABILITY"
   "C9-LINEAR-LEAK"
   "C10-NO-OUTCOME"])

(def checked-core-diagnostic-messages
  {"C1-EVIDENCE-DROP" "checked-core pipeline dropped required evidence"
   "C2-HASH" "reader source identity or incremental hash is missing"
   "C3-ORIGIN" "syntax object origin chain is missing"
   "C4-TRACE" "macro expansion trace is missing or unreplayable"
   "C5-UNRESOLVED" "name resolution did not emit stable binding evidence"
   "C6-VERIFY" "core lowering verifier did not accept the core artifact"
   "C7-VERIFY" "typed-core verifier did not accept type facts"
   "C8-CAPABILITY" "effect checker did not emit capability proof evidence"
   "C9-LINEAR-LEAK" "ownership checker did not emit linear resource evidence"
   "C10-NO-OUTCOME" "safety analysis did not classify all operations"})

(def checked-core-override-diagnostics
  {:evidence-drop ["C1-EVIDENCE-DROP" :effect-check]
   :reader-hash ["C2-HASH" :read-source]
   :syntax-origin ["C3-ORIGIN" :build-syntax]
   :macro-trace ["C4-TRACE" :macro-expand]
   :unresolved-binding ["C5-UNRESOLVED" :resolve-names]
   :core-verify ["C6-VERIFY" :lower-to-core]
   :type-verify ["C7-VERIFY" :type-check]
   :capability-proof ["C8-CAPABILITY" :effect-check]
   :linear-flow ["C9-LINEAR-LEAK" :ownership-check]
   :safety-outcome ["C10-NO-OUTCOME" :safety-analyze]})

(defn checked-core-artifact-id
  [artifact]
  (str "sha256:" (sha256-hex (pr-str artifact))))

(defn checked-core-stage-record
  [stage owner-doc input output input-artifact output-artifact preserves emits]
  {:stage stage
   :owner-doc owner-doc
   :input input
   :output output
   :input-artifact-id (if (string? input-artifact)
                        input-artifact
                        (checked-core-artifact-id input-artifact))
   :output-artifact-id (checked-core-artifact-id output-artifact)
   :preserves preserves
   :emits emits
   :verifier-result :passed
   :diagnostics []})

(defn checked-core-source-overrides
  [module]
  (get-in module [:metadata :compiler :checked-core] {}))

(defn checked-core-fail!
  [id source-path artifact subject extra]
  (fail! id
         (get checked-core-diagnostic-messages id "checked-core validation failed")
         (merge {:source-span (source-span source-path 0)
                 :diagnostic-family :checked-core-integration
                 :stage (:stage subject)
                 :profile (get-in artifact [:module :profile])
                 :target (get-in artifact [:module :target])
                 :input-artifact-id (:input-artifact-id subject)
                 :output-artifact-id (:output-artifact-id subject)
                 :remediation "Preserve or regenerate the required compiler evidence before MIR construction."}
                extra)))

(defn checked-core-stage
  [artifact stage]
  (first (filter #(= stage (:stage %)) (:stage-artifact-records artifact))))

(defn checked-core-validate-overrides!
  [source-path artifact]
  (when-let [fail-kind (get-in artifact [:source-overrides :fail])]
    (let [[id stage] (get checked-core-override-diagnostics fail-kind)
          subject (checked-core-stage artifact stage)]
      (when id
        (checked-core-fail! id source-path artifact subject
                            {:missing-fact fail-kind
                             :missing-fields [fail-kind]})))))