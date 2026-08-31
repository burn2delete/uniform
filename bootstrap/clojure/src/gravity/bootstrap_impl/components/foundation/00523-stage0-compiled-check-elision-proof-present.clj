

(defn stage0-compiled-check-elision-proof-present?
  [record]
  (or (perf-present? (:proof-id record))
      (perf-present? (:proof record))
      (perf-present? (:certificate record))
      (perf-present? (:certificate-id record))
      (perf-present? (:dominating-proof record))))

(defn validate-stage0-compiled-check-elision!
  [module record]
  (let [source-path (:source-path module)
        erased? (or (true? (:erased? record))
                    (seq (:erased-checks record)))
        proof-present? (stage0-compiled-check-elision-proof-present?
                        record)]
    (when (and erased? (not proof-present?))
      (fail! "PERF10-PROOF-MISSING"
             "compiled performance metadata erases a check without proof"
             {:source-span {:source source-path}
              :profile (:profile module)
              :target (:target module)
              :check-class (:check-class record)
              :operation (:operation record)
              :ir-node (:ir-node record)
              :missing-proof :dominating-proof
              :pass-id :stage0-compiled-performance-gate
              :diagnostic-family :compiled-performance-validation
              :remediation "Keep the runtime check or attach a dominating proof and certificate before claiming check elision."}))))