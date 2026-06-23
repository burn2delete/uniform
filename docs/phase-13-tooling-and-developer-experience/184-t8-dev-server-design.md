# T8 - Dev Server Design

Sequence: 184
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines the Gravity dev server. The dev server coordinates file
watching, incremental compilation, hot reload, runtime launch, diagnostics,
artifact events, REPL integration, browser or client refresh, workflow replay,
and AI/runtime ledgers. It is a development convenience layer over real
compiler and runtime contracts.

The dev server must not widen capabilities for convenience. It runs with an
explicit profile, target, and grant set.

## Server Responsibilities

The server handles:

- file watching;
- incremental check and build;
- runtime process supervision;
- hot reload and restart;
- diagnostic streaming;
- artifact event streaming;
- REPL endpoint;
- debugger endpoint;
- workflow replay endpoint;
- AI trace endpoint;
- structured status API.

Each endpoint declares authority and redaction policy.

## Requirements

- Dev sessions MUST declare project, profile, target, and capability grants.
- File changes MUST trigger deterministic incremental compiler updates.
- Hot reload MUST preserve type, effect, and profile assumptions or restart safely.
- Runtime processes MUST inherit only declared development grants.
- Diagnostics MUST match CLI check/build diagnostics.
- Artifact events MUST include artifact id, path, profile, target, and build graph node.
- AI and workflow endpoints MUST record replay-relevant nondeterminism.
- Secrets MUST be redacted from logs, browser payloads, and status APIs.
- Dev-only behavior MUST not affect release artifacts.
- Server state MUST be exportable for bug reports without leaking protected data.

## Semantic Dependencies

- `C16` defines incremental compilation.
- `R9` defines interactive runtime.
- `R12` defines observability.
- `T1`, `T2`, `T5`, and `T6` define CLI, REPL, LSP, and debugger links.
- `A6` defines workflow replay.
- `A11` defines AI trace safety.

## Outputs and Artifacts

The dev server emits:

- dev session record;
- diagnostic stream;
- artifact event stream;
- runtime process log;
- hot reload decisions;
- replay trace records;
- redacted bug-report bundle.

## Example

```bash
gravity dev --project gravity.edn --profile hosted --target jvm-21 --watch
gravity dev --grant http/client --deny secrets/read --emit diagnostics-json
gravity dev --profile ai --record-replay dev-replay.jsonl
```

## Rejection Rules

- Reject dev server launch without profile and target.
- Reject runtime effects not granted to the dev session.
- Reject hot reload when checked assumptions are invalidated.
- Reject secret exposure in diagnostics, logs, or status APIs.
- Reject dev-only generated artifacts being used as release artifacts.
- Reject replay-sensitive workflows with unrecorded nondeterminism.
- Reject unauthenticated remote dev endpoints when policy requires auth.

## Diagnostics

- `T8001` reports missing dev profile or target.
- `T8002` reports dev capability denial.
- `T8003` reports unsafe hot reload.
- `T8004` reports secret redaction violation.
- `T8005` reports dev/release artifact contamination.
- `T8006` reports replay recording gap.
- `T8007` reports dev endpoint auth failure.

## Conformance Criteria

- Dev diagnostics match CLI diagnostics for the same project state.
- Runtime processes receive only declared grants.
- Hot reload restarts when assumptions are invalidated.
- Artifact event streams include profile, target, and artifact id.
- AI/workflow dev sessions record replay-relevant events.
- Bug-report bundles redact secrets.
- Dev-only artifacts cannot satisfy release verification.
