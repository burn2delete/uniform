(ns gravity.p15-public-native-admission.public-api
  (:require [gravity.p15-public-native-admission.contract :refer [default-public-native-admission]]
            [gravity.p15-public-native-admission.validation-support :refer [issue]]
            [gravity.p15-public-native-admission.replay-policy-validation :refer [replay-policy-table-valid?]]
            [gravity.p15-public-native-admission.replay-issue-lookup :refer [first-replay-structure-issue]]
            [gravity.p15-public-native-admission.decision :refer [decision-with-issues validate-request-shape]]))

(defn validate-public-native-admission
  "Reject synthetic W1/W2/W3 v1 replay requests without performing I/O.

  The result is always a data map; malformed, missing, tampered, or
  cross-bound observations never throw and never authorize the public route.
  Dependency success is unreachable because every exact replay-owner policy
  is explicitly unfrozen for W1, W2, and W3.  The replay and checkout-root
  fields only exercise hostile negative validation; this namespace has no
  artifact reader, verifier callback, filesystem observer, or process hook.
  The public v1 entry point returns exactly one terminal issue: the first
  request-shape issue, otherwise the first frozen replay-structure issue,
  otherwise forged owner policy, otherwise the W1 unfrozen-owner blocker.
  It does not invoke the legacy handoff, binding, or cross-workstream
  validators after that terminal boundary.
  Producer handoffs contain implementation A/tree only; external pins and
  observations contain payload-containing B/tree identities, and no later C
  identity is embedded.  Defensive handoff, binding, and cross-link checks do
  not confer authority.  A future v2 request and a separate reviewed W4 route
  artifact are both required before user source or output I/O can be reached."
  [request]
  (cond
    (nil? request)
    default-public-native-admission

    :else
    (let [shape-issue (first (validate-request-shape request))
          pins (:pins request)
          observations (:observations request)
          replay-issue
          (when-not shape-issue
            (first-replay-structure-issue pins observations))
          terminal-issue
          (or shape-issue
              replay-issue
              (when-not (replay-policy-table-valid?)
                (issue :replay-owner-contract-forged-or-incomplete
                       [:replay-owner-policies]))
              (issue :w1-replay-contract-unfrozen
                     [:replay-owner-policies :w1]))]
      (decision-with-issues
       :rejected
       :dependency-interface-rejected
       [terminal-issue]))))

(defn public-native-admission?
  "Return true only for a separately admitted tracked public route.

  Negative-only v1 replay validation cannot authenticate dependencies and
  always returns false here.  This predicate is a convenience over the
  decision shape, not a second authority source."
  [request]
  (true? (:bounded-native-route-admitted?
          (if (map? request)
            (validate-public-native-admission request)
            default-public-native-admission))))

(defn verified-public-route-handoff?
  "Fail-closed v1 seam for the future reviewed W4 route artifact.

  The tracked-route schema and its replay evidence are not implemented yet, so
  no input can authenticate as a public route.  In particular this predicate
  never accepts premature public, seed-retirement, self-hosting, or release
  claims.  Its trailing question mark is retained as the future verifier API."
  [_route]
  false)
