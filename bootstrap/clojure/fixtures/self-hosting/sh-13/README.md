# SH-13 functions and control flow

These co-canonical fixtures exercise the bounded SH-13 normalized-MIR leaf in
`gravity.compiler.c11-mir-specification`.

The accepted pair covers multiple typed functions, conditional branches and
joins, a counted loop, direct self recursion, bounded indirect dispatch, and
the distinct return, error-return, throw, and panic exits. The rejected pair
contains only deterministic mutations of an accepted module so malformed MIR
is rejected before execution. The leaf verifies required checked-core, type,
effect, ownership, capability, safety, source-map, and MIR-verifier lineage;
operation-local fact references; block reachability; SSA value ordering;
operation identity; call signatures; effect ordering; source origins; and
bounded signed 64-bit arithmetic. Cross-function recursion is rejected because
mutual-recursion analysis remains outside this leaf.

This fixture family does not claim authenticated SH-12 adaptation, closures,
mutual-recursion analysis, dynamic or vtable dispatch, the complete C11
operation set, a target runtime, or seedless runtime execution. Its indirect
dispatch case is a statically selected callee constrained by a nonempty
allowlist; it is not dynamic dispatch.
