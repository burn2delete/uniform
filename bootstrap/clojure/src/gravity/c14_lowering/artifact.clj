(ns gravity.c14-lowering.artifact
  (:require [gravity.c14-lowering.operations :as operations]
            [gravity.c14-lowering.policy :as policy]
            [gravity.c14-lowering.projection :as projection]
            [gravity.c14-lowering.validation :as validation]
            [gravity.digest :as digest]
            [gravity.optimization-lowering :as shared]))
(defn- artifact-id [artifact]
  (operations/invoke :c4-artifact-id
                     (fn [value] (str "sha256:" (digest/sha256-hex (pr-str value)))) artifact))
(defn- source-span [path index]
  (operations/invoke :source-span (fn [p i] {:source p :form-index i}) path index))
(defn- diagnostic-catalog [configuration source-path input-id]
  (operations/invoke :c14-lowering-diagnostic-catalog
                     (fn [path identifier] (validation/diagnostic-catalog configuration path identifier source-span))
                     source-path input-id))
(defn source-artifact [configuration source-path source-text]
  (let [records (operations/invoke :read-source-form-records (policy/unsupported :read-source-form-records)
                                   source-path source-text)
        forms (mapv :form records)
        _ (operations/invoke :validate-ns-syntax! (policy/unsupported :validate-ns-syntax!) source-path forms)
        module (operations/invoke :parse-module (policy/unsupported :parse-module) source-path forms)
        source-overrides (operations/invoke :c14-lowering-source-overrides validation/source-overrides module)
        _ (operations/invoke :c14-lowering-validate-source-overrides!
                             (fn [path overrides]
                               (operations/invoke :optimization-lowering-validate-overrides!
                                                  shared/optimization-lowering-validate-overrides!
                                                  path (validation/source-overrides-artifact overrides)))
                             source-path source-overrides)
        optimization-artifact (operations/invoke :compiler-c13-optimization-source-artifact
                                                 (policy/unsupported :compiler-c13-optimization-source-artifact)
                                                 source-path source-text)
        input-id (:artifact-id optimization-artifact)
        diagnostics (diagnostic-catalog configuration source-path input-id)
        artifact-base (projection/artifact-base configuration source-path source-text module source-overrides
                                               optimization-artifact diagnostics)
        _ (operations/invoke :c14-lowering-validate!
                             (fn [path artifact] (validation/validate! configuration path artifact))
                             source-path artifact-base)
        capability-proof (operations/invoke :c14-lowering-capability-proof
                                            (fn [artifact] (validation/capability-proof configuration artifact))
                                            artifact-base)]
    (assoc artifact-base :capability-based-proof capability-proof
           :artifact-id (artifact-id (assoc artifact-base :capability-based-proof capability-proof)))))
(defn file-artifact [configuration path]
  (operations/invoke :compiler-c14-lowering-source-artifact
                     (fn [source-path source-text] (source-artifact configuration source-path source-text))
                     path (slurp path)))
