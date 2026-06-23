# Phase 03 - Profile System

Phase 3 defines profiles as compile-time contracts. A profile decides which Gravity forms, effects, memory regimes, runtime assumptions, capabilities, and target lowerings are legal before code reaches a backend.

## Phase Decisions

- Every namespace declares exactly one active profile with `(:profile ...)`; libraries that are intentionally reusable declare `(:profiles #{...})` plus per-profile exclusions.
- A profile is checked before target lowering. Backends may refine a profile, but they cannot legalize a feature the profile rejected.
- Effects must fit the active profile and package/deployment effect policy; capabilities must fit the package capability manifest and deployment grant. The narrowest applicable policy wins.
- Cross-profile imports are allowed only through a profile-safe facade or an artifact boundary with typed schemas, explicit effects, and capability evidence.
- Macros execute in meta context but expand into the caller profile. Expansion is rejected if it introduces illegal effects, hidden allocation, host reflection, unsafe code, or profile-incompatible runtime dependencies.
- No profile may rely on implicit undefined behavior. A dangerous operation has a `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island` outcome.

## Documents

- `P1` - [Profile System Specification](046-p1-profile-system-specification.md)
- `P2` - [:core Profile Specification](047-p2-core-profile-specification.md)
- `P3` - [:meta Profile Specification](048-p3-meta-profile-specification.md)
- `P4` - [:hosted Profile Specification](049-p4-hosted-profile-specification.md)
- `P5` - [:native Profile Specification](050-p5-native-profile-specification.md)
- `P6` - [:firmware Profile Specification](051-p6-firmware-profile-specification.md)
- `P7` - [:kernel Profile Specification](052-p7-kernel-profile-specification.md)
- `P8` - [:hardware Profile Specification](053-p8-hardware-profile-specification.md)
- `P9` - [:distributed Profile Specification](054-p9-distributed-profile-specification.md)
- `P10` - [:ai Profile Specification](055-p10-ai-profile-specification.md)
- `P11` - [:gpu / Accelerator Profile Specification](056-p11-gpu-accelerator-profile-specification.md)
- `P12` - [:formal Verification Profile Specification](057-p12-formal-verification-profile-specification.md)
- `P13` - [Profile Compatibility Matrix](058-p13-profile-compatibility-matrix.md)

## Artifact Families

- profile manifests
- effect and capability matrices
- cross-profile dependency graphs
- profile compliance fixtures

## Quality Gates

- compile a positive and negative namespace fixture for each profile
- verify illegal effects are rejected after macro expansion
- verify profile manifests name all dependency profile edges
