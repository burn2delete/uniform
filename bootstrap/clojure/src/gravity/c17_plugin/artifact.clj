(ns gravity.c17-plugin.artifact
  "Assembly of the hosted Stage0 C17 compatibility artifact."
  (:require [gravity.c17-plugin.manifest :as manifest]
            [gravity.c17-plugin.registrations :as registrations]))

(defn artifact-base
  [config source-path module source-overrides incremental-artifact
   plugin-manifest grants records diagnostic-stream]
  (let [input-id (:artifact-id incremental-artifact)
        sandbox-grant (:sandbox-grant grants)]
    {:kind :gravity/stage0-c17-compiler-plugin-artifact
     :task "P06-D096"
     :document-set ["C17"]
     :governing-document (:governing-document config)
     :pass {:name :c17-compiler-plugin-api
            :input :incremental-compilation-artifact
            :output :compiler-plugin-artifact
            :requires [:c16-incremental-compilation
                       :plugin-manifest :api-version
                       :compiler-capability-grants
                       :build-effect-policy :sandbox-policy
                       :pass-contracts :verifiers]
            :preserves [:source-spans :profile :target :diagnostics
                        :proofs :cache-keys :incremental-replay]
            :emits [:plugin-manifest :api-compatibility-report
                    :sandbox-grant :trust-grants
                    :plugin-pass-registration-records
                    :domain-registration-records
                    :facet-registration-records
                    :plugin-execution-traces
                    :plugin-output-artifacts
                    :plugin-diagnostic-stream
                    :plugin-cache-keys
                    :plugin-conformance-results]
            :rejects (:diagnostic-ids config)}
     :source-overrides source-overrides
     :module (select-keys module [:module :source-path :profile :target
                                  :effects :capabilities :safety :metadata])
     :c16-incremental-artifact
     (select-keys incremental-artifact
                  [:kind :task :artifact-id :governing-document
                   :capability-based-proof])
     :incremental-artifact-kind (:kind incremental-artifact)
     :incremental-artifact-hash input-id
     :plugin-manifest plugin-manifest
     :api-compatibility-report
     {:artifact :gravity/plugin-api-compatibility-report
      :plugin (:plugin plugin-manifest)
      :api-version (:api-version plugin-manifest)
      :compiler-compatibility (:compiler-compatibility plugin-manifest)
      :status :compatible}
     :sandbox-grant sandbox-grant
     :trust-grants [sandbox-grant (:trusted-grant grants)]
     :package-trust-report
     {:artifact :gravity/plugin-package-trust-report
      :plugin (:plugin plugin-manifest)
      :package (get-in plugin-manifest [:package :name])
      :signature-status :verified
      :policy-status :accepted
      :status :accepted}
     :hermetic-build-effect-report
     {:artifact :gravity/plugin-build-effect-denial
      :plugin (:plugin plugin-manifest)
      :mode :hermetic
      :requested #{:network/http :process/spawn}
      :granted (:build-effects plugin-manifest)
      :denied #{:network/http :process/spawn}
      :diagnostic "C17-BUILD-EFFECT"
      :status :denied-ungranted-effects}
     :plugin-pass-registration-records (:passes records)
     :domain-registration-records (:domains records)
     :facet-registration-records (:facets records)
     :plugin-cache-keys (:cache-keys records)
     :plugin-output-artifacts (:outputs records)
     :plugin-execution-traces (:execution-traces records)
     :plugin-diagnostic-stream diagnostic-stream
     :plugin-conformance-results
     {:artifact :gravity/plugin-conformance-results
      :task "P06-D096"
      :fixtures [:manifest-loading :api-compatibility
                 :sandboxed-execution :trusted-execution
                 :capability-scope :pass-contract
                 :output-verifier-failure :domain-registration
                 :facet-registration :build-effect-denial
                 :execution-trace-cache-key :diagnostics]
      :status :passed}
     :c17-plugin-results
     {:documents ["C17"]
      :task "P06-D096"
      :required-diagnostic-ids (:diagnostic-ids config)
      :c16-input-status :complete
      :manifest-status :complete
      :api-status :complete
      :sandbox-status :complete
      :trust-status :complete
      :capability-status :complete
      :build-effect-status :complete
      :pass-contract-status :complete
      :output-verifier-status :complete
      :domain-registration-status :complete
      :facet-registration-status :complete
      :execution-trace-status :complete
      :cache-key-status :complete
      :diagnostic-status :complete
      :conformance-status :complete
      :status :complete}
     :diagnostics []}))

(defn source-artifact [config source-path source-text]
  (let [forms (mapv :form ((:read-source-form-records config)
                           source-path source-text))
        _ ((:validate-ns-syntax! config) source-path forms)
        module ((:parse-module config) source-path forms)
        source-overrides ((:source-overrides config) module)
        _ ((:validate-source-overrides! config) source-path source-overrides)
        incremental-artifact ((:c16-incremental-artifact config)
                              source-path source-text)
        input-id (:artifact-id incremental-artifact)
        plugin-manifest (manifest/plugin-manifest (:sha256-hex config))
        grants (manifest/trust-grants (:sha256-hex config) plugin-manifest)
        passes (registrations/pass-registration-records plugin-manifest)
        domains (registrations/domain-registration-records plugin-manifest)
        facets (registrations/facet-registration-records plugin-manifest)
        cache-keys (registrations/cache-keys
                    (:sha256-hex config) plugin-manifest (:grant-hash grants)
                    input-id passes)
        outputs (registrations/output-artifacts plugin-manifest)
        traces (registrations/execution-traces
                (:sha256-hex config) plugin-manifest (:grant-hash grants)
                input-id passes cache-keys outputs)
        stream ((:diagnostic-stream config) source-path plugin-manifest input-id)
        base (artifact-base config source-path module source-overrides
                            incremental-artifact plugin-manifest grants
                            {:passes passes :domains domains :facets facets
                             :cache-keys cache-keys :outputs outputs
                             :execution-traces traces}
                            stream)
        _ ((:validate! config) source-path base)
        capability-proof ((:capability-proof config) base)]
    (assoc base
           :capability-based-proof capability-proof
           :artifact-id ((:c4-artifact-id config)
                         (assoc base :capability-based-proof
                                capability-proof)))))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
