(ns gravity.c15-diagnostics.artifact
  (:require [gravity.c15-diagnostics.operations :as operations]
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
(defn- diagnostics [path lowering-id]
  (vec (sort-by :ordering-key
                [(diagnostic-record "C15-FACTS" :info :c15-compiler-diagnostics :diagnostic.structured-facts path 0 lowering-id
                                    {:fact-families [:types :effects :capabilities :safety :proofs :target-features]
                                     :artifact lowering-id} [{:kind :inspect-facts}])
                 (diagnostic-record "C15-ORIGIN" :warning :c15-compiler-diagnostics :diagnostic.generated-origin path 1 lowering-id
                                    {:generated-form "c15-generated-check" :producer :compiler-c15-diagnostics
                                     :source-producer :stage0-build-macro} [{:kind :jump-to-source-producer}]
                                    :generated? true
                                    :origin-chain [{:producer :stage0-build-macro :source (records/source-span path 1)
                                                    :generated-artifact lowering-id}]
                                    :related [{:role :generated-by :span (records/source-span path 1)
                                               :artifact :stage0-build-macro}])
                 (diagnostic-record "C15-REDACTION" :error :c15-compiler-diagnostics :diagnostic.redaction-policy path 2 lowering-id
                                    {:redacted-fields [:credential-value :private-expansion] :policy :public-diagnostic}
                                    [{:kind :move-to-private-artifact-store}]
                                    :redactions [{:field :credential-value :replacement :redacted
                                                  :value-hash "sha256:redacted-stage0"}])
                 (diagnostic-record "C15-GOLDEN" :hint :c15-compiler-diagnostics :diagnostic.golden-fixture path 3 lowering-id
                                    {:fixture :compiler-c15-diagnostics
                                     :asserts [:rule :severity :primary :related :facts :remediation :redactions :ordering]}
                                    [{:kind :regenerate-golden-fixture}])])))
(defn source-artifact [configuration source-path source-text]
  (let [records* (operations/invoke :read-source-form-records (policy/unsupported :read-source-form-records) source-path source-text)
        forms (mapv :form records*)
        _ (operations/invoke :validate-ns-syntax! (policy/unsupported :validate-ns-syntax!) source-path forms)
        module (operations/invoke :parse-module (policy/unsupported :parse-module) source-path forms)
        source-overrides (operations/invoke :c15-diagnostics-source-overrides records/source-overrides module)
        _ (operations/invoke :c15-diagnostics-validate-source-overrides!
                             (fn [path values] (validation/validate-source-overrides! configuration path values))
                             source-path source-overrides)
        lowering-artifact (operations/invoke :compiler-c14-lowering-source-artifact
                                              (policy/unsupported :compiler-c14-lowering-source-artifact) source-path source-text)
        lowering-id (:artifact-id lowering-artifact)
        diagnostics* (diagnostics source-path lowering-id)
        catalog (operations/invoke :c15-diagnostic-catalog (fn [] (records/catalog configuration)))
        base (projection/artifact-base configuration module source-overrides lowering-artifact diagnostics* catalog)
        _ (operations/invoke :c15-diagnostics-validate! (fn [path value] (validation/validate! configuration path value)) source-path base)
        capability-proof (operations/invoke :c15-diagnostics-capability-proof
                                            (fn [value] (proof/capability-proof configuration value)) base)]
    (assoc base :capability-based-proof capability-proof
           :artifact-id (artifact-id (assoc base :capability-based-proof capability-proof)))))
(defn file-artifact [configuration path]
  (operations/invoke :compiler-c15-diagnostics-source-artifact
                     (fn [source-path source-text] (source-artifact configuration source-path source-text)) path (slurp path)))
