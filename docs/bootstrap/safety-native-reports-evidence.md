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

## Unsupported boundaries

No new general SAFE classification, source/Core authentication, proof-provider
trust, certificate verification, profile legality, capability grants, unsafe
approval, runtime check execution, optimization/check erasure, MIR/backend
admission, performance, release, migration, self-hosting or seed retirement.
Existing report completeness and proof labels remain native data. In
particular, the original set comparison does not establish uniqueness of
duplicate outcome records, and vacuous predicates remain compatibility
behavior. This extraction must not strengthen those predicates or silently
claim canonical C10 conformance. Historical held routes, guards, source pins
and denied attempts remain unchanged.
