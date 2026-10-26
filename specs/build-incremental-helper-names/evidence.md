# Incremental build helper evidence

Base: `8b0b2ed4fb20511b46478189a8c8f554d47d5983`, tree
`b56e5a0d2a2d88f889850f07d6bd4460c371a1cd`. Cloud observations: 2026-10-26.

The pristine `clojure -J-Xmx512m -M:leaf-test --namespace gravity.c16-incremental-test`
passed 6 tests / 80 assertions, with the real 48-entry catalog validated.
The added accepted/rejected validation and proof-projection regressions passed
before the rename: 7 tests / 98 assertions. This is focused leaf execution, not
the full catalog or compiler. The existing proof projection can report a
`:complete` status alongside false observations; the regression preserves that
compatibility behavior and does not claim proof authority.

## Candidate validation

Published implementation: `431eaaad9cd797b9eafc99605c6a219efc3bcf27`, tree
`4f7823543eff45f872dfbbd9be54be1ef43b9e9a`, in
[PR399](https://github.com/burn2delete/uniform/pull/399). The published tree
matches the locally executed implementation.

| Command/check | Result |
| --- | --- |
| `clojure -J-Xmx512m -M:leaf-test --namespace gravity.c16-incremental-test` | Exit 0; 7 tests, 98 assertions |
| `clojure -J-Xmx512m -M:leaf-test --namespace gravity.c15-c16-pass-cache-test` | Exit 0; 4 tests, 49 assertions |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Exit 0; 6 tests, 11 assertions |
| `clojure -J-Xmx256m -M tools/validate_clojure_loc.clj` | Exit 0; 2,027 source files, no exceptions |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0; 240 documents, 19 phase indexes |
| Focused ASCII/local-link check | 5 files, 20 local links/anchors resolve |
| `git diff --check` | Passed |

Both leaf invocations validated the real unchanged 48-entry catalog before
executing the selected namespace. No full-catalog or full-compiler execution is
claimed. All four moved helper bodies are byte-identical after their first
namespace line; the facade differs only in four requires. The aliases and calls
are unchanged. Exact old-name search found no remaining live references. A
fresh facade require confirmed all four new namespaces/resources exist and all
four legacy private namespaces/resources are absent.

## Actual planner comparison

The actual planner's `classify-path` and `build-plan` ran before and after the
rename for each old/new helper path, the old group, new group, rename group,
facade, test and complete transaction, with dependant expansion false and true.
All 28 structured records match byte-for-byte:

- Four facade/test requests successfully select `gravity.c16-incremental-test`
  in `:component-test-namespaces`, alongside 291 self-hosting `:namespaces`.
  These are distinct routes; the 291 namespaces were planned, not executed.
- Twenty-four nested-path/group/transaction requests fail closed with
  `SH01-IMPACT-UNOWNED` on both trees. No nested-helper ownership or unknown-path
  acceptance was broadened. Catalog validation did not fail on this base.

## Review and remaining boundary

Independent review accepted exact implementation `431eaaad`/tree `4f782354`
with no blocking findings. The reviewer independently checked byte conservation,
all four caller changes, live reference closure and equality of the 28 planner
records, and inspected the retained passing execution logs. This is not a claim
of an independent compiler-test replay. The complete transaction remains
unowned in the planner; matching refusals are not automatic routing coverage.

The implementation composes cleanly with freshly fetched main `8b0b2ed4`,
yielding the same tree `4f782354`. Documentation-only evidence updates do not
alter the reviewed source/tests. Coordinator final-head/current-main preflight
and merge remain pending. GitHub reported no workflow runs or status checks for
the implementation head; no CI success is inferred. Historical heavy compiler
failures were not replayed or treated as current results.

The private legacy directory is gone. The public `gravity.c16-incremental`
facade, public var names, test selector and serialized schema/diagnostic IDs
remain intentional compatibility boundaries, and historical pins are unchanged.
This finite group completes the selected internal naming migration; it does
not start an automatic public API rename or add cache/proof/release authority.
