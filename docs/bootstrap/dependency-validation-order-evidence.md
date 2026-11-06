# Dependency validation order

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

Move the existing current-source-transition binding and its authenticated
path set after task, future/finish and requirement validation, before the
historical/source-digest section. Every successful result still executes the
complete existing current-source validation once, before any source-digest
exclusion or returned graph can use it. Platform and standard-library source
transitions retain their independent checks and order.

ACCEPTANCE-DEPENDENCY remains the diagnostic owner and stable code. For inputs
with both a malformed task/requirement and a stale or missing current source,
the structural task/requirement refusal now comes first. This intentional
first-error order is part of the correction. A source-only stale or missing
input must still reject through the original source checker or IO exception.

## Validation and acceptance

Retain every existing dependency test and forgery case. Add paired-fault
controls for task/requirement defects plus stale/missing current source, and
source-only controls proving complete current-source validation still runs.
Use the actual source frame and checked-out Git inputs; preserve the special
current-source reader boundary. Compare accepted old/new graph outputs and
deterministic identity in the same live context, and emit a real structural
graph from the production function. Record exact source and artifact hashes.

Run the complete existing dependency-controls command with its unchanged
512m/90-second policy, affected source-view consumers, boundary, lint, docs and
independent semantic/adversarial review. Preserve each failed or incomplete
attempt. Own hosted evidence and exact coordinator integration remain
separate requirements.

## Unsupported boundaries

This correction changes validation order only. It adds no cache, Git protocol
parser, accepted source frame, source pin, fingerprint, authority, task-status
promotion, project accounting, input quota or compiler/runtime implementation.
CI commands, selectors, watchdogs and the workflow remain unchanged.

The dependency graph remains a structural source projection. It authenticates
no implementation receipt or whole-language execution and grants no Main,
provider, platform, release, migration, self-hosting or seed-retirement credit.
No private compiler/source factory, native toolchain, controller or service
operation is involved.
