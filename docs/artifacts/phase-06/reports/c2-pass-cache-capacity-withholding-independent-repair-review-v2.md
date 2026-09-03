# C2 Pass-Cache Capacity Withholding Independent Repair Review V2

## Decision

ACCEPT the exact repaired implementation candidate for its governed C2 local
pass-cache capacity-withholding scope.

The reviewed tuple is:

- base commit: `e4b85b7a4f7bbf1e2f8287ec8052240ffa106f50`;
- candidate commit: `85e051e2bf878a1c20bdad88b9360f2419d3616c`;
- candidate tree: `a9d0db1b34bd22f2b02be22efe4b00ddfc2d9ab6`;
- implementation evidence:
  `docs/artifacts/phase-06/reports/c2-pass-cache-capacity-withholding-implementation-evidence-v1.md`;
- implementation evidence SHA-256:
  `c857dde5193625a18d0998a3597d5d2af54bb257c56b595f072d54a36ecd1081`;
- review-pending governance HEAD:
  `f29288bee919b53a585901f81caa87f9f40e6285`.

The candidate worktree was clean before review. The three commits after the
candidate alter only the active workstream shard and compact ledger. Candidate
source, tests, and evidence at governance HEAD are therefore the exact frozen
tuple above.

Reviewer identity:
`c2-cache-identity-repair-reviewer-v2`, canonical task
`/root/c2_identity_repair_review`.

This review did not change candidate source, tests, or implementation evidence,
perform integration, or touch SH-03 or SH-07 implementation source.

## Resolution Of The Prior Rejection

The prior independent review remains an authoritative REJECT of only candidate
`c8f373fb59509c4ea03339b51bdbb960c82b64cd`, tree
`ca080ee42557232f3a8a938ddb5dac92bb9a512a`, and evidence digest
`0fc613afacd85a4e9e22339ec18aaa53effd25f8ca4c28b36c6bd24ed7a32e77`.
This repaired tuple closes both findings from that report:

1. Requested-hit semantic validation retains the exact decoded artifact object
   in invocation-local state. Public hit projection returns that object rather
   than decoding the envelope again. The focused tests prove that the object
   observed by `:requested-retained-artifact` validation is `identical?` to the
   public returned hit.
2. The private validation test seam records validation role, requested cache-key
   id, validated cache-key id, artifact id, and object identity. Clean miss,
   requested hit, direct store, rejected-hit fallback, distinct recovery, and
   same-key concurrency now have explicit role-bound assertions.

An equal-but-distinct value returned by the semantic validator is rejected with
`C16-STALE`. A retained artifact mutated after successful semantic validation
is re-encoded and rechecked before projection and is also rejected with
`C16-STALE`. These tests directly cover the failure mode missed by the prior
aggregate counters.

## Closed Requested-Hit Binding

Source inspection confirmed that one hit binding is created only after the
requested receipt, entry, blob, envelope, artifact identity, authenticated
boundary, and C2 semantic checks have succeeded. Before returning the hit, the
adapter rechecks:

- the invocation-local token by JVM object identity and the exact operation;
- the exact generic key, entry, envelope, and payload-byte objects;
- requested, semantic, and storage cache-key identities;
- entry, producer-receipt, producer-binding, and validation-binding identities;
- requested, entry, receipt, reuse, envelope, and currently recomputed artifact
  identities;
- the currently recomputed authenticated boundary identity;
- the envelope carrier and payload identities; and
- byte equality between the retained payload and a fresh canonical encoding of
  the exact validated artifact.

The generic layer continues to carry the envelope object required by its cache
contract. The exact C2 object is retained only in the private adapter state and
is substituted into the compatibility result after all generic hit checks.
Neither the object reference nor the invocation token is serialized, persisted,
accepted from a caller, or exposed in public evidence.

The binding is cleared before every fresh producer call and whenever projection
receives a non-hit. Thus a requested validation that succeeds before a later
entry, receipt, or current-binding rejection cannot lend its object to the
fresh fallback. The focused stateful identity test forces precisely that order
and confirms that only the fresh carrier object escapes.

Each public adapter invocation constructs its own token, atom, closures, and hit
binding. No static registry or asynchronous retention path exists. The
same-key concurrency test executes one producer, then one independent hit
validation, with distinct artifact objects and an identity-preserved hit result.
Exact JVM garbage-collection timing is not claimed.

## Capacity, Publication, And Failure Semantics

The repair does not widen the previously reviewed capacity lane. Independent
inspection and the fresh focused run confirm:

- semantic key construction and normal receipt-first lookup precede production;
- fresh and direct-store C2 semantic validation happens exactly once before
  carrier or envelope construction;
- generic output validation is envelope-only and cannot satisfy requested-hit
  validation;
- only the exact invocation-registered capacity exception and exact registered
  handoff wrapper can produce withholding;
- token identity, carrier identity, closed resource enum, positive maximum,
  strict `observed > maximum`, key identities, and artifact identity are
  rechecked at both handoff boundaries;
- clean-miss and rejected-entry withholding retain their distinct exact public
  evidence schemas;
- the rejected-entry variant preserves the prior closed rejection evidence and
  corrupt bytes;
- capacity exits before candidate blob, receipt, entry, or staging publication;
- direct `store!` rejects capacity and has no withholding success path; and
- validator, unsupported-value, I/O, interruption, closed-by-interrupt, fatal,
  key, retained-entry, store-policy, and other unrelated failures cannot be
  recognized by the new identity-bound catches.

The node, depth, and byte fixtures exercise the exact limit and first-plus-one.
Exact-limit artifacts publish normally. First-plus-one artifacts return the
identical prevalidated fresh object and leave all candidate inventories empty.

## Generic Compatibility And Live-Path Audit

The public C2 facade loads `requested_hit_binding.clj` immediately before the
live adapter projection. Definition and call-site inspection found one active
definition for each new binding helper. Public C2 `lookup!`, `store!`, and
`lookup-or-compute!` resolve to the changed adapter; dormant legacy-v1
publication helpers remain forensic-only.

The generic operation schema requires the three distinct validation roles only
when the C2 private capacity handoff is present. Existing non-C2 clients retain
their previous exactly-one-validator contract. The generic pass-cache namespace
passed unchanged, covering ordinary lookup, recovery, locking, atomic
publication, and operation-map compatibility.

No `lookup.clj`, `recovery.clj`, `retention_scan.clj`, `path_policy.clj`, SH-03
reader, SH-07 source, release path, or unrelated cache-client source changed in
the repair.

## Evidence Review

The clean evidence report is internally consistent with the exact source
checkpoint. Every recorded source digest matches the reviewed files. The
integrated architecture report, historical SH-03 provenance report, and
architecture terminal record also match their recorded SHA-256 identities.

The authentic paired command was not repeated during this independent review;
its executable form, assertions, input source, result table, and limitations
were inspected. The checked-in `reader.gravity` input is 257,161 bytes with
SHA-256
`bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77`,
matching the report. The recorded clean run copied those bytes to temporary
`.gravity` and `.qst` paths, used the ordinary uncached and cached SH-03/C2
routes under reader-oracle tripwires, measured the complete 8,256,942-node,
depth-24 census, observed the exact first node excess `524289 > 524288`, and
checked the actual generic-v2 cache root for zero blobs, receipts, entries, and
staging files. Different extension paths correctly produced different artifact
ids because actual-path provenance is identity-bearing. These are honest
implementation receipts, not proof, equivalence, performance, or self-hosting
evidence.

The global LOC gate remains red only for three pre-existing, untouched files:
`authenticated_artifact_pipeline.clj` at 1,069 lines and SH-05/SH-06 components
`00866` and `00880` at 373 and 372 lines. Every changed production source file
is within the 250-line limit. The implementation report records this as an
exclusion rather than a passing global result.

## Fresh Review Receipts

The following commands were run from the clean review-pending worktree:

```text
Focused requested-hit repair and capacity matrix
6 tests, 133 assertions, 0 failures, 0 errors

Complete gravity.c2-pass-cache-test namespace
16 tests, 231 assertions, 0 failures, 0 errors

Complete gravity.pass-cache-test namespace
32 tests, 192 assertions, 0 failures, 0 errors

gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, 0 failures, 0 errors

validate_architecture_authority.clj
passed

validate_gravity_docs.clj
passed: 240 documents, 19 phase indexes

validate_full_language_roadmap.clj
passed

validate_workstream_governance.clj
passed: 147 workstreams

git diff --check
e4b85b7a4f7bbf1e2f8287ec8052240ffa106f50..
85e051e2bf878a1c20bdad88b9360f2419d3616c
passed
```

The proportional preflight confirmed a clean named branch but reports the
candidate lineage and current main as divergent. Current main at review time is
`f88c8094d4150bc5cd3488f598a3d8617e1b7018`; the exact implementation candidate
is 11 commits ahead and 17 commits behind. Its recommendation is
`resolve_divergence_before_integration`.

## Acceptance Scope And Remaining Blocker

This review accepts only the exact tuple named above. It does not make that
tuple integration-eligible. Reconciliation onto authoritative main, a new exact
candidate/tree if content changes, proportional reruns, and fresh independent
review of any changed implementation tuple remain required before integration.

Acceptance grants no C2 or C16 language conformance, SH-03 or SH-07 completion,
proof, equivalence, release, self-hosting, performance, safety, roadmap, or
seed-retirement authority. Clojure and the JVM remain the adapter, validator,
canonical traversal, exception-identity, filesystem, locking, and observer
boundary. The cache remains local, speculative, and non-authoritative.
