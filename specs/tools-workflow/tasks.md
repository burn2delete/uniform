# Developer Tools implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These twelve named service records are the sole editable task authority.
IDs derive from the existing component/section/title-slug convention, including
each original specialist-role cell. The source table had titles, not literal
individual IDs. No external dashboard object identity, clause renumbering or
additional service/task population is claimed. All records remain Open.

<!-- tools-implementation-tasks:start -->
- [ ] `tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer` Format / formatter maintainer

Status: Open

Remaining implementation tasks: Implement deterministic layout/comment/metadata handling; implement check, diff and policy-controlled write; compare Reader outputs before/after

Closing evidence and governing contract: Reader output and semantic syntax identity preserved? Comments retained? Invalid input rejected? Generated-file policy respected? [Formatter](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md) Verify edition/config discovery, ignore ranges, formatter version/config hash, diff/JSON changed ranges, idempotence, and multiline content preservation without macro expansion. Reject edition conflicts, lossy metadata/comments and hidden semantic refactors.

- [ ] `tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer` Lint / analysis-tool maintainer

Status: Open

Remaining implementation tasks: Implement the contract rule classes against named compiler/manifest facts; add project-scoped baselines and rule explanations; implement checked fixes and structured export

Closing evidence and governing contract: Rule source/facts and applicability visible? Unsafe fixes rejected? Baseline/fail-on-new reproducible? [Linter](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md) Require rule id/severity/inputs/fixability/profile/target and source/manifest spans; cover every T4 rule class, JSON/SARIF, baseline and rule-coverage artifacts, stable explain-rule output and combined source/manifest analysis. Reject unknown rules, unavailable facts, policy-forbidden stale spans, unchecked fixes and policy-blocking release findings. Foundation requirement gravity/canonical-concept-vocabulary: Publish the D3 reference meanings and relationships for profile, target, backend, runtime, effect, capability, safety outcome, artifact and file, and check representative compiler/package/runtime manifests against those meanings. Refusals: Reject a manifest that equates an effect with authority, a profile with a backend, or an artifact with its storage file; retain the offending span/field and owning concept reference. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Foundation requirement gravity/cross-phase-term-change-control: Before downstream reliance on a changed cross-phase concept, update the canonical D3 definition and affected contract/schema references together, record the compatibility decision and invalidate dependent terminology checks. Refusals: Reject use of a changed downstream meaning with an unchanged canonical definition, stale dependent checks or an unrecorded compatibility impact. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance - Exact component/slice contracts, declared unsupported scope and claimed-support evidence. Foundation requirement gravity/domain-concept-layer-map: Publish a domain-concept map relating each declared concept to its source forms, Core/domain IR boundary, emitted artifact, effects and required capabilities; verify a representative domain product against that map. Refusals: Reject an unmapped claimed concept, a host API substituted for its source meaning or a mapping that treats an effect declaration as a capability grant. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products. Foundation requirement gravity/qualify-ambiguous-normative-terms: For normative uses whose meaning affects implementation, require an explicit qualified D3 concept or owning contract reference; report the exact ambiguous span and the competing interpretations. Refusals: Reject unqualified uses such as runtime/profile/target or effect/capability where they change the required implementation meaning; ordinary nonnormative prose does not create a new terminology gate. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion.

- [ ] `tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer` Language Server / editor maintainer

Status: Open

Remaining implementation tasks: Implement transport/document state and cancellation; connect diagnostics, hover, completion/navigation/tokens; implement checked rename/actions and formatter delegation

Closing evidence and governing contract: Incremental state converges to CLI checks? Visibility/profile rules preserved? Stale edits and unsafe rename rejected? [Language server](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md) Verify multiple profile/target workspace state, generated-origin marking, semantic tokens, hover capability/docs/constraints, cancellable requests, compiler-parity diagnostics and protocol open/change/save fixtures. Reject profile-illegal completion, ambiguous macro rename, edits of protected generated files and secret-bearing responses or policy-scoped traces.

- [ ] `tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer` Debug / debugger maintainer

Status: Open

Remaining implementation tasks: Implement artifact-bound sessions, source breakpoints and state inspection; implement separately authorized mutation; connect target and replay adapters

Closing evidence and governing contract: Source maps resolve? Unavailable versus optimized-away values distinguished? Unauthorized mutation and secret inspection rejected? [Debugger](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md) Bind profile/target/artifact/debug-data version and source/MIR/type/layout/effect/ownership maps. Verify breakpoint stops where supported, structured stack/frame/value/denial/map reports and explicit partial-target degradation. Reject mismatched metadata, unmappable breakpoints, redacted or no-store inspection, replayed side effects and live-value claims for optimized-away values; link workflow node stepping and AI prompt/model/tool/memory/policy/review ledgers.

- [ ] `tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer` REPL / interactive-runtime tooling maintainer

Status: Open

Remaining implementation tasks: Implement explicit session state and contract commands; compile/check every evaluation; record redacted transcripts and replay policy for effects

Closing evidence and governing contract: Same legality as compiled code? Grant denial before effect? Load/inspect/evaluate run in actual session? [REPL](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md) Verify explicit project/lock/namespace/source/artifact roots, history and runtime ledger links; expose every T2 command through callable forms and preserve expansion spans/metadata. Compare portable pure core and granted hosted evaluation with ordinary compilation. Reject unsafe evaluation without audit policy, secret transcripts, live nondeterministic replay without recorded effects and hot replacement invalidating checked type/effect assumptions.

- [ ] `tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer` IR Explorer / compiler-tool maintainer

Status: Open

Remaining implementation tasks: Expose actual stage artifacts and source edges; implement semantic-versus-format diff; show failed pass and preservation/proof records

Closing evidence and governing contract: Version/profile/target and artifact IDs present? Missing/stale stages shown honestly? Check-elision evidence visible? [IR inspector](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md) Verify every T10 stage with compiler/project/profile/target/stage identity, available semantic facts, upstream/source/generated-origin edges, pass input/output and target assumptions. Export IR JSON, diff/preservation/source-map/redacted bundles; reject missing context, lost origins, secret exports and check removal without preservation evidence. Foundation requirement gravity/tooling-semantic-consistency: Make formatter/LSP/REPL/inspection consumers use actual compiler diagnostics, syntax/source identities and compiled profile/effect/capability boundaries; compare supported tool and ordinary compiler results on the same accepted and rejected inputs. Refusals: Reject stale tool diagnostics, lost syntax identity, a refactor changing source meaning or interactive evaluation bypassing compiled grant/profile checks; no alternate tool-only language semantics. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift - Actual runtime/compiler/tool structured diagnostics with stable owning codes and provenance; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

- [ ] `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer` Profile / performance-tool maintainer

Status: Open

Remaining implementation tasks: Capture real supported target samples; bind environment and source/artifact identity; report overhead, comparisons and safety-check evidence

Closing evidence and governing contract: Samples measured rather than generated? Unavailable counters explicit? No unrelated grant or proofless elision? [Profiler](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md) Verify benchmark/profile/target/artifact/environment/compiler and applicable device identity, source/MIR/sample links, measured or explicitly unknown overhead, structured trace/environment/comparison/regression/layout reports and policy thresholds. Separate model/tool latency, retry, replay and human-review time and actual external costs. Reject incompatible benchmark comparison unless policy permits normalization, unsupported counters, exceeded regression thresholds, missing check-elision evidence and unrelated grants.

- [ ] `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer` Docs / documentation-tool maintainer

Status: Open

Remaining implementation tasks: Generate human/structured API docs from compiler facts; execute runnable examples; detect stale hashes and redact protected values

Closing evidence and governing contract: Signatures/effects/profiles match exports? Failing examples and stale docs rejected? [Documentation generator](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md) Verify source/package/compiler identities; public types/profile availability/effects/capabilities, unsafe/FFI audit links, schema artifact references and generated-AI marking/fact agreement. Emit HTML/JSON/search/source/signature/schema indexes, example report and documentation manifest. Reject mismatched package/source hashes, failing marked-runnable examples, missing audit/schema/effect links, secrets/no-store data and invented generated claims.

- [ ] `tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer` Dev Server / development-session maintainer

Status: Open

Remaining implementation tasks: Implement watcher-to-incremental updates; implement legal reload versus safe restart; expose artifact/status events and redacted sessions

Closing evidence and governing contract: Changes produce actual updated artifacts? Incompatible hot reload restarts? Grants remain explicit? [Dev server](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md) Verify explicit project/profile/target/grants, child grant inheritance, endpoint authority/redaction, CLI diagnostic parity, artifact id/path/profile/target/build-node events, replay nondeterminism and redacted exportable bug bundles. Reject invalid reload assumptions, ungranted effects, secret browser/API/log output, policy-required remote authentication failures, unrecorded replay events and dev-only artifacts satisfying release verification.

- [ ] `tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer` Registry View / package-tool maintainer

Status: Open

Remaining implementation tasks: Implement structured package search/detail and update diffs; expose signatures, SBOM and revocation; enforce private visibility

Closing evidence and governing contract: CLI/API package facts agree? Capability changes visible before install? Unauthorized package existence not disclosed? [Registry UX](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md) Verify CLI/editor/web/API fact parity, profiles/targets and capabilities before install/update, dependency/version/policy views, complete safety/dependency/target/provenance/license diffs and policy compatibility reports. Require machine-verifiable signature/SBOM/provenance/revocation reports behind badges and latent state/generator/review evidence before resolution. Reject hidden capability expansion, policy-incompatible recommendations, private existence/metadata leaks and ignored profile/target/capability/safety filters.

- [ ] `tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer` Safety Explorer / safety-tool maintainer

Status: Open

Remaining implementation tasks: Expose unsafe/grant/taint/FFI/proof graphs from actual evidence; implement source navigation and structured export

Closing evidence and governing contract: Requested/granted/denied/used authority distinguished? Missing evidence visible? Certificate/checker identities retained? [Safety audit](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md) Verify unsafe source/owner/reason/review/evidence, distinct requested/granted/denied/used authority, source-validation-sink-policy taint, FFI ABI/ownership/lifetime/error assumptions, AI prompt/tool/memory/denial facts and package provenance/SBOM. Export audit/unsafe/capability/taint/proof-index/missing-evidence/decision reports. Reject omitted unsafe islands or sinks, unsupported wrapper claims, absent checker identity, missing required audit evidence and redaction failures.

- [ ] `tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer` AI Development / AI-tool maintainer

Status: Open

Remaining implementation tasks: Use declared AI artifacts to propose schema-validated patches/tests; compile-check generated source; enforce human-review policy before write and retain provenance

Closing evidence and governing contract: Repository/tool-output instructions remain untrusted data? Unreviewed writes rejected? Applied change has model/prompt/tool/reviewer identity? [AI tooling](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md) Verify every T13 mode declares Phase11 model/tools/memory/policy/output schema/checks. Emit plan/patch/generated-source/prompt/model/tool/validation/review/replay/applied-change artifacts. Mark generated tests with prompt/input lineage; validate generated source through reader/macro/type/effect/profile/safety and requested tests. Reject schema-invalid patches, hidden tools, policy-required review omissions, repository/docs/issues/package/tool text promoted to authority, package updates without capability/safety diff and incomplete trace/model/prompt/tool/policy provenance; replay detects hidden use.
<!-- tools-implementation-tasks:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier task-source statements are separate from the
current implementation authority and do not establish service completion.

# Tasks: Developer Tools workflow migration

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

All new paths use `tools-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
