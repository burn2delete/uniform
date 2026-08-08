# Gravity

This workspace contains the Gravity design document set derived from `/Users/mattr/Downloads/Gravity Lisp Design.pdf`.

Gravity is a self-hosting, homoiconic, Clojure-inspired language platform for the whole stack. The core design is one semantic model with many compilation profiles, not one runtime everywhere.

## Document Set

- [docs/README.md](docs/README.md) is the entry point.
- [docs/source-concepts.md](docs/source-concepts.md) summarizes the PDF concepts used to write the documents.
- [docs/document-sequence.md](docs/document-sequence.md) lists the final 240-document sequence.
- [docs/document-inventory.json](docs/document-inventory.json) is the machine-readable inventory.
- [docs/implementation-roadmap.md](docs/implementation-roadmap.md) tracks phase-level implementation tasks and links to per-phase roadmaps.
- [docs/roadmap-capability-audit.md](docs/roadmap-capability-audit.md) records the capability-gated correction to roadmap status.
- [docs/phase-18-binary-distribution-and-seedless-release/README.md](docs/phase-18-binary-distribution-and-seedless-release/README.md) owns the open product release roadmap for a user-facing seedless `gravity` executable.
- [docs/bootstrap/clojure-bootstrap.md](docs/bootstrap/clojure-bootstrap.md) describes the active Clojure stage0 bootstrap.

## Stage0 Bootstrap

Gravity source files are co-canonical as `.qst` and `.gravity`. `.qst`
represents QST theory source; `.gravity` represents Gravity-branded source.
Both extensions are valid indefinitely and neither is a compatibility alias.

Run the first executable Gravity fixture:

```bash
clojure -M:gravity run examples/hello.gravity
clojure -M:gravity run examples/hello.qst
```

Run the first hosted core app fixture with local function calls and core
builtins:

```bash
gravity run examples/core-app.qst
gravity run examples/core-app.gravity
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run examples/core-app.qst
```

Run the same hosted core app through the compiled stage0 instruction plan:

```bash
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.qst
```

Inspect the hosted core app proof artifact:

```bash
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

## Product Release Status

Phase 18 now produces a public bootstrap-hosted `gravity` executable for the
current accepted release surface. `bin/gravity` falls back to the packaged JVM
CLI while the P15 final seed-retirement proof is incomplete, and
`bin/gravity-bootstrap` remains the explicit Clojure audit/recovery path. The
current release proof is fail-closed: it records reproducible rebuild evidence,
provenance, SBOM, signing records, and governance metadata, but it still records
`:clojure-seed-boundary? true` until the compiler path, runtime path, release
compiler path, and public binary are actually outside the Clojure seed
boundary.

Current verification commands:

```bash
gravity test
gravity self-host verify
```

`gravity test` covers only the current public bootstrap subset.
`gravity self-host verify` writes a proof artifact and exits with `P18T04007`
until final self-hosting and seed retirement are proven.

For development, plan the smallest Stage 0 verification graph before running
it. The bounded Stage1 SH-01 handoff runs only the planner and parallel-runner
unit namespaces in one JVM; use exact or related test vars for implementation
work outside that unit boundary:

```bash
python3 tools/verify_development.py --dry-run --explain --human
python3 tools/verify_development.py --lane preflight --lane focused --resume --human
clojure -M:sh01-test
clojure -M:dev-test --exact hosted-hello-runs --exact hosted-core-app-runs-user-functions-and-builtins
clojure -M:project-structure-runner-unit
clojure -J-Xmx512m -M:project-structure-test --exact gravity.bootstrap-test/hosted-hello-runs --exact gravity.bootstrap-test/reader-source-unit-identity-preserves-path-extension-and-options --exact gravity.bootstrap-test/reader-file-policy-rejects-extension-and-malformed-utf8 --exact gravity.bootstrap-test/c2-reader-treats-cr-lf-and-crlf-as-line-terminators --fail-fast
```

`:sh01-test` runs its two namespaces in a fixed order, emits an explicitly
non-authoritative EDN result, and exits nonzero on a failure or error. It does
not run the selected self-hosting implementation namespaces or replace exact,
iteration, authoritative, or release verification.

The manifest's focused `stage0-project-structure-extraction` node is the
one-JVM form of the final command. It runs the three extracted leaf test
namespaces before the four compatibility vars, after the cheap runner-unit
prerequisite, and is fresh and
non-authoritative. Changing one of its six source-unit/source-span/digest
leaves selects this node without selecting the broad `stage0-clojure-suite` or
`stage0-bootstrap-authority`. The compatibility component alone was observed
at 4 tests and 190 assertions in 51.05 seconds; the full gate's measured result
was 19 tests and 397 assertions in 51.97 seconds with a peak resident set of
789,315,584 bytes (about 753 MiB). These observations are not an equivalence or
general speedup claim. The runner-unit prerequisite itself observed 9 tests and
28 assertions in 0.62 seconds with a peak resident set of 144,703,488 bytes
(about 138 MiB), without loading the production leaf or bootstrap test
namespaces.

The SH-01 parallel runner treats every child timeout as containment-unproven
and stops the scheduler even without `--fail-fast`: queued and exclusive jobs
are skipped while work already in flight drains. Its `ProcessHandle` cleanup
is best effort, not strict containment of arbitrary descendants. Stream-capture
failure is also a nonzero fatal stop; stdout and stderr are fully drained with
bounded byte/character retention and stateful UTF-8 accounting.

`--resume` applies only to matching non-authoritative receipts. The
`heavy-candidate` lane is always fresh and serialized, but its command results
remain non-authoritative until deferred output-artifact validation and an
explicit authority promotion step exist. See
`docs/development-verification-workflow.md` for the gate, cache, lock, and
evidence rules.

The Stage2 authority-admission unit is a fresh, cheap preflight after the
Stage1 gate:

```bash
python3 -m unittest tools.tests.test_stage2_authority_admission -v
```

An integration that changes a shared or module fingerprint must use the
lock-held wrapper, keeping `/private/tmp/gravity-sh07-heavy.lock` held through
the recheck and fast-forward mutation. An advisory probe grants no reservation
or authority and cannot be used to justify a later merge. For long-running
authority work, use an immutable detached worktree pinned to the candidate
commit/tree and bind the proof to that exact revision; a changed fingerprint
requires a new proof.

The current SH-02 development audit measured namespace require at 5.88 seconds
and about 1.40 GiB peak resident memory, and the first ten leaf vars warm in one
JVM at 13.97 seconds and about 1.46 GiB. The coordinator var exceeded the
60-second bound and was stopped at 66.53 seconds after about 2.47 GiB; these
are scheduling observations, not proof or speed claims. Run cheap exact vars
first, then coordinator vars 11-13 together behind the heavy lock with
`--fail-fast`; separate JVMs repeat the shared proof. Normal-only batching and
SH-07 cache-affine scheduling remain future work.

The current C7 observation is 3351.068 seconds (55.85 minutes) at 176,551
source bytes; a user-provided historical observation is 2416.213 seconds at
142,136 bytes. Different source/shared contexts make these incomparable, so no
speedup or regression is claimed. The backlog records 2411.35 seconds; resolve
that against the raw receipt before replacing a canonical baseline.

The fixed Stage3 development graph is now explicit and non-authoritative. Its
minimum route is runner-unit, source-control-form-arity, coverage/source
binding and fragment preflight, source-plan, the three pure SH08 semantic
batches, the three authenticated boundaries (primitive bool, recursive
integer+string, and higher-order parity+auth), public C7, and finally a fresh
proof candidate. Every C7 node is a capacity-one heavy candidate using the
command-owned canonical `/private/tmp/gravity-sh07-heavy.lock`; no generic
namespace or batching path is admitted. Structural/source/public commands pin
`-J-Xmx2g`; semantic/authentication/proof commands pin `-J-Xmx8g`, and the
manifest validates those declarations against the fixed wrapper batches and
the centralized Stage3 runtime/shared-input contract. The public check uses a
timeout of at least 900 seconds and observed wall/RSS receipt evidence.

The proof candidate is `automatic: false`: a changed C7 source stops after the
public check, while explicit `--check` or `--all` can request the fresh
no-resume proof. Combining the two same-namespace authentication siblings
reduces the old eight cold semantic/authentication JVM boundaries to six. That
is a scheduling observation only; it is not a measured speedup claim.

The successful proof candidate on `206e89f` is retained as stale-after-tool-
integration evidence, not authority or a speed claim: wrapper/proof elapsed
time was 3998.709/3993.553 seconds, observed monitor peak was about 4.74 GiB,
source was 210,220 bytes with SHA
`sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4`,
artifact SHA prefix/suffix was `9ee396...6587`, census was `6580b7...0393`,
and stdout was `730071...5268`. The
source-bound-derived result requires a separate reviewed attestation and does
not support exact-authentic-coverage, aggregate, or release claims. Because
the run used the pre-Stage2 wrapper, the queued tool/dependency fingerprint
changes invalidate it; exactly one fresh no-resume proof candidate is expected
after integration and freeze.

The fixed Stage4 C8/SH09 graph is layered after the narrow Stage3 runner-unit
prerequisite: C8 source structure (proof-contract, control-form arity,
contracts/policy, limitations), one six-selector SH-09 adapter batch (five
synthetic checks followed by its authenticated boundary), and public C8. It uses the same
command-owned canonical heavy lock, fresh capacity-one execution, and complete
central runtime identity. Structural/public commands pin `-J-Xmx2g`; synthetic,
authenticated, and proof candidates pin `-J-Xmx8g`. The public timeout is at
least 600 seconds and records sampled wall/RSS telemetry. The source coverage
file is partial and impact-excluded for deferred vars 5--9, so edits fail
closed. The manual-only `c8-authority` proof candidate is fresh/no-resume,
`authority: none`, and `attestation_required: true`; it is never inferred from
an exit-0 candidate. Historical frozen `eefb20d` evidence is 80,761 bytes with source /
contract 4 tests/96 assertions, synthetic 5/82, and public 108.627 seconds at
2 GiB with about 2.84 GB observed peak RSS. The prior `f3729a5` proof is stale
historical evidence only; no new proof, speedup, equivalence, or authority
claim is made by this graph.

The fixed Stage5 C9 ownership graph is similarly bounded and non-authoritative.
It depends only on the cheap `stage3-runner-unit` prerequisite, then routes
C9 source structure (`-J-Xmx2g`) to the four-selector SH-10 kernel batch
(`-J-Xmx2g`) and the merged five-selector C8-to-C9 adapter batch
(`-J-Xmx8g`), with the kernel branch continuing to the public C9 selector
(`-J-Xmx2g`). The measured kernel boundary was 4 tests/424 assertions in
6.42 seconds at an observed 1,039,777,792-byte peak; the adapter was 5/51 in
68.073 seconds at an observed 4,164,911,104-byte peak. The public node uses a
600-second timeout and records wall/RSS telemetry. C9 source changes select
both branches; kernel fixture/test changes select source/kernel/public only;
within the Stage5 graph, adapter/helper/C8 changes select source/adapter only;
upstream Stage4 routing remains independent. The old broad
Stage0 heavy ownership is impact-excluded for these C9 paths.

The manual `stage5-c9-proof-candidate` is a fresh, no-resume, new-state
`c9-authority` candidate for `c9-ownership` with `-J-Xmx8g`, a 21,600-second
timeout, `automatic: false`, `authority: none`, and
`attestation_required: true`. The b6e80f1 result is planning evidence only
(505.045 seconds; artifact `sha256:56aa7b6c...b2de`; census
`sha256:b28f186a...1a45`) and is invalidated by current source, contract, tool,
and shared-input drift.
No C8 proof is rerun for C9-only work, and no candidate exit status promotes
authority.

The fixed Stage6 C10 safety graph applies the same bounded pattern without
replaying Stage3--5 production lanes. A five-selector source-only gate
(`-J-Xmx2g`) branches to the seven-selector numeric-safety kernel
(`-J-Xmx2g`) and a five-selector C9-to-C10 adapter (`-J-Xmx8g`); the kernel
continues to the exact public C10 selector (`-J-Xmx2g`). The adapter keeps its
four pure checks and the single authenticated `.gravity` boundary in one JVM,
so namespace-local C9/C10 plans and the process cache are reused and `.qst`
remains byte-parity evidence only. C10 source edits select all four automatic
Stage6 nodes, kernel edits select source/kernel/public, and adapter edits select
source/adapter. Legacy broad owners are impact-excluded for these paths.

The manual `stage6-c10-proof-candidate` is fixed to `c10-authority` /
`c10-safety`, fresh, no-resume, new-state, `automatic: false`,
`authority: none`, and `attestation_required: true`. It joins the public and
adapter branches only when explicitly requested. The current C10 source is
112,712 bytes (`sha256:2d334872...4968`); the combined adapter lane passed
5 tests/147 assertions in 69.470 seconds, with its authenticated boundary last.
These are non-authoritative development receipts, not proof or speedup claims.

The fixed Stage7 C11/SH12 graph owns the frozen C11 MIR source without falling
back to the legacy broad Stage0 checks. A 512 MiB three-selector source gate
runs exact source binding, control-form arity, and export-definition checks.
It branches to one 8 GiB six-selector SH12 adapter batch (verification-envelope
helper first, authenticated `.gravity` boundary last) and one 2 GiB public
batch that checks semantic pins before CLI acceptance. The frozen C11 source is 253,588 bytes with SHA
`sha256:34f0e797...4136`; its measured plan/functions hashes are
`sha256:974d3949...fb39` and `sha256:ece068d2...89a4`, while the builder and
verifier hashes remain unchanged. The final adapter lane passed 5 tests/182
assertions in 86.284 seconds with a 1,569,554,432-byte observed peak. These are
non-authoritative development and calibration receipts. The final two-selector
public batch passed 2/39 in 319.494 seconds with a sampled 3,166,142,464-byte
process-tree peak.

The manual `stage7-c11-proof-candidate` is fixed to `c11-authority` / `c11-mir`,
fresh, no-resume, new-state, `automatic: false`, `authority: none`, and
`attestation_required: true`. It joins the adapter and public branches only
when explicitly requested. No current C11 proof candidate or reviewed
attestation is claimed by this graph.

Every Stage 0 manifest check explicitly sets `daemonization: forbidden`.
Commands run in a new process group; ordinary descendants are cleaned before a
resource lock is released, with one bounded host-wide `ps eww` environment
census at command termination for saved
same-run processes. Strict containment of arbitrary cross-session daemonization
remains deferred to an OS job/container and is not a current Stage 0 claim.
Resource locks are direct children of trusted sticky `/private/tmp`; cache
writers serialize with a canonical root/cache identity hashed to a separate
`/private/tmp` lock while keeping cache data access dirfd-relative.

Inspect the hosted core compiled safety proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-safety bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled profile proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-profile bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled performance proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-performance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled math proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled compiler proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled backend proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled runtime proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled domain proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Validate the full Stage 0 Clojure bootstrap suite:

```bash
clojure -M:stage0-test
```

The `:stage0-test` alias invokes `gravity.bootstrap-test` directly. The `:test`
alias remains unchanged and invokes the broader self-hosting test runner used
by the coordinator branch.

Inspect the first module artifact:

```bash
clojure -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity
```

Inspect the first macro expansion artifact:

```bash
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

Inspect the first core artifact:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
```

Inspect the first typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
```

Inspect the first capability and supply-chain safety artifact:

```bash
clojure -M:gravity capability-supply-chain bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity
```

Inspect the first final safety conformance artifact:

```bash
clojure -M:gravity safety-conformance bootstrap/clojure/fixtures/accepted/safety-conformance.gravity
```

Inspect the first profile manifest artifact:

```bash
clojure -M:gravity profile-manifest bootstrap/clojure/fixtures/accepted/profile-manifest.gravity
```

Inspect the first profile-set artifact:

```bash
clojure -M:gravity profile-set bootstrap/clojure/fixtures/accepted/profile-set-core.gravity
```

Inspect the first constrained profile-validation artifact:

```bash
clojure -M:gravity profile-validation bootstrap/clojure/fixtures/accepted/profile-validation-hardware.gravity
```

Inspect the first distributed/AI profile-validation artifact:

```bash
clojure -M:gravity profile-distributed-ai bootstrap/clojure/fixtures/accepted/profile-distributed-ai-distributed.gravity
```

Inspect the first profile compatibility artifact:

```bash
clojure -M:gravity profile-compatibility bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity
```

Inspect the Phase 03 profile compliance suite artifact:

```bash
clojure -M:gravity profile-compliance bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity
```

Inspect the first performance claim artifact:

```bash
clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity
```

Inspect the first zero-cost abstraction artifact:

```bash
clojure -M:gravity zero-cost bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity
```

Inspect the first specialization artifact:

```bash
clojure -M:gravity specialization bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity
```

Inspect the first layout optimization artifact:

```bash
clojure -M:gravity layout bootstrap/clojure/fixtures/accepted/layout-optimization.gravity
```

Inspect the first performance governance artifact:

```bash
clojure -M:gravity performance-governance bootstrap/clojure/fixtures/accepted/performance-governance.gravity
```

Inspect the first realtime governance artifact:

```bash
clojure -M:gravity realtime-governance bootstrap/clojure/fixtures/accepted/realtime-governance.gravity
```

Inspect the first numeric mode artifact:

```bash
clojure -M:gravity numeric-modes bootstrap/clojure/fixtures/accepted/math-numeric-modes.gravity
```

Inspect the first EFIR artifact:

```bash
clojure -M:gravity efir bootstrap/clojure/fixtures/accepted/math-efir.gravity
```

Inspect the first EML artifact:

```bash
clojure -M:gravity eml bootstrap/clojure/fixtures/accepted/math-eml.gravity
```

Inspect the first certified approximation artifact:

```bash
clojure -M:gravity approximation bootstrap/clojure/fixtures/accepted/math-approximation.gravity
```

Inspect the first interval and symbolic proof artifact:

```bash
clojure -M:gravity math-proof bootstrap/clojure/fixtures/accepted/math-proof.gravity
```

Inspect the first math optimization and conformance artifact:

```bash
clojure -M:gravity math-conformance bootstrap/clojure/fixtures/accepted/math-conformance.gravity
```

Inspect the hosted core compiled app math proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app compiler proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app backend proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app runtime proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app domain proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first compiler pass-contract manifest artifact:

```bash
clojure -M:gravity compiler-passes bootstrap/clojure/fixtures/accepted/compiler-passes.gravity
```

Inspect the first checked-core pipeline artifact:

```bash
clojure -M:gravity checked-core bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity
```

Inspect the first MIR artifact:

```bash
clojure -M:gravity mir bootstrap/clojure/fixtures/accepted/compiler-mir.gravity
```

Inspect the first domain IR artifact:

```bash
clojure -M:gravity domain-ir bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity
```

Inspect the first optimization/lowering artifact:

```bash
clojure -M:gravity optimize-lower bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity
```

Inspect the first compiler verification artifact:

```bash
clojure -M:gravity compiler-verify bootstrap/clojure/fixtures/accepted/compiler-verification.gravity
```

Inspect the first C1 compiler architecture document coverage artifact:

```bash
clojure -M:gravity compiler-c1-architecture bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity
```

Inspect the first C2 reader document coverage artifact:

```bash
clojure -M:gravity compiler-c2-reader bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity
```

Inspect the first C3 syntax object document coverage artifact:

```bash
clojure -M:gravity compiler-c3-syntax bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity
```

Inspect the first C4 macro expansion document coverage artifact:

```bash
clojure -M:gravity compiler-c4-macro bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity
```

Inspect the first C5 name resolution document coverage artifact:

```bash
clojure -M:gravity compiler-c5-resolution bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity
```

Inspect the first C6 core lowering document coverage artifact:

```bash
clojure -M:gravity compiler-c6-lowering bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity
```

Inspect the first C7 type checker document coverage artifact:

```bash
clojure -M:gravity compiler-c7-type-check bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity
```

Inspect the first C8 effect checker document coverage artifact:

```bash
clojure -M:gravity compiler-c8-effect-check bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity
```

Inspect the first C9 ownership checker document coverage artifact:

```bash
clojure -M:gravity compiler-c9-ownership-check bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity
```

Inspect the first C10 safety analysis document coverage artifact:

```bash
clojure -M:gravity compiler-c10-safety-analysis bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity
```

Inspect the first C11 MIR specification document coverage artifact:

```bash
clojure -M:gravity compiler-c11-mir-spec bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity
```

Inspect the first C12 domain IR architecture document coverage artifact:

```bash
clojure -M:gravity compiler-c12-domain-ir bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity
```

Inspect the first C13 MIR optimization document coverage artifact:

```bash
clojure -M:gravity compiler-c13-optimization bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity
```

Inspect the first C14 target lowering document coverage artifact:

```bash
clojure -M:gravity compiler-c14-lowering bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity
```

Inspect the first C15 compiler diagnostics document coverage artifact:

```bash
clojure -M:gravity compiler-c15-diagnostics bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity
```

Inspect the first C16 incremental compilation document coverage artifact:

```bash
clojure -M:gravity compiler-c16-incremental bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity
```

Inspect the first C17 compiler plugin/pass API document coverage artifact:

```bash
clojure -M:gravity compiler-c17-plugin bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity
```

Inspect the first C18 compiler verification/pass-correctness document coverage
artifact:

```bash
clojure -M:gravity compiler-c18-verification bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity
```

Inspect the first P07 backend interface/conformance harness artifact:

```bash
clojure -M:gravity backend-interface bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Inspect the first P07 native C/LLVM/MLIR lowering artifact:

```bash
clojure -M:gravity native-lowering bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 hosted Wasm/JVM/JS-TS lowering artifact:

```bash
clojure -M:gravity hosted-lowering bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 specialized GPU/HDL/workflow/query/mobile lowering
artifact:

```bash
clojure -M:gravity specialized-lowering bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 artifact emission/provenance artifact:

```bash
clojure -M:gravity artifact-emission bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Inspect the first P07 backend test matrix artifact:

```bash
clojure -M:gravity backend-test-matrix bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Inspect the first P07 B1 backend interface document coverage artifact:

```bash
clojure -M:gravity backend-b1-document bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Inspect the first P07 B2 C backend document coverage artifact:

```bash
clojure -M:gravity backend-b2-c-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 B3 LLVM backend document coverage artifact:

```bash
clojure -M:gravity backend-b3-llvm-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 B4 Wasm backend document coverage artifact:

```bash
clojure -M:gravity backend-b4-wasm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 B5 JVM backend document coverage artifact:

```bash
clojure -M:gravity backend-b5-jvm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 B6 JavaScript / TypeScript backend document coverage artifact:

```bash
clojure -M:gravity backend-b6-js-ts-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 B7 MLIR backend document coverage artifact:

```bash
clojure -M:gravity backend-b7-mlir-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 B8 GPU backend document coverage artifact:

```bash
clojure -M:gravity backend-b8-gpu-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B9 HDL backend document coverage artifact:

```bash
clojure -M:gravity backend-b9-hdl-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B10 workflow graph backend document coverage artifact:

```bash
clojure -M:gravity backend-b10-workflow-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B11 query/relational backend document coverage artifact:

```bash
clojure -M:gravity backend-b11-query-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B12 mobile backend document coverage artifact:

```bash
clojure -M:gravity backend-b12-mobile-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B13 artifact emission document coverage artifact:

```bash
clojure -M:gravity backend-b13-artifact-document bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Inspect the first P07 B14 backend conformance document coverage artifact:

```bash
clojure -M:gravity backend-b14-conformance-document bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Inspect the first P08 runtime selection and no-runtime proof artifact:

```bash
clojure -M:gravity runtime-selection bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Inspect the first P08 minimal native and memory runtime artifact:

```bash
clojure -M:gravity runtime-minimal-native bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Inspect the first P08 managed host runtime artifact:

```bash
clojure -M:gravity runtime-managed bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity
```

Inspect the first P08 concurrency, distributed, and replay runtime artifact:

```bash
clojure -M:gravity runtime-concurrency bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Inspect the first P08 AI, REPL, FFI, and capability runtime artifact:

```bash
clojure -M:gravity runtime-ai-ffi bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 runtime observability artifact:

```bash
clojure -M:gravity runtime-observability bootstrap/clojure/fixtures/accepted/runtime-observability.gravity
```

Inspect the first P08 R1 runtime architecture document coverage artifact:

```bash
clojure -M:gravity runtime-r1-document bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Inspect the first P08 R2 no-runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r2-document bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Inspect the first P08 R3 minimal native document coverage artifact:

```bash
clojure -M:gravity runtime-r3-document bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Inspect the first P08 R4 managed runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r4-document bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity
```

Inspect the first P08 R5 memory runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r5-document bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Inspect the first P08 R6 concurrency runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r6-document bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Inspect the first P08 R7 distributed runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r7-document bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Inspect the first P08 R8 AI runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r8-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R9 REPL runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r9-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R10 FFI runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r10-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R11 runtime capability enforcement document coverage artifact:

```bash
clojure -M:gravity runtime-r11-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R12 runtime observability document coverage artifact:

```bash
clojure -M:gravity runtime-r12-document bootstrap/clojure/fixtures/accepted/runtime-observability.gravity
```

Inspect the first Phase 09 domain-specific coverage artifact:

```bash
clojure -M:gravity domain-coverage bootstrap/clojure/fixtures/accepted/domain-coverage.gravity
```

Inspect the first Phase 10 schema/data/interop artifact:

```bash
clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity
```

Inspect the hosted core compiled app schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 11 AI/agentic artifact:

```bash
clojure -M:gravity ai-agentic bootstrap/clojure/fixtures/accepted/ai-agentic.gravity
```

Inspect the first Phase 12 package/build/artifact artifact:

```bash
clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity
```

Inspect the hosted core compiled Phase 12 package/build/artifact proof:

```bash
clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 13 tooling/developer-experience artifact:

```bash
clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity
```

Inspect the hosted core compiled Phase 13 tooling/developer-experience proof:

```bash
clojure -M:gravity hosted-core-compiled-tooling bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 14 testing/verification/conformance artifact:

```bash
clojure -M:gravity conformance-system bootstrap/clojure/fixtures/accepted/conformance-system.gravity
```

Inspect the hosted core compiled Phase 14 testing/verification/conformance proof:

```bash
clojure -M:gravity hosted-core-compiled-conformance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 15 bootstrap/self-hosting artifact:

```bash
clojure -M:gravity bootstrap-self-hosting bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity
```

Inspect the first stage1 Gravity bootstrap-source bridge artifact:

```bash
clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src
```

Inspect the first stage1 reader-table execution bridge artifact:

```bash
clojure -M:gravity stage1-reader-execute bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader algorithm bridge artifact:

```bash
clojure -M:gravity stage1-reader-algorithm bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader character pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-character-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader token-classifier pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-classifier-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader token-realizer pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-realizer-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader token-automaton pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-automaton-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader form-builder pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-form-builder-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader executor pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-executor-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader runtime pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader compiled pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-compiled-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader binary pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-binary-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader self-hosted runtime bridge artifact:

```bash
clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader core bootstrap bridge artifact:

```bash
clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader compiler-driver bridge artifact:

```bash
clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader runtime-entrypoint bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader runtime-image bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader verified boot-chain bridge artifact:

```bash
clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader diverse bootstrap verification bridge artifact:

```bash
clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader release attestation seed-retirement bridge artifact:

```bash
clojure -M:gravity stage1-reader-release-attestation-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader formal release governance seed-retirement bridge artifact:

```bash
clojure -M:gravity stage1-reader-formal-release-governance-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the P15-S23 whole-language self-hosting fail-closed gate artifact:

```bash
clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the P15-S23 compiler source inventory artifact:

```bash
clojure -M:gravity p15-s23-compiler-source-inventory bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 compiler pipeline manifest artifact:

```bash
clojure -M:gravity p15-s23-compiler-pipeline-manifest bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 source/syntax serialization proof artifact:

```bash
clojure -M:gravity p15-s23-source-syntax-serialization-proof bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 core lowering and diagnostic preservation artifact:

```bash
clojure -M:gravity p15-s23-core-lowering-diagnostic-preservation bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 runtime manifest and capability enforcement artifact:

```bash
clojure -M:gravity p15-s23-runtime-manifest-capability-enforcement bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 accepted app execution artifact:

```bash
clojure -M:gravity p15-s23-accepted-app-execution bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 rejected app diagnostic artifact:

```bash
clojure -M:gravity p15-s23-rejected-app-diagnostic bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 reproducible rebuild log artifact:

```bash
clojure -M:gravity p15-s23-reproducible-rebuild-log bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage comparison report artifact:

```bash
clojure -M:gravity p15-s23-stage-comparison-report bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 self-hosting conformance report artifact:

```bash
clojure -M:gravity p15-s23-self-hosting-conformance-report bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 bootstrap provenance attestation artifact:

```bash
clojure -M:gravity p15-s23-provenance-attestation bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 trusted-computing-base delta record artifact:

```bash
clojure -M:gravity p15-s23-tcb-delta-record bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 unsafe audit report artifact:

```bash
clojure -M:gravity p15-s23-unsafe-audit-report bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 current-stage whole-language compiler artifact:

```bash
clojure -M:gravity p15-s23-whole-language-compiler-artifact bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 governance and package release record artifact:

```bash
clojure -M:gravity p15-s23-governance-and-package-release-record bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 compiler nucleus transition artifact:

```bash
clojure -M:gravity p15-s23-stage2-compiler-nucleus bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 plan emitter artifact:

```bash
clojure -M:gravity p15-s23-stage2-plan-emitter bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 runtime kernel artifact:

```bash
clojure -M:gravity p15-s23-stage2-runtime-kernel bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 runtime executor artifact:

```bash
clojure -M:gravity p15-s23-stage2-runtime-executor bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 front-end executor artifact:

```bash
clojure -M:gravity p15-s23-stage2-front-end-executor bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 source front-end artifact:

```bash
clojure -M:gravity p15-s23-stage2-source-front-end bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 compiler driver artifact:

```bash
clojure -M:gravity p15-s23-stage2-compiler-driver bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 whole-language compiler stage artifact:

```bash
clojure -M:gravity p15-s23-stage2-whole-language-compiler bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage3 seedless compiler candidate artifact:

```bash
clojure -M:gravity p15-s23-stage3-seedless-compiler-candidate bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage3 equivalence bundle artifact:

```bash
clojure -M:gravity p15-s23-stage3-equivalence-bundle bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage3 self-hosted application execution artifact:

```bash
clojure -M:gravity p15-s23-stage3-self-hosted-application bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 final seed-retirement proof artifact:

```bash
clojure -M:gravity p15-s23-final-seed-retirement-proof bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the first Phase 16 standard-library artifact:

```bash
clojure -M:gravity standard-library bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity
```

Inspect the first Phase 17 governance/evolution artifact:

```bash
clojure -M:gravity governance-evolution bootstrap/clojure/fixtures/accepted/governance-evolution.gravity
```

## Validation

Run:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Expected result:

```text
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders
```

## Generation and Enrichment

- [tools/generate_gravity_docs.py](tools/generate_gravity_docs.py) contains the canonical 240-document inventory and baseline document renderer.
- [tools/enrich_remaining_docs.py](tools/enrich_remaining_docs.py) records the deterministic enrichment pass used for phases that were not completed by workers.
- Do not rerun the baseline generator over edited documents unless you intend to regenerate the full tree and then reapply enrichment.
