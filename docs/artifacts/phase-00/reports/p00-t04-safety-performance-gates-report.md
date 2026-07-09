# P00-T04 Safety And Performance Gate Report

Date: 2026-06-24

Task: `P00-T04` - Safety and performance gate alignment

## Governing Inputs Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/implementation-roadmap.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/phase-00-foundation-and-thesis/README.md`
- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Implemented Surface

- Added `tools/validate_safety_performance_gates.py`.
- Added `docs/artifacts/phase-00/safety-performance-gate-model.json`.
- Added accepted fixture `docs/artifacts/phase-00/fixtures/safety-performance-gates/accepted-gate-records.json`.
- Added rejected fixture `docs/artifacts/phase-00/fixtures/safety-performance-gates/rejected-gate-records.json`.

The gate model binds D6 performance claim fields, D8 legal safety outcomes, and D9 proof certificate fields into one validation surface. The accepted fixture covers the four legal optimization choices: preserve a fact, regenerate a fact, retain a runtime check, or reject the transformation. It also includes an unsafe-island fast path with D8 metadata and audit evidence.

## Accepted Behavior

Model command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/safety-performance-gate-model.json
```

Output:

```text
safety/performance gate model validation passed
```

Accepted fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/fixtures/safety-performance-gates/accepted-gate-records.json
```

Output:

```text
safety/performance gate fixture validation passed: 5 records
```

## Rejected Behavior

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/fixtures/safety-performance-gates/rejected-gate-records.json --expect-failure D6-CHECK-ELISION-UNPROVED
```

Output:

```text
expected diagnostic observed: D6-CHECK-ELISION-UNPROVED
erase-bounds-without-proof erases checks without preserve or regenerate choice
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/fixtures/safety-performance-gates/rejected-gate-records.json --expect-failure D9-CHECK-ELISION-NO-PROOF
```

Output:

```text
expected diagnostic observed: D9-CHECK-ELISION-NO-PROOF
erase-bounds-without-proof erased checks without proof records
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/fixtures/safety-performance-gates/rejected-gate-records.json --expect-failure D8-UNCLASSIFIED-DANGER
```

Output:

```text
expected diagnostic observed: D8-UNCLASSIFIED-DANGER
missing-safety-classification has illegal or missing safety_outcome ':unchecked'
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/fixtures/safety-performance-gates/rejected-gate-records.json --expect-failure D9-CERT-UNCHECKABLE
```

Output:

```text
expected diagnostic observed: D9-CERT-UNCHECKABLE
certificate-missing-checker proof 0 missing fields: ['checker']
```

Rejected fixture command:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_safety_performance_gates.py docs/artifacts/phase-00/fixtures/safety-performance-gates/rejected-gate-records.json --expect-failure D6-FAST-MATH-IMPLICIT
```

Output:

```text
expected diagnostic observed: D6-FAST-MATH-IMPLICIT
implicit-fast-math uses fast math without explicit mode and certificate
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

- This completes only `P00-T04`; Phase 00 remains in progress.
- The accepted records are gate-model fixtures. They do not claim real optimizer, MIR, backend, runtime, benchmark, unsafe-island extraction, or proof-checker support.

## Conformance Rationale

`P00-T04` requires D6, D8, and D9 to share an evidence model for proof-preserving optimization, unsafe islands, and verification records. The model requires D6 claim and benchmark fields, D8's exact four safety outcomes, D8 unsafe-island metadata, and D9 proof certificate fields. Rejected fixtures prove that check erasure without proof, missing safety classification, unchecked certificates, and implicit fast math are release-blocking.
