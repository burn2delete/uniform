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

- [ ] `tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer` Lint / analysis-tool maintainer

Status: Open

Remaining implementation tasks: Implement the contract rule classes against named compiler/manifest facts; add project-scoped baselines and rule explanations; implement checked fixes and structured export

Closing evidence and governing contract: Rule source/facts and applicability visible? Unsafe fixes rejected? Baseline/fail-on-new reproducible? [Linter](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md)

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

- [ ] `tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer` IR Explorer / compiler-tool maintainer

Status: Open

Remaining implementation tasks: Expose actual stage artifacts and source edges; implement semantic-versus-format diff; show failed pass and preservation/proof records

Closing evidence and governing contract: Version/profile/target and artifact IDs present? Missing/stale stages shown honestly? Check-elision evidence visible? [IR inspector](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md)

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
