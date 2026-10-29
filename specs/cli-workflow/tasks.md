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

Closing evidence: Command matrix that distinguishes implemented, rejected and unavailable operations T1 acceptance: enumerate all sixteen normative command families, relevant project/profile/target/package/format options and shell-completion metadata. Reject missing required explicit profile/target and unsupported formats with stable T1001-T1008 diagnostics where applicable; wider families retain their own service dependencies.

- [ ] `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services` Connect check/run/compile/build/test/package/verify to real services

Status: Open

Evidence baseline: [Compile dispatcher](../../bootstrap/clojure/src/gravity/cli/compile_command.clj)

Closing evidence: Executed success/failure fixtures and artifact paths at public command boundary T1 acceptance: actual command execution records and artifact paths/manifest hashes retain declared grants/denials. Reject ungranted effects, silent unsafe/AI/network/shell/secrets/publish operations, hidden network, output-directory escape without grant and missing release artifact verification. Foundation requirement gravity/hosted-hello-execution: Compile the D2 hosted hello source through actual Reader, Syntax, macro expansion and hosted execution, preserving stage/source identities and recording the declared stdout effect and granted authority with the produced output. Refusals: Reject malformed source and the same program without stdout authority; a static hello manifest, host-language print or supplied output string cannot establish execution. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence. Release extension release/public-check-command: Through the public gravity executable, check declared hello, core and nontrivial applications for both .qst and .gravity. Retain expected check results/artifacts, exact source path/extension, executable/compiler identity and normalized command stdout/stderr. Internal calls or relabelled extensions are not public command evidence. Reject missing parity with P18T04001 and invalid inputs with owning diagnostics. Release extension release/public-run-command: Run each declared hello/core/nontrivial application through public gravity under its declared runtime/grants for both canonical extensions; compare actual output/exit with fixture expectations. Preserve supplied source path/kind and normalized transcripts. Reject missing public execution, incorrect output or hidden delegated runtime failure; manifest presence and internal evaluator results do not establish public run. Release extension release/public-compile-output-command: Execute gravity compile for declared hello/core/nontrivial applications in both canonical extensions, including requested -o locations. Retain actual executable outputs/hashes, sidecars/provenance, compiler identity and original source paths/extensions. Reject missing/substituted executable, ignored output location, invalid target, provenance mismatch and command parity failure; supplied candidate records alone are not compilation. Release extension release/generated-application-execution: Invoke the exact artifacts emitted by public compile, including the declared core-app executable, and compare hello/core/nontrivial outputs/exits under declared runtime or no-runtime boundaries. Bind execution to emitted hashes and original provenance for both extensions. Reject missing, substituted, non-executable or behaviorally wrong outputs; successful compilation cannot replace generated-application execution. Release extension release/public-rejection-diagnostics: Exercise parser, semantic/arity, capability, package and release invalid applications through the same public executable. Preserve owning stable code, applicable span/manifest/profile/source context, non-success exit and normalized stdout/stderr; prove paired extension/path rejection parity. Include applicable L1-DELIMITER, L2-FUNCTION-ARITY, P4-HOST-CAPABILITY, PKG10001 and B13-RELEASE cases. Reject swallowed failure, generic substituted categories and invalid success. Release extension release/public-subset-test-bridge: Exercise gravity test's bootstrap-hosted current-public-subset bridge through accepted check/run/compile/generated-execution and rejected stable-diagnostic cases, retaining source paths and both canonical extensions. Proof names actual subset and hosting. Reject test --full and full-language/self-hosted-runner overclaims with P18T04006. Retain this bounded surface separately from final self-hosted full-language conformance. Release extension release/public-incomplete-selfhost-verifier: While final seed retirement or release is incomplete, public gravity self-host verify writes an honest incomplete proof with active compiler source path/extension and seed facts, then fails closed with P18T04007. Invalid self-host invocation rejects with P18T04008. Exercise both through the public boundary. Incomplete proof production or hosted command availability cannot establish final successful self-host verification.

- [ ] `cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services` Connect REPL/format/lint/doc/inspect/profile/audit/AI/registry/explain services

Status: Open

Evidence baseline: [Tooling contracts](../../docs/phase-13-tooling-and-developer-experience/README.md)

Closing evidence: Each declared command has a genuine service result and negative fixture; no metadata-only success T1 acceptance: gravity explain resolves known diagnostic codes and artifact ids to actionable source/policy context. Each invoked T2-T13 service keeps its own semantic owner and positive/negative service evidence.

- [ ] `cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction` Normalize JSON, text, exit codes and redaction

Status: Open

Evidence baseline: [Diagnostic presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj)

Closing evidence: Golden success/error outputs including source, target, grants and redacted secrets T1 acceptance: golden JSON success/failure per family; exits 0 success, 1 policy/check, 2 usage, 3 dependency/registry, 4 build/artifact, 5 runtime, 6 missing authority/review. Reports include code/severity/source-span-or-manifest/profile/target/remediation when available, artifact paths/hashes and redacted text/JSON; CI check/test/build/audit/verify consume JSON. Reject unavailable requested JSON and unstable plugin output.

- [ ] `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` Package and verify user entrypoints

Status: Open

Evidence baseline: [Release contract](../../docs/phase-18-binary-distribution-and-seedless-release/README.md)

Closing evidence: Installed invocation, source-extension provenance and unsupported-option rejection Release extension release/bootstrap-boundary-disclosure: While a public command is bootstrap-hosted, gravity --version, gravity help and emitted evidence truthfully disclose hosting and seedless=false; gravity-bootstrap remains the explicit audit/recovery entrypoint. Exercise hosted and eligible seedless states. Reject hosted wrapper seedless assertions with P18T01001 and packaged JVM final-release assertions with P18T02001. Early packaging cannot establish final release. Release extension release/public-launcher-dispatch: Exercise installed bin/gravity dispatch through each declared executable/packaged boundary, preserving delegated exit status, stdout/stderr, stable diagnostics, source path/extension and artifact identity. Select the final binary only under exact proven eligibility; otherwise retain the labelled packaged bootstrap fallback and explicit recovery path. Reject command parity, package/provenance and unsupported-target mismatches without selecting an ineligible artifact.
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
