# Self-hosted compiler and release implementation specification

Implementation scope added under the 2026-11-01 request. Existing bootstrap,
release, trust and governance owners retain their responsibilities. Historical
workflow migration below does not establish self-hosting or seed retirement.

## Behavior and interfaces

Implement [the Release outcome](../../docs/component-roadmaps/release.md#release-outcome)
under the retained BOOT1-BOOT8 contracts,
[TEST13](../../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md),
PKG7/10/12, D2/D9, applicable governance and the separate
[Phase 18 extension](../../docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md).
Consume the complete declared compiler/generated/runtime/library/package/build
source closure, actual prior compiler, locked build/environment/target inputs,
real component conformance and independent trust/review evidence. Produce real
next-stage compiler artifacts, process/rebuild logs, accepted/rejected execution,
equivalence and diagnostic comparisons, BOOT5 support/gap records, TCB/lineage,
verified provenance/SBOM/signatures and the actual public release distribution.

The [existing SH completion plan](../../docs/self-hosting-milestone-completion-plan.md)
and [SH task crosswalk](../sh-family-crosswalk/spec.md) retain milestone/task
authority. SH-25 constructs and executes the complete component closure; SH-26
actually invokes the prior Gravity compiler to build and run the next stage;
SH-27 compares clean real rebuilds; SH-28 verifies trust and seed boundaries;
SH-29 delivers and verifies the public executable. These are separate outcomes,
not a single descriptor-validation step or a new release checklist here.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`release/remaining-tasks-and-evidence/`. Preserve its five existing records,
source-family refinements, status and evidence boundaries.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `bind-each-claimed-component-to-exact-implementation-and-conformance` | Bind the full claimed source/component/dependency closure and each required operation to actual implementations, accepted/rejected programs, producer/consumer composition and exact conformance. Consume the existing governance evidence interfaces with their policy owners. | Reject omitted/replaced components, fixture-only inventories, hidden dependencies, unsupported profile/target claims, missing review/lifecycle evidence and source/fixture mismatch. Accepted narrow modules or historical stage counters cannot close the whole compiler or 240-document language surface. |
| `complete-stage-compatibilityequivalence-across-the-claimed-compiler` | Use actual stage artifacts and clean controlled rebuilds under declared BOOT7 comparison modes, including content/manifests, diagnostics, conformance, runtime output and applicable IR/performance. Record versioned stage support/gaps, reviewed deltas and residual TCB. | Reject missing output, unexplained semantic/diagnostic drift, overbroad normalization, stale comparison inputs and claimed support without conformance. Supplied comparison records cannot replace the required builds/execution. |
| `produce-actual-self-hosted-compilerruntime-execution` | Invoke the actual prior Gravity compiler on the complete next-stage compiler/runtime/library/build closure, link/package the result and execute it on legal and illegal compiler/application inputs. Exclude Clojure from candidate compilation/runtime/build paths and retain actual process outcomes and lineage. | Reject simulated rebuilds, substituted builders/components, failed/incomplete output and bootstrap-hosted fallbacks presented as self-hosting. A Gravity-authored record validator or public wrapper alone is insufficient; preserve separate SH-25 construction, SH-26 rebuild, SH-27 comparison and final release acceptance. |
| `build-reproducible-signed-distributions-with-provenancesbom` | Rebuild real distribution content under the same locked controlled recipe; compare required binary/manifest/provenance/SBOM/signing/command evidence, verify canonical subject-bound signatures/attestations and traverse compiler lineage with explicit trust roots. | Reject tamper, wrong subject/source/compiler, undeclared generated/binary inputs, unreviewed drift, unknown required schema, revoked inputs, incomplete SBOM, stale or policy-incompatible identity/log/root evidence. Signing and publication retain their actual authority and custody boundaries. |
| `obtain-independent-acceptance-for-the-exact-release-candidate` | Supply the exact supported candidate and complete component/stage/trust/command/governance bundle for coordinator-routed independent acceptance and explicit release disposition. | Reject missing/stale evidence, author-only acceptance, unsupported targets and any remaining Clojure path inside the claimed public compiler/runtime/build/release boundary. Flags and passed structural validators cannot confer release acceptance. |

Final Phase 18 behavior includes real public `check`, `run`, `compile` and
emitted-program execution for both `.gravity` and `.qst`, a nontrivial
application, stable feature-specific failures, full claimed conformance and
successful self-host verification through the self-hosted executable. Keep
`gravity-bootstrap` explicitly outside that boundary. Existing incomplete
P15-S23/P18-T03/T05/T06 evidence stays incomplete until those actual conditions
hold; do not promote early packaged JVM or fail-closed wrapper receipts.

## Starting work, composition and complete-project scope

Ordinary component development uses explicit interfaces and focused real
fixtures without first completing authoritative migration or final release.
Stable module admission later supplies its BOOT3 stage comparison and migration
evidence. Complete component construction precedes actual next-stage rebuilding,
then equivalence/trust/public release consume the real results under the
existing dependency representation. Do not impose final-stage acceptance as a
local start prerequisite or erase a required composition edge.

The selected compiler release uses its required Runtime compiler services,
Standard Library compiler support and compiler CLI closure. Wider required
language, library, domain, backend and Platform outcomes remain in scope with
their own owners and evidence; the first seedless release cannot establish
240/240 conformance or universal target support. Preserve current restrictions,
reserved writers, denied operations and coordinator integration responsibility.

## Historical workflow-transfer specification

# Feature specification: Release workflow migration

Status: prepared; review and integration pending.
Migration owner: backend workflow migration owner. Component owner role: bootstrap/release maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Adopt the repository's Spec Kit artifact workflow for the Release component
without copying its normative semantics or creating a second implementation ledger.
[Component release outcome](../../docs/component-roadmaps/release.md#release-outcome) and [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence) retain outcome and acceptance scope; implementation records are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger.
The following contracts remain canonical and are linked, not replaced:

- [BOOT1 - Bootstrap Strategy](../../docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md)
- [BOOT2 - Seed Compiler Design](../../docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md)
- [BOOT3 - Self-Hosted Compiler Plan](../../docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md)
- [BOOT4 - Compiler-in-Gravity Coding Standard](../../docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md)
- [BOOT5 - Stage Compatibility Matrix](../../docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md)
- [BOOT6 - Trusting Trust and Reproducible Bootstrap Plan](../../docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md)
- [BOOT7 - Self-Hosting Validation and Equivalence Plan](../../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md)
- [BOOT8 - Bootstrap Artifact Provenance Specification](../../docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md)

The [binary release extension](../../docs/phase-18-binary-distribution-and-seedless-release/README.md) remains separate from the 240-contract inventory.

## User scenarios and acceptance

### US1 - Find one authoritative path from work to evidence

A maintainer starts here, follows plan.md to the existing remaining tasks and
acceptance criteria, and records the exact implementation evidence there.
Acceptance: all four package artifacts exist, every local link and anchor
resolves, ownership/dependencies are explicit, and no implementation task is
duplicated or marked complete merely by this migration. A missing contract,
broken task link or unsupported completion claim rejects package acceptance.

### US2 - Preserve the component boundary

Release admission consumes accepted component outcomes and exact candidate evidence. Ordinary development is not release admission. Runtime compiler-services, Standard Library compiler-support and selected compiler CLI outcomes bound the compiler prerequisite surface; wider platform outcomes remain separate. No seed retirement follows from this migration.
Acceptance: review confirms these boundaries agree with the linked roadmap.
Changing a target, grant, semantic contract or release claim requires its own
implementation decision and evidence, not a checkbox in this package.

## Scope and compatibility

Owned files are `specs/release-workflow/{spec,plan,tasks,evidence}.md` and
the task link in `docs/component-roadmaps/release.md`. Existing contracts, phase tasks,
other packages, compiler/runtime source, shared helpers and product behavior
remain unchanged.
This is artifact workflow migration, not component implementation, wholesale
contract replacement, external Specify CLI execution or automatic skill discovery.

## Evidence and outcome ownership

The migration owner prepares and checks artifacts. The component role retains
semantic ownership; this package does not assign an individual implementation
owner. An independent reviewer accepts exact package evidence; Deus owns
integration. See [evidence](evidence.md) for prepared, checked, reviewed and
integrated states, kept separate from component acceptance.

## Unresolved items

Independent package review and main integration remain pending. Existing
component decisions and open implementation evidence remain in the canonical
roadmap; no new generic approval gate is introduced.

## Implementation naming review

`release-workflow` identifies the subsystem and purpose. No new implementation
names or protocol identifiers are introduced. Follow the
[semantic naming rule](../../.specify/semantic-names.md); legacy planning IDs
remain traceability metadata only.
