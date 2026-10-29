# Release implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is claimed. All rows remain Open; transfer is not implementation
acceptance. Original outcome and integration boundaries remain in the roadmap.

<!-- release-implementation-tasks:start -->
- [ ] `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance` Bind each claimed component to exact implementation and conformance

Status: Open

Evidence baseline: [Overall roadmap](../../docs/implementation-roadmap.md)

Closing evidence: Component outcome receipts, positive/negative programs and explicit unsupported surface. Maintain the source-bound task/disposition matrix for all 240 original contracts and the separately declared release and Platform Product extensions. Keep every missing producer/checker/consumer interface, alias or split question, and architecture choice as finite named remaining work with criteria, dependencies and accountable role; an accountability link does not establish an implementation interface. Resolve those decisions and bind actual component evidence before claiming the corresponding release scope. This release-claim obligation does not block ordinary semantic development.

- [ ] `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler` Complete stage compatibility/equivalence across the claimed compiler

Status: Open

Evidence baseline: [Equivalence contract](../../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md)

Closing evidence: Fresh stage-to-stage outputs and diagnostics, residual trusted-code inventory BOOT1/BOOT3/BOOT5/BOOT7: Publish a versioned row for every claimed stage and authoritative module revision, naming source/compiler/artifact identities, supported and unsupported documents/features/profiles/backends/runtime/package/library/test closure, owned or deferred gaps, conformance links and release eligibility. Record exact compiler-module ownership and residual TCB with measurable migration delta. Compare both named compilers and canonical artifacts under declared applicable hash/manifest/diagnostic/conformance/runtime/IR/performance/accepted-delta modes; preserve stable codes/spans and provenance. Reject missing outputs, implicit inherited support, omitted stage rows, unsupported claims, stale matrix versions and unexplained drift; accept deltas only with policy-linked review. Performance bounds apply only when performance is part of acceptance; no compiler work quota is introduced. Execute the intermediate authoritative stage/module admission decision against this exact candidate and declared subset, consuming the fresh conformance, equivalence/accepted-delta, matrix, provenance, TCB and applicable audit records. Record admit or deny with candidate/source/compiler/artifact identities and failed predicates. Deny failing applicable suites, stale or missing required evidence, unexplained drift, unsupported claimed support and missing ownership/matrix updates. Exercise both a valid intermediate admission and each refusal. Retain explicitly owned/deferred gaps outside the claimed subset; final release policy separately rejects unreviewed gaps for claimed release support. This decision does not produce the evidence it consumes, replace final release acceptance, or gate ordinary semantic development.

- [ ] `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution` Produce actual self-hosted compiler/runtime execution

Status: Open

Evidence baseline: [Binary release status](../../docs/phase-18-binary-distribution-and-seedless-release/README.md)

Closing evidence: Actually invoke the prior Gravity compiler process on the complete declared next-stage compiler/runtime/library/build closure, retain real outputs and logs/lineage, link/package and execute that next compiler on accepted and rejected compiler inputs without Clojure in candidate compilation/runtime/build; then public check/run/compile and output executable demonstrate absent seed boundaries. SH25 component construction, SH27 equivalence and final release acceptance remain distinct; supplied rebuild records or plans do not prove execution.

- [ ] `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` Build reproducible signed distributions with provenance/SBOM

Status: Open

Evidence baseline: [Artifact provenance](../../docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md)

Closing evidence: Rebuild identity and signature/provenance verification plus tamper rejection BOOT1/BOOT6/BOOT8: Retain locked dependencies, recipe and environment controls for time/locale/filesystem order/toolchain/network, compiler lineage and manifest/content-hash comparisons. Declare unsupported rebuild strategies; diverse rebuilds require independent toolchain identity. Reproducibility exceptions and accepted deltas require recorded review. Produce canonical signed provenance binding stage/artifact/source graph/compiler/lockfile/recipe/environment/dependencies/builder and conformance/equivalence/safety/SBOM/signature links. Auditor queries must traverse release compiler to seed inputs; permit only explicitly declared stage cycles. Reject lineage gaps, undeclared cycles, uncontrolled network inputs, noncanonical signing, missing required evidence, unexplained drift and revoked compiler/builder/dependency/signature inputs.

- [ ] `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate` Obtain independent acceptance for the exact release candidate

Status: Open

Evidence baseline: [Binary release gates](../../docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md)

Closing evidence: Coordinator-reviewed candidate receipts and explicit release disposition BOOT1/BOOT3/BOOT5/BOOT6/BOOT8: For this exact release candidate, consume fresh source-bound claimed-stage/module conformance, equivalence, compatibility, provenance, TCB and applicable unsafe-audit evidence. Reject failing declared conformance, missing or stale evidence, unreviewed claimed-support gaps and revoked inputs. Preserve policy-qualified reproducibility exceptions. Intermediate stage/module authoritative admission is the separate consumer proposed under release task2 (complete-stage-compatibilityequivalence-across-the-claimed-compiler); ordinary development and final release acceptance remain separate from that migration decision. The task2 interface and executable evidence remain Open until canonically composed and independently accepted. This release gate does not implement compiler passes, native seed ABIs or their tests.
<!-- release-implementation-tasks:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier task-source statements are separate from the
current implementation authority and do not imply product completion.

# Tasks: Release workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence); do not copy its checklist here.

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

Review `release-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
