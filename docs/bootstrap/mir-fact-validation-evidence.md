# MIR: native fact-table equality

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. Its reviewed pending source parent is
ff9c44a61791ff6ffcf0ce472361e3bcea01b74b, tree
a63f61920ce76d12cdc6e3697f36f14c3d808884, retained in source PR820
and composed into pending PR815. Parent review, checks and CI do not transfer
acceptance to this increment.

The first, table-equality portion of Bootstrap's validate-fact-tables! moves
into gravity.mir. Six explicit live owner Vars supply reconstruction of runtime
facts, id-indexed facts, profile/target facts, capability and safety proof data,
and the require operation. Native checked-Core and MIR records are the other
inputs. The unchanged relative rule returns nil after its eight comparisons
or propagates the owner failure. The stage has no imports or Bootstrap
dependency. The wrapper retains its signature and continues with the original
effect-graph rule. That rule and the remaining runtime-check/construction tail
are byte-conserved, including their legacy fields; they are not owned by this
new API.

The stage owns exact supplied type, effect, ownership, capability, safety,
profile/target, capability-proof and proof-certificate table equality. The owner
require operation constructs and throws C11-TYPE, C11-EFFECT, C11-VERIFY,
C11-SAFETY or C11-MODULE with the original missing-fact and source data.
Equality to borrowed records does not authenticate those records, their proof
labels or supplied callbacks. This API issues no certificates or admission.

The existing paired normalized Gravity fixture and current Gravity C11 builder
run through the ordinary fixed seed and emit real MIR. Native table equality
accepts each extension. Emptying an actual emitted type table rejects C11-TYPE,
missing id-indexed-c7-type-facts. Complete source and compiler texts/hashes,
native checked input, emitted MIR, result, changed MIR and full diagnostics are
captured before/after at the same paths. The result is nil; the shared capture
harness's operation-count display is not a count of emitted MIR instructions.

Regression tests substitute each of the eight tables and check original owner
diagnostics, live Var identity, wrapper arguments, exact exceptions and callback
order. Changing only the effect graph is accepted by this table-only API but
rejected by the original wrapper's separate graph rule; this explicitly tests
the preserved boundary.

Native proof/authenticated/derived/runtime-checked/pending labels remain supplied
data. Positive fixtures have no runtime-check nodes. General CFGs, loops,
matches, exceptions, complete MIR verification, runtime-check legality,
input/callback authentication, proof certificates, source admission,
diagnostic identity/redaction/publication, backend/native execution,
performance, migration, release, self-hosting and seed retirement remain
unsupported. No restricted native/private factory, frozen Reader,
package/archive/release builder or Attempt30 route is executed.

Complete data/raw equality, source hashes, exact two-file conservation, focused
and boundary tests, contextual lint, contract validators, independent review,
maintained draft PR and own applicable CI/artifacts remain required. Integration
also requires parent integration, current-main composition and immediate exact
clean preflight. Final identities and receipts are maintained in the PR.
