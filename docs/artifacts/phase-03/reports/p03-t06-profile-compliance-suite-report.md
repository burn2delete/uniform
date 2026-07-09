# P03-T06 Profile Compliance Suite Report

Date: 2026-06-24
Task: `P03-T06`
Status: complete for the Clojure stage0 profile-compliance boundary

## Capability

`clojure -M:gravity profile-compliance` emits
`:gravity/stage0-profile-compliance-suite-artifact`.

The artifact compiles accepted profile namespaces through the owning P1-P13
artifact commands and compiles rejected profile namespaces through the same
profile-specific validators. It records accepted capability evidence, rejected
diagnostics, document coverage, profile coverage, and pre-backend rejection
status.

## Accepted Fixture Suite

- `bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity`

The suite records 23 accepted fixture artifacts. They cover all 11 standard
profiles and all 13 Phase 03 profile documents: P1 through P13.

## Rejected Diagnostics

The suite records 133 profile-specific rejected fixtures. All expected P1-P13
diagnostics are covered, with no missing or unexpected diagnostic IDs. Each
rejection is captured before backend lowering.

## Validation

```text
clojure -M:test
Ran 35 tests containing 1848 assertions.
0 failures, 0 errors.
```

```text
clojure -M:gravity profile-compliance bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity
:gravity/stage0-profile-compliance-suite-artifact
```

Proof record:
`docs/artifacts/phase-03/profile-compliance/stage0-p03-t06-profile-compliance-suite-proof.edn`.

## Limits

This completes Phase 03 profile-system compliance only. Backend execution,
runtime services, performance, package publication, and self-hosting remain
future phase work.
