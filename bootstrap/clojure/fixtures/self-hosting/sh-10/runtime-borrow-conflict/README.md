# SH-10 Runtime-Checked Mutable Borrow Fixtures

These byte-identical `.gravity` and `.qst` pairs exercise one authenticated
owned-mutable lifecycle. C9 statically orders mutable borrow, end, and reborrow
events, while emitting explicit `:runtime-checked` records for dynamic provider
alias state that the authenticated primitive facts cannot prove.

The accepted fixture initializes a value, borrows it mutably, ends that borrow,
reborrows with a fresh identity, ends the reborrow, and reads the owner. The
rejected fixture covers overlapping mutable and immutable/mutable borrows,
move and consume during an active borrow, stale borrow end, and borrow escape.
The dedicated test also rejects upstream identity, fact, provenance, ownership
fact, and runtime-check substitution.

This family does not provide a general runtime, aggregate ownership, regions,
arenas, linear resources, task/actor/FFI transfer, unsafe audit execution, MIR
preservation, trusted digest resolution, SH-10 completion, self-hosting, seed
retirement, roadmap credit, or release authority.
