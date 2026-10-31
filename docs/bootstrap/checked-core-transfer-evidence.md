# Checked-core error-transfer coherence and projection diagnostics

This slice moves the remaining two implementation bodies in the template-carrier
component into gravity.checked-core: projection-diagnostic! and
error-transfers-coherent?. D1, D2, D3, D9, L2, L9, C1, C5, C6, C15, BOOT8 and
TEST2 govern it. Inputs are native Core node/transfer records, C5 provenance,
failure reason and explicit source/hash operations. Outputs are the existing
coherence predicate result or thrown C6-VERIFY diagnostic construction record.

The predicate checks transfer ordinals, node/transfer bijection, evaluated child,
evaluation order and shared source, legality and safety facts. It does not prove
type/effect/profile/ownership/safety legality. The diagnostic preserves actual
source path, upstream/source identity, profile and target and the unresolved
diagnostic-id-request. Safe public diagnostic projection and publication are
outside this boundary. Bootstrap retains owner admission and proof lifetime;
Gravity source retains lowering semantics. Neither function grants admission.

Acceptance requires actual current Gravity Core transfer output and real
tampering rejection, complete paired before/after artifacts and diagnostic data,
all-body/arity conservation, declared operations/dependency direction, native
data boundaries stated honestly, root-interposition regressions, proportionate
fresh tests, independent acceptance, exact-head CI and clean integration
preflight. No accounting quota, pin renewal, historical denied execution,
shared CI edit or watchdog change is introduced. This early draft supplies no
acceptance, full verifier, runtime, release, migration or self-hosting claim.
