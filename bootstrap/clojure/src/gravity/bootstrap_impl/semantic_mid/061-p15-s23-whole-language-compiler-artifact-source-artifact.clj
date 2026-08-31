(defn p15-s23-whole-language-compiler-artifact-source-artifact
  [source-path]
  (p15-s23-context-artifact
   :whole-language-compiler-artifact source-path
   (fn [] (semantic-mid-whole-language-compiler-build source-path))))

(doseq [helper '[semantic-mid-whole-language-compiler-inputs
                 semantic-mid-whole-language-compiler-records
                 semantic-mid-whole-language-compiler-artifact-inputs
                 semantic-mid-whole-language-compiler-artifact-result
                 semantic-mid-whole-language-compiler-build]]
  (ns-unmap *ns* helper))
