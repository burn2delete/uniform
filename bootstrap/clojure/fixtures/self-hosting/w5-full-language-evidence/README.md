# W5 Wave4 Full-Language Evidence Consumer

This slice is a static :meta consumer for the coordinator-owned full-language
inventory and coverage records. It reads an exact sequence of 240 normative
documents and 38 named `FL-Pxx-Tyy` gap tasks. It does not generate the
inventory, matrix, gap report, or completion attestations.

The request is bound to the reviewed reporting-v1 observation at commit
`5b8dd5b6d987c34b36dc71f3be1dfa54b2ce0d88` and tree
`53561999651023ba439f94bea508d3fe9e663785`. It pins the contract, inventory,
generator, attestation, matrix, gap-report, and markdown-report paths to their
exact raw and semantic SHA-256 identities, exact JSON keysets, predicate
version, and four producer command strings. The frozen observation is
`non-authoritative-observation` with admission disabled pending
`target-coherent-public-native-evidence-v2`; any identity, keyset, counter,
status, or admission substitution is rejected before evidence interpretation.
The matrix is explicitly incomplete (`240` documents, `0` complete, `gapCount`
`240`) and supported surface is not completion.

The accepted fixture is valid evidence of an incomplete, blocked,
non-authoritative state: full-language completion is `0/240`, current
attestations are `0`, every named gap task remains open, and positive and
rejected-specific evidence is pending. Producer booleans, narrative receipts,
source ownership, check-only evidence, replay-only evidence, and global
advancement claims cannot close the state.

The consumer preserves path-bearing provenance and source spans while its
identity projection is checkout-path-neutral. It freezes every ordered
inventory record by exact `sequence`, `id`, `title`, `path`, `phase`,
`phaseName`, and `category`; supplied content IDs remain explicitly structural
consumer inputs and are not cryptographic or completion authority. Matrix and
gap rows repeat and cross-bind every one of those document fields, so a
coherent-looking alternate title, path, phase, phase name, or category cannot
substitute for the frozen inventory. Each row's evidence and diagnostic spans
and provenance IDs are cross-bound to its document row, and paths must end in
the exact inventory path. Byte, line, and column coordinates are bounded,
ordered, and fixed to the deterministic row coordinate, rejecting reversal or
coherent coordinate drift. The gap report's no-owner count is frozen at the
reporting-v1 value `7`. Per-document positive and rejected-specific evidence,
the exact `FULL-<document-id>-PENDING` diagnostic, and the ordered six-gap row
are frozen semantics rather than merely self-consistent row-local values. The
checkout-path-neutral identity includes all matrix evidence/diagnostic records
and all gap-report semantic entries, so coherent dual diagnostic or gap-content
substitution changes identity and is rejected. Malformed top-level spans
produce a deterministic valid diagnostic fallback rather than malformed
diagnostics. Valid-shape top-level substitutions of the governed source ID,
byte bounds, line, or column are rejected by the same exact request-span gate
used by the exported request validator and receive the deterministic fallback
diagnostic span; they cannot reach accepted execution.
Both accepted and rejected constructors take the actual fixture extension so
`.gravity` and `.qst` plans retain their own source path; a mismatched top-level
fixture path is rejected before the reporting observation can be accepted.
Rejected mutators cover missing, extra, duplicate, reordered, ID, sequence,
title, path, phase, phase-name, category, content-ID, and evidence-ID
substitutions; coherent matrix and gap document substitution; reversed line
and column coordinates; missing evidence; provenance, diagnostic, gap-report,
coherent dual diagnostic, coherent gap-content, task, and attestation defects;
valid-shape top-span origin and coordinate substitutions; forbidden evidence
claims; global advancement;
target forgery; authority forgery; and result substitution.

The only stage2 harness target is `:jvm`. Candidate evidence is exactly
`:llvm-x86_64-linux` on Linux/x86_64 with LLVM, ELF, and `:sysv-amd64`.
Unsupported targets are the ordered vector `[:darwin :darwin-arm64
:darwin-x86_64 :windows]`; each explicitly has `:invokes-clojure? false`,
`:links-jvm? false`, `:fallback? false`, and `:support :unsupported`. The consumer remains
`:clojure-seed-boundary? true`, non-authoritative, non-self-hosted, and not
release eligible.

The later static test command is:

```text
clojure -M:test --namespace gravity.self-hosting.w5-full-language-evidence-verifier-test
```

No JVM, Clojure, native, Docker, inventory generation, attestation generation,
commit, or release action is performed by this fixture slice.
