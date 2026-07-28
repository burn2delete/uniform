# SH-10 Ownership Transition Fixtures

These paired `.gravity` and `.qst` modules construct normalized owned-mutable
requests for the bounded SH-10 initialization, borrow, lifetime, move, and
consume state machine.

The fixtures cover initialized reads, multiple immutable borrows, one exclusive
mutable borrow, explicit moves, bounded borrow escape, and structured rejection
of invalid transitions. Normalized requests carry SHA-256-shaped type and effect
fact links, event identities are unique, and escape events name the exact active
borrow and an explicit supported destination.

They do not claim persistent-copy semantics, range splitting, regions, arenas,
linear resources, concurrency or FFI transfer, runtime borrow checking, unsafe
audits, authenticated typed-core adaptation, or MIR preservation.
