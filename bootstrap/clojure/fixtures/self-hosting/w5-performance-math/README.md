# W5 Wave6 Slice A performance and math execution leaf

This fixture family covers a bounded static `:meta` consumer of D6, PERF1,
PERF5, PERF10, MATH1 through MATH7, and SAFE15 evidence records. It does not run
a benchmark, prove a theorem, select a native implementation, or grant compiler
authority.

Performance measurements are kept separate from semantic and numeric
correctness. Benchmark evidence cannot erase safety checks or certify an
approximation. EFIR remains the runtime semantic carrier. EML is used only for
normalization, proof, synthesis, and search; EML tree identity does not imply
mathematical equality. Certificate and proof links remain pending, so checks
remain present. The performance claim is exact for the meta profile,
`:llvm-x86_64-linux`, pending native runtime, `:safe-optimized`, the closed
`[-1, 1]` input domain, contiguous F64 layout, empty effects/capabilities and
erased checks, and the ordered proof obligations
`:approximation-bound` then `:roundoff-bound`. Benchmark and claim ids must
cross-link exactly; the benchmark evidence contract is frozen to harness
`:perf/vector-sine`, baseline `:c-o3`, pending sample policy, target fingerprint
`:pending-target-fingerprint`, compiler identity `:pending-compiler-identity`,
source identity `:pending-source-identity`, optimization manifest
`:pending-optimization-manifest`, and pending status. Those ownership ids are
checked against the exact request/evidence contract; a benchmark status never
proves semantic or numeric correctness. Unverified and pending certificates
keep all required checks retained.

The accepted request is structurally valid but incomplete, blocked, and
nonauthoritative. The Clojure/JVM boundary is only the stage2 seed harness.
Public, self-hosted, release, full-language, and 240-document authority are
false.

The candidate is exactly Linux/x86_64/LLVM/ELF/`:sysv-amd64` at
`:llvm-x86_64-linux`. Unsupported targets are ordered as `:darwin`,
`:darwin-arm64`, `:darwin-x86_64`, and `:windows`; each denies Clojure
invocation, JVM linking, and fallback.

Rejected mutators cover target, each benchmark harness/baseline/sample,
target-fingerprint/compiler/source/optimization ownership id, semantic,
numeric, EFIR, EML, certificate, check-elision proof, safety mode, domain,
layout, effects, capabilities, erased checks, proof-obligation
substitution/duplicate/order drift, lineage, provenance, evidence, authority,
and result substitution with stable diagnostics. Dedicated provenance mutators
swap the actual `.gravity`/`.qst` suffix while leaving the declared extension
and source kind untouched, yielding the stable `W5-PM-PROVENANCE` diagnostic.
Both canonical request constructors require the actual source path, extension,
and source kind; `.gravity` and `.qst` are invoked independently.

Later focused validation command (not run by this static-only slice):

`clojure -M:test --namespace gravity.self-hosting.w5-performance-math-executor-test`
