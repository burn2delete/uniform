

(def p15-s23-reference-runtime-rule-keys
  #{:runtime :kernel :runtime-rule-record :kernel-rule-record
    :runtime-engine :runtime-kernel-engine :runtime-rule-hash
    :runtime-kernel-rule-hash :stage2-compiler-source-path
    :stage2-compiler-source-content-hash :runtime-source-path
    :runtime-source-content-hash :runtime-artifact-source-path
    :runtime-artifact-source-content-hash :runtime-artifact-plan
    :runtime-artifact-authoritative-module :runtime-artifact-effects
    :runtime-artifact-capabilities :runtime-artifact-providers
    :runtime-contract-definitions :runtime-contract-definition-hash
    :runtime-contract-validation-record
    :runtime-contract-derived-facts-hash
    :runtime-artifact-function-hashes :runtime-artifact-hash-input
    :runtime-artifact-function :runtime-artifact-concat-function
    :runtime-artifact-println-function
    :runtime-artifact-println-two-function
    :runtime-artifact-closed-plan-function
    :runtime-artifact-closed-plan-function-hash
    :runtime-artifact-closed-plan-helper-functions
    :runtime-artifact-closed-function-hashes
    :runtime-artifact-println-over-two-boundary
    :runtime-artifact-functions
    :runtime-artifact-generic-bridge-residual?
    :runtime-artifact-generic-emitter-effect-summary-credited?
    :runtime-artifact-hash :runtime-rule-source})

(def p15-s23-reference-runtime-expected-executor-hash
  "sha256:4cb663bdff5d1a8c49438555e0ac4127eccb3baa3a1b81e38b128b8ebeb088e9")

(def p15-s23-reference-runtime-expected-kernel-hash
  "sha256:d4058fa9f7791b4478045247997a1a60471a85f1ddfa405447fb6029d061acff")

(def p15-s23-reference-runtime-executor-keys
  #{:plan-emitter :self-hosting-claims :seed-boundary :implemented-by
    :required-evidence-links :stage :lineage :executed-by
    :rejected-diagnostic-contract :accepted-scope :emits :output :status
    :nucleus :runtime-kernel :proof-version :instruction-rules :preserves
    :execution-contract :next-required-capability :artifact :engine :input
    :builtin-rules :governing-documents :module-responsibility
    :compiler-stage})

(def p15-s23-reference-runtime-kernel-keys
  #{:self-hosting-claims :seed-boundary :verified-by :implemented-by
    :required-evidence-links :stage :lineage :executed-by
    :rejected-diagnostic-contract :accepted-scope :emits :output
    :runtime-manifest :runtime-primitives :status :proof-version
    :instruction-rules :preserves :next-required-capability :artifact
    :engine :input :governing-documents :compiled-by
    :module-responsibility :compiler-stage})

(def p15-s23-reference-runtime-plan-keys
  #{:compatibility-kind :diagnostics :plan-id :functions :effect-summary
    :source :binding-table :instruction-summary :module :kind :compiler
    :entrypoint})

(def p15-s23-reference-runtime-plan-module-keys
  #{:capabilities :providers :module :source-path :effects :safety :target
    :profile})

(def p15-s23-reference-runtime-plan-source-keys #{:path :sha256})

(def p15-s23-reference-runtime-plan-function-keys
  #{:name :instructions :params :definition-form :binding :body-form-count
    :arity :body})

(def p15-s23-reference-runtime-authoritative-module-keys
  #{:capabilities :providers :imports :module :source-path :exports
    :requires :effects :safety :target :doc :metadata :profile :forms})

(def p15-s23-reference-runtime-expected-plan-id
  "sha256:633c590fef790e6f7c5727b4fce44b05ef49c2ecf2094158103f53f958f6e979")

(def p15-s23-reference-runtime-expected-authoritative-module-hash
  "sha256:99128713ef7f6c2540239ebaf6f58f08fb42ee6f7a4d8ff1d39ef588b6354a32")

(defn p15-s23-reference-runtime-pinned-file-binding
  [path expected-byte-count expected-content-hash]
  (try
    (when (string? path)
      (let [file (java.io.File. path)]
        (when (and (.isAbsolute file)
                   (.isFile file)
                   (= (.getCanonicalPath file) path)
                   (= expected-byte-count (.length file)))
          (let [bytes (java.nio.file.Files/readAllBytes (.toPath file))
                content-hash (str "sha256:" (sha256-bytes-hex bytes))]
            (when (= expected-content-hash content-hash)
              {:canonical-path (.getCanonicalPath file)
               :byte-count (alength bytes)
               :content-hash content-hash})))))
    (catch Exception _ nil)))

(defn p15-s23-reference-runtime-existing-canonical-path
  [path]
  (try
    (when (string? path)
      (let [file (java.io.File. path)]
        (when (and (.isAbsolute file) (.isFile file)
                   (= (.getCanonicalPath file) path))
          (.getCanonicalPath file))))
    (catch Exception _ nil)))