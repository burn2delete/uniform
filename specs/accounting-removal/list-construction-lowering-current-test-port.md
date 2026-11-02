# PR636 current List-construction lowering test port

Owner: ACC-01. This is a bounded current-source test port, not acceptance of
PR636, its parent stack, or the whole accounting-removal workstream.

## Frozen identities

The original PR636 candidate is commit
`db8aaac9851ba287cf189c186703a4f85730903e`, tree
`b218b0cb3d6cfb90d1acabc155d7af123bfc8e12`, with exact parent/base
`c5e9fccdad293b34c290859cc3e4716870241dd6`, tree
`7b506c824110d5f88fc228bdf9b4c5999b35b3a7`. Its historical source and
control census remain preserved in the existing PR636 records and are not
rewritten by this port.

The port starts at current main commit
`bfc99d626ca31c9094573e03da751ee34da5084f`, tree
`f6305181e0df2e841c15c752f3e1ee1260bdee17`. The scoped port commit is
`94096b849b5c5ce5bb1aabf7bc5943def037d46a`, tree
`3c3f9b9bc9000972d5bc2695c931e64860f5f4c3`, with parents current main
`bfc99d626ca31c9094573e03da751ee34da5084f` and the exact original PR636
head `db8aaac9851ba287cf189c186703a4f85730903e`. The checkout is
`/Users/matt/.codex/worktrees/review-pr-502/uniform` on
`codex/review-636-current`; the pre-existing `codex/review-777-integration`
branch remains untouched.

## Current-source scope

The first-parent tree delta is one file:
`bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj`.
No production source, historical census, historical report, or current-main
CI/review record was replayed or modified. Current main already carries the
historical PR636 records from the #534 integration history.

The current test retains the useful PR636 controls:

- a 1,025-member List type preserves the descriptor extent and lowered List
  shape;
- the lowered value retains identity of the original 1,025-member input List;
- C7 derivation, current List validation, and current lowering reject seven
  malformed cases: unknown element type at index 0, unknown element type at
  index 1024, late `Never`, wrong shape, an extra field, a non-vector element
  carrier, and an odd Map member carrier;
- a wide homogeneous Set element type and an even 1,026-member Map type remain
  accepted by the current C7 and lowering validation entry points.

The stale historical `p15-s23-lc-type-work` calls and `65536` compile-budget
arguments are not present. All ported calls use current
`p15-s23-lc-validate-types` and the current unmetered
`p15-s23-typed-compile-node` interface. Existing runtime owner-wrapper guards
and the already-landed #513/#370 work are outside the changed path and remain
unchanged.

## Checks

The source-focused checks and first structural pass below ran against the
immutable port commit before this receipt was written. The documentation and
roadmap validators were also rerun after this report was added and still
passed:

| Check | Result |
| --- | --- |
| `clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-list-construction-lowering-development-kernel --fail-fast` | 1 test / 154 assertions, 0 failures, 0 errors |
| `clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | 6 tests / 11 assertions, 0 failures, 0 errors |
| `clojure -M tools/validate_gravity_docs.clj` | 240 docs / 19 phase indexes, passed |
| `clojure -M tools/validate_full_language_roadmap.clj` | passed; dated coverage snapshot remains non-authoritative |
| `git diff --check bfc99d626ca31c9094573e03da751ee34da5084f..94096b849b5c5ce5bb1aabf7bc5943def037d46a` | passed |
| `clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref origin/main` | read-only inspect passed; exact base/candidate identities and clean worktree observed; integration mode not run |

The focused check is ordinary hosted Clojure/JVM stage2 development evidence.
No native-C compiler, unsupported direct runtime-shape selector, C12
operation, source-authentication operation, migration fingerprint refresh,
or restricted/denied operation was executed. These checks do not establish
authenticated Reader/Core input, genuine target-runtime acceptance, whole
ACC-01 or PR502 closure, release, self-hosting, or seed retirement.

Independent acceptance and coordinator-owned current-main integration remain
separate gates. The historical PR636 census and CI records retain their
original source identities and nonclaims; this port does not promote them.
