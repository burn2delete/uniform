# End-to-end contract ownership and acceptance

Inspection baseline: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`, tree
`a887eae096998881ef3aef4fa18a6458ebb6ad20` (2026-10-26).
Outcome owner: cross-component ownership maintainer. Component source owners
retain their reservations; the overall coordinator owns integration disposition.

This is the sole detailed backlog for ownership cleanup, using OWN-01 through
OWN-06. The [component index](component-roadmaps/README.md) supplies navigation;
component implementation tasks remain in their existing linked ledgers. These
records describe joins between components, not replacement component checklists.
OWN-04 is complete for its declared supported development adapter; the other
five joins retain the residual acceptance gates below.

## Current cleanup assessment

Assessed 2026-10-26 22:28 MDT (2026-10-27 04:28 UTC) against main
`15a5499e97798621d270c36dd2546ce98ddd62a9`, tree
`aa5dd1d419594b31f9f9837ba6d09e39894b3562`. This updates the original
OWN-01 through OWN-06 criteria, not their scope or the component task ledgers.
The original inspection baseline above remains historical provenance.

| Original ID | Current disposition | Accepted merged progress | Exact remaining acceptance |
| --- | --- | --- | --- |
| OWN-01 | Partial | Full-C11 C2 construction/authentication/replay receipt recorded through PR436; Unicode scalar coordinates through PR471; fresh Reader-to-Syntax decimal and expansion-lineage handoff through PR482. | Complete the original supported source/diagnostic and identity rows with actual paired source paths, origins and tamper diagnostics. PR482 leaves other Reader rows and canonical generated-node coverage open; full-C11 C3/C5 consumption is not accepted. |
| OWN-02 | Partial | Paired-source authenticated source-to-Core pilot PR388 at `001ab9edbe84ca0132f3ebc7e6117792ec8d3e13`, with its original receipt. | Establish the required compiler-owner source population through real immediate parents and current module inputs into authenticated canonical Core. The pilot and a passing C2 receipt do not fill this population; full-C11 C5 attempts remain nonpasses. |
| OWN-03 | Partial | PR421 composes genuine Core/MIR construction, verifier and Native consumers for the exercised Bool development joins. | Populate every claimed type/effect/profile/grant/ownership/safety/origin fact from its actual owner and exercise substitutions at the actual MIR consumer. Fixture declarations and the finite Bool paths do not establish the full required fact population. C12 remains restricted where actually consumed. |
| OWN-04 | Closed for the declared supported Bool development adapter | PR421 zero-arity and Bool-parameter joins, PR438 parameter consumer integration, PR465 missing Bool `.qst` original/fresh join, and PR476 independently reviewed supported-family disposition satisfy the criteria enumerated below. | No additional run is identified inside this adapter. Current-source final admission is OWN-06. General nil/unit and other unsupported product families retain their MIR ledger obligations and receive no acceptance credit. |
| OWN-05 | Open | Existing scoped MIR acceptance is available as input; verified unoptimized MIR is explicitly allowed into Lower. | Demonstrate the same actual accepted MIR through the selected Opt/Lower path, with post-pass verification, fact invalidation/regeneration and target/ABI/provider identities plus stale-input and proofless-elision negatives. PR370 and contextual PR374 remain drafts; no contextual C14 acceptance is inferred. |
| OWN-06 | Partial | Existing final-validator APIs and the accepted development source joins are identified; their structural/report and source-authentication responsibilities are separated below. | Complete genuine current-source public Core construction/fresh report parity, then version propagation into the actual C11 final artifact/public report consumer with source/context/policy/report/artifact negatives. Bind actual target/emitted artifact/execution inputs where this path consumes them. Historical source pins and development acceptance cannot substitute for final admission. |

Merged join identities are PR421 `06a256fdb97f49b5fedea36a6803483302631bbc`,
PR438 `164a28c203ffd61b51ab35830fa950165da0095f`, PR465
`82f7e8d62c71b4e5bd895f8b62c2c76363e15d3b`, and the last merged OWN
criterion disposition, PR476 `5e84b5acb59f605d8f71cc2d5cf21cc24d15a662`.
Their original source, tree and execution receipts remain in OWN-04 below;
none is relabeled as a run on the assessment main. Reader PR482 merged at
`6c3d9e448be2caa290cf0af0a8c2b211c162d723`. Its genuine owner observations
were independently accepted within a combined exit-1 batch; the separate
correct-context composition passed 19/106. This is scoped evidence, not a
blanket passing-batch or complete OWN-01 claim. See its
[existing handoff report](artifacts/phase-06/reports/current-reader-fresh-syntax-handoff.md).

The next original OWN-06 increment belongs to the existing Native public Core
writer and MIR private integration writer. The current frozen candidate is
`f7f86a042a14885489aca5a3a885474a6070626c`, tree
`0f4c6d319fb18573b5b4bed38de772430600beb9`. Independent source/harness review
has accepted preparation for one genuine public Core constructor observation;
there is no accepted original/fresh Core result at this assessment. The prior
expression-owner observation is an accepted prerequisite, not Core admission.
Public PR453 at `10a03c3dcc247bbb2286e92e8edd3c0b24e1aa8a` and PR475 at
`edc0009c4190b8c6fbfb15cb8960dee6fc687f79` remain drafts and are not the
unpublished candidate's result. Fresh Reader-result/SourceRule and expression
binding authentication, typed semantic/target/physical/root parity and actual
final C11 consumer rejection gates remain open. Existing writers and their
guarded process route are preserved; this assessment starts no execution.

The dashboard's 55% from 2026-10-26 04:11 is a historical provisional estimate,
not measured coverage. This assessment supplies completed and remaining
milestones instead of a replacement percentage: the original IDs have unequal
scope and no agreed numerical weights. PR, test and assertion counts are not
a completion denominator. Recent Reader handoffs advance OWN-01; the necessary
PR483 Bootstrap load-order repair restores a shared entry dependency, but does
not itself close another OWN criterion. Full cleanup remains open until the
remaining original joins meet their own positive and negative acceptance gates.

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

#### Selected Syntax: source and diagnostic consumer contract

The next selected-development join must preserve user-source identity separately
from the selected compiler implementation binding. PR523 merged at
`e0f1f50a8009dfd9f9c7487b863a17d7449406b2`, after CI prerequisite PR525 at
`ef280ef40b9ae807097015b6c6f29f124b866c9d`. This removes the hosted C2/C3 depth
policy and repairs three source-resolver expressions; it does not execute the
selected Syntax positive. Main `cbf5c210544a0033e9c755d7ef8358f01f1ad57c`
still builds Syntax with its existing current-binding route. The separately
reviewed selected-operation proposal at
`b83e8b1c24201e052bce4bc6617d5f3b4c9ffe96`, tree
`878ec677a73c122468d6d501527c4bcbbb82cd81`, is not that main composition.

This is the finite applicability contract for the existing Reader/ACC13
consumer, not another implementation task or permission to replace its harness:

| Producer -> consumer | Required source-backed edge | Rejection or comparison oracle |
| --- | --- | --- |
| Source unit -> C3 descriptor | `sh04-syntax-semantic-source-id` hashes logical stem, encoding, byte hash and Reader options. `sh04-syntax-source-descriptor` carries the form ID, token range, byte/scalar/line/column span and Reader-produced source origin. | Compare the actual returned descriptor with its actual C2 source unit, seed and form record. An implementation binding or source hash alone cannot establish these coordinates or origins. |
| Reader expansions -> Syntax envelope | `sh04-syntax-reader-expansion-history` derives roots from the current Reader identity view; a nonempty origin history contributes `:reader-expansion-history-id` to the summary and authenticated descriptor. | `sh04-syntax-reader-expansions-current?` checks history presence/value, the normal envelope verifier and current source-node origins. Removing or substituting history requires rejection, not just a new outer digest. |
| C3 -> SH05 | `sh05-default-macro-request` retains `:origin-chain` and `:call-site-span`; its semantic span substitutes the co-canonical source ID for the physical diagnostic source. | Preserve actual-path diagnostics while comparing semantic identity only within the declared co-canonical domain. Physical paths and semantic source IDs are not interchangeable oracles. |
| SH05 -> C5 | The C5 artifact retains its actual SH05 parent. Its verifier checks `:fresh-upstream-sh05-verification?`, `:authenticated-resolution-request-current?` and `:provenance-current?`, including the request's actual source path. | A changed parent or source provenance must fail normal fresh verification. Equality of a supplied report or an independently reconstructed substitute parent does not prove this join. |
| Rejected generated reference -> diagnostic | `sh06-resolution-diagnostic-origin` transports an existing origin only after exact failed-reference, span, namespace/profile/target, parent and source-revision checks, with one uniquely matching source syntax. | Require the original rule and diagnostic fields unchanged except for the justified origin chain. Module/import errors borrowing a reference's display span, ambiguous owners or altered causal fields must not acquire that chain. |

The implementation edges are in the existing SH03-SH07 components:
[source ID and span](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00852-sh04-syntax-resolve-template.clj),
[source descriptor](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00853-sh04-syntax-reader-binding.clj),
[expansion history](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00854-sh04-syntax-rich-object.clj),
[summary and envelope](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00855-sh04-syntax-resolved-result.clj),
[Macro request](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00858-sh05-macro-execute.clj),
[fresh C5 verification](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00879-sh06-resolution-artifact-verification-bounded-02.clj)
and [causal diagnostic origin](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00880-sh06-resolution-artifact-verification-03.clj).
The existing `sh06-generated-diagnostic-origin-transport-development-kernel`
test covers the bounded transport oracle with interposed authenticated-parent
and request premises; it does not establish a selected-development execution
receipt or authenticate the modeled parent.

Two oracle limits matter. A physical-only provenance substitution may leave
semantic IDs equal and must still fail its owning physical check. Conversely,
requiring every C5 rejection to contain a generated origin would be incorrect:
only the exact causal-reference case above permits that transport. The selected
consumer must exercise these checks through actual owner products and normal
verifiers, without supplying authoritative reports or replacing owner functions.

The prepared 192-byte `.gravity` fixture is one construction case, not paired
extension coverage. The logical-stem function strips `.gravity` and `.qst`, but
semantic identity also consumes bytes and Reader options. OWN-01's declared
paired-source criterion therefore remains applicable: record both actual paths,
spans and origins, and justify any semantic equality from their actual inputs.
No second extension, relocated source or generated-node family is accepted by
static inspection. Genuine selected binding, AE, immediate-parent construction,
per-field rejection and relocation remain with the existing Reader/ACC13 writers;
the first new compiler run also awaits resolution of admission safety.

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

#### Bounded fact producer and consumer contract

The accepted Bool path has one concrete fused producer, not six independently
observed public analysis calls. The Native Core owner is accountable for the
public C6-v2 constructor and fresh verification (`00724a`/`00726`), which execute
`bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity`.
That source has SHA-256
`0e7e30e15bc68dabdcee3cfea4c91740d82808204c5fe322d67ccd0ffdb76b98`.
`c6-c10-build-checked-core-template` calls `c6c10-build-fact-tables` and
`c6c10-build-node-facts` on actual lowered Core nodes. These are executable
Gravity derivations, not fact tables supplied by an acceptance fixture.

| Product in the exercised pure Bool path | Actual fused producer basis | Immediate consumer requirement |
| --- | --- | --- |
| Type facts | Node operation, type, operands and source-origin identity | C11 maps facts to MIR node identities; Native compares the complete type table against the authenticated Core. |
| Effect and capability facts | Pure effect closure; capability requirements, grants and grant-ID vectors are empty, and authority is explicitly `:none-required` | Preserve those exact empty requirements and the linked fact identities. They do not evidence a nonempty grant or effect family. |
| Ownership facts | Persistent shared owner domain and source-value identities | Preserve the actual ownership facts. Borrow, move, consume, region and linear-resource variations are not exercised by this path. |
| Safety facts and proofs | Actual node safety outcomes linked to type, effect and ownership fact IDs | Preserve outcome/proof links and residual runtime checks; labels alone do not authorize check elision. |
| Profile/target and origins | Hosted/JVM facts; `c6c10-origin-products` and `c6c10-register-origin` join actual source origins | Preserve profile/target, origin and dominance relationships rather than reconstructing them from a synthetic source label. |

The MIR producer owns the actual c41 builder's consumption of these products.
The Native consumer owns `00737-p15-s23-c11-mir-validate-fact-tables.clj`, which
independently reindexes the authenticated Core's tables and compares the MIR
tables and capability/safety proofs. This is independent consumer validation
against that same Core, not an independent public C7/C8/C9/C10 producer result.
The existing `00739` and `00740` origin/CFG/runtime-check requirements remain.

Frozen public Core construction/replay used
`cb3701556e9082ae8387c18fa1f6396a9d284617`, tree
`b873b9aa3674622eada97bc0278256f9df584c22`, for source and consumer. Later
accepted C11 construction/replay and substitutions used that original physical
source with consumer `9c27c3e8856b9659482a3e48238896e1548fce02`, tree
`3a08f38f6797a28b4f421146df0fac2e72ef349f`. The real type-fact removal from six
facts to five was rejected by `C11-TYPE/:id-indexed-c7-type-facts`; its retained
independent review SHA-256 is
`19c32be46ded04e9eb63ddb5847d3faf6d77680d212f5c546265e45fbb82d94a`.
It establishes the exercised consumer check, not the full C7 producer population
or a run on current main. Current-consumer integration remains with Native;
MIR retains its existing private validation harness. No new execution is implied.

D1 permits fused implementations with explicit equivalent pass boundaries.
The C7/C8/C9/C10 pass-history labels in this source do not by themselves prove
separate producer API/result identity composition. This contract identifies the
real bounded producer and immediate consumers; the original OWN-03 independent
fact-population/ownership requirement remains partial. General nonempty
grant/effect/ownership/safety variations retain their component obligations;
they are not new unsupported Bool cases to invent for OWN-04. No full raw
positive-report or owner policy-hash recomputation is claimed from this mapping.

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
| Single Bool entry parameter, `.gravity` | The same `38f8f6e0` / `5b2f86c3` tuple: genuine c41 `.gravity` original 1/7 and separate independent fresh replay 1/48, natural exit 0 at 2 GiB/120 seconds. Full typed raw-C6 equality, actual C6/Gravity verification, Native acceptance and parameter negatives passed. | These original receipts retain their `.gravity` attribution; other parameter families are not inferred. |
| Single Bool entry parameter, `.qst` | `66f46c53aaa24bfecffca782eb8f2128b85ac1e1`, tree `bce85a1d8daac945ab26b039f180685cf68990b6`: genuine original 1/7 and separate independent fresh replay 1/48, both natural exit 0 at actual 2 GiB under the unchanged 120-second execution profile. Complete typed raw-C6 equality, fresh C6 verification, Gravity build/verification, Native acceptance and the existing parameter/source/type negatives passed. | This closes the missing `.qst` case for this supported Bool diamond. No other parameter family, final admission or newer-tuple source execution is inferred. |
| Final main reconciliation | `d67406b3` / `6ed68a33`: independent source/contract review conserved the SH04 projection, fallback, descriptors and fresh authentication; fresh focused/boundary/LOC controls 21/94, docs 240/19 and exact integration preflight passed. | Earlier source evidence retains its original tuple. No emitted-code or final-artifact admission is inferred. |

The parameter handoff's strict V3 typed DATA digest is
`b065758e2bcc644b75835046d93ba70dec7479beea5974370864563040012958`.
Its supported concrete collection classes, metadata and backing/view policy
were reviewed; no equality relaxation replaced the source/callback guards.
The C11 producer in these receipts is 567399 bytes, SHA256
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`.
It must not inherit the full-Reader b9 source receipt or vice versa.

The `.qst` candidate changes only the private source-test helper relative to
`38f8f6e0`: its existing one-argument `.gravity` default is preserved, with an
explicit second argument restricted to `.gravity` or `.qst`. Reader released
that helper to the existing Native C6 owner before the change. Native reused
the original guarded controller and shared V3 codec, archiving the historical
`.gravity` controller bytes and receipts. No saved Reader product, accepted Core
or verification result was injected; only original raw C6 DATA crossed the
externally gated handoff. Independent source/harness review and compiler-free
controls passed 6/56 before either source child ran.

Original and replay execution phases were 99803.001334 ms and 106984.993042 ms;
these are not total controller/process wall times. Both external supervisors
observed natural exit zero. All four source/harness bookends matched on the
frozen `.qst` tuple; source and controller stderr were empty. The raw handoff
SHA256 is `c08a443d902d639ef5f72699aac4bb86c94516302be3dd04220aebb17fea8687`.
Original/replay terminal SHA256 values are
`5b87d16342d143871985220e987973bce304998fca149f30f8585f757f06ff7a` and
`6ad973861c7ca05b740a4d8354e375673afddbd87361e260a1d4e9ebb593d3d7`.
Retained-result readback checked those hashes, the `.qst` source selection,
complete typed comparison and actual C6/Gravity/Native verification. Earlier
cheap-control failures remain nonpasses; this adds no original source retry.

Normal main composition retains these receipts at `66f46c53`. Incoming Reader
index changes from PR442/449 do not receive a new full source-run claim here.
The source-test helper, fixture pair, consumed Gravity C6/C11/Reader/emitter sources
and p15 independent constructed-validator components remain identical to that
accepted candidate. The separate hosted `gravity/c11_mir/validation.clj` facade
changed through PR447/`0968daed`, adding termination/dominance checks to its
status, proof and diagnostic handling. The frozen source receipts do not accept
that changed facade or reexecute it on the newer tuple. The helper addition
and this acceptance record require their own reviewed main
integration; merging them does not reissue the frozen semantic receipts.

#### Supported-family disposition

The MIR producer and Native consumer independently confirmed the following
boundary against the unchanged c41 source. This consumes the existing OWN-04
criterion; it does not add a language or parameter family.

| Current development-adapter row | Disposition | Accountable boundary |
| --- | --- | --- |
| Zero-arity literal-Bool diamond, `.gravity` and `.qst` | Accepted source construction/replay, DATA continuity and Native consumption in PR421, with original identities above. No repeat is needed. | Existing MIR producer and Native consumer |
| Single-Bool entry-parameter diamond, `.gravity` and `.qst` | Accepted `.gravity` evidence in PR421 and `.qst` evidence integrated through [PR465](https://github.com/burn2delete/uniform/pull/465), merge `82f7e8d62c71b4e5bd895f8b62c2c76363e15d3b`. No additional parameter permutation is identified inside this supported adapter. | Existing Native C6 constructor/controller and MIR verifier owners |
| Other opcode names or carrier classes in generic schema guards | Schema membership is not a claim of an authenticated source family. The reviewed V3 domain and complete actual artifact checks remain accepted; a generic DATA fixture cannot create another source join. | MIR/Native own their existing schemas and guards |
| Flat zero-arity nil / `:unit` | Outside the current supported/evidence-covered SH12 Bool adapter. No PR421/438/465 source acceptance applies. | Broader MIR construction/fact/verifier/downstream obligations remain open as detailed below |

The current supported Bool development joins and their DATA handoff are
accounted for. No further evidence run is identified inside that declared
adapter. OWN-03's complete fact population and OWN-05's Opt/Lower consumption
remain separate. Current-source final artifact/report successor admission is
OWN-06, not a reason to erase or repeat these accepted development joins.

The original OWN-04 criteria at this declared interface have the following
finite closure, rather than a test-count percentage:

- Producer/consumer accountability and schema agreement: confirmed by the
  existing MIR and Native owners, including the preserved Reader helper handoff.
- Original construction, independent fresh replay, typed DATA continuity and
  actual Gravity/Native positives: accepted in PR421 and PR465 on their exact
  tuples. Shared V3 class, metadata and backing/view rules remain unchanged.
- Crossed source, type, retired quota-field and parameter/fact/safety negatives:
  accepted in those same scoped receipts. PR438's parameter consumer work is
  integrated; its earlier source receipts are not relabeled as c41 executions.
- Reviewed main integration: PR421/438/465 are merged. The accepted source
  receipts keep their frozen identities; current-source final/public admission
  remains the distinct OWN-06 dependency.

These criteria are complete for the currently supported Bool development
adapter. This does not accept every operation named in the general MIR schema,
close general C11 product implementation, or grant current-source final
admission. Unsupported capabilities retain their governing obligations and
explicit owners instead of being counted as passing joins.

The nil row is a verifier-route boundary, not a demonstrated producer/consumer
encoding defect. `c11-build-operation` already derives `:unit` from
`:gravity/nil`, and `c11-build-linear-mir` uses that builder. Native component
00738 expects the same derivation. However, the executable SH12 constructor and
verifier require the original-C6 family preflight, which lacks flat nil support.
The generic `verify-c11-mir-module` returns a `:stage1-source-owned` description;
its check labels supply no executed verification credit. Skipping SH12 or
substituting that description cannot close the join.

General C6/C11 nil/unit semantics remain a product implementation/evidence
obligation under the existing
[MIR tasks](../specs/mir-workflow/tasks.md#implementation-tasks):
`mir1-construct-semantic-mir`, `mir2-attach-fact-tables`,
`mir3-verify-graph-and-legality` and `mir5-gate-downstream-consumption`.
These tasks are not closed or rescoped by this disposition. If that capability
is pursued, the MIR producer owns a narrowly reviewed real-family
preflight/verifier amendment, followed by genuine original/fresh C6 parity and
actual Gravity/Native verification with type, unit/value, payload, source and
fact negatives. Native repairs only a demonstrated consumer defect; its unit
rule and Bool successor scope stay intact. No new implementation or source run
was performed for this disposition.

No C12, Opt/Lower, emitted-code, release, self-hosting or whole-OWN completion
follows from this finite acceptance and unsupported-capability record.

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

#### Preserved v3 successor checkpoint

The 2026-10-28 Native handoff records a separate development candidate
`f66d9ff4554815b89ee7d51ee73b7961f0c33a44`, tree
`1ee9986eb613abe6c0218603d78d5f4279a4ac9d`. Its preservation receipt SHA-256
is `016db5b4ad2e69dce363edf8eb3da7158f63a5f67d3f4c7769bdae8f3a65876e`.
This is an attributed owner checkpoint, not a current-main admission result.
Its `00724a` fresh constructor distinguishes version 3 from the version-2 Bool
branch join: it selects the versioned Reader ingress, authenticates the source
binding, invokes the pinned Gravity builder and replay verifier, then requires
the trusted flat-Bool sequence family. Static inspection of that route does not
establish a successful call.

The preserved physical input composition names C6 source SHA-256
`7d14fef9a3ca0f47fd7ec301d96cd6c0b57c4c6a3f91a0839c673e8c702a6396`
(186745 bytes) and C11 source SHA-256
`4ca2bf8c551c9b0e7aefbeab84e7f7c9b5742bdeee10f68e74aff3470d35b3dd`
(597279 bytes). These owner-reported inputs are distinct from the historical
v2 tuples in OWN-03/04. The retained MIR identity handoff observes program
identities only; it explicitly claims neither Core construction nor execution
of the C11 builder/verifier. Program identity observation is not join acceptance.

The next semantic completion criterion remains genuine v3 Core construction
with complete source/runtime bookends and an external zero exit, independent
positive review, then a separately fresh public Core report using actual policy
state. Only that accepted product can supply genuine flat C11 construction,
fresh Native/Gravity verification and actual type-fact, source, target and
policy negatives for the versioned final consumer. Existing producer owners
retain their source and private harnesses. Historical v2 acceptance does not
discharge these gates or reopen the accepted OWN-04 Bool development adapter.
The recorded pre-admission capacity and child-timeout nonpasses remain
nonpasses; unresolved terminal/cleanup and runtime holds remain in force.
This checkpoint authorizes no execution and confers no OWN-06 closure.

#### V3 Core policy handoff and actual-consumer evidence

The following criteria describe the same preserved Native candidate
`f66d9ff4554815b89ee7d51ee73b7961f0c33a44`, tree
`1ee9986eb613abe6c0218603d78d5f4279a4ac9d`, not a new main-source admission.
The source interfaces are `00726-p15-s23-c6c10-gravity-candidate-verification`,
`00744-opaque-c11-construction-token-helper-02-invoke-checked-core-verifier`,
`00744-opaque-c11-construction-token-helper-03-authenticate-checked-core-ingress`
and `00740a-p15-s23-c11-current-core-policy`, all `.clj` components in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/` at
that exact revision. ACC10 retains genuine Core production and its private
harness; ACC11 retains the versioned consumer join. OWN records the contract.

| Actual boundary | Required evidence and commitments |
| --- | --- |
| ACC10 construction -> separately fresh public Core report (`00726`) | Retain the real artifact/context pair and explicit `{:interface :gravity/c6-c10 :version 3}` invocation. The fresh report must retain `:source-interface`, `:semantic-family :closed-flat-bool-literal-sequence`, actual `:source-binding`, `:source-policy-receipt` and `:source-policy-binding-id`. Its status/semantic authority, typed semantic comparison, target request, physical provenance and sealed-root parity must come from the actual verifier, not supplied report labels. |
| Fresh Core owner -> C11 public ingress (`00744`) | The versioned pure-source route invokes the fresh C6 verifier with that same version before projecting policy. Retain this actual call and result; a saved report, parsed header or standalone final-artifact DATA check cannot replace it. The unversioned overload selects historical version 1, not v3. |
| Fresh report -> C11 policy projection (`00740a`) | Preserve version 3 in the policy, receipt and compiler-program projection, with the flat-Bool semantic family in all three. Preserve `:development-unadmitted`, `:selected-current-source-host-seed` and the receipt's false `:self-hosted?`. Bind the compiler-program source byte count/hash and plan/functions/builder/verifier hashes to the selected source contract. |
| Policy -> actual Core/context and C11 artifact | `:source-policy-binding-id` binds the actual Core `:artifact-id`, context and receipt under `:gravity/c6-c10-source-policy-v3`. The distinct C11 `:source-policy-hash` binds the projected policy under `:gravity/c11-checked-core-policy-v3`. Preserve both identities through the actual versioned consumer, rather than copying a matching-looking string into a fixture. |

The receipt must preserve the actual Reader plan binding, source revision,
Reader-result identities, authenticated envelope and expression binding, plus
the owner-metadata content ID and projection contract. `00740a` checks their
shape, selected source-contract commitments and digest coherence; its helpers
explicitly do not authenticate a supplied report. Fresh authority comes from
the lexical public route's actual owner invocation. Header parsing and these
structural checks remain separate from genuine source-bound acceptance.

Required negatives at the actual consumer are mixed source-interface/artifact
versions; altered semantic family; crossed Core/context/receipt/binding tuples;
and changed Reader-result, Reader-source or expression-binding commitments.
Retain owning-stage rejection diagnostics, including early version-dispatch
rejections separately from `C11-MODULE/:current-core-source-policy-data` failures
where that policy check is reached. A coherent recomputed digest alone does not
establish fresh owner authentication. Existing header/class/metadata controls
are prerequisites, not execution of those genuine artifact substitutions.

Genuine v3 construction, separately fresh public Core verification using actual
policy state, and actual versioned C11/Native/Gravity positives and negatives
remain unaccepted. This field mapping grants no source execution, final
admission, OWN-06 closure or broader OWN credit; existing runtime holds and
accepted OWN-04 development evidence remain unchanged.

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
