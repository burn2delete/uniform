
(defn-
 sh05-macro-artifact-from-c3-artifact*
 "Expand an already authenticated C3 artifact without rebuilding C2/C3.\n\n  The one-argument form derives the supplied source path from the authenticated\n  C3 source-unit record.  Callers that have a path-scoped snapshot should use\n  the two-argument form so path rebinding is checked against that snapshot.\n  "
 ([c3-artifact]
  (sh05-macro-artifact-from-c3-artifact*
   (get-in c3-artifact [:c2-reader-artifact :source-unit-record :path])
   c3-artifact
   false))
 ([source-path c3-artifact]
  (sh05-macro-artifact-from-c3-artifact*
   source-path
   c3-artifact
   false))
 ([source-path c3-artifact legacy?]
  (sh05-macro-artifact-from-c3-artifact*
   source-path
   c3-artifact
   legacy?
   nil))
 ([source-path c3-artifact legacy? invocation]
  (gravity.macro/assemble
   source-path
   c3-artifact
   legacy?
   invocation
   ^{:line 148, :column 101}
   (macro-stage-operations))))

(defn sh05-macro-artifact-from-c3-artifact
  ([c3-artifact]
   (sh05-macro-artifact-from-c3-artifact*
    (get-in c3-artifact [:c2-reader-artifact :source-unit-record :path])
    c3-artifact false))
  ([source-path c3-artifact]
   (sh05-macro-artifact-from-c3-artifact source-path c3-artifact nil))
  ([source-path c3-artifact invocation]
   (sh05-macro-artifact-from-c3-artifact* source-path c3-artifact false invocation)))

(defn sh05-macro-source-artifact
  [source-path source-text]
  ;; Compatibility wrapper: the historical source API still owns C3
  ;; construction, while the new parent API above owns C4 consumption.
  (sh05-macro-artifact-from-c3-artifact*
   source-path
   (compiler-c3-syntax-source-artifact source-path source-text)
   true))

(defn sh05-macro-file-artifact
  [source-path]
  (let [c2-artifact (compiler-c2-reader-file-artifact source-path)
        source-text (c2-reader-artifact-source-text source-path c2-artifact)]
    (sh05-macro-source-artifact source-path source-text)))
