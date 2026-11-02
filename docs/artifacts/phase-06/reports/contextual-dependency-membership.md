# Contextual dependency lexical membership repair

## Scope and source basis

This source increment removes a quadratic sibling-membership scan from hosted
C2 lexical validation, consumed by fresh C3 verification on the contextual
C11/C13-to-C5 dependency route. C2 lexical graph requirements, C3 source identity
and C5 dependency provenance remain governed by C2, C3 and C5 in phase 06.

The base is `b2a78697330b9fe568ef99ee36e5d9a881472b37`, tree
`cf90b43e71ffd906f31cfbabfa59e5e0e0249993`. Historical PR374 remains at
`201ff4e49c688b4f7c20931c88b1287f62924192`, tree
`1e85874aaad9f5e15fb4e3f8fc3cb18881b682e0`. Its
[finite checklist](https://github.com/burn2delete/uniform/blob/201ff4e49c688b4f7c20931c88b1287f62924192/docs/artifacts/phase-06/reports/c14-contextual-reader-boundary.md)
and original selector
`sh17-contextual-module-validates-original-and-rejects-substitution` are
unchanged. This increment neither imports its inherited backend delta nor
creates a Lower task ledger.

The retained run 69917 reached SH05 fresh C3/Reader verification and exited 124
at 1,200 seconds before complete C11 C5 construction. That observation identifies
a verification family, not a measured time share for this leaf. Earlier Syntax
graph-membership experiments are separate historical evidence.

## Repair and retained checks

For each non-root form, the old lexical check scans all children of its declared
parent, even after computing complete global child frequencies. A parent with
width W therefore requires W squared sibling predicate visits in this check.
The new per-call index stores child membership for native keyword-keyed parent
records with native vectors of keyword child IDs. It uses the same last-wins
parent records as the existing form index. Opaque records, collections and IDs
retain the original scan. No authentication or verifier result is reused.

The global frequency must still be exactly one. Missing parents, wrong parents,
duplicate edges, duplicate form IDs, cycles, unreachable forms, source ordering,
span containment, UTF-8 slices and bidirectional links retain their independent
checks and report fields. All source forms and child edges are still traversed.

## Compiler-free controls

Commands:

```bash
clojure -J-Xmx256m -M:dev-test --namespace gravity.c2-lexical-validation-test --namespace gravity.reader.parent-membership-test
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The focused controls pass 10 tests / 1,620 assertions; the language boundary
passes 6 tests / 11 assertions. The focused process does not load Bootstrap.
The controls cover all 512 directed edge combinations on three nodes with three
declared-parent tuples, real invalid mutations, duplicate-parent last-wins
behavior, fresh changed inputs, opaque fallback and wide-parent scan counts.

A separate compiler-free differential control loads the original Clojure leaf
under a reference namespace and compares complete reports. Baseline leaf SHA-256:
`e40e5be3d4369a74f37c32c43cba0318e570893c9dcc7a97d14b5f5e239b1537`.
Candidate leaf SHA-256:
`020aa6e13770259551abd1f8714609c5089c7bae973da11295dd33d575b21062`.
Driver SHA-256:
`d5bef7ad875136e7bebfb8dedfe9fe3b784f030dc064ca192230c7268e2a134f`.

At width 512, complete reports match: the baseline makes 262,144 sibling
predicate visits and the candidate makes zero. This is an algorithmic control,
not a wall-clock benchmark or a claim that the whole constructor completes.

Complete C11/C13 source is read as inert data by Clojure with reader evaluation
disabled, then projected into synthetic graph records for the same full-report
comparison. These are source graph models, not actual Reader products or C5
artifacts. Every model node and edge is retained:

| Source | Top-level forms | Model nodes | Model edges | Complete reports |
| --- | --- | --- | --- | --- |
| C11 | 532 | 64233 | 63701 | Equal |
| C13 | 192 | 18245 | 18053 | Equal |

Both source texts are conserved before and after the control. C11 SHA-256 is
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`;
C13 is `61fed7c8f7f565f396f3893a07a333b003bb1df08a34569af6bfe7b2fd4db483`.
Current C13 differs from PR374's historical source receipt; no earlier execution
is reattributed to this composition. Reader, Syntax, C11 and C13 source bytes
are unchanged relative to this increment's base.

## Remaining acceptance

This repairs demonstrated repeated work in the dependency route. It does not
establish the cause or resolution of the entire historical timeout. No actual
compiler, source producer, Gravity runtime, broker or process probe is run.
Fresh real source/Core/MIR evidence, actual C11/C13/C14 construction, the
`C14-UNSUPPORTED` / `:jvm-b5-target-lowering-contract` scoped-valid result,
independent invalidation-ledger, authentic-Core target and changed-source-hash
negative controls, natural terminal identity bookends and independent residual
PR374 diff review remain open. Any later construction requires separate supported
admission. Full C5, Lower, backend admission, release and self-hosting remain
unclaimed.
