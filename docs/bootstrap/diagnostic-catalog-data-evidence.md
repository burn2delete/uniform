# Diagnostics: native catalog and metadata projection

D1, D2, D3, D8, D9, C1, C15, BOOT8 and TEST2 govern this extraction.
Its independent source base is f43e77992fe86b6dfb55df3516b27cfcdea7fa40,
tree a0e433f9f185b651f6f5ebc54340a085a5cd6721. It does not import the
pending MIR diagnostic candidates or use their acceptance or CI as evidence.

Two existing C15 records bodies move into gravity.diagnostics: source-overrides
and native-catalog. Inputs are native module metadata and the explicit ordered
diagnostic-id collection. Outputs are the original borrowed override value or
empty map, and the original native catalog. Wrappers retain configuration and
operation-map interposition. Rule order, duplicates, severity, message keys,
explain-page spelling, lifecycle and legacy identity-policy labels are unchanged.
The stage requires only clojure.core and clojure.string; it imports no producer,
facade, Bootstrap, CLI or release namespace.

The stage owns native C15 catalog and override projection. Existing owners keep
configuration validation, source parsing/admission, schema decisions, identity,
redaction, rendering and publication. Native complete/catalog labels describe
legacy data, not canonical registry conformance or proof authority. Override
projection does not validate or grant the requested override.

The complete current Gravity C15 source compiles with 108 functions and source
SHA-256 4dea49ae632a5d07399ec5db45112103f44c510534b097a1199c7720da86aacf.
The native validator rejects an actual empty artifact with C15-SCHEMA. Its real
system facts supply a new private source diagnostic, constructed and freshly
verified from separate original fields and a complete-request digest. Malformed
facts reject C15-FACTS. This is fixed-seed compiler-data execution, without
Reader/source admission or upstream authentication.

Before/after captures retain full source text/hash/plan, native catalog,
borrowed override data, native refusal, original fields, unresolved record,
digest request, candidate, fresh verification and source refusal. Complete and
original serialized pairs must match; source hash and full-request digest are
recomputed. Tests cover override precedence, empty/false behavior, borrowed
identity, unvalidated native data, rule order/duplicates, display fields,
late stage dispatch, operation interposition and exact exception identity.
Whole-two-file conservation permits names and the explicit catalog input only,
preserving all surrounding code. Final checks and exact identities remain in
the draft PR.

An ownership check caught a new direct import of the internal C15 records
namespace in a standalone test. Tests now use the existing public C15 facade
to exercise the same wrapper and interposition paths; the ownership rule and
dependency inventory are unchanged. The first temporary capture verifier had
an extra closing delimiter and failed before comparison; that script was
corrected without changing captured inputs, production behavior or assertions.

Canonical catalog/schema conformance, map encoding, source authentication,
public projection, redaction/rendering, publication, migration, release,
self-hosting and seed retirement remain unsupported. No frozen Reader,
restricted factory, package/archive/release builder or Attempt30 route executes.
Independent acceptance, own applicable CI/artifacts, current-main composition
and immediate exact clean integration preflight remain required before merge.
