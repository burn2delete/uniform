# CLI Core argument boundary

## Contract and scope

D1/D2/D3/D8/D9, L1/L2/L3, C1/C6/C15, T1, BOOT1/BOOT8 and TEST2 govern
this increment. The CLI owns argument normalization; Core and Reader owners
retain source decoding, authentication, legality, identity and verification.

The existing `sh07-public-core-cli-input` implementation moves from component
00942 into `gravity.cli/sh07-core-input`. The public input is an argument
sequence for the already selected `sh07-core` command. Output is the existing
`[path options]` value. The helper selects no command, reads no source and
grants no authority. Preserve exact argument acceptance, failure ex-data and
message, the private compatibility wrapper, surrounding component and dispatch.

## Development evidence and dependency

The clean initial scope at653298a393209ac2488726fe0ade6064def5f020 uses actual
Tooling mainfd159988ab06ee0397505718e9bd96bbf8df8228. The original pure
argument test passes1/9. Full CLI tests6/79 report two baseline failures:
their expected help omits three build-help lines already present in main.
Only that expected fixture text is corrected; emitted help/version bodies
remain unchanged and their exact equality checks remain.

The initial documented public current-reader source control fails C6-VERIFY /
construction-dependency-drift before any CLI extraction edits. Its log is
`/tmp/uniform-cli-core-arguments-before-control.log`; the full report is
`/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/clojure-6726046598742302627.edn`.
Independent native tests identify the builder's local execution binding being
mistaken for Var-root drift. Separate Core PR730 repairs that invariant without
removing a guarded Var, changing a pin or supplying an authenticated packet.
That failure carries no Core-emission credit.

CLI development is stacked on exact reviewed Core candidate
721b52892f2af28db4ef2ffc4e45b532d81ed4de, which includes actual Bootstrap
literal main04de291fbed2bdda7070ecfba665d81207cd6d74. The conflict-free scope
compositionf5b3b8cf1f3cc1c4801547ece49e9e76906034e0 is clean before the
new control. This is a proposed source dependency, not an accepted main state.
Final CLI integration requires Core730 on main and fresh current-main
composition, review, own CI and clean integration preflight.

The corrected before-control emits real Core artifacts from both complete
`compiler-exact-keys.gravity` and `.qst` files through the existing private
argument helper and public development-current-reader API. Each has25 nodes,
one API target read, passed template/resolved verification and source hash
`sha256:8e63ea9e37ab40a8e66975b3e58dc8eb5b9b74aa339d2625d5bd67070802758d`.
The recomputed Core id is
`sha256:16ec6d50a8514188f3010880f3022e88f7f1054fe4058a5caa6b74367ee98f0e`
in both spellings, while actual extension/path provenance remains distinct.
The complete current Core implementation binding is390 functions with source
hash455f2d496e410ba8c5f4d17b2e4e558c6fb62306b614a6b9643107c8e1c82770;
its generic-seed, development-unadmitted and incomplete boundaries remain.

The after-control uses the unchanged private wrapper now delegating to the
stage. Full argument/source artifact data and serialization, entire component,
parser body/arity, and both unchanged presentation bodies must compare exactly.
Positive/negative argv tests include empty-path compatibility and rejection of
wrong/missing/duplicate flags before source IO. Source validation stays with
Reader/Core. Neither default pinned execution nor a historical denied route
is used to obtain evidence.

Scripts are `/tmp/uniform-cli-core-arguments-control.clj` and
`/tmp/uniform-cli-core-arguments-verify.clj`; corrected evidence uses
`/tmp/uniform-cli-core-arguments-evidence/cli-{before,after}-*`. Logs use
`/tmp/uniform-cli-core-arguments-*`. Focused/affected tests, language/docs/
roadmap/governance, contextual lint and exact independent review remain
integration obligations.

On the reviewed dependency, CLI tests pass7/104 and focused public ingress
units pass4/50; the pinned-constructor unit uses spies only, not a historical
pinned execution. Language boundary6/11, docs240/19, roadmap and governance185
pass. Corrected before/after controls compare three complete data/serialized
pairs exactly. The whole component/dispatch, moved parser body/arity and both
existing presentation bodies conserve against dependency721b5289. No default
source route, exit code, output format, compiler operation or authority policy
changes with this extraction.

## Actual main composition

Core PR730 is integrated as
c5420931eec8dbdc148bdfe3704ee5f316896303/tree
f4120297f3e7b3ec799707e7225180880cb43e4f. That tree is exactly the reviewed
dependency721b5289 used by the complete before/after controls. The CLI PR is
retargeted to main and normally composes actualc542 after implementation
28cf7eb1044a567d924245928d41eed5fc7dbce9. The merge has no content changes
or conflicts; CLI implementation, tests and complete source-control inputs
remain identical. Documentation records the new actual base. No proposed-stack
CI result becomes current-main acceptance; this composition requires exact
review, contextual lint, own CI and clean integration preflight.

Main then advances through external PR731 to
167ed15a2c3ce7d76e4eb6b9a761a55186f7a4ad/tree
58ce57515855fd4df4925aec86dab707e1edb2e0, parentc542. Its only changes are
the CI workflow and CI documentation: an always-failing placeholder that ran
no compiler is removed. The CLI composition preserves both incoming files
exactly. No compiler source, source-control input, real release admission or
CLI implementation changes in that delta. That composition uses base167;
routine CI must be observed under its actual workflow, without treating an
absent readiness job as a source-Core or release pass.

## Loader and snapshot CI main composition

Actual main advances to625b42aa8da25eb897df4b9807372e21108bd8e4/tree
54ac1c4bbcb47e492d2457e068e1765b2c675615 through shared test support and
direct bootstrap data loading. The normal composition preserves all incoming
123 loader paths exactly. Fresh genuine public current-reader controls emit
both complete source spellings with25 nodes, one API target read and the same
source hash and recomputed Core identity recorded above. All three complete
and serialized products match the corrected before-control. Whole component,
dispatch, parser body/arity and presentation bodies conserve against actual625.
CLI7/104, selected ingress4/50, language6/11, docs240/19, roadmap/governance
and contextual lint3 paths/1 unchanged baseline warning/0 blockers pass.
Receipts are outside the clean source checkout.

The next actual main is b363c32b5569e4ea76df852abd142be7c067292e/tree
49b5e7fa4d82a8d41fb3a8ffec876d39b8b8e495. Only CI workflow, CI docs and
snapshot tooling/tests change from625. The conflict-free composition preserves
those four files exactly; compiler, CLI, control subjects and execution inputs
remain unchanged. Actual b363 is the integration base. Updated-base exact
conservation, independent acceptance, own CI and clean preflight remain gates.
Earlier green or cancelled runs confer no acceptance for a new source tuple.

Current control logs/evidence use `loader-main-*`; the exact product comparison
is `/tmp/uniform-cli-core-arguments-verify-loader-products.clj`. One unsupported
test flag and an invalid namespace-as-exact selector stopped before tests;
correct supported selections pass. These retained launch errors are not test
or compiler failures, and no denied constructor route was executed.

## Affected-stage and cached-shard CI main composition

Actual main0e01fc5937d5b224dd03e14751566f0e541b6529/tree
7bd8ec6c99a12fd3338edb3fce7a3e941064abef adds conservative affected-stage
CI selection and shared development shard caching. Its six changed paths from
b363 are CI tools/docs, cached-shard runner/tests and the verification catalog.
The normal composition preserves all six exactly. Compiler sources, public
Core control subjects, CLI implementation/tests and their execution inputs
remain unchanged. Core products are the freshly executed loader625 controls.
No cached shard route is executed for
this slice's local evidence. Exact current0e conservation, docs validation,
contextual lint, independent acceptance, own CI and clean preflight remain
integration gates; previous-base CI does not satisfy them.

## Verified Build main composition

Own CLI CI41267943769 passes all nine enabled jobs ondd66a480/base0e,
with snapshot/prerelease skips. It is historical after main advances through
the compiler finalizer change and verified Build PR737 to
3b2b7bb267b8a8b1c6a2811b89abba376a0ca2cc/tree
65efa902052c808cb9083b5188a7312cf64ef583. The normal CLI composition has
no conflicts. CLI implementation/tests and surrounding dispatch remain exact;
incoming compiler and Build code, fixtures, unique helper/test identities and
ownership records are preserved. The namespace map merges the independent
CLI and Build rows. Fresh actual-Build-main public Core controls, source and
identity comparisons, affected checks, review, own CI and clean preflight are
required; previous-head green confers no new-base acceptance.

## Unsupported boundaries

This remains a Clojure bootstrap CLI increment. Existing SH07-CLI-ARGS and
the enclosing entrypoint's error exit remain compatibility behavior; this does
not establish T1's full usage-code, JSON, command-family or automation contract.
Other command arguments, dispatch, diagnostic rendering, source authentication,
compiler semantics and process exit stay with their existing owners. The
development reader choice renews no migration or release authority. No backend,
full Core/CLI completion, self-hosting, release or seed-retirement claim follows.

Namespace presence and development checks confer no integration credit.
