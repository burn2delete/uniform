(ns gravity.p15-public-native-admission.replay-policy-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn replay-policy-missing-fields-exact?
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

(defn replay-policy-valid?
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

(defn replay-policy-table-valid?
  []
  (and (exact-keys? replay-policies replay-policy-owner-keys)
       (every? (fn [workstream]
                 (replay-policy-valid?
                  workstream (get replay-policies workstream)))
               producer-order)))

(defn validate-replay-policy
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
