# Types current runtime: Core assembly composition

D1, D2, D3, D8, D9, L5, L17, C1, C7, C15, BOOT8 and TEST2 govern this
composition. Independently accepted runtime source is
8c9bce3dc016c31b929d97359f0d1ae144af5c16, tree
c28e1f6f1ab51e7696018fe4fb091c8bd0b9aaf0. Accepted dependency candidate is
4228b23b8909d32333855d828117d789b525dc82, tree
a128a6906911efc2793c2eef3082e55cc41a21e3. It contains actual main
0be1c7d4ff8f4a0bc1acba68c9799e6f461be2e7, including integrated PR695.
PR696 remains a prerequisite whose final checks and integration are pending.

The normal merge brings eleven paths: five Core assembly source/test paths,
three Core evidence reports, two types composition/failure reports and the
namespace map. Ten paths are byte-identical to the accepted dependency. The
sole adjacent Core/types row conflict is resolved as the dependency's complete
map with only its types row replaced by the accepted current-runtime row.

All five current-runtime source/test files and the original source evidence
report are byte-conserved from 8c9bce3d. The snapshot of those five files and
four Gravity inputs still verifies. The incoming assembly builder/guard changes
are real, but the source evidence's direct current Core test runner does not
enter that final artifact-assembly builder. Gravity C7/Core, compiler/emitter,
Reader and runtime source inputs are unchanged by this merge. This is no new
source-production run at the composed head.

Fresh bootstrap-free C7 tests pass 7 / 204, mocked production-order control
passes 1 / 11, and the retained-data verifier passes all seventeen complete and
serialized pairs, declared plan/Core/provenance hashes, C5 joins and full paired
typed/identity equality. The real source test remains its original 1 / 532
development observation. Source contextual lint on all five changed Clojure
paths passed with 3493 unchanged baseline warnings and zero blockers; its
receipts are retained separately. New composition lint and independent exact
review, prerequisite integration, final-head CI and clean current-main preflight
remain required.

The dependency's older Macro timeout is immutable failed history, with cause
and cleanup unknown; no rerun, waiver, CI edit or causal remedy is claimed.
The stage's eleven APIs retain admission:none. Actual primitive execution and
replay evidence do not establish full function-owner admission or complete
L5/C7 support. Frozen source authority, native ingress, effectful providers or
commands, migration, release, self-hosting and seed retirement remain outside
this slice. Existing coordinator-adapter and digest-resolution requirements
survive in the emitted primitive templates.
