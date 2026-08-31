(ns gravity.p15-public-native-admission.replay-pin-structure
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-replay-pin-structure
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
