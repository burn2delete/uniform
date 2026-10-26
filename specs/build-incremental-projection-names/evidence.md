# Incremental projection rename evidence

Base: `7d008ac94ddc7d0bad78354317035e87ff4889cb`, tree
`e18546b094b24790b1ff343a59019e12e8b685a5`. Cloud observations: 2026-10-26.

## Baseline and verification limits

`clojure -J-Xmx512m -M:dev-test --namespace gravity.c16-incremental-test`
passed on the pristine base: 5 tests, 75 assertions, exit 0.

The aggregate `-M:leaf-test` command exits 2 before tests with
`S0LEAF-CATALOG-SURFACE`: its top-level discovery expects 50 tests but its
reviewed catalog contains 48. The extra files are `native_syntax_invocation_test.clj`
and `reader_invocation_test.clj`; both directly require `gravity.bootstrap`
and are therefore not bootstrap-free leaves. Their proper routing/classification
is a separate repair, not an exclusion or runner-policy change in this rename.
The existing documented `-M:dev-test` explicit-resource lane remains usable.

The heavy bootstrap compatibility failure reported by
[the prior rename](../build-incremental-operation-names/evidence.md#pre-existing-composition-failure)
is historical evidence, not a fresh result on this base. It is not rerun here;
no bootstrap compatibility pass, full compiler replay or authoritative migration
is claimed. Schema fields and source pins remain unchanged.

## Candidate checks

Focused tests and actual before/after planner checks are underway in this early
draft. Independent review, final-base verification and merge remain pending.
