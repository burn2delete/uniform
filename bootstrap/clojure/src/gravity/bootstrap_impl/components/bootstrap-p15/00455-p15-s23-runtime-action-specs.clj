

(def p15-s23-runtime-action-specs
  [{:family :filesystem
    :action-id "compiler/source-read"
    :effect :filesystem/read
    :capability :artifact/source-read
    :provider :bootstrap-artifact-store
    :decision :grant
    :handle-id "handle/source-read/compiler-source"
    :redaction-status :path-only}
   {:family :package-mutation
    :action-id "compiler/artifact-emit"
    :effect :package/write
    :capability :artifact/write
    :provider :bootstrap-artifact-store
    :decision :grant
    :handle-id "handle/artifact-write/p15-s23"
    :redaction-status :artifact-id-only}
   {:family :observability
    :action-id "compiler/local-diagnostic-bundle"
    :effect :runtime/observability
    :capability :observability/write
    :provider :local-diagnostic-bundle
    :decision :grant
    :handle-id "handle/observability/local"
    :redaction-status :public-safe}
   {:family :tool
    :action-id "compiler/test-runner-tool"
    :effect :tool/invoke
    :capability :tool/bootstrap-test-runner
    :provider :clojure-stage0-test-runner
    :decision :delegate
    :handle-id "handle/tool/bootstrap-test-runner"
    :delegate-scope [:clojure-test :document-validator]
    :redaction-status :public-safe}
   {:family :deployment
    :action-id "compiler/revoke-artifact-emitter"
    :effect :deployment/revoke
    :capability :artifact/write
    :provider :bootstrap-artifact-store
    :decision :revoke
    :handle-id "handle/artifact-write/p15-s23"
    :redaction-status :artifact-id-only}
   {:family :network
    :action-id "compiler/ambient-network"
    :effect :network/http
    :capability :http/client
    :provider :ambient-network
    :decision :deny
    :redaction-status :not-sensitive}
   {:family :database
    :action-id "compiler/database-write"
    :effect :database/write
    :capability :db/write
    :provider :ambient-database
    :decision :deny
    :redaction-status :not-sensitive}
   {:family :environment
    :action-id "compiler/env-read"
    :effect :environment/read
    :capability :env/read
    :provider :ambient-environment
    :decision :deny
    :redaction-status :key-only}
   {:family :process
    :action-id "compiler/process-launch"
    :effect :process/launch
    :capability :process/launch
    :provider :ambient-process
    :decision :deny
    :redaction-status :command-digest}
   {:family :shell
    :action-id "compiler/shell"
    :effect :shell/exec
    :capability :shell/exec
    :provider :ambient-shell
    :decision :deny
    :redaction-status :command-digest}
   {:family :secrets
    :action-id "compiler/secret-read"
    :effect :secrets/read
    :capability :secret/read
    :provider :ambient-secret-store
    :decision :deny
    :redaction-status :secret-name-only}
   {:family :ffi
    :action-id "compiler/ffi-call"
    :effect :ffi/call
    :capability :ffi/call
    :provider :ambient-ffi
    :decision :deny
    :redaction-status :public-safe}
   {:family :raw-memory
    :action-id "compiler/raw-memory-write"
    :effect :raw-memory/write
    :capability :raw-memory/write
    :provider :ambient-memory
    :decision :deny
    :redaction-status :not-sensitive}
   {:family :model
    :action-id "compiler/model-call"
    :effect :model/call
    :capability :model/call
    :provider :ambient-model-provider
    :decision :deny
    :redaction-status :prompt-digest}
   {:family :memory
    :action-id "compiler/agent-memory-write"
    :effect :memory/write
    :capability :memory/write
    :provider :ambient-agent-memory
    :decision :deny
    :redaction-status :content-digest}
   {:family :ai/human-review
    :action-id "compiler/human-review-token"
    :effect :ai/human-review
    :capability :human-review/request
    :provider :ambient-human-review
    :decision :deny
    :redaction-status :public-safe}])

(defn p15-s23-runtime-decision-row
  [source-path artifact-id index spec]
  (merge
   {:artifact :gravity/runtime-capability-decision
    :action-id (:action-id spec)
    :principal :gravity.compiler/p15-s23
    :namespace :gravity.bootstrap.p15-s23.compiler
    :package :gravity/bootstrap
    :artifact-id artifact-id
    :source-span (source-span source-path index)
    :family (:family spec)
    :effect (:effect spec)
    :capability (:capability spec)
    :provider (:provider spec)
    :policy-source :p15-s23-bootstrap-policy
    :policy-inputs [:compiled-effects :package-policy :deployment-policy
                    :artifact-manifest]
    :decision (:decision spec)
    :redaction-status (:redaction-status spec)
    :audit-status :recorded
    :runtime-checks-do-not-grant-authority? true}
   (select-keys spec [:handle-id :delegate-scope])))

(defn p15-s23-runtime-capability-table
  [source-path artifact-id]
  (let [rows (mapv #(p15-s23-runtime-decision-row source-path
                                                  artifact-id
                                                  %1 %2)
                   (range)
                   p15-s23-runtime-action-specs)]
    {:artifact :gravity/p15-s23-runtime-capability-table
     :rows rows
     :decisions (set (map :decision rows))
     :families-covered (set (map :family rows))
     :deny-by-default? true
     :runtime-checks-do-not-grant-authority? true
     :ambient-authority-rejected?
     (boolean (some #(and (= :deny (:decision %))
                          (str/includes? (:action-id %) "ambient"))
                    rows))
     :status :complete}))

(defn p15-s23-runtime-capability-manifest
  [capability-table]
  {:artifact :gravity/runtime-capabilities
   :inputs #{:compiled-effects :package-policy :deployment-policy
             :principal :artifact-manifest}
   :handles p15-s23-runtime-action-families
   :decisions #{:grant :deny :delegate :revoke}
   :records #{:capability-table :decision-log :denial-diagnostic
              :delegated-handle-record :revocation-record
              :redaction-secret-handling-record}
   :rejects #{:ambient-authority :effect-outside-grant
              :tool-contract-violation :profile-as-deployment-authority
              :unscoped-handle :secret-leak}
   :table-artifact (:artifact capability-table)
   :deny-by-default? true
   :status :complete})