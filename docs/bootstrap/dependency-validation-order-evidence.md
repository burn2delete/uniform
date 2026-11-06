# Dependency validation order and graph work

## Purpose and governing contracts

D1/D2/D3/D6/D9, C15 and TEST2 govern this tooling correction alongside
docs/acceptance-dependencies.md and docs/development-verification.md. The
source parent is 7864c2c36b348b353b10792fc429302ff4807145, tree
759177e1674b4bb4bd09ba11000a059570120c93, including verified published
Main 41aaea100091dc80067328e9b066f7378845eca3. Inherited namespace
increments retain their own evidence and authority.

Repeated native namespace CI runs reached acceptance-dependencies-test and
timed out under the existing dependency-controls 90-second watchdog. Preserve
those exact NONPASS receipts. A read-only diagnostic of the existing complete
SH task forgery test reported 13 task-sh-view calls, including eleven complete
current-source transition validations. The latter consumed 49,253 ms of the
65,066 ms measured inside task-sh-view; source-bound span checks consumed
183 ms. This is a local observation with pass-through timing wrappers, not
hosted acceptance or a production performance guarantee.

## API, artifact and diagnostic boundary

Keep task-sh-view [crosswalk read-text], its full output schema and its
structural-only authority. Inputs remain the existing crosswalk, snapshots,
task/source records, checked-out source bytes and pinned Git objects. Outputs
remain the actual source-bound dependency graph, incidence, frontier and
unassessed evidence states. No source input, digest, snapshot, node, edge,
criterion, task, refusal fixture or validation rule is removed.

Keep current-source-transition and its authenticated path set before the
platform, standard-library, task, future/finish and requirement live reads.
Every successful result executes the complete existing current-source
validation once, before any source-digest exclusion or returned graph can
use it. Platform and standard-library source transitions retain their
independent checks and order.

Move duplicate task-ID, captured task status/ID/accepted-marker and requirement
prerequisite/applicability/task-reference checks before the independent source
checks that originally followed task-map construction. The initial shared-rule
and milestone reads remain unchanged. These checks still validate the same
snapshot-backed captured data. Move SH07 sequence population, uniqueness and
captured metadata checks early as well. Live
task-body reads remain after platform/standard-library transition validation;
no unvalidated transition path is read early. Requirement source spans and
historical spans and SH07 source spans retain all their checks after the pure
metadata refusals and the current-source authentication guard.

The real 4,421-node, 7,915-edge output graph also exposed duplicate cycle work:
two unchanged cycle computations took 8,468 and 9,414 ms in a local diagnostic.
Use iterative finishing-order and reversed-graph traversals to derive the same
mutual-reachability SCC partition without materializing every node's transitive
closure. Sort component members and the final component list canonically, and
reuse one cycle result for the view's refusal and returned record. No result
is reused across calls or source inputs. A 5,000-node chain and a closed cycle
exercise dependency depth without a recursive host stack.

ACCEPTANCE-DEPENDENCY remains the diagnostic owner and stable code. For inputs
with both a malformed task/requirement and a stale or missing current source,
the structural task/requirement refusal now comes first. This intentional
first-error order is part of the correction. A source-only stale or missing
input must still reject through the original source checker or IO exception.

## Validation and acceptance

Retain every existing dependency test and forgery case. Add paired-fault
controls for task/requirement defects plus stale/missing current source, and
source-only controls proving complete current-source validation still runs.
Pair stale/missing current source with absolute/traversal task, future-task
and requirement paths; assert the current-source refusal precedes any reader
call at those paths. This preserves refusal order, not a new general path
containment or normalized-input authorization API.
Exercise all 512 directed graphs on three nodes to check SCC partition,
mutual reachability and canonical ordering. Check that the actual view graph's
cycle result is computed once without dropping its cycle-refusal condition.
Use the actual source frame and checked-out Git inputs; preserve the special
current-source reader boundary. Compare accepted old/new graph outputs and
deterministic identity in the same live context, and emit a real structural
graph from the production function. Record exact source and artifact hashes.

Run the complete existing dependency-controls command with its unchanged
512m/90-second policy, affected source-view consumers, boundary, lint, docs and
independent semantic/adversarial review. Preserve each failed or incomplete
attempt. Own hosted evidence and exact coordinator integration remain
separate requirements.

## Development evidence

The exact accepted graph comparison rebinds both original function roots,
strongly-connected and task-sh-view, together from scope parent
8333ce28d022e0d9c6be082f539351ad133d40a6 in the same live source/Git context.
Raw records, parsed records and the complete real graph's SCC partition match.
The production function emitted a 4,421-node, 7,915-edge graph with 180
criterion incidences; all current criterion states remain unassessed.
Its SHA256 is
d39be4ad3e8d4415f98e1798fa6d740373e696786a220ab9b6f37760d42b4068.

The earlier metadata-order prototype comparison record is
/tmp/uniform-dependency-validation-order-metadata-parity.edn, SHA256
b2752411a181718ffc8929701c1a84ab2de2c9c78b1f31810ac33e09150bd6e8.
Its conservation record is
/tmp/uniform-dependency-validation-order-metadata-conservation.edn, SHA256
46519be1625687ef2592200d197d490985f2634f4ccca3e393f6801c984da0d1.
It reconstructs the exact production delta from the original source:
existing checks move without changing their bodies, the SCC implementation
changes, and one cycle result is reused. Eight surrounding authentication,
crosswalk, source-frame and CI paths remain byte-identical. All 28 original
test names and refusal cases remain; three tests are added.

The unchanged complete dependency-controls command timed out at 90
seconds for that prototype. Earlier order-only, sort-only and iterative
prototypes and their diagnostic/focused timeout attempts remain NONPASS
history. The latest progress diagnostic returned from the complete SH forgery
test after 39,027 ms, navigation drift after 26,380 ms and the paired-fault test
after 3,932 ms; it then timed out in the historical-routing test. These are
local observations, not performance or CI acceptance. No timeout is converted
to a passing result, and no source pin or watchdog is changed.

Independent precommit review identified that delaying current-source
authentication until after crosswalk-derived live reads changed an adversarial
ordering boundary. That proposal is withdrawn. The implementation restores
the original live-read guard and moves only the existing captured metadata
checks earlier. The prior prototype's passing comparisons and focused 4/832
result remain historical; its remaining-27-test focused command timed out and
does not confer full regression acceptance.

The corrected source conservation record is
/tmp/uniform-dependency-validation-order-guarded-conservation.edn, SHA256
73f1f04c84d7015a415f273ab89856e29210407a58f63a69f19dc870b50867fd.
The corrected-source complete 90-second command also timed out. The focused
graph/source/paired-fault group passed 4 tests and 868 assertions under the
unchanged 512m/90-second process policy. This is development feedback, not a
replacement CI selector or whole-command acceptance.

The fresh corrected-source comparison record is
/tmp/uniform-dependency-validation-order-guarded-parity.edn, SHA256
1cc13c630d27f8d283e8cce52a08a77bbd946a403b3b53bbd16933bf4dd192b2.
It again proves full raw/parsed graph equality and complete SCC partition
equality, and emits the same graph artifact identity recorded above.
The boundary gate passed 6 tests/11 assertions. The 240-document/19-index
ASCII validator and structural roadmap validator passed.

Focused development groups 0, 2 and 3 passed 29 tests/7,516 assertions with
zero failures/errors. The combined navigation/historical-routing group hit
the 90-second watchdog; it is not counted as passing. Repository CI vectors
remain unchanged and the complete dependency-controls command remains NONPASS.
The separately selected navigation regression also reached the unchanged
90-second watchdog. It remains an unverified local regression, with its
original fixture body and all nine platform-locator forgeries retained.
The separately selected historical-routing regression passed 1 test/4
assertions. In total, focused development checks passed 30 of the 31 test
variables and 7,520 assertions; navigation and the complete CI command remain
unverified. This aggregate does not claim the original suite passed.

## Unsupported boundaries

This correction changes validation order and redundant pure graph work. It
adds no cache, Git protocol parser, accepted source frame, source pin,
fingerprint, authority, task-status
promotion, project accounting, input quota or compiler/runtime implementation.
CI commands, selectors, watchdogs and the workflow remain unchanged.

The dependency graph remains a structural source projection. It authenticates
no implementation receipt or whole-language execution and grants no Main,
provider, platform, release, migration, self-hosting or seed-retirement credit.
No private compiler/source factory, native toolchain, controller or service
operation is involved.
