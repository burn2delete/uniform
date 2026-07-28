# SH-14 Compiler Data Layout Leaf Fixtures

This directory contains co-canonical accepted and rejected Gravity source
fixtures for the bounded SH-14 compiler-data layout leaf.

The accepted fixtures cover UTF-8 string bytes, byte storage, symbols,
keywords, tuples, records, variants, vectors, maps, sets, and explicitly
bounded mutable buffers. The rejected fixtures cover missing analysis facts,
duplicate or inconsistent shapes, invalid variant tags, capacity and length
bounds, hidden mutability, invalid allocation regimes, and ABI size or
alignment mismatches.

The leaf validates complete UTF-8 byte structure and byte values, uses signed
64-bit checked size arithmetic, caps element size at 64 bytes and aligned total
size at 128 KiB, and requires exact power-of-two alignment rounding. Fact
lineage values and generated-origin links must use canonical SHA-256-shaped IDs.
Source spans and nonempty origin chains are structural, while checkout paths
remain in provenance and are recursively removed from identity inputs.

The leaf consumes normalized data-operation descriptors and recomputes every
result during verification. Authenticated SH-12 MIR input, target-specific
layout, actual allocation, field-offset calculation, pointer and lifetime
layouts, and removal of the Clojure stage0 execution boundary remain pending.
These fixtures are parallel evidence and do not claim SH-14 completion.
