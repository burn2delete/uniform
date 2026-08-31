(ns gravity.p15-public-native-admission.observation-handoff-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]
            [gravity.p15-public-native-admission.handoff-validation :refer :all]
            [gravity.p15-public-native-admission.w1-binding-validation :refer :all]
            [gravity.p15-public-native-admission.w2-binding-validation :refer :all]
            [gravity.p15-public-native-admission.w3-binding-validation :refer :all]))

(defn validate-observation-handoff
  [workstream pin observation]
  (let [path [:observations workstream]
        handoff (when (map? observation)
                  (:consumer-handoff observation))
        verifier (when (map? handoff) (:verifier handoff))
        review (when (map? handoff) (:review handoff))
        claims (when (map? handoff) (:claims handoff))
        bindings (when (map? handoff) (:bindings handoff))]
    (cond-> []
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
