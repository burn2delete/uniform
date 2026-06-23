# GOV4 - Security Review Process

Sequence: 234
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The security review process defines when and how Gravity changes are reviewed for authority, isolation, taint, secrets, unsafe code, FFI, packages, AI tools, generated code, runtimes, and supply-chain risk.
Security review protects the core safety thesis that safe Gravity has no undefined behavior and no hidden ambient authority.
It also protects users from packages, tools, providers, and generated artifacts that make authority or trust unclear.

Security review is required for changes that alter capability semantics, introduce new effectful APIs, expose unsafe islands, change package provenance, add host delegation, expand AI tool authority, or affect secret handling.
The review result is a durable artifact.

## Requirements

- Security-impacting changes MUST have a security review record before acceptance.
- Capability changes MUST include authority model, least-privilege analysis, negative fixtures, and redaction policy.
- Secret-handling changes MUST include lifetime, memory, serialization, diagnostic, and artifact redaction evidence.
- Taint-sensitive changes MUST include source, propagation, validation, and sink fixtures.
- FFI, unsafe, compiler plugin, and host delegation changes MUST include audit records and boundary tests.
- AI tool and agent changes MUST include prompt-injection probes, tool authorization tests, `:ai/human-review` policy, and eval gates.
- Package and registry changes MUST include provenance, signing, SBOM, dependency-confusion, and malware-risk analysis.
- Security exceptions to compatibility MUST cite GOV2 and include migration guidance.
- Vulnerability handling MUST preserve embargo, advisory, patch, backport, and release evidence.
- Review records MUST name residual risk and accepted mitigations.

## Review Surfaces

- Capabilities and effects.
- Filesystem, network, process, database, platform, crypto, and hardware APIs.
- Unsafe code and FFI boundaries.
- Macro expansion, compiler plugins, and generated code.
- Package manifests, registries, provenance, signatures, SBOMs, and dependencies.
- AI prompts, tools, model calls, memory, `:ai/human-review`, evals, and generated actions.
- Runtime adapters, host delegation, workflow replay, and distributed execution.
- Diagnostics, logs, reports, and artifacts that may expose secrets.

## Dependencies

- `SAFE6`, `SAFE7`, `SAFE10`, `SAFE11`, `SAFE13`, `SAFE14`, `SAFE15`, and `SAFE16` for unsafe islands, FFI, capabilities, taint, AI tool safety, supply-chain safety, proof evidence, and conformance.
- `A7`, `A8`, `A10`, and `A11` for AI memory, policy, `:ai/human-review`, and prompt-injection defenses.
- `PKG6`, `PKG8`, `PKG10`, and `PKG12` for capability manifests, safety metadata, provenance, signing, and SBOMs.
- `STD8`, `STD9`, `STD11`, `STD13`, `STD16`, `STD17`, and `STD18` for effectful and security-sensitive standard modules.
- `GOV1`, `GOV2`, `GOV7`, `GOV9`, and `GOV10` for evolution, compatibility, experiments, unsafe review, and package governance.

## Security Record

```clojure
{:id "SEC-0001"
 :change "STD13-tool-policy-update"
 :surfaces #{:ai-tool :capability :taint :ai/human-review}
 :threat-model "prompt-injection-tool-escalation"
 :evidence [:negative-fixtures :redaction-tests :ai/human-review-tests :eval-probes]
 :residual-risk "tool result can still contain hostile text"
 :decision :accepted-with-mitigation}
```

The record is attached to the RFC or change record.
It is retained in release provenance for audit.

## Review Gates

- Threat modeling identifies assets, actors, trust boundaries, attacker capabilities, and mitigations.
- Capability review checks least privilege and denial fixtures.
- Taint review checks untrusted input propagation to sinks.
- Secret review checks redaction, serialization, diagnostics, memory, and retention.
- Unsafe/FFI review checks safe wrappers and audit records.
- AI review checks prompt-injection, tool authorization, `:ai/human-review`, memory, eval, and generated action controls.
- Supply-chain review checks package identity, provenance, signatures, SBOM, yanks, advisories, and dependency risks.
- Compatibility exception review checks whether security fixes break stable behavior and how migration is communicated.

## Outputs and Artifacts

- Security review records with threat model, evidence, decision, and residual risk.
- Capability matrices and denial fixtures.
- Taint propagation and sink validation fixtures.
- Secret redaction and serialization fixtures.
- Unsafe, FFI, plugin, host delegation, and generated-code audit records.
- AI prompt-injection, tool misuse, `:ai/human-review`, and eval evidence.
- Supply-chain risk records, advisories, and backport plans.
- Security compatibility exception records.

## Rejection Rules

- Reject authority expansion without capability review.
- Reject secret handling without redaction and retention evidence.
- Reject taint-sensitive features without propagation and sink fixtures.
- Reject unsafe, FFI, plugin, or host delegation changes without audit records.
- Reject AI tool expansion without prompt-injection and authorization tests.
- Reject package policy changes without provenance and supply-chain analysis.
- Reject security compatibility breaks without GOV2 classification and migration guidance.
- Reject releases with unresolved high-risk review findings unless explicitly accepted by governance.

## Diagnostics

- `GOV4001` when a security-impacting change lacks a review record.
- `GOV4002` when capability expansion lacks least-privilege evidence.
- `GOV4003` when secret data can leak through diagnostics, logs, reports, or artifacts.
- `GOV4004` when taint propagation or sink validation is missing.
- `GOV4005` when unsafe, FFI, plugin, or host delegation lacks audit evidence.
- `GOV4006` when AI tool authority bypasses policy, `:ai/human-review`, or eval gates.
- `GOV4007` when package changes lack provenance, signatures, SBOM, or dependency-risk analysis.
- `GOV4008` when security exceptions to compatibility lack migration guidance.

## Conformance Criteria

- Security review is mandatory for authority, unsafe, FFI, AI tool, package, secret, or host-delegation changes.
- Review records name threat model, evidence, decision, and residual risk.
- Negative fixtures prove denied capability, taint, secret, and tool-misuse cases.
- Security-breaking changes are classified in compatibility records.
- Release artifacts preserve security review references and advisory/backport status.
- Audit records are available after release for every accepted security-sensitive change.
