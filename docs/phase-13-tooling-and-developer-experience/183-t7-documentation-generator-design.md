# T7 - Documentation Generator Design

Sequence: 183
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines generated documentation for Gravity packages and artifacts.
Documentation generation reads source, syntax metadata, type and effect facts,
schemas, package manifests, artifact manifests, examples, diagnostics, and
safety metadata. It produces human-readable and machine-readable documentation
without inventing claims not backed by compiler or package evidence.

Documentation is part of the artifact graph and may be checked for freshness.

## Documentation Inputs

Inputs include:

- namespaces and public vars;
- doc strings and metadata;
- type signatures;
- effect and capability summaries;
- profile and target availability;
- schemas and generated API docs;
- examples and runnable snippets;
- unsafe and FFI audit summaries;
- package metadata and SBOM summaries;
- diagnostics and rule explanations.

## Requirements

- Generated docs MUST identify source hash, compiler version, and package version.
- Public API docs MUST include type signatures and profile availability when known.
- Effectful APIs MUST show effects and capabilities.
- Unsafe or FFI APIs MUST link to audit metadata.
- Generated schema docs MUST link to schema artifact ids.
- Examples marked runnable MUST be checked by the test or doc-check runner.
- Stale docs MUST be detectable by source and artifact hashes.
- Docs MUST be available in human and structured formats.
- Docs MUST redact secrets and no-store data.
- AI-generated docs MUST be marked generated and checked against source facts.

## Semantic Dependencies

- `L3` defines namespaces.
- `L5`, `L6`, and `L15` define type, effect, and capability facts.
- `S1` defines schema docs.
- `PKG3` defines documentation artifacts.
- `PKG8` defines safety metadata.
- `T1` defines CLI output behavior.
- `TEST7` defines standard library doc validation where applicable.

## Outputs and Artifacts

The generator emits:

- HTML documentation;
- structured JSON documentation;
- search index;
- source link map;
- API signature index;
- schema documentation;
- example validation report;
- documentation artifact manifest.

## Example

```bash
gravity doc generate --from-source src --from-artifacts build/artifacts
gravity doc generate --include-types --include-effects --include-capabilities
gravity doc check --verify-source-spans --verify-examples
```

## Rejection Rules

- Reject publishing docs whose source hash does not match the package artifact.
- Reject public effectful APIs documented without effects or capabilities.
- Reject runnable examples that fail checks.
- Reject unsafe APIs documented without audit links.
- Reject generated docs containing secrets or no-store runtime data.
- Reject AI-generated docs that contradict compiler facts.
- Reject schema docs without schema artifact references.

## Diagnostics

- `T7001` reports stale generated docs.
- `T7002` reports missing effect or capability documentation.
- `T7003` reports failing runnable example.
- `T7004` reports missing unsafe audit link.
- `T7005` reports protected-data leak.
- `T7006` reports generated doc/source mismatch.
- `T7007` reports missing schema artifact link.

## Conformance Criteria

- Documentation artifacts include source, package, and compiler hashes.
- Public APIs show type, effect, capability, and profile facts where known.
- Runnable examples are validated.
- Unsafe and FFI docs link to audit metadata.
- Structured docs are emitted for editor and registry use.
- Stale source changes fail doc checks.
- Protected data is redacted from generated output.
