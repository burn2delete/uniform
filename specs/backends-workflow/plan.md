# Implementation plan: Backends family task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner roles: backend family maintainers named in the canonical portfolio.
Input revision: `a6a324bb48cd07812e97fee2911887f9dbd84092`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts). B1-B14, retained
AI references and the mapped appendix remain normative inputs. Editing authority
does not transfer family ownership or establish semantic acceptance.

Retain the original canonical navigation:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/backends.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Backends canonical remaining work](../../docs/component-roadmaps/backends.md#release-outcome) | the named backend-family and shared-contract sections; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/backends.md#backend-conformance) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-07-backend-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

The existing family anchors remain unchanged:

- [Interface and ABI](../../docs/component-roadmaps/backends.md#backend-interface-and-abi)
- [C](../../docs/component-roadmaps/backends.md#c)
- [LLVM](../../docs/component-roadmaps/backends.md#llvm)
- [Wasm](../../docs/component-roadmaps/backends.md#wasm)
- [JVM](../../docs/component-roadmaps/backends.md#jvm)
- [JavaScript/TypeScript](../../docs/component-roadmaps/backends.md#javascript-and-typescript)
- [MLIR](../../docs/component-roadmaps/backends.md#mlir)
- [GPU](../../docs/component-roadmaps/backends.md#gpu-backend)
- [HDL](../../docs/component-roadmaps/backends.md#hdl)
- [Workflow](../../docs/component-roadmaps/backends.md#workflow-backend)
- [AI artifacts](../../docs/component-roadmaps/backends.md#ai-artifacts)
- [Query](../../docs/component-roadmaps/backends.md#query-and-relational)
- [Mobile](../../docs/component-roadmaps/backends.md#mobile)
- [Emission contract](../../docs/component-roadmaps/backends.md#artifact-emission-contract)
- [Conformance](../../docs/component-roadmaps/backends.md#backend-conformance)

Component role: backend family maintainers named in the canonical portfolio. Interface owners: [mir](../../docs/component-roadmaps/mir.md#release-outcome), [lower](../../docs/component-roadmaps/lower.md#release-outcome), [emit](../../docs/component-roadmaps/emit.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome).
The portfolio contains independent family outcomes. Only targets actually consumed by the selected compiler recipe gate that release. No backend may legalize rejected source semantics; unavailable target execution remains open, not passed.

## Approach and exact files

The marked [implementation region](tasks.md#implementation-tasks) owns fifteen
grouped records using the existing literal backend-* keys. Preserve each heading
and complete Remaining tasks paragraph as one source block. Do not invent
individual-clause IDs or split acceptance questions from evidence/decision/
integration context. Keep the original migration tasks file as a frozen suffix.

Own the roadmap and four workflow artifacts. No shared helper or other component
file changes. Preserve the entire reviewed Additional contract interfaces
appendix as an exact suffix from plan blob
`1b2a18bbc06d4379f7373c3bdaa619de7f89631a`. No backend family, provider or
compiler execution is part of this transfer.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved task criteria, Open statuses, historical suffix, prior
contract/family navigation and reviewed appendix. Check the Markdown documents
for ASCII and file/heading links, then run whitespace and clean
exact-base/head/tree integration preflight. Structural checks require no
compiler semantic suite and confer no backend acceptance.

## Delivery and remaining work

Independent review inspects all seven files and the proposed public body. Publish
a validated draft with exact identities and compact actual final-tree receipts
for maintainer current-head review. Integration follows that disposition and
actual repository gates. This transfer covers existing named sections only; it
adds no future family or new prerequisite to the selected compiler recipe.

## Names, callers and compatibility

API, wire/schema identity, diagnostic, manifest hash, selected target, runtime
provider, grant or admission policy changes. Historical planning IDs remain
traceability and literal family keys remain compatible.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L19](../../docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md#requirements): Lower native/managed/Wasm foreign declarations with explicit ABI, layout and unsupported-profile diagnostics.
- [PERF7](../../docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md#requirements): Generated target variants preserve source maps and compatibility metadata, with explicit feature guards and truthful dispatch overhead.
- [PERF8](../../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md#requirements): Target intrinsic maps and scalar/guarded fallback must match declared features; strict floating reduction cannot silently reassociate.
- [TEST6](../../docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md#requirements): Conformance covers the declared target/backend/runtime matrix, observable semantics, unsupported diagnostics, ABI/layout, manifests and source maps; compile-only evidence is labeled as such.
- [GOV5](../../docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md#requirements): Target support records bind owner, tier, supported/unsupported profiles and actual lowering/conformance evidence; backend availability is not tier promotion.
