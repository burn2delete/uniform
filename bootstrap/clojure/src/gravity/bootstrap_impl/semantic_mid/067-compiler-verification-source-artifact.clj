(defn compiler-verification-source-artifact
  [source-path source-text]
  (let [context
        (semantic-mid-compiler-verification-context source-path source-text)
        artifact (semantic-mid-compiler-verification-artifact context)
        capability-proof (semantic-mid-compiler-verification-proof artifact)
        conformance (semantic-mid-compiler-verification-conformance)]
    (assoc artifact
           :capability-based-proof capability-proof
           :compiler-verification-results conformance)))

(doseq [helper '[semantic-mid-compiler-verification-context
                 semantic-mid-compiler-verification-diagnostics-artifact
                 semantic-mid-compiler-verification-incremental-artifact
                 semantic-mid-compiler-verification-evidence-artifact
                 semantic-mid-compiler-verification-artifact
                 semantic-mid-compiler-verification-proof
                 semantic-mid-compiler-verification-conformance]]
  (ns-unmap *ns* helper))
