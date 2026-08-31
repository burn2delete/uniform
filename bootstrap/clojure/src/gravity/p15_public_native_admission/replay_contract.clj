(ns gravity.p15-public-native-admission.replay-contract)

(def replay-policy-keys
  #{:state :replay-contract-frozen? :missing-fields :claims})

(def replay-policy-owner-keys
  #{:w1 :w2 :w3})

(def ^:dynamic replay-policies
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

(def replay-owner-blockers
  {:w1 :w1-replay-contract-unfrozen
   :w2 :w2-replay-contract-unfrozen
   :w3 :w3-native-replay-schema-unfrozen})

(def replay-diagnostic-order
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

(def replay-structure-diagnostic-order
  (vec (take-while #(not= :replay-owner-contract-forged-or-incomplete %)
                   replay-diagnostic-order)))

(def future-request-v2
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

(def w6-payload-containing-commit-registry
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
