# W5 Wave5 Slice B typed/effect/safety execution leaf

This fixture family exercises a static `:meta` orchestration record over
receipts supplied by the restructuring-owned C7 type, C8 effect, profile,
capability, C9 ownership, and C10 safety validators. The leaf does not redefine
their semantics. Effects and capabilities remain separate receipt domains.

The accepted fixture is valid, incomplete, and nonauthoritative. Each
safety-sensitive operation has exactly one `SAFE1` outcome from
`:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`. Source and
validator provenance remain explicit, while identity input excludes the actual
checkout path. No full-language or 240-document credit is granted.

The JVM is only the Clojure stage0 stage2 seed harness. The frozen candidate is
Linux/x86_64/LLVM/ELF/`:sysv-amd64`. Unsupported targets are exactly `:darwin`,
`:darwin-arm64`, `:darwin-x86_64`, and `:windows`; each denies Clojure
invocation, JVM linking, and fallback. Public, self-hosted, and release
authority remain false.

The rejected fixture provides mutations for validator receipts, receipt
lineage, `SAFE1` classification, provenance, evidence, authority, and result
substitution. Provenance validation requires the exact ordered IDs
`:prov-c7`, `:prov-c8`, `:prov-profile`, `:prov-capability`, `:prov-c9`, and
`:prov-c10` at each concrete C7-C10 receipt position; the top-level vector is
recomputed from those receipt-local IDs. Duplicate, reordered, or substituted
receipt IDs are rejected even when the top-level vector remains canonical.
Verification is fail closed and emits stable structured diagnostics.

Later focused validation command (not run by this static-only slice):

`clojure -M:test --namespace gravity.self-hosting.w5-typed-effect-safety-executor-test`
