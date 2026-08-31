(ns gravity.c17-plugin.registrations
  "C17 pass, extension, cache, output, and execution records.")

(defn pass-registration-records [manifest]
  (let [pass-contracts
        [{:input :gravity/mir
          :output :gravity/mir
          :requires #{:dominators :effect-graph}
          :preserves #{:types :source-origins :safety-outcomes}
          :invalidates #{:dominators :loop-analysis}
          :regenerates #{:effect-ordering}
          :proof-obligations #{:effect-order-preserved}
          :emits #{:optimization-decision-log :verifier-report}}
         {:input :gravity/diagnostic-stream
          :output :gravity/diagnostic-stream
          :requires #{:diagnostic-schema :source-spans}
          :preserves #{:diagnostic-ids :source-origins}
          :invalidates #{}
          :regenerates #{}
          :proof-obligations #{:diagnostic-order-stable}
          :emits #{:diagnostic-stream}}]]
    (mapv (fn [pass contract]
            {:artifact :gravity/plugin-pass-registration
             :plugin (:plugin manifest)
             :pass pass
             :contract contract
             :api-version (:api-version manifest)
             :capabilities #{:compiler/ir-transform}
             :status :registered})
          (:passes manifest)
          pass-contracts)))

(defn domain-registration-records [manifest]
  [{:artifact :gravity/plugin-domain-ir-registration
    :plugin (:plugin manifest)
    :domain :stage0-loop-domain
    :schema :gravity.loop-domain/schema-v1
    :verifier :gravity.loop-domain/verify
    :supported-profiles #{:hosted :native}
    :effects #{}
    :capabilities #{:compiler/ir-transform}
    :lowering-paths [:mir :target-lowering]
    :diagnostics ["C17-DOMAIN"]
    :conformance-fixtures [:stage0-loop-domain-fixture]
    :status :registered}])

(defn facet-registration-records [manifest]
  [{:artifact :gravity/plugin-facet-registration
    :plugin (:plugin manifest)
    :facet :stage0-loop-fusion
    :schema :gravity.loop-fusion/schema-v1
    :verifier :gravity.loop-fusion/verify
    :supported-profiles #{:hosted :native}
    :effects #{}
    :capabilities #{:compiler/ir-transform}
    :lowering-paths [:mir :target-lowering]
    :diagnostics ["C17-FACET"]
    :conformance-fixtures [:stage0-loop-fusion-fixture]
    :status :registered}])

(defn cache-keys [sha256-hex manifest grant-hash input-id registrations]
  (mapv (fn [registration]
          {:artifact :gravity/plugin-cache-key
           :plugin-package (get-in manifest [:package :name])
           :plugin-version (get-in manifest [:package :version])
           :manifest (:manifest-hash manifest)
           :grants grant-hash
           :dependencies [input-id
                          (str "sha256:"
                               (sha256-hex
                                (pr-str (:contract registration))))]
           :replay-record "sha256:c17-plugin-replay"
           :pass (:pass registration)})
        registrations))

(defn output-artifacts [manifest]
  [{:artifact :gravity/plugin-output-artifact
    :kind :optimization-decision-log
    :plugin (:plugin manifest)
    :pass :fuse-adjacent-loops
    :artifact-id "sha256:c17-loop-fuser-decisions"
    :verifier-result :passed
    :status :verified}
   {:artifact :gravity/plugin-output-artifact
    :kind :diagnostic-stream
    :plugin (:plugin manifest)
    :pass :emit-plugin-diagnostics
    :artifact-id "sha256:c17-plugin-diagnostics"
    :verifier-result :passed
    :status :verified}])

(defn execution-traces
  [sha256-hex manifest grant-hash input-id registrations cache-keys outputs]
  (mapv (fn [registration cache-key output]
          {:artifact :gravity/plugin-execution
           :plugin (:plugin manifest)
           :pass (:pass registration)
           :input input-id
           :output (:artifact-id output)
           :grants grant-hash
           :build-effects []
           :decisions [(:artifact-id output)]
           :diagnostics []
           :verifier-result :passed
           :sandbox-result :passed
           :cache-key (str "sha256:" (sha256-hex (pr-str cache-key)))})
        registrations cache-keys outputs))
