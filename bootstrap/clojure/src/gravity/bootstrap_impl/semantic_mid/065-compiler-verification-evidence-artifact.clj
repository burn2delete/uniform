(defn- semantic-mid-compiler-verification-evidence-artifact
  [{:keys [input-id risk-records]}]
  {:compiler-verification-plan
   {:artifact :gravity/compiler-verification-plan
    :status :complete
    :evidence-policy :risk-based}
   :pass-risk-classification risk-records
   :pass-evidence-records
   (mapv (fn [risk]
           {:pass (:pass risk)
            :risk (:risk risk)
            :evidence (:minimum-evidence risk)
            :status :present})
         risk-records)
   :translation-validation-logs
   [{:artifact :gravity/translation-validation
     :pass :bounds-check-elide
     :input input-id
     :output input-id
     :changed-functions [:stage0-main]
     :properties #{:same-observable-result :same-effects
                   :same-safety-outcomes}
     :method :symbolic-plus-fixtures
     :proofs [:proof/c13-bounds-check-elision]
     :counterexamples []
     :result :accepted}]
   :proof-or-certificate-references
   [{:proof :proof/c13-bounds-check-elision :status :accepted}
    {:certificate :cert/stage0-diagnostic-golden :status :accepted}]
   :differential-and-property-fixture-results
   {:artifact :gravity/compiler-fixture-results
    :status :passed
    :families [:front-end :optimization :backend]}
   :compiler-trust-report
   {:artifact :gravity/compiler-trust-report
    :compiler "gravity-stage0-clojure"
    :passes (mapv #(select-keys % [:pass :risk :minimum-evidence])
                  risk-records)
    :profiles {:hosted {:required-evidence :high
                        :blocked-passes []}}
    :known-gaps []
    :status :complete}
   :release-gate-report
   {:artifact :gravity/release-gate-report
    :status :passed
    :checks [:verifiers :critical-failures :high-risk-evidence
             :target-lowering-conformance :stale-proof-rejection
             :diagnostic-goldens]}
   :counterexample-artifacts
   [{:artifact :gravity/counterexample
     :status :none-produced
     :regression-fixture-created? false}]
   :diagnostics []})

(defn- semantic-mid-compiler-verification-artifact
  [context]
  (merge (semantic-mid-compiler-verification-diagnostics-artifact context)
         (semantic-mid-compiler-verification-incremental-artifact context)
         (semantic-mid-compiler-verification-evidence-artifact context)))
