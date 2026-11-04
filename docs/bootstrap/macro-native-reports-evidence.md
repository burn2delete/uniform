# Native Macro proof and ordered refusal extraction

## Governing contracts and ownership

D1/D2/D3/D8/D9, L4/L12/L16, C1/C4/C15 and TEST2 govern this increment.
The source parent is unmerged Syntax PR1184 at
f40b25494c45994f5f6cc4598a9458520096bbc9, tree
2e04f87e8ffb18aaa17afa312c8390e68c367c2d. Observed main is
3c97ad75341c5ef6506cf1bba050b9017301695b. No inherited acceptance or CI
establishes this source's integration eligibility.

Independent ownership review identifies two remaining native macro-stage rule
bodies: c4-macro-capability-proof in component 00028 and c4-macro-validate! in
00029. Move their nine predicates and ordered refusal mapping to gravity.macro.
The large earlier constructor in 00029 is shadowed by the active 00869 adapter;
it is not the extraction target. Existing Gravity macro code remains the
semantic expansion owner, while gravity.macro-expansion and c4-macro-evidence
remain explicit hosted compatibility/projection layers.

## API, artifacts and dependency direction

Add native-capability-proof [artifact diagnostic-ids-var] and validate-native!
[source-path artifact proof-var failure-var]. Keep the original Bootstrap names
and arities as explicit adapters. The native artifact and diagnostic catalog
produce the same nine report predicates and status field; validation computes
the proof once and returns :complete or invokes the existing C4 failure owner.
These are supplied native records, not authenticated compiler evidence.

Macro owns predicate evaluation and ordered rule selection. Bootstrap retains
source reading, metadata override admission, artifact construction, authentication,
cache/storage, dispatch, failure construction and publication. Dependencies flow
from supplied native artifacts/catalog through Macro report rules to existing
consumers. Macro does not discover or import Bootstrap or diagnostics owners.
Explicit Var ports preserve current owner resolution at the original use site.

Preserve C4-RETURN, C4-TRACE, C4-HYGIENE, C4-BUILD-EFFECT and
C4-GENERATED-UNSAFE order, actual paths/spans, owner exception and argument
identity, native truthiness and vacuous every? behavior. Incoming source, cache,
replay, identity and readiness fields are consumed unchanged. No replacement
identity, authorization guard, proof certificate or project quota is added.
C4-SIZE remains historical catalog data; comparison-ready is a readiness label.

## Acceptance and evidence

The accepted compiler-c4-macro-engine fixtures and rejected compiler-c4 corpus
exist in paired .gravity/.qst forms. This increment must emit real native report
and diagnostic records on actual paired paths and retain full before/after data,
source text/hash and original identity fields. Native unit inputs do not claim
that source compilation or authentication took place. Historical committed proof
records remain historical transport data and are not renewed by this extraction.

Tests must cover each predicate and ordered refusal, all-invalid first refusal,
truthy values, vacuous native fields, exact diagnostics, late catalog/proof/failure
owners, returning failures, exceptions, source provenance and facade interposition.
Compare complete native artifacts and diagnostics, not counts or hashes alone.
Keep unrelated Macro/source/assembly/admission/pin/manifests unchanged. Add the
focused tests to the existing Macro CI cohort and update only the API inventory
count needed by that public interface change.

Run focused tests, appropriate composition, boundary/ownership, contextual lint,
formatting and structural validators, independent semantic/adversarial review,
exact-source/base hosted CI and immediate clean integration preflight. Keep this
early draft PR current. The initial scope commit has no implementation credit.

## Explicit unsupported boundaries

This does not complete Macro migration or canonical C4. It does not authenticate
syntax/source, execute macro source, validate build grants or cache replay, verify
safety/proofs, admit compiler products, add release/self-hosting/seed-retirement
authority, or promote legacy report labels into certificates. Existing weak native
predicates, malformed-native host failures, source dispatch and unsupported forms
remain honest boundaries. Held compiler/authentication routes and retained dirty
patches are not exercised or adopted.
