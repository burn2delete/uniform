# TEST4 - Profile Compliance Test Plan

Sequence: 193
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines profile compliance tests. Profiles decide which forms,
effects, capabilities, allocation strategies, runtime services, and target
lowerings are legal. Tests ensure that a compiler and toolchain accept allowed
behavior, reject forbidden behavior, narrow checked behavior, and delegate only
where a profile contract permits delegation.

Profile compliance prevents "works on hosted" behavior from leaking into
kernel, firmware, hardware, formal, GPU, distributed, or AI profiles.

## Profile Matrix

The suite covers:

- `:core`;
- `:meta`;
- `:hosted`;
- `:native`;
- `:firmware`;
- `:kernel`;
- `:hardware`;
- `:distributed`;
- `:ai`;
- `:gpu`;
- `:formal`.

Each profile has allowed, checked, delegated, and rejected fixture categories.

## Requirements

- Fixtures MUST declare profile and target.
- Each profile MUST have positive and negative fixtures.
- Forbidden runtime services MUST be rejected at compile time where possible.
- Target-specific delegation MUST be recorded in artifacts.
- Capability rules MUST be tested per profile.
- Allocation and memory assumptions MUST be tested per profile.
- Dynamic eval, reflection, GC, runtime, and host-service assumptions MUST be tested where profile rules mention them.
- Profile compatibility tests MUST cover multi-profile projects.

## Semantic Dependencies

- `P1` through `P13` define profile rules and compatibility.
- `D4` defines universal computing coverage.
- `L6` and `L15` define effects and capabilities.
- `R1` through `R12` define runtime availability.
- `B1` through `B14` define backend targets.
- `PKG11` defines target matrix data.

## Outputs and Artifacts

Profile tests emit:

- profile compliance report;
- profile/target matrix report;
- positive and negative fixture results;
- capability legality report;
- runtime service legality report;
- artifact delegation report.

## Example

```clojure
(deftest kernel-rejects-dynamic-eval
  {:suite :profiles
   :profile :kernel
   :target :native-kernel
   :source "(eval '(+ 1 2))"
   :expect-diagnostic :profile/forbidden-dynamic-eval})
```

## Rejection Rules

- Reject tests without profile/target identity.
- Reject profile fixtures that rely on default hosted assumptions.
- Reject artifact outputs that omit profile legality results.
- Reject multi-profile packages with unresolved compatibility conflicts.
- Reject forbidden effects accepted as warnings when profile requires errors.

## Diagnostics

- `TEST4001` reports missing profile or target.
- `TEST4002` reports accepted forbidden profile feature.
- `TEST4003` reports rejected allowed profile feature.
- `TEST4004` reports missing delegation artifact.
- `TEST4005` reports multi-profile compatibility conflict.
- `TEST4006` reports profile diagnostic severity mismatch.

## Conformance Criteria

- Every profile has allowed and rejected fixtures.
- Profile diagnostics name the violated profile rule.
- Runtime service legality is tested per profile.
- Capability legality is tested per profile.
- Profile/target matrix results are emitted as artifacts.
- Multi-profile package fixtures detect compatibility conflicts.
- Hosted-only assumptions fail in stricter profiles.
