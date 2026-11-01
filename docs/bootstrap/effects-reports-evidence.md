# Native seed effect report verification

## Contract boundary

D1/D2/D3/D8/D9, L6/L9/L12/L15, C1/C7/C8/C15, BOOT8 and TEST2 govern
this extraction. Move the existing native C8 verifier-report, capability-proof
projection and report validation bodies from c8_effect_checker/verification.clj
into gravity.effects. Preserve every predicate, report field, lazy observation,
callback invocation, failure order, diagnostic id, argument list and return.
Existing compatibility entry points and the interposable C8 facade remain.

Proposed APIs are native-verifier-report, native-capability-proof and
validate-native-report!. Inputs are existing trusted effect catalogs, module,
effect graph, native legality/capability/build/replay/ordering/residual/
diagnostic records, or explicit projection/failure callbacks. Outputs are the
original native verifier report, legacy boolean projection, complete marker or
unchanged C8 refusal. These report labels do not authenticate source, effects,
grants, providers, package policy or actual performed work.

Runtime effect semantics remain Gravity-owned. This stage owns only the moved
native report predicates, projection and ordered missing-field C8 refusal
selection; existing diagnostic operations retain source-span construction and
publication. Dependency direction is clojure.set to gravity.effects to native
C8 compatibility consumers. No stage import of Bootstrap or the C8 facade is
introduced. Effects and capabilities remain distinct.

## Evidence obligations

Emit actual effect graphs and reports from retained paired source/Core/type
products, with full provenance and imported-file hashes. Reject actual changed
effect data and malformed native reports. Preserve complete before/after data
and original serialization; recompute identities at their declared boundaries.
These retained-input products are not a fresh authenticated C7/C8 source
pipeline. Use Bootstrap-free positive/negative host tests for every native
predicate, first-failure ordering, callback/exception identity, compatibility
interposition, source forwarding and explicit namespace contracts. Prove exact
body/wrapper conservation and unchanged facade/catalog/policy/evidence/assembly.
Run focused and affected composition, language/ownership, lint and contract
checks, independent exact acceptance, own CI/artifacts and exact clean current
main preflight before integration.

Initial source is PR763 candidate2e73c5a69ad5dab45ef5a215135aa2b11919492d,
treeedc1b864f4af03a4036f1e3b421667c0648053ea. Its Runtime source and PR761
are inherited pending dependencies, not new work or transferable acceptance.
This early scope draft confers no implementation or integration credit.

## Unsupported boundaries

No canonical C8/L6/L15 conformance, full legality intersection, latent/call
inference, handlers/continuations, source/provider/grant authentication,
untrusted report admission, performed build effects, safety authority, native
backend execution, performance, migration, release, self-hosting or seed
retirement. Legacy vacuous/truthy projection and diagnostic limitations remain
explicit native compatibility behavior. Historical restricted routes, pins,
guards and denied attempts remain unchanged and are not retried.
