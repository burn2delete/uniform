# Accounting removal

Owner: accounting completion. Scope: artificial project accounting across the
compiler, emitter, development runtime and their host producer/consumer paths.
This is the sole detailed accounting-removal backlog; component implementation
tasks remain in their existing Spec Kit ledgers. The shared cleanup index links
here and does not duplicate task status.

## Contract

[Development verification](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
requires removal of logical work units, allowances, reservations, debits, cost
proofs and accounting-only refusal/tests. It includes capacity-oracle maxima
that reject otherwise well-formed inputs. Increasing constants, returning zero
telemetry or adding unbounded sentinels does not satisfy this contract.

Preserve the [C7 type contract](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C8 effect authority](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md),
[MIR invariants](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md),
[optimization safety](../../docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md),
and [runtime semantics](../../docs/phase-08-runtime-architecture/README.md).
D1, D3, D6, D8 and D9 govern interfaces, safety and evidence. Effect/capability
permission sets are real authority, not work allowances.

## Acceptance

1. Inventory every in-scope accounting producer, consumer, schema field,
   refusal, test and current contract requirement, including literal maxima
   that lack accounting names. Record a source-grounded remove/retain/history
   disposition. An unresolved classification is open work.
2. Remove each artificial mechanism coherently across its actual owners.
   Preserve complete traversal, Unicode, native class/metadata/callback rules,
   collision and source validation, refinement forgetting, identity and
   not-applicable behavior, type/effect legality and memory safety.
3. Verify ordinary valid inputs beyond deleted limits and equally late invalid
   inputs. Preserve semantic extents such as checked-member-count and
   checked-element-count with independently effective tamper controls.
4. Exercise genuine affected producer/consumer paths and required runtime
   composition. Direct helpers and normalized owner fixtures establish only
   their named scope; they do not establish source authentication or release.
5. Obtain independent review, integrate accepted changes into current main,
   and record exact commit/tree, tested scope, passed/failed/unrun stages and
   remaining acceptance boundaries. Keep unfinished work draft.

100% means all five conditions hold with no unresolved in-scope removal item.
It does not mean completion of Gravity. Unknown owners, restricted acceptance
paths and incomplete independent review prevent that claim. No percentage is
inferred from search hits, tests, PRs or historical estimates.

## Restrictions

C12 execution is prohibited. The original C7 mode-publication operation remains
unresolved and must not be repeated. Existing writers retain paths until an
explicit handoff. Host brokers, heaps, watchdogs, captures and platform controls
remain intact. Schedules, credentials, sharing and old coordinator state are
outside this change. Historical failed receipts remain historical.
