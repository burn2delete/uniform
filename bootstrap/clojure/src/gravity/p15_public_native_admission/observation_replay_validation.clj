(ns gravity.p15-public-native-admission.observation-replay-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]
            [gravity.p15-public-native-admission.replay-policy-validation :refer [validate-replay-policy]]))

(defn validate-observation-replay
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
      )))
