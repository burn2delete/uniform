# W5 Slice C MIR and lowering execution

This bounded `:meta` leaf consumes supplied C11 Gravity MIR, C12 domain-IR
anchors, C13 optimization contracts and invalidation records, C14 lowering
requests, and C15 structured diagnostics. The executable verifier is
deterministic and data-only: it does not invoke LLVM, native code, a JVM
candidate, Docker, Clojure, or a filesystem/network/process authority.

The accepted request proves schema-shaped MIR, an EFIR anchor, a high-risk
optimization pass with fresh proof and translation validation, the exact
Linux/x86_64/LLVM/ELF/`:sysv-amd64` lowering target, and structured source
spans and origins. Lowering is eligible but no target artifact is emitted.
The record remains incomplete, blocked, and non-authoritative: the JVM is only
the stage2 seed harness; native LLVM execution, independent C14 replay, and
independent proof checking remain residual boundaries. The Stage10a C13
evidence boundary is accepted only as explicit C14 rejection evidence and can
never supply executable continuity.

The rejected co-canonical pair mutates one family at a time: malformed MIR,
domain verification, optimization intent, invalidated-fact regeneration,
optimization proof, target tuple, ABI, Stage10a executable continuity,
diagnostic schema, artifact/schema/profile identity, and substituted result.
Diagnostics preserve stable rules, primary and related spans, origins,
structured facts, and remediation. Request constructors require the actual
source path, extension, and kind; `.gravity`/`:gravity` and `.qst`/`:qst`
provenance are checked as separate executable cases. Rejected mutators also
swap each actual path suffix while leaving its declared extension and kind
unchanged; this is a stable `C15-ORIGIN` failure.

The candidate target is frozen to Linux/x86_64/LLVM/ELF/`:sysv-amd64` with no
fallback. Unsupported targets are the ordered vector
`[:darwin :darwin-arm64 :darwin-x86_64 :windows]`; each target explicitly has
`:invokes-clojure? false`, `:links-jvm? false`, and `:fallback? false`. Seed is
true; public, self-hosted, release, executable-continuity, and full-language
credit claims are false. The `.gravity` and `.qst` fixture pairs are
byte-identical co-canonical inputs.

No full-language or 240-document completion credit is claimed. Static audits
and the JVM seed-harness test are the only intended checks; do not run native,
Docker, or external compiler execution as evidence for this leaf.
