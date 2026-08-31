(ns gravity.p15-native-packet-binding.artifact
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(defn build
  [packet context authority runtime-contract provider
   runtime-rule-sha wire expected-stdout sha256-text
   runtime-contract-relative provider-relative]
  (let [source-path (:source-path context)
        plan (:plan packet)
        compiler-record (:stage2-compiler-artifact-record packet)]
    {:artifact :gravity/p15-native-runtime-packet-binding
     :schema-version 1
     :status :complete-for-internal-bounded-native-runtime-provider
     :source {:path source-path
              :content-hash (:source-content-hash context)
              :extension (if (str/ends-with? source-path ".qst") ".qst"
                             ".gravity")
              :content-hash-verified-by-provider? false}
     :plan {:plan-id (:plan-id plan)
            :entrypoint (:entrypoint plan)
            :kind (:kind plan)
            :instruction-summary (:instruction-summary plan)
            :effect-summary (:effect-summary plan)
            :content-hash
            (sha256-text
             (pr-str
              (bootstrap/c-backend-canonical-value
               (select-keys plan [:kind :entrypoint :functions
                                  :binding-table :instruction-summary
                                  :effect-summary]))))}
     :compiler {:artifact (:artifact compiler-record)
                :artifact-hash (:artifact-hash compiler-record)
                :source-content-hash (:source-content-hash compiler-record)
                :semantic-hash (:semantic-hash compiler-record)
                :plan-assembly-artifact-hash
                (:plan-assembly-artifact-hash compiler-record)}
     :emitter {:source-rule-hash
               (get-in packet [:stage2-plan-emitter-rule :source-rule-hash])}
     :driver {:driver-rule-hash
              (get-in packet [:stage2-compiler-driver-rule
                              :driver-rule-hash])
              :record-status
              (get-in packet [:stage2-compiler-driver-record :status])}
     :runtime {:runtime-rule-hash
               (get-in packet [:stage2-runtime-rule :runtime-rule-hash])
               :runtime-artifact-hash
               (get-in packet [:stage2-runtime-rule :runtime-artifact-hash])
               :execution-status
               (get-in packet [:stage2-runtime-execution-record :status])}
     :runtime-contract (assoc runtime-contract
                              :wire-rule-sha256 runtime-rule-sha)
     :provider (assoc provider
                      :implementation :host-authored-c
                      :provider-kind :host-c
                      :runtime-provider
                      :gravity.native/libsystem-stdio-v1)
     :target {:requested :c
              :provider-host-language :c
              :provider-target :arm64-macos
              :profile :native
              :eligibility (:target-eligibility packet)}
     :effects (select-keys authority
                           [:declared-effects :inferred-effects
                            :required-effects :required-inferred-effects])
     :capabilities (select-keys authority
                                [:declared-capabilities
                                 :required-capabilities])
     :provenance
     {:actual-paths (merge
                     {:source source-path
                      :native-runtime-contract runtime-contract-relative
                      :native-runtime-provider provider-relative}
                     (get-in packet [:provenance :actual-paths]))
      :selected-runtime-clojure-seed-boundary? false
      :selected-child-clojure-seed-boundary? false
      :adapter-clojure-seed-boundary? true
      :compiler-clojure-seed-boundary? true
      :verifier-clojure-seed-boundary? true
      :artifact-clojure-seed-boundary? true
      :artifact-construction-clojure-seed-boundary? true
      :process-clojure-seed-boundary? true
      :file-io-clojure-seed-boundary? true
      :process-and-file-io-clojure-seed-boundary? true
      :public-clojure-seed-boundary? true
      :public-wrapper-clojure-seed-boundary? true
      :global-clojure-seed-boundary? true}
     :wire wire
     :expected-stdout expected-stdout
     :expected-stdout-hash (sha256-text expected-stdout)
     :source-content-hash-verified-by-provider? false
     :source-hash-verification :provider-unverified
     :public-command-route? false
     :compiler-authored-in-gravity? false
     :provider-authored-in-gravity? false
     :backend-complete? false
     :full-language? false
     :whole-language? false
     :formal-language-complete? false
     :self-hosted? false
     :release-ready? false
     :seedless-release? false}))
