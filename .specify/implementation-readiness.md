# Implementation-readiness reconciliation

Scope decision: Matt's 2026-11-01 request. Initial inspected main:
`867554942e3dc1f08ed5a712e8c249ded84908a3`.
This is a readiness report and navigation into existing authorities. It is not
another implementation task ledger, acceptance graph or completion denominator.
Reconciliation remains in progress; independent acceptance and integration of
this change are pending. No compiler or product completion is claimed.

## Existing authority and preserved work

The [canonical inventory](../docs/document-inventory.json) retains all 240
contracts. [Phase 18](../docs/phase-18-binary-distribution-and-seedless-release/README.md)
is a separate release extension. The [full-language gap map](../docs/full-language-implementation-gap-map.md),
[SH completion plan](../docs/self-hosting-milestone-completion-plan.md) and
[seed-retirement gate](../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md)
retain their existing obligations and evidence boundaries.

The existing [requirement reconciliation PR445](https://github.com/burn2delete/uniform/pull/445)
and its source-family successors remain the source-to-task reconciliation
lineage. The preserved aggregate inspected at commit
`55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6`, tree
`57fd99ba53d8a876d8428ee4a37e63edbfd03ce0`, records qualified acceptance of an
explicit partition coverage census only. Its global semantic partition, full
conjunction/owner entailment and current implementation evidence remain
unassessed. It is not integrated into the inspected main. This report neither
imports that aggregate nor retries its stopped public-contract-cluster handoff.
Existing source-family owners and canonical task writers retain their scope.

At the inspected main, 32 feature `spec.md` files exist under `specs/`, of which
24 are component workflows, plus two historical `.specify` packages. Across
their spec/plan/tasks/evidence artifacts, all 240 canonical documents have a
direct Markdown reference. The earlier [contract mapping](contract-mapping.md)
reports its historical 236-link population. Later
[Release governance consumer links](../specs/release-workflow/plan.md#governance-evidence-consumers)
explain the four additional references. These observations establish navigation
only; they do not establish substantive requirement coverage or implementation.

Source applicability was checked separately against the preserved
`source-task-inputs.edn` at that aggregate (SHA-256
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`).
Of its 240 canonical document pins, 239 match the inspected main. STD3 differs:
the preserved hash is
`58d096cc16d922ad72d560c5b3e4d60d093c9527a9472c2414d63deb98630e3d`,
while current main has
`af847392b2ff50155d61a9a9c93bb6e2f1cc573220ceb11920de4dd483bd4160`.
The actual diff removes List-to-Set and Map-keys work/width allowances while
retaining complete physical member validation, allocation/lifetime semantics
and rejection of invalid data. Applicable STD3 requirement joins need that
explicit successor context; earlier source or execution acceptance cannot be
promoted to the changed contract. This observation does not rewrite the
aggregate's preserved pins or adjudicate its pending semantic partition.

## Implementation specifications and task authority

An implementation entry must describe behavior and interfaces, accepted and
rejected programs, artifact/diagnostic preservation and completion evidence.
Historical workflow-transfer delivery is preserved separately. The existing
marked `tasks.md` region remains the sole editable implementation authority;
new specification prose must reference its literal IDs and cannot silently
change their accepted scope or status. Unresolved finer-grained requirement
joins remain with the existing reconciliation owner, not inferred from a
component name or document link.

The expanded frontend entries are [Reader](../specs/reader-workflow/spec.md),
[Syntax](../specs/syntax-workflow/spec.md), [Expand](../specs/expand-workflow/spec.md),
[Resolve](../specs/resolve-workflow/spec.md) and [Core](../specs/core-workflow/spec.md).
Each retains its original spec/plan text as a historical suffix and routes
behavior to its unchanged marked task authority. Reader's
[planning entry](../specs/reader-workflow/plan.md) and
[task authority](../specs/reader-workflow/tasks.md#implementation-tasks) begin
the dependency selection below.
The checked-source entries now also cover
[Types](../specs/types-workflow/spec.md), [Effects](../specs/effects-workflow/spec.md),
[Legality](../specs/legality-workflow/spec.md) and [Auth](../specs/auth-workflow/spec.md).
They describe actual constraint/effect derivation, the required handler classes,
profile and safety enforcement, and provider/grant/artifact verification. They
route to the same existing tasks without adopting pending source-family task
amendments or promoting old receipts. Runtime handler interpretation, surviving
checks and actual action/verification boundaries require real composition.
The downstream entries are [MIR](../specs/mir-workflow/spec.md),
[Opt](../specs/opt-workflow/spec.md), [Lower](../specs/lower-workflow/spec.md),
[Emit](../specs/emit-workflow/spec.md) and [Release](../specs/release-workflow/spec.md).
They route real graph construction/verification, semantics-preserving passes,
target/ABI/provider lowering, emitted bytes and actual next-stage rebuilds
through their existing authorities. SH-25 construction, SH-26 actual rebuild,
SH-27 equivalence, SH-28 trust and SH-29 public delivery remain distinct.
The other component packages remain discoverable through the existing
[component registry](../docs/component-roadmaps/README.md#component-registry).
Their migration specs are not automatically accepted as complete implementation
specifications. No copied per-component progress table is maintained here.

## Dependency navigation and next increment

Use [D2](../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md)
for milestone order and the existing
[typed dependency representation](../docs/acceptance-dependencies.md) for
starting work, composition, acceptance and release. Its read-only audit reads
240 contract identities, 24 component authorities, 36 full-language task IDs,
30 SH milestones and the existing SH/task projection. These populations are
different views and must not be summed or used as an implementation percentage.
The declared staged graph has no cycle; its coarse cycles and the unresolved
Core/Resolve representation-order question remain explicitly documented there.
An advisory frontier does not grant execution permission or transfer a writer.

The first source-coverage increment is the existing
`reader/finite-remaining-work/contract-coverage-reconciliation` task in
`specs/reader-workflow`. It has no completed-Build or self-hosted-compiler start
prerequisite. It must bind the complete L1/C2 surface and product population to
actual implementation locations and real accepted/rejected fixtures, preserving
the exact accepted decode, lexical/origin and fresh-Syntax evidence. Missing
behavior must be represented by concrete failing cases and an owned correction,
not a claim based on source presence. This is substantive source/fixture
reconciliation in an existing task, not a new preparation-only admission gate
for ordinary development. It is not yet certified as the first
behavior-changing increment for the whole project: the source-backed gap and
current writer must be identified before that narrower selection can be made.

In particular, Reader's existing
[pure integration correction](../specs/reader-workflow/pure-native-admission-evidence.md#handoff-and-remaining-gate)
retains the genuine Reader/Gravity/Bootstrap, paired integration, provider,
lease, probe and shared-6343 hold. The
[standard-handler seam](../specs/reader-workflow/normal-dispatch-evidence.md#remaining-gates)
does not establish authenticated execution either. These are source-backed
remaining extension-policy obligations, not permission to rerun the held
transaction or assign its implementation to another writer. Coverage work can
identify and bind those existing outcomes without executing them.

Select that existing increment in Codex:

```text
$speckit-plan Use specs/reader-workflow. Select only reader/finite-remaining-work/contract-coverage-reconciliation; preserve accepted evidence, current ownership and all other task statuses. Bind complete L1/C2 behavior to actual implementations and real positive/negative fixtures, and identify the first concrete remaining implementation gap.
$speckit-tasks Use specs/reader-workflow and the same existing task authority. Reconcile the selected plan without replacing the marked task population or creating a parallel checklist.
$speckit-implement Use specs/reader-workflow and the selected plan/task. Complete the source-backed declared increment with actual products and rejection evidence; preserve restricted operations and leave broader outcomes open.
```

After that selected increment, the existing Reader ledger identifies extension
policy and authentication/quota-removal work and the component acceptance gate.
The existing integration outcomes then connect Reader/Syntax, expanded/resolved
Core, actual type/effect/legality/Auth facts, MIR, required optimization, selected
lowering/emission/runtime/library, Build/CLI and final release. Wider backend,
domain, library, tooling and Platform outcomes retain their independent required
surfaces. This paragraph is navigation into those existing outcomes; it adds no
new edge, task or completion claim.

## Remaining source decisions and restrictions

The dependency contract already records the D1 versus C1/C5/C6 representation
ordering qualification; it stays with the architecture owner. The selected
Linux LLVM compiler release does not adopt an ISA, kernel model, application
inventory or fabrication boundary for the wider Platform Product. Those
decisions stay in the existing
[Platform scope](../specs/platform-workflow/tasks.md#platform-product-scope).
The broader evolution lifecycle still needs its accountable policy owner and
real command-produced/release-blocking behavior; Release's four GOV consumer
links cannot confer that authority.

Current accounting-removal decisions supersede project work/fuel/capacity
requirements still present in contracts such as L1, L12 and C4. Preserve their
source occurrences and explicit superseded disposition until the existing
[accounting-removal outcome](../specs/accounting-removal/spec.md) closes the
owning cleanup. Semantic checks, real host restrictions and external billing
remain distinct.

C12 restrictions, the unresolved original C7 publication and other recorded
denials remain effective. Existing accepted source reviews are reusable only
within their actual qualifications; no stopped handoff or restricted operation
is retried, transferred or reframed by this report. The user has chosen no
messaging to other chats for this goal. Required independent review therefore
remains pending, without substituting author self-audit for acceptance.

## Completion evidence still required

The goal closes only when the existing source reconciliation accounts for every
substantive original and extension obligation, every required implementation
outcome has an actionable Spec Kit route, and unresolved decisions/restrictions
have explicit owners and resolution paths. Exact task/source entailment,
producer/consumer composition, truthful evidence applicability, required
validation, independent acceptance and coordinator integration remain separate
obligations. Link coverage, this report, or the Reader planning entry cannot
establish that result.

## Observed validation of the initial readiness packet

On the change based on the inspected main, both canonical document and
full-language roadmap structural validators exited 0. The existing dependency
tests passed 18 tests and 419 assertions. The read-only dependency audit
completed with 240 contract identities, 24 components, 36 full-language task IDs,
30 SH milestones, no staged or SH/task cycles and no missing task-authority
routes. It still reported `mapping-ready? false`; structural coverage is not
independent semantic acceptance.

Focused source/navigation checks passed for the five changed Markdown files:
ASCII, 62 local path/heading references, exact routing to all seven existing
Reader task IDs, byte-identical historical spec/plan suffixes and unchanged
Reader task/evidence files. `git diff --check` passed. No compiler, runtime,
provider, migration or release execution was performed for this packet. These
observations do not certify the other component specs or the full substantive
requirement population as implementation-ready. Independent review and
integration remain pending.

The subsequent frontend packet expands Syntax, Expand, Resolve and Core without
changing any implementation ledger or receipt. Focused checks passed all 13
changed Markdown files for ASCII and 171 local path/heading references, routed
all 27 existing frontend task identities, preserved all ten historical spec/plan
suffixes and confirmed all ten task/evidence files unchanged from the inspected
main. These are source/navigation conservation checks, not acceptance of the
27 implementation outcomes or an assessment of every attached source facet.

The checked-source packet extends Types, Effects, Legality and Auth. Focused
checks passed 21 changed Markdown files for ASCII and 352 local path/heading
references, all 47 existing task routes, 18 byte-identical historical spec/plan
suffixes and 18 unchanged task/evidence files. An initial ad hoc route selector
incorrectly counted an inline historical alias as a task and was corrected to
select actual checkbox records; no project task or criterion changed. Whitespace
checks passed. No compiler or privileged action was run for these entries.

The downstream packet extends MIR, Opt, Lower, Emit and Release. Focused checks
passed 31 changed Markdown files for ASCII and 533 local path/heading references,
all 72 existing task routes, 28 byte-identical historical spec/plan suffixes and
28 unchanged task/evidence files. `git diff --check` passed. This verifies
navigation and conservation across fourteen component entries; it does not
accept their implementation outcomes, the other ten component specifications,
the global source partition or a seedless release. No compiler or release
execution was performed.
