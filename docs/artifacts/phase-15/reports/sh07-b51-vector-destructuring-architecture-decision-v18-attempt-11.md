# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 11

Status: Draft pre-resolution diagnostic-coordinate correction for independent review

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds the integrated Attempt-10 baseline.
It resolves one contradiction exposed while implementing that baseline:
`:duplicate-vector-binding-name` is authentically detected during pattern
preflight, before any governed Tier-0d core-node id or Tier-1 slot/extraction
id exists, but the inherited Attempt-5 coordinate policy required an
`:extraction` coordinate containing exactly those later success ids.

Attempt 11 preserves the duplicate as one of Root 1's eight authentic pending
reasons. It changes only that reason's coordinate from a success-product
`:extraction` coordinate to an authenticated source
`:pattern-preflight` coordinate. The coordinate selects the earlier duplicate
occurrence, its exact authenticated path, fragment, and owner; generated core,
slot, extraction, and recur ids are nil. Its ordered related semantic ids
remain the earlier occurrence followed by the later occurrence. No partial
success product, ad hoc success hashing, or fabricated id may repair the old
contradiction.

Attempt 11 also makes explicit the already-required recur diagnostic owner
selector. A recur diagnostic's coordinate owner is derived from the selected
authenticated recur source form, its reciprocal ancestry, its unique root and
fragment, and that root's exact definition branch. The owner coordinate's
enclosing form/syntax fields equal the recur source itself; no let/loop
ancestor or target is required. A selected recur target is a separate
identity; it may supply the exact reason-specific expected value or related
semantic id, but it never selects or substitutes the coordinate owner.

Every other integrated Attempt-10 rule, count, topology, digest rule,
Root-4/5 authority split, Root-6 disjointness rule, unary Root-8 rule, evidence
obligation, and nonclaim remains exact. This report contains no implementation,
test, fixture, proof-contract, source-pin, or whole-file-pin change.

## Normative baseline and exact precedence

The authoritative base is integrated Attempt 10:

```text
authoritative main commit
ab516b370fa49b9cbfe7b2dcda6e696c0f8f624d

authoritative main tree
104caaa2c6342b39b5aa276c56c5df1f72c9d420

Attempt-10 architecture candidate
3d780692e8023274c415a351b393ea8e1f9a796e

Attempt-10 candidate tree
144cafd0f8833e35de52e29ea2f596376045ad72

Attempt-10 report SHA-256
9eeb69c974636d08d2a6b1673a1fc2b523567ac27f4d6a8d9a14996874a760b9

Attempt-10 integration commit
ab516b370fa49b9cbfe7b2dcda6e696c0f8f624d
```

The incorporated architecture is:

1. immutable integrated Attempts 5 and 6;
2. terminal Attempts 7, 8, and 9 with their exact blockers;
3. integrated Attempt 10, including its exact eight producer-reachable
   pending reasons and two catalog-only unreachable mappings; and
4. this Attempt-11 correction, which supersedes only the two exact policies
   printed below.

When language in Attempt 5 or an incorporated successor conflicts with this
report, this report controls only:

1. the coordinate policy for `:duplicate-vector-binding-name`; and
2. the owner selector used by the three producer-reachable recur diagnostic
   reasons.

All nonconflicting language remains exact. In particular, this report does not
change a reason, reason count, family, priority, remediation, diagnostic key,
schema version, public root, result tag, purpose, dependency edge, controlled
path, semantic outcome, digest algorithm, or finalization route.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Proven duplicate-coordinate contradiction

Attempt 5 assigns this policy by inheritance:

```text
:duplicate-vector-binding-name
{:template :extraction
 :source [:binding-extractions :first-duplicate]
 :priority [[:binding-extractions :first-duplicate]
            [:binding-extractions :second-duplicate]]}
```

The `:extraction` template requires a concrete owning core-node id, slot id,
and extraction id. Those values are governed success values:

```text
owning core-node id  := Tier-0d declared-digest result
slot id              := Tier-1 declared-digest result
extraction id        := Tier-1 declared-digest result
```

The authentic duplicate predicate is earlier. Root 1 enumerates the raw,
authenticated vector binding leaves in exact pattern preorder, observes a
repeated binding name, and must choose the first duplicate event before
emitting any B51 success request or product. Therefore no authentic
`:binding-extractions` vector or governed values for those three id positions
exist on the rejected branch.

The old policy cannot be implemented by ordering the work differently. Any
such implementation would have to do at least one forbidden thing:

- emit a partial core, slot, or extraction success product before the
  higher-priority rejection is selected;
- construct a Tier-0d or Tier-1 success request outside the admitted 19-purpose
  success plan;
- invoke the declared digest builtin for a success id that the failing plan
  never admits;
- copy an id from another occurrence, owner, fixture, or successful replay;
- fill a required digest-id field with an opaque, provisional, or host id; or
- retain the old coordinate with internally inconsistent nil fields.

Each option violates Attempt 5's no-partial-product requirement, Attempt 10's
exact success plan and digest authority, or the closed coordinate matrix. The
truthful correction is a pre-resolution source coordinate whose nullable
generated-id fields are nil.

## Exact duplicate preflight detector

The duplicate predicate is unchanged. For each admitted vector pattern, Root 1
enumerates binding leaves in authenticated pattern preorder. A duplicate event
is an ordered pair:

```text
duplicate-event :=
{:binding-name symbol
 :earlier duplicate-source-occurrence
 :later duplicate-source-occurrence}

duplicate-source-occurrence :=
{:form-id authenticated-Q-form-id
 :form-ordinal authenticated-Q-global-index
 :syntax-id authenticated-Q-syntax-id
 :source-span authenticated-Q-semantic-span-or-nil
 :generated-origin-chain authenticated-Q-generated-origin
 :fragment-ordinal unique-authenticated-F-ordinal
 :owner-coordinate exact-authenticated-owner-coordinate
 :path exact-authenticated-pattern-relative-path}
```

The earlier occurrence is the first preorder occurrence of the repeated name
within the selected pattern. The later occurrence is the first later preorder
occurrence of that name. When more than one duplicate event exists, the event
whose later occurrence is smallest in the existing module-global occurrence
order wins; its earlier occurrence is then fixed by the first-occurrence rule.
No map iteration, binding-table recovery order, hash order, product emission,
or host traversal participates.

Both occurrences must join byte-for-byte to the same authenticated pattern,
fragment, and selected B51 owner. Their names must equal the diagnostic's
`:observed` symbol. Their paths must be distinct. A missing join, cross-owner
pair, cross-fragment pair, reordered occurrence, different name, or
non-authenticated source value is `:source-integrity-mismatch`, not pending.

This detector preserves the incorporated diagnostic priority exactly:

```text
[:verify :core-shape :origin :lowering-gap :duplicate-binding
 :recur-target :evaluation-order :effect-drop :unsafe-drop :pattern-type]
```

The two V18 resource preflights still run before lower-priority pattern
lowering or duplicate selection. `:unsupported-vector-rest` and
`:unsupported-nested-pattern` still precede the duplicate family. Attempt 11
does not move, merge, or alias a diagnostic family.

## Exact duplicate coordinate replacement

The sole replacement row is:

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

The expanded source selector stored in the 29-key diagnostic is the exact
realized vector path with numeric indexes. It contains none of the symbolic
selector words printed above. The source form fields, fragment ordinal, owner,
and path all come from the authenticated earlier occurrence. The four
generated success/recur id positions are literally nil. No nearby slot,
enclosing core, later occurrence, recovery projection, or target may fill
them.

Here `:pattern-preflight` is pre-resolution only with respect to B51's
declared-digest success plan. Selecting the authentic owner still replays the
already-authenticated SH-06 resolution, binding, fragment, ancestry, trace, and
origin facts. It never substitutes a host-recognized symbol or an unresolved
source guess.

The related vector has exactly two digest ids in this exact order:

```text
[earlier-occurrence.form-id later-occurrence.form-id]
```

It is source-authenticated occurrence evidence, not a pair of generated
extraction ids. Reversing the vector, duplicating one entry, substituting
syntax ids, or using generated slot/extraction ids is rejected. The later
occurrence remains visible through this vector and the independently replayed
event; it does not become the coordinate source.

The reason, kind, rule, expected value, observed symbol, related-id count,
remediation, severity, stage, lowering rule, profile, target, fail-closed flag,
29-key own-id-free semantic value, failure request, and Root-8 30-key
materialization remain otherwise exact.

## No partial or fabricated success authority

On the selected duplicate branch, Root 1 must have emitted zero B51 success
requests and zero B51 products before building the pending result. In
particular, it emits no Tier-0d core-node request and no Tier-1 slot or
extraction request for either occurrence.

The only digest request associated with this branch is the separate
failure-only diagnostic purpose reconstructed under the incorporated unary
Root-8 protocol. Root 8 may compute only that failure hash after replaying
Root 1's exact pending result. The declared builtin does not make a success id
authentic merely because a caller can form a map with the right shape.

The following are exact boundary failures:

- a duplicate pending value with nonnil core, slot, extraction, or recur id;
- a duplicate pending value whose coordinate uses the later occurrence;
- a duplicate pending value whose source, path, fragment, or owner does not
  join the authenticated earlier occurrence;
- a duplicate pending value whose related ids are not exact ordered
  `[earlier later]` source form ids;
- a carrier, helper result, or test injection containing a partial success
  request/product used to justify the coordinate; and
- any provisional, synthetic, copied, host-authored, or ad hoc hashed success
  id.

Root 1 classifies authenticated-source disagreement as
`:source-integrity-mismatch`. Product/template construction disagreement after
source selection remains `:template-construction-mismatch`; success-plan
disagreement remains `:success-plan-mismatch`. None becomes pending merely to
preserve the old extraction policy.

## Exact recur diagnostic owner selector

The producer-reachable recur reasons remain exactly:

```text
[:missing-recur-target :recur-arity-mismatch :recur-not-tail]
```

`:ambiguous-recur-target` remains the Attempt-10 catalog-only unreachable
mapping with zero producer emission and boundary-only injection handling.

For each reachable reason, the selected diagnostic source is the exact
authenticated Q recur list form. Starting at that form, Root 1 independently:

1. verifies the resolved first child is the admitted core `recur` operator;
2. walks the unique reciprocal `:parent-form-id` chain with the incorporated
   cycle, membership, and depth checks;
3. selects the unique terminal top-level root and the unique fragment whose
   manifest, root, content, node, and form memberships all agree;
4. selects that root's exact expanded-defn, source-def, or non-definition
   branch from the authenticated root shape, resolutions, bindings, macro
   trace, and origins; and
5. constructs the diagnostic owner coordinate from that source/root/fragment/
   definition branch while setting its enclosing form/syntax fields to the
   selected recur source form/syntax ids.

The coordinate's top-level form fields and recur form/syntax ids copy the recur
source. Its fragment ordinal copies that source's unique fragment. Its
`:owner-coordinate` is the owner derived by the five steps above. That owner's
`:enclosing-form-id` and `:enclosing-syntax-id` equal the selected recur form
and syntax ids byte-for-byte. No enclosing let/loop is required. This remains
defined for a plain or top-level function recur and for
`:missing-recur-target`; source ownership does not depend on finding a target.

The target selector is separate. The incorporated mixed function/loop target
stack still selects the unique nearest compatible target at greatest lexical
depth. Its identity may populate the exact reason-specific expected value and
related semantic id. It does not supply the diagnostic fragment, root,
definition branch, enclosing form, or owner coordinate. A missing target does
not make source ownership missing, and a selected target owned elsewhere does
not move the coordinate to that target.

The exact recur coordinate expansion remains a source-form expansion:

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

A target-derived owner, target fragment/root substitution, any enclosing form
other than the selected recur source, cross-fragment owner, nearest-name guess,
or host traversal choice is `:source-integrity-mismatch`. The selected
target's id remains separate even when some source and target ids happen to be
byte-equal.

## Preserved G10 architecture

Attempt 11 preserves the exact Attempt-10 pending whitelist:

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

It preserves the two catalog-only unreachable mappings:

```text
[:pattern-type :malformed-authenticated-pattern-shape]
[:recur-target :ambiguous-recur-target]
```

It preserves Root 1 as the sole pending detector, the exact three-result Root-1
topology, and the exact four-value boundary priority:

```text
[:malformed-invocation
 :source-integrity-mismatch
 :template-construction-mismatch
 :success-plan-mismatch]
```

It preserves Roots 2, 4, 5, 6, and 7 as success-or-boundary only. Root 4 still
proves only self-consistency and digest bytes for its template, requests, and
digests; Root 5 alone binds that materialization to raw. Roots 4 through 7
still independently replay and byte-compare the exact success digest stream.

Root 6 remains the separately authored success verifier. It is never invoked
on duplicate or failing-recur pending raw and gains no diagnostic-selection or
failure-finalization authority. It still verifies successful recur semantics
where applicable, may share only scalar predicates and the declared builtin,
and retains zero calls to Roots 1 through 5, Root 7, Root 8, and all producer
outcome, template, request, preimage, path, digest, materialization, or
candidate helpers.

Unary Root 8 remains exactly `sh07-b51-finalize-rejection(raw-carrier)`. It
authenticates raw, calls public Root 1 exactly once, reconstructs the exact
failure request, singleton plan, empty prefix, and failure hash input, computes
the diagnostic id with `sh07-declared-digest-hash`, inserts only that id, and
returns the exact six-key finalized result. It calls no later root, public Root
3, selector, success stream, host resolver, or itself.

The digest algorithm remains the exact semantic migration:

```text
old B51 algorithm
SHA-256(UTF-8(C11-pr-str(hash-input)))

governed B51 algorithm
reader-canonical-hash(hash-input)
```

The only builtin remains unary, pure, capability-free
`sh07-declared-digest-hash`. No general hash, receipt, seal, signature,
capability, or host authority is added.

## Exact public ABI and counts

The public result matrix remains exact:

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

Every result remains the exact common six-key envelope. The success host
sequence remains Root1 -> Root2 -> per-request Root3/host transport -> Root4 ->
Root5 -> Root6 -> Root7. The sole semantic failure sequence remains Root1
whitelisted pending -> unary Root8(raw-carrier) -> finalized rejection.

The counts remain:

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

No field or variant is added. The correction uses the existing nullable
29-key diagnostic coordinate and existing `:pattern-preflight` template, so
schema version 18 remains truthful.

## Evidence obligations

All Attempt-10 evidence remains required. The duplicate positive must now also
prove:

- zero success requests and zero success products precede the pending result;
- the selected pair is the exact first duplicate event under module-global
  occurrence order;
- coordinate form/span/origin/fragment/owner/path all equal the authenticated
  earlier occurrence;
- core, slot, extraction, and recur ids are nil;
- related semantic ids equal exact ordered source form ids `[earlier later]`;
- exact remediation remains source-author `:fix-source` with
  `[:binding-paths]` evidence;
- every one of the fifteen coordinate fields is independently mutated; and
- partial request/product, ad hoc digest, copied id, fabricated id, later-source,
  reversed-related-id, cross-owner, and cross-fragment mutations fail closed.

Recur positives for missing target, function/loop arity mismatch, and non-tail
position must independently reconstruct source/root/fragment/definition-branch
ownership with enclosing form/syntax equal to the recur source. Mutations
substitute the target's owner, another authenticated owner, another root or
fragment, any other enclosing form, and target id for every owner/source
position. The exact source owner must survive all target-kind and
target-identity variants, including no target.

An independently authored test oracle, outside the production Root-1/Root-8
closures, reconstructs both corrected failing selectors directly from raw.
Unary Root-8 evidence proves its exclusive one-Root1 replay independently
validates and reconstructs the corrected pending semantic, then materializes
only the failure id without a public selector or any later root. Root 6 remains
success-only and is not evidence for a failing diagnostic. Digest parity,
prefix/algorithm mutations, two-unreachable negative evidence, no-form
evidence, resource priority, Root-4/5 authority, later-root pending
unreachability, exact pins, diff hygiene, ASCII, and EOF hygiene remain
mandatory.

## Pin and implementation consequences

This report authorizes no implementation or pin update. The existing atomic
implementation workstream must remain paused at this contradiction until an
exact Attempt-11 tuple is frozen, independently accepted, and reconciled to
authoritative main. It must then update its architecture dependency/base and
implement the corrected duplicate/recur selectors inside the same A -> C -> H
stack.

The implementation may regenerate affected B51/Stage2/whole-file pins only
after the complete stack is stable. Frozen B47 source and local pins remain
byte-identical. A, C, and H remain internal reviewed checkpoints and may not
land separately.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-11`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
integrated Attempt 10 and starts from authoritative main
`ab516b370fa49b9cbfe7b2dcda6e696c0f8f624d`.

The report-only candidate commit is immutable architecture content. A separate
draft registration commit records that candidate commit, tree, and report
SHA-256 without making the ledger part of the candidate identity. Attempt 11
must later be frozen as an exact clean tuple and independently reviewed. The
author cannot accept it or confer integration eligibility.

Only an independently accepted and separately reconciled Attempt-11 lifecycle
can establish G11. Draft registration, freezing, review request, acceptance,
integration eligibility, and integration are distinct events. No placeholder
reviewer or self-audit may advance them.

The candidate owns only this report. No implementation, test, fixture,
proof-contract, pin, Attempt-10 report, or other canonical document belongs in
the candidate.

## Nonclaims

The Clojure/JVM host remains the source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, and observer. Host
reader-canonical hashing remains parity/transport evidence only.

The corrected duplicate coordinate proves an authenticated source occurrence
without claiming that any success core/slot/extraction id existed. The recur
selector proves occurrence ownership, not historical execution, target
authority, wall-clock order, or transport provenance.

All Attempt-5/6/10 nonclaims remain exact. This report does not claim an
implementation, general pattern completeness, complete types/effects/
ownership/safety, MIR or optimization completion, public product routing,
aggregate SH-07 completion, self-hosting, seed retirement, release,
performance, or pin acceptance.

## Independent acceptance criteria

An independent reviewer of a later frozen Attempt-11 tuple must confirm:

1. The exact base is integrated Attempt-10 main
   `ab516b370fa49b9cbfe7b2dcda6e696c0f8f624d`, and all earlier integrated and
   terminal architecture history remains immutable.
2. The duplicate contradiction is real: its authentic detector precedes every
   governed Tier-0d/Tier-1 success id required by the inherited extraction
   coordinate.
3. Duplicate remains one of exactly eight Root-1 pending reasons with unchanged
   family, priority, expected/observed values, remediation, and related-id
   cardinality.
4. Its coordinate is exactly the authenticated earlier-occurrence
   `:pattern-preflight` coordinate with exact source/path/fragment/owner,
   generated core/slot/extraction/recur ids nil, and ordered source form ids
   `[earlier later]`.
5. Partial success products, out-of-plan requests, ad hoc success hashing,
   provisional/copied/host ids, and fabricated ids are explicitly forbidden
   and covered by negative evidence.
6. The three reachable recur reasons select coordinate ownership only from the
   recur source's authenticated ancestry/root/fragment/definition branch, with
   owner enclosing form/syntax equal to the recur source; target identity
   remains separate and no let/loop ancestor is required.
7. The two catalog-only mappings remain zero-emission, boundary-only values;
   no positive pending/finalization fixture is fabricated.
8. Root 1 remains the sole pending detector, all non-whitelisted failures use
   the exact four-boundary partition, and Roots 2/4/5/6/7 remain
   success-or-boundary only.
9. Root 4/5 authority, Roots-4-through-7 digest replay, the unary pure declared
   digest builtin, and the explicit C11-to-reader-canonical migration remain
   exact.
10. Root 6 remains separately authored, success-only, and outside all pending
    diagnostic selection/finalization, with its exact zero-call and
    forbidden-helper closure preserved.
11. Unary Root 8 authenticates raw, calls Root 1 exactly once, reconstructs the
    failure request/singleton plan/empty prefix/hash input, finalizes only
    whitelisted pending, and calls no forbidden root/helper/selector.
12. Every public result remains the exact six-key row; boundary priority and
    diagnostic non-recursion remain exact.
13. Counts remain exactly 8 roots, arity 1, 6 keys, schema 18, 19 purposes, 58
    edges, 94/174 paths, 4 outcomes, 1 detector, 4 pending families, 8 reasons,
    2 resource reasons, 2 unreachable mappings, and 1 failure purpose.
14. Documentation, roadmap, governance, exact range diff, language-boundary,
    ASCII, ownership, and EOF checks pass on the exact tuple.
15. The candidate contains only this report, and its separate draft ledger row
    owns only this report; the author does not freeze, request review, accept,
    confer integration eligibility, establish G11, or accept implementation.
