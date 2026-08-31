(ns gravity.p15-public-native-admission.w2-binding-validation
  (:require [gravity.p15-public-native-admission.producer-contract :refer :all]
            [gravity.p15-public-native-admission.replay-contract :refer :all]
            [gravity.p15-public-native-admission.evidence-contract :refer :all]
            [gravity.p15-public-native-admission.validation-support :refer :all]))

(defn validate-w2-bindings
  [binding pin]
  (let [path [:observations :w2 :consumer-handoff :bindings]]
    (cond-> []
      (not (sha256? (:accepted-carrier-artifact-id binding)))
      (conj (issue :w2-accepted-carrier-artifact-id-invalid
                   (conj path :accepted-carrier-artifact-id)))

      (not (sha256? (:accepted-carrier-content-hash binding)))
      (conj (issue :w2-accepted-carrier-content-hash-invalid
                   (conj path :accepted-carrier-content-hash)))

      (not (sha256? (:provider-artifact-id binding)))
      (conj (issue :w2-provider-artifact-id-invalid
                   (conj path :provider-artifact-id)))

      (not= w2-provider-executable-path
            (:provider-executable-path binding))
      (conj (issue :w2-provider-executable-path-mismatch
                   (conj path :provider-executable-path)))

      (not (sha256? (:provider-executable-content-hash binding)))
      (conj (issue :w2-provider-executable-content-hash-invalid
                   (conj path :provider-executable-content-hash)))

      (not (sha256? (:runtime-manifest-id binding)))
      (conj (issue :w2-runtime-manifest-id-invalid
                   (conj path :runtime-manifest-id)))

      (not= w2-packet-schema (:packet-schema binding))
      (conj (issue :w2-packet-schema-mismatch (conj path :packet-schema)))

      (not (sha256? (:source-rule-id binding)))
      (conj (issue :w2-source-rule-id-invalid (conj path :source-rule-id)))

      (not (exact-keys? (:abi binding) abi-keys))
      (conj (issue :w2-abi-keys-not-exact (conj path :abi)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not (same-identity? (:target (:abi binding))
                                supported-target)))
      (conj (issue :w2-abi-target-not-supported
                   (conj path :abi :target)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not= :elf (:binary-format (:abi binding))))
      (conj (issue :w2-abi-binary-format-not-elf
                   (conj path :abi :binary-format)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not= :x86_64 (:architecture (:abi binding))))
      (conj (issue :w2-abi-architecture-not-x86-64
                   (conj path :abi :architecture)))

      (and (exact-keys? (:abi binding) abi-keys)
           (not= :sysv-amd64 (:calling-convention (:abi binding))))
      (conj (issue :w2-abi-calling-convention-not-sysv-amd64
                   (conj path :abi :calling-convention)))

      (not= w2-inherited-fds (:inherited-fds binding))
      (conj (issue :w2-inherited-fds-mismatch
                   (conj path :inherited-fds)))

      (not (exact-structured-values? (:effects binding) w2-effects-keys))
      (conj (issue :w2-effects-not-exact-structured-evidence
                   (conj path :effects)))

      (not (exact-structured-values? (:capabilities binding)
                                     w2-capabilities-keys))
      (conj (issue :w2-capabilities-not-exact-structured-evidence
                   (conj path :capabilities)))

      (not (sha256? (:no-clojure-evidence-id binding)))
      (conj (issue :w2-no-clojure-evidence-missing
                   (conj path :no-clojure-evidence-id)))

      (not (sha256? (:no-jvm-evidence-id binding)))
      (conj (issue :w2-no-jvm-evidence-missing
                   (conj path :no-jvm-evidence-id)))

      (not= #{} (:accepted-diagnostic-ids binding))
      (conj (issue :w2-accepted-diagnostics-not-exact
                   (conj path :accepted-diagnostic-ids)))

      (not= w2-rejected-diagnostic-ids
            (:rejected-diagnostic-ids binding))
      (conj (issue :w2-rejected-diagnostics-not-exact
                   (conj path :rejected-diagnostic-ids)))

      (not= w2-residual-authority (:residual-authority binding))
      (conj (issue :w2-residual-authority-mismatch
                   (conj path :residual-authority))))))
