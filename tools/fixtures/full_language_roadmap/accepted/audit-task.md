# Fixture

## Tasks

- [x] `FL-P00-T02` Enforce full-language completion rules in roadmap tooling.

## Evidence Ledger

| Date | Agent | Task | Evidence | Result |
| --- | --- | --- | --- | --- |
| 2026-08-23 | Codex | `FL-P00-T02` | `tools/validate_full_language_roadmap.clj`; `tools/validate_gravity_docs.clj`; commands: `clojure -M tools/validate_full_language_roadmap.clj --self-test`, `clojure -M tools/validate_full_language_roadmap.clj`, `clojure -M tools/validate_gravity_docs.clj` | Validator rejects deliberately overclaimed scaffold-only full-language task fixtures and accepts audited control tasks with command evidence. |
