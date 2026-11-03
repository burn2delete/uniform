# Build implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is claimed. All rows remain Open; transfer is not implementation
acceptance. Original outcome and integration boundaries remain in the roadmap.

<!-- build-implementation-tasks:start -->
- [ ] `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution` Implement project/lock normalization and constraint resolution

Status: Open

Evidence baseline: [Package report](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00569-stage0-compiled-plan-package-report.clj)

Closing evidence: Real dependency resolution with conflict, profile and capability rejection. Apply declared yank/revocation policy to selection and installation; retain signature/revocation status in locked records and explain each policy decision without silently replacing or erasing locked history. Reject revoked packages unless policy explicitly permits historical rebuild, and report revoked/yanked rejection with PKG5006.

<!-- build-governance-resolution:start -->
Source requirement: `governance/package-policy-consumption`, continuing the
existing source-family binding to this same task. See
[GOV10 conformance](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#conformance-criteria)
and [SAFE14 requirements](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md#requirements).

Additional source criteria, proposed current composition: Under SAFE14 and
GOV2/GOV10, apply the declared yank/revocation, advisory affected/patched-version,
compatibility and capability policy at both dependency selection and
installation before use. Consume complete version-bound advisory/yank and
accountable namespace/emergency decisions from the existing
`evolution/security-response`, `evolution/registry-yank`,
`evolution/namespace-decision` and `evolution/change-review` interfaces;
resolver output cannot produce or authorize those decisions.

Reject conflicting package safety manifests before compiling dependents;
safe/release builds require locked or equivalent pinned provenance. Validate
native source/digest/ABI/target/link/license/wrapper/capability records. Present
transitive runtime/build/unsafe/plugin/model/tool/secret/generated-artifact
authority diffs on updates without granting undeclared authority. Caller policy
checks unsafe summaries, current review, safe wrappers and certificates.
Exercise accepted policy application and rejection for active advisories,
revocation, hidden authority expansion, missing/stale/wrong-scope decisions and
unreviewed emergency lock changes. Preserve existing reproducible locks and
historical artifacts unless explicit emergency policy applies; reject silent
replacement or erasure. Retain PKG5006 and applicable SAFE14/GOV diagnostics,
actual selected/installed content identities, policy inputs and decision trace.
These criteria implement the package-policy consumer, not governance production
or a whole-governance completion prerequisite. Status remains Open.
<!-- build-governance-resolution:end -->


<!-- build-facet-source-lock:start -->

Apply the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 actual facet lock-and-version criterion (Open): Resolve and record each selected facet identity, version, compatibility range, source hash and artifact-schema version in the actual package lock graph before dependent compilation/reuse. Preserve required facet and namespace boundaries, standard/package parity and source/compiler/profile/target identity. Reject missing/contradictory lock entries, incompatible versions, changed source/schema masquerading as the old facet and implicit activation from dependency presence. Required evidence: genuine normalized resolved locks, source-bound accepted/refused version/schema cases and downstream compiler inputs; the illustrative facet version is not a global default or package trust grant.
<!-- build-facet-source-lock:end -->


<!-- build-alternative-types-source-locks:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual type-provider identity and lock criterion (Open): Record the actual deterministically selected custom type provider/version, fact/proof schema, restrictions and required solver/package/compiler inputs in typed-Core metadata, package locks and the artifact graph before dependent compilation/reuse. Preserve declared supported profiles/facets/features and authentic source identity; the example provider name/version/schema is not a default selection. Reject ambiguous/missing providers, incompatible lock/schema identities and coherently substituted providers that reuse another checker's facts or soundness evidence. Required evidence: real resolved-provider/typed-Core/lock joins and changed-provider/source/schema refusal fixtures; completed Build/release and an external solver are not universal prerequisites for local declaration construction.
<!-- build-alternative-types-source-locks:end -->


<!-- build-interop-source-locks:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual boundary identity and compatibility-lock criterion (Open): Resolve authentic incumbent/provider/schema/runtime/ABI/protocol/version/range/source and profile/target constraints into real package/lock/artifact identities before dependent generation or admitted reuse. Preserve declared compatibility/deprecation and temporary-shim state; a generated file or host package name cannot supply its own first source identity or grant. Reject ambiguous/missing/incompatible or coherently substituted foreign/schema/target/lock records. Required evidence: genuine declaration-to-resolved-lock joins, changed-version/source refusals and explicit missing support; ordinary source construction needs no final release, signing or live package mutation.
<!-- build-interop-source-locks:end -->

- [ ] `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects` Execute typed graph under declared build effects

Status: Open

Evidence baseline: [Build architecture](../../docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md)

Closing evidence: Clean build node receipts and undeclared environment/network/shell rejection

<!-- authority-source-build:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete build provider execution criterion (Open): Execute the declared typed build/package graph using actual deterministic selected provider and gate/grant/policy products, keeping build effects distinct from produced runtime effects. Record actual provider inputs/outputs as content-addressed products or sufficient replay/audit metadata, the applicable non-replayability/exception decision and secret-safe provenance. Enforce scope/phase/isolation at each real action sink and produce denial without externally visible action. Package metadata and lock records retain provider id/version/contract/policy; a request, policy exception or graph record is not performed work and cannot waive hermetic authority. Existing Expand gates own macro/plugin invocation and Auth owns grants; this task owns actual graph actions rather than duplicating those authorities. All signing, publication, private-store and other operation holds remain.
<!-- authority-source-build:end -->


<!-- compile-time-source-phases:start -->

Apply the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Preserved finite predicate conjunctions:

Source obligation `language/compiletime/preserve-phase-artifacts`: Schedule compile-time stages in the declared semantic order and prevent later stages or memoization from mutating earlier artifacts or losing original source/generated provenance. Accept: Schedule reading, namespace loading, expansion, name/type/effect analysis, profile/capability validation, constant evaluation, extension execution, revalidated typed-core lowering and target preparation in L12 order. Later stages may inspect immutable prior artifacts; normalization and memoization retain original spans and generated-origin chains. Reject: Reject an out-of-order dependency, unauthorized extension/target preparation, mutation of a prior artifact or loss of original/generated diagnostic provenance. Required outputs: Ordered compile-time event graph, immutable before/after artifact digests, authorization decisions and source/generated-origin diagnostic fixtures. Dependencies consume versioned facts/receipts from expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open.

Complete L12 phase-artifact criterion (Open): Schedule and record actual reading, namespace loading, macro expansion, name/type/effect analysis, profile/capability validation, constant evaluation, authorized extension execution, revalidated checked-core lowering and target preparation with their exact observable products and invalidation. Checked macro/extension program P and early candidate/type/layout facts must be available before the relevant execution, while generated caller Q is checked afterward. Later phases inspect immutable earlier artifacts; normalization/memoization retains original spans and generated-origin chains and produces new identities rather than mutating prior ones. Preserve the existing D1/C1/C5/C6 ordering decision as unresolved under its current architecture owner instead of inventing a new pipeline. Build owns graph scheduling and immutable products, the compiler API owns pass semantics/observable boundaries, and semantic owners supply actual decisions. Reject illegal order, missing facts, unauthorized target/extension work and lost or mutated provenance with real before/after artifacts.
<!-- compile-time-source-phases:end -->


- [ ] `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` Implement dependency-aware caching and invalidation

Status: Open

Evidence baseline: [Pass cache](../../bootstrap/clojure/src/gravity/pass_cache.clj)

Closing evidence: Reuse on equal inputs; rebuild on source, compiler, grant, policy or target changes


<!-- build-canonical-cache-conditions:start -->
Apply the [complete incremental-compilation source criteria](plan.md#complete-incremental-compilation-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending. Existing criterion fragments remain in place.

Source compiler/c16/cache-revalidation: Reuse stage-specific entries only after producing-pass/schema/version/facts/policy/dependency/diagnostic validation, retaining provenance and invalidation records. Concrete acceptance: Change producer pass version, schema, preserved facts, profile/target, policy, dependency or diagnostic schema separately; reject stale reuse and record the actual affected revalidation/recomputation, keeping prior provenance. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. Source compiler/c16/proof-reuse: Reuse proofs/certificates only when complete claim/input/assumption/context/invalidation conditions match; require a mapping when node identity changes but semantics survive. Concrete acceptance: Change each claim/input/assumption/profile/target/compiler/provider/package/invalidation premise, and change a node ID without a proof mapping; reject reuse or reprove, retaining residual checks until evidence is valid. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. Source compiler/c16/recorded-rebuild: Reproduce hermetic/release artifacts from recorded inputs and replay rather than trusting stale or incomplete caches. Concrete acceptance: Remove or alter a recorded build input or effect replay record; the hermetic/release rebuild must refuse the claim or expose output drift rather than passing from a stale cache. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. Source compiler/c16/semantic-cache-key: Key each reusable stage artifact over all meaning/legality/proof/diagnostic/output inputs with producer-owned required fields; reject incomplete or timestamp-only identity. Concrete acceptance: Omit each producer-required semantic/legality/proof/diagnostic/output input, or provide only a timestamp; reject C16-KEY and demonstrate that each changed relevant field prevents a false cache hit. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. Source compiler/c16/speculation-publication-gate: Mark partial interactive reuse speculative and withhold release publication until full revalidation succeeds. Concrete acceptance: Attempt publication from a partially revalidated interactive cache; require C16-SPECULATIVE and no release artifact until complete revalidation succeeds. Retain actual input/output and diagnostic identities plus positive and rejected-case artifacts; supplied success flags, proposal delivery and broad parent links are not executable evidence. Criterion behavior/pkg2/dependency-aware-cache: accept Reuse equal source, lockfile, compiler, profile, target, policy and relevant environment inputs with a matching cache key. Reject Changing any consumed source, lock, compiler, profile, target, safety policy, capability grant or relevant environment input still reuses the cached artifact. Emit Input hashes and reused/rebuilt node/cache-decision log with changed key field and PKG2002. Dependencies: PKG2, PKG7, C16. Decision allocation interface/compiler/c16/dependency-invalidation (Open). Role: build cache/invalidation maintainer. Invalidate downstream artifacts on every specified source/macro/namespace/type/effect/profile/capability/safety/proof/provider/target/backend/package/policy/facet/diagnostic change; record exact affected revalidation stages. Refusals and conditional cases: Change source, reader option/extension, macro/dependency, grant/replay, namespace import/export/alias, package version, type/provider, effect/capability registry, profile, safety/proof/trust, pass contract, target/backend, runtime/provider, facet or diagnostic schema; reject stale reuse and record each affected downstream revalidation stage. Required outputs: Field-sensitive dependency graph, complete change-class invalidation trace and clean-versus-revalidated artifact/diagnostic comparison. Consume actual dependency interface outputs: resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes, pipeline; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- build-canonical-cache-conditions:end -->


<!-- build-canonical-incremental-records:start -->
Apply the [complete incremental-compilation source criteria](plan.md#complete-incremental-compilation-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending. Existing criterion fragments remain in place.

C16 incremental graph/record/diagnostic criterion (Open): The actual incremental graph contains source units, syntax streams, expansion traces, namespace analysis, typed core, effect and ownership graphs, safety outcomes, MIR, optimization decisions, domain and target artifacts, diagnostics, proofs/certificates and package/provider manifests. Record exact field-level dependency edges when available; explicitly coarse early edges are legal but reduce reuse and must not skip invalidation. Emit producer-owned stage key schemas over every meaning/legality/proof/diagnostic/output premise: source, reader options/extensions, syntax, macro expansion, namespace, profile, target, compiler, pass contract, dependencies, build-effect replay, capability policy and language facets as required by that stage. Reject missing required fields and timestamp-only identity; example type-check stage/hash values are illustrative, not global required subsets. Cache entries are inspectable versioned artifacts binding cache key, artifact identity, producing stage/pass version, actual input hashes, preserved facts, invalidation conditions, diagnostic stream, trust context and release revalidation status. Preserve accurate provenance and diagnostics even when an entry is invalidated. Trust labels do not supply capability, policy or proof authority. Revalidation requires equal keys, compatible artifact schema and producer pass versions, preserved facts, fresh proof/certificate, profile/target, diagnostic schema and dependency graph. Mark every partially revalidated interactive artifact :reuse :speculative and record revalidation status; release publication is withheld until full revalidation passes. Required outputs: actual dependency graph, stage key schemas, cache manifests, invalidation trace, reuse/revalidation reports, stale-proof refusals and incremental diagnostic records, with changed-input and unchanged-input comparisons. Preserve all nine C16 diagnostic meanings: KEY incomplete key, ENTRY malformed entry, STALE invalidating change, PROOF stale certificate, SPECULATIVE publishable-boundary misuse, REPLAY missing build-effect replay, POLICY incompatible profile/capability/safety, DIAGNOSTIC stale stream and GRAPH inconsistent dependency graph. Include cache key, artifact ID, stage, invalidating input, available source span or manifest entry, profile, target and remediation. Reject stale origins/facts/diagnostic schemas and malformed graph or manifest even if cached content hashes match. Real conformance covers every graph/key/entry field, changed invalidation class, proof premise, node-ID proof mapping and speculative publish gate, plus reproducible recorded-input/replay release rebuild. Consume authentic C1/stage, L12, SAFE15, PERF3 and package/project/lock/environment interfaces; stage-comparison consumers retain separate bootstrap evidence. Full dependency-document, compiler, proof checker, build execution, migration and release acceptance are not inferred. The existing six C16 source criteria and their independent scopes remain exact prefixes; all five Build tasks stay Open, with no new task units or quota. No compiler/cache-build/runtime/provider/broker/controller/target-probe execution is authorized or reported here.
<!-- build-canonical-incremental-records:end -->

<!-- authority-source-cache-producer:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; current source and implementation acceptance remain pending.

Complete provider cache producer criterion (Open): Construct actual stage-appropriate cache identities using the selected provider id/version/scope/contract/trust/policy and compile-time input/output/replay facts required by that producer, excluding secret values under the stable public cache-key policy. On a relevant provider, policy or recorded-input change, invalidate the affected entry and downstream dependencies, then actually revalidate or recompute rather than accepting a stale hit. Emit field-sensitive keys, invalidation/reuse/rebuild decisions and refused stale, mismatched, non-replayable or secret-leaking cases. This existing dependency-aware caching task owns key construction, reuse and invalidation; the reproducibility task consumes those actual products for repeated locked-build comparisons.
<!-- authority-source-cache-producer:end -->


<!-- compile-time-source-cache:start -->

Apply the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 cache producer criterion (Open): The existing Build cache task owns shared/default cache construction, semantic-input completeness, current-policy admission and invalidation/revalidation integration. A selected L16 engine may implement expansion-specific keys, storage, invalidation and scheduling under the same contracts, exposing authentic keys, entries, trace/replay and current-policy results to this shared boundary. Construct actual compile-time keys and entries from all ten L12 input groups, authentic computed values/traces and source/compiler/macro/function/extension/facet/profile/target/grant/policy/replay identities. Compare actual current-policy legality before reuse, invalidate stricter-policy or changed-input entries and never bless creation-time hidden effects after policy relaxation. Preserve secret-safe environment digest/presence handling without putting secret values in stable public keys. Perform actual revalidation/recomputation and produce clean-versus-reused/refused evaluation/typed-core products, not cache-policy=strict or reuse-allowed metadata alone. Expand consumes these results; this task does not become a second evaluator, grant issuer or repeated-build owner.
<!-- compile-time-source-cache:end -->


<!-- macro-source-cache-shared:start -->

Apply the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro shared cache criterion (Open): Preserve the L16 replacement boundary in the existing Build cache task: own shared/default cache construction, complete semantic-input/current-policy admission and invalidation/revalidation integration, consuming real selected-engine expansion key/entry/trace/replay/policy results. Engine-specific storage/key construction/invalidation/scheduling remains permitted; Build need not duplicate that machinery. Reject incomplete source/macro/provider/dependency/profile/target/facet/grant/compiler/reader/namespace/replay inputs, stale traces, stricter-policy mismatch, hidden creation effects and secret leakage. Produce authentic admitted/refused/miss and recomputed graph/artifact/diagnostic products for actual consumers rather than record-only success.
<!-- macro-source-cache-shared:end -->



<!-- build-facet-source-reuse:start -->

Apply the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 actual facet invalidation criterion (Open): Include authentic selected facet identity/version/source/schema/surface/activation, required facets, profile/target, lowering/checker/provider and declared effect/grant/build-input identities in the applicable semantic cache dependency graph. Changed syntax, IR, lowering, profiles, schema, privacy policy or grants invalidates affected expansion, typed/domain verification, proof, artifacts and diagnostics before real owner regeneration and current-policy admission. Preserve the existing selected-engine/shared-Build cache boundary; source-only inspection can precede semantic products. Reject stale facet proof/validation reuse, hidden build inputs and silent stable compatibility drift. Required evidence: actual unchanged hit, changed-input miss/refusal and clean/rebuilt products; identical keys or assigned preserved flags alone do not establish current validity.
<!-- build-facet-source-reuse:end -->


<!-- build-alternative-types-source-cache:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual type-provider cache and invalidation criterion (Open): Include authentic selected provider/version/fact/proof/solver dependencies, source/compiler/profile/target/macro/facet/grant and permitted build-effect inputs in applicable semantic identities. Source/provider/profile/target/macro/facet/grant changes invalidate affected types, checks, ownership/domain facts, proofs, diagnostics and downstream artifacts before actual owner regeneration/current-policy reuse. Matching digests cannot grant authority or validate a stale proof; early real dependency/checker interfaces precede later actual products. Required evidence: genuine unchanged hit and changed-input miss/refusal with clean-versus-regenerated outputs, without project work/input/cost/attempt allowances or fresh migration proofs as ordinary editing prerequisites.
<!-- build-alternative-types-source-cache:end -->


<!-- build-interop-source-cache:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual interop regeneration and drift-cache criterion (Open): Bind actual source/schema/provider/ABI/runtime/version/layout/profile/target/lock/compiler/generator/effect/grant inputs and their declared compatibility policy to generated bindings, codecs, wrapper audits, type/error/ownership maps, diagnostics and parity artifacts. Changed schema/ABI/CString layout or governing inputs invalidate affected claims before real owner regeneration/rechecking and current-policy reuse; matching digests do not prove wrapper or parity semantics. Reject stale/drifted/coherently substituted products and missing dependencies. Required evidence: real unchanged reuse and changed-input miss/refusal, regenerated source/facts and source-bound drift diagnostics; no project work/input/cost/attempt quota is added.
<!-- build-interop-source-cache:end -->

- [ ] `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records` Produce and verify package/provenance/signing records

Status: Open

Evidence baseline: [Artifact contract](../../docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md)

Closing evidence: Actual published package retrieval/verification fixtures; tamper and missing-record rejection

<!-- build-governance-package:start -->
Source requirement: `governance/registry-release-admission`, continuing the
existing source-family binding to this same task. See
[GOV10 requirements](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#requirements)
and [SAFE14 conformance](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md#conformance-criteria).

Additional source criteria, proposed current composition: Under SAFE14/SAFE15,
PKG9-PKG12 and GOV10, verify actual registry content digest, required signature,
manifest and attestation before use. Generated outputs retain generator/version,
source/output digests, effects/grants, reproducibility and performed safety
checks. Safe claims require reproducibility or accepted provenance attestations;
imported certificates bind the actual package/compiler/schema/profile/target,
provider trust and noninvalidated assumptions.

Validate stable identity, namespace, owner, version, license and signing status,
manifest/lock where applicable, hashes/provenance/SBOM, profile/target matrices
and conformance tied to compiler/target/profile/stdlib versions. Before
installation/publication expose effects/capabilities, GOV9 unsafe metadata,
FFI/host delegation and AI policy/eval/`:ai/human-review`/injection evidence.
Consume the actual applicable library/target/security/namespace/yank decisions
from `evolution/library-admission`, `evolution/target-support-admission`,
`evolution/security-review`, `evolution/namespace-decision` and
`evolution/registry-yank`; this gate does not adjudicate their policy.

Exercise declared registry accept/reject/quarantine behavior and real consumer
results. Reject tamper, wrong/stale/missing required evidence, unsigned content
when signatures are required, nonreproducible or malicious releases under the
declared policy, hidden authority, unsupported conformance, policy-forbidden
advisory/yanked dependencies, reserved-name abuse and unmitigated dependency
confusion. Quarantine unresolved security/provenance risk according to policy.
Preserve original SAFE14/SAFE15/PKG/GOV diagnostics, source and exact content/
policy/tool identities; metadata presence alone does not prove verification.
Actual publication and signing retain their authority boundaries. All existing
task IDs and Open statuses remain; no package operation was executed here.
<!-- build-governance-package:end -->

<!-- compile-time-source-artifact:start -->

Apply the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 evaluation-record packaging criterion (Open): Consume the evaluator's real namespace trace/constants/generated-origin/build-action/replay/cache/diagnostic products and persist their exact source/compiler/profile/target/grant/provider/input/output identities in the typed build artifact graph. Release output ships or archives the complete record according to package policy; safety-critical signing applies only when that policy requires it and remains separately authorized. Verify actual canonical/schema/content/provenance links and secret-safe public versus explicitly permitted private records before consumption. A manifest listing record kinds is not archival, signature evidence, execution or reproduction; current P records follow P evaluation, while preexisting imported Q evidence is verified before Q use. Preserve private-store/signing/publication holds.
<!-- compile-time-source-artifact:end -->


- [ ] `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` Close reproducibility and target matrix

Status: Open

Evidence baseline: [Reproducibility contract](../../docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md)

Closing evidence: Repeated locked builds with matching identities and explicit failing-target diagnostics

<!-- build-foundation-reproducibility:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/artifact-graph-reproducibility: Rebuild the declared artifact graph from recorded source/compiler/pass/policy/provider/target and dependency identities; compare canonical graph edges and outputs under an explicit accepted-delta policy. Refusals: Reject omitted inputs or edges, substituted provider/compiler identities and unexplained output drift; preserve the failed comparison and any policy-reviewed exception. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence.
<!-- build-foundation-reproducibility:end -->


<!-- build-canonical-recorded-rebuild:start -->
Apply the [complete incremental-compilation source criteria](plan.md#complete-incremental-compilation-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending. Existing criterion fragments remain in place.

Source obligation `language/compiletime/reproduce-hermetic-core`: Reproduce byte-identical typed core from declared semantic inputs in required hermetic release/conformance/publication/safety-critical modes, rejecting undeclared ambient inputs. Accept: In release, conformance, reproducible publication and safety-critical modes, repeat the same source/compiler/lock/target/grants/declared inputs and require byte-identical typed core. Declare environment key/value policy, pin or replay network/shell/model/tool/package inputs, record clock/random values or deterministic seeds, use declared target manifests and hash generated inputs. Native-object metadata differences require a marked nonsemantic field and target conformance acceptance. Reject: Reject undeclared filesystem/environment input, unrecorded nondeterminism, ambient target probing, typed-core drift or an unreviewed native-object exception; report L12-HERMETIC-INPUT or L12-NONDETERMINISM as applicable. Required outputs: Hermetic input/replay manifest, paired typed-core byte comparison, generated-input hashes, exception record when applicable and accepted/rejected diagnostics. Dependencies consume versioned facts/receipts from expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects; they do not require blanket component completion. Implementation status remains Open. Criterion behavior/pkg11/explicit-target-fallback: accept Use a fallback target/runtime only when explicitly declared and compatible with project/profile/policy. Reject A failed target silently falls back to a different target or runtime. Emit Visible fallback selection in target/artifact records and PKG11007. Dependencies: PKG11, P13. Criterion behavior/pkg11/host-independent-cross-compile: accept Resolve and compile target profile/backend/runtime/ABI contracts independently of implicit host ABI, services and paths. Reject A cross build inherits host ABI/paths/default target assumptions. Emit Target-specific build/artifact identity and host-leakage rejection with PKG11002. Dependencies: PKG11, B1, R1. Criterion behavior/pkg11/per-target-evidence: accept Attach required conformance and support-level evidence separately to each target and expose every failing profile/target pair. Reject Default-target success hides a required target failure or a release claims support without its per-target conformance. Emit Per-target test/conformance reports, support table and PKG11006. Dependencies: PKG11, TEST3, TEST4, TEST6. Criterion behavior/pkg11/target-compatibility-admission: accept Validate profile/backend legality, dependency variants and capability availability for every concrete target before lowering. Reject A pair is unsupported, contradicts project profiles, lacks a dependency variant or assumes capabilities legal on another target. Emit Checked matrix, per-target graph/capability decision and PKG11001/PKG11003/PKG11004/PKG11008. Dependencies: PKG11, PKG5, P13, B1. Criterion behavior/pkg11/target-matrix-artifact: accept Normalize grouped declarations into concrete profile/backend/triple/ABI/calling-convention/runtime/features/dependency/capability/artifact/test/support entries and emit each target's manifests and support level. Reject Required target/ABI identity is missing, a matrix contradicts the project or a supported claim lacks its target-specific record. Emit Normalized matrix, dependency graph, capability summary, artifact/test/conformance manifests and supported/experimental/internal/build-only/unsupported table; PKG11005/PKG11008. Dependencies: PKG1, PKG11, R1, B1. Criterion behavior/pkg7/canonical-rebuild-comparison: accept Compare canonical manifests and actual content hashes for two locked builds under the same recipe, explaining only declared differences. Reject Verification compares filenames alone or ignores a changed canonical content/manifest identity. Emit Expected/actual digest lists and rebuild verification report; PKG7007. Dependencies: PKG3, PKG7. Criterion behavior/pkg7/deterministic-build-environment: accept Control source/project/lock/compiler/toolchain/graph/profile/target/environment/locale/timezone/order/timestamps/seeds/network/generated/blob/FFI inputs; normalize time/order/paths and seed or record randomness. Reject A reproducible build uses unlocked dependencies, unrecorded external reads/randomness, leaked host paths or generated source without generator/input hashes. Emit Controlled environment and input/output manifests, build recipe, exception list and PKG7002-PKG7006; changed compiler invalidates cache. Dependencies: PKG7, PKG2, PKG10. Criterion behavior/pkg7/nonreproducible-claim-boundary: accept Mark uncontrolled-input artifacts nonreproducible and list their exceptions explicitly. Reject A nonreproducible artifact satisfies a reproducible release/signing gate or a reproducibility claim lacks its recipe. Emit Nonreproducibility exception and release decision; PKG7001/PKG7008. Dependencies: PKG7, PKG12. Criterion behavior/pkg7/recorded-build-recipe: accept Emit a schema-valid release build recipe binding package/version, source/project/lock/compiler/toolchain, graph/task settings, concrete profile/target matrix, controlled environment/locale/timezone/traversal/timestamps/seeds/network, generator/input hashes, blobs/FFI inputs, expected artifact identities, output hashes and verification command or machine-readable equivalent. Rebuild twice under that exact recipe and compare canonical manifests and actual content hashes. For standard-library packages retain source commit, build graph, compiler, package lock, tests and policy-required signatures in the provenance record. Reject Reject a claimed reproducible release with no recipe, omitted or altered consumed inputs/outputs, unlocked dependencies, unrecorded network/randomness, host path leakage, missing generator hashes, filename-only comparison or unexplained hash drift; mark uncontrolled inputs nonreproducible and deny reproducible signing/release claims when policy requires reproducibility. Standard-library provenance and artifact identity must agree; neither API inventory nor a supplied success label proves a rebuild. Emit Versioned build-recipe artifact, controlled-environment manifest, input/output digest lists, actual repeat-build canonical comparison, exception list and PKG7001-PKG7008 diagnostics with recipe/input/artifact/target/environment context; standard-library source/build/lock/test/signature provenance links. Dependencies: PKG1/PKG2 project, lock and typed graph producers, PKG3 canonical manifests, PKG5 complete dependency lock, PKG10/PKG12 provenance/signing evidence, stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests module/package manifests, BOOT6 trust evidence remains separate.
<!-- build-canonical-recorded-rebuild:end -->

<!-- authority-source-cache:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete provider reproducibility/cache consumer criterion (Open): Consume the existing dependency-aware cache task's actual provider-bound key, invalidation, reuse and rebuild-node products in repeated locked-build comparisons. That task remains the owner of shared/default cache construction and invalidation integration, including actual results from permitted L16 engine-specific cache machinery; this existing reproducibility task owns recorded recipe and actual canonical manifest/content comparisons across the claimed targets. Preserve selected provider id/version/scope/contract/trust/policy and compile-time input/output/replay identities in the consumed recipe/results, excluding secret values and enforcing the stable public cache-key secret policy. Changed provider/version/policy/contract or recorded inputs must be visible in the consumed cache decisions and recipe, preventing a stale reproducibility claim. A recorded replay exception alone grants no effect authority. Produce actual repeat-build comparisons and refused stale, mismatched, non-replayable or secret-leaking claims with source/build/compiler/provider identities; key-shaped metadata or a completed status is not reproducibility evidence.
<!-- authority-source-cache:end -->


<!-- compile-time-source-hermetic:start -->

Apply the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 hermetic reproduction criterion (Open): In required release/conformance/reproducible-publication/safety-critical modes, repeat actual builds from identical source, compiler version, lockfile, target manifest, grants and declared inputs/replay records and compare byte-identical typed Core. Reject undeclared filesystem input; environment reads require the exact declared-key/explicit-value-policy premise subject to the retained L6/L12 wording decision. Network/shell/model/tool use requires configured replayable provider or pinned result, clock/random requires recorded values or deterministic seed, probes use declared target manifests, and generated forms include input content hashes. A grant, replay exception or generic reproducible flag proves none of these premises. Native-object metadata variation needs an explicitly marked nonsemantic field and actual target-conformance acceptance; never relax typed-core equality. Consume actual cache results without implementing another cache, and retain unavailable/held cases, source-policy resolution and actual target evidence separately from signing/publication authority.
<!-- compile-time-source-hermetic:end -->


<!-- macro-source-rebuild:start -->

Apply the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro reproducibility consumer criterion (Open): Consume authentic engine/cache/event/value products and perform actual repeated declared-input builds and expansion-trace comparisons with source/compiler/provider/macro/grant/profile/target/facet/replay identity. Compare same-input hermetic typed-Core bytes under L12 separately from structural reference/alternative expansion equivalence under L16. Native metadata exceptions retain their explicitly marked nonsemantic fields and target acceptance; never weaken typed-Core equality. Retain actual cache invalidation/refusal and secret-safe recipe/output evidence without implementing another cache, issuing authority or treating records as reproducibility results.
<!-- macro-source-rebuild:end -->



<!-- build-interop-source-reproducibility:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual versioned binding and artifact reproducibility criterion (Open): Repeat selected real declared generation/builds from the same authentic incumbent/schema/provider/compiler/target/lock and permitted effect inputs, comparing actual binding/codecs/metadata/provenance artifacts under the governing reproducibility contract. Versioned labels and assigned reproducible flags do not establish equality, compatibility, privacy or incumbent parity. Preserve accurate source identity even where selected semantic outputs are stable; unavailable inputs cannot pass. Required evidence: genuine repeated locked-build/generation comparisons and changed-input/missing-source negatives, with final selfhost/release equivalence and authorized signing/publication remaining separate.
<!-- build-interop-source-reproducibility:end -->

<!-- build-implementation-tasks:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier task-source statements are separate from the
current implementation authority and do not imply product completion.

# Tasks: Build workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence); do not copy its checklist here.

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

Review `build-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
