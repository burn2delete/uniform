# C14 contextual Reader boundary checkpoint

This is the finite completion criterion for [PR374](https://github.com/burn2delete/uniform/pull/374),
not acceptance of the complete Lower component. The governing requirement is
[C14 input eligibility](../../../phase-06-compiler-architecture/093-c14-target-lowering-architecture.md).
The broader [Lower task ledger](../../../../specs/lower-workflow/tasks.md), including
its supported-operation and target matrices, remains open.

## Supported case and dependency route

The selected case is the original `true` zero-argument Bool diamond in `.gravity`,
hosted profile and JVM target. The exact selector is
`gravity.self-hosting.sh17-literal-bool-input-boundary-test/sh17-contextual-module-validates-original-and-rejects-substitution`.
It constructs actual source products, original/fresh Core evidence, original MIR,
the C13 rewrite and its independently checked translation relation.

The C14 module bundle separately constructs C5 artifacts from the complete C11
and C13 source files, then resolves the C14 caller against those module artifacts
and compiles its actual dependency plans. The existing four-argument C5 API
creates the explicit selected-current Reader invocation and forwards it through
the real C3, SH05 and SH06 parents. It does not inject precomputed Reader products.
This is development-current evidence, not frozen public-source admission.

At the normal reconciliation of published PR head `83e121ed010729d6f6a8bad4a4d4d33095c14a70`
with main `bffefedfada9e3312e68e0a28a69d71db459d0e2`, source SHA-256 values are:

| Source | SHA-256 |
| --- | --- |
| Reader | `a74e3d1d3bc3d7627493586c78367844b6ba41980cb65b2bee1638fe3273928a` |
| C11 | `c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e` |
| C13 | `ae62f422dbad67921c90ee85e4c584017891fe57ac516013963dab1163eb38de` |
| Contextual C14 | `f8ed09fc65ecd80073449b9193805231a7d637c984cc713559c361a511fc0616` |

These identify source, not a successful new execution. Historical source and
execution receipts keep their original identities.

## Finite acceptance checklist

- [ ] Complete the actual source producer and fresh original Core/MIR checks.
- [ ] Complete the actual three-module contextual construction at an exact
  clean commit/tree with source, dependency and runtime identities retained.
- [ ] Observe the original context as `:rejected/:scoped-valid`, diagnostic
  `C14-UNSUPPORTED`, reason `:jvm-b5-target-lowering-contract`, with original
  provenance, explicit nonclaims and no emitted artifact or authority.
- [ ] Independently reject the changed invalidation ledger, target request and
  source-content hash as `C14-INPUT`. Keep authentic Core for the target control;
  assert the replacement source hash differs before exercising its control.
- [ ] Retain terminal process status, clean identity bookends and complete
  positive/negative results under the supported broker/heap/watchdog profile.
- [ ] Obtain independent exact-head review of tests and the entire residual PR
  delta, including inherited backend changes, before normal integration.

The selector's source-hash tamper is not bidirectional substitution of two
genuinely constructed source tuples. The existing separate cross-source selector
retains that stronger obligation; it receives no credit without execution.

## Actual disposition and owners

The current diagnostic repair follows the C14 catalog: it records the carried
checked-Core artifact identity as `:input-artifact-id`, the existing literal-Bool
scope as the MIR anchor, and distinguishes target `:jvm` from backend
`:gravity.backend/jvm`. Missing-feature, proof and provider expectations are
explicit. Stale C13 and target-substitution errors retain independently verified
original MIR provenance. A failed specialization retains it only when the
original C11 check independently passes; malformed or unauthenticated inputs
receive no artifact, span or origin identity.

The focused diagnostic test extracts the unchanged helper body from the actual
source, narrows only its unused imports/exports, and executes that leaf through
the development runtime. This is supplied-DATA helper coverage, not the
three-module consumer, C5 acceptance or fresh MIR authentication. The contextual
selector also asserts the canonical fields and provenance, but its complete
execution remains required by the unchecked checklist above.

The retained original 120-second contextual run timed out during full C11 C5
dependency construction, before C14 execution. Subsequent full-source diagnostics
at their separately recorded identities did not establish complete C5 acceptance.
No timeout, supplied-DATA result, mergeability or test count closes the checklist.
This checkpoint adds no unchanged heavy replay and no increased execution limit.

The Reader/C14 owner owns this selector and contextual C14 source. C6/C11 source
authentication and genuine producer integration remain with their existing
Native/MIR owners; C13 cross-source work remains with its optimization owner.
Their newer producer evidence must be reviewed for exact interface applicability,
not silently reattributed to this test's development producer. Shared C11,
accounting, C12 and backend emission implementations are not changed here.

The next required integration evidence is complete C11-to-C5 construction on the
selected source route. Any changed diagnostic must follow an evidenced source or
supported stage-boundary change with reviewed provenance. Until then PR374 stays
draft; ABI/runtime/provider lowering, backend admission, emission, C12 execution,
full C5, complete Lower, C14 migration and release remain unclaimed.
