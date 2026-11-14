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


<!-- tooling-source-services-command-family-inventory:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service command-family-inventory criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Implement the complete normative check/build/test/run/repl/fmt/lint/doc/package/registry/audit/verify/inspect-ir/profile/ai/explain command families with relevant explicit project/profile/target/package/output-format options, normalized arguments and actual service/output/artifact contracts. Distinguish implemented, refused and unavailable operations and unsupported options. Reject inferred profile/target where the project requires selection and unstable plugin output. A list of command names, static command-set artifact or selected compiler slice does not implement all families. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-command-family-inventory:end -->


- [ ] `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services` Connect check/run/compile/build/test/package/verify to real services

Status: Open

Evidence baseline: [Compile dispatcher](../../bootstrap/clojure/src/gravity/cli/compile_command.clj)

Closing evidence: Executed success/failure fixtures and artifact paths at public command boundary

<!-- cli-foundation-hosted-hello:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/hosted-hello-execution: Compile the D2 hosted hello source through actual Reader, Syntax, macro expansion and hosted execution, preserving stage/source identities and recording the declared stdout effect and granted authority with the produced output. Refusals: Reject malformed source and the same program without stdout authority; a static hello manifest, host-language print or supplied output string cannot establish execution. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence.
<!-- cli-foundation-hosted-hello:end -->



<!-- tooling-source-services-compiler-service-routing:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service compiler-service-routing criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Connect the actual public check/build/test/run/package/verify boundary as applicable to real selected compiler, Runtime, Build/package, test and verification producers. Exercise genuine success/failure results, relevant capabilities, exact output/artifact manifests and known diagnostic origin. Preserve actual unavailable/unsupported services and owning failure classes; printing a static proof record or injecting a supplied successful result cannot stand in for the claimed service. The selected compiler recipe keeps its separately declared complete command/option closure and both source extensions. Audit remains in the existing specialist-routing task. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-compiler-service-routing:end -->


- [ ] `cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services` Connect REPL/format/lint/doc/inspect/profile/audit/AI/registry/explain services

Status: Open

Evidence baseline: [Tooling contracts](../../docs/phase-13-tooling-and-developer-experience/README.md)

Closing evidence: Each declared command has a genuine service result and negative fixture; no metadata-only success


<!-- tooling-source-services-specialist-service-routing:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service specialist-service-routing criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md), [T2](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md), [T3](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md), [T4](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md).

Route actual repl/fmt/lint and remaining named specialist families to their existing sole service tasks, retaining normalized requests, current source/session/target/grants, real outputs and refusal diagnostics. CLI owns dispatch/presentation; Tools owns formatter/linter/REPL and other actual services, not parallel implementations. Exercise real selected dispatch/service composition and missing service, unsupported option/format, stale request or metadata-only response refusals. Other T5-T13 services remain wider separate source work. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-specialist-service-routing:end -->


- [ ] `cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction` Normalize JSON, text, exit codes and redaction

Status: Open

Evidence baseline: [Diagnostic presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj)

Closing evidence: Golden success/error outputs including source, target, grants and redacted secrets


<!-- tooling-source-services-structured-output-and-exit-classes:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service structured-output-and-exit-classes criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Implement human text plus structured automation for every human-output command, including JSON diagnostics/reports where applicable. Preserve stable shell exits 0 success,1 failed policy/check,2 usage,3 dependency/registry resolution,4 build-graph/artifact verification,5 runtime execution,6 missing required authority/human review; extra structured subcodes do not change these meanings. Exercise actual golden JSON success and failure for every family and text/JSON consistency, source span or manifest path/profile/target/remediation when known. Do not parse prose or accept a static exit-code table instead of actual public-boundary outcomes. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-structured-output-and-exit-classes:end -->



<!-- tooling-source-services-visible-authority-and-secret-output:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service visible-authority-and-secret-output criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Expose actual requested/granted/denied authority and human-review conditions; unsafe/AI/network/shell/secret/publish actions cannot run silently. Redact secret values in text, JSON, diagnostic streams, traces and command execution records. Exercise genuine permitted/denied requests and hidden-network, redaction, missing authority and output-path escape refusals; output outside declared build/output roots requires actual grant. Human integration coordination and scoped native local-model APIs confer no shell, tool or publication grant. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-visible-authority-and-secret-output:end -->



<!-- tooling-source-services-explanation-artifact-and-completion-output:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service explanation-artifact-and-completion-output criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Resolve gravity explain diagnostic codes and artifact ids to actual actionable source/policy context. Artifact-producing build/package commands print and emit genuine paths and manifest hashes; preserve diagnostic streams, execution records, requested traces, shell-completion metadata and golden schemas. Exercise known/unknown diagnostic/artifact resolution and missing/stale/wrong-subject manifest, verification and unsupported JSON/plugin output failures. Artifact labels or unrelated hashes are no verified product. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-explanation-artifact-and-completion-output:end -->


- [ ] `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` Package and verify user entrypoints

Status: Open

Evidence baseline: [Release contract](../../docs/phase-18-binary-distribution-and-seedless-release/README.md)

Closing evidence: Installed invocation, source-extension provenance and unsupported-option rejection


<!-- tooling-source-services-installed-scope-and-wider-tools:start -->

Apply the [command and source service source criteria](../tools-workflow/plan.md#complete-command-and-source-service-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Command/source service installed-scope-and-wider-tools criterion (Open). Sources: [T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md).

Verify the actual installed entrypoint, project/option normalization and artifact locations for its claimed surface, preserving selected compiler versus wider command-family scope. Enumerate all commands/options used by the real selected build/release recipe, retain both .qst/.gravity path/extension provenance and actual produced-executable/public verification results when claimed. Refuse unsupported options and missing artifact/seed-retirement evidence rather than printing success. Existing Phase18/Release gates remain distinct; whole REPL/formatter/linter portfolio completion is no blanket prerequisite unless the actual recipe consumes it. Current implementation and independent source/interface acceptance remain unassessed.
<!-- tooling-source-services-installed-scope-and-wider-tools:end -->

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

## Source command usage increment

Append-only scope for finding
`cli.source-command-usage.check-and-run-compiled-v1` (2026-11-04), owned by
`cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`
in [the existing authority](#implementation-tasks). No new task identity or
completion denominator is introduced. The parent remains Open pending the
remaining command matrix and independent acceptance. Canonical task bodies,
statuses, captured regions and historical migration evidence above are retained;
PR767 readiness changes are not adopted.

The [scoped contract](#source-command-usage-plan) requires local
T1006/exit-2 rejection before service resolution for malformed `check` and
`run-compiled`, with fixed payload-safe diagnostics and unchanged valid routing.
Implementation and fixtures are prepared in the owned entrypoint and
`gravity.cli.source-command-usage-test`; execution and independent acceptance
are pending, as separated in [evidence.md](evidence.md#source-command-usage-increment).

Required fixture names are `check-forwards-source-path`,
`run-compiled-forwards-source-path`, `missing-source`, `blank-source`,
`extra-source`, `unsupported-option`, `service-failure-remains-exit-one` and
`service-t1006-is-not-local-usage`; `non-string-source` covers the injected
boundary's operand type. Positive recording operations establish routing only.
Negative cases require exit 2, zero service resolutions/calls and no raw
secret-marker leakage. Downstream failure controls require the identical
service exception and existing exit 1 even when its ID is T1006.

No tests/checks or Git may execute in this bounded worker. Controller-owned
focused regressions, acceptance dependency controls, language boundary and
document validators remain unrun here; retained refusals remain binding.
Independent acceptance and integration are not supplied by this appendix.

### Usage diagnostic stage repair

2026-11-05, same finding and parent task: the reported focused check found
26 failures because the stderr expectation removed `:stage` although the
presenter retains it. The shared negative-fixture assertion now requires the
complete T1006 payload, including command-line stage. All existing positive,
negative and service-failure controls remain. See the
[repair evidence](evidence.md#usage-diagnostic-stage-repair) for the attributed
failure and unrun controller checks. Parent status remains Open; no independent
acceptance or integration is recorded.

## Source command usage plan

Relocated from the appended `plan.md` source-command usage increment and stage
repair on 2026-11-05. The plan file is an exact SH inspection input; this
appendix retains the scoped implementation contract under the same task
without changing that historical input or its digest. The former ownership
wording about three appended plan/task/evidence sections is superseded by
this location; `plan.md` retains its pre-increment content.

Current scope (2026-11-04): finding
`cli.source-command-usage.check-and-run-compiled-v1`, under the existing
`cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`
task in [tasks.md](tasks.md#implementation-tasks). This is a behavior change,
not a private rename. Apply the local
[implementation skill](../../.agents/skills/speckit-implement/SKILL.md) within
the controller's execution restrictions.

[T1](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md#exit-codes)
owns usage exit 2, invalid-usage diagnostic T1006 and secret redaction;
[C15](../../docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md#redaction)
owns secret-safe diagnostic data. At `gravity.cli.entrypoint/run!`, validate
only `check` and `run-compiled` before dispatch: exactly one string source
operand, nonblank and not starting with `-`, with no remaining arguments.
Reject missing/blank/non-string operands, extra operands and unsupported
options through the existing injected diagnostic/exit boundary. Construct
T1006 from fixed command/usage/remediation text and unavailable context as nil;
never attach raw arguments, paths or causes. Select exit 2 by this local
validation branch, never by a downstream exception's diagnostic ID.

Forward valid requests unchanged through the existing dispatcher. Preserve
legal source-path bytes, output and exactly-once service invocation, including
both `.gravity` and `.qst`. An option-like filename can retain its explicit
`./` path prefix; no option terminator or new option support is added. Reader
still owns extensions, UTF-8 and source authenticity. Build handling, compile
and run parsers, SH07 routing and service exception handling stay unchanged.
Required operand arity is not an input/work quota; add no accounting, grants,
host fallback or compiler semantics.

Own only `bootstrap/clojure/src/gravity/cli/entrypoint.clj`,
`bootstrap/clojure/test/gravity/cli/source_command_usage_test.clj` and these
three appended plan/task/evidence sections. New private helper and test names
describe source-command usage; compatibility diagnostic ID T1006 is retained.
Do not alter canonical task bodies/statuses, captured regions or historical
evidence, and do not adopt PR767 readiness changes.

Starting work needs only the current injected operation interface. Composition
fixtures use the actual entrypoint/dispatcher with recording operations and
the existing diagnostic presenter. They establish routing/presentation only,
not Reader acceptance, actual compiler/runtime service success, installed
execution or final T1 conformance. Scoped acceptance requires the focused
checks and independent review recorded in [evidence.md](evidence.md#source-command-usage-increment).
The wider command matrix and real-service/release dependencies remain open;
they neither block this local implementation nor receive completion credit.

### Usage diagnostic stage repair plan

Repair scope (2026-11-05), same finding and owning task: preserve the complete
local T1006 data, including `:stage :command-line`, in the fixture's stderr
comparison. C15 requires stage in structured diagnostics and golden fixtures;
the existing diagnostic presenter already projects `:stage`. Compare the
rendered EDN with the complete exception data rather than removing that field.
Keep the explicit expected diagnostic map and all routing, exit, redaction and
downstream-failure assertions. No entrypoint or presenter behavior changes.

### SH inspection input repair

2026-11-05, same finding and parent task: controller feedback reported five
acceptance-dependency errors for stale `specs/cli-workflow/plan.md`. Restore
the pre-increment plan and retain its appended contract and repair note here.
Keep all source-digest, provenance and task-body checks unchanged. Existing
positive/negative CLI fixtures remain prepared; controller checks and
independent acceptance remain pending. See the
[repair evidence](evidence.md#sh-inspection-input-repair). Parent status is Open.
