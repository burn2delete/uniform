

(def stage0-compiled-compiler-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-compiler-pass-contract.gravity"
    :diagnostic "C1-PASS-CONTRACT"
    :rejected-behavior :incomplete-compiled-pass-contract}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-compiler-evidence-drop.gravity"
    :diagnostic "C1-EVIDENCE-DROP"
    :rejected-behavior :durable-evidence-dropped-without-replacement}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-compiler-mir-target-leak.gravity"
    :diagnostic "C11-TARGET-LEAK"
    :rejected-behavior :target_specific_opcode_in_generic_mir}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-compiler-lowering-input.gravity"
    :diagnostic "C14-INPUT"
    :rejected-behavior :target_lowering_consumes_unverified_input}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-compiler-proof-metadata.gravity"
    :diagnostic "C14-PROOF-METADATA"
    :rejected-behavior :target_metadata_without_proof}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-compiler-verification-evidence.gravity"
    :diagnostic "C18-EVIDENCE"
    :rejected-behavior :high_risk_pass_without_required_evidence}])