# CI and experimental source snapshots

## Purpose

GitHub-hosted checks replace routine laptop verification for pull
requests and the exact clean integrated main commit. They do not confer
compiler migration or release acceptance.

### Focused CI control tests

From the repository root, the CI control script accepts an explicit selection
of fully qualified test Vars:

```bash
clojure -M tools/ci/run_test.clj --exact \
  ci.run-test/stage-selection-follows-owned-downstream-closure
```

Multiple names follow the single `--exact` flag. The complete selection is
validated before any test runs; malformed arguments, duplicate names, unknown
Vars and non-test Vars fail. Selection resolves already loaded namespaces and
does not load a namespace named by an argument. Selected tests retain their
fixtures, reports and failure exit status. With no arguments, the existing
complete CI control and lint-context suites run unchanged.

Review the selected test bodies against the current operating restrictions.
Selection grants no authority to execute a restricted operation. Loading this
script without controlling its entrypoint still invokes its default suite;
use the explicit CLI route for focused execution.

## Workflow matrix

| Check | PR | Main/manual/weekly | Evidence and limits |
| --- | --- | --- | --- |
| static | Always. Git whitespace only for changes limited to reviewed explanatory docs. | Always | actionlint 1.7.12 workflow syntax, clj-kondo 2025.10.23 on changed Clojure/EDN files and CI tooling, cljfmt 0.13.4 on CI tooling, Git whitespace, and harness failure controls. |
| contracts (projections) | Always. Reviewed explanatory docs select document and roadmap validation. Reviewed JVM backend changes select the language boundary and JVM pipeline. | Always | Full CI runs the canonical 240-document validator, the full-language roadmap validator and its negative fixtures, acceptance dependency controls, native and type-inference suites, and the JVM pipeline. |
| bootstrap | Stage selection | Always | Cold Bootstrap load, Reader and Syntax regressions, language boundary, catalog controls and ownership inventory. Reader changes and full runs also check the plan hash. Edits to reviewed Stage2 module tests run the affected full namespaces in separate fresh JVMs. |
| macro | Stage selection | Always | Three fresh processes run the ordinary pipeline tests, the receipt and artifact verification controls, and the Bootstrap C4 compatibility namespace. |
| checked-core-runtime | Stage selection | Always | Checked-Core pipeline, current-binding and projection-kernel regressions. The Node 20 checks cover B6 scalar execution, checked-add guard identity and the public CLI smoke test as described below. |
| resolution-pipeline, resolution-unmetered, and resolution-invocation-selection | Stage selection | Always | Three independent Resolution matrix jobs may overlap and retain separate logs and receipts. `resolution-pipeline` runs two sequential fresh children (cohort and fresh replay), each with a 600-second command watchdog. The other two modes run one fresh child each. |
| fresh-leaf-regressions / portable and / darwin | The reviewed source/test require closure selects affected catalog entries. Unknown, cross-cutting, structural, or closure-discovery changes use the full platform catalog. Reviewed explanatory docs, JVM backend changes and Stage2 module test-only changes omit both jobs. | Full fresh catalog | 52 portable namespaces on Ubuntu and one Darwin publication namespace on ARM macOS with its exact JDK 26.0.1 contract. Each selected namespace gets a fresh JVM. |
| darwin-unsupported-host | Reviewed explanatory docs, JVM backend changes and Stage2 module test-only changes omit this job. Other PRs run the refusal probe. | Always | The actual macOS 15 kernel must reject required confinement features before any publication open. |
| source-Core-readiness | Omitted | Omitted | No source-Core readiness job is enabled: the former constant-failure placeholder did not execute a genuine compiler route. The real public `gravity self-host verify` boundary remains fail-closed with `P18T04007` while the seed boundary is active; no CI check or source-only snapshot substitutes for that release/self-hosting admission. |
| experimental-source-snapshot | Omitted | Manual dispatch on `main` only | Exact SHA source tarball, one Git archive, SHA-256 manifest, capability notes/changelog and provenance. No compiler binary. |
| experimental-prerelease | Never | Manual dispatch on `main` only, after routine checks and packaging pass | Separate contents-write job publishes the same-run source bundle as a prerelease; Core readiness is explicitly outside this source-only claim. |
| public-release assessment | Never | Explicit dispatch of `public-release.yml` only | Existing P18 public subset proof with authentic public commands and pinned source checks. Read-only repository permissions; no publication. A failed proof remains a failed assessment. |

### Pull-request stage selection

Pull requests run the selector to determine which checks need runners. It
selects the three Resolution jobs together and each runs independently.
Pushes to `main`, scheduled runs and manual runs start the full matrix
directly.
For a regular added or modified file in a known stage-owned
Reader, Macro, Resolution, or checked-Core path, `tools/ci/run.clj` runs that
stage and every downstream stage, while retaining `static` and `contracts`.
Changes spanning multiple known stages select the union of those downstream
closures. This is an execution selection only; it does not change ownership or
the portable leaf catalog. The C7 Gravity type-checker source selects the
checked-Core stage with the current-source SH08 consumer tests. The B6
JavaScript backend source selects the checked-Core stage with its Gravity
source check and the checked JavaScript owner execution. The owner test also
runs for full, unknown, and push CI selections, but is omitted for unrelated
stage-selected changes. Edits to the B6 guard test or its lightweight seed
select the same owner child, which exercises guard identity through both the
lightweight and full seeds. Only the C7 and B6 sources have a stage-owned
exception to full leaf fallback. Other paths outside reviewed leaf closures
retain the full catalog even when a compiler stage owns them.

For changes to the SH-03 Reader plan, its inputs or its adapter test, and on
full runs, Bootstrap runs `sh03-current-reader-plan-checks-keep-historical-priority` in its own
process. It verifies that validation hashes a tampered plan body once and
rejects it with `C2-HASH` even when the public identity helper returns the
forged ID.

Edits to the public JavaScript launcher, dispatch,
checked producer adapter, smoke fixture or smoke tests also run one public
compile followed by a Node 20 import and export calls. This smoke check runs
on main, on scheduled and manual CI, and when PR path discovery fails.
Adding or editing an unowned EDN file under `docs/` runs the full matrix and
changed-file lint. An unknown non-documentation path, a contract/specification change, a dependency
or workflow change, a tooling change, or any delete/rename/mode/type change
falls back to the full semantic matrix. If changed-path discovery is missing
or malformed, the selector also falls back to the full matrix, while `static`
records an explicit failed selection result so an untrusted diff cannot pass
without changed-file lint.

Regular additions or modifications limited to the seven Stage2 module tests
in `contracts/stage0-incremental-test-dependencies.edn` allocate runners for
`static`, `contracts`, and `bootstrap`. Static lints the changed Clojure files.
Bootstrap runs the cold load, language boundary, catalog controls, ownership
inventory, and the affected standalone test namespaces. A `map-order` edit
also runs `map-call` and `literal-map-key`. A `map-call` edit also runs
`literal-map-key`. These downstream namespaces import helpers from their
predecessors.

Mixed or structural diffs run full CI and add the affected Stage2 namespaces
for any known regular test edits. If the selector cannot validate the seven
catalog entries, it runs the full matrix and Bootstrap records a failed
catalog check. Each Stage2 child runs its entire namespace with a
2,700-second process watchdog and keeps its own log and receipt. The
Bootstrap job uses a 20-minute timeout for pushes, schedules, and manual
runs. Pull requests use a 355-minute envelope when Stage2 tests are required
or selector outputs are unavailable or invalid. This covers seven sequential
watchdogs and the full Bootstrap controls within the hosted job time limit.
[GitHub Actions limits](https://docs.github.com/en/actions/reference/limits#existing-system-limits).

The JVM route applies when an authenticated PR diff contains only regular
additions or modifications to the enumerated JVM owner, its two dedicated
Bootstrap adapters, or its pipeline tests. The required `static` check runs
Git whitespace and changed-file Clojure lint. The required `contracts` check
runs the language boundary and the full `gravity.backend.jvm.pipeline-test`
namespace on the pinned JDK. That suite compiles both source spellings with
`javac`, builds and runs deterministic Java 21 JARs, and checks toolchain,
lowering, and artifact refusals. Only the selector, `static`, and `contracts`
need runners for this route. Mixed or structural diffs use full CI, which
also runs the JVM pipeline on main, scheduled, and manual runs.

The `resolution-pipeline` mode starts two fresh 600-second children
sequentially: one cohort and one fresh current-product replay. The other two
Resolution modes each start one fresh 600-second child. Each child records its
own log and receipt, while all three Resolution matrix jobs may run at once.
Each matrix job has a 25-minute hosted-job ceiling so
the two-child pipeline has room for its 1,200 seconds of command watchdogs
plus checkout, toolchain setup, cleanup and artifact upload. The hosted
ceiling is an orchestration envelope; it does not replace or extend either
600-second child watchdog, and a child timeout remains a failed result with
its receipt preserved. This is not
equivalent to the former single combined 900-second command, and it is not a
global parallelism limit or a cost-equivalence claim. The local combined
`resolution` CLI mode remains available with its 900-second watchdog for
compatibility. The Resolution matrix permits all three independent jobs to
overlap; the other matrix pools retain their existing parallelism. Publication
depends on the Resolution pool while retaining the existing manual
source-publish rule.

The documentation-only path applies to regular additions or edits to the
reviewed explanatory paths listed in `tools/ci/run.clj`, such as `README.md`
and `docs/ci.md`. These PRs run Git whitespace, the canonical document
validator, and the roadmap validator with its negative fixtures. Canonical
phase documents, roadmap inputs, dependency records, policy and evidence files, and unknown
Markdown paths retain full coverage. A change to `tools/ci/snapshot.clj` or
`tools/ci/snapshot_test.clj` adds the snapshot repeatability controls to the
static job.

### Explicit public release assessment

The separate `public-release.yml` workflow runs the existing
`gravity.bootstrap-test/p18-public-test-command-runs-current-public-subset`
test in a fresh child process. Its existing setup builds the packaged JVM CLI;
the test exercises five accepted programs through public check/run/compile and
emitted execution, 32 rejected cases with stable diagnostics, source path and
extension provenance, and rejection of a full-conformance overclaim. Dispatch
this workflow explicitly for release assessment of the selected revision. It
does not run on pull requests, pushes or schedules, and is separate from the
ordinary `CI` workflow's manual source-prerelease publication. The underlying
CI mode is `clojure -Srepro -M tools/ci/run.clj public-release`.

This release proof reaches the default pinned SH03 reader through public
`check`. Source drift may correctly fail with `C2-HASH`; a failed release
proof does not make an ordinary semantic change invalid or authorize updating
the pin. Ordinary contracts selections retain their existing commands and do
not run this release proof. The current-reader development route and its limits
remain documented in [development verification](development-verification.md).

The child retains its 900-second host watchdog inside a 20-minute hosted job.
The CI-only `ci.public-release-subset` adapter delegates the same exact test
to the development runner. It redirects the test's three generated P18 proof
files to `target/ci/p18-public-test-artifacts` and emits phase and fixture
markers to stderr. It also retains each returned accepted/rejected fixture
record, including the returned `:out` and `:err` text, in a fresh unique
per-invocation directory beneath that path. Each diagnostic record is explicitly
non-authoritative. Its manifest entry carries the byte count and SHA-256 of the
actual UTF-8 EDN record file. The manifest's own byte count and SHA-256 are
emitted in its `:manifest-written` diagnostic marker and remain covered by the
existing CI checksum and provenance outputs. These files are diagnostic
observations of the existing P18 caller; they do not add proof authority or
claim that a failed assessment is a successful release.

After each successful record capture, the adapter writes another fresh,
non-authoritative manifest with `:status :partial`. Each such checkpoint lists
exactly the captured prefix at that point and is kept alongside earlier
snapshots; it is not overwritten or deleted. The existing `finally` path still
writes a distinct `:status :final` manifest on normal return or aggregate
exception. `:status :final` describes finalizer execution, not a successful or
complete assessment. A partial index likewise records only a valid returned
prefix, not completion of the 37-case suite or release proof.

Capture failures are emitted as diagnostic markers and disable later capture
attempts for that invocation while preserving the wrapped function's original
return value or exception. No record is overwritten or deleted. The adapter
preserves public commands, results and the existing 60-second inner process
watchdog. CI still rejects tracked source mutation; the normal artifact
checksum manifest includes any generated proof or diagnostic files and retains
logs, command receipts and checkout provenance after failure.
An external hard kill may leave unfinished record or snapshot files because the
adapter cannot run its `finally` block; it makes no fsync or crash-proof claim.
Completed snapshots remain observations of their listed prefix; they do not
establish that the remaining fixtures ran or that release proof succeeded.
Markers include monotonic nanoseconds for intervals within that child JVM.
The workflow propagates failure and has no release-publication job. A completed
assessment is not a successful public proof: inspect its exit status and
artifacts. Even a successful current bootstrap-hosted subset proof does not
establish full-language, self-hosting or seedless-release acceptance.

### Read retained public diagnostics

The retained public-release upload can be inspected without replaying the
assessment:

```bash
clojure -X ci.public-diagnostic-report/run! \
  :artifact-dir '"/path/to/extracted-target-ci"'
```

`ci.public-diagnostic-report/report` is the programmatic entrypoint and
`run!` accepts exactly one non-empty `:artifact-dir` option. The directory is
the extracted upload root containing `SHA256SUMS`, checkout and run receipts,
the child receipt, and `p18-public-test-artifacts/`; it is not a checkout to be
replayed. The reader performs no network, provider, Git-source authentication,
subprocess, runtime-Var resolution, public-test invocation, or file write.

The report validates strict UTF-8 and one-form EDN inputs, rejects duplicate,
absolute, traversal or symbolic-link paths, and requires `SHA256SUMS` to cover
every regular file except itself with matching bytes and SHA-256. Producer
marker paths beginning with `target/ci/` are mapped once into the extracted
root. Mixed JVM/progress log text is ignored; only independent
`ci-public-release-subset` marker forms are inspected. Full diagnostic record
files remain the source of record. The report exposes their relative paths,
byte/hash identities, fixture phase and ordinal, record keys, and nested
`:out`/`:err` UTF-8 byte/hash identities without inlining command text or
claiming redaction or raw process-wire completeness.

Every available partial or final prefix manifest is checked against its real
record files and identities. Missing indexes remain `:not-observed` or
`:unindexed`; they are never reconstructed. A `:final` manifest describes
finalizer execution only, not completion of the 37-case assessment or release
proof. The child receipt's negative status, exit code and timeout remain in the
diagnostic result, and a valid report command exits zero even when that
assessment failed. Malformed, unsupported, tampered or contradictory artifact
data fails clearly. The output is diagnostic-only and grants no release,
admission, migration, self-hosting or seed-retirement authority. Hard kills may
leave unfinished files; the reader makes no fsync or crash-proof claim.

### Read retained Macro diagnostics

The retained Macro upload can be inspected without re-running any child:

    clojure -X ci.public-diagnostic-report/run-macro! \
      :artifact-dir '"/path/to/extracted-macro-artifacts"'

ci.public-diagnostic-report/macro-report is the programmatic entrypoint and
run-macro! accepts exactly one non-empty :artifact-dir option. The extracted
root must contain the ordinary SHA256SUMS, checkout.txt, run-url.txt, and
macro-summary.edn files. The summary may retain either the full result vector
or the runner's selected form
{:mode "macro" :selection map :results vector}; the report preserves that
shape and selection map while removing command, stdin, and OS-process fields
from child results.

Each executed result is joined to its confined <id>.edn receipt and
<id>.log file. Receipt identity, source commit, workflow run, attempt, status,
exit code, watchdog, start/finish and elapsed observations must agree with the
summary and checkout/run records. Passed, failed, timed-out, launch-error and
actual skipped rows remain distinct; a skipped or selection-error row does not
receive a fabricated receipt. The reader checks only logical status/exit and
positive watchdog values and imposes no project or input quota.

Only log lines carrying the
:gravity/development-test-progress-v1 schema are parsed as EDN. Other JVM and
test output remains ignored. Recognized frames retain their artifact path and
one-based line number, in log order, after schema, non-authoritative authority,
keyword phase/edge, and optional namespace, test-var, and error-class checks.
Malformed recognized frames fail closed. The report exposes all ordered frames,
the exact seven-phase Macro-paired subset, its count, and the last observed
frame; no missing return is interpreted as a timing, cause, pass, or completion
claim.

The report retains SHA-256 identities for every extracted file and marks all
progress as non-authoritative. It performs no network, provider
authentication, Git-source authentication, subprocess, runtime-Var resolution,
test invocation, or file write. The result is diagnostic-only and grants no
main, CI-pass, release, migration, self-hosting, seed-retirement, repair, or
recovery authority.

### Portable leaf selection

The leaves jobs use the same verified pull-request path records. For regular
source or test changes, `tools/ci/run.clj` follows the reviewed `:require`
closure of each existing catalog entry and runs entries whose closure contains
the changed path. A shared helper can therefore select more than one entry,
while an unrelated leaf is omitted. Changes limited to reviewed explanatory
docs select no leaves.
Unknown or unowned paths, cross-cutting/dependency changes, deletes, renames,
mode/type changes, and any closure or discovery failure fall back to the full
platform catalog. When the selector finds no affected entries for a platform,
the workflow skips that platform before it allocates a runner. Full runs and
failed path discovery dispatch the full platform catalog.
Positive Darwin publication and the unsupported-host refusal probe run on their
required macOS/JDK platform when selected. Main, manual, and scheduled runs use
the full catalog.

### Macro execution cohorts

The Macro mode retains all pipeline and Bootstrap compatibility test bodies and
normal fixtures. It selects the receipt/callback controls and the artifact
forgery and physical provenance test by exact name,
the complement of those names in the same pipeline namespace by regex, and the
whole compatibility namespace. Source-inventory controls prove exhaustive,
disjoint selection. New ordinary pipeline tests join the complement.
Each child has an independent watchdog. The CI control test compares the
hosted job timeouts with the sum of the child watchdogs for Macro and
Checked-Core with all optional consumers selected. It checks for at least
five minutes for setup, cleanup and artifact upload. A child failure fails
the Macro job and later children still run and retain their receipts.

The prior combined run 41713342032 timed out in 900071ms after twenty-five
test returns, during real-paired-macro-expansion-retains-identities-provenance-and-output.
That nonpass remains bound to its old source/provider. This partition changes
execution boundaries and does not claim a compiler or performance fix, waive
tests, or reclassify historical failures. New source-specific command IDs and
receipts identify the three children. Compiler, migration, release and
self-hosting acceptance still require their separate evidence.

TEST2, C18 and D1/D2/D3/D6/D8/D9 govern this orchestration boundary. Runner
controls cover source-inventory completeness, disjoint selectors, missing or
overlapping selection, retained negative fixtures, failure propagation and
all-child receipt production. They pass 48 tests/509 assertions alongside
pinned actionlint, clj-kondo and cljfmt checks and the language boundary.
The first new control had a lexical-scope parenthesis error and failed before
test execution; that log is retained, and the corrected controls pass without
altering any compiler or Macro test body. Real execution of all three children,
exact source review, candidate-owned CI/artifact audit and immediate current-main
preflight remain separate requirements.

Normal composition preserves main
05df36d8535afef03d3f3ebea2b7c80eaab26a76, including its archived-owner
context controls and their runner invocation. The earlier implementation
source e3ab23ae69f3da89decbd9b7541ac4837526969b and 45/487 control result
remain historical. The current four-path repair does not change Macro tests,
their source fixtures, the development runner, or any compiler source. The
frozen PR894 selector additions are outside this patch and must be preserved
if they enter main before integration.

## Requirements and dependencies

The audited baseline has no checked-in Actions workflow, dependency lockfile,
formatter configuration or CODEOWNERS. `deps.edn` has test aliases and only
Clojure's standard library dependency. CI pins CLI/runtime 1.12.5.1654/1.12.5,
Temurin 21.0.8+9.0.LTS (portable checks) and 26.0.1+8 (Darwin), action revisions,
and static tool versions. It does not modify
compiler dependencies or source authentication pins. Hosted runner images can
change; record the run environment and do not claim hermetic reproduction.

The Darwin source contract declares ARM macOS, JDK 26.0.1 and required native
NOFOLLOW_ANY, RESOLVE_BENEATH and exclusive-rename features. These features were
already used by publication; the preflight makes their absence an explicit
`:unsupported-confinement-features` refusal before any publication path opens.
It checks baseline and required flag admission with invalid descriptors for
`fstatat` and `renameatx_np`, requiring exactly EBADF. Unexpected success or any
other error fails closed. It neither opens files nor substitutes weaker flags.
Flag admission alone does not prove path enforcement: every existing identity,
link-count, ACL, mode, symlink, hardlink and race guard remains in force.

The actual `macos-15` run 40538478314 on macOS 15.7.9 / Darwin 24.6.0 rejected
the BENEATH bit with EINVAL and remains a recorded nonpass. A separate hosted
`darwin-unsupported-host` check exercises the new real refusal on that kernel
and asserts zero publication opens. The full positive and hostile suite runs
unchanged, with additional preflight branch controls, on standard ARM `macos-26`.
GitHub lists that label among its standard public-repository runners at
<https://docs.github.com/en/actions/reference/runners/github-hosted-runners#standard-github-hosted-runners-for-public-repositories>.
Runner selection is not acceptance: actual OS, kernel, SDK, FFM diagnostics and
the full suite must pass on the exact candidate. No universal macOS support or
compiler acceptance is inferred from these checks; no hostile case is removed.

PR static checks verify that the checkout has exactly two parents and that its
second parent matches the event's PR head. They compare against the first
parent of that tested merge, so stale event-base metadata cannot lint incoming
main changes as PR changes. Main pushes compare against the push's `before`
SHA and test the exact integrated commit.

CI tooling itself must have zero lint warnings/errors. For other changed
Clojure/EDN files, CI runs the same pinned clj-kondo configuration with caches
disabled against both the immutable base bytes and current files. A warning
is inherited only if Git maps its entire source span to unchanged, identical
lines and the full diagnostic (including type, message, columns and mapped
location) already exists there. Matching consumes each baseline occurrence
once. New warnings, warnings on modified/unknown spans and every current error
block the check, including newly unused definitions on otherwise unchanged
lines. New files receive strict lint; changed `.clj-kondo` configuration disables
baseline allowances. Malformed output, inconsistent exit/count receipts and
tool failures fail closed. No linter is disabled or warning severity lowered.
Both complete diagnostic sets, base source bytes, Git hunks, process receipts
and the classification report remain in the CI artifact. Existing warnings
remain visible debt; a passing comparison does not claim the file is lint-clean.

Bootstrap manifest fragments are loaded into `gravity.bootstrap`, rather than
as independent namespaces. For a changed fragment, lint reads the immutable
base and current manifest closures without evaluation, restores that namespace
before each resource as the loader does, and sends the assembled text to
clj-kondo on standard input. Each resource must also parse independently;
balanced text across two broken resources is rejected. The loader SHA and the
entrypoint's exact loading recipe are checked before assembly. Unsupported
recipes, missing resources, symbolic links, malformed manifests and unmappable
diagnostic spans fail closed. LF and CRLF preserve row/column mapping; lone CR
input is rejected rather than mapped approximately. This view models static
namespace context; it does not execute Bootstrap or establish compiler acceptance.

The authenticated artifact pipeline facade has a second explicit context: its
namespace header followed by its five ordered `load` resources. The entrypoint
namespace and exact load list are checked, every fragment parses separately,
and namespace/load changes inside fragments fail closed. Both base and current
closures use this non-evaluating view and the same diagnostic source mapping.
Unknown fragments are rejected rather than linted without their owning context.
Real-linter controls exercise the actual facade, introduced unknown symbols,
changed dependencies, missing resources and malformed or unsupported recipes.

Every diagnostic in the changed resources is mapped back to its original file,
rows and columns before the same warning comparison. Every selected error
still blocks. Complete context diagnostics remain visible in artifacts;
diagnostics solely in unchanged context dependencies are outside this changed
file selection, except syntax failures, which invalidate the context. Changing
the entrypoint or manifest selects the whole context. Source bytes, input
hashes, assembly hash, source map and redirected-input command receipts are
retained for both revisions. Ordinary files are linted in separate invocations
to prevent unrelated namespace state from corrupting diagnostic filenames.

The C2 facade's private `definterposable` macro has one reviewed static
`analyze-call` model. It retains both wrapper branches, operation keys, dynamic
bindings, arguments and original body nodes, so body diagnostics and generated
function arities remain checked at their source locations. CI never evaluates
the production macro or its body. Before enabling the model, CI parses both
immutable base and current source, verifies the exact macro-definition SHA and
required wrapper declarations, and rejects unsupported definitions/invocations.
The explicit hook configuration is identical for both revisions, with caches
disabled. Root project configuration remains included. Hook/config/recipe
changes invalidate inherited-warning allowances. Complete source observations,
model hashes, configuration and process receipts remain in the static artifact.
This model does not suppress errors or exempt the surrounding Bootstrap context.

Cache tests retain their owner and permission checks: Java temporary
files use a runner-owned private directory, rather than Linux's root-owned
shared `/tmp`. No source security or runtime pin is relaxed for CI.

No self-hosted runners, secrets, paid infrastructure, branch protection or
access settings are created. Fork PR code receives a read-only token, no
persisted checkout credentials and no release permission. No
`pull_request_target` job runs PR source.

Jobs and each child command have wall-clock bounds and explicit JVM heaps.
Timeouts and launch errors fail. Command output and EDN receipts survive other
command failures; artifacts record commit/tree, run URL, toolchain and checksums.
Runner termination may leave only GitHub's own job/step logs and partial
receipts; missing or timed-out output is never acceptance evidence.

Manual source snapshots use tag `experimental-source-<full-commit-SHA>`, never
the latest production release. Snapshot and publication jobs run only for an
explicit `workflow_dispatch` on `main`; ordinary pull requests, automatic main
pushes and weekly runs do not publish source bundles. The generation job has
read-only permission; only the publication job receives `contents: write`,
consumes the same run's exact-SHA artifact, verifies SHA256SUMS and executes no
checked-out source. Publication does not overwrite existing releases on rerun.
No signing credential or OIDC authority is provisioned. Checksums and unsigned
provenance establish content identity, not trusted compiler lineage. Notes
explicitly exclude seedless, complete-language/runtime, native binary,
production, compiler reproducibility, signature, SBOM and independent
release-acceptance claims. Artifacts expire after 14 days; published prerelease
assets persist under GitHub release policy.

## Outputs and acceptance

The strict leaf catalog preserves all 54 primary component records and their
48 bootstrap-free classifications. Four auxiliary helper tests have explicit
production-source, namespace and owner associations. Their actual transitive
requires include the native C3 fixture dependency and remain compiler-free.
Unknown files, metadata drift and missing ownership still fail closed. These
hosted helper tests grant no Reader authentication or compiler execution credit.

The broader ownership inventory exposed the existing unregistered
`bootstrap/clojure/src/gravity/macro_run_storage.clj` production helper. Its
explicit SH-05 registration joins the real early consumer and existing
standalone test. All original 202 owner bindings remain, with one added helper
binding. The original 54 paired components still account for 108 direct paths;
the separate production helper makes 109. No helper exclusion, fabricated
top-level test or 55th primary component is used. CI runs the strict complete
inventory and its negative controls.

The Native owner's reported genuine A baseline failed naturally at
`C6-VERIFY/:actual-selected-current-reader-products`. The corrected C3
Reader-provenance join and public v2 interface are local, unpublished work;
CI must not use them before review/integration. PR483 repaired cold Macro
helper loading; PR482 covers Reader decimal/history semantics. Cold require
and existing Reader positive/negative tests retain those regressions.

Before enabling required checks, observe stable real GitHub runs. Proposed
initial names are `static`, `projections`, `bootstrap`,
`fresh-leaf-regressions / portable`, and `fresh-leaf-regressions / darwin`.
Include `darwin-unsupported-host` after its stable real runs are observed.
Do not add a green `source-Core-readiness` substitute: the former job had no
genuine compiler execution and is intentionally absent. Keep the actual
public `gravity self-host verify` route fail-closed until its release proof is
complete. Main tests check the push SHA, not a cherry-picked candidate or
synthetic source fixture.

Existing finite task authorities remain `specs/build-workflow/tasks.md`
(`build-implementation-tasks`) and `specs/release-workflow/tasks.md`
(`release-implementation-tasks`). Relevant Build outcomes are typed graph
execution, dependency invalidation, package/provenance verification, and
reproducibility/target matrix. Release outcomes are exact component conformance,
stage equivalence, actual self-hosted execution, signed provenance/SBOM
distributions and independent candidate acceptance. CI provides partial tooling
interfaces, closes none of these tasks and creates no new Validation ledger.
Canonical requirements remain owned by the reconciliation owner; dependency
interfaces remain owned by the SH/dependency owner.

Exact Build task IDs under `build/remaining-tasks-and-evidence/` are
`execute-typed-graph-under-declared-build-effects`,
`implement-dependency-aware-caching-and-invalidation`,
`produce-and-verify-packageprovenancesigning-records`, and
`close-reproducibility-and-target-matrix`. Exact Release suffixes under
`release/remaining-tasks-and-evidence/` are
`bind-each-claimed-component-to-exact-implementation-and-conformance`,
`complete-stage-compatibilityequivalence-across-the-claimed-compiler`,
`produce-actual-self-hosted-compilerruntime-execution`,
`build-reproducible-signed-distributions-with-provenancesbom`, and
`obtain-independent-acceptance-for-the-exact-release-candidate`.
