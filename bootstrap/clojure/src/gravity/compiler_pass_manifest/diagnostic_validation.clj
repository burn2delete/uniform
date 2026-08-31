(ns gravity.compiler-pass-manifest.diagnostic-validation
  "Diagnostic schema and deterministic stream validation."
  (:require [clojure.set :as set]
            [gravity.compiler-pass-manifest.diagnostics :as diagnostic-data]
            [gravity.compiler-pass-manifest.failures :as failures]
            [gravity.compiler-pass-manifest.support :as support]))

(defn compiler-pass-validate-diagnostics!
  [source-path manifest suite]
  (let [schema (:diagnostic-schema suite)
        required-fields (:required-fields diagnostic-data/compiler-pass-default-diagnostic-schema)
        schema-fields (set (:required-fields schema))]
    (when-not (set/subset? (set required-fields) schema-fields)
      (failures/compiler-pass-fail! "C15-SCHEMA" source-path manifest schema
                           {:missing-fields (vec (set/difference
                                                  (set required-fields)
                                                  schema-fields))
                            :schema-field :required-fields
                            :remediation "Diagnostic schemas must include stable ids, locations, origins, facts, remediation, redaction, and ordering fields."})))
  (let [rules (map :rule (:diagnostic-catalog suite))]
    (when (not= (count rules) (count (distinct rules)))
      (failures/compiler-pass-fail! "C15-ID" source-path manifest
                           {:stage :diagnostic-catalog}
                           {:remediation "Diagnostic ids must remain unique and stable across wording changes."})))
  (doseq [diagnostic (:diagnostic-fixtures suite)]
    (when-not (support/present? (get-in diagnostic [:primary :span]))
      (failures/compiler-pass-fail! "C15-SPAN" source-path manifest diagnostic
                           {:schema-field :primary
                            :remediation "Diagnostics must include a primary source, generated, manifest, MIR, domain, or artifact location."}))
    (when-not (support/present? (:origin-chain diagnostic))
      (failures/compiler-pass-fail! "C15-ORIGIN" source-path manifest diagnostic
                           {:schema-field :origin-chain
                            :remediation "Generated and downstream diagnostics must preserve origin chains."}))
    (when-not (support/present? (:facts diagnostic))
      (failures/compiler-pass-fail! "C15-FACTS" source-path manifest diagnostic
                           {:schema-field :facts
                            :remediation "Diagnostic facts must be structured fields, not prose-only text."}))
    (when (and (= :error (:severity diagnostic))
               (not (support/present? (:remediation diagnostic))))
      (failures/compiler-pass-fail! "C15-REMEDIATION" source-path manifest diagnostic
                           {:schema-field :remediation
                            :remediation "Actionable diagnostics need structured remediation categories."}))
    (when-not (true? (:secret-free? diagnostic))
      (failures/compiler-pass-fail! "C15-REDACTION" source-path manifest diagnostic
                           {:schema-field :redactions
                            :remediation "Redact secret values while preserving fixable diagnostic structure."})))
  (let [fixtures (:diagnostic-fixtures suite)]
    (when-not (= fixtures (sort-by :ordering-key fixtures))
      (failures/compiler-pass-fail! "C15-ORDER" source-path manifest
                           {:stage :diagnostic-stream}
                           {:remediation "Diagnostic streams must be deterministically ordered by stable semantic keys."})))
  :complete)
