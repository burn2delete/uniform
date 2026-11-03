# MIR: native diagnostic context construction

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
extraction. Its source parent is a6064414045a0e0c8ff8a0a4617dc2beafbc3c37,
tree 3b3cc9034bdaf39a2dab990435b52bf401776814. Parent acceptance and
validation do not transfer to this increment.

The existing diagnostic-context body moves from Bootstrap into gravity.mir.
Its inputs are native checked-core, context and artifact data plus one explicit
scalar-redaction operation. It produces the existing native context map, with
unchanged field precedence, non-map normalization, fallback labels and scalar
operation evaluation order. The wrapper supplies the original live scalar
Var. The stage imports nothing and is consumed by Bootstrap.

This API owns C11 native context projection. The caller retains scalar
redaction, catalog assembly, record and diagnostic identity construction,
class/callback admission, containment and publication. Legacy native fallback
labels are compatibility data, not canonical C15 context availability or
profile/target validation. The extraction does not make them authoritative.

The ordinary fixed seed executes current Gravity C11 and emits MIR for both
normalized Gravity fixtures. Native operation checks accept each product;
changing a real emitted opcode to :llvm/add rejects C11-TARGET-LEAK with
checked-core-operation-parity. Complete before/after captures retain full
fixture/C11/emitter texts and hashes, checked input, MIR, changed MIR, native
contexts, full diagnostics, message, rethrow and display-only changes.

Focused tests cover emitted source/artifact provenance, requested-target
precedence, invalid-but-present target data, non-map defaults, actual scalar
redaction, live Var identity, owner exception identity and callback order.
The initial callback-order expectation was wrong; evaluating the unmodified
parent body confirmed the existing order. Only that test expectation changed.
The initial boundary command named a namespace not owned by its runner and
was refused; the established language/ownership selection remains required.
Whole-two-file conservation permits only the function name and explicit port,
leaving surrounding policy unchanged. Final checks, exact product equality,
source identities and independent review are maintained in the draft PR.

Canonical C15 conformance, unavailable-metadata policy, public projection,
diagnostic publication, full MIR, runtime-check legality, source admission,
certificates, backend/native execution, performance, migration, release,
self-hosting and seed retirement remain unsupported. No restricted factory,
frozen Reader, package/archive/release builder, OWN controller or Attempt30
route executes. Successful applicable CI, independent acceptance, current-main
composition and immediate exact clean integration preflight remain required.
