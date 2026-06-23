# T12 - Safety Audit Explorer Design

Sequence: 188
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines the safety audit explorer. The explorer lets developers and
reviewers inspect unsafe islands, memory safety proofs, FFI boundaries,
capabilities, taint flows, AI tool safety, supply-chain metadata, proof
certificates, and safety-preserving optimizations. It works from compiler,
package, runtime, and artifact evidence.

The explorer must not make unsafe code look safe by omission.

## Audit Views

Views include:

- unsafe island index;
- safe wrapper boundary view;
- ownership and lifetime facts;
- region and arena escape analysis;
- FFI boundary map;
- capability graph;
- taint flow graph;
- AI tool and prompt safety view;
- supply-chain and dependency safety view;
- proof certificate view;
- check elision view.

Each view links to source spans and evidence artifacts.

## Requirements

- Unsafe islands MUST be listed with source spans, owner, reason, review state, and evidence.
- Capability graphs MUST distinguish requested, granted, denied, and used capabilities.
- Taint views MUST show source, sink, validation, and policy decisions.
- FFI views MUST show ABI, ownership, lifetime, and error assumptions.
- AI safety views MUST show prompt authority, tool grants, memory taint, and policy denials.
- Package safety views MUST include unsafe metadata, provenance, and SBOM summaries.
- Proof views MUST show certificate and checker identity.
- The explorer MUST support JSON export for audits.
- Missing evidence MUST be visible as missing, not hidden.

## Semantic Dependencies

- `SAFE1` through `SAFE16` define safety contracts.
- `PKG8` defines package safety metadata.
- `PKG10` and `PKG12` define provenance and SBOM links.
- `A8` and `A11` define AI policy and injection defenses.
- `C15` defines diagnostics.
- `R12` defines runtime safety records.

## Outputs and Artifacts

The explorer emits:

- safety audit report;
- unsafe island report;
- capability graph export;
- taint graph export;
- proof certificate index;
- missing evidence report;
- audit decision summary.

## Example

```bash
gravity audit safety --unsafe-islands --capabilities --taint --format json
gravity audit safety --source-span src/ffi.grav:12:1 --explain-proof
gravity audit safety --fail-on unreviewed-unsafe,ambient-authority
```

## Rejection Rules

- Reject audit reports that omit known unsafe islands.
- Reject safe wrapper claims without evidence.
- Reject capability graphs that collapse requested and granted states.
- Reject taint views missing sinks.
- Reject proof claims with no checker identity.
- Reject audit pass results with missing required evidence.
- Reject redaction failures in exported audit bundles.

## Diagnostics

- `T12001` reports missing unsafe island evidence.
- `T12002` reports unsafe wrapper evidence gap.
- `T12003` reports capability graph ambiguity.
- `T12004` reports taint sink omission.
- `T12005` reports proof checker omission.
- `T12006` reports audit evidence gap.
- `T12007` reports audit export redaction failure.

## Conformance Criteria

- Unsafe fixtures appear in the unsafe island index.
- Capability graph fixtures distinguish requested, granted, denied, and used.
- Taint graph fixtures show source-to-sink paths and validation.
- FFI fixtures show ABI and lifetime assumptions.
- AI safety fixtures show prompt authority and denied tool escalation.
- Missing evidence fails configured audit gates.
- JSON audit exports are stable and redacted.
