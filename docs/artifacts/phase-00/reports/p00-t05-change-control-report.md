# P00-T05 Change-Control Workflow Report

Date: 2026-06-24

Task: `P00-T05` - Change-control workflow

## Governing Inputs Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/implementation-roadmap.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/phase-00-foundation-and-thesis/README.md`
- `docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Implemented Surface

- Added `tools/validate_change_control.py`.
- Added `docs/artifacts/phase-00/change-control-workflow.json`.
- Added `docs/artifacts/phase-00/cross-phase-ambiguity-log.json`.
- Added accepted fixture `docs/artifacts/phase-00/fixtures/change-control/accepted-language-identity-change.json`.
- Added rejected fixture `docs/artifacts/phase-00/fixtures/change-control/rejected-under-evidenced-bootstrap-change.json`.

The workflow defines review paths for language identity, safety guarantees, profile legality, artifact provenance, and bootstrap trust. Each path names governing documents, affected surfaces, reviewers, required evidence, and blocking diagnostics. The ambiguity log records resolved cross-phase ambiguity points that downstream work must not reopen silently.

## Accepted Behavior

Workflow command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/change-control-workflow.json
```

Output:

```text
change-control workflow validation passed
```

Ambiguity log command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/cross-phase-ambiguity-log.json
```

Output:

```text
cross-phase ambiguity log validation passed
```

Accepted request command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/fixtures/change-control/accepted-language-identity-change.json
```

Output:

```text
change-control request fixture validation passed
```

## Rejected Behavior

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/fixtures/change-control/rejected-under-evidenced-bootstrap-change.json --expect-failure P00-T05-MISSING-EVIDENCE
```

Output:

```text
expected diagnostic observed: P00-T05-MISSING-EVIDENCE
bootstrap-trust request missing evidence: ['bootstrap-equivalence-evidence', 'compatibility-analysis', 'conformance-updates', 'reproducible-build-evidence', 'trusting-trust-analysis']
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/fixtures/change-control/rejected-under-evidenced-bootstrap-change.json --expect-failure P00-T05-AFFECTED-SURFACES
```

Output:

```text
expected diagnostic observed: P00-T05-AFFECTED-SURFACES
bootstrap-trust request does not name an affected governed surface
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/fixtures/change-control/rejected-under-evidenced-bootstrap-change.json --expect-failure D2-SAFETY-DEFERRED
```

Output:

```text
expected diagnostic observed: D2-SAFETY-DEFERRED
safety weakening requires safety-review evidence
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/fixtures/change-control/rejected-under-evidenced-bootstrap-change.json --expect-failure D9-BOOTSTRAP-EQUIV
```

Output:

```text
expected diagnostic observed: D9-BOOTSTRAP-EQUIV
bootstrap trust changes require equivalence evidence
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_change_control.py docs/artifacts/phase-00/fixtures/change-control/rejected-under-evidenced-bootstrap-change.json --expect-failure D2-ARTIFACT-MISSING
```

Output:

```text
expected diagnostic observed: D2-ARTIFACT-MISSING
artifact provenance changes require artifact-schema-updates evidence
```

## Repository Validation

Command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Output:

```text
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

## Residual Risks

- This completes only `P00-T05`; Phase 00 remains in progress.
- The accepted change request is a workflow fixture and does not approve a real language identity change.
- Later governance documents still need their own concrete RFC, compatibility, migration, and approval artifacts.

## Conformance Rationale

`P00-T05` requires a review path for edits that alter language identity, safety guarantees, profile legality, artifact provenance, or bootstrap trust. The workflow names those categories directly, binds each to governing documents and reviewers, and rejects requests that lack migration notes, compatibility analysis, conformance updates, safety review, provenance updates, or bootstrap equivalence evidence when required. The ambiguity log records cross-phase decisions so downstream implementation work has an explicit review target instead of relying on unstated assumptions.
