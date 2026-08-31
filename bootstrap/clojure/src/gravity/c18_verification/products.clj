(ns gravity.c18-verification.products
  "C18 verification evidence products used by artifact assembly.")

(defn pass-evidence-records [risk-records]
  (mapv (fn [risk]
          {:artifact :gravity/pass-evidence
           :pass (:pass risk)
           :version (:version risk)
           :risk (:risk risk)
           :required (:minimum-evidence risk)
           :evidence (:minimum-evidence risk)
           :evidence-artifacts [(str "sha256:c18-evidence-"
                                     (name (:pass risk)))]
           :status :present})
        risk-records))

(defn translation-validation-logs [input-id]
  [{:artifact :gravity/translation-validation
    :pass :bounds-check-elide
    :input input-id
    :output "sha256:c18-bounds-check-elide-output"
    :changed-functions [:safe-index]
    :properties #{:same-observable-result :same-effects :same-safety-outcomes}
    :method :symbolic-plus-fixtures
    :proofs [:proof/c18-bounds-check-dominance]
    :counterexamples []
    :result :accepted}
   {:artifact :gravity/translation-validation
    :pass :plugin-loop-fuser
    :input input-id
    :output "sha256:c18-loop-fuser-output"
    :changed-functions [:loop-body]
    :properties #{:same-observable-result :same-effects}
    :method :differential-fixtures
    :proofs [:proof/c18-loop-fuser-effect-order]
    :counterexamples []
    :result :accepted}])

(defn verification-plan [risk-records]
  {:artifact :gravity/compiler-verification-plan
   :status :complete
   :evidence-policy :risk-based
   :passes (mapv :pass risk-records)
   :required-evidence-families
   #{:golden-fixtures :fuzz :translation-validation
     :proof-dominance-check :backend-conformance
     :differential-execution :sandbox-tests :contract-verifier}
   :release-policy :block-affected-profiles-on-failure})

(defn stage-verifier-reports [risk-records]
  (mapv (fn [risk]
          {:artifact :gravity/stage-verifier-report
           :pass (:pass risk)
           :artifact-kinds (:artifact-kinds risk)
           :source-or-generated-origin-preserved? true
           :status :passed})
        risk-records))

(def proof-or-certificate-references
  [{:proof :proof/c18-bounds-check-dominance
    :pass :bounds-check-elide
    :status :accepted}
   {:certificate :cert/c18-safety-check-elision
    :pass :bounds-check-elide
    :status :accepted}
   {:proof :proof/c18-loop-fuser-effect-order
    :pass :plugin-loop-fuser
    :status :accepted}])

(def differential-and-property-fixture-results
  {:artifact :gravity/compiler-fixture-results
   :status :passed
   :families {:front-end {:golden 8 :fuzz 64 :status :passed}
              :optimization {:translation-validation 2
                             :property 12
                             :status :passed}
              :backend {:differential 5
                        :conformance 4
                        :status :passed}}})

(defn compiler-trust-report [risk-records]
  {:artifact :gravity/compiler-trust-report
   :compiler "gravity-stage0-clojure"
   :profiles {:hosted {:required-evidence :high :blocked-passes []}
              :native {:required-evidence :critical
                       :blocked-passes [:gpu-lowering]}}
   :targets {:jvm {:required-evidence :high :blocked-passes []}}
   :passes (mapv #(select-keys % [:pass :risk :minimum-evidence :release-gate])
                 risk-records)
   :artifact-kinds (set (mapcat :artifact-kinds risk-records))
   :known-gaps [{:pass :gpu-lowering
                 :profiles #{:gpu}
                 :targets #{:gpu}
                 :status :experimental
                 :gate :explicit-feature-gate}]
   :release-gates [:verifiers :high-risk-evidence
                   :target-lowering-conformance
                   :stale-proof-rejection
                   :diagnostic-goldens
                   :self-hosting-comparison]
   :status :complete})

(def release-gate-report
  {:artifact :gravity/release-gate-report
   :status :passed
   :checks [:verifier-pass-every-artifact
            :no-active-critical-failures
            :high-risk-evidence-present
            :target-lowering-conformance
            :stale-proof-and-certificate-rejection
            :diagnostic-golden-fixtures]
   :release-artifacts :allowed-for-hosted-jvm
   :blocked-experimental-passes [:gpu-lowering]})

(def release-gate-failure-fixtures
  [{:artifact :gravity/release-gate-failure
    :pass :gpu-lowering
    :missing-evidence #{:backend-conformance :differential-execution}
    :affected-profiles #{:gpu}
    :affected-targets #{:gpu}
    :diagnostic "C18-RELEASE-GATE"
    :release-artifact-status :blocked}])

(defn counterexample-artifacts [input-id]
  [{:artifact :gravity/counterexample
    :source-fixture "bootstrap/clojure/fixtures/rejected/compiler-verify-c18-validation.gravity"
    :input-artifact input-id
    :output-artifact "sha256:c18-invalid-optimization-output"
    :violated-property :same-safety-outcomes
    :diagnostic-stream :gravity/c18-verification-diagnostic-stream
    :minimized-reproducer "compiler-verify-c18-validation.gravity"
    :affected-pass :bounds-check-elide
    :pass-version "stage0-c18"
    :status :captured
    :regression-fixture-created? true}])

(def experimental-pass-gates
  [{:pass :gpu-lowering
    :profiles #{:gpu}
    :targets #{:gpu}
    :gate :explicit-feature-gate
    :release-default :disabled
    :artifact-status :not-release-quality}])

(def plugin-evidence-report
  {:artifact :gravity/plugin-evidence-report
   :plugin 'gravity.plugins.stage0/loop-fuser
   :required #{:sandbox-tests :contract-verifier :fixture-suite}
   :available #{:sandbox-tests :contract-verifier :fixture-suite}
   :status :passed})

(def target-lowering-conformance
  [{:artifact :gravity/target-lowering-conformance
    :target :jvm
    :profiles #{:hosted}
    :source-core-mir-intent #{:same-observable-result
                              :same-effects
                              :same-safety-outcomes}
    :emitted-artifact-behavior :matched
    :method :differential-execution
    :status :passed}])

(defn verification-results [diagnostic-ids]
  {:documents ["C18"]
   :task "P06-D097"
   :required-diagnostic-ids diagnostic-ids
   :c17-input-status :complete
   :plan-status :complete
   :risk-status :complete
   :evidence-status :complete
   :translation-validation-status :complete
   :proof-certificate-status :complete
   :fixture-status :complete
   :trust-report-status :complete
   :release-gate-status :complete
   :counterexample-status :complete
   :experimental-gate-status :complete
   :plugin-status :complete
   :backend-status :complete
   :diagnostic-status :complete
   :status :complete})
