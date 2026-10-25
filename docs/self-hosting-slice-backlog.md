# Self-Hosting Slice Backlog

Status: active coordination plan; no completion status in this file overrides
the governing phase roadmaps or evidence artifacts.

## Purpose

This backlog turns Gravity's self-hosting critical path into a finite,
dependency-ordered set of implementation slices. It is a coordination
projection of `D2`, `BOOT1`, `BOOT3`, `BOOT5`, `BOOT7`, `BOOT8`, `TEST13`, and
the Phase 15 and Phase 18 implementation roadmaps. It is not an additional
normative source document in the 240-document inventory.

Use the [finite milestone completion plan](self-hosting-milestone-completion-plan.md)
for the numbered SH-00 through SH-29 requirements, implementation owners,
validation starting points, composition boundaries and stopping rules. This
backlog remains the live status/evidence record; writing or validating the plan
does not promote any milestone or renew migration evidence.

## SH07 finish line and current outcome ownership

SH07 has one canonical endpoint: required bootstrap source forms pass the
owning source/expansion/resolution and Gravity lowering route to freshly
verified canonical Core, with applicable negative composition, provenance,
source/binding/order preservation and independently accepted scoped migration
evidence. Its [completion plan](self-hosting-sh07-completion-plan.md#one-sh07-finish-line)
records C01-C18 dispositions and accountable complete-outcome owners.

C01 derives the required source/import/generated-form/primitive population
from existing inventories, manifests and BOOT5 records now. It does not wait
for SH23-SH25 implementation, silently equate six selected files with the full
required population, or exclude a required construct to pass. Later compiler
builds consume accepted frontend revisions; they are not a circular prerequisite
for establishing those revisions. SH07 does not claim full C7-C10, MIR/runtime,
backend, package/driver/build completion, self-hosting or seed retirement.

Core owns the permissible authentic frontend/public-ingress outcome through
its adapters; SOURCE owns actual Core-to-C7 consumption and affected adoption;
Diagnostics owns stage-produced preservation joins and the assigned authenticated
envelope quota-removal producer; Cohort owns the assigned resolver quota-removal
producer and current applicability of the actual producer/consumer evidence. ROOT resolves genuine
shared ownership, routes independent acceptance and integrates main. A leaf
freeze does not leave its joins ownerless. Routine work inside unchanged
exclusive ownership needs no per-task ticket, prewrite notice or preparation
handoff; communicate actual conflicts/interface changes, material blockers and
review-ready candidates through existing records. Restricted C12 work and denied
review/oracle actions remain with their exact restrictions, not reassigned.

Milestone dependencies govern completion credit. Existing later managers
continue useful work against available interfaces without waiting for global
SH07 completion. No task-count, attempt, cost, budget or replacement accounting
gate governs that initiative. Reuse applicable execution evidence; source
association or a plan is not a fresh execution result.

## Master Coordinator Goal

Deus `01a172c9-3648-702c-b7ac-03dab5501018` is the sole coordinator for
goals, priorities, dependencies, review routing, integration decisions and user
reporting. Current ROOT references mean Deus; historical receipts keep their
original actor. Former coordinator `019f4993-78b9-7573-b154-139d87a127b5`
is a delegated local executor/integrator only. The supported bridge is
`01a17017-4dc2-75fd-b4d8-113d3092e01f`. Direct cloud-to-local transport is
not assumed. The old hourly coordinator automation is PAUSED, no replacement
is created, and Gravity Progress reporting remains unchanged.

Iterate on Gravity through the 30 smallest independently provable vertical
slices in this plan until the language is fully self-hosted, feature-complete
against its documented contracts, and able to compile to every documented
target with accepted and rejected behavior, stable diagnostics, reproducible
artifacts, and seedless public proof.

The first bounded goal is genuine seed retirement at `SH-29`: a public Gravity
compiler must compile its own compiler, runtime subset, standard-library
subset, and package/build path without the Clojure seed in the release
boundary. After `SH-29`, repeat the applicable target gates as a countable
target-expansion matrix until every documented target passes. The first honest
self-hosting claim therefore does not overclaim that every target is already
implemented.

Use three layers: one master coordinator, persistent standalone slice managers
with finite goals and self-curated work, and bounded disjoint subagents within
a manager's ownership. Preserve the writer -> independent auditor -> next-slice planner
pipeline without self-acceptance or a second scheduler. The coordinator alone
allocates shared files, routes independent acceptance, reconciles current main
and integrates every main PR. Managers freeze isolated local commits for that
review and independently discover and execute eligible owned work meanwhile.
Native/public-proof claims retain selected-candidate provenance and actual
machine safeguards; old proof histories create no ordinary-development
attempt budget or preparation-only gate.

Matt authorizes routine in-scope best judgment on implementation details,
local commits, proportionate tests, dependency/conflict fixes and priorities
without discretionary Matt escalation. Managers independently discover,
prioritize and execute work inside their standing ownership. The coordinator
handles actual conflicting ownership, incompatible contracts and main
integration, and routes independent acceptance. Fixed queue/task-count caps,
one-attempt grants, method-preparation-only staging, mandatory stop-after-ticket
rules and repeated discretionary pre-run approval are removed for ordinary
development, without a renamed replacement. Historical permission-only/
proposal-only/await-human labels create no current ordinary-work hold. Failed,
interrupted, skipped and never-launched attempts keep their actual status;
none is relabelled success or a never-launched consumed proof. Project cost
models, work-unit allowances, debit/reservation plumbing, cost proofs and
budget-exhaustion requirements are removed with their enforcing code,
contracts and budget-only tests. Do not rename these as generic bounds or keep
capacity-oracle node/depth/width/scalar/source-byte/token/form quotas merely to
refuse well-formed input. Authentication, semantic validity, tamper checks and
actual host/platform protections remain independently required. Independent
correctness acceptance still precedes integration. Platform/tool restrictions,
mandatory action-time approvals, destructive/security-sensitive actions,
new external commitments and unrelated scope retain their limits; never
approve an unknown action or bypass a restriction. Do not pause useful work
for adoption acknowledgment.

Accounting details in the retained implementation snapshots below describe
their recorded revisions, including text written in the present tense at that
time. Work totals, capacity constants, paid-operation formulas and exact/short
budget tests are historical evidence, not current requirements. Their removal
does not turn a failed run into success or establish an unmetered successor's
correctness. Continue validating the actual semantic, authentication and
tamper properties; no accounting proof is required before doing that work.

Gradually replace the monolithic bootstrap implementation with contract-aligned,
stage-owned namespaces:

- `gravity.reader`
- `gravity.syntax`
- `gravity.macro`
- `gravity.resolution`
- `gravity.checked-core`
- `gravity.types`
- `gravity.effects`
- `gravity.ownership`
- `gravity.safety`
- `gravity.mir`
- `gravity.optimization`
- `gravity.lowering`
- `gravity.diagnostics`
- `gravity.backend.c`
- `gravity.backend.llvm`
- `gravity.backend.wasm`
- `gravity.backend.jvm`
- `gravity.backend.js`
- `gravity.backend.mlir`
- `gravity.backend.gpu`
- `gravity.backend.hdl`
- `gravity.backend.workflow`
- `gravity.backend.query`
- `gravity.backend.mobile`
- `gravity.runtime`
- `gravity.standard-library`
- `gravity.package`
- `gravity.build`
- `gravity.tooling`
- `gravity.cli`
- `gravity.bootstrap`
- `gravity.release`

Assign each namespace an explicit contract boundary, public API, artifact
inputs and outputs, diagnostic ownership, tests, and dependency direction.
Keep compatibility entrypoints thin while implementation authority moves into
these namespaces. Permit parallel writers only when namespace ownership,
files, tests, and worktrees are genuinely disjoint. Serialize changes to the
shared CLI, diagnostic catalogs, integration manifests, coverage accounting,
completion attestations, and release boundaries.

Every integrated slice must identify its governing contracts, compile or emit
something real, reject something real, preserve provenance and deterministic
identity, pass proportionate regression and adversarial review, state its
unsupported boundaries honestly, and finish as a clean verified commit.

Do not claim target support, feature completeness, self-hosting, seed
retirement, or release readiness before the corresponding documented evidence
gates pass.

## Implementation progress is separate from migration

Use the three-boundary workflow in `docs/development-verification.md` for
ordinary SH-07 work: edit the Gravity owning pass, add accepted/rejected
fixtures, run focused semantic tests, and validate affected composition.
Report the behavior and checks as implementation progress even while the
slice remains partial. The uniform completion gate below is an admission and
completion gate, not an obligation to run every authority proof before each
local improvement. Stale authoritative evidence stays stale until an explicit
stable-module migration decision re-establishes it. Preparation documents,
review counts and scheduler changes are not executable progress.

### C6 compiled-source aggregate binding: development maintenance

The `00701` aggregate pin now includes the three declared compiler-identity
fields added to the raw stage0-seed plan. A read-only inspection reproduced both
the full current digest and, by diagnostic-only field removal, the retained old
digest; source, function, builder and verifier identities remain unchanged.
The focused original compile/validate test passed one test and 57 assertions at
`d34881c5`; its identical two-file patch and unchanged raw dependencies retain
that result after the maintenance-only split. Current boundary checks passed
six tests and 11 assertions; LOC passed for 2,021 files, zero exceptions and
maximum 250 physical lines. These are separate observations, not one run.

The earlier `04d8b22b` source attempt remains failed with zero passes, zero
failures and one error before all 212 semantic assertions. Its unvalidated C11
family and harness are held separately and are not merged here. Public C6
ingress still reaches the distinct live-expression-emitter binding in `00581`;
no bypass or renewal is claimed. The old B44 coverage pin is not renewed.
Current-emitter authentication, fresh C6/C11 composition, migration, bootstrap
and completion evidence remain separate.

### Explicit bounded borrow destinations: SH-10 development increment

The existing C9 normalized ownership state machine now recognizes the contract's
`:closure-capture` and `:global-or-static-storage` destinations alongside
`:function-return`. Each escape still requires an active borrow and a valid
explicit destination endpoint no greater than the owner's endpoint. Outliving
and inactive borrows reject with existing C9 diagnostics and event lineage;
unknown destinations and malformed endpoints fail closed. Both existing
scenario validators use the same destination restriction without broadening
their different acquisition rules.

The Gravity C9 implementation and accepted/rejected request fixtures execute in
three focused tests, passing 257 assertions at development commit `05a0b1f1`.
Four existing ownership-transition regressions passed 424 assertions; the
language-boundary checks passed six tests and 11 assertions. The Clojure LOC
validator passed for 2,021 source files with maximum 250 physical lines. These are
separate completed selections, not one whole-suite run.
Paired QST fixtures receive byte/reader parity only. The adapter tests validate
explicitly supplied records and source/result joins; they are not fresh C8
production or admission evidence. The direct workflow is documented in
`docs/development-verification.md`.

This checks explicit bounded intervals, not inferred source closure/global
lifetimes or a real storage provider. A false finite bound cannot justify
unbounded global storage. C10's read-only ingress does not consume these
lifecycle results, so C10/MIR escape propagation remains separate. SH-10 stays
partial; migration, self-hosting, release and milestone counts are unchanged.

### Exclusive owner writes: SH-10 development increment

C9 now analyzes normalized direct-owner `:write` events. Initialized, available
owned-mutable values accept writing only without active borrows; immutable or
mutable aliases reject with `C9-MUT-ALIAS` and the original event lineage.
Ending only one of several immutable borrows does not permit writing. Existing
moved, consumed and uninitialized errors retain precedence. Persistent values
remain read-only, and write events cannot name a borrowed-access or destination
field. Initialization remains an explicit separate operation.

The first owner-write draft `0f0e4c2f` was unrun. Successor `d7df09e6` uses only
existing binary nil checks rather than adding an unnecessary variadic-equality
requirement to this compiler client. Its three focused tests passed 129
assertions, executing Gravity fixture request constructors and the original
C9 analysis and verifier. Seven existing transition/escape regressions passed
681 assertions; six language-boundary tests passed 11 assertions. The Clojure
LOC validator passed for 2,021 files with maximum 250 physical lines. These
are separate completed selections, not a whole-suite result.
QST has byte/reader parity only. No physical storage
write or effectful source composition ran. Both pure-C8 scenario families
still reject writes; no mutation effect or authority is inferred from these
normalized requests. Requests without writes preserve their earlier scope;
new write scope is derived from current validated events.

The direct checks are listed in `docs/development-verification.md`. This is an
ownership-legality increment, not full SH-10, downstream C10/MIR propagation,
authoritative migration, native support or self-hosting completion.

### Standalone inferred String-Set comparison: development increment

C7, C8 and typed lowering now support standalone binary `=` and `not=` when
at least one operand has the exact inferred homogeneous String-Set carrier.
The other operand must independently establish the same carrier or a complete
finite all-String Set, including empty. Both normal domains are checked; Never
preserves transfer behavior without accepting a malformed normal counterpart.
Unknown cardinality and extent remain unknown. Bare/unknown/mixed-member types
do not gain support, and the unchanged finite/Vector domain helpers still
reject String-Set leaves inside Vectors.

Separate carrier comparison versions preserve complete source, binding,
constraint and fact joins without invented operand counts. `not=` retains its
original source identity and owned equality/branch complement. The unchanged
55-function runtime validates actual contents, uniqueness and worst-case work.
Focused checks cover once-only ordered evaluation, transfers, mixed old/new
ledger producers, exact quotas, coherent substitutions and callback-free
malformed source-ID rejection. Generic emitter guards do not restrict legal
Symbol name tables or all calls to binary shape.

The first focused epoch at `1c2427fb` failed: 25 tests completed with 7,916
passes, four failures and no errors; eleven selected variables were skipped.
All sixteen selected SH01 tests and all three new C7 tests passed. Four
old negative assertions still expected the now-supported homogeneous String
carrier to reject. Test-only successor `9d644b07` changes that negative and the
matching unrun inequality case to genuinely unsupported homogeneous Keyword
Sets. It does not change production or fixtures.

The second epoch completed eight tests with 1,244 passes, 56 failures and no
errors; four variables were skipped. Corrected C7 and four old C8 controls
passed. The new C8 composed fixture supplied Keyword node IDs to a graph
interface requiring SHA IDs, causing fourteen graph prerequisites and their
follow-on assertions to fail. Test-only successor `173709b1` applies the
existing graph fixture identity bridge and prints the rejected graph on a
prerequisite failure; production validation is unchanged. Its affected and
previously unrun five-variable batch passed 1,032 assertions.

Complete passing coverage is partitioned as 24 variables/7,751 assertions from
the first epoch, seven/1,084 from the second and five/1,032 from the third:
36 distinct variables/9,867 assertions. This is not one clean 36-test run.
Both failed aggregates, skipped tails and exact tested commits remain retained;
passing unchanged checks were not replayed. Real source composition is a
separate affected integration boundary, not implied by these normalized checks.

The one affected source invocation at `70e391e6` passed one test/102 assertions
(96 composition plus six existing loader checks), returning
`[true true false true true false true true]`. Its accepted G fixture has two
functions, three definitions, twenty calls, ten insertions, four carrier
equalities, two carrier inequalities and ten inert quoted Sets. One evaluated
module Set initializer runs before the single plan entry; four namespace reads
consume it. Original C7/C8 verification and assembly's public C7 gate pass.
Comparison nodes themselves remain pure; actual initializer and function
allocation remains visible in the correct namespace and function summaries.

The genuine `(set ["seed"])` private-entry negative reaches C7 and rejects
with `C7-ANNOTATION`/`:private-self-independent-entry-types-required`. Its
existing aggregate diagnostic still has nil source IDs/span; this is a known
separate diagnostic-preservation follow-up, not source-rich conformance. No
negative C8/runtime runs. Q has parser parity only; public fresh C6, seed
control and whole-B14 execution were not invoked.

Selected source execution took 162.790 seconds after 11.909 seconds of
namespace loading. Positive/negative C5 construction took 32.350/24.029 seconds;
fresh development C6 sessions took 45.886/36.265 seconds and same-catalog replay
4.113/3.121 seconds. Positive C7 construction/verification took 3.002/1.660
seconds, C8 construction/verification 1.442/0.130 seconds, assembly including
public C7 verification 3.717 seconds, and closed-plan execution 0.155 seconds.
These inclusive nested development observations separate construction from
loading; they are not isolated benchmarks or speedup evidence. The expensive
composition ran once, not again for notes or migration labels.

No new annotation grammar, declared-self expansion, whole-B14 execution,
broader collection equality, target support or authoritative migration follows.
SH-07 remains partial and the existing completion counts are unchanged.

### Qualified current-namespace var objects: development increment

The owning `gravity.checked-core` pass now lowers `(var demo.value/answer)`
and fully qualified same-namespace function targets to var-object Core nodes.
L2's reference-without-dereference/execution behavior and L3's qualified
namespace resolution are preserved, including lexical shadowing, upstream
binding identities, actual resolution order and `:var-target` source mapping.
Alias-qualified, foreign-namespace and lexical var targets remain rejected;
profile legality remains `:pending-sh09`. This does not extend runtime var
semantics, cross-module initialization, type/profile legality or target support.

Paired accepted/rejected fixtures and tests live in the existing SH-07-B5
fixture directory and `sh07_var_reference_test.clj`. The direct
`sh07-b5-qualified-var-development-kernel` executes the Gravity lowerer and
validators on explicitly non-authoritative normalized inputs. Separate tests
exercise real SH-06-to-SH-07 construction, source mapping, request membership
and tampered-product replay. The default commands are in
`docs/development-verification.md`.

Validation: seven focused kernel/cache/revision-guard tests passed 72
assertions; real qualified-var composition passed 26 assertions; authenticated
order/spelling/binding-id and replay mutations passed 18 assertions. The kernel
executed in 1.230 seconds after 6.970 seconds of namespace loading; the positive
composition executed in 127.284 seconds. These are development observations,
not benchmark or proof claims. The initial whole-B5 run was interrupted at ten
minutes without a test report and remains unverified. The full suite and full
fixture matrix were not rerun.

The ordinary adapter executes current source with actual byte/plan identities,
labels it `:development-unadmitted`, and rejects source/toolchain drift rather
than mislabeling an artifact. Historical nine-field migration pins are unchanged
and are enforced at explicit proof entrypoints and on the exact transaction
snapshot. Fresh stable-revision provenance/equivalence/bootstrap verification
and authoritative admission remain outstanding. SH-07 remains partial; no
completion, self-hosting, seed-retirement or release count changes follow.

### Qualified current-namespace mutation targets: development increment

`gravity.checked-core` now lowers `(set! demo.value/target value)` for
authenticated top-level vars/functions in the current namespace. It preserves
the actual SH-06 resolution order and binding identity, does not evaluate the
target, evaluates the RHS once before the write, and records the unit result.
The mutation target is consumed without emitting a target-expression node;
the RHS retains its own resolution and source-map emission, including when a
lexical binding shadows the qualified target's unqualified name.

The existing B6 fixture `qualified-set-target` is promoted to accepted behavior.
New foreign and alias-qualified fixtures retain `C6-LOWERING-GAP`; lexical,
unsupported-location and forged binding joins remain rejected. No public API,
schema, effect registry or migration fingerprint changes are introduced.
SH-06 resolution feeds the existing C6 Core/mutation/mapping products; later
type, effect/profile/capability, ownership and safety legality facts remain
pending. This is Core lowering support, not runtime mutation permission.

Five selected B6 tests passed 231 assertions: the direct Gravity kernel,
authentic qualified products/mapping, request and product tampering,
foreign/alias diagnostics, and the existing unqualified substitution regression.
The direct kernel executed in 1.199 seconds after 6.961 seconds of namespace
loading. Changed test-only mapping/diagnostic oracles were rechecked; passed
unchanged negative and forgery tests were not repeated. These are development
checks, not a full B6 namespace pass or authoritative migration evidence.
SH-07 remains partial and development-unadmitted. The owning tests are in
`sh07_mutation_test.clj`; use its exact
`sh07-b6-qualified-set-development-kernel` through the existing development
runner for the inner loop.

### Quoted-symbol match literals: development increment

`gravity.checked-core` now lowers exact quoted-symbol patterns such as
`'ready`, `'event/ready` and `'_`, both at the root and inside supported fixed
vectors. Quoted underscore is a literal, not a wildcard. The normalized symbol
is preserved in literal pattern records and root branch/node records; existing
vector values and source-child paths remain unchanged. Quote operators and
payloads retain consumed `:match-pattern` mapping rows anchored to the actual
match node, without becoming executable references, calls or bindings.

The existing SH-06 traversal and SH-07 projection walk valid quoted-pattern
branches in their enclosing scope. Actual vector binding symbols still create
branch-local bindings. Malformed quotes and quoted composites remain rejected
by C6; an unresolved executable branch is rejected by C5. Forged payloads,
substituted pattern products and removed/misclassified quote mapping rows are
covered by negative checks. No public API, product schema or migration pin
changes are introduced.

Use `sh07_match_lowering_test.clj` with the existing development runner:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-quoted-symbol-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-quoted-symbol-authentic-composition --exact sh07-b10-quoted-symbol-forgeries-fail-authentication-and-mapping --exact sh07-b10-quoted-symbol-rejections
```

The four selected kernel/composition/negative tests passed 308 assertions. The
cold kernel executed in 1.337 seconds after 7.487 seconds of namespace loading;
the grouped checks executed in 314.985 seconds after 6.990 seconds of loading.
These are development observations, not performance or proof claims. Earlier
test-harness failures and an interrupted group do not count as passing checks.
The full B10 namespace and authoritative migration were not run. Exhaustiveness,
type narrowing, later legality, runtime matching and SH-07 completion remain
outstanding; this revision is development-unadmitted.

### Recursive keyword-keyed map patterns: development increment

`gravity.checked-core` now lowers keyword-keyed map-shape patterns recursively
with existing fixed vectors, scalar/quoted-symbol literals, wildcards and unique
branch bindings. This includes empty maps, maps in vectors, vectors in maps,
multiple bindings and branch-tail `recur`. The two formerly rejected map cases
are promoted to accepted fixtures in the existing SH-07-B11 family.

Map records retain the actual map value and nil `:vector-width`; only value
patterns create child records. Paths use odd flattened source-child coordinates
(`2i+1`), while key data at `2i` has consumed mapping rows joined to its exact
owning map. These coordinates follow authenticated semantic map traversal, not
a textual insertion-order promise. Map keys do not create references, calls or
bindings. Existing limits are unchanged: 256 entries may have 512 source
children; 257 entries reject. Closed producer, resolver, parent/completeness,
source-map and host-coherence checks include the new `:map` variant without
changing schema 16's record keys or migration pins.

Six focused tests passed 487 assertions: the compiled-Gravity kernel, fixture
pair census, authentic recursive products/mapping, coherent and malformed
forgeries, promoted cases/tail-recur/vector regression, and six source
rejections. Unsupported keys, guards, other composites and duplicate bindings remain
rejected; escaped bindings fail in C5. Malformed map values fail verification
without a host exception. The source-composition group executed in 704.105
seconds after 7.300 seconds of namespace loading; these observations are not
benchmark or proof claims. An interrupted pre-fix group is not passing evidence.

For the inner loop, select `sh07-b11-keyword-map-development-kernel` in
`gravity.self-hosting.sh07-vector-pattern-test` through `-M:dev-test`. Separate
exact source tests are named `sh07-b11-keyword-map-authentic-composition`,
`sh07-b11-keyword-map-forgeries-fail-closed`,
`sh07-b11-keyword-map-promotions-and-vector-regression` and
`sh07-b11-keyword-map-source-rejections`.

This is lowering support, not a runtime extra-key policy, exhaustiveness or
type-narrowing proof. Later legality, runtime matching, authoritative migration
and SH-07 completion remain outstanding. The revision is development-unadmitted;
no target, self-hosting, seed-retirement or release claims follow.

### Irrefutable root match clauses: development increment

`gravity.checked-core` rejects a clause after a successfully lowered unguarded
root wildcard or binding pattern. The structured diagnostic is `C6-VERIFY`
with semantic rule `L7-UNREACHABLE` and reason
`:match-clause-after-irrefutable-pattern`. It identifies the offending pattern
and earlier blocking clause by their actual form/syntax IDs, ordinal, kind and
available semantic source span. Existing projected descendants share their
enclosing root span; this increment does not invent more precise leaf spans.

A terminal root catchall remains supported. Nested map/vector bindings are
conditional patterns, and quoted underscore is a literal, not a wildcard.
The sibling-binding fixture now uses conditional vector/map patterns and
checks exactly two distinct branch bindings, scopes and reference joins.
Clause, node, branch-record and hosted coherence checks reject forged
nonterminal catchalls without changing product keys, schema 16 or migration pins.

Reader/form-shape and C5 authentication/resolution still precede C6. Within C6,
outer shape and scrutinee errors, then errors in the earlier clause's pattern
or body, retain priority. Only after that clause succeeds does its catchall
block the later clause before later C6 pattern/body analysis. An unresolved
name in a dead-looking branch still fails C5 rather than being silently dropped.

Use the exact `sh07-b10-irrefutable-clause-development-kernel` selector in
`gravity.self-hosting.sh07-match-lowering-test` for the inner loop. The separate
`sh07-b10-irrefutable-authentic-composition-and-regression`,
`sh07-b10-irrefutable-forgeries-fail-closed` and
`sh07-b10-irrefutable-source-diagnostics-and-priority` selectors exercise the
affected authenticated composition; the existing sibling-scope test covers the
changed fixture. Full literal/structural subsumption, guards, exhaustiveness,
type narrowing, runtime matching and authoritative migration remain outstanding.
This revision is development-unadmitted, not SH-07 or self-hosting completion.

Six selected tests passed 288 assertions: kernel and paired-fixture census
(145), authentic terminal/nested-pattern composition (30), coherent forgeries
(6), six source diagnostic/priority cases (77), and exact sibling binding/scope
joins (30). The kernel executed in 1.195 seconds after 6.992 seconds of namespace
loading; the composition group executed in 775.704 seconds after 7.231 seconds
of loading. These are development observations, not benchmark or proof claims.
Earlier test syntax errors and the unsupported-operation kernel failure are
not passing evidence. The full B10 namespace and migration proofs were not run.

### Root keyword-map guards: development increment

`gravity.checked-core` lowers a root keyword-map pattern's `:when` value as an
executable expression. Structural pattern bindings are visible in both guard
and body. C5 resolves guard references before body references in that same
branch scope, without changing the original flattened map child coordinates.
The `:when` key remains consumed pattern structure; its value and executable
descendants receive normal emitted source mappings.

The Core records describe scrutinee-once evaluation, structural failure skipping
guard/body, guard-once evaluation after structural success, Boolean true selecting
the body, and Boolean false continuing to the next candidate. Guard children are
explicitly indexed after body children, not added to unconditional evaluation.
Guard expressions are not tail positions; branch bodies retain their enclosing
tail target. Calls, mutation, throws, nested conditionals and typed handlers keep
their operation and conditional-region facts. This preserves facts for later
checking; it does not grant effect, profile, capability or safety legality.

Guarded products use the closed nested `:sh07-root-map-guard-v1` rule. Node
attributes contain source IDs/indexes, while actual guard node references are
explicitly resolved in branch records. Unguarded shapes, top-level schema 16,
public APIs and historical migration pins are unchanged. Source joins use the
actual authenticated child-form vector, not an assumed map iteration order.
The existing 256-entry map limit includes `:when`: 255 structural entries plus
the directive fit; 256 plus the directive do not.

Nested structural-map guards remain unsupported; ordinary nested match
expressions in a guard or body remain supported. The existing deferred-guard
fixtures now exercise that nested structural boundary. False, nil and other
guard expressions are retained at this lowering boundary; retention does not
establish typing legality. L7 now requires normally returning guards to produce
Bool: true selects the body, false tries the next clause, and no implicit
truthiness conversion is allowed. Non-returning guards preserve their control
transfer. The `L7-GUARD-TYPE` check and later type/effect legality remain
implementation work; the existing C6 fixture retaining nil is not evidence
that nil is a legal typed guard.
Current C7 match typing, runtime matching, exhaustiveness and authoritative
migration remain incomplete. No SH-07, target or self-hosting completion follows.

Use `sh07-b10-root-map-guard-development-kernel` in the existing
`gravity.self-hosting.sh07-match-lowering-test` namespace for the inner loop.
The separate `sh07-b10-root-map-guard-authentic-composition` and
`sh07-b10-root-map-guard-source-rejections` selectors cover affected source
composition and real rejected programs. They use the existing development runner;
no new lane, wrapper, authority receipt or admission prerequisite is introduced.

Three guard-specific tests passed 453 assertions: the compiled-Gravity kernel
(337), authentic composition (84), and four rejected source programs (32).
The source cases reject unresolved guard references and escaped bindings in C5,
and non-tail guard `recur` and nested structural guards in C6, with structured
diagnostics rather than raw host exceptions. Two legacy map/catchall kernels
also passed 310 assertions. Authentic composition executed in 139.673 seconds;
this is a development observation, not a benchmark or proof claim. The earlier
75-assertion composition result predates the conditional/handler guard-parent
correction; the final 84-assertion run covers that correction and new source
cases. An unsupported-operation kernel failure was corrected before these
passing checks. The selected source runs used `.gravity`; paired `.qst` text
equality was checked without claiming a second full composition run. Full
namespaces, runtime matching and authoritative migration were not run.

### Atomic function annotations: development increment

The ordinary C5 -> C6 -> C7 path now carries fixed-arity, single-body function
annotations such as `(defn identity-i64 [x :- I64] :- I64 x)` and checks typed
calls between such functions. C5 resolves annotation names in the type/protocol
namespace before expression-binding precedence; a lexical parameter named I64
does not capture the I64 type annotation. C6 derives concrete type IDs from the
authenticated core type binding, not its metatype or an unverified spelling.

The bounded declaration family is I8/I16/I32/I64, U8/U16/U32/U64, F32/F64,
Bool, String, Symbol, Keyword and Ratio. It does not introduce implicit numeric
coercion: a generic integer literal is not an I64 value merely because a
parameter or result is declared I64. Composite types, protocols as parameter
types, generics, dynamic boundaries and casts remain outside this increment.

Owning code is `gravity.resolution`, `gravity.checked-core` and
`gravity.compiler.c7-type-checker-engine`. The existing Clojure traversal and
projection entrypoints retain the real source coordinates and binding IDs.
Annotation markers and type leaves are consumed declaration syntax, not
parameters or executable expressions. Annotated functions carry a closed
optional `:sh07-atomic-function-signature-v1` signature and a versioned
declaration mapping; unannotated attribute shapes remain unchanged.

C7 seeds declared parameter and return types before fixed-point inference and
preserves them through existing recursive/higher-order completion. Declarations
appear in canonical typed function products, equality constraints and the
annotated typed identity domain. Unknown/var-only type names belong to C5,
malformed or unsupported annotation forms to C6, and incompatible argument or
result types to C7. Later effect, profile, capability, ownership and safety
legality is not implied by type acceptance.

Use the existing `sh07-b47-function-call-recursion-test` lowering kernel and
`sh08-function-call-type-test` inference kernel for the inner loop. The
`sh08-atomic-signature-authenticated-source-composition` selector exercises the
affected reader/macro/C5/C6/C7 composition and result verification on the paired
I64 identity/forwarding, Bool call and type-name-shadowing source fixture.
Its paired run passed 38 assertions. Three source-negative tests passed 73
assertions on eight real programs: unknown and var-only type names, dangling,
duplicate and composite annotation forms, a String result/argument against I64,
and a generic integer argument against I64. Their `.qst` text equality was
checked; this does not claim a second full negative composition run. Two
existing normalized acyclic-call and recursive fixed-point regressions passed
52 assertions. Final direct C6/C7 kernels passed 46/41 assertions, including
legacy malformed-header diagnostic priority, mixed annotated/unannotated
parameters and stale declaration/mapping rejection. C5/source-tool identity
kernels passed 17 assertions and the repository-language boundary passed five.
These five groups total 11 tests and 272 assertions. Earlier failed development
attempts are not counted as passing evidence.

The affected C8 pure-function adapter derives the annotated identity domain from
the actual declaration-bearing function records, preserving higher-order and
legacy precedence and exact identity/typed-core equality. Its compiled-Gravity
adapter kernel and existing pure-function regression passed 47 assertions;
the same run repeated the five-assertion language-boundary gate. These are
normalized adapter inputs, not another authentic C7 source proof. Declaration
and constraint preservation, stale-domain substitution, stripped declarations,
malformed carriers and result tampering are covered. The narrower effectful
compiler-read adapter remains unsupported for annotated functions. Independent
semantic review accepted this bounded increment; none of these development
checks is migration evidence.

Current-source C5 execution records actual source/plan identities under one
operation-local binding, rejects source/toolchain drift and remains
development-unadmitted. Private cache keys are not transported as semantic
artifact fields. Complete historical SOURCE/migration pins remain enforced at
their explicit boundary. The existing seed compiles one module function table;
general imported stage-function composition remains a separate implementation
task, not a reason to add a facade-only `gravity.types` namespace. No native,
target-completion, self-hosting or seed-retirement claim is made here.

### Boolean match typing: development increment

The ordinary `:meta/:jvm` C7 function path now types unguarded matches over an
independently established Bool scrutinee, using the actual C6 branch, pattern
and decision skeleton records. Supported root patterns are `true`, `false`, `_` and a
lexical binding. For example:

```clojure
(defn select-value [b :- Bool x :- I64 y :- I64] :- I64
  (match b true x false y))
(defn retain-bool [b :- Bool] :- Bool
  (match b true true remainder remainder))
```

Source-ordered residual domains start at `[true false]`. A literal consumes its
case; a wildcard or binding consumes the remaining cases. A branch binding has
Bool type, its actual lexical scope, and the residual value domain (for example
`[false]` for `remainder` above). Pattern shape does not supply a circular Bool
premise for an otherwise unknown scrutinee. Every reachable branch is typed;
the supported result join requires equal established types and preserves
declared function-result checks. No implicit numeric conversion or union is
introduced.

`L7-PATTERN-TYPE` diagnoses a pattern incompatible with a known Bool scrutinee;
`L7-UNREACHABLE` diagnoses an empty residual branch. At the direct C7 boundary,
a missing case in `:formal` without supported partiality evidence is
`L7-NONEXHAUSTIVE`. The current source C6 module boundary accepts only
`:meta/:jvm`: the real formal fixture stops there with `C6-VERIFY` before C7.
It does not establish formal-profile source composition. Outside mandatory
total contexts, partial matching remains explicitly unsupported with
`C7-ANNOTATION`, not an invented nil result or a universal language totality
rule. Unknown scrutinees and unsupported undeclared heterogeneous joins also
request annotations; a branch conflicting with an actual declared result is
`C7-TYPE-MISMATCH`. Upstream scope and irrefutable-pattern diagnostics remain
owned by C5/C6. Malformed C6-product joins are `C7-VERIFY`.

Owning implementation is `gravity.compiler.c7-type-checker-engine`, with the
existing `sh08-function-call-type-test` development selectors. C7 compares the
raw match families with C6's actual canonical preimage and checks source
classification, child order, global ordinals, pattern coordinates, binding
scope and branch-contained uses. Coverage, narrowing, branch-result premises,
origins, dependencies and invalidation inputs are retained in the existing
hashed constraint ledger and type table. Public APIs, outer artifact keys and
existing typed identity domains are unchanged. The pure C8 adapter preserves
the complete typed core and ledger; no effect policy is expanded.

The `sh08-boolean-match-development-kernel` passed 47 assertions; atomic
annotation and existing call regressions passed another 62. A normalized C8
ledger identity/verifier oracle passed eight, for 117 in the final cheap group.
The authentic `sh08-boolean-match-authenticated-source-composition` passed 32 assertions on
both source extensions, including I64 selection, residual Bool binding,
wildcard coverage, forwarding, fresh C7 verification and C8 preservation with
rewrite rejection. Its execution took 189.949 seconds, a development
observation rather than a benchmark. Subsequent ordinal/use-site hardening and
the undeclared-join diagnostic correction are covered by the final cheap
kernel; independent review confirmed the accepted fixture paths unchanged.
The ledger tag was then corrected to the existing `:equality` constraint kind,
retaining the versioned match producer and all premises. The paired source run
predates that one-field payload change: its typed artifact identities are not
the final identities. The final cheap group covers emitted equality facts and
generic C8 preservation/identity/rewrite checks; it is not a new source proof.
Earlier failed draft checks are not counted as passing evidence.

The first real rejection run passed 41 assertions on declared-result mismatch,
wrong pattern type, duplicate and subsumed clauses, then errored on that
upstream formal-profile boundary. It also passed the existing recursive
fixed-point regression (31) and language-boundary gate (5): 77 passed assertions
and one error, not an all-passing group. The remaining source cases were not
executed by that run. The separate boundary-closure selector then passed 24
assertions on the remaining partial/unknown cases, C5 scope escape and the
formal fixture's explicit C6 rejection. The formal fixture was re-evaluated
under that corrected expectation; the first four cases and accepted pair were
not repeated. Source-negative `.qst` text equality was checked without claiming
a second full negative composition. Broader C6 profile support requires its
own executable composition work; this increment does not weaken its boundary.

Guards, structured/union pattern typing, general subtyping and joins,
partiality-proof ingestion, recursive/higher-order match composition,
ownership/effect completion and runtime/backend equivalence remain outside
this Boolean-only increment. The structural increment below supplies
constructor-derived key/element evidence for map/vector bindings; it does not
itself type guards. The guard increment below adds the bounded Boolean case.
These development results are not authoritative migration or release evidence.

### Constructor-derived structural match typing: development increment

The C7 function path now derives recursive fixed-vector and keyword-map shapes
from actual C6 collection constructors and immutable `let` initializer links.
Map patterns select a subset of keys: extra constructor keys are retained,
not rejected. A present key whose value is nil is distinct from an absent key.
For example:

```clojure
(defn choose [flag :- Bool value :- I64 fallback :- I64] :- I64
  (let [packet {:extra :retained :payload [flag {:value value}]}]
    (match packet
      {:payload [true {:value item}]} item
      _ fallback)))
```

`item` receives the established I64 type of the actual value child, its C5
binding/scope identity, and separate pattern and constructor paths. Source
syntax stored in `:source-value` is not evaluated or used as field-value
evidence. Key literals, value-node types, constructor child order, lexical
initializer positions and binding uses supply the facts. Existing established
scalar-call types are retained; pattern shape does not manufacture them.

Ordered residual shapes preserve earlier failed tests. In particular, patterns
`{:a true :b true}`, `{:a true}`, `{:b true}`, `_` over two independent Bool
fields select TT, TF, FT and FF respectively, without overlapping or dropping
cases. Binding/wildcard leaves and supported same-type scalar literal tests
compose with nested maps/vectors. The result join still requires equal
established branch types. A declared return expectation reaches a nested match
only through validated final `let` result links, not through initializers or
arbitrary expressions.

The owner remains `gravity.compiler.c7-type-checker-engine`; focused selectors
remain in `gravity.self-hosting.sh08-function-call-type-test`:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-structural-match-shape-development-kernel \
  --exact sh08-structural-match-inference-development-kernel
```

`sh08-structural-match-c8-ledger-composition` is a separate, costly public C8
bundle check, not an inner-loop kernel.

Run `sh08-structural-match-authenticated-source-composition` when changing the
C5/C6-to-C7 composition or C8 handoff. It uses the existing accepted
`function-structural-matches` `.gravity`/`.qst` pair, including forwarding and
present-nil cases. The public C7 interface and outer artifact fields are
unchanged. Versioned structural premises, extraction facts, residual shapes,
source identities, dependencies and invalidation conditions use the existing
hashed type table and equality-constraint ledger. C8 preserves that complete
ledger; no C8 effect policy or production adapter is expanded.

The accepted pair passed 48 assertions, including fresh C7 verification and C8
shape/key/extraction rewrite rejection, in 191.877 seconds of test execution.
That is a development cost observation, not a benchmark or migration proof.
The source run precedes numeric-pattern diagnostic tightening and the root-vector
join correction described below; its map-root Bool tests, I64 binding extraction
and nil test do not take those corrected cases. Focused kernels cover those
later changes. This is not a source run of the final compiler bytes; the
expensive accepted pair is not repeated solely to refresh its receipt.

The first real rejection/regression group completed with 77 passed assertions
and one failure, not an all-passing result. Field-type mismatch, missing key,
declared result mismatch, unsupported function-return shapes and computed
keys, C5 scope escape, the existing recursive fixed-point regression and the
language-boundary gate were exercised. The vector-width case exposed a C7
join that compared the branch's original source vector with the vector
pattern record's intentionally nil value field; it reported `C7-VERIFY`
instead of `L7-UNREACHABLE`. That failure is retained rather than relabeled.
The corrected join compares the branch value with its authenticated source
form while retaining the vector record's nil/width/child checks. The isolated
`sh08-structural-match-source-vector-width-rejection` then passed six assertions;
the six already-correct source cases and accepted pair were not repeated.
The final current cheap group passed six tests and 173 assertions: shape (23),
inference and record joins (30), C8 preservation (11), Boolean matches (47),
atomic annotations (41), and legacy function chains (21). It includes the
root-vector join, explicit unsupported numeric elaboration, and direct
meta/formal partiality diagnostics. The formal kernel is not formal-profile
source composition.

Computed map keys, collection-shape propagation through function returns or
pattern-bound collection aliases, mutable aliases, generic or external shape
validation, numeric literal elaboration,
guards, general unions/subtyping, partiality proofs, recursive/higher-order
match composition, ownership/effect completion and runtime/backend equivalence
remain unsupported here. Numeric elaboration being unimplemented is not a
claim that an unsuffixed numeric pattern is fundamentally incompatible with a
fixed-width numeric value. Occurrence-specific decimal source descriptors are
not compared as numeric values. Existing shape/depth/residual/evidence bounds
produce explicit implementation-limit diagnostics, not new language legality
rules or silently increased limits. Source composition remains `:meta/:jvm`;
formal-source support still stops at the unchanged C6 boundary. Authoritative
migration, full SH-08, target support and release status do not advance from
these development checks.

### Boolean root-map guard typing: development increment

C7 now types normally returning guards on supported constructor-derived root
map patterns under L7's Boolean rule. For example:

```clojure
(defn permit [flag :- Bool] :- Bool flag)
(defn choose [flag :- Bool value :- I64 fallback :- I64] :- I64
  (match {:ok flag :value value}
    {:ok ok :value item :when (permit ok)} item
    {:value again :when false} again
    _ fallback))
```

The guard result must be established Bool. There is no `if`-style truthiness:
normally returning nil, integer, String and unknown values produce
`L7-GUARD-TYPE` at the guard. Bindings are available for the guard and its own
body, not sibling clauses or outer code. C7 authenticates C6's distinct
source-only, clause and branch guard projections, actual `:when` source slot,
guard node, conditional region and call membership. Match children contain
the scrutinee, all branch bodies, then guard nodes; result inference joins
only the validated body indexes, so Bool guards do not contaminate I64 results.

Structural failure skips the guard and body in the control-flow evidence.
The guard is recorded once after structural success, with true selecting the
body and false continuing. A Bool-returning call is not proof that the guard
is true: possible false fallthrough retains the incoming cases and requires
fallback coverage or another supported totality argument. A literal true can
consume the structurally matched cases; literal false has no selected body
cases and retains fallthrough. Its body is still type-checked, including
declared-result checks. This does not add a dead-code typing exception.

The existing equality ledger retains the guard premise, source, type, truth,
structural/body/fallthrough shapes, actual binding identities, ordinary call
records and evaluation regions. Guard inputs appear in the owning constraint's
dependencies, solution and invalidation fields. Preserved effect/capability
declarations remain declared inputs, not inferred empty-effect proofs. All
ordinary call facts survive the C8 handoff. No C8 effect policy or production
adapter, public C7 API, outer artifact schema, or authority pin is changed.

The owner remains `gravity.compiler.c7-type-checker-engine`. Use the existing
function-call test namespace for fast guard feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-root-map-guard-development-kernel \
  --exact sh08-root-map-guard-literal-development-kernel
```

Use `sh08-root-map-guard-c8-composition` separately when its C8 handoff is
affected; it constructs the public C8 bundle.

The source-ready group passed seven tests and 198 assertions, including
structural, Boolean, atomic-signature and legacy-call regressions. The actual
`sh08-root-map-guard-authenticated-source-composition` then passed 94 assertions
on the `function-root-map-guards` `.gravity`/`.qst` pair. It covers two guards
in one three-body match, I64/Bool separation, false continuation, a binding
used in both guard and body, literal-true total coverage, fresh C7 verification,
and complete C8 ledger/call preservation with rewrite rejection. Its 185.299
seconds of test execution is a development observation, not a benchmark.
Earlier failed draft checks are not acceptance evidence.

The real rejection/regression group passed four tests and 93 assertions:
normally returning nil/integer/String/unknown guards (32), guarded partiality,
false-guard body mismatch, unsupported throw and C5 scope escape (25), the
existing recursive fixed-point regression (31), and the language boundary (5).
Guard, body and match diagnostics are checked against actual core/syntax IDs,
spans and origins. Negative `.qst` fixtures were checked for equal source text,
not counted as a second full negative composition. The compiler remained
unchanged across the final 198-assertion kernels, accepted pair and real
rejections. Use `sh08-root-map-guard-source-type-rejections` and
`sh08-root-map-guard-source-boundary-rejections` for affected source-diagnostic
composition; do not repeat unrelated source construction for a local helper edit.

Nested structural guards, shape propagation through pattern-bound collection
aliases, non-returning/throw/try guard composition, general effect and ownership
legality, recursive/higher-order guard inference, runtime/backend equivalence
and authoritative migration remain outside this increment. Unmodeled guard
control transfer is explicitly unsupported, not assigned a fictitious Bool or
Never type. Direct formal partiality kernels do not extend C6's unchanged
`:meta/:jvm` source boundary. No full SH-08, self-hosting, target, seed-retirement
or release claim follows from these development results.

### Pattern-bound collection aliases: development increment

The C7 implementation extends constructor-derived map/vector evidence through
pattern bindings and authenticated immutable `let` links. An extracted
collection can be the scrutinee of another match in its own guard or body:

```clojure
(defn extract [flag :- Bool value :- I64] :- I64
  (match {:payload [flag {:value value}]}
    {:payload packet}
    (match packet
      [true {:value item}] item
      [false {:value item}] item)))
```

The implementation retains actual binding IDs, owner match/clause, lexical
scope, source uses, constructor IDs and extraction paths. It carries all whole
selected cases, not a first/last case or independently widened fields.
Reconstructing a collection from several extracted values joins their case
origins: shared origins must agree; independent origins form a bounded product.
The focused correlation oracle retains exactly TF/FT residual cases, excludes
impossible TT/FF combinations, and checks a four-case independent product.

Guard typing uses structural extraction premises before guard truth is known.
Literal-false guard bodies remain type-checked, including their nested aliases.
Shape state is derived afresh within each invocation and participates in the
existing inference fixed point. The canonical ledger, dependency and
invalidation facts retain the ancestry and correlated-construction premises;
public request fields and typed identity domains are unchanged. Size/product
overflow produces an explicit implementation-limit diagnostic at the alias
use, replacing prior evidence instead of silently retaining stale facts.

Use the existing function-call test namespace, without an authority refresh:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-pattern-alias-development-kernel \
  --exact sh08-pattern-alias-correlations-development-kernel \
  --exact sh08-pattern-alias-limit-development-kernel
```

`sh08-pattern-alias-c8-composition` is the separate public C8 bundle check.

The final focused group passed 11 tests and 241 assertions, including selected
structural, Boolean, guard, signature, call-chain and C8 regressions. The
initial real source pair passed its C7 checks but failed four C8 assertions:
the fixture had no ordinary call, while the existing C8 function-call adapter
requires nonempty call facts. This remains failed evidence. The corrected
fixture adds a real typed forwarding call; the C8 production adapter and its
acceptance policy are not weakened. That changed `.gravity`/`.qst` pair passed
124 assertions through actual C5/C6 products, fresh C7 verification and C8
preservation. It covers vector/map extraction, repeated immutable forwarding,
guard/body use, literal-false body typing, whole residual cases and correlated
reconstruction, distinct same-spelled binding IDs, and rejection of forged or
dropped ancestry. Test execution took 257.615 seconds; this is a development
observation, not a benchmark or migration proof. The changed source rejection
group then passed 37 assertions for incompatible nested patterns, absent keys,
wrong vector widths, declared-result mismatches, literal-false guard body
mismatches and escaped bindings. Diagnostics retain actual nested-match or
pattern syntax, spans and origins; negative `.qst` files were checked for equal
source text, not counted as a second full negative composition. The existing
recursive fixed-point regression (31 assertions) and language boundary (5)
passed in the same three-test, 73-assertion run. Compiler and test bytes were
unchanged across the final 241-assertion kernels, corrected accepted pair and
source rejections. Use `sh08-pattern-alias-authenticated-source-composition`
and `sh08-pattern-alias-source-rejections` only when that source composition
or its diagnostics are affected; the fast kernels remain the edit loop.

Computed, external, function-return and mutable collection shapes without
constructor evidence remain unsupported. General union/subtyping, recursive
or higher-order alias inference, effect/ownership legality, runtime/backend
equivalence and authoritative migration are not claimed. The source boundary
remains `:meta/:jvm`; no full SH-08, self-hosting, seed-retirement or release
status follows from this development increment.

### Recursive quoted data: development increment

C7's existing function path now derives types for L1/L2's inert quoted data:
nil, Bool, normalized exact integer, character, String, Keyword, Symbol, list,
vector, map and set, including
empty containers and recursively nested supported data. A quote inside an
outer quote remains an ordinary data list. For example:

```clojure
(defn token [] :- Symbol 'unresolved-data-name)
(defn inert-data [] '(throw unresolved-data-name))
(defn extract-token [] :- Symbol
  (match '[true {:value ready}]
    [flag {:value result}] result))
```

Payload symbols do not resolve, and payload lists do not become calls, bindings
or thrown-error effects. The checker authenticates the actual quote owner,
operator, two source children, payload parent/form/syntax/kind/value joins,
recursive child order and cardinality, and zero executable children/bindings
with `:no-evaluation`. A source-form collision with an executable node is
rejected. Quoted list types use the existing collection representation style:
`{:kind :gravity.type/list :element-types [...] :shape :literal}`. Types retain
heterogeneous members rather than widening them or inserting coercions.

Quoted map/vector shapes carry an explicit DATA-origin variant: the real quote
Core node plus actual payload form/syntax IDs and data paths. Payload form IDs
are not fabricated executable Core IDs. Extraction, immutable forwarding,
pattern aliases and correlated reconstruction retain these origins in the
existing canonical ledger, dependencies and invalidation. Quoted Boolean
guards retain their true/false meaning; other normally returning data must
still satisfy the Bool guard rule. Public C7 inputs and identity domains and
the C8 production adapter are unchanged.

Traversal has a shared payload-node-visit budget. Successful and failed
traversals charge consumed visits, including parent nodes and prior siblings;
preflight/collision failures retain the completed traversal count. This is not
a count of all VM operations, repeated shape queries or allocated bytes. A
small-budget oracle checks that a late three-node failure exhausts budget
three, budget four permits the next scalar, and collision rejection still
charges the three visits. Bounds produce explicit implementation-limit
diagnostics rather than silently accepting missing facts.

Use the existing focused runner and namespace:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-quoted-data-recursive-development-kernel \
  --exact sh08-quoted-data-authentication-development-kernel \
  --exact sh08-quoted-data-structural-development-kernel \
  --exact sh08-quoted-data-guard-development-kernel
```

Run `sh08-quoted-data-c8-composition` separately for affected C8 integration;
it is not a cheap quoted-data kernel.

The stable focused group passed 17 tests and 399 assertions, including selected
atomic, structural, alias, guard, Boolean and call-chain regressions. Independent
static review closed the failed-traversal accounting finding. The actual
`function-recursive-quoted-data` `.gravity`/`.qst` pair then passed 144 assertions:
nine functions, ten quote nodes, all supported data families and empty
containers, inert throw/unresolved-name data, real DATA-origin links, Symbol
extraction, immutable/pattern aliases, quoted-false guard and nonkeyword map
data typing. A real forwarding call exercises fresh C7 verification and C8
preservation, with request/fact tampering rejected. Test execution took 224.901
seconds; this is a development observation, not a benchmark.

The changed real rejection group passed 42 assertions: Symbol/String and
generic-integer/I64 result mismatches, quoted-list guard typing, unsupported
decimal representation, nonkeyword structural map key, malformed quote arity
in C6 and unquoted unresolved name in C5. Diagnostics retain the actual quote,
payload or guard syntax, spans and origins. Rejected `.qst` files were checked
for equal paired text, not counted as another source composition. Ratio,
deferred-integer and tagged representations and quoted String/nil guards have
focused-kernel coverage, not additional real rejection runs. The existing
recursive fixed-point regression (31) and language boundary (5) passed in the
same three-test, 78-assertion invocation. Compiler/test bytes were unchanged
across the final kernels, accepted pair and rejection group. Use
`sh08-quoted-data-authenticated-source-composition` and
`sh08-quoted-data-source-rejections` for affected source composition, not as
the default edit loop. None of these checks confers authoritative migration
evidence.

Quoted integers retain generic integer type, not implicit I64 conversion.
Decimal/ratio/deferred-integer and tagged representations remain explicit
implementation limitations. Structural map matching still requires keyword
keys; other supported keys may occur in typed quoted map data. General scalar
root matching, numeric pattern elaboration, list patterns, union/subtyping,
error/import semantics, runtime/backend equivalence and authoritative migration
remain outside this increment. Source composition remains `:meta/:jvm`; no
full SH-08, self-hosting, target, seed-retirement or release claim follows.

### Canonical recursive quoted-DATA lowering: development implementation

The current Gravity emitter now consumes C7's existing recursive quoted-DATA
producer for all eleven supported kinds, rather than only Symbol. Nil, Bool,
represented exact integer, character, String, Keyword, Symbol, list, vector,
map and set lower to the existing `:quote` instruction. Empty and mixed nested
containers, qualified symbols, symbol map keys and nested quote lists remain
DATA. There is no unquote, payload name resolution or execution of a quoted
call/throw. The runtime representation and quote operation are unchanged.

The public typed operation still freshly verifies the same request and
candidate through current C7 before lowering. Its context joins the verified
inputs; the lowerer selects one exact producer/quote ID and checks shallow
owner, operator, payload, source, kind and root-origin coordinates. Recursive
shape, value, type and source authentication remain owned by C7. The projection
returns the recorded value directly instead of duplicating that checker.
Its logical charge remains four with the same cumulative cap. The dispatcher
also avoids hashing a composite type merely to reject nil/unknown types.
Neither fact is a total VM-work, memory or performance claim.

The focused batch passed five tests/264 assertions: all eleven kinds and empty
containers, shallow join rejection, budget three versus four, an opaque
no-recursive-inspection sentinel, and existing catch, raw-source, typed-input
and public orchestration checks. These tests consume normalized verified-context
records, not authentic source proof. The final test-only change removes five
unchanged source-rejection rebuilds from the then-unrun affected selector;
the focused bodies and compiler/fixture bytes are unchanged, so no focused
replay is attributed to that selector change.

The new complete `local-runtime-quoted-data` paired fixture contains sixteen
lowerable functions, including all supported quote roots, inert throw and
nested quote lists, let forwarding and direct same-module forwarding. The
affected check constructs each C5/C6/C7 input once, uses representative public
compile plus whole assembly, and executes the complete runtime leaf. It also
tests fresh public rejection of recursive child-origin, value, type and source
forgeries. Existing C5/C6/C7 malformed quote, unresolved-name, annotation and
unsupported-numeric rejection evidence remains scoped to its unchanged owning
revision rather than being rebuilt for this consumer edit.

The sole affected composition passed one test/78 assertions on the final
candidate, including thirty-two guest function executions and eight fresh
public C7 verification entries. Test execution took 275.677 seconds plus 7.269
seconds of namespace loading at the unchanged 2 GiB heap. Compiler, fixture,
test, C7 and runtime bytes were unchanged through successful process exit.
The eight entries are observed verifier calls, not a count of all internal
construction, traversal or allocation. Focused and composition workloads differ;
these timings establish no speedup or phase dominance.

Evaluated collection constructors, unquote, numeric elaboration, general match
execution, full effect/profile legality, target support, authoritative migration,
self-hosting and seed retirement remain outstanding. The direct focused and
affected commands are in `docs/development-verification.md`.

### Canonical evaluated vectors: development implementation

The existing Gravity emitter lowers C6 vector-constructor children with C7's
known positional types into `:vector-literal` instructions. The actual
`gravity.runtime` evaluator now supports those instructions by reusing its
ordered argument traversal. Empty, mixed and nested vectors, typed Bool/String
parameters, quoted DATA elements, let forwarding and direct same-module returns
are supported within the compiler-internal `:meta/:jvm` development route.
Source collection values are not substituted for evaluated children.

Items evaluate once from left to right with operand recur targets cleared.
The first throw or rejection preserves its original payload and accumulated
output and prevents later item execution. Quoted control-looking records are
ordinary DATA, not transfers. The new descriptor is closed, bounded to 65,536
immediate instruction maps, and checked shallowly before item execution. Fatal
host failures remain fatal. Lowering reserves its join work before traversal
and cumulatively charges child compilation within the unchanged cap; these are
logical work units, not a total VM or heap guarantee.

The final focused batch passed six tests/212 assertions: vector lowering and
runtime behavior, independent existing C8 equations, recursive quote regression,
runtime catalog/unchanged-body conservation and public orchestration. The first
six-test/210-assertion pass preceded a diagnostic correction: valid but
unsupported maps/sets now retain `:unsupported-typed-lowering-operation` rather
than being mislabeled malformed vectors. Two new controls check that boundary.
Unknown/incoherent type, Never, source/count/order and descriptor cases reject;
runtime controls cover transfers in each position, skipped output and fatal
failure identity.

Only the existing evaluator body changed in the runtime leaf; its 22 functions,
arities and four exports remain. Current conservation checks preserve all 23
catalog DATA values and the unchanged 21 leaf/29 reconstructed original
function bodies, explicitly excluding that intentionally changed owner. The
earlier complete C/D body-conservation result remains historical evidence for
its own revision, not a refreshed whole-runtime claim.

The paired `local-runtime-evaluated-vector` fixture contains nine functions and
twelve vector nodes. Its affected composition reuses each original input for
public compile/whole assembly, runtime checks and independent actual C8 body
analysis. A new String-versus-vector result fixture exercises the owning C7
diagnostic. Existing public typed inputs, fresh C7 verification and source
provenance remain unchanged; no old quote/module proof matrix is refreshed.

The first affected run failed overall: 71 assertions passed, 54 failed and
none errored. The new C8 test consumer passed the outer typed artifact instead
of its required `:identity-input` facts, then asserted against a missing graph.
The failed result remains failed. Independent count/dataflow reconciliation
attributes all 54 failures to those 27 C8 checks per extension, leaving the 71
non-C8 source/lowering/runtime/provenance/tamper/rejection assertions completed
and passed. The first raw output chunk was truncated; this attribution uses
the exact failed test, deterministic missing-graph checks and terminal counts,
not a claim that all stdout was retained. Production code was unchanged by the
subsequent test-only correction. A focused two-test/13-assertion check pins the
correct facts object and prevents missing-graph cascades. The corrected C8-only
source check then passed one test/31 assertions on `.gravity` alone, including
full body coverage, constructor/operand effects, order rejection and missing
declaration rejection. It took 126.439 seconds plus 7.397 seconds of loading at
2 GiB; the failed aggregate took 328.618 seconds plus 7.528 seconds of loading.
These are different workloads, not a speedup comparison. Compiler and fixture
bytes were unchanged across both runs; only the C8 test calls and guard changed.
The completed lowering/runtime/public verification work was not repeated. The
corrected combined selector was not rerun or relabeled passed; selective
validation and its limits remain explicit.

P3 permits compiler-owned internal allocation; an empty direct constructor
effect set does not imply zero physical allocation or grant runtime authority.
Operand effects, ordered body coverage and declaration rejection are retained.
Full C8/profile/provider legality is still explicitly pending and is required
before MIR/admission claims. Evaluated maps have a separate bounded increment
below. Evaluated sets, general collection operations,
hosted/constrained representations, target support, authoritative migration,
self-hosting and seed retirement remain outstanding. Commands and boundaries
are in `docs/development-verification.md`.

### Map source-order projection: development implementation

The temporary SH-07 request projection previously enumerated host maps even
though C2 retained textual entry order. The current correction joins retained
C2 key/value occurrences to the existing projected records and permutes only
evaluated map parents' child links. Existing form/syntax IDs, bindings,
reference paths, scopes, decimal coordinates and other record fields remain
unchanged. Known `defn` expansion uses its retained source origin and verified
header/body transfer; missing or ambiguous evaluated occurrences fail with
`C6-ORIGIN` rather than silently falling back to host order.

Quoted map DATA is inert and retains prior materialization, including repeated
keys already collapsed by the reader adapter. This is not a new language-wide
duplicate-map policy. Evaluated duplicate-key occurrence loss remains an
unsupported provenance case, not a newly declared language error. Metadata,
quote abbreviations, nested maps and set member coordinates have focused
coverage. Redundant whole-container equality and hashing of entry values were
removed; no new host-projection work counter or performance claim is made.

The final focused invocation passed two tests/207 assertions, including an
original-C2-shaped repeated-key control with four source children and two
projected children for both prefix and explicit list quotes. Independent
scoped review accepted the final code, focused result and real-source
composition oracle. The preceding revision passed two tests/204 assertions;
the final delta adds abbreviation checks and the list-quote case.
The earlier negative-control runs failed because they targeted a
metadata-only map rather than the actual syntax root; they remain failed
development runs. Real paired source composition then passed one test/55
assertions: both nine-key functions preserve opposite textual orders from C2
through request links to ordered Core children and their associated throw
operands, even though both host maps enumerate keys alike. Original quoted
duplicate entries remain inert materialized DATA. A rehashed pair-order
mutation fails fresh expected-request membership before Core execution.
The run used 2 GiB; the selected test took 197.354 seconds, outer execution
197.356 seconds, and namespace loading 6.854 seconds. These are development
observations, not a performance comparison.
The compiler, test and fixture bytes stayed unchanged across the final focused
and affected composition runs. No evaluated-map runtime,
authoritative migration, target, self-hosting or release claim follows.

### Canonical evaluated maps: development implementation

The current emitter/runtime increment adds evaluated maps with distinct
literal Keyword keys through canonical typed lowering. Empty and nested maps,
map/vector nesting, typed parameter values, immutable let aliases, direct
same-module returns and typed catch are the bounded fixture scope. Ordered C6
children and flat C7 member types drive lowering; no source map is substituted
for evaluated children. The shared collection helper retains vector checks and
their logical charge. Maps reserve `12 + 14 * child-count` logical join units
before cumulative child compilation; this is not a total VM-work claim.

Runtime consumes closed ordered entry instructions, validates their immediate
shape and literal unique Keyword keys before evaluation, then evaluates each
key and value once. The first throw/rejection preserves payload identity and
accumulated output and prevents later execution. Quoted maps remain inert DATA.
Computed, non-Keyword and duplicate evaluated keys remain implementation
limitations, not language-wide prohibitions. The legacy untyped host-map
compiler still sorts keys; this increment's source-order claim belongs to the
canonical typed route, not arbitrary instruction producers.

The final focused group passed seven tests/161 assertions: map lowering and
runtime behavior, vector regressions, runtime DATA/unchanged-body conservation,
independent C8 equations and correct C8 consumer facts. Keyword-first validation
rejects an opaque non-Keyword key without hashing or equality calls. The first
map-focused run failed two assertions because its synthetic Never case used a
reference without a local activation context; only that test input changed.
That failed run remains failed. The single affected paired-source composition
then passed one test/151 assertions, including 24 guest executions across twelve
functions per source spelling. Normal, nested and forwarded values, typed
catch, exact parameter identity, inert duplicate-key quotes and opposite
nine-key first-throw orders all passed. Public compile/assembly freshly entered
C7 verification seven times; type/order forgeries reject, and a rehashed C6
order mutation fails membership before Core execution. Independent existing
C8 body checks retain ordered operands, thrown effects and declaration
rejection. A new declared-String/map-result source fixture fails with the
owning C7 diagnostic. Each positive input was reused for these assertions;
old quote, vector and migration proof matrices were not replayed.

At 2 GiB, the selected composition took 391.939 seconds, outer execution
391.942 seconds, and namespace loading 7.857 seconds. The final focused group
took 1.015 seconds of outer execution plus 7.259 seconds of loading. These are
different workloads, not a speedup comparison or phase-cost attribution.
Compiler, test and fixture bytes remained fixed across the final focused and
composition runs. Public APIs, C7 and C8 production
code, migration pins and explicit pending effect/profile legality are unchanged.
No target, authoritative migration, self-hosting or release claim follows.

### Literal Keyword map lookup: development implementation

The current C7/emitter/effects/runtime increment connects SH-07's existing
one-operand `(:key map-expression)` Core form to executable development IR.
Constructor and verified quoted-map shapes, immutable let aliases and nested
lookup extraction supply member evidence. Present members retain their actual
type; established absence yields Nil, distinct from a present nil member.
A Never operand remains non-returning. Missing shape evidence is rejected
explicitly rather than replaced with Dynamic. Arbitrary map parameters,
call-returned shapes, generic `get`, defaults and non-Keyword keys remain
unsupported implementation boundaries, not new language prohibitions.

The solved C7 row records the source, canonical lookup record, operand and
selected shape, selected coordinate, presence and invalidation dependencies.
Lowering consumes that row behind fresh same-input C7 verification, preserving
ordered key/operand instructions. The runtime evaluates the entire operand
once, even when the key is absent, and preserves first transfer, payload
identity and output. C8 retains operand effects and escaping thrown types;
lookup itself does not erase an undeclared throw.

The final focused group passed seven tests/255 assertions, including C7 and
lowering joins, present/absent/Never results, runtime order and control, C8
equations/consumer facts, map and recursive-quote regressions, and runtime DATA
conservation. Failed shape attempts retain outer logical charges; exact and
one-less controls exercise two inference rounds and shared lookup/loop work.
These charges do not count recursive helper visits or prove total CPU cost;
the existing shape bounds remain separate. At 2 GiB this group took 1.977
seconds of outer execution plus 7.557 seconds of namespace loading. An earlier
runtime-focused test failed to compile because of a misplaced test parenthesis;
the corrected test's success does not relabel that failed invocation.

The existing SH-07 arity regression separately passed one test/78 assertions
across three rejected cases in both source spellings. It reconstructs complete
source frontdoors rather than using normalized Core inputs: selected execution
took 251.631 seconds, outer execution 251.633 seconds, plus 6.801 seconds of
loading. That cost is not part of the focused-kernel timing above; this unchanged
upstream matrix is not a requirement for each lookup edit.

The single affected paired-source composition passed one test/158 assertions.
Each positive source product was reused for public single-function lowering,
whole assembly, nine guest executions and independent C8 body checks: eighteen
executions across the two source spellings. Each assembly contains nine
functions, ten lookup instructions, seven evaluated maps and two quoted values.
The solved rows distinguish eight present lookups, one established absence and
one non-returning operand. Constructor/let/nested/quoted extraction, present
nil, missing key, inert duplicate-key quote materialization and catch around a
throwing operand all passed, including original payload identity.

Four positive public operations and six type/shape/ledger/key/operand/source
tamper cases freshly entered public C7 verification ten times. Both source
spellings produced equal typed identity inputs. The two new source negatives
reject a scalar operand with `C7-ANNOTATION` /
`:keyword-lookup-map-shape-required`, and a declared String result receiving an
absent-key Nil with `C7-TYPE-MISMATCH` /
`:function-result-type-mismatch`. C8 receives the actual typed identity input,
retains lookup operand order and thrown types, and rejects an undeclared throw;
this does not turn pending full profile legality into an accepted claim.

At 2 GiB the selected composition took 415.574 seconds, outer execution
415.575 seconds, and namespace loading 7.695 seconds. Compiler, test and fixture
bytes were unchanged across final focused and composition runs. The result's
158 assertions include six whole-runtime projection checks omitted from an
earlier static count forecast; no test changed to obtain that count. This is
affected composition evidence, not a speedup comparison or attribution of cost
to individual proof-construction phases. No old map/vector/quote migration
matrix was replayed. Focused success and scoped independent acceptance do not
confer migration, complete effect/profile legality, target, self-hosting or
release authority. Existing authoritative pins remain unchanged and are not
evidence for the new code.

### Local call-return shapes: development implementation

The C7 shape path now follows actual same-module acyclic calls into their
callee bodies. Constructor, quoted and immutable-let shape evidence can cross
a forwarding call and feed the existing Keyword lookup and structural-match
consumers. Call, edge, operator binding, definition, function/body, source and
type joins remain explicit in retained ancestry and invalidation conditions.
Same-arity functions with equal member-type vectors do not establish equal
map keys. Caller argument values do not specialize the callee's shape.

The corrected normalized group passed six tests/155 assertions: new call-shape and
fixture checks, the existing lookup lowerer, and structural inference,
pattern-alias and quoted-shape regressions. Controls include caller-before-callee
inference, body/edge/binding/source mutations, absent keys with unchanged member
types, unknown parameter-carried shapes, duplicate records, depth, cumulative
sibling/failure work and unused-index avoidance. New logical index/join and
retained-evidence charges share the inference budget; separately bounded seed
and helper reconstruction is not an aggregate CPU-visit claim. Earlier cheap
runs exposed a source delimiter error and an unsupported seed `symbol` builtin;
both failed invocations remain failed. The correction did not extend the seed.

At 2 GiB the corrected group took 1.732 seconds of outer test execution, with 7.802
seconds of runtime-test namespace loading and 0.325 seconds for the additional
type-test namespace. These normalized results do not establish source
composition. The first affected source run failed on its first input with
`P15S23Q002` / `:malformed-typed-input-families` before guest execution. Its six
passing assertions checked only whole-runtime projection; they are not
call-return evidence. The test attempted lowering before reporting the
upstream typed status. One narrow diagnostic recovered C7's source/type-join
rejection. Static producer/validator comparison then found a sufficient cause:
the canonical edge carries a definition's name-syntax ID, while a dormant
validator compared it with the whole definition's syntax ID. The new normalized
test had mirrored that incorrect expectation. The corrected validator retains
both source joins, and the test now derives its edge from the owning producer,
uses distinct IDs and rejects the old wrong edge. The composition reports typed
acceptance and verification before lowering. A separate affected existing edge
test passed one test/five assertions after its normalized source record was
corrected; this is not a combined seven-test run. The failed composition remains
failed. The narrow diagnostic printed only the first two of eight diagnostics;
the static mismatch was not a measured sole-predicate attribution.

The corrected candidate's single composition passed one test/149 assertions.
Two real source spellings each supplied one fresh product reused for public
function lowering, complete sixteen-function assembly, sixteen top-level guest
executions and C8 body analysis. Seven public operations freshly entered C7
verification, including three ancestry/key tamper rejections. The functions
exercise forward calls before their definitions, nested/quoted/let-returned
maps, equal return types with different literal keys, present nil, established
absence, catches and original payload identity. No emitter or runtime API
change was needed.

Three additional fresh compiler inputs reject an extracted String declared as
Bool with `C7-TYPE-MISMATCH`, accept an exhaustive constructor-derived structural
match, and reject the old wildcard fallback with `L7-UNREACHABLE`. The last check
preserves the second clause's exact syntax/span/origin coordinates. Only the
`.gravity` structural-match input was type-checked; its `.qst` pair was checked
for byte parity. Structural-match execution remains unsupported. At 2 GiB the
corrected selected composition took 551.919 seconds, outer execution 551.921
seconds and namespace loading 7.385 seconds. These costs are separate from
focused feedback and do not identify individual construction phases or prove
a speedup. Corrected production/test/fixture bytes stayed fixed during this
composition; no unchanged old lookup/map/vector/quote proof matrix was replayed.

No runtime match,
imported/recursive return-shape, unknown map-parameter, unresolved alternative
shape, complete profile-legality, target, migration or self-hosting claim
follows. Runtime and emitter APIs and authoritative pins are unchanged.

### Finite Boolean match execution: development implementation

The existing Gravity emitter consumes C7's solved finite Boolean match rows
and lowers root Boolean literals, wildcard and lexical binding patterns into
the existing `if`, `do` and `let` instructions. Rows join by match identity and
clause ordinal, not their branch-body `lhs`. Source, branch, root-pattern,
skeleton, binding scope, type and selected/residual-domain joins remain
explicit. The scrutinee is evaluated once. A residual binder is initialized
with its verified singleton Boolean inside its own branch, preserving an
outer same-named value in a sibling. No generated name, runtime match opcode,
new seed builtin or public API is introduced.

The affected throwing-local-call composition exposed two existing supported-
form restrictions: C7's call-error graph and the independent verifier in
`gravity/effects.gravity` omitted `match`. Both now accept it using their
existing conservative child-error unions, source order, call-body equations,
match-skeleton checks and budgets. This retains source-error potential from
all branches, not selected-path precision. Existing unsupported-operation,
recursion-component, import and higher-order boundaries are not relaxed.

The pre-composition focused group passed four tests/106 assertions: lowering joins
and work limits, false-first/whole-binder/wildcard routes, active C7 call-error
analysis with independent C8 reconstruction, and five fixture-pair parity
checks. Controls cover original throw payload identity, single evaluation,
non-leaking bindings, changed ledger/source/order/call-body evidence, missing
rows, unknown operations, recursion components and insufficient budgets. An
initial binder-use work-limit failure was being relabeled as a generic
binding mismatch; it now retains the original failure, with an intermediate-
budget control that reaches the traversal. A command using unsupported
`--result-file` failed before executing tests; the corrected command uses the
existing `--timing-receipt` option. That failure is not relabeled as a pass.

At 2 GiB, that focused test execution took 2.238 seconds, separate from
7.505 seconds of namespace loading. Independent scoped pre-run review accepted
the exact candidate. Its single real-source composition completed one test/184
assertions: 182 passed, two failed, zero errors. Both failures were the same
instruction-census oracle: two wildcard matches emit `do`, but the test had
omitted the existing source-let body wrapper in the nested function. The
observed count was three, not two. The original aggregate remains FAILED.

The completed semantic blocks exercised 48 top-level guest executions across
the two source extensions, including returned-map lookup into a match,
shadowing, nested binders, branch-tail recur, original thrown-payload identity,
and an ordinary caller through a match to a normally returning Bool helper
that may throw. Both independent C8 analyses passed, retaining conditional
coverage and conservative thrown errors and rejecting an undeclared allowance.
Seven public operations freshly entered C7 verification, including three
row/source mutations that rejected. Three additional `.gravity`
inputs rejected incompatible patterns, unreachable clauses and branch-result
mismatch; a fourth rejected binder escape in C5. Rejected `.qst` pairs were
checked for byte parity, not separately type-checked.

Only the test's expected `do` count and an explanatory comment changed after
that result. The four K kernels plus the existing exact let-lowering kernel
then passed a separate five-test/169-assertion run. Its outer execution took
2.486 seconds, separate from 7.521 seconds of namespace loading. These overlap
the earlier controls and are not added as unique coverage. Independent review
accepted the bounded semantic evidence and exact oracle correction; the
corrected full selector was not replayed or reported as a new 184/184 pass.
No production or fixture bytes changed after the composition. At 2 GiB its
selected execution took 664.653 seconds, outer execution 664.655 seconds and
namespace loading 8.267 seconds. These timings do not attribute construction
phases or establish a speedup. No unchanged upstream matrix was repeated.

Structural-pattern and guard execution, partial Boolean lowering, complete
profile legality, target lowering, authoritative migration and self-hosting
remain outstanding. This finite implementation limit does not impose a
universal L7 totality rule. Normally returning guards retain L7's Bool-only
policy. No historical authority pin is refreshed by this development work.

### Constructor-linked structural match execution: development increment

The Gravity emitter now projects C7's existing unguarded structural pattern
trees into one closed internal `:structural-match` instruction, evaluated by
the stage-owned Gravity runtime. Fixed vectors require exact width; literal-
Keyword subset maps permit extra keys and distinguish absent keys from present
nil. Nested wildcard, lexical binding and represented primitive leaves preserve
source order. Family checks precede literal equality. Represented Integer
matching does not authorize coercion to fixed-width numeric types.

C7 continues to own constructor shapes, partitioning, extraction facts and
exhaustiveness. The emitter joins the consumed tree, source coordinates,
clauses, binding scopes, narrowed types and uses instead of duplicating that
solver. Runtime bindings from a failed partial pattern are discarded before
the next clause. The scrutinee executes once without an outer recur target;
the selected body retains its own tail target and original control payload.
The runtime's four public exports remain unchanged. Descriptor bounds are
local shape bounds, not a global evaluator CPU or termination proof.

The final normalized group passed twelve tests/291 assertions: structural
lowering, runtime behavior, represented Integer matching, fixture parity,
unchanged-runtime conservation, existing Boolean lowering/routes, three C7
structural/quoted-shape kernels and two C8 operation/coverage kernels. Controls
include exact lowering work limits, malformed source/scope/coordinate evidence,
absent-versus-nil and width, same-name binder rollback, opaque values with zero
equality/hash calls, validation before output, recur boundaries and original
fatal-error identity. At 2 GiB the outer test execution took 2.015 seconds,
separate from 7.644 seconds of runtime-test namespace loading and 0.482 seconds
for the additional type-test namespace. Overlapping earlier focused runs are
not added as unique coverage.

Earlier invocations exposed a matcher delimiter error, an unsupported seed
`merge` call, normalized fixture origin mismatch and a test delimiter error.
Those failed invocations remain failed. The corrections preserve the strict
production source join and use a private map-extension loop without extending
the seed. Scoped independent review accepted the frozen candidate for one
affected real-source composition. That original run passed one test/177
assertions without a retry or input changes.

Each real source spelling supplied one fresh product reused for public
function lowering, complete fourteen-function assembly, twenty top-level guest
executions and actual C8 body analysis. The forty executions cover returned
and quoted constructors, nested shadowing, multiple binders, partial-binding
rollback, called throws with original String identity, and source-verified
selected-body function recur. Each source contains eleven matches and
seventeen structural rows. Both C8 analyses preserve conditional coverage and
conservative throw propagation, and reject an undeclared allowance.

Eight tracked emitter operations freshly entered public C7 verification:
four positive compile/assembly operations and four coherent mutations. The
mutations change extraction paths across their aliases, pattern coordinates,
binding scopes or remaining-domain evidence and reject with `P15S23Q002`.
That count excludes the source-product constructor's upstream verification.
Four additional `.gravity` inputs reject incompatible patterns with
`L7-PATTERN-TYPE`, known absent-key and wrong-width clauses with
`L7-UNREACHABLE`, and reachable result mismatch with `C7-TYPE-MISMATCH`.
A fifth rejects binder escape with `C5-UNRESOLVED`. Their rejected `.qst`
pairs were checked for byte parity, not separately type-checked.

At 2 GiB the selected composition took 737.988 seconds, outer test execution
737.990 seconds and namespace loading 7.584 seconds. All fifteen compiler,
runtime, test and fixture files stayed unchanged across the final focused and
composition runs. These different workloads do not establish a speedup or
identify which construction phase dominates. No unchanged Boolean, lookup,
map, vector or quote proof matrix was replayed.

Guards, additional L7 pattern families, complete profile legality, documented
target lowering, authoritative migration, self-hosting and release remain
outstanding. Existing authority pins are unchanged and do not certify this
development code.

### Root-map guard completion and execution: development increment

The current guard increment retains L7's Bool-only normal result policy and
preserves non-returning control. C7 no longer rejects every guard containing a
throw, try or recur descendant. Existing type, target and effect checks still
decide whether those expressions are legal. Every body retains its actual type
and legality checks; a body that cannot execute after a false or Never guard
has separate effective-completion evidence instead of a fabricated body type.
A Never guard removes its structurally matched cases by transferring control,
not by selecting its body or falling through.

The emitter consumes those facts through the existing closed structural-match
instruction. Runtime checks normal results are Boolean, evaluates guards once
after structural success, restores outer bindings on false while preserving
output, and propagates original non-normal control. Guards cannot recur to an
outer target; selected bodies retain their original targets, and a guard-local
loop retains its own target. C8 independently accepts authenticated Bool or
Never guards and retains conservative source-error potential.

Review caught two valid-program regressions in the draft. An Unknown first
branch was being mistaken for an empty result accumulator, allowing a later
Never branch to fix an inferred return prematurely. The correction uses the
existing alternatives join over all effective branch types; owning first-pass
and acyclic caller-before-callee checks preserve Unknown until String resolves.
Nested guards also exposed a difference between grouped Core child layout and
actual guard-before-body lowering order. Binder-use collection now determines
reachability, precharges its original-node scan and emits uses in authenticated
node order. Exact-budget, one-less and shared Boolean/structural lowering
controls passed. Neither finding required another parser or authority wrapper.

The final corrected normalized group passed fifteen tests/685 assertions: C7 completion,
quoted guards and forward inference; C8 guard checks; emitter/runtime controls;
fixture parity; unchanged-runtime conservation; and existing normalized C6 guard,
Boolean lowering, structural lowering and local-loop ancestry regressions.
At 2 GiB outer test execution took 4.781 seconds, with 7.733 seconds of runtime-
test namespace loading, 0.270 seconds for the C6 test namespace and 0.332 seconds
for the type-test namespace. Earlier thirteen-test/612-assertion,
ten-test/235-assertion and four-test/112-assertion groups
overlap these controls and are not added as unique coverage. No unchanged
four-source SH-07 rejection matrix was repeated.

Failed drafts remain failed: a test delimiter error prevented the first
producer batch from executing; a later normalized typed-try fixture retained
stale handler-use and ancestor source values; and the first consumer batch had
two errors from an unsupported seed `dissoc` and a runtime delimiter error.
The corrections keep the production handler source join strict, use an explicit
field projection rather than expanding the seed, and preserve those outcomes.
A broader batch also failed one of 235 assertions because its normalized C8
graph supplied ordered error values without the required escaping-error set.
The fixture now carries both representations, as actual derived nodes do;
the production checker was unchanged. That 234-pass/one-failure run is not
relabeled as the later ten-test/235-assertion pass.

Paired source fixtures cover inline throw, conditional throw, typed catch,
false and Never completion, nested binder use, loop/recur and forward inference.
The first real-source composition failed after nine passing assertions with
one uncaught lowering error, `typed-loop-source-or-target-mismatch`, while
assembling the guard-local loop case. Its one-test/ten-assertion result remains
failed; later runtime, effect and negative-source checks in that test did not
complete. Selected execution took 185.289 seconds, with 7.879 seconds of test
namespace loading. The source/target join requires correction and affected
validation; this is not a reason to remove the legal local-loop case or relax
outer-target recur rejection.

The correction preserves the nearest lexical target while compiling a guard
with tail eligibility disabled, matching C6. All existing loop source, target,
scope and ledger equalities remain intact. Its affected normalized group passed
four tests/145 assertions, including sixteen new guard-local-loop checks,
existing loop lowering and guard lowering/runtime checks. Execution took 2.502
seconds plus 7.659 seconds of namespace loading. An initial version of the new
fixture omitted its branch clause ordinal and failed three of 136 assertions
at the guard-evidence join, before loop compilation; the fixture was corrected,
not the production join. These overlapping focused results are not added to
the earlier group as unique coverage.

The corrected candidate's real-source run completed 222 assertions with 221
passes and one failure. Its only failed assertion read the final C6 diagnostic
from `:id`, although the owning C6 diagnostic and rejection bridge retain
`:rule`; the following exact `:recur-tail-position-required` assertion passed.
The aggregate remains failed, not a 222-assertion pass. Selected execution took
1080.451 seconds, plus 8.031 seconds of namespace loading, at the unchanged
2 GiB heap. Compiler and fixture bytes were unchanged throughout that run.

Its passing assertions cover both source extensions, 45 guest executions,
preserved payload identity, false/Never effective completion, forward String
inference, nested binder use, guard-local loop and selected-body recur. Two
actual C8 analyses retained guard dependencies and source errors, discharged
the typed catch and rejected an undeclared module allowance. Nine tracked
emitter C7 verification entries include four coherent type/truth/completion/
region forgeries; this count excludes upstream constructor verification.
Six C7 negatives and three earlier C5 negatives also passed, with thirteen
fresh source inputs in total. Rejected `.qst` pairs have fixture parity checks,
not separately repeated source construction. No seed throw-transport count
was instrumented or claimed.

The test now reads the owning C6 `:rule` field and retains the exact reason
assertion. A separate five-test/432-assertion focused run passed, including
seven direct checks through the current C6 recur lowerer, rendered rejection
and existing exception bridge. It also covers the normalized C6 guard kernel,
guard-local parent and guard lowering/runtime controls. Execution took 3.185
seconds plus 7.838 and 0.272 seconds of namespace loading. Its first invocation
stopped at a test-source delimiter error before assertions; that outcome is
preserved. No compiler or fixture changed for this correction, and the
18-minute source composition was not replayed or relabeled as passed.

Do not replay unchanged source construction merely to turn a test-only field
correction into a fresh aggregate pass. This development evidence does
not establish full L7, profile/provider legality, documented target support,
authoritative migration, self-hosting or release completion. Historical
authority pins remain unchanged.

### Direct integral ordering: development increment

The comparison increment adds direct binary `<`, `<=`, `>` and `>=`
over two operands of one established integral type. Integer remains distinct
from the eight fixed-width signed/unsigned types; no implicit conversion,
floating ordering or first-class operator value is supplied. The finite
proof-required safety rule derives Bool on normal invocation and Never when
an operand cannot return, while still checking other operands.

C7 reconstructs an occurrence-specific intrinsic descriptor, call fact and
numeric-mode constraint from the actual existing core binding. C8 and lowering
check the finite rule independently without inventing function/body identities
or changing C6's call and edge shapes. The semantic owner is `gravity.math`;
the existing core catalog is compatibility resolution, not a fabricated math
module artifact. Runtime code is unchanged. Inference reuses per-pass indexes
instead of rebuilding a node index inside every scope edge; this is not a
measured speedup or a total CPU-accounting claim.

The focused candidate passed seventeen tests/720 assertions, including the
four numeric kernels, paired fixture checks and affected guard, structural,
Boolean, direct-call and body-effect regressions. Execution took 2.968 seconds
plus 7.868 and 0.343 seconds of namespace loading at 2 GiB. The first group
remains failed: 383 assertions passed and three new runtime test expectations
used `:throw` instead of the existing `:thrown` control tag. Only those test
oracles were corrected; the production runtime was not changed. Overlapping
groups are not summed as unique coverage.

A subsequent dispatch-preservation correction restricts intrinsic lowering to
nonlocal calls whose operator occurrence has the applied-ordering descriptor.
Other calls keep their original direct-call rejection reason. Four affected
lowering kernels passed 140 assertions on that correction (2.282 seconds
execution plus 7.707 seconds loading at 2 GiB); this is separate evidence, not
a claim that the earlier 720-assertion group was rerun on changed bytes.

The first affected real-source composition failed before numeric checking:
six assertions passed, then C5 rejected the fixture's `gravity.core/>` variable
spelling with `C5-ALIAS` / `:unknown-namespace-alias`. It took 38.007 seconds
execution plus 8.047 seconds loading at 2 GiB. This establishes no paired
numeric, guest execution, C8 or negative-source coverage. Qualified core
variable resolution is not implemented by this slice; normalized descriptor
tests do not establish that source admission.

The fixture-only correction uses unqualified `>` and an ordinary String
parameter named `>` passed to a local function. This tests lexical value
shadowing without pretending it is a qualified core call or an intrinsic.
The final fixture/type kernels passed two tests/308 assertions in 0.742 seconds
plus 7.486 and 0.343 seconds loading at 2 GiB. Production code did not change
for this fixture correction.

The corrected paired source composition passed one test/202 assertions in
915.956 seconds execution plus 8.315 seconds loading at 2 GiB. It constructed
two fresh positive products, executed 36 guest cases, analyzed two C8 body
graphs and tracked six public-emitter fresh C7 verification entries: four
positive standalone/assembly calls and two coherent descriptor forgeries.
Seven actual rejected `.gravity` fixtures checked arity, mixed/nonintegral/
unknown types, invalid operands after Never, lexical operator calls and
escaped operator values; their `.qst` counterparts have focused pair parity,
not separate actual construction. Each positive retains ten functions, nine
calls and eight numeric descriptors (six normally invoked, two not invoked).
It covers all four operations, signed/unsigned boundaries, exact Integer
literals, lexical core-name shadowing, a computed guard and one/both Never
operands under typed catches. The runtime remained unchanged. The failed
first composition remains failed; no unchanged expensive matrix was replayed.

Source fixtures declare `:memory/allocate`; empty direct numeric effects do
not claim wrapper purity or close constructor allocation inference. Full
numeric typing, standard-library, profile/provider legality, target support,
authoritative migration, self-hosting and release remain outstanding.

### Collection-constructor allocation: development increment

At `02c1077c`, the body-effect pass's `body-node-record` derives a direct effect
for a normally transferring throw, but no allocation effect for a collection
literal. The development runtime constructs maps and vectors when evaluating
their instructions; quoted DATA instead returns its retained value. An empty
inferred collection effect set therefore does not establish allocation-free
behavior. L6 already registers `:memory/allocate`, and STD3 requires visible
allocation behavior for constructors and updates.

The effect owner now derives conditional `:memory/allocate` for evaluated
vector/map/set constructors. A Never operand suppresses only the constructor's
own allocation; operand effects and errors remain. Quoted DATA stays inert,
function bodies stay latent, and calls propagate their body effects. A static
allocation record identifies normal operand completion and constructor-input
extent in map entries or vector/set elements, not observed heap bytes or the
runtime cardinality of a set after deduplication. Existing graph
equation checking and whole-result reconstruction bind that record. Constructor
kind/count checks and the logical work budget remain enforced.

The focused batch passed ten tests/214 assertions in 0.564 seconds execution
plus 9.092 seconds namespace loading at 2 GiB. The first batch remains failed
(199 passed, one failed assertion and one error): its fixture check read an
existing duplicate-key quoted DATA form through the host reader, and its
normalized function-allowance case lacked the owning function record. Only
test setup changed: inspect the namespace header for allowance parity and
include the function record. The production rule was not relaxed.

The narrow accepted/rejected paired source composition passed one test/93
assertions in 356.126 seconds execution plus 7.655 seconds namespace loading
at 2 GiB. It reused four fresh source/type products: the accepted body-effects
pair and the undeclared-constructor pair. Each positive graph records two
maps, one vector and one set; `choose` and `assemble` infer allocation while
the other four functions remain empty and namespace definition effects remain
empty. Both undeclared constructors pass C7 and reject at C8 with
`C8-UNDECLARED` / `:function-effect-outside-source-allowance` for
`:memory/allocate`. Ten whole-result verifications cover two accepted results,
six coherent erased/forged graph rejections and two valid rejected results.

This closes the finite constructor inference/source-declaration boundary. It
uses the existing effect owner, declaration check and fresh whole-result
verification without rebuilding the unchanged public C8 module bundle.
Existing fixture declarations now acknowledge constructor allocation; their
prior identities and results remain historical, not relabeled current
evidence. C7, emitter and runtime code are unchanged. Persistent association
can follow this correction. Full allocation-strategy, capability, ownership
and deployment/provider acceptance remain separate outstanding gates; a
source allowance is not an allocator grant.

### Persistent map association: development increment

The current-source candidate implements the bounded STD3 operation
`(assoc established-map :literal-key value)` across the C7 engine, effect
owner, typed emitter and stage-owned runtime. It adds a missing Keyword key or
replaces its value without mutating the input. Established Bool parameters,
String results and nested map values are not restricted to literal values.
Derived association ancestry composes with immutable aliases, local returns,
later association, Keyword lookup and supported structural matching. The
descriptor records a derived map, not a fabricated literal-constructor origin.
Normal completion allocates; a Never operand suppresses only invocation and
own allocation, not operand effects or legality checks. Evaluation order is
operator, map, key, value, once each until control transfers.

C7 owns the deep type and shape derivation. C8 reconstructs the finite
source/binding/order/completion premises that determine its effects, preserves
operand dependencies and checks graph equations. It does not duplicate C7's
shape solver. The current public C8 report-envelope gate does not itself
execute fresh C7 verification; the affected source test explicitly constructs
current C7 products and the public lowerer verifies them freshly. No upstream
authentication upgrade is claimed for C8.

The pre-composition focused batch passed ten tests/579 assertions in 2.135
seconds of execution plus 8.541 seconds of namespace loading at 2 GiB. It covers the new
type/effect/lowering/runtime kernels, paired fixture parity and allocation,
ordering and ordinary-call regressions. Earlier failed batches remain failed:
two synthetic error-evidence setup assertions; one test-load helper-scope
error before assertions; and three producer mutations that changed unused
convenience fields instead of the owning call/edge products. Corrections did
not weaken production semantics. Independent review also caught new C8
count-before-shape guards; the old shape guard now dominates indexing and the
new source-vector guard precedes counting, with controlled-negative checks.
C7 charges association work before its source/caller scan.

The affected source composition completed one test/303 assertions: 299 passed,
four failed and none errored. It remains a failed aggregate. Execution took
934.952 seconds plus 7.968 seconds of namespace loading at 2 GiB. Both fresh
positive source products completed public lowering, 30 guest invocations and
C8 checking. Thirteen functions and fourteen calls retain ten association
descriptors: eight normal and two not invoked. Seven public operations freshly
invoke C7: four positive lowering checks and three descriptor-evidence mutation
rejections. Two positive whole-C8 verifications and three coherently resealed
effect-graph rejections completed, as did all seven rejected `.gravity` sources.
Their `.qst` counterparts are byte-identical fixture checks, not separately
executed negative source coverage.

All four failed assertions were two incorrect effect expectations repeated for
the two source extensions. Typed catches clear escaping error types, but
`:inferred` retains the performed `:error/throw` label while handler/profile/
capability discharge is pending. The correct inferred sets are
`#{:memory/allocate :error/throw}` for `caught-value` and `#{:error/throw}` for
`caught-input`; empty residual `:throws` is a separate fact. Only those test
expectations were corrected, not production behavior. The expensive source
composition was not replayed to obtain a green aggregate.

A separate test-only follow-up passed three tests/99 assertions in 1.479
seconds of execution plus 7.738 seconds of namespace loading at 2 GiB. It adds
seven current-C8 checks of conservative performed effects versus covered and
escaping typed errors, and reruns the association effect kernel and fixture
parity. Production and fixture bytes are unchanged. This repairs the oracle;
it does not relabel the original 303-assertion aggregate as passed.

The public mutation tests prove genuine C7 rejects before lowering. Although
repeated descriptor aliases are changed consistently, the altered input/value
shapes do not regenerate every finite transition/result projection. These are
not isolated proofs of deep-ancestry authentication independent of finite
consistency checks. Negative source tests establish rejection and owning rule
families, not a precise per-fixture diagnostic-reason oracle for every earlier
inference path.

The direct commands are in `docs/development-verification.md`.
Dynamic/general keys, variadic association,
unestablished map parameters, transients, complete collection-library support,
full allocator/profile/provider/ownership legality, target completion,
authoritative migration, self-hosting and seedless release remain outstanding.

### Collection count: development increment

The current-source implementation adds `(count collection)` for established
map and vector types through the same C7 engine, effect owner, typed emitter
and stage-owned runtime. Normal completion returns Integer; a Never operand
does not invoke the query. The operand is evaluated once and the runtime
counts its actual top-level members. The query adds no collection-allocation
effect, while operand allocation, errors and control transfer remain visible.

This operation consumes the established collection type, not the narrower
structural-pattern shape domain. Legal quoted maps with String keys or nested
values outside that domain remain countable. There is no recursive shape
proof, static-cardinality field, implicit I64 conversion or new generic Map
annotation. Existing source/binding/call/edge/type joins remain required.
C8 checks its finite effect premises rather than duplicating C7 inference;
the public lowerer retains fresh C7 verification at its existing boundary.

The pre-composition focused batch passed eleven tests/798 assertions in 2.522
seconds of execution plus 8.029 seconds of namespace loading at 2 GiB. A later
three-site correction guards source child-vector shape before counting it in
C7, C8 and lowering; its affected checks passed three tests/289 assertions in
1.398 seconds plus 8.169 seconds of loading. These overlapping batches are
not combined into a new coverage total. A separate paired-fixture/header
check passed one test/20 assertions; it does not parse or execute the source.
Two earlier test-reader delimiter failures occurred before assertions and
remain failed attempts, not semantic results. An earlier producer-only batch
exercised the superseded deep-shape draft and is not final-candidate evidence.

The single affected source composition passed one test/400 assertions with no
failures or errors. Selected execution took 961.418 seconds plus 7.919 seconds
of namespace loading at 2 GiB. Both fresh positive source products completed
public lowering, 38 guest invocations and C8 checking. Fourteen functions and
seventeen calls retain eleven count descriptors: ten normal and one not
invoked. Seven public operations freshly invoke C7: four positive lowering
checks and three descriptor-evidence mutation rejections. Two positive
whole-C8 verifications and three coherently resealed graph rejections passed.

One mutation rebuilds the existing association descriptor's finite projection
after changing its input-body source reference. The private finite validator
accepts that projection, then the genuine public C7 verifier rejects the stale
ancestry before lowering. This isolates more than P's earlier finite-consistency
mutants; it is not a comprehensive verifier-correctness proof.

All nine rejected `.gravity` sources passed their exact-reason checks: arity,
nil, String, scalar, unknown, set, list, wrong result annotation and escaping
the core binding. Their `.qst` counterparts have byte-identical fixture checks,
not separately executed negative source coverage. Quoted duplicate-key DATA
counts its materialized map, and quoted String/compound-key maps remain
countable without being structural patterns. Caught operand errors retain
performed allocation/throw effects while their escaping typed errors clear.
No production, test or fixture repair occurred during the composition; all
27 source-path hashes remained unchanged. Independent review accepted the
scoped implementation and affected integration. Direct edit-loop and
composition commands are in `docs/development-verification.md`.

Strings, nil conventions, sets/lists, unknown operands, additional collection
families, general annotations, full profile/provider/ownership legality,
authoritative migration, target support, self-hosting and seedless release
remain outstanding. Focused implementation feedback does not admit a new
compiler revision or refresh historical authority evidence.

### Proven vector indexing: development increment

The current-source implementation adds two-argument `(nth vector index)` through the
existing C7 engine, effect owner, typed emitter and stage-owned runtime.
Normal selection requires an established positional vector type and an Integer
source literal within its extent. The selected type is the actual member type,
including heterogeneous vectors and Nil members. A Nil value is not missing
type evidence or an out-of-bounds fallback. Never operands retain control
transfer without an executed access, selected value or bounds proof; other
non-Never operands still require their supported type and index facts.

Ordinary scalar selection does not require every member to be a structural
pattern. A later lookup or pattern consumer projects only the selected deeper
ancestry, including nested evaluated and quoted vector selections. Evaluated
vectors still check all immediate child source, order, extent and type joins.
Quoted selection consumes the existing validated quoted DATA before requesting
the terminal selected shape. Immutable aliases and local returns preserve the
existing binding and call lineage. Lookup-produced vectors retain the earlier
lookup owner's shape boundary; this is not general projection for every
collection producer.

Operands execute once, left to right, until transfer. Selection preserves the
actual member identity and adds no collection allocation; operand allocation
and errors remain visible. The finite bounds record does not authorize check
elision: the runtime retains vector, integer and range guards before access.
Malformed development IR rejection is not a source dynamic-bounds failure API.

The final focused projection/type/fixture batch passed seven tests/234
assertions in 1.091 seconds of execution, plus 8.677 seconds of namespace
loading, at 2 GiB. It includes selected and unselected source/order/type
mutations, nested evaluated and quoted selections past unrelated String-key
maps, and paired-fixture parsing. Two affected return-shape and lookup
regressions passed 84 assertions in 1.980 seconds plus 7.747 seconds of loading.
Earlier overlapping focused batches are not summed into unique coverage.

Earlier producer/projection delimiter errors and unsupported internal
`dissoc` failed before semantic assertions. The latter was replaced with the
already supported private-context update, not a seed extension. A combined
focused batch also retained two failed test expectations: when a mocked index
has Never type, its unused literal value is not C8 evidence. Named controls
identified those cases; the test-only correction preserves normal-index and
source-join rejection. Those failed aggregates remain failed history.

The single affected source composition passed one test/630 assertions with no
failures or errors. Selected execution took 1,262.029 seconds plus 8.129 seconds
of namespace loading at 2 GiB. Both positive source extensions completed
typing, lowering, execution and effect checks: 18 functions and 19 calls retain
17 nth descriptors (14 normal, three not invoked) and two local calls.
All 36 guest invocations passed, including first/last and heterogeneous members,
Nil, immutable aliases, local returns, nested evaluated/quoted selection,
pattern consumption, caught operand errors, shadowing and the entrypoint.

Seven public operations freshly invoke C7: four positive standalone/assembled
lowering checks and three coherently altered descriptor/ancestry rejections.
The stale actual callee-body-source mutation still satisfies the shallow nth
validator before genuine public C7 rejects it. This is a specific ancestry
control, not a comprehensive verifier-correctness proof. Two accepted whole-C8
verifications and three coherently resealed effect-graph rejections passed.
All eleven negative `.gravity` fixtures passed their exact rule/reason checks;
the `.qst` negatives have byte-identical parity checks, not separately executed
negative-source coverage. No compiler, test or fixture edits occurred during
the run; all 31 source-path hashes remained unchanged.

The direct kernels and separate composition command are documented in
`docs/development-verification.md`. Dynamic index failure semantics, general
collection types, profile/provider/ownership legality, authoritative migration,
target support, self-hosting and seedless release remain outstanding.

### Bounded counter successor: development increment

The current-source C7 engine, effect owner, typed emitter and stage-owned
runtime implement a finite proof-required Integer `inc`. The supported loop
starts its counter at zero, compares it strictly against runtime `count` of
the same established immutable vector, and advances that same slot by one on
every incoming backedge. The original loop target, slot, initializer, guard,
count operand, vector origin and complete transfer census are checked. The
existing vector child limit bounds its extent to 1024.

The invariant `0 <= i <= N` and dominating true-edge fact `i < N` establish
`0 < i + 1 <= N`. An empty vector has an unsatisfiable increment edge and a
normal zero-iteration exit; it does not produce a fictitious executed increment
or Never result. Immutable vector aliases and multiple coherent backedges are
covered. The result remains Integer, with a separate numeric-mode/range record,
not an implicit I64 or arbitrary-precision arithmetic choice.

The successor adds no allocation or error effect, but preserves operand effects
and control transfer. Count remains at its original evaluation site. No
folding, hoisting, check elision or general termination claim follows. The
runtime retains integer, current-slot and range checks before addition.
Defensive rejection of malformed development IR is not a source overflow API.

The combined focused batch passed seven tests/301 assertions in 2.067 seconds
of selected execution plus 8.157 seconds of namespace loading at 2 GiB. Its
fixture parsing preceded removal of unsupported Integer result annotations;
the corrected paired fixtures subsequently passed 22 parsing assertions.
The final strict-guard/fixture batch passed two tests/29 assertions, and four
affected loop/control regressions passed 186 assertions in 2.137 seconds plus
9.061 seconds of loading. Overlapping batches are not summed as unique coverage.

Earlier focused failures remain recorded: unsupported private `vals` and
`merge` calls were replaced with existing bounded operations, delimiter errors
were corrected, normalized vector-origin fixture joins were completed, and
budget exhaustion now retains its owning diagnostic. No seed extension was
introduced. Scalar, nil, map and string path controls motivated guarded path
consumers rather than host exceptions; the finite consumers do not replace
fresh public C7 reconstruction of the complete candidate.

The first affected source-composition attempt stopped before source processing:
the selector resolved the SH08 test helper before requiring its namespace.
Its result remains one test, zero passes and one setup error, not semantic
validation. After a one-line test-only dependency correction, the cold
namespace/fixture check passed one test/22 assertions. It did not execute the
composition body containing the new require; that dependency path received
targeted static review.

The changed selector's composition completed one test/280 assertions with
278 passes, two failures and no errors. Selected execution took 1,052.035
seconds plus 9.456 seconds of namespace loading at 2 GiB. Both failures were
the paired call-census expectation: ordinary local call facts omit `:dispatch`;
that label exists only on a different higher-order branch. The intrinsic counts
matched, but the expected local dispatch tag did not. The pre-run review missed
this schema distinction too. The failed aggregate remains failed history; no
production change is justified by this test-oracle error.

Both positive extensions completed typing, lowering and execution: four
functions, eleven calls (four successors, three counts, three comparisons and
one local call), three loop bindings, seven recur targets and four transfers.
All twelve guest invocations passed with results zero, one and three, including
the immutable alias and both backedges. Four positive public C7 operations and
three coherently altered candidate rejections passed. The unused source-echo
mutation still satisfies the finite successor predicate before fresh public C7
rejects the full candidate; this is not an isolated callee-body ancestry test
or a comprehensive verifier-correctness proof.

Two accepted whole-C8 verifications and three coherently resealed graph
rejections passed. All ten negative `.gravity` fixtures passed exact rule/reason
checks; paired `.qst` negatives have byte parity, not separately executed
negative-source coverage. The mode-named source fixture lacks the required
range proof; it is not an explicit wrapping-mode syntax test. Descriptor and
development-IR mutations separately cover unsupported represented modes.
All 29 compiler, test and fixture hashes remained unchanged during the run.
The subsequent test-only correction expects the existing nil dispatch and
explicitly joins the local call's node, caller and callee to its canonical
edge/function table. The existing direct-call lowering and paired-fixture
checks passed two tests/78 assertions in 0.629 seconds plus 8.424 seconds of
loading. Those checks do not execute the corrected composition body or its new
local-join assertions. The expensive source composition was not replayed solely
to repair this oracle; no result for the corrected aggregate is claimed.

The direct kernels and separate composition command are documented in
`docs/development-verification.md`. General arithmetic, arbitrary starts and
steps, unproved extents, fixed-width checked errors, BigInt, profile/provider
legality, authoritative migration, target support, self-hosting and seedless
release remain outstanding. This finite domain is not a language-level ban on
other documented numeric behavior.

### Guarded loop vector indexing: development increment

The current C7 engine, effect owner and typed emitter extend established
vector `nth` from literal indices to the original Integer slot of the accepted
bounded counter loop. The vector is nonempty with one established homogeneous
element type. Access must occur beneath the strict count guard's true edge,
using the same immutable vector origin and loop slot. Immutable aliases are
allowed; another vector of the same type and extent is not the same origin.

This rule consumes the existing counter invariant rather than running a
second range solver. Access requires `0 <= i < N`; the successor's weaker
upper bound `i + 1 <= N` does not justify indexing with `(inc i)`. The call
retains its original operator/vector/index evaluation order, returns the
established homogeneous element type, and adds no allocation or error effect.
Operand effects and non-returning control transfer remain visible. The
existing runtime and its vector, integer and bounds guards are unchanged;
no check elision or general termination claim follows.

The cold inference kernel begins without completed successor or nth
descriptors, covers both recur/throw branch orders and preserves current facts
over prior-round facts. Separate origin/scope tests retain a valid counter
invariant while rejecting a different same-extent vector and a nested target.
Deep selected-shape reconstruction is explicitly unsupported for this variable
index; ordinary element typing does not grant constructor-coordinate ancestry.
The existing literal-index and Never-operand rules remain separate.

Focused feedback includes a two-test/77-assertion producer/cold-inference pass,
a separate one-test/21-assertion origin/scope pass, and the corrected effect
kernel's one-test/75-assertion pass. An eight-test/330-assertion regression
batch covers the producer, cold inference, origin/scope, deep-shape fence,
effects, literal nth and bounded-successor lowering, and paired fixture
parsing. A final two-test/98-assertion batch covers changed lowering and
fixture parsing. These overlapping batches are not summed as unique coverage.

Earlier failures remain history: source/test delimiter errors stopped loading;
an effect fixture omitted the existing escaping-error set required by the
record contract; one command used the wrong working directory and stopped
before tests. Lowering's initial 78-assertion run had three real failures:
forged contextual callee edges were accepted even though its core-derived
proof was valid. Lowering now compares contextual call and edge records with
the descriptor's authenticated source records. The changed kernel passed all
78 assertions; the earlier 75-pass/3-fail aggregate remains failed.

The paired source fixtures contain a Bool-vector scan through an immutable
alias, two unit-step backedges, a String throw, catch and normal entrypoint.
Nine rejected fixture pairs cover different origin, wrong slot, derived index,
heterogeneous or empty element typing, nested target, outside-guard access,
false-edge access and an unknown index. Parsing establishes only syntax and
pair parity.

The single frozen source composition completed one test/166 assertions:
150 passes, 16 failures and no errors. Selected execution took 1,001.585 seconds
plus 8.006 seconds of namespace loading at 2 GiB. All 28 compiler, runtime,
test and fixture hashes remained unchanged. The aggregate remains failed.
Both positive extensions completed typing, lowering and guest execution:
three functions, seven calls, one loop, four recur targets, two transfers and
five builtin instructions. Ten guest invocations passed, including two actual
String throw identities per extension, catch recovery to 99 and normal scans
to three. Seven fresh public C7 operations covered four positive operations
and three altered candidates. The unused source-value echo hostile passes the
finite helper but fails full public reconstruction; it is not a comprehensive
verifier-correctness or isolated selected-ancestry proof. The nine negative
`.gravity` fixtures rejected within their explicit allowed diagnostic pairs;
the run does not identify which overlapping pair matched each case. The `.qst`
negative fixtures have byte parity, not separately executed rejection coverage.

Each positive extension had one C8 rejection followed by seven missing-graph
assertion failures. C8's throwing-call verifier excludes loop/recur from its
scoped operation whitelist, although the preceding derivation already checks
their original transfers, effect fixed point and final graph equations. This
is a sufficient static rejection condition; the shared diagnostic reason
alone does not uniquely identify the observed internal callsite. C8's verifier
accepting a well-formed rejected result, and hostiles rejected from an absent
graph, do not establish accepted effect graphs or successful graph-forgery
coverage. The narrow C8 compatibility correction adds only checked loop/recur
to that whitelist, preserving the existing joins and equations. The extended
local throwing-call kernel exercises module-node derivation and the actual
throwing-call verifier, including a called loop's String throw, catch residual,
performed throw label, exact/one-less work bounds and forged targets,
transfers, rows and summaries. Its corrected revision passed one test/44
assertions; the six-test/388-assertion affected regression batch also passed.
The batches overlap. Earlier mock identifier formatting, missing body scope,
and dependent missing-graph/work-bound failures remain recorded; they did not
justify further production changes.

A single-source C8 follow-up against the unchanged `.gravity` positive passed
one test/24 assertions in 114.787 seconds plus 6.935 seconds of namespace
loading at 2 GiB. All 29 compiler, runtime, test and fixture hashes remained
unchanged. It requires an accepted nonempty graph before whole-result
verification and the three coherent graph hostiles. The accepted graph retains
the two-transfer loop fixed point, pure nth dependencies without an allocation,
the scan's escaping String error and the catch's empty residual while keeping
the performed allocation/throw labels. The unchanged paired
source/type/lowering/runtime/public-C7 and negative-source matrix was not
replayed to make the failed aggregate green. This successful affected consumer
result complements, but does not relabel, the original failed run.

Use the focused commands in `docs/development-verification.md`. Empty vectors
without an element type, heterogeneous joins, arbitrary dynamic indices,
deep selected-shape propagation and dynamic bounds-failure APIs remain separate
work, not language bans. Profile/provider/ownership legality, authoritative
migration, target support, self-hosting and seedless release remain outstanding.

### Root Keyword match typing: development implementation

The existing C7 and typed-emitter owners now extend scalar matching to an
established Keyword scrutinee. Parameters, validated immutable aliases and
ordinary local-call results retain their current type. Literal clauses remove
values from an open domain; a fallback binder and its immutable aliases keep
those exclusions. Known literal and quoted domains stay exact in the focused
shape checks. Wildcards consume the remaining domain without a binding.
Open partial Keyword matches reject with an implementation annotation
requirement, not a language-wide totality rule. A known literal caller does not
specialize away a declared Keyword parameter's open domain.

The cold inference check starts without type/proof tables: existing loop and
guarded-index facts establish the Keyword result, and binder seeding preserves
it. A normally returning String branch is not treated as Never. The private
root-scalar demand does not establish a type or authenticate a whole callee;
it avoids demanding deep constructor ancestry for an already established root
scalar. Existing literal-index projection and variable-index deep-shape fences
remain intact. Lowering joins the current Keyword type and existing ordered
source, clause, scope and extraction records, then emits the existing
`structural-match` instruction. Runtime and C8 production code are unchanged.

The combined focused batch passed 13 tests/332 assertions; a separate final
lowering/fixture batch passed two tests/37 assertions. These overlap. The
fallback test initially had a load-time delimiter error, then passed its 15
assertions after correction. Paired fixture parsing is not execution evidence.

The one frozen source composition completed 244 assertions: 243 passed, one
failed and none errored. Test-var execution took 1,044.249 seconds plus 8.248
seconds of namespace loading at 2 GiB; all 31 observed input hashes remained
unchanged. The aggregate remains failed. Both positive extensions passed seven
function types, eight call facts, five Keyword matches and eleven clause rows.
Thirty-four guest outcomes cover parameter dispatch, local-call return typing,
ordinary if truthiness, exact literal aliases, function recur, guarded vector
scans, unmatched Keyword throws, catch recovery and normal entrypoints.
Seven fresh public C7 operations cover four positives and three altered
candidates. A stale source echo remains finite-helper-valid but fails public
reconstruction; fictional exclusions and changed extraction types also fail.
Actual extraction guards and accepted/nonempty effect graphs make those
checks nonvacuous. Both C8 whole-result verifications and three coherently
resealed graph hostiles passed.

All ten negative `.gravity` fixtures rejected. The single failure omitted the
observed outside-guard diagnostic from its allowed set: the unestablished
binder's throw payload is unknown, so the `C7-ANNOTATION` diagnostic
`:typed-throw-payload-type-required` precedes later match/index diagnostics.
The test-only expectation now includes it; no compiler change or expensive
unchanged replay is warranted. The existing direct throw kernel now checks the
exact unknown-payload rule and reason and passed 23 assertions. The combined
post-correction batch passed three tests/60 assertions, including unchanged
lowering and fixture checks. Only two test files changed after the original
composition; these checks do not turn its failed aggregate into a pass.
Other allowed diagnostic memberships are not
uniquely observed diagnostic traces. Negative `.qst` coverage is pair parity
only. The source escape fixture rejects first-class `nth`; match-binder escape
has normalized ownership checks, not an authentic source rejection claim.

Use the owning commands in `docs/development-verification.md`. Open partial
Keyword matching, additional scalar families, union joins and deep
selected-shape propagation remain separate implementation work. Quoted
Keyword domain propagation has focused coverage, but quoted-match source
composition is not exercised by this fixture. This is scoped executable
development support, not authoritative migration, profile/provider or
ownership admission, target support, self-hosting or seedless release.

### Persistent vector append: development implementation

The existing C7, C8, typed-emitter and stage-owned runtime owners implement
fixed-arity `(conj vector value)` under STD3. Established ordered element types
are extended by one value type, preserving the original vector and prefix.
Operands evaluate once in order. Never prevents the append's own allocation,
but does not excuse invalid source or operand types. Normal results retain the
1024-element bound. Count and literal-index projection consume the resulting
type; projection preserves append/source ancestry rather than inventing a
constructor. The guide gives the direct development and composition commands.

The focused affected batch passed 23 tests/1,180 assertions. Independent review
found a real C8 cumulative-accounting defect: append work was deducted locally
but omitted from the returned total. The fix retains that work through module
and producer accumulation; exact-budget and one-unit-short controls pass.
Projection controls cover prefix, appended, chained and nested coordinates.
These normalized checks are not authoritative source admission.

The single frozen source composition ended with 299 passing assertions and one
error, not a passing aggregate. Test-var execution took 980.465 seconds after
7.905 seconds of loading at 2 GiB. Both positive extensions passed seven
function types, eighteen call facts, twenty guest outcomes in total, eight
fresh C7 operations, two accepted/nonempty C8 whole-result checks and four
resealed graph hostiles. Seven negative `.gravity` fixtures rejected before
the error. Negative `.qst` coverage is paired parity only.

The oversized source fixture reached C6's fragment bound first:
`C6-VERIFY/:fragment-root-form-bound`, maximum 1024 forms, observed 1036.
This is an authentic upstream rejection, not a C7 append-extent rejection.
The 1024-to-1025 C7 boundary has normalized kernel coverage. The subsequent
Never/wrong-family and missing-allocation-allowance cases were initially unrun.
A separate two-source check passed all six assertions in 159.817 seconds after
7.882 seconds of loading at 2 GiB: the former rejects at C7 despite the other
operand's Never type; the latter accepts C7 and its verifier but rejects at C8
with the function-first undeclared-allocation diagnostic.

The test-only extent handler now recognizes exactly the original fail-closed
C6 bound and rethrows unexpected diagnostics. Eight synthetic handler controls
plus fixture and lowering checks passed two tests/84 assertions. These overlap
earlier focused checks and do not convert the original failed aggregate into
a pass. No positive composition or oversized source was replayed solely to
refresh an aggregate label. Only the test file changed after that run; compiler
and paired fixture bytes were unchanged for the separate remaining checks.

Whole-shape consumers retain the one-established-vector-shape fence. Growing
vector recur types and appended-vector iteration origins remain separate
implementation frontiers, not language bans. Collection/provider/profile and
ownership completion, target support, authoritative migration, self-hosting
and seedless release remain outstanding.

### Atomic value equality: development implementation

The existing C7, C8, typed-emitter and stage-owned runtime owners implement
direct binary `=` for independently established Nil, Bool, Character, String,
Symbol and Keyword operands under STD2/STD4. Same-family values compare by
value; different supported families compare false. String comparison preserves
the exact scalar sequence without normalization, case folding or locale rules.
Symbol and Keyword comparisons retain their complete namespace and name.
Normal results are Bool. Operands evaluate once in order; Never transfers
without invoking equality or excusing the other operand's legality. Equality
adds no allocation, capability or error effect; operand effects remain visible.

Supplementary Characters retain their original C2 occurrence kind through
C3/SH04 and C6 projection, quoted DATA, typing and lowering. The existing
four-field representation does not turn an identical ordinary source Map into
a Character. Occurrence-derived kind precedes recursion and identity creation;
the global host-value classifier is unchanged. C2 owns scalar decoding and
codepoint/text consistency. Downstream representation guards do not duplicate
that parser or establish a new authority channel.

The combined focused batch passed 23 tests/1,936 assertions, covering the new
type/effect/lowering/runtime behavior and prior quote, map, vector-append,
integer-comparison and throw regressions. A separate reader/fixture batch
passed two tests/46 assertions: both accepted extensions contain six childless
Character forms, five supplementary and one BMP. The final affected batch
passed four tests/188 assertions. These overlap earlier checks, rather than
forming an additive coverage total. A subsequent comment correction and
deletion of one tautological source-definition assertion required no replay.

The single unchanged-candidate source composition passed all 405 assertions.
Test-var execution took 923.743 seconds after 8.121 seconds of namespace loading
at 2 GiB. Both extensions passed nine function types, fifteen call facts and
thirteen equality applications (eleven normal, two Never). Twenty-eight guest
executions cover distinct equal-content Strings, NUL and supplementary text,
composed/decomposed inequality, atomic and cross-family values, Character
literal/quote/vector projection, ordinary Map preservation, parameter shadowing
and both operand-error orders with retained payload identity.

Nine fresh public C7 lowering operations include five altered-evidence cases.
The Character case changes source-identified quote evidence, not every equal
host-map payload; it establishes rejection of inconsistent Character evidence,
not an isolated codepoint/text theorem. Two accepted, nonempty C8 graphs pass
whole-result verification; four resealed graph mutations reject. All nine
negative `.gravity` fixtures reject arity, numeric/vector/map/function/unknown
operands, result annotation or invalid operands despite Never. Negative `.qst`
coverage is paired-source parity only. All 35 compiler/test/fixture inputs and
the Git base remained unchanged throughout the source run.

After that run, the routine LOC gate found the C6 assembly adapter at 253 lines.
Removing two leading blank lines and one blank separator brought it to 250;
all nonblank lines and their order are unchanged. The source result remains
attributed to the original pre-format bytes, not a relabelled proof of new
bytes. Four post-format Character/map checks passed all 314 assertions, and
independent review carries acceptance across this whitespace-only delta. No
expensive source composition was replayed for it.

Review also found two private-IR rejection-path defects: comparing an unknown
value before classifying it could invoke opaque equality, and building a key
set before checking key families could invoke opaque hashing. Existing
primitives now reject those payloads without callbacks; focused sentinel
controls pass. No source host-object support or seed primitive was added.

Use the owning commands in `docs/development-verification.md`. Numeric,
collection and function equality typing, variadic or first-class `=`, complete
standard-library/provider/profile support and other targets remain separate
work, not language prohibitions. Ordinary integer runtime compatibility is
preserved without claiming new numeric typing. Authoritative migration,
self-hosting and seedless release remain outstanding.

### Persistent List prepend: development implementation

STD3 now specifies binary List `conj` as prepend, with ordinary left-to-right
argument evaluation, original-value preservation and explicit allocation and
storage/lifetime obligations. It does not mandate universal physical sharing
or extend variadic/arbitrary-sequence behavior.

The C7 focused batch passed two tests / 271 assertions: 196 new List-prepend
checks and 75 existing vector-append regressions. They cover homogeneous,
positional and bare List result types, heterogeneous/Nil positional values,
Never, rejected member/source/binding mutations, computed-result depth/width,
exact and one-less work, malformed argument containers and large arity.
Early shape/budget checks now precede argument-type collection. The first two
batches stopped at syntax errors in the new test; a third reached an
unsupported `dissoc` in the new Gravity helper, with zero passing assertions
and two errors. The helper now carries its result type directly, avoiding the
unnecessary temporary field without expanding the seed builtin surface.
Those failures remain separate from the passing batch.

C8 reconstruction/allocation and typed lowering/runtime component checks now
pass. The stable combined focused batch passed 72 tests / 5,351 assertions,
including the existing traversal and vector-append regressions, malformed
children, exact/one-less budgets and paired fixture shape checks. Counts overlap
the earlier component batches; they are not additional independent coverage.
Earlier delimiter errors and a C8 call-join dispatch failure remain recorded as
failed attempts, not passing evidence.

The single affected source composition passed 1 test / 245 assertions at the
unchanged 2 GiB heap. Paired Gravity/QST sources exercise eight functions and
24 calls, four prepend descriptors (three normally invoked and one aborted),
32 guest execution cases, ordered reverse-and-join, old-input preservation and
original throw identity. It checks fresh public C7 lowering gates, independently
reconstructed C8 graphs and allocation facts, exact/one-less C8 work, hostile
mutations, eight C7 rejection families and the missing allocation allowance.
The selected test took 1,026.331 seconds after 8.643 seconds of namespace loading;
these timings are development observations, not benchmarks or speedup evidence.

Source homogeneous-member coverage is String; other supported atomic types
and finite/bare results have focused kernel coverage. Bare-List head knowledge,
homogeneous-to-bare compatibility and compound return annotations remain
separate frontiers. Profile-specific representation, provider and ownership
legality, other targets, authoritative migration, self-hosting and release
gates remain outstanding. This is accepted executable development behavior,
not completion of those broader contracts.

### Module-local literal initialization: development implementation

The typed emitter and `gravity.runtime` now execute supported module-local
literal definitions once in source root order before the selected entrypoint.
Each closed-plan execution creates a fresh module instance. Namespace reads
use its private binding-ID pool through calls and `recur`, independently of
lexical shadowing. Direct expression/function evaluation does not initialize
a module or acquire a pool from caller-supplied locals or function tables.
Plans without initialization retain their existing startup behavior.

The emitter joins the complete canonical root/definition census, including
unused initializers, with C5 bindings, source identities, types and C6
evaluation metadata. Function ordinals are not module-root ordinals. Unquoted
Vector and supported Map initializers remain evaluated constructors; quoted
DATA stays inert. C8 owns namespace initialization allocation and legality;
reads add no allocation. Runtime manifest checks do not replace source/type
verification, and reported constructor occurrences are validation data, not
allocation-event measurements.

The focused runtime/catch batch passed 2 tests / 142 assertions. The corrected
emitter kernel passed 1 / 55, including loader assertions that overlap other
checks. Its first attempt remains 37 passes / one failed residual-budget
assertion; the corrected check compares semantic products and explicitly
checks the remaining budget. Independent review also corrected scoped C6
branch-region handling and reserved known summary work before child lowering.

The sole source composition passed 1 test / 126 assertions (120 composition
checks plus six loader checks) at 2 GiB, taking 370.889 seconds of selected
execution after 9.561 seconds of namespace loading.
It preserves W5's exact literal definition and getter in the accepted G/Q
pair, verifies C8 before six closed executions, and rejects an unused call
initializer at the emitter and a missing allocation allowance at C8. It builds
four fresh source products and reuses accepted products for coherent type/effect
mutations. Negative
QST fixtures have byte-parity coverage, not additional source constructions.

Initializer calls, references/aliases, mutation, imported initialization and
other arbitrary initializer code remain unsupported. This initializer-only
evidence does not establish Vector value equality (covered separately below).
Full W5's current empty effect declaration, profile/provider/ownership and
other target gates, authoritative migration, self-hosting and release remain
outstanding. This is executable development progress, not completion credit
for those broader boundaries or a performance benchmark.

### Module-local literal Keyword-Set initialization: development increment

Three existing functions in the typed emitter and runtime extend the module
literal boundary to direct root Set definitions containing distinct literal
Keywords, including empty Sets. C7/C8, public interfaces, runtime function inventory,
IR operations and the binding-ID pool are unchanged. Nested evaluated Sets,
computed members, other member families and arbitrary initializer code remain
outside this increment; quoted DATA retains its old behavior.

Before initialization, exact Set IR and its complete Keyword domain and
uniqueness are validated without constructing a guest Set. For `n` members and
`U` existing UTF-16 representation units, the logical census reservation is
`1 + 3*n + n*(n-1)/2 + (n+1)*U`. The extra `n` beyond the existing constructor
reservation pays the later finite-type member join, whose shape/cardinality
guards run before traversal. Width 1,024 and aggregate module allowance 65,536
remain separate limits. Failed reservations preserve lower-bound meaning;
these units do not establish provider CPU, heap or physical allocation bounds.

The first executed focused batch passed ten tests/1,045 assertions: 202 new
and 843 adjacent assertions. Two test-input corrections preceded execution:
an obsolete supported-Keyword rejection moved to String, and normalized emitter
inputs gained the required source value and coherent synthetic identities.
Earlier unexecuted candidates remain in Git; no failed JVM epoch is claimed. Runtime
instrumentation observed no guest constructor calls during census or rejection
of a later malformed initializer/read, one call for repeated reads of one
binding, two calls for two bindings, and construction again on a fresh module
execution. These are helper-invocation observations and validated constructor
occurrences, not physical allocation-event measurements.

The owning-C2 paired parser check passed one test/12 assertions, including
exact W5 definition-text reuse. The one changed-source composition passed
one test/59 assertions (53 composition plus six existing loader checks) in
137.992 seconds selected execution after 11.870 seconds namespace loading
at 2 GiB. Its exact safety-outcomes definition and unused empty Set produced
two initialized bindings before the single main entry, whose count and membership
queries used three pooled reads and returned `[4 true false]`. The observed guest trace was two constructor
calls followed by entry. Three definitions/roots, one function, three calls,
two Set initializer instructions and one body Vector retained their current
source/type/manifest records. C8 preserved namespace and Set allocation while
the reads added no direct allocation. Runtime function inventory remains 54.

Both fixtures passed original C7/C8 verification and an observed public C7
assembly gate. The String-Set initializer negative then rejected at lowering
with `P15S23Q002` and `:unsupported-typed-module-initializer`, without runtime
entry. Its existing wrapper missing-fact/remediation fields remain in the
diagnostic; the assertion checks primary ID/reason, not an exact-only record.
Q is parser-only, seed controls are zero, and fresh C6 development sessions
plus same-catalog replay are not fresh public C6 verification. Inclusive
timings are not phase-isolated performance evidence. No source replay or
failed executed epoch occurred in this increment.
Separate language-boundary checks passed six tests/11 assertions; LOC validation
passed 2,021 source files, zero exceptions, maximum 250 physical lines.

Whole W5 namespace legality, including its current empty effect declaration,
other targets, authoritative migration, SH-07, self-hosting and release gates
remain separate. Exact definition-form reuse in an allocation-allowed fixture
does not establish an unchanged full compiler-module build.

### Module-local literal String-Set initialization: development increment

The same three existing emitter/runtime initializer functions accept direct
root Sets of distinct homogeneous String literals alongside Keyword literals.
All members are validated and frozen as one actual family before content work
or comparison. Native String UTF-16 units use `count` without formatting or
normalization; Keyword and empty-Set work/results retain the previous path.
The existing aggregate reservation, root-only scope, full preflight-before-
construction and binding-ID pool remain. No C7/C8, public API, IR operation,
helper, runtime function inventory or module manifest change was introduced.

The finite-type join checks shape and bounded matching cardinality before its
prepaid member-type reads, using the actual family of the first previously
validated IR value. It rejects crossed String/Keyword declarations. This is
the existing census-to-type-check lexical contract over the same immutable IR,
not standalone authentication of arbitrary caller-supplied initializer IR.

The first focused batch passed 13 tests/1,364 assertions with no failures or
errors: 307 new, 1,045 adjacent semantic assertions and twelve affected parser
assertions. Cases cover escapes, supplementary and normalization-distinct
Strings, duplicate/mixed supplied records, exact/short and aggregate work,
type/source forgeries, constructor ordering, unused definitions and pooled
reads. A direct probe confirms String content does not pass through `str`.
These are logical work/helper-call observations, not physical allocation,
Unicode-scalar or grapheme counts, provider CPU or peak-memory guarantees.

The former String rejection pair moved byte-for-byte to accepted, while active
rejection checks now use Character initializers. Historical String rejection
evidence remains true at its original revision. The affected old parser ran;
the unchanged expensive Keyword source positive was not replayed.

The new paired parser passed one test/17 assertions. One affected source
composition then passed one test/61 assertions (55 composition plus six loader
checks). It reuses B14's exact ten-String diagnostic Set definition in a tiny
allocation-allowed module. The runtime returned `[true false 10]` for membership
hit, miss and count, with one constructor-function invocation before entry.
Two definitions/roots, one function, three calls, one Set initializer, one body
Vector, three pooled reads and two String-membership descriptors were checked.
Both G fixtures passed original C7/C8 and the assembly's public C7 verification;
the Character fixture then rejected at lowering with `P15S23Q002` and
`:unsupported-typed-module-initializer`, retaining the wrapper's existing
missing-fact and remediation fields. It did not enter the runtime.

The source execution envelope took 147.959 seconds after 11.491 seconds of
namespace loading at 2 GiB. The two fixtures' inclusive C5 construction times
were 30.507/24.379 seconds, fresh development C6-session construction
38.022/32.840 seconds and same-catalog replay 3.354/3.143 seconds. These
observations distinguish product construction from startup; nested work is
included, so they are not independent phase benchmarks. Public fresh C6 was
not invoked, Q remained parser-only and no seed control ran. Each first
invocation passed without a failed/correction/replay epoch.
Separate language-boundary checks passed six tests/eleven assertions; LOC
validation passed 2,021 source files, zero exceptions, maximum 250 lines.

Mixed, Character, computed and nested evaluated Set initializers remain outside
this increment. Full B14/backend execution, String-Set equality, original
namespace effect legality, authoritative migration, SH-07 completion,
self-hosting and release remain separate gates.

### Finite persistent Vector value equality: development implementation

Binary `=` now compares actual persistent Vector values when both operands
have independently established finite Vector types with complete ordered
element types. Members may recursively be Vectors or supported scalar and
nil-bearing scalar alternatives. Counts, positions and corresponding stored
values determine the result, not constructor ancestry, type sameness, identity
or physical sharing. Nested Vector-versus-scalar values compare false after
both complete supported domains validate. Numeric members, unknown/bare
Vector types and other collection domains remain implementation frontiers,
not new language prohibitions.

C7 records a distinct collection-owned equality variant through the existing
core compatibility binding. C8 independently reconstructs its premises using
fresh, measured owner-local type analysis under the shared work budget. The
typed emitter preserves once-only left-to-right non-tail operands and known
work on rejected paths. Runtime validates both complete operand domains before
a length or earlier value mismatch can return false. The query adds no guest
allocation; original constructors retain their C8 allocation effects. Internal
host traversal allocation is not characterized by that claim.

Focused type/runtime checks passed 5 tests / 841 assertions, including existing
atomic, nil-bearing and initialization regressions. C8 passed 3 / 233. Lowering
first passed 2 / 153; independent review then corrected rejected-work carry,
and its changed Vector-only selector passed 1 / 96. These batches overlap in
loader and compatibility assertions. The original type/runtime attempt remains
zero passes / one loader error from unsupported `concat`; the bounded-stack
successor preserves left-to-right and low-coordinate traversal order.

The sole source composition passed 1 test / 316 assertions (310 composition
checks plus six loader checks), with 452.945 seconds of selected execution
after 9.214 seconds of namespace loading at 2 GiB. Four fresh source products
cover accepted Gravity/QST and actual numeric-member and Map-domain Gravity
rejections. Three tracked public C7 assembly/forgery gates are separate from
upstream verification. Sixteen closed executions follow accepted C8 analysis
and passed verification, preserving W5's exact modes definition/getter and
checking equal, reordered, changed, shorter, empty and nested Vector values.
Accepted products are reused for coherent type/effect and graph mutations;
the source effect analysis also checks exact and one-less work budgets.

Negative QST files have byte-parity coverage only. Never, hidden unsupported
payloads and lowering failure-work cases have focused kernel coverage, not
additional source constructions. Constructor-occurrence metadata is not
allocation-event instrumentation; cross-instance nonidentity is not a
no-sharing theorem. Scalar integral equality is covered separately below;
general collection equality,
full W5's empty effect declaration, provider/profile/ownership and other
targets, authoritative migration, self-hosting and release remain separate
gates. These results are executable development evidence, not a benchmark or
completion of those broader contracts.

### Binary unit steps in bounded loops: development implementation

The existing C7, C8, typed emitter and runtime now support exact binary
`(+ counter 1)` in the previously declared zero-initialized strict bounded-loop
domain. The counter must be the original Integer slot, the second source
operand must be the Integer literal one, and the complete incoming-edge census
must independently establish every step as authentic `inc` or exact unit
addition. Extent remains at most 1,024. This proves the specific mathematical
successor; it does not choose a general Integer representation or overflow
fallback. The semantic owner is `gravity.math`, distinct from the
`gravity.math.core` layer-0 owner and `gravity.core/+` compatibility binding.

All-inc evidence retains its v1 representation. Any addition edge selects the
new finite v2 unit-step invariant; mixed inc/addition descriptors, constraints,
facts and complete ordered ledgers remain distinct and are reconstructed by
each consumer. Existing nth/Vector-get consumers accept that exact invariant,
not generic Integer evidence. Binary lowering and runtime preserve both ordered
operands, transfers and runtime slot/unit/range guards. Never is not an
induction premise, and no new range solver or check elision was introduced.

Review caught two bounded-work defects before source integration: oversized
step records could be scanned after an eager failed closure check, and a new
module-wide classification scan preceded its base reservation. Dominant shape,
cardinality and budget guards now precede those scans; selected work is charged
once. Direct hostile no-traversal controls and existing S/T/Vector-get consumer
regressions passed. Runtime passed 2 tests / 80 assertions, including 12 repeated
loader checks. Corrected C7 passed 4 / 244 and consumers/lowering 8 / 657;
these overlap prior development batches and are not disjoint coverage totals.

The first actual source composition failed: six loader passes, one failure,
one error; the first Gravity C7 guard stopped all downstream work. An exact raw
inference prefix then failed 1 pass / six failures, establishing resource-limit
status in round six with -788 remaining work units. A secondary Unknown throw
diagnostic had masked that original failure. Neither result was a source pass.

The bounded correction advances only an independently computed normal type
from an accepted v2-bound nth/get into exact immutable lexical records whose
initializer is that node. Selection and binding work is reserved first; no
expected-type seed, cache or limit increase is involved. Existing v1 paths pay
no added charge. Resource-limit diagnostics now retain priority over secondary
Unknown diagnostics; all non-resource priorities remain unchanged. The lasting
empty-descriptor two-loop control passed 1 / 12; changed source-prefix and
diagnostic-priority controls passed 2 / 41; legacy checks passed 3 / 107.
Earlier test-construction failures, malformed conditional, adapter expectations
and unknown-selector rejection remain failures, not retroactive passes.

The changed source composition passed 1 / 193: 187 composition assertions plus
six existing loader assertions. Five fresh products cover the accepted G/Q
pair and three Gravity negatives; negative QST files have byte parity,
not extra compilation. Both accepted products passed fresh C7/C8 hard guards
before 16 function-table activations (not closed-plan execution). The four
functions exercise an all-addition loop, both mixed inc/addition branches,
stored Keyword errors/catches and the ordinary entry. Ordered binary IR,
complete actual source/backedge facts, get consumption, path-projected function
tables, C7 identities, C8 graphs and results matched across G/Q. Exact/one-less
C8 work and dependency forgery rejection passed.
All three negatives rejected with the intended reason checked across their
full diagnostic vectors; passing vectors were not separately serialized.

Selected construction, verification and execution took 530.465 seconds after
11.632 seconds of namespace loading at 2 GiB. There is no per-stage attribution
or speedup claim. Source-level all-inc-v1 coverage was not repeated; preservation
is supported by focused component regressions. General addition, commuted or
non-unit steps, arbitrary dynamic bounds, ordinary-call induction, termination,
TCO, full W5/SH-07, other targets and authoritative migration remain separate.
No self-hosting, seedless or release status is promoted by this development slice.

### Guarded loop Vector get: development implementation

The existing C7, C8 and typed emitter now support two-operand persistent
Vector `get` in the existing bounded strict-loop domain: a nonempty homogeneous
Vector, original zero-initialized Integer counter, complete unary `inc`
backedges, and access beneath the true edge of `< counter (count same-vector)`.
Immutable aliases retain the same source-linked Vector origin; equal-shaped new
Vectors do not. Get-specific binding, source, descriptor, constraint, call-fact
and ledger joins remain distinct from Map lookup and `nth`. After independent
consumer reconstruction, lowering emits the existing checked `nth` instruction
with once-only Vector-then-index operands. Runtime guards and runtime code are
unchanged. C8 validates the complete ordered nth/get ledger census; local
binary lowering retains its reservation and completed operand work on failure.

Focused checks passed C7 2 tests / 140 assertions (83 new get checks and 57
existing loop checks), consumer/lowering 4 / 339, and routing/paired-fixture
checks 2 / 28. A later loader check repeated ten fixture assertions; these
batches are overlapping development feedback, not disjoint coverage totals.
Earlier failures remain recorded: two adapter expectations used an unsupported
source shape; an emitter test failed lexical loading; and three budget
expectations confused outer rejection with local helper accounting. Corrected
focused successors passed without broadening the language subset.

The affected source run completed FAILED: 1 test, 99 passes, two failures and
zero errors, comprising 93 passing composition assertions, two first-diagnostic
label failures and six existing runtime-loader passes. Both accepted source
extensions passed fresh C7/C8 checks before eight function-table activations:
normal traversal, a stored Keyword throw, typed catch and the ordinary entry.
All six physical function paths and complete path-projected Gravity/QST tables
matched. Alias/origin, complete backedge, checked lowering, effect/error,
exact/one-less budget and resealed dependency-forgery rejection checks passed. Six fresh
source products were constructed: the accepted pair and four Gravity
negatives; negative QST files have byte-parity coverage, not extra compilation.

All four negative rejected-status assertions passed. Wrong-origin and
outside-loop first labels matched. Heterogeneous and nonstrict variants first
reported `C7-ANNOTATION / :typed-throw-payload-type-required` with an Unknown
payload, rather than the anticipated get/range labels. The two expectations
were corrected to those observed first records without replaying unchanged
products. Complete diagnostic vectors were not retained: this does not prove
an isolated get-homogeneity or strictness rejection cause, nor a fresh green
composition after the expectation edit. Focused proof-boundary controls and
independent review support this qualified development increment. An earlier
source invocation was interrupted without counts or a final receipt and has
no validation credit; it is not a claim of zero construction work.

Selected source construction, verification and execution took 606.875 seconds
after 9.866 seconds of namespace loading at 2 GiB. The interval is not separated
into individual phases, scheduler overhead or verification-product cost.
Representation measurement/redesign remains a bounded separate follow-up;
do not replay unchanged products merely for metrics or a green label.

General dynamic/literal-only Vector access outside this proof, heterogeneous
results, explicit defaults and absent/out-of-range result policy remain outside
this increment. Mixed nth/get source-module coverage is not established by the
separate regressions. This is not W5, SH-07/SH-08, target, authoritative migration,
self-hosting or release completion. Use the direct component commands in
`development-verification.md`; fresh authority evidence serves an explicit
stable-revision admission decision, not ordinary edits.

### Private ordinary direct-self inference: development implementation

The C7 engine now infers one monomorphic value signature for a private,
fixed-positive-arity function with one ordinary direct self-call. The complete
direct-use census must exclude exports, value escapes and unsupported captures.
At least one independently normal, nonrecursive entry must exist, and all such
entries must agree on the entire parameter vector. A separate cold return
witness comes from actual return positions, not from a recursive result or a
caller's expected type. Current supported domains are the bounded atomic and
homogeneous-List types used by the declared path, excluding generic Integer
and positional Vector inference. This is a language implementation increment,
not a promotion of SH-07, SH-08 or W5 completion.

Cold inference starts without recursive-result-dependent facts and does not
turn an unknown recursive result into a seed. A callee's Never result is not
an invocation guard; definitely nonnormal operands prevent invocation.
Source census, cold passes, quote reconstruction and return traversal share
the existing cumulative budget. The derived parameter premise supplies type
and immutability only: homogeneous-List `first` still requires the original
same-binding, mutation-free `empty?` branch proof. C8 and lowering reconstruct
the finite source/type joins independently; public verification recomputes the
current result. Effects and escaping errors are solved separately from bottom.
No runtime change, new call IR, authority channel or recursive call-to-recur
conversion was needed.

Focused development checks passed the entry census/extraction, dependency cut,
cold inference, return-witness, quote-budget and guarded-head cases. The final
C7 projection/regression batch passed 5 tests / 157 assertions: 19 new projection
checks, 81 legacy primitive-family, 35 declared-self, five diagnostic and 17
source-owner checks. C8 context/head checks passed 29 new assertions alongside
32 existing declared-self effect checks. The new inferred variant's effect/error
fixed point separately passed 1 / 20. Lowering passed 2 / 61: 42 new finite
context/call/head checks and 19 existing declared-self checks. These batches
overlap prior component runs and are not a count of newly supported behaviors.

Earlier attempts remain failures: cold-result comparison included budget
telemetry; a census expectation omitted an intrinsic call; the head case exposed
a cold operator-fact gap and stale fixture control roots; an effect-role fixture
omitted call facts; and a projection test failed loading because of a delimiter.
The cold operator correction accepts only the current independently checked
intrinsic descriptor, with all evaluated operands still independently known.
The corrected fixtures use the owning call-fact producer. A precharge review
also moved the census cardinality rejection before eager per-call joins, and
scalar entry payloads are rejected before `count`. Changed focused successors
passed; failures were not relabeled and unchanged expensive proofs were not
replayed.

The sole affected source composition passed 1 test / 263 assertions at 2 GiB:
257 composition checks and six existing runtime-projection loader checks.
It constructed five fresh products: the accepted Gravity/QST pair and Gravity
entry-conflict, return-conflict and missing-effect negatives. The negative QST
files have exact byte-parity coverage, not additional compilation. The accepted
eleven-function module contains genuinely unannotated private `join-parts`,
three declared homogeneous-List producers, four normal entries, two non-invoking
entries and one self-call. Its independent return witness is the current String
parameter. Fresh C7/C8 checks passed before twelve closed executions: empty,
one- and two-element traversal, a deeper String throw and two operand-transfer
cases across both files. The acyclic caller's real Bool function `recur` remains
distinct from the private ordinary self-call.

The source checks establish allocation from `str`, operand effects without
borrowed callee errors on non-invoking calls, exact/one-less C8 work behavior,
three additionally tracked public C7 assembly/forgery gates, and fresh public C8
rejection of a coherently inflated equation-valid error graph. That hostile
case is not a claim of isolated rejection causality. All 22 physical function
paths and the complete path-projected Gravity/QST function tables passed
comparison for this AT run. This does not recover missing tables or change the
failed AS source result recorded below.

Selected construction, verification and execution took 702.626 seconds after
9.805 seconds of namespace loading. Existing timing did not separate the
selected interval further; this is neither scheduler attribution nor a speedup
claim. The bounded product-representation follow-up remains separate from
semantic edits and must not replay this unchanged composition just for metrics.

Exported/escaping functions, mutual or indirect recursion, arbitrary captures,
non-core named helpers and try/loop/recur inside the recursive body, imported
recursive producer inference and expanded recursive value domains remain
unsupported here. No termination, TCO, native or other-target support,
authoritative module migration, self-hosting, seed retirement or release claim
follows. Use the direct commands in `development-verification.md`; fresh
authoritative evidence is required only when explicitly admitting a stable
module revision.

### Private indexed ordinary recursion: development implementation

The unchanged W5 `w5-se-member-from?` body now works in a bounded development
family: a private three-argument function, an independently established
seven-element Keyword Vector and Keyword needle, and normal external entry at
the source Integer literal zero. Complete call, export, escape and mutation
censuses remain required. The separate indexed proof does not widen the old
general recursive-domain whitelist or borrow a loop/recur premise.

C7 derives an independent false return witness and the same-binding induction
`0 <= index <= count(values)`. The false equality branch supplies the strict
bound for `get`; the self call preserves the original Vector and needle and
uses source binary `+ index 1`. C8 and the typed emitter independently
reconstruct these premises; C8 derives effects/errors. Runtime activation uses the
current function table and freshly bound actual parameters, clears stale
caller metadata, and restores the caller's private metadata after return or
throw. Calls retain ordinary three-field call IR, depth 256 and no TCO.
`count` still executes. Allocation effects remain explicit.

Owning changes are C7, `effects.gravity`, the typed emitter and `runtime.gravity`.
Focused checks cover entry inference, proof/rejection boundaries, C8, lowering,
actual runtime activation, retained work on failure, malformed metadata and
legacy recursion. Recorded passing runs include 4 tests/66 assertions for C7,
3/49 for C8, 1/76 for lowering/runtime, 5/288 for legacy runtime, 4/52 for
rejection/budgets, 4/131 for affected consumers, and 2/35 for source preflight.
These runs overlap and must not be summed into a coverage score.

The affected source composition passed 1 test/163 assertions: 157 composition
checks plus six runtime-source loader checks. It constructed eight fresh
products, exercised three additionally tracked C7 assembly/forgery gates and
ten function-table activations, and compared accepted Gravity/QST identity,
effect graphs, results and complete path-projected function tables. These
products and tables were not serialized as retained evidence. The six Gravity
negative vectors each contain exactly one C7 annotation diagnostic: zero entry,
step, needle and Vector preservation, public visibility, and absent normal
entry. The public case uses the legacy recursive-shape reason, not a dedicated
privacy diagnostic. These are inference-family boundaries, not declarations
that every such program is language-illegal. QST negatives have separate
byte/parser/census checks only, not source-compilation credit.

Selected source work took 736.875 seconds after 9.620 seconds of namespace
loading with a 2 GiB heap. Construction cost is not scheduling overhead or a
speedup claim. Post-run test edits added six exact diagnostic-vector assertions;
a separate read-only two-assertion oracle checked all six retained vectors and
the complete case set. The original source run remains 163, not 169. An initial
oracle command lacked the test classpath and failed before assertions. Earlier
component fixture/proof-shape and loader failures remain failed attempts; the
passing results do not relabel them. No unchanged expensive source replay was
needed for the tighter oracle.

The independent auditor accepted this scoped implementation and actual source
result. Component exact/one-less budgets are not whole-source budget evidence.
The `vector?` wrapper, arbitrary Vector/index domains, mutual recursion,
termination, full W5, native/other-target support, authoritative migration,
self-hosting, seed retirement and release remain outside this increment.

### Vector family classification: development implementation

Unary `vector?` now classifies independently established closed value types
under STD3: Vector values yield Bool true and supported non-Vector values yield
false. The operand evaluates once in non-tail position; its effects and
transfers survive, and Never prevents invocation. Classification does not
evaluate stored payloads, mutate values or infer member, length, index or
refinement facts. Unknown operands remain outside this implementation frontier,
not a new language-wide prohibition on future polymorphic predicates.

C7, C8 and the typed emitter independently check the current source binding,
unary operand, descriptor, fact and ordered ledger joins. C7's catch/throw
consumer charges deep operand-type validation against its remaining budget and
retains work on rejection. The emitter's bounded producer-row census rejects
orphan rows rather than filtering them away. Runtime uses the existing unary
builtin-call representation and shallow classification after evaluation. The
existing private activation state and public compiler interfaces are unchanged.

The final focused batch passed 3 tests/358 assertions: lowering/runtime 114
(including six actual runtime-source loader checks), C7 235, and unannotated
cold-wrapper inference nine. The earlier C7/C8 batch passed 2/276, including
47 independent C8 checks. These runs overlap; do not sum them into a coverage
score. A 1/19 fixture preflight parsed all five Gravity/QST pairs and checked
both unchanged W5 bodies. Earlier failed runs remain failed: one mechanical
Never-tag mismatch, then four orphan-row lowering checks. Legacy kernels passed
within those failed aggregates; they do not make the aggregates pass. The
independent audit separately found and closed the remaining-budget accounting
gap before the final batch.

One affected source composition passed 1 test/239 assertions: 233 composition
checks plus six runtime-source loader checks. Seven fresh products cover four
positive chains (two programs in both spellings) and three Gravity negatives.
The pure program preserves `w5-se-member-from?` and `w5-se-member?` unchanged,
with independently inferred Vector7/Keyword entry facts and a separate Keyword
false classifier. The second program covers nested-Vector and Never operands
through existing typed catch, without widening cold-handler inference.

The run exercised five additionally tracked public C7 gates, including a forged
compile, independent C8 build/verification and graph-forgery rejection, and
sixteen actual function-table activations. This is not `execute-closed-plan`.
Paired typed identities, effect graphs, results and complete emitted functions
modulo source path were compared; full positive products/tables were not
serialized as retained evidence. The C7 tamper changes one descriptor field,
not a coherently resealed all-field forgery. Component exact/one-less budgets
remain component evidence, not a whole-source budget claim.

All three actual negative vectors include the intended primary diagnostic:
`vector-predicate-operand-type-required` for Unknown and
`vector-predicate-arity-mismatch` for zero/two operands. Each also retains a
secondary `unsupported-or-unresolved-core-node-type` diagnostic. Do not erase
that secondary output, assign its node kind without evidence, or claim a clean
single-diagnostic rejection. QST negatives have byte/parser parity only.
The post-run exact-vector oracle added three future source assertions; the
executed result stays 239, not 242. A separate two-assertion read-only check
matched the three complete retained vectors, without a source replay.

Selected source work took 559.381 seconds after 9.242 seconds of namespace
loading with a 2 GiB heap. This does not separate product construction from
verification or scheduling cost. Independent review accepted the scoped code
and actual results. No general heterogeneous W5 wrapper, full W5, broader cold
inference, native/other-target support, authoritative migration, self-hosting,
seed retirement or release claim follows. Direct development commands remain
in `development-verification.md`; ordinary edits require no admission-system
maintenance or fresh authority proof.

### Integer family classification and ordinary entry inference: development implementation

Unary `integer?` now classifies supported independently established closed
operand types under STD5, with normal Bool results, once-only non-tail operand
evaluation and preserved effects/transfers. Never prevents invocation. Its
`gravity.core` compatibility spelling has `gravity.math` semantic ownership,
not conversion or numeric-proof authority. The existing nine Integer/fixed-
integer type facts are normalized typing cases, not native-width execution
evidence. Unknown remains an implementation frontier, not a language-wide ban.

The initial focused batch passed 10 tests/1,608 assertions, including the new
C7, C8 and lowering/runtime kernels and existing vector-predicate and integral
ordering regressions. Independent review then found two missing reason sets
in the containing throwing-call consumer: integer-predicate exhaustion could
lose its resource classification, and semantic rejection could lose prepaid
work. The narrow fix and a separate owning-consumer check passed 1/19 and were
independently accepted. These are separate results, not a relabeled first run.
Fixture preflight passed 1/17 for all five Gravity/QST pairs and the unchanged
W5 `w5-se-nonnegative-integer?` body.

The first affected source attempt at development base `e61b4fd9` FAILED with
one test, eight passes, one failure and one error. Six runtime-source loader
checks and genuine C5 acceptance/C6 verification passed. At the first pure
Gravity program, C7 retained Unknown for the unannotated W5 helper parameter
and the private Vector classifier. Its complete diagnostics include an
integral-ordering mismatch `[Unknown, Integer]`, two
`integer-predicate-operand-type-required` rejections and a secondary
`unsupported-or-unresolved-core-node-type`. The eight passes do not make the
aggregate green.

The shared constructor internally invokes C7 verification even for a rejected
typed product, but this attempt did not assert or serialize that result before
the prerequisite exception. Its verification outcome is uncredited, not
described as an unperformed call. Lowering, C8 and runtime entry invocations
did not run; the other three positive chains and all three intended source
negatives were unrun. Selected work took 106.992 seconds after 9.779 seconds of namespace
loading at 2 GiB; this is not a construction/scheduling cost breakdown.
The actual failure calls for tracing the ordinary acyclic private entry-type
inference path. Do not annotate the unchanged helper, add an artificial
recursive companion, infer operand types from the predicate result, widen
polymorphism or replay unchanged source products merely to hide that gap.
The source result remains failed until a concrete changed implementation and
its affected checks establish otherwise. No full W5, migration, self-hosting,
native/other-target or release claim follows.

Independent static tracing narrowed the gap to the existing call-constraint
scan, not a missing general acyclic inference subsystem. In
`sh08-ft-call-constraints-with-context`, an unresolved integral-ordering call
returns the entire scan before later normal local-entry calls can contribute
their independently known literal argument types. Here the helper's `>=`
precedes main's entry calls in source order. The bounded fixed-point machinery
already consumes those local-entry joins. The proposed correction continues
genuinely unresolved ordering through that existing path while retaining final
unresolved/concrete-error rejection, monomorphic conflicts and cumulative work;
it does not obtain an operand type from `integer?`'s Bool result.

That narrow correction is implemented and passed a separate focused batch of
3 tests/349 assertions: the new call-entry kernel 38, existing integral
ordering 292, and the throwing-call consumer 19. The batches overlap; do not
sum them into a coverage score. The new kernel exercises actual bounded
fixed-point convergence to Integer/Bool, equivalent normalized call orders,
retained first-round diagnostics, final unresolved/concrete/arity/conflict
rejection and work-limit behavior. Atomic guards prevent composite type
hashing during the two new probes. An exhausted raw pass returns its original
work and diagnostics before return inference, merging or conflict scanning,
so a later round cannot mask its ordering resource reason. At that focused
checkpoint, changed-source integration was still pending; the later result
below does not retroactively make the first source attempt pass.

The changed-C7 source composition subsequently passed one test/269 assertions:
263 composition checks plus six actual runtime-source loader checks. Seven
fresh product requests cover four accepted chains (two programs in both
spellings) and three Gravity negatives. The unchanged W5 helper now receives
independent Integer entry facts for -1/0/7 without an annotation or artificial
recursive companion. Separate Keyword and inferred Vector1 classifiers return
false, and the pure program keeps existing `vector?` and `>=` facts alongside
the new predicate. The separate typed-catch program covers normal and Never
operands without widening cold-handler inference.

All sixteen asserted top-level function-table entry invocations completed;
this is not a census of nested activations or `execute-closed-plan`. The run
also exercised five additionally tracked public C7 assembly/forgery gates,
independent C8 construction/verification and resealed graph-dependency forgery
rejection. Paired full typed identities, effect graphs, results and emitted
functions modulo source path were compared, not serialized as retained full
positive products. The C7 tamper changes one descriptor field, not a coherently
resealed all-field forgery. Exact/one-less component budgets are not a
whole-source budget claim.

All three full retained negative vectors have the intended primary diagnostic:
`integer-predicate-operand-type-required` for Unknown and
`integer-predicate-arity-mismatch` for zero/two operands. Each retains a
secondary `unsupported-or-unresolved-core-node-type`; do not erase it, infer
its node kind, or claim single-diagnostic output. QST negatives retain only
the separately checked byte/parser parity. No work-limit diagnostic caused
these rejections. Three post-run exact-vector assertions make the future
source selector 272 assertions, not an executed 272 result. A separate two-
assertion retained-log check matched all three vectors without a source replay.

Selected source work took 585.444 seconds after 9.579 seconds of namespace
loading at 2 GiB. This includes product construction and verification, not
just scheduling, and does not identify a dominant phase or speedup. The first
source failure remains preserved. General polymorphic predicates, arbitrary
W5 coverage, numeric conversion/range/width proofs, native/other targets,
authoritative migration, self-hosting, seed retirement and release remain
outside this increment. Direct focused commands remain in
`development-verification.md`; ordinary edits require no authority-admission
maintenance.

### Keyword Vector Set conversion: development implementation

The four existing conversion owners extend unary `set` to a finite,
independently established homogeneous Keyword Vector under STD3's existing
law. Nonempty Keyword input carries a distinct versioned input/construction
policy, independently reconstructed by C7, C8 and typed lowering. String,
empty and Never behavior stays on its existing route; the output remains a
bare Set with no member-domain, cardinality, ancestry or ordering refinement.
Runtime projection is 52 functions with four unchanged exports.

All actual input members are validated and frozen before printing or hashing.
The existing width 1,024 and logical-work 65,536 ceilings reserve
`1 + 2n + n(n-1)/2 + (n+1)U`, where U is printed JVM UTF-16 units for cost
only. Existing Gravity Keyword equality collapses duplicates into semantic
representatives before provider Set storage. Same-printing distinct Keywords
remain distinct; original input is preserved. This is not provider CPU evidence.
Allocation includes empty construction; Vector/conj and Set allocation remain
separate. Guard and measured-route corrections ensure source/producer IDs are
validated before lookups and an unrelated integral-ordering probe is skipped
only after the conversion's own accepted measurement.

Focused failures remain in `263fd3d8` (test projection count 51 versus 52),
`6f3a44eb` (C8 fixture omitted its allocation allowance) and `c090a8cd`
(test counted all member reads as owning validation). Accepted nonduplicated
observations at `2339343f` are twelve selectors / 902 passing assertions:
one unchanged runtime selector / 109 from V2, five corrected-production
selectors / 490 from V3 and six / 303 from V4. These are seven new kernels /
496 assertions plus five String/count regressions / 406, not one green batch.
Failed-selector assertions are excluded. The actual scoped executor trace
delegates unchanged arguments/results and retains the meter: thirteen total
member reads include one owning measurement/read, zero unrelated probes and
zero hashes. The other twelve reads are outside owning validation; they are
not individually stack-attributed. Independent audit accepted this reuse and
the corrected code, policy/source/ledger forgeries and zero-callback guards.

Source V1 at `6073f353` and V2 at `8bf42de1` each had six loader passes and
one error from unsupported fixture return annotations (Integer at C5, then
Set at C6). No Int variant ran; no downstream credit follows. Removing only
unnecessary annotations used existing inference without widening grammar.
Source V3 at `4ed3f6a5` remains FAILED: one test / 178 passes, one failure,
zero errors, with the cheap fixture selector unrun. The 178 passes include
172 composition checks and six loader assertions. Its original exit was one.
Both Gravity products completed. The positive completed accepted and freshly
verified C7/C8, assembly, all eight AP closed-module-instance entry executions,
three same-emitted-IR interpreter controls and tracked verification/tamper
controls; the mixed-family product reached its genuine C7 rejection. The only
failure expected an empty inferred effect set
for a caught throw, but actual C8 retained `:error/throw` while the separate
escaping-throws assertion passed empty. L6 preserves performed labels and makes
their discharge authority-dependent; current C8 records that pending state.

The test-only correction at `0b0eb0f5` changes that expectation/comment, not
production, fixtures or the actual failed result. A separate cheap run passed
two tests / 18 assertions: fixture/parser/exact-C16-AST checks eleven and the
existing caught-effect regression seven. Its original exit was zero. The
corrected source aggregate was deliberately not replayed; independent audit
accepted the completed behavior with this explicit failed-aggregate limit.
Selected source execution was 211.571014375 seconds at 2 GiB, including nested
product construction and assertions, not exclusive scheduling cost or speedup.

Nine functions, fifteen source calls and five conversions (three Keyword,
one empty, one Never) are checked. The exact C16 declaration contains 23
Keywords including `:source`; the original binary-conj expression feeds 24
positions to conversion and runtime count observes 23 members. Existing module
instances initialize that declaration rather than substituting a local helper.
Output stays bare Set. The negative asserts primary
`C7-TYPE-MISMATCH/:set-conversion-operand-type-required`; secondary unresolved
annotation is a raw observation only. QST has no typed products. This is not
whole-C16 execution, variadic conj, keys conversion, new Set membership/equality
typing, full source-compiler parity, native execution or authoritative migration.
The three partial current-closure groups and resolver development-unadmitted
status remain unchanged.

### Finite Set conversion and cardinality: development implementation

The owning C7, C8, typed emitter and runtime support unary `set` on an
independently established finite String Vector and unary `count` on an
established Set. The compatibility bindings keep `gravity.collections`
semantic ownership. Conversion returns a persistent bare Set without inferred
member, order or cardinality facts; this is a development frontier, not a
String-only restriction on the language's Set contract. Set count is shallow
and returns a nonnegative Integer, including zero for the empty Set, without
walking or executing members. Quoted heterogeneous payloads remain inert.

Conversion evaluates its operand once in the ordinary non-tail position,
preserves effects and transfers, and records possible allocation even for an
empty result. Never prevents construction. Full member validation precedes
hashing/insertion, so a late invalid member cannot cause earlier unchecked
hash work. The bounded JVM implementation accepts at most 1,024 input entries
and reserves `1 + 2n + n*C + n(n-1)/2` logical work, where C is their total JVM
UTF-16 representation length. Division-based fit checks precede multiplication
against the existing 65,536 component allowance. This conservative content
reservation is not opaque host hash-trie CPU evidence, a public String-count
unit policy, Unicode normalization, native execution or a realtime guarantee.

The first focused attempt failed: two tests, 84 passes and one error. Existing
count lowering passed; runtime plan construction then rejected a malformed
new `if` with two operands. The other twelve selected tests were unrun, not
passes. Independent review also caught a misplaced JVM eligibility clause on
the existing assoc binding instead of the new Set binding. Both narrow fixes
were applied without changing the failed attempt. A separate syntax-only probe
read 48 runtime forms and checked 751 `if` forms with no malformed arity; that
is parsing evidence, not semantic execution.

The corrected focused batch passed 14 tests/1,471 assertions in 5.369 seconds
of selected work, after 9.474 seconds loading SH01 and 1.048 seconds loading
the C7 namespace, at 2 GiB. It includes seven new Set selectors (442
assertions), a 12-assertion cross-owner Set-target/assoc-preservation check,
and affected existing count/integer-predicate regressions. The new throwing-
consumer checks preserve resource classification and prepaid semantic work.
Exact/one-less component budgets, equal-but-distinct String instances,
`FB`/`Ea` provider-hash collision, Unicode exactness, empty/duplicate inputs,
inert payloads, source/descriptor tamper rejection and normal-versus-Never
allocation facts are covered. These component checks are independently accepted;
they are not whole-source budgets, WASM execution or authoritative migration.

Fixture preflight passed one test/20 assertions for all six paired programs
and the unchanged W5 `w5-se-unique-values?` body. The subsequent changed-source
composition passed one test/283 assertions (277 composition checks plus six
runtime-source loader checks). Eight fresh products cover four accepted
Gravity/QST chains and four Gravity negatives. The unchanged W5 helper takes
independently inferred same-width String Vector entries; no annotation,
recursive companion or new polymorphic inference was introduced. All eighteen
top-level function-table entry invocations completed, including the separate
normal/typed-catch program. This is not a census of nested activations.

The run exercised C5/C6/C7 acceptance and verification, independent C8
construction/verification, paired typed/effect/lowering comparisons, five
additionally tracked public C7 gates, descriptor-field integrity tampering
and re-sealed C8 graph-allocation deletion. The C7 descriptor tamper is not a
coherently re-sealed all-field proof forgery, and C8 graph deletion is not an
undeclared-allocation source-program negative. The four complete negative
vectors retain `set-conversion-operand-type-required` for Unknown and mixed
members, or `set-conversion-arity-mismatch` for zero/two operands, followed by
`unsupported-or-unresolved-core-node-type`. They are not single-diagnostic
results. Negative QST fixtures received byte/parser parity only.

Selected source work took 680.209 seconds after 9.863 seconds of namespace
loading at 2 GiB. Existing-call timing observations separate loading from
construction/verification: per product, C5 construction took 21.994-31.250
seconds, C6 construction 21.910-32.552, and C6 verification 26.524-36.758.
These intervals include nested work and are not exclusive CPU attribution or
a representation-cost proof. No metric-only source replay was performed.
Before redesigning products, use the bounded duplicate-verification tracing
follow-up in the SH-00--SH-06 audit at a concrete changed composition or stable
migration decision; do not add another lane, wrapper or proof cache.

Set equality/iteration, general sequence and other member-family conversion,
full W5 execution, native/other-target execution and fresh authoritative
migration remain outstanding. This increment does not complete SH-07,
self-hosting, seed retirement or release. Direct focused tests are development
feedback, not renewed authority for earlier compiler snapshots.

### Map family classification: development implementation

Unary `map?` classifies an independently established Map as true and other
supported closed value families as false. Normal results are Bool, Unknown
remains an unsupported implementation frontier, and Never prevents invocation.
Operand evaluation is once-only and non-tail, with effects and transfers
preserved. Classification neither evaluates stored keys/values nor allocates
a collection, and establishes no key/member types, cardinality, presence or
order facts. `gravity.collections` owns the semantics behind the existing
`gravity.core` compatibility binding.

The C7, C8 and typed-emitter owners independently reconstruct the
source/operator/binding, operand, descriptor, facts and ordered producer rows.
The runtime validates and executes unary IR against actual values. Existing
cumulative work, rejection work and throw-consumer
classification remain in force. The JVM eligibility fence belongs only to the
new predicate; affected existing integer-predicate eligibility is preserved.
The runtime's public exports and 47-function inventory are unchanged.

The focused batch passed eight tests/1,491 assertions: four new Map kernels
(682 assertions) and four affected Integer-predicate regressions (809).
Selected work took 3.928 seconds after 9.351 seconds loading SH01 and 1.072
seconds loading C7 at 2 GiB. Normalized source/descriptor tamper, ordered-ledger
census, key-presence-fact forgery, work/throw accounting, inert stored values,
once/transfer behavior and twelve cross-owner JVM/old-Integer eligibility
checks are covered. The batch is independently accepted, not authentic source
composition, other-target execution or authoritative module migration.

Fixture preflight passed one test/15 assertions for the five Gravity/QST
pairs. The changed-source run completed FAILED: one test, 228 passing and two
failing assertions, zero errors. Of the passes, six are runtime-source loader
checks and 222 are composition checks. Seven fresh products cover four
accepted chains and three Gravity negatives. All twelve asserted top-level
function-table entry invocations returned the expected values, including
empty/heterogeneous quoted Maps, inert stored call-shaped data, Keyword false
classification and actual Never/throw/catch behavior. This is not a census of
nested activations or execution of an unchanged W5 helper.

Both failures are the same harness expectation in the transfer program's G/Q
chains: it expected one `:literal` operand and one `:throw`, while the fixture
uses a quoted empty Map and correctly emits one `:quote` and one `:throw`.
Independent tracing confirmed the existing quoted-data lowerer's behavior;
no compiler change is required for this mismatch. The failed aggregate remains
failed. Correcting that one expected keyword is not a fresh green source run
or permission to rebuild unchanged source products merely to change its label.
The expectation is now corrected. A read-only retained-log check matched both
actual `{:quote 1 :throw 1}` observations; reversing only that expected-keyword
edit reproduces the executed test-file hash. Production and fixture bytes are
unchanged. This validates the oracle correction, not a passing replacement
source aggregate.

C5/C6 status assertions, C7/C8 acceptance and verification guards before each
entry batch, paired typed/effect/lowering comparisons and five additionally
tracked public C7 assembly/forgery gates passed. The key-presence descriptor
tamper changes a field through its copied views, not a coherently re-sealed
all-field C7 proof. The re-sealed C8 dependency deletion is a graph-forgery
check, not a source-program policy rejection. All three complete negative
vectors retain the intended `map-predicate-operand-type-required` for Unknown
or `map-predicate-arity-mismatch` for zero/two operands, each followed by
`unsupported-or-unresolved-core-node-type`. Negative QST fixtures received
byte/parser parity only; unasserted rejected-product verification statuses
are not credited.

Selected source work took 608.661 seconds after 9.810 seconds of namespace
loading at 2 GiB. Existing-call timings include nested work: C5 construction
took 24.408-33.459 seconds per product, C6 construction 22.846-29.889, and C6
verification 27.601-32.686. These are inclusive observations, not exclusive
CPU costs, a representation proof or a speedup. No metric-only source replay
was performed. Full W5, other targets/native execution, authoritative migration,
self-hosting, seed retirement and release remain unestablished.

The next `keys` dependency has a specific unresolved contract boundary: an
ordered List result exposes Map enumeration, while current bare Map facts do
not promise an ordered traversal policy. Host `keys` order or an unused
`first` result does not prove stage equivalence. Settle a deterministic policy
for an independently established supported key domain and check insertion
permutations, representation transitions and actual serialization before
claiming that operation. This is bounded follow-up, not a comparator-framework
refactor or a prerequisite for the independent `keyword?` slice.

### Keyword family classification: development implementation

Unary `keyword?` now has a bounded JVM path through C7, C8, typed lowering and
runtime, with STD2 `gravity.core` ownership. Actual Keywords classify true;
other supported established closed families classify false. Nil-or-Keyword
operands retain their independent type evidence and classify their actual
value. Keyword-like String/Symbol spelling does not create a Keyword;
qualified Keyword values confer neither namespace nor capability authority. Unknown and
Dynamic remain unsupported implementation frontiers, not language-wide bans.

The operation preserves once-only non-tail operand evaluation, effects and
transfers; Never prevents invocation. It adds no collection allocation,
capability requirement or type-flow refinement. C7/C8/emitter independently
reconstruct source, binding, operand, descriptor, fact and ordered-row joins;
the runtime validates and executes unary IR against actual values. Existing
cumulative limits and resource/semantic rejection work remain enforced. The
JVM fence is confined to the new path; prior Integer/Vector/Map predicate
eligibility and the runtime's 47 functions/public exports are preserved.

The focused batch passed eight tests/1,593 assertions: four new Keyword
kernels (784 assertions) and four affected Integer-predicate regressions (809).
Selected work took 3.977 seconds after 9.796 seconds loading SH01 and 1.065
seconds loading C7 at 2 GiB. Closed/nullable families, keyword-like values,
no-authority/no-refinement facts, coherent field tamper, ordered-row census,
malformed-row containment, operand effects/transfers and twenty-four cross-owner
target checks are included. The batch is independently accepted, not fresh
source composition, actual WASM execution or authoritative module migration.

The changed source composition then passed one test/248 assertions (242
composition checks plus six runtime-source loader checks) in 462.861 seconds
after 10.252 seconds loading SH01 at 2 GiB. Four accepted programs
(ordinary callers and transfer handling in both source spellings) reached
fourteen top-level function-table entry invocations, not a count of nested
activations. The unannotated private projection preserves independently derived
key presence and the selected Keyword type; the nullable helper retains its ordinary Bool
callers. This exercises the exact W5 predicate/projection subexpression, not
the unchanged validator helper. Five fresh source products include one actual
zero-arity Gravity rejection; its full diagnostic vector retains the secondary
unresolved-core-node diagnostic. The paired negative spelling has byte/parser
parity checks only. Five additionally tracked public C7 verification gates,
paired typed/effect/result/emitted-function equivalence and descriptor-authority
field tamper rejection passed. The resealed C8 dependency deletion is a graph
forgery, not a source-policy negative. Neither hostile case claims all-field
resealing. Timings include nested work and assertions at their recorded call
boundaries; they do not establish exclusive cost, speedup or representation
dominance. Native/other-target execution, current-closure admission and broader
SH-07/self-hosting completion remain outstanding.

### Character family classification: development implementation

STD4 defines `char?` as Character-only: BMP and supplementary Character values
classify true; Strings, including one-character Strings, and ordinary Maps
resembling a Character carrier classify false. The current JVM development
implementation uses independently established closed operand types in C7,
fresh C8 reconstruction and typed lowering to existing instructions. Known
families evaluate the operand before the Bool result; Nil-or-Character uses
existing nil equality and `if`. Never preserves its original transfer.
There is no new runtime builtin, export or instruction.

Once-only non-tail evaluation, operand effects, control transfers, source/type
joins, ordered producer census and cumulative work remain checked. The query
does not execute stored payloads, coerce values, refine types, validate Unicode
or establish text-boundary facts. Unknown, Dynamic and unsupported families
remain implementation frontiers. Follow the Character section of the
[development workflow](development-verification.md#character-family-classification)
for focused checks and the separately selected source composition.

The initial eight focused kernels passed 1,759 assertions. Independent review
then found two lookup guards, repaired locally in the new C7 step and emitter
sibling-ledger scan. The ten-kernel successor ran all bodies with 1,770 passes
and two failures in a new sibling fixture's positive control; a further cheap
run preserved that failure and exposed host-reader selection in the source
fixture check. These were test-harness mistakes, not accepted negative controls.
After restoring fixed fixture fields and selecting the existing owning C2
reader, the two affected kernels passed all 13 assertions. Earlier failed runs
remain recorded; these epochs are not relabeled as a single green aggregate.

One actual changed-source composition then passed one test/84 assertions
(78 composition checks plus six existing runtime-loader checks) in 156.352
seconds, after 11.732 seconds loading SH01 at 2 GiB. One accepted
`.gravity` program reached public C7 verification, C8 construction/verification,
typed assembly and one runtime function-table entry invocation, returning
`[true true false false false true]` for BMP Character, supplementary Character,
one-character String, supplementary String, carrier-shaped Map and
Nil-or-Character respectively. One zero-arity `.gravity` program rejected with
`:character-predicate-arity-mismatch`. The existing runtime still has 54
functions; no new predicate builtin is used. The `.qst` twins have owning-C2
parser/byte checks only. Fresh development C6 sessions and same-catalog replay
are not fresh public C6 verification. Timings include nested work and establish
no speedup. Native/other-target execution, stable-revision admission and broader
SH-07, self-hosting and release gates remain outstanding.

### String family classification: development implementation

Unary `string?` has a bounded JVM path through C7, C8, typed lowering and
runtime. STD4's `gravity.text` owns the semantics behind the existing core
compatibility spelling. Actual Strings classify true, including empty, NUL,
supplementary and combining values; other supported established closed values
classify false. Nil-or-String operands retain their independent type evidence
and classify their actual value. Unknown, Dynamic and unsupported Text/Bytes
families remain implementation frontiers, not language-wide bans.

Operand evaluation remains once-only and non-tail, with effects and control
transfers preserved; Never prevents invocation. The query performs no payload
execution, conversion, normalization, text scan, own collection construction or
type-flow refinement. It independently establishes no Unicode-boundary, length
or indexing fact. Existing String-validity obligations remain at their owning
boundaries. C7/C8/emitter reconstruct current source, binding, descriptor,
ordered rows and cumulative work; runtime executes shallow unary IR without
coercing host text carriers. The new JVM fence does not alter existing
predicate eligibility, and runtime retains 47 functions and its public exports.

The combined focused batch passed nine tests/1,613 assertions: four String
kernels (816), four adjacent Keyword regressions (784), and fixture preflight
(13). Kernel totals include existing runtime-loader checks. Fixture preflight
checks four exact W5 runtime-field subexpression occurrences plus byte/parser
parity and definition counts for three source pairs; it does not execute the
unchanged W5 validator. The batch took 4.396 seconds after SH01/C7 loading of
10.617/1.193 seconds at 2 GiB, while the separate reader composition ran.
These inclusive timings are not isolated performance comparisons. Independent
review accepted the actual focused results.

The changed-source composition subsequently passed one test/159 assertions
(153 composition assertions plus six runtime-loader checks) at 2 GiB. It
constructed two positive `.gravity` products and one arity rejection, invoked
seven top-level function-table entries and passed three additionally tracked
public C7 gates. The zero-arity diagnostic vector contains the primary
`C7-TYPE-MISMATCH`/`:string-predicate-arity-mismatch` and secondary
`C7-ANNOTATION`/`:unsupported-or-unresolved-core-node-type`. C5/C6 statuses are
asserted; accepted C7/C8 results guard both positive executions. The negative
C7 verifier is invoked, but its status is not separately asserted.

Descriptor-field tampering and a resealed C8 dependency deletion were rejected;
neither case claims all-field resealing. The paired `.qst` evidence remains
the earlier byte/parser preflight, not fresh typed/effect/lowering/result
equivalence. The original focused-test source is preserved in commit
`4213a7f739f46aa851c3a80c40b9b84993c1ecfc`; the later source-only selection
change does not relabel that earlier execution. The source selector took
258.466 seconds after 9.484 seconds of SH01 loading. These elapsed boundaries
include nested work and assertions, not exclusive cost or speedup evidence.
This is not execution of the whole W5 helper, native/other-target support,
current-closure admission or broader SH-07/self-hosting completion.

### Binary nonnumeric atomic inequality: development implementation

Development checkpoint `7f9bff8f40763d4a286e3028a3bde096d041152e` implements
binary `not=` as the Bool complement of applicable atomic value equality under
STD2. C7, effects and the typed emitter authenticate a distinct versioned
inequality descriptor and complete source ledger. Nil, Bool, Character, String,
Symbol and Keyword values, the five existing nil-or-atomic carriers, and Never
are supported. Both domains remain checked even when one cannot return.
Numeric, collection, callable, Unknown and malformed domains are not added.

Owned equality/branch IR evaluates the original operands once, left-to-right and
non-tail, with no fabricated source equality facts. Nullable comparison uses
actual values, not a different-family constant fold. C8 retains operand effects
and dependencies; original throws and non-tail transfer fences survive. No
runtime, seed, public arity, annotation grammar or IR schema changes are made.
The unchanged runtime has 52 functions including its four exports, not 52 private
functions. Entry execution assumes authenticated type-conforming values, not
runtime validation of arbitrary host or FFI values.

Independent review accepted thirteen focused selectors / 10,286 passing
assertions across retained F1/F2/F3 observations, not one green batch. F1 remains
failed (6,798 passes / 420 failures): its new matrix used an invalid tagged BMP
Character. F2 remains failed (9,551 passes / five failures): its test prefix
included 124 units of success-only suffix work. Canonical C2 Character transport
and an observation of the actual pre-child budget corrected tests only; no
production weakening or historical relabelling followed. F3 passed seven tests /
7,153 assertions; unchanged completed selectors from F1/F2 supply the remaining
distinct coverage. Failed selectors' partial assertions are not added to that
passing union.

The first sixteen-function source run passed C5/C6 status checks, C7 and C8
construction/fresh verification, nine Gravity entry results and eight same-IR
seed controls before the seed rejected an existing `:keyword-map-lookup`
instruction. Its aggregate remains 30 passes / one error. The original nullable
`main`, later source census, forgeries and numeric negative were unrun. The seed
reference frontier was corrected without rebuilding this unchanged source.

A new five-function completion input then passed one test / 91 assertions
(85 composition plus six loader assertions), with five Gravity entries, two
same-IR seed controls, four owned inequality descriptors/rows/equality IR sites,
two public C7 assembly gates, a coherent inequality-policy forgery and a resealed
C8 dependency deletion. The numeric/Never source negative ran for the first
time; it retains the primary operand-type-required diagnostic and a secondary
Unknown annotation diagnostic. The oracle asserts the primary, not a sole or
exact-vector diagnostic claim. The new `main` calls `observed`; it does not
retrospectively execute the original nullable wrapper. The original fixture
selection (one test / eight assertions) and completion fixture selection
(one / five) provide separate byte/parser coverage, not Q typed products.
The distinct passing selections total 16 tests / 10,390
assertions across runs; the failed source aggregate is excluded.

The completion source took 192.608 seconds of selected execution after 10.169
seconds of loading at 2 GiB. Positive construction observations were 33.060
seconds for C5, 37.249 for C6 construction and 36.231 for C6 verification; C7
construction/verification were 2.018/1.031 seconds. These caller-side inclusive
boundaries separate product work from loading, not exclusive CPU causes or a
speedup comparison, and do not diagnose the retained large-Core interruption.
No unchanged expensive source proof was replayed for a green label.

This enables the exact one-level W5 diagnostic-span comparison on independent
literal-map DATA, not its whole function or transitive module. Current-closure
admission, the three partial evidence groups, full SH-07, self-hosting, target
coverage, seed retirement and release status remain unchanged. Direct focused
and affected-source commands are in the development-verification guide.

### Binary same-type fixed-integral inequality: development implementation

Development checkpoint `6a7dbeca` extends the existing binary `not=` rule to
I8/I16/I32/I64/U8/U16/U32/U64 of the same established type, including Never
in either operand position. All 24 supported type configurations are checked.
The 72 ordered cross-fixed or generic-Integer/fixed pairs reject; other
unsupported numeric, nullable and collection domains do not become legal.
Old Integer, nonnumeric and Never/Never descriptors are preserved.

The three existing owners independently reconstruct a fixed-family numeric
record, complete constraint solution and source-owned evidence joins. The
record names the actual fixed type and retains the existing math/math.core
original-value policy without conversions, arithmetic, overflow or rounding.
Lowering owns its equality/false/true IR, not an invented source equality fact.
Runtime, seed, public APIs and IR schemas are unchanged.

The single focused run passed 23 tests / 18,153 assertions: six new kernels /
5,186 assertions and seventeen adjacent regressions / 12,967. The adjacent
count is 32 below the earlier Integer batch because two old C7 frontiers each
remove sixteen newly legal fixed/Never rejection assertions. Mixed-family
negative coverage remains. Exact and short budgets, coherent numeric-record
and whole-ledger forgeries, actual child-budget meters, all eight runtime/seed
families and both transfer positions pass. Every normally returning test entry
first checks Integer representation and signed/unsigned range, including
64-bit values adjacent above 2^53; no clamping, conversion or wrapping occurs.
These focused kernels alone do not establish source composition or arbitrary
host width admission.

The separate paired-fixture check passed one test / six assertions. The first
genuine changed Gravity composition then passed one test / 535 assertions:
nine compiled functions retain nine not= descriptors, ledger rows and owned
equality IR sites, eight fixed-family records plus the generic Integer control.
Twenty-eight actual fixed-helper entries cover signed/unsigned bounds and
adjacent large 64-bit values, with one generic main entry; the same 29 entries
also pass through the seed interpreter over the emitted IR. Actual signatures
and every argument's Integer representation/range are checked before each
route. No source Integer conversion or width-checking activation is invented.

Two public C7 assembly gates include a fully propagated fixed-mode forgery.
Fresh C8 construction/verification and a resealed dependency-deletion rejection
pass. The changed I8/U8 Gravity negative passes C5/C6 and rejects at C7 with
the expected mixed-domain diagnostic; a secondary unresolved-annotation
diagnostic is also retained. The oracle matches the required diagnostic rather
than assuming it is first or sole. Q remains byte/parser parity only.

The three selections total 25 tests / 18,694 assertions, zero failures, errors
or unrun selectors. The source selection took 214.867 seconds after 12.029
seconds of loading at 2 GiB. Its positive C5 construction and C6 construction/
verification took 41.326, 37.439 and 41.977 seconds respectively. These are
inclusive existing-call timings, not exclusive costs, a benchmark, hard
deadline, speedup or a diagnosis of the interrupted large-Core attempt.
No earlier source product was reconstructed for a green label. Conditional
typed-entry execution does not establish arbitrary-host/FFI admission,
current-cohort migration, complete numeric support or self-hosting.

Source evidence belongs to checkpoint `b0522928`; the focused batch belongs
to `6a7dbeca`. After the normal main merge, the combined candidate passed
the separate language boundary (six tests / 11 assertions) and LOC validation
(2,020 source files, zero exceptions, maximum 250 lines). The original BN
production, fixture, harness and runtime bytes are unchanged. Independent
compatibility review checked that the disjoint module-vector routing change
does not alter this typed raw-plan path; no unaffected source composition was
repeated. These checks do not renew historical authoritative fingerprints.

### Binary represented-Integer inequality: development implementation

Development checkpoints `792f132b` and `a9267856` extend the existing binary
`not=` rule to Integer/Integer and Integer/Never in either order. Old
Never/Never and nonnumeric descriptors remain unchanged. Integer mixed with
another normal family, fixed-width types, Float/Decimal/Ratio, nullable Integer,
collections and unresolved types remain outside this increment. The old
numeric negative now uses `7.5`; rejecting Integer `7` would test an obsolete
implementation frontier rather than the new supported behavior.

C7, effects and the emitter independently reconstruct a versioned numeric
record, its complete constraint solution and source-owned ledger. The outer
inequality policy names the Integer domain, while the nested math/math.core
policy preserves existing equality's original-value comparison without
conversion, arithmetic, overflow or rounding. Its mode is `:proof-required`
and safety is `:proven-safe`; no source `=` fact or old equality application
is invented. Runtime, seed, public APIs and IR schemas are unchanged.

One focused run passed all seventeen tests / 12,999 assertions: six new kernels
/ 818 assertions and eleven adjacent equality/inequality regressions / 12,181.
The actual scoped consumer boundary confirms prefix work separately from the
success-only suffix. Tests cover exact and short budgets, opaque type guards,
coherent numeric-record forgeries, complete C8 ledgers, original failures,
non-tail transfers and 81 normally returning type-conforming Integer pairs,
including adjacent values above 2^53. They are development kernels, not source
or arbitrary-host-value admission.

The separate fixture check passed one test / six assertions. The genuine
changed Gravity positive and changed Decimal/Never negative then passed one
test / 157 assertions. Six Gravity entries and four same-emitted-IR seed
controls cover literal equality/difference, actual count-produced Integers and
both transfer positions. Five descriptors, rows and owned equality IR sites
retain all numeric metadata; no source equality row appears. Two fresh public
C7 assembly gates include a numeric-mode forgery, and fresh C8 construction,
verification and a resealed dependency-deletion rejection pass. The negative
retains primary `C7-TYPE-MISMATCH` /
`:atomic-inequality-operand-type-required` and secondary
`C7-ANNOTATION` / unresolved Unknown diagnostics; only the primary is the
rejection oracle. Q fixtures provide byte/parser parity, not typed execution.

These three completed selections total nineteen tests / 13,162 assertions,
with no failures, errors or unrun selectors. No earlier source input was
replayed; the focused batch renewed affected equality/inequality regressions
without reconstructing old source products for a green label. The source run
took 214.207 seconds after 11.086
seconds of loading at 2 GiB, below its monitored ten-minute ceiling. Positive
C5 construction, C6 construction and C6 verification took 38.199, 36.784 and
41.022 seconds respectively; C7 construction/verification took 2.400/1.104.
These inclusive caller timings distinguish product work from namespace loading;
they do not establish exclusive cost, a speedup, a hard deadline or the cause
of the earlier large-Core interruption.

Direct commands are in the development-verification guide. This supports the
new comparison over real count results, not a copied numeric `not=` compiler
body or whole consumer module. Full numeric/profile/target support, current
cohort migration, the three partial evidence groups, complete SH-07,
self-hosting and release claims remain outstanding.

### Binary finite Vector inequality: development implementation

The existing C7 engine, effects checker and typed emitter extend binary
`not=` to independently established finite positional Vector types. The
versioned collections premise reuses existing Vector value equality and its
complete domain checks, while preserving the original inequality source,
constraint solution, call fact and ledger. Lowering uses owned equality IR
inside an explicit Bool complement; no source equality fact is invented.
Runtime, seed, public interfaces and the type grammar are unchanged.

The supported nonnumeric recursive domain includes empty Vectors, unequal
lengths, nested Vector/atomic differences, Nil, Bool, Character, String, Symbol,
Keyword and the existing five nullable nonnumeric atomic views. Both complete
domains are validated before mismatch; numeric members reject even when the
other Vector is empty. Existing width/depth/work limits remain. Bare or
unknown Vectors, whole nullable Vectors, nested Never and other collections
are not added. Top-level Never retains original control transfer. Accepted
scalar, represented-Integer and same-fixed-integral behavior remains intact.

The final focused batch passed 33 tests / 20,053 assertions: six new kernels /
1,437 and 27 adjacent inequality/equality regressions / 18,616. Actual owning
validation runs once with prefix 123 and child budget 65,413, without unrelated
probes; exact/short budgets retain failed-child work. Direct derivation keeps
the Vector-specific work-limit reason; the containing C7 verifier keeps its
existing atomic-inequality resource diagnostic and charged work. The private
inferred-call check retains the underlying subtype reason and exact work.
Complete C8 ledgers, independently reconstructed policies, coherently changed
records, owned IR, transfer identity and non-tail restrictions are checked.

Earlier attempts remain distinct: the first command omitted namespaces and
selected no tests; the second failed test namespace loading on a duplicated
tail, also before tests. After removing that accidental test fragment, the
third run completed 23 tests / 17,718 assertions with 17,717 passes and one
diagnostic-oracle failure; ten C8 selectors were unrun. Only that new expected
owning diagnostic keyword changed before the final passing batch. Production
did not change for the oracle correction. The paired fixture check then
passed one test / six assertions, and LOC validation passed 2,021 files, zero
exceptions, maximum 250 lines. These checks are not source or migration proof.

The sole affected genuine Gravity source composition then passed one test /
108 assertions. Five source functions and five calls contain four source-owned
Vector inequality descriptors, ledger rows and owned equality IR sites, with
no fabricated source equality rows. Five Gravity function-table entries and
five same-emitted-IR seed controls cover the seven-Keyword W5 value, equal and
reordered values, nested Vector/atomic differences and empty/unequal widths.
This is a fresh producer and new inequality expressions, not whole-W5 coverage
or independent semantic equivalence established merely by same-IR execution.
Q fixtures provide byte/parser parity only.

Fresh positive C7 and C8 construction/verification passed. Two additional
tracked public C7 assembly gates include a fully propagated numeric-policy
forgery; a resealed C8 dependency deletion rejects. The real `[]` versus `[7]`
source negative reaches C5/C6 successfully and rejects at C7 for unsupported
numeric members, with the secondary Unknown annotation diagnostic retained.
The oracle matches the required primary rule/reason without requiring it to
be the sole or first diagnostic. Allocation remains an inferred wrapper effect,
not a direct effect of value comparison.

The three completed focused, fixture and source selections total 35 tests /
20,167 assertions. A separate language-boundary selection passed six / 11.
Source execution took 195.552 seconds after 10.558 seconds of loading at 2 GiB,
below the monitored ten-minute ceiling. Positive C5 construction, C6
construction and C6 verification took 32.931, 31.910 and 37.897 seconds;
assembly including public C7 verification took 2.717 seconds. These nested
caller observations separate product work from loading; they are not exclusive
phase costs, a benchmark, a hard deadline or a speedup comparison. No unchanged
source composition or migration proof was replayed.

Direct commands are in the development-verification guide. Numeric Vector
members, full current-closure migration, the three partial evidence groups,
complete SH-07, self-hosting and release claims remain outstanding.

### Binary finite Keyword-Set inequality: development implementation

Binary `not=` now accepts independently established finite literal-Keyword
Set types in C7, effects checking and typed lowering. Each owner reconstructs
the complete collections premise while retaining the original inequality
source, constraint, call fact and ledger. Existing equality IR inside an
explicit Bool complement implements the operation; no source equality fact
is invented. Runtime, seed, public interfaces and type grammar are unchanged.

Empty, unequal-width and reordered Sets are supported without requiring
identical static types. Both complete domains are checked before a value
mismatch. Existing per-side width 1,024 and work 65,536 limits remain; failed
child work is retained once after prepaid work. Bare, homogeneous, Unknown,
malformed or whole nullable Sets, non-Keyword members, nested collections and
nested Never remain unsupported. Top-level Never preserves original transfer
when paired with a supported Set domain. Never/Never keeps the atomic rule.

The first focused batch passed all 46 tests / 21,535 assertions: six new
kernels / 953 assertions, the paired fixture check / eight assertions, and
39 adjacent equality/inequality regressions / 20,574 assertions. The fixture
check includes six paired-fixture byte/parser/function-count assertions and
two C17 source/value-basis assertions. Owning validation runs once with prepaid
prefix 123 and child budget 65,413, with no unrelated probe. Independent policies, complete C8
ledgers, coherent tamper rejection, owned IR, exact/short work, transfer and
non-tail restrictions are covered. Focused checks are development feedback,
not current-source construction or authoritative migration evidence.

The sole affected genuine Gravity source composition passed one test / 107
assertions on the unchanged joined candidate. Five source functions and five
calls contain four original inequality descriptors, ledger rows and owned
equality IR sites. Five Gravity function-table entries and five same-emitted-IR
seed controls cover a fresh capability-Set producer, equal membership,
different same-cardinality membership, reordered members and empty versus
singleton Sets. The C17 basis is its capability-Set value, not activation or
compilation of the whole existing helper. Same-IR execution is not independent
semantic equivalence. Q fixtures supply no typed source product.

Fresh positive C7 and C8 construction/verification passed. Two tracked public
C7 assembly gates include a coherently propagated forgery of the existing
member-uniqueness policy; a resealed C8 dependency deletion rejects. The real
empty-Set versus numeric-member Set source negative reaches C5/C6 and rejects
at C7 with `C7-TYPE-MISMATCH/:atomic-inequality-operand-type-required`, retaining
the secondary Unknown annotation diagnostic. Quoted Sets and pure function
graphs do not establish an allocation effect merely from value comparison.

The completed focused batch including fixtures and the separate source
composition total 47 tests / 21,642 assertions. Source execution took 194.433
seconds after 11.063 seconds of loading at 2 GiB, below the monitored ten-minute
ceiling. Positive C5 construction, C6 construction and C6 verification took
34.209, 31.762 and 35.527 seconds; assembly including public C7 verification
took 2.626 seconds. These nested caller observations include their own work;
they are not exclusive phase costs, a benchmark, a hard deadline or a speedup
comparison. No unchanged source proof was replayed. Separate language-boundary
checks passed six tests / 11 assertions; LOC validation passed 2,021 files,
zero exceptions, maximum 250 lines.

Direct commands are in the development-verification guide. Unchanged equality
and Vector inequality proofs are not prerequisites for ordinary edits. The
three partial evidence groups, current-closure admission, full SH-07,
self-hosting and release remain open.

### Nil value classification: development implementation

STD2 defines unary `nil?`: nil classifies true; false and empty collections do
not. C7, effects and typed lowering independently reconstruct a versioned
classification from current validated operand types and retain it in the
descriptor, constraint solution, call fact and complete ledger. Known Nil and
non-Nil types lower to existing do/Bool IR after evaluating the operand;
the five existing nullable atomic families lower to owned equality with Nil.
Never preserves the original transfer in non-tail operand context. No source
equality fact, generated local, runtime primitive or type grammar is added.
Runtime remains byte-exact unchanged at 51 functions with four exports.
Support is limited to authenticated typed calls and type-conforming entries;
arbitrary wrong-typed embedding/FFI calls and check-elision claims are excluded.

Focused V1 at `45247075` stopped after four tests: 796 passes, zero failures
and one test-arithmetic error, with ten selected tests unrun. The test added
an absent early-rejection work-count instead of using the existing zero
fallback. The test-only correction at `d5e88374` passed all fourteen selected
tests / 2,502 assertions: seven new kernels / 1,647 assertions and seven
adjacent nil-if/equality checks / 855 assertions. Do not add the earlier passes
to this result. All five nullable families, false versus nil, effects/transfers,
original failures, coherent evidence forgeries, dominant ID guards, complete
C8 budgets/ledger and failed-child work are covered. Production pins did not
change. Execution was 8.880314334 seconds with a 2 GiB heap, not a benchmark.

Source V1 at `62b8d2c9` stopped with eight passes, one failure and one error:
C5 accepted and C6 passed, but C7 rejected unsupported source `println`.
Assembly, C8, runtime entries and the negative source were unrun. A source-only
fixture correction replaced that operand with `(nil? (do [] nil))`; production
and focused results were unchanged. Source V2 at `c7739c80` passed two tests /
260 assertions: composition 253 (including six runtime-loader assertions) and
paired byte/parser/exact-producer-AST preflight seven. Its original command
completed with exit zero; selected composition took 247.356899958 seconds
with a 2 GiB heap, including nested product construction and assertions.

Sixteen functions, 24 source calls and fourteen nil-query rows (thirteen
normal, one Never) are checked. The exact copied W5 canonical-form producer
returns nullable Keyword to two new queries. Its seven source equality rows
remain seven; two owned nullable lowerings produce nine emitted equalities,
not fabricated source calls. Fifteen type-conforming runtime table entries
include fourteen seed comparisons on the same emitted IR and one caught
transfer. The allocation operand is retained once with its actual transitive
C8 effect; the nil query has no direct allocation. Positive C7/C8 verification
guards execution. The negative asserts primary
`C7-TYPE-MISMATCH/:nil-predicate-arity-mismatch`; the raw secondary unresolved
annotation is observed, not a full diagnostic-vector oracle. QST typed products
are not exercised. This is not whole-W5 execution, independent source compiler
parity, current-closure admission or SH-07/self-hosting completion. The three
partial current-closure groups and resolver development-unadmitted status stay
unchanged.

### Boolean family classification: development implementation

STD2 specifies unary Bool-family classification, distinct from truthiness:
both `true` and `false` classify true, other normal values false. Existing C7,
effects and typed-lowering owners carry a distinct Boolean descriptor, source
joins, context, call fact and complete ledger rule. Valid closed first-order
types are supported and Never transfers; Unknown/Dynamic, malformed carriers
and unsupported callables remain diagnosed. Runtime classification is shallow,
with no String/Keyword coercion, wrapper unwrapping, payload traversal,
hashing/equality, mutation or collection construction. Type grammar, public
APIs, unary IR and seed primitives are unchanged; runtime projection remains
51 functions with unchanged exports.

Focused V1 at `5bbbcea1` passed all 14 tests / 2,431 assertions: seven new
kernels / 939 assertions and seven adjacent Keyword/Symbol regressions / 1,492
assertions. Both Bool literals and a function-returned false value classify
true; the latter retains one observable operand output. Matching String or
Keyword spellings, Symbols, nil, represented numbers and collections classify
false. Policy forgeries, original transfers/fatal failure, hostile-provider
zero-callback checks, measured C7 work, full C8 analyzer budgets/ledger census,
dominant unary/ID guards and failed-child work are included. Execution was
5.934057667 seconds with a 2 GiB heap; namespace loading was separate. The
original command's zero exit was independently observed and tested source pins
remained unchanged. This is component feedback, not a performance benchmark,
full C6 module build, polymorphic scalar-validator or authoritative migration.
The three partial current-closure groups and resolver development-unadmitted
status remain unchanged.

Paired preflight passed 1 test / 7 assertions: six byte/parser/function-count
checks and an AST comparison with the actual C6 scalar-helper defn. Source V1
at `facd04aa` passed 1 test / 231 assertions, including six runtime-loader
assertions, in 248.859418417 seconds selected execution with a 2 GiB heap.
One Gravity positive and one C7 arity rejection ran; QST typed products did not.
Sixteen functions, 21 calls and thirteen Boolean-query descriptors (twelve
normal, one not-invoked) are checked. Fifteen direct runtime table entries
include fourteen seed-interpreter comparisons on the same emitted IR and one
caught transfer, not all dynamic helper calls or independent source compiler
parity. Both false and true supply the copied helper's single inferred Bool
parameter domain. Its initial `(= value nil)` uses unchanged atomic-equality
semantics; it adds neither `nil?` nor parameter-annotation grammar. Other
families use separate direct queries. This is not a full C6 module build or
polymorphic scalar-validator acceptance. Pure source operands do not
independently establish observable I/O ordering; normalized controls do.

Accepted and freshly verified C7/C8 products precede execution. Two additional
tracked C7 gates accept assembly and reject a truthiness-policy forgery; a
resealed C8 dependency deletion rejects. The negative reaches accepted C5 and
passed C6, then rejects at C7. Its test asserts primary
`C7-TYPE-MISMATCH/:boolean-predicate-arity-mismatch`; the raw vector also contains
secondary `C7-ANNOTATION/:unsupported-or-unresolved-core-node-type`. This affected
composition does not renew authoritative migration or broaden the statuses above.

### Symbol family classification: development implementation

STD2 specifies unary Symbol-value classification under ordinary L2 evaluation.
C7, effects and typed lowering carry a distinct Symbol-predicate descriptor,
call fact, context and complete ledger rule. Existing valid closed first-order
types are supported: actual Symbols classify true, other families false and
Never transfers; Unknown/Dynamic, malformed carriers and unsupported callable
types remain diagnosed. The source type grammar, unary IR, seed primitive and
public APIs are unchanged. The runtime remains 51 functions with unchanged
exports, adding only shallow unary classification. It does not coerce
String/Keyword spelling, unwrap syntax objects, inspect payloads or grant
binding, call, macro or capability authority. Qualified and metadata-bearing
Symbols stay values, and ordinary reference resolution precedes evaluation.

Focused V1 at `ee244e49` passed all 14 selected tests / 2,351 assertions:
seven new kernels / 886 assertions and seven adjacent Keyword/Set regressions
/ 1,465 assertions. These include the owning C7 measured throw consumer,
dominant unary/ID guards, complete C8 analyzer budget and ledger controls,
policy forgeries, failed-child work, once-only effects/transfers, original fatal
failure identity and shallow hostile-provider controls. Execution was
6.200353125 seconds with a 2 GiB heap; namespace loading was separate. The
original command completed with exit zero, and the tested source pins remained
unchanged. No failing Symbol-focused attempt is being hidden or merged into
these counts. Component evidence is not a performance benchmark or fresh
frontend-module, target, migration or release acceptance. The three partial
current-closure groups and resolver development-unadmitted status remain open.

Paired preflight passed 1 test / 6 assertions (bytes, parser and function
counts). Source V1 at `2d240a06` passed 1 test / 214 assertions, including six
runtime-loader assertions, in 220.948593334 seconds selected execution with a
2 GiB heap. One Gravity positive and one Gravity C7 arity rejection ran; QST
typed products did not. Fourteen functions, 14 calls and twelve Symbol-query
descriptors (eleven normal, one not-invoked) are checked. Thirteen direct runtime
table entries include twelve seed-interpreter comparisons on the same emitted
IR and one caught transfer, not all dynamic helper calls or independent source
compiler parity. The identity helper uses the existing Symbol return annotation
and a monomorphically inferred quoted-Symbol input, not new parameter grammar.
Qualified quoted Symbols and normally evaluated lexical references classify
true; matching String/Keyword spelling and ordinary wrapper-Map DATA do not.
That wrapper is not evidence of an accepted syntax-object schema, nor does this
test build whole syntax, macro or envelope consumer modules. Pure source
operands do not independently observe I/O ordering; normalized controls do.

Accepted and freshly verified C7/C8 products precede execution. Two additional
tracked C7 gates accept assembly and reject a coercion-policy forgery; a
resealed C8 dependency deletion rejects. The negative reaches accepted C5 and
passed C6, then rejects at C7. Its test asserts primary
`C7-TYPE-MISMATCH/:symbol-predicate-arity-mismatch`; the raw vector additionally
contains secondary `C7-ANNOTATION/:unsupported-or-unresolved-core-node-type`.
This affected composition does not renew authoritative module migration or
change the broader completion statuses above.

### Set family classification: development implementation

STD3 defines unary Set-family classification under ordinary L2 evaluation.
The existing C7 engine, effects, typed emitter and Gravity runtime now carry a
distinct Set-predicate descriptor, call fact and ledger rule. Valid closed
first-order operand families are supported: bare and finite Sets classify
true, other valid families false, and Never transfers. Unknown/Dynamic,
malformed carriers and unsupported callable types remain typing rejections.
This does not add annotation grammar, polymorphism, an IR operation, seed
primitive or public API. The runtime remains 51 functions with unchanged
exports; classification itself is shallow, with no member counting,
enumeration, hashing, equality, mutation or collection construction. A
throwing Set-provider control observes zero payload callbacks.

Focused V1 at `90619dca` remains FAILED: 13 tests / 1,747 passes / one
failure / no errors, with one selected C8 test unrun. The normalized whole-C8
fixture omitted its actual module record and therefore rejected before the
new context/ledger path. The correction supplies the existing module; it does
not relax coverage validation. A separately reviewed malformed-call guard was
also moved before ID-vector construction, with scalar/empty/oversized argument
and scalar-child controls checking rejection without hostile callbacks.
Targeted V2 at `2a0d02ee` passed four C8 tests / 294 assertions: 92 Map,
63 Set-conversion, six whole-analyzer and 133 Set-predicate assertions. The
analyzer checks acceptance, exact/one-less budget, and missing/duplicate/orphan
ledger rows. Its execution was 3.610710917 seconds with a 2 GiB heap.

Independent review accepted the component evidence and proportionate reuse of
unchanged V1 C7/runtime/lowering successes. Across the two runs, all 14 unique
focused selectors have passing observations: 1,883 assertions after excluding
the failed consumer's three passing assertions and 155 overlapping adjacent
C8 assertions. That union is not one successful 14-test run. Seven new kernels
account for 857 assertions and seven adjacent regressions for 1,026. Initial
review also caught omitted context installation/ledger wiring and a missed
integral-ordering-probe skip; those actual-file omissions were corrected before
the first JVM run. No threshold, type grammar or validator was weakened.

Paired preflight passed 1 test / 7 assertions: six byte/parser/definition-count
checks and an AST-equality check against the actual C9 aggregate helper.
Source V1 at `bb505894` passed 1 test / 172 assertions, including six runtime
loader assertions, in 227.950724334 seconds selected execution with a 2 GiB
heap. One Gravity accepted fixture and one Gravity arity rejection ran; QST
typed products did not. Twelve functions, 15 calls and nine Set-predicate
descriptors (eight normal, one not-invoked) are checked. Eleven direct runtime
table entries include ten seed-interpreter comparisons on the same emitted IR
and one caught transfer, not a count of all dynamic helper invocations or an
independent raw-source compiler comparison. One evaluated Set instruction is
checked. The copied C9 helper body runs only on a monomorphic bare Set produced
by existing String-Vector conversion; this is not a full C9 module build or
generic polymorphism. Separate functions cover finite heterogeneous quoted
Sets and non-Set values. Source pure operands do not independently observe I/O
ordering; normalized runtime controls provide that evidence.

Accepted and freshly verified C7/C8 products precede execution. Two additional
tracked C7 gates accept assembly and reject a predicate-policy forgery; a
resealed C8 dependency deletion rejects. The negative reaches accepted C5 and
passed C6, then rejects at C7. Its test asserts primary
`C7-TYPE-MISMATCH/:set-predicate-arity-mismatch`; the raw diagnostic vector also
contains `C7-ANNOTATION/:unsupported-or-unresolved-core-node-type` as secondary.
Complete C9/W5 helper support, broader targets and authoritative migration
remain separate claims. The three current-closure
evidence groups remain open and the resolver remains development-unadmitted.

### Finite Keyword-Set membership: development implementation

STD3 Set membership now has a generic value-equality law. The implementation
frontier is binary `contains?` on the existing finite literal-Keyword Set type
with an independently established Keyword needle, returning Bool or preserving
Never transfer. C7, effects and typed lowering independently reconstruct the
Set-specific descriptor and original source/type/call joins. Existing Map
queries, closed type grammar, seed operation and IR shape remain unchanged.
The runtime validates the complete actual Keyword domain and within-Set
uniqueness before either hit or miss; it uses actual Keyword value equality,
not host Set lookup/hash or printed identity. The query adds no semantic
collection construction or allocation fact. Runtime projection is now 51
functions with unchanged exports.

Focused V3 at `eb88c2fa` passed all 13 tests / 1,262 assertions: six new
membership kernels / 527 assertions and seven adjacent Map, evaluated-Set and
Set-equality regressions / 735 assertions. Execution was 5.272473459 seconds
with a 2 GiB heap; namespace loading was separate. Independent review accepted
this component evidence. It is neither a CPU bound nor source integration.
V1 remains failed (286 pass / 1 fail, eleven selected vars unrun): a test
incorrectly rejected the public empty-Map route. V2 remains failed (960 pass /
1 fail, three selected vars unrun): the unchanged one-read meter found an
unrelated integral-ordering probe hashing a composite Set type. The narrow fix skips
that probe only after this invocation authenticates Set membership; ordinary
integral-ordering and Map behavior are untouched. Both failed snapshots remain in Git.
Early review also closed an uncharged repeated validation and an emitter ID
guard that did not dominate structural comparison. No test threshold was
weakened to close those findings.

Paired fixture preflight passed 1 test / 6 assertions (bytes, parser and
definition counts only). Source V1 at `8de847dc` passed 1 test / 159
assertions, including six runtime-loader assertions, in 214.936999208 seconds
selected execution with a 2 GiB heap. It ran one Gravity accepted fixture and
one Gravity rejected fixture; QST typed products were not run. The accepted
module has 11 functions and ten direct runtime table-entry activations, eight
same-emitted-IR seed interpreter comparisons and two caught-transfer entries.
These are not independent raw-source compiler/lowerer comparisons or a count
of all dynamic helper calls. Source pure `do` operands do not observe I/O
ordering; normalized runtime controls supply observable order/once evidence.
Eight new membership descriptors cover seven normal calls and one not-invoked
call; the left-transfer case retains one old Map not-invoked descriptor. Nine
binary membership IR instructions and one two-Keyword evaluated Set constructor
are checked, not a dynamic-allocation census. C8 preserves operand dependencies
and adds no query allocation. Two tracked public C7 gates accept assembly and
reject a membership-policy forgery; a resealed C8 dependency forgery rejects.
The String-needle fixture reaches accepted C5 and passed C6, then rejects at
C7 with `C7-TYPE-MISMATCH/:keyword-set-contains-operand-type-required` followed
by `C7-ANNOTATION/:unsupported-or-unresolved-core-node-type`.

Width remains 1,024 and logical work allowance 65,536. For N actual members,
stored Keyword representation units C and needle units Q, reservation is
`2 + 2N + choose(N,2) + (N+1)(C+Q)`, guarded before products/comparisons.
This accounts logical validation/comparison work, not provider CPU or heap.
Bare Sets, other member families, general collection membership, List-to-Set
conversion, flow refinement, whole-W5 support and authoritative migration
remain outside this slice. SH-07 and the three current-closure evidence groups
remain open; the resolver remains development-unadmitted.

### Evaluated literal-Keyword Sets: development implementation

The typed emitter and Gravity runtime have a bounded body-construction path
for finite literal Sets of Keywords, including empty Sets. C7/C8 production
is unchanged: their existing finite Set types and normal allocation facts feed
the new lowering and execution path. The lowerer authenticates the actual
owner, ordered child/source/type joins and values, then compiles the children
into existing `:set-literal`/`:items` IR. Cached parent `:source-value` is not
used to construct the result. Dedicated failure handling retains the owning
charge, successful prefix and failed child's reported work.

Closed runtime Set instructions admit only literal Keyword children. The
constructor validates the complete actual domain, prepays bounded logical
content/pair work, rejects duplicate Keyword value classes, then inserts.
It does not silently deduplicate malformed literal/IR inputs. This preserves
L1's literal uniqueness rule, unlike the existing conversion operation.
Instruction traversal includes Set items; evaluated Set module initializers
remain unsupported while quoted Sets remain separate inert DATA.

The corrected focused batch passed seven tests/624 assertions: three owning
kernels (72 lowering, 16 C8 allocation, 148 runtime) and four adjacent Vector,
conversion and Set-equality regressions (388). Counts include existing loader
checks, not 624 distinct new behaviors. The execution envelope took 6.253
seconds after 9.938 seconds of namespace loading at 2 GiB; this is development
feedback, not an isolated performance comparison or provider CPU/heap bound.

The first batch stopped after five tests with 307 passes, one failure and two
errors; two selectors were unrun. Opaque malformed owner/child IDs reached
eager structural equality before their type guards. The narrow repair moves
bounded ID validation before those checks without weakening the hostile
tests. Cached-value isolation passed in that failed batch. Commit `b757b3d3`
preserves the original tested candidate; `7d930eeb` preserves the complete
corrected focused-test source before additive source checks. Independent review
accepted the corrected implementation and focused actuals. Genuine source
composition subsequently passed. The fixture check passed
one test/13 assertions: six positive/String-member byte/parser/function-count
checks, one duplicate-pair byte check and three C2 rejection checks per syntax.
Both duplicate literals reject as `C2-SET`/`:duplicate-set-entry` with no result
committed. This is public reader rejection, not a typed Q product or migration.
The fixture check took 5.067 seconds after 9.752 seconds of loading at 2 GiB.

The changed source composition passed one test/115 assertions (109 composition
plus six existing runtime-loader checks). It constructed one accepted G
program and one G program accepted by C5/C6/C7/C8 but rejected by the public
typed lowerer with `P15S23Q002`/`:unsupported-typed-set-member`. Both C7 and C8
fresh verifications passed before that rejection. This is a lowering frontier,
not a language-wide prohibition on String-member Sets.

The accepted program has seven functions and five static evaluated Set IR
instructions, including empty construction, nine literal Keyword children and
two quotes. Seven top-level Gravity-runtime function-table entries completed,
with seven reference executions through the seed interpreter on the same
emitted IR, not independent raw-source compilation/lowering. Allocation facts
include empty construction; quoted payloads remain inert. Source operands are
pure and do not independently observe once-only order. Three additional tracked
public C7 gates passed/rejected/passed for assembly, a type forgery and negative
compilation. A resealed C8 missing-allocation forgery also rejected; this is not
an all-field tamper claim. Q remains byte/parser/C2-rejection coverage only.

The source selector took 192.446 seconds after 10.193 seconds of namespace
loading at 2 GiB. Inclusive nested construction/verification intervals are
development observations, not benchmark evidence. Runtime has 50 functions,
with one new private constructor helper and unchanged exports; no new seed,
IR operation, namespace or public API was introduced. Keyword List conversion,
`keys` ordering, arbitrary effectful Set members and evaluated Set module
initializers remain separate work. These results do not admit a module revision
or establish broader SH-07, W5, native or self-hosting support.

### Finite Keyword-Set equality: development implementation

Binary `=` has a bounded JVM component path for two independently established
finite Keyword-Set domains, including empty Sets. C7, C8 and the typed emitter
independently reconstruct the source, binding, call, ordered rows and equality
policy. Operand type vectors need not agree, and constructor identity or Set
enumeration order is not an equality premise. Bare Sets and unknown or other
member domains remain implementation frontiers, not language-wide bans.

Both actual Keyword domains validate before a result. Runtime prepays the full
within-operand distinctness and cross-operand comparison/content allowance,
rejects duplicate Keyword value classes in an admitted host representation,
then compares through the existing guarded Keyword leaf. Host Set equality,
lookup/hash and printed spelling do not decide membership. Representation
lengths are logical accounting units only; actual comparison attempts remain
separate from reserved work. Operands retain left-to-right once-only evaluation,
effects and transfers; stored values are inert and the originals unchanged.
The operation adds no semantic collection construction. Runtime now has 49
functions, with two private helpers and unchanged exports and instruction shape.

The corrected focused batch passed 11 tests/2,212 assertions: six new kernels
(521) and five adjacent atomic, integral and Vector regressions (1,691).
These totals include existing runtime-loader checks. Independent review
accepted the inspected implementation and actual focused result, including
the C7 pre-dispatch budget fence, owning rejection-work carry and runtime
duplicate-member regressions. The selected work took 3.915 seconds after
SH01/C7 loads of 10.379/1.182 seconds at 2 GiB, alongside the separate Core
source composition. These are inclusive, non-isolated observations, not a
speedup or provider CPU/heap bound.

The first focused attempt stopped after two tests with 68 passes and one
unsupported-`seq` error; seven selected tests were unrun. The corrected
nine-test/2,155-assertion result preceded the two C7 accounting repairs; the
eleven-test/2,187-assertion result preceded the duplicate-member repair.
Those attempts remain their own evidence, not final-candidate results.
Commit `25bf77d0` preserves the complete source exercised by the final focused
batch before source-integration tests were added. Paired fixture preflight
passed one test/six assertions (bytes, parser interpretation and function
counts only). Its first attempt failed namespace loading before any selected
test ran; that is not a compiler/source execution result. The corrected
source harness compares eight normal entries through the seed interpreter on
the same typed-emitted IR, not independent source compilation/lowering; all
ten Gravity-runtime entries retain explicit expectations, including transfers.
The first source-composition attempt failed: one test reported eight passes,
one failure and one error. Positive C5/C6 status assertions passed, but C7
rejected the fixture's `println` call in `observed-values` with
`C7-ANNOTATION`/`:unsupported-nonlocal-call`, followed by
`:unsupported-or-unresolved-core-node-type`; the prerequisite guard then threw.
Assembly, C8, runtime entries, seed comparisons, tamper controls and the negative
source product were unrun. Its selected test took 136.255 seconds after 10.042
seconds of SH01 loading at 2 GiB. This is a fixture/composition obstruction,
not a passing source result or evidence that Keyword-Set equality itself failed.
The failed candidate is preserved in commit `506212de`. The source-only
correction removed unsupported printing and its IO declarations, retaining
ordinary quoted-Set operands and both genuine caught-throw positions. Its
changed paired fixture preflight passed one test/six assertions; production
and the accepted focused kernels were unchanged and were not replayed.

The corrected source composition passed one test/175 assertions at 2 GiB,
including six runtime-loader checks. It constructed one positive and one
rejected `.gravity` product, executed ten Gravity function-table entries and
compared eight normal entries through the seed interpreter on the same emitted
IR. Two transfer entries retain explicit Gravity expectations. Source stdout
is empty: its pure prefixes are not independent observations of operand order
or once-only evaluation; those observations belong to the focused runtime
kernel. Positive C7/C8 acceptance and verification guard execution; two
additionally tracked public C7 gates, descriptor-field tampering and resealed
C8 dependency deletion are checked. The negative diagnostic vector is the
primary `C7-TYPE-MISMATCH`/`:keyword-set-equality-operand-type-required`, then
`C7-ANNOTATION`/`:unsupported-or-unresolved-core-node-type`.
The negative C7 verifier is invoked, but its status is not separately asserted.

The source test took 230.085 seconds after 9.759 seconds of SH01 loading.
Nested construction/verification times are inclusive development observations,
not independent benchmark samples. The `.qst` products still have byte/parser
checks only, not typed/lowering parity. No unrelated print feature, new public
API, seed extension, bound increase or cached proof was introduced.
Evaluated Set-literal lowering, Keyword List conversion,
`keys` ordering, whole-W5 execution, other targets and authoritative migration
remain separate work; this is not broader SH-07 or self-hosting completion.

### Finite String-Set comparison: development increment

The existing C7, C8, emitter and runtime owners extend binary `=` and `not=`
to independently established homogeneous finite String-Set domains. String
applications carry distinct version, policy, constraint and call-fact identities;
Keyword and neutral-empty outputs retain their previous identities and work.
The shared Vector delegates explicitly retain Keyword-only Set leaves.
No helper, public API, export, arity or instruction operation was added.

Both complete actual domains and within-operand value distinctness validate
before cardinality or membership can return false. The full worst-case
content/pair reservation fits the current remaining allowance before member
comparison; native String UTF-16 lengths are logical units, not semantic
normalization or a provider CPU/heap bound. Guarded native String equality,
not Set equality, hashing, lookup, object identity or printed spelling, decides
member equality. Stored payloads stay inert and original values unchanged.
Inequality retains its own source/type facts and the owned equality/branch
complement, including ordinary operand evaluation and transfers.

Static candidate `45f6a28e` remained unrun: review caught unsupported `dissoc`
in three fact builders and an emitter sibling-producer operator-ID lookup
before its scalar guard. The successor uses closed String fact maps and guards
the lookup, with second-row opaque/composite-ID controls requiring zero
callbacks and the unchanged four-per-ledger reservation.

The first non-fail-fast batch at `8cc17f64` remains failed: 25 tests,
4,397 passes, zero failures and one error. All 24 semantic kernels completed
with 4,393 passing assertions (1,476 in eleven new kernels and 2,917 in thirteen
adjacent kernels). The parser had four passes, then errored while unnecessarily
reading the whole unchanged B14 namespace, reporting `C2-READER` and duplicate
`:unsupported-platforms`. That result does not establish whole-B14 readability.
The test-only successor confines the witness to exact definition bytes in the
owner and fixture and parses only that isolated definition; unchanged semantic
kernels are not replayed. The failed run and its raw log/receipt remain retained.
The corrected parser passed one test/nine assertions. This is a separate
test-only result, not a clean rerun of the original 25-test batch.

The affected source composition passed one test/98 assertions (92 composition
plus six existing loader checks) at `b8790053`, without changing production.
It initializes B14's exact ten-String diagnostic definition and returns
`[true false true]` from two equalities and one inequality. Two original String
equality facts/rows and one original-source atomic-inequality fact/row join
through original C7/C8 and the assembly's public C7 verification. The emitted
plan has three equality instructions and one owned complement branch, not a
runtime `not=` call. Three inert quote instructions join to the original RHS
source values and have no direct allocation; one initializer constructor runs
before entry, with three pooled reads. Runtime inventory remains 54 functions.
The String-Set Vector negative retains primary `C7-TYPE-MISMATCH` with
`:vector-equality-operand-type-required` and secondary `C7-ANNOTATION` with
`:unsupported-or-unresolved-core-node-type`; the check asserts presence of the
primary, not an exact-only diagnostic vector. No negative C8/runtime ran.

The source envelope took 141.159 seconds after 11.307 seconds of namespace
loading at 2 GiB. Positive/negative C5 construction took 31.647/23.217 seconds,
fresh development C6-session construction 38.433/30.420 seconds and same-catalog
replay 3.225/2.481 seconds. These inclusive, nested timings distinguish product
construction from startup, not isolated phase benchmarks or speedup evidence.
Public fresh C6 was not invoked, Q stayed parser-only and no seed control ran.
The source attempt passed once; its earlier quote-oracle draft remained unrun.
Separate language-boundary checks passed six tests/eleven assertions; LOC
validation passed 2,021 source files, zero exceptions, maximum 250 lines.

String-Set Vector leaves, bare/unknown or mixed Set domains, insertion and
recursive accumulator-domain transport remain outside this increment. Full
B14/backend execution, other targets, current-closure admission, SH-07,
self-hosting and release require separate evidence.

### Finite Keyword-Set insertion: development increment

The existing C7, C8, typed emitter and runtime now support binary `conj` of an
independently established finite all-Keyword literal Set and a Keyword value.
STD3's existing rule preserves equal members and adds only absent values.
The normal result is a bare Set; no member, presence, cardinality or private
recursive-carrier facts are invented. The existing empty-Set/Never String route
and List/Vector behavior remain. A nonempty Keyword receiver with Never retains
the non-invoked result/effect rules. Bare, mixed and other unsupported input
domains reject; this does not enable nested insertion without new evidence.

Complete actual Keyword domains and uniqueness validate before hit/miss. One
unchanged membership helper consumes the remaining allowance after reserving
trusted result construction; absent values rebuild a trusted persistent Set.
Present values may retain input storage, but only value preservation is promised.
Both normal branches retain possible allocation and storage obligations.
Selected hostile receivers and opaque IDs are tested, not arbitrary host-provider
CPU, memory or callback behavior. The runtime remains 56 functions, and the
String insertion helper is unchanged. The compiler's guard-Set insertion site
motivates this slice; the whole compiler module was not executed.

The first focused launch at `c696cb31` exited 2 during namespace loading: a new
test referenced a later-defined helper. No tests completed and no timing receipt
was produced. Test-only relocation `8433bc1f` then ran 11 tests with 1456 passes,
one failure and zero errors; 18 tests were skipped by fail-fast. The failure was
an obsolete exact String-domain diagnostic oracle for an unsupported unordered
mixed Set selected by the new Keyword route. That aggregate remains failed.

Test-only correction `c6208ba6` permits the selected String/Keyword rejection
label only for that mixed public case and explicitly requires rejection. Exact
direct-String and pure wrong-family labels remain. Production and fixture bytes
are unchanged by either correction. Ten complete green selectors/1291 assertions
from the failed aggregate and the corrected 19 selectors/1965 assertions cover
all 29 distinct focused selectors/3256 passing assertions. This is two retained
passing partitions, not one green 29-test run. Ten new selectors contribute 918.

One source composition at `70fd49b4` completed naturally: 1 test/96 assertions,
90 composition plus six loader checks. One Gravity function contains three
insertions, three counts and three inert quoted Sets; one closed entry returns
`[1 2 1]` with empty output. C7/C8 construction and verification, allocation,
storage, bare-Set and source joins pass; assembly invokes public C7 verification
once within its observation. Namespace effects are empty and main's latent and
inferred effects are exactly allocation. The genuine Character-Set/Keyword
negative rejects with the existing String-fallback type diagnostic and actual
types/Core/syntax/binding/span, retaining secondary count-Unknown and annotation
diagnostics. No negative C8 or runtime was invoked.

The source run took 143.274301916 seconds in selected execution after
13.726984834 seconds of loading. Timings are inclusive development observations,
not isolated runtime benchmarks or a speedup claim. Q's earlier ten assertions
are parser-only; there is no Q typed composition, public fresh C6 verification,
seed control, native execution, whole-consumer execution or migration credit.
Language-boundary checks pass 6/11; source LOC passes 2021 files, zero exceptions,
maximum 250. SH-07 completion and current-closure admission remain outstanding.

### Finite Symbol-Set insertion: development increment

The existing C7, effects, typed emitter and runtime owners apply STD3's binary
Set insertion law to independently finite literal Symbol Sets and Symbol
values. Original namespace/name equality preserves equal members and adds only
absent values. Full actual domain, coherence, uniqueness and construction plus
membership work precede the result. C8 records possible allocation, including
hits. Runtime remains at 56 functions; results expose only bare Set evidence.

The first frozen candidate `673c25ac` remained unrun after review found a
duplicate C7 selector. Its removal added 14 direct quota/read controls. The
first actual attempt at `aada88a8` failed while compiling the emitter: one
test, zero passes, one error and 34 selectors unrun. Three new selectors had
malformed two-operand `if` forms; an external static check had missed them.
The narrow three-helper correction at `c755727f` passed all 35 focused tests/
3,905 assertions in one current-revision batch: ten new selectors/989 and
25 affected selectors/2,916. No passes were retained from the failed attempt.

The first source composition at `9981231b` passed 1/102 assertions: 96
composition checks and six runtime-loader checks. One function, six calls,
three insertions, three count consumers and six quotes execute to `[1 2 1]`
with empty stdout. Original C7/C8 construction and verification, an observed
public C7 assembly gate and one closed entry were reached. Main retains
inferred/latent allocation; namespace initialization is empty. The actual
Character-Set/Symbol negative rejects at C7 using the existing String-family
insertion reason, with concrete types/IDs/span and secondary count-unknown and
annotation diagnostics retained. Negative C8/runtime are not invoked.

The source run took 152.791 seconds after 12.882 seconds of namespace loading
at 2 GiB. Nested product construction/replay dominates this small subject;
these inclusive observations are not isolated runtime benchmarks. The monitor
is not a hard deadline or containment lease. QST is parser-only; no seed entry,
fresh public C6 verification, native execution or authority renewal is claimed.
Symbol homogeneous carriers, nested insertion inference, Symbol-Set equality
and a loop join for dynamic seen-Symbol scans remain separate work. This is
not whole-helper execution, SH-07 completion or self-hosting evidence.
The normal main join preserved all eleven implementation/test/fixture files;
the added C11/C13 paths are outside this harness's executed dependencies.
Final joined boundary checks passed 6/11 and LOC passed 2,021 files, zero
exceptions, maximum 250. The source result retains its actual revision binding,
not a claim that the joined revision repeated the source run.

### Inferred Symbol-Set carriers: development increment

At `35826d27`, v2 Symbol insertion retains the exact homogeneous Symbol member
carrier instead of the preceding bare-Set result. Repeat insertion and `count`
compose through ordinary local bindings, an inferred zero-argument return and
bounded literal List storage. Existing predicates remain usable. C7, effects
and typed lowering share the same closed carrier rule and v2 policy, constraint,
fact and allocation joins. Old v1 records cannot manufacture the new evidence.
The nine String/private/declared signature predicates and runtime bytes remain
unchanged; runtime still has 56 functions. No extent, presence, membership,
equality, annotation, private/recursive signature or dynamic loop join is added.

All 40 focused tests passed 6,343 assertions in one revision-bound invocation:
four new selectors/489 and 36 affected controls/5,854. Direct carrier work 2
and nested List work 3, exact/one-below quotas, existing depth/width/size bounds,
Never behavior, malformed opaque records and v1 forgery rejection passed.
There was no failed focused epoch. Updating the old source selector's v2 oracle
did not rerun it or confer new source-composition credit.

The distinct source composition at `cb9f0594` passed 1/133 assertions: 127
composition and six loader checks. Two functions, 12 calls, four insertions,
four count consumers, one List construction, two predicates and six quotes
produce `[1 2 2 1 true true]` with empty stdout. The seed return and `seen`/
`widened` locals retain the carrier; `stored` is a literal List of that carrier.
Original C7/C8 checks, one observed public C7 assembly gate and one closed entry
passed. The actual carrier/Symbol membership negative retains the existing
Keyword-family C7 mismatch reason with Core/syntax/binding/span and secondary
unresolved annotation; no negative C8 or runtime executes.

Selected execution took 158.880 seconds after 12.586 seconds of namespace
loading at 2 GiB. These inclusive product-construction/replay observations are
not an isolated runtime benchmark. Monitoring is not hard containment. One G
positive and one G typing negative do not establish Q typed composition, fresh
public C6 verification, seed/native execution, whole-helper support, SH-07
completion, current-closure admission or self-hosting evidence.

### Inferred Symbol-Set membership: development increment

Binary `contains?` now consumes the exact inferred homogeneous Symbol Set and
a Symbol needle, including through a zero-argument return, locals and repeated
insertion. It returns Bool using original namespace/name equality. The existing
C7, effects and typed emitter owners independently reconstruct v2 membership
policy, source, constraint, fact and ledger joins. Carrier work is 3 with exact
one-short rejection; member count remains nil. Finite member validation and
`2 + count` work, Never transfers and legacy-family dispatch remain intact.
Runtime bytes and all nine protected signature predicates are unchanged;
runtime remains at 56 functions. No presence, extent, cardinality, private or
recursive signature, loop-join or Set-equality evidence is introduced.

The first focused run at `9e03b94e` recorded 21 attempted tests, 3,492 passes,
one stale-oracle failure, no errors and eight C8 selectors unrun. The old
insertion test still expected the newly supported query to reject. A test-only
correction at `fdc858c5` passed nine selected tests/1,318 assertions. Independent
review separately found that new shared receiver-key guards walked malformed
wide maps before enforcing their three-key shape. Those pre-correction passes
remain history, not validation of the corrected guards.

At `a7970437`, all three owners check width before key traversal or keyed access.
Three kernels cover non-three-key maps at widths 0/2/4/65,536, exhausted and
admitted quotas, and Symbol/legacy/Never needles, asserting rejection with zero
key traversal and zero keyed reads. The full current focused batch passed
29 tests/5,380 assertions in one invocation: four new selectors/1,563 and
25 affected controls/3,817. These include 900 new wide-map assertions; this is
not a universal hostile-provider CPU/heap bound. Earlier passes are not summed.

The distinct source composition at `f71ca364` passed 1/167 assertions: 161
composition and six loader checks. Two functions, ten calls, four insertions,
five queries, eleven quotes and one local call produce
`[true false true false true]` with empty stdout. Seed and `seen`/`widened`
locals retain the exact carrier; selected v2 queries retain work 3, nil member
count and pure direct effects while operand allocation survives. Original C7/C8
checks, one observed public C7 assembly gate and one closed entry passed.
The wrong-Keyword needle rejects at C7 with actual carrier/Keyword types,
legacy Keyword-family mismatch reason, Core/syntax/binding/span and secondary
unresolved annotation. Negative C8/runtime are not invoked. The existing
rejected fixture and old source oracle were explicitly updated; neither old
source harness was replayed or given new credit.

Selected execution took 155.129 seconds after 12.589 seconds of loading at
2 GiB. These inclusive product construction/replay observations are not isolated
runtime benchmarks; monitoring is not hard containment. Q is parser-only and
development C6 replay is not public fresh C6 verification. The compiler's
seen-Symbol scan remains a motivating subexpression, not whole-helper execution.
SH-07 completion, current-closure admission, native/self-hosting and release
evidence remain outstanding.

### Grounded Symbol-Set loop invariants: development increment

The existing C7/effects/typed-emitter owners now infer a Symbol member invariant
for an independently empty Set loop slot with an independently grounded normal
re-entry. The supported cold witness is v2 `conj` from an empty seed and a
literal/quoted Symbol, including acyclic immutable aliases. All normal entries
must be compatible empty Sets or the same carrier. Actual initializer and
transfer types remain distinct from the invariant; a Never operand anywhere in
the transfer tuple cannot ground it. C8 and lowering reconstruct the cold
premises independently. Runtime and the nine protected signature predicates
remain unchanged. General Set joins, private/recursive signatures, presence,
cardinality, termination and whole compiler-helper execution are not added.

At `608d64a3`, the complete focused batch passed 28 tests/3,348 assertions:
four new selectors/187 and 24 controls/3,161. This covers original loop/function
behavior, independent cold aliases, normal empty/carrier entries, Never before
and after the candidate coordinate, exact/one-below work and opaque quote-child
guards. Earlier load, scope, quote and evaluation-metadata fixture failures
remain distinct; their partial passes are not added to this batch.

The source composition at `44654c7d` completed naturally with 67 passes and one
failed assertion out of 68. The real G alias-loop passed C7/C8 verification,
one observed public C7 assembly gate and one closed runtime entry returning
true with empty stdout. The G conflicting re-entry was rejected and freshly
verified, with Keyword-Set actual type, Core/syntax/binding/span/constraint
locations and secondary unresolved diagnostics. Its expected type was the
actual empty initializer, not a carrier: inference remained pending and did not
publish an invariant. The failed assertion expected that unestablished carrier.
The test-only oracle correction does not relabel this failed receipt or rerun
the unchanged source composition. Known-carrier selected conflicts and pending
fallback have separate direct checks. At `93306d73`, the affected C7 kernel
passed 77 assertions: 70 retained and seven new pending-fallback checks. The
corrected source selector compiled but was not executed again. Q remains
parser-only; these observations do not establish fresh public C6 verification,
native execution, current-closure
admission, SH-07 completion, self-hosting or seedless release.

### Evaluated empty Set loop initializers: development increment

The same current JVM route now cold-grounds an evaluated unquoted `#{}` loop
initializer. C6 already produced `:collection-literal`, and C7 already knew its
literal empty-Set type; the previous cold check recognized only literal/quote
nodes. All three C7/effects/lowering owners now check the closed constructor
record and its actual empty source value, source identities, zero children and
bindings, and empty left-to-right evaluation. The initializer remains empty;
only the separately grounded Symbol invariant types re-entry. Existing Set IR
and constructor allocation are retained, rather than treating it as inert DATA.

The `b0a88558` RED kernel retained 14 passes and seven failures in 21 assertions.
At `089b6a2e`, the focused batch passed 17 tests/791 assertions: four new
selectors/285 (C7 95, C8 83, lowering 89, parser 18) and 13 controls/506.
These cover source/type/evaluation tampering, opaque fields without callbacks,
exact/one-below work, alias evidence, allocation/backedges and quoted controls.

The changed G source composition at `e0cd7d87` passed one test/70 assertions.
The positive unquoted loop passed fresh development C6 session/replay, C7 and
C8 verification, one observed public C7 assembly gate and one closed runtime
entry returning true with empty stdout. Its initializer is a real evaluated
empty Set constructor, with allocation and the existing Set IR. The nonempty
Keyword-Set seed plus Symbol is rejected through the existing insertion-domain
diagnostic, with Core/syntax/binding/span/constraint provenance and a passed
C7 recomputing verifier. Negative C8/runtime construction was not invoked.

The earlier quoted-source 67P/1F receipt remains failed and was not replayed or
relabeled by this distinct changed-source run. Runtime remains 56 functions;
the nine protected signature predicates, L5 law, schema and work bounds remain
unchanged. Q is parser-only. Nonempty/dynamic grounding, generic joins, flow
refinement and whole compiler-helper execution remain unsupported here. These
development observations do not establish fresh public C6 verification, native
execution, current-closure admission, migration, SH-07 completion or release.

### Vector-first Symbol loop grounding: development increment

The current JVM C7/effects/typed-emitter route now grounds the existing v2
Symbol-Set loop invariant from `first` of an evaluated nonempty Vector whose
stored head is independently a literal/quoted Symbol through immutable acyclic
aliases. The original Vector receiver must be the evaluated constructor; its
complete positional type, children, source/forms, bindings, call/edge records,
evaluation order and original Vector-first descriptor are reconstructed.
Member aliases share the same remaining budget and cycle set through the head
path. Quoted/empty/aliased/function-returned Vector receivers, nested-call head
grounding, expected types and loop slots cannot supply this independent origin.

The empty Set initializer and original transfers remain unchanged, including
whole-tuple Never and normal empty/carrier compatibility. C8 and lowering
independently rebuild the existing F/W evidence; no schema, signature predicate,
runtime function, general join, flow refinement or guard elision is added.
Both Vector and Set construction retain allocation, and `first` remains a pure
query with its original runtime checks. Runtime inventory is still 56 and all
nine protected signature predicates are byte-identical.

The final production correction is `3737b5b0`. Reached-source projections omit
only unused C6 fields, and the actual full warm descriptor is paid once at its
final equality boundary. Discarded Keyword formatting is removed from the new
two-visit LSI data walker and the existing one-visit C8 Map/Vector evidence
walker. The latter change affects its 22 existing callsites: long Keyword names
can gain logical-budget acceptance. String/Symbol text charges, first-order
family/depth/pending reservations, full row checks, native equality and its
doubled charge remain; no ceiling is raised. Internal operational counters may
change, while published descriptor, presence, row, invariant and graph checks
remain exact. This does not establish a CPU, heap, hash/equality-cost or hostile
provider bound.

Qualified focused coverage is 44 distinct selectors/2,005 passing assertions
across preserved epochs, not one all-green invocation: unaffected 29/1,496
from `2092fd07`, the corrected cold solver 1/13 at `b7f72147`, new C8 and
private-source checks 8/237 plus affected old loop checks 5/227 at `3737b5b0`,
and the diagnostic oracle 1/32 at `f9026e4d`. Cold C7 converges in five rounds
at exactly 60,847 units and rejects one below. The complete normalized C8
analyzer accepts at 49,576 units with identical observed/unobserved and
exact-limit results, and rejects at 49,575. Earlier failed development receipts
remain failed; their passes are not added again.

All four genuine source attempts are preserved. `ab180a09` and `33b732fb` each
stopped at C7 work limit with 3P/1F/1E; their downstream and negative source
checks were unrun. `f2472dba` passed C7 and its verifier, then stopped at C8 work
limit with 4P/1F/1E; C8 verification, assembly/runtime and the negative were
unrun. At `2677614a`, the complete composition ran 81 assertions: 80P/1F/0E.
Its one-function, three-call G positive passed C7/C8 verification, one observed
public C7 assembly gate and one closed runtime entry returning true with empty
stdout. The retained five-ID member path reaches the original Symbol quote,
with empty initializer type and constructor allocation preserved.

The G empty-Vector negative was rejected with
`C7-ANNOTATION`/`:vector-first-nonempty-complete-positional-type-required`, actual
literal empty-Vector type, source/Core/syntax/binding provenance and passed
fresh C7 verification. Negative C8/runtime were intentionally not invoked.
The sole failed assertion incorrectly expected a constraint map. The original
renderer supplies a present, nil constraint ID for annotations, with nil parity
inside the diagnostic identity request; only type mismatches acquire that map.
The final test-only correction is independently checked against the retained
actual diagnostic and the original derive/renderer kernel, including a real
arity mismatch. It does not manufacture an 81- or 84-pass source receipt.
Raw source outputs use the unique
`vector-first-symbol-loop-witness-source-c8-keyword-visit-20261021` prefix.

The source invocation remains bound to its exact `2677614a` inputs. One main
join to `bc7dbe7d` produced `87c9cd14`, preserving every SOURCE production,
fixture and test byte. Fresh current-main checks passed five load-order checks,
representative C7/C8/emitter 3/44, boundary 6/11 and LOC (2,022 source files,
zero exceptions, maximum 250 physical lines). The 3/44 refresh overlaps the
44-selector total. Q remains parser-only. Public fresh C6, native execution,
whole compiler-helper support, current-closure admission, migration, SH-07
completion, self-hosting and release are not established by these observations.

### Persistent String-Set insertion: development increment

STD3 now explicitly defines binary Set `conj` to preserve existing equal member
classes and add only an absent value class, without changing the original Set.
The semantic rule agrees with ordinary Clojure Set insertion; no object-identity,
physical-sharing, enumeration or allocation-free guarantee follows from it.

The existing C7, C8, emitter and runtime owners implement the bounded String
case: independently established finite all-String Set input and a String value.
The normal result is deliberately a bare Set, with no invented cardinality,
literal ancestry or retained member-domain evidence. `count` can consume it;
another finite-domain insertion, membership or equality needs independently
established domain evidence. Private inferred accumulator transport remains a
separate slice, not an implicit consequence of insertion.

Both complete actual member domains and value-class uniqueness validate before
either hit or miss. The full logical content/comparison and trusted construction
reservation precedes member comparison. Guarded String value equality decides
membership, not receiver Set lookup, hashing, equivalence or construction hooks.
An absent member uses the trusted persistent rebuild; a present member may retain
the original storage. Logical work is not a provider CPU, peak-heap or physical
allocation measurement. Normal invocation retains possible allocation and storage
obligations on both branches; ordinary operand evaluation and transfer semantics
are preserved. The runtime private closure now has 55 functions; this inventory
is an implementation fact, not an authority-admission gate.

The first frozen candidate `8e957a91` remained unrun. Independent review found
that shared dispatcher Unknown probes hashed composite types before their paid
validation. The narrow successor `f8587224` uses nil-left and keyword-guarded
probes, with direct dispatcher opaque-member and opaque-inserted-type controls
at allowances zero, one and 65,536 requiring zero callbacks.

One non-fail-fast focused batch passed 22 tests/2,469 assertions with zero
failures or errors: six new semantic kernels/705 assertions, one paired G/Q
parser kernel/ten assertions and fifteen adjacent kernels/1,754 assertions.
The original process completed naturally with exit zero. Selected execution
took 11.767 seconds; namespace loading took 14.086 seconds, with C8 already
loaded. These are development observations, not benchmarks or migration proof.
The new normalized transfer controls cover the inserted-value position;
receiver Never remains in adjacent-owner coverage and runtime controls cover
both throwing operand positions.

The first affected source composition at `e0abb872` remains a failed aggregate:
one test/83 assertions, 82 passes, one failure and zero errors. It completed
the actual closed-plan result `[1 2 1]` for empty, absent and present insertion,
all three insertion/count source joins, allocation/storage rows, public C7
assembly verification and the genuine Character-member C7 negative. Three
inert quoted receivers are not evaluated Set constructors; no module initializer
runs, and `count` consumes the bare result as Integer without invented extent.
The full rejected diagnostic vector retains the primary String-insertion type
error, a downstream count type error and an unresolved-node annotation error;
the test asserts presence of the primary, not an exact-only diagnostic vector.
No negative C8 or runtime ran; Q remains parser-only and no seed control ran.

The sole failing expectation put function-body allocation in the namespace
initialization effect set. C8 correctly leaves that set empty for a module
containing only a function definition; the function's body effects are latent
at definition time. The test-only correction checks empty namespace effects
and the actual main function's allocation summary. This does not change the
compiler, fixtures or the already accepted focused results, and does not turn
the failed source aggregate into a passing run. The corrected full source test
was not re-executed; unchanged source products were not rebuilt merely to obtain
a green aggregate.

The existing original C8 constructor-allocation regression separately passed
one test/107 assertions after the exact function-ID scope correction. It
exercises the actual node derivation and graph builder, checks inferred and
latent allocation on the function, empty namespace initialization effects and
the original allowance checks. Selected execution took 1.446 seconds after
8.084 seconds of namespace loading. Separate language-boundary checks passed
six tests/eleven assertions; LOC validation passed 2,021 source files, zero
exceptions and maximum 250 physical lines. No production change was needed
after the first successful focused batch.

That source envelope took 146.482 seconds after 12.359 seconds of namespace
loading at 2 GiB. Positive/negative C5 construction took 31.360/24.490 seconds,
fresh development C6-session construction 37.685/33.918 seconds and same-catalog
replay 3.274/3.061 seconds. These inclusive observations separate verification
product construction from startup; they are not isolated phase benchmarks.
Public fresh C6 was not invoked. Full B14/backend execution, recursive Set
domain transport, other targets, current-closure admission, SH-07 completion,
self-hosting and release remain separate gates.

### Inferred String-Set private values: development increment

The existing C7, C8 and typed emitter owners retain a closed inference-only
String-Set member domain through insertion and private inferred ordinary
self-calls. The representation is exactly
`{:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string}`.
Independently established insertion derives it, subsequent insertion preserves
it, and membership and count consume it. It carries no extent, literal ancestry,
presence fact or equality support. Bare Set and a quoted empty entry do not
establish this domain. No Set annotation grammar or declared-recursion rule was
added; declared-self predicates and the List-only parameter premises are
unchanged.

Inference still requires complete independently typed normal entries, the same
monomorphic parameter vector, current use/export/escape and self-call census,
an independent cold-body return witness, and complete self-argument checking.
Non-invoked entries do not supply a witness; a callee's Never result is not an
argument-invocation guard. C8 and both lowering proof consumers reconstruct
the same domain. Insertion and String-membership descriptor, constraint and
fact identities advance to version two with explicit carrier-schema and
invalidation metadata. Old version-one evidence is not relabeled as current.
The runtime remains unchanged at 55 private functions: full actual-domain,
uniqueness, width and work checks remain necessary, since the carrier proves
no static size bound.

Static candidate `179f4c13` remained unrun. Review found that the generic
closed-value walker would reject the richer insertion result, regressing
predicates and List construction which previously accepted the bare Set.
Successor `b166cbd4` recognizes only the exact carrier in the existing paid
homogeneous branch and still visits its actual String leaf. Three owner
kernels check direct and nested exact/one-short costs, insertion-produced
values through List and predicate descriptor paths, and malformed opaque
records without callbacks. No general homogeneous-collection rule, cap increase
or acceptance cache was introduced.

The first non-fail-fast carrier batch at `b166cbd4` remains failed: 34 tests,
3,038 passes, three failures and three errors. All selectors completed; 28
other tests passed. Two rejected Keyword/Character membership cases exposed
diagnostic-family changes in the new homogeneous branch. A pre-existing String
membership assertion still expected finite String-Set equality to reject,
although that behavior had already been implemented by PR100. Three opaque-key
guard tests errored during hosted `array-map` duplicate-key checks, before
calling the candidate guards. These are distinct outcomes, not three production
callback failures. The raw log and timing receipt preserve the complete batch.
Selected execution took 15.829 seconds after 13.397 seconds of namespace loading;
the corrected cases require separate targeted feedback, not relabeling this
aggregate as passed.

The narrow correction `f2df013b` restores existing needle-family rejection and
quota reasons in all three membership owners, constructs deliberately malformed
opaque-key fixtures without invoking host key equality, and corrects the stale
finite-String equality expectation. A separate 12-selector non-fail-fast batch
passed 1,569 assertions with zero failures or errors. The three new guard
kernels each completed 69 assertions, including their previously unexecuted
opaque-key tails. The other nine checks cover Keyword, Character and String
membership across C7, C8 and lowering. Selected execution took 8.226 seconds
after 13.681 seconds of namespace loading at 2 GiB.

Twenty-six unchanged complete passing tests from the first batch, totaling
1,982 assertions, were not replayed. The corrected batch also reran two affected
previously passing checks and added four family-owner checks. These are separate
results, not one green 38-test run. The initial failed batch and unexecuted
`179f4c13` remain preserved. Source composition is a separate integration gate.

The affected source composition at `ad9ae36f` passed one test/79 assertions
(73 composition plus six existing loader checks), returning `[true true false]`.
Three normal caller entries independently seed the String-Set domain through
insertion; the private helper retains `[Bool String carrier]`, an independent
Bool return witness and a complete four-call census including its self-call.
Four version-two insertion facts/rows and one membership fact/row pass original
C7/C8 verification and assembly's public C7 gate. The emitted plan contains two
functions, nine calls and three inert quoted empty Sets, not evaluated Set
constructors or module initializers. Both function bodies retain inferred and
latent allocation; namespace initialization effects stay empty. One closed-plan
entry runs against the unchanged 55-function runtime; no seed control runs.

The genuine direct-empty-entry negative reaches C7 and is rejected with
`C7-ANNOTATION`/`:private-self-independent-entry-types-required`. Its retained
diagnostic has nil source IDs and span in the existing aggregate failure path;
this establishes the rejection, not source-rich diagnostic conformance. A
bounded follow-up should carry the offending entry's existing census/source
coordinates into that diagnostic. No negative C8/runtime ran and Q remains
parser-only.

Selected source execution took 165.782 seconds after 11.825 seconds of namespace
loading at 2 GiB. Positive/negative C5 construction took 33.867/28.371 seconds,
fresh development C6-session construction 42.442/38.298 seconds and same-catalog
replay 4.218/3.875 seconds. C8 construction/verification took 1.050/0.057 seconds;
assembly including public C7 verification took 3.451 seconds and closed-plan
execution 0.174 seconds. These inclusive nested observations distinguish
verification-product construction from loading, not isolated benchmarks or
speedup evidence. Public fresh C6 was not invoked. Homogeneous Set equality,
B14's empty-only entry, heterogeneous record ingress, indexed recursion,
whole-B14 execution, other targets and authoritative migration remain separate.
Separate language-boundary checks passed six tests/eleven assertions; LOC
validation passed 2,021 source files, zero exceptions and maximum 250 lines.

### B14 accepted-result source readability: development repair

The retained String-Set parser error exposed nine repeated fields in the
single evaluated Map returned by `w5-b14-accepted-result`. Removing only the
redundant trailing pairs preserves all 43 fields, the nine retained values,
fourteen request projections and the incomplete/nonauthority/target flags.
The exact diagnostic Set definition used by the comparison witness is unchanged.

One reader-only regression passed 26 assertions at 512 MiB: hosted whole-source
reading, exact definition/Map shape and a bounded hosted duplicate-key negative.
It did not force the namespace's delayed compiler plans or execute the result
function. The selected check took 24.127 milliseconds after 7.355 seconds of
namespace loading; these are development observations, not benchmarks.
The stale test-header clearance instruction now distinguishes this direct
reader check from the full stage2 plan/record suite.

This repairs an actual hosted-reader obstruction, not a newly selected canonical
Gravity duplicate-Map-key law. B14 source identity changed; old B14 provenance
and dependent artifacts are not renewed or interchangeable. The proposed first
frontend/Core cohort selection and endpoint are unchanged; broader backend-conformance
admission still requires its selected fresh evidence. Hosted readability is
not a B14 compiler build, backend execution, conformance, self-hosting or release
result. The historical failed parser run remains failed at its original revision.

### Declared ordinary direct-self calls: development implementation

The existing C7, C8, typed emitter and runtime now have a separate declared
direct-self path, preserving the earlier positional/literal-base proof. One
named fixed-arity function in one singleton self component may use a complete
monomorphic value signature in the supported atomic or homogeneous-List domain.
Every body node, incoming argument, self-argument and actual body return is
still checked. Declared value types provide no effect or thrown-error evidence.

C8 reconstructs the source/component/call/capture joins and derives effect and
escaping-error summaries from bottom. Function summary records compose with
the existing effect-set loop summaries, so an acyclic caller can retain its
ordinary explicit function recur. Classification, parameter slots, rounds,
final reconstruction and equations consume the unchanged cumulative budget.
Ordinary self-calls lower to ordinary call IR and real activations, not recur
or TCO. The runtime preserves pre-argument target/arity/path guards, once-only
left-to-right operands and fresh activation locals. Only the contiguous
current-function suffix may repeat; separated indirect cycles still reject.

Focused runtime checks passed 4 tests / 223 assertions. C7's final component
batch passed 6 / 188, followed by a changed diagnostic-only 1 / 5 control.
C8 passed 3 / 71, then the changed caller-loop combination passed 3 / 96;
the explicit wide-parameter prefix debit passed a separate 1 / 5. Lowering
passed 2 / 75. These batches include overlapping loader and regression
assertions, not disjoint new feature counts. The 1,024-slot prefix control is
normalized accounting evidence, not a full wide-arity source compilation.

Earlier C7 attempts remain failures: 33 passes / two failures, then 115 /
three, then 184 / four. They exposed test ordering, a pre-existing normalized
fixture ordering mismatch, and a stale structural owner assertion. The
read-only HEAD comparison printed the same three old-fixture failures despite
its shell exit zero; it is not a passing baseline. The initial lowering attempt
failed loading the new test before assertions. Changed successors above do not
relabel or erase those results. No unchanged expensive source proof was replayed.

The sole source composition completed with 211 passing assertions, one failure
and no errors: 205 source checks passed in addition to six loader checks.
Selected execution took 615.798 seconds after 9.560 seconds of namespace
loading at 2 GiB. Five fresh products cover the accepted Gravity/QST pair,
wrong recursive argument, normally returning Bool body against declared String,
and absent source effect allowance. Negative QST files have byte-parity checks,
not additional source constructions. The allowance negative removes both
allocation and throw declarations; it does not isolate allocation causality.

Both positive sources completed twelve closed executions after accepted C7/C8
construction and passed verification: empty, one- and two-element traversal,
a deeper String throw and two argument-transfer cases. The two-element entry
first uses actual Bool function recur in an acyclic caller. Lowering retains
one ordinary transformed self-call, not a recur conversion. String concatenation
has derived allocation; non-invoking calls retain operand effects without
borrowing callee errors. Three tracked public C7 assembly/forgery gates are
separate from upstream verification. The real source graph also passed exact
and one-less C8 work checks, equation-valid phantom-error inflation and fresh
public verification rejection of that nonleast candidate. Executions compare
String values, not thrown-object identity.

The failed assertion compared complete lowered Gravity/QST function records,
including their legitimately different physical provenance paths. The failed
output was truncated, and the complete original tables were not persisted;
there is no claim that every original table difference was independently
enumerated. Type/effect identities and actual execution-result parity passed.
The run remains failed, not a green composition or authoritative equivalence
proof. A normalized control of the corrected comparator passed 1 test / 20
assertions using the existing typed-context and lowering owners. It removes
only each function's physical source path; IR, types, source/compiler hashes,
other provenance and nested origin-path differences still fail comparison.
Its first setup attempt remains 15 passes / four failures from a rejected
synthetic whole-compile entry, not valid function-table evidence. The corrected
source selector's sixteen per-function path assertions and projected table
comparison were not rerun on the original products. This correction does not
recover the original tables or turn the failed source run into a green one;
the five unchanged expensive products were not replayed.

Inferred recursive signatures, mutual/indirect recursion, arbitrary captures,
non-core named helpers and try/loop/recur inside the self body remain separate
frontiers. Supplied-path depth checks do not prove host-stack safety or
termination. No full W5, provider/profile legality, other-target, authoritative
migration, self-hosting, seed-retirement or release claim follows from this
development increment.

### Same-type integral value equality: development implementation

Binary `=` now compares original values of two independently established
operands of the same integral type: Integer, I8/I16/I32/I64 or U8/U16/U32/U64.
The numeric-owned descriptor retains the existing core compatibility binding.
STD5/SAFE9 comparison is `:proof-required` and `:proven-safe`: it performs no
conversion or arithmetic, so it cannot overflow or round. Generic Integer
does not select a new fixed-width or arbitrary-precision arithmetic policy.
Mixed types and unsupported domains reject; a Never operand does not excuse
the other operand's legality. Numeric Vector members remain unsupported.

C7 records the distinct numeric constraint and call fact; C8 independently
reconstructs its type, source, binding and policy premises. The typed emitter
preserves once-only left-to-right operands and rejected-work accounting.
The existing runtime integer equality primitive is unchanged. Its represented
Integer checks are not authority to admit arbitrary host values into typed
fixed-width source parameters.

Focused type/runtime checks passed 6 tests / 1,964 assertions, including atomic,
nil-bearing, Vector and integral-ordering regressions. Effect/lowering checks
passed 9 / 2,160. These batches include overlapping loader and compatibility
assertions. All nine integral families and coherent source, binding, type,
numeric-policy and budget negatives have normalized compiler coverage;
runtime checks include adjacent integer values above `2^53`, original values,
operand order and first-transfer suppression. Those focused results alone do
not establish fresh source composition or authoritative migration.

The separate sole source composition passed 1 test / 293 assertions (287
composition checks plus six loader checks), with 345.616 seconds of selected
execution after 9.825 seconds of namespace loading at 2 GiB. Three fresh
products cover the accepted Gravity/QST pair and mixed-I64/U64 Gravity
rejection. Three tracked public C7 assembly/forgery gates are separate from
upstream verification. Twelve closed executions follow accepted C8 analysis
and passed verification: the original W5 modes getter, count equality and
inequality, private ordinal inference and the typed transfer/handler path.
An actual left-operand String throw suppresses the right operand; its handler
leaves no escaping typed error while C8 retains the error effect label and
pending handler-authority boundary. Same-I64/U64 source functions are compiled
and checked, not invoked with raw host values. Negative QST has byte-parity
coverage only. Coherent type/effect/graph negatives and exact/one-less C8
budgets reuse the retained products. No AR attempt failed or was replayed.

Floating, decimal, ratio and mixed-type equality, arithmetic, numeric Vector
members, full W5, provider/profile/ownership and other targets remain separate
frontiers. No self-hosting, seed-retirement, release or performance claim is
made by these development checks.

### Private call-entry Map presence: development implementation

This increment derives literal Keyword presence for an immutable parameter of
a closed private function. It preserves W5's unannotated
`w5-se-span-without-path` helper rather than adding a structural annotation.
Each normally invoking caller must independently establish the same key and
selected scalar type, retaining its own argument source, shape and coordinate.
Matching member-type vectors do not establish keys or a common deep shape.
At least one invoking entry is required; an all-aborting or unused function
cannot establish presence vacuously.

The existing C7 owner checks the complete binding-use, resolution, operator,
call and edge census, actual private visibility and absent export, fixed arity,
escape/mutation exclusions and parameter re-entry. C8 and the typed emitter
reconstruct the finite entry premises against genuine retained upstream shapes;
public C7 owns coherent deep-source verification. Helper cycles and function
parameter recur remain unsupported. A recursive caller can still supply an
independently proved literal argument, and nested-loop recur is not function
parameter re-entry. Existing defaulted and finite-constructor Map lookup paths
and runtime instructions are unchanged.

Independent review identified a cold-inference accounting defect before any
source composition: multiplying every module call by unrelated module data
at each of five lookups and each inference round could not fit the unchanged
65,536-unit budget. The correction retains fresh analysis, reserves global
scans first, then charges actual canonical incoming calls and measured
argument, child, binding-slot and component widths before detailed checks.
Failure/fallback retains spent work. No cache, new wrapper, larger budget or
smaller source helper is used. Known symbol text conversion remains bounded
by the existing canonical scalar contract, not a hard local CPU/allocation
precharge. Candidate entry count must match the actual census before gathering
supplied shapes.

The normalized cold regression passed 1 test / 19 assertions: four functions,
eight calls and five gets infer from Unknown skeletons in six rounds, using
46,030 cumulative units. The exact budget reproduces types and returns with
zero units left; one less rejects. Its first attempt remains failed with 13
passes and six failures: the fixture's node renamer also renamed the semantic
`:operator` position tag. Only that fixture field was corrected; the rejected
fixture's reported 27,720 units is not successful convergence evidence.

The accounting-focused batch passed 6 tests / 209 assertions. A later guard
delta passed 3 / 60, adding scalar and oversized candidate-entry rejection to
the C7, C8 and emitter kernels. These overlap earlier component results and
are not an additive green aggregate. They are development feedback, not source
composition, authoritative module migration, full W5 execution, target
completion, self-hosting or release evidence.

The first source composition remains failed: 1 test / six passes / one failure
and one error, after 108.525 seconds of selected-test execution at 2 GiB. The
first accepted-source candidate instead rejected all five lookups in C7 with
`:map-get-entry-families-required`. No accepted C7 source product, public assembly,
C8 result or guest observation completed; the QST and negative products were
not built. Static inspection of the owning C6 constructor and exact schema
identified the defect: canonical resolutions live under
`[:preserved-resolution :resolution-table]`, while the new consumers and their
normalized inputs incorrectly used the flat identity-preimage field. The
correction belongs to those consumers and test inputs, not a C6 schema change
or a fallback authority channel. The corrected-path focused batch passed six
tests / 108 assertions, including rejection of a flat lookalike table and the
same six-round cold inference with exact/one-below budgets.

The corrected-candidate source composition passed 1 test / 279 assertions
(273 composition checks plus six existing runtime-loader checks) in 322.282
seconds of selected execution, with 9.050 seconds of namespace loading at the
unchanged 2 GiB heap. It built the accepted Gravity/QST pair and one real
missing-key Gravity rejection. Four tracked public C7 gates cover the two
assemblies and two coherent entry forgeries, separately from upstream
verification. Six guest entry evaluations check all five returned fields and
path removal. Each accepted case retains five private-entry descriptors and
constraint rows over two genuine incoming spans with different key orders.
Whole C8 results, equation/ledger/graph hostiles, exact/one-below C8 work and
semantic G/Q equality passed. All sixteen selected inputs remained unchanged.

This supports the unchanged helper in the fixture's explicit allocation
context, not W5's complete module or its current effect declaration. The
negative QST file has parity coverage only. Public or escaping inputs,
unproved/unknown keys, general structural inference, recursive parameter
transfers, other target/profile/provider/ownership gates, authoritative
migration, self-hosting and release remain outstanding. These timings are
development observations, not benchmarks or speedup evidence; the failed V1
did less work and is not a comparison baseline.

### Proven-present Map lookup: development implementation

This increment adds `(get map :key)` when independent finite Map-shape facts
establish the literal Keyword's presence and selected stored type. STD3's
present-key rule preserves false/nil values, inert payloads, the original Map
and Map-then-key evaluation. It defines no absent-key result. C7 derives shape
from the existing owning analysis; C8 and lowering check the retained finite
selection and their own source/type/row premises. Existing three-argument
lookup remains unchanged. No new runtime operation or testing lane is added.

Focused checks currently have six distinct passing selectors / 809 assertions:
C7 present lookup 134, C8 present lookup 190, present lowering 154, and existing
defaulted lookup C7 183, C8 68 and lowering 80. The lowering count includes six
unchanged runtime-loader assertions; its five normal field extractions observe
String, Integer, Keyword, false and nil values. These are normalized inputs,
not accepted source modules.

The first batch remains failed with 794 passing assertions and 15 failures:
full inference had not registered two-argument lookup's shape-context demand.
The correction selects only the authenticated two-argument operation in the
existing demand loop. Its first targeted run stopped on a delimiter error
before semantic assertions; the corrected targeted C7 run passed 134. The
other five passing selectors were retained, not rerun for a green aggregate.

The paired-source composition passed 1 test / 416 assertions, including six
unchanged runtime-loader assertions, in 424.148 seconds at a 2 GiB heap. It
constructed exactly the accepted Gravity/QST pair and the existing empty-Map
missing-key rejection. The accepted products cover 15 functions, 15 calls,
12 present-key lookups (eight normal and four non-returning), one unchanged
defaulted lookup and 30 guest executions. Eight caught String identity checks
preserve real Map/key transfers. Local Map returns and immutable aliases retain
their independently derived selection facts; original Maps and quoted payloads
remain unchanged and inert.

Both whole C8 results passed. Exact/one-below work, ledger/effect mutations and
same-type wrong-field selection reject. Public C7 reconstructs deep source
shape; C8 checks selection against genuine retained input shape, not an
independent quote/type solver. The real missing-key source now rejects with
`C7-ANNOTATION` / `:map-get-presence-required`; its QST counterpart was not
separately built. All 15 selected inputs remained unchanged. This is executable
development support, not unknown-member or dynamic-key lookup, an absent-key
policy, whole W5 execution, authoritative migration, target completion,
self-hosting or release evidence.

### Bare Map parameters: development implementation

This slice adds explicitly annotated bare `Map` parameters and family-only
Map removal. L5 now specifies per-parameter forgetting of independently
established member refinements, retaining actual argument types, provenance and
compatibility evidence. This does not establish keys, members or a constructor
shape inside the callee. Exact bare-Map returns use the existing equality rule;
the parameter rule does not introduce a return restriction or change recur.

The existing checked-core, C7, effect and typed-emitter owners implement the
increment; the runtime is unchanged. Family-only removal retains receiver
node/source/type evidence but no input shape, member transition or result
shape. Normal invocation returns bare Map with possible allocation; operand
transfer remains Never and suppresses invocation. Count and membership require
no member facts. Current member-lookup, association and structural paths still
require their own established facts.

Independently reviewed development results are:

| Selected checks | Passing result | Scope |
| --- | --- | --- |
| Signature and argument compatibility | 5 tests / 467 assertions | Map declarations, source joins, mixed argument orders, type/work rejection and existing List regressions. |
| Compatibility consumers | 5 / 347 | Independent C8/emitter row reconstruction, actual types, malformed rows and cumulative budgets. |
| Family removal | C7/C8 2 / 138; lowering 1 / 62 | Normal/Never cases, no invented shape, allocation and source/row/work guards; separate targeted results. |
| Count and recur | C8 30; emitter 28; recur 5; old List call 36 assertions | Bare-Map count, unchanged exact recur compatibility and affected call regression. |
| Complete Map-call lowering | 1 / 58 | Real normalized definition/call checks, exact/one-below limits and successful-prefix/failing-child work carry. |
| Fixture and literal-key checks | 2 / 29 | Parity/ASCII/header census plus normalized computed-key rejection, not negative-source compilation. |

These are distinct component results, not a replayed passing aggregate. Failed
history remains: family 173 passes / 27 failures (three real bare-Map count
failures and 24 stale reference-order fixture failures); count/call/recur
148 / 1 (an early call guard omitted rejected work); targeted call 53 / 4
(inconsistent new two-parameter fixture metadata/scope). Corrections preserve
definition guards and successful IR, admit only bare Map to the count family,
and retain the actual argument-based call charge plus completed/failing work.

The affected source composition passed 1 test / 452 assertions, including six
unchanged runtime-loader assertions, in 518.096 seconds with a 2 GiB heap.
It constructed exactly four fresh inputs: the accepted Gravity/QST pair, a
non-Map argument rejection at C7 and an undeclared-allocation rejection at C8.
The accepted products cover 15 functions, 19 calls and 48 guest executions,
including mixed parameter compatibility, exact bare-Map returns, chained
removal, count, membership and caught operand transfer. Public C7 rejects
erased upstream types; C8 rejects missing compatibility evidence with genuine
types retained. Forged family shapes, ledgers and effect graphs also reject.

The five other rejected fixture pairs retain only their fixture or normalized
coverage, and the two selected rejected QST variants were not separately
compiled. All 28 selected implementation, test, fixture and contract inputs
remained unchanged. This establishes the stated development behavior, not
general member access on bare Map, W5 execution, authoritative migration,
release, target completeness or self-hosting.

### Persistent Map removal: development implementation

This slice implements binary `dissoc` with one literal Keyword key over an
independently established Map shape. STD3 specifies persistent removal and
ordinary operand evaluation without restricting the language to this initial
implementation frontier. The four existing owners are C7, C8, typed lowering
and the Gravity development runtime; no new seed operation or test lane is
needed. Normal present, absent and empty removal must all expose possible
allocation, while operand control transfer prevents invocation.

The pure C7 transition kernel passed 1 test / 124 assertions. It checks
present/absent/empty cases, removed-entry and survivor provenance, dense
derived coordinates, false/nil payload types, chained path-key removal DATA,
malformed shape inputs and exact/one-below logical work budgets. The first
run stopped on an unclosed delimiter before semantic assertions; that error
is retained separately from the corrected result. Static independent review
accepted the transformation under its established-shape precondition, not as
a type validator or public compiler result.

The source/derive kernel passed 1 test / 90 assertions. It joins the accepted
owning Map-shape result to the actual argument type before applying removal;
coherent nil/Unknown member echoes still reject. It checks original forms,
resolved operator/call/edge identity, present/absent/empty allocation and
storage policy, malformed arguments and exact-budget boundaries. Independent
static review accepted these source and work-accounting joins. Earlier test
preparation failures remain separate: a namespace scoping error before checks,
then 83 passing assertions and an empty-Map negative's setup index error. The
corrected setup does not weaken the nonempty malformed-member rejection.

The inference increment passed 1 test / 132 assertions. The descriptor is
wired into intrinsic recognition, ordered constraints, call facts and existing
shape consumers; independent static review accepted these C7 paths. The
normalized kernel checks cold present/absent/empty inference, descriptor,
allocation, storage and result-type mutations, exact/one-below work limits,
two-unit Unknown deferral and non-returning input/key cases. Shape consumers
re-enter the owning derivation rather than trusting a stored transition.
At this kernel checkpoint, fresh public C7 reconstruction, independent
downstream joins and actual runtime/source integration were still outstanding;
the distinct composition result below closes those development checks.

The independent C8 kernel passed 1 test / 121 assertions for normal
present/absent/empty removal, coherent descriptor/row/fact mutations,
allocation/storage graph mutations and exact/one-below context budgets. Its
first run remains 118 passing assertions and three errors: the production
context counted malformed source forms before checking their vector shape.
The corrected guard preserves the same rejection oracles; independent review
closed that finding. The Never node-record control proves only retained
operand effects/errors and suppression of direct allocation, not source-bound
removal validation or accepted whole-C8 noninvoked behavior. Genuine noninvoked
joins and whole-module budgets were deferred to the distinct composition.

Malformed input-work guards subsequently added nine checks each to C7 and C8;
their updated kernels passed 141 and 130 assertions. The combined four-test
batch nevertheless remains FAILED: 446 passed and eleven failed. Six lowering
oracles demanded an unperformed final child unit; three Never-case mutations
were unchanged empty effect vectors; two runtime checks omitted the new helper
from the exact source-function inventory. These were test corrections, not
production semantic fixes. Only the affected two selectors were rerun and
passed 186 assertions (147 lowering, 39 runtime). Failure-work checks now
retain prepaid work, completed children and the failing child's actual report,
not invented work. Runtime tests cover persistent removal, false/nil values,
inert payloads, operand order/transfer and invalid values/instructions. Raw IR
computed keys do not widen C7's literal-key source frontier. Retained focused
coverage is 671 assertions across six selectors, not an additive count over
their earlier revisions or a replayed green aggregate.

Twelve affected regression selectors passed 775 assertions, covering existing
association, quoted/structural shape, Keyword matching, get/contains/count and
lowering/runtime behavior, plus eighteen paired-fixture header checks. Root
source review removed an unsupported `Int` return annotation from the new
count example before any source run; the composition checks independently
inferred Integer instead. The final source selector covers
seventeen functions, twenty-one calls, fifteen removals (thirteen normal, two
not invoked), all affected Map consumers, original-value preservation and
genuine operand transfer through C7, C8 and runtime. It selects three fresh
products and reuses accepted products for guest and hostile cases. The
quoted-Map missing-allocation case isolates removal's own allocation policy;
other negative pairs have no fresh source-acceptance result yet.

Its first actual invocation passed six loader assertions, then errored before
any source product completed. The existing C2 default path attempted persistent
cache publication and hit its unchanged 512 MiB aggregate admission limit
(538,990,473 observed bytes). This establishes no Map-removal semantic result.
The bounded process correction routes the ordinary reader to its existing
fresh authenticated producer, leaving the explicit cache API, validation,
capacity policy and shared store untouched. Its two affected reader tests
passed 42 assertions; four unchanged strict-admission assertions passed
separately. The first focused batch remains 43 passes and three failed test
expectations (cold publication status and interrupt observation timing), not
a production failure or an additional passing aggregate. Independent review
accepted the corrected dispatch, parity and validation results.

The changed-dependency source composition then passed 1 test / 448 assertions
in 421.346 seconds of selected-test execution at a 2 GiB heap. It constructs
exactly three fresh products, makes four tracked public C7 verification calls
(two accepted assemblies and two coherent forgery rejections), and performs
34 guest executions. Genuine input/key control transfer, surviving source
coordinates, whole-C8 allocation and budgets, ordered removal-row census,
typed lowering and original-value preservation pass on the real source pair.
The quoted-Map absent-key case rejects undeclared allocation. All 28 reviewed
inputs stayed unchanged. These are development integration results, not
authoritative module migration or a generalized performance measurement.

Supported behavior is bounded to binary removal with a normal literal Keyword
key and an independently established Map shape, including present, absent and
empty Maps. Normal invocation retains possible allocation; operand transfer
prevents invocation. General Map parameters, arbitrary source keys, full W5
execution, documented-target completion, authoritative migration and
self-hosting remain outstanding. Other negative fixture pairs retain focused
kernel coverage and fixture parity, not fresh source-rejection credit.

### Homogeneous List canonical-if joins: development implementation

This slice supports an independently inferred common List element
type for unequal normal `if` branches, such as `(list part)` and `(list)` when
`part` has established String type. L5 now states the bounded rule: homogeneous
or uniformly typed positional branches can supply the same atomic element
type; an empty positional branch supplies none. Expected return declarations
cannot supply missing evidence. Equal-type and Never rules remain unchanged.

Implementation belongs to the existing C7 type owner, C8 effect checker and
typed emitter. It retains actual branch types and a source-bound
join-specific compatibility premise, not false equality. The shared normal
join used by match, try, loop and recur must not be widened. No runtime
conversion or new instruction is introduced.

The independent type-helper kernel passed 1 test / 817 assertions over the
15 currently supported atomic element types, both branch orders, empty and
homogeneous branches, incompatible or missing member evidence, malformed
shapes, and exact/one-below work budgets. The initial helper attempt stopped
before semantic assertions on unsupported bootstrap `dissoc`; it was replaced
with the existing projection helper, not a seed extension. The normalized
source-premise kernel also passed 40 assertions, including coherent branch
reordering, source/binding mutations and exact-budget checks. Its earlier
delimiter failure and 38-pass/two-failure test-oracle result are retained;
the corrected reorder negative keeps the condition first instead of making
the join ineligible before its source check. The inference kernel and existing
return-branch-independence regression passed 2 tests / 115 assertions (108 new
inference checks). Nominal and normal-return results, the final function return
and compatibility row agree; non-Bool conditions preserve L2 truthiness and a
Never condition prevents a normal result. An earlier 84-pass/four-failure
oracle compared retained caller budget metadata as well as semantic results;
the corrected comparison excludes only that scheduling context while keeping
types, rows, diagnostics and work counts. Independent review then found the
unknown-condition deferral failed to check its one-unit budget. The corrected
inference kernel passed 128 assertions, including zero/negative remaining
budget and cumulative exhaustion. That C7 finding is closed.

The updated source-owner kernel passed 46 assertions after adding namespace,
binding-kind and binding-class negatives. Independent C8 context reconstruction
passed 33 assertions covering ordered row census, missing/duplicate/orphaned
rows, changed types and source/binding premises, and exact/one-below budgets
for both the new join and legacy equal/Never cases. New downstream contexts
also check the cumulative one-unit cost of non-applicable joins.

The lowering kernel passed 142 assertions with branch values matching each
declared finite or homogeneous type, and condition types matching all twenty
Bool/nil/Keyword/String runtime cases. It emits the existing `if` instruction
and preserves the selected List value's identity. Its earlier 122-assertion
result is retained but did not use matching runtime values for every declared
branch family. The failure-work correction passed the updated 186-assertion
kernel: own rejection paths retain known prepaid work, and failures at each
of the three child positions retain earlier completed work. Preflight
reservation counts are not claims of actual source visits or CPU cost.

The affected 14-test regression batch remains FAILED: 824 assertions passed
and 12 failed in the existing guarded-vector cold-inference kernel. The new
deferral path had suppressed the old exact/Never branch seed while a condition
was still unknown, preventing loop inference from converging. The correction
retains only that legacy seed during deferral, not a new homogeneous join or
compatibility row. Targeted inference and vector/Keyword cold-loop checks then
passed 3 tests / 178 assertions (144 new-inference, 20 vector-cold and 14
Keyword-cold); the other thirteen passing selectors were not replayed merely
to replace the failed aggregate with a green one.

Independent component review accepted the three owners and closed the cold
regression. The actual source composition passed 1 test / 290 assertions,
constructing three fresh products: the accepted Gravity/QST pair and one real
bare-List/empty-List rejection lacking independent element evidence. The ten
accepted functions contain six new joins and two legacy equal/Never cases,
local caller flow and a separately checked return annotation. Accepted products
were reused for 42 guest executions, four public C7 verification gates,
independent whole-C8 graph checks, coherent hostile mutations and exact-budget
checks. The selected test took 370.244774041 seconds with a 2 GiB heap; this is
development timing, not a speedup or containment claim.

Independent actual-result review accepted the scoped executable behavior and
confirmed all eleven frozen inputs unchanged. The 290 assertions comprise
284 selector checks and six whole-loader checks. This does not relabel the
earlier failed aggregate. Match/try/loop/recur widening, other element types,
ownership and profile legality beyond this subset, other targets,
authoritative migration, self-hosting and release gates remain outstanding.

### Nil-bearing canonical-if joins: development implementation

L5 permits an independently established nil branch to join with a known atomic
type without changing either runtime value. The current C7, C8 and emitter
increment represents Bool, Character, String, Symbol and Keyword alternatives
as a closed nil-or type. It retains source-bound branch premises through local
inferred returns and calls. A non-nil return declaration cannot erase nil, and
nil-or-Bool does not satisfy L7's Bool-only guard rule. Ordinary `if` remains
truthy except for false and nil. Other control-flow joins are not widened.

Binary equality compares the original values. In particular, nil-or-Keyword
and nil-or-String can both contain nil; their differing non-nil alternatives
are not grounds for folding equality to false. Existing atomic equality and
List-join records retain their previous versions and contents. No runtime
instruction, annotation syntax, reader rule or seed primitive was added.
Independent review caught an unsupported compiler-seed `dissoc` call before
the first execution; premise construction now uses existing primitives.

The first focused batch passed 15 tests / 2,799 assertions: six new kernels
covering 806 assertions and nine affected regressions covering 1,993. It checks
closed type carriers, independent inference, source and binding mutations,
mixed List/nil row census, work limits, lowering and actual runtime equality.
The separate fixture-fidelity kernel passed 1 test / 10 assertions. The paired
positive source preserves W5's exact unannotated `w5-se-canonical-form [mode]`
helper; runtime cases retain all seven known modes plus an unknown mode. The
negative retains a real Keyword caller but declares a non-nil Keyword return
on the same body.

The first source composition attempt failed after its first accepted Gravity
product reached C7: 1 test / 7 passing assertions / 1 error. Public lowering
then rejected the existing whole-context work reservation before assembly;
the QST and negative products, runtime observations and C8 source checks did
not run. The 170.772-second failed attempt is retained. Its 17-function fixture
included eight redundant mode wrappers. The bounded follow-up simplifies those
test callers while preserving the exact W5 helper, inferred nil-bearing local
return, actual equality/error cases and all mode observations. It does not
increase the work limit or change production accounting.

The context reservation is distinct from JVM startup or scheduling overhead:
it charges whole-table cross-products before lowering. A bounded follow-up,
when a concrete larger-module integration requires it, should measure those
families and compare indexed validation with the current exact joins and
rejection behavior. Preserve source/provenance checks and cumulative work
limits; neither raising the ceiling nor redesigning verification-product
representation is part of this semantic increment.

The changed nine-function fixture passed the affected source composition:
1 test / 371 assertions, including six unchanged loader checks, in 386.096
seconds plus 8.815 seconds of namespace loading at a 2 GiB heap. It built the
accepted Gravity/QST pair and one representative negative Gravity product
once each. Accepted products were reused for 36 guest observations, four
tracked public C7 verification operations, whole-C8 reconstruction, exact and
one-below budgets, and coherent join/effect mutations. Upstream construction
also verifies its products; four is not a count of every C7 invocation.

The actual source has 25 calls, eight nil-bearing join rows and inferred
Keyword parameters for both unannotated helpers. W5's helper returns
nil-or-Keyword; the separate helper returns nil-or-String. Their local calls
retain these types through equality, including nil equals nil across those
two families. The declared-Keyword negative rejects with
`C7-TYPE-MISMATCH` / `function-result-type-mismatch`, actual nil-or-Keyword.
Bool, Character and Symbol alternatives have focused-kernel coverage here,
not additional source-composition coverage.
Throw/catch execution and inferred throw effects are retained, without a
claim that all caught effects escape. All 16 recorded inputs remained
unchanged. The earlier failed attempt is not relabeled as passing.

These development checks do not admit a module revision or establish complete
W5, numeric/general union support, other targets, self-hosting or release.

### Qualified core vars and availability: development implementation

This slice closes the C5 qualified-core frontier found before the List
count source run. L3 already permits fully qualified namespaces and STD2 makes
the permitted core surface available; no new language grant is required.
The owning `gravity.resolution` pass selects literal `gravity.core` vars
from supplied core records, preserving their binding identities and metadata.
Alias, source-definition and import route ownership remains prior to fallback;
an owned missing/private member must not silently borrow a core definition.
Type-position lookup remains separate and qualified macros/special forms are
not admitted by the new var route.

Inspection also found missing checks on supplied direct-core availability
metadata. The bounded correction validates the selected core
binding's profile/target fields after existing ambiguity and privacy checks,
with target exclusion before profile exclusion. Malformed fields reject;
only target `:all` is a wildcard. The shared check applies to existing core
vars, types and special forms without changing legal catalog identities.
It does not synthesize exports, grant capabilities or establish overload
purity. Focused exclusions and forged metadata accompany affected paired
source composition and a C5-only missing-member rejection. This work does not
establish general first-class builtins, unrestricted profile coverage or
authoritative migration.

The initial focused runs stopped before semantic assertions: an unclosed
candidate-selection form, then an unsupported bootstrap `merge` call. The
second correction uses existing `let`/`assoc` forms without extending the seed
or catalog. The corrected batch passed 3 tests / 400 assertions: 354 new
qualified-core/availability checks plus existing type-namespace and module
policy checks. It covers distinct reference IDs sharing the selected binding,
metadata preservation/forgeries, shadowing, owned-route misses, private and
duplicate precedence, malformed/excluded profile and target sets, and legal
type/special-form regressions. Resolution changes remain in the owning
resolver. A later position-guard refinement passed 363 assertions in the
owning kernel; the other 46 checks above are unchanged, not additional new
coverage.

The first paired source integration attempt is retained as failed: 1 test,
7 passing assertions, no assertion failures and one error. The first source
extension reached accepted C5/C6/C7 typing, then public plan assembly rejected
its typed-module definition census (`P15S23Q002`). Execution, the second
extension and the later rejection/forgery assertions did not run. This is not
an accepted source composition. Independent inspection found the inherited
lowering join compared canonical `List` directly with the authenticated source
spelling `gravity.core/List`. The owning emitter now accepts either the
canonical symbol or the exact qualified symbol while retaining binding kind,
namespace, canonical type, declaration and source-syntax checks. It does not
relax the module census or rewrite the fixture. Focused ordinary/qualified
String and List declarations, forged spellings/bindings, raw-source joins and
homogeneous List regressions passed 3 tests / 228 assertions (132 new checks).
The changed candidate passed its distinct source composition: 1 test / 119
assertions, including six loader assertions. Both fresh source extensions
retain one selected count binding and three distinct references, preserve
legal metadata, pass public C5 verification and C6/C7 typing, complete public
lowering and independently checked pure C8 effects, and execute ten guest
cases. A changed resolved binding is rejected by public C5 verification; the
missing-member source stops at C5 with `C5-UNRESOLVED`. Pair identities agree.
The selected composition took about 228 seconds with a 2 GiB heap; this is
development timing, not a speedup claim. The original failed attempt remains
failed. No unchanged expensive replay or migration credit follows.
Final fixture formatting removed only one redundant terminal newline byte in
each paired file; the separate 7-assertion parse/body check passed afterward.
The 119-assertion result remains attached to its original bytes, not silently
relabeled as a run on the formatting-only revision.

The prepared independent homogeneous List join for canonical `if` follows this
existing-contract gap. Its separate L5 clarification and join-specific
evidence remain unimplemented; AH is not reopened to add that behavior.

### Persistent List cardinality: development implementation

This slice extends existing unary `count` to established bare, positional
and homogeneous persistent Lists. STD3 specifies a non-negative integer result
equal to the number of stored elements, including zero, with ordinary operand
evaluation and inert stored payloads. This does not require a member-type,
nonempty or static-length proof. Never still aborts invocation; the query adds
no direct collection-construction effect while operand effects remain.

The same C7 count owner, independent C8 reconstruction, typed emitter and
runtime own the change. Existing Map/Vector behavior must survive. Malformed
argument containers and arity are checked before type collection; source,
binding, row and cumulative-work checks remain. Nil, Strings, Sets, arbitrary
sequences, first-class count and unknown operands are not admitted by this
slice. The first focused batch reached 251 passing assertions before a
sentinel exposed an inherited unknown-type set-membership check hashing the
entire positional type map. The three count validators now use shallow
nil/unknown comparisons instead; member metadata is not visited just to count.
A subsequent batch stopped at an extra closing delimiter introduced in that
edit. Both failed batches remain recorded. The corrected focused batch passed
5 tests / 448 assertions, including 137 new C7 List checks and existing
Map/Vector, C8, lowering and runtime checks. These overlap and are component
feedback, not source-composition coverage. The following consumer batch passed
8 tests / 748 assertions, including the new C7 checks, 105 independent C8
checks, 155 typed-lowering checks, 40 List-runtime assertions and existing
Map/Vector regressions. Exact/one-less work, malformed inputs, changed source,
forged descriptors/rows, operand effects/transfers, inert stored payloads and
unsupported receiver families are covered. Private opaque and large-metadata
controls do not expand upstream source/type bounds or host-object support.
Independent component review accepted this scope; migration support is not
claimed.

Pre-source review caught an existing resolution frontier in the proposed
fixture: C5's `gravity.core` qualification exception applies to type references,
not ordinary operators. The paired cardinality fixture uses unqualified
`count`; normalized qualified-header controls are conditional consumer checks,
not evidence that `gravity.core/count` resolves from source. Qualified core
operator resolution remains separately tracked language work. No C5 expansion
or expensive source run is hidden in this correction.

The stable combined focused batch passed 93 tests / 7,395 assertions across
the five existing namespaces. The same batch had already been launched again
after the paired spelling correction when the narrower fixture-only guidance
arrived; that second result also passed 93 / 7,395. They overlap completely
and are not additive coverage. No further regression rerun was needed for
that fixture-only edit.

The one affected source composition passed 1 test / 280 assertions in
318.138 selected-test seconds with a 2 GiB heap. Both accepted extensions have
seven functions, ten calls and five count facts: four normally invoked List
queries and one Never operand that aborts invocation. The 24 guest cases
exercise empty/nonempty bare Lists, homogeneous tails, AG's explicitly typed
List-returning producer, persistent inputs and original thrown-value identity.
The query adds no allocation effect; producer allocation and operand throws
survive independently reconstructed C8 graphs. Four fresh public C7 operations,
typed/effect forgeries, a resealed allocation-graph forgery, exact/one-less C8
work and the real String rejection were checked. Exactly three full fresh
source products were constructed; no actual source failure or retry occurred.
All eleven frozen code/test/fixture inputs and STD3 were unchanged through
the run. Negative QST coverage is pairing only; large runtime inputs and
private sentinel controls do not enlarge source bounds or admit host objects.

This is executable development support for persistent List cardinality. Other
collection families, general first-class builtins, qualified core-operator
source resolution, profile/provider/ownership coverage, other targets,
authoritative migration, self-hosting and seedless release retain their
separate gates. These timings are scheduling observations, not benchmarks or
speedup evidence.

### Homogeneous List returns: development implementation

This executable slice extends L5's explicit `(List T)` declaration to
function returns. The body type must be established independently; matching
homogeneous or finite member types, including the empty List, can satisfy the
declared return while retaining the actual body type and return-specific
compatibility evidence. An expected declaration does not supply missing
member facts, widen incompatible branches or authorize bare-List erasure.

Implementation is in the owning declaration, type, effect and
typed-lowering passes and the existing C5/C6 declaration-reference bridge.
The declaration/header batch passed 5 tests / 229 assertions, covering named
and unnamed functions, qualified type references, return-only and combined
annotations, source scopes/identities, malformed headers and forged mappings.
Its first run had 221 passing and eight failing assertions: the new test
matched form IDs against reference syntax IDs. Correcting that oracle retained
all four cases and production validation; the failed run remains separate.
The next C6/C7 batch passed 4 tests / 816 assertions, including 154 owning C6
producer/source-map checks and 536 new return checks across the fifteen
supported atomic member types. Empty, nonempty and 1,024-member bodies,
return-specific evidence, source/binding mutations and cumulative exact/one-less
work across functions and inference rounds are covered. A preceding batch
stopped after the 154 C6 assertions at a delimiter error in the new C7 updater;
that error and the later pass remain separate, overlapping results.
The consumer-context batch passed 5 tests / 1,150 assertions, including 163
checks in each independent C8 and lowering context. Twelve return functions
exercise source-record ordering despite a different function-map order;
missing, duplicated, reordered and foreign rows, changed premises and exact
work boundaries are rejected. Counts overlap the earlier C7 batch and existing
parameter/prepend regressions. The definition-join batch then passed 5 tests /
942 assertions, including complete normalized C8 function joins and typed
definition joins with altered source, declaration and proof metadata rejected.
A separate 5-test / 128-assertion branch batch checks equal finite List body
types, differing and unknown branch types, and existing Boolean, structural
match and root-map guard behavior. Declared homogeneous returns do not drive
branch inference; ordinary atomic return checks remain in force. These counts
overlap earlier contexts and are not whole-module source evidence. Actual
source composition was then exercised once. The stable combined focused batch passed
84 tests / 6,619 assertions across five existing namespaces, including the
declaration bridges, all new return kernels and affected atomic, branch,
collection, effect and runtime regressions. This is one overlapping combined
development result, not additional independent proof coverage.

The affected source run completed 377 assertions: 375 passed, two failed and
none errored, in 472.421 selected-test seconds with a 2 GiB heap. Both failures
were the same test-oracle error, once per accepted extension: the C5 uniqueness
assertion read C6's `:reference-syntax-id` field instead of C5's `:syntax-id`,
reporting 65 rows / one nil identity / 65 C6 rows / 65 distinct C6 identities.
The original aggregate remains failed. Its other checks reached both accepted
Gravity/QST programs, 28 runtime cases, four fresh public C7 operations,
independent C8 reconstruction and hostile rejection, the wrong-member C7 and
missing-allocation C8 cases, and a C5-only unresolved-type rejection. Four full
fresh source constructions and the C5-only case were performed; none is
automatically repeated to repair this assertion. The source/production inputs
were unchanged through the run. The test-only accessor correction and explicit
retained C5 schema/uniqueness/forged-ID checks passed a separate focused batch
of 3 tests / 128 assertions. The independent auditor accepted the reached
semantic/integration evidence with this qualification; no corrected 377/377
source pass or direct rerun of the repaired source uniqueness assertion is
claimed.

Empty and finite all-matching Lists, exact homogeneous Lists and existing
non-returning bottom behavior now support explicit homogeneous returns. The
six-function accepted program preserves actual body types, outward declared
types and return-specific premises through C7, C8 and executable lowering;
its composed main returns `"ba"`. Bare/unknown/wrong-member bodies, unresolved
type names and missing allocation allowance remain rejected. Differing finite
branch joins, nested generic declarations and general collection coercions
remain unsupported boundaries, not new language prohibitions. Authoritative
migration, profile/ownership/provider coverage, other targets, self-hosting
and seedless release still require their separate evidence gates.

### Homogeneous List traversal: development implementation

The L5 rule for `(List T)` parameters is implemented in the owning C6 signature
path, C7 types, C8 reconstruction and typed emitter. Initial parameter projection/header and C6 source
mapping checks passed 4 tests / 185 assertions, including qualified type
references, retained compound declarations, forged source identities and
unchanged return-annotation fences. An earlier run had 178 passing assertions
and one test error: it looked up a transient helper deliberately unmapped after
installation. The corrected test uses the retained projection function; no
production helper export or reload was introduced. The two batches overlap.

Homogeneous parameter/type checks passed 4 tests / 434 assertions, and initial
C8/emitter consumer checks passed 6 / 554. The new member scans reject malformed
scalar and nonvector type records before scanning; accepted validation, vector
shape and the reserved work budget dominate those scans. These batches include
existing bare-List and query regressions and are not additive totals.

C7's guarded-head batch passed 3 tests / 418 assertions, including 52 new head
checks. It reconstructs empty/nonempty knowledge from an authentic unary
`empty?` condition on the same immutable binding, under the nearest `if`.
Unrelated nearer conditions, other bindings, tails, Never conditions and erased
or forged proof fields cannot supply that knowledge. A dominant predicate-shape
check prevents scanning arbitrary condition arguments before rejection. Two
earlier batches failed because normalized test remapping also changed call and
operator discriminators; their failures are retained, not semantic passes.

The stable development batch passed 66 tests / 4,706 assertions, including
independent C8 guarded-head reconstruction, typed ordinary-call lowering,
emitter failure-work accounting and existing collection/runtime regressions.
Earlier component batches overlap this total. The initial 67-test batch had
one error in an added legacy macro-route selector: it reached the historical
emitter admission facade and rejected the changed semantic hash. That failure
is retained; the pins were not refreshed. The final development selection
excludes that migration-boundary route, not a failing semantic assertion.

The first actual source composition stopped before the first positive program
reached C7: six assertions passed and one test error reported C6's
`sh06-reference-projection-mismatch` for the `String` type reference. The
temporary C6 reference traversal had not mirrored the C5 compound parameter
projection. This is a transport defect to correct with a cheap parity
regression, not permission to relax the bijection check. The failed run is not
typed traversal or runtime evidence.

The corrected bridge passed three focused tests / 145 assertions, including
retained C5/C6 projection parity and unchanged atomic/return transport. The
subsequent changed-candidate composition passed one test / 257 assertions.
It built the paired seven-function source modules, retained three explicit
finite-to-homogeneous parameter premises and two independently reconstructed
nonempty head proofs, then executed 28 guest calls. Empty, singleton and
two-element Lists produced `""`, `"alpha"` and `"alphabeta"`; original Lists,
stored head objects and thrown objects were preserved. Six operation-local C7
verification calls include four coherent hostile inputs. C8 checked both full
graphs, rejected two graph mutations and exercised the owning analysis at
ample, exact and one-less work budgets. Nine C7 semantic negative families,
the C6 compound-return fence and the C8 missing-allocation rejection passed.
Negative `.qst` files have parity coverage, not additional source executions.

The selected composition took 1,211.036 seconds plus 8.853 seconds of loading
at 2 GiB; this is not a phase-cost or speedup measurement. Earlier focused
counts overlap and must not be added to this integration total. The first
failed composition remains recorded separately.

Actual source member coverage is String; type kernels cover the bounded
non-Nil atomic declaration set. The nearest-if subset is not a general path
solver. Nested type applications, homogeneous-to-bare adaptation, compound
return annotations, arbitrary collection operations and complete profile or
ownership legality remain outside this increment. Authoritative migration,
self-hosting and release status are unchanged.

### Typed shrinking-List traversal: development implementation

L5 now states the explicit bare `List` parameter boundary. The owning Gravity
signature path in C6 recognizes List declarations; C7, C8 and the typed emitter
keep a finite argument's actual type and reconstruct a distinct, versioned
parameter-compatibility premise. Unequal types are not labeled equal. An
unannotated parameter, unknown argument, unrelated sequence family, or erased
premise cannot silently obtain this permission. Schema, ownership, linearity,
region and profile obligations remain intact. Catch/error type maps and global
normal compatibility are unchanged.

The existing List-query family now includes unary `empty?`: established Lists
produce Bool, Never operands preserve their transfer. Query effects remain
separate from operand allocation/throws. Inside an annotated function, `rest`
preserves bare List and function-target recur keeps exact slot checks. Exact
bare-List result annotations are supported separately from finite-to-bare
result adaptation, which is not provided by the parameter rule.

The stable focused/regression batch passed 53 tests / 4,154 assertions. It
covers signature lowering, actual-type preservation, compatibility row and
binding/source mutations, erased-premise rejection, bounded work, independent
C8 reconstruction, typed ordinary-call lowering, runtime empty/nonempty Lists,
original transfers, and manual runtime traversal producing empty/singleton/
two-element markers. Earlier 385-, 594- and 292-assertion batches overlap this
batch and are not additive. The first C7 test compile failure and the second
batch's incorrect expectation of immediate rejection for an intermediate
Unknown are retained; the corrected test requires no compatibility premise for
Unknown and leaves final unresolved-source rejection to the composed check.

The paired source composition passed once: 1 test / 223 assertions. It compiles
and executes an annotated tail function and shrinking traversal, preserving
actual finite argument types through three compatibility rows and exact List
calls and recur slots. The pair covers 28 guest executions, two original thrown
payload identities, six selected tail-storage identities, six fresh public C7
verification gates, two accepted whole C8 graphs and two coherent graph
hostiles. Five real C7 rejected sources and one C8 allocation rejection preserve
the unsupported boundaries. The owning C8 analysis also passes at its observed
exact work budget and rejects one unit below it for the first fixture only.
The negative `.qst` files are parity fixtures, not additional executions.

Selected source-test execution took about 763 seconds; namespace loading took
about 8.3 seconds in the same 2 GiB JVM. These are non-authoritative development
timings, not a benchmark or evidence that scheduling dominates product
construction. The prior focused counts overlap and must not be added to the
source result. Selected persistent-List tail sharing is not a general ownership
or no-allocation guarantee; coherent whole-product mutations do not isolate a
deep source-causality check. The unknown-call rejection permits either of the
two documented unresolved-type diagnostics.

Homogeneous element annotations, guarded head typing, general generics,
imported/higher-order adaptation, full profile and provider legality,
authoritative migration, self-hosting and seed retirement remain outstanding.
Direct commands are in `docs/development-verification.md`.

### Persistent List head and suffix queries: development implementation

STD3 now defines empty and nonempty persistent List queries explicitly.
Owning Gravity C7, C8, typed-emitter and runtime changes implement unary
`first` and `rest`. Finite positional types establish the head (or Nil when
empty) and ordered suffix. Bare List knowledge is sufficient for `rest` to
return bare List; `first` still requires head-type evidence. No constructor or
quoted-value ancestry substitutes for typing, and no Dynamic type is invented.

The operand evaluates once; a transfer skips invocation without losing its
effects, output or original payload. Queries neither mutate the original List
nor execute stored payloads. The selected development PersistentList runtime
returns the stored head or existing immutable tail, possibly retaining input
storage. It adds no direct query allocation in this representation; operand
allocation and error effects remain. Generic sequence, ownership, profile and
backend guarantees do not follow.

The stable focused/regression batch passed 44 tests / 3,739 assertions. It
includes real empty/singleton/heterogeneous List results, tail identity, inert
payloads, false/nil, single evaluation, transfers, malformed runtime rejection,
bounded type work, source joins, effect retention and existing recur paths.
Earlier C7 test compilation and unsupported two-argument `subvec` calls failed;
the corrected Gravity owners use the seed's existing three-argument form.
The consumer test's earlier compile failure and ineffective Bool-to-Bool
mutation failure are retained as failures, not aggregate passes. Earlier
281- and 384-assertion checks overlap the final batch and are not additive.

The single new paired current-source composition passed 1 test / 331 assertions
with zero failures or errors. Each complete assembly contains eight functions
and sixteen calls: ten List queries (eight invoked, two skipped by transfers),
four List constructions and two ordinary local calls. Concrete caller/callee
joins cover payload/form and main/payload without a fabricated dispatch tag.
Twenty guest executions cover computed form payload extraction, empty and
singleton suffixes, inert nested data, false/nil, NUL/Unicode Strings and four
original transfer-payload identity checks.

Six fresh public C7 gates cover two whole assemblies and four coherent binding,
type, order and storage-policy hostiles. Two guarded, nonempty whole C8 results
pass verification; two resealed graph hostiles reject. Four actual `.gravity`
negatives cover arity, wrong collection family, unknown type and wrong return
annotation. A fifth passes C7 and its verifier before C8 rejects missing operand
allocation allowance with `C8-UNDECLARED` /
`function-effect-outside-source-allowance`. Negative `.qst` siblings are parity
checks only; the unknown-type oracle allows two diagnostic pairs, and whole-
result hostile substitutions do not isolate deep source causality.

All nineteen source/test/fixture hashes and STD3 stayed unchanged through the
sole run. Selected execution measured 663.008426708 seconds and namespace
loading 8.460406500 seconds with a 2 GiB heap. These are development scheduling
observations, not phase-cost or speedup evidence. No unchanged constructor,
String or Map source composition was replayed.

Bare-List `rest` is normalized type-boundary coverage, not a new annotated
source case. Bare-List head inspection, annotated List boundaries, shrinking-
List traversal, generic sequences, full profile/provider legality,
authoritative migration, self-hosting and seed retirement remain outstanding.
Direct commands are in `docs/development-verification.md`.

### Evaluated persistent List construction: development implementation

STD3's evaluated `list` constructor now has owning Gravity C7, C8, typed-emitter
and runtime paths. It accepts zero through 1,023 established, lowerable
first-order operands; the existing 1,024-child call limit includes the operator.
Known bare collection types do not require invented member facts. Supplied
positional type records receive bounded validation, with visited work retained
on rejection. The result uses the existing ordered List element-type carrier,
but records evaluated-construction ancestry rather than quoted-data authority.

Operands evaluate once, left-to-right. A transfer prevents construction and
later operand execution without excusing unestablished source types. The
current conservative constructor policy retains latent allocation and records
direct allocation only for normal invocation, including empty Lists. Placement,
provider, ownership and actual heap behavior are not established by that policy.
Returned Lists preserve nesting, duplicates, false, nil and inert payloads.

The first consumer run exposed a genuine StackOverflowError at the supported
1,023-operand runtime boundary: the existing argument collector recursively
called itself. Lowering passed 105 assertions; runtime reached 25 passes before
the error, and fail-fast skipped C8. That run is not an aggregate pass. An
initial List-only collector workaround passed a separate 268 assertions, but
was superseded by the smaller shared-helper loop correction. The final helper
keeps its signature, environment, function/path/target context, ordered output,
accumulated values and first non-normal control tuple; no helper, export,
arity reduction or host-stack increase was introduced.

Current shared-helper code passed 37 focused/regression tests / 2,935 assertions.
Two separate function-recur and loop runtime checks passed 127 assertions.
These executions are distinct; earlier focused counts overlap and are not
additive. C8's malformed scalar-forms guard was corrected and checked. Before
the source run, review also corrected an ordinary local-call dispatch oracle
and added concrete caller/callee joins. Only the cold selector changed after
the focused batch; a separate fixture/namespace smoke check passed ten
assertions without replaying the production kernels.

The single new paired source composition passed 1 test / 265 assertions with
no failures or errors. Each complete assembly contains eight functions and nine
calls: seven List constructions (five normally invoked, two not invoked), one
String concatenation and one ordinary local call. The concrete main/form
caller/callee joins and inferred List returns pass without new annotation
syntax or supplied result types. Twenty-four guest executions include actual
List-family checks, computed inert form DATA, nested collections, NUL/Unicode
Strings and four original transfer-payload identity checks.

Six fresh public C7 checks cover the two whole assemblies and four coherent
binding, type, order and allocation mutations. Two guarded, nonempty whole C8
results pass verification; two resealed graph hostiles reject. Three actual
rejected `.gravity` sources cover unknown operands, unknown operands after a
transfer and a mismatched return annotation. A fourth source passes C7 and
its verifier before rejecting in C8 with `C8-UNDECLARED` /
`function-effect-outside-source-allowance`. Negative `.qst` siblings receive
parity checks only, and the unknown-type tests permit two stated diagnostic
pairs rather than establishing one unique first diagnostic.

All seventeen source/test/fixture hashes and the owning STD3 document stayed
unchanged through the sole run. It measured 551.366308334 seconds in the
selected test and 8.216410500 seconds in namespace loading with a 2 GiB heap.
This is whole-composition development timing, not a phase-cost or speedup
comparison. Independent pre-source review accepted the stable candidate; no
unchanged String/Map/equality source proof was replayed. Direct commands and
supported boundaries are in `docs/development-verification.md`.

List queries have a separate development implementation above. Arbitrary
recursive traversal, generic sequences, List annotation grammar, imported List signatures, syntax-
object authority, full profile/provider/backend legality, self-hosting and seed
retirement remain separate frontiers. This development result does not admit a
stable module revision or confer authoritative migration credit.

### Binary String concatenation: development implementation

STD4's binary String overload of the compatibility spelling `str` now has
owning Gravity C7, C8 and typed-emitter paths. Both normally returning operands
must be established Strings. The result preserves their exact scalar sequences
without normalization, coercion or general formatting. Operands evaluate once,
left-to-right; a transfer aborts invocation but does not erase operand effects.

The applied overload retains latent `:memory/allocate` even when it is not
invoked. Direct allocation is conditional on normal invocation, including for
empty Strings. C8 independently derives this distinction and enforces source
allowances. The authenticated generic C5 binding's empty effect row remains
unchanged; it is not a claim that the applied overload is pure. Runtime
production code remains unchanged.

The final focused batch passed 29 tests / 2,203 assertions across the new
typing, effects, lowering, existing-runtime and fixture kernels and affected
Map lookup/membership, not, association, append, constructor-allocation,
Keyword-lookup and throw regressions. These counts overlap earlier focused
results and are not additive. Two earlier cheap namespace-load failures were
test-parenthesis defects, not semantic passes; their failed attempts remain
historical evidence.

The single affected source composition passed 1 test / 269 assertions, with no
failures or errors. The two complete paired assemblies each contain eight
functions and eleven calls: ten concatenations (seven normally invoked, three
not invoked) and one explicit-default Map lookup. Twenty-four guest executions
and six original String payload-identity checks cover nested calls, empty/NUL
and supplementary Unicode Strings, composed/decomposed distinctions, parameter
shadowing and transfer behavior. The eager `get` default retains its normal
concatenation allocation even on a Map hit.

Six fresh public C7 checks cover two whole assemblies and four coherent
binding, operand-type, order and latent/direct-allocation mutations. Two ready,
nonempty C8 results pass whole-result verification, and two resealed graph
mutations reject allocation/dependency erasure. Four actual rejected `.gravity`
fixtures cover arity, numeric/unknown operands and a result annotation. A fifth
source is accepted by C7 and its verifier, then rejects in C8 with
`C8-UNDECLARED` / `function-effect-outside-source-allowance`. Negative `.qst`
siblings receive parity checks only; the unknown-type case permits two stated
diagnostic pairs rather than claiming one unique first diagnostic.

All eighteen source/test/fixture hashes, the owning STD4 document and unchanged
runtime remained stable through the sole run. It measured 666.852492834 seconds
in the selected test and 8.240701291 seconds in namespace loading with a 2 GiB
heap. This is whole-composition development timing, not a phase-cost or speedup
comparison. Independent component and pre-source review accepted the stable
candidate. No authoritative module migration, target support, self-hosting or
seed-retirement claim follows. The unchanged Map and equality compositions
were not replayed.

Variadic and first-class invocation, other String/formatting overloads, numeric
or Character conversion, implemented library exports and complete profile,
provider and backend legality remain separate frontiers. Direct commands and
the affected composition selector are in `docs/development-verification.md`.

### Map lookup with an explicit default: development implementation

STD3 now states the three-argument Map lookup rule explicitly: evaluate Map,
key and default eagerly in that order, even on a hit; return the associated
value when present, including false or nil, and otherwise the default. A
transfer aborts invocation. The existing C7, C8, emitter and Gravity runtime
implement a finite typed route with dynamic String or Keyword keys.

For normal invocation, established bounded Map member types must give every
potentially matching key-family value the same established type as the default.
Empty Maps use the default type, and unrelated known scalar key families may
have different values. No constructor or static-presence proof is required.
A Never operand makes the call non-invoked/Never without a fictional normal
result join; known bare Maps are then allowed. Other returning operands remain
domain-checked, and supplied structured-type evidence retains its integrity
checks. No selected deep-shape authority follows from the result type.

The latest production and test bytes passed 26 focused/regression tests with
2,058 assertions, including the tightened type guard and preceding membership,
lookup, not, count, association, quote and throw paths. Before source execution,
a fixture-only correction avoided the existing Keyword-only evaluated-Map
literal frontier: the Map operand now allocates a supported Keyword Map before
returning quoted mixed-key Map data. Its final reader/parity check passed ten
assertions. Compiler and test bytes did not change; the earlier proposed pair
was never executed in the source composition, not a failed actual attempt.

The sole corrected paired composition passed all 214 assertions in 601.443
seconds after 8.167 seconds of namespace loading at 2 GiB. Both complete
ten-function assemblies retained ten calls: eight get applications (five
normal, three Never), one membership query and one ordinary local call.
Twenty-six guest executions and six original String payload identity checks
cover local Map returns and aliases, original-value preservation, dynamic keys,
empty Maps, mixed unrelated key families, false/nil presence and eagerly
throwing defaults on present keys.

Five public lowering operations each verified C7 freshly: two whole assemblies
and three coherent binding/type/order mutations. Two accepted, nonempty C8
results passed whole verification, and two resealed graph mutations rejected.
Real allocations in Map, key and default operands remain transitive effects;
get adds no allocation or effect. Four actual `.gravity` rejections cover arity,
an incompatible normal value/default join, Symbol keys and result annotation.
Negative `.qst` evidence is parity only; the broader malformed/unknown/Never
matrix is focused evidence rather than a claim of authentic source coverage
for every case. All seventeen source/test/fixture files, STD3 and the Git base
remained unchanged through execution. No retry or unchanged old proof occurred.

The smaller schedule is scoped integration coverage, not a compiler speedup
measurement. Two-argument lookup, general stored-key families, heterogeneous
normal results, first-class invocation, complete profile/provider or target
support, authoritative migration, self-hosting and seedless release remain
outstanding. See `docs/development-verification.md` for direct commands and the
separate boundaries; no admission or release status changes follow here.

### Map key membership: development implementation

The existing Gravity C7, C8, typed-emitter and runtime owners implement direct
Map `contains?` with dynamic String or Keyword keys under STD3/STD2. An
independently established Map type suffices; no literal constructor, static
presence fact or deep value-shape proof is required. Presence is independent
of the stored value: false and nil entries remain present. Key comparison is
by value, without coercion or Unicode normalization, and stored values remain
uninspected.

Map and key evaluate once, left-to-right, in non-tail positions. Never transfers
before lookup. The query adds no allocation, effect, error or capability;
operand effects remain visible. Runtime uses the existing closed binary
builtin-call representation and checks the Map and key domain before lookup.
No seed primitive, helper, public export, instruction family or test lane was
added. The runtime still has twenty-seven private functions and four exports.

Four focused kernels passed 344 assertions. The final affected regression
batch passed 22 tests/2,129 assertions, covering membership alongside existing
not, association, count, equality, quote, guard and throw behavior. Fixture
parity/header checks passed 24 assertions. These counts overlap and are not
an additive total or authenticated source evidence by themselves.

The sole paired source composition passed all 366 assertions in 1,198.132
seconds after 7.961 seconds of namespace loading at 2 GiB. Each extension
retained ten functions, thirteen calls and eight membership applications
(six normal, two Never), plus one not and four ordinary local calls with
canonical caller/callee/order joins. Forty-eight guest executions cover a
quoted registry returned by a local function, aliases preserving the original
Map, present false/nil and absent keys, dynamic equal String objects, Unicode,
NUL, case and normalization distinctions, Keyword namespaces, empty and
twelve-key Maps, explicit not composition and original String throw payloads.

Nine fresh public C7 lowering operations include five coherent altered
type/fact/constraint cases. Two accepted, nonempty C8 graphs pass whole-result
verification, and four resealed graph mutations reject. Actual evaluated Map
operands retain allocation, and both throwing-operand positions retain their
error effects without adding query allocation. Eleven negative `.gravity`
families reject arity, operand/key domain, unresolved types and result
annotations, including an invalid other operand when one operand is Never.
Negative `.qst` coverage is parity only; unresolved-type oracles permit two
existing diagnostic paths rather than claiming a uniquely observed reason.
All 31 compiler/test/fixture inputs and the Git base remained unchanged through
the run. No unchanged earlier expensive source proof was replayed.

Use `docs/development-verification.md` for the direct focused and affected
composition commands. Other key domains, vector/set membership, presence-based
type narrowing, first-class invocation, complete collections/profile support,
targets, authoritative migration, self-hosting and seedless release remain
outstanding. This is scoped executable development support, not admission of
a stable module revision or a performance claim.

### Unary truthiness complement: development implementation

The existing C7, C8 and typed-emitter owners implement direct unary `not`
under STD2/L2. False and nil yield true; other independently established,
normally returning values yield false. This does not inherit atomic equality's
six-family restriction. Existing unsupported operand source or lowering forms
remain separate frontiers, not new truthiness prohibitions.

The operand evaluates once in non-tail position. Lowering emits the existing
conditional with literal false/true branches, without a runtime, seed, public
API or instruction change. Never transfers before either branch. The operand's
allocation and error effects remain visible; `not` adds none. Bool-only L7
guards are unchanged: `(not (= tag :rejected))` supplies a Bool explicitly,
while a raw Keyword guard still rejects.

The producer kernel passed 165 assertions and the effect/lowering/existing-if
batch passed three tests/245 assertions. The combined affected batch passed
19 tests/1,539 assertions, including prior equality, Character, quoted-DATA,
guard and throw regressions. These overlap, rather than form an additive total.
Pre-run review corrected an authored oracle: ordinary local-call facts have no
`:dispatch`; the higher-order-call tag was inappropriate. Six explicit
canonical caller/callee/order/type joins now retain the local-call evidence.
The corrected namespace and unchanged lowering/runtime kernels passed two
tests/136 assertions before the sole source run.

The paired source composition passed all 464 assertions in 674.647 seconds
after 8.132 seconds of loading at 2 GiB. Each extension passed eight function
types, eighteen call facts and sixteen `not` applications (fifteen normal,
one Never), plus one atomic equality and one ordinary local call. Twenty guest
executions cover three map-guard outcomes, false/nil/zero/empty text and quoted
collections, evaluated vector/map operands, BMP and supplementary Characters,
an ordinary marker-shaped Map, retained String throw payloads and parameter
shadowing. Runtime code remained unchanged.

Nine fresh public C7 lowering operations include five altered type/fact/row
cases. Two accepted, nonempty C8 graphs pass whole-result verification; four
resealed graph mutations reject. A genuine constructed-vector operand retains
transitive allocation while `not` has no own allocation, and the throwing
operand retains its error effect. Five negative `.gravity` cases reject wrong
arity, unknown type, result annotation or raw non-Bool guards. The separate
non-tail recur case rejects with the exact C6 core-lowering diagnostic.
Negative `.qst` coverage is paired-source parity only; the unknown-type oracle
permits two existing diagnostic paths rather than claiming a unique observed
reason. All 20 compiler/test/fixture inputs and the Git base remained unchanged
throughout the source run. No expensive prior equality proof was replayed.

Use `docs/development-verification.md` for the focused and source-composition
commands. First-class `not`, other predicates, complete profile/provider and
standard-library support, target support, authoritative migration, self-hosting
and seedless release remain outstanding. This is scoped executable development
support, not admission of a stable module revision.

### Artifact-backed ordinary module imports: development increment

Under L3, C5 and C16, `gravity/resolution.gravity` now owns ordinary import
selection from explicit, verified callee artifacts. The normal C5 source/file
frontdoors use SH-06 without a metadata-selected stage0 fallback. Their
module-input argument is documented in `docs/development-verification.md`;
the default empty set is sufficient only when there are no external Gravity
requirements. Even an unused `:requires` needs its actual module artifact.
Namespace spelling, `:refer`, header fingerprints and metadata candidates do
not create an exported definition. Explicit foreign imports remain a separate
interop boundary.

Aliases, referred names and fully qualified names join actual local callee
definitions and exports. Private and missing names retain C5 diagnostics.
Repeated imports of one origin do not create false ambiguity; distinct module
origins remain distinct candidates. Target, capability and supported profile
compatibility come from the actual callee, not require-option overrides.
Verified transitive edges participate in cycle rejection. The ordinary input
rejects historical stage0 and strict SOURCE artifacts rather than stripping
their boundary or treating a stored status as verification.

Imported bindings retain actual callee artifact, analysis, binding and
definition-syntax origins separately from caller-side projected IDs. Raw callee
source spans are not nested into the semantic origin projection. Ordinary
artifacts retain their actual module inputs for fresh request reconstruction,
serialization and verification. Conservative invalidation includes callee
artifact IDs even when no name is used. No accepted-proof cache, resource-cap
increase, production fragment, load-order row or authority-pin refresh was
introduced. Clojure transports and verifies the inputs; Gravity selects and
checks the imports.

The existing strict SOURCE request remains v1, separate from the ordinary v2
request. Four B51 linker derivation sites and their forward declaration now
name the explicit base-request helper. A focused test checks actual base and
ordinary shapes, contextual delegation and the structural call sites. This is
adapter-routing evidence, not SOURCE construction, native evidence or admission.

The real `sh-06/modules/{values,consumer}.{gravity,qst}` fixtures define and
export `identity-i64`, preserve a private sibling, and lower an actual aliased
call to C6. The original paired run recorded 23 passing assertions and two
failures: it incorrectly expected a definition-artifact ID directly on C6
reference attributes, once per extension. Both extensions accepted C5/C6;
their artifact identities and explicit C7 unsupported-call checks passed.
The whole run remains failed. The corrected oracle follows the projected
binding into the authenticated C6 request table instead of changing C6's schema.

A separate real origin/negative/replay test passed 29 assertions: the projected
binding/resolution-use join, unused missing requirement through the default
frontdoor, private and missing names, forged export/binding/upstream artifacts,
serialization with fresh replay, and changed callee exports invalidating the
consumer. The unchanged expensive C7 construction was not repeated solely to
refresh the aggregate receipt. Paired test source remains co-canonical.

The initial focused policy/seed/type/API group passed four tests and 52
assertions. The later five-test, 377-assertion group adds genuine derived v2
cycles, distinct-origin ambiguity, strict routing and the normalized v1
rejection matrices. These groups overlap and are not summed as unique coverage.
Historical metadata-driven matrices are explicitly DATA policy tests, not
ordinary module artifacts or additional real module proofs. The direct module
kernel took about 0.26 seconds in the latter run; the selected group took
606.077 seconds after 6.946 seconds of namespace loading. These are development
timings, not benchmark or dominance claims. Full upstream-product construction
remains separate from scheduling and from the default kernel edit loop.

The initial affected B12 batch passed its eight actual-target assertions, but
the call-order fixture failed before assertions: it required `shared.core/combine`
while the new real library exported only `checksum`. C5 correctly reported a
missing module export. The paired library fixtures now define and export the
real three-argument `combine`; production policy was not weakened. The original
batch remains failed. The corrected B12 batch passed two tests and 12 assertions,
covering actual module targets in both extensions and operator-before-argument
order through C6. The affected module/foreign/compiler composition passed one
test and 10 assertions; the strict nil/context/private API checks passed two
tests and 12 assertions. The language boundary passed six tests and 11
assertions. IO and foreign declarations were resolved, not executed. Existing
optional B12 catalog selectors and dependency paths were updated for the
renamed tests, owning resolution source and actual shared-core fixture pair;
no new route, lane or authority record was needed.

Imported C7 signatures, C8 imported effects, runtime module
linking, module initialization, cross-profile facades, additional target
variants and package solving remain later work. This increment does not admit
an authoritative module revision or establish full SH-06/SH-07, self-hosting,
seed-retirement, target or release completion.

### Imported direct-call types: development implementation

The current L3/L5/C7 increment derives imported function types from actual
authenticated callee C7 requests. The owning pass is
`gravity/compiler/c7_type_checker_engine.gravity`; the request interface and
direct test commands are in `docs/development-verification.md`. An exported
callee is joined through the actual C5 module and original binding to its
projected C6 function, not selected by spelling or a copied caller signature.

The initial increment required explicit complete callee parameter/return
annotations; inferred exported signatures were unsupported by that increment,
not forbidden by L5. The producer-inference increment below extends this
boundary. Imported parameter and return types are immutable premises.
Argument/result types and arity are checked without specializing a producer
from caller input.
Operator references also receive their function type. Function signatures and
producer identities are retained in shared, deterministically ordered tables;
call facts and equality constraints refer to stable coordinates rather than
copying a complete producer identity per call. Existing finite input/output
bounds and fresh reconstruction remain in force.

Producer-declared effects and capabilities are preserved as declarations,
not relabeled inferred purity. Imported body effects, effect legality and
thrown-error analysis remain pending. The distinct type-only scope is rejected
by C8 with `:imported-body-effects-pending`, including when declarations are
empty. Runtime linking, higher-order imports, cross-profile typing, generic
specialization, complete error/effect semantics and authoritative migration
remain separate work.

The corrected direct C7 batch passed four tests and 102 assertions; the C8
boundary/local-adapter batch passed two tests and 21 assertions. The C7 groups
include overlapping regressions and are not added as unique coverage. An
earlier test-namespace read failed before tests and was corrected; it is not
passing evidence.

The first real source run reached accepted library/caller C5/C6 construction,
then C7 rejected `:module-type-input-carrier-limit`. The new aggregate check
traversed an entire retained producer request, including repeated core/wrapper
and coordinator preimages, rather than the existing per-producer boundary.
The owned JVM was interrupted after this known blocker instead of constructing
the remaining blocked variants. It exited 130 without a final timing receipt
or complete test counts. The diagnostic did not retain the specific failed
carrier counter; no exact size or node-count claim follows. The corrected
boundary checks the closed input container and actual retained callees, then
fully authenticates and types each distinct producer. It retains the existing
limits: 1,024 distinct producers, depth 128, and summed distinct canonical-core
visits of at most 1,048,576 nodes and 16,777,216 scalar units. Individual core
shape/product/signature limits and the imported output bound remain unchanged.
These counters do not claim to bound every VM operation, raw proof byte or CPU
second. Five direct tests and 118 assertions passed, including the earlier
regressions; a separate local-inference group passed three tests and 31
assertions. The bounds correction passed independent static review.

The corrected real-source composition passed one test and 72 assertions: both
extensions produced I64/Bool/String imported types, fresh replay passed,
signature tampering rejected, and separately reconstructed argument, arity and
declared-result mismatches rejected with source-linked diagnostics. C8 rejected
the authentic accepted result for pending imported body effects. The original
interrupted run remains separate, not relabeled as passing. Final focused
batches also passed local inference (3/31), higher-order regressions (2/27),
recursive regressions (2/33), the C8 boundary/local adapter (2/21), and the
language boundary (6/11). Together with the corrected direct batch (5/118) and
source composition (1/72), these are 21 test executions and 313 assertions,
not a count of unique semantic cases. JVMs were serialized at a 2 GiB heap.
This is development implementation evidence, not authoritative migration,
runtime linking, complete SH-07/SH-08/SH-09 or self-hosting completion.

### Producer-inferred imported scalar signatures: development implementation

The next L3/L5/C7 increment replaces annotation presence with complete types
derived freshly within the producer module. Function, body and syntax IDs,
ordered unique parameter IDs, fixed arity, inferred status and any present
annotations must match the actual producer facts. All existing original C5
export/origin and projected C6 joins remain in force. Caller observations do not
participate in producer inference.

The supported signature boundary is the existing literal/atomic scalar type
catalogs, including the actual nil type. Unknown types, host nil, callable types
and composite type maps reject rather than passing a shallow known-type check.
Composite and higher-order imported signatures remain implementation work, not
L5 restrictions. Request APIs, finite work bounds, immutable imported premises
and the mandatory C8 pending-body-effects rejection are unchanged.

The final direct batch passed five tests and 156 assertions, including existing
annotated imports, local-chain and work-boundary regressions. The earlier
four-test/140-assertion batch is overlapping evidence, not added coverage. The
language boundary passed six tests and 11 assertions. Independent static review
found no remaining blocker in the code, new fixtures or source test.

Real paired-source composition passed one test and 58 assertions. It derived
zero-argument Bool, annotated-parameter/inferred-return I64, producer-local
String identity and an unannotated Integer call chain through separate actual
modules. Fresh replay passed; producer/function/parameter/body tampering and
argument, arity and declared-result mismatches rejected. An actual unresolved
exported identity producer rejected even though its separate caller supplied
Bool. C8 rejected the authentic accepted result for pending imported body
effects. Both source extensions produced the same semantic identity.

The three final batches total 12 test executions and 225 assertions, not unique
semantic cases. Execution was serialized at a 2 GiB heap. The new real-source
test took 1,272.547 seconds of selected execution and 7.012 seconds of namespace
loading; the unchanged annotated-source composition was not replayed. This is
development implementation evidence, not authoritative migration, checked
runtime linking, complete effects/errors, self-hosting or release completion.

### Scalar import catalog: compiler-source dependency repair

At `5262d9cd`, the production change is confined to C7's
`sh08-im-scalar-type?`: a Keyword-family guard and an inert quoted Set replace its
two Vector-tail catalog loops. The recognized domain remains exactly 19 type
IDs from 24 original occurrences: nine literal kinds and fifteen atomic names,
with Bool/String/Symbol/Keyword/Ratio duplicated. The actual nil type is
recognized, while host nil, unknown/never, lookalike Keywords, other scalar
values, composite maps and callable types still return Bool false. Existing
producer identity, complete signature and annotation consistency checks remain.
No effects owner, emitter, runtime, primitive, schema or imported API is changed.

Before editing, original Gravity catalog/classifier/complete-signature calls at
`0699c86e` confirmed the 24-to-19 mapping, all 19 complete signatures and 20
ordinary false cases. The final focused batch at `5262d9cd` passed four tests
and 234 assertions in one invocation: catalog/signature matrix 147, dependency
observer 9, existing imported-function 51 and inferred-signature 27. It covers
all catalog IDs, 26 negative values in both return and parameter positions,
and existing identity/declaration controls. The observer delegates original
arguments/results and limits its observations to the selected loaded plan.

The first test-only RED at `129592a9` remains 154P/2F/0E out of 156. Its generic
observer did not cover optimized unary dispatch, so its zero recorded Vector
tails earn no dependency credit. The corrected `a54bcede` observer ran the
affected nine assertions with 4P/5F/0E, observing two initial Vector-tail calls
for each late recognized, unknown and composite input; later normalized List
tails were excluded. The final classifier observes zero Vector tails, one
membership query for a Keyword, and no query for a non-Keyword. This is an
operation-boundary observation, not a speedup or provider-cost claim.

One main join to `8a0ddf8d` produced `7524d538`, preserving both owned files
byte-for-byte; the intervening LLVM policy/test/notes delta is disjoint. Fresh
boundary checks passed 6/11 and LOC reports 2,022 source files, zero exceptions
and maximum 250 physical lines. The 234 assertions were not replayed merely
for that join. No C5/C6/source proof was run. The unannotated parameter and
surrounding helper typing remain independent gaps; the guard establishes no
general flow fact and this repair does not claim whole-helper source admission,
Vector-rest support, current-closure migration, self-hosting or release credit.

### Fixed match-family preimage checks: compiler-source dependency repair

At `89988457`, C7's private `sh08-ft-match-preimage-valid?` uses three explicit
ordered checks instead of traversing a fixed Vector of family names. It still
compares complete branch, pattern and decision-skeleton records, reading each
preimage value before its core value and returning false at the first mismatch.
The three-argument API, unused projection argument and matching missing/nil
behavior are unchanged. There is no partial normalization, weakened equality,
new schema or authority path; the parent closed-shape and lineage gates remain
byte-identical. Only this function changes production code.

Original Gravity baseline calls confirmed all eight mismatch combinations with
three projection values, six sparse/nil cases and rejection by the parent for
an incomplete preimage. The first test-only `8f589760` run remains
43P/36F/0E out of 79: its generic observer missed optimized `get`, so 24 empty
traces give no read-order credit. Twelve Vector-tail checks also failed. The
corrected original-delegating `9157fcd1` observer covers specialized and generic
`get` plus unary `rest`. Its 67P/12F/0E run passed all 24 exact read sequences,
all Bool outcomes, sparse cases and the parent rejection; only the expected
zero-tail checks failed, observing one initial Vector-tail call when advancing
beyond the first family.

The final `89988457` batch passed five tests/158 assertions in one invocation:
the new 79-assertion matrix, Boolean match 47, structural shape 23, semantic
identity normalization four and fail-closed boundary five. Exact read order,
early rejection and full-value comparisons are preserved, with zero Vector
tails. This is an operation observation, not a speedup or provider-cost claim.

One join to test/notes-only main `9e9c8480` produced `47e79b97`, preserving both
owned files byte-for-byte. Boundary passed 6/11; LOC reports 2,022 source files,
zero exceptions and maximum 250 physical lines. The 158 assertions were not
replayed for that unrelated join. No C5/C6/source proof was invoked. Private
helper observations do not establish authenticated preimages, whole-helper
typing, predicate flow facts, current-closure admission, migration, self-hosting
or release support.

### Ordinary compiler-artifact module calls: development implementation

The L3/C5/BOOT2 enabling increment gives the temporary compiler-artifact seed
interpreter an explicit module-bundle input. Actual caller C5 source and imports
are joined to retained callee C5 public exports and independently compiled raw
plans. Modules retain separate function tables; exact source callsite routes
select the callee. Reconstructing source, exports, plans and the whole bundle
rejects self-rehashed substitutions rather than trusting claimed hashes.
The existing three-argument raw constructor stays acyclic; the four-argument
constructor and direct commands are documented in
`docs/development-verification.md`.

The initial executable subset is unannotated fixed-arity function-only modules
in `:meta/:jvm`, with direct qualified imported calls. Rest parameters,
duplicate parameters, annotation syntax and operator-shadowing cases that the
raw seed cannot lower faithfully reject. Actual C5 bindings determine whether
an operator means a core builtin, namespace function or local binding; matching
source forms alone is not an equivalence argument. General initializers,
imported values and imported calls nested under `let`, `loop`, functions or
collection expressions remain unsupported. These are implementation gaps, not
new language restrictions.

Four final focused tests passed 57 assertions, including argument evaluation
exactly once in order, route/export/function/arity rejection before arguments,
raw-entry rejection, inert quoted DATA, B51 opaque-scope forwarding and legacy
external authorization before arguments. Dispatch instrumentation is unit
regression evidence, not native admission. Raw C7 construction passed one test
and 41 assertions; C8's pending-imported-effects rejection passed one test and
seven assertions.

The single real composition passed one test and 38 assertions. Both source
extensions built two actual modules with different functions named `identity`;
qualified calls returned the callee result for scalar and nested DATA inputs,
while a direct root call returned the caller result. Actual private/missing
exports, stale source, self-rehashed plan/route substitutions, variadics and
builtin/local/namespace-function shadow mismatches rejected. Both extensions
produced the same semantic bundle identity. The language boundary passed six
tests and 11 assertions. These final batches total 13 test executions and 154
assertions; earlier overlapping kernel batches are not added. JVMs were
serialized at a 2 GiB heap. The real composition used 497.822 seconds of selected
execution after 7.184 seconds of namespace loading; unchanged C7 source typing
proofs and historical native admission proofs were not replayed.

The bundle explicitly records development-unadmitted status, unchecked effects
and the Clojure seed/interpreter boundary. It does not accept imported user-call
effects, bypass C8's type-only rejection, grant SOURCE/B51 authority, establish
target support or admit an authoritative module revision. Next, source-local
literal DATA declarations and mixed function/var exports must be supported to
retain the actual C8 engine's six exported catalogs before its existing API can
forward body analysis into `gravity.effects`. A second facade pretending to be
that namespace is not a substitute. Full effects/errors, checked runtime
linking, migration, self-hosting and seedless release remain outstanding.

### Sequential-let module calls: development increment

Development commit `57c077ab836776b0e9b9edc7ca3c7bfac4049601` extends the
existing ordinary four-argument compiler-artifact module interface to imported
calls in symbol-only sequential `let` initializers and bodies. The owning seed
components `00575` and `00580` preserve exact source-order callsite paths through
nested scopes, shadowing and multiple body forms. C5 export/origin joins,
separate module plans, fresh bundle reconstruction and route/arity checks before
arguments are unchanged. No public API, compiler IR, type/effect rule or new
authority channel is introduced. At that revision, imported calls under `loop`,
nested functions and collection expressions were unsupported; quoted DATA stays inert.

The dedicated cheap checks passed two tests / 25 assertions. The actual Gravity
source composition passed one test / 19 assertions: it executed all five
qualified call sites, preserved initializer/body order and lexical scope,
rejected a private import and wrong arity, and rejected one coherently resealed
route before output. Paired `.qst` fixtures were checked for byte/parser parity
only; no Q C5 construction or Q runtime execution is claimed. Four existing
module regressions passed 97 assertions, language-boundary checks passed six
tests / 11 assertions, and the Clojure LOC validator passed. These separate runs
total 13 tests / 152 assertions, not a single batch. Independent implementation
and actual-result review accepted the scoped change; the old unsupported-let
oracle now uses still-unsupported `loop` instead.

The real composition took 199.719 seconds of selected execution after 6.852
seconds of namespace loading at a 2 GiB heap. Its source work was not replayed
for documentation or disjoint adapter factoring. Direct commands are in the
development-verification guide. This is executable temporary-seed composition,
not checked user-call linking/effects, H6, current-closure admission, milestone
completion, self-hosting or release evidence.

### Transitive compiler-module calls: development increment

The existing ordinary four-argument compiler-artifact interface supports an
acyclic root -> middle -> leaf chain. The sole production change is the
existing `00580-p15-s23-stage2-module-bundle.clj`: collect the bounded retained
ordinary C5 closure, freshly validate each unique source and independently
recompile every supplied nonroot plan, including routed nonleaf plans. Full
artifact equality deduplicates shared inputs; it does not cache acceptance.
Per-module route indexes keep identical local function names and callsite
paths separate. Old two-module identity/order, raw three-argument plans,
runtime, public schemas, B51 boundaries and existing limits are unchanged.

The dedicated focused selection passed three tests / 33 assertions, including
fixture parity, bounds and conflicts before source replay, reversed input
order, exact old two-module identity, per-module path isolation and substituted
nonleaf-plan rejection. These normalized tests do not establish C5 freshness.
The separate genuine Gravity composition passed one test / 36 assertions:
three actual C5 modules, independently compiled plans, public reconstruction
and execution returned the leaf value with argument -> leaf -> middle -> root
output order. It also rejected an actual private import, a missing transitive
plan and a coherently resealed transitive-origin mutation before output.
Paired Q sources provide byte/parser parity only.

The existing module route/private-entry/B51/seed-shape regressions passed four
tests / 97 assertions; the existing sequential-let cheap checks passed two /
25. The language boundary passed six / eleven, and the Clojure LOC validator
passed with no exceptions. Together with the new focused and source selections,
these separate runs total sixteen tests / 202 assertions with no failures or
errors. Independent code and actual-result review accepted the scoped change.

The source selection took 236.961 seconds after 6.696 seconds of namespace
loading at 2 GiB, below its ten-minute monitored ceiling. These are inclusive
development timings, not exclusive phase costs, a hard-deadline claim or a
speedup measurement. The complete source test was not repeated to refresh a
stamp. Focused commands and the separate composition trigger are in the
development-verification guide.

At that revision, imported calls under loop, nested functions and collection
expressions were unsupported. Effects extraction still needs its exact function/dependency
boundary and demonstrated token-bound fit; this change does not close the
retained C9 source failure. Checked imported user-call effects, general module
initialization, authoritative migration, milestone completion, self-hosting and
release remain outstanding.

### Loop-context compiler-module calls: development increment

The existing module-call path now supports qualified imports in symbol-only
sequential loop initializers, bodies, conditions and recur arguments. The
`00575` loop compiler threads the existing callsite paths only in module-route
context; raw/B51 contexts retain their original module value. The existing
`00580` walker retains function/collection blocking and inert quotation. A
one-line caller join supplies the verified source path for diagnostics, not
an authority channel. Runtime, bundle schema and public interfaces are unchanged.

The initial cheap selections passed three new tests / 53 assertions and two
existing let tests / 25 assertions. Independent audit then found a real
diagnostic-order problem before source execution: a later malformed loop could
hide an earlier non-tail recur. The failure-path-only correction delegates to
the existing whole-definition body validator, preserving parameter count,
tail position, source order and complete diagnostic fields. It adds no valid
path validation pass or copied parser. The changed diagnostic selector alone
then passed 35 assertions, including four complete walker/original diagnostic
parity controls for earlier recur and throw-arity errors.

The corrected new-namespace coverage is four distinct selectors / 104
assertions across runs: unchanged path / parity checks (20 / six), corrected
diagnostics (35), and one genuine source composition (43). The older 27
diagnostic assertions are not counted twice, and no single green four-selector
batch is claimed. The original results and statically rejected code remain in
history; there was no failed source attempt or source replay.

The actual Gravity composition constructed ordinary C5 leaf/root artifacts,
an independent leaf plan and the public module bundle. Both finite loop
backedges ran, returned `["outer" "inner"]` and preserved initializer/body/
recur-argument output order and lexical shadowing. Ten actual source routes
retain their callee origins. Private import rejection, a C5-accepted wrong-arity
caller rejected before output, and a coherently resealed callsite mutation
rejected before output all passed. Q fixtures provide byte/parser parity only.
The source selection took 199.835 seconds after 6.699 seconds of loading at
2 GiB, below its monitored ten-minute ceiling; these inclusive timings are not
a benchmark, hard deadline or exclusive phase-cost claim.

Final affected regressions passed four SH-01 tests / 97 assertions, one
transitive-routing test / 17 assertions and six language-boundary tests / 11
assertions. Together with the unchanged-path reuse described above and the
existing let checks, scoped coverage is 17 unique tests / 254 assertions,
zero failures or errors across the recorded selections. LOC validation passed
for 2,020 files, maximum 250 lines and zero exceptions.

Commands are in the development-verification guide. At that revision, imported
collection and nested-function contexts were unsupported. General module initialization, checked user-call
effects, Effects token-bound fit and the retained C9 source integration remain
open. This does not establish current-cohort admission, milestone completion,
self-hosting or release support.

### Vector-context compiler-module calls: development increment

The existing `00575` vector compiler and `00580` callsite walker support
qualified imports in function-body vector expressions. Elements retain ordered
zero-based paths through nested vectors, let initializers and loop bodies.
Only the existing module-route context changes; raw/B51 compilation retains
the original module object. Runtime, bundle schema, public interfaces and IR
are unchanged. Quotes remain inert, and inherited function/map/set fences
still reject imported calls. Top-level `def` initializers remain literal-only.

The new cheap selection passed three tests / 50 assertions, including exact
paths, paired parser parity and original diagnostic-order controls. Affected
let and loop selections passed two / 25 and three / 61 respectively: eight
tests / 136 assertions across three separate runs, zero failures or errors.
Exactly four obsolete vector negatives in those owning tests were replaced
with still-unsupported map/set cases; the shared SH-01 fn control is unchanged.
No old source composition was repeated for those oracle updates.

The first genuine Gravity source composition passed one test / 26 assertions.
Six authenticated import routes execute nested vector/let forms and a real
loop backedge, preserve `first`, `loop`, `left`, `right` output order and
return ordered nested values while quoted private-call DATA remains inert.
Private C5 import rejection, a C5-accepted caller rejected for wrong arity by
the bundle constructor, and a coherently resealed route mutation rejected
before the first runtime effect all passed. Paired Q sources provide byte/
parser parity only. The source selection took 197.695 seconds after 6.567
seconds of loading at 2 GiB, below a monitored ten-minute ceiling; this is
not a benchmark, hard deadline or exclusive phase-cost claim. Allocation/IO
declarations do not establish C8 legality or provider permissions: the
temporary seed boundary still reports effects not checked.

Final SH-01 module regressions passed four tests / 97 assertions and the
transitive-routing check passed one / 17. LOC validation passed for 2,020
source files, zero exceptions and maximum 250 lines. A boundary command first
failed before loading tests because it ran from the wrong directory; that
failed output and absent receipt remain recorded. Only that command was
corrected, with new outputs: the repository-root boundary selection passed
six tests / 11 assertions. Scoped unique coverage totals 20 tests / 287
assertions across completed selections, not a claim that every launch passed.

Direct commands are in the development-verification guide. Map/set and nested
function contexts, general module initialization, checked user-call effects,
Effects token-bound fit and C9 source integration remain separate work.
Focused checks do not establish current-cohort admission, milestone completion,
self-hosting or release support.

### Compiler-module Map literal order: development correction

The module-development constructor now derives evaluated Map order from
retained authenticated C2 occurrences after fresh original C5 verification.
An exact `defn` expansion/body join keeps source evaluation order separate
from host-map coordinates. Recur/throw validation and Map emission consume
that same derived order; public bundle reconstruction independently derives
it for every module, including a dependency without imports. An incompatible
legacy raw dependency plan rejects rather than bypassing reconstruction.
Public bundle/IR/runtime interfaces and raw three-argument/B51 behavior are
unchanged. One 118-line stage-owned component contains the join and the moved
existing collection compiler; this is not a new verification lane.

This first module subset requires distinct literal Keyword keys in every
evaluated function-body Map, even pure Maps. Older pure non-Keyword Maps can
therefore reject at this module constructor: this is an explicit development
frontier, not an L2 restriction or preservation of all formerly compilable
modules. Quoted Maps remain DATA. Depth 64 and 65,536 visits are local traversal
guards, not new language rules. Imports inside Maps, Sets and nested functions
remain blocked. No checked-effect, Effects token-fit or C9 repair claim follows.

The first cheap run completed one test / 35 assertions with three failures:
its expected result retained source expressions where compiled IR contained
the literal Keyword values. Production and fixtures were unchanged by the single
oracle correction. The corrected run passed all three selected tests / 45
assertions, including source-order diagnostics and paired parser parity.
Normalized C2 test DATA is not fresh compiler evidence.

The sole affected genuine Gravity source composition passed one test / 15
assertions. Nine nonalphabetical Map entries exercise exact output order and
lookup values in leaf let/loop and root local-call contexts. Current no-import
leaf and root bundles reconstruct, legacy raw dependency plans reject, and a
coherently resealed reordered leaf plan rejects before output. The paired Q
fixtures establish parser parity only. Selected execution took 145.893 seconds
after 6.652 seconds of loading at 2 GiB, below a monitored ten-minute ceiling;
this is not a benchmark or hard deadline. The temporary seed boundary remains
development-unadmitted. Final source-free checks passed the adjusted transitive
routing test (one / 17), SH-01 module regressions (four / 97), existing let
(two / 25), loop (three / 61), vector (three / 50), and language boundary
(six / 11). With the new cheap and source selections this is 23 unique tests /
321 assertions, all passing in the completed final selections. The earlier
three failed oracle assertions remain historical failures, not passing coverage.
The only old transitive-test change adapts its artificial raw-plan stub to the
private context arity and normalizes its empty route index; its original
identity/order assertions remain. LOC validation passed 2,021 files, zero
exceptions, maximum 250 lines. No old source composition was replayed.
Focused implementation and this affected composition do not establish
authoritative migration, milestone or self-hosting completion.

### Private call-entry get with a finite Vector result: development increment

Private-entry proven-present `get` now admits an independently established
finite positional Vector result, including a Vector containing finite Map
types. An empty Vector is a valid lookup result, not a nonempty `first` fact.
This removes the scalar-only implementation restriction in C7, C8 and typed
lowering; L5 already permits the independently established selected stored
type. Runtime lookup, public interfaces, descriptor schema and type grammar
are unchanged. Full entry/use/visibility/re-entry evidence remains required.
Map member types alone do not establish inner keys or a common deep shape.

Complete candidate type validation is paid with the remaining budget before
deep comparison. Existing scalar recursive-work charges remain zero. The
owning scoped C7 consumer pays its new selection scan and reuses that result
only within the invocation. C8 always rebuilds its internal lookup context
from current public facts, checks current headers and entry types, then checks
the descriptor, fact and ledger. That local DATA is not a persisted proof cache.
Existing Vector-index and non-invoked lookup paths retain their own owners.

Three early focused attempts remain failed: V1 completed one test / 13 passing
assertions / two failures, with three selectors unrun, exposing an inverted
lowering budget guard. V2 completed five tests / 198 passing assertions / one
failure; that hostile test rewrote the private leaf's internally derived paid
context as well as public facts. The public C8 path always overwrites that
context, so the corrected test retains the original internal premise and
mutates public facts. V3 completed one test / five passing assertions / six
failures / two errors, with three selectors unrun. Its appended caller nodes
lacked required binding lists and consistent IDs, so the consumer was not
reached; dependent nil-budget assertions caused the secondary errors. The
test graph was repaired without weakening compiler checks.

The affected V4 batch passed all five tests / 320 assertions: the actual
scoped C7 consumer, existing Vector-loop lookup, C8 present-Map normal and
non-invoked behavior, private Vector results and Vector-context compatibility.
The consumer control observes one measured helper, one finish, six paid type
walks, no unrelated IO probe and no old Boolean consumer call. Exact and
one-short budget cases complete. The cheap G/Q fixture check separately
passed one test / seven assertions; Q remains parser-only. LOC passed 2,021
files, zero exceptions, maximum 250 lines.

The first source run completed one test / 62 passing assertions / one failure /
zero errors in 232.745 seconds after 11.187 seconds loading at 2 GiB. It remains
a failed aggregate. Both G source attempts, fresh public C7 assembly checks,
C8 construction/verification and four Gravity runtime return observations
completed. These include distinct stored Map values and an empty Vector
lookup. The missing-key fixture's owning rejection assertion passed; the raw
log does not print its complete diagnostic vector. There are zero same-IR seed
comparisons: the seed cannot execute emitted `:keyword-map-lookup`. Returned
Map observations are not a Gravity Map-equality claim.

The sole source failure pinned coordinates to literal pair order. Quoted
Maps are materialized DATA and promise no such enumeration order; both entries
authenticated the demanded key at coordinate 3. The test-only correction joins
each selection to its own authenticated keyed Map part rather than pinning
host order or claiming source-coordinate diversity. The corrected source
assertion is not source-reexecuted, and the failed aggregate is not relabeled.
Normalized entry controls cover differing coordinates independently. No
production or fixture change justifies replaying unchanged source construction
for this oracle repair. The new normalized selection oracle and the three
affected existing scalar-owner kernels passed four tests / 76 assertions;
the oracle's eight controls cover equal/different coordinates, mismatched
coordinates or selected parts, missing keys and duplicate keys. These checks
do not execute the corrected source assertion. Boundary checks separately
passed six tests / 11 assertions. Inner-key ancestry, unchanged full C17 execution,
current-closure admission, SH-07 and self-hosting completion remain open.

### Private first-entry inner-key lookup: development increment

The C7, C8 and typed-emitter increment reconstructs the demanded chain
`(get (first (get package :signatures)) :algorithm)`, including two immutable
local aliases. Each normal private entry must independently establish the outer
key, nonempty finite Vector, inner stored key and compatible leaf type. No
common deep parameter shape is invented. Runtime instructions and the existing
54-function development projection are unchanged.

Focused development evidence includes these distinct affected batches; their
overlapping assertions must not be added as independent coverage:

- C7/C8/emitter composition and C6-origin compatibility: four tests / 137 passes.
- C7 invocation-local construction reuse: three tests / 78 passes.
- Unused-argument normality plus the existing reuse kernel: two tests / 37 passes,
  including all seven new checks for one or all entries becoming non-invoked.
- The latest C8 projection correction: two tests / 65 passes, including 13
  unconditional malformed-selector checks.
- C8/emitter reference-value traversal: four tests / 167 passes, including all
  65 new value-walk guards. Fixed program-key names are prepaid before reads;
  every dynamic value and all eight independent carrier comparisons remain.

The scalar frontier is Nil, Bool, Integer, Keyword, String and Symbol, not
Character. Symbol controls use a wholly quoted container; quoted scalars inside
evaluated containers are unsupported. Arbitrary deep paths, dynamic indices,
public record ingress, entry argument aliases and callee-return ancestry remain
outside this increment.

The accepted/rejected G/Q fixture pair has two functions and four calls.
Byte/parser checks passed one test / eight assertions. The negative changes
only the stored `:algorithm` key to `:scheme`, preserving its Keyword value/type.
The first four source integration attempts exposed these failures:

- The first run stopped at C7 `:map-get-source-mismatch`: two passes, one failure
  and one prerequisite error. The corrected guard retains C6's bounded
  generated-origin Vector; singleton normalized controls are not macro execution.
- The next run stopped at C7's unchanged work limit with the same counts.
- After the C7 correction, C5/C6 and fresh public C7 verification passed, but C8
  construction hit `:body-deferred-work-limit`: four passes, one failure and one
  prerequisite error.
- After removing a redundant C8 projection, the next source run again passed
  those upstream gates but reached `:list-if-join-work-limit` in C8, with four
  passes, one failure and one prerequisite error.

All failed attempts remain failures. C8 verification, whole-function assembly,
runtime and the negative G product were not reached by those four source runs.

The latest changed-source run reached all those stages: the positive returned
`:sha256` through typed lowering and Gravity runtime execution, and the negative
G product rejected the absent inner key. Its aggregate was 35 passes / one
failure / zero errors, not a passing test: the rejection assertion expected
`C7-ANNOTATION`, but this run's parent-match presence diagnostic came from
`C7-VERIFY`.
The failed aggregate remains retained. The exact corrected assertion was
extracted from the test and checked against the retained actual diagnostics:
nine assertions passed, including wrong-rule, wrong-reason and missing-field
rejections. These were zero new source products, not another source execution
or a relabeling of the failed aggregate.

Bounded attribution separates representation work from scheduling. C7 reuses
constructed source DATA only within one fresh empty-initialized inference
invocation. Current semantic dependencies and fresh parent derivations are still
checked and charged; no acceptance, status or prior work count is reused, and
no table escapes into public results. Public verification independently rebuilds.
C8 and lowering removed an equality-only temporary ancestry view, not the parent proof,
demanded current C6/C2 source replay or independent fact/ledger checks.
The 65,536-unit ceiling is unchanged.

Retained-input diagnostics are useful feedback, not public acceptance. Early
projections omitted actual binding populations and complete binding-record
fields; their apparent budget fit did not establish real-source fit. The
later diagnostic used the closed 18-field binding schema and owning fact
constructors, fitting both C8 and whole-helper lowering under unchanged limits.
That diagnostic alone did not establish source acceptance. No heap, OOM, scheduling bottleneck or
general proof-product redesign follows from these work counts.

The BU source test now uses the existing fresh development-session API to avoid
a second C6 construction. Its durable extracted-initializer test passed one
test / 22 wiring/guard assertions, with zero source products or compiler proofs;
an earlier missing-require error remains retained. The latest source pair used
this route with explicit non-authoritative timing labels. It did not invoke the
original independent public C6 fresh verifier; the earlier source failures did.
Original authoritative verifiers and shared source helpers remain unchanged.

Unchanged whole C17, Q typed products,
current-closure admission, SH-07, self-hosting and release completion remain
unproved.

### C7 private-entry construction work: development follow-up

Two internal corrections remove actual discarded construction, not proof
obligations: C7 no longer builds an equality-only temporary ancestry view,
and its invocation-local dependency data no longer copies argument type trees
already retained by every argument's current/fallback observations. Fresh
BT/BS derivation, nonselected and noninvoked arguments, source joins, public
descriptors and the 65,536-unit budget remain unchanged. The corrected focused
batch passed nine tests / 244 assertions. An earlier new assertion read a
field from the wrong proof owner; that failed batch remains recorded.
The retained negative now reaches the full-projection `C7-ANNOTATION` presence
diagnostic rather than the earlier parent-match `C7-VERIFY` branch. The exact
source-test assertion follows that owner; nine extracted-expression checks
passed on the changed development replay, with zero source re-executions.
The isolated candidate also passed four existing C8/emitter composition kernels
/ 167 assertions on normalized inputs. These are affected integration checks,
not fresh source construction or authoritative migration.

The unchanged C17 signature-label helper remains a separate unsupported
composition, not an admission prerequisite for ordinary edits. Its six-function,
four-entry development candidate passed exact helper AST and direct fixture
checks, but its single typed-source attempt stopped at C7's work limit after
C5 and fresh C6 development-session checks passed. C8, typed assembly and
runtime did not run. The candidate is retained separately, not merged as a
passing integration test. Direct signature-label results are not cryptographic
verification, plugin execution or whole-C17 support.

Retained-input diagnostics measured savings of 17,588 temporary-ancestry units
and 1,484 duplicate-type units, but still failed the first inner query. A
transparent static lower bound also rules out the proposed adjacent-field plus
cross-key prefix fix: it remains over budget before leaves and C2 authentication.
No speculative prefix table, source-guard deletion, budget increase or source
retry follows. The bounded follow-up is an exact construction-ownership design
at the existing BT/BS-to-BU boundary, preserving artifacts, diagnostic order,
core-only versus C2 validation, and fresh per-invocation reconstruction.
These work counts are not heap, elapsed-time or speedup claims.

### Proven-nonempty Vector first: development increment

STD3 now explicitly defines the first stored value of a nonempty persistent
Vector, including false or Nil. The implementation accepts a complete finite
positional Vector type with at least one element, validating the tail as well
as the head. It does not add a general nonempty-flow solver, unknown-length
Vector support or an empty-Vector result policy. Empty/bare/unknown-length
types reject as unsupported typing, not a new SAFE2 bounds prohibition.

C7 typing, C8 effect reconstruction, typed lowering and the existing runtime
are the four implementation owners. Source identities, complete type facts,
selected index zero and independent producer/consumer joins precede lowering.
An operand is evaluated once; transfer aborts selection. Runtime guards remain,
and a stored payload is returned inertly without hashing or executing it.
The query creates no collection of its own; operand construction effects remain.
Existing List behavior and the separate unresolved empty-Vector policy are
not inferred from host behavior.

The nonduplicated passing focused union is 13 selectors / 1,432 assertions
across three runs, not a single aggregate batch. An initial adjacent run had
two failed assertions from a stale List work oracle: the existing 223-unit
source-work prefix is retained, and the namespace-reference rejection adds
64 child-work units. The corrected test records 287 total without changing List production
behavior. Independent review also found and closed a cross-kind guard bypass:
an actual Vector-first receiver cannot evade fact validation and ledger
prepayment by supplying a List-kind operator header. The affected rerun passed
two tests / 156 assertions; seven previously unrun adjacent tests separately
passed 891. The superseded lowering count and failed assertions are not added
to the final passing union.

The first fixture preflight passed one test / seven assertions after the
accepted C5 catalog merge, with all 12 slice-owned files unchanged. It checks
G/Q bytes and parsing and the exact unchanged C17 hexadecimal Character helper
form. Q coverage is parser-only.

The first source composition completed naturally with one test / 142 passing
assertions / one failed assertion / zero errors. It is a failed batch, not a
passing source proof. The sole failure expected three ordinary local-call
facts to carry a dispatch tag which their producer omits; the six intrinsic
dispatches matched. The run completed both genuine G source attempts,
C5/C6/C7 checks, fresh public C7 verification, C8 reconstruction and hostile
consumer controls. Nine Gravity entry invocations completed, including stored
false/Nil, an inert throwing payload and caught operand transfer. Eight normal
entries also completed same-emitted-IR seed comparisons; the caught-transfer
entry was excluded. The empty-Vector fixture rejected at C7
with the supported-subset diagnostic. Execution took 205.184 seconds after
11.113 seconds loading at 2 GiB, within the monitored ten-minute bound.
These observations do not erase the failed histogram assertion or establish
whole-C17 caller coverage. This is an ordinary development increment, not
SH-07, current-closure migration or self-hosting completion.

Independent review traced the failed oracle to the original fact producer:
ordinary local-call facts omit `:dispatch`; `:direct-local-function` belongs
to a different higher-order case. Their canonical call-edge classification is
`:local-function`. A test-only correction checks the intrinsic histogram and
joins ordinary facts to those original edges by ID. The corrected source
assertion is not source-reexecuted; the failed aggregate remains failed.
No production, fixture or source-product change requires rebuilding the same
two source products just to repair this assertion. The focused normalized C7
schema control passed one test / 23 assertions, checking call/callee IDs,
argument/result types, fact domain and the separate higher-order tag case.
It does not execute the corrected source assertion. Boundary checks separately
passed six tests / 11 assertions; LOC passed 2,021 files, zero exceptions,
maximum 250 lines.

### Ordinary C5 recognition of the declared List predicate: development repair

A deliberately selected real runtime producer-consumer check exposed a
declaration mismatch, not a passing whole-module composition. The retained
candidate `536d7395` stopped naturally during dependency C5 construction with
`C5-UNRESOLVED` for operator `list?` in `gravity.runtime`: one test, zero
passes, zero failed assertions and one error. Execution took 565.702 seconds
after 8.986 seconds of namespace loading at 2 GiB, before the monitored
ten-minute bound. No timeout or OOM was observed. Caller C5, both module
bundles, routes, runtime execution and all 59 planned semantic checks were
unrun. Its separate cheap interface test passed one test / ten assertions;
that is AST feedback, not accepted dependency construction.

The ordinary request catalog omitted `list?` even though the existing seed
builtin declarations, executor and B51 catalog already recognize it. The
repair adds only that name to `00870` plus a dedicated focused test. It does
not change the Gravity resolver algorithm, seed or runtime semantics, import
policy, public interfaces, profiles or target implementation support.

The new check passed on its first run: one test / 47 assertions at 512 MiB,
using three tiny genuine source attempts and the original fresh C5 verifier.
It checks unqualified and qualified core identity/provenance, lexical
shadowing and quote inertness; an unknown lookalike and unimported
`clojure.core/list?` reject without a returned artifact. Execution took 67.391
seconds after 7.295 seconds of loading. These are inclusive development
observations, not a benchmark. LOC validation passed 2,021 source files,
zero exceptions, maximum 250 lines.

Catalog insertion can shift sorted core-row ordinals and derived identities.
Affected request, consumer and authoritative evidence must be reassessed at
their declared revisions; no corpus count/hash, old receipt or historical
status is repinned. This recognition check does not complete a typed List
predicate, current-closure resolution or any open SH-05/SH-06 group. The
resolver remains development-unadmitted. The failed runtime attempt is not
retroactively accepted and is not replayed after this repair.

The public C5 route includes C2/C3/SH05 construction and fresh verification,
resolution construction and, on successful construction, replay. This attempt
stopped at its first resolution-template rejection, before C5 construction-proof
replay; hosted C6 was not reached. The phase marker does not isolate those
costs or establish a heap/GC cause, and
does not explain the separate incomplete Checked-Core attempt. A bounded
follow-up may distinguish those existing boundaries on a deliberately small
subject before considering representation changes. No new lane, wrapper,
acceptance cache, increased limit or unchanged large-subject retry is selected.
Ordinary semantic implementation continues independently.

### Evaluated literal-Character Sets: development increment

The existing typed emitter and runtime extend evaluated Set construction to
homogeneous Character literal children. Existing C7 finite member types and
C8 constructor allocation semantics remain unchanged. Source occurrence,
ordered-child, parent, attribute and type joins precede original non-tail
child compilation; the cached aggregate value is not authority. The runtime
validates all values, reserves complete logical work, rejects semantic
duplicates, then inserts. Supplementary carriers compare by Character class
and codepoint, not whole host-Map identity. Source decoding remains C2-owned.

The two new focused kernels passed two tests / 259 assertions on their first
run, including supplementary values, equal codepoints with different carrier
text, source/type mismatches, exact/short budgets, failed-child work and
malformed values without comparisons or insertions. Six adjacent checks
separately passed 816 assertions: Keyword construction, Character membership,
Character typing and constructor allocation. One private runtime helper brings
the selected loader to 54 functions with the same four public exports.

The fixture preflight passed one test / 14 assertions, including complete AST
parity with the original unannotated C17 hex-character helper, paired C2
values and original C2 duplicate supplementary-Character rejection for G and
Q. The changed source composition then passed one test / 146 assertions at
2 GiB. It executed the unchanged helper form, including its unannotated
parameter, with present and absent hex Characters. Nine Gravity entries and
nine same-emitted-IR seed controls also cover reordered, supplementary and
empty Sets. Four actual evaluated Set instructions contain 20 Character
children and no quote substitution. C8 records constructor allocation,
including empty construction and transitive caller effects.

Original fresh C7 verification guards assembly and rejects a bare-Set type
forgery; original C8 verification rejects a resealed deletion of the direct
allocation effect. A mixed Character/String Set passes C5/C6/C7/C8 and their
selected verifiers, then rejects at `P15S23Q002/:unsupported-typed-set-member`.
This is a bounded lowering frontier, not a language-wide mixed-Set ban. Q
receives no typed-product comparison. Seed controls interpret the same emitted
IR, not independently compiled raw source; the whole C17 module is not built.

Source execution took 193.681 seconds after 10.783 seconds of namespace
loading, below the monitored ten-minute bound. These inclusive observations
are not a phase-isolated benchmark or hard launcher deadline. The four passing
selections total ten tests / 1,235 assertions, not one batch; no failed focused
or source attempt was relabelled or repeated. Separate language-boundary
checks passed six tests / 11 assertions. LOC validation passed 2,021 source
files, zero exceptions, maximum 250 lines.

Empty construction and Keyword paths remain unchanged. Mixed families,
arbitrary expression-valued members, evaluated Set module initializers,
Character-Set equality and generic conversion remain outside this bounded
increment, not newly illegal Gravity. Focused and affected-composition results
do not establish current-closure admission, full SH-07, self-hosting or release.

### Evaluated literal-String Sets: development increment

The existing typed emitter and runtime construct homogeneous, distinct literal
String Sets, including empty construction through the unchanged legacy path.
C7 finite member types and C8 allocation semantics are unchanged. Lowering
validates actual source/attribute String values and reserves their content work
before equality, preserving source, child, type and evaluation joins. Runtime
validates the complete homogeneous domain, prepays logical content/comparison
work and rejects duplicate Gravity String values before host Set insertion.
There is no new instruction, function, public export or C7/C8 interface.
The existing 1,024-member/65,536-work bounds remain; selected JVM UTF-16 units
describe representation work, not String cardinality or provider CPU bounds.

The first six-kernel run completed 711 passes, five failures and ten errors.
The failures came from newly legal single-String cases and a mixed-input
first-family diagnostic expectation; the errors came from new test arithmetic
omitting the emitter's existing failed-child default charge of one. A four-line
test-only correction then passed all six
kernels/726 assertions: 247 new String checks and 479 Keyword/Character
regressions. Production bytes did not change between those runs. Same-hash
distinct Strings, equal-content distinct objects, composed/decomposed values,
supplementary Strings, empty values, malformed domains, stale joins, exact and
short work budgets, and zero-callback opaque values are included.

Two fixture kernels separately passed 26 assertions, including owning-C2
G/Q values and decoded duplicate-literal rejection. The former String-only
negative pair is moved byte-identically to accepted fixtures; the old Keyword
composition now selects the unchanged mixed Character/String negative and its
actual `values` function. Its earlier String-only rejection remains historical,
not a current unsupported case. The old large positive was not replayed merely
to update that frontier.

One actual source composition then passed one test/47 assertions (41 composition
checks plus six existing runtime-loader checks) in 131.732 seconds after 10.996
seconds loading SH01 at 2 GiB. The accepted one-function program constructed two unquoted Sets
with four literal String children, retained constructor allocation, and returned
`[#{".gravity" ".qst"} 2]` through the 54-function runtime. Original
C7 and C8 verification guarded the positive composition. The existing mixed
Character/String program independently passed fresh upstream checks and C8
verification, then rejected at `P15S23Q002/:unsupported-typed-set-member`.
Two additionally observed public C7 verification gates passed, one for each
lowering boundary. QST has no typed-product comparison, and no seed reference
execution is claimed. Fresh development C6 sessions and same-catalog replay
are not fresh public C6 authority verification. Inclusive elapsed boundaries
are not a speedup claim.

String-Set membership, computed or mixed members, evaluated Set module
initializers and whole compiler-consumer execution remain outside this slice.
Focused checks and traceability do not admit a stable module revision or close
SH-07, self-hosting or release gates.

### Finite Character-Set membership: development increment

The C7 engine, effects, typed emitter and runtime extend binary `contains?`
to a finite Character-Set value and Character needle. Independently derived
type/descriptor facts retain existing Never transfers, closed domains and
producer-row validation. Runtime work validates all members and the needle,
prepays comparisons, rejects semantic duplicates, then answers hit or miss.
Existing Keyword membership, public interfaces and limits remain unchanged.
C2 owns source Unicode validity; this does not strengthen arbitrary host-value
validation or establish a provider CPU bound.

The independently reviewed focused candidate passed seven tests / 773
assertions, all selected kernels complete. Four earlier failed attempts remain
separate: an unsupported seed operator stopped the first kernel; the next
three exposed test-oracle errors in C2 diagnostic ownership, failed-child work
and the prescan failure phase. The corrected production uses supported seed
operations and rejects opaque producer IDs before indexed lookup. No failed
attempt is counted as passing coverage. Adjacent Keyword/Map membership and
atomic Character equality checks separately passed 11 tests / 920 assertions.

The paired fixture preflight separately passed one test / eight assertions,
including actual C17 sixteen-character value parity and the new producer's
quoted AST. This is parser feedback, not a typed QST source composition.

The affected source composition then passed one test / 158 assertions at
2 GiB: seven Gravity function entries, five normal same-emitted-IR seed
controls, both genuine caught transfer positions and six membership
descriptors. Original fresh C7 verification guards assembly and rejects a
coherently propagated membership-policy forgery; C8 construction/verification
preserves pure queries and rejects a resealed dependency deletion. A numeric
Set passes C5/C6 and rejects at C7 with the operand-type diagnostic; the full
negative output also contains the secondary unresolved-annotation diagnostic.
QST remains parser-only. Supplementary and hostile carriers were checked in
focused IR tests, not this ASCII hex source subject.

Source execution took 208.285 seconds after 10.645 seconds of namespace
loading, below the monitored ten-minute bound. These inclusive observations
are not phase-isolated benchmarks or a hard launcher deadline. Product
construction and verification dominate the elapsed work; no unchanged
expensive proof was replayed for a label refresh.

The four final passing selections total 20 tests / 1,859 assertions, not one
batch; the four earlier failed attempts are excluded. Separate language-boundary
checks passed six tests / 11 assertions. LOC validation passed 2,021 source
files, zero exceptions, maximum 250 lines. These are semantic and
affected-composition development results, not migration evidence. A quoted
C17 hex-character Set value is not execution of the unchanged C17 helper:
evaluated Character-Set construction was outside this membership increment.
Character-Set equality, generic conversion and whole-C17 execution remain
unsupported by this increment. SH-07 and authoritative admission remain open.

### Finite String-Set membership: development increment

The existing C7, C8, typed emitter and runtime membership owners support a
finite all-String Set and String needle, including empty Sets. Distinct closed
String descriptor, fact, producer and policy identities preserve independent
operand typing, source/binding joins, complete ordered-ledger reconstruction
and measured work. Keyword/Character behavior and the existing 1,024-member
and 65,536-work limits remain unchanged. Normal operands retain domain checks
even when another operand transfers; legacy Never dispatch is preserved.

Runtime validates the full String domain and needle, prepays logical content,
pair and search work, rejects duplicate value classes, then answers hit or miss
using exact case-sensitive String equality. No normalization, host Set lookup,
hash authority, insertion or conversion is used to answer membership. The query
adds no source-level allocation effect; physical host allocation-freedom is not
established. Existing constructor allocation in an operand remains visible. There is no
new function, export, opcode or constructor interface.

The first frozen focused run passed 17 tests/2,207 assertions: 764 new String
checks and 1,443 adjacent Keyword/Character membership and String-constructor
checks. Before any JVM run, review caught an early type-hashing path in the
new Never selector; the local guard and direct zero-callback controls are part
of the passing candidate. The tests cover domain/source/fact/ledger tampering,
exact and short budgets, owning-consumer work, non-tail transfer handling,
empty/hit/miss/reordered and supplementary Strings, equal-content distinct
objects, hash collisions, duplicate classes and malformed later members.
The paired owning-C2 fixture preflight separately passed one test/six assertions.

The actual changed-source composition then passed one test/42 assertions
(36 composition checks plus six existing runtime-loader checks) in 153.750
seconds after 11.527 seconds loading SH01 at 2 GiB. A String-annotated helper
executed the extension-Set membership expression for three callers, producing
`[true true false]`. The positive has two functions, four call facts, one String
membership descriptor/ledger row and one evaluated Set instruction with two
literal String children. Original C7 and C8 verification passed, as did an
additional observed public C7 assembly gate. C8 retains constructor allocation
and no direct query allocation effect. One top-level runtime entry is counted;
runtime still has 54 functions.

The numeric-Set/String-needle negative passed fresh C5/C6 development checks
and rejected in C7 with `C7-TYPE-MISMATCH` and
`:string-set-contains-operand-type-required`. Its printed diagnostic vector also
retains the unresolved-core-node annotation diagnostic. That negative has no
C8 or lowering acceptance claim. QST has parser-only evidence, and no seed
reference execution is claimed. Fresh development C6 sessions and same-catalog
replay are not fresh public C6 authority verification. Inclusive timings are
not phase-isolated performance or speedup evidence.

The selected runtime assumes authenticated typing and type-conforming immutable
finite Set providers with coherent cardinality and enumeration. Detectable
inconsistent host views are robustness checks, not exhaustive host-provider
authentication. No eager snapshot or new seed primitive was added to pursue
that separate boundary. Dishonest or infinite host/FFI providers have no claimed
CPU or peak-memory containment. Representation work units do not define String
cardinality or establish a provider CPU bound.

Bare, unknown or mixed Set types, wrong needle families, String-Set equality,
general conversion-result inference and whole compiler-validator execution
remain outside this increment. Focused and traceability results do not close
authoritative migration, SH-07, self-hosting or release gates.

### Finite Integer-Set membership: development increment

The existing C7, C8, typed emitter and local runtime now support ordinary
`contains?` over independently finite literal Sets of generic
`:gravity.type/integer` with the same generic Integer needle. This covers the
reader's codepoint catalogs and compiler arity catalogs as motivating
subexpressions; it does not establish execution or typing of those whole
helpers. L2/L5, STD3/STD5, D8/D9 and R4 govern the slice. Literal Set construction
is supported in the existing emitter/runtime helpers and existing IR; runtime
inventory remains 56 functions and all nine protected carrier predicates are
unchanged.

Membership compares original Integer values, without arithmetic, conversion,
rounding or i64 narrowing. Static domains are literal positional Sets of at
most 1,024 Integer members. Mixed, unknown, bare/homogeneous and fixed-width
member domains remain rejected. Every normal operand is validated even when
another transfers; empty Set plus Never retains the prior ambiguous non-invoked
route. C7 descriptors, constraints and call facts are independently reconstructed
by C8 and the emitter. Operand evaluation remains once, Set then needle;
primitive effects remain empty while literal Set allocation effects survive.

Runtime validation precedes comparison, complete pairwise uniqueness precedes
hit/miss or host insertion, and the full worst-case logical reservation precedes
results. Integer units count validated leaves, never decimal formatting. They
establish no precision, provider CPU or heap bound; runtime work rejection can
precede a result even for a statically accepted width. No Integer Set carrier,
`conj`, equality, refinement, reader or foreign/String boundary is added.

Retained finite failures are not erased: the initial domain RED was 1 / 117
(43 failures); initial integration was 6 / 577 (eight failures: the missing
Integer Set IR gate and one diagnostic expectation); an earlier narrow
affected selection was 9 / 1,360 (one legacy Integer-rejection expectation).
The corrected checks below supersede those expectations, not their receipts.

The focused partition passed 20 tests / 2,742 assertions and the separate
constructor/runtime/paired-fixture partition passed 3 / 211. One actual leased
source integration then passed 1 / 144: the G positive reached fresh C7 and C8
verification, emitted six Set literals and six membership calls, and executed
one closed entry as `[false true true false true true]`. The G Decimal-member
negative was rejected by C7. Its C7 verifier was invoked, but that negative report
status was not asserted or printed. QST has byte/parser parity only. These are
24 distinct selectors / 3,097 passing assertions across three invocations,
not one combined run. Boundary checks passed 6 / 11; LOC passed for 2,022 files,
zero exceptions, maximum 250 physical lines.

Raw source evidence uses the
`/tmp/integer-set-membership-source-lease-20261021` prefix: original PTY 8298,
natural exit 0, all 13 inputs unchanged, canonical heavy lock and memory-heavy
lease released, no Java survivors. Two observed polling intervals exceeded the
planned 30 seconds (35.42s and 37.38s); the postflight retains that qualification.
The single reconciliation with main `6705a23a` produced `ce979be4`; all 11
owned code/test/fixture byte sequences were preserved. Unrelated SH22 request
APIs do not establish this ordinary-source intrinsic or require its replay.
Monitoring was not hard OS containment. Public fresh C6, authoritative migration,
native, whole-helper, self-hosting and release evidence remain unrun.

### Proven nonnegative Integer quotient: development increment

SAFE9/STD5 and L5 govern this ordinary-source numeric slice. The existing C7,
C8, typed emitter and runtime owners admit generic Integer `quot` when the
dividend is an original nonnegative Integer literal or independently
reconstructed normal collection count, and the divisor is an original positive
Integer literal. This covers the emitter's count/divisor subexpressions. The
reader helper's unproven parameter and loop range, aliases and the surrounding
factor-two multiplication remain outside this increment.

The retained proof carries the nonnegative Euclidean law, excludes zero and
value overflow, and bounds the result between zero and the dividend. It does
not prove allocation freedom: normal direct/latent effects include
`:memory/allocate`, and C8 records possible Integer-result storage with
representation/provider/ownership placement still pending. Every normal
operand remains proved under Never; operand effects and left-to-right-once
execution survive. C8 and the emitter independently reconstruct source,
call/edge/binding/type joins, cardinality law, numeric ledger and call facts.
Existing builtin-call IR is used, without narrowing or negative signed-rounding
policy. Runtime validation backs, rather than substitutes for, static proof.
Logical Integer work units establish no precision, provider CPU or heap bound.
Runtime remains 56 functions; all nine protected carrier predicates are
byte-identical to the accepted base.

Failed finite history remains failed. The test-only literal RED and initial
integration each recorded 28 failures in 1 / 32; the bounded identity grouping
was corrected without raising its limit. Initial C8 integration recorded three
failures in 1 / 57 because the owning join dispatch was absent. An analyzer
check recorded one failure in 1 / 61 from a wrong graph-field expectation.
The first broad 26 / 3,160 retained one stale bare-Set count negative; all 13
transitive count-owner functions were unchanged. A Decimal negative and
explicit accepted Set/Integer/persistent-Set controls corrected that oracle.
Original `/tmp/nonnegative-integer-quotient-*` log/EDN receipts are preserved.

At `0cfcea6d` the corrected affected batch passed 26 / 3,163. Independent
review then identified the scoped descriptor-kind bypass: the test-only
`4f34b3a1` epoch recorded 20 passes / one failure in 1 / 21. The correction
selects bounded quotient reconstruction from the actual authenticated binding.
At `1fcfa05c`, missing/nil/foreign/other-family kinds and non-map/nil operator
descriptors reject through both the owner and original scoped consumer;
expanded exact/minus-one checks passed 1 / 27, and four retained shared-dispatch
controls passed 4 / 85. These are separate epochs, not one combined green run.
Boundary passed 6 / 11; LOC passed with 2,022 files, zero exceptions, maximum
250 physical lines. No unchanged whole-proof subject was replayed for the
later narrow changes.

One source composition at `1fcfa05c` passed 1 / 73. Its positive G function
contains three calls: two quotients, one literal dividend and one count-derived
dividend. Fresh C7/C8 verification, assembly-required public C7 verification
and one closed entry pass, yielding `[12 2]` with empty stdout. The G
zero-divisor negative rejects with `STD5002`, Integer operands, proof-required
expectations and source/binding/span provenance; its fresh C7 verification is
asserted and printed passed. A secondary C7-ANNOTATION is retained. Negative
C8/runtime are not invoked. QST has byte/parser parity only; development C6
session construction/replay is not public fresh C6 verification.

The complete source prefix is
`/tmp/nonnegative-integer-quotient-source-lease-scope-corrected-20261021`:
original PTY 18640, natural exit zero, 13 unchanged inputs, selected time
139.044 seconds plus 13.182 seconds namespace loading, actual 2 GiB max heap.
The canonical FileLock and memory-heavy lease were acquired, with 22 ms broker
wait and finally release. Automated monitoring retained ten observations,
maximum gap 16.442 seconds; no operational bound was crossed or signal sent.
Postchecks observed all six lock files free and no Java/Clojure/Clang process;
these are snapshots, not hard OS containment. The earlier source command
remained unrun. The single main reconciliation with `9ee0b085` produced
`e0e01a25` and preserved all 11 owned byte sequences. Static selected-call
ownership review found no dependency on the changed SH22 UTF8/stdout owners;
no replay was needed. Public fresh C6, native, authoritative migration, whole
helpers, reader parameters, self-hosting and release remain unestablished.

### SHA-prefix comparison: existing behavior composition

The literal SHA-prefix expression now has combined development coverage:
original ASCII-71 `vec`, proven slice 0..7 and finite Character-Vector equality
produce true for the correct prefix and false for a same-length wrong prefix.
Existing production passes; this is new composition coverage, not a compiler fix.
Frozen `0ef4de19` passes 13 / 1,701, with four new selectors / 331 assertions
and nine existing controls. Nested proof/fact/ledger rejection, Never, eager
evaluation and allocation joins pass; LOC reports 2,022 / zero exceptions / 250.

One history-preserving join brought accepted C11/SH13 progress from `a31ffda2`
without changing owned or selected source-route bytes. At `cad84ea9`, the
one-shot source run produced `[true false]` with two conversions, two slices,
two equalities, fresh C7/C8 and public C7 assembly verification. The Map negative
correctly produced C7-TYPE-MISMATCH/:vector-equality-operand-type-required,
expected two-established-finite-vector-values, with fresh C7 verification passed.
The aggregate is FAILED 77P/7F/0E in 1 / 84 because the test's atomic-diagnostic
filter returned nil. The two-token oracle-only correction `5e139891` preserves
that failed history. A separate retained printed-diagnostic DATA check passes
1 / 30, including bounded wrong/missing/forged controls and the original raw-log
hash; it has zero fresh compiler/source invocations and grants no green source
selector credit. No unchanged producer was replayed. Detailed pre-test failure,
source failure, resource and reconciliation receipts remain external under
`/tmp/sha-prefix-comparison-*`; the default workflow is unchanged.

String-parameter/alias grounding, String count policy, full SHA helpers,
arbitrary receivers, public fresh C6, native, authoritative migration,
self-hosting and release remain outside this coverage.

### Proven Vector slice: development increment

The next dependency of the real SHA-prefix expression is ternary `subvec`
after its original finite ASCII String `vec`. STD3 now states the general
persistent Vector slice law, inclusive start/exclusive end, stored order,
inert payloads, unchanged input, empty/full cases, eager once-only operands,
transfers and possible allocation/storage obligations. The implementation
subset is narrower: an independently authenticated existing SCV receiver plus
two original nonnegative generic Integer literals on JVM/meta, within the
existing 1,024-width/65,536 logical-work bounds. It establishes
`0 <= start <= end <= receiver-count` before subtraction, positional result
types or host slicing. STD3002 denotes absent/out-of-range bounds evidence;
it is not a claim that every rejected program is mathematically invalid.

Twelve C7, eleven C8 and nine emitter private helpers extend the existing
owners. They reconstruct child SCV proof from actual binding/node/source,
unique call/edge and the canonical expected descriptor, then pay its actual
work again inside the parent. Both child and parent CF/producer rows survive
and are checked independently. Own source rows are unique and both levels'
ordinals must be physical nonnegative Integers. All four ordered children and
their original source occurrences join. Empty/full/prefix/middle/suffix,
generic BigInt indices, malformed metadata/source joins, child/parent missing
or duplicate evidence and selected opaque controls are covered. These are
bounded logical-work and selected callback tests, not universal host containment
or integer-precision/provider CPU/heap guarantees.

Any Never suppresses cross-operand ordering/upper bounds, subtraction,
result count/member construction and direct parent allocation. Every remaining
normal operand still owns its proof. A normal receiver conversion already
evaluated before a later transfer retains transitive allocation. Parent latent
allocation stays; every normal invocation, including empty/full, has possible
allocation and must fit declared allowance. Eager left-to-right lowering is
non-tail; throw and recur controls cover receiver and both indices, with
positive direct-tail recur controls. Unsupported child text/width and child
work failures retain their own diagnostic priority rather than becoming
STD3002. No IR key/op, context key, cache, cap, public host builtin, runtime
function, wrapper or lane was introduced; runtime remains 56 functions.

The runtime's old two-value extraction was insufficient for a ternary call.
It now extracts the actual evaluated end value, validates exact three-map
builtin shape and physical Symbol/keys, then actual bounded Vector and two
Integers plus closed nested vec/String/index-literal shapes before host
subvec. No conversion or child is reevaluated. Index conversion is exact in
the bounded range, without an implicit machine-width or check-elision claim.

Preserve the full failed development history in `/tmp/vector-slice-*`.
Test-only RED `c49456f9`, original PTY 19684, ran 1 / 63 with 49 failures.
Initial inference 22644 passed 1 / 63. C7 namespace compilation 78729 exited
2 before tests; syntax-corrected 4935 passed 2 / 213. Tightening own-row
uniqueness/physical census from two to three scans, transfer exact-work and
child-priority controls led to 62618 passing 3 / 237. Its failed child-work
prefix is 332, not the earlier revision's 328. Initial C8 3600 passed
1 / 237. Initial lowering 74343 ran 1 / 251 with two test-oracle failures
because selected-types [] was unchanged on legitimate empty results;
corrected 54344 passed 2 / 295 without production weakening. Scoped 87969
passed 1 / 23, measuring scope, owners and already-paid child work before the
parent's boundary. First aggregate 71013 stopped at 20 / 1,239 with zero
failures and one host test-construction error: array-map called the opaque
key's equality before Gravity. Corrected PersistentArrayMap construction
led to original 85765 passing all 50 selectors / 3,369 assertions at frozen
`52d6db10`, tree `5e9ce587`. Boundary passed 6 / 11; LOC passed 2,022
files, zero exceptions, maximum 250 physical lines.

The boundary and LOC starts 99736/99737 were SOURCE's brief default-heap JVMs,
overlapping SOURCE's first 2 GiB aggregate and D's 512 MiB RED. That exceeded
the standing two-Java limit. All ended naturally; later cheap JVM work was
serialized. Keep the actual overlap qualification; these ordinary receipts
do not confer protocol compliance or source-execution credit.

Both independent code/result reviews accepted `52d6db10` before further
work. The one actual main join `1f2cc242`, tree `a030c1f8`, preserves
parents `52d6db10` and `3af20721` and all 12 owned files. PRs 197/198
bring C11 and SH12/SH13 Bool-parameter/explicit-argument work; no slice source
owner changes. Entire host source, actual checked_core.gravity/resolution.gravity
and P15 compiler.gravity route bytes are unchanged. Trace is
00899 session -> 00898 authenticated/resolution -> 00894 structural request ->
00885 execution of named SH07 checked-core build/verify functions. The
C11-named host digest remains canonical-value hashing; compiler inventory data
does not invoke the changed C11 source on this method. This is concrete
dependency reconciliation, not blanket evidence reuse; no repeated 50-test
stamp or post-join source relocation was used.

The only successor before source execution adds the exact 185-line method:
`f2490680`, tree `0527a021`. Its form SHA256 is
543d655e057850858e0e1d75f4944b452052a1c68c440816d09282e739140b40.
Original namespace-only PTY 69944/natural zero used 512 MiB and executed no
test. Both finite method/resource reviews accepted before the distinct
one-shot GO. Original PTY 98034 then exited naturally zero with 1 / 68 passing
assertions, no retry/fallback/inner run/repair or cap growth. One positive G
function has one original ASCII-71 conversion and one slice 0..7, with exact
child/parent descriptors, call facts and rows, ordered effects/allocation,
fresh C7/C8 verification, assembly-required public C7 verification once, and
one closed entry returning [\s \h \a \2 \5 \6 \:] with empty stdout.
The end-72 G negative rejects STD3002/:vector-slice-bounds-required with the
71-Character Vector and two Integer actual types, expected bounds and
Core/syntax/binding/span provenance. Fresh negative C7 verification was asserted
and printed passed, retaining the secondary C7-ANNOTATION. Negative C8/runtime
were not invoked; QST is parser-only with zero typed products.

Both final actual/resource reviews and the coordinator accepted this exact
scoped receipt. The one-shot boundary is closed; no proof was replayed.

Preserve the full `/tmp/vector-slice-source-lease-20261021` bundle, plus
source dependencies, monitor plan, launch eligibility and namespace receipts.
Fifteen regular non-symlink input pins plus eleven route pins, exact command,
argv/CWD and actual launch environment remained unchanged. The observer
shell's different PATH was not the launcher: the recipient launch context
actually matched pinned PATH 77f656... and eight unset variables before and
after. Immutable input bytes retain their earlier unlisted status; the later
CREATE_NEW namespace receipt qualifies that without rewriting history.
An incorrect inline manifest hash was immediately withdrawn; the actual
immutable input-manifest SHA is
e5d30c63e03a79554a0f4c7a16d2dcd3355051fb003e977cb123e76dfdcb16d8.
Earlier manifest bytes remain in original tool output; no predecessor file or
timestamp was fabricated.
Executable-string SHA fd4296... excludes the final newline; the 1,463-byte
command-file SHA 45841a... includes it. Broker wait was 27 ms and release was
observed, using the canonical lock and existing memory-heavy lease. Actual
heap was 2 GiB; operational limits were 600 seconds, 8 MiB log and 4 KiB
observed tails with a 15-second target/30-second maximum planned gap. Ten
observations had maximum gap 17.474 seconds; monitor elapsed 152.103 seconds.
Selected execution was 137.352176875 seconds, runner 137.354308459 seconds
and namespace loading 13.547703292 seconds; these are inclusive development
intervals, not provider CPU or speedup benchmarks. Final log was 12,538 bytes
(SHA a0b25471426566429ec84bc798626e0cfee398012b76e4ab86a24e89ddc0005c),
EDN 1,543 bytes (SHA cecfc74f478eedb7d45a378060b902a1330be11b8a81e12c81dfb309f20cc96c).
All six native fcntl availability postchecks were free and selected
Java/Clojure/Clang processes absent at snapshot, with no hard OS/RSS/descendant
guarantee. The earlier ordinary JVM overlap does not qualify this separate,
exclusive one-shot resource record.

No public fresh C6, original String-parameter/alias grounding, arbitrary Vector
receiver support, complete SHA helper, whole compiler consumer, native,
authoritative admission/migration, self-hosting or release follows.

### ASCII literal String vector conversion: development increment

The real SHA-prefix client in checked_core.gravity and resolution.gravity uses
`(= (subvec (vec value) 0 7) [\s \h \a \2 \5 \6 \:])`.
Finite Character-Vector equality was already implemented. Unary String `vec`
is an earlier missing dependency. STD4 now states its general scalar-sequence
law: one Character per Unicode scalar in original order, empty to empty,
without normalization, coercion or supplementary-scalar splitting, retaining
ordinary evaluation, transfers and possible allocation/storage policy.

The implemented subset is original finite ASCII literals only, not a claim
about the client's parameter. The current seed interface supplies no general
scalar-code extractor; an explicit quoted NUL-through-DEL Character domain
avoids adding a host builtin or treating surrogate units as valid Characters.
Physical String/source widths and IDs precede walks and comparisons. Complete
128-domain validation precedes result types or conversion; the existing
1,024-width/65,536-work bounds remain. Unsupported non-ASCII, supplementary
and malformed host text reject C7-ANNOTATION/:string-character-vector-unsupported-text
without an invalid-Unicode claim. No alias, flow-refinement, parameter, subvec,
generic-sequence or whole-SHA grounding follows.

Each existing C7, C8 and emitter consumer reconstructs the original literal,
resolved binding, ordered call/edge and source joins, complete positional
Character result, unique call facts and ledger. Candidate metadata is compared
against the independently derived descriptor. Normal direct/latent allocation
and pending representation/provider/ownership survive even for empty input;
Never preserves the transfer with no normal result extent or direct result
allocation. The runtime checks the actual String and original closed literal
shape, then validates all units before the existing conversion. Instrumented
controls observe no conversion for unsupported/overwidth input. Runtime remains
56 functions, with all nine protected carrier predicates unchanged. There is
no new runtime function, IR key/op, wrapper, bridge, context key or cap. These
are logical work and selected callback checks, not universal host/FFI containment.

Retain the failed history. Original broader-BMP inference RED at `4a5a6192`
was 1 / 36 with 32 failures; the separate ASCII revision also recorded 1 / 36
with 32 failures before support. Initial ASCII inference then passed 1 / 36.
One C7 namespace compilation and one SH01 namespace compilation exited 2 before
tests. The first C7 proof test ran 1 / 108 with five oracle failures because
it looked for nested diagnostics on a direct helper result; the corrected
proof/scoped batch passed 2 / 131. First C8 passed 1 / 104; corrected-syntax
lowering/runtime/transfer passed 3 / 176. Draft-only syntax repairs have no
source-execution credit. Raw prefixes remain `/tmp/literal-string-vector-*`
and `/tmp/ascii-string-vector-*`.

The coalesced earlier candidate `88e67a9d` passed 39 / 2,420: nine new
selectors, nine quotient, nine scaling, five concatenation, four shared scoped
and three Set conversion controls. Boundary passed 6 / 11; LOC passed for 2,022
files, zero exceptions, maximum 250 lines. Review found one actual metadata
closure gap: each new source helper copied the edge ordinal without checking
its physical Integer/nonnegative domain. Coherent nil/negative/opaque ordinal
controls reproduced 12 failures in 3 / 231 at test-only `1468eef2`. The one
grouped correction `e642fb32` guarded all three owners and passed 14 / 579,
including ten ASCII selectors and four scoped controls. The earlier 39-test
pass is not a post-correction replay.

The exact 168-line source method at `e22f9f88` was namespace-listed without
invocation, then ran once under the reviewed lease. Its form has 55 static
assertion forms; execution counted 61 actual assertions: 1 / 61 passed, original
PTY 57427/natural zero. One positive G function/call reached fresh C7/C8,
assembly-required public C7 verification and one closed entry returning [\s \h \a \2 \5 \6 \:]
with empty stdout. The valid non-ASCII G negative retained String type, ASCII
expectation and Core/syntax/binding/span provenance; fresh negative C7
verification was asserted and printed passed, with secondary C7-ANNOTATION
retained. Negative C8/runtime were not invoked and QST remains parser-only.

Preserve `/tmp/ascii-string-vector-source-lease-20261021`: 15 unchanged
inputs including STD4 and C6 projection plus matching command/argv-env/CWD,
actual 2 GiB heap, 132.879 seconds selected time and 12.919 seconds namespace
loading. The 148.220-second monitor interval includes dispatch/poll completion,
not provider CPU benchmarking. Ten observations had maximum gap 16.370 seconds;
no limit crossing, signal, retry or bound growth occurred. Broker wait was
20 ms and finally release was observed. Six free-lock/no-Java/Clojure/Clang
postcheck observations are snapshots, without hard OS/RSS/descendant containment.
Source stayed `e22f9f88` on `b29ae6c8` while main advanced to `6617b67f`.
The one history-preserving join `990ce8c8` retained all 12 producer/method file
bytes. Direct trace follows SH07 session/from-resolution/request/structural
execution to checked_core.gravity and named SH07 build/verify functions, not
the changed private C6-C10 pipeline. Its actual compiler/emitter, source route
and projection are unchanged. The private C6 old zero-arity constructor is
byte-identical after its name change and old hosted export precedence survives.
That separate producer now supports one source-derived Bool parameter in its
private Core family; existing MIR/runtime still reject that family. It does not
ground the SHA String parameter. Post-join namespace listing checked combined
load/discovery only; no fresh source proof was run or blanket validity claimed.
Public fresh C6, full Unicode conversion, parameter/alias grounding, subvec,
complete helpers, native, authoritative migration, self-hosting and release
remain unestablished.

### Count-bounded quotient scaling: development increment

The emitter's actual `(* 2 (quot (count bindings) 4))` subexpression motivates
a relational numeric slice under SAFE9/STD5/L5. The existing C7, C8, typed emitter
and runtime owners admit `(* k (quot (count collection) d))` for original generic
Integer literals `0 <= k <= d`, with `d > 0` and an independently authenticated
normal count/quotient. From the Euclidean `d*q <= n` premise, derive
`0 <= k*q <= d*q <= n`. This general sufficient relation avoids a special
2/4 gate; `q <= n` alone never justifies multiplication. Unsupported proofs
reject with `STD5001`, including mathematically defined products outside the
subset. The inner quotient retains its actual zero-divisor `STD5002` rule.

The new numeric ledger, call facts and source proof retain mode
`:proof-required`, outcome `:proven-safe`, coefficient/divisor and count-law
premises and invalidation dependencies. Each consumer independently reconstructs
source, types, ordered call/edge/binding joins and unique quotient/count
facts and ledger rows with nested work paid before those walks. Normal
results may allocate Integer storage; direct/latent allocation effects and C8
records survive with representation/provider/ownership obligations. Never
preserves each normal operand proof, with no cross-operand bound or direct
result allocation claimed after abort. There is no inferred width, narrowing,
alias, private-parameter range, commuted route or whole-emitter/reader grounding.

The existing runtime builtin-call shape retains only evaluated `[k q]`.
It checks the original fixed nested count/quotient instruction, literal `k`
and positive `d`, coefficient relation and actual Integer/nonnegative values.
The nested operations must already have succeeded; there is no retained `n`,
fresh count comparison, re-evaluation or hidden cache. Source proof remains
mandatory. Two physical Symbol checks in the existing evaluator also close the
observed malformed-function metadata callback path before argument work.
No new runtime function, IR key/op, namespace, wrapper, context key or cap is
added. Runtime inventory is 56; all nine protected carrier predicates match
base `5344a5ec`. Logical Integer units establish no precision/CPU/heap bound;
selected callback tests do not authenticate arbitrary host/FFI providers.

Failed history is retained. The literal inference RED was 1 / 54 with 42
failures. The first proof batch was 2 / 217 with one wrong inert-source-ID
expectation. The first C8 batch was 1 / 105 with five missing ledger-dispatch
failures; its corrected batch passed 1 / 116. First lowering/runtime was
2 / 237 with one malformed-function callback error and its counter failure;
the Symbol guards produced 2 / 237 green. The C8 Never extension recorded
1 / 207 with three unchanged-effect test forgeries; the oracle now changes
normal and aborted effects distinctly. One pre-test namespace compilation
exited 2 before tests. The following 5 / 339 recorded one missing-leaf-work
oracle error, one actual retained-child-ID comparison callback and six
follow-on counter failures. Fixed selected IDs now precede proof retention;
missing leaf work uses the existing zero default. The corrected partition
passed 5 / 340. All raw prefixes remain `/tmp/count-quotient-scaling-*`.

The final coalesced batch at `9b19fae5` passed 39 / 4,074: nine new selectors,
nine prior quotient selectors, 17 other controls and four shared C7 scoped
consumers. Boundary passed 6 / 11; LOC passed for 2,022 files, zero exceptions,
maximum 250 lines. The single main join with `f9eab59d` produced `f88b989d`
and preserved all 11 reviewed implementation/test/fixture byte sequences.
The joined C6 projection is pinned as a fourteenth source dependency; its
separate output-parity evidence is not blanket old-proof validity. The new
180-line source method was namespace-listed without execution before its GO.

One source invocation at `e5117b9d` passed 1 / 68, separate from the 39-test
batch. One G function with scaling/quotient/count calls reached fresh C7/C8
verification, assembly-required public C7 verification and one closed entry,
returning `4` with empty stdout. The k=3,d=2,count=8 G negative rejected with
`STD5001`, Integer operand types, proof-required expectation and provenance.
Fresh negative C7 verification was asserted and printed passed; secondary
C7-ANNOTATION is retained. Negative C8/runtime were not invoked; QST is parser-only.

Preserve `/tmp/count-quotient-scaling-source-lease-20261021`: original PTY
94074, natural exit zero, 14 unchanged inputs and matching argv/env/CWD pins,
actual 2 GiB heap, 139.176 seconds selected time plus 13.151 seconds namespace
loading. Eleven monitored observations had maximum gap 16.409 seconds; no
operational limit crossed or signal was sent. Broker wait was 19 ms and finally
release was observed. Six free-lock and no-Java/Clojure/Clang postcheck
observations are snapshots, without hard OS/RSS/descendant containment claims.
Public fresh C6, complete helpers, parameter/alias grounding, native,
authoritative migration, self-hosting and release remain unestablished.

### Finite Symbol-Set membership: development increment

The existing C7, effects, typed emitter and runtime owners implement binary
membership for independently finite literal Symbol Sets and a Symbol needle,
including empty Sets and the bounded Never transfers. Original namespace/name
values determine equality. Display units only account work. Full domain,
uniqueness, source/policy/constraint and ordered-ledger checks precede results;
runtime remains at 56 functions. There is no new grammar, export or builtin,
Symbol-Set equality/insertion, homogeneous carrier or recursive transport.

Focused coverage is 32 distinct selectors and 3,713 passing assertions across
three qualified revisions, not one same-revision batch: 15 retained/1,595 at
`0252cece`, 12 retained/1,609 at `bc69b988`, and five changed C8 selectors/509 at
`6dc46dfc`. The eleven new selectors contribute 1,242 assertions. Earlier
focused failure `50656` recorded 2,123 passes/one failure across 19 tests and
13 skipped selectors: a known legacy-family needle triggered the new Symbol
receiver probe. The production early-needle guard repaired that behavior;
the old one-read oracle was not relaxed.

The first source attempt `7042` at `8c470986` failed with four passes, one
failure and one error before C8 verification, assembly, entry or the negative
subject. A new whole-ledger guard rejected a legitimate ordinary local-call
row lacking a selected producer. The narrow C8 correction preserves the full
selected Symbol-row census. Its first focused attempt `16648` failed loading
unsupported `nil?`, with one error, zero passes and four unrun selectors.
Using supported constant-left `(= nil producer)` at `6dc46dfc` passed all five
affected selectors/509 assertions. All failed results remain separate.

The changed source composition then naturally passed one test/125 assertions:
119 composition checks and six runtime-loader checks. One Gravity positive
and one typing negative use fresh C5, development C6 sessions and same-catalog
replay, then C7 construction/verification. The positive also reaches independent
C8 construction/verification, an observed public C7 assembly gate and one
closed runtime entry. Two functions, seven calls, six queries, one local call
and twelve quotes produce `[false true true false false true]` with empty
stdout. Membership nodes and the local producer are pure; the main function's
Vector allocation remains declared, inferred and latent. The Character-Set
with Symbol needle rejects at C7 using the existing Keyword-family fallback
diagnostic, with exact actual types, Core/syntax/binding/span and the secondary
annotation diagnostic retained. Negative C8/runtime are not invoked.

Execution took 144.415 seconds after 12.565 seconds of namespace loading at
2 GiB. Nested product construction and replay are included; these are not
phase-isolated runtime benchmarks. The scheduling deadline is not containment
or a lease. QST checks are parser-only; there is no seed reference entry,
fresh public C6 verification, whole compiler-consumer execution, native result,
renewed admission, SH-07 completion or self-hosting claim.

### Flat Generic Integer Vector comparison: development increment

The existing C7, C8, emitter and runtime Vector comparison owners support flat
Vectors containing Generic Integer leaves with `=` and its `not=` complement.
Corresponding Integer positions must both be Integer. Both complete domains
and overlapping type compatibility are checked before length/value mismatch.
The comparison reuses original-value scalar Integer equality without numeric
conversion, widening or arithmetic. Legacy recursive nonnumeric descriptors,
identities and work remain unchanged; new Integer cases use a distinct policy.

The first focused run is retained as FAILED: 21 tests, 6,444 passes, two
failures and no errors. Both failures were a new equality transfer test's
incorrect zero default for failed-child work; the existing equality owner uses
one. A one-line test-only correction passed the affected test's 32 assertions.
The other twenty tests retain 6,414 passing assertions without replay. Thus
nonduplicated focused coverage is 21 tests/6,446 passing assertions across
runs, not a clean full-batch rerun. Production bytes did not change to fix the
test. Controls cover full domains, version/source/fact/ledger tampering, exact
and short work, measured owning consumers, runtime values and transfer fences.

The paired owning-C2 parser selection passed one test/11 assertions. The one
actual changed-source composition passed one test/103 assertions at 2 GiB:
148.082 seconds selected execution after 11.745 seconds namespace loading.
One function returned `[true false false true]` from count-produced
Integer/Keyword Vector equality and its natural inequality complements.
Original C7/C8 verification and an additional observed public C7 assembly gate
passed. Eight source calls produce two equality and two source-inequality
policies; thirteen Vector constructors retain their allocation effects while
the comparison queries have no direct allocation effect. Runtime still has
54 functions; one main entry ran, with no seed control execution.

The nested-Integer inequality negative passed C5 and C6 development checks,
then rejected in C7 with `C7-TYPE-MISMATCH` and
`:atomic-inequality-operand-type-required`. The unresolved-node annotation
diagnostic is also retained; the assertion establishes the primary diagnostic
and nested type, not an exact-only diagnostic vector. That negative has no
C8/lowering acceptance claim. Q remains parser-only. Fresh C6 development
sessions and same-catalog replay are not fresh public C6 verification. Inclusive
timings are diagnostic observations, not phase-isolated performance evidence.
Separate language-boundary checks passed six tests/11 assertions; LOC validation
passed 2,021 source files, zero exceptions, maximum 250 physical lines.

Nested numeric Vectors, fixed-width numeric leaves, Float/Decimal/ratio,
Nil-or-Integer, unknown members and Set-valued leaves remain unsupported.
Existing width/depth/work limits are unchanged; structural work units do not
establish Integer digit-size, provider CPU or peak-memory containment. This is
development progress, not whole C6 validator execution, authoritative admission,
SH-07 completion, self-hosting or release evidence.

### Flat Vector comparison with Keyword-Set leaves: development increment

The existing C7, C8, emitter and runtime Vector comparison owners compose
finite Keyword-Set value equality into flat Vector `=` and `not=`. Both
Vectors must be flat when either has a Set leaf, including an empty Set.
Corresponding Sets require independently established finite all-Keyword types;
corresponding Integers retain Generic Integer equality. Set-only and
Set-plus-Integer cases use a new policy while prior recursive nonnumeric and
flat Integer no-Set versions, identities and work remain unchanged. The new
Set-leaf case retains the existing Set equality JVM restriction.

Runtime fully validates every Set leaf by owned self-equality before any
Vector length/value mismatch. Actual corresponding Set comparison invokes the
owned helper again. Every invocation gets the current remaining aggregate
Vector allowance, and its reported reservation is charged once. No per-leaf
reset, acceptance cache, hash/identity shortcut or new helper/export/opcode
was added. Failed reservation lower bounds are not executed-step counts or
provider CPU/peak-memory guarantees. Nested quota reasons map to the Vector
category while the containing C7 inequality verifier retains its atomic category.

The first frozen focused run passed 19 tests/3,597 assertions with no failures
or errors: 1,630 new and 1,967 adjacent assertions. It covers empty Set policy,
Set-only/Integer policy joins, source/fact/ledger tampering, target restrictions,
both transfer fences, exact/short budgets and old versions. Later malformed
members and duplicate Keyword value classes reject before an earlier mismatch.
Separate aggregate tests exhaust domain validation and actual pair comparison;
the measured owning consumer has one child measurement and no unrelated probes.

The paired owning-C2 parser selection passed one test/eight assertions. One
changed-source composition passed one test/145 assertions (139 composition
checks plus six existing runtime-loader checks) in 156.019 seconds selected
execution after 12.147 seconds namespace loading at 2 GiB. The single function
returned `[true false false true]` for reordered/equal and changed-member Sets
through equality and its natural complements. Original C7/C8 verification and
an additional observed public C7 assembly gate passed. Eight source calls,
four comparison policies, thirteen Vector constructors, eight Set constructors
and sixteen Set-member Keywords matched their current source/type/IR records.
All twenty-one constructor allocations remain distinct from comparison query
direct-none effects. Runtime still has 54 functions; one main entry ran, with
zero seed control executions.

The finite String-Set-leaf negative passed C5 and C6 development checks, then
rejected in C7 with `C7-TYPE-MISMATCH` and
`:vector-equality-operand-type-required`. Its full type and secondary unresolved
annotation diagnostic are retained; the assertion establishes primary presence
and type, not an exact-only diagnostic vector. It has no accepted C8/lowering
claim. Q is parser-only. Fresh C6 development sessions and same-catalog replay
are not fresh public C6 verification. These inclusive timings are diagnostic
observations, not phase-isolated performance evidence. No failed source epoch
or replay occurred in this increment.
Separate language-boundary checks passed six tests/11 assertions; LOC validation
passed 2,021 source files, zero exceptions, maximum 250 physical lines.

Bare, mixed, String or numeric Set domains and nested Set-containing Vectors
remain unsupported. This increment does not infer finite Keyword types from
`set(keys actual)`, execute unchanged List-compatibility/C6/C17 helpers, admit
the current compiler closure or close SH-07, self-hosting or release gates.

### Compiler-module literal String/Keyword keys: development increment

The temporary module constructor now accepts distinct literal String or
Keyword keys, including mixed evaluated Maps. This removes its incidental
Keyword-only restriction, not the separate canonical typed-Gravity Map
frontier. The concrete subject is the unchanged W5 trust diagnostic catalog
and its literal diagnostic-ID dependency, retained in a clearly labelled
fixture namespace. This is not construction of the whole W5 module.
This supersedes the Keyword-only module boundary in the Map-order and
Map-value entries; their historical validation results remain unchanged.

The existing `00575` source-order component extends both its C2 scalar
kind/value fence and actual-C5 reference projection to accept the two literal
families. The complete child census, distinct key/host-slot bijection,
original reference identities and source-ordered routing remain. String
`":tag"` is not Keyword `:tag`; no spelling coercion or ordering authority is
introduced. Missing or collapsed occurrences reject. The public diagnostic
remains `BOOT2006`, with the more accurate local reason
`:map-source-distinct-literal-key-bijection`.

The first focused run passed three tests / 84 assertions: ten fixture parity
and unchanged-subject checks, 54 reference/order checks and 20 rejection
checks. Supplied-record tests cover hash/array/String-sorted representations,
mixed keys, supplementary values, C2 kind/value mismatches, occurrence loss
and unsupported key kinds. Decoded-equivalent duplicate rows are supplied
DATA controls, not claims of a fresh reader execution. Raw UTF-8 supplementary
Strings in paired Gravity fixtures exercise the existing scalar source path;
Clojure tooling uses ASCII escape spellings.

The first source attempt is retained as an error, not a passing composition:
one test, zero passes and one error after 92.777 seconds of execution and
6.772 seconds of loading. Leaf/root C5 construction completed, but the first
leaf bundle stopped at `BOOT2006/:map-source-order-mismatch` before semantic
assertions, root assembly, runtime or negative controls. Verified C2/SH05
materialization and independent seed parsing had equal small Map values with
different host entry layouts. Source-order context must match the actual seed
layout without replacing original C5 reference authority or weakening the
strict key/slot join. No unchanged source candidate was replayed.

The correction changes one existing `00580` module-source line: select the
independent macro forms for seed definitions after the unchanged complete
equality check against verified forms. Original syntax, C5 request/reference
identities, declaration bindings and strict context checks remain intact.
The focused handoff regression uses a tiny genuine C5 input, fresh original
module-source derivation and raw compiler; it is not public bundle or runtime
evidence. It passed one test / eight assertions at 512 MiB, including equal
Map contents with different physical entry order, actual seed slots,
source-ordered compiled entries and strict wrong-layout rejection. It took
31.346 seconds after 6.692 seconds of loading; these observations are inclusive
development costs, not benchmark or speedup evidence.

The changed implementation then passed the full affected source composition:
one test / 49 assertions, with all five G C5 attempts, four public bundle
construction calls and two runtime calls complete. Twelve distinct original
reference routes cover imported String-key values through let, loop/recur and
Vector contexts. Runtime lookup observes the catalog's schema remediation and
non-authority entry without claiming Map enumeration order or whole-W5
eligibility. Output confirms source-order evaluation once. A correctly
resealed reference substitution rejects before output; private imports reject
at C5; source-first arity two rejects before later arity three; a numeric key
passes C5 then rejects at the owning module-constructor boundary. Q remains
byte/parser-only. Source execution took 275.722 seconds after 6.797 seconds of
loading at 2 GiB, below the monitored ten-minute ceiling. These inclusive
caller observations are not a phase-isolated benchmark or hard deadline.

The three passing new selections total five tests / 141 assertions, not one
batch. Separate adjacent Map-call, Map-order, module and language-boundary
checks passed 17 tests / 242 assertions at 512 MiB. LOC validation passed
2,021 source files, zero exceptions, maximum 250 lines. The failed first source
attempt is excluded from these passing totals; no unchanged source matrix was
rerun for a label refresh.

Computed, numeric, Symbol and collection keys remain unsupported by this
module subset, not newly illegal Gravity. Quote inertness, nested-function
and Set-call fences, local work/depth bounds, raw3/B51, runtime and public
interfaces remain unchanged. Source composition is a separate affected check;
neither focused records nor module routing establish checked effects,
authoritative migration, full SH-07, self-hosting or release.

### Compiler-module Map-value calls: development increment

The existing Map-order, module-source and module-bundle components extend the
module-development path to qualified imported calls in supported Map values.
Original C5 host enumeration, seed-host callsite coordinates and C2 source
evaluation order remain separate. A complete occurrence projection matches
all original references in the expanded function owner, including unqualified
ones, by ordinal, identity, position and spans. Keyword keys bridge the actual
C5 Map entries to seed-host slots; imported origin and arity checks then use
source order and the original resolved references. Repeated callee names are
not matched by frequency or a source-ordered zip.

The supported literal Keyword-key Maps compose with existing let, loop,
recur-argument and Vector paths. Quotes remain DATA; inherited Set and nested
function fences reject both inside and outside Maps. A missing derived context
does not authorize the new walker. Original public C5 verification, bindings,
exports, scope, profiles and targets remain authoritative for this constructor.
No new resolver, proof cache, public input, runtime or IR schema is added.
The new full-reference projection and traversal share a local 65,536-unit
allowance and depth-64 guard; unsupported expansion/header shapes reject.
These remain bounded implementation frontiers, not new L2 language rules or
a guarantee of unchanged capacity for every old module. Existing recur/throw
validation phase precedence is retained, not unified with all arity errors.

The first focused launch failed during bootstrap compilation before any test
or assertion ran, with no timing receipt. Its component called an existing
helper loaded later without a forward declaration. The one-line declaration
repair changed no semantics or load manifest. The corrected focused batch
passed all three tests / 81 assertions: six paired byte/parser checks, 59 complete
reference/coordinate controls and 16 fresh-context fence controls. This uses
supplied normalized DATA and the actual reference constructor, not a genuine
C5 acceptance claim. The initial load failure remains separately preserved.

The first genuine source composition completed all six Gravity C5 attempts,
one raw leaf plan, four explicit public bundle-construction calls and two
runtime calls with fresh reconstruction. It retained
ten distinct original reference routes, exact result and left-to-right output,
shadowing and quoted DATA. Private import rejection, source-first arity two
rather than later arity three, inherited Set/function constructor fences and
a coherently resealed reference substitution rejected before output all ran.
Q remains byte/parser-only, effects remain unchecked and development authority
remains unadmitted.

Its actual aggregate is FAILED: one test / 47 assertions, 46 passes and one
failure. The sole failure expected the filename directly on `:source-span`.
Static producer lineage shows that C3 and C5 retain the original span wrapper
with `:primary` and `:all`; the filename belongs under `:primary`. Existing
production forwarded that wrapper unchanged. The future test oracle was
corrected to the nested field, not the production diagnostic. The failed run
is preserved; it is not relabeled as a passing 47-assertion source test.
Completed semantic observations remain useful unchanged evidence, while the
corrected field lookup is checked separately with a cheap normalized diagnostic
consumer control rather than replaying six expensive C5 constructions. That
supplied-DATA control is not a fresh C5 proof or an actual re-execution of the
corrected source assertion.

Selected source execution took 313.923 seconds after 7.046 seconds of loading
at 2 GiB, below a monitored ten-minute ceiling. These are inclusive observations,
not a benchmark, exclusive phase costs, a hard deadline or a speedup claim.
No source or compiler production bytes changed for the oracle correction.

The corrected namespace passed four cheap tests / 89 assertions, including
eight original-reference/route diagnostic controls for exact wrapped-span
forwarding, nested primary path and related spans. Their origin stub checks
the supplied caller, import and callee before returning; it is conditional
test DATA, not an origin proof. The corrected source assertion compiled but
was not re-executed. Final source-free SH-01 module regressions passed four /
97, old Map-order controls three / 45, transitive routing one / 17, and language
boundary six / 11. These five completed selections total 18 tests / 259
assertions, all passing; they do not turn the retained source aggregate green.
LOC validation passed 2,021 files, zero exceptions, maximum 250 lines.

Direct commands are in the development-verification guide. Set/function
import contexts, general module initialization, checked user-call effects,
Effects token-bound fit, C9 source integration and authoritative migration
remain separate work. This bounded executable increment does not complete
SH-07, the three partial evidence groups, self-hosting or release gates.

### Hosted compiler-module construction: development increment

The existing module-development interface now accepts exactly `:meta/:jvm`
and `:hosted/:jvm` modules, with the caller and leaf dependency using the same
profile and target. The owning `00580-p15-s23-stage2-module-source.clj`
component derives declaration and imported-origin profile/target checks from
the authenticated module instead of assuming `:meta`. Ordinary C5 verification,
source, namespace, package, export and origin joins, fresh public bundle
reconstruction, and route validation before argument evaluation remain intact.
This removes an incidental development restriction; it does not implement a
new profile facade or change P13 compatibility rules.

The final focused batch passed eight tests and 195 assertions, including the
existing C5 policy kernel for target, capability and cross-profile diagnostics.
Normalized kernel inputs are not authentic module evidence. The single affected
real composition passed one test and 44 assertions: hosted caller/leaf pairs in
both source extensions and one existing meta/JVM pair. It preserves actual
profile facts, private root entry, same-name function routing, scalar values and
String object identity. Real private/missing imports and direct cross-profile
edges reject; stale source and rehashed plan/route substitutions also reject.
An earlier test-loading delimiter error has no semantic result and is not
counted as a passing batch.

Selected real execution took 476.823 seconds after 7.593 seconds of namespace
loading with a 2 GiB heap. This is an affected integration check, not a routine
edit prerequisite or an isolated measurement of verification phases. No old
full matrix, migration proof or authority-pin refresh was repeated. The module
bundle still records unchecked effects, development-unadmitted status and the
Clojure seed/interpreter boundary. Whole-runtime construction and extraction
into `gravity.runtime` remain next; checked imported-user legality, full hosted
runtime conformance, target support, authoritative migration and seed retirement
remain unproved.

### Whole-runtime source DATA: development increment

The actual `p15_s23/runtime.gravity` can now be constructed through the existing
raw compiler-artifact interface without selecting or rewriting its function
forms. Eighteen catalog maps now explicitly quote symbolic DATA, as required
by L2; the five already-literal maps and all 30 function forms are unchanged.
Pre-edit conservation checks preserve the 23 catalog values and exact function
names/arities. The compiler's construction-local literal table is checked
against those values; no catalog carrier or raw-plan field was added.

Two focused tests passed 50 assertions, including 29 in the new whole-runtime
check and 21 in the existing literal grammar check. One complete-file plan is
reused for nine public runtime invocations: main, normal completion, a real
loop backedge, a function-target swap/backedge preserving String identity,
throw/catch/rethrow, unknown-operation rejection and inert quoted DATA. Output
order and skipped later work remain observable. Removing the first catalog's
quote in an in-memory source variant rejects during initializer validation.
The new check took 0.142 seconds of selected execution; this is not a speedup
comparison with a different test or evidence of all 30 functions' conformance.

No C5/C7 products, old native proof or authoritative runtime binding were rebuilt.
The historical pinned adapter's source-byte and 22-definition fences remain
unchanged and stale for current source. Catalog representation compatibility
does not renew provider/grant claims, admit a runtime revision, prove whole
runtime conformance or retire the Clojure seed. The subsequent stage-owned
runtime increment below completes the call-closed `gravity.runtime` extraction
with thin compatibility entrypoints and real module composition.

### Stage-owned runtime evaluator: development implementation

The complete 22-function evaluator/activation closure now lives in
`bootstrap/gravity/src/gravity/runtime.gravity`, namespace `gravity.runtime`.
At that extraction checkpoint, all function forms and arities were preserved.
It exports only closed-plan
execution, closed-instruction evaluation, context evaluation and function
activation; the other 18 helpers remain private. The old runtime module keeps
23 unchanged catalog values, formatting/main and B51 helpers, plus four direct
qualified compatibility calls. Dependency direction is old root to leaf only.
The guide records the four APIs, DATA inputs/results, diagnostic ownership,
allocation/output boundary and direct test commands.

Existing SH01 and SH08 runtime loaders now compile the entire actual leaf file,
not selected or concatenated function forms. Focused checks passed 12 tests and
474 assertions, including throw/catch, loop and function backedges, original
payload identity, downstream handler behavior and module guards. A subsequent
comment-only correction removed obsolete pinned-entry instructions without
replaying those unchanged semantic checks.

The single genuine two-file composition passed one test and 53 assertions
against the final source. It constructs real ordinary C5 root/leaf artifacts
and an independent leaf plan, retains actual source/module/export/origin facts,
executes all four forwarders, and rejects private/missing imports, stale source,
rehashed independent plans and forged route origins. It took 1,429.233 seconds
of selected execution after 7.274 seconds of loading with a 2 GiB heap. That
total does not isolate construction, verification, hashing or execution cost;
the guide describes a bounded follow-up rather than another test wrapper.

This moves development implementation ownership, not authoritative stage
ownership. No host adapter, historical catalog principal, admission pin, stage
proof or TCB record was renewed. Source declarations are not provider grants;
output remains accumulated DATA. Full runtime/profile conformance, checked
imported-user legality, target ABI/lowering, authoritative migration,
self-hosting and seed retirement remain outstanding.

### Literal module DATA: development implementation

The L2/L3/C5/BOOT2 increment adds source-local literal DATA declarations to the
ordinary compiler-artifact module subset. Scalars, nested vector/map/set
initializers and exact quoted DATA are materialized before function bodies
compile. Unqualified local reads use existing literal instructions; parameters
and `let` bindings shadow module DATA. Quoted symbols, lists, nested quotes and
call-looking payloads remain inert. Actual C5 declarations retain their original
syntax, definition artifact, namespace/package, visibility and function/var
kind. Mixed exports preserve DATA vars rather than dropping them or converting
them into functions. DATA is not callable, and qualified/imported DATA reads,
arbitrary initializer calls, aliases, mutation and general initialization remain
unsupported, not forbidden by the language contract.

Early kernel/namespace compilation errors were corrected before source-backed
acceptance. The first real batch failed with `B1-METADATA` and
`:pinned-sh02-gravity-function-identity`, before any assertions; fail-fast left
the paired test unexecuted. A redundant literal inventory had been added to the
raw plan and existing function-semantic projection. The correction keeps the
table compile-local: actual source hashes and full C5/bundle reconstruction
bind unused declarations, while embedded literal instructions bind used values.
Unused DATA changes module identity without changing an otherwise identical
function projection. No authority pin or strict check was weakened or refreshed.
The failed result remains separate from the corrected passing evidence.

The corrected source batch passed two tests and 23 assertions. Paired modules
read different same-named catalogs, preserve nested DATA and lexical shadowing,
use private DATA locally, retain var exports, and reject non-callable DATA,
literal/export tampering and imported DATA reads. The actual existing C8 source
constructed a bundle retaining all six catalog exports and invoked its existing
quoted contract function without changing quoted-symbol output. This is C8
source compatibility, not evidence of effect analysis. Selected execution took
797.895 seconds for actual C8 and 328.845 seconds for the paired test, totaling
1,126.741 seconds after 6.736 seconds of namespace loading.

After that run, function-table validation was moved ahead of literal collection
to preserve the existing `L2-DUPLICATE-FUNCTION` diagnostic. This ordering-only
change leaves successful plans identical and was checked with focused tests,
not a replay of the expensive compositions. Six final tests passed 91 assertions,
including legacy `def`/`fn` construction, actual checked-core/C8 raw plans,
exact historical SH02 function/plan identities, used/unused DATA invalidation,
literal bounds, initializer and collision rejection, routing and dispatch
regressions. The language boundary passed six tests and 11 assertions. These
final batches total 14 test executions and 125 assertions; earlier overlapping
kernels are not added. JVMs were serialized with a 2 GiB heap.

No new loader resource, test lane, authority flag or raw-plan inventory was
introduced. The actual C8 namespace can now be the caller for the next
Gravity-owned body-analysis increment in `gravity.effects`; no parallel facade
was substituted. Effects, thrown-error completion, checked imported-user calls,
target support, authoritative migration, self-hosting and seedless release
remain outstanding.

### Local body-derived effects: development implementation

The C8/L6 increment moves body analysis into
`bootstrap/gravity/src/gravity/effects.gravity`. The actual existing
`gravity.compiler.c8-effect-checker-engine` exposes the three-argument
`sh09-build-authenticated-body-effect-result` and four-argument
`sh09-verify-authenticated-body-effect-result` as qualified forwarders to that
owner. Existing pure-result and compiler-read interfaces remain separate; no
caller purity flag, replacement C8 facade or accepted-proof cache is added.
The real compiler-artifact module bundle supplies the effects dependency and
retains all six existing C8 DATA catalog exports.

The local first-order subset consumes the actual C7 result, fresh verification
and existing binding containing the complete original request and canonical
core. It derives a complete node graph, function latent-effect table and
namespace summary across literals, collections, references, definitions,
`let`, `do`, `if`, inert quote, function construction, structural matches and
normally returning Bool guards. Acyclic local calls depend on their actual
callee body. Function creation does not execute its latent body. Conditional
bodies and guards contribute conservatively without becoming unconditional
evaluation children. Zero-call functions are included. Declarations remain
allowances: unused nonempty effect/capability declarations do not imply an
action or prevent a supported body from being inferred empty.

Graph coverage, types, source origins, function/body/call coordinates, local
bindings and ordering are joined to the retained core and typed identity.
The complete core preimage and derived graph participate in the new identity
meaning. This is not the older declaration-based pure adapter's identity scope;
the older identity path rejects the new body scope rather than claiming a
partial complete identity. Throws remain `[:pending-sh09]`.

The paired `.gravity`/`.qst` accepted fixture exercises local calls, collections,
inert quoted throw/unresolved DATA, conditionals and guarded matches; the
rejected pair diagnoses a non-Bool guard in C7. The actual composition passed
one test and 59 assertions with no failures or errors on the reviewed
`2ffa5262` effects-source snapshot. It freshly constructs C5/C6 and C7 products,
checks C7 and the current Gravity leaf, rejects coherent input/result mutations,
then constructs the actual C8/effects bundle and invokes the public C8
constructor and verifier for both source extensions. Selected execution took
2,496.702 seconds after 6.585 seconds of namespace loading with a 2 GiB heap.
This measures the combined workload, not phase dominance or a speedup.

After that run, four narrow corrections reject callable parameter/return type
shapes recursively, join the copied global ordering ledger to its identity
preimage, report the actual diagnostic function owner (or nil), and join call
profile/target facts to the callee. Successful fixture graphs and C8 forwarding
are unchanged. Five final body kernels passed 60 assertions, including ordinary
collection/list/quoted-DATA type positives and coherent negative substitutions.
The existing checked-core/SH02 identity kernel passed seven more assertions;
the language boundary passed six tests and 11 assertions. These final batches
and the explicitly attributed earlier composition total 13 test executions and
137 assertions. Earlier overlapping kernel runs are not added. The long run
did not execute the later guards; focused delta checks and static valid-path
review cover those corrections without repeating the expensive composition.

Transitive imported bodies, higher-order/callable value propagation, general recursion,
throw/try/control-transfer composition, general effect legality, provider and
grant validation, complete effect identities, targets, authoritative migration,
self-hosting and seedless release remain outstanding. Legacy imported type-only
input still rejects with `:imported-body-effects-pending`. Use the direct
`sh09-body-effect-test` kernels for semantic iteration and the affected
composition rules in `development-verification.md`; no new lane or wrapper is
needed.

### Direct imported body-derived effects: development implementation

The L3/L6/C7/C8 increment extends the same `gravity.effects` constructor and
fresh verifier to direct imported scalar functions. It consumes the caller's
actual C7 identity facts and complete original four-key request, including
`module-type-inputs`, through the existing binding. Each retained producer C5
artifact, original public export, projected definition, function and body must
join exactly. Producer facts are the actual flat C7 producer identity, not a
fabricated accepted typed-result/verification wrapper. The older compiler-read
interface still requires exactly three request keys, and legacy type-only C8
adapters still reject pending imported bodies.

The same algorithm derives every producer node and local callee body, including
private functions, once per distinct exact producer request within the
invocation. Producer graphs stay indexed by their C5/C6 module coordinates;
caller coverage includes exactly caller nodes. Foreign dependencies carry the
caller import binding, both producer artifact IDs, projected producer definition
binding, function and body. Matching raw node/function IDs in different modules
cannot replace this coordinate. Full producer core/type identities and derived
graphs enter the new effect identity. A call propagates the derived body summary,
not a declaration allowance. Aggregate limits remain 65,536 nodes and 256
functions before graph indexing; one 65,536-attempt deferred-node budget is
shared across producers and caller. These units are not an all-CPU guarantee.

Eight direct Gravity kernels passed 112 assertions for origin/export/body joins,
local compatibility, derived-versus-declared summaries, scoped graph separation,
malformed tables, aggregate limits and deferred work. Five boundary tests passed
48 assertions for unchanged public C8 forwarding, module routes, B51 separation,
historical literal semantic pins and repository language policy. One test-file
compilation error was corrected before actual composition; the later four-test,
56-assertion load/recheck overlaps the eight kernels and is not added again.

The existing `inferred-values`/`inferred-consumer` `.gravity`/`.qst` fixtures
then passed the actual paired composition on its first attempt: one test and
69 assertions, no failures or errors. It constructs real producer/caller C5/C6
products, freshly constructs and verifies caller C7, seals the full request,
and executes the current Gravity effects constructor and fresh verifier.
Complete graphs retain the `integer-chain` call to private `integer-base`,
four imported body dependencies, empty inferred effects and pending throws.
Malformed import tables, coherent origin/body/coordinate substitutions, missing
producer requests and stripped graph/identity/throw evidence reject. Selected
execution took 548.399 seconds after 6.931 seconds of namespace loading with a
2 GiB heap. The three nonoverlapping batches total 14 executions/229 assertions;
earlier overlapping kernels are not added.

Actual C8 source, exports, requires and public three-/four-argument forwarding
forms are unchanged. Their static checks and existing route regressions are
separate from this current-source upstream-to-leaf composition. The expensive
public C8 bundle was not rebuilt: previous route evidence is not a new bundle
identity proof. No new fixture, host implementation, test lane, wrapper,
authority flag, manifest resource or accepted-proof cache was introduced.
Transitive imports, callable values, general recursive propagation, completed throws,
effect/provider/grant legality, targets, authoritative migration, self-hosting
and seedless release remain outside this increment.

### Bounded recursive body effects: development implementation

The L2/L5/L6/C7/C8 increment extends `gravity.effects` to C7's existing one
named, capture-free, positive-fixed-arity self-recursive component. The existing
positional-parameter call, primitive literal base and external parameter evidence
must join the original component, core, function, definition, call facts and
source coordinates. Positive arity means parameter count. Ordinary `if` retains
L2 truthiness; the Bool-only match-guard policy is unchanged. The actual C7
recursive identity, typed-core fields and verification checks are preserved.
Direct imported producers use the same per-module analysis, with their original
flat recursive C7 facts and separate module coordinates.

The solver classifies the complete body, starts function summaries at the
empty set, and iterates within the shared 65,536 node-attempt budget. Only the
authenticated self-body dependency is cut internally during a round. The final
graph retains the backedge and conditional region, even beneath literal false,
and checks the complete graph equations. Fresh verification independently
reconstructs from bottom: an inflated fixed point that satisfies the equations
still rejects. Classification is budget-checked before traversal; iteration,
final reconstruction and equation visits retain the same caller/producer work
counter. This is not an all-CPU bound or a program-termination proof.

The final cheap batch passed 11 tests/155 assertions covering existing local
and imported behavior, recursive proof joins, retained edges, leastness,
ordinary truthy conditions, malformed call-fact rejection, exact work-limit
boundaries and unchanged public forwarding. Four separate route/language tests
passed 44 assertions. Pre-composition independent review found an incidental
Bool restriction on `if`, an unchecked call-fact lookup, and classification
before the remaining-budget check. All were corrected and checked before the
first real composition attempt. One earlier test-file parenthesis compilation
failure and overlapping cheap batches remain historical, not extra coverage.

The real paired composition then passed on its first attempt: one test,
99 assertions, no failures or errors. It constructs and freshly verifies actual
C5/C6/C7 products for the existing Integer and String recursive fixture pairs,
then constructs and freshly verifies current-source body effects. A new paired
caller imports both producers through the ordinary module-input interface.
The existing SH-07 mutual-recursion pair still receives C7's
`:mutual-recursion-unsupported` diagnostic. Coherent proof/call-fact changes,
nonleast effect graphs, erased throws and changed imported proofs reject.
Paired effect identities agree. Selected execution took 1367.254 seconds after
6.879 seconds of namespace loading with a 2 GiB heap; no phase-cost or speedup
claim follows. The nonoverlapping batches total 16 executions/298 assertions.

Production, test and fixture bytes stayed frozen throughout that run. C8's
requires, exports and public three-/four-argument forwarders remain unchanged;
the old expensive public-bundle test was not repeated or claimed as a proof of
the current bundle identity. No new compiler API, host implementation, lane,
wrapper, authority flag, manifest resource or accepted-proof cache was added.
This closes only the bounded recursive body-analysis subset. General or mutual
recursion support, callable recursion, termination, recursion-depth bounds,
tail-call optimization, thrown-error completion, full legality, target execution,
authoritative migration, self-hosting and seedless release remain outstanding.

The existing `body-effect-recursive-fixed-point-development-kernel` now measures
actual helper entries while always delegating to their original runtime
implementation. The unchanged tiny fixture passed one test/28 assertions at
512 MiB: two passes each rebuild two edge rows and one function row, make 11
readiness attempts with zero false results, and repeat 13 invocation predicates
plus 11 support checks and 11 order checks. General/import call validators are unvisited.
Full observed/unobserved results agree; work remains 44, exact 44 accepts and
43 rejects unchanged. No large-source attribution, speedup, source proof or
milestone credit follows. A bounded production proposal must queue with the
current `effects.gravity` source writer; no implementation is selected here.

### Capture-free callable body effects: development implementation

The L3/L5/L6/C7/C8 increment extends the same `gravity.effects` interface to
C7's existing named identity/apply-one proof. The exact identity definition,
apply definition, callable parameter, value argument, nested function type,
outer selection, inner invocation and original call facts must agree. Only
the proof-selected parameter may carry that callable type. The inner C6 call
retains its nonlocal classification and nil direct callee; a separate
proof-bound selected-body dependency identifies the invoked identity body.
Constructing or carrying a function value has no immediate body effect;
invoking it propagates the derived body summary, not declaration allowances.

Direct scalar imports retain the complete producer graph and original module
coordinates. The real producer fixture exports both identity and apply; the
new paired caller selects only identity. Thus the executable example covers
an unselected apply body, not a private apply fixture. Selected imported APIs
remain first-order. The same aggregate node/function limits and shared
65,536-attempt work counter apply. Throws and legality remain pending.

The final direct kernel batch passed 13 tests/190 assertions, and four separate
route/language checks passed 44 assertions. A later identical kernel recheck
after the diagnostic-only test correction overlaps that batch and is not extra
coverage. Pre-composition review added the selected-import first-order fence
and its coherent nested-callable control before any real composition ran.

The first actual composition completed all local callable and scalar-import
positive pairs, fresh verification, full node coverage and coherent proof,
type, edge, call-fact, selected-body, summary and throw mutations. It also
completed both nonfunction and capture rejection pairs. Its aggregate result
was FAILED: 76 passing assertions (66 positive-composition assertions and ten
negative-case assertions), two failing assertions and no errors.
Both failures were the test's hardcoded `C7-ANNOTATION` expectation for capture;
current C7 instead rejects it earlier with
`C7-TYPE-MISMATCH` / `:function-result-type-mismatch`. Nonfunction calls reject
with `C7-ANNOTATION` / `:unsupported-nonlocal-call`. Capture rejection here does
not establish a completed closure-legality diagnostic. The original failed
result is retained, not relabeled as a passing aggregate. Selected execution
took 1008.751 seconds after 7.035 seconds of namespace loading with a 2 GiB
heap; this is not a phase-cost or speedup measurement.

The correction changes only test expectations and factors the negative checks
to permit a focused capture-pair selector; production, C8 forwarding and fixture
bytes are unchanged. That follow-up passed one test/six assertions with exact
rule/reason, C7 rejection and body-analysis rejection for both extensions.
Selected execution took 173.914 seconds after 6.955 seconds of namespace loading
with the same 2 GiB heap. The already exercised positive and nonfunction cases
were not repeated for an oracle-only correction. The passing follow-up is
separate evidence, not a replacement passing result for the original aggregate;
overlapping assertions are not added into a new aggregate count. Independent
review checked the narrow oracle correction. No old public C8 bundle identity
is claimed for this revision.
Captures, multi-hop or arbitrary higher-order dispatch, imported callable APIs,
callable recursion, completed thrown effects, full effect/capability legality,
targets, authoritative migration, self-hosting and seedless release remain
outstanding.

### Private root compiler-kernel entry: development enabling increment

L3 permits current-module private resolution but forbids private imports as
stable APIs. The existing, explicitly unadmitted compiler-artifact development
interpreter now applies that distinction at its root entry. After complete
fresh bundle reconstruction and exact root-module selection, the requested
entry must be an unqualified symbol naming a map-valued function in that root
plan. It need not be exported. This matches the single-source development
interface without adding a flag, field, getter, source rewrite or test export.
It enables existing private compiler kernels to remain directly testable after
moving implementation into stage-owned namespaces.

Missing names, dependency-only names, qualified entries, non-symbols and DATA
declarations reject with `:root-function-member-required`. Function arity,
lexical scope, SOURCE/B51 separation and all cross-module public-export,
namespace, origin, binding, callsite and route checks remain in place. Linked
call checks still precede argument evaluation. The change is not private-import
support, checked user-program execution or a stable public ABI.

The stable focused batch passed six tests/109 assertions for normalized root
entry and lookup order, malformed entries, exact module membership, arity,
scope, pre-argument route rejection, B51 separation, seed shape, existing source
pins and repository language policy. The initial four-test/97-assertion batch
overlaps and is not extra coverage. Normalized checks are not authentic source
or migration evidence.

The new `private-root-consumer` `.gravity`/`.qst` pair then passed the affected
composition on its first attempt: one test/33 assertions, no failures or errors.
It constructs actual ordinary C5 caller/library products and an independent
library plan. The root's private relay invokes the library's public identity
function and returns the real nested value; the public root entry still works.
Each invocation delegates to the original fresh bundle validator. Exported DATA,
dependency-only and qualified entries, non-symbol entry and wrong arity reject.
Actual C5 still rejects importing the library's private function. Coherently
rehashed root-plan/bundle tampering and a changed-root bundle also reject.
Paired identities agree. Selected execution took 380.460 seconds after 7.045
seconds of namespace loading with a 2 GiB heap. This is total selected-test
timing, not a phase-cost or speedup claim. The two nonoverlapping batches total
seven executions/142 assertions.

Only the existing root-entry membership guard changes in production. Old seed
fixtures, public cross-module visibility and unchanged expensive compiler
proofs were not rewritten or replayed. Authoritative migration, target support,
compiler seed retirement, self-hosting and seedless release remain outstanding.

### Loop-target recur typing

The development increment implements loop-slot and normal-return kernels
in the existing Gravity C7 pass, which retains authentication, inference
orchestration and diagnostic ownership. The intended subset uses monomorphic
loop slots from sequential initializers, exact recur argument types, and `Never` for transfers
that do not return normally. C8 derives body-only backedges and bottom-initialized
effect equations; initializers must not be restarted by recur. Function-target
recur typing remains a separate next increment. Ordinary `if` truthiness is
unchanged.

Focused normalized checks, current-source fidelity, inference regressions and
existing effect regressions pass 25 tests/335 assertions. A separate
current-source C6 precedence check passes one test/seven
assertions. Its earlier four failures were an assertion-path mistake: the C6
reason is under `:facts`, not at the diagnostic top level. The failed receipt
is retained separately, not relabeled as passing. The slower source selector
is explicitly separate from the fast development-kernel selectors.

The real paired composition first failed during upstream product construction
at the unchanged 2 GiB heap. Direct ordinary C2 calls reproduced the allocation
failure without C5 or bundle construction. These failed results confer no loop,
runtime, migration or self-hosting credit. The detailed failed-attempt history
remains in the original command outputs, not as prerequisites for development.

The separately reviewed enabling commit `1e2e6f5c` removes discarded canonical
text, duplicate canonical-form construction, a discarded terminal request walk
and redundant private canonical history. It preserves public canonical text,
fixed UTF-8 hashing, fresh validation, complete products, diagnostics and both
canonical and host-container collision checks. Required digest uniqueness
already rejects equal canonical forms: equal forms have equal fixed encoding
bytes and deterministic hashes; no hash-injectivity assumption is needed.
Focused checks pass 15 tests/335 assertions, including full-result parity,
encoding/print settings, rejection precedence, collisions, repeat-call freshness
and primary/cleanup failures. An earlier test-observer mistake remains a
separate failed result. These overlapping helper results are not added to the
loop counts above.

A bounded recording of a failed C2 call showed fresh precomputed-product
verification overlapping the original products, not a scheduling or lock
failure. Allocation samples do not establish exact retained dominators. The
corrected ordinary C2 constructor subsequently returned on the then-current C7 draft
source at 2 GiB: 366.111 seconds inside the call, exit 0. This is constructor
completion, not a general memory bound, speedup, loop acceptance or migration.
The enabling fix does not cache accepted proofs, omit products, increase bounds
or introduce a new test lane. No repeated C2 confirmation is needed for unchanged
inputs. Product-representation redesign remains separate unless an actual
integration blocker requires it; ordinary semantic edits do not depend on this
investigation.

A later paired selector reached its thirty-minute operational limit
without completing. The exact owned JVM exited on TERM (exit 143); its timing
receipt was absent. No completed assertion counts, language rejection or
current execution phase can be inferred. Static inspection finds sixteen
public C7 calls and seventeen full bundle constructions on complete traversal,
but does not establish where the live attempt spent its time. A separate
ten-minute, 2 GiB phase observation also stopped incomplete (TERM, exit 143).
It captured early initial-construction events through its 64-event cap, but
no return boundary or final observer status. Later phase attribution and cost
ratios remain unresolved. No further timing-only retry, larger allowance or
validation-policy change follows.

Independent source inspection found a discarded SH05 verification result inside
C5 construction. Commit `a9c39502` removes only that unused call; consumed parent,
request and final-replay checks and source snapshots remain. Focused comparison
with the restored-call oracle passes three tests/27 assertions for complete
artifacts, serialization, diagnostics, fresh next-invocation checks, forged and
stale inputs, malformed context and interruption. This is a local simplification,
not measured speedup or loop acceptance. The existing single bundle-construction
selector was a separate affected integration check, not the full sixteen-call
loop selector or evidence of its semantic result. It was cancelled incomplete
before its limit when this optional extraction was deferred; it produced no
completed counts or timing receipt and is not a compiler rejection.

The optional, unaccepted `gravity.types` extraction is deferred. It made compiler
code compilation require the full multi-module C5 bundle path before this local
semantic change could be tested. Keeping the single kernel implementation in
the already stage-owned Gravity C7 namespace uses the existing single-source
interface instead. This is a real production-source change with local calls,
not import stripping, a raw-root bypass, duplicated kernels or a new test lane.
The accepted `gravity.effects` boundary and all user-artifact authentication,
fresh result verification and hostile cases remain required. Gradual namespace
extraction remains planned; it is not a per-increment admission prerequisite.

The first real composition after this simplification exposed a binding join:
the authenticated request retains bindings separately from canonical core,
whereas the normalized kernel fixture embedded them. A private C7 working view
now joins the already-authenticated vector for loop analysis only; published
core, request, identity and public schemas remain unchanged. Missing, malformed,
empty and forged-scope binding tables still reject without a fallback.

The corrected, unreduced paired composition passes one test/53 assertions. It
authenticates both `.gravity` and `.qst` source products through C5/C6 and current
C7 construction/fresh verification, preserves the complete request binding,
and constructs/freshly verifies the C8 effect leaf. Both exact source files
execute through the existing seed interface, return `7`, and observe the recur
transfer `[false 7]`. Wrong slot and declared return types reject with
`C7-TYPE-MISMATCH`; tampered type facts, constraints, bindings and effect graphs
reject. The earlier missing-binding failure remains a separate failed result.
The 25/335 focused group overlaps earlier kernel batches and must not be summed
with them. The paired check took about 489 seconds of selected execution at
2 GiB; this is an observation, not a speedup or memory-bound claim.

This supports the stated monomorphic loop-target subset, not function-target
recur, general coercions or joins, termination, tail-call optimization, native
target support, public C8 module-bundle acceptance, authoritative migration,
self-hosting or seedless release. Independent semantic/evidence review accepts
this bounded development slice. Documentation, roadmap, source-language and
Clojure physical-line gates pass; the local slice commit is separate from
authoritative admission and those outstanding claims.

### Function-target recur typing and effects

The next bounded C7/C8 increment supports fixed-arity function-target recur when
the original parameter declarations independently establish each slot type.
For example, an `again` function with `[flag :- Bool value :- String]`, returning
String, accepts `(if flag (recur false value) value)`. Slots retain declaration
order and exact types without caller specialization or implicit coercion.
Unknown undeclared slots remain `C7-ANNOTATION`, even when callers supply known
argument types. Wrong recur slots and declared result mismatches reject with
`C7-TYPE-MISMATCH`. Original C6 arity, tail position, binding and nearest-target
checks still apply. Function construction does not execute its body.

The existing C7 source classifies supported recur as `Never` and retains the
normal-return join. `gravity.effects` joins the original function signature,
parameter facts, body identity and ordered transfers. Function-only recur uses
the existing bounded body-backedge fixed point; the module need not contain a
`loop` node. Solver selection and graph metadata use the same private decision.
Ordinary acyclic code and named self-calls retain their prior paths. Malformed
control tables reject before host operations; nil/absent legacy acyclic inputs
retain their prior behavior.

Focused current-source checks pass 28 tests/392 assertions, including inference
from an initially absent transfer type, declared slot origin, function reset,
work limits, malformed controls, solver parity and effect regressions. The
unreduced paired composition passes one test/69 assertions: real C5/C6/C7
construction and fresh verification, complete request binding, C8 effect-leaf
construction/fresh verification, paired identities, hostile mutations, and all
three negative fixture families in both `.gravity` and `.qst` forms. Selected
execution took about 706 seconds at 2 GiB; no speedup or allocation bound is
claimed. Earlier overlapping kernel groups are not additional coverage.

The first paired attempt passed C7 but failed C8 because function-only recur
entered the acyclic solver. It remains a separate failed result (two passing
assertions, one failure and one error), with later assertions unexecuted. The
new dispatch regression executes the same private helper used after the module
joins; it does not itself replace those joins or the real paired check.

At this compiler-slice boundary, annotated-source seed execution remained open;
the separate increment below closes the stated fixture's execution without
stripping declarations or substituting an untyped example. General recursion,
termination, target legality, public C8 module-bundle acceptance, authoritative
migration, self-hosting and seedless release remain unclaimed.

### Annotated single-source seed definitions

The existing seed now lowers fixed-arity atomic parameter/return declarations
for `defn` and `def`/`fn`, retaining declarations in function records and deriving
logical parameter names and body forms. Macro-expanded returns retain their
actual `typed/return` declaration header; raw prefixes retain their raw form.
Original source bytes and SHA remain unchanged. Declarations affect both plan
and semantic identity independently of executable instructions.

The raw definition parser remains byte-identical. The shared function table
preserves complete plain-record parity for all seven existing C5/B51 module
sources, not merely an assumed seed-only caller boundary. Raw plan construction
reuses its existing source records for original-header validation; it does not
parse twice or add a field to shared macro artifacts. This catches nil/false
return annotations that legacy macro expansion otherwise consumes silently.
The direct emitter reuses its existing raw C2/C3 forms and remains hosted-only.

Focused checks pass 11 tests/179 assertions: valid raw and expanded signatures,
malformed and composite declarations, duplicate/variadic names, pre-execution
rejection, plain whole-plan parity, declaration-sensitive semantic input, shared
table parity, and function/loop transfer isolation. Earlier reader-syntax and
test-profile mistakes remain separate failed attempts; production was unchanged
by the test-only profile correction.

The exact previously compiler-checked function-recur fixture then passes one
test/14 assertions. Both unchanged `.gravity` and `.qst` files retain their
source SHA and declarations, return `"done"`, and observe exactly one transfer
`[false "done"]`. The completed 69-assertion compiler composition was not
replayed to renew a receipt. These are ordinary development results, not fresh
authoritative migration evidence.

Seed grammar recognition does not resolve types or replace C5/C6/C7/C8 legality.
Unknown type names, unsupported semantic forms and mismatched uses retain those
owners. Composite signatures, variadics, checked imported-user execution and
general typed-runtime conformance remain unsupported. No target legality,
termination, public proof, seed retirement or self-hosting claim follows.

### Direct typed throw and source effect allowances

The next bounded C7/C8 increment retains a concrete direct-throw payload type,
types the transfer expression as `Never`, and joins that result with a normal
branch without treating the thrown value as a normal return. Unknown payloads
require annotation; a mismatched normal result still rejects. The C7 ledger
uses the existing equality constraint kind, with original source, payload,
owner, ordering and invalidation premises. The original C6 transfer census is
preserved, including an outer transfer whose operand never returns.

C8 reconstructs the typed-transfer facts independently and derives the direct
`:error/throw` effect before checking original function and namespace source
allowances. Missing declarations produce `C8-UNDECLARED` with the offending
function and source node. Nested function errors stay latent at function
construction, quoted DATA remains inert, and unrelated calls preserve their
previous behavior. At that boundary, local calls involving the newly concrete
throwing owners remained unsupported; the direct-local increment below addresses
that gap. Selected imported concrete throw summaries remain fenced
by C8's existing pending-summary requirement, not a new C7 import claim.

Focused development checks pass 24 tests/425 assertions, covering the new
transfer/type/allowance kernels and existing loop, recur, import, recursion,
callable and body-effect regressions. These overlapping development runs are
not added together as independent coverage. The first actual composition run
failed at C8 canonical-core coverage after fresh C7 acceptance and verification:
one passing assertion, one failure and one error, with later cases unexecuted.
The mismatch was raw-versus-semantic authenticated IDs in throw records. The
correction follows C6's existing one-field identity projection, preserving raw
source authentication and every other field. A direct coverage-guard regression
uses distinct IDs and rejects wrong projection, authentication and other-field
changes. The failed result is retained separately from the corrected run.

The corrected actual composition passes one test/62 assertions over both
unchanged `.gravity` and `.qst` sources: fresh C5/C6/C7 construction and C7
verification, the current C8 leaf and fresh result verification, altered typed
facts and effect graphs, coherently rebound false typed inputs, and all three
real rejection families. It completed within the unchanged 2 GiB/30-minute
development bound. No public C8 compiler bundle was reconstructed. This is
accepted development composition evidence, not refreshed migration authority.

At this compiler-slice boundary, runtime throw execution remained open; the
following seed increment closes the exact fixture's explicit seed observation.
Handlers and cleanup, throwing-call propagation, general error/profile/provider
legality, authoritative migration, target completion, self-hosting and seed
retirement remain separate gates. Source
allowance checks do not establish the complete authority intersection. The
legacy normalized `:error/raise` registry is not silently treated as the source
`:error/throw` registration.

### Exact-source single-source seed throw

The existing raw three-argument plan constructor now lowers one-operand throw
through the temporary Clojure seed. Arity rejects before operand compilation;
execution evaluates one operand and escapes through a private intentional
transport. The payload remains DATA, including control-looking maps and host
Throwable objects supplied to the raw executor. They are not themselves thrown
or interpreted as control. A nested throw preserves the original inner
exception, with no second outer transfer. Existing host diagnostics and fatal
failures keep their prior propagation; ordinary builtin failures keep their
existing diagnostic mapping. This does not complete host-error normalization.

Summary traversal includes the operand. Throw-containing plans explicitly name
seed ownership and pending effect inference/unchecked legality, rather than
claiming Gravity-emitter execution or purity. Original source hashes,
declarations and full plan identity are retained; no-throw plans preserve exact
parity. The existing module-source and instruction-census boundaries reject
executable throw for the four-argument module route, while quoted DATA remains
inert. No new public API, namespace, test lane or authority path was added.

Focused seed checks pass 12 tests/202 assertions, including arity, malformed
instruction, ordering, early escape, nested transfer identity, function/loop
control, host errors, module fences and ordinary plan regressions. A separate
one-test/28-assertion observation executes both unchanged typed-throw fixture
extensions. The ordinary branch returns the identical original String payload;
the throwing branch escapes once with that payload and actual source hash.
Annotations and logical parameters are preserved. The accepted 62-assertion
compiler composition is reused only for those unchanged bytes, not replayed to
renew a receipt. These results do not establish Gravity-authored emitter/runtime
ownership, imported errors, handlers, resource cleanup, a public target exception
ABI, full profile legality, migration authority, self-hosting or seed retirement.

### Acyclic direct-local thrown-error propagation

The current development increment replaces the throwing-local-call fence with
bounded analysis of complete original C6 local edges and supported acyclic
bodies. C7 preserves direct throw records and derives ordered, distinct possible
error types through callee bodies. C8 independently reconstructs those equations
and compares the call premises and function summaries before source allowance
checks. Normal return types remain separate from thrown types. A supported pure
leaf can establish an empty error vector; unknown or pending is not empty.

The paired accepted fixture contains a pure String identity function, two relay
hops, a Bool-throwing callee and an argument that never returns. Conservative
source operand error potential is retained, but that argument prevents normal
callee invocation and excludes its Bool body error. Static call/source evidence
is not erased. Disconnected prior direct-throw owners keep their existing
analysis. Rejection fixtures cover argument/result mismatches, unknown slots and
missing source effect declarations.

The initial focused regression passes 27 tests/468 assertions, including
independent C8 replay, coherent summary tampering, source ordering, pure-leaf
and non-returning operand
cases, cumulative work limits, and existing loop/recur/callable/import controls.
The first affected source composition failed before seed execution: one passing
assertion, one failure and one error. A newly reused legacy edge validator
confused the callee name's syntax ID with the whole definition form's syntax ID;
the actual producer distinguishes them. The failed result is retained. Corrected
exact-edge reconstruction and original work-limit diagnostic precedence pass
three affected tests/57 assertions with distinct-coordinate and budget controls.
The corrected composition completed both source extensions and their same-byte
seed observations: 145 assertions passed, two failed and none errored. Both
failures were the new unknown-slot fixture expecting `C7-ANNOTATION` instead of
the existing `C7-GENERIC/:unresolved-function-type` diagnostic for an unused
unknown parameter. Positive construction, verifiers, coherent tampering,
normal return, escaping payload and early argument escape checks passed. The
aggregate run remains recorded as failed; correcting that test oracle does not
retroactively turn it into a passing run or require replaying its successful
blocks.

That actual composition used `effects.gravity` SHA-256
`4a2b686f680e4aaacf2791947e12bd48c4dc93a971331558b72ebe1d96f5a44b`.
A subsequent exact-output lookup factor uses the existing validated function
index rather than scanning every function for each node; its source SHA-256 is
`4906ff18c485e64535297201b90973da51b1804e8440bf420eacff99e6e34f0e`.
It retains original source-callee evidence separately from the selected
execution target. The final focused signature-status control passes, and
369 parity assertions cover local, non-returning, higher-order, noncall,
missing-ID and duplicate-table boundaries. This does not reattribute the earlier
composition to a different source revision. The first parity run's incomplete
positive test fixture is retained as a test-data failure; the repaired fixture
passes without weakening the production validator.

The final current-source focused regression passes 29 tests/852 assertions,
including the repaired oracle and lookup controls and existing loop/recur,
callable, imported-body and effect checks. These overlapping focused runs are
not added together as separate coverage. The unchanged successful expensive
composition and seed blocks were not replayed merely for a test-oracle edit or
the output-equivalent lookup factor.

Throwing recursive/recur components, imported and indirect throwing calls,
handlers, cleanup and complete error/profile/provider legality remain separate
work. No authoritative module migration, full target implementation, self-hosting
or seed-retirement claim follows from this development increment.

### Current Gravity emitter throw lowering

The Gravity-owned `p15_s23/emitter.gravity` now lowers exactly one `throw`
operand, preserving the owning child path and rejecting malformed arity before
operand compilation. Summaries traverse nested throw operands. Throw-containing
products mark effect inference pending, legality unchecked and authority
non-authoritative; ordinary products retain the existing println-only inference.
Original parsed function declarations and logical parameters are preserved.

The current-source focused regression passes nine tests/272 assertions,
including 62 assertions executing the actual changed Gravity emitter functions
through the existing raw developer interface. The unchanged accepted paired
throw source is lowered and assembled, then its returned instruction is observed
through the temporary seed executor: normal return and escaping payload/source
are preserved. Wrong arity, missing symbols, unknown callees, nested summaries,
inert quoted DATA and ordinary whole-product parity are checked. No old compiler
proof was replayed and no admission pin was refreshed.

Earlier focused results remain failures: the first run had two assertions read
source provenance from the wrong transport field; an adjacent legacy macro-route
test correctly rejected the stale admitted emitter identity; an added parity
oracle initially expected the seed's empty inference instead of the unchanged
Gravity println inference. Only test expectations and the development selection
were corrected. These results are not retroactively relabeled as passing.

Gravity-owned runtime error transfer, handlers, complete legality, target error
lowering and fresh authoritative migration remain outstanding. The emitter
development revision does not inherit its old admitted source identity or
establish full SH-07, self-hosting or seed retirement.

### Current Gravity runtime throw propagation

The local Gravity evaluator in `p15_s23/runtime.gravity` now propagates throw
control through arguments, binding initializers, sequences, conditionals and
top-level outcomes. Its private three-slot result separates normal values,
thrown payloads and rejected instructions. Payload maps, vectors, nil, false
and Throwable objects are DATA, not control records. A transfer preserves prior
output and skips later work. Unknown operations and builtins no longer fall
through to let or comparison evaluation. Ordinary success keeps its seven-key
record, formatting and metadata tolerance; malformed instructions are distinct
from language error values and host failures.

The focused current-function check passes 147 assertions. The final ordinary
regression passes ten tests/419 assertions, including that check, current emitter
lowering and existing raw seed/declaration controls. The same unchanged paired
throw fixture is lowered by the actual Gravity emitter, then evaluated by the
actual Gravity evaluator: false returns normally and true propagates the same
String payload. The seed's private throw transport is not invoked. Controls
include nested transfers, each argument/binding/sequence/branch position,
preserved prior output, inert control-looking DATA, invalid dispatch before child
work, legacy metadata accepted by existing validation, and original fatal host
failure identity. These overlapping runs are not separate coverage totals.

The first whole-runtime direct compilation failed on preexisting manifest
bare-symbol DATA outside that compiler interface's literal subset. Focused tests
instead select the exact six mutually dependent evaluator function forms from
the current source and freshly compile that projection. Names, arities, form
equality and dependency closure are checked. Projection and full parent source
hashes are distinct; this is not whole-module compilation or an admitted runtime
binding. A subsequent test-reader delimiter failure is retained separately.
Review caught and corrected an initial restriction that would have rejected
legacy instruction metadata; the existing ingress and comparison semantics were
not weakened to pass the tests.

No old packet validator, catalog identity or admission pin changes here. The
admitted packet subset still excludes throw. Handler execution, guest function
dispatch, loops/recur in this closed evaluator, resource unwinding, profile/target
error lowering, host normalization and authoritative migration remain open.
Whole-runtime manifest literal compatibility needs a bounded investigation at
its migration boundary, not a prerequisite for every semantic edit. Generic seed
interpretation and primitive execution remain; this is not seed retirement or
full SH-07/self-hosting completion.

### Atomic typed catch and residual source errors

C7 now resolves canonical atomic catch types from authenticated C6 bindings,
seeds each handler's lexical binder with that type, and joins protected/handler
normal results with the existing `Never` rules. Ordered handlers remove matching
protected source errors; a handler's own throws escape that try and can be
caught by an outer try, not a sibling clause. Acyclic direct-local calls carry
the residual error types. C8 independently checks the source joins, handler
premises and residual equations. Covered source errors do not grant effect
discharge: performed `:error/throw` and the source allowance remain required,
with profile/provider/capability authority explicitly pending.

The paired accepted fixture covers recovery, partial handling, nested catches,
rethrow, an unreachable handler, ordered repeated types and a local call under
a catch. Rejected pairs cover incompatible normal results, unsupported resolved
catch types, missing effect allowance, unresolved types and illegal binder use
in protected, sibling or outside scopes. Coherent mutations of type/binder,
clause order, source premises, residual summaries and effect graphs reject.

The broad focused regression passed 41 tests/1,186 assertions. A separate source
scope check passed six tests/80 assertions, including eight real C5 rejection
assertions. The affected authenticated paired composition passed one test/220
assertions with fresh C7 verification and C8 checking. These overlapping checks
are not additive coverage totals. An earlier new-helper reader-delimiter failure
remains a failed attempt. The final rejected-diagnostic provenance correction
passed the six-test/82-assertion direct handler batch, including ten original
source-location/origin assertions. It does not change accepted typed products
or justify repeating their expensive composition.

Optional plain-EDN retention exceeded its chosen serialization cap for both
accepted triplets (43,392,705 and 43,328,121 characters); no retained product file
was written. The optional capture is removed from the normal test rather than
growing caps, stripping proofs or replaying solely to obtain a file. A bounded
representation follow-up is described in `docs/development-verification.md`.

At that boundary, runtime catch execution, type-authoritative lowering inputs, complete Error
hierarchy/subtyping, target/profile error lowering, resource unwinding and
authoritative migration remain open. This increment does not establish full
SH-07/SH-08, self-hosting, release support or seed retirement. The next executable
boundary is an explicit current C7 typed input to the existing Gravity lowerer;
raw type spelling or an old admitted compiler binding is not type authority.

### Explicit current C7 input to Gravity lowering

The existing compile-function and assembly interfaces now accept an explicit
fourth argument containing the original request, typed result and verification
report. A small temporary Clojure component invokes the actual current Gravity
C7 verifier once per operation. The Gravity emitter requires an accepted typed
result and exact fresh report equality, then joins source bytes, expanded forms,
module metadata, declarations, function/body identities and parameter/type
bindings. A passed verification of a rejected type result is not acceptance.
The source snapshot is reconstructed from the same request's retained C2
tokens; target source files need not be reread or remain on disk.

The typed lowerer consumes canonical nodes rather than introducing another
source grammar. Its initial supported subset is fixed named top-level
single-body functions with represented literals, parameter references, `throw`,
`if` and `do`. Unelaborated numeric descriptors and unsupported operations reject
with structured diagnostics retaining available source and generated-origin
information. Current-source provenance includes actual compiler source/plan and
function/body identities; raw internal DATA arguments are not authority tokens.
Existing three-argument calls, the old pinned `00582` assembly envelope and
admission identities remain unchanged. Effect legality stays explicitly pending.

The focused batch passed four tests/283 assertions, including unchanged current
emitter/runtime throw controls. After the test-only namespace/body correction,
the two typed-input selectors passed 74 assertions. The sole affected paired
composition passed one test/65 assertions: it constructs real upstream products
once per extension, invokes fresh current C7 verification for each public typed
operation, compares function and module assembly, and executes current Gravity
instructions through the existing exact runtime projection. Both normal and
escaping String results preserve the original payload; seed throw transport is
not called. Changed source/module/declaration/body, stale reports and altered
type facts reject. These runs overlap and are not additive coverage totals.

Test-reader and emitter delimiter failures, and two omitted-namespace selector
failures, remain failures. The SH05 body-only expansion versus full namespace
parse mismatch was caught statically and corrected before the sole expensive
composition. No proof replay, optional product capture, old pin refresh or
authority-admission workflow was needed for these development corrections.

At this boundary, typed runtime catches and quoted-symbol lowering were the
next dependency-ready semantic work. General calls, bindings, loops, complete numeric representation,
Error subtyping, resource cleanup, profile/target legality and authoritative
migration remain outstanding for this typed route. The runtime test uses the
unchanged exact six-function projection, not whole-runtime compilation. This
is executable development progress, not full SH-07/SH-08, self-hosting, release
readiness or Clojure seed retirement.

### Canonical atomic catch lowering and runtime handling

The current Gravity emitter consumes solved C7 handler records and canonical
Bool, String, Symbol and Keyword types. It rejoins handler order, original
source clauses, binder identities/scopes/types and handler-body ownership.
Quoted Symbol payloads use the solved inert quoted-DATA record; source spelling
alone is not authority. Handler binders extend only their own body environment.

The Gravity runtime evaluates the protected instruction once and selects the
first matching atomic handler only for thrown control. Normal and rejected
control bypass handlers. A selected handler's throw bypasses sibling handlers
and can reach an outer try. False matches Bool; nil does not. Payloads retain
identity, shadowing leaves the outer environment intact, malformed handler
descriptors reject before protected execution, and fatal host failures retain
their original identity rather than becoming guest error values. Descriptor
validation checks body maps shallowly, not whole instruction graphs.

The final direct catch checks passed two tests/128 assertions. Earlier focused
regressions overlap and are not additive coverage. A test-only transport
instrumentation gap was found and corrected before the affected real
composition: all tiny atomic executions must be observed, not merely the
main entrypoint and older fixture cases. No expensive proof was replayed for
that correction. The sole affected composition passed one test/122 assertions
over four genuine upstream inputs, with fresh current C7 verification per
public operation. It exercised original nested/ordered/rethrow cases, all four
atomic payload types, quoted Symbol DATA and exact public main recovery. Seed
throw transport remained unused around every execution; the unsupported guest
call and changed typed ledger rejected. Test execution took about 491 seconds
plus seven seconds of namespace loading. This is a different workload from the
direct checks, not a speedup comparison or an additive coverage total.

At that boundary, general guest calls, symbol-let lowering, quoted containers, numeric-width
catch matching, Error hierarchy/subtyping, resource cleanup and complete
profile/target legality remain outside this subset. The runtime composition
uses nine exact original function definitions, not whole-runtime compilation.
Existing admitted packets and migration identities remain unchanged; this
does not establish authoritative migration, full SH-07/SH-08, self-hosting,
release support or seed retirement.

### Canonical immutable symbol-let lowering

The Gravity emitter now consumes C6 lexical-binding records and C7 inferred
local-binding facts. Each binding joins its source vector/name/initializer,
ordinal, original scope, prior-visible IDs, immutable binder and type fact.
Initializers lower under the outer and preceding bindings; a new binder is
introduced only afterward. Body lowering uses the completed local context
without mutating the outer one. Source operator, parent, exact record census
and body identities are checked as well as inferred types.

The existing resolver models repeated names in one vector as sequential
shadowing with distinct binding identities and scopes. Nested one-binding
runtime lets preserve that behavior while leaving the existing flat runtime
duplicate-label rejection unchanged. Empty binding vectors lower to body
sequence execution. The normal-result check respects a `Never` initializer,
rather than assuming the last body type proves a normal return.

The focused regression batch passed seven tests/470 assertions, followed by
the final let kernel at one test/60 assertions after an additional malformed
family check. These overlap and are not additive coverage. The initial draft
used unsupported `min`; its six-test run recorded 219 passes, seven failures
and four errors. The correction used explicit cardinality checks, not a new
host builtin or wider language allowance. Independent readiness review covered
the literal C6/C7 schemas and prepared real-source test before execution.

The first real composition failed during its first accepted assembly with
`typed-let-source-or-census-mismatch` (nine passed assertions and one error),
before runtime positives or the second extension. Static tracing independently
identified a sufficient rejection condition: the lowerer required the binding
vector container's scope to equal the owner scope, but the actual projection
leaves that container scope nil. C6 requires scopes for the owner, individual
initializers, binders and body, not the vector container. The normalized test
had fabricated that scope. The correction removes only this invented consumer
premise and uses nil in the normalized test, preserving all required scope
joins and the upstream language policy. The generic failure does not prove
it was the sole mismatch. A direct test of the saved projection helper bodies
also checks the real container/initializer/binder/body scope shape without
constructing an authority proof. Its first attempt could not resolve helper
Vars removed by normal bootstrap cleanup; that test error remains recorded.
The corrected focused batch passed eight tests/493 assertions, including 19
projection-schema and 63 let assertions. It does not replace real composition.

The corrected candidate's affected composition passed one test/132 assertions
in about 366 seconds of execution plus seven seconds of loading. Both source
extensions construct their accepted upstream input once and reuse it for all
nine functions, assembly and runtime checks. Actual self/forward-reference
rejections, nested one-binding census, original payload identity, shadowing and
outer restoration, multiple body forms, Never-initializer recovery, catch-local
aliases and exact public main results pass. Each public lowering operation
checks fresh current C7 verification; changed facts and lexical records reject,
and no guest execution uses the seed throw transport. This pass does not erase
the first failure or make overlapping focused counts additive.

Other call forms, closures, mutable locals, destructuring, quoted containers,
numeric coercion, complete effect/profile/target legality and authoritative
migration remain separate gates. Current-source lowering/execution does not
refresh old admitted compiler identities or establish self-hosting, release
support or seed retirement.

### Canonical direct same-module calls

The Gravity emitter now lowers fixed-arity direct calls between current-C7-
accepted named functions in one acyclic module. Canonical call/edge records,
operator binding, declaration/function/body identities, argument order, C7
constraints and call facts determine the target. Qualified references to the
same local binding emit the same target; no runtime string lookup is used.
Arguments with type `Never` make the call non-returning without changing the
callee's normal return declaration. Existing source and thrown-type facts are
retained; effect/profile authority stays pending.

The Gravity runtime carries the closed plan's function table and active call
path explicitly through one private evaluator/helper implementation. Legacy
entrypoints remain thin empty-context compatibility calls. Target shape,
parameter uniqueness, arity and active cycles reject before argument effects.
Arguments evaluate once, left-to-right, in the caller environment. Normal
values enter a fresh callee environment; thrown/rejected argument control
aborts the remaining arguments and invocation. Callee output and control return
unchanged. Fatal host failures are not converted into guest throw values.

The focused batch passed nine tests/592 assertions, including 55 direct-call
lowering and 63 runtime assertions alongside overlapping regressions. Earlier
stale unsupported-call oracle failures and a new-test reader EOF remain failed
attempts. Qualified-reference and declaration/binding identity checks were
reviewed before the real composition, not repaired by relaxing authority.

The sole affected paired composition passed one test/214 assertions in about
869 seconds of execution plus eight seconds of loading. Each accepted input is
constructed once per extension and reused for thirteen-function assembly,
public-function parity and runtime cases. Real chains, same-module qualified
references, let aliases, original payload identities, caller catches, callee
rethrow and first-throwing-argument abort pass. The actual C5 unknown-call and
C7 wrong-arity, wrong-type and nonfunction calls reject. Each public lowering
operation checks fresh current C7 verification; coherent input mutations reject
and the seed throw transport is never used by guest execution. These results
do not make overlapping focused counts additive or compare equivalent timing
workloads.

The current consumer's 256-entry active-path limit is not a language legality
rule or proof of host-stack, total-cost or memory fit. Target body checks are
shallow. The runtime test projection contains 16 exact original definitions,
not the whole runtime module. Existing cross-plan continuation machinery and
admitted identities are unchanged. Imports, recursion, callable parameters,
closures, variadics, full profile/provider legality, authoritative migration
and seed retirement remain open.

Qualified declarations are a separate existing C7 gap: macro/C5/C6 preserve the
original qualified declaration but C7 requires equality with the simple binding
name before typed acceptance. The new lowerer does not claim to fix this
upstream mismatch or turn it into a language-law prohibition. A future C7
identity-join correction needs its own accepted/rejected source cases and
affected validation; qualified references are already a distinct supported
case here.

### Canonical loop-target execution

The current development candidate passed the six-input affected composition:
one test/128 assertions, with a real multi-function `.qst` compile, assembly and
execution, both decimal diagnostic boundaries and three rejected `.qst` inputs.
The selected operations took 586.919 seconds; command elapsed time was 594.656 seconds.
The focused batch passed twelve tests/750 assertions. These are ordinary
development results, not authoritative migration or whole-language completion.
The failed and interrupted attempts below retain their original status.

The current Gravity lowerer and local runtime implement sequential loop
initialization, left-to-right recur arguments evaluated in the old environment,
and simultaneous slot replacement. Canonical C6 target/binding/transfer records
and C7 slot/constraint rows are consumed without changing the public lowering
or execution interfaces. The runtime uses a separate target-bearing control
position, not payload recognition; nested targets, non-tail and try barriers,
callee isolation, source errors and original host failures remain distinct.

The focused batch passed eleven tests/713 assertions, including 57 normalized
lowering and 64 runtime assertions. Current C7 constructs the normalized test
rows; this is not source authentication. The duplicate-index rejection oracle
was corrected to inspect the raw helper result, with a positive index check.
These counts overlap earlier batches and are not additive.

The first real composition failed: one test, 25 passed assertions, one failure
and one error. The unchanged `loop-recur-types.gravity` case completed; the
new `local-runtime-loop.gravity` input was rejected by C7 before lowering, and
the remaining ten inputs did not run. Its diagnostic was `C7-ANNOTATION`,
`:unsupported-throwing-body-operation`, with actual operation `:loop`.
Throws and handlers activate a source-error graph whose operation subset
excluded `loop` and `recur`; this does not establish that an ordinary loop call
alone triggers that graph. The failed run remains failed, not a passing loop
and handler composition.

The bounded C7 correction admits `loop` and `recur` to the existing lexical
source-error union. It adds no control backedge or thrown type for a transfer.
The preceding canonical target/slot/transfer validation, handler order, `Never`
behavior, work limits and fresh reconstruction are unchanged. Fourteen focused
tests/227 assertions pass, including handler-only activation, called-body
propagation and independent handler reconstruction negatives. Four affected
lowering/orchestration tests/170 assertions also pass. These batches overlap
earlier coverage and are not additive. Independent review accepted the changed
implementation for composition. That second run completed the unchanged loop
and new main `.gravity` fixtures, including loop/throw/catch execution, then
stopped on `local-runtime-loop-decimal.gravity`: `C7-VERIFY` reported
`:stale-b47-identity-preimage` before lowering. Its aggregate remains failed
(99 passed assertions, one failure, one error); paired `.qst` and negative
inputs were not reached. The mismatch was traced to the host exporter converting
only canonical decimal node values to `BigDecimal` after their descriptor-bearing
identity preimage was resolved. The exporter now retains the original exact
descriptor, including scale and C2 occurrence links. Its existing explicit
presentation helper remains available but is not canonical input. C7 equality
checks are unchanged. The focused actual-helper/C7 kernel passed 27 assertions;
two affected lowering/orchestration tests passed 67 assertions. These normalized
controls are not authenticated source evidence. Independent review accepted the
correction and the remaining-input selector, which constructs the other ten
inputs freshly without replaying the two successful nondecimal `.gravity`
cases. Those earlier successes retain their pre-correction snapshot attribution;
they are not final-current authoritative identities. That remaining-input run
was interrupted: its JVM is gone, its timing receipt is absent, and no final
aggregate or native exit status was recovered. Retained output contains eight
assertion failures, four for each decimal source extension. C7 accepts both;
lowering rejects earlier than expected because it compares the public raw
function body with C6's descriptor-bearing normalized body. Ordered partial
coverage is not a completed batch; completion of the last three negative `.qst`
inputs is unobserved. The implemented fix joins verified raw roots to C6 root
IDs in source order for definition/table fidelity while compiling only typed
nodes. Its focused batch passed twelve tests/750 assertions, including 37
source-join assertions for decimal fidelity, malformed roots and headers,
definition/table equality, work bounds and ordinary/annotated parity. Those
normalized checks are not source authentication. The affected real-input scope
was one multi-function positive, both decimal extensions and the three unfinished
negative `.qst` cases. This changed-candidate run passed all 128 assertions,
including precise numeric node/syntax/origin/span rejection, current C7
verification once per public operation, unchanged source/ledger tamper rejection
and zero seed throw transport. The original frozen source, test and fixture
inputs remained unchanged. No interrupted or failed aggregate is relabeled
passed, and no full twelve-input final-candidate matrix is claimed.
At that loop-only checkpoint, function-target runtime recur remained open.
Its twenty-definition evaluator projection was development-only. The following
increment adds function-target execution, not complete effect/profile legality,
whole-runtime compilation or authoritative migration.

### Canonical function-target recur execution

The current Gravity emitter and runtime now lower and execute fixed-arity
function recur using the original C6 function target, ordered parameter bindings,
explicit known C7 parameter slots and transfer ledger. Recur arguments run
left-to-right in the old environment. A matching backedge replaces all parameters
together in a fresh local environment, drops prior body locals, preserves output
and retains the same call path. Ordinary recursive calls retain their cycle
restriction. Loop targets, tail/try barriers and caller/callee isolation remain
distinct. Called-function metadata is validated before argument evaluation;
legacy zero-argument entry records may omit parameter/arity fields, but present
malformed fields are not defaulted away.

The full source/header join dominates function compilation once. Independent
review caught a new per-transfer repeat of that join whose source-sized work was
not covered by the precharge. The correction reuses existing active-function
records and C7 parameter facts, retaining target, declaration, ordered binding
and current-type checks without a cache, flag, public input or increased cap.
The final current-source regression passes fourteen tests/861 assertions,
including 48 function-target lowering and 63 runtime assertions. These cover
the exact transfer budget, one-less rejection, no-repeat source control and
parameter tampering. The smaller affected batches overlap this regression;
their counts are not additive. Earlier projection-census and test-syntax
failures remain failed observations.

The affected composition passed once: one test/276 assertions, six freshly
constructed source inputs, with all frozen code/test/fixture bytes unchanged.
Both extensions of the existing annotated function fixture return the original
String through a function restart and an ordinary guest call. Both extensions
of the new ten-function fixture exercise parameter swaps, shadowing, fresh
locals, nested loop/function targets, first-argument throw abort, throws after a
restart, caller catches preserving payload identity, a terminating zero-slot
case, and the unchanged seven-key main execution record. The existing rejected
slot-type pair reports the precise C7 String-versus-Bool mismatch and rejects
public lowering. Target, tail and ledger mutations reject; each public
operation verifies with current C7 once; no seed throw transport is used.
These mutations are not claimed as coherently resealed adversarial products.
The zero-slot case checks lowering, the empty ledger slots and normal entry;
its false branch does not execute a zero-arity backedge. Rejection of leaked
prior body locals is covered by the normalized runtime kernel, not by an
out-of-scope reference in a source fixture.

Selected execution took 660.745 seconds and the completed command took 668.622
seconds with a 2 GiB heap. This includes real construction and verification,
not just scheduling; it does not identify a dominant phase or establish a
speedup. The direct commands are in `docs/development-verification.md`.
The twenty-two original-source runtime functions are a development projection,
not a compiled or admitted whole runtime. Arbitrary recursive calls, closure
execution, full effect/profile/provider legality, termination and total-cost
proofs, authoritative migration, target completion, self-hosting and seedless
release remain outstanding. No unchanged expensive proof was replayed to repair
the cheap tests or documentation.

### Bounded test-construction cleanup

A subsequent test-only change removes duplicate work without changing compiler
or fixture bytes. All six SH01 type-only helper callers still construct genuine
fresh C5/C6/C7 products, but omit the unused SH09 binding seal. C8's existing
helper adds the unchanged seal and preserves its exact preimage and consumer
arguments. The function-recur selector retains every assembled-function and
runtime check, one standalone public compile per accepted input, the rejected
pair and all hostile cases. Its static public verification schedule falls from
36 to 16; each retained public operation still verifies freshly.

Helper flow checks pass two tests/27 assertions. Schedule, public-boundary and
existing recur checks pass four tests/135 assertions; these overlap the earlier
semantic regression and are not extra language coverage. The accepted actual
one-test/276-assertion composition remains attributed to the original test
revision in `55b7dca9`, not to a replay of the revised harness. There is no
measured speedup, new semantic feature or migration claim. The direct commands
and distinctions are in `docs/development-verification.md`.

### Ordered-Map false/Nil presence: affected composition coverage

Test-only `fc72bde2` validates real G/Q Map association outputs consumed by
lookup. Stored false/Nil remain explicit `Some` values; missing/empty inputs
produce `None` without `:value`. Insertion/replacement/order, persistence,
256-entry replacement versus new-entry rejection, malformed/late duplicate
validation and fresh producer/consumer/substitution checks pass. The original-
delegating observer sees one valid lookup and zero on rejected carriers.
C6 scalar/generated-Nil metadata motivates the DATA, not a client-linkage claim.

Prior `00e6e435` passed 5/596, including 338 new assertions. A three-line
positive-observer control successor freshly passed 1/342; four unchanged
controls retain 258 passes from the prior invocation. Superseded 338 are not
summed: selected coverage is 600 assertions, plus retained boundary 6/11,
or 11/611 across three passing invocations. LOC separately passed 2,022 files,
zero exceptions, maximum 250; the later test-only addition is outside its
implementation-source roots and leaves boundary policy inputs unchanged.
Production and fixtures remain unchanged from `26d05dbb`; the complete 4272
suite was not replayed. No RED, production repair, new language law, compiled
C6 client, public admission, native, migration or milestone claim follows.

### Bounded Keyword-key Map removal: request-module increment

The SH-22 request API executes `:ordered-map-dissoc-keyword` with at most 256
entries and actual Keyword keys/needle. Complete pair/key census and needle
classification precede user-key equality; full uniqueness precedes stable
omission. All remaining values/order and the original Map survive, including
false/Nil. Empty/missing cases remain value-equivalent. Possible allocation is
declared through `:memory/allocate` and `:bounded-by-result`, with no capabilities,
even if a missing-key result reports collection-value allocation `:none`.
No physical-sharing or global allocation-free promise follows. Four exports,
envelope, existing diagnostic families and opt-in `:meta` boundary remain;
the owner-local operation/manifest count is 22.

At `c04233a1`, the whole namespace passed 25/5224, including 602 new assertions
and eight added generic fixture checks. Boundary separately passed 6/11:
31/5235 across two passing invocations. LOC separately passed 2,022 files,
zero exceptions, maximum 250; all runners exited naturally. Prospective
802/5424 was an addition mistake, not a failed or corrected semantic run.
Real assoc-to-removal-to-get and original-Map lookup, full malformed/late
duplicate rejection, same-observer positive/negative controls, source identity
and fresh result recomputation/substitution are implemented.

W5's static removal calls motivate the operation only; semantic Keyword DATA
is not a physical-metadata redaction adapter or W5 linkage/admission evidence.
General/foreign key equality, stored-value selection/evaluation, global
callback freedom, native execution, migration and milestone claims remain
excluded. Verification still compares the complete recomputed result.

### Checked i64 subtraction in the experimental bootstrap library

The existing SH-22 Gravity library now executes binary `:checked-sub-i64` under
STD1/STD5/D8. Both operands must be integers in i64 range; pre-operation guards
reject overflow. For negative right operands, the accepted result is computed
through result-minus-one followed by one, avoiding the seed builtin's internal
negation of MIN. This covers MIN-MIN=0, -1-MIN=MAX and MIN-(-1)=MIN+1 without
changing the runtime interface. `STD5001` retains type/range/overflow failures
and `STD1001` retains wrong arity. The owner-local closed operation set and
manifest grow to 14 operations; experimental opt-in, `:meta`, pure/no-capability
and `:none` allocation policy remain explicit. Request/result schema and the
four exported entrypoints are unchanged.

The initial arithmetic RED ran one test/592 assertions with 428 failures. The
first GREEN ran two tests/75 assertions with one seed subtraction-overflow error;
later selected vars were unrun. The corrected focused batch passed eight
tests/864 assertions, including all 604 subtraction assertions and existing
addition, paired fixture, policy, rejection and verifier checks at 512 MiB.
Earlier partial/failing counts are not extra coverage. The language boundary
passed six tests/11 assertions and the Clojure LOC gate passed.

No compiler client linkage, native/provider execution, complete library closure,
authoritative migration or SH-22 completion follows. Historical SH07 source and
digest pins remain unchanged; their evidence does not authenticate this changed
library revision and must be renewed only for an actual admission decision.

### Checked i64 multiplication in the experimental bootstrap library

The same SH-22 Gravity leaf now executes binary `:checked-mul-i64` under
STD1/STD5/SAFE9/D8. Integer family/range checks precede zero handling and four
sign-quadrant quotient thresholds; divisors are nonzero and MIN/-1 is never
formed. Actual multiplication occurs only after the guard. The paired cases
exercise seed quotient behavior in both operand orders, MIN*0/MIN*1/MIN*-1,
MAX*1/MAX*2, signed endpoints and the exact square-overflow boundary. Existing
`:checked-i64`, `STD5001` type/range/overflow and `STD1001` arity behavior remain;
the owner-local operation set/manifest grows to 15 with experimental opt-in,
`:meta`, no effects/capabilities and `:none` allocation. The request/result
envelope and four exported entrypoints are unchanged.

The initial RED ran one test/1000 assertions with 766 failures and no errors.
The corrected first GREEN passed nine tests/1872 assertions at 512 MiB,
including all 1000 multiplication assertions and existing addition/subtraction,
paired fixture, policy, rejection and fresh result-verifier checks. The RED is
not extra coverage. Language-boundary checks passed six tests/11 assertions;
the Clojure LOC gate passed. No compiler client linkage, native/provider
execution, complete SH-22 API closure, authoritative migration or milestone
completion follows. Historical SH07 source/digest pins remain unchanged and
their evidence does not authenticate this changed source revision.

### Checked nonnegative i64 quotient: request-module increment

The existing four-export SH-22 request API executes
`:checked-nonnegative-quot-i64` for dividend `0..MAX` and divisor `1..MAX`.
Both actual Integer types and signed-i64 ranges, then nonnegative/positive
domain checks dominate division; the checked Result is integral and within
`0..dividend`. Existing arity/type/range diagnostics remain; local `STD5002`
reports domain rejection. The experimental opt-in `:meta` policy has 21
operations, no query effects/capabilities and `:none` value allocation; the
request/result envelope and four exports are unchanged.

At `26d05dbb`, the whole SH-22 namespace passed 23/4272: 480 new assertions and
3792 affected controls, including eight added generic fixture checks. Boundary
separately passed 6/11: 29/4283 across two passing invocations. LOC separately
passed 2,022 files, zero exceptions, maximum 250. All runners exited naturally.
RED `0d0c9dec` retained 240 passes/240 failures in 480 assertions without errors:
four failures were new nil-route test-oracle mistakes, corrected to the
unchanged `STD8001/:missing-io-authority` rejection. No general nil-route fix
or 240 prior semantic defects are claimed. Actual original `quot` delegation,
zero calls on rejected inputs, source/origin diagnostics, real checked-sub/mul
producer results and fresh producer/consumer/substitution checks are retained.

Reader decimal normalization supplies the motivating `(quot remaining 10)`
client, not a compiled-client linkage claim. Signed-negative rounding,
remainder, MIN/-1 and arbitrary-precision division are excluded. No reader or
runtime API rewrite, provider/native execution, public admission, migration,
milestone or SH-22 completion follows; historical source/digest pins are unchanged.

### Checked request Results compose into bounded decimal digits

Test-only `e592766e` executes real G/Q quotient-to-multiply-to-subtract-to-add-48
request Results for 9, 10, 11, 123 and MAX, with at most 19 iterations and exact
ordered digit codepoints. Zero is the driver's `[48]` branch with zero arithmetic
requests. Original-delegating observations, preserved inputs, fresh verification
of every request and meaningful result substitutions cover the producer-consumer
chain. A negative checked-sub result feeds a rejected quotient; existing range
and zero-divisor rejection remain. Production, fixtures and the API are unchanged.

The focused invocation naturally passed 5/3972: 1856 new assertions and 2116
affected arithmetic controls. Boundary separately passed 6/11: 11/3983 across
two passing invocations. LOC separately passed 2,022 files, zero exceptions,
maximum 250. There is no RED or production repair. After normal main reconciliation,
the test bytes and raw compiler/seed/library dependencies remained unchanged;
these are retained observations at `e592766e`, not a joined-candidate rerun.
No reader helper compilation/invocation or linkage, arbitrary-precision division,
signed rounding, Unicode admission, C6 proof, native or migration claim follows.

### Invalid UTF-8 count cannot authorize a stdout descriptor

At `150e21d9`, the existing stdout consumer requires an actual Integer recomputed
scalar count before matching supplied facts. This closes nil-equals-nil admission
for the selected invalid byte sequences while preserving prior authority,
carrier, encoding and byte-bound gates and the existing STD4001 fact-mismatch
diagnostic. Real validate-to-stdout Results retain their bytes and facts, including
empty text and multibyte endpoints. An original-delegating observer sees both
successful construction and rejection before a provider-request descriptor;
fresh producer/consumer verification rejects substitutions. No provider IO runs.

Original RED `06eb6226` naturally completed 264 assertions: 210 passes and 54
failures, with no errors. The unchanged regression then passed with the minimal
guard: 8/614 (264 new and 350 affected controls). Boundary separately passed
6/11: 14/625 across two passing invocations. LOC separately passed 2,022 files,
zero exceptions, maximum 250. No operation, API, fixture, manifest or authority
change follows. Other malformed-byte carrier failures, complete text/IO support,
provider/native execution and admission remain outside this repair.

### Special UTF-8 leads classify the second byte before range arithmetic

At `da5a9c55`, E0/ED/F0/F4 use the existing continuation-byte Integer classifier
before their original narrower second-byte ranges. Valid low/high endpoints,
third/fourth-byte order, diagnostics and earlier authority/bound priorities
remain. Real G/Q validate-to-stdout Results and fresh recomputation/substitutions
exercise both consumers without provider IO.

Original RED `9e1471c2` naturally completed 592 assertions: 312 passes and 280
failures, with zero uncaught test-runner errors. Test-local observation retained
64 actual public-call host exceptions (256 failed assertions) and eight accepted
Float-byte producer cases (24 failed assertions); these are distinct failures,
not an oracle correction. The guard successor passed 9/1206 (592 new plus 614
affected controls); boundary separately passed 6/11, or 15/1217 across two passing
invocations. LOC separately passed 2,022 files, zero exceptions, maximum 250.
No operation, schema, API, fixture, provider execution, complete text/IO,
universal host-failure freedom or migration claim follows.

### Keyword membership in bounded ordered Sets

The SH-22 leaf now executes `:ordered-set-contains-keyword` under STD2/STD3.
The query accepts the existing ordered-set carrier with at most 256 actual
Keyword members and a Keyword needle. Bounds and the complete family check dominate member
equality; complete uniqueness validation dominates a query hit, including a
duplicate after a matching prefix. Membership uses qualified Keyword value
equality, is insertion-order independent and preserves the original value.
`STD3001`, `STD10003` and `STD1001` retain bound, malformed/duplicate/family
and arity failures. The experimental opt-in `:meta` operation surface grows to
16; effects/capabilities remain empty and collection-value allocation is `:none`.
The four exports and request/result envelope are unchanged. Metadata overhead
does not establish native allocation-free execution.

An initial reader error executed no tests. Semantic RED then ran one test/248
assertions with 150 failures. First GREEN ran four tests/293 assertions with
eight failures from incorrect common-carrier expectations; four later selected
vars were unrun. Only those test expectations changed: opaque arguments reach
the family check and reject before uniqueness/search without payload invocation;
origin validation rejects separately. This is not universal opaque rejection or
host-printing-callback safety. Corrected GREEN passed eight tests/508 assertions
at 512 MiB, including all 248 membership assertions and existing collection,
fixture, policy, rejection and fresh verifier checks. Partial/failing counts
are not extra coverage. Boundary checks passed six tests/11 assertions and LOC
passed. General Sets, mixed/non-Keyword families, host-provider interop,
compiler linkage, native execution, complete SH-22 and migration remain open.
Historical SH07 pins are unchanged and do not authenticate this source revision.

### Bounded persistent Vector append and real consumers

The SH-22 leaf now executes binary `:vector-append` under STD3's clarified
persistent Vector law. It preserves the original ordered prefix and appends one
inert heterogeneous value. Input length 255 produces 256; full 256 and oversized
257 inputs reject before the constructor (`STD3001`). Non-Vector family and
arity retain `STD3002` and `STD1001`; allocation/authority policies remain
explicit. The experimental opt-in `:meta` surface grows to 17 operations, with
`:memory/allocate`, no capabilities and `:bounded-by-result`, including empty
input. The four exports and request/result envelope are unchanged.

The focused check runs real append-to-nth and append-to-assoc calls, recomputes
all results, preserves inputs and rejects value/fact/identity substitutions.
Original-delegating observations check one constructor entry after admission
and zero for rejected requests. The new dispatch and allocation discriminators
use exact keyword comparisons; nil retains the existing allocation-policy
rejection before append. The older general `sh22-member?` nil weakness remains
outside this correction.

Initial semantic RED completed 1/270 with 110 pass, 160 fail and no errors.
The first 8/538 GREEN is historical passing-but-incomplete coverage. Test-only
nil RED then stopped at 63 total assertions: 62 pass, zero fail and one host
stack error; later assertions in its sole selected var were unrun. A successor
using unsupported hosted-core `or` stopped at one total error assertion with
zero pass/fail and seven selected vars unrun. The supported nested-`if`
successor passed the complete 8/546 batch, including all 278 append assertions,
at 512 MiB. Boundary checks passed 6/11; LOC and both docs validators passed.
These epochs are not summed. The normal main join changed only disjoint
C13/SH16 code/tests and notes; executed SH-22 inputs remained byte-identical.

No variadic/coercing operation, physical sharing, unbounded-profile or new
static type/cardinality authority, host-callback safety, native allocation,
compiler linkage, complete SH-22, migration or self-hosting claim follows.
Historical SH07 source/digest pins remain unchanged and do not authenticate
this revised library source; renewal belongs to a later admission decision.

### Symbol membership after bounded ordered-Set insertion

The SH-22 leaf executes `:ordered-set-contains-symbol` under STD2/STD3 using
the existing ordered carrier with at most 256 actual Symbols and a Symbol
needle. Bound checks precede the complete family census; member/needle checks
precede equality and full uniqueness validation precedes a hit. Qualified
Symbol value equality is insertion-order independent, preserves the input and
does not invoke payloads or grant authority from spelling. Bound, malformed/
duplicate/family and arity failures retain `STD3001`, `STD10003` and `STD1001`.
The owner-local experimental opt-in `:meta` surface grows to 18 operations;
effects/capabilities are empty and collection-value allocation is `:none`.
The four exports and request/result envelope are unchanged. Exact new routing
does not add a nil acceptance or repair the older general membership helper.

At executed candidate `f399090274e013526566106b9e8fe63a2be17584`, the focused
batch passed 10 tests/1106 assertions at 512 MiB, including all 304 Symbol
assertions, existing Keyword membership/Vector append, paired fixtures, policy,
rejections and fresh verification. Real existing Set insertion outputs feed
the query for empty, absent and equal-present Symbol cases; both producer and
consumer results are recomputed. Qualified independently constructed values,
256/257 bounds, duplicate-after-hit, wrong families/needle, opaque payload,
nil-operation, authority and forged results are covered. RED remains a separate
1/304 epoch with 116 pass, 188 fail and no errors, not added coverage. Boundary
checks passed 6/11 and LOC passed; the final two notes do not alter executed
code/test/fixture or runtime/emitter dependencies from main `616c8039`.

No general Set/mixed-family support, host-provider or host-callback safety,
native allocation/execution, compiler client linkage, complete library closure,
migration, SH-22 completion or self-hosting follows. Historical SH07 source/
digest pins remain unchanged and do not authenticate the revised library source.

### Integer membership after bounded ordered-Set insertion

The existing SH-22 request API executes `:ordered-set-contains-integer` with
at most 256 actual Integer members and an Integer needle. Integer includes
supported values beyond signed64; no arithmetic, coercion or implicit width
proof is added. Bound checks precede the complete indexed Vector family
census; both member and needle checks precede equality, and full uniqueness
validation precedes a membership result. Reordered/empty Sets, reader codepoint
and compiler/runtime arity DATA, endpoint/beyond64 values and real existing
Set insertion outputs are covered, with fresh producer/query recomputation.
Late invalid members/duplicates, wrong needles, 257 values, malformed carriers,
authority, nil-operation and substituted results reject at their owning gates.
The opt-in experimental `:meta` policy has 20 operations, pure query effects,
no capabilities and `:none` collection-value allocation. Four exports and the
request/result envelope remain unchanged.

Unsupported-domain RED `a57d0a39` retained 184 passes and 368 failures in 552
assertions, without errors; it is not evidence of 368 prior semantic defects.
Candidate `684eb38d` passed the whole namespace, 22/3784. A helper-only 4+/4-
quality successor `969e0b41` replaces the new Vector-rest census with indexed
access and freshly passes 22/3784, including all 552 new assertions. These
passing epochs are retained separately, not summed. Boundary 6/11 and LOC
2,022 files/zero exceptions/maximum 250 retain their original `684eb38d`
execution binding: the helper-only change leaves their policy/file/line inputs
unchanged, so they were not rerun or relabeled. Selected coverage is 28/3795
across the V2 namespace and retained boundary invocations, with LOC separate.
The accepted C7/SH08-only main join preserves all six owned files and this
direct raw library/fixture execution path; no joined replay is claimed.

These are bounded request-module results, not ordinary source Integer-Set
membership, compiled reader/effects/emitter/runtime client execution, Unicode
ingress, general mixed-family Sets, host-callback safety, native allocation or
execution, public admission, migration, milestone or SH-22 completion evidence.
Historical source/digest pins are unchanged and do not authenticate this source.

### Bounded Keyword Set value equality

The SH-22 leaf executes `:ordered-set-equal-keyword` under the existing STD2/
STD3 value-equality law. Both existing ordered carriers are bounded to 256
actual Keywords. Both bounds precede the complete member-family censuses;
both domains precede member equality and both uniqueness checks precede any
count/membership result, including count mismatch or an apparent first match.
Under those established premises, equal counts and left-member inclusion
establish unordered Set equality. Empty Sets compare equal; both inputs and
inert payloads are preserved. `STD3001`, `STD10003` and `STD1001` retain bound,
malformed/duplicate/family and arity failures. The owner-local experimental
opt-in `:meta` surface grows to 19 operations with no effects/capabilities and
`:none` collection-value allocation; the four exports and envelope are unchanged.

Executed candidate `42ba1831412b06e97c045859268a84be80de2de9` passed the complete
11-test/1436-assertion focused batch at 512 MiB, including all 322 new equality
assertions, both membership queries, Vector append, fixtures, rejection and
fresh verifier controls. Real existing Set insertion outputs feed equality;
producer and consumer are recomputed. Empty/reordered/equal/different Sets,
qualified values, 256/257 on both sides, duplicate-after-match/count mismatch,
wrong families, nil/opaque inputs, arity/authority and forgery are covered.
Prior-production RED remains separate: 1/322, 150 pass, 172 fail, no errors.
Boundary passed 6/11 and LOC passed. The normal main `1b50ced9` join changed
only disjoint C13/SH16 code/tests and notes; executed SH-22 and runtime/emitter
dependency bytes remained unchanged. No general equality, native allocation/
execution, host-callback safety, linkage, migration or completion follows.
Historical SH07 pins remain unchanged and do not authenticate this source.

String membership is a distinct blocked dependency, not a completed operation.
L1 requires Unicode-scalar Strings; STD4 family classification supplies no
validation fact. SH-22 carrier counting and ordinary host equality can admit a
lone UTF16 surrogate. Existing strict UTF-8 primitives have fixed direct callers
and prohibit public runtime use, outside this six-path ownership. A checked
String ingress primitive or independently established validated carrier needs
its owning interface decision; neither host-String semantics nor new authority
was introduced here.

## Current Baseline

The repository has a working Clojure stage0, Gravity-authored compiler source
models, bounded stage2/stage3 application paths, and authenticated operation
slices through selected C, LLVM, and Wasm paths. The current governing evidence
still records:

- `:full-language-compiler-self-hosted? false`;
- `:clojure-seed-retired? false`;
- `:clojure-seed-boundary? true`.

Consequently, proof records, source inventories, public `check` bridges, and a
stage3 application demonstration do not by themselves complete a slice below.
A slice completes only when its named behavior is executed by the claimed
Gravity implementation and passes its exit gate.

## Uniform Slice Gate

Every slice from `SH-03` onward must satisfy all applicable items below before
it changes to `complete`:

1. The governing documents and exact claimed subset are named.
2. The implementation is executable Gravity behavior, not only a source model,
   manifest, proof record, fixture classifier, or Clojure implementation.
3. At least one real accepted `.gravity` input and its co-canonical `.qst`
   input traverse the slice.
4. At least one real rejected input per rejection family produces a stable
   structured diagnostic with the actual source path and no raw host exception.
5. Source spans, syntax identity, preserved compiler facts, effects,
   capabilities, safety classifications, and provenance survive as required by
   the governing pass contracts.
6. Artifact identities are deterministic and checkout-path neutral while
   retaining actual-path provenance outside identity inputs.
7. Mutation, substitution, stale-input, and malformed-graph probes fail closed
   at authenticated boundaries.
8. Focused tests, relevant regression tests, document validation, roadmap
   validation, coverage self-tests, and `git diff --check` pass.
9. The evidence names the remaining trusted computing base and makes no broader
   profile, target, release, performance, safety, or self-hosting claim.

Target-emission slices additionally require execution or validation by an
independent target toolchain. Bootstrap slices additionally require canonical
stage comparison, reproducible rebuild, and compiler-lineage evidence.

### P15-S23 Darwin launcher prerequisite

The bounded host-authored Darwin launcher is tracked as a prerequisite for the
P15-S23 native/runtime boundary, not as completion of a self-hosting slice. Its
development-verification node is `stage0-p15-native-launcher-prerequisite` and
runs `clojure -J-Xmx1g -M:test --namespace gravity.p15-native-launcher-test`
with Xmx1g, a 600-second timeout, fresh/no-resume execution, and
`authority: none`. The parent verifier owns the canonical
`/private/tmp/gravity-sh07-heavy.lock` (`exclusive: true`, `capacity: 1`,
`lock_owner: runner`) and records the observed process-tree resource receipt.
The node binds the launcher source, exact test, five C fixtures, and
`docs/artifacts/phase-15/native-launcher/p15-s23-darwin-launcher-primitive.edn`,
plus only the `deps.edn` and self-hosting test-runner tool inputs required by
the command identity. Legacy Stage0 broad owners exclude these paths, so each
source, test, fixture, or artifact change routes to the focused node and the
cheap `stage0-orchestrator-unit` prerequisite without selecting the broad
Stage0 suite or an earlier/later proof candidate.

Coordinator evidence is exactly 8 tests/60 assertions in 5.983s with peak RSS
213,712,896B under Xmx1g and the canonical lock; independent review is
approved and the artifact status is `partial`. This evidence is bounded and
non-authoritative. It makes no public native-run, self-hosted, release, or
strict process-containment claim, and does not change the SH-19 or P15-S23
completion state.

### P15-S23 native runtime provider prerequisite

The bounded native runtime provider is tracked as a narrow internal
prerequisite, not as a completed self-hosting slice or general native-target
support. It has two parallel fixed profiles. The fast
`stage0-p15-native-runtime-provider-contract-prerequisite` uses the exact
`gravity.self-hosting.sh07-iteration-cache-runner` command with explicit source
and test paths, `--fail-fast`, ten ordered vars (artifact preflight first),
`--max-cache-entries 1`, Xmx1g, and a strict 180-second timeout. It owns the
four shared C/Gravity/test/artifact inputs and the exact 22 legacy fixtures
(26 primary inputs), with an expected 10-test/235-assertion profile.

The authenticated
`stage0-p15-native-runtime-provider-packet-binding-prerequisite` uses the same
runner contract with five ordered vars (packet-binding preflight first),
Xmx8g, and a strict 1,800-second timeout. It owns the four shared inputs, the
exact packet binder, and four new fixtures (9 primary inputs), with an expected
5-test/70-assertion profile. Both nodes are fresh/no-resume, automatic,
`authority: none`, and depend only on `stage0-orchestrator-unit`; deterministic
IDs place contract before packet binding for shared changes. Explicit
`--check` closes only the selected profile plus the orchestrator; `--all`
includes both.

Both use `/private/tmp/gravity-sh07-heavy.lock` with `lock_owner: runner`,
`exclusive: true`, `capacity: 1`, and the observed process-tree receipt. Their
exact shared tool closure is `deps.edn`, the iteration-cache runner, the
self-hosting runner, `bootstrap.clj`, the packet binder, and its six direct
Gravity dependencies. Shared inputs, the binder, and eager namespace-load
helpers select both profiles; old fixtures select only contract, and the four
authenticated fixtures select only packet binding. Existing bootstrap and
six-helper consumers retain their prior Stage0/Stage3/Stage4 routes in addition
to both profiles. Exact exclusions remove provider-specific fixture and binder
replay without replacing genuine helper ownership. No M0, proof, public,
self-hosted, release, seedless, or containment authority is implied by either
provider profile.

The manifest binds exactly `GRAVITY_P15_NATIVE_RUNTIME_REQUIRED=1`. Without the
marker, a direct namespace run retains its unsupported-platform/no-claim skip;
the focused verifier supplies the marker and fails if the ARM64 macOS
Clang/file toolchain is unavailable. Each profile independently performs a
bounded no-follow, strict-UTF8, single-form artifact read with identity/size
checks and current C/Gravity/test hashes; packet binding also validates its
adapter and four authenticated fixtures. The artifact's current source-only
census is 15 tests/305 assertions. The prior ae9f 13-test/303-assertion
receipt, timings, RSS, hashes, and attempt history are explicitly historical
and non-authoritative, not a current runtime measurement. This evidence does
not grant public native execution, self-hosting, release, seedless, or strict
OS-containment authority.

## Countable Backlog

Status meanings:

- `partial`: useful implementation evidence exists, but the exit gate is not
  met.
- `queued`: no current claim is made that the executable exit gate is met.
- `blocked`: its dependency gate is not yet complete.
- `complete`: all entry, exit, validation, and evidence gates passed. Only the
  coordinator may assign this state after reviewing the evidence bundle.

| ID | Slice | Depends on | Current state | Executable exit gate |
| --- | --- | --- | --- | --- |
| `SH-00` | Stabilize the current authenticated Wasm comparison work and restore a clean baseline | none | complete | The current bounded C11-C14-B1-B4 comparison path is committed, externally executed, adversarially reviewed, and the worktree is clean. |
| `SH-01` | Establish parallel-safe module and test ownership boundaries | `SH-00` | complete | Central routing, compiler modules, target modules, fixtures, tests, generated evidence, and coverage accounting have explicit single owners; new tests no longer require unrelated edits to the monolithic bootstrap test file. |
| `SH-02` | Generalize authenticated pass and artifact envelopes | `SH-00` | complete | One reusable contract verifies source revision, semantic inputs, preserved facts, artifact lineage, deterministic identity, graph bounds, and stale or substituted inputs across at least two different compiler stages. |
| `SH-03` | Complete the bootstrap reader and literal policy | `SH-01`, `SH-02` | complete | The Gravity reader, rather than a Clojure reread, handles the complete claimed bootstrap syntax and literal subset with Unicode, newline, delimiter, abbreviation, metadata, extension, and malformed-literal diagnostics. |
| `SH-04` | Complete syntax objects, hygiene, and origin chains | `SH-03` | complete | Gravity constructs and serializes syntax objects with stable identity, scopes, marks, metadata, source spans, generated origins, and adversarial graph validation. |
| `SH-05` | Implement the bootstrap macroexpander in Gravity | `SH-04` | complete | Gravity expands the macro subset required by all compiler sources, preserves hygiene and origins, rejects phase/profile/capability violations, and matches accepted stage0 diagnostics. |
| `SH-06` | Implement namespace and binding resolution in Gravity | `SH-05` | complete | Gravity resolves compiler namespaces, aliases, vars, lexical bindings, imports, visibility, cycles, and unresolved references for the complete bootstrap source set. |
| `SH-07` | Implement core-form semantics and lowering in Gravity | `SH-06` | partial | Every core form required to compile the compiler lowers to a canonical core artifact with stable evaluation order, arity, mutation, recursion, exception, and pattern diagnostics. |
| `SH-08` | Implement the bootstrap type checker in Gravity | `SH-07` | partial | Compiler sources receive genuine type facts for primitives, functions, collections, control flow, calls, records/unions, and required meta-programming values; ill-typed variants fail closed. |
| `SH-09` | Implement effect, capability, and profile checking in Gravity | `SH-08` | partial | The compiler's `:meta` and release-target paths reject undeclared effects, missing authority, ambient access, hidden services, and illegal profile/target assumptions before backend lowering. |
| `SH-10` | Implement ownership, lifetime, initialization, and memory checks in Gravity | `SH-08` | partial | The bootstrap subset has explicit ownership and memory facts, rejects invalid aliasing, use-after-move, uninitialized values, invalid escapes, and unsupported allocation regimes, and preserves facts into MIR. |
| `SH-11` | Implement the safety and numeric-safety classifier in Gravity | `SH-09`, `SH-10` | partial | Each dangerous operation reaches exactly one D8 outcome; bounds, overflow, division, casts, unsafe islands, and residual checks are represented in checked core and diagnostics. |
| `SH-12` | Implement MIR construction and verification in Gravity | `SH-11` | partial | Gravity builds verified C11 MIR for the whole bootstrap subset, including blocks, values, calls, branches, loops, errors, effects, ownership, safety facts, and source provenance; malformed MIR is rejected. The fixed Stage7 development lane checks the frozen 253,588-byte C11 source and defines a six-selector C10-to-MIR adapter batch (helper first, authenticated boundary last). The aggregate receipt at `target/validation/stage7-c11-post-native-3/receipt.json` passed non-authoritatively in 490866.529 ms under the canonical command-owned lock: source 3/62 (runner 298 ms, wrapper 6722.023 ms, peak 522,780,672 bytes), public 2/39 (runner 350265 ms, wrapper 359909.526 ms, peak 2,789,851,136 bytes), and the exact combined adapter 6/235 (runner 80730 ms, wrapper 86737.375 ms, peak 1,892,941,824 bytes). All exited 0 with no skipped selectors and `authority: fresh-command-pass-non-authoritative`. These measurements were fresh on the prior 6084-based composition; coordinator changes since then alter the exact Stage7 tool input `bootstrap/clojure/test/gravity/self_hosting_test_runner.clj`, so this is historical non-authoritative planning/performance evidence, not current admission evidence. It is not proof, reviewed attestation, scoped authority, or release evidence; no C11 proof candidate was rerun. The final exact seven-node rerun is pending coordinator C12/SH13 freeze. The earlier separate 1/53 helper and 5/182 suffix receipts are superseded planning evidence. |
| `SH-13` | Close functions, calls, recursion, and control flow through MIR | `SH-12` | partial | Multiple functions with typed parameters and results, direct and indirect calls required by the compiler, recursion, branches, loops, joins, and error exits execute through verified MIR. The fixed Stage8 development graph adds a 512 MiB C12 source-shape gate, one cache-affine six-selector C11-domain evidence batch at 8 GiB, and one 2 GiB public C12 selector. All are automatic non-authoritative development checks on the canonical capacity-one lock; there is no C12 proof node, and the exact combined graph still requires a fresh post-composition run. |
| `SH-14` | Close compiler-required data, layout, and allocation through MIR | `SH-12` | queued | The bootstrap subset can construct and access the strings, symbols, keywords, tuples, records, variants, maps, sets, vectors, and bounded mutable storage used by the compiler with explicit layout and allocation facts. |
| `SH-15` | Complete compiler diagnostics as Gravity data | `SH-13`, `SH-14` | partial | All bootstrap-stage rejection families originate from Gravity diagnostic construction and preserve stable ids, facts, spans, origin chains, severity, and remediation across stage comparison. |
| `SH-16` | Implement required MIR optimization passes in Gravity | `SH-15` | partial | Gravity executes the passes needed by the bootstrap compiler, verifies declared preserved/invalidated/regenerated facts, and never erases a check without surviving proof or a residual runtime check. The fixed evidence-only Stage9 graph runs the two C13 source-shape selectors at 512 MiB, then four cache-affine C12-to-C13 boundary selectors at 8 GiB. C13/SH16 changes do not force Stage8 production; both nodes are automatic non-authoritative checks with no proof, public, lowering, optimization-credit, self-hosting, or release claim. Historical direct-command measurements are 2/41 in 1.187 seconds at 111,738,880-byte peak RSS and 4/69 in 60.797 seconds at 1,602,322,432-byte peak RSS; they are not current wrapper receipts, and a composed receipt-bearing run remains pending. |
| `SH-17` | Complete the target-lowering and backend interface boundary | `SH-16` | partial | A verified, authenticated, target-independent MIR artifact is accepted by a reusable C14/B1 interface that selects a declared target, ABI, runtime, providers, and artifact policy without source reinterpretation. The fixed Stage10 development graph runs a 22-selector static admission batch, one automatic direct carrier-mutation discriminator, an independent one-selector SH25 inventory node, and a three-selector SH25/SH26 fixture-consumer branch. The costly packet-substitution matrix remains manual and follows direct mutation in one cache-affine JVM. All nodes are non-authoritative; historical W1 measurements guide resource bounds but do not establish current admission, proof, attestation, release, or generalized performance claims. |
| `SH-18` | Emit and run one complete native compiler target | `SH-17` | queued | A genuine `:native` program containing typed arguments, arithmetic, functions, collections, and explicit stdout lowers through C or LLVM to an independently verified and executed native artifact. |
| `SH-19` | Implement the minimal self-hosting runtime subset | `SH-11`, `SH-17` | queued | Startup, panic/error reporting, bounded allocation or explicit no-allocation paths, strings/bytes, file reads required by the compiler, stdout/stderr, and process exit are provided through declared effects and capabilities outside Clojure. |
| `SH-20` | Implement release-grade artifact emission and provenance | `SH-17` | partial | Gravity emits objects/executables, source maps, manifests, compiler identities, dependency graphs, safety summaries, and canonical provenance consumable by independent tools. |
| `SH-21` | Make the compiler source legal and executable under `:meta` | `SH-15`, `SH-17`, `SH-19` | queued | Every authoritative compiler module compiles under the declared `:meta` contract with no ambient filesystem, network, shell, registry, or undeclared host authority. |
| `SH-22` | Implement the bootstrap standard-library core | `SH-13`, `SH-14`, `SH-19` | queued | The Gravity implementations of collections, text, numeric helpers, errors, IO wrappers, artifact data, and compiler meta-programming helpers needed by the compiler pass conformance and safe-wrapper gates. |
| `SH-23` | Implement the bootstrap package and hermetic build subset | `SH-20`, `SH-21`, `SH-22` | queued | Gravity reads the project/lock inputs, resolves the fixed bootstrap dependency graph, enforces build effects and capabilities, and deterministically builds compiler components without Clojure orchestration. |
| `SH-24` | Implement the self-hosted compiler driver and CLI subset | `SH-18`, `SH-19`, `SH-20`, `SH-23` | queued | A Gravity executable owns source loading, pass orchestration, diagnostics, target selection, artifact writing, `check`, `compile`, and the compiler-internal verification commands needed for bootstrap. |
| `SH-25` | Compile every authoritative compiler component with Gravity | `SH-21`, `SH-22`, `SH-24` | queued | The Gravity compiler builds the reader, syntax, macro, analyzer, checked-core, MIR, optimizer, lowering, backend, diagnostic, runtime-subset, standard-library-subset, and package/build components from their Gravity sources. |
| `SH-26` | Rebuild the complete bootstrap compiler with the prior Gravity stage | `SH-25` | blocked | Stage N compiles the complete stage N+1 compiler and its required runtime/library/build components without invoking Clojure in the candidate compiler, runtime, or build path. |
| `SH-27` | Prove stage equivalence and deterministic fixed-point behavior | `SH-26` | blocked | Two clean rebuilds and the stage N/stage N+1 comparison pass the declared artifact, manifest, diagnostic, conformance, runtime-output, IR-modulo-id, and reviewed-delta modes. |
| `SH-28` | Close trusting-trust, provenance, and TCB evidence | `SH-27` | blocked | Diverse or independently anchored rebuild evidence, acyclic compiler lineage, environment/lock/build identities, SBOM, signatures, revocation checks, unsafe audit, and a TCB delta show the Clojure seed outside the release boundary. |
| `SH-29` | Emit and verify the public seedless Gravity release | `SH-28` | blocked | The public `gravity` binary builds and verifies the compiler, runtime subset, standard-library subset, package/build path, and release executable; full bootstrap conformance passes; final artifacts record `:full-language-compiler-self-hosted? true`, `:clojure-seed-retired? true`, and `:clojure-seed-boundary? false`. |

Count: 30 slices (`SH-00` through `SH-29`). The first self-hosting claim is
permitted only after all 30 are complete. A task may be implemented early, but
it cannot be credited before its dependencies and uniform gate pass.

## Dependency Waves and Parallel Ownership

The backlog permits parallel implementation while retaining one integration
owner:

| Wave | Coordinator-owned integration | Parallel lane A | Parallel lane B | Parallel lane C |
| --- | --- | --- | --- | --- |
| 0 | `SH-00` to `SH-02`; central bootstrap routing and coverage | Read-only adversarial review | Native toolchain fixture design | TCB and equivalence inventory |
| 1 | `SH-03`, `SH-04`; source/syntax integration | `SH-05`, `SH-06` after syntax contracts stabilize | `SH-08` type data model and fixtures | `SH-18` native ABI/toolchain harness without shared lowering edits |
| 2 | `SH-07`, `SH-12`; checked-core/MIR integration | `SH-08`, `SH-09` | `SH-10`, `SH-11` | `SH-19`, `SH-20` runtime/artifact leaf modules |
| 3 | `SH-15` to `SH-17`; pass and backend integration | `SH-13` functions/control | `SH-14` data/layout | `SH-18` native emission |
| 4 | `SH-21`, `SH-24`; compiler-driver integration | `SH-22` standard-library core | `SH-23` package/build subset | Stage comparison and mutation suites |
| 5 | `SH-25` to `SH-29`; bootstrap/release authority | Independent rebuild | Diverse verification | Provenance, SBOM, signing, and governance review |

Deus owns shared authority, review and integration decisions, completion
attestations and final status decisions. Named path owners implement their
actual artifacts; the local executor performs approved main operations.
A manager owns only its explicitly allocated files, fixtures and
tests; this wave table grants no implementation reservation. Managers may
delegate bounded disjoint work within that allocation using `gpt-6.1-sol` at
medium or higher effort. Delegation does not confer independent acceptance or
additional machine capacity. No manager edits peer files or regenerates global
artifacts without an exact coordinator allocation.

### Finite SH07 manager queues

The [existing C01-C18 plan](self-hosting-sh07-completion-plan.md#three-layer-ownership-and-finite-queues),
owned for scope/Core closure by `01a16a54-eeb1-7941-ac35-7be30e47bdc7`,
is the current finite closure plan, with accountable managers, the actual
required source/use population and targets, semantic acceptance, exact unknowns and stopping
rules. It is not another runner, lane, schema or admission system. All 18 tasks
remain OPEN. C01-C11/Core variants, C12 construction, C13 public ingress, C14
affected C7 composition, C15 subset negatives, C16 current upstream evidence,
C17 authoritative migration and C18 completion are distinct responsibilities.

Managers curate concrete items without a fixed queue/task-count cap, each with its demonstrated gap,
contracts, exact allocated paths/tests/fixtures, input/output, prerequisites,
accepted/rejected checks, completion and stop conditions. Work states are
`executing`, `resource-queued`, `dependency-blocked`, `awaiting-review`,
`ready-for-integration` and `queue-empty`. A frozen reviewed candidate stays
unchanged; another eligible item needs disjoint isolation and existing scope.
Managers derive useful in-scope successors and notify the coordinator; new
cross-slice allocations still require its ownership decision. No join-all
barrier or invented feature is justified to keep a manager busy.

The standing finite-goal charters apply to the implementation managers.
Managers independently
audit repository/contracts/evidence/tests, discover and prioritize confirmed
dependency-ready work. Within unchanged exclusive ownership, no per-task
prewrite notice, ticket, permission receipt or preparation handoff is required.
Communicate actual ownership/interface changes, material blockers and frozen
review candidates through the existing bridge/records. Coordinate genuine
conflicting writers before changing contested paths; independent work continues.
Self-pull eligible work in the same active turn; one
blocked leaf does not block the whole goal, and an immutable review candidate
does not block disjoint work in safe isolation. Report material completion and
next outcomes without a mandatory pause or handoff ceremony. A drained queue
does not justify default NEED_REFILL;
truly exhausted goal-wide alternatives require an evidence-backed frontier to
Deus, naming the exact owner/action/wake event. The denied advisory wake/handoff
must not be retried, transferred or made a global gate. No daemon, filler
inventory or new cadence follows. The old coordinator hourly task is PAUSED;
Gravity Progress reporting is unchanged. Deus decides review/integration and
the local executor performs only delegated operations.

At `0067e9418de992900a95cd47d016029af62ff99c`, Core completed the allocated
actual API trace and self-pulled the selected-owner seed-plan map/test, then
this corresponding C01 evidence/queue update in the same turn. The
[C01 result and exact plan table](self-hosting-sh07-completion-plan.md#c01-selected-owner-seed-plan-use-map-at-0067e941)
and [EDN map](artifacts/phase-15/reports/sh07-c01-selected-owner-use-map-v1.edn)
join all six selected seed plans to 12668 emitted builtin-call occurrences,
with original definition spans/body paths and actual plan/function identities.
The reader's 231-function closure and 44 excluded functions remain explicit;
potential compiled use does not mean every function was invoked. Rule DATA,
emitter freshness and the complete 2146-resource seed content identity are
bound separately. Runtime primitive accepted/rejected case joins, actual
reachability, Core products and external migration identities remain open.
One normal 2GiB/120s-watchdog batch passed 13 tests/40 assertions in
11730.156792 ms (new 2/17, boundary 6/11, LOC 5/12). Missing/duplicate input,
changed source and forged identity/unknown-instruction record checks reject;
they are not compiler source-rejection or build evidence. The parent namespace
preflight failure remains failed in the plan's raw-evidence record. Core's
four-path candidate was subsequently integrated in PR304; no C01/C-task, native
or migration credit, administrative-only PR or global gate follows. Earlier queue snapshots below
retain their original revision scope; they are not a current refresh of every
manager's work.

Core's next confirmed C01 task at `80881ba6` joined the actual used arities to
existing runtime case tests. One exact selection passed 11 tests/294 assertions
in 14632.794333 ms and left concrete positive-case gaps: binary `<=`/`>`/`>=`,
`assoc` arities 7/9/19/21, binary `conj` and zero-argument `list`. Core self-pulled
the owning selected-owner test for those observed rows. The separate changed
batch passed 12 tests/98 assertions in 7903.280625 ms (new test 1/75,
boundary/LOC 11/23). Both used normal 2GiB/120s checks and natural zero exits;
the two executions must not be presented as a newly executed aggregate.
The [primitive-case supplement](self-hosting-sh07-completion-plan.md#c01-primitive-runtime-cases-at-80881ba6)
and existing EDN now link fully qualified case selectors, exact provenance,
scoped accepted/rejected cases and remaining obligations for each used
primitive. Every observed arity has a normal-value positive case, but source
value domains, actual owner invocation, Core products and later legality
remain OPEN. No new primitive/runtime implementation, shared interface,
allowance, administrative-only PR or milestone credit follows. The prior
two-slot helper candidate remains separately immutable for ROOT review;
the new case-evidence candidate uses an isolated worktree while ROOT's
integration checkout remains untouched.

The following self-derived C01 gap was non-call instruction positioning:
61087 selected instructions had only 16071 positioned call rows. The existing
walk now records all 61087 structural source/plan paths, including the 45016
non-call instructions, with the existing hosted form kind. One actual changed
batch passed 15 tests/132 assertions in a recorded 16887.307333 ms through test
completion (six-owner joins 1/16, record negatives 1/13, quote/reference controls
1/5, runtime arities 1/75, boundary/LOC 11/23), under normal 2GiB/120s checks.
The [instruction-position record](self-hosting-sh07-completion-plan.md#c01-structural-instruction-positions-at-d42f23b2)
preserves original plan/case evidence and separately binds the fresh local
composition, input dependencies and physical paths. Quote payloads remain
unvisited; symbol-to-literal projection does not acquire binding authority.
Missing/duplicate/forged rows reject as record checks, not language rejection.
These paths are neither C2 IDs nor canonical Core products; source domains,
binding/definition links, actual invocation and later legality remain OPEN.
This is substantive ordinary in-goal work, with no revived task-count,
attempt-grant or preparation-only gate and no milestone credit.

The next C01 audit found missing declaration signatures in the selected call
map. All 964 selected functions now retain ordered parameters and fixed arity;
all 3403 local calls match the actual source/plan declarations. The changed
batch passed 16 tests/143 assertions under normal 2GiB/120s checks, including
real seed-plan wrong-arity record rejection and forged declaration controls.
The [declaration-join record](self-hosting-sh07-completion-plan.md#c01-selected-local-call-declaration-joins-at-af04fecc)
and existing EDN preserve earlier rows/evidence, the first admission timeout
before test execution, and explicit corrections for two successful-run
metadata labels. This establishes C01 record joins, with B48 legality, C5
binding authority, C7, execution, full Core products and migration still OPEN.

The next C01 required-use audit covers all 1008 normalized function bodies,
including the 44 reader definitions outside the selected seed plan. It finds
61542 body positions: the existing 61087 selected positions plus 455 outside.
The [full-body requirement matrix](self-hosting-sh07-completion-plan.md#c01-full-function-body-requirement-matrix-at-6585164c)
records concrete control/literal/call families, source-only locators and
verification-open product/rejection joins. It finds no executable mutation,
error/handler, match or nested-function body form, without granting any
broader SH07 support exemption. Qualified reader name membership is not C5
authority. Top-level DATA/declarations and quoted interiors remain separate
unclosed source regions; no full-source or migration credit follows.

The following C01 declaration/leaf audit adds the six namespace headers and
37 top-level DATA declarations to the source inventory. It also closes a
demonstrated test-record gap: after plan/function rehashing, a Bool value change
and redirect to another existing parameter had been accepted despite unchanged
source. Direct literal/local source checks now reject both. The current-seed
composition including `f2bb3a35` passed 20 tests/207 assertions; its host subtree
matches main's new Symbol/reader-policy TCB. The
[declaration and leaf record](self-hosting-sh07-completion-plan.md#c01-source-declarations-and-leaf-equivalence-at-c7d31338)
keeps all prior fields, exact before-fix/timeout history and new dependency
bookends. This is C01 source-to-seed validation, not public Core/C5 acceptance,
runtime argument-domain proof, migration or C-task completion.

The current 2026-10-24 accounting base is inspected remote main
`c4b1d19da11def44f835d552e44d06488d31fe46`, tree
`a4145adf64c5c7d4ebc558c605f07d27f784cac5`. Earlier `64aa890a` queue/adoption
observations are historical. The plan's
[merged-increment table](self-hosting-sh07-completion-plan.md#merged-increments-and-current-composition-frontier)
binds exact revisions and scope: PR294 helper G composition passed 1/133, Q
text parity only; PR295's lawful 13 spelling repairs have separate 3/47
current-reader/seed-leaf and 11/23 boundary/LOC executions; PR296's loop
selection batch passed 15/102; PR297's if/do selection batch passed 17/314.
Their original failed pin/EOF/delimiter attempts remain failed. These scoped
changes do not complete full B51/H6, current migration, native support or a
C-task, and retained changed-worktree evidence is not a fresh merged-main run.

Core's two-document `fc29cdd4` accounting is independently accepted; this
bounded status successor is frozen for review and substantive bridge bundling.
It creates no standalone administrative PR/count credit or global gate.
Construction's exact disjoint two-existing-`00581`-file/new-test options bridge
is frozen at `1af5fd81`/`39ad9017`, awaiting integration after independent CODE
and EVIDENCE acceptance (full tuple in the linked plan): legacy public
arities retain the old fresh callee/two arguments, while explicit-options
arities alone use the policy-aware fresh callee/three arguments. Keep original
source/report validation, the exact typed-input shape and local/default
ceilings; no new schema, wrapper or cache. Its 228-byte minimal-Map-get
producer/fresh-policy-verifier prewrite probe passed within the normal bound;
that is not DSE fit or completion of bridge integration. Separate actual
groups passed at 1/19, 1/17 and 15/135 (17/171 across three runs, not a new
aggregate execution). Original positive V1/V2 aggregates remain FAILED and
the corrected current positive selector is UNRUN. Independent unchanged
production/dependency/passive-observer conservation supports only V2's
actually completed stock operations and 16 passing assertions in their
original scope; no current positive aggregate is inferred passing.

SOURCE's frozen `f3073c24`/`d188d977` has independent focused CODE/EVIDENCE
acceptance for 13/678, but the actual 317-byte fixture whole-default run FAILED:
1 test, 0 passed assertions, 1 failure, 1 error, 126284 ms parent duration.
C7 work-limit reports preceded the deliberate stop; fresh C7 verification,
public C8, assembly and runtime were UNREACHED. Preserve raw evidence at
`/private/tmp/source-dse-full-f3073c24-20261024a`. First printed is not proven
first internal refusal; minimum N and that failed run's global-versus-local
cause remain unresolved.
The existing PR291 trusted-caller cumulative policy makes default 65536 a
default, not a universal semantic requirement; it does not remove local
ceilings or establish fit. SOURCE's producer-only diagnostic subsequently
accepted the genuine 317-byte source at existing N=1048576, five rounds/bound
34: observed ordinary pool 220380 charged/828196 remaining, largest observed
local DSE charge 31728 within unchanged 65536, 22 rows/zero drops/no observer
error. Parent/child exited zero in 119415 ms parent duration with cleanup
complete; raw evidence is at `/private/tmp/source-dse-producer-f3073c24-20261024b`.
This is not minimum N, full C7 total work or fresh-verifier/C8/assembly/runtime
evidence. The producer-only classification is independently accepted; the
whole-default failure remains FAILED. Compact architecture work continues;
at that historical snapshot the next Effects 180s local-fit proposal had not
executed. Its preparation-only/no-GO process status is superseded; ordinary
owned method implementation/checks need no per-run approval within actual
machine/resource and correctness safeguards. This records no later run result.
Original-request context already
exists on the selected three-argument body-effects route; the detached
two-argument route is unsupported for compact GIS, not a reason for a new API.

Cohort's planning matrix is returned/queue-empty while C16 remains OPEN. The
MIR Integer/source-fact and diagnostics producer-representation dependencies
remain; optimization/standard library receive no invented implementation
queue. Compiler C11/C13/C15 manager names are not SH07-C11/C13/C15 assignments.
All ten ordinary-autonomy recipients have adopted; this observation does not
establish sustained throughput. SOURCE and Construction continue independently;
this accounting is not their execution prerequisite.

These are planning/reconciliation deliverables and honest queue dispositions,
not compiler completion or measured sustained throughput. Exact source/test/
interface reservations remain with their existing writers. Two normal broker
leases, one memory-heavy lease and exclusive checks holding all slots remain
the host capacity. Use the existing coordination root and applicable canonical
heavy lock; extra agents do not add slots. Heavy/native/bootstrap/proof/
performance resource safeguards and scope boundaries survive; historical
grants are not ordinary attempt budgets. GIS/H6 claims retain their actual
technical/provenance requirements; compact representation/validation/fees/identity/version and
independent consumer evidence remain technical obligations, not discretionary
human-approval labels. Ordinary eligible work within the
allocation does not require a new per-command approval loop.

### Advisory process review

The existing independent process reviewer
`01a17047-4e39-7013-9490-6feb82746d14` is advisory, not a writer, scheduler,
integrator or global gate. Material process decisions, stalled lanes and
integration outcomes travel through the coordinator to that reviewer.
Actionable findings return only to the coordinator, who records an explicit
disposition: accepted/assigned; rejected with evidence; deferred with a named
dependency/event; or bounded evidence requested. The reviewer independently
investigates, recommends and tracks dispositions/outcomes; only the coordinator
changes manager priorities, instructions and allocations. Managers report to
the coordinator, not peers or the reviewer.

The remit includes unnecessary dependencies/serial handoffs, ownership,
interfaces/goals, approval loops, stale proof/governance burden, queue
starvation, tool access, resources and genuine technical blockers, including
tests, builds, compiler behavior, interfaces, integration and toolchains.
Necessary correctness, security, proof and resource gates remain; no blanket
bypass is implied. The coordinator reconciles plan, operating memory, the saved
heartbeat and all manager acknowledgments before reporting orchestration
migration complete. The old coordinator hourly task is PAUSED, with no
replacement; configuration alone does not establish sustained throughput.
At the frozen PR293 handoff, the reviewer charter/event had been sent with
exposed state `waitingOnApproval` and no pending-operation details; adoption
was pending. Subsequent to PR293, ROOT observed explicit reviewer adoption and
ACTIVE state with no approval flags, followed by an idle/three-findings
observation. Those states are historical. The latest supported snapshot flags
`waitingOnApproval` without a specific operation/request exposed. Do not
claim current unblocked status, approve an unknown action or bypass platform
controls. Advisory review remains non-global; disjoint ordinary work continues
and the coordinator handles any actual exposed action. This status sync does not claim
completed investigation, independent acceptance of this candidate or sustained
throughput.

## Progress Accounting

Keep manager-queue progress separate from executable implementation, affected
producer/consumer integration and authoritative migration. The current Core
plan migration, C12 retained construction reconciliation and C16 invalidation
matrix do not close a compiler task or change the historical slice counts
below. All C01-C18 remain OPEN until their actual criteria and independently
routed acceptance pass. SH-05.1, SH-05.6 and SH-06.6 remain partial OPEN;
the current resolver remains development-unadmitted. Preserve original failed,
interrupted, skipped and unsupported results and exact revision bindings.

Progress is reported as four numbers rather than a percentage inferred from
commit count:

```text
complete slices / 30
executable Gravity-owned slices / 30
slices with Clojure in their claimed execution boundary / 30
blocked slices / 30
```

The coordinator should also report the highest continuous completed prefix.
For example, completion of `SH-18` does not advance the prefix past `SH-07` if
`SH-08` remains incomplete.

Retained reviewed accounting after `SH-06` (not renewed by these increments):

```text
complete slices: 7 / 30
executable Gravity-owned slices: 6 / 30
slices with Clojure in their claimed execution boundary: 7 / 30
blocked slices: 4 / 30
highest continuous completed prefix: SH-00 through SH-06
```

`SH-02` credit is intentionally bounded to envelopes whose explicit reference
closure has at most 128 nodes, 128 edges, and shortest-discovery depth 64. The
Gravity module owns descriptor validation, semantic template construction, and
fresh template replay for both C13 and B1. Canonical encoding, SHA-256 request
resolution, identity equality enforcement, source reads, and final contextual
reconstruction remain in the declared Clojure stage0 boundary; `SH-02` does not
retire that seed or claim release-signature or verifier-correctness proof.

`SH-03` credit is bounded to the bootstrap reader and literal subset exercised
by its co-canonical fixture matrix and authoritative reader-source proof. The
Gravity reader owns Unicode-scalar decoding, newline and delimiter structure,
the ordered token stream, recursive form graph, bootstrap literals,
abbreviations, metadata, registered `inst` and `uuid` extensions, bounded
semantic-value reconstruction, and structured rejection. The public read, C2,
C3, P15, and check ingress consumes the authenticated Gravity result without a
second target-source read. The Clojure stage0 boundary still owns the initial
byte snapshot, compiler-plan execution, canonical digest resolution, the C2
compatibility adapter, central routing, and final artifact construction. The
compatibility facade is explicitly uncredited, and the remaining C2 boundaries
are the full-language literal surface, full-language abbreviation surface,
full-language extension registry, and host/seed retirement. This slice does not
claim overall C2 completion, compiler self-hosting, seed retirement, or release
readiness.

`SH-04` credit is bounded to the syntax-object, hygiene, metadata, source-span,
generated-origin, serialization, and graph-validation subset exercised by its
co-canonical fixture matrix. The Gravity syntax module constructs and verifies
the claimed products, preserves deferred ratio descriptors and exact SH-03/P15
reader lineage, and emits checkout-path-neutral syntax and artifact identities
while retaining actual paths in provenance. The SH-03 descriptor is freshly
verified through the reusable SH-02 envelope contract and independently bound
back to the current C2 semantic products; paired provenance alteration,
same-path cross-source replacement, stale-input, and malformed-graph probes
fail closed. The Clojure stage0 boundary still owns compiler-plan execution,
digest resolution, envelope binding, the C2/C3 compatibility adapter, and
central routing. This slice does not claim overall C3 completion, full compiler
self-hosting, seed retirement, package refresh, or release readiness.

`SH-05` credit is bounded to the compiler-required `defn` expansion subset
executed across the 36 authoritative compiler modules and their measured 1,276
top-level function definitions. The Gravity macro module owns expansion
recipes, hygiene marks, generated origins, build-effect and capability policy,
ordered expansion traces, and all ten structured C4 rejection families. Exact
stage0 body comparison, both source extensions, path-neutral identities,
literal-map preservation, replay and substitution checks, and complete
first/middle/last stream reconstruction passed. SH-04's implementation-local
syntax-stream ceiling is now 2,048 products, the exact capacity of its current
16-item chunks under the canonical width limit of 128; 2,049 is rejected before
downstream use, and aggregate stream carriers are checked separately. The
Clojure stage0 boundary still owns source reads, compiler-plan execution,
canonical digest resolution, envelope binding, compatibility routing, and final
artifact assembly. User-defined `defmacro`, the full macro surface, hierarchical
syntax-stream paging beyond 2,048 products, retained standalone carrier-audit
records, compiler self-hosting, seed retirement, packaged CLI refresh, and
release readiness remain outside this credit.

`SH-06` credit is bounded to the namespace and binding resolution subset
executed across the measured 41-path authoritative compiler-source cohort and the
co-canonical accepted/rejected fixture matrix. The Gravity
`gravity.resolution` module owns namespace, alias, import, var, lexical
binding, visibility, shadowing, dependency-cycle, profile, target, capability,
and foreign-boundary policy; deterministic binding and product identities; and
all ten structured C5 rejection families. Fresh authenticated SH-05 input,
exact Gravity replay, complete resolution-product reconstruction,
checkout-path-neutral identities with actual-path provenance, substitution and
mutation rejection, public C6 consumption, and bounded serialization passed.
The largest measured authentic artifact contained 19,445,399 carrier nodes at
depth 45 and maximum width 66,339 and serialized to 284,437,819 UTF-8 bytes.
Per-component transport limits are 33,554,432 nodes, depth 64, and width
131,072; whole-artifact limits are 67,108,864 nodes, depth 64, width 131,072,
and 1,073,741,824 serialized bytes. The Clojure stage0 boundary still owns initial
source reads, compiler-plan execution, canonical digest resolution,
authenticated-envelope binding, compatibility adaptation and central routing,
and final artifact assembly. That 41-module cohort was the measured bootstrap
source set at credit time; the current inventory contains 42 authoritative
modules after the additive B48 source proof. Neither count is a broader
full-language claim, and the compatibility C5 facade
receives no implementation-authority credit. This slice does not claim overall
C5 or C15 completion, full compiler self-hosting, Clojure seed retirement,
packaged CLI refresh, or release readiness.

`SH-07` remains partial. Its source-level authenticated evidence now covers all
42 authoritative compiler modules, including the complete bootstrap reader
source. For that reader source's earlier gate, Gravity consumes the authentic
SH-06 fragment, binding, and resolution products for 22,209 forms in 298
fragments, lowers 20
qualified definitions and 30 qualified calls, and preserves 24 quoted forms as
data rather than executable references or calls. The measured authenticated
request contains 1,446,007 carrier nodes at depth 25 and maximum width 22,209;
its exact UTF-8 scalar payload is 24,579,750 bytes. The generated core template
contains 9,172,831 nodes and 522,603,428 scalar bytes, and its generated digest
requests contain 6,695,903 nodes and 372,860,236 scalar bytes. Request ingress
is bounded at 8,388,608 nodes and 268,435,456 scalar bytes; template and
resolved-core output are independently bounded at 16,777,216 nodes and
1,073,741,824 scalar bytes; generated digest output is independently bounded at
16,777,216 nodes and 1,073,741,824 scalar bytes. All four boundaries also enforce
depth 256 and width 65,536. Request ingress is checked before lowering, and
each generated product is checked before downstream recursive hashing,
verification, or use. Stable path-neutral core identities, actual-path
provenance, fresh digest resolution, replay, and altered input, graph, binding,
resolution, template, digest, and resolved-product rejection are required by
the authoritative proof. The Gravity C6 core-lowering source is also covered
directly: its 9,109 source bytes authenticate into 7 fragments containing 348
forms, 289 bindings including 27 local bindings, and 13 resolutions, then lower
to 220 canonical core nodes with 7 definitions and no calls, references, or
keyword lookups. Its cache-free authoritative proof passed with 32,888 carrier
nodes at depth 8, width 348, and 498,205 UTF-8 scalar bytes; co-canonical
`.gravity` and `.qst` inputs retain equal semantic identities and distinct
actual-path provenance. This independent zero-lookup source does not broaden
the existing nine-module, 63-lookup B16 cohort. The Gravity L2 core-language
semantics source is covered by the same path: its 17,557 bytes authenticate
into 9 fragments containing 600 forms, 286 bindings including 24 local
bindings, and 15 resolutions, then lower to 465 canonical core nodes with 9
definitions and no calls, references, or keyword lookups. Its cache-free
authoritative proof passed with 44,353 carrier nodes at depth 8, width 600, and
706,674 UTF-8 scalar bytes; its co-canonical source-extension and provenance
gates also passed. The C4 compatibility source adds 4,053 authenticated source
bytes in 4 fragments containing 232 forms, 270 bindings including 8 local
bindings, and 70 resolutions. It lowers to 201 canonical core nodes with 4
definitions, 31 calls, 50 references, 13 conditionals, and 3 functions. Its 11
`get` operators are ordinary resolved symbol calls rather than keyword-headed
map lookups, so its canonical keyword-lookup product count is zero. The
cache-free authoritative proof passed with 29,163 request carrier nodes at
depth 17, width 270, and 427,463 UTF-8 scalar bytes. Co-canonical extension,
provenance, replay, and alteration gates passed, and the B16 cohort remains
unchanged. The C3 compatibility source adds 4,149 authenticated source bytes in
4 fragments containing 238 forms, 270 bindings including 8 local bindings, and
70 resolutions. It lowers to 207 canonical core nodes with 4 definitions, 31
calls, 50 references, 13 conditionals, and 3 functions. Its 11 `get` operators
are likewise ordinary resolved symbol calls with zero keyword-lookup products.
The cache-free proof passed with 29,439 request carrier nodes at depth 17,
width 270, and 432,437 UTF-8 scalar bytes. The source remains explicitly
compatibility-only: it grants no authentication credit, does not supply the
authoritative syntax result, and retains the `gravity.bootstrap.syntax` route.
The C5 compatibility source adds 4,154 authenticated bytes with the same
4-fragment, 232-form, 270-binding, 70-resolution structure as C4. It lowers to
201 canonical core nodes with 4 definitions, 31 calls, 50 references, 13
conditionals, and 3 functions. Its 11 `get` operators are ordinary resolved
calls with zero keyword-lookup products. Its cache-free proof passed with
29,163 request carrier nodes at depth 17, width 270, and 428,299 UTF-8 scalar
bytes. The source remains compatibility-only, denies authentication and
authoritative resolution credit, and retains the `gravity.resolution` route.
The C15 diagnostics source adds 6,580 authenticated bytes in 5 fragments
containing 218 forms, 279 bindings including 17 local bindings, and 9
resolutions. It lowers to 122 canonical core nodes with 5 definitions, 2
functions, and 2 quoted descriptor bodies, with no calls, references, or
keyword-lookup products. Its keyword-rich diagnostic maps therefore remain
quoted data rather than executable lookup operations. The cache-free proof
passed with 26,264 request carrier nodes at depth 8, width 279, and 382,555
UTF-8 scalar bytes. This proves source representation only: Clojure stage0
remains the declared seed/compiler/verifier boundary and no executable
diagnostic, rendering, redaction, stream-verification, or runtime-authority
credit is taken. The C16 incremental-compilation source adds 24,810
authenticated bytes in 17 fragments containing 833 forms, 331 bindings
including 69 local bindings, and 29 resolutions. It lowers to 547 canonical
core nodes with 17 definitions: 11 data contracts and 6 quoted function
bodies. Its keyword-rich descriptors produce no calls, references, or
keyword-lookup products. The cache-free proof passed with 57,611 request
carrier nodes at depth 8, width 833, and 945,725 UTF-8 scalar bytes. This
proves source-model representation only and grants no cache construction,
storage, invalidation, revalidation, proof reuse, filesystem/network, or
release-grade incremental authority. The C17 plugin/pass API source adds 25,254
authenticated bytes in 17 fragments containing 878 forms, 329 bindings
including 67 local bindings, and 27 resolutions. It lowers to 603 canonical
core nodes with 17 definitions: 12 data contracts and 5 quoted function bodies,
with no calls, references, or keyword-lookup products. The cache-free proof
passed with 59,392 request carrier nodes at depth 8, width 878, and 974,774
UTF-8 scalar bytes. This proves source-model representation only: production
plugin loading and pass execution remain disabled, central integration remains
pending, and no sandbox, trust, whitelist, domain, cache, filesystem/network,
or release authority is credited. The B16 cohort remains unchanged. The
SH-21 meta-compiler-legality source adds 28,374 authenticated bytes in 42
fragments containing 2,778 forms, 419 bindings, and 1,109 resolutions. It
lowers to 2,292 canonical core nodes with 42 definitions, 459 calls, 848
references, 151 conditionals, 26 local bindings, and 42 function forms. Its 161
`get` calls divide into 139 literal-keyword calls and 22 dynamic-key or index
calls; all are ordinary resolved calls, so the canonical keyword-lookup product
count remains zero and the B16 cohort remains frozen at nine modules and 63
lookups. The cache-free proof passed with 199,593 request carrier nodes at depth
27, width 2,778, and 3,301,719 UTF-8 scalar bytes. It also proves exact
forward/mutual-recursion binding identity, co-canonical extension parity,
path-neutral identity, actual-path provenance, replay, and altered-product
containment. This is bounded source execution evidence only: authenticated
SH-15 diagnostics, SH-17 lowering, SH-19 runtime services, whole authoritative
compiler execution under `:meta`, and seedless execution remain pending. The
C18 compiler-verification source adds 30,982 authenticated bytes in 22
fragments containing 1,148 forms, 394 bindings, and 42 resolutions. It lowers
to 640 canonical core nodes with 22 definitions: 12 data contracts and 10
quoted function bodies, with no calls, references, or keyword-lookup products.
The cache-free proof passed with 75,762 request carrier nodes at depth 8, width
1,148, and 1,266,488 UTF-8 scalar bytes. The proof pins all quoted artifact
schemas, their actual omissions from declared required fields, the absent
replay builder, the nine-entry diagnostic catalog, the source-owned verifier
checklist, co-canonical extension parity, path-neutral identity, actual-path
provenance, replay, and altered-product containment. This proves source-model
representation only: production verifier execution, schema enforcement,
certificate and evidence checking, translation validation and replay, release
decisions, and plugin/backend conformance execution remain pending. The B16
cohort remains unchanged. The recorded pre-promotion C7 type-checker revision
contributed 142,136 authenticated bytes in 142 fragments containing 12,759
forms and 5,221 resolutions. Its checked-core products contained 137 function
records, 2,069 call edges, ten recursion components, and no keyword-lookup
products. The recorded
cache-free proof passed every contract check in 2,411.35 seconds in its
then-current coordinator context, with artifact
identity
`sha256:fb44649cb56a696eb5132774ca4dd0100c9029e4c59bddc27791bbfb115a40f8`
and verification report
`sha256:7300876921f948fedbd2d9d723d1a4b7ec6827c4329777e167015db582937665`.
The bounded first-order function-typing integration then passed 24 tests and
244 assertions
in 723.332 seconds with 3.627 GB sampled peak resident memory. It consumes
the host-validated B47 report and digest boundary, reconstructs definitions,
fixed-arity functions, direct calls, local bindings, and recursion products in
Gravity, emits path-neutral type identities with retained provenance, and
rejects altered lineage and results. That revision proved a first-order
function, local, and call typing slice, not complete C7 or SH-08: higher-order
functions, complete
recursive annotation inference, records, unions, protocols, generics, casts,
dynamic/layout/schema/ownership legality, public routing, native Gravity digest
verification, and seed retirement remain pending. The later proof-traversal
deduplication changed a shared bootstrap fingerprint without changing this C7
source; a fresh checkpoint-v2 replay is therefore pending before completion or
release evidence, and the recorded v1 receipt is not resumed as current proof.
The stage-owned C7 source revision later proven at `5fe2013` is 176,551 bytes
with hash
`sha256:648c71d18f5b81649a8d6d755e1e73b5ba502d359e3a972e4a9341dff6dee975`.
It promotes exactly one capture-free named monomorphic fixed-arity function value
through one `apply-one` hop, derives the authoritative integer signature from B47
call, edge, binding, and evaluation-order products, preserves SH-09/SH-10 pending
obligations, and recomputes altered candidates through the public verifier. Its
dedicated current-runtime namespace passed 9 tests and 150 assertions; the
existing function-call namespace passed 24 tests and 245 assertions. This is
focused non-authoritative integration evidence, not a replacement for a fresh C7
module proof. General higher-order values, captures and lifetimes, aliases and
returns, multi-hop flow, polymorphism, overloads, multi-arity and variadic calls,
protocol or dynamic dispatch, reflection, and complete recursive inference remain
pending. A fresh source-bound proof for this revision then passed at exact proof
commit `5fe2013` in 3,351.068 seconds (55.85 minutes), with artifact identity
`sha256:689e48e4a588cbe5e39a1f9a8cd385eb9e6d9ef7de35742c16958c404c161e15`.
Its derived census has hash
`sha256:5579f5f73d7a53ad84317a098541ec00f1bf18d30f8075c7694763f03c29341e`,
163 fragments and root forms, 15,505 forms, 6,377 resolutions, 12,752 core
nodes, 163 definitions, 2,561 calls, 5,111 references, and no keyword-lookup
products. The reviewed attestation binds the exact source, proof contract, raw
stdout, artifact, and census while recording that counts were derived rather
than independently precommitted. It therefore does not establish exact
authentic coverage, aggregate or release authority, or seed retirement. Later
development-tool commits changed `deps.edn` after proof completion, so this is
accepted exact-context evidence for `5fe2013`, not current-context authority for
those later commits. The 55.85-minute result is the new observational baseline:
it is 38.7% slower than the 40.27-minute historical run while the source is
24.2% larger, so it does not support a speedup claim or a controlled comparison.
The current stage-owned C7 source is 210,220 bytes with hash
`sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4`.
It retains the bounded one-hop higher-order and one-self-edge recursion
products, and now derives both from the authoritative primitive literal family
integer, bool, and string. The self-recursive case remains exactly one named,
capture-free, positive-fixed-arity component with one direct positional
parameter-forwarding self call, a direct primitive-literal sibling base from
the same family, and concrete external call evidence. Its fixed-point context
is node-order independent, recursive and external B47 edges remain distinct
and identity-bound, and recursive constraints are shared by the typed core,
top-level result, and semantic identity. The combined cheap gate passed 18
selected vars and 330 assertions. Separate authenticated boundary vars passed
the bool higher-order `.gravity` case with 41 assertions and the string
self-recursive `.gravity` case with 36 assertions, one cold carrier each; the
paired `.qst` files were checked only for byte parity. These are current-runtime
non-authoritative integration results. Zero-arity recursion, transformed or
literal recursive arguments, nonliteral bases, multiple recursive calls or
components, mutual recursion, recursive captures or higher-order values, and
polymorphic, multi-arity, variadic, or nonconvergent recursion remain rejected
or pending. The prior source-bound proof and census remain exact historical
evidence for `5fe2013`; they were not reused for this source. A fresh proof
candidate at commit `206e89f` completed with stable context and a checked
output contract in 3,998.709 seconds of wrapper time (3,993.553 seconds in the
proof). It produced artifact
`sha256:9ee396cb1f8d6403ce14061a9e9d9977829da25c7fc42c13d9a0804025006587`
and derived census
`sha256:6580b784bb46755231ad62ed3095ce5efe775f233d6f327098bb802fb6380393`:
192 fragments and roots, 18,554 forms, 1,528 bindings, 1,266 local bindings,
7,687 resolutions, 15,286 core nodes, 192 definitions, 3,082 calls, 6,185
references, 187 function records, 3,082 call edges, 14 recursion components,
and zero keyword-lookup products. Counts were derived rather than precommitted,
the result requires a separate attestation, and no trusted reviewed attestation
was created. It is therefore source-bound proof-candidate evidence only, not
exact-authentic-coverage, aggregate, release, or seed-retirement authority.
The historical C8 effect-checker source added 44,102 authenticated bytes in 40 fragments
containing 3,301 forms, 410 bindings, and 1,078 resolutions. It lowers to 2,788
canonical core nodes with 40 definitions: 34 executable SH-09 functions and
three quoted source-model functions. Its 178 `get` calls divide into 169
literal-key calls and nine dynamic-key calls; all are ordinary resolved calls
rather than keyword-headed lookup forms, so the canonical keyword-lookup
product count remains zero and B16 stays frozen. The cache-free authoritative
proof passed with exact source revision
`sha256:8be72ed8adbe830992ee990ba0cb23bb06ce7d29859360afa7d937f0833e0096`,
complete capability proof, and no failed checks. The bounded source recognizes
`:error/raise` as a declared language-level effect with no capability,
provider, grant, or resource subject. That receipt is historical evidence for
the 44,102-byte revision, not current evidence for the adapter revision.

The current stage-owned C8 source is 82,797 bytes with hash
`sha256:de3fb80e14336cadacf710a0b2fef33b19efab0728d5ca08e7a25c72df7afe16`.
It adds a bounded authenticated SH-08 adapter for exact schema-3 pure primitive
first-order and capture-free one-hop function-typed cores when all declared
call and latent effect sets are empty. It reconstructs the complete C7 identity
preimage rather than accepting a relabeled domain or shaped identifier. The
adapter preserves
`[:pending-sh09]` thrown effects, recomputes the existing C8 legality kernel,
keeps physical path provenance out of semantic identity, and rejects altered
upstream identities, facts, requests, or results. Four synthetic tests passed
64 assertions, and one authenticated `.gravity` function-typed boundary passed
27 assertions in 65.918 seconds with one cold carrier; its `.qst` twin was not
built. These are non-authoritative development results. A fresh source-bound
C8 proof candidate then completed at commit `871cd14` in 877.765 seconds of
checkpoint time (872.388 seconds in the proof), with stable context, empty
stderr, and a checked output contract. It produced artifact
`sha256:ed0a4eb33beafb7c0585cab276ab97aac3d6542872b8bad3e89e9df1930d46a0`
and derived census
`sha256:78ea0c864b4a59694e99470408e4aebbf5783c623827d29ea0f0d4ae9e49c902`:
61 fragments and roots, 5,973 forms, 512 bindings, 250 local bindings, 2,023
resolutions, 5,135 core nodes, 61 definitions, 852 calls and call edges, 1,607
references, 55 function records, one recursion component, and zero keyword
lookups. That proof binds the preceding 73,997-byte revision at commit
`f3729a5`; the current source retains ordered canonical type, effect, and
no-capability digest requests plus exact request/digest binding into a separate
path-neutral effected-core template. Its latest first-order and full-identity
discriminator passed two tests and 28 assertions. Digest computation remains
an external coordinator boundary. Counts were derived rather than precommitted
and no trusted reviewed
attestation was created, so the prior result is historical proof-candidate
evidence only. This does
not establish exact authentic coverage, aggregate or release authority, a
production effect checker, complete schema enforcement, effectful adaptation,
general effect inference, completed thrown effects, latent or transitive call
effects, handlers, namespace or module summaries, complete runtime-profile
policy, MIR preservation, C8-to-C9 adaptation, or SH-09 completion.
The C9 ownership-checker source adds 35,894 authenticated bytes in 31
fragments containing 2,320 forms, 370 bindings, and 741 resolutions. It lowers
to 1,964 canonical core nodes with 31 definitions: 21 executable SH-10
functions and three quoted source-model functions. Its 134 `get` calls divide
into 129 literal-key calls and five dynamic-key calls; all are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The cache-free
authoritative proof passed with exact source revision
`sha256:b4fdf1022eb6eb25d091f1c918332c7b1393b6850acf4bb6988d8b8dbb2269e0`,
complete capability proof, and no failed checks. The bounded executable surface
covers owned-mutable initialization, read, immutable and mutable borrow,
borrow end, move, consume, and one function-return escape policy with exact
ordered facts and structured rejection reasons. Request and event maps remain
shallow non-exact schemas; digest values are shape-checked only; whole-carrier
cycle, depth, width, and scalar preflight is absent; the 1,024-event uniqueness
and execution paths remain recursive; lifetime endpoints are generic numbers;
the capability proof identifier is preserved but not authenticated; and the
verifier is same-implementation recomputation. That result is historical
evidence for the 35,894-byte revision. The current 71,132-byte source at
`sha256:4f26a5ca5fdd7755016f332fc5c795f84a98b83b76cef79806b8021807897fcd`
preserves the owned-mutable kernel and adds an exact C8-to-C9 adapter for
identity-bound pure integer, boolean, and string values as persistent immutable
reads. It resolves each exact ownership fact before deriving and resolving the
whole ownership-core identity, rejects reordered, substituted, missing, or
colliding digest results, and keeps physical provenance outside semantic
identity. Coordinator digest resolution remains shape-checked and explicitly
non-authoritative. Four synthetic adapter tests passed 32 assertions, the
existing kernel passed 424 assertions, and the combined C9-to-C10 lane passed
five tests and 147 assertions in 69.470 seconds with one cold `.gravity`
carrier; its `.qst` twin was byte-parity evidence only. These are
non-authoritative development results. A historical source-bound-derived proof
candidate for a preceding source revision completed in 505.045 seconds of
checkpoint time (499.953 seconds in the proof), with stable context, empty
stderr, artifact
`sha256:56aa7b6cded727e47b7528a7b027b711b7fb911e8dd288df259d15282412b2de`,
and derived census
`sha256:b28f186ab5586620913748d21224937344cbacea22a178c391640a8c2bd61a45`:
44 fragments and roots, 3,274 forms, 426 bindings, 164 local bindings, 1,060
resolutions, 2,776 core nodes, 44 definitions, 423 calls and call edges, 855
references, 37 function records, five recursion components, and zero keyword
lookups. Counts were not precommitted, and no trusted reviewed attestation was
created, so the result is historical planning evidence only. The current
source and fixed wrapper identity invalidate it and require a fresh no-resume
run after integration and freeze; it does not establish exact authentic
coverage, aggregate or release authority, or automatic scoped authority.
Persistent aggregate copy
semantics, owned-mutable, effectful, and nonprimitive SH-09 adaptation, field
and range splitting, regions, arenas, linear resources, task, actor, and FFI
transfer, runtime borrow checks, unsafe-audit execution, trusted digest
resolution, MIR preservation, and SH-10 completion remain pending.
The historical C10 safety-analysis source added 68,327 authenticated bytes in
73 fragments containing 5,652 forms, 542 bindings, and 2,046 resolutions. Its calibrated
lowering contains 4,709 canonical core nodes with 73 definitions: 63
executable SH-11 functions and three quoted source-model functions. Its 337
`get` calls divide into 334 literal-key calls and three dynamic-key calls; all
are ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 380,720 carrier nodes at depth 38, maximum width 5,652,
and 6,314,034 exact UTF-8 scalar bytes; every unchanged B16 bound passes. The
bounded executable surface classifies index bounds, integer overflow, division,
numeric casts, and shifts into exactly one of `:proven-safe`,
`:runtime-checked`, `:rejected`, or `:unsafe-island`, with structured reason
surfaces and recomputing verification. Production safety-analysis authority,
complete contract and diagnostic schema enforcement, memory and lifetime
safety, regions, linear resources, FFI, concurrency, taint, generated-code
safety, floating-point and elementary-function safety, optimization
invalidation, authenticated SH-09/SH-10 convergence, MIR preservation, and
SH-11 completion remain pending. The current C10 source is 112,712 bytes at
`sha256:2d334872a84394acc636280796e205a74b227327aa3d646d6c19d55210bd4968`.
It adds a bounded adapter from the new identity-bound C9 ownership core. The
adapter admits exactly one persistent-immutable integer, boolean, or string
read and permits its private load classifier to return `:proven-safe` only
after sequential proof, result, and whole-core digest resolution. The generic
classifier still rejects load requests, and runtime-checked, unsafe,
nonpersistent, aggregate, and effectful load families remain pending. Its
current cross-stage development receipt is the five-test, 147-assertion lane
above; the final authenticated `.gravity` boundary took 61.220 seconds, used
one cold carrier, and did not build `.qst`. Fresh source-bound-derived proof
candidates for the three frozen sources completed at commit `e27757e` with
stable context, checked output contracts, exit zero, no timeout, and empty
stderr. C8 took 1,035.465 seconds and produced artifact
`sha256:e36f64d3fab9f8419d9f0e8d1b4b2f59f137b2ca07b7756dd9c0a7c92be6c3a4`
and census
`sha256:ecd335f292da034f0e283f79969d75002d459f41cd4f43f3ce10cee887510570`:
72 fragments and roots, 6,757 forms, 570 bindings, 308 local bindings, 2,312
resolutions, 5,792 core nodes, 72 definitions, 976 calls and call edges, 1,847
references, 66 function records, four recursion components, and zero keyword
lookups. C9 took 748.278 seconds and produced artifact
`sha256:ec15a730d9f264b7744e797d29e928adc9ca8953daf186a4f2e84634b6baa86d`
and census
`sha256:a6d95d6a7ddc63c3a446dafc946e102587b753f443cba7a004d3240dafe3d7c6`:
66 fragments and roots, 5,243 forms, 556 bindings, 294 local bindings, 1,797
resolutions, 4,409 core nodes, 66 definitions, 716 calls and call edges, 1,440
references, 59 function records, 13 recursion components, and zero keyword
lookups. C10 took 1,624.000 seconds and produced artifact
`sha256:de279d0e495212d259fd3f78e5575aa57c3a7559984df9acfdb71fd5f0488cb2`
and census
`sha256:d327c313e26d09bfbab9417d06ce4987767e7547941ec9afdeefdf643026c469`:
111 fragments and roots, 9,200 forms, 728 bindings, 466 local bindings, 3,306
resolutions, 7,728 core nodes, 111 definitions, 1,350 calls and call edges,
2,560 references, 104 function records, one recursion component, and zero
keyword lookups. Counts were derived rather than precommitted and all three
receipts require separate attestation. No trusted reviewed attestation,
exact-authentic coverage, aggregate authority, release authority, or
safety-certificate authority is claimed.
The C12 domain-IR source adds 61,946 authenticated bytes in 85 fragments
containing 5,711 forms, 569 bindings, and 2,073 resolutions. Its calibrated
lowering contains 4,591 canonical core nodes with 85 definitions: 72
executable SH-14 functions and four quoted source-model functions. Its 254
`get` calls divide into 209 literal-key calls and 45 dynamic-key or indexed
calls; all are ordinary resolved calls rather than keyword-headed lookup forms,
so the canonical keyword-lookup product count remains zero and B16 stays
frozen. The SH-06 request contains 383,280 carrier nodes at depth 23, maximum
width 5,711, and 6,372,295 exact UTF-8 scalar bytes; every unchanged B16 bound
passes. The bounded executable layout surface now enforces exact per-kind and
nested schemas, canonical bounded identifiers and payloads, iterative
structural preflight, bounded origin chains, portable ASCII symbol and keyword
sizes, supported profile/target pairs, path-neutral identity, and contained
candidate verification. Production domain-IR authority, complete C12 contract
and diagnostic schema enforcement, authenticated SH-12 MIR input,
target-specific layout, actual allocation, field-offset calculation, pointer
and lifetime layouts, and SH-14 completion remain pending.
The C13 MIR-optimization source adds 98,836 authenticated bytes in 114
fragments containing 8,513 forms, 740 bindings, and 3,140 resolutions. Its
calibrated lowering contains 7,036 canonical core nodes with 114 definitions:
100 executable SH-16 functions and five quoted source-model functions. Its 493
`get` calls divide into 445 literal-key calls and 48 dynamic-key or indexed
calls; all are ordinary resolved calls rather than keyword-headed lookup forms,
so the canonical keyword-lookup product count remains zero and B16 stays
frozen. The SH-06 request contains 562,128 carrier nodes at depth 31, maximum
width 8,513, and 9,490,546 exact UTF-8 scalar bytes; every unchanged B16 bound
passes. The cache-free acceptance, core, and rejected proof passed in 1,744.16
seconds with 4,345,921,536 maximum resident bytes and no swaps. The isolated
C13-specific replay proof passed in 1,557.99 seconds with 4,643,569,664 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
all contract checks in 2,588.82 seconds with artifact identity
`sha256:78f6074f338f98f3241ea08e2d2c8f427db0aa71d4d7bb6057f8a5168244fba9`,
6,019,416,064 maximum resident bytes, and no swaps. The bounded executable
surface now applies constant folding, branch
simplification, dead-code elimination, and proof-governed check retention or
elision as a genuine sequential pipeline with conservative invalidation,
signed-i64 containment, exact operation schemas, iterative structural
preflight, path-neutral identity, post-pass verification, and exact
recomputation. Authenticated SH-15 input, complete C11 MIR adaptation,
remaining proof-class replay, whole-function translation validation,
target-lowering proof preservation, a self-hosted certificate checker, and
SH-16 completion remain pending.

The subsequent normalized C13 development repair requires an explicit `:i64`
result before folding a constant addition. Previously, bounded numeric operands
could produce a constant while retaining an unrelated declared result type;
recomputing verification repeated that same defect. The owning
`sh16-constant-folds-only-an-established-i64-result-domain` regression covers
both normalized request constructors, signed boundaries, accepted i64 folds,
unsupported canonical type IDs, diagnostic priority and tamper recomputation.
Nonconstant additions remain unchanged; audited unsafe-island additions
outside the explicit fold domain may be accepted without folding. This does
not introduce general opaque-type interpretation or require rejecting every
non-i64 operation.

The initial regression attempt recorded 156 passes and 80 failures in 236
assertions. Those failures included incorrect test oracles, not exclusively
the production defect. The first guarded attempt recorded 230 passes and six
failures in 236 assertions, with seven remaining selectors unrun. Correcting
only the test's String-as-identifier assumption and earlier diagnostic
expectation produced eight passing selectors and 325 assertions: 236 new and
89 adjacent pipeline, schema, recomputation and check-elision assertions.
The separate language-boundary run passed six tests and 11 assertions, for
14 tests and 336 assertions across the two passing invocations. The production
Clojure LOC validator passed 2,021 files with zero exceptions and maximum 250.
Both failed attempts remain distinct historical results. Focused supplied-MIR
execution supports this development behavior, not fresh C11 source ingress,
native execution, renewed authority or milestone completion. Historical C13
source measurements above are not evidence for this changed revision.

A second bounded C13 repair rejects non-Bool normalized branch conditions
before simplification. The old Set-membership helper vacantly treated Nil as
present, allowing it to become an unconditional else transfer. Only the two
owning Bool-domain checks changed; generic membership helpers and other flag
domains remain untouched. True and false preserve their selected destination,
source/origin, effects and invalidation records. On the ordinary branch-validation
path, Nil and representative other non-Bools reject with `C13-CONTRACT`;
earlier validation gates keep priority.

The actual RED attempt passed 170 and failed 16 of 186 assertions, with no
errors; all sixteen failures exposed the Nil path across the two normalized
constructors, not test-oracle errors. The changed candidate passed nine tests
and 511 assertions (186 new plus 325 adjacent), followed by six boundary tests
and 11 assertions: 15 tests/522 assertions across two passing invocations.
LOC validation separately passed 2,021 files, zero exceptions, maximum 250.
Independent review accepted both code and actual results. This establishes
normalized optimizer behavior, not source truthiness, fresh C11 integration,
authoritative migration or SH-16 completion.

The next C13 repair preserves existing unsafe audit IDs through constant folding
and Bool branch simplification. Previously, valid audited input passed ingress,
but the transformed operation kept `:unsafe-island` while dropping its required
audit ID and therefore failed post-pass verification. Conditional preservation
fixes that output; safe output maps stay unchanged and input/post-pass validation
is not loosened. The third shared retained-runtime-check callsite still rejects
unsafe checks through its existing `:runtime-checked` requirement.

At `c4d9980f`, the coherent RED regression recorded 86 passes and 66 failures in
152 assertions, with no errors. Changed candidate `a891a8e3` passed ten tests and
663 assertions (152 new plus 511 adjacent); the separate boundary run passed
six tests and 11 assertions: 16 tests/674 assertions across two passing
invocations. LOC separately passed 2,021 files, zero exceptions, maximum 250.
The G/Q request constructors, audit/source/safety preservation, decision and
invalidation records, rejected ingress and recomputation/substitution controls
are development evidence for supplied normalized records. Their audit IDs are
not audit-provider evidence, authenticated C11 or source construction, native
execution or proof renewal. Historical failures remain retained separately.

A subsequent C13 diagnostic repair retains the first failing operation's span
and generated-origin chain when BOTH are valid. Previously the ordered validator
discarded this context and reported the enclosing request location. Malformed
operation metadata retains the whole legacy request diagnostic, without skipping
the first error; aggregate errors retain their original priority and location.
Accepted output, rejection reasons, schemas and bounds remain unchanged, and
fresh result recomputation rejects substituted diagnostic metadata.

The original `980cd64c` RED run recorded 298 passes and 48 failures in 346
assertions: 36 attribution failures plus 12 incorrect expectations for a List
carrier rejected by the earlier structural gate. Correcting that test control
produced the 334-assertion regression, without another RED run. Candidate
`1172289d` passed 12 tests and 1,207 assertions (334 new plus 873 adjacent).
The separate boundary run passed six tests and 11 assertions: 18/1,218 across
two passing invocations. LOC separately passed 2,021 files, zero exceptions,
maximum 250. These supplied normalized diagnostic records do not establish
fresh source construction, native execution, migration or completion evidence.

The next C13 pass-composition repair defers an admitted pure, proven-unused ADD
unchanged through constant folding and branch simplification until scheduled
DCE removes it. Previously folding dropped its dead marker/proof and the ADD's
folded value survived DCE. The original operation and proof are not copied
across a rewrite. Live ADDs still fold; input type/overflow/effect/proof checks,
pass order, schema and bounds remain unchanged. Final/post-DCE validators still
reject dead operations. This does not select dead-branch deletion or establish
control-flow unreachability from an unused-result flag.

The `17730a9e` RED run retained 190 passes and 60 failures in 250 assertions,
with no errors. Candidate `abfbb409` passed 13 tests and 1,246 assertions
(250 new plus 996 adjacent, including dead-result and stale-proof controls).
The separate boundary run passed six tests/11 assertions: 19/1,257 across two
passing invocations. LOC separately passed 2,021 files, zero exceptions,
maximum 250. Original G/Q constructors supply normalized proof DATA; checks
cover unchanged pre-DCE operations/proofs, DCE-only decisions and invalidation,
source/origin identity, rejected proofs/effects/safety, and fresh recomputation
against substituted results. These are not proof-provider verification, fresh
authenticated C11/source input, native execution or migration evidence.

The next C13 artifact repair records the dead-proof IDs actually used by DCE.
Only selected operations whose original proof records passed ingress validation
contribute. Runtime-check elision keeps its existing proof occurrences, including
duplicates. Aggregate translation-validation usage concatenates fresh pipeline
decisions in pass/operation order without deduplication; no-change usage is
empty. Existing runtime-proof invalidation and its aggregate are unchanged:
using a dead proof for deletion does not add its ID to invalidation records.
Fresh pipeline reconstruction/comparison still precedes finalization, and
verification recomputes rather than trusts supplied usage metadata.

The `051055cf` RED run retained 136 passes and 24 failures in 160 assertions,
without errors. Candidate `294d6aab` passed 15 tests and 1,428 assertions
(160 new, the prior 1,246 and the existing 22-assertion elision control).
Boundary checks separately passed six tests/11 assertions: 21/1,439 across two
passing invocations. LOC separately passed 2,021 files, zero exceptions,
maximum 250. G/Q supplied-record controls cover distinct/repeated dead and
runtime IDs, mixed DCE/retained checks, deterministic order, unchanged
invalidation, no-change records, rejected proofs and substituted usage. This is
normalized proof DATA, not provider verification, fresh authenticated C11/source
construction, native execution, admission or migration evidence.

The next C13 semantic repair prevents an unused-result proof from authorizing
RETURN removal. Previously a pure RETURN's artificial result could have no
uses, satisfy the exact normalized dead-proof record and be deleted by DCE,
losing the control transfer. That proof has no CFG/unreachability premise;
the original proof gate now rejects this case with `C13-PROOF`. Live returns
and valid pure const/ADD/compute deletion remain unchanged. Existing diagnostic
priority and the runtime-check `C13-SAFETY` fence survive; branch/branch-if
policy, schemas, bounds and public APIs are unchanged.

The `f0ec0dad` RED run retained 96 passes and 14 failures in 110 assertions,
without errors. Candidate `33d1d2c0` passed 16 tests/1,546 assertions: 118 new
(the original 110 plus eight runtime-check safety assertions, not an oracle
correction) and 1,428 adjacent. Boundary checks separately passed six tests/11
assertions: 22/1,557 across two passing invocations. LOC separately passed
2,021 files, zero exceptions, maximum 250. The normal join of SOURCE PR153
preserved both owned files and changed no executed raw compiler-rule/host
dependency; results retain their original candidate binding, not a joined
replay. These G/Q supplied normalized proof records and fresh recomputation
checks do not establish CFG proof, provider verification, fresh authenticated
C11/source input, native execution or migration evidence.

The next C13 semantic repair applies the same control-preservation rule to an
already unconditional `:branch`, including the original Bool simplifier's
output. Its unused artificial result does not make the transfer unreachable;
the no-use/effect-free proof now rejects with `C13-PROOF` instead of allowing
DCE to remove the branch. Live branches at both destinations, Bool branch
simplification, RETURN preservation, pure const/ADD/compute DCE and the
runtime-check safety fence remain unchanged. No branch-if policy, CFG proof,
schema, bound or public API is added.

The `d8d61d40` RED run retained 204 passes and 28 failures in 232 assertions,
without errors. Candidate `12b5cc50` passed 17 tests/1,778 assertions
(232 new plus 1,546 adjacent). Boundary checks separately passed six tests/11
assertions: 23/1,789 across two passing invocations. LOC separately passed
2,021 files, zero exceptions, maximum 250. Original G/Q constructors and the
original Bool simplifier supply normalized branches; public optimization and
fresh recomputation check control preservation, both destinations, proof and
diagnostic priorities, source/origin retention and substituted results. These
are supplied normalized proof DATA, not CFG/unreachability proof, provider
verification, fresh authenticated C11/source input, native execution or
migration evidence.

The next C13 executable increment propagates two distinct, independently typed
i64 constants into one ADD. The candidate must be the first non-const operation
after a pure, proven-safe, nondead const prefix; it must itself be pure,
proven-safe, nondead, explicitly i64/integer-exact with `:constant-operands false`
and reference two actual earlier one-payload producers. Result IDs are unique
across the whole admitted input. Fresh invocation-local counts and a bounded
prefix walk derive the
literal operands for the existing overflow-checked fold; no supplied flag is
accepted as producer authority. The original input references/flag and source,
origin and result identity survive in the appropriate records, while the
existing decision and invalidation ledger record the ADD transformation.
Unknown, forward, duplicate-result, wrong-type, unsafe, effectful, nonconstant,
overflowing or control-dependent joins do not authorize propagation. Existing
ingress rejection and unrelated immediate folds remain unchanged. The input
limit is still 128 operations; no schema, public API, pass or budget is added.

The `567f91da` RED run retained 192 passes and 32 failures in 224 assertions,
without errors. Earlier `4421b204` passed 18/2,010; it remains a distinct result,
not added to final counts. The final `33c4a840` closes whole-input result-ID
uniqueness and passes 18 tests/2,018 assertions (240 new plus 1,778 controls).
Boundary checks separately pass six tests/11 assertions: 24/2,029 across two
passing invocations. LOC separately passes 2,021 files, zero exceptions,
maximum 250. The successor also adds same-producer/duplicate-result controls
and corrects a dead-ADD control's return reference, without claiming another
RED run. These are original G/Q constructors and public optimization/fresh
recomputation over supplied normalized DATA, not proof-provider verification,
fresh authenticated C11/source input, general CFG/SSA propagation, native
execution or migration evidence.

The next C13 executable increment continues the same bounded forward scan
through named ADD intermediates. Each qualifying reference-bearing ADD passes
the existing i64 overflow rule before its checked value is retained under its
original globally unique result ID for later ADDs. Producer admission remains
pure/proven-safe/nondead, i64/integer-exact and two distinct earlier IDs. New
propagation stops at a missing, forward, ambiguous, wrong-type, overflowing,
effectful, unsafe, dead, nonconstant or control barrier, preserving successful
prefix folds without folding an otherwise resolvable suffix. Existing
immediate-literal ADD folds remain unchanged and are not automatically new
chain producers. Original input references/flags, source/origin/result identity,
deterministic decisions, invalidation and fresh recomputation are preserved.
The unchanged 128-operation input limit bounds preparation to 128 count rows
and 128 forward rows; this is not a fixed point or a CPU-time guarantee.

The `4389fabd` RED run retained 174 passes and 12 failures in 186 assertions,
without errors. Candidate `cb6a70f5` passed 19 tests/2,204 assertions
(186 new plus 2,018 controls), including two- and six-ADD chains and later
barrier/suffix controls. Boundary checks separately passed six tests/11
assertions: 25/2,215 across two passing invocations. LOC separately passed
2,021 files, zero exceptions, maximum 250. Original G/Q constructors and public
optimization/recomputation exercise supplied normalized DATA, not general
CFG/SSA propagation, provider verification, fresh authenticated C11/source
input, native execution or migration evidence. No pass, schema, public API or
budget is added.

The next C13 executable increment removes unused intermediate results created
by fresh ADD folding in the existing DCE pass. It reconstructs the fold
selection from the original request, requires exact current-operation equality
to the pure/proven-safe bounded-i64 producer output, unique result IDs across
both original and current operations, and no uses in the complete current
operand scan. Original prefix constants and used final results remain. This
does not delete arbitrary constants, compute or control operations, introduce
a fixed point, or accept a supplied decision as evidence. Explicit validated
dead-proof selections retain their proof IDs; local deletions add no synthetic
or nil proof ID. Existing ordered decision/replay records, runtime invalidation,
original input/source identity and fresh verification remain unchanged for
this normalized interface. Preparation has up to eight bounded operation
traversals and 2,048 operand positions under unchanged 128/16 input limits,
not a two-walk or linear-CPU/performance claim.

The `efbf5013` RED run retained 120 passes and 18 failures in 138 assertions.
The first GREEN attempt at `944005a6` failed namespace loading with no tests
or timing receipt; its delimiter-only successor `31de853e` ran 13 tests with
1,793 passes and two failures in 1,795 assertions, leaving seven selectors
unrun. Both failures were mixed-proof fixture placement: a prepended dead
compute correctly stopped propagation. Moving it after the ADD prefix and
correcting deletion order produced `ee7bf63b`, which passed the changed
selector plus seven unrun selectors: eight tests/767 assertions. Twelve
unchanged selectors/1,621 assertions passed within the failed preceding run;
these are retained individual results, not a second passing invocation.
Together they cover 20 distinct selectors/2,388 assertions, including the
174-assertion local-rule regression. Boundary checks separately pass 6/11;
LOC separately passes 2,021 files, zero exceptions, maximum 250. Failed epochs
remain separate. These are original G/Q constructors and public optimization
and recomputation over supplied normalized DATA, not general CFG/SSA,
proof-provider verification, fresh authenticated C11/source input, native
execution or migration evidence. No schema, API, pass or input cap is added.

The next C13 executable increment permits two operand occurrences to reference
one independently established, globally unique i64 producer, including a
folded intermediate. Both ordered lookups and the exact signed-i64 overflow
rule remain; no multiplication rewrite, new producer authority or general
SSA analysis is introduced. Missing, forward, ambiguous, wrong-type, impure,
unsafe, dead and overflowing joins remain conservative. Original input
references/flags, source/result identity, barriers, post-fold DCE, invalidation
and fresh recomputation are preserved. Two old established-same-ID negatives
are replaced by unestablished repeated-ID negatives; the selected new cases
cover both producer families and representable positive/negative endpoints.

The `16313202` RED run retained 236 passes and 38 failures in 274 assertions.
The first GREEN run at `f3b7188d` completed 11 tests with 1,199 passes and one
error in 1,200 assertions, leaving ten selectors unrun. The old overflow bound
attempted hosted negation of MIN when the right operand was MIN. The owning
predicate now uses the mathematically equivalent zero threshold for that case;
other guards remain unchanged. Added immediate-ADD controls cover `[0, MIN]`
and `[MAX, MIN]`, and refusal of negative-left plus MIN without a host error.
Ordinary explicitly constant overflowing ingress still rejects with
`C13-SAFETY`; nonconstant overflow remains unchanged without folding.

Candidate `5932d3be` reran the affected arithmetic controls after the guard
change and passed 21 tests/2,696 assertions (308 new plus 2,388 controls).
Boundary checks separately passed 6/11: 27/2,707 across two passing invocations;
LOC separately passed 2,021 files, zero exceptions, maximum 250. Failed epochs
are retained separately without pass summing. The observations remain bound
to `5932d3be`, with owned bytes unchanged after the disjoint SH22 main join.
These original G/Q constructors and public optimization/recomputation use
supplied normalized DATA, not fresh authenticated C11/source input, proof
providers, native arithmetic, migration or performance evidence. No schema,
API, pass or input bound is added.

The next C13 executable increment propagates a mixed pair: exactly one actual
established-reference join and one bounded i64 immediate, in either operand
position. Prefix constants and folded intermediates can supply the reference;
canonical ID kind checks precede lookup, and numeric atoms never borrow
identifier meaning from a map key. Two immediates with a false constant flag
stay unchanged. The existing two-reference/repeated-reference routes,
whole-input unique definitions, pure/proven-safe/nondead frontier, barriers,
signed overflow and MIN-right guard, source/result identity, post-fold DCE,
invalidation and fresh verification remain intact. This is not general
CFG/SSA propagation or a new opcode, type, API, schema, pass or input cap.

The `58b732e0` RED run retained 564 passes and 96 failures in 660 assertions,
without errors. Candidate `692ec6ca` passed 22 tests/3,356 assertions (660 new
plus 2,696 controls), including both orders, prefix/folded producers, zero and
signed endpoints, unsupported joins, explicit dead-proof behavior, original
identity, recomputation and substitution rejection. Boundary checks separately
passed 6/11: 28/3,367 across two passing invocations. LOC separately passed
2,021 files, zero exceptions, maximum 250. RED results are not added to passing
coverage. The observations retain their original `692ec6ca` execution binding;
owned code/test bytes remain unchanged after the disjoint SH22 main join.
Original G/Q constructors and public optimization/recomputation consume
supplied normalized DATA, not fresh authenticated C6/C11/source inputs,
proof-provider evidence, native arithmetic, migration or performance proof.

The next C13 executable increment extends post-fold cleanup to original
constants whose actual reference consumers freshly folded. Only canonical
operand IDs from ORIGINAL selected pure, reference-bearing ADD rows establish
that relationship; immediate payloads, numeric map keys, fabricated decisions
and unrelated unused constants do not. Eligible original producers are nondead,
pure/proven-safe, exact one-payload bounded-i64 consts, matched exactly against
the current operations with globally unique original/current result IDs. The
complete current-use scan retains every still-used producer, even when its
user is deleted in this same DCE pass. One-pass operation order, explicit
dead-proof usage, empty local external-proof IDs, conservative runtime
invalidation, source/original request identity and fresh replay are preserved.
This adds two bounded original-row scans and up to two operand-kind checks per
selected ADD: preparation now has up to ten bounded operation traversals plus
the current scan's at most 2,048 operand positions under unchanged 128/16 input
limits. Existing independent replay is additional, not a speed or linear-CPU
claim. No generic constant/control/compute deletion or fixed point is added.

The `670ad03c` RED run retained 208 passes and 22 failures in 230 assertions,
without errors. Earlier delimiter and canonical identity-oracle draft fixes
were static and UNRUN, not failed test epochs. Candidate `b8fe7ee3` passed
23 tests/3,586 assertions (230 new plus 3,356 affected controls), covering
ref/ref, repeated, mixed and chain consumers, unrelated/live producers,
malformed or ambiguous state, no actual fold, one-pass dead-user behavior,
proof usage and fresh result rejection. Boundary checks separately passed
6/11: 29/3,597 across two passing invocations; LOC separately passed 2,021
files, zero exceptions, maximum 250. RED is retained separately. The actuals
keep their original `b8fe7ee3` execution binding, with owned bytes unchanged
after the disjoint SH22 main join. These are original G/Q constructors and
public optimization/recomputation over supplied normalized DATA, not fresh
authenticated C11/source inputs, provider, native, migration or performance
evidence. No schema, opcode, pass, API or cap is added.

The next C13 executable increment admits an independent constant/ADD segment
after an actual pure, proven-safe, nondead `:compute`. The operation remains
exactly unchanged, and the existing forward scan resumes with an EMPTY
producer environment. No pre-compute fact or computed result supplies a new
fold; subsequent constants and checked ADDs use the existing admission,
overflow and global result-uniqueness rules. Effectful, unsafe or dead compute
and all other barriers still stop propagation. Earlier successful folds,
operation order, source/origin/result identity, original request references,
ordered decisions, conservative invalidation, post-fold DCE and fresh replay
are preserved. The old stop-at-all-compute frontier above records the earlier
slice, not a foundational safety requirement. The existing 128 count rows plus
128 forward rows remain bounded; DCE preparation and replay are additional,
not a speed or linear-CPU claim. No API, schema, opcode, pass or cap is added.

The first test attempt at `bbf33209` exited during namespace compilation with
zero tests and no timing receipt; a test-only delimiter correction retained
the fixture bindings. The actual RED at `3d5089a1` retained 262 passes and
48 failures in 310 assertions, without errors. Executed `61dc1f40` passed
24 tests/3,896 assertions (310 new plus 3,586 affected controls). Boundary
checks separately passed 6/11: 30/3,907 across two passing invocations; LOC
separately passed 2,021 files, zero exceptions, maximum 250. These observations
retain the actual execution binding after the disjoint SH22 main join, with
owned code/test and executed raw compiler/bootstrap/runtime dependencies
unchanged, not a joined replay. Original G/Q constructors and public optimizer
recomputation consume supplied normalized DATA, not fresh authenticated C11 or
source input, proof-provider evidence, general CFG/SSA propagation, global CSE,
native execution, migration or performance proof.

The C14 target-lowering source adds 168,303 authenticated bytes in 143
fragments containing 15,213 forms, 936 bindings, and 5,693 resolutions. Its
calibrated lowering contains 12,683 canonical core nodes with 143 definitions:
135 function definitions and eight data contracts. Three legacy function
bodies remain quoted compatibility surfaces; a fourth quoted body supplies the
active pinned C11 function-shape table. Its 1,131 `get` calls divide into 1,032
literal-key calls and 99 dynamic-key or indexed calls; all are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The SH-06
request contains 1,015,002 carrier nodes at depth 32, maximum width 15,213,
16,757,402 exact UTF-8 scalar payload bytes and a 67,007,600-byte conservative
executable scalar charge; every unchanged B16 bound
passes. The isolated authentic acceptance, core, altered-replay, and paired
rejection proof passed in 2,953.89 seconds with 5,054,251,008 maximum resident
bytes and no swaps. The independent cross-root `.gravity`/`.qst` parity proof
passed in 5,391.18 seconds with 5,778,669,568 maximum resident bytes and no
swaps. The final cache-free authoritative process passed every contract check
in 4,780.74 seconds with artifact identity
`sha256:0bfaf8070df705fd62b29cc57f760951ff9414751e7c320ec11df4daa95c2c2b`,
5,970,919,424 maximum resident bytes, and no swaps. The bounded executable
surface accepts LLVM, C, and Wasm requests,
constructs target-specific lowering records, preserves path-neutral identity
with actual-path provenance, and rejects malformed requests and altered
results through structured diagnostics and recomputing verification.
Authenticated SH-16 ingress, complete C11/MIR target coverage, proof
preservation across every supported target, production target-lowering
authority, the complete target matrix, and SH-17 completion remain pending.
The B1 backend-interface source adds 140,646 authenticated bytes in 70
fragments containing 10,980 forms, 530 bindings, and 3,692 resolutions. Its
calibrated lowering contains 9,440 canonical core nodes with 70 definitions:
64 executable functions and six data contracts. Its 1,077 `get` calls divide
into 1,069 literal-key calls and eight dynamic-key or indexed calls; all are
ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 721,001 carrier nodes at depth 29, maximum width 10,980,
11,727,269 exact UTF-8 scalar bytes, and a 46,892,996-byte conservative scalar
charge; every unchanged B16 bound passes. The isolated census completed in
471.74 seconds with 4,039,589,888 maximum resident bytes and no swaps. Fresh
LLVM, C, and Wasm packet construction, verification, and altered-packet
rejection passed in 292.57 seconds with 3,904,782,336 maximum resident bytes
and no swaps. The authentic source, core, altered-replay, and paired rejection
proof passed with 2,439.17 CPU seconds, 5,763,072,000 maximum resident bytes,
and no swaps; its 37,764.43-second wall record includes host suspension. The
cross-root `.gravity`/`.qst` parity proof passed with 4,019.39 CPU seconds,
5,173,477,376 maximum resident bytes, and no swaps; its 30,169.85-second wall
record likewise includes host suspension. The final cache-free authoritative
process passed every contract check in 2,885.68 seconds with artifact identity
`sha256:b24e8ac18ddc88fdc887773b759ab8f36f79e103cb99e13e4f99ee4cb6c77a13`,
5,856,509,952 maximum resident bytes, and no swaps. The bounded executable
surface constructs and freshly verifies LLVM, C, and Wasm backend packets,
preserves source, MIR, optimization, lowering, proof, profile, target, ABI,
runtime, effect, capability, identity, and provenance boundaries, and rejects
missing or altered packet facts through structured diagnostics. Complete B1
MIR and domain-IR coverage, production backend execution, object or executable
emission, external target execution, backend conformance completion, public
Gravity routing, Clojure seed retirement, SH-07 completion, and SH-17
completion remain pending.
The B3 LLVM backend source adds 86,185 authenticated bytes in 83 fragments
containing 7,072 forms, 692 bindings, and 2,762 resolutions. Its calibrated
lowering contains 5,940 canonical core nodes with 83 definitions: 72 executable
functions and 11 data contracts. Its 372 `get` calls divide into 306 literal-key
calls and 66 dynamic-key or indexed calls; all are ordinary resolved calls
rather than keyword-headed lookup forms, so the canonical keyword-lookup
product count remains zero and B16 stays frozen. The SH-06 request contains
484,843 carrier nodes at depth 27, maximum width 7,072, 8,068,604 exact UTF-8
scalar bytes, and a 32,257,096-byte conservative scalar charge; every unchanged
B16 bound passes. The isolated exact census passed in 364.82 seconds with
3,775,266,816 maximum resident bytes and no swaps. Fresh bounded B3 packet and
final-artifact construction, contextual verification, independent lowering
reconstruction, and altered-MIR/lowering rejection passed in 682.65 seconds
with 3,973,185,536 maximum resident bytes and no swaps. The authentic source,
core, altered-replay, and paired rejection proof passed in 1,545.84 seconds
with 4,406,362,112 maximum resident bytes and no swaps. The independent
cross-root `.gravity`/`.qst` parity proof passed in 2,411.56 seconds with
4,782,587,904 maximum resident bytes and no swaps. The final cache-free
authoritative process passed every contract check in 2,189.43 seconds with
artifact identity
`sha256:2c512aaad76cebe430fc2083310ad9699890e314c67467cf899c6e3f0aede59d`,
4,721,672,192 maximum resident bytes, and no swaps. The bounded executable
surface validates a pinned ARM64 macOS LLVM subset, constructs deterministic
textual LLVM for supported scalar and control-flow MIR, preserves target and
source lineage, and rejects malformed envelopes, unsupported operations,
invalid data-flow or CFG structure, unsafe target assumptions, and altered
artifacts through structured diagnostics and contextual verification. The
subsequent authenticated internal gate consumes only verified C11 MIR, preserves
the C13/C14/B1/pass/fact/proof closure, emits an ARM64 Mach-O object and
executable with the pinned Apple Clang/linker, inspects the result with `file`
and `otool`, and executes the binary with the expected scalar exit code.
Standalone accepted `.gravity`/`.qst` fixtures and declared-target rejection
fixtures retain cross-root and extension evidence; target leakage and evidence
tamper reject before Clang, and hostile publication rejects before the final
artifact is visible. Complete B3 MIR and domain-IR coverage, a production or
public LLVM target, backend conformance completion, public Gravity routing,
Clojure seed retirement, SH-07 completion, SH-17 completion, and self-hosting
completion remain pending. The internal gate remains Clojure-seed-bound and
records no public, release, whole-backend, or self-hosting credit.
The B4 Wasm backend source adds 118,298 authenticated bytes in 72 fragments
containing 8,189 forms, 629 bindings, and 2,900 resolutions. Its calibrated
lowering contains 6,830 canonical core nodes with 72 definitions: 61 functions
and 11 data contracts. All 349 `if` forms have normative four-element shape
after the bounded source correction, and its 520 `get` calls divide into 471
literal-key calls and 49 dynamic-key or indexed calls. All are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The SH-06
request contains 561,455 carrier nodes at depth 44, maximum width 8,189,
9,056,474 exact UTF-8 scalar bytes, and a 36,208,772-byte conservative scalar
charge; every unchanged B16 bound passes. The isolated exact census passed in
420.52 seconds with 3,705,847,808 maximum resident bytes and no swaps. The
authentic source, core, altered-replay, and paired rejection proof passed in
1,731.80 seconds with 4,600,299,520 maximum resident bytes and no swaps. The
independent cross-root `.gravity`/`.qst` parity proof passed in 2,612.74
seconds with 4,819,746,816 maximum resident bytes and no swaps. Fresh bounded
B4 construction, contextual verification, and altered-MIR, lowering, and raw
Wasm rejection passed with 9 tests and 122 assertions; each alteration was
contained by its owning B1, B4, or B13 diagnostic boundary before effects. The
authenticated artifact identity is
`sha256:334fe22fbd4367dca63e53b768ff0ef1ed26ef8d8937f8b605471d3fe60da739`.
The bounded executable surface constructs and verifies deterministic Wasm32
core lowering for its supported compiler slice while preserving path-neutral
identity and actual-path provenance. Complete B4 MIR and domain-IR coverage,
production backend execution, component-model and canonical-ABI support,
complete import, export, memory, table, WASI, async, SIMD, atomic,
runtime-binding, artifact-emission, external target-matrix, public-routing,
conformance, release, and self-hosting authority remain pending.
The B2 C backend source adds 122,488 authenticated bytes in 129 fragments
containing 11,151 forms, 832 bindings, and 4,200 resolutions. Its calibrated
lowering contains 129 definitions: 120 functions and nine data contracts. All
441 `if` forms have normative four-element shape, and its 842 `get` calls
divide into 792 literal-key calls and 50 dynamic-key or indexed calls. They are
ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 735,556 carrier nodes at depth 30, maximum width 11,151,
12,379,536 exact UTF-8 scalar bytes, and a 49,502,752-byte conservative scalar
charge; every unchanged B16 bound passes. The isolated exact census passed in
542.06 seconds with 3,733,536,768 maximum resident bytes and no swaps. The
authentic source, core, altered-replay, and paired rejection proof passed in
2,349.54 seconds with 4,543,021,056 maximum resident bytes and no swaps. The
independent cross-root `.gravity`/`.qst` parity proof passed in 3,801.31 seconds
with 4,851,482,624 maximum resident bytes and no swaps. The cache-free
authoritative process passed every contract check in 3,527.56 seconds with
artifact identity
`sha256:4a8e165324a492d57f6a3f7f5ce44ea291abd4ba2ebe424fd2206b6009406ed0`,
5,726,076,928 maximum resident bytes, and no swaps. Fresh Gate A validation,
construction, contextual verification, and altered-input rejection passed 11
tests and 110 assertions in 377.18 seconds with 3,735,666,688 maximum resident
bytes and no swaps. Fresh Gate B validation, construction, contextual
verification, real bounded hosted-C17 source, header, object, and executable
production, exact process records, exit-7 execution, cleanup evidence, and
altered-input rejection passed 11 tests and 231 assertions in 441.10 seconds
with 3,939,500,032 maximum resident bytes and no swaps. Semantic identities
remain path-neutral while actual checkout paths remain in provenance. Complete
B2 MIR and domain-IR coverage, production compiler authority, complete C17 and
target coverage, runtime and library integration, release-grade object and
executable emission, public routing, Clojure seed retirement, backend
conformance completion, SH-07 completion, and self-hosting completion remain
pending.
The cache-free proof-transaction checkpoint preserves the construction and
independent-audit boundary while reusing only thread-confined immutable
verification receipts. All 34 then-authoritative modules were accepted, all
verification and capability checks passed, and the process recorded 476
receipt reuses without a persistent cache. It completed in 31,928.08 seconds
with 7,959,691,264 maximum resident bytes and no swaps, reducing wall time by
about 31 percent from the preceding 46,167-second B38 process. Maximum resident
memory increased from 6,399,033,344 bytes, so this is a measured time-for-memory
trade rather than a memory improvement. The closed receipt binds artifact,
report, stage, mode, check-catalog, and exact verifier identities while
retaining no carrier values after cleanup.
The L1/C2 surface-reader source advances the authenticated compiler inventory
to 35 of 41 modules. It adds 36,920 authenticated bytes in 32 fragments
containing 2,892 forms and 1,019 resolutions; its source hash is
`sha256:cb416baa7330fd7db5507fcd5fc1d78d5c9e848feb9020552d4c21b9e1c17fe0`.
The isolated census passed 9 tests and 193 assertions in 183.00 seconds with
4,021,829,632 maximum resident bytes and no swaps. Authentic replay passed 9
tests and 194 assertions in 545.56 seconds with 4,021,174,272 maximum resident
bytes and no swaps, preserving the genuine reader products and rejecting an
altered token stream. Cross-root `.gravity`/`.qst` parity passed 9 tests and
191 assertions in 989.77 seconds with 4,023,549,952 maximum resident bytes and
no swaps; semantic identity stayed path-neutral while actual paths remained in
provenance. The final cache-free authoritative process passed every contract
check in 622.459 seconds with artifact identity
`sha256:f27d7569ad0a06988241864fcb49ca1fc0df5d39993b55397e96a58524f050f9`,
14 receipt reuses, 3,820,716,032 maximum resident bytes, and no swaps. Complete
reader semantics, public routing, Clojure seed retirement, SH-07 completion,
and self-hosting completion remain pending.
The SH-23 hermetic package-build source advances the authenticated compiler
inventory to 36 of 41 modules. It adds 39,872 authenticated bytes in 50
fragments containing 3,757 forms and 1,428 resolutions; its source hash is
`sha256:70649e49d7130cf33e1d11ac226452ea908fee6731e0e49ca8e003e14cbabe1e`.
The isolated census passed 10 tests and 105 assertions in 204.16 seconds with
4,076,879,872 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 106 assertions in 775.98 seconds with 4,549,574,656 maximum resident
bytes and no swaps, preserving the genuine package-build products and rejecting
altered core data. Cross-root `.gravity`/`.qst` parity passed 10 tests and 103
assertions in 1,286.10 seconds with 4,366,024,704 maximum resident bytes and no
swaps. The alternate-checkout proof preserved equal path-neutral identity while
retaining distinct physical paths in component records, artifact requests,
source maps, and provenance. The final cache-free authoritative process passed
every contract check in 776.594 seconds with artifact identity
`sha256:57811a49093fdc298005c42cebd9b42a089e4fa1b8fbb8009ee0080e3e4bac0c`,
14 receipt reuses, 4,653,350,912 maximum resident bytes, and no swaps. General
dependency solving, action execution, public routing, Clojure seed retirement,
SH-07 completion, SH-23 completion, and self-hosting completion remain pending.
The SH-22 bounded standard-library source advances the authenticated compiler
inventory to 37 of 41 modules. It adds 45,717 authenticated bytes in 64
fragments containing 4,896 forms and 1,944 resolutions; its source hash is
`sha256:6198d87e19ee86f72d51aa1253c2d438da6af8dea495f2dd5eb0e64ca3eff1e4`.
The isolated census passed 10 tests and 77 assertions in 255.76 seconds with
4,108,025,856 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 78 assertions in 811.91 seconds with 4,749,508,608 maximum resident
bytes and no swaps, preserving all 64 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 75 assertions in 1,684.35 seconds with 4,326,227,968 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 1,022.793 seconds with artifact identity
`sha256:27748ea15b172858d829a0f6761b3ad0245e3a088c118a033d9ba678b7a8fe61`,
14 receipt reuses, 4,632,182,784 maximum resident bytes, and no swaps. Complete
Unicode and normalization, authenticated runtime services, the complete
standard library, public routing, Clojure seed retirement, SH-07 completion,
SH-22 completion, and self-hosting completion remain pending.
The reusable authenticated-envelope source advances the authenticated compiler
inventory to 38 of 41 modules. It adds 59,495 authenticated bytes in 74
fragments containing 5,302 forms and 2,146 resolutions; its source hash is
`sha256:04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71`.
The isolated census passed 10 tests and 101 assertions in 318.15 seconds with
4,025,204,736 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 102 assertions in 965.93 seconds with 4,868,505,600 maximum resident
bytes and no swaps, preserving all 74 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 99 assertions in 1,840.12 seconds with 4,610,244,608 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 1,137.379 seconds with artifact identity
`sha256:b2c8ec3ad2dcb1b73f6707914788baa72dc301fb7d3a2ca69f2d2195625a28ee`,
14 receipt reuses, 4,585,766,912 maximum resident bytes, and no swaps. Release
signature authority, verifier-correctness proof, self-hosted envelope
execution, whole-language coverage, public routing, Clojure seed retirement,
SH-07 completion, and self-hosting completion remain pending.
The Gravity resolution source advances the authenticated compiler inventory to
39 of 41 modules. It adds 77,209 authenticated bytes in 97 fragments containing
8,102 forms and 3,278 resolutions; its source hash is
`sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56`.
The isolated census passed 10 tests and 105 assertions in 583.21 seconds with
4,082,614,272 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 106 assertions in 1,578.96 seconds with 5,026,824,192 maximum resident
bytes and no swaps, preserving all 97 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 103 assertions in 2,874.68 seconds with 5,472,419,840 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 1,745.662 seconds with artifact identity
`sha256:f58d88d1103ea2c72bf549ffdae0fac42272efc8dd5f7305ea85d269f68537fb`,
14 receipt reuses, 5,455,511,552 maximum resident bytes, and no swaps. Package
discovery, type and effect checking, self-hosted resolution execution,
whole-language coverage, public routing, Clojure seed retirement, SH-07
completion, and self-hosting completion remain pending.
The bounded C6-C10 checked-core pipeline source advances the authenticated
compiler inventory to 40 of 41 modules. It adds 141,562 authenticated bytes in
144 fragments containing 13,318 forms and 5,210 resolutions; its source hash is
`sha256:0299511c26c8b2a191309a4a4358528de397c88551ed98a2aed03d172067b2a5`.
The isolated census passed 10 tests and 91 assertions in 739.56 seconds with
4,070,670,336 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 92 assertions in 2,648.54 seconds with 6,381,010,944 maximum resident
bytes and no swaps, preserving all 144 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 89 assertions in 5,051.26 seconds with 5,924,700,160 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 3,137.484 seconds with artifact identity
`sha256:aae6f68bd91a2c8b9f0907b20b3a2ca3e68dc64b8fbedca1ed2db8710ae8f9b5`,
14 receipt reuses, 6,959,955,968 maximum resident bytes, and no swaps. Calls,
complete collection and numeric forms, mutation and resource semantics,
runtime checks, unsafe islands, domain IR, backend lowering, whole-language
coverage, public routing, Clojure seed retirement, SH-07 completion, and
self-hosting completion remain pending.
The authoritative checked-core source advances the authenticated compiler
inventory to 41 of 41 modules. The measured B47 revision was 444,331 bytes with
source hash `sha256:fd5c61348a8470650bb314542ad0ee04ff84099ed42fca05cbe72d0c0fc3ebdb`,
308 fragments, 37,411 forms, 2,440 module bindings, and 15,247 resolutions. The
authentic B47 census accepted after 5,397.56 seconds with 7,758,774,272 maximum
resident bytes and no swaps. It emitted 31,428 core nodes, 308 definitions,
12,282 references, 6,547 calls, 305 function records, 6,547 call edges, six
recursion components, and 31,430 digest requests. The measured template carrier
contained 17,681,671 nodes and 1,035,974,980 scalar bytes; the generated-digest
carrier contained 12,572,396 nodes and 721,982,664 scalar bytes. Those results
support the bounded 33,554,432-node template ceiling and the 16,777,216-node
generated-digest ceiling, with the common 1,073,741,824 scalar-byte ceiling;
rejection telemetry remains diagnostic-only and does not weaken acceptance.
The subsequent semantics-preserving carrier preflight optimization advances
the current source revision to 444,325 bytes with hash
`sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092`;
its source, plan, and function identities are pinned, while a fresh cache-free
replay remains required before final B47 integration.
The B47 census proves bounded construction of exact function, direct-call, and
recursion products, but it is not the final cache-free authoritative replay or
cross-root parity gate for this revision. `SH-07` remains partial: general
higher-order calls beyond the bounded C7 one-hop consumer, closure capture,
complete core-form coverage, public routing, and seed
retirement remain pending. `SH-25` remains queued because its supplied component
descriptors are bounded planning inputs rather than authentic `SH-24` build
outputs.

The additive B48 call-arity module advances the authenticated source inventory
to 42 modules without changing the B47 v16 boundary task. Its 56,581-byte source
has hash
`sha256:d101855439ef185727608ee696aacbfb41aff025ee8bbe0c62122f45f7c112ed`.
The recorded module proof passed every contract check in 772.462 seconds in its
then-current coordinator context, lowering 4,849 forms in 52 fragments with
1,953 resolutions, 52 function records, 796
call edges, and no recursion components. It produced artifact identity
`sha256:381630793764afa6adc0d7d7b4419397fcd06d1030897dc2520d03d2503a2023`
and verification report
`sha256:f34777f5923c2296d839281727317fb53855ac8b5ab79a1afc134e0f4fd25b53`.
This proves bounded direct local fixed-arity legality, ordered argument lineage,
explicitly pending nonlocal calls, path-neutral identity, retained provenance,
and structured rejection. Variadic and multi-arity dispatch, closure capture,
general higher-order calls beyond the bounded C7 one-hop consumer, type/effect
legality, execution, public routing, and seed
retirement remain pending, so `SH-07` receives no completion credit. The later
shared proof-traversal deduplication requires a fresh checkpoint-v2 replay
before completion or release evidence; the recorded v1 receipt remains bounded
historical module evidence rather than current-context authority.

The additive bootstrap-hosted `sh07-core` command exposes the authenticated
SH-07 artifact through the strict public source boundary without repointing the
legacy `core` or compiler commands. Its dedicated executable gate passed 4
tests and 72 assertions for accepted `.gravity` and `.qst` inputs, path-neutral
identity, distinct actual-path provenance, deterministic repeats, unrelated
working directories, bootstrap-only and default stale-package bypasses,
structured C6 rejection, extension policy, and malformed UTF-8 containment.
The route still declares the Clojure source-loading, plan-execution, digest,
envelope, and final-assembly boundary; it does not provide package refresh,
seed retirement, complete public compiler routing, or `SH-07` completion.

The Clojure stage0 boundary still owns initial source reads, compiler-plan
execution, canonical digest resolution, authenticated-envelope assembly,
central routing, and final artifact construction. The historical admitted
compiler snapshots passed their source-level authenticated gates; subsequent
development revisions do not inherit that authority. The complete
core-form, mutation, recursion, exception, and pattern surface must pass the
remaining executable gates before `SH-07` can receive completion credit.

### Private self-entry diagnostic source: development repair

The owning C7 entry-value analysis now retains the actual indexed caller node
when rejecting missing independent entry types or conflicting entry types.
The existing diagnostic renderer consequently receives the correct Core and
syntax identities, span, binding and origin facts instead of a missing node.
First normal-entry selection, Never-transfer exclusion, successful proofs,
rejection reasons, work charges and legacy source-less formatting are unchanged.

The new normalized source-bearing regression first exposed 20 missing-node and
source-projection failures among 63 assertions on the unchanged implementation.
After the repair, that regression and four existing entry/proof/resource
controls passed 5 tests and 133 assertions. Language-boundary checks passed
6 tests and 11 assertions; the Clojure size check reported 2,021 files, zero
exceptions and a maximum of 250 physical lines. These are focused development
results, not a new authenticated source execution. Earlier source-composition
results with missing diagnostic metadata remain historical observations; no
C5/C6 construction or expensive source proof was repeated for this repair.
Authoritative migration and full SH-07 completion remain outstanding.

### C11 node-local diagnostic attribution: development repair

The existing single-diamond MIR ingress now retains the first source-valid
node rejected during checked-Core preflight. Both construction and verification
report that node's operation, span and generated origins rather than the
enclosing conditional. Dominance failures also retain the responsible value ID.
The block is `:not-applicable` because preflight has not established a verified
CFG. Failures with malformed or missing source metadata, and aggregate failures,
retain the original fallback. Valid MIR, diagnostic tags/reasons, precedence, public report fields
and operational limits are unchanged.

The original focused regression exposed 50 attribution failures among 129
assertions on the unchanged engine. The final regression adds a separate
malformed-operands/dominance case and passes 151 assertions. Together with
the existing accepted diamond, malformed checked-Core and MIR fact-loss
controls, the focused batch passes 4 tests and 1,597 assertions.
These supplied checked-Core inputs test the existing producer/consumer ingress;
they do not establish fresh C10 source composition, nested conditionals, loops,
C12 anchors, C14 widening, SH-12 completion or authoritative migration.

### C11 operation-type diagnostic attribution: development repair

After the existing single-diamond preflight and structural gates succeed, a
changed operation type or type-fact reference now identifies the first
mismatching operation in canonical order. Its operation ID, block, span and
generated origins come from freshly rebuilt expected MIR, not candidate
metadata. Type-table and function-return mismatches retain priority and their
aggregate diagnostic. Value-only mismatches retain the existing fallback;
simultaneous operation/value mismatches identify the operation, matching the
original type-check order. An unlocatable first mismatch does not skip forward
to a later operation. Earlier rejection gates remain unchanged.

The initial regression completed 63 assertions with 25 failures, all in the
five cases' attribution fields or complete diagnostic maps. That failed result
is retained. The expanded regression passes 75 assertions, including aggregate
and earlier-gate precedence, forged candidate metadata, simultaneous value
failure and malformed expected-source fallback controls. With the accepted
diamond, node-local preflight and fact-loss controls, the focused batch passes
4 tests and 804 assertions. These supplied checked-Core/MIR tests establish
failure attribution at the existing boundary, not fresh C10 composition,
general MIR coverage, SH-12 completion or authoritative migration. Accepted
MIR, diagnostic identifiers, public schemas and resource limits are unchanged.

### C7 invocation-local index construction: development repair

Fixed-point inference now prepares its four immutable source-join lookup
indexes once per invocation rather than once per admitted round. The original
standalone pass remains a fresh-preparation wrapper; public verification also
starts a fresh invocation. Preparation remains after the original round and
cold-work guards. Round-varying types, shapes, private products and remaining
work are not cached. Index input domains, duplicate-last behavior and forms-Map
identity are preserved; caller-supplied lookup indexes are overwritten.

Five focused tests pass 263 assertions. The cold-inference comparison executes
six rounds using 46,030 logical work units. Complete results match an
original-delegating fresh-per-round reference at budget 65,536, the exact used
budget and one unit below it. Each normal index is built once. The comparison
explicitly records its unused initial preparation separately from six forced
per-round preparations (seven observed total); seven is not the legacy count.
Standalone freshness, Map passthrough, duplicate-last and opaque-ID early
rejection controls pass. No C5/C6 source construction is repeated.

This removes repeated lookup construction, not logical ancestry work. It does
not close the retained C17 work-limit deficit, establish a dominant cost or
demonstrate a wall-time speedup. The held C17 subject, provenance/equivalence
gates and authoritative migration status remain unchanged.

### C11 effect/safety diagnostic attribution: development repair

The single-diamond verifier now attributes the first operation effect or safety
fact mismatch to the operation, block, span and generated origins in freshly
rebuilt expected MIR. Candidate location metadata is ignored. The original
Boolean validators and their operation projections are unchanged. Aggregate
effect tables, ordering graph and latent effects, or safety/runtime-check/proof
tables, retain priority. Terminator/value-only failures retain their fallback;
the existing type-before-effect-before-safety order remains intact. An invalid
first expected source keeps the original fallback rather than skipping ahead.

The initial regression completed 175 assertions with 105 passes and 70
attribution failures; it is retained as failed evidence. The expanded regression
passes 179 assertions, including four defensive internal-context fallback
checks. The focused batch passes 5 tests and 939 assertions with operation-type,
preflight, fact-loss and runtime-check-preservation controls. These supplied
checked-Core/MIR checks change failure attribution only. They do not add safety
proofs, fresh C10 source composition, general MIR coverage, SH-12 completion or
authoritative admission. Accepted MIR, schemas and resource limits are unchanged.

### MIR branch argument carriers: development repair

Normalized SH-13 branch and conditional transfers now require Vector argument
carriers before count or indexed access, including non-selected conditional
arms. Missing/Nil, indexed Maps, Lists, Strings and scalars are not empty or
valid argument vectors. Missing-target, arity/type and dominance precedence
remain unchanged. Invalid modules reject before execution; valid G/Q branch,
loop and indirect-call behavior and execution recomputation are preserved.

The unchanged engine's regression completed 302 assertions with 122 failures,
exposing host exceptions, accidental acceptance/execution and failed tamper
rejection; that failed result is retained. The same regression now passes all
302 assertions. With existing invalid-MIR, recursion and runtime-bound controls,
the focused batch passes 4 tests and 512 assertions. These original Gravity/QST
fixture constructors and the runtime test the normalized MIR boundary, not
fresh authenticated SH-12 ingress, broader CFG support, SH-13 completion or
authoritative migration. Public schemas and resource limits are unchanged.

### MIR module rejection before entry lookup: development repair

SH-13 execution now defers entry lookup until its existing module validator
succeeds. This prevents an unnecessary traversal of rejected `:functions`
carriers while preserving the original module diagnostic, error priority and
accepted execution. Some malformed cases already rejected correctly; this does
not claim that every malformed carrier was previously accepted.

The unchanged regression completed 182 assertions with 132 passes and 50
failures before the repair; that failed result is retained. It now passes all
182 assertions. The affected branch-carrier, invalid-MIR and runtime-bound batch
passes 4 tests and 680 assertions using original G/Q constructors, execution
and verification recomputation. This normalized boundary repair establishes
neither authenticated SH-12 ingress nor broader CFG support, SH-13 completion,
native execution or authoritative migration. Schemas and limits are unchanged.

### MIR constant instruction arity: development repair

Normalized SH-13 `:const` instructions now require zero value operands. The
existing constructor already emits an empty Vector; the executor previously
ignored extraneous defined operands. Such instructions now reject with
`C11-TYPE` before execution. Earlier schema, profile, fact, effect and dominance
checks retain their priority; valid factorial execution and verification
recomputation are unchanged. No opcode, public schema or limit was added.

The unchanged-engine regression completed 66 assertions with 54 passes and 12
failures, demonstrating acceptance and execution of malformed constants. That
failed attempt is retained. The repaired regression passes all 66 assertions;
with branch-carrier, module-entry, invalid-MIR and recursion controls, the
focused batch passes 5 tests and 748 assertions. Separate language-boundary
checks pass 6 tests and 11 assertions. These original G/Q normalized module
constructors and execution checks do not establish fresh authenticated SH-12
ingress, native execution, SH-13 completion or authoritative migration.

### MIR normal return dominance diagnostics: development repair

Normal SH-13 returns now classify unavailable value IDs as `C11-DOMINANCE`,
matching C11's use-before-definition rule and existing error-return, throw and
panic validation. Defined values with the wrong return type remain
`C11-TYPE`; earlier source-span and origin failures retain their priority.
This changes diagnostic classification, not the accepted MIR language or
runtime behavior. Existing branch execution returns 42 and factorial returns
120 with verification recomputation preserved.

The unchanged-engine regression completed 102 assertions with 98 passes and
four failures, all normal-return diagnostic mismatches. That failed attempt
is retained. The repaired regression passes all 102 assertions; the affected
constant-arity, invalid-MIR, structured-exit and branch batch passes 5 tests
and 396 assertions. Separate language-boundary checks pass 6 tests and 11
assertions. These normalized G/Q constructor/executor checks are not fresh
authenticated SH-12 ingress, native execution, SH-13 completion or
authoritative migration. Public schemas and resource limits are unchanged.

### MIR later-callee parameter carriers: development repair

SH-13 call-signature validation now checks that a callee's parameters are a
Vector before counting them. A call in an earlier function can reach a later
function before the latter's ordinary shape validation. Malformed parameter
carriers now reject with `C11-MODULE` instead of raising a host exception or
reporting an incidental type mismatch. Missing-callee, dispatch and earlier
operand/profile/fact/effect checks retain their priority. Valid calls and
execution recomputation remain unchanged; no schema or interface was added.

The unchanged-engine regression completed 216 assertions with 168 passes,
46 failures and two errors. The errors came from the original recomputation
verifier counting scalar parameters; they are retained failed evidence.
The repaired regression passes all 216 assertions. With return-dominance,
constant-arity, invalid-MIR and recursion controls, the focused batch passes
5 tests and 582 assertions. These normalized G/Q constructors and execution
checks do not establish fresh authenticated SH-12 ingress, native execution,
SH-13 completion or authoritative migration. Resource limits are unchanged.

### Normalized MIR Bool values: development repair

Normalized SH-13 parameters and constant payloads typed Bool now require actual
`true` or `false`. Nil previously passed a vacant set-lookup comparison and
could execute as a false condition; invalid Bool constants could also pass
whole-module preflight, including in unselected blocks. Both local Bool checks
now reject nil and non-Bool values with `C11-TYPE` before execution. Valid Bool
execution and earlier diagnostic priorities are preserved. The generic
membership helper, other value domains and original-MIR bridge are unchanged;
this does not define source-language truthiness.

The unchanged-engine regression completed 530 assertions with 478 passes and
52 failures, retaining actual nil acceptance and direct invalid-payload hash
callback failures. That attempt remains failed. The repaired regression passes
all 530 assertions; branch, runtime-bound and recomputation controls bring the
focused batch to 4 tests and 568 assertions. Separate language-boundary checks
pass 6 tests and 11 assertions. These normalized G/Q checks do not establish
fresh source or authenticated SH-12 ingress, native execution, SH-13 completion
or authoritative migration. Public schemas and resource limits are unchanged.

### Original SH-12 diamond execution: bounded development composition

The original SH-12 branch/join MIR can now execute through two explicit C11
entrypoints: `sh13-run-branch-join-mir` and
`sh13-verify-branch-join-execution`. They retain the checked-Core/MIR pair and
freshly invoke the original canonical-projection verifier, rather than trusting
a caller's report or relabelling the module as normalized SH-13 evidence.
Private frames reuse the existing interpreter; normalized runtime entrypoints
and the separate non-executable C12 proof-reference adapter are unchanged.

Support is a pure zero-argument single diamond with a literal Bool condition
and homogeneous literal signed-i64 or Bool results. Original operation/value
IDs, incoming CFG edges, fact references and source records remain visible.
The selected arm value transfers into the original if-value join parameter;
the root function boundary is a recorded alias, not an extra computation.
Both arms are checked before execution. Calls, loops, additional operations,
effects, runtime checks and unsafe behavior are excluded. Existing depth/fuel
limits apply to interpreter frames, not exact original instruction counts.

The first run failed: 309 passing assertions and four failures across two
tests exposed acceptance of an unselected nil Bool payload. The local new
literal check was corrected without changing the generic runtime helper.
That failed attempt remains failed. The valid execution selector's 264
assertions remain tied to that earlier revision; review confirmed unchanged
outcomes for its true/false and signed-i64 inputs. Three affected/previously
unrun selectors pass 145 assertions on the corrected revision, including
direct invalid-payload callback controls. This is four distinct tests and 409
passing assertions retained from two invocations, not one clean four-test run;
the first invocation's aggregate result remains failed.

Separate affected controls pass normalized SH-13 3 tests/46 assertions and
the original SH-12 diamond 1 test/98 assertions. Language-boundary checks pass
6 tests/11 assertions. The result's input IDs and fact/source projection do not
claim full input-content authentication. Fresh C10/source ingress, generic MIR
execution, native execution, SH-13 completion and authoritative migration remain
outstanding; no milestone status is promoted by this bounded composition.

### Original MIR integer-equality conditions: executable increment

The bounded SH-12 to SH-13 handoff now executes a computed condition of the
form `(if (= left right) then-value else-value)` when both compared values
are signed-i64 literals and the arms are homogeneous literal i64 or Bool.
SH-12 already emits `:integer-eq`; this maps to the existing interpreter's
equality operation. There is no new source grammar, opcode, argument carrier,
general arithmetic route or duplicate interpreter.

The seven-node case has two entry literals and one comparison. Before any
execution, the new subset check validates both bounded i64 payloads, their
exact ordered operand IDs, the Bool result and original opcode/source-operation
and nonconstant payload marker. The original verifier's fact/type consistency
does not by itself establish this primitive signature. Original verification,
purity, both-arm checks, incoming-value transfer, source/fact references and
root alias remain required. The explicit additive output-domain extension is
seven projection rows with `:integer-equality`; keys and public arities are
unchanged. Existing five-node outputs remain unchanged. The same interpreter
limits apply: five-node frame fuel is 507 and seven-node fuel is 505 after
execution from 512, without increasing a bound.

All five handoff tests pass 559 assertions on the current candidate, including
150 new assertions and rerun affected literal/interface/tamper cases. Separate
normalized branch controls pass 1 test/20 assertions, original producer
controls 1/98, and language-boundary checks 6/11. Thus 13 tests/688 assertions
pass across four invocations, not one combined batch. Checks include true/false
comparisons, signed endpoints, both result types, typed coherent rejection,
original MIR tampering, recomputation and selected-only traces. Supplied
checked-Core construction is not actual C10/source ingress; generic MIR,
calls/loops/errors, native execution, SH-13 completion and authoritative
migration remain outstanding.

### Original MIR strict integer less-than: executable increment

The same bounded handoff now executes `(if (< left right) then-value
else-value)` for two signed-i64 literals and homogeneous literal i64 or Bool
arms. Existing SH-12 `:integer-lt` maps explicitly to the existing interpreter's
`:lt`; it is not a fallback for other nonconstant conditions. The entry check
requires the matching original source operation, exactly two ordered original
literal result IDs, independently bounded i64 payloads, a Bool result and the
nonconstant payload marker. Both arms and original verifier checks remain
mandatory before execution.

This additive projection-domain extension introduces `:integer-less-than`;
keys, public arities, literal-condition and equality results remain unchanged.
The same depth-32/fuel-512 interpreter leaves 505 fuel for the seven-node case
and 507 for the five-node literal case. No bound or supported opcode family
was broadened beyond strict integer less-than.

The current candidate passes all six handoff tests/913 assertions: the original
four tests/409 assertions, equality's 212 assertions and less-than's 292.
Separate normalized loop/indirect controls pass 1/12 and language-boundary
checks 6/11: 13 tests/936 assertions across three passing invocations. Cases
cover less/equal/greater values, signs, signed endpoints, both arm types,
operand ordering, coherent signature rejection, original MIR tampering and
result substitution. Every handoff case uses the original fresh producer and
verifier; the unchanged producer's standalone selection was not repeated.
Clojure source LOC validation passes for 2021 files, zero exceptions, maximum
250 lines. The readable 313-line test namespace is not subject to that
production-source limit. This is supplied checked-Core composition, not actual
C10/source ingress, generic MIR, other ordering/arithmetic, native execution,
SH-13 completion or authoritative migration.

### Original MIR selected-arm comparison: executable increment

The original SH-12 to SH-13 handoff also executes a strict integer less-than
comparison inside exactly one branch of a literal-Bool diamond. The other arm
is a literal Bool. This covers a missing placement of an existing operation,
not a new opcode or general branch-body grammar. The closed instruction-width
layouts are entry/then/else `[1,1,1]`, `[3,1,1]`, `[1,3,1]` or `[1,1,3]`.
Entry-plus-arm computations, two computed arms and arm equality are excluded.

Both complete arms undergo signature, payload, purity and original verifier
checks before execution. The join uses each arm's last original result ID and
the original CFG incoming value. All seven source/fact projection rows remain,
including an unselected comparison; only the selected arm executes. This is an
additive placement-domain extension, not new projection roles, keys or public
arities. The same depth-32/fuel-512 interpreter leaves 505 when the comparison
executes and 507 when it is skipped. No comparison is hoisted into the entry.

All seven focused tests pass 1589 assertions on the current candidate: the
previous six/913 plus 676 selected-arm assertions. These cover both branch
positions and condition values, comparison outcomes and signed endpoints,
selected-only traces, source/fact/incoming joins, unselected invalid operands,
unsupported layouts, MIR tampering and recomputation. Separate normalized
branch controls pass 1 test/20 assertions and language-boundary checks 6/11:
14 tests/1620 assertions across three passing invocations, not one batch.
Clojure source LOC validation passes for 2021 files, zero exceptions, maximum
250 lines. Supplied checked-Core
composition does not establish actual C10/source ingress, generic MIR, native
execution, SH-13 completion or authoritative migration.

### Original MIR literal Keyword returns: executable increment

The bounded handoff now returns actual Keyword literals from homogeneous arms
in the existing five-node literal-condition or seven-node entry-comparison
layouts. Existing compiler helpers such as `c11-opcode` return Keyword markers;
those marker values motivate and exercise the value domain, not whole-helper
lowering. Two local bridge predicates map the existing canonical Keyword type
to runtime `:keyword` and require an actual `keyword?` payload in both arms
before execution. String lookalikes, Symbols and other payloads cannot borrow
the declared type. Namespaced values are preserved without coercion.

This adds the projection runtime-type value `:keyword`, not new roles, keys,
arities, instruction counts, grammar or interpreter machinery. Bool/i64 outputs,
depth 32 and fuel 512 remain unchanged. Entry conditions and three-instruction
computed-arm results remain Bool-only; Keyword conditions and comparison
results are not enabled. Original source/fact and canonical MIR checks remain.

At `7d100933`, all eight focused tests pass 2054 assertions, including 465 new
Keyword assertions and all prior 1589. Separate normalized branch controls
pass 1/20 and language-boundary checks 6/11: 15 tests/2085 assertions across
three passing invocations. Source LOC passes 2021 files, zero exceptions,
maximum 250. Cases cover selection, namespaced marker values, unselected
invalid payloads, zero-callback guards, source/fact/MIR tampering, recomputation
and result substitution. No actual C10/source ingress, generic MIR, native
execution, whole-helper lowering, SH-13 completion or migration is claimed.

After joining the finite Keyword-Set insertion development merge, both MIR
implementation/test files remained byte-identical. A conservative combined
check at `81ccfee4` passed all eight bridge tests plus the normalized branch
control: 9 tests/2074 assertions in one invocation. With retained boundary
6/11, this covers 15 distinct tests/2085 assertions, not additional behavior or
a sum of repeated epochs. The direct raw-stage0/hosted-interpreter route does
not execute the changed Gravity `conj` branch; the join alone does not prove
that the original results were invalid. No source proof was repeated.

### Original MIR selected-arm integer equality: executable increment

The same bounded computed-arm route now accepts integer equality as well as
strict less-than. The compiler's recursive-component check compares a function
count with one inside a selected branch; literal witnesses cover that operation
and placement, not the whole helper or variable/record ingress. The Bool-only
arm gate reuses the existing exact entry-comparison validator: two ordered
bounded signed-i64 literals, matching opcode/source operation, Bool result and
nonconstant payload marker. Both arms validate before execution.

No opcode, projection role, layout, public key/arity, interpreter or resource
bound changes. The original incoming comparison result transfers to the join;
the selected comparison consumes fuel to 505, while skipping it leaves 507
from the same 512. Multiple computed placements, nested CFG, Keyword equality,
other comparisons, variables and calls remain unsupported.

The current candidate passes all nine bridge tests/2938 assertions: 884 new
equality-arm assertions and all previous 2054, including the 676 less-than-arm
assertions. The shared test helper preserves the existing less-than cases;
only the now-supported equality rejection changes to unsupported `integer-lte`.
Separate normalized branch controls pass 1 test/20 assertions and boundary
checks 6/11: 16 tests/2969 assertions across three passing invocations, not one
batch. Source LOC passes 2021 files, zero exceptions, maximum 250 lines.
Cases cover equality and inequality, signed endpoints, both branch positions
and condition values, invalid unselected operands, source/incoming/MIR tampering
and recomputation. No C10/source ingress, whole-helper lowering, generic MIR,
native execution, SH-13 completion or authoritative migration is claimed.

### Original MIR entry integer less-than-or-equal: executable increment

C11/C12/C18 and L2/L5 govern a new bounded SH-12-to-SH-13 operation. The
original producer already constructs and verifies `:integer-lte`. The existing
normalized interpreter now accepts `:lte` over exactly two available signed-i64
values and returns Bool using `<=`. The original producer bridge maps the
opcode and retains the new `:integer-less-than-or-equal` projection role,
original operation IDs, source/fact records, CFG, DFG and incoming values.

At this initial increment, bridge support is entry-only: seven total rows with entry/then/else widths
`[3,1,1]` and matching scalar literal arms. Both arms validate before execution. Explicit
placement and arm fences continue rejecting computed-arm `<=` and an entry
`<=` combined with computed Eq/Lt arms. Existing Eq/Lt arm behavior remains.
Signed endpoints, equality and both branch outcomes execute once with the
selected trace and fuel 505. Public keys/arities, depth 32 and fuel 512 are
unchanged; the normalized opcode and projection domains grow explicitly.

The test-only `23d32fc2` RED recorded 220 passes / 180 failures / zero errors
in the new bridge test's 400 assertions. At `a20b282d`, the bridge passed
1 / 400, the full normalized namespace 21 / 2,626, existing bridge controls
16 / 7,274, original SH-12 producer controls 2 / 578 and boundary checks
6 / 11. This is 46 tests / 10,889 assertions across five passing invocations,
not a single batch or a sum including RED. LOC separately passed 2,022 files,
zero exceptions, maximum 250 physical lines. Retain the original
`/tmp/d-sh13-lte-*-v1-20261021` logs and timing records.

Missing/extra operands, unavailable definitions, non-i64 or out-of-range
values, unselected invalid arms, origin/effect violations, altered MIR or
incoming IDs and substituted results reject. Verification recomputes through
the existing interpreter; no new interpreter or wrapper is introduced. These
are supplied constructor and fresh original MIR builder/verifier compositions,
not fresh C6/C10 source ingress, native execution, whole-helper lowering,
SH-13 completion or authoritative migration. Future stable migration must
account for the changed normalized operation/projection domains; no historical
proof is repinned by this development increment.

### Original MIR selected-arm integer less-than-or-equal: executable increment

The follow-on `2b4406e8` extends the same C11 bridge to a literal Bool entry
and one three-operation signed-i64 `<=` Bool arm. Seven total rows have
entry/then/else widths `[1,3,1]` or `[1,1,3]`; the other arm is one Bool literal.
Both arms validate before execution. Original comparison IDs and transfers
survive, with fuel 505 when selected and 507 when skipped. Entry-only `<=`
remains `[3,1,1]`; old Eq/Lt compositions remain supported. Computed-entry,
truthiness-entry and both-computed-arm `<=` compositions still reject. No new
opcode, role, public key, arity, interpreter or bound is introduced.

C11's origin-chain and i64 predicates provide actual guarded `<=` clients;
supplied literal witnesses cover placement, not those whole helpers or count
lowering. Four passing invocations cover 28 tests / 9,356 assertions: new arm
1 / 1,172, existing bridge 17 / 7,674, normalized controls 4 / 499 and boundary
6 / 11. LOC separately passes 2,022 files, zero exceptions, maximum 250 lines.
Preserve `/tmp/d-sh13-lte-arm-*-v1-20261021`, including the separate RED epoch
of 452 passes / 648 failures / 72 errors in 1 / 1,172. The errors are absent
projection-role inspections after rejection, not comparison execution errors.
This is development producer/consumer evidence, not fresh C6/C10 source
composition, native execution, SH-13 completion or authoritative migration.

### Two-sided MIR integer span guards: executable increment

At `d3653edc`, the existing C11 layout gate accepts entry `<=` plus one `<=`
arm in nine-row `[3,3,1]` or mirrored `[3,1,3]` diamonds. The opposite arm is
a Bool literal, both arms validate, and selected/skipped computed arms leave
fuel 503/505. C11's span predicates provide the two-sided guard client. Literal
pairs model its relation without claiming member/parameter dataflow or whole
helper execution. Existing seven-row and Eq/Lt behavior remains; mixed
comparisons, truthiness and both-computed-arm `<=` layouts still reject.
No opcode, role, public key, arity, interpreter or resource bound changes.

Four passing invocations cover 29 tests / 9,956 assertions, with separate
LOC 2,022 files / zero exceptions / maximum 250 lines. Preserve
`/tmp/d-sh13-double-lte-*-v1-20261021` and its separate RED 276 passes / 360
failures in 1/636. The prior entry/arm test counts become 384/1,152 solely
because 36 now-supported composition assertions no longer expect rejection.
No historical run is relabeled. Fresh source/native composition, SH-13
completion and authoritative migration remain separate outstanding boundaries.

### MIR integer greater-than: executable increment

At `342610b9`, C11's normalized `:gt` compares two available signed-i64 values
and produces Bool. The existing original producer bridge accepts `:integer-gt`
with explicit `:integer-greater-than` role in seven-row entry or single-arm
diamonds and nine-row GT-entry/GT-arm composition. Scalar literal returns,
both-arm validation, original identity/provenance/facts and recomputing result
verification remain. Mixed comparisons, truthiness, DO/let and both-computed
GT arms still reject; depth 32 and fuel 512 are unchanged. This adds an explicit
opcode/projection domain, not keys, arities, a new interpreter or authority.

Four passing invocations cover 48 tests / 14,430 assertions, with separate LOC
2,022 files / zero exceptions / maximum 250 lines. Preserve
`/tmp/d-sh13-gt-*-v1-20261021`; the separate RED is 900 passes / 1,260 failures
in 1/2,160, with no normalized RED invocation. C11's capability helper motivates
the guard shape, but these literal count premises do not establish count or
helper execution, capability/proof authority, fresh source/native composition,
SH-13 completion or current authoritative admission.

### MIR conditional-condition dominance: development repair

Normalized conditional branches now report `C11-DOMINANCE` when the condition
is not in the current block's parameter/ordered-operation type table, rather
than falling through to `C11-TYPE`. Defined non-Bool values retain the type
diagnostic. The membership check follows existing opcode/source/origin and
whole-block operation gates, precedes the Bool check, and leaves the original
then-before-else target ordering intact. No accepted language, schema, public
interface, runtime behavior or resource bound changes.

The test-only RED commit `ac0c13f0` completed 124 assertions with 114 passes and
ten failures, all the wrong TYPE rule for undefined conditions. That failed
epoch is retained. The corrected `254ff6f1` passes five focused tests/560
assertions: 124 new, 20 conditional execution, 12 loop/indirect calls, 102 return
dominance and 302 branch-carrier assertions. Separate boundary checks pass
6/11, giving 11 tests/571 assertions across two passing selections, not one
batch. Source LOC passes 2021 files, zero exceptions, maximum 250 lines.

Original G/Q normalized constructors, the executor and recomputing verifier
cover missing/Nil/absent/later-block conditions, defined i64, competing bad
targets, earlier source/opcode/profile gates, valid results 42/7/120 and result
substitution. These supplied normalized inputs do not establish fresh source
or C10 ingress, native execution, SH-13 completion or authoritative migration.

### Original MIR composed comparison placements: executable increment

The bounded original handoff now composes a computed entry condition with one
computed Bool arm, matching the operation placement of compiler frontier/visited
count guards. Literal signed-i64 witnesses exercise Eq/Lt combinations, not
actual count, collection, variable or whole-helper ingress. Only layouts
entry/then/else `[3,3,1]` and `[3,1,3]` are added. The original SH-12 producer
and verifier, exact Eq/Lt signatures, both-arm pretrace validation, canonical
source/fact/DFG joins and original incoming-value transfer remain unchanged.

This is an additive placement and nine-row projection-cardinality domain,
not new opcodes, roles, keys, public arities or interpreter machinery. Existing
five- and seven-node outputs remain. Both computed arms and all three computed
placements still reject. The same depth-32/fuel-512 interpreter leaves 503 when
the computed arm executes and 505 when skipped, without increasing a bound.

The first attempt at `96bbd40a` exited 1 after 208 passes and one test-helper
arity error; nine other selected tests were unrun. It remains failed. A one-line
test-only correction at `bcd4435d` calls the two-argument coordinates helper
before selecting the arm. Production is unchanged by that correction. The
corrected invocation passes all ten tests/3802 assertions, including 864 new
and all prior 2938; the earlier 208 are not added again. Separate normalized
branch controls pass 1/20 and boundary checks 6/11: 17 tests/3833 assertions
across three passing selections. Source LOC passes 2021 files, zero exceptions,
maximum 250 lines.

The new cases cover entry/arm Eq-Lt combinations, mirrors and selected/skipped
arms, nine ordered source/fact rows, incoming IDs, invalid unselected domains,
unsupported layouts, DFG/operand tampering and result recomputation. This does
not establish fresh C10/source ingress, generic MIR, native execution, whole
compiler execution, SH-13 completion or authoritative migration.

### Scalar SSA forwarding and branch-local let: executable increment

C11's existing interpreter now supports one pure scalar SSA forwarding opcode,
`:local`. It requires exactly one already-defined operand of matching i64,
Bool or Keyword type, preserving earlier definition/effect/fact/source checks.
It forwards the value without reevaluating its producer, retains the original
operation ID in the trace and consumes one existing fuel step. This explicitly
extends the normalized operation domain; no new interpreter, cache or wrapper
is introduced.

The original SH-12 producer/verifier handoff now executes exactly one
branch-local scalar `let` in layouts `[1,4,1]` or `[1,1,4]`: literal initializer,
binding, local reference and scope, opposite a matching literal arm. Both arms
validate before execution. Actual Symbol names, scalar IDs, resolved binding,
one non-shadowing binding, original operand order and plural `:bindings` paths
join the supplied source/fact records. Scope forwards its second operand, the
local-reference result, not the binder. The initializer executes once when
selected. Its three original alias IDs remain visible in forwarding traces.

This adds three projection roles and an eight-row domain; public keys/arities
are unchanged. Existing five/seven/nine-node results remain. Depth 32 and fuel
512 remain, leaving 504 for selected lexical execution and 507 when skipped.
No parameters, cross-block captures, collections, two lexical arms, combined
entry-comparison/let layout or general lexical interpreter is enabled.

The first attempt `ea8cebe7` exited 2 during test namespace compilation, with
zero tests/assertions and no timing receipt. Two test delimiters had closed a
local test scope too early. The test-only correction `3fd567c6` leaves production
and bridge-test bytes unchanged. Its focused invocation passes all 16 tests/
4811 assertions: 163 new normalized-forwarding, 588 new bridge assertions and
4060 affected regressions. Separate boundary checks pass 6/11, giving 22 tests/
4822 assertions across two passing invocations. Source LOC passes 2021 files,
zero exceptions, maximum 250. The failed load epoch is not semantic evidence.

These are original G/Q normalized constructors and supplied SH-12 checked-Core
lexical witnesses, not actual C6/C10 adaptation. C6's `:gravity/*` type family
and truthiness nodes still require a separately validated adapter to this
`:gravity.type/*` interface. Retained lexical attributes/source-form IDs do not
independently establish real source provenance. Fresh source ingress, native
execution, whole compiler/helper behavior, SH-13 completion and authoritative
migration remain outstanding. Consumers must account for the new normalized
opcode/projection domains when selecting a future stable migration revision;
no historical proof is repinned or renewed by this development merge.

### Bounded scalar truthiness: executable increment

The same C11 interpreter now executes one pure normalized `:truthy` operation
over an already-defined Bool, i64 or Keyword value, producing Bool. False
selects false; every other supported value selects true. Nil, Strings and
collections are rejected by this bounded interface. This does not change L2's
source truthiness law, where Nil is false, or implement its complete domain.

The original SH-12 builder/verifier and same interpreter compose a new exact
six-node `[2,1,1]` diamond: literal input, truthiness, two literal arms and the
original join/return records. A new `:truthiness` projection role preserves
original IDs, source/fact rows, CFG, incoming value and selected trace. The
literal executes once; fuel remaining is 506 within the unchanged 512 bound.
Existing five/seven/eight/nine-node domains remain unchanged.

The initial attempt at `6d3fe73d` failed: one bridge test, 178 passes, 480
failures and 48 secondary errors across 706 assertions; the normalized test
was unrun. The original upstream gate rejected the actual truthiness source
path before execution, and later test projections encountered absent outputs.
The changed candidate `a03adc37` preserves the old condition-path prefix and
additionally accepts only original source-op `:truthy` at exact
`if-path + :truthy`. The bridge requires input at `if-path + :test`, fixed
`[nil false]` metadata and canonical Bool result type. Wrong operators,
suffixed/unrelated paths, contradictory metadata, malformed operands, unsupported
types, unselected-arm errors and altered MIR/results reject. This is an
explicit upstream accepted-path-domain addition, not an unchanged interface.

The changed tests passed 2/869 assertions (722 bridge and 147 normalized).
Fourteen affected existing selectors passed 4,671 assertions; six boundary
tests passed 11. Thus 22 tests/5,551 assertions passed across three invocations,
excluding the failed attempt. LOC separately passed 2,021 files, zero
exceptions, maximum 250. These supplied G/Q constructor witnesses do not
establish fresh C6/C10 source adaptation, actual source provenance, native
execution, general truthiness, SH-13 completion or authoritative migration.
The different C6 type family and stable C11 source binding remain separate
integration/admission dependencies; no historical proof was refreshed.

### Explicit Nil scalar transport and truthiness: executable increment

The normalized C11 interpreter and original-producer bridge extend their
scalar domain with explicit Nil. This is not only a condition exception:
matching constants, parameters, local chains, branch/backedge arguments,
direct and allowlisted indirect calls/results, returns and existing typed
exits can transport Nil. Same-type Nil equality is true. Arithmetic, integer
ordering, mixed-type equality and existing effect violations remain rejected.
The original interpreter now yields false for Nil and false, preserving L2's
law for the supported scalar subset.

Nil constants require explicit field presence. Normalized records must contain
`:literal nil`; bridge payloads must contain `:value nil`, and every original
Nil constant must be an original `:literal` node whose attributes contain
`:value nil`. Missing fields cannot become fabricated Nil through a defaulting
lookup. Validation covers unselected and lexical arms before execution.
Original `:implicit-nil` and `:quote` nodes remain excluded by this bridge
subset; actual C6 generated origins and type-family adaptation are not claimed.

Nil returns work in the existing five-node literal, six-node truthiness,
seven-node entry-comparison and eight-node lexical layouts. Their old fuel,
source/fact, incoming-value and trace rules remain. Computed comparison arms
still return Bool. No public keys, arities, opcodes, projection roles or bounds
were added; the scalar type/value domain explicitly grew.

The first frozen candidate `4e2e2abc` remained unrun: static review found one
test scope delimiter and an incoherent Nil-arm fixture. The two test-only
corrections at `8a0993d4` left production unchanged. Its first execution passed
both new tests/584 assertions (294 bridge and 290 normalized), including
missing-field, wrong-value/type, source/fact, arity, effect and recomputation
rejections. Seventeen affected existing selectors passed 6,070 assertions and
six language-boundary tests passed 11: 25 tests/6,665 assertions across three
passing invocations. LOC separately passed 2,021 files, zero exceptions,
maximum 250. There is no failed execution to relabel. These supplied constructor
results do not establish fresh source ingress, native execution, milestone
completion or authoritative migration.

### Pure two-expression branch-local do: executable increment

The original SH-12 builder/verifier and existing SH-13 interpreter compose one
pure two-expression `do` arm under a literal Bool condition. Both body roots
are independently closed i64, Bool, Keyword or explicit Nil literals; the arm
returns the second value and the opposite arm has the same result type. Both
arms validate before execution. Original ordered IDs, `:body 0`/`:body 1`
paths, `:body-count 2`, type/source/fact records, incoming sequence ID and full
CFG remain visible. A selected first false or Nil is an ordinary value, not a
control transfer; both literals execute once in order.

The private frame maps the original sequence result to the already supported
SSA-forwarding operation on its second operand. It retains both constant
operations and the original sequence ID in the trace. This adds the explicit
`:sequence-result` projection role and a source-arm domain, not a normalized
opcode, interpreter, public key or arity. Only seven-row `[1,3,1]` and mirrored
layouts are selected; fuel is 505 selected or 507 skipped under 512.

The first literal-entry test passed 1/1,012 assertions. Review then found that
the draft also admitted an unselected nine-row computed-entry/`do` combination.
Its placement regression recorded 16 passes and 24 failures in 40 assertions,
with no errors. The narrow fence at `ce529e30` requires a literal-width entry
for a `do` arm, preserving existing nine-row comparison families. Both new
selectors then passed 2/1,052 on the changed candidate; 16 affected existing
selectors passed 5,687 and six boundary tests passed 11. Thus 24 tests/6,750
assertions passed across three current-candidate invocations. The earlier
literal pass and placement RED are not added again. LOC separately passed
2,021 files, zero exceptions, maximum 250.

Invalid unselected bodies, metadata, types, source paths, Nil field presence
and altered MIR reject before execution. Substituted results reject through
fresh recomputation and comparison. Empty, nested or effectful
bodies, calls, captures and implicit Nil remain unsupported by this bridge.
Supplied G/Q constructor witnesses do not establish actual C6 type-family or
source adaptation, native execution, whole-compiler behavior, SH-13 completion
or authoritative migration.

### Scalar truthiness with branch-local do: executable increment

At `0dbb01ca`, the existing scalar truthiness entry composes with the preceding
two-expression `do` arm. The additive placement domain is eight-row `[2,3,1]`
or its mirror, and the three-operation arm must end in `:sequence`. Truthiness
entry plus Eq/Lt arm remains rejected, as does computed Eq/Lt entry plus `do`.
Existing seven-row `do` and nine-row comparison behavior remains supported.
There is no new opcode, projection role, public key, arity, interpreter or cap.

The literal input and truthiness operation execute once, followed only by the
selected arm. Nil and false select the false branch; zero and Keywords select
the true branch. Both selected body literals execute once left-to-right, and
the original sequence ID transfers the last value. Fuel is 504 selected or
506 skipped under the unchanged depth-32/fuel-512 bounds. Both arms retain
full source, type, fact, body-path and explicit Nil-presence validation before
execution. Altered MIR rejects before execution; substituted results reject
through fresh recomputation and comparison.

The new selector passed 1/816 assertions; 18 affected existing selectors passed
6,739 and six boundary tests passed 11. Thus 25 tests/7,566 assertions passed
across three invocations, without a failed execution epoch. LOC separately
passed 2,021 files, zero exceptions, maximum 250. G/Q supplied constructor
controls preserve all eight operation IDs, facts, source records, incoming
sequence ID and selected-only trace. These are development composition checks,
not actual C6 type-family/source adaptation, native execution, whole-compiler
behavior, SH-13 completion or authoritative migration.

### Normalized C14 return-type consistency: development repair

C14 now rejects a normalized function when its terminator-selected value's
declared definition type differs from its declared return type. The same
bounded definition-order traversal records types after the original duplicate
and operand-defined checks; the return-type comparison follows the original
return-value-defined check. This adds no type grammar, opcode, ABI rule or
schema and does not repin the historical C11 revision.

The regression initially completed 120 assertions with 78 passes and 42
failures; that failed result is retained. It now passes all 120 assertions.
Together with normalized-schema, three-target, target-substitution and
result-substitution controls, the batch passes 5 tests and 239 assertions.
Matching Integer and Bool declarations and incompatible returns are checked
through the original builder and recomputing verifier for LLVM, C and Wasm
normalized requests. This is neither fresh authenticated C11/SH-16 ingress,
native code generation/execution, SH-17 completion nor authoritative migration.

The same normalized boundary now checks the existing integer-operation
signatures: binary Integer addition returns Integer, and binary Integer
equality returns Bool. Operand/result declarations are checked after original
duplicate and operand-defined gates using the existing bounded type table;
other opcode families are unchanged. This adds no widths, overflow semantics,
coercions, constant-value typing, type grammar or native arithmetic execution.

Its regression initially completed 294 assertions with 150 passes and 144
failures; that failed result is retained. The same regression now passes 294
assertions for LLVM/C/Wasm requests. With the existing return-type, schema,
three-target and substitution controls, the batch passes 6 tests and 533
assertions. These remain normalized-input and recomputation checks, not fresh
upstream authentication, native execution or milestone-completion evidence.

### C14 scalar local forwarding: development repair

The internal C14 C/LLVM/Wasm eligibility gates now require `:local` and
`:local-binding` declarations to match the actual LAST operand's canonical
scalar type. Previously an Integer value could be forwarded unchanged while
declared Bool. Nonempty forwarding arity remains unchanged, including discarded
earlier operands; no unary rule is imposed. Existing source/effect/reference
gates keep priority, and unrelated forwarding families are unchanged.

The `34b75945` RED regression recorded 222 passes and 108 failures in 330
assertions, with no errors. Candidate `e3fd7b11` passed eight tests and 873
assertions (330 new plus 543 adjacent); the separate boundary run passed
six tests and 11 assertions: 14/884 across two passing invocations. LOC
separately passed 2,021 files, zero exceptions, maximum 250. The new tests
invoke the original three internal whole-surface gates and evaluator on
supplied MIR DATA. Adjacent public normalized SH-17 controls do not turn this
into a fresh authenticated C11/C13 packet, native execution or migration proof.

### C14 sequence and lexical-scope forwarding: development repair

The same internal C14 scalar-forwarding check now covers `:sequence` and
`:lexical-scope`. Their declared canonical scalar type must match the actual
LAST referenced operand, consistent with the original C6/C11 ordered `do`/`let`
body result. Nonempty multioperand arity and earlier source/effect/reference
gates remain intact; function-boundary handling is unchanged. No opcode, schema,
public API, bound or pin is added or changed.

The `e12e99af` RED run retained 222 passes and 108 failures in 330 assertions,
without errors. Candidate `856308cd` passed nine tests and 1,203 assertions:
330 sequence/scope checks, the factored unchanged 330 local checks and 543
adjacent controls. Boundary checks separately passed six tests/11 assertions:
15/1,214 across two passing invocations. LOC separately passed 2,021 files,
zero exceptions, maximum 250. The normal join of SOURCE PR149 preserved both
owned files; its C7/effects/typed-emitter changes are outside the executed raw
compiler-rule/host path, so these results retain their original candidate
binding rather than claim a joined replay. These are original internal
C/LLVM/Wasm eligibility gates and evaluator checks on supplied MIR DATA, not
fresh C11/C13 authentication, native execution, migration or release evidence.

### Closed C13-to-C14 integer equality: development increment

The current Gravity C13 builder can translate one closed actual-MIR-shaped
entry `:integer-eq` to a Bool constant when both ordered references independently
join earlier pure/proven-safe bounded-i64 literal producers. Repeated operands
retain two occurrences. Source operation, result identity, source/fact tables,
CFG, original C11 report/pass/preflight ancestry and unrelated operations remain
intact; unsupported cases retain the identity route. The derived constant is
admitted only by the selected LLVM surface, not shared C/Wasm literal guards.
The value payload and exactly two operand edges/producer uses are regenerated
with explicit data-flow/use-def invalidation and a new translation replay.

An independently derived four-row delta retains exact original edge/use
positions without embedding another MIR, operation or source/fact table. The
measured delta contains 127 carrier nodes including map entries. Its pure
stage-owned host reconstruction component is 183 physical lines and has one
explicit load-order entry. Full original inverse roundtrip and output
rederivation are required; C14 independently restores and rederives the same
closed translation. Original reports validate only restored input, never output
through patched counts. The path-neutral output hash excludes its own record,
evidence and seal. Existing 256-operation and carrier bounds remain; repeated
bounded scans/copies/hashing have no speed or free-sharing claim.

The test-only RED at `f6d39aa1` retained 33 passes and 27 failures in 60
assertions. Kernel V1 exited during loading with zero tests/no EDN; V2 retained
one error in one assertion from an unsupported raw core form. V3 at `482b22a2`
retained 185 passes and 73 failures in 258 assertions: the host selector's
misgrouped `when` returned nil. These separate failed attempts are not passing
coverage. Corrected `d5c9e4ec` passed V4 1/332, SH16 32/4,210, direct SH17
11/1,227, boundary 6/11 and existing load-order 1/4: 51 tests/5,784 assertions
across five passing invocations. LOC separately passed 2,022 files, zero
exceptions, maximum 250. The 332 checks include identity fallback, original
versus output/restored-report controls, malformed ingress and translation
source/fact/DFG/delta/result tampering.

These are supplied DATA through current Gravity semantic kernels and fresh
independent reconstruction, not a fresh compiler/source build, authenticated
public C11/C13/C14 packet or native execution. Current public source/pin/packet
boundaries remain unrun and unchanged; no milestone, admission, migration or
proof-provider credit is established.

### B3 folded comparison text emission: development increment

The existing B3 builder now accepts the C13-derived Bool constant that retains
`:source-operation :integer-eq`. Both existing source gates use one predicate:
the derived case requires canonical Bool type, exact actual Bool payload and
zero operands, with old envelope, purity, value-parity and error precedence
unchanged. Literal/implicit-Nil and nonconstant paths are retained. Complete
supplied original MIR goes through current C13 folding and independent C14
restoration/rederivation; the SAME output then emits LLVM text `add i64 0, 0|1`
without comparison/zext instructions. Original comparison and literal-prefix
records remain unchanged, including repeated operands and signed endpoints.

The first test attempt at `2831ca54` exited during namespace loading with zero
tests/no EDN. A two-line fixture-scope correction retained all assertions;
semantic RED `2e87d3e8` recorded 61 passes and 44 failures in 105 assertions,
without errors. Candidate `2e169267` passed 2/449: 117 emission checks including
12 added pending-host/input-lineage controls, plus the current 332 C13/C14
checks. Boundary separately passed 6/11: 8/460 across two passing test
invocations. LOC separately passed 2,022 files, zero exceptions, maximum 250.
The short boundary and LOC processes may briefly have overlapped before the
boundary terminal was polled; no strict serial trace or benchmark is claimed.

B3 still consumes upstream-verified MIR rather than independently authenticating
or reproving C13. The emitted record remains `:constructed-unverified` and
`:pending-host-content-binding`; legacy C11 closure fields are input lineage
and pending upstream obligations, not a fresh C11 proof of transformed output.
These are development supplied-DATA/current semantic kernels and LLVM text,
not public authenticated packets, pin renewal, LLVM tool verification, native
execution, migration, admission or milestone completion.

### Folded predicate to Integer diamond: affected composition coverage

Test-only `6a56ef04` covers complete supplied four-block MIR through current
C13 folding, independent C14 restoration/rederivation and the SAME output into
the existing B3 builder. True/false, repeated and signed-endpoint comparisons
select Integer arms 0, 7, 42 or 255. Exact branch/phi text, original/folded
semantic parity, only two removed comparison operand edges, retained CFG,
incoming values, source/type tables and terminator uses are checked. Mutants
reject at the actual fold, target-surface, original-context or B3 owning layer.
No production change or semantic repair was needed.

The first attempt `f63c4e16` exited during reading with zero tests/no EDN, not a
semantic RED. The corrected current invocation passed 1/120; boundary passed
6/11 separately: 7/131 across two passing invocations. LOC separately passed
2,022 files, zero exceptions, maximum 250. Prior 449 checks were not rerun or
resummed. Constructed/unverified and pending-host obligations remain unchanged;
there is no fresh source/public packet, LLVM tool/native execution, admission,
milestone or migration claim.

### B1 LLVM optimization-status compatibility: development repair

B1's LLVM input accepts the closed `:identity-pass-complete` and
`:constant-fold-pass-complete` enum, with exact nested/outer status agreement.
Optimization-report artifact/semantic/decision/verifier joins, error ordering,
C/Wasm helpers and source/dependency admission remain unchanged. The current
pure C14 expected-record projector supplies semantic request DATA; original
C11 report DATA describes input, not a fresh proof of transformed output.

RED `b6f1e273` retained 54 passes and two failures in 56 assertions. Current
`a3d8520e` passed 1/74; existing incomplete-ingress rejection passed 1/4 and
boundary passed 6/11: 8/89 across three passing invocations. LOC separately
passed 2,022 files, zero exceptions, maximum 250. The original-delegating scoped
observer confirms valid statuses reach the unchanged dependency predicate and
retain its actual rejection; mismatched/unknown statuses and bad report/target
inputs reject before it. Current file identities are retained with compiled
lineage explicitly unestablished, never replaced with a hypothetical old-pin
positive. No complete packet is accepted or sealed; there is no fresh public
source/pin renewal, LLVM tool/native, admission, migration or milestone claim.

### Pure host LLVM folded-Bool parity: development repair

The host operation rejection gate now agrees with current Gravity B3 for the
closed derived `:constant`/`:integer-eq` Bool case: actual empty Vector operands,
exact payload keys, true presence and a Boolean value. Legacy literal and
implicit-Nil behavior, earlier effects/capability/unsupported diagnostics and
all preflight/authority/artifact interfaces remain unchanged. Current C13
folds through C14 checks and the SAME B3 output are compared with the original
host rejection, rendering and evaluation helpers for linear and Integer-diamond
cases, including repeated references and signed endpoints.

RED `a7bb4d8b` retained 232 passes and eight failures in 240 assertions, all
at the host source gate. Candidate `977c2135` passed 3/501: 264 host-parity
checks plus 117 B3 and 120 diamond checks. Boundary separately passed 6/11:
9/512 across two passing invocations. LOC separately passed 2,022 files, zero
exceptions, maximum 250. The accepted SOURCE main join preserves both owned
files and the executed raw compiler-rule/generic runtime/LLVM host dependencies;
changed typed C7/effects/emitter bodies are outside this direct call graph.
Results retain their actual `977c2135` execution binding, not a joined replay.

This is supplied-DATA pure host-consumer parity, not proof of an observed
public-path failure, fresh source/public packet, preflight or pin renewal,
LLVM tool/native execution, admission, migration or milestone completion.

### Folded-Bool cross-target Clang validation: affected composition coverage

Test-only `1a6bec62` passes four unchanged current C13-fold/C14-inverse-and-
forward-check/B3 IR outputs to Clang: linear Bool true/false and four-block
Integer-diamond true/false. All four completed with exit zero and emitted
Linux x86_64 assembly. A separate undefined-SSA IR mutation completed with exit
one, the expected diagnostic and no assembly. This fills external tool
coverage, not a presumed production defect or manufactured RED.

The exact selector passed 1/72; boundary separately passed 6/11: 7/83 across
two passing invocations. LOC separately passed 2,022 files, zero exceptions,
maximum 250. All original runner processes exited naturally; the temporary
directory was removed. The later SOURCE main join changes only C7, its typed
call test and notes, outside this direct C13/C14/B3/raw compiler/harness route;
the one owned test stays byte-identical and results retain their actual
`1a6bec62` execution binding, not a joined replay. Prior 501 checks were not
rerun or resummed.

Apple Clang 21.0.0 reports host `arm64-apple-darwin25.5.0`; the explicit requested
target is `x86_64-unknown-linux-gnu`. Invoked `/usr/bin/clang` is a 118,928-byte
Apple launcher with SHA-256
`179301dcb41ea78accc3fa0048a7e6f6710d891945a751a34addd622020c1818`,
not a hash/pin of the effective Xcode compiler engine or full toolchain closure.
Exact commands, IR/assembly hashes and bounded process results remain
nonauthoritative observations. File-size checks are not hard filesystem quotas;
there is no hard aggregate wall/memory containment or benchmark claim. Supplied
MIR DATA and constructed/unverified, pending-host-binding B3 output do not
establish fresh C11/public packets, pin renewal or native admission. No linking,
generated-program execution, migration or milestone evidence is claimed.

### Native harness preserves noncompleted execution records

At `eedbeda8`, `execute-native` accepts only completed processes with exit zero.
Its timeout result stays distinct; output-limit and readiness-missing records
reject while retaining the original process, executable path/hash and arguments.
The original bounded output producer is unchanged. Test-only `ab071e83` exposed
the consumer's null-pointer failure: RED was 42 passes and 14 failures in 56
assertions, with zero runner errors. Four failures came from a real noisy binary
and ten from explicitly synthetic status records; those are separate evidence
boundaries, not native evidence for every status.

The repaired two selectors passed 2/56, including 19 real-harness checks and
37 synthetic checks. Apple Clang 21.0.0 compiled the fixed 120-byte C program for
host arm64 Darwin; its original execution captured exactly 1 MiB before rejecting
overflow and completing observed cleanup. Boundary separately passed 6/11:
8/67 across two passing test invocations. LOC separately passed 2,022 files,
zero exceptions, maximum 250. Original root sessions exited naturally; temporary
directories were removed and the existing native lease was released. Two earlier
GREEN resource preflights made no subject invocation; GREEN ran only once.
Results retain their `eedbeda8` execution binding. A later metadata-only test
assertion permits the existing discovery families instead of requiring Clang;
the observed Clang record satisfies both assertions, with no subject change.

The existing bounds were not increased. The tool identity does not pin the full
Xcode engine, observed cleanup is not universal process containment, and the
operational monitor is not a hard OS/RSS/filesystem limit. This is a tooling
failure-preservation repair using handwritten C, not Gravity source-to-MIR or
native compiler completion, authoritative migration, self-hosting or release.

### Typed List family predicate: development increment

The current JVM route supports unary `list?` for independently established
closed operand types, including bare, positional and homogeneous Lists.
C7 retains an explicit applied predicate and source-dependent constraints;
C8 reconstructs the joins independently. Normal operands return Bool and
Never preserves transfer. Unknown, dynamic, callable and malformed type
carriers remain unsupported by this typed subset. No member, cardinality,
nonempty or branch-refinement facts are inferred.

Typed lowering emits the ordinary unary builtin call with its operand compiled
once in non-tail position. The existing Gravity closed-plan evaluator now
dispatches `list?`, with no new helper/export and the same 55 functions.
PersistentList and EmptyList values classify true; Cons and vector-sequence
views classify false. Their admission as compiler DATA does not make them
Gravity List values. Classification does not evaluate stored payloads or
construct a collection; operand effects and control transfers are preserved.

The first focused run completed 14 of 23 selected tests: 2,233 passing and five
failing assertions. Thirteen complete green variables with 2,217 assertions
were retained, including all eight SH01 selections. The failed
List throw-consumer exposed omitted List rejection reasons in C7's shared
error/work forwarding; nine variables were skipped. That failed epoch remains
distinct from the narrow consumer correction and affected follow-up checks.
The correction forwards the List-specific rejection reason and actual charged
work without changing accepted paths, work charges or old-family behavior.
Its affected/skipped selection passed 10 tests and 1,259 assertions. Combined
nonoverlapping coverage initially reached 23 variables and 3,476 assertions
across two selections, not one clean 23-test run. Before source execution,
review found that the new STEP lookup guard preempted arity diagnostics.
The guard now checks only its actual lookup keys before delegating arity to
the owning derivation. Two affected C7 kernels passed 715 assertions,
including two new zero/two-operand STEP checks. Final nonoverlapping focused
coverage is 11 retained variables/1,504 assertions, 10 corrected variables/1,259
assertions and two affected variables/715 assertions: 23 variables/3,478
assertions across three selections. Unaffected green variables were not replayed.

The one affected source invocation passed one test and 151 assertions
(145 composition plus six loader checks). The accepted Gravity program's
three functions and 18 calls include 11 List predicates, three List
constructors, one rest, one prepend, two local calls and four inert quotes.
Original C7/C8 construction and verification, assembly's public C7 gate and
one closed-plan entry passed, producing
`[true true true true true true false false false false false]`.
Predicate records remain pure and non-refining; actual producer allocation
stays in the main function, not the namespace or classification helpers.

The arity negative reaches C7 and rejects with
`C7-TYPE-MISMATCH`/`:list-predicate-arity-mismatch`, retaining Core, syntax and
binding identities and a source span. A secondary unresolved-node annotation
also remains; this is not an exact-only diagnostic claim. The negative does
not enter C8 or runtime. Q has parser parity only. Public fresh C6 admission,
same-IR seed control, whole-runtime/B14 support, authoritative migration and
SH-07 completion remain separate; milestone counts are unchanged.

Source execution took 149.549 seconds after 11.724 seconds of namespace
loading. Positive/negative C5 construction took 32.385/21.265 seconds and
development C6 session construction 43.842/31.177 seconds. Positive C7
construction/verification took 2.351/1.066 seconds; C8 took 0.931/0.050 seconds.
These are inclusive development observations, not a benchmark or speedup
claim. The post-assembly interval includes projection and runtime-plan
acquisition as well as execution. No source construction was repeated for
the focused corrections or these notes.

### Finite List value equality: executable development increment

Binary `=` now compares ordered stored values of independently established
finite positional Lists, including empty and paired nested Lists, with supported
nonnumeric atomic leaves. Equal types can yield unequal values; counts, order
and corresponding values determine the result. Both operands follow ordinary
left-to-right evaluation and transfers, payloads stay inert and original values
are preserved. No member, cardinality or flow facts are invented. Bare or
homogeneous Lists, unknown/numeric/callable members, nil-or unions, other
collection families, nested Never and paired nested-List/scalar comparisons
remain unsupported. Nil itself is a supported leaf, not a nil-or union.

C7, independent C8 reconstruction, typed lowering and one bounded runtime
helper implement this route. Complete operand domains and pair compatibility
precede any false result under one cumulative budget. The tested malformed
source/identity and ledger controls reject without callback leakage; the ledger
uses a single paid scan. Prefix quota exhaustion retains its resource diagnostic. Existing atomic,
Vector and Set helper bodies/domains are preserved. The new runtime helper makes
the current inventory 56 functions; old 55-function records remain historical.

Focused coverage is 25 distinct tests and 4,072 passing assertions across three
selections: retained atomic runtime 49, corrected Set runtime 171, then 23 tests
with 3,852 assertions. It is not one clean 25-test run. The first aggregate
218P/2F/0E (23 skipped) exposed mixed-family dispatch regression; the second
171P/0F/1E (22 skipped) exposed an inappropriate host-reader fixture oracle for
a supplementary Character. Both failures are preserved; the fixture now uses
the owning C2 G/Q parser rather than changing reader semantics.

One source composition passes 183 assertions (177 composition plus six loader
checks). Its two functions and 15 calls include 12 actual equality instructions,
two List constructors, one local call and 22 inert quotes. All 12 Bool results
match, including genuine C2 BMP/supplementary Characters, nested values,
different contents/order/lengths and stored false/Nil. The typed Map-shaped
Character negative produces `C7-TYPE-MISMATCH`/`:list-equality-operand-type-required`
with actual Map types and Core/syntax/binding/span data, plus a secondary
annotation diagnostic. Negative C8/runtime execution is not claimed.

Boundary checks pass 6/11; LOC validation passes 2,021 source files, zero
exceptions and maximum 250 physical lines. The source check took 161.113 seconds
selected plus 12.582 seconds namespace loading. Positive inclusive C5/C6
construction intervals were about 34.56/45.12 seconds; these observations
separate product construction from test loading, not provider CPU time or a
speedup benchmark. The post-assembly interval includes runtime-plan acquisition.
No old source corpus or hour-long C17 subject was replayed. Public fresh C6,
seed-control execution, whole-project migration, full SH-07, self-hosting and
release remain unestablished by this slice.

### Finite List value inequality: executable development increment

Binary `not=` now complements binary List value equality for the same finite
positional, nonnumeric domain, including empty and paired nested Lists. Nil
leaves are supported; nil-or unions, unknown/bare/homogeneous Lists, numeric or
callable members, nested Never, other collection families and paired
nested-List/scalar comparisons remain unsupported. Top-level Never retains
the existing non-invoked behavior after complete normal-domain validation.
Operands evaluate left-to-right once and remain non-tail; stored payloads are
inert and no flow or member facts are inferred.

The existing C7, C8 and emitter ANE routes each consume their local List equality
domain once for a selected List-domain classification, retaining actual work
and source/fact reconstruction. Lowering
uses one owned conditional around one builtin equality, not constant folding
or a runtime inequality function. Runtime inventory remains 56; existing
atomic, Vector, Set and List equality helpers are unchanged. The selected
malformed identity, Symbol, form and mixed-ledger fixtures observe no selected
opaque callbacks; this is not a universal hostile-DATA or CPU-bound claim.

All 27 distinct focused selectors have passing results: retained partitions
3 tests/6,984 assertions, 2/817, 14/5,369 and 8/1,423, totaling 14,593 passing
assertions. This was not one green 27-test invocation. Three failed aggregate
epochs remain failed: 7,452P/10F, 847P/2F and 5,494P/9F, all without errors.
The corrections were test-only: identity remapping had changed semantic tags;
one forgery was unchanged and two rejection cases were already valid; a
missing-work default disagreed with the unchanged consumer; and C8 preparation
was incorrectly expected to enforce the final consumer's source joins. No
production gate was weakened or extra verification walk added. A wrong-directory
launch and unsupported boundary timing option also failed before tests; their
logs are retained separately, without test or no-JVM claims.

One source composition passes 232 assertions, including six loader checks.
Two functions and 15 calls produce 12 List-inequality descriptors/ledger rows,
12 owned conditionals and underlying equality calls, two List constructors,
one local call and 22 inert quotes. All 12 expected Bool complements pass,
including genuine C2 BMP/supplementary Characters; stdout is empty. Function
allocation effects and operand order survive, while the queries themselves
have no direct allocation effect. A numeric-List negative rejects with
`C7-TYPE-MISMATCH`/`:atomic-inequality-operand-type-required`, two actual
List-of-Integer types and Core/syntax/binding/span references. A secondary
annotation diagnostic is retained; negative C8/runtime execution is not claimed.

Boundary checks pass 6/11; LOC validation reports 2,021 source files, zero
exceptions and maximum 250 physical lines. Source selection took 163.923
seconds plus 12.247 seconds namespace loading. Positive inclusive C5/C6
construction intervals were about 35.48/46.65 seconds; negative intervals were
24.75/33.53 seconds. These distinguish product construction from test loading,
not provider CPU time or a speedup benchmark. The post-assembly interval includes
projection and runtime-plan acquisition. No old corpus, seed-control entry or
hour-long C17 subject was replayed. Public fresh C6, whole-project migration,
full SH-07, self-hosting and release remain outstanding.

## Beyond First Self-Hosting

After `SH-29`, target expansion becomes a separate matrix. Each canonical
target must repeat the applicable `SH-17` through `SH-20` target gates plus
backend conformance, runtime/provider, packaging, and external execution gates.
The first seedless compiler therefore establishes the compiler authority needed
to implement every target; it does not claim that every target is already
implemented.

## Outputs and Artifacts

This plan expects each completed slice to produce:

- a slice manifest naming dependencies and governing documents;
- accepted and rejected fixtures;
- executable artifacts and stable diagnostics;
- focused, regression, and adversarial test results;
- authenticated pass/artifact records where applicable;
- a TCB boundary record;
- stage comparison evidence for bootstrap slices;
- an evidence-ledger entry linked from the owning phase roadmap.

## Conformance Criteria

- All 30 slices have unambiguous dependencies and executable exit gates.
- No slice receives completion credit for scaffold, source inventory, proof
  metadata, or public fixture routing alone.
- Parallel work has non-overlapping file ownership and a single integration
  owner for central routing and generated evidence.
- The count and continuous completed prefix can be reproduced from reviewed
  evidence bundles.
- `SH-29` cannot pass while any compiler, runtime, standard-library,
  package/build, verifier, or release path inside the public boundary depends on
  Clojure.
