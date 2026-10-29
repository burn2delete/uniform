# Auth

Working name: **Auth** (proposed). Semantic key: `auth`.
Owner role: capability and artifact-trust maintainer; current owner unassigned
by this proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

## Purpose and scope

Preserve the distinction between effects, permission to act and trusted evidence.
Own grant/provider validation, attenuation/revocation, phase separation and
artifact verification interfaces. Effects remain owned by [Effects](effects.md).
Signing implementations and custody remain with Build/Release; Auth does not
mint a grant merely because a manifest describes one. Governing contract:
[capability providers](../phase-01-core-language/025-l15-capability-provider-specification.md).

## Release outcome

Every authority-bearing compiler/build/runtime operation in the declared
provider contract is checked against explicit principal, scope, phase and
lifetime; artifact consumers reject stale, forged or mismatched authority
records before using them. Release outcome open.

## Interfaces and dependencies

Inputs: profile-valid core, effect facts, package/deployment grants, provider
contracts, artifact identity and trust policy. Outputs: capability-valid core,
provider selection, denial diagnostics, scoped enforcement and verification
records. Prerequisites: typed/effected core and profile facts. Provider schemas,
negative fixtures and artifact verification can develop in parallel with
Runtime/Build. Neither a future package manager nor final release is a
prerequisite for checking explicitly supplied development grants.

## Remaining tasks and evidence

Implementation task authority: [Spec Kit ledger](../../specs/auth-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/auth_task_projection.clj --write`.

<!-- auth-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Complete provider declaration and deterministic selection | [Hosted contract](../../bootstrap/clojure/src/gravity/capability_validation/contract.clj) | Typed provider fixtures with deterministic selection and incompatible-provider rejection |
| Enforce grant intersection, attenuation, revocation, phase and lifetime | [Authority implementation](../../bootstrap/clojure/src/gravity/capability_validation/authority.clj) | Real allowed operation and denied widening/revoked/wrong-phase operations. Enforce [SAFE10](../phase-02-safety/039-safe10-capability-security-model.md) intersection across source/function, package/dependency, workspace, profile, invocation/deployment and runtime scope. Reject undeclared ambient authority and forbidden declared ambient contexts; check each capability family's scope. Record parent/derived attenuation and lifetime without expansion; enforce provider-specific revocation or reject unsupported requirements unless a static lifetime substitute is valid. Separate build/runtime secrets and prevent public diagnostic, generated-code, cache-key and artifact leakage. Actual external model billing policy remains separate from removed project accounting. |
| Bind compiler decisions to runtime/build enforcement hooks | [Runtime enforcement contract](../phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md) | No externally visible operation after denial; explicit grant provenance end to end. For SAFE10/SAFE13/SAFE14, execute service-boundary allowed/denied fixtures with provider presence/scope, typed error or declared panic and required audit/telemetry; redact secrets. Tool schema checks, side-effect class and human-review policy must precede invocation; package/build authority cannot silently become runtime grants. A compiler authorization record alone is insufficient runtime enforcement evidence. Criterion behavior/pkg6/request-grant-separation: accept Deploy only scoped handles for capabilities requested by the package and allowed by deployment policy; keep source requests, package requests, denials and actual grants distinct. Reject A dependency grants authority, a deployment grants an unrequested capability, or a runtime effect uses ambient authority instead of its scoped handle. Emit Package-to-deployment grant/handle provenance and PKG6005/PKG6006 before the effect. Dependencies: L15, SAFE10, PKG6, R11. Criterion behavior/pkg6/transitive-deny-precedence: accept Check every source-analysis/project/manifest/resolver/deployment/runtime edge; explicit denials remain effective across transitive dependency requests. Reject Any required edge is missing, a transitive request overrides denial, or an action/build proceeds despite conflicting policy. Emit Authority-chain decision with dependency chain/effect/capability/grant source/policy and PKG6004. Dependencies: PKG6, PKG5, SAFE10, R11. |
| Bind artifact trust to exact source, compiler and policy identity | [Artifact pipeline](../../bootstrap/clojure/src/gravity/authenticated_artifact_pipeline.clj) | Tamper, stale-policy and wrong-source rejection at actual consuming boundary |
| Publish conformance and remaining trust assumptions | [Provider contract](../phase-01-core-language/025-l15-capability-provider-specification.md) | Accepted/rejected matrix and named residual trust roots for each provider |
<!-- auth-task-projection:end -->

The hosted capability contract explicitly marks itself compatibility-only and
not grant, provider-trust, proof, canonical or release authority. Those facts
prevent treating projection tests as completion. Architectural decision owner:
capability maintainer. Recommendation: retain separate capability and artifact
trust interfaces inside this project; do not conflate signatures with grants.

## Acceptance and integration

Yes/no gates: can an undeclared or attenuated-away action occur? (must be no.)
Are provider choice and grant provenance reproducible? Are phase/lifetime
violations rejected? Does tampering fail before consumption? (must be yes.)
Integration milestones: compile-time legality before MIR, runtime hooks before
execution, build effects before graph execution, release trust verification
before distribution. Existing wire IDs are preserved; semantic naming changes
require an explicit compatibility migration, not silent schema edits.

## Noncircular analysis interfaces

Artifact authentication is independently callable with source/artifact identity
and trust policy at Reader/Syntax boundaries; it does not require typed core.
Capability analysis follows typed/effected core and profile legality. The
sequence is Types/Effects -> profile legality -> capability validation ->
ownership/safety -> MIR. Legality exposes profile facts before its later
ownership/safety result. Neither component waits for the other's entire release
outcome before producing the intermediate facts it owns.
