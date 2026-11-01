# Types products: integrated Core assembly composition

D1, D2, D3, D8, D9, L5, L17, C1, C7, C15, BOOT8 and TEST2 govern this
composition. Retained type candidate is
9cd520974f5271db18e512278d5a328da32f247e, tree
9ed75816b12902a50014d0abe3f1b02aff2d8c44. Actual main is
0be1c7d4ff8f4a0bc1acba68c9799e6f461be2e7, tree
68f542b172205a3bd5b40ecbe4bb15f83944477a, the verified normal merge of PR695.

Main contributes nine paths: the checked-core stage, compatibility component,
two construction/receipt guards, its existing tests, three evidence reports and
the namespace map. These are real implementation changes: existing assembly
maps move into the explicit unproved Core data API. The only merge conflict is
the adjacent Core/types map rows. Resolution retains main's complete map with
only its types row replaced by the accepted type-products row. All other
incoming paths are byte-exact main.

Types bodies, inference wrappers, existing C7 tests, ownership EDN/test and
strict JSON registration remain byte-conserved from 9cd520974. Gravity C7,
compiler, emitter, Reader and runtime sources are unchanged by this incoming
delta. The native type API still receives its explicit recorded Core node view;
the Core adapter's relocated assembly maps add no type premise or authority.

The separate Macro NONPASS in run 41203046906 remains terminal failure with
cause and descendant cleanup unknown, as recorded in
types-products-macro-ci-nonpass.md. Every other enabled job passed at that head;
readiness/prerelease were skipped. This incoming composition was already
required by the dependency order. It is not asserted to remedy the timeout,
and the failed head is not rerun or waived. The prior source and local tests
alone confer no integration eligibility.

Focused checks pass the existing bootstrap-free C7 catalog test, 7 / 176, and
complete recorded native-data comparison, twelve complete and twelve serialized
pairs. Their scope is the same 43 facts/constraints
per spelling and C7-VERIFY pending-constraint rejection, preserving complete
records, serialized data, declared Core/provenance hashes, original C5 joins and
test product fingerprint. They do not claim fresh Reader/C5/Core production.
Independent exact-composition review, changed-file lint, applicable final-head
CI and clean current-main integration preflight remain required.

All seven public APIs retain admission:none. The frozen Reader C2-HASH source
refusal, historical denied actions and migration pins are unchanged. Canonical
L5/C7 typing, complete typed Core, general constraints, numeric widths, later
legality, effectful Reader/provider execution, native ingress, performance,
migration, release, self-hosting and seed retirement remain outside this slice.
