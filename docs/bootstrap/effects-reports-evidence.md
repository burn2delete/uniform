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

## Implemented boundary and actual products

All three original bodies (73 lines) now live in the Effects namespace. Only
their public names change; compatibility wrappers keep the old arities and
argument order. Exact text checks conserve the moved bodies, all three prior
graph-builder bodies, wrapper reconstruction and the declared stage file.
All other Bootstrap/Gravity source, facade, catalog, policy, diagnostic,
evidence and artifact assembly code remain unchanged.

Clean baseline5da462a1eb1c2e4a63cabf5f8ad48ca563f076fb, tree
d3a12f20eaec1cf1860d790d71f3a2aeb2082b70, executes before source changes.
After controls execute the working extraction at that HEAD, in this same
physical Analysis checkout. Both processes terminate before committing source.
They retain the historical producer inputs under
/tmp/uniform-types-products-native-evidence. Their old source/toolchain keys
remain historical data; current compiler identity, C7 acceptance or C8 source
admission is not inferred or renewed.

Both spellings emit43-node effect graphs and one function record from those
retained type products. Original Core id
sha256:462f37046c92a22b5f519155432752c7bbc068a015e9389f12ed09ada07777cf,
provenance binding and C5/request/Core joins recompute or compare exactly.
The original fixture at its recorded Runtime path still has SHA-256
f32e08927875808db45dde4866b30fd7e713d4f9e584d8743150942b94dbfc06.
All six imported-file hashes and every graph-node source record survive.

The unchanged pure graph reports failed and its validator raises C8-ORDER:
the native verifier requires a nonempty ordering-record sequence even when
all source effects are pure. Adding io/write to the first fact reports failed
and raises C8-UNDECLARED before the later checks. These original limitations
are preserved; no ordering evidence, allowance, grant, source field, returned
record or authority callback is invented to force acceptance. The capture's
positive label identifies the unmodified input case, not a passing report.
Separate trusted native units exercise genuine report acceptance under their
explicit supplied data, without claiming source or grant authentication.

All28 complete data and original serialized-text pairs pass, including source
inputs, type products, module views, imported hashes, fixture bytes, both
graphs, full reports, projections and complete refusals. No field or root
normalization is used. Focused Bootstrap-free tests pass12/262; affected native
C7/C8/C9 composition passes23/560. Tests cover every native report predicate,
empty-node/status behavior, ordered missing fields, original path forwarding,
legacy truthiness, callback/exception identity and wrapper interposition.

Scripts/logs/products use uniform-effects-reports-* under /tmp. Initial
capture/test reader errors occurred before product-source writes; their error
records remain. The initial capture incorrectly expected a passing pure report;
the retained error record and corrected oracle describe the observed C8-ORDER
gap. The self-hosting runner selector executed no tests because this namespace
belongs to the Bootstrap-free leaf invocation; its nonpass log remains.
A structural checker initially compared separately generated anonymous reader
symbols; its nonpass remains and the corrected checker compares exact original
function text. These corrections change no production predicate or guard.

Language/ownership, committed contextual lint, final contract checks,
independent exact review, actual-main reconciliation, own CI/artifact receipts
and immediate clean integration preflight remain final gates.
