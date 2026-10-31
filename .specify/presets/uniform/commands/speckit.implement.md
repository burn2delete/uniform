---
description: Execute authorized tasks with exact evidence
---

## User Input

```text
$ARGUMENTS
```

Read `AGENTS.md`, `.specify/README.md`, `.specify/memory/constitution.md`,
and the owning canonical contracts before changing artifacts. User authorization
and current coordination remain authoritative. Use the selected feature directory
from explicit user input, `SPECIFY_FEATURE_DIRECTORY`, or checkout-local
`.specify/feature.json`; never select another worker's feature implicitly. If
selection is missing, ask only for that missing context. Do not change shared
files owned by another worker. Keep partial work visible in an early draft PR.
Do not add project quotas, repeated approval gates, hooks, services or schedules.

Private rename-only changes use the ordinary branch/PR workflow described in
`.specify/semantic-names.md`; they do not need a selected feature directory or
four-artifact package. Use this command only when the rename also carries
semantic behavior, public or serialized identity, component ownership or new
acceptance evidence. Required private manifest/digest updates remain part of
the ordinary fast path.

Read the selected `spec.md`, `plan.md`, `tasks.md` and evidence record. Execute
only authorized, owned tasks; reuse existing implementation and receipts when
valid under the canonical development workflow. Run appropriate checks, record
exact commands, revision, result and artifacts, and mark only evidenced tasks.
Continue independent tasks if one is blocked; report the precise blocked action,
reason, completed work and pending/denied/uncertain status. Do not treat unfinished
checklist items as a new permission ceremony. Keep the draft current. Report the
completed declared increment and remaining work; Deus decides integration.

Apply `.specify/semantic-names.md` to newly introduced implementation filenames,
namespaces and application/API names. Use short subsystem/behavior words; keep
planning IDs in metadata/cross-references. Review new names in this change only,
record legacy rename dependencies, and preserve compatibility-bearing IDs.
