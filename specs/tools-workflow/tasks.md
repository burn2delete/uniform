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


<!-- tooling-specialist-services-lsp-current-state-and-queries:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service lsp-current-state-and-queries criterion (Open). Sources: [T5](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md).

Implement real open-document, syntax, namespace/expansion, type/effect, profile/target, diagnostic, package/artifact and cancellation/incremental state per workspace. Actual open/change/save updates converge to CLI check on identical project/document revisions. Expose diagnostics with matching codes/spans, hover facts/docs when known, namespace/visibility/profile/target-correct completion, semantic tokens and artifact/safety navigation. Exercise actual protocol fixtures, multi-profile/target changes and stale/cancelled state, fabricated facts, illegal completion or diagnostic mismatch refusals; a capability matrix is not an editor service. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-lsp-current-state-and-queries:end -->



<!-- tooling-specialist-services-lsp-checked-edits:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service lsp-checked-edits criterion (Open). Sources: [T5](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md).

Implement code actions with safe defaults and explicit required checks, rename preserving real namespace/binding/macro identities, and formatter delegation under T3. Generated files are marked with actual source-artifact origins and reject edits without source policy. Exercise genuine patches/edits and ambiguous generated binding, bypassed compiler checks, stale revision and generated-file denial cases; no editor-only semantics, unvalidated fix or string rename substitutes for source-aware behavior. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-lsp-checked-edits:end -->



<!-- tooling-specialist-services-lsp-protocol-and-traces:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service lsp-protocol-and-traces criterion (Open). Sources: [T5](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md).

Emit actual cancellable protocol responses, compiler-consistent diagnostics, semantic tokens, code-action records and conformance transcripts for open/change/save/diagnostics/hover/completion/rename/actions. Trace logs are disabled or genuinely scoped by user policy and redact protected manifest/runtime data. Exercise real selected stdio/request/response and cancellation flows plus secret trace, wrong source/session and unsupported transport/target cases; source-only queries do not require absent later runtime facts. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-lsp-protocol-and-traces:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer` Debug / debugger maintainer

Status: Open

Remaining implementation tasks: Implement artifact-bound sessions, source breakpoints and state inspection; implement separately authorized mutation; connect target and replay adapters

Closing evidence and governing contract: Source maps resolve? Unavailable versus optimized-away values distinguished? Unauthorized mutation and secret inspection rejected? [Debugger](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md)


<!-- tooling-specialist-services-debug-subjects-and-breakpoints:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service debug-subjects-and-breakpoints criterion (Open). Sources: [T6](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md).

Bind each actual debug session to exact artifact/profile/target/debug-data version, source/symbol/type/layout/MIR/effect/grant and available ownership/lifetime/stack data. Implement genuine source/artifact breakpoint mapping and stopping where target support exists. Exercise matching mappings/stops and missing/mismatched metadata, unmappable breakpoints and unavailable target features; distinguish optimized-away from unavailable and live values. Static frame or passed source-map rows do not prove a running debug session. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-debug-subjects-and-breakpoints:end -->



<!-- tooling-specialist-services-debug-inspect-versus-mutate:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service debug-inspect-versus-mutate criterion (Open). Sources: [T6](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md).

Implement inspection under actual redaction/capability policy and separately authorized state mutation; debugging creates no program authority. Preserve native/kernel raw-memory unsafe/audit context. Exercise permitted selected observations and missing mutation authority, redacted secret/no-store access, stale frame and unsupported mutation refusals with actual no-action denial. A read grant, debug UI or unsafe marker cannot authorize writes, reveal secrets or manufacture optimized-away values. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-debug-inspect-versus-mutate:end -->



<!-- tooling-specialist-services-debug-workflow-ai-and-reports:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service debug-workflow-ai-and-reports criterion (Open). Sources: [T6](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md).

Implement workflow event-log replay/node stepping without repeating effects, and AI trace linkage across model/prompt/tool/memory/policy/human-review products. Emit real session/breakpoint/frame/variable/replay/denial/source-map structured reports with explicit partial-support degradation. Exercise valid selected trace views and side-effecting replay, missing/wrong links, no-store prompt leakage and invented live-state refusals. Historical traces and simulators retain exact scope; held debug/native/replay/provider actions stay unrun. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-debug-workflow-ai-and-reports:end -->


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



<!-- tooling-specialist-services-ir-real-stage-views:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service ir-real-stage-views criterion (Open). Sources: [T10](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md).

Expose actual Reader/syntax/expansion/resolution/typed-effect-Core/ownership-safety/MIR/domain/optimized/backend/artifact products with compiler version/project hash/profile/target/stage/upstream identities, current applicable facts and source/generated/macro origins. Exercise genuine selected structured views and failed-stage reporting, missing/stale/wrong-subject identity and lost origin refusals. A simplified parallel model or display metadata cannot invent compiler state, and source-only inspection needs no unavailable later facts. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-ir-real-stage-views:end -->



<!-- tooling-specialist-services-ir-diff-and-preservation:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service ir-diff-and-preservation criterion (Open). Sources: [T10](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md).

Compare actual pass input/output artifacts, distinguishing semantic changes from stable nonsemantic formatting/id renumbering, and display genuine preservation, proof/analysis and invalidation results. Check removal/elision claims cite matching actual evidence; backend views name target/ABI/runtime/assumptions. Exercise valid selected diffs and absent/stale proof, semantic change hidden as renumbering, dropped source or omitted target assumptions. A preserves field or equal sampled values does not prove legality. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-ir-diff-and-preservation:end -->



<!-- tooling-specialist-services-ir-exports-and-reproduction:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service ir-exports-and-reproduction criterion (Open). Sources: [T10](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md).

Emit real stage views, IR JSON, pass/preservation/source-map reports and redacted explorer bundles reproducible from exact artifact ids. Exercise actual request-to-export/reload consistency and missing/wrong subject, generated-origin gap or protected-data failures. Preserve scriptable CI comparison interfaces without authorizing compiler mutation, pass execution or proof engines through a read-only view. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-ir-exports-and-reproduction:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer` Profile / performance-tool maintainer

Status: Open

Remaining implementation tasks: Capture real supported target samples; bind environment and source/artifact identity; report overhead, comparisons and safety-check evidence

Closing evidence and governing contract: Samples measured rather than generated? Unavailable counters explicit? No unrelated grant or proofless elision? [Profiler](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md)


<!-- tooling-specialist-services-profiler-measurements-and-identity:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service profiler-measurements-and-identity criterion (Open). Sources: [T11](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md).

Measure actual selected wall/CPU, allocations, supported cache/branch/latency/throughput/energy/device, GC/service and specialization/layout/check-elision data as applicable. Bind run/artifact/benchmark/environment/compiler/profile/target and hardware/firmware/GPU device identity; link real samples to source/MIR/artifact when possible. Declare measured profiler overhead or unknown. Exercise genuine selected runs and missing identity, unsupported counters or supplied/fabricated samples; target support is explicit and no unrelated runtime authority is granted. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-profiler-measurements-and-identity:end -->



<!-- tooling-specialist-services-profiler-comparisons-and-regressions:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service profiler-comparisons-and-regressions criterion (Open). Sources: [T11](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md).

Compare actual matching benchmark identities and environments under declared threshold/benchmark policy, permitting normalization only where policy declares it. Emit structured reproducible trend artifacts, environment/sample/comparison/regression decisions and threshold-exceeded failures. Exercise genuine equal/changed baselines, incompatible subjects, unsupported normalization and exceeded thresholds; quality/performance policy thresholds remain semantic constraints, distinct from removed project work accounting. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-profiler-comparisons-and-regressions:end -->



<!-- tooling-specialist-services-profiler-ai-replay-and-safety:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service profiler-ai-replay-and-safety criterion (Open). Sources: [T11](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md).

Separate actual AI/distributed provider/model/tool latency and external cost, retries, replay and human-review time. Show actual check-elision proof/analysis and selected specialization/layout evidence, not silent unsafe speedups. Exercise genuine scoped reports and missing proof, mixed latency/replay subjects or unrelated grant requirements. External billing/account restrictions and target physical limits remain; neither cost records nor a proof filename grants execution or accepts a benchmark. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-profiler-ai-replay-and-safety:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer` Docs / documentation-tool maintainer

Status: Open

Remaining implementation tasks: Generate human/structured API docs from compiler facts; execute runnable examples; detect stale hashes and redact protected values

Closing evidence and governing contract: Signatures/effects/profiles match exports? Failing examples and stale docs rejected? [Documentation generator](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md)


<!-- tooling-specialist-services-docs-real-inputs-and-api:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service docs-real-inputs-and-api criterion (Open). Sources: [T7](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md).

Generate from actual source/docstrings/syntax/public namespace APIs and applicable compiler type/effect/capability/profile/target, schema, package/artifact, diagnostic and safety metadata. Emit real HTML/JSON docs, search/API indexes, source links, schema docs and artifact manifest with source hash/compiler/package version and applicable compiler identity/hash. Show known signatures/availability, actual effects/grants, schema artifact ids and unsafe/FFI audit links. Exercise selected real generation and missing facts/audit/schema-link or unsupported claims; source reading is legal without inventing absent semantic analysis. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-docs-real-inputs-and-api:end -->



<!-- tooling-specialist-services-docs-examples-and-freshness:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service docs-examples-and-freshness criterion (Open). Sources: [T7](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md).

Route examples marked runnable to the genuine owning test/doc-check runner and retain actual checked/failed/unrun outcomes and exact source/artifact/compiler/package subjects. Detect stale docs via genuine source/artifact hashes, refusing mismatched package publication and failing runnable examples. Exercise actual current versus changed-source/changed-artifact checks and source-span/example failure cases; an example-validation count or filename is not a run and documentation generation does not authorize held execution or publication. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-docs-examples-and-freshness:end -->



<!-- tooling-specialist-services-docs-generated-and-protected-data:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service docs-generated-and-protected-data criterion (Open). Sources: [T7](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md).

Mark AI-generated documentation, compare its claims against actual compiler/source facts and preserve generated provenance. Redact secrets and no-store data from HTML/JSON/search/index/manifests and other generated outputs. Exercise true source-supported generated docs and contradictory claims, secret/no-store exposure and missing source provenance refusals. Unknown facts stay unknown; generated prose, signatures or metadata do not confer safety, stability or conformance approval. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-docs-generated-and-protected-data:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer` Dev Server / development-session maintainer

Status: Open

Remaining implementation tasks: Implement watcher-to-incremental updates; implement legal reload versus safe restart; expose artifact/status events and redacted sessions

Closing evidence and governing contract: Changes produce actual updated artifacts? Incompatible hot reload restarts? Grants remain explicit? [Dev server](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md)


<!-- tooling-specialist-services-dev-session-and-incremental-services:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service dev-session-and-incremental-services criterion (Open). Sources: [T8](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md).

Implement actual project/profile/target/grant-bound file watching, deterministic incremental check/build, CLI-consistent diagnostic streaming, artifact events and structured status. Every event carries actual artifact id/path/profile/target/build node. Coordinate existing REPL/debug/replay/AI trace endpoints with explicit authority/redaction rather than parallel engines. Exercise real selected changes and CLI convergence, missing profile/target, stale input/event and hidden-grant refusals; fixed converged/session rows prove no server or watcher. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-dev-session-and-incremental-services:end -->



<!-- tooling-specialist-services-dev-supervision-and-reload:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service dev-supervision-and-reload criterion (Open). Sources: [T8](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md).

Supervise actual permitted runtime processes with only declared development grants and genuine cleanup/restart outcomes. Hot reload preserves current type/effect/profile assumptions or safely restarts; invalidated assumptions reject unsafe replacement. Keep dev-only artifacts from satisfying release verification or altering release artifacts. Exercise actual selected safe reload/restart, authority denial, stale assumptions and dev/release contamination cases; a restart decision row is not a supervised process result. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-dev-supervision-and-reload:end -->



<!-- tooling-specialist-services-dev-endpoint-replay-and-bug-bundles:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service dev-endpoint-replay-and-bug-bundles criterion (Open). Sources: [T8](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md).

Apply actual endpoint authentication where policy requires it and record AI/workflow replay-relevant nondeterminism. Emit real process/diagnostic/event/reload/replay logs and exportable bug-state bundles with protected data redacted from logs/browser payloads/status and bundles. Exercise genuine permitted endpoint/session outputs plus unauthenticated remote calls, missing replay events and secret leakage refusals. Declared endpoints or fixed ledger links do not provide access, replay safety or authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-dev-endpoint-replay-and-bug-bundles:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer` Registry View / package-tool maintainer

Status: Open

Remaining implementation tasks: Implement structured package search/detail and update diffs; expose signatures, SBOM and revocation; enforce private visibility

Closing evidence and governing contract: CLI/API package facts agree? Capability changes visible before install? Unauthorized package existence not disclosed? [Registry UX](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md)


<!-- tooling-specialist-services-registry-facts-and-update-diffs:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service registry-facts-and-update-diffs criterion (Open). Sources: [T9](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md).

Render actual shared structured package facts consistently in CLI/editor/web/API: overview/history/dependency graph/profile/target/capabilities/unsafe-audit/provenance-signing/SBOM/advisory/update/policy/private-latent views. Show capabilities before install/update, actual unsafe/audit metadata and complete capability/safety/dependency/target/provenance/license-policy deltas. Exercise matching human/JSON facts and genuine changed-package diffs; hidden expansion, inconsistent views or policy-incompatible install recommendations reject without changing governing package decisions. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-registry-facts-and-update-diffs:end -->



<!-- tooling-specialist-services-registry-verification-private-and-latent:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service registry-verification-private-and-latent criterion (Open). Sources: [T9](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md).

Show machine-verifiable signature/SBOM/provenance/revocation evidence and current policy result; no badge is accepted without actual matching reports. Deny unauthorized private requests without revealing existence or metadata. Show genuine latent package state, generator provenance and required review before resolution; presentation cannot grant review or resolve a private/latent package. Exercise matching reports and absent/stale/wrong-subject verification, private leakage and missing latent-state/review refusals; held registry/private-store/signing/publication actions stay unrun. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-registry-verification-private-and-latent:end -->



<!-- tooling-specialist-services-registry-search-and-automation:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service registry-search-and-automation criterion (Open). Sources: [T9](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md).

Implement actual profile/target/capability/safety search filters and structured package detail/update/verification/search/access-denial/policy reports consumable by CI without screen scraping. Exercise real selected filtered results, deny-capability policies and actual view/report identity. Refuse ignored filters, incompatible recommendations, missing review and unauthorized metadata; empty fixtures or supplied respected:true flags do not prove search or noninterference. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-registry-search-and-automation:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer` Safety Explorer / safety-tool maintainer

Status: Open

Remaining implementation tasks: Expose unsafe/grant/taint/FFI/proof graphs from actual evidence; implement source navigation and structured export

Closing evidence and governing contract: Requested/granted/denied/used authority distinguished? Missing evidence visible? Certificate/checker identities retained? [Safety audit](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md)


<!-- tooling-specialist-services-safety-evidence-views:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service safety-evidence-views criterion (Open). Sources: [T12](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md).

Display actual complete known unsafe islands with source/owner/reason/review/evidence, checked safe-wrapper and ownership/lifetime/region/arena facts, FFI ABI/ownership/lifetime/error assumptions, and source-linked capability/taint/AI/supply-chain/proof/check-elision views. Distinguish requested/granted/denied/used authority, taint source/sink/validation/policy and prompt/tool/memory/denial facts. Exercise genuine selected unsafe/FFI/taint products and omitted known islands/sinks, collapsed grants or unsupported wrapper claims. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-safety-evidence-views:end -->



<!-- tooling-specialist-services-safety-proof-and-missing-evidence:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service safety-proof-and-missing-evidence criterion (Open). Sources: [T12](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md).

Show actual certificate and checker identity, current proof/analysis/audit scope and package unsafe/provenance/SBOM facts. Missing evidence is explicitly missing and fails configured required-evidence audit gates; summaries cannot supply a specialist Governance verdict. Exercise actual evidence/index and wrong/stale/absent checker, missing wrapper precondition, omitted known evidence or false pass refusals. A zero-length supplied unsafe list or proof id proves no safety. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-safety-proof-and-missing-evidence:end -->



<!-- tooling-specialist-services-safety-structured-audit-products:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service safety-structured-audit-products criterion (Open). Sources: [T12](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md).

Produce stable actual redacted JSON safety/unsafe reports, capability/taint graphs, certificate index, missing-evidence and decision summaries consumed by existing review/CLI authorities. Preserve exact source/artifact/profile/target/policy subjects and genuine accepted/denied outcomes. Exercise real graph/report generation and protected-data export refusal; explorer rendering does not perform independent security/unsafe review, change policy or accept a release. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-safety-structured-audit-products:end -->


- [ ] `tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer` AI Development / AI-tool maintainer

Status: Open

Remaining implementation tasks: Use declared AI artifacts to propose schema-validated patches/tests; compile-check generated source; enforce human-review policy before write and retain provenance

Closing evidence and governing contract: Repository/tool-output instructions remain untrusted data? Unreviewed writes rejected? Applied change has model/prompt/tool/reviewer identity? [AI tooling](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md)


<!-- tooling-specialist-services-ai-development-modes-and-boundaries:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service ai-development-modes-and-boundaries criterion (Open). Sources: [T13](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md).

Implement Phase11 typed agents for diagnostic/search/explanation/patch/test/docs/refactoring/package-review/audit-assistance/performance/replay-analysis modes with actual model/toolset/memory/policy/output schemas and required checks. Treat repository files/docs/issues/package metadata/tool outputs as untrusted prompt data; authentic current grants constrain every tool call. Exercise actual selected typed mode products and injected authority, hidden/overbroad tool and unsupported provider cases. Scoped native local-model permission does not authorize unrestricted agents/tools, writes or compiler bypass. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-ai-development-modes-and-boundaries:end -->



<!-- tooling-specialist-services-ai-patches-generated-checking-and-review:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service ai-patches-generated-checking-and-review criterion (Open). Sources: [T13](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md).

Emit genuine schema-valid plan/patch artifacts and generated-test/source provenance tied to prompts/inputs. Parse and normally check generated source through Reader/macro/type/effect/profile/safety plus applicable package/tests before use; package updates include actual capability/safety diffs. Write actions use explicit human-review policy and required matching actual review before application. Exercise selected valid proposals and invalid schema, unchecked source, missing generated-test provenance, package diff or required review refusals; a validated flag or approved unrelated review cannot authorize a patch. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-ai-patches-generated-checking-and-review:end -->



<!-- tooling-specialist-services-ai-traces-and-applied-provenance:start -->

Apply the [specialist tool service criteria](../tools-workflow/plan.md#complete-specialist-tool-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Specialist tool service ai-traces-and-applied-provenance criterion (Open). Sources: [T13](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md).

Record actual plan/patch/output validation, model/prompt/tool/memory/policy/human-review links and final applied-change provenance. Traces are genuinely replayable or explicitly live-only under policy; selected replay verification detects hidden tools and missing records without assuming live calls can repeat. Exercise real selected reports and missing/wrong/stale model/prompt/tool/policy/review, hidden use or false replay claims. Applying changes, calling providers or checking compiler output remains held where restricted; source criteria and generated manifests perform no such action. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-specialist-services-ai-traces-and-applied-provenance:end -->

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
