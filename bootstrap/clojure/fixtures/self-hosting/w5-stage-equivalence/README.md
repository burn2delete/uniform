# W5 Wave3 stage equivalence

This fixture pair is the static stage-equivalence leaf for Wave3.  The
verifier source is
`bootstrap/gravity/src/gravity/self_hosting/w5_stage_equivalence_verifier.gravity`.
The `.gravity` and `.qst` requests are co-canonical; they must remain byte
identical.

The request describes stage A, stage B, and stage C, two explicit transitions,
the locked recipe/environment/toolchain records, source/executable/artifact
identities, accepted and rejected transcripts, diagnostics and source spans,
normalized target-independent IR, and an independently identified verifier.
Stage output is bound to the next compiler input at each transition.  Identity
records omit actual filesystem paths; provenance records retain those paths so
path changes cannot change semantic identity while diagnostics remain
actionable.  Every artifact and manifest reference on both comparison sides is
bound to the exact owning stage output record, and every conformance reference
is bound to the exact top-level accepted or rejected transcript ID.  Paired
self-consistent substitutions therefore fail closed.  A declared pending
performance bound is checked independently of the seven comparison products;
an over-bound result is `BOOT7007`.

The seven and only seven comparison modes are:

`artifact`, `manifest`, `diagnostic`, `conformance`, `runtime-output`,
`ir-modulo-id`, and `reviewed-delta`.

The accepted request is structurally valid and all seven static comparisons
are equivalent.  It is deliberately not a completion claim: the result is
`:closure-status :incomplete`, `:completion-status :blocked`, and
`:authority :non-authority`.  Stage executions, independent Sol/native
evidence, native runtime/library linkage, and seed retirement remain pending.
The stage2 compiler-plan harness may use `(:target :jvm)` only to compile this
Gravity leaf.  Candidate evidence is exactly
`:llvm-x86_64-linux` / Linux / x86_64 / LLVM / ELF / `:sysv-amd64`.  The
unsupported target vector is exactly `[:darwin :darwin-arm64
:darwin-x86_64 :windows]`; every entry explicitly has `:fallback? false`,
`:invokes-clojure? false`, and `:links-jvm? false`.  The candidate also has
`:no-fallback? true`, `:candidate-invokes-clojure? false`, and
`:candidate-links-jvm? false`.  JVM, Clojure, cross-target inference, and
fallback are rejected.

The rejected request exports one executable mutator for each rejection family:
request shape and lineage, missing stage output, artifact and manifest drift
(including paired stage-B binding), diagnostic and span drift, conformance and
runtime-output drift (including paired transcript binding), IR normalization
and semantic renaming drift, unreviewed delta, performance-bound failure,
environment/network/lock/toolchain differences, random-seed drift, stage-B
toolchain compiler/source substitutions, diagnostic product-shape and
top-level diagnostic-evidence failures, target/cross-target/fallback,
substituted result, and producer-authority substitution.  Diagnostics use the
stable BOOT7 `BOOT7001`--`BOOT7007` family, classify diagnostic-evidence
failures as `BOOT7003`, and preserve a full source-span map bound to the
comparison path in provenance.

The later exact test command is:

```text
clojure -M:test --namespace gravity.self-hosting.w5-stage-equivalence-verifier-test
```

No JVM, Clojure, native, Sol, or Docker execution is evidence for this static
leaf.  The residual checks and pending evidence are intentionally visible in
the policy, report, and verification records.
