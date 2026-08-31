(ns gravity.p15-public-native-admission.w1-binding-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-collection-evidence
  [workstream binding key issues]
  (let [path [:observations workstream :consumer-handoff :bindings key]]
    (append-issue issues :binding-evidence-missing path
                  (nonempty-evidence? (get binding key)))))

(defn validate-w1-bindings
  [binding pin]
  (let [path [:observations :w1 :consumer-handoff :bindings]]
    (cond-> []
      (not (sha256? (:carrier-artifact-id binding)))
      (conj (issue :w1-carrier-artifact-id-invalid
                   (conj path :carrier-artifact-id)))

      (= (:carrier-artifact-id binding) (:artifact-id pin))
      (conj (issue :w1-envelope-artifact-id-substituted-for-carrier
                   (conj path :carrier-artifact-id)))

      (not (sha256? (:carrier-content-hash binding)))
      (conj (issue :w1-carrier-content-hash-invalid
                   (conj path :carrier-content-hash)))

      (= (:carrier-content-hash binding) (:raw-content-hash pin))
      (conj (issue :w1-envelope-content-hash-substituted-for-carrier
                   (conj path :carrier-content-hash)))

      (not= 1 (:carrier-schema binding))
      (conj (issue :w1-carrier-schema-not-b3-v1
                   (conj path :carrier-schema)))

      (not (identifier? (:source-id binding)))
      (conj (issue :w1-source-id-invalid (conj path :source-id)))

      (not (identifier? (:semantic-id binding)))
      (conj (issue :w1-semantic-id-invalid (conj path :semantic-id)))

      (not (identifier? (:profile binding)))
      (conj (issue :w1-profile-invalid (conj path :profile)))

      (not (identifier? (:target binding)))
      (conj (issue :w1-target-invalid (conj path :target)))

      (and (identifier? (:target binding))
           (not (same-identity? (:target binding) supported-target)))
      (conj (issue :w1-target-not-supported
                   (conj path :target)))

      (not (nonempty-evidence? (:effects binding)))
      (conj (issue :w1-effects-evidence-missing (conj path :effects)))

      (not (nonempty-evidence? (:capabilities binding)))
      (conj (issue :w1-capabilities-evidence-missing
                   (conj path :capabilities)))

      (not (nonempty-evidence? (:safety binding)))
      (conj (issue :w1-safety-evidence-missing (conj path :safety)))

      (not (coll? (:accepted-diagnostic-ids binding)))
      (conj (issue :w1-accepted-diagnostics-not-structured
                   (conj path :accepted-diagnostic-ids)))

      (not (coll? (:rejected-diagnostic-ids binding)))
      (conj (issue :w1-rejected-diagnostics-not-structured
                   (conj path :rejected-diagnostic-ids)))

      (not (exact-keys? (:provenance-edges binding)
                        w1-provenance-edges-keys))
      (conj (issue :w1-provenance-edges-keys-not-exact
                   (conj path :provenance-edges)))

      (and (exact-keys? (:provenance-edges binding)
                        w1-provenance-edges-keys)
           (not (same-identity?
                 (:artifact-kind (:provenance-edges binding))
                 (get-in producer-policies [:w1 :policy-metadata :kind]))))
      (conj (issue :w1-provenance-artifact-kind-mismatch
                   (conj path :provenance-edges :artifact-kind)))

      (and (exact-keys? (:provenance-edges binding)
                        w1-provenance-edges-keys)
           (not= 1 (:schema-version (:provenance-edges binding))))
      (conj (issue :w1-provenance-schema-version-mismatch
                   (conj path :provenance-edges :schema-version))))))
