# Uniform adoption constitution

Version: 1.2.0 | Adopted: 2026-10-25 | Updated: 2026-11-01 | Change authority: Matt; integration coordinator: Deus (parent)

This is a concise record of existing user decisions for Spec Kit planning.
It does not replace [AGENTS.md](../../AGENTS.md), the
[canonical inventory](../../docs/document-inventory.json), or the owning
language contracts. Relative links here resolve from this file.

## Semantics and correctness

Follow D0, D1, D2, D3, D6, D8 and D9 in the
[foundation phase](../../docs/phase-00-foundation-and-thesis/README.md).
Preserve Gravity semantics, compiler-stage boundaries, source provenance,
effects/capabilities and safety evidence. Host behavior is not a language
contract. Use positive and negative fixtures when the owning contract requires
them. A passing structural check is not semantic conformance.

## Autonomy without project accounting quotas

Do not introduce project cost, budget, token, accounting or work quotas,
arbitrary admission limits, routine approval tickets or repeated permission
ceremonies. Managers execute authorized work autonomously and coordinate exact
file ownership. Real host safeguards and required language/runtime contracts
remain distinct from project work quotas. Existing authorization and specific
restrictions continue to apply; Spec Kit cannot bypass them.

## Reviewable progress and completed main

Open and maintain draft PRs early for meaningful partial or ongoing work.
Partial work can remain visible in a draft; do not require all unrelated goals
to finish before opening it. Main receives the completed declared change after
its required validation and review. Do not claim completion from a draft,
a task checkbox, a stale receipt or an exit code alone. Deus alone coordinates
integration decisions; the former master is an execution/integration delegate.

## Exact outcomes and evidence ownership

Each change names its owner, exact paths, acceptance and rejection behavior,
input revision/tree, commands, result/exit, artifact identities and limits.
Distinguish locally observed evidence from another owner's reported evidence;
name unrun checks and stale inputs. Implementation owners own their source and
raw evidence; reviewers own review dispositions; Deus owns integration.
Use the existing [development workflow](../../docs/development-verification-workflow.md)
for proof, reuse and authority decisions. Spec Kit is a work-artifact layer,
not a competing evidence authority or a second roadmap.

## Scope and changes

Canonical language changes go to their owning docs. Update this record only
when an actual user decision changes. Matt's 2026-11-01 request authorizes
project-wide implementation coverage through the existing Spec Kit workflow:
all 240 documents, the separate Phase 18 extension and existing full-language,
self-hosting and seed-retirement obligations. Reuse existing requirement
reconciliation, task authorities, dependencies and evidence rather than starting
a competing system. Preserve normative source authority and historical records;
do not bulk-copy the contracts into feature specs. Each substantive requirement
needs an implementation owner, an actionable task or explicit future task,
source traceability and required acceptance evidence. Genuine unresolved
decisions and restrictions remain visible with their affected work and resolution
path. Planning completion does not establish implementation or acceptance.

No new scheduler, autonomous hooks, credentials or global Codex configuration is
authorized.
C12 cybersecurity, the denied oracle Git action and advisory reviewer wake
remain restricted and cannot be retried, transferred or reframed here. The old
coordinator hourly automation remains paused. This adoption PR is reviewed by
Deus; it does not grant itself merge authority.

## Semantic implementation names

Matt's adopted naming rule: planning identifiers such as `SH07`, `C2` and `C16`
belong in spec/task metadata and contract cross-references, not new implementation
filenames, namespaces, application names or API names. Use short recognizable
subsystem/behavior words from the actual architecture; see
[semantic naming guidance](../semantic-names.md). No new numbered replacement
codes or numeric naming-length quota is introduced. Existing names are explicit
incremental migration debt, not a global development stop. Real protocol, schema,
wire and diagnostic identifiers with compatibility meaning are distinct; preserve
them unless their owning contract explicitly approves a versioned migration.
Do not rename restricted work to evade a classifier or transfer its restrictions.
