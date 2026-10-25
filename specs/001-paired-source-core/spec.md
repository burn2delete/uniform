# Feature specification: paired current source to verified Core

Status: reference pilot; implementation remains owned by PR356.
Pilot artifact owner: Spec Kit adoption delegate. Integration coordinator: Deus.
Implementation input: `700e2929c6d5aeb394f4fe342108a86dfb54f58e`.
Adoption baseline: `33b91ba6`; this pilot does not alter implementation.
Existing PR: [356](https://github.com/burn2delete/uniform/pull/356), observed open
and draft on 2026-10-25. This is a dated workflow record, not new source authority.

## Purpose and canonical contracts

Model one existing small test increment through spec -> plan -> tasks -> evidence
without duplicating compiler code or claiming the entire compiler is complete.
The bounded outcome is paired public-current-reader -> authenticated C5 ->
Gravity-built Core evidence for selected two-slot and quoted helper occurrences.

Canonical owners are [L2](../../docs/phase-01-core-language/012-l2-core-language-semantics.md),
[C5](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md)
and [C6](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md).
[The SH07 completion plan](../../docs/self-hosting-sh07-completion-plan.md) owns
broader coverage. The [development workflow](../../docs/development-verification-workflow.md)
owns evidence/reuse/authority. None is rewritten here.

## User scenarios and acceptance

### US1 - Preserve paired source construction and Core facts

- R1: `compiler-all-unique.gravity` and `.qst` enter through the existing public
  API with `:reader :development-current`; fresh verification passes for each
  two-slot product. Retain origins, source map, conditional child order, loop
  target identity, two recur operands and left-to-right operand order.
- R2: `compiler-reader-quote.gravity` and `.qst` preserve quoted data as inert,
  with no child evaluation or calls. Equal semantic identity does not erase
  distinct source provenance. Repeating authentic source construction is stable.
- R3: Coherently rehashed Boolean substitution is rejected by semantic replay,
  even when the tampered product declares a current digest.
- R4: Both extensions reject missing `if` branch with `C6-CORE-SHAPE`, recur
  arity mismatch with `C6-VERIFY`/`:recur-arity-mismatch`, and non-tail recur with
  `C6-VERIFY`/`:recur-tail-position-required`, preserving owning stage and source.
- R5: Existing ingress options, UTF-8 policy and retained-target snapshot controls
  remain covered by the original owner's declared validation command.

## Scope and compatibility

Implementation path (owned by PR356):
`bootstrap/clojure/test/gravity/self_hosting/sh07_current_public_closure_integration_test.clj`.
Existing paired fixtures under `bootstrap/clojure/fixtures/self-hosting/sh-07/`
and rejected fixtures under `sh-07/rejected/` and `sh-07-b4/rejected/` are reused.
The source contains three new `paired-` tests; inspect the pinned commit, not a
mutable working tree, when assessing this record.

This increment adds integration tests only. It does not close six-owner coverage,
C02/C04/C07 migration, native execution, final self-hosting or SH07 completion.
No new runner, registry, lane, source quota or project accounting rule is needed.

## Evidence and outcome ownership

PR356's implementation owner owns code and raw validation receipts. The adoption
delegate owns this pilot's artifact/helper verification only. Reviewers own review
dispositions; Deus owns integration and exact final outcome claims. See
[evidence.md](evidence.md) for source observations, reported results and unrun work.

## Unresolved items

Independent PR356 review and integration disposition remain outside this pilot.
The raw historical validation log was not collected or independently rerun here.
Do not check off those outcomes from this documentation exercise.
