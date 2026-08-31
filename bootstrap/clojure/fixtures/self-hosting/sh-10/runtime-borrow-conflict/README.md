# SH-10 Runtime-Checked Mutable Borrow Fixtures

These byte-identical `.gravity` and `.qst` pairs exercise one authenticated
owned-mutable lifecycle. C9 statically orders mutable and immutable borrow,
end, and reborrow events, while emitting explicit `:runtime-checked` records
for dynamic alias state that the authenticated primitive facts cannot prove.
Borrow identities are lifecycle-global and cannot be reused after end.

The accepted fixture initializes a value, borrows it mutably, ends that borrow,
reborrows with a fresh identity, ends the reborrow, and reads the owner. The
second accepted scenario proves immutable acquisition is also residual-checked.
The rejected fixture covers overlapping mutable and immutable/mutable borrows,
move and consume during an active borrow, stale or reused borrow identities,
active terminal borrows, invalid lifetime coordinates, and borrow escape. The
dedicated test also rejects upstream identity, fact, cross-carrier provenance,
C8 effect/capability, ownership-fact, provider, failure, and runtime-check
substitution.

Lifetime coordinates use nonnegative finite integral i32 values. The provider
contract is bound to the authenticated meta/JVM profile and exact pure C8
effect/capability evidence. Runtime failure is a structured declared error;
panic is forbidden. Provider selection and execution remain a lowering
obligation rather than a claim of this tranche.

This family does not provide a general runtime, aggregate ownership, regions,
arenas, linear resources, task/actor/FFI transfer, unsafe audit execution, MIR
preservation, provider execution, panic recovery, an independent provenance
issuer, full coordinated provenance substitution resistance, trusted digest
resolution, coordinated fact-identity substitution resistance, SH-10
completion, self-hosting, seed retirement, roadmap credit, or release
authority.
