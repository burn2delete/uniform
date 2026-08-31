(ns gravity.p15-public-native-admission.evidence-contract)

(def pin-keys
  #{:artifact-path :raw-content-hash
    :replay-artifact-path :replay-artifact-kind :replay-schema
    :replay-schema-version :replay-artifact-id :replay-raw-content-hash
    :checkout-root-id :checkout-root-commit :checkout-root-tree
    :payload-containing-commit :payload-containing-tree
    :implementation-commit :implementation-tree :artifact-id
    :interface-kind :interface-schema :verifier-predicate :predicate-version})

(def observation-keys
  #{:artifact-path :raw-content-hash
    :replay-artifact-path :replay-artifact-kind :replay-schema
    :replay-schema-version :replay-artifact-id :replay-raw-content-hash
    :checkout-root-id :checkout-root-commit :checkout-root-tree
    :payload-containing-commit :payload-containing-tree
    :consumer-handoff})

(def consumer-handoff-keys
  #{:contract :contract-version :workstream :interface-kind
    :interface-schema :artifact-id :producer-commit :producer-tree
    :verifier :review :bindings :claims})

(def verifier-keys
  #{:predicate :predicate-version :replay-artifact-id
    :replay-content-hash :status})

(def review-keys
  #{:status :reviewer-class :reviewed-commit :review-artifact-id})

(def claims-keys
  #{:public-route? :clojure-seed-boundary? :self-hosted? :release?})

(def w1-json-key-serialization
  {:final-outer-key "consumer-handoff"
   :development-candidate-outer-key "consumer_handoff_candidate"
   :nested-key-spelling :exact-kebab-case-ascii-string
   :parsed-key-mapping :remove-leading-edn-colon-only
   :snake-case-final-keys-admissible? false
   :unknown-or-extra-keys-admissible? false
   :duplicate-transformed-keys-admissible? false})

(def binding-key-sets
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

(def identity-binding-keys
  #{:method :descriptor-relative-execution? :fd-bound-launch-evidence-id
    :identity-stable-snapshot? :seatbelt-contained?})

(def os-gate-keys
  #{:target :tier :evidence-id})

(def process-tree-containment-keys
  #{:os-process-tree-containment? :method :evidence-id})

(def w1-provenance-edges-keys
  #{:artifact-kind :schema-version})

(def abi-keys
  #{:target :binary-format :architecture :calling-convention})

(def w2-provider-executable-path
  "target/phase-15/native-runtime/linux-x86_64/p15-s23-gravity-native-runtime-provider")

(def w2-packet-schema "gravity-native-runtime-v1")

(def w2-inherited-fds
  {:stdin 0 :stdout 1 :stderr 2})

(def w2-effects-keys
  #{:declared :inferred :required})

(def w2-capabilities-keys
  #{:declared :required})

(def w2-residual-authority
  {:clojure-seed-boundary? true
   :public-route? false
   :self-hosted? false
   :release? false})

(def w2-rejected-diagnostic-ids
  #{"P15GNR001" "P15GNR002" "P15GNR003" "P15GNR004"
    "P15NR001" "P15NR002" "P15NR003" "P15NR004" "P15NR005"
    "P15NR006" "P15NR007" "P15NR008" "P15NR009" "P15NR010"})

(def w3-receipt-schema
  "gravity.p15-linux-execveat-cgroup-contained-execution-receipt/v1")

(def w3-timeout-policy
  {:clock :clock-monotonic
   :minimum-ms 1
   :maximum-ms 600000
   :poll-ms 10
   :on-expiry :cgroup-kill})

(def w3-signal-policy
  {:handled [:sigint :sigterm :sighup]
   :handler :record-and-clean
   :cleanup :cgroup-kill
   :cleanup-wait-ms 250})

(def w3-output-policy
  {:stdout-max-bytes 65536
   :stderr-max-bytes 65536
   :retained-prefix-bytes 4096
   :forwarding :bounded-prefix
   :on-overflow :cgroup-kill})

(def w3-resource-policy
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

(def w3-cleanup-policy
  {:process-cleanup :cgroup-kill
   :empty-census :cgroup-procs-empty
   :cleanup-wait-ms 250
   :private-stage-cleanup :unlink-then-rmdir
   :receipt-path :absolute-new-o-excl-no-follow
   :no-survivors? true
   :no-stage-residue? true})

(def w3-unsupported-platforms
  {:targets [:darwin :darwin-arm64 :darwin-x86_64 :windows]
   :support :unsupported
   :fallback :none
   :public-fallback? false
   :diagnostic-id "P15CEA100"})

(def w3-negative-guarantees
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

(def w3-accepted-diagnostic-ids ["P15CEA000"])

(def w3-rejected-diagnostic-ids
  ["P15CEA100" "P15CEA101" "P15CEA102" "P15CEA103" "P15CEA104"
   "P15CEA105" "P15CEA106" "P15CEA107" "P15CEA108" "P15CEA109"
   "P15CEA110" "P15CEA111" "P15CEA112" "P15CEA113" "P15CEA114"
   "P15CEA115" "P15CEA116" "P15CEA117" "P15CEA118" "P15CEA119"
   "P15CEA120" "P15CEA121" "P15CEA199"])
