# TEST5 - Safety Conformance Test Plan

Sequence: 194
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines the safety conformance suite. Safe Gravity has no undefined
behavior: an operation has a `:proven-safe`, `:runtime-checked`, `:rejected`,
or `:unsafe-island` outcome. The suite verifies that claim across memory,
ownership, regions, linear resources, FFI, concurrency, numerics,
capabilities, taint, macros, AI tools, supply chain, proof certificates, and
safety-preserving optimization.

Safety tests are release blockers when a package claims safe behavior.

## Safety Areas

The suite covers:

- null and initialization safety;
- bounds safety;
- ownership and borrowing;
- lifetimes and regions;
- linear resources;
- unsafe islands;
- FFI;
- data-race safety;
- numeric safety;
- capability security;
- taint tracking;
- macro safety;
- AI tool safety;
- supply-chain safety;
- proof and certificates.

## Requirements

- Every safety mechanism MUST have positive, negative, and unsafe-audited fixtures.
- Negative fixtures MUST reject unsafe behavior with stable diagnostics.
- Unsafe fixtures MUST emit audit artifacts.
- Runtime-checked fixtures MUST demonstrate both pass and fail paths.
- Optimization fixtures MUST prove removed checks were redundant.
- Capability and taint fixtures MUST include bypass attempts.
- AI tool safety fixtures MUST include prompt injection and unauthorized tool attempts.
- Safety reports MUST link to source spans, proofs, runtime checks, or audit records.

## Semantic Dependencies

- `SAFE1` through `SAFE16` define safety contracts.
- `L10` and `L11` define memory and concurrency models.
- `C9` and `C10` define safety analysis.
- `PERF10` defines safety check elision.
- `A11` defines AI tool misuse defense.
- `PKG8` defines package safety metadata.

## Outputs and Artifacts

Safety tests emit:

- safety conformance report;
- runtime check report;
- unsafe audit report;
- proof certificate report;
- capability denial report;
- taint flow report;
- optimization check-elision report.

## Example

```clojure
(deftest safe-bounds-check
  {:suite :safety
   :source "(get [1 2] 9)"
   :expect {:outcome :runtime-checked
            :runtime-result :failure}
   :diagnostic :safety/bounds})
```

## Rejection Rules

- Reject safe-code fixtures that compile to undefined behavior.
- Reject unsafe code without audit artifact.
- Reject removed safety checks without proof or analysis evidence.
- Reject capability bypasses accepted at runtime.
- Reject taint sinks without validation.
- Reject AI tool misuse not denied by policy.
- Reject safety reports that omit source spans.

## Diagnostics

- `TEST5001` reports safe-code unsoundness.
- `TEST5002` reports missing unsafe audit artifact.
- `TEST5003` reports invalid check elision.
- `TEST5004` reports capability bypass.
- `TEST5005` reports taint sink violation.
- `TEST5006` reports AI tool safety failure.
- `TEST5007` reports missing safety evidence span.

## Conformance Criteria

- Safe fixtures never rely on undefined behavior.
- Runtime checks fail deterministically for invalid inputs.
- Unsafe islands produce audit records.
- Proof-backed fixtures link to certificates or analysis records.
- Capability bypass attempts are denied.
- Taint flow fixtures show source, validation, and sink.
- AI prompt injection and tool misuse fixtures are denied and recorded.
