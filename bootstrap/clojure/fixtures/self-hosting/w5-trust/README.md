# W5 Wave3 trust, provenance, and TCB verifier

This leaf owns a bounded Gravity-authored static verifier for bootstrap trust
and provenance. It consumes an exact, versioned request and recomputes the
decision from content identifiers. It does not read files, execute a native
toolchain, sign an artifact, or promote a release.

The accepted pair is valid evidence for an incomplete, fail-closed candidate.
It records exact compiler lineage, source and dependency graphs, an offline
lock, hermetic environments, independent toolchains and recipes, two diverse
rebuild descriptors, trust anchors, attestations, an SBOM with capabilities,
generated sources and binary blobs, canonical signature payloads, revocations,
an unsafe-island audit, a derived TCB delta, stage-equivalence and replay
records, upstream identities, and an independent Sol review slot. The stage3
`r3` recipe is request-level cross-bound to its source graph, compiler,
dependency graph, lock, toolchain, and hermetic environment. Two additional
release rebuild recipes bind each rebuild role to the exact release source,
compiler, dependency graph, lock, toolchain, and environment. Each pending
attestation names one exact rebuild and repeats its artifact content, builder
role, source, compiler, lock, graph, recipe, toolchain, environment, and trust
anchor fields. A recipe swap, coherent cross-role recipe swap, or substitution
of the other existing builder therefore fails closed. The verifier also owns
an exact ordered two-entry signature subject map. Each signature ID is
positionally bound to one
attestation, that attestation's rebuild and build recipe, one signer, and the
signer's exact trust anchor. Switching the first signature to the other
existing rebuild recipe or the other well-formed signer/anchor therefore fails
closed without relying on shape or mere existence checks. The records are
descriptor-only or pending where execution or independent verification is
required; raw and canonical byte hashes are never inferred
from descriptor IDs. Native Linux replay, independent trust anchors,
actual stage equivalence, signing, attestation verification, and independent
Sol review remain open, so the result is always `:incomplete` and
`:non-authority`.

The candidate target is exact `:llvm-x86_64-linux` with Linux, x86_64, LLVM,
ELF, and SysV amd64. Darwin and Windows are unsupported; no JVM, Clojure, cross-target, or
fallback evidence is inferred. `(:target :jvm)` appears only as the explicit
stage2 seed harness declaration. Results retain
`:clojure-seed-boundary? true`, `:self-hosted? false`, `:release? false`, and
`:public-authority? false`.

The rejected pair exports one executable mutator for each trust boundary,
including negative and out-of-order spans, forged repository provenance,
lineage cross-bindings, and replay/TCB subject substitutions:

- upstream content identity;
- lineage cycle, gap, and unreachable node;
- dependency graph, lock, environment, toolchain, and build recipe;
- non-diverse rebuilds, single-field and coherent recipe swaps, and invalid
  trust anchors;
- attestation subject, other-existing-builder, and signature subject/payload
  substitutions;
- SBOM, pending hash-status, and revocation records (including exact compiler,
  package, and provenance coverage with no dropped subjects);
- unsafe-island audit and concealed TCB residual;
- stage equivalence, native replay, target, cross-target, Darwin fallback,
  authority, and independent-review forgery;
- result substitution.

Diagnostics carry a stable `W5-TRUST-*` rule, source span, field, severity,
target/profile, remediation, and non-authority metadata. Semantic identity
contains only content IDs and logical records; checkout, artifact, toolchain,
replay, and review paths are retained only in provenance. `.gravity` and `.qst`
fixtures are byte-identical.

The later integration command is:

```text
clojure -M:test --namespace gravity.self-hosting.w5-trust-provenance-verifier-test
```

This worktree performs static audits only; no JVM, Clojure, native, Docker,
signing, or release command is run here.
