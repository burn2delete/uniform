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

The bounded P15-S23 Darwin launcher prerequisite is a separate focused gate:
`stage0-p15-native-launcher-prerequisite` runs the exact direct command
`clojure -J-Xmx1g -M:test --namespace gravity.p15-native-launcher-test` with
Xmx1g, a 600-second timeout, fresh/no-resume execution, and
`authority: none`. It uses the canonical
`/private/tmp/gravity-sh07-heavy.lock` with `exclusive: true`, `capacity: 1`,
and `lock_owner: runner`; the parent verifier owns the lease because this is a
direct Clojure command rather than a command-owned proof wrapper. Its resource
receipt declaration is `observed-peak-process-tree-rss-and-wall-time`.
The node owns the launcher C source, exact test, all five launcher fixtures,
and the partial launcher artifact, with only `deps.edn` and
`bootstrap/clojure/test/gravity/self_hosting_test_runner.clj` as tool inputs.
Each owned source, test, fixture, or artifact path routes to this node plus
`stage0-orchestrator-unit`; legacy Stage0 broad owners impact-exclude those
paths, and no earlier/later proof candidate is selected.

Coordinator evidence for this revision is exactly 8 tests/60 assertions in
5.983s, peak RSS 213,712,896B, with Xmx1g, the canonical lock, and an approved
independent review. The artifact status is `partial`. This remains bounded,
non-authoritative development evidence and makes no public, self-hosted,
release, or strict process-containment claim. Local validation does not rerun
the JVM while coordinator C12 work is active.

The bounded P15-S23 native runtime provider is split into two exact internal
prerequisites. `stage0-p15-native-runtime-provider-contract-prerequisite` is
the fast contract profile. It runs the fixed
`gravity.self-hosting.sh07-iteration-cache-runner` command with the ten
provider-contract vars in source order (artifact preflight first),
`--fail-fast`, `--max-cache-entries 1`, and explicit
`bootstrap/clojure/src` plus `bootstrap/clojure/test` paths. It pins Xmx1g,
a strict 180-second timeout, and a 1,073,741,824-byte floor. It owns the four
shared inputs plus the exact 22 legacy fixtures (26 primary inputs) and reports
10 tests/235 assertions.

`stage0-p15-native-runtime-provider-packet-binding-prerequisite` is the
authenticated packet-binding profile. It uses the same fixed runner shape with
the packet-binding artifact preflight first and the four authenticated vars in
source order, Xmx8g, a strict 1,800-second timeout, and an 8,589,934,592-byte
floor. It owns the four shared inputs, the exact packet binder, and the four
new fixtures (9 primary inputs) and reports 5 tests/70 assertions. Both nodes
depend only on `stage0-orchestrator-unit`; stable ID ordering puts the contract
profile before packet binding whenever shared inputs select both. Explicit
`--check` closes only the selected profile plus the orchestrator, while
`--all` includes both profiles.

Both profiles are fresh/no-resume, automatic, serialized, non-authoritative
gates using `/private/tmp/gravity-sh07-heavy.lock`, `lock_owner: runner`,
`exclusive: true`, `capacity: 1`, and the observed process-tree RSS/wall-time
receipt. The exact executable tool closure is shared: `deps.edn`, the
iteration-cache runner, the self-hosting runner, `bootstrap.clj`, the packet
binder, and its six direct Gravity dependencies. Shared C/Gravity/test/artifact,
the binder, and eager namespace-load helper edits route to both profiles; old
fixtures route only to the contract profile and the four authenticated fixtures
only to packet binding. Existing `bootstrap.clj` and six-helper consumers retain
their prior Stage0/Stage3/Stage4 routes in addition to both provider profiles.
Exact exclusions remove provider-specific fixtures and the new binder from
unrelated broad replay; they do not replace genuine existing helper ownership.
Neither provider profile grants M0, proof, public, self-hosted, release, or
containment authority.

The manifest binds exactly `GRAVITY_P15_NATIVE_RUNTIME_REQUIRED=1`. Without
that marker an ordinary namespace run retains its unsupported-platform/no-claim
behavior; the focused verifier supplies it and fails when the ARM64 macOS
Clang/file toolchain is unavailable. Each profile independently performs a
bounded no-follow, strict-UTF8, single-form EDN read with identity/size checks
and recomputes current hashes; packet binding additionally validates its own
adapter and authenticated fixtures. The artifact's current source-only census
is 15 tests/305 assertions (fast 10/235 plus packet 5/70). The prior ae9f
13-test/303-assertion receipt, timing, RSS, hashes, and attempt history remain
explicitly historical and non-authoritative; no current receipt is fabricated.
This remains a narrow internal provider gate and grants no public native,
self-hosting, release, seedless, or strict process-containment authority.

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

# Hard admission; only this spelling is accepted and the wrapper performs the FF.
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

The wrapper never executes an arbitrary command in the coordinator. Every
shared-heavy `--lock` override must be a direct child of canonical
`/private/tmp`; Darwin's verified `/tmp` system alias is canonicalized there.
Lock content is never written. A free owned stable legacy 0644 inode is
migrated in place to 0600 only after exclusive flock, with the migration
recorded in receipts; a held legacy inode reports busy without mutation.
SH-07 `--list` is Clojure-backed catalog discovery and therefore acquires the
same lock before it starts; it is not an unlocked static listing.
Operational ignored outputs are narrowly limited to ordinary contained
`.cpcache`, declared validation/log, and Python cache files. Ignored classpath
shadows and any symlink, special, or fingerprint-sensitive entry still reject
hard admission.
A successful receipt grants only the lock-held fixed-fast-forward admission
(`integration_admission_granted: true`). It never grants proof authority;
`proof_authority_granted` remains false for success, advisory, and failure.

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

## Fixed Stage3 C7 candidate graph

The Stage3 graph is a fixed, serial development route rather than generic
namespace batching. It begins with the complete
`gravity.self-hosting.stage3-verification-runner-test` unit namespace, then
runs source-control-form-arity, coverage/source binding and fragment
preflight, source-plan, the three pure SH08 semantic batches, the three
authenticated boundaries (primitive bool, recursive integer+string, and
higher-order parity+auth), public C7, and finally the proof candidate. Arity
and fragment are prerequisites of every semantic/authentication node. Each
production node invokes `python3 tools/run_stage3_verification.py` with a
reviewed `--batch` identity from the `:stage3-verification` alias. No generic
`--namespace` or `--exact` selection is used by the production graph.

Every C7 node is a fresh, exclusive, capacity-one heavy candidate using the
command-owned canonical `/private/tmp/gravity-sh07-heavy.lock`. Structural,
source, and public batches fix `-J-Xmx2g`; semantic, authenticated, and proof
batches fix `-J-Xmx8g`, and manifest validation checks equality with the
wrapper's fixed batch command. The public node uses a timeout of at least 900
seconds and retains observed wall/RSS receipt evidence. The final
authority-shaped node is only a fresh no-resume `proof-candidate` with a new
invocation state directory, `automatic: false`, `authority: none`, and
`attestation_required: true`. Changed C7 paths therefore stop at public;
explicit `--check`/`--all` include proof. Reviewed attestation remains a
separate disabled mode and is never inferred from an exit-0 proof candidate.

Combining the two same-namespace authentication sibling pairs removes two
cold semantic/authentication JVM boundaries (eight to six in the graph). This
is a scheduling observation, not a measured speedup claim.

Each command-owned production node fingerprints the centralized
`run_stage3_verification.STAGE3_RUNTIME_DEPENDENCIES` set: `deps.edn`,
`run_stage3_verification.py`, `verify_development.py`,
`run_sh07_authoritative_modules.py`, the Stage3 and iteration-cache Clojure
runners, `gravity/bootstrap.clj`, all five shared Gravity files, and the
`bootstrap/clojure/src/**` tree. The runner-unit is intentionally narrow and
does not claim this production set. Authenticated SH08 nodes bind
`sh08_function_call_test.clj` plus the exact `.gravity` inputs they load (and
paired `.qst` bytes for parity). The proof candidate additionally binds the
authoritative runner and proof contract, so a changed delegate cannot be
hidden behind an unchanged fixed selector.

The source-plan and census test files contain deftests outside their fixed
selector sets. Their paths are impact-excluded and therefore fail closed as
deferred under implicit change-impact selection; the large bootstrap test file
is not claimed by the single public selector. The complete SH08 and fragment
files are covered by the union of their fixed selectors. Explicit `--check` and
`--all` scopes retain the broad Stage0 graph, while a changed C7 implementation
source selects the Stage3 chain and excludes legacy Stage0 heavy checks.

The successful proof candidate on `206e89f` is retained as stale-after-tool-
integration evidence, not authority or a speedup claim. Wrapper/proof elapsed
time was 3998.709/3993.553 seconds, observed monitor peak was about 4.74 GiB,
and source was 210,220 bytes with SHA
`sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4`.
Artifact/census/stdout SHA prefixes/suffixes were `9ee396...6587`,
`6580b7...0393`, and `730071...5268`; the request census was 192
fragments/roots, 18,554 forms, 1,528 bindings, 1,266 locals, and 7,687
resolutions. Core counts were 15,286 nodes, 192 definitions, 3,082 calls,
6,185 references, and zero keywords, with 187 function records, 3,082 call
edges, and 14 recursion components. The source-bound-derived contract still
requires a separate reviewed attestation and does not grant exact, aggregate,
or release authority. Since the run used the pre-Stage2 wrapper, queued
tool/dependency fingerprint changes invalidate it; exactly one fresh no-resume
candidate rerun is required after integration and freeze.

## Fixed Stage4 C8/SH09 candidate graph

The C8/SH09 route is another fixed graph, not a generic namespace or module
passthrough. It begins with the narrow Stage3 runner-unit prerequisite, then
runs `stage4-c8-source-structural`, the combined
`stage4-sh09-adapter`, and `stage4-public-c8` in order. The source batch
deliberately executes proof-contract registration, control-form arity, broader
contracts/policy, and structural limitations. The synthetic adapter batch has
five exact selectors, including the ordered-effect-identity seam from
`eefb20d`; its final selector is the authenticated boundary, keeping all six
same-namespace selectors in one 8 GiB JVM.

The source coverage file is partial: later coverage vars 5--9 are not owned by
this graph, so the file is fingerprinted and impact-excluded. An implicit edit
fails closed as deferred. The public node binds the exact bootstrap/CLI/
diagnostics partial chain, `bin/gravity`, the packaged JVM jar, and the P15-S23
seed-retirement artifact. Every production node inherits the centralized Stage3
runtime identity and uses the command-owned canonical heavy lock with fresh,
exclusive, capacity-one execution. Structural/public nodes pin `-J-Xmx2g`;
synthetic/authenticated nodes pin `-J-Xmx8g`; public timeout is at least 600
seconds and records observed wall/RSS telemetry.

`stage4-c8-proof-candidate` is manual-only (`automatic: false`), fresh/no-
resume, and uses only the fixed `c8-authority`/`c8-effects` policy. It reports
`authority: none`, `proof_candidate: true`, and `attestation_required: true`;
it is not selected by ordinary C8 change impact and cannot promote authority.
The `eefb20d` evidence is 80,761 source bytes
(`sha256:ff072574...`), source/contract 4 tests/96 assertions, synthetic 5/82,
and public 108.627 seconds at `-J-Xmx2g` with an observed peak near 2.84 GB.
The prior `f3729a5` proof (artifact `ed0a4e...`, census `78ea0c...`) remains
stale historical evidence only; no new proof ran during this integration and
no speedup or equivalence claim is made.

## Fixed Stage5 C9 ownership graph

The C9 route is a fixed, non-authoritative graph layered only on the narrow
`stage3-runner-unit` prerequisite. It does not depend on or replay the Stage3
or Stage4 production nodes. Automatic changed-path routing is:

`stage5-c9-source-structural` -> `stage5-c9-kernel` -> `stage5-public-c9`

with a parallel source -> `stage5-sh10-c8-adapter` branch. The source gate
uses the four exact C9 coverage selectors in source order (proof contract,
control-form arity, contracts, limitations), `-J-Xmx2g`, and a 1,200-second
timeout. Its source is pinned to 71,132 bytes and
`sha256:4f26a5ca5fdd7755016f332fc5c795f84a98b83b76cef79806b8021807897fcd`.
Coverage vars 5--9 remain deferred; the partial coverage namespace is
fingerprinted and impact-excluded so an edit fails closed.

The kernel node runs all four SH-10 ownership-transition selectors in one
`-J-Xmx2g` process (4 tests/424 assertions, 6.42 seconds, observed peak
1,039,777,792 bytes). The adapter node runs four synthetic C8-to-C9 selectors
then its authenticated boundary in one `-J-Xmx8g` process (5 tests/51
assertions, 68.073 seconds, observed peak 4,164,911,104 bytes); no skipped
vars or `.qst` carrier was observed. The public C9 node is a fixed selector
with `-J-Xmx2g`, a 600-second timeout, and wall/RSS receipt telemetry.

All production nodes are fresh, exclusive, capacity-one, command-owned users
of `/private/tmp/gravity-sh07-heavy.lock` and inherit the complete centralized
runtime identity. Public inputs include the packaged JVM CLI jar, `bin/gravity`,
the seed-retirement artifact, and the partial bootstrap/CLI/diagnostics chain.
Kernel inputs include accepted/rejected `.gravity` and `.qst` fixture pairs;
the adapter binds its C8/C9/SH-09/SH-08 helper chain and only the accepted
typed-bool `.gravity` fixture loaded by its authenticated boundary.

The `stage5-c9-proof-candidate` is manual-only (`automatic: false`), fresh,
no-resume, new-state, `authority: none`, `proof_candidate: true`, and
`attestation_required: true`; it uses the fixed `c9-authority` policy,
`c9-ownership` module, `-J-Xmx8g`, and a 21,600-second timeout. The b6e80f1
candidate (505.045 seconds, artifact `sha256:56aa7b6c...b2de`, census
`sha256:b28f186a...1a45`) is planning evidence only and is invalidated by the
current source, contract, tool, and shared-input identities; do not rerun proof
during this integration. An
exit-0 candidate does not promote authority.

C9 source changes select source plus both automatic branches. Kernel fixture or
test changes select source/kernel/public only. Within the Stage5 graph,
adapter/helper/C8 changes select source/adapter only; upstream Stage4 routing
for those same C8 paths remains independent. Changed paths never select the proof candidate;
explicit `--check` and `--all` may request its public+adapter closure. C9
paths are impact-excluded from legacy broad Stage0 ownership, and all C9 fixed
nodes carry the centralized runtime/input, heap, lock, and receipt checks.

## Fixed Stage6 C10 safety graph

Stage6 owns C10 with a fixed, non-authoritative graph layered only on
`stage3-runner-unit`; it does not replay Stage3--5 production lanes:

`stage6-c10-source-structural` -> `stage6-c10-kernel` -> `stage6-public-c10`

and, in a separate branch, source -> `stage6-sh11-c9-safety-adapter`. The
source gate runs five source-only selectors in reviewed order: special-form
arity, export completeness, proof-contract registration, exact contracts, and
static lookup/residual boundaries. It binds the 112,712-byte C10 source at
`sha256:2d334872a84394acc636280796e205a74b227327aa3d646d6c19d55210bd4968`.
Deferred artifact/parity/replay vars are absent; the partial source namespace
is fingerprinted and impact-excluded so its own edits fail closed.

The kernel keeps all seven numeric-safety selectors in one `-J-Xmx2g` JVM,
preserving the namespace-local C10 plan and accepted/rejected fixture-plan
delays. The adapter keeps four pure C9-to-C10 checks followed by exactly one
authenticated `.gravity` boundary in one `-J-Xmx8g` JVM. Its measured lane was
5 tests/147 assertions in 69.470 seconds; the final boundary took 61.220
seconds, built one cold carrier, and did not build the byte-identical `.qst`
twin. The public node is one exact C10 selector at `-J-Xmx2g`, with a
600-second timeout and wall/RSS telemetry. These are resource and development
receipts, not speedup, proof, or authority claims.

C10 source changes select the four automatic Stage6 nodes. Kernel test/fixture
changes select source/kernel/public; adapter test/fixture changes select only
source/adapter. C8/C9 changes retain their owning Stage4/5 routing and select
only the Stage6 source/adapter branch as a downstream consumer. Legacy broad
owners are impact-excluded. All Stage6 production nodes are fresh, exclusive,
capacity-one, command-owned users of `/private/tmp/gravity-sh07-heavy.lock`.

`stage6-c10-proof-candidate` is manual-only, fresh, no-resume, and new-state.
It uses fixed `c10-authority` -> `c10-safety`, `-J-Xmx8g`, a 21,600-second
timeout, `authority: none`, `proof_candidate: true`, and
`attestation_required: true`. Explicit selection joins the public and adapter
branches; ordinary change impact never selects it. An exit-0 proof candidate
does not grant authority.

## Fixed Stage7 C11 MIR graph

Stage7 owns the frozen C11 source through a fixed non-authoritative graph. It
depends only on `stage3-runner-unit` and branches as follows:

`stage7-c11-source-structural` -> `stage7-sh12-c10-mir-adapter`

and source -> `stage7-public-c11`. The source gate uses a 512 MiB JVM and runs
exact binding, control-form arity, then export-definition checks. The moving-
source `stage7-c11-shape-preflight` remains a runner-only alias and is not a
durable manifest owner. The frozen source is 253,588 bytes with SHA
`sha256:34f0e797420b35417dbecb32c28465f7ffbb867c18ac59159bf8ace465054136`.
Its calibrated plan and functions hashes are
`sha256:974d3949e224d136a2d95c0c348b11c8858becdddd47542ffd4ae24c0233fb39`
and
`sha256:ece068d2c82e550798cb98e1b0ac9bd0c5e15b5c932c591b93b821411eed89a4`;
the builder and verifier hashes remain pinned and unchanged.

The adapter keeps six exact SH12 selectors in one 8 GiB JVM. The narrow
verification-envelope helper runs first, four semantic checks follow, and the
single authenticated `.gravity` boundary runs last. The aggregate receipt
at `target/validation/stage7-c11-post-native-3/receipt.json` passed in
490866.529 ms with `authority: fresh-command-pass-non-authoritative` under the
canonical command-owned lock; every production command exited 0 and reported
no skipped selectors. The source gate passed 3 tests/62 assertions (runner 298
ms, wrapper 6722.023 ms, peak 522,780,672 bytes), the public batch passed 2/39
(runner 350265 ms, wrapper 359909.526 ms, peak 2,789,851,136 bytes), and the
exact combined six-selector adapter passed 6/235 (runner 80730 ms, wrapper
86737.375 ms, peak 1,892,941,824 bytes). The earlier separate 1/53 helper and
5/182 suffix receipts are superseded planning evidence. These receipts remain
non-authoritative development evidence, not a proof, attestation, scoped
authority, or release result; no C11 proof candidate was rerun. The measurements
were fresh on the prior 6084-based composition. Coordinator changes since then
alter the exact Stage7 tool input
`bootstrap/clojure/test/gravity/self_hosting_test_runner.clj`, so this receipt
is historical non-authoritative planning/performance evidence rather than
current admission evidence. The final exact seven-node rerun is pending
coordinator C12/SH13 freeze. The public branch still validates exact C11
source/builder semantic identity before the compatibility selector.
All three automatic nodes are fresh, exclusive, capacity-one, command-owned
users of `/private/tmp/gravity-sh07-heavy.lock`. C11 source changes select all
three; SH12 test changes select source plus adapter. The C11 source and Stage7
tests are excluded from the legacy broad Stage0/Stage1 ownership matchers.

`stage7-c11-proof-candidate` is manual-only and joins adapter plus public. It
uses fixed `c11-authority` -> `c11-mir`, 8 GiB, a 21,600-second timeout,
fresh/no-resume/new-state execution, `authority: none`, `proof_candidate:
true`, and `attestation_required: true`. No current proof candidate or reviewed
attestation exists; calibration and adapter receipts remain non-authoritative.

## Fixed Stage8 C12 domain-IR graph

Stage8 owns the current C12 source through a bounded non-authoritative graph:

`stage8-c12-source-shape` -> `stage8-sh13-c11-domain-evidence` ->
`stage8-sh14-authenticated-layout`

and source -> `stage8-public-c12`. The source node uses 512 MiB and runs only
the source-only control-form and export-definition checks. The SH13 node keeps
six exact selectors in one 8 GiB JVM so its namespace-local C12 plan and
prepared evidence carrier are reused; its mutation and provenance checks retain
an exact fail-fast skipped tail. The SH14 node uses one 8 GiB JVM for its exact
five source-ordered selectors. The public sibling uses 2 GiB and one exact
C12 compatibility selector. Every node is fresh, exclusive, capacity one,
command-owned on `/private/tmp/gravity-sh07-heavy.lock`, and reports
`authority: none`.

The current C12 source is 162,404 bytes with SHA
`sha256:827610557f96b2e54e5b89c675f44f7110e3c2658bebef4aafba981abfec9233`.
C12 changes select the four cheap unit prerequisites, these four Stage8 nodes,
and the two downstream Stage9 evidence nodes described below, but not legacy
Stage0 or proof work. The three Stage8 test namespaces
remain declared inputs but are excluded from Stage1's broad test glob. There
is deliberately no C12 proof candidate or authority route in this graph. A
fresh combined Stage8 closure on the final tool/input composition is still
required. The separate SH14 R3 5/304 receipt is non-authoritative and not
source-bound; it is not current admission, proof, attestation, or release
evidence.

## Fixed Stage9 C13 evidence boundary

Stage9 is a two-node automatic, non-authoritative boundary. The 512 MiB
`stage9-c13-source-shape` node runs the C13 source-control selector followed by
the export-completeness selector. Its dependent 8 GiB
`stage9-sh16-c13-evidence-boundary` node runs, in exact source order, the SH16
surface, positive, substitution/hostile-carrier, and top-level-provenance
selectors. Keeping the four evidence selectors in one JVM preserves the
namespace-local C13 plan and prepared C12 carrier cache affinity.

C13 and SH16 changes select the four cheap units plus these two Stage9 nodes;
they do not execute Stage8. C12 and SH13 changes independently select their
true Stage8 consumers and the Stage9 downstream consumer. Both Stage9 nodes
are fresh, no-resume, capacity-one, command-owned users of
`/private/tmp/gravity-sh07-heavy.lock` with `authority: none`. There is no
Stage9 proof or public node, and the boundary claims no optimization credit,
lowering, executable load, self-hosting, or release status.

Prior direct commands measured the shape checks at 2 tests/41 assertions in
1.187 seconds with peak RSS 111,738,880 bytes and the evidence checks at 4/69
in 60.797 seconds with peak RSS 1,602,322,432 bytes. These are historical
non-authoritative planning measurements, not current wrapper receipts. The
composed graph still requires a fresh receipt-bearing run.

## Fixed Stage10 W1 lowering admission

Stage10 is a non-authoritative development boundary for the frozen
C13-to-C14/B1-B4 lowering slice. `stage10-w1-static-admission` uses a 2 GiB JVM
and runs the source-only C14 parse/control-form check first, then the exact
source and plan pins, the complete six-test continuity catalog, and the
complete seven-test target-hardening catalog. It branches to the automatic
3 GiB direct carrier-mutation discriminator. An independent 8 GiB SH25 catalog
node owns the full Gravity-source inventory, and an independent 8 GiB
SH25/SH26 fixture consumer runs the remaining three selectors.

The catalog node declares the SH25 ownership map, the complete
`bootstrap/gravity/src/**/*.gravity` catalog that its selector hashes, the SH19
runtime member, the SH25 engine and accepted fixture, and the SH25 test source.
Because that selector loads the SH25 test namespace, its catalog receipt also
binds the transitively loaded SH26 and authenticated-envelope test sources.
The fixture consumer separately declares both SH25 fixture pairs, the exercised
SH26 engine/accepted pair, and the authenticated-envelope source and all three
test sources. Those are receipt inputs, not incidental namespace loads;
raw C15 source drift selects the catalog and fails closed on its stale tuple.
An accepted C15 revision refreshes the SH25 engine and accepted Gravity/QST
pair and runs the SH25/SH26 consumer once; it does not avoid that consumer.

The packet-substitution matrix is deliberately absent from ordinary change
impact. Explicit stable-candidate selection runs `stage10-w1-hostile-stable`:
direct mutation first and packet substitution second in the same JVM, with
fail-fast and one cache entry, so the namespace-local C14 plan and prepared
carrier are not rebuilt between the two checks. All production nodes are
fresh, no-resume, exclusive, capacity one, command-owned on the canonical
heavy lock, and declare `authority: none`. Existing timings are planning
measurements for the frozen W1 implementation; the new wrapper/manifest
identity still requires a focused receipt before current admission is claimed.

## Fixed Stage11 C15 diagnostics boundary

Stage11 begins with a 512 MiB source-only C15 preflight over one bounded,
nofollow, strict-UTF8 snapshot. It checks control-form arity, exports, and the
exact frozen source identity without compiling C15. From that gate the graph
branches independently to the 8 GiB five-selector SH15 semantic batch and the
2 GiB existing public C15 selector. Their timeouts are 600 and 900 seconds;
all three nodes are fresh, no-resume, capacity-one users of the canonical
command-owned lock with `authority: none`.

The source helper establishes one bounded coherent snapshot; adversarial
same-size swap/restore detection belongs to the combined node and supervising
wrapper's transient-mutation boundary, not to the helper alone.

The semantic receipt binds exactly C8-C12 plus C15 and SH09-SH15; C7 and SH08
are not direct inputs. The public node keeps its bootstrap, CLI, and diagnostics
tests as executable inputs while excluding unrelated edits to those broad
files from Stage11 impact. The older deep C15 coverage namespace is excluded
from broad owners and remains unmatched and fail-closed until its census is
refreshed and a future manual deep node owns it. There is no Stage11 proof,
authority, self-hosting, release, or seed-retirement claim.

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
