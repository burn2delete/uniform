# PR502/641 current-owner guard port

This candidate ports the bounded quota-free mixed collection owner source,
producer/fact guards, preserved fixture pair, and PR641 complete-product
comparisons onto exact current main. The post-relocation adoption composes the
port with current main after the Effects source moved to its canonical
`language/compiler/src/gravity/effects.gravity` path; the emitter remains at
`bootstrap/gravity/p15_s23/emitter.gravity`. It is ordinary current-owner
development evidence only. It does not close PR502 or PR598 and does not claim
public C8, L5, source admission, native/runtime, migration, release, or
self-hosting authority.

## Immutable identities

| Item | Commit | Tree |
| --- | --- | --- |
| Current post-relocation integration base | `6f6cc71be14ecb3dffa18a7566f2205d2ceb5b05` | `41687b2b95f67d775684f06eb7cb52e09f51e107` |
| Historical port integration base | `7e59c5cf6437f1c4b769415340c6608c1fc21341` | `c6f034179a37d3518a422035b1a7c0b82db16a8f` |
| Source/test checkpoint | `b21cdceae032d0fb6a6ca2302c35bec7bfa23356` | `6172113850f732faa513ed1d2745cbe2ceb33acd` |
| Normal-merge source checkpoint | `16131511017a375bc483fafbb2f4e2479c565ee9` | `3000e1742b37973398be75655b6e054ff61defad` |
| Historical PR502 source cohort | `d15538d704a0dc198a0b6e9cfdd8ee27542f974c` | `5b3d4a45ef402db22fb2fb45dc6e64c86f0adc69` |
| Accepted PR641 port | `2f24c4aea6487fb81195e583fad186d32b443829` | `65806a397c5eba9182d8e1261843ff5ec317688b` |
| PR641 source checkpoint | `b20adf393667166f75572c6812f4ee9ba75cd2aa` | `7c46a757d962429d4280e0b7dddbec603391e703` |

The historical PR502 record at `1d048c9d...` accounts for all 29 authored
paths and preserves their old blobs. The current port carries only the
quota-free owner/provenance delta and explicitly preserves historical
source-only/non-equivalent status. The old Effects path is retained only as a
historical mapping; it is absent from the current tree and has the same Git
blob as the canonical path. Current test helpers read the canonical Effects
and emitter paths directly.

## Ported paths and semantics

- `emitter.gravity`: raw native-record preflight, explicit absence versus
  malformed producer/dispatch, known/foreign provenance, mixed List-Set,
  Map-Keys and bare-Set census composition, application and higher-order edge
  guards.
- `language/compiler/src/gravity/effects.gravity`: matching body-level
  carrier/provenance and mixed-consumer guards; the prior
  `bootstrap/gravity/src/gravity/effects.gravity` path is historical only.
- `bootstrap/gravity/p15_s23/emitter.gravity`: matching emitter-level
  carrier/provenance and mixed-consumer guards.
- Four current-owner Clojure test namespaces, including PR641's six exact
  comparisons across two families, two ledger orders, and row/fact/compiler
  products. No allowance/payment arguments were restored.
- The missing preserved local-runtime bare-set pair, byte-identical at SHA-256
  `a65f559b...`; it remains outside accepted fixtures.

## Evidence

The current post-relocation checks passed: PR502 six comparisons are covered by
`2 tests / 174 assertions`, the bare-Set owner controls are `2 tests / 83
assertions`, PR598 mixed joins are `3 tests / 216 assertions`, and the exact
preserved fixture pair is `1 test / 25 assertions`. The three Effects selectors
are `3 tests / 778 assertions`. Boundary validation passed `6 tests / 11
assertions`; documentation and roadmap validators passed; pinned changed-file
Clojure lint reported zero blocking warnings/errors. These are bounded
development checks, not public C8/L5 or source-admission proof.

The receipt is
`specs/accounting-removal/pr502-current-owner-guards-port.edn`. Independent
acceptance and exact integration preflight remain pending.

## CI cancellation evidence

The failed-looking Resolution result was inspected from run `41693000912`, job
`126116475947`, rather than treated as a source failure. Its preserved artifact
is `ci-resolution-pipeline-9ae87e8fce94b79e45c01470ccc5d23c7ffc2a42-1`
(artifact `13261850287`), downloaded at
`/tmp/uniform-pr879-resolution-ckF3X6`. The artifact checkout was commit
`9ae87e8fce94b79e45c01470ccc5d23c7ffc2a42`, tree
`4a3443844c04426dd74ff43f50e52ed80f0b2786`.

The first sequential child, the resolution cohort excluding the fresh-replay
case, completed with `:status :passed`, exit `0`, and its 600-second watchdog.
The second child, the exact
`verifier-requires-fresh-replay-current-products-and-public-proof` case, was
still `:running` when the enclosing 15-minute Resolution job was cancelled.
There is no failure receipt or source mutation in the artifact; its generated
worktree entry is only `?? target/ci/`. This is the two sequential 600-second
children versus a 15-minute outer envelope mismatch, not evidence that the
fresh replay failed. It also does not claim that the fresh replay passed.

The narrowly accepted envelope repair is commit
`7108bc14aac80eb1cc76729738c97741f6fc41a2`, limited to
`.github/workflows/ci.yml` and `docs/ci.md`: the Resolution envelope is 25
minutes for the two 600-second children, with the split watchdog documented.
CI was not rerun during this composition.

## Final current-main composition

The candidate was normally composed with the exact returned main base
`77027c72003b636d0c475092fb01309848df9923` (tree
`2be1bf39abd1fd4b290acd0b1f2aa488dbc006b8`) whose parent is
`4bf83407647813cbef54573571e0f31a2d7062b4`. The source/effects and focused
checks were clean at pre-base composition commit
`d89640677bdebbbe1074cc9a9fbbae3c3fe16247` (tree
`040cc06d82dca6f0495d70dd98a7b2f6da9e6594`); that commit contains the
two-file envelope repair and the ten authored PR879 paths. The normal merge
with main 887 is composition commit
`2ea65285ebdca79a2154ec3c30c39d4b75c34946` (tree
`adcc9f5409100bad8ea602457d5612077acf3e1a`), with parents the pre-base
composition and main 887.

The final diff against main 887 is exactly twelve paths: the ten authored
source/test/fixture/report paths listed in the historical accounting above,
plus the two accepted CI-envelope files. Main 887 changed only
`tools/ci/lint.clj` and `tools/ci/lint_facade_test.clj`, disjoint from the
owned Effects/emitter/tests/fixtures and envelope files, so the focused checks
listed above remain attributable to the pre-base composition. No fresh replay,
native callback, public C8/L5, source-admission, release, or self-hosting claim
is made here.

## Explicit nonclaims

The active genuine public-C8 proof (PID `48316`) and its cohort are unchanged.
PR502/PR598 remain open. No public C8/L5, authenticated Reader/Core/C5/C11,
native/callback/runtime, migration, release, self-hosting, or seed-retirement
claim follows from this candidate.

## Accepted public macro receipt adoption

This is an appended bounded adoption frame. The preceding twelve-path final
composition remains historical; this frame adds one accepted test-only source
path and does not rewrite the earlier source/test/fixture accounting.

The exact current base is `77027c72003b636d0c475092fb01309848df9923`
(`2be1bf39abd1fd4b290acd0b1f2aa488dbc006b8`). The clean code checkpoint is
`38769f708e29604cd9a03e74fbcb2fe5a61f992d` with tree
`13b671550ebf5921461e266beec3422535effb08` and parent
`cb8b6f04a316906e1a88f34c9669d54a51883fc7`.

Only `bootstrap/clojure/test/gravity/macro/pipeline_test.clj` was adopted. Its
source blob was `c1e961617a2e0795516091a25e1a338f54004b78`; the result blob is
`2670c12834328613a0f72ba025ec9fc42c383181`. The supplied patch was
`/tmp/uniform-macro-public-receipt-2516.patch`, SHA-256
`91d7f9e8b039728e674ea97259915774781f5f760aa35d5a9c633fa30795ce78`, from
source evidence commit `2516b84c1d9e8a3b5653b12cfc7171aa62672ecf`.

The source evidence at `2516b84c1d9e8a3b5653b12cfc7171aa62672ecf` reports the
following origin-focused controls:

- `public-receipt-reuse-requires-current-stage-and-seed-operations`: `1 test /
  197 assertions`.
- `source-backed-operation-interpositions-stay-live`: `1 test / 27
  assertions`.
- `identical-value-thread-binding-disables-receipt-reuse`: `1 test / 8
  assertions`.

These are recorded as source-evidence receipts, not as a fresh local rerun.
The current diff against the exact base is thirteen paths: the historical
twelve plus this macro test path. The previously verified Resolution
17m13s result and macro 900s nonpass remain historical; current CI is pending
publication. No new C8/native/source-admission/public-L5 or self-hosting claim
is made, and no heavyweight cohort was rerun locally.
