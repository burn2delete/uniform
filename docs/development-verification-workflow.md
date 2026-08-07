# Stage 0 Development Verification and Stage1 SH-01 Bridge

Status: supplemental rollout guide; outside the 240-document inventory

This guide is an implementation bridge for the Stage 0 development runner, the
bounded Stage1 SH-01 unit handoff, and the cheap Stage2 authority-admission
unit. It
does not replace or amend `docs/development-verification.md`, the phase
README files, or the D0-D9, BOOT, TEST, and package contracts. Those documents
remain the source of truth for verification, safety, provenance, authority,
and release claims. The bridge describes how to make the early development
loop faster while keeping those claims explicitly bounded.

## Purpose

The runner starts at the executable Clojure stage0 seed and selects the
smallest dependency-closed set of checks for a change. Cheap checks can run in
parallel, focused checks can reuse exact cache identities, and the serialized
`heavy-candidate` lane can run a fresh expensive command. Every current
receipt is development evidence only: it is non-authoritative until the
normative artifact checks and an explicit authority-promotion decision are
implemented.

The first Stage1 handoff is deliberately smaller than a self-hosting slice
run. `clojure -M:sh01-test` requires and runs only the SH-01 impact-planner and
parallel-runner unit namespaces, in that fixed order and in one JVM. Its final
EDN report states `:authority :non-authoritative`, and any test failure or error
produces a nonzero exit. It validates selection and scheduling mechanics; it
does not execute the namespaces selected by a plan.

The Stage2 unit is similarly bounded. It tests the authority-admission
classifier and lock-held integration transaction in Python, depends on the
Stage1 unit gate, and remains `authority: none`. It does not merge a candidate,
run an authoritative verifier, or grant authority.

The bridge is intentionally subordinate. It records current implementation
behavior and deferred work; it does not define a new language, backend,
self-hosting, seed-retirement, or release contract.

## Implemented and deferred scope

| Area | Implemented in the current bridge | Deferred until the governing contracts and artifacts are wired |
| --- | --- | --- |
| Selection | Changed-path matching, lane filtering, explicit-check selection, dependency/downstream closure, fail-closed unmatched/out-of-lane diagnostics, and a fresh SH-01 planner/runner unit preflight | SH-01 selected-namespace execution, SH-07 proof integration, and later-stage graph migration |
| Receipts | JSON plan and execution receipts with command, input, dependency, lock, mutation, timeout, and non-authoritative status | Promotion to an authority receipt or stage/release decision |
| Lanes | `preflight`, `focused`, and fresh serialized `heavy-candidate`; all current results remain non-authoritative | Authority promotion and any destination lane that can publish authority |
| Freshness | `fresh: true` checks always execute; the SH-01 unit preflight is fresh, and heavy candidates use the shared lock | Reusable JVM evidence, memory admission, and host-wide resource reservation |
| Input/cache safety | Root-bound no-follow descriptor hashing, coherent fstat snapshots, complete redacted runtime/environment identity, conservative invalidation, and no cache write after mutation | Explicit output-artifact validation and authority promotion |
| Mutation monitoring | Kernel vnode watches with final event drain on supported hosts; unsupported polling fallback is non-cacheable; glob roots and existing subtree directories are watched | Cross-platform FSEvents/inotify parity and a future artifact event protocol |
| Lock safety | Shared safe open (`O_NOFOLLOW|O_CREAT|O_RDWR`, mode `0600`) with regular-file, owner, link-count, and parent-path checks; resource locks are direct children of trusted sticky `/private/tmp`, while cache locks hash the canonical resolved root/cache identity under `/private/tmp` | Bounded eviction and cache lifecycle policy |
| Process safety | Every manifest check binds `daemonization: forbidden`; commands run in new process groups, ordinary descendants are cleaned before lock release, and one bounded host-wide `ps eww` environment census at command termination covers saved same-marker processes | Strict containment of arbitrary cross-session daemonization (including double-fork/`setsid`) is deferred to an OS job/container; private output isolation and atomic artifact publication are also deferred |
| Stage rollout | Stage0 selection, receipts, cache/lock safety, targeted verification commands, the Stage1 `stage1-sh01-unit` preflight, and the fresh cheap `stage2-authority-admission-unit` | Authority-affecting merge execution outside the lock-held wrapper, shared heavy-verifier identity, selected-namespace batching/chunking, SH-07/C7 integration, and later-stage claims |

The deferred column is a boundary, not an implied implementation. A passing
command, warm cache hit, benchmark, or heavy-candidate receipt must not be
described as a proof, authority artifact, bootstrap claim, or release result.

## Stage 0 lane model

- `preflight` contains cheap contract, documentation, orchestrator, and fresh
  Stage1 SH-01 planner/runner and Stage2 authority-admission unit checks.
- `focused` contains small reader, hosted, selective, and project-structure
  extraction checks. Exact cache identities may be reused only after input,
  command, dependency, and environment revalidation. The cheap
  `stage0-project-structure-runner-unit` prerequisite executes the runner's
  own synthetic/failure/lifecycle tests in one bounded JVM before the
  `stage0-project-structure-extraction` check runs the three extracted leaf
  test namespaces followed by four qualified `gravity.bootstrap-test` vars in
  one fresh JVM through the bounded `:project-structure-test` alias with
  `-J-Xmx512m`; it is non-authoritative and owns only its declared source
  leaves and contracts.
- `heavy-candidate` contains fresh, serialized, expensive checks such as the
  Stage0 Clojure suite. Its `authority` field is `none`; a command pass remains
  `fresh-command-pass-non-authoritative`.

The lane name describes scheduling cost and candidate freshness, not authority.
The existing `:test` Clojure alias remains the broad self-hosting runner. The
Stage0 manifest uses `:stage0-test` so a development check cannot accidentally
launch that larger target. The Stage1 preflight uses `:sh01-test`; it does not
delegate to `:test` and therefore cannot widen into the broader suite.

The Stage2 admission unit uses the exact Python test module
`tools.tests.test_stage2_authority_admission`. It is fresh and cheap, depends
on `stage1-sh01-unit`, and binds only
`tools/stage2_authority_admission.py`, the shared SH-07 fingerprint-policy
helper, and its test file. Its pass is a unit signal, not permission to merge
or proof authority.

## Stage1 SH-01 handoff and measured boundary

The bounded alias ran 50 tests and 290 assertions in 1.98 seconds in the clean
integration worktree and 2.57 seconds in the active coordinator worktree,
including executor shutdown. Those are absolute observations of this
revision's unit-gate cost, not a cold-start
comparison or a claim that the parallel runner uses fewer processes. The
parallel runner still launches one JVM per selected namespace.

The scheduler's current bounded benefits are narrower and explicit:

- `--fail-fast` stops submitting queued work after the first observed failure;
  already in-flight jobs are allowed to finish and are still reported.
- Every child timeout is a containment-unproven fatal scheduler stop regardless
  of `--fail-fast`. Queued work and the exclusive phase are skipped, while
  already in-flight jobs drain and remain in the report. `ProcessHandle`
  termination is best-effort cleanup of the observed tree, not proof of strict
  arbitrary-descendant containment.
- `--output-limit-bytes` and `--output-limit-chars` bound retained stdout and
  stderr per child while the streams are fully drained. Both default to
  1,048,576 for the current runner. Capture uses stateful UTF-8 decoding across
  read boundaries and accounts for bounded wire bytes and decoded characters.
- A stream-capture error, interruption, or drain timeout makes the child result
  nonzero and fatally stops queued and exclusive work even without
  `--fail-fast`; an incomplete capture cannot be reported as a complete run.
- Results remain non-authoritative. Selection, scheduling, passing unit tests,
  or bounded output do not create proof authority.

Use exact `--namespace` selection for one known leaf and `--changed` with
repeatable `--iteration-slice` for a bounded edit loop. Add `--fail-fast` when
later queued diagnostics would be derivative, and set the output limits when a
child may be noisy. Shared verifier identity and same-JVM selected-namespace
batching/chunking remain deferred; the current process-per-namespace behavior
must be included in any measurement.

## Stage2 authority-admission boundary

Any integration that changes a shared or module-local fingerprint input must
run through the lock-held Stage2 admission wrapper. The wrapper resolves the
base and candidate revisions, computes the prospective tree, classifies changed
paths, acquires `/private/tmp/gravity-sh07-heavy.lock` before mutation, and
keeps the same lock descriptor through the recheck and fast-forward integration:

```bash
# Advisory planning only; this never reserves the lock or authorizes a merge.
python3 tools/stage2_authority_admission.py \
  --cwd <repo> --base <base-oid> --candidate <candidate-oid> \
  --probe-only --human

# Hard admission; the integration command runs while the lock is held.
python3 tools/stage2_authority_admission.py \
  --cwd <repo> --base <base-oid> --candidate <candidate-oid> --exec -- \
  git merge --ff-only <candidate-oid>
```

The advisory probe is useful for planning and diagnostics only. Releasing the
lock before merge creates a time-of-check/time-of-use race, so a probe result
grants no reservation, freshness, merge permission, or proof authority. If the
lock is busy, retry or queue the complete wrapper transaction; do not probe,
release, and merge later. The wrapper is the only admission path for changes
that can invalidate a shared or selected-module fingerprint.

For long-running authority work, an immutable detached alternative is valid:
run the verifier from a clean detached worktree pinned to the exact candidate
commit/tree and bind proof, attestation, and shared/module fingerprints to that
revision. A later merge with different fingerprints requires a new run; an
older detached proof is never carried forward merely because the candidate is
a descendant.

## Stage2 observations and bounded SH-02 loop

The current SH-02 authenticated-envelope development audit measured namespace
require at 5.88 seconds and about 1.40 GiB peak resident memory. The first ten
leaf vars passed warm in one JVM in 13.97 seconds at about 1.46 GiB peak
resident memory. The coordinator integration var exceeded the 60-second audit
bound and was stopped at 66.53 seconds after about 2.47 GiB peak resident
memory; it produced no pass result. These are non-authoritative scheduling
observations, not performance or proof claims.

Run the cheap contract and negative vars first. Then run coordinator vars 11,
12, and 13 together in one JVM behind the shared heavy-run lock, with
`--fail-fast` so derivative vars are skipped after the first failure. Separate
JVMs repeat the shared coordinator proof and its multi-gigabyte construction.
This is a bounded SH-02 test ordering, not a batching-speedup claim. The
normal-only batching ceiling and SH-07 cache-affine strategy remain future
work, and same-JVM selected-namespace batching/chunking is still deferred.

The current C7 observation is 3351.068 seconds (55.85 minutes) at 176,551
source bytes; the user-provided historical observation is 2416.213 seconds at
142,136 source bytes. The contexts and source sizes differ, so they are
incomparable observations and establish no speedup or regression. The backlog
currently records 2411.35 seconds; resolve that against the raw receipt before
replacing any canonical baseline.

## Selection and execution flow

1. Normalize changed paths and validate the manifest before starting a command.
2. Match changes to declared inputs and tools. A path with no owner, or an
   explicit check outside a requested lane, fails closed with owner details;
   an empty filtered plan is never a successful no-op.
3. Close selected checks over downstream checks and dependencies, then emit a
   deterministic topological plan and parallel-ready groups.
4. Compute input identities through root-bound no-follow descriptors. Hash and
   metadata come from one descriptor and must have stable before/after fstat
   identity. Symlinked declared inputs and root escapes are rejected.
5. Bind the command executable, Python/runtime identity, complete child
   environment, manifest overrides, root, and declared inputs into the cache
   key. Environment values are stored only as hashes; receipts never contain
   manifest secrets or ambient values.
6. Run cheap independent checks in bounded parallelism. Run locked/heavy work
   one check at a time. Every manifest command declares `daemonization:
   forbidden` and is placed in a new process group. Any authority-affecting
   integration must additionally run inside the Stage2 lock-held admission
   wrapper; a standalone advisory probe cannot authorize the mutation.
7. Monitor declared files and command identity across execution. On kqueue
   hosts, drain vnode events after the child exits; watch glob parents and all
   existing directories in recursive declared subtrees so create/delete/
   rename events cannot become a transient change-and-restore cache hit.
8. On timeout or a normal parent exit with ordinary surviving descendants,
   terminate the process group before releasing the lock and fail the check.
   A bounded host-wide `ps eww` environment census at command termination
   catches saved same-marker processes across
   groups; strict containment of arbitrary cross-session daemonization is not
   claimed and remains an OS job/container follow-up. A mutation, incoherent
   snapshot, monitor error, or stale post-run identity also fails closed and is
   never cached.
9. Reuse only a matching, non-fresh, non-authoritative cache entry whose
   dependencies were reused with the same keys. Cache writes merge under a
   separately hardened cache lock.

## Identity and cache rules

The cache key includes the manifest schema/name, lane and check declaration,
dependency identities, root, command argv and executable hash, Python
executable/version/platform, complete child environment (names and SHA-256
bindings only), declared input paths and bytes, and lock/fresh/authority
metadata. This is a conservative development key, not a substitute for the
semantic identity required by C16 and the normative verification contract.

Cache reuse is never allowed to hide a changed input, command, dependency,
runtime, environment, manifest, or lane. Unknown, partial, stale, or
non-cacheable entries are treated as misses. A failed or stale command does
not write an entry.

Resource locks are accepted only as direct children of trusted sticky
`/private/tmp` (the `/tmp` spelling is canonicalized there). Cache writers use
a lock name hashed from the canonical resolved repository root and logical
cache path, also under `/private/tmp`; cache data itself remains dirfd-relative
to its opened parent. Both lock paths use no-follow, create, read/write, and
mode `0600`; descriptors are checked for regular type, current-user ownership,
and `nlink == 1` before truncate/write. Symlink and hardlink victims, parent
swaps, and replacement races fail closed without modifying the victim.

## Commands and compatibility

These examples are limited to the Stage0 graph, the bounded Stage1 SH-01 unit
handoff, and the cheap Stage2 authority-admission unit:

```bash
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s tools/tests -p test_verify_development.py -v
python3 -m unittest tools.tests.test_stage2_authority_admission -v
python3 tools/verify_development.py --lane preflight --lane focused --dry-run --explain --human
python3 tools/verify_development.py --check stage1-sh01-unit --dry-run --human
python3 tools/stage2_authority_admission.py --cwd <repo> --base <base-oid> --candidate <candidate-oid> --probe-only --human
python3 tools/verify_development.py --lane heavy-candidate --dry-run --human
clojure -M:sh01-test
clojure -M:stage0-test
clojure -M:project-structure-runner-unit
clojure -J-Xmx512m -M:project-structure-test --exact gravity.bootstrap-test/hosted-hello-runs --exact gravity.bootstrap-test/reader-source-unit-identity-preserves-path-extension-and-options --exact gravity.bootstrap-test/reader-file-policy-rejects-extension-and-malformed-utf8 --exact gravity.bootstrap-test/c2-reader-treats-cr-lf-and-crlf-as-line-terminators --fail-fast
```

The runner should be exercised with targeted checks and dry runs while this
bridge is being integrated. Do not start the heavy/full suite merely to prove
the planner. The Stage1 manifest command is `clojure -M:sh01-test`; it is a
fresh, cheap preflight after `stage0-orchestrator-unit`. The Stage0 heavy
command remains `clojure -M:stage0-test`, and the existing `:test` alias remains
untouched for its broader purpose.

The focused project-structure extraction node is the durable manifest form of
that exact command and depends on the runner-unit node. It runs all 15
extracted leaf tests before the four-var compatibility component in one JVM.
The compatibility component alone was
observed at 4 tests and 190 assertions in 51.05 seconds, avoiding 467 of the
471 bootstrap deftests in the broad suite; the full gate's measured result is
reported separately: 19 tests and 397 assertions in 51.97 seconds with a peak
resident set of 789,315,584 bytes (about 753 MiB). These are development
observations only, not equivalence, authority, or general speedup claims.
The runner-unit prerequisite observed 9 tests and 28 assertions in 0.62
seconds with a peak resident set of 144,703,488 bytes (about 138 MiB); it
intentionally does not load the production leaf or bootstrap test namespaces.
Changes to the six declared
`source_unit`, `source_span`, and `digest` source/test leaves route to this
focused node without selecting `stage0-clojure-suite` or
`stage0-bootstrap-authority`; shared `bootstrap.clj` and `bootstrap_test.clj`
remain owned by the broader checks that declare them.

For a bounded selected-namespace execution outside the manifest unit gate:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-parallel-test-runner --namespace gravity.self-hosting.sh01-impact-test-planner-test --fail-fast --output-limit-bytes 1048576 --output-limit-chars 1048576
```

This command still starts one child JVM for the selected namespace. It is
non-authoritative and is not a replacement for exact/iteration follow-up or a
fresh authoritative verifier where the governing contract requires one.

## Dependencies

Use the existing `docs/development-verification.md` contract and the relevant
phase README/documents first. This bridge is informed by D0, D1, D2, D3, D6,
D8, D9, BOOT, TEST, and package contracts, but it does not redefine them. The
implementation files are `tools/verify_development.py`,
`tools/development_verification_manifest.json`,
`tools/stage2_authority_admission.py`,
`tools/tests/test_stage2_authority_admission.py`, and
`tools/tests/test_verify_development.py`. The Stage1 unit handoff also binds
`deps.edn`, `gravity.self-hosting.sh01-development-test-runner`, the two SH-01
unit namespaces and their planner/runner implementations, the self-hosting
test catalog, and the SH-01 backlog/ownership records they read. The Stage2
unit binds only the admission implementation and its exact Python test module;
the hard wrapper additionally binds the immutable candidate tree and shared or
module fingerprint policy it evaluates.

## Outputs and artifacts

The implemented runner emits a plan/execution receipt, per-check command and
input identities, dependency status, lock result, mutation observations,
timeout cleanup, and cache keys. These are development receipts and
diagnostics. Required output artifact validation, provenance bundles,
coverage-census authority, equivalence/conformance evidence, SBOMs, and
release decisions remain deferred to the normative contracts and later work.
The one-JVM SH-01 alias additionally emits a deterministic namespace-ordered
unit summary with an explicit non-authority marker and pass/fail exit code; it
does not emit a stage-advancement artifact. The Stage2 unit emits ordinary
Python test output only. The admission wrapper may emit a plan or transaction
receipt, but advisory output is explicitly non-authoritative and a hard
transaction is valid only while its lock descriptor remains held through the
integration mutation and post-mutation identity check.

## Conformance and acceptance

- The manifest validates with exactly `preflight`, `focused`, and
  `heavy-candidate` lanes; all manifest checks currently declare
  `authority: none`.
- `stage1-sh01-unit` depends on `stage0-orchestrator-unit`, runs fresh through
  `clojure -M:sh01-test`, and binds only the SH-01 unit/catalog/backlog inputs
  plus `deps.edn`; it does not bind the Stage0 runtime or full bootstrap tree.
- `stage2-authority-admission-unit` depends on `stage1-sh01-unit`, runs fresh
  through `python3 -m unittest tools.tests.test_stage2_authority_admission -v`,
  and binds only the admission implementation, shared fingerprint-policy
  helper, and exact Python test module.
- Authority-affecting integration changes use the lock-held Stage2 wrapper;
  an advisory probe cannot reserve the lock or authorize a later merge, and a
  detached proof is valid only for its immutable candidate tree and matching
  fingerprints.
- A changed path owned only by an excluded lane, or a requested check outside
  the selected lane, produces a failed receipt with owner/lane details.
- A declared input cannot escape the root, follow a symlink, or change during
  descriptor hashing. A transient glob create/use/delete or nested rename is
  observed as stale and is not cached.
- Every manifest check declares `daemonization: forbidden`. A timeout and a
  normal parent exit with an ordinary surviving descendant both clean the
  process group before lock release and fail the check; the bounded terminal
  marker census is evidence for the saved same-marker set. Arbitrary
  cross-session daemonization/strict containment remains deferred to an OS
  job/container.
- Resource and cache lock symlink/hardlink attacks fail closed without changing
  the victim. Cache identity changes when root, runtime, or environment changes.
- Existing docs remain the authority. This bridge never reports a current
  Stage0 command pass as authoritative or complete seed retirement.

The implementation is ready for the next stage only after targeted tests,
manifest JSON parsing, dry-run receipts, and the repository documentation
validator pass. Heavy/full verification, artifact validation, memory admission,
private output publication, bounded eviction, SH-01 selected-namespace/SH-07
proof integration, and later-stage rollout require their own contracts and
evidence. Shared heavy verifier identity, same-JVM selected-namespace
batching/chunking, and strict arbitrary cross-session containment remain
deferred.
