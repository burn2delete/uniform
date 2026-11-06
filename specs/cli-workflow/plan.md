# CLI implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks) and its
actual command/option surface, preserving the selected compiler scope and
historical transfer plan below. Read [spec.md](spec.md), T1/C15 and the full
actual service rule. Locate current dispatcher/adapters and source owners via
[development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Bind explicit arguments/project/profile/target/format/grants, real service,
artifact paths, JSON/text facts and T1 exit class. Supply actual public-command
success/failure cases, both source extensions where relevant, unsupported
options, authority denial and redaction; run emitted output when claimed.
Retain service errors and unavailable behavior without pretending a stub or
metadata report is implementation.

Use focused routing/presentation checks plus affected real service/installed
entrypoint composition. Refine only the selected existing authority, recording
exact commands/artifacts, passed/failed/unrun results and host/seed boundaries.
Preserve current writer and execution restrictions. Selected compiler command,
wider command-family, independent acceptance and release outcomes stay distinct.

## Foundation source criteria

The existing real-service routing task now carries the preserved
`gravity/hosted-hello-execution` clause from the
[foundation context](https://github.com/burn2delete/uniform/blob/df11890b21f71d3f246cc47e9f63d866239a0d68/specs/requirement-reconciliation/foundation-residual-interfaces.edn).
Use [D2's actual hosted hello source and M1 artifacts](../../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md#milestone-1---reader-syntax-and-hosted-hello)
through the real Reader/Syntax/Expand/hosted implementation and the public
[T1 command boundary](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md#requirements).
Bind actual stage products, generated-to-source maps, stdout effect and grant,
execution output, command/options and text/JSON/exit records. Compiler API's
`pipeline`, Legality's expanded-profile facts and Runtime's selected service
products are consumed as concrete interfaces, not whole-component checkboxes.

The named positive must execute Gravity source. Malformed source and removal
of stdout authority must fail at the owning boundary; a host print, supplied
string or static manifest is insufficient. D2's other M1 negatives (unknown
alias, undeclared profile, kernel reflection and lost macro spans) retain their
Reader/Resolve/Expand/Legality producers and actual public diagnostic routes.
T1 exit classes and secret-safe structured output remain applicable. This
hosted milestone fixture neither establishes the selected native compiler
release nor permits held execution. The existing task remains Open.

## Historical workflow-transfer plan

# Implementation plan: CLI workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit the five records and complete selected-command scope only in [tasks.md](tasks.md#implementation-tasks). These original roadmap sections link to the ledger and retain unchanged outcome/acceptance boundaries:

- [remaining tasks and evidence](../../docs/component-roadmaps/cli.md#remaining-tasks-and-evidence)
- [selected compiler cli outcome](../../docs/component-roadmaps/cli.md#selected-compiler-cli-outcome)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: five implementation records, the original selected-command scope and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Compiler API, Build and Runtime supply actual command-family services. Tools owns formatter, editor, debugger and inspection implementations. Release owns installed seedless acceptance.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `cli-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Source command usage increment

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

### Usage diagnostic stage repair

Repair scope (2026-11-05), same finding and owning task: preserve the complete
local T1006 data, including `:stage :command-line`, in the fixture's stderr
comparison. C15 requires stage in structured diagnostics and golden fixtures;
the existing diagnostic presenter already projects `:stage`. Compare the
rendered EDN with the complete exception data rather than removing that field.
Keep the explicit expected diagnostic map and all routing, exit, redaction and
downstream-failure assertions. No entrypoint or presenter behavior changes.
