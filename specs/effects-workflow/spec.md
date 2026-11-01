# Effects implementation specification

Implementation scope added under the 2026-11-01 request. Existing Effects
maintainers retain ownership. The historical task-transfer specification below
does not establish effect inference, handler behavior or checked-Core acceptance.

## Behavior and interfaces

Implement [the Effects outcome](../../docs/component-roadmaps/effects.md#release-outcome)
under [L6](../../docs/phase-01-core-language/016-l6-effect-system-specification.md)
and [C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md).
Actual typed Core, resolved calls/dispatch/closures, registered effect/handler
contracts, declarations, profiles, grants/providers and replay policy produce
direct/latent/transitive/build/residual effect graphs, escaping/handled tables,
ordering constraints, capability-use evidence, replay obligations and diagnostics.

Types establishes call/handler request-response facts; Effects propagates and
checks action facts; Auth checks actual authority; Legality enforces profile,
continuation/resource/safety obligations. Runtime and domain owners implement
the selected handler's actual scheduler/generator/error/workflow/tool/test
interpretation. Neither a handled label nor a declared effect mints capability
authority, and a capability does not establish inferred effects.

## Existing implementation routes and acceptance

The [sole marked ledger](tasks.md#implementation-tasks) retains
`effects/current-evidence-and-remaining-work/` and these existing task suffixes.
The preserved requirement-reconciliation lineage already proposes fuller C8
criteria, including handled-effect facts, in these same records; those pending
criteria are not silently adopted or independently accepted by this entry.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `effects1-infer-direct-effects` | Classify real operations, including allocation, IO/FFI, throws, mutation, concurrency, compile-time, workflow and model/tool/memory actions, with original source references. | Reject unknown/unregistered labels and hidden generated/build/runtime actions. Unsafe islands retain their concrete effects and separate safety classification, not a generic authority-erasing unsafe label. |
| `effects2-propagate-latent-effects` | Derive closure/call/dispatch/recursive summaries to a valid fixed point, preserving declared/inferred/thrown distinctions, conservative branches and handled versus escaping effects. | Reject declaration-only summaries, missing call/type facts, underreported reachability, hidden handler residuals and false label erasure. Retain actual graph derivations and handler identity/coverage/context. |
| `effects3-check-legality-and-ordering` | Check the applicable source/profile/package/build/deployment/provider/safety intersection and emit actual capability-use and sequence/resource/volatile/MMIO/atomic/replay ordering requirements. Check typed handler coverage, residual effects, request/response/result, profile, capabilities and continuation obligations with their owners. | Reject missing grants, forbidden labels/resume modes, authority-widening handlers, unexpected live effects in test interpreters, unsafe continuation capture/resume/clone/serialization and illegal ordering before MIR. Effect and capability sets are never compared as interchangeable sets. |
| `effects4-record-replay-and-invalidation` | Separate build/runtime domains; emit record-versus-replay obligations with typed stable event identities and invalidate changed call/type/declaration/provider/policy/replay inputs. | Reject missing/mismatched/reordered/extra records, replayed live external effects, unreplayable hermetic behavior and stale summaries. Preserve secret redaction and actual source/input context. |
| `effects5-integrate-checked-core-consumers` | Feed actual propagated facts into profile/Auth/safety and MIR, then preserve effects through selected optimization/lowering/runtime consumers. Compose async, generator, resumable-error, workflow replay, tool and test interpreter classes with their actual owning implementations. | Execute generated/higher-order/recursive and handler positives and negatives with real producer/consumer evidence. Static summaries alone do not prove suspension/resume/cancel/close/cleanup behavior, live authority enforcement or no-repeat replay. |

All six required L6 handler classes remain in scope, including affine defaults,
explicit multi-shot proof/profile requirements, transfers across suspension,
completion/cancellation/finalization and deterministic replay restrictions.
Actual runtime interpretation is a cross-component composition obligation;
neither Effects nor a test fake can close it alone. The existing source owner
retains finer conjunct/criterion allocation and current-evidence judgments.

## Starting work and completion

Declared typed-Core, grant, handler and runtime-service interfaces allow local
implementation before every producer/runtime release is complete. Actual
composition requires the selected real checker/provider/interpreter, and
pre-MIR legality cannot be deferred to runtime. The component outcome and each
claimed handler class require actual accepted/rejected execution artifacts,
source/fixture/runtime identities and applicable independent evidence. Preserve
all current writer reservations and restricted operations; no source-only
planning change authorizes a held replay/provider run.

## Historical task-authority-transfer specification

# Feature specification: Effects implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler effects maintainer. Existing implementation reservations are unchanged.
Input revision: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Effects implementation outcomes: direct
and latent inference, legality/ordering, replay/invalidation and checked-core
handoffs. Only the editable task location changes; no implementation outcome
closes. Effect inference never grants capability authority.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/016-l6-effect-system-specification.md) remains normative.
- [Retained contract references](plan.md#contract-alignment) preserve additional navigation and nonexclusive ownership.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/effects.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Effects semantic task ID from the sole task ledger. Its
title, evidence/status and concrete exit evidence remain in that editable
record, and the roadmap provides navigation to it. No second editable
implementation ledger is introduced.
The `effects/1` through `effects/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.
The existing local direct/declared projections do not establish complete
interprocedural fixed-point inference or close the component outcome.

## Scope and compatibility

Own the four Effects workflow artifacts and Effects roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation
and its tests, normative contracts and dashboard behavior are unchanged. No external Specify CLI, automatic
discovery, dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler effects maintainer retains implementation and semantic-acceptance
ownership. Independent review and integration remain separate from local
validation. Exact observations and limits belong in [evidence](evidence.md).
Contract routing is navigation, not exclusive ownership.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Effects must establish actual direct, latent and transitive facts, preserve
ordering/replay obligations and reject undeclared or profile-forbidden behavior.
Capability grants remain separate authority. This transfer neither changes those
requirements nor claims implementation of them.

## Implementation naming review

`effects-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
