---
description: Create evidence-linked Uniform tasks
---

## User Input

```text
$ARGUMENTS
```

Before selecting a feature directory, classify the request with
`.specify/semantic-names.md`. For private rename-only changes, return to the
ordinary branch/PR workflow there and stop: record the old/new map, complete
reference search, focused owner checks and required manifest/digest updates;
create no feature directory or Spec Kit artifact. Otherwise continue only with
this command's applicable bounded workflow. Public/serialized identity,
protocol/schema/diagnostic IDs, ownership, semantics and new evidence are
explicitly not private-only; no substantive edit implicitly requires all four
artifacts.

Read `AGENTS.md`, `.specify/README.md`, `.specify/memory/constitution.md`,
and the owning canonical contracts before changing artifacts. User authorization
and current coordination remain authoritative. Use the selected feature directory
from explicit user input, `SPECIFY_FEATURE_DIRECTORY`, or checkout-local
`.specify/feature.json`; never select another worker's feature implicitly. If
selection is missing, ask only for that missing context. Do not change shared
files owned by another worker. Keep partial work visible in an early draft PR.
Do not add project quotas, repeated approval gates, hooks, services or schedules.

Read the selected `spec.md`, `plan.md` and
`.specify/templates/overrides/tasks-template.md`. If spec or plan is absent,
report that missing prerequisite rather than inventing it. Write dependency-ordered
`tasks.md` using `- [ ] T001 [US1] Description with exact path` (story labels only
where applicable). Track a reviewable declared increment. Check a task only when
its named deliverable and evidence exist; externally reported validation is
explicitly attributed. Keep implementation, review and integration separate.
Do not mark inherited PR implementation complete merely because this planning
exercise is complete. No mandatory all-feature completion gate for draft PRs.

Apply `.specify/semantic-names.md` to newly introduced implementation filenames,
namespaces and application/API names. Use short subsystem/behavior words; keep
planning IDs in metadata/cross-references. Review new names in this change only,
record legacy rename dependencies, and preserve compatibility-bearing IDs.
