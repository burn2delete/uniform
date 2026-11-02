# CLI implementation specification

Implementation scope added under the 2026-11-01 request. Existing command and
service owners retain their boundaries. Historical workflow migration below
does not establish real service routing or installed release behavior.

## Behavior and interfaces

Implement [the CLI outcome](../../docs/component-roadmaps/cli.md#release-outcome)
under T1 and DOM20 retained below, C15 and actual service contracts. Explicit
arguments/project/profile/target/package/format/grants become normalized calls
to actual Compiler API, Build, Runtime, tooling and verification services.
Produce stable exits, consistent text/JSON diagnostics and reports, real artifact
paths/manifest hashes, command/audit records and explanations with redaction.

T1's full command families remain in scope. The existing selected compiler
command scope in [tasks.md](tasks.md#selected-compiler-command-scope) enumerates
the exact compiler recipe's required subset and applies the same five records.
It includes both source extensions, emitted-program execution and truthful
full-conformance/self-host verification at the final release boundary. Wider
REPL/formatter/lint/doc/profile/AI/registry UX stays independently required
without becoming an unnecessary prerequisite for the selected compiler build.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`cli/remaining-tasks-and-evidence/`; preserve existing criteria/status, selected
scope, source-family refinements and receipts.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `reconcile-supported-commandoptions-with-normative-families` | Bind each supported command/option to its actual service, input schema, target/profile and output/exit contract; expose implemented, rejected and unavailable behavior. | Reject unsupported usage/formats/options, missing required profile/target and hidden service fallback. The selected recipe cannot invoke an unlisted command as an implicit exemption. |
| `connect-checkruncompilebuildtestpackageverify-to-real-services` | Execute actual compiler/build/runtime/package/verification services through the public command and emit real outputs, paths and manifest hashes. | Exercise legal and invalid source, dependency/build/runtime/authority/verification failures at the public boundary. Metadata-only success, internal-only test calls and fail-closed self-host wrappers cannot establish successful final seedless execution. |
| `connect-replformatlintdocinspectprofileauditairegistryexplain-services` | Route actual service results and artifact/diagnostic context, including known-ID explanations; preserve service-specific scope and policy. | Reject unsupported service operations, incomplete/unknown IDs, hidden network/host actions and missing authority. Each claimed command needs genuine success/failure evidence; the CLI does not implement every underlying tool itself. |
| `normalize-json-text-exit-codes-and-redaction` | Preserve the same C15 facts in text/JSON, including known source/profile/target, grants/denials and remediation. T1 exits distinguish success (0), policy/check failure (1), usage (2), resolution (3), build/verification (4), runtime (5) and required authority/review (6). | Assert golden structured success/error cases, actual exits, secret redaction and artifact paths. Reject prose-only automation, inconsistent severities/facts/exits, leaked secrets and swallowed blocking diagnostics. |
| `package-and-verify-user-entrypoints` | Exercise actual installed invocation, supported options, both co-canonical source paths/extensions and emitted executable; identify the exact selected compiler/runtime/build boundary. | Reject installation/target/option/provenance mismatch and bootstrap-hosted packaging presented as seedless release. Retain actual command transcripts and exact candidate/fixture/consumer identity. |

DOM20 script/process/file operations retain typed argument/path/command schemas,
scoped roots, taint-safe quoting, timeout/error mapping, dry-run/audit records and
applicable human-review policy with their domain/runtime owners. Routing a
command cannot grant deployment, shell, secret or publication authority.

## Starting work and completion

Argument/schema fixtures and declared service interfaces allow local command
work before all services finish. Actual command acceptance requires real
services and public-boundary success/failure evidence. Whole CLI completion
requires all declared families; selected compiler command acceptance and Release
remain narrower separate outcomes. Preserve reserved writers, operation holds
and applicable custody/policy restrictions; no task status changes here.

## Historical workflow-transfer specification

# Feature specification: CLI workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: developer-tooling maintainer; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Public command routing, argument and option behavior, structured/text output, exit status and declared artifact paths.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/cli.md). Language rules stay in their retained contracts; implementation records and the selected-command scope are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [DOM20 - Scripting, Shell and Automation Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/143-dom20-scripting-shell-and-automation-domain-specification.md)
- [T1 - CLI Specification](../../docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; no second editable implementation checklist exists. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/cli-workflow/{spec.md,plan.md,tasks.md,evidence.md}`. No compiler/runtime source, existing contract, shared helper, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The developer-tooling maintainer retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

The selected compiler recipe must enumerate its actual command/options closure before that release slice can be accepted; wider command families remain independently open. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `cli` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).
