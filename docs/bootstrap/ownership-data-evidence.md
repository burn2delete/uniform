# Ownership: persistent seed data and predicates

## Boundary and contracts

D1, D2, D3, D8, D9, L10, SAFE3, SAFE4, SAFE5, C1, C6-C11, C15,
BOOT8 and TEST2 govern this increment. The base is main
3a4d5ea11c946bd374dfdfa09feaa1d78aa6c582. Nine existing native seed algorithms
move from components 00684, 00685 and 00694 into gravity.ownership:
persistent-ownership, forwarded-value-node-ids, propagate-managed-ownership,
shared-owner-domain-id, canonical-owner-id, persistent-aliasing-valid?,
persistent-ownership-expected-keys, persistent-ownership-schema-valid? and
expected-managed-node-ids.

The public Vars declare exact argument lists, artifact inputs/outputs,
diagnostic responsibility, dependency direction and admission:none. Only
clojure.set is imported. Explicit operations retain the existing digest,
forwarding, owner reconstruction and schema dependencies. Bootstrap wrappers
pass Vars and retain the original public arities and one-to-two argument schema
dispatch. The common-key constant remains with its existing seed owner.

These algorithms construct persistent immutable records, propagate managed
provider fields in existing node order, reconstruct owner-domain identity and
check alias/schema data. They introduce no lifetime rule, provider grant or
authority. Existing callers retain C9-MUT-ALIAS, C9-TRANSFER and C6-VERIFY
diagnostics, including source enrichment. No diagnostic construction moves.
Stored ownership facts retain their historical prospective-subject fallback;
they do not become authority. Extra fields still override construction defaults;
construction is not admission. The schema and alias predicates remain separate.

## Actual development evidence

The before producer runs at clean main 3a4d5ea1 in the original analysis
checkout. The after producer runs with working extraction over that same base,
before final registration/tests/documentation. These are development source
observations, not committed-head production or authority bookends.

Both actual SH-10 accepted and rejected fixture spellings compile. The current
Gravity C9 engine executes their normalized requests: many immutable borrows
are accepted and use after move is rejected with C9-USE-AFTER-MOVE. No genuine
source/Core/C8 admission or native execution is attempted. The Gravity engine
continues to own those transition semantics; the extracted native predicates
do not acquire them through this test.

A separately identified retained SH-12 Gravity fixture record supplies five
node/source records for the native data exercise. An explicit view adds the
existing persistent-ownership construction and operation-specific fields.
The view is data, not accepted Core or an authenticated ownership artifact.
Its inherited sh12:branch-join:checked-core identity and proof/status labels
are fixture data. The node set contains no function node, so this view's owner
lookups remain not-applicable; dedicated data tests cover reconstruction,
digest preimage and fallback precedence. No provenance is manufactured.

Thirty-six complete and thirty-six captured text pairs match, including full
compiled fixture plans, source text/hash records, accepted and rejected requests
and results, retained input records, native views, normalized ownership records,
forwarding/owner data and alias/schema refusals. All recorded source text hashes
and declared fixture plan IDs recompute through the existing protocol. Mutating
the view's sharing policy fails the alias predicate; adding a forged ownership
key fails the schema predicate. Original source paths and origin/span records
remain intact. These native-data refusals are boolean predicates; the existing
caller continues to own diagnostic selection and throwing.

The conservation check compares all nine moved bodies with main 3a4, including
the original schema second arity, after explicit operation substitution. Two
unused local bindings are renamed with an underscore while preserving their
lookup/evaluation order. Anonymous reader-generated symbol numbers are
normalized for structural comparison only. Every unmoved form in the three
components is conserved. Runtime identity encoding is unchanged.

## Tests and retained nonpasses

gravity.ownership.pipeline-test covers actual paired normalized Gravity
ownership execution, declared plan identity, persistent construction, direct
and transitive managed forwarding, order dependence, schema/alias tampering,
owner-domain digest/fallback precedence, extra-field precedence, late Var
interposition, one-to-two arity dispatch and host lookup order. Explicit native
test data and mocks confer no authority. Exact EDN/JSON registration associates
gravity.ownership/sh-ownership/SH-10 with its actual Bootstrap consumer and
standalone test. Existing primary component counts and leaf catalog remain
unchanged. Ownership tests reject forged/missing registration records.

Temporary tooling nonpasses remain retained. The first producer assertion read
the diagnostic id field instead of the existing rule field; the actual Gravity
negative result was rejected. An extraction helper's String.indexOf call failed
before project writes; an inadvertently continued observation still used the
unmodified source and is labeled unmodified-after, not credited as extraction
evidence. A missing require caused compiler startup failure before any changed
Gravity program ran. A registration script delimiter error had written only
contract formatting and the EDN association; the remaining forms were parsed
before execution and completed without replaying the already applied mutation.
The first conservation comparison failed on reader-generated anonymous symbol
numbers, and the first interposition test accidentally replaced the function
whose digest callback it intended to observe. Corrected checks preserve the
same implementation bodies. Logs and Clojure reports remain in /tmp; no denied
semantic/native action, source-authority pin, CI job or external download is
retried or waived.

Focused checks, contextual lint, docs/roadmap/governance, independent exact-head
review, successful candidate CI and a clean current-main integration preflight
are required before integration. This slice does not waive the failed effects
or diagnostics candidates or the separate unavailable Source-Core readiness.

## Unsupported boundaries

This is partial temporary Clojure organization. Canonical C9 check/verify,
general ownership analysis, mutable borrowing/lifetimes, regions, arena
generations, linear resources, task/FFI transfer, runtime borrow checks,
provider grants, public input/callback authentication, full safety or MIR
admission, native/backend execution, performance certification, migration,
release, self-hosting and seed retirement are not supplied by this namespace.
Gravity remains the semantic owner. Existing compiler legality remains a
compile-time requirement, independent of runtime/provider behavior.
