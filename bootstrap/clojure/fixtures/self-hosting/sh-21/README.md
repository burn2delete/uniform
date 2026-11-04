# SH-21 meta-compiler legality

These co-canonical fixtures exercise the Gravity-authored SH-21
legality leaf. The accepted pair describes a hermetic three-module compiler
pipeline with explicit effects, exact capabilities, replayable build inputs,
pass contracts, generated-code rechecking, safety provenance, and an acyclic
dependency graph in explicit topological order. Pass contracts include
regenerated facts, and the compiler lineage is nonempty and acyclic. The
rejected pair supplies deterministic invalid mutations. The focused Clojure
tests execute the Uniform checker on graphs, origins, lineages, pass facts,
artifacts, build inputs and generated records beyond the former population
limits, including invalid entries beyond those boundaries and result replay.

This leaf does not certify every authoritative compiler module and does not
execute a complete compiler. Authenticated SH-15, SH-17, and SH-19 integration,
whole-compiler `:meta` execution, and seedless execution remain pending.
