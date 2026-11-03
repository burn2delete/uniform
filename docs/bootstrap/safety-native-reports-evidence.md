# Native Safety report ownership

## Contract boundary

D1/D2/D3/D8/D9, SAFE1/SAFE15, C1/C9/C10/C11/C15, BOOT8 and TEST2 govern
this extraction. Move the three existing C10 native report bodies from
c10_safety_analysis/verification.clj into gravity.safety. Preserve predicates,
legacy truthy/sequence values, evaluation order, callbacks, diagnostic ids,
source forwarding and return values. Keep the compatibility entry points and
interposable C10 facade.

The proposed public APIs are native-verifier-report, native-capability-proof
and validate-native-report!. Inputs are trusted native inventory, outcome,
runtime-check, obligation, certificate-reference, unsafe, taint/capability,
generated-origin, optimization and diagnostic data, or explicit projection and
failure operations. Outputs are the existing report, legacy projection,
complete marker or ordered C10 missing-field refusal. The stage owns these
native predicates and refusal selection. Existing callers own source spans,
diagnostic construction, transport and all input admission.

Dependency direction is core to gravity.safety to the native C10 compatibility
leaf and facade. The stage must not import Bootstrap or C10. Existing Gravity
classification and checked-core verification remain the semantic owners.

## Evidence obligations

The clean before source is 3e668d81205aa66f991f232e3ea7a2f7ae2f07e0,
tree f4a73f8a6fa5887044ffdaa7ada5684a3f925db1. It includes pending PR880,
which is inherited work and receives no duplicate acceptance or credit here.
Capture the original native artifact, projection, successful validation and
all eleven actual ordered missing-field refusals without Bootstrap loaded.
Compare complete serialized and parsed records and recompute hashes after
extraction; record clean exact source identity on both sides.

Retain paired Gravity/qst numeric-safety plans, real accepted outcomes and
C10-NUMERIC rejection in the existing Safety pipeline tests. These programs
exercise the existing normalized-request classifier, not genuine source/Core
admission. Add focused native predicate, ordering, truthiness, provenance,
callback return/exception and compatibility tests. Prove exact moved-body and
wrapper conservation; run affected C10 composition, language/ownership,
proportionate lint and contract checks. Independent exact-source review, own
CI/artifact verification and current-main preflight remain integration gates.

## Implemented extraction and local checks

The three original whole bodies now live in gravity.safety under the declared
names. Exact text checks reconstruct the original bodies and compatibility
wrappers; no native predicate is strengthened. The stage contract declares
all twelve APIs, trusted input/output data, the seven retained refusal ids,
diagnostic ownership and dependency direction. The C10 facade, policy,
catalogs, assembly and Gravity classifier remain unchanged.

Focused C10/native tests pass 7 tests/224 assertions. The retained paired
Gravity/qst Safety pipeline passes 5/147, including actual normalized-request
classification, deterministic plan identities and C10-NUMERIC rejection.
Language/ownership checks pass 48/1197 with no skipped namespace. Documents,
roadmap and governance validators pass. Fresh clean candidate capture,
committed lint, independent acceptance and CI remain pending.

The initial focused report test had one failed expectation: it expected the
original overall status to fail when inventory status is missing. The
corrected test preserves the observed report inconsistency and verifies that
the subsequent validator still refuses. The first extraction helper had an
extra parenthesis and failed compilation before source writes; its original
file and error record remain. A duplicate-target patch was rejected before
application. No production guard, pin, identity or report field was changed
to force a passing result. Helpers/logs use uniform-safety-native-reports-*
under /tmp. The PR attachment attempt failed at the existing 100-identity
limit and is not retried.

## Complete backend and Safety composition

The integration candidate normally composes reviewed Safety source
23a146e412412c25f5242b5b1fad874c8a478a3e with reviewed backend source
7b38fffbaab2d3da55f72bda816c04d5da6ce5da. The latter includes actual main
c28077686b5bd641c8cc0717bbd23641a701987a, tree
67eb8f7ab968c29fc3b58ce7a4ac980ac41e4d21, and its selected Syntax owner
authentication. All 25 backend paths match the reviewed backend source. All
six Safety paths match the reviewed Safety source before this composition
paragraph. All other tracked paths match actual main. The complete union is
31 paths, preserving both original draft histories without duplicate credit.

Fresh clean native captures, paired Safety/backend controls, exact complete
composition review, own CI/artifact checks and immediate current-main
preflight remain required. No provider source is adopted, and earlier CI
results do not confer integration eligibility on the combined source.

## Residual boundaries

No new general SAFE classification, source/Core authentication, proof-provider
trust, certificate verification, profile legality, capability grants, unsafe
approval, runtime check execution, optimization/check erasure, MIR/backend
admission, performance, release, migration, self-hosting or seed retirement.
Existing report completeness and proof labels remain native data. In
particular, the original set comparison does not establish uniqueness of
duplicate outcome records, and vacuous predicates remain compatibility
behavior. Overall report status also omits the inventory-status predicate;
the subsequent validator still rejects its false completeness field.
This extraction must not strengthen those predicates or silently
claim canonical C10 conformance. Historical held routes, guards, source pins
and denied attempts remain unchanged.
