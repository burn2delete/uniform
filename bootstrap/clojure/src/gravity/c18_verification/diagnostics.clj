(ns gravity.c18-verification.diagnostics
  "C18 source overrides and diagnostic stream projection."
  (:require [clojure.string :as str]))

(defn source-overrides [module]
  (or (get-in module [:metadata :compiler :c18-verification])
      (get-in module [:metadata :compiler :verification])
      {}))

(defn fail!
  [fail-operation diagnostic-messages source-span
   id source-path subject extra]
  (fail-operation
   id
   (get diagnostic-messages id
        "compiler verification/pass-correctness validation failed")
   (merge {:source-span (or (:source-span subject)
                            (source-span source-path 0))
           :diagnostic-family :compiler-pass-correctness
           :stage (or (:stage subject) :c18-compiler-verification)
           :pass-id (:pass-id subject)
           :version (:version subject)
           :risk-class (:risk-class subject)
           :required-evidence (:required-evidence subject)
           :available-evidence (:available-evidence subject)
           :affected-profiles (:affected-profiles subject)
           :affected-targets (:affected-targets subject)
           :artifact-id (:artifact-id subject)
           :remediation "Regenerate compiler verification evidence with risk classification, required evidence records, translation validation, proof or certificate replay, trust reports, release gates, counterexamples, plugin evidence, and backend conformance."}
          extra)))

(defn validate-source-overrides!
  [verification-fail! override-diagnostics diagnostic-ids
   source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get override-diagnostics fail-kind)]
      (when (contains? (set diagnostic-ids) id)
        (verification-fail!
         id source-path
         {:stage subject-kind
          :pass-id subject-kind
          :version "stage0-c18"
          :risk-class :high
          :required-evidence #{:translation-validation
                               :proof-certificate
                               :conformance-fixtures}
          :available-evidence #{}
          :affected-profiles #{:hosted :native}
          :affected-targets #{:jvm}
          :artifact-id (str "c18-verification-artifact-" (name fail-kind))}
         {:missing-fields [fail-kind]})))))

(defn diagnostic-stream [source-span diagnostic-ids source-path input-id]
  {:artifact :gravity/c18-verification-diagnostic-stream
   :stage :c18-compiler-verification
   :input-artifact input-id
   :ordering-key [:rule :pass :risk]
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :c18-compiler-verification
            :message-key (keyword "verification"
                                  (str/lower-case
                                   (str/replace id #"_" "-")))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "c18-verification-syntax-" index)
                      :artifact input-id}
            :related [{:role :generated-origin
                       :span (source-span source-path index)
                       :artifact :compiler-generated-verification-record}]
            :origin-chain [{:producer :c18-compiler-verification
                            :source (source-span source-path index)
                            :generated-artifact input-id}]
            :pass-id (case id
                       "C18-BACKEND" :target-lowering
                       "C18-PLUGIN" :plugin-loop-fuser
                       "C18-VALIDATION" :bounds-check-elide
                       :compiler-verification)
            :version "stage0-c18"
            :risk-class (if (#{"C18-RISK" "C18-TRUST-REPORT"
                                "C18-RELEASE-GATE"} id)
                          :critical
                          :high)
            :required-evidence #{:translation-validation
                                 :proof-or-certificate
                                 :conformance-fixtures}
            :available-evidence #{:fixtures}
            :affected-profiles #{:hosted :native}
            :affected-targets #{:jvm}
            :source-or-artifact-id input-id
            :facts {:rule id
                    :release-blocking? true
                    :evidence-policy :risk-based}
            :remediation [{:kind :add-required-evidence}
                          {:kind :block-release-artifact}]
            :redactions []
            :ordering-key [id :compiler-verification :high]})
         diagnostic-ids
         (range))
   :status :complete})
