# C2 Pass-Cache Capacity Withholding Independent Review V1

## Decision

REJECT the exact frozen implementation candidate. Changes are requested before
another freeze or review.

The reviewed tuple is:

- base commit: `e4b85b7a4f7bbf1e2f8287ec8052240ffa106f50`;
- candidate commit: `c8f373fb59509c4ea03339b51bdbb960c82b64cd`;
- candidate tree: `ca080ee42557232f3a8a938ddb5dac92bb9a512a`;
- implementation evidence:
  `docs/artifacts/phase-06/reports/c2-pass-cache-capacity-withholding-implementation-evidence-v1.md`;
- implementation evidence SHA-256:
  `0fc613afacd85a4e9e22339ec18aaa53effd25f8ca4c28b36c6bd24ed7a32e77`;
- review-pending governance HEAD:
  `d803bcb7180f2d39a6b90d19f76a21714d18efff`.

The candidate worktree was clean. The three commits after the candidate changed
only the active workstream shard and ledger. The source and tests at governance
HEAD are byte-identical to the frozen candidate. This review did not modify the
implementation, accept or integrate the candidate, publish any result, or
touch SH-07.

Reviewer identity:
`c2-cache-implementation-reviewer-v1`, canonical task
`/root/c2_cache_impl_review`.

## Blocking Finding: Requested-Hit Validation Does Not Govern The Returned Object

The integrated architecture requires a fully revalidated requested hit to be
returned unchanged and requires proof that no requested artifact bypasses its
semantic validation.

The candidate does not preserve that invariant:

1. `adapter_projection.clj` lines 83-92 handles
   `:validate-requested-retained-artifact!` by decoding the envelope, invoking
   the C2 semantic validator on decoded artifact A, discarding both A and the
   validator's return value, and returning only the generic envelope.
2. `project-adapter-result` at lines 129-150, especially lines 138-142, sees a
   hit and decodes the envelope again with semantic validation disabled. It
   returns newly decoded artifact B.
3. Artifact B is equal to and has the same declared identity as artifact A, but
   it is not the object that passed the requested-hit semantic validator.

A read-only focused probe against the frozen source observed:

```clojure
{:status :hit,
 :validated-identical-to-returned false,
 :validated-equals-returned true,
 :ids ["sha256:a349e0410ab0745c68d37bbf4f3d047459d94faf3a441fc4657014cab409a0c4"
       "sha256:a349e0410ab0745c68d37bbf4f3d047459d94faf3a441fc4657014cab409a0c4"]}
```

This violates exact-operation-order step 2 and the requested-artifact
no-bypass obligation in the integrated architecture report. It also differs
from the legacy C2 lookup behavior, which returns the validator-produced
artifact.

Required repair: retain the exact successfully validated requested-hit artifact
through the adapter invocation and return that object without a second
unvalidated decode. Add a regression assertion that the object observed by the
requested-hit semantic validator is `identical?` to the public returned hit.

## Blocking Evidence Gap: Validator Calls Are Not Tagged

The architecture requires tests to tag semantic-validator calls by validation
role, requested cache-key id, and artifact id. It requires the tags to prove
the requested artifact is validated once on clean miss, rejected-entry miss,
valid hit, and direct store; zero times for semantic-key failure; without
bypass or duplication under concurrency; and independently from any recovery
artifact validation.

The frozen tests use aggregate counters. In particular:

- `generic-v2-adapter-same-key-concurrency-executes-one-c2-producer` counts two
  validations but does not establish which call is the fresh role and which is
  the requested-hit role, nor which key/artifact each call owned;
- the node/depth/byte, rejected-entry, and direct-store capacity tests count
  calls but do not record role, requested cache-key id, or artifact id;
- no assertion distinguishes requested-hit validation from recovery validation
  using the required tags.

These tests therefore cannot prove implementation acceptance criterion 3 and
did not expose the object substitution above.

Required repair: record and assert a closed validation trace containing the
role, requested cache-key id, and artifact id for every relevant path,
including same-key concurrency and a fixture with a distinct recovery artifact.

## Catch-Scope Disposition

The generic `try` wraps the complete `pass-execution/execute-pass!` call, which
includes the producer callback, envelope output validation, and receipt
construction. It does not wrap lookup or publication. This is broader than the
literal producer callback but is required to observe the new envelope-capacity
exception, which arises in output validation after `:produce!` returns.

This is not a separate rejection blocker. The handoff accepts only the exact
invocation-registered exception identity and otherwise rethrows the received
object. Focused tests confirmed unchanged propagation for validator,
unsupported-value, I/O, interruption, and closed-by-interrupt failures. Fatal
`ThreadDeath` and `VirtualMachineError` are rethrown before the handoff.

## Verified Nonblocking Properties

Independent source and test inspection confirmed these properties apart from
the requested-hit blocker:

- semantic-key construction and receipt-first lookup precede the capacity
  lane;
- fresh semantic validation occurs before carrier construction and requires
  the validator to return the identical artifact;
- the operation schema is closed, requires separate recovery, requested-hit,
  and envelope-output functions for the C2 split, and preserves ordinary
  generic clients;
- token, carrier, capacity exception, and handoff wrapper are invocation-local
  and checked with exact object identity and exact field sets;
- capacity resources are closed to canonical nodes, depth, and bytes, with
  positive integer bounds and strict `observed > maximum`;
- clean-miss and rejected-entry withholding use distinct public schemas and the
  rejected path preserves its prior generic evidence and corrupt bytes;
- direct store has no capacity handoff and propagates the registered capacity
  exception;
- capacity exits before generic blob, receipt, entry, or staging publication;
- successful generic publication retains its lock, immutable-CAS, fsync, and
  post-read verification path;
- unsupported values remain `C16-KEY`, retained decode failures remain
  `C16-ENTRY`, and unrelated exceptions are not translated;
- the fresh success and capacity paths return the exact carrier artifact;
- invocation state has no static registry or asynchronous retention path and
  becomes unreachable with its per-call closures; exact JVM collection timing
  is not claimed;
- active storage remains generic V2, legacy V1 bytes remain forensic-only, and
  authority remains local, speculative, and non-authoritative.

The security-specific diff review found no separate attacker-reachable
vulnerability. The blocking requested-hit issue is an implementation-contract
and validation-ownership failure.

## Validation Receipts

The following commands were rerun from the clean review-pending worktree:

```text
Focused C2 capacity matrix
3 tests, 81 assertions, 0 failures, 0 errors

Complete gravity.pass-cache-test namespace
32 tests, 192 assertions, 0 failures, 0 errors

Complete gravity.c2-pass-cache-test namespace
14 tests, 185 assertions, 0 failures, 0 errors

clojure -M tools/validate_architecture_authority.clj \
  --report docs/artifacts/phase-06/reports/c2-pass-cache-capacity-withholding-architecture-v1.md
passed

clojure -M tools/validate_workstream_governance.clj
passed: 147 workstreams

git diff --check \
  e4b85b7a4f7bbf1e2f8287ec8052240ffa106f50..c8f373fb59509c4ea03339b51bdbb960c82b64cd
passed
```

The 1,658.50-second authentic paired SH-03 command was not rerun. Its complete
executable form and receipts were inspected. The candidate's referenced reader
source is 257,161 bytes with SHA-256
`bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77`,
matching the evidence report. The report records byte-identical `.gravity` and
`.qst` inputs, complete artifacts of 8,256,942 canonical nodes and depth 24,
the first node excess `524289 > 524288`, matching direct and returned artifact
ids per extension, and zero candidate files. This remains credible positive
capacity evidence but cannot cure the requested-hit validation blocker.

## Authority And Next Review

This review rejects only the exact tuple named above. It grants no integration,
publication, C2 or C16 conformance, SH-03 or SH-07 completion, proof,
equivalence, release, self-hosting, performance, safety, roadmap, or
seed-retirement authority.

A repaired implementation must receive a new exact candidate commit, tree,
evidence digest, clean validation receipts, and fresh independent review.
