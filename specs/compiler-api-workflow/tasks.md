# Compiler API implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

The three literal outcome keys below are the sole editable implementation task
authority. Each grouped record retains its complete original five-row task table
and acceptance questions. No individual-row IDs or completion denominator are
invented. All three outcomes remain Open; migration is not compiler acceptance.

<!-- compiler-api-implementation-tasks:start -->
- [ ] `pipeline` Built-in compiler pipeline outcome

Status: Open

Projected task block:
Owner role: compiler API/pipeline maintainer. All tasks open; source evidence
above is partial implementation evidence, not closure.

| Task | Concrete closing evidence |
| --- | --- |
| Stabilize public request/context/result API | Versioned schema and compatibility fixtures for source and stage-artifact calls; missing versions, incompatible schemas and hidden mutable-context access reject. A direct caller and CLI adapter receive the same semantic results. |
| Register and schedule actual passes | Built-in registration binds implementation/version to complete requires/preserves/invalidates/regenerates/effects/profile contracts; execute declared order and reject missing facts, duplicate/conflicting registration and illegal ordering before consumption. |
| Execute and retain stage products | Actual selected compiler invocation records input/output identities, diagnostics, source origins and provenance for every required stage, including fused-pass observable boundaries; verifier failure stops downstream execution. |
| Derive cache keys and invalidate facts | Use actual source/compiler/pass/plugin/grant/replay/policy/target inputs; unchanged-input reuse agrees with a clean run, and every contract invalidation class triggers revalidation or recomputation. Stale proofs, diagnostics and speculative release reuse reject. |
| Integrate compiler consumers | Build, CLI and inspection callers consume the same API artifacts; positive and negative selected-target programs demonstrate no raw/unchecked backend input and no runnable output from diagnostic-only mode. |

Yes/no acceptance: are passes actually invoked? Are all required stage facts
verified and retained? Are clean and reused results semantically equivalent?
Are stale inputs and invalid stage orders rejected? Does public API behavior
remain independent of ambient mutable compiler state? All must be yes.

- [ ] `plugins` Plugin Host: finite remaining tasks

Status: Open

Projected task block:
Owner role: compiler extension-host maintainer. All tasks open.

| Task | Concrete closing evidence |
| --- | --- |
| Check manifest compatibility before code loading | Actual loader validates package identity, API/compiler version, declared passes/artifacts, trust and grants; incompatible/malformed/rejected packages produce diagnostics without executing initialization code. |
| Execute scoped plugin context | Run a deterministic read-only analysis and authorized transformation through actual scoped IR APIs; read-only writes, unauthorized namespaces/phases and hidden compiler mutation reject at the operation boundary. |
| Enforce real sandbox/build-effect boundaries | Exercise sandboxed and policy-trusted plugin execution; filesystem/network/process/environment/model/tool/artifact operations without grants are denied before effects, including host escape paths. Trust labels alone do not disable scoped checks. |
| Register passes, facets and domain interfaces | Require declared schema, verifier, profile support, effects, lowering path and conformance records as applicable; execute registered schema/verifier callbacks and reject opaque, conflicting or unauthorized registration. Component owners retain domain semantics. |
| Verify output, trace execution and revalidate reuse | Run the declared output verifier, reject invalid transformed artifacts, record actual plugin/package/grant/input/output/replay identities, and invalidate cache entries on changed versions, policy or dependencies. |

Yes/no acceptance: can incompatible plugin initialization run? Can a plugin
perform ungranted effects or mutate hidden state? Can an invalid output enter
the pipeline? All must be no. Are allowed sandboxed/trusted invocations real,
traceable and reproducible under their declared replay policy? Must be yes.

- [ ] `pass-evidence` Pass Evidence: finite remaining tasks

Status: Open

Projected task block:
Owner role: compiler verification maintainer. All tasks open.

| Task | Concrete closing evidence |
| --- | --- |
| Classify real pass population | Enumerate every pass in the declared compiler closure with version, risk, affected profiles/targets and governing minimum evidence; reject omitted passes or incompatible evidence classes. |
| Execute required verification gates | Invoke actual schema/semantic verifiers at required compiler boundaries; bind reports to input/output identity and stop affected artifact production on failure rather than accepting supplied success flags. |
| Bind transformation evidence | Changed MIR functions receive the governing translation validation or checked proof evidence; check-elision proofs/certificates are checked, and target lowering has actual differential/backend conformance receipts for claimed targets. |
| Capture failures and preserve trust boundaries | Record counterexample input/output/property/diagnostic and source identities, retain the prior valid artifact after rejected transformation, and turn reproducible failures into regression fixtures. |
| Emit truthful trust and release-gate records | Reports enumerate supported profiles/targets, residual evidence gaps and experimental gates; missing/high-risk/stale evidence blocks affected release artifacts. Release consumes this decision but independently owns final acceptance. |

Yes/no acceptance: do reports derive from actual checks on exact artifacts?
Are plugin and backend passes included when used? Are absent evidence and
counterexamples visible? Do failures block the affected claimed release path?
All must be yes. No claim of a fully mechanized compiler is implied.
<!-- compiler-api-implementation-tasks:end -->

## Historical workflow migration record

The original migration file follows unchanged. Its checkboxes and source links
are historical workflow progress, separate from implementation completion.

# Tasks: Compiler API workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `compiler-api-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
