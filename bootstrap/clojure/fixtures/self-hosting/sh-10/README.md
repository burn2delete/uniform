# SH-10 Ownership Transition Fixtures

These paired `.gravity` and `.qst` modules construct normalized owned-mutable
requests for the bounded SH-10 initialization, borrow, lifetime, move, and
consume state machine.

The fixtures cover initialized reads, multiple immutable borrows, one exclusive
mutable borrow, explicit moves, bounded borrow escape, and structured rejection
of invalid transitions. Normalized requests carry SHA-256-shaped type and effect
fact links, event identities are unique, and escape events name the exact active
borrow and an explicit supported destination.

The current C9 source also contains a separately tested, narrow authenticated
SH-09 adapter. It accepts identity-bound, pure integer, boolean, and string
facts as persistent immutable values and emits read-only ownership requests.
Its single authenticated `.gravity` boundary reuses the actual C7-to-C8
product; `.qst` remains byte-parity evidence only.

The fixtures and adapter do not claim persistent aggregate copy semantics,
owned-mutable SH-09 adaptation, effectful or nonprimitive adaptation, range
splitting, regions, arenas, linear resources, concurrency or FFI transfer,
runtime borrow checking, unsafe audits, trusted digest resolution, or MIR
preservation.
