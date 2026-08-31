(ns gravity.compiler-pass-manifest.diagnostics
  "Compiler pass diagnostic identifiers, schema, catalog, and fixtures."
  (:require [clojure.string :as str]))

(def ^:private c1-diagnostic-ids
  ["C1-PIPELINE" "C1-PASS-CONTRACT" "C1-EVIDENCE-DROP"
   "C1-UNCHECKED-BACKEND" "C1-MANIFEST"])

(def ^:private c15-diagnostic-ids
  ["C15-SCHEMA" "C15-ID" "C15-SPAN" "C15-ORIGIN" "C15-FACTS"
   "C15-REMEDIATION" "C15-REDACTION" "C15-ORDER"])

(def ^:private c16-diagnostic-ids
  ["C16-KEY" "C16-ENTRY" "C16-PROOF" "C16-SPECULATIVE"])

(def ^:private c17-diagnostic-ids
  ["C17-MANIFEST" "C17-API" "C17-CAPABILITY"
   "C17-PASS-CONTRACT" "C17-OUTPUT"])

(def ^:private c18-diagnostic-ids
  ["C18-RISK" "C18-EVIDENCE" "C18-TRUST-REPORT"
   "C18-RELEASE-GATE"])

(def compiler-pass-diagnostic-ids
  (vec (concat c1-diagnostic-ids c15-diagnostic-ids c16-diagnostic-ids
               c17-diagnostic-ids c18-diagnostic-ids)))


(def compiler-pass-default-diagnostic-schema
  {:artifact :gravity/diagnostic-schema
   :required-fields [:rule :severity :primary :related :origin-chain :stage
                     :profile :target :artifacts :facts :remediation
                     :redactions :ordering-key]
   :supported-severities [:error :warning :info :hint :internal-error]
   :renderers [:cli :ide :ci]
   :deterministic-ordering? true
   :secret-redaction? true})

(def compiler-pass-default-diagnostic-catalog
  (mapv (fn [id]
          {:rule id
           :severity :error
           :message-key (keyword "compiler" (str/lower-case id))
           :lifecycle :active})
        compiler-pass-diagnostic-ids))

(def compiler-pass-default-diagnostic-fixtures
  [{:rule "C1-PASS-CONTRACT"
    :diagnostic-id "diag/c1-pass-contract-stage0"
    :severity :error
    :stage :pass-contract-validate
    :primary {:span "compiler/passes.gravity:1:1"
              :artifact :pass-contract/build-mir}
    :related [{:role :pass-contract
               :artifact :pass-contract/build-mir}]
    :origin-chain [{:kind :source
                    :span "compiler/passes.gravity:1:1"}]
    :profile :meta
    :target :jvm
    :artifacts [:pass-contract/build-mir]
    :facts {:missing-field :output}
    :remediation [{:kind :complete-pass-contract}]
    :redactions []
    :secret-free? true
    :ordering-key ["C1" 1]}
   {:rule "C15-ORIGIN"
    :diagnostic-id "diag/c15-origin-generated-stage0"
    :severity :error
    :stage :diagnostic-validate
    :primary {:span "generated:gravity.compiler/pass:1"
              :artifact :diagnostic/generated}
    :related [{:role :generated-by
               :span "compiler/passes.gravity:2:1"}]
    :origin-chain [{:kind :generated
                    :producer :gravity.compiler/pass
                    :inputs [:syntax/pass-contract]}]
    :profile :meta
    :target :jvm
    :artifacts [:diagnostic/generated]
    :facts {:origin-chain :present}
    :remediation [{:kind :preserve-generated-origin}]
    :redactions []
    :secret-free? true
    :ordering-key ["C15" 1]}])
