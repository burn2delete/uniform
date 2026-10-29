# AGENTS.md

Scope: the entire repository.

This repository contains Gravity's normative design contracts and incremental
compiler implementation with a temporary Clojure bootstrap. Treat `docs/` as
normative inputs. Do not replace their semantics with assumptions from a host
language, target backend, or ad hoc prototype.

## Source Language Boundary

- Gravity/Uniform is the destination implementation language.
- Clojure is the only permitted temporary bootstrap, seed, and repository
  tooling language. New bootstrap or tooling code must be Clojure.
- Do not add Python. The boundary gate rejects Python source, bytecode, and
  executable shebangs; Python has no implementation or evidence authority.
- Do not introduce another host language to extend the seed.
- Clojure is retired incrementally behind Gravity/Uniform-authored equivalents
  and the governing self-hosting evidence.

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
- `docs/development-verification.md` for focused development, affected
  integration tests, and the separate authoritative migration boundary.
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

## Project Accounting Removal

Project cost models, work-unit budgets, producer/caller debits, prospective
reservations, cost proofs and budget-exhaustion refusals are removed as
implementation and admission requirements. Delete their enforcing code,
contracts and budget-only tests; do not raise their constants, rename them as
generic bounds or require replacement accounting before useful work proceeds.
Input node, depth, width, scalar, byte, token and form quotas whose only purpose
is refusing otherwise well-formed input are part of this removal.

Preserve independently meaningful semantics and validation: Unicode/UTF8,
types, index and arity legality, target-format representability, profiles,
effects, capabilities, authentic inputs, native class and callback rules,
metadata, source provenance and tamper detection. A retained numeric condition
must enforce one of those concrete requirements, not a work or cost allowance.
Actual host broker, heap, process-watchdog, capture and platform restrictions
are separate from compiler accounting. External billing and account limits are
not changed by this project instruction.

Managers independently discover and execute work toward their standing goals.
No task-count, failed-attempt allowance, preparation-only stage or discretionary
per-run permission is an ordinary-development prerequisite. Coordinate actual
conflicting writers and incompatible interfaces; the coordinator alone routes
independent acceptance and integrates main. Preserve historical attempts as
history, and never retry, transfer or route around a denied/restricted action.

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
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

Run appropriate focused tests after compiler/tooling changes; reserve the full
suite for a broad integration decision. Validation does not prove semantic
consistency across documents. The language-boundary gate enforces the
repository language policy.

## Working Discipline

- Check `git status --short` before editing and avoid touching unrelated files.
- Keep changes scoped to the requested implementation or documentation slice.
- Add positive and negative fixtures, diagnostics, and evidence artifacts when
  the governing document requires them.
- Ordinary semantic development does not require fresh authority proofs or
  migration fingerprint updates first. Execute current source with accurate
  provenance; keep implementation progress separate from authoritative
  migration and release status, and invalidate stale evidence conservatively.
- Do not claim release, milestone, safety, performance, or self-hosting support
  without the evidence bundle required by the relevant docs.

## Workstream Lifecycle And Integration

The machine-readable lifecycle policy is
`contracts/workstream-governance.json`; current dispositions are recorded in
`contracts/workstream-ledger.json`. These gates apply to governed candidate
admission, not every local edit or focused test:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref origin/main
```

- Prevent conflicting writers in the same files or invariant transaction.
  An immutable candidate awaiting review does not impose an active-candidate
  quota or prevent an isolated successor. Coordinate genuine ownership conflicts.
- Rejected candidates remain rejected evidence. Their count does not create a
  failure allowance, architecture-decision quota or stop on further development.
  Correct actual invariant failures; do not route downstream work around them.
- Move candidates through the recorded lifecycle. A self-audit may find
  defects, but it cannot confer acceptance or integration eligibility.
- Integration eligibility is bound to the exact base commit, candidate commit,
  clean worktree, owned paths, governing contracts, positive and negative
  fixtures, stable diagnostics, successful checks, independent acceptance,
  residual host boundaries, and explicit nonclaims.
- Run the preflight in `integration` mode with the exact complete integration
  base/commit/tree immediately before integration. For later lifecycle metadata,
  also bind the ledger's immutable source commit/tree and workstream id through
  the governed-composition path in `docs/workstream-governance.md`; the exact
  permitted metadata delta requires independent review. Dirty or detached worktrees, missing or
  divergent bases, and identity mismatches fail closed.
- Compare both ancestry and tree identity. If the candidate is already
  identical or tree-equivalent to the base, do not merge or replay it again.
- Held, rejected, superseded, and abandoned work receives no roadmap credit.
  Integrated work receives only the authority explicitly recorded for it; it
  does not imply release, self-hosting, or seed-retirement completion.
