# Optimization implementation specification

Implementation scope added under the 2026-11-01 request. Existing optimization,
proof, domain and target owners retain their scope. The historical transfer
specification below establishes no optimization or performance acceptance.

## Behavior and interfaces

Implement [the Opt outcome](../../docs/component-roadmaps/opt.md#release-outcome)
under C13, D6, SAFE15 and the complete applicable PERF1-PERF10 rules in the
[contract alignment](plan.md#contract-alignment). Actual verified MIR plus
declared pass contracts, profile/target/features/providers, proofs, numeric
modes and applicable benchmark/PGO/tuning inputs produce optimized verified
MIR, deterministic pass/decision records, invalidation/regeneration evidence,
residual-check/cost reports and diagnostics consumed by Lower and tooling.

Optimization starts from safe semantics. Performance ranks legal candidates;
measurements cannot justify lost semantics or check removal. Required safety,
effect, capability, ownership, numeric, profile, taint and origin facts must
survive or be regenerated. Keep the full C13 pass families in scope; an actual
constant fold is useful progress, not whole-optimizer acceptance. Lower may
consume valid unoptimized MIR without waiting for every optional optimization.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`opt/current-evidence-and-remaining-work/`. Its existing criteria, status,
source-family refinements and receipts remain authoritative.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `opt1-execute-pass-contracts` | Validate requires/preserves/invalidates/regenerates/proofs/profile/target fields; deterministically execute actual constant, branch, dead-code and common-expression changes, with changed and unchanged decisions. | Reject absent analyses, false preservation, invalid order and changed source semantics. Emit actual before/after MIR and pass identities; registration alone is insufficient. |
| `opt2-implement-structural-families` | Implement inlining/specialization, closure/allocation/region/resource transformations, dispatch/loop/vector preparation and effect-safe scheduling under complete guards and boundary evidence. | Reject hidden effects/allocation, incomplete specialization keys, illegal ABI/persistence/address changes, loop-carried dependencies, alias/bounds/alignment gaps and volatile/MMIO/atomic reorderings. Preserve numeric modes, tails/fallbacks, generic source and generated origins. |
| `opt3-protect-checks-and-anchors` | Retain executable checks unless a named applicable proof dominates the actual operation; record erased and residual checks, invalidation and target assumptions. Domain/math transformations use actual verified anchors and governing whole-expression certificates. | Reject missing/non-dominating/stale proofs, check removal for speed alone, missing residuals and lost anchors. EML tree identity and per-call certificates do not establish fused EFIR equivalence; preserve existing restricted domain execution. |
| `opt4-verify-and-regenerate` | Actually run the MIR verifier after changes to flow/facts/anchors and regenerate invalidated analyses/proofs, retain conservative checks or reject the unsupported mode. Supply the required high-risk proof/translation/differential/domain validation. | Reject stale analysis/certificate caches and structurally valid but semantically changed output. A post-pass passed flag is not verifier execution or translation validation. |
| `opt5-integrate-reproducible-pipeline` | Compose actual MIR -> Opt -> Lower, recording source/compiler/pass/provider/target identity, no-change decisions, residual work and search/replay inputs. Apply PGO identity/privacy/staleness, legal-before-benchmark tuning, complete guards/fallbacks and reviewed performance baselines where used. | Reject unreplayable choices, stale required PGO data, invalid candidate benchmarking, hidden dispatch overhead, incompatible fingerprints and unjustified zero-cost/realtime claims. Preserve failed evidence and exact before/after behavior. |

Correctly rounded math generation retains MATH10's representation/rounding/tie
target, accepted-result intervals, synthesis/evaluation/backend assumptions and
independent checker evidence with its math owner. Approximation or throughput
alone cannot silently weaken that contract. PERF9 latency obligations concern
actual declared target paths; removed project work accounting does not remove
those semantics or establish zero-cost/performance support.

## Starting work and completion

Verified MIR fixtures and declared proof/lowering interfaces permit local pass
work before every producer or backend release. Composition acceptance requires
actual current MIR/verifier and selected Lower consumption; target-specific
proof assumptions need the real selected target. Completion needs all declared
required families and applicable positive/negative preservation, invalidation,
determinism and performance evidence. Preserve current writers and restrictions;
independent acceptance, integration and release remain separate.

## Historical task-authority-transfer specification

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
