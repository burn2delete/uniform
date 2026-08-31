

(defn p15-s23-runtime-proof-diagnostics
  [source-path candidate]
  (let [proof-contract (:proof-contract candidate)
        runtime-manifest (:runtime-manifest candidate)
        service-table (:runtime-service-table candidate)
        capability-manifest (:runtime-capability-manifest candidate)
        capability-table (:capability-enforcement-table candidate)
        audit-records (:audit-records candidate)
        conformance (:capability-conformance-evidence candidate)
        core-artifact (:core-diagnostic-artifact candidate)
        pipeline (:compiler-pipeline-manifest-artifact candidate)
        claims (:self-hosting-claims proof-contract)
        preserves (set (:preserves proof-contract))
        missing-preserves
        (set/difference p15-s23-runtime-required-preserves preserves)]
    (vec
     (concat
      (when-not
       (= :gravity/runtime-manifest-and-capability-enforcement-report
          (:artifact proof-contract))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R001" proof-contract
          {:missing-fields [:artifact]})])
      (when-not
       (and (= :gravity/p15-s23-runtime-manifest
               (:artifact runtime-manifest))
            (= :complete (:status runtime-manifest))
            (= :managed (:family runtime-manifest))
            (= :meta (:profile runtime-manifest))
            (= :jvm (get-in runtime-manifest [:target :backend]))
            (true? (:capability-checks runtime-manifest))
            (contains? (set (:consumed-by runtime-manifest))
                       :self-hosting-gate)
            (empty? missing-preserves))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R002" runtime-manifest
          {:missing-preserves (vec (sort missing-preserves))})])
      (when-not
       (and (= :gravity/p15-s23-runtime-service-table
               (:artifact service-table))
            (= :complete (:status service-table))
            (= #{:linked :generated :delegated :external :forbidden}
               (:classification-kinds service-table))
            (seq (:linked service-table))
            (seq (:generated service-table))
            (seq (:delegated service-table))
            (contains? (:forbidden service-table) :ambient-network)
            (empty? (:hidden-services service-table)))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R003" service-table
          {:missing-fields [:runtime-service-classification]})])
      (when-not
       (and (= :gravity/runtime-capabilities
               (:artifact capability-manifest))
            (= :complete (:status capability-manifest))
            (true? (:deny-by-default? capability-manifest))
            (= #{:grant :deny :delegate :revoke}
               (:decisions capability-table))
            (set/subset? p15-s23-runtime-action-families
                         (:families-covered capability-table))
            (true? (:runtime-checks-do-not-grant-authority?
                    capability-table))
            (true? (:ambient-authority-rejected? capability-table)))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R004" capability-table
          {:required-families p15-s23-runtime-action-families
           :observed-families (:families-covered capability-table)})])
      (when-not
       (and (= :complete
               (get-in audit-records
                       [:principal-identity-record :status]))
            (empty? (get-in audit-records
                            [:principal-identity-record
                             :invalid-principals]))
            (= :complete
               (get-in audit-records [:runtime-decision-log :status]))
            (empty? (get-in audit-records
                            [:runtime-decision-log
                             :missing-required-audit]))
            (= :complete
               (get-in audit-records [:delegated-handle-record :status]))
            (empty? (get-in audit-records
                            [:delegated-handle-record
                             :unscoped-handles]))
            (= :complete
               (get-in audit-records [:revocation-record :status]))
            (every? #(false? (:use-after-revocation? %))
                    (get-in audit-records
                            [:revocation-record :records]))
            (= :complete
               (get-in audit-records
                       [:redaction-secret-handling-record :status]))
            (empty? (get-in audit-records
                            [:redaction-secret-handling-record
                             :secret-leaks]))
            (= :complete (:status conformance)))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R005" audit-records
          {:missing-fields [:principal-audit-delegation-revocation-redaction]})])
      (when-not
       (and (= :gravity/p15-s23-core-lowering-diagnostic-preservation-artifact
               (:kind core-artifact))
            (= :gravity/p15-s23-compiler-pipeline-manifest-artifact
               (:kind pipeline))
            (= (:artifact-id core-artifact)
               (get-in runtime-manifest [:target :artifact])))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R006"
          {:core-artifact core-artifact
           :pipeline pipeline
           :runtime-target-artifact
           (get-in runtime-manifest [:target :artifact])}
          {:missing-fields [:artifact-links]})])
      (when (or (true? (:full-language-compiler-self-hosted? claims))
                (true? (:clojure-seed-retired? claims)))
        [(p15-s23-runtime-diagnostic-record
          source-path "P15S23R007" claims
          {:full-language-compiler-self-hosted?
           (:full-language-compiler-self-hosted? claims)
           :clojure-seed-retired?
           (:clojure-seed-retired? claims)})])))))