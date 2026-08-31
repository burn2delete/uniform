(let [inputs* semantic-tail-stage2-whole-compiler-inputs
      cross-evidence* semantic-tail-stage2-whole-compiler-cross-evidence
      final-artifact* semantic-tail-stage2-whole-compiler-final-artifact]
  (defn p15-s23-stage2-whole-language-compiler-source-artifact*
    [source-path]
    (let [inputs (inputs* source-path)
          cross-evidence (cross-evidence* source-path inputs)]
      (final-artifact* source-path inputs cross-evidence))))

(doseq [helper '[semantic-tail-stage2-whole-compiler-inputs
                 semantic-tail-stage2-whole-compiler-cross-evidence
                 semantic-tail-stage2-whole-compiler-final-artifact]]
  (ns-unmap *ns* helper))
