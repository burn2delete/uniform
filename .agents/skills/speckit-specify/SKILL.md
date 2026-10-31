---
name: speckit-specify
description: Specify one bounded Uniform change
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:uniform
---

# Speckit Specify Skill

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

Use the private rename fast path from `.specify/semantic-names.md` when the
request only renames private implementation files, namespaces or helpers and
updates their private callers, tests, load order, documentation and required
manifests/digests. Do not
create a feature directory or four-artifact package for that change; use the
ordinary branch/PR workflow with an old/new name map, complete reference search
and focused owner checks. Use this command when the change also affects public
or serialized identities, component ownership, semantics or acceptance evidence.

Create or update `spec.md` in the explicitly selected feature directory using
`.specify/templates/overrides/spec-template.md`. Create only an absent owned
directory after checking current feature names. Write the checkout-local
`.specify/feature.json` only if useful, using a repository-relative
`feature_directory`. Branch/worktree ownership stays with the coordinator. For an existing PR, record its immutable head and observed scope;
do not recreate implementation or retrospectively assert full conformance.
Name user scenarios, accepted/rejected behavior, canonical links, scope exclusions,
owner and evidence expectations. Do not bulk-convert existing contracts.

Apply `.specify/semantic-names.md` to newly introduced implementation filenames,
namespaces and application/API names. Use short subsystem/behavior words; keep
planning IDs in metadata/cross-references. Review new names in this change only,
record legacy rename dependencies, and preserve compatibility-bearing IDs.
