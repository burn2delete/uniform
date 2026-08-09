(ns gravity.self-hosting.p15-public-native-admission-test
  "Hostile pure-data admission coverage for the W4 public-native seam.

  The records below are synthetic and explicitly non-authoritative.  A
  matching-shaped W1/W2/W3 packet remains fail-closed while final W3 values
  and native replay evidence are pending; it cannot select the tracked public
  route or retire the Clojure seed boundary.
  This is a leaf under `gravity/self_hosting`, so the self-hosting runner
  discovers it without a runner edit.
  "
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.p15-public-native-admission :as admission]))

(def ^:private request-artifact
  :gravity/p15-public-native-admission-request)
(def ^:private request-schema
  "gravity.p15-public-native-admission-request/v1")
(def ^:private contract-artifact
  :gravity/p15-public-native-admission)
(def ^:private contract-schema
  "gravity.p15-public-native-admission/v1")
(def ^:private source-extension ".gravity")
(def ^:private implementation-commit
  "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
(def ^:private implementation-tree
  "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
(def ^:private payload-containing-commit
  "cccccccccccccccccccccccccccccccccccccccc")
(def ^:private payload-containing-tree
  "dddddddddddddddddddddddddddddddddddddddd")
(def ^:private w1-hash
  "sha256:1010101010101010101010101010101010101010101010101010101010101010")
(def ^:private w2-hash
  "sha256:2222222222222222222222222222222222222222222222222222222222222222")
(def ^:private w3-hash
  "sha256:3333333333333333333333333333333333333333333333333333333333333333")
(def ^:private w3-fd-bound-launch-evidence-id
  "sha256:7070707070707070707070707070707070707070707070707070707070707070")
(def ^:private w3-os-gate-evidence-id
  "sha256:7070707070707070707070707070707070707070707070707070707070707070")
(def ^:private w3-process-tree-evidence-id
  "sha256:7070707070707070707070707070707070707070707070707070707070707070")
(def ^:private w2-kind
  "gravity-authored-native-runtime-provider")
(def ^:private w1-kind
  "w1/executable-c13-c14-b1-llvm-x86_64-linux-backend")
(def ^:private w3-kind
  "linux-execveat-cgroup-contained-execution-authority")
(def ^:private w1-schema
  "gravity.w1.executable-carrier-interface/v1")
(def ^:private w2-schema
  "gravity/p15-s23-gravity-native-runtime-provider-interface-v1")
(def ^:private w3-schema
  "gravity.p15-linux-execveat-cgroup-contained-execution-authority/v1")
(def ^:private w1-placeholder-id
  "sha256:1111111111111111111111111111111111111111111111111111111111111111")
(def ^:private w1-id
  "sha256:1414141414141414141414141414141414141414141414141414141414141414")
(def ^:private w1-carrier-id
  "sha256:1818181818181818181818181818181818181818181818181818181818181818")
(def ^:private w1-carrier-hash
  "sha256:1919191919191919191919191919191919191919191919191919191919191919")
(def ^:private w2-id
  "sha256:2020202020202020202020202020202020202020202020202020202020202020")
(def ^:private w3-id
  "sha256:3030303030303030303030303030303030303030303030303030303030303030")
(def ^:private w3-executable-id
  "sha256:3131313131313131313131313131313131313131313131313131313131313131")
(def ^:private w2-runtime-manifest-id
  "sha256:5050505050505050505050505050505050505050505050505050505050505050")
(def ^:private w2-source-rule-id
  "sha256:5151515151515151515151515151515151515151515151515151515151515151")
(def ^:private w2-replay-id
  "sha256:6060606060606060606060606060606060606060606060606060606060606060")
(def ^:private w1-replay-id
  "sha256:1515151515151515151515151515151515151515151515151515151515151515")
(def ^:private w3-replay-id
  "sha256:7070707070707070707070707070707070707070707070707070707070707070")
(def ^:private w1-replay-raw-content-hash
  "sha256:4141414141414141414141414141414141414141414141414141414141414141")
(def ^:private w2-replay-raw-content-hash
  "sha256:4242424242424242424242424242424242424242424242424242424242424242")
(def ^:private w3-replay-raw-content-hash
  "sha256:4343434343434343434343434343434343434343434343434343434343434343")
(def ^:private w1-replay-artifact-path
  "target/p15-test/replay/w1-native-replay.edn")
(def ^:private w2-replay-artifact-path
  "target/p15-test/replay/w2-native-replay.edn")
(def ^:private w3-replay-artifact-path
  "target/p15-test/replay/w3-native-replay.edn")
(def ^:private w1-replay-artifact-kind :gravity/test-w1-native-replay)
(def ^:private w2-replay-artifact-kind :gravity/test-w2-native-replay)
(def ^:private w3-replay-artifact-kind :gravity/test-w3-native-replay)
(def ^:private w1-replay-schema
  "gravity.test/w1-native-replay/v1")
(def ^:private w2-replay-schema
  "gravity.test/w2-native-replay/v1")
(def ^:private w3-replay-schema
  "gravity.test/w3-native-replay/v1")
(def ^:private replay-schema-version 1)
(def ^:private checkout-root-id
  "sha256:49cd709bd5b532163d7fb6b57a15708f741d462e4a53b11e418dbc0941718e4b")
(def ^:private w2-review-id
  "sha256:9090909090909090909090909090909090909090909090909090909090909090")
(def ^:private w1-review-id
  "sha256:1616161616161616161616161616161616161616161616161616161616161616")
(def ^:private w3-review-id
  "sha256:abababababababababababababababababababababababababababababababab")
(def ^:private w2-no-clojure-evidence-id
  "sha256:1212121212121212121212121212121212121212121212121212121212121212")
(def ^:private w2-no-jvm-evidence-id
  "sha256:1313131313131313131313131313131313131313131313131313131313131313")
(def ^:private w1-path
  "docs/artifacts/workstreams/w1/w1-executable-carrier-interface.json")
(def ^:private w1-development-path
  "docs/artifacts/workstreams/w1/development/w1-executable-carrier-linux-amd64-emulated-evidence.json")
(def ^:private w3-candidate-path
  "docs/artifacts/phase-15/native-launcher/p15-s23-contained-execution-authority-candidate.edn")
(def ^:private w2-candidate-path
  "docs/artifacts/phase-15/native-runtime/p15-s23-gravity-native-runtime-provider-candidate.edn")
(def ^:private w2-path
  "docs/artifacts/phase-15/native-runtime/p15-s23-gravity-native-runtime-provider-interface.edn")
(def ^:private w3-path
  "docs/artifacts/phase-15/native-launcher/p15-s23-contained-execution-authority.edn")
(def ^:private w2-predicate
  "gravity.p15-gravity-native-runtime-provider/consumer-handoff-valid?")
(def ^:private w1-predicate
  "gravity.bootstrap/p15-s23-stage2-b3-llvm-verify!")
(def ^:private w3-predicate
  "gravity.p15-containment-authority/verify-consumer-handoff")
(def ^:private llvm-linux-target :llvm-x86_64-linux)
(def ^:private darwin-arm64-target :darwin-arm64)
(def ^:private w1-b3-artifact-kind
  :gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact)
(def ^:private w1-b3-schema-version 1)
(def ^:private w2-abi
  {:target llvm-linux-target
   :binary-format :elf
   :architecture :x86_64
   :calling-convention :sysv-amd64})

(def ^:private pin-keys
  #{:artifact-path :raw-content-hash :replay-artifact-path
    :replay-artifact-kind :replay-schema :replay-schema-version
    :replay-artifact-id
    :replay-raw-content-hash :checkout-root-id :checkout-root-commit
    :checkout-root-tree :payload-containing-commit :payload-containing-tree
    :implementation-commit :implementation-tree :artifact-id :interface-kind
    :interface-schema :verifier-predicate :predicate-version})

(def ^:private observation-keys
  #{:artifact-path :raw-content-hash :replay-artifact-id
    :replay-raw-content-hash :replay-artifact-path :replay-artifact-kind
    :replay-schema :replay-schema-version :checkout-root-id
    :checkout-root-commit :checkout-root-tree :payload-containing-commit
    :payload-containing-tree :consumer-handoff})

(def ^:private replay-pin-added-keys
  #{:replay-artifact-path :replay-artifact-kind :replay-schema
    :replay-schema-version :replay-artifact-id
    :replay-raw-content-hash :checkout-root-id :checkout-root-commit
    :checkout-root-tree})

(def ^:private replay-observation-added-keys replay-pin-added-keys)

(def ^:private replay-observation-fields
  [:replay-artifact-path :replay-artifact-kind :replay-schema
   :replay-schema-version :replay-artifact-id :replay-raw-content-hash
   :checkout-root-id :checkout-root-commit :checkout-root-tree])

(def ^:private replay-observation-pin-equality-keys
  #{:observation-replay-artifact-path
    :observation-replay-artifact-kind :observation-replay-schema
    :observation-replay-schema-version :observation-replay-artifact-id
    :observation-replay-raw-content-hash :observation-checkout-root-id
    :observation-checkout-root-commit :observation-checkout-root-tree})

(def ^:private future-request-v2-keys
  #{:schema :status :missing-contracts :request-field? :policy-field?
    :result-field? :artifact-field? :authority?})

(def ^:private future-request-v2-value
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

(def ^:private replay-owner-policy-keys
  #{:state :replay-contract-frozen? :missing-fields :claims})

(def ^:private replay-owner-claims
  {:public-route? false
   :clojure-seed-boundary? true
   :self-hosted? false
   :release? false})

(def ^:private replay-owner-missing-fields
  {:w1 #{:accepted-replay-artifact-path :accepted-replay-artifact-kind
         :accepted-replay-schema :accepted-replay-schema-version
         :non-jvm-semantic-verifier-predicate
         :non-jvm-semantic-verifier-predicate-version
         :semantic-verifier-implementation-kind
         :semantic-verifier-implementation-schema
         :semantic-verifier-implementation-schema-version
         :semantic-verifier-implementation-path
         :semantic-verifier-implementation-artifact-id-rule
         :semantic-verifier-implementation-raw-content-hash
         :semantic-verifier-reviewed-a-tree-blob-binding
         :semantic-verifier-call-abi :semantic-verifier-result-schema
         :semantic-verifier-nonrecursive-execution-evidence
         :semantic-verifier-independent-review
         :replay-payload-containing-b-tree-blob-binding
         :w6-registry-c-binding}
   :w2 #{:accepted-replay-artifact-path :accepted-replay-artifact-kind
         :accepted-replay-schema :accepted-replay-schema-version
         :non-jvm-semantic-verifier-predicate
         :non-jvm-semantic-verifier-predicate-version
         :semantic-verifier-implementation-kind
         :semantic-verifier-implementation-schema
         :semantic-verifier-implementation-schema-version
         :semantic-verifier-implementation-path
         :semantic-verifier-implementation-artifact-id-rule
         :semantic-verifier-implementation-raw-content-hash
         :semantic-verifier-reviewed-a-tree-blob-binding
         :semantic-verifier-call-abi :semantic-verifier-result-schema
         :semantic-verifier-nonrecursive-execution-evidence
         :semantic-verifier-independent-review
         :replay-payload-containing-b-tree-blob-binding
         :w6-registry-c-binding
         :accepted-tracked-native-fd0-wire-replay-contract
         :accepted-static-elf-no-pt-interp-provider-replay}
   :w3 #{:accepted-replay-artifact-path :accepted-replay-artifact-kind
         :accepted-replay-schema :accepted-replay-schema-version
         :non-jvm-semantic-verifier-predicate
         :non-jvm-semantic-verifier-predicate-version
         :semantic-verifier-implementation-kind
         :semantic-verifier-implementation-schema
         :semantic-verifier-implementation-schema-version
         :semantic-verifier-implementation-path
         :semantic-verifier-implementation-artifact-id-rule
         :semantic-verifier-implementation-raw-content-hash
         :semantic-verifier-reviewed-a-tree-blob-binding
         :semantic-verifier-call-abi :semantic-verifier-result-schema
         :semantic-verifier-nonrecursive-execution-evidence
         :semantic-verifier-independent-review
         :replay-payload-containing-b-tree-blob-binding
         :w6-registry-c-binding :accepted-native-replay-v2-schema
         :accepted-native-replay-v3-3-supplements
         :accepted-w2-native-fd0-wire-cross-binding
         :accepted-nonrecursive-w3-semantic-verifier}})

(def ^:private replay-diagnostic-order
  [:pin-keys-not-exact :pin-replay-artifact-path-invalid
   :pin-replay-artifact-kind-invalid :pin-replay-schema-invalid
   :pin-replay-schema-version-invalid :pin-replay-artifact-id-invalid
   :pin-replay-raw-content-hash-invalid :pin-checkout-root-id-invalid
   :pin-checkout-root-commit-invalid :pin-checkout-root-tree-invalid
   :pin-checkout-root-commit-does-not-match-payload-containing-commit
   :pin-checkout-root-tree-does-not-match-payload-containing-tree
   :pin-checkout-root-id-does-not-match-derived-b-identity
   :observation-keys-not-exact :observation-replay-artifact-path-invalid
   :observation-replay-artifact-kind-invalid :observation-replay-schema-invalid
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
   :w1-replay-contract-unfrozen :w2-replay-contract-unfrozen
   :w3-native-replay-schema-unfrozen])

(def ^:private replay-fixtures
  {:w1 {:replay-artifact-path w1-replay-artifact-path
        :replay-artifact-kind w1-replay-artifact-kind
        :replay-schema w1-replay-schema
        :replay-schema-version replay-schema-version
        :replay-artifact-id w1-replay-id
        :replay-raw-content-hash w1-replay-raw-content-hash}
   :w2 {:replay-artifact-path w2-replay-artifact-path
        :replay-artifact-kind w2-replay-artifact-kind
        :replay-schema w2-replay-schema
        :replay-schema-version replay-schema-version
        :replay-artifact-id w2-replay-id
        :replay-raw-content-hash w2-replay-raw-content-hash}
   :w3 {:replay-artifact-path w3-replay-artifact-path
        :replay-artifact-kind w3-replay-artifact-kind
        :replay-schema w3-replay-schema
        :replay-schema-version replay-schema-version
        :replay-artifact-id w3-replay-id
        :replay-raw-content-hash w3-replay-raw-content-hash}})

(defn- replay-field
  [workstream field]
  (get-in replay-fixtures [workstream field]))

(defn- replay-artifact-id-for
  [workstream]
  (case workstream
    :w1 w1-replay-id
    :w2 w2-replay-id
    :w3 w3-replay-id))

(defn- replay-raw-content-hash-for
  [workstream]
  (case workstream
    :w1 w1-replay-raw-content-hash
    :w2 w2-replay-raw-content-hash
    :w3 w3-replay-raw-content-hash))

(defn- pin
  [workstream artifact-path raw-content-hash artifact-id interface-kind
   interface-schema verifier-predicate]
  {:artifact-path artifact-path
   :raw-content-hash raw-content-hash
   :replay-artifact-path (replay-field workstream :replay-artifact-path)
   :replay-artifact-kind (replay-field workstream :replay-artifact-kind)
   :replay-schema (replay-field workstream :replay-schema)
   :replay-schema-version (replay-field workstream :replay-schema-version)
   :replay-artifact-id (replay-field workstream :replay-artifact-id)
   :replay-raw-content-hash (replay-field workstream :replay-raw-content-hash)
   :checkout-root-id checkout-root-id
   :checkout-root-commit payload-containing-commit
   :checkout-root-tree payload-containing-tree
   :payload-containing-commit payload-containing-commit
   :payload-containing-tree payload-containing-tree
   :implementation-commit implementation-commit
   :implementation-tree implementation-tree
   :artifact-id artifact-id
   :interface-kind interface-kind
   :interface-schema interface-schema
   :verifier-predicate verifier-predicate
   :predicate-version 1})

(defn- reviewed-handoff
  [workstream artifact-path raw-content-hash artifact-id interface-kind
   interface-schema verifier-predicate bindings]
  {:contract contract-artifact
   :contract-version 1
   :workstream workstream
   :interface-kind interface-kind
   :interface-schema interface-schema
   :artifact-id artifact-id
   :producer-commit implementation-commit
   :producer-tree implementation-tree
   :verifier {:predicate verifier-predicate
              :predicate-version 1
              :replay-artifact-id (replay-field workstream :replay-artifact-id)
              :replay-content-hash
              (replay-field workstream :replay-raw-content-hash)
              :status :passed}
   :review {:status :accepted
            :reviewer-class :independent-sol
            :reviewed-commit implementation-commit
            :review-artifact-id (case workstream
                                  :w1 w1-review-id
                                  :w2 w2-review-id
                                  w3-review-id)}
   :bindings bindings
   :claims {:public-route? false
            :self-hosted? false
            :release? false
            :clojure-seed-boundary? true}})

(def ^:private w1-bindings
  {:carrier-artifact-id w1-carrier-id
   :carrier-content-hash w1-carrier-hash
   :carrier-schema w1-b3-schema-version
   :source-id "gravity/p15-s23-b3-source"
   :semantic-id "gravity/p15-s23-b3-semantic-carrier"
   :profile "gravity-safe-native"
   :target llvm-linux-target
   :effects #{:compile/native :verify/executable}
   :capabilities #{:artifact/read :artifact/verify}
   :safety {:b3-authenticated? true
            :native-evidence-required? true}
   :accepted-diagnostic-ids #{"P15-B3-ACCEPTED"}
   :rejected-diagnostic-ids #{"P18T04002"}
   :provenance-edges {:artifact-kind w1-b3-artifact-kind
                      :schema-version w1-b3-schema-version}})

(def ^:private w2-bindings
  {:accepted-carrier-artifact-id w1-placeholder-id
   :accepted-carrier-content-hash w2-hash
   :provider-artifact-id w3-executable-id
   :provider-executable-path
   "target/phase-15/native-runtime/linux-x86_64/p15-s23-gravity-native-runtime-provider"
   :provider-executable-content-hash w3-hash
   :runtime-manifest-id w2-runtime-manifest-id
   :packet-schema "gravity-native-runtime-v1"
   :source-rule-id w2-source-rule-id
   :abi w2-abi
   :inherited-fds {:stdin 0 :stdout 1 :stderr 2}
   :effects {:declared #{:process/launch}
             :inferred #{:io/stdout}
             :required #{:process/launch}}
   :capabilities {:declared #{:process/contained}
                  :required #{:io/stdout}}
   :no-clojure-evidence-id w2-no-clojure-evidence-id
   :no-jvm-evidence-id w2-no-jvm-evidence-id
   :accepted-diagnostic-ids #{}
   :rejected-diagnostic-ids
   #{"P15GNR001" "P15GNR002" "P15GNR003" "P15GNR004"
     "P15NR001" "P15NR002" "P15NR003" "P15NR004" "P15NR005"
     "P15NR006" "P15NR007" "P15NR008" "P15NR009" "P15NR010"}
   :residual-authority {:clojure-seed-boundary? true
                        :public-route? false
                        :self-hosted? false
                        :release? false}})

(def ^:private w3-bindings
  {:admitted-executable-artifact-id w3-executable-id
   :admitted-executable-path
   "target/phase-15/native-runtime/linux-x86_64/p15-s23-gravity-native-runtime-provider"
   :admitted-executable-content-hash w3-hash
   :identity-binding-method
   {:method "linux-execveat-at-empty-path"
    :descriptor-relative-execution? true
    :fd-bound-launch-evidence-id w3-fd-bound-launch-evidence-id
    :identity-stable-snapshot? true
    :seatbelt-contained? false}
   :os-gate {:target llvm-linux-target
             :tier :supported
             :evidence-id w3-os-gate-evidence-id}
   :process-tree-containment {:os-process-tree-containment? true
                              :method "linux-cgroup-v2-clone-into-cgroup-v1"
                              :evidence-id w3-process-tree-evidence-id}
   :receipt-schema "gravity.p15-linux-execveat-cgroup-contained-execution-receipt/v1"
   :timeout-policy {:clock :clock-monotonic
                    :minimum-ms 1
                    :maximum-ms 600000
                    :poll-ms 10
                    :on-expiry :cgroup-kill}
   :signal-policy {:handled [:sigint :sigterm :sighup]
                   :handler :record-and-clean
                   :cleanup :cgroup-kill
                   :cleanup-wait-ms 250}
   :output-policy {:stdout-max-bytes 65536
                   :stderr-max-bytes 65536
                   :retained-prefix-bytes 4096
                   :forwarding :bounded-prefix
                   :on-overflow :cgroup-kill}
   :resource-policy {:pids-max 1
                     :memory-max-bytes 1073741824
                     :cpu-max {:quota-us 100000 :period-us 100000}
                     :rlimit-cpu-seconds 600
                     :rlimit-address-space-bytes 1073741824
                     :rlimit-open-files 64
                     :rlimit-file-size-bytes 16777216
                     :rlimit-core-bytes 0
                     :run-uid :explicit-nonzero-dedicated
                     :run-gid :explicit-nonzero-dedicated}
   :cleanup-policy {:process-cleanup :cgroup-kill
                    :empty-census :cgroup-procs-empty
                    :cleanup-wait-ms 250
                    :private-stage-cleanup :unlink-then-rmdir
                    :receipt-path :absolute-new-o-excl-no-follow
                    :no-survivors? true
                    :no-stage-residue? true}
   :negative-guarantees {:rename-replacement-blocked? true
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
                         :fallback-used? false}
   :unsupported-platforms {:targets [:darwin :darwin-arm64 :darwin-x86_64 :windows]
                           :support :unsupported
                           :fallback :none
                           :public-fallback? false
                           :diagnostic-id "P15CEA100"}
   :accepted-diagnostic-ids ["P15CEA000"]
   :rejected-diagnostic-ids
   ["P15CEA100" "P15CEA101" "P15CEA102" "P15CEA103" "P15CEA104"
    "P15CEA105" "P15CEA106" "P15CEA107" "P15CEA108" "P15CEA109"
    "P15CEA110" "P15CEA111" "P15CEA112" "P15CEA113" "P15CEA114"
    "P15CEA115" "P15CEA116" "P15CEA117" "P15CEA118" "P15CEA119"
    "P15CEA120" "P15CEA121" "P15CEA199"]})

(def ^:private w3-binding-keys
  #{:admitted-executable-artifact-id :admitted-executable-path
    :admitted-executable-content-hash :identity-binding-method :os-gate
    :process-tree-containment :receipt-schema :timeout-policy :signal-policy
    :output-policy :resource-policy :cleanup-policy :negative-guarantees
    :unsupported-platforms :accepted-diagnostic-ids
    :rejected-diagnostic-ids})

(def ^:private w2-binding-keys
  #{:accepted-carrier-artifact-id :accepted-carrier-content-hash
    :provider-artifact-id :provider-executable-path
    :provider-executable-content-hash :runtime-manifest-id :packet-schema
    :source-rule-id :abi :inherited-fds :effects :capabilities
    :no-clojure-evidence-id :no-jvm-evidence-id :accepted-diagnostic-ids
    :rejected-diagnostic-ids :residual-authority})

(def ^:private w1-binding-keys
  #{:carrier-artifact-id :carrier-content-hash :carrier-schema :source-id
    :semantic-id :profile :target :effects :capabilities :safety
    :accepted-diagnostic-ids :rejected-diagnostic-ids :provenance-edges})

(defn- observation
  [artifact-path raw-content-hash workstream artifact-id interface-kind
   interface-schema verifier-predicate bindings]
  {:artifact-path artifact-path
   :raw-content-hash raw-content-hash
   :replay-artifact-path (replay-field workstream :replay-artifact-path)
   :replay-artifact-kind (replay-field workstream :replay-artifact-kind)
   :replay-schema (replay-field workstream :replay-schema)
   :replay-schema-version (replay-field workstream :replay-schema-version)
   :replay-artifact-id (replay-field workstream :replay-artifact-id)
   :replay-raw-content-hash (replay-field workstream :replay-raw-content-hash)
   :checkout-root-id checkout-root-id
   :checkout-root-commit payload-containing-commit
   :checkout-root-tree payload-containing-tree
   :payload-containing-commit payload-containing-commit
   :payload-containing-tree payload-containing-tree
   :consumer-handoff
   (reviewed-handoff workstream artifact-path raw-content-hash artifact-id
                     interface-kind interface-schema verifier-predicate
                     bindings)})

(defn- observation-without-final-consumer-handoff
  [artifact-path raw-content-hash]
  {:artifact-path artifact-path
   :raw-content-hash raw-content-hash
   :replay-artifact-path (replay-field :w1 :replay-artifact-path)
   :replay-artifact-kind (replay-field :w1 :replay-artifact-kind)
   :replay-schema (replay-field :w1 :replay-schema)
   :replay-schema-version (replay-field :w1 :replay-schema-version)
   :replay-artifact-id (replay-field :w1 :replay-artifact-id)
   :replay-raw-content-hash (replay-field :w1 :replay-raw-content-hash)
   :checkout-root-id checkout-root-id
   :checkout-root-commit payload-containing-commit
   :checkout-root-tree payload-containing-tree
   :payload-containing-commit payload-containing-commit
   :payload-containing-tree payload-containing-tree})

(defn- w1-final-consumer-handoff
  []
  (reviewed-handoff :w1 w1-path w1-hash w1-id w1-kind w1-schema
                    w1-predicate w1-bindings))

(defn- valid-request
  "Build an exact W1 pin and outer observation whose final handoff is missing,
  alongside Linux-shaped W2/W3 evidence.  This is deliberately non-authoritative:
  the W1 final handoff must be authenticated before any dependency decision can
  proceed, and W2 keeps a pending/nonmatching carrier pin.
  "
  []
  {:artifact request-artifact
   :schema-version request-schema
   :pins
   {:w1 (pin :w1 w1-path w1-hash w1-id w1-kind w1-schema w1-predicate)
    :w2 (pin :w2 w2-path w2-hash w2-id w2-kind w2-schema w2-predicate)
    :w3 (pin :w3 w3-path w3-hash w3-id w3-kind w3-schema w3-predicate)}
   :observations
   {:w1 (observation-without-final-consumer-handoff w1-path w1-hash)
    :w2 (observation w2-path w2-hash :w2 w2-id w2-kind w2-schema
                     w2-predicate w2-bindings)
    :w3 (observation w3-path w3-hash :w3 w3-id w3-kind w3-schema
                     w3-predicate w3-bindings)}
   :source-extension source-extension})

(defn- with-w1-final-consumer-handoff
  [request handoff]
  (assoc-in request [:observations :w1 :consumer-handoff] handoff))

(defn- w1-final-shaped-request
  []
  (with-w1-final-consumer-handoff (valid-request)
                                  (w1-final-consumer-handoff)))

(defn- w1-development-candidate-request
  []
  (-> (valid-request)
      (assoc-in [:pins :w1 :artifact-path] w1-development-path)
      (assoc-in [:observations :w1 :artifact-path] w1-development-path)
      (assoc-in [:observations :w1 :consumer_handoff_candidate]
                {:artifact-kind w1-b3-artifact-kind
                 :schema-version w1-b3-schema-version
                 :status :candidate})))

(defn- w3-candidate-request
  []
  (-> (valid-request)
      (assoc-in [:pins :w3 :artifact-path] w3-candidate-path)
      (assoc-in [:observations :w3 :artifact-path] w3-candidate-path)
      (update-in [:observations :w3] dissoc :consumer-handoff)
      (assoc-in [:observations :w3 :consumer-handoff-candidate]
                {:artifact-kind :gravity/p15-s23-contained-execution-authority
                 :schema-version 1
                 :status :candidate})))

(defn- w2-candidate-request
  []
  (-> (valid-request)
      (assoc-in [:pins :w2 :artifact-path] w2-candidate-path)
      (assoc-in [:observations :w2 :artifact-path] w2-candidate-path)
      (update-in [:observations :w2] dissoc :consumer-handoff)
      (assoc-in [:observations :w2 :consumer-handoff-candidate]
                {:artifact-kind :gravity/p15-s23-gravity-native-runtime-provider
                 :schema-version 1
                 :status :candidate})))

(defn- decision
  [request]
  (admission/validate-public-native-admission request))

(defn- with-replay-policies
  [policies thunk]
  (if-let [policy-var (ns-resolve 'gravity.p15-public-native-admission
                                  'replay-policies)]
    (with-redefs-fn {policy-var policies} thunk)
    (thunk)))

(defn- mixed-identifier-request
  "Use the permitted JSON-friendly identifier spellings.

  Contract, workstream, interface-kind, and verifier-predicate identifiers may
  be keywords, symbols, or strings when their canonical text is identical.
  Hashes, paths, commit/tree identities, booleans, and schemas remain strict.
  "
  [request]
  (-> request
      (assoc-in [:observations :w2 :consumer-handoff :contract]
                (symbol "gravity/p15-public-native-admission"))
      (assoc-in [:observations :w3 :consumer-handoff :contract]
                "gravity/p15-public-native-admission")
      (assoc-in [:observations :w2 :consumer-handoff :workstream]
                (symbol "w2"))
      (assoc-in [:observations :w3 :consumer-handoff :workstream] "w3")
      (assoc-in [:pins :w2 :interface-kind]
                (symbol "gravity-authored-native-runtime-provider"))
      (assoc-in [:pins :w3 :interface-kind]
                "linux-execveat-cgroup-contained-execution-authority")
      (assoc-in [:pins :w2 :verifier-predicate]
                (symbol "gravity.p15-gravity-native-runtime-provider/consumer-handoff-valid?"))
      (assoc-in [:pins :w3 :verifier-predicate]
                "gravity.p15-containment-authority/verify-consumer-handoff")
      (assoc-in [:observations :w2 :consumer-handoff :interface-kind]
                "gravity-authored-native-runtime-provider")
      (assoc-in [:observations :w3 :consumer-handoff :interface-kind]
                (symbol "linux-execveat-cgroup-contained-execution-authority"))
      (assoc-in [:observations :w2 :consumer-handoff :verifier :predicate]
                "gravity.p15-gravity-native-runtime-provider/consumer-handoff-valid?")
      (assoc-in [:observations :w3 :consumer-handoff :verifier :predicate]
                (symbol "gravity.p15-containment-authority/verify-consumer-handoff"))))

(defn- assert-rejected
  [request]
  (let [result (decision request)]
    (is (= "P18T04002" (or (:id result) (:diagnostic result))) result)
    (is (false? (:io-authorized? result)) result)
    (is (false? (:public-route? result)) result)
    (is (true? (:clojure-seed-boundary? result)) result)
    (is (false? (:self-hosted? result)) result)
    (is (false? (:release? result)) result)
    (is (= 1 (count (:diagnostics result))) result)
    (is (= 1 (count (:rejections result))) result)
    (is (= (:diagnostics result) (:rejections result)) result)
    result))

(defn- assert-rejected-codes
  [request expected-codes]
  (let [expected-codes (vec expected-codes)
        result (assert-rejected request)
        actual-codes (vec (keep :code (:rejections result)))]
    (is (= 1 (count expected-codes))
        {:expected-codes expected-codes
         :result result})
    (is (= expected-codes actual-codes)
        {:expected-codes expected-codes
         :actual-codes actual-codes
         :result result})
    result))

(defn- assert-legacy-unreachable
  "Legacy handoff/binding validators are private and unreachable from v1.

  Keep the hostile mutation named by its legacy diagnostic, while requiring
  the public entrypoint to expose only the terminal W1 replay blocker."
  [request legacy-codes]
  (let [legacy-codes (vec legacy-codes)
        result (assert-rejected-codes request [:w1-replay-contract-unfrozen])]
    (doseq [legacy-code legacy-codes]
      (is (not-any? #(= legacy-code (:code %)) (:rejections result))
          {:legacy-code legacy-code :result result}))
    result))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/p15_public_native_admission_test.clj")]
    (when-not resource
      (throw (ex-info "P15 public-native admission test source is not on the classpath"
                      {:id "P15-ADMISSION-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "repository root not found"
                        {:id "P15-ADMISSION-REPOSITORY-ROOT"}))
        (and (.isFile (.toFile (.resolve candidate "deps.edn")))
             (.isDirectory (.toFile (.resolve candidate "contracts"))))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- contract-file
  []
  (let [path (.resolve @root "contracts/p15-public-native-admission-v1.edn")]
    (when-not (.isFile (.toFile path))
      (throw (ex-info "P15 public-native admission contract is missing"
                      {:id "P15-ADMISSION-CONTRACT-MISSING"
                       :path (str path)})))
    (edn/read-string (slurp path))))

(defn- value-or-call
  [value]
  (if (fn? value) (value) value))

(deftest public-native-admission-contract-is-incomplete-and-keeps-recovery-separate
  (let [contract (value-or-call admission/public-native-admission-contract)
        default (value-or-call admission/default-public-native-admission)
        file-contract (contract-file)
        workstreams (set (or (:dependencies contract)
                             (get-in contract [:pins :workstreams])
                             (keys (:required-producers contract))))]
    (is (= request-artifact (:artifact contract)) contract)
    (is (= request-schema (:schema-version contract)) contract)
    (is (= pin-keys (set (get-in contract [:pins :keys]))) contract)
    (is (= observation-keys
           (set (get-in contract [:observations :keys]))) contract)
    (is (= replay-observation-added-keys
           (set (get-in contract [:observations :replay-fields]))) contract)
    (is (= replay-observation-fields
           (get-in contract [:observations :replay-fields])) contract)
    (is (= replay-observation-added-keys
           (set (get-in contract [:pins :replay-observation :fields])))
        contract)
    (is (= replay-observation-fields
           (get-in contract [:pins :replay-observation :fields]))
        contract)
    (is (true? (get-in contract [:pins :replay-observation
                                 :synthetic-hostile-only?])) contract)
    (is (false? (get-in contract [:pins :replay-observation
                                  :replay-io-performed?])) contract)
    (is (false? (get-in contract [:pins :replay-observation
                                  :producer-raw-content-hash-equality-required?]))
        contract)
    (is (= replay-owner-policy-keys
           (set (:replay-owner-policy-exact-keys contract))) contract)
    (is (= replay-diagnostic-order
           (:replay-diagnostic-precedence contract)) contract)
    (is (= future-request-v2-keys
           (set (keys (:future-request-v2 contract)))) contract)
    (is (= future-request-v2-value (:future-request-v2 contract)) contract)
    (doseq [workstream [:w1 :w2 :w3]]
      (let [policy (get-in contract [:replay-owner-policies workstream])]
        (is (= replay-owner-policy-keys (set (keys policy))) policy)
        (is (= :unfrozen (:state policy)) policy)
        (is (false? (:replay-contract-frozen? policy)) policy)
        (is (= (get replay-owner-missing-fields workstream)
               (:missing-fields policy)) policy)
        (is (= replay-owner-claims (:claims policy)) policy)))
    (is (= contract-artifact (get-in contract [:decision :artifact])) contract)
    (is (= contract-schema (get-in contract [:decision :schema-version])) contract)
    (is (= :negative-only-v1 (get-in contract [:decision :status])) contract)
    (is (= :unreachable (get-in contract [:decision :success])) contract)
    (is (false? (get-in contract [:decision :replay-io?])) contract)
    (is (= {:diagnostics 1 :rejections 1}
           (get-in contract [:decision :public-result-cardinality])) contract)
    (is (= :missing-reviewed-w1-w2-w3-observations
           (get-in contract [:decision :nil-request-terminal])) contract)
    (is (true? (get-in contract [:decision
                                 :public-predicate-nonmap-uses-default?]))
        contract)
    (is (= :first-existing-shape-issue
           (get-in contract [:decision :request-shape-precedence])) contract)
    (is (= :first-match-exact-replay-diagnostic-order
           (get-in contract [:decision :replay-structure-precedence])) contract)
    (is (= :w1-replay-contract-unfrozen
           (get-in contract [:decision :valid-unfrozen-owner-terminal])) contract)
    (is (false? (get-in contract [:decision :later-owner-blockers-emitted?]))
        contract)
    (is (false? (get-in contract [:decision :legacy-validation-after-terminal?]))
        contract)
    (is (false? (get-in contract [:decision :legacy-diagnostics-exposed?]))
        contract)
    (is (or (= :incomplete (:status default))
            (false? (:complete? default))) default)
    (is (= 1 (count (:diagnostics default))) default)
    (is (= 1 (count (:rejections default))) default)
    (is (= (:diagnostics default) (:rejections default)) default)
    (is (false? (:public-route? default)) default)
    (is (false? (:self-hosted? default)) default)
    (is (false? (:release? default)) default)
    (is (true? (:clojure-seed-boundary? default)) default)
    (is (contains? workstreams :w1) contract)
    (is (contains? workstreams :w2) contract)
    (is (contains? workstreams :w3) contract)
    (is (or (= "bin/gravity-bootstrap" (:recovery-path contract))
            (= "bin/gravity-bootstrap" (get-in contract [:recovery :path]))
            (= "bin/gravity-bootstrap" (get-in contract [:recovery :command]))
            (= "bin/gravity-bootstrap" (:recovery-path default))
            (= "bin/gravity-bootstrap" (get-in default [:recovery :path]))
            (= "bin/gravity-bootstrap" (get-in file-contract [:recovery :command])))
        {:contract contract :default default})
    (is (or (= request-artifact (:artifact file-contract))
            (= contract-artifact (:artifact file-contract))) file-contract)
    (is (or (= request-schema (:schema-version file-contract))
            (= contract-schema (:schema-version file-contract))) file-contract)
    (is (= pin-keys (set (get-in file-contract [:exact-key-sets :pin])))
        file-contract)
    (is (= observation-keys
           (set (get-in file-contract [:exact-key-sets :observation])))
        file-contract)
    (is (= replay-pin-added-keys
           (set (keys (get-in file-contract
                              [:replay-binding :field-contract]))))
        file-contract)
    (is (= replay-observation-pin-equality-keys
           (set (keys (get-in file-contract
                              [:replay-binding
                               :observation-pin-equalities]))))
        file-contract)
    (is (= #{:pin-checkout-root-commit :pin-checkout-root-tree}
           (set (keys (get-in file-contract
                              [:replay-binding :lineage-relations]))))
        file-contract)
    (is (= {:encoding :ascii
            :formula [:sha256-lowercase-64hex
                      [:concat-ascii
                       "gravity-w4-checkout-root-v1" 0
                       [:pins 'ws :payload-containing-commit] 0
                       [:pins 'ws :payload-containing-tree]]]}
           (get-in file-contract [:replay-binding :checkout-root-id]))
        file-contract)
    (is (= replay-owner-policy-keys
           (set (get-in file-contract
                        [:public-native-admission-contract
                         :replay-owner-policy-exact-keys]))) file-contract)
    (is (= replay-diagnostic-order
           (get-in file-contract
                   [:public-native-admission-contract
                    :replay-diagnostic-precedence])) file-contract)
    (is (= {:replay-owner-contract-forged-or-incomplete
            :before-applicable-owner-blocker
            :public-result-cardinality {:diagnostics 1 :rejections 1}
            :nil-request-terminal :missing-reviewed-w1-w2-w3-observations
            :public-predicate-nonmap-uses-default? true
            :request-shape-precedence :first-existing-shape-issue
            :replay-structure-precedence :first-match-exact-replay-diagnostic-order
            :valid-unfrozen-owner-terminal :w1-replay-contract-unfrozen
            :later-owner-blockers-emitted? false
            :legacy-validation-after-terminal? false
            :legacy-diagnostics-exposed? false}
           (get-in file-contract
                   [:public-native-admission-contract
                    :replay-diagnostic-semantics]))
        file-contract)
    (is (= future-request-v2-keys
           (set (keys (get-in file-contract
                              [:public-native-admission-contract
                               :future-request-v2])))) file-contract)
    (is (= future-request-v2-value
           (get-in file-contract
                   [:public-native-admission-contract :future-request-v2]))
        file-contract)
    (is (= :w1-replay-contract-unfrozen
           (get-in file-contract [:current-results :reason]))
        file-contract)
    (is (= (:future-request-v2 contract)
           (get-in file-contract
                  [:public-native-admission-contract :future-request-v2]))
        file-contract)
    (doseq [workstream [:w1 :w2 :w3]]
      (let [policy (get-in file-contract
                           [:public-native-admission-contract
                            :replay-owner-policies workstream])]
        (is (= replay-owner-policy-keys (set (keys policy))) policy)
        (is (= (get replay-owner-missing-fields workstream)
               (:missing-fields policy)) policy)
        (is (= replay-owner-claims (:claims policy)) policy)))
    (is (some #{:w1 :w2 :w3}
              (set (or (:dependencies file-contract)
                       (get-in file-contract [:pins :workstreams])
                       (keys (:required-producers file-contract)))))
        file-contract)))

(deftest terminal-entrypoint-cardinality-and-shape-precedence
  (testing "nil uses the exact fail-closed default terminal"
    (let [result (assert-rejected-codes nil
                                        [:missing-reviewed-w1-w2-w3-observations])]
      (is (= (value-or-call admission/default-public-native-admission)
             result))
      (is (false? (admission/public-native-admission? nil)))))
  (testing "non-map values are rejected by the first request-shape issue"
    (doseq [request [true :not-a-map 42]]
      (assert-rejected-codes request [:request-not-a-map])
      (is (false? (admission/public-native-admission? request)) request)))
  (testing "a nonnil empty request is not a no-evidence shortcut"
    (assert-rejected-codes {} [:request-keys-not-exact])
    (is (false? (admission/public-native-admission? {}))))
  (testing "a partial envelope is rejected by shape before replay or owner checks"
    (assert-rejected-codes {:pins {} :observations {}}
                           [:request-keys-not-exact])
    (assert-rejected-codes
     {:artifact request-artifact
      :schema-version request-schema
      :pins {}
      :observations {}
      :source-extension source-extension}
     [:pin-workstream-keys-not-exact])
    (is (false? (admission/public-native-admission?
                 {:pins {} :observations {}})))))

(deftest request-and-pin-shapes-are-exact
  (let [request (valid-request)]
    (is (= #{:artifact :schema-version :pins :observations :source-extension}
           (set (keys request))))
    (is (= pin-keys (set (keys (get-in request [:pins :w1])))))
    (is (= observation-keys
           (set (keys (get-in request [:observations :w1])))))
    (doseq [workstream [:w1 :w2 :w3]]
      (is (= pin-keys (set (keys (get-in request [:pins workstream]))))
          workstream)
      (when (not= :w1 workstream)
        (is (= observation-keys
               (set (keys (get-in request [:observations workstream]))))
            workstream)))))

(deftest replay-owner-policies-are-exact-and-fail-closed
  (let [contract (value-or-call admission/public-native-admission-contract)
        policies (:replay-owner-policies contract)
        malformed
        [["owner policy table is not a map" true]
         ["owner policy workstream missing" (dissoc policies :w2)]
         ["owner policy workstream extra" (assoc policies :extra {})]
         ["owner policy state forged"
          (assoc-in policies [:w1 :state] :frozen)]
         ["owner policy key missing"
          (update-in policies [:w1] dissoc :missing-fields)]
         ["owner policy key extra"
          (assoc-in policies [:w1 :unexpected] true)]
         ["owner policy claims forged"
          (assoc-in policies [:w1 :claims :public-route?] true)]]]
    (doseq [[label forged] malformed]
      (testing label
        (with-replay-policies forged
          #(assert-rejected-codes
            (w1-final-shaped-request)
            [:replay-owner-contract-forged-or-incomplete]))))
    (testing "forged owner policy precedes its owner blocker"
      (with-replay-policies (assoc-in policies [:w1 :state] :frozen)
        #(assert-rejected-codes
          (w1-final-shaped-request)
          [:replay-owner-contract-forged-or-incomplete])))
    (testing "exact owner policies remain negative-only"
      (with-replay-policies policies
        #(assert-rejected-codes
          (w1-final-shaped-request)
          [:w1-replay-contract-unfrozen])))))

(deftest later-forged-owner-policy-cannot-be-masked-by-w1-blocker
  (let [contract (value-or-call admission/public-native-admission-contract)
        policies (:replay-owner-policies contract)
        mutations
        [["W2 state forged"
          (assoc-in policies [:w2 :state] :frozen)]
         ["W2 claims forged"
          (assoc-in policies [:w2 :claims :public-route?] true)]
         ["W3 state forged"
          (assoc-in policies [:w3 :state] :frozen)]
         ["W3 claims forged"
          (assoc-in policies [:w3 :claims :release?] true)]]]
    (doseq [[label forged] mutations]
      (testing label
        (with-replay-policies forged
          #(assert-rejected-codes
            (w1-final-shaped-request)
            [:replay-owner-contract-forged-or-incomplete]))))))

(deftest replay-metadata-terminal-result-is-rank-first-and-legacy-free
  (let [contract (value-or-call admission/public-native-admission-contract)
        policies (:replay-owner-policies contract)
        request
        (-> (w1-final-shaped-request)
            ;; W3 is discovered after W2, but this lower-rank mismatch must
            ;; sort before W2's higher-rank replay mismatch.
            (assoc-in [:observations :w3 :replay-artifact-path]
                      w2-replay-artifact-path)
            (assoc-in [:observations :w2 :replay-raw-content-hash]
                      w3-replay-raw-content-hash)
            (assoc-in [:observations :w2 :consumer-handoff :verifier
                       :replay-content-hash]
                      w3-replay-raw-content-hash)
            ;; These are legacy W1 pin, handoff, and cross-workstream faults.
            (assoc-in [:pins :w1 :implementation-tree]
                      "tree-implementation-tampered")
            (assoc-in [:observations :w1 :consumer-handoff :review :status]
                      :pending)
            (assoc-in [:observations :w2 :consumer-handoff :bindings
                       :accepted-carrier-artifact-id]
                      w1-placeholder-id))
        expected-replay-code
        :observation-replay-artifact-path-does-not-match-pin]
    (is (= #{:artifact :schema-version :pins :observations :source-extension}
           (set (keys request))) request)
    (is (= #{:w1 :w2 :w3} (set (keys (:pins request)))) request)
    (is (= #{:w1 :w2 :w3} (set (keys (:observations request)))) request)
    (doseq [workstream [:w1 :w2 :w3]]
      (is (= pin-keys (set (keys (get-in request [:pins workstream]))))
          [workstream :pin])
      (is (= observation-keys
             (set (keys (get-in request [:observations workstream]))))
          [workstream :observation])
      (is (= replay-owner-policy-keys
             (set (keys (get policies workstream))))
          [workstream :policy])
      (is (= :unfrozen (:state (get policies workstream)))
          [workstream :policy-state])
      (is (false? (:replay-contract-frozen? (get policies workstream)))
          [workstream :policy-frozen])
      (is (= (get replay-owner-missing-fields workstream)
             (:missing-fields (get policies workstream)))
          [workstream :policy-missing-fields])
      (is (= replay-owner-claims (:claims (get policies workstream)))
          [workstream :policy-claims]))
    (let [result
          (with-replay-policies (assoc-in policies [:w3 :state] :frozen)
            #(assert-rejected-codes request [expected-replay-code]))]
      (doseq [forbidden-code [:pin-implementation-tree-invalid
                              :review-not-independent-or-complete
                              :w1-to-w2-carrier-artifact-cross-binding-mismatch
                              :replay-owner-contract-forged-or-incomplete
                              :w1-replay-contract-unfrozen
                              :w2-replay-contract-unfrozen
                              :w3-native-replay-schema-unfrozen]]
        (is (not-any? #(= forbidden-code (:code %)) (:rejections result))
            {:forbidden-code forbidden-code :result result})))
    (with-replay-policies policies
      #(assert-rejected-codes
        (w1-final-shaped-request)
        [:w1-replay-contract-unfrozen]))))

(deftest replay-v1-is-zero-action-and-authority-free
  (let [contract (value-or-call admission/public-native-admission-contract)
        replay-observation (get-in contract [:pins :replay-observation])
        namespace-var (ns-resolve 'gravity.p15-public-native-admission
                                  'namespace-contract)
        namespace-contract (when namespace-var @namespace-var)
        forbidden (set (get-in namespace-contract
                                [:dependency-direction :forbids]))
        non-owned (set (get-in namespace-contract [:ownership :does-not-own]))]
    (is (true? (:synthetic-hostile-only? replay-observation)) contract)
    (is (false? (:replay-io-performed? replay-observation)) contract)
    (is (true? (:semantic-and-raw-roles-distinct? replay-observation)) contract)
    (is (false? (:producer-raw-content-hash-equality-required?
                 replay-observation)) contract)
    (is (false? (:semantic-and-raw-value-inequality-required?
                 replay-observation)) contract)
    (is (true? (:verifier-binding-defensive-only? replay-observation))
        contract)
    (doseq [forbidden-symbol ['gravity.bootstrap
                              'gravity.diagnostics 'java.io 'java.nio
                              'java.lang.ProcessBuilder]]
      (is (contains? forbidden forbidden-symbol)
          {:forbidden-symbol forbidden-symbol
           :forbids forbidden
           :namespace-contract namespace-contract}))
    (doseq [non-owned-capability [:artifact-reads :raw-content-hash-computation
                                  :replay-semantic-id-computation
                                  :path-admission :process-launch
                                  :public-route-selection :seed-retirement
                                  :self-hosting :release-claims]]
      (is (contains? non-owned non-owned-capability)
          {:non-owned-capability non-owned-capability
           :does-not-own non-owned
           :namespace-contract namespace-contract}))))

(deftest replay-and-checkout-fields-are-type-valid-and-role-bound
  (let [request (valid-request)]
    (doseq [workstream [:w1 :w2 :w3]]
      (let [pin (get-in request [:pins workstream])
            observation (get-in request [:observations workstream])]
        (is (re-matches #"target/[A-Za-z0-9._/-]+"
                        (:replay-artifact-path pin)) pin)
        (is (keyword? (:replay-artifact-kind pin)) pin)
        (is (and (string? (:replay-schema pin))
                 (not (str/blank? (:replay-schema pin)))) pin)
        (is (pos-int? (:replay-schema-version pin)) pin)
        (doseq [field [:replay-artifact-id :replay-raw-content-hash
                       :checkout-root-id]]
          (is (re-matches #"sha256:[0-9a-f]{64}" (get pin field))
              [workstream field pin]))
        (is (re-matches #"[0-9a-f]{40}" (:checkout-root-commit pin)) pin)
        (is (re-matches #"[0-9a-f]{40}" (:checkout-root-tree pin)) pin)
        (is (= checkout-root-id (:checkout-root-id pin)) pin)
        (is (= payload-containing-commit (:checkout-root-commit pin)) pin)
        (is (= payload-containing-tree (:checkout-root-tree pin)) pin)
        (doseq [field replay-observation-added-keys]
          (is (= (get pin field) (get observation field))
              [workstream :replay-field field]))))))

(deftest replay-and-checkout-keysets-and-types-fail-closed
  (let [fields-and-codes
        [[:replay-artifact-path :pin-replay-artifact-path-invalid
          :observation-replay-artifact-path-invalid]
         [:replay-artifact-kind :pin-replay-artifact-kind-invalid
          :observation-replay-artifact-kind-invalid]
         [:replay-schema :pin-replay-schema-invalid
          :observation-replay-schema-invalid]
         [:replay-schema-version :pin-replay-schema-version-invalid
          :observation-replay-schema-version-invalid]
         [:replay-artifact-id :pin-replay-artifact-id-invalid
          :observation-replay-artifact-id-invalid]
         [:replay-raw-content-hash :pin-replay-raw-content-hash-invalid
          :observation-replay-raw-content-hash-invalid]
         [:checkout-root-id :pin-checkout-root-id-invalid
          :observation-checkout-root-id-invalid]
         [:checkout-root-commit :pin-checkout-root-commit-invalid
          :observation-checkout-root-commit-invalid]
         [:checkout-root-tree :pin-checkout-root-tree-invalid
          :observation-checkout-root-tree-invalid]]
        invalid-values
        {:replay-artifact-path "../escape.edn"
         :replay-artifact-kind "not-a-keyword"
         :replay-schema :not-a-schema
         :replay-schema-version "1"
         :replay-artifact-id "not-a-sha256"
         :replay-raw-content-hash "not-a-sha256"
         :checkout-root-id "not-a-sha256"
         :checkout-root-commit "not-a-commit"
         :checkout-root-tree "not-a-tree"}]
    (doseq [workstream [:w1 :w2 :w3]
            field replay-pin-added-keys]
      (testing (str workstream " missing pin " field)
        (assert-rejected-codes
         (update-in (valid-request) [:pins workstream] dissoc field)
         [:pin-keys-not-exact]))
      (testing (str workstream " extra pin " field)
        (assert-rejected-codes
         (assoc-in (valid-request) [:pins workstream :synthetic-extra] true)
         [:pin-keys-not-exact]))
      (testing (str workstream " missing observation " field)
        (assert-rejected-codes
         (update-in (valid-request) [:observations workstream] dissoc field)
         [:observation-keys-not-exact]))
      (testing (str workstream " extra observation " field)
        (assert-rejected-codes
         (assoc-in (valid-request)
                   [:observations workstream :synthetic-extra]
                   true)
         [:observation-keys-not-exact])))
    (doseq [[field pin-code observation-code] fields-and-codes
            workstream [:w1 :w2 :w3]]
      (testing (str workstream " malformed pin " field)
        (assert-rejected-codes
         (assoc-in (valid-request) [:pins workstream field]
                   (get invalid-values field))
         [pin-code]))
      (testing (str workstream " malformed observation " field)
        (assert-rejected-codes
         (assoc-in (valid-request) [:observations workstream field]
                   (get invalid-values field))
         [observation-code])))))

(deftest replay-artifact-path-grammar-is-strict
  (doseq [[label bad-path]
          [["absolute path" "/tmp/p15-replay.edn"]
           ["parent traversal" "target/p15-test/../replay.edn"]
           ["dot segment" "target/./p15-replay.edn"]
           ["empty segment" "target//p15-replay.edn"]
           ["backslash" "target\\p15-replay.edn"]
           ["empty path" ""]]]
    (testing label
      (assert-rejected-codes
       (assoc-in (valid-request)
                 [:pins :w1 :replay-artifact-path]
                 bad-path)
       [:pin-replay-artifact-path-invalid])
      (assert-rejected-codes
       (assoc-in (valid-request)
                 [:observations :w2 :replay-artifact-path]
                 bad-path)
       [:observation-replay-artifact-path-invalid]))))

(deftest replay-and-checkout-observations-must-equal-pins
  (let [mutations
        [[:replay-artifact-path w3-replay-artifact-path
          :observation-replay-artifact-path-does-not-match-pin]
         [:replay-artifact-kind :gravity/test-other-replay
          :observation-replay-artifact-kind-does-not-match-pin]
         [:replay-schema "gravity.test/other-replay/v1"
          :observation-replay-schema-does-not-match-pin]
         [:replay-schema-version 2
          :observation-replay-schema-version-does-not-match-pin]
         [:replay-artifact-id w3-replay-id
          :observation-replay-artifact-id-does-not-match-pin]
         [:replay-raw-content-hash w3-replay-raw-content-hash
          :observation-replay-raw-content-hash-does-not-match-pin]
         [:checkout-root-id
          "sha256:0000000000000000000000000000000000000000000000000000000000000000"
          :observation-checkout-root-id-does-not-match-pin]
         [:checkout-root-commit
          "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
          :observation-checkout-root-commit-does-not-match-pin]
         [:checkout-root-tree
          "ffffffffffffffffffffffffffffffffffffffff"
          :observation-checkout-root-tree-does-not-match-pin]]]
    (doseq [[field value expected-code] mutations]
      (testing (str "observation/pin replay field mismatch " field)
        (assert-rejected-codes
         (assoc-in (valid-request) [:observations :w2 field] value)
         [expected-code])))))

(deftest checkout-root-is-bound-to-payload-containing-b
  (testing "checkout root commit must equal payload-containing commit"
    (assert-rejected-codes
     (assoc-in (valid-request) [:pins :w1 :checkout-root-commit]
               "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
     [:pin-checkout-root-commit-does-not-match-payload-containing-commit]))
  (testing "checkout root tree must equal payload-containing tree"
    (assert-rejected-codes
     (assoc-in (valid-request) [:pins :w1 :checkout-root-tree]
               "ffffffffffffffffffffffffffffffffffffffff")
     [:pin-checkout-root-tree-does-not-match-payload-containing-tree]))
  (testing "checkout root id must be derived from payload-containing B"
    (assert-rejected-codes
     (assoc-in (valid-request) [:pins :w1 :checkout-root-id]
               "sha256:0000000000000000000000000000000000000000000000000000000000000000")
     [:pin-checkout-root-id-does-not-match-derived-b-identity])))

(deftest circular-b-identities-are-disjoint-and-role-bound
  (let [requests {:w1 (w1-final-shaped-request)
                  :w2 (valid-request)
                  :w3 (valid-request)}]
    (doseq [workstream [:w1 :w2 :w3]]
      (let [request (get requests workstream)
            pin (get-in request [:pins workstream])
            observation (get-in request [:observations workstream])
            handoff (get-in observation [:consumer-handoff])]
        (is (= payload-containing-commit
               (:payload-containing-commit pin)) [workstream :pin])
        (is (= payload-containing-tree
               (:payload-containing-tree pin)) [workstream :pin])
        (is (= implementation-commit (:implementation-commit pin))
            [workstream :pin])
        (is (= implementation-tree (:implementation-tree pin))
            [workstream :pin])
        (is (= (replay-artifact-id-for workstream)
               (:replay-artifact-id pin))
            [workstream :pin-replay-artifact])
        (is (= (replay-raw-content-hash-for workstream)
               (:replay-raw-content-hash pin))
            [workstream :pin-replay-content])
        (is (= (replay-field workstream :replay-artifact-path)
               (:replay-artifact-path pin))
            [workstream :pin-replay-path])
        (is (= (replay-field workstream :replay-artifact-kind)
               (:replay-artifact-kind pin))
            [workstream :pin-replay-kind])
        (is (= (replay-field workstream :replay-schema)
               (:replay-schema pin))
            [workstream :pin-replay-schema])
        (is (= replay-schema-version
               (:replay-schema-version pin))
            [workstream :pin-replay-schema-version])
        (is (= checkout-root-id (:checkout-root-id pin))
            [workstream :pin-checkout-root-id])
        (is (= payload-containing-commit (:checkout-root-commit pin))
            [workstream :pin-checkout-root-commit])
        (is (= payload-containing-tree (:checkout-root-tree pin))
            [workstream :pin-checkout-root-tree])
        (is (not= (:payload-containing-commit pin)
                  (:implementation-commit pin))
            [workstream :commit-separation])
        (is (not= (:payload-containing-tree pin)
                  (:implementation-tree pin))
            [workstream :tree-separation])
        (is (= payload-containing-commit
               (:payload-containing-commit observation))
            [workstream :observation])
        (is (= payload-containing-tree
               (:payload-containing-tree observation))
            [workstream :observation])
        (is (= (replay-artifact-id-for workstream)
               (:replay-artifact-id observation))
            [workstream :observation-replay-artifact])
        (is (= (replay-raw-content-hash-for workstream)
               (:replay-raw-content-hash observation))
            [workstream :observation-replay-content])
        (doseq [field [:replay-artifact-path :replay-artifact-kind
                       :replay-schema :replay-schema-version
                       :replay-artifact-id :replay-raw-content-hash
                       :checkout-root-id :checkout-root-commit
                       :checkout-root-tree]]
          (is (= (get pin field) (get observation field))
              [workstream :observation-pin field]))
        (when handoff
          (is (= implementation-commit (:producer-commit handoff))
              [workstream :producer])
          (is (= implementation-tree (:producer-tree handoff))
              [workstream :producer])
          (is (= implementation-commit
                 (get-in handoff [:review :reviewed-commit]))
              [workstream :review])
          (is (= (:replay-artifact-id observation)
                 (get-in handoff [:verifier :replay-artifact-id]))
              [workstream :verifier-replay-artifact])
          (is (= (:replay-raw-content-hash observation)
                 (get-in handoff [:verifier :replay-content-hash]))
              [workstream :verifier-replay-content]))))))

(deftest circular-b-payload-containing-identity-cannot-reuse-implementation
  (doseq [[field expected-code]
          [[:payload-containing-commit
            :pin-payload-containing-commit-not-distinct-from-implementation]
           [:payload-containing-tree
            :pin-payload-containing-tree-not-distinct-from-implementation]]]
    (testing (str "implementation identity copied into " field)
      (assert-legacy-unreachable
       (let [implementation-value
             (if (= :payload-containing-commit field)
               implementation-commit
               implementation-tree)]
         (-> (valid-request)
             (assoc-in [:pins :w2 field] implementation-value)
             (assoc-in [:observations :w2 field] implementation-value)))
       [expected-code]))))

(deftest w1-final-handoff-is-required-and-development-candidate-is-not-final
  (testing "trusted W1 path with a missing final handoff"
    (assert-rejected-codes (valid-request)
                           [:w1-replay-contract-unfrozen]))
  (testing "development W1 candidate path and candidate key"
    (assert-rejected-codes (w1-development-candidate-request)
                           [:w1-replay-contract-unfrozen]))
  (testing "final-shaped incomplete status"
    (assert-legacy-unreachable
     (assoc-in (w1-final-shaped-request)
               [:observations :w1 :consumer-handoff :status]
               :incomplete)
     [:consumer-handoff-keys-not-exact]))
  (testing "final-shaped pending review"
    (assert-legacy-unreachable
     (assoc-in (w1-final-shaped-request)
               [:observations :w1 :consumer-handoff :review :status]
               :pending)
     [:review-not-independent-or-complete])))

(deftest w2-linux-abi-binding-is-exact-but-cannot-bypass-missing-w1-handoff
  (let [binding (get-in (valid-request)
                        [:observations :w2 :consumer-handoff :bindings])]
    (is (= w2-binding-keys (set (keys binding))) binding)
    (is (= #{:target :binary-format :architecture :calling-convention}
           (set (keys (:abi binding)))) binding)
    (is (= w2-abi (:abi binding)) binding)
    (is (= :rejected (:status (decision (valid-request)))))))

(deftest w2-abi-target-and-format-hostility-is-rejected-before-io
  (doseq [[label request expected-code]
          [["ABI key missing"
            (update-in (valid-request)
                       [:observations :w2 :consumer-handoff :bindings :abi]
                       dissoc :architecture)
            :w2-abi-keys-not-exact]
           ["ABI key extra"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :abi
                       :endianness]
                      :little)
            :w2-abi-keys-not-exact]
           ["ABI target mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :abi
                       :target]
                      :darwin-arm64)
            :w2-abi-target-not-supported]
           ["ABI Mach-O format"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :abi
                       :binary-format]
                      :mach-o)
            :w2-abi-binary-format-not-elf]
           ["ABI arm64 architecture"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :abi
                       :architecture]
                      :arm64)
            :w2-abi-architecture-not-x86-64]
           ["ABI calling convention mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :abi
                       :calling-convention]
                      :aapcs64)
            :w2-abi-calling-convention-not-sysv-amd64]]]
    (testing label
      (assert-legacy-unreachable request [expected-code]))))

(deftest w2-runtime-provider-nested-shapes-fail-closed-before-io
  (let [binding (get-in (valid-request)
                        [:observations :w2 :consumer-handoff :bindings])]
    (is (= {:stdin 0 :stdout 1 :stderr 2} (:inherited-fds binding)) binding)
    (is (= #{:declared :inferred :required} (set (keys (:effects binding))))
        binding)
    (is (= #{:declared :required} (set (keys (:capabilities binding))))
        binding)
    (is (= {:clojure-seed-boundary? true
            :public-route? false
            :self-hosted? false
            :release? false}
           (:residual-authority binding)) binding))
  (doseq [[label request expected-code]
          [["inherited fd value mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :inherited-fds :stdout]
                      7)
            :w2-inherited-fds-mismatch]
           ["inherited fd non-map"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :inherited-fds]
                      {:stdin 0 :stdout 1})
            :w2-inherited-fds-mismatch]
           ["effects key missing"
            (update-in (valid-request)
                       [:observations :w2 :consumer-handoff :bindings :effects]
                       dissoc :required)
            :w2-effects-not-exact-structured-evidence]
           ["effects non-map"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :effects]
                      #{:declared :inferred :required})
            :w2-effects-not-exact-structured-evidence]
           ["capabilities key extra"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :capabilities :inferred]
                      #{:io/stdout})
            :w2-capabilities-not-exact-structured-evidence]
           ["capabilities non-map"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :capabilities]
                      #{:declared :required})
            :w2-capabilities-not-exact-structured-evidence]
           ["residual authority mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :residual-authority :public-route?]
                      true)
            :w2-residual-authority-mismatch]
           ["residual authority key missing"
            (update-in (valid-request)
                       [:observations :w2 :consumer-handoff :bindings
                        :residual-authority]
                       dissoc :release?)
            :w2-residual-authority-mismatch]
           ["residual authority key extra"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :residual-authority :docker?]
                      false)
            :w2-residual-authority-mismatch]
           ["accepted diagnostics not exact"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :accepted-diagnostic-ids]
                      #{"P15GNR001"})
            :w2-accepted-diagnostics-not-exact]
           ["rejected diagnostics not exact"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :rejected-diagnostic-ids]
                      #{"P18T04002"})
            :w2-rejected-diagnostics-not-exact]
           ["packet schema mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :packet-schema]
                      "gravity-native-runtime-candidate-v1")
            :w2-packet-schema-mismatch]
           ["source rule id malformed"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :source-rule-id]
                      "gravity/source-rule-p15-native-synthetic")
            :w2-source-rule-id-invalid]]]
    (testing label
      (assert-legacy-unreachable request [expected-code])))
  (testing "candidate W2 path omits final handoff"
    (assert-rejected-codes (w2-candidate-request)
                           [:w1-replay-contract-unfrozen])))

(deftest w3-identity-binding-and-process-containment-fidelity-is-explicit
  (let [bindings (get-in (valid-request)
                         [:observations :w3 :consumer-handoff :bindings])
        identity-binding (:identity-binding-method bindings)
        process-containment (:process-tree-containment bindings)
        verifier (get-in (valid-request)
                         [:observations :w3 :consumer-handoff :verifier])]
    (is (= w3-binding-keys (set (keys bindings))) bindings)
    (is (= #{:method :descriptor-relative-execution?
             :fd-bound-launch-evidence-id :identity-stable-snapshot?
             :seatbelt-contained?}
           (set (keys identity-binding)))
        identity-binding)
    (is (= "linux-execveat-at-empty-path" (:method identity-binding))
        identity-binding)
    (is (true? (:descriptor-relative-execution? identity-binding))
        identity-binding)
    (is (true? (:identity-stable-snapshot? identity-binding)) identity-binding)
    (is (false? (:seatbelt-contained? identity-binding)) identity-binding)
    (is (string? (:fd-bound-launch-evidence-id identity-binding))
        identity-binding)
    (is (not (str/blank? (:fd-bound-launch-evidence-id identity-binding)))
        identity-binding)
    (is (= #{:target :tier :evidence-id}
           (set (keys (:os-gate bindings)))) bindings)
    (is (= llvm-linux-target (get-in bindings [:os-gate :target])) bindings)
    (is (= :supported (get-in bindings [:os-gate :tier])) bindings)
    (is (= w3-os-gate-evidence-id
           (get-in bindings [:os-gate :evidence-id])) bindings)
    (is (= w1-id (get-in (valid-request) [:pins :w1 :artifact-id])) bindings)
    (is (not (contains? (get-in (valid-request) [:observations :w1])
                        :consumer-handoff))
        bindings)
    (is (= w3-id (get-in (valid-request) [:pins :w3 :artifact-id])) bindings)
    (is (not= w3-id (:admitted-executable-artifact-id bindings)) bindings)
    (is (= w3-executable-id (:admitted-executable-artifact-id bindings)) bindings)
    (is (= w3-executable-id
           (get-in (valid-request)
                   [:observations :w2 :consumer-handoff :bindings
                    :provider-artifact-id]))
        bindings)
    (is (= "target/phase-15/native-runtime/linux-x86_64/p15-s23-gravity-native-runtime-provider"
           (get-in (valid-request)
                   [:observations :w2 :consumer-handoff :bindings
                    :provider-executable-path]))
        bindings)
    (is (= w3-hash
           (get-in (valid-request)
                   [:observations :w2 :consumer-handoff :bindings
                    :provider-executable-content-hash]))
        bindings)
    (is (= #{:os-process-tree-containment? :method :evidence-id}
           (set (keys process-containment))) process-containment)
    (is (true? (:os-process-tree-containment? process-containment))
        process-containment)
    (is (= "linux-cgroup-v2-clone-into-cgroup-v1"
           (:method process-containment)) process-containment)
    (is (string? (:evidence-id process-containment)) process-containment)
    (is (= w3-replay-id (:replay-artifact-id verifier)) verifier)
    (is (= w3-replay-raw-content-hash (:replay-content-hash verifier)) verifier)
    (is (= w3-replay-id (:fd-bound-launch-evidence-id identity-binding))
        {:identity identity-binding :verifier verifier})
    (is (= w3-replay-id (get-in bindings [:os-gate :evidence-id]))
        {:os-gate (:os-gate bindings) :verifier verifier})
    (is (= w3-replay-id (:evidence-id process-containment))
        {:process process-containment :verifier verifier})
    ;; Final policy/value maps are intentionally placeholders until the W3
    ;; value schema is independently frozen; hostile mutations below still
    ;; prove they cannot authorize the route.
    (is (map? bindings) bindings)))

(deftest w3-identity-binding-fails-closed-before-io
  (doseq [[label request expected-code]
          [["descriptor-relative execution false"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :descriptor-relative-execution?]
                      false)
            :w3-descriptor-relative-execution-required]
           ["descriptor-relative execution missing"
            (update-in (valid-request)
                       [:observations :w3 :consumer-handoff :bindings
                        :identity-binding-method]
                       dissoc :descriptor-relative-execution?)
            :w3-identity-binding-method-keys-not-exact]
           ["descriptor-relative execution malformed"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :descriptor-relative-execution?]
                      "true")
            :w3-descriptor-relative-execution-required]
           ["fd-bound launch evidence absent"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :fd-bound-launch-evidence-id]
                      nil)
            :w3-fd-bound-launch-evidence-id-invalid]
           ["fd-bound launch evidence missing"
            (update-in (valid-request)
                       [:observations :w3 :consumer-handoff :bindings
                        :identity-binding-method]
                       dissoc :fd-bound-launch-evidence-id)
            :w3-identity-binding-method-keys-not-exact]
           ["fd-bound launch evidence malformed"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :fd-bound-launch-evidence-id]
                      :not-a-hash)
            :w3-fd-bound-launch-evidence-id-invalid]
           ["identity-stable snapshot false"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :identity-stable-snapshot?]
                      false)
            :w3-identity-stable-snapshot-required]
           ["identity-stable snapshot malformed"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :identity-stable-snapshot?]
                      "true")
            :w3-identity-stable-snapshot-required]]]
    (testing label
      (assert-legacy-unreachable request [expected-code]))))

(deftest w3-process-containment-is-independent-of-descriptor-identity
  (let [request
        (assoc-in (valid-request)
                  [:observations :w3 :consumer-handoff :bindings
                   :process-tree-containment]
                  false)
        missing-request
        (update-in (valid-request)
                   [:observations :w3 :consumer-handoff :bindings]
                   dissoc :process-tree-containment)
        generic-contained-request
        (assoc-in (valid-request)
                  [:observations :w3 :consumer-handoff :bindings
                   :process-tree-containment :contained?]
                  true)]
    (is (true? (get-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :descriptor-relative-execution?])))
    (is (true? (get-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :identity-stable-snapshot?])))
    (is (false? (get-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings
                       :identity-binding-method :seatbelt-contained?])))
    (assert-legacy-unreachable request
                               [:w3-process-tree-containment-keys-not-exact])
    (assert-legacy-unreachable missing-request
                               [:w3-process-tree-containment-keys-not-exact])
    (assert-legacy-unreachable generic-contained-request
                               [:w3-process-tree-containment-keys-not-exact])))

(deftest w3-authority-receipt-platform-and-candidate-fidelity-fail-closed
  (testing "receipt schema must be the execution receipt schema"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings :receipt-schema]
               "gravity.p15-linux-execveat-cgroup-contained-execution-authority/v1")
     [:w3-receipt-schema-mismatch]))
  (doseq [unsupported-target [:darwin :darwin-arm64 :darwin-x86_64 :windows]]
    (testing (str "missing unsupported platform " unsupported-target)
      (assert-legacy-unreachable
       (update-in (valid-request)
                  [:observations :w3 :consumer-handoff :bindings
                   :unsupported-platforms :targets]
                  (fn [targets]
                    (vec (remove #{unsupported-target} targets))))
       [:w3-unsupported-platforms-mismatch])))
  (testing "unsupported platforms must retain the exact map"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings
                :unsupported-platforms]
               [:darwin :darwin-arm64 :darwin-x86_64 :windows])
     [:w3-unsupported-platforms-mismatch]))
  (testing "negative guarantees must retain the exact policy map"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings
                :negative-guarantees :fallback-used?]
               true)
     [:w3-negative-guarantees-mismatch]))
  (testing "negative guarantees must remain structured"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings
               :negative-guarantees]
               {:no-fallback true})
     [:w3-negative-guarantees-mismatch]))
  (doseq [[label policy-key expected-code]
          [["timeout policy" :timeout-policy :w3-timeout-policy-mismatch]
           ["signal policy" :signal-policy :w3-signal-policy-mismatch]
           ["output policy" :output-policy :w3-output-policy-mismatch]
           ["resource policy" :resource-policy :w3-resource-policy-mismatch]
           ["cleanup policy" :cleanup-policy :w3-cleanup-policy-mismatch]]]
    (testing label
      (assert-legacy-unreachable
       (assoc-in (valid-request)
                 [:observations :w3 :consumer-handoff :bindings policy-key
                  :tampered]
                 true)
       [expected-code])))
  (testing "accepted diagnostics use the exact ordered vector"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings
                :accepted-diagnostic-ids]
               ["P15CEA001"])
     [:w3-accepted-diagnostics-mismatch]))
  (testing "rejected diagnostics use the exact ordered vector"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings
                :rejected-diagnostic-ids]
               ["P15CEA199"])
     [:w3-rejected-diagnostics-mismatch]))
  (doseq [[label path expected-code]
          [["fd evidence replay binding"
            [:identity-binding-method :fd-bound-launch-evidence-id]
            :w3-fd-evidence-not-bound-to-verifier-replay]
           ["OS gate evidence replay binding"
            [:os-gate :evidence-id]
            :w3-os-gate-evidence-not-bound-to-verifier-replay]
           ["process evidence replay binding"
            [:process-tree-containment :evidence-id]
            :w3-process-evidence-not-bound-to-verifier-replay]]]
    (testing label
      (assert-legacy-unreachable
       (assoc-in (valid-request)
                 (into [:observations :w3 :consumer-handoff :bindings] path)
                 "sha256:9999999999999999999999999999999999999999999999999999999999999999")
       [expected-code])))
  (testing "W3 envelope and executable ids cannot be conflated"
    (assert-legacy-unreachable
     (assoc-in (valid-request)
               [:observations :w3 :consumer-handoff :bindings
                :admitted-executable-artifact-id]
               w3-id)
     [:w3-envelope-and-admitted-executable-must-be-distinct]))
  (testing "candidate W3 path without final handoff is not authority"
    (assert-rejected-codes (w3-candidate-request)
                           [:w1-replay-contract-unfrozen])))

(deftest w1-b3-pinned-interface-and-target-identity-are-explicit
  (let [request (w1-final-shaped-request)
        pin (get-in request [:pins :w1])
        handoff (get-in request [:observations :w1 :consumer-handoff])
        bindings (:bindings handoff)]
    (is (= w1-path (:artifact-path pin)) pin)
    (is (= w1-kind (:interface-kind pin)) pin)
    (is (= w1-schema (:interface-schema pin)) pin)
    (is (= w1-predicate (:verifier-predicate pin)) pin)
    (is (= w1-id (:artifact-id pin)) pin)
    (is (= w1-binding-keys (set (keys bindings))) bindings)
    (is (= llvm-linux-target (:target bindings)) bindings)
    (is (= w1-carrier-id (:carrier-artifact-id bindings)) bindings)
    (is (= w1-carrier-hash (:carrier-content-hash bindings)) bindings)
    (is (= w1-b3-schema-version (:carrier-schema bindings)) bindings)
    (is (= w1-b3-artifact-kind
           (get-in bindings [:provenance-edges :artifact-kind])) bindings)
    (is (= w1-b3-schema-version
           (get-in bindings [:provenance-edges :schema-version])) bindings)
    (is (= w1-placeholder-id
           (get-in request [:observations :w2 :consumer-handoff :bindings
                            :accepted-carrier-artifact-id])) request)
    (is (not= w1-carrier-id
              (get-in request [:observations :w2 :consumer-handoff :bindings
                               :accepted-carrier-artifact-id])) request)
    (is (not= w1-carrier-hash
              (get-in request [:observations :w2 :consumer-handoff :bindings
                               :accepted-carrier-content-hash])) request)
    (is (not= w1-id
              (get-in request [:observations :w2 :consumer-handoff :bindings
                               :accepted-carrier-artifact-id])) request)))

(deftest w1-envelope-identities-cannot-substitute-for-b3-carrier-identities
  (testing "W1 envelope artifact id is not the B3 carrier id"
    (assert-legacy-unreachable
     (assoc-in (w1-final-shaped-request)
               [:observations :w1 :consumer-handoff :bindings
                :carrier-artifact-id]
               w1-id)
     [:w1-replay-contract-unfrozen]))
  (testing "W1 envelope raw hash is not the B3 carrier hash"
    (assert-legacy-unreachable
     (assoc-in (w1-final-shaped-request)
               [:observations :w1 :consumer-handoff :bindings
                :carrier-content-hash]
               w1-hash)
     [:w1-replay-contract-unfrozen])))

(deftest w1-provenance-edges-are-exact-and-hostile
  (doseq [[label request expected-code]
          [["provenance edge key missing"
            (update-in (w1-final-shaped-request)
                       [:observations :w1 :consumer-handoff :bindings
                        :provenance-edges]
                       dissoc :schema-version)
            :w1-provenance-edges-keys-not-exact]
           ["provenance artifact kind mismatch"
            (assoc-in (w1-final-shaped-request)
                      [:observations :w1 :consumer-handoff :bindings
                       :provenance-edges :artifact-kind]
                      :gravity/not-the-reviewed-b3-artifact)
            :w1-provenance-artifact-kind-mismatch]
           ["provenance schema version mismatch"
            (assoc-in (w1-final-shaped-request)
                      [:observations :w1 :consumer-handoff :bindings
                       :provenance-edges :schema-version]
                      2)
            :w1-provenance-schema-version-mismatch]]]
    (testing label
      (assert-legacy-unreachable request [expected-code]))))

(deftest w1-to-w2-carrier-crosslink-remains-explicit-and-pending
  (let [future-crosslinked
        (-> (w1-final-shaped-request)
            (assoc-in [:observations :w2 :consumer-handoff :bindings
                       :accepted-carrier-artifact-id]
                      w1-carrier-id)
            (assoc-in [:observations :w2 :consumer-handoff :bindings
                       :accepted-carrier-content-hash]
                      w1-carrier-hash))
        artifact-tampered
        (assoc-in future-crosslinked
                  [:observations :w2 :consumer-handoff :bindings
                   :accepted-carrier-artifact-id]
                  w1-placeholder-id)
        hash-tampered
        (assoc-in future-crosslinked
                  [:observations :w2 :consumer-handoff :bindings
                   :accepted-carrier-content-hash]
                  w2-hash)]
    (assert-rejected-codes future-crosslinked
                           [:w1-replay-contract-unfrozen])
    (assert-rejected-codes artifact-tampered
                           [:w1-replay-contract-unfrozen])
    (assert-rejected-codes hash-tampered
                           [:w1-replay-contract-unfrozen])))

(deftest target-identity-remains-incomplete-before-w1-final-handoff
  (let [result (decision (valid-request))]
    ;; The W1 policy is pinned, but its final consumer handoff is absent.
    (is (= :rejected (:status result)) result)
    (is (= "P18T04002" (or (:id result) (:diagnostic result))) result)
    (is (= :dependency-interface-rejected (:decision result)) result)
    (is (= [:w1-replay-contract-unfrozen]
           (vec (keep :code (:rejections result)))) result)
    (is (= 1 (count (:diagnostics result))) result)
    (is (not-any? #(= :replay-owner-contract-forged-or-incomplete (:code %))
                  (:rejections result)) result)
    (is (false? (:bounded-native-route-admitted? result)) result)))

(deftest target-records-fail-closed-before-io
  (doseq [[label request expected-codes]
          [["W1 Darwin target is unsupported"
            (assoc-in (w1-final-shaped-request)
                      [:observations :w1 :consumer-handoff :bindings :target]
                      darwin-arm64-target)
            [:w1-replay-contract-unfrozen]]
           ["W1 target missing from final handoff"
            (update-in (w1-final-shaped-request)
                       [:observations :w1 :consumer-handoff :bindings]
                       dissoc :target)
            [:w1-replay-contract-unfrozen]]
           ["W3 Darwin target is unsupported"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings :os-gate
                       :target]
                      darwin-arm64-target)
            [:w1-replay-contract-unfrozen]]
           ["W3 os-gate missing target"
            (update-in (valid-request)
                       [:observations :w3 :consumer-handoff :bindings :os-gate]
                       dissoc :target)
            [:w1-replay-contract-unfrozen]]
           ["W3 os-gate absent"
            (update-in (valid-request)
                       [:observations :w3 :consumer-handoff :bindings]
                       dissoc :os-gate)
            [:w1-replay-contract-unfrozen]]
           ["W3 os-gate malformed"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :bindings :os-gate]
                      :linux)
            [:w1-replay-contract-unfrozen]]
           ["Darwin evidence is unsupported even with coherent W3 target"
            (-> (valid-request)
                (assoc-in [:observations :w3 :consumer-handoff :bindings :os-gate
                           :target]
                          darwin-arm64-target)
                (assoc-in [:observations :w3 :consumer-handoff :bindings :os-gate
                           :tier]
                          :unsupported))
            [:w1-replay-contract-unfrozen]]
           ["Linux W2/W3 provider artifact identity mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :provider-artifact-id]
                      "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
            [:w1-replay-contract-unfrozen]]
           ["Linux W2/W3 provider path identity mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :provider-executable-path]
                      "target/tampered/contained-launcher")
            [:w1-replay-contract-unfrozen]]
           ["Linux W2/W3 provider hash identity mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings
                       :provider-executable-content-hash]
                      "sha256:5555555555555555555555555555555555555555555555555555555555555555")
            [:w1-replay-contract-unfrozen]]
           ["W2 ABI target differs from Linux W3"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :bindings :abi
                       :target]
                      darwin-arm64-target)
            [:w1-replay-contract-unfrozen]]]]
    (testing label
      (assert-rejected-codes request expected-codes))))

(deftest missing-w1-final-handoff-keeps-linux-w2-w3-packet-fail-closed
  (let [request (valid-request)
        result (assert-rejected-codes request
                                      [:w1-replay-contract-unfrozen])]
    (is (false? (:dependencies-authenticated? result)) result)
    (is (false? (:dependency-interface? result)) result)
    (is (false? (:io-authorized? result)) result)
    (is (false? (:bounded-native-route-admitted? result)) result)
    (is (false? (:public-route? result)) result)
    (is (false? (:release? result)) result)
    (is (false? (:self-hosted? result)) result)
    (is (true? (:clojure-seed-boundary? result)) result)
    (is (false? (admission/public-native-admission? request)) request)
    (is (false? (admission/verified-public-route-handoff? request)) request)))

(deftest mixed-json-friendly-identifiers-do-not-thaw-missing-w1-handoff
  (let [request (mixed-identifier-request (valid-request))
        result (assert-rejected-codes request
                                      [:w1-replay-contract-unfrozen])]
    (is (false? (:dependencies-authenticated? result)) result)
    (is (false? (:dependency-interface? result)) result)
    (is (false? (:bounded-native-route-admitted? result)) result)
    (is (false? (:public-route? result)) result)
    (is (true? (:clojure-seed-boundary? result)) result)))

(deftest parsed-w1-json-key-spelling-is-strict-and-fail-closed
  (doseq [[label request expected-code]
          [["snake-case consumer handoff key"
            (update-in (valid-request)
                       [:observations :w2 :consumer-handoff]
                       (fn [handoff]
                         (-> handoff
                             (assoc :contract_version
                                    (:contract-version handoff))
                             (dissoc :contract-version))))
            :consumer-handoff-keys-not-exact]
           ["snake-case W1 carrier binding key"
            (update-in (w1-final-shaped-request)
                       [:observations :w1 :consumer-handoff :bindings]
                       (fn [bindings]
                         (-> bindings
                             (assoc :carrier_artifact_id
                                    (:carrier-artifact-id bindings))
                             (dissoc :carrier-artifact-id))))
            :bindings-keys-not-exact]
           ["snake-case claims key"
            (update-in (valid-request)
                       [:observations :w2 :consumer-handoff :claims]
                       (fn [claims]
                         (-> claims
                             (assoc :public_route?
                                    (:public-route? claims))
                             (dissoc :public-route?))))
            :claims-keys-not-exact]]]
    (testing label
      (assert-legacy-unreachable request [expected-code]))))

(deftest identifier-coercion-is-not-permitted
  (assert-rejected-codes
   (assoc-in (valid-request)
             [:observations :w2 :consumer-handoff :workstream]
             1)
   [:w1-replay-contract-unfrozen])
  (assert-rejected-codes
   (assoc-in (valid-request)
             [:pins :w2 :interface-kind]
             {:coerced "gravity/p15-w2-gravity-runtime-provider-v1"})
   [:w1-replay-contract-unfrozen]))

(deftest default-admission-remains-incomplete
  (let [result (value-or-call admission/default-public-native-admission)]
    (is (or (= :incomplete (:status result))
            (false? (:complete? result))) result)
    (is (false? (:bounded-native-route-admitted? result)) result)
    (is (false? (:public-route? result)) result)
    (is (true? (:clojure-seed-boundary? result)) result)
    (is (false? (:self-hosted? result)) result)
    (is (false? (:release? result)) result)
    (is (= 1 (count (:diagnostics result))) result)
    (is (= 1 (count (:rejections result))) result)
    (is (= (:diagnostics result) (:rejections result)) result)
    (is (= [:missing-reviewed-w1-w2-w3-observations]
           (vec (keep :code (:rejections result)))) result)))

(deftest every-producer-is-required
  (doseq [producer-key [:w1 :w2 :w3]]
    (testing (str "missing " producer-key)
      (assert-rejected-codes
       (update (valid-request) :observations dissoc producer-key)
       [:observation-workstream-keys-not-exact])
      (testing "missing producer pin"
        (assert-rejected-codes
         (update (valid-request) :pins dissoc producer-key)
         [:pin-workstream-keys-not-exact])))))

(deftest candidate-shape-is-exact-and-narrative-records-are-not-evidence
  (testing "missing top-level key"
    (assert-rejected-codes (update (valid-request) :observations dissoc :w1)
                           [:observation-workstream-keys-not-exact]))
  (testing "extra top-level key"
    (assert-rejected-codes (assoc (valid-request) :narrative "reviewed and ready")
                           [:request-keys-not-exact]))
  (testing "extra pin key"
    (assert-rejected-codes
     (assoc-in (valid-request) [:pins :w2 :status] :verified)
     [:pin-keys-not-exact]))
  (testing "extra observation key"
    (assert-rejected-codes
     (assoc-in (valid-request) [:observations :w2 :status] :verified)
     [:observation-keys-not-exact]))
  (testing "narrative/status-only record"
    (assert-rejected-codes
     (assoc (valid-request)
            :observations
            {:status "all dependencies are good"
             :narrative "W1/W2/W3 passed review"})
     [:observation-workstream-keys-not-exact])))

(deftest wrong-kind-schema-predicate-and-version-are-rejected
  (let [request-shape-codes #{:request-artifact-mismatch
                              :request-schema-version-mismatch}]
    (doseq [[label request expected-codes]
          [["wrong request artifact"
            (assoc (valid-request) :artifact :gravity/not-admission)
            [:request-artifact-mismatch]]
           ["wrong request schema"
            (assoc (valid-request) :schema-version "gravity.bad/v9")
            [:request-schema-version-mismatch]]
           ["wrong W1 interface kind"
            (assoc-in (valid-request) [:pins :w1 :interface-kind]
                      :gravity/not-w1)
            [:pin-interface-kind-mismatch]]
           ["wrong W1 interface schema"
            (assoc-in (valid-request) [:pins :w1 :interface-schema]
                      "gravity.bad.w1/v9")
            [:pin-interface-schema-mismatch]]
           ["wrong W1 predicate"
            (assoc-in (valid-request) [:pins :w1 :verifier-predicate]
                      :gravity/not-the-reviewed-w1-predicate)
            [:pin-verifier-predicate-mismatch]]
           ["wrong W1 predicate version"
            (assoc-in (valid-request) [:pins :w1 :predicate-version] 99)
            [:pin-predicate-version-mismatch]]
           ["wrong W2 interface kind"
            (assoc-in (valid-request) [:pins :w2 :interface-kind]
                      :gravity/not-w2)
            [:pin-interface-kind-mismatch]]
           ["wrong W2 interface schema"
            (assoc-in (valid-request) [:pins :w2 :interface-schema]
                      "gravity.bad.w2/v9")
            [:pin-interface-schema-mismatch]]
           ["wrong W3 predicate"
            (assoc-in (valid-request) [:pins :w3 :verifier-predicate]
                      :gravity/not-the-reviewed-predicate)
            [:pin-verifier-predicate-mismatch]]
           ["Darwin W3 interface kind withdrawn"
            (assoc-in (valid-request) [:pins :w3 :interface-kind]
                      "darwin-contained-execution-authority")
            [:pin-interface-kind-mismatch]]
           ["Darwin W3 interface schema withdrawn"
            (assoc-in (valid-request) [:pins :w3 :interface-schema]
                      "gravity/p15-darwin-contained-execution-authority/v1")
            [:pin-interface-schema-mismatch]]
           ["wrong W2 predicate version"
            (assoc-in (valid-request) [:pins :w2 :predicate-version] 99)
            [:pin-predicate-version-mismatch]]]]
      (testing label
        (if (contains? request-shape-codes (first expected-codes))
          (assert-rejected-codes request expected-codes)
          (assert-legacy-unreachable request expected-codes))))))

(deftest malformed-or-mismatched-identities-are-rejected
  (doseq [[label request expected-codes]
          [["malformed raw hash"
            (assoc-in (valid-request) [:pins :w1 :raw-content-hash]
                      "sha256:not-a-digest")
            [:pin-raw-content-hash-invalid]]
           ["malformed W1 artifact id"
            (assoc-in (valid-request) [:pins :w1 :artifact-id]
                      "w1-not-a-sha")
            [:pin-artifact-id-invalid]]
           ["W1 payload-containing commit mismatch"
            (assoc-in (w1-final-shaped-request)
                      [:observations :w1 :payload-containing-commit]
                      "0000000000000000000000000000000000000000")
            [:observation-payload-containing-commit-does-not-match-pin]]
           ["W1 observation raw hash mismatch"
            (assoc-in (w1-final-shaped-request) [:observations :w1 :raw-content-hash]
                      w2-hash)
            [:observation-raw-content-hash-does-not-match-pin]]
           ["W1 implementation tree malformed"
            (assoc-in (valid-request) [:pins :w1 :implementation-tree]
                      "tree-implementation-tampered")
            [:pin-implementation-tree-invalid]]
           ["malformed W2 raw hash"
            (assoc-in (valid-request) [:pins :w2 :raw-content-hash]
                      "sha256:not-a-digest")
            [:pin-raw-content-hash-invalid]]
           ["pin/observation hash mismatch"
            (assoc-in (valid-request) [:observations :w2 :raw-content-hash]
                      w3-hash)
            [:observation-raw-content-hash-does-not-match-pin]]
           ["pin/observation artifact mismatch"
            (assoc-in (valid-request) [:observations :w3 :consumer-handoff
                                       :artifact-id]
                      "sha256:dededededededededededededededededededededededededededededededede")
            [:consumer-handoff-artifact-id-mismatch]]
           ["payload-containing commit mismatch"
            (assoc-in (valid-request)
                      [:observations :w2 :payload-containing-commit]
                      "0000000000000000000000000000000000000000")
            [:observation-payload-containing-commit-does-not-match-pin]]
           ["implementation tree mismatch"
            (assoc-in (valid-request) [:pins :w2 :implementation-tree]
                      "tree-implementation-tampered")
            [:pin-implementation-tree-invalid]]
           ["payload-containing tree mismatch"
            (assoc-in (valid-request)
                      [:observations :w3 :payload-containing-tree]
                      "tree-payload-containing-tampered")
            [:observation-payload-containing-tree-invalid]]
           ["implementation commit mismatch"
            (assoc-in (valid-request) [:pins :w2 :implementation-commit]
                      "0000000000000000000000000000000000000000")
            [:producer-commit-does-not-match-implementation]]
           ["W2 to W3 binding mismatch"
            (assoc-in (valid-request) [:observations :w2 :consumer-handoff
                                       :bindings :provider-artifact-id]
                      "sha256:dededededededededededededededededededededededededededededededede")
            [:w2-to-w3-provider-artifact-cross-binding-mismatch]]]]
    (testing label
      (assert-legacy-unreachable request expected-codes))))

(deftest replay-identities-are-independent-and-cross-bound
  (doseq [[label request expected-codes]
          [["pin replay artifact id malformed"
            (assoc-in (valid-request) [:pins :w1 :replay-artifact-id]
                      "sha256:not-a-replay-artifact")
            [:pin-replay-artifact-id-invalid]]
           ["pin replay raw hash malformed"
            (assoc-in (valid-request) [:pins :w1 :replay-raw-content-hash]
                      "sha256:not-a-replay-hash")
            [:pin-replay-raw-content-hash-invalid]]
           ["observation replay artifact id malformed"
            (assoc-in (valid-request)
                      [:observations :w2 :replay-artifact-id]
                      "sha256:not-a-replay-artifact")
            [:observation-replay-artifact-id-invalid]]
           ["observation replay raw hash malformed"
            (assoc-in (valid-request)
                      [:observations :w2 :replay-raw-content-hash]
                      "sha256:not-a-replay-hash")
            [:observation-replay-raw-content-hash-invalid]]
           ["observation replay artifact id differs from pin"
            (assoc-in (valid-request)
                      [:observations :w2 :replay-artifact-id]
                      w3-replay-id)
            [:observation-replay-artifact-id-does-not-match-pin]]
           ["observation replay raw hash differs from pin"
            (assoc-in (valid-request)
                      [:observations :w2 :replay-raw-content-hash]
                      w3-replay-raw-content-hash)
            [:observation-replay-raw-content-hash-does-not-match-pin]]
           ["pin replay artifact id differs from observation"
            (assoc-in (valid-request)
                      [:pins :w2 :replay-artifact-id]
                      w3-replay-id)
            [:observation-replay-artifact-id-does-not-match-pin]]
           ["pin replay raw hash differs from observation"
            (assoc-in (valid-request)
                      [:pins :w2 :replay-raw-content-hash]
                      w3-replay-raw-content-hash)
            [:observation-replay-raw-content-hash-does-not-match-pin]]]]
    (testing label
      (assert-rejected-codes request expected-codes)))
  (doseq [[label path value expected-code]
          [["unaccepted W2 review"
            [:observations :w2 :consumer-handoff :review :status] :pending
            :review-not-independent-or-complete]
           ["failed W2 replay"
            [:observations :w2 :consumer-handoff :verifier :status]
            :failed
            :verifier-replay-not-passed]
           ["replay predicate mismatch"
            [:observations :w3 :consumer-handoff :verifier :predicate]
            :gravity/not-the-reviewed-predicate
            :verifier-predicate-does-not-match-pin]
           ["review commit mismatch"
            [:observations :w2 :consumer-handoff :review :reviewed-commit]
            "0000000000000000000000000000000000000000"
            :reviewed-commit-does-not-match-implementation]
           ["replay artifact mismatch"
            [:observations :w2 :consumer-handoff :verifier :replay-artifact-id]
            w3-replay-id
            :verifier-replay-artifact-id-does-not-match-observation]
           ["replay hash mismatch"
            [:observations :w2 :consumer-handoff :verifier :replay-content-hash]
            w3-replay-raw-content-hash
            :verifier-replay-content-hash-does-not-match-observation]]]
    (testing label
      (assert-legacy-unreachable (assoc-in (valid-request) path value)
                                 [expected-code]))))

(deftest unsupported-source-extension-and-premature-seed-retirement-are-rejected
  (testing "unsupported extension"
    (assert-rejected-codes (assoc (valid-request) :source-extension ".clj")
                           [:unsupported-source-extension]))
  (testing "attempted public seed retirement without tracked-route proof"
    (assert-rejected-codes
     (assoc-in (valid-request) [:observations :w2 :consumer-handoff :claims
                                :clojure-seed-boundary?]
               false)
     [:w1-replay-contract-unfrozen]))
  (testing "attempted global seed retirement without tracked-route proof"
    (assert-rejected-codes
     (assoc (valid-request) :global-clojure-seed-boundary? false)
     [:request-keys-not-exact]))
  (testing "attempted public route claim without tracked-route proof"
    (assert-rejected-codes
     (assoc-in (valid-request) [:observations :w2 :consumer-handoff :claims
                                :public-route?]
               true)
     [:w1-replay-contract-unfrozen])))

(deftest development-container-and-tool-records-cannot-thaw-admission
  (doseq [[label request expected-codes]
          [["Docker claim in W2 claims"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :claims :docker?]
                      true)
            [:claims-keys-not-exact]]
           ["QEMU claim in W2 claims"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :claims :qemu?]
                      true)
            [:claims-keys-not-exact]]
           ["container boolean in common handoff"
            (assoc-in (valid-request)
                      [:observations :w2 :consumer-handoff :container?]
                      true)
            [:consumer-handoff-keys-not-exact]]
           ["gcc tool boolean in common handoff"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :gcc?]
                      true)
            [:consumer-handoff-keys-not-exact]]
           ["clang tool boolean in claims"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :claims :clang?]
                      true)
            [:claims-keys-not-exact]]
           ["image digest in common handoff"
            (assoc-in (valid-request)
                      [:observations :w3 :consumer-handoff :image-digest]
                      "development-only-placeholder")
            [:consumer-handoff-keys-not-exact]]]]
    (testing label
      (assert-legacy-unreachable request expected-codes))))

(deftest admission-is-pure-and-replay-stable
  (let [request (valid-request)
        first-result (decision request)
        second-result (decision request)]
    (is (= first-result second-result))
    (is (false? (:io-authorized? first-result)))
    (is (not (admission/public-native-admission? request)))
    (is (not (admission/verified-public-route-handoff? request)))))

(deftest future-public-route-marker-is-not-current-authority
  (let [route {:artifact :gravity/p15-public-native-route-admission
               :schema-version "gravity.p15-public-native-route-admission/v1"
               :tracked-route {:synthetic? true}
               :w1 {}
               :w2 {}
               :w3 {}
               :verifier {}
               :review {:status :accepted}
               :claims {:public-route? true
                        :clojure-seed-boundary? false
                        :self-hosted? true
                        :release? true}}]
    (is (false? (admission/verified-public-route-handoff? route)) route)))
