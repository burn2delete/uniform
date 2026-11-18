---
name: speckit-implement
description: Execute authorized tasks with exact evidence
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:uniform
---

# Speckit Implement Skill

## User Input

```text
$ARGUMENTS
```

Before selecting a feature directory, apply the ordinary fix workflow in
`.specify/README.md` and the private rename guidance in `.specify/semantic-names.md`.
If the request is a routine fix and neither the user nor the owning contract
calls for Spec Kit artifacts, use the diff and focused regression checks through
normal PR review and CI, then return before feature selection. Otherwise,
continue with the bounded Spec Kit workflow below.

Read `AGENTS.md`, `.specify/README.md`, `.specify/memory/constitution.md`,
and the owning canonical contracts before changing artifacts. User authorization
and current coordination remain authoritative. Use the selected feature directory
from explicit user input, `SPECIFY_FEATURE_DIRECTORY`, or checkout-local
`.specify/feature.json`; never select another worker's feature implicitly. If
selection is missing, ask only for that missing context. Do not change shared
files owned by another worker. Keep partial work visible in an early draft PR.
Do not add project quotas, repeated approval gates, hooks, services or schedules.

Read the selected `spec.md`, `plan.md`, `tasks.md` and any applicable evidence.
Execute authorized, owned tasks and reuse valid implementation and evidence.
Run appropriate checks, report commands and results on the PR, and mark only
evidenced tasks. Write separate evidence only where required by the owning
contract or governed admission.
Continue independent tasks if one is blocked; report the precise blocked action,
reason, completed work and pending/denied/uncertain status. Do not treat unfinished
checklist items as a new permission ceremony. Keep the draft current. Report the
completed declared increment and remaining work; Deus decides integration.

Apply `.specify/semantic-names.md` to newly introduced implementation filenames,
namespaces and application/API names. Use short subsystem/behavior words; keep
planning IDs in metadata/cross-references. Review new names in this change only,
record legacy rename dependencies, and preserve compatibility-bearing IDs.
