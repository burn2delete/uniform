(ns gravity.c14-lowering.artifact
  (:require [gravity.lowering :as lowering]
            [gravity.c14-lowering.operations :as operations]
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
  (lowering/native-source-artifact configuration source-path source-text
    {:invoke (fn [] operations/invoke) :unsupported (fn [] policy/unsupported)
     :source-overrides (fn [] validation/source-overrides)
     :source-overrides-artifact (fn [] validation/source-overrides-artifact)
     :validate-overrides (fn [] shared/optimization-lowering-validate-overrides!)
     :diagnostic-catalog (fn [] diagnostic-catalog)
     :artifact-base (fn [] projection/artifact-base)
     :validate! (fn [] validation/validate!)
     :capability-proof (fn [] validation/capability-proof)
     :artifact-id (fn [] artifact-id)}))

(defn file-artifact [configuration path]
  (lowering/native-file-artifact configuration path
    {:invoke (fn [] operations/invoke)
     :source-artifact (fn [] source-artifact) :slurp (fn [] slurp)}))
