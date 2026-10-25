# SH-07 Completion Tasks

Status: finite implementation and acceptance checklist; SH-07 remains partial.
Current accounting snapshot: 2026-10-24, inspected remote main
`c4b1d19da11def44f835d552e44d06488d31fe46`, tree
`a4145adf64c5c7d4ebc558c605f07d27f784cac5`. All 18 C-tasks remain OPEN.
Earlier `64aa890a` handoff observations below keep their historical scope.
The C01 seed-plan supplement below is based on `0067e9418de992900a95cd47d016029af62ff99c`;
it does not refresh unrelated queue or compiler evidence snapshots.
Its primitive-case supplement is based on `80881ba6fdc9548d181bf358fa0d033ae4298a22`.
Its instruction-position supplement records execution on local composition
`e4dd1a1505618ec032634e54a5d9a119b21070ff`, based on main `d42f23b2` plus
the separately frozen case and process-removal changes.
This updates management of the existing work, not compiler support or admission.

Historical assessment date: 2026-10-21.
Historical planning baseline: main `192dafcc86867ec6d067ee31d6b46fea4d1b0b92`,
tree `d4a01026dfe10d147c01e9f4bcd6bef8443c7165`.

Retained desk reconciliation: 2026-10-22, committed main
`2a94fb7156f9e85ad085bc88f4d6f0b05b973e4c`, tree
`cd2380533645332e1b9473eab4e064bf686c2c20`. The historical planning observations
below retain their original binding. See the
[C01 working support map](#c01-working-support-map) and
[current input/evidence reconciliation](self-hosting-current-migration-cohort.md#c01c16-reconciliation-at-2a94fb71).
No task is closed by this documentation update.

## Purpose and authority

This document decomposes the existing six
[SH-07 requirements](self-hosting-milestone-completion-plan.md#sh-07---core-form-semantics-and-lowering)
into exactly 18 completion tasks, `SH07-C01` through `SH07-C18`. It is the
task-level expansion of that plan, not another language contract or evidence
system. The [backlog](self-hosting-slice-backlog.md) retains milestone status;
the [current cohort assessment](self-hosting-current-migration-cohort.md)
retains its proposed-module scope and historical evidence classifications.

The deliverable is verified canonical Core for a declared compiler subset,
constructed by Gravity's owning lowerer from authentic resolved input, with
accepted/rejected behavior, preserved facts, and independently accepted
stable-module migration evidence. Lowering constructs and verifies control
semantics; executing compiled application control through MIR is SH-13 work.

The task count is fixed. The exact compiler-wide semantic population is still
an open input to C01, because the actual complete compiler build recipe is not
yet implemented. This plan does not invent that recipe or equate a directory
inventory with its transitive closure. A bounded frontend module migration can
finish before the complete compiler exists, under BOOT5's explicit support
set; it must not be reported as complete compiler coverage.

## Governing contracts and dependencies

D1/D2/D3 define architecture, ordering and terminology; D6/D8/D9 define
performance, safety and evidence. L2 defines the 13 core forms; L7 and L9
define pattern and error semantics. C3/C5 supply source, origin and binding
facts; C6 owns lowering, verification and diagnostics. BOOT3/BOOT5/BOOT7/BOOT8
and TEST13 govern migration and stage comparison. Resolve IDs through the
[document index](document-sequence.md).

The only milestone dependency is SH-06. Current-cohort reconciliation of
SH-05.1, SH-05.6 and SH-06.6 is still open, and the current resolver remains
development-unadmitted in the existing
[evidence audit](self-hosting-sh00-sh06-evidence-audit.md). C16 carries this
dependency; it does not prevent ordinary development in C02-C15.

The primary semantic owner is
`bootstrap/gravity/src/gravity/checked_core.gravity`. Transport defects belong
to its existing Clojure adapter, particularly `00887`/`00890`; hosted
`gravity.c6-core-lowering` is a separate constructor. Existing ownership and
workstream reservations continue to apply. This checklist assigns task
responsibility, not permission to take another writer's files or resume a
held candidate.

## Three-layer ownership and finite queues

The layers are ROOT coordination, persistent standalone slice managers, and
bounded disjoint subagents within an allocated manager queue. ROOT alone
allocates cross-slice/shared files, routes independent acceptance, reconciles
current remote main and integrates every main PR. Managers own isolated local
work, focused checks and exact frozen commit/tree handoffs. Subagent research
or implementation is not independent acceptance of its manager's candidate.
Managers send cross-slice requests and results to ROOT, not directly to peers;
they do not create competing schedulers or standalone worker threads.

Matt authorizes routine in-scope best judgment on implementation details,
local commits, proportionate tests, dependency/conflict fixes and queue
priorities without discretionary escalation to Matt. Managers independently
discover, prioritize and execute work inside their standing ownership.
ROOT coordinates actual conflicting ownership, incompatible contracts and
main integration, and routes independent acceptance. No fixed queue/task-count
cap, one-attempt grant, method-preparation-only stage, stop-after-ticket rule
or repeated discretionary pre-run approval applies to ordinary development.
These process restrictions are removed, not renamed or replaced. Historical
permission-only/proposal-only/await-human labels create no current ordinary
work hold. Preserve failed/interrupted/skipped evidence without converting it
to success or treating a never-launched attempt as a consumed proof.
Project cost/work budgets, debit/reservation accounting, cost proofs and
budget-exhaustion requirements are removed, including their operative contract
and test assertions. Do not rename them as generic bounds or retain
capacity-oracle node/depth/width/scalar/source-byte/token/form quotas that only
refuse otherwise well-formed input. Preserve actual semantic, authentication
and tamper requirements independently, plus host and platform safeguards and
independent correctness acceptance before integration. Platform/tool
restrictions, mandatory action-time approvals, destructive/security-sensitive
actions, new external commitments and unrelated scope retain their limits;
never approve an unknown pending action or bypass a restriction. Adoption
acknowledgment itself must not pause useful authorized work.

These accountable managers supersede the generic role labels in the retained
task descriptions below. This is task accountability, not permission to write
any implementation file:

| Key | Accountable manager | SH07 responsibility |
| --- | --- | --- |
| ROOT | Coordinator `019f4993-78b9-7573-b154-139d87a127b5` | Cross-slice allocation, C17 admission/execution scheduling, C18 decision and all integration. |
| Core | Scope/Core closure manager `01a16a54-eeb1-7941-ac35-7be30e47bdc7` | C01, C02-C11, C13 public ingress and C15 subset-negative closure. Allocated documents remain this plan, the selected-owner EDN map and backlog orchestration/dependency/progress sections. Contract-settled Core production/tests/fixtures follow ROOT's exclusive standing scope; foreign/shared interfaces stay outside it. |
| Construction | `01a138f7-bfa4-7b90-8a22-5b3c417b2e11` | C12 retained construction/source-origin reconciliation and the exact disjoint stock typed-lowering options bridge allocated by ROOT. |
| SOURCE | `01a13b8d-509d-7771-b618-06a24933391b` | C14 affected C7 composition; existing C7/effects/emitter/tests and L5/STD3 reservations remain intact. Frozen candidate, diagnostic-method and compact-design work have distinct outcomes below. |
| Cohort | `01a02f9c-2e5a-7d70-ba6d-d3519701b13f` | C16 current-cohort evidence/invalidation matrix. |

Managers curate concrete work without a fixed queue-size or task-count cap.
An item names the actual
gap, owning contracts, exact allocated paths/tests/fixtures, input/output,
prerequisites, accepted/rejected checks, completion and stop conditions. Use
`executing`, `resource-queued`, `dependency-blocked`, `awaiting-review`,
`ready-for-integration` and `queue-empty`; these are work states, not C-task
completion or migration credit. Freeze candidates during independent review.
Eligible disjoint work within the standing family can proceed meanwhile.
Managers derive and notify ROOT of their in-scope successors; new cross-slice
ownership still routes through ROOT.
Do not invent a compiler feature to demonstrate management migration.

ROOT's standing finite-goal charters apply to all eight implementation
managers. Managers
independently audit the repository, contracts, evidence and tests to discover,
prioritize and execute dependency-ready work inside their exclusive families.
Send ROOT the gap, contract, exact paths/interfaces, checks, dependencies and
stop criteria before writing as ownership notification, not a task-grant wait.
Pull the next eligible item in the same active turn, including during review
of an isolated immutable candidate. One blocked task does not block the whole
goal. Before yielding, report COMPLETED plus NEXT_EXECUTING; a drained leaf
queue does not justify default NEED_REFILL. Truly exhausted alternatives need
an evidence-backed goal-wide frontier routed through ROOT to the existing
reviewer, with the exact owner, required action and wake event. Do not invent
work or poll. ROOT retains shared allocation, independent acceptance and
integration. Completion wakeups and the hourly all-eight fallback retain
their existing cadence and preferences.

The queue snapshot below reconciles allocated work at `c4b1d19d`; it is not
proof of sustained throughput or a compiler completion score. All ten
ordinary-autonomy recipients have adopted the instruction. Advisory adoption
is complete; its later platform flag has no exposed operation details.
Older queue/process and accounting observations retain historical scope; they
cannot revive removed quotas, work allowances, fees, cost proofs, budget-only
refusals, attempt grants or ordinary method/approval staging. Historical
metered outputs do not establish the new unmetered implementation's correctness.

| Manager | Finite current queue / state | Dependency event or stop |
| --- | --- | --- |
| Core | PR304 integrated the selected-owner plan/use map. The next C01 candidate joins existing runtime cases and newly confirmed missing source-arity cases; the separate two-slot helper freeze remains with ROOT for review/integration. | Preserve isolated candidates and self-pull confirmed in-goal gaps. The new case joins do not close source value domains, reachability or Core products; no administrative-only PR/count credit. |
| Construction | Frozen `1af5fd81`/`39ad9017`, `awaiting-integration`, with independent CODE and EVIDENCE acceptance: two existing `bootstrap-p15/00581` adapters plus new `sh01_typed_lowering_work_policy_test.clj`, based on `c4b1d19d`. | This snapshot records the historical metered adapter, not a requirement to preserve its policy API or ceilings. Remove accounting through the actual permitted owner while retaining fresh semantic verification and original source/report joins; the current C12 eligibility restriction cannot be bypassed. |
| Cohort | Six-owner invalidation matrix returned after two disjoint read-only inspections; `queue-empty`. | The planning deliverable is complete; C16 remains OPEN. Refill only for an actual selected cohort/evidence gap, not a new stamp. |
| SOURCE | Frozen `f3073c24` has independently accepted focused code/evidence but failed whole-default composition. The allocated producer-only diagnostic returned accepted at N=1048576; compact architecture finalization continues. | Preserve the failed whole run and producer-only boundary. The historical unrun Effects-method proposal is not a current preparation-only or approval hold. Ordinary owned method implementation/checks proceed under actual machine safeguards; transport availability is not compact implementation/fit evidence. |
| MIR producer `01a16a55-1b44-7551-b595-6caceceef034` | Inferred-Integer producer edge, `dependency-blocked`. | Actual reader `(- codepoint 48)` yields Integer; the existing I64 bridge does not establish its mode/range/source facts. Needs SOURCE's real boundary, not another arithmetic demonstration. |
| Diagnostics `01a16a55-1b07-7202-b7ce-fd71fc0103c2` | Nested generated-origin edge, `dependency-blocked`. | Needs an explicit representation at the true producer edge; no current client is invented. |
| Optimization `01a16a55-1b07-7202-b7ce-fda9f821c3ed` | `queue-empty`; no eligible implementation item. | No demonstrated defect in a new supported actual case. ROOT may refill only from a real required producer/consumer gap. |
| Standard library `01a16a55-1b5b-7250-b702-0168997dbbb3` | `queue-empty`; no eligible implementation item. | Bare-Map compiler-client binding needs Core/SOURCE shape; no new library leaf gap is demonstrated. |
| ROOT | C17 and C18 decisions are `dependency-blocked` on their required evidence. | Route independent review and any separately authorized execution when the exact stable candidate and dependencies are ready. |

The later MIR, optimization and diagnostics managers own compiler C11, C13 and
C15 work respectively; those document IDs are not SH07-C11, SH07-C13 or
SH07-C15 task assignments. All existing source/test/interface reservations and
historical attempt outcomes survive this plan. Managers may use bounded disjoint
`gpt-6.1-sol` subagents at medium or higher effort within their allocations;
they gain neither extra host capacity nor self-acceptance authority.

Ordinary checks share two normal broker leases and one memory-heavy lease;
exclusive checks hold all slots. Use the existing trusted broker coordination
root and canonical heavy lock where applicable. Queue when capacity is
unavailable rather than launching another JVM. Actual heavy/native/bootstrap/
proof/performance resource safeguards and scope boundaries remain applicable;
historical grants are not ordinary-development attempt budgets. GIS/H6 claims
retain their concrete technical/provenance requirements. Compact representation,
semantic validation, identity/version fences and affected independent review
remain technical obligations, not a discretionary human-approval label.
Publication of a design discussion is not implementation/fit evidence.
Ordinary eligible work does not need a new
per-command ROOT approval loop or unchanged expensive proof replay.

### Advisory process review and dispositions

The existing standalone adversarial process reviewer
`01a17047-4e39-7013-9490-6feb82746d14` is advisory and independent. It is not a
writer, scheduler, integrator or global acceptance gate. The durable event path
is material process decision, stalled lane or integration outcome -> ROOT ->
advisory process review -> explicit ROOT disposition. ROOT records one of
accepted/assigned, rejected with evidence, deferred with a named dependency or
event, or bounded evidence requested in the existing coordination record.
Managers report events to ROOT; this does not authorize peer messaging.
The reviewer sends actionable findings only to ROOT and independently
investigates, recommends and tracks dispositions/outcomes. ROOT alone changes
manager priorities, instructions and assignments; the reviewer never directs
managers.

Review may challenge unnecessary dependencies or serial handoffs, ownership,
interfaces/goals, approval loops, stale proof/governance burden, queue
starvation, tool access, resource allocation and genuine technical blockers,
including tests, builds, compiler behavior, interfaces, integration and
toolchains. It preserves necessary correctness, security, proof and resource gates; it
grants no blanket bypass. At the frozen PR293 handoff, the reviewer charter and
event had been sent, with exposed state `waitingOnApproval` and no pending
operation details; adoption was pending and not a global gate. Subsequent to
PR293, ROOT observed the reviewer's explicit adoption acknowledgment and
ACTIVE state with no approval flags; the later idle/three-findings observation
also retains its original scope. The latest supported snapshot instead flags
`waitingOnApproval` without exposing a specific operation or approval request.
Do not infer a current unblocked state, approve an unknown action or bypass
the platform flag. Advisory review is not a global gate: disjoint ordinary
work continues and ROOT handles any actual exposed action at its boundary.
These are adoption/platform observations,
not completed investigation, independent acceptance of this candidate or a
sustained-throughput claim.
The saved heartbeat's existing schedule, target and notification preferences
remain unchanged. ROOT reconciles the
plan, operating memory, heartbeat and all manager acknowledgments before
claiming orchestration migration complete. Configuration alone, this plan,
the C12 reconciliation and the C16 matrix are not compiler progress.

### Merged increments and current composition frontier

These are scoped merged results, not a new inventory or fresh whole-cohort
run. ROOT retained independent reviews, exact-tree conservation and normal
integration gates. Counts belong to their actual executions; separate batches
are not a newly executed aggregate.

| Change | Exact merged revision | Retained actual checks and scope |
| --- | --- | --- |
| [PR294](https://github.com/burn2delete/uniform/pull/294), original loop helper | `223aeebdf496e748082b4a20bba5a5da34ba64dc` | 1 test/133 assertions: actual G current-reader/C5/Gravity Core target, binding, transfer, source/order joins and coherent target replay rejection. Q text parity only. This is the 435-byte extracted subject, not full checked-Core analysis or C7/runtime support. |
| [PR295](https://github.com/burn2delete/uniform/pull/295), lawful selector spelling | `70a9628c37582bd7658ffe28930bc45a83024716` | All 13 malformed private loop-selector spellings repaired in three sources, preserving schema fields/order. V2 current-reader G/Q and seed-leaf checks passed 3 tests/47 assertions; separate V3 boundary/LOC checks passed 11 tests/23 assertions. Reuse after disjoint PR294 was independently checked; no fresh clean-merge run is implied. |
| [PR296](https://github.com/burn2delete/uniform/pull/296), loop-target selection | `59da4107cb6a606e361f935e76c2269a055e513b` | Changed V2 batch passed 15 tests/102 assertions: lawful function-target skipping before loop-only slot lookup, selected occurrence/order and work/diagnostic conservation, plus boundary/LOC. Original-source seed-leaf scope; reader deliberately unselected. |
| [PR297](https://github.com/burn2delete/uniform/pull/297), if/do selection | `c4b1d19da11def44f835d552e44d06488d31fe46` | Changed V2 batch passed 17 tests/314 assertions: four registered if/do field-selector pairs, mixed/skipped occurrence order, charges and malformed-selection priority, plus boundary/LOC. Existing loop behavior and standalone handlers conserved; reader deliberately unselected. |

PR294's failed parent-EOF preflight, PR295's failed C2-pin attempt and
PR296/PR297's failed delimiter/EOF loads remain failed. Passing changed-content
executions used repaired worktree bytes subsequently frozen and independently
matched; they are not invented clean-frozen-revision JVM runs. None closes
full B51/H6, current migration, native support or a C-task.

SOURCE's unmerged candidate is `f3073c247303c9eec6023a82d574b6569001a179`,
tree `d188d97759563f01ce11592bfb94e4ac53c1b2fd`, based on `c4b1d19d`.
Its focused 13 tests/678 assertions have independent CODE and focused EVIDENCE
acceptance. The 317-byte bare-Set fixture's actual supervised whole-default
composition is nevertheless FAILED:
1 test, 0 passed assertions, 1 failure, 1 error; parent duration 126284 ms.
Current-reader/C3, SH05, SH06, SH07, request and C7 returned, with
`C7-GENERIC/:loop-type-work-limit` reports followed by Map-key/Set-conversion
work-limit reports and the deliberate prerequisite stop. Fresh C7 verification,
public C8, assembly and runtime were UNREACHED. No timeout/truncation occurred;
observed cleanup completed. Original artifacts remain at
`/private/tmp/source-dse-full-f3073c24-20261024a`. First printed refusal is not
established as first internal refusal; minimum sufficient N and global-versus-
local cause in that failed run remain unresolved. Focused acceptance does not make the whole result
passing or the candidate ready for integration.

[PR291](https://github.com/burn2delete/uniform/pull/291), merged as
`25cc478b7982c921614c0cdcf40f5f3933d12514`, already provides trusted
caller-selected cumulative C7 work policy. Its default 65536 is not a universal
language-semantic requirement. Options remain outside canonical schema-3
identity and candidate authority; local DSE `min(remaining,65536)` and
independent C8/lowering/runtime limits remain. The stock
`00581-p15-s23-stage2-typed-lowering-operation.clj` still invokes the legacy
fresh C7 verifier with two arguments and has no caller-options bridge.
That concrete interface gap does not establish fresh-verifier or downstream fit.
C12 owns the disjoint two-`00581`-file/new-test repair: old public arities
retain the legacy callee/two arguments; new explicit-options arities alone
use the policy-aware fresh callee/three arguments and unwrap only its
verification envelope. Keep the typed input's exact three keys, source/report
joins, Gravity malformed-option validation and all local/default ceilings;
no new schema, wrapper, cache or implicit allowance change.

C12's prewrite 228-byte minimal-Map-get producer/fresh-policy-verifier probe
passed naturally in 91.403380041 s under the normal 2GiB/120s bound. This is
not DSE fit; bridge units and actual fresh verification remain distinct evidence.
C12 is now frozen at `1af5fd81fc81b66b80e7fbd7ccd36f27dd2a2f74`, tree
`39ad9017f713b0242b6632b8bf50d9116606da2a`, awaiting integration after
independent CODE and EVIDENCE acceptance. Separate actual groups passed
1 test/19 assertions, 1 test/17 assertions and 15 tests/135 assertions:
17 tests/171 assertions across three runs, not a newly executed aggregate.
Original positive V1/V2 aggregates remain FAILED; the corrected current
positive selector is UNRUN. Independent unchanged-production/dependency and
passive-observer conservation supports reuse only of V2's actually completed
stock operations and 16 passing assertions in their original scope. It does
not infer a passing current positive aggregate or DSE fit.

SOURCE's actual producer-only diagnostic accepted the genuine 317-byte source
at existing trusted N=1048576, converging in five rounds within bound 34.
The ordinary inference pool charged 220380 and retained 828196; this is not
minimum N or full C7 total work. The largest observed local DSE charge was
31728, within unchanged 65536 on this trajectory; overlapping rows must not
be summed. All 22 observed calls produced rows, with zero drops and no observer
error. Parent/child exited zero in 119415 ms parent duration, cleanup completed
and the frozen candidate remained clean. Raw evidence is at
`/private/tmp/source-dse-producer-f3073c24-20261024b`; `probe.edn` SHA256 is
`6cb021a30b1a3f998cf838c483690696288e820f2b757327b982c188f7265697`.
This supplies no separate fresh C7 verifier, C8, assembly or runtime result.
The producer-only classification has independent acceptance; the earlier
whole-default failure remains FAILED. At that historical snapshot the next
Effects 180s local-fit proposal had not executed. Its old preparation-only/
no-GO process status is superseded: ordinary method implementation and checks
need no per-run approval, while actual resource bounds, correctness and
ownership still apply. This does not invent a later execution result.

The existing three-argument body-effects route already carries the original
request at `binding.digest-resolution.preimage.request`; the detached
two-argument route lacks it and is unsupported for compact GIS. No new context
API is needed for that selected route, but compact representation, validation,
fees, identity/version and independent consumer evidence remain unresolved.
No held GIS execution follows from transport availability.

## Candidate scope and change control

The first concrete candidate is the existing proposed `:meta/:jvm`
source-to-Core module endpoint. Its six executed owner sources are the
envelope, reader, syntax, macro, resolver and checked-Core sources in the
cohort assessment. Rule DATA in `p15_s23/compiler.gravity`, seed compilation,
generic plan execution, primitives, codecs/digests, source reads and artifact
binding are declared dependencies. The emitter is a selected freshness input;
it is not executed target lowering in this endpoint.

The actual selected source-analysis subjects at the orchestration snapshot
are the following complete files, relative to `bootstrap/gravity/`. This is
the existing six-subject selection, not a repeated whole-directory inventory
or a claim that every declared function executes:

| Subject | Existing executing owner / role |
| --- | --- |
| `src/gravity/compiler/authenticated_envelope.gravity` | Authenticated envelope construction and replay. |
| `src/gravity/bootstrap/reader.gravity` | Full source selected for analysis; executed SH03 subset remains separately selected. |
| `src/gravity/bootstrap/syntax.gravity` | Syntax/origin construction. |
| `src/gravity/macro.gravity` | The source-required `defn` expansion subset, separate from quoted rule DATA. |
| `src/gravity/resolution.gravity` | Current resolver; development-unadmitted. |
| `src/gravity/checked_core.gravity` | Core builder and required full source-analysis subject. |

The endpoint is canonical Core under `:meta/:jvm`, with later type/effect/
ownership/safety facts pending. `p15_s23/compiler.gravity` is interpreted rule
DATA; `p15_s23/emitter.gravity` is a freshness dependency here. The complete
loaded Clojure seed/adapters, actual selected function plans, primitive uses,
classpath/runtime/environment identities and imported callee products still
need their candidate-bound joins, as recorded in the cohort assessment.
Neither a module catalog nor six file paths supplies those joins.

Required now means a real selected executable occurrence, generated form,
primitive/import dependency or C6 preservation/rejection obligation. Later
means a feature not required by that declared endpoint, with an explicit
outside-subset reason and rejection where applicable; discovering a required
use removes that deferral. Complete typed compiler execution, MIR, native
providers, build/driver closure and general target expansion retain their
later owners. The separate staged release target stays
`:llvm-x86_64-linux`, `x86_64-unknown-linux-gnu`, ELF/SysV AMD64; no JVM or
Darwin check supplies its toolchain, provider or release evidence.

At the historical planning baseline, the first seven input rows of that
assessment still had identical bytes and SHA-256. The emitter had changed to 1,328,259
bytes with SHA-256
`1bb3568b48b26e994da2cc140ff5597812709786a1f9836ae7ff4af7044e63ed`.
These are desk observations, not renewed source/plan/proof bindings. C01 must
capture actual selected seed/dependency identities as well as owner bytes.

The Cohort manager's read-only reconciliation at `64aa890a`, forwarded by
ROOT on 2026-10-24, supplies a newer invalidation observation without replacing
the historical tables: five owner sources and compiler rule DATA match
`2a94fb71`; checked-Core changed from 537749 to 538443 bytes through
`44709ea9`'s duplicate-binding diagnostics/facts/preflight, and emitter
freshness changed from 1599698 to 1670330 bytes. The loaded
`bootstrap/clojure/src/` tree changed from `f4204197...` to
`5c9aac1b3a98e4ad1b1c2242a1769f73a2443352` (seven host files), despite
unchanged selected seed/adapter files and `deps.edn`. Test, fixture and
contract trees also changed. Resolver `00874:50-57` and Core `00885:118-128`
bind compiler/emitter freshness even when their bodies are not executed.
These observations require affected-input reasoning, not automatic total
invalidation or automatic reuse of every old result.

The five separate historical hosted-Stage0 subject checks remain historical;
they do not execute the Gravity Core builder. The old checked-Core exit-143
attempt remains INCOMPLETE with unresolved cause. PR266's two-helper,
at-most-2-KiB current-reader composition is useful bounded C3 -> C5 -> Gravity
Core evidence, not full-subject acceptance. The default reader still has its
historical `00808` pin; the explicit current-source route is a separate
development path. No old failed V1/V2 attempt, interrupted run or skipped
selector is overwritten by the reconciliation. SH-05.1, SH-05.6 and SH-06.6
remain partial OPEN, and the resolver remains development-unadmitted.

Construction's historical `64aa890a` C12 reconciliation found all 13 malformed
`:target-kind=:loop` spellings in `sh07_b51/producer_c.gravity`, `root6_a.gravity`
and `root6_b.gravity`. PR295 subsequently integrated their lawful spelling
repair; PR296/PR297 separately repaired selector-before-field traversal.
Those defects no longer remain in current main; that does not update or admit
held H6 inputs/pins. The retained 1670330-byte emitter observation exceeds
the held H6 1048576-byte source-entry cap. That is a static fit mismatch, not
an observed execution result or permission to raise a bound or resume H6.
The integrated source correction does not establish resource fit. The old
checked-Core attempt's named raw `/tmp` log is absent locally; its committed
audit records progress only through the hosted-C6 call, not a returned result.
These are exact C12 stop conditions for those retained claims. They neither
make H6/B51 a prerequisite for the ordinary endpoint nor close the ordinary
full-source/Gravity-construction evidence gap. The retained 36.2-minute failed
attempt stays failed and is distinct from the `b999` interruption. Construction
returned those read-only items and now owns the separate options bridge above;
C12 remains OPEN. Private 12/22 construction, retained proof/classpath inputs,
independent admission-load binding, full comparisons and actual producer/hostile evidence
remain incomplete. Existing load/test timing does not separate construction
from verification. The next transaction needs C01/C11-bound current Gravity
source analysis/replay, authentic producer/consumer/rejection inputs and exact
resource selection, without an unchanged retry, redesign or C17 grant.

SOURCE's returned C14 read-only input locates current required typing
frontiers, not observed whole-helper diagnostics. `sh07-exact-keys?` has two
unannotated parameters; `map?` supplies no refinement. `keys` -> homogeneous
Keyword List -> `set` yields bare Set, but the current equality subset requires
one bare Set and an independently nonempty literal Keyword anchor (1..1024).
A variable expected Set, including empty, does not supply that anchor. The
existing accepted private fixture annotates Map and hardcodes the anchor, so
it cannot establish either unannotated Map/key-domain inference or the full
original helper. A variable-expected-Set equality contract is a prospective
handoff, not a way to close the earlier Map premise or an implementation grant.

For `sh07-vector-contains?`, the actual guard is `=(count remaining)0` before
`first`/`rest`. Finite Vector rest produces List, loop/recur compatibility is
exact, and the homogeneous-List head proof recognizes same-binding `empty?`,
not this count-zero guard. Callers include binding IDs, alias names and `:-`;
no general widening or Keyword-only narrowing follows. C01/C14 must retain
those actual uses and producers before allocating a remedy. SOURCE's input
awaits ROOT review and does not assert whole-helper acceptance or a freshly
executed rejection. It does not supply a missing C6 lowering feature.

The first candidate is a useful module endpoint, not the entire eventual
compiler closure. For milestone-wide credit, C01 must state the supported
compiler subset and why it meets the backlog exit gate. If the evidence only
supports the six-source module endpoint, C18 records that bounded migration
and leaves SH-07 partial. A missing required form cannot be made optional to
obtain completion. SH-25 later checks that the actual compiler consumes the
accepted revisions or accepted successors.

Once C01 is recorded, fixes stay within these 18 tasks. A newly discovered
required form, primitive or import is assigned to its existing owning task;
optional features go to the full-language gap map. Changing the supported
population requires an explicit scope amendment naming the source/use, owning
contract, affected task, acceptance case and invalidated evidence. It does
not silently create C19 or redefine SH-07 as all of Gravity.

## Evidence status at the planning baseline

This is a desk reconciliation, not a fresh semantic test pass. "Located"
means implementation and checks exist; it does not mean the whole task is
complete. No completion checkboxes are checked by this documentation change.

| Population or obligation | Located support | Remaining closure |
| --- | --- | --- |
| All 13 L2 form names | `sh07-core-contract` lists `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`, `var`, `set!`, `try`, `throw`, `match`. | Required variants and composition must pass; listing a form is insufficient. C02-C11. |
| Values, control, bindings, vars, mutation, errors | B1-B9 tests and the current Gravity builder/verifiers. | Reconcile exact compiler uses, unsupported variants, real source composition and rejection coverage. C02-C09. |
| Calls and recursion | B47 function/call/recursion products; separate B48 direct local fixed-arity legality. | General callable/closure/cross-module and dispatch requirements need explicit disposition. B48 has pending nonlocal calls and is not complete call semantics. C05-C07. |
| Patterns | Scalar/quoted-symbol, irrefutable, recursive fixed-vector, keyword-map and root-map guard checks. | Required constructor/rest/nested-guard variants and duplicate binding rejection must be classified; later type/effect/ownership facts remain pending. C10. |
| Canonical products | Existing template/resolved/map verifiers, source mapping, origin, fragment and mutation checks. | Complete task-to-check matrix, declared pending boundaries and affected authentic joins. C11-C15. |
| Source analysis of current checked-Core | Existing audit records an interrupted exit-143 run reaching `:hosted-c6-before`. | INCOMPLETE; original cause unresolved. Current Gravity source-analysis/replay/provenance needs its own finite decision, not a mandatory legacy hosted-C6 redesign. C12. |
| Current upstream migration | Retained historical SH03-SH06 credit and later bounded development results. | Current SH05/SH06 groups and resolver admission remain open. C16. |
| Full compiler graph | Cohort assessment classifies current sources, targets, held work and required broader roles. | Actual executable recipe and semantic closure are not established by the 69+5 inventory. C01; full build ownership remains SH23-SH25. |

The lowerer's header still names some families as pending even where later
direct checks exist, including qualified references/mutation and map guards.
C11 reconciles these declarations from actual behavior. Neither the header
nor an old narrative is an independent completion oracle.

## Ordered task list

All 18 tasks remain OPEN in this checklist. Existing accepted evidence should
close matching obligations without unnecessary reruns. "Reconcile" means
inspect the original implementation, cases, raw result and applicability;
implement or test only the demonstrated gap. It does not mean rewrite a
working form.

| Task | Requirement | Responsibility | Dependencies | Concrete output and exit gate |
| --- | --- | --- | --- | --- |
| SH07-C01 | All six | Coordinator with sh-core | none | Record the exact candidate source/compiler/dependency population, supported profile/target and required variant matrix. Separate the bounded six-owner migration from milestone-wide coverage; name each transitive primitive/import/generated-form dependency and residual seed boundary. Every required use has a C02-C15 owner and accepted/rejected case. No "current compiler" or directory count stands in for this selection. |
| SH07-C02 | SH-07.1 | sh-core | C01 | Close literals, vector/map/set constructors and quoted DATA: preserve literal kind/value, source occurrences and constructor evaluation order; quote never executes contents. Cover nil/Bool/integer/decimal/ratio/character/string/keyword, quoted symbols and nested data as selected by C01. Reject malformed quote, unsupported executable form and malformed constructor, with source diagnostic. |
| SH07-C03 | SH-07.1 | sh-core, sh-resolution for producer defects | C01 | Close lexical/top-level and qualified expression references, `def` and `var`: exact binding/namespace identity, shadowing, definition/value distinction and source map. Select alias/cross-module uses from C01, including real callee artifacts where required. Reject unresolved, private, invalid or substituted binding/var targets at the owning stage. |
| SH07-C04 | SH-07.2 | sh-core | C01 | Close `if`/`do`/`let`: false/nil truthiness, one conditional branch, ordered effects/children, sequential initializer scope, nested shadowing and legal body/arity policy. Destructuring required by C01 must lower with preserved origins or be implemented here; reject malformed bindings and reorder/duplicate/drop mutations. |
| SH07-C05 | SH-07.3 | sh-core | C03, C04 | Close `fn`, function values, captures and callable operators used by C01. Record parameter/body/capture bindings, latent body order and operator-before-arguments-once order. Cover lexical shadowing and nested functions. Reject invalid operator/function/capture structure; preserve later capture ownership and call-type obligations explicitly. |
| SH07-C06 | SH-07.3 | sh-core | C05 | Close arity and call graph: local/nonlocal direct calls, indirect/higher-order calls, cross-module edges and recursive components required by C01. Specify fixed/variadic/multi-arity dispatch separately; required dispatch cannot remain silently pending. Reject wrong arity and malformed/mismatched call evidence. Reconcile B48's separate result with the actual selected consumer rather than treating its source proof as integrated legality. |
| SH07-C07 | SH-07.3 | sh-core | C04, C05 | Close `loop`/`recur`: nearest compatible target, function boundary, tail placement, arity and simultaneous ordered next-binding values; retain binding IDs and recursion edges. Reject absent/wrong targets, nontail transfer, mismatched argument count and forged target/order. Carry type compatibility to SH08. |
| SH07-C08 | SH-07.4 | sh-core | C03, C04 | Close explicit `set!` location/RHS/result representation for C01's locations, including selected qualified vars. Preserve once-only RHS and nested/conditional write order, mutable target identity and declared obligations. Reject immutable/invalid target shape and substituted location facts at their owners; leave type/effect/capability/ownership/safety legality unasserted until checked. |
| SH07-C09 | SH-07.4 | sh-core | C03, C04, C07 | Close `throw`/`try`: error value once, protected expression order, typed handler scope/source order, normal/error exits and nested transfers. Resolve protected/handler sequencing and `finally`/cleanup only where required by C01; otherwise retain explicit unsupported rejection. Reject malformed handlers, invalid placement and lost/forged transfer/origin. Runtime selection and stack unwinding remain downstream. |
| SH07-C10 | SH-07.5 | sh-core | C03, C04, C07, C09 | Close selected scalar/quoted-symbol, wildcard/binding/default, recursive vector/map, record/union/constructor and guard patterns. Every named family receives required support or an explicit outside-subset rejection decision in C01; required cases cannot be excluded. Verify scrutinee once, branch-local unique bindings, clause/guard/body order and structural-failure skipping. Reject duplicate bindings and malformed constructor/guard. Preserve nonreturning guard transfer; SH08 establishes normally returning guard Bool and applicable match exhaustiveness/totality. |
| SH07-C11 | SH-07.6 | sh-core | C02-C10 | Reconcile the canonical Core schema and truthful supported/pending declarations. For every required source occurrence, verify L2/primitive/domain disposition, exact child/binding links, evaluation records, origin/source maps, desugaring traces and retained metadata/declarations. Reject residual unowned surface forms and forged/dropped facts. All six requirement groups map to exact selected checks. |
| SH07-C12 | SH-07.6 | Coordinator with sh-core/sh-resolution | C01; C11 before final selection | Close full-source analysis and actual Gravity-lowerer execution as distinct claims. Select the owning authenticated Gravity-Core route and exact subject/resource fit; legacy hosted-C6 representation redesign is not a prerequisite. A hosted-C6 result cannot substitute for Gravity builder execution, and a normalized kernel cannot substitute for source analysis. Required subjects and unsupported bounds stay explicit. |
| SH07-C13 | SH-07.6 and uniform gate | Coordinator with sh-core | C11 | Verify existing source/file/public `sh07-core` entrypoints on accepted and rejected paired sources: deterministic identity, actual-path provenance, extension/UTF-8/read policy and no target reread. Record which spelling actually traversed each stage; parser parity alone is not paired composition evidence. Use existing routes; complete public compiler repointing is a later driver obligation. |
| SH07-C14 | SH-07.6 | Coordinator with affected consumers | C11 | Pass actual SH06 -> Gravity Core -> C7 boundary checks on selected products; handwritten/macro-generated equivalents preserve behavior, source/origin/binding/order and explicit pending facts. Exercise affected C8/emitter/runtime consumers only for changed interfaces or concrete regressions. Do not require all later milestone implementations to finish. |
| SH07-C15 | All six and uniform gate | sh-core with boundary owners | C11-C14 | Complete diagnostics and negative composition matrix: C6 lowering/shape/order/origin/effect-drop/unsafe-drop/domain/verify families as applicable, plus upstream-owned rejection. Stale/substituted/coherently forged inputs and dropped/reordered graph products fail closed; semantically valid graph sizes are accepted without capacity-only graph quotas. Preserve available source/generated provenance and prevent raw host exceptions. |
| SH07-C16 | SH-06 prerequisite | Coordinator with sh-macro/sh-resolution | C01; semantic work can proceed independently | Reconcile current SH05.1/SH05.6/SH06.6 and resolver support for the exact selected candidate. Retain historical credit; obtain affected source/expansion/resolution/comparison and independent acceptance where missing or invalidated. This task closes the upstream current-cohort dependency, not every optional macro or namespace feature. |
| SH07-C17 | Migration and uniform gate | Coordinator and independent reviewer | C12-C16 | Select the stable revision and produce the applicable fresh module conformance, provenance, actual compiler identities, stage/IR/diagnostic comparisons, reviewed deltas, BOOT5 support/TCB records and independent acceptance. Reuse valid unchanged expensive evidence; refresh affected evidence. Required skipped/failed/interrupted results block their claims. No new authority wrapper or receipt schema is needed. |
| SH07-C18 | Completion decision | Coordinator | C17; C01-C16 closed for claimed scope | Review the complete six-requirement matrix and independently accepted clean revision. Update existing status/evidence records with exact support and residuals. Mark SH-07 complete only if its full declared compiler-subset exit gate and applicable uniform gates pass. Otherwise record bounded module completion and the exact still-open task(s); seed retirement and later-stage completion remain separate. |

### Current task accountability, unknowns and stopping criteria

The exit gates above retain their accepted/rejected behavior. This current
overlay assigns each task to its manager and identifies what still prevents
closure. Every row is OPEN; none is closed by a source census, test count,
inventory, manager acknowledgment or completed documentation queue item.
The existing validation table below supplies exact test-owner paths and
selectors; its locators are not current passing results.

| Task / status | Manager | Current code/evidence and next concrete closure obligation | Stop or rejection criterion |
| --- | --- | --- | --- |
| C01 OPEN | Core | The EDN positions all 61087 selected seed instructions, including 12668 builtin calls, across six sources; 44 reader functions remain outside the selected plan. Finite normal runtime cases cover every observed builtin arity. Source value-domain, binding/definition, actual invocation, generated/Core-product and migration joins remain open. | Structural body/plan paths are not C2 identities or Core authority. Matching an arity does not prove an actual source argument's domain or execution of every retained function; C01 remains open. |
| C02 OPEN | Core | Core literal/quote/collection lowerers and carrier/decimal/character/order tests exist. Join each selected subject's required literal and constructor variants to those real products/results. | Accept preserved kinds, inert quote and ordered constructors; reject malformed quote/constructor, unsupported executable payload and forged origin. Missing joins remain OPEN. |
| C03 OPEN | Core; Cohort for C5 producer facts | Reference/def/var and qualified-name tests exist. Select actual executable local/namespace/import/var occurrences and their real resolver/callee artifacts. | Accept exact binding/source identity and shadowing; reject unresolved/private/substituted targets at the owning ingress. Do not fabricate a callee or host lookup. |
| C04 OPEN | Core | `if`/`do`/`let` owner tests cover exclusive branches and sequential scope. Match selected empty/terminal/nested/transfer variants to actual Core and source order. | Accept false/nil truthiness and once-only ordered children; reject malformed arity/bindings and reorder/drop/duplicate mutations. No unselected destructuring feature is added. |
| C05 OPEN | Core | B47 function/parameter/capture products exist. Establish which selected uses require callable values, lexical captures or nested functions, preserving original signature/parameter/body coordinates. | Accept required construction/capture/order facts; reject invalid callable or capture links. Later capture ownership is pending, not inferred here. |
| C06 OPEN | Core | B47 two-I64 current-source composition is bounded; B48 is separate legality. Select actual local/nonlocal/indirect/recursive/dispatch uses and the consumer of each product. | Accept required arity/edge coverage; reject wrong arity or forged/missing edges. Stop rather than claiming B48 public routing, Q composition or general dispatch from a narrower result. |
| C07 OPEN | Core | PR294 supplies the extracted `sh07-vector-contains?` G source-to-target/transfer/order/source join, 1/133, with Q text parity only. Other required occurrences and full-subject candidate-bound joins remain missing. | Accept nearest target, simultaneous operands, tailness and original coverage; reject wrong target, arity, nontail or reordered transfer. Do not promote this bounded fixture to full-source closure or add a replacement loop feature. |
| C08 OPEN | Core | Qualified mutation and RHS/target tests exist. First establish which selected executable locations require `set!`; quoted dispatch DATA alone is not such a use. | Accept original mutable location and once-only RHS/result; reject immutable/forged locations or dropped order. Type, authority and safety legality remain later obligations. |
| C09 OPEN | Core | Throw/try/multiple-handler tests exist. Select actual protected/handler/transfer variants and cleanup requirements from the selected population. | Accept required ordered exits and handler scope; reject malformed handler, placement or lost transfer/origin. A required cleanup gap cannot be called optional; no held exception design resumes. |
| C10 OPEN | Core | Scalar/quoted-symbol, Vector, Keyword-Map and root-map-guard products/tests exist. Required constructor/rest/nested-guard population still needs C01 disposition. | Accept scrutinee once, branch binding uniqueness and structural-failure skipping; reject duplicate/malformed/forged guard/constructor evidence. Bool/totality/effects remain later owners. |
| C11 OPEN | Core | Canonical node/source maps, fragments and verifiers exist; header pending declarations are not a current support oracle. Reconcile all selected C02-C10 rows and retained declarations against actual products. | Accept complete L2/primitive/domain coverage and maps; reject residual unowned surface forms, malformed child/binding/source links and dropped metadata. No false checked downstream facts. |
| C12 OPEN | Construction | PR295-297 integrated scoped spelling/selector repairs; full-source analysis and actual authenticated Gravity construction remain separate open claims. The independently accepted stock work-policy bridge is frozen at `1af5fd81`, awaiting integration; its minimal-Map-get prewrite probe is not DSE fit. | Retain fresh semantic verification and original source/report joins while removing the work-policy API and local/default work ceilings. Preserve actual C12 eligibility and denied-operation restrictions; unrelated permitted work continues. Full reconstruction still requires real accepted/rejected evidence. |
| C13 OPEN | Core | Existing source/file/public `sh07-core` routes and routing tests are locators. Select accepted/rejected co-canonical inputs with deterministic identity, actual-path provenance and extension/read policy. | Accept the exact spellings actually traversed; reject wrong extension/UTF-8, rereads, stale paths or forged identity. Byte/parser parity alone is not paired downstream composition. |
| C14 OPEN | SOURCE | Unmerged `f3073c24` has independently accepted focused 13/678 code/evidence, but its 317-byte fixture whole-default composition failed C7 work limits; fresh verification/C8/assembly/runtime were unreached. The producer-only diagnostic accepted at existing N=1048576 with observed local DSE charges within unchanged limits. | Preserve the failed historical whole result. Remove work-limit refusals and verify fresh unmetered semantic composition; historical producer-only acceptance does not establish downstream correctness. Original helper Map/key-domain and loop/head frontiers remain separate. |
| C15 OPEN | Core; SOURCE frontier only if allocated by ROOT | Core diagnostics, conformance and graph mutation owners exist. Join each required subset-negative family to its real ingress, stable ID/span/profile/target and retained result, including affected producer failures. | Reject shape/order/origin/effect-drop/unsafe-drop/domain/verify forgeries; accept semantically valid graphs without capacity-only graph quotas. Missing cases or raw host exceptions prevent closure; no diagnostic weakening. |
| C16 OPEN | Cohort | Six-owner invalidation matrix returned; five unchanged owner bytes do not preserve changed loaded context or emitter freshness. Reconcile actual SH05.1/SH05.6/SH06.6 and resolver facts for ROOT's selected candidate. | Reuse only evidence whose actual assumptions survive; reject stale/coherently substituted upstream products. Stop at missing selected plans/environment/cohort comparisons; no invented macro/resolver feature or historical repin. |
| C17 OPEN | ROOT, independently routed reviewers | No stable authoritative candidate is selected here. After required C12-C16 rows, select exact source/compiler/dependencies, support/TCB and comparison modes through existing BOOT5/7/8 records. | Require applicable fresh provenance/conformance/stage/diagnostic comparison and independent acceptance. Failed/interrupted/skipped required evidence or mismatched identities block this claim; ordinary changed-candidate checks require no attempt grant. |
| C18 OPEN | ROOT | Decide only from C01-C16 scope coverage plus accepted C17 evidence and the six milestone requirements. Managers supply frozen handoffs, not their own completion attestations. | Accept only the supported bounded module decision or full declared milestone gate actually met. Reject unsupported scope promotion; bounded module migration leaves SH-07 partial when its broader exit gate remains open. |

## Required cases and evidence per task

C01's matrix is a section/table in the existing planning/support records, not
a new executable manifest. Each required variant row must contain:

1. C-task ID and one of SH-07.1 through SH-07.6, with the owning contract rule.
2. Exact source revision, path, function/form use or transitive dependency
   showing why the behavior is required; generated forms identify their macro.
3. Owning entrypoint and expected Core product/order/control/source invariant.
4. Accepted `.gravity`/`.qst` case and each applicable rejection-family case,
   expected stable diagnostic and provenance; spellings actually run are named.
5. Exact existing/new owning selector, real composition boundary and evidence
   reference, with tested revision and implementation/integration/migration class.
6. Status: located-only, implementation-gap, verification-open, invalidated,
   accepted-for-scope, or outside-subset with justification and rejection case.

A missing required variant, unchecked proof obligation or unsupported required
primitive is an open row. A valid structured rejection closes a negative case;
it does not close required positive support. An outside-subset row is lawful
only when no selected compiler dependency uses that behavior.

The 13 core forms are covered by C02 (`quote`), C03 (`def`, `var`), C04
(`if`, `do`, `let`), C05 (`fn`), C07 (`loop`, `recur`), C08 (`set!`), C09
(`try`, `throw`) and C10 (`match`). C06 covers ordinary calls/dispatch rather
than inventing a fourteenth special form. C11-C15 cover cross-cutting C6
artifacts, preservation, authentic composition and rejection obligations.

## C01 working support map

The selected *source subjects* are the six complete owner files in the
[current cohort table](self-hosting-current-migration-cohort.md#c01c16-reconciliation-at-2a94fb71),
not six snippets, every directory entry, or every later typed/native owner.
The output is ordinary verified canonical Core under `:meta/:jvm`. Source
analysis, execution of the owning Gravity lowerer, and typed execution of a
compiled compiler are three different claims. Only the first two belong to
this bounded proposed module decision; the third still needs its real later
producer/consumer/build graph.

The retained source censuses identify `defn` expansion into original `def`/`fn`
and reused bodies, with quote/catalog DATA kept separate. They do not provide
the complete source-occurrence-to-Core/primitive matrix required to close C01.
The table below populates concrete starting rows without inventing the missing
variant population. Each test name is a locator in the existing owning test
file, not a newly executed or automatically applicable result. Status is
**located-only** unless the original scoped result is explicitly retained by
the audit; candidate-wide acceptance remains **verification-open**.

The [Core test owner](../bootstrap/clojure/test/gravity/self_hosting/sh07_checked_core_test.clj),
[B47 composition owner](../bootstrap/clojure/test/gravity/self_hosting/sh07_b47_function_call_recursion_test.clj)
and [fragment owner](../bootstrap/clojure/test/gravity/self_hosting/sh07_module_fragment_test.clj)
contain the named checks below; the remaining owner paths are in
[Existing validation starting points](#existing-validation-starting-points).

| Required use or unresolved variant decision | Tasks and owning rule/product | Existing accepted/rejected checks and evidence boundary | Missing closure / invalidation |
| --- | --- | --- | --- |
| Six owner `def` DATA and `defn` bodies; quoted type/catalog maps, for example `sh07-atomic-type-id` | C02/C04/C05/C11; L1/L2/C4/C6 preserve kinds, inert quote, constructor order and generated `def`/`fn`. | `sh07_checked_core_test.clj` carrier-shaped DATA and decimal kernels; mutation tests for character occurrences, map source order and quoted duplicates; retained SH05 subject/reference comparisons. | Join every required literal/constructor/quote occurrence, including rejected shape/value/origin cases. A `defn` count is not execution of every body or completeness of primitive support. |
| Named owner calls and exact source binding identities; `sh07-lower-reference`, `sh07-lower-def`, `sh07-lower-var` | C03/C05/C06; C5/C6 retain lexical/namespace/definition/value and call-edge distinctions. | `sh07_references_calls_test.clj`, alias-qualified and var-reference owners; `sh07-b5-qualified-var-development-kernel`; SH06 genuine imported/private/unresolved and stale binding compositions in the audit. | Select actual qualified/import/var uses, if any, from executable positions, not quoted handlers. Preserve upstream C5 rejection versus Core rejection. No ambient host lookup or inferred callee artifacts. |
| Ordinary owner `if`/`let`/`loop`/`recur` algorithms; for example `sh07-vector-contains?` and `sh07-b13-flatten-fragment-field` | C04/C07/C11; L2 control/order/lexical/nearest-target records. | Existing control/let and loop/recur selectors remain scoped evidence. New `sh07_current_loop_helper_test.clj` / `current-loop-helper-source-to-gravity-core-and-hostile-replay` copies verbatim `checked_core.gravity:363-369` into the 435-byte paired `sh-07/compiler-vector-contains` fixtures, preserving private visibility. On base `ae6acbe9`, one actual current-reader SOURCE4 -> C5 -> Gravity Core product and one coherent target-substitution replay passed 1 test/133 assertions; the owning fresh Gravity template verifier returned `C6-VERIFY` / `:template-replay` for the mutant. The normal-broker single 2GiB JVM exited 0 under its unchanged 120s watchdog: 62.32s test execution plus 6.61s namespace loading (development observations, not a performance claim). Original log/timing: `/tmp/sh07-current-loop-helper-ae6acbe9-20261024-attempt1.{log,edn}`. | Only G traversed those compiler stages; Q received UTF-8 fixture-text parity checks. This separate subject authenticates its own fixture path and copied source occurrences, not the full 538443-byte production module. Other required variants and full-subject joins remain OPEN; no C7 compatibility, runtime, migration or C-task completion follows. |
| Ordinary functions/calls, including real two-function products | C05/C06/C12-C14; original function signatures, parameter IDs, ordered arguments, captures and recursion components. | `sh07-b47-two-i64-call-current-gravity-composition`: retained real current-reader G composition, public fresh verification and session mutations at its original revision; Q source selector was not run. B48 is separate direct-call legality. | Do not turn paired byte/seed-parser checks into Q composition, raw Core into checked C7-C10, or B48 into public ordinary-call routing. Required closure/cross-module/indirect/variadic variants need an explicit use and consumer decision. |
| Mutation, typed errors/handlers and structural/guard match variants | C08-C10; L2/L7/L9/C6 location/RHS order, control exits, clause scopes and structural-failure skipping. | Qualified mutation, throw/try/multiple-catch, match/vector-pattern owners; quoted-symbol, keyword-map and root-map-guard kernels plus their source/forgery negatives. | Implementation exists for scoped variants, but source spelling in the lowerer's quoted dispatch DATA is not a required executable use. Do not exclude a variant as optional or close its task without the complete selected use matrix. Later guard Bool/totality, effects and ownership remain their owners' obligations. |
| Complete Core graph, fragments, maps, retained metadata and declarations | C11/C13-C15; C6 canonical products and authentic source/input joins. | `sh07-b13-fragment-manifest-is-exact-root-aligned-and-exhaustive`, `sh07-b13-fragment-coverage-is-exact-ordered-and-complete`, `sh07-b13-fragment-request-alterations-fail-closed`; owning template/resolved/origin/source-map checks. | Inspect actual tested inputs/results and constructor route. Public default-pinned tests are not current-reader evidence. Graph/identity/provenance/source mutations require the real affected boundary, not a copied passed flag. |
| Full checked-Core source as an analyzed subject versus the Gravity builder as executor | C12/C16, distinct from C02-C11 kernels. | Audit records the incomplete exit-143 source attempt and bounded smaller subject results. Existing `b-run-frontend-subject` ends in hosted `gravity.c6-core-lowering`, not `gravity.checked-core`. | Current Gravity source-analysis/replay/provenance remains OPEN with its own finite subject/resource decision. The legacy attempt remains INCOMPLETE, cause unresolved; no unchanged large retry or mandatory hosted-product redesign follows. |
| Required raw seed primitives behind all six selected owner plans | C01/C11-C12; D1/L2/C6/BOOT2/BOOT8 and declared temporary TCB. | The [selected-owner map](artifacts/phase-15/reports/sh07-c01-selected-owner-use-map-v1.edn) and `sh07_selected_owner_primitive_use_test.clj` construct actual seed plans and join emitted calls to original function spans/body paths; details below. `compiler.gravity` remains rule DATA. | Potential compiled-use mapping is now recorded for all six selected plans. Accepted/rejected primitive case joins, actual runtime reachability and subsequent Core products remain OPEN. An allowed builtin declaration is still not actual use or implementation acceptance. |
| Typed downstream primitives used by the actual Core source: `sh07-exact-keys?` uses `map?`, both `count`s, `keys`, `set` and equality; Vector traversal uses `first`/`rest` | C14 affected integration; SH08/SH09/SH13/SH14/SH22 implementation ownership, not new Core forms. | In `sh08_function_call_type_test.clj`: `sh08-vector-rest-current-source-composition`, `sh08-map-keys-current-source-composition`, `sh08-list-set-current-source-composition`, `sh08-bare-keyword-anchor-current-source-composition`. Merged [PR265](https://github.com/burn2delete/uniform/pull/265) and [PR269](https://github.com/burn2delete/uniform/pull/269) add accepted scoped G composition: explicit private `BareMap keys -> HList Keyword -> List set -> bare Set -> nonempty literal Keyword anchor -> Bool`, 61 assertions. | This does not type or execute the whole unannotated helper or establish a variable expected Set, C8, runtime or migration. `map?` supplies no key-domain facts; call shapes do not automatically generalize parameters. Owning SH09/SH01 results remain separate. Do not narrow the helper's non-Map-to-false behavior by silently adding a Map annotation. |

### C01 selected-owner seed-plan use map at `0067e941`

The [EDN report](artifacts/phase-15/reports/sh07-c01-selected-owner-use-map-v1.edn)
records actual construction at base `0067e9418de992900a95cd47d016029af62ff99c`.
The new test uses existing seed/compiler APIs and owning SH03-SH07 builders;
SH02 uses its raw seed plan before the owner's byte-count augmentation, named
explicitly in the report. Each selected function has its original definition
span, definition/plan hashes, instruction summary and emitted call rows with
original body path, plan path, callee and arity. Quoted payloads are inert.
All six complete sources are read, but SH03 compiles its existing prefix
closure only; the 44 other reader functions are named with their source spans.

| Source owner | Selected functions | Builtin-call occurrences | Actual plan SHA256 suffix |
| --- | ---: | ---: | --- |
| Authenticated envelope | 72 | 648 | `e0d42dd125d186e8a1e0a37f0b69d052ef60eecd568a46f3c902b0aba473d10e` |
| Reader | 231 | 2138 | `c419442a5a64d534484bc0181c862aeb118024ae979bcaa7c4d0af628eeb3f96` |
| Syntax | 102 | 858 | `07178c6b5b68b9b17bbd3bd4144aa3e4b97171fa6bda6494410cb339677f0adc` |
| Macro | 46 | 460 | `382fd0afe5e271a96d006f69cc76d40a67a68f0ef8abd091c133271b1f7c3e93` |
| Resolution | 111 | 1474 | `8e029b8266fd3cc88a9a62ec6e83eca41ae86291598a803da2ff14a4a751c506` |
| Checked Core | 402 | 7090 | `4fd7a53bac6597c496e0baf3f4a0db5ae153b075e80087b235fd3fb7dffad4e0` |

The total 12668 builtin-call occurrences describe potential uses in compiled
functions, not a trace of invoked functions. Per-primitive case-join obligations
remain explicit, with existing check owners and locations in the function rows.
The report binds actual source bytes, compiler rule DATA, emitter freshness,
bootstrap/loader/load-order inputs, dependency options and a content identity
over all 2146 seed resources. Runtime/classpath/JVM observations are included;
external toolchain migration identity remains unfilled. These are construction
dependencies, not a claim that Gravity compiled its successor or that emitter
lowering ran.

One actual normal-broker 2GiB/120s-watchdog batch passed 13 tests/40 assertions
in 11730.156792 ms: the new tests 2/17, boundary 6/11 and LOC 5/12. Actual
plan construction/source-call joins and instruction conservation passed.
Missing/duplicate owner, changed input, forged plan/function identity and
unknown-instruction mutations reject in the test join; those record checks
are not compiler source rejection or a compiler build. The quote control
retains unknown symbols as inert DATA. Raw output is
`/tmp/sh07-c01-selected-owner-0067e941-20261024-attempt2.log`, SHA256
`92a1f92841309e8bbbc85df5974a2ae4a1a6832d90f93776d0eb4e5db6a65445`.
The earlier parent namespace preflight failed before test loading, source-plan
construction or assertions; preserve `attempt1.log` beside it, SHA256
`861d20b465985fdbeaeceff08633cce45bba070100999d035a627c8fee9bc90b`.
The original PR304 report SHA256, before the primitive-case supplement, is
`e2792e6582e1c328d977150ff0b44f9bdd5f46ff03494796b4d27ba40b3d964b`.
No full source-to-Core, runtime primitive, native, migration or C01 completion
credit follows. Subsequent required occurrence/product and accepted/rejected
case joins must use this actual population rather than substituting an allowlist.

### C01 primitive runtime cases at `80881ba6`

The EDN's `:primitive-case-evidence` supplements the original construction
record; its six owner rows, selected plan identities and original dependencies
are unchanged. Source, compiler-rule, emitter, dependency-option and complete
seed bytes were conserved from PR304. The seed Git tree is
`6d09b4788df56c2a732f1bbbcdf867560cdabad6`. Existing runtime tests remained
unchanged, SHA256
`9433e4fd53c269eabb1203d024e761cc76faa91da1bae70655d2a985f12ba97d`.

| Actual execution | Result | Scope |
| --- | --- | --- |
| Eleven existing exact SH01 runtime test vars | 11 tests/294 assertions; 14632.794333 ms | Generic/interpreted unary, lookup, equality, association and finite arithmetic cases, including their selected arity and host-error rejections. |
| New `selected-owner-observed-arity-runtime-cases`, plus boundary/LOC | 12 tests/98 assertions; 7903.280625 ms | New test 1/75; boundary/LOC 11/23. These are distinct from the preceding execution, not a new aggregate of both runs. |

The first execution supplied positive cases for 35 of the 36 used primitives.
Joining actual used arities exposed specific uncovered rows: binary `<=`, `>`,
`>=`; `assoc` arities 7, 9, 19 and 21; binary `conj`; and zero-argument `list`.
The new owning C01 test checks those exact observed arities through both
existing generic and interpreted runtime APIs. It checks true/false finite
comparisons, normal persistent Map association, Vector/Set/List insertion and
empty-list construction, with paired wrong-arity diagnostics and noncollection
`conj` rejection where defined. No invalid arity is invented for variadic
`list`. The test verifies that its arities occur in the reviewed use map and
that all six owner byte identities still match.

Each primitive row now links its fully qualified positive/rejection test vars,
observed and tested arities, and explicit remaining domain/invocation/legality
obligations. No observed arity lacks a normal-value positive case in these
selections. This does not prove actual source value domains, all primitive
inputs, dynamic owner reachability, Core products, C7/C8 legality or migration.
The original runtime-case obligation stays OPEN for those stronger joins.

Both commands used one normal broker lease, one 2GiB JVM and the existing
120s watchdog, and exited naturally with no failures/errors. Raw files are
`/tmp/sh07-c01-primitive-case-80881ba6-20261024-attempt1.log` (SHA256
`65c8fd11ee14a565c41e40b3f21f174dc8d17dd7a273e000cedc65e17cd0a0dd`)
and `/tmp/sh07-c01-observed-arities-80881ba6-20261024-attempt1.log` (SHA256
`8be878b47bb72d634cdeea7d3511dcfba67e03261770c84f09c28c9e63b0dbd8`).
The EDN retains per-var counts, domain limits, raw-result hashes, the new test
hash and the post-run unchanged-worktree input-map snapshot. It does not
mislabel that snapshot as a separately measured launch pin or these changed
working-tree executions as clean frozen-commit runs. No source implementation,
runtime provider, shared interface, allowance or milestone status changed.

### C01 structural instruction positions at `d42f23b2`

The next audit found 61087 actual selected instructions but only 16071
individually positioned calls. The remaining 45016 instructions had counts
without source/plan positions. The existing C01 walk now emits a compact tuple
for every visited instruction:
`[opcode source-expression-path plan-path source-form-kind]`. Form kind uses
the existing hosted reader classifier; these are structural function-body
paths, not canonical C2 IDs, token spans or a new source-authority schema.
Rows retain worklist visitation order, not evaluation order. Actual plan child
indexes remain explicit in each path. A symbol source occurrence can emit a
literal instruction; the row preserves that distinction without claiming its
definition/binding legality. Quoted payloads remain leaves and produce no
executable child rows.

One actual normal-broker 2GiB/120s-watchdog batch passed 15 tests/132 assertions:
actual six-owner construction/joins 1/16, forged-record cases 1/13,
quote/reference-kind controls 1/5, observed-arity runtime cases 1/75 and
boundary/LOC 11/23. The recorded test-completion interval was 16887.307333 ms;
it is not a CPU, RSS or whole-release performance claim. All 61087 positions
match their actual opcode summaries and have unique source/plan paths within
each function. Missing, duplicate and forged opcode/source/plan-position rows
reject through the test join. These record checks are not compiler source
rejection or source-to-Core validation.

The EDN preserves all original owner fields, plan/function identities, call
rows and earlier case evidence. Fresh construction observed the same six plan
identities and source coordinates; earlier physical span paths remain recorded
with their original context, while fresh construction paths and dependencies
are separate execution evidence. The measured tracked host inputs still match;
PR308 changed Gravity C15 source outside this selected execution, so that match
does not assert unchanged C15 bytes or consumer behavior. The generic report
projection no longer stamps fresh output with the old hardcoded revision;
the execution record supplies its actual local composition and main reference.

Actual raw output is
`/tmp/sh07-c01-instruction-positions-d42f23b2-attempt2.log`, SHA256
`9cbcb2061b144dadb88fb175e203c6f6605614a7bdcf5a05d12d868e216a6d77`.
The EDN pins its 2156-byte output, result, fresh projection, executed test,
scratch method and unchanged input-map snapshot. The prior stdin-method EOF
failure remains retained as a reader failure before lease/test-body execution,
with zero assertions; it is not a consumed proof. Current construction emitted
real seed instructions, not a compiler binary, canonical Core, type/effect
legality, full-reader execution or migration evidence. All C-tasks remain OPEN.

### C01 selected local-call declaration joins at `af04fecc`

The next source audit joined all 3403 selected local-call occurrences to the
actual normalized declarations behind the six byte-pinned sources. Every
observed call has the declared fixed arity. C01 previously recorded only the
callee name and argument count; it now records the ordered `:parameters` and
`:fixed-arity` for all 964 selected functions. The existing owning test checks
each declaration hash, source/plan signature equality and local-call arity.
Earlier call rows, instruction positions, plan identities, span coordinates
and case evidence remain conserved. The fresh physical paths and dependencies
are separate execution observations.

One actual normal-broker 2GiB/120s-watchdog batch passed 16 tests/143 assertions
on the changed worktree based at `af04fecc709ce585938f555b3d972ab80d5f5b5d`.
The five focused tests passed 120 assertions; boundary/LOC passed 11 tests/23
assertions. The recorded test-completion interval was 13801.49925 ms. Real
seed plans from one matching call and too-few/too-many source calls establish
the C01 record check's acceptance and `SH07-C01-JOIN` / `:local-call-arity`
rejection. These are not B48 compiler-legality results. Forged declaration
hash, fixed arity and parameter rows also reject. No compiler implementation,
public routing or source legality was changed.

The existing EDN's `:local-call-signature-evidence` pins the executed test,
method, raw result, projection and earlier source audit. Raw batch output is
`/tmp/sh07-c01-local-call-signatures-af04fecc-attempt2.log`, SHA256
`33e5828cea9dbec5d09fe478da299248273668cc946364584cea96d91be0ed0c`.
The first controller timed out at broker admission before executing tests;
its raw output and unexecuted selector typo remain historical. The successful
runner's copied main-reference string and count label were metadata mistakes:
the actual main ancestor is `d42f23b22f76cd89cc1f52cc687d3f5ab224ceb8`, verified
as its exact merge base with the execution parent; the printed count 61087 is
instruction positions, not function signatures. Raw files remain verbatim,
with explicit corrections in the EDN. The test result and exact execution
parent are unaffected.

These are selected seed declaration/call joins. They do not establish C5
binding authority, C7 compatibility, argument value domains, execution of all
functions, canonical Core products, migration or C01/C06 completion. The 44
reader definitions outside the selected plan remain explicit; all C-tasks
remain OPEN.

### C01 full function-body requirement matrix at `6585164c`

The selected-plan map could not establish what the 44 uncompiled reader
definitions require. A separate audit read all six byte-pinned sources,
normalized their function headers/bodies through the existing seed APIs and
walked every function body. It found 1008 functions and 61542 structural source
positions. The 964 selected functions account for exactly the existing 61087
positions; the remaining 44 reader functions add 455 positions. Quote payloads
are DATA leaves; binding names are declarations, not evaluated initializer
expressions. The source rows are structural paths, not C2 identities or Core
products.

| Source | All functions | Body positions | `if` | `let` | `loop` | `recur` | `quote` |
| --- | --- | --- | --- | --- | --- | --- | --- |
| authenticated envelope | 72 | 3308 | 278 | 29 | 0 | 0 | 0 |
| reader, including 44 outside the selected plan | 275 | 12283 | 767 | 168 | 79 | 105 | 24 |
| syntax | 102 | 4913 | 303 | 43 | 32 | 36 | 4 |
| macro | 46 | 3255 | 201 | 14 | 7 | 7 | 19 |
| resolution | 111 | 6554 | 454 | 74 | 88 | 116 | 7 |
| checked Core | 402 | 31229 | 2110 | 410 | 200 | 227 | 48 |
| Total | 1008 | 61542 | 4113 | 738 | 406 | 491 | 102 |

The existing EDN's full-body supplement records each source identity and
separates selected/outside summaries. Selected source paths/kinds are joined
to the existing instruction rows without duplicating all 61087 records.
Outside definitions retain hashes, spans, ordered signatures, body positions
and binding-pattern locations. All 2710 `let`/`loop` binding patterns are
symbols. The extra 44 functions add no builtin-name calls and 35 calls matching
their normalized source function table. This last classification is not C5
binding authority: qualified `reader/...` declaration/call spellings still
need their actual resolver/namespace disposition. None of these 44 functions
has acquired a compiled plan, Core product or migration acceptance.

This is a partial required-use matrix. Every row below remains
verification-open for the complete requested scope. Existing selectors in the
validation table are locators; their historical results are not new executions.

| Task / contract | Required source-body family | Required product and remaining acceptance/rejection join |
| --- | --- | --- |
| C02 / L2, C6 | Actual nil, Bool, integer, character, string, keyword, vector/map/set positions and 102 quoted DATA leaves. No executable decimal/ratio body literal was observed outside quoted interiors. | Preserve kind/value, constructor child order and inert quote for each required occurrence. Join the existing literal/decimal/character/order owners and source negatives; broader declared literal support is not waived. |
| C03 / L3, C5, C6 | Symbol references and 1008 source definitions, including qualified reader names in the 44 uncompiled definitions. | Actual C5 definition/reference/namespace identity and Core source maps remain required. Source-table name membership cannot supply resolver or imported-callee artifacts. Top-level `def` initializers are not covered by this body audit. |
| C04 / L2, C6 | 4113 `if`, 738 `let`, symbol-only sequential binding patterns; no executable `do` body form observed. | Preserve truthiness, exclusive branches, initializer scope and source order. Join selected occurrences to actual control/let products and malformed/order/origin rejections. Absence of explicit `do` does not establish sequencing correctness. |
| C05 / L2, C4, C6 | 1008 `defn` headers/bodies require generated function/definition products. No nested executable `fn` was observed. | Preserve macro origin, ordered parameters/body, visibility and captures where applicable. The complete definition-to-Core join remains open; the declared nested-function contract remains separate. |
| C06 / L2, C6 | 12668 selected builtin-call positions, 3403 selected local calls and 35 additional source-table name matches. | Preserve actual operator/argument order and call edges, then obtain the selected consumer's arity/dispatch legality and negative results. A table match is not public B48 routing or call execution. |
| C07 / L2, C6 | 406 `loop` and 491 `recur` positions. | Preserve nearest targets, simultaneous ordered operands and tail placement. Existing scoped helper products do not cover all positions; target/arity/tail/order rejection joins remain required. |
| C08 / L2, C6 | No executable `set!` in these normalized function bodies. | Keep the broader declared location/RHS/order and rejection requirements open. Quoted dispatch tokens are not executable uses; this result is not an outside-subset exemption. |
| C09 / L2, L9, C6 | No executable `try`, `throw`, handler or cleanup body form observed. | Keep the broader declared error/handler/order/origin and rejection requirements open. This census neither implements nor excludes cleanup support. |
| C10 / L2, L7, C6 | No executable `match` in these normalized function bodies. | Keep the broader declared pattern/guard and rejection requirements open. Literal match-handler catalog DATA does not prove an executable pattern use or satisfy its conformance gate. |

Namespace clauses, top-level DATA initializers and quoted interiors were not
inventoried as executable function bodies. Their declaration/value/origin
obligations remain with C02/C03/C05/C11 and require a separate source-region
join. The body inventory cannot establish their absence, complete source
coverage, Core acceptance, downstream legality, execution or any C-task's
completion.

The changed full-body batch passed 18 tests/175 assertions under the existing
normal broker, 2GiB heap and 120s watchdog, with natural exit 0. The seven C01
tests passed 152 assertions; boundary/LOC passed 11 tests/23 assertions. The
recorded completion interval was 15293.026542 ms. The independent source walk
joins every selected path/kind to the existing plan positions and retains all
44 outside definitions. Negative controls distinguish binding declarations
and quoted payloads from executable children, and reject malformed or newly
unclassified inventory shapes. Those are C01 inventory failures, not compiler
source-rejection results. The first controller timed out at broker admission
before the test body and remains historical with zero assertions.

Raw successful output is
`/tmp/sh07-c01-full-body-families-6585164c-attempt2.log`, SHA256
`6c779574203bd2ea81803abb99572a715a824c775a5b00f06dcd42b2dd4dbca5`.
The EDN pins its exact dirty-worktree test source, method, raw result, fresh
family rows and original audit, on parent
`6585164c9f8a954d98d3c3e933456eb2463810e0`. Main reference
`95c3a56e2492b7509a7c55acffb6cc5ca25af72d` had identical six source files at
inspection; it was not this execution's checkout. Every prior report field
remains conserved. Namespace/top-level initialization and source-to-Core
acceptance remain unproven.

### C02 quoted reader helper at `8ee78280`

The exported original `read-source-form-records` helper in
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity` now has a genuine
current-reader C5 -> owning Gravity Core composition. Its complete declaration
is copied verbatim into the paired 356-byte `sh-07/compiler-reader-quote`
fixtures, with the selected original export retained. The fixture namespace
and physical path are the authenticated subject. This does not authenticate
the complete reader module or expand the raw SH03 prefix plan; this function
remains one of the 44 definitions outside that selected seed plan.

`sh07_current_reader_quote_test.clj` verifies the public definition, original
parameter binding, source/macro origins and three actual Core nodes: `def`,
`fn` and `quote`. The quoted map retains its `source-bytes` Symbol and ordered
vector as data. The quote has no executable children, records
`:no-evaluation`, and creates no Core expression references or calls. The
owning Gravity template verifier rejects a same-kind quoted Symbol change and
a quoted vector-order substitution with `C6-VERIFY` / `:template-replay`.
Both changed templates retain matching identity-preimage node fields.

The batch passed 12 tests/73 assertions: focused 1/50 and boundary/LOC 11/23,
with natural exit 0 under the normal broker, 2GiB heap and unchanged 120s
execution watchdog. Execution took 69683.117167 ms. G traversed the actual
compiler stages; Q received fixture-text parity only. C7-C10 remain pending.
Other quoted occurrences and outside-plan functions, complete source coverage,
full-reader compilation, migration, self-hosting and C-task completion remain
OPEN.

The execution used exact base
`8ee78280272622c870a9d336cd777114ede447b5` and host source subtree
`ba78956df0df88f90fddd71fbed3bd14058985d8`, including the new native carrier
header observation TCB. This is a distinct context from earlier `4fda9727`
and `26a8283e` products. The result records unchanged before/after source,
fixture and test pins. Raw evidence is under
`/tmp/sh07-current-reader-quote-8ee78280-`: `attempt1.log`, `result.edn` and
the 715635-byte `products.edn` inspection projection, whose SHA256 is
`f32d424de0a38d7532eeea5ce8e39634da11683173ddc6bd6ad3721fba24797a`.
The projection records actual Core, authenticated request, reader identity and
negative products; it does not replace an admission artifact.

The consumed C3 construction implementation subsequently changed. After
reconciliation onto exact `5b80b77feb1a3d12c12b28f1061da9a7fe68deae`, a fresh
affected source composition and both owning verifier rejections passed again:
12 tests/73 assertions, focused 1/50, natural exit 0 in 74824.111667 ms under
the same normal-broker 2GiB/120s limits. All test/fixture bytes were retained;
the new execution records host subtree
`eb5eef6acb730dbba3317139b3769b83ad2e3ea8`. Its evidence prefix is
`/tmp/sh07-current-reader-quote-5b80b77f-`. The inspection projection is
byte-identical to the earlier projection, with the same SHA256 above. This
observed output equality does not equate the TCBs, relabel the earlier run,
renew migration authority or establish a performance improvement.

### C01 source declarations and leaf equivalence at `c7d31338`

The next source audit found 37 top-level DATA definitions and six namespace
headers in addition to the 1008 function definitions. It compared all selected
seed leaves with their source forms: 21902 literal values, 14750 local
identifier spellings and 78 quoted payloads. All matched. There were zero
selected-body references that inline a top-level DATA symbol, so no such use
is invented. The earlier full-body supplement retains its historical
body-only scope; this new declaration supplement records the additional
namespace/initializer source regions.

A concrete gap remained in the C01 test record validator. On frozen `b32f1c8a`,
an actual envelope-owner probe changed a Bool literal and redirected a local
reference from `state` to the existing parameter `preimage`. It recomputed the
function hash and whole plan ID while preserving the source hash, definition
hash and positions. The validator accepted both altered plans. This was a
C01 source/plan record gap, not a public compiler or Gravity Core verifier
vulnerability claim.

The owning check now compares scalar literal values, local names and quoted
payloads directly with source. It retains the existing seed literal-table
boundary if a source symbol is inlined. The EDN's new declaration/leaf rows
record namespace form hashes/spans, normalized module headers and each DATA
definition's initializer kind/hash and normalized value hash. DATA normalization
uses the declared temporary seed API; it is not an actual Core `def` product,
profile/effect legality or execution of the initializer.

Actual tests reject a same-kind Bool flip and a redirect to another existing
parameter after both plan/function hashes are resealed. Stable record
diagnostics are `SH07-C01-JOIN` / `:literal-value` and `:local-name`. The changed
batch passed 20 tests/207 assertions (C01 9/184; boundary/LOC 11/23) under the
normal broker, 2GiB heap and 120s watchdog, natural exit 0. Its recorded
completion interval was 15933.343916 ms.

The tested private composition is
`c7d31338fb53e832ce63eefd408b3be19a10d14d`, including main
`f2bb3a35e6da25d66be99efbfa82a8aee4cfe28e`, the accepted reconciliation and
the dependent C01 work. Its loaded host source subtree
`4fda9727f1ca84cb8e3ad36f09865da94135ed6c` exactly matches that main reference.
The new Symbol ABI and reader intake policy changed the seed TCB; the new
execution dependencies are recorded separately, with all prior artifact
fields and their earlier contexts preserved. An earlier old-context test
controller timed out before broker admission/test execution and contributes
zero assertions.

Raw successful output is
`/tmp/sh07-c01-source-leaves-c7d31338-attempt1.log`, SHA256
`972dce278389cd4259dc414e09ad602b828881bc1f6dbc2c63f3e2658f0a0d3c`.
The existing EDN pins the source audit, before-fix accepted-mutant probe,
failed admission, current test source/method/result and fresh declaration
rows. C5 binding identities, runtime primitive argument domains, actual Core
products, full-reader compilation, migration and C-task completion remain
OPEN. The private composition is not a claim of testing an exact main checkout.

### C02 original DATA declaration at `4688551f`

The source-region gap now has one actual Core join for the original
`authenticated-envelope-bounds` declaration in
`bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity`.
The paired 724-byte `sh-07/compiler-envelope-bounds` fixtures copy its complete
15-entry initializer and retain the selected declaration's explicit export.
The fixture namespace and physical path are their own; this does not
authenticate the complete owning module or its original namespace artifact.

`sh07_current_data_declaration_test.clj` checks the original text and export,
then constructs genuine current-reader C5 and owning Gravity Core products.
It joins the public definition binding to its once-only initializer, all 30
keyword/integer children, exact constructor order, literal kind/value and
source maps. The resulting Core has 32 nodes. The owning Gravity template
verifier rejects a same-kind integer increment and a complete map-entry
reordering with `C6-VERIFY` / `:template-replay`; the altered templates retain
their corresponding identity-preimage node/order fields.

The corrected batch passed 12 tests/338 assertions, including the focused
1/315 and boundary/LOC 11/23, with natural exit 0. Execution took
68805.182625 ms under the normal broker, 2GiB heap and unchanged 120s execution
watchdog. G traversed the actual compiler stages; Q received fixture-text
parity only. C7-C10 remain pending. The other 36 DATA declarations, complete
source-region coverage, full-owner compilation, migration, self-hosting and
every C-task's completion remain OPEN.

This execution used exact base
`4688551f053fcefbb9b2cabdd2b4f91bfd7558ca` and loaded host source subtree
`4fda9727f1ca84cb8e3ad36f09865da94135ed6c`. Raw output is
`/tmp/sh07-current-data-declaration-4688551f-attempt2.log`, SHA256
`0bb88d1d7b833b387bf17af7a6235b69953a085ec3520f4d3548f6ba43fdc034`.
The matching `-result.edn` records six unchanged source/test/fixture bookends;
`-products.edn` is an 832446-byte inspection projection of actual Core,
authenticated request, reader identity and negative products, SHA256
`97f512ad77986762a3714b12a3bc370eed9daa28da3a43f478649b522bfe55bf`.
It is not a replacement admission artifact.

The first executed fixture omitted the original export and correctly produced
private C5/Core bindings. Its public-visibility assertion failed; the raw log,
test subject and products remain retained as attempt 1. Adding the original
selected export corrected the fixture rather than changing compiler policy.
An earlier stale-local-main controller was cancelled while awaiting broker
admission and contributes no compiler result. None of these histories is
relabelled as execution on a later main checkout.

### C01 and C16 stopping point

The six full subjects, ordinary current-reader intake, repository load boundary
and concrete evidence locators are recorded. Still MISSING are the complete
occurrence/primitive/import-to-Core variant matrix, external compiler/environment
identities, and candidate-bound acceptance/rejection
results for every required row. This is useful reconciliation, not completion
of C01. The selected seed plans and potential builtin-use joins are now
recorded above, with the existing reader subset explicit; no source or fixture
was newly excluded to obtain a passing count.

The manager's bounded read-only next-gap inspection at `64aa890a` did not
establish a missing required Core feature. For example, the actual
`sh07-vector-contains?` loop/nested-if/tail-recur occurrence at
`checked_core.gravity:363-369` already has `sh07-lower-loop`,
`sh07-lower-recur` and `sh07-lower-if` implementations. Existing
`sh07-b4-products-bind-targets-transfers-and-order`,
`sh07-b4-rejections-are-structured-and-oracle-bound` and
`sh07-b4-target-transfer-and-order-mutations-fail-replay` locate accepted and
rejected checks in `sh07_loop_recur_test.clj`. What is missing is the
candidate-bound join from the original expanded/resolved occurrence to its
actual Gravity-built target/transfer/order/source products, selected checks,
tested revision and retained outcomes. The old source-coverage selector's
historical byte/function pins cannot supply that join. That initial handoff
therefore stopped before proposing a new Core feature. PR294 subsequently
closed one extracted-helper G composition/evidence gap without a production
Core change; full-subject and other required occurrence joins remain open.
SOURCE's separate C7 helper-shape work remains its own C14 input. No new
semantic feature is allocated by this accounting update.

C16 retains the audit's historical 39-S/three-P traceability classification,
not a current 39/42 execution score. SH-05.1, SH-05.6 and SH-06.6 remain OPEN;
the resolver stays development-unadmitted. Earlier expansion/resolution and
hosted-consumer results may retain their original scope where dependency
comparison justifies it. Reader/seed/adapter changes since each original
result, together with later loaded-context and emitter freshness drift,
prevent treating them as one fresh selected cohort without that comparison. C16's
remaining action is that exact upstream/cohort reconciliation, not an invented
new macro feature, marginal resolution fixture or historical corpus repin.

Before C17, the coordinator selects one genuinely stable, independently
reviewed revision and comparison method in the existing BOOT5/BOOT7/BOOT8
records. Source-unchanged evidence is reused only with its actual dependency
assumptions. Any remaining required semantic or interface row receives its
existing C02-C15 owner and proportionate accepted/rejected checks. If only this
bounded module migration succeeds, C18 records it and leaves milestone-wide
SH-07 partial; it does not assert SH25 compiler rebuild, seed retirement,
release, or support for all later targets.

## Existing validation starting points

Test stems below live under
`bootstrap/clojure/test/gravity/self_hosting/`. They are evidence locators,
not a batch command or a claim that every task already has exhaustive checks.
Add missing behavior tests to those owners and select exact vars.

| Tasks | Existing test files and representative selectors |
| --- | --- |
| C02 | `sh07_checked_core_test.clj`: `sh07-carrier-shaped-language-data-remains-ordinary-data`, `canonical-decimal-presentation-development-kernel`; `sh07_mutation_test.clj`: character occurrence, map source-order and quoted-duplicate development kernels. |
| C03 | `sh07_var_reference_test.clj`: `sh07-b5-qualified-var-development-kernel` and qualified-current-namespace/authentication checks; `sh07_references_calls_test.clj`; `sh07_alias_qualified_reference_test.clj`. |
| C04 | `sh07_control_flow_test.clj`: `sh07-b1-if-and-do-preserve-order-and-exclusive-branches`, malformed-control and order/origin mutations; `sh07_lexical_binding_test.clj`: `sh07-b3-let-products-preserve-sequential-scope`. |
| C05-C07 | `sh07_b47_function_call_recursion_test.clj`: atomic-signature and List-parameter development kernels, exact function/call products and recursion/shadowing/order; `sh07_b48_call_arity_test.clj`; `sh07_loop_recur_test.clj`: target/tail and transfer/order mutation checks. |
| C08-C09 | `sh07_mutation_test.clj`: `sh07-b6-qualified-set-development-kernel` and target/RHS/order checks; `sh07_throw_test.clj`, `sh07_try_catch_test.clj`, `sh07_multiple_catch_test.clj`. |
| C10 | `sh07_match_lowering_test.clj`: quoted-symbol, irrefutable-clause and root-map-guard development kernels, authentic composition and forged/source rejection checks; `sh07_vector_pattern_test.clj`: keyword-map development kernel and vector/branch/binding mutations. |
| C11-C15 | `sh07_checked_core_test.clj`: genuine upstream/supported Core, source/file equality, exact origins, projection mutation, malformed carrier, identity/provenance and template/resolved verification; `sh07_module_fragment_test.clj`: ordered complete coverage and module assembly; `sh07_public_core_routing_test.clj`; source-coverage tests for the selected actual subjects. |
| C12, C16-C17 | The cohort assessment's per-input evidence map, the existing SH06 frontend-subject helper and owning SH05/SH06 tests; current cohort audit and stable-module comparison/support records. Historical counts alone do not close these tasks. |

For example, inspect then run the existing direct guard kernel:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --list
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-root-map-guard-development-kernel --fail-fast
```

This example is not full guard acceptance or a command to run for this plan.
Use the [development workflow](development-verification.md) for focused tests,
affected composition, resource bounds and stable-module migration. No heavy
proof, source-analysis retry or native execution is authorized by a task table.

## Execution order and stopping rules

The current critical path is C01 selected occurrence rows -> required
C02-C11 Core closure -> affected C12 construction/C13 ingress/C14 consumer
joins -> C15 required negative matrix -> C17 stable migration -> C18 decision.
C16 invalidation/reuse reconciliation runs alongside ordinary work and must
meet its current upstream obligations before C17; its returned planning matrix
alone does not close them. Dependencies apply to the actual required row, not
an unrelated manager's entire queue. Read-only C01/C12/C16 reconciliation can
proceed in parallel. ROOT alone resolves cross-slice allocation or execution
holds, and a demonstrated disjoint semantic gap need not wait for admission
maintenance. Final required evidence is still a real C17 dependency.

The first next action is C01: adopt and populate the exact candidate support
matrix from the existing cohort/source-use evidence. Independently, C12 selects
the owning Gravity-Core source-analysis/replay transaction with exact resource fit.
Neither action requires legacy hosted-product redesign, old whole-corpus replay or a new
proof architecture. Ordinary demonstrated semantic gaps can be fixed meanwhile.

After C01, work in dependency order: C02-C04, C05-C07, C08-C10, then C11.
C12/C13/C14 follow the affected interfaces; C15 closes the negative matrix.
C16 closes the upstream migration dependency before C17. C17 and C18 are the
single stable-candidate acceptance/completion decision. These are ordering
constraints, not instructions to run every test sequentially in one JVM.

Report implementation and evidence separately. For each C-task, retain the
exact required rows still open and the next concrete source/test action. A
new feature or merged PR does not count as closing a task unless its acceptance
rows are satisfied. Administrative task counts are not a percentage of compiler
functionality or elapsed effort.

Required primitive implementations belong to their real owners: types to SH08,
effects/profile/capability checks to SH09, ownership/safety to SH10/SH11,
MIR/runtime control to SH12/SH13, collections/layout/allocation to SH14,
runtime providers to SH19 and library APIs to SH22. A primitive closes SH-07
work only where it changes required Core lowering or enables its selected
builder execution; downstream development must name its downstream milestone.

SH-07 completion does not require the entire type/effect/ownership/safety
pipeline, compiled application execution, all L7 variants in every profile,
Linux/native toolchain support, repointing every public compiler command,
package refresh, full compiler rebuild, seed retirement or release. It does
require honest pending facts and lawfully supported selected source inputs.
The current staged Linux LLVM release target remains unchanged.

Existing held B51/source-publication, exception architecture and other governed
work keeps its recorded disposition. This planning document does not adopt a
held route as a prerequisite, waive its invariant or change its lifecycle.
If a required task actually depends on such work, C01 records the concrete
edge and the coordinator resolves it through the existing owner and policy.

## Outputs and conformance criteria

The required outputs are one populated support/task matrix, the owning Core
implementation and fixture changes for demonstrated gaps, selected focused
and affected-composition results, and the existing stable-module acceptance
bundle. This document supplies the finite checklist and baseline locators;
it supplies no new test results, migration authority or milestone promotion.

Completion requires all 18 tasks closed for the declared claim, every required
variant row accepted, no required input silently excluded, a verified canonical
artifact and stable negative diagnostics, completed applicable dependencies,
and independently accepted exact revision/evidence. C18 distinguishes module
migration from the full SH-07 gate explicitly. Missing evidence cannot be
reported as a pass, and later compiler-closure validation cannot inherit
unsupported semantics from a bounded earlier claim.
