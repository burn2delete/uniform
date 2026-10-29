# Implementation plan: Compiler API workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit implementation records only in [tasks.md](tasks.md#implementation-tasks). The existing outcome sections below retain their headings and acceptance boundaries as generated views:

- [built in compiler pipeline outcome](../../docs/component-roadmaps/compiler-api.md#built-in-compiler-pipeline-outcome)
- [plugin host finite remaining tasks](../../docs/component-roadmaps/compiler-api.md#plugin-host-finite-remaining-tasks)
- [pass evidence finite remaining tasks](../../docs/component-roadmaps/compiler-api.md#pass-evidence-finite-remaining-tasks)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: three literal-key implementation records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.
5. `tools/compiler_api_task_projection{,_test}.clj`: fixed-path parsing, deterministic projection and focused controls. Shared helpers stay unchanged.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Pass input schemas and semantic verifiers are per-stage prerequisites. Plugin grant enforcement is separate from built-in execution. Build, CLI and Tools consume actual API products; Release consumes evidence later.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `compiler-api-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [TEST1](../../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md#requirements): Compose portable language conformance from the owning front-end passes, with fixture/profile/target/type/effect identities and stable diagnostic spans; orchestration does not own each language rule.

## Proposed historical-gap criteria dependencies

The following interfaces supply the concrete criterion's inputs; their entire
components need not be complete before a selected fixture is developed.
Conformance producers execute checks and emit evidence. Intermediate admission
and final release consume it independently. All affected tasks remain Open.

| Source obligation | Concrete task | Required input interfaces |
| --- | --- | --- |
| `compiler/c1/compiler-program-legality` | `pipeline` | `types/current-evidence-and-remaining-work/types5-integrate-typed-core`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` |
| `compiler/c12/domain-entry` | `domain-ir-boundaries` | `pipeline`, `plugins`, `pass-evidence`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `backend-interface` |
| `compiler/c12/domain-fallback` | `domain-ir-boundaries` | `pipeline`, `plugins`, `pass-evidence`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `backend-interface` |
| `compiler/c12/domain-proof-boundary` | `domain-ir-boundaries` | `pipeline`, `plugins`, `pass-evidence`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `backend-interface` |
| `compiler/c12/domain-registration` | `domain-ir-boundaries` | `pipeline`, `plugins`, `pass-evidence`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `backend-interface`, `math`, `schema`, `workflow`, `backend-hdl` |
| `compiler/c12/registered-domain-exit` | `domain-ir-boundaries` | `pipeline`, `plugins`, `pass-evidence`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `backend-interface` |


## Bootstrap residual interface dependencies

These criteria are Open implementation work. Their mapping does not grant implementation, migration or release acceptance. The declared seed subset and three native observations share one new seed-compiler task; its owner is the seed compiler/native-ABI maintainer. Existing pass/module and provenance/equivalence producers remain distinct from intermediate admission and final release.

Criterion `bootstrap/compiler-lineage-query` in task `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`: owner role bootstrap provenance maintainer; depends on producer of stage/source/compiler provenance, release reproducibility and equivalence reports. Closing evidence: compiler lineage graph; auditor query index and successful traversal result; broken-edge/undeclared-cycle/query-failure fixtures and diagnostics. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/compiler-meta-authority` in task `pipeline`: owner role compiler module and profile maintainer; depends on Auth scoped-grant enforcement, Legality meta-profile admission, module-interface manifest. Closing evidence: module profile/effect/capability manifest; allowed invocation and denied-effect fixtures; source-bound authority diagnostics. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/generated-compiler-checking` in task `pipeline`: owner role compiler pipeline maintainer; depends on Expand generated-program integration, normal compiler checkers, source/generated-origin diagnostic producer. Closing evidence: checked generated compiler artifact; generated-origin provenance chain; positive and negative re-entry fixtures and BOOT3004 diagnostic. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/locked-rebuild` in task `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`: owner role bootstrap build maintainer; depends on Build locked dependency and recipe execution, stage/compiler/source provenance, signing and revocation policy. Closing evidence: reproducible bootstrap recipe and controlled environment; real rebuild manifests/content comparisons and diverse-or-unsupported strategy report; accepted-delta/exception and residual-trust records; revocation and negative-fixture reports. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/module-interface` in task `pipeline`: owner role self-hosted compiler module maintainer; depends on compiler stage schema/version registry, pass-evidence producer, stage compatibility matrix and owner projection. Closing evidence: instantiated module migration and compiler-module manifests; module conformance/diagnostic/preservation reports; field-mismatch and missing-interface negative fixtures. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/native-carrier-header` in task `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations`: owner role seed compiler native-ABI maintainer; depends on seed compiler-artifact builtin registration, ordinary once-only operand evaluation, native getter/access TCB under existing host restrictions. Closing evidence: six-slot result corpus for every declared native contour and numeric extrema; callback/non-forcing and exact-class negative reports; sticky-denial/physical-backing and arity/context diagnostic fixtures; seed TCB and ordinary-export exclusion records. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/native-reference-identity` in task `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations`: owner role seed compiler native-ABI maintainer; depends on seed compiler-artifact builtin registration, ordinary operand evaluation and error dispatch. Closing evidence: same/distinct/null/shared/cyclic reference result fixtures; once-evaluation and zero-callback controls; arity/context and no-export diagnostics/records. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/native-symbol-units` in task `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations`: owner role seed compiler native-ABI maintainer; depends on seed native Symbol getter contract, compiler-artifact builtin registration, ordinary operand evaluation. Closing evidence: UTF16 name/namespace/null/surrogate and representability fixtures; once-evaluation/no-callback controls; arity/context/class diagnostic and export-exclusion records. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/pass-determinism` in task `pipeline`: owner role compiler pass implementation maintainer; depends on instantiated module/pass interfaces, controlled declared input identities, diagnostic and artifact canonicalization where applicable. Closing evidence: actual repeated-pass output and order comparisons; deterministic output report; nondeterministic transformation rejection fixture with BOOT4002. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/pass-fact-preservation` in task `pass-evidence`: owner role compiler verification maintainer; depends on module required/preserved/invalidated/regenerated fact contracts, actual pass implementation, source and safety fact verifiers. Closing evidence: before/after pass preservation report; executed positive/negative/preservation fixture records; dropped-fact and missing-test diagnostics BOOT4003/BOOT4007. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/seed-subset` in task `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations`: owner role seed compiler maintainer; depends on seed reader/core/resolution/type/effect/MIR implementations, declared bootstrap backend and diagnostic producer, artifact/source provenance. Closing evidence: seed executable/backend artifact and source-bound manifest; supported/excluded subset and host/runtime trust reports; accepted/rejected subset fixtures, stable BOOT2 diagnostics and stage1 comparison metadata. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.

Criterion `bootstrap/stage-equivalence` in task `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`: owner role bootstrap equivalence maintainer; depends on actual seed/prior-stage compiler outputs, stage support matrix and applicable suites, reviewed delta policy and bootstrap provenance. Closing evidence: actual stage comparison inputs/outputs and equivalence artifact; diagnostic/IR/conformance and accepted-delta reports; missing-output/drift/unreviewed-delta/conformance/conditional-performance negative fixtures. Accepted and rejected fixtures must execute the owning mechanism; supplied records alone are insufficient.