

(def p15-s23-runtime-rejected-candidates
  [{:fixture :internal-p15-s23-runtime-missing-report
    :candidate {}
    :expected-diagnostic "P15S23R001"}
   {:fixture :internal-p15-s23-runtime-manifest-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/runtime-manifest-and-capability-enforcement-report
                 :preserves p15-s23-runtime-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :runtime-manifest
                {:artifact :gravity/p15-s23-runtime-manifest
                 :family nil
                 :target {:backend :jvm}
                 :status :incomplete}}
    :expected-diagnostic "P15S23R002"}
   {:fixture :internal-p15-s23-runtime-service-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/runtime-manifest-and-capability-enforcement-report
                 :preserves p15-s23-runtime-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :runtime-manifest
                {:artifact :gravity/p15-s23-runtime-manifest
                 :profile :meta
                 :target {:backend :jvm}
                 :family :managed
                 :capability-checks true
                 :consumed-by [:self-hosting-gate]
                 :status :complete}
                :runtime-service-table
                {:artifact :gravity/p15-s23-runtime-service-table
                 :classification-kinds #{:linked}
                 :linked #{}
                 :generated #{}
                 :delegated #{}
                 :forbidden #{}
                 :hidden-services [:ambient-network]
                 :status :incomplete}}
    :expected-diagnostic "P15S23R003"}
   {:fixture :internal-p15-s23-runtime-capability-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/runtime-manifest-and-capability-enforcement-report
                 :preserves p15-s23-runtime-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :runtime-manifest
                {:artifact :gravity/p15-s23-runtime-manifest
                 :profile :meta
                 :target {:backend :jvm}
                 :family :managed
                 :capability-checks true
                 :consumed-by [:self-hosting-gate]
                 :status :complete}
                :runtime-service-table
                {:artifact :gravity/p15-s23-runtime-service-table
                 :classification-kinds #{:linked :generated :delegated
                                         :external :forbidden}
                 :linked #{:diagnostic-renderer}
                 :generated #{:runtime-check-table}
                 :delegated #{:clojure-stage0-artifact-store}
                 :forbidden #{:ambient-network}
                 :hidden-services []
                 :status :complete}
                :runtime-capability-manifest
                {:artifact :gravity/runtime-capabilities
                 :deny-by-default? false
                 :status :incomplete}
                :capability-enforcement-table
                {:decisions #{:grant}
                 :families-covered #{:filesystem}
                 :runtime-checks-do-not-grant-authority? false
                 :ambient-authority-rejected? false}}
    :expected-diagnostic "P15S23R004"}
   {:fixture :internal-p15-s23-runtime-audit-redaction-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/runtime-manifest-and-capability-enforcement-report
                 :preserves p15-s23-runtime-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :runtime-manifest
                {:artifact :gravity/p15-s23-runtime-manifest
                 :profile :meta
                 :target {:backend :jvm}
                 :family :managed
                 :capability-checks true
                 :consumed-by [:self-hosting-gate]
                 :status :complete}
                :runtime-service-table
                {:artifact :gravity/p15-s23-runtime-service-table
                 :classification-kinds #{:linked :generated :delegated
                                         :external :forbidden}
                 :linked #{:diagnostic-renderer}
                 :generated #{:runtime-check-table}
                 :delegated #{:clojure-stage0-artifact-store}
                 :forbidden #{:ambient-network}
                 :hidden-services []
                 :status :complete}
                :runtime-capability-manifest
                {:artifact :gravity/runtime-capabilities
                 :deny-by-default? true
                 :status :complete}
                :capability-enforcement-table
                {:decisions #{:grant :deny :delegate :revoke}
                 :families-covered p15-s23-runtime-action-families
                 :runtime-checks-do-not-grant-authority? true
                 :ambient-authority-rejected? true}
                :audit-records
                {:principal-identity-record
                 {:status :complete
                  :invalid-principals []}
                 :runtime-decision-log
                 {:status :complete
                  :missing-required-audit [:compiler/secret-read]}
                 :delegated-handle-record
                 {:status :complete
                  :unscoped-handles []}
                 :revocation-record
                 {:status :complete
                  :records []}
                 :redaction-secret-handling-record
                 {:status :complete
                  :secret-leaks [:secret-value]}}
                :capability-conformance-evidence {:status :failed}}
    :expected-diagnostic "P15S23R005"}
   {:fixture :internal-p15-s23-runtime-link-gap
    :candidate {:proof-contract
                {:artifact
                 :gravity/runtime-manifest-and-capability-enforcement-report
                 :preserves p15-s23-runtime-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? false
                  :clojure-seed-retired? false}}
                :runtime-manifest
                {:artifact :gravity/p15-s23-runtime-manifest
                 :profile :meta
                 :target {:backend :jvm
                          :artifact "other"}
                 :family :managed
                 :capability-checks true
                 :consumed-by [:self-hosting-gate]
                 :status :complete}
                :runtime-service-table
                {:artifact :gravity/p15-s23-runtime-service-table
                 :classification-kinds #{:linked :generated :delegated
                                         :external :forbidden}
                 :linked #{:diagnostic-renderer}
                 :generated #{:runtime-check-table}
                 :delegated #{:clojure-stage0-artifact-store}
                 :forbidden #{:ambient-network}
                 :hidden-services []
                 :status :complete}
                :runtime-capability-manifest
                {:artifact :gravity/runtime-capabilities
                 :deny-by-default? true
                 :status :complete}
                :capability-enforcement-table
                {:decisions #{:grant :deny :delegate :revoke}
                 :families-covered p15-s23-runtime-action-families
                 :runtime-checks-do-not-grant-authority? true
                 :ambient-authority-rejected? true}
                :audit-records
                {:principal-identity-record
                 {:status :complete
                  :invalid-principals []}
                 :runtime-decision-log
                 {:status :complete
                  :missing-required-audit []}
                 :delegated-handle-record
                 {:status :complete
                  :unscoped-handles []}
                 :revocation-record
                 {:status :complete
                  :records []}
                 :redaction-secret-handling-record
                 {:status :complete
                  :secret-leaks []}}
                :capability-conformance-evidence {:status :complete}
                :core-diagnostic-artifact {:kind :wrong
                                           :artifact-id "core"}
                :compiler-pipeline-manifest-artifact
                {:kind
                 :gravity/p15-s23-compiler-pipeline-manifest-artifact}}
    :expected-diagnostic "P15S23R006"}
   {:fixture :internal-p15-s23-runtime-overclaim
    :candidate {:proof-contract
                {:artifact
                 :gravity/runtime-manifest-and-capability-enforcement-report
                 :preserves p15-s23-runtime-required-preserves
                 :self-hosting-claims
                 {:full-language-compiler-self-hosted? true
                  :clojure-seed-retired? true}}}
    :expected-diagnostic "P15S23R007"}])

(defn p15-s23-runtime-rejected-records
  [source-path]
  (mapv (fn [{:keys [fixture candidate expected-diagnostic]}]
          {:fixture fixture
           :status :rejected
           :expected-diagnostic expected-diagnostic
           :diagnostics
           (p15-s23-runtime-proof-diagnostics source-path candidate)})
        p15-s23-runtime-rejected-candidates))

(defn p15-s23-runtime-diagnostic-stream
  [source-path proof-id]
  {:artifact
   :gravity/p15-s23-runtime-manifest-capability-diagnostic-stream
   :stage :p15-s23-runtime-manifest-capability-enforcement-report
   :source-path source-path
   :proof-id proof-id
   :diagnostics
   (mapv (fn [id]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id))
            :diagnostic id
            :severity :error
            :stage
            :p15-s23-runtime-manifest-capability-enforcement-report
            :message (get p15-s23-runtime-diagnostic-messages id)})
         p15-s23-runtime-diagnostic-ids)
   :status :complete})