# MIR: native node, fact, operand and runtime-check id records

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. Its reviewed source parent is
65950b341f955b6d43786fafd8b6ed4d185658c4, tree
c708c556e8e6af244d4651783f6f42a61174c0a4. That source tree was integrated
through PR821's squash 10aa681a502e65e5138bbe81ea971fa98420726c;
parent review, checks and CI do not transfer acceptance to this increment.

Seven existing native bodies move from Bootstrap into gravity.mir:
node-opcode, node-facts, node-operands, runtime-check-operation-id,
runtime-check-token-id, runtime-check-source and runtime-check-fact-id.
Node-operands receives the original live token-id Var explicitly. Other bodies
are pure native projections. Wrappers preserve signatures, arguments, late Var
interposition and original return/error behavior. The source-binding, trusted
carrier, Unicode, metadata and require/diagnostic prefix remains unchanged.
The stage still has no imports or Bootstrap dependency.

Inputs are supplied native Core/node/fact/source/check data and the token-id
operation. Outputs are native opcode classifications, ordered fact/proof
references, operands, generated-source data and string ids. These ids identify
MIR operations, facts and SSA control values; they are not secure execution
tokens, grants or certificates. The stage owns these projections and the
unsupported-operation sentinel. Existing callers retain operation legality,
unknown-kind failure handling, input authentication, diagnostics and admission.
The helpers do not mint proof or capability authority.

The original paired normalized Gravity fixtures and current Gravity C11
builder execute through the ordinary fixed seed and emit real MIR. Native
operation validation accepts both. Each produces five node records. Altering
an emitted opcode to :llvm/add rejects C11-TARGET-LEAK, missing
checked-core-operation-parity. Complete source/compiler texts and hashes,
checked input, MIR, node records, operation products, changed MIR and full
negative diagnostics are captured before/after at the same paths.

Tests compare projected records with real emitted operations and preserve
duplicate/ordered proof references, fallback values, borrowed spans and source
ids, operand identity/order, id suffixes, generated-origin records, late token
Vars and exact owner exceptions. Separate runtime-check-shaped unit data tests
record projection only; the positive compiled fixtures have no runtime-check
nodes. No runtime-check program, authenticated proof or carrier is claimed.
Whole two-file conservation verifies all seven bodies apart from names and the
explicit token-id port, plus the unchanged source/validation prefix.

Native proof/authenticated/derived/runtime-checked/pending labels remain
unverified data. Full MIR, general CFGs/loops/matches/exceptions, runtime-check
legality, input/callback/proof authentication, source admission, diagnostic
identity/redaction/publication, backend/native execution, performance,
migration, release, self-hosting and seed retirement remain unsupported.
No restricted native/private factory, frozen Reader, package/archive/release
builder or Attempt30 route is executed.

Full data/raw equality, source hashes, focused and boundary tests, contextual
lint, contract validators, independent review, maintained draft PR, own
applicable CI/artifacts, current-main composition and immediate exact clean
integration preflight remain required. Final identities and receipts are
maintained in the PR.
