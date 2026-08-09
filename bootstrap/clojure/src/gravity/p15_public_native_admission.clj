(ns gravity.p15-public-native-admission
  "Bootstrap-only, fail-closed consumer seam for the P15/W4 boundary.

  This namespace consumes synthetic parsed W1, W2, and W3 observations only.
  It deliberately does not read an artifact, inspect a filesystem path, start
  a process, or confer dependency, public, self-hosted, release, or
  seed-retirement status.  Replay-owner contracts are unfrozen, so this v1
  seam provides negative validation only and no request can authenticate.
  The request and observation shapes below are intentionally exact.  Keeping
  this contract in a small bootstrap leaf makes a missing or broadened
  producer interface fail before any P18T04002 I/O boundary can be crossed."
  (:require [gravity.digest :as digest]))

(def ^:private p18-id "P18T04002")
(def ^:private request-artifact :gravity/p15-public-native-admission-request)
(def ^:private request-schema
  "gravity.p15-public-native-admission-request/v1")
(def ^:private admission-artifact :gravity/p15-public-native-admission)
(def ^:private admission-schema
  "gravity.p15-public-native-admission/v1")
(def ^:private contract-id :gravity/p15-public-native-admission)
(def ^:private contract-version 1)

(def ^:private producer-order [:w1 :w2 :w3])
(def ^:private source-extensions #{".gravity" ".qst"})
(def ^:private supported-target "llvm-x86_64-linux")
(def ^:private supported-target-tier "supported")

;; These are the reviewed interface names.  Artifact ids, hashes, and commit
;; identities are intentionally not embedded here: they are pins supplied by
;; the caller and are checked against the corresponding observation.
(def ^:private producer-policies
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

(def ^:private replay-policy-keys
  #{:state :replay-contract-frozen? :missing-fields :claims})

(def ^:private replay-policy-owner-keys
  #{:w1 :w2 :w3})

(def ^:private replay-policies
  {:w1
   {:state :unfrozen
    :replay-contract-frozen? false
    :missing-fields
    #{:accepted-replay-artifact-path
      :accepted-replay-artifact-kind
      :accepted-replay-schema
      :accepted-replay-schema-version
      :non-jvm-semantic-verifier-predicate
      :non-jvm-semantic-verifier-predicate-version
      :semantic-verifier-implementation-kind
      :semantic-verifier-implementation-schema
      :semantic-verifier-implementation-schema-version
      :semantic-verifier-implementation-path
      :semantic-verifier-implementation-artifact-id-rule
      :semantic-verifier-implementation-raw-content-hash
      :semantic-verifier-reviewed-a-tree-blob-binding
      :semantic-verifier-call-abi
      :semantic-verifier-result-schema
      :semantic-verifier-nonrecursive-execution-evidence
      :semantic-verifier-independent-review
      :replay-payload-containing-b-tree-blob-binding
      :w6-registry-c-binding}
    :claims
    {:public-route? false
     :clojure-seed-boundary? true
     :self-hosted? false
     :release? false}}
   :w2
   {:state :unfrozen
    :replay-contract-frozen? false
    :missing-fields
    #{:accepted-replay-artifact-path
      :accepted-replay-artifact-kind
      :accepted-replay-schema
      :accepted-replay-schema-version
      :non-jvm-semantic-verifier-predicate
      :non-jvm-semantic-verifier-predicate-version
      :semantic-verifier-implementation-kind
      :semantic-verifier-implementation-schema
      :semantic-verifier-implementation-schema-version
      :semantic-verifier-implementation-path
      :semantic-verifier-implementation-artifact-id-rule
      :semantic-verifier-implementation-raw-content-hash
      :semantic-verifier-reviewed-a-tree-blob-binding
      :semantic-verifier-call-abi
      :semantic-verifier-result-schema
      :semantic-verifier-nonrecursive-execution-evidence
      :semantic-verifier-independent-review
      :replay-payload-containing-b-tree-blob-binding
      :w6-registry-c-binding
      :accepted-tracked-native-fd0-wire-replay-contract
      :accepted-static-elf-no-pt-interp-provider-replay}
    :claims
    {:public-route? false
     :clojure-seed-boundary? true
     :self-hosted? false
     :release? false}}
   :w3
   {:state :unfrozen
    :replay-contract-frozen? false
    :missing-fields
    #{:accepted-replay-artifact-path
      :accepted-replay-artifact-kind
      :accepted-replay-schema
      :accepted-replay-schema-version
      :non-jvm-semantic-verifier-predicate
      :non-jvm-semantic-verifier-predicate-version
      :semantic-verifier-implementation-kind
      :semantic-verifier-implementation-schema
      :semantic-verifier-implementation-schema-version
      :semantic-verifier-implementation-path
      :semantic-verifier-implementation-artifact-id-rule
      :semantic-verifier-implementation-raw-content-hash
      :semantic-verifier-reviewed-a-tree-blob-binding
      :semantic-verifier-call-abi
      :semantic-verifier-result-schema
      :semantic-verifier-nonrecursive-execution-evidence
      :semantic-verifier-independent-review
      :replay-payload-containing-b-tree-blob-binding
      :w6-registry-c-binding
      :accepted-native-replay-v2-schema
      :accepted-native-replay-v3-3-supplements
      :accepted-w2-native-fd0-wire-cross-binding
      :accepted-nonrecursive-w3-semantic-verifier}
    :claims
    {:public-route? false
     :clojure-seed-boundary? true
     :self-hosted? false
     :release? false}}})

(def ^:private replay-owner-blockers
  {:w1 :w1-replay-contract-unfrozen
   :w2 :w2-replay-contract-unfrozen
   :w3 :w3-native-replay-schema-unfrozen})

(def ^:private replay-diagnostic-order
  [:pin-keys-not-exact
   :pin-replay-artifact-path-invalid
   :pin-replay-artifact-kind-invalid
   :pin-replay-schema-invalid
   :pin-replay-schema-version-invalid
   :pin-replay-artifact-id-invalid
   :pin-replay-raw-content-hash-invalid
   :pin-checkout-root-id-invalid
   :pin-checkout-root-commit-invalid
   :pin-checkout-root-tree-invalid
   :pin-checkout-root-commit-does-not-match-payload-containing-commit
   :pin-checkout-root-tree-does-not-match-payload-containing-tree
   :pin-checkout-root-id-does-not-match-derived-b-identity
   :observation-keys-not-exact
   :observation-replay-artifact-path-invalid
   :observation-replay-artifact-kind-invalid
   :observation-replay-schema-invalid
   :observation-replay-schema-version-invalid
   :observation-replay-artifact-id-invalid
   :observation-replay-raw-content-hash-invalid
   :observation-checkout-root-id-invalid
   :observation-checkout-root-commit-invalid
   :observation-checkout-root-tree-invalid
   :observation-replay-artifact-path-does-not-match-pin
   :observation-replay-artifact-kind-does-not-match-pin
   :observation-replay-schema-does-not-match-pin
   :observation-replay-schema-version-does-not-match-pin
   :observation-replay-artifact-id-does-not-match-pin
   :observation-replay-raw-content-hash-does-not-match-pin
   :observation-checkout-root-id-does-not-match-pin
   :observation-checkout-root-commit-does-not-match-pin
   :observation-checkout-root-tree-does-not-match-pin
   :replay-owner-contract-forged-or-incomplete
   :w1-replay-contract-unfrozen
   :w2-replay-contract-unfrozen
   :w3-native-replay-schema-unfrozen])

(def ^:private replay-structure-diagnostic-order
  (vec (take-while #(not= :replay-owner-contract-forged-or-incomplete %)
                   replay-diagnostic-order)))

(def ^:private future-request-v2
  {:schema "gravity.p15-public-native-admission-request/v2"
   :status :unfrozen
   :missing-contracts
   #{:accepted-w1-replay-owner-contract
     :accepted-w2-replay-owner-contract
     :accepted-w3-replay-owner-contract
     :same-object-replay-component-dirfd-observer
     :captured-size-read-loop-and-immediate-eof
     :post-read-descriptor-identity
     :payload-containing-b-git-blob-binding
     :semantic-verifier-implementation-id-and-a-tree-binding
     :descriptor-bound-non-jvm-semantic-verifier
     :nonrecursive-verifier-execution-authority
     :verifier-execution-receipt-and-review
     :symlink-raw-semantic-hostile-fixtures
     :w6-registry-c-binding
     :independent-sol-review}
   :request-field? false
   :policy-field? false
   :result-field? false
   :artifact-field? false
   :authority? false})

(def ^:private w6-payload-containing-commit-registry
  {:artifact :gravity/p18-t06-payload-containing-commit-bindings
   :artifact-path
   "docs/artifacts/phase-18/release/p18-t06-payload-containing-commit-bindings.edn"
   :kind :gravity/p18-t06-payload-containing-commit-bindings
   :schema
   "gravity/p18-t06-payload-containing-commit-bindings/v1"
   :schema-version 1
   :contract :gravity/p18-payload-containing-commit-bindings
   :contract-version 1
   :target :llvm-x86_64-linux
   :binding-slots
   [:w1-executable-carrier
    :w2-runtime-provider
    :w3-contained-execution
    :w4-public-native-route
    :w5-self-host-full-language
    :w6-p18-t03-final-closure
    :w6-p18-t05-seedless-boundary
    :w6-p18-t06-release-proof]
   :claims
   {:public-route? false
    :clojure-seed-boundary? true
    :self-hosted? false
    :release? false}
   :keysets
   {:registry-top
    #{:artifact :schema :schema-version :status :contract :contract-version
      :target :binding-slots :bindings :claims}
    :entry
    #{:slot :workstream :target :payload-path :payload-kind :payload-schema
      :payload-artifact-id :payload-raw-content-hash
      :reviewed-implementation-commit :reviewed-implementation-tree
      :payload-containing-commit :payload-containing-tree :verifier :review
      :claims}
    :verifier
    #{:predicate :predicate-version :command :replay-path
      :replay-artifact-id :replay-raw-content-hash :status}
    :review
    #{:status :reviewer-class :reviewed-commit :reviewed-tree :review-path
      :review-artifact-id :review-raw-content-hash}}
   :self-identity
   {:artifact-id-present? false
    :registry-c-identity-present? false}
   :location :external-registry-c
   :artifact-created? false
   :authority? false})

(def ^:private pin-keys
  #{:artifact-path :raw-content-hash
    :replay-artifact-path :replay-artifact-kind :replay-schema
    :replay-schema-version :replay-artifact-id :replay-raw-content-hash
    :checkout-root-id :checkout-root-commit :checkout-root-tree
    :payload-containing-commit :payload-containing-tree
    :implementation-commit :implementation-tree :artifact-id
    :interface-kind :interface-schema :verifier-predicate :predicate-version})

(def ^:private observation-keys
  #{:artifact-path :raw-content-hash
    :replay-artifact-path :replay-artifact-kind :replay-schema
    :replay-schema-version :replay-artifact-id :replay-raw-content-hash
    :checkout-root-id :checkout-root-commit :checkout-root-tree
    :payload-containing-commit :payload-containing-tree
    :consumer-handoff})

(def ^:private consumer-handoff-keys
  #{:contract :contract-version :workstream :interface-kind
    :interface-schema :artifact-id :producer-commit :producer-tree
    :verifier :review :bindings :claims})

(def ^:private verifier-keys
  #{:predicate :predicate-version :replay-artifact-id
    :replay-content-hash :status})

(def ^:private review-keys
  #{:status :reviewer-class :reviewed-commit :review-artifact-id})

(def ^:private claims-keys
  #{:public-route? :clojure-seed-boundary? :self-hosted? :release?})

(def ^:private w1-json-key-serialization
  {:final-outer-key "consumer-handoff"
   :development-candidate-outer-key "consumer_handoff_candidate"
   :nested-key-spelling :exact-kebab-case-ascii-string
   :parsed-key-mapping :remove-leading-edn-colon-only
   :snake-case-final-keys-admissible? false
   :unknown-or-extra-keys-admissible? false
   :duplicate-transformed-keys-admissible? false})

(def ^:private binding-key-sets
  {:w1 #{:carrier-artifact-id :carrier-content-hash :carrier-schema
         :source-id :semantic-id :profile :target :effects :capabilities
         :safety :accepted-diagnostic-ids :rejected-diagnostic-ids
         :provenance-edges}
   :w2 #{:accepted-carrier-artifact-id :accepted-carrier-content-hash
         :provider-artifact-id :provider-executable-path
         :provider-executable-content-hash :runtime-manifest-id
         :packet-schema :source-rule-id :abi :inherited-fds :effects
         :capabilities :no-clojure-evidence-id :no-jvm-evidence-id
         :accepted-diagnostic-ids :rejected-diagnostic-ids :residual-authority}
   :w3 #{:admitted-executable-artifact-id :admitted-executable-path
         :admitted-executable-content-hash :identity-binding-method :os-gate
         :process-tree-containment :receipt-schema :timeout-policy
         :signal-policy :output-policy :resource-policy :cleanup-policy
         :negative-guarantees :unsupported-platforms
         :accepted-diagnostic-ids :rejected-diagnostic-ids}})

(def ^:private identity-binding-keys
  #{:method :descriptor-relative-execution? :fd-bound-launch-evidence-id
    :identity-stable-snapshot? :seatbelt-contained?})

(def ^:private os-gate-keys
  #{:target :tier :evidence-id})

(def ^:private process-tree-containment-keys
  #{:os-process-tree-containment? :method :evidence-id})

(def ^:private w1-provenance-edges-keys
  #{:artifact-kind :schema-version})

(def ^:private abi-keys
  #{:target :binary-format :architecture :calling-convention})

(def ^:private w2-provider-executable-path
  "target/phase-15/native-runtime/linux-x86_64/p15-s23-gravity-native-runtime-provider")

(def ^:private w2-packet-schema "gravity-native-runtime-v1")

(def ^:private w2-inherited-fds
  {:stdin 0 :stdout 1 :stderr 2})

(def ^:private w2-effects-keys
  #{:declared :inferred :required})

(def ^:private w2-capabilities-keys
  #{:declared :required})

(def ^:private w2-residual-authority
  {:clojure-seed-boundary? true
   :public-route? false
   :self-hosted? false
   :release? false})

(def ^:private w2-rejected-diagnostic-ids
  #{"P15GNR001" "P15GNR002" "P15GNR003" "P15GNR004"
    "P15NR001" "P15NR002" "P15NR003" "P15NR004" "P15NR005"
    "P15NR006" "P15NR007" "P15NR008" "P15NR009" "P15NR010"})

(def ^:private w3-receipt-schema
  "gravity.p15-linux-execveat-cgroup-contained-execution-receipt/v1")

(def ^:private w3-timeout-policy
  {:clock :clock-monotonic
   :minimum-ms 1
   :maximum-ms 600000
   :poll-ms 10
   :on-expiry :cgroup-kill})

(def ^:private w3-signal-policy
  {:handled [:sigint :sigterm :sighup]
   :handler :record-and-clean
   :cleanup :cgroup-kill
   :cleanup-wait-ms 250})

(def ^:private w3-output-policy
  {:stdout-max-bytes 65536
   :stderr-max-bytes 65536
   :retained-prefix-bytes 4096
   :forwarding :bounded-prefix
   :on-overflow :cgroup-kill})

(def ^:private w3-resource-policy
  {:pids-max 1
   :memory-max-bytes 1073741824
   :cpu-max {:quota-us 100000 :period-us 100000}
   :rlimit-cpu-seconds 600
   :rlimit-address-space-bytes 1073741824
   :rlimit-open-files 64
   :rlimit-file-size-bytes 16777216
   :rlimit-core-bytes 0
   :run-uid :explicit-nonzero-dedicated
   :run-gid :explicit-nonzero-dedicated})

(def ^:private w3-cleanup-policy
  {:process-cleanup :cgroup-kill
   :empty-census :cgroup-procs-empty
   :cleanup-wait-ms 250
   :private-stage-cleanup :unlink-then-rmdir
   :receipt-path :absolute-new-o-excl-no-follow
   :no-survivors? true
   :no-stage-residue? true})

(def ^:private w3-unsupported-platforms
  {:targets [:darwin :darwin-arm64 :darwin-x86_64 :windows]
   :support :unsupported
   :fallback :none
   :public-fallback? false
   :diagnostic-id "P15CEA100"})

(def ^:private w3-negative-guarantees
  {:rename-replacement-blocked? true
   :in-place-mutation-blocked? true
   :pathname-exec-used? false
   :extra-inherited-fds? false
   :fork-descendant-created? false
   :posix-spawn-descendant-created? false
   :setsid-descendant-escape? false
   :double-fork-escape? false
   :leader-exit-survivors? false
   :cgroup-migration-escape? false
   :namespace-escape? false
   :timeout-survivors? false
   :interrupt-survivors? false
   :output-overflow-survivors? false
   :resource-ceilings-enforced? true
   :stage-residue? false
   :fallback-used? false})

(def ^:private w3-accepted-diagnostic-ids ["P15CEA000"])

(def ^:private w3-rejected-diagnostic-ids
  ["P15CEA100" "P15CEA101" "P15CEA102" "P15CEA103" "P15CEA104"
   "P15CEA105" "P15CEA106" "P15CEA107" "P15CEA108" "P15CEA109"
   "P15CEA110" "P15CEA111" "P15CEA112" "P15CEA113" "P15CEA114"
   "P15CEA115" "P15CEA116" "P15CEA117" "P15CEA118" "P15CEA119"
   "P15CEA120" "P15CEA121" "P15CEA199"])

(def ^:private namespace-contract
  {:namespace 'gravity.p15-public-native-admission
   :contract-boundary :p15-w4-dependency-authentication-only
   :public-api
   {'public-native-admission-contract
    {:arglists '([])
     :returns :exact-versioned-consumer-contract}
    'default-public-native-admission
    {:arglists '([])
     :returns :incomplete-fail-closed-decision}
    'validate-public-native-admission
    {:arglists '([request])
     :returns :structured-admission-decision}
    'public-native-admission?
    {:arglists '([request])
     :returns :boolean}
    'verified-public-route-handoff?
    {:arglists '([route])
     :returns :boolean}}
   :artifact-inputs [:parsed-w1-observation :parsed-w2-observation
                     :parsed-w3-observation :independent-raw-content-hashes
                     :independent-replay-artifact-ids
                     :independent-replay-raw-content-hashes
                     :synthetic-hostile-replay-identities]
   :artifact-outputs [:negative-only-dependency-decision]
   :ownership
   {:owns [:exact-w1-w2-w3-consumer-contract
           :fail-closed-predicate
           :cross-workstream-identity-binding
           :checkout-root-id-derivation]
    :does-not-own [:artifact-reads :raw-content-hash-computation
                   :replay-semantic-id-computation :path-admission
                   :process-launch :public-route-selection :seed-retirement
                   :self-hosting :release-claims]}
   :dependency-direction
   {:requires ['clojure.core 'gravity.digest]
    :forbids ['gravity.bootstrap 'gravity.diagnostics
              'java.io 'java.nio 'java.lang.ProcessBuilder]}
   :bootstrap-hosted? true
   :public? false
   :public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})

(def public-native-admission-contract
  "The exact W4 consumer request contract.

  `:pins` and `:observations` carry exact synthetic replay and checkout-root
  identities around an unchanged producer handoff envelope.  Producer payloads
  bind implementation A/tree; external fields bind payload-containing B/tree,
  so no payload embeds B or a later C identity.  Replay-owner policies remain
  explicitly unfrozen.  This v1 contract performs no replay reads, accepts no
  reader or verifier callback, and is restricted to hostile negative inputs.
  It cannot authenticate dependencies or authorize source, output, or process
  I/O."
  {:artifact request-artifact
   :schema-version request-schema
   :pins
   {:keys pin-keys
    :workstreams producer-order
    :producer-policies producer-policies
    :identity-separation
    {:producer-payload [:implementation-commit :implementation-tree]
     :external-payload-containing
     [:payload-containing-commit :payload-containing-tree]
     :requires-distinct? true
     :implementation-a-in-payload? true
     :payload-containing-b-in-payload? false
     :later-c-in-payload? false}
    :replay-observation
    {:fields [:replay-artifact-path :replay-artifact-kind
              :replay-schema :replay-schema-version :replay-artifact-id
              :replay-raw-content-hash :checkout-root-id
              :checkout-root-commit :checkout-root-tree]
     :synthetic-hostile-only? true
     :replay-io-performed? false
     :producer-raw-content-hash-equality-required? false
     :semantic-and-raw-roles-distinct? true
     :semantic-and-raw-value-inequality-required? false
     :verifier-binding-defensive-only? true}
    :path-policy (into {} (map (fn [[workstream policy]]
                                 [workstream (:artifact-path policy)])
                               producer-policies))}
   :observations
   {:keys observation-keys
    :replay-fields
    [:replay-artifact-path :replay-artifact-kind
     :replay-schema :replay-schema-version :replay-artifact-id
     :replay-raw-content-hash :checkout-root-id
     :checkout-root-commit :checkout-root-tree]
    :w1-json-key-serialization w1-json-key-serialization
    :consumer-handoff-keys consumer-handoff-keys
    :verifier-keys verifier-keys
    :review-keys review-keys
    :claims-keys claims-keys
    :binding-keys binding-key-sets
    :nested-binding-keys
    {:identity-binding-method identity-binding-keys
     :os-gate os-gate-keys
     :process-tree-containment process-tree-containment-keys
     :w1-provenance-edges w1-provenance-edges-keys
     :abi abi-keys
     :w2-effects w2-effects-keys
     :w2-capabilities w2-capabilities-keys}}
   :replay-owner-policy-exact-keys replay-policy-keys
   :replay-owner-policies replay-policies
   :replay-diagnostic-precedence replay-diagnostic-order
   :future-request-v2 future-request-v2
   :source-extensions source-extensions
   :payload-containing-commit-registry
   w6-payload-containing-commit-registry
   :decision
   {:artifact admission-artifact
    :schema-version admission-schema
    :status :negative-only-v1
    :success :unreachable
    :current-gates [:w1-replay-contract-unfrozen
                    :w2-replay-contract-unfrozen
                    :w3-native-replay-schema-unfrozen]
    :public-result-cardinality {:diagnostics 1 :rejections 1}
    :nil-request-terminal :missing-reviewed-w1-w2-w3-observations
    :public-predicate-nonmap-uses-default? true
    :request-shape-precedence :first-existing-shape-issue
    :replay-structure-precedence :first-match-exact-replay-diagnostic-order
    :valid-unfrozen-owner-terminal :w1-replay-contract-unfrozen
    :later-owner-blockers-emitted? false
    :legacy-validation-after-terminal? false
    :legacy-diagnostics-exposed? false
    :replay-io? false
    :route :separate-reviewed-public-route-artifact}})

(def default-public-native-admission
  "Fail-closed result before reviewed W1/W2/W3 evidence is available."
  {:artifact admission-artifact
   :schema-version admission-schema
   :status :incomplete
   :decision :dependency-interface-incomplete
   :id p18-id
   :diagnostic p18-id
   :diagnostics [{:id p18-id
                  :code :missing-reviewed-w1-w2-w3-observations
                  :path [:observations]}]
   :rejections [{:id p18-id
                 :code :missing-reviewed-w1-w2-w3-observations
                 :path [:observations]}]
   :dependencies-authenticated? false
   :dependency-interface? false
   :dependencies? false
   :bounded-native-route-admitted? false
   :io-authorized? false
   :public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})

(defn- exact-keys?
  [value expected]
  (and (map? value)
       (= expected (set (keys value)))))

(defn- identifier-text
  "Normalize only keyword/symbol/string identity fields.

  JSON producers use strings while EDN producers may use symbols or keywords.
  No arbitrary value is coerced; maps, numbers, booleans, and collections are
  never silently accepted as identities."
  [value]
  (cond
    (string? value) value
    (symbol? value) (str value)
    (keyword? value) (let [text (str value)]
                       (subs text 1))
    :else nil))

(defn- identifier?
  [value]
  (let [text (identifier-text value)]
    (and (string? text)
         (not (empty? text))
         (not (re-find #"[\s\u0000]" text)))))

(defn- same-identity?
  [left right]
  (let [left-text (identifier-text left)
        right-text (identifier-text right)]
    (and (some? left-text) (= left-text right-text))))

(defn- sha256?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- commit?
  [value]
  (and (string? value)
       (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- visible-ascii-string?
  [value]
  (and (string? value)
       (not (empty? value))
       (every? (fn [character]
                 (let [code (int character)]
                   (<= 0x21 code 0x7e)))
               value)))

(defn- exact-ascii-keyword?
  [value]
  (and (keyword? value)
       (let [text (subs (str value) 1)]
         (visible-ascii-string? text))))

(defn- positive-integer?
  [value]
  (and (integer? value) (pos? value)))

(defn- normalized-repo-relative-posix-path?
  [value]
  (and (visible-ascii-string? value)
       (not (.startsWith ^String value "/"))
       (not (.endsWith ^String value "/"))
       (not (.contains ^String value "\\"))
       (not (.contains ^String value "//"))
       (not (re-find #"^[A-Za-z]:" value))
       (not (re-find #"(^|/)\.\.?(/|$)" value))))

(defn- derive-checkout-root-id
  [payload-containing-commit payload-containing-tree]
  (when (and (commit? payload-containing-commit)
             (commit? payload-containing-tree))
    (str "sha256:"
         (digest/sha256-hex
          (str "gravity-w4-checkout-root-v1\u0000"
               payload-containing-commit
               "\u0000"
               payload-containing-tree)))))

(defn- relative-path?
  [value]
  (and (string? value)
       (not (empty? value))
       (not (.startsWith ^String value "/"))
       (not (re-find #"(^|/)\.\.?(/|$)" value))
       (not (re-find #"[\u0000\r\n]" value))))

(defn- nonempty-evidence?
  [value]
  (and (some? value)
       (or (and (string? value) (not (empty? value)))
           (and (identifier? value) (not (empty? (identifier-text value))))
           (and (coll? value) (seq value))
           (and (map? value) (seq value)))))

(defn- exact-structured-values?
  [value expected-keys]
  (and (exact-keys? value expected-keys)
       (every? coll? (vals value))))

(defn- os-gate-target
  "Return the target identity carried by a scalar or target-bearing gate.

  W3 implementations may carry a scalar target identity or a structured gate
  record.  A structured record must expose :target; no other field can
  silently stand in for the target binding."
  [os-gate]
  (when (exact-keys? os-gate os-gate-keys)
    (:target os-gate)))

(defn- issue
  [code path]
  {:id p18-id
   :diagnostic p18-id
   :code code
   :path path})

(defn- append-issue
  [issues code path condition]
  (if condition issues (conj issues (issue code path))))

(defn- replay-policy-missing-fields-exact?
  [workstream value]
  (case workstream
    :w1
    (= value
       #{:accepted-replay-artifact-path
         :accepted-replay-artifact-kind
         :accepted-replay-schema
         :accepted-replay-schema-version
         :non-jvm-semantic-verifier-predicate
         :non-jvm-semantic-verifier-predicate-version
         :semantic-verifier-implementation-kind
         :semantic-verifier-implementation-schema
         :semantic-verifier-implementation-schema-version
         :semantic-verifier-implementation-path
         :semantic-verifier-implementation-artifact-id-rule
         :semantic-verifier-implementation-raw-content-hash
         :semantic-verifier-reviewed-a-tree-blob-binding
         :semantic-verifier-call-abi
         :semantic-verifier-result-schema
         :semantic-verifier-nonrecursive-execution-evidence
         :semantic-verifier-independent-review
         :replay-payload-containing-b-tree-blob-binding
         :w6-registry-c-binding})

    :w2
    (= value
       #{:accepted-replay-artifact-path
         :accepted-replay-artifact-kind
         :accepted-replay-schema
         :accepted-replay-schema-version
         :non-jvm-semantic-verifier-predicate
         :non-jvm-semantic-verifier-predicate-version
         :semantic-verifier-implementation-kind
         :semantic-verifier-implementation-schema
         :semantic-verifier-implementation-schema-version
         :semantic-verifier-implementation-path
         :semantic-verifier-implementation-artifact-id-rule
         :semantic-verifier-implementation-raw-content-hash
         :semantic-verifier-reviewed-a-tree-blob-binding
         :semantic-verifier-call-abi
         :semantic-verifier-result-schema
         :semantic-verifier-nonrecursive-execution-evidence
         :semantic-verifier-independent-review
         :replay-payload-containing-b-tree-blob-binding
         :w6-registry-c-binding
         :accepted-tracked-native-fd0-wire-replay-contract
         :accepted-static-elf-no-pt-interp-provider-replay})

    :w3
    (= value
       #{:accepted-replay-artifact-path
         :accepted-replay-artifact-kind
         :accepted-replay-schema
         :accepted-replay-schema-version
         :non-jvm-semantic-verifier-predicate
         :non-jvm-semantic-verifier-predicate-version
         :semantic-verifier-implementation-kind
         :semantic-verifier-implementation-schema
         :semantic-verifier-implementation-schema-version
         :semantic-verifier-implementation-path
         :semantic-verifier-implementation-artifact-id-rule
         :semantic-verifier-implementation-raw-content-hash
         :semantic-verifier-reviewed-a-tree-blob-binding
         :semantic-verifier-call-abi
         :semantic-verifier-result-schema
         :semantic-verifier-nonrecursive-execution-evidence
         :semantic-verifier-independent-review
         :replay-payload-containing-b-tree-blob-binding
         :w6-registry-c-binding
         :accepted-native-replay-v2-schema
         :accepted-native-replay-v3-3-supplements
         :accepted-w2-native-fd0-wire-cross-binding
         :accepted-nonrecursive-w3-semantic-verifier})

    false))

(defn- replay-policy-valid?
  [workstream policy]
  (and (exact-keys? policy replay-policy-keys)
       (= :unfrozen (:state policy))
       (false? (:replay-contract-frozen? policy))
       (replay-policy-missing-fields-exact?
        workstream (:missing-fields policy))
       (= {:public-route? false
           :clojure-seed-boundary? true
           :self-hosted? false
           :release? false}
          (:claims policy))))

(defn- replay-policy-table-valid?
  []
  (and (exact-keys? replay-policies replay-policy-owner-keys)
       (every? (fn [workstream]
                 (replay-policy-valid?
                  workstream (get replay-policies workstream)))
               producer-order)))

(defn- validate-replay-policy
  [workstream]
  (let [path [:replay-owner-policies workstream]
        blocker (get replay-owner-blockers workstream)]
    (cond-> []
      (and (= :w1 workstream)
           (not (replay-policy-table-valid?)))
      (conj (issue :replay-owner-contract-forged-or-incomplete
                   [:replay-owner-policies]))

      true
      (conj (issue blocker path)))))

(defn- validate-replay-pin-structure
  [workstream pin]
  (let [path [:pins workstream]]
    (cond-> []
      (not (exact-keys? pin pin-keys))
      (conj (issue :pin-keys-not-exact path))

      (and (exact-keys? pin pin-keys)
           (not (normalized-repo-relative-posix-path?
                 (:replay-artifact-path pin))))
      (conj (issue :pin-replay-artifact-path-invalid
                   (conj path :replay-artifact-path)))

      (and (exact-keys? pin pin-keys)
           (not (exact-ascii-keyword? (:replay-artifact-kind pin))))
      (conj (issue :pin-replay-artifact-kind-invalid
                   (conj path :replay-artifact-kind)))

      (and (exact-keys? pin pin-keys)
           (not (visible-ascii-string? (:replay-schema pin))))
      (conj (issue :pin-replay-schema-invalid
                   (conj path :replay-schema)))

      (and (exact-keys? pin pin-keys)
           (not (positive-integer? (:replay-schema-version pin))))
      (conj (issue :pin-replay-schema-version-invalid
                   (conj path :replay-schema-version)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:replay-artifact-id pin))))
      (conj (issue :pin-replay-artifact-id-invalid
                   (conj path :replay-artifact-id)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:replay-raw-content-hash pin))))
      (conj (issue :pin-replay-raw-content-hash-invalid
                   (conj path :replay-raw-content-hash)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:checkout-root-id pin))))
      (conj (issue :pin-checkout-root-id-invalid
                   (conj path :checkout-root-id)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:checkout-root-commit pin))))
      (conj (issue :pin-checkout-root-commit-invalid
                   (conj path :checkout-root-commit)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:checkout-root-tree pin))))
      (conj (issue :pin-checkout-root-tree-invalid
                   (conj path :checkout-root-tree)))

      (and (exact-keys? pin pin-keys)
           (not= (:checkout-root-commit pin)
                 (:payload-containing-commit pin)))
      (conj (issue
             :pin-checkout-root-commit-does-not-match-payload-containing-commit
             (conj path :checkout-root-commit)))

      (and (exact-keys? pin pin-keys)
           (not= (:checkout-root-tree pin)
                 (:payload-containing-tree pin)))
      (conj (issue
             :pin-checkout-root-tree-does-not-match-payload-containing-tree
             (conj path :checkout-root-tree)))

      (and (exact-keys? pin pin-keys)
           (not= (:checkout-root-id pin)
                 (derive-checkout-root-id
                  (:payload-containing-commit pin)
                  (:payload-containing-tree pin))))
      (conj (issue :pin-checkout-root-id-does-not-match-derived-b-identity
                   (conj path :checkout-root-id))))))

(defn- validate-replay-observation-structure
  [workstream pin observation]
  (let [path [:observations workstream]]
    (cond-> []
      (not (exact-keys? observation observation-keys))
      (conj (issue :observation-keys-not-exact path))

      (and (exact-keys? observation observation-keys)
           (not (normalized-repo-relative-posix-path?
                 (:replay-artifact-path observation))))
      (conj (issue :observation-replay-artifact-path-invalid
                   (conj path :replay-artifact-path)))

      (and (exact-keys? observation observation-keys)
           (not (exact-ascii-keyword?
                 (:replay-artifact-kind observation))))
      (conj (issue :observation-replay-artifact-kind-invalid
                   (conj path :replay-artifact-kind)))

      (and (exact-keys? observation observation-keys)
           (not (visible-ascii-string? (:replay-schema observation))))
      (conj (issue :observation-replay-schema-invalid
                   (conj path :replay-schema)))

      (and (exact-keys? observation observation-keys)
           (not (positive-integer? (:replay-schema-version observation))))
      (conj (issue :observation-replay-schema-version-invalid
                   (conj path :replay-schema-version)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:replay-artifact-id observation))))
      (conj (issue :observation-replay-artifact-id-invalid
                   (conj path :replay-artifact-id)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:replay-raw-content-hash observation))))
      (conj (issue :observation-replay-raw-content-hash-invalid
                   (conj path :replay-raw-content-hash)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:checkout-root-id observation))))
      (conj (issue :observation-checkout-root-id-invalid
                   (conj path :checkout-root-id)))

      (and (exact-keys? observation observation-keys)
           (not (commit? (:checkout-root-commit observation))))
      (conj (issue :observation-checkout-root-commit-invalid
                   (conj path :checkout-root-commit)))

      (and (exact-keys? observation observation-keys)
           (not (commit? (:checkout-root-tree observation))))
      (conj (issue :observation-checkout-root-tree-invalid
                   (conj path :checkout-root-tree)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-artifact-path observation)
                 (:replay-artifact-path pin)))
      (conj (issue :observation-replay-artifact-path-does-not-match-pin
                   (conj path :replay-artifact-path)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-artifact-kind observation)
                 (:replay-artifact-kind pin)))
      (conj (issue :observation-replay-artifact-kind-does-not-match-pin
                   (conj path :replay-artifact-kind)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-schema observation)
                 (:replay-schema pin)))
      (conj (issue :observation-replay-schema-does-not-match-pin
                   (conj path :replay-schema)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-schema-version observation)
                 (:replay-schema-version pin)))
      (conj (issue :observation-replay-schema-version-does-not-match-pin
                   (conj path :replay-schema-version)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-artifact-id observation)
                 (:replay-artifact-id pin)))
      (conj (issue :observation-replay-artifact-id-does-not-match-pin
                   (conj path :replay-artifact-id)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-raw-content-hash observation)
                 (:replay-raw-content-hash pin)))
      (conj (issue :observation-replay-raw-content-hash-does-not-match-pin
                   (conj path :replay-raw-content-hash)))

      (and (exact-keys? observation observation-keys)
           (not= (:checkout-root-id observation)
                 (:checkout-root-id pin)))
      (conj (issue :observation-checkout-root-id-does-not-match-pin
                   (conj path :checkout-root-id)))

      (and (exact-keys? observation observation-keys)
           (not= (:checkout-root-commit observation)
                 (:checkout-root-commit pin)))
      (conj (issue :observation-checkout-root-commit-does-not-match-pin
                   (conj path :checkout-root-commit)))

      (and (exact-keys? observation observation-keys)
           (not= (:checkout-root-tree observation)
                 (:checkout-root-tree pin)))
      (conj (issue :observation-checkout-root-tree-does-not-match-pin
                   (conj path :checkout-root-tree))))))

(defn- replay-pin-issue-for-code
  [code workstream pin]
  (let [path [:pins workstream]
        exact? (exact-keys? pin pin-keys)]
    (case code
      :pin-keys-not-exact
      (when-not exact?
        (issue code path))

      :pin-replay-artifact-path-invalid
      (when (and exact?
                 (not (normalized-repo-relative-posix-path?
                       (:replay-artifact-path pin))))
        (issue code (conj path :replay-artifact-path)))

      :pin-replay-artifact-kind-invalid
      (when (and exact?
                 (not (exact-ascii-keyword? (:replay-artifact-kind pin))))
        (issue code (conj path :replay-artifact-kind)))

      :pin-replay-schema-invalid
      (when (and exact?
                 (not (visible-ascii-string? (:replay-schema pin))))
        (issue code (conj path :replay-schema)))

      :pin-replay-schema-version-invalid
      (when (and exact?
                 (not (positive-integer? (:replay-schema-version pin))))
        (issue code (conj path :replay-schema-version)))

      :pin-replay-artifact-id-invalid
      (when (and exact? (not (sha256? (:replay-artifact-id pin))))
        (issue code (conj path :replay-artifact-id)))

      :pin-replay-raw-content-hash-invalid
      (when (and exact? (not (sha256? (:replay-raw-content-hash pin))))
        (issue code (conj path :replay-raw-content-hash)))

      :pin-checkout-root-id-invalid
      (when (and exact? (not (sha256? (:checkout-root-id pin))))
        (issue code (conj path :checkout-root-id)))

      :pin-checkout-root-commit-invalid
      (when (and exact? (not (commit? (:checkout-root-commit pin))))
        (issue code (conj path :checkout-root-commit)))

      :pin-checkout-root-tree-invalid
      (when (and exact? (not (commit? (:checkout-root-tree pin))))
        (issue code (conj path :checkout-root-tree)))

      :pin-checkout-root-commit-does-not-match-payload-containing-commit
      (when (and exact?
                 (not= (:checkout-root-commit pin)
                       (:payload-containing-commit pin)))
        (issue code (conj path :checkout-root-commit)))

      :pin-checkout-root-tree-does-not-match-payload-containing-tree
      (when (and exact?
                 (not= (:checkout-root-tree pin)
                       (:payload-containing-tree pin)))
        (issue code (conj path :checkout-root-tree)))

      :pin-checkout-root-id-does-not-match-derived-b-identity
      (when (and exact?
                 (not= (:checkout-root-id pin)
                       (derive-checkout-root-id
                        (:payload-containing-commit pin)
                        (:payload-containing-tree pin))))
        (issue code (conj path :checkout-root-id)))

      nil)))

(defn- replay-observation-issue-for-code
  [code workstream pin observation]
  (let [path [:observations workstream]
        exact? (exact-keys? observation observation-keys)]
    (case code
      :observation-keys-not-exact
      (when-not exact?
        (issue code path))

      :observation-replay-artifact-path-invalid
      (when (and exact?
                 (not (normalized-repo-relative-posix-path?
                       (:replay-artifact-path observation))))
        (issue code (conj path :replay-artifact-path)))

      :observation-replay-artifact-kind-invalid
      (when (and exact?
                 (not (exact-ascii-keyword?
                       (:replay-artifact-kind observation))))
        (issue code (conj path :replay-artifact-kind)))

      :observation-replay-schema-invalid
      (when (and exact?
                 (not (visible-ascii-string? (:replay-schema observation))))
        (issue code (conj path :replay-schema)))

      :observation-replay-schema-version-invalid
      (when (and exact?
                 (not (positive-integer?
                       (:replay-schema-version observation))))
        (issue code (conj path :replay-schema-version)))

      :observation-replay-artifact-id-invalid
      (when (and exact? (not (sha256? (:replay-artifact-id observation))))
        (issue code (conj path :replay-artifact-id)))

      :observation-replay-raw-content-hash-invalid
      (when (and exact?
                 (not (sha256? (:replay-raw-content-hash observation))))
        (issue code (conj path :replay-raw-content-hash)))

      :observation-checkout-root-id-invalid
      (when (and exact? (not (sha256? (:checkout-root-id observation))))
        (issue code (conj path :checkout-root-id)))

      :observation-checkout-root-commit-invalid
      (when (and exact? (not (commit? (:checkout-root-commit observation))))
        (issue code (conj path :checkout-root-commit)))

      :observation-checkout-root-tree-invalid
      (when (and exact? (not (commit? (:checkout-root-tree observation))))
        (issue code (conj path :checkout-root-tree)))

      :observation-replay-artifact-path-does-not-match-pin
      (when (and exact?
                 (not= (:replay-artifact-path observation)
                       (:replay-artifact-path pin)))
        (issue code (conj path :replay-artifact-path)))

      :observation-replay-artifact-kind-does-not-match-pin
      (when (and exact?
                 (not= (:replay-artifact-kind observation)
                       (:replay-artifact-kind pin)))
        (issue code (conj path :replay-artifact-kind)))

      :observation-replay-schema-does-not-match-pin
      (when (and exact?
                 (not= (:replay-schema observation) (:replay-schema pin)))
        (issue code (conj path :replay-schema)))

      :observation-replay-schema-version-does-not-match-pin
      (when (and exact?
                 (not= (:replay-schema-version observation)
                       (:replay-schema-version pin)))
        (issue code (conj path :replay-schema-version)))

      :observation-replay-artifact-id-does-not-match-pin
      (when (and exact?
                 (not= (:replay-artifact-id observation)
                       (:replay-artifact-id pin)))
        (issue code (conj path :replay-artifact-id)))

      :observation-replay-raw-content-hash-does-not-match-pin
      (when (and exact?
                 (not= (:replay-raw-content-hash observation)
                       (:replay-raw-content-hash pin)))
        (issue code (conj path :replay-raw-content-hash)))

      :observation-checkout-root-id-does-not-match-pin
      (when (and exact?
                 (not= (:checkout-root-id observation)
                       (:checkout-root-id pin)))
        (issue code (conj path :checkout-root-id)))

      :observation-checkout-root-commit-does-not-match-pin
      (when (and exact?
                 (not= (:checkout-root-commit observation)
                       (:checkout-root-commit pin)))
        (issue code (conj path :checkout-root-commit)))

      :observation-checkout-root-tree-does-not-match-pin
      (when (and exact?
                 (not= (:checkout-root-tree observation)
                       (:checkout-root-tree pin)))
        (issue code (conj path :checkout-root-tree)))

      nil)))

(defn- first-replay-structure-issue
  [pins observations]
  (some (fn [code]
          (some (fn [workstream]
                  (or (replay-pin-issue-for-code
                       code workstream (get pins workstream))
                      (replay-observation-issue-for-code
                       code workstream
                       (get pins workstream)
                       (get observations workstream))))
                producer-order))
        replay-structure-diagnostic-order))

(defn- validate-pin
  [workstream pin]
  (let [policy (get producer-policies workstream)
        path [:pins workstream]]
    (cond-> []
      (not (exact-keys? pin pin-keys))
      (conj (issue :pin-keys-not-exact path))

      (and (exact-keys? pin pin-keys)
           (not (normalized-repo-relative-posix-path?
                 (:replay-artifact-path pin))))
      (conj (issue :pin-replay-artifact-path-invalid
                   (conj path :replay-artifact-path)))

      (and (exact-keys? pin pin-keys)
           (not (exact-ascii-keyword? (:replay-artifact-kind pin))))
      (conj (issue :pin-replay-artifact-kind-invalid
                   (conj path :replay-artifact-kind)))

      (and (exact-keys? pin pin-keys)
           (not (visible-ascii-string? (:replay-schema pin))))
      (conj (issue :pin-replay-schema-invalid
                   (conj path :replay-schema)))

      (and (exact-keys? pin pin-keys)
           (not (positive-integer? (:replay-schema-version pin))))
      (conj (issue :pin-replay-schema-version-invalid
                   (conj path :replay-schema-version)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:replay-artifact-id pin))))
      (conj (issue :pin-replay-artifact-id-invalid
                   (conj path :replay-artifact-id)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:replay-raw-content-hash pin))))
      (conj (issue :pin-replay-raw-content-hash-invalid
                   (conj path :replay-raw-content-hash)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:checkout-root-id pin))))
      (conj (issue :pin-checkout-root-id-invalid
                   (conj path :checkout-root-id)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:checkout-root-commit pin))))
      (conj (issue :pin-checkout-root-commit-invalid
                   (conj path :checkout-root-commit)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:checkout-root-tree pin))))
      (conj (issue :pin-checkout-root-tree-invalid
                   (conj path :checkout-root-tree)))

      (and (exact-keys? pin pin-keys)
           (not= (:checkout-root-commit pin)
                 (:payload-containing-commit pin)))
      (conj (issue
             :pin-checkout-root-commit-does-not-match-payload-containing-commit
             (conj path :checkout-root-commit)))

      (and (exact-keys? pin pin-keys)
           (not= (:checkout-root-tree pin)
                 (:payload-containing-tree pin)))
      (conj (issue
             :pin-checkout-root-tree-does-not-match-payload-containing-tree
             (conj path :checkout-root-tree)))

      (and (exact-keys? pin pin-keys)
           (not= (:checkout-root-id pin)
                 (derive-checkout-root-id
                  (:payload-containing-commit pin)
                  (:payload-containing-tree pin))))
      (conj (issue :pin-checkout-root-id-does-not-match-derived-b-identity
                   (conj path :checkout-root-id)))

      (and (exact-keys? pin pin-keys)
           (not= (:artifact-path pin) (:artifact-path policy)))
      (conj (issue :pin-artifact-path-mismatch (conj path :artifact-path)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:raw-content-hash pin))))
      (conj (issue :pin-raw-content-hash-invalid
                   (conj path :raw-content-hash)))

      (and (exact-keys? pin pin-keys)
           (not (sha256? (:artifact-id pin))))
      (conj (issue :pin-artifact-id-invalid (conj path :artifact-id)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:payload-containing-commit pin))))
      (conj (issue :pin-payload-containing-commit-invalid
                   (conj path :payload-containing-commit)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:payload-containing-tree pin))))
      (conj (issue :pin-payload-containing-tree-invalid
                   (conj path :payload-containing-tree)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:implementation-commit pin))))
      (conj (issue :pin-implementation-commit-invalid
                   (conj path :implementation-commit)))

      (and (exact-keys? pin pin-keys)
           (not (commit? (:implementation-tree pin))))
      (conj (issue :pin-implementation-tree-invalid
                   (conj path :implementation-tree)))

      (and (exact-keys? pin pin-keys)
           (= (:payload-containing-commit pin)
              (:implementation-commit pin)))
      (conj (issue
             :pin-payload-containing-commit-not-distinct-from-implementation
             (conj path :payload-containing-commit)))

      (and (exact-keys? pin pin-keys)
           (= (:payload-containing-tree pin)
              (:implementation-tree pin)))
      (conj (issue
             :pin-payload-containing-tree-not-distinct-from-implementation
             (conj path :payload-containing-tree)))

      (and (exact-keys? pin pin-keys)
           (not (same-identity? (:interface-kind pin)
                                (:interface-kind policy))))
      (conj (issue :pin-interface-kind-mismatch (conj path :interface-kind)))

      (and (exact-keys? pin pin-keys)
           (not= (:interface-schema pin) (:interface-schema policy)))
      (conj (issue :pin-interface-schema-mismatch
                   (conj path :interface-schema)))

      (and (exact-keys? pin pin-keys)
           (not (same-identity? (:verifier-predicate pin)
                                (:verifier-predicate policy))))
      (conj (issue :pin-verifier-predicate-mismatch
                   (conj path :verifier-predicate)))

      (and (exact-keys? pin pin-keys)
           (not= (:predicate-version pin) (:predicate-version policy)))
      (conj (issue :pin-predicate-version-mismatch
                   (conj path :predicate-version))))))

(defn- validate-verifier
  [workstream pin verifier]
  (let [path [:observations workstream :consumer-handoff :verifier]
        policy (get producer-policies workstream)]
    (cond-> []
      (not (exact-keys? verifier verifier-keys))
      (conj (issue :verifier-keys-not-exact path))

      (and (exact-keys? verifier verifier-keys)
           (not (same-identity? (:predicate verifier)
                                (:verifier-predicate pin))))
      (conj (issue :verifier-predicate-does-not-match-pin
                   (conj path :predicate)))

      (and (exact-keys? verifier verifier-keys)
           (not (same-identity? (:predicate verifier)
                                (:verifier-predicate policy))))
      (conj (issue :verifier-predicate-not-reviewed
                   (conj path :predicate)))

      (and (exact-keys? verifier verifier-keys)
           (not= (:predicate-version verifier)
                 (:predicate-version pin)))
      (conj (issue :verifier-version-mismatch
                   (conj path :predicate-version)))

      (and (exact-keys? verifier verifier-keys)
           (not= :passed (:status verifier)))
      (conj (issue :verifier-replay-not-passed (conj path :status)))

      (and (exact-keys? verifier verifier-keys)
           (not (sha256? (:replay-artifact-id verifier))))
      (conj (issue :verifier-replay-artifact-id-invalid
                   (conj path :replay-artifact-id)))

      (and (exact-keys? verifier verifier-keys)
           (not (sha256? (:replay-content-hash verifier))))
      (conj (issue :verifier-replay-content-hash-invalid
                   (conj path :replay-content-hash))))))

(defn- validate-review
  [workstream pin review]
  (let [path [:observations workstream :consumer-handoff :review]]
    (cond-> []
      (not (exact-keys? review review-keys))
      (conj (issue :review-keys-not-exact path))

      (and (exact-keys? review review-keys)
           (not= :accepted (:status review)))
      (conj (issue :review-not-independent-or-complete
                   (conj path :status)))

      (and (exact-keys? review review-keys)
           (not= :independent-sol (:reviewer-class review)))
      (conj (issue :review-not-independent (conj path :reviewer-class)))

      (and (exact-keys? review review-keys)
           (not= (:reviewed-commit review)
                 (:implementation-commit pin)))
      (conj (issue :reviewed-commit-does-not-match-implementation
                   (conj path :reviewed-commit)))

      (and (exact-keys? review review-keys)
           (not (sha256? (:review-artifact-id review))))
      (conj (issue :review-artifact-id-invalid
                   (conj path :review-artifact-id))))))

(defn- validate-claims
  [workstream claims]
  (let [path [:observations workstream :consumer-handoff :claims]]
    (cond-> []
      (not (exact-keys? claims claims-keys))
      (conj (issue :claims-keys-not-exact path))

      (and (exact-keys? claims claims-keys)
           (not (false? (:public-route? claims))))
      (conj (issue :premature-public-route-claim
                   (conj path :public-route?)))

      (and (exact-keys? claims claims-keys)
           (not (true? (:clojure-seed-boundary? claims))))
      (conj (issue :premature-seed-boundary-retirement
                   (conj path :clojure-seed-boundary?)))

      (and (exact-keys? claims claims-keys)
           (not (false? (:self-hosted? claims))))
      (conj (issue :premature-self-hosted-claim
                   (conj path :self-hosted?)))

      (and (exact-keys? claims claims-keys)
           (not (false? (:release? claims))))
      (conj (issue :premature-release-claim (conj path :release?))))))

(defn- validate-collection-evidence
  [workstream binding key issues]
  (let [path [:observations workstream :consumer-handoff :bindings key]]
    (append-issue issues :binding-evidence-missing path
                  (nonempty-evidence? (get binding key)))))

(defn- validate-w1-bindings
  [binding pin]
  (let [path [:observations :w1 :consumer-handoff :bindings]]
    (cond-> []
      (not (sha256? (:carrier-artifact-id binding)))
      (conj (issue :w1-carrier-artifact-id-invalid
                   (conj path :carrier-artifact-id)))

      (= (:carrier-artifact-id binding) (:artifact-id pin))
      (conj (issue :w1-envelope-artifact-id-substituted-for-carrier
                   (conj path :carrier-artifact-id)))

      (not (sha256? (:carrier-content-hash binding)))
      (conj (issue :w1-carrier-content-hash-invalid
                   (conj path :carrier-content-hash)))

      (= (:carrier-content-hash binding) (:raw-content-hash pin))
      (conj (issue :w1-envelope-content-hash-substituted-for-carrier
                   (conj path :carrier-content-hash)))

      (not= 1 (:carrier-schema binding))
      (conj (issue :w1-carrier-schema-not-b3-v1
                   (conj path :carrier-schema)))

      (not (identifier? (:source-id binding)))
      (conj (issue :w1-source-id-invalid (conj path :source-id)))

      (not (identifier? (:semantic-id binding)))
      (conj (issue :w1-semantic-id-invalid (conj path :semantic-id)))

      (not (identifier? (:profile binding)))
      (conj (issue :w1-profile-invalid (conj path :profile)))

      (not (identifier? (:target binding)))
      (conj (issue :w1-target-invalid (conj path :target)))

      (and (identifier? (:target binding))
           (not (same-identity? (:target binding) supported-target)))
      (conj (issue :w1-target-not-supported
                   (conj path :target)))

      (not (nonempty-evidence? (:effects binding)))
      (conj (issue :w1-effects-evidence-missing (conj path :effects)))

      (not (nonempty-evidence? (:capabilities binding)))
      (conj (issue :w1-capabilities-evidence-missing
                   (conj path :capabilities)))

      (not (nonempty-evidence? (:safety binding)))
      (conj (issue :w1-safety-evidence-missing (conj path :safety)))

      (not (coll? (:accepted-diagnostic-ids binding)))
      (conj (issue :w1-accepted-diagnostics-not-structured
                   (conj path :accepted-diagnostic-ids)))

      (not (coll? (:rejected-diagnostic-ids binding)))
      (conj (issue :w1-rejected-diagnostics-not-structured
                   (conj path :rejected-diagnostic-ids)))

      (not (exact-keys? (:provenance-edges binding)
                        w1-provenance-edges-keys))
      (conj (issue :w1-provenance-edges-keys-not-exact
                   (conj path :provenance-edges)))

      (and (exact-keys? (:provenance-edges binding)
                        w1-provenance-edges-keys)
           (not (same-identity?
                 (:artifact-kind (:provenance-edges binding))
                 (get-in producer-policies [:w1 :policy-metadata :kind]))))
      (conj (issue :w1-provenance-artifact-kind-mismatch
                   (conj path :provenance-edges :artifact-kind)))

      (and (exact-keys? (:provenance-edges binding)
                        w1-provenance-edges-keys)
           (not= 1 (:schema-version (:provenance-edges binding))))
      (conj (issue :w1-provenance-schema-version-mismatch
                   (conj path :provenance-edges :schema-version))))))

(defn- validate-w2-bindings
  [binding pin]
  (let [path [:observations :w2 :consumer-handoff :bindings]]
    (cond-> []
      (not (sha256? (:accepted-carrier-artifact-id binding)))
      (conj (issue :w2-accepted-carrier-artifact-id-invalid
                   (conj path :accepted-carrier-artifact-id)))

      (not (sha256? (:accepted-carrier-content-hash binding)))
      (conj (issue :w2-accepted-carrier-content-hash-invalid
                   (conj path :accepted-carrier-content-hash)))

      (not (sha256? (:provider-artifact-id binding)))
      (conj (issue :w2-provider-artifact-id-invalid
                   (conj path :provider-artifact-id)))

      (not= w2-provider-executable-path
            (:provider-executable-path binding))
      (conj (issue :w2-provider-executable-path-mismatch
                   (conj path :provider-executable-path)))

      (not (sha256? (:provider-executable-content-hash binding)))
      (conj (issue :w2-provider-executable-content-hash-invalid
                   (conj path :provider-executable-content-hash)))

      (not (sha256? (:runtime-manifest-id binding)))
      (conj (issue :w2-runtime-manifest-id-invalid
                   (conj path :runtime-manifest-id)))

      (not= w2-packet-schema (:packet-schema binding))
      (conj (issue :w2-packet-schema-mismatch (conj path :packet-schema)))

      (not (sha256? (:source-rule-id binding)))
      (conj (issue :w2-source-rule-id-invalid (conj path :source-rule-id)))

      (not (exact-keys? (:abi binding) abi-keys))
      (conj (issue :w2-abi-keys-not-exact (conj path :abi)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not (same-identity? (:target (:abi binding))
                                supported-target)))
      (conj (issue :w2-abi-target-not-supported
                   (conj path :abi :target)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not= :elf (:binary-format (:abi binding))))
      (conj (issue :w2-abi-binary-format-not-elf
                   (conj path :abi :binary-format)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not= :x86_64 (:architecture (:abi binding))))
      (conj (issue :w2-abi-architecture-not-x86-64
                   (conj path :abi :architecture)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not= :sysv-amd64 (:calling-convention (:abi binding))))
      (conj (issue :w2-abi-calling-convention-not-sysv-amd64
                   (conj path :abi :calling-convention)))

      (not= w2-inherited-fds (:inherited-fds binding))
      (conj (issue :w2-inherited-fds-mismatch
                   (conj path :inherited-fds)))

      (not (exact-structured-values? (:effects binding) w2-effects-keys))
      (conj (issue :w2-effects-not-exact-structured-evidence
                   (conj path :effects)))

      (not (exact-structured-values? (:capabilities binding)
                                     w2-capabilities-keys))
      (conj (issue :w2-capabilities-not-exact-structured-evidence
                   (conj path :capabilities)))

      (not (sha256? (:no-clojure-evidence-id binding)))
      (conj (issue :w2-no-clojure-evidence-missing
                   (conj path :no-clojure-evidence-id)))

      (not (sha256? (:no-jvm-evidence-id binding)))
      (conj (issue :w2-no-jvm-evidence-missing
                   (conj path :no-jvm-evidence-id)))

      (not= #{} (:accepted-diagnostic-ids binding))
      (conj (issue :w2-accepted-diagnostics-not-exact
                   (conj path :accepted-diagnostic-ids)))

      (not= w2-rejected-diagnostic-ids
            (:rejected-diagnostic-ids binding))
      (conj (issue :w2-rejected-diagnostics-not-exact
                   (conj path :rejected-diagnostic-ids)))

      (not= w2-residual-authority (:residual-authority binding))
      (conj (issue :w2-residual-authority-mismatch
                   (conj path :residual-authority))))))

(defn- validate-w3-bindings
  [binding pin]
  (let [path [:observations :w3 :consumer-handoff :bindings]]
    (cond-> []
      (not (sha256? (:admitted-executable-artifact-id binding)))
      (conj (issue :w3-admitted-executable-artifact-id-invalid
                   (conj path :admitted-executable-artifact-id)))

      (= (:admitted-executable-artifact-id binding)
         (:artifact-id pin))
      (conj (issue :w3-envelope-and-admitted-executable-must-be-distinct
                   (conj path :admitted-executable-artifact-id)))

      (not (relative-path? (:admitted-executable-path binding)))
      (conj (issue :w3-admitted-executable-path-invalid
                   (conj path :admitted-executable-path)))

      (not (sha256? (:admitted-executable-content-hash binding)))
      (conj (issue :w3-admitted-executable-content-hash-invalid
                   (conj path :admitted-executable-content-hash)))

      (not (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys))
      (conj (issue :w3-identity-binding-method-keys-not-exact
                   (conj path :identity-binding-method)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (same-identity? (get-in binding [:identity-binding-method
                                                  :method])
                                "linux-execveat-at-empty-path")))
      (conj (issue :w3-identity-binding-method-invalid
                   (conj path :identity-binding-method :method)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (true? (get-in binding [:identity-binding-method
                                         :descriptor-relative-execution?]))))
      (conj (issue :w3-descriptor-relative-execution-required
                   (conj path :identity-binding-method
                         :descriptor-relative-execution?)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (sha256? (get-in binding [:identity-binding-method
                                           :fd-bound-launch-evidence-id]))))
      (conj (issue :w3-fd-bound-launch-evidence-id-invalid
                   (conj path :identity-binding-method
                         :fd-bound-launch-evidence-id)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (true? (get-in binding [:identity-binding-method
                                        :identity-stable-snapshot?]))))
      (conj (issue :w3-identity-stable-snapshot-required
                   (conj path :identity-binding-method
                         :identity-stable-snapshot?)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (false? (get-in binding [:identity-binding-method
                                         :seatbelt-contained?]))))
      (conj (issue :w3-seatbelt-contained-must-be-false
                   (conj path :identity-binding-method
                         :seatbelt-contained?)))

      (not (exact-keys? (:os-gate binding) os-gate-keys))
      (conj (issue :w3-os-gate-keys-not-exact (conj path :os-gate)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (identifier? (:target (:os-gate binding)))))
      (conj (issue :w3-os-gate-target-invalid
                   (conj path :os-gate :target)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (same-identity? (:target (:os-gate binding))
                                supported-target)))
      (conj (issue :w3-os-gate-target-not-supported
                   (conj path :os-gate :target)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (identifier? (:tier (:os-gate binding)))))
      (conj (issue :w3-os-gate-tier-invalid
                   (conj path :os-gate :tier)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (same-identity? (:tier (:os-gate binding))
                                supported-target-tier)))
      (conj (issue :w3-os-gate-tier-not-supported
                   (conj path :os-gate :tier)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (sha256? (:evidence-id (:os-gate binding)))))
      (conj (issue :w3-os-gate-evidence-id-invalid
                   (conj path :os-gate :evidence-id)))

      (not (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys))
      (conj (issue :w3-process-tree-containment-keys-not-exact
                   (conj path :process-tree-containment)))

      (and (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys)
           (not (true? (:os-process-tree-containment?
                        (:process-tree-containment binding)))))
      (conj (issue :w3-process-tree-contained-fact-not-proven
                   (conj path :process-tree-containment
                         :os-process-tree-containment?)))

      (and (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys)
           (not (same-identity? (:method (:process-tree-containment binding))
                                "linux-cgroup-v2-clone-into-cgroup-v1")))
      (conj (issue :w3-process-tree-containment-method-invalid
                   (conj path :process-tree-containment :method)))

      (and (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys)
           (not (sha256? (:evidence-id (:process-tree-containment binding)))))
      (conj (issue :w3-process-tree-containment-evidence-id-invalid
                   (conj path :process-tree-containment :evidence-id)))

      (not= w3-receipt-schema (:receipt-schema binding))
      (conj (issue :w3-receipt-schema-mismatch (conj path :receipt-schema)))

      (not= w3-timeout-policy (:timeout-policy binding))
      (conj (issue :w3-timeout-policy-mismatch
                   (conj path :timeout-policy)))

      (not= w3-signal-policy (:signal-policy binding))
      (conj (issue :w3-signal-policy-mismatch
                   (conj path :signal-policy)))

      (not= w3-output-policy (:output-policy binding))
      (conj (issue :w3-output-policy-mismatch
                   (conj path :output-policy)))

      (not= w3-resource-policy (:resource-policy binding))
      (conj (issue :w3-resource-policy-mismatch
                   (conj path :resource-policy)))

      (not= w3-cleanup-policy (:cleanup-policy binding))
      (conj (issue :w3-cleanup-policy-mismatch
                   (conj path :cleanup-policy)))

      (not= w3-negative-guarantees (:negative-guarantees binding))
      (conj (issue :w3-negative-guarantees-mismatch
                   (conj path :negative-guarantees)))

      (not= w3-unsupported-platforms (:unsupported-platforms binding))
      (conj (issue :w3-unsupported-platforms-mismatch
                   (conj path :unsupported-platforms)))

      (not= w3-accepted-diagnostic-ids
            (:accepted-diagnostic-ids binding))
      (conj (issue :w3-accepted-diagnostics-mismatch
                   (conj path :accepted-diagnostic-ids)))

      (not= w3-rejected-diagnostic-ids
            (:rejected-diagnostic-ids binding))
      (conj (issue :w3-rejected-diagnostics-mismatch
                   (conj path :rejected-diagnostic-ids))))))

(defn- validate-observation
  [workstream pin observation]
  (let [policy (get producer-policies workstream)
        path [:observations workstream]
        handoff (when (map? observation)
                  (:consumer-handoff observation))
        verifier (when (map? handoff) (:verifier handoff))
        review (when (map? handoff) (:review handoff))
        claims (when (map? handoff) (:claims handoff))
        bindings (when (map? handoff) (:bindings handoff))]
    (cond-> []
      (not (exact-keys? observation observation-keys))
      (conj (issue :observation-keys-not-exact path))

      (and (exact-keys? observation observation-keys)
           (not (normalized-repo-relative-posix-path?
                 (:replay-artifact-path observation))))
      (conj (issue :observation-replay-artifact-path-invalid
                   (conj path :replay-artifact-path)))

      (and (exact-keys? observation observation-keys)
           (not (exact-ascii-keyword?
                 (:replay-artifact-kind observation))))
      (conj (issue :observation-replay-artifact-kind-invalid
                   (conj path :replay-artifact-kind)))

      (and (exact-keys? observation observation-keys)
           (not (visible-ascii-string? (:replay-schema observation))))
      (conj (issue :observation-replay-schema-invalid
                   (conj path :replay-schema)))

      (and (exact-keys? observation observation-keys)
           (not (positive-integer? (:replay-schema-version observation))))
      (conj (issue :observation-replay-schema-version-invalid
                   (conj path :replay-schema-version)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:replay-artifact-id observation))))
      (conj (issue :observation-replay-artifact-id-invalid
                   (conj path :replay-artifact-id)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:replay-raw-content-hash observation))))
      (conj (issue :observation-replay-raw-content-hash-invalid
                   (conj path :replay-raw-content-hash)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:checkout-root-id observation))))
      (conj (issue :observation-checkout-root-id-invalid
                   (conj path :checkout-root-id)))

      (and (exact-keys? observation observation-keys)
           (not (commit? (:checkout-root-commit observation))))
      (conj (issue :observation-checkout-root-commit-invalid
                   (conj path :checkout-root-commit)))

      (and (exact-keys? observation observation-keys)
           (not (commit? (:checkout-root-tree observation))))
      (conj (issue :observation-checkout-root-tree-invalid
                   (conj path :checkout-root-tree)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-artifact-path observation)
                 (:replay-artifact-path pin)))
      (conj (issue :observation-replay-artifact-path-does-not-match-pin
                   (conj path :replay-artifact-path)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-artifact-kind observation)
                 (:replay-artifact-kind pin)))
      (conj (issue :observation-replay-artifact-kind-does-not-match-pin
                   (conj path :replay-artifact-kind)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-schema observation)
                 (:replay-schema pin)))
      (conj (issue :observation-replay-schema-does-not-match-pin
                   (conj path :replay-schema)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-schema-version observation)
                 (:replay-schema-version pin)))
      (conj (issue :observation-replay-schema-version-does-not-match-pin
                   (conj path :replay-schema-version)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-artifact-id observation)
                 (:replay-artifact-id pin)))
      (conj (issue :observation-replay-artifact-id-does-not-match-pin
                   (conj path :replay-artifact-id)))

      (and (exact-keys? observation observation-keys)
           (not= (:replay-raw-content-hash observation)
                 (:replay-raw-content-hash pin)))
      (conj (issue :observation-replay-raw-content-hash-does-not-match-pin
                   (conj path :replay-raw-content-hash)))

      (and (exact-keys? observation observation-keys)
           (not= (:checkout-root-id observation)
                 (:checkout-root-id pin)))
      (conj (issue :observation-checkout-root-id-does-not-match-pin
                   (conj path :checkout-root-id)))

      (and (exact-keys? observation observation-keys)
           (not= (:checkout-root-commit observation)
                 (:checkout-root-commit pin)))
      (conj (issue :observation-checkout-root-commit-does-not-match-pin
                   (conj path :checkout-root-commit)))

      (and (exact-keys? observation observation-keys)
           (not= (:checkout-root-tree observation)
                 (:checkout-root-tree pin)))
      (conj (issue :observation-checkout-root-tree-does-not-match-pin
                   (conj path :checkout-root-tree)))

      true
      (into (validate-replay-policy workstream))

      (and (exact-keys? observation observation-keys)
           (not= (:artifact-path observation) (:artifact-path pin)))
      (conj (issue :observation-artifact-path-does-not-match-pin
                   (conj path :artifact-path)))

      (and (exact-keys? observation observation-keys)
           (not= (:raw-content-hash observation)
                 (:raw-content-hash pin)))
      (conj (issue :observation-raw-content-hash-does-not-match-pin
                   (conj path :raw-content-hash)))

      (and (exact-keys? observation observation-keys)
           (not (sha256? (:raw-content-hash observation))))
      (conj (issue :observation-raw-content-hash-invalid
                   (conj path :raw-content-hash)))

      (and (exact-keys? observation observation-keys)
           (not= (:payload-containing-commit observation)
                 (:payload-containing-commit pin)))
      (conj (issue
             :observation-payload-containing-commit-does-not-match-pin
             (conj path :payload-containing-commit)))

      (and (exact-keys? observation observation-keys)
           (not= (:payload-containing-tree observation)
                 (:payload-containing-tree pin)))
      (conj (issue
             :observation-payload-containing-tree-does-not-match-pin
             (conj path :payload-containing-tree)))

      (and (exact-keys? observation observation-keys)
           (not (commit? (:payload-containing-commit observation))))
      (conj (issue :observation-payload-containing-commit-invalid
                   (conj path :payload-containing-commit)))

      (and (exact-keys? observation observation-keys)
           (not (commit? (:payload-containing-tree observation))))
      (conj (issue :observation-payload-containing-tree-invalid
                   (conj path :payload-containing-tree)))

      (not (exact-keys? handoff consumer-handoff-keys))
      (conj (issue :consumer-handoff-keys-not-exact
                   (conj path :consumer-handoff)))

      (and (= workstream :w1)
           (not (exact-keys? handoff consumer-handoff-keys)))
      (conj (issue :w1-final-consumer-handoff-missing
                   (conj path :consumer-handoff)))

      (and (= workstream :w3)
           (not (exact-keys? handoff consumer-handoff-keys)))
      (conj (issue :w3-final-consumer-handoff-missing
                   (conj path :consumer-handoff)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (same-identity? (:contract handoff) contract-id)))
      (conj (issue :consumer-handoff-contract-mismatch
                   (conj path :consumer-handoff :contract)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not= (:contract-version handoff) contract-version))
      (conj (issue :consumer-handoff-contract-version-mismatch
                   (conj path :consumer-handoff :contract-version)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (same-identity? (:workstream handoff) workstream)))
      (conj (issue :consumer-handoff-workstream-mismatch
                   (conj path :consumer-handoff :workstream)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (same-identity? (:interface-kind handoff)
                                (:interface-kind pin))))
      (conj (issue :consumer-handoff-interface-kind-mismatch
                   (conj path :consumer-handoff :interface-kind)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not= (:interface-schema handoff)
                 (:interface-schema pin)))
      (conj (issue :consumer-handoff-interface-schema-mismatch
                   (conj path :consumer-handoff :interface-schema)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not= (:artifact-id handoff)
                 (:artifact-id pin)))
      (conj (issue :consumer-handoff-artifact-id-mismatch
                   (conj path :consumer-handoff :artifact-id)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (sha256? (:artifact-id handoff))))
      (conj (issue :consumer-handoff-artifact-id-invalid
                   (conj path :consumer-handoff :artifact-id)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not= (:producer-commit handoff)
                 (:implementation-commit pin)))
      (conj (issue :producer-commit-does-not-match-implementation
                   (conj path :consumer-handoff :producer-commit)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not= (:producer-tree handoff)
                 (:implementation-tree pin)))
      (conj (issue :producer-tree-does-not-match-implementation
                   (conj path :consumer-handoff :producer-tree)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (commit? (:producer-commit handoff))))
      (conj (issue :producer-commit-invalid
                   (conj path :consumer-handoff :producer-commit)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (commit? (:producer-tree handoff))))
      (conj (issue :producer-tree-invalid
                   (conj path :consumer-handoff :producer-tree)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (map? verifier))
      (into (validate-verifier workstream pin verifier))

      (and (exact-keys? observation observation-keys)
           (exact-keys? verifier verifier-keys)
           (not= (:replay-artifact-id verifier)
                 (:replay-artifact-id observation)))
      (conj (issue :verifier-replay-artifact-id-does-not-match-observation
                   (conj path :consumer-handoff :verifier
                         :replay-artifact-id)))

      (and (exact-keys? observation observation-keys)
           (exact-keys? verifier verifier-keys)
           (not= (:replay-content-hash verifier)
                 (:replay-raw-content-hash observation)))
      (conj (issue :verifier-replay-content-hash-does-not-match-observation
                   (conj path :consumer-handoff :verifier
                         :replay-content-hash)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (map? verifier)))
      (conj (issue :verifier-observation-missing
                   (conj path :consumer-handoff :verifier)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (map? review))
      (into (validate-review workstream pin review))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (map? review)))
      (conj (issue :review-observation-missing
                   (conj path :consumer-handoff :review)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (map? claims))
      (into (validate-claims workstream claims))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (map? claims)))
      (conj (issue :claims-observation-missing
                   (conj path :consumer-handoff :claims)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (not (exact-keys? bindings (get binding-key-sets workstream))))
      (conj (issue :bindings-keys-not-exact
                   (conj path :consumer-handoff :bindings)))

      (and (exact-keys? handoff consumer-handoff-keys)
           (exact-keys? bindings (get binding-key-sets workstream))
           (= workstream :w1))
      (into (validate-w1-bindings bindings pin))

      (and (exact-keys? handoff consumer-handoff-keys)
           (exact-keys? bindings (get binding-key-sets workstream))
           (= workstream :w2))
      (into (validate-w2-bindings bindings pin))

      (and (exact-keys? handoff consumer-handoff-keys)
           (exact-keys? bindings (get binding-key-sets workstream))
           (= workstream :w3))
      (into (validate-w3-bindings bindings pin))

      (and (= workstream :w3) (map? bindings) (map? verifier)
           (not= (get-in bindings [:identity-binding-method
                                   :fd-bound-launch-evidence-id])
                 (:replay-artifact-id verifier)))
      (conj (issue :w3-fd-evidence-not-bound-to-verifier-replay
                   (conj path :consumer-handoff :bindings
                         :identity-binding-method
                         :fd-bound-launch-evidence-id)))

      (and (= workstream :w3) (map? bindings) (map? verifier)
           (not= (get-in bindings [:os-gate :evidence-id])
                 (:replay-artifact-id verifier)))
      (conj (issue :w3-os-gate-evidence-not-bound-to-verifier-replay
                   (conj path :consumer-handoff :bindings :os-gate
                         :evidence-id)))

      (and (= workstream :w3) (map? bindings) (map? verifier)
           (not= (get-in bindings [:process-tree-containment :evidence-id])
                 (:replay-artifact-id verifier)))
      (conj (issue :w3-process-evidence-not-bound-to-verifier-replay
                   (conj path :consumer-handoff :bindings
                         :process-tree-containment :evidence-id))))))

(defn- validate-cross-bindings
  [observations]
  (let [w1 (get-in observations [:w1 :consumer-handoff :bindings])
        w2 (get-in observations [:w2 :consumer-handoff :bindings])
        w3 (get-in observations [:w3 :consumer-handoff :bindings])
        w1-path [:observations :w1 :consumer-handoff :bindings]
        w2-path [:observations :w2 :consumer-handoff :bindings]
        w3-path [:observations :w3 :consumer-handoff :bindings]]
    (cond-> []
      (not= (:carrier-artifact-id w1)
            (:accepted-carrier-artifact-id w2))
      (conj (issue :w1-to-w2-carrier-artifact-cross-binding-mismatch
                   (conj w2-path :accepted-carrier-artifact-id)))

      (not= (:carrier-content-hash w1)
            (:accepted-carrier-content-hash w2))
      (conj (issue :w1-to-w2-carrier-content-cross-binding-mismatch
                   (conj w2-path :accepted-carrier-content-hash)))

      (not= (:provider-artifact-id w2)
            (:admitted-executable-artifact-id w3))
      (conj (issue :w2-to-w3-provider-artifact-cross-binding-mismatch
                   (conj w3-path :admitted-executable-artifact-id)))

      (not= (:provider-executable-path w2)
            (:admitted-executable-path w3))
      (conj (issue :w2-to-w3-provider-path-cross-binding-mismatch
                   (conj w3-path :admitted-executable-path)))

      (not= (:provider-executable-content-hash w2)
            (:admitted-executable-content-hash w3))
      (conj (issue :w2-to-w3-provider-content-cross-binding-mismatch
                   (conj w3-path :admitted-executable-content-hash)))

      (not (same-identity? (:target w1)
                           (os-gate-target (:os-gate w3))))
      (conj (issue :w1-target-to-w3-os-gate-cross-binding-mismatch
                   (conj w3-path :os-gate :target)))

      (not (same-identity? (:target w1)
                           (get-in w2 [:abi :target])))
      (conj (issue :w1-target-to-w2-abi-cross-binding-mismatch
                   (conj w2-path :abi :target))))))

(defn- base-decision
  []
  {:artifact admission-artifact
   :schema-version admission-schema
   :id p18-id
   :diagnostic p18-id
   :bounded-native-route-admitted? false
   :io-authorized? false
   :public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})

(defn- decision-with-issues
  [status decision issues]
  (merge (base-decision)
         {:status status
          :decision decision
          :diagnostics (vec issues)
          :rejections (vec issues)
          :dependencies-authenticated? false
          :dependency-interface? false
          :dependencies? false}))

(defn- request-has-no-dependency-evidence?
  [request]
  (and (map? request)
       (or (empty? (keys request))
           (and (contains? request :pins)
                (contains? request :observations)
                (not (contains? request :artifact))
                (not (contains? request :schema-version))
                (not (contains? request :source-extension))))
       (or (not (map? (:pins request)))
           (not (map? (:observations request))))
       (not-any? some? (when (map? (:pins request))
                         (vals (:pins request))))
       (not-any? some? (when (map? (:observations request))
                         (vals (:observations request))))))

(defn- validate-request-shape
  [request]
  (cond-> []
    (not (map? request))
    (conj (issue :request-not-a-map []))

    (and (map? request)
         (not (exact-keys? request
                          #{:artifact :schema-version :pins
                            :observations :source-extension})))
    (conj (issue :request-keys-not-exact []))

    (and (map? request)
         (exact-keys? request
                      #{:artifact :schema-version :pins
                        :observations :source-extension})
         (not (same-identity? (:artifact request) request-artifact)))
    (conj (issue :request-artifact-mismatch [:artifact]))

    (and (map? request)
         (exact-keys? request
                      #{:artifact :schema-version :pins
                        :observations :source-extension})
         (not= (:schema-version request) request-schema))
    (conj (issue :request-schema-version-mismatch [:schema-version]))

    (and (map? request)
         (exact-keys? request
                      #{:artifact :schema-version :pins
                        :observations :source-extension})
         (not (contains? source-extensions (:source-extension request))))
    (conj (issue :unsupported-source-extension [:source-extension]))

    (and (map? request) (map? (:pins request))
         (not= (set (keys (:pins request))) (set producer-order)))
    (conj (issue :pin-workstream-keys-not-exact [:pins]))

    (and (map? request) (map? (:observations request))
         (not= (set (keys (:observations request))) (set producer-order)))
    (conj (issue :observation-workstream-keys-not-exact [:observations]))))

(defn validate-public-native-admission
  "Reject synthetic W1/W2/W3 v1 replay requests without performing I/O.

  The result is always a data map; malformed, missing, tampered, or
  cross-bound observations never throw and never authorize the public route.
  Dependency success is unreachable because every exact replay-owner policy
  is explicitly unfrozen for W1, W2, and W3.  The replay and checkout-root
  fields only exercise hostile negative validation; this namespace has no
  artifact reader, verifier callback, filesystem observer, or process hook.
  The public v1 entry point returns exactly one terminal issue: the first
  request-shape issue, otherwise the first frozen replay-structure issue,
  otherwise forged owner policy, otherwise the W1 unfrozen-owner blocker.
  It does not invoke the legacy handoff, binding, or cross-workstream
  validators after that terminal boundary.
  Producer handoffs contain implementation A/tree only; external pins and
  observations contain payload-containing B/tree identities, and no later C
  identity is embedded.  Defensive handoff, binding, and cross-link checks do
  not confer authority.  A future v2 request and a separate reviewed W4 route
  artifact are both required before user source or output I/O can be reached."
  [request]
  (cond
    (nil? request)
    default-public-native-admission

    :else
    (let [shape-issue (first (validate-request-shape request))
          pins (:pins request)
          observations (:observations request)
          replay-issue
          (when-not shape-issue
            (first-replay-structure-issue pins observations))
          terminal-issue
          (or shape-issue
              replay-issue
              (when-not (replay-policy-table-valid?)
                (issue :replay-owner-contract-forged-or-incomplete
                       [:replay-owner-policies]))
              (issue :w1-replay-contract-unfrozen
                     [:replay-owner-policies :w1]))]
      (decision-with-issues
       :rejected
       :dependency-interface-rejected
       [terminal-issue]))))

(defn public-native-admission?
  "Return true only for a separately admitted tracked public route.

  Negative-only v1 replay validation cannot authenticate dependencies and
  always returns false here.  This predicate is a convenience over the
  decision shape, not a second authority source."
  [request]
  (true? (:bounded-native-route-admitted?
          (if (map? request)
            (validate-public-native-admission request)
            default-public-native-admission))))

(defn verified-public-route-handoff?
  "Fail-closed v1 seam for the future reviewed W4 route artifact.

  The tracked-route schema and its replay evidence are not implemented yet, so
  no input can authenticate as a public route.  In particular this predicate
  never accepts premature public, seed-retirement, self-hosting, or release
  claims.  Its trailing question mark is retained as the future verifier API."
  [_route]
  false)
