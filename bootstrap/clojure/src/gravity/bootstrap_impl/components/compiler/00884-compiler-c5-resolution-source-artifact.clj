

(defn compiler-c5-resolution-source-artifact
  ([source-path source-text]
   (sh06-resolution-source-artifact source-path source-text))
  ([source-path source-text module-inputs]
   (sh06-resolution-source-artifact source-path source-text module-inputs))
  ([source-path source-text module-inputs reader-source-path]
   (compiler-c5-resolution-source-artifact source-path source-text module-inputs reader-source-path
    (when reader-source-path
      {:reader-source-path reader-source-path
       :authenticated-envelope-options {:source :development-current}})))
  ([source-path source-text module-inputs reader-source-path invocation]
   (let [macro-source-mode (or (:macro-source-mode invocation) :pinned)
         reader-invocation (if (map? invocation)
                             (dissoc invocation :macro-source-mode)
                             invocation)]
     (when-not (string? reader-source-path)
       (sh03-reader-read-selected-source-bytes! source-path reader-source-path))
     (let [syntax (compiler-c3-syntax-source-artifact
                   source-path source-text (reader-project-context-for-source source-path)
                   nil nil reader-source-path reader-invocation)
         parent (sh05-macro-artifact-from-c3-artifact
                 source-path syntax reader-invocation macro-source-mode)]
       ;; Keep the macro-source choice explicit at the SH06 boundary too.  The
       ;; adapter strips it before Reader validation, while SH06 carries it in
       ;; its operation binding for the SH05 verifier.
       (sh06-resolution-artifact-from-sh05-artifact
        source-path parent module-inputs reader-source-path invocation)))))

(defn compiler-c5-resolution-file-artifact
  ([source-path] (sh06-resolution-file-artifact source-path))
  ([source-path module-inputs] (sh06-resolution-file-artifact source-path module-inputs)))
