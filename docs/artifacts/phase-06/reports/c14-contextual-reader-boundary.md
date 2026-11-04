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
and compiles its actual dependency plans. The five-argument C5 API receives an
explicit development-current Reader and macro invocation and forwards it through
the real C3, SH05 and SH06 parents. It does not inject precomputed Reader products.
This is development-current evidence, not frozen public-source admission.
The invocation map is
`{:reader-source-path reader
  :authenticated-envelope-options {:source :development-current}
  :macro-source-mode :development-current}`; the ordinary four-argument API
continues to retain its pinned macro default.

The following source table is a historical development snapshot from the
earlier current-main port target `84a929dfa6661e7e4fa8ff16f58767b3652f31f7`
and contextual candidate `7552eb4fe04167ca87870ea1e947dfc110b58d5b`. It is
not the current reviewed source identity and is not an admission pin. Its
source byte counts and SHA-256 values are:

| Source | Bytes | SHA-256 |
| --- | ---: | --- |
| Reader | 246369 | `eec041127cb2785f92e7e2c79d14350d869a0ea8a0194ea3da55f75d52099205` |
| C11 | 567399 | `c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e` |
| C13 | 173647 | `c1209f1f5c448e3b86ec55bcbe2be8ec1896d43097ac222b8470427bca012b63` |
| Contextual C14 | 5406 | `99453ac8c4711892673b60dd7948ddbb4432dce511a218181968a85731138ba1` |

These identify historical source, not a successful new execution. Historical
source and execution receipts keep their original identities.

## Historical source-input checkpoint

The earlier clean composed checkpoint is commit
`082231d0d8cb92e4e4c2cbeb62585d35ebb64700`, tree
`18827cbad69d5db1a94c44546a736564a0c3b54c`. It is retained as a historical
source-input checkpoint, not the current reviewed candidate. Its development
source inputs are:

| Source | Bytes | SHA-256 |
| --- | ---: | --- |
| `bootstrap/gravity/src/gravity/bootstrap/reader.gravity` | 246369 | `eec041127cb2785f92e7e2c79d14350d869a0ea8a0194ea3da55f75d52099205` |
| `bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity` | 563129 | `3dea275512a70ca63b9da6ed8d79e540ca4c776d6e05d21f1dc44e39e62b504b` |
| `bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity` | 175365 | `7bf71720a67d665206259fc06de9eb1c87086f7712f98664fbfa2ca9d597c75a` |
| `bootstrap/gravity/src/gravity/compiler/c14_literal_bool_contextual_lowering.gravity` | 5406 | `99453ac8c4711892673b60dd7948ddbb4432dce511a218181968a85731138ba1` |

## Current source-input checkpoint

The immutable current candidate source-input checkpoint is merge commit
`8ee4fcbe0f69e38efd83ae3fc81500ddb53b3491`, tree
`2bb547602a8c03151f45a2be9fded8b8305ca87d`. It composes the reviewed f488
contextual branch with current main at the normal #892 merge. Its exact current
source inputs are:

| Source | Bytes | SHA-256 |
| --- | ---: | --- |
| `bootstrap/gravity/src/gravity/bootstrap/reader.gravity` | 246369 | `eec041127cb2785f92e7e2c79d14350d869a0ea8a0194ea3da55f75d52099205` |
| `language/compiler/src/gravity/compiler/c11_mir_specification.gravity` | 593009 | `8f5e1949f84e02604919d3fb6acad7721673c53ae9a3aa2d517fdeceac90e9db` |
| `language/compiler/src/gravity/compiler/c13_mir_optimization_passes.gravity` | 175365 | `7bf71720a67d665206259fc06de9eb1c87086f7712f98664fbfa2ca9d597c75a` |
| `language/compiler/src/gravity/compiler/c14_literal_bool_contextual_lowering.gravity` | 5406 | `99453ac8c4711892673b60dd7948ddbb4432dce511a218181968a85731138ba1` |

Erratum: earlier versions of this report printed an invalid 69-character
literal for the C13 SHA-256 in both affected source tables. That invalid record had no
execution authority, did not establish a migration pin, and did not confer
acceptance. The 64-character value shown in both tables is the verified hash of
the recorded C13 bytes; this correction repairs provenance only.

The default `gravity check` / exact C13 source-coverage route retains a
NONPASS at the pinned C2 source guard: it expects Reader bytes `231576` and
observes the current Reader bytes `246369`, yielding `C2-HASH`. This is stale
pinned-source evidence, not malformed current source. No source hash is
repinned and no default-route failure is relabeled as a pass.

The current-main composition moves the C11, C13 and contextual C14 source
inputs to their canonical `language/compiler` owner paths without replaying
the deleted bootstrap source tree. The reader remains owned by its existing
bootstrap path. The current tuple above is a source-location and tree-
composition record, not a successful C5/C14 execution or renewed migration
pin. The historical table above remains an audit record with its original
source identities.

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
