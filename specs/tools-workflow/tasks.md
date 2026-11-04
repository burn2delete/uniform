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

Closing evidence and governing contract: Reader output and semantic syntax identity preserved? Comments retained? Invalid input rejected? Generated-file policy respected? [Formatter](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md)


<!-- tooling-source-services-format-source-preservation:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service format-source-preservation criterion (Open). Sources: [T3](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md).

Implement deterministic whitespace/indentation/line-break/alignment layout over source text, reader grammar version, edition, configuration, comment/metadata positions, optional line width, ignore ranges and generated-source policy. Cover lists/bindings/maps/vectors/namespace/metadata, comment anchors, trailing whitespace/final newline and multiline strings without content mutation. Preserve macro-sensitive syntax identity, comments, metadata and profile semantics. Exercise genuine before/after outputs and malformed source, changed reader meaning, lost comments/metadata or semantic-refactor refusals. Formatting does not require macro expansion or later type/runtime completion. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-format-source-preservation:end -->



<!-- tooling-source-services-format-cli-and-write-policy:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service format-cli-and-write-policy criterion (Open). Sources: [T3](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md), [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Implement --check nonzero when changes are needed, explicit diff and policy-controlled --write with discoverable project or CLI configuration. Reject project-edition conflicts, invalid configuration and generated-file writes unless actual generated-source policy permits them; respect declared ignore ranges and authorized output paths. Exercise check-before/check-after, selected allowed writes and default generated-file denial with actual patch/file outputs and no unauthorized mutation. A policy flag or changed-file count is no observed write decision. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-format-cli-and-write-policy:end -->



<!-- tooling-source-services-format-roundtrip-and-report:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service format-roundtrip-and-report criterion (Open). Sources: [T3](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md).

Compare actual before/after syntax objects or normalized Reader forms and preserve meaningful identities while physical source origins accurately reflect layout. Exercise repeated formatting idempotence, round-trip positives and deliberate mismatch/comment/metadata/string cases. Emit actual formatted files/patches, JSON files/ranges/diagnostics/version, configuration hash, parse failures and round-trip report. Equal supplied hashes, reader-round-trip true or fixture status do not prove preservation; genuine Reader/Syntax products are consumed only for their selected scope. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-format-roundtrip-and-report:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer` Lint / analysis-tool maintainer

Status: Open

Remaining implementation tasks: Implement the contract rule classes against named compiler/manifest facts; add project-scoped baselines and rule explanations; implement checked fixes and structured export

Closing evidence and governing contract: Rule source/facts and applicability visible? Unsafe fixes rejected? Baseline/fail-on-new reproducible? [Linter](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md)

<!-- tools-foundation-lint:start -->
Apply the [foundation criteria and source context](plan.md#foundation-criteria-and-source-context) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/canonical-concept-vocabulary: Publish the D3 reference meanings and relationships for profile, target, backend, runtime, effect, capability, safety outcome, artifact and file, and check representative compiler/package/runtime manifests against those meanings. Refusals: Reject a manifest that equates an effect with authority, a profile with a backend, or an artifact with its storage file; retain the offending span/field and owning concept reference. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion.

Foundation requirement gravity/cross-phase-term-change-control: Before downstream reliance on a changed cross-phase concept, update the canonical D3 definition and affected contract/schema references together, record the compatibility decision and invalidate dependent terminology checks. Refusals: Reject use of a changed downstream meaning with an unchanged canonical definition, stale dependent checks or an unrecorded compatibility impact. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance - Exact component/slice contracts, declared unsupported scope and claimed-support evidence.

Foundation requirement gravity/domain-concept-layer-map: Publish a domain-concept map relating each declared concept to its source forms, Core/domain IR boundary, emitted artifact, effects and required capabilities; verify a representative domain product against that map. Refusals: Reject an unmapped claimed concept, a host API substituted for its source meaning or a mapping that treats an effect declaration as a capability grant. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

Foundation requirement gravity/qualify-ambiguous-normative-terms: For normative uses whose meaning affects implementation, require an explicit qualified D3 concept or owning contract reference; report the exact ambiguous span and the competing interpretations. Refusals: Reject unqualified uses such as runtime/profile/target or effect/capability where they change the required implementation meaning; ordinary nonnormative prose does not create a new terminology gate. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion.
<!-- tools-foundation-lint:end -->



<!-- tooling-source-services-lint-rule-classes-and-facts:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service lint-rule-classes-and-facts criterion (Open). Sources: [T4](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md).

Implement syntax/namespace/profile/effect/capability/unsafe/FFI/taint/package/artifact/AI/performance/documentation rules with stable id, severity, named inputs, fixability and profile/target applicability. Prefer authentic current compiler type/effect/profile/grant/artifact facts over text heuristics where available; inspect manifests as well as source and consume actual taint/prompt/tool/policy evidence for AI safety or performance evidence for hints. Exercise real selected facts/rules and unavailable/wrong-target/stale facts, ambient prompt authority and overbroad-tool refusals. A compiler-facts list is not analysis. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-lint-rule-classes-and-facts:end -->



<!-- tooling-source-services-lint-config-baselines-and-blockers:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service lint-config-baselines-and-blockers criterion (Open). Sources: [T4](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md).

Implement reproducible project-scoped configuration, actual baseline/fail-on-new comparison and policy mapping of severities to warnings or release blockers. Refuse unknown rules, invalid settings, stale changed-span baselines where forbidden and missing actual facts; apply profile/target applicability without inventing support. Exercise genuine unchanged/new/removed/changed-span diagnostics and release-blocker results. Baseline files cannot silently hide new findings, and retired project quotas do not replace real policy semantics. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-lint-config-baselines-and-blockers:end -->



<!-- tooling-source-services-lint-fixes-explanations-and-exports:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service lint-fixes-explanations-and-exports criterion (Open). Sources: [T4](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md).

Produce actual reader-preserving fixes or route semantic changes through genuine compiler-aware refactoring validation before application. Exercise safe patches and deliberately semantics-changing/unchecked fixes; refuse mutation without valid current source/edition/fact context. Emit source/manifest-path diagnostics with rule/severity/explanation, actual baseline/rule metadata/fix patches/coverage, JSON and SARIF-like exports and stable machine-readable --explain-rule docs. A validated flag, report filename or patch proposal is no applied/refactoring-checked change. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-lint-fixes-explanations-and-exports:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer` Language Server / editor maintainer

Status: Open

Remaining implementation tasks: Implement transport/document state and cancellation; connect diagnostics, hover, completion/navigation/tokens; implement checked rename/actions and formatter delegation

Closing evidence and governing contract: Incremental state converges to CLI checks? Visibility/profile rules preserved? Stale edits and unsafe rename rejected? [Language server](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer` Debug / debugger maintainer

Status: Open

Remaining implementation tasks: Implement artifact-bound sessions, source breakpoints and state inspection; implement separately authorized mutation; connect target and replay adapters

Closing evidence and governing contract: Source maps resolve? Unavailable versus optimized-away values distinguished? Unauthorized mutation and secret inspection rejected? [Debugger](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer` REPL / interactive-runtime tooling maintainer

Status: Open

Remaining implementation tasks: Implement explicit session state and contract commands; compile/check every evaluation; record redacted transcripts and replay policy for effects

Closing evidence and governing contract: Same legality as compiled code? Grant denial before effect? Load/inspect/evaluate run in actual session? [REPL](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md)


<!-- tooling-source-services-repl-session-and-command-state:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service repl-session-and-command-state criterion (Open). Sources: [T2](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md).

Implement actual ephemeral or recorded sessions with project/lockfile hashes, profile/target, namespace, loaded source/artifact roots, grants, evaluation history, artifacts, diagnostics, runtime ledger links and transcript. Expose every required :profile/:target/:ns/:load/:eval/:expand/:type/:effects/:capabilities/:inspect/:artifacts/:diagnostics/:replay/:transcript as tool-callable command forms. Exercise real selected session changes and missing/stale/wrong-session/project/profile/target state refusals; fixed session ids or history rows do not establish interaction. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-repl-session-and-command-state:end -->



<!-- tooling-source-services-repl-checked-evaluation:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service repl-checked-evaluation criterion (Open). Sources: [T2](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md).

Run actual Reader/macro/type/effect/profile/safety checks before every evaluated source under the same normal compilation semantics. A pure Core expression needs no ambient host grant; effectful evaluation declares profile/target and visible current grants, unsafe forms actual policy/audit. Record participating Runtime ledgers. Exercise matching compiled/REPL results and missing grant/profile, invalid source/generated forms, unsafe policy and REPL-only semantic mismatch refusals with no unauthorized action. Inspection commands cannot bypass evaluation checks or grant execution. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-repl-checked-evaluation:end -->



<!-- tooling-source-services-repl-inspection-and-transcripts:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service repl-inspection-and-transcripts criterion (Open). Sources: [T2](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md).

Expose actual selected syntax/expansion/type/effect/ownership/capability/profile/artifact/diagnostic facts and preserve syntax metadata/source spans in expansion inspection. Emit authentic evaluation results, views, generated manifests, ledger links and secret-redacted JSONL recorded transcripts; unavailable/stale facts are reported honestly. Exercise real queries, generated origins and transcript outputs plus secret exposure, fabricated facts or wrong source/session refusals. Inspecting macro output is not authorization to run macros or runtime effects. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-repl-inspection-and-transcripts:end -->



<!-- tooling-source-services-repl-replay-and-hot-loading:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service repl-replay-and-hot-loading criterion (Open). Sources: [T2](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md).

Declare AI/workflow/distributed replay behavior and replay recorded session effects only where the actual effect/provider contract permits it. Track current loaded type/effect/ownership/grant assumptions; reject hot replacement invalidating active checked assumptions or replay of live nondeterminism without matching recorded effects. Exercise genuine selected replay/hot-load results and stale/missing/wrong-subject trace, live replay and invalid replacement cases; a replay label or fixed supplied history proves neither deterministic execution nor valid replacement. Held replay/provider/compiler work stays unrun. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-repl-replay-and-hot-loading:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer` IR Explorer / compiler-tool maintainer

Status: Open

Remaining implementation tasks: Expose actual stage artifacts and source edges; implement semantic-versus-format diff; show failed pass and preservation/proof records

Closing evidence and governing contract: Version/profile/target and artifact IDs present? Missing/stale stages shown honestly? Check-elision evidence visible? [IR inspector](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md)

<!-- tools-foundation-inspection:start -->
Apply the [foundation criteria and source context](plan.md#foundation-criteria-and-source-context) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/tooling-semantic-consistency: Make formatter/LSP/REPL/inspection consumers use actual compiler diagnostics, syntax/source identities and compiled profile/effect/capability boundaries; compare supported tool and ordinary compiler results on the same accepted and rejected inputs. Refusals: Reject stale tool diagnostics, lost syntax identity, a refactor changing source meaning or interactive evaluation bypassing compiled grant/profile checks; no alternate tool-only language semantics. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift - Actual runtime/compiler/tool structured diagnostics with stable owning codes and provenance; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.
<!-- tools-foundation-inspection:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer` Profile / performance-tool maintainer

Status: Open

Remaining implementation tasks: Capture real supported target samples; bind environment and source/artifact identity; report overhead, comparisons and safety-check evidence

Closing evidence and governing contract: Samples measured rather than generated? Unavailable counters explicit? No unrelated grant or proofless elision? [Profiler](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer` Docs / documentation-tool maintainer

Status: Open

Remaining implementation tasks: Generate human/structured API docs from compiler facts; execute runnable examples; detect stale hashes and redact protected values

Closing evidence and governing contract: Signatures/effects/profiles match exports? Failing examples and stale docs rejected? [Documentation generator](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer` Dev Server / development-session maintainer

Status: Open

Remaining implementation tasks: Implement watcher-to-incremental updates; implement legal reload versus safe restart; expose artifact/status events and redacted sessions

Closing evidence and governing contract: Changes produce actual updated artifacts? Incompatible hot reload restarts? Grants remain explicit? [Dev server](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer` Registry View / package-tool maintainer

Status: Open

Remaining implementation tasks: Implement structured package search/detail and update diffs; expose signatures, SBOM and revocation; enforce private visibility

Closing evidence and governing contract: CLI/API package facts agree? Capability changes visible before install? Unauthorized package existence not disclosed? [Registry UX](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer` Safety Explorer / safety-tool maintainer

Status: Open

Remaining implementation tasks: Expose unsafe/grant/taint/FFI/proof graphs from actual evidence; implement source navigation and structured export

Closing evidence and governing contract: Requested/granted/denied/used authority distinguished? Missing evidence visible? Certificate/checker identities retained? [Safety audit](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md)

- [ ] `tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer` AI Development / AI-tool maintainer

Status: Open

Remaining implementation tasks: Use declared AI artifacts to propose schema-validated patches/tests; compile-check generated source; enforce human-review policy before write and retain provenance

Closing evidence and governing contract: Repository/tool-output instructions remain untrusted data? Unreviewed writes rejected? Applied change has model/prompt/tool/reviewer identity? [AI tooling](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md)
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
