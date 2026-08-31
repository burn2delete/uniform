(ns gravity.c17-plugin.manifest
  "C17 plugin manifest and authority-grant records.")

(defn plugin-manifest [sha256-hex]
  (let [manifest-base
        {:artifact :gravity/compiler-plugin
         :plugin 'gravity.plugins.stage0/loop-fuser
         :package {:name 'gravity/stage0-loop-fuser
                   :version "0.1.0"
                   :signature "sha256:c17-stage0-loop-fuser"}
         :api-version "1"
         :compiler-compatibility {:min "0.1.0" :max-exclusive "0.2.0"}
         :trust :sandboxed
         :profile :meta
         :build-effects #{}
         :capabilities #{:compiler/ir-transform :compiler/diagnostics}
         :capability-scopes {:compiler/ir-transform
                             #{:read-mir :write-mir :register-pass
                               :emit-artifacts}
                             :compiler/diagnostics #{:emit-diagnostics}}
         :passes [:fuse-adjacent-loops :emit-plugin-diagnostics]
         :domains [:stage0-loop-domain]
         :facets [:stage0-loop-fusion]
         :emits #{:optimization-decision-log :verifier-report
                  :diagnostic-stream}
         :conformance [:compiler-c17-plugin-fixtures]
         :status :accepted}]
    (assoc manifest-base
           :manifest-hash
           (str "sha256:" (sha256-hex (pr-str manifest-base))))))

(defn trust-grants [sha256-hex manifest]
  (let [sandbox-grant
        {:artifact :gravity/plugin-sandbox-grant
         :plugin (:plugin manifest)
         :package (get-in manifest [:package :name])
         :trust :sandboxed
         :status :sandboxed
         :capabilities (:capabilities manifest)
         :capability-scopes (:capability-scopes manifest)
         :build-effects (:build-effects manifest)
         :denied-authority [:filesystem/write :network/http
                            :process/spawn :environment/read
                            :compiler/hidden-state-mutation]}
        trusted-grant
        {:artifact :gravity/plugin-trust-grant
         :plugin 'gravity.plugins.stage0/proof-provider
         :package 'gravity/stage0-proof-provider
         :trust :trusted-package
         :status :granted
         :signature-status :verified
         :capabilities #{:compiler/proof-provider}
         :capability-scopes {:compiler/proof-provider
                             #{:request-proof :provide-proof}}}
        grant-hash (str "sha256:"
                        (sha256-hex (pr-str [sandbox-grant trusted-grant])))]
    {:sandbox-grant sandbox-grant
     :trusted-grant trusted-grant
     :grant-hash grant-hash}))
