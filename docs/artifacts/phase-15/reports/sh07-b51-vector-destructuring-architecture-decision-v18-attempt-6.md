# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 6

Status: Draft correction for independent review

## Purpose

This decision corrects one contradiction in the independently accepted and
integrated attempt-5 architecture. Attempt 5 requires three generated legacy
references inside the Tier-3 core-identity product and retains the exact static
legacy controlled-path census of 94, but its separately declared literal and
exhaustive purpose-dependency catalog omits two of those references. An exact
implementation must therefore either reject the required legacy product or
violate the catalog. No 56-edge interpretation conforms to both requirements.

The correction is deliberately narrow. It adds the two omitted, branch-typed
purpose dependencies and makes the static catalog exactly 58 edges. It does not
add a purpose, request, field, schema, tier, batch, ordinal, semantic outcome,
or implementation feature.

This is an architecture-only draft. It does not edit or accept an
implementation, advance SH-07, or confer integration, self-hosting, release,
performance, or seed-retirement authority.

## Normative baseline and precedence

The incorporated baseline is the exact attempt-5 report:

```text
report
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-5.md
report candidate
39281cdb48a05efc42eae8d22ea44934e9d32f60
report tree
1cc228b13dc5f65ffb8d5a64cd3f7b93a4d870c6
report SHA-256
725deaea624b5bf87898b8512cd30be9e9323f66521683c0aea764560d23f689
integrated lifecycle commit
a14f10aa1d85b58bf481272a9008acf9c8f43431
integrated workstream id
sh07-b51-vector-destructuring-architecture-v18-attempt-5
```

Attempt 5 remains immutable integrated history. This report incorporates its
architecture by reference and replaces only the clauses listed under
`Exact correction`, `Count and evidence corrections`, `Governed implementation
baseline`, and `Independent acceptance criteria`. If an incorporated attempt-5
sentence conflicts with one of those replacements, this attempt-6 sentence
governs. Every other attempt-5 architecture requirement remains unchanged.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C5`, `C6`, `BOOT7`, `BOOT8`, `TEST10`, `TEST11`, `TEST13`,
`docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Confirmed contradiction

Attempt 5 normatively declares this exact legacy generated path vector:

```text
legacy-generated-reference-paths :=
[[:legacy-v16-equivalence :same-request-semantic-id]
 [:legacy-v16-equivalence :predecessor-outcome-semantic-id]
 [:legacy-v16-equivalence :v16-verification-semantic-id]]
```

The attempt-5 target-purpose registry types those three generated terminals as
`:sh07-b51-same-request-semantic-id`,
`:sh07-b51-predecessor-outcome-semantic-id`, and
`:sh07-b51-v16-verification-semantic-id`, respectively. The same report makes
all three paths part of the selected `:legacy-v16-accepted` Tier-3 product and
requires the exact static legacy census:

```text
{:controlled-request-paths 94
 :non-tier3-paths 46
 :tier3-paths 48
 :tier3-top-level-paths 2
 :tier3-generated-product-paths 46}
```

The separate attempt-5 `batch-dependency-catalog` calls itself literal and
exhaustive, yet the `:sh07-b51-core-identity-id` row includes the same-request
dependency and omits the outcome and v16-verification dependencies. Its claimed
56-edge total therefore describes neither the required legacy preimage nor the
typed path registry. Treating either omitted field as concrete is also
forbidden: both are generated internal references, while only the inherited
v16 semantic artifact id is concrete on the legacy route.

Both omitted targets are already valid earlier requests. V16 verification is
batch rank 3, predecessor outcome is batch rank 4, and core identity is batch
rank 14. Adding the edges creates no forward, equal-coordinate, same-batch, or
cyclic dependency.

## Exact correction

In the incorporated attempt-5 `batch-dependency-catalog`, replace only the
`:sh07-b51-core-identity-id` value with this exact value:

```text
:sh07-b51-core-identity-id
[:sh07-b51-same-request-semantic-id
 {:accepted-only :sh07-b51-v16-verification-semantic-id}
 {:accepted-only :sh07-b51-predecessor-outcome-semantic-id}
 :sh07-b51-predecessor-authority-id
 {:non-v16-only :sh07-b51-core-node-id}
 :sh07-b51-product-node-id
 :sh07-b51-binding-slot-id
 :sh07-b51-binding-extraction-id
 :sh07-b51-runtime-check-id
 :sh07-b51-publication-event-id
 :sh07-b51-recur-slot-mapping-id
 :sh07-b51-slot-extraction-transcript-id]
```

The two added static typed edges, written as
`[source-purpose target-purpose branch-selector]`, are exactly:

```text
[[:sh07-b51-core-identity-id
  :sh07-b51-v16-verification-semantic-id
  :legacy-v16-accepted]
 [:sh07-b51-core-identity-id
  :sh07-b51-predecessor-outcome-semantic-id
  :legacy-v16-accepted]]
```

`:accepted-only` means exactly `:legacy-v16-accepted`, matching attempt 5's
closed branch vocabulary. Neither edge exists on
`:b51-vector-frontier-rejected`,
`:legacy-v16-accepted-after-no-form-waiver`, or
`:b51-vector-frontier-rejected-after-no-form-waiver`. Changing either selector
to `:both`, `:non-v16-only`, or another branch value is `C6-VERIFY`.

The order in the corrected row follows the target batch ranks: same request 2,
v16 verification 3, predecessor outcome 4, authority 5, optional non-v16 core
node 6, and the unchanged later product purposes. This row is a static
purpose-level dependency summary. It does not replace the separately ordered
preimage path traversal.

## Count and evidence corrections

The following attempt-5 values remain exact and unchanged:

- 19 success purposes in one-purpose batches ranked 0 through 18;
- 94 static controlled path descriptors for `:legacy-v16-accepted`;
- 174 static controlled path descriptors for each non-v16 outcome;
- 46 accepted non-Tier-3 paths, 48 accepted Tier-3 paths, two accepted
  Tier-3 top-level paths, and 46 accepted Tier-3 generated product paths;
- zero generated ordinary-core request edges on the accepted legacy route;
- all request cardinalities, batch ordinals, subtier ordinals, and global
  ordinals; and
- all schemas, semantic projections, concrete inherited v16 artifact handling,
  diagnostics, resource bounds, and runtime behavior.

The following attempt-5 count clauses are replaced:

```text
old: static purpose-dependency edge count = 56
new: static purpose-dependency edge count = 58
```

Accordingly, the attempt-5 sentences that distinguish the controlled-path
census from 56 dependency edges, state that the static closed expansion has 56
purpose edges, or require producer and verifier to recompute all 56 now mean 58
in every case. The static edge total includes both branch-typed edges even
though they activate only for `:legacy-v16-accepted`.

In the unchanged purpose-catalog order, the exact static dependency-edge
cardinalities are:

```text
[0 0 0 0 4 4 2 1 2 2 4 5 7 7 12 1 2 2 3]
```

Their sum is 58. Only the core-identity entry changes, from 10 to 12; every
other per-purpose cardinality remains the attempt-5 value.

This change does not alter the 94-path census: the paths already existed in
attempt 5. It closes the catalog over those paths. A reviewer must reject any
candidate that changes 94 to 96, changes a Tier-3 path count, adds requests, or
uses the path census as the dependency-edge count.

The attempt-5 ledger evidence string claiming 56 dependencies is immutable
historical evidence for the accepted attempt-5 tuple. It is not evidence for
attempt 6 and grants no authority to retain 56 in an implementation. Attempt-6
evidence must state 19 purposes, 58 typed dependencies, legacy census 94, and
non-v16 census 174.

## Required positive and mutation evidence

The producer and independent verifier must mechanically reconstruct the exact
58-edge static dependency catalog and prove that every generated controlled
path has one exact target purpose. On the legacy route they must additionally
show both corrected edges resolving to the unique earlier requests at ranks 3
and 4. The existing zero-forward-edge, zero-equal-coordinate, zero-cycle, and
strict core-node postorder checks remain unchanged.

Positive evidence must bind all of the following in one result:

```text
success-purpose-count             19
static-purpose-dependency-edges   58
legacy-controlled-path-census     94
non-v16-controlled-path-census    174
missing-target-types              0
duplicate-target-types            0
forward-or-equal-coordinate-edges 0
cycles                            0
```

Mutation evidence must independently reject at least these cases with
`C6-VERIFY` before hashing or promotion:

1. retain the old 56-edge catalog;
2. delete the legacy core-identity to predecessor-outcome edge;
3. delete the legacy core-identity to v16-verification edge;
4. type either path to the other purpose, to same-request, or to authority;
5. make either edge unconditional or active on a non-v16 outcome;
6. duplicate either edge while retaining both paths;
7. convert either generated legacy field to a concrete digest;
8. change 94 to 92, 96, or another value to force agreement with the edge
   catalog; or
9. add a twentieth purpose, a new request, or a new ordinal to represent an
   already existing target request.

All attempt-5 mutation obligations remain in force. These cases extend the
catalog/path-consistency family; they do not replace predecessor closure,
schema, owner, external-binding, diagnostic, bounds, recur, runtime, provenance,
or atomic-stack mutations.

## Governed implementation baseline

This correction uses workstream id
`sh07-b51-vector-destructuring-architecture-v18-attempt-6` and preserves
invariant family
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
the exact integrated attempt-5 workstream and uses authoritative main commit
`a14f10aa1d85b58bf481272a9008acf9c8f43431` as its governance parent.

Attempt 6 must receive a fresh independent accepted review of an exact frozen
tuple, then advance separately through `accepted`, `integration-eligible`, and
`integrated`. Only its exact integrated main result establishes corrected
baseline `G6`. This draft and its author cannot establish G6.

The attempt-5 atomic implementation topology remains otherwise unchanged:

```text
G6 -> A (master-coordinator SH-06 vector-leaf conformance correction)
   -> C (sh-core v18 semantics, products, verifier)
   -> H (coordinator mechanical invocation and assembly)
```

For any implementation candidate governed after this correction, the one
atomic implementation ledger row must use:

```text
id               sh07-b51-v18-atomic-stack
invariant_family self-hosting/sh07-b51-v18-atomic-stack
base_commit      G6
dependencies     [sh07-b51-vector-destructuring-architecture-v18-attempt-6]
candidate        H only
```

This replaces attempt 5's `G`, architecture dependency id, governance-parent,
and draft-record instructions. A shortened id, the attempt-5 id, the attempt-4
id, a title, a report path, or a nonexistent row is invalid for the corrected
implementation candidate. A, C, and H remain internal reviewed checkpoints of
one atomic workstream and cannot land separately.

The existing implementation worktree is not modified or accepted by this
decision. Any implementation result produced under the attempt-5 56-edge
contract is ineligible. A later corrected candidate must descend from G6, bind
the exact attempt-6 dependency, implement and evidence the 58-edge catalog,
and receive fresh exact checkpoint and final independent review as required by
attempt 5. No source or implementation edit belongs in this architecture
candidate.

## Ownership, compatibility, and nonclaims

Attempt-5 ownership remains unchanged: `:master-coordinator` owns A and H;
`:sh-core` owns C. Clojure and the JVM remain the source reader, strict decoder,
SH-06/B47 host, plan executor, digest resolver, runtime-check host, and
observer. The verifier remains evidence and is not a proof of itself.

The correction is compatible within schema 18 because it adds no schema field
or request. It makes the exhaustive dependency catalog agree with the already
required schema-18 preimage. Tier-3 and dependent Tier-4 through Tier-6 digest
values are naturally candidate-specific and must be recomputed; no attempt-5
derived digest may be reused merely because schemas and path counts are
unchanged.

All attempt-5 nonclaims remain exact. In particular, this report does not claim
map/list/set/record/constructor/schema/resource or variable-width patterns,
defaults/rest/guards, parameter destructuring, general match coverage,
type/effect/ownership/safety completion, MIR/optimization, complete exception
semantics, public routing, aggregate SH-07 completion, self-hosting, release,
performance, or seed retirement.

## Independent acceptance criteria

An independent reviewer of a later exact frozen attempt-6 tuple must confirm:

1. The exact attempt-5 report tuple and its integrated lifecycle at `a14f10a...`
   are preserved as immutable history; attempt 6 neither rewrites nor
   self-rejects that terminal ledger row.
2. The contradiction is reproduced directly from the attempt-5
   `legacy-generated-reference-paths`, outcome-selected Tier-3 registry, typed
   target registry, 94-path census, and exhaustive dependency catalog. The
   reviewer confirms that no conforming 56-edge interpretation exists.
3. The corrected core-identity dependency row contains exactly the two added
   `:accepted-only` targets, in addition to every unchanged attempt-5 target,
   and the full static catalog contains exactly 58 typed edges over the same 19
   purposes.
4. Both added edges target unique earlier requests at batch ranks 3 and 4;
   neither creates a forward, equal-coordinate, same-batch, or cyclic edge.
5. The static controlled-path censuses remain exactly 94 and 174, including the
   exact attempt-5 Tier-3 subcounts. No purpose, request, ordinal, path, schema,
   semantic outcome, diagnostic family, or resource bound was added or removed.
6. Positive and negative evidence covers the exact count tuple and every new
   mutation above. Producer and independent verifier both fail closed on the
   old 56-edge catalog or either omitted/mistyped edge.
7. Every attempt-5 obligation not explicitly replaced by this report remains
   normative, including carrier closure, digest protocol, schemas, legacy
   equivalence, owner and external-binding authority, bounds, diagnostics,
   runtime/recur semantics, evidence, atomic topology, ownership, and
   nonclaims.
8. Corrected baseline G6 is established only after exact independent acceptance
   and integration of attempt 6. The atomic implementation dependency is
   exactly `sh07-b51-vector-destructuring-architecture-v18-attempt-6`, and any
   implementation must descend from G6 and receive fresh exact review.
9. The candidate contains architecture/report/governance changes only and does
   not mutate the implementation worktree or confer implementation authority.

The author may report validation results and defects but cannot accept this
decision, confer integration eligibility, establish G6, or accept any
implementation.
