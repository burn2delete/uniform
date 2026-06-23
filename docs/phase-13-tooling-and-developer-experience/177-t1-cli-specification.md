# T1 - CLI Specification

Sequence: 177
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the `gravity` command line. The CLI is the stable
automation surface for local development, CI, package workflows, release
verification, and self-hosting. It must expose compiler truth instead of hiding
profile violations, effect errors, capability denials, unsafe islands, or
artifact gaps behind convenience behavior.

Every command that produces human-oriented output also has a structured output
mode suitable for tests and CI.

## Command Families

The CLI includes:

- `gravity check`;
- `gravity build`;
- `gravity test`;
- `gravity run`;
- `gravity repl`;
- `gravity fmt`;
- `gravity lint`;
- `gravity doc`;
- `gravity package`;
- `gravity registry`;
- `gravity audit`;
- `gravity inspect-ir`;
- `gravity profile`;
- `gravity ai`;
- `gravity explain`.

Command families may delegate to other tools, but the CLI owns argument
normalization, output contracts, exit codes, diagnostic routing, and artifact
locations.

## Requirements

- Commands MUST accept explicit project, profile, target, package, and output format options where relevant.
- Commands MUST return stable exit codes.
- Commands MUST support JSON output for automation when they produce diagnostics or reports.
- Diagnostics MUST include code, severity, source span or manifest path, profile, target, and remediation when available.
- Build and package commands MUST emit artifact paths and manifest hashes.
- Commands that request capabilities MUST show grants and denials.
- Unsafe, AI, network, shell, secrets, and package-publish operations MUST not run silently.
- `gravity explain` MUST resolve diagnostic codes and artifact ids to source and policy context.
- CLI output MUST redact secrets.
- CLI behavior MUST be testable through golden fixtures.

## Exit Codes

- `0` means success.
- `1` means diagnostics failed policy or checks.
- `2` means CLI usage error.
- `3` means dependency or registry resolution failed.
- `4` means build graph or artifact verification failed.
- `5` means runtime execution failed.
- `6` means required authority or human-review is missing.

Commands may add subcodes in structured output, but shell exit codes remain
stable.

## Semantic Dependencies

- `C15` defines diagnostics.
- `PKG1` through `PKG12` define project, package, artifact, and signing behavior.
- `T2` through `T13` define specialized command behavior.
- `TEST1` through `TEST13` define conformance usage.
- `R12` defines runtime observability output.

## Outputs and Artifacts

The CLI emits:

- human text output;
- structured JSON reports;
- diagnostic streams;
- artifact path lists;
- command execution records;
- trace files when requested;
- shell-completion metadata;
- golden output fixture schemas.

## Example

```bash
gravity check --project gravity.edn --profile hosted --target jvm-21 --format json
gravity build --target workflow-graph --emit artifact-manifest --emit diagnostics
gravity explain diagnostic PKG5002 --source-span gravity.edn:18:5
gravity audit safety --format json --fail-on unreviewed-unsafe
```

## Rejection Rules

- Reject commands that infer profile or target when the project requires explicit selection.
- Reject operations requiring capabilities without declared grants.
- Reject release commands with missing artifact verification.
- Reject scripts relying on unstable human output when JSON mode is requested but unavailable.
- Reject output paths outside allowed build/output directories unless explicitly granted.
- Reject commands that would expose secrets in logs.
- Reject hidden network access outside package or build policy.

## Diagnostics

- `T1001` reports missing profile or target.
- `T1002` reports unsupported output format.
- `T1003` reports authority denial.
- `T1004` reports artifact verification failure.
- `T1005` reports secret redaction violation.
- `T1006` reports invalid command usage.
- `T1007` reports unstable plugin output.
- `T1008` reports hidden network access.

## Conformance Criteria

- Each command family has at least one golden JSON success fixture and one failure fixture.
- Exit codes match the documented table.
- Diagnostics include profile, target, source span or manifest path when known.
- Secret values are redacted from text and JSON output.
- Artifact-producing commands print and emit artifact manifest paths.
- CI can run `check`, `test`, `build`, `audit`, and `verify` without parsing human prose.
- `gravity explain` resolves known diagnostic codes to actionable context.
