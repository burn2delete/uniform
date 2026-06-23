# T6 - Debugger Design

Sequence: 182
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines Gravity debugging across hosted, native, workflow, AI,
kernel-like, and no-runtime targets. Debugging maps runtime state back to
source spans, syntax objects, typed core, MIR, effects, ownership facts,
capabilities, and artifacts when the target can expose that data.

The debugger must not create authority that the program did not have. Inspecting
state and mutating state are separate operations with separate permissions.

## Debug Data

Debug artifacts include:

- source maps from target code to Gravity spans;
- symbol tables;
- type and layout maps;
- MIR location maps;
- effect and capability maps;
- ownership and lifetime facts when available;
- runtime stack records;
- workflow event-log indexes;
- AI model/tool ledger links.

Each target declares which debug features it supports.

## Requirements

- Debug sessions MUST identify artifact, profile, target, and debug data version.
- Breakpoints MUST map to source spans or artifact locations.
- State inspection MUST respect redaction and capability policy.
- State mutation MUST require explicit debug authority.
- Workflow debugging MUST support event-log replay and node stepping.
- AI debugging MUST link model calls, prompts, tools, memory, policy decisions, and approvals.
- Native and kernel debugging MUST not hide unsafe memory operations.
- Debug records MUST distinguish optimized-away values from unavailable values.
- Debugger output MUST support structured reports.
- Debugging must degrade explicitly when target support is partial.

## Semantic Dependencies

- `C11` defines MIR.
- `C14` defines target lowering.
- `R12` defines runtime observability.
- `B13` defines artifact debug metadata.
- `A6` defines workflow replay.
- `A11` defines AI safety ledgers.
- `SAFE6` defines unsafe audit context.

## Outputs and Artifacts

The debugger emits:

- debug session records;
- breakpoint records;
- stack and frame reports;
- variable inspection reports;
- replay traces;
- policy denial records;
- source map validation reports.

## Example

```bash
gravity debug --artifact build/app.native --source-map build/app.gmap
gravity debug --break src/main.grav:42 --show-types --show-effects
gravity debug --replay workflow-trace.jsonl --node classify-ticket
```

## Rejection Rules

- Reject debug sessions without matching artifact/debug metadata.
- Reject state mutation without debug authority.
- Reject inspection of redacted values.
- Reject breakpoints that cannot map to source or artifact positions.
- Reject workflow replay that would repeat side effects.
- Reject AI trace views that expose secrets or no-store prompt data.
- Reject claims that optimized-away values are live runtime values.

## Diagnostics

- `T6001` reports debug metadata mismatch.
- `T6002` reports breakpoint mapping failure.
- `T6003` reports debug authority denial.
- `T6004` reports redacted value access.
- `T6005` reports unsafe replay side effect.
- `T6006` reports AI trace redaction violation.
- `T6007` reports optimized-away value.

## Conformance Criteria

- A debug fixture maps source spans to target locations.
- Breakpoints stop at expected source locations where target support exists.
- State mutation is denied without explicit authority.
- Redacted values are hidden in debug output.
- Workflow replay debugging does not repeat side effects.
- AI trace debugging links prompt, model, tool, memory, policy, and approval records.
- Partial target support is reported explicitly.
