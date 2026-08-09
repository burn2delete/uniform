# W5 compiler pipeline verifier (C1/C2/C7/C8/C9/C10)

This directory is the bounded executable W5 leaf for the compiler pipeline
evidence spine.  Its engine is
`bootstrap/gravity/src/gravity/compiler/w5_compiler_pipeline_verifier.gravity`.
It checks the D1 canonical order, C1 stage/pass contracts and verifier gates,
C2 reader evidence, C7 typed-core facts, C8 effect and capability legality, C9
ownership/lifetime facts, and C10 safety operation outcomes before target
lowering and artifact emission.

The accepted `.gravity` and `.qst` sources are byte-identical.  The accepted
record covers every required stage artifact, pass contract, source span and
generated-origin chain, the exact candidate target
`:llvm-x86_64-linux` (Linux, x86_64, LLVM, ELF, `:sysv-amd64`), all C7/C8/C9 facts, and
three sensitive operations.  Each operation carries exactly one SAFE1 outcome
from `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`.
The verifier emits `:accepted` for this bounded record while its gate remains
`:blocked` and `:release-eligible? false`; this is a valid verifier record, not
self-host or release acceptance.

The pipeline order is the exact C1 16-stage sequence, including
`:verify-mir` and `:verify-domain-ir`.  Capability and ownership checks stay
inside their owning stages instead of appearing as invented pipeline stages.
Every stage input is the preceding stage output, each stage has one exact
artifact edge, and every pass input/output is bound to both its owning stage
and neighboring pass.  The stage-artifact and pass-contract vectors each have
exactly 16 entries in that canonical order; appended records are rejected even
when each appended record is independently well shaped.

The rejected `.gravity` and `.qst` sources are also byte-identical.  Their
executable mutators cover the named C1 families (`C1-PIPELINE`,
`C1-PASS-CONTRACT`, `C1-EVIDENCE-DROP`, `C1-UNCHECKED-BACKEND`,
`C1-DOMAIN-ANCHOR`, `C1-MANIFEST`, `C1-SELF-HOST`), every C2 reader family,
every C7 type family, every C8 effect/capability family, every C9
ownership/lifetime family, and every C10 safety family.  Mutators change one
fact only; additional direct substitutions exercise source, stage/pass,
target, artifact, identity, provenance, adjacent-stage/pass, and request-key
cross-links. Diagnostics are recomputed as C15 structured artifacts with a
stable semantic id, active lifecycle, canonical failing stage and pass,
primary and related artifacts, typed facts, structured remediation, and
explicit redactions. Valid request spans and generated-origin chains are
copied exactly and checked against the request. Malformed provenance uses one
deterministic explicit fallback, sets the corresponding preservation flag to
false, and records the rejected value and recovery source; it never claims a
synthetic location or origin was preserved. Dedicated mutators cover malformed
request span, origin chain, and their combined fallback. The exported
diagnostic validator recomputes the request's exact first failure and requires
full equality, binding the catalog rule, stable id, message, facts,
remediation, lifecycle, artifacts, and provenance-recovery semantics; a
caller-coherent diagnostic substitution is rejected.

The ordered unsupported target vector is
`[:darwin :darwin-arm64 :darwin-x86_64 :windows]`; every policy has
`:support :unsupported`, `:invokes-clojure? false`, `:links-jvm? false`, and
`:fallback? false`.

Semantic identity is path-neutral: compiler, pipeline, stage, pass, target, and
artifact identities are derived from the request and never from checkout
paths.  Actual source paths are retained only in provenance.  Records always
carry `:clojure-seed-boundary? true`, `:self-hosted? false`, `:release? false`,
`:public-authority? false`, and `:authority :non-authority`.  The stage2 JVM
compiler-artifact plan used by the eventual test runner is a harness boundary,
including the source namespace `:target :jvm` declaration is only that harness
boundary; it is not the candidate artifact target and not a JVM fallback.  Full-language
coverage, seed retirement, independent review, and public/release authority
remain explicitly incomplete residual boundaries.
