# P00-T03 Terminology Enforcement Report

Date: 2026-06-24

Task: `P00-T03` - Terminology enforcement

## Governing Inputs Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/implementation-roadmap.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/phase-00-foundation-and-thesis/README.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`

## Implemented Surface

- Added `tools/validate_terminology_boundaries.py`.
- Added `docs/artifacts/phase-00/diagnostic-namespace-registry.json`.
- Added accepted fixture `docs/artifacts/phase-00/fixtures/terminology-boundaries/accepted-concept-records.json`.
- Added rejected fixture `docs/artifacts/phase-00/fixtures/terminology-boundaries/rejected-conflations.json`.

The registry records D3 concepts, known profile/target/effect/capability/runtime/backend values, terminology boundaries, and diagnostic namespaces. The validator checks registry structure and validates concept records for profile/target, effect/capability, runtime/backend, artifact/file, and unsafe tracking errors.

## Accepted Behavior

Registry command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/diagnostic-namespace-registry.json
```

Output:

```text
terminology registry validation passed
```

Accepted fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/fixtures/terminology-boundaries/accepted-concept-records.json
```

Output:

```text
terminology fixture validation passed: 3 records
```

## Rejected Behavior

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/fixtures/terminology-boundaries/rejected-conflations.json --expect-failure D3-PROFILE-TARGET-CONFLATION
```

Output:

```text
expected diagnostic observed: D3-PROFILE-TARGET-CONFLATION
profile-target-conflation uses target 'llvm-x86_64-linux' as profile
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/fixtures/terminology-boundaries/rejected-conflations.json --expect-failure D3-EFFECT-CAPABILITY-CONFLATION
```

Output:

```text
expected diagnostic observed: D3-EFFECT-CAPABILITY-CONFLATION
effect-capability-conflation uses capability ':http/client' as effect
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/fixtures/terminology-boundaries/rejected-conflations.json --expect-failure D3-AMBIGUOUS-RUNTIME
```

Output:

```text
expected diagnostic observed: D3-AMBIGUOUS-RUNTIME
runtime-backend-conflation uses backend 'llvm' as runtime
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/fixtures/terminology-boundaries/rejected-conflations.json --expect-failure D3-ARTIFACT-UNSTRUCTURED
```

Output:

```text
expected diagnostic observed: D3-ARTIFACT-UNSTRUCTURED
artifact-file-conflation artifact is missing identity fields: ['capabilities', 'compiler_identity', 'effects', 'kind', 'profile', 'provenance', 'safety_status', 'schema', 'source_hash', 'target']
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_terminology_boundaries.py docs/artifacts/phase-00/fixtures/terminology-boundaries/rejected-conflations.json --expect-failure D3-UNSAFE-UNTRACKED
```

Output:

```text
expected diagnostic observed: D3-UNSAFE-UNTRACKED
unsafe-untracked describes unsafe behavior without audit tracking
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

- This completes only `P00-T03`; Phase 00 remains in progress.
- The terminology checker currently validates registry and fixture artifacts. Full-tree normative prose scanning can build on the same registry in later tooling tasks.

## Conformance Rationale

`P00-T03` requires checks that catch D3 term conflation in docs, manifests, and diagnostics. The registry gives D3 diagnostic namespaces and concept names a stable machine-readable home. The validator accepts records that preserve D3 boundaries and rejects every requested conflation class with D3 diagnostics, including profile versus target, effect versus capability, runtime versus backend, artifact versus file, and unsafe behavior without audit tracking.
