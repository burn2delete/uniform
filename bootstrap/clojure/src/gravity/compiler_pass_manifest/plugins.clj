(ns gravity.compiler-pass-manifest.plugins
  "Compiler plugin manifest, pass contract, and execution trace defaults."
  (:require [gravity.compiler-pass-manifest.contracts :as contracts]))

(def compiler-pass-default-plugin-manifest
  {:artifact :gravity/compiler-plugin
   :plugin 'gravity.compiler.stage0/pass-audit
   :package {:name 'gravity/compiler-pass-audit :version "0.1.0"}
   :api-version "1"
   :compiler-compatibility {:min "0.1.0" :max-exclusive "0.2.0"}
   :trust :sandboxed
   :profile :meta
   :build-effects #{}
   :capabilities #{:compiler/ir-transform}
   :capability-scopes {:compiler/ir-transform
                       #{:read-mir :write-mir :emit-artifacts
                         :emit-diagnostics :register-pass}}
   :requested-scopes #{:read-mir :write-mir :emit-diagnostics}
   :passes [:plugin/stage0-audit]
   :domains []
   :facets []
   :emits #{:plugin-execution-trace}
   :conformance [:compiler-pass-contract-fixtures]})

(def compiler-pass-default-plugin-pass-contracts
  [(assoc (contracts/compiler-pass-contract
           :plugin/stage0-audit :C17 :verified-mir :verified-mir
           [:mir-verifier-report :plugin-grants]
           [:source-spans :origin-chain :profile :target :types :effects
            :ownership :capabilities :safety-outcomes :proofs :diagnostics]
           []
           [:plugin-execution-trace]
           [:plugin-execution-trace :verifier-report]
           ["C17-PASS-CONTRACT" "C17-OUTPUT"] :medium
           [:contract-verifier :fixture-suite])
          :capabilities #{:compiler/ir-transform})])

(def compiler-pass-default-plugin-execution-traces
  [{:artifact :gravity/plugin-execution
    :plugin 'gravity.compiler.stage0/pass-audit
    :pass :plugin/stage0-audit
    :input "sha256:stage0-verified-mir"
    :output "sha256:stage0-verified-mir-audited"
    :grants "sha256:stage0-plugin-grants"
    :build-effects []
    :decisions [:decision/stage0-plugin-audit]
    :diagnostics []
    :verifier-result :passed}])
