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

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/auth-workflow/tasks.md#implementation-tasks).

The hosted capability contract explicitly marks itself compatibility-only and
not grant, provider-trust, proof, canonical or release authority. Those facts
prevent treating navigation links as completion. Architectural decision owner:
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
