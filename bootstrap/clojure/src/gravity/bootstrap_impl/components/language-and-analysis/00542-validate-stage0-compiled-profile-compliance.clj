

(defn validate-stage0-compiled-profile-compliance!
  [module profile-report]
  (when-not (and (:profile profile-report)
                 (:target profile-report))
    (compiled-conformance-fail!
     "TEST4001" module profile-report
     {:missing-fields [:profile :target]})))

(defn validate-stage0-compiled-safety-conformance!
  [module safety-report]
  (when-not (p14-present? (:unsafe-audit-records safety-report))
    (compiled-conformance-fail!
     "TEST5002" module safety-report
     {:missing-fields [:unsafe-audit-records]})))