# MIR: native module/function envelopes and schema sets

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. Its source parent is reviewed pending candidate
901de4acbe04a9ad32276cd56a82aa4d92546346, tree
d95ac45fe2f52ebf11cdb0a0ecb5f0dd909b8108, in draft PR821. Parent
review, checks and CI do not transfer acceptance to this increment.

The native validate-module-envelope! body moves from Bootstrap into gravity.mir,
along with its two module/function key-schema sets. Four live owner Vars
provide parameter lookup, require, type-sensitive structure comparison and pass
contract records. The two schema data values are read at each wrapper call;
legacy schema Vars remain snapshot aliases of the stage defaults. Redefining a
legacy schema Var still interposes at the wrapper. The stage has no imports or
Bootstrap dependency. Other schemas and use-record wrappers remain unchanged.

Inputs are supplied native checked-Core/MIR records, schema sets and owner
operations. Outputs are the original function, root node, return node and
return id, or owner failure. The stage owns relative module/function shape,
Core identity/provenance, construction, pending pass record and native scope
parity. Require and structure operations construct and throw the existing
C11-MODULE, C11-ORIGIN and C11-VERIFY diagnostics. Other callers retain fact,
runtime-check, source authentication and admission decisions.

These are the seed's native schemas and relative rules, not canonical C11
schemas or authentication. Fields and diagnostic names containing
authenticated, gravity-owned, pending or constructed are supplied native data.
Matching them confers no certificate or admission. Existing single-function,
single-conditional scope records and their fields remain unchanged; this
extraction adds no input quota, work accounting or new supported family.

The real paired normalized Gravity fixtures and current Gravity C11 builder
execute through the ordinary fixed seed and emit MIR. Native envelope parity
accepts each extension and returns sh12:branch-join:if as the return id.
Changing the emitted source-Core link rejects C11-MODULE, missing
authenticated-c11-module-envelope. Complete native input, MIR, schemas,
envelope products, changed MIR, full diagnostics and source/compiler texts
with hashes are captured before/after at the same paths.

Tests also reject module/schema/target flags, function name, provenance,
construction, pending pass record and backend/release-credit substitutions.
They retain borrowed record identity, schema alias snapshots, late owner Vars,
per-call legacy schema lookup, exact owner exceptions and callback order.
Whole three-file conservation checks the body apart from names and explicit
ports, both schema definitions and the remaining legacy prefixes.

General CFGs, loops, matches, exceptions, full MIR verification, runtime-check
legality, proof or input/callback/schema authentication, source admission,
diagnostic identity/redaction/publication, backend/native execution,
performance, migration, release, self-hosting and seed retirement remain
unsupported. No restricted native/private factory, frozen Reader,
package/archive/release builder or Attempt30 route is executed.

Complete data/raw equality, source hashes, focused/boundary tests, contextual
lint, contract validators, independent review, maintained draft PR and own
applicable CI/artifacts remain integration gates. Parent integration,
current-main composition and immediate exact clean integration preflight are
also required. Final identities and receipts are maintained in the PR.
