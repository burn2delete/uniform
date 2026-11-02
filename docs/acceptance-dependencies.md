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
deleted unreferenced leaf tasks as closure: the real audit enforces the explicit
existing 36 task identities and SH-00 through SH-29, with malformed-row controls.
An intentional population amendment requires an owning-contract change and
independent review of the new identities. The checker never treats
zero staged cycles as proof that the components run or that all prose has been
formalized. Newly discovered semantic dependencies require explicit owning
contract updates; they cannot be hidden by a green structural report.

Dependency lists consume their whole declaration or table cell: IDs must match
exactly, use the original comma separators and, for full-language declarations,
retain their backticks and final period. `SH-XX`, `SH-003` and `FL-P06-T030`
are rejected rather than silently omitted or truncated. Unknown references,
duplicate references, empty/trailing cells and ambiguous separators reject.
Implementation ledger IDs are read literally inside their marked region;
the census does not require a fabricated component prefix or replace the
owning projector's exact identity validation. Existing Backends keys such as
`backend-llvm` therefore remain unchanged.

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

## Future task construction and composition

The crosswalk captures the exact forty future task bodies from the four
existing marked regions in Domains, Release and Platform. Each keeps its sole
editable component task file, source pin, literal ID, checkbox and full closing
evidence. Capturing these records does not add SH criteria, route keys or
accepted evidence. A changed, missing or duplicated body invalidates this view.

The following declarations formalize the wider Domain tasks' explicit
`Dependencies consume` clauses, the Math/Schema producer and checker boundaries,
the Math parent's independent-verdict consumer, and the Platform Product records together
with [their existing composition plan](../specs/platform-workflow/plan.md#product-decision-and-implementation-order).
They are dependency declarations, not another task/status ledger. Full task IDs
are literal; `none` means this table declares no prerequisite at that boundary.
Each named producer supplies only the selected output described by the
consumer's existing full criteria. A broad parent checkbox is never a proxy
for that output's execution or acceptance.

Construction can use versioned declared producer interfaces. `Start results`
identifies the adopted product decisions needed for selected product work;
ordinary interface/fixture development can precede adoption. `Composition
results` requires real selected outputs before claiming that consumer result.
Start results are also composition inputs. Every selected result stays
execution-and-review-unassessed here, including outputs of checked historical
parents. This view authenticates no execution receipt. Its frontier is advisory
and grants no platform, provider, device or publication permission.

<!-- future-composition-dependencies:start -->
| Consumer | Start results | Composition results | Selected output contract |
| --- | --- | --- | --- |
| `automation` | none | `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | Typed operation graph, granted roots/commands, dry-run and actual action audit are distinct outputs. |
| `blockchain` | none | `schema`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` | Checked versioned state/ABI schemas and deterministic safe transition/authority facts; preserve selected chain rules and current accounting removal. |
| `crypto` | none | `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` | Secret/key authority, audited boundaries and protocol-specific checks; metadata does not prove cryptographic execution. |
| `formal` | none | `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes` | Surviving obligations and exact emitted subject/proof identities, followed by actual checker/integration results. |
| `native` | none | `backend-llvm`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` | Actual selected native artifact and preserved checks/anchors; performance claims require the consumer's real measurements. |
| `services` | none | `query`, `schema` | Executed typed queries and checked request/result/schema contracts with selected service authority and failures. |
| `simulation` | none | `math`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | Selected numeric/rounding contracts and actual runtime boundary behavior for deterministic simulation. |
| `storage` | none | `schema`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | Checked durable data contracts and actual ownership/FFI execution with recovery and failure cases. |
| `visual` | none | `workflow`, `schema`, `ui` | Checked visual/schema mappings and actual workflow/UI operations; visual graph construction alone is insufficient. |
| `math` | none | `math/independent-proof-and-certificate-checking` | Proof-dependent equality, normalization, transforms, provider use and conformance consume the exact independently checked claim/certificate/verdict; numeric-family declarations alone do not require a completed checker. |
| `math/eml-normalization-and-search` | none | `math`, `math/symbolic-rule-and-trace-production`, `math/independent-proof-and-certificate-checking` | Verified EFIR, numeric mode and guarded rule facts feed candidates/traces; promotion consumes the applicable exact accepted proof or bounded certificate and independent replay. |
| `math/certified-implementation-synthesis` | none | `math`, `math/interval-and-real-proof-production`, `math/independent-proof-and-certificate-checking` | Verified EFIR/mode/floating eligibility and separate real/roundoff ledgers feed executable candidates and certificates; independent exact-subject checking precedes accepted ranking/selection. |
| `math/interval-and-real-proof-production` | none | `math`, `math/independent-proof-and-certificate-checking` | Verified EFIR/mode feeds pending claims and exact domains/partitions/ledgers; allowed residual checks consume independent partition replay, preserving unresolved cells and failure behavior. |
| `math/symbolic-rule-and-trace-production` | none | `math`, `math/independent-proof-and-certificate-checking` | EFIR identity, numeric/proof policy and exact independent verdicts feed rule promotion, guarded rewrite and extraction; registration and candidate trace production precede accepted use. |
| `math/independent-proof-and-certificate-checking` | none | `math`, `math/eml-normalization-and-search`, `math/certified-implementation-synthesis`, `math/interval-and-real-proof-production`, `math/symbolic-rule-and-trace-production`, `pass-evidence`, `formal`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` | Independently check the selected actual EFIR/claim/candidate/certificate, EML/symbolic trace or partition with exact assumptions/trust/target/invalidation; these proof methods are applicable alternatives, not a demand for every method on every claim. |
| `schema/compatibility-and-migration` | none | `schema`, `schema/checked-consumer-projection`, `query`, `backend-query`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | Actual old/new source and affected projection contracts feed semantic diffs and checked plans; actual granted migration/reload transitions supply result, state, rollback/loss, lifecycle and replay evidence. |
| `schema/checked-consumer-projection` | none | `schema`, `schema/compatibility-and-migration`, `pipeline`, `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit`, `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `query`, `backend-query`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`, `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `pass-evidence`, `release/remaining-tasks-and-evidence/execute-formal-evidence-checks`, `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance` | Same-source generated API/database/ABI/config/artifact products consume applicable checked compatibility/migration and real compiler/handler/FFI/reload/evidence-consumer outputs. Layout, STD10 codec consumption, Runtime call/callback and actual release decisions remain distinct. |
| `platform-product/adopt-hardware-contract` | none | none | Reviewed ISA/alternative, device model, simulator and executable workloads; hardware architect remains unassigned. |
| `platform-product/adopt-kernel-contract` | none | none | Reviewed boot, memory, scheduling, IPC, service and driver interfaces; OS architect remains unassigned. |
| `platform-product/adopt-application-contract` | none | none | Named end-user workloads and accepted/rejected cases; product owner remains unassigned. |
| `platform-product/adopt-physical-boundary` | none | none | Reviewed simulation/FPGA/reference/fabrication scope and required external owners/provenance/availability; hardware/product owner remains unassigned. |
| `platform-product/implement-hardware` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-physical-boundary` | `hardware` | Uniform-authored selected design plus applicable structural/state/reset/timing/CDC/tag target conformance. |
| `platform-product/implement-firmware` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract`, `platform-product/adopt-physical-boundary` | `platform-product/implement-hardware`, `firmware` | Actual selected hardware and startup/boot/image contract plus applicable firmware target conformance. |
| `platform-product/implement-os` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract` | `kernel` | Uniform-authored selected services and applicable kernel target checks; versioned boot/driver interfaces permit independent development. |
| `platform-product/implement-drivers` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract` | `devices` | Uniform-authored drivers on the named substrate and applicable device/provider conformance; service ABI is a declared interface before stack integration. |
| `platform-product/implement-applications` | `platform-product/adopt-application-contract` | `platform-product/implement-os`, `platform-product/implement-drivers` | Actual selected OS/driver service outputs used by every adopted workload, with versioned interfaces available during construction. |
| `platform-product/integrate-execution` | none | `platform-product/implement-hardware`, `platform-product/implement-firmware`, `platform-product/implement-os`, `platform-product/implement-drivers`, `platform-product/implement-applications` | Same actual five-part stack boots and executes declared workloads with isolation, authority and device-failure evidence. |
| `platform-product/distribute-product` | none | `platform-product/integrate-execution`, `platform-product/adopt-physical-boundary` | Reproducible distribution of the actual integrated stack; physical deliverable/bring-up only where adopted. |
<!-- future-composition-dependencies:end -->

These declarations cover twenty-seven future consumers and the existing Math
parent, with source/routing review still pending. The thirteen Release testing/
seed tasks retain their complete source clauses and are explicitly reported as
unresolved dependency joins. Release suite execution needs selected actual subject/oracle
interfaces, followed separately by stage comparison, intermediate admission
and final release. No empty adjacency may label those joins complete.

The versioned boot and service interfaces resolve construction cycles without
removing the integrated stack's actual five producers. Four product adoption
tasks remain material decisions with unassigned accountable people. A selected
compiler release does not require every product surface universally; complete
project scope still retains those outcomes. Future-to-SH and full-language
finish-line joins, source conjunction review, independent acceptance and
integration remain pending. Existing full-language SCCs and final proof gates
are unchanged by this table.

### Math output dependencies and Schema staging

The checker derives forty-eight existing `math/*` criterion outputs directly
from the exact captured bodies of the Math parent and its five future Math
tasks. For each it preserves the full source-bound criterion, canonical MATH
identity, output declaration, literal `Dependencies:` clause and unchanged
external input contract. Every named Math dependency must resolve to exactly
one criterion in its existing task. Unknown, duplicated or malformed declarations
reject. These are output facts, not forty-eight new tasks or accepted features.
The projection connects cross-task selected results to their exact named output
inputs and reports both the detailed output graph and coarse task backedges.
It does not infer semantic coverage from those edges or clear pending review.

Verified EFIR and explicit numeric facts permit untrusted EML, approximation,
interval and symbolic candidates to be produced before any proof-dependent use.
The independent checker consumes exact recorded candidate/trace/partition inputs;
it cannot trust a producer's pass flag, assume the conclusion or rerun heuristic
search as proof replay. The parent's equality and transform gates consume the
applicable accepted verdict before normalization, extraction, selection or check
elision. A claim chooses a method allowed by D9/MATH5/MATH6/SAFE15; the dependency
declaration does not require all symbolic, interval, SMT and approximation
methods for each claim. The four literal checker criteria and its full MATH5/
MATH10 certificate contract remain one checker authority. The older parent
checker allocation stays qualified for current repartition review.

The detailed literal Math output graph exposes which input facts precede each
operation. Its acyclicity does not prove that every prose dependency has been
formalized: independent certificate use, general compiler facts, target/provider
execution, foundation/profile/safety clauses and full conformance remain explicit
task contracts and selected-output obligations. Current accounting removal still
supersedes historical compiler fuel, candidate/depth/iteration allowances and
quota-only refusals. Preserve numeric error, domain, branch, outward rounding,
target representability and actual host/provider constraints.

Schema's coarse projection/compatibility backedge also remains visible. S1
source validators and S2/S3/S7 contracts permit an initial typed projection
candidate without a migration verdict. Compatibility consumes that exact old/new
projection pair, then produces the checked diff and applicable migration or
reload plan. Actual generated consumers, migration execution or reload use the
matching checked policy/result and declared Runtime authority. S7 layout stays
with Lower, codecs with their STD10/Schema boundary and actual call/callback
checking with Runtime. S9 validates the exact evidence graph and required checker
results; its gate grants no publication authority. The complete inherited C6
consumer conjunction remains required, with its later source amendment and
current independent review qualification preserved.

## SH acceptance to Spec Kit task projection

The [SH-family crosswalk](../specs/sh-family-crosswalk/spec.md) extends the same
read-only dependency representation. Its original inventory verifies SH-00
through SH-29, six criteria each: 180 acceptance-planning units. The separate
SH07-C01 through SH07-C18 sequence expands the same six SH-07 criteria. Neither
population adds implementation completion units. Component tasks retain their
literal IDs and sole editable marked Spec Kit ledgers.

The project-wide readiness successor binds task-file versions through
[its explicit source-version record](../specs/sh-family-crosswalk/implementation-readiness-source-version.edn).
At the Platform checkpoint it conserves 121 of the original 139 task bodies,
declares eighteen changed Open bodies, and registers the resumed ten-task
Governance authority. Governance is not another compiler pass or SH milestone.
The sixteen Domain, thirteen Release and eleven Platform Product future records
remain outside the 149-key current SH task projection. The future-task view
below captures those forty records separately; it does not change SH incidence.
Historical
ownership bytes retain their original pin; live drift is reported as unassessed.
This successor retains the original SH/SH07 routes and scoped Reader receipts,
but its new input fingerprint requires its own independent source/routing
review. Generation or passing structural controls cannot inherit an old review
or establish current whole-criterion execution.

`tools/sh_inventory.clj --write` extracts the original byte/line/hash-bound
criteria; `tools/sh_crosswalk.clj --write` derives routing and current task
records. `--check` refuses a stale projection. The acceptance checker consumes
that crosswalk and adds `:spec-kit-task-self-hosting` to its existing report.
The original report keys, FL36/SH30 edges and read-only CLI remain compatible.
The report is the canonical dependency projection for SH and the dashboard;
neither consumer edits a copied graph or task status. Typed edges preserve the
consumer-to-prerequisite direction, source identity, evidence facet and explicit
condition. Unknown conditions retain their conservative edge and cannot grant
eligibility. An explicitly false condition disables only its specific edge.

SH03-SH29 retain the plan's complete shared completion rule as an exact hashed
source span and explicit acceptance conjunction: actual owning execution,
reject/preservation/composition evidence, target-tool checks where claimed,
applicable BOOT/TEST13 provenance/support/TCB and independent stable-revision
acceptance. These are evidence facets, not additional tasks. SH00-SH02 retain
the original baseline/process exception. Reuse of unchanged evidence remains
allowed; this view introduces no unconditional development replay.

Selected acceptance facets refer to existing task IDs without requiring their
entire wider language or target scope to finish. A current SH facet remains
unassessed without exact applicable execution and independent acceptance. The
historical accepted SH-00 through SH-06 receipts stay retained at their original
commits and scopes. They are not silently relabeled as current closure, and an
open broad component task does not erase that historical completion. The actual
Reader fresh-Syntax task acceptance remains visible in its literal task record;
it does not close the full Reader or SH-03 population.

### Conditional and conflicting source boundaries

- Lower's actual MIR prerequisite survives. Opt is required only when the
  selected invocation requests optimization. The SH-16 to SH-17 edge remains
  a milestone completion dependency; it is not a universal development gate.
- Auth artifact identity work can begin against its explicit source/policy
  contract. Capability-grant composition additionally needs type/effect/profile
  facts. No effect record grants authority and no runtime repairs rejected code.
- D1's pipeline lists Core Gravity AST before Name Resolution. C1's stage data
  lists `resolve-names` before `lower-to-core`; C5 resolves syntax after expansion
  and before type/effect checks; C6 explicitly consumes expanded, resolved syntax
  and C5 binding artifacts. The operational C6 composition edge therefore runs
  Resolve to semantic Core lowering. D1's earlier Core representation boundary
  is retained as a declared interface, not inverted into a completed-Core
  prerequisite for Resolve. Whether both documents intend the identical AST
  construction event remains an explicit contract-ordering qualification for
  the architecture owner; this projection neither rewrites D1 nor asserts an
  invented fused pass. The exact four source files remain pinned in the crosswalk.
- SH07-C01 derives its population now. It has no SH-23/24/25 build-completion
  prerequisite. C12's C11 edge applies before final candidate selection; C16
  applicability runs alongside development; C17 consumes the actual C12-C16
  evidence; C18 requires C01-C17 for the claimed scope. Existing C12 platform
  restrictions remain with their current owner. Advisory eligibility grants no
  execution permission and never transfers a restricted operation.
- The selected native release remains Linux LLVM, independently verified and
  executed. Darwin harness support does not substitute for that release target.
  SH-26 invokes the actual prior compiler on the complete next-stage closure;
  SH-27 compares real clean rebuilds; SH-28 closes trust/TCB/provenance; SH-29
  delivers and verifies the public seedless binary. Record validators and boolean
  flags cannot satisfy those execution gates. Wider hardware/firmware/OS/device
  and deployment obligations retain their separate existing component outcomes.

### Frontier and validation limits

The frontier distinguishes declared-interface development, actual selected
producer/consumer composition and current acceptance. SH07-C01's source-bound
population reconciliation has no implementation prerequisite and is eligible
for that declared work. A task's explicit interface permits local development;
it is not evidence that its implementation executes. Current SH criteria and
SH-29 completion remain unassessed or blocked by unaccepted prerequisites.
Missing selected closure, condition values, source evidence and owner decisions
remain explicit; a zero-cycle result cannot resolve them.

The controls conserve every SH completion edge, all 18 sequence identities and
literal marked task IDs; reject unknown/duplicate/missing nodes, stale byte
spans, forged task checkmarks and malformed ranges; and demonstrate that an
unknown optional edge cannot produce eligibility. Full source/routing review
passed at `c6c80530`, with its receipt and input fingerprint in the crosswalk.
Current implementation and criterion acceptance remain unassessed. Changed
source/routes cannot inherit that review; regeneration checks stamped bytes
against their actual local Git commit before writing. The dashboard collector may
consume this projection only after accepted integration. It remains the sole
writer of its store and Site.

Unassessed source-inspection locators retain the exact inspected Git commit and
hash. The report's `:evidence-source-applicability` separately records whether
the live file matches, changed or is absent; none of those observations grants
current execution acceptance. Ordinary later compiler/test edits therefore do
not erase a valid historical inspection or masquerade as its original bytes.
Current task authorities, acceptance contracts and claims marked current remain
strict. Regeneration still verifies every stamped snapshot against its actual
Git object, including all explicit applying pins, before either output write.
