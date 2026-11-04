(ns gravity.c13-optimization.artifact
  (:require [gravity.optimization :as optimization]
            [gravity.c13-optimization.operations :as operations]
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
  (optimization/native-source-artifact configuration source-path source-text
    {:invoke (fn [] operations/invoke) :unsupported (fn [] policy/unsupported)
     :source-overrides (fn [] validation/source-overrides)
     :source-overrides-artifact (fn [] validation/source-overrides-artifact)
     :validate-overrides (fn [] shared/optimization-lowering-validate-overrides!)
     :pass-contract-record (fn [] shared/optimization-pass-contract-record)
     :decision-record (fn [] shared/optimization-decision-record)
     :diagnostic-catalog (fn [] diagnostic-catalog)
     :artifact-base (fn [] projection/artifact-base)
     :validate! (fn [] validation/validate!)
     :capability-proof (fn [] validation/capability-proof)
     :artifact-id (fn [] artifact-id)}))

(defn file-artifact [configuration path]
  (optimization/native-file-artifact configuration path
    {:invoke (fn [] operations/invoke)
     :source-artifact (fn [] source-artifact) :slurp (fn [] slurp)}))
