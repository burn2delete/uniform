(ns gravity.p15-public-native-admission.producer-contract)

(def p18-id "P18T04002")
(def request-artifact :gravity/p15-public-native-admission-request)
(def request-schema
  "gravity.p15-public-native-admission-request/v1")
(def admission-artifact :gravity/p15-public-native-admission)
(def admission-schema
  "gravity.p15-public-native-admission/v1")
(def contract-id :gravity/p15-public-native-admission)
(def contract-version 1)

(def producer-order [:w1 :w2 :w3])
(def source-extensions #{".gravity" ".qst"})
(def supported-target "llvm-x86_64-linux")
(def supported-target-tier "supported")

;; These are the reviewed interface names.  Artifact ids, hashes, and commit
;; identities are intentionally not embedded here: they are pins supplied by
;; the caller and are checked against the corresponding observation.
(def producer-policies
  {:w1 {:artifact-path
        "docs/artifacts/workstreams/w1/w1-executable-carrier-interface.json"
        :interface-kind
        "w1/executable-c13-c14-b1-llvm-x86_64-linux-backend"
        :interface-schema "gravity.w1.executable-carrier-interface/v1"
        :verifier-predicate
        "gravity.bootstrap/p15-s23-stage2-b3-llvm-verify!"
        :predicate-version 1
        :target "llvm-x86_64-linux"
        :policy-metadata
        {:kind
         "gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact"
         :schema-version 1}}
   :w2 {:artifact-path
        "docs/artifacts/phase-15/native-runtime/p15-s23-gravity-native-runtime-provider-interface.edn"
        :interface-kind "gravity-authored-native-runtime-provider"
        :interface-schema
        "gravity/p15-s23-gravity-native-runtime-provider-interface-v1"
        :verifier-predicate
        "gravity.p15-gravity-native-runtime-provider/consumer-handoff-valid?"
        :predicate-version 1}
   :w3 {:artifact-path
        "docs/artifacts/phase-15/native-launcher/p15-s23-contained-execution-authority.edn"
        :interface-kind
        "linux-execveat-cgroup-contained-execution-authority"
        :interface-schema
        "gravity.p15-linux-execveat-cgroup-contained-execution-authority/v1"
        :verifier-predicate
        "gravity.p15-containment-authority/verify-consumer-handoff"
        :predicate-version 1
        :final-value-schema-frozen? true
        :native-replay-schema-frozen? false
        :native-replay
        {:artifact-path
         "docs/artifacts/phase-15/native-launcher/p15-s23-contained-execution-replay.edn"
         :kind :gravity/p15-linux-contained-execution-native-replay
         :supplement-version :v3.2
         :authoritatively-supersedes :replay-v1
         :schema-version
         "gravity.p15-linux-execveat-cgroup-contained-execution-replay/v2"
         :predicate
         "gravity.p15-containment-authority/verify-native-replay"
         :predicate-version 2
         :third-permitted-symbol
         {:path [:authority :platform-gate :toolchain :predicate]
          :value-kind :native-toolchain-verifier-symbol}
         :w4-finding
         {:v3.1-symbol-fix :recorded
          :permitted-symbol-path-count 3
          :status :not-yet-accepted
          :pending-machine-gaps
          #{:transcript-output-hash-cardinality-conflict
            :receipt-staging-fields-unfrozen
            :external-stimulus-evidence-unfrozen
            :dedicated-run-identity-unproven
            :accepted-case-semantics-unfrozen}
          :external-dependency-blockers
          #{:w2-native-fd0-wire-artifact-unfrozen}}
         :top-keys
         #{:artifact :schema-version :status :artifact-id :interface-kind
           :interface-schema :target :platform :implementation
           :admitted-executable :facts :policies :cases :cleanup
           :producer-commit :producer-tree :claims}
         :raw-content-relations
         {:observation-replay-raw-content-hash
          :independently-computed-native-replay-bytes
          :verifier-replay-content-hash
          :equals-observation-replay-raw-content-hash
          :verifier-replay-artifact-id
          :equals-observation-replay-artifact-id
          :producer-artifact-raw-content-hash
          :independent-not-substitutable-for-replay-raw-content-hash}
         :acceptance
         {:status :not-yet-accepted
          :authority? false}
         :development-seams
         {:hosted-in-memory-wire
          {:path [:wire]
           :exact-keys
           #{:format :text :bytes :content-hash :packet-bytes :rule-sha256
             :source-path-hex :source-sha256 :payload :payload-bytes
             :payload-sha256 :instruction-count}
           :format "gravity-native-runtime-v1"
           :content-hash-relation :sha256-of-bytes
           :classification
           #{:development-only :nontracked :non-native :nonauthoritative}
           :development-only? true
           :tracked? false
           :native? false
           :authority? false}
          :target-neutral-stage2-packet
          {:substitutable-for-native-fd0-wire? false
           :authority? false}}}}})
