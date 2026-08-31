(ns gravity.c13-optimization.artifact
  (:require [gravity.c13-optimization.operations :as operations]
            [gravity.c13-optimization.policy :as policy]
            [gravity.c13-optimization.projection :as projection]
            [gravity.c13-optimization.validation :as validation]
            [gravity.digest :as digest]
            [gravity.optimization-lowering :as shared]))

(defn- source-span [path index]
  (operations/invoke :source-span
                     (fn [source form-index] {:source source :form-index form-index})
                     path index))

(defn- artifact-id [artifact]
  (operations/invoke :c4-artifact-id
                     (fn [value] (str "sha256:" (digest/sha256-hex (pr-str value))))
                     artifact))

(defn- diagnostic-catalog [configuration source-path]
  (operations/invoke :c13-optimization-diagnostic-catalog
                     (fn [path] (validation/diagnostic-catalog configuration path source-span))
                     source-path))

(defn source-artifact [configuration source-path source-text]
  (let [records (operations/invoke :read-source-form-records
                                   (policy/unsupported :read-source-form-records)
                                   source-path source-text)
        forms (mapv :form records)
        _ (operations/invoke :validate-ns-syntax! (policy/unsupported :validate-ns-syntax!)
                             source-path forms)
        module (operations/invoke :parse-module (policy/unsupported :parse-module) source-path forms)
        source-overrides (operations/invoke :c13-optimization-source-overrides
                                            validation/source-overrides module)
        _ (operations/invoke :c13-optimization-validate-source-overrides!
                             (fn [path overrides]
                               (operations/invoke :optimization-lowering-validate-overrides!
                                                  shared/optimization-lowering-validate-overrides!
                                                  path (validation/source-overrides-artifact overrides)))
                             source-path source-overrides)
        domain-ir-artifact (operations/invoke :compiler-c12-domain-ir-source-artifact
                                              (policy/unsupported :compiler-c12-domain-ir-source-artifact)
                                              source-path source-text)
        input-id (:artifact-id domain-ir-artifact)
        contracts (mapv #(operations/invoke :optimization-pass-contract-record
                                            shared/optimization-pass-contract-record %)
                        (:optimization-pass-contract-seed configuration))
        decisions (mapv #(operations/invoke :optimization-decision-record
                                            shared/optimization-decision-record
                                            domain-ir-artifact input-id %2 %1)
                        contracts (range))
        diagnostics (diagnostic-catalog configuration source-path)
        artifact-base (projection/artifact-base configuration source-text module source-overrides
                                               domain-ir-artifact input-id contracts decisions diagnostics)
        _ (operations/invoke :c13-optimization-validate!
                             (fn [path artifact] (validation/validate! configuration path artifact))
                             source-path artifact-base)
        capability-proof (operations/invoke :c13-optimization-capability-proof
                                            (fn [artifact] (validation/capability-proof configuration artifact))
                                            artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (artifact-id (assoc artifact-base :capability-based-proof capability-proof)))))

(defn file-artifact [configuration path]
  (operations/invoke :compiler-c13-optimization-source-artifact
                     (fn [source-path source-text]
                       (source-artifact configuration source-path source-text))
                     path (slurp path)))
