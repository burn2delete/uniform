# P03-T02 Core, Meta, Hosted, And Native Profile Set Report

Date: 2026-06-24
Task: `P03-T02`
Status: complete for the Clojure stage0 profile-set boundary

## Capability

`clojure -M:gravity profile-set` emits
`:gravity/stage0-profile-set-artifact` for `:core`, `:meta`, `:hosted`, and
`:native` fixtures.

The artifact carries the P1 manifest forward and adds an effect/capability
matrix, profile-specific report, and P2-P5 conformance fixture.

## Accepted Fixtures

- `bootstrap/clojure/fixtures/accepted/profile-set-core.gravity`
- `bootstrap/clojure/fixtures/accepted/profile-set-meta.gravity`
- `bootstrap/clojure/fixtures/accepted/profile-set-hosted.gravity`
- `bootstrap/clojure/fixtures/accepted/profile-set-native.gravity`

## Rejected Diagnostics

The automated suite verifies all diagnostics listed by `P2`, `P3`, `P4`, and
`P5`, for 38 profile-specific rejected fixtures.

## Validation

```text
clojure -M:test
Ran 35 tests containing 1848 assertions.
0 failures, 0 errors.
```

Proof record:
`docs/artifacts/phase-03/profile-set/stage0-p03-t02-core-meta-hosted-native-proof.edn`.

## Limits

This completes the first profile set only. Later Phase 03 proof records
complete constrained profiles, distributed and AI profiles, cross-profile
compatibility, and full profile compliance.
