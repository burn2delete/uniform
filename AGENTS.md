# AGENTS.md

Scope: the entire repository.

This repository is currently the Gravity design and implementation contract, not
yet a compiler/runtime codebase. Treat the documents under `docs/` as normative
inputs for implementation work. Do not replace the contract with assumptions
from a host language, target backend, or ad hoc prototype.

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

## Documentation Work

- Keep Markdown and tooling text ASCII unless a task explicitly requires
  otherwise.
- Do not add scaffold or filler markers that the validator rejects to canonical
  docs; see `tools/validate_gravity_docs.py` for the exact pattern.
- Canonical docs are expected to contain sections equivalent to purpose,
  requirements, dependencies, outputs/artifacts, and conformance/acceptance
  criteria.
- Do not rerun `tools/generate_gravity_docs.py` over edited documents unless the
  task is a deliberate full-tree regeneration and you will reapply enrichment
  afterward. `tools/enrich_remaining_docs.py` records the deterministic
  enrichment pass used for earlier incomplete phases.
- When reviewing docs, read the phase README and the documents directly. Search
  is useful for drift scans, but it is not a substitute for document review.

## Validation

Run the structural validator after documentation changes:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Expected output:

```text
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

If that Python path is unavailable, `python3 tools/validate_gravity_docs.py`
should be equivalent in a normal local environment. Passing validation is
necessary but not sufficient; it does not prove semantic consistency across
documents.

## Working Discipline

- Check `git status --short` before editing and avoid touching unrelated files.
- Keep changes scoped to the requested implementation or documentation slice.
- Add positive and negative fixtures, diagnostics, and evidence artifacts when
  the governing document requires them.
- Do not claim release, milestone, safety, performance, or self-hosting support
  without the evidence bundle required by the relevant docs.
