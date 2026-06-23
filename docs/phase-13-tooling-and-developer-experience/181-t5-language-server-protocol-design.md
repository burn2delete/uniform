# T5 - Language Server Protocol Design

Sequence: 181
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines Gravity's language server. The server presents compiler
truth to editors through diagnostics, completion, navigation, semantic tokens,
hover, code actions, rename, formatting hooks, inline type/effect display,
artifact navigation, and safety explanations.

The LSP server is a view over the project compiler state. It does not invent
editor-only semantics.

## Server State

The server maintains:

- open document text;
- parsed syntax objects;
- namespace graph;
- macro expansion graph;
- type and effect facts;
- profile and target selection;
- diagnostics;
- package manifest facts;
- artifact index;
- cancellation and incremental compilation state.

State updates are incremental but must converge to the same results as CLI
`gravity check`.

## Requirements

- Diagnostics MUST match compiler diagnostic codes and spans.
- Hover MUST expose type, effects, capabilities, profile constraints, and docs when known.
- Completion MUST respect namespace, profile, visibility, and target constraints.
- Code actions MUST be safe by default and identify required checks.
- Rename MUST respect namespace and macro expansion boundaries.
- Formatting MUST delegate to the formatter contract.
- LSP requests MUST be cancellable.
- The server MUST support multiple profiles and targets per workspace.
- Generated files MUST be marked and traced to source artifacts.
- Editor output MUST not expose secrets from project manifests or runtime records.

## Semantic Dependencies

- `C15` defines diagnostics.
- `C16` defines incremental compilation.
- `L3` defines namespaces.
- `L4` and `C4` define macros and expansion boundaries.
- `T3` defines formatting.
- `T4` defines lint integration.
- `PKG1` defines project configuration.

## Outputs and Artifacts

The server emits:

- protocol responses;
- diagnostics;
- semantic token streams;
- code action records;
- editor trace logs;
- fixture transcripts for conformance.

Trace logs redact secrets and are disabled or scoped by user policy.

## Example

```bash
gravity lsp --stdio --project gravity.edn
gravity lsp --diagnostics include-profile,effects,capabilities,safety
gravity lsp --export-fixtures editor-protocol-fixtures/
```

## Rejection Rules

- Reject code actions that bypass compiler checks.
- Reject rename across macro-generated bindings when identity is ambiguous.
- Reject completions for symbols illegal in the active profile.
- Reject editor-only diagnostics that conflict with compiler diagnostics.
- Reject trace logs containing secrets.
- Reject generated-file edits unless the source artifact permits edits.

## Diagnostics

- `T5001` reports server/compiler diagnostic mismatch.
- `T5002` reports unsafe code action.
- `T5003` reports ambiguous rename.
- `T5004` reports profile-illegal completion.
- `T5005` reports trace redaction violation.
- `T5006` reports generated-file edit denial.

## Conformance Criteria

- LSP diagnostics match CLI diagnostics on fixture projects.
- Completion respects namespace, profile, and target.
- Hover exposes type and effect facts for checked forms.
- Rename preserves binding identity and rejects ambiguous macro cases.
- Formatting requests delegate to `gravity fmt` behavior.
- Code actions emit patch artifacts or edits with required validation checks.
- Protocol fixtures cover open, change, save, diagnostics, hover, completion, rename, and code actions.
