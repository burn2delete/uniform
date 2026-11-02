# Contextual dependency token/root scan repair

## Scope and provenance

This continues the source-only PR374 dependency-construction diagnosis after
[PR555](https://github.com/burn2delete/uniform/pull/555) merged the parent
membership index as `ba9dce87d60310a3d60f9c4b15f4d725e3b2d82b`. The preserved
PR555 receipt SHA-256 is
`17915186b1cb21976b7556beac5f21d7c6eb1559abc82f1440a9a90eb31ae972`.
Its original test, review and merge receipts retain their original identities.

This increment starts from main `4b6436ffc240e8ca9303d6a3e9fc4f33656f9590`,
tree `4302eebbe95a196b185cc1656eddb1eef4f29426`. C2 lexical facts and C3 source
authentication remain governed by phase-06 C2/C3; C5 governs dependency products.
Only the hosted lexical leaf, its new compiler-free tests and this report change.
The active Reader owner, Gravity sources and Lower task ledger are untouched.

## Callgraph and retained repeated verification

The selected-reader C5 API in compiler component `00884` constructs real C3,
passes it to `sh05-macro-artifact-from-c3-artifact`, then passes the SH05 artifact
to SH06. SH05 authenticates its C3 parent and invokes `c3-syntax-validate!` before
expansion. Its construction proof and final verifier each call C3 authentication
directly and through a fresh C3 capability proof. SH06 authenticates that SH05
parent with its owner verifier and separately validates nested C3.

Expanding those SH05 passing-path call sites without transaction reuse gives
three `parse-module` passes over retained forms and five C3 authentication calls
before the SH06 handoff. These are source-derived counts, not observed executions.
The non-registered-projection C3 branch recomputes C2 integrity, including lexical
validation and hashes. It also requests fresh stream verification, serialization
and deserialization; both serialization functions reverify the complete stream.
The registered-projection branch and admitted transaction reuse have different
routes, so these counts are not universal runtime multiplicities.

These checks bind actual source, invocation, owner, schema, identities, graph,
serialization and envelopes. This patch removes none of them and introduces no
report cache, saved parent, precomputed pass or alternative constructor.

## Demonstrated avoidable work and correction

The lexical leaf's `:no-token-contains-top-level-form?` fact compares every token
with every complete collection-root spelling until a match is found. Short
tokens therefore scan the entire root set even when every root is longer.

For all-String roots, compute the shortest UTF-16 length once per leaf call.
A native keyword-keyed token record whose String spelling is shorter cannot
contain any root. Only this impossible-match case skips the root search. It is
an exact String containment property, not an input quota or an admission limit.
Empty roots, malformed values, opaque records/keys and non-String spellings retain
the original search. Only the native `clojure.string$includes_QMARK_` function
class qualifies. Replaced or thread-bound `clojure.string/includes?` also
retains its original callback route. Changed roots recompute the length; no
source, context or result is reused across invocations.

All other lexical facts, raw slices, Unicode handling, graph checks, parent
membership, form traversal, provenance and complete report fields are unchanged.
Whole-form token substitutions still make the containment fact false, and a
substituted shorter root cannot hide behind the preceding call's length.

## Compiler-free evidence

The focused command selects the existing lexical and parent-membership controls
plus `gravity.reader.token-root-scan-test`. It passes 14 tests / 2,254 assertions.
The language-boundary command passes 6 tests / 11 assertions. Bootstrap is absent
from the focused process. The new controls compare full reports with the original
search retained, cover empty and Unicode strings, malformed values, opaque lookup
counts, replaced callbacks, changed raw text, whole-form substitutions, missing
children and wrong parents. An injected 128-root / 256-short-token case reduces
32,768 root predicate visits to zero without changing its containment result.

A separate driver loads the prepatch Clojure leaf under a reference namespace
and compares complete reports on models derived from all C11/C13 source forms:

| Source model | Roots | Nodes | Edges | Scalar tokens | Before root visits | After root visits |
| --- | --- | --- | --- | --- | --- | --- |
| C11 | 532 | 64233 | 63701 | 43864 | 23335648 | 11704 |
| C13 | 192 | 18245 | 18053 | 12398 | 2380416 | 384 |

All complete reports match, including 20 independent malformed/substitution
fixture pairs. The source is read as inert Clojure data with reader evaluation
disabled. Model scalar spellings and collection-root spellings use `pr-str`;
these are not actual Gravity Reader tokens, source spans or authenticated C5
products. Counts are algorithmic controls, not constructor timings or benchmark
authority. Source bytes are conserved before and after the model control.

Baseline leaf SHA-256:
`020aa6e13770259551abd1f8714609c5089c7bae973da11295dd33d575b21062`.
Candidate leaf SHA-256:
`b0d77b6c6970ef11470ce51b51fa0be6c8622a206c85d44c36292a594488c1db`.
Driver SHA-256:
`f0f0c3fd0cea45ec4e19017415bc27bc702e7307e4058482d733b25a0e4ee352`.
C11 source SHA-256:
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`.
C13 source SHA-256:
`61fed7c8f7f565f396f3893a07a333b003bb1df08a34569af6bfe7b2fd4db483`.

## Acceptance boundary

This demonstrates and corrects one remaining repeated lexical scan. It does not
establish the cause or resolution of the whole historical timeout. No actual
Gravity compiler, producer, runtime, broker or process probe is launched, and
the unchanged 1,200-second attempt is not retried.

The [original PR374 checklist](https://github.com/burn2delete/uniform/blob/201ff4e49c688b4f7c20931c88b1287f62924192/docs/artifacts/phase-06/reports/c14-contextual-reader-boundary.md)
and selector `sh17-contextual-module-validates-original-and-rejects-substitution`
remain unchanged and open. Actual source/Core/MIR, complete C11/C13/C14 module
construction, scoped-valid `C14-UNSUPPORTED` / `:jvm-b5-target-lowering-contract`,
independent ledger/target/source-hash substitutions, natural terminal source and
context bookends and residual-diff review still require their own supported
admission and execution evidence. No full C5, Lower, backend, public admission,
self-hosting or release credit follows from these models.
