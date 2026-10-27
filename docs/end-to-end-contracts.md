# End-to-end contract ownership and acceptance

Inspection baseline: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`, tree
`a887eae096998881ef3aef4fa18a6458ebb6ad20` (2026-10-26).
Outcome owner: cross-component ownership maintainer. Component source owners
retain their reservations; the overall coordinator owns integration disposition.

This is the sole detailed backlog for ownership cleanup, using OWN-01 through
OWN-06. The [component index](component-roadmaps/README.md) supplies navigation;
component implementation tasks remain in their existing linked ledgers. These
records describe joins between components, not replacement component checklists.
All six joins remain open beyond the explicitly accepted scoped increments below.

## Governing boundary

[D1](phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md),
[C1](phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md),
[C18](phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md)
and [D9](phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
require explicit pass inputs, outputs, facts, invalidations and provenance.
Source authentication is distinct from capability analysis. Core construction
and resolution may share traversal, but final binding identities precede Types.
Profile legality precedes capability validation; ownership and safety precede
MIR. An optional domain path needs its declared semantic anchor; it is not a
prerequisite for ordinary MIR. Unoptimized verified MIR is a valid Lower input.

The inventoried path is actual declared source through Reader, Syntax, Expand,
Resolve/Core, Types, Effects, profile/capability/ownership/safety decisions, MIR,
optional optimization, Lower and Native artifact consumption. Diagnostics and
authentication span those interfaces. Compiler/runtime source dependencies,
including compiler, emitter and runtime inputs, must be inventoried when used;
a fixture program alone does not establish compiler-source closure.

Cleanup reaches 100% only when every interface below has one confirmed
accountable producer and consumer, compatible explicit requirements and coherent
scoped positive/negative execution evidence, or an explicitly identified product
capability outside this declared path. An unassigned owner, mismatched tuple,
restricted in-scope dependency or unavailable admission API remains open. A
role name, merged document or successful helper is not execution evidence.
This is not implementation of every future language feature. Other backend
families, domain portfolios and full seedless product release retain their
separate [component outcomes](component-roadmaps/README.md#integration-outcomes);
they cannot be used to exclude a dependency actually consumed by this path.

## Accountable interface chain

Each row names semantic responsibility. A confirmed implementation reservation
for one slice does not assign the entire component. Where current ownership is
unconfirmed, the ownership maintainer must obtain an explicit handoff before
another writer changes source. The existing
[ownership map](self-hosting-slice-ownership.edn) is historical routing, not
proof that an old coordinator or every listed worker is currently active.

| Interface | Accountable producer -> consumer | Required handoff and rejection |
| --- | --- | --- |
| Source -> Reader -> Syntax | Reader maintainer -> Syntax maintainer | Actual bytes/path/options, exact spans/raw literals/trivia, seed and source identities; malformed encoding/forms and substituted source or seeds reject. C2/C3 own semantics. |
| Syntax -> Expand | Syntax maintainer -> Expand maintainer | Immutable verified syntax, namespace/profile/hygiene/metadata and generated origins; capture, wrong phase and unauthorized build effects reject. C3/C4. |
| Expand -> Resolve/Core -> Types | Expand -> Resolve and Core maintainers -> Types maintainer | Expansion lineage, real binding/module identities, evaluation order and verified canonical Core; unresolved names, foreign parents and changed source maps reject. C4/C5/C6/C7. |
| Types -> Effects | Types maintainer -> Effects maintainer | Node and binding identities, types, Fn signatures and latent effects; wrong type facts and undeclared effects reject. C7/C8. |
| Effects -> profile -> capabilities -> ownership/safety -> MIR | Effects -> Legality profile interface -> Auth capability interface -> Legality ownership/safety interface -> MIR maintainer | Explicit effect/grant intersection and current type, profile, ownership, safety, proof/check and origin facts; missing/stale facts reject before MIR acceptance. C7-C11 and L15. |
| MIR -> Opt or Lower | MIR maintainer -> Opt/Lower maintainers | Verified target-independent control/data flow, fact tables, source and domain anchors; malformed CFG, missing facts and target leaks reject. C11/C13/C14. |
| Opt -> Lower | Opt maintainer -> Lower maintainer | Deterministic changed/no-change decisions, invalidation/regeneration and post-pass verification; stale facts and proofless check erasure reject. C13/C14. Opt is optional. |
| Lower -> Native/Emit and runtime | Lower maintainer -> Native consumer and Emit/runtime maintainers | Legal target/ABI/provider request and evidence-bound artifact/report tuple; unchecked input, unsupported operations, missing services and substitutions reject. C14/B1 and declared backend/runtime contracts. |
| Diagnostics and artifact authentication | Each semantic producer -> Diagnostics renderer and Auth verifier -> immediate consumer | Owning-stage diagnostic, source/generated origins and fresh source/artifact/policy identity; formatting or a cached pass cannot grant semantic acceptance. C15 and PKG3. |

These component acceptance interfaces and their sole task ledgers are linked by
[Reader](component-roadmaps/reader.md), [Syntax](component-roadmaps/syntax.md),
[Expand](component-roadmaps/expand.md), [Resolve](component-roadmaps/resolve.md),
[Core](component-roadmaps/core.md), [Types](component-roadmaps/types.md),
[Effects](component-roadmaps/effects.md), [Legality](component-roadmaps/legality.md),
[Auth](component-roadmaps/auth.md), [MIR](component-roadmaps/mir.md),
[Opt](component-roadmaps/opt.md), [Lower](component-roadmaps/lower.md),
[Emit](component-roadmaps/emit.md) and [Runtime](component-roadmaps/runtime.md).
Types, Effects, Legality, Auth and runtime-wide active writer identities are not
established by this inspection. The accounting writer is coordinating its
C7/Effects/emitter handoff; it does not inherit all these semantic components.

## Finite integration backlog

### OWN-01 - Source identity, Syntax and diagnostic continuity

Accountable join: ownership maintainer coordinates Reader/Syntax and Diagnostics.
Existing Reader/C14 writer retains its source and private observation harness.
Contracts: C2/C3/C15. Affected source interfaces:
`bootstrap/gravity/src/gravity/bootstrap/{reader,syntax}.gravity`,
`bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity`,
`bootstrap/clojure/src/gravity/{source_unit,c2_artifact_identity}.clj` and the
Reader rejection/diagnostic tests under `bootstrap/clojure/test/gravity/self_hosting/`.

Dependency: explicit source/options and actual current Reader products. No
whole Build or release prerequisite. Acceptance: both source extensions preserve
their actual paths, byte/scalar spans, origins and current identities through
fresh Syntax; malformed/tampered inputs retain owning-stage diagnostics.
Record supported surface and known missing families against the Reader ledger.
The corrected full-C11-source C2 run now passed on the exact frozen source tuple
recorded below, after independent harness and result review. All nine acceptance
checks passed, including actual Reader execution, authenticated envelope, fresh
verified replay, source bindings and lexical/representation validation. This
closes that positive C2 dependency observation, not all Reader/Syntax surfaces or
the downstream join. The previous 120-second timeout and 206.726-second
observation failure remain separate nonpasses. Genuine C3 and C5 consumption of
the complete source is the next open integration boundary; no downstream run
or source authority is inferred from this receipt.

### OWN-02 - Authenticated Source-to-Core population

Accountable join: ownership maintainer with Expand/Resolve/Core owners; confirm
remaining active writer identities before source changes. Contracts: C3-C6.
Exact interfaces: `bootstrap/clojure/src/gravity/authenticated_artifact_pipeline/01-pipeline-schema-version.clj`
and `04-envelope-valid.clj`; Gravity `c3_syntax_object_model.gravity`,
`c4_macro_expansion_engine.gravity`, `c5_name_resolution_namespace_analyzer.gravity`,
`c6_core_lowering_engine.gravity` under `bootstrap/gravity/src/gravity/compiler/`,
and `bootstrap/gravity/src/gravity/checked_core.gravity`.

Dependency: OWN-01 for real source cases. The ordinary authenticated pipeline
exposes only source/Reader/Syntax/expansion/resolution/checked-Core stages; its
checked-Core endpoint does not establish C7-C10 facts. Acceptance: explicit
required source population, actual immediate-parent products and current module
inputs reach verified canonical Core, preserve source lineage and reject
coherently rehashed substitutions. Accepted development invocation
[pilot evidence](../.specify/reader-core-invocation/evidence.md#verified-integration-receipt)
is retained: implementation #388 at `001ab9ed`, documentation #381. That finite
paired-source receipt does not accept the full compiler-owner population.
Remaining population and its source-to-Core results stay open under the
[current migration cohort](self-hosting-current-migration-cohort.md).

### OWN-03 - Checked facts into actual MIR

Accountable join: ownership maintainer coordinates Types/Effects/Legality/Auth
with the existing MIR producer owner. Contracts: C7-C11 and L15.
Source: `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`,
`c8_effect_checker_engine.gravity`, `c9_ownership_checker_engine.gravity`,
`c10_safety_analysis_pipeline.gravity`, `c6_c10_checked_core_pipeline.gravity`
and `c11_mir_specification.gravity`. Consumer fixtures include
`bootstrap/clojure/test/gravity/self_hosting/sh12_c10_mir_adapter_test.clj` and
`sh12_authenticated_mir_integration_test.clj`.

Dependency: OWN-02 plus real independently owned analysis facts; development
against pinned fixtures can proceed earlier. Acceptance: original-source Core
and actual analysis products reach the generic MIR builder and actual verifier;
type/effect/profile/grant/ownership/safety/origin substitutions reject. Preserve
runtime checks and proof references through serialization. Confirm currently
unassigned component ownership and pair each claimed fact with its actual
producer; constructed fixture declarations cannot fill a missing producer.
The finite producer/consumer composition preserving #378 and #383 ancestry
merged through #421 at `06a256fdb97f49b5fedea36a6803483302631bbc` (receipt
below). This accepts the exercised development joins, not the complete analysis
fact population required by OWN-03. C12 execution remains prohibited; any consumed C12
dependency remains a blocker, not an excluded passing path.

### OWN-04 - MIR/Native schema, parameters and DATA continuity

Accountable join: existing MIR composite owner with Native consumer owner;
ownership maintainer records compatibility and acceptance, not a second codec.
Contracts: C6/C11/B1. Producer source is C11 above. Native consumer files are
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00734-p15-s23-c11-mir-runtime-check-fact-record.clj`,
`00735-p15-s23-c11-mir-downstream-ids.clj`,
`00736-p15-s23-c11-mir-block-keys.clj`,
`00738-p15-s23-c11-mir-validate-operations.clj`,
`00739-p15-s23-c11-mir-validate-terminator.clj`,
`00740-p15-s23-c11-mir-validate-effect-edge-placement.clj` and
`00741-p15-s23-c11-mir-map-blocks.clj` in that same directory. Native #383 added
`bootstrap/clojure/test/gravity/self_hosting/sh01_native_original_bool_compatibility_test.clj`;
that fixture is now integrated through #421. Its absence at the original
inspection baseline is historical, not the current disposition.

Dependency: OWN-03's exact input tuple and one reviewed shared DATA domain.
Acceptance: genuine original construction, complete typed DATA preservation,
independent fresh replay and actual Gravity/Native positives and crossed-source,
parameter/type/quota negatives on one compatible tuple. The generic SH12 branch
builder already delegates to the generic C11 builder; another run of that helper
does not itself exercise a new producer. Native #383 removed stale consumer quota
expectations in 00734/00736/00741; merged #421 composes that change with #378 and
the independently guarded Bool-entry-parameter consumer. Native owns the
parameter descriptor and shared codec; MIR owns producer/integration semantics.
The shared V3 DATA domain and genuine source joins below received independent
review. The earlier controlled codec probe alone was insufficient; the later
accepted evidence must not be described as that probe or as timeout-only work.

#### Accepted finite OWN-04 joins

[PR421](https://github.com/burn2delete/uniform/pull/421) merged reviewed head
`d67406b305d93e052f54c9439da8142efbb663c0` as
`06a256fdb97f49b5fedea36a6803483302631bbc`, with identical full tree
`6ed68a337f85d9a044d7eb3d08aa1ae8eebccea2`. The
[frozen receipt](https://github.com/burn2delete/uniform/pull/421#issuecomment-6428826929)
retains each producer identity; reconciliation does not reissue its source runs.

| Accepted join | Exact producer and evidence | Remaining boundary |
| --- | --- | --- |
| Zero-arity source to C6/MIR and Native | `8b9dd46ea38eafba1d3d7a49d8c6ca6b6488563f`, tree `ed9d118e18f5f1a3f04138603e282ade9fb70701`: genuine true `.gravity` and false `.qst` construction, separate fresh C6 verification, Gravity reconstruction verification, Native positives, both source-substitution directions and exact type/quota negatives. Separate children naturally exited 0 at 2 GiB/120 seconds. | B's DATA was inspected in-process but not retained for a later independent saved-artifact rewalk. No other source or parameter family is inferred. |
| Composed Native consumer | `38f8f6e0`, tree `5b2f86c3`: retained A passed real seal/provenance, Gravity and Native consumption, with type, retired-scope, source-Core and parameter-impostor negatives; 42 tests / 426 assertions, natural exit 0. | This reconsumes A; it is not a new original source construction. |
| Single Bool entry parameter | The same `38f8f6e0` / `5b2f86c3` tuple: genuine c41 `.gravity` original 1/7 and separate independent fresh replay 1/48, natural exit 0 at 2 GiB/120 seconds. Full typed raw-C6 equality, actual C6/Gravity verification, Native acceptance and parameter negatives passed. | Parameter `.qst` and other parameter families are not accepted by these receipts. |
| Final main reconciliation | `d67406b3` / `6ed68a33`: independent source/contract review conserved the SH04 projection, fallback, descriptors and fresh authentication; fresh focused/boundary/LOC controls 21/94, docs 240/19 and exact integration preflight passed. | Earlier source evidence retains its original tuple. No emitted-code or final-artifact admission is inferred. |

The parameter handoff's strict V3 typed DATA digest is
`b065758e2bcc644b75835046d93ba70dec7479beea5974370864563040012958`.
Its supported concrete collection classes, metadata and backing/view policy
were reviewed; no equality relaxation replaced the source/callback guards.
The C11 producer in these receipts is 567399 bytes, SHA256
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`.
It must not inherit the full-Reader b9 source receipt or vice versa.

These finite development joins are complete. OWN-04 still requires a disposition
for each remaining inventoried supported schema/parameter path and its actual
producer-consumer evidence; unsupported families receive no acceptance credit.
OWN-03's complete fact population remains separate. Current-source final
artifact/report successor admission belongs to OWN-06 below, not a reason to
erase or repeat these accepted development joins. No C12, Opt/Lower, emitted-code,
release or self-hosting acceptance follows from the counts.

### OWN-05 - Optimization and lowering consume the same accepted MIR

Accountable join: Opt #370 owner, Reader/C14 #374 owner and Native/Lower owners.
Contracts: C11/C13/C14/B1. Source:
`bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity`,
`c14_target_lowering_architecture.gravity`,
`bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity`;
fixtures include `sh16_checked_mir_identity_bridge_test.clj` and
`sh17_c13_optimized_mir_carrier_test.clj` in the self-hosting test directory.

Dependency: OWN-03/04 for the consumed MIR interface; OWN-01/02 for contextual
source. An unoptimized path may bypass Opt, never verification. Acceptance:
actual current producer output reaches Opt/Lower with post-pass verification,
fact invalidation/regeneration, residual checks and target/ABI/provider identity;
stale source/layout/facts, unsupported operations and proofless check elision
reject. #374 still needs real C5 dependencies and original C6/C11/C13 products
before contextual C14 acceptance. #370's current consumer contract belongs to
its existing owner; static synthetic fixtures are not genuine source composition.

### OWN-06 - Final artifact/report and execution admission

Accountable join: ownership maintainer coordinates Native/Lower/Emit/runtime
owners and the overall coordinator's final disposition. Contracts: C14/B1,
PKG3 and the selected backend/runtime contracts. Exact admission surface:
`contracts/p15-public-native-admission-v1.edn`,
`bootstrap/clojure/src/gravity/p15_public_native_admission.clj`,
`bootstrap/gravity/p15_s23/{compiler,emitter,runtime,native_plan_c_emitter}.gravity`
and `bootstrap/clojure/test/gravity/self_hosting/p15_public_native_admission_test.clj`.

Dependency: OWN-01 through OWN-05 for each actually consumed path. Acceptance:
accepted source/implementation/policy/target inputs bind the actual emitted
artifact, verifier report and execution result; original source/facts/report/
artifact substitution and undeclared runtime services reject at the real consumer.
`p15-s23-c11-mir-validate-constructed!` accepts Core/MIR and produces a fresh
report; saved-report equality demonstrates reproducibility, not rejection of a
supplied report. The distinct existing final-artifact validator in 00743 accepts
an artifact-contained report, recomputes it through 00740 and requires ordinary
and type-sensitive equality. The public C11 verifier in 00744 additionally
authenticates Core/context and performs independent Gravity builder replay.
Those APIs do not yet accept the current c41 source through the historical
330835-byte/cf7 source-rule contract. The MIR producer and Native consumer owners
are preparing an explicitly versioned successor through these existing APIs;
design work is not a passing final-admission receipt. The frozen final artifact route and admission contract's
unfrozen replay-owner policies remain separate open interfaces. No repinning,
injected pass report, synthetic final artifact or relaxed source guard closes
them. Runtime writer/acceptance ownership must be confirmed; accounting removal
does not transfer Unicode, collision, callback or semantic safety responsibility.

## Exact evidence and outstanding boundaries

Locally inspected main and open PR metadata at the baseline above. Draft heads:
359 `4c5aac01190e52fe013909437987071826492d5e`;
363 `95f6afeb6e0ae853ebb9a4073a99ef3ba2e6599e`;
370 `516f5f6ca77aede73fc7a6d7ec56d5a6da606ee6`;
374 `83e121ed010729d6f6a8bad4a4d4d33095c14a70`;
378 `55a195f089e319caacb4b51e3a4946cb5d66be20`;
383 `356ba311afa4ec80afae4188db87d1511b5dc6ce`;
421 `8b9dd46ea38eafba1d3d7a49d8c6ca6b6488563f`.
These are inspection identities, not statements that each worker is running.

Reader owner reports source revision `2422be8ce0d1de5e12a3e741244ef96a319bba9b`,
tree `329a1978796509ae7d7b159a89921d97d2064d61`, C11 source 573124 bytes,
SHA256 `b9b68c69eaeb1118eee84aaa8f911917884e572995f83421bd9086288ee5184f`.
Local Git-object readback independently confirms this C11 source hash on the
inspected main baseline; it does not rerun the owner's construction.
MIR owner reports composite tree `ed9d118e18f5f1a3f04138603e282ade9fb70701`,
C11 source 567399 bytes, SHA256
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`.
The initial latter-source run timed out at 120 seconds with no completed test
counts; it remains a historical nonpass. Helper 36/327, harness 10/52 and
controlled codec 10/92 receipts did not replace it. The subsequently accepted
source and composed-consumer receipts are recorded under OWN-04 above.
These are attributed owner reports, not locally rerun acceptance. Different
source tuples cannot be joined by comparing counts or relabeling historical pins.

### Independently checked full-C11-source C2 increment

The corrected run used frozen commit
`2422be8ce0d1de5e12a3e741244ef96a319bba9b`, tree
`329a1978796509ae7d7b159a89921d97d2064d61`, with the 573124-byte C11 source
and `b9b68c69...5184f` hash above. It exited naturally with 0 in 193.576
seconds at an actual 2147483648-byte maximum heap under the unchanged
240-second host profile. Source identity and all captured control/input hashes
were equal before and after; the worktree was clean. The valid observer retained
12 detailed events, 55 completed resolver calls and no resolver errors.

Independent result readback checked all nine owner acceptance flags and the
actual execution/fresh-verification events against the reviewed harness. The
receipt SHA256 is
`04462d360ca77faa67aacde3626ae61f22b75fb04b7d46e318c3c7de17719829`;
its unchanged plan/harness/capture SHA256 identities are respectively
`31e9e12540c214b5e10dd88b0e98e37a8a0752e62cd6b1e228e0dcec9f12e48d`,
`b9ff51c1e183804e0f254ddbacce771ad180f9d4768d7376d043e888784a2802`,
and `8accd74a643d3cb8572b48fa16fd39974a36585de8a9cae127c9d5ddfbb8514f`.
No construction was repeated during readback. This is development evidence for
the complete C11 source through C2, not a current-main run, speedup comparison,
C3/C5 composition, C6/MIR producer admission, C14, migration or public release.

The supported next proposal is one ordinary five-argument current-source C5
constructor, which builds C2/C3 once and forwards the actual parent/invocation
through SH05/SH06. The current-selected C3 API requires direct construction;
saved C2 injection or nil-argument/token workarounds are not an alternative.
The proposal needs its own exact plan, profile and execution evidence. It does
not itself establish the downstream acceptance or authorize another run.

Every future closure receipt must name exact head/tree and source/dependency/
harness identities; actual producer and consumer invocations; passed, failed and
unrun stages; original/fresh/substitution artifact identities; runtime/seed and
development-unadmitted versus frozen/public boundary; and independent review and
integration disposition. Cheap harness/serialization/saturation checks precede
heavy work under the existing brokers and locks. Reuse immutable evidence only
for demonstrated unchanged dependencies. C12 remains restricted and the original
C7 publication operation unresolved; neither is retried or transferred here.

This documentation increment changes no compiler source, component status,
admission policy, source pin or execution authorization. Structural validation
and independent review of this document do not close OWN-01 through OWN-06.
