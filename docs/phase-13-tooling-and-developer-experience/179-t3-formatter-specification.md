# T3 - Formatter Specification

Sequence: 179
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines `gravity fmt`. The formatter rewrites source layout
without changing reader output, syntax identity that matters to macros, source
metadata, comments, or profile semantics. Formatting is deterministic,
round-trip checked, and suitable for CI.

The formatter is intentionally not a refactoring engine. It may rearrange
whitespace, indentation, line breaks, and alignment, but semantic changes
belong to compiler-aware refactoring tools.

## Formatting Inputs

The formatter reads:

- source text;
- reader grammar version;
- edition;
- formatter configuration;
- comment and metadata positions;
- optional maximum line width;
- ignore ranges;
- generated-source policy.

It produces formatted text and a report describing changed files.

## Requirements

- Formatting MUST preserve reader output.
- Formatting MUST preserve comments and metadata.
- Formatting MUST be deterministic for the same inputs.
- `--check` MUST return nonzero when formatting changes are needed.
- `--write` MUST not alter generated files unless policy allows it.
- Formatter configuration MUST be discoverable from the project file or explicit CLI option.
- The formatter MUST support diff output and JSON reports.
- Formatting MUST not require macro expansion.
- Formatting MUST reject source that the reader cannot parse.
- Round-trip tests MUST compare syntax objects or normalized reader forms.

## Rules

Default rules cover:

- list indentation;
- binding form indentation;
- map and vector layout;
- metadata placement;
- namespace form layout;
- multiline string handling without content mutation;
- comment anchoring;
- trailing whitespace removal;
- final newline insertion.

Profile-specific syntax may add rules, but cannot change the core round-trip
guarantee.

## Semantic Dependencies

- `L1` defines surface syntax.
- `C2` defines reader implementation.
- `C3` defines syntax objects.
- `L4` and `C4` define macro-sensitive syntax metadata.
- `T1` defines CLI behavior.

## Outputs and Artifacts

The formatter emits:

- formatted files or patches;
- JSON change report;
- reader round-trip report;
- diagnostics for parse failures;
- formatter version and configuration hash.

## Example

```bash
gravity fmt src --check --format json
gravity fmt src --write --line-width 100
gravity fmt --verify-round-trip syntax-fixtures/
```

## Rejection Rules

- Reject unreadable source.
- Reject formatting changes that alter reader output.
- Reject generated file writes without generated-source policy.
- Reject formatter configuration that conflicts with project edition.
- Reject lossy comment or metadata movement.
- Reject hidden semantic refactors under formatter commands.

## Diagnostics

- `T3001` reports reader parse failure.
- `T3002` reports round-trip mismatch.
- `T3003` reports generated-source write denial.
- `T3004` reports invalid formatter configuration.
- `T3005` reports comment or metadata preservation failure.
- `T3006` reports semantic change attempted by formatter.

## Conformance Criteria

- Formatting the same file twice produces identical output.
- Formatted output reads to the same normalized syntax objects.
- `--check` fails when changes are needed and succeeds after formatting.
- Comments and metadata survive representative fixtures.
- Generated files are protected by default.
- JSON reports list files, changed ranges, diagnostics, and formatter version.
- Formatter tests cover namespace forms, binding forms, macros, collections, comments, and metadata.
