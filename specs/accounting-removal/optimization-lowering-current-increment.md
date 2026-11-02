# PR370 current optimization and lowering increment

Owner: ACC-11 / PR370. This report records a bounded current-source increment;
it is not a migration, release, self-hosting, or whole-accounting completion
record.

## Immutable source and governing contracts

The checked source is commit `dcf9173d4637b406136963b0cfa44a7879d443b0`,
tree `3aabe0af1d7e2d4c051feff7e18cc8cff65b06eb`. Its immediate parent is
`b0a12af5bc44defcba3e704d59b27ab8217a30b7`, tree
`76fcc78944c18105c94eae177e9bda9386d9b651`; the PR370 source ancestry is
based on `266da04e8fa734ac5e45aa893440d9082f02e8d4`, tree
`63995ea3a1c413c949d312f9eb584f9bfe7f7e35`. The inspected checkout was
`/Users/matt/.codex/worktrees/review-370-c6-memory/uniform`, branch
`codex/review-370-c6-memory`, with no pre-existing worktree changes.

The owning accounting-removal contract is [the ACC specification](spec.md),
especially its removal, preserved-semantics, producer/consumer, evidence and
independent-review requirements. The development boundary is
[Development Verification](../../docs/development-verification.md): focused
semantic checks and affected integration are distinct from authoritative
migration. C13 and C14 remain governed by [C13](../../docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md)
and [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md),
and the required pass and handoff boundaries remain in the [SH-16/17 plan](../../docs/self-hosting-milestone-completion-plan.md).

This increment removes artificial project accounting only where its source
owners establish that disposition. It does not replace type, profile, effect,
capability, source, provenance, target-representability, native-class,
callback, tamper, or memory-safety validation with a new quota.

## Current-source receipts

The fresh selected checks were run against the immutable source above. The
receipts are retained at the following paths:

| Scope | Result | Receipt |
| --- | --- | --- |
| B1/B2/B4/C13/C14 current source-model controls, with five explicitly baseline-blocked canonical selectors excluded | 26 tests / 456 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-current-source-model-controls.log` |
| Trusted carrier and checked-owner cycle controls | 2 tests / 22 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-current-trusted-carrier.log` |
| Actual C13/C14/B1 development composition | 1 test / 21 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-current-development-composition.log` |
| Actual current-source SH-16 optimizer, relative report, and SH-17 lowering leaves | 54 tests / 5,868 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-current-dynamic-leaves.log` |

The source-model receipt covers the selected B1/B2/B4/C13/C14 source paths;
the composition and dynamic receipts cover the affected producer/consumer
interfaces. The dynamic and composition checks are ordinary current-source
integration evidence. They do not establish authenticated source migration or
canonical self-hosting authority.

The earlier mixed source-coverage observation remains historical and is not
rewritten: 31 tests / 493 assertions with 19 failures and 3 errors. The `b0`
source snapshot also retains its earlier five-failure result. The final
selected receipts above are not a claim that either nonpassing observation
passed, nor a claim that an unselected selector was silently waived.

## Carrier and authority boundary

The standalone `sh16-*` and `sh17-*` development kernels exercise finite raw
DATA shape and current-source behavior. They are not canonical source
authentication or release authority. Their direct development invocation must
not be promoted to an admission proof.

The checked owner path is different. The stage-2 C13/C14/B1 packet owner
requires a trusted C11 artifact before construction and calls
`p15-s23-trusted-carrier-validation` before traversal. That guard admits the
exact trusted persistent carrier classes, rejects unsupported native carriers,
and tracks active ancestors with `java.util.IdentityHashMap`; cyclic carriers
are rejected while finite shared aliases remain valid. C13 and C14 production
records are then derived from the guarded C11 artifact. This is the scope of
the two trusted-carrier checks, not a claim that the raw development kernels
themselves prove canonical acyclicity.

The report therefore preserves both facts: raw finite DATA is suitable for the
development shape lanes, and the checked public owner has the existing
identity-based cycle boundary. No quota, callback, new host language, or
permission gate is introduced to bridge the two.

## Known nonpasses and preserved limits

The following limitations remain explicit and are not converted into green
results:

- The canonical C2 Reader pin refuses with expected `231576` and observed
  `246369`. The Reader source and contract pins are unchanged from the main
  baseline. This is a pre-existing canonical baseline block; no expected hash
  refresh is authorized here. Diagnostics that depend on that refusal are
  downstream `blocked-by-baseline` observations, not independent PR370
  regressions.
- SH-07 registration retains the baseline mismatch `B33` expected versus
  current `B47`. It is recorded as a separate canonical baseline block; no
  registration refresh is performed by this increment.
- The historical C12 attempt is a nonpass: it exited 130 under the 2 GiB
  resource boundary. Diagnostic JFR 70 showed the runtime building large
  maps/vectors for the live SH-03 graph; it did not prove a dominator leak or
  justify restoring an accounting quota. C12 was not retried or reframed.
- The initial B1/B2 lookup-count observations and C14 unknown-backend
  diagnostic observations were independently reviewed and corrected in
  `b0`/`dcf`: the B1 baseline count is recorded as `1085/1077/8`, the B2
  quota-removal count as `836/786/50`, and the C14 oracle now uses the correct
  `TARGET target-contract-mismatch` diagnostic already present in base. The
  final source-model receipt includes these repaired controls (`26/456`), so
  they are not unresolved audit items. The historical mixed-run failures stay
  recorded as history; no production pin or admission behavior was refreshed.

These records preserve the initial nonpasses, the later focused passes and
their causal limits. They do not create an approval gate for ordinary semantic
work, and they do not waive the required positive and negative producer /
consumer checks.

## Disposition and nonclaims

This report supports a bounded current-source optimization/lowering increment:
the selected source controls, trusted-carrier controls, C13/C14/B1 composition,
and current-source dynamic leaves passed on the exact source identity above.
Independent acceptance and coordinator integration remain required before
this work receives main-roadmap credit.

The evidence does not claim:

- canonical C2 Reader or SH-07 registration conformance;
- authenticated source migration, canonical SH-16 authority, or C12 success;
- release, seed retirement, self-hosting, native toolchain, or whole-accounting
  completion; or
- that the historical mixed run, its downstream canonical diagnostics, or the
  resource-limited C12 attempt passed.

Later main changes, including the landed PR804 C12-C14 quota removal, are not
part of this frozen source receipt. The coordinator must reconcile that newer
main against this source and its producer/consumer evidence before any merge;
this report is not a request to replay or blindly apply overlapping hunks.

## PR370 reconciled current-source receipt

The following receipt records a normal local reconciliation of PR370 against
the frozen main parent `73df05ab2935c1c4dcf5b48e7d81980cc8446c1d` (tree
`c906f87024c479d47c691add481c8f0a04efe107`). It does not rewrite the original
PR370 source evidence above, and it is not a migration or release receipt.

| Identity | Value |
| --- | --- |
| Frozen PR370 base | `266da04e8fa734ac5e45aa893440d9082f02e8d4` / tree `63995ea3a1c413c949d312f9eb584f9bfe7f7e35` |
| Corrected PR370 report head | `63a4a6e1bbe4c5d613b9a15f69e417e84ff1a476` / tree `c3cf951c35ed5b417912bdeae48c0aaa5f42a1ed` |
| Normal merge source commit | `cc8377115efa8e56c2ec24b377591a1699c43dd5` / tree `fd666b1ce24df11c7e2875371c79afc5ddfced6d` |
| Final checked source commit | `c5561a06316d3a2062c3485cd8d04c3a321732bb` / tree `6a3e6e827393c5ca3ab747c92d5407dfd81fadad` |

The merge retained main's PR804 C12 quota removal and its semantic carrier and
sequence checks. It retained PR370's C13 relative flat-bool report shape,
semantic carrier checks, C14 no-byte-quota and no-follow source checks, and
the affected negative and positive fixtures. The obsolete C13 adapter that
filtered semantic carrier labels and the old quota-only assertions were not
carried forward. The C13 source fingerprint was updated only to the actual
post-resolution source (`175301` bytes, SHA-256
`88ed5b2969992306f138e2a5dcea1391fdf5acf78461308f8dcfb6a6ec089ae3`, dynamic
last line `3724`); the C14 source identity remained `197233` bytes with SHA-256
`f7bf52be5e41c26e3f4a9474ec7cb56cdf0c924ccdd698dea2882b4939ea27e0`.
The SH17 suite also retains PR804's 10,000-level nested carrier and 300-key
map fixtures, without restoring the obsolete long-identifier rejection.

The pinned changed-file lint then found four newly exposed warnings caused by
the PR370 source rename: `:non-arg-vec-return-type-hint` on the four C6 stream
helpers in `00703b-p15-s23-c6c10-canonical-stream.clj`. Moving each return hint
after the function name is syntax-only and preserves the compiled contract.
With pinned clj-kondo `v2025.10.23`, the rerun reported `3480` unchanged
baseline warnings and `0` blocking warnings/errors. Receipt:
`/tmp/uniform-pr370-reconciled-static-lint.log`.

| Check | Result | Receipt |
| --- | --- | --- |
| Current B1/B2/B4/C13/C14 source-model controls | 26 tests / 456 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-reconciled-source-model-controls.log` |
| Trusted carrier, SH16, relative report, and SH17 dynamic leaves | 79 tests / 6174 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-reconciled-dynamic-leaves.log` |
| C13/C14/B1 development composition | 1 test / 21 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-reconciled-development-composition.log` |
| Language boundary | 6 tests / 11 assertions, 0 failures, 0 errors | `/tmp/uniform-pr370-reconciled-boundary.log` |
| Document validator | 240 docs / 19 phase indexes, passed | `/tmp/uniform-pr370-reconciled-docs.log` |
| Full-language roadmap validator | passed; dated coverage snapshot remains non-authoritative | `/tmp/uniform-pr370-reconciled-roadmap.log` |
| Workstream governance validator | 185 workstreams, passed | `/tmp/uniform-pr370-reconciled-governance.log` |
| Whitespace check | passed | `git diff --check` |

The source-model script reports `:native-process-execution false` and
`:historical-gates-executed false`; its canonical and environment-gated
selectors remain explicitly unrun. The reflective final-field warning from
the trusted helper lane is retained as a runtime warning, not a test failure.
The broad `gravity.bootstrap-test` namespace attempt was not credited: it
encountered the recorded C2/C6 baseline mismatches and was terminated, so it
does not change the focused receipts above. The existing mixed source run,
`b0` snapshot failures, C2 pin mismatch, SH07 B33 mismatch, and resource-
limited C12 exit 130 remain historical nonpasses and were not retried or
reclassified.

This receipt is ordinary current-source evidence only. It claims neither
canonical source migration, C12 success, native-C execution, self-hosting,
release, nor roadmap completion. It is pinned to the `73df` merge parent;
later main commits, including subsequent PR513 and root-version changes, are
outside this frozen reconciliation and require a new coordinator composition
if they overlap these paths.
