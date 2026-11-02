# Types products: Reader-main composition

D1, D2, D3, D8, D9, L5, L17, C1, C7, C15, BOOT8 and TEST2 govern this
composition. The independently accepted ownership correction is
c00b60b4d4377f0744922b0bcd5c28f6cebf6f01, tree
e75bd52c05298cf52649100921153c74b5410b48. Actual main is
482417c3b241544685045e9ec3578a1be9e0e39e, tree
ccce555bb8a68a0029ad47e8c7df3d2e72a31485.

Main supplies fifty paths from the declared-input Reader increment, including
compiler, emitter, runtime, Reader, build, CLI, ownership and lint tooling.
This is a real dependency composition. The normal merge has no conflicts.
The Types implementation, inference wrappers, existing C7 test and strict JSON
helper registration are byte-conserved from c00b60b4. The ownership EDN/test
auto-merge retains both the types helper and main's Gravity Reader module;
main's module inventory counts remain unchanged. All other incoming paths
are preserved as committed on main. Shared CI configuration is unchanged.

The existing bootstrap-free C7 leaf runner passes 7 tests / 176 assertions,
with its unchanged 53-entry catalog and catalog hash. Ownership now passes
21 / 821, including the helper's negative registration tests and the incoming
Reader module. Language boundary passes 6 / 11. Docs, roadmap and workstream
governance pass. Exact changed-file lint, independent composition acceptance,
final-head CI and clean current-main preflight remain integration requirements.

The complete recorded native-data comparison passes again: twelve complete
and twelve serialized-text pairs, 43 facts and constraints per spelling, the
same product fingerprint, declared Core/provenance hashes, original C5 joins
and C7-VERIFY rejection of a pending constraint. Those records retain their
original source-production paths and control context. This is a fresh replay
of data consumers, not fresh source production with the new Reader/compiler.
The incoming sources do not alter this pure API's explicit native-data inputs.

CI run 41200102977 remains failed history at head 7441bbbb. The exact missing
helper registration is corrected by c00b60b4; this does not rerun or waive that
run. The separately refused frozen Reader C2-HASH source route is not retried,
and no authoritative pin is renewed. Main's development source-binding change
is consumed as an existing dependency, without migration admission.

These seven APIs remain data-only with admission:none. Canonical L5/C7 typing,
complete typed Core, general constraints, numeric widths, profile, effects,
capabilities, ownership, safety, effectful Reader/provider execution, genuine
native ingress, migration, release, self-hosting and seed retirement remain
outside this slice. Namespace presence does not establish completion.
