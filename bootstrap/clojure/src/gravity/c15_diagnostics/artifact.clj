(ns gravity.c15-diagnostics.artifact
  (:require [gravity.diagnostics :as stage]
            [gravity.c15-diagnostics.operations :as operations]
            [gravity.c15-diagnostics.policy :as policy]
            [gravity.c15-diagnostics.projection :as projection]
            [gravity.c15-diagnostics.proof :as proof]
            [gravity.c15-diagnostics.records :as records]
            [gravity.c15-diagnostics.validation :as validation]
            [gravity.digest :as digest]))
(defn- artifact-id [x]
  (operations/invoke :c4-artifact-id (fn [v] (str "sha256:" (digest/sha256-hex (pr-str v)))) x))
(defn- diagnostic-record [& args]
  (apply operations/invoke :c15-diagnostic-record records/diagnostic-record args))
(defn- diagnostics [path lowering-id module]
  (stage/native-fixture-diagnostics path lowering-id module
    {:diagnostic-record (fn [] diagnostic-record) :source-span (fn [] records/source-span)}))

(defn source-artifact [configuration source-path source-text]
  (stage/native-source-artifact configuration source-path source-text
    {:invoke (fn [] operations/invoke) :unsupported (fn [] policy/unsupported)
     :source-overrides (fn [] records/source-overrides)
     :validate-source-overrides! (fn [] validation/validate-source-overrides!)
     :fixture-diagnostics (fn [] diagnostics) :catalog (fn [] records/catalog)
     :artifact-base (fn [] projection/artifact-base) :validate! (fn [] validation/validate!)
     :capability-proof (fn [] proof/capability-proof) :artifact-id (fn [] artifact-id)}))

(defn file-artifact [configuration path]
  (stage/native-file-artifact configuration path
    {:invoke (fn [] operations/invoke) :source-artifact (fn [] source-artifact)
     :slurp (fn [] slurp)}))
