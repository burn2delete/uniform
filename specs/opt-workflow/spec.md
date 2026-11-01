# Feature specification: Opt implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: compiler optimization maintainer.
Input revision: `963f70c41b1da261da3a3acfdfe055f23cee6971`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable authority for
the five existing Opt implementation outcomes. The
[component roadmap](../../docs/component-roadmaps/opt.md) retains release,
accounting, admission and acceptance wording with a direct task-ledger link.
Retain direct navigation to its [release outcome](../../docs/component-roadmaps/opt.md#release-outcome)
and [remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work).
The [C13 contract](../../docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md)
remains normative. The reviewed [additional contract interfaces](plan.md#additional-contract-interfaces),
including PERF10, retain their exact text and authority boundaries.

## User scenarios and acceptance

### US1 - Find one editable implementation task

Preserve all five component/heading/title semantic IDs, opt/1 through opt/5
display labels, Open statuses and concrete exit criteria in the sole task
ledger. The roadmap links directly to those records. There is no second editable
implementation ledger or
renumbering.

### US2 - Preserve task meaning and acceptance requirements

The existing Opt -> Lower text remains literal ledger text. Other angle syntax,
images, malformed links and insecure receipt targets remain rejected. No shared
policy is broadened.

Preserve the complete historical tasks file outside the parsed implementation
region. Its checkboxes describe migration delivery, not compiler completion.
All five implementation outcomes remain Open and unchecked. A checked task
requires an explicit Accepted; status and independently reviewed immutable owner
evidence. The ledger records status separately from semantic receipt validity.

## Scope and compatibility

Own the Opt roadmap and four workflow artifacts. Preserve every source link,
historical record and all roadmap wording outside
additive task navigation. Preserve the reviewed plan's entire Additional
contract interfaces appendix byte-for-byte, including PERF10. Shared helpers,
mapping artifacts, normative contracts and compiler implementation/tests remain
unchanged.

## Evidence and outcome ownership

The compiler optimization maintainer retains semantic ownership. Verified MIR
and input facts still gate integration; Lower may consume unoptimized verified
MIR. Legal no-change decisions remain valid. A benchmark or pass-specific proof
is not blanket correctness evidence. Proof invalidation must regenerate proof,
retain checks or reject unsupported no-checks compilation. This migration
changes none of those requirements. See [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Metadata claiming a passed verifier is not execution-derived verification.
This workflow transfer establishes no pass implementation, check-elision proof,
producer/consumer acceptance, component release, self-hosting or seed retirement.

## Implementation naming review

`opt-workflow` and its component roadmap use the component key and behavior. Follow
[semantic naming](../../.specify/semantic-names.md); existing compatibility IDs,
historical planning references and source names remain unchanged.
