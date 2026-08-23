# P15-SR-A2 Unresolved-Question Record

Status: held; no contract acceptance or implementation authority

Date: 2026-08-23

## Decision

P15-SR-A2 remains held. The repository does not determine the stage's purpose,
whether it is executable, which P15-SR-A1 result it consumes, or what closed
result it produces. Those choices are upstream of every required failure,
resource, diagnostic, and behavioral-fixture decision. Selecting them here
would invent semantics.

The precise unresolved question is:

> What repository-governed operation, if any, must P15-SR-A2 perform; which
> exact accepted P15-SR-A1 result is its input; and what exact closed result is
> its output?

No checked-in contract answers this question. `P15-SR-SEQ003` therefore applies
and the workstream moves from `draft` to `held`. This record authorizes no
implementation and no successor.

## Scope and evidence boundary

This record is the only artifact produced by the authorized
`p15-sr-a2-contract-candidate-v1` task, together with its ledger disposition.
It is a contract and fixture specification, not an evaluator, compiler pass,
runtime component, proof, conformance result, or bootstrap stage.

The decision basis is limited to:

- D1, D2, D3, D8, and D9;
- BOOT7 and BOOT8;
- the integrated post-P15-SR-A1 sequencing decision;
- the frozen P15-SR-A1 decision, report, provenance, source, tests, and
  fixtures; and
- the workstream governance contract and ledger.

Rejected-lineage summaries are hostile review context only. Private forensic
tuples, historical evaluator or projection names, Phase 10 S1/S3, SH-22, and
Phase 11 document A2 are not semantic authority for P15-SR-A2.

## Immutable P15-SR-A1 dependency

Every future P15-SR-A2 proposal must bind this complete tuple:

- integrated P15-SR-A1 main record:
  `2308a771419ac4d762b35b148146dc2df7c9b3cb`;
- frozen P15-SR-A1 candidate:
  `def733f3fbb641b49bc9495d58c4be24580b8eff`; and
- frozen artifact and source graph:
  `sha256:d701e6dab722f4fd188b74da8ee29161c775b45e0366e33897db895fdd801b4b`.

P15-SR-A1 exposes exactly `canonical-copy`, `admit-schema-registry`, and
`validate-and-copy`. Each returns exactly one of these shapes:

```clojure
{"status" "accepted", "diagnostic" "OK", "value" copied-value, "path" []}
{"status" "typed-rejected", "diagnostic" diagnostic-id, "value" nil,
 "path" bounded-path}
```

That boundary is immutable, but it is not an A2 handoff contract. Repository
evidence does not choose whether A2 would consume the accepted `value`, the
complete result envelope, registry/schema/value inputs, or a provenance
artifact. A naked value without its accepted result envelope cannot be claimed
as a P15-SR-A1 handoff. A tuple mismatch rejects a proposal with
`P15-SR-SEQ001`.

## Required semantic slots and disposition

| Slot | Supported requirement | Unresolved value |
| --- | --- | --- |
| Purpose and role | If A2 is later classified as a compiler stage, D1 requires a pass contract; if it emits an artifact, D1 requires an artifact record; D2 forbids roadmap credit for documentation alone | Whether A2 exists as executable behavior and, if so, its exact operation |
| Input and output | The frozen P15-SR-A1 public boundary cannot be mutated or bypassed; any output must be closed and explicit | Which accepted A1 result is consumed, exact arity, ownership/copy isolation, and result shape |
| Total failure | D1 requires host failures to be normalized; D8 forbids undefined behavior and requires every dangerous operation to have one declared outcome | Public entrypoint boundary, malformed-input behavior, interruption policy, caught exception set, excluded process failures, and terminal result |
| Resource limits | Any bounded executable role must define deterministic input, depth, live-state, work, and output limits before implementation | Every numeric limit, charge unit, arithmetic overflow rule, and terminal reserve size |
| Reservation lifecycle | Work, allocation, state change, and output may not amplify before the governing contract's reservation permits them | Budget scope and the exact atomic reserve, commit, release, rollback, cleanup, and refund rules |
| Diagnostics | D9 requires stable testable runtime checks; sequencing diagnostics remain stable for coordination failures | A2 runtime IDs, phase order, precedence, paths, wording boundary, and relation to A1 diagnostics |
| Equality and order | Results and diagnostics must be deterministic and must not inherit host accident | A2 equality, ordering, mixed-type behavior, normalization, and iteration rules |
| Fixtures and evidence | Positive and negative conformance fixtures, provenance, validation receipts, and independent review are required before implementation can be considered | Behavioral inputs and expected runtime outputs cannot be instantiated without the preceding slots |

The P15-SR-A1 numeric envelope and `E-*` diagnostics are expressly A1-specific,
not universal Gravity or A2 defaults. Reusing them would not close these slots.
The historical bounded-expression-evaluator and projection hypotheses are not
checked-in accepted semantics and remain rejected as inference sources.

## Total failure and resource decision

No A2 totality claim is made. A future contract must place arity checking,
input admission, selected behavior, output construction, and ordinary host
exception normalization inside one closed public boundary. It must state how
interruption is handled and enumerate any excluded process failures. Malformed
input may not escape as a raw host exception.

No A2 numeric resource value or accounting transition is selected. A future
bounded executable contract must define one deterministic budget model before
implementation, including:

- the unit and limit for every input, depth, live-state, work, and output
  counter;
- the reservation made before each chargeable operation or allocation;
- atomic failure behavior with no partial state or output;
- the point at which reserved work commits;
- which unstarted reservations may be released or rolled back;
- whether committed work is refundable;
- checked arithmetic and overflow behavior;
- terminal-result reservation and cleanup on every exit; and
- evidence that observable resource use cannot precede its reservation.

Until those choices are sourced and exact, neither `E-BOUND` nor any other A1
failure can be promoted to an A2 runtime diagnostic.

## Diagnostic decision

This record defines no runtime diagnostic catalog. These already accepted
coordination diagnostics apply to contract review:

| ID | Contract-review meaning |
| --- | --- |
| `P15-SR-SEQ001` | the immutable P15-SR-A1 tuple does not match |
| `P15-SR-SEQ002` | a seed-retirement A-label is unqualified or collides with Phase 11 |
| `P15-SR-SEQ003` | one or more required A2 semantic slots is open |
| `P15-SR-SEQ004` | a required dependency is below its admitted state |
| `P15-SR-SEQ006` | implementation or downstream authority is claimed beyond the record |

The runtime catalog, phase order, precedence, path model, and equality or
mixed-type rules remain unresolved. A future contract must specify them before
any code or executable fixture is authorized.

## Fixture specification

These are contract-review specifications, not executed A2 conformance evidence.

Accepted contract records must specify:

1. the exact immutable P15-SR-A1 tuple and qualified P15-SR-A2 label;
2. a repository-sourced purpose, entrypoint, input, and closed result;
3. exact totality and excluded-failure boundaries;
4. every numeric resource limit and accounting transition;
5. stable runtime diagnostic IDs and deterministic precedence;
6. equality, order, normalization, and mixed-type rules;
7. positive fixtures for every selected behavior and boundary value;
8. negative fixtures for wrong arity/type, malformed A1 input, unsupported
   behavior where applicable, mixed types,
   limit-minus-one/limit/limit-plus-one, reservation failure, exception
   totality, interruption, order determinism, and downstream overclaim; and
9. provenance, validation receipts, and independent accepted review.

The following contract records are rejected:

| Fixture specification | Expected classification |
| --- | --- |
| exact A1 tuple mismatch | `P15-SR-SEQ001` |
| bare or Phase 11-colliding A2 label | `P15-SR-SEQ002` |
| inferred evaluator, projection, SH-22, S1, or S3 role | `P15-SR-SEQ003` |
| any open role, boundary, totality, limit, accounting, diagnostic, equality, or fixture slot while claiming acceptance | `P15-SR-SEQ003` |
| A1 dependency below `integrated` | `P15-SR-SEQ004` |
| implementation code, A3/Stage B/C activation, G1-G6 credit, or product/release claim | `P15-SR-SEQ006` |

This unresolved record itself is a valid candidate outcome: exact dependency,
evidence gap, held disposition, and nonclaims are explicit. It is not a
positive executable fixture and confers no implementation authority.

## Provenance and review obligations

The exact candidate must record its base, commit, tree, changed paths, governing
contracts, focused Clojure validation receipt, language-boundary validation
receipt, governance and diff checks, residual Clojure/JVM boundary, and
independent read-only verdict. This documentation-only record has no
compiler-stage output, so BOOT5/BOOT7 stage compatibility and equivalence are
not applicable and stage advancement is forbidden. Any later bootstrap
artifact must satisfy BOOT8 with source graph, compiler, lockfile, build,
environment, dependency, evidence, and builder identities. A fixture
specification is not execution evidence, and a review is not a proof.

## Nonclaims and held successors

This record does not establish:

- an A2 executable role, implementation, API, result, diagnostic, or resource
  model;
- a compiler stage, S1/S3 implementation, SH-22 completion, BOOT5 support, or
  BOOT7 equivalence;
- P15-SR-A3, Stage B, or Stage C authority;
- G1 through G6 authority;
- self-hosting, Clojure-seed retirement, seedless operation, or release; or
- correctness, safety, performance, or conformance beyond the frozen
  P15-SR-A1 dependency.

P15-SR-A3, Stage B, Stage C, G1-G6, self-hosting, seed retirement, and release
remain held. A later implementation decision may not be considered until a new
repository-backed A2 contract closes every unresolved slot and receives a fresh
independent review.

## Independent review

An independent read-only review accepted this unresolved-question record and
held disposition after two fixture/receipt wording gaps were corrected. The
review does not accept A2 semantics or authorize implementation. The reviewer
verified the exact P15-SR-A1 tuple, lifecycle, held successors, nonclaims,
terminology boundary, and two-path diff; governance, focused P15-SR-A1 tests,
the language-boundary gate, ASCII/newline checks, and `git diff --check` passed
with no remaining findings.
