# PR650 current-main port and path accounting

PR650's authored head is
`63f461c851032e3d6bf3816bca34917f28530008`, tree
`7b3e919f66b7236d49c880eb926379c4838ad9ef`, on base
`6aa6f827b7c42f4fd896b9329fc9c420d123f2a4`, tree
`9cd86e0c96fedc2327378908e07ac5eeb1d5e027`.
The original delta has 57 paths: 15 implementation/test paths and 42
source/evidence/history paths. The original production edits are not replayed;
the current main owners already contain the accounting removal and the
Map/parameter APIs. This port retains only the missing explicit owner-route
controls and their malformed-input check.

## Current composition

The candidate was normally composed with current main `5fa103d2b384d36b3fc67a0d502f2212d0a225a8`,
tree `56b99d048302548a6c73b5385798e62301fdc6fc`, before the port repair.
The clean source checkpoint before this report is commit
`99200a49ba8fe439dfc1c864c709c9a553bd7a98`, tree
`7ee9a7772f0f7ae1c68b171490b28496c397adc0`. It contains the preserved
39-line reader-host parity preflight from the earlier current-source port and
the following PR650-derived route controls:

- `current-gravity-mixed-map-list-parameter-development-kernel` now selects
  the C7 and P15-S23 Map compatibility owners for the Map slot and the List
  owner for every other slot, including the binding-tamper rejection path.
- `current-gravity-map-parameter-call-lowering-development-kernel` now uses
  the C7 Map owner for the first parameter and for the first parameter of the
  two-argument lowering case; the second String parameter remains on the
  ordinary path and all existing lowering/refusal checks remain intact.
- `sh08-bare-map-parameter-development-kernel` directly exercises
  `sh08-mp-compatibility`, rather than only reaching it through the List
  compatibility wrapper.
- `body-mixed-map-list-parameter-development-kernel` directly checks the
  SH09 Map owner for a valid Map and for a malformed type-binding, while its
  existing row/source/refinement tamper controls remain unchanged.

The focused run was:

```text
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --namespace gravity.self-hosting.sh08-function-call-type-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-mixed-map-list-parameter-development-kernel --exact current-gravity-map-parameter-call-lowering-development-kernel --exact sh08-bare-map-parameter-development-kernel --exact body-mixed-map-list-parameter-development-kernel --fail-fast
Ran 4 tests containing 278 assertions.
0 failures, 0 errors. Exit 0.
```

Log: `/tmp/uniform-pr650-99200-map-routes.log`.

The language-boundary gate passed (6 tests, 11 assertions). The document and
full-language roadmap validators passed. The pinned kondo invocation found no
new warning on a changed line, but returned the repository's existing warning
set (189 unused-binding/private-var warnings), so lint is not claimed as a
warning-free gate. `git diff --check` passed.

## Complete authored-path accounting

The three route-control test files above are the only current port. The three
PMK carrier source/regression tests remain historical/static source checks;
their old source equations or proof-call shapes are not current authority.
The six private-map model files contain 22 inert DATA test definitions and are
not executable semantic coverage. The three Gravity production files are
already represented by current main and are deliberately unchanged here.
All 42 paths below are preserved as historical source/evidence/checker or
receipt material; none is treated as current semantic authority.

The current port does not restore a project budget, source pin, production
wrapper, native-C route, genuine compiler/bootstrap execution, or release
claim. The reader-host parity preflight is retained as a separate inherited
test artifact and is not counted as one of PR650's 57 authored paths.

Independent review and integration preflight remain pending.
