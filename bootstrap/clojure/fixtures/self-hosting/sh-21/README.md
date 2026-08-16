# SH-21 meta-compiler legality

These co-canonical fixtures exercise the bounded Gravity-authored SH-21
legality leaf. The accepted pair describes a hermetic three-module compiler
pipeline with explicit effects, exact capabilities, replayable build inputs,
pass contracts, generated-code rechecking, safety provenance, and an acyclic
dependency graph in explicit topological order. Collection bounds are checked
before recursive validation. Pass contracts include regenerated facts, and the
compiler lineage is bounded and acyclic. The rejected pair supplies
deterministic invalid mutations.

This leaf does not certify every authoritative compiler module and does not
execute a complete compiler. Authenticated SH-15, SH-17, and SH-19 integration,
whole-compiler `:meta` execution, and seedless execution remain pending.
