(defn- semantic-mid-whole-language-compiler-artifact-result
  [{:keys [source-path proof-contract stage-support link-table
           accepted-record rejected-record boundary-record lineage-record
           manifest auditor-query rejected-records]}]
  {:stage-support-matrix stage-support
   :compiler-evidence-link-table link-table
   :accepted-application-compile-record accepted-record
   :rejected-application-diagnostic-record rejected-record
   :residual-trusted-boundary-record boundary-record
   :compiler-artifact-lineage-record lineage-record
   :compiler-artifact-manifest manifest
   :auditor-query-record auditor-query
   :full-language-compiler-self-hosted?
   (get-in proof-contract
           [:self-hosting-claims :full-language-compiler-self-hosted?])
   :clojure-seed-retired?
   (get-in proof-contract [:self-hosting-claims :clojure-seed-retired?])
   :accepted-p15-s23-whole-language-compiler-fixtures
   [{:fixture source-path
     :status :accepted
     :compiler-artifact-id (:compiler-artifact-id manifest)}
    {:fixture p15-s23-accepted-app-source-path
     :status :accepted
     :stdout (:stdout accepted-record)
     :compiled-plan-id (:compiled-plan-id accepted-record)}]
   :rejected-p15-s23-whole-language-compiler-fixtures rejected-records
   :p15-s23-whole-language-compiler-results
   {:accepted-fixtures 2
    :rejected-fixtures (count rejected-records)
    :diagnostic-count (count p15-s23-whole-language-compiler-diagnostic-ids)
    :compiler-artifact-id (:compiler-artifact-id manifest)
    :accepted-app-output (:stdout accepted-record)
    :rejected-diagnostics (:diagnostics rejected-record)
    :residual-clojure-boundary?
    (:clojure-stage0-still-required? boundary-record)
    :status :in-progress}
   :diagnostics []})

(defn- semantic-mid-whole-language-compiler-build
  [source-path]
  (let [context
        (-> (semantic-mid-whole-language-compiler-inputs source-path)
            (semantic-mid-whole-language-compiler-records))
        artifact-base
        (merge (semantic-mid-whole-language-compiler-artifact-inputs context)
               (let [result
                     (semantic-mid-whole-language-compiler-artifact-result
                      context)]
                 (assoc result
                        :p15-s23-whole-language-compiler-diagnostic-stream
                        (p15-s23-whole-language-compiler-diagnostic-stream
                         source-path (:proof-id context)))))
        proof (p15-s23-whole-language-compiler-artifact-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id
           (c4-artifact-id
            (assoc artifact-base :capability-based-proof proof)))))
