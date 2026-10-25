# Tasks: semantic names adoption handoff

Planning references: D1/D3, SH07/C2/C16. Implementation examples use semantic names.

- [x] T001 Record the user naming decision in `.specify/memory/constitution.md` and `.specify/semantic-names.md`.
- [x] T002 Add spec/plan/task review examples using `core/source_map.clj` and `gravity.core.source-map` in `.specify/templates/overrides/`.
- [x] T003 Map sampled legacy imports/callers/tests/compatibility in `.specify/naming-pilot/plan.md`; perform no implementation rename.
- [x] T004 Regenerate naming-aware Codex commands from `.specify/local-preset/` and record actual checks in `.specify/naming-pilot/evidence.md`.
- [ ] T005 Parent/reviewer accepts this draft and selects any later owned rename transaction; no legacy rename is completed here.

The diff review is limited to new implementation names. Existing debt and real
wire/schema identifiers are classified rather than mass-rewritten or used as a
global stop. No new hooks, schedules, global installs or approval tickets.
