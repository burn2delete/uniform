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
2. Perform the normal receipt-first local lookup. A fully revalidated hit is
   returned unchanged and the reader is not executed.
3. On a miss or retained-entry rejection, execute the ordinary fresh C2
   producer while holding the existing per-key cooperative lock.
4. Independently validate the fresh C2 artifact against the exact current key,
   producer binding, C2 pass contract, and SH-03 authenticated boundary before
   attempting to encode or account for a cache envelope.
5. Preserve the exact validated value in memory. Envelope construction and
   generic producer-receipt construction may inspect it but may not replace,
   normalize, summarize, truncate, or round-trip the return value.
6. Attempt the bounded cache-envelope preflight. Only the closed capacity
   outcome defined below can branch to capacity withholding.
7. On envelope success, continue through the unchanged generic execution,
   receipt, admission, and atomic publication path.
8. On the closed capacity outcome, skip every blob, receipt, entry, and staging
   publication operation; return the exact preserved fresh artifact with the
   contained local evidence defined below.

The capacity branch occurs before any candidate bytes or staging file are
created. Existing store directories and lock files are outside the candidate
artifact set and may already exist, but no blob, receipt, entry, temporary
stage, or on-disk capacity receipt may be created for the withheld candidate.

## Closed envelope-capacity outcome

The implementation may recognize exactly one internal closed outcome shape:

```clojure
{:artifact :gravity/c2-pass-cache-envelope-capacity-outcome
 :schema-version 1
 :stage :c2-reader
 :operation :local-cache-envelope-publication
 :value-role :fresh-validated-c2-artifact
 :dimension one-of-[:nodes :depth :bytes]
 :maximum positive-integer
 :observed positive-integer
 :semantic-key-id sha256
 :storage-key-id sha256
 :c2-artifact-id sha256}
```

The map has exactly these keys. `:observed` is the first violating node or byte
count, or the actual violating depth. The outcome is minted only by bounded
accounting of the cache envelope for the already validated fresh artifact. It
is not minted by semantic-key canonicalization, source snapshotting, retained
entry/receipt/blob decoding, artifact validation, store inventory, filesystem
admission, or an arbitrary canonicalization request.

The catch boundary matches the complete typed map, not an exception message,
exception class, or a generic `C16-KEY`/`C16-POLICY` identifier. Unknown keys,
unknown dimensions, nonpositive bounds, mismatched key or artifact identities,
and outcomes originating outside the fresh-envelope phase propagate as their
owning failure.

This is a resource-policy result, not evidence that the C2 artifact is
malformed. Its externally contained diagnostic is `C16-POLICY`.

## Local withholding evidence

The public C2 compatibility projection returns the existing local evidence
family with a closed capacity extension equivalent to:

```clojure
{:artifact :gravity/c2-local-pass-cache-evidence
 :schema-version 1
 :stage :c2-reader
 :semantic-key-id sha256
 :storage-key-id sha256
 :status :miss
 :reason :fresh-artifact-envelope-capacity
 :contained-diagnostic "C16-POLICY"
 :contained-capacity envelope-capacity-outcome
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

The result's artifact field is the exact fresh validated object retained before
envelope construction. Tests must establish value, metadata, identity, source
provenance, diagnostic stream, and SH-03 boundary equality with the direct
uncached result, and use an identity tripwire where the host representation
permits it. The evidence is returned in memory only. It is not a cache entry,
producer receipt, proof, equivalence report, release record, or bootstrap
provenance artifact.

The existing corruption path remains separate: a rejected retained entry is
kept as evidence and its fresh result is returned with publication withheld as
already specified. Capacity evidence must not overwrite, relabel, or hide that
retained-entry evidence.

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
| Filesystem or other IO failure | Propagate the contained owning failure; do not return a successful artifact result from this architecture lane. |
| `InterruptedException`, closed-by-interrupt, `ThreadDeath`, or VM fatal error | Propagate, preserving interruption state where required. |
| Closed fresh-envelope `:nodes`, `:depth`, or `:bytes` outcome | Return the exact fresh artifact and contained local `C16-POLICY` withholding evidence. |

Direct `store!` is not a lookup-or-compute optimization and has no fresh-result
fallback. It continues to reject any envelope or publication capacity failure;
it must not return `:withheld` success. Store count and aggregate-byte admission
limits also continue to reject in both APIs because they are store policy, not
fresh-envelope representability.

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
  orchestration, while `gravity.pass-cache.publication` owns direct storage and
  atomic blob/receipt/entry publication.
- The C2 and generic canonical encoders have different roles and bounds. No
  implementation may solve this decision by raising either global bound,
  weakening key accounting, reinterpreting v1, or making all generic cache
  clients swallow capacity failures.
- The compiler integration in the C2 bootstrap component owns authentic SH-03
  revalidation. A cache leaf cannot replace or weaken it.

The narrow successor may change only the live C2 adapter, the minimum generic
canonical-accounting seam needed to mint the closed phase-tagged outcome, and
focused C2/generic cache tests. Touching the SH-03 reader, reader contracts,
compiler artifact semantics, release paths, SH-07 sources, Attempt-32 records,
or unrelated cache clients requires a separate decision.

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
   validator before any capacity outcome is recognized;
3. only the exact closed node/depth/byte outcome is caught;
4. the exact fresh artifact is returned without envelope round-trip or semantic
   rewriting, with complete contained local `C16-POLICY` evidence;
5. the capacity path creates no candidate blob, producer receipt, entry,
   staging file, or on-disk capacity evidence;
6. key overflow remains `C16-KEY`, retained-data overflow/malformation remains
   `C16-ENTRY`, direct store rejects, and validator, unsupported-type, IO,
   interruption, and fatal failures propagate;
7. at-limit and first-plus-one node/depth/byte cases, real paired SH-03 input,
   artifact identity, metadata/provenance equality, mutation/substitution,
   concurrency, corrupted retained entry, and disk-inventory assertions pass;
8. the duplication audit proves the live adapter and generic call sites were
   exercised without weakening other cache clients; and
9. focused tests, relevant regressions, document and roadmap validation,
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
