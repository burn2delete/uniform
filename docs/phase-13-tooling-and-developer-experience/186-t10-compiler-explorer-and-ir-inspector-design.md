# T10 - Compiler Explorer and IR Inspector Design

Sequence: 186
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines tools for inspecting compilation stages. The explorer shows
how source forms become syntax objects, macro output, typed core, effect facts,
profile checks, MIR, domain IR, optimized MIR, backend IR, and final artifacts.
It is a debugging and education tool, but it must report the real compiler
state, not a simplified parallel model.

The inspector is scriptable so CI can compare IR and verify preservation.

## Stages

Inspectable stages include:

- reader forms;
- syntax objects;
- macro expansion;
- resolved names;
- typed core;
- effect-checked core;
- ownership and region facts;
- safety analysis facts;
- MIR;
- domain IR;
- optimized MIR;
- backend lowering;
- artifact manifests.

Each stage links back to source spans and upstream stage ids.

## Requirements

- Inspector output MUST include compiler version, project hash, profile, target, and stage id.
- Stage views MUST preserve source span links.
- Type, effect, capability, ownership, and safety facts MUST be shown when available.
- IR diffs MUST distinguish semantic changes from formatting or id renumbering.
- Pass views MUST show input artifact, output artifact, and preservation claims.
- Failed checks MUST show the stage where failure occurred.
- Inspector exports MUST be structured and redacted.
- Generated code and macro output MUST retain origin chains.
- Optimization views MUST show proof or analysis that justifies check elision.
- Backend views MUST show target-specific assumptions.

## Semantic Dependencies

- `C2` through `C18` define compiler stages.
- `L4`, `L5`, and `L6` define macro, type, and effect facts.
- `SAFE15` defines proof artifacts.
- `PERF10` defines check elision evidence.
- `B13` defines artifact emission.
- `T1` defines CLI output.

## Outputs and Artifacts

The inspector emits:

- stage artifact views;
- IR JSON;
- pass diff reports;
- preservation reports;
- source-span maps;
- redacted explorer bundles.

## Example

```bash
gravity inspect-ir src/main.grav --stage typed-core --format json
gravity inspect-ir src/main.grav --stage mir --show-effects --show-safety
gravity inspect-ir --diff before.mir after.mir --verify-preservation
```

## Rejection Rules

- Reject stage exports with missing profile or target.
- Reject IR views that drop source origins.
- Reject optimization diffs without preservation evidence when checks are removed.
- Reject backend views that omit target assumptions.
- Reject explorer bundles containing secrets.
- Reject generated-code views that lose origin chains.

## Diagnostics

- `T10001` reports missing stage identity.
- `T10002` reports lost source origin.
- `T10003` reports missing preservation evidence.
- `T10004` reports omitted backend target assumption.
- `T10005` reports explorer redaction failure.
- `T10006` reports generated origin gap.

## Conformance Criteria

- Each compiler stage can emit a structured view for fixtures.
- Source spans survive from reader through MIR and lowered artifacts where applicable.
- IR diffs ignore stable nonsemantic renumbering.
- Check elision reports cite proof or analysis evidence.
- Failed checks identify the failing stage.
- Backend views name profile, target, ABI, runtime, and assumptions.
- Explorer bundles are redacted and reproducible from artifact ids.
