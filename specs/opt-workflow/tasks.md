# Tasks: Opt implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. The linked component roadmap is navigation to this ledger.
Historical migration checkboxes below are separate from implementation
completion. Existing admission rules remain in force.

<!-- opt-implementation-tasks:start -->

- [ ] `opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts` opt/1: Execute pass contracts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Validate required/preserved/invalidated facts and deterministic ordering, then run actual constant/branch/dead-code/common-expression transformations.


<!-- opt-canonical-contracts:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 pass-contract criterion (Open): Execute only actual verified target-independent MIR transformations under a registry contract identifying pass, input/output IR, required analyses, preserved/invalidated/regenerated facts, proof obligations, profile constraints, target assumptions and emitted products. Check the contract before execution and after actual output. Preserve checked semantics, effects, capabilities, ownership, safety outcomes, source/generated maps, profiles and proof obligations without importing backend undefined behavior. Required outputs: actual pass registry, checked contracts, input/output identities and positive/refused contract fixtures. Reject missing required analysis, invalid contracts, unsupported profile/target assumptions, false preservation and changed/absent required facts with C13-CONTRACT/PRESERVE and contextual diagnostics. The sample bounds-check contract illustrates named dominator/range/safety inputs, dominator/data-flow invalidation, runtime-check regeneration and a dominating proof; its pass/profile values are examples, not universal pass eligibility or a verification result.
<!-- opt-canonical-contracts:end -->

- [ ] `opt/current-evidence-and-remaining-work/opt2-implement-structural-families` opt/2: Implement structural families

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement inlining/specialization, closure/allocation/region/resource transformations, dispatch/loop/vectorization preparation and effect-safe scheduling with explicit decisions.


<!-- opt-canonical-families:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 pass-family/legal-variant criterion compiler/c13/legal-variant-selection (Open): Implement constant folding/propagation, dead-code and common-subexpression elimination, inlining/specialization/partial evaluation, closure preparation, allocation sinking/escape-based stack-or-region placement, region/arena changes, branch/match decisions, protocol/generic dispatch, loop canonicalization/unrolling, vectorization preparation, effect-aware scheduling, resource cleanup and target-independent layout preparation, with real decisions and preserved semantics. Check retention/elision and domain entry/exit/EFIR fusion/math-provider changes consume the separate governing proof and anchor contracts; target-specific instruction selection remains Lower's responsibility. Rankings/benchmarks choose only among correct legal variants and never supply correctness evidence. Removing project accounting creates no zero-cost/performance claim; retain the existing actual PERF2 equivalent-form/residual-work evidence requirement. Required outputs: actual family transformations, accepted/refused legality fixtures and source-mapped decision/variant records, including a correct slower candidate when required by safety/determinism/portability. Reject invalid or uncertified variants before measurement, illegal effect reorder and backend-UB assumptions; the existing PERF1/6/7 criteria remain distinct and unchanged.

Ownership clarification: consume the actual constant-fold/branch/dead-code/common-expression implementations owned by opt1-execute-pass-contracts. This task owns full family coverage and the remaining structural transforms; do not create a duplicate basic-pass implementation or a second task authority.
<!-- opt-canonical-families:end -->


<!-- opt-protocol-source-specialize:start -->

Apply the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 proved dispatch-specialization criterion (Open): Perform actual legal dispatch specialization using current implementation/type/profile/target facts and surviving governing proof. Preserve source diagnostics and fallback behavior, or prove that fallback unreachable for the exact subject before erasing it; refusal to transform preserves the original legal dispatch. Reject speculative closed-world assumptions, stale/missing proof, hidden effects/capabilities/unsafe checks or changed fallback/diagnostics. Preserve actual before/after identities, proof/checker assumptions, residual/fallback paths, source/generated mappings and affected fact invalidation/regeneration through verification. Benchmarks or a direct-call label do not prove semantic equivalence. Required evidence: specialized execution/artifact comparisons and negative proof/fallback/diagnostic/staleness fixtures under L8 and C13.
<!-- opt-protocol-source-specialize:end -->


<!-- performance-contract-source-specialization-keys-guards-and-maps:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract specialization-keys-guards-and-maps criterion (Open). Sources: [PERF3](../../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md).

Implement actual type/const/literal/shape/schema/protocol/numeric/profile/target-feature/layout/provider/effect/proof specialization. Keys include every behavior-affecting fact and dependency; guards preserve generic behavior for every admitted input and run at the declared compile/link/dispatch/runtime stage before narrowed use. Emit genuine generic-to-specialized source/generated maps, variants/profile/target/effects/grants/proof state and selection decisions. Exercise valid selected specializations and missing/unstable key, late/overbroad guard, lost source map, illegal variant or false equivalence refusals; runtime guard cost remains visible unless truly erased/equivalent. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-specialization-keys-guards-and-maps:end -->



<!-- performance-contract-source-partial-evaluation-and-invalidation:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract partial-evaluation-and-invalidation criterion (Open). Sources: [PERF3](../../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md), [D6](../../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md).

Partially evaluate actual pure or explicitly granted declared build-effect computation under the human L12 replayable-input policy, preserving separate pure-constant rules and genuine source/input/output provenance. Consume authentic source/macro/type/const/profile/target/features/layout/provider/numeric/build-effect/replay/proof/dependency invalidation products before reuse. Exercise actual eligible results and undeclared/ungranted/nonreplayable inputs, changed subjects and stale variants; cached records, fixed source flags or provider labels do not establish execution, replayability or unchanged behavior. Build owns graph inputs/cache enforcement rather than a duplicate optimizer cache authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-partial-evaluation-and-invalidation:end -->



<!-- performance-contract-source-internal-layout-transformations:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract internal-layout-transformations criterion (Open). Sources: [PERF4](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md).

Implement actual field reorder/pack/padding, AoS/SoA, hot/cold split, inline/unboxed/tagged representation, cache-line/device packing/hardware-layout preparation only with authentic alias/ownership/address-identity, initialization and boundary facts. Preserve typed source field behavior, numeric/resource semantics and source/debug maps; layout-derived check erasure uses actual proof. Exercise real selected transformations and observable addresses, retained FFI pointers, unsafe offsets, absent serializer adapters or illegal boundary changes. Lower owns actual target ABI/layout products; no internal layout claim grants public repacking. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-internal-layout-transformations:end -->



<!-- performance-contract-source-pgo-identity-staleness-and-privacy:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract pgo-identity-staleness-and-privacy criterion (Open). Sources: [PERF6](../../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md).

Consume real profile-data schema and source/typed artifact/MIR/compiler/profile/target/provider/workload identities with declared privacy/redaction/aggregation/retention/access policy. Preserve accepted/advisory/stale/rejected/required-missing states and exact-source/exact-MIR/advisory-similar/require-exact/ignore policy meanings; release defaults reject mismatched source or MIR unless policy explicitly accepts advisory data. Exercise actual selected identity/status/workload changes and raw secret/user/prompt/tool/customer data leakage. Anonymous blobs or an accepted flag do not establish matching current data or policy authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-pgo-identity-staleness-and-privacy:end -->



<!-- performance-contract-source-pgo-transformations-and-reproduction:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract pgo-transformations-and-reproduction criterion (Open). Sources: [PERF6](../../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md).

Use genuine function/branch/path/allocation/type/protocol/shape/cache/lock/error/provider-frequency data to guide selected inlining, hot/cold/block/layout/specialization/variant/branch/dispatch/allocation/error-path decisions. Each actual transformation logs profile data, pass, expected benefit, preserved facts, invalidated proofs and outputs, preserves all semantic/safety/secret/profile constraints and is reproducible from exact data/compiler inputs. Exercise actual decisions and stale reuse, unlogged decisions, false evidence and nonreproducibility; measurements guide a lawful transformation but prove no semantic equivalence. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-pgo-transformations-and-reproduction:end -->



<!-- performance-contract-source-autotuning-candidates-and-selection:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract autotuning-candidates-and-selection criterion (Open). Sources: [PERF7](../../docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md).

Declare candidate space/objective/constraints before real expansion; reject candidates lacking required type/effect/capability/profile/safety/math evidence before benchmarking. Compare actual admitted variants and bind selected candidate, target fingerprint, benchmark/constraint/certificate evidence, rejected alternatives and selection reason. Preserve source/MIR/compiler/backend/provider, benchmark inputs and random seeds where used; release uses genuine pinned or policy-permitted reproduced tuning. Exercise selected valid/slower lawful choices and invalid-before-measurement, absent comparison/certificate or unpinnable/nonreproducible selection refusals. A selection certificate name is no checked result or tuning run. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-autotuning-candidates-and-selection:end -->



<!-- performance-contract-source-simd-lanes-bounds-and-math:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract simd-lanes-bounds-and-math criterion (Open). Sources: [PERF8](../../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md).

Implement actual selected vectorization with lane independence, legal aliasing, valid element size/lane bounds, alignment or supported safe unaligned access, target features, tail and mask semantics. Preserve loop dependencies/effects and explicit reduction associativity/numeric mode; strict floating reductions cannot reassociate without declared relaxation. Elementary-function vectorization needs matching certificates unless exactly equivalent in the selected mode. Exercise actual scalar/vector comparisons, all applicable tail forms and missing lane/alias/bounds/alignment/math proof, overlapping mutable slices, strict-mode or unsupported intrinsic cases; absent proof rejects or retains a profile-allowed genuine runtime guard. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-simd-lanes-bounds-and-math:end -->



<!-- performance-contract-source-cache-order-and-intrinsic-products:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract cache-order-and-intrinsic-products criterion (Open). Sources: [PERF4](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md), [PERF8](../../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md).

Produce genuine tiling/interchange/blocking/prefetch/hot-cold/layout/stream-store/alignment decisions and cache-shape evidence naming access/working set/stride/hotness/cache-line/prefetch/target/benchmark facts. Preserve volatile/MMIO/atomics/synchronization/capability/effect ordering and all source iteration behavior. Target intrinsic maps consume actual Lower/Backend operation/instruction/feature/numeric/alignment/order/fallback/proof products; unsupported no-fallback variants are guarded from execution. Exercise selected legal locality/intrinsic composition and effect reorder, wrong target, missing fallback, public layout mutation and unsupported cache claims. Target-independent Opt preparation is distinct from actual instruction emission. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-cache-order-and-intrinsic-products:end -->


- [ ] `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` opt/3: Protect checks and anchors

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Require governing proof for check elision and domain/math transformations; retain checks or reject unsupported transformations and preserve anchors/source provenance.


<!-- opt-canonical-checks:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 proof/check/anchor criterion (Open): Preserve bounds, overflow, null, initialization and lifetime checks unless actual applicable PERF10 evidence justifies their elision for the exact subject with required dominance and surviving assumptions. Domain entry/exit, EFIR fusion and provider selection preserve typed-core/MIR anchors and governing MATH10/SAFE15 semantics/certificates. Consume actual domain/math/proof-checker results; a proof name or target metadata is not verification. Regenerate invalidated type/effect/ownership/capability/safety/math/profile facts and evidence, keep lawful residual checks or reject the transformation. Required outputs: proof/certificate-use and check-elision/retention records, residual checks, actual accepted/rejected missing/non-dominating/stale-proof and invalid-anchor fixtures, and preserved source/generated origins. Reject proofless check erasure, stale safety outcomes, invalid domain transformations and illegal effect reorder with C13-PROOF/CHECK-ELISION/SAFETY/DOMAIN/EFFECT; no required policy or authority check disappears for speed.
<!-- opt-canonical-checks:end -->


<!-- opt-library-source-preservation:start -->

Apply the [complete library-principles source criteria](../stdlib-workflow/plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 library numeric and safe-API preservation criterion (Open): Preserve actual declared public numeric width/overflow/floating/vector/rounding/NaN/infinity mode and library error/resource/wrapper/authority semantics through each selected optimization, specialization and inline/lowering path. Explicitly relaxed source modes permit only their declared variation; host defaults, normal-result equality, metadata retention or structural validity cannot justify lost failures, checks, cleanup, diagnostics or audit. Obtain the governing exact-subject proof/certificate/validation before dependent use, regenerate affected owner facts and verify the changed subject. Required evidence: actual before/after accepted/refused mode, check, resource and wrapper products plus preserved source/profile/target identity. Valid unoptimized programs remain usable; no universal optimizer or full future Formal prerequisite is introduced.
<!-- opt-library-source-preservation:end -->


<!-- library-base-source-mode-preservation:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation mode-preservation criterion (Open): Preserve declared width/signedness, overflow, floating/vector/rounding/exceptional-value/domain and error semantics through actual library specialization/inlining/optimization/lowering. A faster mode requires explicit source relaxation or governing current proof; checks cannot become unchecked target behavior by default. Exact dominating proof must remain valid after transformations, and invalidation needs repair/rechecking, check restoration or rejection. Evidence: real original/transformed comparisons, selected independent certificate/check results and negative mode/target/proof cases; no blanket fast-math, host default or source metadata proves execution.
<!-- library-base-source-mode-preservation:end -->



<!-- opt-alternative-types-source-refinement:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual refinement-proof consumption criterion (Open): Use alternative type facts only when produced by the actual selected provider, valid for the active profile/target, serialized in common typed-Core proof/refinement facts or a proof artifact, stable under the selected transformation and rechecked or invalidated with changed source/provider/profile/target/macro/facet/grant inputs. When erasing a runtime check, retain the actual proof reference that justifies that exact removal and the governing checked preservation evidence under D9/SAFE15/PERF10/C18. Unchecked casts, assigned profile-safe flags, structural validity or a provider manifest are not safe proof. Reject stale/wrong-subject/unselected/incompatible facts and evidence-erasing transforms. Required evidence: actual selected before/after check/fact/proof/diagnostic products and erased-check/proof-link refusals; valid unoptimized programs need no universal optimizer or whole future Formal gate.
<!-- opt-alternative-types-source-refinement:end -->



<!-- performance-contract-source-check-class-proof-and-policy:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract check-class-proof-and-policy criterion (Open). Sources: [PERF10](../../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md), [D8](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md), [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md).

Apply every canonical PERF10 elision-table row at its actual operation: bounds, overflow/explicit wrap-saturate, nonzero division, shift width, null/option match, initialization, region escape, borrow/alias, linear terminal flow, race, taint sink and capability/effect/unsafe-audit/replay/AI-review policy. Require the exact governing proof/operator/match/validation/replay/policy or workflow-gate artifact, current dominance and preserved assumptions. Emit actual erased/residual checks, proof/pass/profile/target/invalidation and source/generated context. Policy rows discharge only through equivalent policy evidence, never speed; partial proof keeps genuine narrower residual checks. Exercise each claimed positive and missing/non-dominating/false/stale proof, absent residual or policy bypass refusal; source flags and proof ids alone prove nothing. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-check-class-proof-and-policy:end -->



<!-- performance-contract-source-performance-preservation-of-latency:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract performance-preservation-of-latency criterion (Open). Sources: [PERF9](../../docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md), [PERF1](../../docs/phase-04-performance-model/059-perf1-performance-model-specification.md).

Preserve actual required latency, jitter, bounded allocation/blocking/runtime checks and target/preemption assumptions through optimization; reject average-speed wins introducing unpredictable/unbounded tail behavior. Keep genuine conditional bounded providers or isolated effects under the governing profile rather than declaring all IO universally forbidden. Exercise selected original/transformed deterministic paths and hidden runtime/check/dispatch cost, changed physical assumptions or lost bound evidence. Required latency failure rejects the build; optimization selection cannot downgrade the source contract. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-performance-preservation-of-latency:end -->


- [ ] `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate` opt/4: Verify and regenerate

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Actually verify mutated MIR, regenerate invalidated analyses or keep conservative checks, and translation-validate or prove high-risk changes.


<!-- opt-canonical-verification:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 post-pass/invalidation criterion compiler/c13/post-pass-verification (Open): Actually run the MIR verifier after every pass changing control flow, data flow, effects, safety facts, ownership facts or domain anchors, and gate exposed output on the actual current report. Structural MIR verification is distinct from high-risk semantic evidence: attach and identify applicable local proof, translation validation, differential fixture, certificate checker, domain verifier or conformance acceptance only for nonsemantic differences. Required outputs: execution-derived verifier reports, checked evidence references and a conservative invalidation ledger naming invalidated analyses/facts, required rerun passes, invalid proofs/certificates, restored runtime checks, cleared caches and affected diagnostics, plus actual regeneration results. If continued fact validity cannot be proved, invalidate it. Positive fixtures verify each mutation family and lawful regeneration/residual path; reject verifier failure, stale or fabricated success, missing invalidation/regeneration and absent required high-risk evidence with C13-VERIFY/INVALIDATE/PRESERVE/PROOF. Actual MIR verifiers and proof/translation/domain checkers retain their own implementations; decision metadata cannot stand in for execution.
Current C18 application: consume the actual pass risk/evidence policy and checker products from Compiler API's existing pass-evidence owner. For behavior-relevant MIR changes retain checked translation validation per affected function with exact input/output/function/result/effect/safety identities. Stronger mechanized pass proof is optional unless applicable policy requires it and cannot waive checked matrix minimum evidence; a mechanized method must establish the required affected-function validation rather than merely name a proof. Generic differential fixtures, structural success or nonsemantic conformance cannot replace that scope. Failed validation retains the prior valid artifact and counterexample evidence. This task owns invocation and current applicability, not another proof checker or policy authority.
<!-- opt-canonical-verification:end -->


<!-- opt-alternative-types-source-regeneration:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual alternative-fact regeneration criterion (Open): Expose actual invalidation dependencies for changed source, provider, profile, target, macro, facet and grants, requiring real affected owner/type/refinement/ownership/effect/domain rechecking or invalidation before a transformed subject is consumed. Preserve check-erasure proof references and recompute their exact applicability rather than copying complete/safe/preserved records. Early actual checker/property interfaces precede work and actual new facts/verifier results follow; no whole alternative theory or future final release is the first regeneration service. Reject stale serialized proof/type facts and mismatched changed subjects. Required evidence: real unchanged/changed-input fact lifecycle, actual owner regeneration and changed-subject refusal/verification records for all seven governing trigger categories.
<!-- opt-alternative-types-source-regeneration:end -->



<!-- performance-contract-source-proof-invalidation-and-current-verification:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract proof-invalidation-and-current-verification criterion (Open). Sources: [PERF10](../../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md), [PERF1](../../docs/phase-04-performance-model/059-perf1-performance-model-specification.md), [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md).

Reassess actual proof validity after control/data flow, layout, numeric, ownership/alias/profile/target/provider/backend assumption changes. Regenerate and check the required actual proof/facts, keep lawful residual checks or reject the no-check compilation mode; record invalidating pass and current verifier/counterexample products. Preserve the existing C18 affected-function translation-validation minimum and scoped checker owners; a stronger method is valid only when it establishes required scope. Exercise actual changed subjects and failed regeneration, reused wrong-subject proof, fabricated verifier success or missing source map; valid unoptimized MIR remains usable and no universal Formal completion gate is added. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-proof-invalidation-and-current-verification:end -->


- [ ] `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline` opt/5: Integrate reproducible pipeline

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record source/compiler/pass identities, no-change decisions, residual work and replay inputs; demonstrate changed semantics rejection and unchanged semantics across Opt -> Lower.


<!-- opt-canonical-pipeline:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 reproducibility/decision criterion compiler/c13/reproducible-pass-order compiler/c13/optimization-decision (Open): Choose deterministic pass order for the actual source/compiler/profile/target/features/package graph/build inputs and record pass order/versions, optimization level, source hashes, profile/target/features, provider set, random seeds when used, benchmark/autotuning inputs and proof artifacts. Nondeterministic search emits an actual replay record or rejects in hermetic builds; hidden ordering is refused. Every executed pass, including no-change, emits a stable explanatory decision binding pass/decision IDs, input/output MIR hashes, changed operations, reason, preserved/invalidated facts, used proofs, residual checks, benchmark references and actual verifier result. Size-threshold and passed example values do not impose project quotas or fabricate verification. Required outputs: deterministic pipeline manifest, decision log, analysis-cache and evidence-use records, verifier/invalidation products and actual changed/unchanged/replay/refusal/Opt-to-Lower consumer fixtures usable by diagnostics/conformance/self-hosting comparison. Reject unexplained source-visible changes, missing no-change records, nondeterministic/unreplayable inputs or false evidence with all applicable C13 diagnostics. Retain C13-CONTRACT/PRESERVE/INVALIDATE/PROOF/CHECK-ELISION/EFFECT/SAFETY/DOMAIN/NONDETERMINISM/VERIFY meanings and pass/decision/input-output-MIR/source-span-when-available/changed-ops/missing-fact/proof/profile/target/remediation context. These are future execution requirements, not present optimizer/verifier/proof/runtime/probe results or independent acceptance.
<!-- opt-canonical-pipeline:end -->



<!-- performance-contract-source-complete-claim-and-evidence:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract complete-claim-and-evidence criterion (Open). Sources: [PERF1](../../docs/phase-04-performance-model/059-perf1-performance-model-specification.md), [D6](../../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md), [D8](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md), [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md).

Emit actual performance claim/contract, decisions and products naming source/compiler/backend/runtime/provider, profile/target/features, safety mode, input domain/distribution/shape/layout, objective, erased checks, proof assumptions, matching benchmark/harness and target fingerprint. Preserve source semantics, numeric/taint/secret/unsafe-audit, type/effect/ownership/capability/init/bounds/math/profile facts. Exercise genuine selected complete claims and missing context, hidden effects/authority/host work, unsafe shortcut or unsupported evidence refusals. Slower lawful code remains valid; benchmark/cost-model evidence cannot prove correctness or discharge a safety proof. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-complete-claim-and-evidence:end -->



<!-- performance-contract-source-zero-cost-comparison:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract zero-cost-comparison criterion (Open). Sources: [PERF2](../../docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md), [PERF1](../../docs/phase-04-performance-model/059-perf1-performance-model-specification.md).

For protocols/interfaces/generics/records/newtypes/iterators/collection views/patterns/macros/facets/higher-order/error/resource/numeric/library wrappers, identify the genuinely equivalent handwritten lower-level form and expected erased versus actual residual work. Compare actual before/after artifacts and publish specialization/dispatch behavior, allocation/boxing/alias/check/FFI/host/synchronization/closure/guard costs and proof/target/profile assumptions. Exercise genuine selected erasure and accurate residual reports; reject zero-cost based on source shape, small overhead, hidden residual operations or profile-illegal behavior. Work shared by the equivalent baseline may remain, but must be demonstrated, not asserted. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-zero-cost-comparison:end -->



<!-- performance-contract-source-variant-guards-fallback-and-overhead:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract variant-guards-fallback-and-overhead criterion (Open). Sources: [PERF3](../../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md), [PERF7](../../docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md).

Emit and validate complete deterministic target/device/input-shape/alignment/domain/error/layout/provider/profile/PGO guards under the selected variant policy, with source maps and compatibility metadata. Refuse missing/unsafe/ambiguous overlapping guards and unsupported execution; if none matches, select a genuinely valid baseline. Measure/model actual runtime guard/branch/indirection/cache/code-size/warmup overhead or record genuine static dispatch erasure. Exercise matched/unmatched/overlap/fallback and wrong-target cases; do not call dynamic dispatch zero-cost or hide its real work behind a manifest. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-variant-guards-fallback-and-overhead:end -->

<!-- opt-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Opt workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Opt canonical remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `opt-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
