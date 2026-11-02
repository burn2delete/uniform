# PR533 Current-Main Selective Port

## Purpose

This record binds the useful PR533 Effects controls to current main without
replaying its obsolete production tree. It separates semantic test evidence,
source/result provenance, and SH-01 process custody. It grants no semantic,
migration, release, self-hosting, or seed-retirement authority.

## Source identities

- Current-main base: `bfc99d626ca31c9094573e03da751ee34da5084f`
- Current-main base tree: `f6305181e0df2e841c15c752f3e1ee1260bdee17`
- Original PR533 head preserved in ancestry: `770b24cec5f5afe21a3761e2ceeb8fe5090ab496`
- Original PR533 tree: `b514bfecb860fcd7031564d69cbe3c7b4bc809f5`
- Selective-port ancestry merge: `8926a1ac4b3eedf6ac660ad1f78e5cac0663da2d`
- Production quota removal already present through #725:
  `7019c174703d534e8da0341587a172a9214b9f20`
- Final composition current-main base: `5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e`
- Final composition merge: `e9342cc705ae832c5551867f7b000c6f0f86bddf`
- Final composition tree before this report-only update:
  `0c13cf9c43c52664bcc8c171dddf82e9c5cac378`

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
- Current SH-01 lifecycle hooks preserve the raw Process/token, source-drift
  recheck, interruption and suppressed-cleanup behavior. The exact PR533
  public retained-Git result wrapper is not copied. The selected observer now
  injects the existing runner controller down through the wiring cycle: the
  runner performs bounded strict-UTF-8 capture and owned cleanup, while wiring
  validates the complete/non-truncated receipt and returns only canonical
  bytes to the Git identity callers. No duplicate controller or PMK-specific
  exclusive-lease gate is introduced here.

## Validation and evidence limits

The adapter tests are pure Clojure controls. The semantic selectors require a
fresh JVM when run and should be selected exactly. Focused test success is
development feedback only. No native C compiler, restricted process route,
broker admission, or remote operation was executed for this port.

The two vector controls and the retained shape-control selector pass together
with 39 assertions. The SH07 owner repair is isolated in commit
`80501c6d77ca67b617546f3bb5fa30925704f44f` (tree
`049e566342b5883acfea7b37ffa9370fec2792ca`). Its construction guard derives
the Macro mode from the authenticated SH06/C4 product, compares any caller
assertion to that mode, uses the matching fresh SH05 binding/source, and keeps
the historical pinned default. The full invocation carries
`:macro-source-mode` through SH07/SH06; the exact SH03/C3 Reader map strips it
before Reader authentication. Fresh controls cover current-positive,
pinned-default, current-to-pinned downgrade, source/mode tampering, and fresh
Macro-binding provenance (2 tests, 9 assertions passed).

The current SH-01 custody adapter's inert controls pass in
`producer-git-observation-test`: one positive case verifies controller launch,
retention, stdin close, remaining-deadline handoff, strict complete capture,
and published receipt metadata; one negative case rejects a truncated receipt
after cleanup. The runner's existing process-result tests continue to cover
owned-handle finalization and interruption behavior. These tests use injected
process/controller ports and are not evidence of actual broker admission.

The PMK composition selector was retried only after that owner repair. Its
fresh JVM remained CPU-bound in the C5/C6 construction for approximately 40
minutes with multi-gigabyte garbage-collection cycles and was stopped cleanly
without a result. This is an uncompleted host-bound run, not PMK acceptance;
no synthetic product, authentication bypass, source-owner refresh, C6/C7
success, or public C8 execution is claimed. The former pinned C4 rejection is
therefore no longer the current code-level blocker, but the downstream PMK
composition remains unevidenced pending a bounded permitted run.

The retained-Git/source-custody behavior is now connected in the actual
current SH01 owner: `gravity.build.producer-git-observation` and its
`*producer-git-lifecycle*` bookends use the canonical retained-process APIs,
preserve raw Process/token identity, recheck source bindings before and after
the selected C2 observation, and retain only non-authoritative observation
facts. The wiring binds one monotonic operation deadline for the child,
selected Git bookends, controller wait/capture/cleanup, and final publication;
the remaining timeout is handed to the existing runner rather than resetting
at Git start. The published observation contains capture status, observed and
retained byte counts, limits, truncation and forced-close flags, while
the wiring's strict decode records `:utf8-valid? true`; `capture-v1-ready?`
remains false. These are process-custody facts, not a semantic or
authoritative receipt.

Those paths are present in current main through commits
`e047157d7`/`e02d28e15`/`7a74f6e38` and are preserved by this candidate; the
old PR533 `effects_retained_git` wrapper and PMK lease gate are not replayed.
No project-accounting or PMK-exclusive lease is reintroduced. The process
controller's cleanup and capture are still host-bounded and descendant
coverage remains `:unknown`; no global process containment or genuine PMK
admission is claimed.

The old 257-entry rejection in the neighboring import-table test was removed
because the current predicate is shape-only; its type and collection-shape
negatives remain.

## Final composition check

The selective port was normally merged with current `origin/main` at
`5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e` in a clean reusable checkout.
The merge had no conflicts and did not replay the original PR tree. Focused
custody, runner UTF-8/capture, Effects adapter, vector, boundary, document,
and roadmap checks were rerun on the composed tree. The broader SH-01 wiring
suite still reports its existing standalone ownership/dependency-record
failure; that remains a nonpass and is not relabeled as PR533 evidence.
Current-main changes outside the twelve listed port paths are inherited from
the composition parent and are not attributed to PR533.

## Acceptance criteria

- The current-main tree remains otherwise intact.
- The three semantic selectors and adapter tests pass with stable diagnostics.
- The selected observer controller adapter and complete-receipt negative
  control pass as inert custody tests; they do not execute a native compiler or
  restricted process route.
- The historical acceptance record remains byte-for-byte unchanged.
- The final candidate has an exact base, commit, tree, clean worktree, and
  independent review before any integration decision.
