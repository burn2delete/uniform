# SH-26 bounded prior-stage rebuild leaf

This fixture root contains a bounded Gravity-owned validator and deterministic
record builder for a stage-N to stage-N+1 compiler rebuild.

It consumes the exact SH-25 request, complete result, fresh verification, and
final projection as one contextual ingress. Canonical content identities for
all four values are bound through the reusable SH-02 descriptor, digest-request
resolution, sealing, replay, and contextual-verification path. It validates
the authoritative 42-component catalog, output bindings, traversable
prior-stage lineage, controlled environment, physical provenance, typed
pending process records, and structured stage manifest.

The descriptor source revision identifies and pins the authoritative
`authenticated_envelope.gravity` implementation. It intentionally does not
self-identify `stage_rebuild_engine.gravity`, which would create a circular
source-hash requirement for the adapter containing the pin.

The SH-26 revision pin additionally binds the envelope verifier semantic hash
and the exact shapes of all 72 envelope functions. Host digest resolution is
bounded to the exact 47-request graph produced by this 42-component contract.
Each request ordinal, purpose, preimage reference set, dependency edge, and
resolved digest is paired one-to-one. The trusted Clojure seed computes a
canonical root over those exact pairs before Gravity invocation. The
coordinator passes the complete binding, root, descriptor identity, sealed
record identity, replay identity, and contextual-verification identity as a
separate `trusted-context` argument. None of those authority fields are copied
from the rebuild request. Gravity cross-checks the observed request records
against that out-of-band context and binds the trusted-context identity into
the result, rebuild record, and verification. The physical-provenance-bearing
trusted-context identity is not part of the path-neutral rebuild identity
input.

The caller is responsible for keeping `trusted-context` under coordinator
control. Adversarial replacement of that argument is outside this leaf's trust
model; malformed contexts fail closed, while cryptographic construction and
custody remain explicit Clojure-seed authority.

The root is recomputed instead of hard-coded because the final provenance
request intentionally binds checkout-specific physical paths. This is a
bounded SH-26 contract, not a general Gravity-owned digest resolver.

The leaf still does not execute rebuild actions, independently recompute SH-25,
or retire the Clojure seed boundary. It therefore provides no SH-26 completion
or seed-retirement credit.
