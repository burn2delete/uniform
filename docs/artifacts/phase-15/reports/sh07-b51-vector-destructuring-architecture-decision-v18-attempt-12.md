# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 12

Status: Draft closed recur-related-id correction for independent review

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds terminally rejected Attempt 11. It
preserves every Attempt-11 correction that passed independent review:

- `:duplicate-vector-binding-name` uses the authenticated earlier-occurrence
  `:pattern-preflight` coordinate, with generated success ids nil;
- partial success products, ad hoc success hashing, provisional ids, copied
  ids, and fabricated ids remain forbidden;
- every reachable recur diagnostic derives coordinate ownership from its
  authenticated recur source, reciprocal root, unique fragment, and exact
  definition branch;
- recur owner enclosing form/syntax fields equal the recur source itself, so no
  let/loop ancestor or target-derived owner is required;
- Root 6 remains success-only; and
- unary Root 8 remains the exclusive Root-1 pending finalizer.

Attempt 11 was rejected for one exact omission. Attempt 5 requires
`:related-semantic-ids` to contain exactly one SHA-256 id for every reachable
`:recur-target` diagnostic, but Attempt 11 printed an exact selector only for
`:recur-arity-mismatch`. It left `:missing-recur-target` and
`:recur-not-tail` open to unauthorized inference among owner-root, recur-form,
and target ids.

Attempt 12 closes that set with a mandatory reason dispatch:

```text
:missing-recur-target -> selected recur owner-coordinate.root-form-id
:recur-arity-mismatch -> unique selected target.target-id
:recur-not-tail       -> unique selected target.target-id
```

Each resulting vector has cardinality one. There is no fallback chain,
recur-form substitution, target-derived owner, optional selector, or
caller-supplied value. Root 1, an independently authored raw oracle, and unary
Root 8 must reconstruct the same 29-key semantic value byte-for-byte.

Every other integrated Attempt-10 and passed Attempt-11 rule, count, topology,
digest rule, Root-4/5 authority split, Root-6 disjointness rule, unary Root-8
rule, evidence obligation, and nonclaim remains exact. This report contains no
implementation, test, fixture, proof-contract, source-pin, or whole-file-pin
change.

## Normative baseline and terminal history

The authoritative base is:

```text
authoritative main commit
0326fe6b6d234746a123366b442ffe3f8468f171

authoritative main tree
680846532cb5819a6b42e04a2bddc302dad8ff03

integrated Attempt-10 architecture candidate
3d780692e8023274c415a351b393ea8e1f9a796e

integrated Attempt-10 candidate tree
144cafd0f8833e35de52e29ea2f596376045ad72

integrated Attempt-10 report SHA-256
9eeb69c974636d08d2a6b1673a1fc2b523567ac27f4d6a8d9a14996874a760b9

Attempt-10 integration commit
ab516b370fa49b9cbfe7b2dcda6e696c0f8f624d

terminal Attempt-11 candidate
a29469c84e15ae3a55d2606421364c020c19571f

terminal Attempt-11 candidate tree
20433cc260cc6baa466a5667bcc06445155eca49

terminal Attempt-11 report SHA-256
11c95a69638c7212832e07b1de8fa0ff163a8da8cdec87e0e1069397e60111f6

Attempt-11 terminal rejection / Attempt-12 base
0326fe6b6d234746a123366b442ffe3f8468f171
```

Attempts 5, 6, and 10 remain immutable integrated authority. Attempts 7, 8,
9, and 11 remain exact terminal rejection history and receive no integration
credit. Attempt 12 incorporates Attempt 11 by reference and supersedes only
its missing closed selector for reachable recur related semantic ids.

When incorporated language conflicts with this report, this report controls
only the closed mapping printed below. It does not change a pending reason,
reason count, family, priority, coordinate owner, remediation, diagnostic key,
schema version, public root, result tag, purpose, dependency edge, controlled
path, semantic outcome, digest algorithm, or finalization route.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Exact reachable recur reason set

The reachable Root-1 recur pending reasons remain exactly:

```text
reachable-recur-pending-reasons :=
[:missing-recur-target
 :recur-arity-mismatch
 :recur-not-tail]
```

The catalog-only pair
`:recur-target/:ambiguous-recur-target` remains outside pending authority. The
authenticated reciprocal ancestry and unique-nearest-compatible target rule
produce no authentic ambiguous return. Its producer emission count remains
zero, it has no positive pending/finalization fixture, injected occurrences
are `:source-integrity-mismatch`, and unary Root 8 never materializes it.

The incorporated producer selection remains ordered and fail closed. Root 1
first authenticates the selected recur source and its source ownership. It
replays the mixed function/loop target stack at that source and forms the
`nearest-compatible-selection`: all compatible entries at the greatest lexical
depth, or `[]` when no compatible entry exists. Compatible ancestors at
shallower lexical depths are allowed and remain outside that selection. A
selection cardinality of zero selects `:missing-recur-target`; cardinality one
supplies the unique selected target required before either
`:recur-arity-mismatch` or `:recur-not-tail` can be selected. Multiplicity or a
tie at the greatest compatible depth is invalid source integrity; it does not
emit catalog-only `:ambiguous-recur-target`. Arity mismatch precedes the later
tail-position failure according to the incorporated producer semantics. No
reason is chosen from a transported kind/reason or supplied related id.

## Mandatory closed related-id dispatch

The exact mapping is:

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

The materialized field is exactly:

```text
related-semantic-ids(reason) :=
case reason of
  :missing-recur-target =>
    [selected-recur-owner-coordinate.root-form-id]

  :recur-arity-mismatch =>
    [selected-target.target-id]

  :recur-not-tail =>
    [selected-target.target-id]

  otherwise => boundary :source-integrity-mismatch
```

The result is always a one-element vector containing an exact authenticated
digest id. The mapping dispatches on the already independently selected
reachable reason. A map lookup failure, unknown reason, absent value, nil,
non-digest, duplicate value, two-element vector, or scalar substitution is a
boundary and never pending.

For `:missing-recur-target`, the selected recur owner coordinate is the exact
source/root/fragment/definition coordinate independently derived from the
authenticated recur occurrence. Its `:root-form-id` identifies the top-level
root whose authenticated lexical target stack was searched and found to have
an empty nearest-compatible selection. It remains available even though no
target exists. The value is not the recur form id unless those independently
authenticated ids happen to be byte-equal; the selector is still the owner
root field.

For `:recur-arity-mismatch`, the exact unique nearest compatible target is the
same target whose authenticated function `:arity` or loop `:slot-count`
supplies the expected value. Its `:target-id` is mandatory and remains the sole
related semantic id.

For `:recur-not-tail`, Root 1 must also have a nearest-compatible selection of
cardinality one at the greatest compatible depth. The sole related semantic id
is that target's exact `:target-id`. Non-tail position does not authorize using
the recur form, owner root, enclosing form, owner core, slot, or another stack
entry.

## No fallback or inferred selector

The dispatch is closed. These candidate chains are forbidden:

```text
selected-target.target-id
  else owner-coordinate.root-form-id
  else recur-form-id

owner-coordinate.root-form-id
  else recur-form-id
  else selected-target.target-id

first-nonnil(target-id, root-form-id, recur-form-id)
```

No branch may choose a value by availability, equality, fixture shape, helper
return, host traversal, or map order. A target id on
`:missing-recur-target` contradicts target cardinality zero. An owner-root id
on either target-bearing reason violates the mandatory target selector. A
recur-form id is never an authorized related semantic id for these three
reasons.

For target-bearing reasons, missing or multiple selected targets cannot be
repaired by the owner root. Missing target selects the distinct missing reason;
a tie or multiplicity at the greatest compatible depth and any malformed,
cross-fragment, or non-authenticated target selection are
`:source-integrity-mismatch`. Farther compatible ancestors at shallower depth
are valid stack context but cannot replace or join the unique greatest-depth
selection. For the missing reason, an absent or ambiguous source owner/root
likewise returns the boundary rather than falling back to the recur form.

Supplied, transported, or prebuilt related vectors have no authority. Root 1
derives the reason and exact id from raw. The independent raw oracle derives
the same pair without calling Root 1 or Root 8. Unary Root 8 accepts only raw,
calls Root 1 exactly once, independently validates and reconstructs the exact
pending semantic including this reason-dispatched vector, and inserts only the
failure digest id.

## Preserved Attempt-11 duplicate correction

The duplicate predicate remains an authenticated pattern preflight. Root 1
selects the first duplicate event in module-global order, uses the earlier
occurrence as coordinate source, and preserves the later occurrence as ordered
related evidence.

The exact coordinate remains:

```text
:duplicate-vector-binding-name
{:template :pattern-preflight
 :source [:authenticated-sh06-request :forms
          :selected-duplicate-event :earlier]
 :priority [[:authenticated-pattern-preorder
             :selected-duplicate-event :later]
            [:authenticated-pattern-preorder
             :selected-duplicate-event :earlier]]
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

No Tier-0d/Tier-1 success id exists at this failure point. Root 1 emits zero
B51 success requests and zero B51 products before the pending result. The sole
associated digest request is the separate failure-only diagnostic purpose
reconstructed by unary Root 8. Attempt 12 does not reopen the infeasible
Attempt-5 extraction coordinate.

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

The coordinate remains:

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

The owner coordinate's enclosing form/syntax fields equal the selected recur
form/syntax ids byte-for-byte. This works for a plain or top-level function
recur and for missing target. The target never supplies fragment, root,
definition branch, enclosing fields, or coordinate owner. Attempt 12's related
id mapping is a separate diagnostic field and does not weaken this source-owner
rule.

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

They retain zero producer emission, no positive fixture, boundary on
injection, and no Root-8 finalization.

Root 1 remains the sole pending detector. Its exact boundary priority remains:

```text
[:malformed-invocation
 :source-integrity-mismatch
 :template-construction-mismatch
 :success-plan-mismatch]
```

Roots 2, 4, 5, 6, and 7 remain success-or-boundary only. Root 4 proves only
self-consistency and digest bytes for its template, requests, and digests;
Root 5 alone binds materialization to raw. Roots 4 through 7 independently
replay and byte-compare the exact success digest stream.

Root 6 remains a separately authored success verifier. It is never invoked on
missing-target, arity-mismatch, non-tail, duplicate, or any other Root-1
pending raw. It receives no failing diagnostic selector or finalization
authority. Its successful recur verification, scalar-only sharing, declared
builtin access, 21 checks, and forbidden-call/helper closure remain exact.

Unary Root 8 remains exactly
`sh07-b51-finalize-rejection(raw-carrier)`. It authenticates raw, calls public
Root 1 exactly once, reconstructs the exact failure request, singleton plan,
empty prefix, and failure hash input, computes the id with
`sh07-declared-digest-hash`, inserts only that id, and returns the exact
six-key finalized result. It calls no later root, public Root 3, public
selector, success stream, host resolver, or itself.

The B51 algorithm remains:

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

No field or variant is added. The closed mapping fills the existing one-id
field under schema 18.

## Exact evidence obligations

All Attempt-10 and passed Attempt-11 evidence remains required. Each reachable
recur reason needs an authentic Root-1 pending fixture, independent raw-oracle
reconstruction, and unary Root-8 finalization fixture.

For `:missing-recur-target`, evidence proves:

- nearest-compatible selection cardinality zero, with no selected target;
- one authentic selected recur source owner/root;
- exact related vector `[owner-coordinate.root-form-id]`;
- the root id names the top-level root whose stack was searched;
- target id, recur form id, recur syntax id, enclosing id, owner core id, other
  authenticated root id, nil, scalar, empty vector, and two-id vector all fail;
- absent/ambiguous owner or root is boundary-only; and
- adding a compatible target changes the authentic reason rather than silently
  retaining the owner-root mapping.

For `:recur-arity-mismatch`, evidence includes distinct function and loop
targets and proves:

- nearest-compatible selection cardinality one at the greatest compatible
  depth, while farther compatible ancestors at shallower depth are allowed;
- expected value comes from function `:arity` or loop `:slot-count`;
- exact related vector `[selected-target.target-id]`;
- owner-root, recur-form, enclosing, owner-core, other target, parent target,
  nil, scalar, empty vector, and two-id vector substitutions all fail; and
- missing/multiple/malformed target selection cannot fall back to another id.

For `:recur-not-tail`, evidence proves:

- nearest-compatible selection cardinality one at the greatest compatible
  depth before tail validation, while farther compatible ancestors at
  shallower depth are allowed;
- exact related vector `[selected-target.target-id]`;
- owner-root, recur-form, enclosing, owner-core, other target, parent target,
  nil, scalar, empty vector, and two-id vector substitutions all fail; and
- removing the selected target when no compatible shallower ancestor remains
  selects missing-target or boundary as governed, never a fallback id for
  non-tail.

A combined authentic fixture makes the same selected recur both wrong-arity
and non-tail. Root 1, the disjoint raw oracle, and unary Root 8 must select
`:recur-arity-mismatch` with exact related vector
`[selected-target.target-id]`. Forcing `:recur-not-tail` or its otherwise valid
target-id semantic on that byte-identical raw carrier is
`:source-integrity-mismatch`. Separate arity and non-tail fixtures do not
replace this priority proof.

Target-stack mutations add a farther compatible ancestor without changing the
greatest-depth selected target or related id, remove the nearest target so the
governed shallower target becomes the new selection, and introduce a same-depth
tie that fails source integrity without emitting catalog-only ambiguous.

Cross-reason mutation fixtures use distinct authenticated root, target, and
recur-form ids, then swap all three vectors while keeping the original reason;
each substitution fails. If two authorized source ids happen to be
byte-identical, the materialized 29-key value cannot reveal which selector was
used. That coincidence therefore requires static call/closure and exact
source-path evidence that each reason branch reads only its mandatory field,
plus the distinct-id mutations; it does not create an alternate selector or a
claim that equal bytes encode provenance. Fallback-chain helpers, first-nonnil
logic, caller-supplied related vectors, and transported kind/reason/remediation
are statically unreachable in Root 1, the oracle, and Root 8.

The oracle shares no Root-1/Root-8 selector helper. Root-8 closure evidence
shows exactly one Root-1 call, independent exact reconstruction of the
reason-dispatched one-id vector, and zero Root-2-through-7, public-Root3,
selector, success-stream, resolver, or self calls. Root 6 remains success-only
and is not failing-diagnostic evidence.

Duplicate preflight, resource priority, two-unreachable negatives, no-form
composition, Root-4/5 authority, later-root pending unreachability, Root-6
disjointness, digest parity/mutations, eventual pins, exact diff hygiene,
ASCII, and EOF hygiene remain mandatory.

## Pin and implementation consequences

Attempt 12 authorizes no implementation or pin update. The existing atomic
implementation workstream remains outside this candidate. It must remain
paused until an exact Attempt-12 tuple is frozen, independently accepted, and
reconciled to authoritative main. It must then update its architecture
dependency/base and implement the closed reason dispatch inside the same
A -> C -> H stack.

Affected B51/Stage2/whole-file pins may be regenerated only after the complete
stack is stable. Frozen B47 source and local pins remain byte-identical. A, C,
and H remain internal reviewed checkpoints and cannot land separately.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-12`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
integrated Attempt 10, preserves terminal Attempt 11 by reference, and starts
from authoritative main `0326fe6b6d234746a123366b442ffe3f8468f171`.

The report-only candidate commit is immutable architecture content. A separate
draft registration commit records that candidate commit, tree, and report
SHA-256 without making the ledger part of the candidate identity. Attempt 12
must later be frozen as an exact clean tuple and independently reviewed. The
author cannot accept it or confer integration eligibility.

Only an independently accepted and separately reconciled Attempt-12 lifecycle
can establish G12. Draft registration, freezing, review request, acceptance,
integration eligibility, and integration are distinct events.

The candidate owns only this report. No implementation, test, fixture,
proof-contract, pin, Attempt-10/11 report, terminal ledger edit, or unrelated
canonical document belongs in the candidate.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, and observer. Host
reader-canonical hashing remains parity/transport evidence only.

The reason-dispatched related id proves deterministic diagnostic identity input
for one authenticated raw occurrence. It does not prove historical execution,
wall-clock order, target authority, transport provenance, or host identity.

All Attempt-5/6/10 and passed Attempt-11 nonclaims remain exact. This report
does not claim implementation, general pattern completeness, complete types/
effects/ownership/safety, MIR or optimization completion, public product
routing, aggregate SH-07 completion, self-hosting, seed retirement, release,
performance, or pin acceptance.

## Independent acceptance criteria

An independent reviewer of a later frozen Attempt-12 tuple must confirm:

1. The exact base is `0326fe6b6d234746a123366b442ffe3f8468f171`,
   integrated Attempt 10 remains authority, and terminal Attempt 11 remains
   immutable rejection history.
2. Every passed Attempt-11 duplicate, no-partial-id, recur-source-owner,
   Root-6-success-only, and unary-Root8 correction remains exact.
3. The reachable recur reason set is exactly missing target, arity mismatch,
   and non-tail; nearest-compatible selection counts only compatible targets
   at greatest lexical depth, permits farther compatible ancestors, uses
   cardinality zero/one for missing/target-bearing reasons, and treats a
   greatest-depth tie as boundary while catalog-only ambiguous remains
   zero-emission.
4. Related-id count remains exactly one for each reachable recur reason.
5. Missing target maps only to exact authenticated selected recur
   `owner-coordinate.root-form-id`, which identifies the searched top-level
   root when no target exists.
6. Arity mismatch maps only to exact unique selected `target.target-id`.
7. Non-tail maps only to exact unique selected `target.target-id`; a combined
   authentic wrong-arity/non-tail carrier selects arity mismatch first and
   forcing non-tail on that raw is boundary.
8. Fallback chains, first-nonnil logic, recur-form substitution,
   target-derived owner, owner-root substitution on target-bearing reasons,
   target substitution on missing, and absent/ambiguous values are forbidden.
9. Root 1, an independently authored raw oracle, and unary Root 8 reconstruct
   the reason-dispatched 29-key semantic byte-for-byte; supplied related ids
   confer no authority.
10. Root 6 remains separately authored, success-only, and outside pending
    selector/finalizer authority with its exact disjoint closure.
11. Root 4/5 authority, Roots-4-through-7 success digest replay, the unary pure
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
15. The candidate contains only this report, and its separate draft ledger row
    owns only this report; the author does not freeze, request review, accept,
    confer integration eligibility, establish G12, or accept implementation.
