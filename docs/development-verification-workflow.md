# Development Verification Workflow: Stage 0 to SH-07/C7/HO2

Status: implementation process contract; this file is outside the 240-document inventory
Scope: reverse verification from the executable stage0 seed toward the current SH-07/C7/HO2 workstream

## Purpose

This workflow makes re-verification cheaper without weakening Gravity's
semantic, safety, provenance, or self-hosting claims. It starts at the current
stage0 executable Clojure seed, follows the D2 milestone order, and feeds the
later self-hosting and whole-language work. The operational labels SH-07, C7,
and HO2 are workstream labels; BOOT1-BOOT8, C1-C18, TEST1-TEST13, D0-D9, and
the phase roadmaps remain the normative contracts.

The workflow is based on the current optimization observations: static and
source checks are much cheaper than carrier construction; related test vars
belong in one JVM; request-level negative mutations can reuse an authenticated
accepted carrier; cache-affine hits have measured at about 1.2-2.2 seconds
versus 57-59 seconds for cold construction; a heavy JVM needs a shared lock;
full-namespace execution is a stable-candidate gate; one fresh authoritative
module should feed its coverage census; and checkpoint/resume is safe only
when identity is conservative and exact. These are measurements and process
inputs, not new language or release claims.

## Workflow Invariants

- Every gate is fail-closed. A rejected cheap or preflight input does not start
  a carrier, JVM, backend, or heavy transaction.
- Stage0 remains an explicit Clojure seed boundary until BOOT5/BOOT7 evidence
  and the P15 final seed-retirement proof say otherwise. A comparison oracle is
  not an authoritative self-hosted compiler.
- A cache is an optimization. Reuse never replaces profile, effect,
  capability, ownership, safety, proof, diagnostic, or provenance checks.
- A source, artifact, fixture, or receipt is identified by content and the
  contract inputs that give it meaning. Timestamps and a path alone are never
  identity.
- Proof, test, benchmark, differential, replay, and audit evidence remain
  separate artifact classes. A passing benchmark cannot prove correctness, and
  a fixture pass cannot replace a required proof or certificate.
- Concurrent work may read immutable artifacts, but each writer uses an
  isolated output location and publishes atomically. Canonical artifacts,
  shared test namespaces, and heavy transactions are not concurrent writers.

## Gate Model

The four execution gates are ordered. A later gate consumes the receipt of the
earlier gate and must not silently repeat work with a broader scope.

### Gate C0 - Cheap static checks

Gate C0 runs without a JVM and without constructing an expensive authenticated
carrier. It checks:

- the requested files, source extensions, source hashes, and workspace scope;
- fixture shape, namespace/module metadata, profile, target, expected artifact
  kind, and expected diagnostic category;
- exact symbol and var names, namespace aliases, and static reachability from
  the requested entrypoint; quoted data is not treated as executable code;
- that the requested oracle is present, versioned, applicable to the profile
  and target, and names its observables and allowed divergences;
- that a changed source, compiler rule, census, or harness has an owner and an
  impact class;
- that no source, fixture, receipt, or census pin is missing or guessed.

C0 emits a `cheap-check-receipt` with the semantic identity, diagnostics,
reachability result, oracle identity, and the next permitted gate. Failure is a
local diagnostic and does not consume heavy resources.

### Gate P1 - Preflight and admission

P1 loads the exact source and contract inputs in a bounded process. It does not
publish authority. It:

- verifies the accepted carrier, source-unit identity, compiler/pass identity,
  dependency and lockfile identity, profile/target/runtime/provider records,
  effects, capabilities, safety mode, and proof policy;
- computes a change-impact plan and the exact related vars, fixtures, stages,
  cache entries, and evidence classes that may run;
- validates cache receipts, invalidation conditions, resource estimates, and
  output isolation; and
- records whether the work belongs to the cheap, focused, or heavy lane.

P1 emits an `admission-receipt`. A cache miss is normal. An identity mismatch,
unknown oracle, malformed carrier, or resource overcommit is a preflight
failure, not a reason to guess or to start the authoritative transaction.

### Gate F2 - Focused execution

F2 runs only the exact dependency-connected vars and fixtures selected by P1.
Related vars run in one JVM process so that namespace loading, compiler state,
authenticated carriers, and diagnostics have one identity. Independent
components may use separate JVMs only when the impact planner proves that they
share no mutable state or carrier.

Request-level negative tests mutate an admitted, authenticated accepted carrier
when the rejection is a field or request mutation. The mutation receipt records
the base carrier hash, mutation path, expected diagnostic, and actual result.
Malformed-source fixtures that cannot be represented as a request mutation are
admitted directly by C0/P1 and retain their own source identity.

F2 may reuse a cache hit after full receipt revalidation. It emits a focused
test, diagnostic, differential, or replay receipt, but it does not claim fresh
authority or stage advancement. Semantic failures are preserved as failures;
only an infrastructure timeout or process loss may receive one bounded retry
with the same identity and lane.

### Gate A3 - Authoritative execution

A3 is the first gate that may produce fresh authority for a candidate. It must
construct one fresh authoritative module for the exact source/compiler/census
identity and record compiler lineage, source and artifact hashes, diagnostics,
conformance, equivalence, safety, and provenance. The full namespace is run
only for a stable candidate after C0, P1, and F2 pass.

Every heavy JVM, SH-07 full transaction, and full C7 namespace run acquires the
single shared heavy-lane lock. Other heavy jobs queue. Read-only consumers may
parallelize after A3 and consume that module's coverage census; they may not
present a census or a cached artifact as a second authority.

A3 emits an `authority-receipt` and the required stage artifacts. Any missing
output, unexplained drift, stale proof, diagnostic regression, resource abort,
or conformance failure blocks stage advancement and preserves a minimized
reproducer.

## Change-Impact Mapping

The impact planner maps changed inputs to the smallest legal work set. The plan
must contain changed identities, impacted symbols, stages, fixtures, cache
keys, evidence classes, resource class, and authority requirement.

| Change class | Minimum recheck | Reuse allowed | Fresh authority rule |
| --- | --- | --- | --- |
| Leaf source body or literal | C0, P1, related F2 vars and diagnostics | Unchanged dependency-connected artifacts with exact keys | A3 only for a stable candidate or release claim |
| Namespace, alias, import/export, profile, effect, capability, safety mode | Namespace plus all downstream facts and diagnostics | None across the changed boundary | Fresh module and coverage census |
| Reader, macro, analyzer, typed-core, C7, or compiler rule | All dependent stages in the pass graph | Only unaffected graph components with exact producer/pass keys | Fresh authority for every affected module |
| Fixture expected result or diagnostic oracle | Fixture C0/P1 and focused comparison | Compiler artifacts may remain valid | New test/differential receipt; authority only if the fixture is part of the candidate |
| Test harness, oracle, comparison mode, or benchmark harness | The affected evidence class | Compiler artifacts may remain valid | Fresh evidence for the changed class |
| Lockfile, dependency, environment, target, backend, runtime, provider, policy, proof checker, or certificate trust | All artifacts whose meaning depends on it | Path-neutral semantic artifacts only when the policy permits | Fresh authority and provenance |
| Actual source path or current working directory | Path-sensitive maps and provenance | Path-neutral semantic artifacts with identical bytes and extension | Re-emit path provenance; fresh authority if output embeds paths |
| Documentation only | Documentation validator and link checks | Code/test artifacts | No code authority claim |

The planner must distinguish a changed semantic input from a changed
provenance path. A path change may preserve a semantic artifact, but it never
permits reuse of an old path-sensitive source map or provenance record.

## Identity, Cache, and Receipt Rules

The semantic cache key is a canonical hash of the complete contract inputs:

```text
source-unit bytes and actual extension
fixture or accepted-carrier identity and request mutation
stage, pass, compiler, and source-rule identities
profile, target, backend, runtime, provider, effects, capabilities, safety mode
dependency graph, lockfile, build-effect replay, and policy
oracle and harness identity, proof/certificate policy, and language facets
```

Actual paths, invocation directory, environment fingerprint, and host details
belong in a separate provenance identity. A path-neutral semantic id can be
reused across roots only when all semantic fields match; a new provenance
sidecar is still required. Source extension is part of identity because `.qst`
and `.gravity` are co-canonical and the actual extension must be preserved.

Every cache entry and receipt records:

- semantic key and artifact hash;
- producer stage/pass/version and input hashes;
- preserved, transformed, and invalidated facts;
- diagnostics and source-origin stream hash;
- proof/certificate freshness and revalidation status;
- profile, target, effects, capabilities, safety, oracle, and environment
  records;
- lane, JVM affinity, resource usage, lock result, and status.

Invalidate downstream entries when source bytes or extension, reader options,
macro or namespace facts, type/effect/profile/capability/safety policy,
dependency or lockfile, compiler/pass contract, target/backend/runtime/provider,
oracle/harness, proof checker, certificate trust, or diagnostic schema changes.
Unknown, partial, speculative, or stale entries are invalid. Speculative
interactive entries cannot publish an artifact until full revalidation passes.
Proofs and certificates require the same claim, inputs, assumptions, checker,
profile, target, provider, and invalidation state; a test or benchmark receipt
cannot authorize their reuse.

The cache is bounded by an explicit size and entry-count budget. Eviction emits
a receipt and never changes semantic identity. Authoritative artifacts and
their provenance are retained separately from speculative or focused entries.

## Fixture, Reachability, and Oracle Admission

Expensive carriers are created only after fixture admissibility succeeds.
Admissibility checks source syntax and metadata, namespace/profile/target
compatibility, expected artifacts and diagnostics, duplicate identity, source
hash, and static reachability. A fixture that cannot reach the requested pass,
var, or oracle is rejected before carrier construction.

An oracle manifest names the oracle kind, trust level, compiler/artifact id,
profile, target, backend/runtime, numeric mode when relevant, observables,
diagnostic comparison mode, and allowed target-specific divergence. The
Clojure stage0 evaluator may be a comparison oracle, but it is explicitly
non-authoritative until the BOOT stage contract retires that boundary.

Static checks must also confirm that executable qualified symbols resolve to the
declared namespace and that quoted payloads are values. Dynamic reachability,
ambient host calls, or an oracle selected by test output do not satisfy
admission. Missing or stale source/census pins fail closed; the planner never
substitutes a nearby hash.

## Evidence and Artifact Authority

Use these evidence classes consistently:

- Proof or certificate: a machine-checkable claim with inputs, assumptions,
  checker, result, and invalidation conditions. It supports safety,
  optimization, math, or pass correctness.
- Test or conformance receipt: an executable positive or negative fixture result
  with source, compiler, profile, target, diagnostics, and artifact identity.
- Differential or replay receipt: compared observables, oracle identity,
  expected divergence, and reproducer or event trace.
- Benchmark receipt: harness, source/compiler/target identity, environment,
  metric, samples, variance, baseline, and semantic/safety gates. It measures
  speed and cannot prove correctness.
- Authority artifact: a fresh module or stage output with provenance,
  equivalence, conformance, TCB delta, and release decision links.

Artifact reuse is allowed for C0/P1 and F2 when identity and invalidation checks
pass. A fresh A3 authority artifact is required after a semantic compiler,
source, census, policy, proof-checker, or target change and before a stable
candidate, stage advancement, SH-07 transaction, or seed-retirement claim.

## Parallelism and Resource Budgets

The scheduler assigns every job a lane, CPU estimate, memory high-water mark,
disk/output scope, JVM count, and shared-lock set before admission.

- Cheap jobs are read-only and may run in bounded CPU parallelism.
- Preflight jobs may parallelize across disconnected modules and use read-only
  caches; they must not write canonical artifacts.
- Focused jobs use one JVM per dependency-connected batch. They may run in
  parallel only when the planner proves disjoint namespaces, carriers, and
  output directories.
- Authoritative jobs use one heavy JVM at a time under the shared heavy-lane
  lock. The observed C7 and SH-07 runs consume roughly 3.5-8 GB, so the
  scheduler reserves the measured high-water mark plus host headroom and
  queues when it cannot reserve that budget.
- The sum of admitted CPU, memory, JVM, disk, and lock budgets must stay within
  host limits. Unknown resource demand is treated as heavy, not guessed.
- A job that publishes an artifact writes a private directory and atomically
  publishes only after its gate passes. Shared mutable target directories are
  never used by concurrent jobs.

This policy increases parallelism in cheap, preflight, and disconnected
focused lanes while making the expensive lane predictable. It does not trade
away one-JVM test affinity or evidence quality for more process count.

## Escalation, Failure, and Resume

- C0 failure: report the stable diagnostic and stop before carrier/JVM work.
- P1 identity or admissibility failure: invalidate the affected receipt and
  rebuild only after the exact source, compiler, census, and oracle identities
  are available.
- F2 semantic failure: preserve the failure and minimizer. Retry once only for
  a recorded infrastructure loss with the same key; do not retry a semantic
  failure to seek a pass.
- Cache miss, repeated invalidation, or low hit rate: inspect the key and impact
  map. Never broaden reuse by dropping a contract field.
- Resource or lock denial: queue the job or reduce its declared scope through
  a new impact plan; do not run concurrent heavy JVMs.
- A3 drift, stale proof, missing artifact, diagnostic mismatch, or conformance
  failure: block the candidate and emit a counterexample/authority receipt.
- A long run may checkpoint completed graph nodes only at declared identity
  boundaries. Resume requires exact semantic key, producer/pass version,
  source/census pin, lock policy, and environment compatibility. If any field
  is unknown, start a conservative fresh run.
- Two repeated identity, authority, or resource failures escalate to an
  independent review of the impact plan and receipt, not to an unrecorded
  command or a guessed stale artifact.

## Metrics and Review Signals

Record per gate and per change class:

- wall time, CPU time, JVM starts, queue time, lock wait, peak memory, disk,
  and checkpoint/resume rate;
- cold versus warm cache latency, hit rate, reuse rate, invalidation rate,
  false-reuse count (must remain zero), and unnecessary-invalidation rate;
- fixture rejection before carrier construction, reachability/oracle admission
  failures, focused-to-authoritative promotion rate, and fresh-authority rate;
- diagnostics by stable code, minimized reproducer count, proof/certificate
  rechecks, differential divergence, and artifact/provenance completeness;
- stage conformance, equivalence, TCB delta, and remaining seed-boundary facts.

The 1.2-2.2 second warm-hit and 57-59 second cold-build observations, the
roughly 40-minute C7 run, the roughly 8.9-hour SH-07 transaction, and the
roughly 4-hour-46-minute parity run are baseline measurements for planning.
They are not acceptance thresholds until a reproducible benchmark receipt
declares the host, compiler, source, harness, and metric.

## Staged Rollout

1. Stage 0 baseline: record source/compiler/environment identities and run the
   existing stage0 positive, negative, artifact, and full-test commands. Keep
   the Clojure seed boundary explicit.
2. Stage 0 shadow planner: implement C0/P1 receipts, static reachability and
   oracle checks, fixture admissibility, impact mapping, and cache keys in
   report-only mode. Compare the planned graph with actual touched vars.
3. Stage 0 focused reuse: enable bounded, revalidated cache reuse and exact
   related-var one-JVM batches. Permit request-level negative mutations only
   from admitted accepted carriers.
4. Stage 0 heavy lane: add the shared lock, resource admission, private output
   directories, and conservative checkpoint/resume. Run one fresh C7 or SH-07
   authority candidate, then parallelize read-only coverage census consumers.
5. Stage 1 and Stage 2 migration: apply the same gates in D2/BOOT3 migration
   order. Move one module at a time, compare stage artifacts, and consume only
   the fresh module's census. Preserve source spans, diagnostics, facts,
   effects, capabilities, safety outcomes, and provenance.
6. SH-07/C7/HO2 candidate: require C0, P1, F2, and A3 receipts; run full
   namespace only at the stable-candidate gate; require BOOT5/BOOT7
   equivalence, TEST13 conformance, BOOT8 provenance, and a TCB delta.
7. Stage 3 and seed retirement: require reproducible rebuilds, accepted-delta
   review, provenance, safety audit, and final seed-retirement evidence. A
   fail-closed result remains incomplete when `:clojure-seed-boundary? true`.

## Expected Validation Commands

These are existing repository commands. Parent integration must bind any
SH-07-specific runner to a real command before adding it here; this workflow
does not invent a command name.

Cheap/preflight examples:

```bash
git status --short
python3 tools/validate_gravity_docs.py
GRAVITY_BOOTSTRAP_ONLY=1 bin/gravity check bootstrap/gravity/p15_s23/compiler.gravity
bin/gravity check examples/hello.gravity
bin/gravity check examples/core-app.gravity
```

Focused C7 and stage0 examples:

```bash
clojure -M:gravity compiler-c7-type-check bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity
clojure -M:gravity run examples/hello.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Authoritative and release-boundary examples:

```bash
clojure -M:test
clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
clojure -M:gravity p15-s23-final-seed-retirement-proof bootstrap/gravity/p15_s23/compiler.gravity
bin/gravity self-host verify
```

The commands emit or inspect the stage0, C7, P15, and public verifier artifacts
described by README.md, docs/bootstrap/clojure-bootstrap.md, BOOT5-BOOT8, and
the phase roadmaps. A failed or interrupted command is not credited until its
receipt records the gate, identity, diagnostics, and resource outcome.

## Requirements

- The implementation MUST run C0 and P1 before carrier construction and MUST
  record a receipt for every gate.
- The impact planner MUST map all semantic and evidence inputs to dependent
  stages, fixtures, cache entries, and authority requirements.
- Cache keys MUST include all C16 semantic inputs and MUST separate
  path-neutral semantic identity from path-sensitive provenance.
- Related vars MUST keep one-JVM affinity; heavy authority MUST use one shared
  lock and an admitted resource budget.
- Fixture admissibility, static reachability, and oracle identity MUST pass
  before expensive carriers or authoritative execution.
- Reused artifacts MUST pass schema, producer, preserved-fact, proof,
  diagnostic, policy, and invalidation checks. Unknown or speculative reuse
  MUST not publish.
- A3 MUST use a fresh authoritative module after affected source, compiler,
  census, policy, proof, target, or oracle changes and MUST emit provenance and
  equivalence evidence.
- Stage advancement MUST follow BOOT1-BOOT8 and TEST13, preserve exact claims,
  and keep the seed boundary explicit until its proof is complete.

## Dependencies

This workflow is derived from D0, D1, D2, D3, D6, D8, and D9; C1-C18;
TEST1-TEST13; PKG3, PKG7, PKG10, and PKG12; and BOOT1-BOOT8. It consumes the
current stage0 bootstrap contract, phase roadmaps, `plan.md`, and `heartbeat.md`.
It informs the Stage 0 implementation, SH-01 impact planning, SH-07
authoritative execution, C7 type-checker verification, and later HO2 work, but
does not redefine those contracts.

## Outputs and Artifacts

The workflow produces:

- cheap-check and admission receipts;
- change-impact plans and dependency/reachability graphs;
- fixture-admissibility and oracle manifests;
- stage-specific cache keys, cache entries, invalidation traces, and reuse
  reports;
- focused test, diagnostic, differential, replay, and benchmark receipts;
- heavy-lane lock and resource records;
- checkpoint/resume records and minimized reproducers;
- fresh authority modules, coverage census links, equivalence reports,
  conformance reports, provenance, TCB deltas, and release decisions.

## Diagnostics

Implementations should use existing C16, C18, TEST, BOOT, D6, D8, and D9
diagnostics where applicable. This process adds no replacement diagnostic
namespace. A process receipt must still identify the stable diagnostic id,
source or artifact, gate, semantic key, lane, and remediation.

## Conformance Criteria

- A changed source, compiler rule, policy, target, oracle, proof checker, or
  harness invalidates every dependent receipt and no unrelated receipt.
- Related focused vars run in one JVM, and concurrent heavy authority runs are
  prevented by the shared lock.
- A structurally inadmissible or unreachable fixture fails before expensive
  carrier construction.
- A cache hit is measurably faster than a cold build while preserving all
  required identity and evidence fields; false reuse is zero.
- One fresh authoritative module can feed parallel read-only coverage census
  consumers without creating conflicting authority claims.
- Checkpoint/resume accepts only exact identity and otherwise performs a fresh
  run.
- Proof, test, benchmark, differential, replay, and audit artifacts remain
  distinguishable and linked to provenance.
- Stage0, stage1, stage2, SH-07/C7/HO2, and stage3 claims remain bounded by
  their actual evidence; the current incomplete seed-retirement state is not
  reported as complete.

## Acceptance Criteria

Parent integration is accepted when the repository contains this workflow,
reviews confirm that every command named above exists, and the following
evidence is attached to the implementation work:

1. A Stage 0 shadow run shows C0/P1 receipts and an impact plan before any
   expensive carrier or heavy JVM invocation.
2. A focused run demonstrates exact related-var one-JVM affinity and a
   request-level negative mutation tied to an accepted carrier hash.
3. A cache hit and a forced invalidation demonstrate receipt identity and
   invalidation for source/compiler/policy/oracle changes.
4. A heavy run demonstrates shared-lock serialization, resource admission, and
   conservative checkpoint/resume behavior.
5. A fresh authoritative module produces a coverage census and provenance;
   consumers reuse it read-only without claiming a second authority.
6. The existing docs validator and required stage0/C7/P15 commands pass, while
   any failure remains attached to a stable diagnostic and an honest incomplete
   status.
