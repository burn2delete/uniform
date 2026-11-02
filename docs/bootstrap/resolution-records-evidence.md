# Resolution declaration and core records

This increment moves the ten SH-06 declaration and core-catalog normalization
algorithms in the existing special-symbols component into gravity.resolution.
D1, D2, D3, D9, L2, L3, C3, C4, C5, C15 and TEST2 govern the change. The
Gravity resolver continues to own resolution policy and binding identity.

The stage consumes expanded syntax, parsed module declarations, explicit
candidate records, source spans, the seed core catalog and a canonical digest
operation. It emits declaration records, core binding records, package records
and upstream source/envelope references. Record fields preserve namespace,
visibility, profile, target, effects, capabilities, safety and source origins.
The seed retains catalog configuration and compatibility entry points.

The public APIs added by this increment are semantic-span, source-revision-id,
envelope-id, package-record, definition-kind, definition-records, core-record,
core-records, request-overrides and candidate-for. Exact arguments and explicit
operations are recorded in the namespace contract. The stage does not require
gravity.bootstrap. Supplied operation maps remain trusted implementation
dependencies and confer no independent authority.

These normalizers do not introduce a new diagnostic policy. The existing
Gravity resolver owns C5 semantic rejection; authentication, construction and
verification gates retain their existing owners and order. Receipt reuse and
the construction drift seam must track every new stage dependency and catalog
root without invoking replaceable code during qualification.

Acceptance requires real current-development module products, actual rejection,
paired spelling provenance, deterministic identities and complete before/after
artifact equality. Focused checks cover declaration classification, visibility,
qualified names, catalog ordering, source/envelope fallbacks and interposition.
Independent acceptance, exact-head CI and clean integration preflight remain
required before merging.

Lexical traversal, dependency/import normalization, request assembly, artifact
assembly, verification and the legacy C5 engine remain outside this increment.
It grants no complete resolution, authoritative migration, self-hosting,
seed-retirement, release or performance claim.

The namespace cold-loads twenty public APIs without Bootstrap. The language
boundary passed 6 tests / 11 assertions; the three new normalization tests
passed 3 / 62. Direct stage lint reports zero errors or warnings. Pure
normalizers retain pure arities; only algorithms with seed dependencies take
the explicit operation map. The contract metadata records that distinction.

An initial local regression process ended with signal 143 and no test result.
It is not passing evidence. The final resolution regression selection passed
28 tests / 879 assertions, including actual Gravity emission and unresolved
reference rejection. Ownership and leaf-catalog checks passed 45 / 1036;
combined with the language boundary this is 51 / 1047. Repository lint passes
with zero blockers; the initially unused fixture bindings were corrected.
Structural documentation (240 documents / 19 phase indexes), roadmap and
workstream governance validation pass.

Before extraction, actual current-development values and consumer modules
compiled and verified in both .gravity and .qst. A fresh final replay asserts
the twenty-API surface and pure package-record arity before loading Bootstrap,
then compares the complete four resulting artifacts with their original
values. All four compare equal, preserving each spelling's provenance. The
library emits 267 bindings / 10 resolutions, with identity
sha256:7cee4a4156c5eb1ddd4ed19e94a6ee6938bd19bb594cdc495863eb648f8cf0a4.
The consumer imports the actual verified library product and emits 267 bindings
/ 6 resolutions, with identity
sha256:4b7cf3c7880ab84ad20efb41b158d70f43392107a8a107724473d7fdb935903d.
The replay retains the original canonical source and Reader paths while
selecting the new Clojure stage implementation through its explicit classpath.
This is current-development evidence, not frozen Reader admission.

Source implementation commit 296609b4 and the fixture-only lint successor
51be90ce compose unchanged into 803ce358, based on prerequisite 2bf1c336.
No ownership input, compiler authority pin or migration record changes in this
increment. Independent acceptance, exact-head CI and the final clean integration
preflight remain separate requirements.
