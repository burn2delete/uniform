# SH-16 MIR Optimization Leaf Fixtures

This directory contains co-canonical accepted and rejected Gravity source
fixtures for the bounded SH-16 optimization leaf.

The accepted fixtures cover deterministic constant folding under exact integer
semantics, branch simplification, pure dead-operation removal, runtime-check
retention, locally recomputed proof-backed bounds-check removal, no-op pass
decisions, and
checkout-path-neutral identity. The rejected fixtures cover unverified MIR,
duplicate operation identity, missing effect ordering, effectful removal,
missing or invalid check-removal proof, claimed-dead operations that still
have uses, policy-check removal, stale safety
outcomes, incomplete provenance, undeclared nondeterminism, and unsupported
opcodes.

The leaf accepts exact normalized request, operation, proof, span, and origin
schemas. Dead-code elimination recomputes direct operation uses within the
bounded operation vector; it does not trust the supplied no-use claim alone.
Operation and runtime-check identities are unique, operands and provenance are
bounded, and the accepted profile/target pair is `:native`/`:portable-mir`.
Only concrete bounds-check elision is enabled in this leaf. Other check classes
remain pending until their proof conditions can be replayed rather than trusted
from descriptor status fields.

Authenticated SH-15 input, complete C11 MIR adaptation, whole-function
translation validation, target-lowering proof preservation, a self-hosted
certificate checker, and removal of the Clojure stage0 execution boundary
remain pending. This leaf is early executable evidence and does not claim
SH-16 completion.
