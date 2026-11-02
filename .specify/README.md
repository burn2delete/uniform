# Spec Kit adoption in Uniform

Status: initial adoption merged in [PR371](https://github.com/burn2delete/uniform/pull/371).
Semantic naming is the successor draft [PR379](https://github.com/burn2delete/uniform/pull/379).
Deus owns review routing and integration decisions.

This is a work-artifact layer for bounded changes. The 240 contracts under
[docs](../docs/README.md), their inventory, roadmap, AGENTS.md and the existing
[development workflow](../docs/development-verification.md) remain canonical.

## Private rename fast path

The canonical classification and fast-path instructions are in
[semantic-names.md](semantic-names.md). Apply them before selecting a feature
directory: a private rename-only change returns to the ordinary branch/PR
workflow, records its old/new name map, complete reference search and focused
owner checks, and creates no feature directory or Spec Kit artifact. Public or
serialized identity, protocol/schema/diagnostic IDs, ownership, semantic
changes and new acceptance evidence remain outside that fast path. Use only
the established bounded-workflow artifacts applicable to the change; no
substantive edit implicitly requires all four. Historical packages remain
authoritative for their original scope and are not rewritten.

## Official version and supported customization

Evaluated 2026-10-25: GitHub Spec Kit v1.2.3, commit
`5ea8b1cf0a38699340a38e00d02770308997d8c0`. See [pin](upstream.json),
[preserved MIT notice](LICENSE.spec-kit) and [dependency observations](validation/environment.txt).
Read official [installation](https://github.github.io/spec-kit/installation.html),
[existing-project guidance](https://github.github.io/spec-kit/guides/existing-projects.html),
[Codex integration](https://github.github.io/spec-kit/integrations/codex.html),
[customization](https://github.github.io/spec-kit/guides/customization.html), and
[lean preset source](https://github.com/github/spec-kit/tree/5ea8b1cf0a38699340a38e00d02770308997d8c0/presets/lean).
The evaluated source, not remembered CLI flags, governed setup.

Uniform uses a local preset with prompt/artifact-only commands, following the
official lean preset's customization pattern. Core spec/plan/tasks templates use
supported project-local overrides. All ten generated commands use this preset,
so generic repeated approvals, project quotas and all-feature completion gates
are not imported. No extension, community code, runtime event, autonomous
workflow, scheduler, credential, service or global agent setting is enabled.

## Versioned file map

| Path | Purpose |
| --- | --- |
| `.specify/memory/constitution.md` | Actual user decisions and canonical links |
| `.specify/local-preset/` | Editable Uniform command sources, v0.2.0 |
| `.specify/presets/uniform/` | Official CLI-installed copy; regenerate from source |
| `.specify/templates/overrides/` | Bounded spec, plan and task formats |
| `.specify/templates/` | Official generated core templates, converted to ASCII |
| `.agents/skills/speckit-*/SKILL.md` | Official CLI-generated Codex skills; do not edit directly |
| `.specify/integration.json`, `init-options.json`, `integrations/*.manifest.json` | Codex selection, events false and upstream ownership baseline |
| `.specify/validation/` | Clojure verifier, observations and adaptation provenance |
| `specs/001-paired-source-core/` | Dated PR356 spec -> plan -> tasks -> evidence reference pilot |

Current [repository language policy](../docs/tooling-language-migration.md)
permits only Clojure tooling and Gravity/Uniform successors. Therefore executable
upstream helpers are deliberately **not vendored**. The shared ownership manifest is narrowed
to the copied text assets, retaining their upstream baseline hashes. This is an
explicit packaging adaptation, not a claim of full upstream initialization.
The external Specify CLI is Python; it was evaluated only in `/tmp` and has no
compiler or evidence authority. This integration does not create a language-policy
exception. Committed skills read/write artifacts directly; they need no Specify
CLI or shell helper. Upstream CLI runtime outside the repository remains optional
for maintenance, not an implementation dependency.

The original Codex manifest tracks upstream core command hashes. The official
preset replaces those commands, so `integration status` reports fourteen modified
managed files (ten preset skills, three ASCII templates and the adapted ignore
file), zero missing/invalid paths and zero unchecked manifests. This is a disclosed
upstream ownership warning, not evidence of clean upstream hashes. Do not use
`upgrade --force` to suppress it. Review/regenerate in scratch. The shared text-only
inventory does not claim helper scripts are installed. No executable helpers
or additional generated infrastructure may be copied into Uniform without a
separate policy decision from Deus.

## Use in Codex

Start/reload a Codex session rooted in this checkout to discover local skills.
The initial execution environment had no `codex` executable; generated skill
files were read and followed manually for the pilot. A later supported fresh
local task on exact `8b9ab88a` automatically discovered all ten skills before
filesystem reads and used the read-only `speckit-analyze` instructions. See
[the attributed observation](../specs/001-paired-source-core/evidence.md#fresh-task-observation).
Literal `$speckit-*` command parsing and a full workflow remain untested.

In Codex chat (not the shell):

```text
$speckit-specify Use specs/001-paired-source-core to model the existing PR356 increment; preserve its immutable evidence scope and do not implement again.
$speckit-plan Use existing contracts and verification conventions.
$speckit-tasks Separate artifact work, reported implementation, review and integration.
$speckit-analyze Check consistency; do not run compiler tests or contact reviewers.
$speckit-converge Report the bounded outcome and remaining evidence.
```

Name the feature directory explicitly. `.specify/feature.json` is ignored,
checkout-local state; never commit it or infer another worker's active feature.
Use `SPECIFY_FEATURE_DIRECTORY` as explicit context when useful. The preset
preserves existing plans and reports missing spec/plan prerequisites. It does not
create Git branches; coordinator ownership stays separate.

## Safe maintenance and validation

The official existing-project `init --here --force` can overwrite conflicting
managed files. This adoption initialized an empty scratch directory, inspected
its output and copied only absent owned text paths. No existing agent/config
files were present in the original checkout or corrected current-main baseline.
The default bundled workflow was removed with the official CLI before copying.
Shared AGENTS/README/development/SH07 files are untouched.

Optional external maintenance, pinned to the reviewed source (not a global install):

```bash
python3 -m venv /tmp/uniform-spec-kit-venv
/tmp/uniform-spec-kit-venv/bin/python -m pip install --no-cache-dir 'git+https://github.com/github/spec-kit.git@5ea8b1cf0a38699340a38e00d02770308997d8c0'
/tmp/uniform-spec-kit-venv/bin/specify version
```

Evaluation installed the same pinned clone from a local path after source review.
The [dependency list](validation/environment.txt) records the resolved environment;
the source commit alone does not freeze transitive dependencies. No venv or Python
source belongs in this repository.

Reproducible checks from the repository root:

```bash
clojure -J-Xmx256m -M .specify/validation/verify.clj
# Optional: repeat actual official scratch initialization/preset regeneration.
clojure -J-Xmx256m -M .specify/validation/verify.clj /tmp/uniform-spec-kit-venv/bin/specify
clojure -J-Xmx256m -M tools/validate_gravity_docs.clj
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The optional verifier runs actual `init --integration codex
--integration-options='--events false' --script sh --non-interactive
--ignore-agent-tools`, removes `speckit` workflow, installs the local preset,
checks inventories, regenerates via supported `preset update --dev`, and compares
generated skills byte-for-byte, all in a fresh disposable directory. It does not
run `workflow run`, Codex, compiler tests, network installation or scripts in
Uniform. `--ignore-agent-tools` discloses the unavailable executable check.
The [results](validation/results.edn) and [pilot evidence](../specs/001-paired-source-core/evidence.md)
separate observed, reported and unrun behavior.

[ASCII adaptations](validation/ascii-adaptations.json) record original and adapted
core-template digests. Local presets/overrides are Uniform-authored text; the
MIT notice covers copied upstream material. For future edits, change the local
preset, validate in scratch, and copy reviewed generated skills/preset state.
Upstream documents preset update as remove/add without replacement rollback;
never run it against live shared work to test a replacement.

## Migration map and later decisions

| Existing source | Canonical authority retained | Spec Kit owns |
| --- | --- | --- |
| Inventory, sequence, 240 docs | Language and implementation contracts | Exact links in bounded feature artifacts; no bulk copying |
| D0/D1/D2/D3/D6/D8/D9 | Thesis, architecture, order, terms, safety and evidence | Constitution references and plan alignment |
| Main/phase roadmaps and SH07 plan/backlog | Goals, milestones and completion status | One declared increment; no competing roadmap |
| Development docs, runners and receipts | Validation, reuse and authority | Attributed revision/command/result/artifact records |
| AGENTS.md and process bridge | Working instructions and exact file ownership | Compliant local command/template behavior |
| Implementation PR356 | Its code, validation and integration disposition | A dated reference workflow, no duplicated implementation |

Old documents remain in place and feature artifacts link their owning sections.
The pilot is a historical reference, not a living replacement language contract.
New work gets its own bounded directory. Later adoption decisions for Deus:
which future slices use Spec Kit, collision-free feature naming across workers,
historical versus living spec persistence, CLI/dependency upgrade policy, and
whether external CLI maintenance remains useful. Broader conversion, issue
integration and automated workflows are outside this increment.

Proposed shared navigation text (through the process bridge only):
`For optional bounded-change planning, see [Spec Kit adoption](../.specify/README.md).
Canonical contracts and evidence rules remain here.` No global approval ticket
or new routine ceremony is required to use the current artifact workflow.

New implementation naming follows [semantic naming guidance](semantic-names.md).
The [naming pilot and incremental legacy audit](naming-pilot/plan.md) is a
bounded adoption handoff, separate from the component registry/Reader roadmap
owned by the parent cloud worker. Planning IDs remain traceability metadata.
