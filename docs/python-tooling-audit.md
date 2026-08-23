# Python Tooling Audit And Migration Recommendation

Date: 2026-08-23

## Purpose

This audit classifies the frozen Python tree by whether the repository still
needs the behavior, whether the behavior is trustworthy enough to preserve,
and whether migration means porting or deletion. It does not treat the legacy
Python inventory as evidence that the code is correct.

The audit began with 162 Python files and 70,974 lines: 54 semantic files under
`src/gravity`, 89 production files under `tools`, and 19 Python tests. This
iteration replaces or retires all eighty-nine production tools, all fifty-four
semantic files, and all nineteen Python tests. No tracked Python files remain.

## Audit Method

The audit used five independent questions for each contract component and its
matched paths:

1. Is the path called by a current command, manifest, Clojure source, or test?
2. Does a normative Gravity document require its behavior, or is it historical
   scaffold evidence?
3. Does it test real user behavior, or only validate self-described JSON?
4. Does it have accepted and rejected fixtures, stable diagnostics, bounded
   effects, and output-path controls worth preserving?
5. Is there already a Clojure or Gravity implementation that owns the behavior?

Historical roadmap and artifact references were treated as provenance, not as
live callers. The current live call graph was checked separately from those
records.

## Component Findings

| Component | Current need | Correctness finding | Migration disposition |
| --- | --- | --- | --- |
| `src/gravity/*.py` semantic scaffolds (54 files) | No live product or Clojure caller | These were host-language models and JSON-fixture helpers, not the canonical compiler pipeline | Retired in this iteration; Clojure and Gravity implementations remain the product path |
| Document-coverage semantic modules (16 of the 54) | Historical phase evidence only | They largely verified fixture manifests and counts rather than executing the designed feature | Retired with their uncalled Python phase validators |
| Artifact and phase validators (`tools/validate_*.py`) | Mixed | Many validators accepted JSON records whose booleans asserted the property being checked; this was scaffold consistency, not implementation proof | Uncalled scaffolds were retired; live policy behavior is owned by Clojure gates and tests |
| Phase 00 JSON inventory validators (6 files) | Redundant manifest prerequisites only | They validate self-described artifact fields and path presence, not the normative documents' meaning or executable compiler behavior; they have no independent test suite | Retired in this iteration; the strict Clojure document gate and compiler tests remain live |
| Structural docs and full-language roadmap validators | Live and named by repository guidance | The docs validator accepted duplicate JSON keys, allowed inventory path escape, matched section names as substrings, and hid a Python-to-Python validator dependency | Replaced in this slice with strict Clojure validators and negative tests |
| Project-structure validator and renderer | Redundant coordination layer | The contract tracked removed Python ownership and overlapped the Clojure ownership/workstream controls | Retired; Clojure project-structure tests, ownership EDN, and workstream governance remain |
| Development orchestrators (`verify_development`, Stage3, SH-07, heartbeat, telemetry) | Python wrappers around Clojure runners | Their receipts were explicitly non-authoritative; the Clojure runners already expose the live fixed batches | Python wrappers retired and current docs switched to direct Clojure commands |
| Stage2 authority admission | Not a permitted current command | The repository explicitly said it could not confer authority; its name overstated its admitted status | Retired; Clojure workstream governance and exact-identity preflight own integration checks |
| Coverage matrix generator | Historical reports only | Completion admission was disabled and classification was path/text based rather than executable proof | Retired with its non-authoritative contract; the Clojure roadmap validator owns live overclaim rejection |
| Authoritative-evidence composer | No trusted admission root | The tool itself records that it cannot mint authority; its name is misleading | Do not mechanically port; retain only any needed non-authoritative composition behind a renamed Clojure contract |
| Reviewed document generators | Not part of normal validation and explicitly unsafe to rerun over enriched docs | They rewrite normative source and encode an obsolete generation history | Archive or delete after confirming no recovery obligation; do not port as normal tooling |
| Output publication | Private Python producer helper only | Clojure Stage3 already has bounded atomic receipt publication | Retired with its final Python callers |
| Python tooling/evidence contracts and their tests | Migration bookkeeping only | Both validators encoded mandatory Python components and could not represent valid deletion; the tooling validator also had stale README assertions | Retired in this iteration; the Clojure language-boundary contract remains the freeze and deletion-only gate |
| Baseline, receipt-composition, telemetry, and structure-rendering utilities | No live callers | Each was a closed Python test island or helper of another unused Python command | Retired without replacement |

## Correctness Conclusions

The Python tree must not be translated file for file. A mechanical port would
preserve several wrong boundaries:

- fixture-manifest consistency would continue to be mistaken for executable
  implementation evidence;
- non-authoritative evidence composition would retain authority-sounding names;
- reviewed-source generators would remain easy to run accidentally;
- overlapping Python and Clojure orchestration would continue to drift; and
- validators would keep relying on permissive JSON and textual token searches.

The retired evidence-producer validator hard-coded the complete producer-id set in
Python. After the two reviewed-source producers were correctly retired, the
machine-readable contract could represent 66 remaining producers but the
validator rejected that shrinkage because its Python constants still require
the deleted ids. The Python tooling validator likewise required nonempty
reviewed-generator paths after the last generator was removed. Neither could
govern its own removal, so both contracts and validators were retired rather
than weakened or translated.

The first replacement therefore strengthens behavior rather than copying the
bugs. `tools/validate_gravity_docs.clj` uses strict duplicate-key rejection,
contains inventory paths beneath `docs/`, requires actual heading lines, checks
the 240-entry sequence and unique ids/paths, preserves ASCII and phase-index
checks, and invokes the Clojure full-language roadmap validator. The roadmap
replacement preserves current accepted/rejected fixtures and fail-closed
seed-retirement checks.

## Recommended Migration Sequence

1. Record one tooling-language-migration invariant family in the workstream
   ledger before another implementation slice. Bind each candidate to exact
   paths, fixtures, diagnostics, and nonclaims.
2. Finish the live policy surface first: development orchestration, process
   containment, project-structure coordination, coverage generation, and
   generic output publication. Reuse existing Clojure runners where behavior
   already overlaps.
3. For every live port, capture the Python predecessor's accepted and rejected
   behavior, add independent negative cases for discovered bugs, switch all live
   callers, then delete the Python file in the same candidate.
4. Keep the retired semantic scaffold fixtures as historical evidence only;
   future feature behavior belongs in Clojure or Gravity tests.
5. Retire reviewed document generators and authority-named composers unless a
   current contract proves a continuing need.
6. Keep the language-boundary gate active after final deletion so Python
   cannot be reintroduced.
7. Replace the admitted Clojure tooling incrementally with Gravity/Uniform under
   BOOT7/BOOT8 equivalence and provenance evidence. Do not claim seed retirement
   from Python removal alone.

## Acceptance And Nonclaims

This iteration removes all eighty-nine Python tools, all fifty-four Python
semantic files, and all nineteen Python tests. Three live policy behaviors have
Clojure replacements; obsolete generators, non-authoritative composition,
redundant JSON inventories, semantic scaffolds, and wrapper-only orchestration
were retired. Zero Python does not confer compiler or evidence authority and
does not establish self-hosting, seed retirement, or release eligibility.
