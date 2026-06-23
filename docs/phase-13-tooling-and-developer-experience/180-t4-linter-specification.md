# T4 - Linter Specification

Sequence: 180
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines `gravity lint`. The linter reports policy,
maintainability, portability, safety, package, and domain rules that are not
necessarily compiler errors. It must use compiler facts instead of text-only
heuristics whenever type, effect, profile, capability, or artifact data exists.

Lints may be warnings or release blockers depending on project policy.

## Rule Classes

Rule classes include:

- syntax style;
- namespace hygiene;
- profile portability;
- effect visibility;
- capability minimization;
- unsafe audit completeness;
- FFI boundary hygiene;
- taint and input safety;
- package and dependency policy;
- artifact and provenance policy;
- AI prompt/tool safety;
- performance hints with evidence;
- documentation completeness.

Each rule has id, severity, inputs, fixability, and profile applicability.

## Requirements

- Lint diagnostics MUST include rule id, severity, source span or manifest path, and explanation.
- Rules using compiler facts MUST name which facts they consumed.
- Auto-fixes MUST preserve reader output or be routed through refactoring checks.
- Lints MUST support baselines and fail-on-new behavior.
- Rule configuration MUST be project-scoped and reproducible.
- Rules MUST declare profile and target applicability.
- Package and artifact lints MUST operate on manifests as well as source.
- AI and tool-safety lints MUST use taint, policy, prompt, and capability facts.
- Lint output MUST support JSON and SARIF-like export.
- Rule docs MUST be available through `gravity lint --explain-rule`.

## Semantic Dependencies

- `C15` defines diagnostic structure.
- `L5`, `L6`, and `L15` define type, effect, and capability facts.
- `SAFE6`, `SAFE10`, `SAFE11`, and `SAFE13` define safety-relevant rules.
- `PKG1` through `PKG12` define package lint inputs.
- `A1` through `A11` define AI safety lint inputs.

## Outputs and Artifacts

The linter emits:

- lint report;
- baseline file;
- rule metadata;
- auto-fix patch artifact when requested;
- SARIF-like report for external systems;
- rule coverage report.

## Example

```bash
gravity lint src --rules profile,effects,safety --format json
gravity lint src --baseline lint-baseline.json --fail-on-new
gravity lint --explain-rule SAFE_UNREVIEWED_UNSAFE
```

## Rejection Rules

- Reject lint auto-fixes that alter semantics without refactoring validation.
- Reject rule configurations with unknown rule ids.
- Reject baselines that hide diagnostics from changed source spans when policy forbids stale baselines.
- Reject lints that claim compiler facts unavailable for the current target.
- Reject release builds when policy maps lint severity to blocker.

## Diagnostics

- `T4001` reports unknown lint rule.
- `T4002` reports stale baseline entry.
- `T4003` reports unsafe auto-fix.
- `T4004` reports unavailable compiler fact.
- `T4005` reports release-blocking lint.
- `T4006` reports invalid rule configuration.

## Conformance Criteria

- A lint report includes stable rule ids and source spans.
- Baseline mode fails on newly introduced diagnostics.
- Auto-fix patches are validated before application.
- Rule applicability changes by profile and target.
- Package manifests and source files can be linted in one run.
- AI safety lints detect untrusted prompt authority and overbroad tools.
- Rule explanation output is stable and machine-readable.
