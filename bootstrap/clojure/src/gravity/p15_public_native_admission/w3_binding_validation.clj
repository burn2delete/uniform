(ns gravity.p15-public-native-admission.w3-binding-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-w3-bindings
  [binding pin]
  (let [path [:observations :w3 :consumer-handoff :bindings]]
    (cond-> []
      (not (sha256? (:admitted-executable-artifact-id binding)))
      (conj (issue :w3-admitted-executable-artifact-id-invalid
                   (conj path :admitted-executable-artifact-id)))

      (= (:admitted-executable-artifact-id binding)
         (:artifact-id pin))
      (conj (issue :w3-envelope-and-admitted-executable-must-be-distinct
                   (conj path :admitted-executable-artifact-id)))

      (not (relative-path? (:admitted-executable-path binding)))
      (conj (issue :w3-admitted-executable-path-invalid
                   (conj path :admitted-executable-path)))

      (not (sha256? (:admitted-executable-content-hash binding)))
      (conj (issue :w3-admitted-executable-content-hash-invalid
                   (conj path :admitted-executable-content-hash)))

      (not (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys))
      (conj (issue :w3-identity-binding-method-keys-not-exact
                   (conj path :identity-binding-method)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (same-identity? (get-in binding [:identity-binding-method
                                                  :method])
                                "linux-execveat-at-empty-path")))
      (conj (issue :w3-identity-binding-method-invalid
                   (conj path :identity-binding-method :method)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (true? (get-in binding [:identity-binding-method
                                         :descriptor-relative-execution?]))))
      (conj (issue :w3-descriptor-relative-execution-required
                   (conj path :identity-binding-method
                         :descriptor-relative-execution?)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (sha256? (get-in binding [:identity-binding-method
                                           :fd-bound-launch-evidence-id]))))
      (conj (issue :w3-fd-bound-launch-evidence-id-invalid
                   (conj path :identity-binding-method
                         :fd-bound-launch-evidence-id)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (true? (get-in binding [:identity-binding-method
                                        :identity-stable-snapshot?]))))
      (conj (issue :w3-identity-stable-snapshot-required
                   (conj path :identity-binding-method
                         :identity-stable-snapshot?)))

      (and (exact-keys? (:identity-binding-method binding)
                        identity-binding-keys)
           (not (false? (get-in binding [:identity-binding-method
                                         :seatbelt-contained?]))))
      (conj (issue :w3-seatbelt-contained-must-be-false
                   (conj path :identity-binding-method
                         :seatbelt-contained?)))

      (not (exact-keys? (:os-gate binding) os-gate-keys))
      (conj (issue :w3-os-gate-keys-not-exact (conj path :os-gate)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (identifier? (:target (:os-gate binding)))))
      (conj (issue :w3-os-gate-target-invalid
                   (conj path :os-gate :target)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (same-identity? (:target (:os-gate binding))
                                supported-target)))
      (conj (issue :w3-os-gate-target-not-supported
                   (conj path :os-gate :target)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (identifier? (:tier (:os-gate binding)))))
      (conj (issue :w3-os-gate-tier-invalid
                   (conj path :os-gate :tier)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (same-identity? (:tier (:os-gate binding))
                                supported-target-tier)))
      (conj (issue :w3-os-gate-tier-not-supported
                   (conj path :os-gate :tier)))

      (and (exact-keys? (:os-gate binding) os-gate-keys)
           (not (sha256? (:evidence-id (:os-gate binding)))))
      (conj (issue :w3-os-gate-evidence-id-invalid
                   (conj path :os-gate :evidence-id)))

      (not (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys))
      (conj (issue :w3-process-tree-containment-keys-not-exact
                   (conj path :process-tree-containment)))

      (and (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys)
           (not (true? (:os-process-tree-containment?
                        (:process-tree-containment binding)))))
      (conj (issue :w3-process-tree-contained-fact-not-proven
                   (conj path :process-tree-containment
                         :os-process-tree-containment?)))

      (and (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys)
           (not (same-identity? (:method (:process-tree-containment binding))
                                "linux-cgroup-v2-clone-into-cgroup-v1")))
      (conj (issue :w3-process-tree-containment-method-invalid
                   (conj path :process-tree-containment :method)))

      (and (exact-keys? (:process-tree-containment binding)
                        process-tree-containment-keys)
           (not (sha256? (:evidence-id (:process-tree-containment binding)))))
      (conj (issue :w3-process-tree-containment-evidence-id-invalid
                   (conj path :process-tree-containment :evidence-id)))

      (not= w3-receipt-schema (:receipt-schema binding))
      (conj (issue :w3-receipt-schema-mismatch (conj path :receipt-schema)))

      (not= w3-timeout-policy (:timeout-policy binding))
      (conj (issue :w3-timeout-policy-mismatch
                   (conj path :timeout-policy)))

      (not= w3-signal-policy (:signal-policy binding))
      (conj (issue :w3-signal-policy-mismatch
                   (conj path :signal-policy)))

      (not= w3-output-policy (:output-policy binding))
      (conj (issue :w3-output-policy-mismatch
                   (conj path :output-policy)))

      (not= w3-resource-policy (:resource-policy binding))
      (conj (issue :w3-resource-policy-mismatch
                   (conj path :resource-policy)))

      (not= w3-cleanup-policy (:cleanup-policy binding))
      (conj (issue :w3-cleanup-policy-mismatch
                   (conj path :cleanup-policy)))

      (not= w3-negative-guarantees (:negative-guarantees binding))
      (conj (issue :w3-negative-guarantees-mismatch
                   (conj path :negative-guarantees)))

      (not= w3-unsupported-platforms (:unsupported-platforms binding))
      (conj (issue :w3-unsupported-platforms-mismatch
                   (conj path :unsupported-platforms)))

      (not= w3-accepted-diagnostic-ids
            (:accepted-diagnostic-ids binding))
      (conj (issue :w3-accepted-diagnostics-mismatch
                   (conj path :accepted-diagnostic-ids)))

      (not= w3-rejected-diagnostic-ids
            (:rejected-diagnostic-ids binding))
      (conj (issue :w3-rejected-diagnostics-mismatch
                   (conj path :rejected-diagnostic-ids))))))
