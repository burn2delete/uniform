# Feature specification: Core workflow migration

Status: prepared; independent review and integration pending.
Owner: Frontend Spec Kit migration maintainer. Component owner role: compiler core maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Make the existing Core component navigable through the repository's Spec Kit
spec -> plan -> tasks -> evidence workflow without replacing its normative
contracts or copying its implementation task ledger.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/012-l2-core-language-semantics.md) remains normative.
- [Component roadmap](../../docs/component-roadmaps/core.md) owns implementation status and release acceptance.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

A maintainer follows [the plan](plan.md#canonical-work-and-handoffs) to core/1 through core/5 in the owning table.
The link resolves to the existing canonical table, not a copied checklist.
An unresolved link, copied implementation status or claim of completed language
behavior from these documents rejects this migration increment.

### US2 - Distinguish migration from implementation completion

[Tasks](tasks.md) tracks only this package's documentation migration.
[Evidence](evidence.md) distinguishes prepared artifacts, validation, independent
review and integration. Component acceptance remains at its owning roadmap.
The accepted Reader-to-Core invocation pilot does not establish full core-form, typed-core, runtime, self-hosting or release acceptance.

## Scope and compatibility

Own only `specs/core-workflow/{spec,plan,tasks,evidence}.md`.
Do not modify compiler code, normative documents, component task rows, existing
feature packages, schemas, diagnostics or wire IDs. No automatic command
discovery, external Specify CLI execution or whole-project migration is claimed.

## Evidence and outcome ownership

The migration maintainer owns these artifacts and link validation. The compiler core maintainer
owns implementation evidence under existing reservations; this package transfers
no implementation ownership. An independent reviewer owns the review disposition;
Deus coordinates integration. Exact result identities belong in [evidence](evidence.md).

## Unresolved items

Independent review and integration of this new package are pending. Existing
component questions remain in [the roadmap](../../docs/component-roadmaps/core.md), not new approval gates here.

## Implementation naming review

The semantic package name is `core-workflow`. Planning identifiers appear
only in retained contract references. Follow [semantic naming guidance](../../.specify/semantic-names.md);
this migration adds no implementation namespace or legacy rename.
