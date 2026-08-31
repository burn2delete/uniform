(ns gravity.p15-public-native-admission.replay-issue-lookup
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn replay-pin-issue-for-code
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

(defn replay-observation-issue-for-code
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

(defn first-replay-structure-issue
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
