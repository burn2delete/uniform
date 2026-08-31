

(defn validate-stage0-compiled-math!
  [module]
  (let [manifest (stage0-compiled-math-manifest module)]
    (when (stage0-compiled-math-suite-present? module)
      (let [suite (numeric-mode-suite manifest)]
        (numeric-validate-math1! (:source-path module) manifest suite)
        (numeric-validate-math7! (:source-path module) manifest suite)
        (numeric-validate-math8! (:source-path module) manifest suite)))
    (validate-stage0-compiled-floating-literals! module manifest)))