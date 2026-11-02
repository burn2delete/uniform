# MIR: native effect placement and data-flow rules

D1, D2, D3, D8, D9, C1, C6-C11, C15, BOOT8 and TEST2 govern this
bounded extraction. Its clean source parent is reviewed pending candidate
5b6e607935fa9620c3d7210545d30b47c2a5498e, tree
edd4c7ff635779d2416af794aa0d3c9cb055a113, in the Analysis checkout.
That parent is included in draft PR815 and retains source PR817. Parent
acceptance, source checks and CI are not transferred to this increment.

Two existing native C11 bodies move from Bootstrap into gravity.mir:
validate-effect-edge-placement! and validate-data-flow!. The first receives
the owner require operation plus native source, MIR and operation products.
The second receives eleven explicit owner Vars in an operation map plus native
checked-Core, MIR and operation products. It reconstructs definitions, values,
uses and source maps, checks exact data/terminator edges and dominance, then
checks direct-effect boundaries. Outputs are the existing passed placement
label, count record or owner failure. Wrappers retain signatures and late Var
interposition. The remaining construction, containment, carrier and admission
code is unchanged. The stage still has no imports or Bootstrap dependency.
Local lint identified a redundant nested let in the initial explicit-operation
binding. The binding is folded into the original let without changing a rule,
callback order or input; the initial lint nonpass remains historical.

The stage owns these native C11-EFFECT, C11-DOMINANCE, C11-TYPE and C11-ORIGIN
decisions; the require operation constructs and throws the existing diagnostic.
Other C11 callers retain their fact-table, runtime-check, source authentication
and admission decisions. Passing these relative checks is not a MIR certificate
or a canonical type/effect/safety proof. Supplied operations and records are
not authenticated. General CFGs, loops, matches, exceptions, complete C11
verification and runtime-check legality remain unsupported.

The existing paired normalized Gravity fixture and current Gravity C11 builder
execute through the ordinary fixed seed and emit real MIR. Both native rule
calls accept the original data: four edges, four terminator uses, five
definitions and five values. Changing an actual edge rejects C11-DOMINANCE,
missing exact-checked-core-data-flow. Complete source texts/hashes, native
inputs, MIR, results and full negative diagnostics are captured before/after.
Native fixture identity and source/proof labels remain unverified data.

The original pure fixture has a vector effect graph with no effect-event edges.
Separate supplied map-shaped geometry
variants derive event edges from its real data-flow dependencies and test
in-block order, cross-block dominance and join pairing; reverse edges and
missing operations reject C11-EFFECT. These variants are not claimed to be
verified effect graphs or effectful source programs. Separate read-only probes
observed native fact-table acceptance but native runtime-check rejection of the
normalized fixture; no label or fact was changed to force that rejected path.
The first geometry test attempted assoc-in through the original vector and
failed before the rule call. Its corrected explicit map variant remains
unverified test data and does not alter the original acceptance capture.

Regression tests also reject changed terminator uses, definitions, values,
source maps and direct-effect carriers. They check owner callback order,
exception identity, captured late Vars and wrapper argument identity. Whole
two-file conservation checks both bodies apart from names and explicit ports,
including the unchanged construction/admission tail. Complete data/raw text
conservation, source hashes, focused/boundary tests, lint, contract validators,
independent acceptance, own draft PR CI/artifacts, parent integration and
immediate exact clean current-main preflight remain integration gates. Final
receipts are maintained in the PR. No full MIR, backend/native execution,
performance, migration, release, self-hosting or seed retirement is claimed.
No restricted native/private factory, frozen Reader, package/archive/release
builder or Attempt30 route is executed.
