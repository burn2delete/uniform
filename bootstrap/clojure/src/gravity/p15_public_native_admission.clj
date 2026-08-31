(ns gravity.p15-public-native-admission
  "Bootstrap-only, fail-closed consumer seam for the P15/W4 boundary.

  The public namespace remains the stable test and consumer facade. Semantic
  implementation details live in the same-named component directory."
  (:require [gravity.p15-public-native-admission.contract :as contract]
            [gravity.p15-public-native-admission.decision :as decision]
            [gravity.p15-public-native-admission.evidence-contract :as evidence]
            [gravity.p15-public-native-admission.handoff-validation :as handoff]
            [gravity.p15-public-native-admission.observation-handoff-validation :as observation-handoff]
            [gravity.p15-public-native-admission.observation-replay-validation :as observation-replay]
            [gravity.p15-public-native-admission.observation-validation :as observation]
            [gravity.p15-public-native-admission.pin-validation :as pin]
            [gravity.p15-public-native-admission.producer-contract :as producer]
            [gravity.p15-public-native-admission.public-api :as api]
            [gravity.p15-public-native-admission.replay-contract :as replay]
            [gravity.p15-public-native-admission.replay-issue-lookup :as replay-issues]
            [gravity.p15-public-native-admission.replay-observation-structure :as replay-observation]
            [gravity.p15-public-native-admission.replay-pin-structure :as replay-pin]
            [gravity.p15-public-native-admission.replay-policy-validation :as replay-policy]
            [gravity.p15-public-native-admission.validation-support :as support]
            [gravity.p15-public-native-admission.w1-binding-validation :as w1]
            [gravity.p15-public-native-admission.w2-binding-validation :as w2]
            [gravity.p15-public-native-admission.w3-binding-validation :as w3]))

;; Stable private seams retained for focused hostile-input tests.
(def ^:private p18-id producer/p18-id)
(def ^:private request-artifact producer/request-artifact)
(def ^:private request-schema producer/request-schema)
(def ^:private admission-artifact producer/admission-artifact)
(def ^:private admission-schema producer/admission-schema)
(def ^:private contract-id producer/contract-id)
(def ^:private contract-version producer/contract-version)
(def ^:private producer-order producer/producer-order)
(def ^:private source-extensions producer/source-extensions)
(def ^:private supported-target producer/supported-target)
(def ^:private supported-target-tier producer/supported-target-tier)
(def ^:private producer-policies producer/producer-policies)
(def ^:private replay-policy-keys replay/replay-policy-keys)
(def ^:private replay-policy-owner-keys replay/replay-policy-owner-keys)
(def ^:private replay-policies replay/replay-policies)
(def ^:private replay-owner-blockers replay/replay-owner-blockers)
(def ^:private replay-diagnostic-order replay/replay-diagnostic-order)
(def ^:private replay-structure-diagnostic-order replay/replay-structure-diagnostic-order)
(def ^:private future-request-v2 replay/future-request-v2)
(def ^:private w6-payload-containing-commit-registry replay/w6-payload-containing-commit-registry)
(def ^:private pin-keys evidence/pin-keys)
(def ^:private observation-keys evidence/observation-keys)
(def ^:private consumer-handoff-keys evidence/consumer-handoff-keys)
(def ^:private verifier-keys evidence/verifier-keys)
(def ^:private review-keys evidence/review-keys)
(def ^:private claims-keys evidence/claims-keys)
(def ^:private w1-json-key-serialization evidence/w1-json-key-serialization)
(def ^:private binding-key-sets evidence/binding-key-sets)
(def ^:private identity-binding-keys evidence/identity-binding-keys)
(def ^:private os-gate-keys evidence/os-gate-keys)
(def ^:private process-tree-containment-keys evidence/process-tree-containment-keys)
(def ^:private w1-provenance-edges-keys evidence/w1-provenance-edges-keys)
(def ^:private abi-keys evidence/abi-keys)
(def ^:private w2-provider-executable-path evidence/w2-provider-executable-path)
(def ^:private w2-packet-schema evidence/w2-packet-schema)
(def ^:private w2-inherited-fds evidence/w2-inherited-fds)
(def ^:private w2-effects-keys evidence/w2-effects-keys)
(def ^:private w2-capabilities-keys evidence/w2-capabilities-keys)
(def ^:private w2-residual-authority evidence/w2-residual-authority)
(def ^:private w2-rejected-diagnostic-ids evidence/w2-rejected-diagnostic-ids)
(def ^:private w3-receipt-schema evidence/w3-receipt-schema)
(def ^:private w3-timeout-policy evidence/w3-timeout-policy)
(def ^:private w3-signal-policy evidence/w3-signal-policy)
(def ^:private w3-output-policy evidence/w3-output-policy)
(def ^:private w3-resource-policy evidence/w3-resource-policy)
(def ^:private w3-cleanup-policy evidence/w3-cleanup-policy)
(def ^:private w3-unsupported-platforms evidence/w3-unsupported-platforms)
(def ^:private w3-negative-guarantees evidence/w3-negative-guarantees)
(def ^:private w3-accepted-diagnostic-ids evidence/w3-accepted-diagnostic-ids)
(def ^:private w3-rejected-diagnostic-ids evidence/w3-rejected-diagnostic-ids)
(def ^:private namespace-contract contract/namespace-contract)

(def public-native-admission-contract contract/public-native-admission-contract)
(def default-public-native-admission contract/default-public-native-admission)

(def ^:private exact-keys? support/exact-keys?)
(def ^:private identifier-text support/identifier-text)
(def ^:private identifier? support/identifier?)
(def ^:private same-identity? support/same-identity?)
(def ^:private sha256? support/sha256?)
(def ^:private commit? support/commit?)
(def ^:private visible-ascii-string? support/visible-ascii-string?)
(def ^:private exact-ascii-keyword? support/exact-ascii-keyword?)
(def ^:private positive-integer? support/positive-integer?)
(def ^:private normalized-repo-relative-posix-path? support/normalized-repo-relative-posix-path?)
(def ^:private derive-checkout-root-id support/derive-checkout-root-id)
(def ^:private relative-path? support/relative-path?)
(def ^:private nonempty-evidence? support/nonempty-evidence?)
(def ^:private exact-structured-values? support/exact-structured-values?)
(def ^:private os-gate-target support/os-gate-target)
(def ^:private issue support/issue)
(def ^:private append-issue support/append-issue)
(def ^:private replay-policy-missing-fields-exact? replay-policy/replay-policy-missing-fields-exact?)
(def ^:private replay-policy-valid? replay-policy/replay-policy-valid?)
(def ^:private replay-policy-table-valid? replay-policy/replay-policy-table-valid?)
(def ^:private validate-replay-policy replay-policy/validate-replay-policy)
(def ^:private validate-replay-pin-structure replay-pin/validate-replay-pin-structure)
(def ^:private validate-replay-observation-structure replay-observation/validate-replay-observation-structure)
(def ^:private replay-pin-issue-for-code replay-issues/replay-pin-issue-for-code)
(def ^:private replay-observation-issue-for-code replay-issues/replay-observation-issue-for-code)
(def ^:private first-replay-structure-issue replay-issues/first-replay-structure-issue)
(def ^:private validate-pin pin/validate-pin)
(def ^:private validate-verifier handoff/validate-verifier)
(def ^:private validate-review handoff/validate-review)
(def ^:private validate-claims handoff/validate-claims)
(def ^:private validate-collection-evidence w1/validate-collection-evidence)
(def ^:private validate-w1-bindings w1/validate-w1-bindings)
(def ^:private validate-w2-bindings w2/validate-w2-bindings)
(def ^:private validate-w3-bindings w3/validate-w3-bindings)
(def ^:private validate-observation observation/validate-observation)
(def ^:private validate-cross-bindings decision/validate-cross-bindings)
(def ^:private base-decision decision/base-decision)
(def ^:private decision-with-issues decision/decision-with-issues)
(def ^:private request-has-no-dependency-evidence? decision/request-has-no-dependency-evidence?)
(def ^:private validate-request-shape decision/validate-request-shape)

(defn validate-public-native-admission
  "Reject synthetic W1/W2/W3 v1 replay requests without performing I/O."
  [request]
  (binding [replay/replay-policies replay-policies]
    (api/validate-public-native-admission request)))

(defn public-native-admission?
  "Return true only for a separately admitted tracked public route."
  [request]
  (true? (:bounded-native-route-admitted?
          (if (map? request)
            (validate-public-native-admission request)
            default-public-native-admission))))

(defn verified-public-route-handoff?
  "Fail-closed v1 seam for the future reviewed W4 route artifact."
  [_route]
  false)

;; Alias vars keep the original facade metadata as well as its values. This is
;; important for tests and tooling that inspect private arglists or API docs.
(doseq [[facade-var component-var]
        [[#'public-native-admission-contract #'contract/public-native-admission-contract]
         [#'default-public-native-admission #'contract/default-public-native-admission]
         [#'exact-keys? #'support/exact-keys?]
         [#'identifier-text #'support/identifier-text]
         [#'identifier? #'support/identifier?]
         [#'same-identity? #'support/same-identity?]
         [#'sha256? #'support/sha256?]
         [#'commit? #'support/commit?]
         [#'visible-ascii-string? #'support/visible-ascii-string?]
         [#'exact-ascii-keyword? #'support/exact-ascii-keyword?]
         [#'positive-integer? #'support/positive-integer?]
         [#'normalized-repo-relative-posix-path? #'support/normalized-repo-relative-posix-path?]
         [#'derive-checkout-root-id #'support/derive-checkout-root-id]
         [#'relative-path? #'support/relative-path?]
         [#'nonempty-evidence? #'support/nonempty-evidence?]
         [#'exact-structured-values? #'support/exact-structured-values?]
         [#'os-gate-target #'support/os-gate-target]
         [#'issue #'support/issue]
         [#'append-issue #'support/append-issue]
         [#'replay-policy-missing-fields-exact? #'replay-policy/replay-policy-missing-fields-exact?]
         [#'replay-policy-valid? #'replay-policy/replay-policy-valid?]
         [#'replay-policy-table-valid? #'replay-policy/replay-policy-table-valid?]
         [#'validate-replay-policy #'replay-policy/validate-replay-policy]
         [#'validate-replay-pin-structure #'replay-pin/validate-replay-pin-structure]
         [#'validate-replay-observation-structure #'replay-observation/validate-replay-observation-structure]
         [#'replay-pin-issue-for-code #'replay-issues/replay-pin-issue-for-code]
         [#'replay-observation-issue-for-code #'replay-issues/replay-observation-issue-for-code]
         [#'first-replay-structure-issue #'replay-issues/first-replay-structure-issue]
         [#'validate-pin #'pin/validate-pin]
         [#'validate-verifier #'handoff/validate-verifier]
         [#'validate-review #'handoff/validate-review]
         [#'validate-claims #'handoff/validate-claims]
         [#'validate-collection-evidence #'w1/validate-collection-evidence]
         [#'validate-w1-bindings #'w1/validate-w1-bindings]
         [#'validate-w2-bindings #'w2/validate-w2-bindings]
         [#'validate-w3-bindings #'w3/validate-w3-bindings]
         [#'validate-observation #'observation/validate-observation]
         [#'validate-cross-bindings #'decision/validate-cross-bindings]
         [#'base-decision #'decision/base-decision]
         [#'decision-with-issues #'decision/decision-with-issues]
         [#'request-has-no-dependency-evidence? #'decision/request-has-no-dependency-evidence?]
         [#'validate-request-shape #'decision/validate-request-shape]
         [#'validate-public-native-admission #'api/validate-public-native-admission]
         [#'public-native-admission? #'api/public-native-admission?]
         [#'verified-public-route-handoff? #'api/verified-public-route-handoff?]]]
  (alter-meta! facade-var merge
               (select-keys (meta component-var) [:doc :arglists])))
