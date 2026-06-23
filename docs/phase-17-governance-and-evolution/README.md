# Phase 17 - Governance and Evolution

Defines how the language, standard library, targets, unsafe code, and ecosystem change over time.

## Documents

- [231 GOV1 - Language Evolution Process](231-gov1-language-evolution-process.md)
- [232 GOV2 - Compatibility Policy](232-gov2-compatibility-policy.md)
- [233 GOV3 - Standard Library Governance](233-gov3-standard-library-governance.md)
- [234 GOV4 - Security Review Process](234-gov4-security-review-process.md)
- [235 GOV5 - Target Support Policy](235-gov5-target-support-policy.md)
- [236 GOV6 - RFC Process](236-gov6-rfc-process.md)
- [237 GOV7 - Experimental Feature Policy](237-gov7-experimental-feature-policy.md)
- [238 GOV8 - Deprecation and Stabilization Policy](238-gov8-deprecation-and-stabilization-policy.md)
- [239 GOV9 - Unsafe Code Governance Policy](239-gov9-unsafe-code-governance-policy.md)
- [240 GOV10 - Ecosystem Package Governance Policy](240-gov10-ecosystem-package-governance-policy.md)

## Phase Authoring Contract

- Phase 17 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
