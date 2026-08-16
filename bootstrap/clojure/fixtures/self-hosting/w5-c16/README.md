# W5 C16 incremental-execution leaf

This fixture owns the executable C16 incremental-compilation slice.  It is a
stage-owned `:meta` Gravity component, compiled and invoked through the existing
stage2 compiler-artifact plan.  It is intentionally not a global completion,
release, or self-hosting claim.

The engine is
`bootstrap/gravity/src/gravity/compiler/w5_c16_incremental_executor.gravity`.
The accepted and rejected requests are co-canonical byte-identical `.gravity`
and `.qst` sources.

The accepted request constructor requires checkout root, source extension, and
fixture kind; actual `.gravity`/`.qst` paths are retained only in provenance.
It contains a complete `:type-check` cache key, a producer-
bound cache entry, a dependency graph, replay record, provenance, and all C16
invalidation causes.  Execution records one real unchanged reuse plus changed-
input invalidation for source, macro, namespace, type, effect, profile,
capability, safety, proof, dependency, target, backend, plugin, and diagnostic
schema inputs.  Semantic identity excludes actual source paths; provenance
retains them so alternate checkouts remain auditable.

The graph names `:source-unit` as its exact root and requires every declared
node to be reachable from that root through directed dependency edges. A
coherent substitution to another existing node is rejected as `C16-GRAPH`.
Cache reuse additionally
requires the canonical ordered input vector, exact compiler/pass producer,
preserved and invalidated fact sets, local trust, and accepted revalidation.
Revoked trust, rejected revalidation, producer substitution, and coherent
cache-key/input drift all fail closed before publication.

Target scope is exact: `:llvm-x86_64-linux` on Linux/x86_64 with the LLVM,
ELF, and `:sysv-amd64` bindings.  Darwin and Windows targets are ordered
unsupported entries with no Clojure invocation, JVM linking, fallback, or
cross-target inference.

The rejected fixture exposes one total, non-throwing request transformer for
each stable C16 diagnostic family:

- `C16-KEY` - malformed or incomplete stage key;
- `C16-ENTRY` - malformed cache entry;
- `C16-STALE` - stale cache reuse or provenance mismatch;
- `C16-PROOF` - stale proof/certificate;
- `C16-SPECULATIVE` - speculative reuse reaching publication;
- `C16-REPLAY` - incomplete build-effect replay;
- `C16-POLICY` - incompatible profile/target policy;
- `C16-DIAGNOSTIC` - stale diagnostic stream;
- `C16-GRAPH` - dependency graph inconsistency;
- `C16-VERIFY` - recomputation mismatch in the result verifier.

The rejected pair also exercises malformed lineage, replay inputs, and missing
source spans; malformed diagnostics use a deterministic zero-width fallback
span rather than copying invalid input.

Every diagnostic carries its rule, stage, cache-key identity, artifact ID,
invalidating input, source span, manifest entry, profile/target, facts, and
remediation.  The result verifier recomputes the complete result and rejects
mutation or substitution.

The boundary record remains explicit: `:clojure-seed-boundary? true`,
`:self-hosted? false`, `:release? false`, and `:public-authority? false`.
The residual implementation boundary is the Clojure stage2 compiler plan and
JVM stage2 runtime; no filesystem or network cache authority is granted.
