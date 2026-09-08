# SH-07 B51 Binding-Replay Index Architecture Independent Review V1

## Decision

ACCEPT the exact frozen architecture candidate for a separately governed
implementation. The proposed substitution is semantically sound because the
existing locally constructed form index records the same definition-syntax-id
membership as the reference scan, and the unchanged duplicate guards establish
that every admitted membership has multiplicity at most one before indexed
presence is interpreted.

The reviewed tuple is:

- base commit:
  `0a391faa3ea0cc299e60b476df0c3718d4b5af8c`;
- candidate commit:
  `eed780e60c54dde3a381d1889c6dcf62301be728`;
- candidate tree:
  `4bbeafc71259e81241b5493c17e7a43011e161bd`;
- architecture report:
  `docs/artifacts/phase-15/reports/sh07-b51-binding-replay-index-architecture-v1.md`;
- report SHA-256:
  `5d7d8e8f4a2799b8a29ee12137ecc4a300db2b3d33d563a747852254f12aeacf`;
- provisional evidence:
  `docs/artifacts/phase-15/reports/sh07-b51-binding-replay-index-provisional-evidence-v1.md`;
- provisional-evidence SHA-256:
  `741c75d0c509de947b45c997ca2cb03ee0aca783b4767a99d2fcf775ba843e53`;
- review-pending lifecycle HEAD:
  `ea8fa65aba671f03bc27430d240e35226015ad48`.

The review-pending worktree was clean before this artifact was added. The
candidate range adds exactly the architecture report and provisional-evidence
artifact. The post-candidate range through the review-pending head changes only
the active workstream record and compact ledger.

Reviewer identity: `sh07-b51-binding-replay-index-architecture-reviewer-v1`,
canonical task `/root/binding_replay_review`.

## Source and semantic equivalence

Independent inspection of implementation-localization commit
`6974b0abb9ee27db57014a247fbb13718de66603`, tree
`1c0b09f2124fc0b674874d378dfdac15541239fd`, confirmed the exact source
topology used by the decision:

- `sh07-b51-index-request` constructs `:forms/:by-secondary` from the exact
  request form vector using each form's `:syntax-id`;
- `sh07-b51-find-form-by-syntax` reads only that secondary map;
- `sh07-b51-derived-no-form-failing-binding-ids` checks form primary and
  secondary duplicates and then binding primary and secondary duplicates before
  entering the binding loop;
- the reference loop performs exactly the named full-vector
  `sh07-b51-find-by` lookup before the binding-record shape predicate; and
- the only direct caller of the changed function is record-shape replay. Its
  four live replay entries each construct the index locally from the same
  request before calling it.

With both duplicate guards false, a valid matching form record appears exactly
once in the vector and exactly once under its syntax id in the secondary map.
A map result is therefore the reference count one, and nil is the reference
count zero. Map last-write behavior is never accepted as uniqueness evidence.
Malformed form or binding records cannot acquire authority from indexed
presence: the unchanged binding predicate and subsequent complete form-record
replay still reject them. The lookup remains before the binding-shape predicate,
so the retained per-binding validation order is unchanged. On a retained early
rejection, the dynamic call law applies to the exact visited binding-table
prefix; it does not authorize visiting or skipping later bindings.

The index is ephemeral Gravity data. It is not accepted from the request,
carrier, runtime, caller, cache, or host. The public source, plan, manifest, and
carrier admission chain prevents direct invocation with a forged index from
becoming a public authority path. The separately authored Root-6 replay neither
uses nor inherits the producer index result.

## Duplicate, missing, and external-binding laws

The decision preserves the exact outcome partition:

- duplicate form primary or syntax identity rejects before indexed presence;
- duplicate binding primary or definition identity rejects before indexed
  presence;
- core and import bindings retain their no-local-form allowance;
- a unique local form continues for every binding class;
- an absent namespace form appends its binding id once in binding-table order;
  and
- an absent nonwaivable binding rejects atomically with the existing reason and
  empty partial products.

Indexed absence alone does not authenticate a namespace binding. The unchanged
projection path still requires a unique resolution, exact upstream and
declaration agreement, an allowed namespace resolution order, external
membership, and terminal `:authenticated-external`. Missing or malformed
declarations, zero or multiple resolutions, local membership, disagreement,
unauthorized resolution order, and unauthorized cross-fragment edges therefore
remain fail-closed.

The proposed edit does not participate in selecting the owning top-level
definition or public root. Attempt-32 owner, route, root, plan, and arity
admission and V3's caller-blind child Root-1 entry remain unchanged.

## External-edge identity and census

The exact source occurrence is one producer-B-to-producer-A call. Replacing
`sh07-b51-find-by` arity 3 with
`sh07-b51-find-form-by-syntax` arity 2 preserves the callsite cardinality and
the `:producer-internal` owner class. It substitutes one census occurrence and
therefore leaves the closed external-link count at exactly 1,480.

The decision correctly requires the complete affected identity cascade to be
rebuilt: producer-B source and semantic identities, function/callsite and plan
identities, link manifest and routes, M0/M1/M2/M3-v2/S30, semantic root,
certificates, physical census, carrier, pins, checkers, and evidence. Static-row
and runtime-trace reference equivalence is evaluated under that deterministic
old-to-new identity mapping. The sole semantic row delta is the named callee and
arity substitution; regenerated module, plan, manifest, route, bundle, and
ordinal identities are consequences of that authenticated substitution, not
permission for an additional call or unexplained trace row. Every unaffected
caller/callee/owner/arity/order relation must remain equivalent, and every
identity must match the newly frozen tuple.

The required mutation family covers caller, callee, owner, arity, callsite,
source, function, plan, route, manifest, duplicate-row, and removed-row
tampering. This is sufficient to prevent the indexed call from bypassing the
existing authenticate-before-execute boundary.

## Metering and evidence findings

The architecture changes an algorithm, not a limit or authority source. The
existing index build remains real counted Gravity work. Only transitions from
the removed repeated full-form scan may disappear. The decision expressly
forbids raising, resetting, refunding, hiding, reclassifying, aggregating, or
moving fuel into a host answer.

The retained ceilings are exact:

```text
8,388,608 machine transitions per external machine
4,096 live frames
65,536 authenticated external calls
1,480 linked external edges
```

The successor evidence gate covers the final legal recurrence and first-plus-
one rejection for transitions, frames, and calls; authentic direct Root 1,
direct Root 6, and selected Root 8; V3 pending/mint/scope/receipt/cleanup
mutations; accepted and rejected scan/index parity; external-binding and edge
mutations; and atomic final-snapshot publication and reload validation.

The D6/D9 record must separate semantic recurrence counts from trusted-runtime
bookkeeping and bind source/function/phase counts, binding iterations, form
visits, index builds and lookups, retained digest predicates, resource maxima,
toolchain, command, wall and CPU time, allocation or peak RSS, repetition, and
variance. Independent reference-scan and successor-index checkers require
complete declared closures; neither checker nor the materializer may import an
unbounded bootstrap closure, reuse a supplied index, or turn a provisional pin
into semantic authority.

The 250,000- and 500,000-transition observations are correctly limited to
localization. They prove neither completion within the fixed bound nor
performance, correctness, implementation acceptance, or integration.

## Validation receipts

The following bounded gates passed from the clean review-pending worktree
before this review artifact was added:

```text
clojure -M tools/validate_architecture_authority.clj \
  --report docs/artifacts/phase-15/reports/sh07-b51-binding-replay-index-architecture-v1.md
architecture authority validation passed

clojure -M tools/validate_workstream_governance.clj
validation passed: 158 workstreams; accepted=3, held=9, integrated=92,
integration-eligible=1, rejected=48, review-pending=2, superseded=3

clojure -M tools/validate_gravity_docs.clj
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders

clojure -M tools/validate_full_language_roadmap.clj
full-language roadmap validation passed

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, 0 failures, 0 errors

git diff --check \
  0a391faa3ea0cc299e60b476df0c3718d4b5af8c..eed780e60c54dde3a381d1889c6dcf62301be728
passed
```

Base ancestry, exact candidate/tree identities, report and evidence digests,
candidate and post-candidate path sets, integrated Attempt-32 and V3 dependency
states, source cutpoint, index construction, replay caller closure, duplicate
guards, and clean status were checked independently.

## Authority and SH-07 status

This review accepts only the exact architecture tuple above. It authorizes the
coordinator to record architecture acceptance and reconcile it onto current
main before registering a conforming implementation candidate. It grants no
acceptance to implementation bytes, materializer or checker code,
certificates, carrier, transcripts, pins, runtime results, fixed-fuel
completion, integration, performance, broader core-form coverage, roadmap
credit, self-hosting, seed retirement, release readiness, or aggregate SH-07
completion.

Overall SH-07 remains partial at four of the current eight completion stages.
This acceptance closes only the architecture-review gate within stage five.
