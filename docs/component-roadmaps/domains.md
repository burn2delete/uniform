# Domain Projects

Working registry name: **Domains** (proposed). Semantic key: `domains`.
This page is a registry of independent domain component projects, not one giant
compiler pass or one product feature. All names below are working defaults.
Owner role: domain architecture maintainer plus the named specialist role below;
current owners unassigned by this proposal. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit domain ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/domains_task_projection.clj --write`;
the sixteen outcome/task/acceptance blocks below are generated. The original
seven project keys retain their positions; nine additional records are Open
integration tasks within Domains, with no new completed-component claim.

## Purpose and scope

Preserve domain semantics in explicit IRs and artifacts anchored to typed core
or MIR. [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
defines the boundaries. Domains do not bypass type/effect/profile/authority
checking. Full hardware/OS work belongs to [Platform](platform.md), not here.
The [domain coverage contracts](../phase-09-domain-specific-computing-coverage/README.md)
also describe application sectors (mobile, services, games, security and others):
those are integration claims over component outcomes, not automatically new
compiler components or completed products.

## Release outcome

Each project below emits its declared executable or consumable artifact,
preserves its typed/MIR anchor and provenance, and demonstrates its own positive
and negative acceptance cases. The aggregate outcome is open and means all
listed outcomes, not a metadata manifest or a single generic domain fixture.
No assertion of full application-sector replacement follows.

## Common interfaces and evidence boundary

Inputs: typed core or MIR, source origins, profile/effect/capability facts,
domain declarations and selected target policy. Outputs: the per-project IR,
artifacts, diagnostics and evidence listed below. MIR and semantic facts are
integration prerequisites; artifact schemas, reference interpreters, fixtures
and target contracts can develop in parallel. Build packages outputs later and
is not an implementation prerequisite.

The existing [schema report](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00566-stage0-compiled-plan-schema-report.clj)
and [workflow backend source](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
are source evidence to reconcile, not proof that all domain operations execute.
No compiler tests or domain execution were performed for this document.

## Math

Key `math`; owner role: numeric/proof maintainer. Purpose: preserve numeric
modes, domains and approximation guarantees. Scope: numeric semantics, EFIR,
EML proof/search attachment and certified approximations; not arbitrary
mathematical equality by tree identity. Input: typed numeric expressions and
mode/domain requirements. Output: EFIR, checked proof/certificate references,
selected implementation and diagnostic records.

<!-- domains-task-projection/math:start -->
Outcome: [math contracts](../phase-05-mathematical-and-elementary-function-system/README.md) execute with the declared
numeric semantics and checked justification for transformations. Remaining
finite tasks, all open: implement numeric-mode/domain checking; implement EFIR
lowering and evaluation; connect EML/certificate checking to justified rewrites;
implement certified approximation/interval and symbolic operations; run the
contract conformance matrix with rejected unproved transformations.

Yes/no acceptance: do domain and branch errors reject? Do proof-dependent
rewrites require checked evidence? Do strict-mode results match the selected
semantics? Integration: Types/MIR to EFIR, Opt proof preservation, Lower/Stdlib
numeric APIs. Decision owner: numeric maintainer; recommendation: keep EML as
adjacent proof/search representation, never ordinary runtime equality authority.

SAFE9 acceptance: execute checked/panic/wrapping/saturating/arbitrary-precision/proof-required integer modes, exceptional divisor/shift/minimum-negation cases and explicit float conversion behavior; reject implicit narrowing, hidden target defaults and unapproved relaxed math. Inspect strict/relaxed floating contracts including NaN, infinity, signed zero, rounding and fusion; elementary providers expose domain/range/accuracy/target evidence. Invalidated numeric proofs restore checks or reprove; target instructions preserve mode or are guarded/rejected. Explicit wrapping/saturating narrowing retains the source conflict with checked/proven narrowing until adjudicated. P2/P12 profile interface: check portable/core and exact/symbolic/interval/bitvector/rounded/certified mode eligibility and explicit conversions/assumptions; reject hidden target-default or provider-dependent core math. Formal symbolic lowering emits symbolic IR, theorem/assumption/trusted-kernel records and checked conclusions; imported proof validation and actual arithmetic remain distinct interfaces. Dependencies consume Types/Legality facts and declared proof-provider outputs, not completion of another component.

Foundation requirement gravity/approximation-certificate-checkability: Produce and independently check an approximation certificate bound to the exact EFIR expression, domain, error measure/bound, rounding, branch policy, target and checker assumptions; preserve its checked result through the selected consumer. Refusals: Reject a changed domain, error bound, rounding mode, branch, target, checker or expression; a named checker and benchmark agreement alone cannot establish checkability. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs.

Foundation requirement gravity/elementary-math-semantic-carrier: Lower analyzable elementary expressions into EFIR with domain/numeric-mode/source anchors, use EML only for declared proof/normalization/search work, and retain checked evidence for any semantic rewrite. Refusals: Reject declaring expressions equal solely because their EML trees or spellings match, dropping EFIR analysis facts or replacing strict semantics with an unproved approximate provider. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts; pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

Criterion profile-gap/formal/numeric-semantics (Open): Input: Formal numeric expressions with exact integer/rational/real, bitvector, symbolic, interval, correctly rounded or certified mode and explicit conversion/assumption/provider data. Accept: Admit only declared formal math modes; verify explicit conversions and assumptions and preserve rounding/precision/certificate facts through symbolic or target lowering. Policy cases do not imply that all arithmetic providers are implemented. Reject: Relaxed or target-default math, hidden conversion assumptions, missing proof-mode or certificate, and incompatible backend numeric assumptions. Required outputs: Formal numeric/mode/rounding records, conversion and assumption checks, accepted/rejected P12-MATH-MODE/P12-ASSUMPTION/P12-BACKEND fixtures with theorem/proof/provider/kernel/span/target context. Dependencies: types/current-evidence-and-remaining-work/types2-check-declared-abstractions, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping formal/numeric-semantics; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/formal/profile-proof-contract (Open): Input: Formal source and theorem/refinement/pre-postcondition claims with quantified inputs or explicit assumptions, trust roots, proof provider/kernel and selected profile/target. Accept: Check deterministic source admission; lower eligible source to symbolic IR; produce proof objects/theorem summaries and explicit assumptions/trusted-kernel/input hashes/certificate chain/conclusion; validate imported source digest/schema/trust/assumptions/theorem/profile/target/hash-chain before policy-permitted use. Reject: Runtime effects or unmodeled nondeterminism/failure, unsafe/raw FFI, hidden target assumptions, untrusted provider, theorem mismatch, failed proof or incompatible imported certificate. Unverified imports follow the declared ignore-or-reject policy, never silent proof acceptance. Required outputs: Symbolic IR, proof object, assumption and trusted-kernel manifests, checked theorem/certificate chain/import verification, positive/negative source/proof/import cases and origin-rich formal diagnostics. Dependencies: legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping formal/profile-proof-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/math/numeric-profile-eligibility (Open): Input: Numeric family/mode/conversion/provider request with active profile/target, allocation support, precision/rounding/NaN/branch/unit and proof facts. Accept: Emit and check the concrete profile support matrix for fixed/arbitrary integers, rationals, proof reals, floats, complex, intervals, symbolic and enabled units. Keep numeric families explicit in types/EFIR/runtime artifacts; admit explicit exact/checked/rounded/saturating/wrapping/proof/unsafe conversion classes only under their actual declared legality; preserve domain/codomain/precision/branch/source annotations. Symbolic equality requires proof rather than tree shape. Reject: Unavailable family/mode/provider, hidden allocation in constrained/core contexts, implicit narrowing/truncation/overflow/precision or rounding/NaN-policy loss, ambiguous branch/unit conversion and symbolic equality without valid evidence. Keep independent source conflicts on particular conversion modes open rather than choosing an undocumented policy. Required outputs: Numeric-kind lattice, conversion-rule table, machine-readable support matrix/mode/provider records, EFIR annotations and conformance cases for every supported conversion/family; MATH1-FAMILY/CONVERSION/NARROW/PRECISION/ROUNDING/BRANCH/ALLOCATION/EQUALITY/PROFILE diagnostics with span/family/mode/profile/target/provider/proof. Dependencies: types/current-evidence-and-remaining-work/types2-check-declared-abstractions, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping math/numeric-profile-eligibility; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

Math source-specific implementation criteria (proposed adoption, all Open).
These stable criterion IDs refine the existing literal `math` task; they do not add
project tasks or mark any implementation accepted. Each dependency names an input
interface and can be satisfied by its exact available artifact, not completion of
the whole component. Each declared supported case needs actual positive and negative
execution evidence before closure; unavailable combinations remain explicit.

Current AGENTS accounting-removal policy governs historical fuel/depth/node/time/candidate-count passages: no project work allowance or otherwise-valid-input quota is an admission requirement. Preserve deterministic choices, semantic legality, complete proof/partition/trace evidence and truthful unresolved outcomes. Declared runtime performance objectives, error bounds, target representability and actual host restrictions remain distinct; no replacement work allowance is proposed.

Criterion `math/approximation-certificate-production` (MATH5; Open).
Implement: Produce a content-identified approximation certificate and independent-checker transcript/proof references bound to its exact EFIR implementation contract.
Boundary: Each supported approximation family/domain/mode/precision/target; approximation, coefficient, roundoff, reconstruction and target error ledgers remain acceptance facets.
Done when: For each declared polynomial, rational, piecewise, table, SIMD/GPU/hardware or fused family, parse the emitted certificate, recompute semantic identity, check domain coverage, separate approximation/coefficient/reduction/evaluation/reconstruction/roundoff/target error and combine within the precision contract.
Negative fixtures: Reject incomplete domain coverage, poles, branch/exception drift, relative-error-only claims near zero, unsupported target assumptions or a synthesizer attestation substituted for independent checking.
Outputs: Certificate, target-assumption manifest, exceptional-path coverage and independent checker transcript references.
Dependencies: math/efir-verification, math/interval-bound-ledger. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH5-CHECKER is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/conformance-execution-report` (MATH11; Open).
Implement: Run the math suite, actually parse/check emitted evidence, compare expected results/rejections and emit exact-source provenance-bearing results and failures.
Boundary: Independent certificate/trace replay, all applicable invalidation cases, profile/target matrix and minimized failures; artifact existence is insufficient.
Done when: Execute the declared available fixture matrix, parse and independently check every expected artifact, compare values and exact expected diagnostics, and bind compiler/provider/source/suite/oracle identities to each result.
Negative fixtures: Fail missing artifacts, wrong or missing negative diagnostics, stale source/domain/branch/mode/precision/target/provider/pass/lowering evidence, replay mismatches and unavailable combinations mislabeled pass.
Outputs: Per-fixture/profile/target results, checked artifact/replay transcripts, invalidation results and minimized failure records.
Dependencies: math/conformance-suite-composition, math/conformance-oracle-adapter. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH11-DIAGNOSTIC is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/conformance-oracle-adapter` (MATH11; Open).
Implement: Select and validate the fixture's reference oracle with recorded version, independence, inputs, error model and trust assumptions.
Boundary: A benchmark or target implementation cannot silently serve as an independent correctness oracle.
Done when: Bind each fixture to exact integer/rational, high-precision floating, interval, symbolic, imported proof, counterexample or named provider oracle with version, independence, input identity and error/trust policy.
Negative fixtures: Reject an unavailable or untrusted oracle, mismatched input/version/error model, or a benchmark or implementation-under-test used as an independent oracle.
Outputs: Versioned reference-oracle manifest and checked fixture input/error/trust mapping.
Dependencies: declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH11-ORACLE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/conformance-suite-composition` (MATH11; Open).
Implement: Compose the declared MATH1-MATH10 fixture/profile/target matrix with exact expected artifacts, diagnostics, checker/oracle and availability scope.
Boundary: Boundary/adversarial/random/generated-origin cases cover each required family; unavailable combinations are explicit, never implicit passes.
Done when: Enumerate every required family and declared supported combination, including boundaries, branch-sensitive, exceptional, random, adversarial and macro-generated origins, with expected outcome and explicit unsupported cases.
Negative fixtures: Reject omitted required families, fixtures lacking mode/domain/oracle or expected artifacts, hidden unsupported combinations and a positive-only matrix.
Outputs: Content-addressed fixture corpus and MATH1-MATH10 profile/target/artifact/diagnostic/oracle matrix.
Dependencies: declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH11-FIXTURE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/correctly-rounded-synthesis` (MATH10; Open).
Implement: Synthesize and check an implementation meeting exact accepted-output intervals for the requested representation and rounding modes, or reject infeasibility without silent downgrade.
Boundary: Tie/subnormal/zero/exception cells, coefficient/evaluation/FMA/intermediate/reduction/reconstruction/lowering constraints, and multi-format/multi-rounding intersection or separate certified kernels.
Done when: For each declared input/output representation and rounding mode prove every generated result lies in the exact accepted-result interval, including ties/subnormals/signed zero/exceptions and coefficient, evaluation, FMA, intermediate, reduction, reconstruction and backend assumptions; use a proved intersection or separately certified kernels.
Negative fixtures: Reject unresolved rounding cells, unsupported modes, unchecked/infeasible constraints and silent downgrade to faithful/libm/approximate behavior.
Outputs: Rounding-target manifest, accepted-result interval ledger, synthesis transcript, kernel digests and independent certificate.
Dependencies: math/efir-verification, math/numeric-mode-resolution, math/interval-bound-ledger. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-SYNTHESIS is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/efir-content-identity` (MATH3; Open).
Implement: Derive the same EFIR graph identity from the same semantic graph independently of incidental serialization order.
Boundary: Semantic content identity; not runtime provider identity or formatting identity.
Done when: Reorder incidental node/map serialization while preserving semantic graph fields and obtain the same graph identity; changing a consumed semantic field must invalidate it.
Negative fixtures: Reject a claimed identity reused across changed domain, branch, numeric mode, precision or semantic graph content; do not equate graph hash with runtime/provider identity.
Outputs: Semantic EFIR content identity and serialization-order equivalence fixtures.
Dependencies: declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH3-NODE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/efir-domain-inference` (MATH3; Open).
Implement: Infer domain and codomain facts for the EFIR subgraph.
Boundary: Supported real/complex/interval/symbolic/bitvector/floating/provider proof domains; branch-sensitive operations retain required branch policy.
Done when: Infer supported real, complex, interval, symbolic, bitvector, floating and provider proof domains from typed elementary inputs and retain compatible codomain and branch-sensitive facts.
Negative fixtures: Reject inconsistent domain/codomain facts, an omitted branch condition or unsupported provider-domain evidence rather than silently importing host semantics.
Outputs: Per-node domain/codomain environment with source and branch premises.
Dependencies: math/numeric-family-type-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH3-DOMAIN is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/efir-guarded-normalization` (MATH3; Open).
Implement: Rewrite within EFIR only under applicable semantic guards and proof obligations, recording preserved and invalidated facts.
Boundary: EFIR transform behavior; accepted equality requires the proof gate and approximate transformed nodes retain their certificate obligations.
Done when: Apply only applicable guarded rewrites, retain source/generated anchors, mode/domain/branch/precision facts, and accepted equality or explicit approximation obligations.
Negative fixtures: Reject an unproved equality rewrite, lost branch/source facts or erased certificate/check obligation.
Outputs: Transformed EFIR with rule/premise records, semantic anchors and preserved/invalidated facts.
Dependencies: math/efir-verification, math/semantic-equality-proof-gate. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH3-REWRITE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/efir-precision-attachment` (MATH3; Open).
Implement: Attach the resolved numeric mode and requested precision contract to EFIR nodes/graphs.
Boundary: Numeric-mode and precision facts are explicit inputs to later transforms and provider selection; their full resolver is owned by MATH7.
Done when: Attach the resolved compatible mode, result-quality fields, representation, exceptional and residual policy to each applicable EFIR node/graph.
Negative fixtures: Reject missing/impossible precision, conflicting per-operation modes and a target default replacing the resolved source contract.
Outputs: Per-operation and graph numeric-mode/precision annotations with inheritance origin.
Dependencies: math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH3-PRECISION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/efir-verification` (MATH3; Open).
Implement: Accept a well-formed/current EFIR graph or reject missing semantic/source/anchor/proof facts before verified consumption.
Boundary: Node shapes, domain/codomain/mode/precision/branch requirements, source/generated origin and typed-core/MIR anchors; invalid graphs do not become executable backend inputs.
Done when: Validate node/index/arity shape, domain, codomain, numeric mode, precision, required branch policy, source/generated origins, typed-core/MIR anchor and transformation/approximation obligations.
Negative fixtures: Reject missing or stale facts, unresolved links, unproved equality and bare EFIR offered as executable selected backend code.
Outputs: EFIR verifier report with node/source/semantic-anchor/proof diagnostics.
Dependencies: math/efir-domain-inference, math/efir-precision-attachment. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH3-SOURCE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/egraph-equality-saturation` (MATH9; Open).
Implement: Build and merge e-graph equality classes only under checked equality premises and conservative compatible analysis joins.
Boundary: Unproved edges cannot justify code, proofs, side conditions or further equality; conflict requires rejection/restriction/candidate status.
Done when: Merge only checked compatible equalities; join facts valid for selected members across domain/codomain/branch/mode/floating/exception/provider effects/checks/proof/origins, keeping discovery-only edges distinct.
Negative fixtures: Reject or restrict conflicting analysis joins; reject an unproved edge used for code, proof, a side condition or further accepted equality.
Outputs: Guarded equality edges, conservative e-class analyses and explanation report.
Dependencies: math/symbolic-rule-promotion, math/symbolic-guarded-rewrite. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-PROOF is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/egraph-extraction` (MATH9; Open).
Implement: Extract a legal representative using the declared versioned runtime cost model, deterministic ties and replayable selection explanation.
Boundary: Hidden semantic constraints or unproved edges cannot enter extraction; optimization fuel/stopping passages remain separately scoped.
Done when: Select a legal representative with explicit runtime/proof/stability/allocation/check/provider/code-size objective and deterministic ties; reconstruct its equality path and relevant rejected choices without rerunning saturation.
Negative fixtures: Reject extraction through unproved edges, hidden semantic constraints in heuristic weights, changed cost-model identity, nondeterministic ties or an unreplayable explanation.
Outputs: Extracted representative, versioned runtime-cost model, tie policy and independent explanation replay.
Dependencies: math/egraph-equality-saturation, math/symbolic-independent-trace-replay. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/elementary-registration` (MATH2; Open).
Implement: Validate an elementary declaration and produce its registry entry with a semantic EFIR anchor.
Boundary: Built-in and user-defined elementary functions; required fields define semantic domain/codomain/branch/exception/modes/form and provider requirements, not the literal sigmoid example.
Done when: Register built-in and user declarations with domain/codomain, semantic form, branch/exception policy, numeric modes and provider requirements bound to the semantic anchor.
Negative fixtures: Reject an incomplete declaration, missing branch/exception facts or a runtime library binding substituted for the semantic definition.
Outputs: Elementary registry entry, EFIR anchor and declaration validation report.
Dependencies: math/numeric-family-type-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH2-DECLARATION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/elementary-subgraph-detection` (MATH3; Open).
Implement: Detect elementary subgraphs in typed core for EFIR construction.
Boundary: Typed source graph detection, not evidence that synthesis/provider execution already succeeds.
Done when: Detect supported elementary operations from typed source graphs and preserve domain, branch, mode, source spans and generated origins for EFIR construction.
Negative fixtures: Reject lost anchors, untyped raw syntax offered as verified EFIR input or detection reported as provider execution/synthesis acceptance.
Outputs: Typed-core/MIR elementary subgraph report with source/generated-origin map.
Dependencies: math/elementary-registration. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-DETECT is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-candidate-promotion` (MATH4; Open).
Implement: Prevent lowering from using a candidate before accepted exact proof or accepted bounded-approximation evidence.
Boundary: Generated/typed/domain/branch/proof-requested/proved/bounded/rejected states; bounded candidates require the applicable MATH5 certificate, not just a state label.
Done when: Retain generated/typed/domain-checked/branch-checked/proof-requested/proved/bounded/rejected states; allow lowering only after the applicable independent proof or MATH5 certificate is accepted for the exact candidate.
Negative fixtures: Reject generated or merely labeled-proved candidates, stale evidence, missing bounded certificates and tree-identity equality.
Outputs: Candidate lifecycle decision with accepted exact-proof or bounded-certificate reference.
Dependencies: math/semantic-equality-proof-gate, math/efir-verification. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-CANDIDATE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-candidate-search` (MATH4; Open).
Implement: Return ranked search/normalization candidates with proof obligations and rejection reasons without making search output an accepted compiler rewrite.
Boundary: Same declared source/profile/target/provider/search inputs select the same accepted candidate or report a deterministic tie; search-stopping policy remains separately visible.
Done when: Given identical source/profile/target/provider/manifest inputs emit replayable candidate ordering and explicit obligations; discovery grants no rewrite authority.
Negative fixtures: Reject hidden search choices, domain/branch-invalid candidates promoted as accepted and nondeterministic selection without an explicit tie.
Outputs: Ranked candidates, outstanding proof obligations, deterministic ties and rejection reasons.
Dependencies: math/eml-construction, math/eml-search-manifest. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-SEARCH is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-construction` (MATH3; Open).
Implement: Lower verified EFIR into the optional EML representation while preserving source/node correspondence and semantic facts.
Boundary: Exp-minus-log basis with arithmetic/variables/constants/domain guards/branch facts; EML is optional proof/search input and never a mandatory runtime representation.
Done when: Lower verified EFIR through exp-minus-log basis with constants/variables/arithmetic/guards, preserving EFIR identity, source map, domain/codomain, mode, precision, branch choices and explicit complex intermediates.
Negative fixtures: Reject unverified input, lost source/node correspondence, missing branch assumptions or making EML the required runtime representation.
Outputs: Optional EML expression/DAG, EFIR node correspondence and semantic context.
Dependencies: math/efir-verification. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-EFIR is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-guarded-normalization` (MATH4; Open).
Implement: Normalize EML through guarded registered transformations while preserving the final domain, branch, numeric mode and pending proof obligations.
Boundary: Basis introduction, domain/branch transport and complex simplification; complex intermediates require permission, recorded choices, valid final domain and applicable proof/error evidence.
Done when: Introduce the basis, transport domains/branches and simplify complex intermediates only with permission, recorded choices and applicable final-domain/equality/error evidence.
Negative fixtures: Reject log-exp identities outside their domain/periodic guards, forbidden complex intermediates or dropped numeric/precision/proof facts.
Outputs: Guarded normalized candidate with transported domain/branch facts and pending obligations.
Dependencies: math/eml-construction, math/symbolic-rule-promotion. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-BRANCH is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-independent-trace-replay` (MATH4; Open).
Implement: Independently and deterministically check a recorded EML normalization trace.
Boundary: Replay of recorded rules/premises/choices; rerunning the heuristic search is not the checker.
Done when: Replay each recorded rule, premise, assumption, choice and before/after identity using the independent checker; reproduce preserved and invalidated facts without rerunning heuristic search.
Negative fixtures: Reject missing/reordered/substituted steps, changed rule/provider/target facts, unsupported assumptions or optimizer self-attestation.
Outputs: Independent deterministic trace-check report bound to exact input and rule versions.
Dependencies: math/eml-trace-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-search-manifest` (MATH4; Open).
Implement: Emit the declared search-space/objective/ranking/pruning inputs into the build artifact when search affects generated code.
Boundary: Grammar/domain/provider/target/source/profile inputs and tie policy; fuel/depth/time accounting passages remain individually unresolved against AGENTS.
Done when: Record every semantic and heuristic input affecting generated code, including source/profile/target/provider identities and deterministic ranking/ties, so accepted candidate choice can be reproduced.
Negative fixtures: Reject omitted objective/pruning/ranking inputs, hidden provider/target choice or a changed manifest reused for the same recorded decision.
Outputs: Build-bound search grammar/domain/objective/ranking/pruning/provider/target manifest.
Dependencies: math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-SEARCH is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-trace-production` (MATH4; Open).
Implement: Emit a normalization trace that identifies every applied rule, before/after form, premises, assumptions, origins, invalidations and proof obligation.
Boundary: Trace artifact-production contract; target/provider/heuristic choices that affect the run are recorded, not hidden in a process.
Done when: Emit each applied step and relevant heuristic/target/provider choice with exact EFIR/EML identity and generated origins, retaining pending obligations and branch/complex facts.
Negative fixtures: Reject missing rule versions, lost assumptions/origins, absent invalidations or a trace concealing an unproved replacement.
Outputs: Normalization trace with rule/premise/before-after/assumption/source/invalidation/proof entries.
Dependencies: math/eml-construction. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/floating-backend-eligibility` (MATH8; Open).
Implement: Map the floating manifest to target/backend features or choose an explicit legal fallback or rejection.
Boundary: Target instruction selection preserves mode/certificate assumptions and records unsupported semantics.
Done when: Map every floating manifest requirement to supported target format/rounding/exceptions/FMA/reassociation/denormal/status behavior and preserve certificate assumptions in lowering.
Negative fixtures: Reject unsupported rounding/format/features, silent flush-to-zero, status reordering or fallback with weaker source semantics.
Outputs: Backend instruction/provider mapping, feature assumptions and explicit legal fallback/rejection.
Dependencies: math/floating-policy-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH8-BACKEND is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/floating-policy-resolution` (MATH8; Open).
Implement: Resolve and emit a floating manifest and reject undeclared target-native format or behavior assumptions.
Boundary: Per-operation inherited or explicit policy; format encoding, precision/exponent/features and target availability are facets.
Done when: Resolve explicit or inherited format, exponent/significand/encoding, rounding, NaN/infinity/zero/denormal/status, FMA/reassociation/reciprocal and determinism policy for arithmetic and elementary operations.
Negative fixtures: Reject implicit target-native formats, missing NaN/zero/denormal/status behavior or unsupported representation declared portable.
Outputs: Floating manifest and format/rounding/exception/status/determinism tables.
Dependencies: math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH8-MANIFEST is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/floating-reproducibility-record` (MATH8; Open).
Implement: Emit a truthful floating reproducibility claim with the declared source/target/bitwise/provider level and required fixture coverage.
Boundary: A weaker scoped reproducibility result does not establish a stronger one or all targets.
Done when: Emit the exact claimed level, target/provider fingerprints and cross-target evidence required for stronger claims, including exceptional values and signed zero.
Negative fixtures: Reject a provider-scoped result presented as bitwise portability, absent target coverage, provider drift or weaker evidence promoted to a stronger claim.
Outputs: Scoped source/target/bitwise/provider reproducibility claim and supporting target fixtures.
Dependencies: math/floating-policy-resolution, math/conformance-oracle-adapter. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH8-BACKEND is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/floating-transform-legality` (MATH8; Open).
Implement: Permit floating reassociation, contraction/FMA and other semantic changes only under the explicit mode and required proof/certificate evidence.
Boundary: Strict versus relaxed transformations and observable status ordering; unproved fast-math is not a strict implementation.
Done when: Admit reassociation, contraction/FMA, reciprocal and replacement only under exact permitted relaxations or accepted preserving proof/bound, preserving observable status behavior.
Negative fixtures: Reject strict-mode unproved fast math, hidden flags, stale certificates and removed/reordered observable status effects.
Outputs: Transform decision with manifest, source span, proof/certificate/opt-in and selected instruction.
Dependencies: math/floating-policy-resolution, math/semantic-equality-proof-gate. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH8-FMA is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/fused-kernel-generation` (MATH10; Open).
Implement: Fuse elementary operations only with whole-expression domain/branch/numeric and combined-error evidence.
Boundary: Independent per-call certificates do not prove fused intermediate/roundoff behavior.
Done when: Generate a kernel for the verified combined graph, preserving intermediate exceptional behavior, precision, source diagnostics and combined roundoff/reconstruction obligations.
Negative fixtures: Reject fusion justified only by separate per-call certificates, incompatible branches or unapproved FMA/reassociation.
Outputs: Fused implementation candidate and whole-expression error/branch/domain evidence.
Dependencies: math/efir-verification, math/floating-transform-legality. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-FUSION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/implementation-candidate-generation` (MATH10; Open).
Implement: Generate executable elementary implementation candidates tied to verified EFIR and the requested semantic/target contract.
Boundary: Declared libm/intrinsic/symbolic/polynomial/rational/table/fused/vector/GPU/generated families; generation alone grants no acceptance.
Done when: Generate declared library, intrinsic, symbolic, polynomial, rational, table, piecewise, fused, vector/GPU, generated or residual-checked candidates while keeping each unaccepted until its obligations pass.
Negative fixtures: Reject missing semantic anchors, target/mode mismatch, hidden allocation/effects or generation mislabeled as proof/selection.
Outputs: Executable candidate set with EFIR/source/target/semantic contract and required proof references.
Dependencies: math/efir-verification, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-CANDIDATE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-bound-ledger` (MATH6; Open).
Implement: Produce separate real and roundoff bound ledgers whose entries expose their method, assumptions and invalidation conditions.
Boundary: Exact arithmetic/Taylor/minimax/Bernstein/monotonicity/Lipschitz/symbolic/external-prover/roundoff methods; changed consumed assumptions invalidate the entries.
Done when: Record exact interval, Taylor/minimax/Bernstein/monotonicity/Lipschitz/symbolic/external-proof or roundoff derivations and combine only under current consumed assumptions.
Negative fixtures: Reject conflated real/roundoff error, missing method/premises, insufficient bounds or reuse after branch/target/mode/coefficient/layout changes.
Outputs: Separate exact-real and floating-roundoff bound ledgers with method and invalidation conditions.
Dependencies: math/interval-claim-production, math/interval-domain-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-BOUND is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-claim-production` (MATH6; Open).
Implement: Produce a proof claim bound to exact source EFIR/EML, domain, branch, numeric mode, precision and requested property.
Boundary: Exact equivalence, bounded error, coverage, denominator, range-reduction or monotonic claims; a pending claim is not a proved result.
Done when: Emit equality, bounded-error, coverage, denominator, range-reduction or monotonicity claims with the actual requested property and source origins.
Negative fixtures: Reject omitted or mismatched input identities, a pending claim presented as proof, or a widened domain/property without corresponding evidence.
Outputs: Pending claim with exact EFIR/EML/input/domain/branch/mode/precision/property identity.
Dependencies: math/efir-verification, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-CLAIM is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-domain-check` (MATH6; Open).
Implement: Parse and validate an interval domain with exact endpoints, boundary openness, exclusions and bound provenance.
Boundary: Closed-by-default versus explicit open/half-open intervals; reject empty domains except an explicit no-usable-domain claim.
Done when: Parse rational endpoints or equivalently exact checked enclosures with closed default/open/half-open policy, exclusions, symbolic predicates and complex projection provenance.
Negative fixtures: Reject reversed/empty domains except explicit no-usable-domain claims, inexact unproved endpoints, hidden exclusions or missing bound provenance.
Outputs: Validated exact interval-domain map and bound provenance.
Dependencies: declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-DOMAIN is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-enclosure` (MATH1; Open).
Implement: Execute the required interval arithmetic operations with an inclusion-preserving result and applicable domain/branch/nonzero-denominator evidence.
Boundary: Required arithmetic, powers, root/exp/log, trig/hyperbolic, comparisons, polynomial/rational and piecewise operations; floating evaluation needs certified outward rounding.
Done when: Execute supported add/subtract/multiply/divide, integer/rational powers, sqrt/exp/log, trig/hyperbolic, abs/comparison, polynomial/rational and min/max/piecewise cases against exact or independent enclosure references.
Negative fixtures: Reject denominator cells containing zero without proof, invalid roots/branches or target-floating enclosure without certified outward rounding.
Outputs: Inclusion-preserving operation results and outward-rounding/nonzero/branch evidence.
Dependencies: math/interval-domain-check, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-ROUNDING is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-partition-production` (MATH6; Open).
Implement: Produce a complete domain partition with cell order, split decisions, local assumptions, statuses and unresolved cells.
Boundary: Heuristic construction is permitted; stopping/fuel accounting is retained for policy review without hiding cells.
Done when: Produce cells whose union covers the claimed domain with explicit boundary/exclusion policy, recorded heuristic split inputs and truthful unresolved regions.
Negative fixtures: Reject omitted cells, coverage gaps, incompatible boundary overlap or unresolved cells hidden as proved.
Outputs: Domain partition tree, ordered split decisions/local assumptions/statuses and unresolved-cell report.
Dependencies: math/interval-domain-check, math/interval-claim-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-PARTITION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-partition-replay` (MATH6; Open).
Implement: Replay the emitted partition deterministically and reject incomplete or unreplayable partitions.
Boundary: Partition proof artifact, not a second run of the adaptive search.
Done when: Replay emitted cell order, split points, local assumptions and statuses from the original claim, verifying complete coverage without rerunning adaptive search.
Negative fixtures: Reject a missing/substituted cell, changed split input/domain/claim or unreplayable ordering.
Outputs: Independent deterministic partition/coverage replay report.
Dependencies: math/interval-partition-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-PARTITION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-residual-check-emission` (MATH6; Open).
Implement: Emit the exact preserved runtime predicate for unresolved cells only when the numeric mode permits it, otherwise reject total-proof acceptance.
Boundary: Cell domains, placement, failure behavior, cost classification and SAFE15 preservation record; predicate execution belongs to the applicable runtime/check consumer.
Done when: Emit the exact residual predicate only in a mode permitting it, retaining domain, placement, observable failure and performance classification; runtime execution remains a consumer obligation.
Negative fixtures: Reject unresolved total-proof acceptance, omitted checks, predicate/domain drift or residual checks in a total compile-time-proof mode.
Outputs: Unresolved-cell runtime predicates, check placement/failure policy and SAFE15 preservation record.
Dependencies: math/interval-partition-replay, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-UNRESOLVED is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/legal-implementation-ranking` (MATH10; Open).
Implement: Rank only semantically eligible implementations under the declared cost/benchmark/autotuning policy and record choice, rejected candidates and fallback.
Boundary: Provider representation/mode/domain/branch/exception/target/version/certificate eligibility precedes performance ranking; benchmarks never prove semantics.
Done when: Filter by representation/mode/domain/branch/exceptions/target/provider/version/certificate/effect/allocation legality before benchmarking or autotuning; record rejected alternatives, deterministic ties and legal target-mismatch fallback.
Negative fixtures: Reject a faster but illegal provider, benchmark-only correctness, changed target fingerprints, unreplayable tuning or weaker fallback semantics.
Outputs: Semantic eligibility comparison, versioned runtime objective, replayable ranking/selection and fallback.
Dependencies: math/implementation-candidate-generation, math/floating-backend-eligibility. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-PROVIDER-COMPARE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/numeric-family-type-check` (MATH1; Open).
Implement: Type-check the requested numeric family and expose its explicit family/type annotations.
Boundary: Supported numeric families; fixed-width, arbitrary integer, rational, proof-real, floating, complex, interval, symbolic and unit-bearing forms retain distinct type semantics.
Done when: Type-check supported fixed/arbitrary integers, rationals, proof reals, floats, complex, interval, symbolic and unit-bearing forms, preserving explicit family, conversion class, allocation and EFIR facts.
Negative fixtures: Reject implicit narrowing, unrecorded precision loss, undeclared conversion/rounding or a host untyped-number substitution.
Outputs: Numeric type/family annotations and conversion diagnostics.
Dependencies: declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH1-CONVERSION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/numeric-mode-resolution` (MATH7; Open).
Implement: Resolve one compatible numeric mode and precision contract per operation/graph from source scope and emit its explicit origin-bearing record.
Boundary: Nearest compatible scope wins; generated code inherits the caller unless an explicit transition is permitted; every standard mode retains its declared evidence and exceptional behavior.
Done when: Resolve nearest compatible package/namespace/type/function/expression/declaration/provider mode with meaningful quality/representation/domain/rounding/exception/certificate/residual fields; generated code inherits unless an explicit transformation is allowed.
Negative fixtures: Reject missing/conflicting modes, impossible precision, hidden target defaults, relative error without near-zero behavior and unrecorded generated-mode changes.
Outputs: Numeric-mode environment, precision table and scope/generated-origin inheritance trace.
Dependencies: math/numeric-family-type-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH7-MISSING is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/numeric-mode-transition` (MATH7; Open).
Implement: Classify and validate a requested mode change, requiring enclosing opt-in and recorded evidence for weakening or representation changes.
Boundary: Exact preservation, strengthening, weakening and cross-representation transitions; provider selection never silently changes the source mode.
Done when: Classify equivalent, strengthening, weakening, representation-only and illegal changes and preserve the single resolved operation contract through provider replacement.
Negative fixtures: Reject unapproved weakening, representation change that loses branch/source facts or a target-default substitution; accepted weakening still records its origin and diagnostic.
Outputs: Mode transition classification, enclosing opt-in, evidence and downgrade diagnostic record.
Dependencies: math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH7-DOWNGRADE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/numeric-profile-matrix` (MATH1; Open).
Implement: Emit a concrete machine-readable numeric family/mode support matrix for the implementation's declared profiles and targets.
Boundary: Core/hosted/native/firmware/kernel/hardware/GPU/formal support and declared providers; optional support is not assumed universally present.
Done when: Enumerate concrete core/hosted/native/firmware/kernel/hardware/GPU/formal support and explicit unsupported combinations, including allocation/provider requirements and conversion/mode availability.
Negative fixtures: Reject undocumented-only support, optional providers assumed universal, hidden allocation in constrained profiles or unavailable family/mode combinations marked supported.
Outputs: Machine-readable family/mode/profile/target/provider support matrix.
Dependencies: math/numeric-family-type-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH1-PROFILE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/rational-value-check` (MATH1; Open).
Implement: Check a rational denominator and declare the effects of rational normalization before exposing a safe rational value.
Boundary: Ratio construction/normalization; downstream exact arithmetic implementation scope is not inferred from type existence.
Done when: Accept nonzero denominator construction and normalize exactly under the declared family/profile/effect/provider contract while retaining numeric annotations.
Negative fixtures: Reject zero denominators at safe-value admission, hidden normalization allocation/effects or type existence reported as executed exact rational arithmetic.
Outputs: Checked rational construction and declared normalization/allocation effects.
Dependencies: math/numeric-family-type-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH1-ALLOCATION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/real-proof-provider-import` (MATH6; Open).
Implement: Import a real-proof provider result only through an accepted checker validating claim/input identities, method, domain, branch, trust, replay and current assumptions.
Boundary: Opaque provider output may guide search but cannot certify transformations or erase checks.
Done when: Check exact claim/input hashes, domain/branch/method/trust/replay/proof-object/current assumptions and invalidation conditions independently of the provider or optimizer.
Negative fixtures: Reject opaque attestations used to certify, unsupported trust/method, stale inputs, branch mismatch or invalidated assumptions.
Outputs: Accepted-checker import report and SAFE15 proof reference.
Dependencies: math/interval-claim-production, math/interval-partition-replay. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-PROVIDER is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/semantic-equality-proof-gate` (MATH1; Open).
Implement: Reject a claimed symbolic/elementary equality unless applicable proof/certificate evidence establishes equality over the declared domain.
Boundary: Equality claim acceptance; syntax/tree identity is insufficient. EFIR rewrite and EML candidate interfaces retain separate producer/consumer facets pending central equivalence review.
Done when: Accept symbolic/elementary equality only after applicable independent proof or approved certificate validates the exact expressions, domain, branch, mode, precision and current assumptions.
Negative fixtures: Reject equality by syntax/EFIR/EML tree identity, optimizer assertion, missing evidence or proof for a narrower/different claim.
Outputs: Claim-to-proof/certificate acceptance decision over the declared domain.
Dependencies: math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH1-EQUALITY is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-counterexample-production` (MATH9; Open).
Implement: Preserve a rejected rule and reproducible counterexample with the original failed assumptions.
Boundary: Later accepted narrower rules retain distinct provenance rather than overwriting the broad rejection.
Done when: Reproduce a failing broad rule such as sqrt(x*x)=x at negative x, bind inputs/rule version/domain/reason and preserve it when a separately justified narrower rule is introduced.
Negative fixtures: Reject overwritten rejection history, missing failed assumptions or a narrowed rule used to retroactively validate the original broad claim.
Outputs: Rejected rule/application and reproducible counterexample with original assumptions.
Dependencies: math/symbolic-rule-registration. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-COUNTEREXAMPLE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-guarded-rewrite` (MATH9; Open).
Implement: Match and apply a registered symbolic rewrite only when its typed semantic side conditions hold and output obligations remain explicit.
Boundary: Guarded pattern matching and independent domain/branch/mode premises; no tree-shape equality shortcut.
Done when: Match registered typed patterns, discharge domain/branch/nonzero/periodic/exception/mode/floating/provider side conditions and preserve effects/checks/source/generated origins.
Negative fixtures: Reject an undischarged condition promoted to accepted rewrite, tree-shape equality shortcut or erased runtime check/provider effect.
Outputs: Typed guarded rewrite result with explicit pending/discharged obligations and anchors.
Dependencies: math/symbolic-rule-promotion, math/semantic-equality-proof-gate. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-PROOF is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-independent-trace-replay` (MATH9; Open).
Implement: Independently check the symbolic rewrite trace without trusting or rerunning the heuristic optimizer.
Boundary: Exact registered rules, premises, semantic identities and proof artifacts.
Done when: Reconstruct the transformation from exact input, registered rule versions, side-condition results and proof artifacts without trusting or rerunning the optimizer/saturation scheduler.
Negative fixtures: Reject missing/substituted/reordered steps, changed guards or versions, unproved edges and optimizer-generated pass status without checker replay.
Outputs: Independent rule/edge/premise/identity replay and proof-status report.
Dependencies: math/symbolic-trace-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-rule-promotion` (MATH9; Open).
Implement: Keep candidate, proved, bounded, rejected and deprecated rules distinct and select only a rule whose required proof or approximation certificate is accepted.
Boundary: Deprecated rules remain readable but unselected; a narrowed later rule does not validate a rejected broad rule.
Done when: Distinguish declared/candidate/guarded/proved/bounded/rejected/deprecated, permitting code changes only for current proved or bounded evidence; retain deprecated readability and broad-rule rejection history.
Negative fixtures: Reject selection of a candidate, deprecated/rejected rule, missing MATH5 bounded certificate or stale evidence under changed domain/branch/mode.
Outputs: Rule-state transition with exact proof or bounded-approximation certificate.
Dependencies: math/symbolic-rule-registration, math/semantic-equality-proof-gate. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-PROOF is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-rule-registration` (MATH9; Open).
Implement: Validate and register a versioned semantic rewrite-rule artifact with pattern/replacement, domain/branch/mode guards, proof obligations and provenance.
Boundary: Standard/package/provider/generated rules; package import obeys trust policy and semantic content identity.
Done when: Validate pattern/replacement, typed variables, domain/codomain, branch/modes, side conditions, proof status/refs, invalidation rules and source provenance before registry insertion.
Negative fixtures: Reject incomplete rule shape, missing branch/mode guards, semantic content changes under an unchanged version identity or untrusted package import.
Outputs: Versioned content-addressed rule artifact and package trust/source provenance.
Dependencies: math/efir-content-identity. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-RULE-SHAPE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-trace-production` (MATH9; Open).
Implement: Emit a symbolic rewrite trace retaining before/after identities, rule/version, side conditions, evidence, source origins and invalidations.
Boundary: Proof/diagnostic back-edges survive rewrite execution and extraction.
Done when: Emit each actual rule application and relevant heuristic/target/provider/runtime-cost choice with exact graph/node/trace identities and proof or candidate status.
Negative fixtures: Reject lost diagnostic/source back-edges, hidden choices, missing proof/side-condition results or accepted status assigned to a discovery-only step.
Outputs: Rewrite trace with before/after/rule/version/premises/evidence/origins/preserved-invalidated facts.
Dependencies: math/symbolic-rule-registration. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `domain/numeric-provider-binding`: Bind external numeric libraries/providers to typed FFI, numeric-mode/domain/error and approximation contracts. Accepted fixture: Bind numeric providers with implementation/version, typed FFI, numeric mode/domain/range/error and approximation contracts. Negative fixture: Reject mode mismatches, unsafe/opaque FFI and provider-dependent results presented as portable semantics. Required evidence: numeric provider/FFI manifests and mode/reference boundary fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume backend-llvm, legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/scientific-sine-integration`: Demonstrate declared certified sine with EFIR/EML semantic evidence, exact numeric mode/domain/error guarantees and reproducible reference accuracy/performance. Accepted fixture: Emit certified sine for the declared domain/error contract using EFIR semantics, EML proof/search and checked approximation/roundoff/branch/target evidence. Negative fixture: Reject domain gaps, unverified certificate or unauthorized backend fast-math flags. Required evidence: EFIR/EML/certificate, native/GPU kernel and reference accuracy report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume backend-llvm, legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.
<!-- domains-task-projection/math:end -->

## Schema

Key `schema`; owner role: schema/interop maintainer. Purpose: share typed data
contracts across boundaries. Scope: schema, serialization/canonical identity,
GraphQL/OpenAPI generation, migration/ABI/configuration/artifact schemas; not
permission to weaken source types. Input: typed declarations and version policy.
Output: validators, generated interface artifacts, encodings and migration
records under [schema contracts](../phase-10-schema-data-and-interop/README.md).

<!-- domains-task-projection/schema:start -->
Outcome: generated consumers enforce the same source schema. Remaining tasks,
all open: implement schema normalization and validators; implement canonical
serialization and version identities; generate GraphQL/OpenAPI and ABI/config
artifacts; implement checked migration mapping; execute source/generated parity
and weakening/taint/invalid-data rejection fixtures.

Yes/no acceptance: is schema identity stable? Do generated consumers reject
invalid values? Are migration loss and secret exposure prevented or explicitly
rejected? Integration: Types to schema, Emit generated artifacts, Build schema
identity, Query/AI/Stdlib consumers. Decision owner: schema maintainer;
recommendation: shared canonical schema IR with explicit target adapters.

Source-specific acceptance for S1-S9 remains open. S1: expanded manifests and
static types preserve every declared schema family, recursive strategy,
refinement classification, version, hash, source span, compatibility mode,
validation boundary and derivation target. Shared validators cover API,
database, message, workflow, AI, config, decoded-data, FFI and artifact sinks;
schema validation alone cannot clear stronger sink taint. Semantic diffs cover
field removal/rename, nullability, enums/variants, refinements and binary layout;
missing compatibility or migration policy rejects.

S2: codecs declare representability, unknown/duplicate/missing-field,
numeric/string/byte/variant and conversion policies, retain schema hashes and
pass round trips. Reject schemaless or unbounded polymorphic decoding, implicit
lossy conversion and decoded values used before intended-boundary validation.
Recursion/length checks may enforce concrete encoding or target representation
legality; arbitrary input/work quotas confer no acceptance authority.

S3: byte-for-byte vectors cover every supported canonical type family and
schema/version identity, encoded-byte map/set ordering, absent versus null,
numeric special values, UTF-8/normalization, timestamps and metadata policy.
Host order, identity, timezone and layout cannot affect bytes. Reject values
outside the declared domain and noncanonical hash/signature inputs.

S4/S5: generated GraphQL SDL/resolvers and OpenAPI routes, errors, validators
and contract tests preserve source constraints, nullability, auth/grants,
effects, provenance and compatibility. Route method/path, parameter/header/body
schemas, response/status/error models, versioning and required idempotency
policy remain explicit. Unsupported mappings require explicit
validated adapters or rejection. Typed clients retain schema/operation hashes
and operation validation. Reject hidden resolver/handler effects, weakened
types, unvalidated HTTP input and undeclared breaking changes.

S6: declared-dialect tables/views, constraints, indexes and typed row adapters
preserve source schemas. Typed migration plans record from/to identities,
transaction/backfill policy, rollback or forward-only behavior and deployment
order. Additive, breaking and destructive fixtures require data-loss decisions,
any policy-required review and evidence; execution requires write/admin grants.
Check shared API/client/AI compatibility and fixture databases or equivalent
plan validation. Reject lossy adapters, hidden dialect dependence in portable
claims, unauthorized migration and string-only migration without schema diffs.

S7: schema-hash-bound binary codecs and FFI inputs declare field widths/order,
endian, alignment/padding, discriminants, pointer ownership/lifetime, target and
calling convention. Cross-target byte vectors reject implicit host layouts,
uninitialized padding and unstable fields in stable formats. Runtime wrapper
execution remains a Runtime consumer, separate from layout production.

S8: config loaders record deterministic source precedence, defaults/required
fields, environment identity, secret access/rotation/audit and redaction policy.
Reads require declared effects and grants; external inputs stay tainted until
validated. Hermetic builds capture config inputs or reject the reads. Reload
fixtures require compatibility, lifecycle and observability policy. Reject
ambient reads, missing validation and secrets in logs, manifests, source maps,
prompts, traces or diagnostics except policy-allowed redacted markers.

S9: validators cover each declared artifact kind and the common versioned
source/compiler/dependency/pass provenance, profile/target/backend/runtime,
effects/grants, content identity, safety/proof/diagnostic/conformance envelope.
Preserve relevant unsafe-island, AI/tool, generated-code and redaction records;
require canonical hash/signing/replay/release inputs, bootstrap provenance for
graph cycles, and compatibility/migration records for version changes. Missing
required release evidence rejects at the schema gate; Release owns the actual
release decision. Each S1-S9 fixture records its stable diagnostic family,
schema/artifact identity, failing path or edge, applicable source/target/boundary
and remediation. These clauses specify future evidence, not passing results.

Foundation requirement gravity/source-schema-projection: Generate a validator, JSON Schema and typed artifact schema from the same source schema/version; run accepted and invalid payloads through every generated consumer and compare field/refinement/compatibility and diagnostic-path behavior. Refusals: Reject a weakened generated constraint, changed schema identity, malformed payload accepted by one consumer or unknown projection version; a generated file name alone is not consumer parity. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

Criterion `schema/graphql-generation/full-source-interface` (Open).
Implement a source-schema-to-GraphQL producer and its resolver boundary using
declared schemas, resolver IDs, query/mutation/subscription effects, grants,
auth/error/naming/nullability/pagination policy and compatibility mode. Generate
SDL, resolver adapters, typed clients, query validation, auth metadata, source
maps, a generation manifest and schema diff from the same source identity.
Cover objects/interfaces, Option/Result, enums/tagged unions, refinements,
lists/IDs/scalars and deprecated fields without weakening constraints;
unsupported mappings require explicit validated adapters or rejection. Compare
generated null/error/constraint behavior with source validators. Execute resolver
fixtures with allowed grants and reject hidden IO/model/tool effects or missing
grants. Reject missing auth/origin metadata, client schema hashes or operation
validation, and undeclared breaking/nullability/enum/field/resolver-policy
changes. Record S4-MAPPING/NULLABILITY/RESOLVER/AUTH/DIFF/CLIENT/SOURCEMAP/MANIFEST
diagnostics with schema, type/field, resolver, span, effect/grant, compatibility
and remediation. Closing evidence is the generated bundle, source/generated
parity and resolver grant results, valid/invalid operation fixtures and diff
report tied to exact source/compiler/policy hashes. Dependencies consume S1
expanded schemas/validators, S2/S3 identities, Types facts and Runtime grant
enforcement outputs; generated adapters alone do not prove resolver invocation.

Criterion `schema/database-mapping/full-source-interface` (Open).
Implement declared-dialect schema/relational-IR mapping into tables, views,
column types, constraints, indexes, primary/foreign/composite keys, nullability,
enum/variant representation, prepared bindings and typed row adapters. Exercise
scalars, decimals/timestamps, optional fields, generated columns, references,
JSON/arrays/views/computed fields with recorded dialect behavior. Compare each
generated row validator to its source schema; external rows remain tainted
until intended validation. Reject lossy mappings/adapters, unsupported dialect
features and hidden dialect dependence when portability is claimed. When this
mapping supplies migration inputs, preserve schema diffs and from/to identity,
transaction/backfill, data-loss/review, rollback-or-forward-only and deployment
ordering; preserve API/client/AI-output compatibility. Require fixture databases
or equivalent plan checks for generated changes, and write/admin grants for
actual migration execution. Reject string-only migrations without schema diffs,
destructive changes lacking required policy/review/evidence and unauthorized
migrations. Closing evidence is the mapping manifest, generated structures,
typed adapters, query-plan metadata, dialect/row-parity fixture results and
applicable migration/compatibility records. Diagnostics use the applicable S6
family with schema/field/table/column, dialect, migration ID, effect/grant,
data-loss state and remediation. Dependencies consume S1 schemas, B11 relational
IR and SAFE10/SAFE11/P9/R7 authority, taint and replay records; consuming a
migration plan is separate from evidence that it executed.

Criterion `schema/configuration-reload/full-source-interface` (Open).
Implement a reload boundary for a declared typed-config schema and environment
using recorded deterministic source precedence, defaults/required/secret fields,
validation and redaction policy. Require an explicit compatibility, lifecycle
and observability policy for each reload. Exercise a compatible reload and
policy-rejected incompatible or missing-policy reloads, preserving config ID,
schema/source versions, lifecycle result and redacted observation records.
External replacement values stay tainted until validated. Reject malformed or
missing required values and env/file/secret/provider reads without declared
effects/grants. Reject ambient config reads and secret leakage in logs,
manifests, source maps, prompts, traces or diagnostics; only policy-allowed
redacted markers may appear. Capture build-time reads as reproducibility inputs
or reject them in hermetic builds. Closing evidence is the versioned reload
policy, typed manifest/source-precedence/secret records, accepted/rejected
reload fixtures and redaction report. Emit applicable S8 diagnostics including
S8-RELOAD with config/field/source/span-or-edge, effect/grant, secret/redaction
state and remediation. Dependencies consume S1/S2 schema/decoding and Runtime
lifecycle, SAFE10/SAFE11/R11 grants/taint and R12 observations; loader generation
alone does not prove an actual reload.

Criterion `schema/artifact-validation/full-source-interface` (Open).
Implement versioned artifact-envelope and graph validators for each declared
Gravity artifact kind, including source-derived schemas, IR/backend outputs,
runtime/package manifests, conformance/proof objects, workflows/agents, APIs,
migrations, HDL/mobile outputs, docs and governance/release records. Preserve
common kind/schema-version, source graph/compiler/dependency/pass provenance,
profile/target/backend/runtime, effects/grants, content/source hashes and
safety/proof/certificate/diagnostic/conformance fields plus kind-specific fields.
Preserve relevant unsafe-island, model/tool policy, generated-code provenance,
runtime service and secret-redaction records. Require canonical encoding for
hash/signing/replay/release uses, bootstrap/self-hosting provenance for graph
cycles, and compatibility/migration records for schema version changes.
Closing evidence is the artifact/graph/content/provenance/evidence schemas,
validator outputs, canonical vectors, compatibility report and per-kind valid
and invalid manifest fixtures. Reject unsupported schemas, missing fields or
evidence, invalid hashes/provenance, noncanonical identity inputs, unexplained
cycles and hidden authority/unsafe decisions with S9 diagnostics naming the
artifact/kind/version, field or graph edge, release-gate context and remediation.
Dependencies consume S1 schemas/validators, S3 canonical bytes and B13 emitted
artifact records; valid schema shape is separate from checked evidence truth.

Criterion `schema/release-evidence-gating/full-source-interface` (Open).
Implement the artifact release-evidence admission interface: derive applicable
requirements from the versioned release policy, bind the artifact graph and
source/compiler/spec/dependency identities, and verify every required provenance,
safety/proof/effect/grant/test/conformance record before emitting a gate result.
Require canonical identity input and explicit bootstrap provenance for cycles;
schema changes retain compatibility/migration records. For formal claims used
by a gate, require machine-checkable evidence naming claim, assumptions/trusted
basis, checker/version and exact input hashes; a proof reference or well-shaped
manifest alone cannot admit a claim. Keep checked proof evidence and executable
test results distinct, retaining checker reports, counterexamples and invalidation
records when applicable. Closing evidence is the release-gate schema, applicable
policy matrix, exact evidence graph, checker/validator results and admitted and
rejected gate fixtures. Reject missing evidence/provenance, noncanonical or
forged hashes, stale source/compiler/spec/dependency bindings, missing assumptions,
unknown checker identity and prose-only/uncheckable proof claims; report the
applicable S9-EVIDENCE/PROVENANCE/HASH/CANONICAL/CYCLE/COMPATIBILITY or TEST11003
condition with artifact/claim, failing edge, policy gate and remediation.
Dependencies consume S9 validation, S3 canonical identity, D9/SAFE15/TEST11
checker results and Release policy inputs. Release owns publication decisions;
its decision consumer must reject absent, failed or identity-mismatched gate
results, with explicit consumer-refusal fixtures in the closing evidence.
This criterion produces and consumes a checked gate result and confers no
publication authority.

Criterion `schema/api-client-generation`: Generate typed API clients tied to schema/operation hashes and validate operations against the declared server contract. Accepted fixture: Generate typed clients bound to operation/schema hashes and validate request/response/error behavior against the declared server contract. Negative fixture: Reject stale schema/version/hash, missing validators, invalid runtime data and silent schema drift. Required evidence: API client/declaration/validator artifacts and client/server parity fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `schema/configuration-loading`: Load typed configuration using deterministic recorded precedence, required/default/secret fields and validated provider inputs. Accepted fixture: Load required/default/secret typed fields with deterministic recorded precedence and validated declared provider inputs. Negative fixture: Reject missing required values, invalid source types, undeclared ambient precedence and secret exposure. Required evidence: configuration schema/loader/precedence manifest and redacted load/error fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `schema/openapi-generation`: Generate source-faithful OpenAPI route/schema/error/auth documents and request/response validators with handler contract tests. Accepted fixture: Generate source-faithful route/schema/request/response/error/auth OpenAPI artifacts with runtime validators and handler parity tests. Negative fixture: Reject incompatible operation/schema identity, weakened validation, invalid requests/responses and drift from actual handler contracts. Required evidence: OpenAPI/schema/validator artifacts and handler contract fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `schema/source-manifest-and-validator/decision-interface` (Open): Input: S1 source schema and resolved type/effect/capability/taint facts, declared refinement class, boundaries and derivation policy. Outputs: Expanded versioned schema manifest; Generated runtime validator; Recursive/refined schema fixtures and source/generated parity report. Accept: Expand records/maps/arrays/sets/tuples/enums/tagged unions/optional/refined/alias/reference/recursive/opaque schemas into an authoritative typed manifest, preserving id/version/hash/span/compatibility/boundary/derivation target. Generate the validator from that manifest; preserve compile-time/runtime/boundary/proof/advisory refinement classes and intended-schema versus stronger-sink trust distinctions. Run accepted and invalid payloads through API/database/message/workflow/AI/config/decoded/FFI/artifact boundary fixtures; documented narrowing projections remain explicit. Refuse: Malformed shape, unsupported/unclassified refinement or recursion without strategy; Missing identity/boundary/manifest metadata or generated constraints weaker than source; External values trusted before intended validator/sink validation or AI output bypass. Dependencies consume actual artifacts: Types resolved schema identities and constraints; Effects/Auth/Legality authority and taint facts; S2-S9 consumers receive the expanded schema and validator; no whole-component completion gate. Diagnostics: Applicable S1-SHAPE/REFINEMENT/BOUNDARY/PROJECTION/TAINT/RECURSION/MANIFEST with schema/version/span/boundary/target/field and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/declared-format-codecs/decision-interface` (Open): Input: S2 schema/version/format representability, field/unknown-field/numeric/string-byte/variant/conversion/trust/canonicalization policy and actual typed values or external bytes. Outputs: Serializer manifest and encoder/decoder products; Validation/trust/compatibility records; Round-trip and invalid-byte/value fixtures tied to schema hashes. Accept: Encode typed values under the declared policies and decode external bytes while checking required/unknown/duplicate fields, variants/refinements, precision, UTF-8/normalization and binary representation legality. Preserve numeric/string/timestamp/decimal/UUID/binary/variant conversions only under explicit compatible loss policy. Round-trip all claimed formats and retain untrusted/authenticated-unvalidated/schema-validated/sink-validated distinctions. Refuse: Schemaless or unsafe polymorphic decode without a finite declared type set; Implicit lossy conversion, duplicate/missing/incompatible fields, invalid variants or encoding; Trusting decoded data before intended validation, missing round-trip evidence, or noncanonical hash/signature use. Dependencies consume actual artifacts: S1 schema/validator and SAFE11 sink-taint facts; S3 canonical rules when canonical identity is claimed; S7 target representation when binary/ABI format is claimed. Diagnostics: Applicable S2 format/field/variant/numeric/string/polymorphic/taint/roundtrip/manifest diagnostics bind schema/format/path/span/policy/trust state. Removed work/input quotas are not reinstated by mixed recursion/length text. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/canonical-codecs-and-identity/decision-interface` (Open): Input: S1 schema hash, format version, canonical domain/policy, typed semantic value or encoded bytes; hash/sign/replay consumers supply their actual identity input. Outputs: Canonical encoder/decoder and format manifest; Byte-exact per-type reference vectors; Hash/sign/replay input admission and rejection records. Accept: Encode equal values identically under the same schema/version/policy, using encoded-byte map/set ordering, type/version tags and explicit absent/null, integer/decimal width, NaN/infinity/signed-zero, timestamp/timezone, UUID/symbol/keyword, union, UTF-8/normalization, byte and metadata rules. Decode against that same version/domain and reference vectors. Actual hash/sign/replay identity consumers require those canonical bytes and reject mismatched or unsupported encodings before use. Refuse: Host iteration/object identity/target layout/timezone/encoding changing canonical bytes; Ambiguous values, missing canonical rules, invalid UTF-8/metadata or vector mismatch; Hashes/signatures over noncanonical or wrong-schema/version bytes; a canonical flag alone is insufficient. Dependencies consume actual artifacts: S1 expanded schema and S2 representation policy; B13/package/workflow/AI/proof/release consume checked canonical bytes and input records. Diagnostics: S3-NONCANONICAL/ORDER/NUMERIC/STRING/METADATA/HASH/VECTOR/MANIFEST bind schema/value-path/rule/version/consumer context and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/binary-layout-codecs-and-vectors/decision-interface` (Open): Input: S1/S2 schema and declared target triple/calling convention, explicit field representation, compatibility, pointer ownership/lifetime and target ABI facts. Outputs: Binary/ABI and layout manifests; Schema-hash-bound codecs and typed FFI binding input; Ownership/lifetime map, cross-target report and byte reference fixtures. Accept: Produce field-order/width/alignment/padding/endian/signedness/optional/tag/pointer layouts including struct/union/enum/string/buffer/error/ownership-transfer ABI policy. Generate codecs/binding input from the accepted layout and its schema hash. Check each supported target against byte-exact reference vectors or a declared target-independent encoding, including buffers/handles/callback/bitfield/resource forms where claimed. Refuse: Implicit host layout/packing/pointer size or ABI/calling-convention mismatch; Uninitialized/nondeterministic stable bytes or pointers/handles missing ownership/lifetime policy; Wrong schema hash, unsupported representation, missing vectors or undeclared binary-breaking changes. Dependencies consume actual artifacts: Types/ownership facts and S1/S2 schema contracts; B2/B3/B4/B9/B12 and SAFE2/SAFE7/SAFE15 layout/proof contracts; R10 consumes binding input, while actual foreign invocation remains a separate runtime consumer. Diagnostics: Applicable S7 layout/endian/pointer/ABI/variant/fixture/compatibility/manifest diagnostics bind schema/field/target/rule/pointer-or-variant/fixture/proof and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/semantic-compatibility-diff/decision-interface` (Open): Input: Exact old/new schema identities, expanded source and generated projection contracts, and additive/migration-required/binary-stable/reviewed-breaking/append-only/exact compatibility policy. Outputs: Semantic diff and compatibility report; Required migration/review decisions; Accepted and rejected evolution fixtures. Accept: Classify added/removed/renamed fields, changed nullability/enums/variants/refinements/binary layouts and semantic hazards. Include GraphQL resolver policy, HTTP route/error/auth, database dialect/migration, ABI and artifact-schema changes; preserve public schema-library consumer context when those adapters are used. Emit the required compatibility/migration decision for each changed boundary instead of accepting a textual-only diff. Refuse: Changed schema/projection without declared policy or required migration/review evidence; Hidden API/client/AI or ABI compatibility breaks and weakened constraints; Wrong from/to identity, dropped negative condition or schema helper availability substituted for a checked diff. Dependencies consume actual artifacts: S1 old/new schemas and S4/S5/S6/S7/S9 projection/consumer records; STD10 schema APIs and Build/Query/Workflow/AI/Runtime consume the applicable checked diff, not a new whole-component completion condition. Diagnostics: Applicable S1-COMPATIBILITY, S4-DIFF, S5-DIFF, S6-MIGRATION/DATA-LOSS, S7-COMPATIBILITY and S9-COMPATIBILITY retain changed path, boundary/policy and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/migration-plan-admission-and-validation/decision-interface` (Open): Input: Checked S1/S6 old/new schema diff, declared dialect, transaction/backfill/rollback-or-forward-only/deployment ordering, data-loss policy and any required review record; runtime supplies actual database write/admin grants. Outputs: Typed SQL/provider migration and plan/identity manifest; Data-loss/review/rollback-or-forward-only and API-client compatibility records; Actual admission/denial and fixture-database or equivalent plan-validation results. Accept: Generate a typed migration with exact from/to identities and all declared policy fields; include shared GraphQL/OpenAPI/client/AI compatibility. At the actual execution boundary admit only with applicable database write/admin grant and required destructive-change policy/review/evidence. Validate generated additive/breaking/destructive changes using fixture databases or equivalent checked plans, binding exact subject, dialect and policy in results. Refuse: String-only migration without schema diff or wrong from/to/dialect; Destructive change lacking explicit data-loss decision or policy-required review/evidence; Missing write/admin grant, missing rollback/forward-only policy, hidden API break or missing/mismatched validation fixture. Dependencies consume actual artifacts: S1 schema/diff and B11 relational artifacts; SAFE10/SAFE11/P9/R7 authority, taint, deployment and replay records; runtime actual execution/admission is not established by plan generation. Diagnostics: Applicable S6-MIGRATION/DATA-LOSS/ROLLBACK/CAPABILITY/FIXTURE/MANIFEST bind schema/path/dialect/migration/effect/grant/data-loss and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/executed-http-contract-parity/decision-interface` (Open): Input: Pinned generated OpenAPI/request/response/error schemas, schema/operation/route identities and actual handler/provider/version with declared effects/grants/auth/idempotency policy. Outputs: Executed handler contract-test report; Accepted/invalid request/response/error fixtures and stable source-mapped diagnostics. Accept: Execute generated contract tests against the actual declared handlers and compare request/response/error/status/nullability/constraint/auth behavior to the authoritative route/schema contract; bind handler/provider, schema/operation and source/compiler identities in results. Retain raw external-input taint until intended validation. Refuse: Invalid request/response/error admitted, drifted operation/schema/handler identity or weakened source constraint; Undeclared effect/grant/auth/idempotency requirement and unvalidated HTTP input; Generated test source or OpenAPI document substituted for actual execution results. Dependencies consume actual artifacts: S1-S3 schema/identity and generated S5 document/validators/client contracts; DOM8/B11/R11/R12 actual handler, authority and observation interfaces. Diagnostics: Applicable S5 route/schema/taint/capability/idempotency/diff/client/source-map diagnostics retain route/method/path/span/schema/effect/grant/compatibility and remediation. These are required future products/results; no execution or implementation acceptance is claimed.
<!-- domains-task-projection/schema:end -->

## Query

Key `query`; owner role: database/query maintainer. Purpose: preserve typed query
and transaction semantics. Scope: relational IR, parameters, row schemas,
transactions, migrations and database authority; not a complete database engine.
Input: typed query forms, schema and provider contract. Output: query IR,
parameterized executable query, row validation and transaction diagnostics.
Governed by [query contract](../phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md).

<!-- domains-task-projection/query:start -->
Outcome: real parameterized queries return validated rows and respect declared
transaction/provider semantics. Remaining tasks, all open: implement relational
IR and typed parameters; lower to a declared provider; connect row/schema and
transaction checks; execute result-equivalence, injection, missing-grant and
invalid-row fixtures. Yes/no acceptance: are parameters separated from query
syntax? Are results typed and grants enforced? Do transaction failures preserve
semantics? Integration: Schema/MIR to Query, Runtime provider, Stdlib database
API. Decision owner: query maintainer; recommendation: explicit provider dialect
contract, not host SQL strings as semantic authority.

Criterion `domain/analytic-plan-lowering`: Lower typed analytics/aggregation/window plans to an explicit SQL/native/GPU/workflow/host venue preserving numeric/null/order/determinism/approximation and policy lineage. Accepted fixture: Lower aggregate/window inputs to the declared SQL/native/GPU/workflow venue, preserving order, nulls, numeric mode, determinism, approximation and lineage. Negative fixture: Reject undeclared execution venues, nondeterministic order or lost policy lineage. Required evidence: typed dataflow plan and venue/reference comparison; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/database-query-integration`: Demonstrate the declared schema-migration/query slice with typed rows, grants, failures and portability evidence. Accepted fixture: Run schema migration and typed query with prepared bindings, validated rows, explicit transactions/grants, compatibility and rollback/forward policy. Negative fixture: Reject tainted query syntax, invalid null/timezone/numeric/enum rows, missing grants and unapproved data loss. Required evidence: migration artifact, prepared query, row adapter and reference fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/revenue-aggregate-integration`: Demonstrate declared revenue aggregation against reference and invalid-input/authority/semantic rejection cases. Accepted fixture: Run the declared revenue aggregate with decimal mode, source/output schemas, grouping/null/order semantics, database authority and source-table lineage. Negative fixture: Reject schema drift, unauthorized data access, changed numeric semantics or lost lineage. Required evidence: SQL/native plan, lineage/schema reports and reference/rejection fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/streaming-materialization`: Enforce declared streaming/materialization state and memory policy, rejecting unbounded materialization in constrained profiles. Accepted fixture: Execute declared streaming/materialization and memory behavior for the chosen constrained runtime/provider, preserving data and failure semantics. Negative fixture: Reject materialization that violates the actual declared runtime memory/streaming policy; do not introduce compiler input/work allowances. Required evidence: execution/memory plan and stream/materialization failure fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.
<!-- domains-task-projection/query:end -->

## Workflow

Key `workflow`; owner role: durable execution maintainer. Purpose: make durable
steps and nondeterminism auditable. Scope: workflow graph, retries, compensation,
idempotency, timers and replay; not ambient network execution. Input: checked
workflow forms, event schemas and effect/provider records. Output: workflow IR,
executable graph, versioned event log and replay evidence. Governed by
[workflow contract](../phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md).

<!-- domains-task-projection/workflow:start -->
Outcome: a workflow executes and replays without repeating recorded external
effects. Remaining tasks, all open: implement graph validation/lowering;
implement event-log schema and durable transitions; connect retry/compensation/
timer providers; execute crash/restart/replay and unrecorded-nondeterminism
rejection fixtures. Yes/no acceptance: is replay deterministic? Are external
calls deduplicated? Are invalid transitions rejected? Integration: MIR/Schema,
Runtime durability, AI review steps, Stdlib workflows. Decision owner: workflow
maintainer; recommendation: versioned event log as replay input, explicit
provider boundary for storage rather than a hidden database choice.

DOM9 acceptance also requires declared CRDT identity/order/merge/update and
serialization laws, causal history, offline authorization, conflict visibility,
tombstone/compaction and migration policy. Classify coordination as free,
required or deferred; reject nonmonotonic rules without an explicit mechanism.
The checkout fixture must not double-charge on replay. The local-first cart
fixture must converge after partition/heal, duplicate/reordered delivery,
restart and migration, and reject invalid merge laws or hidden intent loss.
Dependencies: Schema state/event versions, Runtime durable storage/replay and
Auth grants. These criteria add no acceptance credit or provider selection. P9 profile interface: emit typed step input/output/effect/grant/retry/timeout/cancellation/idempotency/compensation and event/replay records; validate versioned messages and persisted state with migration/taint/retention/order/delivery policy. Execute recovery separately from admitted step construction; replay must consume recorded nondeterminism without repeating external actions. Reject schema drift without migration, untyped external failure, unrecorded replay inputs and missing required compensation. Service-boundary admission consumes Schema/Auth and Runtime facts, not their whole-task completion.

Criterion profile-gap/distributed/durable-step-contract (Open): Input: Workflow graph and step input/output schemas, declared effects/grants, idempotency key, retry/timeout/cancellation/compensation and failure policy. Accept: Construct typed step records and durable transitions; execute admitted external steps with declared idempotency; execute failure/retry/timeout/cancel/compensation transitions separately; replay recorded results/decisions without repeating the recorded external action. Retain human-review state where applicable. Reject: Untyped external failure, missing required idempotency/compensation or timeout/failure contract, ambient grants, raw device operations and unrecorded nondeterminism in replay-sensitive steps. Required outputs: Workflow graph, typed step table, event-log/persistence boundary records, retry/timeout/compensation table, replay policy/schema and accepted/rejected recovery fixtures. Dependencies: runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution, schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping distributed/durable-step-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/distributed/message-state-boundary (Open): Input: Message, persisted state and service endpoint with schema/version, compatibility/migration, taint/retention, delivery/order and relevant idempotency policies. Accept: Validate transport input/output against the named schemas, apply or reject state/schema migrations under replay compatibility, enforce data-sink taint/retention, and admit services only with declared effect/grant/timeout/retry/error and required backpressure/circuit-breaker policy. Reject: Schemaless boundary, version drift without migration, untyped service failure, ambient service authority and missing delivery/order or required side-effect policy. Required outputs: Message schema bundle, migration result, service capability/error boundary manifest, state persistence and delivery/ordering conformance records. Dependencies: schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping distributed/message-state-boundary; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

Criterion `domain/coordination-classification`: Classify replicated rules as coordination-free/required/deferred and require explicit coordination evidence for nonmonotonic invariants. Accepted fixture: Classify monotonic and nonmonotonic replicated computations as coordination-free, required or deferred with source-linked mechanism evidence. Negative fixture: Reject a nonmonotonic invariant labeled coordination-free without an explicit barrier or reconciliation mechanism. Required evidence: coordination analysis report and mechanism fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/crdt-convergence`: Implement declared CRDT state/wire identity/order, merge algebra and update rules with type-preserving convergence evidence. Accepted fixture: Check associative, commutative, idempotent and type-preserving merge, identity/order and inflationary update or declared operation-based delivery semantics. Negative fixture: Reject invalid merge/update laws, insufficient recovery serialization or false convergence evidence. Required evidence: CRDT law proof/model fixtures and typed merge/update trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/distributed-checkout-integration`: Execute the declared checkout workflow with durable idempotency/retry/compensation and typed failure/replay evidence. Accepted fixture: Execute checkout payment/shipping steps with typed schemas, recorded results, idempotency, retry/timeout/cancellation and compensation. Negative fixture: Reject unrecorded clock/randomness, repeat charging on replay or missing required compensation. Required evidence: workflow graph, event schema and restart/replay/failure trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/local-first-cart-integration`: Execute the declared local-first cart reconciliation with explicit merge/causality/conflict/coordination boundaries. Accepted fixture: Mutate a replicated cart note offline, retain causality, heal partitions and converge under delayed/duplicated/reordered delivery and restart/migration. Negative fixture: Reject nonmonotonic rules without coordination and reconciliation that hides or loses conflicting intent. Required evidence: CRDT/sync/conflict manifests, convergence and rejection fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/local-first-sync`: Persist and exchange enough causal state/history to reconcile offline replicas with declared topology, dedup, conflicts, deletion/compaction and authorization changes. Accepted fixture: Persist and exchange declared causal state/history with replica identity, topology, dedup, retention/snapshots, tombstones/compaction, authorization and explicit conflict visibility. Negative fixture: Reject incomplete recovery history, undeclared clock authority or silent overwrite/drop of conflicting intent. Required evidence: sync manifest, conflict table and partition/restart/migration traces; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.
<!-- domains-task-projection/workflow:end -->

## AI

Key `ai`; owner role: AI language/runtime maintainer. Purpose: compile model,
prompt, tool, memory and review declarations into auditable artifacts. Scope:
[AI contracts](../phase-11-ai-and-agentic-programming/README.md); not treating
model output or retrieved text as authority. Input: checked declarations,
schema, policy and explicit provider grants. Output: agent/prompt/tool manifests,
validated structured results, review events and evaluation records.

<!-- domains-task-projection/ai:start -->
Outcome: the declared AI workflow executes through schema, capability, taint
and human-review boundaries. Remaining tasks, all open: implement declaration
lowering; connect model/tool/memory providers with explicit policies; implement
structured-output and taint validation; connect human review and replay;
execute evaluations plus prompt-injection, missing-review, cross-tenant and
unvalidated-output rejection fixtures.

Source acceptance for A1-A11 remains open. A1: lower all declared AI forms
through syntax, typed core and legal profiles into linked manifests, preserving
source/macro origins, every AI effect, explicit replay modes and runtime audit
records. Reject ambient credentials, hidden adapter effects, missing model or
schema identities, generated source bypassing the normal compiler pipeline,
and privileged deployments without review policy or required evaluation evidence.

A2: normalize provider/model declarations, including adapter, endpoint, modes,
version/tokenizer, schema enforcement, retention and credential requirements.
Require a provider handle in addition to the effect; network access alone is
insufficient. Check embedding dimensions, CI live-call allowance, fallback
mode/schema/retention/evaluation compatibility and floating-identity upgrades.
Record reproducibility parameters, response/refusal/validation state, typed
failure classes, drift and retry/fallback decisions. Pinned audit replay rejects
identity mismatch. Secret/no-store bodies stay out of prompts, memory, datasets
and ledgers; retain permitted hashes, validation facts and observed cost/latency.
Preserve real external spending/account preflight before issuing paid calls.

A3: emit typed prompt schemas, tool visibility, authority/taint maps, render
hashes and compatibility policy. Validate format/schema, canonicalization,
per-field taint and intended use before typed output admission. Reject arbitrary
model-selected schemas, illegal partial outputs, undeclared tools and breaking
output changes mislabeled compatible. Refusals follow a typed branch; repair
policy and attempts are recorded. Secret-to-provider flow requires explicit
policy; no-store responses retain only permitted hashes and facts.
The inherited prompt-authority bridge retains approved sanitizer OR approved
delimiter policy without promoting data to instruction authority or clearing
source/taint, grant, secret or review obligations.

A4-A5: emit tool and agent manifests with complete dependencies and separate
memory read/write modes. Tool declaration, agent toolset, package/deployment
grant, scoped runtime handle and policy must all agree. Plans and model text
grant no authority. Validate tool and agent results, expose implementation
effects, normalize foreign errors, redact logs and model-facing descriptions,
and record agent identity on every invocation event. Privileged tools require
review policy; non-idempotent retries/replay require declared idempotency or
compensation. Generated agent source re-enters compiler validation.

A6: consume the Workflow task's typed graph, state/event-log schemas, durable
transitions and replay records. Preserve node effects, capabilities, idempotency,
review payload binding, retry/compensation and lowering metadata. Validate
branch inputs and typed/effect-checked compensation. Record or forbid model,
tool, memory, random, clock and external IO nondeterminism; reject unsafe repeated
side effects, changed review payloads and incompatible graph/state/log evolution
without migration. Replay fixtures reuse the actual recorded decisions.

A7: emit memory item/metadata schemas, provenance, partitions, retention,
redaction, indexing, embedding and replay policies. Enforce separate scoped
read/write and embedding capabilities. Reject invalid writes, unauthorized
protected-data embedding/persistence, implicit cross-tenant sharing, retrieved
instruction authority, incompatible index reuse and fresh retrieval where a
recorded result is required. Record query/result ids, scores, embedding identity
and old/new embedding lineage. Deletion/redaction must remove stale retrievable
entries and update indexes and ledgers.

A8: compile explicit production policy and deterministic layered decisions with
deny precedence. Later policies cannot silently broaden authority. Record action
identity, effects/capabilities, data/taint, review, eval and replay facts and the
resulting rule/decision. Reject cyclic policy, unsafe fallback, unvalidated output
or generated code and protected-data logging. Authority-broadening changes need
compatibility/security review; emergency overrides are explicit, scoped,
time-bound and auditable, with no new capability implied.

A9: produce versioned dataset/schema/provenance/split artifacts and eval reports
bound to every behavior-changing subject/model/prompt/tool/memory/policy/runtime
identity. Record changed artifacts, metric direction/threshold/aggregation and
confidence/sample or variance rules, seeds/traces, redacted samples or hashes,
probe results and release decisions. Required safety probes cover injection,
tool misuse, denial, schema failure, refusal and protected-data leakage. Reject
absent/stale/incompatible release evidence, name-similarity fallback, missing
probes or thresholds and unauthorized live-provider evaluation; preserve real
credentials, provider spend/account policy and report-signing requirements.

A10: compile typed review declarations with action, actor/role, displayed evidence
and evidence hash, payload hash, timestamp, expiry, revocation, denial/timeout,
replay and emergency rules. Block required actions until matching valid review;
reject absent records, wrong roles, changed payloads, expiry or revocation.
Display enough typed evidence for the decision, follow denial as a workflow
outcome, and reuse recorded decisions only under the declared replay policy.
Audit requests and lifecycle transitions with redaction and permitted hashes.

A11: keep user/web/file/package/retrieved/tool/model content in its declared
data authority, preserving taint back into model context. Data cannot grant
tools or change policy. Reject instruction promotion, unauthorized secret flow,
unvalidated tool output and generated source, and privileged releases without
defense probes. Monitor escalation, override and exposure attempts; incident
bundles reconstruct prompt, taint, tool, memory, policy and workflow decisions
without exposing protected raw content. Each source's diagnostic identifiers
and contextual fields and its positive/negative conformance fixtures are part
of acceptance, not separate implementation tasks.

The exact mixed A1/A3/A5/A6 call-count, repair-attempt and retry-accounting
clauses remain unresolved in the bounded family proposal; they cannot receive
acceptance by linking this task. Remove synthetic project/compiler work quotas
under current accounting policy while preserving real external billing/account
limits, semantic representability and actual host restrictions. Do not silently
supersede a mixed source clause or invent a replacement work allowance.

Yes/no acceptance: does untrusted output
remain data? Are actions denied without grants/review? Are evaluations bound to
exact prompt/model/tool identities? Integration: Schema/Auth, Workflow/Runtime,
Stdlib AI. Decision owner: AI maintainer; recommendation: provider-independent
artifacts and explicit model identity, never a vendor SDK side path.

SAFE13 acceptance: inspect model/provider/version, input/output schemas, nondeterminism, real external cost, retention, safety and replay records. Preserve structured prompt roles and provenance; reject untrusted grant/policy changes, privileged concatenation without approved sanitization/delimiting, and unvalidated tool arguments. Distinguish read-only/write/destructive/external/privileged tools; explicit grants and required human/policy decisions bind action/diff, reviewer, time and result before invocation. Generated code carries model/prompt origins and passes ordinary compiler/package checks before execution. Memory remains tainted absent trust metadata and enforces retention/redaction/privacy/deletion and secret policy. Check exact/audit replay and idempotent side effects through Runtime; prompt comments are not review decisions.

P10 profile interface: keep model admission/execution, tool schema/grant/review admission, generated-program compiler validation, prompt-role/taint checks, memory retention/deletion/privacy and secret-sink gates distinct. Emit prompt/model/provider/tool/policy/review/input/output/error/retry/generated-code digests and replay records. An evaluation hook emits separately identified results and grants no action authority. Reject prompt policy escalation, unvalidated outputs, missing applicable review, ungranted destructive tools and unauthorized secret disclosure. Provider billing limits remain external; no compiler work quota is introduced. Runtime model/tool/memory and Schema/Auth/generated-code APIs supply interface facts.

Foundation requirement gravity/ai-tool-schema-policy-record: Execute a declared model/tool/memory interaction with typed request and response schemas, separately checked effects and grants, output validation, tool/review policy and a redacted attributable decision record naming provider/model/tool/memory identities. Refusals: Reject schema-invalid output, an ungranted tool, crossed memory authority and model text attempting to supply review authority before any protected effect. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): schema - Versioned source schema, generated validators/codecs and parity artifacts; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

Historical-gap criterion interfaces (all Open). The stable labels below
reuse inherited source behavior IDs; they add no implementation task IDs.
Each requires its complete original source-context facets, including the
retained negative conditions, diagnostics and conditional consumer roles.

Criterion `behavior/a1/credential-source` (Open; implementation unassessed).
Required behavior: Reject ambient provider credentials embedded in AI source.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a provider credential literal embedded in source before declaration admission.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/declaration-lowering` (Open; implementation unassessed).
Required behavior: Compile declared AI forms from syntax objects to typed declarations and an AI program manifest preserving source/macro origins and declared cross-artifact edges.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject an AI declaration whose emitted manifest loses source/macro origin or a required dependency edge.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/deployment-eval-gate` (Open; implementation unassessed).
Required behavior: Require production agent deployment to name its required evaluation evidence and carry accepted report references.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject production promotion when a required evaluation report is absent or has a different subject identity.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/effect-exposure` (Open; implementation unassessed).
Required behavior: Expose every AI operation as its declared nonpure effect in typed/effect output and emitted manifests, including hidden provider tool effects.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a provider adapter whose implementation performs an undeclared tool effect.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/external-call-policy` (Open; implementation unassessed).
Required behavior: Resolve and enforce the source-specific model-call budget/timeout policy, preserving actual external provider spend and controls while excluding removed synthetic compiler accounting.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Deny an actual paid call before issuance when its external spending policy is exceeded; do not refuse a legal mock call merely for synthetic call-count exhaustion.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/memory-write-policy` (Open; implementation unassessed).
Required behavior: Require AI memory writes to carry retention, redaction, tenant partition and source provenance policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a write missing retention, redaction, tenant or source-provenance policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/model-call-contract` (Open; implementation unassessed).
Required behavior: Require a model call's model/provider identity, input/output schema and replay policy before admitting it; retain the source-specific budget requirement for interpretation.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a call missing provider/model identity, input/output schema or explicit replay mode.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/output-admission` (Open; implementation unassessed).
Required behavior: Keep model output tainted until declared schema validation succeeds; explicitly unstructured text cannot drive a trusted tool action.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a raw or explicitly unstructured response used to drive a trusted tool action.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/prompt-authority` (Open; implementation unassessed).
Required behavior: Prevent untrusted retrieved, user or tool data from acquiring system/developer instruction authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject retrieved/user/tool data promoted to developer or system instruction authority.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/replay-execution` (Open; implementation unassessed).
Required behavior: Require explicit replay modes in distributed/workflow AI contexts and emit/reuse nondeterministic step records instead of issuing undeclared live effects during replay.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a fresh provider or tool side effect substituted for a required recorded replay result.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/runtime-audit-ledger` (Open; implementation unassessed).
Required behavior: Emit model/tool/memory/review/refusal/repair/denial/replay execution ledgers, retaining canonical hashes where raw data cannot be stored.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject an invocation audit bundle missing a model/tool/memory/review/denial/replay event or a required permitted hash.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/emergency-review-bypass` (Open; implementation unassessed).
Required behavior: Permit review bypass only under the declared emergency policy and retain scoped actor/reason/action records; bypass does not create capabilities.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject bypass without declared scoped emergency policy, actor/reason/action audit or the independent capability grant.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/payload-bound-review-gate` (Open; implementation unassessed).
Required behavior: Execute a human-review-required action only under the matching actor/role/action/payload/policy review record with its evidence hash, while it is unexpired and unrevoked; reject absent records, changed payloads, insufficient roles and missing evidence hashes. Denial remains a typed workflow outcome.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject absent, expired, revoked, wrong-role or changed-payload review, and follow the typed denial branch.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/review-audit-ledger` (Open; implementation unassessed).
Required behavior: Record review requests, decisions and lifecycle transitions with protected payload redaction and canonical hashes.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject a review lifecycle record with missing actor/payload/evidence hash or protected raw data.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/review-declaration-artifact` (Open; implementation unassessed).
Required behavior: Compile the typed human-review declaration with action schema, role/principal, displayed evidence, payload hash, lifecycle, replay and emergency policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject a review declaration missing action/evidence schema, role, lifecycle, replay or emergency binding.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/review-replay-policy` (Open; implementation unassessed).
Required behavior: During replay reuse a valid recorded review decision unless the declared replay mode requires a live human decision.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject live review in a mode requiring recorded review, or recorded reuse with changed payload/policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/typed-reviewer-evidence` (Open; implementation unassessed).
Required behavior: Display enough typed action evidence through the UI/API for the reviewer to understand what is being authorized.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject an action API/UI that conceals the typed payload, evidence hash or action consequences from the reviewer.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/generated-source-trust` (Open; implementation unassessed).
Required behavior: Treat model-generated source as untrusted until the ordinary compiler validation and required review complete.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject execution of generated source before ordinary reader, macro, type, effect, profile and safety validation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/incident-reconstruction` (Open; implementation unassessed).
Required behavior: Emit sufficient prompt/model/tool/policy/retrieval/review/result lineage to reconstruct a malicious or denied action without leaking protected raw data.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject an incident bundle with an unjoinable prompt/tool/memory/policy/workflow decision or leaked protected body.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/privileged-defense-gate` (Open; implementation unassessed).
Required behavior: Require privileged AI releases to pass applicable prompt-injection, unauthorized-tool, secret and policy-bypass defense probes.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject privileged promotion with missing, stale or failing injection/tool-escalation/exfiltration/policy-bypass probes.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/runtime-escalation-monitor` (Open; implementation unassessed).
Required behavior: Record denied tool escalation, policy override attempts and protected-data exposure attempts in runtime monitoring records.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject an audit result that omits denied escalation, policy override or protected-data exposure attempts.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/secret-access-policy` (Open; implementation unassessed).
Required behavior: Permit secret access only under explicit scoped policy and reject protected-data exfiltration through prompts, tools, memory or logs.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject secret exfiltration via prompt, tool, memory or log without explicit scoped policy and grant.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/tool-authority-agreement` (Open; implementation unassessed).
Required behavior: Deny a model-requested tool action unless tool declaration, agent toolset, deployment grants and runtime capability handle all agree.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject a requested tool if declaration, agent toolset, package/deployment grant, scoped runtime handle or policy disagrees.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/untrusted-instruction-boundary` (Open; implementation unassessed).
Required behavior: Keep repository/web/retrieved/tool/model text in its declared untrusted data role and prevent instruction or policy override.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject repository/web/file/package/retrieved/tool/model data that changes instructions, grants or policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/credential-binding` (Open; implementation unassessed).
Required behavior: Load provider credentials from a declared secret source and reject missing or unsafe credential bindings.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject missing or ambient credential binding and any credential value copied into a forbidden artifact.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/embedding-shape-admission` (Open; implementation unassessed).
Required behavior: Reject an embedding provider whose vector dimension differs from the consuming memory manifest.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject embedding output whose vector dimension differs from the consuming memory manifest.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/external-spend-preflight` (Open; implementation unassessed).
Required behavior: Deny an actual live provider call before issuance when its known external spending limit is already exceeded; retain max-cost-usd and observed cost metadata.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject known-over-limit external spend before a live request, retaining max-cost policy and observed cost facts.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/failure-classification` (Open; implementation unassessed).
Required behavior: Classify provider failures as retryable, permanent, spending/policy denied or schema invalid so callers observe the declared failure contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject provider failures collapsed into untyped exceptions instead of retryable/permanent/spend-denied/policy-denied/schema-invalid results.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/fallback-admission` (Open; implementation unassessed).
Required behavior: Admit a fallback only if requested mode, schema guarantee, retention/data policy and applicable evaluation gate remain satisfied; preserve external spending policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject fallback that changes mode/schema/retention/data/spend policy or lacks the applicable comparison evaluation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/floating-identity-upgrade` (Open; implementation unassessed).
Required behavior: Accept a floating provider/model identity upgrade only under an agent/workflow evaluation gate covering that new identity.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject floating model upgrades not covered by the agent/workflow evaluation gate for the new identity.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/live-ci-policy` (Open; implementation unassessed).
Required behavior: Reject live provider calls in CI unless package/build policy explicitly permits them.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a live CI provider request without explicit package/build allowance.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-call-ledger` (Open; implementation unassessed).
Required behavior: Emit request/response, retry/fallback, drift, redaction and validation records with the required source/agent/prompt/schema/tools/handle/parameter/result identities and observed usage/cost/latency.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject call records missing source/agent/prompt/schema/tool/handle/parameter/response/refusal/validation/usage/redaction identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-effect` (Open; implementation unassessed).
Required behavior: Require :ai/model-call or :ai/embedding for an actual provider operation.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject inference or embedding without its declared AI effect.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-handle` (Open; implementation unassessed).
Required behavior: Reject a provider call without the provider capability handle even when network access is granted.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a model call with network access but no scoped provider capability handle.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-manifest` (Open; implementation unassessed).
Required behavior: Normalize provider/model declarations from package, deployment or local-profile inputs into one compiler-visible package artifact with the declared adapter, endpoint, modes, representability, schema, safety and retention contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a normalized provider/model artifact missing adapter, endpoint, mode, version, schema, safety or retention requirements.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-retention-admission` (Open; implementation unassessed).
Required behavior: Reject provider use when its data retention behavior conflicts with the active no-retention policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a retaining provider under a no-retention policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/replay-model-identity` (Open; implementation unassessed).
Required behavior: Record the complete provider/model/version/endpoint/adapter/tokenizer/output/safety-retention identity; audit-sensitive pinned replay rejects any mismatch or undeclared reproducibility-affecting request parameter.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject pinned replay after model/version/endpoint/adapter/tokenizer/output/safety-retention drift or undeclared reproducibility parameters.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/secret-and-no-store-isolation` (Open; implementation unassessed).
Required behavior: Prevent provider credentials and no-store data from leaking into prompt/log/memory/eval/replay records and omit forbidden raw request/response bodies while preserving canonical hashes and validation facts.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject credentials/no-store bodies entering prompt/log/memory/eval/replay records; retain only policy-permitted hashes and validation facts.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/structured-mode-contract` (Open; implementation unassessed).
Required behavior: Declare and honor whether structured-output guarantees come from provider enforcement, runtime validation or both; reject unsupported modes and report validation failures against that mode.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject unsupported output mode or a response admitted without the declared provider/runtime validation guarantees.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/tool-mediation-contract` (Open; implementation unassessed).
Required behavior: Declare provider-native, Gravity-mediated or runtime-adapted tool execution and expose its effects instead of hiding tool authority in the adapter.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject provider-native or adapted tool execution whose mediation mode, effects or authority are hidden.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/input-schema-admission` (Open; implementation unassessed).
Required behavior: Reject an agent/workflow prompt without its declared input schema.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject an agent/workflow prompt without its declared input schema.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/output-schema-admission` (Open; implementation unassessed).
Required behavior: Reject a response consumed by trusted code without its declared output schema, and prevent the model from choosing an arbitrary output schema identity.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject trusted code consuming a response with no declared output schema or a model-selected arbitrary schema.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/partial-output-admission` (Open; implementation unassessed).
Required behavior: Reject partial output unless the chosen output schema explicitly defines a legal partial state.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject partial output unless the declared schema admits that partial state.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/prompt-artifact` (Open; implementation unassessed).
Required behavior: Compile a syntax-derived typed prompt artifact with declared input/output-or-unstructured schema, authority/taint/tool/provider/validation/compatibility references rather than treating it as an arbitrary string.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject a prompt treated as an arbitrary string without source/schema/authority/taint/tool/provider/validation/compatibility identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/prompt-evolution` (Open; implementation unassessed).
Required behavior: Classify prompt evolution as additive, breaking or eval-required, rejecting a compatibility claim that changes output meaning.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject a compatibility claim that changes output meaning without the required breaking/evaluation disposition.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/prompt-no-store` (Open; implementation unassessed).
Required behavior: Reject storing response bodies in an active no-store context while preserving permitted validation/hash records.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject raw response-body storage in a no-store context.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/refusal-routing` (Open; implementation unassessed).
Required behavior: Represent provider refusal in the output type or declared workflow branch and follow that branch.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject provider refusal treated as success instead of its declared output type or workflow branch.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/render-identity` (Open; implementation unassessed).
Required behavior: Record the prompt artifact hash and actual rendered-input hash for each rendered prompt.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject rendering records whose prompt artifact hash or actual rendered-input hash is missing or substituted.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/repair-recording` (Open; implementation unassessed).
Required behavior: Permit repair only under declared repair policy and record each attempt and final validation outcome; validation failure can branch only to declared repair/retry/review/failure handling.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject repair outside declared policy, an unrecorded attempt or validation failure escaping its typed retry/review/failure branch.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/repair-termination-policy` (Open; implementation unassessed).
Required behavior: Resolve the source-specific maximum repair attempt rule and reject illegal repair loops without inventing a replacement synthetic compiler work quota.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject a mixed-clause disposition that removes validation/refusal/data policy with synthetic attempt accounting, or substitutes a renamed compiler quota.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/secret-to-provider-policy` (Open; implementation unassessed).
Required behavior: Reject secrets in rendered prompts except when the policy explicitly permits that secret-to-provider flow.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject rendered secrets without an explicit secret-to-provider policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/taint-propagation` (Open; implementation unassessed).
Required behavior: Carry interpolated-value taint through rendering and per-field output validation until the policy for intended use is satisfied.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject interpolated-value or output-field taint disappearing before intended-use policy validation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/tool-visibility` (Open; implementation unassessed).
Required behavior: Expose only prompt-declared tool schemas under the declared tool-call policy; reject undeclared tool exposure.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject exposing tools not declared by the prompt toolset/policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/typed-output-admission` (Open; implementation unassessed).
Required behavior: Validate tainted provider output by declared format/schema, required canonicalization, per-field taint and intended-use data policy before converting it to a typed core value or allowing tools/workflow/generated-source use.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject conversion to typed core, workflow branch, tool argument or generated-source input before format/schema/canonicalization/taint/use-policy validation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/foreign-error-normalization` (Open; implementation unassessed).
Required behavior: Convert foreign implementation failures to the declared typed tool error contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject foreign exceptions escaping the declared typed tool result contract.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/nonidempotent-retry` (Open; implementation unassessed).
Required behavior: Do not retry or replay a non-idempotent effect without its declared idempotency/compensation contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject a non-idempotent retry/replay without its declared idempotency or compensation contract.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/privileged-tool-review` (Open; implementation unassessed).
Required behavior: Stop privileged tool actions until the declared human-review policy is satisfied without treating review as capability creation.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject privileged execution before required review, or treating review as a new capability grant.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-authority-agreement` (Open; implementation unassessed).
Required behavior: Authorize a tool call only when declaration, package/deployment grant, agent toolset and scoped runtime implementation handle agree.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject a requested tool if declaration, agent toolset, package/deployment grant, scoped runtime handle or policy disagrees.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-effects` (Open; implementation unassessed).
Required behavior: Expose every implementation effect at the tool boundary; hidden effectful adapter behavior is rejected.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject any effect performed by a tool implementation but absent from its declaration.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-manifest` (Open; implementation unassessed).
Required behavior: Compile typed tool declarations with declared input/output schemas and public model-facing descriptions that expose neither secret values nor implementation handles.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject a tool with missing input/output schemas or a model-visible description exposing credentials, internal policy or implementation handles.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-redaction` (Open; implementation unassessed).
Required behavior: Emit tool call and failure records with declared redaction, preserving auditable identities without leaking secrets.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject tool success/failure ledgers leaking secret/private/no-store fields or omitting required audit identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-result-admission` (Open; implementation unassessed).
Required behavior: Validate tool results before they become trusted program values or downstream action inputs.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject an unvalidated tool result entering an agent/workflow or trusted downstream sink.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-eval-admission` (Open; implementation unassessed).
Required behavior: Require production agent deployment to bind the applicable passing evaluation evidence.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject production deployment without passing evidence for its release class and exact agent dependencies.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-external-call-policy` (Open; implementation unassessed).
Required behavior: Interpret and enforce the specific agent call/retry/wall-time/token/spend policy without substituting removed synthetic compiler accounting for actual external controls.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject a source disposition that keeps synthetic agent call/retry work allowances or removes actual external spend/account, effect or authority policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-ledger-identity` (Open; implementation unassessed).
Required behavior: Bind every agent execution ledger to the exact agent artifact and dependency identities for reconstruction and replay.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject any model/tool/memory/review/workflow ledger event not bound to its agent artifact and dependency identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-manifest` (Open; implementation unassessed).
Required behavior: Compile the agent's complete model/prompt/tool/memory/policy/eval/workflow dependencies into its normalized artifact and reject incomplete bindings.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject an agent missing a declared model, prompt, tool, memory, policy, evaluation or workflow dependency.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-memory-access` (Open; implementation unassessed).
Required behavior: Require distinct declared read/write memory access modes and matching runtime authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject a memory write through an agent's read-only binding or a missing separate write capability.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-output-admission` (Open; implementation unassessed).
Required behavior: Validate agent output before a typed consumer or tool action may use it.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject an unvalidated agent result used as typed data or tool input.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-tool-authority` (Open; implementation unassessed).
Required behavior: Constrain tool use to the intersection of the agent-declared toolset and actual grants; neither a model plan nor an agent declaration enlarges authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject a plan or declaration used to enlarge the intersection of declared toolset and actual grants.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/generated-code-reentry` (Open; implementation unassessed).
Required behavior: Send agent-generated source through the ordinary compiler checks before use.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject agent-generated source used without normal compiler validation and preserved generated origin.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a6/validated-branch-input` (Open; implementation unassessed).
Required behavior: Allow workflow branches only on validated typed values, never raw model output.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed graph/state/event-log artifacts with branch, compensation, review-payload, replay and retry transition evidence.
Negative control: Reject a workflow branch selected from raw, unvalidated model output.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a6/workflow-retry-policy` (Open; implementation unassessed).
Required behavior: Preserve the declared retry policy and records while resolving any source-specific accounting clause separately.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed graph/state/event-log artifacts with branch, compensation, review-payload, replay and retry transition evidence.
Negative control: Reject a retry that loses its declared replay/idempotency/compensation record or turns removed accounting into a new compiler allowance.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a6/workflow-review-binding` (Open; implementation unassessed).
Required behavior: Bind workflow human-review decisions to the exact executed payload and declared review/replay policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed graph/state/event-log artifacts with branch, compensation, review-payload, replay and retry transition evidence.
Negative control: Reject workflow review reuse after payload change or outside the declared review/replay policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/deletion-index-coherence` (Open; implementation unassessed).
Required behavior: Apply memory deletion to stored items, index state and the required audit ledger consistently.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject deletion/redaction that leaves a retrievable stale item or inconsistent index/ledger.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/embedding-authority` (Open; implementation unassessed).
Required behavior: Require actual embedding-provider authority and a dimension-compatible embedding contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject embedding without the actual provider grant or with an incompatible dimension contract.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/memory-access-handles` (Open; implementation unassessed).
Required behavior: Require the distinct read and write handles for the requested memory operation; declarations do not supply ambient access.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject a read/write using a missing or wrong-mode scoped memory handle.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/memory-manifest` (Open; implementation unassessed).
Required behavior: Compile a typed memory store/item schema with provenance, partition, retention, index and embedding identity.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject a memory artifact missing item/metadata schema, provenance, partition, retention, indexing, embedding or replay policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/persistence-data-policy` (Open; implementation unassessed).
Required behavior: Reject protected-data persistence that violates retention, redaction, deletion or permitted data-class policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject protected-data embedding/persistence contrary to data-class, retention, redaction or deletion policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/reembedding-lineage` (Open; implementation unassessed).
Required behavior: Record old/new embedding and source identities when re-embedding; incompatible dimensions cannot reuse an index silently.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject re-embedding without source-item hash and old/new embedding identities, or silent incompatible-index reuse.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/ai-policy-artifact` (Open; implementation unassessed).
Required behavior: Compile an explicit production AI policy binding and preserve its input/output domains and decision rules.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject production AI policy without its explicit normalized input/output domains and rules.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/authority-change-compatibility` (Open; implementation unassessed).
Required behavior: Treat policy changes that broaden authority as security/compatibility changes requiring the declared review, rather than harmless metadata edits.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject authority-broadening policy changes released without compatibility and security review.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/emergency-override-policy` (Open; implementation unassessed).
Required behavior: Allow only explicitly scoped/time-bound emergency policy overrides and retain the actor, reason and action audit trail.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject unscoped, expired, unlogged or unauditable emergency policy override.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/fallback-evidence-policy` (Open; implementation unassessed).
Required behavior: Deny model/tool fallback when its policy or applicable evidence requirements are not met.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject a fallback missing required safety/retention/schema/spend policy or applicable evaluation evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/generated-code-admission` (Open; implementation unassessed).
Required behavior: Require ordinary compiler validation of AI-generated source before policy permits its use.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject generated code executed, published or used as a dependency before ordinary compiler validation and required tests.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/output-and-instruction-boundary` (Open; implementation unassessed).
Required behavior: Keep model/tool/retrieved output tainted data until permitted validation; data cannot change instructions or grant authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject tainted model/tool/retrieved data becoming trusted instructions or authority without its permitted validation boundary.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/policy-composition` (Open; implementation unassessed).
Required behavior: Compose policy layers deterministically with deny precedence; a later layer may narrow but cannot silently broaden authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject cyclic/nondeterministic policy, an allow overriding an equal/narrower deny, or silent authority broadening by a later layer.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/policy-decision-ledger` (Open; implementation unassessed).
Required behavior: Record each AI action's policy inputs, decision, rule and artifact identity so approval or denial can be reconstructed.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject an action record missing principal/agent/effect/grant/data/taint/review/eval/replay facts, rule or decision.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/review-rule-enforcement` (Open; implementation unassessed).
Required behavior: Apply typed human-review role/payload/expiry/revocation/replay rules to the action covered by the policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject a policy decision authorizing the wrong action/role/payload or expired/revoked review.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/changed-artifact-comparison` (Open; implementation unassessed).
Required behavior: Report which model/prompt/tool/policy artifacts changed and the compared evaluation result identities, not an unscoped aggregate score.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject evaluation comparisons that omit a changed model/prompt/tool/memory/policy artifact or result identity.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/eval-dataset-artifact` (Open; implementation unassessed).
Required behavior: Version datasets with schema, provenance and split identities so consumed evaluation input is reproducible and attributable.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject datasets without schema, version, provenance or split identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/eval-redaction` (Open; implementation unassessed).
Required behavior: Redact protected data from evaluation datasets/reports while retaining the permitted hashes and provenance needed to reproduce the claim.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject evaluation samples or reports storing protected raw data contrary to policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/live-eval-policy` (Open; implementation unassessed).
Required behavior: Permit live provider evaluation only under explicit credentials, real external spending/account and retention/data policy; recorded/mocked evaluation is identified as such.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject live evaluation without explicit credentials and actual external spend/account/retention policy, or a mock result mislabeled live evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/nondeterministic-eval-evidence` (Open; implementation unassessed).
Required behavior: Declare seeds, trace reuse or repeat/variance evidence for nondeterministic evaluation instead of claiming exact replay from uncontrolled live results.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject a nondeterministic evaluation claiming reproducibility without seeds, trace reuse or declared repeat/variance evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/safety-probe-coverage` (Open; implementation unassessed).
Required behavior: Run context-relevant safety probes covering prompt injection, tool misuse, policy denial, schema failure, refusal handling and protected-data leakage. Required release evaluation fails closed for missing, stale or incompatible evidence.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject a release safety claim without matching injection/tool-misuse/denial/schema/refusal/leakage probes and compatible evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion profile-gap/ai/profile-tool-contract (Open): Input: AI model/tool declaration, provider policy, typed arguments, caller/deployment grants, side-effect class and applicable review record. Accept: Admit and execute the declared model request through its provider; validate tool input/output schemas and side-effect/review policy before a tool action; re-enter ordinary macro/type/effect/capability/safety/package/profile checking before generated Gravity code can execute. Review approval never manufactures a missing capability. Reject: Missing provider/effect/policy, tool schema or grant; write/destructive/shell/deployment/package/secret action without applicable review; raw memory/FFI in agent logic; generated code executed before compiler validation. Required outputs: Agent/model/tool manifests, schema validation result, effect/grant/review decisions and generated-code validation/provenance record. The mixed original cost/token/call predicates require a source-specific billing-versus-removed-accounting decision; retain that Open policy outcome and all adjacent authority restrictions. Dependencies: schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping ai/profile-tool-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/ai/prompt-memory-boundary (Open): Input: Prompt messages with roles/provenance and retrieved/tool/memory/model values carrying trust, taint, retention/deletion/privacy and secret policy. Accept: Preserve each source-visible role and origin; validate model output for the particular sink before trusting it; enforce memory retention/deletion/privacy and trust on reads/writes; permit secret-bearing prompt/tool/provider/log disclosure only through explicit matching policy. Reject: Retrieved or tool content overriding privileged policy, unvalidated model output at a trusted sink, stale/deleted or cross-policy memory access, and unauthorized secret disclosure. Required outputs: Prompt provenance/role record, sink validation and taint result, memory policy/lifecycle evidence, redacted secret-policy denial diagnostics. Dependencies: runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping ai/prompt-memory-boundary; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/ai/replay-evaluation-artifacts (Open): Input: Agent execution with model/provider/prompt/tool/policy identities, review decisions, retry/error state and selected audit/replay mode. Accept: Emit the execution ledger with prompt/message and generated-code digests, tool inputs/outputs, review/policy/nondeterminism/error/retry records; replay required operations from matching records. Expose an evaluation hook that produces separately identified fixtures/results without granting authority. Reject: Replay/audit-required execution missing required records, digest or identity substitution, or evaluation results used to bypass capability/safety/review checks. Required outputs: Model/tool call trace, replay/audit log schema and records, generated-code digest, independently identified evaluation result. Dependencies: workflow, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping ai/replay-evaluation-artifacts; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

Criterion `domain/support-agent-integration`: Execute and evaluate declared support triage with prompt provenance, structured-output/taint, durable graph/replay, tool policy and required refund/review transitions. Accepted fixture: Run support triage with prompt provenance, structured validated outputs, retrieval trust, durable replay, tool policy and explicit refund human-review transition. Negative fixture: Reject prompt policy escalation, unvalidated output, missing tool grant or write tool without required review. Required evidence: agent/tool/prompt/memory/review graph, eval and replay reports; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, workflow outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Source-qualified acceptance: Source obligation `language/facet/emit-agent-plan`: Emit an agent facet's prompt/tool/model/evaluation/trace/guardrail artifacts with explicit model/tool effects and capabilities. Accept: Compile an explicitly activated agent facet into prompt artifacts, tool schemas, model-call declarations, evaluation hooks, trace requirements and guardrail checks; retain model/tool effects and capabilities without hidden runtime privilege. The SupportAgent example is an artifact-generation fixture, not authorization for live calls. Reject: Reject missing prompt/tool/model/evaluation/trace/guardrail products, inactive/unsupported facet use or authority inferred from prompt text. Required outputs: Versioned source-bound agent artifact bundle and accepted fixture plus missing-product/effect/grant rejection diagnostics. Dependencies consume versioned facts/receipts from expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime; they do not require blanket component completion. Implementation status remains Open.
<!-- domains-task-projection/ai:end -->

## GPU

Key `gpu`; owner role: accelerator compiler maintainer. Purpose: preserve kernel
memory-space and synchronization semantics. Scope: kernel IR, workgroup shape,
barriers, vectorization and declared target features; not universal accelerator
support. Input: checked kernels and target manifest. Output: GPU IR, kernel
artifact and memory/synchronization diagnostics. Governed by
[GPU contract](../phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md).

<!-- domains-task-projection/gpu:start -->
Outcome: kernels execute on the adopted target with validated memory/barrier
behavior. Remaining tasks, all open: adopt target and kernel ABI; implement GPU
IR/checking; lower kernel and host dispatch; execute numeric/result and illegal
memory-space/barrier fixtures. Yes/no acceptance: do kernels produce the
specified results? Are invalid synchronization and target features rejected?
Integration: MIR/Legality, Lower/Emit, explicit Runtime dispatch. Decision owner:
accelerator maintainer; recommendation: one named reference target first,
additional targets only by their own conformance evidence. Target choice is
not silently adopted by this roadmap.

P11 profile interface: emit host/device argument/address-space/layout/alignment/alias/lifetime, transfer/synchronization and feature/workgroup/launch/occupancy records. Reject host services in kernels, use-after-free/double release/unsynchronized host reads, unsupported launch/features and invalid barriers; safe sharing needs provider evidence. Preserve precision/rounding/fusion/denormal/approximation/provider/certificate facts through lowering or diagnose. ABI production, kernel/launch checks, buffer validity and actual Runtime transfers remain separate interfaces; a graph is not executed transfer evidence.

Criterion profile-gap/gpu/host-device-boundary (Open): Input: Separate host orchestration and GPU kernel artifacts with argument address-space/layout/alignment/alias/lifetime and declared target feature/workgroup/grid/occupancy/launch configuration. Accept: Validate kernel-only legality, construct the typed host/device ABI boundary and transfer/lifetime records, and check target launch/features independently. Host profile remains explicit; device orchestration cannot import ambient host semantics into a kernel. Reject: Host IO/reflection/eval/allocation/GC/exceptions/ordinary threads in kernels, unsupported recursion/dispatch, missing argument or transfer boundary, invalid target or launch configuration. Required outputs: Host/device and target/launch/occupancy manifests, kernel IR, typed argument ABI and accepted/rejected kernel/launch/feature fixtures with kernel/buffer/address-space/origin context. Dependencies: backend-interface, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping gpu/host-device-boundary; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/gpu/memory-synchronization (Open): Input: GPU buffers/slices and transfer/queue/kernel/barrier graph with ownership/lifetime/address-space/alignment/layout/alias/visibility and sharing-provider facts. Accept: Treat buffers as linear unless provider-backed sharing is proved; validate use/release and host/device visibility; preserve launch/fence/workgroup barrier/atomic/queue/transfer-completion order and address spaces through backend lowering. Actual transfer execution consumes the validated graph separately. Reject: Use-after-free, double release, unsynchronized host read, invalid device alias, missing barrier/queue dependency/transfer completion or unsupported host allocation. Required outputs: Lifetime report and transfer/synchronization graphs, accepted ownership/transfer/barrier cases and P11-DEVICE-MEMORY/TRANSFER/SYNC/ALIAS/RAW diagnostics with exact buffer/address-space/edge/target/origin. Dependencies: legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping gpu/memory-synchronization; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/gpu/numeric-mode-contract (Open): Input: GPU/EFIR numeric operations with declared precision, rounding, fusion, denormal, approximation error, provider, certificate/validation and device feature facts. Accept: Check the selected device implementation and target mapping against every declared numeric/accuracy fact and preserve the certificate contract or emit a diagnostic. Reject: Implicit backend approximate math, missing mode/provider/certificate or accuracy record, undeclared denormal/fusion change and unsupported target features. Required outputs: Math-mode/feature manifest and approximation certificate bundle, accepted/failed numeric-preservation cases and P11-MATH/TARGET-FEATURE context with kernel/target/origin/certificate. Dependencies: math, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping gpu/numeric-mode-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

Criterion `domain/gpu-dispatch`: Dispatch declared kernels using explicit buffer state, transfer, layout, ownership and synchronization contracts. Accepted fixture: Dispatch a declared kernel through explicit host/device buffer layout, ownership/lifetime, transfer state and synchronization contracts. Negative fixture: Reject implicit transfer, invalid lifetime/aliasing or unsynchronized device-state use. Required evidence: host adapter, launch descriptor and transfer/synchronization graphs; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume backend-gpu, lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/gpu-kernel-validation`: Validate/lower GPU kernels under explicit target/features/geometry/memory-space/barrier/atomic and numeric/certificate constraints. Accepted fixture: Validate selected target/features, launch geometry, address spaces, layout, barriers/atomics and numeric/certificate mode before kernel lowering. Negative fixture: Reject host effects, unsupported recursion/dynamic allocation, invalid launch geometry or unjustified fast math. Required evidence: checked GPU IR, device memory and numeric/feature reports; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume backend-gpu, lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/gpu-reduction-integration`: Demonstrate declared reduction against reference with selected numeric ordering and target/launch/layout/performance evidence. Accepted fixture: Run the selected reduction with explicit deterministic or relaxed order and compare device output with CPU/MIR reference using pinned launch/layout/input context. Negative fixture: Reject deterministic-mode nondeterminism, host IO, invalid synchronization or unsupported target feature assumptions. Required evidence: kernel/host artifacts, reference comparison and performance context; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume backend-gpu, lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.
<!-- domains-task-projection/gpu:end -->

## UI

Key `ui`; owner role: UI compiler/runtime maintainer. Purpose: preserve typed UI
state, event and rendering boundaries. Scope: UI IR and declared backend adapter,
not all web/mobile applications. Input: checked component/state/event forms and
platform capabilities. Output: UI IR, render/event artifacts and source maps.
Governed by [UI domain contract](../phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

<!-- domains-task-projection/ui:start -->
Outcome: an actual UI artifact renders, handles typed events and enforces
capabilities through the adopted adapter. Remaining tasks, all open: lock UI
IR/state/event contract; implement lowering and rendering adapter; connect
runtime events and source maps; execute render/update and invalid-event/unsafe
DOM/authority rejection fixtures. Yes/no acceptance: do state transitions and
rendering follow typed semantics? Are unsafe or unauthorized actions rejected?
Integration: Types/MIR, Lower/Emit, Runtime and Stdlib UI. Decision owner: UI
maintainer; recommendation: explicit adapter contract rather than browser host
semantics defining the language. Backend choice remains an explicit decision.

DOM6 browser acceptance requires the ticket-list slice, typed API/schema drift
checks and source-mapped loading, validation and unsafe-sink failures. DOM7
mobile acceptance separately requires an adopted platform adapter with permission,
lifecycle, main-thread/actor, null/error and taint contracts; background timeout,
retry and cancellation; local schema/migration/encryption/retention/conflict
policy; and keychain/keystore grants and redaction. The offline ticket fixture
must exercise permission denial, offline edit, sync retry and crash source maps
on a named simulator/device. Dependencies: Schema generated clients and local
migrations, Auth grants, Runtime lifecycle and Backend mobile artifacts. Browser
evidence cannot establish mobile conformance; both remain Open.

Criterion `domain/mobile-background-lifecycle`: Execute mobile background work under declared suspension/lifecycle, timeout/retry/network/storage/cancellation rules. Accepted fixture: Run mobile background work under suspension/lifecycle, timeout/retry, network/storage grants and cancellation contracts. Negative fixture: Reject hidden background execution, invalid lifecycle assumptions and missing cancellation/failure mapping. Required evidence: background lifecycle manifest and simulator transition/error trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/mobile-offline-storage`: Persist and synchronize mobile local data using declared schemas, migrations, encryption/retention and conflict policy. Accepted fixture: Persist and sync local values with schema/migration, encryption/storage provider, retention and explicit conflict handling. Negative fixture: Reject schema-less storage, unsafe migration, missing encryption/provider policy and silent conflict loss. Required evidence: offline schema/migration bundle and offline-edit/sync fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/mobile-platform-adaptation`: Adapt mobile APIs using declared permissions, lifecycle, main-thread/actor, null/error and taint rules. Accepted fixture: Adapt platform APIs with explicit permissions, capabilities, lifecycle, main-thread/actor, null/error and taint behavior. Negative fixture: Reject missing permissions, unchecked platform nulls and lifecycle/thread-unsafe UI callbacks. Required evidence: platform binding/permission manifest and simulator adapter fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/mobile-secret-provider`: Access mobile secrets through a declared keychain/keystore or equivalent provider with grant/redaction policy. Accepted fixture: Read/write secrets through declared keychain/keystore or equivalent provider grants with redaction and failure policy. Negative fixture: Reject ambient secret access, plaintext/unsafe storage or secret disclosure through crash/log artifacts. Required evidence: secret-provider manifest and grant/redaction/failure fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/mobile-ticket-integration`: Demonstrate the offline mobile ticket slice with permission/lifecycle/offline/crash cases on a named simulator/device. Accepted fixture: Run the offline ticket screen with local schema, migration, sync, lifecycle/threading, network and notification permission contracts. Negative fixture: Reject permission denial bypass, hidden background work, unchecked platform null and unsafe storage. Required evidence: app/binding/resource manifests and simulator permission/offline/retry/crash tests; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/ui-rendering`: Lower typed UI/component/state/event forms and execute declared rendering/event adapters without unsafe or unauthorized DOM actions. Accepted fixture: Lower typed components/state/events and execute the adopted renderer with source maps, event schemas and capability/taint-safe DOM boundaries. Negative fixture: Reject invalid events, unsafe rendering sinks, ambient globals and generated code bypassing normal checks. Required evidence: typed UI IR, rendered/event trace and source-map diagnostics; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/web-ticket-integration`: Render/update the declared ticket-list browser slice with source maps, validated inputs and API/schema drift rejection. Accepted fixture: Render/update the ticket-list route with typed props/API clients, validated network/user input and browser source maps. Negative fixture: Reject schema drift, untyped imports, ambient browser authority and unsafe HTML/script sinks. Required evidence: JS/declaration/client artifacts and loading/validation/input/browser fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.
<!-- domains-task-projection/ui:end -->

## Rollup and nonclaims

Integration reads the named outcomes above, not a duplicate task list. None is
closed by this proposal. Shared semantic fact transport does not merge domain
ownership, and a future target decision does not block unrelated compiler work.
Existing planning references remain traceability; new implementation namespaces
should use these semantic subsystem names without whimsical codename prefixes.


## Additional integration task views

These nine Open task interfaces implement declared application slices within Domains. They add no completed component or product claim.

### Typed automation and action audit

<!-- domains-task-projection/automation:start -->
Outcome: Typed automation and action audit with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/automation-plan-audit`: Produce declared dry-run/audit records of planned writes/deletes/deployments/commands without hiding the eventual effect contract. Accepted fixture: Produce a dry-run of writes/deletes/deployments/commands and compare it with the authorized actual action audit. Negative fixture: Reject omitted effects, misleading dry-run authority or unredacted secret-bearing audit fields. Required evidence: dry-run plan and source-linked action audit; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/file-transform-integration`: Demonstrate declared typed file transform with root/path/command authority and invalid-input rejection. Accepted fixture: Transform declared input/output files with argument/path schemas, granted roots, source-linked actual audit and a dry-run. Negative fixture: Reject malformed input, root escape, missing write grant and tainted command construction. Required evidence: script package, path/argument schemas, dry-run and file-change audit; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime. Decision owner: Typed automation and action audit maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/automation:end -->

### Deterministic chain and programmable accounts

<!-- domains-task-projection/blockchain:start -->
Outcome: Deterministic chain and programmable accounts with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/account-session-delegation`: Constrain programmable-account session keys and delegated execution by identity, entrypoints, grants, storage/reentrancy, replay domain, nonce and revocation policy. Accepted fixture: Execute a scoped session call and revoke its delegation across the declared account, entrypoint, nonce and replay domain. Negative fixture: Reject revoked, cross-domain, expired or storage/reentrancy-exceeding delegation. Required evidence: session authorization and revocation trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/blockchain-token-transfer-integration`: Demonstrate typed token transfer with selected chain/account/ordering/resource/state/authorization and failure evidence. Accepted fixture: Transfer tokens under pinned chain/account/ABI/state/authorization/ordering rules and prove conservation and insufficient-balance behavior. Negative fixture: Reject unauthorized mutation, unchecked overflow, nondeterminism and incompatible upgrade. Required evidence: contract/ABI artifact, conservation fixture and upgrade report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/chain-state-transition`: Execute typed, versioned deterministic contract state transitions with declared numeric/canonical/storage/protocol-resource and authorization semantics. Accepted fixture: Execute deterministic typed/versioned state transitions with explicit arithmetic, serialization, canonicalization, storage and selected protocol resource semantics. Negative fixture: Reject host nondeterminism, unchecked arithmetic, unknown ABI/state schema or unauthorized mutation. Required evidence: transition trace, state/ABI schema and protocol conformance report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/eip7702-delegation`: Validate and apply selected EIP-7702 authorizations with tuple/signing domain, delegate code/loading, nonce/replay/duplicate/revocation/failure and client accounting semantics. Accepted fixture: Validate the selected EIP-7702 authorization tuple/signing domain, code/delegate loading, nonce/replay/revocation and client gas semantics. Negative fixture: Reject wrong chain/domain/nonce, duplicate or stale authorization and incompatible delegation/failure behavior. Required evidence: pinned EIP-7702 profile and authorization/client fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/erc4337-operation-processing`: Implement the selected ERC-4337 EntryPoint/UserOperation/account validation flow with explicit domains/nonces/factory/aggregation/bundler/simulation and wallet/client contracts. Accepted fixture: Execute selected EntryPoint/UserOperation validation, nonce/domain/factory, aggregation, bundler/simulation and wallet encoding contracts. Negative fixture: Reject stale or missing profile/version/schema, invalid signature/validation time and incompatible chain/client assumptions. Required evidence: pinned ERC-4337 profile, encoding and operation-processing fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/erc7579-module-lifecycle`: Implement selected modular-account interface/type/module install/configure/uninstall/revoke and authorization-hook semantics. Accepted fixture: Install, configure, invoke, uninstall and revoke declared module types and authorization/hooks with account compatibility metadata. Negative fixture: Reject invalid module interfaces/types, unauthorized hooks and invocation after revocation. Required evidence: ERC-7579 profile, module manifest and lifecycle fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/paymaster-enforcement`: Validate sponsor/paymaster operations and post-operation behavior under declared deposit/stake/quota/gas/anti-drain policy. Accepted fixture: Validate sponsorship and post-operation accounting under named payer, selected protocol deposit/stake/quota/gas and anti-drain/failure policy. Negative fixture: Reject unbounded sponsorship, missing postOp accounting, unauthorized payer use or incompatible EntryPoint assumptions. Required evidence: paymaster profile, sponsorship/anti-drain and failure fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/transaction-order-safety`: Model order dependence explicitly and produce evidence that declared visibility/reordering/MEV mitigations preserve invariants under the chosen chain/provider model. Accepted fixture: Model declared mempool visibility, ordering authority, sequencer/builder/private-flow trust and MEV exposure with slippage/deadline/nonce/max-loss or equivalent mitigation evidence. Negative fixture: Reject missing ordering assumptions, unsafe private-flow fallback or invariant-breaking reordering. Required evidence: ordering/MEV report and adversarial ordering/provider-fallback fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries. Decision owner: Deterministic chain and programmable accounts maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/blockchain:end -->

### Cryptographic and protected-computation boundaries

<!-- domains-task-projection/crypto:start -->
Outcome: Cryptographic and protected-computation boundaries with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/credential-lifecycle`: Apply declared credential rotation/recovery/decommissioning/unknown-or-accepted signaling policy with audit records. Accepted fixture: Rotate, recover, decommission and signal unknown/accepted credentials under the declared account binding, custody and privacy policy. Negative fixture: Reject use of decommissioned credentials, unsupported recovery or privacy-leaking lifecycle records. Required evidence: redacted credential lifecycle and policy audit; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/encrypted-aggregation-integration`: Demonstrate encrypted aggregation using approved arithmetic and valid custody/parameter/noise/depth/precision/transcript/reveal evidence. Accepted fixture: Aggregate protected values through approved arithmetic with explicit custody, participants, scheme parameters, noise/depth/precision/transcript and reveal policy. Negative fixture: Reject implicit decrypt, secret-dependent branching, missing custody or operations outside concrete provider constraints. Required evidence: protected aggregation parameter/boundary manifests and test vectors; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/passkey-assertion`: Verify passkey assertions using origin/RP/account binding, fresh challenge/replay protection, signature and required presence/verification proof. Accepted fixture: Verify origin/RP/account binding, fresh challenge/replay protection, signature, user-presence and policy-required verified user evidence. Negative fixture: Reject challenge reuse, origin/RP mismatch, missing signature/verification proof and unsupported custody assumptions. Required evidence: redacted authentication transcript and credential-policy fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/passkey-registration`: Register credentials through fresh challenges, RP/origin/consent/storage policy and declared attestation/user-verification/trust/custody evidence. Accepted fixture: Register credentials with fresh challenge, RP/origin/consent/storage policy and explicit attestation trust, presence/verification, backup/sync and custody assumptions. Negative fixture: Reject undeclared trust roots, treating synced passkeys as device-bound and attestation tracking beyond privacy policy. Required evidence: registration transcript, WebAuthn/passkey manifest and privacy/replay fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/protected-disclosure-boundary`: Require explicit authorized audited encrypt/decrypt/reveal/reconstruct/attest/compare/branch/serialize/prompt/log/export boundaries with declared leakage/reveal policy. Accepted fixture: Authorize and audit encrypt/decrypt/reveal/reconstruct/attest/compare/branch/serialize/prompt/log/export operations with explicit reveal reason, participants and leakage state. Negative fixture: Reject implicit crossings, unsupported plaintext/ciphertext mixing and disclosures beyond declared output/metadata/timing/transcript policy. Required evidence: protected-boundary manifest and leakage/disclosure fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/protected-provider-execution`: Execute supported FHE/MPC/threshold/enclave/private operations while validating and updating scheme-specific noise/depth/scale/precision/transcript/participant state. Accepted fixture: Execute the declared FHE/MPC/threshold/enclave operation subset, validating and updating actual scheme noise/depth/scale/precision/transcript/participant state. Negative fixture: Reject unsupported operation or parameter/custody/adversary assumptions, stale state and exceeded concrete scheme constraints. Required evidence: provider parameter manifest, updated protected-state records and test vectors; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/protected-value-typing`: Type protected computation values/operations with explicit scheme, parameters, custody, participant/adversary and leakage state. Accepted fixture: Distinguish plaintext, ciphertext, shares, commitments, blinded/enclave and revealed values with scheme, parameters, key custody, participants/adversary and leakage metadata. Negative fixture: Reject unauthorized value-kind coercion, missing custody/leakage state and unsupported protected operations. Required evidence: typed protected-value report and positive/negative type fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/token-signing-integration`: Demonstrate token signing with declared key/randomness/provider policy, test vectors and denied secret disclosure. Accepted fixture: Sign tokens through the declared secret key/provider/algorithm and approved randomness when required, with scoped key access and test vectors. Negative fixture: Reject secret logging/prompting, ungranted key access and unsafe or undeclared provider boundary. Required evidence: signing provider/wrapper manifest, test vectors and redaction trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries. Decision owner: Cryptographic and protected-computation boundaries maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/crypto:end -->

### Formal proof verification and integration

<!-- domains-task-projection/formal:start -->
Outcome: Formal proof verification and integration with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/formal-bounds-integration`: Demonstrate declared checked bounds proof with property/source/counterexample and invalid-proof evidence. Accepted fixture: Emit a source/MIR-bound array bounds obligation, obtain proof or source-mapped counterexample, check the certificate and gate check elision on the valid proof. Negative fixture: Reject stale/invalid proof, hidden solver assumption or check elision before verification. Required evidence: bounds obligation, proof-check report and counterexample/elision fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/proof-checking`: Check proof artifacts against declared property, trust/provider/assumptions and exact source/artifact identity before proof-dependent use. Accepted fixture: Check a proof against the exact property, source/artifact/profile/target, trusted basis, provider and assumptions before dependent use. Negative fixture: Reject unverified or stale certificates, hidden solver assumptions and EML syntax identity used as semantic proof. Required evidence: claim/proof/assumption manifests and verifier acceptance/rejection report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/recursive-proof-chain`: Validate recursive/folding/IVC parent links, root state, step-relation hashes and stale-proof applicability. Accepted fixture: Verify recursive/folding/IVC parent links, accumulator/root state and source-bound step relation hashes before using the root result. Negative fixture: Reject missing parents, stale step proofs, invalid root state or incompatible setup/provider assumptions. Required evidence: proof-chain manifest, root verification and stale-chain fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/zk-relation-integration`: Demonstrate the declared checked zero-knowledge relation with intentional public outputs and rejected invalid/private-boundary cases. Accepted fixture: Emit and verify the selected relation with public/private/witness schemas, custody/disclosure policy, source/MIR hashes, setup/trust/key identity and intentional public outputs. Negative fixture: Reject malformed witness, hidden setup, stale proof/key/provider, invalid chain or contradictory privacy output evidence. Required evidence: relation/witness/setup/verifier artifacts, privacy and verification/rejection reports; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/zk-relation-verification`: Verify declared circuit/constraint/AIR/R1CS/Plonkish/zkVM relations with explicit public/private/witness/setup/trust/key/proof-system/verifier contracts. Accepted fixture: Check the declared circuit/constraint/AIR/R1CS/Plonkish/zkVM relation against public/private/witness, setup/trust, proof-system/provider and verifier-key contracts. Negative fixture: Reject missing/mismatched relation/input/key linkage, hidden setup/provider assumptions or unsupported privacy claims. Required evidence: relation/claim/setup/input manifests and verifier acceptance/rejection fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes. Decision owner: Formal proof verification and integration maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/formal:end -->

### Native performance slice

<!-- domains-task-projection/native:start -->
Outcome: Native performance slice with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/native-saxpy-integration`: Demonstrate declared scalar/vector SAXPY behavior against a mode-correct reference and reproducible target/input/layout/proof/measurement evidence. Accepted fixture: Emit scalar/vector SAXPY under explicit numeric mode, layout, target features and bounds/alias/alignment justification; compare with the mode-correct reference. Negative fixture: Reject proofless check elision, target UB, undeclared feature assumptions or unqualified fast math/benchmark claims. Required evidence: native artifact/ABI, proof bundle, differential and contextual benchmark report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume backend-llvm, opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: backend-llvm, opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors. Decision owner: Native performance slice maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/native:end -->

### Backend service integration

<!-- domains-task-projection/services:start -->
Outcome: Backend service integration with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/service-crud-integration`: Execute the declared CRUD service using typed API, query/transaction, runtime authority and failure evidence. Accepted fixture: Execute typed CRUD request/response/auth/error routes with schema/taint checks, prepared database transaction, grants and source-linked observability. Negative fixture: Reject schema-less route, ungranted filesystem/secret access, secret logging and broken transaction/failure mapping. Required evidence: service/API/config/transaction artifacts and service integration trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume query, schema outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: query, schema. Decision owner: Backend service integration maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/services:end -->

### Deterministic simulation slice

<!-- domains-task-projection/simulation:start -->
Outcome: Deterministic simulation slice with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/fixed-step-simulation-integration`: Execute declared fixed-step physics/simulation with timestep, numeric/determinism/replay/realtime allocation/blocking and target/frame semantics. Accepted fixture: Execute the fixed timestep physics/entity step with explicit numeric mode, deterministic recorded inputs, allocation/blocking policy and target timing evidence. Negative fixture: Reject hidden frame allocation, unrecorded gameplay randomness and unapproved numeric or plugin authority changes. Required evidence: simulation manifest, replay comparison and target/frame measurement report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume math, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: math, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries. Decision owner: Deterministic simulation slice maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/simulation:end -->

### Storage durability and recovery

<!-- domains-task-projection/storage:start -->
Outcome: Storage durability and recovery with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/storage-engine-durability`: Implement declared page/binary/checksum/WAL/concurrency/persistence-order and corruption/recovery behavior. Accepted fixture: Implement declared endian/alignment/page/checksum/WAL layout, concurrency, persistence ordering and fsync/provider crash recovery semantics. Negative fixture: Reject schema-less binary layout, unsupported durability assumptions or undetected corruption. Required evidence: storage layout/WAL schema, persistence-order and crash/corruption fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/wal-recovery-integration`: Demonstrate WAL-backed page persistence/recovery including declared crash/corruption behavior. Accepted fixture: Persist a WAL-backed page, restart/replay after declared crash points and detect or recover corruption under the declared provider ordering policy. Negative fixture: Reject false durability success, missing checksum detection or replay that violates the declared state transition. Required evidence: page/WAL artifacts and deterministic crash/replay/corruption report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries. Decision owner: Storage durability and recovery maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/storage:end -->

### Checked visual workflow lowering

<!-- domains-task-projection/visual:start -->
Outcome: Checked visual workflow lowering with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/visual-review-flow-integration`: Execute declared support workflow with typed edges, source-linked diagnostics and explicit policy-required human-review transition. Accepted fixture: Execute classify/policy/human-review/reply nodes with typed edges, source provenance and recorded review/model/tool results. Negative fixture: Reject untyped edges, missing required review or diagnostics without node/edge mapping. Required evidence: visual/source/workflow artifacts, review graph and mapped replay/rejection trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume workflow, schema, ui outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/visual-workflow-lowering`: Lower typed visual nodes/edges to ordinary checked workflow source/artifacts while exposing effects/review/retry/timeout/compensation/nondeterminism and round-tripping source diagnostics. Accepted fixture: Lower typed visual node/edge schemas to ordinary checked source/MIR/workflow artifacts exposing effects, grants, review, retry/timeout/compensation, nondeterminism and graph migration. Negative fixture: Reject hidden effects, visual-only authority, incompatible edges, skipped compiler checks and lost node/edge diagnostics. Required evidence: visual graph, generated-source/MIR map, schema/migration and policy/replay artifacts; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in specs/requirement-reconciliation/domain-gap-completion.edn. Dependencies consume workflow, schema, ui outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: workflow, schema, ui. Decision owner: Checked visual workflow lowering maintainer; person unassigned. Provider/target choice remains explicit.
<!-- domains-task-projection/visual:end -->
