# SH-07 Completion Tasks

Status: finite implementation and acceptance checklist; SH-07 remains partial.
Orchestration snapshot: 2026-10-24, inspected remote main
`64aa890ad5afa6fec23e13bdfbc4b739dbfeead4`. All 18 C-tasks remain OPEN.
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
priorities without discretionary escalation to Matt. Managers act inside
their exact cap-three allocations; cross-slice/interface/ownership decisions
go to ROOT, which alone routes independent acceptance and integrates main.
Permission-only/proposal-only/await-human labels are superseded for ordinary
authorized work; name actual technical, correctness, provenance, ownership,
review or resource blockers. Do not blindly execute held attempts or replay
consumed runs. Resource/control changes need evidence, the actual owner,
validation and independent review, not blanket limit removal. Platform/tool
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
| Core | Scope/Core closure manager `01a16a54-eeb1-7941-ac35-7be30e47bdc7` | C01, C02-C11, C13 public ingress and C15 subset-negative closure. Current write allocation is this plan and only backlog orchestration/dependency/progress-accounting sections. |
| Construction | `01a138f7-bfa4-7b90-8a22-5b3c417b2e11` | C12 retained construction/source-origin reconciliation. |
| SOURCE | `01a13b8d-509d-7771-b618-06a24933391b` | C14 affected C7 composition and one allocated read-only helper-shape reconciliation; existing C7/effects/emitter/tests and L5/STD3 reservations remain intact. A proposed C15 frontier is conditional and unallocated. |
| Cohort | `01a02f9c-2e5a-7d70-ba6d-d3519701b13f` | C16 current-cohort evidence/invalidation matrix. |

Each manager queue has at most three concrete items. An item names the actual
gap, owning contracts, exact allocated paths/tests/fixtures, input/output,
prerequisites, accepted/rejected checks, completion and stop conditions. Use
`executing`, `resource-queued`, `dependency-blocked`, `awaiting-review`,
`ready-for-integration` and `queue-empty`; these are work states, not C-task
completion or migration credit. Freeze candidates during independent review.
Only already allocated eligible disjoint work can proceed meanwhile. Refill
through ROOT on a concrete dependency event; an empty eligible queue yields.
Do not invent a compiler feature to demonstrate management migration.

The queue snapshot below is an allocation/adoption observation at handoff, not
proof of sustained throughput. ROOT has received all eight intended managers'
acknowledgments of bounded delegation and ROOT-only integration. Advisory
process-review adoption remains pending as recorded below.

| Manager | Finite current queue / state | Dependency event or stop |
| --- | --- | --- |
| Core | 1: existing two-document migration frozen for `awaiting-review`; 2: bounded read-only inspection returned, required-Core-gap allocation proposal `dependency-blocked` by the missing candidate-bound C01 occurrence join. | ROOT routes independent review of item 1. Item 2 stops without implementation because no required missing Core behavior is demonstrated. |
| Construction | Both allocated read-only reconciliation items returned; `queue-empty`, charter acknowledged. | C12 stays OPEN at the exact construction/source-origin evidence gaps. No H6/B51, compact-proof or GIS implementation/execution follows. |
| Cohort | Six-owner invalidation matrix returned after two disjoint read-only inspections; `queue-empty`. | The planning deliverable is complete; C16 remains OPEN. Refill only for an actual selected cohort/evidence gap, not a new stamp. |
| SOURCE | Read-only C14 helper-shape reconciliation returned, `awaiting-review` by ROOT: actual `sh07-exact-keys?` / `sh07-vector-contains?` -> current C7/SH08 tests and retained frontiers. | Supplies C01/C14 input, distinct from Core's C6 gap proposal. The second negative-diagnostic proposal is conditional/unallocated. No JVM, implementation, unannotated-Map extension or held proof trial is granted. |
| MIR producer `01a16a55-1b44-7551-b595-6caceceef034` | Inferred-Integer producer edge, `dependency-blocked`. | Actual reader `(- codepoint 48)` yields Integer; the existing I64 bridge does not establish its mode/range/source facts. Needs SOURCE's real boundary, not another arithmetic demonstration. |
| Diagnostics `01a16a55-1b07-7202-b7ce-fd71fc0103c2` | Nested generated-origin edge, `dependency-blocked`. | Needs an explicit representation at the true producer edge; no current client is invented. |
| Optimization `01a16a55-1b07-7202-b7ce-fda9f821c3ed` | `queue-empty`; no eligible implementation item. | No demonstrated defect in a new supported actual case. ROOT may refill only from a real required producer/consumer gap. |
| Standard library `01a16a55-1b5b-7250-b702-0168997dbbb3` | `queue-empty`; no eligible implementation item. | Bare-Map compiler-client binding needs Core/SOURCE shape; no new library leaf gap is demonstrated. |
| ROOT | C17 and C18 decisions are `dependency-blocked` on their required evidence. | Route independent review and any separately authorized execution when the exact stable candidate and dependencies are ready. |

The later MIR, optimization and diagnostics managers own compiler C11, C13 and
C15 work respectively; those document IDs are not SH07-C11, SH07-C13 or
SH07-C15 task assignments. All existing source/test/interface reservations and
held attempts survive this plan. Managers may use bounded disjoint
`gpt-6.1-sol` subagents at medium or higher effort within their allocations;
they gain neither extra host capacity nor self-acceptance authority.

Ordinary checks share two normal broker leases and one memory-heavy lease;
exclusive checks hold all slots. Use the existing trusted broker coordination
root and canonical heavy lock where applicable. Queue when capacity is
unavailable rather than launching another JVM. Exact heavy/native/bootstrap/
proof/performance holds and consumed grants remain unchanged. Compact-proof,
GIS and H6 implementation/execution remain HOLD; publication of a design
discussion is not clearance. Ordinary eligible work does not need a new
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
ACTIVE state with no approval flags or pending approval/denial blocker. The
reviewer is now idle/unblocked, with three findings pending ROOT disposition;
it is not a migration gate. The earlier pending label remains historical.
This is an adoption observation,
not completed investigation, independent acceptance of this candidate or a
sustained-throughput claim.
The saved heartbeat's existing schedule, target and notification preferences
remain unchanged. ROOT reconciles the
plan, operating memory, heartbeat and all manager acknowledgments before
claiming orchestration migration complete. Configuration alone, this plan,
the C12 reconciliation and the C16 matrix are not compiler progress.

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

Construction's returned read-only C12 input identifies separate concrete
held-route obstructions: `sh07_b51/producer_c.gravity`, `root6_a.gravity` and
`root6_b.gravity` still retain all 13 `:target-kind=:loop` spellings; their
source correction is not integrated. The current 1670330-byte emitter exceeds
the held H6 1048576-byte source-entry cap. That is a static fit mismatch, not
an observed execution result or permission to raise a bound or resume H6.
Source correction and resource fit require separate decisions. The old
checked-Core attempt's named raw `/tmp` log is absent locally; its committed
audit records progress only through the hosted-C6 call, not a returned result.
These are exact C12 stop conditions for those retained claims. They neither
make H6/B51 a prerequisite for the ordinary endpoint nor close the ordinary
full-source/Gravity-construction evidence gap. The retained 36.2-minute failed
attempt stays failed and is distinct from the `b999` interruption. Construction
has acknowledged its charter and returned both read-only items; C12 remains
OPEN. Private 12/22 construction, retained proof/classpath inputs, independent
admission-load binding, full comparisons and actual producer/hostile evidence
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
| SH07-C15 | All six and uniform gate | sh-core with boundary owners | C11-C14 | Complete diagnostics and negative composition matrix: C6 lowering/shape/order/origin/effect-drop/unsafe-drop/domain/verify families as applicable, plus upstream-owned rejection. Stale/substituted/coherently forged inputs and dropped/reordered graph products fail closed; graph limits accept at-bound and reject above-bound. Preserve available source/generated provenance and prevent raw host exceptions. |
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
| C01 OPEN | Core | Six subjects and endpoint are selected; populate original executable/generated occurrences -> Core/primitive/import product -> exact accepted/rejected selectors, actual selected plans and tested identities. Use the existing support map and cohort load boundary. | Stop before an implementation allocation if the allegedly missing required occurrence/product is unproven. Do not exclude a required use or infer closure from declarations. |
| C02 OPEN | Core | Core literal/quote/collection lowerers and carrier/decimal/character/order tests exist. Join each selected subject's required literal and constructor variants to those real products/results. | Accept preserved kinds, inert quote and ordered constructors; reject malformed quote/constructor, unsupported executable payload and forged origin. Missing joins remain OPEN. |
| C03 OPEN | Core; Cohort for C5 producer facts | Reference/def/var and qualified-name tests exist. Select actual executable local/namespace/import/var occurrences and their real resolver/callee artifacts. | Accept exact binding/source identity and shadowing; reject unresolved/private/substituted targets at the owning ingress. Do not fabricate a callee or host lookup. |
| C04 OPEN | Core | `if`/`do`/`let` owner tests cover exclusive branches and sequential scope. Match selected empty/terminal/nested/transfer variants to actual Core and source order. | Accept false/nil truthiness and once-only ordered children; reject malformed arity/bindings and reorder/drop/duplicate mutations. No unselected destructuring feature is added. |
| C05 OPEN | Core | B47 function/parameter/capture products exist. Establish which selected uses require callable values, lexical captures or nested functions, preserving original signature/parameter/body coordinates. | Accept required construction/capture/order facts; reject invalid callable or capture links. Later capture ownership is pending, not inferred here. |
| C06 OPEN | Core | B47 two-I64 current-source composition is bounded; B48 is separate legality. Select actual local/nonlocal/indirect/recursive/dispatch uses and the consumer of each product. | Accept required arity/edge coverage; reject wrong arity or forged/missing edges. Stop rather than claiming B48 public routing, Q composition or general dispatch from a narrower result. |
| C07 OPEN | Core | `sh07-vector-contains?` has real loop/tail-recur uses and existing lowering/positive/negative tests. Candidate-bound occurrence-to-target/transfer/order/source joins are still missing. | Accept nearest target, simultaneous operands, tailness and original coverage; reject wrong target, arity, nontail or reordered transfer. Do not replace missing evidence with a new loop feature. |
| C08 OPEN | Core | Qualified mutation and RHS/target tests exist. First establish which selected executable locations require `set!`; quoted dispatch DATA alone is not such a use. | Accept original mutable location and once-only RHS/result; reject immutable/forged locations or dropped order. Type, authority and safety legality remain later obligations. |
| C09 OPEN | Core | Throw/try/multiple-handler tests exist. Select actual protected/handler/transfer variants and cleanup requirements from the selected population. | Accept required ordered exits and handler scope; reject malformed handler, placement or lost transfer/origin. A required cleanup gap cannot be called optional; no held exception design resumes. |
| C10 OPEN | Core | Scalar/quoted-symbol, Vector, Keyword-Map and root-map-guard products/tests exist. Required constructor/rest/nested-guard population still needs C01 disposition. | Accept scrutinee once, branch binding uniqueness and structural-failure skipping; reject duplicate/malformed/forged guard/constructor evidence. Bool/totality/effects remain later owners. |
| C11 OPEN | Core | Canonical node/source maps, fragments and verifiers exist; header pending declarations are not a current support oracle. Reconcile all selected C02-C10 rows and retained declarations against actual products. | Accept complete L2/primitive/domain coverage and maps; reject residual unowned surface forms, malformed child/binding/source links and dropped metadata. No false checked downstream facts. |
| C12 OPEN | Construction | Reconcile full-source analysis separately from actual authenticated Gravity builder construction/replay. The legacy hosted-C6 attempt is incomplete; bounded Gravity results do not cover all six subjects. Specific retained H6 source-spelling and emitter-cap mismatches are recorded above. | Accept required subjects and the owning constructor under a selected method/resource boundary; reject stale/substituted source membership. Stop those held claims at their exact obstructions; ordinary-route evidence remains separate. No cap increase, H6 replay or new representation/runner. |
| C13 OPEN | Core | Existing source/file/public `sh07-core` routes and routing tests are locators. Select accepted/rejected co-canonical inputs with deterministic identity, actual-path provenance and extension/read policy. | Accept the exact spellings actually traversed; reject wrong extension/UTF-8, rereads, stale paths or forged identity. Byte/parser parity alone is not paired downstream composition. |
| C14 OPEN | SOURCE | Read-only helper-shape frontiers returned for ROOT review: unannotated Map/key domain, variable expected-Set equality, and count-zero/Vector-to-List loop/head premises. Existing real Core -> C7 and primitive compositions remain bounded; no whole-helper diagnostic was executed. | Accept fresh producer outputs and preserved origins/bindings/order; reject substituted/stale/coherently forged facts at affected C7/C8/lowering boundaries. Select actual use/consumer and contract before a remedy; no primitive-kernel-to-whole-helper, native or migration promotion. |
| C15 OPEN | Core; SOURCE frontier only if allocated by ROOT | Core diagnostics, conformance and graph mutation owners exist. Join each required subset-negative family to its real ingress, stable ID/span/profile/target and retained result, including affected producer failures. | Reject shape/order/origin/effect-drop/unsafe-drop/domain/verify forgeries and above-bound graphs; accept at-bound legal graphs. Missing cases or raw host exceptions prevent closure; no diagnostic weakening. |
| C16 OPEN | Cohort | Six-owner invalidation matrix returned; five unchanged owner bytes do not preserve changed loaded context or emitter freshness. Reconcile actual SH05.1/SH05.6/SH06.6 and resolver facts for ROOT's selected candidate. | Reuse only evidence whose actual assumptions survive; reject stale/coherently substituted upstream products. Stop at missing selected plans/environment/cohort comparisons; no invented macro/resolver feature or historical repin. |
| C17 OPEN | ROOT, independently routed reviewers | No stable authoritative candidate is selected here. After required C12-C16 rows, select exact source/compiler/dependencies, support/TCB and comparison modes through existing BOOT5/7/8 records. | Require applicable fresh provenance/conformance/stage/diagnostic comparison and independent acceptance. Failed/interrupted/skipped required evidence or mismatched identities block this claim; no consumed grant replay. |
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
| Required raw seed primitives behind all six executed owner plans | C01/C11-C12; D1/L2/C6 and declared temporary TCB. | `compiler.gravity` rule DATA, `00575`/`00576`/`00580` seed construction and `00588`/`00591`/`00593` execution; repository closure bound in the cohort document. | Exact used primitive/instruction manifest and actual compiled plan identities are MISSING. The allowed builtin list is not a used-call census. A required unsupported primitive remains open at its implementation owner. |
| Typed downstream primitives used by the actual Core source: `sh07-exact-keys?` uses `map?`, both `count`s, `keys`, `set` and equality; Vector traversal uses `first`/`rest` | C14 affected integration; SH08/SH09/SH13/SH14/SH22 implementation ownership, not new Core forms. | In `sh08_function_call_type_test.clj`: `sh08-vector-rest-current-source-composition`, `sh08-map-keys-current-source-composition`, `sh08-list-set-current-source-composition`, `sh08-bare-keyword-anchor-current-source-composition`. Merged [PR265](https://github.com/burn2delete/uniform/pull/265) and [PR269](https://github.com/burn2delete/uniform/pull/269) add accepted scoped G composition: explicit private `BareMap keys -> HList Keyword -> List set -> bare Set -> nonempty literal Keyword anchor -> Bool`, 61 assertions. | This does not type or execute the whole unannotated helper or establish a variable expected Set, C8, runtime or migration. `map?` supplies no key-domain facts; call shapes do not automatically generalize parameters. Owning SH09/SH01 results remain separate. Do not narrow the helper's non-Map-to-false behavior by silently adding a Map annotation. |

### C01 and C16 stopping point

The six full subjects, ordinary current-reader intake, repository load boundary
and concrete evidence locators are recorded. Still MISSING are the complete
occurrence/primitive/import variant matrix, actual selected plans and external
compiler/environment identities, and candidate-bound acceptance/rejection
results for every required row. This is useful reconciliation, not completion
of C01. No source or fixture was excluded to obtain a passing count.

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
historical byte/function pins cannot supply that join. Item 2 therefore stops
at that precise missing C01 input: no new files/tests/fixtures or semantic
allocation is proposed. SOURCE's separate C7 helper-shape inspection remains
its own C14 input, not a competing Core implementation investigation.

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
