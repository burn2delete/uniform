# Auth implementation specification

Implementation scope added under the 2026-11-01 request. Capability and
artifact-trust maintainers retain their current ownership. The historical
transfer specification below establishes no new grant or trust acceptance.

## Behavior and interfaces

Implement [the Auth outcome](../../docs/component-roadmaps/auth.md#release-outcome)
under L15, SAFE10/11/13/14/15, R11, S3/S9 and PKG6/10/12 in the existing
[contract alignment](plan.md#contract-alignment). Distinguish source effects,
capability requests, actual grants, selected providers and artifact trust.
Consume actual checked source facts, principal/package/build/deployment policy,
provider declarations, scoped handles, exact artifact/source/compiler inputs
and required verification evidence. Emit provider decisions, grant derivations,
delegation/revocation/phase/lifetime decisions, enforcement hooks, redacted
audit and denial records, and artifact verification results.

Effects do not grant authority. Dependency resolution, valid signatures,
trusted artifacts and profile allowances do not grant authority. Early artifact
verification can run before semantic compilation using exact inputs and trust
policy; operation admission still requires the actual applicable grants and
provider contract. Build/Release own signing implementation, key custody and
publication; schema/canonical encoding owners supply real validated bytes.
Runtime/build/tool/plugin/domain owners enforce decisions at their actual
action boundaries, with Auth preserving the derivation and failure behavior.

## Existing implementation routes and acceptance

The [sole marked ledger](tasks.md#implementation-tasks) retains
`auth/remaining-tasks-and-evidence/` and the following existing suffixes.
Existing task status, finer requirement joins and receipts retain their scope.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `complete-provider-declaration-and-deterministic-selection` | Validate typed provider kind, effects, capability implementation, profile/target, memory/safety/conformance contracts and actual selection under declared policy. | Reject absent, ambiguous, incompatible or unauthorized providers, implicit host fallback and providers that weaken safe semantics. Emit deterministic decisions with actual declarations, inputs and diagnostics. |
| `enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | Compute effective authority from the applicable source/package/build/deployment/provider restrictions; retain explicit denies, principal, scoped typed handles, delegation attenuation, phase/lifetime and supported revocation semantics. | Reject widening, ambient authority, revoked/expired/wrong-phase handles and requests beyond policy. Unsupported revocation cannot be presented as enforced. Exercise a real allowed action and each selected denial with no action side effect after denial. |
| `bind-compiler-decisions-to-runtimebuild-enforcement-hooks` | Connect actual inferred actions and grants to real compiler, build and runtime dispatch; check caller and tool/plugin contracts, workflow replay, AI human-review and redaction policy where applicable. | Reject missing enforcement edges, caller/callee contract mismatch, unauthorized observability or secret disclosure and deployment widening. Test actual action boundaries across each claimed R11 family, not only manifest validation or a supplied decision. |
| `bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | Verify required canonical schema/content/manifest, exact source/compiler/builder/dependency/recipe identity, safety, provenance, attestations, transitive SBOM and current trust policy at the consuming boundary. Evaluate required policy level/track and key-managed or keyless identity/log/root/freshness/revocation evidence. | Reject tampered, stale, coherently substituted, wrong-source/compiler/policy, noncanonical, incomplete, untrusted or downgraded inputs before use. A valid signature/log entry cannot validate a different subject or satisfy unrelated policy. Preserve named trust roots, failed checks and secret-safe diagnostics. |
| `publish-conformance-and-remaining-trust-assumptions` | Compose actual selected providers, grants, verifiers and consuming actions with accepted/rejected cases, exact provenance, audit outputs and explicit residual host/trust assumptions. | Reject claims beyond exercised provider/action/artifact families or unavailable verification. Historical-rebuild exceptions require their actual policy; they do not silently grant ordinary release admission. Publish remaining unsupported surfaces without converting fixtures into live authority. |

PKG6's package requests and deployment grants remain separate, and dependency
updates expose actual effect/capability expansion. PKG12's transitive SBOM and
subject-bound attestation checks are distinct; reproducibility, isolation,
hermeticity and certified claims each require their own evidence. Unknown
required release schemas fail closed. Model-generated inputs retain generated
provenance and taint until the governing compiler/review conditions are met.

## Starting work and completion

Declared checker/provider/action and canonical-artifact interfaces permit local
work with explicit non-privileged fixtures. Real enforcement and verification
acceptance require actual producing and consuming implementations and the
claimed policy inputs; no complete Build release is needed to start. Preserve
all existing operation restrictions, signing custody, writer reservations and
stopped handoffs. Component acceptance requires actual allowed/denied operations
and rejected trust failures, exact identities, audit/redaction evidence and
named residual assumptions. Independent acceptance and release remain separate.

## Historical task-authority-transfer specification

# Feature specification: Auth implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: capability and artifact-trust maintainer.
Input revision: `846644db7402c26a421eb7afd52615d52f145590`.

## Purpose and canonical contracts

Move the five existing Auth implementation records to a single editable
[Spec Kit ledger](tasks.md#implementation-tasks). Effects, grants and artifact
trust remain distinct. Signing implementations and custody stay with
Build/Release; a signature or manifest does not mint a grant. The
[component roadmap](../../docs/component-roadmaps/auth.md) retains its
acceptance criteria, dependencies and direct task-ledger link. The
[plan](plan.md#contract-alignment) preserves all contract navigation. Normative
contracts and compiler semantics do not change.

## User scenarios and acceptance

### US1 - Find and update a real implementation task

Each existing component/heading/title semantic ID identifies one preserved task
in the sole ledger. Titles, evidence baselines, statuses and closing evidence
remain there, and the roadmap links directly to those records. Checkbox and
Accepted; status must agree. Semantic acceptance still requires an independently
reviewed immutable receipt; navigation cannot grant it.

### US2 - Preserve evidence provenance and historical progress

Relative links resolve from their actual document locations to the same existing
repository files. The runtime-enforcement and provider-contract links remain
rebased in the ledger with their original targets and spelling. Absolute HTTPS
receipts remain supported. The original
migration ledger remains a frozen suffix outside the implementation region; its
checkboxes do not indicate implementation completion.

## Scope and compatibility

Own the Auth roadmap and four workflow artifacts. Existing component data and
link policies are unchanged. Compiler source,
compiler tests, dashboard and normative contracts are unchanged. No external
Specify CLI, automatic discovery or overall completion percentage is claimed.

## Evidence and outcome ownership

The component maintainer retains semantic acceptance authority. Independent
review and integration are separate from local validation. Hosted capability
views lack grant/provider-trust, proof and release authority. This documentation
changes no credentials, permissions or trust roots. See [evidence](evidence.md)
for observations and limits.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Workflow artifacts and structural validation establish no compiler completion,
self-hosting or seed retirement. Grant intersection, attenuation, revocation
and phase/lifetime checks must still be enforced at real consuming boundaries.

## Implementation naming review

`auth-workflow` and its component roadmap name the subsystem and behavior. Existing semantic
IDs and contract references remain compatible. Follow the repository's
[semantic naming guidance](../../.specify/semantic-names.md); no legacy rename
is part of this change.
