

(defn validate-stage0-compiled-profiler!
  [module profiler-report]
  (when-not (p13-present? (get-in profiler-report
                                  [:check-elision-report :evidence]))
    (compiled-tooling-fail!
     "T11003" module profiler-report
     {:missing-fields [:check-elision-evidence]})))

(defn validate-stage0-compiled-safety-audit!
  [module safety-audit-report]
  (when (and (seq (:unsafe-islands safety-audit-report))
             (or (false? (:unsafe-island-evidence? safety-audit-report))
                 (not (p13-present? (:proof-index safety-audit-report)))))
    (compiled-tooling-fail!
     "T12001" module safety-audit-report
     {:missing-fields [:unsafe-island-evidence]})))