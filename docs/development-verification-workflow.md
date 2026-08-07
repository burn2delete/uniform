# Stage 0 Development Verification Bridge

Status: supplemental rollout guide; outside the 240-document inventory

This guide is an implementation bridge for the Stage 0 development runner. It
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

The bridge is intentionally subordinate. It records current implementation
behavior and deferred work; it does not define a new language, backend,
self-hosting, seed-retirement, or release contract.

## Implemented and deferred scope

| Area | Implemented in the Stage 0 runner | Deferred until the governing contracts and artifacts are wired |
| --- | --- | --- |
| Selection | Changed-path matching, lane filtering, explicit-check selection, dependency/downstream closure, and fail-closed unmatched/out-of-lane diagnostics | SH-01/SH-07 impact planning and later-stage graph migration |
| Receipts | JSON plan and execution receipts with command, input, dependency, lock, mutation, timeout, and non-authoritative status | Promotion to an authority receipt or stage/release decision |
| Lanes | `preflight`, `focused`, and fresh serialized `heavy-candidate`; all current results remain non-authoritative | Authority promotion and any destination lane that can publish authority |
| Freshness | `fresh: true` checks always execute; heavy candidates use the shared lock | Memory admission and host-wide resource reservation |
| Input/cache safety | Root-bound no-follow descriptor hashing, coherent fstat snapshots, complete redacted runtime/environment identity, conservative invalidation, and no cache write after mutation | Explicit output-artifact validation and authority promotion |
| Mutation monitoring | Kernel vnode watches with final event drain on supported hosts; unsupported polling fallback is non-cacheable; glob roots and existing subtree directories are watched | Cross-platform FSEvents/inotify parity and a future artifact event protocol |
| Lock safety | Shared safe open (`O_NOFOLLOW|O_CREAT|O_RDWR`, mode `0600`) with regular-file, owner, link-count, and parent-path checks; resource locks are direct children of trusted sticky `/private/tmp`, while cache locks hash the canonical resolved root/cache identity under `/private/tmp` | Bounded eviction and cache lifecycle policy |
| Process safety | Every manifest check binds `daemonization: forbidden`; commands run in new process groups, ordinary descendants are cleaned before lock release, and one bounded host-wide `ps eww` environment census at command termination covers saved same-marker processes | Strict containment of arbitrary cross-session daemonization (including double-fork/`setsid`) is deferred to an OS job/container; private output isolation and atomic artifact publication are also deferred |
| Stage rollout | Stage0 selection, receipts, cache/lock safety, and targeted verification commands | Stage1/Stage2 rollout, SH-01, SH-07, C7/HO2 integration, and later-stage claims |

The deferred column is a boundary, not an implied implementation. A passing
command, warm cache hit, benchmark, or heavy-candidate receipt must not be
described as a proof, authority artifact, bootstrap claim, or release result.

## Stage 0 lane model

- `preflight` contains cheap contract, documentation, and orchestrator checks.
- `focused` contains small reader, hosted, and selective Stage0 checks. Exact
  cache identities may be reused only after input, command, dependency, and
  environment revalidation.
- `heavy-candidate` contains fresh, serialized, expensive checks such as the
  Stage0 Clojure suite. Its `authority` field is `none`; a command pass remains
  `fresh-command-pass-non-authoritative`.

The lane name describes scheduling cost and candidate freshness, not authority.
The existing `:test` Clojure alias remains the broad self-hosting runner. The
Stage0 manifest uses `:stage0-test` so a development check cannot accidentally
launch that larger target.

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
   forbidden` and is placed in a new process group.
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

These examples are intentionally limited to existing Stage0 commands:

```bash
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s tools/tests -p test_verify_development.py -v
python3 tools/verify_development.py --lane preflight --lane focused --dry-run --explain --human
python3 tools/verify_development.py --lane heavy-candidate --dry-run --human
clojure -M:stage0-test
```

The runner should be exercised with targeted checks and dry runs while this
bridge is being integrated. Do not start the heavy/full suite merely to prove
the planner. The manifest command is `clojure -M:stage0-test`; the existing
`:test` alias remains untouched for its broader purpose.

## Dependencies

Use the existing `docs/development-verification.md` contract and the relevant
phase README/documents first. This bridge is informed by D0, D1, D2, D3, D6,
D8, D9, BOOT, TEST, and package contracts, but it does not redefine them. The
implementation files are `tools/verify_development.py`,
`tools/development_verification_manifest.json`, and
`tools/tests/test_verify_development.py`.

## Outputs and artifacts

The implemented runner emits a plan/execution receipt, per-check command and
input identities, dependency status, lock result, mutation observations,
timeout cleanup, and cache keys. These are development receipts and
diagnostics. Required output artifact validation, provenance bundles,
coverage-census authority, equivalence/conformance evidence, SBOMs, and
release decisions remain deferred to the normative contracts and later work.

## Conformance and acceptance

- The manifest validates with exactly `preflight`, `focused`, and
  `heavy-candidate` lanes; all manifest checks currently declare
  `authority: none`.
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
private output publication, bounded eviction, SH-01/SH-07 integration, and
later-stage rollout require their own contracts and evidence.
