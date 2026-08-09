# W5 Wave5 Slice A reader-module execution leaf

This fixture owns a static, stage2 `:meta` reader/module record.  It is a
non-authoritative executable data boundary, not a completed reader, compiler,
self-host, release, or full-language implementation.  It grants no FL/240
credit.

The engine is
`bootstrap/gravity/src/gravity/self_hosting/w5_reader_module_executor.gravity`.
The accepted and rejected requests are co-canonical byte-identical `.gravity`
and `.qst` sources.  The source-unit record retains the supplied path,
extension, source kind, and project-relative path as provenance while the
identity input is path-neutral.  The source extension policy is exactly
`.qst` followed by `.gravity`.

The request carries the C2 reader/source-unit record, C3 syntax-origin stream,
C4 macro expansion and hygiene trace, C5 namespace/module artifact, and C6
core-lowering record.  The C2-to-C6 lineage preserves spans, origins,
metadata, bindings, effects, capabilities, profile, target, and extension
provenance.  Source-byte resolution, expansion, lowering, candidate execution,
and independent evidence remain pending.  Hash values are placeholders only;
no final hash or artifact authority is created.

The candidate target is exactly `:llvm-x86_64-linux` with Linux, x86_64, LLVM,
ELF, and `:sysv-amd64` facts.  Unsupported targets are ordered exactly as
`:darwin`, `:darwin-arm64`, `:darwin-x86_64`, and `:windows`; every unsupported
record has `:invokes-clojure? false`, `:links-jvm? false`, and `:fallback?`
false.  Cross-target inference and fallback are denied.  The only residual
execution boundary is the Clojure stage0 stage2 compiler-plan and JVM stage2
runtime harness; no host, filesystem, network, process, native, or container
authority is used.

The rejected fixture exposes total mutators for artifact, schema, verifier,
profile, status, target, residual seed harness, source span, each C2-C6 record,
lineage, provenance, evidence, and authority. Each produces one stable family
diagnostic with a source span, provenance, phase facts, and remediation. The
explicit request-at entry point carries the actual `.gravity` or `.qst` path,
extension, and source kind; no extension is inferred from the fixture name.
The result verifier recomputes from the request and rejects substituted
results with `W5-RM-SUBSTITUTION`.

Later validation command (intentionally not run for this static-only change):

`clojure -M:test --namespace gravity.self-hosting.w5-reader-module-executor-test`
