# W5 Wave6 Slice C tooling/conformance verifier

This fixture owns a bounded static `:meta` tooling and conformance evidence
record.  It is an executable data consumer only: accepted means structurally
valid, incomplete, blocked, and nonauthoritative.  It does not assert a public
`bin/gravity`, plugin or package authority, backend conformance, self-hosting,
release readiness, full-language support, or any 240-document matrix credit.

The engine is
`bootstrap/gravity/src/gravity/self_hosting/w5_tooling_conformance_verifier.gravity`.
The accepted and rejected fixtures are co-canonical byte-identical `.gravity`
and `.qst` files.  Source provenance retains the actual path, extension,
source kind, project-relative path, check artifact path, and replay artifact
path.  Semantic identity is path-neutral and uses placeholder identities only;
no final hashes are generated. Canonical request entry points require the actual
source path, extension, and source kind, so the `.gravity` and `.qst` programs
are invoked separately without a hardcoded extension default.

Provenance binds the normalized suffix exactly: `.gravity` requires a
`.gravity` path and `:gravity`, while `.qst` requires a `.qst` path and `:qst`.
Suffix substitutions reject with stable `W5-TC-PROVENANCE` diagnostics.
Every tooling span freezes the exact `:source-id`, `:start-byte`, `:end-byte`,
`:line`, and `:column` keyset and types, with nonnegative byte offsets,
valid start/end order, and one-based line/column values. Missing, extra,
mistyped, negative, and reversed spans are direct rejected fixtures.

Nested source provenance and request provenance are each validated and must be
exactly equal, including normalized path suffix, extension, and source kind.
Cross-kind substitutions reject with stable `W5-TC-PROVENANCE` diagnostics.

The request keeps five records distinct: source input, structured checker
output, deterministic replay, narrative explanation, and conformance suite.
Narrative text cannot substitute for structured checker facts.  Replay cannot
substitute for a check.  Source, checker, replay, and narrative substitutions
are rejected with stable diagnostics carrying source spans and provenance.
The result verifier recomputes from the request and rejects a substituted
result with `W5-TC-SUBSTITUTION`.

The candidate target is exactly `:llvm-x86_64-linux` with Linux, x86_64, LLVM,
ELF, and `:sysv-amd64` facts.  Unsupported targets are ordered exactly
`:darwin`, `:darwin-arm64`, `:darwin-x86_64`, and `:windows`; each has
`:invokes-clojure? false`, `:links-jvm? false`, and `:fallback? false`.
Cross-target inference and fallback are denied.  The only residual boundary is
the Clojure stage0 stage2 compiler-plan and JVM stage2 runtime harness.

Later validation command (intentionally not run for this static-only change):

`clojure -M:test --namespace gravity.self-hosting.w5-tooling-conformance-verifier-test`
