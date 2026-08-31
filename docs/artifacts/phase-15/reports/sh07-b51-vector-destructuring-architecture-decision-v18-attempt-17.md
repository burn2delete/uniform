# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 17

Status: Draft caller-scoped byte-equality correction for pre-freeze audit

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds terminally rejected Attempt 16. The
independent reviewer accepted its bounded independent probes, commutative
Boolean-OR failure aggregation, opaque
`:unordered-decoration-failure`, same/different multi-failure behavior,
current-collection versus child-failure split, limits/cycles, compiler/runtime
closure, G13 preservation, migration, pins, evidence structure, and nonclaims.

Attempt 16 was rejected for one wording contradiction. Several sentences
required Root 1, the independent source oracle, and Root 6 to return
byte-identical public results to one another. G13 requires those three callers
to retain distinct envelopes and/or reasons. Their results cannot generally be
equal across callers.

Attempt 17 changes only that equality scope. Each caller must be byte-stable
against itself across unordered carrier permutations, collection kind,
failure count/multiplicity, and hidden cause combinations. No cross-caller
byte equality is required or permitted. Every accepted Attempt-16 semantic
rule otherwise remains exact.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and terminal history

```text
authoritative main commit / terminal Attempt-16 commit
50e8d14422effd26a6ded2f34f94765f050461ac

authoritative main tree
2525039e65c41c00af39807f948f45d16f44ee53

integrated G13 main
3ed00a806f63e9263305f7f51c69897683f81e3b

terminal Attempt-16 candidate
6f9e736710e2838026e08a5379f1029427ad8b31

terminal Attempt-16 candidate tree
95a61137482291664c20f7fda0ca46669f6b6e78

terminal Attempt-16 report SHA-256
e6ccc61c2bf2e7c205e4d7f620b2a506996d42b68b62753ff1a05d9e82560d3c
```

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Incorporated Attempt-16 authority

Attempt 17 incorporates Attempt 16 in full except every cross-caller equality
phrase superseded below. The exact pure capability-free binary UTF-8 comparator
remains unchanged. B51 still uses recursive C11 canonical-readable order: sets
by member text, maps by key text then value text, vectors/lists in authenticated
order. Unicode, character, decimal, nested collection, collision, and no-host
leakage semantics remain exact. G13 digest identity remains separately
`reader-canonical-hash`; digests never order collections.

Attempt-15/16 bounds remain exact:

```text
maximum value-node occurrences                 4096
maximum root-relative depth                      96
maximum width of any collection                 512
maximum UTF-8 bytes of any scalar spelling     32768
maximum final canonical output UTF-8 bytes     262144
```

Counting points, root depth zero, at-limit/+1 behavior, checked aggregation,
active-path identity cycles, direct/indirect `:cyclic-value`, shared acyclic
recounting, explicit bounded traversal, and caller mappings remain exact.

Every set member and every map key/value is still independently probed exactly
once under the same owning active-ancestor context and independent sibling
state. Failures still aggregate only through commutative Boolean OR. One or
many failures, same or different hidden reasons, key or value, and every
carrier permutation still yield exact contained
`:unordered-decoration-failure`. No reason, coordinate, role, ordinal, carrier
identity, path, partial text, hash, audit trace, or exception enters the fold.

If every probe succeeds, summaries still combine commutatively before complete
canonical texts sort. Current-collection admission failures retain direct
Attempt-15 reasons. Failures inside unordered child probes become the generic
contained reason. Ordered vector/list failure behavior remains left-to-right.

## Exact caller identities and envelopes

The three mandated callers are distinct semantic evidence surfaces:

```text
caller R1  public Root 1 / B51 producer
caller O   independently authored source oracle
caller R6  public Root 6 / independent success verifier
```

Their failure mappings remain distinct:

```text
R1 unordered decoration failure
  exact six-key :template-boundary-rejected envelope
  outer reason :source-integrity-mismatch

O unordered decoration failure
  exact independently authored C6-VERIFY oracle failure record

R6 unordered decoration failure
  exact six-key :independent-verifier-boundary-rejected envelope
  outer reason :source-integrity-mismatch
```

The source oracle is not a public B51 root envelope. Root 1 and Root 6 have
different mandatory public tags and boundary identities. Their difference is
evidence of authority separation, not nondeterminism.

No implementation, test, review, or report may normalize those callers into a
shared result, compare their complete results for equality, substitute one
caller's expected bytes for another, or weaken the mandated caller-specific
tag/reason to force cross-caller agreement.

## Exact within-caller equality law

Let `result(C, raw, carrier)` mean the complete semantic result emitted by
caller `C` for authenticated semantic raw under one admissible substrate
carrier realization. For each fixed caller independently:

```text
for C in [R1 O R6]
and any carrier realizations P and Q of the same semantic raw:

bytes(result(C, raw, P)) = bytes(result(C, raw, Q))
```

This equality covers the complete caller-specific result, not only a tag or
contained reason. It must hold within `R1`, within `O`, and within `R6` across:

- every map/set insertion or iteration permutation;
- one failure versus multiple failures;
- duplicate multiplicity of the same hidden reason at distinct semantic
  children;
- same-reason versus different-reason hidden cause combinations;
- set member, map key, map value, both key/value, and different-entry failures;
- nested unordered collections and shared acyclic subgraphs; and
- sequential, batched, or permitted parallel probe scheduling.

Failure count/multiplicity and hidden cause combinations may differ only when
the fixture intentionally changes the semantic raw while preserving the same
generic unordered failure classification. For each caller, all such generic
failure fixtures must still emit that caller's one exact generic-boundary byte
result, because the hidden audit trace is non-semantic and stripped.

No coordinate or carrier detail may perturb within-caller bytes. Private audit
traces may differ in observation order but never enter the semantic result,
digest, equality comparison, pin, or public evidence.

## Explicit prohibition of cross-caller equality

The architecture defines no law of the form:

```text
bytes(result(R1, raw, carrier)) = bytes(result(O, raw, carrier))
bytes(result(R1, raw, carrier)) = bytes(result(R6, raw, carrier))
bytes(result(O, raw, carrier))  = bytes(result(R6, raw, carrier))
```

Those equalities are forbidden acceptance criteria. A coincidental equality of
some internal contained token does not make the complete caller results equal
and conveys no authority. Cross-caller testing instead verifies the exact
mandated difference:

- R1 has its exact Root-1 boundary tag/envelope;
- O has its exact C6-VERIFY oracle record; and
- R6 has its exact Root-6 boundary tag/envelope.

The shared semantic fact is only that each independently concludes the same
opaque unordered-decoration classification before applying its own mandated
caller mapping. That classification agreement is checked structurally in
independent evidence; it is not complete-result byte equality.

## Corrected evidence matrix

Evidence is grouped by caller. For each of `R1`, `O`, and `R6`, build at least
three carrier realizations for every applicable map/set fixture and compare
complete bytes only within that caller's row:

```text
fixture family                         R1=P=Q=...  O=P=Q=...  R6=P=Q=...
one failing set member                 required    required   required
same hidden reason at two members      required    required   required
scalar-limit plus cycle                required    required   required
depth plus width                       required    required   required
map key only / value only              required    required   required
key plus value in one entry            required    required   required
same/different reasons across entries  required    required   required
nested map/set multi-failure           required    required   required
permitted probe schedule permutations  required    required   required
```

`R1=P=Q` means the complete R1 result is byte-identical across R1 carrier
variants P and Q. It never means R1 equals O or R6. The same scoping applies to
the other columns.

Cross-caller assertions must instead prove caller distinction:

```text
R1 exact tag/envelope != R6 exact tag/envelope
R1 exact public envelope is not O oracle record
R6 exact public envelope is not O oracle record
```

Fixtures with one failure, multiple failures, same/different hidden causes,
key/value roles, and permutations must retain those inequalities while each
column remains internally stable.

At-bound evidence remains required for node 4096, depth 96, width 512, scalar
spelling 32768 UTF-8 bytes, and output 262144 UTF-8 bytes, plus every +1. For
each caller, carrier permutations around the same at-bound semantic value must
be byte-identical within that caller. Cross-caller boundary/tag differences
remain mandatory. Child-local limit/cycle failures become generic; direct
current-collection limits retain exact Attempt-15 reasons.

Mutation evidence must reject:

- any cross-caller complete-result equality assertion;
- using R1 golden bytes for O or R6, or any reciprocal substitution;
- normalizing caller tags/reasons/envelopes into a common record;
- comparing only tags while ignoring other bytes within a caller;
- allowing carrier order, multiplicity, hidden reason combination, key/value
  role, nested order, or probe schedule to change bytes within one caller;
- leaking private audit cause/order/count into any caller's semantic result;
- using cross-caller difference as permission for within-caller instability;
- canonical/coordinate/reason priority, first failure, short-circuit, partial
  text, host iteration, task completion, exception order, or hash ordering; and
- any drift from Attempt-16 probe/OR, Attempt-15 bound/cycle, or G13 authority.

Independent authorship remains mandatory. The producer, source oracle, and Root
6 may agree on the opaque classification but may not share probe, aggregation,
sorting, candidate, or caller-mapping helpers. Static closure evidence must
prove each caller constructs its own exact envelope/reason.

## Preserved caller and root authority

Root 1 remains the sole pending detector and uses its exact boundary envelope
for contained ordering failure. Root 6 remains separately authored,
success-only, and uses its exact independent-verifier boundary. The source
oracle remains non-public C6-VERIFY evidence. Root 8 remains the unary exclusive
pending finalizer, calls Root 1 exactly once, and cannot call the comparator,
printer ordering closure, probe, fold, oracle, or Root 6.

Root-4/5 authority, Roots-4-through-7 success digest replay, the eight pending
reasons, duplicate `[earlier, later]`, recur mappings/priority, reader-canonical
digest algorithm, every ABI/tag, and every boundary remain exact.

Counts remain exactly 8 roots, Root-8 arity 1, 6 envelope keys, schema 18, 19
success purposes, 58 edges, 94/174 controlled paths, 4 outcomes, 1 pending
detector, 4 pending families, 8 reasons, 2 resource reasons, 2 unreachable
mappings, and 1 failure-only purpose. Caller-scoped equality adds no field,
variant, reason, edge, purpose, or path.

## Compiler/runtime closure

Attempt-16 compiler/runtime closure and bounded-work rules remain exact.
Producer and Root 6 independently author bounded probes, active-path handling,
OR aggregation, summary combination, sorting, and caller mapping. The oracle is
also independently authored. Only admitted scalar predicates, UTF-8
validity/count, `sh07-canonical-text-compare`, and
`sh07-declared-digest-hash` may be shared.

Backends may batch/parallelize only with exact within-caller byte evidence.
Host iteration, stack, exception order, task order, cancellation, object order,
printer, comparator, locale, encoding, reflection, callback, FFI, generic sort,
and generic compare remain non-authoritative.

## Pins and implementation consequences

Attempt 17 authorizes no implementation or pin change. Implementation remains
blocked until an exact Attempt-17 tuple is frozen, independently accepted, and
reconciled to authoritative main. A later atomic implementation must preserve
distinct caller mappings while proving within-caller stability. No partial
comparator, probe, producer, oracle, Root6, evidence, or pin change may land.

Frozen B47 sources/pins remain unchanged unless separately governed. No
repository-wide C2 rewrite, public comparator, generic sort/failure API,
cross-caller normalized envelope, or unrelated pin churn is authorized.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-17`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. Its lifecycle
dependency is integrated Attempt 13. It preserves terminal Attempts 14, 15,
and 16 as exact rejection history and addresses Attempt 16's sole wording
blocker. It starts from authoritative main
`50e8d14422effd26a6ded2f34f94765f050461ac`.

This task creates an immutable report-only candidate followed by a separate
draft ledger registration. It does not freeze, request review, accept, or
confer integration eligibility. The report candidate owns only this file;
draft registration owns only `contracts/workstream-ledger.json`.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded printer/
comparator/probe substrate, and observer. It is not semantic authority.

Within-caller byte stability does not imply cross-caller representation,
authority, or ABI equality. The generic contained reason intentionally does not
localize an unordered child failure. This report does not claim readable
printer self-hosting, general cyclic-value support, generic C2 correction,
implementation, aggregate SH-07 completion, full language support,
self-hosting, seed retirement, release, performance, or pin acceptance. All
G13 and accepted Attempt-14/15/16 nonclaims remain exact.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 17 starts from terminal Attempt-16 main and changes only
   cross-caller equality wording.
2. Attempt-16 bounded probes, commutative OR, generic failure, multi-failure,
   current/child split, limits/cycles, closure, G13, pins, and nonclaims remain.
3. Complete byte equality is required independently within R1, within O, and
   within R6 across carrier permutations, failure multiplicity, hidden causes,
   key/value roles, nesting, and probe schedules.
4. No complete-result equality is required or permitted between R1, O, and R6;
   their exact mandated envelopes/reasons remain distinct.
5. Evidence tables and prose never use ambiguous `Root1/oracle/Root6 equality`
   or cross-caller byte-identical language.
6. Scalar-limit+cycle, depth+width, key+value, same/different reasons,
   nested/permuted maps/sets, at-bound/+1, and schedule evidence is scoped per
   caller and compares complete bytes.
7. Cross-caller tests prove exact caller-specific distinction without weakening
   independent semantic classification agreement.
8. No shared mapping helper, normalized envelope, private trace leakage, or
   within-caller instability is admitted.
9. G13 topology, digest, ABI, tags, counts, pins, and nonclaims remain exact;
   no implementation changes occur.
10. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, and exact range-diff checks pass.
11. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, or claim SH-07 completion.
