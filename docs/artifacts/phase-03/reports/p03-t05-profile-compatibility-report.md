# P03-T05 Profile Compatibility Report

Date: 2026-06-24
Task: `P03-T05`
Status: complete for the Clojure stage0 profile-compatibility boundary

## Capability

`clojure -M:gravity profile-compatibility` emits
`:gravity/stage0-profile-compatibility-artifact`.

The artifact carries the P1 manifest forward and adds the P13 compatibility
matrix, cross-profile dependency graph, facade manifest, artifact boundary
manifest, evidence records, conformance results, and capability-based proof.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity`

This fixture covers legal direct imports, a `:kernel` to `:native`
facade-required edge with no-GC/no-hidden-allocation/no-throw evidence, an
artifact-only HDL edge, and a standard-library facade record.

## Rejected Diagnostics

The automated suite verifies all P13 diagnostics for illegal direct imports,
invalid facades, artifact-only source imports, missing evidence, unsupported
runtime assumptions, incompatible memory, illegal effects, illegal
capabilities, generated illegal edges, and matrix contradictions.

## Validation

```text
clojure -M:test
Ran 35 tests containing 1848 assertions.
0 failures, 0 errors.
```

Proof record:
`docs/artifacts/phase-03/profile-compatibility/stage0-p03-t05-profile-compatibility-proof.edn`.

## Limits

This completes profile compatibility validation only. The final all-profile
compliance fixture suite, backend execution, runtime services, performance,
package publication, and self-hosting remain open.
