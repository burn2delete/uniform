# PR623 current-main List admission DATA port

Owner: ACC-01. This is a bounded test-only successor for the useful PR623
normalized DATA controls. It is not acceptance of PR623, its old base stack, or
the whole accounting-removal workstream.

## Frozen identities

The original PR623 head is `b7bb26497b4cec472265ccd414d364350a105cb1`, tree
`7b506c824110d5f88fc228bdf9b4c5999b35b3a7`; its authored source candidate is
`c5e9fccdad293b34c290859cc3e4716870241dd6` with the same tree. The original
base is `e047157d711652a5624012f0da7d6911ce086718`, tree
`2b2168fe83fc08a7b4a882d75650c5b6e6eac31d`. The historical PR623 source,
census, and review-consumption receipts remain unchanged and retain their
original nonclaims.

This successor is based on frozen current main `7201ddcc82ab3bf778f65f9f1f6e6ac821632128`,
tree `b1703faa759a7bf0daa79b03258737d1dab80012`. The source-only port commit
is `4d3a888fd3522664e9c017d7994c955d38129c33`, tree
`a81f4270ab09bb4561514803a4870ec4d8ee43b9`, with exact parent frozen main.
The final receipt commit is the child of that source-only commit; its exact
tuple is reported separately after the receipt is committed. The checkout is
`/Users/matt/.codex/worktrees/review-pr-659-recheck/uniform` on
`codex/review-623-current`.

The later clean source composition checkpoint is
`c013fd4db9ba9889e55e21503b0811ec3a289ee4`, tree
`a85544f0bf2e91ba97aef8d21a97aaf4aeb9f4fd`, over main
`5fa103d2b384d36b3fc67a0d502f2212d0a225a8`. The test and all three
production inputs are unchanged by the intervening schema and test ports.
Original PR623 history is retained without importing its old tree. The
current receipt corrects a missing digit in its development C7 hash; no
compiler authority pin or source is changed.

## Current-source scope

Current main already contains the PR623 production width-removal behavior:

- C7 list construction derives and steps without the former width/accounting
  allowance.
- The C8 Effects owner and C14 lowering owner retain their current validation,
  source-shape, type, and edge contracts.
- Current arity diagnostics and later native/exact validation guards are not
  changed by this port.

The only new path is
`bootstrap/clojure/test/gravity/self_hosting/list_construction_admission_current_test.clj`.
It adds three isolated current-owner matrices over widths `0`, `1023`, `1024`,
`1025`, and `2049`:

1. C7 `sh08-lc-derive` and `sh08-lc-step` over normalized DATA, including
   late unknown-type refusal, stale source-parent refusal, and stale edge
   refusal. The direct C7 source helper is also checked for the malformed edge.
2. C8 `body-lc-source` over the same DATA, including late unknown-type,
   malformed call/edge shape, and malformed validation refusal.
3. C14 `p15-s23-lc-source` over the same DATA and the same late type, call,
   edge, and validation refusals.

The matrices assert exact List descriptor shape and width, preserve the
current no-accounting-field contract, and compare C8/C14 output with the
current C7 source owner. They do not add production code, restore allowance
arguments, or alter current arity behavior.

## Checks

The focused check passed on the source-only port commit:

```text
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.list-construction-admission-current-test --fail-fast
3 tests / 133 assertions / 0 failures / 0 errors
```

The remaining structural checks run after the receipt is added:

- language-boundary test;
- documentation validator;
- full-language roadmap validator;
- pinned changed-file contextual lint for the new Clojure test;
- `git diff --check` and clean worktree bookends.

This is ordinary Clojure/JVM normalized-DATA evidence. No native-C compiler,
authenticated source admission, target-runtime result acceptance, migration
fingerprint refresh, restricted operation, GitHub write, or main integration
is claimed. The historical PR623 CI/source receipts remain historical evidence
and are not silently promoted by this port.
