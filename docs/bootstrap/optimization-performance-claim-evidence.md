# Native performance claim validation

## Scope and governing contracts

Move the complete PERF1 family from `00138-perf1-required-claim-fields.clj`
to the existing `gravity.optimization`: the required-field vector and six
algorithms for presence, claim aliases, missing fields, diagnostic facts,
target fingerprint fallback and ordered claim validation. Keep the original
Bootstrap names and arities as live adapters. Bootstrap retains the Stage0
performance manifest and PERF1-before-PERF10 composition.

D0, D1, D2, D3, D6, D8, D9, PERF1, PERF10, SAFE15, C1, C13, C15,
BOOT8 and TEST2 govern this slice. This is temporary seed organization and
native metadata validation, not canonical performance certification.

## Public API and artifacts

The owner adds `native-performance-required-claim-fields` and:

```clojure
(native-performance-present? value)
(native-performance-normalize-claim claim)
(native-performance-missing-claim-fields present-var fields-var claim)
(native-performance-failure! failure-var id source-path manifest claim extra)
(native-performance-target-fingerprint claim)
(validate-native-performance-claim!
  missing-fields-var fingerprint-var present-var failure-var
  source-path manifest performance claim)
```

Inputs are caller-owned claims, manifests and performance metadata plus
explicit live compatibility Vars. Outputs retain the existing Boolean,
normalized claim, ordered missing-field vector, fingerprint, owner result or
exception, and relative `:complete` label. The vector is policy data; it
does not authenticate any artifact. Existing Bootstrap Vars remain observable
at their original call sites, including required-field rebinding.

The owner selects the nine PERF1 refusal rules, messages, remediation and
facts. Generic failure construction, publication and exception transport stay
with the supplied existing owner. Extra diagnostic facts retain their original
override precedence. Do not infer map-expression evaluation order from printed
key order.

Dependency direction is Bootstrap -> Optimization -> Clojure core. The owner
must not import Bootstrap, discover its Vars, invoke an orchestrator, or
authenticate supplied callbacks. The existing explicit Bootstrap import of
Optimization already supplies the dependency; no inventory schema or CI
definition change is planned.

## Verification and current disposition

This draft records the boundary before moving production code. Capture real
paired `.gravity`/`.qst` source plans, reports and interpreted/compiled
agreements before and after the move, plus all nine genuine PERF1 refusals
before function compilation. Preserve source/module metadata, source hashes,
deterministic plan/report identities and the separate PERF10 refusal order.

Use the existing Bootstrap-free C13 native test leaf for owner contracts,
field/alias/fallback rules, first refusal, live Var selection, lazy missing-field
reads, returning failures, opaque values and exception identity. Extend the
existing plan assembly regression for genuine source composition and adapter
ports. Update the broader Optimization test's declared API set without running
its private compiler-carrier path locally. Run focused lint, document validation,
language/ownership gates and the full changed-file comparison against Main.
Independent semantic/adversarial acceptance and this source's own hosted CI
audit remain required before integration. Earlier source/CI results confer no
acceptance on this slice.

## Unsupported boundaries

Presence of native proof, benchmark, target, layout or guard labels does not
validate their truth, authenticity, provider, dominance, compatibility or
content. This family does not compare the claim's target with actual target
execution, prove optimized semantics, verify numeric certificates, enforce
canonical C13 passes or issue a verifier certificate. It performs no actual
optimization, check removal, benchmark measurement or backend execution.

The ordinary emitted Stage0 plan omits module metadata and its performance
report retains `:optimization-mode :none` and `:no-checks-elided`. Do not inject
metadata into a plan to manufacture performance evidence. Source/module
metadata and content provenance remain independently observable upstream.

No held Reader/authentication/private source-factory/pin route is part of the
local controls. No migration, release, self-hosting or seed-retirement credit
is claimed. Clean source, exact independent reviews, provider CI and current
base integration remain separate obligations.
