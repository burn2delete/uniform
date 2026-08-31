(ns gravity.optimization-lowering.diagnostics
  "Diagnostic catalogs and structured failure data for C13/C14.")

(def c13-optimization-diagnostic-ids
  ["C13-CONTRACT" "C13-PRESERVE" "C13-INVALIDATE" "C13-PROOF"
   "C13-CHECK-ELISION" "C13-EFFECT" "C13-SAFETY" "C13-DOMAIN"
   "C13-NONDETERMINISM" "C13-VERIFY"])

(def c14-lowering-diagnostic-ids
  ["C14-INPUT" "C14-PROFILE" "C14-TARGET" "C14-ABI" "C14-RUNTIME"
   "C14-PROVIDER" "C14-PROOF-METADATA" "C14-CAPABILITY"
   "C14-UNSUPPORTED" "C14-MANIFEST"])

(def diagnostic-ids
  (vec (concat c13-optimization-diagnostic-ids c14-lowering-diagnostic-ids)))

(def diagnostic-messages
  {"C13-CONTRACT" "MIR optimization pass contract is invalid"
   "C13-PRESERVE" "optimization claimed to preserve a missing or changed fact"
   "C13-INVALIDATE" "optimization is missing an invalidation record"
   "C13-PROOF" "optimization transformation lacks required proof evidence"
   "C13-CHECK-ELISION" "check elision violated PERF10 proof policy"
   "C13-EFFECT" "optimization reordered effects without evidence"
   "C13-SAFETY" "optimization left stale safety outcomes"
   "C13-DOMAIN" "optimization corrupted a domain anchor"
   "C13-NONDETERMINISM" "optimization choice is not replayable"
   "C13-VERIFY" "post-optimization MIR verifier failed"
   "C14-INPUT" "target lowering input is unverified or stale"
   "C14-PROFILE" "backend is ineligible under the active profile"
   "C14-TARGET" "target feature is missing or unsupported"
   "C14-ABI" "ABI or layout cannot represent the artifact"
   "C14-RUNTIME" "runtime service is missing or forbidden"
   "C14-PROVIDER" "provider support is missing"
   "C14-PROOF-METADATA" "target metadata lacks Gravity proof evidence"
   "C14-CAPABILITY" "lowering would add or lose authority"
   "C14-UNSUPPORTED" "MIR or domain feature lacks legal lowering"
   "C14-MANIFEST" "target artifact manifest is incomplete"})

(def override-diagnostics
  {:contract ["C13-CONTRACT" :optimization-pass]
   :preserve ["C13-PRESERVE" :optimization-decision]
   :invalidate ["C13-INVALIDATE" :invalidation-ledger]
   :proof ["C13-PROOF" :optimization-proof]
   :check-elision ["C13-CHECK-ELISION" :check-elision]
   :effect ["C13-EFFECT" :effect-scheduling]
   :safety ["C13-SAFETY" :safety-outcome]
   :domain ["C13-DOMAIN" :domain-anchor]
   :nondeterminism ["C13-NONDETERMINISM" :replay]
   :verify ["C13-VERIFY" :post-pass-verifier]
   :input ["C14-INPUT" :lowering-input]
   :profile ["C14-PROFILE" :target-eligibility]
   :target ["C14-TARGET" :target-feature]
   :abi ["C14-ABI" :abi-layout]
   :runtime ["C14-RUNTIME" :runtime-provider]
   :provider ["C14-PROVIDER" :provider-selection]
   :proof-metadata ["C14-PROOF-METADATA" :target-metadata]
   :capability ["C14-CAPABILITY" :capability-preservation]
   :unsupported ["C14-UNSUPPORTED" :unsupported-feature]
   :manifest ["C14-MANIFEST" :target-artifact-manifest]})

(defn failure-data
  [messages source-span id source-path artifact subject extra]
  [(get messages id "optimization or target lowering validation failed")
   (merge {:source-span (or (:source-span subject)
                            (get-in subject [:source :span])
                            (source-span source-path 0))
           :diagnostic-family :optimization-lowering
           :stage :optimize-lower
           :pass-id (or (:pass subject) (:pass-id subject))
           :decision-id (:decision-id subject)
           :input-artifact-id (or (:input-mir subject) (:input artifact))
           :output-artifact-id (:output-mir subject)
           :changed-operations (:changed-ops subject)
           :missing-fact (:missing-fact subject)
           :proof-id (or (:proof-id subject) (:proof-id extra))
           :profile (or (:profile subject)
                        (get-in artifact [:lowering-request :profile]))
           :target (or (:target subject)
                       (get-in artifact [:lowering-request :target :backend]))
           :backend (get-in artifact [:lowering-request :target :backend])
           :missing-feature (:missing-feature subject)
           :fallback-status (:fallback-status subject)
           :remediation "Regenerate optimization and lowering records with pass contracts, invalidation, verifier, proof, provider, capability, fallback, and target artifact evidence."}
          extra)])
