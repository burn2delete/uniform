# A1 Canonical Schema Candidate v1 Report

Status: accepted and frozen for A1 only; all downstream gates remain held

Decision authority:
`docs/artifacts/phase-15/reports/a1-canonical-schema-invariant-architecture-decision.md`

Provenance record:
`docs/artifacts/phase-15/bootstrap/a1-canonical-schema-candidate-v1.edn`

## Scope and result

The candidate implements only the accepted A1 canonical-value and closed-schema
kernel in Clojure. It exposes `canonical-copy`, `admit-schema-registry`, and
`validate-and-copy`; no compiler stage, emitter, control surface, or gate was
added. The namespace has no other public vars.

The implementation uses exact host-type admission, canonical UTF-8 map-key
traversal, bounded index mergesort with in-place cycle permutation, a
depth-bounded cursor-frame acyclic-registry check, a bounded
incremental SHA-256 uniqueness preimage, full accepted-payload reservation
and commit before copy allocation, atomic diagnostic-path payload/work
reservation before path materialization, closed total results for
`java.lang.Exception`, and
interrupt restoration for `InterruptedException`.

## Conformance evidence

The focused Clojure command is:

```text
clojure -M:test --namespace gravity.self-hosting.a1-canonical-schema-test
```

Observed result:

```text
Ran 22 tests containing 167 assertions.
0 failures, 0 errors.
Clojure validation passed: 22 tests, 167 assertions, 1 namespaces
```

The suite covers accepted and rejected literal catalogs; every admitted host
type; mixed and excluded host types; all eight schema kinds; exact object and
tagged-union behavior; diagnostic rank, path, phase, and argument order;
constructor exceptions with retained attempted-output charge; interrupt
restoration; reservation/commit/release;
live-resource closure; hostile untagged-union early rejection without a
reference graph; and an unselected hostile tagged branch with unchanged phase-3
work.

The resource audit additionally covers reverse and interleaved 1,024-key
ordering, the exact 65,536-work public success,
the exact 786,432-input public success, terminal reservation commit, zero
residual reservations/live slots, a reserved 64-element diagnostic path at the
64/65 value-depth boundary, a diagnostic-path reservation fallback, a 64/65
schema-reference-depth boundary, and the near-exhaustion mixed-key
insertion-order regression.

The accepted resource edges are 65,536 UTF-8 bytes, 1,024 container items, 512
schemas, depth 64, 786,432 input-meter units, and 750,000 variable-output
payload units.
Each limit-plus-one fixture returns `E-BOUND`. The 1,024-key ordering fixture
also proves work exhaustion is terminal before a later invalid value is
visited.

Two fresh Clojure/JVM processes produced byte-identical `pr-str` output for a
canonical nested-map fixture:

```text
{"status" "accepted", "diagnostic" "OK", "value" {"z" [3 2 1], "a" {"x" 1, "y" 2}}, "path" []}
```

The final independent read-only review returned `ACCEPT` with no remaining
contract, resource, totality, fixture, or evidence blockers. The review matched
the recorded source, test, fixture, and ADR hashes to the current files.

## Provenance and nonclaims

The BOOT8 candidate record names the source graph, compiler artifact and hash,
`deps.edn` seed input, build recipe, environment manifest, dependency graph,
builder, decision, and conformance/safety links. It records BOOT5 and BOOT7 as
policy-backed not-applicable because this artifact is not a compiler stage and
does not compare compiler artifacts. Those records forbid stage advancement;
they do not waive later compiler evidence.

This report does not claim seed retirement, self-hosting, release eligibility,
or completion of A2, A3, Stage B, Stage C, or G1-G6. All remain held.

## Recommendation

The accepted tuple is frozen by the BOOT8 provenance record. Any semantic,
resource-limit, diagnostic, fixture, source, test, or decision change requires
a new reviewed decision and a new candidate identity. This freeze grants no
authority to begin A2, A3, Stage B/C, or G1-G6; those remain held pending a
separately authorized next step.
