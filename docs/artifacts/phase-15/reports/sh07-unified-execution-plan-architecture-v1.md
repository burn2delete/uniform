# SH-07 Unified Execution and Artifact Architecture v1

Status: pre-implementation design on local authoritative `main`

Exact base commit: `4cf14c5f27f761955394dd729c1d2926cb8df557`

Exact base tree: `b692650dea63da1ce1e207008c18f4f12f935676`

## Purpose

This report freezes the smallest complete architecture for simplifying Gravity
development and SH-07 verification. It introduces one closed execution-plan
engine, two explicit lanes, one authenticated compiler-artifact pipeline, one
reviewed verification manifest, and a parity-gated retirement process. Existing
authoritative behavior remains the compatibility oracle until independently
reviewed parity evidence permits deletion.

This is tooling architecture only. It grants no SH roadmap, integration,
proof, release, self-hosting, seed-retirement, safety, or performance authority.

## Governing inputs

The design is governed by `AGENTS.md`, D0, D1, D2, D3, D6, D8, D9, C1 through
C7, C16, C18, PKG3, PKG7, PKG10, BOOT1, BOOT2, BOOT3, BOOT6, BOOT7, BOOT8,
TEST1, TEST2, TEST13, `docs/development-verification.md`, the SH backlog and
ownership records, `sh07_proof_contract.edn`, and workstream governance.

## Current status and measured baseline

At this base, 7 of 30 SH slices are complete, 6 of 30 have executable
Gravity-owned implementations, 7 of 30 retain Clojure in the claimed execution
boundary, 4 of 30 are blocked, and the highest continuous completed prefix is
SH-00 through SH-06. SH-07 remains partial.

The following development-only measurements were taken in the clean candidate
worktree. They are observations, not performance evidence:

| Operation | Elapsed | Maximum RSS or result |
| --- | ---: | ---: |
| Empty Clojure JVM, `-J-Xmx512m` | 1.02 s | 104,742,912 bytes |
| Repository snapshot | 1.86 s process; 1,091.652 ms call | 389,349,376 bytes; 7,807 paths |
| C2 artifact for accepted `hello.gravity` | 10.10 s process; 3,590.176 ms construction after require | 1,211,760,640 bytes |
| One exact CLI presentation test through the iteration runner | 6.71 s process; 5 ms test | 1,123,713,024 bytes |
| One exact bounded-runner receipt test | 6.44 s process; 21 ms test | 1,128,464,384 bytes |
| Clojure parent launching an empty child JVM | 342.180 ms | exit 0 |

The repository snapshot identity was
`sha256:2bc2e0fb4c906df3874f80196b6e2441675b0ae30184aa6a12ea6205215a7497`.
No broad or authoritative SH-07 suite was run to collect these numbers.

## Complete infrastructure inventory

The overlapping selection and execution layer comprises:

| Area | Current mechanisms | Overlap |
| --- | --- | --- |
| Authoritative SH-07 | `sh07_authoritative_runner.clj`, proof contract, module catalog | selection, closure, fresh process, receipts, census |
| Bounded development | `sh07_bounded_development_runner.clj` | closed routes, limits, child process, receipts |
| Feature shards | `sh07_cached_shard_runner.clj`, `sh07_contract_shard_runner.clj`, `sh07_parallel_shard_runner.clj` | hard-coded B10/B11/B12/B13/B15 selectors and caches |
| Iteration cache | `sh07_iteration_cache_runner.clj` | selectors, authenticated cache keys, execution, telemetry, receipts |
| Stage verification | `stage3_verification_runner.clj` | 40 fixed batches, execution profiles, scheduling, receipts |
| SH-01 development | development runner, incremental check, impact planner, parallel runner | namespace/route catalogs, dependency selection, scheduling |
| Shared execution substrates | host resource broker, exact-tree warm worker, process helpers | leases, bounded execution, warm-JVM protocol |
| Persistent caches | development test cache, `pass_cache`, `c2_pass_cache`, C15/C16 and C17/C18 pass caches | separate keys, validation, secure storage, publication |
| Compiler identity/incrementality | C2 artifact identity, compiler-pass manifest incremental support, C16 incremental support | identity, dependency and invalidation policy |
| Hand-maintained inventories | SH ownership EDN, stage-0 incremental dependencies, proof contract catalogs, runner-local catalogs, `deps.edn` aliases | ownership, routing, closure and evidence duplicated in code/data |

The twelve largest specialized runner, planner, cache, worker, and broker files
contain 12,526 lines. This count is a size observation, not a deletion target.
The broker and warm worker implement useful substrate behavior and are not
themselves competing plan authorities.

The SH-07 test surface contains 65 namespaces. Thirty source-coverage
namespaces contain 254 tests, approximately 2,915 assertions, and 18,708 lines;
all duplicate core harness operations. The SH-07 fixture trees contain 165
accepted/rejected cases, each paired with a co-canonical `.qst` file. Fourteen
microfeature suites account for 158 tests and 147 fixture pairs and are the
initial table-consolidation candidates.

## Final architecture

### One closed execution-plan engine

A request is data in a closed EDN schema. The engine performs, in order:

1. strict request, route, selector, and policy validation;
2. manifest lookup and explicit dependency closure;
3. source, fixture, contract, runtime, and tool identity snapshotting;
4. lane and cache-policy authorization;
5. resource-broker acquisition;
6. bounded warm-JVM or fresh-process execution;
7. post-execution mutation and identity validation; and
8. deterministic receipt construction.

Plans declare selection, dependency closure, resource class and bounds, cache
eligibility/key inputs, execution mode, and required evidence. Unknown keys,
unknown routes, cycles, unowned paths, lane-policy contradictions, incomplete
closures, and malformed results fail closed with stable diagnostics.

The host resource broker, exact-tree warm worker, existing secure cache I/O,
and authoritative SH-07 executor may remain implementation substrates. They do
not own selection or policy. Existing runner entry points initially become thin
compatibility projections into this engine.

### Two explicit lanes

`:fast` is incremental, bounded, warm where eligible, cacheable where the
manifest explicitly permits it, and always non-authoritative. Its receipts must
state that it cannot confer integration, proof, release, self-hosting,
seed-retirement, safety, or performance authority. Cache identity covers the
entire declared closure and runtime/tool/policy identity. Source mutation,
symlink substitution, path rebinding, stale entries, malformed entries, and
unknown dependencies fail closed.

`:fresh` exports the exact clean candidate tree into isolation, uses
`-Srepro -Sforce`, exposes no development or compiler cache, forbids resume and
warm workers, executes in fresh bounded processes, and emits a write-once
receipt. The existing SH-07 authoritative proof contract and independent replay
remain mandatory. A fresh-lane routing receipt does not by itself confer SH-07
authority; only the governing executable exit gate can do so.

### One authenticated compiler-artifact pipeline

The content-addressed DAG is:

`source -> reader -> syntax -> macroexpansion -> resolution -> checked-core`

Each node records schema, stage, canonical content identity, parent identities,
source-byte identity, compiler/pass/contract/policy identities, normalized
semantic path identity plus actual-path provenance, dependency and macro
environment closure, profile/target/effect/capability policy, preserved and
invalidated facts, deterministic diagnostics identity, and verification status.
Consumers request the earliest artifact they need and authenticate every parent
edge before use.

The fast lane may reuse an unchanged authenticated intermediate only after full
key equality, source snapshot/re-read, mutation checks, and stage verification.
Path rebinding may preserve semantic content identity while changing actual-path
provenance. The fresh lane cannot observe the store and reconstructs every stage
from the isolated source tree. Partial, speculative, stale, malformed, unknown-
schema, or mismatched artifacts fail closed. Proof and certificate products are
never generalized beyond their exact claim, inputs, assumptions, checker,
provider, and package identities.

### One reviewed manifest

`contracts/verification-catalog.edn` is the sole reviewed source for:

- module identity, source ownership, and earliest compiler artifact;
- routes and selectors;
- explicit dependency closure;
- resource class and bounds;
- lane and cache eligibility;
- suites, accepted/rejected fixtures, and expected diagnostics;
- coverage ownership/accounting; and
- required evidence, authority ceiling, lifecycle, and replacement metadata.

Generated projections are validated, not independently edited. The validator
rejects duplicate identifiers or paths, missing ownership, unknown references,
cycles, incomplete fixture pairs, route/resource contradictions, fresh routes
with cache eligibility, authoritative routes in the fast lane, and missing
evidence or nonclaims.

### Data-driven conformance suites

The initial fixture tables cover the repetitive source-coverage and
microfeature cases whose semantics are expressible as accepted/rejected rows.
Rows preserve module, fixture pair, profile, target, expected diagnostic code,
category, span and remediation, expected artifact identities, and coverage
obligations. A common harness executes the tables.

Concurrency, mutation, cancellation, cleanup, security boundaries, state
transitions, cache/provenance behavior, receipt transactions, recursion/order,
arity, set mutation, exceptions/error exits, and other complex semantics remain
bespoke tests.

## Deletion and consolidation map

| Existing responsibility | Successor | Removal gate |
| --- | --- | --- |
| Runner-local route and selector catalogs | verification manifest projections | exact route and rejection parity |
| Bounded/cached/contract/parallel shard orchestration | unified plan engine | behavior, diagnostics and receipt parity |
| Stage3 fixed batch catalog | manifest route entries | exact batch membership and profile parity |
| SH-01 impact/development route ownership | manifest dependency projection | closure and invalidation parity |
| Runner-local execution policy | engine plus broker/warm/fresh substrates | bounds, cleanup and mutation parity |
| Independent stage cache policy front ends | authenticated artifact pipeline adapters | key, invalidation, security and provenance parity |
| Repetitive fixture loops and source-coverage scaffolds | conformance tables and common harness | exact fixture/diagnostic/artifact parity |
| Superseded executable contracts | immutable terminal lifecycle records | independent review and accepted replacement |

No old executable is removed in the first compatibility phase. Retirement
requires old/new execution against the same pinned snapshot in bounded exact-var
batches, equality of selected vars and fixtures, exit status, ordered structured
diagnostics, artifact and receipt identities, provenance, resource ceilings,
mutation/substitution/stale/malformed failures, repeated-run behavior, and a
separate cache-free replay. An independent semantic and security reviewer must
accept the exact candidate tuple before governed deletion and publication.

## Invariants

- All schemas and catalogs are closed and unknown input fails closed.
- Existing diagnostic identifiers, ordering, payloads, and remediation survive.
- Source bytes, dependency closure, compiler/tool/policy identity, and actual
  path provenance are authenticated before and after execution.
- Cache keys include every semantic and policy input; cache hits never weaken
  verification, mutation detection, or provenance.
- Fresh acceptance is isolated, cache-free, deterministic, bounded, and
  independently replayable.
- Resource classes, FIFO coordination, timeouts, output bounds, process-tree
  cleanup, and capacity limits are preserved.
- Candidate-specific behavior is manifest data, not permanent runner code.
- Tests are removed only after exact parity, not after approximate coverage.
- One candidate is active per invariant family; base, candidate, and tree
  identities are exact. If local main advances, the whole accepted candidate is
  reconciled and re-reviewed.
- Terminal lifecycle records are append-only and published through the governed
  transaction.

## Nonclaims

This work does not complete an SH slice and does not change the backlog counts.
Fast-lane success, cache hits, manifest validation, routing success, development
receipts, historical timing, or tooling simplification are not proof, release,
integration, safety, performance, self-hosting, seed-retirement, or roadmap
evidence. Clojure and the JVM remain temporary host, loader, verifier, cache,
process, receipt, and observation boundaries. There is no claim of hostile
same-user isolation, full compiler completion, macro-runtime authority,
cross-process production cache trust, or release reproducibility.

## Implementation sequence

1. Add and validate the manifest and its closed projections.
2. Add the plan engine and lane receipts; route bounded compatibility cases.
3. Add the authenticated stage pipeline and fast/fresh policies.
4. Add table-driven conformance without deleting legacy tests.
5. Produce bounded exact parity evidence and obtain independent review.
6. Replace compatibility wrappers, retire proven redundant code, reconcile the
   whole candidate with current local main, and publish immutable lifecycle
   records.
