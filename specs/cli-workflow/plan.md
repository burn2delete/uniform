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
