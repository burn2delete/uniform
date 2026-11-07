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

The draft opened at clean scope `ebbf58db458f36928b196e2f8dca99d109d652f1`
before moving production code. Capture real
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

The complete six function bodies and one policy definition now live in the
owner. Raw-text extraction receipt
`d18c08ed63cb0574d4703d32c71e5f10b279d59e082812089ae2c10e0de833f1`
preserves originals and moved forms without walking or reprinting maps.
Only names, explicit arguments and operation references change. Optimization
has twenty-three public members; the C13 facade API is unchanged.

The placement review recommends this existing owner and retaining both
Bootstrap orchestrators. Its suggestion of private missing-field/failure
helpers is not adopted: the original Bootstrap validator selects those live
Vars at each invocation, and the public compatibility signatures also call
them independently. Both explicit owner APIs preserve that observable boundary
without reverse imports or Var discovery. The policy vector is native seed
data, not canonical proof authority. The existing component JSON and CI
definitions are unchanged.

All twenty-six full paired source observations match, including six emitted
plans/reports/interpreted-compiled agreements and twenty refusals before any
function compilation. Each PERF1 id and the following PERF10 refusal occur on
both source extensions. The baseline
`e767b26b007d480c29e2d578894f5764377f203cd3788dce6987c09e03c7e426`
binds the clean scope commit. The after receipt
`b76d001adb4a9bdb4ac5f049b690ce8b72c47e726da883110bfa6b1d5aac54be`
binds that actual HEAD with two dirty producer hashes, not a future commit.
These are caller-supplied source texts through genuine macro/module/plan
composition, not authenticated files or the separate native PERF1 artifact
fixture route. Original source/module metadata and plan/report hashes survive.

The first source-control harness incorrectly expected a false primary target
fingerprint with absent fallback to survive `or`; it actually selects nil.
The preserved failed run reached four successful source plans before that
expectation failed. The corrected positive uses an absent primary and false
environment fallback, retaining the original native presence behavior. A
native target-refusal test also initially supplied an empty benchmark, which
correctly triggers PERF1-CLAIM before PERF1-TARGET. Its corrected fixture keeps
a nonempty harness while omitting the fingerprint. Neither observation caused
a production semantic change; both failed logs remain historical evidence.

Focused native controls pass 21 tests / 334 assertions; whole plan/instruction
regressions pass 45 / 1004; language/ownership gates pass 48 / 1202. Tests
retain lazy presence selection, policy Var rebinding, all-nine returning-failure
order, selected-owner-before-payload behavior, extra-fact precedence and
opaque Exception/Error identity. Full Main comparison, independent exact-source
reviews and this source's hosted CI audit remain pending at this record.

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
