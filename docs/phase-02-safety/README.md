# Phase 2 - Safety

Defines the rules that make safe Gravity code memory-safe, race-safe, auditable, and capability-governed.

## Safety Dependency Spine

The PDF makes safety part of the language semantics, not an optional lint layer. Phase 2 therefore depends on this chain:

`L2 Core Semantics -> L5 Type System -> L6 Effect System -> L10 Memory -> L11 Concurrency -> L15 Capability Providers -> SAFE1 Safe Semantics -> SAFE2 Memory Safety -> SAFE3 Ownership/Borrowing -> SAFE4 Regions -> SAFE6 Unsafe Audit -> SAFE16 Conformance`

Every safe operation must fall into exactly one outcome path: `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`. No backend, macro, AI tool, package, or optimization may introduce a fifth path.

Critical safety documents for the first implementation pass are `SAFE1`, `SAFE2`, `SAFE3`, `SAFE6`, `SAFE8`, and `SAFE10`; the remaining documents close the proof, policy, and conformance gaps around those contracts.

## Documents

- [030 SAFE1 - Safe Gravity Semantics](030-safe1-safe-gravity-semantics.md)
- [031 SAFE2 - Memory Safety Model](031-safe2-memory-safety-model.md)
- [032 SAFE3 - Ownership, Borrowing & Lifetimes](032-safe3-ownership-borrowing-and-lifetimes.md)
- [033 SAFE4 - Region and Arena Safety](033-safe4-region-and-arena-safety.md)
- [034 SAFE5 - Linear Resource Safety](034-safe5-linear-resource-safety.md)
- [035 SAFE6 - Unsafe Code and Audit Model](035-safe6-unsafe-code-and-audit-model.md)
- [036 SAFE7 - FFI Safety](036-safe7-ffi-safety.md)
- [037 SAFE8 - Concurrency and Data-Race Safety](037-safe8-concurrency-and-data-race-safety.md)
- [038 SAFE9 - Numeric Safety](038-safe9-numeric-safety.md)
- [039 SAFE10 - Capability Security Model](039-safe10-capability-security-model.md)
- [040 SAFE11 - Taint Tracking and Input Safety](040-safe11-taint-tracking-and-input-safety.md)
- [041 SAFE12 - Macro Safety](041-safe12-macro-safety.md)
- [042 SAFE13 - AI Tool Safety](042-safe13-ai-tool-safety.md)
- [043 SAFE14 - Supply-Chain Safety](043-safe14-supply-chain-safety.md)
- [044 SAFE15 - Safety Proof and Certificate Model](044-safe15-safety-proof-and-certificate-model.md)
- [045 SAFE16 - Safety Conformance Test Plan](045-safe16-safety-conformance-test-plan.md)

## Phase Authoring Contract

- Phase 2 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
