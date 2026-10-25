# Spec Kit adoption in Uniform

Status: initial adoption draft. Deus owns integration decisions.

This directory supplies work artifacts for bounded changes. The 240 contracts
under [docs](../docs/README.md), their inventory, roadmap, AGENTS.md and existing
evidence workflow remain canonical. No compiler implementation changes here.

## Evaluated official version

GitHub Spec Kit v1.2.3, commit
`5ea8b1cf0a38699340a38e00d02770308997d8c0`, evaluated 2026-10-25.
See [upstream.json](upstream.json) and the preserved [MIT notice](LICENSE.spec-kit).
The official current [repository](https://github.com/github/spec-kit),
[installation](https://github.github.io/spec-kit/installation.html),
[existing-project guide](https://github.github.io/spec-kit/guides/existing-projects.html),
[Codex guide](https://github.github.io/spec-kit/integrations/codex.html) and
[customization guide](https://github.github.io/spec-kit/guides/customization.html)
were evaluated against that pinned release's source, not assumed legacy flags.

## What is installed

- `.specify/scripts/bash/`: six unchanged official helper scripts.
- `.specify/templates/`: official core templates; Uniform overrides in `overrides/`.
- `.specify/local-preset/`: editable Uniform command sources, version 0.1.0.
- `.specify/presets/uniform/`: CLI-installed copy of that local preset.
- `.agents/skills/speckit-*/SKILL.md`: CLI-generated Codex skills; edit the preset,
  never these generated files.
- `.specify/memory/constitution.md`: concise actual user decisions and canonical links.
- `.specify/integration.json`, `init-options.json`, `integrations/`: official state
  and file ownership manifests. Codex is selected; runtime events are disabled.
- `specs/001-paired-source-core/`: pilot spec, plan, tasks and attributed evidence.

The default bundled automated workflow was removed with the official CLI before
copying this additive scaffold. No extensions, community code, hooks, schedules,
external services, credentials or global agent settings are installed.
The local preset replaces the ten generated commands so generic process gates
cannot silently reintroduce repeated approvals or all-feature completion rules.
It preserves the usual spec -> plan -> tasks -> implementation/evidence loop.

## Local use

Committed helpers and skills work without installing the CLI. Python 3 and Bash
are used by the helper scripts. Codex must discover this checkout's local skills;
start/reload a Codex session rooted here after checkout. This existing session did
not dynamically reload them. Invoke inside Codex chat, not a shell:

```text
$speckit-specify Create/update specs/001-paired-source-core from PR356, preserving its declared scope and attributed evidence; do not implement again.
$speckit-plan Use the existing compiler contracts, integration tests and evidence workflow.
$speckit-tasks Separate pilot artifacts, inherited implementation, review and integration.
$speckit-analyze Check consistency without running compiler tests or contacting reviewers.
$speckit-converge Report the bounded pilot outcome and remaining evidence.
```

For non-interactive helper use, avoid a shared feature pointer:

```bash
export SPECIFY_FEATURE_DIRECTORY="$PWD/specs/001-paired-source-core"
bash .specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks
bash .specify/scripts/bash/setup-tasks.sh --json
```

`setup-plan.sh` preserves an existing plan. `create-new-feature.sh` creates work
artifacts; it does not create a Git branch in this release. Inspect `--help` and
`--dry-run` first. Set `SPECIFY_FEATURE_NO_PERSIST=1` if creating a directory
without changing `.specify/feature.json`; that pointer is ignored checkout-local
state. Do not commit it or infer a feature from another worker's checkout.

## Reproducing the scaffold safely

Use an isolated scratch directory. The official existing-project `init --here
--force` may replace conflicting managed files; do not run it over live work.
There was no `.specify`, `.agents` or `.codex` configuration in the baseline or
primary checkout when evaluated. Initialization was tested in a new `/tmp`
directory, inspected, customized, then copied only into absent owned paths.

A project-local virtual environment is optional for maintenance; do not globally
install or change `$CODEX_HOME`:

```bash
python3 -m venv /tmp/uniform-spec-kit-venv
/tmp/uniform-spec-kit-venv/bin/python -m pip install --no-cache-dir 'git+https://github.com/github/spec-kit.git@5ea8b1cf0a38699340a38e00d02770308997d8c0'
/tmp/uniform-spec-kit-venv/bin/specify version
/tmp/uniform-spec-kit-venv/bin/specify init /tmp/uniform-spec-kit-generated --integration codex --integration-options='--events false' --script sh --non-interactive --ignore-agent-tools
```

For this evaluation the same pinned source was cloned, inspected and installed
from its local directory into `/tmp`; dependency versions are recorded in
[evidence](validation/environment.txt). The source pin does not freeze transitive
Python dependencies; evaluate them again before upgrades. `--ignore-agent-tools`
was needed because this execution host has no `codex` executable. It skips only
the executable check and does not prove Codex runtime dispatch.

Within the disposable generated project, the tested maintenance operations are:

```bash
/tmp/uniform-spec-kit-venv/bin/specify workflow remove speckit
# Copy the reviewed .specify/local-preset and templates/overrides into scratch.
/tmp/uniform-spec-kit-venv/bin/specify preset add --dev .specify/local-preset
/tmp/uniform-spec-kit-venv/bin/specify integration status
/tmp/uniform-spec-kit-venv/bin/specify preset list --json
```

For changes to an already installed local preset, use `preset update uniform
--dev .specify/local-preset` only in a clean disposable copy, inspect its diff,
and copy the reviewed additive result. Upstream documents update as remove/add,
without rollback on replacement failure. No automatic update is enabled.

## Migration map and decisions

| Existing source | Authority retained | Spec Kit responsibility |
| --- | --- | --- |
| `docs/document-inventory.json`, `document-sequence.md`, 240 docs | Language and implementation contracts | Feature artifacts link exact owners; never bulk-copy them |
| D0/D1/D2/D3/D6/D8/D9 | Thesis, architecture, ordering, terms, safety and evidence | Constitution points to them; plans explain alignment |
| `docs/implementation-roadmap.md`, phase roadmaps, SH07 plan/backlog | Project goals, milestones and completion status | One feature records its declared increment; no second roadmap |
| `docs/development-verification*.md`, existing tools and receipts | Validation, reuse and authority | Evidence record names revision, owner, checks, outcomes and limits |
| AGENTS.md and active process bridge | Working instructions and exact ownership | Local preset follows them; proposes a small navigation link through owner |
| Existing implementation PR356 | Source/test implementation and its review | Pilot models its bounded workflow, without duplicating code or validation |

The pilot is a dated reference record. Canonical semantics evolve in owning docs;
new work gets a new bounded feature directory. Do not silently promote this
historical record to a living language contract. Deus can later choose a different
persistence model explicitly, as the official existing-project guide recommends.

Later adoption requires decisions about feature-directory naming across workers,
which future slices use this optional workflow, maintained-versus-historical spec
persistence, and CLI/dependency upgrade policy. Parent review of this increment
and a fresh Codex session's skill discovery remain to be completed. Broader
conversion, issue automation, runtime events and workflow execution are outside
this increment; none are prerequisites for current bounded use.

A shared navigation link is proposed through the process bridge: in the existing
development workflow, add `For optional bounded-change planning, see
[Spec Kit adoption](../.specify/README.md). Canonical contracts and evidence rules
remain here.` This PR does not edit shared documentation owned by active writers.
