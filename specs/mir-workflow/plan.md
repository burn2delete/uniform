# MIR implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the historical transfer plan below. Read [spec.md](spec.md), C11 and the owning
source rule for each selected operation/fact. Resolve actual Gravity semantics,
temporary transport and current focused fixtures through
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name actual Core and analysis producers, operation families, CFG/data-flow/fact
tables, source/proof edges, verifier and Opt/Lower consumer. Provide real legal
construction plus targeted malformed-flow, dominance, stale/missing-fact,
ordering, check-guard, origin and target-leak mutations relevant to the slice.
Use an explicit fixture interface for local development and actual producers
for composition; do not accept a supplied passed report as verification.

Run focused owner checks and affected producer/verifier/consumer composition.
Record exact artifacts, serialization/invalidation observations and passed,
failed or unrun cases in the selected existing authority. Preserve C12 and
other recorded restrictions and reserved writers. Retain independent acceptance,
complete operation-family coverage and release as separate outcomes.

## Historical task-authority-transfer plan

# Implementation plan: MIR task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler MIR maintainer.
Input revision: `d5d8e15c773929337e1d9646da4f32af2d461e17`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[C11 MIR specification](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md).
The [specification's canonical contracts](spec.md#purpose-and-canonical-contracts)
retain the original package navigation.
The workflow records tasks and evidence without replacing normative authority.

Retain the existing navigation and semantic ownership boundaries:

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/mir.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [MIR canonical remaining work](../../docs/component-roadmaps/mir.md#current-evidence-and-remaining-work) | mir/1 through mir/5; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/mir.md#acceptance-criteria-yesno) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: compiler MIR maintainer. Interface owners: [core](../../docs/component-roadmaps/core.md#release-outcome), [legality](../../docs/component-roadmaps/legality.md#release-outcome), [opt](../../docs/component-roadmaps/opt.md#release-outcome), [lower](../../docs/component-roadmaps/lower.md#release-outcome).
Real checked-Core facts are required for integrated MIR construction. Opt and Lower may use pinned verified fixtures while upstream analyses mature. Domain anchors apply only to programs using them; no restricted domain work is enabled.

## Approach and exact files

Move only the five real implementation records into the marked
[implementation region](tasks.md#implementation-tasks). Each preserves a stable
semantic ID, display title, unchecked checkbox, exact evidence/status and concrete
exit criteria. Preserve the full historical tasks file as a frozen suffix. Its
four migration tasks remain separate from compiler completion.

The roadmap links to the task ledger, which retains all admission and acceptance
wording, source links and milestones. No producer or consumer receipt
is imported as task completion. New task authority is an editing location, not
an admission result or a change to the requirement for checked producer facts.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved task criteria, IDs and historical suffix. Check the
affected Markdown documents for ASCII and file/heading links, preserve the
canonical-truth links and C11 reference, and run whitespace plus clean
exact-base/head/tree integration preflight. No compiler semantic suite is
required here.

## Delivery and remaining work

Independent review inspects the complete public payload and proposed PR body.
Publish a draft with exact identities and validation receipts for maintainer
current-head review. Normal integration follows maintainer disposition and
repository gates. All five implementation outcomes remain open; no source,
producer, consumer or final artifact acceptance follows from this transfer.

## Names, callers and compatibility

Preserve existing anchors, status/acceptance text, task identities and historical
pins. No compiler caller, runtime API, wire/schema identity, diagnostic, manifest
hash or load-order change is included. Semantic tool names follow the accepted
component entrypoint convention.
