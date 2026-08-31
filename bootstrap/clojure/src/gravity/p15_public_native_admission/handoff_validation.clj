(ns gravity.p15-public-native-admission.handoff-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-verifier
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

(defn validate-review
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

(defn validate-claims
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
