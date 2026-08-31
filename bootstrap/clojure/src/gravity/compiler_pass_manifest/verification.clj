(ns gravity.compiler-pass-manifest.verification
  "Pass risk, trust report, and release gate defaults.")

(defn compiler-pass-default-risk-classification
  [contracts]
  (mapv (fn [contract]
          {:pass (:pass contract)
           :risk (:risk contract)
           :reason #{:stage0-pass-contract}
           :affected-profiles (:profiles contract)
           :affected-targets #{:jvm}
           :minimum-evidence (set (:evidence-class contract))
           :available-evidence (set (:evidence-class contract))
           :release-gate (if (#{:critical :high} (:risk contract))
                           :required
                           :verifier-only)})
        contracts))

(defn compiler-pass-default-trust-report
  [contracts risk-records]
  {:artifact :gravity/compiler-trust-report
   :compiler :gravity-stage0-clojure-bootstrap
   :passes (mapv #(select-keys % [:pass :risk :available-evidence])
                 risk-records)
   :profiles {:meta {:required-evidence :high
                     :blocked-passes []}}
   :known-gaps []
   :covered-passes (mapv :pass contracts)})

(def compiler-pass-default-release-gate-report
  {:artifact :gravity/compiler-release-gate
   :status :passed
   :evidence-gaps []
   :blocked-passes []
   :release-artifacts [:pass-contract-manifest]})
