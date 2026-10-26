# Finite acceptance dependencies

Inspection baseline: `176c4ee65bb3ceb545f51edfaa599902030a0cb7` (2026-10-26).
Outcome owner: dependency and completion-boundary maintainer. Existing component
writers retain their paths. This is the sole detailed FIN cleanup backlog;
[the existing component index](component-roadmaps/README.md#cleanup-completion-boundaries)
provides shared navigation. It does not reproduce component implementation status.

## Scope and completion

This cleanup removes ambiguous, circular or indefinitely extensible acceptance
definitions from the existing roadmap layers. The 240 normative contracts,
their stable IDs, all 31 retained Spec Kit packages, historical evidence,
outstanding decisions and accepted approvals remain. Mapping #422 gives 236
direct package links and four explicit GOV1/GOV6/GOV7/GOV8 proposals, not four
accepted replacements. Future explicitly authorized packages are additions,
not a reason to retire those contracts or rewrite the historical population.

FIN is complete only after FIN-01 through FIN-05 below are independently
reviewed, validated and integrated, with no unowned, infinite or circular
in-scope criterion. This is acceptance of the cleanup definitions and their
executable structural checks. It cannot close any unfinished compiler task,
other cleanup owner's backlog, final self-hosting gate or future language work.
The overall six-outcome cleanup remains open while any owner's declared outcome
is unfinished or blocked. A named role is accountable for resolving a missing
source owner; it is not proof of a source reservation or permission to overwrite.

## Source-grounded cycles

`tools/acceptance_dependencies.clj` reads all 240 canonical documents and records
their SHA256 digests. It reads the component registry and every registered
component roadmap; identifies an implementation ledger only inside its explicit
implementation markers; and excludes historical migration checkboxes outside
those regions. It extracts all 36 full-language tasks and 30 SH completion rows.
Its EDN output contains exact root-relative source identities, directed edges
and strongly connected components. It performs no compiler execution, evidence
authentication, task write or acceptance promotion.

Edges point from consumer to prerequisite. Reading the existing full-language
phase dependency declarations as final-task-completion prerequisites produces
these actual mutually reachable sets (not just pairs of cross references):

| Strongly connected set | Concrete circular requirement | Staged contract and accountable boundary |
| --- | --- | --- |
| FL-P04-T01, FL-P05-T02, FL-P06-T03 | Performance needs lowering; lowering needs certified math; math needs performance/lowering. | Compiler, math and performance maintainers publish explicit IR/certificate interfaces, implement their own operations, then execute certificate-preserving joint lowering. No proofless check elision or math claim passes on an interface. |
| FL-P07-T01, FL-P08-T02 | Backend acceptance requires the replaced runtime; runtime requires the backend. | Backend and runtime maintainers agree ABI/services, then independently validate emitted artifacts and actual runtime behavior on the same candidate. Host execution alone cannot establish seed retirement. |
| FL-P10-T01, FL-P12-T01 | Source schemas need packages; packages need source schemas. | Schema and Build maintainers implement declared schema/manifest interfaces separately, then exercise real hermetic builds, schema weakening rejection and provenance on the joint candidate. |
| FL-P11-T01, FL-P13-T01, FL-P13-T02, FL-P14-T01 | AI and tooling need the harness; the harness requires those feature phases. | Conformance maintainer builds executable selection/diagnostic interfaces first; AI and tooling maintainers supply actual cases. Full-population harness acceptance still requires executing them. |
| FL-P17-T01, FL-P18-T01 | Governance requires public release; release requires governance. | Governance maintainer supplies executable rejection rules; release maintainer produces the real candidate and evaluates them before final acceptance. No governance record is waived. |

The wildcard "all feature phases whose behavior is under test" expands to
phases 01 through 13 plus 16. Foundation is governance of this track; Bootstrap,
Governance and Release are separately gated final consumers. "All previous
full-language tasks" expands to every task in phases numerically before 18.
These expansions are explicit audit interpretation, tested by fixtures, not
inferred runtime dependency discovery. Unmentioned implementation dependencies
must still be inventoried by their actual component owner.

There is also a textual final-proof backedge: FL-P14-T02 requires the self-hosted
public binary, while Phase 15 names FL-P14-T02 as a prerequisite. The task-edge
extractor does not infer this edge from prose. The explicit correction in
[the full-language work order](full-language-implementation-gap-map.md#dependency-stages-and-finite-acceptance)
allows the executable runner implementation to be consumed by the rebuild;
both tasks retain their final proof on the actual rebuilt/public candidate.
It does not accept an unexecuted runner or exclude any required feature.

The 30 SH completion dependencies are acyclic. Their existing source closure,
six acceptance requirements and stopping rules remain in the
[milestone plan](self-hosting-milestone-completion-plan.md).
Component Markdown links are reported separately as reference edges. Reader to
Syntax and Syntax to Reader links, or Auth/Diagnostics references spanning
components, do not by themselves establish circular acceptance. The existing
component index already distinguishes declared interfaces from completed
releases; this correction preserves that distinction at the full-language layer.

## Executable boundary

Each task retains its existing ID and full scope. The staged graph has four
facets: `interface`, `implementation`, `composition`, `acceptance`. These are
evidence dependencies, not a new four-task successor chain. Construction uses
declared interfaces. Composition depends on every original prerequisite's real
implementation, not its document or final checkbox. Acceptance requires that
composition and the complete unchanged task-specific proof gates. The graph
does not encode or authenticate those proof receipts; they remain obligations
of the owning task and independently reviewed acceptance.

The checker verifies all referenced graph nodes exist, identifies true strongly
connected sets (including self edges), and demonstrates that every original
prerequisite remains an executable composition input. It rejects missing or
duplicate IDs, unknown dependency syntax, malformed implementation regions,
unknown prerequisites, SH cycles and a cyclic staged graph. It never treats
zero staged cycles as proof that the components run or that all prose has been
formalized. Newly discovered semantic dependencies require explicit owning
contract updates; they cannot be hidden by a green structural report.

Run from the repository root under the existing normal resource brokers:

```bash
clojure -J-Xmx256m -M tools/acceptance_dependencies.clj --check
clojure -J-Xmx256m -M tools/acceptance_dependencies_test.clj
```

The first command prints the graph and source digests; it writes nothing.
The second runs pure graph/parser and real-source read-only controls, including
unknown/self/cyclic dependencies, executable edge conservation, historical
migration exclusion and attempted write/extra-argument rejection. Neither needs
the heavyweight lock, C12 execution or a compiler namespace.

## Finite task-authority migration population

The existing component registry is the finite population, not an endless series
of successor PRs. Each registered component's existing finite task sections must
have exactly one editable implementation authority; the other views are links
or deterministic projections. Compiler API's built-in pipeline, pass evidence
and plugin host; named Backend families; Developer Tools services; domain
projects; and Runtime/Standard Library selected-versus-wider outcomes remain
separate named sections. A migration must preserve every original row, stable
ID, status, exit criterion and historical record in those sections.

The task-authority migration owner owns this transfer and its existing
projectors. Completion is a readback of the fixed registry and every declared
section showing no remaining editable duplicate, missing row or stale
projection, with independent review and integrated evidence. Per-component
successors stop when that population is exhausted. Optional new component
scope requires an explicit decision and a changed registry, not a silent new
condition attached to this cleanup. This criterion does not mark any underlying
implementation row accepted. In-flight Backends and other existing migrations
remain that owner's work; no second writer is created here.

## Finite FIN backlog

| ID | Exact deliverable and dependencies | Yes/no acceptance evidence |
| --- | --- | --- |
| FIN-01 | Source census: canonical inventory/documents, component registry/roadmaps, explicitly marked Spec Kit implementation regions, full-language gap map and SH milestone index. No implementation dependency. | Exact source digests and source-derived graph populations; historical migration boxes excluded; unknown/duplicate/absent-node negative controls. All 240 contracts and 31 baseline packages remain retained, with the four explicit GOV proposals preserved. |
| FIN-02 | Actual SCC classification in this document and `tools/acceptance_dependencies.clj`; depends on FIN-01. | Five coarse full-language SCCs and the explicit runner/public-binary prose backedge explained; SH graph checked; component links labeled reference-only. No invented cycle or claim of exhaustive semantic dependency inference. |
| FIN-03 | Staged contracts in the existing full-language gap map and finite authority-migration population above; depends on FIN-02. | Every original task/status/proof gate and executable prerequisite retained; construction can proceed independently, composition needs actual producers, final proof cannot be satisfied by an interface or synthetic receipt; no new safety deferral. |
| FIN-04 | `tools/acceptance_dependencies_test.clj` plus shared cleanup navigation in the existing component index; depends on FIN-03 and each owner's one detailed contract link. | Positive/negative graph controls, exact source readback, docs/roadmap/language checks, one canonical navigation with ACC/RV/CO/OWN/AS/FIN IDs and no duplicate component status. No broad compiler validation credited. |
| FIN-05 | Independent review and normal integration of the completed FIN increment; depends on FIN-01 through FIN-04. | Exact base/head/tree, scoped passed/failed/unrun receipt, independent review disposition, current-main compatibility and remote/main readback. Keep unfinished work draft. Other owners' unfinished acceptance and restricted operations remain open. |

These records define finite deliverables and carry no inferred progress score.
Observed validation and exact review/integration receipts belong in the change's
public PR record. Historical failed syntax/launcher attempts remain failed;
only successful final-tree commands receive passing credit. No compiler suite,
current-source construction, C12 run, authoritative migration or public seedless
acceptance is part of this documentation/tooling increment.
