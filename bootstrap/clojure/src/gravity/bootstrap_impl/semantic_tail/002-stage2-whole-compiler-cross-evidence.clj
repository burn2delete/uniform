(defn- semantic-tail-stage2-whole-compiler-cross-evidence
  [source-path
   {:keys [proof-contract inventory-artifact pipeline-artifact
           whole-compiler-artifact driver-artifact
           source-front-end-artifact front-end-executor-artifact
           plan-emitter-artifact runtime-executor-artifact
           runtime-kernel-artifact accepted-artifact rejected-artifact
           stage-comparison-artifact conformance-artifact
           provenance-artifact tcb-artifact unsafe-artifact]}]
  (let [source-record
        (p15-s23-stage2-whole-language-compiler-source-record
         source-path proof-contract inventory-artifact)
        stage-record
        (p15-s23-stage2-whole-language-compiler-stage-record
         source-path proof-contract source-record)
        evidence-link-record
        (p15-s23-stage2-whole-language-compiler-evidence-link-record
         whole-compiler-artifact pipeline-artifact driver-artifact
         source-front-end-artifact front-end-executor-artifact
         plan-emitter-artifact runtime-executor-artifact
         runtime-kernel-artifact accepted-artifact rejected-artifact
         stage-comparison-artifact conformance-artifact
         provenance-artifact tcb-artifact unsafe-artifact)
        accepted-record
        (p15-s23-stage2-whole-language-compiler-accepted-record
         source-record driver-artifact whole-compiler-artifact)
        rejected-record
        (p15-s23-stage2-whole-language-compiler-rejected-record
         driver-artifact whole-compiler-artifact)
        boundary-record
        (p15-s23-stage2-whole-language-compiler-boundary-record
         proof-contract driver-artifact whole-compiler-artifact
         tcb-artifact)
        lineage-record
        (p15-s23-stage2-whole-language-compiler-lineage-record
         source-path proof-contract inventory-artifact driver-artifact
         whole-compiler-artifact provenance-artifact)
        candidate {:proof-contract proof-contract
                   :source-record source-record
                   :stage-record stage-record
                   :accepted-record accepted-record
                   :rejected-record rejected-record
                   :evidence-link-record evidence-link-record
                   :boundary-record boundary-record
                   :lineage-record lineage-record}
        diagnostics
        (p15-s23-stage2-whole-language-compiler-proof-diagnostics
         source-path candidate)
        _ (when (seq diagnostics)
            (p15-s23-stage2-whole-language-compiler-fail!
             (:diagnostic (first diagnostics)) source-path candidate
             {:diagnostics diagnostics}))
        proof-id
        (str "sha256:"
             (sha256-hex
              (pr-str {:source-path source-path
                       :proof-contract proof-contract
                       :source-record source-record
                       :stage-record stage-record
                       :accepted-record accepted-record
                       :rejected-record rejected-record
                       :evidence-link-record evidence-link-record
                       :boundary-record boundary-record
                       :lineage-record lineage-record})))
        rejected-records
        (p15-s23-stage2-whole-language-compiler-rejected-records
         source-path candidate)]
    {:source-record source-record
     :stage-record stage-record
     :accepted-record accepted-record
     :rejected-record rejected-record
     :evidence-link-record evidence-link-record
     :boundary-record boundary-record
     :lineage-record lineage-record
     :proof-id proof-id
     :rejected-records rejected-records}))
