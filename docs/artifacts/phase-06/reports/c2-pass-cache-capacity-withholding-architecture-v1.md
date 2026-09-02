# C2 Pass-Cache Capacity Withholding Architecture Version 1

Status: draft architecture candidate; report only; no implementation authority

## Purpose

The local C2 pass cache can receive a fresh, authentic C2 reader artifact that
passes the current C2 and SH-03 validators but cannot be represented inside the
bounded local cache envelope. Cache reuse is an optimization under C16. Failure
to publish that optional local representation must not erase an already valid
fresh reader result, but only a closed resource-capacity result may take that
path.

This decision defines that narrow boundary. It does not change C2 reader
acceptance, the SH-03 reader, the generic pass-cache authority ceiling, cache
key meaning, retained-entry validation, direct storage, or any release,
equivalence, proof, or self-hosting gate.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "c2-pass-cache-capacity-withholding-architecture-v1",
  "invariant_family": "architecture/compiler-c2-pass-cache-capacity-withholding-v1",
  "report_path": "docs/artifacts/phase-06/reports/c2-pass-cache-capacity-withholding-architecture-v1.md",
  "status": "draft",
  "base_commit": "0565b642d46fc2504412177bbd55b8e970804c4f",
  "dependencies": [],
  "historical_references": [],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```

## Governing boundary

C2 owns the validity and exact identity of the fresh reader artifact. C16 owns
whether that artifact is reusable through the local cache. The cache remains a
speculative, local-development optimization. Therefore the only permissible
semantic result of this decision is:

```text
valid fresh C2 artifact + unrepresentable local cache envelope
  -> return that exact fresh C2 artifact
  -> withhold only local cache publication
  -> emit local non-authoritative C16-POLICY evidence
```

An invalid C2 artifact is still rejected. A value that cannot be a C2 artifact
is still rejected. An unavailable or corrupt store is still rejected. No cache
capacity result may manufacture C2 acceptance.

## Exact operation order

The active C2 `lookup-or-compute!` adapter must preserve this order:

1. Build and validate the complete semantic key before entering the capacity
   lane. Key construction has no withholding fallback.
2. Perform the normal receipt-first local lookup through the failure taxonomy
   below. A fully revalidated hit invokes the C2 semantic validator exactly
   once, is returned unchanged, and does not execute the reader.
3. On a miss or retained-entry rejection, execute the ordinary fresh C2
   producer while holding the existing per-key cooperative lock.
4. Independently validate the fresh C2 artifact against the exact current key,
   producer binding, C2 pass contract, and SH-03 authenticated boundary before
   attempting to encode or account for a cache envelope. This is the one and
   only semantic-validation call on the fresh path.
5. Preserve the exact validated value in the closed, in-memory prevalidated
   carrier defined below. Envelope construction and generic producer-receipt
   construction may inspect it but may not replace, normalize, summarize,
   truncate, or round-trip the return value.
6. Attempt the bounded cache-envelope preflight. Only the closed capacity
   outcome defined below can branch to capacity withholding.
7. On envelope success, generic `:validate-output!` validates only the envelope
   shape and identity bindings. It must not invoke the C2 semantic validator.
   Continue through the generic receipt, admission, and atomic publication
   path.
8. On the closed capacity outcome, skip every blob, receipt, entry, and staging
   publication operation; return the exact preserved fresh artifact with the
   contained local evidence defined below.

The capacity branch occurs before any candidate bytes or staging file are
created. Existing store directories and lock files are outside the candidate
artifact set and may already exist, but no blob, receipt, entry, temporary
stage, rejected-entry replacement, or on-disk capacity receipt may be created
for the withheld candidate.

## Exactly-once semantic validation

The active C2 adapter owns C2 artifact semantics. The generic cache owns only
envelope and receipt mechanics. A successor implementation must split the
current overloaded generic validation operation into two explicitly named
roles:

- `:validate-retained-artifact!` is used only after receipt-first integrity
  validation and envelope decoding on a hit. The C2 adapter implementation of
  this callback invokes the current C2 semantic validator exactly once.
- `:validate-output!` is used only for the already prevalidated fresh or direct-
  store envelope. It validates the closed envelope shape, payload digest, C2
  artifact id, boundary projection id, key ids, and carrier binding. It never
  invokes the C2 semantic validator.

`gravity.pass-cache.receipt-validation/validate-cache-operations!` must require
both roles for this adapter and reject aliases, missing functions, or a single
function supplied for both roles. `validate-artifact-against-receipt!` must use
only `:validate-retained-artifact!`; pass execution and direct publication must
use only `:validate-output!`. A boolean skip flag is insufficient evidence of
ownership and must be replaced or encapsulated by these typed roles.

After a fresh producer succeeds, or before the C2 public direct `store!` enters
generic publication, the adapter creates this exact-key, in-memory-only state:

```clojure
{:artifact :gravity/c2-pass-cache-prevalidated-carrier
 :schema-version 1
 :state :semantic-validated
 :exact-artifact exact-current-C2-object
 :c2-artifact-id sha256
 :boundary-projection-id sha256
 :semantic-key-id sha256
 :storage-key-id sha256
 :producer-binding-id sha256
 :validation-binding-id sha256
 :carrier-id sha256}
```

The map has exactly these keys. `:carrier-id` is the domain-separated canonical
digest of every field except `:exact-artifact` and `:carrier-id`, plus the
artifact's independently computed `:c2-artifact-id`; it is not authority. The
carrier remains in the adapter's lexical operation state, is never returned by
the generic API, encoded, written, or accepted from a caller, and is destroyed
with that operation. The envelope binds `:carrier-id`. The C2 result projector
uses `:exact-artifact` from the same carrier for a fresh success or withholding
result and must not decode the envelope to manufacture the returned value.

Semantic-validator ownership is therefore closed:

| Path | Semantic validator owner and count |
| --- | --- |
| Clean miss and fresh production | C2 adapter immediately after `produce!`, exactly 1; generic output validation is envelope-only. |
| Rejected retained entry followed by fresh production | Same fresh C2 adapter point, exactly 1; the failed retained attempt may not invoke the semantic validator after structural rejection. |
| Valid hit | `:validate-retained-artifact!` after receipt, blob, envelope, and identity checks, exactly 1; producer count 0. |
| C2 public direct `store!` | C2 adapter before carrier/envelope construction, exactly 1; generic direct store performs envelope-only validation and still rejects capacity. |

A semantic validation failure propagates from its owning point after one call,
creates no carrier accepted for publication, and creates no candidate file.
Tests must instrument the actual current validator and prove call counts of one
for clean miss, rejected-entry miss, hit, and direct store; zero for semantic-
key failure and retained structural rejection before fallback production; and
no duplicate or bypass path under concurrency. Unsupported envelope values
propagate after the one fresh semantic-validation call if production reached
that point.

## Closed typed envelope-capacity transport

Only `clojure.lang.ExceptionInfo` may transport the internal capacity result.
Its `ex-data` has exactly this shape:

```clojure
{:id "C16-POLICY"
 :artifact :gravity/c2-pass-cache-envelope-capacity-exception
 :schema-version 1
 :stage :c2-reader
 :operation one-of-[:lookup-or-compute :store]
 :phase one-of-[:fresh-envelope-preflight :direct-store-envelope-preflight]
 :phase-origin {:artifact :gravity/c2-pass-cache-phase-origin
                :schema-version 1
                :phase same-phase
                :carrier-id sha256}
 :resource one-of-[:canonical-nodes :canonical-depth :canonical-bytes]
 :maximum positive-integer
 :observed positive-integer
 :semantic-key-id sha256
 :storage-key-id sha256
 :c2-artifact-id sha256}
```

Both the outer and nested maps have exactly the shown keys. The exception must
be minted by the private bounded encoder only while it encodes the envelope
from the carrier in the fresh-envelope lexical phase. `:phase-origin` must
equal the origin built from the exact carrier held by the catch site; merely
presenting the right diagnostic id or a forgeable look-alike map is
insufficient. `:observed` is the first violating node or byte count, or the
actual violating depth, and must be strictly greater than `:maximum`.

The withholding catch additionally requires `:operation :lookup-or-compute`
and `:phase :fresh-envelope-preflight`. It verifies the exception class, exact
key sets, enum members, positive
integer fields, strict `observed > maximum`, all three key/artifact identities,
and exact carrier-bound phase origin. Any failed check rethrows the original
exception unchanged. The exception is not minted by semantic-key
canonicalization, source snapshotting, retained entry/receipt/blob decoding,
semantic validation, store inventory, filesystem admission, or an arbitrary
canonicalization request. Direct store may mint the closed form with
`:operation :store` and `:phase :direct-store-envelope-preflight`, but has no
withholding catch and therefore rejects it unchanged.

This is a resource-policy result, not evidence that the C2 artifact is
malformed. Its externally contained diagnostic is `C16-POLICY`.

## Closed public withholding evidence

For a clean miss, the public C2 compatibility projection returns exactly:

```clojure
{:artifact :gravity/c2-local-pass-cache-evidence
 :schema-version 1
 :stage :c2-reader
 :semantic-key-id sha256
 :storage-key-id sha256
 :status :miss
 :reason :fresh-artifact-envelope-capacity
 :contained-diagnostic "C16-POLICY"
 :contained-capacity exception-ex-data
 :cache-publication :withheld
 :disk-candidate-publication :none
 :reader-executed? true
 :artifact-reused? false
 :artifact-id sha256
 :local-development-only? true
 :release-authority? false
 :proof-authority? false
 :equivalence-authority? false
 :self-hosting-credit? false}
```

The map has exactly those keys. If lookup first rejected retained data and the
fresh artifact then exceeds envelope capacity, the evidence instead has exactly
the same keys plus `:rejected-entry-evidence`, and its reason is
`:rejected-entry-fresh-artifact-envelope-capacity`. The added value is the
unchanged closed generic rejected-entry evidence returned by lookup. It must
not be overwritten, flattened, relabelled, or hidden by capacity evidence.
Clean-miss evidence must not contain `:rejected-entry-evidence`.

In both schemas, `:contained-capacity` is the exact validated `ex-data` map, not
the exception object or its message. The result's artifact field is the exact
fresh validated object held by the carrier. Tests must establish value,
metadata, host identity where observable, source provenance, diagnostic stream,
and SH03 boundary equality with the direct uncached result. The evidence is
in-memory only, not a cache entry, producer receipt, proof, equivalence report,
release record, or bootstrap provenance artifact.

## Generic lookup failure taxonomy seam

The live public C2 adapter calls `gravity.pass-cache.execution`, which calls
`gravity.pass-cache.lookup`. Today the generic `lookup-unlocked!` and outer
`lookup!` catch almost every `Throwable` and turn it into rejected-entry
evidence. That erases ordinary filesystem failures and
`java.nio.channels.ClosedByInterruptException`. This decision authorizes the
minimum generic correction in `gravity.pass-cache.policy` and
`gravity.pass-cache.lookup`, plus focused generic tests; it does not authorize
a new cache policy or changes to unrelated clients.

The generic classifier has this closed result set:

- `:propagate-fatal`: `ThreadDeath` and `VirtualMachineError`; rethrow the same
  object.
- `:propagate-interruption`: `InterruptedException` and
  `ClosedByInterruptException`; restore the current thread's interrupted flag
  and rethrow the same object.
- `:propagate-io`: every other `java.io.IOException`; rethrow the same object.
- `:retained-corruption`: only a private, exact-key retained-failure marker
  minted around deterministic validation of bytes that were successfully read
  from an existing entry, receipt, or blob. Convert this marker to rejected-
  entry evidence while retaining its `C16-ENTRY` or `C16-STALE` diagnostic.
- `:propagate-other`: every unsupported value, callback/validator failure,
  unmarked `ExceptionInfo`, and other throwable; rethrow the same object.

The marker's exact data binds `:artifact
:gravity/pass-cache-retained-failure`, `:schema-version 1`, `:phase` in
`[:entry-decode :entry-validation :receipt-decode :receipt-validation
:blob-decode :blob-identity :entry-derived-validation]`, `:diagnostic-id` in
`["C16-ENTRY" "C16-STALE"]`, and the current cache-key id. It may wrap only
deterministic cache-owned decode, schema, digest, receipt-binding, and derived-
entry checks after a successful bounded read. A successful existence probe
that reports a missing receipt or blob may mint a retained `C16-STALE` marker;
an exception thrown by that probe may not. The current validator callback and
all secure-directory/open/attribute/read operations, recovery, lock, and probe
exceptions remain outside the marker or are explicitly rethrown before
marking. In particular, a validator throwing `C16-ENTRY` is still
`:propagate-other`; diagnostic text or id alone never proves retained
corruption.

Both catch sites must use this classifier. Exact-path tests in
`bootstrap/clojure/test/gravity/pass_cache_test.clj` must inject ordinary
`IOException` and `ClosedByInterruptException` at entry, receipt, and blob read
boundaries and recovery/outer-lookup boundaries; assert identical throwable
identity, no rejected evidence, and restored interrupt state; and separately
show malformed/over-node/over-depth/over-byte retained bytes become closed
rejected-entry evidence. Callback tests must show validator exceptions,
unsupported decoded values, and unmarked `ExceptionInfo` propagate unchanged.

## Diagnostic ownership and propagation

| Condition | Required behavior |
| --- | --- |
| Semantic-key node, depth, or byte overflow | Propagate `C16-KEY`; never convert it to capacity withholding. |
| Semantic-key unsupported type, metadata, or nonfinite scalar | Propagate its current key/validator diagnostic; never convert it. |
| Malformed, oversized, over-deep, or over-node retained entry, receipt, or blob | Remain a retained-data rejection with contained `C16-ENTRY`; never relabel it as envelope capacity. |
| Stale retained identities, receipts, boundary projections, or current bindings | Retain the current `C16-STALE` behavior. |
| Fresh C2 producer rejection | Propagate the owning C2/L1 diagnostic and create no cache candidate. |
| Fresh artifact validator rejection or exception | Propagate unchanged and create no cache candidate. |
| Fresh envelope unsupported value or unknown canonical form | Propagate unchanged; unsupported type is not capacity. |
| Store policy, unsafe path, permissions, provider, inventory, or admission failure | Propagate the owning `C16-POLICY`; it is not the closed envelope outcome. |
| Filesystem or other IO failure, including entry/receipt/blob read and recovery | Propagate the identical throwable; do not return rejected-entry or successful artifact evidence. |
| `InterruptedException`, closed-by-interrupt, `ThreadDeath`, or VM fatal error | Propagate, preserving interruption state where required. |
| Closed fresh-envelope `:nodes`, `:depth`, or `:bytes` outcome | Return the exact fresh artifact and contained local `C16-POLICY` withholding evidence. |

Direct `store!` is not a lookup-or-compute optimization and has no fresh-result
fallback. It continues to reject any envelope or publication capacity failure;
it must not return `:withheld` success. Store count and aggregate-byte admission
limits also continue to reject in both APIs because they are store policy, not
fresh-envelope representability.

## Bound failure provenance

The standalone artifact
`docs/artifacts/phase-06/reports/c2-pass-cache-capacity-withholding-sh03-failure-provenance-v1.md`
is a required input to this decision. It records the observed dirty-worktree
SH03 run at base `2178263120d81ea0a659218e6c2376bfde928b27`, its exact command,
21-test/983-assertion/1-error result, failing test and lines, 257161-byte source,
exception data, first-rejected-visit inference, unknown complete count, and the
recoverable source/test/encoder file digests. Its status is provisional because
the dirty worktree was not frozen at run time. This architecture binds that
limitation and does not promote the observation into implementation evidence.

A later implementation review must retain this historical record and add a
separate clean rerun receipt. Repeating the observed numbers without a clean
commit, tree, complete output, file digests, and measured complete census is not
stronger evidence.

## Authentic SH-03 acceptance gate

A later implementation is conforming only if its positive capacity fixture is
an authentic ordinary C2 product, not a hand-authored accepted map. The fixture
must:

- use a real byte-identical `.gravity`/`.qst` pair through the current
  Gravity-owned SH-03 reader and the ordinary uncached C2 producer;
- preserve the current `:gravity/sh03-to-c2-reader-products-v2` adapter,
  `:SH-03` slice, `:gravity-source` owner, exact current plan binding, accepted
  resolved-reader result, authenticated SH-02 descriptor envelope, semantic
  value-table binding, and `:target-source-reread? false`;
- preserve the C2 artifact kind, `P06-D081` task, `C2` document set, pass
  contract, exact source-unit identity, reconstructed source bytes, source
  spans, raw literal facts, trivia, diagnostics, incremental hashes, integrity
  record, boundary projection, and artifact id;
- keep `:clojure-adapter-residual? true`, `:self-hosted? false`, and every local
  cache authority field false beyond local speculative reuse; and
- prove with the existing reader-oracle tripwires that no target source reread,
  Clojure reader oracle, or alternate producer was invoked.

The direct uncached and capacity-withheld paths must return the same authentic
accepted artifact for both extensions. A mutation or substitution of the
source unit, reader policy, plan binding, authenticated envelope, semantic
value table, incremental hash, artifact id, or actual-path provenance must fail
the current validator and must not be converted to withholding.

The implementation evidence must additionally exercise the three closed
dimensions at their exact limit and first-plus-one with independently validated
C2-shaped values. At least the discovered production blocker must be reproduced
with the authentic SH-03 route above. Synthetic values may test the other two
accounting dimensions but confer no C2 or SH-03 authority.

These requirements preserve the repository Uniform Slice Gate: real paired
input, stable structured failures with actual path, retained facts and
provenance, deterministic path-neutral identity, mutation/substitution checks,
focused regressions, residual-host disclosure, and no broader claim.

## Duplication audit and owned implementation seam

The repository contains two layers with similar names, so implementation
review must prove that it changed the live path rather than a dormant analogue:

- `gravity.c2-pass-cache` retains C2-specific bounded key and opaque artifact
  encoding. Its semantic-key API remains authoritative for C2 cache key shape.
- `gravity.c2-pass-cache.publication` contains the frozen legacy-v1 helpers.
  The active public storage functions are the later definitions in
  `gravity.c2-pass-cache.adapter-projection`; v1 bytes are forensic-only.
- `gravity.c2-pass-cache.adapter-request` constructs the live opaque C2 v2
  envelope and bridges the exact C2 producer and validator into the generic
  pass execution contract.
- `gravity.c2-pass-cache.adapter-projection` owns the live C2 public
  `lookup!`, `store!`, and `lookup-or-compute!` compatibility projection.
- `gravity.pass-cache.execution` owns generic lookup/compute/publication
  orchestration, `gravity.pass-cache.lookup` owns the two live lookup catch
  boundaries, `gravity.pass-cache.policy` owns the closed propagation
  classifier, `gravity.pass-cache.receipt-validation` owns the split retained
  and output validator roles, and `gravity.pass-cache.publication` owns direct
  storage and atomic blob/receipt/entry publication.
- The C2 and generic canonical encoders have different roles and bounds. No
  implementation may solve this decision by raising either global bound,
  weakening key accounting, reinterpreting v1, or making all generic cache
  clients swallow capacity failures.
- The compiler integration in the C2 bootstrap component owns authentic SH-03
  revalidation. A cache leaf cannot replace or weaken it.

The narrow successor may change only the live C2 adapter; the minimum bounded
canonical-accounting seam needed to mint the closed phase-tagged outcome; the
generic `policy.clj`, `lookup.clj`, `receipt_validation.clj`, `execution.clj`,
and `publication.clj` seams specified above; and focused C2/generic cache tests.
It must demonstrate unchanged behavior for every non-C2 generic client.
Touching the SH-03 reader, reader contracts, compiler artifact semantics,
release paths, SH-07 sources, Attempt-32 records, or unrelated cache clients
requires a separate decision.

Implementation review must include an `rg`-based definition/call-site audit,
prove that the public facade resolves to the changed live functions, and show
that dormant legacy tests/helpers did not provide the passing evidence.

## Alternatives rejected

- Raising global node, depth, byte, file, or store bounds hides the distinction
  between a valid C2 product and optional cache representability.
- Catching every `C16-KEY` or `C16-POLICY` would swallow semantic-key, path,
  provider, admission, and unsupported-type failures.
- Encoding a digest, truncated product, summary, handle, or alternate DAG in
  place of the fresh artifact would return something other than the exact C2
  product and would change cache semantics.
- Publishing a partial envelope, capacity receipt, rejected entry, or staging
  file creates a false reusable candidate and complicates crash recovery.
- Applying the fallback to direct `store!` changes an explicit publication API
  into an implicit success path.
- Treating a cache miss as release, proof, equivalence, or self-hosting evidence
  violates the existing authority ceiling.

## Implementation acceptance criteria

A separately governed implementation may be accepted only when an independent
reviewer verifies one exact commit, tree, and evidence bundle and confirms:

1. the semantic key and lookup complete before the new lane;
2. an authentic fresh SH-03/C2 artifact passes the unchanged independent
   validator exactly once before any capacity outcome is recognized;
3. clean miss, rejected-entry miss, hit, and direct-store call-count tests prove
   exactly-once semantic validator ownership, envelope-only generic output
   validation, and no bypass or duplicate under concurrency;
4. only the exact closed `ExceptionInfo` node/depth/byte transport with strict
   `observed > maximum` and carrier-bound phase origin is caught;
5. the exact fresh artifact is returned without envelope round-trip or semantic
   rewriting, with complete contained local `C16-POLICY` evidence;
6. clean-miss and rejected-entry capacity evidence match their separate exact
   schemas, retaining rejected-entry evidence only in the latter;
7. the capacity path creates no candidate blob, producer receipt, entry,
   staging file, or on-disk capacity evidence;
8. key overflow remains `C16-KEY`, retained-data overflow/malformation remains
   `C16-ENTRY`, direct store rejects, and validator, unsupported-type, IO,
   interruption, and fatal failures propagate;
9. injected entry/receipt/blob/recovery IO and closed-by-interrupt failures
   propagate identical throwable objects with interrupt state preserved, while
   only marker-bound retained corruption becomes rejected-entry evidence;
10. at-limit and first-plus-one node/depth/byte cases, real paired SH-03 input,
   artifact identity, metadata/provenance equality, mutation/substitution,
   concurrency, corrupted retained entry, and disk-inventory assertions pass;
11. the provisional historical SH03 provenance remains bound and a separate
   clean rerun receipt supplies the stronger evidence it explicitly lacks;
12. the duplication audit proves the live adapter and generic call sites were
   exercised without weakening other cache clients; and
13. focused tests, relevant regressions, document and roadmap validation,
   language-boundary validation, governance validation, and `git diff --check`
   pass from a clean named-branch worktree.

## Relationship to SH-07 Attempt 32

Integrated SH-07 Attempt 32 is a governance precedent for bounded accounting,
explicit residual host boundaries, and strict nonclaims. It is not a semantic,
artifact, code, or authority dependency of this C2/C16 decision. This report
therefore records no Attempt-32 dependency and grants no SH-07 authority.

## Residual boundaries and nonclaims

Clojure and the JVM remain the temporary source-byte loader, SH-03/C2 adapter,
validator host, canonical encoder, digest implementation, local filesystem and
locking host, and test observer. The local filesystem provider and same-user
mutation exclusions remain those of the existing pass-cache contract.

This report does not implement the behavior, accept any implementation, publish
or integrate a candidate, prove C2 or C16 conformance, complete SH-03, SH-07, or
any roadmap slice, retire the Clojure seed boundary, or establish cache,
release, proof, equivalence, performance, safety, self-hosting, or
seed-retirement authority. It authorizes only independent review of this exact
architecture tuple and, after separate acceptance, at most one separately
governed implementation candidate.
