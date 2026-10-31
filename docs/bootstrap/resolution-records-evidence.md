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
It is not passing evidence. The complete final regression selection, full
module-product comparison, independent acceptance and exact-head CI remain
pending.
