

(defn validate-stage0-compiled-conformance-backend-report!
  [module backend-report]
  (when-not (:lowered-artifact-manifest backend-report)
    (compiled-conformance-fail!
     "TEST6004" module backend-report
     {:missing-fields [:lowered-artifact-manifest]})))