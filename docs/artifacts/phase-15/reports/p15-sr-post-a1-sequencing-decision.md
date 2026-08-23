# Post-P15-SR-A1 Seed-Retirement Sequencing Decision

Status: accepted sequencing decision; exactly one contract candidate authorized

Date: 2026-08-23

## Decision boundary

This decision establishes a coordination sequence after the integrated bounded
canonical-schema candidate. It does not define an evaluator, projection
compiler, backend, runtime, verifier, artifact format, or release mechanism.
Those semantics are absent from the accepted repository contracts and are not
invented here.

The selected model is contract-first and serial within the seed-retirement
micro-stages:

```text
P15-SR-A1 (integrated, immutable)
  -> P15-SR-A2 contract candidate (the only authorized candidate)
  -> P15-SR-A3 (held)
  -> Stage B (held)
  -> Stage C (held)

G1 (held) -> G2 (held) -> G3/G4 (held) -> G5 (held) -> G6 (held)
```

P15-SR-A2 is the only authorized draft. Its successors and every G gate remain
held, and this decision creates no dependency edge between the two chains. A
gate-specific decision is still required before any G candidate is authorized.

## Immutable dependency

`P15-SR-A1` is the new qualified reference to the existing integrated
workstream `a1-canonical-schema-candidate-v1`, whose source and artifact tuple
is frozen. It does not rename or modify that record. Every downstream record
must bind this complete tuple:

- integrated main record: `2308a771419ac4d762b35b148146dc2df7c9b3cb`;
- frozen candidate commit:
  `def733f3fbb641b49bc9495d58c4be24580b8eff`; and
- artifact and source-graph identity:
  `sha256:d701e6dab722f4fd188b74da8ee29161c775b45e0366e33897db895fdd801b4b`.

A mismatch is `P15-SR-SEQ001` and rejects the downstream candidate. No
downstream decision may mutate, reinterpret, or replace the frozen P15-SR-A1
source, tests, fixtures, ADR, report, or provenance record.

## Terminology

New records use `P15-SR-A1`, `P15-SR-A2`, and `P15-SR-A3` in prose and
`p15-sr-a1`, `p15-sr-a2`, and `p15-sr-a3` in machine identifiers. `SR` means
seed retirement. This convention is introduced by this decision; it is not a
retroactive claim about earlier records.

Bare `A1`, `A2`, and `A3` remain the normative Phase 11 AI document labels:
sequence 154 AI Programming Model, sequence 155 Model Provider, and sequence
156 Prompt and Structured Output under
`docs/phase-11-ai-and-agentic-programming/`. A new seed-retirement record using
an unqualified A-label is `P15-SR-SEQ002` and is rejected. Existing frozen
workstream text and existing Phase 11 maps remain unchanged.

## P15-SR-A2: contract-definition candidate

### Scope and authorization

The repository does not establish what executable role, if any, P15-SR-A2
should have. This decision authorizes exactly one candidate: a
documentation-and-fixture-specification candidate that resolves that question
and closes a contract before implementation. It authorizes no Clojure or
Gravity implementation code.

The candidate's exact inputs are the immutable P15-SR-A1 tuple, D1, D2, D3,
D8, D9, BOOT7, BOOT8, this decision, and the checked-in frozen P15-SR-A1
decision, report, provenance, tests, and fixtures. The counterexample summaries
in the frozen P15-SR-A1 decision are review context, never identity inputs or
semantic authority. SH-22 and the full S1/S3 contracts are not substitutes for
this narrower contract-definition task.

The candidate's exact output is one independently reviewable P15-SR-A2
semantic decision plus accepted and rejected fixture specifications, or an
unresolved-question record that authorizes no successor. Before any
implementation can be authorized, that decision must close all of these
slots:

1. the stage purpose and whether it has any executable behavior;
2. the exact P15-SR-A1 registry/value entry boundary and one closed result
   shape;
3. a total failure boundary, including wrong arity, malformed admitted data,
   interruption, host exceptions, and explicitly excluded process failures;
4. deterministic diagnostic precedence and stable diagnostic identifiers;
5. numeric limits for input size, depth, live state, work, and output, with
   reserve-before-work and commit/rollback rules that forbid amplification;
6. deterministic equality, ordering, and mixed-type behavior;
7. positive fixtures for every selected behavior and boundary value; and
   negative fixtures for an unsupported operation if an operation set is
   selected, wrong arity/type, mixed types, malformed P15-SR-A1 input,
   limit-minus-one/limit/limit-plus-one, reservation failure, exception
   totality, interruption, nondeterministic order, and attempted downstream
   authority; and
8. provenance, focused Clojure validation, language-boundary validation, and
   independent read-only review requirements.

Until every slot has an exact value, the proposal is rejected with
`P15-SR-SEQ003`. A dependency below `integrated` is rejected with
`P15-SR-SEQ004`. Accepted contract behavior means all eight slots are closed,
the exact P15-SR-A1 tuple matches, all specified hostile fixtures are
classified, and the independent review accepts the frozen documentation tuple.
Rejected behavior includes copying prior opcode semantics, leaving any slot
open, starting implementation, modifying P15-SR-A1, accepting host exceptions,
charging after work begins, or claiming downstream completion.

An unresolved-question record is a valid review output but is not an accepted
P15-SR-A2 contract: it moves the workstream to `held` and authorizes no
implementation or successor.

No stage purpose, numeric limits, operations, runtime diagnostic set, or
executable result format is selected here because the repository supplies no
sound value for them. Resolving those exact values, or recording the exact
question that prevents resolution, is the only authorized P15-SR-A2
candidate's task.

### Evidence and nonclaims

The P15-SR-A2 contract candidate must provide its decision, accepted/rejected
fixture specifications, a frozen identity tuple, governance and
language-boundary receipts, and an independent accepted review. It receives no
implementation, compiler-stage, S1, S3, BOOT5, BOOT7-equivalence, P15-SR-A3,
Stage B/C, G1-G6, self-hosting, seed-retirement, or release credit.

## P15-SR-A3: held unresolved slot

`P15-SR-A3` is a held post-A2 decision slot. Its only currently admissible
inputs are the immutable P15-SR-A1 tuple and a future integrated P15-SR-A2
contract and, if that contract requires it, implementation. It has no selected
executable role or output.

No P15-SR-A3 candidate is authorized. A future P15-SR-A3 decision must close
its purpose, input/output boundary, identity inputs, aliasing/copy-isolation
rules if applicable, resource reservation, total failure semantics, stable
diagnostics, and accepted/rejected fixtures before implementation. Acceptance
means those slots are exact, P15-SR-A2 is integrated, the P15-SR-A1 tuple
matches, and independent review accepts the frozen contract. Rejection
includes an invented or open role, an identity that omits dependencies,
unbounded work, open diagnostics, P15-SR-A1/P15-SR-A2 mutation, or any
implementation started before a separate authorization. A P15-SR-A3 proposal
while held is `P15-SR-SEQ005`.

P15-SR-A3 grants no Stage B/C, G1-G6, compiler-stage, self-hosting,
seed-retirement, or release authority.

## Stage B and Stage C

The retained labels mean only:

- Stage B: an unresolved decision slot after integrated P15-SR-A3; and
- Stage C: an unresolved decision slot after integrated Stage B.

Their data models, operations, limits, diagnostics, fixtures, and artifacts
are unresolved. Both remain held, and neither may be used as evidence for a G
gate. This decision records dependency order only.

## G1-G6 gate map

The ledger titles and dependency edges are authoritative coordination facts;
the repository does not otherwise define the word `closure` for these gates.
The table therefore records the smallest concrete candidate evidence anchors,
not formal G semantics. It does not authorize implementation or assert that
the criteria are already met.

| Gate | Dependencies | Required future acceptance evidence | Held condition |
| --- | --- | --- | --- |
| G1 backend closure | integrated P15-SR-A1 | A gate-specific ADR; D1 canonical-pass inputs/outputs and preserved facts; B1 backend-interface profile/target/ABI/runtime/provider selection; B14 positive and negative conformance fixtures; deterministic artifacts and provenance | No G1 ADR, candidate, fixtures, diagnostics, or review exists |
| G2 execution kernel closure | accepted/integrated G1 | A gate-specific ADR; D1 execution-layer and R1 runtime-family/service/capability boundary; accepted and rejected execution fixtures; bounded failure behavior; stable diagnostics and provenance | G1 is held; executable substrate is unaccepted |
| G3 containment closure | accepted/integrated G2 | A gate-specific ADR; D8 classification of every dangerous operation; explicit effects/capabilities, unsafe-island audit, host-service denial fixtures, and containment evidence | G2 is held and exact containment semantics are undefined |
| G4 verifier closure | accepted/integrated G2 | A gate-specific ADR; D9/BOOT7 equivalence modes, TEST13/C18 conformance and mutation fixtures, stable diagnostics, reproducibility, and provenance | G2 is held and exact verifier scope is undefined |
| G5 materialization closure | accepted/integrated G2 and G4 | A gate-specific ADR; B13 and package contracts for artifact/manifest/hash/source-map/proof/safety/effect/capability/runtime/target data; malformed/stale/substitution fixtures; reproducibility and provenance | G2 and G4 are held and exact materialization scope is undefined |
| G6 release closure | accepted/integrated G5 | A gate-specific ADR; BOOT7/BOOT8 equivalence and lineage; GOV6 admission; SH-26 through SH-29 completion; reproducible release, SBOM, signature, revocation, TCB, and seed-boundary evidence | G5 is held and the Clojure seed boundary remains active |

For every gate, acceptance also requires the workstream-governance lifecycle,
an exact frozen candidate tuple, successful focused validation, and independent
accepted review. Missing or attempted bypass of a held dependency is
`P15-SR-SEQ004`. Any claim beyond a record's authority is
`P15-SR-SEQ006`.

## Alternatives rejected

1. **Implement the historical evaluator model now.** Rejected because its
   proposed role, operation set, resource envelope, totality, and diagnostics
   were not accepted repository semantics.
2. **Treat SH-22, S1, or S3 as P15-SR-A2.** Rejected because their domains and
   authority differ from the frozen P15-SR-A1 consumer boundary.
3. **Run P15-SR-A2 and P15-SR-A3 in parallel.** Rejected because P15-SR-A3's
   input contract depends on the still-unresolved P15-SR-A2 output.
4. **Wake G1-G6 from P15-SR-A1 integration alone.** Rejected because every
   gate lacks a gate-specific decision and evidence.
5. **Create detailed Stage B/C or G semantics here.** Rejected because the
   repository does not support them and doing so would broaden this decision.

## Independent decision review

An independent read-only review accepted this decision after two wording
findings were addressed: P15-SR-A2 is the sole authorized draft rather than a
held stage, and the P15-SR-A1 workstream is integrated while its exact source
and artifact tuple remains frozen. The reviewer independently passed the
governance validator, focused P15-SR-A1 tests, language-boundary gate, and diff
checks and found no remaining state, dependency, scope, or code-change issue.

## Validation and next candidate

The single next candidate is `p15-sr-a2-contract-candidate-v1`: close and
independently review the eight P15-SR-A2 semantic-contract slots above without
adding implementation code. All other post-A1 candidates remain held.

This decision changes only this report and the coordination ledger. It does
not modify the frozen P15-SR-A1 tuple or any implementation, test, fixture,
roadmap, phase contract, or provenance record.
