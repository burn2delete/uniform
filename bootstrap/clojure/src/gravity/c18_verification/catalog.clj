(ns gravity.c18-verification.catalog
  "Stable C18 document, diagnostic, and evidence-field catalogs."
  (:require [gravity.compiler-verification-shared :as shared]))

(def diagnostic-messages
  shared/compiler-verification-diagnostic-messages)

(def override-diagnostics
  shared/compiler-verification-override-diagnostics)

(def governing-document
  "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md")

(def diagnostic-ids
  ["C18-RISK" "C18-EVIDENCE" "C18-VALIDATION" "C18-PROOF"
   "C18-TRUST-REPORT" "C18-RELEASE-GATE" "C18-COUNTEREXAMPLE"
   "C18-PLUGIN" "C18-BACKEND"])

(def pass-risk-required-fields
  [:artifact :pass :version :risk :reason :affected-profiles
   :affected-targets :artifact-kinds :minimum-evidence :release-gate])

(def trust-report-required-fields
  [:artifact :compiler :profiles :targets :passes :artifact-kinds
   :known-gaps :release-gates :status])
