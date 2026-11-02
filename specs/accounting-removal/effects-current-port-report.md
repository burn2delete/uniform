# PR533 Current-Main Selective Port

## Purpose

This record binds the useful PR533 Effects controls to current main without
replaying its obsolete production tree. It separates semantic test evidence,
source/result provenance, and SH-01 process custody. It grants no semantic,
migration, release, self-hosting, or seed-retirement authority.

## Source identities

- Current-main base: `bfc99d626ca31c9094573e03da751ee34da5084f`
- Original PR533 head preserved in ancestry: `770b24cec5f5afe21a3761e2ceeb8fe5090ab496`
- Original PR533 tree: `b514bfecb860fcd7031564d69cbe3c7b4bc809f5`
- Selective-port ancestry merge: `8926a1ac4b3eedf6ac660ad1f78e5cac0663da2d`
- Production quota removal already present through #725:
  `7019c174703d534e8da0341587a172a9214b9f20`

The ancestry merge uses the current-main tree and records the original PR as a
parent only. The PR's old `effects.gravity` production tree is not replayed.

## Requirements and dependencies

- Effects semantics remain owned by
  `bootstrap/gravity/src/gravity/effects.gravity`.
- C8 public forwarding remains owned by
  `bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity`.
- Development receipts come from the existing
  `gravity/development-test-result-v1` runner sink.
- Source and Git identity checks remain read-only. Existing SH-01 custody and
  source owners remain responsible for process admission and cleanup.
- `.gravity` and `.qst` PMK fixtures are paired for reader and identity
  coverage.

## Current semantic port

`sh09_body_effect_test.clj` adds three controls:

1. `body-private-map-keys-authenticated-builder-composition` checks the PMK
   source pair through C5/C6/C7, the Effects builder/verifier, and public C8
   build/verify forwarding. It checks the allocation rule, identity-input
   parity, and wrong-binding `C8-VERIFY` refusal at both boundaries.
2. `body-module-vectors-have-no-cumulative-quota` accepts vectors beyond the
   retired 65,536-node and 256-function ceilings, including the combined case.
3. `body-module-vectors-check-caller-and-every-producer` checks a long valid
   producer list and rejects malformed caller and late-producer vectors.

These are development controls. They do not establish full Reader/C8
authentication, native execution, release, or self-hosting authority.

## Source/result adapter

`tools/effects_acceptance.clj` is a reduced, current-main adapter. It binds:

- exact source/resource/dependency identities and clean dependency roots;
- the three current selectors, with an explicit PMK singleton option;
- current C8 forwarding forms and the unmetered producer-analysis route;
- the current runner schema, qualified test selection, counts, outcome, exit,
  and before/after source identity.

The adapter is non-launching and returns `:semantic-acceptance false`. It does
not require or manufacture a broker lease, process port, native operation, or
authoritative receipt. Its tests use synthetic source and runner maps only.

## Per-path reconciliation

The original historical acceptance record
`specs/accounting-removal/effects-aggregate-acceptance.md` is preserved
unchanged. Its stale pins and `:authority :none` status remain historical and
are not relabeled as current evidence.

The original retained-Git and process-binding files are not copied into the
Effects semantic port. Their useful behavior remains owned as follows:

- `gravity.resolve.selected-source-harness/prepare-inputs!` and
  `recheck-inputs!` already provide read-only source Git identity and drift
  refusal.
- SH-01's `run-git-bytes!` and `*producer-git-lifecycle*` in
  `sh01_development_loop_wiring.clj` already provide the current Git process
  lifecycle hook; SH-01's `start-process`, `retain-owned-process!`,
  `process-result`, and `finalize-owned-process!` remain the custody API.
- The exact PR533 combination of a public retained-Git result wrapper, strict
  complete-UTF-8/truncation receipt, shared outer deadline across Git/child/
  cleanup/publication, and suppressed-cleanup error preservation is not
  exposed by those current public interfaces. That is a concrete SH-01/source
  custody interface delta and remains pending; no duplicate controller or
  PMK-specific exclusive-lease gate is introduced here.

## Validation and evidence limits

The adapter tests are pure Clojure controls. The semantic selectors require a
fresh JVM when run and should be selected exactly. Focused test success is
development feedback only. No native C compiler, restricted process route,
broker admission, or remote operation was executed for this port.

The two vector controls and the retained shape-control selector pass together
with 39 assertions. The PMK composition selector remains held: the current
main pinned C2 reader identity rejects the fixture before C5/C6/C7, and an
attempt to select the supported current Reader route reaches SH-05's pinned
macro-source requirement. Resolving that requires an SH-03/SH-05/SH-07 owner
change; this port does not refresh those source identities or weaken the
authentication boundary. The old 257-entry rejection in the neighboring
import-table test was removed because the current predicate is shape-only; its
type and collection-shape negatives remain.

## Acceptance criteria

- The current-main tree remains otherwise intact.
- The three semantic selectors and adapter tests pass with stable diagnostics.
- The historical acceptance record remains byte-for-byte unchanged.
- The final candidate has an exact base, commit, tree, clean worktree, and
  independent review before any integration decision.
