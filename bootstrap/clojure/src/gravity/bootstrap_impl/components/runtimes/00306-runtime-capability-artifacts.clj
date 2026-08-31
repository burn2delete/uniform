

(defn runtime-capability-artifacts
  [source-path input-id]
  {:runtime-capability-manifest
   {:artifact :gravity/runtime-capabilities
    :input-artifact input-id
    :inputs #{:compiled-effects :package-policy :deployment-policy
              :principal :artifact-manifest}
    :handles #{:filesystem :network :database :secrets :process :shell
               :ffi :raw-memory :model :tool :human-review :observability}
    :decisions #{:grant :deny :delegate :revoke}
    :records #{:capability-table :decision-log :denial-diagnostic}
    :rejects #{:ambient-authority :effect-outside-grant
               :tool-contract-violation}
    :deny-by-default? true
    :status :complete}
   :capability-table
   {:artifact :gravity/runtime-capability-table
    :input-artifact input-id
    :rows [{:effect :ai/model-call :capability :model/call
            :provider :model-provider/stage0 :decision :grant}
           {:effect :ffi/call :capability :ffi/c
            :provider :ffi-provider/stage0 :decision :grant}
           {:effect :secrets/read :capability :secret/read
            :provider :secret-store :decision :deny}
           {:effect :runtime/observability :capability :observability/write
            :provider :local-observability :decision :grant}]
    :status :complete}
   :principal-identity-record
   {:artifact :gravity/principal-identity-record
    :input-artifact input-id
    :principals [{:principal :agent/support
                  :package :gravity/runtime-stage0
                  :artifact-id input-id
                  :source-span (source-span source-path 0)
                  :valid? true}]
    :invalid-principals []
    :status :complete}
   :runtime-decision-log
   {:artifact :gravity/runtime-decision-log
    :input-artifact input-id
    :decisions [{:action-id "model/call"
                 :principal :agent/support
                 :effect :ai/model-call
                 :capability :model/call
                 :provider :model-provider/stage0
                 :decision :grant
                 :redaction :prompt-hash-only
                 :audit :recorded}
                {:action-id "secret/read"
                 :principal :agent/support
                 :effect :secrets/read
                 :capability :secret/read
                 :provider :secret-store
                 :decision :deny
                 :redaction :secret-name-only
                 :audit :recorded}]
    :missing-required-audit []
    :status :complete}
   :delegated-handle-record
   {:artifact :gravity/delegated-handle-record
    :input-artifact input-id
    :handles [{:handle-id "model-handle/stage0"
               :grantor :deployment-policy
               :recipient :agent/support
               :allowed-effects #{:ai/model-call}
               :lifetime :session
               :revocable? true
               :scope :single-agent}]
    :unscoped-handles []
    :status :complete}
   :revocation-record
   {:artifact :gravity/revocation-record
    :input-artifact input-id
    :records [{:handle-id "model-handle/stage0"
               :revocation-supported? true
               :state :active
               :use-after-revocation? false}]
    :status :complete}
   :denial-diagnostic-record
   {:artifact :gravity/denial-diagnostic-record
    :input-artifact input-id
    :denials [{:action-id "secret/read"
               :source-span (source-span source-path 2)
               :principal :agent/support
               :effect :secrets/read
               :capability :secret/read
               :provider :secret-store
               :decision :deny
               :remediation :request-secret-grant}]
    :status :complete}
   :redaction-secret-handling-record
   {:artifact :gravity/redaction-secret-handling-record
    :input-artifact input-id
    :policies [{:category :secret
                :redaction :secret-name-only
                :logs-secret-value? false}
               {:category :prompt
                :redaction :digest-only
                :logs-secret-value? false}]
    :secret-leaks []
    :status :complete}
   :capability-conformance-evidence
   {:artifact :gravity/runtime-capability-conformance-evidence
    :input-artifact input-id
    :deny-by-default-demonstrated? true
    :grant-deny-delegate-revoke-covered? true
    :caller-tool-plugin-contracts-covered? true
    :secret-redaction-covered? true
    :deployment-policy-narrowing-covered? true
    :status :complete}})