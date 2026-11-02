# Checked-core test execution: current C7 and emitter composition

This composition carries the existing request preflight and test-only execution
boundary over actual main 5cf644298b95940b6499c52af1260dfdb609e7e0, tree
d44273d39d13a0b3a7f3b98c7a3ef9810a0e798e. Retained execution source is
97b071f4442027f36ab4923a35691f07d2ce0741, tree
4c8a236b5be2c228f050a9c22a3b82ddb4eb3eec. D1, D2, D3, D9, L1, L2,
C1-C7, C15, BOOT8 and TEST2 govern this dependency composition.

Main integrates PR669's C7 accounting removal, including actual Gravity emitter,
C7 engine and effect-descriptor changes, two typed compiler adapter updates,
affected tests/fixtures and STD3 documentation: eighteen changed paths. The
Core plan source-key includes the compiler and emitter source snapshots, so
the new emitter is a real compilation input. The normal merge has no conflict.
The retained Core stage/test, SH-07 components 885-909, Gravity Core source and
shared CI paths are byte-conserved. No own implementation correction, source
pin renewal, new accounting gate or CI/watchdog change is introduced.

The boundary remains 53 public APIs and four private helpers, all admission:none.
Bootstrap retains production owner admission/publication and scoped dynamic Var
lifetime. The executable test runner authenticates current C5 ownership and
exact request membership; the structural runner does not authenticate C5.
Gravity retains lowering semantics. Full type/effect/profile/ownership/safety
legality and release/migration authority remain outside this extraction.

Fresh runtime CI mode passes stage 31 tests / 2070 assertions,
current-binding/kernel 4 / 57 and projection kernels 3 / 286: 38 tests / 2413
assertions, zero failures/errors, with all three child exits zero. The language
boundary passes 6 / 11. Fresh current Reader/C5/Core execution completes for
source-occurrences.gravity and source-occurrences.qst. Each emits 43 nodes and
46 resolved digests, with passing template/resolved replay reports. Changing
the actual request projection binding rejects at preflight with C6-VERIFY /
request-projection-binding-mismatch and at the executable boundary with
authenticated-sh06-request-membership-mismatch. Producer and comparison
processes exit zero. The data-only comparison loader's require quoting and a
separate diagnostic probe's local scope were corrected without rerunning the
producer or changing emitted evidence; the failed probe logs remain history.

The actual Core toolchain source-key is unchanged across each execution and
names main's emitter hash
`sha256:df70318fc3846b5e927bed60217fb8a7725539ad47ff4c8a458b7fac2ac42ad7`,
which independently matches main 5cf64429's file. It differs from the previous
emitter hash
`sha256:2bde97f1c95363f737a84564bfad4541b4e931a1abc2c29f03dcc7fa2b35037e`.
Core source remains
`sha256:455f2d496e410ba8c5f4d17b2e4e558c6fb62306b614a6b9643107c8e1c82770`.
The source hash, plan semantic hash, 390-function count, development-unadmitted
status and self-hosted:false flag equal the retained Reader composition's
binding view. Complete canonical/provenance preimages and digest vectors equal
the retained products separately for each spelling; both declared hash
domains/purposes recompute the emitted identities. Candidate identity remains
`sha256:462f37046c92a22b5f519155432752c7bbc068a015e9389f12ed09ada07777cf`.
Actual fresh C5 owner identity remains
`sha256:81f394e461a95b8c6cea45d27d6953526e293804a18cc46971161bec933dc342`;
request, candidate lineage and both diagnostics join that actual parent.
All four complete negative diagnostic records equal the retained records,
including their owner facts. Raw carrier byte equality is not claimed.

Docs pass 240 / 19; roadmap structural and governance validators pass without
granting completion authority. Contextual lint, independent exact-composition
acceptance, final-head CI and clean current-main integration preflight remain
required.
Development execution uses HEAD 97b071f4 with main 5cf64429 staged; it does not
claim execution at a later committed HEAD or authoritative migration.

PR687's earlier run 41188984948 remains an immutable NONPASS at the original
candidate tree. Its child receipt records timeout-seconds 600, exit 124 and
status:timeout despite captured output reporting 54 tests / 2307 assertions
with zero failures/errors. Cause and descendant cleanup remain unknown. Receipt
SHA256 is e7a9e47732a0c96826f5c0b58a4a83f050bcde8127493ddccde070e7814d586a.
The failed head is not rerun, waived or causally repaired by this actual source
composition. PR684's earlier failure remains separate history. No denied
historical/native Reader execution, Core-owner admission, full legality,
performance, publication, migration, release, self-hosting or seed retirement
is claimed.
