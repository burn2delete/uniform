# Artifact emission implementation specification

Implementation scope added under the 2026-11-01 request. Existing emission and
backend maintainers retain ownership. Historical task transfer below does not
establish artifact generation, conformance or release acceptance.

## Behavior and interfaces

Implement [the Emit outcome](../../docs/component-roadmaps/emit.md#release-outcome)
under [B13](../../docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md)
and [B14](../../docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md),
with actual B1/C14 inputs and selected backend rules. Consume Lower's verified
backend-ready input, explicit target/ABI/runtime/providers, proof/safety/grant
and source/dependency identities. Invoke the real adapter/toolchain and produce
actual output bytes or canonical semantic artifacts, common versioned manifests,
content hashes, source/debug/generated maps, provenance/artifact graphs,
reproducibility/conformance references and owning diagnostics.

The staged Linux LLVM compiler release remains the selected path. Backend
owners implement concrete target families; Emit binds their actual products
and evidence for Runtime, Build, tooling and Release. Development-only evidence
is labeled with the downstream operations it cannot support. Missing required
evidence cannot be hidden by producing bytes or a successful-looking manifest.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`emit/current-evidence-and-remaining-work/`. Status, criteria and finer source
joins remain with the existing implementation and reconciliation owners.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `emit1-reconcile-selected-target-contracts` | Bind exact selected target/ABI/runtime/toolchain tuples, supported operations, output kinds and fresh actual Lower inputs. Keep hosted and native release claims separate. | Reject undeclared targets, stale or missing required evidence and incompatible layouts/services before invoking emission. Retain eligibility, source/op and missing-evidence diagnostics. |
| `emit2-emit-real-outputs` | Invoke actual selected adapters to produce declared objects/executables/libraries or semantic artifacts with explicit runtime/helper linkage and surfaced tool failures. | Reject unsupported operations, tool failure and hidden runtime dependencies. A file name, hand-written output fixture or declared emission is not code generation; retain actual bytes/tool identity and result. |
| `emit3-bind-provenance-to-bytes` | Hash the actual output, bind common schema/kind/backend/profile/target/build/source/compiler/pass/dependency identity, and preserve safety/proof/effect/capability/audit/ABI/runtime and origin records through the artifact graph. | Reject missing, stale, mismatched or contradictory evidence, broken graph/source edges and unrecorded output-affecting environment/nondeterminism. Verify content/canonical bytes rather than trusting supplied hashes. |
| `emit4-establish-conformance` | Execute actual selected-target positives and relevant MIR/reference comparisons; verify metadata, manifest and diagnostic behavior, tamper refusal and reproducibility under its declared scope. | Reject illegal/unsupported inputs and metadata loss even if numerical results match. Assert exact owning diagnostics. Unavailable targets need explicit availability records; shared manifest/rejection tests remain required, and skipped execution earns no execution support. |
| `emit5-integrate-artifact-consumers` | Deliver real content-addressed outputs/manifests to actual Runtime/Build/tooling consumers and retain exact source/fixture/tool/runtime receipts with applicable release evidence. | Reject tampered, wrong-subject or incomplete consumer inputs and promotion of development-only evidence. Signing, SBOM, provenance attestations and seed-retirement acceptance remain with their separate owning outcomes. |

The common B13 artifact contract applies across the wider required backend
kinds; one LLVM output cannot close C/Wasm/JVM/JS/MLIR/GPU/HDL/workflow/query/
mobile artifacts. Their independent backend/domain routes retain full B14
fixture and metadata obligations. Rejected builds retain relevant partial
diagnostic artifacts without becoming accepted products.

## Starting work and completion

Declared Lower/backend/consumer interfaces and verified fixtures allow local
emission work before Build and Release finish. Accepted target emission needs
real adapter output and actual consuming/execution evidence for the claimed
tuple. Component completion requires its supported matrix, complete manifests,
metadata/diagnostic negatives and real consumer composition. Preserve writer
reservations and recorded operation restrictions. Independent acceptance,
integration, wider backend completion and seedless signed release stay separate.

## Historical task-authority-transfer specification

# Feature specification: Emit implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: compiler/backend maintainer.
Input revision: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable authority for
the five existing Emit implementation outcomes. The
[component roadmap](../../docs/component-roadmaps/emit.md) retains selected-target,
hosted-development, producer/admission, accounting and acceptance wording with
a direct task-ledger link. Preserve direct navigation to its
[release outcome](../../docs/component-roadmaps/emit.md#release-outcome) and
[remaining work](../../docs/component-roadmaps/emit.md#current-evidence-and-remaining-work).
These retained contracts remain normative:

- [B13 - Artifact Emission Specification](../../docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md)
- [B14 - Backend Conformance Test Plan](../../docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md)

## User scenarios and acceptance

### US1 - Find one editable implementation task

Preserve five component/heading/title semantic IDs, emit/1 through emit/5 display
labels, exact evidence/status and concrete exit criteria in the sole task
ledger. The roadmap links directly to those records. There is no second editable
implementation ledger or
positional renumbering.

### US2 - Separate workflow delivery from artifact acceptance

The complete historical migration task file remains frozen outside the parsed
implementation region. Its checkboxes do not indicate compiler completion.
All five implementation outcomes remain Open and unchecked. Checking a task
requires explicit Accepted; status and independently reviewed immutable owner
evidence. Structural checks cannot validate semantic receipts. Listing required
records, metadata, assertion counts or workflow artifacts does not establish
real emitted bytes, conformance or seedless release.

## Scope and compatibility

Own the Emit roadmap and four workflow artifacts. Preserve complete history,
B13/B14 and every original package link target.
Preserve all roadmap wording outside additive task navigation. Baseline plan blob
`d62412306c4a726aa9c077a1abd0b72158c55677` has no mapped appendix; none is
introduced. Other packages, compiler/runtime implementation/tests, normative
contracts and mapping artifacts remain unchanged.

## Evidence and outcome ownership

The compiler/backend maintainer retains semantic ownership. Lower establishes
legal backend input and Runtime supplies service/ABI contracts. Build consumes
emitted records later. Preserve the selected staged Linux LLVM candidate and
explicit hosted seed/host boundary. Additional backend families retain their
separate ownership and cannot replace the selected candidate implicitly.
Independent review and integration remain separate from local validation. See
[evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Actual artifact bytes, content identity, provenance and backend conformance
still require source-pinned execution and owner review. This migration provides
no artifact/runtime acceptance, whole-backend support, self-hosting, seedless
signed release or seed retirement.

## Implementation naming review

`emit-workflow` and its component roadmap name the subsystem and behavior. Follow the
[semantic naming rule](../../.specify/semantic-names.md); compatibility IDs,
historical planning references and compiler source names remain unchanged.
