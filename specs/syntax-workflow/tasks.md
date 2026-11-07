# Tasks: Syntax implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler syntax maintainer. Existing reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Syntax implementation task ledger. The roadmap's
[remaining work](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work)
is a navigation view. Order is display order, not a dependency
edge. Stable IDs preserve the phase/title identity convention; legacy syntax/N
labels remain unchanged display metadata, not new implementation names.
Unchecked means not accepted complete. A checked task requires explicit
`Accepted;` status and an independently accepted immutable evidence receipt.
The source evidence referred to in the records remains in the linked roadmap.
No task closes through this transfer.

<!-- syntax-implementation-tasks:start -->

- [ ] `syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax` syntax/1: Construct canonical syntax

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement every required form kind with stable semantic identity, namespace/profile/phase and metadata; reject malformed fields and source references.

<!-- macro-source-syntax:start -->

Apply the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro syntax interface criterion (Open): Expose actual form-kind/children, literal, span, namespace/lexical context, metadata read/attach, fresh-identifier introduction, hygiene-aware identifier comparison, explicit capture, generated-origin recording and artifact serialization across reference and alternative representations. Preserve form, file/span, namespace, lexical/hygiene context, metadata, phase, caller profile, macro namespace, origin and build context without mutating input syntax. Lazy/compact/indexed/arena/persistent/incremental/typed representation is permitted when observable operations, semantic immutability and diagnostics survive. Reject lost context, invalid identifier comparison, malformed representations and nonserializable claimed artifacts through actual adapter/verifier cases.
<!-- macro-source-syntax:end -->



<!-- syntax-source-construction:start -->

Apply the [complete syntax source criteria](plan.md#complete-syntax-source-criteria) within this existing task. Preserve its original status, accepted evidence scope and historical criteria.

Complete C3 construction criterion (Open): Construct immutable syntax values from actual authenticated C2 tokens/form trees/seeds/source maps with stable content-derived semantic identity, form kind, sufficient raw spelling and decoded value, source or generated span, origin chain, namespace, compile phase, caller profile, metadata and inspectable hygiene context. Cover all fifteen C3 form families: list, vector, map, set, symbol, keyword, string, character, numeric literal, boolean, nil, tagged literal, metadata wrapper, abbreviation expansion and generated form; numeric subkinds follow the owning language contract. Retain the required source-id/form-id/token-range references and primary/all spans. Derive identity from actual semantic fields and origin links; tooling-only trivia is referenced and excluded unless the artifact kind requires it. Reject malformed fields, inconsistent ids and invalid source joins rather than assigning positional identity or trusting supplied identity/immutable flags.
<!-- syntax-source-construction:end -->

- [ ] `syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations` syntax/2: Preserve transformations

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record generated producer/input origins, explicit metadata changes, hygiene marks and intentional captures; reject accidental capture.

<!-- macro-source-syntax-origins:start -->

Apply the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro syntax preservation criterion (Open): Syntax transformations produce new linked values with original definition/call/generated spans, lexical context, hygiene marks, metadata, phase/caller-profile and complete generator identity/version/input/output origins. Metadata survives unless an explicit documented transformation changes it; compact or normalized representations cannot erase diagnostic anchors. Verify actual reference and alternate adapter products and reject lost, forged, stale or disconnected context/origin chains. Expand produces expansion-specific facts; Syntax owns their faithful carrier and transformation semantics, rather than duplicating macro execution.
<!-- macro-source-syntax-origins:end -->



<!-- syntax-source-transformations:start -->

Apply the [complete syntax source criteria](plan.md#complete-syntax-source-criteria) within this existing task. Preserve its original status, accepted evidence scope and historical criteria.

Complete C3 transformation criterion (Open): Produce new immutable syntax values linked to prior syntax ids, preserving complete source maps/origin chains and metadata unless the actual transformation records an explicit metadata change. Generated origins retain source/generated spans, producer kind/id/version, input syntax ids, generation reason, expansion step and relevant build effects. Preserve inspectable marks, lexical scopes, renames, introduced identifiers, explicit capture declarations and macro definition/call namespaces. Intentional capture names the identifier and requesting macro API; reject accidental or undeclared capture, broken/forged origins, hidden hygiene and implicit metadata loss. Carry generated syntax under the caller's profile unless its producing contract explicitly authorizes a different transition. Syntax carries/verifies these products; Expand produces expansion-specific facts and Auth enforces actual authority.
<!-- syntax-source-transformations:end -->


<!-- syntax-facet-source-syntax:start -->

Apply the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 actual facet syntax-provenance criterion (Open): Preserve original syntax identity, source spans, hygiene/lexical context, metadata, generated-origin chains and profile assumptions when base macros introduce facet forms, facets parse permitted external syntax, or facet outputs cross back into ordinary Gravity. Types/effects/grants/proof facts remain producer- and version-bound; changed subjects invalidate affected facts instead of copying successful labels. Preserve original external-source and macro definition/call/generated locations for diagnostic consumers. Reject opaque string payloads as the primary facet representation, fabricated origins, lost hygiene and stale annotations. Required evidence: actual transformed and serialized syntax/origin products with external/nested/generated round-trip and tamper/loss negatives; domain payload verification remains with its actual owner.
<!-- syntax-facet-source-syntax:end -->


- [ ] `syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts` syntax/3: Invalidate facts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Version attached facts and invalidate them when the producing input or transformation changes; reject stale fact reuse.


<!-- syntax-source-facts:start -->

Apply the [complete syntax source criteria](plan.md#complete-syntax-source-criteria) within this existing task. Preserve its original status, accepted evidence scope and historical criteria.

Complete C3 fact lifecycle criterion (Open): Attach actual compiler-produced tentative namespace, declared profile/effects/capabilities, safety mode, resolved binding, type/effect/ownership facts and proof obligations with producer stage, version and invalidation conditions. When an input or transformation affects a fact, invalidate it before reuse, preserve unaffected facts only when their producer conditions still hold, and consume actual regeneration/replacement where required. Exercise attachment, affected invalidation, unaffected preservation and stale-use refusal through real transformations and downstream consumers. A fixed attached/invalidated demo ledger, current=true label or version field without the corresponding lifecycle is insufficient; syntax metadata cannot invent semantic analysis, authority or proof results.
<!-- syntax-source-facts:end -->

- [ ] `syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify` syntax/4: Serialize and verify

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Round-trip all required form kinds and generated graphs, including shared origin references; demonstrate identity stability and tamper rejection.


<!-- syntax-source-verification:start -->

Apply the [complete syntax source criteria](plan.md#complete-syntax-source-criteria) within this existing task. Preserve its original status, accepted evidence scope and historical criteria.

Complete C3 verification and serialization criterion (Open): Before expansion or analysis, execute the real verifier for required fields, stable semantic ids, spans resolving to actual source units, valid generated producers/origin references, scoped hygiene/declared capture, metadata shape, structurally valid namespace context, allowed phase transitions and actual serialization round-trip. Reconstruct every required source/generated form and shared-origin graph with the same semantic identity and preserved fields; stable serialization serves actual incremental and self-hosting comparison without forcing every representation to share a physical encoding. Reject malformed, substituted, stale, disconnected or non-round-tripping products and prevent downstream use. Exercise all nine C3 diagnostics against actual bad subjects; a projected payload round-trip, fixture-requested failure or passed report alone does not verify the full syntax artifact. Preserve legitimate graph sharing, semantic validation, source authentication and actual host restrictions without project node/depth/width/byte/work quotas or mandatory replacement proof admission.
<!-- syntax-source-verification:end -->

- [ ] `syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers` syntax/5: Integrate consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Exercise Reader -> Syntax -> Expand and Resolve consumption with original/generated diagnostic spans; record source-pinned accepted and rejected artifacts.


<!-- syntax-source-consumers:start -->

Apply the [complete syntax source criteria](plan.md#complete-syntax-source-criteria) within this existing task. Preserve its original status, accepted evidence scope and historical criteria.

Complete C3 artifact and consumer criterion (Open): Produce the actual syntax schema and stream, hygiene context map, origin-chain graph, metadata ledger, generated syntax report, verification report and serialization fixture. Integrate actual C2 construction and selected Expand/Resolve consumption, retaining original/generated source maps, phase/caller-profile/hygiene/metadata facts and invalidation boundaries for analyzers, diagnostics, tools, incremental builds and bootstrap comparison. Emit real C3-SHAPE, C3-ID, C3-SPAN, C3-ORIGIN, C3-HYGIENE, C3-CAPTURE, C3-METADATA, C3-FACT-STALE and C3-SERIALIZE failures with syntax id, form kind, phase, producer, source span, origin chain, hygiene summary and remediation; generated diagnostics expose both generated and producing source/macro-call origins. Reject raw-list macro APIs, originless or mutable syntax, hidden hygiene and generated caller-profile bypass. Bind actual source/implementation/fixture/producer/consumer identities, positive products and negative diagnostics; catalog/schema presence or assigned complete flags grant no component, self-hosting or release acceptance.
<!-- syntax-source-consumers:end -->

<!-- syntax-implementation-tasks:end -->

## Historical migration record

The following original scaffold is frozen history, not implementation progress.
Its preparation-time pending statements and navigation-only authority are
superseded by the implementation ledger above, not silently reclassified.

# Tasks: Syntax workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/syntax-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/syntax-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Syntax implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `syntax-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
