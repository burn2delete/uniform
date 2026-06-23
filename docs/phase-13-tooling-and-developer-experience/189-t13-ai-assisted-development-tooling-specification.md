# T13 - AI-Assisted Development Tooling Specification

Sequence: 189
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines AI-assisted development tooling for Gravity. AI
tools may search code, explain diagnostics, propose patches, generate tests,
draft documentation, or review artifacts. They must operate as Gravity AI
agents with typed prompts, tools, memory, policy, evaluation, human-review,
replay, and artifact records.

AI assistance proposes changes. It does not bypass compiler, safety, package,
test, or `:ai/human-review` gates.

## Tooling Modes

Supported modes include:

- diagnostic explanation;
- code search and summarization;
- patch proposal;
- test generation;
- documentation generation;
- refactoring proposal;
- package update review;
- safety audit assistance;
- performance investigation;
- workflow replay analysis.

Each mode declares its model, tools, memory, policy, output schema, and
required checks.

## Requirements

- AI development tools MUST use the Phase 11 AI programming model.
- Patch proposals MUST be emitted as patch artifacts with provenance.
- Generated source MUST be parsed and checked by the compiler before use.
- Generated tests MUST be identified as generated and linked to prompts and inputs.
- Tool calls MUST be limited by the agent toolset and capabilities.
- Write actions MUST require explicit human-review policy.
- Prompt injection defenses MUST apply to repository files, docs, issues, package metadata, and tool output.
- AI output MUST be schema-validated before application.
- AI traces MUST be replayable or marked live-only according to policy.
- Final applied changes MUST record model, prompt, tool, and human-review provenance.

## Semantic Dependencies

- `A1` through `A11` define AI program behavior.
- `T1`, `T4`, `T10`, and `T12` define CLI, lint, IR, and safety integration.
- `PKG10` defines provenance for generated code.
- `A10` defines human-review.
- `TEST1` through `TEST13` define validation checks.

## Outputs and Artifacts

AI tooling emits:

- plan artifact;
- patch artifact;
- generated-source provenance;
- prompt and model ledger links;
- tool-call ledger;
- validation report;
- human-review record;
- replay trace;
- final applied-change provenance.

## Example

```bash
gravity ai propose --task "add schema validator" --emit plan --emit patch-artifact
gravity ai review patch.plan --require-checks type,effect,safety,test
gravity ai apply patch.plan --human-review required
gravity ai replay trace.jsonl --verify-no-hidden-tool-use
```

## Rejection Rules

- Reject AI patches applied without schema-valid patch artifacts.
- Reject generated source not checked by the compiler.
- Reject hidden tool use.
- Reject write actions without required human-review.
- Reject prompts that treat repository content as instruction authority.
- Reject generated tests whose provenance is missing.
- Reject package updates proposed without capability and safety diff.
- Reject AI traces that omit model, prompt, tool, or policy records.

## Diagnostics

- `T13001` reports invalid AI patch artifact.
- `T13002` reports unchecked generated source.
- `T13003` reports hidden or unauthorized tool use.
- `T13004` reports missing required human-review.
- `T13005` reports prompt authority violation.
- `T13006` reports missing generated-test provenance.
- `T13007` reports missing package safety diff.
- `T13008` reports incomplete AI trace.

## Conformance Criteria

- A patch proposal emits plan and patch artifacts.
- Generated source is checked by reader, macro, type, effect, profile, and safety phases before use.
- Applying a patch requires human-review when policy says so.
- Repository files and package metadata are treated as untrusted data in prompts.
- Tool calls are limited to the declared agent toolset.
- Validation reports include type, effect, safety, and test results as requested.
- Replaying an AI trace detects hidden tool use and missing policy records.
