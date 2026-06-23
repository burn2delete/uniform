# Phase 15 - Bootstrap and Self-Hosting

Defines how Gravity moves from a seed compiler to a mostly self-hosted compiler.

## Documents

- [203 BOOT1 - Bootstrap Strategy](203-boot1-bootstrap-strategy.md)
- [204 BOOT2 - Seed Compiler Design](204-boot2-seed-compiler-design.md)
- [205 BOOT3 - Self-Hosted Compiler Plan](205-boot3-self-hosted-compiler-plan.md)
- [206 BOOT4 - Compiler-in-Gravity Coding Standard](206-boot4-compiler-in-gravity-coding-standard.md)
- [207 BOOT5 - Stage Compatibility Matrix](207-boot5-stage-compatibility-matrix.md)
- [208 BOOT6 - Trusting Trust and Reproducible Bootstrap Plan](208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md)
- [209 BOOT7 - Self-Hosting Validation and Equivalence Plan](209-boot7-self-hosting-validation-and-equivalence-plan.md)
- [210 BOOT8 - Bootstrap Artifact Provenance Specification](210-boot8-bootstrap-artifact-provenance-specification.md)

## Phase Authoring Contract

- Phase 15 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
