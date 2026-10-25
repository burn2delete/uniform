# Naming adoption evidence and handoff

Baseline: main `c21068a6d39259b7110845bbbcefaf9bf6901232`.
Owner: Spec Kit adoption delegate. Status: draft guidance and sampled audit.

Read the existing constitution, local command preset, template overrides and
compiler phase architecture directly. Sampled reader diagnostics, incremental
compilation, pass-cache and Core source-tree/test declarations and located their
callers with `rg`. Exact sampled paths and migration obligations are in plan.md.
No implementation rename or compatibility change was made.

The constitution and templates record the actual new user decision. Examples
use semantic paths/names; planning IDs appear only as metadata, traceability or
explicit rejected/legacy examples. The lightweight new-change check is human
review of added/renamed implementation paths and introduced declarations in the
candidate diff. It is not an automated parser or a full-tree gate.

Generated skills/preset refresh and final checks are pending at this early draft
snapshot. Existing initial-adoption 78-check, boundary and discovery observations
retain their original scope; they are not naming-policy enforcement evidence.
Literal command parser and complete model workflow remain untested.

Parent has assigned a separate cloud worker to component registry/Reader-first
roadmaps. This lane does not create those files. Link its canonical path only
after parent confirms it; this naming guide does not become a second roadmap.

## Completed successor checks

The naming-aware local preset v0.2.0 was installed by the already evaluated
pinned official v1.2.3 CLI in a fresh external scratch project. Its generated
skills and installed preset were copied into the owned Spec Kit paths.

Actual commands and results:

- `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj`: exit 0; 240 docs,
  19 phase indexes, ASCII, no placeholders.
- `clojure -J-Xmx256m -M .specify/validation/verify.clj /tmp/uniform-spec-kit-venv/bin/specify`:
  exit 0; 82 scaffold checks, including local/installed preset consistency,
  generated skill bodies, ASCII/JSON/links, actual official scratch init,
  preset installation/update and byte-identical skill regeneration.
  The [successor snapshot](scaffold-checks.edn) is separate from the unchanged
  initial-adoption snapshot. The higher count reflects additional checked links,
  not new compiler or naming-enforcement tests.
- `git diff --check`: passed.
- Both documented new-change diff commands against `c21068a6` for `bootstrap`,
  `tools` and `bin` produced no changes: no implementation names are introduced
  or renamed in this increment. Human review of the template examples classified
  semantic names as accepted, planning-ID names as revise, metadata references as
  allowed, and compatibility IDs as preserve. No automatic parser is claimed.

No compiler semantic tests or new language-boundary source checks were needed
for this text-only successor. No fresh model workflow, literal command parser,
legacy rename or public API compatibility run was performed. Existing restricted
work is unchanged. Review and parent integration remain pending.

## Clean cloud continuation

Continue from draft PR379, branch `tooling/spec-kit-semantic-names`; inspect the
exact published head rather than the original PR371 branch. No running work or
pending implementation mutation is handed off. The external scratch CLI is
optional: committed instructions/templates work without it. Do not repeat setup
or globally install tools just to read this handoff. The next independent reviewer
can assess the rule, diff check and sample migration plan. After parent supplies
the cloud component-registry path, link to it rather than authoring a second
registry or Reader roadmap. Legacy implementation renames remain unperformed
owner-coordinated follow-up work.
