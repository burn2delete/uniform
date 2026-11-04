# Artifact emission implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the transfer plan below. Read [spec.md](spec.md), B13/B14 and the actual selected
backend and consumer contracts. Locate current adapters and focused tests via
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name exact Lower input, target/ABI/runtime/provider/toolchain, output kinds,
actual adapter/tool invocation, byte/canonical hash inputs, required evidence
and Runtime/Build consumer. Plan actual output and execution/semantic comparison
plus relevant invalid-input, tool-failure, unsupported-target, tamper and
metadata-loss controls. Capture real diagnostic IDs and source/generated maps;
fixture files or supplied hashes cannot establish actual production.

Run focused owner checks and actual selected Lower/backend/Runtime/Build
composition needed for the claim. Record target availability and unrun work,
reproducibility inputs and residual host services. Refine only the selected
existing task; preserve restrictions, custody and writer reservations.
Development results, independent acceptance and release evidence stay distinct.

## Resolution dependency interface

Use the [resolution producer and consumer order](../resolve-workflow/plan.md#resolution-producer-and-consumer-order) and the existing selected-result table. Consume actual visible artifact-kind entrypoint binding/artifact tables and current selected module metadata, then real Lower/backend results before selected emission. Preserve PKG1003 refusals at the owning resolver stage; no synthetic symbol or linker fallback supplies missing source resolution.

Existing task bodies/statuses, source predicates, frozen evidence and actual restrictions remain unchanged. Current source/interface entailment, independent acceptance and final release require their own applicable evidence; no held action is enabled.

## Pattern-matching dependency interface

Use the [pattern-matching producer and consumer order](../core-workflow/plan.md#pattern-matching-producer-and-consumer-order) and existing selected-result table. Supply real early artifact writer/manifest/source-map contracts, then actual selected target bytes and readback/provenance for backend and compiler comparisons. Metadata presence or a declaration cannot establish branch behavior, and final conformance is not the first emitter input.

Task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Protocol and dispatch dependency interface

Use the [protocol and dispatch producer and consumer order](../types-workflow/plan.md#protocol-and-dispatch-producer-and-consumer-order) and existing selected-result table. Early real writer/manifest/map contracts precede actual selected interface/call bytes and metadata readback; final conformance is later consumer evidence.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete error-handling source interface

Use the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria). Write/read back actual selected error/handler/trap/interface artifacts and current source/proof/effect/grant/profile/runtime dependencies. A target label or manifest does not demonstrate its failure semantics.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Error-handling dependency interface

Use the [error-handling producer and consumer order](../core-workflow/plan.md#error-handling-producer-and-consumer-order) and existing selected-result table. Early writer/manifest/source-map contracts precede actual selected error/handler/trap/graph bytes and metadata readback. Emitted labels do not demonstrate specified failure behavior or final consumer acceptance.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Library dependency interface

Use the [library producer and consumer order](../stdlib-workflow/plan.md#library-producer-and-consumer-order) and existing selected-result table. Early genuine manifest/canonical-record schemas precede actual selected library bytes and readback. The signer then binds actual bytes; its future final signed assembly cannot be the first byte-emission input.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Facet dependency interface

Use the [facet producer and consumer order](../expand-workflow/plan.md#facet-producer-and-consumer-order) and existing selected-result table. Early actual artifact/source/diagnostic schemas precede output construction, then admitted selected generated/domain/target products precede real written bytes/metadata/readback. The later complete bundle is no first schema; every source facet needs no native artifact or final release.

Task bodies/statuses, source criteria, historical reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending. No held action is enabled.


## Interoperability dependency interface

Use the [interoperability producer and consumer order](../runtime-workflow/plan.md#interoperability-producer-and-consumer-order) and sole selected-result table. Complete L19 writes consume early real artifact/manifest/map/error schemas, then actual admitted selected output bytes and required metadata/provenance/readback. Later actual conformance packs apply before policy-required release-grade claims, not the first development writer. Source generation needs no universal native bytes/signature; explicit development-only evidence gaps remain non-release and cannot be laundered by manifests.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## B1-B3 dependency interface

Use the [B1-B3 producer and consumer order](../backends-workflow/plan.md#b1-b3-producer-and-consumer-order) and sole selected-result table. Complete B1-B3 writer interfaces expose actual encoding/write/readback/map/manifest contracts before output construction. Consume actual admitted selected Lower/family bytes and required current records, then read back concrete C/header/LLVM/bitcode/object/debug artifacts as requested. Later conformance/release bundles follow actual subjects and are not first writer schemas. Neither both native families nor signing is universally required; preserve diagnostic/refusal products and explicit development-only evidence gaps.

Task bodies/statuses, all twenty source criteria, historical review scopes and forty future source bindings remain unchanged. Current source/interface entailment, implementation and independent acceptance remain unassessed. No held operation is enabled.


## Historical task-authority-transfer plan

# Implementation plan: Emit task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler/backend maintainer.
Input revision: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
B13/B14 retain artifact/provenance and backend-conformance requirements;
workflow artifacts do not replace semantic authority.

Retain existing canonical navigation:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/emit.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Emit canonical remaining work](../../docs/component-roadmaps/emit.md#current-evidence-and-remaining-work) | emit/1 through emit/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/emit.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-07-backend-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler/backend maintainer. Interface owners: [lower](../../docs/component-roadmaps/lower.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [build](../../docs/component-roadmaps/build.md#release-outcome), [backends](../../docs/component-roadmaps/backends.md#release-outcome).
Lower establishes legal backend input and Runtime supplies service/ABI contracts. Build consumes emitted records later. Preserve the selected staged Linux LLVM candidate; a hosted artifact does not establish seedless release or all-backend support.

## Approach and exact files

Move only the five real rows to the marked
[implementation region](tasks.md#implementation-tasks), retaining stable semantic
IDs, display labels, exact evidence/status and concrete exit criteria. Keep the
complete original migration task file as a frozen suffix with unchanged
checkboxes. Workflow delivery stays separate from implementation completion.

Own the roadmap and four workflow artifacts. No
compiler/runtime source/test, other package, mapping artifact or shared helper
is edited. The source plan blob `d62412306c4a726aa9c077a1abd0b72158c55677`
contains no Additional contract interfaces appendix; preserve B13/B14 and all
original navigation without creating an appendix. Preserve selected-target,
hosted, producer/admission, accounting, conformance, compatibility,
wider-artifact and acceptance wording while linking the roadmap directly to the
task ledger.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved task criteria, historical suffix, prior navigation and
reviewed appendix. Verify B13/B14 targets. Check the Markdown files for ASCII
and file/heading links, then run whitespace and clean exact-base/head/tree
integration preflight. These structural checks require no compiler semantic or
emission suite and grant no artifact acceptance.

## Delivery and remaining work

Independent review inspects all seven files and the proposed public body. Publish
a validated draft with exact identities and compact actual final-tree receipts
for maintainer current-head review. Integration follows that disposition and
repository gates. All five implementation outcomes remain Open.

## Names, callers and compatibility

runtime API, wire/schema identity, diagnostic, manifest hash, target choice,
admission policy or legacy source name changes. Planning IDs remain traceability.
