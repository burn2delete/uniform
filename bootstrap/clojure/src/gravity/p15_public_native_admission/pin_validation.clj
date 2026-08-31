(ns gravity.p15-public-native-admission.pin-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-pin
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
