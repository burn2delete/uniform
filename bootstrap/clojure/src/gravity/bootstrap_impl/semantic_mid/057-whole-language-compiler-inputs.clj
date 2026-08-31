(defn- semantic-mid-whole-language-compiler-inputs
  [source-path]
  (let [source-data (p15-s23-compiler-source-form-record source-path)
        proof-contract
        (p15-s23-compiler-def-value
         source-path (:forms source-data)
         'p15-s23-whole-language-compiler-artifact)
        inventory-artifact
        (p15-s23-compiler-source-inventory-source-artifact source-path)
        pipeline-artifact
        (p15-s23-compiler-pipeline-manifest-source-artifact source-path)
        source-syntax-artifact
        (p15-s23-source-syntax-serialization-proof-source-artifact source-path)
        core-artifact
        (p15-s23-core-lowering-diagnostic-preservation-source-artifact
         source-path)
        runtime-artifact
        (p15-s23-runtime-manifest-capability-enforcement-source-artifact
         source-path)
        accepted-artifact
        (p15-s23-accepted-app-execution-source-artifact source-path)
        rejected-artifact
        (p15-s23-rejected-app-diagnostic-source-artifact source-path)
        rebuild-artifact
        (p15-s23-reproducible-rebuild-log-source-artifact source-path)
        stage-comparison-artifact
        (p15-s23-stage-comparison-report-source-artifact source-path)
        conformance-artifact
        (p15-s23-self-hosting-conformance-report-source-artifact source-path)
        provenance-artifact
        (p15-s23-provenance-attestation-source-artifact source-path)
        tcb-artifact
        (p15-s23-tcb-delta-record-source-artifact source-path)
        unsafe-artifact
        (p15-s23-unsafe-audit-report-source-artifact source-path)
        hosted-compiler-fn
        (resolve
         'gravity.bootstrap/hosted-core-compiled-compiler-proof-file-artifact)
        _ (when-not hosted-compiler-fn
            (p15-s23-whole-language-compiler-fail!
             "P15S23W003" source-path nil
             {:missing-fields
              [:hosted-core-compiled-compiler-proof-file-artifact]}))
        hosted-compiler-artifact
        (p15-s23-context-artifact
         :hosted-core-compiled-compiler-proof
         p15-s23-accepted-app-source-path
         (fn [] (hosted-compiler-fn p15-s23-accepted-app-source-path)))]
    {:source-path source-path
     :proof-contract proof-contract
     :inventory-artifact inventory-artifact
     :pipeline-artifact pipeline-artifact
     :source-syntax-artifact source-syntax-artifact
     :core-artifact core-artifact
     :runtime-artifact runtime-artifact
     :accepted-artifact accepted-artifact
     :rejected-artifact rejected-artifact
     :rebuild-artifact rebuild-artifact
     :stage-comparison-artifact stage-comparison-artifact
     :conformance-artifact conformance-artifact
     :provenance-artifact provenance-artifact
     :tcb-artifact tcb-artifact
     :unsafe-artifact unsafe-artifact
     :hosted-compiler-artifact hosted-compiler-artifact}))
