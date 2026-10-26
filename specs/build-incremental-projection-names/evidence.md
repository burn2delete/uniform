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

Published implementation: `003dbbe6a4a8aba1e97e0c6b952877bdb2be0675`, tree
`ea2d10f85d38e17b6142ea57c83267fce6dc0f35`, in
[PR397](https://github.com/burn2delete/uniform/pull/397). Published source/test
bytes match the locally executed implementation; later work-artifact updates
change documentation only.

| Command/check | Actual result |
| --- | --- |
| `clojure -J-Xmx512m -M:dev-test --namespace gravity.c16-incremental-test` | Exit 0; 6 tests, 80 assertions, no failures/errors |
| `clojure -J-Xmx512m -M:dev-test --namespace gravity.c15-c16-pass-cache-test` | Exit 0; 4 tests, 49 assertions, no failures/errors |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Exit 0; 6 tests, 11 assertions, no failures/errors |
| `clojure -J-Xmx256m -M tools/validate_clojure_loc.clj` | Exit 0; 2,026 source files, no baselined exceptions |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0; 240 documents, 19 phase indexes |
| Focused Markdown scan | 5 ASCII files, 22 local links/anchors resolve |
| `git diff --check` | Passed |

A fresh Clojure process required the unchanged public facade and confirmed the
new projection namespace/resource exist while the old namespace/resource are
absent. A byte comparison after the first namespace line confirms the entire
helper implementation is unchanged. The single require, all three alias-based
calls, updated document target, and complete new-name diff were reviewed.
Schema/diagnostic/identity literals are retained compatibility data, not new
planning-ID implementation names.

## Actual planner and aggregate limitations

The real `gravity.self-hosting.sh01-impact-test-planner/build-plan` ran on both
pristine base and candidate for six selections: old helper, new helper, rename
pair, facade, test, and the complete source/test transaction. Each selection ran
with `:expand-dependants?` false and true. All 12 structured records match
byte-for-byte; no successful automatic selection is implied:

- Eight requests fail closed with `SH01-IMPACT-UNOWNED`: the nested helper paths,
  their pair and the source/test transaction are unowned in the existing planner.
- Four facade/test requests fail with `SH01-IMPACT-CATALOG` because
  `gravity.self-hosting.mir-scalar-admission-test` and
  `gravity.self-hosting.mir-source-snapshot-test` lack the existing planner's
  non-slice classification. These baseline routing defects are not repaired here.

The aggregate leaf catalog error data also match exactly before and after the
rename. Explicit focused development tests above passed; neither aggregate
catalog validation nor full compiler/ownership validation is claimed passing.
No runner, ownership contract, unknown-path policy or historical receipt changed.

## Independent review and integration

Independent peer review of exact implementation `003dbbe6`/tree `ea2d10f8`
found no blocking findings. The reviewer independently checked byte conservation,
the caller graph, document target, regression coverage, retained execution logs
and all 12 planner records. This reports inspection of execution evidence, not
an independent compiler-test replay.

Both implementation and review independently observed a clean merge-tree result
against main `1927aaee826766ea3bf61af609b2333ccd3b5c43`: prospective tree
`d6c2b4f0d1f65ce5b3736931fa474ec42021b525`. This is compatibility observation,
not integration. GitHub reported no workflow runs/status checks for the
implementation head. Coordinator final-head/current-main verification and merge
remain pending. No proof, real-cache, release or self-hosting authority is added.
