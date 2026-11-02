# MIR: native fact-table reconstruction

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. The clean original control is main
8ceaec2ee113c9263194fe9c0921b8c0132ae435, tree
b792e45ab3b37ca25baae8717d495231f41fe741, in the Analysis checkout.

Six existing native C11 helpers move into gravity.mir: runtime-check-fact-record,
add-runtime-check-facts, reindex-fact-table, profile-target-table,
capability-proof-table and safety-proof-table. Explicit owner id and fact-record
operations plus native checked-Core, node, fact, profile and proof data produce
the original records and id-indexed tables. Existing Bootstrap wrappers keep
their signatures and pass late owner Vars. The stage still has no imports or
Bootstrap dependency. The remaining pass-contract, ordering and path helpers
in the original fragment are unchanged.

This stage owns native record/table reconstruction. Existing callers retain
fact equality checks, C11-TYPE/EFFECT/SAFETY/MODULE/VERIFY refusal decisions,
source enrichment, authentication and admission. Labels such as derived,
runtime-checked, proof, authority-id and granted remain borrowed native data;
constructing a record does not prove its safety, grant authority or authenticate
an input or certificate. Runtime-check legality, general MIR verification,
source admission and canonical C11 completeness remain unsupported.

Both existing branch/join fixture spellings and the current Gravity C11
builder/verifier compile through the ordinary fixed seed. Their normalized
fixtures emit real MIR and pass the Gravity verifier; emptying the emitted
type table produces the actual C11-TYPE refusal. The initial capture looked
for :id, while this Gravity diagnostic uses :diagnostic-id. The corrected
harness oracle changes no source, input or diagnostic. Native table outputs
and all six runtime-check fact kinds are separately captured, using explicitly
unverified data for check/proof/authority references. Historical fixture labels
are not source/Core authentication or migration evidence.

Tests cover exact ids, source/profile/proof object retention, sorted tables,
fallback ids, runtime-check classification and non-DCE data, callback order,
exact owner exceptions, no-check table identity and wrapper Var interposition.
The first extraction used an undeclared short namespace alias in the Bootstrap
wrappers; compilation rejected it. The wrappers now use the existing fully
qualified gravity.mir namespace, without adding an import or changing a body.
The original derived profile-target runtime-check record omits source-origin-id;
a new test initially assumed otherwise and was corrected to preserve that
existing boundary. Other runtime-check fact kinds retain the supplied origin.
Complete before/after data and raw serialization, source hashes, whole-body
conservation, focused/boundary checks, lint, contract validators, independent
acceptance, own draft PR CI/artifacts and immediate exact clean current-main
integration preflight remain integration gates. Final receipts are maintained
in the draft PR. No complete MIR, backend execution, performance, migration,
release, self-hosting or seed retirement is claimed. No restricted native or
private factory, frozen Reader, package/archive/release builder or Attempt30
route is executed.
