

(defn stage0-unsafe-forms
  [form]
  (cond
    (and (seq? form) (= 'quote (first form)))
    []

    (and (seq? form) (= 'unsafe (first form)))
    [form]

    (seq? form)
    (mapcat stage0-unsafe-forms form)

    (coll? form)
    (mapcat stage0-unsafe-forms form)

    :else
    []))

(defn validate-stage0-unsafe-island!
  [module form]
  (let [metadata (second form)
        source-path (:source-path module)
        safety-mode (:safety module)]
    (when (#{:safe :safe-optimized} safety-mode)
      (fail! "SAFE6-UNSAFE-FORBIDDEN"
             "unsafe islands are forbidden in this stage0 executable safety mode"
             {:source-span {:source source-path}
              :safety-mode safety-mode
              :safety-outcome :rejected
              :operator 'unsafe
              :remediation "Remove the unsafe island or move it behind an audited safe wrapper in a mode that permits unsafe islands."}))
    (when-not (map? metadata)
      (fail! "SAFE6-MISSING-METADATA"
             "unsafe island requires an audit metadata map"
             {:source-span {:source source-path}
              :safety-mode safety-mode
              :safety-outcome :rejected
              :operator 'unsafe
              :missing-fact :unsafe-audit-metadata
              :remediation "Attach SAFE6 metadata with owner, invariant, effects, capabilities, review policy, and safe boundary."}))
    (let [missing (seq (sort (set/difference stage0-unsafe-required-metadata
                                             (set (keys metadata)))))]
      (when missing
        (fail! "SAFE6-MISSING-METADATA"
               "unsafe island lacks required audit metadata"
               {:source-span {:source source-path}
                :safety-mode safety-mode
                :safety-outcome :rejected
                :operator 'unsafe
                :missing-fact (first missing)
                :remediation "Provide the full SAFE6 unsafe island audit record before compiling executable code."})))))