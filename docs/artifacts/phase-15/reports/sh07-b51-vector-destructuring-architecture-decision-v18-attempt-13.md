# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 13

Status: Draft duplicate-priority restoration for pre-freeze audit

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds terminally rejected Attempt 12. It
preserves every Attempt-12 recur correction that passed independent review:

- the mandatory one-id mapping for all three reachable recur reasons;
- nearest-compatible selection scoped to greatest lexical depth;
- compatible ancestors at shallower depth remaining valid but unselected;
- a same-depth tie failing source integrity without emitting catalog-only
  ambiguous;
- combined wrong-arity plus non-tail raw selecting arity mismatch first;
- exact static selector-source evidence when authorized ids happen to be
  byte-equal;
- recur coordinate ownership anchored to authenticated recur source/root/
  fragment/definition branch and independent of target identity;
- Root 6 remaining success-only; and
- unary Root 8 remaining the exclusive Root-1 pending finalizer.

Attempt 12 was rejected for one unrelated preservation error. It claimed to
preserve the passed frozen Attempt-11 duplicate policy while printing duplicate
coordinate selector priority `[later, earlier]`. The exact frozen Attempt-11
row is `[earlier, later]`.

Attempt 13 restores that exact row. The duplicate detector still selects the
governed first duplicate event from authenticated pattern preorder. After that
event is fixed, the diagnostic coordinate selector priority is the earlier
occurrence as source followed by the later occurrence as evidence. Event
selection and coordinate selector priority are distinct. Attempt 13 makes no
new duplicate semantic policy change.

Every other integrated Attempt-10, passed Attempt-11, and passed Attempt-12
rule, count, topology, digest rule, Root-4/5 authority split, Root-6
disjointness rule, unary Root-8 rule, evidence obligation, and nonclaim remains
exact. This report contains no implementation, test, fixture, proof-contract,
source-pin, or whole-file-pin change.

## Normative baseline and terminal history

The authoritative base is:

```text
authoritative main commit
3624255adf586fc9d368d76335170393aece49c0

authoritative main tree
c45950b7a4cd71bcb9d4566d99afa49a2ae0785d

integrated Attempt-10 architecture candidate
3d780692e8023274c415a351b393ea8e1f9a796e

integrated Attempt-10 candidate tree
144cafd0f8833e35de52e29ea2f596376045ad72

integrated Attempt-10 report SHA-256
9eeb69c974636d08d2a6b1673a1fc2b523567ac27f4d6a8d9a14996874a760b9

Attempt-10 integration commit
ab516b370fa49b9cbfe7b2dcda6e696c0f8f624d

terminal Attempt-11 candidate/tree/report SHA-256
a29469c84e15ae3a55d2606421364c020c19571f
20433cc260cc6baa466a5667bcc06445155eca49
11c95a69638c7212832e07b1de8fa0ff163a8da8cdec87e0e1069397e60111f6

terminal Attempt-12 candidate/tree/report SHA-256
b9f025965baf786af80701defde036e9bae1b4ca
9f0f4376da77b1206dd3eae3c25837fecb980fcf
995cc70e06568f2863a5a4e071ecd6def162f9b23cc2e54ba196b7c1f6657e84

Attempt-12 terminal rejection / Attempt-13 base
3624255adf586fc9d368d76335170393aece49c0
```

Attempts 5, 6, and 10 remain immutable integrated authority. Attempts 7, 8,
9, 11, and 12 remain exact terminal rejection history and receive no
integration credit. Attempt 13 incorporates Attempt 12 by reference and
supersedes only its unauthorized duplicate priority change.

When incorporated language conflicts with this report, this report controls
only the restored duplicate coordinate selector priority printed below. It
does not change duplicate event selection, pending reason, reason count,
family, remediation, diagnostic key, schema version, public root, result tag,
purpose, dependency edge, controlled path, semantic outcome, digest algorithm,
recur selector, or finalization route.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Governed first duplicate event

The authentic duplicate detector is unchanged. For each admitted vector
pattern, Root 1 enumerates binding leaves in authenticated pattern preorder.
A duplicate event contains one repeated name, its first earlier occurrence,
and its first later occurrence:

```text
duplicate-event :=
{:binding-name symbol
 :earlier authenticated-duplicate-source-occurrence
 :later authenticated-duplicate-source-occurrence}
```

The earlier occurrence is the first preorder occurrence of the repeated name
within the selected pattern. The later occurrence is the first later preorder
occurrence that proves repetition. When multiple duplicate events exist, the
governed module-global first-offender reconstruction selects exactly one event.
The event is selected before any B51 success request or product exists.

This event detector is not the coordinate priority vector. It establishes the
pair. Once the pair is fixed, the coordinate source and evidence order are
governed separately by the restored row below. A detector implementation may
use the later occurrence to establish which event is the first offending
event; that does not authorize later-first coordinate priority.

No map iteration, hash order, binding-table recovery order, product emission,
fixture order, or host traversal participates. Both occurrences must join the
same authenticated pattern, fragment, owner, and binding name. Their paths are
distinct. A missing join, cross-pattern pair, cross-owner pair,
cross-fragment pair, reordered occurrence, different name, or unauthenticated
source value is `:source-integrity-mismatch`.

## Exact restored duplicate coordinate row

The exact frozen Attempt-11 row is:

```text
:duplicate-vector-binding-name
{:template :pattern-preflight
 :source [:authenticated-sh06-request :forms
          :selected-duplicate-event :earlier]
 :priority [[:authenticated-pattern-preorder
             :selected-duplicate-event :earlier]
            [:authenticated-pattern-preorder
             :selected-duplicate-event :later]]
 :expected :unique-vector-leaf-name
 :observed [:selected-duplicate-event :binding-name]
 :related-semantic-ids
 [[:selected-duplicate-event :earlier :form-id]
  [:selected-duplicate-event :later :form-id]]
 :remediation
 {:action :fix-source
  :owner :source-author
  :required-evidence [:binding-paths]}
 :expanded-coordinate-constraints
 {:form-id :required-equal-earlier-form
  :form-ordinal :required-equal-earlier-Q-global-index
  :syntax-id :required-equal-earlier-syntax
  :source-span :required-equal-earlier-semantic-span
  :generated-origin-chain :required-equal-earlier-generated-origin
  :fragment-ordinal :required-equal-earlier-unique-F-ordinal
  :owner-coordinate :required-equal-earlier-selected-owner
  :core-node-id nil
  :slot-id nil
  :extraction-id nil
  :recur-form-id nil
  :recur-syntax-id nil
  :path :required-equal-earlier-authenticated-pattern-path}}
```

The priority vector is exactly `[earlier, later]`: earlier is the coordinate
source and later is the paired evidence. It is not an event-sort key and does
not change which governed duplicate event wins.

The related semantic ids remain exact ordered source form ids:

```text
[earlier-occurrence.form-id later-occurrence.form-id]
```

The coordinate uses the authenticated earlier form, global ordinal, syntax,
semantic span, generated origin, fragment, owner, and exact path. Generated
core, slot, extraction, and recur ids are nil. The remediation remains exact
source-author `:fix-source` with `[:binding-paths]` evidence.

Root 1 emits zero B51 success requests and zero B51 products before this
pending result. A partial core/slot/extraction product, out-of-plan request, ad
hoc success hash, provisional id, copied id, host id, fabricated id,
later-source coordinate, reversed related vector, or `[later, earlier]`
coordinate selector priority is boundary-only.

## Preserved closed recur related-id mapping

The reachable recur reasons remain exactly:

```text
[:missing-recur-target
 :recur-arity-mismatch
 :recur-not-tail]
```

Their mandatory related-id mapping remains byte-for-byte:

```text
reachable-recur-related-semantic-id-map :=
{:missing-recur-target
 {:related-id-count 1
  :source [:selected-recur-owner-coordinate :root-form-id]
  :value selected-recur-owner-coordinate.root-form-id
  :nearest-compatible-selection-cardinality 0}

 :recur-arity-mismatch
 {:related-id-count 1
  :source [:authenticated-recur-target-stack
           :unique-nearest-compatible-target :target-id]
  :value selected-target.target-id
  :nearest-compatible-selection-cardinality 1}

 :recur-not-tail
 {:related-id-count 1
  :source [:authenticated-recur-target-stack
           :unique-nearest-compatible-target :target-id]
  :value selected-target.target-id
  :nearest-compatible-selection-cardinality 1}}
```

The materialized vectors remain:

```text
:missing-recur-target =>
[selected-recur-owner-coordinate.root-form-id]

:recur-arity-mismatch =>
[selected-target.target-id]

:recur-not-tail =>
[selected-target.target-id]
```

Each vector has cardinality one and contains an exact authenticated digest id.
There is no first-nonnil chain, fallback order, recur-form substitution,
target-derived owner, optional source, caller-supplied value, or transported
authority.

For missing target, the owner root id identifies the top-level root whose
authenticated target stack was searched and produced an empty nearest
compatible selection. For arity mismatch and non-tail, the id is the unique
greatest-depth selected target. Owner-root substitution on target-bearing
reasons and target substitution on missing remain forbidden.

## Preserved nearest-depth selection

At the selected authenticated recur source, Root 1 forms:

```text
nearest-compatible-selection :=
  all compatible target entries at the greatest lexical depth,
  or [] when no compatible entry exists
```

Compatible ancestors at shallower lexical depth are allowed and remain outside
that selection. Cardinality zero selects `:missing-recur-target`. Cardinality
one supplies the unique selected target required for arity mismatch or
non-tail. A tie or multiplicity at the greatest compatible depth is invalid
source integrity and never emits catalog-only `:ambiguous-recur-target`.

The architecture never requires total compatible-target count one. Adding a
farther compatible ancestor cannot change the greatest-depth selected target
or related id. Removing the nearest target may promote a valid shallower
target. Introducing a same-depth tie is boundary-only.

Catalog-only `:ambiguous-recur-target` retains zero producer emission, no
positive pending/finalization fixture, boundary on injection, and no Root-8
materialization.

## Preserved recur source ownership

For all three reachable recur reasons, the diagnostic source remains the exact
authenticated Q recur list form. Root 1:

1. verifies its resolved first child is the admitted core `recur` operator;
2. walks its unique reciprocal parent chain;
3. selects the unique top-level root and unique fragment;
4. selects that root's exact expanded-defn, source-def, or non-definition
   branch from authenticated shape, resolutions, bindings, trace, and origins;
   and
5. constructs the source owner coordinate with enclosing form/syntax equal to
   the recur source form/syntax ids.

The exact coordinate remains:

```text
{:form-id selected-recur.form-id
 :form-ordinal selected-recur.Q-global-index
 :syntax-id selected-recur.syntax-id
 :source-span selected-recur.semantic-span
 :generated-origin-chain selected-recur.generated-origin
 :fragment-ordinal selected-recur.unique-fragment.ordinal
 :owner-coordinate selected-recur.source-root-fragment-owner-coordinate
 :core-node-id nil
 :slot-id nil
 :extraction-id nil
 :recur-form-id selected-recur.form-id
 :recur-syntax-id selected-recur.syntax-id
 :path nil}
```

The target never supplies fragment, root, definition branch, enclosing fields,
or coordinate owner. Missing target does not make source ownership missing.
Attempt 13's duplicate priority restoration does not change recur ownership or
the separate related-id dispatch.

## Preserved arity-before-tail priority

Arity mismatch remains earlier than non-tail within the authenticated
target-bearing recur outcome. One combined authentic fixture makes the same
selected recur both wrong-arity and non-tail. Root 1, the disjoint raw oracle,
and unary Root 8 must select `:recur-arity-mismatch` with exact vector
`[selected-target.target-id]`. Forcing `:recur-not-tail` or its otherwise valid
target-id semantic on that byte-identical raw is
`:source-integrity-mismatch`.

Separate arity and non-tail fixtures do not replace this priority proof.
Function and loop variants still read only function `:arity` or loop
`:slot-count`, and the observed argument count still comes from authenticated
recur child forms.

## Preserved equal-id evidence rule

Cross-reason mutation fixtures use distinct authenticated root, target, and
recur-form ids and swap vectors while retaining the original reason; each
substitution fails. If two authorized source ids happen to be byte-identical,
the materialized 29-key value cannot reveal which selector was used.

That coincidence requires static call/closure and exact source-path evidence
that each reason branch reads only its mandatory field, plus the distinct-id
mutations. It does not create an alternate selector and does not claim that
equal bytes encode provenance.

Root 1, the independently authored raw oracle, and unary Root 8 each reconstruct
the exact reason-dispatched vector. The oracle shares no Root-1/Root-8 selector
helper. Supplied related vectors, kind, reason, or remediation confer no
authority.

## Preserved G10 topology and authority

The exact Root-1 pending whitelist remains:

```text
{:lowering-gap
 [:unsupported-vector-rest
  :unsupported-nested-pattern]

 :duplicate-binding
 [:duplicate-vector-binding-name]

 :recur-target
 [:missing-recur-target
  :recur-arity-mismatch
  :recur-not-tail]

 :verify
 [:module-slot-limit-exceeded
  :module-binding-leaf-limit-exceeded]}
```

It contains exactly eight producer-reachable reasons across four families. The
catalog-only mappings remain exactly:

```text
[:pattern-type :malformed-authenticated-pattern-shape]
[:recur-target :ambiguous-recur-target]
```

Root 1 remains the sole pending detector. Its exact boundary priority remains:

```text
[:malformed-invocation
 :source-integrity-mismatch
 :template-construction-mismatch
 :success-plan-mismatch]
```

Roots 2, 4, 5, 6, and 7 remain success-or-boundary only. Root 4 proves only
self-consistency and digest bytes for template, requests, and digests; Root 5
alone binds materialization to raw. Roots 4 through 7 independently replay and
byte-compare the exact success digest stream.

Root 6 remains a separately authored success verifier. It is never invoked on
duplicate, missing-target, arity-mismatch, non-tail, or other Root-1 pending
raw. It receives no failing diagnostic selector or finalization authority. Its
21 checks, successful recur verification, scalar-only sharing, declared
builtin access, and forbidden-call/helper closure remain exact.

Unary Root 8 remains exactly
`sh07-b51-finalize-rejection(raw-carrier)`. It authenticates raw, calls public
Root 1 exactly once, independently validates and reconstructs the pending
semantic, failure request, singleton plan, empty prefix, and failure hash
input, computes the id with `sh07-declared-digest-hash`, inserts only that id,
and returns the exact six-key finalized result. It calls no later root, public
Root 3, public selector, success stream, host resolver, or itself.

The B51 digest migration remains:

```text
old B51 algorithm
SHA-256(UTF-8(C11-pr-str(hash-input)))

governed B51 algorithm
reader-canonical-hash(hash-input)
```

The only builtin remains unary, pure, and capability-free
`sh07-declared-digest-hash`. Unrelated inherited C11 identities remain exact.

## Exact public ABI and counts

The public result matrix remains:

```text
root  success tag                    pending tag          boundary tag
1     :template-built                :template-rejected   :template-boundary-rejected
2     :template-verified             forbidden            :template-verification-boundary-rejected
3     :digest-preimage-resolved      forbidden            :digest-preimage-boundary-rejected
4     :template-resolved             forbidden            :template-resolution-boundary-rejected
5     :resolved-verified             forbidden            :resolved-verification-boundary-rejected
6     :independent-verifier-bound    forbidden            :independent-verifier-boundary-rejected
7     :final-artifact-built          forbidden            :final-artifact-boundary-rejected
8     :rejection-finalized           forbidden            :rejection-finalizer-boundary-rejected
```

Every result remains the exact six-key envelope. Success remains Root1 ->
Root2 -> per-request Root3/host transport -> Root4 -> Root5 -> Root6 -> Root7.
Failure remains Root1 whitelisted pending -> unary Root8(raw-carrier) ->
finalized rejection.

The exact counts remain:

```text
public roots                              8
Root-8 positional arity                   1
six-key ABI envelope keys                 6
schema version                            18
success purposes                          19
static purpose-dependency edges           58
legacy controlled-path descriptors        94
non-v16 controlled-path descriptors       174
semantic outcome variants                 4
source-semantic pending detector           1 (Root 1)
pending diagnostic families                4
pending reasons                            8
pending V18 resource reasons               2
catalog-only unreachable mappings          2
failure-only purposes                      1 (outside success stream)
```

No field or variant is added.

## Exact evidence obligations

All Attempt-10 and passed Attempt-11/12 evidence remains required. Duplicate
evidence must now independently distinguish:

- governed first duplicate event selection from the coordinate priority row;
- exact coordinate priority `[earlier, later]`;
- earlier occurrence as source and later occurrence as evidence;
- exact ordered related ids `[earlier.form-id later.form-id]`;
- exact earlier source/path/fragment/owner fields;
- nil generated core/slot/extraction/recur ids;
- zero success requests/products before pending; and
- later-first priority, later-source, reversed related ids, other-event,
  partial-product, out-of-plan request, ad hoc hash, copied id, and fabricated
  id mutations failing closed.

Multiple-event fixtures prove the governed detector still selects the first
duplicate event while the selected event's coordinate remains earlier-first.
Static scans prove there is no later-first coordinate priority literal or
fallback in Root 1, the oracle, or Root 8.

Recur evidence preserves:

- missing -> exact one-id owner-root vector;
- arity/non-tail -> exact one-id selected-target vector;
- nearest-compatible selection cardinality `0/1/1` at greatest depth;
- valid farther compatible ancestors and invalid greatest-depth ties;
- no fallback, first-nonnil, recur-form, owner/target substitution, or supplied
  selector authority;
- combined wrong-arity/non-tail arity-first selection;
- static exact source paths plus distinct-id mutations for equal-id cases;
- disjoint raw-oracle reconstruction; and
- unary Root-8 exclusive one-Root1 replay and reconstruction.

Resource priority, two-unreachable negatives, no-form composition, Root-4/5
authority, later-root pending unreachability, Root-6 disjointness, digest
parity/mutations, eventual pins, exact diff hygiene, ASCII, and EOF hygiene
remain mandatory.

## Pin and implementation consequences

Attempt 13 authorizes no implementation or pin update. The existing atomic
implementation workstream remains outside this candidate and paused until an
exact Attempt-13 tuple is frozen, independently accepted, and reconciled to
authoritative main. It must then update its architecture dependency/base and
implement the restored duplicate priority together with the preserved recur
rules inside the same A -> C -> H stack.

Affected B51/Stage2/whole-file pins may be regenerated only after the complete
stack is stable. Frozen B47 source and local pins remain byte-identical. A, C,
and H remain internal reviewed checkpoints and cannot land separately.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-13`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
integrated Attempt 10, preserves terminal Attempts 11 and 12 by reference, and
starts from authoritative main `3624255adf586fc9d368d76335170393aece49c0`.

This task creates only a report-only candidate commit for exact pre-freeze
audit. It does not create a ledger row, freeze the tuple, request review,
accept the decision, or confer integration eligibility. A later parent signal
is required before separate draft registration.

The candidate owns only this report. No implementation, test, fixture,
proof-contract, pin, Attempt-10/11/12 report, terminal ledger edit, or unrelated
canonical document belongs in the candidate.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, and observer. Host
reader-canonical hashing remains parity/transport evidence only.

The restored duplicate priority proves deterministic source coordinate order;
the recur mapping proves deterministic one-id diagnostic input. Neither proves
historical execution, wall-clock order, target authority, transport
provenance, or host identity.

All Attempt-5/6/10 and passed Attempt-11/12 nonclaims remain exact. This report
does not claim implementation, general pattern completeness, complete types/
effects/ownership/safety, MIR or optimization completion, public product
routing, aggregate SH-07 completion, self-hosting, seed retirement, release,
performance, or pin acceptance.

## Independent acceptance criteria

An independent reviewer of a later frozen Attempt-13 tuple must confirm:

1. The exact base is `3624255adf586fc9d368d76335170393aece49c0`,
   integrated Attempt 10 remains authority, and terminal Attempts 11/12 remain
   immutable history.
2. The governed duplicate detector still selects the first authentic duplicate
   event, independently of coordinate field priority.
3. The duplicate coordinate row is exact frozen `[earlier, later]`, with
   earlier source, later evidence, ordered related ids, nil generated ids, and
   unchanged remediation.
4. No later-first preservation claim, new duplicate policy, partial success
   product, ad hoc hash, provisional/copied/fabricated id, or later-source
   coordinate remains.
5. Missing recur maps only to exact authenticated owner root id; arity and
   non-tail map only to exact unique selected target id, each cardinality one.
6. Nearest-compatible selection counts only greatest-depth compatible targets,
   permits farther compatible ancestors, uses cardinality zero/one, and treats
   a greatest-depth tie as boundary while ambiguous remains zero-emission.
7. Combined authentic wrong-arity/non-tail raw selects arity mismatch first;
   forced non-tail on that raw is boundary.
8. Equal digest values use static exact source-path/closure evidence plus
   distinct-id mutations, without impossible byte-level provenance claims.
9. Recur coordinate ownership remains source/root/fragment/definition-derived,
   enclosing fields equal the recur source, and target identity remains
   separate.
10. Root 1, the disjoint raw oracle, and unary Root 8 reconstruct exact pending
    semantics; Root 6 remains separately authored and success-only.
11. Root-4/5 authority, Roots-4-through-7 success digest replay, the unary pure
    declared digest builtin, and C11-to-reader-canonical migration remain
    exact.
12. Every public result remains the exact six-key row, Root 1 remains the sole
    pending detector, later roots remain success-or-boundary, and unary Root 8
    calls only Root 1 exactly once.
13. Counts remain exactly 8 roots, arity 1, 6 keys, schema 18, 19 purposes, 58
    edges, 94/174 paths, 4 outcomes, 1 detector, 4 pending families, 8 reasons,
    2 resource reasons, 2 unreachable mappings, and 1 failure purpose.
14. Documentation, roadmap, governance, exact range diff, language-boundary,
    JSON, ASCII, ownership, and EOF checks pass on the exact tuple.
15. The candidate contains only this report; the author creates no ledger row,
    does not freeze or request review, does not confer acceptance or
    integration eligibility, and does not establish G13 or accept
    implementation.
