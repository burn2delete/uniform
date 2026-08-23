# AGENTS.md

Scope: the entire repository.

This repository is currently the Gravity design and implementation contract, not
yet a compiler/runtime codebase. Treat the documents under `docs/` as normative
inputs for implementation work. Do not replace the contract with assumptions
from a host language, target backend, or ad hoc prototype.

## Source Language Boundary

- Gravity/Uniform is the destination implementation language.
- Clojure is the only permitted temporary bootstrap, seed, and repository
  tooling language. New bootstrap or tooling code must be Clojure.
- Do not add Python. The former Python migration debt has been removed and had
  no implementation or evidence authority.
- Do not introduce another host language to replace Python or extend the seed.
- A Clojure replacement must preserve the old tool's accepted and rejected
  behavior before the corresponding Python is removed. Clojure itself is then
  retired incrementally behind Gravity/Uniform-authored equivalents and the
  governing self-hosting evidence.

Run the boundary gate after adding, removing, or replacing source or tooling:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

## Start Here

Before making project changes, read:

- `README.md` for repository purpose, validation, and generation caveats.
- `docs/README.md` for the phase map and critical pre-implementation set.
- `docs/source-concepts.md` for the compact concept map derived from the PDF.
- `docs/document-sequence.md` or `docs/document-inventory.json` to locate
  owning documents by sequence/id.
- The relevant phase `README.md` and each document that governs the requested
  implementation slice.

For compiler, runtime, package, tooling, or standard-library work, also read the
foundation contracts that apply broadly:

- `docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

Use `tmp/pdfs/gravity-lisp-design.txt` as the local PDF text fallback when a
question requires checking the original source basis. `docs/review-ledger.md`
records the second-pass review standard and phase status.

## Source Of Truth

- `docs/document-inventory.json` is the machine-readable inventory for all 240
  documents.
- `docs/document-sequence.md` is the human navigation index for the same
  sequence.
- Phase `README.md` files are navigation layers, not replacements for the
  documents they link.
- `docs/phase-18-binary-distribution-and-seedless-release/` is an
  implementation roadmap extension for the product release boundary. It is not
  an additional source document in the 240-document inventory.
- The 240 documents are implementation contracts. Prefer adding code/tests that
  cite or follow the relevant document over copying large spec text into new
  files.
- If two docs appear to conflict, resolve terminology through `D3`, architecture
  through `D1`, roadmap/release ordering through `D2`, safety through `D8`, and
  proof/evidence obligations through `D9`. If the conflict remains, make the
  ambiguity explicit instead of guessing.

## Implementation Principles

- Follow `D2` milestone ordering. Each implementation milestone must compile or
  emit something real and reject something real.
- Keep the distinction between profile, target, backend, runtime, effect,
  capability, artifact, and file exactly as defined in `D3`.
- Profiles are compile-time contracts. Do not defer profile, effect,
  capability, ownership, or safety legality to a backend or runtime-only check.
- The canonical pipeline in `D1` is the architectural reference. Optimized or
  fused passes must still expose equivalent inputs, outputs, invalidated facts,
  diagnostics, and artifacts.
- Safe Gravity has no undefined behavior. Dangerous operations must become one
  of `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`; there
  is no implicit fifth outcome.
- Unsafe behavior must be explicit, isolated, audited, and connected to safe API
  boundaries. Preserve metadata such as `:unsafe-island`, `:runtime-checked`,
  and `:ai/human-review` when it is part of manifests or policy records.
- Effects and capabilities are separate. An effect says what code does; a
  capability is the authority to do it.
- Performance work starts from safe semantics. Check elision, fast math,
  target-specific lowering, and realtime claims require surviving proof,
  certificate, benchmark, or manifest evidence as required by `D6` and `D9`.
- EFIR is the semantic carrier for analyzable elementary math. EML is for
  proof, normalization, synthesis, and search; do not treat EML tree identity as
  equality or as the required runtime representation.
- Artifacts matter. Binaries, schemas, workflow graphs, AI manifests, proof
  certificates, diagnostics, benchmark reports, SBOMs, and bootstrap records
  need provenance when the governing document requires it.

## Agent Guardrails: Preventing Over-Engineering

1. **Deliver the Smallest Complete Solution:** Write only the minimum code
   necessary to satisfy the immediate requirements. Do not add unrequested
   features, premature abstractions, or "gold-plating."
2. **Strict Scope Control:** Use only the tools and capabilities necessary for
   the explicit task. Avoid redundant autonomous logic loops or wasting tokens
   on unrequested analysis.
3. **Require Proof of Intent:** Justify all code changes using existing
   repository evidence and current requirements. Never add code to handle
   hypothetical future use cases.
4. **Preserve Surrounding Behavior:** Modify only the exact lines of code
   required to complete the task. Actively preserve all surrounding code and
   avoid unrelated "clean-ups" or improvements.

## Documentation Work

- Keep Markdown and tooling text ASCII unless a task explicitly requires
  otherwise.
- Do not add scaffold or filler markers that the validator rejects to canonical
  docs; see `tools/validate_gravity_docs.clj` for the exact pattern.
- Canonical docs are expected to contain sections equivalent to purpose,
  requirements, dependencies, outputs/artifacts, and conformance/acceptance
  criteria.
- The historical reviewed-source generators were retired after the document
  set was enriched. Do not reconstruct or rerun them over canonical documents;
  Git history preserves them for audit and recovery.
- When reviewing docs, read the phase README and the documents directly. Search
  is useful for drift scans, but it is not a substitute for document review.

## Validation

The structural document and full-language roadmap validators are Clojure
tooling. Run them with the repository's Clojure tests; do not add new Python to
close validation gaps:

```bash
clojure -M:test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

Passing the Clojure suite is necessary but not sufficient; it does not prove
semantic consistency across documents. The language-boundary gate prevents
reintroduction of the retired Python tooling.

## Working Discipline

- Check `git status --short` before editing and avoid touching unrelated files.
- Keep changes scoped to the requested implementation or documentation slice.
- Add positive and negative fixtures, diagnostics, and evidence artifacts when
  the governing document requires them.
- Do not claim release, milestone, safety, performance, or self-hosting support
  without the evidence bundle required by the relevant docs.

## Workstream Lifecycle And Integration

The machine-readable lifecycle policy is
`contracts/workstream-governance.json`; current dispositions are recorded in
`contracts/workstream-ledger.json`. Validate both before proposing or admitting
work:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref origin/main
```

- Use one active candidate per invariant family. A competing attempt must wait
  until the current candidate is integrated, held, rejected, superseded, or
  abandoned.
- After two rejected candidates in one invariant family, stop implementation.
  Record a nonempty architecture decision before another candidate becomes
  active. Do not route downstream work around the failed invariant.
- Move candidates through the recorded lifecycle. A self-audit may find
  defects, but it cannot confer acceptance or integration eligibility.
- Integration eligibility is bound to the exact base commit, candidate commit,
  clean worktree, owned paths, governing contracts, positive and negative
  fixtures, stable diagnostics, successful checks, independent acceptance,
  residual host boundaries, and explicit nonclaims.
- Run the preflight in `integration` mode with the ledger's exact identities
  immediately before integration. Dirty or detached worktrees, missing or
  divergent bases, and identity mismatches fail closed.
- Compare both ancestry and tree identity. If the candidate is already
  identical or tree-equivalent to the base, do not merge or replay it again.
- Held, rejected, superseded, and abandoned work receives no roadmap credit.
  Integrated work receives only the authority explicitly recorded for it; it
  does not imply release, self-hosting, or seed-retirement completion.
