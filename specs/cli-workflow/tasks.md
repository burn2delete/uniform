# CLI implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is claimed. All rows remain Open; transfer is not implementation
acceptance. Original outcome and integration boundaries remain in the roadmap.

<!-- cli-implementation-tasks:start -->
- [ ] `cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families` Reconcile supported command/options with normative families

Status: Open

Evidence baseline: [Dispatcher](../../bootstrap/clojure/src/gravity/cli/dispatch.clj)

Closing evidence: Command matrix that distinguishes implemented, rejected and unavailable operations

- [ ] `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services` Connect check/run/compile/build/test/package/verify to real services

Status: Open

Evidence baseline: [Compile dispatcher](../../bootstrap/clojure/src/gravity/cli/compile_command.clj)

Closing evidence: Executed success/failure fixtures and artifact paths at public command boundary

- [ ] `cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services` Connect REPL/format/lint/doc/inspect/profile/audit/AI/registry/explain services

Status: Open

Evidence baseline: [Tooling contracts](../../docs/phase-13-tooling-and-developer-experience/README.md)

Closing evidence: Each declared command has a genuine service result and negative fixture; no metadata-only success

- [ ] `cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction` Normalize JSON, text, exit codes and redaction

Status: Open

Evidence baseline: [Diagnostic presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj)

Closing evidence: Golden success/error outputs including source, target, grants and redacted secrets

- [ ] `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` Package and verify user entrypoints

Status: Open

Evidence baseline: [Release contract](../../docs/phase-18-binary-distribution-and-seedless-release/README.md)

Closing evidence: Installed invocation, source-extension provenance and unsupported-option rejection
<!-- cli-implementation-tasks:end -->

## Selected compiler command scope

The existing `cli/compiler-commands` outcome below is the sole editable
selected-scope authority, generated back under its original roadmap heading.
Its three closure steps apply the five implementation records above; they
are not additional task IDs or a second completion denominator.

<!-- cli-compiler-commands-scope:start -->
`cli/compiler-commands` is the finite CLI slice required by the selected
compiler distribution, not the entire command-family outcome above. Owner:
CLI maintainer, current owner unchanged/unassigned by this proposal. Its inputs
are the selected compiler source/project/target, declared policy and grants,
locked build/package records, conformance fixtures and release evidence. Its
outputs are actual checked/compiled/executed artifacts, build/test/package and
audit/verification reports, diagnostic explanations, text/JSON and stable exit
codes for that selected surface.

The required public inventory follows the [final phase-18 gate](../../docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md#final-phase-18-completion-gate):
`check`, `run`, `compile`, execution of the produced binary, final `test` (or
the explicitly accepted equivalent full-language conformance runner through
the self-hosted public binary), and `self-host verify` (or its explicitly
accepted equivalent through that same boundary). Both `.qst` and `.gravity`
must work with actual path/extension provenance. In addition, the exact
compiler build/release recipe must enumerate every build, package, audit,
verification or diagnostic-explanation command and option it actually invokes;
each is required for this slice when used. Their wider T1 command families
remain open independently, not blanket prerequisites and not silently waived.
The slice does not replace the existing reproducibility, package, SBOM, signing
or verification obligations with an unrecorded script. Verification must truthfully reject incomplete seed retirement until
the release evidence exists; a fail-closed verifier is not a seedless pass.

Finite closure work, all open, uses the relevant canonical task rows above:

1. Reconcile this inventory with the exact compiler build/release recipe and
   its source/service closure; record all required options and artifact paths.
   Any additional invoked command becomes an explicit requirement, not an
   implicit exemption or an assertion of whole-family support.
2. Connect every selected command to its actual compiler/build/runtime/package/
   verification service; execute positive and negative public-boundary fixtures,
   including both extensions and running the emitted executable.
3. Verify JSON/text consistency, diagnostic explanations, exit codes, grant
   denial and redaction; bind results to the exact candidate and installed
   entrypoint. Record seed/host boundaries separately from semantic success.

Acceptance is yes/no: do all listed required commands execute their real
services, preserve provenance and reject the relevant invalid inputs? Does the
actual recipe use only commands/options covered by those receipts? Are installed
check/run/compile and the output executable exercised through the release
boundary? Does verification distinguish incomplete from accepted release
proof? All answers must be yes for their stated boundary; final seedless
acceptance remains Release's separate gate.

REPL, formatting, linting, documentation generation, profiling, AI and registry
UX remain wider CLI/service outcomes unless the declared compiler recipe truly
uses them. They are not blanket prerequisites for selected compiler release,
and excluding them from this slice does not close or waive their wider outcomes.
[Developer Tools](../../docs/component-roadmaps/tools.md#release-outcome) owns those actual service
implementations; CLI owns their command routing. Selected-command parsing and
fixtures can develop in parallel with services; actual integrated acceptance
requires those services, not completion of the entire Developer Tools portfolio.
<!-- cli-compiler-commands-scope:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier task-source statements are separate from the
current implementation authority and do not imply product completion.

# Tasks: CLI workflow migration

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

All new paths use `cli-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
