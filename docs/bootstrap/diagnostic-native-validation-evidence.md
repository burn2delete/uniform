# Diagnostics: native record and stream validation

D1, D2, D3, D8, D9, C1, C15, BOOT8 and TEST2 govern this extraction.
Its source parent is 06af36c694dcdd38191991591799c87ec74d85ac, tree
17105fbe8c14d4a5c1b7f0dc0ef7ad0b228a922c, the pending catalog candidate
in PR837. That candidate's acceptance and CI do not transfer to this slice.

Three existing C15 validation bodies move into gravity.diagnostics:
native-context-valid?, native-primary-valid? and validate-native!. Inputs are
native diagnostic/artifact data, configuration and four explicit live owner
Vars: failure, stable identity, context and primary-location predicates.
Outputs remain the original predicate values, native complete label or owner
exception. Wrappers retain configuration, operation-map interposition and
failure construction. The stage requires only clojure.core/clojure.string;
it imports no facade, producer, Bootstrap, CLI or release namespace.

The stage owns the original ordered native context/location, field, identity,
origin, facts, remediation, ordering, catalog and supplied-label checks.
Callers retain source/configuration admission, diagnostic construction,
identity operations, redaction and publication. A public-safe flag or matched
golden row is supplied data: checking it performs no secret redaction or
golden comparison. Returning complete does not establish canonical C15
conformance or permit public disclosure. Gravity C15 retains canonical source
schema and substitution semantics.

Before/after controls compile the complete current Gravity C15 source with
108 functions and SHA-256
4dea49ae632a5d07399ec5db45112103f44c510534b097a1199c7720da86aacf.
Native empty-artifact validation rejects C15-SCHEMA. Its actual system facts
produce a private source record freshly verified from separate original fields
and a complete-request digest; malformed source facts reject C15-FACTS.
Captures retain full source text/hash/plan, catalog, metadata projections,
native refusal, original fields, unresolved record, digest request, candidate,
fresh verification and source refusal. Complete/raw pairs and recomputed
source/digest identities remain required.

Existing native C15 tests retain their phase, span, identity, order and rule
refusals. Added tests cover unavailable versus missing context, native primary
values, live wrapper Vars/configuration, exact exceptions, refusal before
identity/predicate work and the legacy label-only result. The first new test
incorrectly expected literal true for a string syntax ID; isolated evaluation
of the unchanged parent confirmed its truthy character-sequence return.
Only the test expectation changed. Whole-two-file conservation permits names
and explicit operation ports, preserving all surrounding code and rule order.
Final checks, source identities and independent review remain in the draft PR.

Lint also identified the validation module's pre-existing unused shared-catalog
import after its namespace form changed. That unused import is removed; the
C15 facade retains its explicit shared-catalog dependency and configuration.
No message lookup, catalog value or validation rule changes. The initial lint
nonpass is retained, and the source controls run again after this import delta.

Untrusted native ingress, canonical catalog/schema/identity conformance,
redaction/rendering, public projection/publication, source authentication,
migration, release, self-hosting and seed retirement remain unsupported.
No frozen Reader, restricted factory, package/archive/release builder or
Attempt30 route executes. Own applicable CI/artifacts, independent acceptance,
current-main composition and exact clean integration preflight remain required.
