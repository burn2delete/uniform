(ns gravity.c18-verification.risks
  "Stable C18 pass-risk classification records.")

(defn pass-risk-records []
  [{:artifact :gravity/pass-risk
    :pass :reader :version "stage0-c18" :risk :critical
    :reason #{:trusted-semantic-base}
    :affected-profiles #{:core :hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:syntax-object-stream}
    :minimum-evidence #{:golden-fixtures :round-trip :fuzz}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :macro-expansion :version "stage0-c18" :risk :critical
    :reason #{:generated-code :build-effects}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:macro-expansion-trace :expanded-syntax}
    :minimum-evidence #{:hygiene-fixtures :generated-origin-fixtures
                        :build-effect-tests}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :type-effect-check :version "stage0-c18" :risk :high
    :reason #{:semantic-legality :effect-capability-policy}
    :affected-profiles #{:hosted :native :kernel}
    :affected-targets #{:jvm}
    :artifact-kinds #{:typed-core :effect-graph}
    :minimum-evidence #{:positive-negative-fixtures :property-tests}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :ownership-safety :version "stage0-c18" :risk :critical
    :reason #{:safety-outcomes :unsafe-boundary}
    :affected-profiles #{:hosted :native :kernel :firmware}
    :affected-targets #{:jvm}
    :artifact-kinds #{:ownership-graph :safety-report}
    :minimum-evidence #{:vulnerability-fixtures :proof-certificate-checks}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :mir-construction :version "stage0-c18" :risk :high
    :reason #{:core-to-mir-semantics}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:gravity/mir}
    :minimum-evidence #{:mir-verifier :core-to-mir-goldens}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :bounds-check-elide :version "stage0-c18" :risk :high
    :reason #{:removes-runtime-checks :depends-on-proof}
    :affected-profiles #{:native :kernel :firmware :gpu}
    :affected-targets #{:jvm :native}
    :artifact-kinds #{:optimized-mir :safety-outcome}
    :minimum-evidence #{:translation-validation :proof-dominance-check}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :target-lowering :version "stage0-c18" :risk :critical
    :reason #{:emits-backend-artifacts :profile-runtime-contract}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:target-artifact-manifest}
    :minimum-evidence #{:backend-conformance :differential-execution}
    :release-gate :required}
   {:artifact :gravity/pass-risk
    :pass :plugin-loop-fuser :version "stage0-c18" :risk :medium
    :reason #{:plugin-ir-transform}
    :affected-profiles #{:hosted :native}
    :affected-targets #{:jvm}
    :artifact-kinds #{:plugin-output-artifact}
    :minimum-evidence #{:sandbox-tests :contract-verifier :fixture-suite}
    :release-gate :required}])
